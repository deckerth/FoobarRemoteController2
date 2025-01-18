package com.deckerth.thomas.foobarremotecontroller2.model

import android.annotation.SuppressLint
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.errorHandler
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
    val discNumber: String,
    val track: String,
    val playbackTime: String,
    val playlistId: String,
    val index: String,
    val duration: String,
    var position: String,
    val artworkUrl: String,
    val playbackState: PlaybackState,
    val playbackMode: PlaybackMode
) {

    fun getPos(): Float {
        try {
            val duration = this.duration.toFloat()
            val position = this.position.toFloat()
            return position / duration * 1f
        } catch (ex: NumberFormatException) {
            return 0f
        }
    }

    fun setPos(relativePos : Float) {
        if (!errorHandler.sick())
            CoroutineScope(Dispatchers.IO).launch {
                val absolutePosition = relativePos * duration.toFloat()
                withContext(Dispatchers.Main) { position = absolutePosition.toString() }
                PlayerAccess.getInstance().setPosition(absolutePosition)
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
}
