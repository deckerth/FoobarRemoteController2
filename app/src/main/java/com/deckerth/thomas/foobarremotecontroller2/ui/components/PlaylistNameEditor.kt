package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

private var playlistName by mutableStateOf("")
private var createEmptyPlaylist by mutableStateOf(true)

private fun nameIsValid(vm: AppViewModel?): Boolean {
    if (vm != null && playlistName.isNotBlank())
        if (!vm.playlistsViewModel.playlistExists(playlistName)) return true
    return false
}

@Composable
fun PlaylistNameDialog(
    modifier: Modifier = Modifier,
    vm: AppViewModel? = null,
    onConfirm: (String, Boolean) -> Unit,
    onDismiss: () -> Unit = {}
) {
    AlertDialog(onDismissRequest = {
        playlistName = ""
        vm?.createPlaylistRequest = false
        createEmptyPlaylist = true
        onDismiss()
    }, dismissButton = {
        TextButton(onClick = {
            playlistName = ""
            vm?.createPlaylistRequest = false
            onDismiss()
        }) {
            Text(
                stringResource(R.string.cancel), style = MaterialTheme.typography.bodyMedium
            )
        }
    }, confirmButton = {
        TextButton(
            onClick = {
                vm?.createPlaylistRequest = false
                onConfirm(playlistName, createEmptyPlaylist)
                playlistName = ""
            },
            enabled = nameIsValid(vm)
        ) {
            Text(
                stringResource(R.string.button_create_playlist),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }, title = {
        Text(
            text = stringResource(R.string.create_playlist),
            style = MaterialTheme.typography.headlineSmall
        )
    }, text = {
        Column {
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                label = { Text(stringResource(R.string.playlist_name)) },
                singleLine = true,
                isError = !nameIsValid(vm),
                trailingIcon = {
                    if (playlistName.isNotBlank())
                        IconButton(onClick = {
                            playlistName = ""
                        }) {
                            Icon(Icons.Filled.Clear, "Clear Icon")
                        }
                },
                modifier = modifier
                    .padding(8.dp)
            )

            if (vm == null || playlistName.isNotBlank() && !nameIsValid(vm))
                Text(
                    text = stringResource(R.string.invalid_playlist_name),
                    style = MaterialTheme.typography.bodyMedium
                )

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (createEmptyPlaylist),
                        onClick = { createEmptyPlaylist = !createEmptyPlaylist },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (createEmptyPlaylist),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = stringResource(R.string.create_empty_playlist),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (!createEmptyPlaylist),
                        onClick = { createEmptyPlaylist = !createEmptyPlaylist },
                        enabled = vm?.displayedPlaylist != null && vm.displayedPlaylist!!.isNotEmpty(vm),
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (!createEmptyPlaylist),
                    enabled = vm?.displayedPlaylist != null && vm.displayedPlaylist!!.isNotEmpty(vm),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = stringResource(R.string.choose_titles_from_current_playlist),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }, modifier = modifier
        .padding(16.dp)
        .fillMaxWidth()
    )
}

@Preview(
    showBackground = true,
)

@Composable
fun PlaylistNameDialogPreview() {
    Foobar2000RemoteControllerTheme {
        PlaylistNameDialog(onConfirm = { _, _ -> }, onDismiss = {})
    }
}
