package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark-Mode First Color Scheme (Benchmark: Strava, Nike Run Club, Apple Fitness)
private val AthleticDarkColorScheme = darkColorScheme(
    primary = HyperLime,
    onPrimary = ObsidianNavy,
    primaryContainer = HyperLimeDark,
    onPrimaryContainer = PureWhite,

    secondary = StravaOrange,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF4D1A0A),
    onSecondaryContainer = Color(0xFFFFCCBC),

    tertiary = CyberCyan,
    onTertiary = ObsidianNavy,
    tertiaryContainer = Color(0xFF00363D),
    onTertiaryContainer = Color(0xFFE0F7FA),

    background = ObsidianNavy,
    onBackground = PureWhite,

    surface = GlassSurfaceDark,
    onSurface = PureWhite,
    surfaceVariant = GlassSurfaceElevated,
    onSurfaceVariant = TextSecondary,

    outline = DarkBorder,
    outlineVariant = Color(0xFF334155),

    error = ElectricCoral,
    onError = PureWhite
)

@Composable
fun BikeRouteTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AthleticDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
