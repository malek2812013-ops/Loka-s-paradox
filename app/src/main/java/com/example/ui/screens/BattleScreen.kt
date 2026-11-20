package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.RetroSoundEngine
import com.example.model.AppLanguage
import com.example.model.ArmorEquipment
import com.example.model.BattlePhase
import com.example.model.BulletKind
import com.example.model.ControlScheme
import com.example.model.EnemyId
import com.example.model.EnemyRoster
import com.example.model.MenuSubTab
import com.example.model.PortraitExpression
import com.example.model.SoulType
import com.example.model.WeaponCatalog
import com.example.model.WeaponEquipment
import com.example.model.WeaponStyle
import com.example.model.WorldContentCatalog
import com.example.model.WorldId
import com.example.model.tr
import com.example.ui.components.AshenFlowerCanvas
import com.example.ui.components.BossSpriteCanvas
import com.example.ui.components.ControllerPadOverlay
import com.example.ui.components.LokaPortraitCanvas
import com.example.ui.components.drawBossChompHeadAndClaw
import com.example.ui.components.drawDirectionalArrow
import com.example.ui.components.drawPixelBomb
import com.example.ui.components.drawPixelHeart
import com.example.ui.components.drawPixelSaveStar
import com.example.ui.components.drawSoulEmblem
import kotlinx.coroutines.delay
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.HpBarGreen
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.MutedSepiaInk
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaAmber
import com.example.ui.theme.SepiaGold
import com.example.viewmodel.BattleUiState
import com.example.viewmodel.BattleViewModel

private val UndertaleOrange = Color(0xFFFF8C1A)
private val UndertaleHpYellow = Color(0xFFFFFF00)
private val UndertaleHpRed = Color(0xFFC80000)

