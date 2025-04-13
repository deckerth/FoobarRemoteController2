package com.deckerth.thomas.foobarremotecontroller2.ui.theme

import android.graphics.Bitmap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.deckerth.thomas.foobarremotecontroller2.getDynamicColorSchemeEnabledBlocking
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.viewModelInstance
import com.materialkolor.dynamicColorScheme
import com.materialkolor.ktx.animateColorScheme
import com.materialkolor.ktx.themeColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private var busy = false
var seedColor by mutableStateOf(Color.Black)

@Composable
fun Foobar2000RemoteControllerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = animateColorScheme(when (seedColor){
        Color.Black -> if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        else -> dynamicColorScheme(seedColor = seedColor,darkTheme,false)
    },tween(durationMillis = 3000))


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

fun calculateSeedColor(bitmap: Bitmap): Color {
    // convert Bitmap to ImageBitmap
    val imageBitmap = bitmap.asImageBitmap()
    val suitableColors = imageBitmap.themeColors(fallback = Color.Blue)
    return suitableColors.first()
}

fun updateColorScheme(
    bitmap: Bitmap
){
    if (!busy){
        CoroutineScope(Dispatchers.IO).launch {
            updateColorSchemeAsync(bitmap)
        }
    }
}

private suspend fun updateColorSchemeAsync(
    bitmap: Bitmap
){
    if (!getDynamicColorSchemeEnabledBlocking())
        return
    busy = true
    val result = calculateSeedColor(bitmap)
    if (!getDynamicColorSchemeEnabledBlocking())
        return
    seedColor = result
    busy = false
}