package com.deckerth.thomas.foobarremotecontroller2.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Album(
    val originalTitle: ITitle
) {
    private var _titles = mutableListOf<ITitle>()

    val titles: List<ITitle>
        get() {
            return _titles.toList()
        }

    var isSelected by mutableStateOf(false)

    var isAutomaticSelection = true

    var endIndex: Int = 0

    fun matches(pattern: String): Boolean {
        if (pattern.isEmpty()) return true
        for (title in _titles)
            if (title.matches(pattern)) return true
        return false
    }

    fun hasIndex(index: Int): Boolean {
        return index in originalTitle.index..endIndex
    }

    fun addTitle(title: ITitle) {
        _titles.add(title)
        endIndex = title.index
    }
}
