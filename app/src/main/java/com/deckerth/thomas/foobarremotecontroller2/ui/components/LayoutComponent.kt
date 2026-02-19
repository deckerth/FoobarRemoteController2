package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.model.Album
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ItemSize
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItem
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ProgressBarFormat
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.TextAlignment
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel

private fun intToDp(value: Int): Dp = value.dp

@Composable
fun getTextStyle(itemSize: ItemSize): TextStyle {
    return when (itemSize) {
        ItemSize.TITLE_LARGE -> MaterialTheme.typography.titleLarge
        ItemSize.TITLE_MEDIUM -> MaterialTheme.typography.titleMedium
        ItemSize.BODY_MEDIUM -> MaterialTheme.typography.bodyMedium
        ItemSize.BODY_SMALL -> MaterialTheme.typography.bodySmall
        else -> MaterialTheme.typography.bodySmall
    }
}

@Composable
fun TextComponent(text: String, item: LayoutItem) {
    if (text == "") return
    Text(
        modifier = Modifier
            .fillMaxWidth(),
        textAlign = when (item.alignment) {
            TextAlignment.LEFT -> TextAlign.Left; TextAlignment.RIGHT -> TextAlign.Right; TextAlignment.CENTER -> TextAlign.Center
        },
        text = text,
        style = getTextStyle(item.itemSize),
        fontStyle = if (item.italic) FontStyle.Italic else FontStyle.Normal,
        maxLines = item.maxLines
    )
}

@Composable
fun LayoutComponent(vm: AppViewModel, playerViewModel: PlayerViewModel, layoutItem: LayoutItem) {
    when (layoutItem.item) {
        LayoutItems.LABEL -> TextComponent(text = playerViewModel.label, item = layoutItem)
        LayoutItems.CATALOG -> TextComponent(text = playerViewModel.catalog, item = layoutItem)
        LayoutItems.LABEL_CATALOG -> TextComponent(
            text = playerViewModel.label + " " + playerViewModel.catalog,
            item = layoutItem
        )

        LayoutItems.TITLE -> TextComponent(text = playerViewModel.title, item = layoutItem)
        LayoutItems.ALBUM -> TextComponent(text = playerViewModel.album, item = layoutItem)
        LayoutItems.ALBUM_ARTIST -> TextComponent(text = playerViewModel.albumArtist, item = layoutItem)
        LayoutItems.ARTIST_TITLE -> TextComponent(text = playerViewModel.artist + " - " + playerViewModel.title, item = layoutItem)
        LayoutItems.ARTIST -> TextComponent(text = playerViewModel.artist, item = layoutItem)
        LayoutItems.SAMPLE_RATE -> TextComponent(text = playerViewModel.sampleRate+ " Hz", item = layoutItem)
        LayoutItems.GENRE -> TextComponent(text = playerViewModel.genre, item = layoutItem)
        LayoutItems.PROGRESS -> PlayerProgress(vm, playerViewModel)
        LayoutItems.COMPOSER ->
            if (playerViewModel.composer != "" && playerViewModel.composer != "?") {
                TextComponent(text = playerViewModel.composer, item = layoutItem)
            }

        else -> Text("UNKNOWN ITEM")
    }
}

@Composable
fun LayoutComponent(vm: AppViewModel, album: Album, layoutItem: LayoutItem, albumIsCurrentlyPlaying: Boolean) {
    when (layoutItem.item) {
        LayoutItems.LABEL -> TextComponent(text = album.originalTitle.label, item = layoutItem)
        LayoutItems.CATALOG -> TextComponent(text = album.originalTitle.catalog, item = layoutItem)
        LayoutItems.LABEL_CATALOG -> TextComponent(
            text = album.originalTitle.label + " " + album.originalTitle.catalog,
            item = layoutItem
        )

        LayoutItems.ALBUM -> TextComponent(text = album.originalTitle.album, item = layoutItem)
        LayoutItems.ARTIST -> TextComponent(text = album.originalTitle.artist, item = layoutItem)
        LayoutItems.ALBUM_ARTIST -> TextComponent(text = album.originalTitle.albumArtist, item = layoutItem)
        LayoutItems.ARTIST_TITLE -> TextComponent(text = album.originalTitle.artist + " - " + album.originalTitle.title, item = layoutItem)

        LayoutItems.COMPOSER ->
            if (album.originalTitle.composer != "" && album.originalTitle.composer != "?") {
                TextComponent(text = album.originalTitle.composer, item = layoutItem)
            }
        LayoutItems.PROGRESS -> if (albumIsCurrentlyPlaying) {
            val wavy = when (layoutItem.progressBarFormat) {
                ProgressBarFormat.WAVY_WHEN_PLAYING -> vm.playerViewModel.playbackState == PlaybackState.PLAYING
                ProgressBarFormat.UNDEFINED -> false
                ProgressBarFormat.WAVY -> true
                ProgressBarFormat.FLAT -> false
            }
            AlbumProgress(album, vm.playerViewModel, withTimingDetails = layoutItem.progressBarShowTimings, wavy, intToDp(layoutItem.waveSpeed))

        }
        else -> Text("UNKNOWN ITEM")
    }
}

@Composable
fun LayoutComponent(vm: PlayerViewModel, album: Album, title: ITitle, checkArtist: Boolean, layoutItem: LayoutItem, titleIsCurrentlyPlaying: Boolean) {
    when (layoutItem.item) {
        LayoutItems.LABEL -> TextComponent(text = title.label, item = layoutItem)
        LayoutItems.CATALOG -> TextComponent(text = title.catalog, item = layoutItem)
        LayoutItems.LABEL_CATALOG -> TextComponent(
            text = title.label + " " + title.catalog,
            item = layoutItem
        )

        LayoutItems.ALBUM -> TextComponent(text = title.album, item = layoutItem)
        LayoutItems.ALBUM_ARTIST -> TextComponent(text = title.albumArtist, item = layoutItem)
        LayoutItems.TITLE -> TextComponent(text = title.title, item = layoutItem)
        LayoutItems.ARTIST -> TextComponent(text = title.artist, item = layoutItem)
        LayoutItems.ARTIST_TITLE -> TextComponent(text = title.artist + " - " + title.title, item = layoutItem)
        LayoutItems.SMART_ARTIST ->
            if (!checkArtist || !title.artist.equals(album.originalTitle.artist))
                TextComponent(text = title.artist, item = layoutItem)

        LayoutItems.COMPOSER ->
            if (title.composer != "" && title.composer != "?") {
                TextComponent(text = title.composer, item = layoutItem)
            }
        LayoutItems.PROGRESS -> if (titleIsCurrentlyPlaying) {
            val wavy = when (layoutItem.progressBarFormat) {
                ProgressBarFormat.WAVY_WHEN_PLAYING -> vm.playbackState == PlaybackState.PLAYING
                ProgressBarFormat.UNDEFINED -> false
                ProgressBarFormat.WAVY -> true
                ProgressBarFormat.FLAT -> false
            }
            TitleProgress(
                playerViewModel = vm,
                withTimingDetails = layoutItem.progressBarShowTimings,
                wavy = wavy,
                waveSpeed = intToDp(layoutItem.waveSpeed)
            )
        }
        else -> Text("UNKNOWN ITEM")
    }
}

