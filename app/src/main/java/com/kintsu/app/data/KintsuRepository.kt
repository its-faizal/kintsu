package com.kintsu.app.data

import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.SubtaskEntity
import com.kintsu.app.data.model.TaskCompletionEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import com.kintsu.app.data.model.TaskRecurrenceType
import com.kintsu.app.data.model.WeeklyReviewEntity
import com.kintsu.app.domain.GoldRepairConfig
import com.kintsu.app.domain.GoldRepairManager
import com.kintsu.app.domain.HabitRepairState
import com.kintsu.app.domain.StreakCalculator
import com.kintsu.app.domain.task.TaskRecurrenceCalculator
import com.kintsu.app.sync.AuthSession
import com.kintsu.app.sync.KintsuCloudClient
import com.kintsu.app.sync.KintsuSyncEngine
import com.kintsu.app.sync.SyncResult
import com.kintsu.app.sync.model.SyncOperation
import com.kintsu.app.sync.model.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class ProgressStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalRepairsGold: Int,
    val totalCracks: Int = 0,
    val weeklyCompletionRate: Float, // 0.0 to 1.0
    val monthlyCompletionRate: Float,
    val totalHabitsDone: Int,
    val totalTasksDone: Int,
    val focusMinutesToday: Int = 0,
    val focusMinutesThisWeek: Int = 0,
    val focusSessionsCount: Int = 0
)

