package com.kintsu.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class AccentOption(val displayName: String, val color: Color) {
    GOLD("Kintsugi Gold", Color(0xFFB8935A)),
    SAGE("Dusty Sage", Color(0xFF6B8E7D)),
    CLAY("Terracotta Clay", Color(0xFFB86B52)),
    LAVENDER("Muted Lavender", Color(0xFF81729E)),
    STONE("Warm Stone", Color(0xFF7E7569)),
    TEAL("Misty Slate", Color(0xFF607D8B))
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppTheme(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val defaultAccent: AccentOption,
    val previewPalette: List<Color>
) {
    SAGE_IVORY(
        id = "SAGE_IVORY",
        displayName = "Sage & Ivory",
        subtitle = "Warm ivory, dusty sage & muted gold",
        defaultAccent = AccentOption.SAGE,
        previewPalette = listOf(
            Color(0xFFF7F5EE), // Warm ivory
            Color(0xFFDFE8D9), // Soft sage
            Color(0xFF829B83), // Muted green
            Color(0xFFB49A68)  // Antique gold
        )
    ),
    LAVENDER_MIST(
        id = "LAVENDER_MIST",
        displayName = "Lavender Mist",
        subtitle = "Pearl white, dusty lavender & plum",
        defaultAccent = AccentOption.LAVENDER,
        previewPalette = listOf(
            Color(0xFFF8F6FA), // Pearl
            Color(0xFFEAE4F1), // Dusty lavender
            Color(0xFF9B8BAD), // Muted violet
            Color(0xFFB5A0C5)  // Soft lilac
        )
    ),
    TERRACOTTA_LINEN(
        id = "TERRACOTTA_LINEN",
        displayName = "Terracotta Linen",
        subtitle = "Cream, pale clay & warm brown",
        defaultAccent = AccentOption.CLAY,
        previewPalette = listOf(
            Color(0xFFFAF5ED), // Cream
            Color(0xFFF0DDD0), // Pale clay
            Color(0xFFBF826C), // Terracotta
            Color(0xFF8E685B)  // Warm brown
        )
    ),
    COASTAL_MIST(
        id = "COASTAL_MIST",
        displayName = "Coastal Mist",
        subtitle = "Cool porcelain, mist blue & slate",
        defaultAccent = AccentOption.TEAL,
        previewPalette = listOf(
            Color(0xFFF4F8F9), // Cool white
            Color(0xFFDCE9EC), // Mist blue
            Color(0xFF7F9FAA), // Muted teal
            Color(0xFF607D8B)  // Slate
        )
    ),
    MIDNIGHT_CHAMPAGNE(
        id = "MIDNIGHT_CHAMPAGNE",
        displayName = "Midnight Champagne",
        subtitle = "Warm charcoal, graphite & champagne gold",
        defaultAccent = AccentOption.GOLD,
        previewPalette = listOf(
            Color(0xFF202224), // Charcoal
            Color(0xFF303236), // Graphite
            Color(0xFFC4AC7B), // Champagne gold
            Color(0xFFECE5D8)  // Warm pearl
        )
    )
}

@Immutable
data class KintsuColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceTranslucent: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSoft: Color,
    val goldSeam: Color,
    val goldSeamSoft: Color,
    val isDark: Boolean
)

// 01. Sage & Ivory
val SageIvoryLightPalette = KintsuColors(
    background = Color(0xFFF7F5EE),
    surface = Color(0xFFEDEAE0),
    surfaceElevated = Color(0xFFDFE8D9),
    surfaceTranslucent = Color(0xF2F7F5EE),
    border = Color(0xFFD2D9CC),
    textPrimary = Color(0xFF1E231F),
    textSecondary = Color(0xFF566157),
    textTertiary = Color(0xFF869287),
    accent = Color(0xFF829B83),
    accentSoft = Color(0xFFDFE8D9),
    goldSeam = Color(0xFFB49A68),
    goldSeamSoft = Color(0x33B49A68),
    isDark = false
)

val SageIvoryDarkPalette = KintsuColors(
    background = Color(0xFF181B18),
    surface = Color(0xFF222722),
    surfaceElevated = Color(0xFF2D332D),
    surfaceTranslucent = Color(0xF2181B18),
    border = Color(0xFF384038),
    textPrimary = Color(0xFFF2F5F0),
    textSecondary = Color(0xFFA5B2A4),
    textTertiary = Color(0xFF869585),
    accent = Color(0xFF90AC92),
    accentSoft = Color(0x3590AC92),
    goldSeam = Color(0xFFC4AC7B),
    goldSeamSoft = Color(0x40C4AC7B),
    isDark = true
)

// 02. Lavender Mist
val LavenderMistLightPalette = KintsuColors(
    background = Color(0xFFF8F6FA),
    surface = Color(0xFFEFEBF3),
    surfaceElevated = Color(0xFFEAE4F1),
    surfaceTranslucent = Color(0xF2F8F6FA),
    border = Color(0xFFDCD4E4),
    textPrimary = Color(0xFF211C28),
    textSecondary = Color(0xFF625970),
    textTertiary = Color(0xFF968D9F),
    accent = Color(0xFF9B8BAD),
    accentSoft = Color(0xFFEAE4F1),
    goldSeam = Color(0xFFB8935A),
    goldSeamSoft = Color(0x33B8935A),
    isDark = false
)

