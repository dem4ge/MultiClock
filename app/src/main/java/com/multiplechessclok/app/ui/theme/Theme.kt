package com.multiplechessclok.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Background = Color(0xFF121416)
private val Surface = Color(0xFF1B1E22)
private val OnSurface = Color(0xFFE7E2DA)
private val OnSurfaceVariantDark = Color(0xFFBEB8AE)
private val SurfaceVariantDark = Color(0xFF25282D)
private val OutlineDark = Color(0xFF6B6560)

private val LightBackground = Color(0xFFF2EDE4)
private val LightSurface = Color(0xFFFAF6EE)
private val OnSurfaceLight = Color(0xFF1E1C18)
private val OnSurfaceVariantLight = Color(0xFF5A554A)
private val SurfaceVariantLight = Color(0xFFE8E2D8)
private val OutlineLight = Color(0xFF7A7468)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFB5A994),
    onPrimary = Color(0xFF1F1B16),
    secondary = Color(0xFF8F8A7E),
    onSecondary = Color(0xFF1A1A16),
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF4A4338),
    onPrimary = Color(0xFFF4EFE6),
    secondary = Color(0xFF5C574C),
    onSecondary = Color(0xFFF4EFE6),
    background = LightBackground,
    onBackground = OnSurfaceLight,
    surface = LightSurface,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

@Composable
fun ChessClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}
