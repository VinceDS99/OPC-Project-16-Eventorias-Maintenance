package com.openclassrooms.eventorias.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EventoriasColorScheme = darkColorScheme(
    primary    = RedPrimary,
    background = DarkBackground,
    surface    = DarkSurface,
    onPrimary  = White,
    onBackground = White,
    onSurface  = White
)

@Composable
fun EventoriasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EventoriasColorScheme,
        content = content
    )
}