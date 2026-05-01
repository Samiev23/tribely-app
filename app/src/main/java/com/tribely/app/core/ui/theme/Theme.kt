package com.tribely.app.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TribelyDarkColorScheme = darkColorScheme(
    primary = TribelyAccent,
    onPrimary = Color.Black,
    secondary = TribelyPink,
    onSecondary = Color.Black,
    tertiary = TribelyCyan,
    background = TribelyBgBlack,
    onBackground = TribelyTextPrimary,
    surface = TribelyBgDark,
    onSurface = TribelyTextPrimary,
    surfaceVariant = TribelyBgGrey,
    error = Color(0xFFFF4444)
)

@Composable
fun TribelyTheme(content: @Composable () -> Unit) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = TribelyDarkColorScheme,
        typography = TribelyTypography,
        content = content
    )
}
