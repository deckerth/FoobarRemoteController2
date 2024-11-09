package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import androidx.compose.ui.text.font.FontStyle
import kotlinx.serialization.Serializable

@Serializable
data class LayoutItem(
    val item: LayoutItems = LayoutItems.UNDEFINED,  // enable anonymous constructor
    var itemSize: ItemSize = ItemSize.UNDEFINED,
    var italic:  Boolean = false,
    var bold: Boolean = false,
    var maxLines: Int = 1,
    var alignment: TextAlignment = TextAlignment.LEFT) {

    fun verbose(): String {
        //TODO Make this nice:
        return "item: $item, itemSize: $itemSize, italic: $italic, maxLines: $maxLines"
    }
}
