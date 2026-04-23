package com.cowork.app_client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Light color scheme ────────────────────────────────────────────────────────
private val CoworkLightColorScheme = lightColorScheme(
    primary              = CoworkColors.Red500,
    onPrimary            = Color.White,
    primaryContainer     = CoworkColors.Red50,
    onPrimaryContainer   = CoworkColors.Red900,

    secondary            = CoworkColors.Red700,
    onSecondary          = Color.White,
    secondaryContainer   = CoworkColors.Red100,
    onSecondaryContainer = CoworkColors.Red900,

    tertiary             = CoworkColors.Blue500,
    onTertiary           = Color.White,
    tertiaryContainer    = CoworkColors.Blue50,
    onTertiaryContainer  = CoworkColors.Blue900,

    background           = CoworkColors.Neutral50,
    onBackground         = Color(0xFF060607),

    surface              = Color.White,
    onSurface            = Color(0xFF060607),
    surfaceVariant       = CoworkColors.Neutral100,
    onSurfaceVariant     = CoworkColors.Neutral500,
    surfaceContainer     = CoworkColors.Neutral50,
    surfaceContainerLow  = Color.White,
    surfaceContainerHigh = CoworkColors.Neutral100,

    outline              = CoworkColors.Neutral200,
    outlineVariant       = CoworkColors.Neutral100,

    error                = CoworkColors.Red700,
    onError              = Color.White,
    errorContainer       = CoworkColors.Red50,
    onErrorContainer     = CoworkColors.Red900,

    inverseSurface       = CoworkColors.Neutral800,
    inverseOnSurface     = CoworkColors.Neutral100,
    inversePrimary       = CoworkColors.Red300,
)

// ── Dark color scheme (Discord-style neutral surfaces) ────────────────────────
private val CoworkDarkColorScheme = darkColorScheme(
    primary              = CoworkColors.Red400,
    onPrimary            = Color.White,
    primaryContainer     = CoworkColors.Red900,
    onPrimaryContainer   = CoworkColors.Red100,

    secondary            = CoworkColors.Red300,
    onSecondary          = CoworkColors.Red900,
    secondaryContainer   = CoworkColors.Red800,
    onSecondaryContainer = CoworkColors.Red50,

    tertiary             = CoworkColors.Blue300,
    onTertiary           = CoworkColors.Blue900,
    tertiaryContainer    = CoworkColors.Blue900,
    onTertiaryContainer  = CoworkColors.Blue100,

    background           = CoworkColors.Neutral850,
    onBackground         = Color(0xFFDBDEE1),

    surface              = CoworkColors.Neutral800,
    onSurface            = Color(0xFFDBDEE1),
    surfaceVariant       = CoworkColors.Neutral700,
    onSurfaceVariant     = CoworkColors.Neutral300,
    surfaceContainer     = CoworkColors.Neutral750,
    surfaceContainerLow  = CoworkColors.Neutral800,
    surfaceContainerHigh = CoworkColors.Neutral700,

    outline              = CoworkColors.Neutral600,
    outlineVariant       = CoworkColors.Neutral700,

    error                = CoworkColors.Red400,
    onError              = Color.White,
    errorContainer       = CoworkColors.Red900,
    onErrorContainer     = CoworkColors.Red100,

    inverseSurface       = CoworkColors.Neutral100,
    inverseOnSurface     = CoworkColors.Neutral800,
    inversePrimary       = CoworkColors.Red600,
)

@Composable
fun CoworkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    ProvideCoworkExtendedColors(darkTheme = darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) CoworkDarkColorScheme else CoworkLightColorScheme,
            typography  = CoworkTypography,
            shapes      = CoworkShapes,
            content     = content,
        )
    }
}
