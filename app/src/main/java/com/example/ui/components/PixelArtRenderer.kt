package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaGold
import com.example.ui.theme.SoulAegisAmber
import com.example.ui.theme.SoulInfernalViolet
import com.example.ui.theme.SoulParadoxCrimson
import com.example.ui.theme.SoulSeraphCyan

/**
 * Renders crisp Ultra-HD Pixel Art sprites (128x128 / 128x160 EPX + directional pixel shader)
 * blitted cleanly onto Compose Canvas with FilterQuality.None at 60 FPS.
 */
fun DrawScope.drawPixelMatrix(
    rows: List<String>,
    palette: Map<Char, Color>,
    center: Offset,
    targetSizePx: Float,
    alpha: Float = 1f
) {
    if (rows.isEmpty()) return
    val hdBitmap = UltraPixelArtEngine.getHdSpriteBitmap(rows, palette)
    val maxDim = maxOf(hdBitmap.width, hdBitmap.height).coerceAtLeast(1)
    val drawW = (targetSizePx * (hdBitmap.width.toFloat() / maxDim)).roundToInt().coerceAtLeast(2)
    val drawH = (targetSizePx * (hdBitmap.height.toFloat() / maxDim)).roundToInt().coerceAtLeast(2)
    val left = (center.x - drawW * 0.5f).roundToInt()
    val top = (center.y - drawH * 0.5f).roundToInt()

    drawImage(
        image = hdBitmap,
        dstOffset = IntOffset(left, top),
        dstSize = IntSize(drawW, drawH),
        alpha = alpha.coerceIn(0f, 1f),
        filterQuality = FilterQuality.None
    )
}

/**
 * Hand-drawn Chunky Stepped Pixel-Block Outline Heart (13x12) matching the user's Soul sketches!
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
    val palette = mutableMapOf(
        'X' to fillColor
    )
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

/**
 * Image 1 — The 6 Chests (شكل الصناديق):
 * Closed top row:
 *  - SMALL_DOT: square box with lid line + small round clasp
 *  - SQUARE_STAR: compact square chest with star/cross clasp
 *  - WIDE_PADLOCK: wider box with lid line + padlock clasp
 * Open bottom row:
 *  - Raised lid + shaded horizontal opening band inside!
 */
