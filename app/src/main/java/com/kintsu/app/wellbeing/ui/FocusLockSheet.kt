package com.kintsu.app.wellbeing.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.FocusLockSessionEntity
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClock
import com.kintsu.app.ui.components.IconShield
import com.kintsu.app.ui.components.IconSparkle
import com.kintsu.app.ui.components.KintsuButton
import com.kintsu.app.ui.components.KintsuButtonVariant
import com.kintsu.app.ui.components.KintsuCard
import com.kintsu.app.ui.components.KintsuDialog
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusLockSheet(
    activeSession: FocusLockSessionEntity?,
    appGroups: List<AppGroupEntity>,
    onStartSession: (title: String, durationMinutes: Int, restrictedGroupIds: String, websites: Boolean, strictness: String, breaks: Boolean) -> Unit,
    onEndSession: (session: FocusLockSessionEntity, wasEarly: Boolean) -> Unit,
    onTakeBreak: (session: FocusLockSessionEntity, minutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Setup state
    var selectedDuration by remember { mutableIntStateOf(25) }
    var sessionTitle by remember { mutableStateOf("Deep Focus") }
    var restrictWebsites by remember { mutableStateOf(true) }
    var allowBreaks by remember { mutableStateOf(true) }
    var selectedStrictness by remember { mutableStateOf("COMMITTED") } // FLEXIBLE, COMMITTED, DEEP_COMMITMENT

    // Active session countdown state
    var secondsRemaining by remember {
        mutableLongStateOf(
            if (activeSession != null) ((activeSession.endTimeMillis - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
            else 0L
        )
    }

    LaunchedEffect(activeSession) {
        while (activeSession != null && !activeSession.isCompleted) {
            val rem = ((activeSession.endTimeMillis - System.currentTimeMillis()) / 1000L)
            if (rem <= 0) {
                secondsRemaining = 0
                onEndSession(activeSession, false)
                break
            } else {
                secondsRemaining = rem
            }
            delay(1000)
        }
    }

    var showEarlyExitConfirmDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.92f)
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
            if (activeSession != null && !activeSession.isCompleted) {
                // --- ACTIVE FOCUS LOCK HUD ---
                val totalSec = activeSession.durationMinutes * 60L
                val progress = if (totalSec > 0) 1f - (secondsRemaining.toFloat() / totalSec.toFloat()) else 1f
                val minutesLeft = secondsRemaining / 60
                val secsLeft = secondsRemaining % 60

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Focus Lock Active",
                        style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = colors.goldSeam
                    )
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Text(
                        text = activeSession.title,
                        style = typography.screenTitle.copy(fontSize = 24.sp),
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(spacing.xl))

                    // Progress Ring
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = colors.goldSeam,
                            trackColor = colors.surfaceElevated,
                            strokeWidth = 10.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%02d:%02d", minutesLeft, secsLeft),
                                style = typography.screenTitle.copy(fontSize = 36.sp, fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Remaining",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.xl))

                    // Session Info Card
                    KintsuCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surfaceElevated
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.m),
                            verticalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Commitment Level", style = typography.caption, color = colors.textSecondary)
                                Text(activeSession.strictnessMode, style = typography.caption.copy(fontWeight = FontWeight.SemiBold), color = colors.goldSeam)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Website Filter", style = typography.caption, color = colors.textSecondary)
                                Text(if (activeSession.restrictedWebsitesEnabled) "Active" else "Off", style = typography.caption, color = colors.textPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Breaks Remaining", style = typography.caption, color = colors.textSecondary)
                                Text(
                                    "${(activeSession.maxBreaks - activeSession.breaksTaken).coerceAtLeast(0)} of ${activeSession.maxBreaks}",
                                    style = typography.caption,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.xl))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.m)
                    ) {
                        if (activeSession.breaksAllowed && activeSession.breaksTaken < activeSession.maxBreaks) {
                            KintsuButton(
                                text = "Take 5m Break",
                                onClick = { onTakeBreak(activeSession, 5) },
                                variant = KintsuButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        KintsuButton(
                            text = "End Session",
                            onClick = {
                                if (activeSession.strictnessMode == "FLEXIBLE") {
                                    onEndSession(activeSession, true)
                                } else {
                                    showEarlyExitConfirmDialog = true
                                }
                            },
                            variant = KintsuButtonVariant.DANGER,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // --- START NEW FOCUS LOCK SETUP ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Start Focus Lock",
                            style = typography.screenTitle.copy(fontSize = 24.sp),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Strict distraction shield for uninterrupted work",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))

                // Title
                OutlinedTextField(
                    value = sessionTitle,
                    onValueChange = { sessionTitle = it },
                    label = { Text("Session Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing.l))

                // Duration Presets
                Text(
                    text = "SESSION DURATION",
                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(spacing.xs))

                val presets = listOf(15, 25, 45, 60, 90)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    presets.forEach { mins ->
                        val isSel = selectedDuration == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) colors.goldSeamSoft else colors.surfaceElevated)
                                .border(1.dp, if (isSel) colors.goldSeam else colors.border, RoundedCornerShape(8.dp))
                                .clickable { selectedDuration = mins }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                fontSize = 13.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) colors.goldSeam else colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))

                // Strictness Commitment Mode
                Text(
                    text = "COMMITMENT STRICTNESS",
                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(spacing.xs))

                val strictnessModes = listOf(
                    "FLEXIBLE" to "Flexible" to "Can pause or end normally",
                    "COMMITTED" to "Committed" to "Requires conscious exit confirmation",
                    "DEEP_COMMITMENT" to "Deep Commitment" to "Adds 60s reflection delay before unlock"
                )

                strictnessModes.forEach { (pair, desc) ->
                    val (modeKey, modeTitle) = pair
                    val isSel = selectedStrictness == modeKey
                    KintsuCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedStrictness = modeKey },
                        backgroundColor = if (isSel) colors.goldSeamSoft else colors.surfaceElevated,
                        borderColor = if (isSel) colors.goldSeam else colors.border
                    ) {
                        Column(modifier = Modifier.padding(spacing.m)) {
                            Text(
                                text = modeTitle,
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

                // Toggles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { restrictWebsites = !restrictWebsites },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Restrict Distracting Websites", style = typography.body, color = colors.textPrimary)
                        Text("Applies domain filter to browser requests", style = typography.caption, color = colors.textSecondary)
                    }
                    Switch(
                        checked = restrictWebsites,
                        onCheckedChange = { restrictWebsites = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.goldSeam,
                            checkedTrackColor = colors.goldSeamSoft
                        )
                    )
                }

                Spacer(modifier = Modifier.height(spacing.s))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { allowBreaks = !allowBreaks },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow 2 Mindful Breaks (5m)", style = typography.body, color = colors.textPrimary)
                        Text("Enables intentional emergency access", style = typography.caption, color = colors.textSecondary)
                    }
                    Switch(
                        checked = allowBreaks,
                        onCheckedChange = { allowBreaks = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.goldSeam,
                            checkedTrackColor = colors.goldSeamSoft
                        )
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xl))

                KintsuButton(
                    text = "Begin Focus Lock (${selectedDuration}m)",
                    onClick = {
                        val groupIds = if (appGroups.isNotEmpty()) appGroups.map { it.id }.joinToString(",") else "ALL"
                        onStartSession(
                            sessionTitle.ifBlank { "Deep Focus" },
                            selectedDuration,
                            groupIds,
                            restrictWebsites,
                            selectedStrictness,
                            allowBreaks
                        )
                    },
                    variant = KintsuButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showEarlyExitConfirmDialog && activeSession != null) {
        KintsuDialog(
            title = "End Focus Lock Early?",
            onDismissRequest = { showEarlyExitConfirmDialog = false },
            confirmButton = {
                KintsuButton(
                    text = "End Session",
                    onClick = {
                        showEarlyExitConfirmDialog = false
                        onEndSession(activeSession, true)
                    },
                    variant = KintsuButtonVariant.DANGER
                )
            },
            dismissButton = {
                KintsuButton(
                    text = "Stay Focused",
                    onClick = { showEarlyExitConfirmDialog = false },
                    variant = KintsuButtonVariant.PRIMARY
                )
            }
        ) {
            Text(
                text = "You are in a ${activeSession.strictnessMode} session. Take a breath and consider if you really need to step away right now.",
                style = typography.body,
                color = colors.textPrimary
            )
        }
    }
}
