package com.deckerth.thomas.foobarremotecontroller2.model

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
    val position: String,
    val artworkUrl: String,
    val playbackState: PlaybackState,
    val playbackMode: PlaybackMode
) {

    fun getPos(): Float {
        val duration = this.duration.toFloat()
        val position = this.position.toFloat()
        return position / duration * 1f
    }

    fun getNiceDuration(): String{
        val duration = duration.toFloat().toInt()
        val minutes:Int = duration/60
        val seconds:Int = duration%60
        return String.format("%01d:%02d", minutes, seconds)
    }
}
