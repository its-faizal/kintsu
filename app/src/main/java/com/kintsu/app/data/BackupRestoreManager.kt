package com.kintsu.app.data

import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.BlockScheduleEntity
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.DigitalDetoxEntity
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.QuitHabitEntity
import com.kintsu.app.data.model.SubtaskEntity
import com.kintsu.app.data.model.TaskCompletionEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import com.kintsu.app.data.model.TaskRecurrenceType
import com.kintsu.app.data.model.UrgeLogEntity
import com.kintsu.app.data.model.WeeklyReviewEntity
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

enum class ImportMode {
    REPLACE,
    MERGE
}

data class BackupData(
    val schemaVersion: Int,
    val exportedAt: Long,
    val habits: List<HabitEntity>,
    val logs: List<HabitLogEntity>,
    val tasks: List<TaskEntity>,
    val goals: List<GoalEntity>,
    val focusSessions: List<FocusSessionEntity> = emptyList(),
    val dailyIntentions: List<DailyIntentionEntity> = emptyList(),
    val journalEntries: List<JournalEntryEntity> = emptyList(),
    val weeklyReviews: List<WeeklyReviewEntity> = emptyList(),
    val subtasks: List<SubtaskEntity> = emptyList(),
    val taskCompletions: List<TaskCompletionEntity> = emptyList(),
    val homeSectionOrder: String? = null,
    val hiddenHomeSections: String? = null,
    val quickActionsOrder: String? = null,
    val appTheme: String? = null,
    val contentDensity: String? = null,
    val hapticsEnabled: Boolean? = null,
    val reducedMotion: Boolean? = null,
    val appGroups: List<AppGroupEntity> = emptyList(),
    val blockRules: List<BlockRuleEntity> = emptyList(),
    val blockSchedules: List<BlockScheduleEntity> = emptyList(),
    val detoxSessions: List<DigitalDetoxEntity> = emptyList(),
    val quitHabits: List<QuitHabitEntity> = emptyList(),
    val urgeLogs: List<UrgeLogEntity> = emptyList()
)

data class BackupValidation(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val data: BackupData? = null
)

object BackupRestoreManager {
    const val CURRENT_SCHEMA_VERSION = 5

    suspend fun createBackupJson(
        database: KintsuDatabase,
        settings: KintsuSettings? = null
    ): String {
        val habits = database.habitDao().getAllHabits().first()
        val logs = database.habitDao().getAllLogs().first()
        val tasks = database.taskDao().getAllTasks().first()
        val goals = database.goalDao().getAllGoals().first()
        val focusSessions = database.focusDao().getAllSessions().first()
        val dailyIntentions = database.dailyIntentionDao().getAllIntentions().first()
        val journalEntries = database.journalDao().getAllEntries().first()
        val weeklyReviews = database.weeklyReviewDao().getAllReviews().first()
        val subtasks = database.subtaskDao().getAllSubtasks().first()
        val taskCompletions = database.taskCompletionDao().getAllCompletions().first()
        val wellbeingDao = database.wellbeingDao()
        val appGroups = wellbeingDao.getAllAppGroups().first()
        val blockRules = wellbeingDao.getAllBlockRules().first()
        val blockSchedules = wellbeingDao.getAllSchedules().first()
        val detoxSessions = wellbeingDao.getAllDetoxSessions().first()
        val quitHabits = wellbeingDao.getAllQuitHabits().first()

        val root = JSONObject()
        root.put("app", "Kintsu")
        root.put("schemaVersion", CURRENT_SCHEMA_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        if (settings != null) {
            val homePrefs = JSONObject()
            homePrefs.put("section_order", settings.homeSectionOrder)
            homePrefs.put("hidden_sections", settings.hiddenHomeSections)
            homePrefs.put("quick_actions_order", settings.quickActionsOrder)
            root.put("home_preferences", homePrefs)

            val appearancePrefs = JSONObject()
            appearancePrefs.put("app_theme", settings.appTheme.name)
            appearancePrefs.put("theme_mode", settings.themeMode.name)
            appearancePrefs.put("accent_option", settings.accentOption.name)
            appearancePrefs.put("content_density", settings.contentDensity.name)
            appearancePrefs.put("haptics_enabled", settings.hapticsEnabled)
            appearancePrefs.put("reduced_motion", settings.reducedMotion)
            root.put("appearance_preferences", appearancePrefs)
        }

        val habitsArr = JSONArray()
        habits.forEach { h ->
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("name", h.name)
            obj.put("iconName", h.iconName)
            obj.put("colorHex", h.colorHex)
            obj.put("frequencyType", h.frequencyType.name)
            obj.put("weekdaysMask", h.weekdaysMask)
            obj.put("reminderTime", h.reminderTime ?: "")
            obj.put("reminderTimes", h.reminderTimes)
            obj.put("frequencyTarget", h.frequencyTarget)
            obj.put("routineGroup", h.routineGroup ?: "")
            obj.put("linkedGoalId", h.linkedGoalId ?: -1L)
            obj.put("isArchived", h.isArchived)
            obj.put("createdAt", h.createdAt)
            obj.put("displayOrder", h.displayOrder)
            habitsArr.put(obj)
        }
        root.put("habits", habitsArr)

        val logsArr = JSONArray()
        logs.forEach { l ->
            val obj = JSONObject()
            obj.put("id", l.id)
            obj.put("habitId", l.habitId)
            obj.put("epochDay", l.epochDay)
            obj.put("isCompleted", l.isCompleted)
            obj.put("isRepairedGold", l.isRepairedGold)
            obj.put("repairedAt", l.repairedAt ?: -1L)
            obj.put("isCrack", l.isCrack)
            obj.put("completionCount", l.completionCount)
            obj.put("note", l.note ?: "")
            obj.put("mood", l.mood ?: "")
            obj.put("completedAt", l.completedAt)
            logsArr.put(obj)
        }
        root.put("logs", logsArr)

        val tasksArr = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("dueDateEpochDay", t.dueDateEpochDay ?: -1L)
            obj.put("priority", t.priority.name)
            obj.put("linkedGoalId", t.linkedGoalId ?: -1L)
            obj.put("isCompleted", t.isCompleted)
            obj.put("createdAt", t.createdAt)
            obj.put("completedAt", t.completedAt ?: -1L)
            obj.put("recurrenceType", t.recurrenceType.name)
            obj.put("recurrenceWeekdaysMask", t.recurrenceWeekdaysMask)
            obj.put("recurrenceActive", t.recurrenceActive)
            obj.put("plannedDateEpochDay", t.plannedDateEpochDay ?: -1L)
            obj.put("plannedTimeBlock", t.plannedTimeBlock ?: "")
            obj.put("planOrder", t.planOrder)
            obj.put("category", t.category ?: "")
            obj.put("notes", t.notes ?: "")
            obj.put("estimatedMinutes", t.estimatedMinutes ?: -1)
            tasksArr.put(obj)
        }
        root.put("tasks", tasksArr)

        val subtasksArr = JSONArray()
        subtasks.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("taskId", s.taskId)
            obj.put("title", s.title)
            obj.put("isCompleted", s.isCompleted)
            obj.put("orderIndex", s.orderIndex)
            obj.put("createdAt", s.createdAt)
            subtasksArr.put(obj)
        }
        root.put("subtasks", subtasksArr)

