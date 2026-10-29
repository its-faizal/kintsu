package com.kintsu.app.domain

import com.kintsu.app.data.model.HabitEntity

enum class DayTileType {
    INTACT_COMPLETED,
    REPAIRED_GOLD,
    REPAIRABLE_CRACK,
    PERMANENT_CRACK,
    EMPTY_FUTURE_OR_PAST,
    NOT_SCHEDULED
}

data class DayStatus(
    val epochDay: Long,
    val type: DayTileType,
    val remainingRepairHours: Long = 0,
    val habitId: Long? = null,
    val habitName: String? = null
)

data class HabitRepairState(
    val habit: HabitEntity,
    val missedEpochDay: Long,
    val remainingHours: Long,
    val canRepair: Boolean,
    val reasonDisabled: String? = null
)

data class StreakResult(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalRepairs: Int,
    val totalCracks: Int,
    val activeRepairAvailable: Boolean = false
)
