package com.kintsu.app.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.ui.theme.AccentOption
import com.kintsu.app.ui.theme.AppTheme
import com.kintsu.app.ui.theme.ThemeMode
import com.kintsu.app.ui.theme.getThemePalette
import kotlinx.coroutines.flow.first

data class WidgetColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSoft: Color,
    val goldSeam: Color,
    val goldSeamSoft: Color,
    val isDark: Boolean,
    val backgroundProvider: ColorProvider,
    val surfaceProvider: ColorProvider,
    val surfaceElevatedProvider: ColorProvider,
    val borderProvider: ColorProvider,
    val textPrimaryProvider: ColorProvider,
    val textSecondaryProvider: ColorProvider,
    val textTertiaryProvider: ColorProvider,
    val accentProvider: ColorProvider,
    val accentSoftProvider: ColorProvider,
    val goldSeamProvider: ColorProvider
)

object KintsuWidgetTheme {

    suspend fun getColors(context: Context): WidgetColors {
        val settingsStore = SettingsDataStore(context)
        val settings = settingsStore.settingsFlow.first()

        val isSystemNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDark = when (settings.themeMode) {
            ThemeMode.SYSTEM -> isSystemNight
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        val palette = getThemePalette(settings.appTheme, isDark)
        val activeAccent = settings.accentOption.color

        return WidgetColors(
            background = palette.background,
            surface = palette.surface,
            surfaceElevated = palette.surfaceElevated,
            border = palette.border,
            textPrimary = palette.textPrimary,
            textSecondary = palette.textSecondary,
            textTertiary = palette.textTertiary,
            accent = activeAccent,
            accentSoft = palette.accentSoft,
            goldSeam = palette.goldSeam,
            goldSeamSoft = palette.goldSeamSoft,
            isDark = isDark,
            backgroundProvider = ColorProvider(palette.background),
            surfaceProvider = ColorProvider(palette.surface),
            surfaceElevatedProvider = ColorProvider(palette.surfaceElevated),
            borderProvider = ColorProvider(palette.border),
            textPrimaryProvider = ColorProvider(palette.textPrimary),
            textSecondaryProvider = ColorProvider(palette.textSecondary),
            textTertiaryProvider = ColorProvider(palette.textTertiary),
            accentProvider = ColorProvider(activeAccent),
            accentSoftProvider = ColorProvider(palette.accentSoft),
            goldSeamProvider = ColorProvider(palette.goldSeam)
        )
    }
}
