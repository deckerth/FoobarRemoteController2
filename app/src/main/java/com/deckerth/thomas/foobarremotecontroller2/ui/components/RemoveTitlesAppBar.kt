package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveTitlesAppBar(appViewModel: AppViewModel){
    AnimatedVisibility(
        visible = appViewModel.removeTitlesMode,
        enter = fadeIn()
    ) {
        TopAppBar(
            title = {
                Text(
                    if (appViewModel.noOfTitlesToRemove > 1)
                        appViewModel.noOfTitlesToRemove.toString() + " " + stringResource(
                            R.string.selected_titles
                        )
                    else
                        appViewModel.noOfTitlesToRemove.toString() + " " + stringResource(
                            R.string.zero_or_one_title_selected
                        )
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    appViewModel.disableRemoveTitlesMode(true)
                }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Close"
                    )
                }

            },
            actions = {
                Row {
                    IconButton(onClick = {
                        appViewModel.displayedPlaylist!!.removeSelectedTracks(
                            appViewModel
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        )
    }
}