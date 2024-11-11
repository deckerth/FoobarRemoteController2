package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getLayoutItemsFor
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.getPlaylist

@Composable
fun TitleDetails(title: ITitle, onDismiss: () -> Unit) {
    val layout = layoutManager.getLayout()
    val playerItems = layout.playerLayout
    val allItems = getLayoutItemsFor(ViewsWithLayout.PLAYER)
    val unusedItems = allItems.filter { item -> !playerItems.items.any { it.item == item } }


    AlertDialog(
        onDismissRequest = { onDismiss() },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(R.string.button_close),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = { },
        title = {
            Text(
                text = mainActivity.baseContext.getString(R.string.button_details),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                for (item in playerItems.items)
                    DisplayItemDetail(title, item.item)
                for (item in unusedItems)
                    DisplayItemDetail(title, item)
            }
        },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

@Composable
fun TitleDetails(player: Player, onDismiss: () -> Unit) {
        if (player.playlistId.isEmpty() || player.index.isEmpty()) return
        val playlist = getPlaylist(player.playlistId)
        val title = playlist.getTitle(player.index.toInt())
        if (title != null) {
            TitleDetails(title, onDismiss)
        }
}

@Composable
fun DisplayItemDetail(title: ITitle, item: LayoutItems) {

    if (item == LayoutItems.ARTWORK || item == LayoutItems.PROGRESS) return
    Text(
        text = item.text,
        style = MaterialTheme.typography.titleMedium
    )
    when (item) {
        LayoutItems.TITLE -> Text(
            text = title.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 20
        )

        LayoutItems.ALBUM -> Text(
            text = title.album,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 20
        )

        LayoutItems.ARTIST -> Text(
            text = title.artist,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 20
        )

        LayoutItems.COMPOSER -> Text(
            text = title.composer,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2
        )

        LayoutItems.CATALOG -> Text(
            text = title.catalog,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2
        )

        LayoutItems.LABEL -> Text(
            text = title.label,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2
        )

        else -> {}
    }
    Spacer(modifier = Modifier.height(8.dp))
}