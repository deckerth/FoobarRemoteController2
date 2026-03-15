package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.StandardLayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getLayoutItemsFor
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

@Composable
fun TitleDetails(vm: AppViewModel, title: ITitle, onDismiss: () -> Unit) {
    val layout = layoutManager.getLayout()
    val playerItems = layout.playerLayout.getLayoutItems(vm)
    val availableItemsForPlayer = getLayoutItemsFor(vm, ViewsWithLayout.PLAYER)
    val itemsNotDisplayedOnPlayer = availableItemsForPlayer.filter { item -> !playerItems.any { it.item == item.item } }

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
                text = mainActivity!!.baseContext.getString(R.string.button_details),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                for (item in playerItems)
                    DisplayItemDetail(vm, title, item)
                for (item in itemsNotDisplayedOnPlayer)
                    DisplayItemDetail(vm, title, item)
            }
        },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

@Composable
fun TitleDetails(vm: AppViewModel, onDismiss: () -> Unit) {
    if (!vm.playerViewModel.valid) return
    if (vm.playerViewModel.playlistId.isEmpty() || vm.playerViewModel.index.isEmpty()) return
    val playlist = vm.playlistsViewModel.getPlaylist(vm.playerViewModel.playlistId)
    val title = playlist.getTitle(vm.playerViewModel.index.toInt())
    if (title != null) {
        TitleDetails(vm, title, onDismiss)
    }
}

@Composable
fun DisplayItemDetail(vm: AppViewModel, title: ITitle, item: LayoutItems) {

    val value = when (item.item) {
        StandardLayoutItems.TITLE -> title.title
        StandardLayoutItems.ALBUM -> title.album
        StandardLayoutItems.ARTIST -> title.artist
        StandardLayoutItems.ALBUM_ARTIST -> title.albumArtist
        StandardLayoutItems.COMPOSER -> title.composer
        StandardLayoutItems.CATALOG -> title.catalog
        StandardLayoutItems.LABEL -> title.label
        StandardLayoutItems.SAMPLE_RATE -> title.sampleRate + " Hz"
        StandardLayoutItems.GENRE -> title.genre
        StandardLayoutItems.CUSTOM_FIELD -> title.customFields.getContent(item.customFieldName)
        else -> ""
    }

    if (value.isEmpty()) return

    if (item.item == StandardLayoutItems.ARTWORK || item.item == StandardLayoutItems.PROGRESS || item.item == StandardLayoutItems.LABEL_CATALOG) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(
                text = item.text,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 20
            )
        }
        IconButton(onClick = {
            // Get the ClipboardManager from the context
            val clipboardManager =
                mainActivity?.getSystemService(android.content.ClipboardManager::class.java)
            // Create a ClipData object
            val clip = android.content.ClipData.newPlainText(item.text, value)
            // Set the data to the clipboard
            clipboardManager?.setPrimaryClip(clip)
        }) {
            Icon(
                painter = painterResource(R.drawable.content_copy),
                contentDescription = stringResource(R.string.button_content_copy),
                modifier = Modifier.size(16.dp)
            )
        }
    }


}