        val taskCompletionsArr = JSONArray()
        taskCompletions.forEach { tc ->
            val obj = JSONObject()
            obj.put("id", tc.id)
            obj.put("taskId", tc.taskId)
            obj.put("epochDay", tc.epochDay)
            obj.put("completedAt", tc.completedAt)
            taskCompletionsArr.put(obj)
        }
        root.put("task_completions", taskCompletionsArr)

        val goalsArr = JSONArray()
        goals.forEach { g ->
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("title", g.title)
            obj.put("targetDateEpochDay", g.targetDateEpochDay)
            obj.put("progressType", g.progressType.name)
            obj.put("manualProgressPercent", g.manualProgressPercent)
            obj.put("isCompleted", g.isCompleted)
            obj.put("isArchived", g.isArchived)
            obj.put("notes", g.notes ?: "")
            obj.put("createdAt", g.createdAt)
            goalsArr.put(obj)
        }
        root.put("goals", goalsArr)

        val focusArr = JSONArray()
        focusSessions.forEach { f ->
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("dateEpochDay", f.dateEpochDay)
            obj.put("durationMinutes", f.durationMinutes)
            obj.put("linkedHabitId", f.linkedHabitId ?: -1L)
            obj.put("linkedTaskId", f.linkedTaskId ?: -1L)
            obj.put("isCompleted", f.isCompleted)
            obj.put("startTimeMillis", f.startTimeMillis)
            obj.put("endTimeMillis", f.endTimeMillis)
            focusArr.put(obj)
        }
        root.put("focus_sessions", focusArr)

        val intentionsArr = JSONArray()
        dailyIntentions.forEach { i ->
            val obj = JSONObject()
            obj.put("id", i.id)
            obj.put("dateEpochDay", i.dateEpochDay)
            obj.put("text", i.text)
            obj.put("isCompleted", i.isCompleted)
            obj.put("updatedAt", i.updatedAt)
            intentionsArr.put(obj)
        }
        root.put("daily_intentions", intentionsArr)

        val journalArr = JSONArray()
        journalEntries.forEach { j ->
            val obj = JSONObject()
            obj.put("id", j.id)
            obj.put("dateEpochDay", j.dateEpochDay)
            obj.put("content", j.content)
            obj.put("mood", j.mood ?: "")
            obj.put("createdAt", j.createdAt)
            obj.put("updatedAt", j.updatedAt)
            journalArr.put(obj)
        }
        root.put("journal_entries", journalArr)

