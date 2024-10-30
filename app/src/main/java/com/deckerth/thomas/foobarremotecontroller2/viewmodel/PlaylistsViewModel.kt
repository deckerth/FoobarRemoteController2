package com.deckerth.thomas.foobarremotecontroller2.viewmodel

import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists

private val playlistRegistry = mutableListOf<Playlist>()

fun getPlaylist(id: String): Playlist {
    var list = playlistRegistry.firstOrNull { it.playlistEntity.playlistId == id }
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
            entry.clear()
            println("FOOB invalidatePlaylist: ${entry.playlistEntity.playlistId}")
        }
        entry.playlistEntity.apply {
            name = entity.name
            isCurrent = entity.isCurrent
            noOfTracks = entity.noOfTracks
        }

        if (selectedPlaylist == "" && entity.playlistId == playingPlaylistId) {
            // select first playlistsetSelectedPlaylist(entity.playlistId)
            selectedPlaylist = entity.playlistId
            displayedPlaylist = getPlaylist(entity.playlistId) //.clone()
            println("FOOB setSelectedPlaylist: ${entity.playlistId}")
        }
    }

    // process removed playlists
    for (list in playlistRegistry) {
        if (!playlists.playlists.any { it.playlistId == list.playlistEntity.playlistId }) {
            list.clear()
            list.playlistEntity.noOfTracks = 0
            println("FOOB invalidating removed playlist ${list.playlistEntity.playlistId}")
        }
    }

    // check if playing playlist is still current
    if (player != null) {
        val playingList = getPlaylist(player!!.playlistId)
        if (playingList.mTitles.count() >= player!!.getIndex()) {  // otherwise do not yet check
            val playlistTitle = playingList.mTitles[player!!.getIndex()]
            if (playlistTitle.album != player?.album || playlistTitle.title != player?.title) {
                playingList.clear()
                println("FOOB clearing changed playlist")
            }

        }
    }

    updateIfRequired()
}

fun getPlaylistToBeUpdated(): Playlist? {
    // check selected playlist
    if (selectedPlaylist.isNotBlank()) {
        val selected = getPlaylist(selectedPlaylist)
        if (selected.mTitles.count() < selected.playlistEntity.noOfTracks)
            return selected
    }
    // check player
    if (player != null) {
        if (player!!.playlistId != selectedPlaylist) {
            val played = getPlaylist(player!!.playlistId)
            if (played.mTitles.count() < played.playlistEntity.noOfTracks)
                return played
        }
    }
    return null
}

private fun updateIfRequired() {
    if (!loadingList) {
        val next = getPlaylistToBeUpdated()
        if (next != null)
            updatePlaylist(next)
    }
}

private fun updatePlaylist(playlist: Playlist) {
    loadingList = true
    Thread {
        var currentPlaylist: Playlist? = playlist
        println("FOOB starting updatePlaylist: ${currentPlaylist!!.playlistEntity.playlistId}, titles: ${currentPlaylist.mTitles.count()}")
        do {
            // invariant: currentPlaylist is not null
            val startIndex = currentPlaylist!!.mTitles.count()
            val playlistPart = PlaylistAccess.getInstance()
                .getPlaylist(currentPlaylist.playlistEntity, startIndex)
            // validity check
            if (currentPlaylist.mTitles.count() == startIndex) {
                for (title in playlistPart.mTitles)
                    currentPlaylist.addTitle(title)
                if (playlist.playlistEntity.playlistId == selectedPlaylist)
                    displayedPlaylist = currentPlaylist //.clone()
                println("FOOB updatePlaylist: ${currentPlaylist.playlistEntity.playlistId}, titles: ${currentPlaylist.mTitles.count()}")
            }
            currentPlaylist = getPlaylistToBeUpdated()
        } while (currentPlaylist != null)
        loadingList = false
        println("FOOB updatePlaylist finished")
    }.start()
}

fun invalidatePlaylist(playlistId: String) {
    if (playlistId != "") {
        val playlist = getPlaylist(playlistId)
        playlist.clear()
        updateIfRequired()
    }
}

fun setSelectedPlaylist(id: String) {
    selectedPlaylist = id
    displayedPlaylist = getPlaylist(id) //.clone()
    updateIfRequired()
}