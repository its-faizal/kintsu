package com.kintsu.app.wellbeing.ui

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockScheduleEntity
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuDialog
import com.kintsu.app.ui.theme.KintsuTheme

data class ScheduleTemplate(
    val name: String,
    val startMinute: Int,
    val endMinute: Int,
    val daysMask: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockingSchedulesSheet(
    schedules: List<BlockScheduleEntity>,
    appGroups: List<AppGroupEntity>,
    onAddSchedule: (ruleId: Long, startMin: Int, endMin: Int, daysMask: Int) -> Unit,
    onToggleSchedule: (BlockScheduleEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTemplateName by remember { mutableStateOf("Evening Wind Down") }
    var startMinuteInput by remember { mutableIntStateOf(1320) } // 22:00
    var endMinuteInput by remember { mutableIntStateOf(420) }   // 07:00 (Overnight)
    var daysMaskInput by remember { mutableIntStateOf(127) }      // All 7 days

    val templates = listOf(
        ScheduleTemplate("Study Time", 540, 720, 31),        // 09:00 - 12:00 (Mon-Fri)
        ScheduleTemplate("Deep Focus", 840, 1020, 31),       // 14:00 - 17:00 (Mon-Fri)
        ScheduleTemplate("Evening Wind Down", 1260, 1440, 127), // 21:00 - 24:00 (All)
        ScheduleTemplate("Sleep Routine", 1320, 420, 127)     // 22:00 - 07:00 (Overnight)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(spacing.m)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Automatic Schedules",
                        style = typography.itemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Recurring daily windows to protect deep focus & sleep",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }

                KintsuButton(
                    text = "New Schedule",
                    onClick = { showAddDialog = true },
                    variant = KintsuButtonVariant.PRIMARY
                )
            }

            Spacer(modifier = Modifier.height(spacing.m))

            // Starter Templates Strip
            Text(
                text = "TEMPLATE PROFILES",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.xs))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                templates.forEach { t ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedTemplateName = t.name
                                startMinuteInput = t.startMinute
                                endMinuteInput = t.endMinute
                                daysMaskInput = t.daysMask
                                showAddDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(t.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.goldSeam)
                            Text(
                                "${formatMinuteOfDay(t.startMinute)} – ${formatMinuteOfDay(t.endMinute)}",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.m))

            if (schedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No active schedules yet", style = typography.itemTitle, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text("Add a schedule or choose a template above.", style = typography.caption, color = colors.textTertiary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleRowItem(
                            schedule = schedule,
                            onToggle = { onToggleSchedule(schedule.copy(isEnabled = !schedule.isEnabled)) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        KintsuDialog(
            title = "Add Protection Schedule",
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                KintsuButton(
                    text = "Save Schedule",
                    onClick = {
                        val targetRuleId = appGroups.firstOrNull()?.id ?: 1L
                        onAddSchedule(targetRuleId, startMinuteInput, endMinuteInput, daysMaskInput)
                        showAddDialog = false
                    },
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Cancel",
                    onClick = { showAddDialog = false },
                    variant = KintsuButtonVariant.SECONDARY
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                Text(
                    text = "Profile: $selectedTemplateName",
                    style = typography.itemTitle,
                    color = colors.goldSeam
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Start Time", style = typography.caption, color = colors.textSecondary)
                        Text(formatMinuteOfDay(startMinuteInput), style = typography.itemTitle, color = colors.textPrimary)
                    }
                    Column {
                        Text("End Time", style = typography.caption, color = colors.textSecondary)
                        Text(formatMinuteOfDay(endMinuteInput), style = typography.itemTitle, color = colors.textPrimary)
                    }
                }

                if (endMinuteInput < startMinuteInput) {
                    Text(
                        text = "Crosses midnight into next morning",
                        style = typography.caption.copy(fontWeight = FontWeight.Medium),
                        color = colors.goldSeam
                    )
                }

                Text(
                    text = "Active Days:",
                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )

                val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayNames.forEachIndexed { index, name ->
                        val bit = 1 shl index
                        val isDaySelected = (daysMaskInput and bit) != 0
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDaySelected) colors.goldSeam else colors.surface)
                                .border(1.dp, if (isDaySelected) colors.goldSeam else colors.border, CircleShape)
                                .clickable {
                                    daysMaskInput = if (isDaySelected) daysMaskInput and bit.inv() else daysMaskInput or bit
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDaySelected) colors.background else colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleRowItem(
    schedule: BlockScheduleEntity,
    onToggle: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (schedule.isEnabled) colors.surfaceElevated else colors.surface,
        borderColor = if (schedule.isEnabled) colors.goldSeam.copy(alpha = 0.4f) else colors.border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    IconClock(size = 20.dp, color = colors.goldSeam)
                }
                Spacer(modifier = Modifier.width(spacing.m))
                Column {
                    Text(
                        text = "${formatMinuteOfDay(schedule.startMinuteOfDay)} – ${formatMinuteOfDay(schedule.endMinuteOfDay)}",
                        style = typography.itemTitle,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (schedule.endMinuteOfDay < schedule.startMinuteOfDay) "Overnight • Everyday" else "Daily Window",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            Switch(
                checked = schedule.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.goldSeam,
                    checkedTrackColor = colors.goldSeamSoft
                )
            )
        }
    }
}

private fun formatMinuteOfDay(totalMinutes: Int): String {
    val h = (totalMinutes / 60) % 24
    val m = totalMinutes % 60
    return String.format("%02d:%02d", h, m)
}
