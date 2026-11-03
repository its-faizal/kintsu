package com.kintsu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kintsu.app.ui.theme.KintsuTheme
import kotlin.math.hypot

@Composable
fun VisualPatternCanvas(
    gridSize: Int = 3,
    selectedNodes: List<Int>,
    onNodeSelected: (Int) -> Unit,
    onDragStarted: () -> Unit,
    onDragEnded: () -> Unit,
    canvasSize: Dp = 280.dp,
    stealthMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = KintsuTheme.colors
    var currentDragPosition by remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = modifier
            .size(canvasSize)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .pointerInput(gridSize) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDragStarted()
                        currentDragPosition = offset
                        val node = findPatternNode(offset, size.width.toFloat(), size.height.toFloat(), gridSize)
                        if (node != -1) {
                            onNodeSelected(node)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentDragPosition = change.position
                        val node = findPatternNode(change.position, size.width.toFloat(), size.height.toFloat(), gridSize)
                        if (node != -1 && !selectedNodes.contains(node)) {
                            onNodeSelected(node)
                        }
                    },
                    onDragEnd = {
                        currentDragPosition = null
                        onDragEnded()
                    },
                    onDragCancel = {
                        currentDragPosition = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.size(canvasSize)) {
            val colStep = size.width / gridSize
            val rowStep = size.height / gridSize

            // 1. Draw connected path lines (only when stealth mode is OFF)
            if (!stealthMode && selectedNodes.size > 1) {
                for (i in 0 until selectedNodes.size - 1) {
                    val from = selectedNodes[i]
                    val to = selectedNodes[i + 1]
                    val fromCenter = Offset(
                        (from % gridSize) * colStep + colStep / 2,
                        (from / gridSize) * rowStep + rowStep / 2
                    )
                    val toCenter = Offset(
                        (to % gridSize) * colStep + colStep / 2,
                        (to / gridSize) * rowStep + rowStep / 2
                    )
                    drawLine(
                        color = colors.goldSeam,
                        start = fromCenter,
                        end = toCenter,
                        strokeWidth = if (gridSize == 5) 3.dp.toPx() else 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2. Draw live line from last selected node to finger touch (only when stealth mode is OFF)
            if (!stealthMode && currentDragPosition != null && selectedNodes.isNotEmpty()) {
                val last = selectedNodes.last()
                val lastCenter = Offset(
                    (last % gridSize) * colStep + colStep / 2,
                    (last / gridSize) * rowStep + rowStep / 2
                )
                drawLine(
                    color = colors.goldSeam.copy(alpha = 0.75f),
                    start = lastCenter,
                    end = currentDragPosition!!,
                    strokeWidth = if (gridSize == 5) 2.5.dp.toPx() else 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3. Draw nodes (bright, clean, tight halo to prevent over-expansion)
            for (row in 0 until gridSize) {
                for (col in 0 until gridSize) {
                    val nodeIndex = row * gridSize + col
                    val center = Offset(col * colStep + colStep / 2, row * rowStep + rowStep / 2)
                    val isSelected = selectedNodes.contains(nodeIndex)

                    val outerRadius = if (gridSize == 5) colStep * 0.22f else colStep * 0.22f
                    val dotRadius = if (gridSize == 5) {
                        if (isSelected && !stealthMode) 5.dp.toPx() else 3.5.dp.toPx()
                    } else {
                        if (isSelected && !stealthMode) 7.5.dp.toPx() else 5.dp.toPx()
                    }

                    if (isSelected && !stealthMode) {
                        // Tight outer glowing aura (controlled radius, no over-expanding)
                        drawCircle(
                            color = colors.goldSeam.copy(alpha = 0.22f),
                            radius = outerRadius,
                            center = center
                        )
                        // Tight outer ring stroke
                        drawCircle(
                            color = colors.goldSeam,
                            radius = outerRadius,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Bright center dot
                    drawCircle(
                        color = if (isSelected && !stealthMode) colors.goldSeam else colors.textSecondary.copy(alpha = 0.85f),
                        radius = dotRadius,
                        center = center
                    )
                }
            }
        }
    }
}

fun findPatternNode(pos: Offset, width: Float, height: Float, gridSize: Int): Int {
    val colStep = width / gridSize
    val rowStep = height / gridSize
    val touchRadius = colStep * 0.44f

    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            val center = Offset(col * colStep + colStep / 2, row * rowStep + rowStep / 2)
            val dist = hypot(pos.x - center.x, pos.y - center.y)
            if (dist <= touchRadius) {
                return row * gridSize + col
            }
        }
    }
    return -1
}
