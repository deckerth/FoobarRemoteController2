package com.deckerth.thomas.foobarremotecontroller2.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.state.ToggleableState
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.TitleFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.system.measureTimeMillis

enum class PlaylistLifecycleState {
    Valid,           // can be updated anytime
    RequiresUpdate,  // for late update at the beginning of next playlist recompose
    Invalid          // will not be updated or displayed
}

class Playlist(var playlistEntity: PlaylistEntity) {
    val titles = mutableListOf<ITitle>()
    val albums = mutableListOf<Album>()
    val filteredAlbums = mutableListOf<Album>()
    val filteredTitles = mutableListOf<ITitle>()
    private val genres = mutableListOf<String>()
    var lifecycleState by mutableStateOf(PlaylistLifecycleState.Valid)
    var ipAddress: String = ""

    fun clear() {
        titles.clear()
        albums.clear()
        filteredAlbums.clear()
        genres.clear()
        lifecycleState = PlaylistLifecycleState.Valid
    }

    fun getGenres(addAllGenresText: Boolean = false): List<String> {
        val result = mutableListOf<String>()
        if (addAllGenresText)
            result.add(mainActivity!!.getString(R.string.all_genres))
        genres.sort()
        result.addAll(genres)
        return result
    }

    private var applyingFilter = false

    fun applyFilter(vm: AppViewModel) {
        val filter = vm.playlistsViewModel.filterValue
        if (!applyingFilter && filter.isActive && filter.hasChanged) {
            vm.loadingListProgress = 0f
            vm.loadingList = true
            applyingFilter = true
            filteredAlbums.clear()
            filteredTitles.clear()
            CoroutineScope(Dispatchers.IO).launch {
                applyFilterParallelAsync(vm)
            }
        }
    }

    private suspend fun applyFilterParallelAsync(vm: AppViewModel) {
        val filter = vm.playlistsViewModel.filterValue
        val result = Playlist(PlaylistEntity("", "", false, 0))

        val timeTaken = measureTimeMillis {
            val filterResult = parallelTitleFilter(vm, filter)
            filterResult.forEach(result::addTitle)
            withContext(Dispatchers.Main) {
                if (filter.isActive) {
                    filteredAlbums.addAll(result.albums)
                    filteredTitles.addAll(result.titles)
                    filter.hasChanged = false
                }
            }
        }
        println("FOOB Filtering took $timeTaken ms")
        withContext(Dispatchers.Main) {
            vm.loadingList = false
        }
        applyingFilter = false
    }

    private fun partitionTitleList(): List<List<ITitle>> {
        val minChunkSize = 100
        val maxChunkNumber = 100
        val numChunks = min((titles.size + minChunkSize - 1) / minChunkSize, maxChunkNumber)
        val chunkSize = (titles.size + numChunks - 1) / numChunks
        var transferredForChunk = 0
        val result = mutableListOf<List<ITitle>>()
        var currentChunk = mutableListOf<ITitle>()

        println("FOOB Filtering: Chunks: $numChunks, approx. size $chunkSize")

        // albums must not be split between chunks
        albums.forEach { album ->
            // add all tracks to current chunk
            album.tracks.forEach { track ->
                transferredForChunk++
                currentChunk.add(track.details)
            }
            if (transferredForChunk >= chunkSize) {
                result.add(currentChunk)
                currentChunk = mutableListOf<ITitle>()
                transferredForChunk = 0
            }
        }
        result.add(currentChunk)
        return result
    }

    private suspend fun parallelTitleFilter(
        vm: AppViewModel,
        filter: TitleFilter,
    ): List<ITitle> {
        val filteredItems = mutableListOf<ITitle>()
        val chunkedTitles = partitionTitleList()
        val resultArray = Array(chunkedTitles.count()) { mutableListOf<ITitle>() }

        coroutineScope {
            chunkedTitles.forEachIndexed { i, items ->
                launch(Dispatchers.Default) { // Use Dispatchers.Default for CPU-bound tasks
                    for ((index, title) in items.withIndex()) {
                        if (!filter.isActive) break
                        if (i == 0) // progress bar is visualizes progress for first chunk
                            withContext(Dispatchers.Main) {
                                vm.loadingListProgress = index.toFloat() / items.size
                            }
                        if (title.matches(filter)) {
                            resultArray[i].add(title)
                        }
                    }
                }
            }
            launch {
                // Ensure all worker coroutines are completed
                coroutineContext[Job]!!.children.forEach { it.join() }
            }
        }
        for (element in resultArray)
            filteredItems.addAll(element)
        return filteredItems
    }

