package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ItemSize
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItem

@Composable
fun ArtWork(player: Player, item: LayoutItem, previewMode: Boolean = false) {
    val index = player.getIndex()
    if (index >= 0) {
        val size = when (item.itemSize) {
            ItemSize.SMALL_COVER -> 120.dp
            ItemSize.MEDIUM_COVER -> 240.dp
            ItemSize.LARGE_COVER -> 320.dp
            else -> 240.dp
        }
        if (previewMode) {
            if (item.itemSize == ItemSize.MAX_COVER)
                Image(
                    bitmap = ImageBitmap.imageResource(id = player.artworkUrl.toInt()),
                    contentDescription = stringResource(R.string.desc_album_picture),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .wrapContentSize()
                )
            else
                Image(
                    bitmap = ImageBitmap.imageResource(id = player.artworkUrl.toInt()),
                    contentDescription = stringResource(R.string.desc_album_picture),
                    modifier = Modifier
                        .size(size)
                        .wrapContentSize()
                )
        } else
            if (item.itemSize == ItemSize.MAX_COVER)
                AsyncImage(
                    model = player.artworkUrl,
                    contentDescription = stringResource(R.string.desc_album_picture),
                    placeholder = painterResource(id = R.drawable.icon),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .wrapContentSize()
                )
            else
                AsyncImage(
                    model = player.artworkUrl,
                    contentDescription = stringResource(R.string.desc_album_picture),
                    placeholder = painterResource(id = R.drawable.icon),
                    modifier = Modifier
                        .size(size)
                        .wrapContentSize()
                )
    } else {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .size(
                    when (item.itemSize) {
                        ItemSize.SMALL_COVER -> 120.dp
                        ItemSize.MEDIUM_COVER -> 240.dp
                        ItemSize.LARGE_COVER -> 320.dp
                        else -> 240.dp
                    }
                )
                .clip(MaterialTheme.shapes.medium)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center),
                    text = stringResource(R.string.info_removed)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}