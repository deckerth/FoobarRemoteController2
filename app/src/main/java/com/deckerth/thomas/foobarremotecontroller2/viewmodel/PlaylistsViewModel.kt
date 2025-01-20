package com.deckerth.thomas.foobarremotecontroller2.viewmodel


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerObserver
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistsViewModel(private val vm: AppViewModel) : ViewModel() {
    private val playlistRegistry = mutableListOf<Playlist>()

    var showFilter = mutableStateOf(false)
    var filterValue = mutableStateOf("")

    private val playerObserver = PlayerObserver(vm, vm.playlistAccess, this)

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
                if (list.playlistEntity.playlistId == vm.selectedPlaylist)
                    return index
            return -1
        }

    fun startPlayerObserver() {
        Thread {
            if (vm.ipAddress != null) {
                var count = 0
                do {
                    vm.valid = vm.connector.checkConnection(vm.ipAddress!!)
                    count++
                    if (!vm.valid) {
                        Thread.sleep(500)
                    }
                    if (count > 20) {
                        break
                    }
                } while (!vm.valid)

                println("FOOB ${vm.ipAddress} valid:${vm.valid}")

                playerObserver.observer?.cancel(true)
                if (vm.valid) {
                    playerObserver.startPlayerObserver()
                }
            }
        }.start()
    }

    fun restartObserver() {
        playerObserver.observer?.cancel(true)
        playlistRegistry.clear()
        vm.clearState()
        playerObserver.startPlayerObserver()
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

        if (vm.player != null)
            playingPlaylistId = vm.player!!.playlistId

        for (entity in playlists.playlists) {
            val entry = getPlaylist(entity.playlistId)
            if (entity.noOfTracks < entry.playlistEntity.noOfTracks) {
                if (vm.displayedPlaylist == null || vm.displayedPlaylist!!.playlistEntity.playlistId != entity.playlistId) {
                    entry.clear()
                    updatePlaylists()
                } else entry.valid = false
                println("FOOB invalidatePlaylist: ${entry.playlistEntity.playlistId}")
            }
            entry.playlistEntity.apply {
                name = entity.name
                isCurrent = entity.isCurrent
                noOfTracks = entity.noOfTracks
                if (vm.selectedPlaylist == entry.playlistEntity.playlistId)
                    vm.selectedPlaylistName = entity.name
            }

            if (vm.selectedPlaylist == "" && entity.playlistId == playingPlaylistId) {
                // select first playlist setSelectedPlaylist(entity.playlistId)
                vm.selectedPlaylist = entity.playlistId
                vm.selectedPlaylistName = entity.name
                vm.displayedPlaylist = getPlaylist(entity.playlistId) //.clone()
                println("FOOB setSelectedPlaylist: ${entity.playlistId}")
            }
        }

        // process removed playlists
        for (list in playlistRegistry) {
            if (!playlists.playlists.any { it.playlistId == list.playlistEntity.playlistId }) {
                list.playlistEntity.noOfTracks = 0
                invalidatePlaylist(list)
                println("FOOB invalidating removed playlist ${list.playlistEntity.playlistId}")
            }
        }

        // check if playing playlist is still current
        if (vm.player != null && vm.player!!.playlistId.isNotEmpty()) {
            val playingList = getPlaylist(vm.player!!.playlistId)
            if (vm.player!!.getIndex() >= 0 && playingList.titles.count() > vm.player!!.getIndex()) {  // otherwise do not yet check
                val playlistTitle = playingList.titles[vm.player!!.getIndex()]
                if (playlistTitle.album != vm.player?.album || playlistTitle.title != vm.player?.title) {
                    invalidatePlaylist(playingList)
                    println("FOOB clearing changed playlist")
                }
            }
        }
        updatePlaylists()
    }

    private fun getPlaylistToBeUpdated(): Playlist? {
        // check selected playlist
        if (vm.selectedPlaylist.isNotBlank()) {
            val selected = getPlaylist(vm.selectedPlaylist)
            if (selected.valid && selected.titles.count() < selected.playlistEntity.noOfTracks)
                return selected
        }
        // check player
        if (vm.player != null && vm.player!!.playlistId.isNotBlank()) {
            if (vm.player!!.playlistId != vm.selectedPlaylist) {
                val played = getPlaylist(vm.player!!.playlistId)
                if (played.valid && played.titles.count() < played.playlistEntity.noOfTracks)
                    return played
            }
        }
        return null
    }

    fun updatePlaylists() {
        if (!vm.loadingList && !vm.errorHandler.sick()) {
            val next = getPlaylistToBeUpdated()
            if (next != null)
                updatePlaylist(next)
        }
    }

    private fun updatePlaylist(playlist: Playlist) {
        vm.loadingList = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var currentPlaylist: Playlist? = playlist
                println("FOOB starting updatePlaylist: ${currentPlaylist!!.playlistEntity.playlistId}, titles: ${currentPlaylist.titles.count()}")
                do {
                    // invariant: currentPlaylist is not null
                    val startIndex = currentPlaylist!!.titles.count()
                    val playlistPart = vm.playlistAccess
                        .getPlaylist(currentPlaylist.playlistEntity, startIndex)
                    // validity check
                    if (playlistPart != null && playlistPart.ipAddress == vm.ipAddress && currentPlaylist.titles.count() == startIndex) {
                        for (title in playlistPart.titles)
                            currentPlaylist.addTitle(title)
                        if (playlist.playlistEntity.playlistId == vm.selectedPlaylist)
                            withContext(Dispatchers.Main) {
                                vm.displayedPlaylist = currentPlaylist
                            }//.clone()
                        println("FOOB updatePlaylist: ${currentPlaylist.playlistEntity.playlistId}, titles: ${currentPlaylist.titles.count()}")
                    }
                    currentPlaylist = getPlaylistToBeUpdated()
                } while (currentPlaylist != null && !vm.errorHandler.sick())
                withContext(Dispatchers.Main) { vm.loadingList = false }
                println("FOOB updatePlaylist finished")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { vm.loadingList = false }
            }
        }
    }

    private fun invalidatePlaylist(playlistId: String) {
        if (playlistId != "")
            invalidatePlaylist(getPlaylist(playlistId))
    }

    private fun invalidatePlaylist(playlist: Playlist) {
        if (vm.displayedPlaylist == null || vm.displayedPlaylist!!.playlistEntity.playlistId != playlist.playlistEntity.playlistId) {
            playlist.clear()
            updatePlaylists()
        } else playlist.valid = false
    }

    fun updateList() {
        invalidatePlaylist(vm.selectedPlaylist)
    }

    fun setSelectedPlaylist(id: String) {
        if (id.isNotEmpty()) {
            if (id != vm.selectedPlaylist)
                println("FOOB changing from playlist: $vm.selectedPlaylist to $id")
            vm.selectedPlaylist = id
            vm.displayedPlaylist = getPlaylist(id) //.clone()
            vm.selectedPlaylistName = vm.displayedPlaylist!!.playlistEntity.name
            updatePlaylists()
        }
    }
}
