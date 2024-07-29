package com.deckerth.thomas.foobarremotecontroller2.model

enum class PlaybackState {
    STOPPED,
    PLAYING,
    PAUSED
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
    val playbackState: PlaybackState
) {

    fun getPos(): Float {
        val duration = this.duration.toFloat()
        val position = this.position.toFloat()
        return position / duration * 1f
    }
}
