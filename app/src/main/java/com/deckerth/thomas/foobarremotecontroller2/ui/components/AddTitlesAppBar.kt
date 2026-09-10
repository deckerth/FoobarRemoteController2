package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.getAddTrackBehavior
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTitlesAppBar(appViewModel: AppViewModel) {
    val addBehavior = getAddTrackBehavior()
    AnimatedVisibility(
        visible = appViewModel.addTitlesMode,
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
                    appViewModel.playlistsViewModel.nameOfNewPlaylist = ""
                    appViewModel.disableAddTitlesMode()
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
                    IconButton(
                        onClick = {
                            appViewModel.playlistsViewModel.addPlaylist(addBehavior)
                        },
                        enabled = appViewModel.noOfSelectedTitles > 0
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.format_list_bulleted_add),
                            contentDescription = "New playlist"
                        )
                    }
//                    Button(onClick = {
//                        appViewModel.playlistsViewModel.addPlaylist(addBehavior)
//                    }) {
//                        Text(stringResource(R.string.button_create_playlist))
//                    }
                }
            }
        )
    }
}