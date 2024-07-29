package com.deckerth.thomas.foobarremotecontroller2

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.model.Player

@Composable
fun PlayingPage(){
    updatePlayer()
    if (player == null) {
        return
    }
    PlayerCard(player = player!!)
}

@Composable
fun PlayerCard(player: Player) {
    Column(
        modifier = Modifier
            .padding(40.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = player.artworkUrl,
            //placeholder = painterResource(R.drawable.album),
            contentDescription = "Album Picture",
            modifier = Modifier
                .aspectRatio(1f)
                .size(320.dp)
                .clip(MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            text = player.title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            text = player.album,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            text = player.artist,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            FilledTonalButton(
                onClick = { /*TODO*/ },
            ) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(
                onClick = { /*TODO*/ },
            ) {
                Text(text = "Play")
            }
            Spacer(modifier = Modifier.width(4.dp))
            FilledTonalButton(
                onClick = { /*TODO*/ },
            ) {
                Text(text = "Next")
            }
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
fun PlayerCardPreview(){
    PlayerCard(player = Player(
        "",
        "",
        "Album",
        "Title",
        "Artist",
        "0",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        PlaybackState.PAUSED
    ))
}