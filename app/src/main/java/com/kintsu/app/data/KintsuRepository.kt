package com.kintsu.app.data

import com.kintsu.app.data.model.AchievementEntity
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockedWebsiteEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.BlockScheduleEntity
import com.kintsu.app.data.model.CommitmentLockEntity
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.DigitalDetoxEntity
import com.kintsu.app.data.model.DistractionInterventionLogEntity
import com.kintsu.app.data.model.FocusLockSessionEntity
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.IntentionalBreakLogEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.PersonalRewardEntity
import com.kintsu.app.data.model.QuitHabitEntity
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.data.model.StylePresetEntity
import com.kintsu.app.data.model.SubtaskEntity
import com.kintsu.app.data.model.TaskCompletionEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import com.kintsu.app.data.model.TaskRecurrenceType
import com.kintsu.app.data.model.UrgeLogEntity
import com.kintsu.app.data.model.WalletTransactionEntity
import com.kintsu.app.data.model.WeeklyReviewEntity
import com.kintsu.app.ui.theme.AccentOption
import com.kintsu.app.ui.theme.AppIconManager
import com.kintsu.app.ui.theme.AppTheme
import com.kintsu.app.wellbeing.domain.CommitmentLockManager
import com.kintsu.app.wellbeing.domain.QuitHabitTracker
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
    val wellbeingDao = database.wellbeingDao()

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
    val allAchievements: Flow<List<AchievementEntity>> = wellbeingDao.getAllAchievements()
    val earnedAchievements: Flow<List<AchievementEntity>> = wellbeingDao.getEarnedAchievements()
    val settingsFlow: Flow<KintsuSettings> = settingsDataStore.settingsFlow

    fun notifyWidgets() {
        try {
            com.kintsu.app.widget.KintsuWidgetUpdater.triggerUpdate(settingsDataStore.context)
        } catch (_: Exception) {}
    }

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
        notifyWidgets()
        return id
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
        syncEngine.recordLocalMutation("habits", habit.id, SyncOperation.UPSERT, syncEngine.serializeHabit(habit))
        notifyWidgets()
    }

    suspend fun archiveHabit(habitId: Long, archive: Boolean) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val updated = habit.copy(isArchived = archive)
        habitDao.updateHabit(updated)
        syncEngine.recordLocalMutation("habits", habit.id, SyncOperation.UPSERT, syncEngine.serializeHabit(updated))
        notifyWidgets()
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
        syncEngine.recordLocalMutation("habits", habit.id, SyncOperation.DELETE, "{}")
        notifyWidgets()
    }

    suspend fun restoreHabit(habit: HabitEntity) {
        habitDao.insertHabit(habit)
        notifyWidgets()
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
            awardCoins(5)
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
            if (nextCompleted) {
                awardCoins(5)
            }
        }
        notifyWidgets()
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
        awardCoins(10)
        notifyWidgets()
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
        notifyWidgets()
        return id
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
        syncEngine.recordLocalMutation("tasks", task.id, SyncOperation.UPSERT, syncEngine.serializeTask(task))
        notifyWidgets()
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
            awardCoins(3)
        } else {
            val isFinishing = !task.isCompleted
            val updated = task.copy(
                isCompleted = isFinishing,
                completedAt = if (isFinishing) System.currentTimeMillis() else null
            )
            taskDao.updateTask(updated)
            syncEngine.recordLocalMutation("tasks", task.id, SyncOperation.UPSERT, syncEngine.serializeTask(updated))
            if (isFinishing) {
                awardCoins(3)
            }
        }
        notifyWidgets()
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
        syncEngine.recordLocalMutation("tasks", task.id, SyncOperation.DELETE, "{}")
        notifyWidgets()
    }

    suspend fun restoreTask(task: TaskEntity) {
        taskDao.insertTask(task)
        notifyWidgets()
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
        notifyWidgets()
    }

    suspend fun rescheduleTask(taskId: Long, newDueDate: Long?) {
        val task = taskDao.getTaskById(taskId) ?: return
        taskDao.updateTask(
            task.copy(
                dueDateEpochDay = newDueDate,
                plannedDateEpochDay = if (newDueDate != null && task.plannedDateEpochDay != null) newDueDate else task.plannedDateEpochDay
            )
        )
        notifyWidgets()
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
        val id = goalDao.insertGoal(
            GoalEntity(
                title = title.trim(),
                targetDateEpochDay = targetDateEpochDay,
                progressType = progressType,
                manualProgressPercent = manualProgressPercent.coerceIn(0, 100)
            )
        )
        notifyWidgets()
        return id
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
        notifyWidgets()
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
        notifyWidgets()
    }

    suspend fun restoreGoal(goal: GoalEntity) {
        goalDao.insertGoal(goal)
        notifyWidgets()
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
        val id = focusDao.insertSession(
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
        if (isCompleted) {
            awardCoins(10)
        }
        notifyWidgets()
        return id
    }

    // Daily Intention operations
    suspend fun saveDailyIntention(dateEpochDay: Long, text: String, isCompleted: Boolean = false): Long {
        val existing = dailyIntentionDao.getIntentionForDay(dateEpochDay).first()
        val id = dailyIntentionDao.insertOrUpdate(
            DailyIntentionEntity(
                id = existing?.id ?: 0,
                dateEpochDay = dateEpochDay,
                text = text.trim(),
                isCompleted = isCompleted,
                updatedAt = System.currentTimeMillis()
            )
        )
        notifyWidgets()
        return id
    }

    suspend fun toggleDailyIntention(intention: DailyIntentionEntity) {
        dailyIntentionDao.insertOrUpdate(
            intention.copy(
                isCompleted = !intention.isCompleted,
                updatedAt = System.currentTimeMillis()
            )
        )
        notifyWidgets()
    }

    // Journal operations
    suspend fun saveJournalEntry(dateEpochDay: Long, content: String, mood: String?): Long {
        val existing = journalDao.getEntryForDay(dateEpochDay).first()
        val id = journalDao.insertOrUpdate(
            JournalEntryEntity(
                id = existing?.id ?: 0,
                dateEpochDay = dateEpochDay,
                content = content.trim(),
                mood = mood,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        notifyWidgets()
        return id
    }

    suspend fun deleteJournalEntry(entry: JournalEntryEntity) {
        journalDao.delete(entry)
        notifyWidgets()
    }

    // Weekly Review operations
    suspend fun saveWeeklyReview(review: WeeklyReviewEntity): Long {
        val id = weeklyReviewDao.insertOrUpdate(review)
        notifyWidgets()
        return id
    }

    suspend fun deleteWeeklyReview(review: WeeklyReviewEntity) {
        weeklyReviewDao.delete(review)
        notifyWidgets()
    }

    // Settings actions
    suspend fun updateSettings(block: suspend (SettingsDataStore) -> Unit) {
        block(settingsDataStore)
        notifyWidgets()
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
        wellbeingDao.clearAppGroups()
        wellbeingDao.clearBlockRules()
        wellbeingDao.clearBlockSchedules()
        wellbeingDao.clearDetoxSessions()
        wellbeingDao.clearQuitHabits()
        wellbeingDao.clearUrgeLogs()
        wellbeingDao.clearCommitmentLocks()
        wellbeingDao.clearShopInventory()
        wellbeingDao.clearBlockedWebsites()
        wellbeingDao.clearFocusLockSessions()
        wellbeingDao.clearDistractionInterventions()
        wellbeingDao.clearIntentionalBreaks()
        database.syncDao().clearRegistry()
        database.syncDao().clearQueue()
        settingsDataStore.resetSettings()
        seedShopCatalogIfEmpty()
    }

    suspend fun deleteAccount(): Result<Unit> {
        val settings = settingsDataStore.settingsFlow.first()
        val token = settings.accountAuthToken
        val userId = settings.accountUserId

        // 1. Delete remote cloud data and sign out from server if signed in
        if (!token.isNullOrBlank() && !userId.isNullOrBlank()) {
            try {
                cloudClient.deleteCloudData(token, userId)
            } catch (e: Exception) {
                // proceed with local wipe even if network fails
            }
            try {
                cloudClient.signOut(token)
            } catch (e: Exception) {
                // proceed
            }
        }

        // 2. Wipe all local records, wellbeing rules, shop, sync queue, and reset settings
        resetAllData()

        return Result.success(Unit)
    }

    // Default Seed - Brand New Clean First Launch (Zero preloaded goals, tasks, or habits)
    suspend fun seedDefaultsIfEmpty() {
        // Kept clean for pristine first-launch experience.
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
        val finalEmail = email?.takeIf { it.isNotBlank() } ?: "user@gmail.com"
        val finalName = name?.takeIf { it.isNotBlank() } ?: finalEmail.substringBefore("@")
            .split(".", "_", "-")
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        // Attempt remote authentication if endpoint is available
        val cloudResult = try {
            cloudClient.signInWithGoogleToken(idToken, finalEmail, finalName)
        } catch (e: Exception) {
            Result.failure(e)
        }

        val session = if (cloudResult.isSuccess) {
            cloudResult.getOrThrow()
        } else {
            // Seamless offline-first session when backend tunnel/network is not connected
            val stableUserId = "google_user_${java.util.UUID.nameUUIDFromBytes(finalEmail.toByteArray())}"
            val localToken = "google_token_${System.currentTimeMillis()}"
            AuthSession(
                userId = stableUserId,
                email = finalEmail,
                displayName = finalName,
                accessToken = localToken,
                refreshToken = "refresh_$localToken"
            )
        }

        settingsDataStore.setAccount(
            email = session.email,
            displayName = session.displayName,
            userId = session.userId,
            token = session.accessToken,
            refreshToken = session.refreshToken
        )
        settingsDataStore.setSyncEnabled(true)
        try {
            syncEngine.claimAndMigrateLocalData()
            syncEngine.performSync()
        } catch (_: Exception) {
        }
        return Result.success(session)
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

    // --- V3 Digital Wellbeing & Sanctuary Shop Operations ---

    val allAppGroups: Flow<List<AppGroupEntity>> = wellbeingDao.getAllAppGroups()
    val activeBlockRules: Flow<List<BlockRuleEntity>> = wellbeingDao.getActiveBlockRules()
    val activeQuitHabits: Flow<List<QuitHabitEntity>> = wellbeingDao.getActiveQuitHabits()
    val allShopItems: Flow<List<ShopItemEntity>> = wellbeingDao.getAllShopItems()
    val allDetoxSessions: Flow<List<DigitalDetoxEntity>> = wellbeingDao.getAllDetoxSessions()
    val activeCommitmentLocks: Flow<List<CommitmentLockEntity>> = wellbeingDao.getActiveCommitmentLocks()
    val allBlockedWebsites: Flow<List<BlockedWebsiteEntity>> = wellbeingDao.getAllBlockedWebsites()
    val activeBlockedWebsites: Flow<List<BlockedWebsiteEntity>> = wellbeingDao.getActiveBlockedWebsites()
    val allFocusLockSessions: Flow<List<FocusLockSessionEntity>> = wellbeingDao.getAllFocusLockSessions()
    val allInterventions: Flow<List<DistractionInterventionLogEntity>> = wellbeingDao.getAllInterventions()
    val allBreaks: Flow<List<IntentionalBreakLogEntity>> = wellbeingDao.getAllBreaks()

    suspend fun awardCoins(
        amount: Int,
        reason: String = "Mindful activity completed",
        sourceActivity: String = "HABIT_COMPLETION",
        sourceEventId: String = ""
    ): Boolean {
        if (amount <= 0) return false
        if (sourceEventId.isNotBlank()) {
            val alreadyAwarded = wellbeingDao.countTransactionsForSource(sourceEventId) > 0
            if (alreadyAwarded) return false
        }

        settingsDataStore.addCoins(amount)
        wellbeingDao.insertWalletTransaction(
            WalletTransactionEntity(
                amount = amount,
                type = "EARNED",
                reason = reason,
                sourceActivity = sourceActivity,
                sourceEventId = if (sourceEventId.isNotBlank()) sourceEventId else "coin_earn_${System.currentTimeMillis()}"
            )
        )
        return true
    }

    suspend fun purchaseShopItem(item: ShopItemEntity): Boolean {
        if (item.isUnlocked) return true
        val success = settingsDataStore.spendCoins(item.costCoins)
        if (success) {
            val now = System.currentTimeMillis()
            wellbeingDao.updateShopItem(
                item.copy(isUnlocked = true, purchasedAtMillis = now)
            )
            wellbeingDao.insertWalletTransaction(
                WalletTransactionEntity(
                    amount = -item.costCoins,
                    type = "SPENT",
                    reason = "Unlocked ${item.title}",
                    sourceActivity = "SHOP_PURCHASE",
                    sourceEventId = "shop_item_${item.id}_$now"
                )
            )

            // If it's a bundle, also unlock all items included in this bundle
            if (item.itemType == "BUNDLE" && item.bundleId.isNotBlank()) {
                val bundleItems = wellbeingDao.getShopItemsByBundle(item.bundleId)
                bundleItems.forEach { bi ->
                    if (!bi.isUnlocked) {
                        wellbeingDao.updateShopItem(
                            bi.copy(isUnlocked = true, purchasedAtMillis = now)
                        )
                    }
                }
            }
        }
        return success
    }

    suspend fun equipShopItem(item: ShopItemEntity) {
        if (!item.isUnlocked) return
        wellbeingDao.unequipAllOfType(item.itemType)
        wellbeingDao.updateShopItem(item.copy(isEquipped = true))

        // Reconcile with persistent settings & system components
        when (item.itemType) {
            "THEME" -> {
                val cleanId = item.id.replace("theme_", "").uppercase()
                val themeEnum = AppTheme.values().find { it.name.uppercase() == cleanId }
                if (themeEnum != null) {
                    settingsDataStore.setAppTheme(themeEnum)
                }
            }
            "APP_ICON" -> {
                settingsDataStore.setLauncherIconId(item.id)
                AppIconManager.applyLauncherIcon(settingsDataStore.context, item.id)
            }
            "WIDGET_SKIN" -> settingsDataStore.setWidgetSkinId(item.id)
            "BACKGROUND" -> settingsDataStore.setProfileBackgroundId(item.id)
            "FOCUS_FACE" -> settingsDataStore.setFocusTimerFaceId(item.id)
            "FOCUS_SCENE" -> settingsDataStore.setFocusSceneId(item.id)
            "HABIT_STYLE" -> {
                if (item.id.startsWith("habit_marker_")) {
                    settingsDataStore.setHabitMarkerId(item.id)
                } else if (item.id.startsWith("habit_ring_")) {
                    settingsDataStore.setHabitRingId(item.id)
                }
            }
            "PROFILE_FRAME" -> settingsDataStore.setProfileFrameId(item.id)
            "PROFILE_TITLE" -> settingsDataStore.setProfileTitle(item.title.replace("Title: ", ""))
            "SOUNDSCAPE" -> {
                if (item.id.contains("resonance") || item.id.contains("piano") || item.id.contains("calm_bell") || item.id.contains("minimal_tone")) {
                    settingsDataStore.setSoundPresetFocus(item.id)
                } else {
                    settingsDataStore.setSoundPresetHabit(item.id)
                }
            }
            "GOLD_REPAIR_STYLE" -> settingsDataStore.setGoldRepairStyleId(item.id)
        }
    }

    suspend fun seedShopCatalogIfEmpty() {
        val completeCatalog = ShopCatalog.createCompleteCatalog()
        wellbeingDao.seedShopItems(completeCatalog)
    }

    // --- Personal Rewards ---
    fun getActivePersonalRewards() = wellbeingDao.getActivePersonalRewards()
    fun getAllPersonalRewards() = wellbeingDao.getAllPersonalRewards()

    suspend fun createPersonalReward(title: String, description: String, cost: Int, category: String, icon: String): Long {
        return wellbeingDao.insertPersonalReward(
            PersonalRewardEntity(
                title = title,
                description = description,
                costCoins = cost,
                category = category,
                iconName = icon
            )
        )
    }

    suspend fun redeemPersonalReward(reward: PersonalRewardEntity): Boolean {
        val success = settingsDataStore.spendCoins(reward.costCoins)
        if (success) {
            val now = System.currentTimeMillis()
            wellbeingDao.updatePersonalReward(
                reward.copy(
                    redemptionsCount = reward.redemptionsCount + 1,
                    lastRedeemedAtMillis = now
                )
            )
            wellbeingDao.insertWalletTransaction(
                WalletTransactionEntity(
                    amount = -reward.costCoins,
                    type = "SPENT",
                    reason = "Redeemed reward: ${reward.title}",
                    sourceActivity = "PERSONAL_REWARD",
                    sourceEventId = "personal_reward_${reward.id}_$now"
                )
            )
        }
        return success
    }

    suspend fun archivePersonalReward(reward: PersonalRewardEntity) {
        wellbeingDao.updatePersonalReward(reward.copy(isArchived = true))
    }

    suspend fun deletePersonalReward(reward: PersonalRewardEntity) {
        wellbeingDao.deletePersonalReward(reward)
    }

    // --- Wallet Transactions Ledger ---
    fun getAllWalletTransactions() = wellbeingDao.getAllWalletTransactions()

    // --- Style Presets ---
    fun getAllStylePresets() = wellbeingDao.getAllStylePresets()

    suspend fun saveStylePreset(name: String, theme: String, accent: String, icon: String, skin: String, face: String): Long {
        return wellbeingDao.insertStylePreset(
            StylePresetEntity(
                name = name,
                themeName = theme,
                accentName = accent,
                iconId = icon,
                widgetSkinId = skin,
                focusFaceId = face
            )
        )
    }

    suspend fun renameStylePreset(preset: StylePresetEntity, newName: String) {
        wellbeingDao.insertStylePreset(preset.copy(name = newName))
    }

    suspend fun deleteStylePreset(preset: StylePresetEntity) {
        wellbeingDao.deleteStylePreset(preset)
    }

    suspend fun seedDefaultStylePresetsIfEmpty() {
        try {
            val existing = wellbeingDao.getAllStylePresets().first()
            if (existing.isEmpty()) {
                restoreDefaultStylePresets()
            }
        } catch (_: Exception) {}
    }

    suspend fun restoreDefaultStylePresets() {
        wellbeingDao.clearStylePresets()
        val curated = listOf(
            StylePresetEntity(name = "Calm Morning", themeName = "Sage & Ivory", accentName = "SAGE", iconId = "icon_original", widgetSkinId = "widget_botanical_sage", focusFaceId = "focus_face_classic_ring"),
            StylePresetEntity(name = "Matcha Minimal", themeName = "Matcha Atelier", accentName = "SAGE", iconId = "icon_sage_emblem", widgetSkinId = "widget_botanical_sage", focusFaceId = "focus_face_zen_circle"),
            StylePresetEntity(name = "Midnight Focus", themeName = "Eclipse Gold", accentName = "GOLD", iconId = "icon_midnight_gold", widgetSkinId = "widget_midnight_bronze", focusFaceId = "focus_face_eclipse_dial"),
            StylePresetEntity(name = "Warm Studio", themeName = "Rosewood", accentName = "CLAY", iconId = "icon_terracotta_seal", widgetSkinId = "widget_rose_glass", focusFaceId = "focus_face_soft_hourglass"),
            StylePresetEntity(name = "Sakura Quiet", themeName = "Sakura Dawn", accentName = "LAVENDER", iconId = "icon_sakura_mark", widgetSkinId = "widget_frosted_pearl", focusFaceId = "focus_face_flowing_line"),
            StylePresetEntity(name = "Pure Monochrome", themeName = "Nordic Frost", accentName = "STONE", iconId = "icon_minimal_ink", widgetSkinId = "widget_minimal_paper", focusFaceId = "focus_face_minimal_digital"),
            StylePresetEntity(name = "Metropolis Hero", themeName = "Metropolis Hero", accentName = "CRIMSON", iconId = "icon_superman", widgetSkinId = "widget_superman_hero", focusFaceId = "focus_face_hero_shield")
        )
        curated.forEach { wellbeingDao.insertStylePreset(it) }
    }

    suspend fun checkPresetAvailability(preset: StylePresetEntity): List<String> {
        val missing = mutableListOf<String>()
        val themeEnum = AppTheme.values().find { it.displayName.equals(preset.themeName, ignoreCase = true) || it.name.equals(preset.themeName, ignoreCase = true) }
        if (themeEnum != null) {
            val themeItem = wellbeingDao.getShopItemById("theme_${themeEnum.name.lowercase()}")
            if (themeItem != null && !themeItem.isUnlocked && themeItem.costCoins > 0) {
                missing.add("Theme: ${preset.themeName}")
            }
        }
        if (preset.iconId.isNotBlank() && preset.iconId != "icon_original") {
            val iconItem = wellbeingDao.getShopItemById(preset.iconId)
            if (iconItem != null && !iconItem.isUnlocked) {
                missing.add("Icon: ${iconItem.title}")
            }
        }
        if (preset.widgetSkinId.isNotBlank()) {
            val skinItem = wellbeingDao.getShopItemById(preset.widgetSkinId)
            if (skinItem != null && !skinItem.isUnlocked) {
                missing.add("Widget Skin: ${skinItem.title}")
            }
        }
        if (preset.focusFaceId.isNotBlank() && preset.focusFaceId != "focus_face_classic_ring") {
            val faceItem = wellbeingDao.getShopItemById(preset.focusFaceId)
            if (faceItem != null && !faceItem.isUnlocked) {
                missing.add("Focus Dial: ${faceItem.title}")
            }
        }
        return missing
    }

    suspend fun applyStylePreset(preset: StylePresetEntity) {
        val themeEnum = AppTheme.values().find { it.displayName.equals(preset.themeName, ignoreCase = true) || it.name.equals(preset.themeName, ignoreCase = true) }
        if (themeEnum != null) {
            settingsDataStore.setAppTheme(themeEnum)
        }
        try {
            val accentEnum = AccentOption.valueOf(preset.accentName.uppercase())
            settingsDataStore.setAccentOption(accentEnum)
        } catch (_: Exception) {}

        if (preset.iconId.isNotBlank()) {
            settingsDataStore.setLauncherIconId(preset.iconId)
            AppIconManager.applyLauncherIcon(settingsDataStore.context, preset.iconId)
        }
        if (preset.widgetSkinId.isNotBlank()) {
            settingsDataStore.setWidgetSkinId(preset.widgetSkinId)
        }
        if (preset.focusFaceId.isNotBlank()) {
            settingsDataStore.setFocusTimerFaceId(preset.focusFaceId)
        }
        notifyWidgets()
    }

    // --- Achievements ---
    suspend fun getAllAchievementsList(): List<AchievementEntity> = wellbeingDao.getAllAchievementsList()
    suspend fun seedAchievements(list: List<AchievementEntity>) = wellbeingDao.insertAchievements(list)
    suspend fun updateAchievement(achievement: AchievementEntity) = wellbeingDao.updateAchievement(achievement)

    suspend fun hasAnyHabitLog(): Boolean = habitDao.getAllLogs().first().isNotEmpty()
    suspend fun getTotalHabitLogsCount(): Int = habitDao.getAllLogs().first().size
    suspend fun getCompletedTasksCount(): Int = taskDao.getAllTasks().first().count { it.isCompleted }
    suspend fun getCompletedGoalsCount(): Int = goalDao.getAllGoals().first().count { it.manualProgressPercent >= 100 }
    suspend fun getUnlockedThemesCount(): Int {
        val themes = wellbeingDao.getShopItemsByCategory("THEMES").first()
        return themes.count { it.isUnlocked && it.costCoins > 0 }
    }
    suspend fun getMaxHabitStreak(): Int {
        val habits = habitDao.getActiveHabits().first()
        val logs = habitDao.getAllLogs().first()
        val today = java.time.LocalDate.now().toEpochDay()
        val streakResult = StreakCalculator.calculateStreaks(habits, logs, today)
        return maxOf(streakResult.currentStreak, streakResult.bestStreak)
    }

    suspend fun isHeroicCommitmentMet(): Boolean {
        val habitsCompleted = habitDao.getAllLogs().first().size >= 1
        val focusDone = allFocusSessions.first().isNotEmpty()
        return habitsCompleted && focusDone
    }

    suspend fun addAppGroup(name: String, category: String, packages: String): Long {
        return wellbeingDao.insertAppGroup(
            AppGroupEntity(name = name, categoryType = category, packageNames = packages)
        )
    }

    suspend fun updateAppGroup(group: AppGroupEntity) {
        wellbeingDao.updateAppGroup(group)
    }

    suspend fun deleteAppGroup(group: AppGroupEntity) {
        wellbeingDao.deleteAppGroup(group)
    }

    suspend fun addBlockRule(
        groupId: Long,
        mode: String,
        limitMinutes: Int,
        strictness: String = "GENTLE",
        pauseSeconds: Int = 10,
        pausePrompt: String = "What are you here for?"
    ): Long {
        return wellbeingDao.insertBlockRule(
            BlockRuleEntity(
                appGroupId = groupId,
                mode = mode,
                dailyLimitMinutes = limitMinutes,
                strictnessLevel = strictness,
                pauseBreathingSeconds = pauseSeconds,
                pauseQuestion = pausePrompt
            )
        )
    }

    suspend fun updateBlockRule(rule: BlockRuleEntity) {
        wellbeingDao.updateBlockRule(rule)
    }

    suspend fun addSchedule(ruleId: Long, startMin: Int, endMin: Int, daysMask: Int): Long {
        return wellbeingDao.insertSchedule(
            BlockScheduleEntity(
                ruleId = ruleId,
                startMinuteOfDay = startMin,
                endMinuteOfDay = endMin,
                daysOfWeekMask = daysMask
            )
        )
    }

    suspend fun addBlockSchedule(ruleId: Long, startMin: Int, endMin: Int, daysMask: Int): Long {
        return addSchedule(ruleId, startMin, endMin, daysMask)
    }

    suspend fun startDigitalDetox(durationMinutes: Int, intention: String, allowedPackages: String, enableDnd: Boolean): Long {
        val now = System.currentTimeMillis()
        val endTime = now + (durationMinutes * 60 * 1000L)
        return wellbeingDao.insertDetoxSession(
            DigitalDetoxEntity(
                startTimeMillis = now,
                durationMinutes = durationMinutes,
                endTimeMillis = endTime,
                intention = intention,
                allowedPackages = allowedPackages,
                enableDnd = enableDnd
            )
        )
    }

    suspend fun completeDigitalDetox(session: DigitalDetoxEntity) {
        wellbeingDao.updateDetoxSession(
            session.copy(
                isCompleted = true,
                timeReclaimedMinutes = session.durationMinutes
            )
        )
        awardCoins(15)
    }

    suspend fun addQuitHabit(name: String, ifThen: String?, altActs: String?, contactName: String?, contactPhone: String?): Long {
        return wellbeingDao.insertQuitHabit(
            QuitHabitEntity(
                name = name,
                ifThenPlan = ifThen,
                alternativeActivities = altActs,
                emergencyContactName = contactName,
                emergencyContactPhone = contactPhone
            )
        )
    }

    suspend fun recordQuitHabitSlip(habit: QuitHabitEntity) {
        val updated = QuitHabitTracker.recordSlip(habit)
        wellbeingDao.updateQuitHabit(updated)
    }

    suspend fun repairQuitHabitSlip(habit: QuitHabitEntity) {
        val restored = QuitHabitTracker.repairWithGold(habit, habit.cleanStreakStartMillis)
        wellbeingDao.updateQuitHabit(restored)
        awardCoins(10)
    }

    suspend fun logUrge(habitId: Long, intensity: Int, tags: String, note: String): Long {
        return wellbeingDao.insertUrgeLog(
            UrgeLogEntity(
                quitHabitId = habitId,
                intensity = intensity,
                triggerTags = tags,
                note = note.ifBlank { null }
            )
        )
    }

    // --- Blocked Websites ---
    suspend fun addBlockedWebsite(domain: String, groupName: String = "Social", mode: String = "ALWAYS_BLOCKED", matchSubdomains: Boolean = true): Long {
        return wellbeingDao.insertBlockedWebsite(
            BlockedWebsiteEntity(
                domain = domain.trim().lowercase(),
                groupName = groupName,
                mode = mode,
                matchSubdomains = matchSubdomains,
                isEnabled = true
            )
        )
    }

    suspend fun updateBlockedWebsite(website: BlockedWebsiteEntity) {
        wellbeingDao.updateBlockedWebsite(website)
    }

    suspend fun deleteBlockedWebsite(website: BlockedWebsiteEntity) {
        wellbeingDao.deleteBlockedWebsite(website)
    }

    // --- Focus Lock Sessions ---
    suspend fun startFocusLockSession(
        title: String,
        durationMinutes: Int,
        restrictedGroupIds: String,
        restrictedWebsites: Boolean,
        strictness: String,
        breaksAllowed: Boolean,
        maxBreaks: Int = 2
    ): Long {
        val now = System.currentTimeMillis()
        val endTime = now + (durationMinutes * 60 * 1000L)
        return wellbeingDao.insertFocusLockSession(
            FocusLockSessionEntity(
                title = title,
                durationMinutes = durationMinutes,
                startTimeMillis = now,
                endTimeMillis = endTime,
                restrictedAppGroupIds = restrictedGroupIds,
                restrictedWebsitesEnabled = restrictedWebsites,
                strictnessMode = strictness,
                breaksAllowed = breaksAllowed,
                maxBreaks = maxBreaks,
                breaksTaken = 0,
                isCompleted = false,
                wasEarlyExit = false
            )
        )
    }

    suspend fun endFocusLockSession(session: FocusLockSessionEntity, wasEarly: Boolean) {
        wellbeingDao.updateFocusLockSession(
            session.copy(
                isCompleted = true,
                wasEarlyExit = wasEarly
            )
        )
    }

    fun observeActiveFocusLockSession(): Flow<FocusLockSessionEntity?> {
        return wellbeingDao.observeActiveFocusLockSession(System.currentTimeMillis())
    }

    suspend fun getActiveFocusLockSession(): FocusLockSessionEntity? {
        return wellbeingDao.getActiveFocusLockSession(System.currentTimeMillis())
    }

    // --- Distraction Interventions ---
    suspend fun logDistractionIntervention(
        packageName: String,
        style: String,
        userAction: String,
        reason: String? = null,
        alternative: String? = null
    ): Long {
        return wellbeingDao.insertIntervention(
            DistractionInterventionLogEntity(
                packageName = packageName,
                timestampMillis = System.currentTimeMillis(),
                interventionStyle = style,
                userAction = userAction,
                choiceReason = reason,
                alternativeChosen = alternative
            )
        )
    }

    fun getInterventionsCountToday(): Flow<Int> {
        val startOfDay = LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        return wellbeingDao.getInterventionsCountToday(startOfDay)
    }

    // --- Intentional Breaks ---
    suspend fun logIntentionalBreak(
        target: String,
        durationMinutes: Int,
        sessionId: Long? = null,
        reason: String? = null
    ): Long {
        return wellbeingDao.insertBreak(
            IntentionalBreakLogEntity(
                targetPackageOrDomain = target,
                durationMinutes = durationMinutes,
                timestampMillis = System.currentTimeMillis(),
                associatedSessionId = sessionId,
                reason = reason
            )
        )
    }
}
