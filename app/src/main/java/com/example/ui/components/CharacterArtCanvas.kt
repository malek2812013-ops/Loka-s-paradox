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
 * Crisp Undertale-style Frisk-proportioned Pixel Portrait for Loka:
 * - Rounded chibi bob chestnut hair with highlights (NO hat)
 * - Eyes grey shaded with cyan (#6B7D85 + #38BDF8)
 * - Grey T-shirt with collar/sleeve shading, beige pants trim, and golden heart necklace
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
        val spriteSize = minOf(w, h) * 0.88f

        if (isFutureTransformed || expression == PortraitExpression.DETERMINED) {
            val auraSize = minOf(w, h) * 0.86f
            drawRect(
                color = SoulSeraphCyan.copy(alpha = if (isFutureTransformed) 0.24f else 0.14f),
                topLeft = Offset(cx - auraSize * 0.5f, cy - auraSize * 0.5f),
                size = SizeCompat(auraSize, auraSize),
                style = Stroke(2.5f)
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
            'O' to Color(0xFF140C09),
            'H' to (if (isSickGlitched) Color(0xFF52525B) else Color(0xFF852B17)),
            'h' to (if (isSickGlitched) Color(0xFFA1A1AA) else Color(0xFFBA472D)),
            'l' to (if (isSickGlitched) Color(0xFFE4E4E7) else Color(0xFFE06D4F)),
            'd' to (if (isSickGlitched) Color(0xFF27272A) else Color(0xFF4E180D)),
            'S' to Color(0xFFF3D2B3),
            's' to Color(0xFFD4A27D),
            'r' to Color(0xFFEC8F87),
            'm' to Color(0xFFA6624B),
            'W' to Color.White,
            'G' to Color(0xFF64748B),
            'C' to (if (isSickGlitched) Color.White else if (expression == PortraitExpression.DETERMINED) Color(0xFF7DD3FC) else Color(0xFF38BDF8)),
            'c' to (if (isSickGlitched) Color(0xFFD4D4D8) else Color(0xFF7DD3FC)),
            'K' to Color(0xFFB0B7C3),
            'T' to Color(0xFF78808C),
            't' to Color(0xFF525864),
            'Y' to SepiaGold,
            'y' to Color(0xFFB45309),
            'P' to Color(0xFFE2CEAB),
            'p' to Color(0xFFB8A07B),
            'B' to Color(0xFF3D271A),
            'b' to Color(0xFF63422D)
        )

        drawPixelMatrix(
            rows = PixelSprites.lokaPortraitGrid,
            palette = palette,
            center = Offset(cx, cy),
            targetSizePx = spriteSize
        )

        // Dynamic Undertale-style expression overlays (aligned to 64x64 portrait grid)
        val u = spriteSize / 64f
        when (expression) {
            PortraitExpression.HAPPY -> {
                drawRect(Color(0xFF140C09), Offset(cx - u * 3.0f, cy - u * 3.8f), SizeCompat(u * 6.0f, u * 1.4f))
                drawPixelSaveStar(Offset(cx + u * 14.5f, cy - u * 14.5f), u * 2.2f, SepiaGold)
            }
            PortraitExpression.SURPRISED -> {
                drawRect(Color.White, Offset(cx - u * 8.5f, cy - u * 10.5f), SizeCompat(u * 2.5f, u * 2.8f))
                drawRect(Color.White, Offset(cx + u * 5.5f, cy - u * 10.5f), SizeCompat(u * 2.5f, u * 2.8f))
                drawRect(Color(0xFF140C09), Offset(cx - u * 1.3f, cy - u * 4.2f), SizeCompat(u * 2.6f, u * 2.6f))
            }
            PortraitExpression.ANGRY -> {
                drawRect(Color(0xFF140C09), Offset(cx - u * 9.5f, cy - u * 13.0f), SizeCompat(u * 5.2f, u * 1.5f))
                drawRect(Color(0xFF140C09), Offset(cx + u * 4.3f, cy - u * 13.0f), SizeCompat(u * 5.2f, u * 1.5f))
                drawRect(Color(0xFFEF4444), Offset(cx + u * 6.2f, cy - u * 9.0f), SizeCompat(u * 1.8f, u * 1.8f))
            }
            PortraitExpression.DETERMINED -> {
                drawPixelSaveStar(Offset(cx + u * 7.0f, cy - u * 9.5f), u * 2.8f, Color(0xFF38BDF8))
            }
            PortraitExpression.NEUTRAL -> Unit
        }
    }
}

