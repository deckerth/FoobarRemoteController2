package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.deckerth.thomas.foobarremotecontroller2.model.Album
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ItemSize
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItem
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.TextAlignment
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

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
fun LayoutComponent(vm: AppViewModel, player: Player, layoutItem: LayoutItem) {
    when (layoutItem.item) {
        LayoutItems.LABEL -> TextComponent(text = player.label, item = layoutItem)
        LayoutItems.CATALOG -> TextComponent(text = player.catalog, item = layoutItem)
        LayoutItems.LABEL_CATALOG -> TextComponent(
            text = player.label + " " + player.catalog,
            item = layoutItem
        )

        LayoutItems.TITLE -> TextComponent(text = player.title, item = layoutItem)
        LayoutItems.ALBUM -> TextComponent(text = player.album, item = layoutItem)
        LayoutItems.ARTIST -> TextComponent(text = player.artist, item = layoutItem)
        LayoutItems.SAMPLE_RATE -> TextComponent(text = player.sampleRate+ " Hz", item = layoutItem)
        LayoutItems.PROGRESS -> PlayerProgress(vm, player)
        LayoutItems.COMPOSER ->
            if (player.composer != "" && player.composer != "?") {
                TextComponent(text = player.composer, item = layoutItem)
            }

        else -> Text("UNKNOWN ITEM")
    }
}

@Composable
fun LayoutComponent(album: Album, layoutItem: LayoutItem) {
    when (layoutItem.item) {
        LayoutItems.LABEL -> TextComponent(text = album.originalTitle.label, item = layoutItem)
        LayoutItems.CATALOG -> TextComponent(text = album.originalTitle.catalog, item = layoutItem)
        LayoutItems.LABEL_CATALOG -> TextComponent(
            text = album.originalTitle.label + " " + album.originalTitle.catalog,
            item = layoutItem
        )

        LayoutItems.ALBUM -> TextComponent(text = album.originalTitle.album, item = layoutItem)
        LayoutItems.ARTIST -> TextComponent(text = album.originalTitle.artist, item = layoutItem)
        LayoutItems.COMPOSER ->
            if (album.originalTitle.composer != "" && album.originalTitle.composer != "?") {
                TextComponent(text = album.originalTitle.composer, item = layoutItem)
            }

        else -> Text("UNKNOWN ITEM")
    }
}

@Composable
fun LayoutComponent(album: Album, title: ITitle, checkArtist: Boolean, layoutItem: LayoutItem) {
    when (layoutItem.item) {
        LayoutItems.LABEL -> TextComponent(text = title.label, item = layoutItem)
        LayoutItems.CATALOG -> TextComponent(text = title.catalog, item = layoutItem)
        LayoutItems.LABEL_CATALOG -> TextComponent(
            text = title.label + " " + title.catalog,
            item = layoutItem
        )

        LayoutItems.ALBUM -> TextComponent(text = title.album, item = layoutItem)
        LayoutItems.TITLE -> TextComponent(text = title.title, item = layoutItem)
        LayoutItems.ARTIST -> TextComponent(text = title.artist, item = layoutItem)
        LayoutItems.SMART_ARTIST ->
            if (!checkArtist || !title.artist.equals(album.originalTitle.artist))
                TextComponent(text = title.artist, item = layoutItem)

        LayoutItems.COMPOSER ->
            if (title.composer != "" && title.composer != "?") {
                TextComponent(text = title.composer, item = layoutItem)
            }

        else -> Text("UNKNOWN ITEM")
    }
}

