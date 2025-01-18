package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.model.Album
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists
import com.deckerth.thomas.foobarremotecontroller2.model.Title
import com.deckerth.thomas.foobarremotecontroller2.ui.components.ImageWithLoadingPlaceholder
import com.deckerth.thomas.foobarremotecontroller2.ui.components.LayoutComponent
import com.deckerth.thomas.foobarremotecontroller2.ui.components.TitleDetails
import com.deckerth.thomas.foobarremotecontroller2.ui.components.TitleSearchBarDialog
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.autoScrollIndex
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.autoscroll
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.displayedPlaylist
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.filterValue
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.getCurrentAlbumIndex
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.loadingList
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.player
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.playlistState
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.playlists
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.selectedPlaylistIndex
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.selectedPlaylistName
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.setSelectedPlaylist
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.showFilter
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.updatePlaylists

@Composable
fun PlaylistPage() {
    if (displayedPlaylist != null &&
        !displayedPlaylist!!.valid
    ) {
        displayedPlaylist!!.clear()
        displayedPlaylist = null
        updatePlaylists()  // delayed update to avoid crashes during layout update
    }
    if (displayedPlaylist != null)
        Column {
            PlaylistSwitcher(playlists = playlists)
            /*if (showFilter.value)
                TitleSearchBar(
                    onClosed = {
                        showFilter.value = false
                        filterValue.value = ""
                    },
                    onSearch = { searchString -> filterValue.value = searchString }
                )*/
            if (displayedPlaylist != null)
                Playlist(displayedPlaylist!!)
        }
    if (showFilter.value)
        TitleSearchBarDialog(
            onSearch = { searchString ->
                filterValue.value = searchString
                showFilter.value = false
            },
            onDismiss = {
                showFilter.value = false
                filterValue.value = ""
            }
        )
    if (loadingList || displayedPlaylist == null)
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
        )
}

@Composable
fun AlbumCard(album: Album, layout: Layout?, previewMode: Boolean = false) {

    /*
    in automatic mode (initial state):
      automatically expand albums that are played, collapse them otherwise
    automatic mode is left when the user collapses a playing album, or expands a not playing album
    automatic mode is re-entered otherwise
    */

    if (!previewMode && player != null) {
        val currentlyPlaying =
            album.originalTitle.playlistId == player!!.playlistId && album.hasIndex(
                player!!.getIndex()
            )
        if (album.isAutomaticSelection)
            album.isSelected = currentlyPlaying
        else
            album.isAutomaticSelection = album.isSelected == currentlyPlaying
    }

    // if the album contains a single title without name, the album itself represents the title, and can be selected
    val albumRepresentsTitle = album.titles.size == 1 && album.titles[0].title == ""
    var modifier: Modifier = Modifier
    if (albumRepresentsTitle) {
        var titleSelected = false
        if (player != null)
            titleSelected =
                album.titles[0].index == player!!.getIndex() && album.titles[0].playlistId == player!!.playlistId

        if (titleSelected)
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer)
    }

    if (album.titles.isNotEmpty() && !previewMode)
        modifier = modifier.clickable {
            filterValue.value = ""
            showFilter.value = false
            PlayerAccess.getInstance().playTrack(album.titles[0].playlistId, album.titles[0].index)
        }

    ElevatedCard(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(modifier = modifier) {
            Row {
                if (previewMode)
                    Image(
                        bitmap = ImageBitmap.imageResource(id = album.originalTitle.artworkUrl.toInt()),
                        contentDescription = stringResource(R.string.desc_album_picture),
                        modifier = Modifier
                            .size(80.dp)
                    )
                else
                    ImageWithLoadingPlaceholder(
                        imageUrl = album.originalTitle.artworkUrl,
                        modifier = Modifier
                            .size(80.dp)
                    )
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .height(80.dp)
                ) {
                    if (layout != null)
                        for (item in layout.albumLayout.items) {
                            LayoutComponent(album, item)
                        }
                }
                if (albumRepresentsTitle) {
                    var infoButtonClicked by remember { mutableStateOf(false) }
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = { infoButtonClicked = true }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.info_i),
                            contentDescription = stringResource(R.string.button_details),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (infoButtonClicked)
                        TitleDetails(
                            title = album.titles[0],
                            onDismiss = { infoButtonClicked = false },
                        )
                }
            }
            if (album.titles.size > 1) {
                HorizontalDivider()
                Box {
                    TextButton(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        onClick = {
                            if (!previewMode) {
                                album.isAutomaticSelection = false
                                album.isSelected = !album.isSelected
                            }
                        }
                    ) {
                        if (album.isSelected) {
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
                        text = stringResource(R.string.info_titles, album.titles.size)
                    )
                }

                if (album.isSelected) {
                    Column {
                        album.titles.forEach { title ->
                            if (title.matches(filterValue.value))
                                TitleEntry(album = album, title = title, previewMode = previewMode)
                        }
                    }
                }
            } else if (!albumRepresentsTitle) {
                TitleEntry(album = album, title = album.titles[0], previewMode = previewMode)
            }
        }
    }

}

