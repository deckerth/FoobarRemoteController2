package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import android.annotation.SuppressLint
import android.content.Context
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

enum class LayoutItems(text : String) {
    CATALOG(mainActivity.baseContext.getString(R.string.medium)),
    COMPOSER(mainActivity.baseContext.getString(R.string.composer)),
    ALBUM(mainActivity.baseContext.getString(R.string.album)),
    TITLE(mainActivity.baseContext.getString(R.string.title)),
    ARTIST(mainActivity.baseContext.getString(R.string.artist)),
    PROGRESS(mainActivity.baseContext.getString(R.string.progress_bar)),
    ARTWORK(mainActivity.baseContext.getString(R.string.cover)),
    PLAYLIST_ID(mainActivity.baseContext.getString(R.string.playlist_id)),
    INDEX(mainActivity.baseContext.getString(R.string.index))
}