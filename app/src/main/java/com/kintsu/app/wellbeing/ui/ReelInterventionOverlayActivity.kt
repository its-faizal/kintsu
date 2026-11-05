package com.kintsu.app.wellbeing.ui

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.MainActivity
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.data.model.DistractionInterventionLogEntity
import com.kintsu.app.ui.components.IconBook
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.IconLock
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.theme.KintsuTheme
import com.kintsu.app.wellbeing.service.KintsuAccessibilityBlockerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReelInterventionOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PLATFORM = "extra_platform"
        const val EXTRA_PACKAGE = "extra_package"
        private val temporaryUnlockTimestamps = mutableMapOf<String, Long>()
        private val recentPassExpiredTimestamps = mutableMapOf<String, Long>()
        private var emergencyPassDateEpochDay = 0L
        private var emergencyPassCountToday = 0

        fun getRemainingEmergencyPasses(): Int {
            val todayEpoch = java.time.LocalDate.now().toEpochDay()
            if (emergencyPassDateEpochDay != todayEpoch) {
                emergencyPassDateEpochDay = todayEpoch
                emergencyPassCountToday = 0
            }
            return (2 - emergencyPassCountToday).coerceAtLeast(0)
        }

        fun consumeEmergencyPass(pkgName: String): Boolean {
            if (getRemainingEmergencyPasses() <= 0) return false
            emergencyPassCountToday++
            grantTemporaryPass(pkgName, 5)
            return true
        }

        fun isTemporarilyUnlocked(pkgName: String): Boolean {
            val expireTime = temporaryUnlockTimestamps[pkgName] ?: return false
            if (System.currentTimeMillis() < expireTime) {
                return true
            }
            temporaryUnlockTimestamps.remove(pkgName)
            recentPassExpiredTimestamps[pkgName] = System.currentTimeMillis() + (15 * 60 * 1000L)
            return false
        }

        fun hasPassExpiredRecently(pkgName: String): Boolean {
            val expireWindow = recentPassExpiredTimestamps[pkgName] ?: return false
            if (System.currentTimeMillis() < expireWindow) {
                recentPassExpiredTimestamps.remove(pkgName)
                return true
            }
            recentPassExpiredTimestamps.remove(pkgName)
            return false
        }

        fun grantTemporaryPass(pkgName: String, minutes: Int) {
            temporaryUnlockTimestamps[pkgName] = System.currentTimeMillis() + (minutes * 60 * 1000L)
        }

        fun getRemainingTemporaryPassMillis(pkgName: String): Long {
            val expireTime = temporaryUnlockTimestamps[pkgName] ?: return 0L
            val remaining = expireTime - System.currentTimeMillis()
            return remaining.coerceAtLeast(0L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enforceFullscreenLock()

        val platform = intent.getStringExtra(EXTRA_PLATFORM) ?: "Short-Form Feed"
        val pkgName = intent.getStringExtra(EXTRA_PACKAGE) ?: ""

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
                    stepAwayAndClose()
                }

                ReelOverlayContent(
                    platform = platform,
                    packageName = pkgName,
                    onCloseReels = { stepAwayAndClose() },
                    onStartBreathing = {
                        logIntervention(pkgName, "BREATHING_PAUSE")
                    },
                    onAllowFiveMinutes = {
                        if (consumeEmergencyPass(pkgName)) {
                            logIntervention(pkgName, "5_MIN_PASS")
                            finish()
                        }
                    },
                    onLockForToday = {
                        grantTemporaryPass(pkgName, 0)
                        logIntervention(pkgName, "LOCKED_FOR_TODAY")
                        stepAwayAndClose()
                    },
                    onReflectInJournal = {
                        logIntervention(pkgName, "JOURNAL_REFLECTION")
                        openJournalReflection()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enforceFullscreenLock()
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        if (isInMultiWindowMode) {
            escapeToHomeAndClose()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            escapeToHomeAndClose()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            // Lost focus means user clicked outside overlay into the underlying app
            stepAwayAndClose()
        }
    }

    private fun enforceFullscreenLock() {
        window.setLayout(
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.MATCH_PARENT
        )
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

        if (isInMultiWindowMode || isInPictureInPictureMode) {
            escapeToHomeAndClose()
            return
        }

        val displayMetrics = resources.displayMetrics
        val decorView = window.decorView
        val visibleRect = android.graphics.Rect()
        decorView.getGlobalVisibleRect(visibleRect)
        if (visibleRect.width() > 0 && visibleRect.height() > 0) {
            val isWindowShrunk = visibleRect.width() < displayMetrics.widthPixels * 0.82f ||
                                 visibleRect.height() < displayMetrics.heightPixels * 0.82f
            if (isWindowShrunk) {
                escapeToHomeAndClose()
            }
        }
    }

    private fun escapeToHomeAndClose() {
        com.kintsu.app.wellbeing.service.KintsuAccessibilityBlockerService.hideFloatingPill()
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(homeIntent)
        finishAffinity()
    }

    private fun stepAwayAndClose() {
        com.kintsu.app.wellbeing.service.KintsuAccessibilityBlockerService.hideFloatingPill()
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(homeIntent)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = com.kintsu.app.data.KintsuDatabase.getDatabase(applicationContext)
                val settings = com.kintsu.app.data.SettingsDataStore(applicationContext)
                val repo = com.kintsu.app.data.KintsuRepository(db, settings)
                repo.awardCoins(
                    amount = 1,
                    reason = "Conscious choice: Resisted short-form feed",
                    sourceActivity = "REEL_RESIST",
                    sourceEventId = "reel_resist_${System.currentTimeMillis()}"
                )
            } catch (_: Exception) {}
        }
        finish()
    }

    private fun openJournalReflection() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_SECTION", "JOURNAL")
            putExtra("EXTRA_JOURNAL_PROMPT", "Mindful Pause: Noticed the urge to scroll short-form videos. What feeling or task was I pausing from?")
        }
        startActivity(intent)
        finish()
    }

    private fun logIntervention(pkgName: String, action: String) {
        val db = KintsuDatabase.getDatabase(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.wellbeingDao().insertIntervention(
                    DistractionInterventionLogEntity(
                        timestampMillis = System.currentTimeMillis(),
                        packageName = pkgName,
                        interventionStyle = "REEL_OVERLAY",
                        userAction = action
                    )
                )
            } catch (_: Exception) {}
        }
    }
}

