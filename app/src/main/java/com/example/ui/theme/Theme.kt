package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val StudioDarkColorScheme = darkColorScheme(
    primary = RosePrimary,
    onPrimary = TextPrimary,
    primaryContainer = RoseContainer,
    onPrimaryContainer = RoseSoft,
    secondary = AmethystSecondary,
    onSecondary = TextPrimary,
    secondaryContainer = AmethystContainer,
    onSecondaryContainer = AmethystSoft,
    tertiary = EmeraldAccent,
    onTertiary = TextPrimary,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldAccent,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    error = RosePrimary,
    onError = TextPrimary
)

private val StudioShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun PersonalAICreativeStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioDarkColorScheme,
        typography = StudioTypography,
        shapes = StudioShapes,
        content = content
    )
}
