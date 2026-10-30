package com.kintsu.app.data

import com.kintsu.app.data.model.AchievementEntity

object AchievementCatalog {
    val INITIAL_ACHIEVEMENTS = listOf(
        AchievementEntity(
            id = "FIRST_HABIT",
            title = "First Step Taken",
            description = "Completed your first habit ritual",
            pointsAwarded = 10,
            badgeIcon = "🌱",
            category = "MILESTONE"
        ),
        AchievementEntity(
            id = "FIRST_GOAL",
            title = "North Star Aligned",
            description = "Achieved your first long-term objective",
            pointsAwarded = 25,
            badgeIcon = "🌟",
            category = "MILESTONE"
        ),
        AchievementEntity(
            id = "FIRST_FOCUS",
            title = "Deep Sanctuary",
            description = "Completed your first mindful focus session",
            pointsAwarded = 10,
            badgeIcon = "⏳",
            category = "FOCUS"
        ),
        AchievementEntity(
            id = "SEVEN_HABITS",
            title = "Unbroken Rhythm",
            description = "Logged seven scheduled habit ritual completions",
            pointsAwarded = 20,
            badgeIcon = "🌿",
            category = "CONSISTENCY"
        ),
        AchievementEntity(
            id = "FIRST_REVIEW",
            title = "Mirror of Reflection",
            description = "Completed your first weekly retrospective review",
            pointsAwarded = 15,
            badgeIcon = "🪞",
            category = "REFLECTION"
        ),
        AchievementEntity(
            id = "TEN_TASKS",
            title = "Clarity of Action",
            description = "Completed ten intentional focus tasks",
            pointsAwarded = 15,
            badgeIcon = "🎯",
            category = "PRODUCTIVITY"
        ),
        AchievementEntity(
            id = "FIRST_THEME",
            title = "Atelier Aesthetic",
            description = "Unlocked your first custom Kintsu cosmetic theme",
            pointsAwarded = 15,
            badgeIcon = "🎨",
            category = "PERSONALISATION"
        ),
        AchievementEntity(
            id = "SEVEN_DAY_STREAK",
            title = "Kintsugi Gold Seam",
            description = "Maintained a 7-day unbroken habit streak",
            pointsAwarded = 25,
            badgeIcon = "✨",
            category = "CONSISTENCY"
        )
    )
}
