package com.deckerth.thomas.foobarremotecontroller2.model

class Playlist(val playlistEntity: PlaylistEntity) {
    private val mTitles = mutableListOf<ITitle>()

    fun clear() {
        mTitles.clear()
    }

    fun addTitle(title: ITitle) {
        mTitles.add(title)
    }

    val albums: List<Album>
        get() {
            if (mTitles.isNotEmpty()) {
                val albums = mutableListOf<Album>()
                var currentAlbum = Album(mTitles[0])
                albums.add(currentAlbum)
                playlist.forEach { iTitle ->
                    if (iTitle.album == currentAlbum.originalTitle.album){
                        currentAlbum.titles.add(iTitle)
                    }else{
                        currentAlbum = Album(iTitle)
                        currentAlbum.titles.add(iTitle)
                        albums.add(currentAlbum)
                    }
                }
                return albums
            }else
                return listOf()
        }

    val playlist: List<ITitle>
        get() = mTitles
}
