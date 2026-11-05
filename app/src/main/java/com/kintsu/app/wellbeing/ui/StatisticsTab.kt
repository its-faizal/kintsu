package com.kintsu.app.wellbeing.ui

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.components.AppLogo
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.IconDevice
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.domain.ScreenTimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

data class AppUsageRecord(
    val packageName: String,
    val appName: String,
    val durationMinutes: Long
)

@Composable
fun StatisticsTab() {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var activeMetric by remember { mutableStateOf("Screen") } // "Screen" or "Data"
    var totalScreenTimeTodayMinutes by remember { mutableLongStateOf(0L) }
    var hasUsagePermission by remember { mutableStateOf(ScreenTimeHelper.hasUsageStatsPermission(context)) }
    val topAppsList = remember { mutableStateListOf<AppUsageRecord>() }

    // Load actual usage stats from Android system
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            hasUsagePermission = ScreenTimeHelper.hasUsageStatsPermission(context)
            val totalMins = ScreenTimeHelper.getScreenTimeTodayMinutes(context)
            val perApp = ScreenTimeHelper.getPerAppUsageTodayMinutes(context)
            val pm = context.packageManager

            val parsed = perApp.toList()
                .sortedByDescending { it.second }
                .take(10)
                .map { (pkg, mins) ->
                    val label = try {
                        val ai = pm.getApplicationInfo(pkg, 0)
                        pm.getApplicationLabel(ai).toString()
                    } catch (_: Exception) {
                        pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
                    }
                    AppUsageRecord(pkg, label, mins)
                }

            withContext(Dispatchers.Main) {
                totalScreenTimeTodayMinutes = totalMins
                topAppsList.clear()
                topAppsList.addAll(parsed)
            }
        }
    }

    val hours = totalScreenTimeTodayMinutes / 60
    val minutes = totalScreenTimeTodayMinutes % 60
    val formattedScreenTime = if (totalScreenTimeTodayMinutes > 0) {
        if (hours > 0) "$hours hours, $minutes minutes" else "$minutes minutes"
    } else {
        "0 minutes"
    }

    Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
        if (!hasUsagePermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceElevated)
                    .border(1.2.dp, colors.goldSeam, RoundedCornerShape(14.dp))
                    .clickable {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                    .padding(spacing.m)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconDevice(size = 22.dp, color = colors.goldSeam)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Grant System Usage Access",
                            style = typography.itemTitle.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Tap to enable permission in Settings so Kintsu can show live screen time & app stats.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
        // --- 1. HEADER (Title "Statistics" & Screen / Data Switcher) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Statistics",
                style = typography.screenTitle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )

            // Switcher: Screen / Data
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeMetric == "Screen") colors.accentSoft else Color.Transparent)
                        .clickable { activeMetric = "Screen" }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Screen",
                        fontSize = 11.sp,
                        fontWeight = if (activeMetric == "Screen") FontWeight.Bold else FontWeight.Normal,
                        color = if (activeMetric == "Screen") colors.accent else colors.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeMetric == "Data") colors.accentSoft else Color.Transparent)
                        .clickable { activeMetric = "Data" }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Data",
                        fontSize = 11.sp,
                        fontWeight = if (activeMetric == "Data") FontWeight.Bold else FontWeight.Normal,
                        color = if (activeMetric == "Data") colors.accent else colors.textSecondary
                    )
                }
            }
        }

        // --- 2. SCREEN TIME HERO CARD (Image 5) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    IconDevice(color = colors.accent, size = 22.dp)
                }
                Column {
                    Text(
                        text = "Screen time",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = formattedScreenTime,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }
        }

        // --- 3. WEEKLY SCREEN TIME BAR CHART (Image 5) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Bar Chart Canvas with Dotted Guidelines
                val barHeights = listOf(0.95f, 0.72f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f) // Mon, Tue, Wed, Thu, Fri, Sat, Sun
                val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = this.size.width
                        val h = this.size.height
                        val linePaint = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                        // 4 Horizontal guidelines (10h, 8h, 5h, 3h, 0s)
                        val levels = listOf(0.1f, 0.3f, 0.55f, 0.75f, 0.95f)
                        levels.forEach { frac ->
                            val y = h * frac
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = linePaint
                            )
                        }
                    }

                    // Render Bars
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        barHeights.forEachIndexed { index, fraction ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.height(150.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .height((140 * fraction).dp)
                                        .clip(RoundedCornerShape(topStart = 13.dp, topEnd = 13.dp, bottomStart = 13.dp, bottomEnd = 13.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    if (index == 1) colors.accent else colors.surface,
                                                    if (index == 1) colors.accentSoft else colors.surfaceElevated
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }

                    // Day labels along bottom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        dayLabels.forEach { d ->
                            Text(
                                text = d,
                                fontSize = 11.sp,
                                color = colors.textTertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Week Range Selector: < Nov 2 - Nov 8 >
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "‹   Nov 2 - Nov 8   ›",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // --- 4. MOST USED APPS (Image 5) ---
        val todayStr = remember {
            LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Most used apps",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = todayStr,
                fontSize = 11.sp,
                color = colors.textTertiary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            topAppsList.forEach { app ->
                val h = app.durationMinutes / 60
                val m = app.durationMinutes % 60
                val timeLabel = if (h > 0) "$h hours, $m minutes" else "$m minutes"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            AppLogo(
                                packageName = app.packageName,
                                size = 42.dp,
                                shape = RoundedCornerShape(10.dp),
                                fallbackAppName = app.appName
                            )

                            Column {
                                Text(
                                    text = app.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = timeLabel,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        IconClock(color = colors.textTertiary, size = 18.dp)
                    }
                }
            }
        }
    }
}
