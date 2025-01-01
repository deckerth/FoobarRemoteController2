package com.deckerth.thomas.foobarremotecontroller2.model

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

enum class AddTracksBehaviors(val text: String) {
    ADD_BEHAVIOR_ADD(mainActivity.baseContext.getString(R.string.add_behavior_add)),
    ADD_BEHAVIOR_ADD_PLAY(mainActivity.baseContext.getString(R.string.add_behavior_add_play)),
    ADD_BEHAVIOR_REPLACE_PLAY(mainActivity.baseContext.getString(R.string.add_behavior_replace_play))
}
