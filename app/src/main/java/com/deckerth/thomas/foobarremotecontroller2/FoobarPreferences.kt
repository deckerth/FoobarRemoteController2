package com.deckerth.thomas.foobarremotecontroller2

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.deckerth.thomas.foobarremotecontroller2.connector.ConnectionManager
import com.deckerth.thomas.foobarremotecontroller2.model.AddTracksBehaviors
import com.deckerth.thomas.foobarremotecontroller2.model.CustomFieldList
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layouts
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "settings")

private val IP_ADDRESS_KEY = stringPreferencesKey("ip_address")
private val USERNAME_KEY = stringPreferencesKey("username")
private val PASSWORD_KEY = byteArrayPreferencesKey("password")
private val IV_STRING_KEY = byteArrayPreferencesKey("iv_string")
private val VIEW_MODE_KEY = intPreferencesKey("view_mode")
private val CUSTOM_LAYOUT_KEY = stringPreferencesKey("custom_layout")
private val FOOBAR_VOLUME_CONTROL_KEY = booleanPreferencesKey("foobar_volume_control")
private val PAUSE_DURING_PHONE_CALLS_KEY = booleanPreferencesKey("pause_during_phone_calls")
private val ADD_TRACK_BEHAVIOR_KEY = intPreferencesKey("add_track_behavior")
private val ALWAYS_ON_DISPLAY_KEY = booleanPreferencesKey("always_on_display")
private val DYNAMIC_COLOR_SCHEME_KEY = booleanPreferencesKey("dynamic_color_scheme")
private val CREATION_OF_NON_EMPTY_PLAYLISTS_KEY =
    booleanPreferencesKey("creation_of_non_empty_playlists")
private val CONNECTIONS_KEY = stringPreferencesKey("connections")
private val RELEASE_KEY = stringPreferencesKey("release")
private val CUSTOM_FIELDS_KEY = stringPreferencesKey("custom_fields")
private val ERROR_LOGGING_KEY = booleanPreferencesKey("error_logging")

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

suspend fun <T> getValueBlocking(context: Context, key: Preferences.Key<T>, initial: T): T {
    return context.dataStore.data
        .map { preferences ->
            preferences[key] ?: initial
        }
        .first() // Wait for the first emission from the Flow
}

fun saveIpAddress(packedIPAddress: String, context: Context) {
    runBlocking {
        saveValue(context, packedIPAddress, IP_ADDRESS_KEY)
    }
}

@Composable
fun getIpAddress(): String {
    return if (mainActivity == null) ""
    else getValue(mainActivity!!, IP_ADDRESS_KEY, "")
}

suspend fun getIpAddressBlocking(): String {
    return if (mainActivity == null) "" else
        getValueBlocking(
            mainActivity!!,
            IP_ADDRESS_KEY,
            ""
        )
}

suspend fun getUsernameBlocking(): String {
    return if (mainActivity == null) "" else getValueBlocking(
        mainActivity!!,
        USERNAME_KEY,
        ""
    )
}

suspend fun getIvStringBlocking(): ByteArray {
    return if (mainActivity == null) ByteArray(0) else getValueBlocking(
        mainActivity!!,
        IV_STRING_KEY,
        ByteArray(0)
    )
}

fun saveCredentials(
    username: String,
    encryptedPassword: ByteArray,
    ivString: ByteArray,
    context: Context
) {
    runBlocking {
        saveValue(context, username, USERNAME_KEY)
        saveValue(context, encryptedPassword, PASSWORD_KEY)
        saveValue(context, ivString, IV_STRING_KEY)
    }
}


suspend fun getPasswordBlocking(): ByteArray {
    return if (mainActivity == null) ByteArray(0) else getValueBlocking(
        mainActivity!!,
        PASSWORD_KEY,
        ByteArray(0)
    )
}

fun saveViewMode(previousMode: Layouts, mode: Layouts, context: Context) {
    layoutManager.changeLayout(previousMode, mode)
    runBlocking {
        saveValue(context, mode.ordinal, VIEW_MODE_KEY)
    }
}

@Composable
fun getViewMode(): Layouts {
    return if (mainActivity == null) Layouts.LAYOUT_MODERN else Layouts.entries[getValue(
        mainActivity!!,
        VIEW_MODE_KEY,
        Layouts.LAYOUT_MODERN.ordinal
    )]
}

@Composable
fun getCustomLayout(): Layout? {
    if (mainActivity == null) return null
    val layoutString = getValue(mainActivity!!.baseContext, CUSTOM_LAYOUT_KEY, "")
    return if (layoutString.isEmpty()) null
    else Json.decodeFromString(layoutString)
}

fun saveCustomLayout(context: Context, layout: Layout) {
    val layoutString = Json.encodeToString(layout)
    runBlocking {
        saveValue(context, layoutString, CUSTOM_LAYOUT_KEY)
    }
}

suspend fun getCustomFieldsBlocking(): CustomFieldList? {
    if (mainActivity == null)
        return null
    val customFieldsString = getValueBlocking(mainActivity!!.baseContext, CUSTOM_FIELDS_KEY, "")
    return if (customFieldsString.isEmpty()) null
    else Json.decodeFromString(customFieldsString)
}

fun saveCustomFields(context: Context, fields: CustomFieldList) {
    val layoutString = Json.encodeToString(fields)
    runBlocking {
        saveValue(context, layoutString, CUSTOM_FIELDS_KEY)
    }
}

