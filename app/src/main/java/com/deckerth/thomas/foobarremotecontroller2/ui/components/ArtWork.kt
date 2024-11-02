package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ItemSize
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItem

@Composable
fun ArtWork(player: Player, item: LayoutItem) {
    val index = player.getIndex()
    if (index >= 0) {
        AsyncImage(
            model = player.artworkUrl,
            //placeholder = painterResource(R.drawable.album),
            contentDescription = stringResource(R.string.desc_album_picture),
            placeholder = painterResource(id = R.drawable.icon),
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
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ){
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