package com.kintsu.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.DailyPlanItem
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.SubtaskEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.TaskPriority
import com.kintsu.app.data.model.TaskRecurrenceType
import com.kintsu.app.ui.components.IconAdd
import com.kintsu.app.ui.components.IconCalendar
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.IconFocus
import com.kintsu.app.ui.components.IconGoal
import com.kintsu.app.ui.components.IconHabit
import com.kintsu.app.ui.components.IconPlay
import com.kintsu.app.ui.components.IconRepeat
import com.kintsu.app.ui.components.IconTask
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
import java.time.LocalDate

enum class TaskViewMode(val title: String) {
    TODAY("Today"),
    ALL("Upcoming"),
    DONE("Completed")
}

enum class TaskSortOption(val displayName: String) {
    DUE_DATE("Due Date"),
    PRIORITY("Priority"),
    TITLE("Title")
}

enum class TaskFilterOption(val displayName: String) {
    ALL("All"),
    HIGH_PRIORITY("High Priority"),
    RECURRING("Recurring"),
    HAS_SUBTASKS("Subtasks")
}

val TIME_BLOCKS = listOf("Morning", "Afternoon", "Evening", "Anytime")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    goals: List<GoalEntity>,
    habits: List<HabitEntity> = emptyList(),
    todayLogs: List<HabitLogEntity> = emptyList(),
    subtasks: List<SubtaskEntity> = emptyList(),
    onToggleHabit: (Long) -> Unit = {},
    onToggleTask: (TaskEntity) -> Unit,
    onAddTask: (
        title: String,
        dueDateEpochDay: Long?,
        priority: TaskPriority,
        linkedGoalId: Long?,
        recurrenceType: TaskRecurrenceType,
        recurrenceWeekdaysMask: Int,
        recurrenceActive: Boolean,
        plannedDateEpochDay: Long?,
        plannedTimeBlock: String?,
        category: String?,
        notes: String?,
        estimatedMinutes: Int?,
        initialSubtasks: List<String>
    ) -> Unit,
    onUpdateTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onAddSubtask: (taskId: Long, title: String) -> Unit = { _, _ -> },
    onToggleSubtask: (SubtaskEntity) -> Unit = {},
    onUpdateSubtask: (SubtaskEntity) -> Unit = {},
    onDeleteSubtask: (SubtaskEntity) -> Unit = {},
    onReorderSubtasks: (List<SubtaskEntity>) -> Unit = {},
    onPlanTaskForToday: (taskId: Long, timeBlock: String?) -> Unit = { _, _ -> },
    onRemoveTaskFromPlan: (taskId: Long) -> Unit = {},
    onRescheduleTask: (taskId: Long, newDueDate: Long?) -> Unit = { _, _ -> },
    onSaveDailyPlan: (List<DailyPlanItem>) -> Unit = {},
    onStartFocusForTask: (TaskEntity) -> Unit = {},
    onOpenSettings: () -> Unit,
    onBackClick: () -> Unit = {},
    externalOpenAddSheet: Boolean = false,
    onExternalAddSheetConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var viewMode by remember { mutableStateOf(TaskViewMode.TODAY) }
    var sortOption by remember { mutableStateOf(TaskSortOption.DUE_DATE) }
    var filterOption by remember { mutableStateOf(TaskFilterOption.ALL) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showPlanMyDaySheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var prefilledTimeBlock by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val todayEpochDay = remember { LocalDate.now().toEpochDay() }

    val availableCategories = remember(tasks) {
        tasks.mapNotNull { it.category?.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filterTask: (TaskEntity) -> Boolean = { task ->
        val matchesSearch = searchQuery.isBlank() ||
            task.title.contains(searchQuery, ignoreCase = true) ||
            (task.category?.contains(searchQuery, ignoreCase = true) == true) ||
            (task.notes?.contains(searchQuery, ignoreCase = true) == true)
        val matchesCategory = selectedCategoryFilter == null ||
            task.category.equals(selectedCategoryFilter, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    LaunchedEffect(externalOpenAddSheet) {
        if (externalOpenAddSheet) {
            showAddSheet = true
            onExternalAddSheetConsumed()
        }
    }

    // Categorized Tasks
    val overdueTasks = remember(tasks, todayEpochDay, searchQuery, selectedCategoryFilter) {
        tasks.filter { !it.isCompleted && it.dueDateEpochDay != null && it.dueDateEpochDay < todayEpochDay && filterTask(it) }
    }

    val plannedForTodayTasks = remember(tasks, todayEpochDay, searchQuery, selectedCategoryFilter) {
        tasks.filter { !it.isCompleted && (it.plannedDateEpochDay == todayEpochDay || (it.dueDateEpochDay == todayEpochDay && it.plannedDateEpochDay == null)) && filterTask(it) }
    }

    val unscheduledPendingTasks = remember(tasks, todayEpochDay, searchQuery, selectedCategoryFilter) {
        tasks.filter {
            !it.isCompleted &&
            (it.dueDateEpochDay == null || it.dueDateEpochDay > todayEpochDay) &&
            it.plannedDateEpochDay != todayEpochDay &&
            filterTask(it)
        }
    }

    // Filtered and Sorted Tasks for ALL and DONE views
    val displayedTasks = remember(tasks, viewMode, sortOption, filterOption, todayEpochDay, subtasks) {
        var list = when (viewMode) {
            TaskViewMode.TODAY -> tasks.filter { !it.isCompleted && (it.plannedDateEpochDay == todayEpochDay || it.dueDateEpochDay == todayEpochDay) }
            TaskViewMode.ALL -> tasks.filter { !it.isCompleted }
            TaskViewMode.DONE -> tasks.filter { it.isCompleted }
        }

        // Apply filters
        list = when (filterOption) {
            TaskFilterOption.ALL -> list
            TaskFilterOption.HIGH_PRIORITY -> list.filter { it.priority == TaskPriority.HIGH }
            TaskFilterOption.RECURRING -> list.filter { it.isRecurring }
            TaskFilterOption.HAS_SUBTASKS -> list.filter { t -> subtasks.any { it.taskId == t.id } }
        }.filter(filterTask)

        // Apply sorting
        when (sortOption) {
            TaskSortOption.DUE_DATE -> list.sortedWith(
                compareBy<TaskEntity> { it.dueDateEpochDay ?: Long.MAX_VALUE }
                    .thenByDescending { it.priority.ordinal }
            )
            TaskSortOption.PRIORITY -> list.sortedWith(
                compareByDescending<TaskEntity> { it.priority.ordinal }
                    .thenBy { it.dueDateEpochDay ?: Long.MAX_VALUE }
            )
            TaskSortOption.TITLE -> list.sortedBy { it.title.lowercase() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        KintsuTopBar(
            title = "Tasks & Plan",
            subtitle = "Plan • Do • Complete",
                                )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.s)
        ) {
            // Row 1: Apple-style Minimal Segmented Control
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TaskViewMode.values().forEach { mode ->
                        val isSelected = viewMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) colors.accentSoft.copy(alpha = 0.85f) else Color.Transparent)
                                .clickable { viewMode = mode }
                                .padding(vertical = spacing.xs + 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.title,
                                style = typography.caption,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) colors.textPrimary else colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Row 2: Action Area (Clean, balanced, unified buttons)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KintsuButton(
                        text = "New Task",
                        onClick = {
                            prefilledTimeBlock = null
                            showAddSheet = true
                        },
                        variant = KintsuButtonVariant.ACCENT_SOFT,
                        leadingIcon = { IconAdd(color = it, size = 16.dp) },
                        modifier = Modifier.weight(1f)
                    )

                    if (viewMode == TaskViewMode.TODAY) {
                        KintsuButton(
                            text = "Plan Day",
                            onClick = { showPlanMyDaySheet = true },
                            variant = KintsuButtonVariant.SECONDARY,
                            leadingIcon = { IconCalendar(color = it, size = 16.dp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            // Search Bar & Subject Filter
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search tasks, subjects or notes...",
                                style = typography.caption,
                                color = colors.textTertiary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border.copy(alpha = 0.5f)
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                KintsuIconButton(
                                    icon = { IconClose(color = it, size = 14.dp) },
                                    contentDescription = "Clear search",
                                    onClick = { searchQuery = "" }
                                )
                            }
                        }
                    )

                    if (availableCategories.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KintsuFilterChip(
                                label = "All",
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null }
                            )
                            availableCategories.forEach { cat ->
                                KintsuFilterChip(
                                    label = cat,
                                    selected = selectedCategoryFilter == cat,
                                    onClick = {
                                        selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                    }
                                )
                            }
                        }
                    }
                }
            }


            // Row 3: Filter & Sort Controls (Displayed cleanly on All Tasks & Completed views)
            if (viewMode != TaskViewMode.TODAY) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filter:",
                                style = typography.caption,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(end = spacing.xxs)
                            )
                            TaskFilterOption.values().forEach { filter ->
                                KintsuFilterChip(
                                    label = filter.displayName,
                                    selected = filterOption == filter,
                                    onClick = { filterOption = filter }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sort:",
                                style = typography.caption,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(end = spacing.xxs)
                            )
                            TaskSortOption.values().forEach { sort ->
                                KintsuFilterChip(
                                    label = sort.displayName,
                                    selected = sortOption == sort,
                                    onClick = { sortOption = sort }
                                )
                            }
                        }
                    }
                }
            }

            // TODAY VIEW: Daily Planner Layout
            if (viewMode == TaskViewMode.TODAY) {
                // 1. Overdue Tasks Section (if any)
                if (overdueTasks.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KintsuTheme.shapes.medium)
                                .background(colors.accentSoft.copy(alpha = 0.35f))
                                .border(1.dp, colors.accent.copy(alpha = 0.4f), KintsuTheme.shapes.medium)
                                .padding(spacing.m),
                            verticalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Overdue (${overdueTasks.size})",
                                    style = typography.itemTitle,
                                    color = colors.accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(
                                    onClick = {
                                        overdueTasks.forEach { onRescheduleTask(it.id, todayEpochDay) }
                                    }
                                ) {
                                    Text(
                                        text = "Reschedule All to Today",
                                        style = typography.caption,
                                        color = colors.accent
                                    )
                                }
                            }

                            overdueTasks.forEach { task ->
                                val daysLate = (todayEpochDay - (task.dueDateEpochDay ?: todayEpochDay)).coerceAtLeast(1)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(KintsuTheme.shapes.small)
                                        .background(colors.surface)
                                        .padding(spacing.s),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = typography.body,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "$daysLate day${if (daysLate > 1) "s" else ""} late",
                                            style = typography.caption,
                                            color = colors.accent
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                        KintsuButton(
                                            text = "Today",
                                            onClick = { onRescheduleTask(task.id, todayEpochDay) },
                                            variant = KintsuButtonVariant.SECONDARY
                                        )
                                        KintsuIconButton(
                                            icon = { IconCheck(color = it, size = 16.dp) },
                                            contentDescription = "Complete",
                                            onClick = { onToggleTask(task) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Timeline Time Blocks (Morning, Afternoon, Evening, Anytime)
                TIME_BLOCKS.forEach { block ->
                    val blockTasks = plannedForTodayTasks.filter {
                        if (block == "Anytime") (it.plannedTimeBlock == null || it.plannedTimeBlock == "Anytime")
                        else it.plannedTimeBlock == block
                    }.sortedBy { it.planOrder }

                    // Associated habits for this block
                    val blockHabits = habits.filter { habit ->
                        val reminder = habit.reminderTime ?: ""
                        when (block) {
                            "Morning" -> reminder.isNotEmpty() && reminder < "12:00"
                            "Afternoon" -> reminder.isNotEmpty() && reminder in "12:00".."17:00"
                            "Evening" -> reminder.isNotEmpty() && reminder > "17:00"
                            else -> reminder.isEmpty()
                        }
                    }

                    if (blockTasks.isEmpty() && blockHabits.isEmpty()) {
                        // Compact single-line slot for empty period without habits (solves A3)
                        item(key = "empty_block_$block") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(KintsuTheme.shapes.small)
                                    .background(colors.surface.copy(alpha = 0.4f))
                                    .clickable {
                                        prefilledTimeBlock = block
                                        showAddSheet = true
                                    }
                                    .padding(horizontal = spacing.m, vertical = spacing.s),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                                ) {
                                    Text(
                                        text = block,
                                        style = typography.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = "•  No tasks planned",
                                        style = typography.caption,
                                        color = colors.textTertiary
                                    )
                                }
                                Text(
                                    text = "+ Add",
                                    style = typography.caption,
                                    color = colors.accent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        item(key = "header_block_$block") {
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
                                        text = block,
                                        style = typography.sectionHeader,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = "${blockTasks.size} task${if (blockTasks.size != 1) "s" else ""}",
                                        style = typography.caption,
                                        color = colors.textTertiary
                                    )
                                }

                                // Show scheduled habits with clean subheading (solves A5)
                                if (blockHabits.isNotEmpty()) {
                                    Text(
                                        text = if (block == "Anytime") "Today's Rituals" else "$block Rituals",
                                        style = typography.caption,
                                        color = colors.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = spacing.xxs)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                                    ) {
                                        blockHabits.forEach { habit ->
                                            val isDone = todayLogs.any { it.habitId == habit.id && (it.isCompleted || it.isRepairedGold) }
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isDone) colors.surfaceElevated.copy(alpha = 0.5f) else colors.surface)
                                                    .border(
                                                        0.8.dp,
                                                        if (isDone) colors.accent.copy(alpha = 0.5f) else colors.border,
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { onToggleHabit(habit.id) }
                                                    .padding(horizontal = spacing.s, vertical = spacing.xs),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
                                            ) {
                                                if (isDone) {
                                                    IconCheck(color = colors.accent, size = 12.dp)
                                                } else {
                                                    IconHabit(color = colors.textSecondary, size = 12.dp)
                                                }
                                                Text(
                                                    text = habit.name,
                                                    style = typography.caption,
                                                    color = if (isDone) colors.textTertiary else colors.textPrimary,
                                                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (blockTasks.isEmpty()) {
                            item(key = "empty_tasks_block_$block") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(KintsuTheme.shapes.small)
                                        .background(colors.surface.copy(alpha = 0.35f))
                                        .clickable {
                                            prefilledTimeBlock = block
                                            showAddSheet = true
                                        }
                                        .padding(horizontal = spacing.m, vertical = spacing.xs),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "No tasks planned for $block",
                                        style = typography.caption,
                                        color = colors.textTertiary
                                    )
                                    Text(
                                        text = "+ Add",
                                        style = typography.caption,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            items(blockTasks, key = { "block_task_${it.id}" }) { task ->
                                val taskSubtasks = subtasks.filter { it.taskId == task.id }
                                val linkedGoal = goals.find { it.id == task.linkedGoalId }
                                TaskRowItem(
                                    task = task,
                                    linkedGoal = linkedGoal,
                                    subtasks = taskSubtasks,
                                    todayEpochDay = todayEpochDay,
                                    onToggle = { onToggleTask(task) },
                                    onEdit = { taskToEdit = task },
                                    onDelete = { onDeleteTask(task) },
                                    onAddSubtask = { onAddSubtask(task.id, it) },
                                    onToggleSubtask = onToggleSubtask,
                                    onUpdateSubtask = onUpdateSubtask,
                                    onDeleteSubtask = onDeleteSubtask,
                                    onStartFocus = { onStartFocusForTask(task) },
                                    onChangeBlock = { newBlock -> onPlanTaskForToday(task.id, newBlock) }
                                )
                            }
                        }
                    }
                }

                // 3. Unscheduled / Backlog Tasks Section
                if (unscheduledPendingTasks.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = spacing.m),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            Text(
                                text = "Later (${unscheduledPendingTasks.size})",
                                style = typography.sectionHeader,
                                color = colors.textSecondary
                            )
                        }
                    }

                    items(unscheduledPendingTasks, key = { "unscheduled_task_${it.id}" }) { task ->
                        val taskSubtasks = subtasks.filter { it.taskId == task.id }
                        val linkedGoal = goals.find { it.id == task.linkedGoalId }
                        TaskRowItem(
                            task = task,
                            linkedGoal = linkedGoal,
                            subtasks = taskSubtasks,
                            todayEpochDay = todayEpochDay,
                            onToggle = { onToggleTask(task) },
                            onEdit = { taskToEdit = task },
                            onDelete = { onDeleteTask(task) },
                            onAddSubtask = { onAddSubtask(task.id, it) },
                            onToggleSubtask = onToggleSubtask,
                            onUpdateSubtask = onUpdateSubtask,
                            onDeleteSubtask = onDeleteSubtask,
                            onStartFocus = { onStartFocusForTask(task) },
                            onQuickPlanForToday = { onPlanTaskForToday(task.id, "Anytime") }
                        )
                    }
                }
            } else {
                // ALL TASKS / DONE VIEW
                if (displayedTasks.isEmpty()) {
                    item {
                        val emptyDesc = when (viewMode) {
                            TaskViewMode.TODAY -> "All quiet for today. No pending tasks."
                            TaskViewMode.ALL -> "No tasks found matching your filter."
                            TaskViewMode.DONE -> "Tasks you complete will appear quietly here."
                        }
                        KintsuEmptyState(
                            title = "Clear Space",
                            description = emptyDesc,
                            icon = { IconTask(color = it, size = 32.dp) },
                            actionText = if (viewMode != TaskViewMode.DONE) "Add Task" else null,
                            onActionClick = { showAddSheet = true }
                        )
                    }
                } else {
                    items(displayedTasks, key = { "displayed_task_${it.id}" }) { task ->
                        val taskSubtasks = subtasks.filter { it.taskId == task.id }
                        val linkedGoal = goals.find { it.id == task.linkedGoalId }
                        TaskRowItem(
                            task = task,
                            linkedGoal = linkedGoal,
                            subtasks = taskSubtasks,
                            todayEpochDay = todayEpochDay,
                            onToggle = { onToggleTask(task) },
                            onEdit = { taskToEdit = task },
                            onDelete = { onDeleteTask(task) },
                            onAddSubtask = { onAddSubtask(task.id, it) },
                            onToggleSubtask = onToggleSubtask,
                            onUpdateSubtask = onUpdateSubtask,
                            onDeleteSubtask = onDeleteSubtask,
                            onStartFocus = { onStartFocusForTask(task) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacing.xxxl))
            }
        }
    }

    if (showAddSheet) {
        TaskEditSheet(
            task = null,
            goals = goals,
            todayEpochDay = todayEpochDay,
            initialTimeBlock = prefilledTimeBlock,
            initialPlannedDate = if (viewMode == TaskViewMode.TODAY) todayEpochDay else null,
            onDismiss = {
                showAddSheet = false
                prefilledTimeBlock = null
            },
            onSave = { title, dueDate, priority, goalId, recType, mask, recActive, plannedDate, timeBlock, cat, nts, est, subtasksList ->
                onAddTask(title, dueDate, priority, goalId, recType, mask, recActive, plannedDate, timeBlock, cat, nts, est, subtasksList)
                showAddSheet = false
                prefilledTimeBlock = null
            }
        )
    }

    taskToEdit?.let { task ->
        TaskEditSheet(
            task = task,
            goals = goals,
            todayEpochDay = todayEpochDay,
            onDismiss = { taskToEdit = null },
            onSave = { title, dueDate, priority, goalId, recType, mask, recActive, plannedDate, timeBlock, cat, nts, est, _ ->
                onUpdateTask(
                    task.copy(
                        title = title,
                        dueDateEpochDay = dueDate,
                        priority = priority,
                        linkedGoalId = goalId,
                        recurrenceType = recType,
                        recurrenceWeekdaysMask = mask,
                        recurrenceActive = recActive,
                        plannedDateEpochDay = plannedDate,
                        plannedTimeBlock = timeBlock,
                        category = cat,
                        notes = nts,
                        estimatedMinutes = est
                    )
                )
                taskToEdit = null
            }
        )
    }

    if (showPlanMyDaySheet) {
        PlanMyDaySheet(
            tasks = tasks.filter { !it.isCompleted },
            todayEpochDay = todayEpochDay,
            onDismiss = { showPlanMyDaySheet = false },
            onSavePlan = { planItems ->
                onSaveDailyPlan(planItems)
                showPlanMyDaySheet = false
            }
        )
    }
}

