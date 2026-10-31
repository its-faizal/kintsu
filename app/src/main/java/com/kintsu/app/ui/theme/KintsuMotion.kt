package com.kintsu.app.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Apple Design & Emil Kowalski Motion Principles:
 * - Fluid, interruptible spring physics
 * - Direct 1:1 manipulation without sluggish delays
 * - Respond on pointer-down (instant press feedback) rather than pointer-up
 */
object KintsuSprings {
    // 1. Gentle: High damping, smooth deceleration for modal sheets and overlays
    val Gentle: AnimationSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = 280f
    )

    // 2. Fluid: Standard Apple fluid interactive spring (WWDC 2018 Fluid Interfaces)
    val Fluid: AnimationSpec<Float> = spring(
        dampingRatio = 0.78f,
        stiffness = 380f
    )

    // 3. Snappy: Quick response for toggles, micro-switches, and segmented controls
    val Snappy: AnimationSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = 450f
    )

    // 4. Bouncy: Heroic milestones, celebration pop, and achievement badges
    val Bouncy: AnimationSpec<Float> = spring(
        dampingRatio = 0.65f,
        stiffness = 320f
    )
}

typealias AppleMotion = KintsuSprings

/**
 * Apple Design principle: "Feedback lives on the press, and it's instant."
 * Respond on pointer-down with 0.96f scale, not waiting for release or tap timeout.
 * Interruptible spring physics with immediate velocity inheritance.
 */
fun Modifier.applePressable(
    pressedScale: Float = 0.96f,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    if (!enabled) return@composed this

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = if (isPressed) KintsuSprings.Snappy else KintsuSprings.Fluid,
        label = "apple_pressable_scale"
    )

    this
        .scale(scale)
        .pointerInput(enabled) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                val upOrCancel = waitForUpOrCancellation()
                isPressed = false
                if (upOrCancel != null && onClick != null) {
                    onClick()
                }
            }
        }
}
