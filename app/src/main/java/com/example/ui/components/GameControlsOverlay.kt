package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ControlScheme
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Hand-drawn Control Styles from Image 6:
 * - Style 1 (`DPAD_8WAY`): 3x3 square directional arrow grid (`↖ ↑ ↗ / ← ○ → / ↙ ↓ ↘`)
 * - Style 2 (`JOYSTICK`): Segmented circular analog wheel with draggable center knob
 */
@Composable
fun ControllerPadOverlay(
    controlScheme: ControlScheme,
    accentColor: Color = Color.White,
    padSize: Dp = 120.dp,
    onDirectionChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    when (controlScheme) {
        ControlScheme.DPAD_8WAY -> EightWayGridPad(
            accentColor = accentColor,
            padSize = padSize,
            onDirectionChanged = onDirectionChanged,
            modifier = modifier
        )

        ControlScheme.JOYSTICK -> SegmentedWheelJoystick(
            accentColor = accentColor,
            padSize = padSize,
            onDirectionChanged = onDirectionChanged,
            modifier = modifier
        )

        ControlScheme.TOUCH_DRAG -> Unit
    }
}

@Composable
private fun EightWayGridPad(
    accentColor: Color,
    padSize: Dp,
    onDirectionChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Box(
        modifier = modifier
            .size(padSize)
            .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(4.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
            .padding(4.dp)
            .testTag("control_pad_8way")
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()

                    fun updateCell(pos: Offset) {
                        val col = ((pos.x / size.width.toFloat()) * 3f).toInt().coerceIn(0, 2)
                        val row = ((pos.y / size.height.toFloat()) * 3f).toInt().coerceIn(0, 2)
                        val dx = (col - 1).toFloat()
                        val dy = (row - 1).toFloat()
                        activeCell = row to col
                        if (dx == 0f && dy == 0f) {
                            onDirectionChanged(0f, 0f)
                        } else {
                            val len = hypot(dx, dy).coerceAtLeast(1f)
                            onDirectionChanged(dx / len, dy / len)
                        }
                    }

                    updateCell(down.position)
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            change.consume()
                            updateCell(change.position)
                        }
                    } while (event.changes.any { it.pressed })

                    activeCell = null
                    onDirectionChanged(0f, 0f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gap = 3.dp.toPx()
            val cellW = (size.width - gap * 2f) / 3f
            val cellH = (size.height - gap * 2f) / 3f

            for (row in 0..2) {
                for (col in 0..2) {
                    val left = col * (cellW + gap)
                    val top = row * (cellH + gap)
                    val cx = left + cellW * 0.5f
                    val cy = top + cellH * 0.5f
                    val isCenter = row == 1 && col == 1
                    val isSelected = activeCell == (row to col)

                    if (isCenter) {
                        // Drawn center circle `○` from Image 6 Style 1
                        drawCircle(
                            color = if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Black,
                            radius = minOf(cellW, cellH) * 0.36f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = if (isSelected) accentColor else Color.White,
                            radius = minOf(cellW, cellH) * 0.36f,
                            center = Offset(cx, cy),
                            style = Stroke(2.2f)
                        )
                    } else {
                        // Square box with triangular directional arrow inside
                        drawRect(
                            color = if (isSelected) accentColor else Color(0xFF101016),
                            topLeft = Offset(left, top),
                            size = Size(cellW, cellH)
                        )
                        drawRect(
                            color = if (isSelected) Color.White else accentColor.copy(alpha = 0.85f),
                            topLeft = Offset(left, top),
                            size = Size(cellW, cellH),
                            style = Stroke(2f)
                        )

                        val dx = (col - 1).toFloat()
                        val dy = (row - 1).toFloat()
                        val angle = atan2(dy, dx)
                        val r = minOf(cellW, cellH) * 0.28f
                        val tip = Offset(cx + cos(angle) * r, cy + sin(angle) * r)
                        val baseLeft = Offset(cx + cos(angle + 2.35f) * r * 0.92f, cy + sin(angle + 2.35f) * r * 0.92f)
                        val baseRight = Offset(cx + cos(angle - 2.35f) * r * 0.92f, cy + sin(angle - 2.35f) * r * 0.92f)

                        val tri = Path().apply {
                            moveTo(tip.x, tip.y)
                            lineTo(baseLeft.x, baseLeft.y)
                            lineTo(baseRight.x, baseRight.y)
                            close()
                        }
                        drawPath(
                            path = tri,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedWheelJoystick(
    accentColor: Color,
    padSize: Dp,
    onDirectionChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobNorm by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(padSize)
            .testTag("control_pad_joystick")
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()

                    fun updateKnob(pos: Offset) {
                        val center = Offset(size.width * 0.5f, size.height * 0.5f)
                        val maxR = minOf(size.width, size.height) * 0.40f
                        val dx = pos.x - center.x
                        val dy = pos.y - center.y
                        val dist = hypot(dx, dy)
                        if (dist <= 3f) {
                            knobNorm = Offset.Zero
                            onDirectionChanged(0f, 0f)
                        } else {
                            val clamped = dist.coerceAtMost(maxR)
                            val nx = (dx / dist) * (clamped / maxR)
                            val ny = (dy / dist) * (clamped / maxR)
                            knobNorm = Offset(nx, ny)
                            onDirectionChanged(nx, ny)
                        }
                    }

                    updateKnob(down.position)
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            change.consume()
                            updateKnob(change.position)
                        }
                    } while (event.changes.any { it.pressed })

                    knobNorm = Offset.Zero
                    onDirectionChanged(0f, 0f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width * 0.5f, size.height * 0.5f)
            val outerR = minOf(size.width, size.height) * 0.46f
            val innerR = outerR * 0.78f

            // Outer rim circle (matching Image 6 Style 2)
            drawCircle(color = Color.Black.copy(alpha = 0.76f), radius = outerR, center = c)
            drawCircle(color = Color.White, radius = outerR, center = c, style = Stroke(2.5f))
            drawCircle(color = accentColor.copy(alpha = 0.35f), radius = innerR, center = c, style = Stroke(2f))

            // 6 segmented wheel spokes matching the hand-drawn spoked wheel!
            for (i in 0 until 6) {
                val ang = (i * (PI / 3.0) + PI / 6.0).toFloat()
                val start = Offset(c.x + cos(ang) * (outerR * 0.20f), c.y + sin(ang) * (outerR * 0.20f))
                val end = Offset(c.x + cos(ang) * innerR, c.y + sin(ang) * innerR)
                drawLine(Color.White.copy(alpha = 0.55f), start, end, strokeWidth = 2.4f)
            }

            // Movable center analog thumb circle
            val knobCenter = Offset(
                c.x + knobNorm.x * outerR * 0.52f,
                c.y + knobNorm.y * outerR * 0.52f
            )
            drawCircle(color = accentColor.copy(alpha = 0.90f), radius = outerR * 0.24f, center = knobCenter)
            drawCircle(color = Color.White, radius = outerR * 0.24f, center = knobCenter, style = Stroke(2.5f))
        }
    }
}
