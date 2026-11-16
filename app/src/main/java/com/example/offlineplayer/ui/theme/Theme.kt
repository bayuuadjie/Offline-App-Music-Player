package com.example.offlineplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SpotGreen = Color(0xFF1DB954)
val DarkBg = Color(0xFF121212)
val DarkSurface = Color(0xFF282828)

private val AppColors = darkColorScheme(
    primary = SpotGreen,
    onPrimary = Color.Black,
    background = DarkBg,
    onBackground = Color.White,
    surface = DarkBg,
    onSurface = Color.White,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = Color(0xFFB3B3B3),
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color.White
)

@Composable
fun OfflinePlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, content = content)
}