/**
 * Full-body Frisk-proportioned standing Loka Canvas used on cutscene stages (e.g. meeting smaller Ashen).
 */
@Composable
fun LokaFullBodySpriteCanvas(
    modifier: Modifier = Modifier,
    isGlitched: Boolean = false,
    expression: PortraitExpression = PortraitExpression.NEUTRAL
) {
    val transition = rememberInfiniteTransition(label = "lokaStageIdle")
    val bob by transition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1150, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob"
    )
    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.52f + bob
        val unit = minOf(size.width, size.height) * 0.31f
        drawLokaOverworldSprite(center = Offset(cx, cy), unit = unit, isGlitched = isGlitched)
        if (expression == PortraitExpression.DETERMINED) {
            drawPixelSaveStar(Offset(cx + unit * 1.15f, cy - unit * 0.9f), unit * 0.22f, Color(0xFF38BDF8))
        }
    }
}

/**
 * Crisp top-down Undertale-style Overworld Pixel Sprite for Frisk-bodied Loka.
 */
fun DrawScope.drawLokaOverworldSprite(
    center: Offset,
    unit: Float,
    isGlitched: Boolean,
    isMoving: Boolean = false,
    isRunning: Boolean = false,
    animClock: Float = 0f,
    facingRight: Boolean = true
) {
    val freq = if (isRunning) 13.5f else 7.5f
    val phase = if (isMoving) (animClock * freq).toInt() % 4 else 0
    val bobOffset = when {
        !isMoving -> 0f
        isRunning -> if (phase % 2 == 1) -unit * 0.19f else 0f
        else -> if (phase % 2 == 1) -unit * 0.08f else 0f
    }

    // Running / walking pixel dust puffs behind Loka's heels
    if (isMoving) {
        val dirSign = if (facingRight) -1f else 1f
        val dustCount = if (isRunning) 3 else 1
        for (i in 0 until dustCount) {
            val puffPhase = ((animClock * (if (isRunning) 5.5f else 3.2f) + i * 0.33f) % 1f)
            val dx = dirSign * unit * (0.65f + puffPhase * (if (isRunning) 1.65f else 0.9f))
            val dy = unit * (1.28f - puffPhase * (if (isRunning) 0.55f else 0.25f))
            val puffSize = unit * (if (isRunning) 0.38f else 0.24f) * (1f - puffPhase * 0.65f)
            val dustColor = if (isGlitched) Color.Cyan else Color(0xFFE2CEAB)
            drawRect(
                color = dustColor.copy(alpha = (1f - puffPhase) * (if (isRunning) 0.72f else 0.42f)),
                topLeft = Offset(center.x + dx - puffSize * 0.5f, center.y + dy - puffSize * 0.5f),
                size = SizeCompat(puffSize, puffSize)
            )
        }
    }

    drawSteppedPixelShadow(
        center = Offset(center.x, center.y + unit * 1.48f),
        widthPx = unit * (if (isRunning && isMoving) 2.30f else 2.15f),
        heightPx = unit * 0.56f
    )

    val palette = mapOf(
        'O' to Color(0xFF140C09),
        'H' to (if (isGlitched) Color(0xFF52525B) else Color(0xFF852B17)),
        'h' to (if (isGlitched) Color(0xFFA1A1AA) else Color(0xFFBA472D)),
        'l' to (if (isGlitched) Color(0xFFE4E4E7) else Color(0xFFE06D4F)),
        'd' to (if (isGlitched) Color(0xFF27272A) else Color(0xFF4E180D)),
        'S' to Color(0xFFF3D2B3),
        's' to Color(0xFFD4A27D),
        'r' to Color(0xFFEC8F87),
        'm' to Color(0xFFA6624B),
        'W' to Color.White,
        'G' to Color(0xFF64748B),
        'C' to (if (isGlitched) Color.White else Color(0xFF38BDF8)),
        'c' to (if (isGlitched) Color(0xFFD4D4D8) else Color(0xFF7DD3FC)),
        'K' to Color(0xFFB0B7C3),
        'T' to Color(0xFF78808C),
        't' to Color(0xFF525864),
        'Y' to SepiaGold,
        'y' to Color(0xFFB45309),
        'P' to Color(0xFFE2CEAB),
        'p' to Color(0xFFB8A07B),
        'B' to Color(0xFF3D271A),
        'b' to Color(0xFF63422D)
    )

    val activeGrid = when {
        !isMoving -> PixelSprites.lokaOverworldGrid
        isRunning -> if (phase % 2 == 0) PixelSprites.lokaRunFrameAGrid else PixelSprites.lokaRunFrameBGrid
        else -> when (phase) {
            0 -> PixelSprites.lokaWalkFrameAGrid
            2 -> PixelSprites.lokaWalkFrameBGrid
            else -> PixelSprites.lokaOverworldGrid
        }
    }

    drawPixelMatrix(
        rows = activeGrid,
        palette = palette,
        center = Offset(center.x, center.y + bobOffset),
        targetSizePx = unit * 3.55f,
        flipX = !facingRight
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
    monochromeBattle: Boolean = false,
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
        val targetPx = minOf(size.width, size.height) * 0.90f

        drawSteppedPixelShadow(
            center = Offset(cx, cy + targetPx * 0.44f),
            widthPx = targetPx * 0.80f,
            heightPx = targetPx * 0.14f,
            color = Color(0x88000000)
        )

        if (isSpareReady || expression == PortraitExpression.HAPPY) {
            drawPixelSaveStar(Offset(cx - targetPx * 0.42f, cy - targetPx * 0.36f), targetPx * 0.08f, Color(0xFFFACC15))
            drawPixelSaveStar(Offset(cx + targetPx * 0.42f, cy - targetPx * 0.36f), targetPx * 0.08f, Color(0xFFFACC15))
        } else if (expression == PortraitExpression.ANGRY) {
            drawRect(Color.Red.copy(alpha = 0.25f), Offset(cx - targetPx * 0.48f, cy - targetPx * 0.48f), SizeCompat(targetPx * 0.96f, targetPx * 0.96f), style = Stroke(2f))
        }

        drawUndertaleBossPixelSprite(
            enemyId = enemyId,
            center = Offset(cx, cy),
            sizePx = targetPx,
            isHitFlash = isHitFlash,
            isSpareReady = isSpareReady,
            monochromeBattle = monochromeBattle
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
        val sr = sizePx * 1.85f
        val st = (sr * 0.14f).coerceAtLeast(2.5f)
        val shieldCol = SoulType.GREEN.secondaryColor ?: SoulType.GREEN.color
        drawSteppedPixelShadow(center = center, widthPx = sr * 2.0f, heightPx = sr * 2.0f, color = SoulType.GREEN.color.copy(alpha = 0.20f))
        // Crisp 1-pixel stepped octagon frame
        drawRect(shieldCol, Offset(center.x - sr * 0.65f, center.y - sr), SizeCompat(sr * 1.30f, st))
        drawRect(shieldCol, Offset(center.x - sr * 0.65f, center.y + sr - st), SizeCompat(sr * 1.30f, st))
        drawRect(shieldCol, Offset(center.x - sr, center.y - sr * 0.65f), SizeCompat(st, sr * 1.30f))
        drawRect(shieldCol, Offset(center.x + sr - st, center.y - sr * 0.65f), SizeCompat(st, sr * 1.30f))
        drawRect(shieldCol, Offset(center.x - sr * 0.85f, center.y - sr * 0.85f), SizeCompat(sr * 0.25f, sr * 0.25f))
        drawRect(shieldCol, Offset(center.x + sr * 0.60f, center.y - sr * 0.85f), SizeCompat(sr * 0.25f, sr * 0.25f))
        drawRect(shieldCol, Offset(center.x - sr * 0.85f, center.y + sr * 0.60f), SizeCompat(sr * 0.25f, sr * 0.25f))
        drawRect(shieldCol, Offset(center.x + sr * 0.60f, center.y + sr * 0.60f), SizeCompat(sr * 0.25f, sr * 0.25f))
    }

    // Olive Soul visibly larger hitbox stepped pixel frame
    if (soul == SoulType.GREEN) {
        val hr = sizePx * soul.hitboxScale * 1.15f
        val ht = 2.5f
        val oliveCol = (soul.secondaryColor ?: soul.color).copy(alpha = 0.72f)
        drawRect((soul.secondaryColor ?: soul.color).copy(alpha = 0.16f), Offset(center.x - hr, center.y - hr), SizeCompat(hr * 2f, hr * 2f))
        drawRect(oliveCol, Offset(center.x - hr, center.y - hr), SizeCompat(hr * 2f, ht))
        drawRect(oliveCol, Offset(center.x - hr, center.y + hr - ht), SizeCompat(hr * 2f, ht))
        drawRect(oliveCol, Offset(center.x - hr, center.y - hr), SizeCompat(ht, hr * 2f))
        drawRect(oliveCol, Offset(center.x + hr - ht, center.y - hr), SizeCompat(ht, hr * 2f))
    }

    val alpha = if (isInvulnerable) blinkAlpha else 1f
    val outlineColor = if (soul == SoulType.WHITE_SICK) {
        Color(0xFF6B7280)
    } else {
        Color(0xFF0A0A0D)
    }

    if (soul == SoulType.SPLIT_DUO) {
        drawSteppedPixelShadow(
            center = center,
            widthPx = sizePx * 2.6f,
            heightPx = sizePx * 2.2f,
            color = Color.White.copy(alpha = 0.16f * alpha)
        )
        drawSplitPixelHeart(
            center = center,
            sizePx = sizePx,
            leftColor = soul.color,
            rightColor = soul.secondaryColor ?: Color(0xFF2DD4BF),
            outlineColor = outlineColor,
            splitHalfIndex = splitHalfIndex,
            alpha = alpha
        )
    } else {
        drawPixelHeart(
            center = center,
            sizePx = sizePx * 1.08f,
            fillColor = Color.White,
            outlineColor = Color.White,
            cracked = false,
            alpha = 0.55f * alpha
        )
        drawPixelHeart(
            center = center,
            sizePx = sizePx,
            fillColor = soul.color,
            outlineColor = outlineColor,
            cracked = soul == SoulType.WHITE_SICK,
            alpha = alpha
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
        val spriteSize = minOf(w, h) * 0.90f

        // Stepped pixel soil shadow beneath Ashen's roots
        drawSteppedPixelShadow(
            center = Offset(w * 0.5f, h * 0.85f),
            widthPx = w * 0.56f,
            heightPx = h * 0.14f,
            color = Color(0xFF1A1A20)
        )

        val palette = mapOf(
            'O' to Color(0xFF09090B),
            'B' to Color(0xFF09090B),
            'D' to Color(0xFF3F3F46),
            'G' to Color(0xFF71717A),
            'P' to Color(0xFFD4D4D8),
            'W' to Color(0xFFF4F4F5)
        )

        val useCreepy = isCreepySmirk || expression == PortraitExpression.ANGRY || expression == PortraitExpression.DETERMINED
        drawPixelMatrix(
            rows = if (useCreepy) PixelSprites.ashenFlowerCreepy else PixelSprites.ashenFlowerNormal,
            palette = palette,
            center = Offset(cx, cy),
            targetSizePx = spriteSize
        )

        val u = spriteSize / 32f
        when (expression) {
            PortraitExpression.HAPPY -> {
                drawRect(Color(0xFF09090B), Offset(cx - u * 2.0f, cy - u * 2.2f), SizeCompat(u * 4.0f, u * 0.9f))
            }
            PortraitExpression.SURPRISED -> {
                drawRect(Color.White, Offset(cx - u * 3.2f, cy - u * 6.0f), SizeCompat(u * 1.7f, u * 2.0f))
                drawRect(Color.White, Offset(cx + u * 1.5f, cy - u * 6.0f), SizeCompat(u * 1.7f, u * 2.0f))
            }
            else -> Unit
        }
    }
}
