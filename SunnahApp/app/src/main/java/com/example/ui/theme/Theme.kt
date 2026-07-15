package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    // Dynamically retrieve values evaluating the mutable custom state getters
    val colorScheme = darkColorScheme(
        primary = GoldClassic,
        onPrimary = GoldDark,
        primaryContainer = EmeraldSoft,
        onPrimaryContainer = GoldLight,
        secondary = GoldAccent,
        onSecondary = GoldDark,
        background = EmeraldDark,
        onBackground = IvoryWhite,
        surface = CardBackground,
        onSurface = IvoryWhite,
        surfaceVariant = EmeraldLight,
        onSurfaceVariant = SecondaryText
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
