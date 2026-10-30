package com.kintsu.app.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.components.IconAdd
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.IconWidget
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.widget.AestheticQuoteWidgetReceiver
import com.kintsu.app.widget.CompactTasksWidgetReceiver
import com.kintsu.app.widget.DailyHabitsWidgetReceiver
import com.kintsu.app.widget.DailyProgressWidgetReceiver
import com.kintsu.app.widget.DailyQuoteWidgetReceiver
import com.kintsu.app.widget.FocusTimerWidgetReceiver
import com.kintsu.app.widget.GoalsProgressWidgetReceiver
import com.kintsu.app.widget.QuickAddWidgetReceiver
import com.kintsu.app.widget.StackedSummaryWidgetReceiver
import com.kintsu.app.widget.TodayOverviewWidgetReceiver
import com.kintsu.app.widget.TodayTasksWidgetReceiver

data class WidgetInfoItem(
    val id: String,
    val name: String,
    val sizeLabel: String,
    val category: String,
    val summary: String,
    val definition: String,
    val receiverClass: Class<*>,
    val previewType: WidgetPreviewType
)

enum class WidgetPreviewType {
    TODAY_OVERVIEW,
    FOCUS_TIMER,
    TODAY_TASKS,
    DAILY_HABITS,
    GOALS_PROGRESS,
    DAILY_QUOTE,
    DAILY_PROGRESS,
    QUICK_ADD,
    STACKED_SUMMARY,
    AESTHETIC_QUOTE,
    COMPACT_TASKS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetShowcaseSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    val allWidgets = remember {
        listOf(
            WidgetInfoItem(
                id = "today_overview",
                name = "Today Overview",
                sizeLabel = "4 × 2",
                category = "Dashboard",
                summary = "Your primary daily dashboard and command center.",
                definition = "Displays a personalized greeting, date, and 4 live progress indicators: Habits completed today, Tasks finished today, Goal milestones, and Focus minutes. Tapping any metric deep-links directly into its specific section in Kintsu.",
                receiverClass = TodayOverviewWidgetReceiver::class.java,
                previewType = WidgetPreviewType.TODAY_OVERVIEW
            ),
            WidgetInfoItem(
                id = "focus_timer",
                name = "Focus Timer",
                sizeLabel = "4 × 2",
                category = "Focus",
                summary = "Mindful countdown timer with interactive controls.",
                definition = "Directly synchronized with Kintsu's FocusForegroundService. Displays your active focus duration, current intention/task, and provides immediate Start, Pause, Resume, and Reset actions. Select presets (15m, 25m, 45m) directly from your home screen.",
                receiverClass = FocusTimerWidgetReceiver::class.java,
                previewType = WidgetPreviewType.FOCUS_TIMER
            ),
            WidgetInfoItem(
                id = "today_tasks",
                name = "Today's Tasks",
                sizeLabel = "4 × 2",
                category = "Tasks",
                summary = "Live task list with direct one-tap completion.",
                definition = "Prioritizes tasks planned for today, due today, or urgent. Interactive circular checkboxes allow completing and uncompleting tasks instantly. Tapping any task opens its details, and '+ Task' opens the creation sheet.",
                receiverClass = TodayTasksWidgetReceiver::class.java,
                previewType = WidgetPreviewType.TODAY_TASKS
            ),
            WidgetInfoItem(
                id = "daily_habits",
                name = "Daily Habits",
                sizeLabel = "4 × 1",
                category = "Habits",
                summary = "Compact horizontal habit tracker strip.",
                definition = "Displays your daily completion count and circular habit indicators. Tap any habit circle to mark it done for today with gold seam reinforcement, or tap the title area to jump straight to Habits.",
                receiverClass = DailyHabitsWidgetReceiver::class.java,
                previewType = WidgetPreviewType.DAILY_HABITS
            ),
            WidgetInfoItem(
                id = "goals_progress",
                name = "Goals Progress",
                sizeLabel = "4 × 1",
                category = "Goals",
                summary = "Keep long-term objectives and vision in sight.",
                definition = "Keeps your life targets and milestone progress in view. Shows the active goal title, gold repair progress bar, and completion percentage. Tapping opens Goal details or the goal creation flow.",
                receiverClass = GoalsProgressWidgetReceiver::class.java,
                previewType = WidgetPreviewType.GOALS_PROGRESS
            ),
            WidgetInfoItem(
                id = "daily_quote",
                name = "Daily Quote / Intention",
                sizeLabel = "4 × 1",
                category = "Mindfulness",
                summary = "A quiet contemplative space for your morning intention.",
                definition = "Displays your personalized daily morning intention with gold seam accents. If no intention is set, offers inspiring philosophical guidance. Tap to open the intention editor.",
                receiverClass = DailyQuoteWidgetReceiver::class.java,
                previewType = WidgetPreviewType.DAILY_QUOTE
            ),
            WidgetInfoItem(
                id = "daily_progress",
                name = "Daily Progress",
                sizeLabel = "2 × 2",
                category = "Dashboard",
                summary = "Bold circular percentage badge of today's completed items.",
                definition = "Compact square widget featuring a high-contrast percentage badge calculating combined daily ritual and task completion. Sub-counters display exact fractions for habits and tasks. Tap to open Progress.",
                receiverClass = DailyProgressWidgetReceiver::class.java,
                previewType = WidgetPreviewType.DAILY_PROGRESS
            ),
            WidgetInfoItem(
                id = "quick_add",
                name = "Quick Add",
                sizeLabel = "2 × 2",
                category = "Shortcuts",
                summary = "Instant one-tap access to creation workflows.",
                definition = "Provides rapid access to Kintsu's core actions: New Task, New Habit, Start Focus, and Journal Entry. Tapping any action launches the creation sheet immediately without navigating through Home.",
                receiverClass = QuickAddWidgetReceiver::class.java,
                previewType = WidgetPreviewType.QUICK_ADD
            ),
            WidgetInfoItem(
                id = "stacked_summary",
                name = "Stacked Summary",
                sizeLabel = "2 × 2",
                category = "Dashboard",
                summary = "4-row compact overview of habits, tasks, goals & focus.",
                definition = "A dense, balanced square widget summarizing all four pillars of your daily system. Each row shows current metrics and acts as an independent touch target to open its destination.",
                receiverClass = StackedSummaryWidgetReceiver::class.java,
                previewType = WidgetPreviewType.STACKED_SUMMARY
            ),
            WidgetInfoItem(
                id = "aesthetic_quote",
                name = "Aesthetic Intention",
                sizeLabel = "2 × 2",
                category = "Mindfulness",
                summary = "Artistic card with gold kintsugi seam & philosophy.",
                definition = "Crafted for visual serenity. Displays your daily intention or mindful mantra in centered serif styling framed by gold kintsugi divider lines (— ✦ —). Tap to reflect or edit.",
                receiverClass = AestheticQuoteWidgetReceiver::class.java,
                previewType = WidgetPreviewType.AESTHETIC_QUOTE
            ),
            WidgetInfoItem(
                id = "compact_tasks",
                name = "Compact Tasks",
                sizeLabel = "2 × 2",
                category = "Tasks",
                summary = "Space-efficient square widget for upcoming tasks.",
                definition = "Shows your top 2 pending tasks with interactive checkboxes and an instant '+ Task' button. Perfect for tight home screen layouts.",
                receiverClass = CompactTasksWidgetReceiver::class.java,
                previewType = WidgetPreviewType.COMPACT_TASKS
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Dashboard", "Focus", "Tasks", "Habits", "Goals", "Mindfulness", "Shortcuts")

    val filteredWidgets = remember(selectedCategory) {
        if (selectedCategory == "All") allWidgets else allWidgets.filter { it.category == selectedCategory }
    }

    fun pinWidget(item: WidgetInfoItem) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            val provider = ComponentName(context, item.receiverClass)
            val success = appWidgetManager.requestPinAppWidget(provider, null, null)
            if (success) {
                Toast.makeText(context, "Adding \"${item.name}\" to Home Screen...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Long-press your home screen to place \"${item.name}\"", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Touch and hold an empty space on your home screen, then tap Widgets to add.", Toast.LENGTH_LONG).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .background(colors.border, CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.l)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.s),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconWidget(color = colors.goldSeam, size = 22.dp)
                    Spacer(modifier = Modifier.width(spacing.s))
                    Column {
                        Text(
                            text = "Home Screen Widgets",
                            style = typography.screenTitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "11 live widgets • Tap 'Add' to pin directly",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    IconClose(color = colors.textSecondary, size = 16.dp)
                }
            }

            // Category filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = spacing.s),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) colors.accent else colors.surfaceElevated)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.surface else colors.textSecondary
                        )
                    }
                }
            }

            // Widget List
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                items(filteredWidgets, key = { it.id }) { item ->
                    WidgetShowcaseCard(
                        item = item,
                        onPin = { pinWidget(item) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.xl))
                }
            }
        }
    }
}

