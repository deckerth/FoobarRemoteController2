package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import android.annotation.SuppressLint
import android.content.Context
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

enum class LayoutItems(text : String) {
    CATALOG(mainActivity.baseContext.getString(R.string.layout_item_medium)),
    COMPOSER(mainActivity.baseContext.getString(R.string.layout_item_composer)),
    ALBUM(mainActivity.baseContext.getString(R.string.layout_item_album)),
    TITLE(mainActivity.baseContext.getString(R.string.layout_item_title)),
    ARTIST(mainActivity.baseContext.getString(R.string.layout_item_artist)),
    SMART_ARTIST(mainActivity.baseContext.getString(R.string.layout_item_smart_artist)),
    PROGRESS(mainActivity.baseContext.getString(R.string.layout_item_progress_bar)),
    ARTWORK(mainActivity.baseContext.getString(R.string.layout_item_cover)),
    PLAYLIST_ID(mainActivity.baseContext.getString(R.string.layout_item_playlist_id)),
    INDEX(mainActivity.baseContext.getString(R.string.layout_item_index))
}