package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.RetroSoundEngine
import com.example.model.AppLanguage
import com.example.model.LoreSlide
import com.example.model.tr
import com.example.ui.components.drawLokaOverworldSprite
import com.example.ui.components.drawPixelSaveStar
import com.example.ui.components.drawWingPair
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.GlitchMagenta
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaAmber
import com.example.ui.theme.SpaceMonoFamily
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LoreIntroScreen(
    language: AppLanguage,
    slides: List<LoreSlide>,
    sepiaIntensity: Float,
    glitchIntensity: Float,
    isMuted: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleMute: () -> Unit,
    onFinishIntro: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var visibleChars by remember { mutableIntStateOf(0) }
    var glitchTick by remember { mutableIntStateOf(0) }
    var cyanFlashAlpha by remember { mutableFloatStateOf(0f) }

    BackHandler(enabled = currentIndex > 0) {
        currentIndex--
    }

    val slide = slides.getOrElse(currentIndex) { slides.last() }
    val fullText = if (language == AppLanguage.AR) slide.narrationAr else slide.narration
    val chapterTitle = if (language == AppLanguage.AR) slide.chapterTagAr else slide.chapterTag

    val amberInk = Color(0xFFF5B951)
    val crimsonInk = Color(0xFFDC4C4C)
    val mutedAmber = Color(0xFF8C7355)

    val advanceStory: () -> Unit = {
        if (visibleChars < fullText.length) {
            visibleChars = fullText.length
        } else if (currentIndex < slides.lastIndex) {
            currentIndex++
        } else {
            onFinishIntro()
        }
    }

    // Typewriter with video-style temporal cyan flash (Slide 2: Loka's Paradox) & CRT corruption (Glitch finale)
    LaunchedEffect(currentIndex, fullText) {
        visibleChars = 0
        val isParadoxSlide = slide.id == 2
        cyanFlashAlpha = if (isParadoxSlide) 0.85f else 0f

        if (isParadoxSlide) {
            RetroSoundEngine.playGlitchNoise(100)
        } else if (slide.isGlitchFinale) {
            RetroSoundEngine.playGlitchNoise(140)
        }

        val charDelay = if (slide.isGlitchFinale) 24L else 28L
        for (i in 1..fullText.length) {
            if (visibleChars >= fullText.length) break
            delay(charDelay)
            if (visibleChars >= fullText.length) break
            visibleChars = i
            if (cyanFlashAlpha > 0f) {
                cyanFlashAlpha = (cyanFlashAlpha - 0.10f).coerceAtLeast(0f)
            }
            if (i % 3 == 1 && fullText[i - 1] != ' ') {
                if (slide.isGlitchFinale && Random.nextFloat() < 0.45f) {
                    RetroSoundEngine.playGlitchNoise(42)
                } else {
                    RetroSoundEngine.playTypeBlip(if (slide.isGlitchFinale) 225f else if (isParadoxSlide) 295f else 345f)
                }
            }
        }
        cyanFlashAlpha = 0f
        if (slide.isGlitchFinale) {
            RetroSoundEngine.playGlitchNoise(180)
        }
    }

    LaunchedEffect(slide.isGlitchFinale, slide.id) {
        if (slide.isGlitchFinale || slide.id == 2) {
            while (true) {
                delay(70L)
                glitchTick++
            }
        }
    }

    val kenBurnsTransition = rememberInfiniteTransition(label = "kenBurns")
    val zoom by kenBurnsTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse),
        label = "zoom"
    )
    val scenePhase by kenBurnsTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "scenePhase"
    )
    val cursorAlpha by kenBurnsTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse),
        label = "cursorAlpha"
    )

    val sepiaMatrix = remember(sepiaIntensity) {
        ColorMatrix().apply {
            setToSaturation(1f - sepiaIntensity * 0.52f)
        }
    }

    val bgCenterGlow by animateColorAsState(
        targetValue = when {
            cyanFlashAlpha > 0.08f -> Color(0xFF0E2D38)
            slide.isGlitchFinale -> Color(0xFF1F0709)
            else -> Color(0xFF170F07)
        },
        animationSpec = tween(220),
        label = "bgGlow"
    )

    // Live character corruption during the glitched finale (matching video CRT scramble aesthetic)
    val displayedText = remember(fullText, visibleChars, glitchTick, slide.isGlitchFinale) {
        val raw = fullText.take(visibleChars)
        if (slide.isGlitchFinale && visibleChars > 10) {
            val glitchPool = charArrayOf('%', '&', '#', '8', '!', '?')
            val r = Random(glitchTick * 313 + visibleChars)
            buildString {
                raw.forEach { ch ->
                    if (ch != ' ' && ch != '—' && r.nextFloat() < 0.09f * glitchIntensity.coerceAtLeast(0.7f)) {
                        append(glitchPool[r.nextInt(glitchPool.size)])
                    } else {
                        append(ch)
                    }
                }
            }
        } else if (slide.id == 2 && visibleChars in 1..3 && language == AppLanguage.EN) {
            raw.replace('a', '€')
        } else {
            raw
        }
    }

    val textColor = when {
        cyanFlashAlpha > 0.15f -> Color(0xFF7DD3FC)
        slide.isGlitchFinale && glitchTick % 4 == 0 -> crimsonInk
        else -> amberInk
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(bgCenterGlow, Color(0xFF050404))
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = advanceStory
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .testTag("intro_tap_area")
    ) {
        // Subtle whole-screen temporal flash & CRT scanlines overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (cyanFlashAlpha > 0.01f) {
                drawRect(
                    color = Color(0xFF0E2D38).copy(alpha = cyanFlashAlpha * 0.65f),
                    topLeft = Offset.Zero,
                    size = size
                )
            }
            if (slide.isGlitchFinale) {
                val scanStep = 4.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = crimsonInk.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += scanStep
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 880.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Bar: Chapter Tag + Subtle Tap Hint + AR/EN, Mute, SKIP (48dp touch targets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = chapterTitle,
                        style = TextStyle(fontFamily = SpaceMonoFamily, fontSize = 12.sp, letterSpacing = 1.sp),
                        color = if (slide.isGlitchFinale) crimsonInk else amberInk
                    )
                    Text(
                        text = tr(language, "TAP ANYWHERE TO CONTINUE", "اضغط في أي مكان للمتابعة"),
                        style = TextStyle(fontFamily = SpaceMonoFamily, fontSize = 10.sp, letterSpacing = 1.5.sp),
                        color = mutedAmber.copy(alpha = 0.78f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(0.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, mutedAmber.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("intro_language_button")
                    ) {
                        Text(language.buttonLabel, style = MaterialTheme.typography.labelSmall, color = amberInk)
                    }
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier.testTag("intro_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Toggle sound",
                            tint = mutedAmber
                        )
                    }
                    OutlinedButton(
                        onClick = onFinishIntro,
                        shape = RoundedCornerShape(0.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, mutedAmber.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("intro_skip_button")
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = null, tint = amberInk, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(tr(language, "SKIP", "تخطي"), style = MaterialTheme.typography.labelSmall, color = amberInk)
                    }
                }
            }

            // Centerpiece: Classic Sepia Pixel-Art Story Illustrations + Live Overlays OR Video-Styled CRT Glitch Cutoff
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF100B06))
                    .border(
                        width = 2.5.dp,
                        color = when {
                            slide.isGlitchFinale && glitchTick % 3 == 0 -> crimsonInk
                            slide.isGlitchFinale -> GlitchCyan
                            cyanFlashAlpha > 0.1f -> GlitchCyan
                            else -> amberInk.copy(alpha = 0.85f)
                        },
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                if (!slide.isGlitchFinale && slide.imageRes != null) {
                    Image(
                        painter = painterResource(id = slide.imageRes),
                        contentDescription = chapterTitle,
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(sepiaMatrix),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                            }
                    )
                    // Soft warm vignette framing the illustration cleanly
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        SepiaAmber.copy(alpha = 0.06f * sepiaIntensity),
                                        Color(0xFF160B04).copy(alpha = 0.48f)
                                    )
                                )
                            )
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val scanStep = 5.dp.toPx()
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = Color.Black.copy(alpha = 0.10f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f
                            )
                            y += scanStep
                        }
                        val bob = sin(scenePhase * 6.283f)
                        when (slide.id) {
                            1 -> {
                                val angle = scenePhase * 6.283f
                                val colors = listOf(Color(0xFFF59E0B), Color(0xFF4ADE80), Color(0xFF38BDF8), Color(0xFFEF4444))
                                colors.forEachIndexed { idx, col ->
                                    val a = angle + idx * 1.57f
                                    val ex = size.width * 0.5f + cos(a) * size.width * 0.34f
                                    val ey = size.height * 0.22f + sin(a) * size.height * 0.08f
                                    drawPixelSaveStar(Offset(ex, ey), 7.dp.toPx(), col, Color.White)
                                }
                                drawWingPair(
                                    center = Offset(size.width * 0.16f, size.height * 0.80f + bob * 4f),
                                    spanPx = 28.dp.toPx(),
                                    isAngelWings = true,
                                    fillColor = Color(0xFF38BDF8).copy(alpha = 0.35f),
                                    outlineColor = ParchmentInk.copy(alpha = 0.7f)
                                )
                                drawWingPair(
                                    center = Offset(size.width * 0.84f, size.height * 0.80f - bob * 4f),
                                    spanPx = 28.dp.toPx(),
                                    isAngelWings = false,
                                    fillColor = Color(0xFFEF4444).copy(alpha = 0.35f),
                                    outlineColor = ParchmentInk.copy(alpha = 0.7f)
                                )
                            }
                            2 -> {
                                val pulse = (bob + 1f) * 0.5f
                                drawCircle(
                                    color = amberInk.copy(alpha = 0.22f * pulse),
                                    radius = size.minDimension * 0.30f,
                                    center = Offset(size.width / 2f, size.height / 2f)
                                )
                                val walkX = size.width * (0.20f + 0.60f * scenePhase)
                                val walkY = size.height * 0.80f + abs(bob) * -4.dp.toPx()
                                drawLokaOverworldSprite(
                                    center = Offset(walkX, walkY),
                                    unit = 7.dp.toPx(),
                                    isGlitched = cyanFlashAlpha > 0.2f
                                )
                            }
                            3 -> {
                                val crackColor = if (bob > 0f) GlitchCyan.copy(alpha = 0.55f) else MercyYellow.copy(alpha = 0.55f)
                                drawLine(
                                    color = crackColor,
                                    start = Offset(size.width * 0.15f, size.height * 0.12f),
                                    end = Offset(size.width * 0.48f, size.height * 0.45f),
                                    strokeWidth = 3f
                                )
                                drawLine(
                                    color = crackColor,
                                    start = Offset(size.width * 0.48f, size.height * 0.45f),
                                    end = Offset(size.width * 0.84f, size.height * 0.18f),
                                    strokeWidth = 3f
                                )
                            }
                            4 -> {
                                val kidColors = listOf(Color(0xFFF472B6), Color(0xFF38BDF8), MercyYellow, Color(0xFF4ADE80))
                                kidColors.forEachIndexed { idx, col ->
                                    val kx = size.width * (0.18f + idx * 0.21f)
                                    val ky = size.height * 0.78f + sin(scenePhase * 6.283f + idx * 1.4f) * 10.dp.toPx()
                                    drawPixelSaveStar(Offset(kx, ky), 8.dp.toPx(), col, Color.White)
                                }
                            }
                        }
                    }
                } else {
                    GlitchCutoffCanvas(
                        language = language,
                        glitchTick = glitchTick,
                        intensity = glitchIntensity,
                        crimsonInk = crimsonInk,
                        amberInk = amberInk,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Video-Style Centered Space Mono Amber Typewriter Narration + Glowing Solid Block Cursor █
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val annotated = buildAnnotatedString {
                    withStyle(SpanStyle(color = textColor)) {
                        append(displayedText)
                    }
                    val cursorVis = if (visibleChars < fullText.length) 1f else cursorAlpha
                    withStyle(SpanStyle(color = textColor.copy(alpha = cursorVis))) {
                        append(" █")
                    }
                }
                Text(
                    text = annotated,
                    style = TextStyle(
                        fontFamily = SpaceMonoFamily,
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                        letterSpacing = 0.3.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Action Bar + Bottom Progress Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bottom Progress Dot Indicator matching video
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        slides.forEachIndexed { index, item ->
                            val dotColor = when {
                                index == currentIndex && item.isGlitchFinale -> crimsonInk
                                index == currentIndex -> amberInk
                                index < currentIndex -> amberInk.copy(alpha = 0.42f)
                                else -> Color(0xFF3A3024)
                            }
                            Box(
                                modifier = Modifier
                                    .size(if (index == currentIndex) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentIndex == slides.lastIndex) {
                            OutlinedButton(
                                onClick = { currentIndex = 0 },
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.dp, amberInk.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("intro_replay_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = amberInk, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(tr(language, "Replay", "إعادة"), style = TextStyle(fontFamily = SpaceMonoFamily, fontSize = 12.sp), color = amberInk)
                            }
                        }
                        Button(
                            onClick = advanceStory,
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (slide.isGlitchFinale) crimsonInk else amberInk,
                                contentColor = ObsidianBg
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("intro_next_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (currentIndex == slides.lastIndex && visibleChars >= fullText.length) {
                                    tr(language, "WAKE UP", "ابدأ الرحلة")
                                } else {
                                    tr(language, "NEXT", "التالي")
                                },
                                style = TextStyle(fontFamily = SpaceMonoFamily, fontSize = 13.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlitchCutoffCanvas(
    language: AppLanguage,
    glitchTick: Int,
    intensity: Float,
    crimsonInk: Color,
    amberInk: Color,
    modifier: Modifier = Modifier
) {
    val rand = remember(glitchTick) { Random(glitchTick * 7919) }
    Box(modifier = modifier.background(Color(0xFF0A0506))) {
        Image(
            painter = painterResource(id = R.drawable.img_pixel_more_kids),
            contentDescription = "Corrupted memory feed",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.25f
                    translationX = (rand.nextFloat() - 0.5f) * 26f * intensity
                    translationY = (rand.nextFloat() - 0.5f) * 14f * intensity
                }
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            // CRT red & amber horizontal distortion slices matching the video's glitch effect
            val sliceCount = (10 * intensity).toInt().coerceAtLeast(5)
            for (i in 0 until sliceCount) {
                val y = rand.nextFloat() * size.height
                val h = (4f + rand.nextFloat() * 18f)
                val color = when (i % 3) {
                    0 -> crimsonInk.copy(alpha = 0.34f)
                    1 -> amberInk.copy(alpha = 0.24f)
                    else -> GlitchMagenta.copy(alpha = 0.24f)
                }
                drawRect(
                    color = color,
                    topLeft = Offset(rand.nextFloat() * size.width * 0.2f, y),
                    size = Size(size.width * (0.45f + rand.nextFloat() * 0.55f), h)
                )
            }
            val scanStep = 4.dp.toPx()
            var sy = 0f
            while (sy < size.height) {
                drawLine(
                    color = crimsonInk.copy(alpha = 0.16f),
                    start = Offset(0f, sy),
                    end = Offset(size.width, sy),
                    strokeWidth = 1.2f
                )
                sy += scanStep
            }
            repeat(48) {
                drawRect(
                    color = Color.White.copy(alpha = rand.nextFloat() * 0.45f),
                    topLeft = Offset(rand.nextFloat() * size.width, rand.nextFloat() * size.height),
                    size = Size(4f, 3f)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (glitchTick % 4 == 0) {
                    tr(language, "( ARCHIVE INTERRUPTED )", "( انقطع الأرشيف )")
                } else {
                    tr(language, "( DATA EXPUNGED )", "( بيانات محذوفة )")
                },
                style = TextStyle(fontFamily = SpaceMonoFamily, fontSize = 20.sp, letterSpacing = 2.sp),
                color = if (glitchTick % 2 == 0) crimsonInk else amberInk,
                textAlign = TextAlign.Center
            )
            Text(
                text = tr(language, "LOKA'S TIMELINE RECORD WAS ERASED", "بقية سجل مفارقة لوكا تم إخفاؤه"),
                style = TextStyle(fontFamily = SpaceMonoFamily, fontSize = 12.sp, letterSpacing = 1.sp),
                color = ParchmentInk.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}
