package com.deckerth.thomas.foobarremotecontroller2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.model.Player

val list = mutableStateListOf<ITitle>()

fun updateList() {
    Thread() {
        var playlist = PlaylistAccess.getInstance().currentPlaylist.playlist
        if (playlist != null) {
            list.clear()
            list.addAll(playlist)
        }
    }.start()
}

var player by mutableStateOf<Player?>(null)

fun updatePlayer(){
    Thread() {
        player = PlayerAccess.getInstance().playerState
    }.start()
}