package com.kintsu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Consistent Lucide/Phosphor-style thin line icons (1.6dp stroke, round cap & join).
 */

@Composable
fun IconHome(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        val p = Path().apply {
            moveTo(w * 0.18f, h * 0.44f)
            lineTo(w * 0.5f, h * 0.16f)
            lineTo(w * 0.82f, h * 0.44f)
            lineTo(w * 0.82f, h * 0.84f)
            cubicTo(w * 0.82f, h * 0.88f, w * 0.78f, h * 0.88f, w * 0.74f, h * 0.88f)
            lineTo(w * 0.26f, h * 0.88f)
            cubicTo(w * 0.22f, h * 0.88f, w * 0.18f, h * 0.88f, w * 0.18f, h * 0.84f)
            close()
        }
        drawPath(p, color = color, style = s)

        // Doorway
        val door = Path().apply {
            moveTo(w * 0.38f, h * 0.88f)
            lineTo(w * 0.38f, h * 0.56f)
            cubicTo(w * 0.38f, h * 0.48f, w * 0.62f, h * 0.48f, w * 0.62f, h * 0.56f)
            lineTo(w * 0.62f, h * 0.88f)
        }
        drawPath(door, color = color, style = s)
    }
}

@Composable
fun IconHabit(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Minimalist leafy bud / seedling (habit growth)
        val p = Path().apply {
            moveTo(w * 0.5f, h * 0.85f)
            lineTo(w * 0.5f, h * 0.45f)
            cubicTo(w * 0.5f, h * 0.25f, w * 0.2f, h * 0.25f, w * 0.2f, h * 0.45f)
            cubicTo(w * 0.2f, h * 0.65f, w * 0.5f, h * 0.65f, w * 0.5f, h * 0.65f)
            moveTo(w * 0.5f, h * 0.52f)
            cubicTo(w * 0.5f, h * 0.32f, w * 0.8f, h * 0.32f, w * 0.8f, h * 0.52f)
            cubicTo(w * 0.8f, h * 0.72f, w * 0.5f, h * 0.72f, w * 0.5f, h * 0.72f)
        }
        drawPath(p, color = color, style = s)
    }
}

