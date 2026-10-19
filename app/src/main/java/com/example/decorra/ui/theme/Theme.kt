package com.example.decorra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Scheme = lightColorScheme(
    primary = Espresso,
    onPrimary = Cream,
    secondary = Sand,
    onSecondary = Espresso,
    tertiary = Sage,
    background = Cream,
    onBackground = Espresso,
    surface = Cream,
    onSurface = Espresso,
    outline = Taupe,
)

@Composable
fun DecorraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}