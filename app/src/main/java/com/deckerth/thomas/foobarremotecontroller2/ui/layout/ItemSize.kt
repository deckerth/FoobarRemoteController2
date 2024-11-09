package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ItemSize.entries

enum class ItemSize(val text: String, val isFontSize: Boolean = true) {
    UNDEFINED("UNDEFINED"),
    TITLE_LARGE(mainActivity.baseContext.getString(R.string.title_large)),
    TITLE_MEDIUM(mainActivity.baseContext.getString(R.string.title_medium)),
    BODY_MEDIUM(mainActivity.baseContext.getString(R.string.body_medium)),
    BODY_SMALL(mainActivity.baseContext.getString(R.string.body_small)),
    SMALL_COVER(mainActivity.baseContext.getString(R.string.small_cover), isFontSize = false),
    MEDIUM_COVER(mainActivity.baseContext.getString(R.string.medium_cover), isFontSize = false),
    LARGE_COVER(mainActivity.baseContext.getString(R.string.large_cover), isFontSize = false),
    MAX_COVER(mainActivity.baseContext.getString(R.string.max_cover), isFontSize = false),
}

fun getItemSizesForAlbums() :List<ItemSize> {
    return entries.filter { !it.isFontSize && it != ItemSize.UNDEFINED }
}

fun getItemSizesForFonts() :List<ItemSize> {
    return entries.filter { it.isFontSize && it != ItemSize.UNDEFINED}
}