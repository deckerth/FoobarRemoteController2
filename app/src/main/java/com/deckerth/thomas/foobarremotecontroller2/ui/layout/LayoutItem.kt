package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import androidx.compose.ui.text.font.FontStyle

data class LayoutItem(
    val item: LayoutItems,
    val itemSize: ItemSize = ItemSize.UNDEFINED,
    val fontStyle: FontStyle = FontStyle.Normal,
    val maxLines: Int = 1 ) {
}
