package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

enum class ProgressBarFormat(
        val text: String
) {
    UNDEFINED("UNDEFINED"),
    FLAT(mainActivity!!.baseContext.getString(R.string.progress_bar_flat)),
    WAVY(mainActivity!!.baseContext.getString(R.string.progress_bar_wavy)),
    WAVY_WHEN_PLAYING(mainActivity!!.baseContext.getString(R.string.progress_bar_wavy_when_playing))

}

fun getProgressBarFormats(): List<ProgressBarFormat> {
    return listOf(ProgressBarFormat.FLAT, ProgressBarFormat.WAVY_WHEN_PLAYING, ProgressBarFormat.WAVY)
}