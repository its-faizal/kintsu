package com.kintsu.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kintsu.app.data.model.WeeklyReviewEntity
import com.kintsu.app.domain.review.WeeklyReviewGenerator
import com.kintsu.app.ui.components.IconFlame
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewSheet(
    draftReview: WeeklyReviewEntity,
    onDismiss: () -> Unit,
    onSave: (WeeklyReviewEntity) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var reflection1 by remember { mutableStateOf(draftReview.reflection1) }
    var reflection2 by remember { mutableStateOf(draftReview.reflection2) }
    var reflection3 by remember { mutableStateOf(draftReview.reflection3) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    fun requestDismiss() {
        val hasChanges = reflection1 != draftReview.reflection1 ||
            reflection2 != draftReview.reflection2 ||
            reflection3 != draftReview.reflection3
        if (hasChanges && (reflection1.isNotBlank() || reflection2.isNotBlank() || reflection3.isNotBlank())) {
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
        title = "Weekly Review & Perspective"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
            Text(
                text = "Pure perspective derived from your real practice. No judgment, only awareness.",
                style = typography.caption,
                color = colors.textSecondary
            )

            // Real Data Metric Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                KintsuCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(spacing.m)) {
                        Text(
                            text = "Rhythm",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${(draftReview.completionRate * 100).toInt()}%",
                            style = typography.sectionHeader,
                            color = colors.accent
                        )
                    }
                }

                KintsuCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(spacing.m)) {
                        Text(
                            text = "Focus",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${draftReview.focusMinutes}m",
                            style = typography.sectionHeader,
                            color = colors.textPrimary
                        )
                    }
                }

                KintsuCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(spacing.m)) {
                        Text(
                            text = "Gold Mended",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${draftReview.totalRepairs}",
                            style = typography.sectionHeader,
                            color = colors.goldSeam
                        )
                    }
                }
            }

            // Consistency Highlights
            if (draftReview.mostConsistentHabitName != null) {
                Text(
                    text = "Most Steady Ritual: ${draftReview.mostConsistentHabitName}",
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }
            if (draftReview.leastConsistentHabitName != null) {
                Text(
                    text = "Area Requesting Care: ${draftReview.leastConsistentHabitName}",
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }
            Text(
                text = "Horizon Goals: ${draftReview.goalsProgressSummary}",
                style = typography.caption,
                color = colors.textTertiary
            )

            Spacer(modifier = Modifier.height(spacing.xxs))

            // Fixed Reflection Prompts
            Text(
                text = WeeklyReviewGenerator.PROMPT_1,
                style = typography.secondary,
                color = colors.textPrimary
            )
            OutlinedTextField(
                value = reflection1,
                onValueChange = { reflection1 = it },
                placeholder = {
                    Text(
                        text = "e.g. Early mornings with hot tea, long breaths between tasks",
                        style = typography.caption,
                        color = colors.textTertiary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = KintsuTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border
                ),
                maxLines = 3
            )

            Text(
                text = WeeklyReviewGenerator.PROMPT_2,
                style = typography.secondary,
                color = colors.textPrimary
            )
            OutlinedTextField(
                value = reflection2,
                onValueChange = { reflection2 = it },
                placeholder = {
                    Text(
                        text = "e.g. Wednesday was overwhelming; I mended it Thursday with grace",
                        style = typography.caption,
                        color = colors.textTertiary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = KintsuTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border
                ),
                maxLines = 3
            )

            Text(
                text = WeeklyReviewGenerator.PROMPT_3,
                style = typography.secondary,
                color = colors.textPrimary
            )
            OutlinedTextField(
                value = reflection3,
                onValueChange = { reflection3 = it },
                placeholder = {
                    Text(
                        text = "e.g. Keep movement gentle; protect 25 minutes of silent reading daily",
                        style = typography.caption,
                        color = colors.textTertiary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = KintsuTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(spacing.s))

            KintsuButton(
                text = "Close & Seal Weekly Review",
                onClick = {
                    onSave(
                        draftReview.copy(
                            reflection1 = reflection1,
                            reflection2 = reflection2,
                            reflection3 = reflection3
                        )
                    )
                },
                variant = KintsuButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = {
                Text(
                    text = "Discard Review Changes?",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "You have unsaved review reflections. Are you sure you want to discard them?",
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
