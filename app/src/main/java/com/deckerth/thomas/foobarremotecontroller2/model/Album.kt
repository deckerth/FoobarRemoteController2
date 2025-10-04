package com.deckerth.thomas.foobarremotecontroller2.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.state.ToggleableState
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.TitleFilter
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.viewModelInstance

data class SelectableTitle(val album: Album, val details: ITitle) {
    var isSelected by mutableStateOf(false)

    fun toggleIsSelected(appViewModel: AppViewModel) {
        setSelected(appViewModel, !isSelected)
    }

    fun setSelected(appViewModel: AppViewModel, value: Boolean) {
        if (isSelected == value) return
        isSelected = value
        if (isSelected) {
            appViewModel.titleToRemoveWasSelected = true
            appViewModel.increaseNoOfSelectedTitles()
        } else {
            appViewModel.decreaseNoOfSelectedTitles()
        }
        album.adjustIsSelected()
    }
}

data class Album(
    val originalTitle: ITitle
) {
    private var _tracks = mutableListOf<SelectableTitle>()

    val tracks: List<SelectableTitle>
        get() {
            return _tracks.toList()
        }

    var isExpanded by mutableStateOf(false)

    var isSelected by mutableStateOf(ToggleableState.Off)

    fun setIsSelected(value: ToggleableState) {
        isSelected = value
        if (value == ToggleableState.On)
            for (title in _tracks) title.setSelected(viewModelInstance, true)
        else if (value == ToggleableState.Off)
            for (title in _tracks) title.setSelected(viewModelInstance, false)
    }

    fun adjustIsSelected() {
        if (_tracks.any { it.isSelected })
            if (_tracks.all { it.isSelected })
                setIsSelected(ToggleableState.On)
            else
                setIsSelected(ToggleableState.Indeterminate)
        else
            setIsSelected(ToggleableState.Off)
    }

    fun toggleIsSelected() {
        if (isSelected == ToggleableState.On)
            setIsSelected(ToggleableState.Off)
        else
            setIsSelected(ToggleableState.On)
    }

    var isAutomaticSelection = true

    private var endIndex: Int = 0

    fun matches(filter: TitleFilter): Boolean {
        if (!filter.isActive) return true
        for (title in _tracks)
            if (title.details.matches(filter)) return true
        return false
    }

    fun hasIndex(index: Int): Boolean {
        return index in originalTitle.index..endIndex
    }

    fun addTitle(title: ITitle) {
        _tracks.add(SelectableTitle(this, title))
        endIndex = title.index
    }
}
