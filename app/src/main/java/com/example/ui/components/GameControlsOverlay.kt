package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
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
 * Upgraded Retro Undertale-Framed Control Styles:
 * - `DPAD_8WAY`: Crisp double-beveled 3x3 directional grid (`↖ ↑ ↗ / ← ◆ → / ↙ ↓ ↘`) with corner rivets & active glow pulse
 * - `JOYSTICK`: Spoked pixel compass wheel with directional vector beam & glowing dual-ring Soul thumb core
 */
@Composable
fun ControllerPadOverlay(
    controlScheme: ControlScheme,
    accentColor: Color = Color.White,
    padSize: Dp = 126.dp,
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
            .background(Color(0xD8050508), RoundedCornerShape(0.dp))
            .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(0.dp))
            .padding(2.dp)
            .border(1.5.dp, accentColor.copy(alpha = 0.72f), RoundedCornerShape(0.dp))
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
            val rivet = 2.5.dp.toPx()

            // Corner pixel rivets on pad housing
            drawRect(accentColor, Offset.Zero, Size(rivet, rivet))
            drawRect(accentColor, Offset(size.width - rivet, 0f), Size(rivet, rivet))
            drawRect(accentColor, Offset(0f, size.height - rivet), Size(rivet, rivet))
            drawRect(accentColor, Offset(size.width - rivet, size.height - rivet), Size(rivet, rivet))

            for (row in 0..2) {
                for (col in 0..2) {
                    val left = col * (cellW + gap)
                    val top = row * (cellH + gap)
                    val cx = left + cellW * 0.5f
                    val cy = top + cellH * 0.5f
                    val isCenter = row == 1 && col == 1
                    val isSelected = activeCell == (row to col)
                    val isCardinal = (row == 1) xor (col == 1)

                    if (isCenter) {
                        // Retro Soul Emblem Pivot Cell
                        drawRect(
                            color = if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0xFF0A0A10),
                            topLeft = Offset(left, top),
                            size = Size(cellW, cellH)
                        )
                        drawRect(
                            color = if (isSelected) Color.White else accentColor.copy(alpha = 0.65f),
                            topLeft = Offset(left, top),
                            size = Size(cellW, cellH),
                            style = Stroke(1.8f)
                        )
                        val r = minOf(cellW, cellH) * 0.30f
                        val diamond = Path().apply {
                            moveTo(cx, cy - r)
                            lineTo(cx + r, cy)
                            lineTo(cx, cy + r)
                            lineTo(cx - r, cy)
                            close()
                        }
                        drawPath(diamond, color = if (activeCell != null) accentColor else Color.White.copy(alpha = 0.85f))
                        drawRect(Color.White, Offset(cx - r * 0.25f, cy - r * 0.25f), Size(r * 0.5f, r * 0.5f))
                    } else {
                        val boxBg = when {
                            isSelected -> accentColor
                            isCardinal -> Color(0xFF151622)
                            else -> Color(0xFF0D0E16)
                        }
                        drawRect(
                            color = boxBg,
                            topLeft = Offset(left, top),
                            size = Size(cellW, cellH)
                        )
                        // Pixel top-left highlight bevel inside unpressed pad cells
                        if (!isSelected) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.22f),
                                start = Offset(left + 1.5f, top + 1.5f),
                                end = Offset(left + cellW - 1.5f, top + 1.5f),
                                strokeWidth = 1.5f
                            )
                        }
                        drawRect(
                            color = if (isSelected) Color.White else accentColor.copy(alpha = if (isCardinal) 0.90f else 0.58f),
                            topLeft = Offset(left, top),
                            size = Size(cellW, cellH),
                            style = Stroke(if (isSelected) 2.6f else 1.8f)
                        )

                        val dx = (col - 1).toFloat()
                        val dy = (row - 1).toFloat()
                        val angle = atan2(dy, dx)
                        val r = minOf(cellW, cellH) * (if (isCardinal) 0.34f else 0.28f)
                        val tip = Offset(cx + cos(angle) * r, cy + sin(angle) * r)
                        val baseLeft = Offset(cx + cos(angle + 2.30f) * r * 0.92f, cy + sin(angle + 2.30f) * r * 0.92f)
                        val baseRight = Offset(cx + cos(angle - 2.30f) * r * 0.92f, cy + sin(angle - 2.30f) * r * 0.92f)

                        // Subtle dark drop shadow behind arrow for higher clarity
                        if (!isSelected) {
                            val shadowTri = Path().apply {
                                moveTo(tip.x + 1.5f, tip.y + 1.5f)
                                lineTo(baseLeft.x + 1.5f, baseLeft.y + 1.5f)
                                lineTo(baseRight.x + 1.5f, baseRight.y + 1.5f)
                                close()
                            }
                            drawPath(path = shadowTri, color = Color.Black.copy(alpha = 0.7f))
                        }

                        val tri = Path().apply {
                            moveTo(tip.x, tip.y)
                            lineTo(baseLeft.x, baseLeft.y)
                            lineTo(baseRight.x, baseRight.y)
                            close()
                        }
                        drawPath(
                            path = tri,
                            color = if (isSelected) Color.Black else if (isCardinal) Color.White else Color.White.copy(alpha = 0.88f)
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
            .background(Color(0xAA050508), RoundedCornerShape(0.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(0.dp))
            .padding(3.dp)
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
            val innerR = outerR * 0.74f
            val isPressed = hypot(knobNorm.x, knobNorm.y) > 0.05f

            // Double-bevel compass ring housing
            drawCircle(color = Color(0xD908080E), radius = outerR, center = c)
            drawCircle(
                color = if (isPressed) Color.White else Color.White.copy(alpha = 0.88f),
                radius = outerR,
                center = c,
                style = Stroke(2.6f)
            )
            drawCircle(
                color = accentColor.copy(alpha = if (isPressed) 0.78f else 0.45f),
                radius = innerR,
                center = c,
                style = Stroke(2f)
            )

            // 8 directional pixel compass ticks along outer rim
            for (i in 0 until 8) {
                val ang = (i * (PI / 4.0)).toFloat()
                val start = Offset(c.x + cos(ang) * (innerR * 0.90f), c.y + sin(ang) * (innerR * 0.90f))
                val end = Offset(c.x + cos(ang) * (outerR * 0.96f), c.y + sin(ang) * (outerR * 0.96f))
                drawLine(
                    color = if (i % 2 == 0) accentColor.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.45f),
                    start = start,
                    end = end,
                    strokeWidth = if (i % 2 == 0) 2.5f else 1.6f
                )
            }

            // Movable dual-ring Soul thumb core with active vector tether
            val knobCenter = Offset(
                c.x + knobNorm.x * outerR * 0.52f,
                c.y + knobNorm.y * outerR * 0.52f
            )
            if (isPressed) {
                drawLine(
                    color = accentColor.copy(alpha = 0.75f),
                    start = c,
                    end = knobCenter,
                    strokeWidth = 3f
                )
            }
            drawCircle(
                color = accentColor.copy(alpha = if (isPressed) 0.96f else 0.80f),
                radius = outerR * 0.27f,
                center = knobCenter
            )
            drawCircle(
                color = Color.White,
                radius = outerR * 0.27f,
                center = knobCenter,
                style = Stroke(2.5f)
            )
            val core = outerR * 0.09f
            drawRect(
                color = Color.White,
                topLeft = Offset(knobCenter.x - core * 0.5f, knobCenter.y - core * 0.5f),
                size = Size(core, core)
            )
        }
    }
}
