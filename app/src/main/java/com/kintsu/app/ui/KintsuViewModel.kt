package com.kintsu.app.ui

import com.kintsu.app.coin.FireStreakEngine
import com.kintsu.app.coin.FireStreakTier
import com.kintsu.app.coin.NegativeEconomyEngine
import com.kintsu.app.coin.CoinSecurityGuard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kintsu.app.data.AchievementTracker
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.KintsuRepository
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.ProgressStats
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.data.model.AchievementEntity
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockedWebsiteEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.BlockScheduleEntity
import com.kintsu.app.data.model.CommitmentLockEntity
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.DailyPlanItem
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
import com.kintsu.app.data.model.JournalCollectionEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.JournalTemplateEntity
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

    private val _isDigitalBalanceOpen = MutableStateFlow(false)
    val isDigitalBalanceOpen: StateFlow<Boolean> = _isDigitalBalanceOpen.asStateFlow()

    private val _isCustomizationOpen = MutableStateFlow(false)
    val isCustomizationOpen: StateFlow<Boolean> = _isCustomizationOpen.asStateFlow()

    fun setShopOpen(open: Boolean) { _isShopOpen.value = open }
    fun setDetoxOpen(open: Boolean) { _isDetoxOpen.value = open }
    fun setQuitHabitsOpen(open: Boolean) { _isQuitHabitsOpen.value = open }
    fun setWellbeingWizardOpen(open: Boolean) { _isWellbeingWizardOpen.value = open }
    fun setDigitalBalanceOpen(open: Boolean) { _isDigitalBalanceOpen.value = open }
    fun setCustomizationOpen(open: Boolean) { _isCustomizationOpen.value = open }

    val allShopItems: StateFlow<List<ShopItemEntity>> = repository.allShopItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePersonalRewards: StateFlow<List<PersonalRewardEntity>> = repository.getActivePersonalRewards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = repository.getAllWalletTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStylePresets: StateFlow<List<StylePresetEntity>> = repository.getAllStylePresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievementTracker = AchievementTracker(repository)

    val allAchievements: StateFlow<List<AchievementEntity>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val earnedAchievements: StateFlow<List<AchievementEntity>> = repository.earnedAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeQuitHabits: StateFlow<List<QuitHabitEntity>> = repository.activeQuitHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDetoxSessions: StateFlow<List<DigitalDetoxEntity>> = repository.allDetoxSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppGroups: StateFlow<List<AppGroupEntity>> = repository.allAppGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBlockRules: StateFlow<List<BlockRuleEntity>> = repository.activeBlockRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBlockSchedules: StateFlow<List<BlockScheduleEntity>> = repository.wellbeingDao.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBlockedWebsites: StateFlow<List<BlockedWebsiteEntity>> = repository.allBlockedWebsites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFocusLockSessions: StateFlow<List<FocusLockSessionEntity>> = repository.allFocusLockSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFocusLockSession: StateFlow<FocusLockSessionEntity?> = repository.observeActiveFocusLockSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allInterventions: StateFlow<List<DistractionInterventionLogEntity>> = repository.allInterventions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val interventionsTodayCount: StateFlow<Int> = repository.getInterventionsCountToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allBreaks: StateFlow<List<IntentionalBreakLogEntity>> = repository.allBreaks
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

    val currentStreakDays: StateFlow<Int> = allLogs
        .map { logs -> FireStreakEngine.calculateCurrentStreak(logs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentFireTier: StateFlow<FireStreakTier> = currentStreakDays
        .map { days -> FireStreakEngine.getTier(days) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FireStreakTier.NONE)

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

    val allJournalCollections: StateFlow<List<JournalCollectionEntity>> = repository.allJournalCollections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJournalTemplates: StateFlow<List<JournalTemplateEntity>> = repository.allJournalTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedJournalCollectionId = MutableStateFlow(0L)
    val selectedJournalCollectionId: StateFlow<Long> = _selectedJournalCollectionId.asStateFlow()

    fun selectJournalCollection(collectionId: Long) {
        _selectedJournalCollectionId.value = collectionId
    }

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
            repository.seedDefaultStylePresetsIfEmpty()
            repository.seedDefaultTemplatesIfNeeded()
            achievementTracker.evaluateAchievements()
            NegativeEconomyEngine.checkDailySlothDecay(getApplication())
        }
    }

    fun purchaseShopItem(item: ShopItemEntity) {
        viewModelScope.launch {
            repository.purchaseShopItem(item)
            achievementTracker.evaluateAchievements()
        }
    }

    fun equipShopItem(item: ShopItemEntity) {
        viewModelScope.launch { repository.equipShopItem(item) }
    }

    fun createPersonalReward(title: String, description: String, cost: Int, category: String, icon: String) {
        viewModelScope.launch { repository.createPersonalReward(title, description, cost, category, icon) }
    }

    fun redeemPersonalReward(reward: PersonalRewardEntity) {
        viewModelScope.launch { repository.redeemPersonalReward(reward) }
    }

    fun archivePersonalReward(reward: PersonalRewardEntity) {
        viewModelScope.launch { repository.archivePersonalReward(reward) }
    }

    fun deletePersonalReward(reward: PersonalRewardEntity) {
        viewModelScope.launch { repository.deletePersonalReward(reward) }
    }

    fun saveStylePreset(name: String, theme: String, accent: String, icon: String, skin: String, face: String) {
        viewModelScope.launch { repository.saveStylePreset(name, theme, accent, icon, skin, face) }
    }

    fun renameStylePreset(preset: StylePresetEntity, newName: String) {
        viewModelScope.launch { repository.renameStylePreset(preset, newName) }
    }

    fun deleteStylePreset(preset: StylePresetEntity) {
        viewModelScope.launch { repository.deleteStylePreset(preset) }
    }

    fun restoreDefaultStylePresets() {
        viewModelScope.launch { repository.restoreDefaultStylePresets() }
    }

    fun applyStylePreset(preset: StylePresetEntity) {
        viewModelScope.launch { repository.applyStylePreset(preset) }
    }

    fun evaluateAchievements() {
        viewModelScope.launch { achievementTracker.evaluateAchievements() }
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

    fun setShortFormBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setShortFormBlockingEnabled(enabled) } }
    }

    fun setBlockShortFormPlatforms(yt: Boolean, ig: Boolean, tt: Boolean, fb: Boolean, snap: Boolean, web: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setBlockShortFormPlatforms(yt, ig, tt, fb, snap, web) } }
    }

    fun setMaxContinuousSessionMinutes(minutes: Int) {
        viewModelScope.launch { repository.updateSettings { it.setMaxContinuousSessionMinutes(minutes) } }
    }

    fun setSessionCooldownMinutes(minutes: Int) {
        viewModelScope.launch { repository.updateSettings { it.setSessionCooldownMinutes(minutes) } }
    }

    fun setAppLaunchLimitEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setAppLaunchLimitEnabled(enabled) } }
    }

    fun setDefaultAppMaxLaunches(maxLaunches: Int) {
        viewModelScope.launch { repository.updateSettings { it.setDefaultAppMaxLaunches(maxLaunches) } }
    }

    fun setBedtimeLock(enabled: Boolean, startHour: Int, endHour: Int) {
        viewModelScope.launch { repository.updateSettings { it.setBedtimeLock(enabled, startHour, endHour) } }
    }

    fun setMindfulPauseOnLaunch(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setMindfulPauseOnLaunch(enabled) } }
    }

    fun setBlockNsfw(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setBlockNsfw(enabled) } }
    }

    fun setBedtimeSchedule(enabled: Boolean, startHour: Int, endHour: Int, daysMask: Int, dnd: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setBedtimeSchedule(enabled, startHour, endHour, daysMask, dnd) } }
    }

    fun setNotificationBatching(
        enabled: Boolean,
        storeAll: Boolean,
        historyDays: Int,
        recapType: String,
        morning: Boolean,
        afternoon: Boolean,
        evening: Boolean
    ) {
        viewModelScope.launch {
            repository.updateSettings {
                it.setNotificationBatching(enabled, storeAll, historyDays, recapType, morning, afternoon, evening)
            }
        }
    }

    fun setInvincibleMode(
        enabled: Boolean,
        windowHour: Int,
        windowMinute: Int,
        includeShorts: Boolean,
        includeBedtime: Boolean
    ) {
        viewModelScope.launch {
            repository.updateSettings {
                it.setInvincibleMode(enabled, windowHour, windowMinute, includeShorts, includeBedtime)
            }
        }
    }

    fun setParentalControlsProtection(
        protectedAccess: Boolean,
        tamperProtection: Boolean,
        uninstallHour: Int,
        uninstallMinute: Int
    ) {
        viewModelScope.launch {
            repository.updateSettings {
                it.setParentalControlsProtection(protectedAccess, tamperProtection, uninstallHour, uninstallMinute)
            }
        }
    }

    fun resetFailSafe() {
        viewModelScope.launch { repository.updateSettings { it.setProtectionFailSafeTripped(false) } }
    }

    fun addAppGroup(name: String, category: String, packages: String) {
        viewModelScope.launch { repository.addAppGroup(name, category, packages) }
    }

    fun addBlockRule(groupId: Long, mode: String, dailyLimitMinutes: Int) {
        viewModelScope.launch { repository.addBlockRule(groupId, mode, dailyLimitMinutes) }
    }

    fun addBlockedWebsite(domain: String, group: String, mode: String, matchSubdomains: Boolean) {
        viewModelScope.launch { repository.addBlockedWebsite(domain, group, mode, matchSubdomains) }
    }

    fun updateBlockedWebsite(website: BlockedWebsiteEntity) {
        viewModelScope.launch { repository.updateBlockedWebsite(website) }
    }

    fun deleteBlockedWebsite(website: BlockedWebsiteEntity) {
        viewModelScope.launch { repository.deleteBlockedWebsite(website) }
    }

    fun startFocusLockSession(
        title: String,
        durationMinutes: Int,
        restrictedGroupIds: String,
        restrictedWebsites: Boolean,
        strictness: String,
        breaksAllowed: Boolean
    ) {
        viewModelScope.launch {
            repository.startFocusLockSession(
                title = title,
                durationMinutes = durationMinutes,
                restrictedGroupIds = restrictedGroupIds,
                restrictedWebsites = restrictedWebsites,
                strictness = strictness,
                breaksAllowed = breaksAllowed
            )
        }
    }

    fun endFocusLockSession(session: FocusLockSessionEntity, wasEarly: Boolean) {
        viewModelScope.launch {
            repository.endFocusLockSession(session, wasEarly)
        }
    }

    fun takeFocusLockBreak(session: FocusLockSessionEntity, minutes: Int) {
        viewModelScope.launch {
            val updated = session.copy(breaksTaken = session.breaksTaken + 1)
            repository.wellbeingDao.updateFocusLockSession(updated)
            repository.logIntentionalBreak(
                target = "Focus Lock Break",
                durationMinutes = minutes,
                sessionId = session.id,
                reason = "Mindful break"
            )
        }
    }

    fun addBlockSchedule(ruleId: Long, startMin: Int, endMin: Int, daysMask: Int) {
        viewModelScope.launch {
            repository.addBlockSchedule(ruleId, startMin, endMin, daysMask)
        }
    }

    fun updateBlockSchedule(schedule: BlockScheduleEntity) {
        viewModelScope.launch {
            repository.wellbeingDao.updateSchedule(schedule)
        }
    }

    fun setDailyScreenTimeBudgetMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.updateSettings { it.setDailyScreenTimeBudgetMinutes(minutes) }
        }
    }

    fun setReinterventionIntervalMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.updateSettings { it.setReinterventionIntervalMinutes(minutes) }
        }
    }

    fun setBreathingPreferences(style: String, durationSec: Int, reinterventionMin: Int, haptics: Boolean) {
        viewModelScope.launch {
            repository.updateSettings {
                it.setInterventionStyle(style)
                it.setBreathingDurationSeconds(durationSec)
                it.setReinterventionIntervalMinutes(reinterventionMin)
                it.setWellbeingHapticsEnabled(haptics)
            }
        }
    }

    fun setDnsProtectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.setDnsProtectionEnabled(enabled) }
        }
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

    fun updateProfileAvatarId(avatarId: String) {
        viewModelScope.launch {
            repository.updateSettings { it.setProfileAvatarId(avatarId) }
        }
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
            achievementTracker.evaluateAchievements()
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
            achievementTracker.evaluateAchievements()
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
        category: String? = null,
        notes: String? = null,
        estimatedMinutes: Int? = null,
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
                planOrder = planOrder,
                category = category,
                notes = notes,
                estimatedMinutes = estimatedMinutes
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

    fun setLauncherIcon(iconId: String, context: android.content.Context) {
        viewModelScope.launch {
            repository.updateSettings { it.setLauncherIconId(iconId) }
            com.kintsu.app.ui.theme.AppIconManager.applyLauncherIcon(context, iconId)
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

    fun resetAllData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.resetAllData()
            refreshStats()
            onComplete()
        }
    }

    fun deleteAccount(onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.deleteAccount()
            refreshStats()
            onResult(res)
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
    fun saveJournalEntry(content: String, mood: String?, tags: String = "") {
        viewModelScope.launch {
            repository.saveJournalEntry(todayEpochDay, content, mood, tags)
        }
    }

    fun saveRichJournalEntry(entry: JournalEntryEntity, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.saveRichJournalEntry(entry)
            repository.awardCoins(
                amount = 5,
                reason = "Mindful Reflection Logged",
                sourceActivity = "JOURNAL",
                sourceEventId = "JOURNAL_${entry.dateEpochDay}_${System.currentTimeMillis() / 60000}"
            )
            onDone?.invoke()
        }
    }

    fun toggleFavoriteJournalEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            repository.saveRichJournalEntry(entry.copy(isFavorite = !entry.isFavorite))
        }
    }

    fun createJournalCollection(name: String, colorHex: String, iconName: String, isPrivate: Boolean = false) {
        viewModelScope.launch {
            repository.createJournalCollection(name, colorHex, iconName, isPrivate)
        }
    }

    fun updateJournalCollection(collection: JournalCollectionEntity) {
        viewModelScope.launch {
            repository.updateJournalCollection(collection)
        }
    }

    fun deleteJournalCollection(collection: JournalCollectionEntity) {
        viewModelScope.launch {
            repository.deleteJournalCollection(collection)
        }
    }

    fun saveJournalTemplate(template: JournalTemplateEntity) {
        viewModelScope.launch {
            repository.saveJournalTemplate(template)
        }
    }

    fun deleteJournalTemplate(template: JournalTemplateEntity) {
        viewModelScope.launch {
            repository.deleteJournalTemplate(template)
        }
    }

    fun moveJournalEntry(entry: JournalEntryEntity, targetCollectionId: Long) {
        viewModelScope.launch {
            repository.saveRichJournalEntry(entry.copy(collectionId = targetCollectionId, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteJournalEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
            _undoEvents.emit(
                UndoAction(
                    message = "Journal entry removed",
                    onUndo = {
                        repository.saveRichJournalEntry(entry)
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

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setAppLockEnabled(enabled) } }
    }

    fun setAppLockType(type: String) {
        viewModelScope.launch { repository.updateSettings { it.setAppLockType(type) } }
    }

    fun setAppLockCode(code: String) {
        viewModelScope.launch { repository.updateSettings { it.setAppLockCode(code) } }
    }

    fun setAppLockBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.setAppLockBiometricEnabled(enabled) } }
    }

    fun triggerAutoBackup(onResult: ((Result<com.kintsu.app.data.SecureBackupInfo>) -> Unit)? = null) {
        viewModelScope.launch {
            if (settings.value.syncEnabled) {
                try { repository.performSync() } catch (e: Exception) {}
            }
            val res = com.kintsu.app.data.SecureEncryptedBackupManager.performAutoBackup(
                context = getApplication(),
                database = database,
                settings = settings.value
            )
            if (res.isSuccess) {
                repository.updateSettings { it.setLastAutoBackupTime(System.currentTimeMillis()) }
            }
            onResult?.invoke(res)
        }
    }

    fun restoreFromEncryptedBackup(onResult: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            val res = com.kintsu.app.data.SecureEncryptedBackupManager.restoreFromEncryptedBackup(
                context = getApplication(),
                database = database
            )
            onResult(res)
        }
    }
}
