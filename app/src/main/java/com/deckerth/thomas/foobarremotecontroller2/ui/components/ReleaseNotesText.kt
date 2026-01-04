package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.saveReleaseNotesDisplayedForRelease
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

@Composable
private fun getReleaseNotes(appVersion: String?, withSettingsNote: Boolean) : AnnotatedString {
    return buildAnnotatedString {
        withStyle(headingStyle) {
            append(stringResource(R.string.release_notes_title) + "\n")
        }
        if (appVersion != null)
            withStyle(smallTextStyle) {
                append(stringResource(R.string.release_notes_version, appVersion) + "\n\n")
            }
        withStyle(titleStyle) {
            append(stringResource(R.string.release_notes_1_header) + "\n\n")
        }
        withStyle(textStyle) {
            append(stringResource(R.string.release_notes_1_text) + "\n\n")
        }
//        withStyle(titleStyle) {
//            append(stringResource(R.string.release_notes_2_header) + "\n\n")
//        }
//        withStyle(textStyle) {
//            append(stringResource(R.string.release_notes_2_text) + "\n\n\n")
//        }
//        withStyle(titleStyle) {
//            append(stringResource(R.string.release_notes_3_header) + "\n\n")
//        }
//        withStyle(textStyle) {
//            append(stringResource(R.string.release_notes_3_text) + "\n\n\n")
//        }
        if (withSettingsNote)
            withStyle(smallTextStyle) {
                append(stringResource(R.string.release_notes_remark) + "\n")
            }
    }
}

@Composable
fun ReleaseNotesText(
    appVersion: String?,
    withSettingsNote: Boolean,
    modifier: Modifier = Modifier
) {
    InitStyles()

    val annotatedString = getReleaseNotes(appVersion, withSettingsNote)

    Text(
        text = annotatedString,
        modifier = modifier
            .fillMaxWidth()
    )
}

private fun dismiss(vm: AppViewModel, appVersion: String?) {
    vm.showReleaseNotes = false
    if (appVersion != null && vm.releaseNotesDisplayedForRelease != appVersion) {
        vm.releaseNotesDisplayedForRelease = appVersion
        saveReleaseNotesDisplayedForRelease(mainActivity!!, appVersion)
    }
}

@Composable
fun ReleaseNotes(vm: AppViewModel) {

    val releaseNotesValidity = "2601"

    var appVersion by remember {
        mutableStateOf(
            mainActivity!!.packageManager.getPackageInfo(
                mainActivity!!.packageName,
                0
            ).versionName
        )
    }

    var withSettingsNote by remember { mutableStateOf(false) }

    if (!vm.showReleaseNotes && appVersion != null && vm.releaseNotesDisplayedForRelease < releaseNotesValidity) {
        vm.showReleaseNotes = true
        withSettingsNote =
            true // This note is displayed only if the Release Notes are displayed the first time automatically
    }


    if (vm.showReleaseNotes)
        AlertDialog(
            onDismissRequest = { dismiss(vm, appVersion) },
            dismissButton = {
                TextButton(onClick = { dismiss(vm, appVersion) }) {
                    Text(
                        stringResource(R.string.button_close),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {},
            text = { ReleaseNotesText(appVersion, withSettingsNote) }
        )
}

@Preview(
    showBackground = true,
)
@Composable
fun ReleaseNotesPreview() {
    Foobar2000RemoteControllerTheme {
        ReleaseNotesText("2510", true)
    }
}