@Composable
fun IconTask(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Checklist clipboard
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.2f, h * 0.22f),
            size = Size(w * 0.6f, h * 0.64f),
            cornerRadius = CornerRadius(6f, 6f),
            style = s
        )
        // Clip header
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.35f, h * 0.14f),
            size = Size(w * 0.3f, h * 0.16f),
            cornerRadius = CornerRadius(4f, 4f),
            style = s
        )
        // Lines
        drawLine(color, Offset(w * 0.34f, h * 0.44f), Offset(w * 0.66f, h * 0.44f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.34f, h * 0.60f), Offset(w * 0.56f, h * 0.60f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun IconGoal(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Target bullseye / horizon compass
        drawCircle(color = color, radius = w * 0.38f, center = Offset(w * 0.5f, h * 0.5f), style = s)
        drawCircle(color = color, radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.5f), style = s)
        drawCircle(color = color, radius = w * 0.04f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
fun IconProgress(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Minimal stepped bars / spark chart
        drawLine(color, Offset(w * 0.25f, h * 0.78f), Offset(w * 0.25f, h * 0.55f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.50f, h * 0.78f), Offset(w * 0.50f, h * 0.38f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.75f, h * 0.78f), Offset(w * 0.75f, h * 0.22f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.15f, h * 0.85f), Offset(w * 0.85f, h * 0.85f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun IconUser(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Head circle
        drawCircle(color = color, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.32f), style = s)

        // Shoulders arc
        val p = Path().apply {
            moveTo(w * 0.22f, h * 0.82f)
            cubicTo(
                w * 0.25f, h * 0.62f,
                w * 0.75f, h * 0.62f,
                w * 0.78f, h * 0.82f
            )
        }
        drawPath(p, color = color, style = s)
    }
}

@Composable
fun IconShop(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Bag body
        val p = Path().apply {
            moveTo(w * 0.22f, h * 0.38f)
            lineTo(w * 0.78f, h * 0.38f)
            lineTo(w * 0.84f, h * 0.84f)
            lineTo(w * 0.16f, h * 0.84f)
            close()
        }
        drawPath(p, color = color, style = s)

        // Bag handle
        val handle = Path().apply {
            moveTo(w * 0.36f, h * 0.38f)
            cubicTo(
                w * 0.36f, h * 0.20f,
                w * 0.64f, h * 0.20f,
                w * 0.64f, h * 0.38f
            )
        }
        drawPath(handle, color = color, style = s)
    }
}

@Composable
fun IconSettings(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Clean sliders icon (Apple-style settings glyph)
        drawLine(color, Offset(w * 0.2f, h * 0.32f), Offset(w * 0.8f, h * 0.32f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = color, radius = w * 0.09f, center = Offset(w * 0.40f, h * 0.32f), style = s)

        drawLine(color, Offset(w * 0.2f, h * 0.68f), Offset(w * 0.8f, h * 0.68f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = color, radius = w * 0.09f, center = Offset(w * 0.65f, h * 0.68f), style = s)
    }
}

@Composable
fun IconCheck(color: Color, modifier: Modifier = Modifier, size: Dp = 16.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        val p = Path().apply {
            moveTo(w * 0.2f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.74f)
            lineTo(w * 0.82f, h * 0.26f)
        }
        drawPath(p, color = color, style = s)
    }
}

@Composable
fun IconAdd(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.6f.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        drawLine(color, Offset(w * 0.5f, h * 0.22f), Offset(w * 0.5f, h * 0.78f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.22f, h * 0.5f), Offset(w * 0.78f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun IconClose(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.6f.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        drawLine(color, Offset(w * 0.28f, h * 0.28f), Offset(w * 0.72f, h * 0.72f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.72f, h * 0.28f), Offset(w * 0.28f, h * 0.72f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun IconChevronRight(color: Color, modifier: Modifier = Modifier, size: Dp = 16.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        val p = Path().apply {
            moveTo(w * 0.35f, h * 0.22f)
            lineTo(w * 0.65f, h * 0.5f)
            lineTo(w * 0.35f, h * 0.78f)
        }
        drawPath(p, color = color, style = s)
    }
}

@Composable
fun IconCalendar(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.5f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.18f, h * 0.24f),
            size = Size(w * 0.64f, h * 0.62f),
            cornerRadius = CornerRadius(5f, 5f),
            style = s
        )
        drawLine(color, Offset(w * 0.18f, h * 0.42f), Offset(w * 0.82f, h * 0.42f), strokeWidth = 1.3f.dp.toPx())
        drawLine(color, Offset(w * 0.35f, h * 0.14f), Offset(w * 0.35f, h * 0.24f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.65f, h * 0.14f), Offset(w * 0.65f, h * 0.24f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun IconGoldSeam(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Kintsugi lightning branch
        val p = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.35f, h * 0.45f)
            lineTo(w * 0.65f, h * 0.52f)
            lineTo(w * 0.45f, h * 0.85f)
        }
        drawPath(p, color = color, style = s)
    }
}

@Composable
fun IconFlame(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        val p = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            cubicTo(w * 0.68f, h * 0.35f, w * 0.82f, h * 0.55f, w * 0.78f, h * 0.72f)
            cubicTo(w * 0.74f, h * 0.88f, w * 0.58f, h * 0.9f, w * 0.5f, h * 0.9f)
            cubicTo(w * 0.42f, h * 0.9f, w * 0.26f, h * 0.88f, w * 0.22f, h * 0.72f)
            cubicTo(w * 0.18f, h * 0.55f, w * 0.32f, h * 0.35f, w * 0.5f, h * 0.15f)
            close()
        }
        drawPath(p, color = color, style = s)
    }
}

@Composable
fun IconTrash(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.5f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        drawLine(color, Offset(w * 0.22f, h * 0.26f), Offset(w * 0.78f, h * 0.26f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
        val p = Path().apply {
            moveTo(w * 0.3f, h * 0.26f)
            lineTo(w * 0.33f, h * 0.82f)
            lineTo(w * 0.67f, h * 0.82f)
            lineTo(w * 0.7f, h * 0.26f)
        }
        drawPath(p, color = color, style = s)
        drawLine(color, Offset(w * 0.42f, h * 0.38f), Offset(w * 0.42f, h * 0.70f), strokeWidth = 1.3f.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.58f, h * 0.38f), Offset(w * 0.58f, h * 0.70f), strokeWidth = 1.3f.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun IconArchive(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.5f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        drawRoundRect(color, Offset(w * 0.15f, h * 0.2f), Size(w * 0.7f, h * 0.2f), CornerRadius(4f, 4f), style = s)
        drawRoundRect(color, Offset(w * 0.2f, h * 0.4f), Size(w * 0.6f, h * 0.45f), CornerRadius(4f, 4f), style = s)
        drawLine(color, Offset(w * 0.42f, h * 0.55f), Offset(w * 0.58f, h * 0.55f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun IconClock(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.5f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        drawCircle(color = color, radius = w * 0.38f, center = Offset(w * 0.5f, h * 0.5f), style = s)
        drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.28f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.68f, h * 0.5f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun IconPlay(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.32f, h * 0.22f)
            lineTo(w * 0.78f, h * 0.5f)
            lineTo(w * 0.32f, h * 0.78f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.5f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun IconBook(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.5f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Book open spine and pages
        drawLine(color, Offset(w * 0.5f, h * 0.25f), Offset(w * 0.5f, h * 0.8f), strokeWidth = 1.5f.dp.toPx(), cap = StrokeCap.Round)
        val leftPage = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.3f)
            cubicTo(w * 0.38f, h * 0.25f, w * 0.28f, h * 0.25f, w * 0.18f, h * 0.28f)
            lineTo(w * 0.18f, h * 0.78f)
            cubicTo(w * 0.28f, h * 0.75f, w * 0.38f, h * 0.75f, w * 0.5f, h * 0.8f)
        }
        val rightPage = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.3f)
            cubicTo(w * 0.62f, h * 0.25f, w * 0.72f, h * 0.25f, w * 0.82f, h * 0.28f)
            lineTo(w * 0.82f, h * 0.78f)
            cubicTo(w * 0.72f, h * 0.75f, w * 0.62f, h * 0.75f, w * 0.5f, h * 0.8f)
        }
        drawPath(leftPage, color, style = s)
        drawPath(rightPage, color, style = s)
    }
}

@Composable
fun IconRepeat(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Top arc with arrow pointing right
        val p1 = Path().apply {
            moveTo(w * 0.25f, h * 0.45f)
            cubicTo(w * 0.25f, h * 0.25f, w * 0.75f, h * 0.25f, w * 0.75f, h * 0.45f)
            lineTo(w * 0.85f, h * 0.35f)
        }
        drawPath(p1, color = color, style = s)

        // Bottom arc with arrow pointing left
        val p2 = Path().apply {
            moveTo(w * 0.75f, h * 0.55f)
            cubicTo(w * 0.75f, h * 0.75f, w * 0.25f, h * 0.75f, w * 0.25f, h * 0.55f)
            lineTo(w * 0.15f, h * 0.65f)
        }
        drawPath(p2, color = color, style = s)
    }
}

@Composable
fun IconFocus(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Outer focus circle + inner dot
        drawCircle(color = color, radius = w * 0.38f, center = Offset(w * 0.5f, h * 0.5f), style = s)
        drawCircle(color = color, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
fun IconWidget(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        val gap = w * 0.14f
        val boxW = (w - gap) / 2f
        val boxH = (h - gap) / 2f
        val r = CornerRadius(boxW * 0.28f, boxW * 0.28f)

        // Top-left
        drawRoundRect(color, Offset(0f, 0f), Size(boxW, boxH), r, s)
        // Top-right
        drawRoundRect(color, Offset(boxW + gap, 0f), Size(boxW, boxH), r, s)
        // Bottom-left
        drawRoundRect(color, Offset(0f, boxH + gap), Size(boxW, boxH), r, s)
        // Bottom-right
        drawRoundRect(color, Offset(boxW + gap, boxH + gap), Size(boxW, boxH), r, s)
    }
}

@Composable
fun IconBack(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        drawLine(color, Offset(w * 0.75f, h * 0.5f), Offset(w * 0.25f, h * 0.5f), strokeWidth = 1.8f.dp.toPx(), cap = StrokeCap.Round)
        val p = Path().apply {
            moveTo(w * 0.45f, h * 0.28f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.72f)
        }
        drawPath(p, color = color, style = s)
    }
}

@Composable
fun IconShield(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        val p = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.82f, h * 0.25f)
            lineTo(w * 0.82f, h * 0.55f)
            cubicTo(
                w * 0.82f, h * 0.75f,
                w * 0.5f, h * 0.88f,
                w * 0.5f, h * 0.88f
            )
            cubicTo(
                w * 0.5f, h * 0.88f,
                w * 0.18f, h * 0.75f,
                w * 0.18f, h * 0.55f
            )
            lineTo(w * 0.18f, h * 0.25f)
            close()
        }
        drawPath(p, color = color, style = s)
    }
}

@Composable
fun IconGlobe(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        drawCircle(color = color, radius = w * 0.38f, center = Offset(w * 0.5f, h * 0.5f), style = s)
        drawLine(color, Offset(w * 0.12f, h * 0.5f), Offset(w * 0.88f, h * 0.5f), strokeWidth = 1.5f.dp.toPx())

        val p = Path().apply {
            moveTo(w * 0.5f, h * 0.12f)
            cubicTo(w * 0.28f, h * 0.3f, w * 0.28f, h * 0.7f, w * 0.5f, h * 0.88f)
            cubicTo(w * 0.72f, h * 0.7f, w * 0.72f, h * 0.3f, w * 0.5f, h * 0.12f)
        }
        drawPath(p, color = color, style = s)
    }
}

@Composable
fun IconLock(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Shackle
        val shackle = Path().apply {
            moveTo(w * 0.32f, h * 0.44f)
            lineTo(w * 0.32f, h * 0.28f)
            cubicTo(w * 0.32f, h * 0.14f, w * 0.68f, h * 0.14f, w * 0.68f, h * 0.28f)
            lineTo(w * 0.68f, h * 0.44f)
        }
        drawPath(shackle, color = color, style = s)

        // Lock Body
        val r = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.20f, h * 0.44f),
            size = Size(w * 0.60f, h * 0.44f),
            cornerRadius = r,
            style = s
        )

        // Keyhole
        drawCircle(color = color, radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.62f))
    }
}