/**
 * Stepped integer-pixel Undertale ground shadow (3 stepped bands, zero smooth ovals).
 */
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

    // Loka Portrait (28x28 Micro-Pixel): Crisp Frisk-proportioned rounded bob (NO hat), iconic crown tuft,
    // 3-tone chestnut shading, warm shadow directly under bangs, cyan-grey eyes + cheek tint, golden heart necklace
    val lokaPortraitGrid = listOf(
        "...........OO...............",
        "..........OHhO..............",
        ".......OOOOHHHOOOO..........",
        "......OHHhHHHHHHhHO.........",
        ".....OHhHHHHHHHHHHhO........",
        "....OHHHHHhHHHHHhHHHO.......",
        "....OHHHHHHHHHHHHHHHO.......",
        "...OHHhHHHHHHHHHHHHhHO......",
        "...OHHHOdSSSSSSSSdOHHO......",
        "...OHHdSSSSSSSSSSSSdHO......",
        "...OHHSSSWGCHSSGCHWSHO......",
        "...OHHdSSGGCHSSGCHGdHO......",
        "...OOHSSrSSSSSSSSrSSHOO.....",
        "....OOHSSSSssssSSSSHOO......",
        ".....OOdSSSSSSSSSSdOO.......",
        "......OOOTllllllTOO.........",
        "....OOOTTTTtyYYytTTTTOO.....",
        "...OttTTTTTTYYYYTTTTTttO....",
        "...OTTTTTTTTWYYyTTTTTTTO....",
        "...OStTTTTTTtyytTTTTTTtO....",
        "...OStTTTTTTTTTTTTTTTTtO....",
        "....OOtTTTTTTTTTTTTTTtOO....",
        ".....OOOtTTTTTTTTTTtOOO.....",
        "......OPPPPPp..pPPPPPO......",
        "......OPPPPPp..pPPPPPO......",
        "......OPPPPP....PPPPPO......",
        "......OBBBBBO..OBBBBBO......",
        "......OOOOOOO..OOOOOOO......"
    )

    // Loka Overworld (28x32 Micro-Pixel): Expressive full-body Frisk silhouette with shaded hair tuft, bangs shadow,
    // collar, grey shirt stripe, glowing 3D golden heart necklace, creased beige trousers, and two-tone boots
    val lokaOverworldGrid = listOf(
        "...........OO...............",
        "..........OHhO..............",
        ".......OOOOHHHOOOO..........",
        "......OHHhHHHHHHhHO.........",
        ".....OHhHHHHHHHHHHhO........",
        "....OHHHHHhHHHHHhHHHO.......",
        "....OHHHHHHHHHHHHHHHO.......",
        "...OHHhHHHHHHHHHHHHhHO......",
        "...OHHHSSdHHHHHHdSSSHO......",
        "...OHHdSSSSSSSSSSSSdHO......",
        "...OHHSSSWGCHdSGCHWSHO......",
        "...OHHdSSGGCHdSGCHGdHO......",
        "...OOHSSrSSSSSSSSrSSHOO.....",
        "....OOHSSSSssssSSSSHOO......",
        ".....OOdSSSSSSSSSSdOO.......",
        "......OOOTllllllTOO.........",
        "....OOOTTTTTYYYYTTTTTOO.....",
        "...OSttTTTTTWYYyTTTTTttO....",
        "...OStTTTTTTyYYyTTTTTTtO....",
        "...OOSTTTTTTTyyTTTTTTTSOO...",
        ".....OTTTTTTTTTTTTTTTTTO....",
        ".....OOtTTTTTTTTTTTTtOO.....",
        "......OPPPPPPPppPPPPPPO.....",
        "......OPPPPPPp..pPPPPPO.....",
        "......OPPPpPP....PPpPPO.....",
        "......OPPPPPP....PPPPPO.....",
        "......OPPPpPP....PPpPPO.....",
        "......OPPPPPP....PPPPPO.....",
        "......OBBBBBb....bBBBBO.....",
        "......OBBBBBB....BBBBBO.....",
        "......OBBBBBb....bBBBBO.....",
        "......OOOOOOO....OOOOOO....."
    )

    // Deltarune-style Combat Stance Loka facing RIGHT (28x32): swept bob hair, Frisk side silhouette,
    // grey shirt + golden heart necklace, and ready forward blade/hand posture
    val lokaBattleStanceGrid = listOf(
        "..........OO................",
        ".........OHhO...............",
        "......OOOOHHHOOOO...........",
        ".....OHHhHHHHHHhHO..........",
        "....OHhHHHHHHHHHHhHO........",
        "...OHHHHHhHHHHHhHHHHO.......",
        "...OHHHHHHHHHHHHHHHHO.......",
        "...OHHhHHHHHHHHHHHdSHO......",
        "...OHHHHdSSSSSSSWGCdHO......",
        "...OHHHdSSSSSSSSSGGCSO......",
        "....OOHSSSSSSSSrSSSSSO......",
        ".....OOHdSSSSSssssSdO.......",
        "......OOOTllllllTTOO........",
        ".....OOTTTTTYYYYTTTTTOO.....",
        "....OSttTTTTWYYyTTTTSttO....",
        "....OStTTTTTyYYyTTTTSOO.....",
        ".....OOSTTTTTyyTTTTTS.......",
        "......OTTTTTTTTTTTTTO.......",
        "......OOtTTTTTTTTtOO........",
        "......OPPPPPPPppPPPPO.......",
        ".....OPPPPPp..pPPPPPO.......",
        "....OPPPPPp....pPPPPPO......",
        "....OPPPPp......pPPPPO......",
        "....OBBBBb......bBBBBO......",
        "....OBBBBB......BBBBBO......",
        "....OOOOOO......OOOOOO......"
    )

    // Black-and-Grey Ashen Flower (28x28 Micro-Pixel): Sculpted 6-petal monochrome blossom with inner crease shading,
    // faint brow expression, curved stem with veined leaves, and textured soil mound
    val ashenFlowerNormal = listOf(
        ".......OOOOO....OOOOO.......",
        "......OWPPPPO..OPPPPWO......",
        ".....OWPGPPGPWWPGPPGPWO.....",
        ".....OWPGDDDDWWDDDDGPWO.....",
        "..OOOOOPGDDDDDDDDDDGPOOOOO..",
        ".OWPPPPGDDPPPPPPPPDDGPPPPWO.",
        ".OWPGGDDPPWWPPPPWWPPDDGGPWO.",
        ".OWPGDDPPPBPPPPPPBPPPDDGPWO.",
        "..OGGDDPPPBPPPPPPBPPPDDGGO..",
        ".OWPGDDPPPPPGGGGPPPPPPDDGPO.",
        ".OWPGGDDPPBPPPPPPBPPDDGGPWO.",
        ".OWPPPGDDPPBBBBBBPPDDGPPPWO.",
        "..OOOOOPGDDDDDDDDDDGPOOOOO..",
        ".....OWGGGDDDDDDDDGGGWO.....",
        "......OOOGGDDDDDDGGOOO......",
        "..........OOODDDOO..........",
        "...........OODDDDO..........",
        "....OOOO...ODDDDDO...OOOO...",
        "...OGPGGO..ODDDDO...OGPGGO..",
        "...ODGGGDDDDDDDDDDDDGGGDO...",
        "....OODDDDDGDDDDGDDDDDOO....",
        ".....OOODDDDDDDDDDDDOOO.....",
        "......OOOOOOOOOOOOOOOO......",
        "............................",
        "............................",
        "............................",
        "............................",
        "............................"
    )

    val ashenFlowerCreepy = listOf(
        ".......OOOOO....OOOOO.......",
        "......ODPPPPO..OPPPPDO......",
        ".....ODPGDDDDWWDDDDGPDO.....",
        ".....ODDDDDDDDDDDDDDDDO.....",
        "..OOOOODDDDDPPPPDDDDDOOOOO..",
        ".ODDPPGDDPPBBBBBBPPDDGPPDDO.",
        ".ODDPGDPPBBBBBBBBBBPPDGPDDO.",
        ".ODDPGDPPBPPWWWWPPBPPDGPDDO.",
        "..OGPGDPPPPPPPPPPPPPPDGPGO..",
        ".ODDPGDPBBBBBBBBBBBBPPDPDDO.",
        ".ODDPGDDPBPWWWWWWPBPDDGPDDO.",
        ".ODDPGGDDPBBBBBBBBPDDGGPDDO.",
        "..OOOOODDDDDDDDDDDDDDOOOOO..",
        ".....ODGGGDDDDDDDDGGGDO.....",
        "......OOOGGDDDDDDGGOOO......",
        "..........OOODDDOO..........",
        "...........OODDDDO..........",
        "....OOOO...ODDDDDO...OOOO...",
        "...OGGGGO..ODDDDO...OGGGGO..",
        "...ODDDDDDDDDDDDDDDDDDDDO...",
        "....OODDDDDDDDDDDDDDDDOO....",
        ".....OOODDDDDDDDDDDDOOO.....",
        "......OOOOOOOOOOOOOOOO......",
        "............................",
        "............................",
        "............................",
        "............................",
        "............................"
    )

    // 24x24 Micro-Pixel Boss Battle Sprites
    val ruinsDummyGrid = listOf(
        ".......OOOOOOOOOO.......",
        "......OWWWWWWWWWWO......",
        "......OWWWWWWWWWWO......",
        "....OOOWWWWWWWWWWO......",
        "....OCCCCCCCCCCCCO......",
        "...OCCCCECCCCDECCCO.....",
        "...OCCCCECCCCDECCCO.....",
        "...OCCCCCCCCCCCCCCCO....",
        "....OOCCCCMMMMCCCOO.....",
        "......OCCCMMMCCCO.......",
        ".....OOCCCMCCCCCOO......",
        "....OCCCCCMCCCCCCCO.....",
        "....OCCCCEMCCCCCCCO.....",
        "...OCCCCCCMCCCCCCCCO....",
        "...OCCCCCCMCCCCCCCCO....",
        "...OCCCCCCCCCCCCCCCO....",
        "..OCCCCCCCCCCCCCCCCCO...",
        "..OCCCCCCCCCCCCCCCCCO...",
        "...OOOOOOOOOOOOOOOOO....",
        ".........OWWWO..........",
        ".........OWWWO..........",
        "......OOOWWWWOOO........",
        ".....OOOOOOOOOOOO.......",
        "........................"
    )

    val vexBossGrid = listOf(
        ".......OOOOOOOOOO.......",
        "......ODDDDDDDDDDO......",
        ".....ODDDDDDDDDDDDO.....",
        ".....ODAAAAAAAAAAAO.....",
        ".....ODAAAWWWWAAAAO.....",
        ".....ODAAAAAAAAAAAO.....",
        ".....ODDDDDDDDDDDDO.....",
        "......ODDDDDDDDDDO......",
        "...OO..OOOOOOOOOO..OO...",
        "..OAAOODDDDDDDDDDOOAAO..",
        ".OAAAOODDDDWWDDDDOOAAAO.",
        ".OAA..ODDDDDDDDDDO..AAO.",
        "......ODDDDAAADDDO......",
        ".....ODDDDDAAADDDDO.....",
        ".....ODDDDDDDDDDDDO.....",
        ".....ODDDDDAAADDDDO.....",
        ".....ODDDDDDDDDDDDO.....",
        ".....ODDDDDDDDDDDDO.....",
        "......ODDDD..DDDDO......",
        "......ODDDD..DDDDO......",
        "......ODDDD..DDDDO......",
        ".....OODDD....DDDOO.....",
        ".....OOOOO....OOOOO.....",
        "........................"
    )

    val brambleBossGrid = listOf(
        ".OOO................OOO.",
        ".OGGO..............OGGO.",
        "..OGGO..OOOOOOOO..OGGO..",
        "...OGGODDDDDDDDDDOGGO...",
        "..ODDDDDDDDDDDDDDDDDDO..",
        ".ODDDDDDDDDDDDDDDDDDDDO.",
        ".ODDDGGDDDDDDDDDDGGDDDO.",
        ".ODDDDDDDDDDDDDDDDDDDDO.",
        ".ODDDDWWDDDDDDDDWWDDDDO.",
        ".ODDDDWBDDDDDDDDWBDDDDO.",
        ".ODDDDDDDDDDDDDDDDDDDDO.",
        "..ODDDDDDDGGGGDDDDDDDO..",
        "..OODDDDDGGGGGGDDDDDOO..",
        "...ODDDDDGGGGGGDDDDDO...",
        "...ODDDDDDDDDDDDDDDDO...",
        "...ODDDDDDDDDDDDDDDDO...",
        "....ODDDDDDDDDDDDDDO....",
        "....ODDDDD....DDDDDO....",
        "....ODDDD......DDDDO....",
        "....ODDDD......DDDDO....",
        "...OODDD........DDDOO...",
        "...OOOOO........OOOOO...",
        "........................",
        "........................"
    )

    val vesperaBossGrid = listOf(
        "........OOOOOOOO........",
        ".......OYYYYYYYYO.......",
        "......ODDDDDDDDDDO......",
        ".....ODDDDDDDDDDDDO.....",
        ".....ODDDYYDDDYYDDO.....",
        ".....ODDDDDDDDDDDDO.....",
        "......ODDDDDDDDDDO......",
        ".....OODDDDDDDDDDOO.....",
        "....ODDDDDDDDDDDDDDO....",
        "...ODDDDDCCCCCCDDDDDO...",
        "..ODDDDDCCCCCCCCDDDDDO..",
        "..ODDDDCCCCYYCCCCDDDDO..",
        ".ODDDDDDDDDDDDDDDDDDDDO.",
        ".ODDDDDDDDDYYDDDDDDDDDO.",
        "ODDDDDDDDDDDDDDDDDDDDDDO",
        "ODDDDDDDDDDDDDDDDDDDDDDO",
        ".OOOOOOOOOOOOOOOOOOOOOO.",
        "......ODDDD..DDDDO......",
        "......ODDDD..DDDDO......",
        ".....OODDD....DDDOO.....",
        ".....OOOOO....OOOOO.....",
        "........................",
        "........................",
        "........................"
    )

    val orinBossGrid = listOf(
        ".....OOOOOOOOOOOOOO.....",
        "....ODDDDDDDDDDDDDDO....",
        "...ODDDDDDDDDDDDDDDDO...",
        "...ODDDDWWDDDDWWDDDDO...",
        "...ODDDDWBDDDDWBDDDDO...",
        "...ODDDDDDDDDDDDDDDDO...",
        "...ODDDDDDDDDDDDDDDDO...",
        "..OODDDDDDDDDDDDDDDDOO..",
        ".ODDDDDDBBBBBBBBDDDDDDO.",
        ".ODDDDDBBWWWWWWBBDDDDDO.",
        ".ODDDDDBBWBBBBWBBDDDDDO.",
        ".ODDDDDBBWWWWWWBBDDDDDO.",
        ".ODDDDDDBBBBBBBBDDDDDDO.",
        "..OODDDDDDDDDDDDDDDDOO..",
        "...ODDDDDDDDDDDDDDDDO...",
        "...ODDDDDDDDDDDDDDDDO...",
        "...ODDDDDD....DDDDDDO...",
        "....ODDDDD....DDDDDO....",
        "....ODDDDD....DDDDDO....",
        "....ODDDDD....DDDDDO....",
        "...OODDDD......DDDDOO...",
        "...OOOOOO......OOOOOO...",
        "........................",
        "........................"
    )

    val seraphielBossGrid = listOf(
        "......OOOOOOOOOOOO......",
        ".....OYYYYYYYYYYYYO.....",
        "...OO.OOOOOOOOOOOO.OO...",
        "..OCCODDDDDDDDDDDDOCCO..",
        ".OCCCODDDDYYDDYYDDOCCCO.",
        "OCCCCODDDDDDDDDDDDOCCCCO",
        ".OCCC.OODDDDDDDDOO.CCCO.",
        "..OO.ODDDDDDDDDDDDO.OO..",
        ".OOCCODDDDDYYDDDDDOCCOO.",
        "OCCCOODDDDDYYDDDDDOOCCCO",
        ".OO..ODDDDDDDDDDDDO..OO.",
        "....ODDDDDDDDDDDDDDO....",
        "...ODDDDDDDYYDDDDDDDO...",
        "...ODDDDDDDDDDDDDDDDO...",
        "..ODDDDDDDDDDDDDDDDDDO..",
        "..ODDDDDDDDDDDDDDDDDDO..",
        "..OOOOOOOOOOOOOOOOOOOO..",
        "......ODDD....DDDO......",
        "......ODDD....DDDO......",
        ".....OODDD....DDDOO.....",
        ".....OOOOO....OOOOO.....",
        "........................",
        "........................",
        "........................"
    )

    val malakorBossGrid = listOf(
        ".OOO................OOO.",
        ".OVVO..............OVVO.",
        "..OVVO..OOOOOOOO..OVVO..",
        "...OVVODDDDDDDDDDOVVO...",
        "...ODDDDDDDDDDDDDDDDO...",
        "...ODDDVVVDDDDVVVDDDO...",
        "...ODDDDDDDDDDDDDDDDO...",
        "..OODDDDDDDDDDDDDDDDOO..",
        ".ODDDDDDDDDDDDDDDDDDDDO.",
        ".ODDDDDDDVVVVVVDDDDDDDO.",
        ".ODDDDDDVWWWWWWVDDDDDDO.",
        ".ODDDDDDVWVVVVWVDDDDDDO.",
        ".ODDDDDDDVVVVVVDDDDDDDO.",
        "..ODDDDDDDDDDDDDDDDDDO..",
        "..ODDDDDDDDDDDDDDDDDDO..",
        "...ODDDDDD....DDDDDDO...",
        "...ODDDDD..OO..DDDDDO...",
        "....ODDDD..OO..DDDDO....",
        "....OODDD......DDDOO....",
        "....OOOOO......OOOOO....",
        "........................",
        "........................",
        "........................",
        "........................"
    )

    val echoLokaBossGrid = listOf(
        "......OOOOOOOOOOOO......",
        ".....OCCCCCCCCCCCCO.....",
        "....ODDDDDDDDDDDDDDO....",
        "...ODDDDDDDDDDDDDDDDO...",
        "...ODDDWWDDDDDDCCDDDO...",
        "...ODDDWWDDDDDDCCDDDO...",
        "...ODDDDDDDDDDDDDDDDO...",
        "....ODDDDDDDDDDDDDDO....",
        "...OODDDDDDDDDDDDDDOO...",
        "..ODDDDDDDDDDDDDDDDDDO..",
        "..ODDDDDDWWWWWWDDDDDDO..",
        "..ODDDDDWCCCCCCWDDDDDO..",
        "..ODDDDDDWCCCCWDDDDDDO..",
        "..ODDDDDDDWWWWDDDDDDDO..",
        "...ODDDDDDDDDDDDDDDDO...",
        "....ODDDDDDDDDDDDDDO....",
        "....ODDDDD....DDDDDO....",
        "....ODDDDD....DDDDDO....",
        "...OODDDD......DDDDOO...",
        "...OOOOOO......OOOOOO...",
        "........................",
        "........................",
        "........................",
        "........................"
    )

    val futureLokaBossGrid = listOf(
        ".....OOOOOOOOOOOOOO.....",
        "....OHHHHHHHHHHHHHHO....",
        "...OHHHHHHHHHHHHHHHHO...",
        "...OHHSSSSSSSSSSSSSHO...",
        "...OHSSCCSSSSSSCCSSHO...",
        "...OHSSCCSSSSSSCCSSHO...",
        "...OHHHHHHHHHHHHHHHHO...",
        "....OSSSSSSSSSSSSSSO....",
        "..OOODDDDDDDDDDDDDDOOO..",
        ".ODDDDDDDDDDDDDDDDDDDDO.",
        ".ODDDDDDYYYYYYYYDDDDDDO.",
        ".ODDDDDDDYYYYYYDDDDDDDO.",
        ".ODDDDDDDDYYYYDDDDDDDDO.",
        "ODDDDDDDDDDDDDDDDDDDDDDO",
        "ODDDDDDDDDDDDDDDDDDDDDDO",
        "ODDDDDDDDDDDDDDDDDDDDDDO",
        "ODDDDDDDD......DDDDDDDDO",
        "ODDDDDDD........DDDDDDDO",
        ".OOOOOOO........OOOOOOO.",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................"
    )

    // 24x24 High-Detail Overworld & Battle Normal Monsters
    val normalSlimeGrid = listOf(
        ".........OOOOOO.........",
        "........OGGGGGGO........",
        ".......OGGDDDDGGO.......",
        ".........OOGGOO.........",
        "......OOOOOOOOOOOO......",
        "....OOGGGGGGGGGGGGOO....",
        "...OGGGGGWWGGGGGGGGGO...",
        "..OGGGGGGGGGGGGGGGGGGO..",
        "..OGGGGWWGGGGGGWWGGGGO..",
        ".OGGGGGWBGGGGGGWBGGGGGO.",
        ".OGGGGGGGGGGGGGGGGGGGGO.",
        ".OGGGGGGGGGWWGGGGGGGGGO.",
        "OGGGGGGGGGGGGGGGGGGGGGGO",
        "OGGGGGGGGGDDDDGGGGGGGGGO",
        "OGGGGGGGDDDDDDDDGGGGGGGO",
        ".OOOOOOOOOOOOOOOOOOOOOO.",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................"
    )

    val feralHoundGrid = listOf(
        ".OO..................OO.",
        ".OROO..............OORO.",
        "..ORRO...OOOOOO...ORRO..",
        "..ODDDDOODDDDDDOODDDDO..",
        ".ODDDDRRWDDDDDDWRRDDDDO.",
        ".ODDDDDDDDDDDDDDDDDDDDO.",
        "OODDDDDWWWWWWWWWWDDDDDOO",
        "ODDDDDDWDDDDDDDDWDDDDDDO",
        ".OODDDDDDDDDDDDDDDDDDOO.",
        "..ODDDDDDRRRRRRDDDDDDO..",
        "..ODDDDDDDDDDDDDDDDDDO..",
        "..ODDDD..ODDDDO..DDDDO..",
        "..ODDD...ODDDDO...DDDO..",
        "..ODDD...O....O...DDDO..",
        "..OOOO............OOOO..",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................"
    )

    val sandMothGrid = listOf(
        "...OO..............OO...",
        "....OO....OOOO....OO....",
        ".....OO..ODDDDO..OO.....",
        ".OOOOOCCOODWWDOOCCOOOOO.",
        "OCCCCCCCOODDDDOOCCCCCCCO",
        "OCCCYYCCCOODDOOCCCYYCCCO",
        "OCCYWWYCCODDDDOCCYWWYCCO",
        "OCCCCCCCCODDDDOCCCCCCCCO",
        ".OCCCCCCCODDDDOCCCCCCCO.",
        "..OOOOCC.ODDDDO.CCOOOO..",
        "......OO.ODDDDO.OO......",
        "..........ODDO..........",
        "..........OOOO..........",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................"
    )

    val ironStalkerGrid = listOf(
        ".....OOOOOOOOOOOOOO.....",
        "....ODDDDDDDDDDDDDDO....",
        "...ODDDDDRRRRRRDDDDDO...",
        "...ODDDDWWWWWWWWDDDDO...",
        "....ODDDDDDDDDDDDDDO....",
        "..OO.OOOOOOOOOOOOOO.OO..",
        ".ORROODDDDDDDDDDDDOORRO.",
        ".ORROODDDDDYYDDDDDOORRO.",
        ".ORROODDDDYYYYDDDDOORRO.",
        "..OO.ODDDDDDDDDDDDO.OO..",
        ".....ODDDDDDDDDDDDO.....",
        ".....ODDDD....DDDDO.....",
        ".....ODDD......DDDO.....",
        "....OODDD......DDDOO....",
        "....OOOOO......OOOOO....",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................"
    )

    val haloScribeGrid = listOf(
        "......YYYYYYYYYYYY......",
        ".....YOOOOOOOOOOOOY.....",
        ".......OOOOOOOOOO.......",
        "......OPPPPPPPPPPO......",
        ".....OPPPWWPPPWWPPPO....",
        ".....OPPPWBPPPWBPPPO....",
        ".....OPPPPPPPPPPPPPO....",
        "...O..OPPPPPPPPPPO..O...",
        "..OPPOOOOOOOOOOOOOOPPO..",
        ".OPPPPOPPPPPPPPPPOPPPPO.",
        "..OOOO.OPPPPPPPPO.OOOO..",
        ".......OPPPYYPPPO.......",
        "........OPPPPPPO........",
        ".........OPPPPO.........",
        "..........OPPO..........",
        "...........OO...........",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................"
    )

    val ashImpGrid = listOf(
        "..OO................OO..",
        "..OROO....OOOO....OORO..",
        "...ORROOODDDDDDOOORRO...",
        "...ODDDDDDDDDDDDDDDDO...",
        "..ODDDDYYDDDDDDYYDDDDO..",
        "..ODDDDDDDDDDDDDDDDDDO..",
        "...ODDDDWWWWWWWWDDDDO...",
        "..OODDDDDDDDDDDDDDDDOO..",
        ".ORRODDDDDRRRRDDDDDORRO.",
        ".ORRODDDDDDDDDDDDDDORRO.",
        "..OO..ODDDD..DDDDO..OO..",
        "......ODDD....DDDO......",
        ".....OODD......DDOO.....",
        ".....OOOO......OOOO.....",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................",
        "........................"
    )
}

