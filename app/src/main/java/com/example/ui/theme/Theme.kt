package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF8FF2FF),
    secondary = NeonEmerald,
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF005234),
    onSecondaryContainer = Color(0xFF86F8BF),
    tertiary = NeonOrange,
    onTertiary = Color(0xFF492900),
    tertiaryContainer = Color(0xFF693C00),
    onTertiaryContainer = Color(0xFFFFDCC1),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = NeonRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007C91),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F3FF),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = Color(0xFF087F5B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA7F3D0),
    onSecondaryContainer = Color(0xFF002114),
    tertiary = Color(0xFF9A4D00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBBE),
    onTertiaryContainer = Color(0xFF321200),
    background = Color.White,
    onBackground = Color(0xFF111827),
    surface = Color(0xFFF5F7FA),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color.White,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    error = Color(0xFFB91C1C),
    onError = Color.White
)

@Composable
fun JumpVpnTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
