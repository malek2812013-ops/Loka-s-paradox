package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import com.example.model.GameDifficulty
import com.example.model.MenuSubTab
import com.example.model.PortraitExpression
import com.example.model.RelicCatalog
import com.example.model.RelicEquipment
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
import com.example.ui.components.UltraPixelArtEngine
import com.example.ui.components.drawBossChompHeadAndClaw
import com.example.ui.components.drawDirectionalArrow
import com.example.ui.components.drawPixelBomb
import com.example.ui.components.drawPixelHeart
import com.example.ui.components.drawPixelSaveStar
import com.example.ui.components.drawSoulEmblem
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
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

private val UndertaleOrange = Color(0xFFFF8C1A)
private val UndertaleHpYellow = Color(0xFFFFFF00)
private val UndertaleHpRed = Color(0xFFC80000)

@Composable
fun BattleScreen(
    language: AppLanguage,
    battleSessionId: Long = 1L,
    enemyId: EnemyId,
    worldId: WorldId,
    equippedArmor: ArmorEquipment,
    equippedWeapon: WeaponEquipment = WeaponCatalog.byId("worn_stick"),
    equippedRelic: RelicEquipment = RelicCatalog.byId("golden_locket"),
    difficulty: GameDifficulty = GameDifficulty.NORMAL,
    reduceFlash: Boolean = false,
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

    LaunchedEffect(battleSessionId, enemyId, worldId, helpedKidIds, equippedWeapon.id, equippedArmor.id, equippedRelic.id, difficulty) {
        battleViewModel.initEncounter(
            encounterId = battleSessionId,
            enemyId = enemyId,
            worldId = worldId,
            armor = equippedArmor,
            weapon = equippedWeapon,
            relic = equippedRelic,
            difficulty = difficulty,
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

    LaunchedEffect(state.encounterId, state.phase) {
        if (state.encounterId == battleSessionId && state.phase == BattlePhase.VICTORY) {
            val remainingMap = state.items.associate { it.id to it.count }
            onBattleFinished(state.enemy.id, state.battleWonSpared, remainingMap)
        }
    }

    // CLASSIC AUTHENTIC UNDERTALE BATTLE SCREEN (640x480 4:3 Vibe, Pure Black Ground, White Box, 4 Orange Commands)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // 1. TOP UNDERTALE ENCOUNTER HEADER (Clear Boss vs Monster Badge + Stats + Controls)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onExitBattle,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("battle_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Flee battle", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                val (typeLabel, typeColor) = when {
                    state.enemy.id == EnemyId.ECHO_LOKA || state.enemy.id == EnemyId.FUTURE_LOKA ->
                        tr(language, "★ FINAL BOSS", "★ زعيم نهائي") to GlitchCyan
                    state.enemy.id == EnemyId.GOV_MONSTER_CHIEF ->
                        tr(language, "★ GOV BOSS (FRIEND)", "★ زعيم الحكومة (صديق)") to MercyYellow
                    state.enemy.id == EnemyId.GOV_DIRECTOR ->
                        tr(language, "★ GOV LEADER", "★ رئيس الحكومة") to GlitchCyan
                    !state.enemy.isNormalMonster ->
                        tr(language, "★ BOSS", "★ زعيم") to MercyYellow
                    state.enemy.canBefriend ->
                        tr(language, "MONSTER", "وحش ودود") to HpBarGreen
                    else ->
                        tr(language, "HOSTILE", "وحش عدائي") to DangerRed
                }
                Box(
                    modifier = Modifier
                        .background(Color.Black)
                        .border(1.5.dp, typeColor, RoundedCornerShape(0.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = typeLabel, style = MaterialTheme.typography.labelSmall, color = typeColor)
                }
                Column {
                    Text(
                        text = "${state.enemy.name.uppercase()} · ${tr(language, "TURN ${state.turnNumber}", "دور ${state.turnNumber}")}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${if (language == AppLanguage.AR) state.activeSoul.titleAr else state.activeSoul.title} · ATK +${state.attackBonus} · DEF +${state.effectiveDefenseBonus}",
                        style = MaterialTheme.typography.labelSmall,
                        color = state.activeSoul.color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = onToggleControls,
                    shape = RoundedCornerShape(0.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    border = BorderStroke(1.5.dp, SepiaAmber),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("battle_controls_toggle")
                ) {
                    Text(if (language == AppLanguage.AR) controlScheme.labelAr else controlScheme.label, style = MaterialTheme.typography.labelSmall, color = SepiaAmber)
                }
                OutlinedButton(
                    onClick = onToggleLanguage,
                    shape = RoundedCornerShape(0.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    border = BorderStroke(1.5.dp, Color.White),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(language.buttonLabel, style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }

        // 2. MAIN STAGE + WHITE-BORDERED UNDERTALE BOX
        if (state.phase == BattlePhase.BULLET_HELL) {
            // Spacious Bullet Hell Stage: Soul Controls (Left) | Wide White-Bordered Undertale Dodge Box (Center) | Enemy Info + D-Pad (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Panel: World Soul Switcher + [C] Ability Button
                Column(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight()
                        .background(Color.Black)
                        .border(2.dp, Color.White, RoundedCornerShape(0.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tr(language, "* DODGE!", "* تفادَ الطلقات!"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MercyYellow
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        state.allowedSouls.forEach { soul ->
                            val active = soul == state.activeSoul
                            Button(
                                onClick = { battleViewModel.switchSoul(soul, duringBulletHell = true) },
                                shape = RoundedCornerShape(0.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (active) soul.color else Color.Black,
                                    contentColor = if (active) Color.Black else soul.color
                                ),
                                border = BorderStroke(1.5.dp, soul.color),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .testTag("dodge_soul_${soul.name.lowercase()}")
                            ) {
                                Text(
                                    text = if (language == AppLanguage.AR) soul.titleAr else soul.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    val ready = state.abilityCooldownSec <= 0f
                    val abilityLabel = when {
                        !ready -> "${"%.1f".format(state.abilityCooldownSec)}s"
                        state.activeSoul == SoulType.SPLIT_DUO && !state.isSplitActive ->
                            tr(language, "[C] SPLIT", "[C] انقسام")
                        state.activeSoul == SoulType.SPLIT_DUO && state.isSplitActive ->
                            tr(language, "[C] SWAP", "[C] تبديل")
                        else -> if (language == AppLanguage.AR) state.activeSoul.abilityNameAr else state.activeSoul.abilityName
                    }
                    Button(
                        onClick = battleViewModel::triggerSoulAbility,
                        enabled = ready,
                        shape = RoundedCornerShape(0.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (ready) state.activeSoul.color else Color(0xFF16161B),
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(2.dp, if (ready) Color.White else Color(0xFF4B5563)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("battle_ability_button")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(text = abilityLabel.uppercase(), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }

                // Center: Classic Wide Undertale Solid-White Bordered Bullet Arena
                Box(
                    modifier = Modifier
                        .weight(1.38f)
                        .fillMaxHeight()
                        .background(Color.Black, RoundedCornerShape(0.dp))
                        .border(4.dp, Color.White, RoundedCornerShape(0.dp))
                ) {
                    BulletHellArena(
                        state = state,
                        controlScheme = controlScheme,
                        onMoveByDelta = battleViewModel::moveSoulByDelta
                    )
                    if (state.isRewindingTime) {
                        Text(
                            text = "<< REWINDING <<",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlitchCyan,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 4.dp)
                        )
                    }
                }

                // Right Panel: Monochrome Enemy Preview + HP Bar + Optional Controller Pad
                Column(
                    modifier = Modifier
                        .weight(0.64f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BossSpriteCanvas(
                        enemyId = state.enemy.id,
                        isHitFlash = !reduceFlash && state.isEnemyHitFlash,
                        isSpareReady = state.canSpare,
                        expression = state.bossExpression,
                        monochromeBattle = true,
                        modifier = Modifier.size(if (controlScheme == ControlScheme.TOUCH_DRAG) 118.dp else 72.dp)
                    )
                    UndertaleEnemyCompactStatus(language = language, state = state)
                    if (controlScheme != ControlScheme.TOUCH_DRAG) {
                        ControllerPadOverlay(
                            controlScheme = controlScheme,
                            accentColor = state.activeSoul.color,
                            padSize = 98.dp,
                            onDirectionChanged = battleViewModel::setDpadDirection
                        )
                    }
                }
            }
        } else {
            // NON-DODGE CLASSIC UNDERTALE VIEW: Centered 512x512 Monochrome Boss on 640x480 Encounter Grid + Spacious White Box Below
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.02f)
                    .background(Color.Black)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val bgBitmap = UltraPixelArtEngine.getUndertaleBattleBgBitmap()
                    drawImage(
                        image = bgBitmap,
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.roundToInt().coerceAtLeast(1), size.height.roundToInt().coerceAtLeast(1)),
                        filterQuality = FilterQuality.None
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Optional Left Slot: Ashen Flower Helper if active + Enemy HP/Mercy Meter
                    Column(
                        modifier = Modifier
                            .weight(0.72f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ) {
                        UndertaleEnemyCompactStatus(
                            language = language,
                            state = state,
                            modifier = Modifier.width(156.dp)
                        )
                        if (state.ashenHelpingThisTurn) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .background(Color.Black)
                                    .border(1.5.dp, Color.White, RoundedCornerShape(0.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                AshenFlowerCanvas(
                                    isCreepySmirk = false,
                                    expression = PortraitExpression.HAPPY,
                                    modifier = Modifier.size(34.dp)
                                )
                                Text(
                                    text = tr(language, "* Ashen is helping!", "* آشن يساعدك!"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MercyYellow
                                )
                            }
                        }
                    }

                    // Centerpiece: Large 512x512 Monochrome White-on-Black Undertale Opponent Sprite + Slash FX
                    Box(
                        modifier = Modifier
                            .size(164.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BossSpriteCanvas(
                            enemyId = state.enemy.id,
                            isHitFlash = !reduceFlash && state.isEnemyHitFlash,
                            isSpareReady = state.canSpare,
                            expression = state.bossExpression,
                            monochromeBattle = true,
                            modifier = Modifier.size(152.dp)
                        )
                        if (state.slashFxTimerSec > 0f && state.lastStrikeDamage != null) {
                            val progress = (1f - (state.slashFxTimerSec / 0.50f)).coerceIn(0f, 1f)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width * 0.5f
                                val cy = size.height * 0.5f
                                val span = minOf(size.width, size.height) * 0.44f
                                val sweepStart = Offset(cx - span, cy - span * 0.85f)
                                val sweepEnd = Offset(
                                    cx - span + (span * 2f) * progress,
                                    cy - span * 0.85f + (span * 1.7f) * progress
                                )
                                drawLine(
                                    color = if (state.lastStrikeCritical) MercyYellow else state.equippedWeapon.accentColor,
                                    start = sweepStart,
                                    end = sweepEnd,
                                    strokeWidth = if (state.lastStrikeCritical) 9f else 5.5f
                                )
                                drawLine(Color.White, sweepStart, sweepEnd, strokeWidth = 2.5f)
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
                                text = if (state.lastStrikeCritical) "CRIT! -${state.lastStrikeDamage}" else "-${state.lastStrikeDamage}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (state.lastStrikeCritical) MercyYellow else DangerRed,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .background(Color.Black)
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }

                    // Right Slot: Classic White Undertale Monster Speech Bubble during Enemy Dialogue
                    Box(
                        modifier = Modifier
                            .weight(0.95f)
                            .fillMaxHeight()
                            .padding(start = 10.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (state.phase == BattlePhase.ENEMY_DIALOGUE) {
                            val enemyVoicePitch = when (state.enemy.id) {
                                EnemyId.RUINS_DUMMY -> 280f
                                EnemyId.NORMAL_SLIME -> 360f
                                EnemyId.FERAL_HOUND -> 150f
                                EnemyId.SAND_MOTH -> 440f
                                EnemyId.IRON_STALKER -> 140f
                                EnemyId.HALO_SCRIBE -> 480f
                                EnemyId.ASH_IMP -> 320f
                                EnemyId.VEX -> 240f
                                EnemyId.BRAMBLE -> 185f
                                EnemyId.VESPERA -> 410f
                                EnemyId.ORIN -> 165f
                                EnemyId.SERAPHIEL -> 455f
                                EnemyId.MALAKOR -> 170f
                                EnemyId.ECHO_LOKA -> 215f
                                EnemyId.FUTURE_LOKA -> 295f
                                EnemyId.GOV_MONSTER_CHIEF -> 155f
                                EnemyId.GOV_DIRECTOR -> 190f
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.86f)
                                    .background(Color.White, RoundedCornerShape(4.dp))
                                    .border(2.5.dp, Color(0xFF1F2937), RoundedCornerShape(4.dp))
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                TypewriterBattleText(
                                    fullText = state.dialogueText,
                                    voicePitchHz = enemyVoicePitch,
                                    textColor = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // Spacious Classic Undertale White-Bordered Battle Text / Fight Eye Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.78f)
                    .background(Color.Black)
                    .border(3.5.dp, Color.White, RoundedCornerShape(0.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                when (state.phase) {
                    BattlePhase.PLAYER_MENU -> PlayerMenuInsideBox(
                        language = language,
                        state = state,
                        onSelectSubTab = battleViewModel::selectSubTab,
                        onSwitchSoul = { battleViewModel.switchSoul(it, duringBulletHell = false) },
                        onPerformAct = battleViewModel::performAct,
                        onPerformSpare = battleViewModel::performSpare,
                        onFleeBattle = onExitBattle,
                        onUseItem = battleViewModel::useItem,
                        onCallAlly = battleViewModel::callFriendAid,
                        onCallKidAlly = battleViewModel::callLostKidAid
                    )

                    BattlePhase.FIGHT_MINIGAME -> UndertaleFightEyeBarInsideBox(
                        language = language,
                        cursorPos = state.fightCursorPos,
                        soulColor = state.activeSoul.color,
                        weapon = state.equippedWeapon,
                        onStrike = battleViewModel::commitFightStrike
                    )

                    BattlePhase.ENEMY_DIALOGUE -> Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "* ${state.dialogueText}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = battleViewModel::advanceFromDialogueToDodging,
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = state.activeSoul.color, contentColor = Color.Black),
                            border = BorderStroke(2.dp, Color.White),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("battle_dodge_continue_button")
                        ) {
                            Text(tr(language, "DODGE!", "تفادَ الآن!"), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    BattlePhase.VICTORY, BattlePhase.DEFEAT -> Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = when {
                                    state.phase == BattlePhase.DEFEAT -> tr(language, "SOUL SHATTERED", "تحطّمت الروح")
                                    state.battleWonSpared && !state.enemy.canBefriend -> tr(language, "YOU WON! STOPPED WITHOUT KILLING!", "فزت! تم إيقافه بدون قتل!")
                                    state.battleWonSpared -> tr(language, "YOU WON! FRIENDSHIP FORGED!", "فزت! صداقة جديدة!")
                                    else -> tr(language, "YOU WON!", "انتصار!")
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = when {
                                    state.phase == BattlePhase.DEFEAT -> DangerRed
                                    state.battleWonSpared && !state.enemy.canBefriend -> GlitchCyan
                                    state.battleWonSpared -> MercyYellow
                                    else -> SepiaGold
                                }
                            )
                            Text(text = "* ${state.dialogueText}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.phase == BattlePhase.DEFEAT) {
                                Button(
                                    onClick = {
                                        battleViewModel.initEncounter(
                                            encounterId = battleSessionId,
                                            enemyId = enemyId,
                                            worldId = worldId,
                                            armor = equippedArmor,
                                            weapon = equippedWeapon,
                                            relic = equippedRelic,
                                            difficulty = difficulty,
                                            allies = friendAllies,
                                            helpedKidIds = helpedKidIds,
                                            itemCounts = itemCounts
                                        )
                                    },
                                    shape = RoundedCornerShape(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = UndertaleOrange, contentColor = Color.Black),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text(tr(language, "RETRY", "إعادة"), style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            OutlinedButton(
                                onClick = onExitBattle,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(2.dp, Color.White),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("battle_finish_exit_button")
                            ) {
                                Text(tr(language, "CONTINUE", "متابعة"), style = MaterialTheme.typography.labelLarge, color = Color.White)
                            }
                        }
                    }

                    BattlePhase.BULLET_HELL -> Unit
                }
            }
        }

        // 3. CLASSIC UNDERTALE BOTTOM BAR: LOKA   LV 1   HP [YELLOW/RED BAR]   20 / 20  +  SOUL / ALLY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("LOKA", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text("LV 1", style = MaterialTheme.typography.labelMedium, color = Color.White)
                Text("HP", style = MaterialTheme.typography.labelSmall, color = Color.White)
                Box(
                    modifier = Modifier
                        .width(128.dp)
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
                Text(
                    text = "${state.playerHp} / ${state.maxPlayerHp}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state.playerHp <= 6) DangerRed else Color.White
                )
            }

            if (state.phase == BattlePhase.PLAYER_MENU) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            battleViewModel.selectSubTab(if (state.subTab == MenuSubTab.SOUL_SELECT) MenuSubTab.ROOT else MenuSubTab.SOUL_SELECT)
                        },
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.5.dp, state.activeSoul.color),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("battle_soul_btn")
                    ) {
                        Text(tr(language, "SOUL", "الروح"), style = MaterialTheme.typography.labelSmall, color = state.activeSoul.color)
                    }
                    OutlinedButton(
                        onClick = {
                            battleViewModel.selectSubTab(if (state.subTab == MenuSubTab.ALLY_LIST) MenuSubTab.ROOT else MenuSubTab.ALLY_LIST)
                        },
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.5.dp, HpBarGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("battle_ally_btn")
                    ) {
                        Text(tr(language, "ALLY", "حليف"), style = MaterialTheme.typography.labelSmall, color = HpBarGreen)
                    }
                }
            }
        }

        // 4. FOUR ICONIC UNDERTALE ORANGE COMMAND BUTTONS: [FIGHT]  [ACT]  [ITEM]  [MERCY]
        if (state.phase == BattlePhase.PLAYER_MENU) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UndertaleCmdBtn(
                    label = tr(language, "FIGHT", "هجوم"),
                    selected = false,
                    soulColor = state.activeSoul.color,
                    onClick = battleViewModel::startFightMinigame,
                    modifier = Modifier.weight(1f).testTag("battle_fight_btn")
                )
                UndertaleCmdBtn(
                    label = tr(language, "ACT", "تصرّف"),
                    selected = state.subTab == MenuSubTab.ACT_LIST,
                    soulColor = state.activeSoul.color,
                    onClick = {
                        battleViewModel.selectSubTab(if (state.subTab == MenuSubTab.ACT_LIST) MenuSubTab.ROOT else MenuSubTab.ACT_LIST)
                    },
                    modifier = Modifier.weight(1f).testTag("battle_act_btn")
                )
                UndertaleCmdBtn(
                    label = tr(language, "ITEM", "حقيبة"),
                    selected = state.subTab == MenuSubTab.ITEM_LIST,
                    soulColor = state.activeSoul.color,
                    onClick = {
                        battleViewModel.selectSubTab(if (state.subTab == MenuSubTab.ITEM_LIST) MenuSubTab.ROOT else MenuSubTab.ITEM_LIST)
                    },
                    modifier = Modifier.weight(1f).testTag("battle_item_btn")
                )
                val isStopOnlyEnemy = !state.enemy.canBefriend && state.enemy.canStopWithoutKilling
                val mercyBtnLabel = when {
                    isStopOnlyEnemy && state.canSpare -> tr(language, "STOP!", "إيقاف!")
                    isStopOnlyEnemy -> tr(language, "STOP", "إيقاف")
                    state.canSpare -> tr(language, "SPARE!", "عفو!")
                    else -> tr(language, "MERCY", "رحمة")
                }
                UndertaleCmdBtn(
                    label = mercyBtnLabel,
                    selected = state.canSpare,
                    soulColor = if (isStopOnlyEnemy) GlitchCyan else MercyYellow,
                    onClick = battleViewModel::performSpare,
                    modifier = Modifier.weight(1f).testTag("battle_spare_button")
                )
            }
        }
    }
}

@Composable
private fun UndertaleEnemyCompactStatus(
    language: AppLanguage,
    state: BattleUiState,
    modifier: Modifier = Modifier
) {
    val hpRatio = (state.enemyHp.toFloat() / state.enemy.maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .background(Color(0xDD000000))
            .border(1.5.dp, Color.White, RoundedCornerShape(0.dp))
            .padding(7.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("HP", style = MaterialTheme.typography.labelSmall, color = Color.White)
            Text("${state.enemyHp}/${state.enemy.maxHp}", style = MaterialTheme.typography.labelSmall, color = HpBarGreen)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(UndertaleHpRed)
                .border(1.dp, Color.White, RoundedCornerShape(0.dp))
        ) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(hpRatio).background(HpBarGreen))
        }
        if (state.enemy.canBefriend) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(tr(language, "MERCY", "رحمة"), style = MaterialTheme.typography.labelSmall, color = MercyYellow)
                Text("${state.mercyProgress}%", style = MaterialTheme.typography.labelSmall, color = MercyYellow)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF27272E))
                    .border(1.dp, MercyYellow.copy(alpha = 0.7f), RoundedCornerShape(0.dp))
            ) {
                val mercyRatio = (state.mercyProgress / 100f).coerceIn(0f, 1f)
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(mercyRatio).background(MercyYellow))
            }
        } else if (state.enemy.canStopWithoutKilling) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(tr(language, "STOP (NO FRIEND)", "إيقاف (بلا صداقة)"), style = MaterialTheme.typography.labelSmall, color = GlitchCyan)
                Text("${state.mercyProgress}%", style = MaterialTheme.typography.labelSmall, color = GlitchCyan)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF27272E))
                    .border(1.dp, GlitchCyan.copy(alpha = 0.7f), RoundedCornerShape(0.dp))
            ) {
                val stopRatio = (state.mercyProgress / 100f).coerceIn(0f, 1f)
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(stopRatio).background(GlitchCyan))
            }
        } else {
            Text(
                text = tr(language, "HOSTILE · NO MERCY", "عدائي · بلا رحمة"),
                style = MaterialTheme.typography.labelSmall,
                color = DangerRed
            )
        }
    }
}

@Composable
private fun UndertaleCmdBtn(
    label: String,
    selected: Boolean,
    soulColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MercyYellow else UndertaleOrange
    val textColor = if (selected) MercyYellow else UndertaleOrange
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(2.5.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) borderColor.copy(alpha = 0.14f) else Color.Black
        ),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                Text("♥", style = MaterialTheme.typography.labelLarge, color = soulColor)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun PlayerMenuInsideBox(
    language: AppLanguage,
    state: BattleUiState,
    onSelectSubTab: (MenuSubTab) -> Unit,
    onSwitchSoul: (SoulType) -> Unit,
    onPerformAct: (String) -> Unit,
    onPerformSpare: () -> Unit,
    onFleeBattle: () -> Unit,
    onUseItem: (String) -> Unit,
    onCallAlly: (EnemyId) -> Unit,
    onCallKidAlly: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (state.subTab) {
            MenuSubTab.ROOT -> {
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

            MenuSubTab.SOUL_SELECT -> {
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

            MenuSubTab.ACT_LIST -> {
                val hint = if (language == AppLanguage.AR) state.enemy.spareMethodHintAr else state.enemy.spareMethodHintEn
                if (hint.isNotBlank()) {
                    Text(
                        text = "* $hint",
                        style = MaterialTheme.typography.labelSmall,
                        color = MercyYellow
                    )
                }
                state.enemy.actOptions.forEach { act ->
                    val prereqMet = act.requiredPrevActId == null || act.requiredPrevActId in state.completedActIds
                    val done = act.id in state.completedActIds
                    val pctGain = if (!state.enemy.isNormalMonster && state.enemy.id != EnemyId.RUINS_DUMMY) 10 else act.mercyGain
                    OutlinedButton(
                        onClick = { onPerformAct(act.id) },
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(
                            1.5.dp,
                            when {
                                !prereqMet -> MutedSepiaInk.copy(alpha = 0.65f)
                                done -> HpBarGreen
                                else -> Color.White
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val prefix = when {
                            !prereqMet -> "🔒 "
                            done -> "✓ "
                            else -> "* "
                        }
                        val actLabel = when {
                            !prereqMet -> "$prefix${act.title} (${tr(language, "Needs prev step", "يتطلب الخطوة السابقة")})"
                            state.enemy.canBefriend && pctGain > 0 ->
                                "$prefix${act.title} (+$pctGain% ${tr(language, "Mercy", "رحمة")})"
                            state.enemy.canStopWithoutKilling && pctGain > 0 ->
                                "$prefix${act.title} (+$pctGain% ${tr(language, "Stop", "إيقاف")})"
                            else -> "$prefix${act.title}"
                        }
                        Text(
                            text = actLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (!prereqMet) MutedSepiaInk else if (done) HpBarGreen else Color.White
                        )
                    }
                }
                if (state.enemy.id != EnemyId.FUTURE_LOKA) {
                    if (state.enemy.canBefriend || state.enemy.canStopWithoutKilling) {
                        val readyBg = if (state.enemy.canBefriend) MercyYellow else GlitchCyan
                        Button(
                            onClick = onPerformSpare,
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.canSpare) readyBg else Color(0xFF18181D),
                                contentColor = if (state.canSpare) Color.Black else Color(0xFF9CA3AF)
                            ),
                            border = BorderStroke(2.dp, if (state.canSpare) Color.White else if (state.enemy.canBefriend) UndertaleOrange else GlitchCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val btnText = when {
                                state.enemy.canBefriend && state.canSpare -> tr(language, "* SPARE & BEFRIEND!", "* اعفُ وصادِقه!")
                                state.enemy.canBefriend -> tr(language, "* SPARE (${state.mercyProgress}%/100%)", "* عفو (${state.mercyProgress}%/100%)")
                                state.canSpare -> tr(language, "* STOP WITHOUT KILLING!", "* إيقاف بدون قتل!")
                                else -> tr(language, "* STOP GOVERNMENT (${state.mercyProgress}%/100%)", "* إيقاف بدون قتل (${state.mercyProgress}%/100%)")
                            }
                            Text(
                                text = btnText,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Text(
                            text = tr(
                                language,
                                "* Hostile monster — cannot be befriended! Defeat it (FIGHT) or FLEE.",
                                "* وحش عدائي — لا يمكن مصادقته! قاتله (FIGHT) أو اهرب (FLEE)."
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed
                        )
                    }
                    if (state.enemy.isNormalMonster) {
                        OutlinedButton(
                            onClick = onFleeBattle,
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(1.5.dp, UndertaleOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("battle_flee_button")
                        ) {
                            Text(
                                text = tr(language, "* FLEE BATTLE", "* هروب من المعركة"),
                                style = MaterialTheme.typography.labelMedium,
                                color = UndertaleOrange
                            )
                        }
                    }
                }
            }

            MenuSubTab.ITEM_LIST -> {
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

            MenuSubTab.ALLY_LIST -> {
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
            delay(18L)
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

/**
 * CLASSIC UNDERTALE OVAL TARGET EYE FIGHT BAR:
 * - Iconic Undertale `target_spr` concentric oval target centered at x = 0.50f
 * - Bright vertical white/black cursor bar sweeping back and forth across the eye
 * - Tap the bar directly or tap [STRIKE!] to deal critical center hit!
 */
@Composable
private fun UndertaleFightEyeBarInsideBox(
    language: AppLanguage,
    cursorPos: Float,
    soulColor: Color,
    weapon: WeaponEquipment = WeaponCatalog.byId("worn_stick"),
    onStrike: () -> Unit
) {
    val targetX = 0.50f
    val inCritZone = abs(cursorPos - targetX) <= (0.055f + weapon.critWindowBonus * 0.45f)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tr(language, "* TAP STRIKE AT THE CENTER EYE!", "* اضغط اضرب عندما يصل الخط لمنتصف العين!"),
                style = MaterialTheme.typography.labelMedium,
                color = if (inCritZone) MercyYellow else Color.White
            )
            Text(
                text = if (language == AppLanguage.AR) weapon.nameAr else weapon.name,
                style = MaterialTheme.typography.labelSmall,
                color = weapon.accentColor
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onStrike
                    )
            ) {
                val w = size.width
                val h = size.height
                drawRect(Color.Black)

                // Classic Undertale Concentric Target Oval Rings
                drawOval(
                    color = Color.White,
                    topLeft = Offset(w * 0.04f, h * 0.08f),
                    size = Size(w * 0.92f, h * 0.84f),
                    style = Stroke(width = 3f)
                )
                drawOval(
                    color = Color(0xFF22C55E).copy(alpha = 0.55f),
                    topLeft = Offset(w * 0.22f, h * 0.16f),
                    size = Size(w * 0.56f, h * 0.68f),
                    style = Stroke(width = 2.5f)
                )

                // Center sweet-spot eye band
                val centerHalfW = (0.055f + weapon.critWindowBonus * 0.45f) * w
                drawRect(
                    color = if (inCritZone) MercyYellow.copy(alpha = 0.40f) else Color(0xFF16A34A).copy(alpha = 0.35f),
                    topLeft = Offset(w * 0.5f - centerHalfW, h * 0.10f),
                    size = Size(centerHalfW * 2f, h * 0.80f)
                )
                drawRect(
                    color = if (inCritZone) MercyYellow else Color.White,
                    topLeft = Offset(w * 0.5f - centerHalfW, h * 0.10f),
                    size = Size(centerHalfW * 2f, h * 0.80f),
                    style = Stroke(2.2f)
                )

                // Classic Undertale Target Eye Vertical Tick Lines
                for (i in 1..11) {
                    val tx = w * (i / 12f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(tx, h * 0.24f),
                        end = Offset(tx, h * 0.76f),
                        strokeWidth = 2f
                    )
                }

                // Sweeping Undertale White/Black Strike Cursor Bar
                val cx = cursorPos * w
                drawRect(
                    color = if (inCritZone) MercyYellow else Color.White,
                    topLeft = Offset(cx - 6.dp.toPx(), 4.dp.toPx()),
                    size = Size(12.dp.toPx(), h - 8.dp.toPx())
                )
                drawRect(
                    color = if (inCritZone) soulColor else Color.Black,
                    topLeft = Offset(cx - 2.5.dp.toPx(), 7.dp.toPx()),
                    size = Size(5.dp.toPx(), h - 14.dp.toPx())
                )
            }

            Button(
                onClick = onStrike,
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (inCritZone) MercyYellow else UndertaleOrange,
                    contentColor = Color.Black
                ),
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .height(54.dp)
                    .testTag("battle_commit_strike_button")
            ) {
                Text(tr(language, "STRIKE!", "اضرب!"), style = MaterialTheme.typography.labelLarge)
            }
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
                        drawRect(Color.Red, Offset(0f, centerCoord - 1.5f), Size(size.width, 3f))
                    } else {
                        drawRect(laser.color.copy(alpha = 0.22f), Offset(centerCoord - halfThick, 0f), Size(halfThick * 2f, size.height), style = Stroke(2f))
                        drawRect(Color.Red, Offset(centerCoord - 1.5f, 0f), Size(3f, size.height))
                    }
                }
            }

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

            state.arrowTelegraphs.forEach { arw ->
                drawDirectionalArrow(
                    origin = Offset(arw.originX * size.width, arw.originY * size.height),
                    dirX = arw.dirX,
                    dirY = arw.dirY,
                    lengthPx = minOf(size.width, size.height) * 0.40f,
                    color = arw.color
                )
            }

            state.bombs.forEach { bomb ->
                drawPixelBomb(
                    center = Offset(bomb.x * size.width, bomb.y * size.height),
                    radiusPx = 14.dp.toPx(),
                    fuseProgress = (bomb.elapsedSec / bomb.fuseSec).coerceIn(0f, 1f),
                    glowColor = bomb.color
                )
            }

            state.sideWarnings.forEach { sw ->
                val edgeX = if (sw.fromRightEdge) size.width - 14.dp.toPx() else 14.dp.toPx()
                val cy = sw.yNorm * size.height
                val dir = if (sw.fromRightEdge) -1f else 1f
                drawRect(Color.Red, Offset(edgeX - 2.dp.toPx(), cy - 11.dp.toPx()), Size(4.dp.toPx(), 13.dp.toPx()))
                drawRect(Color.Red, Offset(edgeX - 2.dp.toPx(), cy + 5.dp.toPx()), Size(4.dp.toPx(), 4.dp.toPx()))
                drawDirectionalArrow(
                    origin = Offset(edgeX + dir * 10.dp.toPx(), cy),
                    dirX = dir,
                    dirY = 0f,
                    lengthPx = 22.dp.toPx(),
                    color = sw.color
                )
            }

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
                        drawRect(Color.White, Offset(cx - r * 0.45f, cy - r * 1.3f), Size(r * 0.9f, r * 2.6f))
                        drawRect(Color.White, Offset(cx - r * 0.9f, cy - r * 0.55f), Size(r * 1.8f, r * 1.1f))
                        drawRect(b.color, Offset(cx - r * 0.35f, cy - r * 0.9f), Size(r * 0.7f, r * 1.8f))
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
                val rad = minOf(size.width, size.height) * 0.20f
                drawRect(
                    color = SoulType.BLUE.color.copy(alpha = 0.28f),
                    topLeft = Offset(state.soulX * size.width - rad, state.soulY * size.height - rad),
                    size = Size(rad * 2f, rad * 2f),
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

            if (state.activeSoul == SoulType.SPLIT_DUO && state.isSplitActive) {
                val otherHalfIdx = 1 - state.controlledHalfIndex
                val otherCenter = Offset(state.secondHalfX * size.width, state.secondHalfY * size.height)
                val otherBoosted = (otherHalfIdx == 0 && state.splitBoostTimer0Sec > 0f) ||
                    (otherHalfIdx == 1 && state.splitBoostTimer1Sec > 0f)
                drawLine(
                    color = Color.White.copy(alpha = 0.28f),
                    start = Offset(state.soulX * size.width, state.soulY * size.height),
                    end = otherCenter,
                    strokeWidth = 2f
                )
                if (otherBoosted) {
                    val sz = 16.dp.toPx()
                    drawRect(MercyYellow.copy(alpha = 0.55f), Offset(otherCenter.x - sz, otherCenter.y - sz), Size(sz * 2f, sz * 2f), style = Stroke(2.5f))
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
                val sz = 18.dp.toPx()
                drawRect(
                    color = MercyYellow.copy(alpha = 0.6f),
                    topLeft = Offset(state.soulX * size.width - sz, state.soulY * size.height - sz),
                    size = Size(sz * 2f, sz * 2f),
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
