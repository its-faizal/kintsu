package com.kintsu.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class KintsuSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val s: Dp = 12.dp,
    val m: Dp = 16.dp,
    val l: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 40.dp
)

val CompactSpacing = KintsuSpacing(
    xxs = 2.dp,
    xs = 6.dp,
    s = 10.dp,
    m = 12.dp,
    l = 16.dp,
    xl = 20.dp,
    xxl = 24.dp,
    xxxl = 32.dp
)

@Immutable
data class KintsuShapes(
    val small: Shape = RoundedCornerShape(10.dp),
    val medium: Shape = RoundedCornerShape(16.dp),
    val large: Shape = RoundedCornerShape(22.dp),
    val full: Shape = RoundedCornerShape(999.dp)
)

@Immutable
data class KintsuElevation(
    val none: Dp = 0.dp,
    val low: Dp = 2.dp,
    val medium: Dp = 6.dp,
    val high: Dp = 12.dp
)

@Immutable
data class KintsuMotion(
    val fastMs: Int = 180,
    val normalMs: Int = 260,
    val slowMs: Int = 400
)

val ReducedMotion = KintsuMotion(
    fastMs = 0,
    normalMs = 0,
    slowMs = 0
)

val LocalKintsuColors = staticCompositionLocalOf { LightPalette }
val LocalKintsuSpacing = staticCompositionLocalOf { KintsuSpacing() }
val LocalKintsuShapes = staticCompositionLocalOf { KintsuShapes() }
val LocalKintsuElevation = staticCompositionLocalOf { KintsuElevation() }
val LocalKintsuMotion = staticCompositionLocalOf { KintsuMotion() }
