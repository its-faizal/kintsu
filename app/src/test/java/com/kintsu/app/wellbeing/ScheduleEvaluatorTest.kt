package com.kintsu.app.wellbeing

import com.kintsu.app.data.model.BlockScheduleEntity
import com.kintsu.app.wellbeing.domain.ScheduleEvaluator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ScheduleEvaluatorTest {

    @Test
    fun testSameDayWindowActive() {
        val schedule = BlockScheduleEntity(
            ruleId = 1,
            startMinuteOfDay = 9 * 60, // 09:00
            endMinuteOfDay = 17 * 60,  // 17:00
            daysOfWeekMask = 127,       // Mon-Sun
            isEnabled = true
        )

        // Inside window at 12:30
        val active = ScheduleEvaluator.isScheduleActive(schedule, LocalTime.of(12, 30), DayOfWeek.WEDNESDAY)
        assertTrue(active)

        // Outside window at 08:59
        val early = ScheduleEvaluator.isScheduleActive(schedule, LocalTime.of(8, 59), DayOfWeek.WEDNESDAY)
        assertFalse(early)

        // Outside window at 17:01
        val late = ScheduleEvaluator.isScheduleActive(schedule, LocalTime.of(17, 1), DayOfWeek.WEDNESDAY)
        assertFalse(late)
    }

    @Test
    fun testOvernightWindowActive() {
        val schedule = BlockScheduleEntity(
            ruleId = 1,
            startMinuteOfDay = 22 * 60, // 22:00
            endMinuteOfDay = 7 * 60,   // 07:00
            daysOfWeekMask = ScheduleEvaluator.createWeekdayMask(listOf(DayOfWeek.FRIDAY)),
            isEnabled = true
        )

        // Friday night at 23:15 -> Active
        assertTrue(ScheduleEvaluator.isScheduleActive(schedule, LocalTime.of(23, 15), DayOfWeek.FRIDAY))

        // Saturday early morning at 05:30 -> Active (started Friday night)
        assertTrue(ScheduleEvaluator.isScheduleActive(schedule, LocalTime.of(5, 30), DayOfWeek.SATURDAY))

        // Saturday morning at 07:30 -> Inactive (window ended at 07:00)
        assertFalse(ScheduleEvaluator.isScheduleActive(schedule, LocalTime.of(7, 30), DayOfWeek.SATURDAY))

        // Friday afternoon at 15:00 -> Inactive
        assertFalse(ScheduleEvaluator.isScheduleActive(schedule, LocalTime.of(15, 0), DayOfWeek.FRIDAY))
    }

    @Test
    fun testDisabledScheduleIsNeverActive() {
        val schedule = BlockScheduleEntity(
            ruleId = 1,
            startMinuteOfDay = 0,
            endMinuteOfDay = 24 * 60 - 1,
            daysOfWeekMask = 127,
            isEnabled = false
        )
        assertFalse(ScheduleEvaluator.isScheduleActive(schedule, LocalTime.NOON, DayOfWeek.MONDAY))
    }
}
