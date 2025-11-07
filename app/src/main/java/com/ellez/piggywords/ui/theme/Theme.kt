package com.ellez.piggywords.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TealBackground,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2F1), // Very light teal
    onPrimaryContainer = TealDark,
    secondary = PiggyPink,
    onSecondary = Color(0xFF5D3A3A), // Dark text on pink
    secondaryContainer = Color(0xFFFFE0E0), // Very light pink
    onSecondaryContainer = Color(0xFF8B4545),
    tertiary = WarmOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4CC), // Light orange
    onTertiaryContainer = Color(0xFF8B5A2B),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFC62828),
    background = Color(0xFFF8F9FA), // Soft off-white
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray300,
    outlineVariant = Gray200
)

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF003737),
    primaryContainer = TealDark,
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = PiggyPinkLight,
    onSecondary = Color(0xFF5D3A3A),
    secondaryContainer = Color(0xFF8B4545),
    onSecondaryContainer = Color(0xFFFFE0E0),
    tertiary = WarmOrange,
    onTertiary = Color(0xFF5D3A1A),
    tertiaryContainer = Color(0xFF8B5A2B),
    onTertiaryContainer = Color(0xFFFFE4CC),
    error = ErrorRed,
    onError = Color(0xFF5F2120),
    errorContainer = Color(0xFFC62828),
    onErrorContainer = Color(0xFFFFCDD2),
    background = DarkBackground,
    onBackground = Gray100,
    surface = DarkSurface,
    onSurface = Gray100,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray400,
    outline = Gray600,
    outlineVariant = Gray700
)

@Composable
fun PiggyWordsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use containerColor instead of deprecated statusBarColor
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}