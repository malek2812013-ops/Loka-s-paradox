package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.RetroSoundEngine
import com.example.model.AppLanguage
import com.example.model.LoreSlide
import com.example.model.tr
import com.example.ui.components.drawLokaOverworldSprite
import com.example.ui.components.drawPixelSaveStar
import com.example.ui.components.drawWingPair
import com.example.ui.theme.EspressoSurface
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.GlitchMagenta
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaAmber
import com.example.ui.theme.SepiaBronze
import com.example.ui.theme.SepiaDarkBorder
import com.example.ui.theme.SepiaGold
import kotlinx.coroutines.delay
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

    val slide = slides.getOrElse(currentIndex) { slides.last() }
    val fullText = if (language == AppLanguage.AR) slide.narrationAr else slide.narration
    val chapterTitle = if (language == AppLanguage.AR) slide.chapterTagAr else slide.chapterTag

    val advanceStory: () -> Unit = {
        if (visibleChars < fullText.length) {
            visibleChars = fullText.length
        } else if (currentIndex < slides.lastIndex) {
            currentIndex++
        } else {
            onFinishIntro()
        }
    }

    LaunchedEffect(currentIndex, fullText) {
        visibleChars = 0
        val charDelay = if (slide.isGlitchFinale) 24L else 32L
        for (i in 1..fullText.length) {
            if (visibleChars >= fullText.length) break
            delay(charDelay)
            if (visibleChars >= fullText.length) break
            visibleChars = i
            if (i % 3 == 1 && fullText[i - 1] != ' ') {
                if (slide.isGlitchFinale && Random.nextFloat() < 0.45f) {
                    RetroSoundEngine.playGlitchNoise(45)
                } else {
                    RetroSoundEngine.playTypeBlip(if (slide.isGlitchFinale) 220f else 350f)
                }
            }
        }
        if (slide.isGlitchFinale) {
            RetroSoundEngine.playGlitchNoise(180)
        }
    }

    LaunchedEffect(slide.isGlitchFinale) {
        if (slide.isGlitchFinale) {
            while (true) {
                delay(70L)
                glitchTick++
            }
        }
    }

    val kenBurnsTransition = rememberInfiniteTransition(label = "kenBurns")
    val zoom by kenBurnsTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse),
        label = "zoom"
    )
    val scenePhase by kenBurnsTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "scenePhase"
    )

    val sepiaMatrix = remember(sepiaIntensity) {
        ColorMatrix().apply {
            setToSaturation(1f - sepiaIntensity * 0.55f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = advanceStory
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("intro_tap_area")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Compact Top Bar with Language Toggle, Mute, Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (slide.isGlitchFinale) GlitchCyan else SepiaAmber
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slides.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .widthIn(min = if (index == currentIndex) 20.dp else 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        index == currentIndex && slide.isGlitchFinale -> GlitchCyan
                                        index == currentIndex -> SepiaAmber
                                        index < currentIndex -> SepiaBronze
                                        else -> SepiaDarkBorder
                                    }
                                )
                        )
                    }
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SepiaDarkBorder),
                        modifier = Modifier.testTag("intro_language_button")
                    ) {
                        Text(language.buttonLabel, style = MaterialTheme.typography.labelSmall, color = SepiaGold)
                    }
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier.testTag("intro_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Toggle sound",
                            tint = SepiaAmber
                        )
                    }
                    OutlinedButton(
                        onClick = onFinishIntro,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SepiaDarkBorder),
                        modifier = Modifier.testTag("intro_skip_button")
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = null, tint = ParchmentInk)
                        Spacer(Modifier.widthIn(min = 4.dp))
                        Text(tr(language, "SKIP", "تخطي"), style = MaterialTheme.typography.labelLarge, color = ParchmentInk)
                    }
                }
            }

            // Centerpiece Classic Yellow-Brown Sepia PIXEL ART Frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF140D07))
                    .border(
                        width = 3.dp,
                        color = if (slide.isGlitchFinale && glitchTick % 3 == 0) GlitchMagenta else SepiaAmber,
                        shape = RoundedCornerShape(4.dp)
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        SepiaAmber.copy(alpha = 0.08f * sepiaIntensity),
                                        Color(0xFF281608).copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val scanStep = 5.dp.toPx()
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = Color.Black.copy(alpha = 0.11f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f
                            )
                            y += scanStep
                        }
                        val bob = sin(scenePhase * 6.283f)
                        when (slide.id) {
                            1 -> {
                                // Animated Four Races Emblems + Angel/Devil winged silhouettes
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
                                // Pulsing golden heart aura + mini Loka walking across timeline portals
                                val pulse = (bob + 1f) * 0.5f
                                drawCircle(
                                    color = SepiaGold.copy(alpha = 0.22f * pulse),
                                    radius = size.minDimension * 0.32f,
                                    center = Offset(size.width / 2f, size.height / 2f)
                                )
                                val walkX = size.width * (0.20f + 0.60f * scenePhase)
                                val walkY = size.height * 0.82f + kotlin.math.abs(bob) * -4.dp.toPx()
                                drawLokaOverworldSprite(
                                    center = Offset(walkX, walkY),
                                    unit = 7.dp.toPx(),
                                    isGlitched = false
                                )
                            }
                            3 -> {
                                // Paradox lightning rifts cracking across war backdrop
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
                                // Other Lost Kids stars drifting apart into separate portals
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
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Classic Undertale RPG Dialogue Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp)
                    .background(EspressoSurface, RoundedCornerShape(4.dp))
                    .border(
                        width = 2.5.dp,
                        color = if (slide.isGlitchFinale) GlitchCyan else ParchmentInk,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val shownText = fullText.take(visibleChars) + if (visibleChars < fullText.length) "_" else ""
                    Text(
                        text = shownText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (slide.isGlitchFinale && glitchTick % 5 == 0) GlitchMagenta else ParchmentInk,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (visibleChars >= fullText.length) {
                        Text(
                            text = if (currentIndex == slides.lastIndex) {
                                tr(language, "TAP SCREEN TO WAKE UP >>", "اضغط الشاشة للاستيقاظ >>")
                            } else {
                                tr(language, "TAP SCREEN FOR NEXT >>", "اضغط الشاشة للتالي >>")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (slide.isGlitchFinale) GlitchCyan else SepiaAmber,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
                Button(
                    onClick = advanceStory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (slide.isGlitchFinale) GlitchCyan else SepiaAmber,
                        contentColor = ObsidianBg
                    ),
                    modifier = Modifier.testTag("intro_next_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text(
                        text = if (currentIndex == slides.lastIndex && visibleChars >= fullText.length) {
                            tr(language, "WAKE UP", "ابدأ")
                        } else {
                            tr(language, "NEXT", "التالي")
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
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
    modifier: Modifier = Modifier
) {
    val rand = remember(glitchTick) { Random(glitchTick * 7919) }
    Box(modifier = modifier.background(Color(0xFF070609))) {
        Image(
            painter = painterResource(id = R.drawable.img_pixel_more_kids),
            contentDescription = "Corrupted memory feed",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.28f
                    translationX = (rand.nextFloat() - 0.5f) * 28f * intensity
                    translationY = (rand.nextFloat() - 0.5f) * 14f * intensity
                }
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val sliceCount = (10 * intensity).toInt().coerceAtLeast(4)
            for (i in 0 until sliceCount) {
                val y = rand.nextFloat() * size.height
                val h = (4f + rand.nextFloat() * 20f)
                val color = when (i % 3) {
                    0 -> GlitchCyan.copy(alpha = 0.42f)
                    1 -> GlitchMagenta.copy(alpha = 0.42f)
                    else -> Color.White.copy(alpha = 0.30f)
                }
                drawRect(
                    color = color,
                    topLeft = Offset(rand.nextFloat() * size.width * 0.25f, y),
                    size = Size(size.width * (0.4f + rand.nextFloat() * 0.6f), h)
                )
            }
            drawRect(
                color = Color.Black.copy(alpha = 0.78f),
                topLeft = Offset(0f, size.height * (0.28f + (glitchTick % 4) * 0.08f)),
                size = Size(size.width, size.height * 0.20f)
            )
            repeat(55) {
                drawRect(
                    color = Color.White.copy(alpha = rand.nextFloat() * 0.6f),
                    topLeft = Offset(rand.nextFloat() * size.width, rand.nextFloat() * size.height),
                    size = Size(4f, 3f)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (glitchTick % 4 == 0) {
                    tr(language, "ERR // SIGNAL LOST", "خطأ // انقطع البث")
                } else {
                    tr(language, "[ MORE DATA EXPUNGED ]", "[ بيانات محذوفة ]")
                },
                style = MaterialTheme.typography.headlineMedium,
                color = if (glitchTick % 2 == 0) GlitchCyan else GlitchMagenta,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = tr(language, "THE REST OF THE RECORD WAS TAKEN AWAY", "بقية التسجيل تم سحبه وإخفاؤه"),
                style = MaterialTheme.typography.labelMedium,
                color = ParchmentInk,
                textAlign = TextAlign.Center
            )
        }
    }
}
