package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.model.ChestStyle
import com.example.model.EnemyId
import com.example.model.LandmarkKind
import com.example.model.LostKid
import com.example.model.SoulType
import com.example.model.WorldId
import com.example.model.WorldNpc
import com.example.model.ZoneDoorInfo
import com.example.model.ZoneEdge
import kotlin.math.roundToInt
import kotlin.math.sin
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.SepiaAmber
import com.example.ui.theme.SepiaGold
import com.example.ui.theme.SoulAegisAmber
import com.example.ui.theme.SoulInfernalViolet
import com.example.ui.theme.SoulParadoxCrimson
import com.example.ui.theme.SoulSeraphCyan

/**
 * Renders 512x512 Ultra-Crisp Integer-Scaled Undertale Pixel Art Sprites
 * (32x32 / 64x64 grids upscaled cleanly at integer factor via FilterQuality.None).
 */
fun DrawScope.drawPixelMatrix(
    rows: List<String>,
    palette: Map<Char, Color>,
    center: Offset,
    targetSizePx: Float,
    alpha: Float = 1f,
    flipX: Boolean = false
) {
    if (rows.isEmpty()) return
    val gridW = rows.maxOf { it.length }.coerceAtLeast(1)
    val gridH = rows.size.coerceAtLeast(1)
    val hdBitmap = UltraPixelArtEngine.getHdSpriteBitmap(rows, palette)
    val drawSize = UltraPixelArtEngine.computeIntegerScaledSize(gridW, gridH, targetSizePx)
    val left = (center.x - drawSize.width * 0.5f).roundToInt()
    val top = (center.y - drawSize.height * 0.5f).roundToInt()

    if (flipX) {
        scale(scaleX = -1f, scaleY = 1f, pivot = center) {
            drawImage(
                image = hdBitmap,
                dstOffset = IntOffset(left, top),
                dstSize = drawSize,
                alpha = alpha.coerceIn(0f, 1f),
                filterQuality = FilterQuality.None
            )
        }
    } else {
        drawImage(
            image = hdBitmap,
            dstOffset = IntOffset(left, top),
            dstSize = drawSize,
            alpha = alpha.coerceIn(0f, 1f),
            filterQuality = FilterQuality.None
        )
    }
}

/**
 * Classic Undertale Stepped Pixel-Block Outline Heart (13x12).
 */
fun DrawScope.drawPixelHeart(
    center: Offset,
    sizePx: Float,
    fillColor: Color,
    outlineColor: Color? = Color(0xFF0A0A0D),
    cracked: Boolean = false,
    alpha: Float = 1f
) {
    val grid = listOf(
        "..OOO...OOO..",
        ".OXXXO.OXXXO.",
        "OXXXXXOXXXXXO",
        "OXXXXXXXXXXXO",
        "OXXXXXXXXXXXO",
        "OXXXXXXXXXXXO",
        ".OXXXXXXXXXO.",
        "..OXXXXXXXO..",
        "...OXXXXXO...",
        "....OXXXO....",
        ".....OXO.....",
        "......O......"
    )
    val palette = mutableMapOf('X' to fillColor)
    palette['O'] = outlineColor ?: fillColor
    drawPixelMatrix(grid, palette, center, sizePx * 2.05f, alpha = alpha)

    if (cracked) {
        val crack = listOf(
            ".....C.....",
            "....C......",
            ".....C.....",
            "......C....",
            ".....C....."
        )
        drawPixelMatrix(crack, mapOf('C' to Color(0xFF4B5563)), center, sizePx * 1.15f, alpha = alpha)
    }
}

/**
 * Split Soul (الروح الانقسامية): Left half Red, Right half Teal.
 */
fun DrawScope.drawSplitPixelHeart(
    center: Offset,
    sizePx: Float,
    leftColor: Color,
    rightColor: Color,
    outlineColor: Color = Color(0xFF0A0A0D),
    splitHalfIndex: Int? = null,
    alpha: Float = 1f
) {
    val grid = when (splitHalfIndex) {
        0 -> listOf(
            "..OOOO..",
            ".OLLLLO.",
            "OLLLLLLO",
            "OLLLLLLO",
            "OLLLLLLO",
            "OLLLLLLO",
            ".OLLLLLO",
            "..OLLLL.",
            "...OLLLO",
            "....OLLO",
            ".....OLO",
            "......O."
        )
        1 -> listOf(
            "..OOOO..",
            ".ORRRRO.",
            "ORRRRRRO",
            "ORRRRRRO",
            "ORRRRRRO",
            "ORRRRRRO",
            "ORRRRRO.",
            ".RRRRO..",
            "ORRRRO..",
            "ORRRO...",
            "ORRO....",
            ".O......"
        )
        else -> listOf(
            "..OOO...OOO..",
            ".OLLLO.ORRRO.",
            "OLLLLLORRRRRO",
            "OLLLLLDRRRRRO",
            "OLLLLLDRRRRRO",
            "OLLLLLDRRRRRO",
            ".OLLLLDRRRRO.",
            "..OLLLCRRRO..",
            "...OLLCRRO...",
            "....OLCRO....",
            ".....OLO.....",
            "......O......"
        )
    }
    val palette = mapOf(
        'O' to outlineColor,
        'L' to leftColor,
        'R' to rightColor,
        'D' to Color(0xFF0A0A0D),
        'C' to Color(0xFF1F2937)
    )
    val scale = if (splitHalfIndex != null) sizePx * 1.65f else sizePx * 2.05f
    drawPixelMatrix(grid, palette, center, scale, alpha = alpha)
}

fun DrawScope.drawSteppedPixelShadow(
    center: Offset,
    widthPx: Float,
    heightPx: Float,
    color: Color = Color(0x70000000)
) {
    val stepH = (heightPx / 3f).coerceAtLeast(2f)
    drawRect(color, Offset(center.x - widthPx * 0.34f, center.y - heightPx * 0.5f), Size(widthPx * 0.68f, stepH))
    drawRect(color, Offset(center.x - widthPx * 0.50f, center.y - heightPx * 0.5f + stepH), Size(widthPx, stepH))
    drawRect(color, Offset(center.x - widthPx * 0.34f, center.y - heightPx * 0.5f + stepH * 2f), Size(widthPx * 0.68f, stepH))
}

fun DrawScope.drawPixelChest(
    center: Offset,
    sizePx: Float,
    style: ChestStyle,
    isOpen: Boolean,
    accentColor: Color = Color(0xFFFACC15)
) {
    val woodShadow = Color(0xFF3E2312)
    val woodDark = Color(0xFF5C3A21)
    val woodMid = Color(0xFF7C4D29)
    val woodLight = Color(0xFFA06A3E)
    val rim = Color(0xFF0A0A0D)
    val innerRim = Color(0xFFE5DFD5)
    val ironTrim = Color(0xFF94A3B8)
    val shade = Color(0xFF14111A)
    val gold = if (isOpen) accentColor.copy(alpha = 0.72f) else accentColor

    val widthFactor = if (style == ChestStyle.WIDE_PADLOCK) 1.38f else if (style == ChestStyle.SMALL_DOT) 1.06f else 0.96f
    val w = sizePx * widthFactor
    val h = sizePx * 0.94f
    val left = center.x - w * 0.5f
    val top = center.y - h * 0.5f

    drawSteppedPixelShadow(
        center = Offset(center.x, top + h * 0.96f),
        widthPx = w + 6f,
        heightPx = h * 0.24f,
        color = Color(0x66000000)
    )

    if (isOpen) {
        drawRect(woodLight, Offset(left, top - h * 0.14f), Size(w, h * 0.34f))
        drawRect(woodMid, Offset(left, top + h * 0.06f), Size(w, h * 0.14f))
        drawRect(rim, Offset(left, top - h * 0.14f), Size(w, h * 0.34f), style = Stroke(2.2f))
        drawRect(shade, Offset(left + 2f, top + h * 0.20f), Size(w - 4f, h * 0.28f))
        for (i in 0..4) {
            val sx = left + (w * (i + 0.5f) / 5f)
            drawRect(gold, Offset(sx - 2f, top + h * 0.26f), Size(4f, 4f))
        }
        drawRect(woodDark, Offset(left, top + h * 0.48f), Size(w, h * 0.52f))
        drawRect(woodShadow, Offset(left, top + h * 0.84f), Size(w, h * 0.16f))
        drawRect(ironTrim, Offset(left, top + h * 0.48f), Size(w * 0.14f, h * 0.52f))
        drawRect(ironTrim, Offset(left + w * 0.86f, top + h * 0.48f), Size(w * 0.14f, h * 0.52f))
        drawRect(innerRim, Offset(left, top + h * 0.48f), Size(w, h * 0.52f), style = Stroke(2f))
    } else {
        drawRect(woodDark, Offset(left, top), Size(w, h))
        drawRect(woodLight, Offset(left, top), Size(w, h * 0.34f))
        drawRect(woodShadow, Offset(left, top + h * 0.82f), Size(w, h * 0.18f))
        drawRect(ironTrim, Offset(left + w * 0.10f, top), Size(w * 0.11f, h))
        drawRect(ironTrim, Offset(left + w * 0.79f, top), Size(w * 0.11f, h))
        drawRect(innerRim, Offset(left, top), Size(w, h), style = Stroke(2.2f))
        drawRect(rim, Offset(left, top + h * 0.34f), Size(w, 2.5f))

        val cx = center.x
        val cy = top + h * 0.36f
        when (style) {
            ChestStyle.SMALL_DOT -> {
                val r = h * 0.11f
                drawRect(Color.Black, Offset(cx - r * 1.25f, cy - r * 1.25f), Size(r * 2.5f, r * 2.5f))
                drawRect(gold, Offset(cx - r * 0.85f, cy - r * 0.85f), Size(r * 1.7f, r * 1.7f))
            }
            ChestStyle.SQUARE_STAR -> {
                drawPixelSaveStar(center = Offset(cx, cy), sizePx = h * 0.16f, color = gold, coreColor = Color.White)
            }
            ChestStyle.WIDE_PADLOCK -> {
                val r = h * 0.11f
                drawRect(gold, Offset(cx - r, cy - r * 1.2f), Size(r * 2f, r * 1.1f), style = Stroke(2.2f))
                drawRect(gold, Offset(cx - h * 0.14f, cy), Size(h * 0.28f, h * 0.26f))
                drawRect(Color.Black, Offset(cx - h * 0.14f, cy), Size(h * 0.28f, h * 0.26f), style = Stroke(1.8f))
            }
        }
    }
}

/**
 * Classic Undertale 4-point pixel Save Star (9x9).
 */
fun DrawScope.drawPixelSaveStar(
    center: Offset,
    sizePx: Float,
    color: Color,
    coreColor: Color = Color(0xFFFFFBEB)
) {
    val grid = listOf(
        "....O....",
        "....O....",
        "...OXO...",
        "..OXXXO..",
        "OOXXXXXOO",
        "..OXXXO..",
        "...OXO...",
        "....O....",
        "....O...."
    )
    drawPixelMatrix(
        rows = grid,
        palette = mapOf('O' to color, 'X' to coreColor),
        center = center,
        targetSizePx = sizePx * 2f
    )
}

object PixelSprites {

    // 64x64 Boyish Heroic Loka Bust Portrait (512x512 @ 8x integer scale):
    // Tousled spiky chestnut hair (cropped cleanly around ears — no long bob curtains),
    // focused determined eyes with brows, clean jawline (no pink cheek blush), collared adventure jacket, golden heart locket.
    val lokaPortraitGrid = listOf(
        "................................................................",
        "...........................OOO.....OOO..........................",
        ".........................OOHhhOO.OOHhhOO........................",
        ".......................OOHhhllhHOOHhhllhHOO.....................",
        "....................OOOHhHHhhhhHHHHhhhhHHhHOOO..................",
        "..................OOHHhHHHHHHHHhHHhHHHHHHHHhHOO.................",
        "................OOHhHHHHhllHHHHHHHHHHHHllhHHHHhHOO..............",
        "..............OOHHHhHHHHHHHHHHHHHHHHHHHHHHHHHHhHHHOO............",
        ".............OHHhHHHHHHHHhHHHHHHHHHHHHHHhHHHHHHHHhHHO...........",
        "............OHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHO..........",
        "...........OHHhHHHHHHHHHdHHHHHHHhHHHHHHHdHHHHHHHHHHhHHO.........",
        "..........OHHHHHHHHHHHddHHHHHHHHHHHHHHHHHHddHHHHHHHHHHHO........",
        "..........OHHHHHHHHHddHHHddHHHHHHHHHHHHddHHHddHHHHHHHHHO........",
        "..........OHHHHHHHddHHHdd.dHHHdHHHHdHHHdd..ddHHHddHHHHHO........",
        "..........OHHHHHddHHddd.OSSddHHdHHdHHddSSdO..dddHHddHHHO........",
        "..........OHHHHddHdd..OSSSSSSdHdHHdHdSSSSSSO...ddHddHHHO........",
        "..........OHHHddHd...OSSSSSSSSSdHHdSSSSSSSSSO....dHddHHO........",
        "..........OHHddHd...OSSSSSSSSSSSSSSSSSSSSSSSSO....dHddHO........",
        "..........OHOdHO...OSSSOOOOOOOSSSSSSSSOOOOOOSSO....OHdOO........",
        "...........OO.OO..OSSSOOOOOOOSSsSSSSsSSOOOOOOSSO...OO.O.........",
        "..............O...OSSOWWWCGcOSSSSSSSSSSOWWWCGcSO...O............",
        "..............O..OSSOOWWCGccOSSSSSSSSSSOWWCGccSSO..O............",
        ".............OdsOOSSOOGGGCcCOSSSSSSSSSSOGGGCcCOSSOsdO...........",
        ".............OSSSssSSOOCCCCOOSSSSSSSSSSOOCCCCOOSSssSO...........",
        ".............OSSsSSsSSSSSSSSSSSSsSSsSSSSSSSSSSSSsSSSO...........",
        ".............OOssSSSSSSSSSSSSSSSSssSSSSSSSSSSSSSSSSOO...........",
        "..............OOSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSOO............",
        "...............OOSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSOO.............",
        "................OOssssSSSSSSSSSmmmmmmSSSSSSSSssssO..............",
        ".................OOsssSSSSSSSSSSSSSSSSSSSSSSSsssOO..............",
        "..................OOsssssSSSSSSSSSSSSSSSSSSsssssO...............",
        "...................OOOssssssssssssssssssssssssOOO...............",
        ".....................OOOOOssssssssssssssOOOOOO..................",
        ".................OOOOOKKKKKKssYYYYYYYYssKKKKKKOOOO..............",
        "...............OOKKTTTTTTTttKKssYYYYssKKttTTTTTTTKKOO...........",
        ".............OOKTTTTTTTTTTTtKKyYYYYYYyKKtTTTTTTTTTTTKKOO........",
        "...........OOTTTTTTTTTTTTTTtKKyYYWWYYyKKtTTTTTTTTTTTTTTTOO......",
        "..........OOTTTTTTTTTTTTTTTtyYYWWYYYYYYytTTTTTTTTTTTTTTTTOO.....",
        ".........OOTTTTtTTTTTTTTTTTTyYYYYYYYYYYyTTTTTTTTTTTTtTTTTTOO....",
        "........OOTTTTtTTTTTTTTTTTTTtyYYYYYYYYytTTTTTTTTTTTTTtTTTTTOO...",
        "........OOTTTTtTTTTTTTTTTTTTTtyYYYYYYytTTTTTTTTTTTTTTtTTTTTOO...",
        "........OOTTTTtTTTTTTTTTTTTTTTtyYYYYytTTTTTTTTTTTTTTTtTTTTTOO...",
        ".......OOTTTTtKKKKKKKKKKKKKKKKKtyYYytKKKKKKKKKKKKKKKKKtTTTTTOO..",
        ".......OOTTTTtTTTTTTTTTTTTTTTTTTtyytTTTTTTTTTTTTTTTTTTtTTTTTOO..",
        ".......OOTTTTtTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTtTTTTTOO..",
        ".......OOKKKKKTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTKKKKKKOO..",
        ".......OSSSSsOOOTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTOOOSSSSSSO..",
        ".......OSSSSsOO.OttttttttttttttttttttttttttttttttttO.OOsSSSSSO..",
        "........OOOOOO..OPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPO..OOOOOOO...",
        "................OPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPO............",
        "................OPPPPPPPPPPPPPPPppPPPPPPPPPPPPPPPPPO............",
        "................OPPPPPPPPPPPPPPP..PPPPPPPPPPPPPPPPPO............",
        "................OPPPPPPPPPPPPPP....PPPPPPPPPPPPPPPPO............",
        "................OpppppppppppppO....OppppppppppppppPO............",
        "................OOOOOOOOOOOOOOO....OOOOOOOOOOOOOOOOO............",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................"
    )

