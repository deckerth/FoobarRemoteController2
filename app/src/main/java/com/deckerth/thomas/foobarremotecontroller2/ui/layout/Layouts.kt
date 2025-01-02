package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

enum class Layouts(val text: String) {
    LAYOUT_MODERN(mainActivity.baseContext.getString(R.string.layout_modern)),
    LAYOUT_CLASSIC(mainActivity.baseContext.getString(R.string.layout_classic)),
    LAYOUT_CUSTOM(mainActivity.baseContext.getString(R.string.layout_custom))
}