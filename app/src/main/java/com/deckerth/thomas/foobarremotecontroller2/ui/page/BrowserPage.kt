package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.getAddTrackBehavior
import com.deckerth.thomas.foobarremotecontroller2.model.MusicDirectoryEntry
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.BrowserViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.displayedPlaylist
import kotlinx.coroutines.launch

@Composable
fun BrowserPage(vm: BrowserViewModel, navController: NavHostController) {
    val path = vm.getCurrentPath()

    println("FOOB BrowserPage $path")

    val playlistState =
        rememberLazyListState(initialFirstVisibleItemIndex = 0)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val filesAddedMessage = if (displayedPlaylist == null) "" else stringResource(
        R.string.files_added,
        displayedPlaylist!!.playlistEntity.name
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (path != "")
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(modifier = Modifier.padding(8.dp), text = vm.getCurrentPath())
                }

            if (vm.loadingData)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            else
                LazyColumn(state = playlistState) {
                    if (vm.getDirectory().getEntries().isNotEmpty()) {
                        items(vm.getDirectory().getEntries()) { entry ->
                            if (path == vm.getCurrentPath()) // stop drawing if the path was changed
                                DirectoryEntry(vm, entry, navController)
                        }
                    }
                }

            LaunchedEffect(vm.filesAdded) {
                if (vm.filesAdded)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = filesAddedMessage,
                            duration = SnackbarDuration.Short
                        )
                        vm.filesAdded = false
                    }
            }
        }
    }
}

@Composable
fun DirectoryEntry(
    vm: BrowserViewModel,
    entry: MusicDirectoryEntry,
    navController: NavHostController
) {
    val addBehavior = getAddTrackBehavior()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            var modifier: Modifier = Modifier
            if (entry.isDirectory())
                modifier = modifier.clickable {
                    println("FOOB BrowserPage navigating to ${entry.path}")
                    if (entry.isParentDirectory())
                        navController.popBackStack()
                    else
                        navController.navigate("${Screen.Directory.ROUTE_TEMPLATE}${entry.path}")
                }
            if (entry.isDirectory())
                Icon(
                    painter = painterResource(R.drawable.folder),
                    contentDescription = stringResource(R.string.desc_folder),
                    modifier = modifier
                        .align(Alignment.CenterVertically)
                        .size(32.dp)
                        .padding(start = 8.dp)
                )
            else
                Icon(
                    painter = painterResource(R.drawable.audio_file),
                    contentDescription = stringResource(R.string.desc_audio_file),
                    modifier = modifier
                        .align(Alignment.CenterVertically)
                        .size(32.dp)
                        .padding(start = 8.dp)
                )

            Text(
                text = entry.name,
                modifier = modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            )

            if (!entry.isParentDirectory())
                if (entry.isAdded.value)
                    IconButton(onClick = { }){
                        Icon(
                            painter = painterResource(R.drawable.library_add_check),
                            contentDescription = stringResource(R.string.desc_added),
                            modifier = Modifier
                                .size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                else
                    IconButton(onClick = {
                        vm.addToPlaylist(entry, addBehavior)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.library_add),
                            contentDescription = stringResource(R.string.desc_add),
                            modifier = Modifier
                                .size(24.dp),
                        )
                    }
        }
    }
}
