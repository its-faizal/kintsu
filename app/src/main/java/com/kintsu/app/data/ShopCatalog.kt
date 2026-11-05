package com.kintsu.app.data

import com.kintsu.app.data.model.ShopItemEntity

object ShopCatalog {

    val CATEGORIES = listOf(
        "Themes",
        "Icons",
        "Packs"
    )

    fun createCompleteCatalog(): List<ShopItemEntity> {
        val list = mutableListOf<ShopItemEntity>()

        // ==========================================
        // 1. KINTSUGI STYLE THEMES (10 Themes)
        // ==========================================

        // 1. Ceramic Ivory (Default, Free)
        list.add(
            ShopItemEntity(
                id = "theme_sage_ivory",
                itemType = "THEME",
                title = "Ceramic Ivory",
                description = "Warm ivory stone, dusty sage accents and antique gold fracture. The foundational Kintsu aesthetic.",
                costCoins = 0,
                requiredStreakDays = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Themes",
                collectionName = "Artisan Material Collection",
                tag = "FREE"
            )
        )

        // 2. Obsidian Gold
        list.add(
            ShopItemEntity(
                id = "theme_obsidian_gold",
                itemType = "THEME",
                title = "Obsidian Gold",
                description = "Midnight volcanic stone, warm graphite and radiant 24k gold fissure. Bold and luminous.",
                costCoins = 250,
                requiredStreakDays = 3,
                category = "Themes",
                bundleId = "bundle_kintsugi_themes",
                collectionName = "Artisan Material Collection",
                tag = "POPULAR"
            )
        )

        // 3. Carrara Silver
        list.add(
            ShopItemEntity(
                id = "theme_carrara_silver",
                itemType = "THEME",
                title = "Carrara Silver",
                description = "Pure white Tuscan marble, cool slate grey and molten liquid silver seam. Clean and serene.",
                costCoins = 250,
                requiredStreakDays = 3,
                category = "Themes",
                bundleId = "bundle_kintsugi_themes",
                collectionName = "Artisan Material Collection",
                tag = "SERENE"
            )
        )

        // 4. Imperial Jade
        list.add(
            ShopItemEntity(
                id = "theme_imperial_jade",
                itemType = "THEME",
                title = "Imperial Jade",
                description = "Deep translucent green nephrite jade with luminous gold fracture. Regal Japanese garden harmony.",
                costCoins = 300,
                requiredStreakDays = 7,
                category = "Themes",
                bundleId = "bundle_kintsugi_themes",
                collectionName = "Artisan Material Collection",
                tag = "PRESTIGE"
            )
        )

        // 5. Lapis Lazuli
        list.add(
            ShopItemEntity(
                id = "theme_lapis_lazuli",
                itemType = "THEME",
                title = "Lapis Lazuli",
                description = "Deep celestial ultramarine cobalt stone with liquid silver crack and gold flecks.",
                costCoins = 300,
                requiredStreakDays = 7,
                category = "Themes",
                bundleId = "bundle_kintsugi_themes",
                collectionName = "Artisan Material Collection",
                tag = "MAJESTIC"
            )
        )

        // 6. Rose Quartz
        list.add(
            ShopItemEntity(
                id = "theme_rose_quartz",
                itemType = "THEME",
                title = "Rose Quartz",
                description = "Soft crystalline rose quartz, delicate blush pink, and warm champagne gold fracture.",
                costCoins = 300,
                requiredStreakDays = 7,
                category = "Themes",
                bundleId = "bundle_kintsugi_themes",
                collectionName = "Artisan Material Collection",
                tag = "ELEGANT"
            )
        )

        // 7. Terracotta Clay
        list.add(
            ShopItemEntity(
                id = "theme_terracotta_clay",
                itemType = "THEME",
                title = "Terracotta Clay",
                description = "Warm sun-baked pottery clay, earthen sienna, and molten bronze kintsugi seam.",
                costCoins = 250,
                requiredStreakDays = 3,
                category = "Themes",
                bundleId = "bundle_kintsugi_themes",
                collectionName = "Artisan Material Collection",
                tag = "EARTHEN"
            )
        )

        // 8. Opal Pearl
        list.add(
            ShopItemEntity(
                id = "theme_opal_pearl",
                itemType = "THEME",
                title = "Opal Pearl",
                description = "Iridescent mother-of-pearl sheen, prismatic violet and cyan reflections with liquid gold seam.",
                costCoins = 350,
                requiredStreakDays = 14,
                category = "Themes",
                bundleId = "bundle_kintsugi_themes",
                collectionName = "Artisan Material Collection",
                tag = "IRIDESCENT"
            )
        )

        // 9. Celestial Glass
        list.add(
            ShopItemEntity(
                id = "theme_celestial_glass",
                itemType = "THEME",
                title = "Celestial Glass",
                description = "Ethereal frosted starlight mist glass, soft aurora blue, and glowing gold fracture.",
                costCoins = 350,
                requiredStreakDays = 14,
                category = "Themes",
                bundleId = "bundle_kintsugi_themes",
                collectionName = "Artisan Material Collection",
                tag = "ETHEREAL"
            )
        )

        // 10. Brushed Titanium
        list.add(
            ShopItemEntity(
                id = "theme_brushed_titanium",
                itemType = "THEME",
                title = "Brushed Titanium",
                description = "Industrial dark titanium alloy, carbon graphite, and radiant molten gold crack.",
                costCoins = 350,
                requiredStreakDays = 14,
                category = "Themes",
                bundleId = "bundle_kintsugi_themes",
                collectionName = "Artisan Material Collection",
                tag = "MODERN"
            )
        )

        // ==========================================
        // 2. CIRCULAR EMBLEM ICONS (10 Icons)
        // ==========================================

        // 1. Ceramic Ivory Emblem (Default Free)
        list.add(
            ShopItemEntity(
                id = "icon_original",
                itemType = "APP_ICON",
                title = "Ceramic Ivory Emblem",
                description = "Circular ceramic ivory stone with hand-laid 24k gold kintsugi crack.",
                costCoins = 0,
                requiredStreakDays = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Icons",
                collectionName = "Circular Emblem Collection",
                tag = "DEFAULT"
            )
        )

        // 2. Obsidian Gold Emblem
        list.add(
            ShopItemEntity(
                id = "icon_obsidian_gold",
                itemType = "APP_ICON",
                title = "Obsidian Gold Emblem",
                description = "Circular midnight volcanic obsidian stone with glowing gold fracture.",
                costCoins = 100,
                requiredStreakDays = 0,
                category = "Icons",
                bundleId = "bundle_kintsugi_emblems",
                collectionName = "Circular Emblem Collection",
                tag = "POPULAR"
            )
        )

        // 3. Carrara Silver Emblem
        list.add(
            ShopItemEntity(
                id = "icon_carrara_silver",
                itemType = "APP_ICON",
                title = "Carrara Silver Emblem",
                description = "Circular pure white Tuscan marble with molten liquid silver seam.",
                costCoins = 100,
                requiredStreakDays = 0,
                category = "Icons",
                bundleId = "bundle_kintsugi_emblems",
                collectionName = "Circular Emblem Collection",
                tag = "SILVER"
            )
        )

        // 4. Imperial Jade Emblem
        list.add(
            ShopItemEntity(
                id = "icon_imperial_jade",
                itemType = "APP_ICON",
                title = "Imperial Jade Emblem",
                description = "Circular emerald nephrite jade with luminous gold fissure.",
                costCoins = 120,
                requiredStreakDays = 0,
                category = "Icons",
                bundleId = "bundle_kintsugi_emblems",
                collectionName = "Circular Emblem Collection",
                tag = "JADE"
            )
        )

        // 5. Lapis Lazuli Emblem
        list.add(
            ShopItemEntity(
                id = "icon_lapis_lazuli",
                itemType = "APP_ICON",
                title = "Lapis Lazuli Emblem",
                description = "Circular celestial cobalt stone with silver seam and gold flecks.",
                costCoins = 120,
                requiredStreakDays = 0,
                category = "Icons",
                bundleId = "bundle_kintsugi_emblems",
                collectionName = "Circular Emblem Collection",
                tag = "ROYAL"
            )
        )

        // 6. Rose Quartz Emblem
        list.add(
            ShopItemEntity(
                id = "icon_rose_quartz",
                itemType = "APP_ICON",
                title = "Rose Quartz Emblem",
                description = "Circular soft crystalline rose quartz with warm champagne gold crack.",
                costCoins = 120,
                requiredStreakDays = 0,
                category = "Icons",
                bundleId = "bundle_kintsugi_emblems",
                collectionName = "Circular Emblem Collection",
                tag = "CRYSTAL"
            )
        )

        // 7. Terracotta Clay Emblem
        list.add(
            ShopItemEntity(
                id = "icon_terracotta_clay",
                itemType = "APP_ICON",
                title = "Terracotta Clay Emblem",
                description = "Circular rustic pottery clay with rich molten bronze seam.",
                costCoins = 100,
                requiredStreakDays = 0,
                category = "Icons",
                bundleId = "bundle_kintsugi_emblems",
                collectionName = "Circular Emblem Collection",
                tag = "EARTH"
            )
        )

        // 8. Opal Pearl Emblem
        list.add(
            ShopItemEntity(
                id = "icon_opal_pearl",
                itemType = "APP_ICON",
                title = "Opal Pearl Emblem",
                description = "Circular iridescent mother-of-pearl sheen with brilliant gold fracture.",
                costCoins = 150,
                requiredStreakDays = 0,
                category = "Icons",
                bundleId = "bundle_kintsugi_emblems",
                collectionName = "Circular Emblem Collection",
                tag = "PRISM"
            )
        )

        // 9. Celestial Glass Emblem
        list.add(
            ShopItemEntity(
                id = "icon_celestial_glass",
                itemType = "APP_ICON",
                title = "Celestial Glass Emblem",
                description = "Circular ethereal frosted starlight glass with radiant golden crack.",
                costCoins = 150,
                requiredStreakDays = 0,
                category = "Icons",
                bundleId = "bundle_kintsugi_emblems",
                collectionName = "Circular Emblem Collection",
                tag = "STARS"
            )
        )

        // 10. Brushed Titanium Emblem
        list.add(
            ShopItemEntity(
                id = "icon_brushed_titanium",
                itemType = "APP_ICON",
                title = "Brushed Titanium Emblem",
                description = "Circular dark industrial titanium alloy with luminous gold seam.",
                costCoins = 150,
                requiredStreakDays = 0,
                category = "Icons",
                bundleId = "bundle_kintsugi_emblems",
                collectionName = "Circular Emblem Collection",
                tag = "ALLOY"
            )
        )

        // ==========================================
        // 3. PACKS & BUNDLES (3 Packs)
        // ==========================================

        // 1. The Artisan 9-Emblem Pack
        list.add(
            ShopItemEntity(
                id = "bundle_kintsugi_emblems_pack",
                itemType = "BUNDLE",
                title = "Artisan 9-Emblem Pack",
                description = "Unlock all 9 circular stone & metallic kintsugi emblem icons at once: Obsidian, Jade, Lapis, Rose Quartz, Terracotta, Opal, Celestial Glass, Titanium and Carrara Marble.",
                costCoins = 550,
                requiredStreakDays = 0,
                category = "Packs",
                bundleId = "bundle_kintsugi_emblems",
                collectionName = "Artisan Heritage Bundles",
                tag = "45% OFF"
            )
        )

        // 2. Master Kintsugi Theme Pack
        list.add(
            ShopItemEntity(
                id = "bundle_kintsugi_themes_pack",
                itemType = "BUNDLE",
                title = "Master Theme Collection Pack",
                description = "Unlock all 9 authentic material themes: Obsidian Gold, Carrara Silver, Imperial Jade, Lapis Lazuli, Rose Quartz, Terracotta, Opal, Celestial Glass, and Titanium.",
                costCoins = 1200,
                requiredStreakDays = 0,
                category = "Packs",
                bundleId = "bundle_kintsugi_themes",
                collectionName = "Artisan Heritage Bundles",
                tag = "55% OFF"
            )
        )

        // 3. All-in-One Master Artisan Heritage Pack
        list.add(
            ShopItemEntity(
                id = "bundle_master_artisan_complete_pack",
                itemType = "BUNDLE",
                title = "Artisan Heritage Complete Pack",
                description = "The ultimate collection. Unlocks all 9 circular emblem icons AND all 9 matching style themes in a single prestige acquisition.",
                costCoins = 1500,
                requiredStreakDays = 0,
                category = "Packs",
                bundleId = "bundle_kintsugi_all",
                collectionName = "Artisan Heritage Bundles",
                tag = "PRESTIGE ALL-IN-ONE"
            )
        )

        return list
    }
}
