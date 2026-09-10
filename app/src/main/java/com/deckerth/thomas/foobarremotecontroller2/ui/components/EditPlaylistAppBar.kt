package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaylistEditOperation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlaylistAppBar(appViewModel: AppViewModel) {
    var playlistSelectorExpanded by remember {
        mutableStateOf(false)
    }

    AnimatedVisibility(
        visible = appViewModel.playlistEditMode,
        enter = fadeIn()
    ) {
        TopAppBar(
            title = {
                Text(
                    if (appViewModel.noOfSelectedTitles > 1)
                        appViewModel.noOfSelectedTitles.toString() + " " + stringResource(
                            R.string.selected_titles
                        )
                    else
                        appViewModel.noOfSelectedTitles.toString() + " " + stringResource(
                            R.string.zero_or_one_title_selected
                        )
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    appViewModel.disablePlaylistEditMode(true)
                }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Close"
                    )
                }

            },
            actions = {
                Row {
                    IconButton(
                        onClick = {
                            appViewModel.displayedPlaylist!!.selectAllTracks(
                                appViewModel
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.select_all),
                            contentDescription = "Select all"
                        )
                    }
                    IconButton(
                        onClick = {
                            appViewModel.displayedPlaylist!!.deselectAllTracks(
                                appViewModel
                            )
                        },
                        enabled = appViewModel.noOfSelectedTitles > 0
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.remove_selection),
                            contentDescription = "Remove selection"
                        )
                    }
                    when (appViewModel.playlistEditOperation) {
                        PlaylistEditOperation.REMOVE -> IconButton(
                            onClick = {
                                appViewModel.displayedPlaylist!!.removeSelectedTracks(
                                    appViewModel
                                )
                            },
                            enabled = appViewModel.noOfSelectedTitles > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }

                        PlaylistEditOperation.COPY -> {
                            IconButton(
                                onClick = {
                                    playlistSelectorExpanded = true
                                },
                                enabled = appViewModel.noOfSelectedTitles > 0
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.content_copy),
                                    contentDescription = "Copy"
                                )
                            }

                            PlaylistSelector(
                                appViewModel,
                                playlistSelectorExpanded,
                                onClick = {
                                    playlistSelectorExpanded = false
                                    if (it != null) {
                                        appViewModel.displayedPlaylist!!.copySelectedTitles(
                                            appViewModel,
                                            it
                                        )
                                    }
                                }
                            )
                        }

                        PlaylistEditOperation.ADD_TO_PLAYBACK_QUEUE -> IconButton(
                            onClick = {
                                appViewModel.displayedPlaylist!!.addSelectedTitlesToPlaybackQueue(
                                    appViewModel
                                )
                            },
                            enabled = appViewModel.noOfSelectedTitles > 0
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.play),
                                contentDescription = "Add titles to playback queue"
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun PlaylistSelector(
    appViewModel: AppViewModel,
    expanded: Boolean,
    onClick: (PlaylistEntity?) -> Unit
) {
    val playlists = appViewModel.playlistsViewModel.playlists
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onClick(null) }) {
        playlists.playlists.forEachIndexed { index, playlistEntity ->
            if (playlistEntity.playlistId != appViewModel.displayedPlaylist!!.playlistEntity.playlistId)
                DropdownMenuItem(
                    text = {
                        Text(text = playlistEntity.name)
                    },
                    onClick = { onClick(playlists.playlists[index]) })
        }
    }
}