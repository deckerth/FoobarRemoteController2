# AppViewModel Lifecycle

## Overview

`AppViewModel` is the central state holder for the entire application. Unlike a standard AndroidX `ViewModel` obtained through `ViewModelProvider`, it is managed manually via a top-level global variable:

```kotlin
// ViewModel.kt
var appViewModel: AppViewModel? = null
```

Every new instance registers itself into this global on construction, and clears it on destruction. When multiple instances exist briefly (e.g., after `MainActivity` recreation), the stale-VM guards in background workers use this global as the identity check to stop themselves.

---

## Lifecycle Stages

### 1. Construction

**Triggered by:** `MainActivity.onCreate()` on every Activity creation.

```kotlin
// MainActivity.kt
val existingGlobal = ::appViewModel
if (::appViewModel.isInitialized) {
    appViewModel = existingGlobal.get()   // reuse within same instance (defensive)
} else {
    appViewModel = AppViewModel("MainActivity")
    appViewModel.initialize()
}
```

`::appViewModel` is a property reference to `MainActivity`'s own `private lateinit var appViewModel`. Because a new `MainActivity` instance starts with an uninitialized field, `isInitialized` is `false` on every normal creation and recreation, so the else-branch always runs. A fresh `AppViewModel` is always constructed and `initialize()` is always called.

> **Why the check exists at all:** the guard prevents a hypothetical double-initialization if `onCreate()` were somehow called twice on the same `MainActivity` instance, without relying on the package-level global (which could be non-null from a `FoobarMediaService`-owned ViewModel, causing `initialize()` to be skipped — the startup bug this commit fixed).

The `init` block immediately writes `this` into the global:

```kotlin
// ViewModel.kt lines 36–39
init {
    appViewModel = this
    println("FOOBQUERY($owner) View model created")
}
```

`initialize()` instantiates all collaborating objects:

| Object | Role |
|---|---|
| `CredentialsManager` | Loads saved connections from DataStore |
| `HTTPConnector` | Low-level HTTP client |
| `ErrorHandler` | Tracks and auto-heals error state |
| `PlayerAccess` | Sends playback commands to foobar2000 |
| `PlaylistAccess` | Reads/writes playlists |
| `BrowserAccess` | Navigates the music library |
| `PlaylistsViewModel` | Manages playlist list state and the polling loop |
| `BrowserViewModel` | Manages the library browser state |
| `CustomFields` | Stores user-defined metadata columns |
| `VolumeControl` | Holds current volume state |

`CredentialsManager.initialize()` immediately launches a coroutine to load saved connections from DataStore asynchronously.

**Also triggered by:** `FoobarMediaService.onCreate()` — the service constructs `AppViewModel("FoobarMediaService")` only when the global is `null` (i.e., before `MainActivity` runs, or after a crash). Otherwise it reuses the existing global.

---

### 2. Activity Recreation (Orientation Change / Config Change)

On recreation `MainActivity` is a new instance, so `::appViewModel.isInitialized` is `false` and a fresh `AppViewModel` is always created (see §1). The previous ViewModel remains alive until its background workers detect the global has changed and self-cancel via the stale-VM guard (`vm !== appViewModel`).

On phones, portrait orientation is locked at the very start of `onCreate()` to reduce the frequency of recreations in the first place.

---

### 3. Preference Loading and Background Work Start

After construction, `MainActivity`'s Compose content starts two `LaunchedEffect` blocks:

**First effect (runs once):** reads all persisted preferences from DataStore and populates the ViewModel:
- username, encrypted password, IV
- IP address of the foobar2000 server
- release notes marker, custom fields, error-logging flag

**Second effect (runs when `ipAddress` changes):**
```kotlin
// MainActivity.kt lines 193–196
LaunchedEffect(ipAddress) {
    appViewModel.ipAddress = ipAddress
    appViewModel.playlistsViewModel.startPlayerObserver()
}
```

`startPlayerObserver()` launches the two long-lived background workers:

#### QueryAccess (SSE thread)
A raw `Thread` that holds an open `HttpURLConnection` to foobar2000's Server-Sent Events endpoint. It receives push events for playback state and playlist changes, dispatching them back to the ViewModel via callbacks. Each callback guards against stale ViewModels:

```kotlin
if (vm !== appViewModel) { stop(); return }
```

#### PlayerObserver (scheduled executor)
A `ScheduledExecutorService` that fires every **800 ms** to:
1. Call `vm.updatePlayer()` — refreshes player state, updates `mediaSession` metadata, and triggers the system notification.
2. Call `vm.playlistsViewModel.setPlaylists(...)` — syncs the playlist list.

The observer cancels itself when `vm !== appViewModel`.

---

### 4. Active State

While active, `AppViewModel` acts as the single source of truth for all UI state. All observable fields use Jetpack Compose's snapshot system (`mutableStateOf`, `mutableIntStateOf`, `mutableFloatStateOf`) — there is no LiveData or StateFlow.

