package com.deckerth.thomas.foobarremotecontroller2.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Playlist(var playlistEntity: PlaylistEntity) {
    val titles = mutableListOf<ITitle>()
    val albums = mutableListOf<Album>()
    var valid by mutableStateOf(true)
    var ipAddress: String = ""

    fun clear() {
        titles.clear()
        albums.clear()
        valid = true
    }

    fun addTitle(title: ITitle) {
        titles.add(title)

        if (titles.count() == 1) {
            val currentAlbum = Album(title)
            albums.add(currentAlbum)
            currentAlbum.addTitle(title)
        } else {
            var currentAlbum = albums[albums.count() - 1]
            if (title.album == currentAlbum.originalTitle.album) {
                currentAlbum.addTitle(title)
            } else {
                currentAlbum = Album(title)
                currentAlbum.addTitle(title)
                albums.add(currentAlbum)
            }
        }
    }

    fun getTitle(index: Int): ITitle? {
        return titles.find { it.index == index }
    }

}