    // 64x64 Fuller Boyish Heroic Loka Overworld Sprite (512x512 @ 8x integer scale):
    // Short spiky tousled hair (no girly side flaps), clear ears, focused eyes, broad collared jacket,
    // glowing golden heart locket, sturdy legs and chunky adventure boots filling rows 1..61.
    val lokaOverworldGrid = listOf(
        "...........................OOO.....OOO..........................",
        ".........................OOHhhOO.OOHhhOO........................",
        "......................OOOHhllhhHOOHhhllhHOOO....................",
        "...................OOOHhHHHHHHHHHHHHHHHHHHhHOOO.................",
        ".................OOHHhHHHHhllHHHHHHHHllhHHHHhHOO................",
        "...............OOHhHHHHHHHHHHHHHHHHHHHHHHHHHHHHhHOO.............",
        ".............OOHHHhHHHHHHHHHHHHHHHHHHHHHHHHHHHHhHHHOO...........",
        "............OHHhHHHHHHHHHdHHHHHHHHHHHHHHdHHHHHHHHHHhHO..........",
        "...........OHHHHHHHHHHHddHHHHHHHHHHHHHHHHddHHHHHHHHHHHO.........",
        "..........OHHHHHHHHHddHHHddHHHHHHHHHHHHddHHHddHHHHHHHHHO........",
        "..........OHHHHHHHddHHHdddHHdHHHHHHHHdHHdddHHHddHHHHHHHO........",
        "..........OHHHHHddHddd..OSSdHHdHHHHdHHdSSO..dddHddHHHHHO........",
        "..........OHHHHddHd...OSSSSSdHdHHHHdHdSSSSSO...dHddHHHHO........",
        "..........OHHHddHO...OSSSSSSSdHHHHHHdSSSSSSSO...OHddHHHO........",
        "...........OOOdHO...OSSSOOOOOOSSSSSSOOOOOOSSSO...OHdOOO.........",
        "..............OO...OSSSOOOOOOOsSSSSsOOOOOOOSSSO...OO............",
        ".............OdsOO.OSSOWWWCGcOSSSSSSOWWWCGcOSSO..OsdO...........",
        ".............OSSsO.OSSOWWCGccOSSSSSSOWWCGccOSSO..OsSSO..........",
        ".............OSSsO.OSSOOGGGCcOSSSSSSOGGGCcOOSSO..OsSSO..........",
        "..............OOsO.OSSSOOCCCOOSSSSSSOOCCCOOSSSO..OsOO...........",
        "...............OO..OSSSSSSSSSSSSSSSSSSSSSSSSSSO...OO............",
        "..................OOssssssssSSSSSSSSSSssssssssOO................",
        "..................OSSSSSSSSSSmmmmmmmmSSSSSSSSSSO................",
        "...................OOSSSSSSSSSSSSSSSSSSSSSSSSOO.................",
        "....................OOsssssSSSSSSSSSSSSsssssOO..................",
        ".....................OOOOssssssssssssssssOOOO...................",
        ".................OOOOOKKKKKssYYYYYYYYssKKKKKOOOO................",
        "..............OOKKTTTTTTTttKKssYYYYssKKttTTTTTTTKKOO............",
        "............OOKTTTTTTTTTTTtKKyYYYYYYyKKtTTTTTTTTTTTKOO..........",
        "...........OOTTTTTTTTTTTTTtKKyYYWWYYyKKtTTTTTTTTTTTTTOO.........",
        "..........OOTTTTtTTTTTTTTTtyYYWWYYYYYYytTTTTTTTTTtTTTTOO........",
        "..........OOTTTTtTTTTTTTTTTyYYYYYYYYYYyTTTTTTTTTTtTTTTOO........",
        ".........OOTTTTTtKKKKKKKKKKtyYYYYYYYYytKKKKKKKKKKtTTTTTOO.......",
        ".........OOTTTTTtTTTTTTTTTTTtyYYYYYYytTTTTTTTTTTTtTTTTTOO.......",
        ".........OOTTTTTtTTTTTTTTTTTTtyYYYYytTTTTTTTTTTTTtTTTTTOO.......",
        ".........OOTTTTTtTTTTTTTTTTTTTtyYYytTTTTTTTTTTTTTtTTTTTOO.......",
        ".........OOTTTTTtTTTTTTTTTTTTTTtyytTTTTTTTTTTTTTTtTTTTTOO.......",
        ".........OOKKKKKKTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTKKKKKKOO.......",
        ".........OSSSSSsOOTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTOOsSSSSSO.......",
        ".........OSSSSSsOOTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTOOsSSSSSO.......",
        ".........OSSSSSsOOttttttttttttttttttttttttttttttOOsSSSSSO.......",
        "..........OOOOOO.OPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPO.OOOOOO........",
        ".................OPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPO...............",
        ".................OPPPPPPPPPPPPPPPppPPPPPPPPPPPPPO...............",
        ".................OPPPPPPPPPPPPPPpOOpPPPPPPPPPPPPO...............",
        ".................OPPPPPPPPPPPPPPO..OPPPPPPPPPPPPO...............",
        ".................OPPPpPPPPPPPPPPO..OPPPPPPPpPPPPO...............",
        ".................OPPPPPPPPPPPPPPO..OPPPPPPPPPPPPO...............",
        ".................OPPPpPPPPPPPPPPp..pPPPPPPPpPPPPO...............",
        ".................OPPPPPPPPPPPPPPO..OPPPPPPPPPPPPO...............",
        ".................OPPPPPPPPPPPPPPO..OPPPPPPPPPPPPO...............",
        ".................OppppppppppppppO..OppppppppppppO...............",
        "................ObbbbbbbbbbbbbbOO..OObbbbbbbbbbbbbO.............",
        "................OBBBBBBBBBBBBBBO....OBBBBBBBBBBBBBO.............",
        "...............OBBBBBBBBBBBBBBBO....OBBBBBBBBBBBBBBO............",
        "...............OBBBBBBBBBBBBBBBO....OBBBBBBBBBBBBBBO............",
        "...............OBBBBBBBBBBBBBBBO....OBBBBBBBBBBBBBBO............",
        "...............OBBBBBBBBBBBBBBBO....OBBBBBBBBBBBBBBO............",
        "...............ObbbbbbbbbbbbbbOO....OObbbbbbbbbbbbOO............",
        "................OOOOOOOOOOOOOOO......OOOOOOOOOOOOOO.............",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................"
    )

    // 64x64 Horizontal Fallen Loka Sprite (shown when Loka collapses onto the stone ground on DEFEAT):
    // Left: tousled spiky chestnut head slumped sideways with closed wincing eyes;
    // Center: crumpled grey jacket with spilled Golden Heart Locket resting beside torso;
    // Right: legs in beige trousers & tipped brown boots lying flat on the floor.
    val lokaFallenGrid = listOf(
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "............OOOOO...............................................",
        ".........OOHHhhhHOO.............................................",
        ".......OOHhllHHHHhHOO.......OOOOOOO.............................",
        ".....OOHhHHHHHHHHHHhHO....OOTTTTTTTOO...........................",
        "....OHhHHHHddHHHHdHHhHO.OOKTTTTTTTTTTOOO........................",
        "...OOHHHHddSSSdHHHdSSdOOTTTTTTTTtTTTTTTTTOOOOO..................",
        "..OOHHHHdSSSSSSdHdSSSSOTTTTTtTTTtTTTTTTTTTTTTTTOOO..............",
        "..OHHHddSSOOOSSdSSOOSSOKKKTTTTTTtTTTTTTTTPPPPPPPPPOOO...........",
        "...OHHdSSSSOSSSSSSSSSSOTTTTtTTTTTTTTTTTTTPPPPPPPPPPPPOOOO.......",
        "...OOHdSSSSSSSOSSmmSSSOtTTTTtTTTTTTTTTTTTPPPPPPPPpPPPPPPPOOOOO..",
        "....OOHdsssssSSSSSSSSSOOOKKKTTTTTTTTTTTTTPPPPPPppPPPPPPPPbbBBBO.",
        ".....OOHHdddsssssssssSSOOSSsOTTTTTtTTTTTTPPPPPPPPPPPPPPPPBBBBBO.",
        "......OOOOOOHHdddssOOOOOSSSSsOTTTtTTTTTTTPPPPPPPpPPPPPPPPBBBBBO.",
        "...........OOOOO..OSSSSsOOOOOtTTTTTTTTTTTPPPPPPPPPPPPPPPPbbbBBO.",
        "..................OOOOOO.OOOOOKKKTTTTTTttPPPPpPPPPPPPPPPPOOOOO..",
        ".........................OYYYYyyOOTTTTTTTPPPPPppPPPPPPPPPPPO....",
        ".........................OYWWYYYOOtTTTTTTPPPPPPPPPPPPbbbbBBO....",
        "..........................OYYYYyOO.OOOOOOPPPPPPPPPPPPBBBBBBBO...",
        "...........................OYYyOO.......OOpppppppppppBBBBBBBO...",
        "............................OOOO..........OOOOOOOOOOOObbbbBBO...",
        "......................................................OOOOOOO...",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................",
        "................................................................"
    )

    val lokaWalkFrameAGrid: List<String> by lazy {
        buildLokaMotionFrame(
            bodyDx = 0,
            headDy = -1,
            leftLegDx = -1,
            leftLegDy = -3,
            rightLegDx = 1,
            rightLegDy = 1,
            leftArmDx = -2,
            leftArmDy = 1,
            rightArmDx = 2,
            rightArmDy = -2,
            isSprint = false
        )
    }

    val lokaWalkFrameBGrid: List<String> by lazy {
        buildLokaMotionFrame(
            bodyDx = 0,
            headDy = -1,
            leftLegDx = -1,
            leftLegDy = 1,
            rightLegDx = 1,
            rightLegDy = -3,
            leftArmDx = -2,
            leftArmDy = -2,
            rightArmDx = 2,
            rightArmDy = 1,
            isSprint = false
        )
    }

    // Forward-leaning sprint frames (facing right; flipped horizontally when moving left)
    val lokaRunFrameAGrid: List<String> by lazy {
        buildLokaMotionFrame(
            bodyDx = 3,
            headDy = -1,
            leftLegDx = -5,
            leftLegDy = -2,
            rightLegDx = 5,
            rightLegDy = -4,
            leftArmDx = -4,
            leftArmDy = 1,
            rightArmDx = 4,
            rightArmDy = -3,
            isSprint = true
        )
    }

    val lokaRunFrameBGrid: List<String> by lazy {
        buildLokaMotionFrame(
            bodyDx = 2,
            headDy = 0,
            leftLegDx = 5,
            leftLegDy = -3,
            rightLegDx = -5,
            rightLegDy = -1,
            leftArmDx = 4,
            leftArmDy = -3,
            rightArmDx = -4,
            rightArmDy = 1,
            isSprint = true
        )
    }

    private fun buildLokaMotionFrame(
        bodyDx: Int,
        headDy: Int,
        leftLegDx: Int,
        leftLegDy: Int,
        rightLegDx: Int,
        rightLegDy: Int,
        leftArmDx: Int,
        leftArmDy: Int,
        rightArmDx: Int,
        rightArmDy: Int,
        isSprint: Boolean
    ): List<String> {
        val h = lokaOverworldGrid.size
        val w = lokaOverworldGrid.first().length
        val canvas = Array(h) { CharArray(w) { '.' } }

        fun plot(x: Int, y: Int, ch: Char) {
            if (x in 0 until w && y in 0 until h && ch != '.') {
                canvas[y][x] = ch
            }
        }

        // 1. Legs & Boots (rows 42..60)
        for (y in 42..60) {
            val row = lokaOverworldGrid[y]
            for (x in 0 until w) {
                val ch = row[x]
                if (ch == '.') continue
                if (x < 32) {
                    plot(x + leftLegDx, y + leftLegDy, ch)
                } else {
                    plot(x + rightLegDx, y + rightLegDy, ch)
                }
            }
        }

        // 2. Torso & Arms (rows 26..41)
        for (y in 26..41) {
            val row = lokaOverworldGrid[y]
            for (x in 0 until w) {
                val ch = row[x]
                if (ch == '.') continue
                val torsoDy = if (isSprint) headDy / 2 else headDy
                when {
                    x <= 15 -> plot(x + bodyDx / 2 + leftArmDx, y + torsoDy + leftArmDy, ch)
                    x >= 48 -> plot(x + bodyDx / 2 + rightArmDx, y + torsoDy + rightArmDy, ch)
                    else -> plot(x + bodyDx / 2, y + torsoDy, ch)
                }
            }
        }

        // 3. Head & Hair (rows 0..25) with crisp wind-swept crown tuft during Sprint
        for (y in 0..25) {
            val row = lokaOverworldGrid[y]
            for (x in 0 until w) {
                val ch = row[x]
                if (ch == '.') continue
                val leanX = if (isSprint) bodyDx else 0
                plot(x + leanX, y + headDy, ch)
                if (isSprint && ch in setOf('H', 'h', 'l', 'O') && x in 10..17 && y in 3..14) {
                    plot(x + leanX - 2, y + headDy, ch)
                }
            }
        }

        return canvas.map { String(it) }
    }

    // 32x32 Monochrome Undertale Ashen Flower (512x512 @ 16x integer scale):
    // Sculpted 6-petal white/silver Undertale blossom inspired by Flowey art, curved stem, veined leaves, pixel dirt mound.
    val ashenFlowerNormal = listOf(
        ".......OOOOOO......OOOOOO.......",
        "......OWWWWWWO....OWWWWWWO......",
        ".....OWPPPPPPWO..OWPPPPPPWO.....",
        ".....OWPPGGPPWWOOOWWPPGGPWO.....",
        ".....OWPGGPPPPWWWWPPPPGGPWO.....",
        "..OOOOOWPPPPPPPPPPPPPPPPOOOOOO..",
        ".OWWWWPPPPPPPPGGGGPPPPPPPPWWWWO.",
        "OWPPPPGGPPPPWWWWWWWWPPPPGGPPPPO.",
        "OWPPPGGPPPWPPPPPPPPPPWPPPGGPPPO.",
        "OWPPGGPPPWPPBBPPPPBBPPWPPPGPPPO.",
        "OWPPGGPPPWPPBBPPPPBBPPWPPPGPPPO.",
        ".OWPPPGGPWPPPPPPPPPPPPWPPGGPPPO.",
        ".OWWWWWPPWPPPBBBBBBPPPWPPWWWWWO.",
        "..OOOOOWPPWPPPPWWPPPPWPPOOOOOO..",
        ".....OWPGGPWWWWWWWWWWPGGPWO.....",
        ".....OWPPGGGGGGGGGGGGGGPPWO.....",
        "......OOOGGGGGDDDDGGGGGOOO......",
        "........OOOOOODDDDOOOOOO........",
        "..............ODDDO.............",
        "..............ODDDO.............",
        ".....OOOOO....ODDDO...OOOOO.....",
        "....OGPPPGO...ODDDDO.OGPPPGO....",
        "....OGGGGGDO..ODDDO.ODGGGGGO....",
        ".....OOOGGGDO.ODDDO.ODGGGOO.....",
        "........OOOGDODDDDODGGOOO.......",
        ".......OOOOODDDDDDDDOOOOO.......",
        ".....OODDDDDGGDDDDGGDDDDDOO.....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        ".....OOOOOOOOOOOOOOOOOOOOOO.....",
        "................................",
        "................................",
        "................................"
    )

