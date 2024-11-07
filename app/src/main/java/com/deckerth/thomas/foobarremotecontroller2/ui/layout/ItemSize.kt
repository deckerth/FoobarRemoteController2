package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

enum class ItemSize(val text: String) {
    UNDEFINED("UNDEFINED"),
    TITLE_LARGE(mainActivity.baseContext.getString(R.string.title_large)),
    TITLE_MEDIUM(mainActivity.baseContext.getString(R.string.title_medium)),
    BODY_MEDIUM(mainActivity.baseContext.getString(R.string.body_medium)),
    BODY_SMALL(mainActivity.baseContext.getString(R.string.body_small)),
    SMALL_COVER(mainActivity.baseContext.getString(R.string.small_cover)),
    MEDIUM_COVER(mainActivity.baseContext.getString(R.string.medium_cover)),
    LARGE_COVER(mainActivity.baseContext.getString(R.string.large_cover))
}