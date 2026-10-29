package com.kintsu.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class ThemePaletteTest {

    private fun getLinearComponent(c: Float): Double {
        return if (c <= 0.04045f) {
            c / 12.92
        } else {
            ((c + 0.055) / 1.055).pow(2.4)
        }
    }

    private fun getLuminance(color: Color): Double {
        val r = getLinearComponent(color.red)
        val g = getLinearComponent(color.green)
        val b = getLinearComponent(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun calculateContrast(foreground: Color, background: Color): Double {
        val l1 = getLuminance(foreground)
        val l2 = getLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun testAllFiveThemesExistWithPreviews() {
        val themes = AppTheme.values()
        assertEquals(5, themes.size)

        themes.forEach { theme ->
            assertTrue(theme.displayName.isNotBlank())
            assertTrue(theme.subtitle.isNotBlank())
            assertEquals(4, theme.previewPalette.size)
            assertNotNull(theme.defaultAccent)
        }
    }

    @Test
    fun testWcagContrastForAllThemeLightPalettes() {
        AppTheme.values().forEach { theme ->
            val palette = getThemePalette(theme, isDark = false)
            val contrastPrimary = calculateContrast(palette.textPrimary, palette.background)
            val contrastSecondary = calculateContrast(palette.textSecondary, palette.background)

            assertTrue(
                "Theme ${theme.displayName} Light textPrimary contrast $contrastPrimary must be >= 4.5:1",
                contrastPrimary >= 4.5
            )
            assertTrue(
                "Theme ${theme.displayName} Light textSecondary contrast $contrastSecondary must be >= 4.0:1",
                contrastSecondary >= 4.0
            )
            assertEquals(false, palette.isDark)
        }
    }

    @Test
    fun testWcagContrastForAllThemeDarkPalettes() {
        AppTheme.values().forEach { theme ->
            val palette = getThemePalette(theme, isDark = true)
            val contrastPrimary = calculateContrast(palette.textPrimary, palette.background)
            val contrastSecondary = calculateContrast(palette.textSecondary, palette.background)

            assertTrue(
                "Theme ${theme.displayName} Dark textPrimary contrast $contrastPrimary must be >= 4.5:1",
                contrastPrimary >= 4.5
            )
            assertTrue(
                "Theme ${theme.displayName} Dark textSecondary contrast $contrastSecondary must be >= 4.0:1",
                contrastSecondary >= 4.0
            )
            assertEquals(true, palette.isDark)
        }
    }

    @Test
    fun testMidnightChampagneSpecialIdentityRules() {
        val darkPalette = getThemePalette(AppTheme.MIDNIGHT_CHAMPAGNE, isDark = true)
        val lightPalette = getThemePalette(AppTheme.MIDNIGHT_CHAMPAGNE, isDark = false)

        assertTrue(darkPalette.isDark)
        assertEquals(false, lightPalette.isDark)

        // Charcoal background for dark identity
        assertEquals(Color(0xFF202224), darkPalette.background)
        // Warm pearl cream for light identity
        assertEquals(Color(0xFFF5F2ED), lightPalette.background)

        // Gold seam preserved
        assertEquals(Color(0xFFC4AC7B), darkPalette.goldSeam)
        assertEquals(Color(0xFFC4AC7B), lightPalette.goldSeam)
    }

    @Test
    fun testGoldRepairSeamPreservedAcrossAllThemes() {
        AppTheme.values().forEach { theme ->
            val light = getThemePalette(theme, isDark = false)
            val dark = getThemePalette(theme, isDark = true)

            assertNotNull(light.goldSeam)
            assertNotNull(light.goldSeamSoft)
            assertNotNull(dark.goldSeam)
            assertNotNull(dark.goldSeamSoft)
        }
    }
}