fun DrawScope.drawUndertaleBossPixelSprite(
    enemyId: EnemyId,
    center: Offset,
    sizePx: Float,
    isHitFlash: Boolean,
    isSpareReady: Boolean,
    monochromeBattle: Boolean = false
) {
    val lineCol = when {
        isHitFlash -> Color.White
        isSpareReady -> MercyYellow
        else -> Color.White
    }
    val darkFill = if (isHitFlash) Color.White else Color(0xFF040406)
    val midTone = if (isHitFlash) Color.White else Color(0xFFA1A1AA)
    val dimTone = if (isHitFlash) Color.White else Color(0xFF52525B)

    val (grid, palette) = if (monochromeBattle) {
        // 100% Authentic Undertale Black-and-White Battle Sprite Palette (turns MercyYellow when SPARE-ready)
        val monoWhite = lineCol
        val monoBright = if (isSpareReady && !isHitFlash) MercyYellow else Color.White
        val monoEye = when (enemyId) {
            EnemyId.ECHO_LOKA -> if (isHitFlash) Color.White else GlitchCyan
            EnemyId.FUTURE_LOKA -> if (isHitFlash) Color.White else SoulSeraphCyan
            else -> monoBright
        }
        val monoGold = if (isHitFlash) Color.White else if (isSpareReady) MercyYellow else Color(0xFFD4D4D8)
        when (enemyId) {
            EnemyId.RUINS_DUMMY -> PixelSprites.ruinsDummyGrid to mapOf(
                'O' to lineCol, 'C' to midTone, 'E' to monoBright, 'D' to dimTone,
                'M' to darkFill, '0' to lineCol, 'W' to dimTone
            )
            EnemyId.VEX -> PixelSprites.vexBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'A' to monoGold, 'W' to monoBright
            )
            EnemyId.BRAMBLE -> PixelSprites.brambleBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'G' to midTone, 'B' to darkFill, 'W' to monoBright
            )
            EnemyId.VESPERA -> PixelSprites.vesperaBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'C' to monoBright, 'Y' to monoGold
            )
            EnemyId.ORIN -> PixelSprites.orinBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'B' to midTone, 'W' to monoBright
            )
            EnemyId.SERAPHIEL -> PixelSprites.seraphielBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'C' to monoBright, 'Y' to monoGold
            )
            EnemyId.MALAKOR -> PixelSprites.malakorBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'V' to midTone, 'W' to monoBright
            )
            EnemyId.ECHO_LOKA -> PixelSprites.echoLokaBossGrid to mapOf(
                'O' to (if (isHitFlash) Color.White else GlitchCyan), 'D' to darkFill, 'C' to monoEye, 'W' to monoWhite
            )
            EnemyId.FUTURE_LOKA -> PixelSprites.futureLokaBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'H' to dimTone, 'S' to midTone, 'C' to monoEye, 'Y' to monoGold
            )
            EnemyId.NORMAL_SLIME -> PixelSprites.normalSlimeGrid to mapOf(
                'O' to lineCol, 'G' to midTone, 'D' to dimTone, 'B' to darkFill, 'W' to monoBright
            )
            EnemyId.FERAL_HOUND -> PixelSprites.feralHoundGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'R' to monoBright, 'W' to monoBright
            )
            EnemyId.SAND_MOTH -> PixelSprites.sandMothGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'C' to monoBright, 'Y' to midTone, 'W' to monoBright
            )
            EnemyId.IRON_STALKER -> PixelSprites.ironStalkerGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'R' to monoBright, 'Y' to midTone, 'W' to monoBright
            )
            EnemyId.HALO_SCRIBE -> PixelSprites.haloScribeGrid to mapOf(
                'O' to lineCol, 'Y' to monoGold, 'P' to midTone, 'B' to darkFill, 'W' to monoBright
            )
            EnemyId.ASH_IMP -> PixelSprites.ashImpGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'R' to midTone, 'Y' to monoBright, 'W' to monoBright
            )
        }
    } else {
        val white = if (isSpareReady && !isHitFlash) MercyYellow else Color.White
        when (enemyId) {
            EnemyId.RUINS_DUMMY -> PixelSprites.ruinsDummyGrid to mapOf(
                'O' to lineCol,
                'C' to (if (isHitFlash) Color.White else Color(0xFFD6C29F)),
                'E' to (if (isHitFlash) Color.White else Color(0xFFD8B4FE)),
                'D' to (if (isHitFlash) Color.White else Color(0xFFD8B4FE)),
                'M' to Color(0xFF4A2E1B),
                '0' to lineCol,
                'W' to (if (isHitFlash) Color.White else Color(0xFF785135))
            )
            EnemyId.VEX -> PixelSprites.vexBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'A' to (if (isHitFlash) Color.White else SoulAegisAmber), 'W' to white
            )
            EnemyId.BRAMBLE -> PixelSprites.brambleBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'G' to (if (isHitFlash) Color.White else Color(0xFF4ADE80)), 'B' to Color(0xFF09090B), 'W' to white
            )
            EnemyId.VESPERA -> PixelSprites.vesperaBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'C' to (if (isHitFlash) Color.White else SoulSeraphCyan), 'Y' to (if (isHitFlash) Color.White else MercyYellow)
            )
            EnemyId.ORIN -> PixelSprites.orinBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'B' to (if (isHitFlash) Color.White else Color(0xFF3B82F6)), 'W' to white
            )
            EnemyId.SERAPHIEL -> PixelSprites.seraphielBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'C' to (if (isHitFlash) Color.White else SoulSeraphCyan), 'Y' to (if (isHitFlash) Color.White else SepiaGold)
            )
            EnemyId.MALAKOR -> PixelSprites.malakorBossGrid to mapOf(
                'O' to lineCol, 'D' to darkFill, 'V' to (if (isHitFlash) Color.White else SoulInfernalViolet), 'W' to white
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
                'O' to lineCol, 'G' to (if (isHitFlash) Color.White else Color(0xFF4ADE80)), 'D' to (if (isHitFlash) Color.White else Color(0xFF166534)), 'B' to Color(0xFF0A0A0D), 'W' to white
            )
            EnemyId.FERAL_HOUND -> PixelSprites.feralHoundGrid to mapOf(
                'O' to lineCol, 'D' to (if (isHitFlash) Color.White else Color(0xFF1F1D26)), 'R' to (if (isHitFlash) Color.White else Color(0xFFEF4444)), 'W' to white
            )
            EnemyId.SAND_MOTH -> PixelSprites.sandMothGrid to mapOf(
                'O' to lineCol, 'D' to (if (isHitFlash) Color.White else Color(0xFF422C1D)), 'C' to (if (isHitFlash) Color.White else SoulSeraphCyan), 'Y' to (if (isHitFlash) Color.White else MercyYellow), 'W' to white
            )
            EnemyId.IRON_STALKER -> PixelSprites.ironStalkerGrid to mapOf(
                'O' to lineCol, 'D' to (if (isHitFlash) Color.White else Color(0xFF334155)), 'R' to (if (isHitFlash) Color.White else Color(0xFFF97316)), 'Y' to (if (isHitFlash) Color.White else MercyYellow), 'W' to white
            )
            EnemyId.HALO_SCRIBE -> PixelSprites.haloScribeGrid to mapOf(
                'O' to lineCol, 'Y' to (if (isHitFlash) Color.White else MercyYellow), 'P' to (if (isHitFlash) Color.White else Color(0xFFF472B6)), 'B' to Color(0xFF0A0A0D), 'W' to white
            )
            EnemyId.ASH_IMP -> PixelSprites.ashImpGrid to mapOf(
                'O' to lineCol, 'D' to (if (isHitFlash) Color.White else Color(0xFF2B1B24)), 'R' to (if (isHitFlash) Color.White else SoulParadoxCrimson), 'Y' to (if (isHitFlash) Color.White else Color(0xFFF59E0B)), 'W' to white
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
                monochromeBattle -> Color(0xFF16161C)
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
                monochromeBattle -> Color(0xFF16161C)
                else -> SoulInfernalViolet.copy(alpha = 0.28f)
            },
            outlineColor = lineCol
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
    // Stepped pixel diagonal brace
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
 * Overworld Pixel Art NPC Sprite (Human, Monster, Angel with halo + stepped wings, Devil with horns + jagged wings).
 */
