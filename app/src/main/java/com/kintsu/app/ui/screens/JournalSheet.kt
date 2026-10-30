package com.kintsu.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalSheet(
    existingEntry: JournalEntryEntity?,
    onDismiss: () -> Unit,
    onSave: (content: String, mood: String?) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var content by remember { mutableStateOf(existingEntry?.content ?: "") }
    var selectedMood by remember { mutableStateOf(existingEntry?.mood) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    val moodOptions = listOf("🌿 Calm", "⛰ Grounded", "☀️ Energized", "🪷 Peaceful", "🤍 Grateful")

    fun requestDismiss() {
        val hasChanges = content.isNotBlank() && (
            content != (existingEntry?.content ?: "") ||
            selectedMood != existingEntry?.mood
        )
        if (hasChanges) {
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
        title = if (existingEntry == null) "Quiet Reflection" else "Edit Reflection"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
            Text(
                text = "A pause to capture what was noticed, felt, or mended today.",
                style = typography.caption,
                color = colors.textSecondary
            )

            // Mood chips
            Text(
                text = "Atmosphere of Mind",
                style = typography.caption,
                color = colors.textSecondary
            )
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                verticalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                moodOptions.forEach { mood ->
                    KintsuFilterChip(
                        label = mood,
                        selected = selectedMood == mood,
                        onClick = {
                            selectedMood = if (selectedMood == mood) null else mood
                        }
                    )
                }
            }

            // Free text reflection
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text(
                        text = "Pour words gently without judgment. What remains true at the end of the day?",
                        style = typography.body,
                        color = colors.textTertiary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = KintsuTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border
                ),
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(spacing.s))

            KintsuButton(
                text = "Preserve Reflection",
                onClick = {
                    if (content.isNotBlank()) {
                        onSave(content, selectedMood)
                    }
                },
                enabled = content.isNotBlank(),
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
                    text = "Discard Reflection?",
                    style = typography.sectionHeader,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "You have unsaved reflections. Are you sure you want to discard them?",
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
                    Text("Keep Writing", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface,
            shape = KintsuTheme.shapes.large
        )
    }
}
