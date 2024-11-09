package com.deckerth.thomas.foobarremotecontroller2.viewmodel


import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.errorHandler
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists

private val playlistRegistry = mutableListOf<Playlist>()

val playlists: Playlists
    get() {
        val playlists = Playlists()
        for (list in playlistRegistry)
            playlists.playlists.add(list.playlistEntity)
        return playlists
    }

val selectedPlaylistIndex: Int
    get() {
        for ((index, list) in playlistRegistry.withIndex())
            if (list.playlistEntity.playlistId == selectedPlaylist)
                return index
        return -1
    }


fun getPlaylist(id: String): Playlist {
    var list =
        if (playlistRegistry.isEmpty()) null else playlistRegistry.firstOrNull { it.playlistEntity.playlistId == id }
    if (list == null) {
        list = Playlist(PlaylistEntity(id, "", false, 0))
        playlistRegistry.add(list)
    }
    return list
}

fun setPlaylists(playlists: Playlists) {
    // update registry
    var playingPlaylistId = ""

    if (player != null)
        playingPlaylistId = player!!.playlistId

    for (entity in playlists.playlists) {
        val entry = getPlaylist(entity.playlistId)
        if (entity.noOfTracks < entry.playlistEntity.noOfTracks) {
            if (displayedPlaylist == null || displayedPlaylist!!.playlistEntity.playlistId != entity.playlistId) {
                entry.clear()
                updatePlaylists()
            } else entry.valid = false
            println("FOOB invalidatePlaylist: ${entry.playlistEntity.playlistId}")
        }
        entry.playlistEntity.apply {
            name = entity.name
            isCurrent = entity.isCurrent
            noOfTracks = entity.noOfTracks
            if (selectedPlaylist == entry.playlistEntity.playlistId)
                selectedPlaylistName = entity.name
        }

        if (selectedPlaylist == "" && entity.playlistId == playingPlaylistId) {
            // select first playlistsetSelectedPlaylist(entity.playlistId)
            selectedPlaylist = entity.playlistId
            selectedPlaylistName = entity.name
            displayedPlaylist = getPlaylist(entity.playlistId) //.clone()
            println("FOOB setSelectedPlaylist: ${entity.playlistId}")
        }
    }

    // process removed playlists
    for (list in playlistRegistry) {
        if (!playlists.playlists.any { it.playlistId == list.playlistEntity.playlistId }) {
            invalidatePlaylist(list)
            list.playlistEntity.noOfTracks = 0
            println("FOOB invalidating removed playlist ${list.playlistEntity.playlistId}")
        }
    }

    // check if playing playlist is still current
    if (player != null && player!!.playlistId.isNotEmpty()) {
        val playingList = getPlaylist(player!!.playlistId)
        if (player!!.getIndex() >= 0 && playingList.titles.count() > player!!.getIndex()) {  // otherwise do not yet check
            val playlistTitle = playingList.titles[player!!.getIndex()]
            if (playlistTitle.album != player?.album || playlistTitle.title != player?.title) {
                invalidatePlaylist(playingList)
                println("FOOB clearing changed playlist")
            }
        }
    }
    updatePlaylists()
}

fun getPlaylistToBeUpdated(): Playlist? {
    // check selected playlist
    if (selectedPlaylist.isNotBlank()) {
        val selected = getPlaylist(selectedPlaylist)
        if (selected.valid && selected.titles.count() < selected.playlistEntity.noOfTracks)
            return selected
    }
    // check player
    if (player != null) {
        if (player!!.playlistId != selectedPlaylist) {
            val played = getPlaylist(player!!.playlistId)
            if (played.valid && played.titles.count() < played.playlistEntity.noOfTracks)
                return played
        }
    }
    return null
}

fun updatePlaylists() {
    if (!loadingList && !errorHandler.sick()) {
        val next = getPlaylistToBeUpdated()
        if (next != null)
            updatePlaylist(next)
    }
}

private fun updatePlaylist(playlist: Playlist) {
    loadingList = true
    Thread {
        try {
            var currentPlaylist: Playlist? = playlist
            println("FOOB starting updatePlaylist: ${currentPlaylist!!.playlistEntity.playlistId}, titles: ${currentPlaylist.titles.count()}")
            do {
                // invariant: currentPlaylist is not null
                val startIndex = currentPlaylist!!.titles.count()
                val playlistPart = PlaylistAccess.getInstance()
                    .getPlaylist(currentPlaylist.playlistEntity, startIndex)
                // validity check
                if (playlistPart != null && currentPlaylist.titles.count() == startIndex) {
                    for (title in playlistPart.titles)
                        currentPlaylist.addTitle(title)
                    if (playlist.playlistEntity.playlistId == selectedPlaylist)
                        displayedPlaylist = currentPlaylist //.clone()
                    println("FOOB updatePlaylist: ${currentPlaylist.playlistEntity.playlistId}, titles: ${currentPlaylist.titles.count()}")
                }
                currentPlaylist = getPlaylistToBeUpdated()
            } while (currentPlaylist != null && !errorHandler.sick())
            loadingList = false
            println("FOOB updatePlaylist finished")
        } catch (e: Exception){
            loadingList = false
        }
    }.start()
}

fun invalidatePlaylist(playlistId: String) {
    if (playlistId != "")
        invalidatePlaylist(getPlaylist(playlistId))
}

fun invalidatePlaylist(playlist: Playlist) {
    if (displayedPlaylist == null || displayedPlaylist!!.playlistEntity.playlistId != playlist.playlistEntity.playlistId) {
        playlist.clear()
        updatePlaylists()
    } else playlist.valid = false
}

fun setSelectedPlaylist(id: String) {
    if (id.isNotEmpty()) {
        if (id != selectedPlaylist)
            println("FOOB changing from playlist: $selectedPlaylist to $id")
        selectedPlaylist = id
        displayedPlaylist = getPlaylist(id) //.clone()
        selectedPlaylistName = displayedPlaylist!!.playlistEntity.name
        updatePlaylists()
    }
}
