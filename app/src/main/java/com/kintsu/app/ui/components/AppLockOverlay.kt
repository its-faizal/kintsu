package com.kintsu.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kintsu.app.security.AppLockManager
import com.kintsu.app.security.AppLockType
import com.kintsu.app.ui.theme.KintsuTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppLockOverlay(
    storedCodeHash: String,
    lockType: AppLockType,
    biometricEnabled: Boolean,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = KintsuTheme.colors
    val spacing = KintsuTheme.spacing
    val typography = KintsuTheme.typography
    val scope = rememberCoroutineScope()

    var enteredPin by remember { mutableStateOf("") }
    var enteredPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var stealthMode by remember { mutableStateOf(false) }

    // Pattern state
    val isConfigured5x5 = storedCodeHash.startsWith("5x5:")
    var patternGridSize by remember { mutableIntStateOf(if (isConfigured5x5) 5 else 3) }
    val enteredPattern = remember { mutableStateListOf<Int>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Shake animation for incorrect inputs
    val shakeOffset = remember { Animatable(0f) }

    fun triggerError(msg: String) {
        errorMessage = msg
        scope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 350
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -8f at 250
                    8f at 300
                    0f at 350
                }
            )
            enteredPin = ""
            enteredPassword = ""
            enteredPattern.clear()
        }
    }

    fun verifySecretInput(rawInput: String) {
        if (AppLockManager.verifySecret(rawInput, storedCodeHash)) {
            onUnlocked()
        } else {
            triggerError("Incorrect passcode")
        }
    }

    fun verifyPatternInput(points: List<Int>, gridSize: Int) {
        if (AppLockManager.verifyPattern(points, gridSize, storedCodeHash)) {
            onUnlocked()
        } else {
            triggerError("Incorrect pattern")
        }
    }

    // Auto-prompt biometrics if enabled
    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled) {
            AppLockManager.authenticateWithBiometrics(
                context = context,
                onSuccess = { onUnlocked() },
                onError = { /* Keep overlay visible for PIN/pattern/password fallback */ }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = spacing.xl, vertical = spacing.l),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                .fillMaxWidth()
        ) {
            // Emblem
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colors.accentSoft.copy(alpha = 0.35f))
                    .border(1.5.dp, colors.goldSeam.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconGoldSeam(color = colors.goldSeam, size = 30.dp)
            }

            Spacer(modifier = Modifier.height(spacing.s))

            Text(
                text = "KINTSU",
                style = typography.screenTitle.copy(letterSpacing = 4.sp),
                color = colors.textPrimary
            )

            Text(
                text = "Sanctuary Locked",
                style = typography.caption,
                color = colors.goldSeam,
                fontWeight = FontWeight.Medium
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = errorMessage ?: "",
                    style = typography.caption,
                    color = colors.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(spacing.m))

            // Sub-mode layout
            when (lockType) {
                AppLockType.PIN -> {
                    // Uniform PIN Indicators (No jumping, no fixed empty dots, stealth support)
                    if (!stealthMode) {
                        if (enteredPin.isEmpty()) {
                            Text(
                                text = "Enter Passcode",
                                style = typography.caption,
                                color = colors.textTertiary,
                                modifier = Modifier.padding(vertical = spacing.xs)
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = spacing.xs)
                            ) {
                                val visibleCount = minOf(14, enteredPin.length)
                                repeat(visibleCount) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(colors.goldSeam)
                                    )
                                }
                                if (enteredPin.length > 14) {
                                    Text(
                                        text = "+${enteredPin.length - 14}",
                                        style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                        color = colors.goldSeam
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = spacing.xs)
                        ) {
                            IconShield(color = colors.goldSeam.copy(alpha = 0.7f), size = 14.dp)
                            Text(
                                text = "Stealth Mode Active",
                                style = typography.caption.copy(fontSize = 11.sp),
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.m))

                    // Keypad Grid
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("BIO", "0", "DEL")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        keys.forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                row.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (key == "BIO" || key == "DEL") Color.Transparent
                                                else colors.surfaceElevated
                                            )
                                            .border(
                                                1.dp,
                                                if (key == "BIO" || key == "DEL") Color.Transparent
                                                else colors.border.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                            .clickable {
                                                when (key) {
                                                    "DEL" -> {
                                                        if (enteredPin.isNotEmpty()) {
                                                            enteredPin = enteredPin.dropLast(1)
                                                            errorMessage = null
                                                        }
                                                    }
                                                    "BIO" -> {
                                                        if (biometricEnabled) {
                                                            AppLockManager.authenticateWithBiometrics(
                                                                context = context,
                                                                onSuccess = { onUnlocked() },
                                                                onError = { err -> triggerError(err) }
                                                            )
                                                        }
                                                    }
                                                    else -> {
                                                        if (enteredPin.length < 24) {
                                                            val next = enteredPin + key
                                                            enteredPin = next
                                                            errorMessage = null
                                                            if (next.length == 24) {
                                                                verifySecretInput(next)
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (key) {
                                            "DEL" -> Text(
                                                text = "⌫",
                                                color = colors.textPrimary,
                                                fontSize = 20.sp
                                            )
                                            "BIO" -> {
                                                if (biometricEnabled) {
                                                    Text(
                                                        text = "◎",
                                                        color = colors.accent,
                                                        fontSize = 22.sp
                                                    )
                                                }
                                            }
                                            else -> Text(
                                                text = key,
                                                style = typography.screenTitle,
                                                color = colors.textPrimary,
                                                fontSize = 22.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.s))

                    Row(
                        modifier = Modifier.fillMaxWidth(0.72f),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (stealthMode) colors.goldSeam.copy(alpha = 0.15f) else colors.surfaceElevated)
                                .border(1.dp, if (stealthMode) colors.goldSeam.copy(alpha = 0.6f) else colors.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { stealthMode = !stealthMode }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (stealthMode) "Stealth: ON" else "Stealth: OFF",
                                style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                color = if (stealthMode) colors.goldSeam else colors.textSecondary
                            )
                        }

                        KintsuButton(
                            text = if (stealthMode) "Unlock" else "Unlock (${enteredPin.length})",
                            onClick = { verifySecretInput(enteredPin) },
                            enabled = enteredPin.length >= 4,
                            variant = KintsuButtonVariant.PRIMARY,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }

                AppLockType.PASSWORD -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.s)
                    ) {
                        OutlinedTextField(
                            value = enteredPassword,
                            onValueChange = {
                                enteredPassword = it
                                errorMessage = null
                            },
                            placeholder = { Text("Password (case-sensitive)", color = colors.textTertiary) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                autoCorrect = false
                            ),
                            keyboardActions = KeyboardActions(onDone = { verifySecretInput(enteredPassword) }),
                            trailingIcon = {
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
                                    fontSize = 12.sp,
                                    color = colors.accent,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clickable { passwordVisible = !passwordVisible }
                                        .padding(8.dp)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = KintsuTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.border
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.s)
                        ) {
                            if (biometricEnabled) {
                                KintsuButton(
                                    text = "Biometric",
                                    onClick = {
                                        AppLockManager.authenticateWithBiometrics(
                                            context = context,
                                            onSuccess = { onUnlocked() },
                                            onError = { err -> triggerError(err) }
                                        )
                                    },
                                    variant = KintsuButtonVariant.SECONDARY,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            KintsuButton(
                                text = "Unlock",
                                onClick = { verifySecretInput(enteredPassword) },
                                variant = KintsuButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                AppLockType.PATTERN -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.s)
                    ) {
                        // If user configured 5x5, show 5x5; if using standard 3x3, remove 5x5 toggle completely
                        if (isConfigured5x5) {
                            Text(
                                text = "5 × 5 Secure Pattern",
                                style = typography.caption.copy(fontSize = 11.sp),
                                color = colors.goldSeam
                            )
                        }

                        // Touch Pattern Canvas
                        VisualPatternCanvas(
                            gridSize = patternGridSize,
                            selectedNodes = enteredPattern,
                            onNodeSelected = { node ->
                                enteredPattern.add(node)
                                errorMessage = null
                            },
                            onDragStarted = {
                                enteredPattern.clear()
                                errorMessage = null
                            },
                            onDragEnded = {
                                if (enteredPattern.size >= 3) {
                                    verifyPatternInput(enteredPattern, patternGridSize)
                                } else if (enteredPattern.isNotEmpty()) {
                                    triggerError("Connect at least 3 points")
                                }
                            },
                            canvasSize = 270.dp,
                            stealthMode = stealthMode
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (stealthMode) colors.goldSeam.copy(alpha = 0.15f) else colors.surfaceElevated)
                                .border(1.dp, if (stealthMode) colors.goldSeam.copy(alpha = 0.6f) else colors.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { stealthMode = !stealthMode }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (stealthMode) "Stealth Mode: ON" else "Stealth Mode: OFF",
                                style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                color = if (stealthMode) colors.goldSeam else colors.textSecondary
                            )
                        }

                        if (biometricEnabled) {
                            Text(
                                text = "Tap for Biometric Unlock",
                                style = typography.caption.copy(color = colors.accent, fontWeight = FontWeight.Medium),
                                modifier = Modifier
                                    .clickable {
                                        AppLockManager.authenticateWithBiometrics(
                                            context = context,
                                            onSuccess = { onUnlocked() },
                                            onError = { err -> triggerError(err) }
                                        )
                                    }
                                    .padding(top = spacing.xs)
                            )
                        }
                    }
                }
            }
        }
    }
}