@Composable
fun WidgetShowcaseCard(
    item: WidgetInfoItem,
    onPin: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .padding(spacing.m)
    ) {
        // Card Header (Title + Size chip + Category chip)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = typography.itemTitle,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.summary,
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Box(
                    modifier = Modifier
                        .background(colors.surfaceElevated, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.sizeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                }
                Box(
                    modifier = Modifier
                        .background(colors.goldSeamSoft, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.goldSeam
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        // Visual Mockup / Live Preview Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            WidgetMockupView(previewType = item.previewType)
        }

        Spacer(modifier = Modifier.height(spacing.s))

        // Definition text
        Text(
            text = item.definition,
            style = typography.caption,
            color = colors.textSecondary,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(spacing.m))

        // Direct Pin Action Button
        Button(
            onClick = onPin,
            modifier = Modifier.fillMaxWidth().height(42.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.surface
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconAdd(color = colors.surface, size = 16.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add to Home Screen",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WidgetMockupView(previewType: WidgetPreviewType) {
    val colors = KintsuTheme.colors

    when (previewType) {
        WidgetPreviewType.TODAY_OVERVIEW -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Good morning, Seeker", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("Wednesday, Oct 30", fontSize = 10.sp, color = colors.textSecondary)
                    }
                    Text("✦ Kintsu", fontSize = 10.sp, color = colors.goldSeam, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    MetricPill("3/5", "Habits", colors.accent)
                    MetricPill("4/6", "Tasks", colors.accent)
                    MetricPill("75%", "Goals", colors.goldSeam)
                    MetricPill("45m", "Focus", colors.textPrimary)
                }
            }
        }
        WidgetPreviewType.FOCUS_TIMER -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Mindful Focus", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("25:00", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Box(
                    modifier = Modifier
                        .background(colors.accent, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Start ▶", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.surface)
                }
            }
        }
        WidgetPreviewType.TODAY_TASKS -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Today's Tasks (4 left)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("+ Task", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                }
                Spacer(modifier = Modifier.height(6.dp))
                MockTaskRow("✓", "Morning reflection & intention", isDone = true)
                MockTaskRow("○", "Deep work: Complete draft", isDone = false)
            }
        }
        WidgetPreviewType.DAILY_HABITS -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Daily Habits", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("3 of 4 done", fontSize = 10.sp, color = colors.accent)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MockHabitCircle("✓", isDone = true)
                    MockHabitCircle("✓", isDone = true)
                    MockHabitCircle("M", isDone = false)
                }
            }
        }
        WidgetPreviewType.GOALS_PROGRESS -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Goal: Read 12 Mindful Books", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("8 of 12 completed • On track", fontSize = 10.sp, color = colors.textSecondary)
                }
                Text("67%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.goldSeam)
            }
        }
        WidgetPreviewType.DAILY_QUOTE -> {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("✦", fontSize = 12.sp, color = colors.goldSeam, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("“Move with gentle patience. Even broken vessels mend with gold.”", fontSize = 11.sp, color = colors.textPrimary)
            }
        }
        WidgetPreviewType.DAILY_PROGRESS -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Daily Progress", fontSize = 11.sp, color = colors.textSecondary)
                Text("82%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                Text("3/4 Habits • 5/6 Tasks", fontSize = 10.sp, color = colors.textSecondary)
            }
        }
        WidgetPreviewType.QUICK_ADD -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Quick Add Actions", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MockQuickChip("＋ Task")
                    MockQuickChip("＋ Habit")
                    MockQuickChip("▶ Focus")
                    MockQuickChip("✎ Journal")
                }
            }
        }
        WidgetPreviewType.STACKED_SUMMARY -> {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MockSummaryRow("Habits", "3/5", colors.accent)
                MockSummaryRow("Tasks", "4/6", colors.accent)
                MockSummaryRow("Goals", "2 active", colors.goldSeam)
                MockSummaryRow("Focus", "45 min", colors.textPrimary)
            }
        }
        WidgetPreviewType.AESTHETIC_QUOTE -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("— ✦ —", fontSize = 11.sp, color = colors.goldSeam, fontWeight = FontWeight.Bold)
                Text("“Small steps every day. The vessel is mended with gold.”", fontSize = 11.sp, color = colors.textPrimary, textAlign = TextAlign.Center)
            }
        }
        WidgetPreviewType.COMPACT_TASKS -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tasks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("2 left", fontSize = 10.sp, color = colors.accent, fontWeight = FontWeight.Bold)
                }
                MockTaskRow("○", "Evening Review & perspective", isDone = false)
            }
        }
    }
}

@Composable
private fun MetricPill(value: String, label: String, color: Color) {
    val colors = KintsuTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 9.sp, color = colors.textSecondary)
    }
}

@Composable
private fun MockTaskRow(mark: String, text: String, isDone: Boolean) {
    val colors = KintsuTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mark,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDone) colors.accent else colors.textSecondary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (isDone) colors.textSecondary else colors.textPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun MockHabitCircle(letter: String, isDone: Boolean) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .size(26.dp)
            .background(if (isDone) colors.accent else colors.surface, CircleShape)
            .border(1.dp, if (isDone) colors.accent else colors.border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDone) colors.surface else colors.textPrimary
        )
    }
}

@Composable
private fun MockQuickChip(text: String) {
    val colors = KintsuTheme.colors
    Box(
        modifier = Modifier
            .background(colors.surface, RoundedCornerShape(8.dp))
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 9.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MockSummaryRow(label: String, value: String, valueColor: Color) {
    val colors = KintsuTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, color = colors.textSecondary)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
