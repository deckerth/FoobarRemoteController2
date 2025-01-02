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

        val discNumber = try {
            player.discNumber.toInt()
        } catch (e: NumberFormatException) {
            -1
        }
        val track = try {
            player.track.toInt()
        } catch (e: NumberFormatException) {
            -1
        }
        if (track != -1) {
            val text = if (discNumber != -1)
                stringResource(
                    R.string.info_disc_track,
                    discNumber,
                    track
                )
            else
                stringResource(
                    R.string.info_track,
                    track
                )
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = text,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
