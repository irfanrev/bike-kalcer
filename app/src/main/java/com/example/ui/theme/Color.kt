package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==========================================
// GEN Z ATHLETIC PALETTE (DARK-MODE FIRST)
// ==========================================

// Primary High-Energy Accents
val HyperLime = Color(0xFFD4FF00)         // Cyber Lime / Cyber Yellow
val HyperLimeDark = Color(0xFFA6CC00)     // Pressed/Darker Lime
val StravaOrange = Color(0xFFFF5722)      // Electric Athletic Orange
val StravaOrangeLight = Color(0xFFFF8A65)
val CyberCyan = Color(0xFF00F2FE)         // Electric Neon Cyan
val CyberCyanDark = Color(0xFF00B4D8)
val ElectricCoral = Color(0xFFFF3366)     // High-energy finish/alert coral
val PureWhite = Color(0xFFFFFFFF)

// Deep Obsidian & Midnight Backgrounds
val ObsidianNavy = Color(0xFF0B0F17)      // Deepest background
val DarkCanvas = Color(0xFF0D121D)        // Secondary background
val GlassSurfaceDark = Color(0xFF131A29)  // Card / Sheet surface
val GlassSurfaceElevated = Color(0xFF1B2438) // Floating elements
val DarkBorder = Color(0xFF263248)        // Subtle structural border

// Typography Muted Grays
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF94A3B8)     // Sleek slate 400
val TextMuted = Color(0xFF64748B)         // Subtle slate 500

// Glassmorphism Tint Colors
val GlassFrostWhite = Color(0x14FFFFFF)   // 8% white for frosted glass
val GlassFrostWhiteMedium = Color(0x22FFFFFF) // 13% white
val GlassBorderSubtle = Color(0x2EFFFFFF)  // 18% white for glass highlight borders

// ==========================================
// REUSABLE GLASSMORPHIC MODIFIER HELPER
// ==========================================
fun Modifier.glassmorphic(
    cornerRadius: Dp = 28.dp,
    backgroundColor: Color = Color(0xCC131A29), // Frosted obsidian
    borderColor: Color = GlassBorderSubtle,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 10.dp
): Modifier = composed {
    this
        .shadow(elevation, RoundedCornerShape(cornerRadius), ambientColor = Color.Black, spotColor = Color.Black)
        .clip(RoundedCornerShape(cornerRadius))
        .background(backgroundColor)
        .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
}
