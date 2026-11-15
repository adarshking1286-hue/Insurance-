package com.insurancebazaar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF0B57D0),
    secondary = Color(0xFFFF8F00)
)
private val Dark = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    secondary = Color(0xFFFFB74D)
)

@Composable
fun InsuranceBazaarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
