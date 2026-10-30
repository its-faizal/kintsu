package com.kintsu.app.wellbeing.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.DistractionInterventionLogEntity
import com.kintsu.app.data.model.FocusLockSessionEntity
import com.kintsu.app.data.model.IntentionalBreakLogEntity
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.ScreenTimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistractionInsightsSheet(
    interventions: List<DistractionInterventionLogEntity>,
    focusLockSessions: List<FocusLockSessionEntity>,
    breaks: List<IntentionalBreakLogEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var screenTimeTodayMin by remember { mutableStateOf(0L) }
    var weeklyTrends by remember { mutableStateOf<List<Pair<String, Long>>>(emptyList()) }
    var topAppsList by remember { mutableStateOf<List<Pair<String, Long>>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val stToday = ScreenTimeHelper.getScreenTimeTodayMinutes(context)
            val trends = ScreenTimeHelper.getWeeklyScreenTimeTrends(context)
            val pm = context.packageManager
            val perApp = ScreenTimeHelper.getPerAppUsageTodayMinutes(context)
            val topApps = perApp.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { entry ->
                    val appName = try {
                        val ai = pm.getApplicationInfo(entry.key, 0)
                        pm.getApplicationLabel(ai).toString()
                    } catch (_: Exception) {
                        entry.key.substringAfterLast('.')
                    }
                    appName to entry.value
                }

            withContext(Dispatchers.Main) {
                screenTimeTodayMin = stToday
                weeklyTrends = trends
                topAppsList = topApps
            }
        }
    }

    val totalInterventions = interventions.size
    val returnedCount = interventions.count { it.userAction == "RETURNED_TO_FOCUS" }
    val successRatePct = if (totalInterventions > 0) ((returnedCount * 100) / totalInterventions) else 0

    val focusLockTimeCompletedMinutes = focusLockSessions.filter { it.isCompleted }.sumOf { it.durationMinutes }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(spacing.l)
        ) {
            // Header
            Text(
                text = "Distraction Insights",
                style = typography.itemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Text(
                text = "Real screen-time patterns and mindful intervention metrics",
                style = typography.caption,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // 4 High-level Insight Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                MetricCard(
                    title = "Screen Time",
                    value = "${screenTimeTodayMin / 60}h ${screenTimeTodayMin % 60}m",
                    subtitle = "Today's usage",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Mindful Pauses",
                    value = "$totalInterventions",
                    subtitle = "$successRatePct% stepped back",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(spacing.s))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                MetricCard(
                    title = "Focus Lock",
                    value = "${focusLockTimeCompletedMinutes}m",
                    subtitle = "${focusLockSessions.count { it.isCompleted }} sessions completed",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Breaks Taken",
                    value = "${breaks.size}",
                    subtitle = "Intentional pauses",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // 7-Day Screen Time Trend Bar Chart
            Text(
                text = "7-DAY SCREEN TIME TRENDS",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.s))

            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m)
                ) {
                    val maxTrendMin = (weeklyTrends.maxOfOrNull { it.second } ?: 120L).coerceAtLeast(60L)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyTrends.forEach { (day, minutes) ->
                            val heightRatio = (minutes.toFloat() / maxTrendMin.toFloat()).coerceIn(0.08f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (minutes > 0) "${minutes}m" else "0",
                                    fontSize = 10.sp,
                                    color = colors.textTertiary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height((heightRatio * 85).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (minutes > 120) colors.goldSeam else colors.goldSeamSoft)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Top Distracting Applications
            Text(
                text = "TOP APPLICATIONS TODAY",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.s))

            if (topAppsList.isEmpty()) {
                Text("No app usage recorded today yet.", style = typography.body, color = colors.textSecondary)
            } else {
                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surfaceElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m),
                        verticalArrangement = Arrangement.spacedBy(spacing.s)
                    ) {
                        topAppsList.forEach { (appName, mins) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(appName, style = typography.body, color = colors.textPrimary)
                                Text("${mins}m", style = typography.body.copy(fontWeight = FontWeight.SemiBold), color = colors.goldSeam)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    KintsuCard(
        modifier = modifier,
        backgroundColor = colors.surfaceElevated
    ) {
        Column(modifier = Modifier.padding(spacing.m)) {
            Text(title, style = typography.caption, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(value, style = typography.itemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = colors.goldSeam)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = colors.textTertiary)
        }
    }
}
