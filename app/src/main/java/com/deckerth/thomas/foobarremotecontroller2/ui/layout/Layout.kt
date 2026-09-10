package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import kotlinx.serialization.Serializable

@Serializable
data class Layout(
    var playerLayout: LayoutDescription,
    var albumLayout: LayoutDescription,
    var titleLayout: LayoutDescription
) {
    val albumLayoutHasArtist: Boolean
        get() {
            return (albumLayout.items.any { it.item == StandardLayoutItems.ARTIST })
        }

    val albumLayoutHasProgressBar: Boolean
        get() {
            return (albumLayout.items.any { it.item == StandardLayoutItems.PROGRESS })
        }

    val titleLayoutHasProgressBar: Boolean
        get() {
            return (titleLayout.items.any { it.item == StandardLayoutItems.PROGRESS })
        }

    val titleProgress: LayoutItem
        get() {
            return titleLayout.items.first { it.item == StandardLayoutItems.PROGRESS }
        }

    fun getViewModesWith(fieldName : String) : List<ViewsWithLayout> {
        val layouts = mutableListOf<ViewsWithLayout>()
        if (playerLayout.isUsedInLayout(fieldName)) layouts.add(ViewsWithLayout.PLAYER)
        if (albumLayout.isUsedInLayout(fieldName)) layouts.add(ViewsWithLayout.ALBUM)
        if (titleLayout.isUsedInLayout(fieldName)) layouts.add(ViewsWithLayout.TITLE)
        return layouts
    }

    fun removeFieldFromLayouts(fieldName: String) {
        playerLayout.removeFieldFromLayout(fieldName)
        albumLayout.removeFieldFromLayout(fieldName)
        titleLayout.removeFieldFromLayout(fieldName)
    }
}



fun highlightPlayingItem(layout: LayoutDescription): Boolean
    {
    // Always highlight the current title. The only exception: The layout contains a progress bar that shall replace the background color.
        return (!layout.items.any { it.item == StandardLayoutItems.PROGRESS && it.progressBarReplacesBackgroundColoring })
    }