package com.deckerth.thomas.foobarremotecontroller2.viewmodel

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

enum class PlaybackState {
    STOPPED,
    PLAYING,
    PAUSED
}

enum class PlaybackMode {
    DEFAULT,
    REPEAT_PLAYLIST,
    REPEAT_TRACK,
    RANDOM,
    SHUFFLE_TRACKS,
    SHUFFLE_ALBUMS,
    SHUFFLE_FOLDERS
}

class PlayerViewModel : ViewModel() {
    var valid by mutableStateOf(false)

    var label by mutableStateOf("")
    var catalog by mutableStateOf("")
    var composer by mutableStateOf("")
    var album by mutableStateOf("")
    var albumArtist by mutableStateOf("")
    var title by mutableStateOf("")
    var artist by mutableStateOf("")
    var sampleRate by mutableStateOf("")
    var genre by mutableStateOf("")
    var discNumber by mutableStateOf("")
    var track by mutableStateOf("")
    var playbackTime by mutableStateOf("")
    var playlistId by mutableStateOf("")
    var index by mutableStateOf("")
    var duration by mutableStateOf("")
    var position by mutableStateOf("")
    var artworkUrl by mutableStateOf("")
    var playbackState by mutableStateOf(PlaybackState.STOPPED)
    var playbackMode by mutableStateOf(PlaybackMode.DEFAULT)
    var ipAddress by mutableStateOf("")
    var invalidCredentials by mutableStateOf(false)

    var lastKnownPosition by mutableStateOf("")
    var lastKnownPlaybackTime by mutableStateOf("")
    var lastUpdateTimestamp by mutableLongStateOf(0L)

    fun update(
        label: String,
        catalog: String,
        composer: String,
        album: String,
        title: String,
        artist: String,
        albumArtist: String,
        sampleRate: String,
        genre: String,
        discNumber: String,
        track: String,
        playbackTime: String,
        playlistId: String,
        index: String,
        duration: String,
        position: String,
        artworkUrl: String,
        playbackState: PlaybackState,
        playbackMode: PlaybackMode,
        ipAddress: String,
        invalidCredentials: Boolean
    ) {
        this.valid = true
        this.label = label
        this.catalog = catalog
        this.composer = composer
        this.album = album
        this.albumArtist = albumArtist
        this.title = title
        this.artist = artist
        this.sampleRate = sampleRate
        this.genre = genre
        this.discNumber = discNumber
        this.track = track
        this.playbackTime = playbackTime
        this.playlistId = playlistId
        this.index = index
        this.duration = duration
        this.position = position
        this.artworkUrl = artworkUrl
        this.playbackState = playbackState
        this.playbackMode = playbackMode
        this.ipAddress = ipAddress
        this.invalidCredentials = invalidCredentials
        this.lastKnownPosition = position
        this.lastKnownPlaybackTime = playbackTime
        this.lastUpdateTimestamp = System.currentTimeMillis()
    }

    fun updatePlayer() {
        if (!valid) return

        if (!duration.isBlank() && playbackState == PlaybackState.PLAYING) {
            val durationFloat = duration.toFloat()
            val positionFloat = lastKnownPosition.toFloat()

            val now = System.currentTimeMillis()
            val elapsedSeconds = (now - lastUpdateTimestamp) / 1000f
            var newPosition = positionFloat + elapsedSeconds
            if (newPosition > durationFloat) newPosition = durationFloat
            position = newPosition.toString()

            val timeParts = lastKnownPlaybackTime.split(":")
            if (timeParts.size == 2) {
                try {
                    // Convert parts to numbers and calculate total seconds
                    val minutes = timeParts[0].toLong()
                    val seconds = timeParts[1].toLong()
                    val currentTotalSeconds = (minutes * 60) + seconds

                    var newTotalSeconds = currentTotalSeconds + elapsedSeconds
                    if (newTotalSeconds > durationFloat) newTotalSeconds = durationFloat

                    val newPlaybackTimeSeconds = newTotalSeconds.roundToInt()

                    // Format the new total seconds back into a "minutes:seconds" string
                    val newMinutes = newPlaybackTimeSeconds / 60
                    val newSeconds = newPlaybackTimeSeconds % 60
                    playbackTime =
                        String.format(Locale.getDefault(), "%d:%02d", newMinutes, newSeconds)

                } catch (e: NumberFormatException) {
                    // Handle cases where the string is not in the expected format
                    // For now, we'll just leave the original time
                }
            }
        }
    }


    fun getIndex(): Int {
        try {
            val pos = index.toInt()
            return pos
        } catch (ex: NumberFormatException) {
            return -1
        }
    }

    fun setPos(vm: AppViewModel, relativePos: Float) {
        if (!vm.errorHandler.sick() && duration.isNotBlank())
            CoroutineScope(Dispatchers.IO).launch {
                val absolutePosition = relativePos * duration.toFloat()
                withContext(Dispatchers.Main) { position = absolutePosition.toString() }
                vm.playerAccess.setPosition(absolutePosition)
            }
    }

    @SuppressLint("DefaultLocale")
    fun getNiceDuration(): String {
        try {
            val duration = duration.toFloat().toInt()
            val minutes: Int = duration / 60
            val seconds: Int = duration % 60
            return String.format("%01d:%02d", minutes, seconds)
        } catch (ex: NumberFormatException) {
            return ""
        }
    }

    fun getPos(): Float {
        try {
            val duration = this.duration.toFloat()
            val position = this.position.toFloat()
            return if (duration == 0f)
                0f
            else
                position / duration * 1f
        } catch (ex: NumberFormatException) {
            return 0f
        }
    }
}
