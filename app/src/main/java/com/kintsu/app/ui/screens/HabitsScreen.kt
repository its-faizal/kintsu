package com.kintsu.app.ui.screens

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.FrequencyType
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.WeeklyReviewEntity
import com.kintsu.app.domain.HabitRepairState
import com.kintsu.app.domain.focus.FocusState
import com.kintsu.app.domain.focus.FocusTimerManager
import com.kintsu.app.domain.review.WeeklyReviewGenerator
import com.kintsu.app.ui.components.IconAdd
import com.kintsu.app.ui.components.IconArchive
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.IconFlame
import com.kintsu.app.ui.components.IconGoal
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.IconHabit
import com.kintsu.app.ui.components.IconTrash
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuEmptyState
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.components.KintsuIconButton
import com.kintsu.app.ui.components.KintsuTopBar
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    userName: String,
    currentStreak: Int,
    habits: List<HabitEntity>,
    todayLogs: List<HabitLogEntity>,
    availableRepairs: List<HabitRepairState> = emptyList(),
    goals: List<GoalEntity> = emptyList(),
    selectedRoutineGroup: String? = null,
    onSelectRoutineGroup: (String?) -> Unit = {},
    onToggleHabit: (Long) -> Unit,
    onToggleHabitWithDetails: (Long, String?, String?) -> Unit = { id, _, _ -> onToggleHabit(id) },
    onRepairHabitWithGold: (Long, Long) -> Unit = { _, _ -> },
    onMendWithGold: (Long) -> Unit,
    onAddHabit: (name: String, icon: String, colorHex: String, freq: FrequencyType, mask: Int, reminder: String?, reminderTimes: String, target: Int, routine: String?, linkedGoalId: Long?) -> Unit,
    onUpdateHabit: (HabitEntity) -> Unit,
    onArchiveHabit: (Long, Boolean) -> Unit,
    onDeleteHabit: (HabitEntity) -> Unit,
    onOpenSettings: () -> Unit,
    focusState: FocusState = FocusState(),
    onOpenFocus: () -> Unit = {},
    todayIntention: DailyIntentionEntity? = null,
    onSaveIntention: (String, Boolean) -> Unit = { _, _ -> },
    onToggleIntention: (DailyIntentionEntity) -> Unit = {},
    todayJournalEntry: JournalEntryEntity? = null,
    onOpenJournal: () -> Unit = {},
    weeklyReviewDraft: WeeklyReviewEntity? = null,
    onOpenWeeklyReview: () -> Unit = {},
    settings: KintsuSettings = KintsuSettings(),
    externalOpenAddSheet: Boolean = false,
    onExternalAddSheetConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var showAddSheet by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<HabitEntity?>(null) }
    var showArchived by remember { mutableStateOf(false) }
    var habitToLogMood by remember { mutableStateOf<HabitEntity?>(null) }

    var isEditingIntention by remember { mutableStateOf(false) }
    var intentionText by remember { mutableStateOf(todayIntention?.text ?: "") }

    LaunchedEffect(externalOpenAddSheet) {
        if (externalOpenAddSheet) {
            showAddSheet = true
            onExternalAddSheetConsumed()
        }
    }

    val todayFormatted = remember {
        val now = LocalDate.now()
        now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))
    }
    val todayEpoch = remember { LocalDate.now().toEpochDay() }
    val isEndOfWeek = remember(todayEpoch, settings.weekStartMonday) {
        WeeklyReviewGenerator.isEndOfWeek(todayEpoch, settings.weekStartMonday)
    }

    val baseHabits = if (showArchived) habits.filter { it.isArchived } else habits.filter { !it.isArchived }
    val displayedHabits = if (selectedRoutineGroup != null && !showArchived) {
        baseHabits.filter { it.routineGroup.equals(selectedRoutineGroup, ignoreCase = true) }
    } else {
        baseHabits
    }

    if (habitToLogMood != null) {
        BackHandler {
            habitToLogMood = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
        ) {
            KintsuTopBar(
                title = "Habits",
                subtitle = "Good day, $userName"
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
            item {
                Spacer(modifier = Modifier.height(spacing.xxs))
                // Streak & Date Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = todayFormatted,
                        style = typography.caption,
                        color = colors.textSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(KintsuTheme.shapes.full)
                            .background(colors.accentSoft)
                            .padding(horizontal = spacing.m, vertical = spacing.xxs)
                    ) {
                        IconFlame(color = colors.accent, size = 15.dp)
                        Spacer(modifier = Modifier.width(spacing.xxs))
                        Text(
                            text = "$currentStreak days active",
                            style = typography.caption,
                            color = colors.accent
                        )
                    }
                }
            }

            // --- V2 Feature Entry Cards ---

            // 1. Daily Intention Card
            if (settings.dailyIntentionEnabled && !showArchived) {
                item {
                    KintsuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surface
                    ) {
                        Column(modifier = Modifier.padding(spacing.m)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Daily Intention",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                                if (todayIntention != null && !isEditingIntention) {
                                    Text(
                                        text = "Edit",
                                        style = typography.caption,
                                        color = colors.accent,
                                        modifier = Modifier.clickable {
                                            intentionText = todayIntention.text
                                            isEditingIntention = true
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(spacing.xs))

                            if (isEditingIntention || todayIntention == null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                                ) {
                                    OutlinedTextField(
                                        value = intentionText,
                                        onValueChange = { intentionText = it },
                                        placeholder = {
                                            Text(
                                                text = "What matters most today?",
                                                style = typography.body,
                                                color = colors.textTertiary
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = KintsuTheme.shapes.small,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.accent,
                                            unfocusedBorderColor = colors.border
                                        )
                                    )
                                    KintsuButton(
                                        text = "Set",
                                        onClick = {
                                            if (intentionText.isNotBlank()) {
                                                onSaveIntention(intentionText, todayIntention?.isCompleted ?: false)
                                                isEditingIntention = false
                                            }
                                        },
                                        enabled = intentionText.isNotBlank(),
                                        variant = KintsuButtonVariant.ACCENT_SOFT
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (todayIntention.isCompleted) colors.accent else colors.border,
                                                shape = CircleShape
                                            )
                                            .background(if (todayIntention.isCompleted) colors.accentSoft else Color.Transparent)
                                            .clickable { onToggleIntention(todayIntention) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (todayIntention.isCompleted) {
                                            IconCheck(color = colors.accent, size = 16.dp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(spacing.s))
                                    Text(
                                        text = todayIntention.text,
                                        style = typography.body,
                                        color = if (todayIntention.isCompleted) colors.textTertiary else colors.textPrimary,
                                        textDecoration = if (todayIntention.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Mindful Focus Timer Card
            if (settings.focusEnabled && !showArchived) {
                item {
                    KintsuCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFocus() },
                        backgroundColor = if (focusState.isRunning) colors.accentSoft.copy(alpha = 0.6f) else colors.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(spacing.m),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (focusState.isRunning) colors.accent else colors.surfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                IconFlame(
                                    color = if (focusState.isRunning) colors.background else colors.accent,
                                    size = 18.dp
                                )
                            }
                            Spacer(modifier = Modifier.width(spacing.m))
                            Column {
                                Text(
                                    text = if (focusState.isRunning) {
                                        val rem = FocusTimerManager.getRemainingSeconds(focusState)
                                        val m = rem / 60
                                        val s = rem % 60
                                        String.format("In Focus • %02d:%02d remaining", m, s)
                                    } else {
                                        "Mindful Focus Timer"
                                    },
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (focusState.isRunning) {
                                        if (focusState.isPaused) "Session paused • Tap to open" else "Undisturbed presence • Tap to expand"
                                    } else {
                                        "${settings.focusDefaultPresetMinutes} min conscious interval • Tap to begin"
                                    },
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Evening Reflection Card (Journal)
            if (settings.journalEnabled && !showArchived) {
                item {
                    KintsuCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenJournal() },
                        backgroundColor = colors.surface
                    ) {
                        Column(modifier = Modifier.padding(spacing.m)) {
                            Text(
                                text = "Evening Reflection",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(spacing.xxs))
                            Text(
                                text = if (todayJournalEntry != null) {
                                    "${todayJournalEntry.mood ?: "🌿"} \"${todayJournalEntry.content.take(45)}${if (todayJournalEntry.content.length > 45) "…" else ""}\""
                                } else {
                                    "A quiet pause to close the day with awareness. Tap to write."
                                },
                                style = typography.secondary,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            // 4. Weekly Review Card (if end of week)
            if (settings.weeklyReviewEnabled && isEndOfWeek && !showArchived) {
                item {
                    KintsuCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenWeeklyReview() },
                        backgroundColor = colors.goldSeam.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(spacing.m),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.goldSeam.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconGoldSeam(color = colors.goldSeam, size = 20.dp)
                            }
                            Spacer(modifier = Modifier.width(spacing.m))
                            Column {
                                Text(
                                    text = "Weekly Review Ready",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Close this cycle with honest perspective from real data.",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Routine Group Stacking (Morning / Evening / All)
            if (!showArchived) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KintsuFilterChip(
                            label = "All Routines",
                            selected = selectedRoutineGroup == null,
                            onClick = { onSelectRoutineGroup(null) }
                        )
                        KintsuFilterChip(
                            label = "Morning",
                            selected = selectedRoutineGroup.equals("Morning", ignoreCase = true),
                            onClick = { onSelectRoutineGroup("Morning") }
                        )
                        KintsuFilterChip(
                            label = "Evening",
                            selected = selectedRoutineGroup.equals("Evening", ignoreCase = true),
                            onClick = { onSelectRoutineGroup("Evening") }
                        )
                    }
                }
            }

            // Quick actions & toggle archived
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        KintsuFilterChip(
                            label = "Active",
                            selected = !showArchived,
                            onClick = { showArchived = false }
                        )
                        KintsuFilterChip(
                            label = "Archived",
                            selected = showArchived,
                            onClick = { showArchived = true }
                        )
                    }

                    if (!showArchived) {
                        KintsuButton(
                            text = "New Habit",
                            onClick = { showAddSheet = true },
                            variant = KintsuButtonVariant.ACCENT_SOFT,
                            leadingIcon = { IconAdd(color = it, size = 16.dp) }
                        )
                    }
                }
            }

            if (displayedHabits.isEmpty()) {
                item {
                    KintsuEmptyState(
                        title = if (showArchived) "No Archived Habits" else "A Clean Ceramic Canvas",
                        description = if (showArchived) "Archived habits will rest quietly here." else "Begin your journey by adding a daily ritual.",
                        icon = { IconHabit(color = it, size = 32.dp) },
                        actionText = if (!showArchived) "Add First Habit" else null,
                        onActionClick = { showAddSheet = true }
                    )
                }
            } else {
                items(displayedHabits, key = { it.id }) { habit ->
                    val log = todayLogs.find { it.habitId == habit.id }
                    val isDone = log?.isCompleted == true
                    val isRepaired = log?.isRepairedGold == true
                    val repairState = availableRepairs.find { it.habit.id == habit.id }

                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        // Gentle Gold Repair Banner if crack exists within repair window
                        if (repairState != null) {
                            GoldRepairBanner(
                                repairState = repairState,
                                onMend = {
                                    onRepairHabitWithGold(habit.id, repairState.missedEpochDay)
                                }
                            )
                        }

                        HabitRowItem(
                            habit = habit,
                            isDone = isDone,
                            isRepaired = isRepaired,
                            note = log?.note,
                            mood = log?.mood,
                            linkedGoal = goals.find { it.id == habit.linkedGoalId },
                            onToggle = {
                                if (!isDone) {
                                    habitToLogMood = habit
                                } else {
                                    onToggleHabit(habit.id)
                                }
                            },
                            onMendGold = { onMendWithGold(habit.id) },
                            onEdit = { habitToEdit = habit },
                            onArchive = { onArchiveHabit(habit.id, !habit.isArchived) },
                            onDelete = { onDeleteHabit(habit) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacing.xxxl))
            }
        }
    }
}

    // Completion Note & Mood Tag Dialog
    habitToLogMood?.let { habit ->
        CompletionNoteAndMoodDialog(
            habit = habit,
            onDismiss = {
                onToggleHabit(habit.id)
                habitToLogMood = null
            },
            onSave = { note, mood ->
                onToggleHabitWithDetails(habit.id, note.ifBlank { null }, mood.ifBlank { null })
                habitToLogMood = null
            }
        )
    }

    if (showAddSheet) {
        HabitEditSheet(
            habit = null,
            goals = goals,
            onDismiss = { showAddSheet = false },
            onSave = { name, colorHex, freq, mask, reminder, reminderTimes, target, routine, linkedGoalId ->
                onAddHabit(name, "habit", colorHex, freq, mask, reminder, reminderTimes, target, routine, linkedGoalId)
                showAddSheet = false
            }
        )
    }

    habitToEdit?.let { habit ->
        HabitEditSheet(
            habit = habit,
            goals = goals,
            onDismiss = { habitToEdit = null },
            onSave = { name, colorHex, freq, mask, reminder, reminderTimes, target, routine, linkedGoalId ->
                onUpdateHabit(
                    habit.copy(
                        name = name,
                        colorHex = colorHex,
                        frequencyType = freq,
                        weekdaysMask = mask,
                        reminderTime = reminder,
                        reminderTimes = reminderTimes,
                        frequencyTarget = target,
                        routineGroup = routine,
                        linkedGoalId = linkedGoalId
                    )
                )
                habitToEdit = null
            }
        )
    }
}

@Composable
fun GoldRepairBanner(
    repairState: HabitRepairState,
    onMend: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isAnimating by remember { mutableStateOf(false) }
    val seamProgress = remember { Animatable(0f) }

    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KintsuTheme.shapes.medium)
            .background(colors.goldSeam.copy(alpha = 0.12f))
            .border(1.dp, colors.goldSeam.copy(alpha = 0.35f), KintsuTheme.shapes.medium)
            .padding(spacing.m)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    IconGoldSeam(color = colors.goldSeam, size = 16.dp)
                    Text(
                        text = "Gold Seam Repair Available",
                        style = typography.caption,
                        color = colors.goldSeam
                    )
                }

                Text(
                    text = "${repairState.remainingHours}h left",
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }

            Text(
                text = "Missed yesterday · Repairing with gold keeps your streak unbroken.",
                style = typography.secondary,
                color = colors.textSecondary
            )

            // Animated gold seam path
            if (isAnimating) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(vertical = 2.dp)
                ) {
                    val progress = seamProgress.value
                    drawLine(
                        color = colors.goldSeam,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width * progress, size.height / 2),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }

            if (repairState.canRepair) {
                Spacer(modifier = Modifier.height(spacing.xxs))
                KintsuButton(
                    text = if (isAnimating) "Mending with Gold..." else "Mend Crack with Gold",
                    onClick = {
                        if (!isAnimating) {
                            scope.launch {
                                isAnimating = true
                                if (!reduceMotion) {
                                    seamProgress.animateTo(1f, animationSpec = tween(400))
                                }
                                onMend()
                                isAnimating = false
                            }
                        }
                    },
                    variant = KintsuButtonVariant.ACCENT_SOFT,
                    leadingIcon = { IconGoldSeam(color = it, size = 16.dp) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (repairState.reasonDisabled != null) {
                Text(
                    text = repairState.reasonDisabled,
                    style = typography.caption,
                    color = colors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun HabitRowItem(
    habit: HabitEntity,
    isDone: Boolean,
    isRepaired: Boolean,
    note: String? = null,
    mood: String? = null,
    linkedGoal: GoalEntity? = null,
    onToggle: () -> Unit,
    onMendGold: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var showActions by remember { mutableStateOf(false) }

    KintsuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showActions = !showActions }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Checkbox Circle (Min 48dp touch target)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .semantics {
                                role = Role.Checkbox
                                contentDescription = if (isDone) "Mark ${habit.name} incomplete" else "Mark ${habit.name} complete"
                            }
                            .clickable(onClick = onToggle),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .then(
                                    when {
                                        isRepaired -> Modifier
                                            .border(1.5.dp, colors.goldSeam, CircleShape)
                                            .background(colors.goldSeam.copy(alpha = 0.2f))
                                        isDone -> Modifier.background(colors.accent)
                                        else -> Modifier.border(1.5.dp, colors.border, CircleShape)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRepaired) {
                                IconGoldSeam(color = colors.goldSeam, size = 14.dp)
                            } else if (isDone) {
                                IconCheck(color = colors.background, size = 14.dp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(spacing.s))

                    Column {
                        Text(
                            text = habit.name,
                            style = typography.itemTitle,
                            color = if (isDone) colors.textTertiary else colors.textPrimary
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.s),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (habit.frequencyType) {
                                    FrequencyType.DAILY -> "Daily"
                                    FrequencyType.SPECIFIC_DAYS -> "Scheduled Days"
                                    FrequencyType.TIMES_PER_WEEK -> "${habit.frequencyTarget}x / week"
                                    FrequencyType.TIMES_PER_DAY -> "${habit.frequencyTarget}x / day"
                                },
                                style = typography.secondary,
                                color = colors.textSecondary
                            )

                            habit.routineGroup?.let { routine ->
                                Text(
                                    text = "• $routine",
                                    style = typography.secondary,
                                    color = colors.accent
                                )
                            }

                            linkedGoal?.let { goal ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(KintsuTheme.shapes.full)
                                        .background(colors.accentSoft)
                                        .padding(horizontal = spacing.xs, vertical = 2.dp)
                                ) {
                                    IconGoal(color = colors.accent, size = 10.dp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = goal.title,
                                        style = typography.caption,
                                        color = colors.accent,
                                        maxLines = 1
                                    )
                                }
                            }

                            if (!habit.reminderTime.isNullOrBlank() || habit.reminderTimes.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconClock(color = colors.textTertiary, size = 12.dp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = habit.reminderTimes.ifBlank { habit.reminderTime ?: "" },
                                        style = typography.caption,
                                        color = colors.textTertiary
                                    )
                                }
                            }
                        }

                        // Display note & mood tag if recorded
                        if (note != null || mood != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (mood != null) {
                                    Text(
                                        text = mood,
                                        style = typography.caption,
                                        color = colors.goldSeam
                                    )
                                }
                                if (note != null) {
                                    Text(
                                        text = "“$note”",
                                        style = typography.caption,
                                        color = colors.textTertiary
                                    )
                                }
                            }
                        }
                    }
                }

                if (isRepaired) {
                    Box(
                        modifier = Modifier
                            .clip(KintsuTheme.shapes.full)
                            .background(colors.goldSeam.copy(alpha = 0.15f))
                            .padding(horizontal = spacing.s, vertical = spacing.xxs)
                    ) {
                        Text(
                            text = "Mended with Gold",
                            style = typography.caption,
                            color = colors.goldSeam
                        )
                    }
                }
            }

            // Quick Actions Drawer
            if (showActions) {
                Spacer(modifier = Modifier.height(spacing.s))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KintsuIconButton(
                        onClick = onEdit,
                        icon = { IconHabit(color = it, size = 18.dp) },
                        contentDescription = "Edit habit"
                    )
                    KintsuIconButton(
                        onClick = onArchive,
                        icon = { IconArchive(color = it, size = 18.dp) },
                        contentDescription = if (habit.isArchived) "Unarchive" else "Archive"
                    )
                    KintsuIconButton(
                        onClick = onDelete,
                        icon = { IconTrash(color = colors.accent, size = 18.dp) },
                        contentDescription = "Delete habit"
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionNoteAndMoodDialog(
    habit: HabitEntity,
    onDismiss: () -> Unit,
    onSave: (note: String, mood: String) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var note by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("🌿 Calm") }

    val moodOptions = listOf("🌿 Calm", "⛰ Grounded", "☀️ Energized", "🪷 Peaceful", "🤍 Grateful")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ritual Completed",
                style = typography.sectionHeader,
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                Text(
                    text = "How do you feel after '${habit.name}'?",
                    style = typography.body,
                    color = colors.textSecondary
                )

                // Mood selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
                ) {
                    moodOptions.forEach { mood ->
                        KintsuFilterChip(
                            label = mood,
                            selected = selectedMood == mood,
                            onClick = { selectedMood = mood }
                        )
                    }
                }

                // Optional one-line note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Add an optional reflection...", color = colors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = KintsuTheme.shapes.medium,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(note, selectedMood) }) {
                Text("Save", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip Note", color = colors.textTertiary)
            }
        },
        containerColor = colors.surface,
        shape = KintsuTheme.shapes.large
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitEditSheet(
    habit: HabitEntity?,
    goals: List<GoalEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String, freq: FrequencyType, weekdaysMask: Int, reminder: String?, reminderTimes: String, target: Int, routine: String?, linkedGoalId: Long?) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var name by remember { mutableStateOf(habit?.name ?: "") }
    var selectedColor by remember { mutableStateOf(habit?.colorHex ?: "#B8935A") }
    var frequencyType by remember { mutableStateOf(habit?.frequencyType ?: FrequencyType.DAILY) }
    var weekdaysMask by remember { mutableIntStateOf(habit?.weekdaysMask ?: 31) }
    var targetCount by remember { mutableIntStateOf(habit?.frequencyTarget ?: 1) }
    var routineGroup by remember { mutableStateOf(habit?.routineGroup ?: "") }
    var reminderTimes by remember { mutableStateOf(habit?.reminderTimes.orEmpty().ifBlank { habit?.reminderTime.orEmpty() }) }
    var hasReminder by remember { mutableStateOf(!habit?.reminderTime.isNullOrBlank() || !habit?.reminderTimes.isNullOrBlank()) }
    var selectedGoalId by remember { mutableStateOf<Long?>(habit?.linkedGoalId) }

    val curatedColors = listOf("#B8935A", "#6B8E7D", "#B86B52", "#81729E", "#7E7569")
    var showDiscardConfirm by remember { mutableStateOf(false) }

    fun requestDismiss() {
        val hasChanges = name.isNotBlank() && (
            name != (habit?.name ?: "") ||
            selectedColor != (habit?.colorHex ?: "#B8935A") ||
            frequencyType != (habit?.frequencyType ?: FrequencyType.DAILY) ||
            routineGroup != (habit?.routineGroup ?: "") ||
            selectedGoalId != habit?.linkedGoalId
        )
        if (hasChanges) {
            showDiscardConfirm = true
        } else {
            onDismiss()
        }
    }

    BackHandler {
        requestDismiss()
    }

    KintsuBottomSheet(
        onDismissRequest = { requestDismiss() },
        title = if (habit == null) "New Daily Ritual" else "Edit Ritual"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
            // Name input with validation
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text(
                        text = "e.g. Morning Stillness, Hydration, Breathwork",
                        style = typography.body,
                        color = colors.textTertiary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = KintsuTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border
                ),
                singleLine = true
            )

            // Color Palette Picker
            Text(
                text = "Color Essence",
                style = typography.caption,
                color = colors.textSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
                curatedColors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = selectedColor == hex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = hex }
                            .then(
                                if (isSelected) Modifier.border(2.dp, colors.textPrimary, CircleShape) else Modifier
                            )
                    )
                }
            }

            // Frequency
            Text(
                text = "Frequency & Rhythm",
                style = typography.caption,
                color = colors.textSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                KintsuFilterChip(
                    label = "Daily",
                    selected = frequencyType == FrequencyType.DAILY,
                    onClick = { frequencyType = FrequencyType.DAILY }
                )
                KintsuFilterChip(
                    label = "Specific Days",
                    selected = frequencyType == FrequencyType.SPECIFIC_DAYS,
                    onClick = { frequencyType = FrequencyType.SPECIFIC_DAYS }
                )
                KintsuFilterChip(
                    label = "Times/Week",
                    selected = frequencyType == FrequencyType.TIMES_PER_WEEK,
                    onClick = { frequencyType = FrequencyType.TIMES_PER_WEEK }
                )
                KintsuFilterChip(
                    label = "Times/Day",
                    selected = frequencyType == FrequencyType.TIMES_PER_DAY,
                    onClick = { frequencyType = FrequencyType.TIMES_PER_DAY }
                )
            }

            if (frequencyType == FrequencyType.SPECIFIC_DAYS) {
                Text(
                    text = "Scheduled Days of Week",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
                ) {
                    dayLabels.forEachIndexed { index, day ->
                        val bit = 1 shl index
                        val isDaySelected = (weekdaysMask and bit) != 0
                        KintsuFilterChip(
                            label = day,
                            selected = isDaySelected,
                            onClick = {
                                val toggled = if (isDaySelected) {
                                    val candidate = weekdaysMask and bit.inv()
                                    if (candidate == 0) weekdaysMask else candidate
                                } else {
                                    weekdaysMask or bit
                                }
                                weekdaysMask = toggled
                            }
                        )
                    }
                }
            }

            if (frequencyType == FrequencyType.TIMES_PER_WEEK || frequencyType == FrequencyType.TIMES_PER_DAY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Count",
                        style = typography.itemTitle,
                        color = colors.textPrimary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.s),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KintsuButton(
                            text = "-",
                            onClick = { if (targetCount > 1) targetCount-- },
                            variant = KintsuButtonVariant.SECONDARY
                        )
                        Text(
                            text = "$targetCount",
                            style = typography.itemTitle,
                            color = colors.accent
                        )
                        KintsuButton(
                            text = "+",
                            onClick = { if (targetCount < 10) targetCount++ },
                            variant = KintsuButtonVariant.SECONDARY
                        )
                    }
                }
            }

            // Routine Group (Habit Stacking)
            Text(
                text = "Routine Group (Stacking)",
                style = typography.caption,
                color = colors.textSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                KintsuFilterChip(
                    label = "None",
                    selected = routineGroup.isBlank(),
                    onClick = { routineGroup = "" }
                )
                KintsuFilterChip(
                    label = "Morning",
                    selected = routineGroup.equals("Morning", ignoreCase = true),
                    onClick = { routineGroup = "Morning" }
                )
                KintsuFilterChip(
                    label = "Evening",
                    selected = routineGroup.equals("Evening", ignoreCase = true),
                    onClick = { routineGroup = "Evening" }
                )
            }

            // Link to Goal (Optional)
            if (goals.isNotEmpty()) {
                Text(
                    text = "Link to Goal (Optional)",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    KintsuFilterChip(
                        label = "None",
                        selected = selectedGoalId == null,
                        onClick = { selectedGoalId = null }
                    )
                    goals.forEach { goal ->
                        KintsuFilterChip(
                            label = goal.title,
                            selected = selectedGoalId == goal.id,
                            onClick = {
                                selectedGoalId = if (selectedGoalId == goal.id) null else goal.id
                            }
                        )
                    }
                }
            }

            // Optional Reminder Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reminder Times (e.g. 08:30, 20:00)",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                KintsuFilterChip(
                    label = if (hasReminder) "Active" else "None",
                    selected = hasReminder,
                    onClick = {
                        hasReminder = !hasReminder
                        if (hasReminder && reminderTimes.isBlank()) reminderTimes = "08:30"
                    }
                )
            }

            if (hasReminder) {
                OutlinedTextField(
                    value = reminderTimes,
                    onValueChange = { reminderTimes = it },
                    placeholder = { Text("08:30, 20:00 (comma-separated)", color = colors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = KintsuTheme.shapes.medium,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border
                    )
                )
            }

            Spacer(modifier = Modifier.height(spacing.s))

            // Save Button
            KintsuButton(
                text = if (habit == null) "Create Ritual" else "Save Changes",
                onClick = {
                    if (name.isNotBlank()) {
                        val finalMask = when (frequencyType) {
                            FrequencyType.DAILY -> 127
                            FrequencyType.SPECIFIC_DAYS -> if (weekdaysMask == 0) 127 else weekdaysMask
                            else -> 127
                        }
                        val firstReminder = reminderTimes.split(",").firstOrNull()?.trim()
                        onSave(
                            name,
                            selectedColor,
                            frequencyType,
                            finalMask,
                            if (hasReminder) firstReminder else null,
                            if (hasReminder) reminderTimes else "",
                            targetCount,
                            routineGroup.ifBlank { null },
                            selectedGoalId
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = {
                Text(
                    text = "Discard Unsaved Ritual?",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Are you sure you want to discard them?",
                    style = typography.body,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardConfirm = false
                        onDismiss()
                    }
                ) {
                    Text("Discard", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("Keep Editing", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }
}
