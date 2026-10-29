package com.kintsu.app.wellbeing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuProgressRing
import com.kintsu.app.ui.theme.KintsuTheme

data class ScreenTimeStats(
    val todayMinutes: Int = 85,
    val dailyGoalMinutes: Int = 120,
    val phonePickupsToday: Int = 24,
    val longestSessionMinutes: Int = 32,
    val sevenDayAverageMinutes: Int = 105,
    val topAppBreakdown: List<Pair<String, Int>> = listOf(
        Pair("Social & Feeds", 40),
        Pair("Reading & News", 25),
        Pair("Mindful Tools", 20)
    )
)

@Composable
fun ScreenTimeInsightsCard(
    stats: ScreenTimeStats,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    val goalProgress = if (stats.dailyGoalMinutes > 0) {
        (stats.todayMinutes.toFloat() / stats.dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val hours = stats.todayMinutes / 60
    val minutes = stats.todayMinutes % 60
    val timeLabel = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    KintsuCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(spacing.m)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Screen Time Today",
                        style = typography.itemTitle,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "$timeLabel of ${stats.dailyGoalMinutes / 60}h mindful daily intention",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }

                KintsuProgressRing(
                    progress = goalProgress,
                    size = 44.dp,
                    strokeWidth = 3.5.dp,
                    showPercentage = false
                )
            }

            Spacer(modifier = Modifier.height(spacing.m))

            // Pickups & Longest session row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceTranslucent)
                        .padding(spacing.s)
                ) {
                    Column {
                        Text(
                            text = "${stats.phonePickupsToday}",
                            style = typography.itemTitle.copy(color = colors.accent, fontSize = 16.sp)
                        )
                        Text(
                            text = "Phone Pickups",
                            style = typography.caption.copy(fontSize = 11.sp),
                            color = colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceTranslucent)
                        .padding(spacing.s)
                ) {
                    Column {
                        Text(
                            text = "${stats.longestSessionMinutes}m",
                            style = typography.itemTitle.copy(color = colors.goldSeam, fontSize = 16.sp)
                        )
                        Text(
                            text = "Longest Flow",
                            style = typography.caption.copy(fontSize = 11.sp),
                            color = colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceTranslucent)
                        .padding(spacing.s)
                ) {
                    Column {
                        Text(
                            text = "${stats.sevenDayAverageMinutes / 60}h ${stats.sevenDayAverageMinutes % 60}m",
                            style = typography.itemTitle.copy(color = colors.textPrimary, fontSize = 16.sp)
                        )
                        Text(
                            text = "7-Day Avg",
                            style = typography.caption.copy(fontSize = 11.sp),
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.m))

            // Top app / category bars
            stats.topAppBreakdown.forEach { (catName, catMinutes) ->
                val fraction = if (stats.todayMinutes > 0) catMinutes.toFloat() / stats.todayMinutes.toFloat() else 0f
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = catName,
                        style = typography.caption,
                        color = colors.textSecondary,
                        modifier = Modifier.width(110.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.accent)
                        )
                    }
                    Spacer(modifier = Modifier.width(spacing.s))
                    Text(
                        text = "${catMinutes}m",
                        style = typography.caption.copy(fontSize = 11.sp),
                        color = colors.textTertiary,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}