@Composable
fun ReelOverlayContent(
    platform: String,
    packageName: String,
    onCloseReels: () -> Unit,
    onStartBreathing: () -> Unit,
    onAllowFiveMinutes: () -> Unit,
    onLockForToday: () -> Unit,
    onReflectInJournal: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography

    var isBreathingActive by remember { mutableStateOf(false) }
    var breathingPhase by remember { mutableStateOf("Inhale") }
    var breathingSecondsLeft by remember { mutableIntStateOf(60) }
    var showEmergencyFrictionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(isBreathingActive) {
        if (isBreathingActive) {
            onStartBreathing()
            while (breathingSecondsLeft > 0 && isBreathingActive) {
                breathingPhase = if ((breathingSecondsLeft % 8) >= 4) "Inhale" else "Exhale"
                delay(1000L)
                breathingSecondsLeft--
            }
            if (breathingSecondsLeft <= 0) {
                isBreathingActive = false
                onCloseReels()
            }
        }
    }

    // Outer translucent backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(spacing.l),
        contentAlignment = Alignment.Center
    ) {
        if (showEmergencyFrictionSheet) {
            EmergencyFrictionSheet(
                onDismiss = { showEmergencyFrictionSheet = false },
                onSuccess = {
                    showEmergencyFrictionSheet = false
                    onAllowFiveMinutes()
                }
            )
        } else {
            // Main overlay card (Spacious, prominent, not tiny pills!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surfaceElevated)
                    .border(1.2.dp, colors.goldSeam.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(spacing.l)
            ) {
                if (isBreathingActive) {
                // Interactive Breathing Exercise inside overlay
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    Text(
                        text = "Conscious Breathing",
                        style = typography.itemTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "$breathingPhase ($breathingSecondsLeft s remaining)",
                        style = typography.caption,
                        color = colors.goldSeam
                    )

                    val infiniteTransition = rememberInfiniteTransition(label = "breath")
                    val breathScale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(4000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "breathScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(breathScale)
                            .clip(CircleShape)
                            .background(colors.goldSeamSoft)
                            .border(2.dp, colors.goldSeam, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = breathingPhase,
                            style = typography.caption.copy(fontWeight = FontWeight.Bold),
                            color = colors.goldSeam
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.s))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .clickable { isBreathingActive = false }
                            .padding(vertical = 12.dp)
                            .semantics { role = Role.Button },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel Breathing",
                            style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                // Standard Reel Options Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.goldSeamSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            IconShield(size = 22.dp, color = colors.goldSeam)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Conscious Pause",
                                style = typography.itemTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "$platform reel detected · Choose mindfully",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Option 1: Step Away / Close Reels (Hero Primary Button)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.accent)
                            .clickable(onClick = onCloseReels)
                            .padding(vertical = 14.dp, horizontal = 16.dp)
                            .semantics { role = Role.Button },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconClose(size = 18.dp, color = Color.Black)
                            Text(
                                text = "Close Reels & Step Away",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // Option 2: 1-Minute Mindful Breath
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable { isBreathingActive = true }
                            .padding(vertical = 14.dp, horizontal = 16.dp)
                            .semantics { role = Role.Button }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(colors.goldSeamSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                IconClock(size = 16.dp, color = colors.goldSeam)
                            }
                            Column {
                                Text(
                                    text = "Take 1-Minute Mindful Breath",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Ground yourself before deciding to continue",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    // Option 3: Allow 5 Minutes Conscious Watch (Emergency 5 Mins x 2 Max)
                    val remainingPasses = ReelInterventionOverlayActivity.getRemainingEmergencyPasses()
                    val canUsePass = remainingPasses > 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (canUsePass) colors.surface else colors.surface.copy(alpha = 0.4f))
                            .border(1.dp, if (canUsePass) colors.border.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable(enabled = canUsePass) { showEmergencyFrictionSheet = true }
                            .padding(vertical = 14.dp, horizontal = 16.dp)
                            .semantics { role = Role.Button }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (canUsePass) colors.goldSeamSoft else colors.border),
                                contentAlignment = Alignment.Center
                            ) {
                                IconClock(size = 16.dp, color = if (canUsePass) colors.goldSeam else colors.textTertiary)
                            }
                            Column {
                                Text(
                                    text = if (canUsePass) "Emergency 5-Min Pass ($remainingPasses of 2 Left)" else "Emergency Passes Exhausted (0 of 2 Left)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (canUsePass) colors.textPrimary else colors.textTertiary
                                )
                                Text(
                                    text = if (canUsePass) "Mindful 5m pass. Strictly limited to 2x per day." else "Daily quota of 2 emergency passes reached.",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    // Option 4: Lock Shorts for Rest of Today
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable(onClick = onLockForToday)
                            .padding(vertical = 14.dp, horizontal = 16.dp)
                            .semantics { role = Role.Button }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(colors.error.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconLock(size = 16.dp, color = colors.error)
                            }
                            Column {
                                Text(
                                    text = "Lock Shorts for Rest of Today",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Zero scrolls until tomorrow morning",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    // Option 5: Reflect in Kintsu Journal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable(onClick = onReflectInJournal)
                            .padding(vertical = 14.dp, horizontal = 16.dp)
                            .semantics { role = Role.Button }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(colors.goldSeamSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                IconBook(size = 16.dp, color = colors.goldSeam)
                            }
                            Column {
                                Text(
                                    text = "Reflect in Kintsu Journal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Capture why you felt the urge to browse",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

}
