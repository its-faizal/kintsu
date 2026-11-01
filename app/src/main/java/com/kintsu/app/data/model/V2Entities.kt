package com.kintsu.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "focus_sessions",
    indices = [
        Index("linkedTaskId")
    ]
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val durationMinutes: Int,
    val linkedHabitId: Long? = null,
    val linkedTaskId: Long? = null,
    val isCompleted: Boolean = true,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val endTimeMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_intentions")
data class DailyIntentionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val text: String,
    val isCompleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "journal_entries",
    indices = [
        Index("collectionId"),
        Index("dateEpochDay"),
        Index("isFavorite")
    ]
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val content: String,
    val mood: String? = null,
    val title: String? = null,
    val moodNote: String? = null,
    val emotions: String = "",
    val tags: String = "",
    val photoUris: String = "",
    val isFavorite: Boolean = false,
    val collectionId: Long = 1L,
    val linkedHabitIds: String = "",
    val linkedTaskIds: String = "",
    val linkedGoalIds: String = "",
    val linkedFocusSessionId: Long? = null,
    val isPrivate: Boolean = false,
    val promptUsed: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_collections")
data class JournalCollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#7F9A83",
    val iconName: String = "book",
    val isPrivate: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_templates")
data class JournalTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val templateContent: String,
    val category: String,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "weekly_reviews")
data class WeeklyReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekStartEpochDay: Long,
    val weekEndEpochDay: Long,
    val completionRate: Float,
    val bestStreak: Int,
    val totalCracks: Int,
    val totalRepairs: Int,
    val focusMinutes: Int,
    val mostConsistentHabitName: String?,
    val leastConsistentHabitName: String?,
    val goalsProgressSummary: String,
    val reflection1: String = "",
    val reflection2: String = "",
    val reflection3: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
