package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import androidx.compose.ui.text.font.FontStyle
import kotlinx.serialization.Serializable

@Serializable
data class LayoutItem(
    val item: LayoutItems = LayoutItems.UNDEFINED,  // enable anonymous constructor
    val itemSize: ItemSize = ItemSize.UNDEFINED,
    val italic: Boolean = false,
    val maxLines: Int = 1 ) {
}
