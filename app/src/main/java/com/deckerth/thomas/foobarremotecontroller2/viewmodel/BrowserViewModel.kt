package com.deckerth.thomas.foobarremotecontroller2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.browserAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.errorHandler
import com.deckerth.thomas.foobarremotecontroller2.model.AddTracksBehaviors
import com.deckerth.thomas.foobarremotecontroller2.model.MusicDirectory
import com.deckerth.thomas.foobarremotecontroller2.model.MusicDirectoryEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrowserViewModel : ViewModel() {

    private var filesystem = mutableStateOf(MusicDirectory("ROOT", "", "NULL"))
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
            return MusicDirectory(currentPath.value, "", "NULL") // return empty directory
        if (!directories[currentPath.value]!!.isExpanded())
            expand(directories[currentPath.value]!!)
        return directories[currentPath.value]!!
    }

    private fun expand(musicDirectory: MusicDirectory) {
        if (!loadingData.value && !errorHandler.sick()) {
            loadingData.value = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    musicDirectory.expand()
                    for (entry in musicDirectory.getEntries())
                        if (entry is MusicDirectory && !entry.isParentDirectory())
                            directories[entry.path] = entry
                    withContext(Dispatchers.Main) { loadingData.value = false }
                    println("FOOB BrowserViewModel loadingData ${loadingData.value}")
                } catch (e: Exception) {
                    loadingData.value = false
                }
            }
        }
    }

    fun getCurrentPath(): String {
        return currentPath.value
    }

    fun addToPlaylist(entry: MusicDirectoryEntry, addBehavior: AddTracksBehaviors) {
        if (!errorHandler.sick())
            Thread {
                if (displayedPlaylist != null) {
                    loadingData.value = true
                    PlaylistAccess.getInstance()
                        .addPathToPlaylist(
                            displayedPlaylist!!.playlistEntity.playlistId,
                            browserAccess.escapePathSeparator(entry.path),
                            addBehavior
                        )
                    entry.setIsAdded(true)
                    loadingData.value = false
                    filesAdded = true
                }
            }.start()
    }

    fun refreshRoots() {
        if (currentPath.value == "")
            directories[currentPath.value]!!.setExpanded(false)
    }
}