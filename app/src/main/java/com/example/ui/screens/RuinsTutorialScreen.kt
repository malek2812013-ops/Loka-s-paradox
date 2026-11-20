package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.audio.RetroSoundEngine
import com.example.model.AppLanguage
import com.example.model.ControlScheme
import com.example.model.PortraitExpression
import com.example.model.tr
import com.example.ui.components.ControllerPadOverlay
import com.example.ui.components.LokaPortraitCanvas
import com.example.ui.components.drawDummyRoomProps
import com.example.ui.components.drawLokaOverworldSprite
import com.example.ui.components.drawSaveStar
import com.example.ui.components.drawUndertaleBossPixelSprite
import com.example.ui.theme.EspressoCard
import com.example.ui.theme.EspressoSurface
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.HpBarGreen
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.MutedSepiaInk
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaAmber
import com.example.ui.theme.SepiaDarkBorder
import com.example.ui.theme.SepiaGold
import com.example.viewmodel.AppUiState
import kotlinx.coroutines.delay
import kotlin.math.hypot

@Composable
fun RuinsTutorialScreen(
    state: AppUiState,
    onMovePlayer: (Float, Float) -> Unit,
    onActivateSwitch: () -> Unit,
    onPracticeDummy: () -> Unit,
    onToggleControls: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleMute: () -> Unit,
    onExitRuins: () -> Unit
) {
    var exitMonologueStep by remember { mutableIntStateOf(0) }
    var introDialogueStep by remember { mutableIntStateOf(0) }
    var dummyExplained by remember { mutableStateOf(false) }
    var padDirX by remember { mutableFloatStateOf(0f) }
    var padDirY by remember { mutableFloatStateOf(0f) }
    var ambientClock by remember { mutableFloatStateOf(0f) }

    BackHandler(enabled = exitMonologueStep > 0 || introDialogueStep > 0) {
        if (exitMonologueStep > 0) {
            exitMonologueStep--
        } else if (introDialogueStep > 0) {
            introDialogueStep--
        }
    }

    LaunchedEffect(state.controlScheme, exitMonologueStep) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    ambientClock = (ambientClock + dt) % 600f
                    if (exitMonologueStep == 0 && (padDirX != 0f || padDirY != 0f)) {
                        onMovePlayer(padDirX * 0.42f * dt, padDirY * 0.42f * dt)
                    }
                }
                lastNanos = nanos
            }
        }
    }

    val lang = state.language
    val px = state.ruinsPlayerX
    val py = state.ruinsPlayerY

    val switchPos = 0.34f to 0.50f
    val dummyPos = 0.60f to 0.48f
    val gatePos = 0.88f to 0.52f

    val nearSwitch = hypot(px - switchPos.first, py - switchPos.second) < 0.14f
    val nearDummy = hypot(px - dummyPos.first, py - dummyPos.second) < 0.14f
    val nearGate = hypot(px - gatePos.first, py - gatePos.second) < 0.15f

    val dialogueLines = listOf(
        tr(
            lang,
            "Loka: \"Whoa... purple stone walls and ancient pillars—looks like we woke up in the Ruins.\"",
            "لوكا: «واو... جدران حجرية بنفسجية وأعمدة قديمة—يبدو أننا استيقظنا داخل الرونز!»"
        ),
        tr(
            lang,
            "Loka: \"Hey, you! Drag on the floor (or switch to D-Pad) to walk me over to that glowing Golden Switch first.\"",
            "لوكا: «هيي أنت! اسحب إصبعك على الأرضية (أو بدّل إلى الأسهم) لتحرّكني نحو الزر الذهبي المضيء أولاً.»"
        )
    )

    val fourthWallLines = listOf(
        tr(
            lang,
            "Loka: \"The Ruins gate is open. Beyond here, the time paradox throws us into random worlds—we can't choose where.\"",
            "لوكا: «بوابة الرونز مفتوحة. بعد هذا الباب سترمي بنا المفارقة إلى عوالم عشوائية ولن نستطيع التحكم بوجهتنا.»"
        ),
        tr(
            lang,
            "Loka: \"...Wait. Hold on a second.\"",
            "لوكا: «...انتظر. توقف لحظة واحدة.»"
        ),
        tr(
            lang,
            "Loka: \"Don't think I didn't notice. I know I'm inside a game... and I know there's someone out there controlling me!\"",
            "لوكا: «لا تظن أنني لم ألاحظ... أنا أعلم أنني داخل لعبة، وأعلم أن هناك شخصاً خلف الشاشة يتحكم بي!»"
        ),
        tr(
            lang,
            "Loka: \"Alright then... let's see what kind of journey you make for us.\"",
            "لوكا: «حسناً إذن... دعنا نرى كيف ستقود هذه الرحلة.»"
        )
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left: Interactive Undertale-style Purple Ruins Chamber
        Box(
            modifier = Modifier
                .weight(1.55f)
                .fillMaxHeight()
                .border(3.dp, Color(0xFF9353B8), RoundedCornerShape(0.dp))
                .pointerInput(state.controlScheme, exitMonologueStep) {
                    if (state.controlScheme == ControlScheme.TOUCH_DRAG && exitMonologueStep == 0) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dx = dragAmount.x / size.width.toFloat()
                            val dy = dragAmount.y / size.height.toFloat()
                            onMovePlayer(dx, dy)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRuinsChamber(
                    switchPos = switchPos,
                    dummyPos = dummyPos,
                    gatePos = gatePos,
                    switchActive = state.ruinsSwitchActivated,
                    dummyPracticed = state.ruinsDummyPracticed,
                    ambientClock = ambientClock
                )
                drawLokaOverworldSprite(
                    center = Offset(px * size.width, py * size.height),
                    unit = 11.5.dp.toPx(),
                    isGlitched = false
                )
            }

            Text(
                text = tr(lang, "THE PARADOX RUINS · TUTORIAL", "أنقاض المفارقة · بداية اللعب"),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFE9D5FF),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            if (state.controlScheme != ControlScheme.TOUCH_DRAG && exitMonologueStep == 0) {
                ControllerPadOverlay(
                    controlScheme = state.controlScheme,
                    accentColor = Color(0xFFC084FC),
                    padSize = 112.dp,
                    onDirectionChanged = { dx, dy ->
                        padDirX = dx
                        padDirY = dy
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }
        }

        // Right: Loka's Undertale-Style Dialogue & Tutorial Panel
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black, RoundedCornerShape(0.dp))
                .border(3.dp, Color.White, RoundedCornerShape(0.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val lokaExpr = when {
                    exitMonologueStep in 1..2 -> PortraitExpression.SURPRISED
                    exitMonologueStep >= 3 -> PortraitExpression.DETERMINED
                    nearDummy -> PortraitExpression.DETERMINED
                    state.ruinsDummyPracticed || state.ruinsSwitchActivated -> PortraitExpression.HAPPY
                    else -> PortraitExpression.NEUTRAL
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LokaPortraitCanvas(
                        saveCount = 0,
                        expression = lokaExpr,
                        modifier = Modifier
                            .size(48.dp)
                            .background(EspressoCard, RoundedCornerShape(6.dp))
                            .border(1.5.dp, SepiaAmber, RoundedCornerShape(6.dp))
                    )
                    Column {
                        Text(tr(lang, "LOKA", "لوكا"), style = MaterialTheme.typography.titleMedium, color = SepiaGold)
                        Text(
                            text = if (exitMonologueStep > 0) tr(lang, "Speaking to YOU...", "يكلمك أنت...") else tr(lang, "The Ruins", "داخل الرونز"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (exitMonologueStep > 0) GlitchCyan else MutedSepiaInk
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SepiaDarkBorder),
                        modifier = Modifier.testTag("ruins_language_button")
                    ) {
                        Text(lang.buttonLabel, style = MaterialTheme.typography.labelSmall, color = SepiaGold)
                    }
                    IconButton(onClick = onToggleMute) {
                        Icon(
                            imageVector = if (state.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Mute",
                            tint = SepiaAmber
                        )
                    }
                }
            }

            val activeText = when {
                exitMonologueStep in 1..4 -> fourthWallLines[exitMonologueStep - 1]
                nearDummy && dummyExplained -> tr(
                    lang,
                    "Loka: \"Use FIGHT to strike the center, or ACT -> SPARE to make Friends! While dodging, switch Souls with SOUL and tap ABILITY!\"",
                    "لوكا: «استخدم FIGHT للضرب في المنتصف، أو ACT ثم SPARE لتكوين أصدقاء! وأثناء التفادي بدّل الأرواح واضغط القدرة!»"
                )
                nearDummy && state.ruinsDummyPracticed -> tr(
                    lang,
                    "Loka: \"Awesome practice! Now walk to the tall Exit Gate on the right so we can leave the Ruins.\"",
                    "لوكا: «تدريب ممتاز! الآن امشِ نحو بوابة الخروج الكبيرة على اليمين لنغادر الرونز.»"
                )
                nearDummy -> tr(
                    lang,
                    "Loka: \"Look—a Cotton Dummy! Try a quick practice fight or spare it to learn combat.\"",
                    "لوكا: «انظر—دمية قطنية! جرّب قتالاً تدريبياً أو صادقها لتفهم طريقة اللعب.»"
                )
                nearGate && (!state.ruinsSwitchActivated || (!state.ruinsDummyPracticed && !dummyExplained)) -> tr(
                    lang,
                    "Loka: \"Wait—press the glowing Golden Switch and check the Dummy first so you know how to survive out there!\"",
                    "لوكا: «انتظر—اضغط الزر الذهبي المضيء وتفقد الدمية أولاً حتى تعرف كيف تلعب وتتفادى في الخارج!»"
                )
                nearGate -> tr(
                    lang,
                    "Loka: \"There's the Exit Gate out of the Ruins. Ready to step through?\"",
                    "لوكا: «هذه بوابة الخروج من الرونز. هل أنت مستعد للخروج؟»"
                )
                state.ruinsSwitchActivated -> tr(
                    lang,
                    "Loka: \"Nice! Whenever we approach landmarks, NPCs, or other lost kids, a button pops up. Walk right to the Dummy next!\"",
                    "لوكا: «رائع! كلما اقتربنا من مكان أو شخص أو طفل مفقود سيظهر زر التفاعل. اتجه يميناً نحو الدمية الآن!»"
                )
                nearSwitch -> tr(
                    lang,
                    "Loka: \"Tap PRESS SWITCH! That's how we interact with secrets and quests in every world.\"",
                    "لوكا: «اضغط الزر الذهبي! هكذا نتفاعل مع المهام والأسرار في كل العوالم.»"
                )
                else -> dialogueLines[introDialogueStep.coerceIn(0, dialogueLines.lastIndex)]
            }

            val formattedActiveText = "* $activeText"
            var visibleChars by remember(formattedActiveText) { mutableIntStateOf(0) }
            LaunchedEffect(formattedActiveText) {
                visibleChars = 0
                for (i in 1..formattedActiveText.length) {
                    if (visibleChars >= formattedActiveText.length) break
                    delay(19L)
                    if (visibleChars >= formattedActiveText.length) break
                    visibleChars = i
                    if (i % 4 == 1 && formattedActiveText[i - 1] != ' ' && formattedActiveText[i - 1] != '*') {
                        RetroSoundEngine.playTypeBlip(if (exitMonologueStep >= 3) 290f else 345f)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 6.dp)
                    .background(Color.Black, RoundedCornerShape(0.dp))
                    .border(
                        width = 2.5.dp,
                        color = if (exitMonologueStep >= 3) GlitchCyan else Color.White,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { visibleChars = formattedActiveText.length }
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = formattedActiveText.take(visibleChars),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (exitMonologueStep >= 3) GlitchCyan else Color.White
                )
            }

            // Context Action Button + Drag/Dpad toggle
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                when {
                    exitMonologueStep in 1..3 -> {
                        Button(
                            onClick = { exitMonologueStep++ },
                            colors = ButtonDefaults.buttonColors(containerColor = GlitchCyan, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("ruins_monologue_next")
                        ) {
                            Text(tr(lang, "CONTINUE...", "تابع..."), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    exitMonologueStep >= 4 -> {
                        Button(
                            onClick = onExitRuins,
                            colors = ButtonDefaults.buttonColors(containerColor = SepiaAmber, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("ruins_step_out_button")
                        ) {
                            Text(tr(lang, "LEAVE THE RUINS >>", "اخرج من الرونز >>"), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    nearSwitch && !state.ruinsSwitchActivated -> {
                        Button(
                            onClick = onActivateSwitch,
                            colors = ButtonDefaults.buttonColors(containerColor = SepiaGold, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("ruins_switch_button")
                        ) {
                            Text(tr(lang, "PRESS GOLDEN SWITCH", "اضغط الزر الذهبي"), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    nearDummy -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    dummyExplained = true
                                    onPracticeDummy()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC084FC), contentColor = ObsidianBg),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("ruins_dummy_battle_button")
                            ) {
                                Text(tr(lang, "PRACTICE BATTLE", "تدرّب بالقتال"), style = MaterialTheme.typography.labelSmall)
                            }
                            if (!dummyExplained) {
                                OutlinedButton(
                                    onClick = { dummyExplained = true },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SepiaAmber),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text(tr(lang, "HOW TO?", "كيف؟"), style = MaterialTheme.typography.labelSmall, color = SepiaAmber)
                                }
                            }
                        }
                    }

                    nearGate && state.ruinsSwitchActivated && (state.ruinsDummyPracticed || dummyExplained) -> {
                        Button(
                            onClick = { exitMonologueStep = 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = HpBarGreen, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("ruins_open_gate_button")
                        ) {
                            Text(tr(lang, "EXIT THE RUINS", "افتح بوابة الخروج"), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    else -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    if (introDialogueStep < dialogueLines.lastIndex) introDialogueStep++
                                },
                                border = androidx.compose.foundation.BorderStroke(1.dp, SepiaDarkBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = when {
                                        !state.ruinsSwitchActivated && introDialogueStep == 0 -> tr(lang, "NEXT TIP >>", "النصيحة التالية >>")
                                        !state.ruinsSwitchActivated -> tr(lang, "WALK TO SWITCH ->", "اتجه للزر ->")
                                        !state.ruinsDummyPracticed && !dummyExplained -> tr(lang, "WALK TO DUMMY ->", "اتجه للدمية ->")
                                        else -> tr(lang, "WALK TO EXIT ->", "اتجه للبوابة ->")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentInk
                                )
                            }
                            OutlinedButton(
                                onClick = onToggleControls,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SepiaDarkBorder),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(if (lang == AppLanguage.AR) state.controlScheme.labelAr else state.controlScheme.label, style = MaterialTheme.typography.labelSmall, color = MutedSepiaInk)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawRuinsChamber(
    switchPos: Pair<Float, Float>,
    dummyPos: Pair<Float, Float>,
    gatePos: Pair<Float, Float>,
    switchActive: Boolean,
    dummyPracticed: Boolean,
    ambientClock: Float
) {
    // Deep Undertale Ruins dark-purple outer border backdrop
    drawRect(color = Color(0xFF1D0926))

    // Brick Top Wall (authentic Undertale Ruins purple brick mortar + top highlight bevel)
    val wallH = size.height * 0.25f
    drawRect(color = Color(0xFF542266), topLeft = Offset.Zero, size = Size(size.width, wallH))
    val brickH = wallH / 3f
    val brickW = size.width / 8f
    val mortar = Color(0xFF2D0E3A)
    for (row in 0..2) {
        val y = row * brickH
        drawLine(mortar, Offset(0f, y), Offset(size.width, y), strokeWidth = 3f)
        drawLine(Color(0xFF6B2E82), Offset(0f, y + 2f), Offset(size.width, y + 2f), strokeWidth = 1.5f)
        val xShift = if (row % 2 == 1) brickW * 0.5f else 0f
        for (col in 0..8) {
            val x = col * brickW + xShift
            if (x in 0f..size.width) {
                drawLine(mortar, Offset(x, y), Offset(x, y + brickH), strokeWidth = 3f)
            }
        }
    }
    drawRect(Color(0xFF230B2E), Offset(0f, wallH - 5.dp.toPx()), Size(size.width, 5.dp.toPx()))
    drawLine(Color(0xFF8C49A8), Offset(0f, wallH), Offset(size.width, wallH), strokeWidth = 3f)

    // Vine-draped stone columns along the Ruins wall
    listOf(0.18f, 0.46f, 0.74f).forEach { px ->
        val x = px * size.width
        drawRect(color = Color(0xFF3E164E), topLeft = Offset(x - 16.dp.toPx(), 0f), size = Size(32.dp.toPx(), wallH))
        drawRect(color = Color(0xFF5D2673), topLeft = Offset(x - 14.dp.toPx(), 0f), size = Size(10.dp.toPx(), wallH))
        drawRect(color = Color(0xFF7B3B96), topLeft = Offset(x - 16.dp.toPx(), 0f), size = Size(32.dp.toPx(), wallH), style = Stroke(2f))
        // Pixel hanging vines & small moss cap
        drawRect(color = Color(0xFF15803D), topLeft = Offset(x - 8.dp.toPx(), 4.dp.toPx()), size = Size(5.dp.toPx(), wallH * 0.55f))
        drawRect(color = Color(0xFF22C55E), topLeft = Offset(x + 4.dp.toPx(), 8.dp.toPx()), size = Size(5.dp.toPx(), wallH * 0.72f))
        drawRect(color = Color(0xFF4ADE80), topLeft = Offset(x + 6.dp.toPx(), 10.dp.toPx()), size = Size(2.dp.toPx(), wallH * 0.35f))
    }

    // Flickering Undertale Wall Torches between columns
    listOf(0.32f, 0.60f).forEachIndexed { idx, tx ->
        val fx = tx * size.width
        val fy = wallH * 0.52f
        val flicker = kotlin.math.sin(ambientClock * 9f + idx * 2.1f) * 2.dp.toPx()
        // Warm glow halo
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(Color(0x55F59E0B), Color(0x18F97316), Color.Transparent),
                center = Offset(fx, fy),
                radius = 44.dp.toPx() + flicker
            ),
            radius = 44.dp.toPx() + flicker,
            center = Offset(fx, fy)
        )
        // Iron sconce + multi-layer pixel flame
        drawRect(Color(0xFF475569), Offset(fx - 3.dp.toPx(), fy), Size(6.dp.toPx(), 12.dp.toPx()))
        drawRect(Color(0xFFEA580C), Offset(fx - 5.dp.toPx(), fy - 9.dp.toPx() - flicker * 0.4f), Size(10.dp.toPx(), 9.dp.toPx()))
        drawRect(Color(0xFFFACC15), Offset(fx - 3.dp.toPx(), fy - 7.dp.toPx() - flicker * 0.3f), Size(6.dp.toPx(), 6.dp.toPx()))
        drawRect(Color(0xFFFFFBEB), Offset(fx - 1.5.dp.toPx(), fy - 4.dp.toPx()), Size(3.dp.toPx(), 3.dp.toPx()))
    }

    // Lavender checkered Ruins stone tile floor
    val floorTop = wallH
    val floorH = size.height - wallH
    val cols = 12
    val rows = 6
    val tileW = size.width / cols
    val tileH = floorH / rows
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val isEdge = r == 0 || r == rows - 1 || c == 0 || c == cols - 1
            val baseColor = when {
                isEdge -> Color(0xFF3A184A)
                (r + c) % 2 == 0 -> Color(0xFF7C4391)
                else -> Color(0xFF6E3882)
            }
            val tileTop = floorTop + r * tileH
            drawRect(
                color = baseColor,
                topLeft = Offset(c * tileW, tileTop),
                size = Size(tileW + 0.5f, tileH + 0.5f)
            )
            if (!isEdge) {
                drawLine(
                    color = Color(0xFF9155A8).copy(alpha = 0.25f),
                    start = Offset(c * tileW + 2f, tileTop + 2f),
                    end = Offset((c + 1) * tileW - 2f, tileTop + 2f),
                    strokeWidth = 1.2f
                )
            }
            drawRect(
                color = Color(0xFF4B215C).copy(alpha = 0.45f),
                topLeft = Offset(c * tileW, tileTop),
                size = Size(tileW, tileH),
                style = Stroke(1.2f)
            )
        }
    }

    // Central crimson-plum Ruins walkway carpet + inner trim & tassels
    val rugLeft = size.width * 0.08f
    val rugTop = size.height * 0.41f
    val rugW = size.width * 0.80f
    val rugH = size.height * 0.24f
    drawRect(color = Color(0xFF4A1F5A), topLeft = Offset(rugLeft, rugTop), size = Size(rugW, rugH))
    drawRect(color = Color(0xFF642B78), topLeft = Offset(rugLeft + 5.dp.toPx(), rugTop + 5.dp.toPx()), size = Size(rugW - 10.dp.toPx(), rugH - 10.dp.toPx()))
    drawRect(color = Color(0xFFA264BA), topLeft = Offset(rugLeft, rugTop), size = Size(rugW, rugH), style = Stroke(2.5f))

    // Iconic red autumn Ruins leaf pile (classic Toriel's Ruins aesthetic)
    val leafSpots = listOf(0.20f to 0.74f, 0.24f to 0.78f, 0.22f to 0.81f, 0.49f to 0.34f, 0.53f to 0.36f, 0.72f to 0.76f)
    leafSpots.forEach { (lx, ly) ->
        drawRect(Color(0xFF991B1B), Offset(lx * size.width - 1f, ly * size.height + 2f), Size(7.dp.toPx(), 5.dp.toPx()))
        drawRect(Color(0xFFDC2626), Offset(lx * size.width, ly * size.height), Size(6.dp.toPx(), 5.dp.toPx()))
        drawRect(Color(0xFFF97316), Offset(lx * size.width + 4.dp.toPx(), ly * size.height + 2.dp.toPx()), Size(4.dp.toPx(), 4.dp.toPx()))
    }

    // Golden Ruins Floor Switch (Pixel Pad)
    val sc = Offset(switchPos.first * size.width, switchPos.second * size.height)
    drawCircle(color = (if (switchActive) HpBarGreen else MercyYellow).copy(alpha = 0.22f), radius = 26.dp.toPx(), center = sc)
    drawRect(color = Color(0xFF2B1838), topLeft = Offset(sc.x - 14.dp.toPx(), sc.y - 14.dp.toPx()), size = Size(28.dp.toPx(), 28.dp.toPx()))
    drawRect(color = if (switchActive) HpBarGreen else SepiaGold, topLeft = Offset(sc.x - 14.dp.toPx(), sc.y - 14.dp.toPx()), size = Size(28.dp.toPx(), 28.dp.toPx()), style = Stroke(2f))
    drawSaveStar(center = sc, radius = 11.dp.toPx(), color = if (switchActive) HpBarGreen else MercyYellow)

    // Crisp Pixel Cotton Dummy on wooden stand with shaded Sack & Crate props
    val dc = Offset(dummyPos.first * size.width, dummyPos.second * size.height)
    drawDummyRoomProps(dummyCenter = dc, unitPx = 11.dp.toPx())
    drawCircle(color = (if (dummyPracticed) HpBarGreen else Color(0xFFC084FC)).copy(alpha = 0.22f), radius = 24.dp.toPx(), center = dc)
    drawUndertaleBossPixelSprite(
        enemyId = com.example.model.EnemyId.RUINS_DUMMY,
        center = dc,
        sizePx = 38.dp.toPx(),
        isHitFlash = false,
        isSpareReady = dummyPracticed
    )

    // Arched Pixel Stone Ruins Exit Gate with keystone arch & shadow depth
    val gc = Offset(gatePos.first * size.width, gatePos.second * size.height)
    drawRect(
        color = Color(0xFF3E164E),
        topLeft = Offset(gc.x - 24.dp.toPx(), gc.y - 36.dp.toPx()),
        size = Size(48.dp.toPx(), 68.dp.toPx())
    )
    drawRect(
        color = if (switchActive) Color(0xFF07050A) else Color(0xFF3E294D),
        topLeft = Offset(gc.x - 19.dp.toPx(), gc.y - 31.dp.toPx()),
        size = Size(38.dp.toPx(), 60.dp.toPx())
    )
    drawRect(
        color = if (switchActive) SepiaGold else Color(0xFF9E6AC2),
        topLeft = Offset(gc.x - 20.dp.toPx(), gc.y - 32.dp.toPx()),
        size = Size(40.dp.toPx(), 62.dp.toPx()),
        style = Stroke(3.5f)
    )
    // Keystone crown above gate
    drawRect(
        color = if (switchActive) SepiaGold else Color(0xFFA855F7),
        topLeft = Offset(gc.x - 8.dp.toPx(), gc.y - 38.dp.toPx()),
        size = Size(16.dp.toPx(), 8.dp.toPx())
    )
    if (!switchActive) {
        for (bx in listOf(-10f, 0f, 10f)) {
            drawLine(
                Color(0xFFA78BFA),
                Offset(gc.x + bx.dp.toPx(), gc.y - 30.dp.toPx()),
                Offset(gc.x + bx.dp.toPx(), gc.y + 28.dp.toPx()),
                strokeWidth = 3f
            )
        }
    } else {
        drawSaveStar(center = Offset(gc.x, gc.y - 2.dp.toPx()), radius = 8.dp.toPx(), color = SepiaGold)
    }
}
