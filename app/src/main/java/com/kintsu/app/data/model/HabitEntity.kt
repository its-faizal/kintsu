package com.kintsu.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class FrequencyType {
    DAILY,
    SPECIFIC_DAYS,
    TIMES_PER_WEEK,
    TIMES_PER_DAY
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String = "habit",
    val colorHex: String = "#B8935A",
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val weekdaysMask: Int = 127, // Bitmask: Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64
    val reminderTime: String? = null,
    val reminderTimes: String = "", // Comma-separated or JSON list of times e.g. "08:00,20:00"
    val frequencyTarget: Int = 1,   // Target count for TIMES_PER_WEEK or TIMES_PER_DAY
    val routineGroup: String? = null, // Morning, Evening, or custom routine
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val displayOrder: Int = 0,
    val linkedGoalId: Long? = null
)

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("habitId"),
        Index(value = ["habitId", "epochDay"], unique = true)
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val epochDay: Long,
    val isCompleted: Boolean = false,
    val isRepairedGold: Boolean = false,
    val repairedAt: Long? = null,
    val isCrack: Boolean = false,
    val completionCount: Int = 1,
    val note: String? = null,
    val mood: String? = null,
    val completedAt: Long = System.currentTimeMillis()
)
