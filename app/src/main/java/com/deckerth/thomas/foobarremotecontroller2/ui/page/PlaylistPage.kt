package com.deckerth.thomas.foobarremotecontroller2.ui.page

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.getAddTrackBehavior
import com.deckerth.thomas.foobarremotecontroller2.model.Album
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistLifecycleState
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists
import com.deckerth.thomas.foobarremotecontroller2.model.SelectableTitle
import com.deckerth.thomas.foobarremotecontroller2.ui.components.ImageWithLoadingPlaceholder
import com.deckerth.thomas.foobarremotecontroller2.ui.components.LayoutComponent
import com.deckerth.thomas.foobarremotecontroller2.ui.components.PlaylistNameDialog
import com.deckerth.thomas.foobarremotecontroller2.ui.components.PlaylistSelector
import com.deckerth.thomas.foobarremotecontroller2.ui.components.TitleDetails
import com.deckerth.thomas.foobarremotecontroller2.ui.components.TitleSearchBarDialog
import com.deckerth.thomas.foobarremotecontroller2.ui.isTablet
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.highlightPlayingItem
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel

var showCoverFullscreen by mutableStateOf(false)
var imageUrl by mutableStateOf("")

@Composable
fun PlaylistPage(vm: AppViewModel) {
    // The currently displayed playlists may have been changed in foobar so that is got invalidated
    val colors = MaterialTheme.colorScheme

    if (vm.displayedPlaylist != null && vm.displayedPlaylist!!.lifecycleState != PlaylistLifecycleState.Valid) {
        val displayToast = vm.playlistEditMode || vm.playlistsViewModel.showFilter

        if (vm.playlistEditMode) vm.disablePlaylistEditMode(true)
        if (vm.addTitlesMode) vm.disableAddTitlesMode()

        if (vm.playlistsViewModel.filterValue.isActive) {
            vm.playlistsViewModel.showFilter = false
            vm.playlistsViewModel.filterValue.clear()
        }

        println("FOOBPLAYLISTPAGE Clearing playlist: ${vm.displayedPlaylist!!.playlistEntity.playlistId}")
        vm.displayedPlaylist!!.clear()
        if (vm.displayedPlaylist!!.lifecycleState == PlaylistLifecycleState.RequiresUpdate) vm.displayedPlaylist!!.lifecycleState =
            PlaylistLifecycleState.Valid // for delayed update to avoid crashes during layout update

        vm.displayedPlaylist = null

        if (displayToast) Toast.makeText(
            mainActivity, stringResource(R.string.playlist_changed_in_foobar), Toast.LENGTH_SHORT
        ).show()
    }

    Column {
        PlaylistSwitcher(vm, playlists = vm.playlistsViewModel.playlists)

        if (vm.displayedPlaylist != null && vm.displayedPlaylist!!.lifecycleState == PlaylistLifecycleState.Valid)
            Playlist(
                vm,
                vm.displayedPlaylist!!
            )
    }

    if (showCoverFullscreen)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .clickable { showCoverFullscreen = false }
        ) {
            ImageWithLoadingPlaceholder(
                imageUrl, Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { showCoverFullscreen = false }, vm
            )
        }

    if (vm.playlistsViewModel.showFilter) TitleSearchBarDialog(
        vm = vm,
        onSearch = { searchString, genre, highRes ->
            // activates the filter automatically if values are not initial
            vm.playlistsViewModel.filterValue.pattern = searchString
            vm.playlistsViewModel.filterValue.genre = genre
            vm.playlistsViewModel.filterValue.highRes = highRes
            vm.playlistsViewModel.showFilter = false
            if (vm.playlistsViewModel.filterValue.isActive) vm.autoscroll = false
            vm.displayedPlaylist!!.applyFilter(vm)
        },
        onDismiss = {
            vm.playlistsViewModel.showFilter = false
            vm.playlistsViewModel.filterValue.clear()
        })

    if (vm.createPlaylistRequest) PlaylistNameDialog(
        vm = vm, onConfirm = { playlistName, createEmpty ->
            vm.playlistsViewModel.nameOfNewPlaylist = playlistName
            if (createEmpty) {
                vm.playlistsViewModel.addPlaylist()
            } else vm.addTitlesMode = true
        })

    if (!mainActivity!!.isTablet() && (vm.loadingList || vm.displayedPlaylist == null)) LinearProgressIndicator(
        progress = {
            vm.loadingListProgress
        },
        modifier = Modifier.fillMaxWidth(),
    )
    if (vm.displayToastOnPlaylistPage) {
        Toast.makeText(
            mainActivity,
            vm.toastOnPlaylistPageMessage,
            Toast.LENGTH_SHORT
        ).show()
        vm.displayToastOnPlaylistPage = false
    }
}

