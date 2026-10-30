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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingSettingsSheet(
    currentStyle: String,
    currentDurationSeconds: Int,
    currentReinterventionMinutes: Int,
    currentHaptics: Boolean,
    onSavePreferences: (style: String, durationSec: Int, reinterventionMin: Int, haptics: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedStyle by remember { mutableStateOf(currentStyle) }
    var selectedDuration by remember { mutableIntStateOf(currentDurationSeconds) }
    var selectedReintervention by remember { mutableIntStateOf(currentReinterventionMinutes) }
    var hapticsEnabled by remember { mutableStateOf(currentHaptics) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.9f)
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
                        text = "Mindful Interventions",
                        style = typography.itemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Customize the calm pause presented on opening distracting apps",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Style Picker (4 Styles from Spec)
            Text(
                text = "INTERVENTION STYLE",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.xs))

            val styles = listOf(
                "BREATHING" to "Breathing Circle" to "Softly expanding & contracting circle with inhale/exhale pacing",
                "INTENTIONAL_CHOICE" to "Intentional Choice" to "Ask 'What are you opening this for?' with quick reflection chips",
                "GENTLE_DELAY" to "Gentle Delay" to "A simple, non-distracting progress countdown bar",
                "REFLECTION" to "Reflection Question" to "Presents 'Is this how you want to spend the next few minutes?'"
            )

            styles.forEach { (pair, desc) ->
                val (styleKey, title) = pair
                val isSel = selectedStyle == styleKey
                KintsuCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedStyle = styleKey },
                    backgroundColor = if (isSel) colors.goldSeamSoft else colors.surfaceElevated,
                    borderColor = if (isSel) colors.goldSeam else colors.border
                ) {
                    Column(modifier = Modifier.padding(spacing.m)) {
                        Text(
                            text = title,
                            style = typography.itemTitle,
                            color = if (isSel) colors.goldSeam else colors.textPrimary
                        )
                        Text(
                            text = desc,
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Duration Presets
            Text(
                text = "INTERVENTION DURATION",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.xs))

            val durations = listOf(5, 10, 15, 30)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                durations.forEach { sec ->
                    val isSel = selectedDuration == sec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) colors.goldSeamSoft else colors.surfaceElevated)
                            .border(1.dp, if (isSel) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                            .clickable { selectedDuration = sec }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${sec}s",
                            fontSize = 13.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) colors.goldSeam else colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Re-intervention Intervals
            Text(
                text = "RE-INTERVENTION INTERVAL",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.xs))

            val intervals = listOf(5, 10, 15, 20)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                intervals.forEach { min ->
                    val isSel = selectedReintervention == min
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) colors.goldSeamSoft else colors.surfaceElevated)
                            .border(1.dp, if (isSel) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                            .clickable { selectedReintervention = min }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Every ${min}m",
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) colors.goldSeam else colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Haptics Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hapticsEnabled = !hapticsEnabled },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gentle Breathing Haptics", style = typography.body, color = colors.textPrimary)
                    Text("Soft tactile pulse during inhale/exhale phases", style = typography.caption, color = colors.textSecondary)
                }
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = { hapticsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.goldSeam,
                        checkedTrackColor = colors.goldSeamSoft
                    )
                )
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            KintsuButton(
                text = "Save Intervention Preferences",
                onClick = {
                    onSavePreferences(selectedStyle, selectedDuration, selectedReintervention, hapticsEnabled)
                    onDismiss()
                },
                variant = KintsuButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
