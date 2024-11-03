package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

enum class ViewsWithLayout(val text: String) {
    PLAYER(mainActivity.baseContext.getString(R.string.choose_player_layout)),
    ALBUM(mainActivity.baseContext.getString(R.string.choose_album_layout)),
    TITLE(mainActivity.baseContext.getString(R.string.choose_title_layout)),
    UNDEFINED("undefined")
}