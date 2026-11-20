package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.ChestStyle
import com.example.model.EnemyId
import com.example.model.LandmarkKind
import com.example.model.LostKid
import com.example.model.SoulType
import com.example.model.WorldNpc
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaGold
import com.example.ui.theme.SoulAegisAmber
import com.example.ui.theme.SoulInfernalViolet
import com.example.ui.theme.SoulParadoxCrimson
import com.example.ui.theme.SoulSeraphCyan
import kotlin.math.floor

/**
 * Renders crisp Undertale-style pixel art grids snapped cleanly to pixel boundaries.
 */
fun DrawScope.drawPixelMatrix(
    rows: List<String>,
    palette: Map<Char, Color>,
    center: Offset,
    targetSizePx: Float
) {
    if (rows.isEmpty()) return
    val rowCount = rows.size
    val colCount = rows.maxOf { it.length }
    val pxSize = targetSizePx / maxOf(rowCount, colCount)
    val startX = center.x - (colCount * pxSize) * 0.5f
    val startY = center.y - (rowCount * pxSize) * 0.5f

    for (r in rows.indices) {
        val row = rows[r]
        for (c in row.indices) {
            val ch = row[c]
            if (ch == '.' || ch == ' ') continue
            val color = palette[ch] ?: continue
            val x = floor(startX + c * pxSize)
            val y = floor(startY + r * pxSize)
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(pxSize + 0.6f, pxSize + 0.6f)
            )
        }
    }
}

/**
 * Hand-drawn Chunky Stepped Pixel-Block Outline Heart (13x12) matching the user's Soul sketches!
 */
fun DrawScope.drawPixelHeart(
    center: Offset,
    sizePx: Float,
    fillColor: Color,
    outlineColor: Color? = Color(0xFF0A0A0D),
    cracked: Boolean = false
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
    val palette = mutableMapOf(
        'X' to fillColor
    )
    palette['O'] = outlineColor ?: fillColor
    drawPixelMatrix(grid, palette, center, sizePx * 2.05f)

    if (cracked) {
        val crack = listOf(
            ".....C.....",
            "....C......",
            ".....C.....",
            "......C....",
            ".....C....."
        )
        drawPixelMatrix(crack, mapOf('C' to Color(0xFF4B5563)), center, sizePx * 1.15f)
    }
}

/**
 * Image 3 — Split Soul (الروح الانقسامية):
 * Left half Red, Right half Teal, with chunky stepped pixel-block outline.
 * When splitApart == true, draws either left half (halfIndex == 0) or right half (halfIndex == 1).
 */
fun DrawScope.drawSplitPixelHeart(
    center: Offset,
    sizePx: Float,
    leftColor: Color,
    rightColor: Color,
    outlineColor: Color = Color(0xFF0A0A0D),
    splitHalfIndex: Int? = null
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
    drawPixelMatrix(grid, palette, center, scale)
}

/**
 * Image 1 — The 6 Chests (شكل الصناديق):
 * Closed top row:
 *  - SMALL_DOT: square box with lid line + small round clasp
 *  - SQUARE_STAR: compact square chest with star/cross clasp
 *  - WIDE_PADLOCK: wider box with lid line + padlock clasp
 * Open bottom row:
 *  - Raised lid + shaded horizontal opening band inside!
 */
