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
    val onAccent = when (accentOption) {
        AccentOption.GOLD, AccentOption.SAGE -> if (isDark) androidx.compose.ui.graphics.Color(0xFF161A17) else androidx.compose.ui.graphics.Color(0xFF1E231F)
        else -> androidx.compose.ui.graphics.Color.White
    }
    val accentText = if (isDark) {
        when (accentOption) {
            AccentOption.GOLD -> androidx.compose.ui.graphics.Color(0xFFE2C498)
            AccentOption.SAGE -> androidx.compose.ui.graphics.Color(0xFFA6C7B5)
            AccentOption.CLAY -> androidx.compose.ui.graphics.Color(0xFFE6AB9B)
            AccentOption.LAVENDER -> androidx.compose.ui.graphics.Color(0xFFC5B6DE)
            AccentOption.STONE -> androidx.compose.ui.graphics.Color(0xFFC6BEB2)
            AccentOption.TEAL -> androidx.compose.ui.graphics.Color(0xFF9EC0CE)
            AccentOption.CRIMSON -> androidx.compose.ui.graphics.Color(0xFFFF8B94)
            AccentOption.BLUE -> androidx.compose.ui.graphics.Color(0xFF64D2FF)
            AccentOption.NEON_GREEN -> androidx.compose.ui.graphics.Color(0xFF1ED760)
        }
    } else {
        when (accentOption) {
            AccentOption.GOLD -> androidx.compose.ui.graphics.Color(0xFF76561E)
            AccentOption.SAGE -> androidx.compose.ui.graphics.Color(0xFF3D5C4E)
            AccentOption.CLAY -> androidx.compose.ui.graphics.Color(0xFF7E3D27)
            AccentOption.LAVENDER -> androidx.compose.ui.graphics.Color(0xFF564771)
            AccentOption.STONE -> androidx.compose.ui.graphics.Color(0xFF544D42)
            AccentOption.TEAL -> androidx.compose.ui.graphics.Color(0xFF355361)
            AccentOption.CRIMSON -> androidx.compose.ui.graphics.Color(0xFF900C18)
            AccentOption.BLUE -> androidx.compose.ui.graphics.Color(0xFF0071E3)
            AccentOption.NEON_GREEN -> androidx.compose.ui.graphics.Color(0xFF107C35)
        }
    }

    val activeColors = basePalette.copy(
        accent = accentOption.color,
        accentSoft = if (isDark) {
            accentOption.color.copy(alpha = 0.22f)
        } else {
            accentOption.color.copy(alpha = 0.16f)
        },
        onAccent = onAccent,
        accentText = accentText
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
            onPrimary = activeColors.onAccent,
            primaryContainer = activeColors.accentSoft,
            onPrimaryContainer = activeColors.textPrimary,
            surface = activeColors.surface,
            onSurface = activeColors.textPrimary,
            surfaceVariant = activeColors.surfaceElevated,
            onSurfaceVariant = activeColors.textSecondary,
            background = activeColors.background,
            onBackground = activeColors.textPrimary,
            error = activeColors.error,
            onError = activeColors.onError,
            errorContainer = activeColors.errorContainer,
            onErrorContainer = activeColors.onErrorContainer,
            outline = activeColors.border,
            outlineVariant = activeColors.border.copy(alpha = 0.5f)
        )
    } else {
        lightColorScheme(
            primary = activeColors.accent,
            onPrimary = activeColors.onAccent,
            primaryContainer = activeColors.accentSoft,
            onPrimaryContainer = activeColors.textPrimary,
            surface = activeColors.surface,
            onSurface = activeColors.textPrimary,
            surfaceVariant = activeColors.surfaceElevated,
            onSurfaceVariant = activeColors.textSecondary,
            background = activeColors.background,
            onBackground = activeColors.textPrimary,
            error = activeColors.error,
            onError = activeColors.onError,
            errorContainer = activeColors.errorContainer,
            onErrorContainer = activeColors.onErrorContainer,
            outline = activeColors.border,
            outlineVariant = activeColors.border.copy(alpha = 0.5f)
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
