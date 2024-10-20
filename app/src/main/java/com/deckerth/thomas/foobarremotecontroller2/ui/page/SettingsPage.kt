package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.footerPreference
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.textFieldPreference

@Composable
fun SettingsPage() {
    Column {
        Title("Connectivity")
        PreferenceItem("IP Address", summary = "http://0.0.0.0", onClick = {
            mainActivity.navController.navigate("DeviceSelectionPage")
        })

        Title("Appearance")

        PreferenceItem("View mode", summary = "Modern", optionList = listOf("Modern", "Classic", "Custom"))
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
fun ListSelector(values: List<String>, onDismissRequest: () -> Unit) {
    var selectedText by remember { mutableStateOf("Select an option") }
    DropdownMenu(expanded = true, onDismissRequest = { onDismissRequest() }) {
        for(option in values) {
            DropdownMenuItem(onClick = {
                selectedText = option
                onDismissRequest()
            }, text =  {
                Text(text = option)
            } )
        }
    }
}

@Composable
fun PreferenceItem(
    title: String,
    summary: String,
    icon: ImageVector? = null,
    onClick: (Boolean) -> Unit = {},
    isChecked: Boolean = false,
    showToggle: Boolean = false,
    optionList: List<String>? = null ,
    isEnabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if(optionList == null)  { onClick(isChecked) } else { expanded = true }
            }
            .fillMaxWidth()
            .padding(start = 17.dp,
                     end = 17.dp,
                bottom = 12.dp,
                top = 12.dp )
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
                if (optionList != null && expanded ) {
                    ListSelector(values = optionList, onDismissRequest = {expanded = false})
                }
            }
        }
        if (showToggle) {
            Switch(
                checked = isChecked,
                enabled = isEnabled,
                onCheckedChange = { onClick(isChecked) },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
fun SettingsPreview() {
    SettingsPage()
}

@Composable
fun SettingsPageOld() {
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