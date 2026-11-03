package com.kintsu.app.data

import com.kintsu.app.data.model.ShopItemEntity

object ShopCatalog {

    val CATEGORIES = listOf(
        "Discover",
        "Themes",
        "App Icons",
        "Widgets",
        "Backgrounds",
        "Focus",
        "Habits",
        "Profile",
        "Sounds",
        "Gold Repair",
    )

    fun createCompleteCatalog(): List<ShopItemEntity> {
        val list = mutableListOf<ShopItemEntity>()

        // 1. THEMES (Preserve existing + 8 new themes)
        list.add(
            ShopItemEntity(
                id = "theme_sage_ivory",
                itemType = "THEME",
                title = "Sage & Ivory",
                description = "Warm ivory, dusty sage and muted gold. The original Kintsu aesthetic.",
                costCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Themes",
                collectionName = "Classic Collection",
                tag = "FREE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_sakura_dawn",
                itemType = "THEME",
                title = "Sakura Dawn",
                description = "Soft cherry-blossom pink, porcelain white, pale rose and muted mauve. Airy, gentle and elegant.",
                costCoins = 50,
                category = "Themes",
                bundleId = "bundle_sakura",
                collectionName = "Sakura Collection",
                tag = "NEW"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_matcha_atelier",
                itemType = "THEME",
                title = "Matcha Atelier",
                description = "Cream, matcha green, muted olive and warm stone. Japanese-inspired natural minimalism.",
                costCoins = 50,
                category = "Themes",
                bundleId = "bundle_matcha",
                collectionName = "Matcha Collection",
                tag = "FEATURED"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_ocean_pearl",
                itemType = "THEME",
                title = "Ocean Pearl",
                description = "Pale aqua, pearl white, subdued teal and cool slate. Fresh, spacious and peaceful.",
                costCoins = 50,
                category = "Themes",
                collectionName = "Ocean Collection",
                tag = "NEW"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_rosewood",
                itemType = "THEME",
                title = "Rosewood",
                description = "Warm brown, dusty rose, blush beige and antique gold. Sophisticated, warm and understated.",
                costCoins = 50,
                category = "Themes",
                collectionName = "Rosewood Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_nordic_frost",
                itemType = "THEME",
                title = "Nordic Frost",
                description = "Cool ivory, fog grey, muted blue and soft silver. Clean, calm and architectural.",
                costCoins = 50,
                category = "Themes",
                bundleId = "bundle_nordic",
                collectionName = "Nordic Collection",
                tag = "NEW"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_eclipse_gold",
                itemType = "THEME",
                title = "Eclipse Gold",
                description = "Deep graphite, warm charcoal, muted bronze and restrained champagne. Elegant and premium.",
                costCoins = 60,
                category = "Themes",
                bundleId = "bundle_midnight",
                collectionName = "Midnight Collection",
                tag = "FEATURED"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_golden_hour",
                itemType = "THEME",
                title = "Golden Hour",
                description = "Sandstone, pale peach, creamy ivory and muted amber. Warm, relaxed and atmospheric.",
                costCoins = 50,
                category = "Themes",
                bundleId = "bundle_golden_hour",
                collectionName = "Golden Hour Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_botanical_paper",
                itemType = "THEME",
                title = "Botanical Paper",
                description = "Soft paper white, eucalyptus, pale moss and gentle ink tones. Organic and refined.",
                costCoins = 50,
                category = "Themes",
                collectionName = "Botanical Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_metropolis_hero",
                itemType = "THEME",
                title = "Metropolis Hero",
                description = "Midnight navy, crystalline ice, crimson cape and sunburst gold. Heroic resolve and unbreakable fortitude.",
                costCoins = 60,
                category = "Themes",
                bundleId = "bundle_superman",
                collectionName = "Metropolis Hero Collection",
                tag = "HERO"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_apple_cupertino",
                itemType = "THEME",
                title = "Apple Cupertino",
                description = "Pure frosted glass, San Francisco slate and California blue. Fluid, interruptible spring interface.",
                costCoins = 60,
                category = "Themes",
                bundleId = "bundle_apple",
                collectionName = "Cupertino Glass Collection",
                tag = "FLUID"
            )
        )
        list.add(
            ShopItemEntity(
                id = "theme_spotify_pulse",
                itemType = "THEME",
                title = "Spotify Pulse",
                description = "Pitch dark obsidian with electric pulse green. Bold typography, high energy and rhythmic focus.",
                costCoins = 60,
                category = "Themes",
                bundleId = "bundle_spotify",
                collectionName = "Spotify Pulse Collection",
                tag = "PULSE"
            )
        )

        // 2. APP ICONS (12 launcher icons)
        list.add(
            ShopItemEntity(
                id = "icon_original",
                itemType = "APP_ICON",
                title = "Kintsu Original",
                description = "Official Kintsu gold fracture emblem over rich charcoal.",
                costCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "App Icons",
                tag = "FREE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_golden_seam",
                itemType = "APP_ICON",
                title = "Golden Seam",
                description = "Minimal gold fracture line with warm deep background.",
                costCoins = 30,
                category = "App Icons"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_sage_emblem",
                itemType = "APP_ICON",
                title = "Sage Emblem",
                description = "Muted green geometric origami symbol.",
                costCoins = 30,
                category = "App Icons",
                bundleId = "bundle_matcha",
                collectionName = "Matcha Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_ivory_monogram",
                itemType = "APP_ICON",
                title = "Ivory Monogram",
                description = "Elegant serif K monogram with antique gold kintsugi crack.",
                costCoins = 35,
                category = "App Icons"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_midnight_gold",
                itemType = "APP_ICON",
                title = "Midnight Gold",
                description = "Charcoal and champagne crosshair crest.",
                costCoins = 40,
                category = "App Icons",
                bundleId = "bundle_midnight",
                collectionName = "Midnight Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_sakura_mark",
                itemType = "APP_ICON",
                title = "Sakura Mark",
                description = "Subtle pink botanical flower icon with golden core.",
                costCoins = 40,
                category = "App Icons",
                bundleId = "bundle_sakura",
                collectionName = "Sakura Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_terracotta_seal",
                itemType = "APP_ICON",
                title = "Terracotta Seal",
                description = "Japanese hanko-inspired clay seal stamp.",
                costCoins = 35,
                category = "App Icons"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_ocean_glass",
                itemType = "APP_ICON",
                title = "Ocean Glass",
                description = "Misty teal abstract hexagonal jewel.",
                costCoins = 40,
                category = "App Icons"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_minimal_ink",
                itemType = "APP_ICON",
                title = "Minimal Ink",
                description = "Monochrome sumi line symbol on pure white.",
                costCoins = 30,
                category = "App Icons",
                bundleId = "bundle_nordic",
                collectionName = "Nordic Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_zen_circle",
                itemType = "APP_ICON",
                title = "Zen Circle",
                description = "Restrained circular brush-inspired enso motif.",
                costCoins = 35,
                category = "App Icons"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_botanical_crest",
                itemType = "APP_ICON",
                title = "Botanical Crest",
                description = "Eucalyptus sprig with delicate leaves in sage and gold.",
                costCoins = 35,
                category = "App Icons"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_prismatic_pearl",
                itemType = "APP_ICON",
                title = "Prismatic Pearl",
                description = "Soft layered geometric crystal facets.",
                costCoins = 45,
                category = "App Icons"
            )
        )
        list.add(
            ShopItemEntity(
                id = "icon_superman",
                itemType = "APP_ICON",
                title = "Man of Steel",
                description = "Heroic diamond shield emblem in crimson and gold.",
                costCoins = 40,
                category = "App Icons",
                bundleId = "bundle_superman",
                collectionName = "Metropolis Hero Collection"
            )
        )

        // 3. WIDGET SKINS (8 skins)
        list.add(
            ShopItemEntity(
                id = "widget_frosted_pearl",
                itemType = "WIDGET_SKIN",
                title = "Frosted Pearl Skin",
                description = "Translucent frosted card treatment with pale silver borders.",
                costCoins = 25,
                category = "Widgets"
            )
        )
        list.add(
            ShopItemEntity(
                id = "widget_warm_linen",
                itemType = "WIDGET_SKIN",
                title = "Warm Linen Skin",
                description = "Organic linen texture background with warm amber progress accents.",
                costCoins = 25,
                category = "Widgets"
            )
        )
        list.add(
            ShopItemEntity(
                id = "widget_botanical_sage",
                itemType = "WIDGET_SKIN",
                title = "Botanical Sage Skin",
                description = "Muted eucalyptus card surface with sage and olive indicators.",
                costCoins = 25,
                category = "Widgets",
                bundleId = "bundle_matcha",
                collectionName = "Matcha Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "widget_rose_glass",
                itemType = "WIDGET_SKIN",
                title = "Rose Glass Skin",
                description = "Soft rosewood and blush glass treatment with subtle glow.",
                costCoins = 30,
                category = "Widgets"
            )
        )
        list.add(
            ShopItemEntity(
                id = "widget_midnight_bronze",
                itemType = "WIDGET_SKIN",
                title = "Midnight Bronze Skin",
                description = "Deep obsidian surface with brushed bronze and champagne accents.",
                costCoins = 30,
                category = "Widgets",
                bundleId = "bundle_midnight",
                collectionName = "Midnight Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "widget_minimal_paper",
                itemType = "WIDGET_SKIN",
                title = "Minimal Paper Skin",
                description = "Monochrome high-contrast Japanese washi paper styling.",
                costCoins = 20,
                category = "Widgets",
                bundleId = "bundle_nordic",
                collectionName = "Nordic Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "widget_ocean_mist",
                itemType = "WIDGET_SKIN",
                title = "Ocean Mist Skin",
                description = "Soft teal gradient with clean minimalist white markers.",
                costCoins = 25,
                category = "Widgets"
            )
        )
        list.add(
            ShopItemEntity(
                id = "widget_gold_vein",
                itemType = "WIDGET_SKIN",
                title = "Gold Vein Skin",
                description = "Signature kintsugi gold fracture pattern running across widget edges.",
                costCoins = 35,
                category = "Widgets",
                tag = "SIGNATURE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "widget_superman_hero",
                itemType = "WIDGET_SKIN",
                title = "Metropolis Fortress Skin",
                description = "Midnight crystalline card with gold shield seams and crimson indicators.",
                costCoins = 40,
                category = "Widgets",
                bundleId = "bundle_superman",
                collectionName = "Metropolis Hero Collection"
            )
        )

        // 4. BACKGROUNDS (16 original artworks)
        val natureBgs = listOf(
            "bg_misty_mountains" to "Misty Mountains",
            "bg_quiet_lake" to "Quiet Lake",
            "bg_soft_forest" to "Soft Forest",
            "bg_morning_meadow" to "Morning Meadow"
        )
        natureBgs.forEach { (id, title) ->
            list.add(
                ShopItemEntity(
                    id = id,
                    itemType = "BACKGROUND",
                    title = title,
                    description = "Serene nature landscape for header, profile and focus timer.",
                    costCoins = 20,
                    category = "Backgrounds"
                )
            )
        }

        val botanicalBgs = listOf(
            "bg_eucalyptus_branches" to "Eucalyptus Branches",
            "bg_sakura_petals" to "Sakura Petals",
            "bg_minimal_leaves" to "Minimal Leaves",
            "bg_watercolour_flowers" to "Watercolour Flowers"
        )
        botanicalBgs.forEach { (id, title) ->
            list.add(
                ShopItemEntity(
                    id = id,
                    itemType = "BACKGROUND",
                    title = title,
                    description = "Botanical organic pattern with soothing color balance.",
                    costCoins = 25,
                    category = "Backgrounds",
                    collectionName = if (id.contains("sakura")) "Sakura Collection" else "Botanical Collection"
                )
            )
        }

        val abstractBgs = listOf(
            "bg_paper_texture" to "Washi Paper Texture",
            "bg_pastel_fields" to "Blurred Pastel Fields",
            "bg_geometric_forms" to "Soft Geometric Forms",
            "bg_glass_layers" to "Gentle Glass Layers"
        )
        abstractBgs.forEach { (id, title) ->
            list.add(
                ShopItemEntity(
                    id = id,
                    itemType = "BACKGROUND",
                    title = title,
                    description = "Minimalist abstract texture that preserves text legibility.",
                    costCoins = 20,
                    category = "Backgrounds"
                )
            )
        }

        val atmosphericBgs = listOf(
            "bg_golden_hour_sky" to "Golden Hour Sky",
            "bg_midnight_stars" to "Midnight Stars",
            "bg_rainy_window" to "Rainy Window",
            "bg_coastal_haze" to "Soft Coastal Haze"
        )
        atmosphericBgs.forEach { (id, title) ->
            list.add(
                ShopItemEntity(
                    id = id,
                    itemType = "BACKGROUND",
                    title = title,
                    description = "Atmospheric gradient and subtle light effects.",
                    costCoins = 25,
                    category = "Backgrounds",
                    collectionName = if (id.contains("golden")) "Golden Hour Collection" else ""
                )
            )
        }

        // 5. FOCUS STUDIO (Timer Faces, Scenes, Effects)
        list.add(
            ShopItemEntity(
                id = "focus_face_classic_ring",
                itemType = "FOCUS_FACE",
                title = "Classic Ring",
                description = "Clean circular countdown ring with elapsed interval markers.",
                costCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Focus",
                tag = "FREE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_face_golden_arc",
                itemType = "FOCUS_FACE",
                title = "Golden Arc",
                description = "Sweeping golden fracture arc reflecting session completion.",
                costCoins = 30,
                category = "Focus"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_face_minimal_digital",
                itemType = "FOCUS_FACE",
                title = "Minimal Digital",
                description = "Large, typography-driven numeric timer with zero distraction.",
                costCoins = 25,
                category = "Focus",
                bundleId = "bundle_nordic",
                collectionName = "Nordic Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_face_zen_circle",
                itemType = "FOCUS_FACE",
                title = "Zen Circle",
                description = "Hand-drawn brush enso circle that closes upon completion.",
                costCoins = 35,
                category = "Focus"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_face_botanical_progress",
                itemType = "FOCUS_FACE",
                title = "Botanical Progress",
                description = "A vine that sprouts delicate leaves as minutes elapse.",
                costCoins = 35,
                category = "Focus"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_face_soft_hourglass",
                itemType = "FOCUS_FACE",
                title = "Soft Hourglass",
                description = "Modern stylized sand glass with gentle sand particles.",
                costCoins = 30,
                category = "Focus"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_face_eclipse_dial",
                itemType = "FOCUS_FACE",
                title = "Eclipse Dial",
                description = "Celestial moon disc gradually revealing champagne gold glow.",
                costCoins = 40,
                category = "Focus",
                bundleId = "bundle_midnight",
                collectionName = "Midnight Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_face_flowing_line",
                itemType = "FOCUS_FACE",
                title = "Flowing Line",
                description = "Gentle sine wave undulating at the pace of mindful breath.",
                costCoins = 30,
                category = "Focus"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_face_hero_shield",
                itemType = "FOCUS_FACE",
                title = "Fortress Shield",
                description = "Concentric hero timer dial with crimson countdown ticks and sunburst gold ring.",
                costCoins = 35,
                category = "Focus",
                bundleId = "bundle_superman",
                collectionName = "Metropolis Hero Collection"
            )
        )

        // Focus Scenes
        list.add(
            ShopItemEntity(
                id = "focus_scene_quiet_garden",
                itemType = "FOCUS_SCENE",
                title = "Quiet Garden Scene",
                description = "Japanese zen rock and bamboo garden ambient backdrop.",
                costCoins = 25,
                category = "Focus",
                bundleId = "bundle_matcha",
                collectionName = "Matcha Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_scene_rainy_afternoon",
                itemType = "FOCUS_SCENE",
                title = "Rainy Afternoon Scene",
                description = "Rain drops sliding down a cosy windowpane.",
                costCoins = 25,
                category = "Focus"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_scene_mountain_retreat",
                itemType = "FOCUS_SCENE",
                title = "Mountain Retreat Scene",
                description = "Peaceful alpine mist and morning sun rays.",
                costCoins = 25,
                category = "Focus"
            )
        )
        list.add(
            ShopItemEntity(
                id = "focus_scene_deep_night",
                itemType = "FOCUS_SCENE",
                title = "Deep Night Scene",
                description = "Starlit sky and tranquil crescent moon.",
                costCoins = 25,
                category = "Focus"
            )
        )

        // 6. HABIT COSMETICS (Completion Markers & Progress Rings)
        list.add(
            ShopItemEntity(
                id = "habit_marker_classic_check",
                itemType = "HABIT_STYLE",
                title = "Classic Check",
                description = "Clean minimalist checkmark icon upon completion.",
                costCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Habits",
                tag = "FREE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "habit_marker_soft_gold_dot",
                itemType = "HABIT_STYLE",
                title = "Soft Gold Dot",
                description = "Radiant golden pearl marker upon completion.",
                costCoins = 20,
                category = "Habits"
            )
        )
        list.add(
            ShopItemEntity(
                id = "habit_marker_filled_circle",
                itemType = "HABIT_STYLE",
                title = "Filled Circle",
                description = "Solid tactile pebble fill for completed rituals.",
                costCoins = 15,
                category = "Habits"
            )
        )
        list.add(
            ShopItemEntity(
                id = "habit_marker_gold_seam",
                itemType = "HABIT_STYLE",
                title = "Gold Seam Mark",
                description = "Kintsugi gold crack fracture stamping each victory.",
                costCoins = 30,
                category = "Habits",
                tag = "SIGNATURE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "habit_ring_minimal",
                itemType = "HABIT_STYLE",
                title = "Minimal Circular Ring",
                description = "Understated slim progress ring around habit icons.",
                costCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Habits",
                tag = "FREE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "habit_ring_fine_gold",
                itemType = "HABIT_STYLE",
                title = "Fine Gold Ring",
                description = "Luminous metallic gold progress ring.",
                costCoins = 25,
                category = "Habits",
                bundleId = "bundle_golden_hour",
                collectionName = "Golden Hour Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "habit_ring_soft_gradient",
                itemType = "HABIT_STYLE",
                title = "Soft Gradient Ring",
                description = "Two-tone harmonic hue gradient ring.",
                costCoins = 25,
                category = "Habits",
                bundleId = "bundle_sakura",
                collectionName = "Sakura Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "habit_ring_botanical",
                itemType = "HABIT_STYLE",
                title = "Botanical Wreath Ring",
                description = "Delicate leaf wreath filling around ritual targets.",
                costCoins = 30,
                category = "Habits"
            )
        )

        // 7. PROFILE STUDIO (Frames, Avatars, Titles)
        list.add(
            ShopItemEntity(
                id = "frame_classic_thin",
                itemType = "PROFILE_FRAME",
                title = "Classic Thin Frame",
                description = "Minimalist single-pixel subtle border frame.",
                costCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Profile",
                tag = "FREE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "frame_soft_gold",
                itemType = "PROFILE_FRAME",
                title = "Soft Gold Frame",
                description = "Warm champagne gold bevel border with rounded corners.",
                costCoins = 25,
                category = "Profile",
                bundleId = "bundle_golden_hour",
                collectionName = "Golden Hour Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "frame_sage_circle",
                itemType = "PROFILE_FRAME",
                title = "Sage Ceramic Frame",
                description = "Earthy celadon ceramic ring texture.",
                costCoins = 25,
                category = "Profile",
                bundleId = "bundle_matcha",
                collectionName = "Matcha Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "frame_pearl_edge",
                itemType = "PROFILE_FRAME",
                title = "Pearl Edge Frame",
                description = "Luminous iridescence with delicate pearl white border.",
                costCoins = 30,
                category = "Profile",
                bundleId = "bundle_sakura",
                collectionName = "Sakura Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "frame_rosewood",
                itemType = "PROFILE_FRAME",
                title = "Rosewood Inlay Frame",
                description = "Polished warm wood grain frame with gold corner brackets.",
                costCoins = 25,
                category = "Profile"
            )
        )
        list.add(
            ShopItemEntity(
                id = "frame_eclipse",
                itemType = "PROFILE_FRAME",
                title = "Eclipse Frame",
                description = "Graphite dark ring with subtle glowing corona.",
                costCoins = 30,
                category = "Profile",
                bundleId = "bundle_midnight",
                collectionName = "Midnight Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "frame_botanical",
                itemType = "PROFILE_FRAME",
                title = "Botanical Vine Frame",
                description = "Intertwined leafy branch ring.",
                costCoins = 30,
                category = "Profile"
            )
        )

        // Profile Titles
        val titles = listOf(
            "title_steady_steps" to "Steady Steps",
            "title_quiet_focus" to "Quiet Focus",
            "title_thoughtful_planner" to "Thoughtful Planner",
            "title_daily_explorer" to "Daily Explorer",
            "title_growing_mind" to "Growing Mind",
            "title_intentional_living" to "Intentional Living"
        )
        titles.forEach { (id, title) ->
            list.add(
                ShopItemEntity(
                    id = id,
                    itemType = "PROFILE_TITLE",
                    title = "Title: $title",
                    description = "Cosmetic profile title badge displaying your mindful philosophy.",
                    costCoins = 15,
                    category = "Profile"
                )
            )
        }

        // 8. SOUNDS (Habit and Focus audio chimes)
        list.add(
            ShopItemEntity(
                id = "sound_soft_chime",
                itemType = "SOUNDSCAPE",
                title = "Soft Chime",
                description = "Harmonic major triad acoustic chime for habit completion.",
                costCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Sounds",
                tag = "FREE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "sound_gentle_bell",
                itemType = "SOUNDSCAPE",
                title = "Gentle Bell",
                description = "Pure acoustic meditation bell with rich overtones.",
                costCoins = 20,
                category = "Sounds"
            )
        )
        list.add(
            ShopItemEntity(
                id = "sound_wooden_tap",
                itemType = "SOUNDSCAPE",
                title = "Wooden Tap",
                description = "Crisp, organic woodblock tactile tap.",
                costCoins = 20,
                category = "Sounds"
            )
        )
        list.add(
            ShopItemEntity(
                id = "sound_quiet_sparkle",
                itemType = "SOUNDSCAPE",
                title = "Quiet Sparkle",
                description = "Ascending harp arpeggio marking joyful accomplishments.",
                costCoins = 25,
                category = "Sounds"
            )
        )
        list.add(
            ShopItemEntity(
                id = "sound_warm_resonance",
                itemType = "SOUNDSCAPE",
                title = "Warm Resonance",
                description = "Deep Tibetan singing bowl resonance with 2.5Hz calming binaural beat.",
                costCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Sounds",
                tag = "FREE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "sound_soft_piano",
                itemType = "SOUNDSCAPE",
                title = "Soft Piano Tone",
                description = "Warm acoustic grand piano note resonating into silence.",
                costCoins = 25,
                category = "Sounds"
            )
        )

        // 9. GOLD REPAIR COLLECTION (8 styles)
        list.add(
            ShopItemEntity(
                id = "repair_classic_gold",
                itemType = "GOLD_REPAIR_STYLE",
                title = "Classic Gold Seam",
                description = "Traditional 24k lacquer kintsugi repair for missed ritual days.",
                costCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                category = "Gold Repair",
                tag = "FREE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "repair_fine_kintsugi",
                itemType = "GOLD_REPAIR_STYLE",
                title = "Fine Kintsugi Fracture",
                description = "Delicate spiderweb gold filigree tracing the cracks of time.",
                costCoins = 35,
                category = "Gold Repair"
            )
        )
        list.add(
            ShopItemEntity(
                id = "repair_rose_gold",
                itemType = "GOLD_REPAIR_STYLE",
                title = "Rose Gold Repair",
                description = "Blush copper and rose gold seam with soft warm shine.",
                costCoins = 35,
                category = "Gold Repair"
            )
        )
        list.add(
            ShopItemEntity(
                id = "repair_antique_bronze",
                itemType = "GOLD_REPAIR_STYLE",
                title = "Antique Bronze Seam",
                description = "Weathered bronze patina repair with historic wabi-sabi character.",
                costCoins = 30,
                category = "Gold Repair"
            )
        )
        list.add(
            ShopItemEntity(
                id = "repair_pearl_repair",
                itemType = "GOLD_REPAIR_STYLE",
                title = "Pearl & Mica Seam",
                description = "Iridescent nacre and powdered pearl repair.",
                costCoins = 40,
                category = "Gold Repair"
            )
        )
        list.add(
            ShopItemEntity(
                id = "repair_botanical_gold",
                itemType = "GOLD_REPAIR_STYLE",
                title = "Botanical Gold Tendril",
                description = "Living ivy gold tendril sealing habit fracture lines.",
                costCoins = 40,
                category = "Gold Repair"
            )
        )
        list.add(
            ShopItemEntity(
                id = "repair_midnight_gold",
                itemType = "GOLD_REPAIR_STYLE",
                title = "Midnight Gold Repair",
                description = "Deep charcoal lacquer embedded with brilliant gold leaf dust.",
                costCoins = 45,
                category = "Gold Repair",
                bundleId = "bundle_midnight",
                collectionName = "Midnight Collection"
            )
        )
        list.add(
            ShopItemEntity(
                id = "repair_minimal_trace",
                itemType = "GOLD_REPAIR_STYLE",
                title = "Minimal Gold Trace",
                description = "Understated single hairline fracture in burnished gold.",
                costCoins = 30,
                category = "Gold Repair"
            )
        )

        // 10. COLLECTIONS & BUNDLES (5 bundled packs with deductions)
        list.add(
            ShopItemEntity(
                id = "bundle_matcha",
                itemType = "BUNDLE",
                title = "Matcha Atelier Bundle",
                description = "Includes Matcha Theme, Sage App Icon, Botanical Widget Skin, Quiet Garden Focus Scene, and Sage Frame. 35% bundle savings.",
                costCoins = 110,
                category = "Collections",
                bundleId = "bundle_matcha",
                collectionName = "Matcha Collection",
                tag = "BUNDLE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "bundle_sakura",
                itemType = "BUNDLE",
                title = "Sakura Dawn Bundle",
                description = "Includes Sakura Dawn Theme, Sakura Mark App Icon, Soft Gradient Ring, Sakura Petals Background, and Pearl Edge Frame. 35% bundle savings.",
                costCoins = 120,
                category = "Collections",
                bundleId = "bundle_sakura",
                collectionName = "Sakura Collection",
                tag = "BUNDLE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "bundle_midnight",
                itemType = "BUNDLE",
                title = "Midnight Sanctuary Bundle",
                description = "Includes Eclipse Gold Theme, Midnight Gold Icon, Midnight Bronze Skin, Eclipse Focus Dial, and Midnight Gold Repair. 40% bundle savings.",
                costCoins = 135,
                category = "Collections",
                bundleId = "bundle_midnight",
                collectionName = "Midnight Collection",
                tag = "BUNDLE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "bundle_golden_hour",
                itemType = "BUNDLE",
                title = "Golden Hour Bundle",
                description = "Includes Golden Hour Theme, Golden Hour Sky, Fine Gold Habit Ring, and Soft Gold Frame. 35% bundle savings.",
                costCoins = 115,
                category = "Collections",
                bundleId = "bundle_golden_hour",
                collectionName = "Golden Hour Collection",
                tag = "BUNDLE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "bundle_nordic",
                itemType = "BUNDLE",
                title = "Nordic Frost Bundle",
                description = "Includes Nordic Frost Theme, Minimal Ink Icon, Minimal Paper Skin, and Minimal Digital Timer. 35% bundle savings.",
                costCoins = 110,
                category = "Collections",
                bundleId = "bundle_nordic",
                collectionName = "Nordic Collection",
                tag = "BUNDLE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "bundle_superman",
                itemType = "BUNDLE",
                title = "Metropolis Hero Collection",
                description = "Includes Metropolis Hero Theme, Man of Steel Icon, Fortress Widget Skin, and Fortress Shield Timer Face. 40% hero savings.",
                costCoins = 120,
                category = "Collections",
                bundleId = "bundle_superman",
                collectionName = "Metropolis Hero Collection",
                tag = "BUNDLE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "bundle_apple",
                itemType = "BUNDLE",
                title = "Cupertino Glass Collection",
                description = "Includes Apple Cupertino Theme, Frosted Pearl Skin, Flowing Line Timer Face, and California Blue Accent. 40% bundle savings.",
                costCoins = 120,
                category = "Collections",
                bundleId = "bundle_apple",
                collectionName = "Cupertino Glass Collection",
                tag = "BUNDLE"
            )
        )
        list.add(
            ShopItemEntity(
                id = "bundle_spotify",
                itemType = "BUNDLE",
                title = "Spotify Pulse Collection",
                description = "Includes Spotify Pulse Theme, Midnight Bronze Skin, Minimal Digital Timer Face, and Electric Green Accent. 40% bundle savings.",
                costCoins = 120,
                category = "Collections",
                bundleId = "bundle_spotify",
                collectionName = "Spotify Pulse Collection",
                tag = "BUNDLE"
            )
        )

        return list.map { it.copy(costCoins = 0, isUnlocked = true, tag = "FREE") }
    }
}