class KintsuRepository(
    private val database: KintsuDatabase,
    private val settingsDataStore: SettingsDataStore
) {
    private val habitDao = database.habitDao()
    private val taskDao = database.taskDao()
    private val goalDao = database.goalDao()
    private val focusDao = database.focusDao()
    private val dailyIntentionDao = database.dailyIntentionDao()
    private val journalDao = database.journalDao()
    private val weeklyReviewDao = database.weeklyReviewDao()
    private val subtaskDao = database.subtaskDao()
    private val taskCompletionDao = database.taskCompletionDao()
    private val syncDao = database.syncDao()

    val cloudClient = KintsuCloudClient()
    val syncEngine = KintsuSyncEngine(database, settingsDataStore, cloudClient)
    val syncState: StateFlow<SyncState> = syncEngine.syncState
    val syncError: StateFlow<String?> = syncEngine.lastError

    val activeHabits: Flow<List<HabitEntity>> = habitDao.getActiveHabits()
    val archivedHabits: Flow<List<HabitEntity>> = habitDao.getArchivedHabits()
    val allLogs: Flow<List<HabitLogEntity>> = habitDao.getAllLogs()
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allSubtasks: Flow<List<SubtaskEntity>> = subtaskDao.getAllSubtasks()
    val allTaskCompletions: Flow<List<TaskCompletionEntity>> = taskCompletionDao.getAllCompletions()
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()
    val allFocusSessions: Flow<List<FocusSessionEntity>> = focusDao.getAllSessions()
    val allDailyIntentions: Flow<List<DailyIntentionEntity>> = dailyIntentionDao.getAllIntentions()
    val allJournalEntries: Flow<List<JournalEntryEntity>> = journalDao.getAllEntries()
    val allWeeklyReviews: Flow<List<WeeklyReviewEntity>> = weeklyReviewDao.getAllReviews()
    val settingsFlow: Flow<KintsuSettings> = settingsDataStore.settingsFlow

    fun getLogsForDay(epochDay: Long): Flow<List<HabitLogEntity>> =
        habitDao.getLogsForDay(epochDay)

    fun getFocusSessionsForDay(epochDay: Long): Flow<List<FocusSessionEntity>> =
        focusDao.getSessionsForDay(epochDay)

    fun getDailyIntention(epochDay: Long): Flow<DailyIntentionEntity?> =
        dailyIntentionDao.getIntentionForDay(epochDay)

    fun getJournalEntry(epochDay: Long): Flow<JournalEntryEntity?> =
        journalDao.getEntryForDay(epochDay)

    fun getWeeklyReview(weekStartEpochDay: Long): Flow<WeeklyReviewEntity?> =
        weeklyReviewDao.getReviewForWeek(weekStartEpochDay)

    suspend fun addHabit(
        name: String,
        iconName: String,
        colorHex: String,
        frequencyType: FrequencyType,
        weekdaysMask: Int,
        reminderTime: String?,
        reminderTimes: String = "",
        frequencyTarget: Int = 1,
        routineGroup: String? = null,
        linkedGoalId: Long? = null
    ): Long {
        val habit = HabitEntity(
            name = name.trim(),
            iconName = iconName,
            colorHex = colorHex,
            frequencyType = frequencyType,
            weekdaysMask = weekdaysMask,
            reminderTime = reminderTime,
            reminderTimes = reminderTimes,
            frequencyTarget = frequencyTarget,
            routineGroup = routineGroup,
            linkedGoalId = linkedGoalId
        )
        val id = habitDao.insertHabit(habit)
        syncEngine.recordLocalMutation("habits", id, SyncOperation.UPSERT, syncEngine.serializeHabit(habit.copy(id = id)))
        return id
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
        syncEngine.recordLocalMutation("habits", habit.id, SyncOperation.UPSERT, syncEngine.serializeHabit(habit))
    }

    suspend fun archiveHabit(habitId: Long, archive: Boolean) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val updated = habit.copy(isArchived = archive)
        habitDao.updateHabit(updated)
        syncEngine.recordLocalMutation("habits", habit.id, SyncOperation.UPSERT, syncEngine.serializeHabit(updated))
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
        syncEngine.recordLocalMutation("habits", habit.id, SyncOperation.DELETE, "{}")
    }

    suspend fun restoreHabit(habit: HabitEntity) {
        habitDao.insertHabit(habit)
    }

    suspend fun toggleHabitCompletion(
        habitId: Long,
        epochDay: Long,
        note: String? = null,
        mood: String? = null
    ) {
        val current = habitDao.getLog(habitId, epochDay)
        if (current == null) {
            habitDao.upsertLog(
                HabitLogEntity(
                    habitId = habitId,
                    epochDay = epochDay,
                    isCompleted = true,
                    isRepairedGold = false,
                    note = note,
                    mood = mood
                )
            )
        } else {
            val nextCompleted = !current.isCompleted
            habitDao.upsertLog(
                current.copy(
                    isCompleted = nextCompleted,
                    isRepairedGold = if (nextCompleted) current.isRepairedGold else false,
                    note = if (nextCompleted) note ?: current.note else null,
                    mood = if (nextCompleted) mood ?: current.mood else null
                )
            )
        }
    }

    /**
     * Kintsugi Repair: missed days are mended into gold!
     */
    suspend fun mendHabitDayWithGold(habitId: Long, epochDay: Long) {
        val current = habitDao.getLog(habitId, epochDay)
        val now = System.currentTimeMillis()
        if (current == null) {
            habitDao.upsertLog(
                HabitLogEntity(
                    habitId = habitId,
                    epochDay = epochDay,
                    isCompleted = true,
                    isRepairedGold = true,
                    repairedAt = now
                )
            )
        } else {
            habitDao.upsertLog(
                current.copy(
                    isCompleted = true,
                    isRepairedGold = true,
                    repairedAt = now
                )
            )
        }
    }

    // Tasks
    suspend fun addTask(
        title: String,
        dueDateEpochDay: Long?,
        priority: TaskPriority,
        linkedGoalId: Long? = null,
        recurrenceType: TaskRecurrenceType = TaskRecurrenceType.NEVER,
        recurrenceWeekdaysMask: Int = 0,
        recurrenceActive: Boolean = true,
        plannedDateEpochDay: Long? = null,
        plannedTimeBlock: String? = null,
        planOrder: Int = 0
    ): Long {
        val task = TaskEntity(
            title = title.trim(),
            dueDateEpochDay = dueDateEpochDay,
            priority = priority,
            linkedGoalId = linkedGoalId,
            recurrenceType = recurrenceType,
            recurrenceWeekdaysMask = recurrenceWeekdaysMask,
            recurrenceActive = recurrenceActive,
            plannedDateEpochDay = plannedDateEpochDay,
            plannedTimeBlock = plannedTimeBlock,
            planOrder = planOrder
        )
        val id = taskDao.insertTask(task)
        syncEngine.recordLocalMutation("tasks", id, SyncOperation.UPSERT, syncEngine.serializeTask(task.copy(id = id)))
        return id
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
        syncEngine.recordLocalMutation("tasks", task.id, SyncOperation.UPSERT, syncEngine.serializeTask(task))
    }

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        if (task.isRecurring && task.recurrenceActive && !task.isCompleted) {
            val today = LocalDate.now().toEpochDay()
            val tcId = taskCompletionDao.insertCompletion(
                TaskCompletionEntity(
                    taskId = task.id,
                    epochDay = today,
                    completedAt = System.currentTimeMillis()
                )
            )
            syncEngine.recordLocalMutation("task_completions", tcId, SyncOperation.UPSERT, syncEngine.serializeTaskCompletion(TaskCompletionEntity(id = tcId, taskId = task.id, epochDay = today, completedAt = System.currentTimeMillis())))
            val currentDue = task.dueDateEpochDay ?: today
            val nextDue = TaskRecurrenceCalculator.computeNextDueDate(
                currentEpochDay = currentDue,
                recurrenceType = task.recurrenceType,
                weekdaysMask = task.recurrenceWeekdaysMask
            )
            val updated = task.copy(
                dueDateEpochDay = nextDue,
                isCompleted = false,
                completedAt = null,
                plannedDateEpochDay = null
            )
            taskDao.updateTask(updated)
            syncEngine.recordLocalMutation("tasks", task.id, SyncOperation.UPSERT, syncEngine.serializeTask(updated))
        } else {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            taskDao.updateTask(updated)
            syncEngine.recordLocalMutation("tasks", task.id, SyncOperation.UPSERT, syncEngine.serializeTask(updated))
        }
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
        syncEngine.recordLocalMutation("tasks", task.id, SyncOperation.DELETE, "{}")
    }

    suspend fun restoreTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun planTask(taskId: Long, epochDay: Long?, timeBlock: String?, order: Int = 0) {
        val task = taskDao.getTaskById(taskId) ?: return
        taskDao.updateTask(
            task.copy(
                plannedDateEpochDay = epochDay,
                plannedTimeBlock = timeBlock,
                planOrder = order
            )
        )
    }

    suspend fun rescheduleTask(taskId: Long, newDueDate: Long?) {
        val task = taskDao.getTaskById(taskId) ?: return
        taskDao.updateTask(
            task.copy(
                dueDateEpochDay = newDueDate,
                plannedDateEpochDay = if (newDueDate != null && task.plannedDateEpochDay != null) newDueDate else task.plannedDateEpochDay
            )
        )
    }

    // Subtasks
    fun getSubtasksForTask(taskId: Long): Flow<List<SubtaskEntity>> = subtaskDao.getSubtasksForTask(taskId)

    suspend fun addSubtask(taskId: Long, title: String, orderIndex: Int = 0): Long {
        return subtaskDao.insertSubtask(
            SubtaskEntity(
                taskId = taskId,
                title = title.trim(),
                orderIndex = orderIndex,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateSubtask(subtask: SubtaskEntity) {
        subtaskDao.updateSubtask(subtask)
    }

    suspend fun toggleSubtask(subtask: SubtaskEntity) {
        subtaskDao.updateSubtask(subtask.copy(isCompleted = !subtask.isCompleted))
    }

    suspend fun deleteSubtask(subtask: SubtaskEntity) {
        subtaskDao.deleteSubtask(subtask)
    }

    suspend fun reorderSubtasks(subtasks: List<SubtaskEntity>) {
        subtasks.forEachIndexed { index, subtask ->
            if (subtask.orderIndex != index) {
                subtaskDao.updateSubtask(subtask.copy(orderIndex = index))
            }
        }
    }

    // Goals
    suspend fun addGoal(
        title: String,
        targetDateEpochDay: Long,
        progressType: GoalProgressType,
        manualProgressPercent: Int
    ): Long {
        return goalDao.insertGoal(
            GoalEntity(
                title = title.trim(),
                targetDateEpochDay = targetDateEpochDay,
                progressType = progressType,
                manualProgressPercent = manualProgressPercent.coerceIn(0, 100)
            )
        )
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
    }

    suspend fun restoreGoal(goal: GoalEntity) {
        goalDao.insertGoal(goal)
    }

    // Focus session operations
    suspend fun recordFocusSession(
        dateEpochDay: Long,
        durationMinutes: Int,
        linkedHabitId: Long?,
        linkedTaskId: Long? = null,
        isCompleted: Boolean,
        startTimeMillis: Long,
        endTimeMillis: Long
    ): Long {
        return focusDao.insertSession(
            FocusSessionEntity(
                dateEpochDay = dateEpochDay,
                durationMinutes = durationMinutes,
                linkedHabitId = linkedHabitId,
                linkedTaskId = linkedTaskId,
                isCompleted = isCompleted,
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis
            )
        )
    }

    // Daily Intention operations
    suspend fun saveDailyIntention(dateEpochDay: Long, text: String, isCompleted: Boolean = false): Long {
        val existing = dailyIntentionDao.getIntentionForDay(dateEpochDay).first()
        return dailyIntentionDao.insertOrUpdate(
            DailyIntentionEntity(
                id = existing?.id ?: 0,
                dateEpochDay = dateEpochDay,
                text = text.trim(),
                isCompleted = isCompleted,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleDailyIntention(intention: DailyIntentionEntity) {
        dailyIntentionDao.insertOrUpdate(
            intention.copy(
                isCompleted = !intention.isCompleted,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // Journal operations
    suspend fun saveJournalEntry(dateEpochDay: Long, content: String, mood: String?): Long {
        val existing = journalDao.getEntryForDay(dateEpochDay).first()
        return journalDao.insertOrUpdate(
            JournalEntryEntity(
                id = existing?.id ?: 0,
                dateEpochDay = dateEpochDay,
                content = content.trim(),
                mood = mood,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteJournalEntry(entry: JournalEntryEntity) {
        journalDao.delete(entry)
    }

    // Weekly Review operations
    suspend fun saveWeeklyReview(review: WeeklyReviewEntity): Long {
        return weeklyReviewDao.insertOrUpdate(review)
    }

    suspend fun deleteWeeklyReview(review: WeeklyReviewEntity) {
        weeklyReviewDao.delete(review)
    }

    // Settings actions
    suspend fun updateSettings(block: suspend (SettingsDataStore) -> Unit) {
        block(settingsDataStore)
    }

    // Stats calculations
    suspend fun calculateStats(): ProgressStats {
        val today = LocalDate.now().toEpochDay()
        val habits = habitDao.getActiveHabits().first()
        val logs = habitDao.getAllLogs().first()
        val tasks = taskDao.getAllTasks().first()
        val settings = settingsFlow.first()
        val focusSessions = focusDao.getAllSessions().first()
        val taskCompletions = taskCompletionDao.getAllCompletions().first()
        return computeStats(habits, logs, tasks, focusSessions, settings, today, taskCompletions)
    }

    companion object {
        fun computeStats(
            habits: List<HabitEntity>,
            logs: List<HabitLogEntity>,
            tasks: List<TaskEntity>,
            focusSessions: List<FocusSessionEntity>,
            settings: KintsuSettings,
            today: Long = LocalDate.now().toEpochDay(),
            taskCompletions: List<TaskCompletionEntity> = emptyList()
        ): ProgressStats {
            val completedTasks = tasks.count { it.isCompleted } + taskCompletions.size

            val streakResult = StreakCalculator.calculateStreaks(
                habits = habits,
                logs = logs,
                todayEpochDay = today,
                goldRepairEnabled = settings.goldRepairEnabled
            )

            // Weekly completion (last 7 days)
            val past7Days = (today - 6..today)
            val logsPast7 = logs.count { it.isCompleted && past7Days.contains(it.epochDay) }
            val habitsCount = habits.size.coerceAtLeast(1)
            val weeklyRate = (logsPast7.toFloat() / (habitsCount * 7).toFloat()).coerceIn(0f, 1f)

            // Monthly completion (last 30 days)
            val past30Days = (today - 29..today)
            val logsPast30 = logs.count { it.isCompleted && past30Days.contains(it.epochDay) }
            val monthlyRate = (logsPast30.toFloat() / (habitsCount * 30).toFloat()).coerceIn(0f, 1f)

            // Focus statistics
            val completedFocusSessions = focusSessions.filter { it.isCompleted }
            val focusMinutesToday = completedFocusSessions
                .filter { it.dateEpochDay == today }
                .sumOf { it.durationMinutes }
            val focusMinutesThisWeek = completedFocusSessions
                .filter { past7Days.contains(it.dateEpochDay) }
                .sumOf { it.durationMinutes }
            val focusSessionsCount = completedFocusSessions.size

            return ProgressStats(
                currentStreak = streakResult.currentStreak,
                bestStreak = streakResult.bestStreak,
                totalRepairsGold = streakResult.totalRepairs,
                totalCracks = streakResult.totalCracks,
                weeklyCompletionRate = weeklyRate,
                monthlyCompletionRate = monthlyRate,
                totalHabitsDone = logs.count { it.isCompleted },
                totalTasksDone = completedTasks,
                focusMinutesToday = focusMinutesToday,
                focusMinutesThisWeek = focusMinutesThisWeek,
                focusSessionsCount = focusSessionsCount
            )
        }
    }

    // Export as clean JSON
    suspend fun exportDataAsJson(): String {
        val root = JSONObject()
        val habitsArr = JSONArray()
        habitDao.getAllHabits().first().forEach { h ->
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("name", h.name)
            obj.put("frequency", h.frequencyType.name)
            obj.put("isArchived", h.isArchived)
            habitsArr.put(obj)
        }
        root.put("habits", habitsArr)

        val tasksArr = JSONArray()
        taskDao.getAllTasks().first().forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("isCompleted", t.isCompleted)
            obj.put("priority", t.priority.name)
            tasksArr.put(obj)
        }
        root.put("tasks", tasksArr)

        val goalsArr = JSONArray()
        goalDao.getAllGoals().first().forEach { g ->
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("title", g.title)
            obj.put("targetDate", g.targetDateEpochDay)
            goalsArr.put(obj)
        }
        root.put("goals", goalsArr)

        return root.toString(2)
    }

    suspend fun resetAllData() {
        habitDao.clearAllLogs()
        habitDao.clearAllHabits()
        taskDao.clearAllTasks()
        subtaskDao.clearAllSubtasks()
        taskCompletionDao.clearAllCompletions()
        goalDao.clearAllGoals()
        focusDao.clearAllSessions()
        dailyIntentionDao.clearAll()
        journalDao.clearAll()
        weeklyReviewDao.clearAll()
        settingsDataStore.resetSettings()
    }

    // Default Seed
    suspend fun seedDefaultsIfEmpty() {
        val habits = habitDao.getActiveHabits().first()
        if (habits.isEmpty()) {
            val h1 = habitDao.insertHabit(
                HabitEntity(name = "Morning Tea & Stillness", iconName = "tea", colorHex = "#B8935A")
            )
            val h2 = habitDao.insertHabit(
                HabitEntity(name = "Kettlebell Movement", iconName = "movement", colorHex = "#6B8E7D")
            )
            val h3 = habitDao.insertHabit(
                HabitEntity(name = "Read 10 Pages", iconName = "reading", colorHex = "#81729E")
            )

            val today = LocalDate.now().toEpochDay()
            // Mark today's first habit complete
            habitDao.upsertLog(HabitLogEntity(habitId = h1, epochDay = today, isCompleted = true))

            // Seed historical completions and a repaired gold day
            for (i in 1..12) {
                val d = today - i
                if (i == 4) {
                    // Kintsugi gold repair day!
                    habitDao.upsertLog(HabitLogEntity(habitId = h1, epochDay = d, isCompleted = true, isRepairedGold = true))
                    habitDao.upsertLog(HabitLogEntity(habitId = h2, epochDay = d, isCompleted = true, isRepairedGold = true))
                } else if (i % 5 != 0) {
                    habitDao.upsertLog(HabitLogEntity(habitId = h1, epochDay = d, isCompleted = true))
                    habitDao.upsertLog(HabitLogEntity(habitId = h2, epochDay = d, isCompleted = true))
                }
            }

            // Seed 1 Horizon Goal
            val goalId = goalDao.insertGoal(
                GoalEntity(
                    title = "Cultivate Physical & Mental Stillness",
                    targetDateEpochDay = today + 60,
                    progressType = GoalProgressType.AUTO_FROM_ITEMS
                )
            )

            // Seed Tasks
            taskDao.insertTask(
                TaskEntity(
                    title = "Review quarterly intentions",
                    dueDateEpochDay = today,
                    priority = TaskPriority.HIGH,
                    linkedGoalId = goalId
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Quiet walk without phone",
                    dueDateEpochDay = today + 1,
                    priority = TaskPriority.MEDIUM,
                    linkedGoalId = goalId
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Order fresh ceramic matcha bowl",
                    dueDateEpochDay = today + 4,
                    priority = TaskPriority.LOW
                )
            )
        }
    }

    // --- V3.0 Cloud Sync & Account Operations ---

    suspend fun setCloudConfig(url: String, key: String) {
        settingsDataStore.setCloudConfig(url, key)
        cloudClient.updateConfig(url, key)
    }

    private suspend fun syncCloudClientConfig() {
        val settings = settingsDataStore.settingsFlow.first()
        cloudClient.updateConfig(settings.cloudEndpointUrl, settings.cloudApiKey)
    }

    suspend fun performSync(): Result<SyncResult> {
        syncCloudClientConfig()
        return syncEngine.performSync()
    }

    suspend fun signIn(email: String, pass: String): Result<AuthSession> {
        syncCloudClientConfig()
        val result = cloudClient.signInWithPassword(email, pass)
        if (result.isSuccess) {
            val session = result.getOrThrow()
            settingsDataStore.setAccount(
                email = session.email,
                displayName = session.displayName,
                userId = session.userId,
                token = session.accessToken,
                refreshToken = session.refreshToken
            )
            settingsDataStore.setSyncEnabled(true)
            syncEngine.claimAndMigrateLocalData()
            syncEngine.performSync()
        }
        return result
    }

    suspend fun signUp(email: String, pass: String, name: String?): Result<AuthSession> {
        syncCloudClientConfig()
        val result = cloudClient.signUp(email, pass, name)
        if (result.isSuccess) {
            val session = result.getOrThrow()
            settingsDataStore.setAccount(
                email = session.email,
                displayName = session.displayName,
                userId = session.userId,
                token = session.accessToken,
                refreshToken = session.refreshToken
            )
            settingsDataStore.setSyncEnabled(true)
            syncEngine.claimAndMigrateLocalData()
            syncEngine.performSync()
        }
        return result
    }

    suspend fun signInWithGoogle(idToken: String, email: String? = null, name: String? = null): Result<AuthSession> {
        syncCloudClientConfig()
        val result = cloudClient.signInWithGoogleToken(idToken, email, name)
        if (result.isSuccess) {
            val session = result.getOrThrow()
            settingsDataStore.setAccount(
                email = session.email,
                displayName = session.displayName,
                userId = session.userId,
                token = session.accessToken,
                refreshToken = session.refreshToken
            )
            settingsDataStore.setSyncEnabled(true)
            syncEngine.claimAndMigrateLocalData()
            syncEngine.performSync()
        }
        return result
    }

    suspend fun signOut(keepLocalData: Boolean = true) {
        val settings = settingsDataStore.settingsFlow.first()
        val token = settings.accountAuthToken
        if (!token.isNullOrBlank()) {
            cloudClient.signOut(token)
        }
        settingsDataStore.signOutAccount()
        if (!keepLocalData) {
            resetAllData()
            database.syncDao().clearRegistry()
            database.syncDao().clearQueue()
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        settingsDataStore.setSyncEnabled(enabled)
        if (enabled) {
            syncEngine.claimAndMigrateLocalData()
            syncEngine.performSync()
        }
    }

    suspend fun deleteCloudData(): Result<Unit> {
        val settings = settingsDataStore.settingsFlow.first()
        val token = settings.accountAuthToken
        val userId = settings.accountUserId
        if (token.isNullOrBlank() || userId.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Not signed in"))
        }
        val result = cloudClient.deleteCloudData(token, userId)
        if (result.isSuccess) {
            settingsDataStore.setLastSyncTime(0L)
        }
        return result
    }
}
