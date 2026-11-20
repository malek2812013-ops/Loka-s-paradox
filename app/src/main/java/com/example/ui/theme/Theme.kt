package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LokaParadoxColorScheme = darkColorScheme(
    primary = SepiaAmber,
    onPrimary = ObsidianBg,
    primaryContainer = EspressoCardElevated,
    onPrimaryContainer = SepiaGold,
    secondary = SepiaBronze,
    onSecondary = ObsidianBg,
    secondaryContainer = EspressoCard,
    onSecondaryContainer = ParchmentInk,
    tertiary = SoulParadoxCrimson,
    onTertiary = ParchmentInk,
    background = ObsidianBg,
    onBackground = ParchmentInk,
    surface = EspressoSurface,
    onSurface = ParchmentInk,
    surfaceVariant = EspressoCard,
    onSurfaceVariant = MutedSepiaInk,
    outline = SepiaDarkBorder,
    outlineVariant = SepiaBronze,
    error = DangerRed,
    onError = ParchmentInk
)

@Composable
fun LokaParadoxTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LokaParadoxColorScheme,
        typography = AppTypography,
        content = content
    )
}
