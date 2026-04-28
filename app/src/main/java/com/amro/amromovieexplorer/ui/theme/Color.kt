package com.amro.amromovieexplorer.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * App-level color tokens.
 *
 * Features should not reference these directly. Prefer `MaterialTheme.colorScheme.*` roles in UI.
 * These tokens are only used to build the light/dark `ColorScheme` in `Theme.kt`.
 */
object AMROColorTokens {
    // Dark theme tokens
    val PrimaryDark = Color(0xFFD0BCFF)
    val SecondaryDark = Color(0xFFCCC2DC)
    val TertiaryDark = Color(0xFFEFB8C8)

    // Light theme tokens
    val PrimaryLight = Color(0xFF6650A4)
    val SecondaryLight = Color(0xFF625B71)
    val TertiaryLight = Color(0xFF7D5260)
}