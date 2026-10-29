package com.kintsu.app.domain.task

import com.kintsu.app.data.model.TaskRecurrenceType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TaskRecurrenceTest {

    @Test
    fun testNeverRecurrenceReturnsCurrent() {
        val today = LocalDate.of(2026, 10, 29).toEpochDay()
        val next = TaskRecurrenceCalculator.computeNextDueDate(today, TaskRecurrenceType.NEVER)
        assertEquals(today, next)
    }

    @Test
    fun testDailyRecurrenceAdvancesByOneDay() {
        val date = LocalDate.of(2026, 10, 29).toEpochDay()
        val next = TaskRecurrenceCalculator.computeNextDueDate(date, TaskRecurrenceType.DAILY)
        val expected = LocalDate.of(2026, 10, 30).toEpochDay()
        assertEquals(expected, next)
    }

    @Test
    fun testWeekdaysRecurrenceSkipsWeekend() {
        // Friday Oct 30, 2026
        val friday = LocalDate.of(2026, 10, 30).toEpochDay()
        // Mask for Mon-Fri: bits 0..4 set (1 + 2 + 4 + 8 + 16 = 31)
        val monFriMask = 31
        val next = TaskRecurrenceCalculator.computeNextDueDate(friday, TaskRecurrenceType.WEEKDAYS, monFriMask)
        // Next weekday is Monday Nov 2, 2026
        val monday = LocalDate.of(2026, 11, 2).toEpochDay()
        assertEquals(monday, next)
    }

    @Test
    fun testWeeklyRecurrenceAdvancesBySevenDays() {
        val date = LocalDate.of(2026, 10, 29).toEpochDay()
        val next = TaskRecurrenceCalculator.computeNextDueDate(date, TaskRecurrenceType.WEEKLY)
        val expected = LocalDate.of(2026, 11, 5).toEpochDay()
        assertEquals(expected, next)
    }

    @Test
    fun testMonthlyRecurrenceHandlesMonthEnds() {
        // Jan 31, 2026 -> plus 1 month -> Feb 28, 2026 (non-leap year)
        val jan31 = LocalDate.of(2026, 1, 31).toEpochDay()
        val nextFromJan31 = TaskRecurrenceCalculator.computeNextDueDate(jan31, TaskRecurrenceType.MONTHLY)
        val expectedFeb28 = LocalDate.of(2026, 2, 28).toEpochDay()
        assertEquals(expectedFeb28, nextFromJan31)

        // Mar 31, 2026 -> plus 1 month -> Apr 30, 2026
        val mar31 = LocalDate.of(2026, 3, 31).toEpochDay()
        val nextFromMar31 = TaskRecurrenceCalculator.computeNextDueDate(mar31, TaskRecurrenceType.MONTHLY)
        val expectedApr30 = LocalDate.of(2026, 4, 30).toEpochDay()
        assertEquals(expectedApr30, nextFromMar31)
    }

    @Test
    fun testMonthlyRecurrenceHandlesLeapYear() {
        // Jan 31, 2028 (leap year) -> plus 1 month -> Feb 29, 2028
        val jan31Leap = LocalDate.of(2028, 1, 31).toEpochDay()
        val nextLeap = TaskRecurrenceCalculator.computeNextDueDate(jan31Leap, TaskRecurrenceType.MONTHLY)
        val expectedFeb29 = LocalDate.of(2028, 2, 29).toEpochDay()
        assertEquals(expectedFeb29, nextLeap)
    }
}