Key state groups:

| Group | Fields |
|---|---|
| Connection | `ipAddress`, `user`, `password`, `connectionManager`, `ipAddressIsValid` |
| Player | delegated to `PlayerViewModel` (valid, title, artist, album, position, duration, playbackState, playbackMode, …) |
| Playlist display | `displayedPlaylist`, `selectedPlaylist`, `selectedPlaylistName`, `loadingList`, `loadingListProgress` |
| UI mode | `selectedView`, `autoscroll`, `autoScrollIndex`, `playlistEditMode`, `addTitlesMode`, `showTopAppBar` |
| Error / health | `isSick`, `errorLogging`, `askForPassword` |

`updatePlayer()` contains a stale-VM guard at the top:

```kotlin
// ViewModel.kt line 205
if (this !== appViewModel) return
```

This ensures a recreated-but-not-yet-cleared old ViewModel does not drive the global `mediaSession`.

---

### 5. Background Work Stop

Background workers are stopped in two places:

#### `stopObservers()` (manual)
Called explicitly when the app wants to halt polling without destroying the ViewModel — for example, before switching to a different server. Stops `queryAccess` and the `PlayerObserver` via `playlistsViewModel.stopObserver()`.

#### `onCleared()` (lifecycle)
```kotlin
// ViewModel.kt lines 112–124
override fun onCleared() {
    super.onCleared()
    queryAccess?.stop()
    if (::playlistsViewModel.isInitialized) {
        playlistsViewModel.stopObserver()
    }
    if (appViewModel === this) {
        appViewModel = null
    }
}
```

> **Important:** Because `AppViewModel` is not stored in a `ViewModelStore`, the Android framework **never calls `onCleared()` automatically**. It would only be invoked if the ViewModel were obtained through `ViewModelProvider`. In the current design `onCleared()` is dead code from the framework's perspective — cleanup relies entirely on `stopObservers()` and on the background workers' own stale-VM self-cancellation guards.

---

### 6. State Reset (Server Switch)

When the user changes the server address, `clearState()` is called to flush stale data:

```kotlin
// ViewModel.kt lines 144–153
fun clearState() {
    displayedPlaylist = null
    queryAccess?.setPlaylist("")
    selectedPlaylist = ""
    selectedPlaylistName = ""
    selectedView = ViewsWithLayout.PLAYER
    isSick = false
    errorHandler.reset()
    playerViewModel.valid = false
}
```

After `clearState()`, `startPlayerObserver()` is called again to reconnect to the new server.

---

## Sequence Summary

```
App launch
  └─ MainActivity.onCreate()
       ├─ ::appViewModel.isInitialized == false (new instance)
       │    → AppViewModel("MainActivity")  ← init{} sets global
       │    → initialize()                  ← creates all collaborators
       ├─ startForegroundService(FoobarMediaService)
       │    └─ FoobarMediaService.onCreate()
       │         └─ global appViewModel == null?
       │              Yes → AppViewModel("FoobarMediaService")
       │              No  → reuse existing global
       └─ Compose content starts
            ├─ LaunchedEffect(Unit): load preferences → populate ViewModel
            └─ LaunchedEffect(ipAddress): set IP → startPlayerObserver()
                 ├─ Thread: poll checkConnection() until valid
                 ├─ startQueryAccess()  → SSE background Thread (runs until stop())
                 └─ playerObserver.startPlayerObserver()
                      └─ ScheduledExecutor @800ms → updatePlayer() + setPlaylists()

Config change / orientation (phone)
  └─ MainActivity.onCreate() (new instance)
       ├─ ::appViewModel.isInitialized == false → new AppViewModel created
       │    → init{} overwrites global with new VM
       │    → initialize() called
       └─ old workers (QueryAccess, PlayerObserver) detect vm !== appViewModel
            → self-cancel on next tick / callback

Server switch
  └─ stopObservers() → stop SSE thread + cancel executor
  └─ clearState()    → flush stale UI state
  └─ startPlayerObserver() → reconnect to new server

App destroyed
  └─ stopObservers() (called manually; onCleared() is not triggered by framework)
       ├─ queryAccess?.stop()           → SSE thread exits on next readLine()
       └─ playlistsViewModel.stopObserver() → executor.cancel()
  └─ global appViewModel = null
```

---

## Collaborating ViewModels

`AppViewModel` owns the following nested ViewModels as plain Kotlin properties (not managed by `ViewModelProvider`):

| ViewModel | Created in | Purpose |
|---|---|---|
| `PlayerViewModel` | `AppViewModel` constructor | All playback/track state |
| `PlaylistsViewModel` | `initialize()` | Playlist list, observer lifecycle |
| `BrowserViewModel` | `initialize()` | Music library browser state |

`LayoutViewModel` is the only ViewModel in the app that uses `ViewModelProvider` (via the Compose `viewModel()` function). It receives `appViewModel` as a constructor argument and uses it for layout-editor previews.