@Composable
private fun TaskRowItem(
    task: TaskEntity,
    linkedGoal: GoalEntity?,
    subtasks: List<SubtaskEntity> = emptyList(),
    todayEpochDay: Long,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSubtask: (String) -> Unit = {},
    onToggleSubtask: (SubtaskEntity) -> Unit = {},
    onUpdateSubtask: (SubtaskEntity) -> Unit = {},
    onDeleteSubtask: (SubtaskEntity) -> Unit = {},
    onStartFocus: () -> Unit = {},
    onQuickPlanForToday: (() -> Unit)? = null,
    onChangeBlock: ((String) -> Unit)? = null
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var showActions by remember { mutableStateOf(false) }
    var showSubtasksList by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }
    var isAddingSubtask by remember { mutableStateOf(false) }

    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> colors.accent
        TaskPriority.MEDIUM -> colors.textSecondary
        TaskPriority.LOW -> colors.textTertiary
    }

    val dueText = when {
        task.dueDateEpochDay == null -> null
        task.dueDateEpochDay == todayEpochDay -> "Today"
        task.dueDateEpochDay == todayEpochDay + 1 -> "Tomorrow"
        task.dueDateEpochDay < todayEpochDay -> "Past due"
        else -> {
            val date = LocalDate.ofEpochDay(task.dueDateEpochDay)
            "${date.dayOfMonth} ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}"
        }
    }

    val completedSubtasksCount = subtasks.count { it.isCompleted }

    KintsuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (task.isCompleted) colors.surfaceElevated.copy(alpha = 0.5f) else colors.surface
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
                // Checkbox target (48dp accessible touch target)
                val checkBg by animateColorAsState(
                    targetValue = if (task.isCompleted) colors.accent else Color.Transparent,
                    label = "taskCheckBg"
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggle
                        )
                        .semantics {
                            role = Role.Checkbox
                            contentDescription = if (task.isCompleted) "Mark task ${task.title} incomplete" else "Complete task ${task.title}"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                color = if (task.isCompleted) checkBg else colors.border,
                                shape = CircleShape
                            )
                            .background(checkBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            IconCheck(color = colors.background, size = 14.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(spacing.xs))

                // Task details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = typography.itemTitle,
                        color = if (task.isCompleted) colors.textTertiary else colors.textPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        modifier = Modifier.padding(top = spacing.xxs)
                    ) {
                        // Priority Badge
                        Text(
                            text = task.priority.displayName,
                            style = typography.caption,
                            color = priorityColor
                        )

                        if (task.plannedDateEpochDay == todayEpochDay && task.dueDateEpochDay != null && task.dueDateEpochDay != todayEpochDay) {
                            Text(
                                text = "• Planned for Today (Due $dueText)",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        } else if (dueText != null) {
                            Text(
                                text = "• $dueText",
                                style = typography.caption,
                                color = if (task.dueDateEpochDay != null && task.dueDateEpochDay < todayEpochDay) colors.accent else colors.textTertiary
                            )
                        }

                        // Recurrence Indicator
                        if (task.isRecurring) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "•", style = typography.caption, color = colors.textTertiary)
                                Spacer(modifier = Modifier.width(2.dp))
                                IconRepeat(
                                    color = if (task.recurrenceActive) colors.accent else colors.textTertiary,
                                    size = 11.dp
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (task.recurrenceActive) task.recurrenceType.displayName else "Paused",
                                    style = typography.caption,
                                    color = if (task.recurrenceActive) colors.accent else colors.textTertiary
                                )
                            }
                        }

                        // Subtasks Indicator
                        if (subtasks.isNotEmpty()) {
                            Text(
                                text = "• $completedSubtasksCount/${subtasks.size} subtasks",
                                style = typography.caption,
                                color = if (completedSubtasksCount == subtasks.size) colors.accent else colors.textSecondary,
                                modifier = Modifier.clickable { showSubtasksList = !showSubtasksList }
                            )
                        }

                                                if (linkedGoal != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "·",
                                    style = typography.caption,
                                    color = colors.textTertiary
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                IconGoal(color = colors.accent, size = 12.dp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = linkedGoal.title.take(18) + if (linkedGoal.title.length > 18) " …" else "",
                                    style = typography.caption,
                                    color = colors.accent
                                )
                            }
                        }

                        // Category / Subject Badge
                        if (!task.category.isNullOrBlank()) {
                            Text(
                                text = "· ${task.category}",
                                style = typography.caption,
                                color = colors.accent,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Estimated Duration Badge
                        if (task.estimatedMinutes != null && task.estimatedMinutes > 0) {
                            Text(
                                text = "· ${task.estimatedMinutes}m",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Notes preview if present
                    if (!task.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.notes,
                            style = typography.caption.copy(fontSize = 11.sp),
                            color = colors.textTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } }

                // Quick Plan button for unscheduled items
                if (onQuickPlanForToday != null && !task.isCompleted) {
                    KintsuButton(
                        text = "Plan",
                        onClick = onQuickPlanForToday,
                        variant = KintsuButtonVariant.SECONDARY
                    )
                }
            }

            // Subtasks checklist accordion
            AnimatedVisibility(visible = showSubtasksList || (showActions && subtasks.isNotEmpty())) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp, top = spacing.s),
                    verticalArrangement = Arrangement.spacedBy(spacing.xxs)
                ) {
                    subtasks.forEach { sub ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleSubtask(sub) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(
                                            1.dp,
                                            if (sub.isCompleted) colors.accent else colors.border,
                                            CircleShape
                                        )
                                        .background(if (sub.isCompleted) colors.accent else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (sub.isCompleted) {
                                        IconCheck(color = colors.background, size = 10.dp)
                                    }
                                }
                                Text(
                                    text = sub.title,
                                    style = typography.body,
                                    color = if (sub.isCompleted) colors.textTertiary else colors.textPrimary,
                                    textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }
                            KintsuIconButton(
                                icon = { IconClose(color = it, size = 12.dp) },
                                contentDescription = "Delete subtask",
                                onClick = { onDeleteSubtask(sub) },
                                tint = colors.textTertiary
                            )
                        }
                    }

                    if (isAddingSubtask) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            OutlinedTextField(
                                value = newSubtaskText,
                                onValueChange = { newSubtaskText = it },
                                placeholder = { Text("Subtask title...", style = typography.caption) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = typography.caption
                            )
                            KintsuButton(
                                text = "Add",
                                onClick = {
                                    if (newSubtaskText.isNotBlank()) {
                                        onAddSubtask(newSubtaskText.trim())
                                        newSubtaskText = ""
                                        isAddingSubtask = false
                                    }
                                },
                                variant = KintsuButtonVariant.ACCENT_SOFT
                            )
                            KintsuIconButton(
                                icon = { IconClose(color = it, size = 14.dp) },
                                contentDescription = "Cancel",
                                onClick = { isAddingSubtask = false }
                            )
                        }
                    } else {
                        TextButton(
                            onClick = { isAddingSubtask = true },
                            modifier = Modifier.padding(top = spacing.xxs)
                        ) {
                            Text("+ Add subtask", style = typography.caption, color = colors.accent)
                        }
                    }
                }
            }

            // Quick actions drawer
            AnimatedVisibility(visible = showActions) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.m),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    // Block assignment buttons if planned
                    if (onChangeBlock != null && !task.isCompleted) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Block:", style = typography.caption, color = colors.textTertiary)
                            TIME_BLOCKS.forEach { blk ->
                                KintsuFilterChip(
                                    label = blk,
                                    selected = task.plannedTimeBlock == blk,
                                    onClick = { onChangeBlock(blk) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!task.isCompleted) {
                            KintsuButton(
                                text = "Focus",
                                onClick = onStartFocus,
                                variant = KintsuButtonVariant.TEXT,
                                leadingIcon = { IconFocus(color = it, size = 14.dp) }
                            )
                            Spacer(modifier = Modifier.width(spacing.xs))
                        }
                        KintsuButton(
                            text = "Edit",
                            onClick = onEdit,
                            variant = KintsuButtonVariant.TEXT
                        )
                        KintsuIconButton(
                            icon = { IconTrash(color = it, size = 16.dp) },
                            contentDescription = "Delete task",
                            onClick = onDelete,
                            tint = colors.textTertiary
                        )
                    }
                }
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditSheet(
    task: TaskEntity?,
    goals: List<GoalEntity>,
    todayEpochDay: Long,
    initialTimeBlock: String? = null,
    initialPlannedDate: Long? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        dueDate: Long?,
        priority: TaskPriority,
        linkedGoalId: Long?,
        recurrenceType: TaskRecurrenceType,
        recurrenceWeekdaysMask: Int,
        recurrenceActive: Boolean,
        plannedDate: Long?,
        timeBlock: String?,
        category: String?,
        notes: String?,
        estimatedMinutes: Int?,
        subtasksList: List<String>
    ) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var title by remember { mutableStateOf(task?.title ?: "") }
    var category by remember { mutableStateOf(task?.category ?: "") }
    var notes by remember { mutableStateOf(task?.notes ?: "") }
    var estimatedMinutesText by remember { mutableStateOf(task?.estimatedMinutes?.toString() ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var selectedDueOffset by remember {
        mutableStateOf(
            when (task?.dueDateEpochDay) {
                null -> if (initialPlannedDate == todayEpochDay) "Today" else "None"
                todayEpochDay -> "Today"
                todayEpochDay + 1 -> "Tomorrow"
                else -> "Later"
            }
        )
    }
    var selectedGoalId by remember { mutableStateOf(task?.linkedGoalId) }

    // Recurrence settings
    var recurrenceType by remember { mutableStateOf(task?.recurrenceType ?: TaskRecurrenceType.NEVER) }
    var recurrenceMask by remember { mutableStateOf(task?.recurrenceWeekdaysMask ?: 31) } // Mon-Fri default
    var recurrenceActive by remember { mutableStateOf(task?.recurrenceActive ?: true) }

    // Planning settings
    var plannedTimeBlock by remember { mutableStateOf(initialTimeBlock ?: task?.plannedTimeBlock ?: "Morning") }

    // Initial subtasks
    var newSubtaskInput by remember { mutableStateOf("") }
    val initialSubtasks = remember { mutableListOf<String>() }
    var subtasksState by remember { mutableStateOf(initialSubtasks.toList()) }

    var showDiscardConfirm by remember { mutableStateOf(false) }

    fun requestDismiss() {
        if (title.isNotBlank() && title != (task?.title ?: "")) {
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
        title = if (task == null) "New Task" else "Edit Task"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.m),
            modifier = Modifier.padding(bottom = spacing.l)
        ) {
            // Task title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = "What needs to be attended to?",
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

            // Difficulty & Priority Selector (3 Options with Gold Rewards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Difficulty & Reward",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Text(
                    text = when (priority) {
                        TaskPriority.LOW -> "Easy • +10 Gold"
                        TaskPriority.MEDIUM -> "Medium • +20 Gold"
                        TaskPriority.HIGH -> "Hard • +35 Gold"
                    },
                    style = typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = colors.goldSeam
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                listOf(
                    TaskPriority.LOW to "Easy (+10)",
                    TaskPriority.MEDIUM to "Medium (+20)",
                    TaskPriority.HIGH to "Hard (+35)"
                ).forEach { (p, label) ->
                    KintsuFilterChip(
                        label = label,
                        selected = priority == p,
                        onClick = { priority = p }
                    )
                }
            }

            // Due Date
            Text(
                text = "Due Date",
                style = typography.caption,
                color = colors.textSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                listOf("None", "Today", "Tomorrow", "Later").forEach { label ->
                    KintsuFilterChip(
                        label = label,
                        selected = selectedDueOffset == label,
                        onClick = { selectedDueOffset = label }
                    )
                }
            }

            // Recurrence Selector
            Text(
                text = "Repeat",
                style = typography.caption,
                color = colors.textSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                TaskRecurrenceType.values().forEach { rt ->
                    KintsuFilterChip(
                        label = rt.displayName,
                        selected = recurrenceType == rt,
                        onClick = { recurrenceType = rt }
                    )
                }
            }

            // If selected weekdays, show weekday selector
            if (recurrenceType == TaskRecurrenceType.WEEKDAYS) {
                val weekdayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    weekdayLabels.forEachIndexed { idx, label ->
                        val bit = 1 shl idx
                        val isSelected = (recurrenceMask and bit) != 0
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.accent else colors.surface)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, CircleShape)
                                .clickable {
                                    recurrenceMask = if (isSelected) recurrenceMask and bit.inv() else recurrenceMask or bit
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = typography.caption,
                                color = if (isSelected) colors.background else colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (recurrenceType != TaskRecurrenceType.NEVER) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (recurrenceActive) "Recurrence is Active" else "Recurrence is Paused",
                        style = typography.caption,
                        color = if (recurrenceActive) colors.accent else colors.textTertiary
                    )
                    KintsuButton(
                        text = if (recurrenceActive) "Pause" else "Resume",
                        onClick = { recurrenceActive = !recurrenceActive },
                        variant = KintsuButtonVariant.SECONDARY
                    )
                }
            }

            // Subtasks list (only when creating a new task)
            if (task == null) {
                Text(
                    text = "Subtasks",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    subtasksState.forEachIndexed { index, sub ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "• $sub", style = typography.body, color = colors.textPrimary)
                            KintsuIconButton(
                                icon = { IconClose(color = it, size = 12.dp) },
                                contentDescription = "Remove",
                                onClick = {
                                    val list = subtasksState.toMutableList()
                                    list.removeAt(index)
                                    subtasksState = list
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubtaskInput,
                            onValueChange = { newSubtaskInput = it },
                            placeholder = { Text("Add subtask...", style = typography.caption, color = colors.textTertiary) },
                            singleLine = true,
                            shape = KintsuTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.border
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        KintsuButton(
                            text = "+",
                            onClick = {
                                if (newSubtaskInput.isNotBlank()) {
                                    subtasksState = subtasksState + newSubtaskInput.trim()
                                    newSubtaskInput = ""
                                }
                            },
                            variant = KintsuButtonVariant.ACCENT_SOFT
                        )
                    }
                }
            }

            // Category / Academic Subject
            Text(
                text = "Subject / Category",
                style = typography.caption,
                color = colors.textSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                listOf("Academics", "Math", "Science", "Literature", "Work", "Personal").forEach { suggestion ->
                    KintsuFilterChip(
                        label = suggestion,
                        selected = category.equals(suggestion, ignoreCase = true),
                        onClick = { category = if (category.equals(suggestion, ignoreCase = true)) "" else suggestion }
                    )
                }
            }
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                placeholder = { Text("Or type custom subject / category...", style = typography.caption, color = colors.textTertiary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = KintsuTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border
                )
            )

            // Estimated Duration
            Text(
                text = "Estimated Focus Duration",
                style = typography.caption,
                color = colors.textSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                listOf(15, 25, 45, 60, 90).forEach { mins ->
                    val isSelected = estimatedMinutesText == mins.toString()
                    KintsuFilterChip(
                        label = "${mins}m",
                        selected = isSelected,
                        onClick = { estimatedMinutesText = if (isSelected) "" else mins.toString() }
                    )
                }
            }
            OutlinedTextField(
                value = estimatedMinutesText,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) estimatedMinutesText = it },
                placeholder = { Text("Duration in minutes (e.g. 25)...", style = typography.caption, color = colors.textTertiary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = KintsuTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border
                )
            )

            // Study Notes / Context
            Text(
                text = "Notes & Context",
                style = typography.caption,
                color = colors.textSecondary
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Add instructions, key references, study notes...", style = typography.caption, color = colors.textTertiary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = KintsuTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border
                )
            )

            // Link to Goal
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
                    goals.forEach { g ->
                        KintsuFilterChip(
                            label = g.title,
                            selected = selectedGoalId == g.id,
                            onClick = {
                                selectedGoalId = if (selectedGoalId == g.id) null else g.id
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.s))

            KintsuButton(
                text = if (task == null) "Create Task" else "Save Changes",
                onClick = {
                    if (title.isNotBlank()) {
                        val dueDay = when (selectedDueOffset) {
                            "Today" -> todayEpochDay
                            "Tomorrow" -> todayEpochDay + 1
                            "Later" -> todayEpochDay + 7
                            else -> null
                        }
                        val plannedDate = initialPlannedDate ?: (if (selectedDueOffset == "Today") todayEpochDay else task?.plannedDateEpochDay)
                        onSave(
                            title,
                            dueDay,
                            priority,
                            selectedGoalId,
                            recurrenceType,
                            recurrenceMask,
                            recurrenceActive,
                            plannedDate,
                            plannedTimeBlock,
                            category.trim().ifBlank { null },
                            notes.trim().ifBlank { null },
                            estimatedMinutesText.toIntOrNull(),
                            subtasksState
                        )
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
                    text = "Discard Unsaved Task?",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanMyDaySheet(
    tasks: List<TaskEntity>,
    todayEpochDay: Long,
    onDismiss: () -> Unit,
    onSavePlan: (List<DailyPlanItem>) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    // Map of taskId -> selected boolean
    val selectedTasks = remember {
        mutableStateMapOf<Long, Boolean>().apply {
            tasks.forEach { t ->
                this[t.id] = t.plannedDateEpochDay == todayEpochDay || t.dueDateEpochDay == todayEpochDay
            }
        }
    }

    // Map of taskId -> timeBlock
    val taskBlocks = remember {
        mutableStateMapOf<Long, String>().apply {
            tasks.forEach { t ->
                this[t.id] = t.plannedTimeBlock ?: "Morning"
            }
        }
    }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Plan My Day"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            Text(
                text = "Select what you intend to focus on today, and optionally assign each task to a calm time block.",
                style = typography.body,
                color = colors.textSecondary
            )

            if (tasks.isEmpty()) {
                Text(
                    text = "No open tasks to schedule. Create some tasks first!",
                    style = typography.caption,
                    color = colors.textTertiary
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        val isSelected = selectedTasks[task.id] == true
                        val block = taskBlocks[task.id] ?: "Morning"

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KintsuTheme.shapes.medium)
                                .background(if (isSelected) colors.surfaceElevated else colors.surface)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.accent.copy(alpha = 0.5f) else colors.border,
                                    KintsuTheme.shapes.medium
                                )
                                .clickable { selectedTasks[task.id] = !isSelected }
                                .padding(spacing.m),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .border(
                                                1.5.dp,
                                                if (isSelected) colors.accent else colors.border,
                                                CircleShape
                                            )
                                            .background(if (isSelected) colors.accent else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            IconCheck(color = colors.background, size = 12.dp)
                                        }
                                    }

                                    Text(
                                        text = task.title,
                                        style = typography.itemTitle,
                                        color = colors.textPrimary
                                    )
                                }

                                Text(
                                    text = task.priority.displayName,
                                    style = typography.caption,
                                    color = when (task.priority) {
                                        TaskPriority.HIGH -> colors.accent
                                        else -> colors.textTertiary
                                    }
                                )
                            }

                            if (isSelected) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = spacing.xxs),
                                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                                ) {
                                    TIME_BLOCKS.forEach { tb ->
                                        KintsuFilterChip(
                                            label = tb,
                                            selected = block == tb,
                                            onClick = { taskBlocks[task.id] = tb }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            KintsuButton(
                text = "Save Today's Plan",
                onClick = {
                    val plan = selectedTasks
                        .filter { it.value }
                        .map { (id, _) ->
                            DailyPlanItem(
                                taskId = id,
                                timeBlock = taskBlocks[id] ?: "Morning"
                            )
                        }
                    onSavePlan(plan)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
