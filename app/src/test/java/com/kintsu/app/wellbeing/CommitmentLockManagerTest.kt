package com.kintsu.app.wellbeing

import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.wellbeing.domain.CommitmentLockManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommitmentLockManagerTest {

    @Test
    fun testWeakeningChangeDetected() {
        val baseRule = BlockRuleEntity(
            id = 1,
            appGroupId = 10,
            isEnabled = true,
            mode = "DAILY_LIMIT",
            dailyLimitMinutes = 30,
            strictnessLevel = "STRICT",
            pauseBreathingSeconds = 15
        )

        // 1. Increasing limit from 30m to 60m is weakening
        val weakerLimit = baseRule.copy(dailyLimitMinutes = 60)
        assertTrue(CommitmentLockManager.isWeakeningChange(baseRule, weakerLimit))

        // 2. Decreasing limit from 30m to 15m is stricter (not weakening)
        val stricterLimit = baseRule.copy(dailyLimitMinutes = 15)
        assertFalse(CommitmentLockManager.isWeakeningChange(baseRule, stricterLimit))

        // 3. Lowering strictness from STRICT to GENTLE is weakening
        val weakerStrictness = baseRule.copy(strictnessLevel = "GENTLE")
        assertTrue(CommitmentLockManager.isWeakeningChange(baseRule, weakerStrictness))

        // 4. Disabling rule is weakening
        val disabledRule = baseRule.copy(isEnabled = false)
        assertTrue(CommitmentLockManager.isWeakeningChange(baseRule, disabledRule))
    }

    @Test
    fun testPendingLockUnlocksAfterCoolingOff() {
        val now = 1_000_000_000L
        val lock = CommitmentLockManager.createPendingLock(
            targetType = "RULE",
            targetId = 1,
            pendingAction = "WEAKEN",
            requestedAt = now,
            coolingOffMillis = 86_400_000L // 24h
        )

        // Before 24h -> Locked
        assertFalse(CommitmentLockManager.isLockUnlocked(lock, now + 10_000L))
        assertFalse(CommitmentLockManager.isLockUnlocked(lock, now + 86_399_999L))

        // Exactly at or after 24h -> Unlocked
        assertTrue(CommitmentLockManager.isLockUnlocked(lock, now + 86_400_000L))
        assertTrue(CommitmentLockManager.isLockUnlocked(lock, now + 90_000_000L))
    }
}
