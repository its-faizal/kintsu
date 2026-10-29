package com.kintsu.app.ui

import com.kintsu.app.ui.components.KintsuNavTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationBackStackTest {

    @Test
    fun `initial tab is home and pop returns false`() {
        val nav = NavigationBackStack()
        assertEquals(KintsuNavTab.HOME, nav.currentTab)
        assertEquals(listOf(KintsuNavTab.HOME), nav.stack.value)
        assertFalse(nav.pop())
        assertEquals(KintsuNavTab.HOME, nav.currentTab)
    }

    @Test
    fun `switching tabs preserves back stack hierarchy`() {
        val nav = NavigationBackStack()

        // Home -> Habits
        nav.selectTab(KintsuNavTab.HABITS)
        assertEquals(KintsuNavTab.HABITS, nav.currentTab)
        assertEquals(listOf(KintsuNavTab.HOME, KintsuNavTab.HABITS), nav.stack.value)

        // Habits -> Tasks
        nav.selectTab(KintsuNavTab.TASKS)
        assertEquals(KintsuNavTab.TASKS, nav.currentTab)
        assertEquals(listOf(KintsuNavTab.HOME, KintsuNavTab.HABITS, KintsuNavTab.TASKS), nav.stack.value)

        // Tasks -> Goals
        nav.selectTab(KintsuNavTab.GOALS)
        assertEquals(KintsuNavTab.GOALS, nav.currentTab)
        assertEquals(
            listOf(KintsuNavTab.HOME, KintsuNavTab.HABITS, KintsuNavTab.TASKS, KintsuNavTab.GOALS),
            nav.stack.value
        )

        // Pop back: Goals -> Tasks
        assertTrue(nav.pop())
        assertEquals(KintsuNavTab.TASKS, nav.currentTab)

        // Pop back: Tasks -> Habits
        assertTrue(nav.pop())
        assertEquals(KintsuNavTab.HABITS, nav.currentTab)

        // Pop back: Habits -> Home
        assertTrue(nav.pop())
        assertEquals(KintsuNavTab.HOME, nav.currentTab)

        // Pop at Home -> false (exit app)
        assertFalse(nav.pop())
        assertEquals(KintsuNavTab.HOME, nav.currentTab)
    }

    @Test
    fun `selecting current tab is a no-op`() {
        val nav = NavigationBackStack()
        nav.selectTab(KintsuNavTab.TASKS)
        assertEquals(listOf(KintsuNavTab.HOME, KintsuNavTab.TASKS), nav.stack.value)

        nav.selectTab(KintsuNavTab.TASKS)
        assertEquals(listOf(KintsuNavTab.HOME, KintsuNavTab.TASKS), nav.stack.value)
    }

    @Test
    fun `selecting existing tab moves it to top without duplicate`() {
        val nav = NavigationBackStack()
        nav.selectTab(KintsuNavTab.TASKS)
        nav.selectTab(KintsuNavTab.GOALS)
        assertEquals(listOf(KintsuNavTab.HOME, KintsuNavTab.TASKS, KintsuNavTab.GOALS), nav.stack.value)

        // Re-selecting Tasks moves it to top
        nav.selectTab(KintsuNavTab.TASKS)
        assertEquals(listOf(KintsuNavTab.HOME, KintsuNavTab.GOALS, KintsuNavTab.TASKS), nav.stack.value)
        assertEquals(KintsuNavTab.TASKS, nav.currentTab)
    }

    @Test
    fun `selecting home tab collapses stack to root`() {
        val nav = NavigationBackStack()
        nav.selectTab(KintsuNavTab.HABITS)
        nav.selectTab(KintsuNavTab.TASKS)
        nav.selectTab(KintsuNavTab.GOALS)
        nav.selectTab(KintsuNavTab.PROGRESS)

        // Tapping Home returns directly to Home and clears intermediate stack
        nav.selectTab(KintsuNavTab.HOME)
        assertEquals(listOf(KintsuNavTab.HOME), nav.stack.value)
        assertEquals(KintsuNavTab.HOME, nav.currentTab)
        assertFalse(nav.pop())
    }
}