@Composable
fun BattleScreen(
    language: AppLanguage,
    enemyId: EnemyId,
    worldId: WorldId,
    equippedArmor: ArmorEquipment,
    equippedWeapon: WeaponEquipment = WeaponCatalog.byId("worn_stick"),
    friendAllies: Set<EnemyId>,
    helpedKidIds: Set<String>,
    itemCounts: Map<String, Int>,
    controlScheme: ControlScheme,
    onToggleControls: () -> Unit,
    onToggleLanguage: () -> Unit,
    onBattleFinished: (EnemyId, Boolean, Map<String, Int>) -> Unit,
    onExitBattle: () -> Unit,
    battleViewModel: BattleViewModel = viewModel()
) {
    BackHandler { onExitBattle() }

    LaunchedEffect(enemyId, worldId, helpedKidIds, equippedWeapon.id, equippedArmor.id) {
        battleViewModel.initEncounter(
            enemyId = enemyId,
            worldId = worldId,
            armor = equippedArmor,
            weapon = equippedWeapon,
            allies = friendAllies,
            helpedKidIds = helpedKidIds,
            itemCounts = itemCounts
        )
    }

    val state by battleViewModel.state.collectAsState()
    val hasActiveFx = state.slashFxTimerSec > 0f || state.isEnemyHitFlash

    LaunchedEffect(state.phase, hasActiveFx) {
        if (state.phase == BattlePhase.FIGHT_MINIGAME || state.phase == BattlePhase.BULLET_HELL || hasActiveFx) {
            var lastNanos = 0L
            while (true) {
                withFrameNanos { nanos ->
                    if (lastNanos != 0L) {
                        battleViewModel.tick((nanos - lastNanos) / 1_000_000_000f)
                    }
                    lastNanos = nanos
                }
            }
        }
    }

    LaunchedEffect(state.phase) {
        if (state.phase == BattlePhase.VICTORY) {
            val remainingMap = state.items.associate { it.id to it.count }
            onBattleFinished(state.enemy.id, state.battleWonSpared, remainingMap)
        }
    }

    // Authentic Undertale Landscape Battle Layout on stark pitch-black void
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // LEFT COLUMN: Undertale Enemy Stage, Retro Green Gridlines, Speech Bubble & Dodge Controls
        Column(
            modifier = Modifier
                .weight(0.88f)
                .fillMaxHeight()
                .background(Color.Black)
                .border(2.5.dp, Color.White, RoundedCornerShape(0.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onExitBattle, modifier = Modifier.testTag("battle_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Flee battle", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(state.enemy.name.uppercase(), style = MaterialTheme.typography.titleMedium, color = state.enemy.accentColor)
                    Text(
                        text = tr(
                            language,
                            "TURN ${state.turnNumber} · DEF +${state.effectiveDefenseBonus} · ATK +${state.attackBonus}",
                            "الدور ${state.turnNumber} · دفاع +${state.effectiveDefenseBonus} · هجوم +${state.attackBonus}"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB0B0B8)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = onToggleControls,
                        shape = RoundedCornerShape(0.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        border = BorderStroke(1.5.dp, SepiaAmber),
                        modifier = Modifier.height(32.dp).testTag("battle_controls_toggle")
                    ) {
                        Text(if (language == AppLanguage.AR) controlScheme.labelAr else controlScheme.label, style = MaterialTheme.typography.labelSmall, color = SepiaAmber)
                    }
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(0.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(language.buttonLabel, style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }

            // Classic Undertale Monster Bars (Green over Dark Red for Enemy HP, Yellow for Mercy)
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = tr(language, "ENEMY HP ${state.enemyHp}/${state.enemy.maxHp}", "دم العدو ${state.enemyHp}/${state.enemy.maxHp}"),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
                LinearProgressIndicator(
                    progress = { (state.enemyHp.toFloat() / state.enemy.maxHp).coerceIn(0f, 1f) },
                    color = HpBarGreen,
                    trackColor = UndertaleHpRed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                )
                Text(
                    text = tr(language, "MERCY ${state.mercyProgress}%", "الرحمة ${state.mercyProgress}%"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MercyYellow
                )
                LinearProgressIndicator(
                    progress = { (state.mercyProgress / 100f).coerceIn(0f, 1f) },
                    color = MercyYellow,
                    trackColor = Color(0xFF222228),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
            }

            // Undertale Encounter Grid Background + Pixel Boss Sprite
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
                    .background(Color(0xFF040705))
                    .border(1.5.dp, Color(0xFF1F4D2B), RoundedCornerShape(0.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Classic Undertale Dark-Green Perspective Battle Background Grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cols = 6
                    val rows = 4
                    val gridColor = Color(0xFF174A24)
                    for (c in 1 until cols) {
                        val x = size.width * (c / cols.toFloat())
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5f)
                    }
                    for (r in 1 until rows) {
                        val y = size.height * (r / rows.toFloat())
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
                    }
                }

                BossSpriteCanvas(
                    enemyId = state.enemy.id,
                    isHitFlash = state.isEnemyHitFlash,
                    isSpareReady = state.canSpare,
                    expression = state.bossExpression,
                    modifier = Modifier.size(124.dp)
                )

                // Undertale/Deltarune Weapon Slash Sweep FX + Damage Popup
                if (state.slashFxTimerSec > 0f && state.lastStrikeDamage != null) {
                    val progress = (1f - (state.slashFxTimerSec / 0.50f)).coerceIn(0f, 1f)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width * 0.5f
                        val cy = size.height * 0.5f
                        val span = minOf(size.width, size.height) * 0.36f
                        val sweepStart = Offset(cx - span, cy - span * 0.85f)
                        val sweepEnd = Offset(
                            cx - span + (span * 2f) * progress,
                            cy - span * 0.85f + (span * 1.7f) * progress
                        )
                        drawLine(
                            color = state.equippedWeapon.accentColor,
                            start = sweepStart,
                            end = sweepEnd,
                            strokeWidth = if (state.lastStrikeCritical) 9f else 5.5f
                        )
                        drawLine(
                            color = Color.White,
                            start = sweepStart,
                            end = sweepEnd,
                            strokeWidth = 2.5f
                        )
                        if (state.equippedWeapon.style == WeaponStyle.DUAL_BLADE || state.lastStrikeCritical) {
                            drawLine(
                                color = MercyYellow,
                                start = Offset(cx + span, cy - span * 0.75f),
                                end = Offset(cx + span - (span * 2f) * progress, cy - span * 0.75f + (span * 1.5f) * progress),
                                strokeWidth = 4f
                            )
                        }
                    }
                    Text(
                        text = if (state.lastStrikeCritical) {
                            "CRIT! -${state.lastStrikeDamage}"
                        } else {
                            "-${state.lastStrikeDamage}"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (state.lastStrikeCritical) MercyYellow else DangerRed,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp)
                    )
                }

                if (state.ashenHelpingThisTurn) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(0.dp))
                            .border(1.5.dp, Color(0xFFD4D4D8), RoundedCornerShape(0.dp))
                            .padding(4.dp)
                    ) {
                        AshenFlowerCanvas(
                            isCreepySmirk = false,
                            expression = PortraitExpression.HAPPY,
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = tr(language, "ASHEN HELPS!", "آشن يساعدك!"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD4D4D8)
                        )
                    }
                }

                if (state.isRewindingTime) {
                    Text(
                        text = "<< REWINDING <<",
                        style = MaterialTheme.typography.labelMedium,
                        color = GlitchCyan,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                    )
                }
            }

            // Mid-Dodge World Soul Switcher + Ability Button + Optional D-Pad
            if (state.phase == BattlePhase.BULLET_HELL) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        state.allowedSouls.forEach { soul ->
                            val active = soul == state.activeSoul
                            Button(
                                onClick = { battleViewModel.switchSoul(soul, duringBulletHell = true) },
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (active) soul.color else Color.Black,
                                    contentColor = if (active) Color.Black else soul.color
                                ),
                                border = BorderStroke(2.dp, soul.color),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("dodge_soul_${soul.name.lowercase()}")
                            ) {
                                Text(soul.title.replace(" Soul", "").uppercase(), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    val ready = state.abilityCooldownSec <= 0f
                    val abilityLabel = when {
                        !ready -> "${"%.1f".format(state.abilityCooldownSec)}s"
                        state.activeSoul == SoulType.SPLIT_DUO && !state.isSplitActive ->
                            tr(language, "[C] SPLIT SOUL", "[C] انقسام الروح")
                        state.activeSoul == SoulType.SPLIT_DUO && state.isSplitActive ->
                            tr(language, "[C] SWITCH HALF", "[C] انتقل للجزء الثاني")
                        else -> if (language == AppLanguage.AR) state.activeSoul.abilityNameAr else state.activeSoul.abilityName
                    }
                    Button(
                        onClick = battleViewModel::triggerSoulAbility,
                        enabled = ready,
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (ready) state.activeSoul.color else Color(0xFF16161B),
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(2.dp, if (ready) Color.White else Color(0xFF4B5563)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("battle_ability_button")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = abilityLabel.uppercase(), style = MaterialTheme.typography.labelSmall)
                    }

                    if (controlScheme != ControlScheme.TOUCH_DRAG) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            ControllerPadOverlay(
                                controlScheme = controlScheme,
                                accentColor = state.activeSoul.color,
                                padSize = 104.dp,
                                onDirectionChanged = battleViewModel::setDpadDirection
                            )
                        }
                    }
                }
            } else {
                val soulName = if (language == AppLanguage.AR) state.activeSoul.titleAr else state.activeSoul.title
                Text(
                    text = "${soulName.uppercase()} · ${state.worldId.title.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = state.activeSoul.color
                )
            }
        }

        // RIGHT COLUMN: Iconic Sharp White-Bordered Undertale Battle Box + Classic Yellow/Red HP HUD
        Column(
            modifier = Modifier
                .weight(1.42f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ArenaBlack, RoundedCornerShape(0.dp))
                    .border(
                        width = 3.5.dp,
                        color = if (state.phase == BattlePhase.BULLET_HELL) state.activeSoul.color else Color.White,
                        shape = RoundedCornerShape(0.dp)
                    )
            ) {
                when (state.phase) {
                    BattlePhase.PLAYER_MENU -> PlayerMenuInsideBox(
                        language = language,
                        state = state,
                        onSelectSubTab = battleViewModel::selectSubTab,
                        onSwitchSoul = { battleViewModel.switchSoul(it, duringBulletHell = false) },
                        onStartFight = battleViewModel::startFightMinigame,
                        onPerformAct = battleViewModel::performAct,
                        onPerformSpare = battleViewModel::performSpare,
                        onUseItem = battleViewModel::useItem,
                        onCallAlly = battleViewModel::callFriendAid,
                        onCallKidAlly = battleViewModel::callLostKidAid
                    )

                    BattlePhase.FIGHT_MINIGAME -> FightMinigameInsideBox(
                        language = language,
                        cursorPos = state.fightCursorPos,
                        soulColor = state.activeSoul.color,
                        weapon = state.equippedWeapon,
                        onStrike = battleViewModel::commitFightStrike
                    )

                    BattlePhase.ENEMY_DIALOGUE -> {
                        // Classic Undertale White Speech Bubble + Boss Portrait + Typewriter Voice Blips
                        val enemyVoicePitch = when (state.enemy.id) {
                            EnemyId.RUINS_DUMMY -> 280f
                            EnemyId.VEX -> 240f
                            EnemyId.BRAMBLE -> 185f
                            EnemyId.VESPERA -> 410f
                            EnemyId.ORIN -> 165f
                            EnemyId.SERAPHIEL -> 455f
                            EnemyId.MALAKOR -> 170f
                            EnemyId.ECHO_LOKA -> 215f
                            EnemyId.FUTURE_LOKA -> 295f
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .background(Color(0xFF0B0B0F), RoundedCornerShape(0.dp))
                                        .border(2.dp, state.enemy.accentColor, RoundedCornerShape(0.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BossSpriteCanvas(
                                        enemyId = state.enemy.id,
                                        isHitFlash = state.isEnemyHitFlash,
                                        isSpareReady = state.canSpare,
                                        expression = state.bossExpression,
                                        modifier = Modifier.size(58.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White, RoundedCornerShape(6.dp))
                                        .border(2.5.dp, Color(0xFF222228), RoundedCornerShape(6.dp))
                                        .padding(12.dp)
                                ) {
                                    TypewriterBattleText(
                                        fullText = state.dialogueText,
                                        voicePitchHz = enemyVoicePitch,
                                        textColor = Color.Black
                                    )
                                }
                            }
                            Button(
                                onClick = battleViewModel::advanceFromDialogueToDodging,
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = state.activeSoul.color, contentColor = Color.Black),
                                border = BorderStroke(2.dp, Color.White),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .height(48.dp)
                                    .testTag("battle_dodge_continue_button")
                            ) {
                                Text(tr(language, "DODGE!", "تفادَ الآن!"), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    BattlePhase.BULLET_HELL -> BulletHellArena(
                        state = state,
                        controlScheme = controlScheme,
                        onMoveByDelta = battleViewModel::moveSoulByDelta
                    )

                    BattlePhase.VICTORY, BattlePhase.DEFEAT -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when {
                                    state.phase == BattlePhase.DEFEAT -> tr(language, "SOUL SHATTERED", "تحطّمت الروح")
                                    state.battleWonSpared -> tr(language, "YOU WON! FRIENDSHIP FORGED!", "فزت! صداقة جديدة!")
                                    else -> tr(language, "YOU WON!", "انتصار!")
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                color = when {
                                    state.phase == BattlePhase.DEFEAT -> DangerRed
                                    state.battleWonSpared -> MercyYellow
                                    else -> SepiaGold
                                }
                            )
                            TypewriterBattleText(
                                fullText = "* ${state.dialogueText}",
                                voicePitchHz = if (state.battleWonSpared) 380f else 220f,
                                textColor = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (state.phase == BattlePhase.DEFEAT) {
                                    Button(
                                        onClick = {
                                            battleViewModel.initEncounter(
                                                enemyId = enemyId,
                                                worldId = worldId,
                                                armor = equippedArmor,
                                                weapon = equippedWeapon,
                                                allies = friendAllies,
                                                helpedKidIds = helpedKidIds,
                                                itemCounts = itemCounts
                                            )
                                        },
                                        shape = RoundedCornerShape(0.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = UndertaleOrange, contentColor = Color.Black)
                                    ) {
                                        Text(tr(language, "RETRY", "إعادة"), style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                                OutlinedButton(
                                    onClick = onExitBattle,
                                    shape = RoundedCornerShape(0.dp),
                                    border = BorderStroke(2.dp, Color.White),
                                    modifier = Modifier.testTag("battle_finish_exit_button")
                                ) {
                                    Text(tr(language, "CONTINUE", "متابعة"), style = MaterialTheme.typography.labelLarge, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Classic Undertale Player Status HUD: LOKA  LV 1  HP [Yellow over Red]  20 / 20
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOKA   LV 1   HP",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .background(UndertaleHpRed)
                        .border(1.5.dp, Color.White, RoundedCornerShape(0.dp))
                ) {
                    val ratio = (state.playerHp.toFloat() / state.maxPlayerHp).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(ratio)
                            .background(UndertaleHpYellow)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${state.playerHp} / ${state.maxPlayerHp}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.playerHp <= 6) DangerRed else Color.White
                )
            }
        }
    }
}

@Composable
private fun PlayerMenuInsideBox(
    language: AppLanguage,
    state: BattleUiState,
    onSelectSubTab: (MenuSubTab) -> Unit,
    onSwitchSoul: (SoulType) -> Unit,
    onStartFight: () -> Unit,
    onPerformAct: (String) -> Unit,
    onPerformSpare: () -> Unit,
    onUseItem: (String) -> Unit,
    onCallAlly: (EnemyId) -> Unit,
    onCallKidAlly: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            when (state.subTab) {
                MenuSubTab.ROOT -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .background(Color(0xFF0D0D12), RoundedCornerShape(0.dp))
                                .border(2.dp, if (state.ashenHelpingThisTurn) Color(0xFFD4D4D8) else state.activeSoul.color, RoundedCornerShape(0.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.ashenHelpingThisTurn) {
                                AshenFlowerCanvas(
                                    isCreepySmirk = false,
                                    expression = PortraitExpression.HAPPY,
                                    modifier = Modifier.size(48.dp)
                                )
                            } else {
                                LokaPortraitCanvas(
                                    saveCount = 0,
                                    isSickGlitched = state.worldId == WorldId.GLITCHED_WORLD,
                                    expression = state.speakerExpression,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                            TypewriterBattleText(
                                fullText = "* ${state.dialogueText}",
                                voicePitchHz = if (state.ashenHelpingThisTurn) 195f else 335f,
                                textColor = Color.White
                            )
                            Text(
                                text = tr(
                                    language,
                                    "* ${state.equippedWeapon.name} (+${state.equippedWeapon.attackBonus} ATK) · ${state.equippedArmorName}",
                                    "* ${state.equippedWeapon.nameAr} (+${state.equippedWeapon.attackBonus} هجوم) · ${state.equippedArmorName}"
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSepiaInk
                            )
                        }
                    }
                }

                MenuSubTab.SOUL_SELECT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = tr(language, "* WORLD SOULS (${state.worldId.title})", "* أرواح هذا العالم (${state.worldId.title})"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MercyYellow
                        )
                        state.allowedSouls.forEach { soul ->
                            val sName = if (language == AppLanguage.AR) soul.titleAr else soul.title
                            val sDesc = if (language == AppLanguage.AR) soul.abilityDescAr else soul.abilityDesc
                            OutlinedButton(
                                onClick = {
                                    onSwitchSoul(soul)
                                    onSelectSubTab(MenuSubTab.ROOT)
                                },
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(2.dp, soul.color),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("* $sName — $sDesc", style = MaterialTheme.typography.labelSmall, color = soul.secondaryColor ?: soul.color)
                            }
                        }
                    }
                }

                MenuSubTab.ACT_LIST -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.enemy.actOptions.forEach { act ->
                            OutlinedButton(
                                onClick = { onPerformAct(act.id) },
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.5.dp, Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("* ${act.title} (+${act.mercyGain}% ${tr(language, "Mercy", "رحمة")})", style = MaterialTheme.typography.labelMedium, color = Color.White)
                            }
                        }
                        if (state.enemy.id != EnemyId.FUTURE_LOKA) {
                            Button(
                                onClick = onPerformSpare,
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.canSpare) MercyYellow else Color(0xFF18181D),
                                    contentColor = if (state.canSpare) Color.Black else Color(0xFF9CA3AF)
                                ),
                                border = BorderStroke(2.dp, if (state.canSpare) Color.White else UndertaleOrange),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("battle_spare_button")
                            ) {
                                Text(
                                    text = if (state.canSpare) {
                                        tr(language, "* SPARE & BEFRIEND!", "* اعفُ وصادِقه!")
                                    } else {
                                        tr(language, "* SPARE (${state.mercyProgress}%/100%)", "* عفو (${state.mercyProgress}%/100%)")
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                MenuSubTab.ITEM_LIST -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val available = state.items.filter { it.count > 0 }
                        if (available.isEmpty()) {
                            Text(
                                text = tr(
                                    language,
                                    "* No items left! Explore Hills, Caves, Mansions & Labs for more.",
                                    "* لا توجد عناصر! استكشف التلال والكهوف والقصور والمختبرات."
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSepiaInk
                            )
                        } else {
                            available.forEach { item ->
                                OutlinedButton(
                                    onClick = { onUseItem(item.id) },
                                    shape = RoundedCornerShape(0.dp),
                                    border = BorderStroke(1.5.dp, SepiaAmber),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("* ${item.name} (x${item.count}) — ${item.description}", style = MaterialTheme.typography.labelSmall, color = ParchmentInk)
                                }
                            }
                        }
                    }
                }

                MenuSubTab.ALLY_LIST -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val helpedKids = WorldContentCatalog.lostKids.filter { it.id in state.helpedKids }
                        if (state.activeAllies.isEmpty() && helpedKids.isEmpty()) {
                            Text(
                                text = tr(
                                    language,
                                    "* No allies yet. Spare bosses via ACT or help Lost Kids!",
                                    "* لا يوجد حلفاء بعد. صادِق الزعماء عبر ACT أو ساعد الأطفال المفقودين!"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSepiaInk
                            )
                        } else {
                            helpedKids.forEach { kid ->
                                val kidDisplayName = if (language == AppLanguage.AR) kid.nameAr else kid.name
                                val kidBuff = if (language == AppLanguage.AR) kid.buffAr else kid.buffEn
                                val healVal = if (kid.healAmount > 0) kid.healAmount else 4
                                OutlinedButton(
                                    onClick = { onCallKidAlly(kid.id) },
                                    shape = RoundedCornerShape(0.dp),
                                    border = BorderStroke(1.5.dp, kid.accentColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ally_kid_${kid.id}")
                                ) {
                                    Text(
                                        text = tr(
                                            language,
                                            "* Call $kidDisplayName (+${healVal} HP) · $kidBuff",
                                            "* استدعِ $kidDisplayName (+$healVal دم) · $kidBuff"
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = kid.accentColor
                                    )
                                }
                            }
                            state.activeAllies.forEach { allyId ->
                                val ally = EnemyRoster.byId(allyId)
                                OutlinedButton(
                                    onClick = { onCallAlly(allyId) },
                                    shape = RoundedCornerShape(0.dp),
                                    border = BorderStroke(1.5.dp, HpBarGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = tr(language, "* Call ${ally.name} — ${ally.allyBonusDesc}", "* استدعِ ${ally.name} — ${ally.allyBonusDesc}"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HpBarGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Iconic Sharp-Cornered Undertale Action Buttons Row!
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ActionTabBtn(tr(language, "FIGHT", "هجوم"), UndertaleOrange, state.subTab == MenuSubTab.ROOT, onStartFight, Modifier.weight(1f).testTag("battle_fight_btn"))
            ActionTabBtn(tr(language, "SOUL", "الروح"), state.activeSoul.color, state.subTab == MenuSubTab.SOUL_SELECT, {
                onSelectSubTab(if (state.subTab == MenuSubTab.SOUL_SELECT) MenuSubTab.ROOT else MenuSubTab.SOUL_SELECT)
            }, Modifier.weight(1f).testTag("battle_soul_btn"))
            ActionTabBtn(tr(language, "ACT", "تصرّف"), UndertaleOrange, state.subTab == MenuSubTab.ACT_LIST, {
                onSelectSubTab(if (state.subTab == MenuSubTab.ACT_LIST) MenuSubTab.ROOT else MenuSubTab.ACT_LIST)
            }, Modifier.weight(1f).testTag("battle_act_btn"))
            ActionTabBtn(tr(language, "ITEM", "حقيبة"), UndertaleOrange, state.subTab == MenuSubTab.ITEM_LIST, {
                onSelectSubTab(if (state.subTab == MenuSubTab.ITEM_LIST) MenuSubTab.ROOT else MenuSubTab.ITEM_LIST)
            }, Modifier.weight(1f).testTag("battle_item_btn"))
            ActionTabBtn(tr(language, "ALLY", "حليف"), HpBarGreen, state.subTab == MenuSubTab.ALLY_LIST, {
                onSelectSubTab(if (state.subTab == MenuSubTab.ALLY_LIST) MenuSubTab.ROOT else MenuSubTab.ALLY_LIST)
            }, Modifier.weight(1f).testTag("battle_ally_btn"))
        }
    }
}

@Composable
private fun ActionTabBtn(
    label: String,
    accent: Color,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = if (highlighted) MercyYellow else accent
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(2.dp, activeColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (highlighted) activeColor.copy(alpha = 0.14f) else Color.Black
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = if (highlighted) "♥ $label" else label,
            style = MaterialTheme.typography.labelSmall,
            color = activeColor
        )
    }
}

@Composable
private fun TypewriterBattleText(
    fullText: String,
    voicePitchHz: Float,
    textColor: Color,
    textAlign: TextAlign = TextAlign.Start
) {
    var shownChars by remember(fullText) { mutableIntStateOf(0) }
    LaunchedEffect(fullText) {
        shownChars = 0
        for (i in 1..fullText.length) {
            delay(20L)
            shownChars = i
            if (i % 4 == 1 && fullText[i - 1] != ' ' && fullText[i - 1] != '*') {
                RetroSoundEngine.playTypeBlip(voicePitchHz)
            }
        }
    }
    Text(
        text = fullText.take(shownChars),
        style = MaterialTheme.typography.bodyLarge,
        color = textColor,
        textAlign = textAlign
    )
}

@Composable
private fun FightMinigameInsideBox(
    language: AppLanguage,
    cursorPos: Float,
    soulColor: Color,
    weapon: WeaponEquipment = WeaponCatalog.byId("worn_stick"),
    onStrike: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(tr(language, "TAP STRIKE AT CENTER!", "اضغط اضرب عند المنتصف!"), style = MaterialTheme.typography.titleMedium, color = MercyYellow)
            Text(
                text = if (language == AppLanguage.AR) "${weapon.nameAr} · ${weapon.descriptionAr}" else "${weapon.name} · ${weapon.descriptionEn}",
                style = MaterialTheme.typography.labelSmall,
                color = weapon.accentColor
            )
        }

        // Undertale / Deltarune Weapon-Styled Target Eye Reticle!
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp)
        ) {
            drawRect(color = Color.Black)

            val h = size.height
            val w = size.width
            val midY = h * 0.5f

            val topArc = Path().apply {
                moveTo(w * 0.04f, midY)
                quadraticTo(w * 0.50f, -h * 0.10f, w * 0.96f, midY)
            }
            val bottomArc = Path().apply {
                moveTo(w * 0.04f, midY)
                quadraticTo(w * 0.50f, h * 1.10f, w * 0.96f, midY)
            }
            val eyeOutlineColor = when (weapon.style) {
                WeaponStyle.WRENCH_SPARK -> weapon.accentColor
                WeaponStyle.DUAL_BLADE -> MercyYellow
                WeaponStyle.CHRONO_ARC -> Color(0xFFD946EF)
                WeaponStyle.SINGLE_BAR -> Color.White
            }
            drawPath(topArc, color = eyeOutlineColor, style = Stroke(3f))
            drawPath(bottomArc, color = eyeOutlineColor, style = Stroke(3f))

            if (weapon.style == WeaponStyle.DUAL_BLADE) {
                // Deltarune-style dual strike lane bands
                drawLine(weapon.accentColor.copy(alpha = 0.55f), Offset(w * 0.06f, midY - 12f), Offset(w * 0.94f, midY - 12f), strokeWidth = 2f)
                drawLine(weapon.accentColor.copy(alpha = 0.55f), Offset(w * 0.06f, midY + 12f), Offset(w * 0.94f, midY + 12f), strokeWidth = 2f)
            }

            // Vertical accuracy gradient ribs inside the eye
            val tickPositions = listOf(0.14f, 0.24f, 0.34f, 0.43f, 0.57f, 0.66f, 0.76f, 0.86f)
            tickPositions.forEach { t ->
                val distFromCenter = kotlin.math.abs(t - 0.5f)
                val halfSpan = (0.48f - distFromCenter * 0.75f).coerceAtLeast(0.12f) * h
                val tickColor = if (distFromCenter < 0.12f + weapon.critWindowBonus) Color(0xFF4ADE80) else Color(0xFF6B7280)
                drawLine(tickColor, Offset(w * t, midY - halfSpan), Offset(w * t, midY + halfSpan), strokeWidth = 2.5f)
            }

            // Central golden critical column (widens with higher weapon critWindowBonus)
            val critHalfW = (0.06f + weapon.critWindowBonus * 0.5f)
            drawRect(
                color = MercyYellow.copy(alpha = 0.28f),
                topLeft = Offset(w * (0.5f - critHalfW), h * 0.08f),
                size = Size(w * critHalfW * 2f, h * 0.84f)
            )
            drawLine(
                color = Color.White,
                start = Offset(w * 0.5f, h * 0.06f),
                end = Offset(w * 0.5f, h * 0.94f),
                strokeWidth = 3f
            )

            // Moving Weapon-Styled Cursor
            val cx = cursorPos * w
            val barColor = if (weapon.style == WeaponStyle.SINGLE_BAR) soulColor else weapon.accentColor
            when (weapon.style) {
                WeaponStyle.DUAL_BLADE -> {
                    for (dx in listOf(-6.dp.toPx(), 6.dp.toPx())) {
                        drawRect(Color.Black, Offset(cx + dx - 4.dp.toPx(), 6.dp.toPx()), Size(8.dp.toPx(), h - 12.dp.toPx()))
                        drawRect(barColor, Offset(cx + dx - 2.5.dp.toPx(), 8.dp.toPx()), Size(5.dp.toPx(), h - 16.dp.toPx()))
                        drawRect(Color.White, Offset(cx + dx - 4.dp.toPx(), 6.dp.toPx()), Size(8.dp.toPx(), h - 12.dp.toPx()), style = Stroke(1.8f))
                    }
                }
                WeaponStyle.CHRONO_ARC -> {
                    drawRect(Color.Black, Offset(cx - 8.dp.toPx(), 4.dp.toPx()), Size(16.dp.toPx(), h - 8.dp.toPx()))
                    drawRect(barColor, Offset(cx - 5.dp.toPx(), 6.dp.toPx()), Size(10.dp.toPx(), h - 12.dp.toPx()))
                    drawPixelSaveStar(Offset(cx, midY), 10.dp.toPx(), MercyYellow, Color.White)
                }
                else -> {
                    drawRect(Color.Black, Offset(cx - 7.dp.toPx(), 4.dp.toPx()), Size(14.dp.toPx(), h - 8.dp.toPx()))
                    drawRect(barColor, Offset(cx - 5.dp.toPx(), 6.dp.toPx()), Size(10.dp.toPx(), h - 12.dp.toPx()))
                    drawRect(Color.White, Offset(cx - 7.dp.toPx(), 4.dp.toPx()), Size(14.dp.toPx(), h - 8.dp.toPx()), style = Stroke(2f))
                }
            }
        }

        Button(
            onClick = onStrike,
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = weapon.accentColor, contentColor = Color.Black),
            border = BorderStroke(2.dp, Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("battle_commit_strike_button")
        ) {
            Text(tr(language, "STRIKE!", "اضرب!"), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun BulletHellArena(
    state: BattleUiState,
    controlScheme: ControlScheme,
    onMoveByDelta: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(controlScheme) {
                if (controlScheme == ControlScheme.TOUCH_DRAG) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dx = dragAmount.x / size.width.toFloat()
                        val dy = dragAmount.y / size.height.toFloat()
                        onMoveByDelta(dx, dy)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Attack #1 (Image 2 Top-Left): Vertical Rectangular Lane Telegraphs with Up/Down Arrows
            state.laneTelegraphs.forEach { lane ->
                val centerX = lane.xNorm * size.width
                val laneW = lane.widthNorm * size.width
                val left = centerX - laneW * 0.5f
                if (lane.isFiring) {
                    drawRect(lane.color, Offset(left, 0f), Size(laneW, size.height))
                    drawRect(Color.White, Offset(centerX - laneW * 0.24f, 0f), Size(laneW * 0.48f, size.height))
                } else {
                    drawRect(lane.color.copy(alpha = 0.16f), Offset(left, 0f), Size(laneW, size.height))
                    drawRect(Color.White, Offset(left, 0f), Size(laneW, size.height), style = Stroke(2f))
                    val dirY = if (lane.isUpward) -1f else 1f
                    val startY = if (lane.isUpward) size.height * 0.72f else size.height * 0.28f
                    drawDirectionalArrow(
                        origin = Offset(centerX, startY),
                        dirX = 0f,
                        dirY = dirY,
                        lengthPx = size.height * 0.44f,
                        color = lane.color
                    )
                }
            }

            // Laser beams
            state.lasers.forEach { laser ->
                val centerCoord = laser.positionNorm * (if (laser.isHorizontal) size.height else size.width)
                val halfThick = laser.thicknessNorm * (if (laser.isHorizontal) size.height else size.width) * 0.5f
                if (laser.isFiring) {
                    if (laser.isHorizontal) {
                        drawRect(laser.color, Offset(0f, centerCoord - halfThick), Size(size.width, halfThick * 2f))
                        drawRect(Color.White, Offset(0f, centerCoord - halfThick * 0.45f), Size(size.width, halfThick * 0.9f))
                    } else {
                        drawRect(laser.color, Offset(centerCoord - halfThick, 0f), Size(halfThick * 2f, size.height))
                        drawRect(Color.White, Offset(centerCoord - halfThick * 0.45f, 0f), Size(halfThick * 0.9f, size.height))
                    }
                } else {
                    if (laser.isHorizontal) {
                        drawRect(laser.color.copy(alpha = 0.22f), Offset(0f, centerCoord - halfThick), Size(size.width, halfThick * 2f), style = Stroke(2f))
                        drawLine(Color.Red, Offset(0f, centerCoord), Offset(size.width, centerCoord), strokeWidth = 2.5f)
                    } else {
                        drawRect(laser.color.copy(alpha = 0.22f), Offset(centerCoord - halfThick, 0f), Size(halfThick * 2f, size.height), style = Stroke(2f))
                        drawLine(Color.Red, Offset(centerCoord, 0f), Offset(centerCoord, size.height), strokeWidth = 2.5f)
                    }
                }
            }

            // Attack #2 (Image 2 Top-Right): Boss Chomp Head + Claw Strike
            state.jawAttacks.forEach { jaw ->
                val jc = Offset(jaw.xNorm * size.width, jaw.yNorm * size.height)
                val cc = Offset((jaw.xNorm + jaw.clawOffsetX) * size.width, jaw.yNorm * size.height)
                drawBossChompHeadAndClaw(
                    enemyId = state.enemy.id,
                    jawCenter = jc,
                    clawCenter = cc,
                    sizePx = jaw.radiusNorm * minOf(size.width, size.height) * 1.65f,
                    isChomping = jaw.isChomping,
                    accentColor = jaw.color
                )
            }

            // Attack #3 (Image 2 Middle-Left): Crossed Directional Arrows (replaced by bullets when ready!)
            state.arrowTelegraphs.forEach { arw ->
                drawDirectionalArrow(
                    origin = Offset(arw.originX * size.width, arw.originY * size.height),
                    dirX = arw.dirX,
                    dirY = arw.dirY,
                    lengthPx = minOf(size.width, size.height) * 0.40f,
                    color = arw.color
                )
            }

            // Attack #4 (Image 2 Middle-Right): Fuse Bomb
            state.bombs.forEach { bomb ->
                drawPixelBomb(
                    center = Offset(bomb.x * size.width, bomb.y * size.height),
                    radiusPx = 14.dp.toPx(),
                    fuseProgress = (bomb.elapsedSec / bomb.fuseSec).coerceIn(0f, 1f),
                    glowColor = bomb.color
                )
            }

            // Attack #5 (Image 2 Bottom-Right): Side Wall "! <-" / "-> !" Shot Warnings
            state.sideWarnings.forEach { sw ->
                val edgeX = if (sw.fromRightEdge) size.width - 14.dp.toPx() else 14.dp.toPx()
                val cy = sw.yNorm * size.height
                val dir = if (sw.fromRightEdge) -1f else 1f
                // Exclamation "!" bar + dot + pointer arrow
                drawRect(Color.Red, Offset(edgeX - 2.dp.toPx(), cy - 11.dp.toPx()), Size(4.dp.toPx(), 13.dp.toPx()))
                drawRect(Color.Red, Offset(edgeX - 2.dp.toPx(), cy + 5.dp.toPx()), Size(4.dp.toPx(), 4.dp.toPx()))
                drawDirectionalArrow(
                    origin = Offset(edgeX + dir * 10.dp.toPx(), cy),
                    dirX = dir,
                    dirY = 0f,
                    lengthPx = 24.dp.toPx(),
                    color = sw.color
                )
            }

            // Crisp Pixel/Diamond Bullet Shapes
            state.bullets.forEach { b ->
                val cx = b.x * size.width
                val cy = b.y * size.height
                val r = b.radiusNorm * minOf(size.width, size.height)
                when (b.kind) {
                    BulletKind.PLAYER_SHOT -> {
                        drawRect(Color.White, Offset(cx - r * 0.5f, cy - r * 1.4f), Size(r, r * 2.8f))
                        drawRect(b.color, Offset(cx - r * 0.35f, cy - r * 1.1f), Size(r * 0.7f, r * 2.2f))
                    }
                    BulletKind.LANCE -> {
                        val diamond = Path().apply {
                            moveTo(cx, cy - r * 1.4f)
                            lineTo(cx + r * 0.9f, cy)
                            lineTo(cx, cy + r * 1.4f)
                            lineTo(cx - r * 0.9f, cy)
                            close()
                        }
                        drawPath(diamond, Color.White)
                        drawPath(diamond, b.color, style = Stroke(3f))
                    }
                    BulletKind.TIME_SHARD -> {
                        drawPixelSaveStar(Offset(cx, cy), r * 1.1f, b.color, Color.White)
                    }
                    else -> {
                        drawRect(Color.White, Offset(cx - r * 0.75f, cy - r * 0.75f), Size(r * 1.5f, r * 1.5f))
                        drawRect(b.color, Offset(cx - r * 0.75f, cy - r * 0.75f), Size(r * 1.5f, r * 1.5f), style = Stroke(2.2f))
                    }
                }
            }

            state.particles.forEach { p ->
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(p.x * size.width - p.sizeDp.dp.toPx() * 0.5f, p.y * size.height - p.sizeDp.dp.toPx() * 0.5f),
                    size = Size(p.sizeDp.dp.toPx(), p.sizeDp.dp.toPx())
                )
            }

            if (state.activeSoul == SoulType.RED) {
                val ex = state.redEchoX * size.width
                val ey = state.redEchoY * size.height
                drawPixelHeart(Offset(ex, ey), 9.dp.toPx(), SoulType.RED.color.copy(alpha = 0.28f), SoulType.RED.color.copy(alpha = 0.75f))
            }

            if (state.activeSoul == SoulType.BLUE) {
                drawCircle(
                    color = SoulType.BLUE.color.copy(alpha = 0.22f),
                    radius = minOf(size.width, size.height) * 0.20f,
                    center = Offset(state.soulX * size.width, state.soulY * size.height),
                    style = Stroke(2f)
                )
            }

            if (state.hasGreenShield) {
                val rad = Math.toRadians(state.greenOrbitAngle.toDouble())
                val orbR = 22.dp.toPx()
                val ox = state.soulX * size.width + (kotlin.math.cos(rad) * orbR).toFloat()
                val oy = state.soulY * size.height + (kotlin.math.sin(rad) * orbR).toFloat()
                drawRect(SoulType.GREEN.color, Offset(ox - 5.dp.toPx(), oy - 5.dp.toPx()), Size(10.dp.toPx(), 10.dp.toPx()))
            }

            val isPhasing = state.purpleMirageTimerSec > 0f
            val blinkAlpha = if (isPhasing) 0.45f else if (((state.invulnTimerSec * 18).toInt() % 2) == 0) 0.35f else 1f

            // Render stationary Split Half when SPLIT_DUO is split apart!
            if (state.activeSoul == SoulType.SPLIT_DUO && state.isSplitActive) {
                val otherHalfIdx = 1 - state.controlledHalfIndex
                val otherCenter = Offset(state.secondHalfX * size.width, state.secondHalfY * size.height)
                val otherBoosted = (otherHalfIdx == 0 && state.splitBoostTimer0Sec > 0f) ||
                    (otherHalfIdx == 1 && state.splitBoostTimer1Sec > 0f)
                // Sightline tether showing you can aim & press [C] to swap!
                drawLine(
                    color = Color.White.copy(alpha = 0.28f),
                    start = Offset(state.soulX * size.width, state.soulY * size.height),
                    end = otherCenter,
                    strokeWidth = 2f
                )
                if (otherBoosted) {
                    drawCircle(color = MercyYellow.copy(alpha = 0.45f), radius = 16.dp.toPx(), center = otherCenter, style = Stroke(2.5f))
                }
                drawSoulEmblem(
                    center = otherCenter,
                    sizePx = 11.dp.toPx(),
                    soul = state.activeSoul,
                    isInvulnerable = state.invulnTimerSec > 0f,
                    blinkAlpha = blinkAlpha,
                    hasGreenShield = false,
                    splitHalfIndex = otherHalfIdx
                )
            }

            if (state.activeSplitBoosted) {
                drawCircle(
                    color = MercyYellow.copy(alpha = 0.55f),
                    radius = 18.dp.toPx(),
                    center = Offset(state.soulX * size.width, state.soulY * size.height),
                    style = Stroke(2.5f)
                )
            }

            drawSoulEmblem(
                center = Offset(state.soulX * size.width, state.soulY * size.height),
                sizePx = 11.dp.toPx(),
                soul = state.activeSoul,
                isInvulnerable = state.invulnTimerSec > 0f || isPhasing,
                blinkAlpha = blinkAlpha,
                hasGreenShield = state.hasGreenShield,
                splitHalfIndex = if (state.activeSoul == SoulType.SPLIT_DUO && state.isSplitActive) state.controlledHalfIndex else null
            )
        }
    }
}
