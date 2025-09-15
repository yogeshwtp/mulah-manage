package com.example.mulahmanage.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    background = DarkJungleGreen,
    surface = SlateGray,
    surfaceVariant = SlateGray,
    onPrimary = DarkJungleGreen,
    onBackground = LightGray,
    onSurface = LightGray,
    onSurfaceVariant = MediumGray,
    error = ErrorRed,
    onError = DarkJungleGreen
)

@Composable
fun MulahManageTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AppDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