@Composable
fun TitleDropdownMenu(vm: AppViewModel, title: ITitle? = null, album: Album? = null) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var showTitleDetails by remember { mutableStateOf(false) }
    var playlistSelectorExpanded by remember { mutableStateOf(false) }

    val addBehavior = getAddTrackBehavior()

    if (vm.displayedPlaylist == null) return
    Box {
        IconButton(
            onClick = { dropdownMenuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = dropdownMenuExpanded,
            onDismissRequest = { dropdownMenuExpanded = false }) {

            if (title != null || album != null)
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_item_add_to_playback_queue)) },
                    onClick = {
                        if (title != null)
                            vm.displayedPlaylist!!.addTitleToPlaybackQueue(
                                vm,
                                vm.displayedPlaylist!!.playlistEntity.playlistId,
                                title.index
                            )
                        else if (album != null)
                            vm.displayedPlaylist!!.addAlbumToPlaybackQueue(vm, album)
                        dropdownMenuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = "Add to playback queue"
                        )
                    }
                )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_item_add_to_playlist)) },
                onClick = {
                    playlistSelectorExpanded = true
                    dropdownMenuExpanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add music"
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_item_remove_from_playlist)) },
                onClick = {
                    if (title != null)
                        vm.displayedPlaylist!!.removeTitleFromPlaylist(vm, title)
                    else if (album != null)
                        vm.displayedPlaylist!!.removeAlbumFromPlaylist(vm, album)
                    dropdownMenuExpanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete titles from playlist"
                    )
                }
            )
            if (title != null || (album != null && album.tracks.size == 1)) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_item_show_title_details)) },
                    onClick = {
                        dropdownMenuExpanded = false
                        showTitleDetails = true
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.info_i),
                            contentDescription = stringResource(R.string.button_details),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }

    PlaylistSelector(
        vm,
        playlistSelectorExpanded,
        onClick = {
            playlistSelectorExpanded = false
            if (it != null) {
                if (title != null)
                    vm.displayedPlaylist!!.addTitleToPlaylist(
                        vm,
                        title,
                        it.playlistId,
                        addBehavior
                    )
                else if (album != null)
                    vm.displayedPlaylist!!.addAlbumToPlaylist(vm, album, it.playlistId, addBehavior)
            }
        }
    )

    if (showTitleDetails)
        if (title != null)
            TitleDetails(
                title = title,
                onDismiss = { showTitleDetails = false },
            )
        else if (album != null && album.tracks.size == 1)
            TitleDetails(
                title = album.tracks[0].details,
                onDismiss = { showTitleDetails = false },
            )
}

fun playlistInfoString(vm: AppViewModel): String {
    val count = if (vm.displayedPlaylist != null) {
        if (vm.playlistsViewModel.filterValue.isActive) vm.displayedPlaylist!!.filteredTitles.count() else vm.displayedPlaylist!!.titles.count()
    } else 0
    return if (vm.playlistsViewModel.filterValue.isActive)
        if (count == 1)
            mainActivity!!.getString(
                R.string.playlist_titles_singular
            ) + mainActivity!!.getString(R.string.playlist_filtered_indicator)
        else
            mainActivity!!.getString(
                R.string.playlist_titles_plural, count.toString()
            ) + mainActivity!!.getString(R.string.playlist_filtered_indicator)
    else
        if (count == 1) mainActivity!!.getString(R.string.playlist_titles_singular)
        else
            mainActivity!!.getString(R.string.playlist_titles_plural, count.toString())
}

