package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audio.RetroSoundEngine
import com.example.model.AppLanguage
import com.example.model.EnemyId
import com.example.model.PortraitExpression
import com.example.model.RouteType
import com.example.model.tr
import com.example.ui.components.BossSpriteCanvas
import com.example.ui.components.LokaPortraitCanvas
import com.example.ui.components.drawPixelSaveStar
import com.example.ui.components.drawSteppedPixelShadow
import com.example.ui.components.drawWingPair
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.GlitchMagenta
import com.example.ui.theme.HpBarGreen
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.MutedSepiaInk
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaAmber
import com.example.ui.theme.SepiaDarkBorder
import com.example.ui.theme.SepiaGold
import com.example.ui.theme.SoulSeraphCyan
import com.example.viewmodel.AppUiState
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RouteFinaleScreen(
    state: AppUiState,
    onToggleLanguage: () -> Unit,
    onFightFutureLoka: () -> Unit,
    onChoosePeaceSacrifice: () -> Unit,
    onAcceptPerfectPower: () -> Unit,
    onRestartWorldFromTapes: (RouteType) -> Unit,
    onReturnToWorlds: () -> Unit
) {
    BackHandler { onReturnToWorlds() }

    val lang = state.language
    val route = state.activeFinaleRoute

    val routeTitle = when (route) {
        RouteType.NO_MERCY -> tr(lang, "NO MERCY ROUTE", "مسار بلا رحمة")
        RouteType.MOST_EXPECTED -> tr(lang, "THE MOST EXPECTED ROUTE", "المسار الأكثر توقعاً")
        RouteType.PERFECT -> tr(lang, "THE PERFECT ROUTE", "المسار المثالي")
        RouteType.LOST_TAPES -> tr(lang, "LOST TAPES ROUTE", "مسار الأشرطة المفقودة")
    }
    val routeSubtitle = when (route) {
        RouteType.NO_MERCY -> tr(lang, route.subtitle, "اقتل الجميع · اهزم لوكا المستقبلي · دمّر العالم")
        RouteType.MOST_EXPECTED -> tr(lang, route.subtitle, "رحمة وقتل مختلط · تضحية لوكا من أجل سلام الأجناس الأربعة")
        RouteType.PERFECT -> tr(lang, route.subtitle, "أنقذ واعفُ عن الجميع · رِثْ قوة لوكا المستقبلي عبر الخطوط الزمنية")
        RouteType.LOST_TAPES -> tr(lang, route.subtitle, "اكشف كل الأسرار · تحكّم بالانتقال العشوائي · أعد تشغيل العالم")
    }

    val lokaExpr = when {
        route == RouteType.NO_MERCY -> PortraitExpression.ANGRY
        state.isFutureTransformed -> PortraitExpression.DETERMINED
        state.peaceSacrificeEnding || route == RouteType.PERFECT -> PortraitExpression.HAPPY
        else -> PortraitExpression.SURPRISED
    }
    val futureExpr = when {
        route == RouteType.NO_MERCY && !state.worldDestroyedEnding -> PortraitExpression.ANGRY
        route == RouteType.PERFECT -> PortraitExpression.HAPPY
        else -> PortraitExpression.NEUTRAL
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left: Animated Pixel Cutscene Stage
        Box(
            modifier = Modifier
                .weight(0.95f)
                .fillMaxHeight()
                .background(Color.Black, RoundedCornerShape(0.dp))
                .border(3.dp, route.badgeColor, RoundedCornerShape(0.dp))
        ) {
            RouteCutsceneCanvas(
                route = route,
                endingResolved = state.worldDestroyedEnding || state.peaceSacrificeEnding || state.isFutureTransformed,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
                    ) {
                        Text(lang.buttonLabel, style = MaterialTheme.typography.labelSmall, color = ParchmentInk)
                    }
                }
                Text(
                    text = routeTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = route.badgeColor,
                    textAlign = TextAlign.Center
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LokaPortraitCanvas(
                        saveCount = if (state.saveMysteryDiscovered) state.saveCounter else 0,
                        isFutureTransformed = state.isFutureTransformed,
                        expression = lokaExpr,
                        modifier = Modifier.size(105.dp)
                    )
                    BossSpriteCanvas(
                        enemyId = EnemyId.FUTURE_LOKA,
                        isHitFlash = state.worldDestroyedEnding,
                        isSpareReady = route == RouteType.PERFECT,
                        expression = futureExpr,
                        modifier = Modifier.size(118.dp)
                    )
                }
                Text(
                    text = routeSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentInk,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Right: Route-Specific Interactive Choices with Typewriter Narration
        Column(
            modifier = Modifier
                .weight(1.15f)
                .fillMaxHeight()
                .background(Color.Black, RoundedCornerShape(0.dp))
                .border(3.dp, Color.White, RoundedCornerShape(0.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (route) {
                    RouteType.NO_MERCY -> {
                        if (state.worldDestroyedEnding) {
                            Text(tr(lang, "WORLD DESTROYED", "دُمّر العالم"), style = MaterialTheme.typography.headlineMedium, color = DangerRed)
                            FinaleTypewriterText(
                                text = tr(
                                    lang,
                                    "You defeated Future-Loka, seized their timeline power, and erased all four worlds into silent ash.",
                                    "هزمت نسختك المستقبلية، واستوليت على قوتها الزمنية، ومحوت العوالم الأربعة إلى رماد صامت."
                                ),
                                voicePitchHz = 180f
                            )
                        } else {
                            FinaleTypewriterText(
                                text = tr(
                                    lang,
                                    "\"You killed everyone, Loka. To destroy this world, you'll have to go through ME—your future self!\"",
                                    "«لقد قتلت الجميع يا لوكا. لتدمير هذا العالم، عليك أن تتجاوزني أولاً—نسختك المستقبلية!»"
                                ),
                                voicePitchHz = 275f
                            )
                            Button(
                                onClick = onFightFutureLoka,
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = ParchmentInk),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("finale_fight_future_loka")
                            ) {
                                Text(tr(lang, "CONFRONT FUTURE-LOKA", "واجه لوكا المستقبلي"), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    RouteType.MOST_EXPECTED -> {
                        if (state.peaceSacrificeEnding) {
                            Text(tr(lang, "PEACE ACHIEVED — LOKA'S SACRIFICE", "تحقّق السلام — تضحية لوكا"), style = MaterialTheme.typography.headlineMedium, color = SepiaGold)
                            FinaleTypewriterText(
                                text = tr(
                                    lang,
                                    "Loka gave up his life to seal the paradoxes. The Government disbanded, and Humans, Monsters, Angels, and Devils finally lived in peace.",
                                    "ضحّى لوكا بحياته لإغلاق المفارقات الزمنية. انحلّت الحكومة، وعاش البشر والوحوش والملائكة والشياطين في سلام أخيراً."
                                ),
                                voicePitchHz = 320f
                            )
                        } else {
                            FinaleTypewriterText(
                                text = tr(
                                    lang,
                                    "\"You spared some and lost some, Loka. Do you want a better future instead of this? It will cost your life to bring peace to every race and end the Government.\"",
                                    "«عفوت عن البعض وخسرت البعض يا لوكا. هل تريد مستقبلاً أفضل من هذا؟ سيكلفك ذلك حياتك لتجلب السلام لكل الأجناس وتنهي الحكومة.»"
                                ),
                                voicePitchHz = 290f
                            )
                            Button(
                                onClick = onChoosePeaceSacrifice,
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SepiaAmber, contentColor = ObsidianBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("finale_sacrifice_peace")
                            ) {
                                Text(tr(lang, "YES — GIVE MY LIFE FOR PEACE", "نعم — أضحّي بحياتي من أجل السلام"), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    RouteType.PERFECT -> {
                        if (state.isFutureTransformed) {
                            Text(tr(lang, "ASCENDED: FUTURE-LOKA", "الارتقاء: لوكا المستقبلي"), style = MaterialTheme.typography.headlineMedium, color = SoulSeraphCyan)
                            FinaleTypewriterText(
                                text = tr(
                                    lang,
                                    "By sparing everyone and saving every life, Future-Loka entrusted their power to you. You became a copy of your futuristic self to end all evil across every timeline!",
                                    "لأنك عفوت عن الجميع وأنقذت حياتهم، منحك لوكا المستقبلي قوته الكاملة. أصبحت نسخة منه لتنهي كل شر في بقية الخطوط الزمنية!"
                                ),
                                voicePitchHz = 365f
                            )
                        } else {
                            FinaleTypewriterText(
                                text = tr(
                                    lang,
                                    "\"You gave mercy to every single soul! Take my timeline power, Loka—and end all evil across the other timelines.\"",
                                    "«منحت الرحمة لكل روح دون استثناء! خذ قوتي الزمنية يا لوكا—وأنهِ كل شر في الخطوط الزمنية الأخرى.»"
                                ),
                                voicePitchHz = 330f
                            )
                            Button(
                                onClick = onAcceptPerfectPower,
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SoulSeraphCyan, contentColor = ObsidianBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("finale_accept_perfect_power")
                            ) {
                                Text(tr(lang, "ACCEPT POWER & TRANSFORM", "اقبل القوة وتحوّل"), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    RouteType.LOST_TAPES -> {
                        Text(
                            text = tr(lang, "ALL SECRETS UNLOCKED — CURSED TELEPORT MASTERED", "كُشفت كل الأسرار — تحكم كامل بالانتقال"),
                            style = MaterialTheme.typography.titleMedium,
                            color = GlitchCyan
                        )
                        FinaleTypewriterText(
                            text = tr(
                                lang,
                                "Having uncovered every hidden Lab & Mansion tape across the worlds, Loka understands the 8-save paradox and masters his random teleport! Restart the world to choose your destiny:",
                                "بعد كشف جميع الأشرطة السرية في المختبرات والقصور، فهم لوكا سر السيڤ الثامن وسيطر على انتقاله العشوائي! أعد تشغيل العالم لاختيار مصيرك:"
                            ),
                            voicePitchHz = 240f
                        )
                        Button(
                            onClick = { onRestartWorldFromTapes(RouteType.PERFECT) },
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoulSeraphCyan, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tapes_restart_perfect")
                        ) {
                            Text(tr(lang, "RESTART -> PERFECT MERCY ROUTE", "إعادة العالم -> مسار الرحمة المثالي"), style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = { onRestartWorldFromTapes(RouteType.NO_MERCY) },
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = ParchmentInk),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tapes_restart_nomercy")
                        ) {
                            Text(tr(lang, "RESTART -> NO MERCY ROUTE", "إعادة العالم -> مسار بلا رحمة"), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onReturnToWorlds,
                shape = RoundedCornerShape(0.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tr(lang, "RETURN TO WORLDS", "العودة إلى العوالم"), style = MaterialTheme.typography.labelLarge, color = ParchmentInk)
            }
        }
    }
}

@Composable
private fun FinaleTypewriterText(
    text: String,
    voicePitchHz: Float
) {
    var shownChars by remember(text) { mutableIntStateOf(0) }
    LaunchedEffect(text) {
        shownChars = 0
        for (i in 1..text.length) {
            delay(18L)
            shownChars = i
            if (i % 4 == 1 && text[i - 1] != ' ') {
                RetroSoundEngine.playTypeBlip(voicePitchHz)
            }
        }
    }
    Text(
        text = text.take(shownChars),
        style = MaterialTheme.typography.bodyLarge,
        color = ParchmentInk
    )
}

@Composable
private fun RouteCutsceneCanvas(
    route: RouteType,
    endingResolved: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "routeCutscene")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4600, easing = LinearEasing), RepeatMode.Restart),
        label = "cutscenePhase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.52f

        when (route) {
            RouteType.NO_MERCY -> {
                // Falling crimson ash & stepped pixel paradox crack
                for (i in 0 until 14) {
                    val ax = ((i * 0.17f + phase * 0.25f) % 1f) * w
                    val ay = ((i * 0.23f + phase) % 1f) * h
                    drawRect(
                        color = if (endingResolved) Color(0xFF9CA3AF).copy(alpha = 0.35f) else DangerRed.copy(alpha = 0.40f),
                        topLeft = Offset(ax, ay),
                        size = Size(5.dp.toPx(), 5.dp.toPx())
                    )
                }
                for (step in 0..8) {
                    drawRect(
                        color = DangerRed.copy(alpha = 0.28f),
                        topLeft = Offset(w * step / 9f, cy - 36f + step * 9f),
                        size = Size(w / 9f + 2f, 4.dp.toPx())
                    )
                }
            }

            RouteType.MOST_EXPECTED -> {
                // Rising 4-race harmony stars & warm golden peace seal
                val colors = listOf(SepiaGold, HpBarGreen, SoulSeraphCyan, Color(0xFFEC4899))
                for (i in 0 until 8) {
                    val ang = phase * 6.283f + i * 0.785f
                    val sx = cx + cos(ang) * w * 0.36f
                    val sy = cy + sin(ang) * h * 0.26f
                    drawPixelSaveStar(Offset(sx, sy), 6.dp.toPx(), colors[i % colors.size], Color.White)
                }
            }

            RouteType.PERFECT -> {
                // Glowing cyan/gold wings & stepped pixel aura
                val auraW = minOf(w, h) * 0.70f
                drawSteppedPixelShadow(Offset(cx, cy), auraW, auraW * 0.80f, SoulSeraphCyan.copy(alpha = 0.14f))
                drawWingPair(
                    center = Offset(cx, cy),
                    spanPx = minOf(w, h) * 0.78f,
                    isAngelWings = true,
                    fillColor = MercyYellow.copy(alpha = 0.16f),
                    outlineColor = SoulSeraphCyan.copy(alpha = 0.50f)
                )
            }

            RouteType.LOST_TAPES -> {
                // Orbiting 8 Mystery Save Stars + cyan/magenta VHS waveform
                for (i in 0 until 8) {
                    val ang = phase * 6.283f + i * 0.785f
                    val sx = cx + cos(ang) * w * 0.35f
                    val sy = cy + sin(ang) * h * 0.28f
                    drawPixelSaveStar(Offset(sx, sy), 7.dp.toPx(), if (i % 2 == 0) GlitchCyan else GlitchMagenta, Color.White)
                }
            }
        }
    }
}
