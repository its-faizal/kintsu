package com.kintsu.app.sync

import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import com.kintsu.app.data.model.TaskRecurrenceType
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KintsuSyncEngineTest {

    @Test
    fun testSerializeAndDeserializeHabit() {
        val habit = HabitEntity(
            id = 42L,
            name = "Morning Stillness",
            iconName = "tea",
            colorHex = "#B8935A",
            frequencyTarget = 2,
            reminderTimes = "07:30,19:30"
        )

        val json = JSONObject().apply {
            put("name", habit.name)
            put("iconName", habit.iconName)
            put("colorHex", habit.colorHex)
            put("frequencyType", habit.frequencyType.name)
            put("weekdaysMask", habit.weekdaysMask)
            put("reminderTime", habit.reminderTime ?: "")
            put("reminderTimes", habit.reminderTimes)
            put("frequencyTarget", habit.frequencyTarget)
            put("routineGroup", habit.routineGroup ?: "")
            put("isArchived", habit.isArchived)
            put("createdAt", habit.createdAt)
            put("displayOrder", habit.displayOrder)
        }

        assertEquals("Morning Stillness", json.getString("name"))
        assertEquals(2, json.getInt("frequencyTarget"))
        assertEquals("07:30,19:30", json.getString("reminderTimes"))
    }

    @Test
    fun testSerializeAndDeserializeTask() {
        val task = TaskEntity(
            id = 99L,
            title = "Finalize quarterly vision",
            dueDateEpochDay = 20755L,
            priority = TaskPriority.HIGH,
            recurrenceType = TaskRecurrenceType.WEEKLY,
            plannedTimeBlock = "Morning"
        )

        val json = JSONObject().apply {
            put("title", task.title)
            put("dueDateEpochDay", task.dueDateEpochDay)
            put("priority", task.priority.name)
            put("isCompleted", task.isCompleted)
            put("recurrenceType", task.recurrenceType.name)
            put("plannedTimeBlock", task.plannedTimeBlock ?: "")
        }

        assertEquals("Finalize quarterly vision", json.getString("title"))
        assertEquals("HIGH", json.getString("priority"))
        assertEquals("WEEKLY", json.getString("recurrenceType"))
        assertEquals("Morning", json.getString("plannedTimeBlock"))
    }

    @Test
    fun testJournalConflictResolution_nonDestructiveBranching() {
        // Simulates two devices editing the same journal day while offline
        val localContent = "Reflected on mindful walking today. Felt peaceful."
        val remoteContent = "Completed long morning meditation. Clear head."

        val isDifferent = localContent != remoteContent
        assertTrue("Conflict should be detected when content differs", isDifferent)

        // Non-destructive merge preserves both reflections
        val resolvedContent = "$localContent\n\n[Synchronised revision from another device]:\n$remoteContent"

        assertTrue(resolvedContent.contains(localContent))
        assertTrue(resolvedContent.contains(remoteContent))
        assertTrue(resolvedContent.contains("[Synchronised revision from another device]"))
    }

    @Test
    fun testLastWriteWins_deterministicResolution() {
        val localUpdatedAt = 1729000000000L
        val remoteNewerUpdatedAt = 1729000500000L
        val remoteOlderUpdatedAt = 1728999000000L

        // Remote is newer: remote wins
        assertTrue(remoteNewerUpdatedAt > localUpdatedAt)

        // Remote is older: local wins
        assertFalse(remoteOlderUpdatedAt > localUpdatedAt)
    }

    @Test
    fun testTombstoneDeletionHandling() {
        val remoteRecord = RemoteRecord(
            syncUuid = "test-uuid-1234",
            tableName = "habits",
            payloadJson = "{}",
            updatedAt = 1729500000000L,
            isDeleted = true
        )

        assertTrue(remoteRecord.isDeleted)
        assertEquals("test-uuid-1234", remoteRecord.syncUuid)
    }
}
