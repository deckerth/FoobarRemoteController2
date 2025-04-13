package com.deckerth.thomas.foobarremotecontroller2.viewmodel


import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerObserver
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistLifecycleState
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TitleFilter {
    var isActive by mutableStateOf(false)

    private fun setIsActive() {
        isActive = pattern.isNotBlank() || genre.isNotBlank() || highRes
    }

    private var _pattern: String = ""
    var pattern: String
        get() = _pattern
        set(value) {
            _pattern = value
            setIsActive()
        }

    private var _genre: String = ""
    var genre: String
        get() = _genre
        set(value) {
            _genre = value
            setIsActive()
        }

    private var _highRes: Boolean = false
    var highRes: Boolean
        get() = _highRes
        set(value) {
            _highRes = value
            setIsActive()
        }

    fun clear() {
        _pattern = ""
        _genre = ""
        _highRes = false
        isActive = false
    }
}

class PlaylistsViewModel(private val vm: AppViewModel) : ViewModel() {

    private val playlistRegistry = mutableListOf<Playlist>()

    var showFilter by mutableStateOf(false)
    var filterValue by mutableStateOf(TitleFilter())

    private val playerObserver = PlayerObserver(vm, vm.playlistAccess, this)

    /**
     *   @return all known playlists that are not invalid
     *   @see Playlists
     *   @see PlaylistLifecycleState
     */
    val playlists: Playlists
        get() {
            val playlists = Playlists()
            for (list in playlistRegistry)
                if (list.lifecycleState != PlaylistLifecycleState.Invalid)
                    playlists.playlists.add(list.playlistEntity)
            return playlists
        }

    val selectedPlaylistIndex: Int
        get() {
            for ((index, list) in playlistRegistry.withIndex())
                if (list.playlistEntity.playlistId == vm.selectedPlaylist)
                    return index
            return -1
        }

    fun startPlayerObserver() {
        Thread {
            if (vm.ipAddress != null) {
                var count = 0
                do {
                    vm.valid = vm.connector.checkConnection(vm.ipAddress!!)
                    count++
                    if (!vm.valid) {
                        Thread.sleep(500)
                    }
                    if (count > 20) {
                        break
                    }
                } while (!vm.valid)

                println("FOOB ${vm.ipAddress} valid:${vm.valid}")

                playerObserver.observer?.cancel(true)
                if (vm.valid) {
                    playerObserver.startPlayerObserver()
                }
            }
        }.start()
    }

    fun restartObserver() {
        playerObserver.observer?.cancel(true)
        playlistRegistry.clear()
        vm.clearState()
        playerObserver.startPlayerObserver()
    }

    fun getPlaylist(id: String): Playlist {
        var list =
            if (playlistRegistry.isEmpty()) null else playlistRegistry.firstOrNull { it.playlistEntity.playlistId == id }
        if (list == null) {
            list = Playlist(PlaylistEntity(id, "", false, 0))
            playlistRegistry.add(list)
        }
        return list
    }

    fun setPlaylists(newPlaylists: Playlists) {
        // update registry
        var playingPlaylistId = ""

        if (vm.player != null)
            playingPlaylistId = vm.player!!.playlistId

        // Check if any playlist has now a different number of tracks than before
        // Clear changed playlists so that they are loaded from scratch
        for (newEntity in newPlaylists.playlists) {
            val currentEntry = getPlaylist(newEntity.playlistId)
            if (newEntity.noOfTracks != currentEntry.playlistEntity.noOfTracks) {
                if (vm.displayedPlaylist == null || vm.displayedPlaylist!!.playlistEntity.playlistId != newEntity.playlistId) {
                    currentEntry.clear()
                } else
                // Do not clear the playlist if it is currently displayed.
                // It is set to invalid, and will later be loaded again when recompose begins.
                    currentEntry.lifecycleState = PlaylistLifecycleState.RequiresUpdate
                println("FOOB triggerUpdatePlaylist: ${currentEntry.playlistEntity.playlistId}")
            }
            // Update the current entry with the new data
            currentEntry.playlistEntity.apply {
                name = newEntity.name
                isCurrent = newEntity.isCurrent
                noOfTracks = newEntity.noOfTracks
                if (vm.selectedPlaylist == currentEntry.playlistEntity.playlistId)
                    vm.selectedPlaylistName = newEntity.name
            }

            if (vm.selectedPlaylist == "" && newEntity.playlistId == playingPlaylistId) {
                // select first playlist setSelectedPlaylist(entity.playlistId)
                vm.selectedPlaylist = newEntity.playlistId
                vm.selectedPlaylistName = newEntity.name
                vm.displayedPlaylist = getPlaylist(newEntity.playlistId) //.clone()
                println("FOOB setSelectedPlaylist: ${newEntity.playlistId}")
            }
        }

        // process removed playlists
        for (currentList in playlistRegistry) {
            if (!newPlaylists.playlists.any { it.playlistId == currentList.playlistEntity.playlistId }) {
                currentList.playlistEntity.noOfTracks = 0
                currentList.lifecycleState = PlaylistLifecycleState.Invalid
                if (vm.selectedPlaylist == currentList.playlistEntity.playlistId) {
                    vm.selectedPlaylist = ""
                    vm.selectedPlaylistName = ""
                    // vm.displayedPlaylist = null
                }
                println("FOOB invalidating removed playlist ${currentList.playlistEntity.playlistId}")
            }
        }

        // check if playing playlist is still current
        if (vm.player != null && vm.player!!.playlistId.isNotEmpty()) {
            val playingList = getPlaylist(vm.player!!.playlistId)
            if (vm.player!!.getIndex() >= 0 && playingList.titles.count() > vm.player!!.getIndex()) {  // otherwise do not yet check
                val playlistTitle = playingList.titles[vm.player!!.getIndex()]
                if (playlistTitle.album != vm.player?.album || playlistTitle.title != vm.player?.title) {
                    triggerPlaylistUpdate(playingList)
                    println("FOOB clearing changed playlist")
                }
            }
        }

        updatePlaylists()
    }

