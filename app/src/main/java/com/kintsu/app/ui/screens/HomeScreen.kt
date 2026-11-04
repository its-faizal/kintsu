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
import androidx.compose.ui.text.font.FontWeight
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
import com.kintsu.app.ui.components.IconLeaf
import com.kintsu.app.ui.components.IconPlay
import com.kintsu.app.ui.components.IconProgress
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.IconSparkle
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
    onOpenReflection: () -> Unit = {},
    allJournalEntries: List<com.kintsu.app.data.model.JournalEntryEntity> = emptyList(),
    onOpenWeeklyReview: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenShop: () -> Unit = {},
    onOpenCustomization: () -> Unit = {},
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

    // Creative probabilistic greetings matching the diurnal phase (User requirement)
    val greeting = remember(userName) {
        val hour = LocalTime.now().hour
        val lateNightGreetings = listOf(
            "Night owl wanderer",
            "Stillness under starlight",
            "Rest beckons, night bird",
            "Midnight philosopher",
            "Quiet nocturnal calm"
        )
        val earlyDawnGreetings = listOf(
            "First light unfolds",
            "Dawn breaks with clarity",
            "Awaken gently, early riser",
            "Fresh horizon awaits",
            "The morning breathes anew"
        )
        val morningGreetings = listOf(
            "Mindful morning begins",
            "A day of purposeful craft",
            "Morning clarity is yours",
            "Steady steps forward",
            "Breathe in morning calm"
        )
        val afternoonGreetings = listOf(
            "Midday pause & breath",
            "Steady focus in the sun",
            "Afternoon momentum",
            "Sun at its peak, mind centered",
            "Keep your center, seeker"
        )
        val eveningGreetings = listOf(
            "Golden dusk settles",
            "Twilight quietude",
            "Unwinding with gratitude",
            "The day gently slows",
            "A peaceful twilight arrives"
        )
        val nightGreetings = listOf(
            "Night bird returns to calm",
            "Closing the day in peace",
            "Gentle thoughts for the night",
            "Stars watch over your quiet",
            "Rest well, mindful soul"
        )

        val candidateList = when (hour) {
            in 0..4 -> lateNightGreetings
            in 5..7 -> earlyDawnGreetings
            in 8..11 -> morningGreetings
            in 12..16 -> afternoonGreetings
            in 17..20 -> eveningGreetings
            else -> nightGreetings
        }

        val chosen = candidateList.random()
        if (userName.isNotBlank() && userName != "Seeker") {
            "$chosen, $userName"
        } else {
            chosen
        }
    }

    // Creative daily quotes at top of home (User requirement)
    val dailyQuotes = remember {
        listOf(
            "\"The wound is the place where the Light enters you.\" \u2014 Rumi",
            "\"Adopt the pace of nature: her secret is patience.\" \u2014 Ralph Waldo Emerson",
            "\"Mended with gold, our broken pieces become our greatest strength.\" \u2014 Kintsugi",
            "\"Between stimulus and response there is a space. In that space lies our freedom.\" \u2014 Viktor Frankl",
            "\"You have power over your mind, not outside events. Realize this, and you will find strength.\" \u2014 Marcus Aurelius",
            "\"Simplicity is the ultimate sophistication.\" \u2014 Leonardo da Vinci",
            "\"Quiet the mind, and the soul will speak.\" \u2014 Ma Jaya Sati Bhagavati",
            "\"Do not dwell in the past, do not dream of the future, concentrate the mind on the present moment.\" \u2014 Buddha",
            "\"In the midst of movement and chaos, keep stillness inside of you.\" \u2014 Deepak Chopra",
            "\"To be calm is the highest achievement of the self.\" \u2014 Zen Proverb"
        )
    }
    val todayQuote = remember(todayEpoch) {
        val index = (todayEpoch.toInt().let { if (it < 0) -it else it }) % dailyQuotes.size
        dailyQuotes[index]
    }

    // Live clock ticker synced with time for focus timer on home (User requirement)
    var liveClockMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(focusState.isRunning, focusState.isPaused) {
        while (focusState.isRunning && !focusState.isPaused) {
            delay(1000L)
            liveClockMillis = System.currentTimeMillis()
        }
    }

    val liveFocusSecondsRemaining = remember(focusState, liveClockMillis) {
        FocusTimerManager.getRemainingSeconds(focusState, liveClockMillis)
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
                onShopClick = onOpenShop,
                onCustomizationClick = onOpenCustomization,
                profileAvatarId = settings.profileAvatarId
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                // --- TOP OF HOME: CREATIVE DAILY QUOTE (User requirement) ---
                item(key = "home_top_creative_quote") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surfaceElevated.copy(alpha = 0.6f))
                            .border(1.dp, colors.goldSeam.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconGoldSeam(color = colors.goldSeam, size = 16.dp)
                            Text(
                                text = todayQuote,
                                style = typography.caption.copy(
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    lineHeight = 18.sp
                                ),
                                color = colors.textPrimary
                            )
                        }
                    }
                }

                // --- TOP OF HOME: LIVE SYNCED FOCUS TIMER (User requirement) ---
                item(key = "home_live_focus_timer") {
                    KintsuCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFocus() },
                        backgroundColor = colors.surfaceElevated
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.m),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (focusState.isRunning) {
                                val minsLeft = liveFocusSecondsRemaining / 60
                                val secsLeft = liveFocusSecondsRemaining % 60
                                val timeText = String.format(Locale.US, "%02d:%02d", minsLeft, secsLeft)
                                val progress = (liveFocusSecondsRemaining.toFloat() / (focusState.durationMinutes * 60f).coerceAtLeast(1f)).coerceIn(0f, 1f)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                                            KintsuProgressRing(progress = progress, size = 46.dp, progressColor = colors.accent, showPercentage = false)
                                            IconClock(color = colors.accent, size = 18.dp)
                                        }

                                        Column {
                                            Text(
                                                text = if (focusState.isPaused) "Focus Paused" else "Focus Sanctuary Active",
                                                style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (focusState.isPaused) colors.goldSeam else colors.accent
                                            )
                                            Text(
                                                text = "$timeText remaining",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.accentSoft)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Open HUD",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent
                                        )
                                    }
                                }
                            } else {
                                // Inactive: Quick Presets synced to Home
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(colors.accentSoft),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconPlay(color = colors.accent, size = 16.dp)
                                        }
                                        Column {
                                            Text(
                                                text = "Focus Timer",
                                                style = typography.caption.copy(fontWeight = FontWeight.Bold),
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "Tap a preset to begin deep work",
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }

                                    IconChevronRight(color = colors.textTertiary, size = 16.dp)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val presets = listOf(15, 25, 45, 60)
                                    presets.forEach { duration ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.surface)
                                                .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .clickable { onStartFocusPreset(duration) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${duration}m",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 1. TODAY AT A GLANCE (Stays at the very top!)
                item(key = "home_section_key_numbers") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        Text(
                            text = "Today at a Glance",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
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

                // Header Focus Mini-Bar (Compact, only visible when timer is running)
                if (focusState.isRunning) {
                    item(key = "focus_mini_bar") {
                        FocusMiniBar(
                            focusState = focusState,
                            onOpenFocus = onOpenFocus
                        )
                    }
                }

                // 2. TODAY'S HABITS SECTION
                if (scheduledHabitsToday.isNotEmpty()) {
                    item(key = "today_section_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = spacing.xs, bottom = spacing.xxs),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Habits ($completedCount/${scheduledHabitsToday.size})",
                                style = typography.itemTitle,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "View All",
                                style = typography.caption,
                                color = colors.accent,
                                modifier = Modifier
                                    .clickable { onNavigateToTasks() }
                                    .padding(vertical = spacing.xxs)
                            )
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
                                .clip(KintsuTheme.shapes.medium)
                                .clickable { onToggleHabit(habit.id) },
                            backgroundColor = if (isDone) colors.accentSoft.copy(alpha = 0.3f) else colors.surface,
                            borderColor = if (isRepaired) colors.goldSeam else if (isDone) colors.accent.copy(alpha = 0.4f) else colors.border
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacing.m, vertical = spacing.s),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 1.5.dp,
                                            color = when {
                                                isRepaired -> colors.goldSeam
                                                isDone -> colors.accent
                                                else -> colors.border
                                            },
                                            shape = CircleShape
                                        )
                                        .background(
                                            when {
                                                isRepaired -> colors.goldSeam
                                                isDone -> colors.accent
                                                else -> Color.Transparent
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone || isRepaired) {
                                        IconCheck(color = colors.background, size = 14.dp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(spacing.m))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = habit.name,
                                        style = typography.body,
                                        color = if (isDone) colors.textSecondary else colors.textPrimary,
                                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    val habitSub = habit.routineGroup ?: habit.reminderTime
                                    if (!habitSub.isNullOrBlank()) {
                                        Text(
                                            text = habitSub,
                                            style = typography.caption,
                                            color = colors.textTertiary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (habits.isEmpty()) {
                    // Pristine Clean First-Launch Welcome Card (when 0 habits)
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

                // 3. MINDFUL PRACTICE TILES (Journal, Quick Reflection, Daily Intention, Addiction Shield)
                item(key = "mindful_practice_tiles") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.s)
                    ) {
                        Text(
                            text = "Daily Practices",
                            style = typography.caption,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = spacing.xs)
                        )

                        // Row 1: Journal & Quick Reflection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            // Journal Tile
                            KintsuCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenJournal() }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(spacing.m),
                                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(colors.accentSoft),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconBook(color = colors.accent, size = 18.dp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(KintsuTheme.shapes.small)
                                                .background(colors.surfaceElevated)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${allJournalEntries.size}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Journal",
                                        style = typography.itemTitle,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Chronicles & personal notes",
                                        style = typography.caption,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Quick Reflection Tile
                            KintsuCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenReflection() }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(spacing.m),
                                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(colors.accentSoft),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconSparkle(color = colors.accent, size = 18.dp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(KintsuTheme.shapes.small)
                                                .background(colors.surfaceElevated)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Pause",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Quick Reflection",
                                        style = typography.itemTitle,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Evening pause & mood check",
                                        style = typography.caption,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Row 2: Daily Intention & Addiction Shield
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            // Daily Intention Tile
                            KintsuCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isEditingIntention = true }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(spacing.m),
                                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(colors.accentSoft),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconLeaf(color = colors.accent, size = 18.dp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(KintsuTheme.shapes.small)
                                                .background(if (todayIntention?.isCompleted == true) colors.accentSoft else colors.surfaceElevated)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (todayIntention?.isCompleted == true) "Aligned" else "Focus",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (todayIntention?.isCompleted == true) colors.accent else colors.textSecondary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Daily Intention",
                                        style = typography.itemTitle,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = todayIntention?.text?.takeIf { it.isNotBlank() } ?: "Set your mindful focus",
                                        style = typography.caption,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Addiction Shield Tile
                            KintsuCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenDigitalBalance() }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(spacing.m),
                                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (settings.wellbeingEnabled) colors.warning.copy(alpha = 0.15f) else colors.surfaceElevated),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconShield(
                                                color = if (settings.wellbeingEnabled) colors.warning else colors.textTertiary,
                                                size = 18.dp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(KintsuTheme.shapes.small)
                                                .background(if (settings.wellbeingEnabled) colors.warning.copy(alpha = 0.15f) else colors.surfaceElevated)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (settings.wellbeingEnabled) "Active" else "Off",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (settings.wellbeingEnabled) colors.warning else colors.textSecondary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Addiction Shield",
                                        style = typography.itemTitle,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (settings.wellbeingEnabled) "Digital balance active" else "Configure pauses",
                                        style = typography.caption,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.xxl))
                }
            }
        }
    }

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