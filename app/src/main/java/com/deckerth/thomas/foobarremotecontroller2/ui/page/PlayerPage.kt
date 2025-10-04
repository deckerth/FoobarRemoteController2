@file:OptIn(ExperimentalMaterial3Api::class)

package com.deckerth.thomas.foobarremotecontroller2.ui.page

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackMode
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistLifecycleState
import com.deckerth.thomas.foobarremotecontroller2.ui.components.ArtWork
import com.deckerth.thomas.foobarremotecontroller2.ui.components.LayoutComponent
import com.deckerth.thomas.foobarremotecontroller2.ui.components.TitleDetails
import com.deckerth.thomas.foobarremotecontroller2.ui.components.UserPasswordDialog
import com.deckerth.thomas.foobarremotecontroller2.ui.isTablet
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayingPage(vm: AppViewModel) {
    val pullToRefreshState = rememberPullToRefreshState()
    var showLoading = vm.loadingList
    var isRefreshing by remember { mutableStateOf(false) }
    var maxBoxHeight by remember { mutableStateOf(0.dp) }
    var maxBoxWidth by remember { mutableStateOf(0.dp) }
    val coroutineScope = rememberCoroutineScope()
    layoutManager.InitLayoutManager()
    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh =
            {
                isRefreshing = true
                coroutineScope.launch  {
                    onRefresh(vm)
                    delay(1000)
                    isRefreshing = false
                }
            },
        modifier = Modifier
            .fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        )
        {
            maxBoxHeight = maxHeight
            maxBoxWidth = maxWidth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState(), enabled = true)
            ) {
                if (vm.player == null) {
                    showLoading = !vm.isSick
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        if (vm.askForPassword)
                            UserPasswordDialog(vm)
                        Icon(
                            painter = painterResource(R.drawable.signal_disconnected),
                            contentDescription = stringResource(R.string.desc_album_picture),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(180.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            text = stringResource(R.string.info_no_connection),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            onClick = { onRefresh(vm) }
                        ) {
                            Text(text = "Refresh")
                        }
                    }
                } else
                    PlayerCard(
                        vm,
                        player = vm.player!!,
                        boxSize = IntSize(maxBoxWidth.value.toInt(), maxBoxHeight.value.toInt())
                    )
            }
        }
    }
    if (!mainActivity.isTablet() && showLoading)
        LinearProgressIndicator(
            progress = { vm.loadingListProgress },
            modifier = Modifier.fillMaxWidth(),
        )
}

fun onRefresh(vm: AppViewModel) {
    vm.playlistsViewModel.restartObserver()
}

