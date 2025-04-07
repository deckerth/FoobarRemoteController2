package com.deckerth.thomas.foobarremotecontroller2.ui.theme

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.materialkolor.dynamicColorScheme
import com.materialkolor.ktx.animateColorScheme
import com.materialkolor.ktx.themeColors
import com.materialkolor.palettes.CorePalette
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

var seedColor by mutableStateOf(Color(0xFFEE0000))

@Composable
fun Foobar2000RemoteControllerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = animateColorScheme(dynamicColorScheme(seedColor = seedColor,darkTheme,false),tween(durationMillis = 3000))
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

fun calculateSeedColor(drawable: Drawable): Color {
    val suitableColors = drawable.toBitmap().asImageBitmap().themeColors(fallback = Color.Blue)
    return suitableColors.first()
}

fun updateColorScheme(
    drawable: Drawable
){
    // Update in Background
    CoroutineScope(Dispatchers.IO).launch {
        seedColor = calculateSeedColor(drawable)
    }
}