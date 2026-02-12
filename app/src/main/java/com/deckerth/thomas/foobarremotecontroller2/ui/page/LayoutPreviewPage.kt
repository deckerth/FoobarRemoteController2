package com.deckerth.thomas.foobarremotecontroller2.ui.page

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity
import com.deckerth.thomas.foobarremotecontroller2.model.Title
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Layouts
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaybackMode
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel

val previewPlayerClassic = PlayerViewModel().apply { update(
    "Decca",
    "421 670-2",
    "Puccini, Giacomo",
    "Tosca",
    "Act 1 - Scene 1 - \"Ah! Finalmente!\"",
    "Leontyne Price / Giuseppe di Stefano / Giuseppe Taddei / Carlo Cava / Fernando Corena / Piero de Palma / Leonardo Monreale / Alfredo Mariotti / Herbert Weiss / Wiener Staatsopernchor / Wiener Philharmoniker / Herbert von Karajan",
    "Price / Karajan",
    "44100",
    "Opera",
    "1",
    "1",
    "0:53",
    "p4",
    "1",
    "125.1700625",
    "53.58589853333333",
    R.drawable.cover_tosca.toString(),
    PlaybackState.PLAYING,
    PlaybackMode.DEFAULT,
    "",
    false) }

val previewPlayerPop = PlayerViewModel().apply { update(
    "Polydor",
    "517 007-2",
    "Björn Ulvaeus",
    "Gold - Greatest Hits",
    "Dancing Queen",
    "ABBA",
    "ABBA",
    "44100",
    "Pop",
    "1",
    "1",
    "0:50",
    "p4",
    "0",
    "232.2",
    "51.080651833333334",
    R.drawable.cover_abba.toString(),
    PlaybackState.PLAYING,
    PlaybackMode.DEFAULT,
    "",
    false) }

val previewPlayer = PlayerViewModel()

val previewPlaylist = Playlist(PlaylistEntity("p4", "Preview", true, 2))

private fun setupPreviewPlaylist() {
    previewPlaylist.clear()
    previewPlaylist.addTitle(
        Title(
            "p4",
            0,
            "Polydor",
            "517 007-2",
            "Björn Ulvaeus",
            "Gold - Greatest Hits",
            "Dancing Queen",
            "ABBA",
            "ABBA",
            "44100",
            "Pop",
            "1",
            "1",
            "0:50",
            "232.2",
            "51.080651833333334",
            R.drawable.cover_abba.toString(), ""
        )
    )
    previewPlaylist.addTitle(
        Title(
            "p4",
            1,
            "Decca",
            "421 670-2",
            "Puccini, Giacomo",
            "Tosca",
            "Act 1 - Scene 1 - \"Ah! Finalmente!\"",
            "\"Leontyne Price / Giuseppe di Stefano / Giuseppe Taddei / Carlo Cava / Fernando Corena / Piero de Palma / Leonardo Monreale / Alfredo Mariotti / Herbert Weiss / Wiener Staatsopernchor / Wiener Philharmoniker / Herbert von Karajan\"",
            "Price / Karajan",
            "44100",
            "Opera",
            "1",
            "1",
            "0:53",
            "125.1700625",
            "53.58589853333333",
            R.drawable.cover_tosca.toString(), ""
        )
    )
    previewPlaylist.albums[0].isExpanded = true
}

var currentPlayer by mutableStateOf(previewPlayerPop)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LayoutPreviewPage(appViewModel: AppViewModel) {
    var maxBoxHeight by remember { mutableStateOf(0.dp) }
    var maxBoxWidth by remember { mutableStateOf(0.dp) }

    if (appViewModel.selectedView == ViewsWithLayout.PLAYER) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            maxBoxHeight = maxHeight
            maxBoxWidth = maxWidth
            PlayerCard(
                appViewModel,
                currentPlayer,
                true,
                { currentPlayer = previewPlayerPop },
                { currentPlayer = previewPlayerClassic },
                boxSize = IntSize(maxBoxWidth.value.toInt(), maxBoxHeight.value.toInt())
            )
        }
    } else {
        setupPreviewPlaylist()
        PlaylistPreview(appViewModel, previewPlaylist)
    }
}

@Composable
fun PlaylistPreview(vm: AppViewModel, playlist: Playlist) {
    val playlistState =
        rememberLazyListState(initialFirstVisibleItemIndex = 0)

    LazyColumn(state = playlistState) {
        try {
            items(playlist.albums) { album ->
                AlbumCard(vm, currentPlayer, album, layoutManager.getLayout(Layouts.LAYOUT_CUSTOM), true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}