package com.kintsu.app.wellbeing.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.service.KintsuAccessibilityBlockerService
import kotlinx.coroutines.delay

class BlockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_RULE_MODE = "extra_rule_mode"
        const val EXTRA_STRICTNESS = "extra_strictness"
        const val EXTRA_PROMPT = "extra_prompt"
        const val EXTRA_PAUSE_SECONDS = "extra_pause_seconds"
        const val CONFIRMATION_PHRASE = "I choose to continue"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val ruleMode = intent.getStringExtra(EXTRA_RULE_MODE) ?: "DAILY_LIMIT"
        val strictness = intent.getStringExtra(EXTRA_STRICTNESS) ?: "GENTLE"
        val prompt = intent.getStringExtra(EXTRA_PROMPT) ?: "What are you here for?"
        val pauseSeconds = intent.getIntExtra(EXTRA_PAUSE_SECONDS, 10).coerceAtLeast(3)

        val settingsStore = com.kintsu.app.data.SettingsDataStore(applicationContext)

        setContent {
            val settings by settingsStore.settingsFlow.collectAsState(initial = com.kintsu.app.data.KintsuSettings())
            KintsuTheme(
                appTheme = settings.appTheme,
                themeMode = settings.themeMode,
                accentOption = settings.accentOption,
                contentDensity = settings.contentDensity,
                reducedMotion = settings.reducedMotion
            ) {
                BackHandler {
                    returnToHomeScreen()
                }

                BlockShieldContent(
                    targetPackage = targetPackage,
                    ruleMode = ruleMode,
                    strictness = strictness,
                    prompt = prompt,
                    pauseSeconds = pauseSeconds,
                    onStepBack = { returnToHomeScreen() },
                    onContinue = {
                        KintsuAccessibilityBlockerService.unlockPackageTemporarily(targetPackage, 15)
                        finish()
                    }
                )
            }
        }
    }

    private fun returnToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

@Composable
fun BlockShieldContent(
    targetPackage: String,
    ruleMode: String,
    strictness: String,
    prompt: String,
    pauseSeconds: Int,
    onStepBack: () -> Unit,
    onContinue: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var secondsRemaining by remember { mutableIntStateOf(pauseSeconds) }
    var pauseFinished by remember { mutableStateOf(false) }

    var showBalancedReasonDialog by remember { mutableStateOf(false) }
    var showEmergencyExitFlow by remember { mutableStateOf(false) }
    var emergencyCountdown by remember { mutableIntStateOf(60) }
    var confirmationInput by remember { mutableStateOf("") }

    // Breathing timer
    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
        pauseFinished = true
    }

    // Emergency timer
    LaunchedEffect(showEmergencyExitFlow) {
        if (showEmergencyExitFlow) {
            emergencyCountdown = 60
            while (emergencyCountdown > 0) {
                delay(1000)
                emergencyCountdown--
            }
        }
    }

    val motion = KintsuTheme.motion
    val isReducedMotion = motion.fastMs == 0

    // Breathing pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val animatedBreathScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    val breathScale = if (isReducedMotion) 1.0f else animatedBreathScale

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Calm Breathing Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(breathScale)
                    .clip(CircleShape)
                    .background(colors.accentSoft.copy(alpha = 0.35f))
                    .border(1.5.dp, colors.goldSeam.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (!pauseFinished) "${secondsRemaining}s" else "Breathe",
                        style = typography.screenTitle.copy(fontSize = 28.sp),
                        color = colors.goldSeam
                    )
                    Text(
                        text = if (!pauseFinished) "Inhale & Exhale" else "Mindful Moment",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            // Calm question copy
            Text(
                text = "Take a breath",
                style = typography.screenTitle,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.s))

            Text(
                text = prompt,
                style = typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.xxl))

            // Actions based on strictness
            if (!showEmergencyExitFlow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    KintsuButton(
                        text = "Step Back",
                        onClick = onStepBack,
                        variant = KintsuButtonVariant.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )

                    if (strictness == "GENTLE") {
                        KintsuButton(
                            text = if (pauseFinished) "Continue" else "Wait (${secondsRemaining}s)",
                            onClick = onContinue,
                            enabled = pauseFinished,
                            variant = KintsuButtonVariant.ACCENT_SOFT,
                            modifier = Modifier.weight(1f)
                        )
                    } else if (strictness == "BALANCED") {
                        KintsuButton(
                            text = "Choose Reason",
                            onClick = { showBalancedReasonDialog = true },
                            enabled = pauseFinished,
                            variant = KintsuButtonVariant.ACCENT_SOFT,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (strictness == "STRICT") {
                    Spacer(modifier = Modifier.height(spacing.l))
                    Text(
                        text = "Commitment Active • App restricted until window ends",
                        style = typography.caption,
                        color = colors.textTertiary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(spacing.m))
                    Text(
                        text = "Emergency Exit",
                        style = typography.caption.copy(color = colors.goldSeam),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showEmergencyExitFlow = true }
                            .padding(horizontal = spacing.s, vertical = spacing.xs)
                    )
                }
            } else {
                // Strict Emergency Exit Slow Flow
                KintsuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surfaceElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.l),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Emergency Exit",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "A 60-second pause to prevent impulse unlocks.",
                            style = typography.caption,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        if (emergencyCountdown > 0) {
                            Text(
                                text = "${emergencyCountdown}s remaining",
                                style = typography.itemTitle,
                                color = colors.goldSeam
                            )
                        } else {
                            Text(
                                text = "Type \"${BlockActivity.CONFIRMATION_PHRASE}\" to unlock for 15m:",
                                style = typography.caption,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(spacing.s))
                            OutlinedTextField(
                                value = confirmationInput,
                                onValueChange = { confirmationInput = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.goldSeam,
                                    unfocusedBorderColor = colors.border
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.m))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            KintsuButton(
                                text = "Cancel",
                                onClick = { showEmergencyExitFlow = false },
                                variant = KintsuButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                            KintsuButton(
                                text = "Unlock (15m)",
                                onClick = onContinue,
                                enabled = emergencyCountdown == 0 &&
                                        confirmationInput.trim().equals(BlockActivity.CONFIRMATION_PHRASE, ignoreCase = true),
                                variant = KintsuButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Balanced Mode Reason Selector
        if (showBalancedReasonDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showBalancedReasonDialog = false },
                contentAlignment = Alignment.Center
            ) {
                KintsuCard(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clickable(enabled = false) {},
                    backgroundColor = colors.surfaceElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.l)
                    ) {
                        Text(
                            text = "Mindful Intention",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "Choose your purpose for opening this app:",
                            style = typography.caption,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        val reasons = listOf(
                            "Work or essential task",
                            "Responding to important message",
                            "Intentional check (15m limit)"
                        )

                        reasons.forEach { reason ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        showBalancedReasonDialog = false
                                        onContinue()
                                    }
                                    .padding(vertical = spacing.s)
                            ) {
                                Text(
                                    text = "• $reason",
                                    style = typography.body,
                                    color = colors.accent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.m))

                        KintsuButton(
                            text = "Cancel",
                            onClick = { showBalancedReasonDialog = false },
                            variant = KintsuButtonVariant.SECONDARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
