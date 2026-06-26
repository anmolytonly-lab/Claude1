package com.shingar.salon.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = White,
    primaryContainer = PinkLight,
    onPrimaryContainer = PinkDark,
    secondary = DarkBlue,
    onSecondary = White,
    secondaryContainer = DarkBlueLight,
    onSecondaryContainer = White,
    tertiary = SoftPurple,
    onTertiary = White,
    tertiaryContainer = SoftPurpleLight,
    onTertiaryContainer = DarkBlue,
    background = ScreenBackground,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = LightGray,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = White,
    outline = MediumGray
)

@Composable
fun ShingSalonTheme(content: @Composable () -> Unit) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBlue.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
