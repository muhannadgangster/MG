package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MgCyan,
    onPrimary = MgPitchBlack,
    primaryContainer = MgDarkSurfaceVariant,
    onPrimaryContainer = MgCyan,
    secondary = MgPurple,
    onSecondary = MgPitchBlack,
    secondaryContainer = MgDarkSurfaceVariant,
    onSecondaryContainer = MgPurple,
    tertiary = MgIndigo,
    onTertiary = MgPitchBlack,
    background = MgPitchBlack,
    onBackground = MgTextPrimary,
    surface = MgDarkBackground,
    onSurface = MgTextPrimary,
    surfaceVariant = MgDarkSurface,
    onSurfaceVariant = MgTextSecondary,
    outline = MgBorderDark,
    error = MgHeartRed
)

private val LightColorScheme = lightColorScheme(
    primary = MgCyan,
    onPrimary = MgPitchBlack,
    primaryContainer = MgLightSurfaceVariant,
    onPrimaryContainer = MgLightTextPrimary,
    secondary = MgPurple,
    onSecondary = MgPitchBlack,
    secondaryContainer = MgLightSurfaceVariant,
    onSecondaryContainer = MgPurple,
    tertiary = MgIndigo,
    onTertiary = MgPitchBlack,
    background = MgLightBackground,
    onBackground = MgLightTextPrimary,
    surface = MgLightSurface,
    onSurface = MgLightTextPrimary,
    surfaceVariant = MgLightSurfaceVariant,
    onSurfaceVariant = MgLightTextSecondary,
    outline = MgLightBorder,
    error = MgHeartRed
)

@Composable
fun MgTheme(
    darkTheme: Boolean = true, // Default to dark mode as requested for primary experience
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) MgPitchBlack.toArgb() else MgLightBackground.toArgb()
            window.navigationBarColor = if (darkTheme) MgPitchBlack.toArgb() else MgLightBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