        val reviewsArr = JSONArray()
        weeklyReviews.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("weekStartEpochDay", r.weekStartEpochDay)
            obj.put("weekEndEpochDay", r.weekEndEpochDay)
            obj.put("completionRate", r.completionRate.toDouble())
            obj.put("bestStreak", r.bestStreak)
            obj.put("totalCracks", r.totalCracks)
            obj.put("totalRepairs", r.totalRepairs)
            obj.put("focusMinutes", r.focusMinutes)
            obj.put("mostConsistentHabitName", r.mostConsistentHabitName ?: "")
            obj.put("leastConsistentHabitName", r.leastConsistentHabitName ?: "")
            obj.put("goalsProgressSummary", r.goalsProgressSummary)
            obj.put("reflection1", r.reflection1)
            obj.put("reflection2", r.reflection2)
            obj.put("reflection3", r.reflection3)
            obj.put("createdAt", r.createdAt)
            reviewsArr.put(obj)
        }
        root.put("weekly_reviews", reviewsArr)

        val groupsArr = JSONArray()
        appGroups.forEach { g ->
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("name", g.name)
            obj.put("categoryType", g.categoryType)
            obj.put("packageNames", g.packageNames)
            obj.put("isPredefined", g.isPredefined)
            groupsArr.put(obj)
        }
        root.put("app_groups", groupsArr)

        val rulesArr = JSONArray()
        blockRules.forEach { br ->
            val obj = JSONObject()
            obj.put("id", br.id)
            obj.put("appGroupId", br.appGroupId)
            obj.put("mode", br.mode)
            obj.put("dailyLimitMinutes", br.dailyLimitMinutes)
            obj.put("maxOpensPerDay", br.maxOpensPerDay)
            obj.put("strictnessLevel", br.strictnessLevel)
            obj.put("pauseBreathingSeconds", br.pauseBreathingSeconds)
            obj.put("pauseQuestion", br.pauseQuestion)
            obj.put("isEnabled", br.isEnabled)
            rulesArr.put(obj)
        }
        root.put("block_rules", rulesArr)

        val schedulesArr = JSONArray()
        blockSchedules.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("ruleId", s.ruleId)
            obj.put("startMinuteOfDay", s.startMinuteOfDay)
            obj.put("endMinuteOfDay", s.endMinuteOfDay)
            obj.put("daysOfWeekMask", s.daysOfWeekMask)
            obj.put("isEnabled", s.isEnabled)
            schedulesArr.put(obj)
        }
        root.put("block_schedules", schedulesArr)

        val detoxArr = JSONArray()
        detoxSessions.forEach { d ->
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("startTimeMillis", d.startTimeMillis)
            obj.put("durationMinutes", d.durationMinutes)
            obj.put("endTimeMillis", d.endTimeMillis)
            obj.put("intention", d.intention ?: "")
            obj.put("allowedPackages", d.allowedPackages)
            obj.put("enableDnd", d.enableDnd)
            obj.put("isCompleted", d.isCompleted)
            detoxArr.put(obj)
        }
        root.put("detox_sessions", detoxArr)

        val quitHabitsArr = JSONArray()
        quitHabits.forEach { q ->
            val obj = JSONObject()
            obj.put("id", q.id)
            obj.put("name", q.name)
            obj.put("iconName", q.iconName)
            obj.put("colorHex", q.colorHex)
            obj.put("cleanStreakStartMillis", q.cleanStreakStartMillis)
            obj.put("bestStreakDays", q.bestStreakDays)
            obj.put("totalSlips", q.totalSlips)
            obj.put("totalRepairs", q.totalRepairs)
            obj.put("ifThenPlan", q.ifThenPlan ?: "")
            obj.put("emergencyContactName", q.emergencyContactName ?: "")
            obj.put("emergencyContactPhone", q.emergencyContactPhone ?: "")
            quitHabitsArr.put(obj)
        }
        root.put("quit_habits", quitHabitsArr)

        return root.toString(2)
    }

    fun validateBackup(jsonString: String): BackupValidation {
        return try {
            val root = JSONObject(jsonString)
            val app = root.optString("app")
            if (app != "Kintsu") {
                return BackupValidation(
                    isValid = false,
                    errorMessage = "Invalid backup file: not a recognized Kintsu backup."
                )
            }

            val schemaVersion = root.optInt("schemaVersion", -1)
            if (schemaVersion !in 1..CURRENT_SCHEMA_VERSION) {
                return BackupValidation(
                    isValid = false,
                    errorMessage = "Unsupported schema version: $schemaVersion. Update Kintsu to restore."
                )
            }

            val habitsList = mutableListOf<HabitEntity>()
            val habitsArr = root.optJSONArray("habits") ?: JSONArray()
            for (i in 0 until habitsArr.length()) {
                val h = habitsArr.getJSONObject(i)
                val freqStr = h.optString("frequencyType", FrequencyType.DAILY.name)
                val freq = try { FrequencyType.valueOf(freqStr) } catch (e: Exception) { FrequencyType.DAILY }
                habitsList.add(
                    HabitEntity(
                        id = h.optLong("id", 0L),
                        name = h.optString("name", "Ritual"),
                        iconName = h.optString("iconName", "habit"),
                        colorHex = h.optString("colorHex", "#B8935A"),
                        frequencyType = freq,
                        weekdaysMask = h.optInt("weekdaysMask", 127),
                        reminderTime = h.optString("reminderTime").ifBlank { null },
                        reminderTimes = h.optString("reminderTimes", ""),
                        frequencyTarget = h.optInt("frequencyTarget", 1),
                        routineGroup = h.optString("routineGroup").ifBlank { null },
                        linkedGoalId = h.optLong("linkedGoalId", -1L).let { if (it > 0) it else null },
                        isArchived = h.optBoolean("isArchived", false),
                        createdAt = h.optLong("createdAt", System.currentTimeMillis()),
                        displayOrder = h.optInt("displayOrder", 0)
                    )
                )
            }

            val logsList = mutableListOf<HabitLogEntity>()
            val logsArr = root.optJSONArray("logs") ?: JSONArray()
            for (i in 0 until logsArr.length()) {
                val l = logsArr.getJSONObject(i)
                val repairedAt = l.optLong("repairedAt", -1L)
                logsList.add(
                    HabitLogEntity(
                        id = l.optLong("id", 0L),
                        habitId = l.optLong("habitId", 0L),
                        epochDay = l.optLong("epochDay", 0L),
                        isCompleted = l.optBoolean("isCompleted", false),
                        isRepairedGold = l.optBoolean("isRepairedGold", false),
                        repairedAt = if (repairedAt > 0) repairedAt else null,
                        isCrack = l.optBoolean("isCrack", false),
                        completionCount = l.optInt("completionCount", 1),
                        note = l.optString("note").ifBlank { null },
                        mood = l.optString("mood").ifBlank { null },
                        completedAt = l.optLong("completedAt", System.currentTimeMillis())
                    )
                )
            }

            val tasksList = mutableListOf<TaskEntity>()
            val tasksArr = root.optJSONArray("tasks") ?: JSONArray()
            for (i in 0 until tasksArr.length()) {
                val t = tasksArr.getJSONObject(i)
                val prioStr = t.optString("priority", TaskPriority.MEDIUM.name)
                val prio = try { TaskPriority.valueOf(prioStr) } catch (e: Exception) { TaskPriority.MEDIUM }
                val due = t.optLong("dueDateEpochDay", -1L)
                val goalId = t.optLong("linkedGoalId", -1L)
                val compAt = t.optLong("completedAt", -1L)
                val recStr = t.optString("recurrenceType", TaskRecurrenceType.NEVER.name)
                val recType = try { TaskRecurrenceType.valueOf(recStr) } catch (e: Exception) { TaskRecurrenceType.NEVER }
                val plannedDate = t.optLong("plannedDateEpochDay", -1L)
                val plannedBlock = t.optString("plannedTimeBlock").ifBlank { null }
                tasksList.add(
                    TaskEntity(
                        id = t.optLong("id", 0L),
                        title = t.optString("title", "Task"),
                        dueDateEpochDay = if (due > 0) due else null,
                        priority = prio,
                        linkedGoalId = if (goalId > 0) goalId else null,
                        isCompleted = t.optBoolean("isCompleted", false),
                        createdAt = t.optLong("createdAt", System.currentTimeMillis()),
                        completedAt = if (compAt > 0) compAt else null,
                        recurrenceType = recType,
                        recurrenceWeekdaysMask = t.optInt("recurrenceWeekdaysMask", 0),
                        recurrenceActive = t.optBoolean("recurrenceActive", true),
                        plannedDateEpochDay = if (plannedDate > 0) plannedDate else null,
                        plannedTimeBlock = plannedBlock,
                        planOrder = t.optInt("planOrder", 0),
                        category = t.optString("category").ifBlank { null },
                        notes = t.optString("notes").ifBlank { null },
                        estimatedMinutes = t.optInt("estimatedMinutes", -1).let { if (it > 0) it else null }
                    )
                )
            }

            val subtasksList = mutableListOf<SubtaskEntity>()
            val subtasksArr = root.optJSONArray("subtasks") ?: JSONArray()
            for (i in 0 until subtasksArr.length()) {
                val s = subtasksArr.getJSONObject(i)
                subtasksList.add(
                    SubtaskEntity(
                        id = s.optLong("id", 0L),
                        taskId = s.optLong("taskId", 0L),
                        title = s.optString("title", "Subtask"),
                        isCompleted = s.optBoolean("isCompleted", false),
                        orderIndex = s.optInt("orderIndex", 0),
                        createdAt = s.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val taskCompletionsList = mutableListOf<TaskCompletionEntity>()
            val tcArr = root.optJSONArray("task_completions") ?: JSONArray()
            for (i in 0 until tcArr.length()) {
                val tc = tcArr.getJSONObject(i)
                taskCompletionsList.add(
                    TaskCompletionEntity(
                        id = tc.optLong("id", 0L),
                        taskId = tc.optLong("taskId", 0L),
                        epochDay = tc.optLong("epochDay", 0L),
                        completedAt = tc.optLong("completedAt", System.currentTimeMillis())
                    )
                )
            }

            val goalsList = mutableListOf<GoalEntity>()
            val goalsArr = root.optJSONArray("goals") ?: JSONArray()
            for (i in 0 until goalsArr.length()) {
                val g = goalsArr.getJSONObject(i)
                val pTypeStr = g.optString("progressType", GoalProgressType.AUTO_FROM_ITEMS.name)
                val pType = try { GoalProgressType.valueOf(pTypeStr) } catch (e: Exception) { GoalProgressType.AUTO_FROM_ITEMS }
                goalsList.add(
                    GoalEntity(
                        id = g.optLong("id", 0L),
                        title = g.optString("title", "Goal"),
                        targetDateEpochDay = g.optLong("targetDateEpochDay", 0L),
                        progressType = pType,
                        manualProgressPercent = g.optInt("manualProgressPercent", 0),
                        isCompleted = g.optBoolean("isCompleted", false),
                        isArchived = g.optBoolean("isArchived", false),
                        notes = g.optString("notes").ifBlank { null },
                        createdAt = g.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val focusList = mutableListOf<FocusSessionEntity>()
            val focusArr = root.optJSONArray("focus_sessions") ?: JSONArray()
            for (i in 0 until focusArr.length()) {
                val f = focusArr.getJSONObject(i)
                val hId = f.optLong("linkedHabitId", -1L)
                val tId = f.optLong("linkedTaskId", -1L)
                focusList.add(
                    FocusSessionEntity(
                        id = f.optLong("id", 0L),
                        dateEpochDay = f.optLong("dateEpochDay", 0L),
                        durationMinutes = f.optInt("durationMinutes", 25),
                        linkedHabitId = if (hId > 0) hId else null,
                        linkedTaskId = if (tId > 0) tId else null,
                        isCompleted = f.optBoolean("isCompleted", true),
                        startTimeMillis = f.optLong("startTimeMillis", System.currentTimeMillis()),
                        endTimeMillis = f.optLong("endTimeMillis", System.currentTimeMillis())
                    )
                )
            }

            val intentionList = mutableListOf<DailyIntentionEntity>()
            val intentionsArr = root.optJSONArray("daily_intentions") ?: JSONArray()
            for (i in 0 until intentionsArr.length()) {
                val item = intentionsArr.getJSONObject(i)
                intentionList.add(
                    DailyIntentionEntity(
                        id = item.optLong("id", 0L),
                        dateEpochDay = item.optLong("dateEpochDay", 0L),
                        text = item.optString("text", ""),
                        isCompleted = item.optBoolean("isCompleted", false),
                        updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val journalList = mutableListOf<JournalEntryEntity>()
            val journalArr = root.optJSONArray("journal_entries") ?: JSONArray()
            for (i in 0 until journalArr.length()) {
                val j = journalArr.getJSONObject(i)
                journalList.add(
                    JournalEntryEntity(
                        id = j.optLong("id", 0L),
                        dateEpochDay = j.optLong("dateEpochDay", 0L),
                        content = j.optString("content", ""),
                        mood = j.optString("mood").ifBlank { null },
                        createdAt = j.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = j.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val reviewsList = mutableListOf<WeeklyReviewEntity>()
            val reviewsArr = root.optJSONArray("weekly_reviews") ?: JSONArray()
            for (i in 0 until reviewsArr.length()) {
                val r = reviewsArr.getJSONObject(i)
                reviewsList.add(
                    WeeklyReviewEntity(
                        id = r.optLong("id", 0L),
                        weekStartEpochDay = r.optLong("weekStartEpochDay", 0L),
                        weekEndEpochDay = r.optLong("weekEndEpochDay", 0L),
                        completionRate = r.optDouble("completionRate", 0.0).toFloat(),
                        bestStreak = r.optInt("bestStreak", 0),
                        totalCracks = r.optInt("totalCracks", 0),
                        totalRepairs = r.optInt("totalRepairs", 0),
                        focusMinutes = r.optInt("focusMinutes", 0),
                        mostConsistentHabitName = r.optString("mostConsistentHabitName").ifBlank { null },
                        leastConsistentHabitName = r.optString("leastConsistentHabitName").ifBlank { null },
                        goalsProgressSummary = r.optString("goalsProgressSummary", ""),
                        reflection1 = r.optString("reflection1", ""),
                        reflection2 = r.optString("reflection2", ""),
                        reflection3 = r.optString("reflection3", ""),
                        createdAt = r.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val homePrefsObj = root.optJSONObject("home_preferences")
            val homeSectionOrder = homePrefsObj?.optString("section_order")?.ifBlank { null }
            val hiddenHomeSections = homePrefsObj?.optString("hidden_sections")
            val quickActionsOrder = homePrefsObj?.optString("quick_actions_order")?.ifBlank { null }

            val appearanceObj = root.optJSONObject("appearance_preferences")
            val appTheme = appearanceObj?.optString("app_theme")?.ifBlank { null }
            val contentDensity = appearanceObj?.optString("content_density")?.ifBlank { null }
            val hapticsEnabled = if (appearanceObj?.has("haptics_enabled") == true) appearanceObj.optBoolean("haptics_enabled") else null
            val reducedMotion = if (appearanceObj?.has("reduced_motion") == true) appearanceObj.optBoolean("reduced_motion") else null

            val groupsList = mutableListOf<AppGroupEntity>()
            val groupsArr = root.optJSONArray("app_groups") ?: JSONArray()
            for (i in 0 until groupsArr.length()) {
                val g = groupsArr.getJSONObject(i)
                groupsList.add(
                    AppGroupEntity(
                        id = g.optLong("id", 0L),
                        name = g.optString("name", ""),
                        categoryType = g.optString("categoryType", "CUSTOM"),
                        isPredefined = g.optBoolean("isPredefined", false),
                        packageNames = g.optString("packageNames", "")
                    )
                )
            }

            val rulesList = mutableListOf<BlockRuleEntity>()
            val rulesArr = root.optJSONArray("block_rules") ?: JSONArray()
            for (i in 0 until rulesArr.length()) {
                val r = rulesArr.getJSONObject(i)
                rulesList.add(
                    BlockRuleEntity(
                        id = r.optLong("id", 0L),
                        appGroupId = r.optLong("appGroupId", 0L),
                        isEnabled = r.optBoolean("isEnabled", true),
                        mode = r.optString("mode", "DAILY_LIMIT"),
                        dailyLimitMinutes = r.optInt("dailyLimitMinutes", 60),
                        maxOpensPerDay = r.optInt("maxOpensPerDay", 0),
                        strictnessLevel = r.optString("strictnessLevel", "GENTLE"),
                        pauseBreathingSeconds = r.optInt("pauseBreathingSeconds", 10),
                        pauseQuestion = r.optString("pauseQuestion", "What are you here for?")
                    )
                )
            }

            val schedulesList = mutableListOf<BlockScheduleEntity>()
            val schedulesArr = root.optJSONArray("block_schedules") ?: JSONArray()
            for (i in 0 until schedulesArr.length()) {
                val s = schedulesArr.getJSONObject(i)
                schedulesList.add(
                    BlockScheduleEntity(
                        id = s.optLong("id", 0L),
                        ruleId = s.optLong("ruleId", 0L),
                        startMinuteOfDay = s.optInt("startMinuteOfDay", 1320),
                        endMinuteOfDay = s.optInt("endMinuteOfDay", 420),
                        daysOfWeekMask = s.optInt("daysOfWeekMask", 127),
                        isEnabled = s.optBoolean("isEnabled", true)
                    )
                )
            }

            val detoxList = mutableListOf<DigitalDetoxEntity>()
            val detoxArr = root.optJSONArray("detox_sessions") ?: JSONArray()
            for (i in 0 until detoxArr.length()) {
                val d = detoxArr.getJSONObject(i)
                detoxList.add(
                    DigitalDetoxEntity(
                        id = d.optLong("id", 0L),
                        startTimeMillis = d.optLong("startTimeMillis", System.currentTimeMillis()),
                        durationMinutes = d.optInt("durationMinutes", 60),
                        endTimeMillis = d.optLong("endTimeMillis", System.currentTimeMillis() + 3600000L),
                        intention = d.optString("intention").ifBlank { null },
                        allowedPackages = d.optString("allowedPackages", ""),
                        enableDnd = d.optBoolean("enableDnd", true),
                        isCompleted = d.optBoolean("isCompleted", false)
                    )
                )
            }

            val quitHabitsList = mutableListOf<QuitHabitEntity>()
            val quitHabitsArr = root.optJSONArray("quit_habits") ?: JSONArray()
            for (i in 0 until quitHabitsArr.length()) {
                val q = quitHabitsArr.getJSONObject(i)
                quitHabitsList.add(
                    QuitHabitEntity(
                        id = q.optLong("id", 0L),
                        name = q.optString("name", ""),
                        iconName = q.optString("iconName", "shield"),
                        colorHex = q.optString("colorHex", "#B8935A"),
                        cleanStreakStartMillis = q.optLong("cleanStreakStartMillis", System.currentTimeMillis()),
                        bestStreakDays = q.optInt("bestStreakDays", 0),
                        totalSlips = q.optInt("totalSlips", 0),
                        totalRepairs = q.optInt("totalRepairs", 0),
                        ifThenPlan = q.optString("ifThenPlan").ifBlank { null },
                        emergencyContactName = q.optString("emergencyContactName").ifBlank { null },
                        emergencyContactPhone = q.optString("emergencyContactPhone").ifBlank { null }
                    )
                )
            }

            BackupValidation(
                isValid = true,
                data = BackupData(
                    schemaVersion = schemaVersion,
                    exportedAt = root.optLong("exportedAt", System.currentTimeMillis()),
                    habits = habitsList,
                    logs = logsList,
                    tasks = tasksList,
                    goals = goalsList,
                    focusSessions = focusList,
                    dailyIntentions = intentionList,
                    journalEntries = journalList,
                    weeklyReviews = reviewsList,
                    subtasks = subtasksList,
                    taskCompletions = taskCompletionsList,
                    homeSectionOrder = homeSectionOrder,
                    hiddenHomeSections = hiddenHomeSections,
                    quickActionsOrder = quickActionsOrder,
                    appTheme = appTheme,
                    contentDensity = contentDensity,
                    hapticsEnabled = hapticsEnabled,
                    reducedMotion = reducedMotion,
                    appGroups = groupsList,
                    blockRules = rulesList,
                    blockSchedules = schedulesList,
                    detoxSessions = detoxList,
                    quitHabits = quitHabitsList
                )
            )
        } catch (e: Exception) {
            BackupValidation(
                isValid = false,
                errorMessage = "Corrupt backup file: ${e.localizedMessage ?: "Failed to parse JSON"}"
            )
        }
    }

    suspend fun applyBackup(
        database: KintsuDatabase,
        data: BackupData,
        mode: ImportMode,
        settingsStore: SettingsDataStore? = null
    ) {
        val habitDao = database.habitDao()
        val taskDao = database.taskDao()
        val goalDao = database.goalDao()
        val focusDao = database.focusDao()
        val dailyIntentionDao = database.dailyIntentionDao()
        val journalDao = database.journalDao()
        val weeklyReviewDao = database.weeklyReviewDao()
        val subtaskDao = database.subtaskDao()
        val taskCompletionDao = database.taskCompletionDao()

        if (settingsStore != null) {
            if (data.homeSectionOrder != null) {
                settingsStore.setHomeSectionOrder(data.homeSectionOrder)
                data.hiddenHomeSections?.let { settingsStore.setHiddenHomeSections(it) }
                data.quickActionsOrder?.let { settingsStore.setQuickActionsOrder(it) }
            }
            if (data.appTheme != null) {
                try {
                    val theme = com.kintsu.app.ui.theme.AppTheme.valueOf(data.appTheme)
                    settingsStore.setAppTheme(theme)
                } catch (e: Exception) {
                    // Ignore unknown theme enum in older or newer builds
                }
            }
            if (data.contentDensity != null) {
                try {
                    val density = ContentDensity.valueOf(data.contentDensity)
                    settingsStore.setContentDensity(density)
                } catch (e: Exception) {
                    // Ignore
                }
            }
            if (data.hapticsEnabled != null) {
                settingsStore.setHapticsEnabled(data.hapticsEnabled)
            }
            if (data.reducedMotion != null) {
                settingsStore.setReducedMotion(data.reducedMotion)
            }
        }

        if (mode == ImportMode.REPLACE) {
            habitDao.clearAllLogs()
            habitDao.clearAllHabits()
            taskDao.clearAllTasks()
            goalDao.clearAllGoals()
            focusDao.clearAllSessions()
            dailyIntentionDao.clearAll()
            journalDao.clearAll()
            weeklyReviewDao.clearAll()
            subtaskDao.clearAllSubtasks()
            taskCompletionDao.clearAllCompletions()

            data.habits.forEach { habitDao.insertHabit(it) }
            data.logs.forEach { habitDao.upsertLog(it) }
            data.tasks.forEach { taskDao.insertTask(it) }
            data.subtasks.forEach { subtaskDao.insertSubtask(it) }
            data.taskCompletions.forEach { taskCompletionDao.insertCompletion(it) }
            data.goals.forEach { goalDao.insertGoal(it) }
            data.focusSessions.forEach { focusDao.insertSession(it) }
            data.dailyIntentions.forEach { dailyIntentionDao.insertOrUpdate(it) }
            data.journalEntries.forEach { journalDao.insertOrUpdate(it) }
            data.weeklyReviews.forEach { weeklyReviewDao.insertOrUpdate(it) }

            val wellbeingDao = database.wellbeingDao()
            wellbeingDao.clearBlockRules()
            wellbeingDao.clearAppGroups()
            data.appGroups.forEach { wellbeingDao.insertAppGroup(it) }
            data.blockRules.forEach { wellbeingDao.insertBlockRule(it) }
            data.blockSchedules.forEach { wellbeingDao.insertSchedule(it) }
            data.detoxSessions.forEach { wellbeingDao.insertDetoxSession(it) }
            data.quitHabits.forEach { wellbeingDao.insertQuitHabit(it) }
        } else {
            // MERGE MODE
            val existingHabits = habitDao.getAllHabits().first().associateBy { it.name.trim().lowercase() }
            val habitIdMap = mutableMapOf<Long, Long>()

            data.habits.forEach { importedHabit ->
                val key = importedHabit.name.trim().lowercase()
                val existing = existingHabits[key]
                if (existing != null) {
                    habitIdMap[importedHabit.id] = existing.id
                } else {
                    val newId = habitDao.insertHabit(importedHabit.copy(id = 0L))
                    habitIdMap[importedHabit.id] = newId
                }
            }

            data.logs.forEach { log ->
                val targetHabitId = habitIdMap[log.habitId] ?: log.habitId
                val existingLog = habitDao.getLog(targetHabitId, log.epochDay)
                if (existingLog == null) {
                    habitDao.upsertLog(log.copy(id = 0L, habitId = targetHabitId))
                }
            }

            val existingTasks = taskDao.getAllTasks().first().associateBy { it.title.trim().lowercase() }
            val taskIdMap = mutableMapOf<Long, Long>()
            data.tasks.forEach { task ->
                val key = task.title.trim().lowercase()
                val existing = existingTasks[key]
                if (existing != null) {
                    taskIdMap[task.id] = existing.id
                } else {
                    val newId = taskDao.insertTask(task.copy(id = 0L))
                    taskIdMap[task.id] = newId
                }
            }

            data.subtasks.forEach { subtask ->
                val targetTaskId = taskIdMap[subtask.taskId] ?: subtask.taskId
                subtaskDao.insertSubtask(subtask.copy(id = 0L, taskId = targetTaskId))
            }

            data.taskCompletions.forEach { tc ->
                val targetTaskId = taskIdMap[tc.taskId] ?: tc.taskId
                taskCompletionDao.insertCompletion(tc.copy(id = 0L, taskId = targetTaskId))
            }

            val existingGoals = goalDao.getAllGoals().first().map { it.title.trim().lowercase() }.toSet()
            data.goals.forEach { goal ->
                if (!existingGoals.contains(goal.title.trim().lowercase())) {
                    goalDao.insertGoal(goal.copy(id = 0L))
                }
            }

            data.focusSessions.forEach { focus ->
                focusDao.insertSession(focus.copy(id = 0L))
            }

            val existingIntentions = dailyIntentionDao.getAllIntentions().first().associateBy { it.dateEpochDay }
            data.dailyIntentions.forEach { intention ->
                if (!existingIntentions.containsKey(intention.dateEpochDay)) {
                    dailyIntentionDao.insertOrUpdate(intention.copy(id = 0L))
                }
            }

            val existingJournals = journalDao.getAllEntries().first().associateBy { it.dateEpochDay }
            data.journalEntries.forEach { journal ->
                if (!existingJournals.containsKey(journal.dateEpochDay)) {
                    journalDao.insertOrUpdate(journal.copy(id = 0L))
                }
            }

            val existingReviews = weeklyReviewDao.getAllReviews().first().associateBy { it.weekStartEpochDay }
            data.weeklyReviews.forEach { review ->
                if (!existingReviews.containsKey(review.weekStartEpochDay)) {
                    weeklyReviewDao.insertOrUpdate(review.copy(id = 0L))
                }
            }

            val wellbeingDao = database.wellbeingDao()
            data.appGroups.forEach { wellbeingDao.insertAppGroup(it.copy(id = 0L)) }
            data.blockRules.forEach { wellbeingDao.insertBlockRule(it.copy(id = 0L)) }
            data.blockSchedules.forEach { wellbeingDao.insertSchedule(it.copy(id = 0L)) }
            data.detoxSessions.forEach { wellbeingDao.insertDetoxSession(it.copy(id = 0L)) }
            data.quitHabits.forEach { wellbeingDao.insertQuitHabit(it.copy(id = 0L)) }
        }

        if (settingsStore != null) {
            // Restore rules safely without auto-arming Strict mode until user confirms
            settingsStore.setWellbeingStrictness("GENTLE")
        }
    }

    fun generateHumanReadableSummary(data: BackupData): String {
        return buildString {
            appendLine("=== Kintsu Sanctuary Life Backup Summary ===")
            appendLine("Exported: ${java.time.Instant.ofEpochMilli(data.exportedAt)}")
            appendLine("• Habits: ${data.habits.size}")
            appendLine("• Completed Logs: ${data.logs.size}")
            appendLine("• Tasks: ${data.tasks.size}")
            appendLine("• Goals: ${data.goals.size}")
            appendLine("• Focus Sessions: ${data.focusSessions.size}")
            appendLine("• Daily Intentions: ${data.dailyIntentions.size}")
            appendLine("• Journal Reflections: ${data.journalEntries.size}")
            appendLine("• Weekly Reviews: ${data.weeklyReviews.size}")
            appendLine("• Digital Wellbeing App Groups: ${data.appGroups.size}")
            appendLine("• Active Blocking Rules: ${data.blockRules.size}")
            appendLine("• Quit Habit Trackers: ${data.quitHabits.size}")
            appendLine("• Digital Detox Sessions: ${data.detoxSessions.size}")
            appendLine("===========================================")
        }
    }
}
