package com.kintsu.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

object KintsuTheme {
    val colors: KintsuColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKintsuColors.current

    val typography: KintsuTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalKintsuTypography.current

    val spacing: KintsuSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalKintsuSpacing.current

    val shapes: KintsuShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalKintsuShapes.current

    val elevation: KintsuElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalKintsuElevation.current

    val motion: KintsuMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalKintsuMotion.current
}

@Composable
fun KintsuTheme(
    appTheme: AppTheme = AppTheme.SAGE_IVORY,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentOption: AccentOption = AccentOption.GOLD,
    contentDensity: com.kintsu.app.data.ContentDensity = com.kintsu.app.data.ContentDensity.COMFORTABLE,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> if (appTheme == AppTheme.MIDNIGHT_CHAMPAGNE) true else isSystemDark
    }

    val basePalette = getThemePalette(appTheme, isDark)
    val activeColors = basePalette.copy(
        accent = accentOption.color,
        accentSoft = if (isDark) {
            accentOption.color.copy(alpha = 0.22f)
        } else {
            accentOption.color.copy(alpha = 0.16f)
        }
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = activeColors.background.toArgb()
            window.navigationBarColor = activeColors.background.toArgb()
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = !isDark
            insets.isAppearanceLightNavigationBars = !isDark
        }
    }

    val materialColors = if (isDark) {
        darkColorScheme(
            primary = activeColors.accent,
            onPrimary = activeColors.background,
            primaryContainer = activeColors.accentSoft,
            onPrimaryContainer = activeColors.textPrimary,
            surface = activeColors.surface,
            onSurface = activeColors.textPrimary,
            surfaceVariant = activeColors.surfaceElevated,
            onSurfaceVariant = activeColors.textSecondary,
            background = activeColors.background,
            onBackground = activeColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = activeColors.accent,
            onPrimary = activeColors.background,
            primaryContainer = activeColors.accentSoft,
            onPrimaryContainer = activeColors.textPrimary,
            surface = activeColors.surface,
            onSurface = activeColors.textPrimary,
            surfaceVariant = activeColors.surfaceElevated,
            onSurfaceVariant = activeColors.textSecondary,
            background = activeColors.background,
            onBackground = activeColors.textPrimary
        )
    }

    val activeSpacing = if (contentDensity == com.kintsu.app.data.ContentDensity.COMPACT) CompactSpacing else KintsuSpacing()
    val activeMotion = if (reducedMotion) ReducedMotion else KintsuMotion()

    CompositionLocalProvider(
        LocalKintsuColors provides activeColors,
        LocalKintsuSpacing provides activeSpacing,
        LocalKintsuShapes provides KintsuShapes(),
        LocalKintsuElevation provides KintsuElevation(),
        LocalKintsuMotion provides activeMotion,
        LocalKintsuTypography provides KintsuTypography()
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = MaterialTypographyBridge,
            content = content
        )
    }
}
