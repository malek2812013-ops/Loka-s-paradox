package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.EnemyId
import com.example.model.PortraitExpression
import com.example.model.SoulType
import com.example.ui.theme.SepiaBronze
import com.example.ui.theme.SepiaGold
import com.example.ui.theme.SoulSeraphCyan
import kotlin.math.cos
import kotlin.math.sin

/**
 * Crisp Undertale-style Pixel Portrait for Loka:
 * - Shaggy Clover-style chestnut hair, NO hat
 * - Eyes grey shaded with cyan (#6B7D85 + #38BDF8)
 * - Grey T-shirt, beige pants, and golden heart necklace
 * - Supports dynamic facial expressions (HAPPY, SURPRISED, ANGRY, DETERMINED)
 */
@Composable
fun LokaPortraitCanvas(
    modifier: Modifier = Modifier,
    saveCount: Int = 8,
    isSickGlitched: Boolean = false,
    isFutureTransformed: Boolean = false,
    expression: PortraitExpression = PortraitExpression.NEUTRAL
) {
    val transition = rememberInfiniteTransition(label = "lokaPixelIdle")
    val bob by transition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "orbit"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f + bob
        val spriteSize = minOf(w, h) * 0.86f

        if (isFutureTransformed || expression == PortraitExpression.DETERMINED) {
            drawCircle(
                color = SoulSeraphCyan.copy(alpha = if (isFutureTransformed) 0.22f else 0.14f),
                radius = minOf(w, h) * 0.45f,
                center = Offset(cx, cy)
            )
        }

        if (saveCount > 0) {
            for (i in 0 until 8) {
                val angleRad = Math.toRadians((orbit + i * 45.0))
                val r = minOf(w, h) * 0.43f
                val sx = cx + (cos(angleRad) * r).toFloat()
                val sy = cy + (sin(angleRad) * r * 0.62f).toFloat()
                val active = i < saveCount
                drawPixelSaveStar(
                    center = Offset(sx, sy),
                    sizePx = if (active) spriteSize * 0.08f else spriteSize * 0.045f,
                    color = if (isSickGlitched) Color.White.copy(alpha = 0.6f)
                    else if (active) SepiaGold else SepiaBronze.copy(alpha = 0.35f)
                )
            }
        }

        val palette = mapOf(
            'O' to Color(0xFF140E0A),
            'H' to Color(0xFF8C2D19),
            'h' to Color(0xFFC9442B),
            'S' to Color(0xFFEBC7A8),
            'G' to Color(0xFF6B7D85),
            'C' to (if (isSickGlitched) Color.White else if (expression == PortraitExpression.DETERMINED) Color(0xFF7DD3FC) else Color(0xFF38BDF8)),
            'T' to Color(0xFF747982),
            'Y' to SepiaGold,
            'P' to Color(0xFFD6C29F)
        )

        drawPixelMatrix(
            rows = PixelSprites.lokaPortraitGrid,
            palette = palette,
            center = Offset(cx, cy),
            targetSizePx = spriteSize
        )

        // Dynamic Undertale-style expression overlays
        val u = spriteSize / 18f
        when (expression) {
            PortraitExpression.HAPPY -> {
                // Warm small smile & cheerful sparkle
                drawRect(Color(0xFF140E0A), Offset(cx - u * 0.8f, cy + u * 1.4f), SizeCompat(u * 2.2f, u * 0.7f))
                drawPixelSaveStar(Offset(cx + u * 4.8f, cy - u * 4.5f), u * 1.0f, SepiaGold)
            }
            PortraitExpression.SURPRISED -> {
                // Wide shocked eye highlight & open O mouth
                drawRect(Color.White, Offset(cx + u * 2.1f, cy - u * 1.3f), SizeCompat(u * 1.3f, u * 1.5f))
                drawRect(Color(0xFF140E0A), Offset(cx + u * 0.2f, cy + u * 1.5f), SizeCompat(u * 1.3f, u * 1.3f))
            }
            PortraitExpression.ANGRY -> {
                // Furrowed diagonal brow & intense glare
                drawLine(Color(0xFF140E0A), Offset(cx + u * 1.1f, cy - u * 2.0f), Offset(cx + u * 3.8f, cy - u * 1.1f), strokeWidth = u * 0.9f)
                drawRect(Color(0xFFEF4444), Offset(cx + u * 2.4f, cy - u * 0.7f), SizeCompat(u * 0.9f, u * 0.9f))
            }
            PortraitExpression.DETERMINED -> {
                // Glowing cyan determination eye flare
                drawCircle(Color(0xFF38BDF8).copy(alpha = 0.55f), radius = u * 2.0f, center = Offset(cx + u * 2.7f, cy - u * 0.5f))
                drawPixelSaveStar(Offset(cx + u * 2.7f, cy - u * 0.5f), u * 1.2f, Color(0xFF38BDF8))
            }
            PortraitExpression.NEUTRAL -> Unit
        }
    }
}

