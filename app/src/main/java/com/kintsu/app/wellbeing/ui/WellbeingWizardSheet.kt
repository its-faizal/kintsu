package com.kintsu.app.wellbeing.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kintsu.app.ui.components.KintsuBottomSheet
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme

data class WizardStep(
    val title: String,
    val description: String,
    val permissionType: String,
    val isOptional: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellbeingWizardSheet(
    onDismiss: () -> Unit,
    onCompleted: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val context = LocalContext.current

    val steps = remember {
        listOf(
            WizardStep(
                title = "1. Accessibility Service",
                description = "Enables real-time, zero-battery detection of distracting apps and displays the mindful breathing pause.",
                permissionType = "ACCESSIBILITY"
            ),
            WizardStep(
                title = "2. Usage Statistics Access",
                description = "Calculates your daily screen time, phone unlocks, and app usage breakdown privately on your device.",
                permissionType = "USAGE_STATS"
            ),
            WizardStep(
                title = "3. Display Over Other Apps",
                description = "Allows the calm block shield and reflection prompts to float seamlessly over restricted applications.",
                permissionType = "OVERLAY"
            ),
            WizardStep(
                title = "4. Do Not Disturb Access",
                description = "Silences incoming pings during Digital Detox, Bedtime, and deep Focus sessions.",
                permissionType = "DND",
                isOptional = true
            ),
            WizardStep(
                title = "5. Unrestricted Battery",
                description = "Prevents aggressive phone battery savers from freezing your scheduled limits and reminders.",
                permissionType = "BATTERY_OPT",
                isOptional = true
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]

    KintsuBottomSheet(
        onDismissRequest = onDismiss,
        title = "Wellbeing Setup"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == currentStepIndex) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == currentStepIndex) colors.goldSeam
                                else if (idx < currentStepIndex) colors.accent
                                else colors.border
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            Text(
                text = step.title,
                style = typography.itemTitle,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.s))

            Text(
                text = step.description,
                style = typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.xxl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                if (step.isOptional || currentStepIndex > 0) {
                    KintsuButton(
                        text = if (step.isOptional) "Skip" else "Back",
                        onClick = {
                            if (step.isOptional && currentStepIndex < steps.size - 1) {
                                currentStepIndex++
                            } else if (currentStepIndex > 0 && !step.isOptional) {
                                currentStepIndex--
                            } else {
                                onCompleted()
                            }
                        },
                        variant = KintsuButtonVariant.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                }

                KintsuButton(
                    text = "Grant Permission",
                    onClick = {
                        launchPermissionSettings(context, step.permissionType)
                        if (currentStepIndex < steps.size - 1) {
                            currentStepIndex++
                        } else {
                            onCompleted()
                        }
                    },
                    variant = KintsuButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun launchPermissionSettings(context: Context, type: String) {
    try {
        when (type) {
            "ACCESSIBILITY" -> {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            "USAGE_STATS" -> {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            "OVERLAY" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            }
            "DND" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                }
            }
            "BATTERY_OPT" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            }
        }
    } catch (_: Exception) {
        // Fallback to application details
        val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(appDetails)
    }
}
