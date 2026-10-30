package com.kintsu.app.wellbeing.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.DigitalDetoxEntity
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalDetoxSheet(
    activeDetox: DigitalDetoxEntity?,
    onStartDetox: (durationMinutes: Int, intention: String, allowedPackages: String, enableDnd: Boolean) -> Unit,
    onEndDetoxEarly: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    val durations = listOf(
        Pair("1 Hour", 60),
        Pair("3 Hours", 180),
        Pair("8 Hours", 480),
        Pair("24 Hours", 1440)
    )

    var selectedDurationMinutes by remember { mutableIntStateOf(60) }
    var intentionText by remember { mutableStateOf("") }
    var dndEnabled by remember { mutableStateOf(true) }
    var allowedPackagesText by remember { mutableStateOf("com.google.android.dialer,com.google.android.apps.messaging,com.google.android.apps.maps") }

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Digital Detox"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (activeDetox == null) {
                // Configure & Start Detox
                Text(
                    text = "Step away from digital noise and reclaim your stillness.",
                    style = typography.caption,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(spacing.l))

                // Duration Selector
                Text(
                    text = "CHOOSE DURATION",
                    style = typography.caption,
                    color = colors.textTertiary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    durations.forEach { (label, minutes) ->
                        val isSelected = selectedDurationMinutes == minutes
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) colors.goldSeam else colors.border,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(if (isSelected) colors.goldSeam.copy(alpha = 0.15f) else colors.surfaceElevated)
                                .clickable { selectedDurationMinutes = minutes }
                                .padding(vertical = spacing.m),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = typography.caption.copy(
                                    color = if (isSelected) colors.goldSeam else colors.textPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.m))

                // Intention Input
                OutlinedTextField(
                    value = intentionText,
                    onValueChange = { intentionText = it },
                    label = { Text("Personal Intention (e.g. Reading, Walking, Rest)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.goldSeam,
                        unfocusedBorderColor = colors.border
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing.m))

                // DND Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Silence Notifications (DND)",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Silences non-emergency pings during the detox.",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                    Switch(
                        checked = dndEnabled,
                        onCheckedChange = { dndEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.goldSeam,
                            checkedTrackColor = colors.goldSeam.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xl))

                KintsuButton(
                    text = "Begin Mindful Detox",
                    onClick = {
                        onStartDetox(
                            selectedDurationMinutes,
                            intentionText.ifBlank { "Reclaiming stillness" },
                            allowedPackagesText,
                            dndEnabled
                        )
                    },
                    variant = KintsuButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Active Detox Running Screen
                val now = System.currentTimeMillis()
                val remainingMillis = (activeDetox.endTimeMillis - now).coerceAtLeast(0L)
                val remHours = remainingMillis / (60 * 60 * 1000L)
                val remMinutes = (remainingMillis % (60 * 60 * 1000L)) / (60 * 1000L)
                val remSeconds = (remainingMillis % (60 * 1000L)) / 1000L

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(colors.accentSoft.copy(alpha = 0.35f))
                        .border(1.5.dp, colors.goldSeam, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%02d:%02d:%02d", remHours, remMinutes, remSeconds),
                            style = typography.screenTitle.copy(fontSize = 24.sp),
                            color = colors.goldSeam
                        )
                        Text(
                            text = "Time Reclaimed",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))

                Text(
                    text = "Detox in Progress",
                    style = typography.itemTitle,
                    color = colors.textPrimary
                )

                if (!activeDetox.intention.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Text(
                        text = "\"${activeDetox.intention}\"",
                        style = typography.body,
                        color = colors.accent,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(spacing.m))

                Text(
                    text = "Distracting apps are shielded. Essentials remain open.",
                    style = typography.caption,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(spacing.xl))

                KintsuButton(
                    text = "End Detox Gracefully",
                    onClick = onEndDetoxEarly,
                    variant = KintsuButtonVariant.SECONDARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
