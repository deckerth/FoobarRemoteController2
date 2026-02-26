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
            return (albumLayout.items.any { it.item == LayoutItems.ARTIST })
        }

    val albumLayoutHasProgressBar: Boolean
        get() {
            return (albumLayout.items.any { it.item == LayoutItems.PROGRESS })
        }

    val titleLayoutHasProgressBar: Boolean
        get() {
            return (titleLayout.items.any { it.item == LayoutItems.PROGRESS })
        }

    val titleProgress: LayoutItem
        get() {
            return titleLayout.items.first { it.item == LayoutItems.PROGRESS }
        }

}

fun highlightPlayingItem(layout: LayoutDescription): Boolean
    {
    // Always highlight the current title. The only exception: The layout contains a progress bar that shall replace the background color.
        return (!layout.items.any { it.item == LayoutItems.PROGRESS && it.progressBarReplacesBackgroundColoring })
    }