package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import com.example.ui.components.LokaBattleStanceCanvas
import com.example.ui.components.LokaPortraitCanvas
import com.example.ui.components.UltraPixelArtEngine
import com.example.ui.components.drawBossChompHeadAndClaw
import com.example.ui.components.drawDirectionalArrow
import com.example.ui.components.drawPixelBomb
import com.example.ui.components.drawPixelHeart
import com.example.ui.components.drawPixelSaveStar
import com.example.ui.components.drawSoulEmblem
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.HpBarGreen
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.MutedSepiaInk
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaAmber
import com.example.ui.theme.SepiaGold
import com.example.ui.theme.SoulSeraphCyan
import com.example.viewmodel.BattleUiState
import com.example.viewmodel.BattleViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

private val UndertaleOrange = Color(0xFFFF8C1A)
private val DeltaruneTpOrange = Color(0xFFFFA040)
private val DeltarunePurpleLine = Color(0xFFA855F7)
private val DeltaruneCyan = Color(0xFF00E5FF)
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

    LaunchedEffect(battleSessionId, enemyId, worldId, helpedKidIds, equippedWeapon.id, equippedArmor.id) {
        battleViewModel.initEncounter(
            encounterId = battleSessionId,
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

    LaunchedEffect(state.encounterId, state.phase) {
        if (state.encounterId == battleSessionId && state.phase == BattlePhase.VICTORY) {
            val remainingMap = state.items.associate { it.id to it.count }
            onBattleFinished(state.enemy.id, state.battleWonSpared, remainingMap)
        }
    }

    val gridTransition = rememberInfiniteTransition(label = "deltaruneGridScroll")
    val gridScroll by gridTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "gridScroll"
    )

    // FULL DELTARUNE BATTLE SCREEN (Top Animated Purple Grid Stage + Vertical TP Gauge; Bottom Deck + Party Bar)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // ==================== TOP: DELTARUNE STAGE AREA ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.24f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Far-Left Vertical TP Gauge (Tension Points 0% - 100%)
            DeltaruneVerticalTpBar(
                tpPercent = state.tpPercent,
                modifier = Modifier
                    .width(34.dp)
                    .fillMaxHeight()
            )

            // Main Deltarune Purple Pixel Grid Backdrop & Combatants / Bullet Arena
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF06030C))
                    .border(2.dp, Color(0xFF4C1D95), RoundedCornerShape(0.dp))
            ) {
                // Deltarune Scrolling Purple Horizon & Floor Perspective Grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val bgBitmap = UltraPixelArtEngine.getDeltaruneBattleBgBitmap()
                    drawImage(
                        image = bgBitmap,
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.roundToInt().coerceAtLeast(1), size.height.roundToInt().coerceAtLeast(1)),
                        filterQuality = FilterQuality.None
                    )
                    // Animated scrolling purple/indigo grid lines
                    val cols = 10
                    val stepW = size.width / cols
                    val offsetPx = (gridScroll * stepW)
                    for (c in -1..cols + 1) {
                        val x = c * stepW - offsetPx
                        drawLine(
                            color = DeltarunePurpleLine.copy(alpha = 0.28f),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 2f
                        )
                    }
                    val horizonY = size.height * 0.42f
                    drawLine(DeltarunePurpleLine.copy(alpha = 0.45f), Offset(0f, horizonY), Offset(size.width, horizonY), strokeWidth = 2.5f)
                    for (r in 1..4) {
                        val y = horizonY + ((size.height - horizonY) * ((r / 4f + gridScroll * 0.25f) % 1f))
                        drawLine(DeltarunePurpleLine.copy(alpha = 0.26f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.8f)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Compact Top Header Strip inside Stage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = onExitBattle,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("battle_back_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Flee battle", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "${state.enemy.name.uppercase()} · ${tr(language, "TURN ${state.turnNumber}", "دور ${state.turnNumber}")}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = state.enemy.accentColor,
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
                                    .height(30.dp)
                                    .testTag("battle_controls_toggle")
                            ) {
                                Text(if (language == AppLanguage.AR) controlScheme.labelAr else controlScheme.label, style = MaterialTheme.typography.labelSmall, color = SepiaAmber)
                            }
                            OutlinedButton(
                                onClick = onToggleLanguage,
                                shape = RoundedCornerShape(0.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                border = BorderStroke(1.5.dp, Color.White),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(language.buttonLabel, style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }

                    // STAGE BODY: Either Left-vs-Right Combat Stage OR Central Bullet Box flanked by Loka + Monster/Controls
                    if (state.phase == BattlePhase.BULLET_HELL) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Side: Loka Stance + compact World Soul Buttons + [C] Ability
                            Column(
                                modifier = Modifier
                                    .weight(0.68f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LokaBattleStanceCanvas(
                                    isGlitched = state.worldId == WorldId.GLITCHED_WORLD,
                                    isDefending = state.isDefending,
                                    isSlashing = state.slashFxTimerSec > 0f,
                                    weaponAccent = state.equippedWeapon.accentColor,
                                    expression = state.speakerExpression,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                                        state.allowedSouls.forEach { soul ->
                                            val active = soul == state.activeSoul
                                            Button(
                                                onClick = { battleViewModel.switchSoul(soul, duringBulletHell = true) },
                                                shape = RoundedCornerShape(0.dp),
                                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (active) soul.color else Color.Black,
                                                    contentColor = if (active) Color.Black else soul.color
                                                ),
                                                border = BorderStroke(1.5.dp, soul.color),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(34.dp)
                                                    .testTag("dodge_soul_${soul.name.lowercase()}")
                                            ) {
                                                Text(
                                                    text = soul.title.replace(" Soul", "").uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1
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
                                        border = BorderStroke(1.5.dp, if (ready) Color.White else Color(0xFF4B5563)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .testTag("battle_ability_button")
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text(text = abilityLabel.uppercase(), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                    }
                                }
                            }

                            // Center: Bright Green / Soul-Bordered Deltarune Bullet Box Arena!
                            Box(
                                modifier = Modifier
                                    .weight(1.22f)
                                    .fillMaxHeight()
                                    .background(Color.Black, RoundedCornerShape(0.dp))
                                    .border(
                                        width = 3.5.dp,
                                        color = if (state.activeSoul == SoulType.RED) Color(0xFF22C55E) else state.activeSoul.color,
                                        shape = RoundedCornerShape(0.dp)
                                    )
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
                                            .padding(top = 3.dp)
                                    )
                                }
                            }

                            // Right Side: Monochrome Undertale Enemy Sprite + HP/Mercy + Optional Controller Pad
                            Column(
                                modifier = Modifier
                                    .weight(0.68f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                BossSpriteCanvas(
                                    enemyId = state.enemy.id,
                                    isHitFlash = state.isEnemyHitFlash,
                                    isSpareReady = state.canSpare,
                                    expression = state.bossExpression,
                                    monochromeBattle = true,
                                    modifier = Modifier.size(if (controlScheme == ControlScheme.TOUCH_DRAG) 112.dp else 68.dp)
                                )
                                DeltaruneEnemyCompactMeters(language = language, state = state)
                                if (controlScheme != ControlScheme.TOUCH_DRAG) {
                                    ControllerPadOverlay(
                                        controlScheme = controlScheme,
                                        accentColor = state.activeSoul.color,
                                        padSize = 94.dp,
                                        onDirectionChanged = battleViewModel::setDpadDirection
                                    )
                                }
                            }
                        }
                    } else {
                        // NON-DODGE STAGE: Classic Deltarune Left Loka vs Right Undertale-Monochrome Enemy + Meters & Speech Bubble!
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // LEFT PARTY FIGHTER: Loka Battle Stance
                            Box(
                                modifier = Modifier
                                    .weight(0.85f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                LokaBattleStanceCanvas(
                                    isGlitched = state.worldId == WorldId.GLITCHED_WORLD,
                                    isDefending = state.isDefending,
                                    isSlashing = state.slashFxTimerSec > 0f,
                                    weaponAccent = state.equippedWeapon.accentColor,
                                    expression = state.speakerExpression,
                                    modifier = Modifier
                                        .size(134.dp)
                                        .padding(start = 10.dp)
                                )
                                if (state.ashenHelpingThisTurn) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .background(Color.Black, RoundedCornerShape(0.dp))
                                            .border(1.5.dp, Color(0xFFD4D4D8), RoundedCornerShape(0.dp))
                                            .padding(4.dp)
                                    ) {
                                        AshenFlowerCanvas(
                                            isCreepySmirk = false,
                                            expression = PortraitExpression.HAPPY,
                                            modifier = Modifier.size(34.dp)
                                        )
                                        Text(
                                            text = tr(language, "ASHEN!", "آشن!"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFD4D4D8)
                                        )
                                    }
                                }
                            }

                            // CENTER-RIGHT: Enemy Speech Bubble (when talking) + Deltarune Slash FX
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
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(0.95f)
                                        .fillMaxHeight()
                                        .padding(vertical = 8.dp)
                                        .background(Color.White, RoundedCornerShape(0.dp))
                                        .border(2.5.dp, Color(0xFF1F2937), RoundedCornerShape(0.dp))
                                        .padding(8.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    TypewriterBattleText(
                                        fullText = state.dialogueText,
                                        voicePitchHz = enemyVoicePitch,
                                        textColor = Color.Black
                                    )
                                }
                            }

                            // RIGHT OPPONENT: Undertale Monochrome Enemy Sprite + Deltarune Compact HP / MERCY Gauges
                            Row(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(134.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BossSpriteCanvas(
                                        enemyId = state.enemy.id,
                                        isHitFlash = state.isEnemyHitFlash,
                                        isSpareReady = state.canSpare,
                                        expression = state.bossExpression,
                                        monochromeBattle = true,
                                        modifier = Modifier.size(124.dp)
                                    )
                                    if (state.slashFxTimerSec > 0f && state.lastStrikeDamage != null) {
                                        val progress = (1f - (state.slashFxTimerSec / 0.50f)).coerceIn(0f, 1f)
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val cx = size.width * 0.5f
                                            val cy = size.height * 0.5f
                                            val span = minOf(size.width, size.height) * 0.42f
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
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                DeltaruneEnemyCompactMeters(
                                    language = language,
                                    state = state,
                                    modifier = Modifier.width(126.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==================== BOTTOM: DELTARUNE PARTY STATUS & COMMAND DECK ====================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.95f)
                .background(Color.Black)
                .border(2.5.dp, Color.White, RoundedCornerShape(0.dp)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Deltarune Top Bar of Bottom Deck: Loka Status Card + Iconic Deltarune Command Row!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0810))
                    .border(
                        width = 2.dp,
                        color = if (state.isDefending) DeltaruneCyan else state.activeSoul.color,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Loka Portrait + Name + Deltarune HP Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(0.85f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.Black)
                            .border(1.5.dp, DeltaruneCyan, RoundedCornerShape(0.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        LokaPortraitCanvas(
                            saveCount = 0,
                            isSickGlitched = state.worldId == WorldId.GLITCHED_WORLD,
                            expression = state.speakerExpression,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.isDefending) "LOKA [GUARD]" else "LOKA",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (state.isDefending) DeltaruneCyan else Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "HP ${state.playerHp}/${state.maxPlayerHp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.playerHp <= 6) DangerRed else Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(UndertaleHpRed)
                                .border(1.dp, Color.White, RoundedCornerShape(0.dp))
                        ) {
                            val ratio = (state.playerHp.toFloat() / state.maxPlayerHp).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio)
                                    .background(DeltaruneCyan)
                            )
                        }
                    }
                }

                // Deltarune Square Command Icons Row (FIGHT · ACT · ITEM · SPARE · DEFEND · SOUL · ALLY)
                if (state.phase == BattlePhase.PLAYER_MENU) {
                    Row(
                        modifier = Modifier.weight(1.75f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DeltaruneCmdBtn(tr(language, "FIGHT", "هجوم"), UndertaleOrange, false, battleViewModel::startFightMinigame, Modifier.weight(1f).testTag("battle_fight_btn"))
                        DeltaruneCmdBtn(tr(language, "ACT", "تصرّف"), UndertaleOrange, state.subTab == MenuSubTab.ACT_LIST, {
                            battleViewModel.selectSubTab(if (state.subTab == MenuSubTab.ACT_LIST) MenuSubTab.ROOT else MenuSubTab.ACT_LIST)
                        }, Modifier.weight(1f).testTag("battle_act_btn"))
                        DeltaruneCmdBtn(tr(language, "ITEM", "حقيبة"), UndertaleOrange, state.subTab == MenuSubTab.ITEM_LIST, {
                            battleViewModel.selectSubTab(if (state.subTab == MenuSubTab.ITEM_LIST) MenuSubTab.ROOT else MenuSubTab.ITEM_LIST)
                        }, Modifier.weight(1f).testTag("battle_item_btn"))
                        DeltaruneCmdBtn(
                            label = if (state.canSpare) tr(language, "SPARE!", "عفو!") else tr(language, "MERCY", "رحمة"),
                            accent = if (state.canSpare) MercyYellow else UndertaleOrange,
                            highlighted = state.canSpare,
                            onClick = battleViewModel::performSpare,
                            modifier = Modifier.weight(1f).testTag("battle_spare_button")
                        )
                        DeltaruneCmdBtn(tr(language, "DEFEND", "دفاع"), DeltaruneCyan, false, battleViewModel::performDefend, Modifier.weight(1f).testTag("battle_defend_btn"))
                        DeltaruneCmdBtn(tr(language, "SOUL", "الروح"), state.activeSoul.color, state.subTab == MenuSubTab.SOUL_SELECT, {
                            battleViewModel.selectSubTab(if (state.subTab == MenuSubTab.SOUL_SELECT) MenuSubTab.ROOT else MenuSubTab.SOUL_SELECT)
                        }, Modifier.weight(0.95f).testTag("battle_soul_btn"))
                        DeltaruneCmdBtn(tr(language, "ALLY", "حليف"), HpBarGreen, state.subTab == MenuSubTab.ALLY_LIST, {
                            battleViewModel.selectSubTab(if (state.subTab == MenuSubTab.ALLY_LIST) MenuSubTab.ROOT else MenuSubTab.ALLY_LIST)
                        }, Modifier.weight(0.95f).testTag("battle_ally_btn"))
                    }
                } else {
                    val phaseLabel = when (state.phase) {
                        BattlePhase.FIGHT_MINIGAME -> tr(language, ">> DELTARUNE ATTACK TIMING — HIT LEFT TARGET BOX!", ">> شريط قتال ديلتا رون — اضرب عند المربع الأيسر!")
                        BattlePhase.ENEMY_DIALOGUE -> tr(language, ">> ENEMY TURN — TAP DODGE!", ">> دور العدو — اضغط تفادَ الآن!")
                        BattlePhase.BULLET_HELL -> tr(language, ">> DODGE & GRAZE FOR +TP!", ">> تفادَ واقترب من الطلقات لزيادة الـ TP!")
                        BattlePhase.VICTORY -> tr(language, ">> BATTLE WON!", ">> انتصرت في القتال!")
                        BattlePhase.DEFEAT -> tr(language, ">> SOUL SHATTERED...", ">> تحطمت الروح...")
                        BattlePhase.PLAYER_MENU -> ""
                    }
                    Text(
                        text = phaseLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MercyYellow,
                        modifier = Modifier.weight(1.75f),
                        textAlign = TextAlign.End
                    )
                }
            }

            // Lower Main Panel Content (Deltarune Fight Bar / Dialogue / ACTs / Items / Dodge Continue / Victory)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ArenaBlack)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                when (state.phase) {
                    BattlePhase.PLAYER_MENU -> PlayerMenuInsideBox(
                        language = language,
                        state = state,
                        onSelectSubTab = battleViewModel::selectSubTab,
                        onSwitchSoul = { battleViewModel.switchSoul(it, duringBulletHell = false) },
                        onPerformAct = battleViewModel::performAct,
                        onPerformSpare = battleViewModel::performSpare,
                        onPerformTpXSlash = battleViewModel::performTpXSlash,
                        onPerformTpMercy = battleViewModel::performTpMercyPulse,
                        onFleeBattle = onExitBattle,
                        onUseItem = battleViewModel::useItem,
                        onCallAlly = battleViewModel::callFriendAid,
                        onCallKidAlly = battleViewModel::callLostKidAid
                    )

                    BattlePhase.FIGHT_MINIGAME -> DeltaruneFightBarInsideBox(
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
                            if (state.isDefending) {
                                Text(
                                    text = tr(language, "* GUARD ACTIVE: 50% damage reduction this turn!", "* وضع الدفاع مفعّل: تقليل الضرر للنصف!"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeltaruneCyan
                                )
                            }
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

                    BattlePhase.BULLET_HELL -> Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = tr(
                                language,
                                "* Stay close to bullets without getting hit to build TP! Press [C] for ${state.activeSoul.abilityName}.",
                                "* اقترب من الطلقات دون لمسها لشحن الـ TP! واضغط [C] لتفعيل قدرة الروح."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        if (state.isDefending) {
                            Text(
                                text = "GUARD 50%",
                                style = MaterialTheme.typography.labelMedium,
                                color = DeltaruneCyan,
                                modifier = Modifier
                                    .border(1.5.dp, DeltaruneCyan, RoundedCornerShape(0.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
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
                                    state.battleWonSpared -> tr(language, "YOU WON! FRIENDSHIP FORGED!", "فزت! صداقة جديدة!")
                                    else -> tr(language, "YOU WON!", "انتصار!")
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = when {
                                    state.phase == BattlePhase.DEFEAT -> DangerRed
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
                }
            }
        }
    }
}

@Composable
private fun DeltaruneVerticalTpBar(
    tpPercent: Int,
    modifier: Modifier = Modifier
) {
    val fillRatio = (tpPercent / 100f).coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .background(Color.Black)
            .border(2.dp, Color.White, RoundedCornerShape(0.dp))
            .padding(vertical = 4.dp, horizontal = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("T", style = MaterialTheme.typography.labelMedium, color = DeltaruneTpOrange)
        Text("P", style = MaterialTheme.typography.labelMedium, color = DeltaruneTpOrange)
        Box(
            modifier = Modifier
                .weight(1f)
                .width(14.dp)
                .padding(vertical = 3.dp)
                .background(Color(0xFF450A0A))
                .border(1.dp, Color.White, RoundedCornerShape(0.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillRatio)
                    .background(if (tpPercent >= 100) MercyYellow else DeltaruneTpOrange)
            )
        }
        Text(
            text = if (tpPercent >= 100) "MAX" else "$tpPercent%",
            style = MaterialTheme.typography.labelSmall,
            color = if (tpPercent >= 100) MercyYellow else Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun DeltaruneEnemyCompactMeters(
    language: AppLanguage,
    state: BattleUiState,
    modifier: Modifier = Modifier
) {
    val hpRatio = (state.enemyHp.toFloat() / state.enemy.maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .background(Color(0xCC000000))
            .border(1.5.dp, Color.White, RoundedCornerShape(0.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("HP", style = MaterialTheme.typography.labelSmall, color = Color.White)
            Text("${(hpRatio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = HpBarGreen)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
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
        } else {
            Text(
                text = tr(language, "NO MERCY", "بلا رحمة"),
                style = MaterialTheme.typography.labelSmall,
                color = DangerRed
            )
        }
    }
}

@Composable
private fun DeltaruneCmdBtn(
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
            containerColor = if (highlighted) activeColor.copy(alpha = 0.20f) else Color.Black
        ),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = if (highlighted) "♥$label" else label,
            style = MaterialTheme.typography.labelSmall,
            color = activeColor,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
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
    onPerformTpXSlash: () -> Unit,
    onPerformTpMercy: () -> Unit,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr(
                            language,
                            "* ${state.equippedWeapon.name} (+${state.equippedWeapon.attackBonus} ATK) · ${state.equippedArmorName}",
                            "* ${state.equippedWeapon.nameAr} (+${state.equippedWeapon.attackBonus} هجوم) · ${state.equippedArmorName}"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSepiaInk,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.tpPercent >= 50) {
                        Button(
                            onClick = onPerformTpXSlash,
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeltaruneTpOrange, contentColor = Color.Black),
                            border = BorderStroke(1.5.dp, Color.White),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(tr(language, "X-SLASH (50% TP)", "ضربة إكس (50% TP)"), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (state.tpPercent >= 40 && state.enemy.canBefriend && !state.canSpare) {
                        Button(
                            onClick = onPerformTpMercy,
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MercyYellow, contentColor = Color.Black),
                            border = BorderStroke(1.5.dp, Color.White),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(tr(language, "PACIFY (40% TP)", "تهدئة (40% TP)"), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
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
                state.enemy.actOptions.forEach { act ->
                    OutlinedButton(
                        onClick = { onPerformAct(act.id) },
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val actLabel = if (state.enemy.canBefriend && act.mercyGain > 0) {
                            "* ${act.title} (+${act.mercyGain}% ${tr(language, "Mercy", "رحمة")})"
                        } else {
                            "* ${act.title}"
                        }
                        Text(actLabel, style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
                if (state.enemy.id != EnemyId.FUTURE_LOKA) {
                    if (state.enemy.canBefriend) {
                        Button(
                            onClick = onPerformSpare,
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.canSpare) MercyYellow else Color(0xFF18181D),
                                contentColor = if (state.canSpare) Color.Black else Color(0xFF9CA3AF)
                            ),
                            border = BorderStroke(2.dp, if (state.canSpare) Color.White else UndertaleOrange),
                            modifier = Modifier.fillMaxWidth()
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
 * AUTHENTIC DELTARUNE FIGHT BAR:
 * - Left portrait icon slot (Cyan Loka frame)
 * - Left Target Zone rectangle at x = 0.16f (glows bright Gold/Cyan when timing bar aligns!)
 * - Crisp Right-to-Left sweeping attack cursor with fading pixel afterimage trail
 */
@Composable
private fun DeltaruneFightBarInsideBox(
    language: AppLanguage,
    cursorPos: Float,
    soulColor: Color,
    weapon: WeaponEquipment = WeaponCatalog.byId("worn_stick"),
    onStrike: () -> Unit
) {
    val targetX = 0.16f
    val inCritZone = abs(cursorPos - targetX) <= (0.048f + weapon.critWindowBonus * 0.40f)
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
                text = tr(language, "← TAP STRIKE INSIDE THE LEFT BOX!", "← اضغط اضرب عندما يصل الخط للمربع الأيسر!"),
                style = MaterialTheme.typography.labelMedium,
                color = if (inCritZone) MercyYellow else DeltaruneCyan
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Deltarune Fighter Icon Slot
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF0A0D18))
                    .border(2.dp, DeltaruneCyan, RoundedCornerShape(0.dp)),
                contentAlignment = Alignment.Center
            ) {
                LokaPortraitCanvas(saveCount = 0, modifier = Modifier.size(48.dp))
            }

            // Deltarune Horizontal Rectangular Track (Tap directly on bar or press STRIKE!)
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(62.dp)
                    .border(2.dp, DeltaruneCyan, RoundedCornerShape(0.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onStrike
                    )
            ) {
                val w = size.width
                val h = size.height
                drawRect(color = Color(0xFF06060C))

                // Subtle vertical Deltarune timing guide ticks across track
                for (i in 1..10) {
                    val tx = w * (i / 11f)
                    drawLine(
                        color = Color(0xFF1F2937),
                        start = Offset(tx, h * 0.16f),
                        end = Offset(tx, h * 0.84f),
                        strokeWidth = 2f
                    )
                }

                // Target Hit Box on the Left (x = 0.16f)
                val targetHalfW = (0.048f + weapon.critWindowBonus * 0.40f) * w
                val txLeft = (targetX * w) - targetHalfW
                val targetFill = if (inCritZone) MercyYellow.copy(alpha = 0.42f) else DeltaruneCyan.copy(alpha = 0.22f)
                val targetOutline = if (inCritZone) MercyYellow else DeltaruneCyan
                drawRect(
                    color = targetFill,
                    topLeft = Offset(txLeft, 4.dp.toPx()),
                    size = Size(targetHalfW * 2f, h - 8.dp.toPx())
                )
                drawRect(
                    color = targetOutline,
                    topLeft = Offset(txLeft, 4.dp.toPx()),
                    size = Size(targetHalfW * 2f, h - 8.dp.toPx()),
                    style = Stroke(width = 3f)
                )

                // Secondary Dual-Blade track line if equipped weapon has DUAL_BLADE style
                if (weapon.style == WeaponStyle.DUAL_BLADE) {
                    drawLine(
                        color = MercyYellow.copy(alpha = 0.45f),
                        start = Offset(0f, h * 0.5f),
                        end = Offset(w, h * 0.5f),
                        strokeWidth = 2f
                    )
                }

                // Trailing Deltarune Ghost Afterimages moving right -> left
                val barColor = if (weapon.style == WeaponStyle.SINGLE_BAR) soulColor else weapon.accentColor
                for (trail in 3 downTo 1) {
                    val ghostX = (cursorPos + trail * 0.032f).coerceIn(0f, 1f) * w
                    drawRect(
                        color = barColor.copy(alpha = 0.16f * (4 - trail)),
                        topLeft = Offset(ghostX - 4.dp.toPx(), 4.dp.toPx()),
                        size = Size(8.dp.toPx(), h - 8.dp.toPx())
                    )
                }

                // Primary Moving Rectangular Timing Bar
                val cx = cursorPos * w
                drawRect(
                    color = if (inCritZone) MercyYellow else Color.White,
                    topLeft = Offset(cx - 5.dp.toPx(), 2.dp.toPx()),
                    size = Size(10.dp.toPx(), h - 4.dp.toPx())
                )
                drawRect(
                    color = if (inCritZone) Color.White else barColor,
                    topLeft = Offset(cx - 3.dp.toPx(), 5.dp.toPx()),
                    size = Size(6.dp.toPx(), h - 10.dp.toPx())
                )
            }

            Button(
                onClick = onStrike,
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (inCritZone) MercyYellow else weapon.accentColor,
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
            // Vertical Rectangular Lane Telegraphs with Stepped Up/Down Arrows
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
                        drawRect(Color.Red, Offset(0f, centerCoord - 1.5f), Size(size.width, 3f))
                    } else {
                        drawRect(laser.color.copy(alpha = 0.22f), Offset(centerCoord - halfThick, 0f), Size(halfThick * 2f, size.height), style = Stroke(2f))
                        drawRect(Color.Red, Offset(centerCoord - 1.5f, 0f), Size(3f, size.height))
                    }
                }
            }

            // Pixel Monster Chomp Head + Claw Strike
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

            // Crossed Stepped Arrow Telegraphs
            state.arrowTelegraphs.forEach { arw ->
                drawDirectionalArrow(
                    origin = Offset(arw.originX * size.width, arw.originY * size.height),
                    dirX = arw.dirX,
                    dirY = arw.dirY,
                    lengthPx = minOf(size.width, size.height) * 0.40f,
                    color = arw.color
                )
            }

            // Pixel Fuse Bombs
            state.bombs.forEach { bomb ->
                drawPixelBomb(
                    center = Offset(bomb.x * size.width, bomb.y * size.height),
                    radiusPx = 14.dp.toPx(),
                    fuseProgress = (bomb.elapsedSec / bomb.fuseSec).coerceIn(0f, 1f),
                    glowColor = bomb.color
                )
            }

            // Side Wall "! <-" / "-> !" Shot Warnings
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

            // Crisp Stepped Pixel Bullets
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
                        // Stepped pixel diamond lance
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
