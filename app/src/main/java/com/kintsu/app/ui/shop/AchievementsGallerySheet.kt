package com.kintsu.app.ui.shop

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.kintsu.app.data.model.AchievementEntity
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsGallerySheet(
    achievements: List<AchievementEntity>,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var selectedFilter by remember { mutableStateOf("ALL") }

    val earnedCount = achievements.count { it.isEarned }
    val totalPointsEarned = achievements.filter { it.isEarned }.sumOf { it.pointsAwarded }

    val filteredAchievements = remember(achievements, selectedFilter) {
        when (selectedFilter) {
            "EARNED" -> achievements.filter { it.isEarned }
            "LOCKED" -> achievements.filter { !it.isEarned }
            else -> achievements
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Achievements & Collectibles",
        scrollable = false
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            // Milestone Summary Banner
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.m),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "$earnedCount of ${achievements.size} Unlocked",
                            style = typography.itemTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Quiet milestones on your journey of intention.",
                            style = typography.caption.copy(fontSize = 12.sp),
                            color = colors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accent.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+$totalPointsEarned Gold",
                            style = typography.itemTitle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = colors.accent
                        )
                    }
                }
            }

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                val filters = listOf(
                    "ALL" to "All (${achievements.size})",
                    "EARNED" to "Earned ($earnedCount)",
                    "LOCKED" to "In Progress (${achievements.size - earnedCount})"
                )
                items(filters) { (id, label) ->
                    val isSelected = selectedFilter == id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) colors.accent else colors.surfaceElevated)
                            .clickable { selectedFilter = id }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                            color = if (isSelected) Color.Black else colors.textSecondary
                        )
                    }
                }
            }

            // Achievement Cards List
            if (filteredAchievements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No achievements found in this section.",
                        style = typography.secondary,
                        color = colors.textSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    items(filteredAchievements, key = { it.id }) { ach ->
                        KintsuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (ach.isEarned) {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = colors.accent.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    } else Modifier
                                ),
                            backgroundColor = if (ach.isEarned) colors.surfaceElevated else colors.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.m),
                                horizontalArrangement = Arrangement.spacedBy(spacing.m),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Collectible Badge Icon
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (ach.isEarned) colors.accent.copy(alpha = 0.2f)
                                            else colors.border.copy(alpha = 0.3f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ach.badgeIcon,
                                        fontSize = 22.sp
                                    )
                                }

                                // Details
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                                    ) {
                                        Text(
                                            text = ach.title,
                                            style = typography.itemTitle.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = colors.textPrimary
                                        )
                                        if (ach.isEarned) {
                                            Text(
                                                text = "✦",
                                                color = colors.accent,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = ach.description,
                                        style = typography.caption.copy(fontSize = 12.sp),
                                        color = colors.textSecondary
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    if (ach.isEarned && ach.earnedAtMillis != null) {
                                        Text(
                                            text = "Earned on ${dateFormat.format(Date(ach.earnedAtMillis))}",
                                            style = typography.caption.copy(fontSize = 10.sp),
                                            color = colors.accent
                                        )
                                    } else {
                                        Text(
                                            text = "Locked",
                                            style = typography.caption.copy(fontSize = 10.sp),
                                            color = colors.textSecondary.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                // Reward Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (ach.isEarned) colors.accent.copy(alpha = 0.15f)
                                            else colors.border.copy(alpha = 0.2f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "+${ach.pointsAwarded} Gold",
                                        style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                        color = if (ach.isEarned) colors.accent else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
