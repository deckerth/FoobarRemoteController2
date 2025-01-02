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
}
