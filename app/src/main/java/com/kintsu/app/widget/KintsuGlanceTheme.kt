package com.kintsu.app.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

object KintsuGlanceTheme {
    val background = Color(0xFFF4EFE7)
    val surface = Color(0xFFEBE3D7)
    val textPrimary = Color(0xFF2B2622)
    val textSecondary = Color(0xFF756C63)
    val goldAccent = Color(0xFFB8935A)
    val goldAccentSoft = Color(0xFFE9DCBA)

    val backgroundProvider = ColorProvider(background)
    val surfaceProvider = ColorProvider(surface)
    val textPrimaryProvider = ColorProvider(textPrimary)
    val textSecondaryProvider = ColorProvider(textSecondary)
    val goldAccentProvider = ColorProvider(goldAccent)
    val goldAccentSoftProvider = ColorProvider(goldAccentSoft)
}
