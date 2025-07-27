package com.deckerth.thomas.foobarremotecontroller2.viewmodel

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.deckerth.thomas.foobarremotecontroller2.connector.BrowserAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.ErrorHandler
import com.deckerth.thomas.foobarremotecontroller2.connector.HTTPConnector
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess
import com.deckerth.thomas.foobarremotecontroller2.foobarMediaService
import com.deckerth.thomas.foobarremotecontroller2.mediaSession
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.VolumeControl
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.updateColorScheme
import kotlin.math.floor

private var instance: AppViewModel? = null
val viewModelInstance: AppViewModel get() {
    if (instance == null) {
        instance = AppViewModel()
        instance!!.initialize()
    }
    return instance!!
}

enum class PlaylistEditOperation {
    REMOVE, COPY
}

class AppViewModel : ViewModel() {

    init {
        instance = this
    }

    var ipAddress: String? = null
    var valid = false

    lateinit var errorHandler: ErrorHandler
    lateinit var playerAccess: PlayerAccess
    lateinit var playlistAccess: PlaylistAccess
    lateinit var browserAccess: BrowserAccess
    lateinit var playlistsViewModel: PlaylistsViewModel
    lateinit var browserViewModel: BrowserViewModel
    lateinit var connector: HTTPConnector
    lateinit var foobVolumeControl: VolumeControl

    var autoscroll by mutableStateOf(true)
    var enforceAutoscroll by mutableStateOf(false)
    private var autoscrollSave by mutableStateOf(true)
    var autoScrollIndex by mutableIntStateOf(0)
    var displayedPlaylist by mutableStateOf<Playlist?>(null)
    var loadingList by mutableStateOf(false)
    var loadingListProgress by mutableFloatStateOf(0f)
    var selectedView by mutableStateOf(ViewsWithLayout.PLAYER)
    var selectedPlaylist by mutableStateOf("")
    var selectedPlaylistName by mutableStateOf("")
    var isSick by mutableStateOf(false)
    var playlistEditMode by mutableStateOf(false)
    var playlistEditOperation by mutableStateOf(PlaylistEditOperation.REMOVE)
    var displayToastOnPlaylistPage by mutableStateOf(false)
    var toastOnPlaylistPageMessage by mutableStateOf("")
    var addTitlesMode by mutableStateOf(false)
    var showTopAppBar by mutableStateOf(true)
    var noOfSelectedTitles by mutableIntStateOf(0)
    var titleToRemoveWasSelected by mutableStateOf(false)
    var createPlaylistRequest by mutableStateOf(false)

    fun initialize() {
        connector = HTTPConnector(this)
        errorHandler = ErrorHandler(this)
        playerAccess = PlayerAccess(this)
        playlistAccess = PlaylistAccess(this)
        browserAccess = BrowserAccess(this)
        playlistsViewModel = PlaylistsViewModel(this)
        browserViewModel = BrowserViewModel(this)
        foobVolumeControl = VolumeControl(false, 0, 1, "db", 0)
    }

    fun clearState() {
        displayedPlaylist = null
        selectedPlaylist = ""
        selectedPlaylistName = ""
        selectedView = ViewsWithLayout.PLAYER
        isSick = false
        errorHandler.reset()
    }

    fun increaseNoOfTitlesToRemove() {
        noOfSelectedTitles++
    }

    fun decreaseNoOfTitlesToRemove() {
        noOfSelectedTitles--
    }

    fun enablePlaylistEditMode(operation: PlaylistEditOperation) {
        if (playlistEditMode) return
        showTopAppBar = false
        autoscrollSave = autoscroll
        autoscroll = false
        playlistEditOperation = operation
        //appViewModel.playlistEditMode = true - Is done later in a LaunchedEffect to realize a delay
    }

    fun disablePlaylistEditMode(resetSelection: Boolean) {
        if (!playlistEditMode) return
        playlistEditMode = false
        showTopAppBar = true
        autoscroll = autoscrollSave
        if (resetSelection) // not done when titles shall be removed / copied
            if (displayedPlaylist != null)
                displayedPlaylist!!.resetSelectedTracks(this)
    }

    fun disableAddTitlesMode() {
        if (!addTitlesMode) return
        addTitlesMode = false
        showTopAppBar = true
        autoscroll = autoscrollSave
        if (displayedPlaylist != null)
            displayedPlaylist!!.resetSelectedTracks(this)
    }

    fun getCurrentAlbumIndex(): Int {
        if (player == null || player!!.getIndex() == -1 || displayedPlaylist == null || displayedPlaylist!!.albums.isEmpty())
            return -1
        for ((index, album) in displayedPlaylist!!.albums.withIndex()) {
            if (album.hasIndex(player!!.getIndex()))
                return index
        }
        return -1
    }

    var player by mutableStateOf<Player?>(null)

    fun updatePlayer() {
        val newPlayer = playerAccess.getPlayerState()
        if (newPlayer.ipAddress != ipAddress) return
        player = newPlayer
        val currentAlbumIndex = getCurrentAlbumIndex()
        if (autoscroll && player != null &&
            displayedPlaylist != null &&
            displayedPlaylist!!.albums.isNotEmpty() &&
            currentAlbumIndex != autoScrollIndex &&
            currentAlbumIndex != -1
        ) {
            autoScrollIndex = currentAlbumIndex
        }
        if (mediaSession != null && player != null) {
            val state = when (player!!.playbackState) {
                PlaybackState.STOPPED -> PlaybackStateCompat.STATE_STOPPED
                PlaybackState.PLAYING -> PlaybackStateCompat.STATE_PLAYING
                PlaybackState.PAUSED -> PlaybackStateCompat.STATE_PAUSED
            }

            if (player!!.getIndex() != -1) {
                val bitmap = connector.getBitmapFromURL(player!!.artworkUrl)
                if (bitmap != null) {
                    // Update the color scheme with the new bitmap
                    updateColorScheme(bitmap)
                }
                mediaSession!!.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, player!!.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, player!!.artist)
                        .putBitmap(
                            MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                            bitmap
                        )
                        .putLong(
                            MediaMetadataCompat.METADATA_KEY_DURATION,
                            (floor(player!!.duration.toDouble() * 1000)).toLong()
                        )
                        .build()
                )
                mediaSession!!.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(
                            state,
                            floor(player!!.position.toDouble() * 1000).toLong(),
                            1f
                        )
                        .setActions(
                            (if (state == PlaybackStateCompat.STATE_PLAYING) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY) or
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                        .build()
                )
            } else if (player!!.playbackState != PlaybackState.STOPPED) {// no longer in playlist -> no image
                mediaSession!!.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, player!!.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, player!!.artist)
                        .putLong(
                            MediaMetadataCompat.METADATA_KEY_DURATION,
                            (floor(player!!.duration.toDouble() * 1000)).toLong()
                        )
                        .build()
                )
                mediaSession!!.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(
                            state,
                            floor(player!!.position.toDouble() * 1000).toLong(),
                            1f
                        )
                        .setActions(
                            (if (state == PlaybackStateCompat.STATE_PLAYING) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY) or
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                        .build()
                )
            } else
                mediaSession!!.setMetadata(null)

            println("FOOB updatePlayer: ${player!!.position}")

            foobarMediaService?.updateNotification()
        }
    }
}