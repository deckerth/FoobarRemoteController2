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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PlaylistLifecycleState {
    Valid,           // can be updated anytime
    RequiresUpdate,  // for late update at the beginning of next playlist recompose
    Invalid          // will not be updated or displayed
}

class Playlist(var playlistEntity: PlaylistEntity) {
    val titles = mutableListOf<ITitle>()
    val albums = mutableListOf<Album>()
    val filteredAlbums = mutableListOf<Album>()
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
            result.add(mainActivity.getString(R.string.all_genres))
        result.addAll(genres)
        return result
    }

    fun applyFilterSync(filter: TitleFilter) {
        if (filter.isActive && filter.hasChanged) {
            filteredAlbums.clear()
            for (album in albums) {
                if (album.matches(filter)) {
                    filteredAlbums.add(album)
                }
            }
        }
        filter.hasChanged = false
    }

    private var applyingFilter = false

    fun applyFilter(vm: AppViewModel) {
        val filter = vm.playlistsViewModel.filterValue
        if (!applyingFilter && filter.isActive && filter.hasChanged) {
            applyingFilter = true
            filteredAlbums.clear()
            CoroutineScope(Dispatchers.IO).launch {
                applyFilterAsync(vm)
            }
        }
    }

    private suspend fun applyFilterAsync(vm: AppViewModel) {
        val filter = vm.playlistsViewModel.filterValue
        val result = mutableListOf<Album>()
        withContext(Dispatchers.Main) {
            vm.loadingListProgress = 0f
            vm.loadingList = true
        }
        for ((index, album) in albums.withIndex()) {
            if (!filter.isActive) break
            withContext(Dispatchers.Main) {
                vm.loadingListProgress = index.toFloat() / albums.size
            }
            if (album.matches(filter)) {
                result.add(album)
            }
        }
        withContext(Dispatchers.Main) {
            if (filter.isActive) filteredAlbums.addAll(result)
            vm.loadingList = false
            filter.hasChanged = false
        }
        applyingFilter = false
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

    fun removeSelectedTracks(vm: AppViewModel) {
        if (vm.noOfTitlesToRemove > 0) {
            val titlesToRemove = mutableListOf<Int>()
            for (album in albums)
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
        vm.noOfTitlesToRemove = 0
        vm.disableRemoveTitlesMode(false)
    }

    fun resetSelectedTracks(vm: AppViewModel) {
        if (vm.noOfTitlesToRemove > 0) {
            for (album in albums)
                album.setIsSelected(ToggleableState.Off)
        }
    }
}
