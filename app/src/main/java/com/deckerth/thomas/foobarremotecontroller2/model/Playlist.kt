package com.deckerth.thomas.foobarremotecontroller2.model

class Playlist(var playlistEntity: PlaylistEntity) {
    val mTitles = mutableListOf<ITitle>()

    val albums = mutableListOf<Album>()

    fun clear() {
        mTitles.clear()
        albums.clear()
    }

    fun addTitle(title: ITitle) {
        mTitles.add(title)

        if (mTitles.count() == 1) {
            val currentAlbum = Album(title)
            albums.add(currentAlbum)
            currentAlbum.addTitle(title)
        } else {
            var currentAlbum = albums[albums.count()-1]
            if (title.album == currentAlbum.originalTitle.album){
                currentAlbum.addTitle(title)
            }else{
                currentAlbum = Album(title)
                currentAlbum.addTitle(title)
                albums.add(currentAlbum)
            }
        }
    }

    fun clone(): Playlist {
        val playlist = Playlist(playlistEntity)
        mTitles.forEach {
            playlist.addTitle(it.clone())
        }
        return playlist
    }
}
