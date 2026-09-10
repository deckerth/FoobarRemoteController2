package com.deckerth.thomas.foobarremotecontroller2.model

import androidx.compose.runtime.mutableStateOf

open class MusicDirectoryEntry(val name: String, val path: String) {

    var isAdded = mutableStateOf(false)

    open fun setIsAdded(added: Boolean) {
        isAdded.value = added
    }

    open fun isDirectory(): Boolean {
        return false
    }

    open fun isParentDirectory(): Boolean {
        return false
    }

}