@Composable
fun TitleEntry(album: Album, title: ITitle, previewMode: Boolean = false) {
    var titleSelected = false
    if (previewMode)
        titleSelected = title.index == 0
    else if (player != null)
        titleSelected =
            title.index == player!!.getIndex() && title.playlistId == player!!.playlistId
    var modifier = Modifier
        .clickable {
            if (!previewMode) {
                filterValue.value = ""
                showFilter.value = false
                PlayerAccess.getInstance().playTrack(title.playlistId, title.index)
            }
        }
    if (titleSelected)
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer)
    Column(
        modifier = modifier
    ) {
        var infoButtonClicked by remember { mutableStateOf(false) }
        HorizontalDivider()
        Box {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                val layout = layoutManager.getLayout()
                for (item in layout.titleLayout.items) {
                    LayoutComponent(album, title, layout.albumLayoutHasArtist, item)
                }
            }
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { infoButtonClicked = true }
            ) {
                Icon(
                    painter = painterResource(R.drawable.info_i),
                    contentDescription = stringResource(R.string.button_details),
                    modifier = Modifier.size(16.dp)
                )
            }
            if (infoButtonClicked)
                TitleDetails(
                    title = title,
                    onDismiss = { infoButtonClicked = false },
                )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlbumCardPreview() {
    val title = Title(
        "",
        -1,
        "",
        "",
        "Composer",
        "Ibrahim Ferrer (Buena Vista Social Club Presents)",
        "Mamí Me Gustó",
        "Ibrahim Ferrer",
        "",
        "",
        "",
        "",
        "",
        "",
    )
    val album = Album(title)
    album.addTitle(title)
    album.addTitle(title)
    album.addTitle(title)
    album.addTitle(title)
    album.addTitle(title)

    //val fields = LayoutDescription()
    //val albumItem = LayoutItem(LayoutItems.ALBUM, ItemSize.TITLE_MEDIUM)
    //val albumItem = LayoutItem() // <--- does not work for some reason
    //fields.items.add(LayoutItem(LayoutItems.ALBUM, ItemSize.TITLE_MEDIUM))
    //fields.items.add(LayoutItem(LayoutItems.ARTIST, ItemSize.BODY_SMALL))
    //fields.items.add(LayoutItem(LayoutItems.COMPOSER, ItemSize.BODY_SMALL))

    //val layout = Layout(playerLayout = fields, albumLayout = fields, titleLayout = fields)
    AlbumCard(album, null)
}

@Preview(showBackground = true)
@Composable
fun AlbumCardPreview2() {
    val title = Title(
        "",
        -1,
        "",
        "",
        "Composer",
        "Ibrahim Ferrer (Buena Vista Social Club Presents)",
        "",
        "Ibrahim Ferrer",
        "",
        "",
        "",
        "",
        "",
        "",
    )
    val album = Album(title)
    album.addTitle(title)

    //val fields = LayoutDescription()
    //val albumItem = LayoutItem(LayoutItems.ALBUM, ItemSize.TITLE_MEDIUM)
    //val albumItem = LayoutItem() // <--- does not work for some reason
    //fields.items.add(LayoutItem(LayoutItems.ALBUM, ItemSize.TITLE_MEDIUM))
    //fields.items.add(LayoutItem(LayoutItems.ARTIST, ItemSize.BODY_SMALL))
    //fields.items.add(LayoutItem(LayoutItems.COMPOSER, ItemSize.BODY_SMALL))

    //val layout = Layout(playerLayout = fields, albumLayout = fields, titleLayout = fields)
    AlbumCard(album, null)
}

