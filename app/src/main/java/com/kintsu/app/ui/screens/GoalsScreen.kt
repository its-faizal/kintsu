package com.kintsu.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.GoalProgressType
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.ui.components.IconAdd
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconGoal
import com.kintsu.app.ui.components.IconHabit
import com.kintsu.app.ui.components.IconTask
import com.kintsu.app.ui.components.IconTrash
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuEmptyState
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.components.KintsuIconButton
import com.kintsu.app.ui.components.KintsuProgressRing
import com.kintsu.app.ui.components.KintsuTopBar
import com.kintsu.app.ui.theme.KintsuTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    tasks: List<TaskEntity>,
    habits: List<HabitEntity> = emptyList(),
    todayLogs: List<HabitLogEntity> = emptyList(),
    onAddGoal: (title: String, targetDate: Long, progressType: GoalProgressType, manualProgress: Int) -> Unit,
    onUpdateGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit,
    onOpenSettings: () -> Unit,
    externalOpenAddSheet: Boolean = false,
    onExternalAddSheetConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var showAddSheet by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<GoalEntity?>(null) }
    val todayEpochDay = remember { LocalDate.now().toEpochDay() }

    LaunchedEffect(externalOpenAddSheet) {
        if (externalOpenAddSheet) {
            showAddSheet = true
            onExternalAddSheetConsumed()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        KintsuTopBar(
            title = "Goals",
            onSettingsClick = onOpenSettings
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            item {
                Spacer(modifier = Modifier.height(spacing.xxs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Intentions on the Horizon",
                        style = typography.caption,
                        color = colors.textSecondary
                    )

                    KintsuButton(
                        text = "New Goal",
                        onClick = { showAddSheet = true },
                        variant = KintsuButtonVariant.ACCENT_SOFT,
                        leadingIcon = { IconAdd(color = it, size = 16.dp) }
                    )
                }
            }

            if (goals.isEmpty()) {
                item {
                    KintsuEmptyState(
                        title = "No Goals Yet",
                        description = "Set a meaningful direction. Tasks and rituals can link to it directly.",
                        icon = { IconGoal(color = it, size = 32.dp) },
                        actionText = "Create Horizon Goal",
                        onActionClick = { showAddSheet = true }
                    )
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    val linkedTasks = tasks.filter { it.linkedGoalId == goal.id }
                    val linkedHabits = habits.filter { it.linkedGoalId == goal.id }
                    GoalRowItem(
                        goal = goal,
                        linkedTasks = linkedTasks,
                        linkedHabits = linkedHabits,
                        todayLogs = todayLogs,
                        todayEpochDay = todayEpochDay,
                        onEdit = { goalToEdit = goal },
                        onDelete = { onDeleteGoal(goal) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacing.xxxl))
            }
        }
    }

    if (showAddSheet) {
        GoalEditSheet(
            goal = null,
            todayEpochDay = todayEpochDay,
            onDismiss = { showAddSheet = false },
            onSave = { title, targetDate, type, progress ->
                onAddGoal(title, targetDate, type, progress)
                showAddSheet = false
            }
        )
    }

    goalToEdit?.let { goal ->
        GoalEditSheet(
            goal = goal,
            todayEpochDay = todayEpochDay,
            onDismiss = { goalToEdit = null },
            onSave = { title, targetDate, type, progress ->
                onUpdateGoal(
                    goal.copy(
                        title = title,
                        targetDateEpochDay = targetDate,
                        progressType = type,
                        manualProgressPercent = progress
                    )
                )
                goalToEdit = null
            }
        )
    }
}

@Composable
private fun GoalRowItem(
    goal: GoalEntity,
    linkedTasks: List<TaskEntity>,
    linkedHabits: List<HabitEntity> = emptyList(),
    todayLogs: List<HabitLogEntity> = emptyList(),
    todayEpochDay: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var showActions by remember { mutableStateOf(false) }

    val totalLinkedItems = linkedTasks.size + linkedHabits.size
    val doneTasks = linkedTasks.count { it.isCompleted }
    val doneHabits = linkedHabits.count { h -> todayLogs.any { it.habitId == h.id && it.isCompleted } }
    val totalDoneItems = doneTasks + doneHabits

    val progressFloat: Float = if (goal.progressType == GoalProgressType.AUTO_FROM_ITEMS) {
        if (totalLinkedItems == 0) 0f else {
            totalDoneItems.toFloat() / totalLinkedItems.toFloat()
        }
    } else {
        goal.manualProgressPercent.toFloat() / 100f
    }

    val daysRemaining = goal.targetDateEpochDay - todayEpochDay
    val targetDate = LocalDate.ofEpochDay(goal.targetDateEpochDay)
    val targetDateFormatted = "${targetDate.dayOfMonth} ${targetDate.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${targetDate.year}"

    KintsuCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable { showActions = !showActions }
                .padding(spacing.m)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = typography.itemTitle,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(spacing.xxs))

                    Text(
                        text = "Target: $targetDateFormatted" + if (daysRemaining > 0) " (${daysRemaining}d left)" else " (Reached)",
                        style = typography.secondary,
                        color = colors.textTertiary
                    )

                    if (totalLinkedItems > 0) {
                        val summaryText = buildString {
                            append("$totalDoneItems of $totalLinkedItems linked items complete")
                            if (linkedHabits.isNotEmpty()) {
                                append(" • $doneHabits/${linkedHabits.size} rituals today")
                            }
                        }
                        Text(
                            text = summaryText,
                            style = typography.caption,
                            color = colors.accent
                        )
                    }
                }

                KintsuProgressRing(
                    progress = progressFloat,
                    size = 54.dp,
                    strokeWidth = 4.5.dp,
                    progressColor = colors.goldSeam
                )
            }

            AnimatedVisibility(visible = showActions) {
                Column(modifier = Modifier.padding(top = spacing.m)) {
                    // Linked tasks preview if any
                    if (linkedTasks.isNotEmpty()) {
                        Text(
                            text = "Linked Tasks",
                            style = typography.caption,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = spacing.xs)
                        )
                        linkedTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${task.title}",
                                    style = typography.secondary,
                                    color = if (task.isCompleted) colors.textTertiary else colors.textPrimary
                                )
                                if (task.isCompleted) {
                                    IconCheck(color = colors.accent, size = 12.dp)
                                }
                            }
                        }
                    }

                    // Linked rituals preview if any
                    if (linkedHabits.isNotEmpty()) {
                        if (linkedTasks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(spacing.xs))
                        }
                        Text(
                            text = "Linked Daily Rituals",
                            style = typography.caption,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = spacing.xs)
                        )
                        linkedHabits.forEach { habit ->
                            val isDone = todayLogs.any { it.habitId == habit.id && it.isCompleted }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconHabit(color = colors.accent, size = 12.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = habit.name,
                                        style = typography.secondary,
                                        color = if (isDone) colors.textTertiary else colors.textPrimary
                                    )
                                }
                                if (isDone) {
                                    IconCheck(color = colors.accent, size = 12.dp)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.s),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KintsuButton(
                            text = "Edit",
                            onClick = onEdit,
                            variant = KintsuButtonVariant.TEXT
                        )
                        KintsuIconButton(
                            icon = { IconTrash(color = it, size = 16.dp) },
                            contentDescription = "Delete goal",
                            onClick = onDelete,
                            tint = colors.textTertiary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditSheet(
    goal: GoalEntity?,
    todayEpochDay: Long,
    onDismiss: () -> Unit,
    onSave: (title: String, targetDate: Long, progressType: GoalProgressType, manualProgress: Int) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var title by remember { mutableStateOf(goal?.title ?: "") }
    val initialHorizonOffset = remember {
        if (goal == null) "3 Months" else {
            val diffDays = goal.targetDateEpochDay - todayEpochDay
            when {
                diffDays <= 45 -> "1 Month"
                diffDays <= 120 -> "3 Months"
                diffDays <= 240 -> "6 Months"
                else -> "1 Year"
            }
        }
    }
    var selectedHorizonOffset by remember { mutableStateOf(initialHorizonOffset) }
    var hasSelectedCustomHorizon by remember { mutableStateOf(false) }
    var progressType by remember { mutableStateOf(goal?.progressType ?: GoalProgressType.AUTO_FROM_ITEMS) }
    var manualProgress by remember { mutableFloatStateOf(goal?.manualProgressPercent?.toFloat() ?: 0f) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    fun requestDismiss() {
        val hasChanges = title.isNotBlank() && (
            title != (goal?.title ?: "") ||
            progressType != (goal?.progressType ?: GoalProgressType.AUTO_FROM_ITEMS) ||
            (progressType == GoalProgressType.MANUAL && manualProgress.toInt() != (goal?.manualProgressPercent ?: 0)) ||
            hasSelectedCustomHorizon
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
        title = if (goal == null) "New Goal" else "Edit Goal"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = "e.g. Master Kettlebell Snatch, Read 12 Books",
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

            // Horizon Timeline
            Text(
                text = "Target Timeline",
                style = typography.caption,
                color = colors.textSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                listOf("1 Month", "3 Months", "6 Months", "1 Year").forEach { label ->
                    KintsuFilterChip(
                        label = label,
                        selected = selectedHorizonOffset == label,
                        onClick = {
                            selectedHorizonOffset = label
                            hasSelectedCustomHorizon = true
                        }
                    )
                }
            }

            // Progress Mode
            Text(
                text = "Progress Calculation",
                style = typography.caption,
                color = colors.textSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                KintsuFilterChip(
                    label = "Auto (from linked items)",
                    selected = progressType == GoalProgressType.AUTO_FROM_ITEMS,
                    onClick = { progressType = GoalProgressType.AUTO_FROM_ITEMS }
                )
                KintsuFilterChip(
                    label = "Manual %",
                    selected = progressType == GoalProgressType.MANUAL,
                    onClick = { progressType = GoalProgressType.MANUAL }
                )
            }

            if (progressType == GoalProgressType.MANUAL) {
                Column {
                    Text(
                        text = "Progress: ${manualProgress.toInt()}%",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    Slider(
                        value = manualProgress,
                        onValueChange = { manualProgress = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                            inactiveTrackColor = colors.border
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.s))

            KintsuButton(
                text = if (goal == null) "Create Goal" else "Save Changes",
                onClick = {
                    if (title.isNotBlank()) {
                        val targetDays = if (goal != null && !hasSelectedCustomHorizon) {
                            goal.targetDateEpochDay
                        } else {
                            when (selectedHorizonOffset) {
                                "1 Month" -> todayEpochDay + 30
                                "3 Months" -> todayEpochDay + 90
                                "6 Months" -> todayEpochDay + 180
                                "1 Year" -> todayEpochDay + 365
                                else -> todayEpochDay + 90
                            }
                        }
                        onSave(title, targetDays, progressType, manualProgress.toInt())
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = {
                Text(
                    text = "Discard Unsaved Goal?",
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
