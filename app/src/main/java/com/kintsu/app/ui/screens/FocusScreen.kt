package com.kintsu.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.focus.FocusSoundEngine
import com.kintsu.app.focus.SoundscapeMode
import com.kintsu.app.domain.focus.FocusState
import com.kintsu.app.domain.focus.FocusTimerManager
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.IconFlame
import com.kintsu.app.ui.components.IconFocus
import com.kintsu.app.ui.components.IconHabit
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.components.KintsuIconButton
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(
    focusState: FocusState,
    habits: List<HabitEntity>,
    tasks: List<TaskEntity> = emptyList(),
    initialLinkedTask: TaskEntity? = null,
    recentSessions: List<FocusSessionEntity> = emptyList(),
    onStartFocus: (durationMinutes: Int, habitId: Long?, taskId: Long?, taskTitle: String?, isBreak: Boolean) -> Unit,
    onPauseFocus: () -> Unit,
    onResumeFocus: () -> Unit,
    onStopFocus: () -> Unit,
    onClose: () -> Unit,
    onReflectInJournal: (prompt: String) -> Unit = {},
    onCompleteTask: (TaskEntity) -> Unit = {},
    defaultPresetMinutes: Int = 25,
    breakLengthMinutes: Int = 5,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var selectedFocusDuration by remember { mutableIntStateOf(defaultPresetMinutes) }
    var selectedBreakDuration by remember { mutableIntStateOf(breakLengthMinutes) }
    var selectedSoundscape by remember { mutableStateOf(FocusSoundEngine.currentMode) }
    var customDuration by remember { mutableFloatStateOf(if (defaultPresetMinutes !in listOf(15, 25, 50)) defaultPresetMinutes.toFloat() else 30f) }
    var selectedHabitId by remember { mutableStateOf<Long?>(null) }
    var linkedTask by remember { mutableStateOf(initialLinkedTask) }
    var isBreakMode by remember { mutableStateOf(false) }
    var showCompletionModal by remember { mutableStateOf(false) }
    var completedTaskCandidate by remember { mutableStateOf<TaskEntity?>(null) }

    val activeDuration = if (isBreakMode) selectedBreakDuration else selectedFocusDuration

    // Ticking remaining time
    val initialSeconds = if (!focusState.isRunning) activeDuration * 60L else FocusTimerManager.getRemainingSeconds(focusState)
    var remainingSeconds by remember { mutableLongStateOf(initialSeconds) }

    LaunchedEffect(focusState, activeDuration) {
        if (!focusState.isRunning) {
            remainingSeconds = activeDuration * 60L
        } else {
            while (true) {
                remainingSeconds = FocusTimerManager.getRemainingSeconds(focusState)
                delay(500L)
            }
        }
    }

    val displaySeconds = if (focusState.isRunning) remainingSeconds else (activeDuration * 60L)
    val totalDurationSeconds = if (focusState.isRunning) {
        focusState.durationMinutes * 60L
    } else {
        activeDuration * 60L
    }

    val progressFraction = if (focusState.isRunning && totalDurationSeconds > 0) {
        ((totalDurationSeconds - remainingSeconds).toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val minutes = displaySeconds / 60
    val seconds = displaySeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    BackHandler {
        onClose()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.xl, vertical = spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.l)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (focusState.isBreak) "Rest Interval" else "Mindful Focus",
                        style = typography.screenTitle,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (focusState.isRunning) "Undisturbed stillness" else "Choose a conscious interval",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
                KintsuIconButton(
                    icon = { IconClose(color = it, size = 18.dp) },
                    contentDescription = "Close focus screen",
                    onClick = onClose
                )
            }

            // Digital Shield Active Status Pill
            if (focusState.isRunning) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.accentSoft.copy(alpha = 0.25f))
                        .border(0.5.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = spacing.s, vertical = spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
                ) {
                    IconShield(color = colors.accent, size = 13.dp)
                    Text(
                        text = "Digital Shield Active · Distractions Dimmed",
                        style = typography.caption.copy(fontSize = 11.sp),
                        color = colors.accent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Linked Task Banner when running
            if (focusState.isRunning && focusState.linkedTaskTitle != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KintsuTheme.shapes.small)
                        .background(colors.accentSoft.copy(alpha = 0.35f))
                        .border(1.dp, colors.accent.copy(alpha = 0.5f), KintsuTheme.shapes.small)
                        .padding(horizontal = spacing.m, vertical = spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    IconFocus(color = colors.accent, size = 16.dp)
                    Text(
                        text = "Focusing on: ${focusState.linkedTaskTitle}",
                        style = typography.caption,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.s))

            // Large Circular Timer Ring
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 8.dp.toPx()
                    // Track
                    drawCircle(
                        color = colors.border,
                        radius = (size.minDimension - stroke) / 2,
                        style = Stroke(stroke)
                    )
                    // Progress Arc
                    val sweepAngle = progressFraction * 360f
                    drawArc(
                        color = colors.accent,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formattedTime,
                        style = typography.screenTitle.copy(fontSize = 38.sp, lineHeight = 42.sp),
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = when {
                            !focusState.isRunning -> "Ready"
                            focusState.isPaused -> "Paused"
                            focusState.isBreak -> "Resting"
                            else -> "In Focus"
                        },
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            // Controls when running vs when idle
            if (focusState.isRunning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    if (focusState.isPaused) {
                        KintsuButton(
                            text = "Resume",
                            onClick = onResumeFocus,
                            variant = KintsuButtonVariant.PRIMARY,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        KintsuButton(
                            text = "Pause",
                            onClick = onPauseFocus,
                            variant = KintsuButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    KintsuButton(
                        text = "End Session",
                        onClick = {
                            val activeTask = linkedTask ?: tasks.find { it.id == focusState.linkedTaskId }
                            completedTaskCandidate = activeTask
                            onStopFocus()
                            showCompletionModal = true
                        },
                        variant = KintsuButtonVariant.TEXT,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Preset selection (Short 15m, Standard 25m, Deep 50m, Custom)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    Text(
                        text = "Duration Presets",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    val isPresetSelected: (Int) -> Boolean = { min ->
                        if (min > 0) selectedFocusDuration == min else (selectedFocusDuration !in listOf(15, 25, 50))
                    }
                    val isCustomSelected = selectedFocusDuration !in listOf(15, 25, 50)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        listOf(15 to "Short (15m)", 25 to "Standard (25m)", 50 to "Deep (50m)", 0 to "Custom").forEach { (min, label) ->
                            KintsuFilterChip(
                                label = label,
                                selected = isPresetSelected(min),
                                onClick = {
                                    if (min > 0) {
                                        selectedFocusDuration = min
                                    } else {
                                        selectedFocusDuration = customDuration.toInt()
                                    }
                                }
                            )
                        }
                    }

                    if (isCustomSelected) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Custom: ${selectedFocusDuration} minutes",
                                    style = typography.caption.copy(fontWeight = FontWeight.Bold),
                                    color = colors.goldSeam
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(colors.surface)
                                            .border(1.dp, colors.border, CircleShape)
                                            .clickable {
                                                val next = (selectedFocusDuration - 5).coerceAtLeast(5)
                                                selectedFocusDuration = next
                                                customDuration = next.toFloat()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-5", fontSize = 11.sp, color = colors.textPrimary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(colors.surface)
                                            .border(1.dp, colors.goldSeam.copy(alpha = 0.5f), CircleShape)
                                            .clickable {
                                                val next = (selectedFocusDuration + 5).coerceAtMost(180)
                                                selectedFocusDuration = next
                                                customDuration = next.toFloat()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+5", fontSize = 11.sp, color = colors.goldSeam)
                                    }
                                }
                            }
                            Slider(
                                value = customDuration,
                                onValueChange = {
                                    customDuration = it
                                    selectedFocusDuration = it.toInt()
                                },
                                valueRange = 5f..180f,
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.goldSeam,
                                    activeTrackColor = colors.goldSeam,
                                    inactiveTrackColor = colors.border
                                )
                            )
                        }
                    }
                }

                // Zen Studio and Desk Clock Entry Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF241F18), Color(0xFF171512))
                            )
                        )
                        .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(16.dp))
                        .clickable {
                            val intent = android.content.Intent(context, com.kintsu.app.ui.screens.ZenStudioActivity::class.java)
                            context.startActivity(intent)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD4AF37))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ZEN STUDIO & DESK CLOCK",
                                    color = Color(0xFFD4AF37),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Split-Flap Mechanical Clock and 5 Soundscapes",
                                color = Color(0xFFF5F2EB),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Bamboo Rain, Singing Bowl, River Koto, Fireside, Bell",
                                color = Color(0xFF8E8B85),
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2E271D))
                                .border(1.dp, Color(0x60D4AF37), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Enter",
                                color = Color(0xFFD4AF37),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Ambient Focus Soundscape (Synthesizer)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = "Synthesized Soundscape",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        listOf(
                            SoundscapeMode.OFF to "Silent",
                            SoundscapeMode.BROWN_NOISE to "Deep Rain",
                            SoundscapeMode.BINAURAL_40HZ to "40Hz Gamma"
                        ).forEach { (mode, label) ->
                            KintsuFilterChip(
                                label = label,
                                selected = selectedSoundscape == mode,
                                onClick = {
                                    selectedSoundscape = mode
                                    FocusSoundEngine.setMode(mode)
                                }
                            )
                        }
                    }
                }

                // Break mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KintsuFilterChip(
                        label = "Focus Session",
                        selected = !isBreakMode,
                        onClick = { isBreakMode = false }
                    )
                    KintsuFilterChip(
                        label = "Short Break ($breakLengthMinutes min)",
                        selected = isBreakMode,
                        onClick = {
                            isBreakMode = true
                            selectedBreakDuration = breakLengthMinutes
                        }
                    )
                }

                // Attached Task indicator or Task selector
                if (!isBreakMode) {
                    if (linkedTask != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KintsuTheme.shapes.small)
                                .background(colors.surface)
                                .border(1.dp, colors.accent.copy(alpha = 0.4f), KintsuTheme.shapes.small)
                                .padding(horizontal = spacing.m, vertical = spacing.s),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.s),
                                modifier = Modifier.weight(1f)
                            ) {
                                IconFocus(color = colors.accent, size = 16.dp)
                                Column {
                                    Text(
                                        text = "Attached Task",
                                        style = typography.caption,
                                        color = colors.accent
                                    )
                                    Text(
                                        text = linkedTask!!.title,
                                        style = typography.itemTitle,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                            KintsuIconButton(
                                icon = { IconClose(color = it, size = 16.dp) },
                                contentDescription = "Unlink task",
                                onClick = { linkedTask = null }
                            )
                        }
                    } else if (tasks.any { !it.isCompleted }) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            Text(
                                text = "Attach to a Task (Optional)",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                            ) {
                                tasks.filter { !it.isCompleted }.take(5).forEach { t ->
                                    KintsuFilterChip(
                                        label = t.title,
                                        selected = false,
                                        onClick = { linkedTask = t },
                                        modifier = Modifier.widthIn(max = 240.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Linked Ritual selector (if focus mode)
                if (!isBreakMode && habits.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        Text(
                            text = "Attach to a Ritual (Completes on finish)",
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
                                selected = selectedHabitId == null,
                                onClick = { selectedHabitId = null }
                            )
                            habits.take(5).forEach { habit ->
                                KintsuFilterChip(
                                    label = habit.name,
                                    selected = selectedHabitId == habit.id,
                                    onClick = { selectedHabitId = habit.id },
                                    modifier = Modifier.widthIn(max = 200.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.s))

                // Primary Start Button
                KintsuButton(
                    text = if (isBreakMode) "Begin Rest Interval ($selectedBreakDuration min)" else "Begin Mindful Focus ($selectedFocusDuration min)",
                    onClick = {
                        onStartFocus(
                            activeDuration,
                            if (isBreakMode) null else selectedHabitId,
                            if (isBreakMode) null else linkedTask?.id,
                            if (isBreakMode) null else linkedTask?.title,
                            isBreakMode
                        )
                    },
                    variant = KintsuButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Focus History View (B4)
            if (recentSessions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(spacing.s))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = "Recent Focus History",
                        style = typography.sectionHeader,
                        color = colors.textSecondary
                    )
                    recentSessions.take(5).forEach { session ->
                        val sessionTask = tasks.find { it.id == session.linkedTaskId }
                        val sessionHabit = habits.find { it.id == session.linkedHabitId }
                        val sessionTitle = when {
                            sessionTask != null -> sessionTask.title
                            sessionHabit != null -> sessionHabit.name
                            else -> "Mindful Stillness"
                        }
                        val statusText = if (session.isCompleted) "Completed" else "Stopped early"
                        val statusColor = if (session.isCompleted) colors.accent else colors.textTertiary

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KintsuTheme.shapes.small)
                                .background(colors.surface.copy(alpha = 0.5f))
                                .border(0.5.dp, colors.border.copy(alpha = 0.4f), KintsuTheme.shapes.small)
                                .padding(horizontal = spacing.m, vertical = spacing.s),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sessionTitle,
                                    style = typography.caption,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${session.durationMinutes} min • $statusText",
                                    style = typography.caption,
                                    color = statusColor
                                )
                            }
                            if (session.isCompleted) {
                                IconCheck(color = colors.accent, size = 14.dp)
                            }
                        }
                    }
                }
            }
        }

    if (showCompletionModal) {
        val candidate = completedTaskCandidate
        AlertDialog(
            onDismissRequest = { showCompletionModal = false },
            title = {
                Text(
                    text = "Conscious Stillness Completed",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                    Text(
                        text = "Your mindful focus interval has been preserved. Take a moment to capture your thoughts or update your intentions.",
                        style = typography.body,
                        color = colors.textSecondary
                    )
                    if (candidate != null && !candidate.isCompleted) {
                        Text(
                            text = "Linked Task: \"${candidate.title}\"",
                            style = typography.caption,
                            color = colors.accent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    if (candidate != null && !candidate.isCompleted) {
                        TextButton(
                            onClick = {
                                onCompleteTask(candidate)
                                showCompletionModal = false
                            }
                        ) {
                            Text("Mark Task Done", color = colors.accent)
                        }
                    }
                    TextButton(
                        onClick = {
                            showCompletionModal = false
                            val title = candidate?.title ?: "Mindful Stillness"
                            onReflectInJournal("Reflection on Focus: $title")
                        }
                    ) {
                        Text("Reflect in Journal", color = colors.accent)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletionModal = false }) {
                    Text("Close", color = colors.textTertiary)
                }
            }
        )
    }
}
}
