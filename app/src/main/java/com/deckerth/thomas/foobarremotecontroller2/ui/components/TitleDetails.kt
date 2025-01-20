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
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getLayoutItemsFor
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

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
fun TitleDetails(vm: AppViewModel, onDismiss: () -> Unit) {
    if (vm.player == null) return
    if (vm.player!!.playlistId.isEmpty() || vm.player!!.index.isEmpty()) return
    val playlist = vm.playlistsViewModel.getPlaylist(vm.player!!.playlistId)
    val title = playlist.getTitle(vm.player!!.index.toInt())
    if (title != null) {
        TitleDetails(title, onDismiss)
    }
}

@Composable
fun DisplayItemDetail(title: ITitle, item: LayoutItems) {

    val value = when (item) {
        LayoutItems.TITLE -> title.title
        LayoutItems.ALBUM -> title.album
        LayoutItems.ARTIST -> title.artist
        LayoutItems.COMPOSER -> title.composer
        LayoutItems.CATALOG -> title.catalog
        LayoutItems.LABEL -> title.label
        else -> ""
    }

    if (value.isEmpty()) return

    if (item == LayoutItems.ARTWORK || item == LayoutItems.PROGRESS || item == LayoutItems.LABEL_CATALOG) return
    Text(
        text = item.text,
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = value,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 20
    )
    Spacer(modifier = Modifier.height(8.dp))
}