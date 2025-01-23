package com.deckerth.thomas.foobarremotecontroller2.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.state.ToggleableState
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class PlaylistLifecycleState {
    Valid,           // can be updated anytime
    RequiresUpdate,  // for late update at the beginning of next playlist recompose
    Invalid          // will not be updated or displayed
}

class Playlist(var playlistEntity: PlaylistEntity) {
    val titles = mutableListOf<ITitle>()
    val albums = mutableListOf<Album>()
    var lifecycleState by mutableStateOf(PlaylistLifecycleState.Valid)
    var ipAddress: String = ""

    fun clear() {
        titles.clear()
        albums.clear()
        lifecycleState = PlaylistLifecycleState.Valid
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
