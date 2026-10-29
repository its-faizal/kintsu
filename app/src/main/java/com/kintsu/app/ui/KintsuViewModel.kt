package com.kintsu.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.KintsuRepository
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.ProgressStats
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.CommitmentLockEntity
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.DailyPlanItem
import com.kintsu.app.data.model.DigitalDetoxEntity
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.QuitHabitEntity
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.data.model.SubtaskEntity
import com.kintsu.app.data.model.TaskCompletionEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import com.kintsu.app.data.model.TaskRecurrenceType
import com.kintsu.app.data.model.UrgeLogEntity
import com.kintsu.app.data.model.WeeklyReviewEntity
import com.kintsu.app.domain.GoldRepairManager
import com.kintsu.app.domain.HabitRepairState
import com.kintsu.app.domain.StreakCalculator
import com.kintsu.app.domain.focus.FocusState
import com.kintsu.app.domain.home.HomeKeyNumbers
import com.kintsu.app.domain.home.HomeStatsCalculator
import com.kintsu.app.domain.progress.AdvancedInsightsCalculator
import com.kintsu.app.domain.progress.AdvancedProgressInsights
import com.kintsu.app.domain.review.WeeklyReviewGenerator
import com.kintsu.app.focus.FocusForegroundService
import com.kintsu.app.ui.components.KintsuNavTab
import com.kintsu.app.ui.theme.AccentOption
import com.kintsu.app.ui.theme.AppTheme
import com.kintsu.app.ui.theme.ThemeMode
import com.kintsu.app.sync.AuthSession
import com.kintsu.app.sync.model.SyncState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UndoAction(
    val message: String,
    val onUndo: suspend () -> Unit
)

class KintsuViewModel(application: Application) : AndroidViewModel(application) {
    val database = KintsuDatabase.getDatabase(application)
    val settingsStore = SettingsDataStore(application)
    val repository = KintsuRepository(database, settingsStore)

