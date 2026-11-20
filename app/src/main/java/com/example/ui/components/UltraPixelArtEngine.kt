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
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Ultra-HD Pixel Art Engine (128x128 Sprites · 128x160 Characters · 256x144 Terrains & Stages).
 *
 * Uses Eric's Pixel Expansion (EPX / Scale2x) edge refinement combined with a per-pixel
 * 128-resolution directional rim-lighting, drop-shadow, and Bayer 2x2 micro-dithering shader,
 * cached cleanly into crisp ARGB_8888 ImageBitmaps rendered with FilterQuality.None at 60 FPS.
 */
object UltraPixelArtEngine {

    const val SPRITE_RES = 128
    const val STAGE_W = 256
    const val STAGE_H = 144

    private val spriteCache = LruCache<String, ImageBitmap>(96)
    private val sceneCache = LruCache<String, ImageBitmap>(24)

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
        var pixels = IntArray(w0 * h0)
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

        // Pure 100% Undertale authentic crisp nearest-neighbor pixel upscaling (zero corner rounding!)
        val targetW = SPRITE_RES
        val targetH = if (h0 > w0) ((SPRITE_RES * h0.toFloat() / w0.toFloat()).toInt().coerceIn(SPRITE_RES, 160)) else SPRITE_RES
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
     * Generates a cached 256x144 Undertale/Deltarune crisp pixel void horizon backdrop for BattleScreen.
     */
    fun getDeltaruneBattleBgBitmap(): ImageBitmap {
        val key = "deltarune_battle_bg_256x144"
        sceneCache.get(key)?.let { return it }

        val w = STAGE_W
        val h = STAGE_H
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val isTile = (x % 16 == 0) || (y % 16 == 0)
                val bayer = ((x and 1) shl 1) or (y and 1)
                val argb = when {
                    y < 18 -> if (bayer == 0 && (x + y) % 7 == 0) 0xFF1F0936.toInt() else 0xFF000000.toInt()
                    y in 18..42 -> {
                        val wave = ((x / 8) + y) % 5 == 0
                        when {
                            isTile -> 0xFF280B42.toInt()
                            wave && bayer < 2 -> 0xFF1A072B.toInt()
                            else -> 0xFF07020D.toInt()
                        }
                    }
                    else -> {
                        val gridStepX = x % 20 == 0
                        val gridStepY = y % 14 == 0
                        when {
                            gridStepX || gridStepY -> 0xFF3E1263.toInt()
                            bayer == 0 && (x / 20 + y / 14) % 2 == 0 -> 0xFF0E0418.toInt()
                            else -> 0xFF030106.toInt()
                        }
                    }
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
     * Generates a cached 256x144 (36,864 pixels!) rich pixel-art terrain backdrop per world and zone.
     */
    fun getWorldTerrainBitmap(world: WorldId, zone: WorldZone): ImageBitmap {
        val key = "terrain_${world.name}_${zone.name}"
        sceneCache.get(key)?.let { return it }

        val w = STAGE_W
        val h = STAGE_H
        val pixels = IntArray(w * h)
        val rng = Random(world.ordinal * 997 + zone.ordinal * 313 + 42)
        val isSanctum = zone == WorldZone.SANCTUM

        for (y in 0 until h) {
            for (x in 0 until w) {
                val tileCheck = ((x / 12) + (y / 12)) % 2 == 0
                val noise = rng.nextInt(100)
                val bayer = ((x and 1) shl 1) or (y and 1)

                val col = when (world) {
                    WorldId.NORMAL_WORLD -> {
                        if (isSanctum) {
                            val gridEdge = x % 16 == 0 || y % 16 == 0
                            when {
                                gridEdge -> 0xFF151C26.toInt()
                                (x % 32 == 8 || y % 32 == 8) && (x + y) % 5 == 0 -> 0xFF1F384A.toInt()
                                tileCheck -> 0xFF202937.toInt()
                                else -> 0xFF263142.toInt()
                            }
                        } else {
                            when {
                                noise < 5 -> 0xFF254D22.toInt()
                                noise < 9 -> 0xFF4F8A47.toInt()
                                tileCheck && bayer == 0 -> 0xFF41753A.toInt()
                                tileCheck -> 0xFF3B6B35.toInt()
                                bayer == 3 -> 0xFF30592C.toInt()
                                else -> 0xFF35602F.toInt()
                            }
                        }
                    }

                    WorldId.TRI_RACE_REALM -> {
                        if (isSanctum) {
                            val seam = x % 16 == 0 || y % 16 == 0
                            when {
                                seam -> 0xFF1E1A26.toInt()
                                tileCheck -> 0xFF2C2638.toInt()
                                else -> 0xFF352E44.toInt()
                            }
                        } else {
                            // Wind-swept sand ripple waves across dunes
                            val ripple = ((x + y * 2) / 7) % 3 == 0
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
                            val tileBorder = x % 16 == 0 || y % 16 == 0
                            when {
                                tileBorder -> 0xFF5D2D7A.toInt()
                                noise < 5 -> 0xFFF472B6.toInt()
                                tileCheck -> 0xFF7B409E.toInt()
                                else -> 0xFF9B4683.toInt()
                            }
                        } else {
                            val emberCrack = (x * 3 + y * 5) % 41 == 0 && noise < 28
                            when {
                                emberCrack -> 0xFFEF4444.toInt()
                                noise < 6 -> 0xFF32343B.toInt()
                                tileCheck -> 0xFF5B3F2D.toInt()
                                else -> 0xFF4B4D56.toInt()
                            }
                        }
                    }

                    WorldId.GLITCHED_WORLD -> {
                        when {
                            y % 18 == 0 && (x / 8) % 3 == 0 -> 0xFF14353D.toInt()
                            noise < 3 -> 0xFF0E7490.toInt()
                            noise == 98 -> 0xFF86198F.toInt()
                            (x + y) % 16 == 0 -> 0xFF0A0C12.toInt()
                            else -> 0xFF040407.toInt()
                        }
                    }
                }
                pixels[y * w + x] = col
            }
        }

        // Sprinkle 256x144 pixel grass tufts, flower clusters & crystal shards
        if (!isSanctum && world != WorldId.GLITCHED_WORLD) {
            repeat(54) {
                val fx = rng.nextInt(10, w - 10)
                val fy = rng.nextInt(10, h - 10)
                when (world) {
                    WorldId.NORMAL_WORLD -> {
                        // Tiny 3x3 pixel wildflower + green leaves
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
                        // Desert pebble & shimmering cyan shard
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
     * Generates a cached 256x144 (36,864 pixels!) fanart-inspired Ashen Ruins Spotlight Stage backdrop.
     */
    fun getAshenStageBitmap(): ImageBitmap {
        val key = "ashen_stage_256x144"
        sceneCache.get(key)?.let { return it }

        val w = STAGE_W
        val h = STAGE_H
        val pixels = IntArray(w * h)
        val floorY = (h * 0.69f).toInt()
        val beamCx = (w * 0.63f)
        val archCx = (w * 0.72f)
        val archHalfW = (w * 0.085f).toInt()
        val archTopY = (floorY - h * 0.44f).toInt()

        for (y in 0 until h) {
            for (x in 0 until w) {
                // 1. Deep purple-indigo Ruins brickwork & floor
                var r = 10
                var g = 7
                var b = 18

                if (y < floorY) {
                    val brickH = 12
                    val brickW = 24
                    val rowIdx = y / brickH
                    val shift = if (rowIdx % 2 == 1) brickW / 2 else 0
                    val isMortar = (y % brickH == 0) || ((x + shift) % brickW == 0)
                    if (isMortar) {
                        r = 7; g = 5; b = 13
                    } else if (y % brickH == 1) {
                        r = 22; g = 15; b = 36
                    } else {
                        r = 16; g = 11; b = 28
                    }
                } else {
                    // Dotted moss & dark cobblestone floor
                    val dist = ((x - beamCx) * (x - beamCx) * 0.32f + (y - floorY) * (y - floorY) * 1.4f)
                    if (dist < 420f) {
                        r = 28; g = 34; b = 26
                    } else {
                        r = 9; g = 8; b = 14
                    }
                }

                // 2. Ominous Arched Stone Doorway behind Ashen
                if (abs(x - archCx) < archHalfW && y in archTopY until floorY) {
                    val dx = abs(x - archCx)
                    val inner = dx < archHalfW - 4 && y > archTopY + 7
                    val archCurve = dx * dx + (y - (archTopY + 12)).coerceAtMost(0) * (y - (archTopY + 12)).coerceAtMost(0) < 90
                    if (inner && (y > archTopY + 12 || archCurve)) {
                        r = 3; g = 2; b = 6
                    } else {
                        r = 36; g = 24; b = 56
                    }
                }

                // 3. 4x4 Bayer-Dithered Diagonal Golden Spotlight Cone from top!
                if (y <= floorY + 12) {
                    val t = (y.toFloat() / floorY.toFloat()).coerceIn(0f, 1.15f)
                    val halfBeam = 10f + t * 38f
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

        // Lush 256x144 pixel flowerbed around Ashen's spotlight puddle
        val flowerOffsets = listOf(-28, -21, -14, -7, 6, 13, 20, 27, -18, 16)
        flowerOffsets.forEachIndexed { idx, ox ->
            val fx = beamCx.toInt() + ox
            val fy = floorY + (idx % 4) * 3 - 2
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
     * Generates cached 256x144 (36,864 pixels!) Purple Ruins Chamber bitmaps for all 3 expanded Ruins rooms:
     *  - Room 0: Flowerbed Awakening Chamber with 4x4 Bayer spotlight & dense golden Ruins flowers
     *  - Room 1: Pillar & Switch Corridor with stone mosaic & shimmering Ruins basin
     *  - Room 2: Dummy & Exit Sanctum with ornate purple border & royal masonry
     */
    fun getRuinsChamberBitmap(roomIndex: Int = 0): ImageBitmap {
        val key = "ruins_chamber_${roomIndex}_256x144"
        sceneCache.get(key)?.let { return it }

        val w = STAGE_W
        val h = STAGE_H
        val pixels = IntArray(w * h)
        val wallH = (h * 0.25f).toInt()
        val floorH = h - wallH
        val spotCx = (w * 0.25f)
        val spotFloorY = (h * 0.56f)

        for (y in 0 until h) {
            for (x in 0 until w) {
                var r: Int
                var g: Int
                var b: Int
                if (y < wallH) {
                    val brickH = (wallH / 3).coerceAtLeast(6)
                    val brickW = 28
                    val rowIdx = y / brickH
                    val shift = if (rowIdx % 2 == 1) brickW / 2 else 0
                    val isMortar = (y % brickH == 0) || ((x + shift) % brickW == 0)
                    when {
                        y >= wallH - 3 -> { r = 35; g = 11; b = 46 }
                        isMortar -> { r = 45; g = 14; b = 58 }
                        y % brickH == 1 -> { r = 126; g = 58; b = 152 }
                        else -> {
                            r = if (roomIndex == 0) 76 else if (roomIndex == 1) 84 else 92
                            g = if (roomIndex == 0) 30 else if (roomIndex == 1) 34 else 32
                            b = if (roomIndex == 0) 96 else if (roomIndex == 1) 102 else 112
                        }
                    }
                } else {
                    val tileW = (w / 14).coerceAtLeast(8)
                    val tileH = (floorH / 7).coerceAtLeast(6)
                    val row = ((y - wallH) / tileH).coerceIn(0, 6)
                    val col = (x / tileW).coerceIn(0, 13)
                    val isEdge = row == 0 || row == 6 || col == 0 || col == 13
                    val seam = (x % tileW == 0) || ((y - wallH) % tileH == 0)
                    when {
                        isEdge -> { r = 56; g = 22; b = 72 }
                        seam -> { r = 74; g = 33; b = 92 }
                        (row + col) % 2 == 0 -> {
                            r = if (roomIndex == 1) 118 else 124
                            g = if (roomIndex == 1) 64 else 67
                            b = if (roomIndex == 1) 140 else 145
                        }
                        else -> { r = 108; g = 54; b = 128 }
                    }
                }

                // Room 0: 4x4 Bayer-dithered golden spotlight beam onto Loka's waking flowerbed!
                if (roomIndex == 0 && y <= spotFloorY + 14) {
                    val t = (y.toFloat() / spotFloorY).coerceIn(0f, 1.15f)
                    val halfBeam = 8f + t * 28f
                    val dist = abs(x - spotCx)
                    if (dist < halfBeam) {
                        val intensity = (1f - dist / halfBeam) * (1.0f - 0.25f * t)
                        val bayer = (((x and 3) * 4 + (y and 3)) % 16) / 16f
                        if (intensity > bayer * 0.85f) {
                            val wMix = if (intensity > 0.6f) 1f else 0.55f
                            r = min(255, r + (110 * wMix).toInt())
                            g = min(255, g + (90 * wMix).toInt())
                            b = min(255, b + (36 * wMix).toInt())
                        }
                    }
                }

                pixels[y * w + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        // Room 0: Lush golden & cream flowerbed directly under spotlight
        if (roomIndex == 0) {
            val rng = Random(777)
            val cx = spotCx.toInt()
            val cy = spotFloorY.toInt()
            for (dy in -10..10) {
                for (dx in -22..22) {
                    if (dx * dx * 0.22f + dy * dy <= 105f) {
                        val px = cx + dx
                        val py = cy + dy
                        val shade = if ((dx + dy) % 3 == 0) 0xFF15803D.toInt() else 0xFF22C55E.toInt()
                        setPx(pixels, w, h, px, py, shade)
                    }
                }
            }
            repeat(18) {
                val fx = cx + rng.nextInt(-18, 19)
                val fy = cy + rng.nextInt(-8, 9)
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
