package com.kintsu.app.domain

import com.kintsu.app.domain.focus.FocusTimerManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusTimerManagerTest {

    @Test
    fun `derived remaining seconds matches target timestamp`() {
        val start = 1_000_000L
        val state = FocusTimerManager.start(durationMinutes = 25, nowMillis = start)

        assertTrue(state.isRunning)
        assertFalse(state.isPaused)
        assertEquals(25 * 60L, FocusTimerManager.getRemainingSeconds(state, nowMillis = start))

        // Advance 10 minutes (600,000 ms)
        val advanced = start + (10 * 60 * 1000L)
        assertEquals(15 * 60L, FocusTimerManager.getRemainingSeconds(state, nowMillis = advanced))
        assertFalse(FocusTimerManager.isCompleted(state, nowMillis = advanced))

        // Advance 25 minutes (session complete)
        val completeTime = start + (25 * 60 * 1000L)
        assertEquals(0L, FocusTimerManager.getRemainingSeconds(state, nowMillis = completeTime))
        assertTrue(FocusTimerManager.isCompleted(state, nowMillis = completeTime))
    }

    @Test
    fun `pause freezes remaining time across time skips`() {
        val start = 1_000_000L
        val state = FocusTimerManager.start(durationMinutes = 15, nowMillis = start)

        // Advance 5 minutes then pause
        val pauseTime = start + (5 * 60 * 1000L)
        val pausedState = FocusTimerManager.pause(state, nowMillis = pauseTime)

        assertTrue(pausedState.isRunning)
        assertTrue(pausedState.isPaused)
        assertEquals(10 * 60L, pausedState.remainingSecondsWhenPaused)

        // Time passes 2 hours while paused
        val muchLater = pauseTime + (2 * 3600 * 1000L)
        assertEquals(10 * 60L, FocusTimerManager.getRemainingSeconds(pausedState, nowMillis = muchLater))
        assertFalse(FocusTimerManager.isCompleted(pausedState, nowMillis = muchLater))

        // Resume at muchLater
        val resumedState = FocusTimerManager.resume(pausedState, nowMillis = muchLater)
        assertFalse(resumedState.isPaused)
        assertEquals(10 * 60L, FocusTimerManager.getRemainingSeconds(resumedState, nowMillis = muchLater))

        // 10 minutes later it finishes
        val finishedTime = muchLater + (10 * 60 * 1000L)
        assertEquals(0L, FocusTimerManager.getRemainingSeconds(resumedState, nowMillis = finishedTime))
        assertTrue(FocusTimerManager.isCompleted(resumedState, nowMillis = finishedTime))
    }

    @Test
    fun `reboot clears running session cleanly`() {
        val start = 1_000_000L
        val state = FocusTimerManager.start(durationMinutes = 25, nowMillis = start)
        val cleaned = FocusTimerManager.handleReboot(state)
        assertFalse(cleaned.isRunning)
    }

    @Test
    fun `task linking retains taskId and taskTitle across timer lifecycle`() {
        val start = 1_000_000L
        val state = FocusTimerManager.start(
            durationMinutes = 50,
            linkedTaskId = 42L,
            linkedTaskTitle = "Complete Kintsu V2.5 Architecture",
            nowMillis = start
        )

        assertTrue(state.isRunning)
        assertEquals(42L, state.linkedTaskId)
        assertEquals("Complete Kintsu V2.5 Architecture", state.linkedTaskTitle)
        assertEquals(50 * 60L, FocusTimerManager.getRemainingSeconds(state, start))

        val paused = FocusTimerManager.pause(state, nowMillis = start + 60_000L)
        assertTrue(paused.isPaused)
        assertEquals(42L, paused.linkedTaskId)
        assertEquals("Complete Kintsu V2.5 Architecture", paused.linkedTaskTitle)

        val resumed = FocusTimerManager.resume(paused, nowMillis = start + 120_000L)
        assertFalse(resumed.isPaused)
        assertEquals(42L, resumed.linkedTaskId)
        assertEquals("Complete Kintsu V2.5 Architecture", resumed.linkedTaskTitle)
    }

    @Test
    fun `presets 15, 25, 50 minutes calculate remaining seconds accurately`() {
        val start = 2_000_000L
        val shortSession = FocusTimerManager.start(durationMinutes = 15, nowMillis = start)
        assertEquals(15 * 60L, FocusTimerManager.getRemainingSeconds(shortSession, start))

        val standardSession = FocusTimerManager.start(durationMinutes = 25, nowMillis = start)
        assertEquals(25 * 60L, FocusTimerManager.getRemainingSeconds(standardSession, start))

        val deepSession = FocusTimerManager.start(durationMinutes = 50, nowMillis = start)
        assertEquals(50 * 60L, FocusTimerManager.getRemainingSeconds(deepSession, start))
    }
}
