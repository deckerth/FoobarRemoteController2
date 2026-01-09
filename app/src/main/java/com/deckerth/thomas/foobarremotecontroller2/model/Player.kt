package com.deckerth.thomas.foobarremotecontroller2.model

import android.annotation.SuppressLint
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

data class Player(
    val label: String,
    val catalog: String,
    val composer: String,
    val album: String,
    val title: String,
    val artist: String,
    val sampleRate: String,
    val genre: String,
    val discNumber: String,
    val track: String,
    var playbackTime: String,
    val playlistId: String,
    val index: String,
    val duration: String,
    var position: String,
    val artworkUrl: String,
    val playbackState: PlaybackState,
    val playbackMode: PlaybackMode,
    val ipAddress: String,
    var invalidCredentials: Boolean = false
) {
    val timestamp: Long = System.currentTimeMillis()

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

    fun getIndex(): Int {
        try {
            val pos = index.toInt()
            return pos
        } catch (ex: NumberFormatException) {
            return -1
        }
    }

    fun clonePlayer(): Player {
        return Player(
            label,
            catalog,
            composer,
            album,
            title,
            artist,
            sampleRate,
            genre,
            discNumber,
            track,
            playbackTime,
            playlistId,
            index,
            duration,
            position,
            artworkUrl,
            playbackState,
            playbackMode,
            ipAddress,
            invalidCredentials
        )
    }
}
