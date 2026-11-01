package com.kintsu.app.data

import com.kintsu.app.data.model.AchievementEntity
import com.kintsu.app.data.model.StylePresetEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementTrackerTest {

    @Test
    fun testAchievementCatalogInitialDefinition() {
        val initialAchievements = AchievementCatalog.INITIAL_ACHIEVEMENTS
        assertTrue("Expected at least 8 initial achievements", initialAchievements.size >= 8)

        val uniqueIds = initialAchievements.map { it.id }.toSet()
        assertEquals("Achievement IDs must be strictly unique", initialAchievements.size, uniqueIds.size)

        initialAchievements.forEach { ach ->
            assertTrue("Title must not be blank for ${ach.id}", ach.title.isNotBlank())
            assertTrue("Description must not be blank for ${ach.id}", ach.description.isNotBlank())
            assertTrue("Points must be positive for ${ach.id}", ach.pointsAwarded > 0)
            assertTrue("Badge icon must not be blank for ${ach.id}", ach.badgeIcon.isNotBlank())
            assertFalse("Initial achievement must start locked", ach.isEarned)
        }
    }

    @Test
    fun testCuratedStylePresetsIntegrity() {
        val curated = listOf(
            StylePresetEntity(name = "Calm Morning", themeName = "Sage & Ivory", accentName = "SAGE", iconId = "icon_original", widgetSkinId = "widget_botanical_sage", focusFaceId = "focus_face_classic_ring"),
            StylePresetEntity(name = "Matcha Minimal", themeName = "Matcha Atelier", accentName = "SAGE", iconId = "icon_sage_emblem", widgetSkinId = "widget_botanical_sage", focusFaceId = "focus_face_zen_circle"),
            StylePresetEntity(name = "Midnight Focus", themeName = "Eclipse Gold", accentName = "GOLD", iconId = "icon_midnight_gold", widgetSkinId = "widget_midnight_bronze", focusFaceId = "focus_face_eclipse_dial"),
            StylePresetEntity(name = "Warm Studio", themeName = "Rosewood", accentName = "CLAY", iconId = "icon_terracotta_seal", widgetSkinId = "widget_rose_glass", focusFaceId = "focus_face_soft_hourglass"),
            StylePresetEntity(name = "Sakura Quiet", themeName = "Sakura Dawn", accentName = "LAVENDER", iconId = "icon_sakura_mark", widgetSkinId = "widget_frosted_pearl", focusFaceId = "focus_face_flowing_line"),
            StylePresetEntity(name = "Pure Monochrome", themeName = "Nordic Frost", accentName = "STONE", iconId = "icon_minimal_ink", widgetSkinId = "widget_minimal_paper", focusFaceId = "focus_face_minimal_digital"),
            StylePresetEntity(name = "Metropolis Hero", themeName = "Metropolis Hero", accentName = "CRIMSON", iconId = "icon_superman", widgetSkinId = "widget_superman_hero", focusFaceId = "focus_face_hero_shield"),
            StylePresetEntity(name = "Cupertino Fluid", themeName = "Apple Cupertino", accentName = "BLUE", iconId = "icon_prismatic_pearl", widgetSkinId = "widget_frosted_pearl", focusFaceId = "focus_face_flowing_line"),
            StylePresetEntity(name = "Spotify Pulse", themeName = "Spotify Pulse", accentName = "NEON_GREEN", iconId = "icon_midnight_gold", widgetSkinId = "widget_midnight_bronze", focusFaceId = "focus_face_minimal_digital")
        )

        assertEquals("Expected exactly 9 curated designer style presets", 9, curated.size)

        curated.forEach { preset ->
            assertTrue("Preset name must not be blank", preset.name.isNotBlank())
            assertTrue("Theme name must not be blank", preset.themeName.isNotBlank())
            assertTrue("Accent name must not be blank", preset.accentName.isNotBlank())
            assertTrue("Launcher icon must not be blank", preset.iconId.isNotBlank())
            assertTrue("Widget skin must not be blank", preset.widgetSkinId.isNotBlank())
            assertTrue("Focus dial face must not be blank", preset.focusFaceId.isNotBlank())
        }
    }

    @Test
    fun testAchievementIdempotencyKeys() {
        val achievements = AchievementCatalog.INITIAL_ACHIEVEMENTS
        val sourceEventIds = achievements.map { "ACHIEVEMENT_${it.id}" }
        val uniqueSourceIds = sourceEventIds.toSet()

        assertEquals("Every achievement must map to a unique idempotent wallet sourceEventId", achievements.size, uniqueSourceIds.size)
    }
}
