package com.deckerth.thomas.foobarremotecontroller2.ui.layout

data class Layout(val playerLayout : LayoutDescription,
                  val albumLayout : LayoutDescription,
                  val titleLayout : LayoutDescription) {
    val albumLayoutHasArtist: Boolean
        get() {
            return (albumLayout.items.any { it.item == LayoutItems.ARTIST })
        }
}
