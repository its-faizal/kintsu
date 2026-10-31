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
        LauncherIconInfo(
            id = "icon_original",
            title = "Kintsu Original",
            description = "Official Kintsu gold fracture emblem",
            aliasName = ".MainActivityAliasOriginal",
            drawableResId = R.drawable.ic_kintsu_original,
            costCoins = 0,
            isDefault = true
        ),
        LauncherIconInfo(
            id = "icon_golden_seam",
            title = "Golden Seam",
            description = "Minimal gold fracture motif",
            aliasName = ".MainActivityAliasGoldenSeam",
            drawableResId = R.drawable.ic_kintsu_golden_seam,
            costCoins = 30
        ),
        LauncherIconInfo(
            id = "icon_sage_emblem",
            title = "Sage Emblem",
            description = "Muted green geometric symbol",
            aliasName = ".MainActivityAliasSageEmblem",
            drawableResId = R.drawable.ic_kintsu_sage_emblem,
            costCoins = 30
        ),
        LauncherIconInfo(
            id = "icon_ivory_monogram",
            title = "Ivory Monogram",
            description = "Elegant K monogram with antique gold fracture",
            aliasName = ".MainActivityAliasIvoryMonogram",
            drawableResId = R.drawable.ic_kintsu_ivory_monogram,
            costCoins = 35
        ),
        LauncherIconInfo(
            id = "icon_midnight_gold",
            title = "Midnight Gold",
            description = "Charcoal and champagne mark",
            aliasName = ".MainActivityAliasMidnightGold",
            drawableResId = R.drawable.ic_kintsu_midnight_gold,
            costCoins = 40
        ),
        LauncherIconInfo(
            id = "icon_sakura_mark",
            title = "Sakura Mark",
            description = "Subtle pink botanical icon",
            aliasName = ".MainActivityAliasSakuraMark",
            drawableResId = R.drawable.ic_kintsu_sakura_mark,
            costCoins = 40
        ),
        LauncherIconInfo(
            id = "icon_terracotta_seal",
            title = "Terracotta Seal",
            description = "Warm clay emblem seal",
            aliasName = ".MainActivityAliasTerracottaSeal",
            drawableResId = R.drawable.ic_kintsu_terracotta_seal,
            costCoins = 35
        ),
        LauncherIconInfo(
            id = "icon_ocean_glass",
            title = "Ocean Glass",
            description = "Misty teal abstract mark",
            aliasName = ".MainActivityAliasOceanGlass",
            drawableResId = R.drawable.ic_kintsu_ocean_glass,
            costCoins = 40
        ),
        LauncherIconInfo(
            id = "icon_minimal_ink",
            title = "Minimal Ink",
            description = "Monochrome line symbol",
            aliasName = ".MainActivityAliasMinimalInk",
            drawableResId = R.drawable.ic_kintsu_minimal_ink,
            costCoins = 30
        ),
        LauncherIconInfo(
            id = "icon_zen_circle",
            title = "Zen Circle",
            description = "Restrained circular brush-inspired design",
            aliasName = ".MainActivityAliasZenCircle",
            drawableResId = R.drawable.ic_kintsu_zen_circle,
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
            id = "icon_prismatic_pearl",
            title = "Prismatic Pearl",
            description = "Soft layered geometric icon",
            aliasName = ".MainActivityAliasPrismaticPearl",
            drawableResId = R.drawable.ic_kintsu_prismatic_pearl,
            costCoins = 45
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
        return ICONS.find { it.id == id } ?: ICONS.first()
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
