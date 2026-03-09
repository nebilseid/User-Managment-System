package com.sliide.usermanagement.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SliideRed,
    onPrimary = SliideWhite,
    primaryContainer = SliideGray400,
    onPrimaryContainer = SliideWhite,
    secondary = SliideGray150,
    onSecondary = SliideBlack,
    secondaryContainer = SliideGray400,
    onSecondaryContainer = SliideGray50,
    background = SliideBlack,
    onBackground = SliideWhite,
    surface = SliideGray500,
    surfaceVariant = SliideGray400,
    onSurface = SliideWhite,
    onSurfaceVariant = SliideGray150,
    error = ErrorRed,
    onError = SliideBlack,
    errorContainer = SliideGray400,
    onErrorContainer = ErrorRed,
    inverseSurface = SliideGray400,
    inverseOnSurface = SliideWhite
)

private val LightColorScheme = lightColorScheme(
    primary = SliideRed,
    onPrimary = SliideWhite,
    primaryContainer = SliideGray100,
    onPrimaryContainer = SliideBlack,
    secondary = SliideGray300,
    onSecondary = SliideWhite,
    secondaryContainer = SliideGray100,
    onSecondaryContainer = SliideGray500,
    background = SliideGray50,
    onBackground = SliideBlack,
    surface = SliideWhite,
    surfaceVariant = SliideGray100,
    onSurface = SliideBlack,
    onSurfaceVariant = SliideGray300,
    error = SliideRed,
    onError = SliideWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun SliideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = rememberSliideTypography()
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
