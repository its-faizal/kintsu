package com.kintsu.app.domain.focus

import kotlin.math.max

data class FocusState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isBreak: Boolean = false,
    val durationMinutes: Int = 25,
    val targetEndTimeMillis: Long = 0L,
    val remainingSecondsWhenPaused: Long = 0L,
    val linkedHabitId: Long? = null,
    val linkedTaskId: Long? = null,
    val linkedTaskTitle: String? = null,
    val sessionStartMillis: Long = 0L
)

object FocusTimerManager {

    fun getRemainingSeconds(state: FocusState, nowMillis: Long = System.currentTimeMillis()): Long {
        if (!state.isRunning) {
            return state.durationMinutes * 60L
        }
        if (state.isPaused) {
            return state.remainingSecondsWhenPaused
        }
        val remaining = (state.targetEndTimeMillis - nowMillis) / 1000L
        return max(0L, remaining)
    }

    fun isCompleted(state: FocusState, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!state.isRunning || state.isPaused) return false
        return state.targetEndTimeMillis > 0L && nowMillis >= state.targetEndTimeMillis
    }

    fun start(
        durationMinutes: Int,
        linkedHabitId: Long? = null,
        linkedTaskId: Long? = null,
        linkedTaskTitle: String? = null,
        isBreak: Boolean = false,
        nowMillis: Long = System.currentTimeMillis()
    ): FocusState {
        val totalMillis = durationMinutes * 60 * 1000L
        return FocusState(
            isRunning = true,
            isPaused = false,
            isBreak = isBreak,
            durationMinutes = durationMinutes,
            targetEndTimeMillis = nowMillis + totalMillis,
            remainingSecondsWhenPaused = 0L,
            linkedHabitId = linkedHabitId,
            linkedTaskId = linkedTaskId,
            linkedTaskTitle = linkedTaskTitle,
            sessionStartMillis = nowMillis
        )
    }

    fun pause(state: FocusState, nowMillis: Long = System.currentTimeMillis()): FocusState {
        if (!state.isRunning || state.isPaused) return state
        val remaining = max(0L, (state.targetEndTimeMillis - nowMillis) / 1000L)
        return state.copy(
            isPaused = true,
            remainingSecondsWhenPaused = remaining,
            targetEndTimeMillis = 0L
        )
    }

    fun resume(state: FocusState, nowMillis: Long = System.currentTimeMillis()): FocusState {
        if (!state.isRunning || !state.isPaused) return state
        val newTarget = nowMillis + (state.remainingSecondsWhenPaused * 1000L)
        return state.copy(
            isPaused = false,
            targetEndTimeMillis = newTarget,
            remainingSecondsWhenPaused = 0L
        )
    }

    fun cancel(): FocusState {
        return FocusState()
    }

    fun handleReboot(state: FocusState, nowMillis: Long = System.currentTimeMillis()): FocusState {
        if (!state.isRunning) return state
        // If the session was running before reboot, end cleanly
        return FocusState()
    }
}
