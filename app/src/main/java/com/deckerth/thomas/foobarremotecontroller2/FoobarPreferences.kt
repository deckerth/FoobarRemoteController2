package com.deckerth.thomas.foobarremotecontroller2

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "settings")

private val IP_ADDRESS_KEY = stringPreferencesKey("ip_address")

fun saveIpAddress(ip: String, context: Context) {
    runBlocking {
        context.dataStore.edit { preferences ->
            preferences[IP_ADDRESS_KEY] = ip
        }
    }
}

fun getIpAddress(context: Context): Flow<String?> {
    return context.dataStore.data
        .map { preferences ->
            preferences[IP_ADDRESS_KEY] ?: "0.0.0.0"
        }
}