fun DrawScope.drawOverworldNpcSprite(
    npc: WorldNpc,
    center: Offset,
    unitPx: Float
) {
    drawSteppedPixelShadow(
        center = Offset(center.x, center.y + unitPx * 1.02f),
        widthPx = unitPx * 1.9f,
        heightPx = unitPx * 0.55f
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
            "....YYYYYYYYYYYY....",
            "...OOOWWWWWWWWO0O...",
            "..OSSSSSSSSSSSSSSO..",
            "..OSWWBSSSSSSBWWSSO.",
            "..OSSBBSSSSSSBBSSSO.",
            "...OOSSSSSSSSSSSOO..",
            "..OOWWAAAAAAAAWWOO..",
            ".OWWAAAAAAAAAAAAWWO.",
            ".OWAAAAYYYYYYAAAAWO.",
            "..OAAAAAAAAAAAAAAO..",
            "...OCCCCCCCCCCCCO...",
            "....OCCOO..OOCCO....",
            "....OCCO....OCCO....",
            "....OOOO....OOOO...."
        )
        isDevil -> listOf(
            ".OO..............OO.",
            ".OAO............OAO.",
            "..OAO..OOOOOO..OAO..",
            "..OOODDDDDDDDDDOOO..",
            "..ODDDWWDDDDWWDDDO..",
            "..ODDDDDDDDDDDDDDO..",
            "...OODDDDDDDDDDOO...",
            "..OCCCCCCCCCCCCCCOO.",
            ".OCCAAAAWWWWAAAACCO.",
            ".OCCAAAAAAAAAAAACCO.",
            "..OCCCCCCCCCCCCCCOO.",
            "....ODDDD..DDDDO....",
            "....ODDD....DDDO....",
            "....OOOO....OOOO...."
        )
        isMonster -> listOf(
            "....OOOOOOOOOOOO....",
            "..OOWWAAAAAAAAWWOO..",
            ".OAAAAWWAAAAWWAAAAO.",
            ".OAAAAWBAAAAWBAAAAO.",
            ".OAAAAAAAAAAAAAAAAO.",
            "..OAAOOAAAAAAOOAAO..",
            "..OCCCCCCCCCCCCCCOO.",
            ".OCCAAAAWWWWAAAACCO.",
            "..OCCCCCCCCCCCCCCOO.",
            "....ODDDD..DDDDO....",
            "....ODDD....DDDO....",
            "....OOOO....OOOO...."
        )
        else -> listOf(
            "....OOOOOOOOOOOO....",
            "..OOHHHHHHHHHHHHOO..",
            ".OHHHHHHHHHHHHHHHHO.",
            ".OHHSSSSSSSSSSSSSHO.",
            ".OHSSWWBSSSSBWWSSHO.",
            "..OSSSSSSSSSSSSSSO..",
            "..OOOWAAAAAAAAWOOO..",
            ".OSSAAAAAAAAAAAAASSO",
            "..OOOPPPPPPPPPPOOO..",
            "....OPPPP..PPPPO....",
            "....OBBB....BBBO....",
            "....OOOO....OOOO...."
        )
    }
    val palette = mapOf(
        'O' to Color(0xFF0A0A0D),
        '0' to Color(0xFF0A0A0D),
        'D' to Color(0xFF1F1622),
        'Y' to MercyYellow,
        'S' to Color(0xFFEBC7A8),
        'H' to Color(0xFF5A3825),
        'B' to Color(0xFF1F2937),
        'W' to Color.White,
        'A' to npc.accentColor,
        'C' to npc.accentColor.copy(alpha = 0.72f),
        'P' to Color(0xFF334155)
    )
    drawPixelMatrix(grid, palette, center, unitPx * 2.75f)
}

