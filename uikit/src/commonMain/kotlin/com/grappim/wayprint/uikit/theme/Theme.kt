package com.grappim.wayprint.uikit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

internal val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = GreenContainerLight,
    onPrimaryContainer = OnGreenLight,
    secondary = SlateGreen40,
    onSecondary = Color.White,
    secondaryContainer = SlateGreenContainerLight,
    onSecondaryContainer = OnSlateGreenLight,
    tertiary = Teal40,
    onTertiary = Color.White,
    tertiaryContainer = TealContainerLight,
    onTertiaryContainer = OnTealLight,
    error = Red40,
    onError = Color.White,
    errorContainer = RedContainerLight,
    onErrorContainer = OnRedLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

internal val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = OnGreenDark,
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = GreenContainerLight,
    secondary = SlateGreen80,
    onSecondary = OnSlateGreenDark,
    secondaryContainer = SlateGreenContainerDark,
    onSecondaryContainer = SlateGreenContainerLight,
    tertiary = Teal80,
    onTertiary = OnTealDark,
    tertiaryContainer = TealContainerDark,
    onTertiaryContainer = TealContainerLight,
    error = Red80,
    onError = OnRedDark,
    errorContainer = RedContainerDark,
    onErrorContainer = RedContainerLight,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

/**
 * The `Surface` is the theme's, not a caller's — it's the only thing that paints
 * `colorScheme.surface` and provides `LocalContentColor` on a screen with no `Scaffold`, which
 * today's placeholder screen is. Without it the visible background is the *window's* and
 * Compose's default black wins for any text that doesn't name a colour.
 */
@Composable
fun WayprintTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    ) {
        Surface(modifier = Modifier.fillMaxSize(), content = content)
    }
}
