package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Flota strictly adheres to White + Light Green theme as defined in PRD §7.1
private val FlotaLightColorScheme = lightColorScheme(
    primary = FlotaGreenDark,
    onPrimary = FlotaBackgroundWhite,
    primaryContainer = FlotaGreenContainer,
    onPrimaryContainer = FlotaGreenDeep,
    inversePrimary = FlotaGreenAccent,
    secondary = FlotaGreenPrimary,
    onSecondary = FlotaBackgroundWhite,
    secondaryContainer = FlotaGreenContainerSubtle,
    onSecondaryContainer = FlotaGreenDeep,
    tertiary = FlotaInfo,
    onTertiary = FlotaBackgroundWhite,
    background = FlotaBackgroundWhite,
    onBackground = FlotaTextPrimary,
    surface = FlotaSurfaceWhite,
    onSurface = FlotaTextPrimary,
    surfaceVariant = FlotaSurfaceTinted,
    onSurfaceVariant = FlotaTextSecondary,
    outline = FlotaBorder,
    outlineVariant = FlotaBorderSubtle,
    error = FlotaError,
    onError = FlotaBackgroundWhite,
    errorContainer = FlotaErrorContainer,
    onErrorContainer = FlotaError
)

@Composable
fun FlotaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FlotaLightColorScheme,
        typography = Typography,
        content = content
    )
}
