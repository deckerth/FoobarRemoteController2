package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.Player

@Composable
fun ArtWork(player: Player) {
    AsyncImage(
        model = player.artworkUrl,
        //placeholder = painterResource(R.drawable.album),
        contentDescription = stringResource(R.string.desc_album_picture),
        placeholder = painterResource(id = R.drawable.icon),
        modifier = Modifier
            .aspectRatio(1f)
            .size(320.dp)
            .clip(MaterialTheme.shapes.medium)
    )
    Spacer(modifier = Modifier.height(16.dp))
}