package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import kotlinx.serialization.Serializable

@Serializable
data class LayoutItem(
    val item: LayoutItems = LayoutItems.UNDEFINED,  // enable anonymous constructor
    var itemSize: ItemSize = ItemSize.UNDEFINED,
    var italic: Boolean = false,
    var bold: Boolean = false,
    var maxLines: Int = 1,
    var alignment: TextAlignment = TextAlignment.LEFT,
    var progressBarFormat: ProgressBarFormat = ProgressBarFormat.WAVY_WHEN_PLAYING,
    var progressBarShowTimings: Boolean = false,
    var progressBarReplacesBackgroundColoring: Boolean = true,
    var waveSpeed: Int = 5
) {

    fun verbose(): String {
        when (item) {
            LayoutItems.ARTWORK ->
                return mainActivity!!.baseContext.getString(R.string.size_property) + " ${itemSize.text}"

            LayoutItems.PROGRESS ->
                return ""

            else -> {
                val italic =
                    if (italic) ", " + mainActivity!!.baseContext.getString(R.string.font_italic) else ""
                val bold =
                    if (bold) ", " + mainActivity!!.baseContext.getString(R.string.font_bold) else ""
                val alignment =
                    if (alignment != TextAlignment.LEFT) ", " + alignment.text else ""
                return mainActivity!!.baseContext.getString(R.string.font_properties) + ": ${itemSize.text}" + italic + bold + alignment
            }
        }
    }
}
