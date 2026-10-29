package com.kintsu.app.wellbeing.domain

import com.kintsu.app.data.model.BlockScheduleEntity
import java.time.DayOfWeek
import java.time.LocalTime

object ScheduleEvaluator {

    /**
     * Checks if the given schedule is active at [currentTime] and [dayOfWeek].
     * Supports both standard same-day windows (start < end) and overnight windows (start > end).
     *
     * @param schedule The schedule entity with startMinute, endMinute, and daysOfWeekMask.
     * @param currentTime The local time to evaluate against.
     * @param dayOfWeek The current day of week.
     */
    fun isScheduleActive(
        schedule: BlockScheduleEntity,
        currentTime: LocalTime,
        dayOfWeek: DayOfWeek
    ): Boolean {
        if (!schedule.isEnabled) return false

        val currentMinute = currentTime.hour * 60 + currentTime.minute
        val startMin = schedule.startMinuteOfDay
        val endMin = schedule.endMinuteOfDay

        return if (startMin <= endMin) {
            // Same-day window (e.g. 09:00 to 17:00)
            if (isDayActive(schedule.daysOfWeekMask, dayOfWeek)) {
                currentMinute in startMin until endMin
            } else {
                false
            }
        } else {
            // Overnight window (e.g. 22:00 to 07:00)
            // Part 1: Tonight after startMin (matches today's active day)
            val matchesTonight = currentMinute >= startMin && isDayActive(schedule.daysOfWeekMask, dayOfWeek)
            // Part 2: Early morning before endMin (matches yesterday's active day)
            val yesterday = dayOfWeek.minus(1)
            val matchesMorning = currentMinute < endMin && isDayActive(schedule.daysOfWeekMask, yesterday)

            matchesTonight || matchesMorning
        }
    }

    /**
     * Checks if any schedule in the list is currently active.
     */
    fun isAnyScheduleActive(
        schedules: List<BlockScheduleEntity>,
        currentTime: LocalTime,
        dayOfWeek: DayOfWeek
    ): Boolean {
        return schedules.any { isScheduleActive(it, currentTime, dayOfWeek) }
    }

    /**
     * Checks whether a day of the week is included in the bitmask.
     * Monday = bit 0 (value 1)
     * Sunday = bit 6 (value 64)
     * All 7 days = mask 127
     */
    fun isDayActive(mask: Int, dayOfWeek: DayOfWeek): Boolean {
        val bit = 1 shl (dayOfWeek.value - 1)
        return (mask and bit) != 0
    }

    /**
     * Creates a weekday mask from a list of DayOfWeek values.
     */
    fun createWeekdayMask(days: Collection<DayOfWeek>): Int {
        var mask = 0
        days.forEach { day ->
            mask = mask or (1 shl (day.value - 1))
        }
        return mask
    }
}