@Composable
fun AlbumCard(
    vm: AppViewModel,
    playerViewModel: PlayerViewModel,
    album: Album,
    layout: Layout,
    previewMode: Boolean = false
) {

    /*
    in automatic mode (initial state):
      automatically expand albums that are played, collapse them otherwise
    automatic mode is left when the user collapses a playing album, or expands a not playing album
    automatic mode is re-entered otherwise
    */

    if (!previewMode && playerViewModel.valid) {
        val currentlyPlaying =
            album.originalTitle.playlistId == playerViewModel.playlistId && album.hasIndex(
                playerViewModel.getIndex()
            )
        if (album.isAutomaticSelection) album.isExpanded = currentlyPlaying
        else album.isAutomaticSelection = album.isExpanded == currentlyPlaying
    }

    val albumIsCurrentlyPlaying =
        playerViewModel.valid && album.originalTitle.playlistId == playerViewModel.playlistId && album.hasIndex(
            playerViewModel.getIndex()
        )

    // if the album contains a single title without name, the album itself represents the title, and can be selected
    val albumRepresentsTitle = album.tracks.size == 1 && album.tracks[0].details.title == ""
    val renderTitleProgressBar = albumRepresentsTitle && !layout.albumLayoutHasProgressBar && layout.titleLayoutHasProgressBar

    var modifier: Modifier = Modifier
    var titleSelected = false

    if (albumRepresentsTitle) {
        if (playerViewModel.valid) titleSelected =
            album.tracks[0].details.index == playerViewModel.getIndex() && album.tracks[0].details.playlistId == playerViewModel.playlistId

        if (titleSelected &&
            ( renderTitleProgressBar && highlightPlayingItem(layout.titleLayout) || !renderTitleProgressBar && highlightPlayingItem(layout.albumLayout))) modifier =
            modifier.background(MaterialTheme.colorScheme.primaryContainer)
    }

    if (album.tracks.isNotEmpty() && !previewMode) modifier = modifier.clickable {
        if (vm.playlistEditMode || vm.addTitlesMode) album.toggleIsSelected()
        else {
            vm.playlistsViewModel.filterValue.clear()
            vm.playlistsViewModel.showFilter = false

            if (titleSelected)
                if (playerViewModel.playbackState == PlaybackState.PLAYING)
                    vm.playerAccess.pausePlayback()
                else
                    vm.playerAccess.startPlayback()
            else
                vm.playerAccess.playTrack(
                    album.tracks[0].details.playlistId, album.tracks[0].details.index
                )
            vm.autoscroll = true
        }
    }

    ElevatedCard(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(modifier = modifier) {
            Row {
                if (vm.playlistEditMode || vm.addTitlesMode) TriStateCheckbox(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    state = album.isSelected,
                    onClick = {
                        album.toggleIsSelected()
                    })
                if (previewMode) Image(
                    bitmap = ImageBitmap.imageResource(id = album.originalTitle.artworkUrl.toInt()),
                    contentDescription = stringResource(R.string.desc_album_picture),
                    modifier = Modifier.size(80.dp)
                )
                else ImageWithLoadingPlaceholder(
                    imageUrl = album.originalTitle.artworkUrl,
                    modifier = Modifier
                        .size(80.dp)
                        .clickable {
                            imageUrl = album.originalTitle.artworkUrl; showCoverFullscreen = true
                        },
                    vm
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    for (item in layout.albumLayout.items) {
                        LayoutComponent(vm, album, item, albumIsCurrentlyPlaying)
                    }
                    if (renderTitleProgressBar)
                        LayoutComponent(vm, album, layout.titleProgress, albumIsCurrentlyPlaying)
                }
                TitleDropdownMenu(vm, album = album)
            }
            if (album.tracks.size > 1) {
                HorizontalDivider()
                Box {
                    TextButton(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), onClick = {
                            if (!previewMode) {
                                album.isAutomaticSelection = false
                                album.isExpanded = !album.isExpanded
                            }
                        }) {
                        if (album.isExpanded) {
                            Text(text = stringResource(R.string.button_collapse))
                        } else {
                            Text(text = stringResource(R.string.button_expand))
                        }
                    }
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        textAlign = TextAlign.Right,
                        style = MaterialTheme.typography.bodySmall,
                        text = stringResource(R.string.info_titles, album.tracks.size)
                    )
                }

                if (album.isExpanded) {
                    Column {
                        album.tracks.forEach { title ->
                            TitleEntry(
                                vm = vm,
                                playerViewModel = playerViewModel,
                                album = album,
                                title = title,
                                layout,
                                previewMode = previewMode
                            )
                        }
                    }
                }
            } else if (!albumRepresentsTitle) {
                TitleEntry(
                    vm = vm,
                    playerViewModel = playerViewModel,
                    album = album,
                    title = album.tracks[0],
                    layout,
                    previewMode = previewMode
                )
            }
        }
    }

}

@Composable
fun TitleEntry(
    vm: AppViewModel,
    playerViewModel: PlayerViewModel,
    album: Album,
    title: SelectableTitle,
    layout: Layout,
    previewMode: Boolean = false
) {
    var titleSelected = false
    if (previewMode) titleSelected = title.details.index == 0
    else if (playerViewModel.valid) titleSelected =
        title.details.index == playerViewModel.getIndex() && title.details.playlistId == playerViewModel.playlistId
    var modifier = Modifier.clickable {
        if (!previewMode) {
            if (vm.playlistEditMode || vm.addTitlesMode) title.toggleIsSelected(vm)
            else {
                vm.playlistsViewModel.filterValue.clear()
                vm.playlistsViewModel.showFilter = false
                if (titleSelected)
                    if (playerViewModel.playbackState == PlaybackState.PLAYING)
                        vm.playerAccess.pausePlayback()
                    else
                        vm.playerAccess.startPlayback()
                else {
                    vm.playerAccess.playTrack(title.details.playlistId, title.details.index)
                    vm.autoscroll = true
                }
            }
        }
    }
    if (titleSelected && highlightPlayingItem(layout.titleLayout)) modifier =
        modifier.background(MaterialTheme.colorScheme.primaryContainer)
    Column(
        modifier = modifier
    ) {
        HorizontalDivider()
        Row {
            if (vm.playlistEditMode || vm.addTitlesMode) Checkbox(
                modifier = Modifier.align(Alignment.CenterVertically),
                checked = title.isSelected,
                onCheckedChange = { title.setSelected(vm, it) })

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                for (item in layout.titleLayout.items) {
                    LayoutComponent(
                        playerViewModel,
                        album,
                        title.details,
                        layout.albumLayoutHasArtist,
                        item,
                        titleSelected
                    )
                }
            }
            TitleDropdownMenu(vm, title.details)
        }
    }
}

