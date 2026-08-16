package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val KorvaDarkColorScheme = darkColorScheme(
    primary = KorvaCyan,
    onPrimary = EngineBackground,
    primaryContainer = EngineSurfaceVariant,
    onPrimaryContainer = KorvaCyan,
    secondary = KorvaBlue,
    onSecondary = EngineBackground,
    secondaryContainer = EngineCardBg,
    onSecondaryContainer = KorvaBlue,
    tertiary = KorvaAmber,
    onTertiary = EngineBackground,
    background = EngineBackground,
    onBackground = TextPrimary,
    surface = EngineSurface,
    onSurface = TextPrimary,
    surfaceVariant = EngineSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = EngineBorder,
    outlineVariant = EngineBorderGlow.copy(alpha = 0.3f),
    error = KorvaRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // We enforce Arabic RTL and dark engine theme
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = KorvaDarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}

