package com.kintsu.app.data

import com.kintsu.app.data.model.ShopItemEntity

object ShopCatalog {

    val CATEGORIES = listOf(
        "Themes"
    )

    fun createCompleteCatalog(): List<ShopItemEntity> {
        val list = mutableListOf<ShopItemEntity>()

        // 1. Sage & Ivory (Default, Free)
        list.add(
            ShopItemEntity(
                id = "theme_sage_ivory",
                itemType = "THEME",
                title = "Sage & Ivory",
                description = "Warm ivory, dusty sage and muted gold. The original Kintsu aesthetic.",
                costCoins = 0,
                requiredStreakDays = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Themes",
                collectionName = "Classic Collection",
                tag = "FREE"
            )
        )

        // 2. Sakura Dawn (Soft Pink & Rose)
        list.add(
            ShopItemEntity(
                id = "theme_sakura_dawn",
                itemType = "THEME",
                title = "Sakura Dawn",
                description = "Soft cherry-blossom pink, porcelain white, pale rose and muted mauve. Airy, gentle and elegant.",
                costCoins = 200,
                requiredStreakDays = 7,
                category = "Themes",
                collectionName = "Sakura Collection",
                tag = "NEW"
            )
        )

        // 3. Matcha Atelier (Japanese Natural Minimalism)
        list.add(
            ShopItemEntity(
                id = "theme_matcha_atelier",
                itemType = "THEME",
                title = "Matcha Atelier",
                description = "Cream, matcha green, muted olive and warm stone. Japanese-inspired natural minimalism.",
                costCoins = 450,
                requiredStreakDays = 14,
                category = "Themes",
                collectionName = "Matcha Collection",
                tag = "POPULAR"
            )
        )

        // 4. Ocean Pearl (Aqua, Slate & Teal)
        list.add(
            ShopItemEntity(
                id = "theme_ocean_pearl",
                itemType = "THEME",
                title = "Ocean Pearl",
                description = "Pale aqua, pearl white, subdued teal and cool slate. Fresh, spacious and peaceful.",
                costCoins = 800,
                requiredStreakDays = 30,
                category = "Themes",
                collectionName = "Ocean Collection",
                tag = "SERENE"
            )
        )

        // 5. Midnight Champagne (Charcoal, Graphite & Champagne Gold)
        list.add(
            ShopItemEntity(
                id = "theme_midnight_champagne",
                itemType = "THEME",
                title = "Midnight Champagne",
                description = "Warm charcoal, graphite, deep obsidian and champagne gold. Bold, dark and opulent.",
                costCoins = 1500,
                requiredStreakDays = 60,
                category = "Themes",
                collectionName = "Nocturne Collection",
                tag = "PRESTIGE"
            )
        )

        return list
    }
}