/**
 * Crisp top-down Undertale-style Overworld Pixel Sprite for Loka.
 */
fun DrawScope.drawLokaOverworldSprite(
    center: Offset,
    unit: Float,
    isGlitched: Boolean
) {
    // Pixel floor shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.42f),
        topLeft = Offset(center.x - unit * 0.95f, center.y + unit * 0.85f),
        size = SizeCompat(unit * 1.9f, unit * 0.65f)
    )

    val palette = mapOf(
        'O' to Color(0xFF120C08),
        'H' to Color(0xFF8C2D19),
        'h' to Color(0xFFC9442B),
        'S' to Color(0xFFEBC7A8),
        'G' to Color(0xFF6B7D85),
        'C' to (if (isGlitched) Color.White else Color(0xFF38BDF8)),
        'T' to Color(0xFF747982),
        'Y' to SepiaGold,
        'P' to Color(0xFFD6C29F),
        'B' to Color(0xFF3A271B)
    )

    drawPixelMatrix(
        rows = PixelSprites.lokaOverworldGrid,
        palette = palette,
        center = center,
        targetSizePx = unit * 2.75f
    )
}

private fun SizeCompat(w: Float, h: Float) = androidx.compose.ui.geometry.Size(w, h)

/**
 * Undertale-style animated Pixel Art Battle Sprite for every boss with dynamic expressions.
 */
@Composable
fun BossSpriteCanvas(
    enemyId: EnemyId,
    isHitFlash: Boolean,
    isSpareReady: Boolean,
    expression: PortraitExpression = PortraitExpression.NEUTRAL,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "bossPixelSprite")
    val stepBob by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "stepBob"
    )
    val jitter by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(140, easing = LinearEasing), RepeatMode.Reverse),
        label = "jitter"
    )

    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f + (if (isHitFlash || expression == PortraitExpression.SURPRISED) jitter * 7f else 0f)
        val cy = size.height * 0.5f + stepBob
        val targetPx = minOf(size.width, size.height) * 0.92f

        if (isSpareReady || expression == PortraitExpression.HAPPY) {
            drawPixelSaveStar(Offset(cx - targetPx * 0.42f, cy - targetPx * 0.36f), targetPx * 0.08f, Color(0xFFFACC15))
            drawPixelSaveStar(Offset(cx + targetPx * 0.42f, cy - targetPx * 0.36f), targetPx * 0.08f, Color(0xFFFACC15))
        } else if (expression == PortraitExpression.ANGRY) {
            drawCircle(Color.Red.copy(alpha = 0.16f), radius = targetPx * 0.48f, center = Offset(cx, cy))
        }

        drawUndertaleBossPixelSprite(
            enemyId = enemyId,
            center = Offset(cx, cy),
            sizePx = targetPx,
            isHitFlash = isHitFlash,
            isSpareReady = isSpareReady
        )
    }
}

