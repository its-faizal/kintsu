package com.kintsu.app.ui.theme

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.kintsu.app.R

data class LauncherIconInfo(
    val id: String,
    val title: String,
    val description: String,
    val aliasName: String,
    val drawableResId: Int,
    val costCoins: Int = 0,
    val isDefault: Boolean = false
)

object AppIconManager {
    val ICONS = listOf(
        // 1. Ceramic Ivory (Default)
        LauncherIconInfo(
            id = "icon_original",
            title = "Ceramic Ivory",
            description = "Circular ceramic ivory stone with hand-laid 24k gold kintsugi crack",
            aliasName = ".MainActivityAliasOriginal",
            drawableResId = R.drawable.ic_kintsu_original,
            costCoins = 0,
            isDefault = true
        ),
        // 2. Obsidian Gold
        LauncherIconInfo(
            id = "icon_obsidian_gold",
            title = "Obsidian Gold",
            description = "Circular midnight volcanic obsidian stone with glowing gold fracture",
            aliasName = ".MainActivityAliasMidnightGold",
            drawableResId = R.drawable.ic_kintsu_midnight_gold,
            costCoins = 100
        ),
        // 3. Carrara Silver
        LauncherIconInfo(
            id = "icon_carrara_silver",
            title = "Carrara Silver",
            description = "Circular pure white Tuscan marble with molten liquid silver seam",
            aliasName = ".MainActivityAliasMinimalInk",
            drawableResId = R.drawable.ic_kintsu_minimal_ink,
            costCoins = 100
        ),
        // 4. Imperial Jade
        LauncherIconInfo(
            id = "icon_imperial_jade",
            title = "Imperial Jade",
            description = "Circular emerald nephrite jade with luminous gold fissure",
            aliasName = ".MainActivityAliasSageEmblem",
            drawableResId = R.drawable.ic_kintsu_sage_emblem,
            costCoins = 120
        ),
        // 5. Lapis Lazuli
        LauncherIconInfo(
            id = "icon_lapis_lazuli",
            title = "Lapis Lazuli",
            description = "Circular celestial cobalt stone with silver seam and gold flecks",
            aliasName = ".MainActivityAliasOceanGlass",
            drawableResId = R.drawable.ic_kintsu_ocean_glass,
            costCoins = 120
        ),
        // 6. Rose Quartz
        LauncherIconInfo(
            id = "icon_rose_quartz",
            title = "Rose Quartz",
            description = "Circular soft crystalline rose quartz with warm champagne gold crack",
            aliasName = ".MainActivityAliasSakuraMark",
            drawableResId = R.drawable.ic_kintsu_sakura_mark,
            costCoins = 120
        ),
        // 7. Terracotta Clay
        LauncherIconInfo(
            id = "icon_terracotta_clay",
            title = "Terracotta Clay",
            description = "Circular rustic pottery clay with rich molten bronze seam",
            aliasName = ".MainActivityAliasTerracottaSeal",
            drawableResId = R.drawable.ic_kintsu_terracotta_seal,
            costCoins = 100
        ),
        // 8. Opal Pearl
        LauncherIconInfo(
            id = "icon_opal_pearl",
            title = "Opal Pearl",
            description = "Circular iridescent mother-of-pearl sheen with brilliant gold fracture",
            aliasName = ".MainActivityAliasPrismaticPearl",
            drawableResId = R.drawable.ic_kintsu_prismatic_pearl,
            costCoins = 150
        ),
        // 9. Celestial Glass
        LauncherIconInfo(
            id = "icon_celestial_glass",
            title = "Celestial Glass",
            description = "Circular ethereal frosted starlight glass with radiant golden crack",
            aliasName = ".MainActivityAliasGoldenSeam",
            drawableResId = R.drawable.ic_kintsu_golden_seam,
            costCoins = 150
        ),
        // 10. Brushed Titanium
        LauncherIconInfo(
            id = "icon_brushed_titanium",
            title = "Brushed Titanium",
            description = "Circular dark industrial titanium alloy with luminous gold seam",
            aliasName = ".MainActivityAliasZenCircle",
            drawableResId = R.drawable.ic_kintsu_zen_circle,
            costCoins = 150
        ),
        // Additional creative variants
        LauncherIconInfo(
            id = "icon_ivory_monogram",
            title = "Ivory Monogram",
            description = "Elegant K monogram with antique gold fracture",
            aliasName = ".MainActivityAliasIvoryMonogram",
            drawableResId = R.drawable.ic_kintsu_ivory_monogram,
            costCoins = 35
        ),
        LauncherIconInfo(
            id = "icon_botanical_crest",
            title = "Botanical Crest",
            description = "Simple natural leaf motif",
            aliasName = ".MainActivityAliasBotanicalCrest",
            drawableResId = R.drawable.ic_kintsu_botanical_crest,
            costCoins = 35
        ),
        LauncherIconInfo(
            id = "icon_superman",
            title = "Man of Steel",
            description = "Heroic diamond shield emblem in crimson and gold",
            aliasName = ".MainActivityAliasSuperman",
            drawableResId = R.drawable.ic_kintsu_superman,
            costCoins = 40
        )
    )

    fun getIconById(id: String): LauncherIconInfo {
        return ICONS.find { it.id == id }
            ?: when (id) {
                "icon_ceramic_ivory" -> ICONS.find { it.id == "icon_original" }
                "icon_obsidian_gold" -> ICONS.find { it.id == "icon_obsidian_gold" }
                "icon_carrara_silver" -> ICONS.find { it.id == "icon_carrara_silver" }
                "icon_imperial_jade" -> ICONS.find { it.id == "icon_imperial_jade" }
                "icon_lapis_lazuli" -> ICONS.find { it.id == "icon_lapis_lazuli" }
                "icon_rose_quartz" -> ICONS.find { it.id == "icon_rose_quartz" }
                "icon_terracotta_clay" -> ICONS.find { it.id == "icon_terracotta_clay" }
                "icon_opal_pearl" -> ICONS.find { it.id == "icon_opal_pearl" }
                "icon_celestial_glass" -> ICONS.find { it.id == "icon_celestial_glass" }
                "icon_brushed_titanium" -> ICONS.find { it.id == "icon_brushed_titanium" }
                else -> null
            }
            ?: ICONS.first()
    }

    fun applyLauncherIcon(context: Context, iconId: String): Boolean {
        val targetIcon = getIconById(iconId)
        val pm = context.packageManager
        val pkg = context.packageName

        return try {
            // First enable the target alias to guarantee a launcher entry always exists
            val targetComponent = ComponentName(pkg, pkg + targetIcon.aliasName)
            pm.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // Disable all other aliases
            ICONS.forEach { icon ->
                if (icon.id != targetIcon.id) {
                    val comp = ComponentName(pkg, pkg + icon.aliasName)
                    pm.setComponentEnabledSetting(
                        comp,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
