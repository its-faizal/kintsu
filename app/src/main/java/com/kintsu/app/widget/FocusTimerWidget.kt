package com.kintsu.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kintsu.app.data.SettingsDataStore
import com.kintsu.app.domain.focus.FocusTimerManager
import com.kintsu.app.focus.FocusForegroundService
import kotlinx.coroutines.flow.first

class FocusTimerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settingsStore = SettingsDataStore(context)
        val settings = settingsStore.settingsFlow.first()
        val colors = KintsuWidgetTheme.getColors(context)

        val focusState = FocusForegroundService.focusState.value
        val isRunning = focusState.isRunning && !focusState.isPaused
        val isPaused = focusState.isRunning && focusState.isPaused

        val totalSec = if (focusState.isRunning) {
            FocusTimerManager.getRemainingSeconds(focusState).toInt()
        } else {
            settings.focusDefaultPresetMinutes * 60
        }
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        val timeDisplay = "%02d:%02d".format(minutes, seconds)

        val taskTitle = focusState.linkedTaskTitle ?: if (focusState.isBreak) "Rest & Breathe" else "Mindful Focus"
        val statusLabel = when {
            isRunning -> "RUNNING"
            isPaused -> "PAUSED"
            else -> "IDLE"
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surfaceProvider)
                    .cornerRadius(22.dp)
                    .padding(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_OPEN_FOCUS))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Focus Sanctuary",
                            style = TextStyle(
                                color = colors.accentProvider,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = taskTitle,
                            style = TextStyle(
                                color = colors.textPrimaryProvider,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .background(if (isRunning) colors.accentSoftProvider else colors.surfaceElevatedProvider)
                            .cornerRadius(8.dp)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = TextStyle(
                                color = if (isRunning) colors.accentProvider else colors.textSecondaryProvider,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Timer & Controls Row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Big Time Display
                    Text(
                        text = timeDisplay,
                        style = TextStyle(
                            color = colors.textPrimaryProvider,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier
                            .clickable(actionStartActivity(WidgetIntents.createIntent(context, WidgetIntents.ACTION_OPEN_FOCUS)))
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Interactive Action Button
                    if (isRunning) {
                        // Pause Button
                        Box(
                            modifier = GlanceModifier
                                .background(colors.surfaceElevatedProvider)
                                .cornerRadius(12.dp)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .clickable(
                                    actionRunCallback<FocusControlWidgetCallback>(
                                        actionParametersOf(WidgetFocusActionKey to "PAUSE")
                                    )
                                )
                        ) {
                            Text(
                                text = "Pause",
                                style = TextStyle(
                                    color = colors.textPrimaryProvider,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        // Stop Button
                        Box(
                            modifier = GlanceModifier
                                .background(colors.surfaceElevatedProvider)
                                .cornerRadius(12.dp)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .clickable(
                                    actionRunCallback<FocusControlWidgetCallback>(
                                        actionParametersOf(WidgetFocusActionKey to "STOP")
                                    )
                                )
                        ) {
                            Text(
                                text = "Stop",
                                style = TextStyle(
                                    color = colors.goldSeamProvider,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else if (isPaused) {
                        // Resume Button
                        Box(
                            modifier = GlanceModifier
                                .background(colors.accentProvider)
                                .cornerRadius(12.dp)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .clickable(
                                    actionRunCallback<FocusControlWidgetCallback>(
                                        actionParametersOf(WidgetFocusActionKey to "RESUME")
                                    )
                                )
                        ) {
                            Text(
                                text = "Resume",
                                style = TextStyle(
                                    color = colors.backgroundProvider,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        // Stop Button
                        Box(
                            modifier = GlanceModifier
                                .background(colors.surfaceElevatedProvider)
                                .cornerRadius(12.dp)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .clickable(
                                    actionRunCallback<FocusControlWidgetCallback>(
                                        actionParametersOf(WidgetFocusActionKey to "STOP")
                                    )
                                )
                        ) {
                            Text(
                                text = "Stop",
                                style = TextStyle(
                                    color = colors.goldSeamProvider,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        // Idle - Start Button
                        Box(
                            modifier = GlanceModifier
                                .background(colors.accentProvider)
                                .cornerRadius(12.dp)
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                                .clickable(
                                    actionRunCallback<FocusControlWidgetCallback>(
                                        actionParametersOf(
                                            WidgetFocusActionKey to "START",
                                            WidgetFocusPresetKey to settings.focusDefaultPresetMinutes
                                        )
                                    )
                                )
                        ) {
                            Text(
                                text = "▶ Start",
                                style = TextStyle(
                                    color = colors.backgroundProvider,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class FocusTimerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FocusTimerWidget()
}
