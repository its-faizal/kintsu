package com.kintsu.app.coin

import com.kintsu.app.data.model.HabitLogEntity
import java.time.LocalDate

enum class FireStreakTier(
    val dayRange: IntRange,
    val multiplier: Float,
    val title: String,
    val rankBadge: String,
    val description: String,
    val rekindleCostCoins: Int
) {
    NONE(
        dayRange = 0..0,
        multiplier = 1.0f,
        title = "Ashes",
        rankBadge = "灰",
        description = "A dormant spark waiting to be kindled",
        rekindleCostCoins = 0
    ),
    SPARK(
        dayRange = 1..6,
        multiplier = 1.0f,
        title = "Spark",
        rankBadge = "始",
        description = "The first flame of mindful discipline",
        rekindleCostCoins = 25
    ),
    EMBER(
        dayRange = 7..13,
        multiplier = 1.1f,
        title = "Ember",
        rankBadge = "炭",
        description = "One week of unbroken heat (+10% coins)",
        rekindleCostCoins = 35
    ),
    GOLDEN_FLAME(
        dayRange = 14..29,
        multiplier = 1.25f,
        title = "Golden Flame",
        rankBadge = "焔",
        description = "Two weeks of steady resolve (+25% coins)",
        rekindleCostCoins = 50
    ),
    SACRED_BLAZE(
        dayRange = 30..59,
        multiplier = 1.5f,
        title = "Sacred Blaze",
        rankBadge = "熾",
        description = "One month of pure attention (+50% coins)",
        rekindleCostCoins = 75
    ),
    PHOENIX(
        dayRange = 60..89,
        multiplier = 2.0f,
        title = "Eternal Phoenix",
        rankBadge = "鳳",
        description = "60-Day Milestone: True Double Coins (2.0x Max)!",
        rekindleCostCoins = 100
    ),
    SOLAR_FLARE(
        dayRange = 90..99,
        multiplier = 2.0f,
        title = "Solar Flare",
        rankBadge = "日",
        description = "90-Day Quarter-Year Prestige (2.0x Capped)",
        rekindleCostCoins = 125
    ),
    CENTURION(
        dayRange = 100..119,
        multiplier = 2.0f,
        title = "Centurion Flame",
        rankBadge = "百",
        description = "100-Day Centurion Mastery (2.0x Capped)",
        rekindleCostCoins = 150
    ),
    DIVINE_SUN(
        dayRange = 120..149,
        multiplier = 2.0f,
        title = "Divine Sun",
        rankBadge = "神",
        description = "120-Day Sacred Pillar of Will (2.0x Capped)",
        rekindleCostCoins = 200
    ),
    TRANSCENDENT(
        dayRange = 150..Int.MAX_VALUE,
        multiplier = 2.0f,
        title = "Transcendent Lotus",
        rankBadge = "蓮",
        description = "150-Day Apex Enlightenment (2.0x Capped)",
        rekindleCostCoins = 250
    );

    companion object {
        fun fromDays(days: Int): FireStreakTier {
            if (days <= 0) return NONE
            return values().firstOrNull { days in it.dayRange } ?: TRANSCENDENT
        }
    }
}

object FireStreakEngine {

    /**
     * Calculates the current continuous active streak in days across all completed habit logs.
     */
    fun calculateCurrentStreak(habitLogs: List<HabitLogEntity>, todayEpochDay: Long = LocalDate.now().toEpochDay()): Int {
        if (habitLogs.isEmpty()) return 0

        // Collect all distinct epochDays where at least one habit was completed or golden-repaired
        val completedDays = habitLogs
            .filter { it.isCompleted || it.isRepairedGold }
            .map { it.epochDay }
            .toSet()

        if (completedDays.isEmpty()) return 0

        var currentDay = todayEpochDay
        var streak = 0

        // If today isn't completed yet, check if yesterday was completed to keep the streak alive
        if (!completedDays.contains(currentDay)) {
            currentDay -= 1
            if (!completedDays.contains(currentDay)) {
                return 0
            }
        }

        while (completedDays.contains(currentDay)) {
            streak++
            currentDay--
        }

        return streak
    }

    /**
     * Resolves the active FireStreakTier for a given streak count.
     */
    fun getTier(streakDays: Int): FireStreakTier = FireStreakTier.fromDays(streakDays)

    /**
     * Multiplies the base coin reward (1, 2, or 3) by the active streak tier.
     * Guaranteed minimum equal to baseAmount. Maximum cap is strictly 2.0x.
     */
    fun applyMultiplier(baseAmount: Int, tier: FireStreakTier): Int {
        if (baseAmount <= 0) return 0
        val multiplied = Math.round(baseAmount * tier.multiplier)
        return maxOf(baseAmount, multiplied)
    }

    /**
     * Checks if a user's current streak qualifies for a shop item with a streak rank barrier.
     */
    fun isEligibleForShopItem(currentStreakDays: Int, requiredStreakDays: Int): Boolean {
        return currentStreakDays >= requiredStreakDays
    }
}
