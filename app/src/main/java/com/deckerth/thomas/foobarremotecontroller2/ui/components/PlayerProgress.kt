package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProgress(vm: AppViewModel, playerViewModel: PlayerViewModel) {
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

    if (!isDragging) sliderValue = playerViewModel.getPos()

    Spacer(modifier = Modifier.height(8.dp))

    Column {
        Box {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = playerViewModel.playbackTime,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth(),
                text = playerViewModel.getNiceDuration(),
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Slider(
            value = sliderValue,
            onValueChange = { newPosition -> sliderValue = newPosition },
            onValueChangeFinished = { playerViewModel.setPos(vm, sliderValue) },
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth(),
            track = { sliderState ->
                var fraction = 0f
                try {
                    // Calculate fraction of the slider that is active
                    fraction =
                        (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                } catch (e: ArithmeticException) {
                }
                if (fraction.isNaN())
                    fraction = 0f

                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .align(Alignment.CenterStart)
                            .height(6.dp)
                            .padding(end = 6.dp)
                            .background(SliderDefaults.colors().activeTrackColor, CircleShape)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth(1f - fraction)
                            .align(Alignment.CenterEnd)
                            .height(1.dp)
                            .padding(start = 6.dp)
                            .background(SliderDefaults.colors().inactiveTrackColor, CircleShape)
                    )
                }
            },
            thumb = {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(16.dp)
                        .background(SliderDefaults.colors().thumbColor)
                )
            }
        )
        Spacer(modifier = Modifier.height(2.dp))

        val discNumber = try {
            playerViewModel.discNumber.toInt()
        } catch (e: NumberFormatException) {
            -1
        }
        val track = try {
            playerViewModel.track.toInt()
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
