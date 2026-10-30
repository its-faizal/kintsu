package com.kintsu.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.domain.GoldRepairManager
import com.kintsu.app.domain.HabitRepairState
import com.kintsu.app.domain.focus.FocusState
import com.kintsu.app.domain.focus.FocusTimerManager
import com.kintsu.app.domain.home.HomeKeyNumbers
import com.kintsu.app.ui.components.IconAdd
import com.kintsu.app.ui.components.IconBook
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconChevronRight
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.IconFlame
import com.kintsu.app.ui.components.IconGoal
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.IconHabit
import com.kintsu.app.ui.components.IconPlay
import com.kintsu.app.ui.components.IconProgress
import com.kintsu.app.ui.components.IconTask
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuProgressRing
import com.kintsu.app.ui.components.KintsuTopBar
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    userName: String,
    homeStats: HomeKeyNumbers,
    habits: List<HabitEntity>,
    todayLogs: List<HabitLogEntity>,
    availableRepairs: List<HabitRepairState> = emptyList(),
    focusState: FocusState = FocusState(),
    todayIntention: DailyIntentionEntity? = null,
    settings: KintsuSettings = KintsuSettings(),
    scrollToTopEvents: SharedFlow<Unit>? = null,
    onToggleHabit: (Long) -> Unit,
    onRepairHabitWithGold: (Long, Long) -> Unit,
    onStartFocusPreset: (Int) -> Unit,
    onOpenFocus: () -> Unit,
    onSaveIntention: (String, Boolean) -> Unit,
    onToggleIntention: (DailyIntentionEntity) -> Unit,
    onOpenAddHabit: () -> Unit,
    onOpenAddTask: () -> Unit,
    onOpenAddGoal: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenWeeklyReview: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenShop: () -> Unit = {},
    onOpenDetox: () -> Unit = {},
    onOpenQuitHabits: () -> Unit = {},
    onOpenDigitalBalance: () -> Unit = {},
    onOpenWellbeingWizard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Listen to scroll to top events
    LaunchedEffect(scrollToTopEvents) {
        scrollToTopEvents?.collect {
            listState.animateScrollToItem(0)
        }
    }

    val todayEpoch = remember { LocalDate.now().toEpochDay() }
    val todayFormatted = remember {
        com.kintsu.app.util.KintsuDateFormatter.formatHeaderDate()
    }

    val greeting = remember(userName) {
        val hour = LocalTime.now().hour
        val salutation = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
        if (userName.isNotBlank() && userName != "Seeker") {
            "$salutation, $userName"
        } else {
            salutation
        }
    }

    // Filter habits scheduled for today
    val scheduledHabitsToday = remember(habits, todayEpoch) {
        habits.filter { !it.isArchived && GoldRepairManager.isHabitScheduledOn(it, todayEpoch) }
    }

    val todayLogsMap = remember(todayLogs) {
        todayLogs.associateBy { it.habitId }
    }

    val completedCount = remember(scheduledHabitsToday, todayLogsMap) {
        scheduledHabitsToday.count { todayLogsMap[it.id]?.isCompleted == true }
    }

    val todayProgress = remember(scheduledHabitsToday.size, completedCount) {
        if (scheduledHabitsToday.isEmpty()) 0f else completedCount.toFloat() / scheduledHabitsToday.size.toFloat()
    }

    var isEditingIntention by remember { mutableStateOf(false) }
    var intentionInput by remember { mutableStateOf(todayIntention?.text ?: "") }

    val hiddenSections = remember(settings.hiddenHomeSections) {
        settings.hiddenHomeSections.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    val sectionOrder = remember(settings.homeSectionOrder) {
        settings.homeSectionOrder.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    val quickActionsList = remember(settings.quickActionsOrder) {
        settings.quickActionsOrder.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(5)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 640.dp)
                .fillMaxWidth()
        ) {
            KintsuTopBar(
                title = greeting,
                subtitle = todayFormatted,
                onProfileClick = onOpenSettings,
                coinsBalance = settings.coinsBalance,
                onShopClick = onOpenShop
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                // Pristine Clean First-Launch Welcome Card (when 0 habits)
                if (habits.isEmpty()) {
                    item(key = "empty_rituals_welcome") {
                        KintsuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenAddHabit() }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.l),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "A Mindful Beginning",
                                    style = typography.itemTitle,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(spacing.xs))
                                Text(
                                    text = "Start with a single gentle ritual. Tap below to begin your daily journey.",
                                    style = typography.caption,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(spacing.m))
                                KintsuButton(
                                    text = "Create First Ritual",
                                    onClick = onOpenAddHabit,
                                    variant = KintsuButtonVariant.ACCENT_SOFT
                                )
                            }
                        }
                    }
                }

                // Header Focus Mini-Bar (Compact, only visible when timer is running)
                if (focusState.isRunning) {
                    item(key = "focus_mini_bar") {
                        FocusMiniBar(
                            focusState = focusState,
                            onOpenFocus = onOpenFocus
                        )
                    }
                }

                // 2. TODAY SECTION (Circular progress ring + today's habits; hides if empty)
                if (scheduledHabitsToday.isNotEmpty()) {
                    item(key = "today_section_header") {
                        KintsuCard(
                            modifier = Modifier.fillMaxWidth()
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Today's Rituals",
                                            style = typography.itemTitle,
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(spacing.xxs))
                                        Text(
                                            text = if (completedCount == scheduledHabitsToday.size) {
                                                "All rituals complete • Calm restored"
                                            } else {
                                                "$completedCount of ${scheduledHabitsToday.size} completed"
                                            },
                                            style = typography.caption,
                                            color = if (completedCount == scheduledHabitsToday.size) colors.goldSeam else colors.textSecondary
                                        )
                                    }

                                    KintsuProgressRing(
                                        progress = todayProgress,
                                        size = 48.dp,
                                        strokeWidth = 4.dp,
                                        showPercentage = true
                                    )
                                }
                            }
                        }
                    }

                    // Available Gold Repairs
                    if (availableRepairs.isNotEmpty() && settings.goldRepairEnabled) {
                        items(availableRepairs, key = { "repair_${it.habit.id}_${it.missedEpochDay}" }) { repair ->
                            KintsuCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRepairHabitWithGold(repair.habit.id, repair.missedEpochDay) },
                                backgroundColor = colors.accentSoft.copy(alpha = 0.35f),
                                borderColor = colors.goldSeam.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(spacing.m),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(colors.goldSeam.copy(alpha = 0.15f))
                                                .border(1.dp, colors.goldSeam, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconGoldSeam(color = colors.goldSeam, size = 16.dp)
                                        }
                                        Spacer(modifier = Modifier.width(spacing.m))
                                        Column {
                                            Text(
                                                text = "Repair ${repair.habit.name} with gold",
                                                style = typography.itemTitle,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "Missed seam • ${repair.remainingHours}h remaining to mend",
                                                style = typography.caption,
                                                color = colors.goldSeam
                                            )
                                        }
                                    }
                                    KintsuButton(
                                        text = "Mend",
                                        onClick = { onRepairHabitWithGold(repair.habit.id, repair.missedEpochDay) },
                                        variant = KintsuButtonVariant.ACCENT_SOFT
                                    )
                                }
                            }
                        }
                    }

                    // Today's Habits List
                    items(scheduledHabitsToday, key = { "habit_${it.id}" }) { habit ->
                        val log = todayLogsMap[habit.id]
                        val isDone = log?.isCompleted == true
                        val isRepaired = log?.isRepairedGold == true

                        KintsuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleHabit(habit.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.m),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // 48dp touch target checkbox
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .semantics {
                                                role = Role.Checkbox
                                                contentDescription = if (isDone) "Mark ${habit.name} incomplete" else "Mark ${habit.name} complete"
                                            }
                                            .clickable { onToggleHabit(habit.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
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
                                                IconGoldSeam(color = colors.goldSeam, size = 13.dp)
                                            } else if (isDone) {
                                                IconCheck(color = colors.background, size = 13.dp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(spacing.s))

                                    Column {
                                        Text(
                                            text = habit.name,
                                            style = typography.itemTitle,
                                            color = if (isDone) colors.textTertiary else colors.textPrimary,
                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                        if (!habit.routineGroup.isNullOrBlank()) {
                                            Text(
                                                text = habit.routineGroup,
                                                style = typography.caption,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                }

                                if (isRepaired) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.goldSeam.copy(alpha = 0.15f))
                                            .padding(horizontal = spacing.xs, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Repaired",
                                            style = typography.caption.copy(fontSize = 11.sp),
                                            color = colors.goldSeam
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Render Reorderable / Toggleable Sections
                sectionOrder.forEach { sectionId ->
                    if (!hiddenSections.contains(sectionId)) {
                        when (sectionId) {
                            "DAILY_INTENTION" -> {
                                if (settings.dailyIntentionEnabled) {
                                    item(key = "home_section_daily_intention") {
                                        KintsuCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isEditingIntention = true }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(spacing.m),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    // Checkbox for intention
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clickable {
                                                                if (todayIntention != null) {
                                                                    onToggleIntention(todayIntention)
                                                                } else {
                                                                    isEditingIntention = true
                                                                }
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(22.dp)
                                                                .clip(CircleShape)
                                                                .then(
                                                                    if (todayIntention?.isCompleted == true) {
                                                                        Modifier.background(colors.accent)
                                                                    } else {
                                                                        Modifier.border(1.5.dp, colors.border, CircleShape)
                                                                    }
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (todayIntention?.isCompleted == true) {
                                                                IconCheck(color = colors.background, size = 12.dp)
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(spacing.s))

                                                    Column {
                                                        Text(
                                                            text = "Daily Intention",
                                                            style = typography.caption,
                                                            color = colors.textSecondary
                                                        )
                                                        Text(
                                                            text = todayIntention?.text?.ifBlank { "What is your intention today?" }
                                                                ?: "What is your intention today?",
                                                            style = typography.body,
                                                            color = if (todayIntention?.isCompleted == true) colors.textTertiary else colors.textPrimary,
                                                            textDecoration = if (todayIntention?.isCompleted == true) TextDecoration.LineThrough else TextDecoration.None,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "FOCUS" -> {
                                if (settings.focusEnabled) {
                                    item(key = "home_section_focus") {
                                        KintsuCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenFocus() }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(spacing.m),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(if (focusState.isRunning) colors.accentSoft else colors.surfaceElevated),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        IconClock(color = colors.accent, size = 20.dp)
                                                    }
                                                    Spacer(modifier = Modifier.width(spacing.m))
                                                    Column {
                                                        Text(
                                                            text = if (focusState.isRunning) {
                                                                "Focus In Progress"
                                                            } else {
                                                                "${settings.focusDefaultPresetMinutes} Min Focus"
                                                            },
                                                            style = typography.itemTitle,
                                                            color = colors.textPrimary
                                                        )
                                                        Text(
                                                            text = if (focusState.isRunning) {
                                                                val remSeconds = FocusTimerManager.getRemainingSeconds(focusState)
                                                                val m = remSeconds / 60
                                                                val s = remSeconds % 60
                                                                String.format("%02d:%02d remaining • Tap to open", m, s)
                                                            } else {
                                                                "One tap to begin undisturbed time"
                                                            },
                                                            style = typography.caption,
                                                            color = colors.textSecondary
                                                        )
                                                    }
                                                }

                                                KintsuButton(
                                                    text = if (focusState.isRunning) "View" else "Start",
                                                    onClick = {
                                                        if (focusState.isRunning) {
                                                            onOpenFocus()
                                                        } else {
                                                            onStartFocusPreset(settings.focusDefaultPresetMinutes)
                                                        }
                                                    },
                                                    variant = KintsuButtonVariant.ACCENT_SOFT
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            "KEY_NUMBERS" -> {
                                item(key = "home_section_key_numbers") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(spacing.s)
                                    ) {
                                        // Streak Pill
                                        KintsuCard(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onNavigateToProgress() }
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = spacing.m, horizontal = spacing.s),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    IconFlame(color = colors.accent, size = 16.dp)
                                                    Spacer(modifier = Modifier.width(spacing.xxs))
                                                    Text(
                                                        text = "${homeStats.currentStreak}",
                                                        style = typography.itemTitle,
                                                        color = colors.textPrimary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Day Streak",
                                                    style = typography.caption.copy(fontSize = 11.sp),
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }

                                        // Tasks Due Today Pill
                                        KintsuCard(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onNavigateToTasks() }
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = spacing.m, horizontal = spacing.s),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    IconTask(color = colors.accent, size = 16.dp)
                                                    Spacer(modifier = Modifier.width(spacing.xxs))
                                                    Text(
                                                        text = "${homeStats.tasksDueToday}",
                                                        style = typography.itemTitle,
                                                        color = colors.textPrimary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Due Today",
                                                    style = typography.caption.copy(fontSize = 11.sp),
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }

                                        // Focus Minutes Today Pill
                                        KintsuCard(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onOpenFocus() }
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = spacing.m, horizontal = spacing.s),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    IconClock(color = colors.accent, size = 16.dp)
                                                    Spacer(modifier = Modifier.width(spacing.xxs))
                                                    Text(
                                                        text = "${homeStats.focusMinutesToday}m",
                                                        style = typography.itemTitle,
                                                        color = colors.textPrimary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Focus Today",
                                                    style = typography.caption.copy(fontSize = 11.sp),
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            "QUICK_ACTIONS" -> {
                                if (quickActionsList.isNotEmpty()) {
                                    item(key = "home_section_quick_actions") {
                                        KintsuCard(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = spacing.m, horizontal = spacing.s)
                                            ) {
                                                Text(
                                                    text = "Quick Actions",
                                                    style = typography.caption,
                                                    color = colors.textSecondary,
                                                    modifier = Modifier.padding(start = spacing.xs, bottom = spacing.s)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceAround,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    quickActionsList.forEach { actionId ->
                                                        when (actionId) {
                                                            "ADD_HABIT" -> HomeActionButton(
                                                                icon = { IconHabit(color = colors.accent, size = 20.dp) },
                                                                label = "Habit",
                                                                onClick = onOpenAddHabit
                                                            )
                                                            "ADD_TASK" -> HomeActionButton(
                                                                icon = { IconTask(color = colors.accent, size = 20.dp) },
                                                                label = "Task",
                                                                onClick = onOpenAddTask
                                                            )
                                                            "START_FOCUS" -> HomeActionButton(
                                                                icon = { IconPlay(color = colors.accent, size = 20.dp) },
                                                                label = "Focus",
                                                                onClick = onOpenFocus
                                                            )
                                                            "JOURNAL" -> HomeActionButton(
                                                                icon = { IconBook(color = colors.accent, size = 20.dp) },
                                                                label = "Journal",
                                                                onClick = onOpenJournal
                                                            )
                                                            "NEW_GOAL" -> HomeActionButton(
                                                                icon = { IconGoal(color = colors.accent, size = 20.dp) },
                                                                label = "Goal",
                                                                onClick = onOpenAddGoal
                                                            )
                                                            "WEEKLY_REVIEW" -> HomeActionButton(
                                                                icon = { IconProgress(color = colors.accent, size = 20.dp) },
                                                                label = "Review",
                                                                onClick = onOpenWeeklyReview
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.xxxl))
                }
            }
        }
    }

    // Daily Intention Edit Dialog
    if (isEditingIntention) {
        AlertDialog(
            onDismissRequest = { isEditingIntention = false },
            title = {
                Text(
                    text = "Daily Intention",
                    style = typography.screenTitle.copy(fontSize = 18.sp),
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "What is the single quiet priority grounding your day?",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(spacing.m))
                    OutlinedTextField(
                        value = intentionInput,
                        onValueChange = { intentionInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Breathe deep and write clean code", style = typography.body) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveIntention(intentionInput.trim(), todayIntention?.isCompleted ?: false)
                        isEditingIntention = false
                    }
                ) {
                    Text("Save", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingIntention = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }
}

@Composable
private fun HomeActionButton(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.xxs, vertical = spacing.xxs)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.accentSoft.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(spacing.xs))
        Text(
            text = label,
            style = typography.caption.copy(fontSize = 11.sp),
            color = colors.textPrimary
        )
    }
}

@Composable
private fun FocusMiniBar(
    focusState: FocusState,
    onOpenFocus: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var remSeconds by remember { mutableLongStateOf(FocusTimerManager.getRemainingSeconds(focusState)) }
    LaunchedEffect(focusState) {
        while (focusState.isRunning) {
            remSeconds = FocusTimerManager.getRemainingSeconds(focusState)
            delay(500L)
        }
    }

    val m = remSeconds / 60
    val s = remSeconds % 60
    val timeStr = String.format("%02d:%02d", m, s)

    KintsuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenFocus() },
        backgroundColor = colors.accentSoft.copy(alpha = 0.5f),
        borderColor = colors.accent.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.m, vertical = spacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                )
                Spacer(modifier = Modifier.width(spacing.s))
                val inFocusLabel = if (focusState.linkedTaskTitle != null) {
                    "Focusing: ${focusState.linkedTaskTitle.take(18)}${if (focusState.linkedTaskTitle!!.length > 18) "…" else ""} • $timeStr"
                } else {
                    "In Focus • $timeStr remaining"
                }
                Text(
                    text = inFocusLabel,
                    style = typography.caption,
                    color = colors.accentText
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Return",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.width(spacing.xxs))
                IconChevronRight(color = colors.textSecondary, size = 14.dp)
            }
        }
    }
}