    private fun getPlaylistToBeUpdated(): Playlist? {
        // check selected playlist
        if (vm.selectedPlaylist.isNotBlank()) {
            val selected = getPlaylist(vm.selectedPlaylist)
            if (selected.lifecycleState == PlaylistLifecycleState.Valid && selected.titles.count() < selected.playlistEntity.noOfTracks)
                return selected
        }
        // check player
        if (vm.player != null && vm.player!!.playlistId.isNotBlank()) {
            if (vm.player!!.playlistId != vm.selectedPlaylist) {
                val played = getPlaylist(vm.player!!.playlistId)
                if (played.lifecycleState == PlaylistLifecycleState.Valid && played.titles.count() < played.playlistEntity.noOfTracks)
                    return played
            }
        }
        return null
    }

    private fun updatePlaylists() {
        if (!vm.loadingList && !vm.errorHandler.sick()) {
            val next = getPlaylistToBeUpdated()
            if (next != null)
                updatePlaylist(next)
        }
    }

    private fun updatePlaylist(playlist: Playlist) {
        vm.loadingList = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var currentPlaylist: Playlist? = playlist
                println("FOOB starting updatePlaylist: ${currentPlaylist!!.playlistEntity.playlistId}, titles: ${currentPlaylist.titles.count()}")
                do {
                    withContext(Dispatchers.Main) { vm.loadingListProgress = currentPlaylist!!.titles.count().toFloat() / currentPlaylist!!.playlistEntity.noOfTracks }
                    // invariant: currentPlaylist is not null
                    val startIndex = currentPlaylist!!.titles.count()
                    val playlistPart = vm.playlistAccess
                        .getPlaylist(currentPlaylist.playlistEntity, startIndex)
                    // validity check
                    if (playlistPart != null && playlistPart.ipAddress == vm.ipAddress && currentPlaylist.titles.count() == startIndex) {
                        for (title in playlistPart.titles)
                            currentPlaylist.addTitle(title)
                        if (currentPlaylist.playlistEntity.playlistId == vm.selectedPlaylist)
                            withContext(Dispatchers.Main) {
                                vm.displayedPlaylist = currentPlaylist
                            }
                        println("FOOB updatePlaylist: ${currentPlaylist.playlistEntity.playlistId}, titles: ${currentPlaylist.titles.count()}")
                    }
                    currentPlaylist = getPlaylistToBeUpdated()
                } while (currentPlaylist != null && !vm.errorHandler.sick())
                withContext(Dispatchers.Main) { vm.loadingList = false }
                println("FOOB updatePlaylist finished")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { vm.loadingList = false }
            }
        }
    }

    private fun triggerPlaylistUpdate(playlistId: String) {
        if (playlistId != "")
            triggerPlaylistUpdate(getPlaylist(playlistId))
    }

    private fun triggerPlaylistUpdate(playlist: Playlist) {
        if (vm.displayedPlaylist == null || vm.displayedPlaylist!!.playlistEntity.playlistId != playlist.playlistEntity.playlistId) {
            playlist.clear()
            updatePlaylists()
        } else
            playlist.lifecycleState = PlaylistLifecycleState.RequiresUpdate
    }

    fun updateList() {
        triggerPlaylistUpdate(vm.selectedPlaylist)
    }

    fun setSelectedPlaylist(id: String) {
        if (id.isNotEmpty()) {
            if (id != vm.selectedPlaylist)
                println("FOOB changing from playlist: $vm.selectedPlaylist to $id")
            vm.selectedPlaylist = id
            val playlist = getPlaylist(id)
            vm.selectedPlaylistName = playlist.playlistEntity.name
            if (playlist.lifecycleState != PlaylistLifecycleState.Valid || playlist.titles.count() < playlist.playlistEntity.noOfTracks)
                vm.displayedPlaylist = null  // invalidate displayed playlist
            else
                vm.displayedPlaylist = playlist

            updatePlaylists()
        }
    }

    fun playlistExists(name: String): Boolean {
        return playlistRegistry.any { it.playlistEntity.name == name && it.lifecycleState != PlaylistLifecycleState.Invalid }
    }

    fun addPlaylist(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                vm.playlistAccess.addPlaylist(noOfValidPlaylists(), name)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        mainActivity,
                        mainActivity.getString(R.string.playlist_added),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        mainActivity,
                        mainActivity.getString(R.string.playlist_adding_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun noOfValidPlaylists(): Int {
        return playlistRegistry.count { it.lifecycleState == PlaylistLifecycleState.Valid || it.lifecycleState == PlaylistLifecycleState.RequiresUpdate }
    }
}
