package com.kintsu.app.data

import com.kintsu.app.data.model.AchievementEntity
import kotlinx.coroutines.flow.first

class AchievementTracker(private val repository: KintsuRepository) {

    suspend fun seedAchievementsIfNeeded() {
        try {
            repository.seedAchievements(AchievementCatalog.INITIAL_ACHIEVEMENTS)
        } catch (_: Exception) {}
    }

    suspend fun evaluateAchievements(): List<AchievementEntity> {
        seedAchievementsIfNeeded()
        val allAchievements = repository.getAllAchievementsList()
        val newlyEarned = mutableListOf<AchievementEntity>()

        for (ach in allAchievements) {
            if (ach.isEarned) continue

            val conditionMet = try {
                when (ach.id) {
                    "FIRST_HABIT" -> repository.hasAnyHabitLog()
                    "SEVEN_HABITS" -> repository.getTotalHabitLogsCount() >= 7
                    "TEN_TASKS" -> repository.getCompletedTasksCount() >= 10
                    "FIRST_GOAL" -> repository.getCompletedGoalsCount() >= 1
                    "FIRST_FOCUS" -> repository.allFocusSessions.first().isNotEmpty()
                    "FIRST_REVIEW" -> repository.allWeeklyReviews.first().isNotEmpty()
                    "FIRST_THEME" -> repository.getUnlockedThemesCount() >= 1
                    "SEVEN_DAY_STREAK" -> repository.getMaxHabitStreak() >= 7
                    "HEROIC_COMMITMENT" -> repository.isHeroicCommitmentMet()
                    else -> false
                }
            } catch (_: Exception) {
                false
            }

            if (conditionMet) {
                val now = System.currentTimeMillis()
                val updated = ach.copy(
                    isEarned = true,
                    earnedAtMillis = now
                )
                repository.updateAchievement(updated)
                // Idempotently award gold points to user wallet
                repository.awardCoins(
                    amount = ach.pointsAwarded,
                    reason = "Achievement Unlocked: ${ach.title}",
                    sourceActivity = "ACHIEVEMENT",
                    sourceEventId = "ACHIEVEMENT_${ach.id}"
                )
                newlyEarned.add(updated)
            }
        }

        return newlyEarned
    }
}