@Composable
fun IconSearch(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        drawCircle(color = color, radius = w * 0.28f, center = Offset(w * 0.42f, h * 0.42f), style = s)
        drawLine(
            color = color,
            start = Offset(w * 0.62f, h * 0.62f),
            end = Offset(w * 0.85f, h * 0.85f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun IconSparkle(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val p = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            cubicTo(w * 0.5f, h * 0.4f, w * 0.6f, h * 0.5f, w * 0.9f, h * 0.5f)
            cubicTo(w * 0.6f, h * 0.5f, w * 0.5f, h * 0.6f, w * 0.5f, h * 0.9f)
            cubicTo(w * 0.5f, h * 0.6f, w * 0.4f, h * 0.5f, w * 0.1f, h * 0.5f)
            cubicTo(w * 0.4f, h * 0.5f, w * 0.5f, h * 0.4f, w * 0.5f, h * 0.1f)
            close()
        }
        drawPath(p, color = color)
    }
}

@Composable
fun IconLeaf(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val p = Path().apply {
            moveTo(w * 0.15f, h * 0.85f)
            cubicTo(w * 0.15f, h * 0.35f, w * 0.5f, h * 0.15f, w * 0.85f, h * 0.15f)
            cubicTo(w * 0.85f, h * 0.65f, w * 0.65f, h * 0.85f, w * 0.15f, h * 0.85f)
            close()
        }
        drawPath(p, color = color)
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.85f),
            end = androidx.compose.ui.geometry.Offset(w * 0.7f, h * 0.3f),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
fun IconFavorite(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val p = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.62f, h * 0.38f)
            lineTo(w * 0.88f, h * 0.42f)
            lineTo(w * 0.69f, h * 0.61f)
            lineTo(w * 0.74f, h * 0.87f)
            lineTo(w * 0.5f, h * 0.75f)
            lineTo(w * 0.26f, h * 0.87f)
            lineTo(w * 0.31f, h * 0.61f)
            lineTo(w * 0.12f, h * 0.42f)
            lineTo(w * 0.38f, h * 0.38f)
            close()
        }
        drawPath(p, color = color)
    }
}

