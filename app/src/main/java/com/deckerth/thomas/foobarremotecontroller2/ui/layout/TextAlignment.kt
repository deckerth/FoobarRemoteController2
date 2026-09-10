package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

enum class TextAlignment(val text: String) {
    LEFT(mainActivity!!.baseContext.getString(R.string.alignment_left)),
    CENTER(mainActivity!!.baseContext.getString(R.string.alignment_centered)),
    RIGHT(mainActivity!!.baseContext.getString(R.string.alignment_right))
}