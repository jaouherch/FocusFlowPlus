package com.example.focusflowplus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WellnessColorScheme = lightColorScheme(
    primary = Sage,
    onPrimary = WarmWhite,
    primaryContainer = SageLight,
    onPrimaryContainer = SageDark,

    secondary = Sand,
    onSecondary = WarmWhite,
    secondaryContainer = SandLight,
    onSecondaryContainer = SandDark,

    background = CreamWhite,
    onBackground = TextPrimary,

    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceWarm,
    onSurfaceVariant = TextSecondary,

    outline = SoftGray,
    error = SoftRed
)

@Composable
fun FocusFlowPlusTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WellnessColorScheme,
        typography = Typography,
        content = content
    )
}