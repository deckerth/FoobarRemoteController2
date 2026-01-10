package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.model.Title
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getLayoutItemsFor
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
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
                text = mainActivity!!.baseContext.getString(R.string.button_details),
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
    if (!vm.playerViewModel.valid) return
    if (vm.playerViewModel.playlistId.isEmpty() || vm.playerViewModel.index.isEmpty()) return
    val playlist = vm.playlistsViewModel.getPlaylist(vm.playerViewModel.playlistId)
    val title = playlist.getTitle(vm.playerViewModel.index.toInt())
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
        LayoutItems.SAMPLE_RATE -> title.sampleRate + " Hz"
        LayoutItems.GENRE -> title.genre
        else -> ""
    }

    if (value.isEmpty()) return

    if (item == LayoutItems.ARTWORK || item == LayoutItems.PROGRESS || item == LayoutItems.LABEL_CATALOG) return
    Text(
        text = item.text,
        style = MaterialTheme.typography.titleMedium
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(1f),
            text = value,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 20
        )
        IconButton(onClick = {
            // Get the ClipboardManager from the context
            val clipboardManager = mainActivity?.getSystemService(android.content.ClipboardManager::class.java)
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

@Preview(
    showBackground = true,
)
@Composable
fun TitleDetailsPreview() {
    Foobar2000RemoteControllerTheme {
        val title = Title(
            "",
            0,
            "",
            "",
            "Composer",
            "Ibrahim Ferrer (Buena Vista Social Club Presents)",
            "",
            "Ibrahim Ferrer",
            "44100",
            "Classical",
            "",
            "",
            "",
            "",
            "",
            "", "")
        val item = LayoutItems.COMPOSER
        DisplayItemDetail(title, item)
    }
}