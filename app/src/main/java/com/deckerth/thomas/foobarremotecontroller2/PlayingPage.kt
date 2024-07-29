package com.deckerth.thomas.foobarremotecontroller2

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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.model.Player

@Composable
fun PlayingPage() {
    if (player == null) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth()
        )
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            text = player.title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .fillMaxWidth(),
            progress = { player.getPos() }
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row {
            IconButton(
                onClick = {
                    PlayerAccess.getInstance().previousTrack()
                },
                modifier = Modifier
                    .size(60.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_previous),
                    contentDescription = "Skip Previous",
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(40.dp))
            FilledIconButton(
                onClick = {
                    if (player.playbackState == PlaybackState.PLAYING)
                        PlayerAccess.getInstance().pausePlayback()
                    else
                        PlayerAccess.getInstance().startPlayback()
                },
                modifier = Modifier
                    .size(60.dp)
            ) {
                if (player.playbackState == PlaybackState.PAUSED) {
                    Icon(
                        painter = painterResource(R.drawable.play),
                        contentDescription = "Play",
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.pause),
                        contentDescription = "Pause",
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxSize()
                    )
                }

            }
            Spacer(modifier = Modifier.width(40.dp))
            IconButton(
                onClick = {
                    PlayerAccess.getInstance().nextTrack()
                },
                modifier = Modifier
                    .size(60.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_next),
                    contentDescription = "Skip Next",
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxSize()
                )
            }
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
fun PlayerCardPreview() {
    PlayerCard(
        player = Player(
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
            "1",
            "0.5",
            "",
            PlaybackState.PLAYING
        )
    )
}