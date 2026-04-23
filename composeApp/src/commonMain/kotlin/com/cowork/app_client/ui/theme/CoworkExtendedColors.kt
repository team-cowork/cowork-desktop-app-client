package com.cowork.app_client.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CoworkExtendedColors(
    // ── Semantic status ───────────────────────────────────────────────────────
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,

    // ── Interactive ───────────────────────────────────────────────────────────
    val link: Color,

    // ── Surface layers (Discord-style depth) ─────────────────────────────────
    val surfaceDepth0: Color,  // 최하단 (앱 배경)
    val surfaceDepth1: Color,  // 사이드바, 패널
    val surfaceDepth2: Color,  // 카드, 리스트 아이템
    val surfaceDepth3: Color,  // 드롭다운, 호버

    // ── User status ───────────────────────────────────────────────────────────
    val statusOnline: Color,
    val statusDnd: Color,
    val statusIdle: Color,
    val statusOffline: Color,

    // ── Text emphasis ─────────────────────────────────────────────────────────
    val textNormal: Color,
    val textMuted: Color,
    val textSubtle: Color,
)

private val LightExtendedColors = CoworkExtendedColors(
    success          = CoworkColors.Green500,
    onSuccess        = Color.White,
    successContainer = CoworkColors.Green50,
    warning          = CoworkColors.Amber500,
    onWarning        = Color.White,
    warningContainer = CoworkColors.Amber50,
    link             = CoworkColors.Blue500,
    surfaceDepth0    = Color(0xFFF2F3F5),
    surfaceDepth1    = Color(0xFFFFFFFF),
    surfaceDepth2    = Color(0xFFF2F3F5),
    surfaceDepth3    = Color(0xFFE3E5E8),
    statusOnline     = CoworkColors.Green500,
    statusDnd        = CoworkColors.Red500,
    statusIdle       = CoworkColors.Amber400,
    statusOffline    = CoworkColors.Neutral400,
    textNormal       = Color(0xFF060607),
    textMuted        = Color(0xFF4E5058),
    textSubtle       = Color(0xFF80848E),
)

private val DarkExtendedColors = CoworkExtendedColors(
    success          = CoworkColors.Green400,
    onSuccess        = Color.White,
    successContainer = CoworkColors.Green900,
    warning          = CoworkColors.Amber400,
    onWarning        = Color.Black,
    warningContainer = CoworkColors.Amber900,
    link             = CoworkColors.Blue300,
    surfaceDepth0    = CoworkColors.Neutral850,
    surfaceDepth1    = CoworkColors.Neutral800,
    surfaceDepth2    = CoworkColors.Neutral750,
    surfaceDepth3    = CoworkColors.Neutral700,
    statusOnline     = CoworkColors.Green400,
    statusDnd        = CoworkColors.Red400,
    statusIdle       = CoworkColors.Amber300,
    statusOffline    = CoworkColors.Neutral500,
    textNormal       = Color(0xFFDBDEE1),
    textMuted        = Color(0xFF949BA4),
    textSubtle       = Color(0xFF6D6F78),
)

val LocalCoworkExtendedColors = staticCompositionLocalOf<CoworkExtendedColors> {
    error("CoworkExtendedColors not provided")
}

@Composable
fun ProvideCoworkExtendedColors(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    CompositionLocalProvider(LocalCoworkExtendedColors provides colors, content = content)
}

val coworkExtendedColors: CoworkExtendedColors
    @Composable get() = LocalCoworkExtendedColors.current