    // 32x32 Creepy Undertale Smirk Ashen Flower (512x512 @ 16x integer scale)
    val ashenFlowerCreepy = listOf(
        ".......OOOOOO......OOOOOO.......",
        "......ODDDDDDO....ODDDDDDO......",
        ".....ODPPPPPPDO..ODPPPPPPDO.....",
        ".....ODDDGGPPDDDDDDPPGGDDDO.....",
        ".....ODPGGDDDDPPPPDDDDGGPDO.....",
        "..OOOOODPPPPPPPPPPPPPPPPDOOOOO..",
        ".ODDDDPPPPPPGGGGGGGGPPPPPPDDDDO.",
        "ODPPPPGGPPPPGGGGGGGGPPPPGGPPPPO.",
        "ODPPPGGPPPGPPPPPPPPPPGPPGGPPPPO.",
        "ODPPGGPPPGPBBBPPPPBBBPGGPPPGPPO.",
        "ODPPGGPPPGPBBWWPPWWBBPGGPPPGPPO.",
        ".ODPPPGGPGPPPPPPPPPPPPGGPGGPPPO.",
        ".ODDDDDPPGPBBBBBBBBBBPGGPDDDDDO.",
        "..OOOOODPPGGBWWWWWWBBGPPDOOOOO..",
        ".....ODPGGPGGGBBBBGGGPPGGDO.....",
        ".....ODDDGGGGGGGGGGGGGGDDDO.....",
        "......OOOGGGGGDDDDGGGGGOOO......",
        "........OOOOOODDDDOOOOOO........",
        "..............ODDDO.............",
        "..............ODDDO.............",
        ".....OOOOO....ODDDO...OOOOO.....",
        "....OGGGGGGO..ODDDDO.OGGGGGGO...",
        "....ODGGGGDO..ODDDO.ODGGGGDO....",
        ".....OOODGGDO.ODDDO.ODGDOOO.....",
        "........OOOGDODDDDODGGOOO.......",
        ".......OOOOODDDDDDDDOOOOO.......",
        ".....OODDDDDDDDDDDDDDDDDDDOO....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        ".....OOOOOOOOOOOOOOOOOOOOOO.....",
        "................................",
        "................................",
        "................................"
    )

    // ==================== 32x32 UNDERTALE ENEMIES & BOSSES (512x512 @ 16x SCALE) ====================

    // 32x32 Ruins Training Dummy Phase 1: Blue top hat, peanut stitched body, X X eyes, 2 thin legs & oval base
    val ruinsDummyGrid = listOf(
        "...........OOOOOOOOOO...........",
        "..........OHHhhhhHHHO...........",
        "..........OHHHHHHHHHHO..........",
        "..........OHBBBBBBBBHO..........",
        "........OOOHHHHHHHHHHOOO........",
        ".......OHHhhhhhhhhhhhhHHO.......",
        "........OOOOOOOOOOOOOOOO........",
        "..........OOLLLLLLLLOO..........",
        ".........OLCCCCCCCCCCLO.........",
        "........OLCBBCBBBBCCBCLO........",
        "........OCCCBCBBBBCCCBCO........",
        "........OCCBBCBBBBCCBBCO........",
        "........OCCCCCCCCCCCCCCO........",
        ".......OOCCCCCCCCCCCCCCOO.......",
        "......OOLLCCBMCCCCCCCCCCOO......",
        "....OOLCCCCMBMBCCCCCCCCCCCO.....",
        "....OLCCCCCMBMCCCCCCCCCCCCCO....",
        ".....OOCBCCCBMCCCCCCCCCCCCCO....",
        "......OCCCCCCBMCCCCCCCCCCCCO....",
        "......OCCCCCCCBMCCCCCCCCCCCO....",
        ".......OCCCCCCCBMCCCCCCCCCO.....",
        "........OCCCCCCCCCCCCCCCCO......",
        "........OOOOOOOOOOOOOOOOOO......",
        "...........OCCO....OCCO.........",
        "...........OCCO....OCCO.........",
        "...........OCCO....OCCO.........",
        "...........OCCO....OCCO.........",
        ".........OOOHHHHHHHHOOO.........",
        ".......OHHHHHHHHHHHHHHHHO.......",
        ".......OHHhhhhhhhhhhhhHHO.......",
        "........OOOOOOOOOOOOOOOO........",
        "................................"
    )

    // 32x32 Ruins Training Dummy Phase 2: Bare torn head (hat knocked off), shocked X/> eyes & white cotton bursting from tears
    val ruinsDummyPhase2Grid = listOf(
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "..........OOOWWOOOOOO...........",
        ".........OLLWWWWCCCCOO..........",
        "........OLCCWWWWCCCCCCLO........",
        "........OLCBCWWWWCCCBCCO........",
        "........OCBCCWWWWCCBBCBO........",
        "........OCBCCWCCCCCBCCCO........",
        "........OCCCCCCCCCCCCCCO........",
        ".......OOCCCCCCCCCCCCCCOO.......",
        "......OOLLCCWWWWCCCCCCCCOO......",
        "....OOLCCCWWWWWWWCCCCCCCCCO.....",
        "....OLCCCWWWWWWWWWWCCCCCCCCO....",
        ".....OOCBWWWWWWWWWWCCCCCCCCO....",
        "......OCCCCWWWWWWWCCCCCCCCCO....",
        "......OCCCCCCWWWWBCCCCCCCCCO....",
        ".......OCCCCCCCMBMCCCCCCCCO.....",
        "........OCCCCCCCCCCCCCCCCO......",
        "........OOOOOOOOOOOOOOOOOO......",
        "...........OCCO....OCCO.........",
        "...........OCCO....OCCO.........",
        "...........OCCO....OCCO.........",
        "...........OCCO....OCCO.........",
        ".........OOOHHHHHHHHOOO.........",
        ".......OHHHHHHHHHHHHHHHHO.......",
        ".......OHHhhhhhhhhhhhhHHO.......",
        "........OOOOOOOOOOOOOOOO........",
        "................................",
        "................................"
    )

    // 14x8 Top Hat grid for Phase 2 falling animation
    val dummyTopHatGrid = listOf(
        "...OOOOOOOO...",
        "..OHHhhhhHHO..",
        "..OHHHHHHHHO..",
        "..OHBBBBBBHO..",
        "OOOHHHHHHHHOOO",
        "OHHhhhhhhhhHHO",
        ".OOOOOOOOOOOO."
    )

    // 10x7 Fluffy Cotton Puff grid for Phase 2 floor & tear scatter
    val cottonPuffGrid = listOf(
        "..OOOOOO..",
        ".OWWWWWWO.",
        "OWWWWWWWWO",
        "OWWWCCWWWO",
        ".OWWWWWWO.",
        "..OOOOOO.."
    )

