package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.deckerth.thomas.foobarremotecontroller2.model.Album
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItem
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.Typography

@Composable
fun getTextStyle(typography: Typography): TextStyle {
    when (typography) {
        Typography.TITLE_LARGE -> return MaterialTheme.typography.titleLarge
        Typography.TITLE_MEDIUM -> return MaterialTheme.typography.titleMedium
        Typography.BODY_MEDIUM -> return MaterialTheme.typography.bodyMedium
        Typography.BODY_SMALL -> return MaterialTheme.typography.bodySmall
        else -> return MaterialTheme.typography.bodySmall
    }
}

@Composable
fun TextComponent(text: String, item: LayoutItem) {
    Text(
        modifier = Modifier
            .fillMaxWidth(),
        text = text,
        style = getTextStyle(item.style)
    )
}

@Composable
fun LayoutComponent(player: Player, layoutItem: LayoutItem) {
    when (layoutItem.item) {
        LayoutItems.ARTWORK -> ArtWork(player)
        LayoutItems.TITLE -> TextComponent(text = player.title, item = layoutItem)
        LayoutItems.ALBUM -> TextComponent(text = player.album, item = layoutItem)
        LayoutItems.ARTIST -> TextComponent(text = player.artist, item = layoutItem)
        LayoutItems.PROGRESS -> PlayerProgress(player)
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
        LayoutItems.TITLE -> TextComponent(text = title.title, item = layoutItem)
        LayoutItems.ARTIST -> TextComponent(text = title.artist, item = layoutItem)
        LayoutItems.SMART_ARTIST ->
            if (!checkArtist || !title.artist.equals(album.originalTitle.artist))
                TextComponent(text = title.artist, item = layoutItem)
        else -> Text("UNKNOWN ITEM")
    }
}