    fun addTitle(title: ITitle) {
        titles.add(title)
        if (title.genre.isNotBlank() && !genres.contains(title.genre)) {
            genres.add(title.genre)
        }

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

    fun getPathsOfSelectedTracks(vm: AppViewModel): List<String> {
        val result = mutableListOf<String>()
        val albumList = if (vm.playlistsViewModel.filterValue.isActive) filteredAlbums else albums
        for (album in albumList)
            for (title in album.tracks)
                if (title.isSelected)
                    result.add(title.details.path)
        return result
    }

    fun selectAllTracks(vm: AppViewModel) {
        val albumList =
            if (vm.playlistsViewModel.filterValue.isActive) filteredAlbums else albums
        for (album in albumList)
            album.setIsSelected(ToggleableState.On)
    }

    fun deselectAllTracks(vm: AppViewModel) {
        val albumList =
            if (vm.playlistsViewModel.filterValue.isActive) filteredAlbums else albums
        for (album in albumList)
            album.setIsSelected(ToggleableState.Off)
    }

    fun removeSelectedTracks(vm: AppViewModel) {
        if (vm.noOfSelectedTitles > 0) {
            val titlesToRemove = mutableListOf<Int>()
            val albumList =
                if (vm.playlistsViewModel.filterValue.isActive) filteredAlbums else albums
            for (album in albumList)
                for (title in album.tracks)
                    if (title.isSelected) {
                        titlesToRemove.add(title.details.index)

                    }
            CoroutineScope(Dispatchers.IO).launch {
                if (vm.displayedPlaylist != null)
                    vm.playlistAccess.removeTitles(
                        vm.displayedPlaylist!!.playlistEntity.playlistId,
                        titlesToRemove
                    )
            }
        }
        vm.noOfSelectedTitles = 0
        vm.disablePlaylistEditMode(false)
    }

    fun copySelectedTitles(vm: AppViewModel, toPlaylist: PlaylistEntity) {
        if (vm.noOfSelectedTitles > 0) {
            val titlesToCopy = getSelectedTitles(vm)
            resetSelectedTracks(vm)
            CoroutineScope(Dispatchers.IO).launch {
                if (vm.displayedPlaylist != null) {
                    if (titlesToCopy.size == 1)
                        vm.toastOnPlaylistPageMessage = mainActivity!!.getString(
                            R.string.title_copied_to_playlist
                        )
                    else
                        vm.toastOnPlaylistPageMessage = mainActivity!!.getString(
                            R.string.titles_copied_to_playlist,
                            titlesToCopy.size.toString()
                        )

                    vm.playlistAccess.copyTitles(
                        vm.displayedPlaylist!!.playlistEntity.playlistId,
                        toPlaylist.playlistId,
                        vm.playlistsViewModel.getPlaylist(toPlaylist.playlistId).titles.size,
                        titlesToCopy
                    )
                    vm.displayToastOnPlaylistPage = true
                }
            }
        }
        vm.disablePlaylistEditMode(false)
    }

    private fun getSelectedTitles(vm: AppViewModel): MutableList<Int> {
        val titlesToCopy = mutableListOf<Int>()
        val albumList =
            if (vm.playlistsViewModel.filterValue.isActive) filteredAlbums else albums
        for (album in albumList)
            for (title in album.tracks)
                if (title.isSelected) {
                    titlesToCopy.add(title.details.index)
                }
        return titlesToCopy
    }

    fun addTitleToPlaybackQueue(vm: AppViewModel, playlistId: String, titleIndex: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            vm.toastOnPlaylistPageMessage = mainActivity!!.getString(
                R.string.title_added_to_playback_queue
            )

            vm.playlistAccess.addTitleToPlaybackQueue(
                playlistId,
                titleIndex
            )
        }
        vm.displayToastOnPlaylistPage = true
    }

