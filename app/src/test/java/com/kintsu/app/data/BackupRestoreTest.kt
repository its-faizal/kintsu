package com.kintsu.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRestoreTest {

    @Test
    fun testValidateBackup_validJson() {
        val validJson = """
            {
                "app": "Kintsu",
                "schemaVersion": 2,
                "exportedAt": 1729000000000,
                "habits": [
                    {
                        "id": 1,
                        "name": "Morning Tea",
                        "frequencyType": "DAILY",
                        "weekdaysMask": 127
                    }
                ],
                "logs": [],
                "tasks": [],
                "goals": []
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(validJson)
        assertTrue(result.isValid)
        assertNotNull(result.data)
        assertEquals(1, result.data!!.habits.size)
        assertEquals("Morning Tea", result.data!!.habits[0].name)
    }

    @Test
    fun testValidateBackup_wrongApp() {
        val wrongAppJson = """
            {
                "app": "AnotherApp",
                "schemaVersion": 2,
                "habits": []
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(wrongAppJson)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("not a recognized Kintsu backup"))
    }

    @Test
    fun testValidateBackup_unsupportedSchemaVersion() {
        val futureJson = """
            {
                "app": "Kintsu",
                "schemaVersion": 999,
                "habits": []
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(futureJson)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Unsupported schema version"))
    }

    @Test
    fun testValidateBackup_corruptJson() {
        val corruptJson = "{ this is not json"
        val result = BackupRestoreManager.validateBackup(corruptJson)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Corrupt backup file"))
    }

    @Test
    fun testValidateBackup_schema1BackwardCompatibility() {
        val schema1Json = """
            {
                "app": "Kintsu",
                "schemaVersion": 1,
                "exportedAt": 1729000000000,
                "habits": [
                    {
                        "id": 1,
                        "name": "Breathwork",
                        "frequencyType": "DAILY",
                        "weekdaysMask": 127
                    }
                ],
                "logs": [],
                "tasks": [],
                "goals": []
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(schema1Json)
        assertTrue(result.isValid)
        assertNotNull(result.data)
        assertEquals(1, result.data!!.schemaVersion)
        assertEquals(1, result.data!!.habits.size)
        assertTrue(result.data!!.focusSessions.isEmpty())
        assertTrue(result.data!!.dailyIntentions.isEmpty())
        assertTrue(result.data!!.journalEntries.isEmpty())
        assertTrue(result.data!!.weeklyReviews.isEmpty())
    }

    @Test
    fun testValidateBackup_schema3V2Data() {
        val schema3Json = """
            {
                "app": "Kintsu",
                "schemaVersion": 3,
                "exportedAt": 1729100000000,
                "habits": [],
                "logs": [],
                "tasks": [],
                "goals": [],
                "focus_sessions": [
                    {
                        "id": 1,
                        "dateEpochDay": 20000,
                        "durationMinutes": 25,
                        "linkedHabitId": 10,
                        "isCompleted": true,
                        "startTimeMillis": 1729100000000,
                        "endTimeMillis": 1729101500000
                    }
                ],
                "daily_intentions": [
                    {
                        "id": 1,
                        "dateEpochDay": 20000,
                        "text": "Breathe deep and write clean code",
                        "isCompleted": true,
                        "updatedAt": 1729100000000
                    }
                ],
                "journal_entries": [
                    {
                        "id": 1,
                        "dateEpochDay": 20000,
                        "content": "Peaceful evening reflection",
                        "mood": "🌿 Calm",
                        "createdAt": 1729100000000,
                        "updatedAt": 1729100000000
                    }
                ],
                "weekly_reviews": [
                    {
                        "id": 1,
                        "weekStartEpochDay": 19994,
                        "weekEndEpochDay": 20000,
                        "completionRate": 0.85,
                        "bestStreak": 7,
                        "totalCracks": 1,
                        "totalRepairs": 1,
                        "focusMinutes": 120,
                        "mostConsistentHabitName": "Meditation",
                        "leastConsistentHabitName": "Running",
                        "goalsProgressSummary": "1 on track",
                        "reflection1": "Calm focus",
                        "reflection2": "Repaired Tuesday crack",
                        "reflection3": "Continue steady rhythm",
                        "createdAt": 1729102000000
                    }
                ]
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(schema3Json)
        assertTrue(result.isValid)
        assertNotNull(result.data)
        assertEquals(3, result.data!!.schemaVersion)
        assertEquals(1, result.data!!.focusSessions.size)
        assertEquals(25, result.data!!.focusSessions[0].durationMinutes)
        assertEquals(1, result.data!!.dailyIntentions.size)
        assertEquals("Breathe deep and write clean code", result.data!!.dailyIntentions[0].text)
        assertEquals(1, result.data!!.journalEntries.size)
        assertEquals("🌿 Calm", result.data!!.journalEntries[0].mood)
        assertEquals(1, result.data!!.weeklyReviews.size)
        assertEquals(120, result.data!!.weeklyReviews[0].focusMinutes)
    }

    @Test
    fun testValidateBackup_schema4HomePreferences() {
        val schema4Json = """
            {
                "app": "Kintsu",
                "schemaVersion": 4,
                "exportedAt": 1729200000000,
                "habits": [],
                "logs": [],
                "tasks": [],
                "goals": [],
                "home_preferences": {
                    "section_order": "KEY_NUMBERS,FOCUS,DAILY_INTENTION,QUICK_ACTIONS",
                    "hidden_sections": "FOCUS",
                    "quick_actions_order": "START_FOCUS,ADD_HABIT,ADD_TASK,JOURNAL,NEW_GOAL"
                }
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(schema4Json)
        assertTrue(result.isValid)
        assertNotNull(result.data)
        assertEquals(4, result.data!!.schemaVersion)
        assertEquals("KEY_NUMBERS,FOCUS,DAILY_INTENTION,QUICK_ACTIONS", result.data!!.homeSectionOrder)
        assertEquals("FOCUS", result.data!!.hiddenHomeSections)
        assertEquals("START_FOCUS,ADD_HABIT,ADD_TASK,JOURNAL,NEW_GOAL", result.data!!.quickActionsOrder)
    }

    @Test
    fun testValidateBackup_habitsWithLinkedGoalId() {
        val backupJson = """
            {
                "app": "Kintsu",
                "schemaVersion": 4,
                "exportedAt": 1729300000000,
                "habits": [
                    {
                        "id": 1,
                        "name": "Morning Run",
                        "frequencyType": "DAILY",
                        "weekdaysMask": 127,
                        "linkedGoalId": 10
                    },
                    {
                        "id": 2,
                        "name": "Read",
                        "frequencyType": "DAILY",
                        "weekdaysMask": 127
                    }
                ],
                "logs": [],
                "tasks": [],
                "goals": []
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(backupJson)
        assertTrue(result.isValid)
        assertNotNull(result.data)
        assertEquals(2, result.data!!.habits.size)
        assertEquals(10L, result.data!!.habits[0].linkedGoalId)
        assertEquals(null, result.data!!.habits[1].linkedGoalId)
    }

    @Test
    fun testValidateBackup_appearancePreferencesAppTheme() {
        val backupJson = """
            {
                "app": "Kintsu",
                "schemaVersion": 4,
                "exportedAt": 1729400000000,
                "habits": [],
                "logs": [],
                "tasks": [],
                "goals": [],
                "appearance_preferences": {
                    "app_theme": "MIDNIGHT_CHAMPAGNE",
                    "theme_mode": "DARK",
                    "accent_option": "GOLD"
                }
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(backupJson)
        assertTrue(result.isValid)
        assertNotNull(result.data)
        assertEquals("MIDNIGHT_CHAMPAGNE", result.data!!.appTheme)
    }

    @Test
    fun testValidateBackup_schema5SubtasksAndRecurrence() {
        val backupJson = """
            {
                "app": "Kintsu",
                "schemaVersion": 5,
                "exportedAt": 1729500000000,
                "habits": [],
                "logs": [],
                "tasks": [
                    {
                        "id": 1,
                        "title": "Weekly Planning",
                        "priority": "HIGH",
                        "recurrenceType": "WEEKLY",
                        "recurrenceWeekdaysMask": 0,
                        "recurrenceActive": true,
                        "plannedDateEpochDay": 20755,
                        "plannedTimeBlock": "Morning",
                        "planOrder": 0
                    }
                ],
                "subtasks": [
                    {
                        "id": 10,
                        "taskId": 1,
                        "title": "Review metrics",
                        "isCompleted": true,
                        "orderIndex": 0
                    }
                ],
                "task_completions": [
                    {
                        "id": 100,
                        "taskId": 1,
                        "epochDay": 20748,
                        "completedAt": 1729400000000
                    }
                ],
                "goals": []
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(backupJson)
        assertTrue(result.isValid)
        assertNotNull(result.data)
        assertEquals(5, result.data!!.schemaVersion)
        assertEquals(1, result.data!!.tasks.size)
        assertEquals("WEEKLY", result.data!!.tasks[0].recurrenceType.name)
        assertEquals("Morning", result.data!!.tasks[0].plannedTimeBlock)
        assertEquals(1, result.data!!.subtasks.size)
        assertEquals("Review metrics", result.data!!.subtasks[0].title)
        assertTrue(result.data!!.subtasks[0].isCompleted)
        assertEquals(1, result.data!!.taskCompletions.size)
    }

    @Test
    fun testValidateBackup_focusSessionWithLinkedTask() {
        val backupJson = """
            {
                "app": "Kintsu",
                "schemaVersion": 5,
                "exportedAt": 1729600000000,
                "habits": [],
                "logs": [],
                "tasks": [
                    {
                        "id": 10,
                        "title": "Deep Focus Work",
                        "priority": "HIGH"
                    }
                ],
                "goals": [],
                "focus_sessions": [
                    {
                        "id": 1,
                        "dateEpochDay": 20750,
                        "durationMinutes": 50,
                        "linkedTaskId": 10,
                        "isCompleted": true,
                        "startTimeMillis": 1729500000000,
                        "endTimeMillis": 1729503000000
                    }
                ]
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(backupJson)
        assertTrue(result.isValid)
        assertNotNull(result.data)
        assertEquals(1, result.data!!.focusSessions.size)
        val session = result.data!!.focusSessions[0]
        assertEquals(50, session.durationMinutes)
        assertEquals(10L, session.linkedTaskId)
        assertTrue(session.isCompleted)
    }

    @Test
    fun testValidateBackup_v26AppearancePreferences() {
        val backupJson = """
            {
                "app": "Kintsu",
                "schemaVersion": 5,
                "exportedAt": 1729600000000,
                "habits": [],
                "logs": [],
                "tasks": [],
                "goals": [],
                "appearance_preferences": {
                    "app_theme": "SAGE_IVORY",
                    "theme_mode": "DARK",
                    "accent_option": "GOLD",
                    "content_density": "COMPACT",
                    "haptics_enabled": false,
                    "reduced_motion": true
                }
            }
        """.trimIndent()

        val result = BackupRestoreManager.validateBackup(backupJson)
        assertTrue(result.isValid)
        assertNotNull(result.data)
        assertEquals("SAGE_IVORY", result.data!!.appTheme)
        assertEquals("COMPACT", result.data!!.contentDensity)
        assertEquals(false, result.data!!.hapticsEnabled)
        assertEquals(true, result.data!!.reducedMotion)
    }
}
