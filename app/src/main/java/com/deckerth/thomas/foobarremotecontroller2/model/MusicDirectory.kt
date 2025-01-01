package com.deckerth.thomas.foobarremotecontroller2.model

import androidx.compose.runtime.mutableStateOf
import com.deckerth.thomas.foobarremotecontroller2.connector.browserAccess

open class MusicDirectory(name: String, path: String, private val parentDirectory: String) :
    MusicDirectoryEntry(name, path) {

    private val entries = mutableListOf<MusicDirectoryEntry>()
    protected val expanded = mutableStateOf(false)

    fun addEntry(entry: MusicDirectoryEntry) {
        entries.add(entry)
    }

    fun getEntries(): List<MusicDirectoryEntry> {
        return entries
    }

    override fun isDirectory(): Boolean {
        return true
    }

    override fun setIsAdded(added: Boolean) {
        super.setIsAdded(added)
        for (entry in entries) {
            entry.setIsAdded(added)
        }
    }

    fun isExpanded(): Boolean {
        return expanded.value
    }

    fun expand() {
        val result =
            if (path == "")
                browserAccess.getRoots()
            else
                browserAccess.getDirectory(path, parentDirectory)
        if (result != null) {
            for (entry in result.entries) {
                println("FOOB addEntry: ${entry.name}")
                entry.setIsAdded(isAdded.value)
                addEntry(entry)
            }
            expanded.value = true
        }
    }
}

class ParentDirectory(name: String, path: String) : MusicDirectory(name, path, "?") {
    init {
        expanded.value = true
    }

    override fun isParentDirectory(): Boolean {
        return true
    }
}