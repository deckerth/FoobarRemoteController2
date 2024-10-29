package com.deckerth.thomas.foobarremotecontroller2.model

class Playlist(val playlistEntity: PlaylistEntity) {
    private val mTitles = mutableListOf<ITitle>()

    var albums = mutableListOf<Album>()

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
}