suspend fun getErrorLoggingBlocking(): Boolean {
    if (mainActivity == null)
        return false
    return getValueBlocking(mainActivity!!.baseContext, ERROR_LOGGING_KEY, false)
}

fun saveErrorLogging(context: Context, errorLogging: Boolean) {
    runBlocking {
        saveValue(context, errorLogging, ERROR_LOGGING_KEY)
    }
}


fun saveFoobarVolumeControl(enabled: Boolean, context: Context) {
    runBlocking {
        saveValue(context, enabled, FOOBAR_VOLUME_CONTROL_KEY)
    }
}

@Composable
fun getFoobarVolumeControl(): Boolean {
    return if (mainActivity == null) true
    else getValue(mainActivity!!, FOOBAR_VOLUME_CONTROL_KEY, true)
}

suspend fun getFoobarVolumeControlBlocking(): Boolean {
    return if (mainActivity == null) false
    else {
        getValueBlocking(mainActivity!!, FOOBAR_VOLUME_CONTROL_KEY, true)
        getValueBlocking(mainActivity!!, FOOBAR_VOLUME_CONTROL_KEY, true)
    }
}

fun savePauseDuringPhoneCalls(enabled: Boolean, context: Context) {
    runBlocking {
        saveValue(context, enabled, PAUSE_DURING_PHONE_CALLS_KEY)
    }
}

fun saveAlwaysOnDisplay(enabled: Boolean, context: Context) {
    runBlocking {
        saveValue(context, enabled, ALWAYS_ON_DISPLAY_KEY)
    }
}

@Composable
fun getAlwaysOnDisplay(): Boolean {
    return if (mainActivity == null) false
    else getValue(mainActivity!!, ALWAYS_ON_DISPLAY_KEY, false)
}

@Composable
fun getPauseDuringPhoneCalls(): Boolean {
    return if (mainActivity == null) true
    else getValue(mainActivity!!, PAUSE_DURING_PHONE_CALLS_KEY, true)
}

suspend fun getPauseDuringPhoneCallsBlocking(): Boolean {
    return if (mainActivity == null) false else
        getValueBlocking(mainActivity!!, PAUSE_DURING_PHONE_CALLS_KEY, true)
}

fun saveAddTrackBehavior(behavior: AddTracksBehaviors, context: Context) {
    runBlocking {
        saveValue(context, behavior.ordinal, ADD_TRACK_BEHAVIOR_KEY)
    }
}

@Composable
fun getAddTrackBehavior(): AddTracksBehaviors {
    return if (mainActivity == null) AddTracksBehaviors.ADD_BEHAVIOR_ADD_PLAY
    else AddTracksBehaviors.entries[getValue(
        mainActivity!!,
        ADD_TRACK_BEHAVIOR_KEY,
        AddTracksBehaviors.ADD_BEHAVIOR_ADD_PLAY.ordinal
    )]

}

fun saveDynamicColorScheme(enabled: Boolean, context: Context) {
    runBlocking {
        saveValue(context, enabled, DYNAMIC_COLOR_SCHEME_KEY)
    }
}

@Composable
fun getDynamicColorSchemeEnabled(): Boolean {
    return if (mainActivity == null) true
    else getValue(mainActivity!!, DYNAMIC_COLOR_SCHEME_KEY, true)
}

suspend fun getDynamicColorSchemeEnabledBlocking(): Boolean {
    return if (mainActivity == null) true
    else getValueBlocking(
        mainActivity!!,
        DYNAMIC_COLOR_SCHEME_KEY,
        true
    )
}

private const val creationOfNonEmptyPlaylistsInitialValue = true

fun saveCreationOfNonEmptyPlaylists(enabled: Boolean, context: Context) {
    runBlocking {
        saveValue(context, enabled, CREATION_OF_NON_EMPTY_PLAYLISTS_KEY)
    }
}

@Composable
fun getCreationOfNonEmptyPlaylists(): Boolean {
    return if (mainActivity == null) creationOfNonEmptyPlaylistsInitialValue
    else getValue(
        mainActivity!!,
        CREATION_OF_NON_EMPTY_PLAYLISTS_KEY,
        creationOfNonEmptyPlaylistsInitialValue
    )
}

suspend fun getCreationOfNonEmptyPlaylistsBlocking(): Boolean {
    return if (mainActivity == null) creationOfNonEmptyPlaylistsInitialValue
    else getValueBlocking(
        mainActivity!!,
        CREATION_OF_NON_EMPTY_PLAYLISTS_KEY,
        creationOfNonEmptyPlaylistsInitialValue
    )
}

suspend fun getFoobarConnectionsBlocking(): ConnectionManager {
    if (mainActivity == null)
        return ConnectionManager()
    val connectionsString = getValueBlocking(mainActivity!!.baseContext, CONNECTIONS_KEY, "")
    return if (connectionsString.isEmpty()) ConnectionManager()
    else Json.decodeFromString(connectionsString)
}

fun saveFoobarConnections(context: Context, connections: ConnectionManager) {
    val connectionsString = Json.encodeToString(connections)
    runBlocking {
        saveValue(context, connectionsString, CONNECTIONS_KEY)
    }
}

suspend fun getReleaseNotesDisplayedForReleaseBlocking(): String {
    return if (mainActivity == null) ""
    else getValueBlocking(mainActivity!!.baseContext, RELEASE_KEY, "")
}

fun saveReleaseNotesDisplayedForRelease(context: Context, release: String) {
    runBlocking {
        saveValue(context, release, RELEASE_KEY)
    }
}
