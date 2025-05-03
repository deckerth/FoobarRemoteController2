package com.deckerth.thomas.foobarremotecontroller2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.deckerth.thomas.foobarremotecontroller2.model.AddTracksBehaviors
import com.deckerth.thomas.foobarremotecontroller2.model.MusicDirectory
import com.deckerth.thomas.foobarremotecontroller2.model.MusicDirectoryEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrowserViewModel(private val vm : AppViewModel) : ViewModel() {

    private var filesystem = mutableStateOf(MusicDirectory(vm,"ROOT", "", "NULL"))
    private var currentPath = mutableStateOf("")
    var loadingData = mutableStateOf(false)
    private var directories = HashMap<String, MusicDirectory>()
    var filesAdded by mutableStateOf(false)

    init {
        directories[""] = filesystem.value
    }

    fun setCurrentPath(path: String) {
        currentPath.value = path
    }

    fun getDirectory(): MusicDirectory {
        if (directories[currentPath.value] == null)
            return MusicDirectory(vm, currentPath.value, "", "NULL") // return empty directory
        if (!directories[currentPath.value]!!.isExpanded())
            expand(directories[currentPath.value]!!)
        return directories[currentPath.value]!!
    }

    private fun expand(musicDirectory: MusicDirectory) {
        if (!loadingData.value && !vm.errorHandler.sick()) {
            loadingData.value = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    musicDirectory.expand()
                    for (entry in musicDirectory.getEntries())
                        if (entry is MusicDirectory && !entry.isParentDirectory())
                            withContext(Dispatchers.Main) { directories[entry.path] = entry }
                    withContext(Dispatchers.Main) { loadingData.value = false }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { loadingData.value = false }
                }
            }
        }
    }

    fun getCurrentPath(): String {
        return currentPath.value
    }

    fun addToPlaylist(entry: MusicDirectoryEntry, addBehavior: AddTracksBehaviors) {
        if (!vm.errorHandler.sick())
            CoroutineScope(Dispatchers.IO).launch {
                if (vm.displayedPlaylist != null) {
                    loadingData.value = true
                    vm.playlistAccess
                        .addPathsToPlaylist(
                            vm.displayedPlaylist!!.playlistEntity.playlistId,
                            vm.browserAccess.escapePathSeparator(entry.path),
                            addBehavior
                        )
                    entry.setIsAdded(true)
                    loadingData.value = false
                    filesAdded = true
                }
            }
    }

    fun refreshRoots() {
        if (currentPath.value == "")
            directories[currentPath.value]!!.setExpanded(false)
    }
}