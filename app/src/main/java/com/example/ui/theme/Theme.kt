package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LuminaViolet,
    onPrimary = Color.White,
    primaryContainer = LuminaVioletContainer,
    onPrimaryContainer = Color(0xFFE9D8FD),
    secondary = LuminaCyan,
    onSecondary = Color.Black,
    secondaryContainer = LuminaCyanContainer,
    onSecondaryContainer = Color(0xFFC5F6FA),
    tertiary = LuminaEmerald,
    background = LuminaObsidian,
    onBackground = LuminaTextPrimary,
    surface = LuminaSurface,
    onSurface = LuminaTextPrimary,
    surfaceVariant = LuminaSurfaceElevated,
    onSurfaceVariant = LuminaTextSecondary,
    outline = LuminaSurfaceBorder,
    error = LuminaError
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF059669),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626)
)

@Composable
fun LuminaTheme(
    darkTheme: Boolean = true, // Default to Dark mode as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