/**
 * Overworld High-Detail Micro-Pixel Lost Paradox Kid Sprite with pulsing beacon aura.
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
        "....OOOOOOOO....",
        "...OHHHhHhHHHO..",
        "..OHHhHHHHHhHHO.",
        "..OHSSSSSSSSSHO.",
        "..OHSSBBSSBBSSO.",
        "...OSSSSSSSSSSO.",
        "..OOWAAAAAAAAWOO",
        "..OSAAAWWWWAAASO",
        "...OAAAAAAAAAO..",
        "....OPPPPPPPPO..",
        "....OPPP..PPPO..",
        "....OBBB..BBBO..",
        "....OOOO..OOOO.."
    )
    val palette = mapOf(
        'O' to Color(0xFF120C08),
        'H' to Color(0xFF78350F),
        'h' to Color(0xFFB45309),
        'S' to Color(0xFFEBC7A8),
        'B' to Color(0xFF1E293B),
        'W' to Color.White,
        'A' to kid.accentColor,
        'P' to Color(0xFF334155)
    )
    drawPixelMatrix(grid, palette, center, unitPx * 2.45f)
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
            // Stepped pixel roof layers (Undertale Ruin-house silhouette)
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
            // Pure stepped Undertale pixel cave mound
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
            // Stepped pixel mountain silhouette
            drawRect(accent.copy(alpha = 0.78f), Offset(center.x - half * 1.02f, center.y + half * 0.34f), Size(half * 2.04f, half * 0.40f))
            drawRect(accent.copy(alpha = 0.88f), Offset(center.x - half * 0.74f, center.y - half * 0.04f), Size(half * 1.48f, half * 0.38f))
            drawRect(accent, Offset(center.x - half * 0.42f, center.y - half * 0.40f), Size(half * 0.84f, half * 0.36f))
            drawRect(Color.White, Offset(center.x - half * 0.22f, center.y - half * 0.58f), Size(half * 0.44f, half * 0.18f))
            drawRect(rim, Offset(center.x - half * 1.02f, center.y + half * 0.34f), Size(half * 2.04f, half * 0.40f), style = Stroke(1.8f))
            // Flag
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
        targetSizePx = sizePx * 0.86f
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
    // Crisp stepped pixel progress bar waiting for Ashen's return
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
    ambientClock: Float
) {
    val accent = when (world) {
        WorldId.NORMAL_WORLD -> MercyYellow
        WorldId.TRI_RACE_REALM -> SoulSeraphCyan
        WorldId.SPLIT_REALM -> if (door.doorX < 0.5f) Color(0xFFEC4899) else Color(0xFFEF4444)
        WorldId.GLITCHED_WORLD -> GlitchCyan
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
    drawRect(accent.copy(alpha = pulse), Offset(center.x - gw * 0.5f, center.y - gh * 0.5f), Size(gw, gh), style = Stroke(2.2f))
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

    // Stepped pixel spotlight floor pool
    val poolW = bottomWidthPx * 1.24f
    val poolH = poolW * 0.30f
    drawSteppedPixelShadow(Offset(beamCenterX, floorCenterY), poolW, poolH, outerColor)
    drawSteppedPixelShadow(Offset(beamCenterX, floorCenterY), poolW * 0.74f, poolH * 0.70f, midColor)

    for (i in 0..5) {
        val sx = beamCenterX + sin(i * 2.1f + ambientClock * 0.7f) * bottomWidthPx * 0.48f
        val sy = beamH * (0.18f + (i * 0.13f)) + cos(ambientClock * 1.6f + i) * 5f
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
    // Stepped pixel doorway arch
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

    // Stepped pixel mossy base
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

    // Pixel golden flowers at moss corners
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



