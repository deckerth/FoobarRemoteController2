package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

@Composable
fun PlayerProgress(vm: AppViewModel, player: Player) {
    val interactionSource = remember { MutableInteractionSource() }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isDragging = true
                is PressInteraction.Release -> isDragging = false
                is PressInteraction.Cancel -> isDragging = false
                is FocusInteraction.Focus -> isDragging = true
                is FocusInteraction.Unfocus -> isDragging = false
                is DragInteraction.Start -> isDragging = true
                is DragInteraction.Stop -> isDragging = false
                is DragInteraction.Cancel -> isDragging = false
            }
        }
    }

    if (!isDragging) sliderValue = player.getPos()

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
        Slider(
            value = sliderValue,
            onValueChange = { newPosition -> sliderValue = newPosition },
            onValueChangeFinished = { player.setPos(vm, sliderValue) },
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
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
