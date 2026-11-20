package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.audio.RetroSoundEngine
import com.example.model.AppLanguage
import com.example.model.PortraitExpression
import com.example.model.tr
import com.example.ui.components.AshenFlowerCanvas
import com.example.ui.components.LokaFullBodySpriteCanvas
import com.example.ui.components.UltraPixelArtEngine
import com.example.ui.components.drawPixelSaveStar
import com.example.ui.components.drawSteppedPixelShadow
import com.example.ui.theme.ParchmentInk
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun AshenMeetingScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onSaveWithAshen: () -> Unit = {},
    onContinueToRandomWorld: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var savedByAshen by remember { mutableIntStateOf(0) }

    BackHandler(enabled = step > 0) {
        step--
    }

    val linesEn = listOf(
        "* ???: \"Heh... well look who finally walked out of the Ruins.\"",
        "* Ashen: \"My name is Ashen. Funny little golden heart necklace you've got there, Loka. Take a tip from me: watch telegraphs before dodging!\"",
        "* Ashen: \"Outside this door, you can't pick where time throws you—and I might even save your progress or pop up in your fights to help you!\"",
        "* Ashen: \"Oh, and if you ever land in the colorless Glitched Void... don't blink. Those 10 seconds only show shadows of your own past. Hehehe...\""
    )
    val linesAr = listOf(
        "* ؟؟؟: «هه... انظروا من خرج أخيراً من بوابة الرونز!»",
        "* آشن: «اسمي آشن. قلادة قلب ذهبي جميلة يا لوكا! نصيحة سريعة: راقب الأسهم والمستطيلات الحمراء قبل أن تنطلق!»",
        "* آشن: «خارج هذا الباب، قد أحفظ تقدمك وأعطيك نصائح، وربما أدخل معك في قتالاتك فجأة لأساعدك!»",
        "* آشن: «وإن سقطت يوماً في العالم المقلتش... فلن تبقى سوى 10 ثوانٍ تلاحقك فيها شظايا ماضيك. هيهيهي...»"
    )
    val lines = if (language == AppLanguage.AR) linesAr else linesEn
    val currentLine = lines[step]
    var visibleChars by remember(currentLine) { mutableIntStateOf(0) }

    LaunchedEffect(currentLine) {
        visibleChars = 0
        for (i in 1..currentLine.length) {
            if (visibleChars >= currentLine.length) break
            delay(20L)
            if (visibleChars >= currentLine.length) break
            visibleChars = i
            if (i % 4 == 1 && currentLine[i - 1] != ' ' && currentLine[i - 1] != '*') {
                RetroSoundEngine.playTypeBlip(if (step >= 3) 175f else 205f)
            }
        }
    }

    val advance: () -> Unit = {
        if (visibleChars < currentLine.length) {
            visibleChars = currentLine.length
        } else {
            RetroSoundEngine.playTypeBlip(if (step >= 2) 190f else 250f)
            if (step < lines.lastIndex) {
                step++
            } else {
                onContinueToRandomWorld()
            }
        }
    }

    val lokaExpression = when {
        step <= 1 -> PortraitExpression.SURPRISED
        step == 2 -> PortraitExpression.NEUTRAL
        else -> PortraitExpression.DETERMINED
    }
    val ashenExpression = when {
        step == 0 -> PortraitExpression.NEUTRAL
        step in 1..2 -> PortraitExpression.HAPPY
        else -> PortraitExpression.DETERMINED
    }

    val spotlightTransition = rememberInfiniteTransition(label = "ashenSpotlight")
    val ambientClock by spotlightTransition.animateFloat(
        initialValue = 0f,
        targetValue = 62.8f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing)),
        label = "ambientClock"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0712))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = advance
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // 256x144 (36,864 pixels!) Ultra-HD Pixel-Art Ruins Spotlight Stage + Animated Sparkles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stageBitmap = UltraPixelArtEngine.getAshenStageBitmap()
            drawImage(
                image = stageBitmap,
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(size.width.roundToInt().coerceAtLeast(1), size.height.roundToInt().coerceAtLeast(1)),
                filterQuality = FilterQuality.None
            )
            val beamCx = size.width * 0.63f
            val beamH = size.height * 0.69f
            for (i in 0..6) {
                val sx = beamCx + sin(i * 2.1f + ambientClock * 0.65f) * size.width * 0.16f
                val sy = beamH * (0.14f + i * 0.12f) + cos(ambientClock * 1.5f + i) * 5f
                val twinkle = 0.45f + 0.55f * abs(sin(ambientClock * 2.2f + i * 1.3f))
                drawPixelSaveStar(
                    center = Offset(sx, sy),
                    sizePx = 5.5.dp.toPx() * twinkle,
                    color = Color(0xFFFDE68A).copy(alpha = 0.9f * twinkle),
                    coreColor = Color.White.copy(alpha = twinkle)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar with Save-by-Ashen & Language Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (savedByAshen > 0) {
                        tr(language, "* ASHEN SAVED YOUR PROGRESS!", "* آشن حفظ تقدمك بنجاح!")
                    } else {
                        tr(language, "* BEYOND THE RUINS DOOR", "* خارج بوابة الرونز")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (savedByAshen > 0) Color(0xFF4ADE80) else Color(0xFF9CA3AF)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            RetroSoundEngine.playSaveChime()
                            savedByAshen++
                            onSaveWithAshen()
                        },
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFFACC15)),
                        modifier = Modifier.testTag("ashen_save_button")
                    ) {
                        Text(
                            text = if (savedByAshen > 0) tr(language, "SAVED ♥", "تم الحفظ ♥") else tr(language, "SAVE", "حفظ التقدم"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFACC15)
                        )
                    }
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFA1A1AA)),
                        modifier = Modifier.testTag("ashen_language_button")
                    ) {
                        Text(language.buttonLabel, style = MaterialTheme.typography.labelSmall, color = ParchmentInk)
                    }
                }
            }

            // Center Stage: Standing Frisk-bodied Loka facing much smaller Monochrome Ashen in spotlight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier.size(164.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawSteppedPixelShadow(
                            center = Offset(size.width * 0.5f, size.height * 0.87f),
                            widthPx = size.width * 0.68f,
                            heightPx = size.height * 0.16f,
                            color = Color(0x36D6C29F)
                        )
                    }
                    LokaFullBodySpriteCanvas(
                        expression = lokaExpression,
                        modifier = Modifier
                            .size(146.dp)
                            .padding(bottom = 12.dp)
                    )
                }
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawSteppedPixelShadow(
                            center = Offset(size.width * 0.5f, size.height * 0.82f),
                            widthPx = size.width * 0.78f,
                            heightPx = size.height * 0.22f,
                            color = Color(0x38E4E4E7)
                        )
                        drawSteppedPixelShadow(
                            center = Offset(size.width * 0.5f, size.height * 0.82f),
                            widthPx = size.width * 0.42f,
                            heightPx = size.height * 0.10f,
                            color = Color(0xFF27272E)
                        )
                    }
                    AshenFlowerCanvas(
                        isCreepySmirk = step >= 3,
                        expression = ashenExpression,
                        modifier = Modifier
                            .size(72.dp)
                            .padding(bottom = 14.dp)
                    )
                }
            }

            // Classic Sharp White-Bordered Undertale Dialogue Box with Speaker Portrait
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .background(Color.Black, RoundedCornerShape(0.dp))
                    .border(3.dp, Color(0xFFD4D4D8), RoundedCornerShape(0.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(Color(0xFF0B0B0D))
                        .border(2.dp, Color(0xFF52525B), RoundedCornerShape(0.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AshenFlowerCanvas(
                        isCreepySmirk = step >= 3,
                        expression = ashenExpression,
                        modifier = Modifier.size(66.dp)
                    )
                }
                Text(
                    text = currentLine.take(visibleChars),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                )
                Button(
                    onClick = advance,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4D4D8),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("ashen_next_button")
                ) {
                    Text(
                        text = if (step < lines.lastIndex) tr(language, "NEXT", "التالي") else tr(language, "WARP!", "انطلق!"),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
