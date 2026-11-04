package com.kintsu.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kintsu.app.data.BackupRestoreManager
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.notifications.ReminderScheduler
import com.kintsu.app.ui.components.KintsuNavBar
import com.kintsu.app.ui.components.KintsuNavTab
import com.kintsu.app.ui.components.PermissionRequestSheet
import com.kintsu.app.ui.components.AppLockOverlay
import com.kintsu.app.security.AppLockType
import com.kintsu.app.util.PermissionHelper
import com.kintsu.app.ui.screens.FocusScreen
import com.kintsu.app.ui.screens.GoalsScreen
import com.kintsu.app.ui.screens.HabitsScreen
import com.kintsu.app.wellbeing.ui.DigitalBalanceScreen
import com.kintsu.app.wellbeing.ui.DigitalDetoxSheet
import com.kintsu.app.wellbeing.ui.QuitHabitsSheet
import com.kintsu.app.wellbeing.ui.SanctuaryShopSheet
import com.kintsu.app.wellbeing.ui.WellbeingWizardSheet
import com.kintsu.app.ui.shop.ShopHomeScreen
import com.kintsu.app.ui.screens.HomeScreen
import com.kintsu.app.ui.screens.CustomizeStudioSheet
import com.kintsu.app.ui.screens.JournalSheet
import com.kintsu.app.ui.screens.JournalEditorScreen
import com.kintsu.app.ui.screens.JournalStudioSheet
import com.kintsu.app.ui.screens.OnboardingScreen
import com.kintsu.app.ui.screens.ProgressScreen
import com.kintsu.app.ui.screens.SettingsScreen
import com.kintsu.app.ui.screens.TasksScreen
import com.kintsu.app.ui.screens.WeeklyReviewSheet
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun KintsuApp(
    viewModel: KintsuViewModel = viewModel(),
    shortcutAction: String? = null,
    onShortcutHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    var isAppUnlocked by remember(settings.appLockEnabled) {
        mutableStateOf(!settings.appLockEnabled || settings.appLockCode.isBlank())
    }

    val habits by viewModel.activeHabits.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val subtasks by viewModel.allSubtasks.collectAsState()
    val goals by viewModel.allGoals.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val advancedInsights by viewModel.advancedInsights.collectAsState()
    val homeStats by viewModel.homeStats.collectAsState()
    val availableRepairs by viewModel.availableRepairs.collectAsState()
    val selectedRoutineGroup by viewModel.selectedRoutineGroup.collectAsState()

    val focusState by viewModel.focusState.collectAsState()
    val todayIntention by viewModel.todayIntention.collectAsState()
    val allIntentions by viewModel.allIntentions.collectAsState()
    val todayJournalEntry by viewModel.todayJournalEntry.collectAsState()
    val allJournalEntries by viewModel.allJournalEntries.collectAsState()
    val allJournalCollections by viewModel.allJournalCollections.collectAsState()
    val allWeeklyReviews by viewModel.allWeeklyReviews.collectAsState()
    val allFocusSessions by viewModel.allFocusSessions.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val syncError by viewModel.syncError.collectAsState()

    val isShopOpen by viewModel.isShopOpen.collectAsState()
    val isDetoxOpen by viewModel.isDetoxOpen.collectAsState()
    val isQuitHabitsOpen by viewModel.isQuitHabitsOpen.collectAsState()
    val isWellbeingWizardOpen by viewModel.isWellbeingWizardOpen.collectAsState()
    val isDigitalBalanceOpen by viewModel.isDigitalBalanceOpen.collectAsState()
    val allAppGroups by viewModel.allAppGroups.collectAsState()
    val activeBlockRules by viewModel.activeBlockRules.collectAsState()
    val allBlockSchedules by viewModel.allBlockSchedules.collectAsState()
    val allBlockedWebsites by viewModel.allBlockedWebsites.collectAsState()
    val activeFocusLockSession by viewModel.activeFocusLockSession.collectAsState()
    val allFocusLockSessions by viewModel.allFocusLockSessions.collectAsState()
    val allInterventions by viewModel.allInterventions.collectAsState()
    val interventionsTodayCount by viewModel.interventionsTodayCount.collectAsState()
    val allBreaks by viewModel.allBreaks.collectAsState()
    val allShopItems by viewModel.allShopItems.collectAsState()
    val personalRewards by viewModel.activePersonalRewards.collectAsState()
    val walletTransactions by viewModel.walletTransactions.collectAsState()
    val stylePresets by viewModel.allStylePresets.collectAsState()
    val allAchievements by viewModel.allAchievements.collectAsState()
    val streakDays by viewModel.currentStreakDays.collectAsState()
    val activeQuitHabits by viewModel.activeQuitHabits.collectAsState()
    val allDetoxSessions by viewModel.allDetoxSessions.collectAsState()
    val activeDetox = remember(allDetoxSessions) {
        allDetoxSessions.firstOrNull { !it.isCompleted && it.endTimeMillis > System.currentTimeMillis() }
    }

    var isFocusOpen by remember { mutableStateOf(false) }
    var isJournalOpen by remember { mutableStateOf(false) }
    var isJournalStudioOpen by remember { mutableStateOf(false) }
    var focusReflectionPrompt by remember { mutableStateOf<String?>(null) }
    var isReflectionOpen by remember { mutableStateOf(false) }
    var isWeeklyReviewOpen by remember { mutableStateOf(false) }
    var isCustomizeOpen by remember { mutableStateOf(false) }
    var focusLinkedTask by remember { mutableStateOf<TaskEntity?>(null) }

    var externalAddHabit by remember { mutableStateOf(false) }
    var externalAddTask by remember { mutableStateOf(false) }
    var externalAddGoal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    fun triggerHaptic(type: HapticFeedbackType = HapticFeedbackType.LongPress) {
        if (settings.hapticsEnabled) {
            haptic.performHapticFeedback(type)
        }
    }

    // Handle App Shortcuts & Widget Navigation Actions
    LaunchedEffect(shortcutAction) {
        when (shortcutAction) {
            "com.kintsu.app.widget.TAB_HOME" -> {
                viewModel.selectTab(KintsuNavTab.HOME)
                onShortcutHandled()
            }
            "com.kintsu.app.widget.TAB_HABITS" -> {
                viewModel.selectTab(KintsuNavTab.HABITS)
                onShortcutHandled()
            }
            "com.kintsu.app.widget.TAB_TASKS" -> {
                viewModel.selectTab(KintsuNavTab.TASKS)
                onShortcutHandled()
            }
            "com.kintsu.app.widget.TAB_GOALS" -> {
                viewModel.selectTab(KintsuNavTab.GOALS)
                onShortcutHandled()
            }
            "com.kintsu.app.widget.TAB_PROGRESS" -> {
                viewModel.selectTab(KintsuNavTab.PROGRESS)
                onShortcutHandled()
            }
            "com.kintsu.app.shortcut.ADD_TASK", "com.kintsu.app.widget.ADD_TASK" -> {
                externalAddTask = true
                viewModel.selectTab(KintsuNavTab.TASKS)
                onShortcutHandled()
            }
            "com.kintsu.app.widget.ADD_HABIT" -> {
                viewModel.selectTab(KintsuNavTab.HABITS)
                onShortcutHandled()
            }
            "com.kintsu.app.shortcut.START_FOCUS", "com.kintsu.app.widget.OPEN_FOCUS" -> {
                isFocusOpen = true
                onShortcutHandled()
            }
            "com.kintsu.app.shortcut.JOURNAL", "com.kintsu.app.widget.OPEN_JOURNAL" -> {
                isJournalOpen = true
                onShortcutHandled()
            }
            "com.kintsu.app.widget.OPEN_INTENTION" -> {
                viewModel.selectTab(KintsuNavTab.HOME)
                onShortcutHandled()
            }
        }
    }

    var showPermissionPrompt by remember { mutableStateOf(false) }

    LaunchedEffect(settings.onboardingCompleted, settings.permissionsReviewed) {
        if (settings.onboardingCompleted && !settings.permissionsReviewed && !PermissionHelper.isNotificationPermissionGranted(context)) {
            showPermissionPrompt = true
        }
    }

    // Listen for undo events
    LaunchedEffect(Unit) {
        viewModel.undoEvents.collectLatest { action ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = action.message,
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    action.onUndo()
                }
            }
        }
    }

    // Prompt to complete parent task when all subtasks are finished
    LaunchedEffect(Unit) {
        viewModel.parentCompletionPrompts.collectLatest { parentTaskId ->
            val parentTask = tasks.find { it.id == parentTaskId } ?: return@collectLatest
            if (!parentTask.isCompleted) {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "All subtasks completed for '${parentTask.title}'",
                        actionLabel = "Complete",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.toggleTask(parentTask)
                    }
                }
            }
        }
    }

    // Schedule reminders whenever settings change
    LaunchedEffect(
        settings.notificationsEnabled,
        settings.defaultReminderTime,
        settings.goldRepairReminderEnabled,
        settings.dailyIntentionReminderTime,
        settings.journalReminderTime,
        settings.weeklyReviewReminderTime
    ) {
        ReminderScheduler.scheduleAll(context)
    }

    // Schedule or cancel background Cloud Sync via WorkManager
    LaunchedEffect(settings.syncEnabled) {
        if (settings.syncEnabled) {
            com.kintsu.app.sync.KintsuSyncWorker.schedulePeriodicSync(context)
        } else {
            com.kintsu.app.sync.KintsuSyncWorker.cancelSync(context)
        }
    }

    // Dynamic reactive theme driven by user settings!
    KintsuTheme(
        appTheme = settings.appTheme,
        themeMode = settings.themeMode,
        accentOption = settings.accentOption,
        contentDensity = settings.contentDensity,
        reducedMotion = settings.reducedMotion
    ) {
        if (!settings.onboardingCompleted) {
            OnboardingScreen(
                onComplete = { name, accent ->
                    viewModel.completeOnboarding(name, accent)
                }
            )
        } else if (isFocusOpen) {
            FocusScreen(
                focusState = focusState,
                habits = habits,
                tasks = tasks,
                initialLinkedTask = focusLinkedTask,
                recentSessions = allFocusSessions,
                onStartFocus = { duration, habitId, taskId, taskTitle, isBreak ->
                    viewModel.startFocusSession(duration, habitId, taskId, taskTitle, isBreak)
                },
                onPauseFocus = { viewModel.pauseFocusSession() },
                onResumeFocus = { viewModel.resumeFocusSession() },
                onStopFocus = { viewModel.stopFocusSession() },
                onClose = {
                    isFocusOpen = false
                    focusLinkedTask = null
                },
                onReflectInJournal = { prompt ->
                    isFocusOpen = false
                    focusLinkedTask = null
                    focusReflectionPrompt = prompt
                    isReflectionOpen = true
                },
                onCompleteTask = { task ->
                    viewModel.toggleTask(task)
                },
                defaultPresetMinutes = settings.focusDefaultPresetMinutes,
                breakLengthMinutes = settings.focusBreakMinutes
            )
        } else if (isSettingsOpen) {
            BackHandler {
                viewModel.setSettingsOpen(false)
            }
            SettingsScreen(
                settings = settings,
                syncState = syncState,
                syncError = syncError,
                onSyncNow = { viewModel.syncNow() },
                onUpdateSyncEnabled = { viewModel.setSyncEnabled(it) },
                onSignIn = { email, pass, cb -> viewModel.signIn(email, pass, cb) },
                onSignUp = { email, pass, name, cb -> viewModel.signUp(email, pass, name, cb) },
                onSignInWithGoogle = { token, email, name, cb -> viewModel.signInWithGoogle(token, email, name, cb) },
                onUpdateCloudConfig = { url, key -> viewModel.setCloudConfig(url, key) },
                onSignOut = { keep -> viewModel.signOut(keep) },
                onDeleteCloudData = { cb -> viewModel.deleteCloudData(cb) },
                onUpdateDisplayName = { viewModel.setDisplayName(it) },
                onUpdateProfileAvatar = { viewModel.updateProfileAvatarId(it) },
                onUpdateThemeMode = { viewModel.setThemeMode(it) },
                onUpdateAppTheme = { viewModel.setAppTheme(it) },
                onUpdateAccent = { viewModel.setAccentOption(it) },
                onUpdateContentDensity = { viewModel.setContentDensity(it) },
                onUpdateHapticsEnabled = { viewModel.setHapticsEnabled(it) },
                onUpdateReducedMotion = { viewModel.setReducedMotion(it) },
                onUpdateNotifications = { viewModel.setNotificationsEnabled(it) },
                onUpdateReminderTime = { viewModel.setDefaultReminderTime(it) },
                onUpdateWeekStartMonday = { viewModel.setWeekStartMonday(it) },
                onUpdateGoldRepairEnabled = { viewModel.setGoldRepairEnabled(it) },
                onUpdateNotifyOnlyIfNotDone = { viewModel.setNotifyOnlyIfNotDone(it) },
                onUpdateGoldRepairReminderEnabled = { viewModel.setGoldRepairReminderEnabled(it) },
                onUpdateFocusEnabled = { viewModel.setFocusEnabled(it) },
                onUpdateFocusDefaultPreset = { viewModel.setFocusDefaultPreset(it) },
                onUpdateFocusBreakMinutes = { viewModel.setFocusBreakMinutes(it) },
                onUpdateFocusSoundEnabled = { viewModel.setFocusSoundEnabled(it) },
                onUpdateFocusVibrationEnabled = { viewModel.setFocusVibrationEnabled(it) },
                onUpdateDailyIntentionEnabled = { viewModel.setDailyIntentionEnabled(it) },
                onUpdateDailyIntentionReminderTime = { viewModel.setDailyIntentionReminderTime(it) },
                onUpdateJournalEnabled = { viewModel.setJournalEnabled(it) },
                onUpdateJournalReminderTime = { viewModel.setJournalReminderTime(it) },
                onUpdateWeeklyReviewEnabled = { viewModel.setWeeklyReviewEnabled(it) },
                onUpdateWeeklyReviewReminderTime = { viewModel.setWeeklyReviewReminderTime(it) },
                onUpdateHomeSectionOrder = { viewModel.setHomeSectionOrder(it) },
                onUpdateHiddenHomeSections = { viewModel.setHiddenHomeSections(it) },
                onUpdateQuickActionsOrder = { viewModel.setQuickActionsOrder(it) },
                onResetHomePreferences = { viewModel.resetHomePreferences() },
                onUpdateAppLockEnabled = { viewModel.setAppLockEnabled(it) },
                onUpdateAppLockType = { viewModel.setAppLockType(it) },
                onUpdateAppLockCode = { viewModel.setAppLockCode(it) },
                onUpdateAppLockBiometricEnabled = { viewModel.setAppLockBiometricEnabled(it) },
                onTriggerEncryptedBackup = { cb -> viewModel.triggerAutoBackup(cb) },
                onRestoreEncryptedBackup = { cb -> viewModel.restoreFromEncryptedBackup(cb) },
                onExportData = { viewModel.exportDataJson() },
                onExportToFile = { uri ->
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        val json = BackupRestoreManager.createBackupJson(viewModel.database, viewModel.settings.value)
                        os.write(json.toByteArray(Charsets.UTF_8))
                    }
                },
                onImportFromFile = { uri, mode ->
                    try {
                        val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                        val validation = BackupRestoreManager.validateBackup(content)
                        if (validation.isValid && validation.data != null) {
                            BackupRestoreManager.applyBackup(viewModel.database, validation.data, mode, viewModel.settingsStore)
                            viewModel.refreshStats()
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception(validation.errorMessage ?: "Validation failed"))
                        }
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                },
                onOpenDigitalBalance = { viewModel.setDigitalBalanceOpen(true) },
                onOpenWellbeingWizard = { viewModel.setWellbeingWizardOpen(true) },
                onOpenDetox = { viewModel.setDetoxOpen(true) },
                onOpenQuitHabits = { viewModel.setQuitHabitsOpen(true) },
                stylePresets = stylePresets,
                onApplyStylePreset = { viewModel.applyStylePreset(it) },
                onSaveStylePreset = { name, theme, accent, icon, skin, face ->
                    viewModel.saveStylePreset(name, theme, accent, icon, skin, face)
                },
                onDeleteStylePreset = { viewModel.deleteStylePreset(it) },
                onRestoreDefaultPresets = { viewModel.restoreDefaultStylePresets() },
                onUpdateLauncherIcon = { viewModel.setLauncherIcon(it, context) },
                allAchievements = allAchievements,
                onUpdateWellbeingEnabled = { viewModel.setWellbeingEnabled(it) },
                onUpdateStrictness = { viewModel.setWellbeingStrictness(it) },
                onUpdateScreenTimeGoalMinutes = { viewModel.setScreenTimeGoalMinutes(it) },
                onResetFailSafe = { viewModel.resetFailSafe() },
                onResetData = { viewModel.resetAllData() },
                onDeleteAccount = { onResult ->
                    viewModel.deleteAccount { res ->
                        onResult(res)
                    }
                },
                onClose = { viewModel.setSettingsOpen(false) }
            )
        } else {
            // From any non-Home tab, back returns to previous step or Home first
            if (currentTab != KintsuNavTab.HOME) {
                BackHandler {
                    viewModel.popBackStack()
                }
            }

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    KintsuNavBar(
                        selectedTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        onTabReselected = { tab ->
                            if (tab == KintsuNavTab.HOME) {
                                viewModel.onHomeTabReselected()
                            }
                        }
                    )
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                containerColor = KintsuTheme.colors.background
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 640.dp)
                            .fillMaxWidth()
                    ) {
                    Crossfade(
                        targetState = currentTab,
                        animationSpec = tween(durationMillis = if (settings.reducedMotion) 0 else 240),
                        label = "tabCrossfade"
                    ) { tab ->
                        when (tab) {
                            KintsuNavTab.HOME -> {
                                HomeScreen(
                                    userName = settings.displayName,
                                    homeStats = homeStats,
                                    habits = habits,
                                    todayLogs = todayLogs,
                                    availableRepairs = availableRepairs,
                                    focusState = focusState,
                                    todayIntention = todayIntention,
                                    settings = settings,
                                    scrollToTopEvents = viewModel.homeScrollToTopEvents,
                                    onToggleHabit = {
                                        triggerHaptic()
                                        viewModel.toggleHabit(it)
                                    },
                                    onRepairHabitWithGold = { id, day ->
                                        triggerHaptic()
                                        viewModel.repairHabitWithGold(id, day)
                                    },
                                    onStartFocusPreset = { duration -> viewModel.startFocusSession(durationMinutes = duration, isBreak = false) },
                                    onOpenFocus = { isFocusOpen = true },
                                    onSaveIntention = { text, completed -> viewModel.saveDailyIntention(text, completed) },
                                    onToggleIntention = { viewModel.toggleDailyIntention(it) },
                                    onOpenAddHabit = {
                                        externalAddHabit = true
                                        viewModel.selectTab(KintsuNavTab.HABITS)
                                    },
                                    onOpenAddTask = {
                                        externalAddTask = true
                                        viewModel.selectTab(KintsuNavTab.TASKS)
                                    },
                                    onOpenAddGoal = {
                                        externalAddGoal = true
                                        viewModel.selectTab(KintsuNavTab.GOALS)
                                    },
                                    onOpenJournal = { isJournalStudioOpen = true },
                                    onOpenReflection = { isReflectionOpen = true },
                                    allJournalEntries = allJournalEntries,
                                    onOpenWeeklyReview = { isWeeklyReviewOpen = true },
                                    onNavigateToTasks = { viewModel.selectTab(KintsuNavTab.TASKS) },
                                    onNavigateToProgress = { viewModel.selectTab(KintsuNavTab.PROGRESS) },
                                    onOpenSettings = { viewModel.setSettingsOpen(true) },
                                    onOpenShop = { viewModel.setShopOpen(true) },
                                    onOpenCustomization = { isCustomizeOpen = true },
                                    onOpenDetox = { viewModel.setDetoxOpen(true) },
                                    onOpenQuitHabits = { viewModel.setQuitHabitsOpen(true) },
                                    onOpenDigitalBalance = { viewModel.setDigitalBalanceOpen(true) },
                                    onOpenWellbeingWizard = { viewModel.setWellbeingWizardOpen(true) }
                                )
                            }

                            KintsuNavTab.HABITS -> {
                                HabitsScreen(
                                    userName = settings.displayName,
                                    currentStreak = stats.currentStreak,
                                    habits = habits,
                                    todayLogs = todayLogs,
                                    availableRepairs = availableRepairs,
                                    goals = goals,
                                    selectedRoutineGroup = selectedRoutineGroup,
                                    onSelectRoutineGroup = { viewModel.setSelectedRoutineGroup(it) },
                                    onToggleHabit = {
                                        triggerHaptic()
                                        viewModel.toggleHabit(it)
                                    },
                                    onToggleHabitWithDetails = { id, note, mood ->
                                        triggerHaptic()
                                        viewModel.toggleHabit(id, note, mood)
                                    },
                                    onRepairHabitWithGold = { id, day ->
                                        triggerHaptic()
                                        viewModel.repairHabitWithGold(id, day)
                                    },
                                    onMendWithGold = {
                                        triggerHaptic()
                                        viewModel.mendWithGold(it)
                                    },
                                    onAddHabit = { name, icon, color, freq, mask, reminder, reminderTimes, target, routine, linkedGoalId ->
                                        viewModel.addHabit(name, icon, color, freq, mask, reminder, reminderTimes, target, routine, linkedGoalId)
                                    },
                                    onUpdateHabit = { viewModel.updateHabit(it) },
                                    onArchiveHabit = { id, archive -> viewModel.archiveHabit(id, archive) },
                                    onDeleteHabit = { viewModel.deleteHabit(it) },
                                    onOpenSettings = { viewModel.setSettingsOpen(true) },
                                    onBackClick = { viewModel.popBackStack() },
                                    focusState = focusState,
                                    onOpenFocus = { isFocusOpen = true },
                                    todayIntention = todayIntention,
                                    onSaveIntention = { text, completed -> viewModel.saveDailyIntention(text, completed) },
                                    onToggleIntention = { viewModel.toggleDailyIntention(it) },
                                    todayJournalEntry = todayJournalEntry,
                                    onOpenJournal = { isJournalOpen = true },
                                    onOpenWeeklyReview = { isWeeklyReviewOpen = true },
                                    settings = settings,
                                    externalOpenAddSheet = externalAddHabit,
                                    onExternalAddSheetConsumed = { externalAddHabit = false }
                                )
                            }

                            KintsuNavTab.TASKS -> {
                                TasksScreen(
                                    tasks = tasks,
                                    goals = goals,
                                    habits = habits,
                                    todayLogs = todayLogs,
                                    subtasks = subtasks,
                                    onToggleHabit = {
                                        triggerHaptic()
                                        viewModel.toggleHabit(it)
                                    },
                                    onToggleTask = {
                                        triggerHaptic()
                                        viewModel.toggleTask(it)
                                    },
                                    onAddTask = { title, dueDate, priority, goalId, recType, mask, recActive, plannedDate, timeBlock, category, notes, estimatedMinutes, initialSubtasks ->
                                        viewModel.addTask(
                                            title = title,
                                            dueDate = dueDate,
                                            priority = priority,
                                            linkedGoalId = goalId,
                                            recurrenceType = recType,
                                            recurrenceWeekdaysMask = mask,
                                            recurrenceActive = recActive,
                                            plannedDateEpochDay = plannedDate,
                                            plannedTimeBlock = timeBlock,
                                            category = category,
                                            notes = notes,
                                            estimatedMinutes = estimatedMinutes,
                                            initialSubtasks = initialSubtasks
                                        )
                                    },
                                    onUpdateTask = { viewModel.updateTask(it) },
                                    onDeleteTask = { viewModel.deleteTask(it) },
                                    onAddSubtask = { taskId, title -> viewModel.addSubtask(taskId, title) },
                                    onToggleSubtask = {
                                        triggerHaptic(HapticFeedbackType.TextHandleMove)
                                        viewModel.toggleSubtask(it)
                                    },
                                    onUpdateSubtask = { viewModel.updateSubtask(it) },
                                    onDeleteSubtask = { viewModel.deleteSubtask(it) },
                                    onReorderSubtasks = { viewModel.reorderSubtasks(it) },
                                    onPlanTaskForToday = { taskId, timeBlock -> viewModel.planTaskForToday(taskId, timeBlock) },
                                    onRemoveTaskFromPlan = { viewModel.removeTaskFromPlan(it) },
                                    onRescheduleTask = { taskId, newDue -> viewModel.rescheduleTask(taskId, newDue) },
                                    onSaveDailyPlan = { viewModel.saveDailyPlan(it) },
                                    onStartFocusForTask = { task ->
                                        focusLinkedTask = task
                                        isFocusOpen = true
                                    },
                                    onOpenSettings = { viewModel.setSettingsOpen(true) },
                                    onBackClick = { viewModel.popBackStack() },
                                    externalOpenAddSheet = externalAddTask,
                                    onExternalAddSheetConsumed = { externalAddTask = false }
                                )
                            }

                            KintsuNavTab.GOALS -> {
                                GoalsScreen(
                                    goals = goals,
                                    tasks = tasks,
                                    habits = habits,
                                    todayLogs = todayLogs,
                                    onAddGoal = { title, targetDate, progressType, manualProgress ->
                                        viewModel.addGoal(title, targetDate, progressType, manualProgress)
                                    },
                                    onUpdateGoal = { viewModel.updateGoal(it) },
                                    onDeleteGoal = { viewModel.deleteGoal(it) },
                                    onOpenSettings = { viewModel.setSettingsOpen(true) },
                                    onBackClick = { viewModel.popBackStack() },
                                    externalOpenAddSheet = externalAddGoal,
                                    onExternalAddSheetConsumed = { externalAddGoal = false }
                                )
                            }

                            KintsuNavTab.PROGRESS -> {
                                ProgressScreen(
                                    stats = stats,
                                    insights = advancedInsights,
                                    logs = allLogs,
                                    goals = goals,
                                    tasks = tasks,
                                    habits = habits,
                                    focusSessions = allFocusSessions,
                                    allIntentions = allIntentions,
                                    allJournalEntries = allJournalEntries,
                                    allWeeklyReviews = allWeeklyReviews,
                                    onDeleteJournalEntry = { viewModel.deleteJournalEntry(it) },
                                    onDeleteWeeklyReview = { viewModel.deleteWeeklyReview(it) },
                                    onOpenSettings = { viewModel.setSettingsOpen(true) },
                                    onBackClick = { viewModel.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isJournalStudioOpen) {
        JournalStudioSheet(
            allEntries = allJournalEntries,
            collections = allJournalCollections,
            onDismiss = { isJournalStudioOpen = false },
            onSaveEntry = { entry ->
                viewModel.saveRichJournalEntry(entry)
            },
            onDeleteEntry = { entry ->
                viewModel.deleteJournalEntry(entry)
            },
            onToggleFavorite = { entry ->
                viewModel.toggleFavoriteJournalEntry(entry)
            }
        )
    }

    if (isReflectionOpen) {
        JournalSheet(
            existingEntry = todayJournalEntry,
            onDismiss = {
                isReflectionOpen = false
                focusReflectionPrompt = null
            },
            onSave = { content, mood, tags ->
                viewModel.saveJournalEntry(content, mood, tags)
                isReflectionOpen = false
                focusReflectionPrompt = null
            }
        )
    }

    if (isJournalOpen) {
        JournalEditorScreen(
            initialEntry = todayJournalEntry,
            prefilledPrompt = focusReflectionPrompt,
            collections = allJournalCollections,
            onDismiss = {
                isJournalOpen = false
                focusReflectionPrompt = null
            },
            onSaveEntry = { entry ->
                viewModel.saveRichJournalEntry(entry)
                isJournalOpen = false
                focusReflectionPrompt = null
            },
            onDeleteEntry = { entry ->
                viewModel.deleteJournalEntry(entry)
                isJournalOpen = false
                focusReflectionPrompt = null
            }
        )
    }

        if (isWeeklyReviewOpen) {
            val reviewDraft = remember(allLogs, habits, tasks, goals, allFocusSessions) {
                viewModel.generateCurrentWeekReviewDraft()
            }
            WeeklyReviewSheet(
                draftReview = reviewDraft,
                onDismiss = { isWeeklyReviewOpen = false },
                onSave = { review ->
                    viewModel.saveWeeklyReview(review)
                    isWeeklyReviewOpen = false
                }
            )
        }

        if (showPermissionPrompt) {
            PermissionRequestSheet(
                onDismissRequest = {
                    showPermissionPrompt = false
                    viewModel.setPermissionsReviewed(true)
                },
                onContinue = {
                    showPermissionPrompt = false
                    viewModel.setPermissionsReviewed(true)
                }
            )
        }

        if (isShopOpen) {
            ShopHomeScreen(
                coinsBalance = settings.coinsBalance,
                coinsEarnedTotal = settings.coinsEarnedTotal,
                currentSettings = settings,
                allShopItems = allShopItems,
                personalRewards = personalRewards,
                walletTransactions = walletTransactions,
                stylePresets = stylePresets,
                allAchievements = allAchievements,
                streakDays = streakDays,
                onPurchaseItem = { viewModel.purchaseShopItem(it) },
                onEquipItem = { viewModel.equipShopItem(it) },
                onCreatePersonalReward = { title, desc, cost, cat, icon ->
                    viewModel.createPersonalReward(title, desc, cost, cat, icon)
                },
                onRedeemPersonalReward = { viewModel.redeemPersonalReward(it) },
                onArchivePersonalReward = { viewModel.archivePersonalReward(it) },
                onSaveStylePreset = { name, theme, accent, icon, skin, face ->
                    viewModel.saveStylePreset(name, theme, accent, icon, skin, face)
                },
                onRenameStylePreset = { preset, newName ->
                    viewModel.renameStylePreset(preset, newName)
                },
                onApplyStylePreset = { preset ->
                    viewModel.applyStylePreset(preset)
                },
                onDeleteStylePreset = { viewModel.deleteStylePreset(it) },
                onRestoreDefaultPresets = { viewModel.restoreDefaultStylePresets() },
                onDismiss = { viewModel.setShopOpen(false) }
            )
        }

        if (isDetoxOpen) {
            DigitalDetoxSheet(
                activeDetox = activeDetox,
                onStartDetox = { dur, intentStr, allowPkg, dnd ->
                    viewModel.startDigitalDetox(dur, intentStr, allowPkg, dnd)
                },
                onEndDetoxEarly = {
                    activeDetox?.let { viewModel.endDigitalDetoxEarly(it) }
                },
                onDismiss = { viewModel.setDetoxOpen(false) }
            )
        }

        if (isQuitHabitsOpen) {
            QuitHabitsSheet(
                quitHabits = activeQuitHabits,
                onAddQuitHabit = { name, ifThen, alt, cName, cPhone ->
                    viewModel.addQuitHabit(name, ifThen, alt, cName, cPhone)
                },
                onRecordSlip = { viewModel.recordQuitHabitSlip(it) },
                onRepairSlip = { viewModel.repairQuitHabitSlip(it) },
                onLogUrge = { id, intensity, tags, note ->
                    viewModel.logUrge(id, intensity, tags, note)
                },
                onDismiss = { viewModel.setQuitHabitsOpen(false) }
            )
        }

        if (isWellbeingWizardOpen) {
            WellbeingWizardSheet(
                onDismiss = { viewModel.setWellbeingWizardOpen(false) },
                onCompleted = {
                    viewModel.setWellbeingWizardOpen(false)
                    viewModel.setWellbeingEnabled(true)
                }
            )
        }

        if (isDigitalBalanceOpen) {
            BackHandler {
                viewModel.setDigitalBalanceOpen(false)
            }
            DigitalBalanceScreen(
                settings = settings,
                appGroups = allAppGroups,
                blockRules = activeBlockRules,
                blockSchedules = allBlockSchedules,
                blockedWebsites = allBlockedWebsites,
                activeFocusLock = activeFocusLockSession,
                allFocusLockSessions = allFocusLockSessions,
                interventions = allInterventions,
                breaks = allBreaks,
                interventionsTodayCount = interventionsTodayCount,
                activeQuitHabits = activeQuitHabits,
                onBack = { viewModel.setDigitalBalanceOpen(false) },
                onSaveAppGroup = { name, cat, pkgs -> viewModel.addAppGroup(name, cat, pkgs) },
                onSaveRule = { gid, mode, limit -> viewModel.addBlockRule(gid, mode, limit) },
                onAddWebsite = { domain, group, mode, matchSubs -> viewModel.addBlockedWebsite(domain, group, mode, matchSubs) },
                onToggleWebsite = { viewModel.updateBlockedWebsite(it) },
                onDeleteWebsite = { viewModel.deleteBlockedWebsite(it) },
                onStartFocusLock = { title, dur, groups, webs, strict, breaks ->
                    viewModel.startFocusLockSession(title, dur, groups, webs, strict, breaks)
                },
                onEndFocusLock = { session, wasEarly -> viewModel.endFocusLockSession(session, wasEarly) },
                onTakeBreak = { session, minutes -> viewModel.takeFocusLockBreak(session, minutes) },
                onSaveSchedule = { rid, start, end, days -> viewModel.addBlockSchedule(rid, start, end, days) },
                onToggleSchedule = { viewModel.updateBlockSchedule(it) },
                onSaveDailyBudget = { viewModel.setDailyScreenTimeBudgetMinutes(it) },
                onSaveReintervention = { viewModel.setReinterventionIntervalMinutes(it) },
                onSaveBreathingPreferences = { style, dur, reint, haptics ->
                    viewModel.setBreathingPreferences(style, dur, reint, haptics)
                },
                onToggleDns = { viewModel.setDnsProtectionEnabled(it) },
                onToggleMasterShield = { viewModel.setWellbeingEnabled(it) },
                onSaveShortFormEnabled = { viewModel.setShortFormBlockingEnabled(it) },
                onSaveShortFormPlatforms = { yt, ig, tt, fb, snap, web -> viewModel.setBlockShortFormPlatforms(yt, ig, tt, fb, snap, web) },
                onSaveContinuousSession = { maxS, cd ->
                    viewModel.setMaxContinuousSessionMinutes(maxS)
                    viewModel.setSessionCooldownMinutes(cd)
                },
                onSaveLaunchLimit = { en, maxL ->
                    viewModel.setAppLaunchLimitEnabled(en)
                    viewModel.setDefaultAppMaxLaunches(maxL)
                },
                onSaveBedtimeLock = { en, st, enH -> viewModel.setBedtimeLock(en, st, enH) },
                onSaveMindfulPause = { viewModel.setMindfulPauseOnLaunch(it) },
                onStartInstantDetox = { mins -> viewModel.startDigitalDetox(mins, "Instant Sanctuary Detox", "", false) },
                onSaveBedtimeSchedule = { en, st, enH, days, dnd -> viewModel.setBedtimeSchedule(en, st, enH, days, dnd) },
                onSaveNotificationBatching = { en, storeAll, historyDays, recapType, morning, afternoon, evening ->
                    viewModel.setNotificationBatching(en, storeAll, historyDays, recapType, morning, afternoon, evening)
                },
                onSaveInvincibleMode = { en, wH, wM, sh, bt ->
                    viewModel.setInvincibleMode(en, wH, wM, sh, bt)
                },
                onSaveParentalControlsProtection = { prot, tamp, unH, unM ->
                    viewModel.setParentalControlsProtection(prot, tamp, unH, unM)
                },
                onAddQuitHabit = { name, ifThen, alt, cName, cPhone ->
                    viewModel.addQuitHabit(name, ifThen, alt, cName, cPhone)
                },
                onRecordSlip = { viewModel.recordQuitHabitSlip(it) },
                onRepairSlip = { viewModel.repairQuitHabitSlip(it) },
                onLogUrge = { id, intensity, tags, note ->
                    viewModel.logUrge(id, intensity, tags, note)
                }
            )
        }
        if (settings.appLockEnabled && !isAppUnlocked && settings.appLockCode.isNotBlank()) {
            AppLockOverlay(
                storedCodeHash = settings.appLockCode,
                lockType = try { AppLockType.valueOf(settings.appLockType) } catch (e: Exception) { AppLockType.PIN },
                biometricEnabled = settings.appLockBiometricEnabled,
                onUnlocked = { isAppUnlocked = true }
            )
        }

        if (isCustomizeOpen) {
            CustomizeStudioSheet(
                currentSettings = settings,
                stylePresets = stylePresets,
                onApplyPreset = { viewModel.applyStylePreset(it) },
                onSavePreset = { name, theme, accent, icon, skin, face ->
                    viewModel.saveStylePreset(name, theme, accent, icon, skin, face)
                },
                onDeletePreset = { viewModel.deleteStylePreset(it) },
                onRestoreDefaultPresets = { viewModel.restoreDefaultStylePresets() },
                onUpdateAppTheme = { viewModel.setAppTheme(it) },
                onUpdateAccent = { viewModel.setAccentOption(it) },
                onUpdateThemeMode = { viewModel.setThemeMode(it) },
                onUpdateContentDensity = { viewModel.setContentDensity(it) },
                onUpdateLauncherIcon = { iconId -> viewModel.setLauncherIcon(iconId, context) },
                onUpdateHapticsEnabled = { viewModel.setHapticsEnabled(it) },
                onUpdateReducedMotion = { viewModel.setReducedMotion(it) },
                onDismiss = { isCustomizeOpen = false }
            )
        }
    }
}
