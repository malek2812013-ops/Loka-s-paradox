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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.audio.RetroSoundEngine
import com.example.model.AppLanguage
import com.example.model.ControlScheme
import com.example.model.PortraitExpression
import com.example.model.tr
import com.example.ui.components.ControllerPadOverlay
import com.example.ui.components.LokaPortraitCanvas
import com.example.ui.components.UltraPixelArtEngine
import com.example.ui.components.drawDummyRoomProps
import com.example.ui.components.drawLokaOverworldSprite
import com.example.ui.components.drawPixelBrickSaveMonument
import com.example.ui.components.drawSaveStar
import com.example.ui.components.drawSteppedPixelShadow
import com.example.ui.components.drawUndertaleBossPixelSprite
import kotlin.math.roundToInt
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
    onInspectLore: () -> Unit,
    onClaimCandy: () -> Unit,
    onActivateSwitch: () -> Unit,
    onPracticeDummy: () -> Unit,
    onToggleRunMode: () -> Unit,
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
    var moveAnimHoldSec by remember { mutableFloatStateOf(0f) }
    var facingRight by remember { mutableStateOf(true) }

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
                    moveAnimHoldSec = (moveAnimHoldSec - dt).coerceAtLeast(0f)
                    if (exitMonologueStep == 0 && (padDirX != 0f || padDirY != 0f)) {
                        if (padDirX > 0.05f) facingRight = true
                        else if (padDirX < -0.05f) facingRight = false
                        moveAnimHoldSec = 0.16f
                        onMovePlayer(padDirX * 0.42f * dt, padDirY * 0.42f * dt)
                    }
                }
                lastNanos = nanos
            }
        }
    }

    val lang = state.language
    val roomIndex = state.ruinsRoomIndex
    val px = state.ruinsPlayerX
    val py = state.ruinsPlayerY

    val pillarPos = 0.62f to 0.48f
    val switchPos = 0.40f to 0.48f
    val candyPos = 0.68f to 0.70f
    val dummyPos = 0.44f to 0.50f
    val gatePos = 0.84f to 0.52f

    val nearPillar = roomIndex == 0 && hypot(px - pillarPos.first, py - pillarPos.second) < 0.14f
    val nearSwitch = roomIndex == 1 && hypot(px - switchPos.first, py - switchPos.second) < 0.14f
    val nearCandy = roomIndex == 1 && hypot(px - candyPos.first, py - candyPos.second) < 0.13f
    val nearDummy = roomIndex == 2 && hypot(px - dummyPos.first, py - dummyPos.second) < 0.14f
    val nearGate = roomIndex == 2 && hypot(px - gatePos.first, py - gatePos.second) < 0.15f

    val roomTitle = when (roomIndex) {
        0 -> tr(lang, "THE RUINS (1/3) · FLOWERBED ENTRY", "أنقاض المفارقة (١/٣) · فراش الزهور")
        1 -> tr(lang, "THE RUINS (2/3) · PILLAR & SWITCH HALL", "أنقاض المفارقة (٢/٣) · رواق الأعمدة")
        else -> tr(lang, "THE RUINS (3/3) · DUMMY & EXIT GATE", "أنقاض المفارقة (٣/٣) · قاعة الخروج")
    }

    val dialogueLines = listOf(
        tr(
            lang,
            "Loka: \"Whoa... golden flowers cushioned our fall! Purple stone walls and ancient Ruins stretch way further than I expected.\"",
            "لوكا: «واو... أزهار ذهبية خفّفت سقوطنا! تبدو الرونز أكبر وأعمق بكثير مما توقعت!»"
        ),
        tr(
            lang,
            "Loka: \"Walk through the glowing arched doorways on the right/left edges (→ ←) to explore all 3 Ruins halls!\"",
            "لوكا: «اعبر عبر البوابات المقوّسة على اليمين واليسار (→ ←) لتستكشف قاعات الرونز الثلاث!»"
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
                            if (kotlin.math.abs(dx) > 0.0008f) {
                                facingRight = dx >= 0f
                            }
                            if (hypot(dx, dy) > 0.0008f) {
                                moveAnimHoldSec = 0.22f
                            }
                            onMovePlayer(dx, dy)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRuinsChamber(
                    roomIndex = roomIndex,
                    pillarPos = pillarPos,
                    switchPos = switchPos,
                    candyPos = candyPos,
                    dummyPos = dummyPos,
                    gatePos = gatePos,
                    loreRead = state.ruinsLoreRead,
                    candyClaimed = state.ruinsCandyClaimed,
                    switchActive = state.ruinsSwitchActivated,
                    dummyPracticed = state.ruinsDummyPracticed,
                    ambientClock = ambientClock
                )
                drawLokaOverworldSprite(
                    center = Offset(px * size.width, py * size.height),
                    unit = 11.5.dp.toPx(),
                    isGlitched = false,
                    isMoving = moveAnimHoldSec > 0f,
                    isRunning = state.isRunningMode,
                    animClock = ambientClock,
                    facingRight = facingRight
                )
            }

            Text(
                text = roomTitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFE9D5FF),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(0.dp))
                    .border(1.5.dp, Color(0xFFA855F7), RoundedCornerShape(0.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            if (roomIndex > 0) {
                Text(
                    text = "← ${tr(lang, "PREV ROOM", "القاعة السابقة")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SepiaGold,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                        .background(Color.Black.copy(alpha = 0.88f), RoundedCornerShape(0.dp))
                        .border(1.5.dp, SepiaAmber, RoundedCornerShape(0.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            if (roomIndex < 2) {
                Text(
                    text = "${tr(lang, "NEXT ROOM", "القاعة التالية")} →",
                    style = MaterialTheme.typography.labelSmall,
                    color = SepiaGold,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                        .background(Color.Black.copy(alpha = 0.88f), RoundedCornerShape(0.dp))
                        .border(1.5.dp, SepiaAmber, RoundedCornerShape(0.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

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
                            .background(Color.Black, RoundedCornerShape(0.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(0.dp))
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
                        shape = RoundedCornerShape(0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, SepiaDarkBorder),
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
                nearPillar && state.ruinsLoreRead -> tr(
                    lang,
                    "Carving: \"When the Golden Necklace hums beneath the violet sky, even shattered timelines leave a doorway home.\" Head right (→) to the Corridor!",
                    "النقش القديم: «حين تهمس القلادة الذهبية تحت السماء البنفسجية، حتى الخطوط الزمنية المكسورة تترك باباً للعودة.» اتجه يميناً (→)!"
                )
                nearPillar -> tr(
                    lang,
                    "Loka: \"There's an ancient Ruins carving on this stone pillar! Tap INSPECT PILLAR to read it.\"",
                    "لوكا: «هناك نقش قديم على هذا العمود الحجري! اضغط لقراءة النقش.»"
                )
                nearCandy && !state.ruinsCandyClaimed -> tr(
                    lang,
                    "Loka: \"Ooh! Someone left a gleaming Chrono Shard on this Ruins pedestal. Take one!\"",
                    "لوكا: «أوه! ترك أحدهم شظية زمنية علاجية على هذه المنصة. خذها معك!»"
                )
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
                    "Loka: \"Wait—press the Golden Switch in Hall 2 (←) and test the Cotton Dummy first so the bars open!\"",
                    "لوكا: «انتظر—اضغط الزر الذهبي في القاعة ٢ (←) وجرّب الدمية أولاً حتى تفتح قضبان البوابة!»"
                )
                nearGate -> tr(
                    lang,
                    "Loka: \"There's the Exit Gate out of the Ruins. Ready to step through?\"",
                    "لوكا: «هذه بوابة الخروج من الرونز. هل أنت مستعد للخروج؟»"
                )
                nearSwitch && state.ruinsSwitchActivated -> tr(
                    lang,
                    "Loka: \"The Ruins Exit Gate bars unlocked in Hall 3! Walk through the right archway (→) to meet the Dummy.\"",
                    "لوكا: «فُتحت قضبان بوابة الخروج في القاعة ٣! اعبر البوابة اليمنى (→) نحو الدمية.»"
                )
                nearSwitch -> tr(
                    lang,
                    "Loka: \"Tap PRESS SWITCH! That opens the stone bars in the Exit Hall.\"",
                    "لوكا: «اضغط الزر الذهبي! هذا يفتح بوابة الخروج في القاعة الثالثة.»"
                )
                roomIndex == 1 && !state.ruinsSwitchActivated -> tr(
                    lang,
                    "Loka: \"This corridor has ancient torches and autumn leaves. Walk up to the Golden Switch Monument!\"",
                    "لوكا: «هذا الرواق مليء بالمشاعل وأوراق الخريف. اقترب من نصب الزر الذهبي واضغطه!»"
                )
                roomIndex == 1 -> tr(
                    lang,
                    "Loka: \"Switch activated! Check the pedestal below or walk right (→) into the Dummy & Exit Hall.\"",
                    "لوكا: «تم تفعيل الزر! تفقد المنصة بالأسفل أو اعبر يميناً (→) لقاعة الدمية والخروج.»"
                )
                roomIndex == 2 && !state.ruinsSwitchActivated -> tr(
                    lang,
                    "Loka: \"The Exit Gate bars are locked! Walk back left (←) to Hall 2 to press the Golden Switch.\"",
                    "لوكا: «بوابة الخروج ما زالت مقفلة! عد يساراً (←) للقاعة ٢ لتضغط الزر الذهبي.»"
                )
                roomIndex == 2 -> tr(
                    lang,
                    "Loka: \"Practice with the Cotton Dummy or walk over to the open Ruins Exit Gate on the right!\"",
                    "لوكا: «تدرّب مع الدمية القطنية أو اتجه نحو بوابة الخروج المفتوحة على اليمين!»"
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
                            shape = RoundedCornerShape(0.dp),
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
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SepiaAmber, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("ruins_step_out_button")
                        ) {
                            Text(tr(lang, "LEAVE THE RUINS >>", "اخرج من الرونز >>"), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    nearPillar && !state.ruinsLoreRead -> {
                        Button(
                            onClick = onInspectLore,
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC084FC), contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("ruins_inspect_lore_button")
                        ) {
                            Text(tr(lang, "INSPECT ANCIENT PILLAR", "اقرأ النقش القديم"), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    nearCandy && !state.ruinsCandyClaimed -> {
                        Button(
                            onClick = onClaimCandy,
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SepiaGold, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("ruins_claim_candy_button")
                        ) {
                            Text(tr(lang, "TAKE CHRONO SHARD (+1)", "خذ شظية زمنية (+١)"), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    nearSwitch && !state.ruinsSwitchActivated -> {
                        Button(
                            onClick = onActivateSwitch,
                            shape = RoundedCornerShape(0.dp),
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
                                shape = RoundedCornerShape(0.dp),
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
                                    shape = RoundedCornerShape(0.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SepiaAmber),
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
                            shape = RoundedCornerShape(0.dp),
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
                                shape = RoundedCornerShape(0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, SepiaDarkBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = when {
                                        roomIndex == 0 && introDialogueStep == 0 -> tr(lang, "NEXT TIP >>", "النصيحة التالية >>")
                                        roomIndex == 0 -> tr(lang, "WALK EAST DOOR →", "اعبر البوابة اليمنى →")
                                        !state.ruinsSwitchActivated -> tr(lang, "PRESS SWITCH (HALL 2)", "اضغط الزر (قاعة ٢)")
                                        roomIndex == 1 -> tr(lang, "WALK EAST DOOR →", "اعبر نحو الدمية →")
                                        !state.ruinsDummyPracticed && !dummyExplained -> tr(lang, "WALK TO DUMMY", "اتجه للدمية")
                                        else -> tr(lang, "WALK TO EXIT →", "اتجه للبوابة →")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentInk
                                )
                            }
                            OutlinedButton(
                                onClick = onToggleRunMode,
                                shape = RoundedCornerShape(0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (state.isRunningMode) MercyYellow else SepiaDarkBorder),
                                modifier = Modifier.height(48.dp).testTag("ruins_run_toggle_button")
                            ) {
                                Text(
                                    text = if (state.isRunningMode) tr(lang, "RUN ⚡", "ركض ⚡") else tr(lang, "WALK", "مشي"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.isRunningMode) MercyYellow else MutedSepiaInk
                                )
                            }
                            OutlinedButton(
                                onClick = onToggleControls,
                                shape = RoundedCornerShape(0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, SepiaDarkBorder),
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
    roomIndex: Int,
    pillarPos: Pair<Float, Float>,
    switchPos: Pair<Float, Float>,
    candyPos: Pair<Float, Float>,
    dummyPos: Pair<Float, Float>,
    gatePos: Pair<Float, Float>,
    loreRead: Boolean,
    candyClaimed: Boolean,
    switchActive: Boolean,
    dummyPracticed: Boolean,
    ambientClock: Float
) {
    // 256x144 (36,864 pixels!) crisp cached Ruins brick & lavender tile base per room
    val ruinsBitmap = UltraPixelArtEngine.getRuinsChamberBitmap(roomIndex)
    drawImage(
        image = ruinsBitmap,
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(size.width.roundToInt().coerceAtLeast(1), size.height.roundToInt().coerceAtLeast(1)),
        filterQuality = FilterQuality.None
    )

    val wallH = size.height * 0.25f

    // Vine-draped stone columns along the Ruins wall
    val columnXs = when (roomIndex) {
        0 -> listOf(0.14f, 0.44f, 0.78f)
        1 -> listOf(0.18f, 0.50f, 0.82f)
        else -> listOf(0.16f, 0.42f, 0.68f)
    }
    columnXs.forEach { px ->
        val x = px * size.width
        drawRect(color = Color(0xFF3E164E), topLeft = Offset(x - 16.dp.toPx(), 0f), size = Size(32.dp.toPx(), wallH))
        drawRect(color = Color(0xFF5D2673), topLeft = Offset(x - 14.dp.toPx(), 0f), size = Size(10.dp.toPx(), wallH))
        drawRect(color = Color(0xFF7B3B96), topLeft = Offset(x - 16.dp.toPx(), 0f), size = Size(32.dp.toPx(), wallH), style = Stroke(2f))
        drawRect(color = Color(0xFF15803D), topLeft = Offset(x - 8.dp.toPx(), 4.dp.toPx()), size = Size(5.dp.toPx(), wallH * 0.55f))
        drawRect(color = Color(0xFF22C55E), topLeft = Offset(x + 4.dp.toPx(), 8.dp.toPx()), size = Size(5.dp.toPx(), wallH * 0.72f))
        drawRect(color = Color(0xFF4ADE80), topLeft = Offset(x + 6.dp.toPx(), 10.dp.toPx()), size = Size(2.dp.toPx(), wallH * 0.35f))
    }

    // Flickering Undertale Wall Torches between columns
    val torchXs = when (roomIndex) {
        0 -> listOf(0.58f, 0.88f)
        1 -> listOf(0.34f, 0.66f)
        else -> listOf(0.29f, 0.55f)
    }
    torchXs.forEachIndexed { idx, tx ->
        val fx = tx * size.width
        val fy = wallH * 0.52f
        val flicker = kotlin.math.sin(ambientClock * 9f + idx * 2.1f) * 2.dp.toPx()
        // Stepped pixel torch glow blocks (authentic Undertale lighting)
        val glowW = 36.dp.toPx() + flicker
        drawRect(Color(0x16F97316), Offset(fx - glowW * 0.65f, fy - glowW * 0.45f), Size(glowW * 1.3f, glowW * 0.9f))
        drawRect(Color(0x2CF59E0B), Offset(fx - glowW * 0.42f, fy - glowW * 0.30f), Size(glowW * 0.84f, glowW * 0.6f))
        drawRect(Color(0xFF475569), Offset(fx - 3.dp.toPx(), fy), Size(6.dp.toPx(), 12.dp.toPx()))
        drawRect(Color(0xFFEA580C), Offset(fx - 5.dp.toPx(), fy - 9.dp.toPx() - flicker * 0.4f), Size(10.dp.toPx(), 9.dp.toPx()))
        drawRect(Color(0xFFFACC15), Offset(fx - 3.dp.toPx(), fy - 7.dp.toPx() - flicker * 0.3f), Size(6.dp.toPx(), 6.dp.toPx()))
        drawRect(Color(0xFFFFFBEB), Offset(fx - 1.5.dp.toPx(), fy - 4.dp.toPx()), Size(3.dp.toPx(), 3.dp.toPx()))
    }

    // West & East Room Transition Archway Thresholds (aligns with ny in 0.36f..0.74f)
    val doorTop = size.height * 0.38f
    val doorH = size.height * 0.32f
    val doorPulse = 0.25f + 0.15f * kotlin.math.sin(ambientClock * 3.5f)
    if (roomIndex > 0) {
        drawRect(Color(0xFF2B1038), Offset(0f, doorTop), Size(14.dp.toPx(), doorH))
        drawRect(SepiaGold.copy(alpha = doorPulse), Offset(2.dp.toPx(), doorTop + 4.dp.toPx()), Size(8.dp.toPx(), doorH - 8.dp.toPx()))
        drawRect(Color(0xFFA264BA), Offset(0f, doorTop), Size(14.dp.toPx(), doorH), style = Stroke(2f))
    }
    if (roomIndex < 2) {
        val rightX = size.width - 14.dp.toPx()
        drawRect(Color(0xFF2B1038), Offset(rightX, doorTop), Size(14.dp.toPx(), doorH))
        drawRect(SepiaGold.copy(alpha = doorPulse), Offset(rightX + 4.dp.toPx(), doorTop + 4.dp.toPx()), Size(8.dp.toPx(), doorH - 8.dp.toPx()))
        drawRect(Color(0xFFA264BA), Offset(rightX, doorTop), Size(14.dp.toPx(), doorH), style = Stroke(2f))
    }

    when (roomIndex) {
        0 -> {
            // Room 0: Flowerbed Awakening Entry & Ancient Ruins Lore Pillar
            val rugLeft = size.width * 0.42f
            val rugTop = size.height * 0.44f
            val rugW = size.width * 0.56f
            val rugH = size.height * 0.20f
            drawRect(color = Color(0xFF4A1F5A), topLeft = Offset(rugLeft, rugTop), size = Size(rugW, rugH))
            drawRect(color = Color(0xFF642B78), topLeft = Offset(rugLeft + 4.dp.toPx(), rugTop + 4.dp.toPx()), size = Size(rugW - 8.dp.toPx(), rugH - 8.dp.toPx()))
            drawRect(color = Color(0xFFA264BA), topLeft = Offset(rugLeft, rugTop), size = Size(rugW, rugH), style = Stroke(2.5f))

            // Floating golden dust motes over the spotlight flowerbed
            for (i in 0..5) {
                val mx = size.width * (0.19f + 0.025f * i)
                val my = size.height * (0.26f + ((ambientClock * 0.14f + i * 0.17f) % 0.34f))
                drawRect(Color(0xFFFEF08A).copy(alpha = 0.72f), Offset(mx, my), Size(3.dp.toPx(), 3.dp.toPx()))
            }

            // Ancient Lore Pillar Monument
            val pc = Offset(pillarPos.first * size.width, pillarPos.second * size.height)
            val runeGlow = if (loreRead) SepiaGold else GlitchCyan
            drawSteppedPixelShadow(Offset(pc.x, pc.y + 18.dp.toPx()), 42.dp.toPx(), 14.dp.toPx(), runeGlow.copy(alpha = 0.22f))
            drawRect(Color(0xFF421A54), Offset(pc.x - 16.dp.toPx(), pc.y - 24.dp.toPx()), Size(32.dp.toPx(), 44.dp.toPx()))
            drawRect(Color(0xFF6B2D84), Offset(pc.x - 13.dp.toPx(), pc.y - 21.dp.toPx()), Size(26.dp.toPx(), 38.dp.toPx()))
            drawRect(Color(0xFFA864BF), Offset(pc.x - 16.dp.toPx(), pc.y - 24.dp.toPx()), Size(32.dp.toPx(), 44.dp.toPx()), style = Stroke(2.5f))
            // Rune tablet plaque & glowing glyph lines
            drawRect(Color(0xFF1F0C28), Offset(pc.x - 10.dp.toPx(), pc.y - 12.dp.toPx()), Size(20.dp.toPx(), 18.dp.toPx()))
            drawRect(runeGlow, Offset(pc.x - 7.dp.toPx(), pc.y - 8.dp.toPx()), Size(14.dp.toPx(), 2.5.dp.toPx()))
            drawRect(runeGlow.copy(alpha = 0.85f), Offset(pc.x - 7.dp.toPx(), pc.y - 3.dp.toPx()), Size(10.dp.toPx(), 2.5.dp.toPx()))
            drawRect(runeGlow.copy(alpha = 0.75f), Offset(pc.x - 7.dp.toPx(), pc.y + 2.dp.toPx()), Size(12.dp.toPx(), 2.5.dp.toPx()))
        }

        1 -> {
            // Room 1: Pillar & Switch Hall (Fountain Basin, Autumn Leaves, Golden Switch, Chrono Shard Pedestal)
            val rugLeft = size.width * 0.04f
            val rugTop = size.height * 0.42f
            val rugW = size.width * 0.92f
            val rugH = size.height * 0.22f
            drawRect(color = Color(0xFF4A1F5A), topLeft = Offset(rugLeft, rugTop), size = Size(rugW, rugH))
            drawRect(color = Color(0xFF642B78), topLeft = Offset(rugLeft + 5.dp.toPx(), rugTop + 5.dp.toPx()), size = Size(rugW - 10.dp.toPx(), rugH - 10.dp.toPx()))
            drawRect(color = Color(0xFFA264BA), topLeft = Offset(rugLeft, rugTop), size = Size(rugW, rugH), style = Stroke(2.5f))

            // Decorative Ruins Fountain Basin along upper stone walkway
            val fx = size.width * 0.72f
            val fy = size.height * 0.31f
            val shimmer = kotlin.math.sin(ambientClock * 5f) * 0.15f + 0.75f
            drawRect(Color(0xFF4C1D60), Offset(fx - 26.dp.toPx(), fy - 8.dp.toPx()), Size(52.dp.toPx(), 18.dp.toPx()))
            drawRect(Color(0xFF0E7490), Offset(fx - 21.dp.toPx(), fy - 5.dp.toPx()), Size(42.dp.toPx(), 12.dp.toPx()))
            drawRect(GlitchCyan.copy(alpha = shimmer), Offset(fx - 15.dp.toPx(), fy - 2.dp.toPx()), Size(18.dp.toPx(), 3.dp.toPx()))
            drawRect(Color(0xFFA864BF), Offset(fx - 26.dp.toPx(), fy - 8.dp.toPx()), Size(52.dp.toPx(), 18.dp.toPx()), style = Stroke(2f))

            // Iconic red autumn Ruins leaf piles
            val leafSpots = listOf(0.20f to 0.74f, 0.24f to 0.78f, 0.22f to 0.81f, 0.49f to 0.34f, 0.53f to 0.36f, 0.84f to 0.76f)
            leafSpots.forEach { (lx, ly) ->
                drawRect(Color(0xFF991B1B), Offset(lx * size.width - 1f, ly * size.height + 2f), Size(7.dp.toPx(), 5.dp.toPx()))
                drawRect(Color(0xFFDC2626), Offset(lx * size.width, ly * size.height), Size(6.dp.toPx(), 5.dp.toPx()))
                drawRect(Color(0xFFF97316), Offset(lx * size.width + 4.dp.toPx(), ly * size.height + 2.dp.toPx()), Size(4.dp.toPx(), 4.dp.toPx()))
            }

            // Golden Ruins Brick & Moss Switch Pedestal
            val sc = Offset(switchPos.first * size.width, switchPos.second * size.height)
            drawSteppedPixelShadow(Offset(sc.x, sc.y + 16.dp.toPx()), 44.dp.toPx(), 14.dp.toPx(), (if (switchActive) HpBarGreen else MercyYellow).copy(alpha = 0.24f))
            drawPixelBrickSaveMonument(
                center = Offset(sc.x, sc.y + 4.dp.toPx()),
                sizePx = 30.dp.toPx(),
                ambientClock = ambientClock,
                brickBase = Color(0xFF7B3F8E),
                brickShadow = Color(0xFF4C2259),
                brickHighlight = Color(0xFFA864BF),
                mossColor = Color(0xFF2D6A4F),
                emblemColor = if (switchActive) HpBarGreen else MercyYellow
            )

            // Chrono Shard Relic Pedestal
            val cc = Offset(candyPos.first * size.width, candyPos.second * size.height)
            drawRect(Color(0xFF421A54), Offset(cc.x - 14.dp.toPx(), cc.y - 6.dp.toPx()), Size(28.dp.toPx(), 18.dp.toPx()))
            drawRect(Color(0xFFA864BF), Offset(cc.x - 14.dp.toPx(), cc.y - 6.dp.toPx()), Size(28.dp.toPx(), 18.dp.toPx()), style = Stroke(2f))
            if (!candyClaimed) {
                val bob = kotlin.math.sin(ambientClock * 4f) * 2.5.dp.toPx()
                drawSteppedPixelShadow(Offset(cc.x, cc.y - 10.dp.toPx() + bob), 24.dp.toPx(), 18.dp.toPx(), SepiaGold.copy(alpha = 0.24f))
                drawSaveStar(center = Offset(cc.x, cc.y - 10.dp.toPx() + bob), radius = 7.dp.toPx(), color = SepiaGold)
            }
        }

        else -> {
            // Room 2: Dummy & Exit Sanctum
            val rugLeft = size.width * 0.04f
            val rugTop = size.height * 0.42f
            val rugW = size.width * 0.78f
            val rugH = size.height * 0.22f
            drawRect(color = Color(0xFF4A1F5A), topLeft = Offset(rugLeft, rugTop), size = Size(rugW, rugH))
            drawRect(color = Color(0xFF642B78), topLeft = Offset(rugLeft + 5.dp.toPx(), rugTop + 5.dp.toPx()), size = Size(rugW - 10.dp.toPx(), rugH - 10.dp.toPx()))
            drawRect(color = Color(0xFFA264BA), topLeft = Offset(rugLeft, rugTop), size = Size(rugW, rugH), style = Stroke(2.5f))

            listOf(0.18f to 0.75f, 0.23f to 0.79f, 0.62f to 0.76f).forEach { (lx, ly) ->
                drawRect(Color(0xFF991B1B), Offset(lx * size.width - 1f, ly * size.height + 2f), Size(7.dp.toPx(), 5.dp.toPx()))
                drawRect(Color(0xFFDC2626), Offset(lx * size.width, ly * size.height), Size(6.dp.toPx(), 5.dp.toPx()))
            }

            // Crisp Pixel Cotton Dummy on wooden stand with shaded Sack & Crate props
            val dc = Offset(dummyPos.first * size.width, dummyPos.second * size.height)
            drawDummyRoomProps(dummyCenter = dc, unitPx = 11.dp.toPx())
            drawSteppedPixelShadow(Offset(dc.x, dc.y + 14.dp.toPx()), 40.dp.toPx(), 12.dp.toPx(), (if (dummyPracticed) HpBarGreen else Color(0xFFC084FC)).copy(alpha = 0.24f))
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
    }
}