fun DrawScope.drawPixelChest(
    center: Offset,
    sizePx: Float,
    style: ChestStyle,
    isOpen: Boolean,
    accentColor: Color = Color(0xFFFACC15)
) {
    val woodDark = Color(0xFF5C3A21)
    val woodLight = Color(0xFF8C5A32)
    val rim = Color.White
    val shade = Color(0xFF1E1B24)
    val gold = if (isOpen) accentColor.copy(alpha = 0.7f) else accentColor

    val widthFactor = if (style == ChestStyle.WIDE_PADLOCK) 1.38f else if (style == ChestStyle.SMALL_DOT) 1.05f else 0.94f
    val w = sizePx * widthFactor
    val h = sizePx * 0.92f
    val left = center.x - w * 0.5f
    val top = center.y - h * 0.5f

    if (isOpen) {
        // Raised top lid
        drawRect(woodLight, Offset(left, top - h * 0.14f), Size(w, h * 0.34f))
        drawRect(rim, Offset(left, top - h * 0.14f), Size(w, h * 0.34f), style = Stroke(2f))
        // Shaded dark interior band across the chest (matching Image 1 bottom row)
        drawRect(shade, Offset(left + 2f, top + h * 0.20f), Size(w - 4f, h * 0.28f))
        for (i in 0..4) {
            val sx = left + (w * (i + 0.5f) / 5f)
            drawLine(gold.copy(alpha = 0.65f), Offset(sx - 3f, top + h * 0.44f), Offset(sx + 3f, top + h * 0.24f), strokeWidth = 2f)
        }
        // Lower chest base
        drawRect(woodDark, Offset(left, top + h * 0.48f), Size(w, h * 0.52f))
        drawRect(rim, Offset(left, top + h * 0.48f), Size(w, h * 0.52f), style = Stroke(2f))
    } else {
        // Closed chest body & lid seam
        drawRect(woodDark, Offset(left, top), Size(w, h))
        drawRect(woodLight, Offset(left, top), Size(w, h * 0.36f))
        drawRect(rim, Offset(left, top), Size(w, h), style = Stroke(2.2f))
        drawLine(rim, Offset(left, top + h * 0.36f), Offset(left + w, top + h * 0.36f), strokeWidth = 2f)

        val cx = center.x
        val cy = top + h * 0.36f
        when (style) {
            ChestStyle.SMALL_DOT -> {
                drawCircle(gold, radius = h * 0.12f, center = Offset(cx, cy))
                drawCircle(Color.Black, radius = h * 0.12f, center = Offset(cx, cy), style = Stroke(1.5f))
            }
            ChestStyle.SQUARE_STAR -> {
                drawPixelSaveStar(center = Offset(cx, cy), sizePx = h * 0.16f, color = gold, coreColor = Color.White)
            }
            ChestStyle.WIDE_PADLOCK -> {
                drawCircle(gold, radius = h * 0.12f, center = Offset(cx, cy - h * 0.04f), style = Stroke(2.2f))
                drawRect(gold, Offset(cx - h * 0.14f, cy), Size(h * 0.28f, h * 0.26f))
                drawRect(Color.Black, Offset(cx - h * 0.14f, cy), Size(h * 0.28f, h * 0.26f), style = Stroke(1.5f))
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

    // Loka Portrait (18x18): Matches uploaded spiky shaggy hair silhouette (NO hat), sweeping bangs over right side,
    // cyan-grey eye peeking beneath bangs, grey T-shirt, golden heart necklace, and beige pants!
    val lokaPortraitGrid = listOf(
        "......OOOO........",
        ".....OHhHHO.......",
        "....OHHHhHHHO.....",
        "...OHHHHHHhHHHO...",
        "..OHHhHHHHHhHHHO..",
        "..OHHHHHHHHHHHHO..",
        ".OHHHhHHhHHHHHHHO.",
        ".OHHSSHHhHSSSSSHO.",
        "..OHSSShHHSSGCSHO.",
        "..OOHSSSSSSSGCSHO.",
        "...OOHSSSSSSSSSO..",
        ".....OSSSSSSSSO...",
        "...OOOTTTTTTTTO...",
        "..OTTTTGGYYGGTTTO.",
        "..OTTTTGGYYGGTTTO.",
        "..OTTTTTTTTTTTTTO.",
        "....OPPPP..PPPPO..",
        "....OOOOO..OOOOO.."
    )

    // Loka Overworld (12x14): Top-down crisp sprite with spiky shaggy tuft (no hat), grey tee, golden necklace, beige pants
    val lokaOverworldGrid = listOf(
        "....OOOO....",
        "...OHhHHO...",
        "..OHHHHHHO..",
        ".OHHhHHhHHO.",
        ".OHSShSSGHO.",
        "..OSSSSSSO..",
        "..OTTTTTTO..",
        ".OSTTYYTTSO.",
        ".OSTTTTTTSO.",
        "..OTTTTTTO..",
        "..OPPP.PPPO.",
        "..OPPP.PPPO.",
        "..OBB...BBO.",
        "............"
    )

    // Black-and-Grey Flowey-like Flower (Ashen, 16x16) matching Image 8 (شكل الوردة: petals, dot/line eyes, curved stem & bowl base)
    val ashenFlowerNormal = listOf(
        "....OOO..OOO....",
        "...OGGGOOGGGO...",
        "..OOGGDDDDGGOO..",
        ".OGGDDPPPPDDGGO.",
        ".OGGDPPPPPPDGGO.",
        "..OGDPBPPBPDGO..",
        ".OGGDPBPPBPDGGO.",
        ".OGGDPPPPPPDGGO.",
        "..OGGDDDDDDGGO..",
        "....OOODDDOOO...",
        ".......ODDO.....",
        "......ODDO......",
        ".......ODDO.....",
        "...OO..ODDO..OO.",
        "..OGGGGGGGGGGGGO",
        "...OOOOOOOOOOOO."
    )

    val ashenFlowerCreepy = listOf(
        "....OOO..OOO....",
        "...ODDDODDDDO...",
        "..OODDDDDDDDOO..",
        ".ODDDPPPPPPDDDO.",
        ".ODDPBPPPPBPDDO.",
        "..ODPBPPPPBPDO..",
        ".ODDPPPPPPPPDDO.",
        ".ODDPBBBBBBPDDO.",
        "..ODDPBPPBPDDDO.",
        "...ODDDDDDDDO...",
        "....OOODDDOOO...",
        "......ODDO......",
        ".......ODDO.....",
        "...OO..ODDO..OO.",
        "..ODDDDDDDDDDDDO",
        "...OOOOOOOOOOOO."
    )

    // Boss 18x18 Undertale-style high-contrast pixel battle sprites
    // Image 8 Dummy Shape (شكل الدمية): top hat, stitched snout/torso, flared robe bottom, wooden stand
    val ruinsDummyGrid = listOf(
        "......OOOOOO......",
        ".....OWWWWWWO.....",
        "...OOOWWWWWWO.....",
        "...OCCCCCCCCO.....",
        "..OCCECCCDECCO....",
        "..OCCCCCCCCCCCO...",
        "...OOCCMMMMCCO....",
        ".....OCCMCCO......",
        "....OOCMCCCCO.....",
        "....OCCMCCCCCO....",
        "...OCCCEMCCCCCO...",
        "...OCCCCMCCCCCO...",
        "..OCCCCCMCCCCCC0..",
        "..OCCCCCCCCCCCCO..",
        "...OOOOOOOOOOOO...",
        ".......OWWO.......",
        ".....OOWWWWO......",
        "....OOOOOOOOO....."
    )

    val vexBossGrid = listOf(
        ".....OOOOOOOO.....",
        "....ODDDDDDDDO....",
        "...ODDDDDDDDDDO...",
        "...ODAAAAAAAAAO...",
        "...ODAAWWWWAAAO...",
        "...ODDDDDDDDDDO...",
        "....ODDDDDDDDO....",
        "..OO.OOOOOOOO.OO..",
        ".OAAODDDDDDDDOAAO.",
        ".OA.ODDDWWDDDO.AO.",
        "....ODDDDDDDDO....",
        "....ODDDAAADDO....",
        "....ODDDDDDDDO....",
        "....ODDDDDDDDO....",
        "....ODDD..DDDO....",
        "....ODDD..DDDO....",
        "...OODD....DDOO...",
        "...OOOO....OOOO..."
    )

    val brambleBossGrid = listOf(
        ".OOO..........OOO.",
        ".OGGO........OGGO.",
        "..OGGO.OOOO.OGGO..",
        "...OGGDDDDDDGGO...",
        "..ODDDDDDDDDDDDO..",
        ".ODDDDDDDDDDDDDDO.",
        ".ODDGGDDDDDDGGDDO.",
        ".ODDDDDDDDDDDDDDO.",
        ".ODDDWWDDDDWWDDDO.",
        ".ODDDDDDDDDDDDDDO.",
        "..ODDDDDDDDDDDDO..",
        "..ODDDDGGGGDDDDO..",
        "..ODDDDDDDDDDDDO..",
        "...ODDDDDDDDDDO...",
        "...ODDD....DDDO...",
        "...ODDD....DDDO...",
        "..OODD......DDOO..",
        "..OOOO......OOOO.."
    )

    val vesperaBossGrid = listOf(
        "......OOOOOO......",
        ".....ODDDDDDO.....",
        "....ODDDDDDDDO....",
        "....ODDYYDDYYO....",
        "....ODDDDDDDDO....",
        ".....ODDDDDDO.....",
        "....OODDDDDDOO....",
        "...ODDDDDDDDDDO...",
        "..ODDDDDDDDDDDDO..",
        "..ODDDCCCCCCCDO...",
        ".ODDDCCCCCCCCCDO..",
        ".ODDDDDDDDDDDDDO..",
        "ODDDDDDDDDDDDDDDO.",
        "ODDDDDYYDDDDDDDDO.",
        "ODDDDDDDDDDDDDDDO.",
        ".OOOOOOOOOOOOOOO..",
        "....ODDD..DDDO....",
        "....OOOO..OOOO...."
    )

    val orinBossGrid = listOf(
        "....OOOOOOOOOO....",
        "...ODDDDDDDDDDO...",
        "..ODDDDDDDDDDDDO..",
        "..ODDDWWDDWWDDDO..",
        "..ODDDDDDDDDDDDO..",
        "..ODDDDDDDDDDDDO..",
        ".OODDDDDDDDDDDDOO.",
        ".ODDDDBBBBBBBDDDO.",
        ".ODDDBBWWWWBBDDDO.",
        ".ODDDBBWWWWBBDDDO.",
        ".ODDDDBBBBBBBDDDO.",
        ".OODDDDDDDDDDDDOO.",
        "..ODDDDDDDDDDDDO..",
        "..ODDDDDDDDDDDDO..",
        "...ODDDD..DDDDO...",
        "...ODDDD..DDDDO...",
        "..OODDD....DDDOO..",
        "..OOOOO....OOOOO.."
    )

    val seraphielBossGrid = listOf(
        ".....OOOOOOOO.....",
        "....OYYYYYYYYO....",
        "..OO.OOOOOOOO.OO..",
        ".OCCODDDDDDDDOCCO.",
        "OCCCODDDYYDDDOCCCO",
        "OCCCODDDDDDDDOCCCO",
        ".OCC.OODDDDOO.CCO.",
        "OOCCODDDDDDDDOCCOO",
        "OCCODDDDDDDDDDOCCO",
        ".OO.ODDDYYDDDDO.OO",
        "....ODDDDDDDDO....",
        "...ODDDDDDDDDDO...",
        "...ODDDDDDDDDDO...",
        "..ODDDDDDDDDDDDO..",
        "..ODDDDDDDDDDDDO..",
        "..OOOOOOOOOOOOOO..",
        ".....OOO..OOO.....",
        ".....OOO..OOO....."
    )

    val malakorBossGrid = listOf(
        ".OOO..........OOO.",
        ".OVVO........OVVO.",
        "..OVVO.OOOO.OVVO..",
        "...OVODDDDDDOVO...",
        "...ODDDDDDDDDDO...",
        "...ODDVVDDVVDDO...",
        "...ODDDDDDDDDDO...",
        "..OODDDDDDDDDDOO..",
        ".ODDDDDDDDDDDDDDO.",
        ".ODDDDDVVVVDDDDDO.",
        ".ODDDDVWWWWVDDDDO.",
        ".ODDDDDVVVVDDDDDO.",
        "..ODDDDDDDDDDDDO..",
        "..ODDDDDDDDDDDDO..",
        "...ODDD.OO.DDDO...",
        "...ODD..OO..DDO...",
        "...OOO......OOO...",
        ".................."
    )

    val echoLokaBossGrid = listOf(
        ".....OOOOOOOO.....",
        "....OCCCCCCCCO....",
        "...ODDDDDDDDDDO...",
        "...ODDDDDDDDDDO...",
        "...ODWWDDDDCCDO...",
        "...ODDDDDDDDDDO...",
        "....ODDDDDDDDO....",
        "...OODDDDDDDDOO...",
        "..ODDDDDDDDDDDDO..",
        "..ODDDDWWWWDDDDO..",
        "..ODDDWCCCCWDDDO..",
        "..ODDDDWWWWDDDDO..",
        "...ODDDDDDDDDDO...",
        "...ODDD....DDDO...",
        "...ODDD....DDDO...",
        "..OODD......DDOO..",
        "..OOOO......OOOO..",
        ".................."
    )

    val futureLokaBossGrid = listOf(
        "....OOOOOOOOOO....",
        "...OHHHHHHHHHHO...",
        "..OHHHHHHHHHHHHO..",
        "..OHHSSSSSSSSSHO..",
        "..OHSCCSSSSCCSHO..",
        "..OHHHHHHHHHHHHO..",
        "...OSSSSSSSSSSO...",
        "..OODDDDDDDDDDOO..",
        ".ODDDDDDDDDDDDDDO.",
        ".ODDDDYYYYYYDDDDO.",
        ".ODDDDDYYYYDDDDDO.",
        ".ODDDDDDDDDDDDDDO.",
        "ODDDDDDDDDDDDDDDDO",
        "ODDDDDDDDDDDDDDDDO",
        "ODDDDDD....DDDDDDO",
        "ODDDDD......DDDDDO",
        ".OOOOO......OOOOO.",
        ".................."
    )
}

fun DrawScope.drawUndertaleBossPixelSprite(
    enemyId: EnemyId,
    center: Offset,
    sizePx: Float,
    isHitFlash: Boolean,
    isSpareReady: Boolean
) {
    val white = if (isSpareReady && !isHitFlash) MercyYellow else Color.White
    val darkFill = if (isHitFlash) Color.White else Color(0xFF0A0A0D)
    val outline = when {
        isHitFlash -> Color.White
        isSpareReady -> MercyYellow
        else -> Color.White
    }

    val (grid, palette) = when (enemyId) {
        EnemyId.RUINS_DUMMY -> PixelSprites.ruinsDummyGrid to mapOf(
            'O' to outline,
            'C' to (if (isHitFlash) Color.White else Color(0xFFD6C29F)),
            'E' to (if (isHitFlash) Color.White else Color(0xFFD8B4FE)),
            'D' to (if (isHitFlash) Color.White else Color(0xFFD8B4FE)),
            'M' to Color(0xFF4A2E1B),
            '0' to outline,
            'W' to (if (isHitFlash) Color.White else Color(0xFF785135))
        )
        EnemyId.VEX -> PixelSprites.vexBossGrid to mapOf(
            'O' to outline,
            'D' to darkFill,
            'A' to (if (isHitFlash) Color.White else SoulAegisAmber),
            'W' to white
        )
        EnemyId.BRAMBLE -> PixelSprites.brambleBossGrid to mapOf(
            'O' to outline,
            'D' to darkFill,
            'G' to (if (isHitFlash) Color.White else Color(0xFF4ADE80)),
            'W' to white
        )
        EnemyId.VESPERA -> PixelSprites.vesperaBossGrid to mapOf(
            'O' to outline,
            'D' to darkFill,
            'C' to (if (isHitFlash) Color.White else SoulSeraphCyan),
            'Y' to (if (isHitFlash) Color.White else MercyYellow)
        )
        EnemyId.ORIN -> PixelSprites.orinBossGrid to mapOf(
            'O' to outline,
            'D' to darkFill,
            'B' to (if (isHitFlash) Color.White else Color(0xFF3B82F6)),
            'W' to white
        )
        EnemyId.SERAPHIEL -> PixelSprites.seraphielBossGrid to mapOf(
            'O' to outline,
            'D' to darkFill,
            'C' to (if (isHitFlash) Color.White else SoulSeraphCyan),
            'Y' to (if (isHitFlash) Color.White else SepiaGold)
        )
        EnemyId.MALAKOR -> PixelSprites.malakorBossGrid to mapOf(
            'O' to outline,
            'D' to darkFill,
            'V' to (if (isHitFlash) Color.White else SoulInfernalViolet),
            'W' to white
        )
        EnemyId.ECHO_LOKA -> PixelSprites.echoLokaBossGrid to mapOf(
            'O' to (if (isHitFlash) Color.White else GlitchCyan),
            'D' to darkFill,
            'C' to (if (isHitFlash) Color.White else GlitchCyan),
            'W' to white
        )
        EnemyId.FUTURE_LOKA -> PixelSprites.futureLokaBossGrid to mapOf(
            'O' to outline,
            'D' to darkFill,
            'H' to (if (isHitFlash) Color.White else Color(0xFF4A2E1B)),
            'S' to (if (isHitFlash) Color.White else Color(0xFFEBC7A8)),
            'C' to (if (isHitFlash) Color.White else SoulSeraphCyan),
            'Y' to (if (isHitFlash) Color.White else SepiaGold)
        )
    }

    when (enemyId) {
        EnemyId.SERAPHIEL, EnemyId.VESPERA -> drawWingPair(
            center = Offset(center.x, center.y - sizePx * 0.05f),
            spanPx = sizePx * 1.25f,
            isAngelWings = true,
            fillColor = if (isHitFlash) Color.White else SoulSeraphCyan.copy(alpha = 0.25f),
            outlineColor = outline
        )
        EnemyId.MALAKOR -> drawWingPair(
            center = Offset(center.x, center.y - sizePx * 0.05f),
            spanPx = sizePx * 1.25f,
            isAngelWings = false,
            fillColor = if (isHitFlash) Color.White else SoulInfernalViolet.copy(alpha = 0.28f),
            outlineColor = outline
        )
        else -> Unit
    }

    drawPixelMatrix(rows = grid, palette = palette, center = center, targetSizePx = sizePx)
}

/**
 * Image 8 Top (أجنحة الملائكة & أجنحة الشياطين):
 *  - isAngelWings = true: crisp stepped rectangular horizontal feather tiers
 *  - isAngelWings = false: jagged zig-zag demon bat wings
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
        drawPath(path, outlineColor, style = Stroke(2.2f))
    }
}

/**
 * Image 8 Middle (كيس + صندوق) next to the Dummy in the Ruins.
 */
fun DrawScope.drawDummyRoomProps(
    dummyCenter: Offset,
    unitPx: Float
) {
    // Left Sack ("كيس")
    val sackCenter = Offset(dummyCenter.x - unitPx * 3.1f, dummyCenter.y + unitPx * 0.5f)
    drawOval(
        color = Color(0xFFB59A73),
        topLeft = Offset(sackCenter.x - unitPx * 1.1f, sackCenter.y - unitPx * 0.85f),
        size = Size(unitPx * 2.2f, unitPx * 1.75f)
    )
    drawOval(
        color = Color(0xFF2B1838),
        topLeft = Offset(sackCenter.x - unitPx * 1.1f, sackCenter.y - unitPx * 0.85f),
        size = Size(unitPx * 2.2f, unitPx * 1.75f),
        style = Stroke(2.2f)
    )
    drawRect(
        color = Color(0xFF785135),
        topLeft = Offset(sackCenter.x - unitPx * 0.35f, sackCenter.y - unitPx * 1.15f),
        size = Size(unitPx * 0.7f, unitPx * 0.4f)
    )

    // Right Crate ("صندوق")
    val crateLeft = dummyCenter.x + unitPx * 2.1f
    val crateTop = dummyCenter.y - unitPx * 0.35f
    val crateSize = unitPx * 1.85f
    drawRect(Color(0xFF785135), Offset(crateLeft, crateTop), Size(crateSize, crateSize))
    drawRect(Color.White, Offset(crateLeft, crateTop), Size(crateSize, crateSize), style = Stroke(2f))
    drawLine(Color(0xFF3E2717), Offset(crateLeft + crateSize * 0.35f, crateTop), Offset(crateLeft + crateSize * 0.35f, crateTop + crateSize), strokeWidth = 2f)
    drawLine(Color(0xFF3E2717), Offset(crateLeft + crateSize * 0.68f, crateTop), Offset(crateLeft + crateSize * 0.68f, crateTop + crateSize), strokeWidth = 2f)
}

/**
 * Image 2 Top-Right: Monster Chomp Head + Claw Strike themed to whichever boss is currently battling!
 */
fun DrawScope.drawBossChompHeadAndClaw(
    enemyId: EnemyId,
    jawCenter: Offset,
    clawCenter: Offset,
    sizePx: Float,
    isChomping: Boolean,
    accentColor: Color
) {
    val mouthOpen = if (isChomping) sizePx * 0.08f else sizePx * 0.28f
    val fill = if (isChomping) Color.White else accentColor.copy(alpha = 0.35f)
    val outline = if (isChomping) accentColor else Color.White

    // Stylized boss-themed jagged jaw head
    val r = sizePx * 0.55f
    drawCircle(color = Color(0xFF0A0A0D), radius = r, center = jawCenter)
    drawCircle(color = outline, radius = r, center = jawCenter, style = Stroke(3f))
    // Eyes colored by boss
    val eyeColor = if (enemyId == EnemyId.FUTURE_LOKA || enemyId == EnemyId.SERAPHIEL) Color(0xFF38BDF8) else accentColor
    drawCircle(eyeColor, radius = r * 0.16f, center = Offset(jawCenter.x - r * 0.34f, jawCenter.y - r * 0.35f))
    drawCircle(eyeColor, radius = r * 0.16f, center = Offset(jawCenter.x + r * 0.34f, jawCenter.y - r * 0.35f))

    // Upper & lower jagged chomp teeth
    val topTeeth = Path().apply {
        moveTo(jawCenter.x - r * 0.72f, jawCenter.y - mouthOpen * 0.2f)
        lineTo(jawCenter.x - r * 0.40f, jawCenter.y + r * 0.22f - mouthOpen * 0.2f)
        lineTo(jawCenter.x, jawCenter.y - mouthOpen * 0.2f)
        lineTo(jawCenter.x + r * 0.40f, jawCenter.y + r * 0.22f - mouthOpen * 0.2f)
        lineTo(jawCenter.x + r * 0.72f, jawCenter.y - mouthOpen * 0.2f)
    }
    val bottomTeeth = Path().apply {
        moveTo(jawCenter.x - r * 0.72f, jawCenter.y + mouthOpen + r * 0.18f)
        lineTo(jawCenter.x - r * 0.40f, jawCenter.y + mouthOpen - r * 0.14f)
        lineTo(jawCenter.x, jawCenter.y + mouthOpen + r * 0.18f)
        lineTo(jawCenter.x + r * 0.40f, jawCenter.y + mouthOpen - r * 0.14f)
        lineTo(jawCenter.x + r * 0.72f, jawCenter.y + mouthOpen + r * 0.18f)
    }
    drawPath(topTeeth, fill)
    drawPath(topTeeth, outline, style = Stroke(2.4f))
    drawPath(bottomTeeth, fill)
    drawPath(bottomTeeth, outline, style = Stroke(2.4f))

    // 3-prong monster claw paw next to the head (matching drawing Top-Right)
    val clawPath = Path().apply {
        moveTo(clawCenter.x + r * 0.55f, clawCenter.y)
        lineTo(clawCenter.x - r * 0.50f, clawCenter.y - r * 0.36f)
        lineTo(clawCenter.x - r * 0.22f, clawCenter.y - r * 0.08f)
        lineTo(clawCenter.x - r * 0.62f, clawCenter.y + r * 0.04f)
        lineTo(clawCenter.x - r * 0.22f, clawCenter.y + r * 0.18f)
        lineTo(clawCenter.x - r * 0.46f, clawCenter.y + r * 0.42f)
        close()
    }
    drawPath(clawPath, if (isChomping) accentColor else Color(0xFF141418))
    drawPath(clawPath, outline, style = Stroke(2.4f))
}

/**
 * Image 2 Middle-Right: Round Fuse Bomb (قنبلة) with curved spark fuse.
 */
fun DrawScope.drawPixelBomb(
    center: Offset,
    radiusPx: Float,
    fuseProgress: Float,
    glowColor: Color
) {
    // Bomb body
    drawCircle(Color(0xFF111116), radius = radiusPx, center = center)
    drawCircle(if (fuseProgress > 0.72f) Color.Red else Color.White, radius = radiusPx, center = center, style = Stroke(2.5f))
    // Specular shine block
    drawRect(Color.White, Offset(center.x - radiusPx * 0.45f, center.y - radiusPx * 0.45f), Size(radiusPx * 0.28f, radiusPx * 0.28f))
    // Fuse neck + curved cord
    val fuseStart = Offset(center.x + radiusPx * 0.2f, center.y - radiusPx * 0.95f)
    val fuseTip = Offset(center.x + radiusPx * 0.75f, center.y - radiusPx * 1.55f)
    drawLine(Color(0xFFD6C29F), fuseStart, fuseTip, strokeWidth = 3f)
    // Flickering fuse spark
    val sparkRadius = radiusPx * (0.28f + 0.15f * sin(fuseProgress * 28f))
    drawCircle(glowColor, radius = sparkRadius * 1.4f, center = fuseTip)
    drawCircle(Color.Yellow, radius = sparkRadius, center = fuseTip)
}

/**
 * Image 2 Middle-Left: Directional Arrow Telegraph before projectile spawns.
 */
fun DrawScope.drawDirectionalArrow(
    origin: Offset,
    dirX: Float,
    dirY: Float,
    lengthPx: Float,
    color: Color
) {
    val tip = Offset(origin.x + dirX * lengthPx, origin.y + dirY * lengthPx)
    drawLine(color, origin, tip, strokeWidth = 3.5f)
    val angle = atan2(dirY, dirX)
    val headLen = lengthPx * 0.26f
    val leftWing = Offset(
        tip.x - cos(angle - 0.55f) * headLen,
        tip.y - sin(angle - 0.55f) * headLen
    )
    val rightWing = Offset(
        tip.x - cos(angle + 0.55f) * headLen,
        tip.y - sin(angle + 0.55f) * headLen
    )
    val arrowHead = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(leftWing.x, leftWing.y)
        lineTo(rightWing.x, rightWing.y)
        close()
    }
    drawPath(arrowHead, color)
}

/**
 * Overworld Pixel Art NPC Sprite (Human, Monster, Angel with halo + stepped wings, Devil with horns + jagged wings).
 */
fun DrawScope.drawOverworldNpcSprite(
    npc: WorldNpc,
    center: Offset,
    unitPx: Float
) {
    drawOval(
        color = Color.Black.copy(alpha = 0.40f),
        topLeft = Offset(center.x - unitPx * 0.85f, center.y + unitPx * 0.72f),
        size = Size(unitPx * 1.7f, unitPx * 0.55f)
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
            "..YYYYYY..",
            ".OOOOOOOO.",
            ".OSSSSSSO.",
            ".OSBSSBSO.",
            "..OSSSSO..",
            ".OAAAAAAO.",
            "OAAAAAAAAO",
            ".OAAAAAAO.",
            "..OO..OO..",
            "..OO..OO.."
        )
        isDevil -> listOf(
            ".O......O.",
            ".OO....OO.",
            ".OAAAAAAO.",
            ".OAWAWAWO.",
            "..OAAAAO..",
            ".OCCCCCC0.",
            "OCCAAAAACO",
            ".OCCCCCC0.",
            "..OO..OO..",
            "..OO..OO.."
        )
        isMonster -> listOf(
            "..OOOOOO..",
            ".OAAAAAAO.",
            "OAAWAAWAAO",
            "OAAAAAAAAO",
            ".OAOAAOAO.",
            ".OCCCCCC0.",
            "OCCCCCCCCO",
            ".OCCCCCC0.",
            "..OO..OO..",
            "..OO..OO.."
        )
        else -> listOf(
            "..OOOOOO..",
            ".OHHHHHHO.",
            ".OHSSSSHO.",
            ".OSBSSBSO.",
            "..OSSSSO..",
            ".OAAAAAAO.",
            "OSAAAAAASO",
            ".OPPPPPPO.",
            "..OO..OO..",
            "..OO..OO.."
        )
    }
    val palette = mapOf(
        'O' to Color(0xFF0A0A0D),
        '0' to Color(0xFF0A0A0D),
        'Y' to MercyYellow,
        'S' to Color(0xFFEBC7A8),
        'H' to Color(0xFF5A3825),
        'B' to Color(0xFF1F2937),
        'W' to Color.White,
        'A' to npc.accentColor,
        'C' to npc.accentColor.copy(alpha = 0.72f),
        'P' to Color(0xFF334155)
    )
    drawPixelMatrix(grid, palette, center, unitPx * 2.4f)
}

/**
 * Overworld Pixel Art Lost Paradox Kid Sprite.
 */
fun DrawScope.drawOverworldKidSprite(
    kid: LostKid,
    center: Offset,
    unitPx: Float,
    isRescued: Boolean
) {
    drawCircle(
        color = (if (isRescued) Color(0xFF4ADE80) else MercyYellow).copy(alpha = 0.24f),
        radius = unitPx * 1.55f,
        center = center
    )
    val grid = listOf(
        "..OOOO..",
        ".OHHHHO.",
        ".OSSSSO.",
        ".OSBSS0.",
        "..OSSO..",
        ".OAAAAO.",
        ".OAAAAO.",
        "..O..O.."
    )
    val palette = mapOf(
        'O' to Color.White,
        '0' to Color(0xFF0A0A0D),
        'H' to Color(0xFF78350F),
        'S' to Color(0xFFEBC7A8),
        'B' to Color(0xFF1E293B),
        'A' to kid.accentColor
    )
    drawPixelMatrix(grid, palette, center, unitPx * 1.95f)
}

/**
 * Overworld Pixel Art Buildings & Landmarks (Houses/Mansions, Labs, Caves, and Hills).
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

    when (kind) {
        LandmarkKind.MANSION -> {
            // Undertale-style Pitched-Roof House / Mansion
            val roof = Path().apply {
                moveTo(center.x, center.y - half * 0.95f)
                lineTo(center.x + half * 1.05f, center.y - half * 0.15f)
                lineTo(center.x - half * 1.05f, center.y - half * 0.15f)
                close()
            }
            drawPath(roof, accent.copy(alpha = 0.85f))
            drawPath(roof, rim, style = Stroke(2.2f))
            // Walls + windows + door
            drawRect(Color(0xFF27222E), Offset(center.x - half * 0.82f, center.y - half * 0.15f), Size(half * 1.64f, half * 0.98f))
            drawRect(rim, Offset(center.x - half * 0.82f, center.y - half * 0.15f), Size(half * 1.64f, half * 0.98f), style = Stroke(2f))
            drawRect(MercyYellow.copy(alpha = 0.85f), Offset(center.x - half * 0.56f, center.y + half * 0.05f), Size(half * 0.30f, half * 0.30f))
            drawRect(MercyYellow.copy(alpha = 0.85f), Offset(center.x + half * 0.26f, center.y + half * 0.05f), Size(half * 0.30f, half * 0.30f))
            drawRect(Color(0xFF6B4423), Offset(center.x - half * 0.18f, center.y + half * 0.22f), Size(half * 0.36f, half * 0.60f))
        }

        LandmarkKind.LAB -> {
            // Tech Chrono Lab Dome with Antenna & Glowing Visor
            drawLine(rim, Offset(center.x, center.y - half * 1.05f), Offset(center.x, center.y - half * 0.60f), strokeWidth = 2.4f)
            drawCircle(if (isSecret) GlitchCyan else accent, radius = half * 0.16f, center = Offset(center.x, center.y - half * 1.08f))
            drawRect(Color(0xFF1F2937), Offset(center.x - half * 0.92f, center.y - half * 0.60f), Size(half * 1.84f, half * 1.38f))
            drawRect(rim, Offset(center.x - half * 0.92f, center.y - half * 0.60f), Size(half * 1.84f, half * 1.38f), style = Stroke(2.2f))
            drawRect(if (isSecret) GlitchCyan.copy(alpha = 0.85f) else Color(0xFF38BDF8), Offset(center.x - half * 0.62f, center.y - half * 0.30f), Size(half * 1.24f, half * 0.36f))
            drawRect(Color(0xFF0F172A), Offset(center.x - half * 0.22f, center.y + half * 0.24f), Size(half * 0.44f, half * 0.54f))
        }

        LandmarkKind.CAVE -> {
            // Stepped Pixel Cave Mound + Dark Tunnel Entrance
            drawOval(Color(0xFF574434), Offset(center.x - half * 0.98f, center.y - half * 0.65f), Size(half * 1.96f, half * 1.45f))
            drawOval(rim, Offset(center.x - half * 0.98f, center.y - half * 0.65f), Size(half * 1.96f, half * 1.45f), style = Stroke(2.2f))
            drawOval(Color(0xFF09080C), Offset(center.x - half * 0.42f, center.y - half * 0.08f), Size(half * 0.84f, half * 0.86f))
        }

        LandmarkKind.HILLS -> {
            // Twin Stepped Pixel Hills + Shrine Flag
            val hill1 = Path().apply {
                moveTo(center.x - half * 1.05f, center.y + half * 0.72f)
                lineTo(center.x - half * 0.25f, center.y - half * 0.62f)
                lineTo(center.x + half * 0.52f, center.y + half * 0.72f)
                close()
            }
            val hill2 = Path().apply {
                moveTo(center.x - half * 0.15f, center.y + half * 0.72f)
                lineTo(center.x + half * 0.45f, center.y - half * 0.40f)
                lineTo(center.x + half * 1.05f, center.y + half * 0.72f)
                close()
            }
            drawPath(hill1, accent.copy(alpha = 0.72f))
            drawPath(hill1, rim, style = Stroke(2f))
            drawPath(hill2, accent.copy(alpha = 0.52f))
            drawPath(hill2, rim, style = Stroke(1.8f))
            // Little quest flag on peak
            drawLine(Color.White, Offset(center.x - half * 0.25f, center.y - half * 0.62f), Offset(center.x - half * 0.25f, center.y - half * 1.02f), strokeWidth = 2f)
            drawRect(if (completed) Color(0xFF4ADE80) else MercyYellow, Offset(center.x - half * 0.25f, center.y - half * 1.02f), Size(half * 0.42f, half * 0.24f))
        }
    }
}