@Composable
fun IconHistory(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(
            color = color,
            radius = w * 0.38f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
            end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.28f),
            strokeWidth = 1.8.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
            end = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.5f),
            strokeWidth = 1.8.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun IconShare(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val r = 2.5.dp.toPx()
        val c1 = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.25f)
        val c2 = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.5f)
        val c3 = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.75f)

        drawLine(color = color, start = c2, end = c1, strokeWidth = 1.6.dp.toPx())
        drawLine(color = color, start = c2, end = c3, strokeWidth = 1.6.dp.toPx())
        drawCircle(color = color, radius = r, center = c1)
        drawCircle(color = color, radius = r, center = c2)
        drawCircle(color = color, radius = r, center = c3)
    }
}

@Composable
fun IconMore(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val r = 1.8.dp.toPx()
        drawCircle(color = color, radius = r, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.25f))
        drawCircle(color = color, radius = r, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f))
        drawCircle(color = color, radius = r, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.75f))
    }
}

@Composable
fun IconEdit(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.6.dp.toPx()
        // Pen body
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.75f),
            end = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.25f),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Pen tip
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.8f),
            end = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.75f),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun IconDelete(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.6.dp.toPx()
        // Lid
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.3f),
            end = androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.3f),
            strokeWidth = stroke,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Body
        val bodyPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.28f, h * 0.35f)
            lineTo(w * 0.33f, h * 0.82f)
            lineTo(w * 0.67f, h * 0.82f)
            lineTo(w * 0.72f, h * 0.35f)
        }
        drawPath(
            path = bodyPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun IconCustomizer(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Main shape - paintbrush/palette hybrid
        val p = Path().apply {
            // Brush handle
            moveTo(w * 0.25f, h * 0.75f)
            lineTo(w * 0.35f, h * 0.35f)
            // Ferrule
            lineTo(w * 0.65f, h * 0.35f)
            lineTo(w * 0.75f, h * 0.75f)
            close()
        }
        drawPath(p, color = color, style = s)

        // Bristles - three strokes
        drawLine(
            color = color,
            start = Offset(w * 0.38f, h * 0.35f),
            end = Offset(w * 0.38f, h * 0.18f),
            strokeWidth = 1.6f.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.35f),
            end = Offset(w * 0.5f, h * 0.12f),
            strokeWidth = 1.6f.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.62f, h * 0.35f),
            end = Offset(w * 0.62f, h * 0.18f),
            strokeWidth = 1.6f.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Sparkle accent on brush tip
        val sparkle = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.54f, h * 0.16f)
            lineTo(w * 0.62f, h * 0.16f)
            lineTo(w * 0.56f, h * 0.22f)
            lineTo(w * 0.58f, h * 0.3f)
            lineTo(w * 0.5f, h * 0.26f)
            lineTo(w * 0.42f, h * 0.3f)
            lineTo(w * 0.44f, h * 0.22f)
            lineTo(w * 0.38f, h * 0.16f)
            lineTo(w * 0.46f, h * 0.16f)
            close()
        }
        drawPath(sparkle, color = color)
    }
}





