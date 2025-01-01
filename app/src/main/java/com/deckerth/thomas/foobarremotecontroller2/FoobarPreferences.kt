package com.deckerth.thomas.foobarremotecontroller2

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.deckerth.thomas.foobarremotecontroller2.model.AddTracksBehaviors
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layouts
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "settings")

private val IP_ADDRESS_KEY = stringPreferencesKey("ip_address")
private val VIEW_MODE_KEY = intPreferencesKey("view_mode")
private val CUSTOM_LAYOUT_KEY = stringPreferencesKey("custom_layout")
private val FOOBAR_VOLUME_CONTROL_KEY = booleanPreferencesKey("foobar_volume_control")
private val PAUSE_DURING_PHONE_CALLS_KEY = booleanPreferencesKey("pause_during_phone_calls")
private val ADD_TRACK_BEHAVIOR_KEY = intPreferencesKey("add_track_behavior")

private fun <T> getFlow(context: Context, key: Preferences.Key<T>): Flow<T?> {
    return context.dataStore.data.map { preferences ->
            preferences[key]
        }

}

@Composable
private fun <T> getValue(context: Context, key: Preferences.Key<T>, initial: T): T {
    val flow = getFlow(context, key)
    return flow.collectAsState(initial = initial).value ?: initial
}

private fun <T> saveValue(context: Context, value: T, key: Preferences.Key<T>) {
    runBlocking {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}

fun saveIpAddress(ip: String, context: Context) {
    runBlocking {
        saveValue(context, ip, IP_ADDRESS_KEY)
    }
}

@Composable
fun getIpAddress(): String {
    return getValue(mainActivity, IP_ADDRESS_KEY, "0.0.0.0")
}

fun saveViewMode(previousMode: Layouts, mode: Layouts, context: Context) {
    layoutManager.changeLayout(previousMode, mode)
    runBlocking {
        saveValue(context, mode.ordinal, VIEW_MODE_KEY)
    }
}

@Composable
fun getViewMode(): Layouts {
    return Layouts.entries[getValue(mainActivity, VIEW_MODE_KEY, Layouts.LAYOUT_MODERN.ordinal)]
}

@Composable
fun getCustomLayout(): Layout? {
    val layoutString = getValue(mainActivity.baseContext, CUSTOM_LAYOUT_KEY, "")
    return if (layoutString.isEmpty()) null
    else Json.decodeFromString(layoutString)
}

fun saveCustomLayout(context: Context, layout: Layout) {
    val layoutString = Json.encodeToString(layout)
    runBlocking {
        saveValue(context, layoutString, CUSTOM_LAYOUT_KEY)
    }
}

fun saveFoobarVolumeControl(enabled: Boolean, context: Context) {
    runBlocking {
        saveValue(context, enabled, FOOBAR_VOLUME_CONTROL_KEY)
    }
}

@Composable
fun getFoobarVolumeControl(): Boolean {
    return getValue(mainActivity, FOOBAR_VOLUME_CONTROL_KEY, true)
}

fun savePauseDuringPhoneCalls(enabled: Boolean, context: Context) {
    runBlocking {
        saveValue(context, enabled, PAUSE_DURING_PHONE_CALLS_KEY)
    }
}

@Composable
fun getPauseDuringPhoneCalls(): Boolean {
    return getValue(mainActivity, PAUSE_DURING_PHONE_CALLS_KEY, true)
}

fun saveAddTrackBehavior(behavior: AddTracksBehaviors, context: Context) {
    runBlocking {
        saveValue(context, behavior.ordinal, ADD_TRACK_BEHAVIOR_KEY)
    }
}

@Composable
fun getAddTrackBehavior(): AddTracksBehaviors {
    return AddTracksBehaviors.entries[getValue(mainActivity, ADD_TRACK_BEHAVIOR_KEY, AddTracksBehaviors.ADD_BEHAVIOR_ADD_PLAY.ordinal)]

}