@Composable
fun Playlist(vm: AppViewModel, playlist: Playlist) {
    if (vm.playlistsViewModel.filterValue.isActive) playlist.applyFilter(vm)

    if (!vm.loadingList) {
        val currentAlbumIndex = vm.getCurrentAlbumIndex()
        val playlistState =
            rememberLazyListState(initialFirstVisibleItemIndex = if (!vm.loadingList && vm.autoscroll && currentAlbumIndex != -1) currentAlbumIndex else 0)

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = playlistInfoString(vm),
            style = MaterialTheme.typography.titleMedium,
            fontStyle = FontStyle.Normal
        )

        LazyColumn(state = playlistState) {
            if (vm.playlistsViewModel.filterValue.isActive) {
                println("FOOB filtered albums: ${playlist.filteredAlbums.size}")
                try {
                    items(playlist.filteredAlbums) { album ->
                        if (album.matches(vm.playlistsViewModel.filterValue)) AlbumCard(
                            vm,
                            vm.playerViewModel,
                            album,
                            layoutManager.getLayout()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (playlist.albums.isNotEmpty()) try {
                items(playlist.albums) { album ->
                    AlbumCard(vm, vm.playerViewModel, album, layoutManager.getLayout())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        LaunchedEffect(vm.autoScrollIndex, vm.enforceAutoscroll) {
            if (vm.autoscroll && vm.autoScrollIndex != -1) {
                if (vm.enforceAutoscroll) {
                    vm.enforceAutoscroll = false
                    playlistState.scrollToItem(if (vm.autoScrollIndex > 0) vm.autoScrollIndex - 1 else 1)
                }

                playlistState.scrollToItem(vm.autoScrollIndex)
            }
        }
        LaunchedEffect(vm.scrollToTop) {
            if (vm.scrollToTop) {
                vm.scrollToTop = false
                playlistState.scrollToItem(0)
            }
        }
        LaunchedEffect(vm.scrollToBottom) {
            if (vm.scrollToBottom) {
                vm.scrollToBottom = false
                if (playlist.albums.isNotEmpty())
                    if (vm.playlistsViewModel.filterValue.isActive)
                        playlistState.scrollToItem(playlist.filteredAlbums.size - 1)
                    else
                        playlistState.scrollToItem(playlist.albums.size - 1)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSwitcher(vm: AppViewModel, playlists: Playlists) {
    var expanded by remember {
        mutableStateOf(false)
    }
    var selectedIndex by remember {
        mutableIntStateOf(vm.playlistsViewModel.selectedPlaylistIndex)
    }
    val focusRequester = remember { FocusRequester() }
    ExposedDropdownMenuBox(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = {
            if (!vm.playlistEditMode && !vm.addTitlesMode) expanded = !expanded
        }) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused) // clear focus immediately
                        focusRequester.freeFocus()
                },
            label = { Text(text = stringResource(R.string.button_playlist_switcher)) },
            readOnly = true,
            enabled = !vm.playlistEditMode && !vm.addTitlesMode,
            value = vm.selectedPlaylistName,
            trailingIcon = {
                if (!vm.playlistEditMode && !vm.addTitlesMode) ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            onValueChange = {})
        DropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = expanded,
            onDismissRequest = { expanded = false }) {
            playlists.playlists.forEachIndexed { index, playlistEntity ->
                DropdownMenuItem(text = {
                    Text(text = playlistEntity.name)
                }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding, onClick = {
                    selectedIndex = index
                    expanded = false
                    vm.autoscroll = false
                    vm.playlistsViewModel.setSelectedPlaylist(playlists.playlists[selectedIndex].playlistId)
                }, trailingIcon = {
                    if (playlistEntity.isCurrent) {
                        Icon(
                            painter = painterResource(id = R.drawable.equalizer),
                            contentDescription = "Playing"
                        )
                    }
                })
            }
        }
    }

}

