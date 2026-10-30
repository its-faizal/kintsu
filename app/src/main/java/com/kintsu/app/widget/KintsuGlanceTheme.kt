package com.kintsu.app.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import com.kintsu.app.R

object KintsuGlanceTheme {
    val background = Color(0xFFF7F5EE)
    val surface = Color(0xFFEDEAE0)
    val textPrimary = Color(0xFF1E231F)
    val textSecondary = Color(0xFF566157)
    val goldAccent = Color(0xFFB49A68)
    val goldAccentSoft = Color(0xFFDFE8D9)

    val backgroundProvider = ColorProvider(R.color.widget_background)
    val surfaceProvider = ColorProvider(R.color.widget_surface)
    val textPrimaryProvider = ColorProvider(R.color.widget_text_primary)
    val textSecondaryProvider = ColorProvider(R.color.widget_text_secondary)
    val goldAccentProvider = ColorProvider(R.color.widget_gold_accent)
    val goldAccentSoftProvider = ColorProvider(R.color.widget_gold_accent_soft)
}
