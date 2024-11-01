package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.freeFocus
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.model.Album
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists
import com.deckerth.thomas.foobarremotecontroller2.model.Title
import com.deckerth.thomas.foobarremotecontroller2.ui.components.LayoutComponent
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.getCurrentAlbumIndex
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.loadingList
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.player
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.displayedPlaylist
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.playlistState
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.autoScrollIndex
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.autoscroll
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.playlists
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.selectedPlaylist
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.selectedPlaylistIndex
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.setSelectedPlaylist
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.updatePlaylists

@Composable
fun PlaylistPage() {
    if (displayedPlaylist != null &&
        !displayedPlaylist!!.valid
    ) {
        displayedPlaylist!!.clear()
        displayedPlaylist = null
        updatePlaylists()  // delayed update to avoid crashed during layout update
    }
    Column() {
        PlaylistSwitcher(playlists = playlists)
        if (displayedPlaylist != null)
            Playlist(displayedPlaylist!!)
    }

    if (loadingList || displayedPlaylist == null)
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
        )
}

@Composable
fun AlbumCard(album: Album, layout: Layout?) {

    /*
    in automatic mode (initial state):
      automatically expand albums that are played, collapse them otherwise
    automatic mode is left when the user collapses a playing album, or expands a not playing album
    automatic mode is re-entered otherwise
    */

    if (player != null) {
        val currentlyPlaying =
            album.originalTitle.playlistId == player!!.playlistId && album.hasIndex(
                player!!.getIndex()
            )
        if (album.isAutomaticSelection)
            album.isSelected = currentlyPlaying
        else
            album.isAutomaticSelection = album.isSelected == currentlyPlaying
    }

    ElevatedCard(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column {
            Row {
                AsyncImage(
                    model = album.originalTitle.artworkUrl,
                    placeholder = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = stringResource(id = R.string.desc_album_picture),
                    modifier = Modifier
                        .size(80.dp)
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .height(80.dp)
                ) {
                    if (layout != null)
                        for (item in layout.albumLayout.items) {
                            LayoutComponent(album, item)
                        }
                }
            }
            if (album.titles.size > 1) {
                HorizontalDivider()
                Box {
                    TextButton(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        onClick = {
                            album.isAutomaticSelection = false
                            album.isSelected = !album.isSelected
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
                            TitleEntry(album = album, title = title)
                        }
                    }
                }
            } else {
                TitleEntry(album = album, title = album.titles[0])
            }
        }
    }

}

@Composable
fun AlbumCardOld(album: Album) {
    var isSelected by rememberSaveable {
        mutableStateOf(false)
    }
//    if (player != null){
//        isSelected = album.hasIndex(player!!.index)
//    }

    val surfaceColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
    )
    ElevatedCard(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
//            .clickable {
//                PlayerAccess
//                    .getInstance()
//                    .playTrack(album.playlistId, album.index)
//            }
    ) {
        Column {
            Row {
                AsyncImage(
                    model = album.originalTitle.artworkUrl,
                    placeholder = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = stringResource(id = R.string.desc_album_picture),
                    modifier = Modifier
                        .size(80.dp)
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .height(80.dp)
                ) {
                    Text(
                        text = album.originalTitle.album,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = album.originalTitle.artist,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (album.originalTitle.composer != "" && album.originalTitle.composer != "?" && album.originalTitle.composer != album.originalTitle.artist) {
                        Text(
                            text = album.originalTitle.composer,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }


            }
            if (album.titles.size > 1) {
                HorizontalDivider()
                Box {
                    TextButton(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        onClick = { isSelected = !isSelected }
                    ) {
                        if (isSelected) {
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

                if (isSelected) {
                    Column {
                        album.titles.forEach { title ->
                            TitleEntry(album = album, title = title)
                        }
                    }
                }
            } else {
                TitleEntry(album = album, title = album.titles[0])
            }

        }
    }

}

@Composable
fun TitleEntry(album: Album, title: ITitle) {
    var titleSelected = false
    if (player != null)
        titleSelected =
            title.index == player!!.getIndex() && title.playlistId == player!!.playlistId
    var modifier = Modifier
        .clickable {
            PlayerAccess.getInstance().playTrack(title.playlistId, title.index)
        }
    if (titleSelected)
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer)
    Column(
        modifier = modifier
    ) {
        HorizontalDivider()
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            val layout = layoutManager.getLayout()
            for (item in layout.titleLayout.items) {
                LayoutComponent(album, title, layout.albumLayoutHasArtist, item)
            }
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
            value = if (playlists.playlists.isEmpty()) "" else playlists.playlists[if (selectedIndex == -1) 0 else selectedIndex].name,
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