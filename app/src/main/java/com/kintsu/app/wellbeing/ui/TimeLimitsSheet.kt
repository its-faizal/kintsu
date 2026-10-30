package com.kintsu.app.wellbeing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLimitsSheet(
    dailyBudgetMinutes: Int,
    reinterventionMinutes: Int,
    appGroups: List<AppGroupEntity>,
    blockRules: List<BlockRuleEntity>,
    onSaveDailyBudget: (Int) -> Unit,
    onSaveReintervention: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentBudget by remember { mutableIntStateOf(dailyBudgetMinutes) }
    var currentSessionLimit by remember { mutableIntStateOf(reinterventionMinutes) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.85f)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Screen-Time Limits",
                        style = typography.itemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Set healthy boundaries for your daily device usage",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Combined Daily Screen-Time Budget
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.l)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Combined Daily Budget",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${currentBudget / 60}h ${currentBudget % 60}m",
                            style = typography.itemTitle.copy(fontWeight = FontWeight.Bold),
                            color = colors.goldSeam
                        )
                    }
                    Text(
                        text = "Total daily screen-time allowance across distracting apps",
                        style = typography.caption,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(spacing.m))

                    Slider(
                        value = currentBudget.toFloat(),
                        onValueChange = { currentBudget = (it / 15).toInt() * 15 },
                        valueRange = 15f..360f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.goldSeam,
                            activeTrackColor = colors.goldSeam,
                            inactiveTrackColor = colors.border
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("15m", style = typography.caption, color = colors.textTertiary)
                        Text("2 hours", style = typography.caption, color = colors.textTertiary)
                        Text("6 hours", style = typography.caption, color = colors.textTertiary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Mindful Session Limit
            KintsuCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.l)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Single Session Allowance",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${currentSessionLimit}m",
                            style = typography.itemTitle.copy(fontWeight = FontWeight.Bold),
                            color = colors.goldSeam
                        )
                    }
                    Text(
                        text = "How long you can use a distracting app after a mindful pause before the next check-in",
                        style = typography.caption,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(spacing.m))

                    val presets = listOf(5, 10, 15, 20, 30)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        presets.forEach { mins ->
                            val isSel = currentSessionLimit == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) colors.goldSeamSoft else colors.surface)
                                    .border(1.dp, if (isSel) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                                    .clickable { currentSessionLimit = mins }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) colors.goldSeam else colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            KintsuButton(
                text = "Save Time Boundaries",
                onClick = {
                    onSaveDailyBudget(currentBudget)
                    onSaveReintervention(currentSessionLimit)
                    onDismiss()
                },
                variant = KintsuButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
