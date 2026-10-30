package com.kintsu.app.wellbeing.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.MainActivity
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.data.model.DistractionInterventionLogEntity
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.IconSparkle
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.service.KintsuAccessibilityBlockerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BlockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_RULE_MODE = "extra_rule_mode"
        const val EXTRA_STRICTNESS = "extra_strictness"
        const val EXTRA_PROMPT = "extra_prompt"
        const val EXTRA_PAUSE_SECONDS = "extra_pause_seconds"
        const val EXTRA_INTERVENTION_STYLE = "extra_intervention_style"
        const val CONFIRMATION_PHRASE = "I choose to continue"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val ruleMode = intent.getStringExtra(EXTRA_RULE_MODE) ?: "DAILY_LIMIT"
        val strictness = intent.getStringExtra(EXTRA_STRICTNESS) ?: "GENTLE"
        val prompt = intent.getStringExtra(EXTRA_PROMPT) ?: "What are you here for?"
        val pauseSeconds = intent.getIntExtra(EXTRA_PAUSE_SECONDS, 10).coerceAtLeast(3)
        val style = intent.getStringExtra(EXTRA_INTERVENTION_STYLE) ?: "BREATHING"

        val settingsStore = SettingsDataStore(applicationContext)

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
                    logActionAndExit(targetPackage, style, "RETURNED_TO_FOCUS") {
                        returnToHomeScreen()
                    }
                }

                BlockShieldContent(
                    targetPackage = targetPackage,
                    ruleMode = ruleMode,
                    strictness = strictness,
                    prompt = prompt,
                    pauseSeconds = pauseSeconds,
                    style = style,
                    onReturnToFocus = {
                        logActionAndExit(targetPackage, style, "RETURNED_TO_FOCUS") {
                            returnToHomeScreen()
                        }
                    },
                    onContinueToApp = { reason ->
                        logActionAndExit(targetPackage, style, "CONTINUED_TO_APP", reason) {
                            val interval = settings.reinterventionIntervalMinutes.coerceAtLeast(5)
                            KintsuAccessibilityBlockerService.unlockPackageTemporarily(targetPackage, interval)
                            finish()
                        }
                    },
                    onHealthyAlternativeChosen = { altKey ->
                        logActionAndExit(targetPackage, style, "HEALTHY_ALTERNATIVE", alternative = altKey) {
                            openKintsuSection(altKey)
                        }
                    }
                )
            }
        }
    }

    private fun logActionAndExit(
        targetPackage: String,
        style: String,
        action: String,
        reason: String? = null,
        alternative: String? = null,
        onDone: () -> Unit
    ) {
        val db = KintsuDatabase.getDatabase(applicationContext)
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                db.wellbeingDao().insertIntervention(
                    DistractionInterventionLogEntity(
                        packageName = targetPackage,
                        timestampMillis = System.currentTimeMillis(),
                        interventionStyle = style,
                        userAction = action,
                        choiceReason = reason,
                        alternativeChosen = alternative
                    )
                )
            } catch (_: Exception) {}
            launch(Dispatchers.Main) {
                onDone()
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

    private fun openKintsuSection(sectionKey: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_SECTION", sectionKey)
        }
        startActivity(intent)
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
    style: String,
    onReturnToFocus: () -> Unit,
    onContinueToApp: (String?) -> Unit,
    onHealthyAlternativeChosen: (String) -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var secondsRemaining by remember { mutableIntStateOf(pauseSeconds) }
    var pauseFinished by remember { mutableStateOf(false) }

    var selectedChoiceReason by remember { mutableStateOf<String?>(null) }
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
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    val breathScale = if (isReducedMotion) 1.0f else animatedBreathScale

    // Inhale / Exhale phase text
    val breathPhaseText = remember(animatedBreathScale, pauseFinished) {
        if (pauseFinished) "Continue when ready"
        else if (animatedBreathScale > 1.02f) "Breathe out..."
        else "Breathe in..."
    }

    val progressAnim by animateFloatAsState(
        targetValue = if (pauseSeconds > 0) 1f - (secondsRemaining.toFloat() / pauseSeconds.toFloat()) else 1f,
        animationSpec = tween(500, easing = LinearEasing),
        label = "progressAnim"
    )

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
            // Calm Header
            Text(
                text = "Take a moment.",
                style = typography.screenTitle.copy(fontSize = 30.sp, fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            Text(
                text = "Do you want to spend time here right now?",
                style = typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // Style Variants
            when (style) {
                "INTENTIONAL_CHOICE" -> {
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
                                text = "What are you opening this app for?",
                                style = typography.itemTitle,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(spacing.m))

                            val choices = listOf(
                                "Something important",
                                "Taking a short break",
                                "Just checking",
                                "Not sure"
                            )
                            choices.forEach { choice ->
                                val isSelected = selectedChoiceReason == choice
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) colors.goldSeamSoft else colors.surface)
                                        .border(
                                            1.dp,
                                            if (isSelected) colors.goldSeam else colors.border,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedChoiceReason = choice }
                                        .padding(horizontal = spacing.m, vertical = spacing.s)
                                ) {
                                    Text(
                                        text = choice,
                                        style = typography.body,
                                        color = if (isSelected) colors.goldSeam else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
                "GENTLE_DELAY" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.l),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { progressAnim },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colors.goldSeam,
                            trackColor = colors.surfaceElevated
                        )
                        Spacer(modifier = Modifier.height(spacing.m))
                        Text(
                            text = if (!pauseFinished) "${secondsRemaining}s" else "Pause complete",
                            style = typography.itemTitle,
                            color = colors.goldSeam
                        )
                    }
                }
                "REFLECTION" -> {
                    KintsuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surfaceElevated
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.xl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "“Is this how you want to spend the next few minutes?”",
                                style = typography.body.copy(fontSize = 18.sp, lineHeight = 26.sp),
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            if (!pauseFinished) {
                                Spacer(modifier = Modifier.height(spacing.m))
                                Text(
                                    text = "Reflecting: ${secondsRemaining}s",
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Default: Animated Breathing Circle
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(breathScale)
                            .clip(CircleShape)
                            .background(colors.accentSoft.copy(alpha = 0.35f))
                            .border(1.5.dp, colors.goldSeam.copy(alpha = 0.65f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (!pauseFinished) "${secondsRemaining}s" else "Center",
                                style = typography.screenTitle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                                color = colors.goldSeam
                            )
                            Text(
                                text = breathPhaseText,
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Action section after intervention
            if (pauseFinished) {
                Text(
                    text = "Still want to continue?",
                    style = typography.itemTitle,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(spacing.m))
            }

            if (!showEmergencyExitFlow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    KintsuButton(
                        text = "Return to Focus",
                        onClick = onReturnToFocus,
                        variant = KintsuButtonVariant.PRIMARY,
                        modifier = Modifier.weight(1.1f)
                    )

                    if (strictness == "GENTLE" || strictness == "FLEXIBLE") {
                        KintsuButton(
                            text = if (pauseFinished) "Continue to App" else "Wait (${secondsRemaining}s)",
                            onClick = { onContinueToApp(selectedChoiceReason) },
                            enabled = pauseFinished,
                            variant = KintsuButtonVariant.SECONDARY,
                            modifier = Modifier.weight(0.9f)
                        )
                    } else if (strictness == "BALANCED" || strictness == "COMMITTED") {
                        KintsuButton(
                            text = if (pauseFinished) "Continue" else "Wait (${secondsRemaining}s)",
                            onClick = { onContinueToApp(selectedChoiceReason) },
                            enabled = pauseFinished,
                            variant = KintsuButtonVariant.SECONDARY,
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }

                if (strictness == "STRICT" || strictness == "DEEP_COMMITMENT") {
                    Spacer(modifier = Modifier.height(spacing.l))
                    Text(
                        text = "Commitment Active • Restricted by your intentional rule",
                        style = typography.caption,
                        color = colors.textTertiary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(spacing.s))
                    Text(
                        text = "Emergency Override",
                        style = typography.caption.copy(color = colors.goldSeam),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showEmergencyExitFlow = true }
                            .padding(horizontal = spacing.s, vertical = spacing.xs)
                    )
                }
            } else {
                // Strict 60s intentional friction reflection flow
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
                            text = "Emergency Override",
                            style = typography.itemTitle,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "A 60-second reflection pause to ensure intentional choice.",
                            style = typography.caption,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(spacing.m))

                        if (emergencyCountdown > 0) {
                            Text(
                                text = "${emergencyCountdown}s reflection remaining",
                                style = typography.itemTitle,
                                color = colors.goldSeam
                            )
                        } else {
                            Text(
                                text = "Type \"${BlockActivity.CONFIRMATION_PHRASE}\" to proceed:",
                                style = typography.caption,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(spacing.s))

                            OutlinedTextField(
                                value = confirmationInput,
                                onValueChange = { confirmationInput = it },
                                singleLine = true,
                                textStyle = typography.body.copy(color = colors.textPrimary),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.goldSeam,
                                    unfocusedBorderColor = colors.border
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(spacing.m))

                            val matches = confirmationInput.trim().equals(BlockActivity.CONFIRMATION_PHRASE, ignoreCase = true)
                            KintsuButton(
                                text = "Confirm & Open for 10m",
                                onClick = { onContinueToApp("EMERGENCY_OVERRIDE") },
                                enabled = matches,
                                variant = KintsuButtonVariant.PRIMARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.s))
                        Text(
                            text = "Cancel Override",
                            style = typography.caption.copy(color = colors.textSecondary),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showEmergencyExitFlow = false }
                                .padding(spacing.xs)
                        )
                    }
                }
            }

            // Section 12: Healthy Alternatives
            Spacer(modifier = Modifier.height(spacing.xxl))

            Text(
                text = "OR CHOOSE A MINDFUL ALTERNATIVE",
                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(spacing.s))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                AlternativeChip("Resume Task", colors.surfaceElevated, colors.border, colors.textPrimary) {
                    onHealthyAlternativeChosen("TASKS")
                }
                AlternativeChip("Start Focus", colors.surfaceElevated, colors.border, colors.textPrimary) {
                    onHealthyAlternativeChosen("FOCUS")
                }
                AlternativeChip("Daily Intention", colors.surfaceElevated, colors.border, colors.textPrimary) {
                    onHealthyAlternativeChosen("INTENTION")
                }
                AlternativeChip("Habit Check-in", colors.surfaceElevated, colors.border, colors.textPrimary) {
                    onHealthyAlternativeChosen("HABITS")
                }
                AlternativeChip("Go Home", colors.surfaceElevated, colors.border, colors.textPrimary) {
                    onHealthyAlternativeChosen("HOME")
                }
            }
        }
    }
}

@Composable
private fun AlternativeChip(
    label: String,
    bg: Color,
    border: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