@Composable
fun IconAlignLeft(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.8f.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawLine(color, Offset(w * 0.15f, h * 0.25f), Offset(w * 0.85f, h * 0.25f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.15f, h * 0.45f), Offset(w * 0.60f, h * 0.45f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.15f, h * 0.65f), Offset(w * 0.85f, h * 0.65f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.15f, h * 0.85f), Offset(w * 0.50f, h * 0.85f), stroke, StrokeCap.Round)
    }
}

@Composable
fun IconAlignCenter(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.8f.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawLine(color, Offset(w * 0.15f, h * 0.25f), Offset(w * 0.85f, h * 0.25f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.28f, h * 0.45f), Offset(w * 0.72f, h * 0.45f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.15f, h * 0.65f), Offset(w * 0.85f, h * 0.65f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.32f, h * 0.85f), Offset(w * 0.68f, h * 0.85f), stroke, StrokeCap.Round)
    }
}

@Composable
fun IconAlignRight(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.8f.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawLine(color, Offset(w * 0.15f, h * 0.25f), Offset(w * 0.85f, h * 0.25f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.40f, h * 0.45f), Offset(w * 0.85f, h * 0.45f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.15f, h * 0.65f), Offset(w * 0.85f, h * 0.65f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * 0.50f, h * 0.85f), Offset(w * 0.85f, h * 0.85f), stroke, StrokeCap.Round)
    }
}


