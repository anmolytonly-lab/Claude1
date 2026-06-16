package com.kawach.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealVariant,
    error = SosRed,
    errorContainer = SosRedContainer,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = TealVariant,
    primaryContainer = TealPrimary,
    onPrimaryContainer = TealContainer,
    error = SosRedLight,
    errorContainer = SosRedDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark
)

@Composable
fun KawachTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KawachTypography,
        content = content
    )
}