@Composable
fun PlayerButtons(
    vm: AppViewModel,
    player: Player,
    onPreviousTrack: () -> Unit = { vm.playerAccess.previousTrack() },
    onNextTrack: () -> Unit = { vm.playerAccess.nextTrack() },
    previewMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val space = 15.dp
        IconButton(
            onClick = {
                if (!previewMode)
                    when (player.playbackMode) {
                        PlaybackMode.REPEAT_TRACK -> vm.playerAccess
                            .setPlaybackMode(PlaybackMode.DEFAULT)

                        PlaybackMode.REPEAT_PLAYLIST -> vm.playerAccess
                            .setPlaybackMode(PlaybackMode.REPEAT_TRACK)

                        else -> vm.playerAccess
                            .setPlaybackMode(PlaybackMode.REPEAT_PLAYLIST)
                    }
            },
            modifier = Modifier
                .size(40.dp)
        ) {
            Icon(
                painter = when (player.playbackMode) {
                    PlaybackMode.REPEAT_TRACK -> painterResource(R.drawable.repeat_one_on)
                    PlaybackMode.REPEAT_PLAYLIST -> painterResource(R.drawable.repeat_on)
                    else -> painterResource(R.drawable.repeat)
                },
                contentDescription = stringResource(R.string.desc_repeat),
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(space))
        IconButton(
            onClick = onPreviousTrack,
            enabled = player.playbackState != PlaybackState.STOPPED,
            modifier = Modifier
                .size(60.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_previous),
                contentDescription = stringResource(R.string.desc_skip_previous),
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(space))
        FilledIconButton(
            onClick = {
                if (!previewMode)
                    if (player.playbackState == PlaybackState.PLAYING)
                        vm.playerAccess.pausePlayback()
                    else
                        vm.playerAccess.startPlayback()
            },
            modifier = Modifier
                .size(60.dp)
        ) {
            if (player.playbackState == PlaybackState.PAUSED || player.playbackState == PlaybackState.STOPPED) {
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = stringResource(R.string.desc_play),
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.pause),
                    contentDescription = stringResource(R.string.desc_pause),
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxSize()
                )
            }

        }
        Spacer(modifier = Modifier.width(space))
        IconButton(
            onClick = onNextTrack,
            enabled = player.playbackState != PlaybackState.STOPPED,
            modifier = Modifier
                .size(60.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_next),
                contentDescription = stringResource(R.string.desc_skip_next),
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(space))
        IconButton(
            onClick = {
                if (!previewMode)
                    when (player.playbackMode) {
                        PlaybackMode.SHUFFLE_TRACKS -> vm.playerAccess
                            .setPlaybackMode(PlaybackMode.DEFAULT)

                        PlaybackMode.SHUFFLE_ALBUMS -> vm.playerAccess
                            .setPlaybackMode(PlaybackMode.DEFAULT)

                        PlaybackMode.SHUFFLE_FOLDERS -> vm.playerAccess
                            .setPlaybackMode(PlaybackMode.DEFAULT)

                        else -> vm.playerAccess
                            .setPlaybackMode(PlaybackMode.SHUFFLE_TRACKS)
                    }
            },
            modifier = Modifier
                .size(40.dp)
        ) {
            Icon(
                painter = when (player.playbackMode) {
                    PlaybackMode.SHUFFLE_TRACKS -> painterResource(R.drawable.shuffle_on)
                    PlaybackMode.SHUFFLE_ALBUMS -> painterResource(R.drawable.shuffle_on)
                    PlaybackMode.SHUFFLE_FOLDERS -> painterResource(R.drawable.shuffle_on)
                    else -> painterResource(R.drawable.shuffle)
                },
                contentDescription = stringResource(R.string.desc_shuffle),
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun PlayerStopped(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(R.drawable.stop_circle),
            contentDescription = stringResource(R.string.desc_album_picture),
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .size(180.dp)
                .clip(MaterialTheme.shapes.medium)
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            textAlign = TextAlign.Center,
            text = stringResource(R.string.info_no_track_playing),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun PlayerCard(
    vm: AppViewModel,
    player: Player,
    previewMode: Boolean = false,
    onPreviousTrack: (() -> Unit)? = null,
    onNextTrack: (() -> Unit)? = null,
    boxSize: IntSize
) {
    // In phone mode: Trigger loading of the current playlist if it is not already loaded.
    if (!mainActivity.isTablet())
        if(vm.displayedPlaylist != null && vm.displayedPlaylist!!.lifecycleState == PlaylistLifecycleState.RequiresUpdate) {
            vm.displayedPlaylist!!.lifecycleState = PlaylistLifecycleState.Valid
            vm.displayedPlaylist = null
        }

    // The main layout container, occupying the entire screen.
    Column(
        modifier = Modifier
            .requiredSize(boxSize.width.dp, boxSize.height.dp)
    ) {
        // This Box is used to contain the image and make it occupy the remaining space.
        Box(
            modifier = Modifier
                // Assigns a weight of 1 to the Box, making it take up all available space not used by other elements in the Column.
                .weight(1f)
                // Makes the Box occupy the full width of the screen.
                .fillMaxWidth()
        ) {
            if (player.playbackState == PlaybackState.STOPPED)
                PlayerStopped(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center))
            else
                ArtWork(player, previewMode = previewMode, vm)
        }

        // This Column contains the text fields and other controls.
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                if (player.playbackState != PlaybackState.STOPPED) {
                    val layout = layoutManager.getLayout()

                    for (item in layout.playerLayout.items) {
                        if (item.item != LayoutItems.ARTWORK)
                            LayoutComponent(vm, player, item)
                    }
                }
                if (onPreviousTrack != null && onNextTrack != null)
                    PlayerButtons(
                        vm,
                        player,
                        onPreviousTrack,
                        onNextTrack,
                        previewMode,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                else
                    PlayerButtons(vm, player, modifier = Modifier.padding(bottom = 24.dp))
            }
            var infoButtonClicked by remember { mutableStateOf(false) }
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = { infoButtonClicked = true }
            ) {
                Icon(
                    painter = painterResource(R.drawable.info_i),
                    contentDescription = stringResource(R.string.button_details),
                    modifier = Modifier.size(16.dp)
                )
            }
            if (infoButtonClicked && !previewMode)
                TitleDetails(
                    vm = vm,
                    onDismiss = { infoButtonClicked = false },
                )
        }
    }
}

@Preview(
    name = "light mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "dark mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)

@Composable
fun PlayerCardPreview() {
    Foobar2000RemoteControllerTheme {
        Surface {
            PlayerCard(
                vm = AppViewModel(),
                player = Player(
                    "label",
                    "xyz",
                    "",
                    "Album",
                    "Title",
                    "Artist",
                    "44100",
                    "Pop",
                    "0",
                    "02",
                    "0:28",
                    "",
                    "",
                    "955.8266666666667",
                    "28.9795",
                    "",
                    PlaybackState.PLAYING,
                    PlaybackMode.DEFAULT,
                    ""
                ),
                boxSize = IntSize(200, 200)
            )
        }

    }
}