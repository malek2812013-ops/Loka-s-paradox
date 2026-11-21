package com.example.ui.components

import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import com.example.model.WorldId
import com.example.model.WorldZone
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

/**
 * Ultra-Fine Undertale-Authentic Micro-Pixel Engine:
 * - Character / Boss / Monster Sprites: 512x512 crisp nearest-neighbor pixel resolution (32x32 / 64x64 integer scale)
 * - Overworld & Ruins Stages: High-density 640x480 micro-pixel resolution (40x30 tiles of 16x16 smaller pixels)
 * - Battle Encounter Backdrop: High-density 960x720 encounter resolution
 */
object UltraPixelArtEngine {

    const val SPRITE_RES = 512
    const val ICON_RES = 128
    const val OVERWORLD_W = 640
    const val OVERWORLD_H = 480
    const val BATTLE_W = 960
    const val BATTLE_H = 720

    private val spriteCache = LruCache<String, ImageBitmap>(32)
    private val sceneCache = LruCache<String, ImageBitmap>(16)

    fun getHdSpriteBitmap(
        rows: List<String>,
        palette: Map<Char, Color>
    ): ImageBitmap {
        if (rows.isEmpty()) {
            return Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888).asImageBitmap()
        }
        val keyBuilder = StringBuilder(96)
        keyBuilder.append(rows.size).append('x').append(rows.maxOf { it.length }).append(':')
        keyBuilder.append(rows.joinToString("/").hashCode()).append(':')
        palette.entries.sortedBy { it.key }.forEach { (ch, col) ->
            keyBuilder.append(ch).append('=').append(col.copy(alpha = 1f).toArgb()).append(',')
        }
        val key = keyBuilder.toString()
        spriteCache.get(key)?.let { return it }

        val w0 = rows.maxOf { it.length }.coerceAtLeast(1)
        val h0 = rows.size.coerceAtLeast(1)
        val pixels = IntArray(w0 * h0)
        for (r in 0 until h0) {
            val row = rows[r]
            for (c in 0 until w0) {
                val ch = if (c < row.length) row[c] else '.'
                if (ch != '.' && ch != ' ') {
                    val col = palette[ch]
                    if (col != null) {
                        pixels[r * w0 + c] = col.copy(alpha = 1f).toArgb()
                    }
                }
            }
        }

        // Character & Monster matrices (>= 18 px) render at full 512x512 crisp pixel art;
        // tiny hearts/stars/sparks render at 128x128 to keep LRU cache fast and lightweight.
        val isCharacterSprite = w0 >= 18 || h0 >= 18
        val baseRes = if (isCharacterSprite) SPRITE_RES else ICON_RES
        val targetW = baseRes
        val targetH = if (h0 > w0) {
            ((baseRes * h0.toFloat() / w0.toFloat()).toInt().coerceIn(baseRes, (baseRes * 1.25f).toInt()))
        } else {
            baseRes
        }

        val hdPixels = IntArray(targetW * targetH)
        for (y in 0 until targetH) {
            val sy = (y * h0) / targetH
            for (x in 0 until targetW) {
                val sx = (x * w0) / targetW
                hdPixels[y * targetW + x] = pixels[sy * w0 + sx]
            }
        }

