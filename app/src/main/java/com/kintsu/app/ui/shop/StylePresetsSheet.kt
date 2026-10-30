package com.kintsu.app.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.KintsuSettings
import com.kintsu.app.data.model.StylePresetEntity
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuDialog
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylePresetsSheet(
    presets: List<StylePresetEntity>,
    currentSettings: KintsuSettings,
    onSavePreset: (name: String, theme: String, accent: String, icon: String, skin: String, face: String) -> Unit,
    onRenamePreset: (StylePresetEntity, String) -> Unit = { _, _ -> },
    onApplyPreset: (StylePresetEntity) -> Unit,
    onDeletePreset: (StylePresetEntity) -> Unit,
    onRestoreDefaults: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

    var renamingPreset by remember { mutableStateOf<StylePresetEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Style Presets",
        scrollable = false,
        trailingAction = {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                KintsuButton(
                    text = "Curated",
                    onClick = onRestoreDefaults,
                    variant = KintsuButtonVariant.SECONDARY
                )
                KintsuButton(
                    text = "+ Save Look",
                    onClick = { showSaveDialog = true },
                    variant = KintsuButtonVariant.PRIMARY
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            Text(
                text = "Save and switch between your favorite holistic aesthetic combinations with a single tap.",
                style = typography.secondary,
                color = colors.textSecondary
            )

            if (presets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.m)
                    ) {
                        Text(
                            text = "No saved style presets yet.\nTap 'Curated' to load ready-made designer combinations, or '+ Save Look' to capture your current aesthetic.",
                            style = typography.secondary,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        KintsuButton(
                            text = "Load 6 Ready-Made Presets",
                            onClick = onRestoreDefaults,
                            variant = KintsuButtonVariant.PRIMARY
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    items(presets, key = { it.id }) { preset ->
                        KintsuCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.surfaceElevated
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.m),
                                verticalArrangement = Arrangement.spacedBy(spacing.xs)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = typography.itemTitle.copy(fontSize = 15.sp),
                                        color = colors.textPrimary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                        KintsuButton(
                                            text = "Apply",
                                            onClick = { onApplyPreset(preset) },
                                            variant = KintsuButtonVariant.PRIMARY
                                        )
                                        KintsuButton(
                                            text = "Rename",
                                            onClick = {
                                                renamingPreset = preset
                                                renameInput = preset.name
                                            },
                                            variant = KintsuButtonVariant.SECONDARY
                                        )
                                        KintsuButton(
                                            text = "Delete",
                                            onClick = { onDeletePreset(preset) },
                                            variant = KintsuButtonVariant.SECONDARY
                                        )
                                    }
                                }
                                Text(
                                    text = "${preset.themeName} • ${preset.accentName} • ${preset.widgetSkinId.replace("widget_", "").replace("_", " ").replaceFirstChar { it.uppercase() }} • ${preset.focusFaceId.replace("focus_face_", "").replace("_", " ").replaceFirstChar { it.uppercase() }}",
                                    style = typography.caption.copy(fontSize = 11.sp),
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        KintsuDialog(
            title = "Save Appearance Preset",
            onDismissRequest = { showSaveDialog = false },
            confirmButton = {
                KintsuButton(
                    text = "Save",
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            onSavePreset(
                                presetNameInput.trim(),
                                currentSettings.appTheme.displayName,
                                currentSettings.accentOption.name,
                                currentSettings.launcherIconId,
                                currentSettings.widgetSkinId,
                                currentSettings.focusTimerFaceId
                            )
                            showSaveDialog = false
                            presetNameInput = ""
                        }
                    },
                    enabled = presetNameInput.isNotBlank(),
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Cancel",
                    onClick = { showSaveDialog = false },
                    variant = KintsuButtonVariant.SECONDARY
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text(
                    text = "Save current active theme (${currentSettings.appTheme.displayName}), icon, and widget skins as a one-tap preset:",
                    style = typography.secondary,
                    color = colors.textPrimary
                )
                OutlinedTextField(
                    value = presetNameInput,
                    onValueChange = { presetNameInput = it },
                    label = { Text("Preset Name") },
                    placeholder = { Text("e.g. My Morning Zen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border
                    )
                )
            }
        }
    }

    renamingPreset?.let { preset ->
        KintsuDialog(
            title = "Rename Style Preset",
            onDismissRequest = { renamingPreset = null },
            confirmButton = {
                KintsuButton(
                    text = "Update",
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRenamePreset(preset, renameInput.trim())
                            renamingPreset = null
                        }
                    },
                    enabled = renameInput.isNotBlank(),
                    variant = KintsuButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Cancel",
                    onClick = { renamingPreset = null },
                    variant = KintsuButtonVariant.SECONDARY
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border
                    )
                )
            }
        }
    }
}
