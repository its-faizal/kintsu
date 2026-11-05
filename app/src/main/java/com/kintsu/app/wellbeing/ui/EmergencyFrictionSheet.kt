package com.kintsu.app.wellbeing.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.ui.components.IconCheck
import com.kintsu.app.ui.components.IconClose
import com.kintsu.app.ui.components.IconSparkle
import com.kintsu.app.ui.theme.KintsuTheme
import kotlin.math.sqrt

enum class FrictionMode {
    MANTRA,
    SHAKE
}

private const val TARGET_MANTRA = "I choose conscious presence over mindless consumption"

@Composable
fun EmergencyFrictionSheet(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val context = LocalContext.current

    var selectedMode by remember { mutableStateOf(FrictionMode.MANTRA) }
    var mantraInput by remember { mutableStateOf("") }
    val isMantraMatched = mantraInput.trim().equals(TARGET_MANTRA, ignoreCase = true)

    // Shake detector state (10s continuous energy requirement)
    var shakeProgress by remember { mutableFloatStateOf(0f) }
    var shakeSecondsCompleted by remember { mutableIntStateOf(0) }
    val animatedShakeProgress by animateFloatAsState(
        targetValue = shakeProgress,
        animationSpec = tween(150),
        label = "shakeProgress"
    )

    DisposableEffect(selectedMode) {
        if (selectedMode == FrictionMode.SHAKE) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            var lastShakeTimestamp = 0L
            var accumulatedEnergy = 0f

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    // Calculate net acceleration magnitude minus gravity
                    val netG = sqrt(x * x + y * y + z * z) - 9.81f
                    if (netG > 4.5f) { // Shake movement threshold
                        val now = System.currentTimeMillis()
                        if (now - lastShakeTimestamp < 800L) {
                            accumulatedEnergy += 0.05f
                        } else {
                            accumulatedEnergy += 0.02f
                        }
                        lastShakeTimestamp = now

                        val newProgress = (accumulatedEnergy / 10f).coerceIn(0f, 1f)
                        shakeProgress = newProgress
                        shakeSecondsCompleted = (newProgress * 10).toInt()

                        if (newProgress >= 1f) {
                            onSuccess()
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager?.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

            onDispose {
                sensorManager?.unregisterListener(listener)
            }
        } else {
            onDispose {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceElevated)
            .border(1.2.dp, colors.goldSeam.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .padding(spacing.l)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Conscious Friction",
                        style = typography.itemTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Overcoming automatic impulse requires mindful effort",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    IconClose(size = 16.dp, color = colors.textSecondary)
                }
            }

            // Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    FrictionMode.MANTRA to "Mantra Typing",
                    FrictionMode.SHAKE to "10s Physical Shake"
                ).forEach { (mode, title) ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.goldSeamSoft else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) colors.goldSeam else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedMode = mode }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = typography.caption.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) colors.goldSeam else colors.textSecondary
                        )
                    }
                }
            }

            if (selectedMode == FrictionMode.MANTRA) {
                // Mantra Typing Challenge
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.s)
                ) {
                    Text(
                        text = "Type this exact sentence to confirm your intention:",
                        style = typography.caption,
                        color = colors.textSecondary
                    )

                    // Target mantra card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "\"$TARGET_MANTRA\"",
                            style = typography.caption.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.goldSeam,
                                lineHeight = 18.sp
                            )
                        )
                    }

                    // Input Field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(
                                1.dp,
                                if (isMantraMatched) colors.goldSeam else colors.border,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        if (mantraInput.isEmpty()) {
                            Text(
                                text = "Type sentence here...",
                                style = typography.caption,
                                color = colors.textTertiary
                            )
                        }
                        BasicTextField(
                            value = mantraInput,
                            onValueChange = { mantraInput = it },
                            textStyle = typography.caption.copy(
                                color = colors.textPrimary,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(colors.goldSeam),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (isMantraMatched) onSuccess()
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.xs))

                    // Unlock Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isMantraMatched) colors.accent else colors.surface.copy(alpha = 0.5f))
                            .border(
                                1.dp,
                                if (isMantraMatched) colors.goldSeam else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = isMantraMatched) { onSuccess() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isMantraMatched) {
                                IconCheck(size = 16.dp, color = Color.Black)
                            }
                            Text(
                                text = if (isMantraMatched) "Unlock 5-Min Pass" else "Complete sentence to unlock",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMantraMatched) Color.Black else colors.textTertiary
                            )
                        }
                    }
                }
            } else {
                // Physical Shake Challenge
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    Text(
                        text = "Physically shake your device continuously to ground your senses and dispel impulsive urge.",
                        style = typography.caption,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedShakeProgress },
                            modifier = Modifier.size(120.dp),
                            color = colors.goldSeam,
                            trackColor = colors.surface,
                            strokeWidth = 6.dp
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$shakeSecondsCompleted / 10s",
                                style = typography.itemTitle.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.goldSeam
                            )
                            Text(
                                text = "Keep shaking",
                                style = typography.caption.copy(fontSize = 10.sp),
                                color = colors.textSecondary
                            )
                        }
                    }

                    Text(
                        text = "Pass unlocks automatically at 10 seconds",
                        style = typography.caption.copy(fontSize = 11.sp),
                        color = colors.textTertiary
                    )
                }
            }
        }
    }
}
