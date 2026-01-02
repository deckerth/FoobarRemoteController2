package com.deckerth.thomas.foobarremotecontroller2.model

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

enum class AddTracksBehaviors() {
    ADD_BEHAVIOR_ADD(),
    ADD_BEHAVIOR_ADD_PLAY(),
    ADD_BEHAVIOR_REPLACE_PLAY();

    fun get_text(value: AddTracksBehaviors): String {
        return if (mainActivity == null) value.toString() else
            when (value) {
                ADD_BEHAVIOR_ADD -> mainActivity!!.baseContext.getString(R.string.add_behavior_add)
                ADD_BEHAVIOR_ADD_PLAY -> mainActivity!!.baseContext.getString(R.string.add_behavior_add_play)
                ADD_BEHAVIOR_REPLACE_PLAY -> mainActivity!!.baseContext.getString(R.string.add_behavior_replace_play)
            }
    }
}