    fun addAlbumToPlaybackQueue(vm: AppViewModel, album: Album) {
        CoroutineScope(Dispatchers.IO).launch {
            if (album.tracks.size == 1)
                vm.toastOnPlaylistPageMessage = mainActivity!!.getString(
                    R.string.title_added_to_playback_queue
                )
            else
                vm.toastOnPlaylistPageMessage = mainActivity!!.getString(
                    R.string.titles_added_to_playback_queue,
                    album.tracks.size.toString()
                )

            for (title in album.tracks)
                vm.playlistAccess.addTitleToPlaybackQueue(
                    title.details.playlistId,
                    title.details.index
                )
        }
        vm.displayToastOnPlaylistPage = true
    }

    fun addTitleToPlaylist(
        vm: AppViewModel,
        title: ITitle,
        toPlaylistId: String,
        addBehavior: AddTracksBehaviors
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            vm.toastOnPlaylistPageMessage = mainActivity!!.getString(
                R.string.title_copied_to_playlist
            )
            vm.playlistAccess.addPathsToPlaylist(
                toPlaylistId,
                title.path,
                addBehavior
            )
        }
        vm.displayToastOnPlaylistPage = true
    }

    fun addAlbumToPlaylist(
        vm: AppViewModel,
        album: Album,
        toPlaylistId: String,
        addBehavior: AddTracksBehaviors
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (album.tracks.size == 1)
                vm.toastOnPlaylistPageMessage = mainActivity!!.getString(
                    R.string.title_copied_to_playlist
                )
            else
                vm.toastOnPlaylistPageMessage = mainActivity!!.getString(
                    R.string.titles_copied_to_playlist,
                    album.tracks.size.toString()
                )
            val paths: MutableList<String> = ArrayList<String>()
            for (title in album.tracks)
                paths.add(title.details.path)
            vm.playlistAccess.addPathsToPlaylist(
                toPlaylistId,
                paths,
                addBehavior
            )
        }
        vm.displayToastOnPlaylistPage = true
    }

    fun removeTitleFromPlaylist(vm: AppViewModel, title: ITitle) {
        CoroutineScope(Dispatchers.IO).launch {
            if (vm.displayedPlaylist != null)
                vm.playlistAccess.removeTitles(
                    playlistEntity.playlistId,
                    listOf(title.index)
                )
        }
    }

    fun removeAlbumFromPlaylist(
        vm: AppViewModel,
        album: Album ) {
        CoroutineScope(Dispatchers.IO).launch {
            val indexes: MutableList<Int> = ArrayList<Int>()
            for (title in album.tracks)
                indexes.add(title.details.index)
            vm.playlistAccess.removeTitles(playlistEntity.playlistId, indexes)
        }
    }

    fun addSelectedTitlesToPlaybackQueue(vm: AppViewModel) {
        if (vm.noOfSelectedTitles > 0) {
            val titlesToAdd = getSelectedTitles(vm)
            resetSelectedTracks(vm)
            CoroutineScope(Dispatchers.IO).launch {
                if (vm.displayedPlaylist != null) {
                    if (titlesToAdd.size == 1)
                        vm.toastOnPlaylistPageMessage = mainActivity!!.getString(
                            R.string.title_added_to_playback_queue
                        )
                    else
                        vm.toastOnPlaylistPageMessage = mainActivity!!.getString(
                            R.string.titles_added_to_playback_queue,
                            titlesToAdd.size.toString()
                        )

                    for (title in titlesToAdd) {
                        vm.playlistAccess.addTitleToPlaybackQueue(
                            vm.displayedPlaylist!!.playlistEntity.playlistId,
                            title
                        )
                    }
                    vm.displayToastOnPlaylistPage = true
                }
            }
            vm.disablePlaylistEditMode(false)
        }
    }

    fun resetSelectedTracks(vm: AppViewModel) {
        if (vm.noOfSelectedTitles > 0) {
            val albumList =
                if (vm.playlistsViewModel.filterValue.isActive) filteredAlbums else albums
            for (album in albumList)
                album.setIsSelected(ToggleableState.Off)
        }
    }

    fun isNotEmpty(vm: AppViewModel): Boolean {
        return if (vm.playlistsViewModel.filterValue.isActive) filteredAlbums.isNotEmpty() else albums.isNotEmpty()
    }
}
