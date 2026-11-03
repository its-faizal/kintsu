package com.kintsu.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuFilterChip
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalSheet(
    existingEntry: JournalEntryEntity?,
    onDismiss: () -> Unit,
    onSave: (content: String, mood: String?, tags: String) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var content by remember { mutableStateOf(existingEntry?.content ?: "") }
    var selectedMood by remember { mutableStateOf(existingEntry?.mood) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    // Custom tags state
    var tagsList by remember {
        mutableStateOf(
            existingEntry?.tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        )
    }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }

    // Clean, signature text moods without raw emojis
    val moodOptions = listOf("Calm", "Grounded", "Energized", "Peaceful", "Grateful")

    fun requestDismiss() {
        val hasChanges = content.isNotBlank() && (
            content != (existingEntry?.content ?: "") ||
            selectedMood != existingEntry?.mood ||
            tagsList.joinToString(",") != (existingEntry?.tags ?: "")
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

            // Custom Tags Section
            Text(
                text = "Tags & Themes",
                style = typography.caption,
                color = colors.textSecondary
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                verticalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                tagsList.forEach { tag ->
                    KintsuFilterChip(
                        label = "#$tag ×",
                        selected = true,
                        onClick = {
                            tagsList = tagsList.filter { it != tag }
                        }
                    )
                }
                KintsuFilterChip(
                    label = "+ Add Tag",
                    selected = false,
                    onClick = {
                        newTagInput = ""
                        showAddTagDialog = true
                    }
                )
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
                        onSave(content, selectedMood, tagsList.joinToString(","))
                    }
                },
                enabled = content.isNotBlank(),
                variant = KintsuButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = {
                Text(
                    text = "Add Custom Tag",
                    style = typography.itemTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    placeholder = { Text("e.g. Gratitude, DeepFocus, Walk", color = colors.textTertiary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = KintsuTheme.shapes.medium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clean = newTagInput.trim().replace("#", "").replace(",", "")
                        if (clean.isNotBlank() && !tagsList.contains(clean)) {
                            tagsList = tagsList + clean
                        }
                        showAddTagDialog = false
                    }
                ) {
                    Text("Add", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.background,
            shape = KintsuTheme.shapes.large
        )
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = {
                Text(
                    text = "Release Words?",
                    style = typography.itemTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "This reflection has not been preserved. Discarding will release what you have written.",
                    style = typography.secondary,
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
                    Text("Discard", color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("Keep Writing", color = colors.textPrimary)
                }
            },
            containerColor = colors.background,
            shape = KintsuTheme.shapes.large
        )
    }
}