    // Commander Vex (32x32): Armored Paradox Officer with angular visor & tactical coat
    val vexBossGrid = listOf(
        "..........OOOOOOOOOOOO..........",
        ".........ODDDDDDDDDDDDO.........",
        "........ODDDDDDDDDDDDDDO........",
        ".......ODDDAAAAAAAAAAADDO.......",
        ".......ODDAAAAAAAAAAAAADO.......",
        ".......ODAAAWWWWWWWWAAADO.......",
        ".......ODDAAAAAAAAAAAAADO.......",
        "........ODDDDDDDDDDDDDDO........",
        ".........ODDDDDDDDDDDDO.........",
        ".....OOO..OOOOOOOOOOOO..OOO.....",
        "....OAAAOODDDDDDDDDDDDOOAAAO....",
        "...OAAAAOODDDDDWWDDDDDOOAAAAO...",
        "...OAAAO.ODDDDDDDDDDDDO.OAAAO...",
        "...OAAO..ODDDDDAAADDDDO..OAAO...",
        ".........ODDDDDAAADDDDO.........",
        "........ODDDDDDWWDDDDDDO........",
        "........ODDDDDDDDDDDDDDO........",
        "........ODDDDDDAAADDDDDO........",
        "........ODDDDDDAAADDDDDO........",
        "........ODDDDDDDDDDDDDDO........",
        ".........ODDDDDDDDDDDDO.........",
        ".........ODDDDD..DDDDDO.........",
        ".........ODDDDD..DDDDDO.........",
        ".........ODDDDD..DDDDDO.........",
        ".........ODDDDD..DDDDDO.........",
        "........OODDDD....DDDDOO........",
        "........OOOOOO....OOOOOO........",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // Bramble (32x32): Gentle horned Boss Monster (Asgore/Toriel silhouette) with mossy coat
    val brambleBossGrid = listOf(
        ".OOO........................OOO.",
        ".OGGO......................OGGO.",
        "..OGGO......OOOOOOOO......OGGO..",
        "...OGGO...OODDDDDDDDOO...OGGO...",
        "....OGGGOODDDDDDDDDDDDOOGGGO....",
        "...ODDDDDDDDDDDDDDDDDDDDDDDDO...",
        "..ODDDDDGGDDDDDDDDDDDDGGDDDDDO..",
        "..ODDDDDDDDDDDDDDDDDDDDDDDDDDO..",
        "..ODDDDDDWWWDDDDDDDDWWWDDDDDDO..",
        "..ODDDDDDWBBDDDDDDDDWBBDDDDDDO..",
        "...ODDDDDDDDDDDDDDDDDDDDDDDDO...",
        "...ODDDDDDDDGGGGGGGGDDDDDDDDO...",
        "....OODDDDDDGGGGGGGGDDDDDDOO....",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        "....ODDDGGDDDDDDDDDDDDGGDDDO....",
        "...ODDDDGGDDDDWWWWDDDDGGDDDDO...",
        "...ODDDDDDDDDDWWWWDDDDDDDDDDO...",
        "...ODDDDDDDDDDDDDDDDDDDDDDDDO...",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        ".....ODDDDDDDD....DDDDDDDDO.....",
        "......ODDDDDD......DDDDDDO......",
        "......ODDDDDD......DDDDDDO......",
        ".....OODDDDD........DDDDDOO.....",
        ".....OOOOOOO........OOOOOOO.....",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // Lady Vespera (32x32): Noble butterfly/spider-noble silhouette with floating crown crest & gown
    val vesperaBossGrid = listOf(
        "..........OOOOOOOOOOOO..........",
        ".........OYYYYYYYYYYYYO.........",
        "..........OOYYYYYYYYOO..........",
        ".........ODDDDDDDDDDDDO.........",
        "........ODDDDDDDDDDDDDDO........",
        "........ODDDYYDDDDYYDDDO........",
        "........ODDDYWDDDDWYDDDO........",
        ".........ODDDDDDDDDDDDO.........",
        "........OODDDDDDDDDDDDOO........",
        ".......ODDDDDCCCCCCDDDDDO.......",
        "......ODDDDDCCCCCCCCDDDDDO......",
        ".....ODDDDDCCCCYYCCCCDDDDDO.....",
        "....ODDDDDDDDDDYYDDDDDDDDDDO....",
        "...ODDDDDDDDDDYYYYDDDDDDDDDDO...",
        "..ODDDDDDDDDDDDDDDDDDDDDDDDDDO..",
        "..ODDDDDDDDDDDDDDDDDDDDDDDDDDO..",
        ".ODDDDDDDDCCCCDDDDCCCCDDDDDDDDO.",
        ".ODDDDDDDDDDDDDDDDDDDDDDDDDDDDO.",
        "..OOOOOOOOOOOOOOOOOOOOOOOOOOOO..",
        "........ODDDDDD..DDDDDDO........",
        "........ODDDDD....DDDDDO........",
        "........ODDDDD....DDDDDO........",
        ".......OODDDD......DDDDOO.......",
        ".......OOOOOO......OOOOOO.......",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // Sentinel Orin (32x32): Ancient carved stone golem construct with glowing rune core
    val orinBossGrid = listOf(
        "......OOOOOOOOOOOOOOOOOOOO......",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        "....ODDDDDWWWDDDDDDWWWDDDDDO....",
        "....ODDDDDWBBDDDDDDWBBDDDDDO....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        "...OODDDDDDDDDDDDDDDDDDDDDDOO...",
        "..ODDDDDDDDDDDDDDDDDDDDDDDDDDO..",
        ".ODDDDDDDDBBBBBBBBBBBBDDDDDDDDO.",
        ".ODDDDDDDBBWWWWWWWWWWBBDDDDDDDO.",
        ".ODDDDDDDBBWBBBBBBBBWBBDDDDDDDO.",
        ".ODDDDDDDBBWBWWWWWWBWBBDDDDDDDO.",
        ".ODDDDDDDBBWBBBBBBBBWBBDDDDDDDO.",
        ".ODDDDDDDBBWWWWWWWWWWBBDDDDDDDO.",
        "..ODDDDDDDBBBBBBBBBBBBDDDDDDDO..",
        "...OODDDDDDDDDDDDDDDDDDDDDDOO...",
        "...ODDDDDDDDDDDDDDDDDDDDDDDDO...",
        "....ODDDDDDDDD....DDDDDDDDDO....",
        "....ODDDDDDDDD....DDDDDDDDDO....",
        ".....ODDDDDDD......DDDDDDDO.....",
        ".....ODDDDDDD......DDDDDDDO.....",
        ".....ODDDDDDD......DDDDDDDO.....",
        "....OODDDDDD........DDDDDDOO....",
        "....OOOOOOOO........OOOOOOOO....",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // Seraphiel (32x32): Six-winged Halo Angel Arbiter in layered ceremonial robes
    val seraphielBossGrid = listOf(
        ".......OOOOOOOOOOOOOOOOOO.......",
        "......OYYYYYYYYYYYYYYYYYYO......",
        ".......OOOOOOOOOOOOOOOOOO.......",
        "....OOO..OOOOOOOOOOOOOO..OOO....",
        "...OCCCOODDDDDDDDDDDDDDOOCCCO...",
        "..OCCCCOODDDDYYDDYYDDDDOOCCCCO..",
        ".OCCCCCOODDDDDDDDDDDDDDOOCCCCCO.",
        "..OCCCC..OODDDDDDDDDDOO..CCCCO..",
        "...OOO..ODDDDDDDDDDDDDDO..OOO...",
        "..OOCCCOODDDDDDYYDDDDDDOOCCCOO..",
        ".OCCCCCOODDDDDYYYYDDDDDOOCCCCCO.",
        "..OOO...ODDDDDDYYDDDDDDO...OOO..",
        ".......ODDDDDDDDDDDDDDDDO.......",
        "......ODDDDDDDYYYYDDDDDDDO......",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        "....OOOOOOOOOOOOOOOOOOOOOOOO....",
        "........ODDDDD....DDDDDO........",
        "........ODDDDD....DDDDDO........",
        ".......OODDDD......DDDDOO.......",
        ".......OOOOOO......OOOOOO.......",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // Malakor (32x32): Fierce Devil Knight with curved horns, spiky pauldrons & chest emblem
    val malakorBossGrid = listOf(
        ".OOOO......................OOOO.",
        ".OVVVO....................OVVVO.",
        "..OVVVO.....OOOOOOOO.....OVVVO..",
        "...OVVVO..OODDDDDDDDOO..OVVVO...",
        "....OVVVODDDDDDDDDDDDDDOVVVO....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        "....ODDDDVVVVDDDDDDVVVVDDDDO....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        "...OODDDDDDDDDDDDDDDDDDDDDDOO...",
        "..ODDDDDDDDDDDDDDDDDDDDDDDDDDO..",
        ".ODDDDDDDDDVVVVVVVVVVDDDDDDDDDO.",
        ".ODDDDDDDDVWWWWWWWWWWVDDDDDDDDO.",
        ".ODDDDDDDDVWVVVVVVVVWVDDDDDDDDO.",
        ".ODDDDDDDDDVVVVVVVVVVDDDDDDDDDO.",
        "..ODDDDDDDDDDDDDDDDDDDDDDDDDDO..",
        "...ODDDDDDDDDDDDDDDDDDDDDDDDO...",
        "....ODDDDDDDDD....DDDDDDDDDO....",
        "....ODDDDDDDD..OO..DDDDDDDDO....",
        ".....ODDDDDDD..OO..DDDDDDDO.....",
        ".....OODDDDD........DDDDDOO.....",
        ".....OOOOOOO........OOOOOOO.....",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // Echo-Loka (32x32): Corrupted mirror echo in glitch halo, asymmetric void/cyan eyes & cracked chest core
    val echoLokaBossGrid = listOf(
        ".......OOOOOOOOOOOOOOOOOO.......",
        "......OCCCCCCCCCCCCCCCCCCO......",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        "....ODDDDWWWDDDDDDDDCCCDDDDO....",
        "....ODDDDWWWDDDDDDDDCCCDDDDO....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        "....OODDDDDDDDDDDDDDDDDDDDOO....",
        "...ODDDDDDDDDDDDDDDDDDDDDDDDO...",
        "...ODDDDDDDWWWWWWWWWWDDDDDDDO...",
        "...ODDDDDDWCCCCCCCCCCWDDDDDDO...",
        "...ODDDDDDDWCCCCCCCCWDDDDDDDO...",
        "...ODDDDDDDDWCCCCCCWDDDDDDDDO...",
        "....ODDDDDDDWWWWWWWWDDDDDDDO....",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        ".....ODDDDDDD......DDDDDDDO.....",
        ".....ODDDDDDD......DDDDDDDO.....",
        "....OODDDDDD........DDDDDDOO....",
        "....OOOOOOOO........OOOOOOOO....",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // Future-Loka (32x32): Older trench-coated Loka with billowing cloak, glowing cyan eyes & golden heart crest
    val futureLokaBossGrid = listOf(
        ".......OOOOOOOOOOOOOOOOOO.......",
        "......OHHHHHHHHHHHHHHHHHHO......",
        ".....OHHHHHHHHHHHHHHHHHHHHO.....",
        ".....OHHSSSSSSSSSSSSSSSSSHO.....",
        ".....OHSSCCCSSSSSSSSCCCSSHO.....",
        ".....OHSSCCCSSSSSSSSCCCSSHO.....",
        ".....OHHHHHHHHHHHHHHHHHHHHO.....",
        "......OSSSSSSSSSSSSSSSSSSO......",
        "...OOOODDDDDDDDDDDDDDDDDDOOOO...",
        "..ODDDDDDDDDDDDDDDDDDDDDDDDDDO..",
        ".ODDDDDDDDYYYYYYYYYYYYDDDDDDDDO.",
        ".ODDDDDDDDDYYYYYYYYYYDDDDDDDDDO.",
        ".ODDDDDDDDDDYYYYYYYYDDDDDDDDDDO.",
        "ODDDDDDDDDDDDYYYYYYDDDDDDDDDDDDO",
        "ODDDDDDDDDDDDDDDDDDDDDDDDDDDDDDO",
        "ODDDDDDDDDDDDDDDDDDDDDDDDDDDDDDO",
        "ODDDDDDDDDDDDDDDDDDDDDDDDDDDDDDO",
        "ODDDDDDDDDD..........DDDDDDDDDDO",
        "ODDDDDDDDD............DDDDDDDDDO",
        ".OOOOOOOOO............OOOOOOOOO.",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // 32x32 Pumb (user sketch): two round ears + bent top antenna, giant expressive eyes, dark oval body, clawed outstretched arms & legs
    val normalSlimeGrid = listOf(
        ".............OOOOOO.............",
        ".............OGWWGO.............",
        ".......OOOO..OGGOOO.OOOO........",
        "......OGWWGO.OGGO..OGWWGO.......",
        "......OGGGGO.OGGO..OGGGGO.......",
        ".......OOOOGGGGGGGGOOOOO........",
        "......OGGGGGGGGGGGGGGGGGO.......",
        ".....OGGWWWWWWGGGWWWWWWGGO......",
        ".....OGGWWBBWWGGGWWBBWWGGO......",
        ".....OGGWWBBWWGGGWWBBWWGGO......",
        ".....OGGWWWWWWGGGWWWWWWGGO......",
        "......OGGGGGGGGGGGGGGGGGO.......",
        ".......OOOGDDDDDDDDGOOOO........",
        "....OO..OGDDDDDDDDDDGO..OO......",
        "..OOGGOOOGDDDDWWDDDDGOOOGGOO....",
        ".OGWWGOOOGDDDDDDDDDDGOOOGWWGO...",
        "OGGGGGGGGGDDDDDDDDDDGGGGGGGGGO..",
        ".OGGGGGGGODDDDDDDDDDOGGGGGGGO...",
        "..OOGGGO.ODDDDDDDDDDO.OGGGOO....",
        ".........ODDDDDDDDDDO...........",
        "..........ODDDDDDDDO............",
        "..........OODDDDDDOO............",
        "...........OGGOOGGO.............",
        "...........OGGOOGGO.............",
        "...........OGGOOGGO.............",
        ".........OOGGGOOGGGOO...........",
        ".........OGGWGOOGWGGO...........",
        ".........OOOOOO.OOOOO...........",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    val feralHoundGrid = listOf(
        ".OOO........................OOO.",
        ".ORRO......................ORRO.",
        "..ORRO.....OOOOOOOOOO.....ORRO..",
        "..ODDDDOOODDDDDDDDDDDDOOODDDDO..",
        ".ODDDDDDRRWWDDDDDDDDWWRRDDDDDDO.",
        ".ODDDDDDRRRWDDDDDDDDWRRRDDDDDDO.",
        ".ODDDDDDDDDDDDDDDDDDDDDDDDDDDDO.",
        "OODDDDDDDWWWWWWWWWWWWWWDDDDDDDOO",
        "ODDDDDDDDW.W.W.W.W.W.WWDDDDDDDDO",
        ".OODDDDDDWWWWWWWWWWWWWWDDDDDDOO.",
        "..ODDDDDDDDDDRRRRRRDDDDDDDDDDO..",
        "..ODDDDDDDRRRWWWWWWRRRDDDDDDDO..",
        "...ODDDDDDDDDDDDDDDDDDDDDDDDO...",
        "...ODDDDDDD..ODDDDDO..DDDDDDO...",
        "...ODDDDDD...ODDDDDO...DDDDDO...",
        "...ODDDDD....ODDDDDO....DDDDO...",
        "...OWWWWO....OWWWWWO....OWWWO...",
        "...OOOOOO....OOOOOOO....OOOOO...",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // 32x32 Cyan Moth / Butterfly (user sketch): two curved antennae, cute dot eyes, slender abdomen & 4 rounded wings with ring eyespots
    val sandMothGrid = listOf(
        "..........OO......OO............",
        "..........OYO....OYO............",
        "...........OYO..OYO.............",
        "............OOOOOO..............",
        "...........ODDDDDDO.............",
        "..........ODDWWDDWWDO...........",
        "..........ODDBBDDDBDO...........",
        "....OOOOOOODDDDDDDDOOOOOOOO.....",
        "..OOCCCCCCCCOODDDDOOCCCCCCCCOO..",
        ".OCCCCCCCCCCCOODDDOCCCCCCCCCCCO.",
        "OCCCCYYYYCCCCOODDDOCCCCYYYYCCCCO",
        "OCCYYWWWWYYCCOODDDOOCCYYWWWWYYCO",
        "OCCYWWOOOWYCCOODDDOOCCYWWOOOWYCO",
        "OCCYYWWWWYYCCOODDDOOCCYYWWWWYYCO",
        "OCCCCYYYYCCCCOODDDOCCCCYYYYCCCCO",
        ".OCCCCCCCCCCCOODDDOCCCCCCCCCCCO.",
        "..OOOOOOCCCOOODDDDOOOCCCOOOOOO..",
        "...OOCCCCCCOOODDDDOOOCCCCCCOO...",
        "..OCCCCYYYCCOODDDDOOCCYYYCCCCO..",
        "..OCCYWWWWYCOODDDDOOCYWWWWYCCO..",
        "..OCCYWOOWYCOODDDDOOCYWOOWYCCO..",
        "..OCCYWWWWYCOODDDDOOCYWWWWYCCO..",
        "..OCCCCYYYCCOOODDDOOCCYYYCCCCO..",
        "...OOCCCCCCOO.ODDO.OOCCCCCCOO...",
        ".....OOOOOO...ODDO...OOOOOO.....",
        "..............OOOO..............",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    val ironStalkerGrid = listOf(
        "....OO....................OO....",
        "....ORRO.OOOOOOOOOOOOOO.ORRO....",
        ".....ORROODDDDDDDDDDDDOORRO.....",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        "....ODDDDDDRRRRRRRRRRDDDDDDO....",
        "....ODDDDDWWWWWWWWWWWWDDDDDO....",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        "..OOO.OOOOOOOOOOOOOOOOOOOO.OOO..",
        ".ORRROODDDDDDDWWWWDDDDDDDOORRRO.",
        ".ORRROODDDDDDYYYYYYDDDDDDOORRRO.",
        ".ORRROODDDDDYYYYYYYYDDDDDOORRRO.",
        "..OOO.ODDDDDDDWWWWDDDDDDDO.OOO..",
        "......ODDDDDDDDDDDDDDDDDDO......",
        "......ODDDDDDD....DDDDDDDO......",
        "......ODDDDDD......DDDDDDO......",
        ".....OODDDDD........DDDDDOO.....",
        ".....ORRRRRO........ORRRRRO.....",
        ".....OOOOOOO........OOOOOOO.....",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    val haloScribeGrid = listOf(
        ".......YYYYYYYYYYYYYYYYYY.......",
        "......YOOOOOOOOOOOOOOOOOOY......",
        "........OOOOOOOOOOOOOOOO........",
        ".......OPPPPPPPPPPPPPPPPO.......",
        "......OPPPPWWPPPPPPWWPPPPO......",
        "......OPPPPWBPPPPPPWBPPPPO......",
        "......OPPPPPPPPPPPPPPPPPPO......",
        "...OO..OPPPPPPPPPPPPPPPPO..OO...",
        "..OWWOOOOOOOOOOOOOOOOOOOOOOWWO..",
        ".OPPPPOPPPPPPPPYYPPPPPPPOPPPPO..",
        "..OPPPPOPPPPPYYYYYYPPPPPOPPPPO..",
        "...OOOO..OPPPYYYYYYPPPO..OOOO...",
        "........OPPPPPPPPPPPPPPO........",
        ".........OPPPWWWWWWPPPO.........",
        "..........OPPPPPPPPPPO..........",
        "...........OPPPPPPPPO...........",
        "............OOOOOOOO............",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // 32x32 Devil / Cinder Imp (user sketch): pointed horns, V-brow over huge eyes, 3-claw spread bat wings, standing legs & long curved arrow tail
    val ashImpGrid = listOf(
        ".....OO..................OO.....",
        ".....ORRO....OOOOOO....ORRO.....",
        "......ORROOODDDDDDDDOOORRO......",
        ".......ODDDDDDDDDDDDDDDDO.......",
        "......ODDDDDDDDDDDDDDDDDDO......",
        "......ODDDWWYDDDDDDYWWDDDO......",
        "......ODDDWRWDDDDDDWRWDDDO......",
        ".......ODDDDDDDDDDDDDDDDO.......",
        ".......OODDDDDDDDDDDDDDOO.......",
        "....OOOOOODDDDDDDDDDDDOOOOOO....",
        "..OORRRRRRODDDDDDDDDDORRRRRROO..",
        ".ORRROORRRRODDDDDDDDORRRROORRRO.",
        "ORRO.ORRO.ODDDDDDDDDDO.ORRO.ORRO",
        ".OO..OO...ODDDDDDDDDDO...OO..OO.",
        "..........ODDDDDDDDDDO..........",
        "...........ODDDDDDDDO.........OO",
        "...........ODDD..DDDO........OYO",
        "..........ODDD....DDDO......ORRO",
        "..........ORRO....ORRO.....ORRO.",
        "........OOORRO....ORRROO..ORRO..",
        "........ORRRRO....ORRRROOORRO...",
        "........OOOOOO....OOOOOOOOOO....",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    // 32x32 Stray Walker Human (user sketch): big round head, tall large eyes, compact body, straight raised arms & legs
    val humanDrifterGrid = listOf(
        "..........OOOOOOOOOOOO..........",
        "........OODSSSSSSSSSSDOO........",
        ".......ODSSSSSSSSSSSSSSDO.......",
        "......ODSSSSSSWWWWSSSSSSDO......",
        "......ODSSSOWWSSSSWWOOSSDO......",
        "......ODSSSWBBSSSSWBBWSSDO......",
        "......ODSSSWBBSSSSWBBWSSDO......",
        "......ODSSSWWWSSSSWWWWSSDO......",
        ".......ODSSSSSSSSSSSSSSDO.......",
        "........OODSSSSSSSSSSDOO........",
        "..........OOOOOOOOOOOO..........",
        "....OO....ODDDDDDDDDDO....OO....",
        "..OORROO..ODDDDDDDDDDO..OORROO..",
        ".ORRRRROOOODDDDDDDDDDOOOORRRRRO.",
        "..OOOO...OODDDDDDDDDDOO...OOOO..",
        "..........ODDDDDDDDDDO..........",
        "..........ODDDDDDDDDDO..........",
        "...........ODDDDDDDDO...........",
        "...........ODDO..ODDO...........",
        "...........ODDO..ODDO...........",
        "...........ODDO..ODDO...........",
        "...........ODDO..ODDO...........",
        "..........OODDO..ODDOO..........",
        "..........OOOOO..OOOOO..........",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )

    val govMonsterChiefGrid = listOf(
        ".....OOO................OOO.....",
        "....OGGGO..............OGGGO....",
        "....OGGGGO............OGGGGO....",
        ".....OGGGGOOOOOOOOOOOOGGGGO.....",
        ".....ODDDDDDDDDDDDDDDDDDDDO.....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        "....ODDDDYYYYDDDDDDYYYYDDDDO....",
        "...ODDDDDYYWWDDDDDDWWYYDDDDDO...",
        "...ODDDDDWWWWDDDDDDWWWWDDDDDO...",
        "...ODDDDDDDDDDDDDDDDDDDDDDDDO...",
        "...ODDDDDDDDYYYYYYYYDDDDDDDDO...",
        "....ODDDDDDDWWWWWWWWDDDDDDDO....",
        "....OODDDDDDDDDDDDDDDDDDDDOO....",
        "..OOODDDDDGGGGGGGGGGGGDDDDDOOO..",
        ".ODDDDDDDGGYYYYYYYYYYGGDDDDDDDO.",
        ".ODDDDDDGGYDDDDDDDDDDYGGDDDDDDO.",
        ".ODDDDDDDGDDDDGGGGDDDDGDDDDDDDO.",
        ".ODDDDDDDDDDDGYYYYGDDDDDDDDDDDO.",
        "..OOODDDDDDDDGYYYYGDDDDDDDOOOO..",
        "...ODDDDDDDDDDGGGGDDDDDDDDDDO...",
        "...ODDDDDDDDDDDDDDDDDDDDDDDDO...",
        "...ODDDDDDDYYYYYYYYYYDDDDDDDO...",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        "....ODDDDDDDDDDDDDDDDDDDDDDO....",
        "....ODDDDDDDDOOOOOODDDDDDDDO....",
        "....ODDDDDDDO......ODDDDDDDO....",
        "....ODDDDDDDO......ODDDDDDDO....",
        "....ODDDDDDDO......ODDDDDDDO....",
        "....OGGGGGGO........OGGGGGGO....",
        "....OOOOOOOO........OOOOOOOO....",
        "................................",
        "................................"
    )

    val govDirectorGrid = listOf(
        "..........OOOOOOOOOOOO..........",
        ".........OYYYYYYYYYYYYO.........",
        "........OYYRRRRRRRRRRYYO........",
        "........ODDDDDDDDDDDDDDO........",
        ".......ODDDDDDDDDDDDDDDDO.......",
        ".......ODDDYYYYDDYYYYDDDO.......",
        ".......ODDDYYWWDDWWYYDDDO.......",
        ".......ODDDDDDDDDDDDDDDDO.......",
        "........ODDDDYYYYYYDDDDO........",
        "........OODDDDDDDDDDDDOO........",
        "......OOODDDDDDDDDDDDDDOOO......",
        "....OODDDDYYRRRRRRRRYYDDDDOO....",
        "...ODDDDDDYRDDDDDDDDYDDDDDDDO...",
        "...ODDDDDDYRDDDYYDDDYDDDDDDDO...",
        "..ODDDDDDDYRDDDWWDDDYDDDDDDDDO..",
        "..ODDDDDDDDYRRRRRRRRYDDDDDDDDO..",
        "..ODDDDDDDDDYYYYYYYYDDDDDDDDDO..",
        "..ORRODDDDDDDDDDDDDDDDDDDDORRO..",
        "..ORRODDDDDDRRRRRRRRDDDDDDORRO..",
        "..OOO.ODDDDDDDDDDDDDDDDDDO.OOO..",
        "......ODDDDDDDDDDDDDDDDDDO......",
        "......ODDDDDDDDDDDDDDDDDDO......",
        "......ODDDDDDRRRRRRDDDDDDO......",
        "......ODDDDDDOOOOOODDDDDDO......",
        "......ODDDDDDO....ODDDDDDO......",
        "......ODDDDDDO....ODDDDDDO......",
        "......ODDDDDDO....ODDDDDDO......",
        "......ORRRRRO......ORRRRRO......",
        "......OOOOOOO......OOOOOOO......",
        "................................",
        "................................",
        "................................"
    )

    private val sculpted64Cache = HashMap<Int, List<String>>()

    /**
     * Expands 32x32 Undertale sprite grids into unified crisp 64x64 pixel-art using EPX / Scale2x
     * diagonal contour sculpting so every boss & monster shares Loka's 64x64 pixel density with
     * smooth stepped silhouettes and zero anti-aliased blur.
     */
    fun sculptGridTo64(rows: List<String>): List<String> {
        if (rows.isEmpty()) return rows
        val w = rows.maxOf { it.length }
        val h = rows.size
        if (w >= 64 || h >= 64) return rows
        val cacheKey = System.identityHashCode(rows) * 31 + h
        sculpted64Cache[cacheKey]?.let { return it }

        fun chAt(x: Int, y: Int): Char {
            if (y !in 0 until h) return '.'
            val row = rows[y]
            return if (x in 0 until row.length) row[x] else '.'
        }

        val out = ArrayList<String>(h * 2)
        for (y in 0 until h) {
            val topBuf = StringBuilder(w * 2)
            val botBuf = StringBuilder(w * 2)
            for (x in 0 until w) {
                val p = chAt(x, y)
                val a = chAt(x, y - 1)
                val b = chAt(x + 1, y)
                val c = chAt(x - 1, y)
                val d = chAt(x, y + 1)

                val p00 = if (c == a && c != d && a != b) a else p
                val p01 = if (a == b && a != c && b != d) b else p
                val p10 = if (d == c && d != b && c != a) c else p
                val p11 = if (b == d && b != a && d != c) d else p

                topBuf.append(p00).append(p01)
                botBuf.append(p10).append(p11)
            }
            out.add(topBuf.toString())
            out.add(botBuf.toString())
        }
        sculpted64Cache[cacheKey] = out
        return out
    }
}

fun DrawScope.drawUndertaleBossPixelSprite(
    enemyId: EnemyId,
    center: Offset,
    sizePx: Float,
    isHitFlash: Boolean,
    isSpareReady: Boolean,
    monochromeBattle: Boolean = false,
    dummyPhase: Int = 1,
    dummyHatFallProgress: Float = 0f
) {
    val lineCol = when {
        isHitFlash -> Color.White
        isSpareReady -> MercyYellow
        else -> Color.White
    }

    val dummyGrid = if (dummyPhase >= 2) PixelSprites.ruinsDummyPhase2Grid else PixelSprites.ruinsDummyGrid

    val (grid, palette) = if (monochromeBattle) {
        // 100% Authentic Undertale White-on-Black Battle Sprite Palette:
        // Solid bright White bodies/armor with pure Black eyes, seams, and interior details!
        val mainBody = when {
            isHitFlash -> Color.White
            isSpareReady -> MercyYellow
            else -> Color.White
        }
        val lightShade = when {
            isHitFlash -> Color.White
            isSpareReady -> Color(0xFFFDE047)
            else -> Color(0xFFD4D4D8)
        }
        val midShade = when {
            isHitFlash -> Color.White
            isSpareReady -> Color(0xFFCA8A04)
            else -> Color(0xFF9CA3AF)
        }
        val darkSeam = if (isHitFlash) Color.White else Color(0xFF000000)
        val hatColor = when {
            isHitFlash -> Color.White
            isSpareReady -> MercyYellow
            else -> Color(0xFF3B82F6)
        }
        val hatHighlight = when {
            isHitFlash -> Color.White
            isSpareReady -> Color(0xFFFDE047)
            else -> Color(0xFF93C5FD)
        }
        val eyeGlint = when {
            isHitFlash -> Color.White
            enemyId == EnemyId.ECHO_LOKA -> GlitchCyan
            enemyId == EnemyId.FUTURE_LOKA -> SoulSeraphCyan
            else -> darkSeam
        }
        val brightAccent = when {
            isHitFlash -> Color.White
            enemyId == EnemyId.ECHO_LOKA -> GlitchCyan
            enemyId == EnemyId.FUTURE_LOKA -> MercyYellow
            else -> mainBody
        }

        when (enemyId) {
            EnemyId.RUINS_DUMMY -> dummyGrid to mapOf(
                'O' to lineCol, 'L' to mainBody, 'C' to lightShade,
                'W' to Color.White, 'B' to darkSeam, 'M' to midShade,
                'H' to hatColor, 'h' to hatHighlight
            )
            EnemyId.VEX -> PixelSprites.vexBossGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'A' to lightShade, 'W' to darkSeam
            )
            EnemyId.BRAMBLE -> PixelSprites.brambleBossGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'G' to lightShade, 'B' to darkSeam, 'W' to darkSeam
            )
            EnemyId.VESPERA -> PixelSprites.vesperaBossGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'C' to lightShade, 'Y' to darkSeam, 'W' to mainBody
            )
            EnemyId.ORIN -> PixelSprites.orinBossGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'B' to midShade, 'W' to darkSeam
            )
            EnemyId.SERAPHIEL -> PixelSprites.seraphielBossGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'C' to lightShade, 'Y' to darkSeam
            )
            EnemyId.MALAKOR -> PixelSprites.malakorBossGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'V' to lightShade, 'W' to darkSeam
            )
            EnemyId.ECHO_LOKA -> PixelSprites.echoLokaBossGrid to mapOf(
                'O' to (if (isHitFlash) Color.White else GlitchCyan),
                'D' to mainBody, 'C' to eyeGlint, 'W' to darkSeam
            )
            EnemyId.FUTURE_LOKA -> PixelSprites.futureLokaBossGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'H' to midShade,
                'S' to lightShade, 'C' to eyeGlint, 'Y' to brightAccent
            )
            EnemyId.NORMAL_SLIME -> PixelSprites.normalSlimeGrid to mapOf(
                'O' to lineCol, 'G' to mainBody, 'D' to midShade, 'B' to darkSeam, 'W' to Color.White
            )
            EnemyId.FERAL_HOUND -> PixelSprites.feralHoundGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'R' to darkSeam, 'W' to darkSeam
            )
            EnemyId.SAND_MOTH -> PixelSprites.sandMothGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'C' to lightShade, 'Y' to midShade, 'W' to mainBody, 'B' to darkSeam
            )
            EnemyId.IRON_STALKER -> PixelSprites.ironStalkerGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'R' to lightShade, 'Y' to darkSeam, 'W' to darkSeam
            )
            EnemyId.HALO_SCRIBE -> PixelSprites.haloScribeGrid to mapOf(
                'O' to lineCol, 'Y' to brightAccent, 'P' to mainBody, 'B' to darkSeam, 'W' to darkSeam
            )
            EnemyId.ASH_IMP -> PixelSprites.ashImpGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'R' to lightShade, 'Y' to midShade, 'W' to darkSeam
            )
            EnemyId.HUMAN_DRIFTER -> PixelSprites.humanDrifterGrid to mapOf(
                'O' to lineCol, 'S' to mainBody, 'D' to lightShade, 'R' to midShade, 'W' to Color.White, 'B' to darkSeam
            )
            EnemyId.GOV_MONSTER_CHIEF -> PixelSprites.govMonsterChiefGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'G' to lightShade, 'Y' to darkSeam, 'W' to darkSeam
            )
            EnemyId.GOV_DIRECTOR -> PixelSprites.govDirectorGrid to mapOf(
                'O' to lineCol, 'D' to mainBody, 'R' to lightShade, 'Y' to darkSeam, 'W' to darkSeam
            )
        }
    } else {
        // Full-color Overworld Sprites
        val darkFill = if (isHitFlash) Color.White else Color(0xFF0E0E14)
        val white = if (isSpareReady && !isHitFlash) MercyYellow else Color.White
        when (enemyId) {
            EnemyId.RUINS_DUMMY -> dummyGrid to mapOf(
                'O' to lineCol,
                'L' to (if (isHitFlash) Color.White else Color(0xFFF3E8D2)),
                'C' to (if (isHitFlash) Color.White else Color(0xFFD6C29F)),
                'W' to white,
                'B' to Color(0xFF1F1624),
                'M' to (if (isHitFlash) Color.White else Color(0xFF8B5E3C)),
                'H' to (if (isHitFlash) Color.White else Color(0xFF2563EB)),
                'h' to (if (isHitFlash) Color.White else Color(0xFF93C5FD))
            )
            EnemyId.VEX -> PixelSprites.vexBossGrid to mapOf(
                'O' to lineCol, 'D' to Color(0xFF1E293B), 'A' to (if (isHitFlash) Color.White else SoulAegisAmber), 'W' to white
            )
            EnemyId.BRAMBLE -> PixelSprites.brambleBossGrid to mapOf(
                'O' to lineCol, 'D' to Color(0xFF1F2A22), 'G' to (if (isHitFlash) Color.White else Color(0xFF4ADE80)), 'B' to Color(0xFF09090B), 'W' to white
            )
            EnemyId.VESPERA -> PixelSprites.vesperaBossGrid to mapOf(
                'O' to lineCol, 'D' to Color(0xFF2E1065), 'C' to (if (isHitFlash) Color.White else SoulSeraphCyan), 'Y' to (if (isHitFlash) Color.White else MercyYellow), 'W' to white
            )
            EnemyId.ORIN -> PixelSprites.orinBossGrid to mapOf(
                'O' to lineCol, 'D' to Color(0xFF334155), 'B' to (if (isHitFlash) Color.White else Color(0xFF3B82F6)), 'W' to white
            )
            EnemyId.SERAPHIEL -> PixelSprites.seraphielBossGrid to mapOf(
                'O' to lineCol, 'D' to Color(0xFF1E293B), 'C' to (if (isHitFlash) Color.White else SoulSeraphCyan), 'Y' to (if (isHitFlash) Color.White else SepiaGold)
            )
            EnemyId.MALAKOR -> PixelSprites.malakorBossGrid to mapOf(
                'O' to lineCol, 'D' to Color(0xFF2B121E), 'V' to (if (isHitFlash) Color.White else SoulInfernalViolet), 'W' to white
            )
            EnemyId.ECHO_LOKA -> PixelSprites.echoLokaBossGrid to mapOf(
                'O' to (if (isHitFlash) Color.White else GlitchCyan), 'D' to darkFill, 'C' to (if (isHitFlash) Color.White else GlitchCyan), 'W' to white
            )
            EnemyId.FUTURE_LOKA -> PixelSprites.futureLokaBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'H' to (if (isHitFlash) Color.White else Color(0xFF4A2E1B)),
                'S' to (if (isHitFlash) Color.White else Color(0xFFEBC7A8)),
                'C' to (if (isHitFlash) Color.White else SoulSeraphCyan),
                'Y' to (if (isHitFlash) Color.White else SepiaGold)
            )
            EnemyId.NORMAL_SLIME -> PixelSprites.normalSlimeGrid to mapOf(
                'O' to lineCol, 'G' to (if (isHitFlash) Color.White else Color(0xFF4ADE80)), 'D' to (if (isHitFlash) Color.White else Color(0xFF14532D)), 'B' to Color(0xFF0A0A0D), 'W' to white
            )
            EnemyId.FERAL_HOUND -> PixelSprites.feralHoundGrid to mapOf(
                'O' to lineCol, 'D' to (if (isHitFlash) Color.White else Color(0xFF27222D)), 'R' to (if (isHitFlash) Color.White else Color(0xFFEF4444)), 'W' to white
            )
            EnemyId.SAND_MOTH -> PixelSprites.sandMothGrid to mapOf(
                'O' to lineCol, 'D' to (if (isHitFlash) Color.White else Color(0xFF422C1D)), 'C' to (if (isHitFlash) Color.White else SoulSeraphCyan), 'Y' to (if (isHitFlash) Color.White else MercyYellow), 'W' to white, 'B' to Color(0xFF0A0A0D)
            )
            EnemyId.IRON_STALKER -> PixelSprites.ironStalkerGrid to mapOf(
                'O' to lineCol, 'D' to (if (isHitFlash) Color.White else Color(0xFF334155)), 'R' to (if (isHitFlash) Color.White else Color(0xFFF97316)), 'Y' to (if (isHitFlash) Color.White else MercyYellow), 'W' to white
            )
            EnemyId.HALO_SCRIBE -> PixelSprites.haloScribeGrid to mapOf(
                'O' to lineCol, 'Y' to (if (isHitFlash) Color.White else MercyYellow), 'P' to (if (isHitFlash) Color.White else Color(0xFFF472B6)), 'B' to Color(0xFF0A0A0D), 'W' to white
            )
            EnemyId.ASH_IMP -> PixelSprites.ashImpGrid to mapOf(
                'O' to lineCol, 'D' to (if (isHitFlash) Color.White else Color(0xFF331B24)), 'R' to (if (isHitFlash) Color.White else SoulParadoxCrimson), 'Y' to (if (isHitFlash) Color.White else Color(0xFFF59E0B)), 'W' to white
            )
            EnemyId.HUMAN_DRIFTER -> PixelSprites.humanDrifterGrid to mapOf(
                'O' to lineCol, 'S' to (if (isHitFlash) Color.White else Color(0xFFEBC7A8)), 'D' to (if (isHitFlash) Color.White else Color(0xFF334155)), 'R' to (if (isHitFlash) Color.White else SepiaGold), 'W' to white, 'B' to Color(0xFF0A0A0D)
            )
            EnemyId.GOV_MONSTER_CHIEF -> PixelSprites.govMonsterChiefGrid to mapOf(
                'O' to lineCol, 'D' to (if (isHitFlash) Color.White else Color(0xFF1E293B)), 'G' to (if (isHitFlash) Color.White else Color(0xFF4ADE80)), 'Y' to (if (isHitFlash) Color.White else MercyYellow), 'W' to white
            )
            EnemyId.GOV_DIRECTOR -> PixelSprites.govDirectorGrid to mapOf(
                'O' to lineCol, 'D' to (if (isHitFlash) Color.White else Color(0xFF1F1218)), 'R' to (if (isHitFlash) Color.White else Color(0xFFEF4444)), 'Y' to (if (isHitFlash) Color.White else MercyYellow), 'W' to white
            )
        }
    }

    when (enemyId) {
        EnemyId.SERAPHIEL, EnemyId.VESPERA -> drawWingPair(
            center = Offset(center.x, center.y - sizePx * 0.05f),
            spanPx = sizePx * 1.25f,
            isAngelWings = true,
            fillColor = when {
                isHitFlash -> Color.White
                monochromeBattle -> Color(0xFF1E1E24)
                else -> SoulSeraphCyan.copy(alpha = 0.25f)
            },
            outlineColor = lineCol
        )
        EnemyId.MALAKOR -> drawWingPair(
            center = Offset(center.x, center.y - sizePx * 0.05f),
            spanPx = sizePx * 1.25f,
            isAngelWings = false,
            fillColor = when {
                isHitFlash -> Color.White
                monochromeBattle -> Color(0xFF1E1E24)
                else -> SoulInfernalViolet.copy(alpha = 0.28f)
            },
            outlineColor = lineCol
        )
        else -> Unit
    }

    val refinedGrid = PixelSprites.sculptGridTo64(grid)
    drawPixelMatrix(rows = refinedGrid, palette = palette, center = center, targetSizePx = sizePx)

    if (enemyId == EnemyId.RUINS_DUMMY && dummyPhase >= 2) {
        drawDummyPhase2Overlay(
            center = center,
            sizePx = sizePx,
            hatFallProgress = dummyHatFallProgress,
            isHitFlash = isHitFlash,
            monochromeBattle = monochromeBattle
        )
    }
}

/**
 * Draws Phase 2 Ruins Dummy effects: the blue top hat arcing from the head to the left floor
 * ("انيميشن سقوط الطاقية") plus crisp pixel cotton puffs ("قطن") bursting around the pedestal base.
 */
fun DrawScope.drawDummyPhase2Overlay(
    center: Offset,
    sizePx: Float,
    hatFallProgress: Float,
    isHitFlash: Boolean,
    monochromeBattle: Boolean
) {
    val t = hatFallProgress.coerceIn(0f, 1f)
    val inv = 1f - t
    // Quadratic bezier arc from head crown -> high left peak -> left ground next to base
    val p0 = Offset(center.x, center.y - sizePx * 0.38f)
    val pCtrl = Offset(center.x - sizePx * 0.28f, center.y - sizePx * 0.56f)
    val pEnd = Offset(center.x - sizePx * 0.39f, center.y + sizePx * 0.34f)
    val hatPos = Offset(
        x = inv * inv * p0.x + 2f * inv * t * pCtrl.x + t * t * pEnd.x,
        y = inv * inv * p0.y + 2f * inv * t * pCtrl.y + t * t * pEnd.y
    )
    // Gentle wobble angle mid-air settling flat at 0 deg on landing
    val tiltDeg = -28f * sin(t * Math.PI.toFloat())

    val hatPalette = mapOf(
        'O' to Color.White,
        'H' to if (isHitFlash) Color.White else if (monochromeBattle) Color(0xFF3B82F6) else Color(0xFF2563EB),
        'h' to if (isHitFlash) Color.White else Color(0xFF93C5FD),
        'B' to Color(0xFF0A0A0E)
    )

    rotate(degrees = tiltDeg, pivot = hatPos) {
        drawPixelMatrix(
            rows = PixelSprites.dummyTopHatGrid,
            palette = hatPalette,
            center = hatPos,
            targetSizePx = sizePx * 0.28f
        )
    }

    // Motion trail dots behind the falling hat while airborne
    if (t in 0.05f..0.90f) {
        for (i in 1..3) {
            val tt = (t - i * 0.08f).coerceAtLeast(0f)
            val ti = 1f - tt
            val trailPos = Offset(
                x = ti * ti * p0.x + 2f * ti * tt * pCtrl.x + tt * tt * pEnd.x,
                y = ti * ti * p0.y + 2f * ti * tt * pCtrl.y + tt * tt * pEnd.y
            )
            val dotPx = sizePx * 0.03f
            drawRect(
                color = Color(0xFF93C5FD).copy(alpha = (1f - i * 0.25f) * 0.75f),
                topLeft = Offset(trailPos.x - dotPx * 0.5f, trailPos.y - dotPx * 0.5f),
                size = Size(dotPx, dotPx)
            )
        }
    }

    // Cotton puffs ("قطن") bursting onto the floor around the dummy base & waist
    val cottonScale = (t * 1.45f).coerceIn(0.25f, 1f)
    val cottonPalette = mapOf(
        'O' to Color.White,
        'W' to Color.White,
        'C' to Color(0xFFE4E4E7)
    )
    val puffOffsets = listOf(
        Offset(-0.22f, 0.36f),
        Offset(0.26f, 0.35f),
        Offset(0.36f, 0.27f)
    )
    puffOffsets.forEachIndexed { idx, rel ->
        val popLift = (1f - cottonScale) * (0.14f + idx * 0.04f)
        val pc = Offset(
            center.x + rel.x * sizePx,
            center.y + (rel.y - popLift) * sizePx
        )
        drawPixelMatrix(
            rows = PixelSprites.cottonPuffGrid,
            palette = cottonPalette,
            center = pc,
            targetSizePx = sizePx * (0.14f + (idx % 2) * 0.03f) * cottonScale
        )
    }
}

/**
 * Stepped Angel & Devil Wing Pairs.
 */
fun DrawScope.drawWingPair(
    center: Offset,
    spanPx: Float,
    isAngelWings: Boolean,
    fillColor: Color,
    outlineColor: Color
) {
    val half = spanPx * 0.5f
    for (dir in listOf(-1f, 1f)) {
        val path = Path().apply {
            if (isAngelWings) {
                moveTo(center.x + dir * half * 0.18f, center.y - half * 0.22f)
                lineTo(center.x + dir * half * 0.98f, center.y - half * 0.34f)
                lineTo(center.x + dir * half * 0.98f, center.y - half * 0.14f)
                lineTo(center.x + dir * half * 0.72f, center.y - half * 0.14f)
                lineTo(center.x + dir * half * 0.72f, center.y + half * 0.04f)
                lineTo(center.x + dir * half * 0.50f, center.y + half * 0.04f)
                lineTo(center.x + dir * half * 0.50f, center.y + half * 0.20f)
                lineTo(center.x + dir * half * 0.18f, center.y + half * 0.20f)
                close()
            } else {
                moveTo(center.x + dir * half * 0.18f, center.y - half * 0.12f)
                lineTo(center.x + dir * half * 0.52f, center.y - half * 0.44f)
                lineTo(center.x + dir * half * 0.66f, center.y - half * 0.18f)
                lineTo(center.x + dir * half * 0.98f, center.y - half * 0.38f)
                lineTo(center.x + dir * half * 0.80f, center.y + half * 0.18f)
                lineTo(center.x + dir * half * 0.48f, center.y + half * 0.02f)
                lineTo(center.x + dir * half * 0.30f, center.y + half * 0.22f)
                lineTo(center.x + dir * half * 0.18f, center.y + half * 0.14f)
                close()
            }
        }
        drawPath(path, fillColor)
        drawPath(path, outlineColor, style = Stroke(2.4f))
    }
}

/**
 * Undertale Ruins Chamber Props: Grain Sack ("كيس"), Wooden Crate ("صندوق"), & Stone Pillar Accent.
 */
fun DrawScope.drawDummyRoomProps(
    dummyCenter: Offset,
    unitPx: Float
) {
    // Left Pixel Sack ("كيس") — stepped Undertale grain sack with pixel knot
    val sackCenter = Offset(dummyCenter.x - unitPx * 3.1f, dummyCenter.y + unitPx * 0.55f)
    drawSteppedPixelShadow(Offset(sackCenter.x, sackCenter.y + unitPx * 0.85f), unitPx * 2.3f, unitPx * 0.55f)
    val sackGrid = listOf(
        "...OOOO...",
        "...OMMO...",
        "..OOOOOO..",
        ".OOLLLLOO.",
        "OMMLLLLMMO",
        "OMMMMMMMMO",
        "OMDDDDDDMO",
        ".ODDDDDDO.",
        "..OOOOOO.."
    )
    drawPixelMatrix(
        rows = sackGrid,
        palette = mapOf(
            'O' to Color(0xFF140E0A),
            'L' to Color(0xFFD6BE96),
            'M' to Color(0xFFB59A73),
            'D' to Color(0xFF785135)
        ),
        center = sackCenter,
        targetSizePx = unitPx * 2.2f
    )

    // Right Wood-Framed Pixel Crate ("صندوق")
    val crateLeft = dummyCenter.x + unitPx * 2.1f
    val crateTop = dummyCenter.y - unitPx * 0.30f
    val crateSize = unitPx * 1.85f
    drawSteppedPixelShadow(Offset(crateLeft + crateSize * 0.5f, crateTop + crateSize * 1.02f), crateSize + 4f, unitPx * 0.45f)
    drawRect(Color(0xFF785135), Offset(crateLeft, crateTop), Size(crateSize, crateSize))
    drawRect(Color(0xFF9C6B43), Offset(crateLeft, crateTop), Size(crateSize, crateSize * 0.24f))
    for (s in 0..4) {
        val bx = crateLeft + s * (crateSize / 5f)
        val by = crateTop + s * (crateSize / 5f)
        drawRect(Color(0xFF5A3921), Offset(bx, by), Size(crateSize / 5f, crateSize / 5f))
    }
    drawRect(Color(0xFF3E2717), Offset(crateLeft + crateSize * 0.35f, crateTop), Size(2.dp.toPx(), crateSize))
    drawRect(Color(0xFF3E2717), Offset(crateLeft + crateSize * 0.68f, crateTop), Size(2.dp.toPx(), crateSize))
    drawRect(Color(0xFFE9D5FF), Offset(crateLeft, crateTop), Size(crateSize, crateSize), style = Stroke(2f))
}

/**
 * Pure Undertale Stepped Pixel-Art Monster Chomp Head + 3-Prong Claw Strike.
 */
fun DrawScope.drawBossChompHeadAndClaw(
    enemyId: EnemyId,
    jawCenter: Offset,
    clawCenter: Offset,
    sizePx: Float,
    isChomping: Boolean,
    accentColor: Color
) {
    val eyeChar = if (enemyId == EnemyId.FUTURE_LOKA || enemyId == EnemyId.SERAPHIEL) 'C' else 'A'
    val headGrid = if (isChomping) {
        listOf(
            "..OOOOOOOO..",
            ".OWWWWWWWWO.",
            "OWW$eyeChar$eyeChar" + "WW$eyeChar$eyeChar" + "WWO",
            "OWWWWWWWWWWO",
            "OW.W.W.W.W.O",
            "OWW.W.W.W.WO",
            ".OOOOOOOOOO."
        )
    } else {
        listOf(
            "..OOOOOOOO..",
            ".ODDDDDDDDO.",
            "ODD$eyeChar$eyeChar" + "DD$eyeChar$eyeChar" + "DDO",
            "ODDDDDDDDDDO",
            "OW.W.W.W.W.O",
            "............",
            "OW.W.W.W.W.O",
            ".OOOOOOOOOO."
        )
    }
    drawPixelMatrix(
        rows = headGrid,
        palette = mapOf(
            'O' to if (isChomping) accentColor else Color.White,
            'W' to Color.White,
            'D' to Color(0xFF0A0A0D),
            'C' to Color(0xFF38BDF8),
            'A' to accentColor
        ),
        center = jawCenter,
        targetSizePx = sizePx * 1.15f
    )
    val clawGrid = listOf(
        ".OOO....",
        "OAAOOO..",
        "...OO...",
        "OOOOOO..",
        "...OO...",
        "OAAOOO..",
        ".OOO...."
    )
    drawPixelMatrix(
        rows = clawGrid,
        palette = mapOf(
            'O' to if (isChomping) Color.White else accentColor,
            'A' to if (isChomping) accentColor else Color(0xFF141418)
        ),
        center = clawCenter,
        targetSizePx = sizePx * 0.95f
    )
}

/**
 * Pure Undertale 10x12 Pixel Fuse Bomb ("قنبلة").
 */
fun DrawScope.drawPixelBomb(
    center: Offset,
    radiusPx: Float,
    fuseProgress: Float,
    glowColor: Color
) {
    val blink = fuseProgress > 0.72f
    val bombGrid = listOf(
        "......FF.S",
        ".....F..SS",
        "...OOOO...",
        "..OLLLLOO.",
        ".OLWWDDDD.",
        ".OLWDDDDDO",
        ".ODDDDDDDO",
        ".ODDDDDDDO",
        "..ODDDDDO.",
        "...OOOOO.."
    )
    drawPixelMatrix(
        rows = bombGrid,
        palette = mapOf(
            'O' to if (blink) Color.Red else Color.White,
            'L' to Color(0xFF27272F),
            'D' to Color(0xFF09090D),
            'W' to Color.White,
            'F' to Color(0xFFD6C29F),
            'S' to if (((fuseProgress * 20f).toInt() % 2) == 0) Color.Yellow else glowColor
        ),
        center = center,
        targetSizePx = radiusPx * 2.5f
    )
}

/**
 * Stepped Pixel-Art Directional Arrow Telegraph.
 */
fun DrawScope.drawDirectionalArrow(
    origin: Offset,
    dirX: Float,
    dirY: Float,
    lengthPx: Float,
    color: Color
) {
    val px = (lengthPx * 0.14f).coerceIn(3.5f, 7f)
    val steps = (lengthPx / px).toInt().coerceIn(4, 16)
    for (i in 0 until steps) {
        val t = i.toFloat() / steps
        val sx = origin.x + dirX * lengthPx * t
        val sy = origin.y + dirY * lengthPx * t
        drawRect(color, Offset(sx - px * 0.5f, sy - px * 0.5f), Size(px, px))
    }
    val tip = Offset(origin.x + dirX * lengthPx, origin.y + dirY * lengthPx)
    val perpX = -dirY
    val perpY = dirX
    drawRect(color, Offset(tip.x - px * 0.7f, tip.y - px * 0.7f), Size(px * 1.4f, px * 1.4f))
    for (ring in 1..2) {
        val backX = tip.x - dirX * px * ring
        val backY = tip.y - dirY * px * ring
        val span = px * ring
        drawRect(color, Offset(backX + perpX * span - px * 0.5f, backY + perpY * span - px * 0.5f), Size(px, px))
        drawRect(color, Offset(backX - perpX * span - px * 0.5f, backY - perpY * span - px * 0.5f), Size(px, px))
    }
}

/**
 * Overworld High-Detail 32x32 Micro-Pixel NPC Sprite (rendered at 512x512).
 */
fun DrawScope.drawOverworldNpcSprite(
    npc: WorldNpc,
    center: Offset,
    unitPx: Float
) {
    drawSteppedPixelShadow(
        center = Offset(center.x, center.y + unitPx * 1.05f),
        widthPx = unitPx * 1.95f,
        heightPx = unitPx * 0.52f
    )
    val isAngel = npc.race.contains("Angel", ignoreCase = true)
    val isDevil = npc.race.contains("Devil", ignoreCase = true)
    val isMonster = npc.race.contains("Monster", ignoreCase = true)
    if (isAngel || isDevil) {
        drawWingPair(
            center = Offset(center.x, center.y - unitPx * 0.1f),
            spanPx = unitPx * 3.1f,
            isAngelWings = isAngel,
            fillColor = npc.accentColor.copy(alpha = 0.45f),
            outlineColor = Color.White
        )
    }

    val grid = when {
        isAngel -> listOf(
            "..........OOOOOOOOOOOO..........",
            ".........OYYYYYYYYYYYYO.........",
            "........OYYWWWWWWWWWWYYO........",
            ".........OOOOOOOOOOOOOO.........",
            ".........OOWWWWWWWWWWO..........",
            "........OWWWWWWWWWWWWWWO........",
            ".......OWWWSSSSSSSSSSWWWO.......",
            ".......OWWSSSSSSSSSSSSWWO.......",
            ".......OWWSSWWBSSBWWSSWWO.......",
            ".......OWWSSWWBSSBWWSSWWO.......",
            ".......OWWSSSSSSSSSSSSWWO.......",
            "........OWSSSSSSmmSSSSWO........",
            ".........OSSSSSSSSSSSSO.........",
            "..........OOSSSSSSSSOO..........",
            "........OOOWWAAAAAAWWOOO........",
            ".......OWWWAAAAAAAAAAAAWO.......",
            "......OWWAAAAYYYYYYAAAAWWO......",
            "......OWWAAAYYWWWWYYAAAWWO......",
            "......OWWAAAAYYYYYYAAAAWWO......",
            ".......OAAAAAAAAAAAAAAAAO.......",
            ".......OCCCCCCCCCCCCCCCCO.......",
            "........OCCCCCYYYYCCCCCO........",
            "........OCCCCCCCCCCCCCCO........",
            ".........OCCCCCCCCCCCCO.........",
            ".........OCCCCOOOOCCCCO.........",
            ".........OCCCCO..OCCCCO.........",
            ".........OCCCCO..OCCCCO.........",
            ".........OBBBBO..OBBBBO.........",
            ".........OOOOOO..OOOOOO.........",
            "................................",
            "................................",
            "................................"
        )
        isDevil -> listOf(
            "...OO......................OO...",
            "...OAO....................OAO...",
            "....OAO..................OAO....",
            "....OAAO....OOOOOOOO....OAAO....",
            ".....OOAO..ODDDDDDDDO..OAOO.....",
            ".....OODDDDDDDDDDDDDDDDDDOO.....",
            "......ODDDDDDDDDDDDDDDDDDO......",
            "......ODDDWWDDDDDDDDWWDDDO......",
            "......ODDDWWADDDDDDAWWDDDO......",
            "......ODDDDDDDDDDDDDDDDDDO......",
            ".......ODDDDDDWWWWDDDDDDO.......",
            "........OODDDDDDDDDDDDOO........",
            ".......OOCCCCCCCCCCCCCCOO.......",
            "......OCCCCAAAAWWWWAAAACCO......",
            ".....OCCCCCAAAAWWWWAAAACCCO.....",
            ".....OCCCCCAAAAAAAAAAAACCCO.....",
            "......OCCCCCCCCCCCCCCCCCCO......",
            "......OCCCCAAAYYYYAAAACCCO......",
            ".......OCCCCCCCCCCCCCCCCO.......",
            "........OCCCCCCCCCCCCCCO........",
            "........OCCCCCCOOCCCCCCO........",
            ".........ODDDDD..DDDDDO.........",
            ".........ODDDDD..DDDDDO.........",
            ".........ODDDDD..DDDDDO.........",
            ".........ODDDDD..DDDDDO.........",
            ".........OBBBBB..BBBBBO.........",
            ".........OOOOOO..OOOOOO.........",
            "................................",
            "................................",
            "................................",
            "................................",
            "................................"
        )
        isMonster -> listOf(
            "........OOOOOOOOOOOOOOOO........",
            "......OOAAAAAAAAAAAAAAAAOO......",
            ".....OAAWWAAAAAAAAAAAAWWAAO.....",
            "....OAAWWWWAAOOOOOOAAWWWWAAO....",
            "....OAAWWWBAAAAAAAAAAAWBBAAO....",
            "....OAAWWWBAAAAAAAAAAAWBBAAO....",
            "....OAAAAAAAAAAAAAAAAAAAAAAO....",
            ".....OAAOOAAAAAAAAAAAAOOAAO.....",
            ".....OAOOOWWWWWWWWWWWWOOAAO.....",
            "......OOAAAAAAAAAAAAAAAAOO......",
            "......OOCCCCCCCCCCCCCCCCOO......",
            ".....OCCCCCAAAAWWWWAAAACCCO.....",
            "....OCCCCCCAAAAWWWWAAAACCCCO....",
            "....OCCCCCCAAAAAAAAAAAACCCCO....",
            ".....OCCCCCCCCCCCCCCCCCCCCO.....",
            ".....OCCCCCAAAAAAAAAAACCCCO.....",
            "......OCCCCCCCCCCCCCCCCCCO......",
            ".......OCCCCCCOOOOCCCCCCCO......",
            "........ODDDDD....DDDDDO........",
            "........ODDDDD....DDDDDO........",
            "........ODDDDD....DDDDDO........",
            "........ODDDDD....DDDDDO........",
            "........OBBBBB....BBBBBO........",
            "........OOOOOO....OOOOOO........",
            "................................",
            "................................",
            "................................",
            "................................",
            "................................",
            "................................",
            "................................",
            "................................"
        )
        else -> listOf(
            "........OOOOOOOOOOOOOOOO........",
            "......OOHHHHHHHHHHHHHHHHOO......",
            ".....OHHHHHHhHHHHHHhHHHHHHO.....",
            "....OHHHHHHHHHHHHHHHHHHHHHHO....",
            "....OHHHSSSSSSSSSSSSSSSSHHHO....",
            "....OHHSSSSSSSSSSSSSSSSSSSHO....",
            "....OHHSSSWWBSSSSSSBWWSSSHHO....",
            "....OHHSSSWWBSSSSSSBWWSSSHHO....",
            ".....OSSSSSSSSSSSSSSSSSSSSO.....",
            "......OSSSSSSmmmmmmSSSSSSO......",
            ".......OOSSSSSSSSSSSSSSOO.......",
            "......OOOWWWAAAAAAAAWWWOOO......",
            ".....OSSSAAAAAAAAAAAAAAAASSO....",
            "....OSSSSAAAAYYYYYYAAAAASSSSO...",
            "....OSSSSAAAAAAAAAAAAAAAASSSO...",
            ".....OSSSAAAAAAAAAAAAAAAASSO....",
            "......OOOPPPPPPPPPPPPPPPPOOO....",
            ".......OPPPPPPYYYYYPPPPPPPO.....",
            ".......OPPPPPPPPPPPPPPPPPPO.....",
            "........OPPPPPP..PPPPPPPO.......",
            "........OPPPPPP..PPPPPPPO.......",
            "........OBBBBBB..BBBBBBBO.......",
            "........OBBBBBB..BBBBBBBO.......",
            "........OOOOOOO..OOOOOOO........",
            "................................",
            "................................",
            "................................",
            "................................",
            "................................",
            "................................",
            "................................",
            "................................"
        )
    }
    val palette = mapOf(
        'O' to Color(0xFF0A0A0D),
        '0' to Color(0xFF0A0A0D),
        'D' to Color(0xFF1F1622),
        'Y' to MercyYellow,
        'S' to Color(0xFFEBC7A8),
        'H' to Color(0xFF5A3825),
        'h' to Color(0xFF8C583A),
        'm' to Color(0xFFC47A68),
        'B' to Color(0xFF1F2937),
        'W' to Color.White,
        'A' to npc.accentColor,
        'C' to npc.accentColor.copy(alpha = 0.72f),
        'P' to Color(0xFF334155)
    )
    drawPixelMatrix(grid, palette, center, unitPx * 2.85f)
}

/**
 * Overworld High-Detail 32x32 Micro-Pixel Lost Paradox Kid Sprite with pulsing beacon aura.
 */
fun DrawScope.drawOverworldKidSprite(
    kid: LostKid,
    center: Offset,
    unitPx: Float,
    isRescued: Boolean
) {
    val auraColor = if (isRescued) Color(0xFF4ADE80) else MercyYellow
    drawRect(
        color = auraColor.copy(alpha = 0.65f),
        topLeft = Offset(center.x - unitPx * 1.35f, center.y - unitPx * 1.35f),
        size = Size(unitPx * 2.7f, unitPx * 2.7f),
        style = Stroke(1.8f)
    )
    drawSteppedPixelShadow(
        center = Offset(center.x, center.y + unitPx * 0.96f),
        widthPx = unitPx * 1.6f,
        heightPx = unitPx * 0.46f
    )
    val grid = listOf(
        ".........OOOOOOOOOOOOOO.........",
        ".......OOHHHHhhhhhhHHHHOO.......",
        "......OHHhhHHHHHHHHHHhhHHO......",
        ".....OHHHHHHHHHHHHHHHHHHHHO.....",
        ".....OHHHhHHHHHHHHHHHHhHHHO.....",
        ".....OHHHSSSSSSSSSSSSSSSHHO.....",
        ".....OHHSSSSSSSSSSSSSSSSSHO.....",
        ".....OHHSSWWBBSSSSBBWWSSSHO.....",
        ".....OHHSSWWBBSSSSBBWWSSSHO.....",
        "......OSSSSSSrrrrrrSSSSSSO......",
        "......OSSSSSSSSSSSSSSSSSSO......",
        ".......OOSSSSSSSSSSSSSSOO.......",
        "......OOWWWAAAAAAAAAAWWWOO......",
        ".....OSSAAAAAAAAAAAAAAAAASSO....",
        ".....OSSAAAAWWWWWWWWAAAAASSO....",
        ".....OSSAAAAAAAAAAAAAAAAASSO....",
        "......OAAAAAAAAAAAAAAAAAAAO.....",
        "......OOOPPPPPPPPPPPPPPOOO......",
        ".......OPPPPPPPPPPPPPPPPO.......",
        ".......OPPPPPP....PPPPPPO.......",
        ".......OPPPPPP....PPPPPPO.......",
        ".......OBBBBBB....BBBBBBO.......",
        ".......OBBBBBB....BBBBBBO.......",
        ".......OOOOOOO....OOOOOOO.......",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................",
        "................................"
    )
    val palette = mapOf(
        'O' to Color(0xFF120C08),
        'H' to Color(0xFF78350F),
        'h' to Color(0xFFB45309),
        'S' to Color(0xFFEBC7A8),
        'r' to Color(0xFFE08C7E),
        'B' to Color(0xFF1E293B),
        'W' to Color.White,
        'A' to kid.accentColor,
        'P' to Color(0xFF334155)
    )
    drawPixelMatrix(grid, palette, center, unitPx * 2.55f)
}

/**
 * Overworld Stepped Pixel-Art Buildings & Landmarks (Houses, Labs, Caves, and Hills).
 */
fun DrawScope.drawOverworldLandmarkSprite(
    kind: LandmarkKind,
    center: Offset,
    sizePx: Float,
    accent: Color,
    isSecret: Boolean,
    completed: Boolean
) {
    val rim = if (completed) Color(0xFF4ADE80) else if (isSecret) GlitchCyan else Color.White
    val half = sizePx * 0.5f

    drawSteppedPixelShadow(
        center = Offset(center.x, center.y + half * 0.80f),
        widthPx = half * 2.15f,
        heightPx = half * 0.46f
    )

    when (kind) {
        LandmarkKind.MANSION -> {
            drawRect(Color(0xFF4B3832), Offset(center.x + half * 0.42f, center.y - half * 0.96f), Size(half * 0.24f, half * 0.52f))
            drawRect(rim, Offset(center.x + half * 0.42f, center.y - half * 0.96f), Size(half * 0.24f, half * 0.52f), style = Stroke(1.6f))
            drawRect(accent, Offset(center.x - half * 1.10f, center.y - half * 0.36f), Size(half * 2.20f, half * 0.24f))
            drawRect(accent, Offset(center.x - half * 0.82f, center.y - half * 0.62f), Size(half * 1.64f, half * 0.26f))
            drawRect(accent, Offset(center.x - half * 0.48f, center.y - half * 0.86f), Size(half * 0.96f, half * 0.24f))
            drawRect(rim, Offset(center.x - half * 1.10f, center.y - half * 0.36f), Size(half * 2.20f, half * 0.24f), style = Stroke(1.6f))

            drawRect(Color(0xFF2B2433), Offset(center.x - half * 0.86f, center.y - half * 0.14f), Size(half * 1.72f, half * 1.00f))
            drawRect(Color(0xFF1D1824), Offset(center.x - half * 0.86f, center.y + half * 0.66f), Size(half * 1.72f, half * 0.20f))
            drawRect(rim, Offset(center.x - half * 0.86f, center.y - half * 0.14f), Size(half * 1.72f, half * 1.00f), style = Stroke(2f))
            for (wx in listOf(-0.62f, 0.28f)) {
                drawRect(MercyYellow, Offset(center.x + half * wx, center.y + half * 0.04f), Size(half * 0.34f, half * 0.34f))
                drawRect(Color(0xFF27222E), Offset(center.x + half * (wx + 0.15f), center.y + half * 0.04f), Size(2f, half * 0.34f))
                drawRect(Color(0xFF27222E), Offset(center.x + half * wx, center.y + half * 0.19f), Size(half * 0.34f, 2f))
            }
            drawRect(Color(0xFF6B4423), Offset(center.x - half * 0.19f, center.y + half * 0.22f), Size(half * 0.38f, half * 0.63f))
            drawRect(MercyYellow, Offset(center.x + half * 0.08f, center.y + half * 0.50f), Size(3.dp.toPx(), 3.dp.toPx()))
            drawRect(Color(0xFF9CA3AF), Offset(center.x - half * 0.28f, center.y + half * 0.84f), Size(half * 0.56f, half * 0.12f))
        }

        LandmarkKind.LAB -> {
            drawRect(rim, Offset(center.x - 1.5f, center.y - half * 1.08f), Size(3f, half * 0.48f))
            val orb = if (isSecret) GlitchCyan else accent
            drawRect(orb, Offset(center.x - half * 0.14f, center.y - half * 1.24f), Size(half * 0.28f, half * 0.28f))
            drawRect(Color.White, Offset(center.x - half * 0.06f, center.y - half * 1.18f), Size(half * 0.10f, half * 0.10f))
            drawRect(Color(0xFF1F2937), Offset(center.x - half * 0.96f, center.y - half * 0.62f), Size(half * 1.92f, half * 1.42f))
            drawRect(Color(0xFF334155), Offset(center.x - half * 0.96f, center.y - half * 0.62f), Size(half * 1.92f, half * 0.24f))
            drawRect(rim, Offset(center.x - half * 0.96f, center.y - half * 0.62f), Size(half * 1.92f, half * 1.42f), style = Stroke(2.2f))
            val visorColor = if (isSecret) GlitchCyan else Color(0xFF38BDF8)
            drawRect(visorColor, Offset(center.x - half * 0.66f, center.y - half * 0.30f), Size(half * 1.32f, half * 0.36f))
            drawRect(Color.White, Offset(center.x - half * 0.52f, center.y - half * 0.22f), Size(half * 0.50f, 3f))
            drawRect(Color(0xFF0F172A), Offset(center.x - half * 0.24f, center.y + half * 0.22f), Size(half * 0.48f, half * 0.58f))
            drawRect(visorColor, Offset(center.x - 1f, center.y + half * 0.22f), Size(2f, half * 0.58f))
        }

        LandmarkKind.CAVE -> {
            drawRect(Color(0xFF473729), Offset(center.x - half * 0.96f, center.y - half * 0.24f), Size(half * 1.92f, half * 1.00f))
            drawRect(Color(0xFF5D4837), Offset(center.x - half * 0.76f, center.y - half * 0.54f), Size(half * 1.52f, half * 0.32f))
            drawRect(Color(0xFF725943), Offset(center.x - half * 0.48f, center.y - half * 0.72f), Size(half * 0.96f, half * 0.20f))
            drawRect(rim, Offset(center.x - half * 0.96f, center.y - half * 0.24f), Size(half * 1.92f, half * 1.00f), style = Stroke(2f))
            drawRect(Color(0xFF1F1612), Offset(center.x - half * 0.48f, center.y - half * 0.04f), Size(half * 0.96f, half * 0.80f))
            drawRect(Color(0xFF060509), Offset(center.x - half * 0.34f, center.y + half * 0.12f), Size(half * 0.68f, half * 0.64f))
            drawRect(accent, Offset(center.x + half * 0.56f, center.y + half * 0.28f), Size(half * 0.16f, half * 0.34f))
            drawRect(Color.White, Offset(center.x + half * 0.58f, center.y + half * 0.30f), Size(half * 0.06f, half * 0.16f))
        }

        LandmarkKind.HILLS -> {
            drawRect(accent.copy(alpha = 0.78f), Offset(center.x - half * 1.02f, center.y + half * 0.34f), Size(half * 2.04f, half * 0.40f))
            drawRect(accent.copy(alpha = 0.88f), Offset(center.x - half * 0.74f, center.y - half * 0.04f), Size(half * 1.48f, half * 0.38f))
            drawRect(accent, Offset(center.x - half * 0.42f, center.y - half * 0.40f), Size(half * 0.84f, half * 0.36f))
            drawRect(Color.White, Offset(center.x - half * 0.22f, center.y - half * 0.58f), Size(half * 0.44f, half * 0.18f))
            drawRect(rim, Offset(center.x - half * 1.02f, center.y + half * 0.34f), Size(half * 2.04f, half * 0.40f), style = Stroke(1.8f))
            drawRect(Color.White, Offset(center.x - 1f, center.y - half * 1.06f), Size(2.5f, half * 0.48f))
            drawRect(if (completed) Color(0xFF4ADE80) else MercyYellow, Offset(center.x + 1f, center.y - half * 1.06f), Size(half * 0.46f, half * 0.26f))
        }
    }
}

/**
 * Overworld Ashen Save Point: monochrome Ashen flower on a stepped soil patch with golden Save sparkles.
 */
fun DrawScope.drawOverworldAshenSaveSprite(
    center: Offset,
    sizePx: Float,
    ambientClock: Float
) {
    val pulse = 1f + 0.06f * sin(ambientClock * 3.8f)
    drawRect(
        color = MercyYellow.copy(alpha = 0.65f),
        topLeft = Offset(center.x - sizePx * 0.54f * pulse, center.y - sizePx * 0.54f * pulse),
        size = Size(sizePx * 1.08f * pulse, sizePx * 1.08f * pulse),
        style = Stroke(2f)
    )
    drawSteppedPixelShadow(
        center = Offset(center.x, center.y + sizePx * 0.30f),
        widthPx = sizePx * 0.92f,
        heightPx = sizePx * 0.26f,
        color = Color(0xFF1F1D24)
    )
    val palette = mapOf(
        'O' to Color(0xFF09090B),
        'B' to Color(0xFF09090B),
        'D' to Color(0xFF3F3F46),
        'G' to Color(0xFF71717A),
        'P' to Color(0xFFD4D4D8),
        'W' to Color(0xFFF4F4F5)
    )
    drawPixelMatrix(
        rows = PixelSprites.ashenFlowerNormal,
        palette = palette,
        center = Offset(center.x, center.y - sizePx * 0.04f),
        targetSizePx = sizePx * 0.90f
    )
    drawPixelSaveStar(
        center = Offset(center.x + sizePx * 0.48f, center.y - sizePx * 0.38f + sin(ambientClock * 4.5f) * 2.5f),
        sizePx = sizePx * 0.18f,
        color = MercyYellow
    )
}

/**
 * Overworld Underground Pixel Burrow Mound displayed after Ashen saves & hides for ~1m 15s.
 */
fun DrawScope.drawOverworldAshenBurrow(
    center: Offset,
    sizePx: Float,
    secondsRemaining: Float,
    ambientClock: Float
) {
    drawSteppedPixelShadow(
        center = Offset(center.x, center.y + sizePx * 0.24f),
        widthPx = sizePx * 0.96f,
        heightPx = sizePx * 0.38f,
        color = Color(0xFF151218)
    )
    drawRect(Color(0xFF3F3F46), Offset(center.x - sizePx * 0.38f, center.y + sizePx * 0.10f), Size(sizePx * 0.76f, sizePx * 0.26f), style = Stroke(2f))
    drawRect(Color(0xFF040407), Offset(center.x - sizePx * 0.22f, center.y + sizePx * 0.15f), Size(sizePx * 0.44f, sizePx * 0.16f))
    val progress = (1f - (secondsRemaining / 75f)).coerceIn(0f, 1f)
    val barW = sizePx * 0.92f
    drawRect(Color(0xFF111116), Offset(center.x - barW * 0.5f, center.y - sizePx * 0.10f), Size(barW, 5.dp.toPx()))
    drawRect(Color(0xFFD4D4D8), Offset(center.x - barW * 0.5f, center.y - sizePx * 0.10f), Size(barW * progress, 5.dp.toPx()))
    drawRect(Color.White.copy(alpha = 0.7f), Offset(center.x - barW * 0.5f, center.y - sizePx * 0.10f), Size(barW, 5.dp.toPx()), style = Stroke(1.2f))
}

/**
 * Overworld Pixel Stone Doorways for all 5 doors per world.
 */
fun DrawScope.drawZoneDoorway(
    door: ZoneDoorInfo,
    world: WorldId,
    center: Offset,
    ambientClock: Float,
    hasUnresolvedBoss: Boolean = false
) {
    val isWorldGate = door.targetWorld != null
    val accent = when {
        isWorldGate -> GlitchCyan
        hasUnresolvedBoss -> MercyYellow
        world == WorldId.NORMAL_WORLD -> MercyYellow
        world == WorldId.TRI_RACE_REALM -> SoulSeraphCyan
        world == WorldId.SPLIT_REALM -> if (door.doorX < 0.5f) Color(0xFFEC4899) else Color(0xFFEF4444)
        world == WorldId.GOVERNMENT_HQ -> SepiaGold
        else -> GlitchCyan
    }
    val u = 8f
    val pulse = 0.75f + 0.25f * sin(ambientClock * 4f)

    if (door.edge == null) {
        drawRect(Color(0xFF1F2937), Offset(center.x - u * 3.0f, center.y - u * 2.8f), Size(u * 6.0f, u * 5.2f))
        drawRect(Color(0xFF334155), Offset(center.x - u * 3.2f, center.y - u * 3.2f), Size(u * 6.4f, u * 1.1f))
        drawRect(GlitchCyan, Offset(center.x - u * 3.0f, center.y - u * 2.8f), Size(u * 6.0f, u * 5.2f), style = Stroke(2.2f))
        drawRect(Color(0xFF090910), Offset(center.x - u * 1.8f, center.y - u * 1.6f), Size(u * 3.6f, u * 4.0f))
        drawRect(MercyYellow.copy(alpha = pulse), Offset(center.x - u * 1.4f, center.y - u * 1.2f), Size(u * 2.8f, u * 3.4f), style = Stroke(2f))
        drawPixelSaveStar(center = Offset(center.x, center.y - u * 0.2f), sizePx = u * 1.1f, color = GlitchCyan)
        return
    }

    val isHorizontal = door.edge == ZoneEdge.TOP || door.edge == ZoneEdge.BOTTOM
    val gw = if (isHorizontal) u * 8.2f else u * 3.0f
    val gh = if (isHorizontal) u * 3.0f else u * 8.2f

    drawRect(Color(0xFF1A1B22), Offset(center.x - gw * 0.5f, center.y - gh * 0.5f), Size(gw, gh))
    drawRect(Color(0xFF050508), Offset(center.x - gw * 0.36f, center.y - gh * 0.36f), Size(gw * 0.72f, gh * 0.72f))
    if (isWorldGate) {
        drawRect(MercyYellow.copy(alpha = pulse), Offset(center.x - gw * 0.58f, center.y - gh * 0.58f), Size(gw * 1.16f, gh * 1.16f), style = Stroke(2.4f))
    }
    drawRect(accent.copy(alpha = pulse), Offset(center.x - gw * 0.5f, center.y - gh * 0.5f), Size(gw, gh), style = Stroke(if (isWorldGate || hasUnresolvedBoss) 3f else 2.2f))
    val (dx, dy) = when (door.edge) {
        ZoneEdge.TOP -> 0f to -1f
        ZoneEdge.BOTTOM -> 0f to 1f
        ZoneEdge.LEFT -> -1f to 0f
        ZoneEdge.RIGHT -> 1f to 0f
    }
    drawDirectionalArrow(
        origin = Offset(center.x - dx * u * 0.75f, center.y - dy * u * 0.75f),
        dirX = dx,
        dirY = dy,
        lengthPx = u * 1.7f,
        color = accent.copy(alpha = pulse)
    )
    if (hasUnresolvedBoss || isWorldGate) {
        drawPixelSaveStar(center = center, sizePx = u * 0.95f, color = if (isWorldGate) GlitchCyan else MercyYellow)
    }
}

/**
 * Overworld Roaming Monster Sprite with stepped Undertale shadow & crisp pixel [!] alert box.
 */
fun DrawScope.drawOverworldChasingMonster(
    enemyId: EnemyId,
    center: Offset,
    sizePx: Float,
    accentColor: Color,
    canBefriend: Boolean,
    isAlreadyFriend: Boolean,
    isChasing: Boolean,
    ambientClock: Float
) {
    val ringColor = when {
        isAlreadyFriend -> Color(0xFF4ADE80)
        canBefriend -> accentColor
        else -> Color(0xFFEF4444)
    }
    drawRect(
        color = ringColor.copy(alpha = if (isChasing) 0.55f else 0.25f),
        topLeft = Offset(center.x - sizePx * 0.56f, center.y - sizePx * 0.56f),
        size = Size(sizePx * 1.12f, sizePx * 1.12f),
        style = Stroke(1.8f)
    )
    drawSteppedPixelShadow(
        center = Offset(center.x, center.y + sizePx * 0.40f),
        widthPx = sizePx * 0.84f,
        heightPx = sizePx * 0.22f
    )
    drawUndertaleBossPixelSprite(
        enemyId = enemyId,
        center = center,
        sizePx = sizePx,
        isHitFlash = false,
        isSpareReady = isAlreadyFriend,
        monochromeBattle = false
    )
    if (isChasing && !isAlreadyFriend) {
        val bounce = sin(ambientClock * 12f) * 2.5f
        val bx = center.x
        val by = center.y - sizePx * 0.68f + bounce
        drawRect(Color.Black, Offset(bx - 5f, by - 12f), Size(10f, 17f))
        drawRect(Color.White, Offset(bx - 5f, by - 12f), Size(10f, 17f), style = Stroke(1.5f))
        drawRect(Color(0xFFEF4444), Offset(bx - 2f, by - 9.5f), Size(4f, 8f))
        drawRect(Color(0xFFEF4444), Offset(bx - 2f, by + 0.5f), Size(4f, 3f))
    }
}

/**
 * Pixel-Art Dithered Spotlight Cone + Stepped Floor Spotlight Pool + Twinkle Stars.
 */
fun DrawScope.drawPixelSpotlightBeam(
    beamCenterX: Float,
    topWidthPx: Float,
    bottomWidthPx: Float,
    floorCenterY: Float,
    coreColor: Color,
    midColor: Color,
    outerColor: Color,
    ambientClock: Float
) {
    val px = 4.5f
    val topY = 0f
    val beamH = floorCenterY.coerceAtLeast(10f)
    val rows = (beamH / px).toInt().coerceAtLeast(1)

    for (r in 0 until rows) {
        val t = r.toFloat() / rows
        val halfW = (topWidthPx + (bottomWidthPx - topWidthPx) * t) * 0.5f
        val y = topY + r * px
        val minCol = ((beamCenterX - halfW) / px).toInt()
        val maxCol = ((beamCenterX + halfW) / px).toInt()
        for (c in minCol..maxCol) {
            val x = c * px
            val distNorm = kotlin.math.abs((x + px * 0.5f) - beamCenterX) / halfW.coerceAtLeast(1f)
            val checker = (r + c) % 2 == 0
            val color = when {
                t < 0.45f && distNorm < 0.65f -> coreColor
                t < 0.72f && (distNorm < 0.55f || (distNorm < 0.85f && checker)) -> midColor
                distNorm < 0.68f && checker -> midColor
                distNorm < 0.94f && ((r + c * 2) % 3 != 0) -> outerColor
                else -> Color.Transparent
            }
            if (color != Color.Transparent) {
                drawRect(color = color, topLeft = Offset(x, y), size = Size(px + 0.4f, px + 0.4f))
            }
        }
    }

    val poolW = bottomWidthPx * 1.24f
    val poolH = poolW * 0.30f
    drawSteppedPixelShadow(Offset(beamCenterX, floorCenterY), poolW, poolH, outerColor)
    drawSteppedPixelShadow(Offset(beamCenterX, floorCenterY), poolW * 0.74f, poolH * 0.70f, midColor)

    for (i in 0..5) {
        val sx = beamCenterX + sin(i * 2.1f + ambientClock * 0.7f) * bottomWidthPx * 0.48f
        val sy = beamH * (0.18f + (i * 0.13f)) + kotlin.math.cos(ambientClock * 1.6f + i) * 5f
        val twinkle = (0.4f + 0.6f * kotlin.math.abs(sin(ambientClock * 2.5f + i * 1.3f)))
        drawPixelSaveStar(
            center = Offset(sx, sy),
            sizePx = 5.5f * twinkle,
            color = coreColor.copy(alpha = 0.85f * twinkle),
            coreColor = Color.White.copy(alpha = twinkle)
        )
    }
}

/**
 * Stepped Pixel Ruins Stone Archway silhouette in the backdrop.
 */
fun DrawScope.drawAmbientRuinArchway(
    centerX: Float,
    floorY: Float,
    widthPx: Float,
    heightPx: Float,
    wallTint: Color,
    trimTint: Color
) {
    val left = centerX - widthPx * 0.5f
    val top = floorY - heightPx
    drawRect(wallTint, Offset(left, top), Size(widthPx, heightPx))
    drawRect(trimTint, Offset(left, top), Size(widthPx, heightPx), style = Stroke(2.5f))
    val inset = widthPx * 0.22f
    drawRect(Color(0xFF040307), Offset(left + inset * 1.35f, top + heightPx * 0.16f), Size(widthPx - inset * 2.7f, heightPx * 0.10f))
    drawRect(Color(0xFF040307), Offset(left + inset, top + heightPx * 0.24f), Size(widthPx - inset * 2f, heightPx * 0.76f))
    drawRect(trimTint, Offset(centerX - widthPx * 0.14f, top + heightPx * 0.06f), Size(widthPx * 0.28f, heightPx * 0.06f))
    for (dx in listOf(-0.12f, 0f, 0.12f)) {
        val sz = if (dx == 0f) 5f else 3.5f
        drawRect(trimTint, Offset(centerX + widthPx * dx - sz * 0.5f, top + heightPx * 0.17f - sz * 0.5f), Size(sz, sz))
    }
}

/**
 * Shaded Pixel-Art Brick & Moss Pedestal Monument.
 */
fun DrawScope.drawPixelBrickSaveMonument(
    center: Offset,
    sizePx: Float,
    ambientClock: Float,
    brickBase: Color = Color(0xFF9C5E3E),
    brickShadow: Color = Color(0xFF6E3B25),
    brickHighlight: Color = Color(0xFFD68D5A),
    mossColor: Color = Color(0xFF3E6F38),
    emblemColor: Color = MercyYellow
) {
    val w = sizePx
    val h = sizePx * 0.72f
    val left = center.x - w * 0.5f
    val top = center.y - h * 0.35f

    drawSteppedPixelShadow(Offset(center.x, top + h * 0.85f), w * 1.36f, h * 0.58f, mossColor)

    drawRect(brickBase, Offset(left, top), Size(w, h))
    drawRect(brickHighlight, Offset(left, top), Size(w, h * 0.22f))
    drawRect(brickShadow, Offset(left, top + h * 0.80f), Size(w, h * 0.20f))
    drawRect(Color(0xFF331B12), Offset(left, top + h * 0.45f), Size(w, 2f))
    drawRect(Color(0xFF331B12), Offset(left + w * 0.32f, top), Size(2f, h * 0.45f))
    drawRect(Color(0xFF331B12), Offset(left + w * 0.68f, top), Size(2f, h * 0.45f))
    drawRect(Color(0xFF24110B), Offset(left, top), Size(w, h), style = Stroke(2.2f))

    drawRect(Color(0xFF7D4830), Offset(left + w * 0.20f, top + h * 0.52f), Size(w * 0.60f, h * 0.36f))
    drawRect(Color(0xFF24110B), Offset(left + w * 0.20f, top + h * 0.52f), Size(w * 0.60f, h * 0.36f), style = Stroke(1.6f))
    drawRect(Color(0xFF472517), Offset(left + w * 0.28f, top + h * 0.66f), Size(w * 0.44f, 2f))

    listOf(left - w * 0.08f to top + h * 0.82f, left + w * 0.94f to top + h * 0.86f).forEach { (fx, fy) ->
        drawRect(Color(0xFF140E0A), Offset(fx - 4f, fy - 4f), Size(8f, 8f))
        drawRect(emblemColor, Offset(fx - 2.5f, fy - 2.5f), Size(5f, 5f))
        drawRect(Color.White, Offset(fx - 1f, fy - 1f), Size(2f, 2f))
    }

    val floatY = top - h * 0.28f + sin(ambientClock * 3.8f) * 3f
    drawPixelSaveStar(
        center = Offset(center.x, floatY),
        sizePx = w * 0.16f,
        color = emblemColor
    )
}
