package com.kintsu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GoalProgressType {
    MANUAL,
    AUTO_FROM_ITEMS
}

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateEpochDay: Long,
    val progressType: GoalProgressType = GoalProgressType.AUTO_FROM_ITEMS,
    val manualProgressPercent: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
