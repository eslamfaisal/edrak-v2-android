package me.edrakai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = EdrakColors.Primary,
    onPrimary = EdrakColors.BackgroundDark,
    secondary = EdrakColors.PrimaryLight,
    onSecondary = EdrakColors.BackgroundDark,
    tertiary = EdrakColors.PrimaryDark,
    background = EdrakColors.BackgroundLight,
    surface = EdrakColors.Surface,
    surfaceVariant = EdrakColors.SurfaceVariant,
    onBackground = EdrakColors.OnSurface,
    onSurface = EdrakColors.OnSurface,
    onSurfaceVariant = EdrakColors.OnSurfaceVariant,
    error = EdrakColors.Error,
)

private val DarkColorScheme = darkColorScheme(
    primary = EdrakColors.Primary,
    onPrimary = EdrakColors.BackgroundDark,
    secondary = EdrakColors.PrimaryLight,
    onSecondary = EdrakColors.BackgroundDark,
    tertiary = EdrakColors.PrimaryDark,
    background = EdrakColors.BackgroundDark,
    surface = EdrakColors.CardDark,
    surfaceVariant = EdrakColors.SurfaceVariantDark,
    surfaceContainer = EdrakColors.NavDark,
    onBackground = EdrakColors.Slate100,
    onSurface = EdrakColors.Slate100,
    onSurfaceVariant = EdrakColors.Slate400,
    error = EdrakColors.Error,
)

@Composable
fun EdrakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EdrakTypography,
        content = content,
    )
}
