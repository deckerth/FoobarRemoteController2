package com.deckerth.thomas.foobarremotecontroller2.model

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.state.ToggleableState
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.TitleFilter
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.appViewModel

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
            for (title in _tracks) title.setSelected(appViewModel!!, true)
        else if (value == ToggleableState.Off)
            for (title in _tracks) title.setSelected(appViewModel!!, false)
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

    private var totalPlayingTime = 0.0f

    fun matches(filter: TitleFilter, customFields: CustomFields): Boolean {
        if (!filter.isActive) return true
        for (title in _tracks)
            if (title.details.matches(filter, customFields)) return true
        return false
    }

    fun hasIndex(index: Int): Boolean {
        return index in originalTitle.index..endIndex
    }

    fun getTitleAt(index: Int): ITitle? {
        for (title in _tracks)
            if (title.details.index == index)
                return title.details
        return null
    }

    fun addTitle(title: ITitle) {
        _tracks.add(SelectableTitle(this, title))
        endIndex = title.index
        title.elapsedTimeWhenTitleStarts = totalPlayingTime
        try {
            val duration = title.duration.toFloat()
            totalPlayingTime += duration
        } catch (_: NumberFormatException) {
        }
    }

    fun getAlbumPosition(pvm : PlayerViewModel): Float {
        try {
            val duration = totalPlayingTime
            if (duration == 0f) return 0f
            val currentTitle = getTitleAt(pvm.getIndex())
            if (currentTitle == null) return 0.0f
            val position = pvm.getPosInSeconds() + currentTitle.elapsedTimeWhenTitleStarts

            return position / duration * 1f
        } catch (_: NumberFormatException) {
            return 0f
        }
    }

    @SuppressLint("DefaultLocale")
    fun getNiceDuration(): String {
        try {
            val duration = totalPlayingTime.toInt()
            val minutes: Int = duration / 60
            val seconds: Int = duration % 60
            if (minutes > 59) {
                val hours: Int = minutes / 60
                val remainingMinutes: Int = minutes % 60
                return String.format("%01d:%02d:%02d", hours, remainingMinutes, seconds)
            }
            return String.format("%01d:%02d", minutes, seconds)
        } catch (_: NumberFormatException) {
            return ""
        }
    }

    @SuppressLint("DefaultLocale")
    fun getNicePosition(fraction : Float): String {
        try {
            val position = (totalPlayingTime * fraction).toInt()
            val minutes: Int = position / 60
            val seconds: Int = position % 60
            if (minutes > 59) {
                val hours: Int = minutes / 60
                val remainingMinutes: Int = minutes % 60
                return String.format("%01d:%02d:%02d", hours, remainingMinutes, seconds)
            }
            return String.format("%01d:%02d", minutes, seconds)
        } catch (_: NumberFormatException) {
            return ""
        }
    }
}