        val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        bmp.setPixels(hdPixels, 0, targetW, 0, 0, targetW, targetH)
        val img = bmp.asImageBitmap()
        spriteCache.put(key, img)
        return img
    }

    /**
     * Generates a cached 960x720 Classic Undertale Encounter Backdrop with smaller, finer dark-green grid lines.
     */
    fun getUndertaleBattleBgBitmap(): ImageBitmap {
        val key = "undertale_battle_bg_960x720"
        sceneCache.get(key)?.let { return it }

        val w = BATTLE_W
        val h = BATTLE_H
        val pixels = IntArray(w * h)
        val gridLimitY = 356
        val borderLeft = 26
        val borderRight = w - 26
        val borderTop = 14
        val cell = 66

        for (y in 0 until h) {
            for (x in 0 until w) {
                val insidePanel = y in borderTop..gridLimitY && x in borderLeft..borderRight
                val argb = if (insidePanel) {
                    val gx = (x - borderLeft) % cell
                    val gy = (y - borderTop) % cell
                    val onOuter = x <= borderLeft + 1 || x >= borderRight - 1 || y <= borderTop + 1 || y >= gridLimitY - 1
                    val onGridLine = gx == 0 || gy == 0
                    when {
                        onOuter -> 0xFF005500.toInt()
                        onGridLine -> 0xFF003B00.toInt()
                        else -> 0xFF000000.toInt()
                    }
                } else {
                    0xFF000000.toInt()
                }
                pixels[y * w + x] = argb
            }
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        val img = bmp.asImageBitmap()
        sceneCache.put(key, img)
        return img
    }

    /**
     * Generates a cached 640x480 (40x30 fine 16x16 tiles = 307,200 micro-pixels!) terrain backdrop per world and zone.
     */
    fun getWorldTerrainBitmap(world: WorldId, zone: WorldZone): ImageBitmap {
        val key = "terrain_640x480_${world.name}_${zone.name}"
        sceneCache.get(key)?.let { return it }

        val w = OVERWORLD_W
        val h = OVERWORLD_H
        val tile = 16
        val pixels = IntArray(w * h)
        val rng = Random(world.ordinal * 997 + zone.ordinal * 313 + 42)
        val isSanctum = zone == WorldZone.SANCTUM

        for (y in 0 until h) {
            for (x in 0 until w) {
                val tx = x / tile
                val ty = y / tile
                val lx = x % tile
                val ly = y % tile
                val tileCheck = (tx + ty) % 2 == 0
                val isTileEdge = lx == 0 || ly == 0
                val noise = rng.nextInt(100)
                val bayer = ((x and 1) shl 1) or (y and 1)

                val col = when (world) {
                    WorldId.NORMAL_WORLD -> {
                        if (isSanctum) {
                            when {
                                isTileEdge -> 0xFF141B26.toInt()
                                (lx == 8 && ly == 8) -> 0xFF38BDF8.toInt()
                                (lx in 3..12 && ly in 3..12 && (lx == 3 || ly == 3)) -> 0xFF1B2433.toInt()
                                tileCheck -> 0xFF202937.toInt()
                                else -> 0xFF263142.toInt()
                            }
                        } else {
                            when {
                                isTileEdge && (tx + ty) % 3 == 0 -> 0xFF2C5427.toInt()
                                noise < 5 -> 0xFF254D22.toInt()
                                noise < 10 -> 0xFF4F8A47.toInt()
                                tileCheck && bayer == 0 -> 0xFF41753A.toInt()
                                tileCheck -> 0xFF3B6B35.toInt()
                                bayer == 3 -> 0xFF30592C.toInt()
                                else -> 0xFF35602F.toInt()
                            }
                        }
                    }

                    WorldId.TRI_RACE_REALM -> {
                        if (isSanctum) {
                            when {
                                isTileEdge -> 0xFF1E1A26.toInt()
                                (lx == 8 && ly == 8) -> 0xFF22D3EE.toInt()
                                tileCheck -> 0xFF2C2638.toInt()
                                else -> 0xFF352E44.toInt()
                            }
                        } else {
                            val ripple = ((x + y * 2) / 10) % 3 == 0
                            when {
                                noise < 4 -> 0xFF9C7B50.toInt()
                                ripple && bayer < 2 -> 0xFFE3CEA8.toInt()
                                tileCheck -> 0xFFD9C29A.toInt()
                                else -> 0xFFCBB186.toInt()
                            }
                        }
                    }

                    WorldId.SPLIT_REALM -> {
                        val isAngel = when (zone) {
                            WorldZone.WEST, WorldZone.SANCTUM -> true
                            WorldZone.EAST -> false
                            else -> x < w / 2
                        }
                        if (isAngel) {
                            when {
                                isTileEdge -> 0xFF5D2D7A.toInt()
                                noise < 5 -> 0xFFF472B6.toInt()
                                tileCheck -> 0xFF7B409E.toInt()
                                else -> 0xFF9B4683.toInt()
                            }
                        } else {
                            val emberCrack = (x * 3 + y * 5) % 53 == 0 && noise < 30
                            when {
                                emberCrack -> 0xFFEF4444.toInt()
                                noise < 6 -> 0xFF32343B.toInt()
                                tileCheck -> 0xFF5B3F2D.toInt()
                                else -> 0xFF4B4D56.toInt()
                            }
                        }
                    }

                    WorldId.GOVERNMENT_HQ -> {
                        val isCenterCarpet = (x in (w / 2 - 44)..(w / 2 + 44)) && !isSanctum
                        val isCarpetTrim = isCenterCarpet && (x <= w / 2 - 40 || x >= w / 2 + 40)
                        if (isSanctum) {
                            when {
                                isTileEdge -> 0xFF0F172A.toInt()
                                (lx == 8 && ly == 8) -> 0xFF38BDF8.toInt()
                                tileCheck -> 0xFF1E293B.toInt()
                                else -> 0xFF283548.toInt()
                            }
                        } else when {
                            isCarpetTrim -> 0xFFFACC15.toInt()
                            isCenterCarpet -> if ((tx + ty) % 2 == 0) 0xFF7F1D1D.toInt() else 0xFF991B1B.toInt()
                            isTileEdge -> 0xFF111827.toInt()
                            noise < 4 -> 0xFF334155.toInt()
                            tileCheck -> 0xFF1E293B.toInt()
                            else -> 0xFF263346.toInt()
                        }
                    }

                    WorldId.GLITCHED_WORLD -> {
                        when {
                            y % tile == 0 && (tx % 3 == 0) -> 0xFF14353D.toInt()
                            noise < 3 -> 0xFF0E7490.toInt()
                            noise == 98 -> 0xFF86198F.toInt()
                            (x + y) % 24 == 0 -> 0xFF0A0C12.toInt()
                            else -> 0xFF040407.toInt()
                        }
                    }
                }
                pixels[y * w + x] = col
            }
        }

        // Sprinkle 180 micro-pixel wildflowers, pebbles & shards across 640x480
        if (!isSanctum && world != WorldId.GLITCHED_WORLD) {
            repeat(180) {
                val fx = rng.nextInt(16, w - 16)
                val fy = rng.nextInt(16, h - 16)
                when (world) {
                    WorldId.NORMAL_WORLD -> {
                        setPx(pixels, w, h, fx - 1, fy + 1, 0xFF15803D.toInt())
                        setPx(pixels, w, h, fx + 1, fy + 1, 0xFF15803D.toInt())
                        val petal = if (it % 3 == 0) 0xFFFACC15.toInt() else if (it % 3 == 1) 0xFFF9A8D4.toInt() else 0xFF93C5FD.toInt()
                        setPx(pixels, w, h, fx, fy - 1, petal)
                        setPx(pixels, w, h, fx - 1, fy, petal)
                        setPx(pixels, w, h, fx + 1, fy, petal)
                        setPx(pixels, w, h, fx, fy + 1, petal)
                        setPx(pixels, w, h, fx, fy, 0xFFFFFBEB.toInt())
                    }
                    WorldId.TRI_RACE_REALM -> {
                        setPx(pixels, w, h, fx, fy, 0xFF785A3A.toInt())
                        setPx(pixels, w, h, fx + 1, fy, 0xFF9C7B50.toInt())
                        if (it % 4 == 0) {
                            setPx(pixels, w, h, fx, fy - 1, 0xFF22D3EE.toInt())
                            setPx(pixels, w, h, fx, fy - 2, 0xFFCFFAFE.toInt())
                        }
                    }
                    WorldId.SPLIT_REALM -> {
                        val isAngelSpot = zone == WorldZone.WEST || (zone != WorldZone.EAST && fx < w / 2)
                        val speck = if (isAngelSpot) 0xFFFBCFE8.toInt() else 0xFFDC2626.toInt()
                        setPx(pixels, w, h, fx, fy, speck)
                        setPx(pixels, w, h, fx + 1, fy, 0xFF1F161D.toInt())
                    }
                    WorldId.GOVERNMENT_HQ -> {
                        if (it % 5 == 0) {
                            setPx(pixels, w, h, fx, fy, 0xFF38BDF8.toInt())
                            setPx(pixels, w, h, fx + 1, fy, 0xFF0F172A.toInt())
                        }
                    }
                    else -> Unit
                }
            }
        }

        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        val img = bmp.asImageBitmap()
        sceneCache.put(key, img)
        return img
    }

    /**
     * Generates a cached 640x480 Undertale Ruins Spotlight Stage backdrop for meeting Ashen.
     */
    fun getAshenStageBitmap(): ImageBitmap {
        val key = "ashen_stage_640x480"
        sceneCache.get(key)?.let { return it }

        val w = OVERWORLD_W
        val h = OVERWORLD_H
        val pixels = IntArray(w * h)
        val floorY = (h * 0.68f).toInt()
        val beamCx = (w * 0.63f)
        val archCx = (w * 0.72f)
        val archHalfW = (w * 0.085f).toInt()
        val archTopY = (floorY - h * 0.44f).toInt()

        for (y in 0 until h) {
            for (x in 0 until w) {
                var r = 10
                var g = 7
                var b = 18

                if (y < floorY) {
                    val brickH = 24
                    val brickW = 48
                    val rowIdx = y / brickH
                    val shift = if (rowIdx % 2 == 1) brickW / 2 else 0
                    val isMortar = (y % brickH == 0) || ((x + shift) % brickW == 0)
                    if (isMortar) {
                        r = 7; g = 5; b = 13
                    } else if (y % brickH == 1) {
                        r = 26; g = 16; b = 42
                    } else {
                        r = 18; g = 11; b = 32
                    }
                } else {
                    val dist = ((x - beamCx) * (x - beamCx) * 0.25f + (y - floorY) * (y - floorY) * 1.2f)
                    if (dist < 2600f) {
                        r = 28; g = 34; b = 26
                    } else {
                        r = 9; g = 8; b = 14
                    }
                }

                // Ominous Undertale Ruin Archway (scaled for 640x480)
                if (abs(x - archCx) < archHalfW && y in archTopY until floorY) {
                    val dx = abs(x - archCx)
                    val inner = dx < archHalfW - 10 && y > archTopY + 18
                    val archCurve = dx * dx + (y - (archTopY + 30)).coerceAtMost(0) * (y - (archTopY + 30)).coerceAtMost(0) < 560
                    if (inner && (y > archTopY + 30 || archCurve)) {
                        r = 2; g = 2; b = 5
                    } else {
                        r = 42; g = 26; b = 64
                    }
                }

                // 4x4 Bayer-Dithered Diagonal Spotlight Cone
                if (y <= floorY + 32) {
                    val t = (y.toFloat() / floorY.toFloat()).coerceIn(0f, 1.15f)
                    val halfBeam = 24f + t * 96f
                    val distFromCenter = abs(x - beamCx)
                    if (distFromCenter < halfBeam) {
                        val normDist = distFromCenter / halfBeam
                        val intensity = (1f - normDist) * (1.05f - 0.35f * t)
                        val bayerThreshold = (((x and 3) * 4 + (y and 3)) % 16) / 16f
                        if (intensity > bayerThreshold * 0.85f) {
                            val warmth = if (intensity > 0.65f) 1.0f else 0.55f
                            r = min(255, r + (145 * warmth).toInt())
                            g = min(255, g + (112 * warmth).toInt())
                            b = min(255, b + (48 * warmth).toInt())
                        }
                    }
                }

                pixels[y * w + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        // Lush fine-pixel Undertale flower patch around Ashen
        val flowerOffsets = listOf(-72, -52, -32, -16, 16, 32, 52, 72, -44, 40, -62, 60)
        flowerOffsets.forEachIndexed { idx, ox ->
            val fx = beamCx.toInt() + ox
            val fy = floorY + (idx % 4) * 8 - 4
            val petalCol = if (idx % 2 == 0) 0xFFFDE047.toInt() else 0xFFD4D4D8.toInt()
            setPx(pixels, w, h, fx - 1, fy + 2, 0xFF15803D.toInt())
            setPx(pixels, w, h, fx, fy + 2, 0xFF166534.toInt())
            setPx(pixels, w, h, fx + 1, fy + 2, 0xFF15803D.toInt())
            setPx(pixels, w, h, fx, fy - 1, petalCol)
            setPx(pixels, w, h, fx - 1, fy, petalCol)
            setPx(pixels, w, h, fx + 1, fy, petalCol)
            setPx(pixels, w, h, fx, fy + 1, petalCol)
            setPx(pixels, w, h, fx, fy, 0xFF27272A.toInt())
        }

        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        val img = bmp.asImageBitmap()
        sceneCache.put(key, img)
        return img
    }

    /**
     * Generates cached 640x480 (32x18 fine 20x20 tiles) Purple Ruins Chamber bitmaps for all 3 rooms.
     */
    fun getRuinsChamberBitmap(roomIndex: Int = 0): ImageBitmap {
        val key = "ruins_chamber_${roomIndex}_640x480"
        sceneCache.get(key)?.let { return it }

        val w = OVERWORLD_W
        val h = OVERWORLD_H
        val pixels = IntArray(w * h)
        val wallH = 120
        val tile = 20
        val spotCx = (w * 0.25f)
        val spotFloorY = (h * 0.56f)

        for (y in 0 until h) {
            for (x in 0 until w) {
                var r: Int
                var g: Int
                var b: Int
                if (y < wallH) {
                    val brickH = 24
                    val brickW = 48
                    val rowIdx = y / brickH
                    val shift = if (rowIdx % 2 == 1) brickW / 2 else 0
                    val isMortar = (y % brickH == 0) || ((x + shift) % brickW == 0)
                    when {
                        y >= wallH - 6 -> { r = 38; g = 11; b = 48 }
                        isMortar -> { r = 48; g = 14; b = 62 }
                        y % brickH == 1 -> { r = 132; g = 60; b = 158 }
                        else -> {
                            r = if (roomIndex == 0) 80 else if (roomIndex == 1) 88 else 96
                            g = if (roomIndex == 0) 30 else if (roomIndex == 1) 34 else 32
                            b = if (roomIndex == 0) 102 else if (roomIndex == 1) 108 else 118
                        }
                    }
                } else {
                    val tx = x / tile
                    val ty = (y - wallH) / tile
                    val lx = x % tile
                    val ly = (y - wallH) % tile
                    val isBorderTile = ty == 0 || ty == 17 || tx == 0 || tx == 31
                    val seam = lx == 0 || ly == 0
                    val innerSquare = lx in 4..15 && ly in 4..15 && (lx == 4 || lx == 15 || ly == 4 || ly == 15)
                    when {
                        isBorderTile -> { r = 58; g = 22; b = 74 }
                        seam -> { r = 74; g = 33; b = 92 }
                        innerSquare && (tx + ty) % 3 == 0 -> { r = 102; g = 49; b = 122 }
                        (tx + ty) % 2 == 0 -> {
                            r = if (roomIndex == 1) 120 else 128
                            g = if (roomIndex == 1) 66 else 70
                            b = if (roomIndex == 1) 142 else 150
                        }
                        else -> { r = 110; g = 56; b = 132 }
                    }
                }

                // Room 0: Undertale opening golden spotlight beam onto flowerbed
                if (roomIndex == 0 && y <= spotFloorY + 40) {
                    val t = (y.toFloat() / spotFloorY).coerceIn(0f, 1.15f)
                    val halfBeam = 20f + t * 68f
                    val dist = abs(x - spotCx)
                    if (dist < halfBeam) {
                        val intensity = (1f - dist / halfBeam) * (1.0f - 0.25f * t)
                        val bayer = (((x and 3) * 4 + (y and 3)) % 16) / 16f
                        if (intensity > bayer * 0.85f) {
                            val wMix = if (intensity > 0.6f) 1f else 0.55f
                            r = min(255, r + (115 * wMix).toInt())
                            g = min(255, g + (92 * wMix).toInt())
                            b = min(255, b + (36 * wMix).toInt())
                        }
                    }
                }

                pixels[y * w + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        // Room 0: Lush golden Ruins flowerbed directly under spotlight (scaled to 640x480)
        if (roomIndex == 0) {
            val rng = Random(777)
            val cx = spotCx.toInt()
            val cy = spotFloorY.toInt()
            for (dy in -28..28) {
                for (dx in -56..56) {
                    if (dx * dx * 0.22f + dy * dy <= 740f) {
                        val px = cx + dx
                        val py = cy + dy
                        val shade = if ((dx + dy) % 3 == 0) 0xFF15803D.toInt() else 0xFF22C55E.toInt()
                        setPx(pixels, w, h, px, py, shade)
                    }
                }
            }
            repeat(64) {
                val fx = cx + rng.nextInt(-44, 45)
                val fy = cy + rng.nextInt(-22, 24)
                val petal = if (it % 3 == 0) 0xFFFEF08A.toInt() else 0xFFFACC15.toInt()
                setPx(pixels, w, h, fx, fy - 1, petal)
                setPx(pixels, w, h, fx - 1, fy, petal)
                setPx(pixels, w, h, fx + 1, fy, petal)
                setPx(pixels, w, h, fx, fy + 1, petal)
                setPx(pixels, w, h, fx, fy, 0xFFB45309.toInt())
            }
        }

        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        val img = bmp.asImageBitmap()
        sceneCache.put(key, img)
        return img
    }

    private fun setPx(pixels: IntArray, w: Int, h: Int, x: Int, y: Int, color: Int) {
        if (x in 0 until w && y in 0 until h) {
            pixels[y * w + x] = color
        }
    }
}
