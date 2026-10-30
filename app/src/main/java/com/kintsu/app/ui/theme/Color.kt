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
    ),
    SAKURA_DAWN(
        id = "SAKURA_DAWN",
        displayName = "Sakura Dawn",
        subtitle = "Cherry-blossom pink, pale rose & mauve",
        defaultAccent = AccentOption.LAVENDER,
        previewPalette = listOf(
            Color(0xFFFFF5F7),
            Color(0xFFF5DFE6),
            Color(0xFFC9637E),
            Color(0xFF967280)
        )
    ),
    MATCHA_ATELIER(
        id = "MATCHA_ATELIER",
        displayName = "Matcha Atelier",
        subtitle = "Cream, matcha green, olive & warm stone",
        defaultAccent = AccentOption.SAGE,
        previewPalette = listOf(
            Color(0xFFF7F6F0),
            Color(0xFFE6E2D6),
            Color(0xFF637C58),
            Color(0xFFC5A059)
        )
    ),
    OCEAN_PEARL(
        id = "OCEAN_PEARL",
        displayName = "Ocean Pearl",
        subtitle = "Pale aqua, pearl white, teal & slate",
        defaultAccent = AccentOption.TEAL,
        previewPalette = listOf(
            Color(0xFFF2F7F7),
            Color(0xFFDCE7E8),
            Color(0xFF3E777D),
            Color(0xFF4A606A)
        )
    ),
    ROSEWOOD(
        id = "ROSEWOOD",
        displayName = "Rosewood",
        subtitle = "Warm brown, dusty rose & antique gold",
        defaultAccent = AccentOption.CLAY,
        previewPalette = listOf(
            Color(0xFFF9F4F2),
            Color(0xFFE8DCD7),
            Color(0xFFA6655B),
            Color(0xFFB88F51)
        )
    ),
    NORDIC_FROST(
        id = "NORDIC_FROST",
        displayName = "Nordic Frost",
        subtitle = "Cool ivory, fog grey & muted blue",
        defaultAccent = AccentOption.STONE,
        previewPalette = listOf(
            Color(0xFFF4F6F8),
            Color(0xFFDEE3E8),
            Color(0xFF4B6E8A),
            Color(0xFF8A96A0)
        )
    ),
    ECLIPSE_GOLD(
        id = "ECLIPSE_GOLD",
        displayName = "Eclipse Gold",
        subtitle = "Graphite, charcoal, bronze & champagne",
        defaultAccent = AccentOption.GOLD,
        previewPalette = listOf(
            Color(0xFF1C1E20),
            Color(0xFF24262A),
            Color(0xFFD4B36A),
            Color(0xFFE2CCA2)
        )
    ),
    GOLDEN_HOUR(
        id = "GOLDEN_HOUR",
        displayName = "Golden Hour",
        subtitle = "Sandstone, pale peach & muted amber",
        defaultAccent = AccentOption.GOLD,
        previewPalette = listOf(
            Color(0xFFFBF7F0),
            Color(0xFFEDE2D0),
            Color(0xFFB87333),
            Color(0xFFC48F42)
        )
    ),
    BOTANICAL_PAPER(
        id = "BOTANICAL_PAPER",
        displayName = "Botanical Paper",
        subtitle = "Paper white, eucalyptus & pale moss",
        defaultAccent = AccentOption.SAGE,
        previewPalette = listOf(
            Color(0xFFFAF8F5),
            Color(0xFFE6E1D7),
            Color(0xFF5B7B69),
            Color(0xFF7A8580)
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
    val isDark: Boolean,
    val accentText: Color = Color.Unspecified,
    val onAccent: Color = Color.Unspecified,
    val error: Color = Color(0xFFBA1A1A),
    val onError: Color = Color(0xFFFFFFFF),
    val errorContainer: Color = Color(0xFFFFDAD6),
    val onErrorContainer: Color = Color(0xFF410002),
    val warning: Color = Color(0xFF8A5100)
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
    textTertiary = Color(0xFF5C695D),
    accent = Color(0xFF829B83),
    accentSoft = Color(0xFFDFE8D9),
    goldSeam = Color(0xFFB49A68),
    goldSeamSoft = Color(0x33B49A68),
    isDark = false,
    accentText = Color(0xFF3D5C4E),
    onAccent = Color(0xFF1E231F),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
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
    isDark = true,
    accentText = Color(0xFFA6C7B5),
    onAccent = Color(0xFF161A17),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
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
    textTertiary = Color(0xFF696173),
    accent = Color(0xFF9B8BAD),
    accentSoft = Color(0xFFEAE4F1),
    goldSeam = Color(0xFFB8935A),
    goldSeamSoft = Color(0x33B8935A),
    isDark = false,
    accentText = Color(0xFF564771),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
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
    isDark = true,
    accentText = Color(0xFFC5B6DE),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
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
    textTertiary = Color(0xFF735D53),
    accent = Color(0xFFBF826C),
    accentSoft = Color(0xFFF0DDD0),
    goldSeam = Color(0xFFB8935A),
    goldSeamSoft = Color(0x33B8935A),
    isDark = false,
    accentText = Color(0xFF7E3D27),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
)

val TerracottaLinenDarkPalette = KintsuColors(
    background = Color(0xFF1D1815),
    surface = Color(0xFF28211D),
    surfaceElevated = Color(0xFF332B25),
    surfaceTranslucent = Color(0xF21D1815),
    border = Color(0xFF423730),
    textPrimary = Color(0xFFF7F2EC),
    textSecondary = Color(0xFFB4A198),
    textTertiary = Color(0xFF9E8C82),
    accent = Color(0xFFD49580),
    accentSoft = Color(0x35D49580),
    goldSeam = Color(0xFFC7A268),
    goldSeamSoft = Color(0x40C7A268),
    isDark = true,
    accentText = Color(0xFFE6AB9B),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
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
    textTertiary = Color(0xFF546B75),
    accent = Color(0xFF607D8B),
    accentSoft = Color(0xFFDCE9EC),
    goldSeam = Color(0xFFB8935A),
    goldSeamSoft = Color(0x33B8935A),
    isDark = false,
    accentText = Color(0xFF355361),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
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
    isDark = true,
    accentText = Color(0xFF9EC0CE),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
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
    textTertiary = Color(0xFF9E9587),
    accent = Color(0xFFC4AC7B),
    accentSoft = Color(0x35C4AC7B),
    goldSeam = Color(0xFFC4AC7B),
    goldSeamSoft = Color(0x40C4AC7B),
    isDark = true,
    accentText = Color(0xFFE2C498),
    onAccent = Color(0xFF161A17),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
)

val MidnightChampagneLightPalette = KintsuColors(
    background = Color(0xFFF5F2ED),
    surface = Color(0xFFECE6DD),
    surfaceElevated = Color(0xFFE2DDD2),
    surfaceTranslucent = Color(0xF2F5F2ED),
    border = Color(0xFFD2CBC0),
    textPrimary = Color(0xFF202224),
    textSecondary = Color(0xFF5D5D61),
    textTertiary = Color(0xFF656569),
    accent = Color(0xFFA88E5C),
    accentSoft = Color(0x35A88E5C),
    goldSeam = Color(0xFFC4AC7B),
    goldSeamSoft = Color(0x33C4AC7B),
    isDark = false,
    accentText = Color(0xFF76561E),
    onAccent = Color(0xFF1E231F),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
)


// SAKURA DAWN
val SakuraDawnLightPalette = KintsuColors(
    background = Color(0xFFFFF5F7),
    surface = Color(0xFFFCEEF2),
    surfaceElevated = Color(0xFFF7E5EB),
    surfaceTranslucent = Color(0xF2FFF5F7),
    border = Color(0xFFEAD0D9),
    textPrimary = Color(0xFF362128),
    textSecondary = Color(0xFF785E67),
    textTertiary = Color(0xFF967280),
    accent = Color(0xFFC9637E),
    accentSoft = Color(0x30C9637E),
    goldSeam = Color(0xFFD4AF37),
    goldSeamSoft = Color(0x33D4AF37),
    isDark = false,
    accentText = Color(0xFF8C344E),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
)
val SakuraDawnDarkPalette = KintsuColors(
    background = Color(0xFF1A1215),
    surface = Color(0xFF241A1E),
    surfaceElevated = Color(0xFF2F2228),
    surfaceTranslucent = Color(0xF21A1215),
    border = Color(0xFF47363E),
    textPrimary = Color(0xFFF5DFE6),
    textSecondary = Color(0xFFB8A0A9),
    textTertiary = Color(0xFF8C777F),
    accent = Color(0xFFE88FA7),
    accentSoft = Color(0x35E88FA7),
    goldSeam = Color(0xFFE2C498),
    goldSeamSoft = Color(0x40E2C498),
    isDark = true,
    accentText = Color(0xFFFFB1C5),
    onAccent = Color(0xFF3D1421),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
)

// MATCHA ATELIER
val MatchaAtelierLightPalette = KintsuColors(
    background = Color(0xFFF7F6F0),
    surface = Color(0xFFEFECE2),
    surfaceElevated = Color(0xFFE6E2D6),
    surfaceTranslucent = Color(0xF2F7F6F0),
    border = Color(0xFFD4CFC2),
    textPrimary = Color(0xFF252923),
    textSecondary = Color(0xFF5C6357),
    textTertiary = Color(0xFF7B8275),
    accent = Color(0xFF637C58),
    accentSoft = Color(0x30637C58),
    goldSeam = Color(0xFFC5A059),
    goldSeamSoft = Color(0x33C5A059),
    isDark = false,
    accentText = Color(0xFF3F5436),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
)
val MatchaAtelierDarkPalette = KintsuColors(
    background = Color(0xFF151713),
    surface = Color(0xFF1E211C),
    surfaceElevated = Color(0xFF272B24),
    surfaceTranslucent = Color(0xF2151713),
    border = Color(0xFF3C4238),
    textPrimary = Color(0xFFE6E9E3),
    textSecondary = Color(0xFF9DA598),
    textTertiary = Color(0xFF7E8578),
    accent = Color(0xFF8DA382),
    accentSoft = Color(0x358DA382),
    goldSeam = Color(0xFFD8B570),
    goldSeamSoft = Color(0x40D8B570),
    isDark = true,
    accentText = Color(0xFFAEC4A3),
    onAccent = Color(0xFF182214),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
)

// OCEAN PEARL
val OceanPearlLightPalette = KintsuColors(
    background = Color(0xFFF2F7F7),
    surface = Color(0xFFE7EFF0),
    surfaceElevated = Color(0xFFDCE7E8),
    surfaceTranslucent = Color(0xF2F2F7F7),
    border = Color(0xFFC5D5D7),
    textPrimary = Color(0xFF1E292B),
    textSecondary = Color(0xFF516467),
    textTertiary = Color(0xFF708488),
    accent = Color(0xFF3E777D),
    accentSoft = Color(0x303E777D),
    goldSeam = Color(0xFFC9A865),
    goldSeamSoft = Color(0x33C9A865),
    isDark = false,
    accentText = Color(0xFF265257),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
)
val OceanPearlDarkPalette = KintsuColors(
    background = Color(0xFF111617),
    surface = Color(0xFF182022),
    surfaceElevated = Color(0xFF202A2D),
    surfaceTranslucent = Color(0xF2111617),
    border = Color(0xFF334347),
    textPrimary = Color(0xFFE2EBED),
    textSecondary = Color(0xFF95A6A9),
    textTertiary = Color(0xFF738386),
    accent = Color(0xFF5FA3AB),
    accentSoft = Color(0x355FA3AB),
    goldSeam = Color(0xFFD4B679),
    goldSeamSoft = Color(0x40D4B679),
    isDark = true,
    accentText = Color(0xFF85CBD3),
    onAccent = Color(0xFF0F2629),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
)

// ROSEWOOD
val RosewoodLightPalette = KintsuColors(
    background = Color(0xFFF9F4F2),
    surface = Color(0xFFF1E8E4),
    surfaceElevated = Color(0xFFE8DCD7),
    surfaceTranslucent = Color(0xF2F9F4F2),
    border = Color(0xFFD4C3BC),
    textPrimary = Color(0xFF302320),
    textSecondary = Color(0xFF68544F),
    textTertiary = Color(0xFF85706A),
    accent = Color(0xFFA6655B),
    accentSoft = Color(0x30A6655B),
    goldSeam = Color(0xFFB88F51),
    goldSeamSoft = Color(0x33B88F51),
    isDark = false,
    accentText = Color(0xFF783E36),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
)
val RosewoodDarkPalette = KintsuColors(
    background = Color(0xFF1A1312),
    surface = Color(0xFF261C1A),
    surfaceElevated = Color(0xFF322522),
    surfaceTranslucent = Color(0xF21A1312),
    border = Color(0xFF4A3A35),
    textPrimary = Color(0xFFF4EAE7),
    textSecondary = Color(0xFFB8A29D),
    textTertiary = Color(0xFF8C7771),
    accent = Color(0xFFC78074),
    accentSoft = Color(0x35C78074),
    goldSeam = Color(0xFFC5A880),
    goldSeamSoft = Color(0x40C5A880),
    isDark = true,
    accentText = Color(0xFFE8A599),
    onAccent = Color(0xFF3B1712),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
)

// NORDIC FROST
val NordicFrostLightPalette = KintsuColors(
    background = Color(0xFFF4F6F8),
    surface = Color(0xFFE9EDF1),
    surfaceElevated = Color(0xFFDEE3E8),
    surfaceTranslucent = Color(0xF2F4F6F8),
    border = Color(0xFFC8D0D8),
    textPrimary = Color(0xFF1F252C),
    textSecondary = Color(0xFF525D69),
    textTertiary = Color(0xFF727D8A),
    accent = Color(0xFF4B6E8A),
    accentSoft = Color(0x304B6E8A),
    goldSeam = Color(0xFFBFA26F),
    goldSeamSoft = Color(0x33BFA26F),
    isDark = false,
    accentText = Color(0xFF2E4B63),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
)
val NordicFrostDarkPalette = KintsuColors(
    background = Color(0xFF12161A),
    surface = Color(0xFF1A2026),
    surfaceElevated = Color(0xFF232B33),
    surfaceTranslucent = Color(0xF212161A),
    border = Color(0xFF35424F),
    textPrimary = Color(0xFFE3E8ED),
    textSecondary = Color(0xFF96A3AF),
    textTertiary = Color(0xFF73808C),
    accent = Color(0xFF6D94B3),
    accentSoft = Color(0x356D94B3),
    goldSeam = Color(0xFFCEB382),
    goldSeamSoft = Color(0x40CEB382),
    isDark = true,
    accentText = Color(0xFF94BBDB),
    onAccent = Color(0xFF102538),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
)

// ECLIPSE GOLD
val EclipseGoldLightPalette = KintsuColors(
    background = Color(0xFFF4F4F3),
    surface = Color(0xFFE8E7E5),
    surfaceElevated = Color(0xFFDCDAD7),
    surfaceTranslucent = Color(0xF2F4F4F3),
    border = Color(0xFFC4C1BC),
    textPrimary = Color(0xFF1E1F21),
    textSecondary = Color(0xFF585755),
    textTertiary = Color(0xFF737270),
    accent = Color(0xFF8A6D3B),
    accentSoft = Color(0x308A6D3B),
    goldSeam = Color(0xFFC4A053),
    goldSeamSoft = Color(0x33C4A053),
    isDark = false,
    accentText = Color(0xFF614B23),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
)
val EclipseGoldDarkPalette = KintsuColors(
    background = Color(0xFF131416),
    surface = Color(0xFF1B1D20),
    surfaceElevated = Color(0xFF24262A),
    surfaceTranslucent = Color(0xF2131416),
    border = Color(0xFF3A3D42),
    textPrimary = Color(0xFFE7E6E2),
    textSecondary = Color(0xA3A19C00),
    textTertiary = Color(0xFF7D7B76),
    accent = Color(0xFFD4B36A),
    accentSoft = Color(0x35D4B36A),
    goldSeam = Color(0xFFE2CCA2),
    goldSeamSoft = Color(0x40E2CCA2),
    isDark = true,
    accentText = Color(0xFFF0D597),
    onAccent = Color(0xFF2B2109),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
)

// GOLDEN HOUR
val GoldenHourLightPalette = KintsuColors(
    background = Color(0xFFFBF7F0),
    surface = Color(0xFFF5EDE0),
    surfaceElevated = Color(0xFFEDE2D0),
    surfaceTranslucent = Color(0xF2FBF7F0),
    border = Color(0xFFD9CBB5),
    textPrimary = Color(0xFF31271D),
    textSecondary = Color(0xFF6B5B49),
    textTertiary = Color(0xFF8B7A66),
    accent = Color(0xFFB87333),
    accentSoft = Color(0x30B87333),
    goldSeam = Color(0xFFC48F42),
    goldSeamSoft = Color(0x33C48F42),
    isDark = false,
    accentText = Color(0xFF854E1B),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
)
val GoldenHourDarkPalette = KintsuColors(
    background = Color(0xFF181410),
    surface = Color(0xFF241E18),
    surfaceElevated = Color(0xFF2F2720),
    surfaceTranslucent = Color(0xF2181410),
    border = Color(0xFF473C32),
    textPrimary = Color(0xFFF7EFE3),
    textSecondary = Color(0xFFBFAFA0),
    textTertiary = Color(0xFF948373),
    accent = Color(0xFFD99455),
    accentSoft = Color(0x35D99455),
    goldSeam = Color(0xFFE5AC5A),
    goldSeamSoft = Color(0x40E5AC5A),
    isDark = true,
    accentText = Color(0xFFF5BC87),
    onAccent = Color(0xFF361C05),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
)

// BOTANICAL PAPER
val BotanicalPaperLightPalette = KintsuColors(
    background = Color(0xFFFAF8F5),
    surface = Color(0xFFF1EDE6),
    surfaceElevated = Color(0xFFE6E1D7),
    surfaceTranslucent = Color(0xF2FAF8F5),
    border = Color(0xFFD1CABF),
    textPrimary = Color(0xFF242A27),
    textSecondary = Color(0xFF59635E),
    textTertiary = Color(0xFF7A8580),
    accent = Color(0xFF5B7B69),
    accentSoft = Color(0x305B7B69),
    goldSeam = Color(0xFFBFA467),
    goldSeamSoft = Color(0x33BFA467),
    isDark = false,
    accentText = Color(0xFF385544),
    onAccent = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    warning = Color(0xFF8A5100)
)
val BotanicalPaperDarkPalette = KintsuColors(
    background = Color(0xFF141715),
    surface = Color(0xFF1C211E),
    surfaceElevated = Color(0xFF252B28),
    surfaceTranslucent = Color(0xF2141715),
    border = Color(0xFF38423E),
    textPrimary = Color(0xFFE6ECE8),
    textSecondary = Color(0xFF9CAAA3),
    textTertiary = Color(0xFF7C8B83),
    accent = Color(0xFF7E9F8E),
    accentSoft = Color(0x357E9F8E),
    goldSeam = Color(0xFFD2BC82),
    goldSeamSoft = Color(0x40D2BC82),
    isDark = true,
    accentText = Color(0xFFA5C5B5),
    onAccent = Color(0xFF15261E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFB951)
)

fun getThemePalette(theme: AppTheme, isDark: Boolean): KintsuColors {
    return when (theme) {
        AppTheme.SAGE_IVORY -> if (isDark) SageIvoryDarkPalette else SageIvoryLightPalette
        AppTheme.LAVENDER_MIST -> if (isDark) LavenderMistDarkPalette else LavenderMistLightPalette
        AppTheme.TERRACOTTA_LINEN -> if (isDark) TerracottaLinenDarkPalette else TerracottaLinenLightPalette
        AppTheme.COASTAL_MIST -> if (isDark) CoastalMistDarkPalette else CoastalMistLightPalette
        AppTheme.MIDNIGHT_CHAMPAGNE -> if (isDark) MidnightChampagneDarkPalette else MidnightChampagneLightPalette
        AppTheme.SAKURA_DAWN -> if (isDark) SakuraDawnDarkPalette else SakuraDawnLightPalette
        AppTheme.MATCHA_ATELIER -> if (isDark) MatchaAtelierDarkPalette else MatchaAtelierLightPalette
        AppTheme.OCEAN_PEARL -> if (isDark) OceanPearlDarkPalette else OceanPearlLightPalette
        AppTheme.ROSEWOOD -> if (isDark) RosewoodDarkPalette else RosewoodLightPalette
        AppTheme.NORDIC_FROST -> if (isDark) NordicFrostDarkPalette else NordicFrostLightPalette
        AppTheme.ECLIPSE_GOLD -> if (isDark) EclipseGoldDarkPalette else EclipseGoldLightPalette
        AppTheme.GOLDEN_HOUR -> if (isDark) GoldenHourDarkPalette else GoldenHourLightPalette
        AppTheme.BOTANICAL_PAPER -> if (isDark) BotanicalPaperDarkPalette else BotanicalPaperLightPalette
    }
}

// Backwards-compatible aliases
val LightPalette = SageIvoryLightPalette
val DarkPalette = SageIvoryDarkPalette