    val navigationBackStack = NavigationBackStack(KintsuNavTab.HOME)
    val currentTab: StateFlow<KintsuNavTab> = navigationBackStack.stack
        .map { it.lastOrNull() ?: KintsuNavTab.HOME }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KintsuNavTab.HOME)

    private val _homeScrollToTopEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val homeScrollToTopEvents: SharedFlow<Unit> = _homeScrollToTopEvents.asSharedFlow()

    fun onHomeTabReselected() {
        _homeScrollToTopEvents.tryEmit(Unit)
    }

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isShopOpen = MutableStateFlow(false)
    val isShopOpen: StateFlow<Boolean> = _isShopOpen.asStateFlow()

    private val _isDetoxOpen = MutableStateFlow(false)
    val isDetoxOpen: StateFlow<Boolean> = _isDetoxOpen.asStateFlow()

    private val _isQuitHabitsOpen = MutableStateFlow(false)
    val isQuitHabitsOpen: StateFlow<Boolean> = _isQuitHabitsOpen.asStateFlow()

    private val _isWellbeingWizardOpen = MutableStateFlow(false)
    val isWellbeingWizardOpen: StateFlow<Boolean> = _isWellbeingWizardOpen.asStateFlow()

    fun setShopOpen(open: Boolean) { _isShopOpen.value = open }
    fun setDetoxOpen(open: Boolean) { _isDetoxOpen.value = open }
    fun setQuitHabitsOpen(open: Boolean) { _isQuitHabitsOpen.value = open }
    fun setWellbeingWizardOpen(open: Boolean) { _isWellbeingWizardOpen.value = open }

    val allShopItems: StateFlow<List<ShopItemEntity>> = repository.allShopItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeQuitHabits: StateFlow<List<QuitHabitEntity>> = repository.activeQuitHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDetoxSessions: StateFlow<List<DigitalDetoxEntity>> = repository.allDetoxSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppGroups: StateFlow<List<AppGroupEntity>> = repository.allAppGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBlockRules: StateFlow<List<BlockRuleEntity>> = repository.activeBlockRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCommitmentLocks: StateFlow<List<CommitmentLockEntity>> = repository.activeCommitmentLocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedRoutineGroup = MutableStateFlow<String?>(null)
    val selectedRoutineGroup: StateFlow<String?> = _selectedRoutineGroup.asStateFlow()

    private val _undoEvents = MutableSharedFlow<UndoAction>()
    val undoEvents: SharedFlow<UndoAction> = _undoEvents.asSharedFlow()

    val settings: StateFlow<KintsuSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KintsuSettings())

    val activeHabits: StateFlow<List<HabitEntity>> = repository.activeHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayEpochDay: Long = LocalDate.now().toEpochDay()

    val todayLogs: StateFlow<List<HabitLogEntity>> = repository.getLogsForDay(todayEpochDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<HabitLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubtasks: StateFlow<List<SubtaskEntity>> = repository.allSubtasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTaskCompletions: StateFlow<List<TaskCompletionEntity>> = repository.allTaskCompletions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _parentCompletionPrompts = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val parentCompletionPrompts: SharedFlow<Long> = _parentCompletionPrompts.asSharedFlow()

    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusState: StateFlow<FocusState> = FocusForegroundService.focusState

    val todayIntention: StateFlow<DailyIntentionEntity?> = repository.getDailyIntention(todayEpochDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allIntentions: StateFlow<List<DailyIntentionEntity>> = repository.allDailyIntentions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayJournalEntry: StateFlow<JournalEntryEntity?> = repository.getJournalEntry(todayEpochDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allJournalEntries: StateFlow<List<JournalEntryEntity>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWeeklyReviews: StateFlow<List<WeeklyReviewEntity>> = repository.allWeeklyReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFocusSessions: StateFlow<List<FocusSessionEntity>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncState: StateFlow<SyncState> = repository.syncState
    val syncError: StateFlow<String?> = repository.syncError

    val availableRepairs: StateFlow<List<HabitRepairState>> = combine(
        activeHabits,
        allLogs,
        settings
    ) { habits, logs, s ->
        habits.mapNotNull { h ->
            GoldRepairManager.findAvailableRepair(
                habit = h,
                logs = logs,
                todayEpochDay = todayEpochDay,
                goldRepairEnabled = s.goldRepairEnabled,
                weekStartMonday = s.weekStartMonday
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeStats: StateFlow<HomeKeyNumbers> = combine(
        activeHabits,
        allLogs,
        allTasks,
        allFocusSessions,
        settings
    ) { habits, logs, tasks, sessions, s ->
        HomeStatsCalculator.calculate(
            habits = habits,
            logs = logs,
            tasks = tasks,
            focusSessions = sessions,
            todayEpochDay = todayEpochDay,
            goldRepairEnabled = s.goldRepairEnabled
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeKeyNumbers(0, 0, 0)
    )

    val stats: StateFlow<ProgressStats> = combine(
        combine(activeHabits, allLogs, allTasks) { h, l, t -> Triple(h, l, t) },
        combine(allFocusSessions, settings, repository.allTaskCompletions) { f, s, c -> Triple(f, s, c) }
    ) { (habits, logs, tasks), (sessions, s, taskCompletions) ->
        KintsuRepository.computeStats(
            habits = habits,
            logs = logs,
            tasks = tasks,
            focusSessions = sessions,
            settings = s,
            today = todayEpochDay,
            taskCompletions = taskCompletions
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ProgressStats(
            currentStreak = 0,
            bestStreak = 0,
            totalRepairsGold = 0,
            totalCracks = 0,
            weeklyCompletionRate = 0f,
            monthlyCompletionRate = 0f,
            totalHabitsDone = 0,
            totalTasksDone = 0
        )
    )

    val advancedInsights: StateFlow<AdvancedProgressInsights> = combine(
        combine(activeHabits, allLogs, allTasks) { h, l, t -> Triple(h, l, t) },
        combine(allTaskCompletions, allGoals, allFocusSessions) { tc, g, fs -> Triple(tc, g, fs) }
    ) { (habits, logs, tasks), (taskCompletions, goals, focusSessions) ->
        AdvancedInsightsCalculator.calculate(
            habits = habits,
            logs = logs,
            tasks = tasks,
            taskCompletions = taskCompletions,
            goals = goals,
            focusSessions = focusSessions,
            todayEpochDay = todayEpochDay
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AdvancedInsightsCalculator.calculate(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    )

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
            repository.seedShopCatalogIfEmpty()
        }
    }

    fun purchaseShopItem(item: ShopItemEntity) {
        viewModelScope.launch { repository.purchaseShopItem(item) }
    }

    fun equipShopItem(item: ShopItemEntity) {
        viewModelScope.launch { repository.equipShopItem(item) }
    }

    fun startDigitalDetox(durationMinutes: Int, intention: String, allowedPackages: String, enableDnd: Boolean) {
        viewModelScope.launch {
            repository.startDigitalDetox(durationMinutes, intention, allowedPackages, enableDnd)
        }
    }

    fun endDigitalDetoxEarly(session: DigitalDetoxEntity) {
        viewModelScope.launch {
            repository.completeDigitalDetox(session)
        }
    }

    fun addQuitHabit(name: String, ifThen: String?, altActs: String?, contactName: String?, contactPhone: String?) {
        viewModelScope.launch {
            repository.addQuitHabit(name, ifThen, altActs, contactName, contactPhone)
        }
    }

    fun recordQuitHabitSlip(habit: QuitHabitEntity) {
        viewModelScope.launch { repository.recordQuitHabitSlip(habit) }
    }

    fun repairQuitHabitSlip(habit: QuitHabitEntity) {
        viewModelScope.launch { repository.repairQuitHabitSlip(habit) }
    }

    fun logUrge(habitId: Long, intensity: Int, tags: String, note: String) {
        viewModelScope.launch { repository.logUrge(habitId, intensity, tags, note) }
    }

    fun setWellbeingEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setWellbeingEnabled(enabled) } }
    }

    fun resetFailSafe() {
        viewModelScope.launch { repository.updateSettings { it.setProtectionFailSafeTripped(false) } }
    }

    fun setWellbeingStrictness(strictness: String) {
        viewModelScope.launch { repository.updateSettings { it.setWellbeingStrictness(strictness) } }
    }

    fun setScreenTimeGoalMinutes(minutes: Int) {
        viewModelScope.launch { repository.updateSettings { it.setScreenTimeGoalMinutes(minutes) } }
    }

    fun refreshStats() {
        // Stats is computed reactively via combine(activeHabits, allLogs, allTasks, allFocusSessions, settings)
    }

    fun selectTab(tab: KintsuNavTab) {
        navigationBackStack.selectTab(tab)
    }

    fun popBackStack(): Boolean {
        return navigationBackStack.pop()
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun setSelectedRoutineGroup(group: String?) {
        _selectedRoutineGroup.value = group
    }

    // Habits actions
    fun toggleHabit(habitId: Long, note: String? = null, mood: String? = null) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, todayEpochDay, note, mood)
            refreshStats()
        }
    }

    fun repairHabitWithGold(habitId: Long, missedEpochDay: Long) {
        viewModelScope.launch {
            repository.mendHabitDayWithGold(habitId, missedEpochDay)
            refreshStats()
        }
    }

    fun mendWithGold(habitId: Long) {
        viewModelScope.launch {
            repository.mendHabitDayWithGold(habitId, todayEpochDay)
            refreshStats()
        }
    }

    fun addHabit(
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
    ) {
        viewModelScope.launch {
            repository.addHabit(
                name = name,
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
            refreshStats()
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun archiveHabit(habitId: Long, archive: Boolean) {
        viewModelScope.launch {
            repository.archiveHabit(habitId, archive)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            refreshStats()
            _undoEvents.emit(
                UndoAction(
                    message = "Habit '${habit.name}' removed",
                    onUndo = {
                        repository.restoreHabit(habit)
                        refreshStats()
                    }
                )
            )
        }
    }

    // Task actions
    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            refreshStats()
        }
    }

    fun addTask(
        title: String,
        dueDate: Long?,
        priority: TaskPriority,
        linkedGoalId: Long? = null,
        recurrenceType: TaskRecurrenceType = TaskRecurrenceType.NEVER,
        recurrenceWeekdaysMask: Int = 0,
        recurrenceActive: Boolean = true,
        plannedDateEpochDay: Long? = null,
        plannedTimeBlock: String? = null,
        planOrder: Int = 0,
        initialSubtasks: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val taskId = repository.addTask(
                title = title,
                dueDateEpochDay = dueDate,
                priority = priority,
                linkedGoalId = linkedGoalId,
                recurrenceType = recurrenceType,
                recurrenceWeekdaysMask = recurrenceWeekdaysMask,
                recurrenceActive = recurrenceActive,
                plannedDateEpochDay = plannedDateEpochDay,
                plannedTimeBlock = plannedTimeBlock,
                planOrder = planOrder
            )
            initialSubtasks.forEachIndexed { index, subtaskTitle ->
                if (subtaskTitle.isNotBlank()) {
                    repository.addSubtask(taskId, subtaskTitle.trim(), index)
                }
            }
            refreshStats()
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            refreshStats()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            refreshStats()
            _undoEvents.emit(
                UndoAction(
                    message = "Task '${task.title}' deleted",
                    onUndo = {
                        repository.restoreTask(task)
                        refreshStats()
                    }
                )
            )
        }
    }

    fun planTaskForToday(taskId: Long, timeBlock: String? = null) {
        viewModelScope.launch {
            repository.planTask(taskId, todayEpochDay, timeBlock)
        }
    }

    fun removeTaskFromPlan(taskId: Long) {
        viewModelScope.launch {
            repository.planTask(taskId, null, null)
        }
    }

    fun rescheduleTask(taskId: Long, newDueDate: Long?) {
        viewModelScope.launch {
            repository.rescheduleTask(taskId, newDueDate)
        }
    }

    fun saveDailyPlan(planItems: List<DailyPlanItem>) {
        viewModelScope.launch {
            planItems.forEachIndexed { index, item ->
                repository.planTask(
                    taskId = item.taskId,
                    epochDay = todayEpochDay,
                    timeBlock = item.timeBlock,
                    order = index
                )
            }
        }
    }

    // Subtask actions
    fun addSubtask(taskId: Long, title: String) {
        viewModelScope.launch {
            val existing = allSubtasks.value.filter { it.taskId == taskId }
            repository.addSubtask(taskId, title, existing.size)
        }
    }

    fun toggleSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch {
            val nextState = !subtask.isCompleted
            repository.toggleSubtask(subtask)
            if (nextState) {
                // If this was marked done, check if all sibling subtasks are now completed
                val siblings = allSubtasks.value.filter { it.taskId == subtask.taskId }
                val allCompletedNow = siblings.all {
                    if (it.id == subtask.id) true else it.isCompleted
                }
                if (allCompletedNow && siblings.isNotEmpty()) {
                    _parentCompletionPrompts.tryEmit(subtask.taskId)
                }
            }
        }
    }

    fun updateSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch {
            repository.updateSubtask(subtask)
        }
    }

    fun deleteSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch {
            repository.deleteSubtask(subtask)
        }
    }

    fun reorderSubtasks(subtasks: List<SubtaskEntity>) {
        viewModelScope.launch {
            repository.reorderSubtasks(subtasks)
        }
    }

    // Goal actions
    fun addGoal(title: String, targetDate: Long, progressType: GoalProgressType, manualProgress: Int) {
        viewModelScope.launch {
            repository.addGoal(title, targetDate, progressType, manualProgress)
            refreshStats()
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal)
            refreshStats()
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            refreshStats()
            _undoEvents.emit(
                UndoAction(
                    message = "Goal '${goal.title}' removed",
                    onUndo = {
                        repository.restoreGoal(goal)
                        refreshStats()
                    }
                )
            )
        }
    }

    // Settings actions
    fun setDisplayName(name: String) {
        viewModelScope.launch {
            repository.updateSettings { it.setDisplayName(name) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.updateSettings { it.setThemeMode(mode) }
        }
    }

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            repository.updateSettings { it.setAppTheme(theme) }
        }
    }

    fun setAccentOption(accent: AccentOption) {
        viewModelScope.launch {
            repository.updateSettings { it.setAccentOption(accent) }
        }
    }

    fun setContentDensity(density: com.kintsu.app.data.ContentDensity) {
        viewModelScope.launch {
            repository.updateSettings { it.setContentDensity(density) }
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.setHapticsEnabled(enabled) }
        }
    }

    fun setReducedMotion(reduced: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.setReducedMotion(reduced) }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.setNotificationsEnabled(enabled) }
        }
    }

    fun setDefaultReminderTime(time: String) {
        viewModelScope.launch {
            repository.updateSettings { it.setDefaultReminderTime(time) }
        }
    }

    fun setWeekStartMonday(monday: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.setWeekStartMonday(monday) }
        }
    }

    fun setGoldRepairEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.setGoldRepairEnabled(enabled) }
            refreshStats()
        }
    }

    fun setNotifyOnlyIfNotDone(onlyIfNotDone: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.setNotifyOnlyIfNotDone(onlyIfNotDone) }
        }
    }

    fun setGoldRepairReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.setGoldRepairReminderEnabled(enabled) }
        }
    }

    fun completeOnboarding(displayName: String, accent: AccentOption) {
        viewModelScope.launch {
            repository.updateSettings {
                it.setDisplayName(displayName)
                it.setAccentOption(accent)
                it.setOnboardingCompleted(true)
            }
        }
    }

    fun setPermissionsReviewed(reviewed: Boolean) {
        viewModelScope.launch {
            repository.updateSettings {
                it.setPermissionsReviewed(reviewed)
            }
        }
    }

    suspend fun exportDataJson(): String = repository.exportDataAsJson()

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            refreshStats()
        }
    }

    // --- V3.0 Cloud Sync & Account Actions ---

    fun syncNow() {
        viewModelScope.launch {
            repository.performSync()
        }
    }

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSyncEnabled(enabled)
        }
    }

    fun signIn(email: String, pass: String, onResult: (Result<AuthSession>) -> Unit) {
        viewModelScope.launch {
            val res = repository.signIn(email, pass)
            onResult(res)
        }
    }

    fun signUp(email: String, pass: String, name: String?, onResult: (Result<AuthSession>) -> Unit) {
        viewModelScope.launch {
            val res = repository.signUp(email, pass, name)
            onResult(res)
        }
    }

    fun signInWithGoogle(idToken: String, email: String? = null, name: String? = null, onResult: (Result<AuthSession>) -> Unit) {
        viewModelScope.launch {
            val res = repository.signInWithGoogle(idToken, email, name)
            onResult(res)
        }
    }

    fun signOut(keepLocalData: Boolean = true) {
        viewModelScope.launch {
            repository.signOut(keepLocalData)
        }
    }

    fun deleteCloudData(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = repository.deleteCloudData()
            onResult(res)
        }
    }

    fun setCloudConfig(url: String, key: String) {
        viewModelScope.launch {
            repository.setCloudConfig(url, key)
        }
    }

    // --- V2 Feature Operations ---

    // Focus Timer
    fun startFocusSession(
        durationMinutes: Int,
        habitId: Long? = null,
        taskId: Long? = null,
        taskTitle: String? = null,
        isBreak: Boolean = false
    ) {
        FocusForegroundService.startFocus(
            context = getApplication(),
            durationMinutes = durationMinutes,
            habitId = habitId,
            taskId = taskId,
            taskTitle = taskTitle,
            isBreak = isBreak
        )
    }

    fun startFocusSession(durationMinutes: Int, habitId: Long?, isBreak: Boolean) {
        startFocusSession(durationMinutes, habitId, null, null, isBreak)
    }

    fun pauseFocusSession() {
        FocusForegroundService.pauseFocus(getApplication())
    }

    fun resumeFocusSession() {
        FocusForegroundService.resumeFocus(getApplication())
    }

    fun stopFocusSession() {
        FocusForegroundService.stopFocus(getApplication())
        refreshStats()
    }

    // Daily Intention
    fun saveDailyIntention(text: String, isCompleted: Boolean = false) {
        viewModelScope.launch {
            repository.saveDailyIntention(todayEpochDay, text, isCompleted)
        }
    }

    fun toggleDailyIntention(intention: DailyIntentionEntity) {
        viewModelScope.launch {
            repository.toggleDailyIntention(intention)
        }
    }

    // Journal
    fun saveJournalEntry(content: String, mood: String?) {
        viewModelScope.launch {
            repository.saveJournalEntry(todayEpochDay, content, mood)
        }
    }

    fun deleteJournalEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
            _undoEvents.emit(
                UndoAction(
                    message = "Journal entry removed",
                    onUndo = {
                        repository.saveJournalEntry(entry.dateEpochDay, entry.content, entry.mood)
                    }
                )
            )
        }
    }

    // Weekly Review
    fun generateCurrentWeekReviewDraft(): WeeklyReviewEntity {
        val habits = activeHabits.value
        val logs = allLogs.value
        val goals = allGoals.value
        val tasks = allTasks.value
        val focusSessions = allFocusSessions.value
        val s = settings.value
        val streakResult = StreakCalculator.calculateStreaks(
            habits = habits,
            logs = logs,
            todayEpochDay = todayEpochDay,
            goldRepairEnabled = s.goldRepairEnabled
        )

        return WeeklyReviewGenerator.generateReviewDraft(
            habits = habits,
            logs = logs,
            goals = goals,
            tasks = tasks,
            focusSessions = focusSessions,
            todayEpochDay = todayEpochDay,
            weekStartMonday = s.weekStartMonday,
            streakResult = streakResult
        )
    }

    fun saveWeeklyReview(review: WeeklyReviewEntity) {
        viewModelScope.launch {
            repository.saveWeeklyReview(review)
            refreshStats()
        }
    }

    fun deleteWeeklyReview(review: WeeklyReviewEntity) {
        viewModelScope.launch {
            repository.deleteWeeklyReview(review)
            _undoEvents.emit(
                UndoAction(
                    message = "Weekly review archived",
                    onUndo = { repository.saveWeeklyReview(review) }
                )
            )
        }
    }

    // Settings V2 setters
    fun setFocusEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setFocusEnabled(enabled) } }
    }

    fun setFocusDefaultPreset(minutes: Int) {
        viewModelScope.launch { repository.updateSettings { it.setFocusDefaultPresetMinutes(minutes) } }
    }

    fun setFocusBreakMinutes(minutes: Int) {
        viewModelScope.launch { repository.updateSettings { it.setFocusBreakMinutes(minutes) } }
    }

    fun setFocusSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setFocusSoundEnabled(enabled) } }
    }

    fun setFocusVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setFocusVibrationEnabled(enabled) } }
    }

    fun setDailyIntentionEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setDailyIntentionEnabled(enabled) } }
    }

    fun setDailyIntentionReminderTime(time: String) {
        viewModelScope.launch { repository.updateSettings { it.setDailyIntentionReminderTime(time) } }
    }

    fun setJournalEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setJournalEnabled(enabled) } }
    }

    fun setJournalReminderTime(time: String) {
        viewModelScope.launch { repository.updateSettings { it.setJournalReminderTime(time) } }
    }

    fun setWeeklyReviewEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setWeeklyReviewEnabled(enabled) } }
    }

    fun setWeeklyReviewReminderTime(time: String) {
        viewModelScope.launch { repository.updateSettings { it.setWeeklyReviewReminderTime(time) } }
    }

    fun setHomeSectionOrder(order: String) {
        viewModelScope.launch { repository.updateSettings { it.setHomeSectionOrder(order) } }
    }

    fun setHiddenHomeSections(hidden: String) {
        viewModelScope.launch { repository.updateSettings { it.setHiddenHomeSections(hidden) } }
    }

    fun setQuickActionsOrder(order: String) {
        viewModelScope.launch { repository.updateSettings { it.setQuickActionsOrder(order) } }
    }

    fun resetHomePreferences() {
        viewModelScope.launch { repository.updateSettings { it.resetHomePreferences() } }
    }
}
