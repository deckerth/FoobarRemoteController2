package com.deckerth.thomas.foobarremotecontroller2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.footerPreference
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.textFieldPreference

@Composable
fun SettingsPage(){
    ProvidePreferenceLocals {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            textFieldPreference(
                key = "ip_address_preference",
                defaultValue = "0.0.0.0",
                title = { Text(text = "IP Address") },
                textToValue = { it },
                summary = { Text(text = "http://$it/") }
            )
            listPreference(
                key = "view_mode_preference",
                defaultValue = "Modern",
                values = listOf("Modern", "Classic"),
                title = { Text(text = "View Mode") },
                summary = { Text(text = it) }
            )
            footerPreference(
                key = "footer_preference",
                summary = { Text(text = "Made by Thomas and Paul Decker\nThanks to @zhanghai for the Preference Library") }
            )
        }
    }
}