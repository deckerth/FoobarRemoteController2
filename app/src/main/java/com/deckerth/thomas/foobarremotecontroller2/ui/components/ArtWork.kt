package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.Player

@Composable
fun ArtWork(player: Player, previewMode: Boolean = false, modifier: Modifier = Modifier) {
    val index = player.getIndex()
    if (index >= 0) {
        if (previewMode) {
            Image(
                bitmap = ImageBitmap.imageResource(id = player.artworkUrl.toInt()),
                contentDescription = stringResource(R.string.desc_album_picture),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            )
        } else
            ImageWithLoadingPlaceholder(
                imageUrl = player.artworkUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            )
    } else {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize()
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