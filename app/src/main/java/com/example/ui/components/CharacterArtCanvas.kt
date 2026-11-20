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
            'O' to Color(0xFF140E0A),
            'H' to Color(0xFF7C2614),
            'h' to Color(0xFFB8452A),
            'd' to Color(0xFF4E170C),
            'S' to Color(0xFFEBC7A8),
            's' to Color(0xFFD2A37E),
            'r' to Color(0xFFE89A8A),
            'W' to Color.White,
            'G' to Color(0xFF6B7D85),
            'C' to (if (isSickGlitched) Color.White else if (expression == PortraitExpression.DETERMINED) Color(0xFF7DD3FC) else Color(0xFF38BDF8)),
            'l' to Color(0xFF9CA3AF),
            'T' to Color(0xFF747982),
            't' to Color(0xFF52565E),
            'Y' to SepiaGold,
            'y' to Color(0xFFB45309),
            'P' to Color(0xFFD6C29F),
            'p' to Color(0xFFB09B76),
            'B' to Color(0xFF3A271B),
            'b' to Color(0xFF5C3E2A)
        )

        drawPixelMatrix(
            rows = PixelSprites.lokaPortraitGrid,
            palette = palette,
            center = Offset(cx, cy),
            targetSizePx = spriteSize
        )

        // Dynamic Undertale-style expression overlays
        val u = spriteSize / 28f
        when (expression) {
            PortraitExpression.HAPPY -> {
                drawRect(Color(0xFF140E0A), Offset(cx - u * 1.4f, cy + u * 1.2f), SizeCompat(u * 2.8f, u * 0.75f))
                drawPixelSaveStar(Offset(cx + u * 6.0f, cy - u * 5.4f), u * 1.1f, SepiaGold)
            }
            PortraitExpression.SURPRISED -> {
                drawRect(Color.White, Offset(cx - u * 3.8f, cy - u * 1.9f), SizeCompat(u * 1.3f, u * 1.5f))
                drawRect(Color.White, Offset(cx + u * 2.5f, cy - u * 1.9f), SizeCompat(u * 1.3f, u * 1.5f))
                drawRect(Color(0xFF140E0A), Offset(cx - u * 0.7f, cy + u * 1.3f), SizeCompat(u * 1.4f, u * 1.4f))
            }
            PortraitExpression.ANGRY -> {
                drawRect(Color(0xFF140E0A), Offset(cx - u * 4.2f, cy - u * 2.5f), SizeCompat(u * 2.8f, u * 0.85f))
                drawRect(Color(0xFF140E0A), Offset(cx + u * 1.4f, cy - u * 2.5f), SizeCompat(u * 2.8f, u * 0.85f))
                drawRect(Color(0xFFEF4444), Offset(cx + u * 2.5f, cy - u * 0.9f), SizeCompat(u * 0.95f, u * 0.95f))
            }
            PortraitExpression.DETERMINED -> {
                drawPixelSaveStar(Offset(cx + u * 3.0f, cy - u * 0.8f), u * 1.5f, Color(0xFF38BDF8))
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
 * Deltarune-style Combat Stance Canvas for Loka (standing on left side of the battle stage facing right).
 */
@Composable
fun LokaBattleStanceCanvas(
    modifier: Modifier = Modifier,
    isGlitched: Boolean = false,
    isDefending: Boolean = false,
    isSlashing: Boolean = false,
    weaponAccent: Color = SepiaGold,
    expression: PortraitExpression = PortraitExpression.DETERMINED
) {
    val transition = rememberInfiniteTransition(label = "lokaDeltaruneStance")
    val idleBob by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(720, easing = LinearEasing), RepeatMode.Reverse),
        label = "idleBob"
    )
    Canvas(modifier = modifier) {
        val lungeX = if (isSlashing) size.width * 0.14f else 0f
        val cx = size.width * 0.46f + lungeX
        val cy = size.height * 0.52f + idleBob
        val targetSize = minOf(size.width, size.height) * 0.86f

        // Stepped pixel shadow beneath Loka's boots
        drawSteppedPixelShadow(
            center = Offset(cx, cy + targetSize * 0.44f),
            widthPx = targetSize * 0.72f,
            heightPx = targetSize * 0.14f,
            color = Color(0x88000000)
        )

        val palette = mapOf(
            'O' to Color(0xFF0A080C),
            'H' to Color(0xFF7C2614),
            'h' to Color(0xFFB8452A),
            'd' to Color(0xFF4E170C),
            'S' to Color(0xFFEBC7A8),
            's' to Color(0xFFD2A37E),
            'r' to Color(0xFFE89A8A),
            'W' to Color.White,
            'G' to Color(0xFF6B7D85),
            'C' to (if (isGlitched) Color.White else if (expression == PortraitExpression.DETERMINED) Color(0xFF7DD3FC) else Color(0xFF38BDF8)),
            'l' to Color(0xFF9CA3AF),
            'T' to Color(0xFF747982),
            't' to Color(0xFF52565E),
            'Y' to SepiaGold,
            'y' to Color(0xFFB45309),
            'P' to Color(0xFFD6C29F),
            'p' to Color(0xFFB09B76),
            'B' to Color(0xFF3A271B),
            'b' to Color(0xFF5C3E2A)
        )

        drawPixelMatrix(
            rows = PixelSprites.lokaBattleStanceGrid,
            palette = palette,
            center = Offset(cx, cy),
            targetSizePx = targetSize
        )

        // Pixel sword/stick blade gleam held toward enemy
        val px = targetSize / 26f
        val armX = cx + targetSize * 0.25f
        val armY = cy + targetSize * 0.04f
        if (isDefending) {
            // Deltarune Cyan/Gold Guard Pixel Shield Barrier
            drawRect(Color(0xFF0A0A10), Offset(armX + px, cy - targetSize * 0.34f), SizeCompat(px * 4.5f, targetSize * 0.68f))
            drawRect(SoulSeraphCyan, Offset(armX + px * 1.5f, cy - targetSize * 0.30f), SizeCompat(px * 3.2f, targetSize * 0.60f), style = Stroke(3f))
            drawPixelSaveStar(Offset(armX + px * 3.1f, cy), px * 1.4f, SoulSeraphCyan, Color.White)
        } else {
            val bladeLen = if (isSlashing) targetSize * 0.42f else targetSize * 0.28f
            val bladeY = if (isSlashing) armY - px * 2f else armY - px * 4f
            // Stepped diagonal pixel weapon blade
            for (s in 0..4) {
                val bx = armX + s * (bladeLen / 5f)
                val by = bladeY - s * (px * 1.1f)
                drawRect(Color(0xFF0A0A0D), Offset(bx - 1f, by - 1f), SizeCompat(px * 2.1f, px * 2.1f))
                drawRect(weaponAccent, Offset(bx, by), SizeCompat(px * 1.6f, px * 1.6f))
            }
        }
    }
}

