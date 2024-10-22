package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.Player

    @Composable
    fun PlayerProgress(player: Player) {
        Spacer(modifier = Modifier.height(8.dp))

        Column {
            Box {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = player.playbackTime,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = player.getNiceDuration(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .fillMaxWidth(),
                progress = { player.getPos() }
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(
                    R.string.info_disc_track,
                    player.discNumber,
                    player.track
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
