package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.deckerth.thomas.foobarremotecontroller2.R

@Composable
fun ImageWithLoadingPlaceholder(imageUrl: String, modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }

    Box {
        AsyncImage(
            modifier = modifier,
            model = imageUrl,
            contentDescription = stringResource(R.string.desc_album_picture),
            //contentScale = ContentScale.Fit,
            onLoading = { isLoading = true },
            onSuccess = { isLoading = false },
            onError = { isLoading = false } // Handle error state as well
        )

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .shimmer() // Apply shimmer effect (see below)
            )
        }
    }
}

@Composable
fun Modifier.shimmer() = composed {
    var shimmerTranslate by remember { mutableFloatStateOf(0f) }

    val shimmerTransition = rememberInfiniteTransition(label = "shimmerTransition")
    shimmerTranslate = shimmerTransition.animateFloat(
        initialValue = -200f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmerTranslate"
    ).value

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f),
                MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f),
                MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f),
            ),
            start = Offset(shimmerTranslate, shimmerTranslate),
            end = Offset(shimmerTranslate + 200f, shimmerTranslate + 200f)
        )
    )
}