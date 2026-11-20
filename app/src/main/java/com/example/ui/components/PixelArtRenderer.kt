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
    val rim = Color(0xFFE5DFD5)
    val ironTrim = Color(0xFF94A3B8)
    val shade = Color(0xFF14111A)
    val gold = if (isOpen) accentColor.copy(alpha = 0.72f) else accentColor

    val widthFactor = if (style == ChestStyle.WIDE_PADLOCK) 1.38f else if (style == ChestStyle.SMALL_DOT) 1.06f else 0.96f
    val w = sizePx * widthFactor
    val h = sizePx * 0.94f
    val left = center.x - w * 0.5f
    val top = center.y - h * 0.5f

    // Ground drop shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.36f),
        topLeft = Offset(left - 2f, top + h * 0.84f),
        size = Size(w + 4f, h * 0.24f)
    )

    if (isOpen) {
        // Raised top lid with planks
        drawRect(woodLight, Offset(left, top - h * 0.14f), Size(w, h * 0.34f))
        drawRect(woodMid, Offset(left, top + h * 0.06f), Size(w, h * 0.14f))
        drawRect(rim, Offset(left, top - h * 0.14f), Size(w, h * 0.34f), style = Stroke(2f))
        // Shaded dark interior band + golden treasure glint
        drawRect(shade, Offset(left + 2f, top + h * 0.20f), Size(w - 4f, h * 0.28f))
        for (i in 0..4) {
            val sx = left + (w * (i + 0.5f) / 5f)
            drawLine(gold.copy(alpha = 0.75f), Offset(sx - 3f, top + h * 0.44f), Offset(sx + 3f, top + h * 0.24f), strokeWidth = 2f)
        }
        // Lower chest base + corner brackets
        drawRect(woodDark, Offset(left, top + h * 0.48f), Size(w, h * 0.52f))
        drawRect(woodShadow, Offset(left, top + h * 0.84f), Size(w, h * 0.16f))
        drawRect(ironTrim, Offset(left, top + h * 0.48f), Size(w * 0.14f, h * 0.52f))
        drawRect(ironTrim, Offset(left + w * 0.86f, top + h * 0.48f), Size(w * 0.14f, h * 0.52f))
        drawRect(rim, Offset(left, top + h * 0.48f), Size(w, h * 0.52f), style = Stroke(2f))
    } else {
        // Closed chest body & wood plank shading
        drawRect(woodDark, Offset(left, top), Size(w, h))
        drawRect(woodLight, Offset(left, top), Size(w, h * 0.34f))
        drawRect(woodShadow, Offset(left, top + h * 0.82f), Size(w, h * 0.18f))
        // Vertical iron bands & corner rivets
        drawRect(ironTrim.copy(alpha = 0.85f), Offset(left + w * 0.10f, top), Size(w * 0.11f, h))
        drawRect(ironTrim.copy(alpha = 0.85f), Offset(left + w * 0.79f, top), Size(w * 0.11f, h))
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
            'B' to Color(0xFF09090B),
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
        EnemyId.NORMAL_SLIME -> PixelSprites.normalSlimeGrid to mapOf(
            'O' to outline,
            'G' to (if (isHitFlash) Color.White else Color(0xFF4ADE80)),
            'D' to (if (isHitFlash) Color.White else Color(0xFF166534)),
            'B' to Color(0xFF0A0A0D),
            'W' to white
        )
        EnemyId.FERAL_HOUND -> PixelSprites.feralHoundGrid to mapOf(
            'O' to outline,
            'D' to (if (isHitFlash) Color.White else Color(0xFF1F1D26)),
            'R' to (if (isHitFlash) Color.White else Color(0xFFEF4444)),
            'W' to white
        )
        EnemyId.SAND_MOTH -> PixelSprites.sandMothGrid to mapOf(
            'O' to outline,
            'D' to (if (isHitFlash) Color.White else Color(0xFF422C1D)),
            'C' to (if (isHitFlash) Color.White else SoulSeraphCyan),
            'Y' to (if (isHitFlash) Color.White else MercyYellow),
            'W' to white
        )
        EnemyId.IRON_STALKER -> PixelSprites.ironStalkerGrid to mapOf(
            'O' to outline,
            'D' to (if (isHitFlash) Color.White else Color(0xFF334155)),
            'R' to (if (isHitFlash) Color.White else Color(0xFFF97316)),
            'Y' to (if (isHitFlash) Color.White else MercyYellow),
            'W' to white
        )
        EnemyId.HALO_SCRIBE -> PixelSprites.haloScribeGrid to mapOf(
            'O' to outline,
            'Y' to (if (isHitFlash) Color.White else MercyYellow),
            'P' to (if (isHitFlash) Color.White else Color(0xFFF472B6)),
            'B' to Color(0xFF0A0A0D),
            'W' to white
        )
        EnemyId.ASH_IMP -> PixelSprites.ashImpGrid to mapOf(
            'O' to outline,
            'D' to (if (isHitFlash) Color.White else Color(0xFF2B1B24)),
            'R' to (if (isHitFlash) Color.White else SoulParadoxCrimson),
            'Y' to (if (isHitFlash) Color.White else Color(0xFFF59E0B)),
            'W' to white
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
    // Left Shaded Grain Sack ("كيس") with tied knot & shadow
    val sackCenter = Offset(dummyCenter.x - unitPx * 3.1f, dummyCenter.y + unitPx * 0.55f)
    drawOval(
        color = Color.Black.copy(alpha = 0.35f),
        topLeft = Offset(sackCenter.x - unitPx * 1.18f, sackCenter.y + unitPx * 0.42f),
        size = Size(unitPx * 2.36f, unitPx * 0.65f)
    )
    drawOval(
        color = Color(0xFFB59A73),
        topLeft = Offset(sackCenter.x - unitPx * 1.1f, sackCenter.y - unitPx * 0.85f),
        size = Size(unitPx * 2.2f, unitPx * 1.75f)
    )
    drawOval(
        color = Color(0xFFD6BE96),
        topLeft = Offset(sackCenter.x - unitPx * 0.72f, sackCenter.y - unitPx * 0.58f),
        size = Size(unitPx * 1.0f, unitPx * 0.72f)
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
    drawRect(
        color = Color(0xFFD6C29F),
        topLeft = Offset(sackCenter.x - unitPx * 0.42f, sackCenter.y - unitPx * 0.85f),
        size = Size(unitPx * 0.84f, unitPx * 0.14f)
    )

    // Right Wood-Framed Crate ("صندوق") with diagonal brace & highlights
    val crateLeft = dummyCenter.x + unitPx * 2.1f
    val crateTop = dummyCenter.y - unitPx * 0.30f
    val crateSize = unitPx * 1.85f
    drawOval(
        color = Color.Black.copy(alpha = 0.32f),
        topLeft = Offset(crateLeft - 2f, crateTop + crateSize * 0.84f),
        size = Size(crateSize + 4f, unitPx * 0.48f)
    )
    drawRect(Color(0xFF785135), Offset(crateLeft, crateTop), Size(crateSize, crateSize))
    drawRect(Color(0xFF9C6B43), Offset(crateLeft, crateTop), Size(crateSize, crateSize * 0.24f))
    drawLine(Color(0xFF5A3921), Offset(crateLeft, crateTop), Offset(crateLeft + crateSize, crateTop + crateSize), strokeWidth = 3f)
    drawRect(Color(0xFFE9D5FF), Offset(crateLeft, crateTop), Size(crateSize, crateSize), style = Stroke(2f))
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
        color = Color.Black.copy(alpha = 0.44f),
        topLeft = Offset(center.x - unitPx * 0.95f, center.y + unitPx * 0.74f),
        size = Size(unitPx * 1.9f, unitPx * 0.58f)
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
    drawCircle(
        color = auraColor.copy(alpha = 0.18f),
        radius = unitPx * 1.85f,
        center = center
    )
    drawOval(
        color = Color.Black.copy(alpha = 0.42f),
        topLeft = Offset(center.x - unitPx * 0.80f, center.y + unitPx * 0.72f),
        size = Size(unitPx * 1.6f, unitPx * 0.48f)
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
 * Overworld Pixel Art Buildings & Landmarks (Houses/Mansions, Labs, Caves, and Hills) with rich brickwork & depth.
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

    // Grounded drop shadow under every landmark
    drawOval(
        color = Color.Black.copy(alpha = 0.44f),
        topLeft = Offset(center.x - half * 1.12f, center.y + half * 0.56f),
        size = Size(half * 2.24f, half * 0.50f)
    )

    when (kind) {
        LandmarkKind.MANSION -> {
            // Chimney + Brick detail
            drawRect(Color(0xFF4B3832), Offset(center.x + half * 0.42f, center.y - half * 0.96f), Size(half * 0.24f, half * 0.52f))
            drawRect(rim, Offset(center.x + half * 0.42f, center.y - half * 0.96f), Size(half * 0.24f, half * 0.52f), style = Stroke(1.6f))
            // Pitched Roof + shingle stripes
            val roof = Path().apply {
                moveTo(center.x, center.y - half * 1.02f)
                lineTo(center.x + half * 1.14f, center.y - half * 0.14f)
                lineTo(center.x - half * 1.14f, center.y - half * 0.14f)
                close()
            }
            drawPath(roof, accent.copy(alpha = 0.90f))
            drawPath(roof, rim, style = Stroke(2.2f))
            drawLine(Color.Black.copy(alpha = 0.25f), Offset(center.x - half * 0.55f, center.y - half * 0.56f), Offset(center.x + half * 0.55f, center.y - half * 0.56f), strokeWidth = 2f)
            // Walls + masonry courses + arched double windows + front steps
            drawRect(Color(0xFF2B2433), Offset(center.x - half * 0.86f, center.y - half * 0.15f), Size(half * 1.72f, half * 1.00f))
            drawRect(Color(0xFF1D1824), Offset(center.x - half * 0.86f, center.y + half * 0.66f), Size(half * 1.72f, half * 0.20f))
            drawRect(rim, Offset(center.x - half * 0.86f, center.y - half * 0.15f), Size(half * 1.72f, half * 1.00f), style = Stroke(2f))
            for (wx in listOf(-0.62f, 0.28f)) {
                drawRect(MercyYellow.copy(alpha = 0.92f), Offset(center.x + half * wx, center.y + half * 0.04f), Size(half * 0.34f, half * 0.34f))
                drawLine(Color(0xFF27222E), Offset(center.x + half * (wx + 0.17f), center.y + half * 0.04f), Offset(center.x + half * (wx + 0.17f), center.y + half * 0.38f), strokeWidth = 1.5f)
                drawLine(Color(0xFF27222E), Offset(center.x + half * wx, center.y + half * 0.21f), Offset(center.x + half * (wx + 0.34f), center.y + half * 0.21f), strokeWidth = 1.5f)
            }
            drawRect(Color(0xFF6B4423), Offset(center.x - half * 0.19f, center.y + half * 0.22f), Size(half * 0.38f, half * 0.63f))
            drawCircle(MercyYellow, radius = 2f, center = Offset(center.x + half * 0.10f, center.y + half * 0.52f))
            drawRect(Color(0xFF9CA3AF), Offset(center.x - half * 0.28f, center.y + half * 0.84f), Size(half * 0.56f, half * 0.12f))
        }

        LandmarkKind.LAB -> {
            // Tech Chrono Lab Dome with Radar Antenna & Glowing Secret Visor
            drawLine(rim, Offset(center.x, center.y - half * 1.12f), Offset(center.x, center.y - half * 0.62f), strokeWidth = 2.4f)
            drawCircle((if (isSecret) GlitchCyan else accent).copy(alpha = 0.30f), radius = half * 0.34f, center = Offset(center.x, center.y - half * 1.12f))
            drawCircle(if (isSecret) GlitchCyan else accent, radius = half * 0.15f, center = Offset(center.x, center.y - half * 1.12f))
            drawRect(Color(0xFF1F2937), Offset(center.x - half * 0.96f, center.y - half * 0.62f), Size(half * 1.92f, half * 1.42f))
            drawRect(Color(0xFF334155), Offset(center.x - half * 0.96f, center.y - half * 0.62f), Size(half * 1.92f, half * 0.24f))
            drawRect(rim, Offset(center.x - half * 0.96f, center.y - half * 0.62f), Size(half * 1.92f, half * 1.42f), style = Stroke(2.2f))
            val visorColor = if (isSecret) GlitchCyan else Color(0xFF38BDF8)
            drawRect(visorColor.copy(alpha = 0.88f), Offset(center.x - half * 0.66f, center.y - half * 0.30f), Size(half * 1.32f, half * 0.38f))
            drawLine(Color.White.copy(alpha = 0.72f), Offset(center.x - half * 0.54f, center.y - half * 0.19f), Offset(center.x + half * 0.12f, center.y - half * 0.19f), strokeWidth = 2f)
            drawRect(Color(0xFF0F172A), Offset(center.x - half * 0.24f, center.y + half * 0.22f), Size(half * 0.48f, half * 0.58f))
            drawLine(visorColor.copy(alpha = 0.65f), Offset(center.x, center.y + half * 0.22f), Offset(center.x, center.y + half * 0.80f), strokeWidth = 1.6f)
        }

        LandmarkKind.CAVE -> {
            // Layered Pixel Stone Cave Mound + Stalactites & Glowing Crystal
            drawOval(Color(0xFF473729), Offset(center.x - half * 1.02f, center.y - half * 0.68f), Size(half * 2.04f, half * 1.48f))
            drawOval(Color(0xFF6B533E), Offset(center.x - half * 0.78f, center.y - half * 0.58f), Size(half * 1.36f, half * 0.56f))
            drawOval(rim, Offset(center.x - half * 1.02f, center.y - half * 0.68f), Size(half * 2.04f, half * 1.48f), style = Stroke(2.2f))
            drawOval(Color(0xFF1F1612), Offset(center.x - half * 0.52f, center.y - half * 0.16f), Size(half * 1.04f, half * 0.94f))
            drawOval(Color(0xFF060509), Offset(center.x - half * 0.40f, center.y - half * 0.04f), Size(half * 0.80f, half * 0.82f))
            // Glowing crystal cluster beside entrance
            drawRect(accent.copy(alpha = 0.9f), Offset(center.x + half * 0.56f, center.y + half * 0.28f), Size(half * 0.16f, half * 0.34f))
            drawRect(Color.White.copy(alpha = 0.75f), Offset(center.x + half * 0.58f, center.y + half * 0.30f), Size(half * 0.06f, half * 0.16f))
        }

        LandmarkKind.HILLS -> {
            val hill1 = Path().apply {
                moveTo(center.x - half * 1.08f, center.y + half * 0.72f)
                lineTo(center.x - half * 0.26f, center.y - half * 0.66f)
                lineTo(center.x + half * 0.54f, center.y + half * 0.72f)
                close()
            }
            val hill2 = Path().apply {
                moveTo(center.x - half * 0.15f, center.y + half * 0.72f)
                lineTo(center.x + half * 0.48f, center.y - half * 0.42f)
                lineTo(center.x + half * 1.08f, center.y + half * 0.72f)
                close()
            }
            drawPath(hill1, accent.copy(alpha = 0.82f))
            drawPath(hill1, rim, style = Stroke(2f))
            drawPath(hill2, accent.copy(alpha = 0.58f))
            drawPath(hill2, rim, style = Stroke(1.8f))
            // Flag on peak
            drawLine(Color.White, Offset(center.x - half * 0.26f, center.y - half * 0.66f), Offset(center.x - half * 0.26f, center.y - half * 1.08f), strokeWidth = 2f)
            drawRect(if (completed) Color(0xFF4ADE80) else MercyYellow, Offset(center.x - half * 0.26f, center.y - half * 1.08f), Size(half * 0.48f, half * 0.28f))
        }
    }
}

/**
 * Overworld Ashen Save Point: monochrome Ashen flower on a soil patch with a golden Save aura & mini star!
 */
fun DrawScope.drawOverworldAshenSaveSprite(
    center: Offset,
    sizePx: Float,
    ambientClock: Float
) {
    val pulse = 1f + 0.08f * sin(ambientClock * 3.8f)
    drawCircle(
        color = MercyYellow.copy(alpha = 0.22f),
        radius = sizePx * 0.76f * pulse,
        center = center
    )
    drawCircle(
        color = MercyYellow.copy(alpha = 0.60f),
        radius = sizePx * 0.60f * pulse,
        center = center,
        style = Stroke(2f)
    )
    drawOval(
        color = Color(0xFF1F1D24),
        topLeft = Offset(center.x - sizePx * 0.44f, center.y + sizePx * 0.16f),
        size = Size(sizePx * 0.88f, sizePx * 0.28f)
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
 * Overworld Underground Burrow Mound displayed after Ashen saves & hides for ~1m 15s!
 */
fun DrawScope.drawOverworldAshenBurrow(
    center: Offset,
    sizePx: Float,
    secondsRemaining: Float,
    ambientClock: Float
) {
    val pulse = 0.5f + 0.5f * sin(ambientClock * 3f)
    // Disturbed dark soil crater + pebbles
    drawOval(
        color = Color(0xFF151218),
        topLeft = Offset(center.x - sizePx * 0.48f, center.y + sizePx * 0.05f),
        size = Size(sizePx * 0.96f, sizePx * 0.42f)
    )
    drawOval(
        color = Color(0xFF3F3F46),
        topLeft = Offset(center.x - sizePx * 0.42f, center.y + sizePx * 0.09f),
        size = Size(sizePx * 0.84f, sizePx * 0.34f),
        style = Stroke(2f)
    )
    drawOval(
        color = Color(0xFF060508),
        topLeft = Offset(center.x - sizePx * 0.22f, center.y + sizePx * 0.16f),
        size = Size(sizePx * 0.44f, sizePx * 0.18f)
    )
    // Faint monochrome dust specks & progress ring waiting for Ashen's return
    val progress = (1f - (secondsRemaining / 75f)).coerceIn(0f, 1f)
    drawArc(
        color = Color(0xFFD4D4D8).copy(alpha = 0.55f + 0.25f * pulse),
        startAngle = -90f,
        sweepAngle = 360f * progress,
        useCenter = false,
        topLeft = Offset(center.x - sizePx * 0.55f, center.y - sizePx * 0.05f),
        size = Size(sizePx * 1.10f, sizePx * 0.62f),
        style = Stroke(2.2f)
    )
}

/**
 * Overworld Pixel Stone Doorways for all 5 doors per world (4 directional border gates + Ornate Door V Portal).
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
    val pulse = 0.72f + 0.28f * sin(ambientClock * 4f)

    if (door.edge == null) {
        // Door V: Ornate Sanctum Archway inside Central Plaza
        drawCircle(
            color = GlitchCyan.copy(alpha = 0.24f * pulse),
            radius = u * 4.4f,
            center = center
        )
        // Carved dual pillars + arch lintel
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

    // Stone archway threshold
    drawRect(
        color = Color(0xFF1A1B22),
        topLeft = Offset(center.x - gw * 0.5f, center.y - gh * 0.5f),
        size = Size(gw, gh)
    )
    drawRect(
        color = Color(0xFF050508),
        topLeft = Offset(center.x - gw * 0.36f, center.y - gh * 0.36f),
        size = Size(gw * 0.72f, gh * 0.72f)
    )
    drawRect(
        color = accent.copy(alpha = 0.85f * pulse),
        topLeft = Offset(center.x - gw * 0.5f, center.y - gh * 0.5f),
        size = Size(gw, gh),
        style = Stroke(2.2f)
    )
    // Glowing directional arrow inside threshold
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
 * Overworld Roaming Monster Sprite with alert badge (!) when actively chasing Loka.
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
    drawCircle(
        color = ringColor.copy(alpha = if (isChasing) 0.34f else 0.18f),
        radius = sizePx * 0.62f,
        center = center
    )
    drawOval(
        color = Color.Black.copy(alpha = 0.42f),
        topLeft = Offset(center.x - sizePx * 0.42f, center.y + sizePx * 0.28f),
        size = Size(sizePx * 0.84f, sizePx * 0.24f)
    )
    drawUndertaleBossPixelSprite(
        enemyId = enemyId,
        center = center,
        sizePx = sizePx,
        isHitFlash = false,
        isSpareReady = isAlreadyFriend
    )
    if (isChasing && !isAlreadyFriend) {
        val bounce = sin(ambientClock * 12f) * 2.5f
        val bx = center.x
        val by = center.y - sizePx * 0.68f + bounce
        drawRect(Color.Black, Offset(bx - 4f, by - 11f), Size(8f, 15f))
        drawRect(Color(0xFFEF4444), Offset(bx - 2.5f, by - 9.5f), Size(5f, 8f))
        drawRect(Color(0xFFEF4444), Offset(bx - 2.5f, by + 0.5f), Size(5f, 3.5f))
    }
}

/**
 * Pixel-Art Dithered Spotlight Cone + Twinkle Stars inspired by classic Undertale/UTY Ruins spotlight scenes!
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

    // Dithered circular floor spotlight pool
    val poolRx = bottomWidthPx * 0.64f
    val poolRy = poolRx * 0.34f
    drawOval(
        color = outerColor,
        topLeft = Offset(beamCenterX - poolRx, floorCenterY - poolRy),
        size = Size(poolRx * 2f, poolRy * 2f)
    )
    drawOval(
        color = midColor,
        topLeft = Offset(beamCenterX - poolRx * 0.74f, floorCenterY - poolRy * 0.72f),
        size = Size(poolRx * 1.48f, poolRy * 1.44f)
    )

    // Twinkling Undertale pixel sparkle stars around the beam
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
 * Ominous dark Ruins Stone Archway silhouette in the backdrop (behind Ashen's flowerbed).
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
    // Arch frame pillars
    drawRect(wallTint, Offset(left, top), Size(widthPx, heightPx))
    drawRect(trimTint, Offset(left, top), Size(widthPx, heightPx), style = Stroke(2.5f))
    // Inner dark doorway void + arch keystone
    val inset = widthPx * 0.22f
    drawRect(Color(0xFF040307), Offset(left + inset, top + heightPx * 0.24f), Size(widthPx - inset * 2f, heightPx * 0.76f))
    drawOval(Color(0xFF040307), Offset(left + inset, top + heightPx * 0.12f), Size(widthPx - inset * 2f, heightPx * 0.28f))
    // Carved top sigil crescent + 3 small dots
    drawRect(trimTint, Offset(centerX - widthPx * 0.14f, top + heightPx * 0.06f), Size(widthPx * 0.28f, heightPx * 0.06f))
    drawCircle(trimTint, radius = 2.5f, center = Offset(centerX - widthPx * 0.12f, top + heightPx * 0.17f))
    drawCircle(trimTint, radius = 3.2f, center = Offset(centerX, top + heightPx * 0.17f))
    drawCircle(trimTint, radius = 2.5f, center = Offset(centerX + widthPx * 0.12f, top + heightPx * 0.17f))
}

/**
 * Shaded Pixel-Art Brick & Moss Pedestal Monument (inspired by warm masonry + golden flower + floating SAVE heart).
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

    // Mossy grass halo at pedestal base
    drawOval(mossColor, Offset(left - w * 0.18f, top + h * 0.52f), Size(w * 1.36f, h * 0.62f))
    drawOval(Color(0xFF284A24), Offset(left - w * 0.18f, top + h * 0.52f), Size(w * 1.36f, h * 0.62f), style = Stroke(2f))

    // Brick pedestal body + mortar seams + top bevel
    drawRect(brickBase, Offset(left, top), Size(w, h))
    drawRect(brickHighlight, Offset(left, top), Size(w, h * 0.22f))
    drawRect(brickShadow, Offset(left, top + h * 0.80f), Size(w, h * 0.20f))
    drawLine(Color(0xFF331B12), Offset(left, top + h * 0.45f), Offset(left + w, top + h * 0.45f), strokeWidth = 1.8f)
    drawLine(Color(0xFF331B12), Offset(left + w * 0.32f, top), Offset(left + w * 0.32f, top + h * 0.45f), strokeWidth = 1.8f)
    drawLine(Color(0xFF331B12), Offset(left + w * 0.68f, top), Offset(left + w * 0.68f, top + h * 0.45f), strokeWidth = 1.8f)
    drawRect(Color(0xFF24110B), Offset(left, top), Size(w, h), style = Stroke(2.2f))

    // Engraved front stone plaque
    drawRect(Color(0xFF7D4830), Offset(left + w * 0.20f, top + h * 0.52f), Size(w * 0.60f, h * 0.36f))
    drawRect(Color(0xFF24110B), Offset(left + w * 0.20f, top + h * 0.52f), Size(w * 0.60f, h * 0.36f), style = Stroke(1.6f))
    drawLine(Color(0xFF472517), Offset(left + w * 0.28f, top + h * 0.66f), Offset(left + w * 0.72f, top + h * 0.66f), strokeWidth = 1.6f)

    // Twin golden flowers flanking the mossy corners
    listOf(left - w * 0.08f to top + h * 0.82f, left + w * 0.94f to top + h * 0.86f).forEach { (fx, fy) ->
        drawCircle(Color(0xFF140E0A), radius = 4.5f, center = Offset(fx, fy))
        drawCircle(emblemColor, radius = 3.2f, center = Offset(fx, fy))
        drawCircle(Color.White, radius = 1.2f, center = Offset(fx, fy))
    }

    // Hovering glowing pixel star/heart above monument
    val floatY = top - h * 0.28f + sin(ambientClock * 3.8f) * 3f
    drawPixelSaveStar(
        center = Offset(center.x, floatY),
        sizePx = w * 0.16f,
        color = emblemColor
    )
}



