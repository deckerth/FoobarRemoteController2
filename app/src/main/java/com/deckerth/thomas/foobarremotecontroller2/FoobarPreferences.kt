package com.deckerth.thomas.foobarremotecontroller2

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "settings")

private val IP_ADDRESS_KEY = stringPreferencesKey("ip_address")
private val VIEW_MODE_KEY = stringPreferencesKey("view_mode")

private fun <T> getFlow(context: Context,key: Preferences.Key<T>): Flow<T?> {
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
    return getValue(mainActivity, IP_ADDRESS_KEY,"0.0.0.0")
}

fun saveViewMode(mode: String, context: Context) {
    runBlocking {
        saveValue(context, mode, VIEW_MODE_KEY)
    }
}

@Composable
fun getViewMode(): String {
    return getValue(mainActivity, VIEW_MODE_KEY,"Modern")
}