package com.kintsu.app.sync

import android.content.Context
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.SettingsDataStore
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
import com.kintsu.app.sync.model.SyncOperation
import com.kintsu.app.sync.model.SyncQueueEntity
import com.kintsu.app.sync.model.SyncRegistryEntity
import com.kintsu.app.sync.model.SyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

data class SyncResult(
    val uploadedCount: Int,
    val downloadedCount: Int,
    val timestamp: Long
)

class KintsuSyncEngine(
    private val database: KintsuDatabase,
    private val settingsDataStore: SettingsDataStore,
    private val cloudClient: KintsuCloudClient
) {
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val syncDao = database.syncDao()
    private val habitDao = database.habitDao()
    private val taskDao = database.taskDao()
    private val goalDao = database.goalDao()
    private val focusDao = database.focusDao()
    private val dailyIntentionDao = database.dailyIntentionDao()
    private val journalDao = database.journalDao()
    private val weeklyReviewDao = database.weeklyReviewDao()
    private val subtaskDao = database.subtaskDao()
    private val taskCompletionDao = database.taskCompletionDao()

    /**
     * Records a local mutation in sync_registry and sync_queue so that it will be
     * sent to the cloud upon the next sync cycle.
     */
    suspend fun recordLocalMutation(
        tableName: String,
        localId: Long,
        operation: SyncOperation,
        payloadJson: String
    ) = withContext(Dispatchers.IO) {
        val existing = syncDao.getRegistryEntry(tableName, localId)
        val syncUuid = existing?.syncUuid ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        syncDao.upsertRegistry(
            SyncRegistryEntity(
                localTableName = tableName,
                localRecordId = localId,
                syncUuid = syncUuid,
                updatedAt = now,
                isDeleted = operation == SyncOperation.DELETE
            )
        )

        syncDao.enqueue(
            SyncQueueEntity(
                syncUuid = syncUuid,
                tableName = tableName,
                operation = operation.name,
                payloadJson = payloadJson,
                createdAt = now
            )
        )
    }

    /**
     * Migrates all existing local guest data into sync_registry and queues them
     * for upload when the user signs in to a cloud account for the first time.
     */
    suspend fun claimAndMigrateLocalData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        // 1. Habits
        val habits = habitDao.getAllHabits().first()
        for (h in habits) {
            if (syncDao.getRegistryEntry("habits", h.id) == null) {
                val uuid = UUID.randomUUID().toString()
                val json = serializeHabit(h)
                syncDao.upsertRegistry(SyncRegistryEntity("habits", h.id, uuid, now, false))
                syncDao.enqueue(SyncQueueEntity(syncUuid = uuid, tableName = "habits", operation = "UPSERT", payloadJson = json, createdAt = now))
            }
        }

        // 2. Habit Logs
        val logs = habitDao.getAllLogs().first()
        for (l in logs) {
            if (syncDao.getRegistryEntry("habit_logs", l.id) == null) {
                val uuid = UUID.randomUUID().toString()
                val json = serializeHabitLog(l)
                syncDao.upsertRegistry(SyncRegistryEntity("habit_logs", l.id, uuid, now, false))
                syncDao.enqueue(SyncQueueEntity(syncUuid = uuid, tableName = "habit_logs", operation = "UPSERT", payloadJson = json, createdAt = now))
            }
        }

        // 3. Tasks
        val tasks = taskDao.getAllTasks().first()
        for (t in tasks) {
            if (syncDao.getRegistryEntry("tasks", t.id) == null) {
                val uuid = UUID.randomUUID().toString()
                val json = serializeTask(t)
                syncDao.upsertRegistry(SyncRegistryEntity("tasks", t.id, uuid, now, false))
                syncDao.enqueue(SyncQueueEntity(syncUuid = uuid, tableName = "tasks", operation = "UPSERT", payloadJson = json, createdAt = now))
            }
        }

        // 4. Subtasks
        val subtasks = subtaskDao.getAllSubtasks().first()
        for (st in subtasks) {
            if (syncDao.getRegistryEntry("subtasks", st.id) == null) {
                val uuid = UUID.randomUUID().toString()
                val json = serializeSubtask(st)
                syncDao.upsertRegistry(SyncRegistryEntity("subtasks", st.id, uuid, now, false))
                syncDao.enqueue(SyncQueueEntity(syncUuid = uuid, tableName = "subtasks", operation = "UPSERT", payloadJson = json, createdAt = now))
            }
        }

        // 5. Task Completions
        val completions = taskCompletionDao.getAllCompletions().first()
        for (tc in completions) {
            if (syncDao.getRegistryEntry("task_completions", tc.id) == null) {
                val uuid = UUID.randomUUID().toString()
                val json = serializeTaskCompletion(tc)
                syncDao.upsertRegistry(SyncRegistryEntity("task_completions", tc.id, uuid, now, false))
                syncDao.enqueue(SyncQueueEntity(syncUuid = uuid, tableName = "task_completions", operation = "UPSERT", payloadJson = json, createdAt = now))
            }
        }

        // 6. Goals
        val goals = goalDao.getAllGoals().first()
        for (g in goals) {
            if (syncDao.getRegistryEntry("goals", g.id) == null) {
                val uuid = UUID.randomUUID().toString()
                val json = serializeGoal(g)
                syncDao.upsertRegistry(SyncRegistryEntity("goals", g.id, uuid, now, false))
                syncDao.enqueue(SyncQueueEntity(syncUuid = uuid, tableName = "goals", operation = "UPSERT", payloadJson = json, createdAt = now))
            }
        }

        // 7. Focus Sessions
        val focusSessions = focusDao.getAllSessions().first()
        for (fs in focusSessions) {
            if (syncDao.getRegistryEntry("focus_sessions", fs.id) == null) {
                val uuid = UUID.randomUUID().toString()
                val json = serializeFocusSession(fs)
                syncDao.upsertRegistry(SyncRegistryEntity("focus_sessions", fs.id, uuid, now, false))
                syncDao.enqueue(SyncQueueEntity(syncUuid = uuid, tableName = "focus_sessions", operation = "UPSERT", payloadJson = json, createdAt = now))
            }
        }

        // 8. Daily Intentions
        val intentions = dailyIntentionDao.getAllIntentions().first()
        for (di in intentions) {
            if (syncDao.getRegistryEntry("daily_intentions", di.id) == null) {
                val uuid = UUID.randomUUID().toString()
                val json = serializeDailyIntention(di)
                syncDao.upsertRegistry(SyncRegistryEntity("daily_intentions", di.id, uuid, now, false))
                syncDao.enqueue(SyncQueueEntity(syncUuid = uuid, tableName = "daily_intentions", operation = "UPSERT", payloadJson = json, createdAt = now))
            }
        }

        // 9. Journal Entries
        val journals = journalDao.getAllEntries().first()
        for (je in journals) {
            if (syncDao.getRegistryEntry("journal_entries", je.id) == null) {
                val uuid = UUID.randomUUID().toString()
                val json = serializeJournalEntry(je)
                syncDao.upsertRegistry(SyncRegistryEntity("journal_entries", je.id, uuid, now, false))
                syncDao.enqueue(SyncQueueEntity(syncUuid = uuid, tableName = "journal_entries", operation = "UPSERT", payloadJson = json, createdAt = now))
            }
        }

        // 10. Weekly Reviews
        val reviews = weeklyReviewDao.getAllReviews().first()
        for (wr in reviews) {
            if (syncDao.getRegistryEntry("weekly_reviews", wr.id) == null) {
                val uuid = UUID.randomUUID().toString()
                val json = serializeWeeklyReview(wr)
                syncDao.upsertRegistry(SyncRegistryEntity("weekly_reviews", wr.id, uuid, now, false))
                syncDao.enqueue(SyncQueueEntity(syncUuid = uuid, tableName = "weekly_reviews", operation = "UPSERT", payloadJson = json, createdAt = now))
            }
        }
    }

    /**
     * Executes a complete bidirectional synchronization cycle:
     * 1. Validates authentication and sync status.
     * 2. Flushes pending mutations to the cloud.
     * 3. Downloads remote changes since lastSyncTime.
     * 4. Reconciles differences deterministically into Room.
     * 5. Updates sync timestamps and status reactively.
     */
    suspend fun performSync(): Result<SyncResult> = withContext(Dispatchers.IO) {
        val settings = settingsDataStore.settingsFlow.first()
        val token = settings.accountAuthToken
        val userId = settings.accountUserId

        if (token.isNullOrBlank() || userId.isNullOrBlank()) {
            _syncState.value = SyncState.IDLE
            return@withContext Result.failure(IllegalStateException("No cloud account signed in"))
        }

        if (!settings.syncEnabled) {
            _syncState.value = SyncState.IDLE
            return@withContext Result.failure(IllegalStateException("Sync is currently disabled"))
        }

        _syncState.value = SyncState.SYNCING
        _lastError.value = null

        try {
            // Update cloud client configuration if custom endpoints were provided
            cloudClient.updateConfig(settings.cloudEndpointUrl, settings.cloudApiKey)

            // Step 1: Claim any newly added local records that haven't been mapped to UUIDs
            claimAndMigrateLocalData()

            // Step 2: Push pending local mutations to cloud
            val queueItems = syncDao.peekQueue(limit = 100)
            var uploadedCount = 0

            if (queueItems.isNotEmpty()) {
                val mutationPayloads = queueItems.map { q ->
                    SyncMutationPayload(
                        syncUuid = q.syncUuid,
                        tableName = q.tableName,
                        operation = q.operation,
                        payloadJson = q.payloadJson,
                        timestamp = q.createdAt
                    )
                }

                val uploadResult = cloudClient.uploadMutations(token, userId, mutationPayloads)
                if (uploadResult.isSuccess) {
                    val syncedUuids = uploadResult.getOrThrow().toSet()
                    val toDeleteIds = queueItems.filter { syncedUuids.contains(it.syncUuid) }.map { it.id }
                    syncDao.dequeueBatch(toDeleteIds)
                    uploadedCount = toDeleteIds.size
                } else {
                    // Queue retry count increment
                    queueItems.forEach { syncDao.incrementRetryCount(it.id) }
                }
            }

            // Step 3: Pull remote changes modified since lastSyncTime
            val downloadResult = cloudClient.downloadChanges(token, userId, settings.lastSyncTime)
            var downloadedCount = 0

            if (downloadResult.isSuccess) {
                val changes = downloadResult.getOrThrow()
                for (remote in changes.records) {
                    applyRemoteRecord(remote)
                    downloadedCount++
                }

                val now = System.currentTimeMillis()
                settingsDataStore.setLastSyncTime(now)
            }

            _syncState.value = SyncState.SUCCESS
            Result.success(SyncResult(uploadedCount, downloadedCount, System.currentTimeMillis()))
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            _lastError.value = e.localizedMessage ?: "Sync connection interrupted"
            Result.failure(e)
        }
    }

    /**
     * Resolves and applies a remote record into Room using deterministic rules:
     * - If tombstoned (isDeleted): removes local entity.
     * - If missing locally: inserts into Room and registers mapping.
     * - If present locally: compares timestamps (LWW), branching conflicting journal entries safely.
     */
    private suspend fun applyRemoteRecord(remote: RemoteRecord) {
        val existingRegistry = syncDao.getRegistryByUuid(remote.syncUuid)

        if (remote.isDeleted) {
            if (existingRegistry != null && !existingRegistry.isDeleted) {
                deleteLocalEntity(existingRegistry.localTableName, existingRegistry.localRecordId)
                syncDao.upsertRegistry(existingRegistry.copy(isDeleted = true, updatedAt = remote.updatedAt))
            }
            return
        }

        if (existingRegistry == null) {
            // New record from another device! Insert into local Room table
            val newLocalId = insertRemoteEntity(remote.tableName, remote.payloadJson)
            if (newLocalId > 0L) {
                syncDao.upsertRegistry(
                    SyncRegistryEntity(
                        localTableName = remote.tableName,
                        localRecordId = newLocalId,
                        syncUuid = remote.syncUuid,
                        updatedAt = remote.updatedAt,
                        isDeleted = false
                    )
                )
            }
        } else {
            // Record exists locally: compare timestamps (Last-Write-Wins)
            if (remote.updatedAt > existingRegistry.updatedAt) {
                updateRemoteEntity(existingRegistry.localTableName, existingRegistry.localRecordId, remote.payloadJson)
                syncDao.upsertRegistry(existingRegistry.copy(updatedAt = remote.updatedAt, isDeleted = false))
            }
        }
    }

    private suspend fun insertRemoteEntity(tableName: String, jsonStr: String): Long {
        val json = JSONObject(jsonStr)
        return when (tableName) {
            "habits" -> {
                val habit = HabitEntity(
                    name = json.getString("name"),
                    iconName = json.optString("iconName", "habit"),
                    colorHex = json.optString("colorHex", "#B8935A"),
                    frequencyType = try { FrequencyType.valueOf(json.getString("frequencyType")) } catch (_: Exception) { FrequencyType.DAILY },
                    weekdaysMask = json.optInt("weekdaysMask", 127),
                    reminderTime = json.optString("reminderTime").ifBlank { null },
                    reminderTimes = json.optString("reminderTimes", ""),
                    frequencyTarget = json.optInt("frequencyTarget", 1),
                    routineGroup = json.optString("routineGroup").ifBlank { null },
                    isArchived = json.optBoolean("isArchived", false),
                    createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    displayOrder = json.optInt("displayOrder", 0)
                )
                habitDao.insertHabit(habit)
            }
            "tasks" -> {
                val task = TaskEntity(
                    title = json.getString("title"),
                    dueDateEpochDay = if (json.has("dueDateEpochDay")) json.optLong("dueDateEpochDay") else null,
                    priority = try { TaskPriority.valueOf(json.getString("priority")) } catch (_: Exception) { TaskPriority.MEDIUM },
                    isCompleted = json.optBoolean("isCompleted", false),
                    completedAt = if (json.has("completedAt")) json.optLong("completedAt") else null,
                    createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    recurrenceType = try { TaskRecurrenceType.valueOf(json.optString("recurrenceType", "NEVER")) } catch (_: Exception) { TaskRecurrenceType.NEVER },
                    recurrenceWeekdaysMask = json.optInt("recurrenceWeekdaysMask", 0),
                    recurrenceActive = json.optBoolean("recurrenceActive", true),
                    plannedDateEpochDay = if (json.has("plannedDateEpochDay")) json.optLong("plannedDateEpochDay") else null,
                    plannedTimeBlock = json.optString("plannedTimeBlock").ifBlank { null },
                    planOrder = json.optInt("planOrder", 0)
                )
                taskDao.insertTask(task)
            }
            "goals" -> {
                val goal = GoalEntity(
                    title = json.getString("title"),
                    targetDateEpochDay = json.optLong("targetDateEpochDay", System.currentTimeMillis() / (24 * 3600 * 1000)),
                    progressType = try { GoalProgressType.valueOf(json.optString("progressType", "AUTO_FROM_ITEMS")) } catch (_: Exception) { GoalProgressType.AUTO_FROM_ITEMS },
                    manualProgressPercent = json.optInt("manualProgressPercent", 0),
                    createdAt = json.optLong("createdAt", System.currentTimeMillis())
                )
                goalDao.insertGoal(goal)
            }
            "journal_entries" -> {
                val entry = JournalEntryEntity(
                    dateEpochDay = json.getLong("dateEpochDay"),
                    content = json.getString("content"),
                    mood = json.optString("mood").ifBlank { null },
                    createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
                )
                journalDao.insertOrUpdate(entry)
            }
            "daily_intentions" -> {
                val intention = DailyIntentionEntity(
                    dateEpochDay = json.getLong("dateEpochDay"),
                    text = json.getString("text"),
                    isCompleted = json.optBoolean("isCompleted", false),
                    updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
                )
                dailyIntentionDao.insertOrUpdate(intention)
            }
            "weekly_reviews" -> {
                val review = WeeklyReviewEntity(
                    weekStartEpochDay = json.getLong("weekStartEpochDay"),
                    weekEndEpochDay = json.getLong("weekEndEpochDay"),
                    completionRate = json.optDouble("completionRate", 0.0).toFloat(),
                    bestStreak = json.optInt("bestStreak", 0),
                    totalCracks = json.optInt("totalCracks", 0),
                    totalRepairs = json.optInt("totalRepairs", 0),
                    focusMinutes = json.optInt("focusMinutes", 0),
                    mostConsistentHabitName = json.optString("mostConsistentHabitName").ifBlank { null },
                    leastConsistentHabitName = json.optString("leastConsistentHabitName").ifBlank { null },
                    goalsProgressSummary = json.optString("goalsProgressSummary", ""),
                    reflection1 = json.optString("reflection1", ""),
                    reflection2 = json.optString("reflection2", ""),
                    reflection3 = json.optString("reflection3", ""),
                    createdAt = json.optLong("createdAt", System.currentTimeMillis())
                )
                weeklyReviewDao.insertOrUpdate(review)
            }
            else -> 0L
        }
    }

    private suspend fun updateRemoteEntity(tableName: String, localId: Long, jsonStr: String) {
        val json = JSONObject(jsonStr)
        when (tableName) {
            "habits" -> {
                val existing = habitDao.getHabitById(localId) ?: return
                val updated = existing.copy(
                    name = json.getString("name"),
                    iconName = json.optString("iconName", existing.iconName),
                    colorHex = json.optString("colorHex", existing.colorHex),
                    frequencyType = try { FrequencyType.valueOf(json.getString("frequencyType")) } catch (_: Exception) { existing.frequencyType },
                    weekdaysMask = json.optInt("weekdaysMask", existing.weekdaysMask),
                    reminderTime = json.optString("reminderTime").ifBlank { null },
                    reminderTimes = json.optString("reminderTimes", existing.reminderTimes),
                    frequencyTarget = json.optInt("frequencyTarget", existing.frequencyTarget),
                    routineGroup = json.optString("routineGroup").ifBlank { null },
                    isArchived = json.optBoolean("isArchived", existing.isArchived)
                )
                habitDao.updateHabit(updated)
            }
            "tasks" -> {
                val existing = taskDao.getTaskById(localId) ?: return
                val updated = existing.copy(
                    title = json.getString("title"),
                    dueDateEpochDay = if (json.has("dueDateEpochDay")) json.optLong("dueDateEpochDay") else null,
                    priority = try { TaskPriority.valueOf(json.getString("priority")) } catch (_: Exception) { existing.priority },
                    isCompleted = json.optBoolean("isCompleted", existing.isCompleted),
                    completedAt = if (json.has("completedAt")) json.optLong("completedAt") else existing.completedAt,
                    recurrenceType = try { TaskRecurrenceType.valueOf(json.optString("recurrenceType", existing.recurrenceType.name)) } catch (_: Exception) { existing.recurrenceType },
                    plannedDateEpochDay = if (json.has("plannedDateEpochDay")) json.optLong("plannedDateEpochDay") else null,
                    plannedTimeBlock = json.optString("plannedTimeBlock").ifBlank { null }
                )
                taskDao.updateTask(updated)
            }
            "journal_entries" -> {
                // Recoverable non-destructive conflict resolution for reflections:
                val dateEpochDay = json.getLong("dateEpochDay")
                val existing = journalDao.getEntryForDay(dateEpochDay).first()
                val remoteContent = json.getString("content")
                if (existing != null && existing.content != remoteContent && existing.content.isNotBlank()) {
                    // Content differs: merge/preserve safely without overwriting user reflection!
                    val preservedContent = "${existing.content}\n\n[Synchronised revision from another device]:\n$remoteContent"
                    journalDao.insertOrUpdate(existing.copy(content = preservedContent, updatedAt = System.currentTimeMillis()))
                } else if (existing != null) {
                    journalDao.insertOrUpdate(existing.copy(content = remoteContent, mood = json.optString("mood").ifBlank { null }))
                }
            }
            "daily_intentions" -> {
                val dateEpochDay = json.getLong("dateEpochDay")
                val existing = dailyIntentionDao.getIntentionForDay(dateEpochDay).first() ?: return
                dailyIntentionDao.insertOrUpdate(existing.copy(text = json.getString("text"), isCompleted = json.optBoolean("isCompleted", existing.isCompleted)))
            }
        }
    }

    private suspend fun deleteLocalEntity(tableName: String, localId: Long) {
        when (tableName) {
            "habits" -> habitDao.getHabitById(localId)?.let { habitDao.deleteHabit(it) }
            "tasks" -> taskDao.getTaskById(localId)?.let { taskDao.deleteTask(it) }
            "goals" -> goalDao.getGoalById(localId)?.let { goalDao.deleteGoal(it) }
            "subtasks" -> subtaskDao.deleteSubtaskById(localId)
        }
    }

    // --- Serializers for Room Entities to Cloud JSON ---

    fun serializeHabit(h: HabitEntity): String = JSONObject().apply {
        put("name", h.name)
        put("iconName", h.iconName)
        put("colorHex", h.colorHex)
        put("frequencyType", h.frequencyType.name)
        put("weekdaysMask", h.weekdaysMask)
        put("reminderTime", h.reminderTime ?: "")
        put("reminderTimes", h.reminderTimes)
        put("frequencyTarget", h.frequencyTarget)
        put("routineGroup", h.routineGroup ?: "")
        put("isArchived", h.isArchived)
        put("createdAt", h.createdAt)
        put("displayOrder", h.displayOrder)
    }.toString()

    fun serializeHabitLog(l: HabitLogEntity): String = JSONObject().apply {
        put("epochDay", l.epochDay)
        put("isCompleted", l.isCompleted)
        put("isRepairedGold", l.isRepairedGold)
        put("repairedAt", l.repairedAt ?: -1L)
        put("isCrack", l.isCrack)
        put("completionCount", l.completionCount)
        put("note", l.note ?: "")
        put("mood", l.mood ?: "")
    }.toString()

    fun serializeTask(t: TaskEntity): String = JSONObject().apply {
        put("title", t.title)
        if (t.dueDateEpochDay != null) put("dueDateEpochDay", t.dueDateEpochDay)
        put("priority", t.priority.name)
        put("isCompleted", t.isCompleted)
        if (t.completedAt != null) put("completedAt", t.completedAt)
        put("createdAt", t.createdAt)
        put("recurrenceType", t.recurrenceType.name)
        put("recurrenceWeekdaysMask", t.recurrenceWeekdaysMask)
        put("recurrenceActive", t.recurrenceActive)
        if (t.plannedDateEpochDay != null) put("plannedDateEpochDay", t.plannedDateEpochDay)
        put("plannedTimeBlock", t.plannedTimeBlock ?: "")
        put("planOrder", t.planOrder)
    }.toString()

    fun serializeSubtask(st: SubtaskEntity): String = JSONObject().apply {
        put("title", st.title)
        put("isCompleted", st.isCompleted)
        put("orderIndex", st.orderIndex)
        put("createdAt", st.createdAt)
    }.toString()

    fun serializeTaskCompletion(tc: TaskCompletionEntity): String = JSONObject().apply {
        put("epochDay", tc.epochDay)
        put("completedAt", tc.completedAt)
    }.toString()

    fun serializeGoal(g: GoalEntity): String = JSONObject().apply {
        put("title", g.title)
        put("targetDateEpochDay", g.targetDateEpochDay)
        put("progressType", g.progressType.name)
        put("manualProgressPercent", g.manualProgressPercent)
        put("createdAt", g.createdAt)
    }.toString()

    fun serializeFocusSession(fs: FocusSessionEntity): String = JSONObject().apply {
        put("dateEpochDay", fs.dateEpochDay)
        put("durationMinutes", fs.durationMinutes)
        put("isCompleted", fs.isCompleted)
        put("startTimeMillis", fs.startTimeMillis)
        put("endTimeMillis", fs.endTimeMillis)
    }.toString()

    fun serializeDailyIntention(di: DailyIntentionEntity): String = JSONObject().apply {
        put("dateEpochDay", di.dateEpochDay)
        put("text", di.text)
        put("isCompleted", di.isCompleted)
        put("updatedAt", di.updatedAt)
    }.toString()

    fun serializeJournalEntry(je: JournalEntryEntity): String = JSONObject().apply {
        put("dateEpochDay", je.dateEpochDay)
        put("content", je.content)
        put("mood", je.mood ?: "")
        put("createdAt", je.createdAt)
        put("updatedAt", je.updatedAt)
    }.toString()

    fun serializeWeeklyReview(wr: WeeklyReviewEntity): String = JSONObject().apply {
        put("weekStartEpochDay", wr.weekStartEpochDay)
        put("weekEndEpochDay", wr.weekEndEpochDay)
        put("completionRate", wr.completionRate.toDouble())
        put("bestStreak", wr.bestStreak)
        put("totalCracks", wr.totalCracks)
        put("totalRepairs", wr.totalRepairs)
        put("focusMinutes", wr.focusMinutes)
        put("mostConsistentHabitName", wr.mostConsistentHabitName ?: "")
        put("leastConsistentHabitName", wr.leastConsistentHabitName ?: "")
        put("goalsProgressSummary", wr.goalsProgressSummary)
        put("reflection1", wr.reflection1)
        put("reflection2", wr.reflection2)
        put("reflection3", wr.reflection3)
        put("createdAt", wr.createdAt)
    }.toString()
}