@Composable
fun Playlist(playlist: Playlist) {
    val currentAlbumIndex = getCurrentAlbumIndex()
    playlistState =
        rememberLazyListState(initialFirstVisibleItemIndex = if (!loadingList && autoscroll && currentAlbumIndex != -1) currentAlbumIndex else 0)
    var animating by remember { mutableStateOf(false) }

    LazyColumn(state = playlistState) {
        if (playlist.albums.isNotEmpty()) {
            try {
                items(playlist.albums) { album ->
                    if (album.matches(filterValue.value))
                        AlbumCard(album, layoutManager.getLayout())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
//        if (!animating && !loadingList && playlistState.firstVisibleItemIndex != autoScrollIndex)
//            autoscroll = false
    LaunchedEffect(autoScrollIndex) {
        if (autoscroll) {
            animating = true
            playlistState.animateScrollToItem(autoScrollIndex)
            animating = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSwitcher(playlists: Playlists) {
    var expanded by remember {
        mutableStateOf(false)
    }
    var selectedIndex by remember {
        mutableIntStateOf(selectedPlaylistIndex)
    }
    val focusRequester = remember { FocusRequester() }
    ExposedDropdownMenuBox(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused) // clear focus immediately
                        focusRequester.freeFocus()
                },
            label = { Text(text = stringResource(R.string.button_playlist_switcher)) },
            readOnly = true,
            value = selectedPlaylistName,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            onValueChange = {
            }
        )
        DropdownMenu(
            modifier = Modifier
                .exposedDropdownSize(),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            playlists.playlists.forEachIndexed { index, playlistEntity ->
                DropdownMenuItem(
                    text = {
                        Text(text = playlistEntity.name)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    onClick = {
                        selectedIndex = index
                        expanded = false
                        autoscroll = false
                        setSelectedPlaylist(playlists.playlists[selectedIndex].playlistId)
                    },
                    trailingIcon = {
                        if (playlistEntity.isCurrent) {
                            Icon(
                                painter = painterResource(id = R.drawable.equalizer),
                                contentDescription = "Playing"
                            )
                        }
                    }
                )
            }
        }
    }

}

@Preview(
    showBackground = true
)
@Composable
fun PlaylistSwitcherPreview() {
//    var text by remember {
//        mutableStateOf("text")
//    }
//    OutlinedTextField(
//        value = text,
//        onValueChange = {s: String ->
//            text = s
//        },
//        label = {
//            Text(text = "Playlist")
//        }
//    )
    val playlists = Playlists()
    playlists.addPlaylistEntity(
        PlaylistEntity(
            "p1",
            "name1",
            false, 10
        )
    )
    playlists.addPlaylistEntity(
        PlaylistEntity(
            "p2",
            "name2",
            false, 10
        )
    )
    playlists.addPlaylistEntity(
        PlaylistEntity(
            "p3",
            "name3",
            true, 10
        )
    )
    playlists.addPlaylistEntity(
        PlaylistEntity(
            "p4",
            "name4",
            false, 10
        )
    )
    PlaylistSwitcher(playlists = playlists)
}


@Preview(
    showBackground = true,
)
@Composable
fun PlaylistPreview() {
    Foobar2000RemoteControllerTheme {
        val title = Title(
            "",
            0,
            "",
            "",
            "Composer",
            "Ibrahim Ferrer (Buena Vista Social Club Presents)",
            "Mamí Me Gustó",
            "Ibrahim Ferrer",
            "",
            "",
            "",
            "",
            "",
            "",
        )
        val playlist = Playlist(PlaylistEntity("p4", "main", true, 10))
        playlist.addTitle(title)
        playlist.addTitle(title)
        playlist.addTitle(title)
        playlist.addTitle(title)
        playlist.addTitle(title)
        playlist.addTitle(title)
        val title2 = Title(
            "",
            0,
            "",
            "",
            "Composer",
            "Ibrahim Ferrer (Buena Vista Social Club Presents)",
            "",
            "Ibrahim Ferrer",
            "",
            "",
            "",
            "",
            "",
            "",
        )
        playlist.addTitle(title2)
        playlist.addTitle(title2)
//        var titles = PlaylistAccess().getCurrentPlaylist();
//        if (titles == null) {
//            return@ComposePlaylistTheme
//        }
        Column {
            PlaylistSwitcherPreview()
            Playlist(playlist)
        }
    }
}