@Composable
fun IconNotifications(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.8f.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            cubicTo(w * 0.35f, h * 0.15f, w * 0.25f, h * 0.30f, w * 0.25f, h * 0.55f)
            lineTo(w * 0.18f, h * 0.72f)
            lineTo(w * 0.82f, h * 0.72f)
            lineTo(w * 0.75f, h * 0.55f)
            cubicTo(w * 0.75f, h * 0.30f, w * 0.65f, h * 0.15f, w * 0.5f, h * 0.15f)
            close()
        }
        drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        drawCircle(color, 2.dp.toPx(), Offset(w * 0.5f, h * 0.82f))
    }
}

@Composable
fun IconSun(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.8f.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawCircle(color, w * 0.22f, Offset(w * 0.5f, h * 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
        // 8 rays
        for (i in 0 until 8) {
            val angle = i * Math.PI / 4.0
            val x1 = (w * 0.5f + Math.cos(angle) * w * 0.32f).toFloat()
            val y1 = (h * 0.5f + Math.sin(angle) * h * 0.32f).toFloat()
            val x2 = (w * 0.5f + Math.cos(angle) * w * 0.44f).toFloat()
            val y2 = (h * 0.5f + Math.sin(angle) * h * 0.44f).toFloat()
            drawLine(color, Offset(x1, y1), Offset(x2, y2), stroke, StrokeCap.Round)
        }
    }
}

@Composable
fun IconMoon(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.8f.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.65f, h * 0.15f)
            cubicTo(w * 0.30f, h * 0.20f, w * 0.20f, h * 0.60f, w * 0.45f, h * 0.85f)
            cubicTo(w * 0.20f, h * 0.75f, w * 0.20f, h * 0.35f, w * 0.50f, h * 0.20f)
            close()
        }
        drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}

@Composable
fun IconDevice(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 1.8f.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.25f, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(w * 0.50f, h * 0.70f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
        )
        drawLine(color, Offset(w * 0.45f, h * 0.78f), Offset(w * 0.55f, h * 0.78f), stroke * 0.9f, StrokeCap.Round)
    }
}

@Composable
fun IconUmbrella(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 2.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        // Canopy arc
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.15f, h * 0.55f)
            cubicTo(w * 0.15f, h * 0.20f, w * 0.85f, h * 0.20f, w * 0.85f, h * 0.55f)
            close()
        }
        drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        // Pole
        drawLine(color, Offset(w * 0.5f, h * 0.25f), Offset(w * 0.5f, h * 0.80f), stroke, StrokeCap.Round)
        // Handle hook
        val hook = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.80f)
            cubicTo(w * 0.5f, h * 0.90f, w * 0.38f, h * 0.90f, w * 0.38f, h * 0.82f)
        }
        drawPath(hook, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round))
    }
}
