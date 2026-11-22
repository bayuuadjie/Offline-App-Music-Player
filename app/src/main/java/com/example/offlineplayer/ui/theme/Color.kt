package com.example.offlineplayer.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// TEMA LIQUID GLASS: PUTIH, HITAM, HIJAU
// ==========================================

// Hitam & Dark Obsidian (Background & Glass Depth)
val PureBlack = Color(0xFF000000)
val DarkBg = Color(0xFF080B09)
val DarkBgElevated = Color(0xFF0F1511)
val DarkSurface = Color(0x33121E15)
val DarkSurfaceHigh = Color(0x591B2B20)

// Hijau Liquid Glass (Neon Emerald & Mint)
val LiquidGreen = Color(0xFF1ED760)       // Spotify / iOS vibrant green
val LiquidGreenLight = Color(0xFF38EF7D)  // Bright mint highlight
val LiquidGreenDark = Color(0xFF10873E)   // Deep emerald
val LiquidGreenGlow = Color(0x401ED760)   // 25% green glow
val LiquidGreenSubtle = Color(0x1F1ED760) // 12% translucent green

// Putih & Translucent Glass Frosted
val PureWhite = Color(0xFFFFFFFF)
val TextWhite = Color(0xFFFFFFFF)
val TextDim = Color(0xB8FFFFFF)           // 72% white
val TextMuted = Color(0x70FFFFFF)         // 44% white

val GlassWhiteSurface = Color(0x1FFFFFFF) // 12% translucent glass
val GlassWhiteElevated = Color(0x2EFFFFFF)
val GlassWhiteHighlight = Color(0x47FFFFFF)
val GlassBorderSubtle = Color(0x29FFFFFF) // 16% white border

// Gradients
val AppGradient: Brush = Brush.linearGradient(
    listOf(LiquidGreenLight, LiquidGreen, LiquidGreenDark)
)

val DarkScreenGradient: Brush = Brush.verticalGradient(
    listOf(
        Color(0xFF0D1E14),
        Color(0xFF09120D),
        Color(0xFF060907)
    )
)

val GlassBorderGradient: Brush = Brush.linearGradient(
    listOf(
        Color.White.copy(alpha = 0.35f),
        Color(0xFF1ED760).copy(alpha = 0.25f),
        Color.White.copy(alpha = 0.08f)
    )
)

val GlassActiveBorderGradient: Brush = Brush.linearGradient(
    listOf(
        Color(0xFF1ED760),
        Color.White.copy(alpha = 0.55f),
        Color(0xFF1ED760).copy(alpha = 0.8f)
    )
)
