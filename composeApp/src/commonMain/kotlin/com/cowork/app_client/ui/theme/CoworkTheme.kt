package com.cowork.app_client.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object CoworkColors {
    val Red50 = Color(0xFFFFEEEE)
    val Red100 = Color(0xFFFFD4D6)
    val Red200 = Color(0xFFFEAFB4)
    val Red300 = Color(0xFFFB8890)
    val Red400 = Color(0xFFF66570)
    val Red500 = Color(0xFFF04452)
    val Red700 = Color(0xFFD22030)
    val Red800 = Color(0xFFBC1B2A)
    val Red900 = Color(0xFFA51926)
}

private val CoworkLightColorScheme = lightColorScheme(
    primary = CoworkColors.Red500,
    onPrimary = Color.White,
    primaryContainer = CoworkColors.Red50,
    onPrimaryContainer = CoworkColors.Red900,
    secondary = CoworkColors.Red700,
    onSecondary = Color.White,
    secondaryContainer = CoworkColors.Red100,
    onSecondaryContainer = CoworkColors.Red900,
    tertiary = Color(0xFF2F6FED),
    onTertiary = Color.White,
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A1A),
    surface = Color.White,
    onSurface = Color(0xFF201A1A),
    surfaceVariant = Color(0xFFF5DDDF),
    onSurfaceVariant = Color(0xFF534344),
    outline = Color(0xFF857374),
    error = CoworkColors.Red700,
    onError = Color.White,
)

private val CoworkDarkColorScheme = darkColorScheme(
    primary = CoworkColors.Red500,
    onPrimary = Color.White,
    primaryContainer = CoworkColors.Red900,
    onPrimaryContainer = CoworkColors.Red50,
    secondary = CoworkColors.Red300,
    onSecondary = Color(0xFF4C0008),
    secondaryContainer = CoworkColors.Red800,
    onSecondaryContainer = CoworkColors.Red50,
    tertiary = Color(0xFF9FC2FF),
    onTertiary = Color(0xFF00315F),
    background = Color(0xFF191113),
    onBackground = Color(0xFFF1DDDF),
    surface = Color(0xFF201719),
    onSurface = Color(0xFFF1DDDF),
    surfaceVariant = Color(0xFF534344),
    onSurfaceVariant = Color(0xFFD8C2C4),
    outline = Color(0xFFA08C8E),
    error = CoworkColors.Red300,
    onError = Color(0xFF69000F),
)

@Composable
fun CoworkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) CoworkDarkColorScheme else CoworkLightColorScheme,
        content = content,
    )
}
