package com.kintsu.app.wellbeing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLimitsSessionSheet(
    dailyBudgetMinutes: Int,
    maxContinuousSessionMinutes: Int,
    sessionCooldownMinutes: Int,
    appLaunchLimitEnabled: Boolean,
    defaultAppMaxLaunches: Int,
    bedtimeLockEnabled: Boolean,
    bedtimeStartHour: Int,
    bedtimeEndHour: Int,
    mindfulPauseOnLaunch: Boolean,
    onSaveDailyBudget: (Int) -> Unit,
    onSaveContinuousSession: (maxSession: Int, cooldown: Int) -> Unit,
    onSaveLaunchLimit: (enabled: Boolean, maxLaunches: Int) -> Unit,
    onSaveBedtimeLock: (enabled: Boolean, startHour: Int, endHour: Int) -> Unit,
    onSaveMindfulPause: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var budgetMin by remember { mutableIntStateOf(dailyBudgetMinutes) }
    var continuousMin by remember { mutableIntStateOf(maxContinuousSessionMinutes) }
    var cooldownMin by remember { mutableIntStateOf(sessionCooldownMinutes) }
    var launchLimitOn by remember { mutableStateOf(appLaunchLimitEnabled) }
    var maxLaunches by remember { mutableIntStateOf(defaultAppMaxLaunches) }
    var bedtimeOn by remember { mutableStateOf(bedtimeLockEnabled) }
    var bedtimeStart by remember { mutableIntStateOf(bedtimeStartHour) }
    var bedtimeEnd by remember { mutableIntStateOf(bedtimeEndHour) }
    var mindfulPauseOn by remember { mutableStateOf(mindfulPauseOnLaunch) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.l, vertical = spacing.s)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.l)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App Limits & Session Guardian",
                        style = typography.itemTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Prevent doomscroll loops and establish intentional boundaries",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    IconClose(color = colors.textSecondary, size = 16.dp)
                }
            }

            // 1. Daily Total Screen Time Budget
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    text = "DAILY SCREEN TIME BUDGET",
                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                    color = colors.textTertiary
                )
                Text(
                    text = "Total daily device allowance across all tracked applications.",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                val budgetOptions = listOf(30, 45, 60, 90, 120, 180, 240)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    budgetOptions.forEach { m ->
                        val isSelected = budgetMin == m
                        val label = if (m >= 60) "${m / 60}h${if (m % 60 != 0) "${m % 60}m" else ""}" else "${m}m"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.accentSoft else colors.surface)
                                .border(1.dp, if (isSelected) colors.accent else colors.border.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                budgetMin = m
                                onSaveDailyBudget(m)
                            }
                            .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.accent else colors.textPrimary
                            )
                        }
                    }
                }
            }

            // 2. Active Continuous Session Guardian (Doomscroll Limiter)
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    text = "CONTINUOUS SESSION GUARDIAN",
                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                    color = colors.textTertiary
                )
                Text(
                    text = "Stops unbroken doomscrolling by enforcing a mandatory cool-down timeout.",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Max Continuous Usage Before Lock",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                val sessionOptions = listOf(10, 15, 20, 30, 45)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sessionOptions.forEach { m ->
                        val isSelected = continuousMin == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.goldSeamSoft else colors.surface)
                                .border(1.dp, if (isSelected) colors.goldSeam else colors.border.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    continuousMin = m
                                    onSaveContinuousSession(m, cooldownMin)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${m}m",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.goldSeam else colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Mandatory Cooldown Duration",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                val cooldownOptions = listOf(3, 5, 10, 15)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cooldownOptions.forEach { m ->
                        val isSelected = cooldownMin == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.goldSeamSoft else colors.surface)
                                .border(1.dp, if (isSelected) colors.goldSeam else colors.border.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    cooldownMin = m
                                    onSaveContinuousSession(continuousMin, m)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${m}m rest",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.goldSeam else colors.textPrimary
                            )
                        }
                    }
                }
            }

            // 3. Daily App Launch Limiter
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "APP LAUNCH LIMITER",
                            style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                            color = colors.textTertiary
                        )
                        Text(
                            text = "Cap how many times distracting apps can be opened per day.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                    Switch(
                        checked = launchLimitOn,
                        onCheckedChange = {
                            launchLimitOn = it
                            onSaveLaunchLimit(it, maxLaunches)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.accent,
                            checkedTrackColor = colors.accentSoft
                        )
                    )
                }

                if (launchLimitOn) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Max Launches Per Day",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    val launchOptions = listOf(3, 5, 8, 10, 15, 20)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        launchOptions.forEach { count ->
                            val isSelected = maxLaunches == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.accentSoft else colors.surface)
                                    .border(1.dp, if (isSelected) colors.accent else colors.border.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        maxLaunches = count
                                        onSaveLaunchLimit(true, count)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count opens",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.accent else colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 4. Bedtime Sanctuary Curfew
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BEDTIME SANCTUARY CURFEW",
                            style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                            color = colors.textTertiary
                        )
                        Text(
                            text = "Lock out addictive apps during sleeping hours.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                    Switch(
                        checked = bedtimeOn,
                        onCheckedChange = {
                            bedtimeOn = it
                            onSaveBedtimeLock(it, bedtimeStart, bedtimeEnd)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.accent,
                            checkedTrackColor = colors.accentSoft
                        )
                    )
                }

                if (bedtimeOn) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Curfew Starts", fontSize = 11.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            val startOptions = listOf(21, 22, 23, 0)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                startOptions.forEach { h ->
                                    val isSelected = bedtimeStart == h
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) colors.goldSeamSoft else colors.surface)
                                            .border(1.dp, if (isSelected) colors.goldSeam else colors.border.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .clickable {
                                                bedtimeStart = h
                                                onSaveBedtimeLock(true, h, bedtimeEnd)
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (h == 0) "12 AM" else "${if (h > 12) h - 12 else h} PM",
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) colors.goldSeam else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Curfew Ends", fontSize = 11.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            val endOptions = listOf(5, 6, 7, 8)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                endOptions.forEach { h ->
                                    val isSelected = bedtimeEnd == h
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) colors.goldSeamSoft else colors.surface)
                                            .border(1.dp, if (isSelected) colors.goldSeam else colors.border.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .clickable {
                                                bedtimeEnd = h
                                                onSaveBedtimeLock(true, bedtimeStart, h)
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$h AM",
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) colors.goldSeam else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Mindful Pause on Launch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mindful Launch Pause",
                        style = typography.itemTitle.copy(fontSize = 14.sp),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Presents a 5-second breath before launching distracting apps",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
                Switch(
                    checked = mindfulPauseOn,
                    onCheckedChange = {
                        mindfulPauseOn = it
                        onSaveMindfulPause(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accent,
                        checkedTrackColor = colors.accentSoft
                    )
                )
            }

            Spacer(modifier = Modifier.height(spacing.s))
        }
    }
}