/**
 * Crisp top-down Undertale-style Overworld Pixel Sprite for Frisk-bodied Loka.
 */
fun DrawScope.drawLokaOverworldSprite(
    center: Offset,
    unit: Float,
    isGlitched: Boolean
) {
    drawSteppedPixelShadow(
        center = Offset(center.x, center.y + unit * 1.38f),
        widthPx = unit * 2.04f,
        heightPx = unit * 0.56f
    )

    val palette = mapOf(
        'O' to Color(0xFF120C08),
        'H' to Color(0xFF7C2614),
        'h' to Color(0xFFB8452A),
        'd' to Color(0xFF4E170C),
        'S' to Color(0xFFEBC7A8),
        's' to Color(0xFFD2A37E),
        'r' to Color(0xFFE89A8A),
        'W' to Color.White,
        'G' to Color(0xFF6B7D85),
        'C' to (if (isGlitched) Color.White else Color(0xFF38BDF8)),
        'l' to Color(0xFF9CA3AF),
        'T' to Color(0xFF747982),
        't' to Color(0xFF52565E),
        'Y' to SepiaGold,
        'y' to Color(0xFFB45309),
        'P' to Color(0xFFD6C29F),
        'p' to Color(0xFFB09B76),
        'B' to Color(0xFF3A271B),
        'b' to Color(0xFF5C3E2A)
    )

    drawPixelMatrix(
        rows = PixelSprites.lokaOverworldGrid,
        palette = palette,
        center = center,
        targetSizePx = unit * 3.15f
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

        val u = spriteSize / 28f
        when (expression) {
            PortraitExpression.HAPPY -> {
                drawRect(Color(0xFF09090B), Offset(cx - u * 1.8f, cy - u * 0.8f), SizeCompat(u * 3.6f, u * 0.85f))
            }
            PortraitExpression.SURPRISED -> {
                drawRect(Color.White, Offset(cx - u * 2.8f, cy - u * 3.2f), SizeCompat(u * 1.6f, u * 1.8f))
                drawRect(Color.White, Offset(cx + u * 1.2f, cy - u * 3.2f), SizeCompat(u * 1.6f, u * 1.8f))
            }
            else -> Unit
        }
    }
}
