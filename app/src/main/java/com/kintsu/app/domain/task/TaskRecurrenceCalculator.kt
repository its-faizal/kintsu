package com.kintsu.app.domain.task

import com.kintsu.app.data.model.TaskRecurrenceType
import java.time.LocalDate
import kotlin.math.max

object TaskRecurrenceCalculator {

    /**
     * Calculates the next due epoch day for a recurring task.
     * Accurately handles month-ends, leap years, time zone independence, and selected weekday bitmasks.
     */
    fun calculateNextDueDate(
        currentDueDate: Long?,
        recurrenceType: TaskRecurrenceType,
        weekdaysMask: Int,
        referenceEpochDay: Long = LocalDate.now().toEpochDay()
    ): Long {
        val baseDate = currentDueDate ?: referenceEpochDay
        val baseLocalDate = LocalDate.ofEpochDay(baseDate)

        return when (recurrenceType) {
            TaskRecurrenceType.NEVER -> baseDate
            TaskRecurrenceType.DAILY -> baseDate + 1L
            TaskRecurrenceType.WEEKDAYS -> {
                val effectiveMask = if (weekdaysMask == 0) 31 else weekdaysMask // Default to Mon-Fri
                var candidate = baseDate + 1L
                var found = false
                // Look ahead up to 14 days
                for (i in 0 until 14) {
                    val dow = LocalDate.ofEpochDay(candidate).dayOfWeek.value // 1 = Monday .. 7 = Sunday
                    val bit = 1 shl (dow - 1)
                    if ((effectiveMask and bit) != 0) {
                        found = true
                        break
                    }
                    candidate++
                }
                if (found) candidate else baseDate + 1L
            }
            TaskRecurrenceType.WEEKLY -> {
                baseLocalDate.plusWeeks(1).toEpochDay()
            }
            TaskRecurrenceType.MONTHLY -> {
                baseLocalDate.plusMonths(1).toEpochDay()
            }
        }
    }

    fun computeNextDueDate(
        currentEpochDay: Long,
        recurrenceType: TaskRecurrenceType,
        weekdaysMask: Int = 0,
        referenceEpochDay: Long = LocalDate.now().toEpochDay()
    ): Long = calculateNextDueDate(currentEpochDay, recurrenceType, weekdaysMask, referenceEpochDay)
}