fun DrawScope.drawSoulEmblem(
    center: Offset,
    sizePx: Float,
    soul: SoulType,
    isInvulnerable: Boolean,
    blinkAlpha: Float,
    hasGreenShield: Boolean,
    splitHalfIndex: Int? = null
) {
    if (hasGreenShield) {
        drawCircle(
            color = SoulType.GREEN.color.copy(alpha = 0.24f),
            radius = sizePx * 1.85f,
            center = center
        )
        drawCircle(
            color = SoulType.GREEN.secondaryColor ?: SoulType.GREEN.color,
            radius = sizePx * 1.85f,
            center = center,
            style = Stroke(width = 2.8f)
        )
    }

    // Olive Soul visibly larger hitbox ring (matching drawing 5)
    if (soul == SoulType.GREEN) {
        drawCircle(
            color = (soul.secondaryColor ?: soul.color).copy(alpha = 0.18f),
            radius = sizePx * soul.hitboxScale * 1.15f,
            center = center
        )
        drawCircle(
            color = (soul.secondaryColor ?: soul.color).copy(alpha = 0.65f),
            radius = sizePx * soul.hitboxScale * 1.15f,
            center = center,
            style = Stroke(width = 2f)
        )
    }

    val alpha = if (isInvulnerable) blinkAlpha else 1f
    val outlineColor = if (soul == SoulType.WHITE_SICK) {
        Color(0xFF6B7280).copy(alpha = alpha)
    } else {
        Color(0xFF0A0A0D).copy(alpha = alpha)
    }

    if (soul == SoulType.SPLIT_DUO) {
        drawCircle(color = Color.White.copy(alpha = 0.18f * alpha), radius = sizePx * 1.35f, center = center)
        drawSplitPixelHeart(
            center = center,
            sizePx = sizePx,
            leftColor = soul.color.copy(alpha = alpha),
            rightColor = (soul.secondaryColor ?: Color(0xFF2DD4BF)).copy(alpha = alpha),
            outlineColor = outlineColor,
            splitHalfIndex = splitHalfIndex
        )
    } else {
        drawPixelHeart(
            center = center,
            sizePx = sizePx * 1.08f,
            fillColor = Color.White.copy(alpha = 0.55f * alpha),
            outlineColor = Color.White.copy(alpha = 0.55f * alpha),
            cracked = false
        )
        drawPixelHeart(
            center = center,
            sizePx = sizePx,
            fillColor = soul.color.copy(alpha = alpha),
            outlineColor = outlineColor,
            cracked = soul == SoulType.WHITE_SICK
        )
    }
}

fun DrawScope.drawSaveStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    drawPixelSaveStar(
        center = center,
        sizePx = radius,
        color = color
    )
}

/**
 * Undertale-style Pixel Flower Sprite for Ashen (strictly black, charcoal, and grey palette).
 * Supports HAPPY / SURPRISED / ANGRY / DETERMINED expressions.
 */
@Composable
fun AshenFlowerCanvas(
    isCreepySmirk: Boolean = false,
    expression: PortraitExpression = PortraitExpression.NEUTRAL,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "ashenPixelFlower")
    val sway by transition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "sway"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f + sway
        val cy = h * 0.5f
        val spriteSize = minOf(w, h) * 0.88f

        drawOval(
            color = Color(0xFF1F1F23),
            topLeft = Offset(w * 0.22f, h * 0.78f),
            size = SizeCompat(w * 0.56f, h * 0.14f)
        )

        val palette = mapOf(
            'O' to Color(0xFF09090B),
            'B' to Color(0xFF09090B),
            'D' to Color(0xFF3F3F46),
            'G' to Color(0xFF71717A),
            'P' to Color(0xFFD4D4D8)
        )

        val useCreepy = isCreepySmirk || expression == PortraitExpression.ANGRY || expression == PortraitExpression.DETERMINED
        drawPixelMatrix(
            rows = if (useCreepy) PixelSprites.ashenFlowerCreepy else PixelSprites.ashenFlowerNormal,
            palette = palette,
            center = Offset(cx, cy),
            targetSizePx = spriteSize
        )

        val u = spriteSize / 16f
        when (expression) {
            PortraitExpression.HAPPY -> {
                drawRect(Color(0xFF09090B), Offset(cx - u * 1.2f, cy - u * 0.4f), SizeCompat(u * 2.4f, u * 0.6f))
            }
            PortraitExpression.SURPRISED -> {
                drawCircle(Color.White, radius = u * 0.75f, center = Offset(cx - u * 1.4f, cy - u * 1.8f))
                drawCircle(Color.White, radius = u * 0.75f, center = Offset(cx + u * 1.4f, cy - u * 1.8f))
            }
            else -> Unit
        }
    }
}