val LavenderMistDarkPalette = KintsuColors(
    background = Color(0xFF19171F),
    surface = Color(0xFF23202C),
    surfaceElevated = Color(0xFF2E2A3A),
    surfaceTranslucent = Color(0xF219171F),
    border = Color(0xFF3C374A),
    textPrimary = Color(0xFFF5F2F9),
    textSecondary = Color(0xFFAFA7BD),
    textTertiary = Color(0xFF8E869C),
    accent = Color(0xFFB5A0C5),
    accentSoft = Color(0x35B5A0C5),
    goldSeam = Color(0xFFC7A268),
    goldSeamSoft = Color(0x40C7A268),
    isDark = true
)

// 03. Terracotta Linen
val TerracottaLinenLightPalette = KintsuColors(
    background = Color(0xFFFAF5ED),
    surface = Color(0xFFF2EAE0),
    surfaceElevated = Color(0xFFF0DDD0),
    surfaceTranslucent = Color(0xF2FAF5ED),
    border = Color(0xFFDFCEBE),
    textPrimary = Color(0xFF261D19),
    textSecondary = Color(0xFF6E584E),
    textTertiary = Color(0xFF9E877B),
    accent = Color(0xFFBF826C),
    accentSoft = Color(0xFFF0DDD0),
    goldSeam = Color(0xFFB8935A),
    goldSeamSoft = Color(0x33B8935A),
    isDark = false
)

val TerracottaLinenDarkPalette = KintsuColors(
    background = Color(0xFF1D1815),
    surface = Color(0xFF28211D),
    surfaceElevated = Color(0xFF332B25),
    surfaceTranslucent = Color(0xF21D1815),
    border = Color(0xFF423730),
    textPrimary = Color(0xFFF7F2EC),
    textSecondary = Color(0xFFB4A198),
    textTertiary = Color(0xFF948278),
    accent = Color(0xFFD49580),
    accentSoft = Color(0x35D49580),
    goldSeam = Color(0xFFC7A268),
    goldSeamSoft = Color(0x40C7A268),
    isDark = true
)

// 04. Coastal Mist
val CoastalMistLightPalette = KintsuColors(
    background = Color(0xFFF4F8F9),
    surface = Color(0xFFE8F0F2),
    surfaceElevated = Color(0xFFDCE9EC),
    surfaceTranslucent = Color(0xF2F4F8F9),
    border = Color(0xFFCCDCE0),
    textPrimary = Color(0xFF142127),
    textSecondary = Color(0xFF4E636C),
    textTertiary = Color(0xFF81959E),
    accent = Color(0xFF607D8B),
    accentSoft = Color(0xFFDCE9EC),
    goldSeam = Color(0xFFB8935A),
    goldSeamSoft = Color(0x33B8935A),
    isDark = false
)

val CoastalMistDarkPalette = KintsuColors(
    background = Color(0xFF13181C),
    surface = Color(0xFF1B2228),
    surfaceElevated = Color(0xFF242C33),
    surfaceTranslucent = Color(0xF213181C),
    border = Color(0xFF2E3841),
    textPrimary = Color(0xFFEEF3F6),
    textSecondary = Color(0xFF9CB1BC),
    textTertiary = Color(0xFF8198A4),
    accent = Color(0xFF7F9FAA),
    accentSoft = Color(0x357F9FAA),
    goldSeam = Color(0xFFC7A268),
    goldSeamSoft = Color(0x40C7A268),
    isDark = true
)

// 05. Midnight Champagne
val MidnightChampagneDarkPalette = KintsuColors(
    background = Color(0xFF202224),
    surface = Color(0xFF282B2E),
    surfaceElevated = Color(0xFF303236),
    surfaceTranslucent = Color(0xF2202224),
    border = Color(0xFF3F4247),
    textPrimary = Color(0xFFECE5D8),
    textSecondary = Color(0xFFABA496),
    textTertiary = Color(0xFF8E8679),
    accent = Color(0xFFC4AC7B),
    accentSoft = Color(0x35C4AC7B),
    goldSeam = Color(0xFFC4AC7B),
    goldSeamSoft = Color(0x40C4AC7B),
    isDark = true
)

val MidnightChampagneLightPalette = KintsuColors(
    background = Color(0xFFF5F2ED),
    surface = Color(0xFFECE6DD),
    surfaceElevated = Color(0xFFE2DDD2),
    surfaceTranslucent = Color(0xF2F5F2ED),
    border = Color(0xFFD2CBC0),
    textPrimary = Color(0xFF202224),
    textSecondary = Color(0xFF5D5D61),
    textTertiary = Color(0xFF8B8B8F),
    accent = Color(0xFFA88E5C),
    accentSoft = Color(0x35A88E5C),
    goldSeam = Color(0xFFC4AC7B),
    goldSeamSoft = Color(0x33C4AC7B),
    isDark = false
)

fun getThemePalette(theme: AppTheme, isDark: Boolean): KintsuColors {
    return when (theme) {
        AppTheme.SAGE_IVORY -> if (isDark) SageIvoryDarkPalette else SageIvoryLightPalette
        AppTheme.LAVENDER_MIST -> if (isDark) LavenderMistDarkPalette else LavenderMistLightPalette
        AppTheme.TERRACOTTA_LINEN -> if (isDark) TerracottaLinenDarkPalette else TerracottaLinenLightPalette
        AppTheme.COASTAL_MIST -> if (isDark) CoastalMistDarkPalette else CoastalMistLightPalette
        AppTheme.MIDNIGHT_CHAMPAGNE -> if (isDark) MidnightChampagneDarkPalette else MidnightChampagneLightPalette
    }
}

// Backwards-compatible aliases
val LightPalette = SageIvoryLightPalette
val DarkPalette = SageIvoryDarkPalette
