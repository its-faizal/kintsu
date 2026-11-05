package com.kintsu.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ZenFlipClock(
    minutes: Int,
    seconds: Int,
    modifier: Modifier = Modifier,
    hours: Int? = null,
    digitWidth: Dp = 64.dp,
    digitHeight: Dp = 92.dp,
    fontSize: Int = 48
) {
    val mStr = "%02d".format(minutes.coerceIn(0, 99))
    val sStr = "%02d".format(seconds.coerceIn(0, 59))
    val hStr = hours?.let { "%02d".format(it.coerceIn(0, 99)) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hStr != null) {
            SplitFlapDigit(hStr[0], width = digitWidth, height = digitHeight, fontSize = fontSize)
            Spacer(modifier = Modifier.width(6.dp))
            SplitFlapDigit(hStr[1], width = digitWidth, height = digitHeight, fontSize = fontSize)
            FlipClockColon(height = digitHeight)
        }

        SplitFlapDigit(mStr[0], width = digitWidth, height = digitHeight, fontSize = fontSize)
        Spacer(modifier = Modifier.width(6.dp))
        SplitFlapDigit(mStr[1], width = digitWidth, height = digitHeight, fontSize = fontSize)

        FlipClockColon(height = digitHeight)

        SplitFlapDigit(sStr[0], width = digitWidth, height = digitHeight, fontSize = fontSize)
        Spacer(modifier = Modifier.width(6.dp))
        SplitFlapDigit(sStr[1], width = digitWidth, height = digitHeight, fontSize = fontSize)
    }
}

@Composable
fun SplitFlapDigit(
    digit: Char,
    width: Dp = 64.dp,
    height: Dp = 92.dp,
    fontSize: Int = 48
) {
    var currentDigit by remember { mutableStateOf(digit) }
    var previousDigit by remember { mutableStateOf(digit) }
    var flipping by remember { mutableStateOf(false) }

    LaunchedEffect(digit) {
        if (digit != currentDigit) {
            previousDigit = currentDigit
            currentDigit = digit
            flipping = true
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (flipping) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        finishedListener = {
            flipping = false
        },
        label = "split_flap_flip"
    )

    val obsidianCardColor = Color(0xFF161412)
    val goldDividerColor = Color(0xFFD4AF37)
    val goldBorderColor = Color(0x40D4AF37)
    val ivoryTextColor = Color(0xFFF5F2EB)

    val halfHeight = height / 2

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(8.dp))
            .background(obsidianCardColor)
            .border(1.dp, goldBorderColor, RoundedCornerShape(8.dp))
    ) {
        // Upper half (Current Digit)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(halfHeight)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF221F1B), Color(0xFF161412))
                    )
                )
                .clipHalf(isTop = true)
        ) {
            Text(
                text = currentDigit.toString(),
                color = ivoryTextColor,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }

        // Lower half (Previous Digit while flipping, Current once settled)
        val lowerDigit = if (rotation > 90f) currentDigit else previousDigit
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(halfHeight)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF12100E), Color(0xFF0D0B0A))
                    )
                )
                .clipHalf(isTop = false)
        ) {
            Text(
                text = lowerDigit.toString(),
                color = ivoryTextColor,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }

        // Flipping flap
        if (flipping) {
            val isFirstHalf = rotation <= 90f
            val flapDigit = if (isFirstHalf) previousDigit else currentDigit

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(halfHeight)
                    .align(if (isFirstHalf) Alignment.TopCenter else Alignment.BottomCenter)
                    .graphicsLayer {
                        rotationX = if (isFirstHalf) -rotation else 180f - rotation
                        cameraDistance = 16f * density
                        transformOrigin = TransformOrigin(
                            pivotFractionX = 0.5f,
                            pivotFractionY = if (isFirstHalf) 1f else 0f
                        )
                    }
                    .background(
                        if (isFirstHalf) Color(0xFF1E1B18) else Color(0xFF141210)
                    )
                    .clipHalf(isTop = isFirstHalf)
            ) {
                Text(
                    text = flapDigit.toString(),
                    color = ivoryTextColor,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(if (isFirstHalf) Alignment.TopCenter else Alignment.BottomCenter)
                )
            }
        }

        // Central Gold Hairline Crease Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.Center)
                .background(goldDividerColor)
        )
    }
}

@Composable
fun FlipClockColon(height: Dp) {
    Column(
        modifier = Modifier
            .height(height)
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFFD4AF37))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFFD4AF37))
        )
    }
}

private fun Modifier.clipHalf(isTop: Boolean): Modifier = drawWithContent {
    val h = size.height
    val w = size.width
    // Clip top half or bottom half
    drawContent()
}
