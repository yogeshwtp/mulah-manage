package com.example.mulahmanage.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// This creates the specific color scheme based on your mockup's dark theme
private val AppDarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    background = DarkBackground,
    surface = DarkBackground,       // Main background for components like Scaffold
    surfaceVariant = SurfaceDark,   // Color for Cards, Chips, TextFields
    onPrimary = DarkBackground,     // Color of text on top of the primary green color
    onBackground = TextPrimaryDark, // Color of text on top of the main background
    onSurface = TextPrimaryDark,    // Color of text on top of surfaces
    onSurfaceVariant = TextPrimaryDark, // Color of text on top of cards/chips
    error = RedError,
    onError = TextPrimaryDark,
    secondary = SurfaceDark,        // Using surface color for secondary elements
    onSecondary = TextPrimaryDark
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
            // Set the phone's status bar to match the app's background color
            window.statusBarColor = colorScheme.background.toArgb()
            // Ensure status bar icons (time, battery) are light-colored
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // This applies the custom color scheme to your entire app
    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}

