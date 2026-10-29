package com.kintsu.app.wellbeing.domain

import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.CommitmentLockEntity

object CommitmentLockManager {

    const val DEFAULT_COOLING_OFF_MILLIS: Long = 24 * 60 * 60 * 1000L // 24 hours

    /**
     * Determines whether a rule modification is "Weakening" or "Stricter".
     * Stricter changes:
     * - Reducing dailyLimitMinutes (e.g. 60 -> 30)
     * - Increasing strictnessLevel (GENTLE -> BALANCED -> STRICT)
     * - Increasing pauseBreathingSeconds
     * - Enabling a disabled rule
     *
     * Weakening changes:
     * - Increasing dailyLimitMinutes (e.g. 30 -> 60)
     * - Lowering strictnessLevel (STRICT -> BALANCED -> GENTLE)
     * - Disabling an enabled rule
     * - Deleting a rule or app group
     * - Removing packages from a blocked group
     */
    fun isWeakeningChange(
        existingRule: BlockRuleEntity,
        newRule: BlockRuleEntity
    ): Boolean {
        // Disabling an enabled rule is weakening
        if (existingRule.isEnabled && !newRule.isEnabled) return true

        // Increasing time limit gives more screen time (weakening)
        if (newRule.dailyLimitMinutes > existingRule.dailyLimitMinutes) return true

        // Lowering strictness hierarchy is weakening
        val existingRank = getStrictnessRank(existingRule.strictnessLevel)
        val newRank = getStrictnessRank(newRule.strictnessLevel)
        if (newRank < existingRank) return true

        // Reducing breathing pause is weakening
        if (newRule.pauseBreathingSeconds < existingRule.pauseBreathingSeconds) return true

        return false
    }

    private fun getStrictnessRank(level: String): Int {
        return when (level.uppercase()) {
            "STRICT" -> 3
            "BALANCED" -> 2
            "GENTLE" -> 1
            else -> 1
        }
    }

    /**
     * Creates a new pending commitment lock with a 24-hour cooling-off period.
     */
    fun createPendingLock(
        targetType: String,
        targetId: Long,
        pendingAction: String,
        payloadJson: String = "",
        requestedAt: Long = System.currentTimeMillis(),
        coolingOffMillis: Long = DEFAULT_COOLING_OFF_MILLIS
    ): CommitmentLockEntity {
        return CommitmentLockEntity(
            targetType = targetType,
            targetId = targetId,
            pendingAction = pendingAction,
            pendingPayloadJson = payloadJson,
            requestedAtMillis = requestedAt,
            unlocksAtMillis = requestedAt + coolingOffMillis,
            isCancelled = false,
            isApplied = false
        )
    }

    /**
     * Checks if a commitment lock has passed its cooling-off period and is ready to apply.
     */
    fun isLockUnlocked(lock: CommitmentLockEntity, currentTimeMillis: Long): Boolean {
        return !lock.isCancelled && !lock.isApplied && currentTimeMillis >= lock.unlocksAtMillis
    }

    /**
     * Formats remaining cooling-off hours and minutes in calm human-readable copy.
     */
    fun getRemainingCoolingOffString(lock: CommitmentLockEntity, currentTimeMillis: Long): String {
        val remMillis = (lock.unlocksAtMillis - currentTimeMillis).coerceAtLeast(0L)
        val hours = remMillis / (60 * 60 * 1000L)
        val minutes = (remMillis % (60 * 60 * 1000L)) / (60 * 1000L)

        return when {
            hours > 0 -> "${hours}h ${minutes}m cooling off"
            minutes > 0 -> "${minutes}m cooling off"
            else -> "Ready to confirm"
        }
    }
}
