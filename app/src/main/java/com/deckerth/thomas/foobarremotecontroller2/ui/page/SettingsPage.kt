@file:OptIn(ExperimentalMaterial3Api::class)

package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.enableVolumeControl
import com.deckerth.thomas.foobarremotecontroller2.getAddTrackBehavior
import com.deckerth.thomas.foobarremotecontroller2.getFoobarVolumeControl
import com.deckerth.thomas.foobarremotecontroller2.getIpAddress
import com.deckerth.thomas.foobarremotecontroller2.getPauseDuringPhoneCalls
import com.deckerth.thomas.foobarremotecontroller2.getViewMode
import com.deckerth.thomas.foobarremotecontroller2.model.AddTracksBehaviors
import com.deckerth.thomas.foobarremotecontroller2.saveAddTrackBehavior
import com.deckerth.thomas.foobarremotecontroller2.saveFoobarVolumeControl
import com.deckerth.thomas.foobarremotecontroller2.savePauseDuringPhoneCalls
import com.deckerth.thomas.foobarremotecontroller2.saveViewMode
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layouts
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

@Composable
fun SettingsPage() {

    val snackbarHostState = remember { SnackbarHostState() }
     Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            Title(stringResource(R.string.settings_connectivity))
            PreferenceItem<Boolean>(
                stringResource(R.string.field_ip_address),
                summary = getIpAddress(),
                onClick = {
                    mainActivity.navigateTo("DeviceSelectionPage")
                })

            Title(stringResource(R.string.settings_appearance))
            var isOpen by remember { mutableStateOf(false) }
            val viewMode = getViewMode()
            PreferenceItem<Boolean>(stringResource(R.string.settings_view_mode),
                summary = viewMode.text,
                showButton = viewMode == Layouts.LAYOUT_CUSTOM,
                buttonText = stringResource(R.string.open_layout_editor),
                onButtonClick = { mainActivity.navigateTo("Layout selection") },
                onClick = {
                    isOpen = true
                })
            if (isOpen) {
                ListPreference(
                    values = Layouts.entries,
                    title = stringResource(R.string.settings_view_mode),
                    selectedItem = getViewMode(),
                    onClick = { mode: Layouts? ->
                        if (mode != null) {
                            saveViewMode(viewMode, mode, mainActivity)
                        }
                        isOpen = false
                    },
                    getText = { v: Layouts -> v.text }
                )
            }

            Title(stringResource(R.string.settings_playback))
            PreferenceItem(
                stringResource(R.string.settings_foobar_volume_control),
                summary = "",
                onClick = { enabled: Boolean ->
                    val intendedSettingActive = !enabled
                    saveFoobarVolumeControl(intendedSettingActive, mainActivity)
                    if (intendedSettingActive)
                        enableVolumeControl()
                    else
                        mainActivity.restartService()
                },
                showToggle = true,
                isChecked = getFoobarVolumeControl(),
                isEnabled = true
            )

            PreferenceItem(
                stringResource(R.string.settings_pause_during_phone_call),
                summary = "",
                onClick = { enabled: Boolean -> savePauseDuringPhoneCalls(!enabled, mainActivity) },
                showToggle = true,
                isChecked = getPauseDuringPhoneCalls(),
                isEnabled = true
            )

            Title(stringResource(R.string.settings_browser))
            var isChooseAddOptionsOpen by remember { mutableStateOf(false) }
            val behavior = getAddTrackBehavior()
            PreferenceItem<Boolean>(stringResource(R.string.settings_add_behavior),
                summary = behavior.text,
                showButton = false,
                onClick = {
                    isChooseAddOptionsOpen = true
                })
            if (isChooseAddOptionsOpen) {
                ListPreference(
                    values = AddTracksBehaviors.entries,
                    title = stringResource(R.string.settings_add_behavior),
                    selectedItem = getAddTrackBehavior(),
                    onClick = { chosen: AddTracksBehaviors? ->
                        if (chosen != null) {
                            saveAddTrackBehavior(chosen, mainActivity)
                        }
                        isChooseAddOptionsOpen = false
                    },
                    getText = { v: AddTracksBehaviors -> v.text }
                )
            }
        }
    }
}

@Composable
fun Title(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier
            .padding(start = 17.dp)
            .padding(bottom = 8.dp)
            .padding(top = 8.dp),
    )
}

@Composable
fun PageTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
            .padding(start = 17.dp)
            .padding(bottom = 16.dp)
            .padding(top = 16.dp),
    )
}

@Composable
fun ListSelector(values: List<String>, onDismissRequest: (String?) -> Unit) {
    val textSelectAnOption = stringResource(R.string.text_select_an_option)
    var selectedText by remember { mutableStateOf(textSelectAnOption) }
    DropdownMenu(expanded = true, onDismissRequest = { onDismissRequest(null) }) {
        for (option in values) {
            DropdownMenuItem(onClick = {
                selectedText = option
                onDismissRequest(selectedText)
            }, text = {
                Text(text = option)
            })
        }
    }
}

@Composable
fun <T> PreferenceItem(
    title: String,
    summary: String,
    icon: ImageVector? = null,
    onClick: (T) -> Unit = {},
    isChecked: Boolean = false,
    showToggle: Boolean = false,
    showButton: Boolean = false,
    buttonText: String = "",
    onButtonClick: () -> Unit = {},
    optionList: List<String>? = null,
    isEnabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (optionList == null) {
                    @Suppress("UNCHECKED_CAST")
                    onClick(isChecked as T)
                } else {
                    expanded = true
                }
            }
            .fillMaxWidth()
            .padding(
                start = 17.dp,
                end = 17.dp,
                bottom = 12.dp,
                top = 12.dp
            )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(end = 22.dp),
            )
        }
        Row(modifier = Modifier.weight(0.5F)) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.headlineMedium,
                )
                if (summary.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.alpha(0.60F),
                    )
                }
                if (optionList != null && expanded) {
                    ListSelector(
                        values = optionList,
                        onDismissRequest = { value: String? ->
                            expanded = false
                            if (value != null) {
                                @Suppress("UNCHECKED_CAST")
                                onClick(value as T)
                            }
                        },
                    )
                }
            }
        }
        if (showToggle) {
            Switch(
                checked = isChecked,
                enabled = isEnabled,
                onCheckedChange = {
                    @Suppress("UNCHECKED_CAST")
                    onClick(isChecked as T)
                },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        if (showButton) {
            Button(
                enabled = isEnabled,
                onClick = { onButtonClick() },
                modifier = Modifier.padding(start = 30.dp),
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun <T> ListPreference(
    values: List<T>,
    title: String,
    selectedItem: T,
    onClick: (T?) -> Unit,
    getText: (T) -> String = { v -> v.toString() }
) {
    var currentItem by remember { mutableStateOf(selectedItem) }
    currentItem = selectedItem

    AlertDialog(
        onDismissRequest = { onClick(null) },
        confirmButton = {
            TextButton(onClick = { onClick(null) }) {
                Text(
                    stringResource(android.R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {

            LazyColumn {
                items(values) { value ->
                    Row(
                        modifier = Modifier
                            .clickable {
                                println("FOOB $value")
                                onClick(value)
                            }
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentItem == value,
                            onClick = {
                                currentItem = value
                                onClick(value)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = getText(value))
                    }
                }
            }
        },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}


@Preview(
    showBackground = true
)
@Composable
fun SettingsPreview() {
    SettingsPage()
}
