package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.audio.RetroSoundEngine
import com.example.model.AppLanguage
import com.example.model.PortraitExpression
import com.example.model.tr
import com.example.ui.components.AshenFlowerCanvas
import com.example.ui.components.LokaPortraitCanvas
import com.example.ui.theme.ParchmentInk
import kotlinx.coroutines.delay

@Composable
fun AshenMeetingScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onSaveWithAshen: () -> Unit = {},
    onContinueToRandomWorld: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var savedByAshen by remember { mutableIntStateOf(0) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1F1F26), Color.Black)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = advance
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
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

            // Center Stage: Pixel Loka facing Monochrome Pixel Ashen Flower under spotlight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LokaPortraitCanvas(
                    saveCount = 0,
                    expression = lokaExpression,
                    modifier = Modifier.size(134.dp)
                )
                AshenFlowerCanvas(
                    isCreepySmirk = step >= 3,
                    expression = ashenExpression,
                    modifier = Modifier.size(164.dp)
                )
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
                    modifier = Modifier.weight(1f)
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
