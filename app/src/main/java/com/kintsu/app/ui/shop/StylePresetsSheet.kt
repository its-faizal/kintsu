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
    onApplyPreset: (StylePresetEntity) -> Unit,
    onDeletePreset: (StylePresetEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Style Presets",
        scrollable = false,
        trailingAction = {
            KintsuButton(
                text = "+ Save Look",
                onClick = { showSaveDialog = true },
                variant = KintsuButtonVariant.PRIMARY
            )
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
                    Text(
                        text = "No saved style presets yet.\nTap '+ Save Look' to save your current theme, icon, and widget configuration.",
                        style = typography.secondary,
                        color = colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.m),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        style = typography.itemTitle.copy(fontSize = 14.sp),
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${preset.themeName} • ${preset.accentName} • ${preset.widgetSkinId.replace("widget_", "").replace("_", " ").capitalize()}",
                                        style = typography.caption.copy(fontSize = 11.sp),
                                        color = colors.textSecondary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                    KintsuButton(
                                        text = "Apply",
                                        onClick = { onApplyPreset(preset) },
                                        variant = KintsuButtonVariant.PRIMARY
                                    )
                                    KintsuButton(
                                        text = "Delete",
                                        onClick = { onDeletePreset(preset) },
                                        variant = KintsuButtonVariant.SECONDARY
                                    )
                                }
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
}
