package com.kintsu.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3)
}

enum class TaskRecurrenceType(val displayName: String) {
    NEVER("Never"),
    DAILY("Every day"),
    WEEKDAYS("Selected weekdays"),
    WEEKLY("Every week"),
    MONTHLY("Every month")
}

data class DailyPlanItem(
    val taskId: Long,
    val timeBlock: String? = null
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedGoalId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("linkedGoalId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dueDateEpochDay: Long? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val linkedGoalId: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val recurrenceType: TaskRecurrenceType = TaskRecurrenceType.NEVER,
    val recurrenceWeekdaysMask: Int = 0,
    val recurrenceActive: Boolean = true,
    val plannedDateEpochDay: Long? = null,
    val plannedTimeBlock: String? = null,
    val planOrder: Int = 0,
    val category: String? = null,
    val notes: String? = null,
    val estimatedMinutes: Int? = null
) {
    val isRecurring: Boolean
        get() = recurrenceType != TaskRecurrenceType.NEVER
}

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "task_completions",
    indices = [Index("taskId")]
)
data class TaskCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val epochDay: Long,
    val completedAt: Long = System.currentTimeMillis()
)
