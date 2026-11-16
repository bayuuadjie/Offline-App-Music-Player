package com.example.offlineplayer.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Lime = Color(0xFFC8FF3E)
val Violet = Color(0xFF8B5CF6)
val Pink = Color(0xFFFF4FA3)
val DarkBg = Color(0xFF0E0B16)
val DarkSurface = Color(0xFF1B1626)
val DarkSurfaceHigh = Color(0xFF2A2240)
val TextDim = Color(0xFFE2DCF2)

val AppGradient: Brush = Brush.linearGradient(listOf(Violet, Pink))

private val AppColors = darkColorScheme(
    primary = Lime,
    onPrimary = Color(0xFF11140A),
    secondary = Violet,
    tertiary = Pink,
    background = DarkBg,
    onBackground = Color.White,
    surface = DarkBg,
    onSurface = Color.White,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = TextDim,
    secondaryContainer = DarkSurfaceHigh,
    onSecondaryContainer = Color.White,
    surfaceContainerHigh = DarkSurfaceHigh,
    outline = Color(0xFF4A4262)
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(
        fontWeight = FontWeight.ExtraBold, fontSize = 30.sp,
        lineHeight = 36.sp, letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
        lineHeight = 18.sp, letterSpacing = 0.8.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun OfflinePlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
