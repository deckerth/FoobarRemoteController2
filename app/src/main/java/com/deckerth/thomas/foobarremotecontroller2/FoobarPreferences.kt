package com.deckerth.thomas.foobarremotecontroller2

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutDescription
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layouts
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "settings")

private val IP_ADDRESS_KEY = stringPreferencesKey("ip_address")
private val VIEW_MODE_KEY = intPreferencesKey("view_mode")
private val CUSTOM_LAYOUT_KEY = stringPreferencesKey("custom_layout")

private fun <T> getFlow(context: Context, key: Preferences.Key<T>): Flow<T?> {
    return context.dataStore.data
        .map { preferences ->
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
    return Layouts.entries.get(getValue(mainActivity, VIEW_MODE_KEY, Layouts.LAYOUT_MODERN.ordinal))
}

//@Composable
//fun getCustomLayout(): LayoutDescription {
//    val layoutString = getValue(mainActivity, CUSTOM_LAYOUT_KEY, "")
//    if (layoutString.isEmpty()) {
//        return LayoutDescription(ViewsWithLayout.PLAYER)//layoutManager.createModernLayout()
//    }
//    return Json.decodeFromString(layoutString)
//    //return context.dataStore.data.map { preferences -> preferences[CUSTOM_LAYOUT_KEY]?.let { Json.decodeFromString(it) } ?: emptyList()
//}
//
//fun saveCustomLayout(context: Context, layout: LayoutDescription) {
//    val layoutString = Json.encodeToString(layout)
//    runBlocking {
//        saveValue(context, layoutString, CUSTOM_LAYOUT_KEY)
//    }
//}

@Composable
fun getCustomLayout(): Layout {
    val layoutString = getValue(mainActivity.baseContext, CUSTOM_LAYOUT_KEY, "")
    var layout: Layout? = null
    layout = if (layoutString.isEmpty())
        // should not happen for the following reason:
        // when the user selects "Custom layout" for the first time, the layout that
        // was previously selected is saved as custom layout
        layoutManager.createModernLayout()
    else
        Json.decodeFromString(layoutString)

    return layout!!
}

fun saveCustomLayout(context: Context, layout: Layout) {
    val layoutString = Json.encodeToString(layout)
    runBlocking {
        saveValue(context, layoutString, CUSTOM_LAYOUT_KEY)
    }
}