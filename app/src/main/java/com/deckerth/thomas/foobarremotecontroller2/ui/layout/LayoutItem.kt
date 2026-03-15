package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import kotlinx.serialization.Serializable

@Serializable
data class LayoutItem(
    val item: StandardLayoutItems = StandardLayoutItems.UNDEFINED,  // enable anonymous constructor
    val customFieldName: String = "",
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
            StandardLayoutItems.ARTWORK ->
                return mainActivity!!.baseContext.getString(R.string.size_property) + " ${itemSize.text}"

            StandardLayoutItems.PROGRESS ->
                return progressBarFormat.text

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

    fun getText() : String {
        return if (item == StandardLayoutItems.CUSTOM_FIELD) {
            customFieldName
        } else {
            item.text
        }
    }

}
