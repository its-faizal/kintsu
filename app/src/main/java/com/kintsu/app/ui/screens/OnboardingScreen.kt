package com.kintsu.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.IconGoldSeam
import com.kintsu.app.ui.components.IconHabit
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.theme.AccentOption
import com.kintsu.app.ui.theme.KintsuTheme

@Composable
fun OnboardingScreen(
    onComplete: (displayName: String, accent: AccentOption) -> Unit,
    onNotificationsPermissionResult: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val context = LocalContext.current

    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var displayName by rememberSaveable { mutableStateOf("Seeker") }
    var selectedAccent by rememberSaveable { mutableStateOf(AccentOption.GOLD) }

    var notificationPermissionRequested by rememberSaveable { mutableStateOf(false) }
    var notificationPermissionGranted by rememberSaveable {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionRequested = true
        notificationPermissionGranted = isGranted
        onNotificationsPermissionResult(isGranted)
    }

    // Predictive back / back gesture: returns to previous onboarding step
    BackHandler(enabled = currentStep > 0) {
        currentStep--
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 640.dp)
                .padding(horizontal = spacing.xl, vertical = spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Step Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                modifier = Modifier.padding(top = spacing.s)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(width = if (index == currentStep) 28.dp else 8.dp, height = 6.dp)
                            .clip(KintsuTheme.shapes.full)
                            .background(if (index == currentStep) colors.accent else colors.border)
                    )
                }
            }

            // Scrollable Content Area for Accessibility / Font Scaling up to 200%
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = spacing.m),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboardingSlide"
                ) { step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (step) {
                            0 -> {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(colors.accentSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconGoldSeam(color = colors.goldSeam, size = 48.dp)
                                }

                                Spacer(modifier = Modifier.height(spacing.xl))

                                Text(
                                    text = "The Art of Kintsugi",
                                    style = typography.screenTitle,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(spacing.m))

                                Text(
                                    text = "In Japanese pottery, broken ceramic is mended with lacquer dusted with gold. The mended seams do not hide the break—they illuminate it.",
                                    style = typography.body,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = spacing.m)
                                )
                            }

                            1 -> {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(colors.accentSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconHabit(color = colors.accent, size = 48.dp)
                                }

                                Spacer(modifier = Modifier.height(spacing.xl))

                                Text(
                                    text = "No Failure. Only Gold.",
                                    style = typography.screenTitle,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(spacing.m))

                                Text(
                                    text = "A missed day is never a failure or a reset to zero. It is simply a seam waiting to be mended with mindful focus within 48 hours. Kintsu will never shame you.",
                                    style = typography.body,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = spacing.m)
                                )
                            }

                            2 -> {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(colors.accentSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconClock(color = colors.accent, size = 48.dp)
                                }

                                Spacer(modifier = Modifier.height(spacing.xl))

                                Text(
                                    text = "Mindful Reminders",
                                    style = typography.screenTitle,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(spacing.m))

                                Text(
                                    text = "Kintsu can send gentle, quiet reminders when a ritual is scheduled, and notify you when a crack is eligible for gold repair. Skipped if already done.",
                                    style = typography.body,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = spacing.m)
                                )

                                Spacer(modifier = Modifier.height(spacing.l))

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (notificationPermissionGranted) {
                                        Text(
                                            text = "✓ Notifications Enabled",
                                            style = typography.sectionHeader,
                                            color = colors.accent
                                        )
                                    } else {
                                        KintsuButton(
                                            text = if (notificationPermissionRequested) "Permission Denied (Quiet Mode)" else "Enable Gentle Reminders",
                                            onClick = {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            },
                                            variant = if (notificationPermissionRequested) KintsuButtonVariant.SECONDARY else KintsuButtonVariant.ACCENT_SOFT,
                                            modifier = Modifier.fillMaxWidth(0.85f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(spacing.xs))
                                    Text(
                                        text = "You can adjust reminders at any time in Settings.",
                                        style = typography.caption,
                                        color = colors.textTertiary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            3 -> {
                                Text(
                                    text = "Welcome to Kintsu",
                                    style = typography.screenTitle,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(spacing.s))

                                Text(
                                    text = "How would you like to be greeted?",
                                    style = typography.body,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(spacing.l))

                                OutlinedTextField(
                                    value = displayName,
                                    onValueChange = { displayName = it },
                                    placeholder = { Text("Your name", color = colors.textTertiary) },
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    shape = KintsuTheme.shapes.medium,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.accent,
                                        unfocusedBorderColor = colors.border
                                    )
                                )

                                Spacer(modifier = Modifier.height(spacing.l))

                                Text(
                                    text = "Choose your essence color",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )

                                Spacer(modifier = Modifier.height(spacing.s))

                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
                                    AccentOption.values().forEach { accent ->
                                        val isSelected = selectedAccent == accent
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(accent.color)
                                                .clickable { selectedAccent = accent }
                                                .then(
                                                    if (isSelected) Modifier.border(2.5.dp, colors.textPrimary, CircleShape) else Modifier
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    KintsuButton(
                        text = "Back",
                        onClick = { currentStep-- },
                        variant = KintsuButtonVariant.TEXT
                    )
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                KintsuButton(
                    text = if (currentStep == 3) "Enter Sanctuary" else "Continue",
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            onComplete(displayName.ifBlank { "Seeker" }, selectedAccent)
                        }
                    },
                    variant = KintsuButtonVariant.PRIMARY
                )
            }
        }
    }
}
