package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.model.AppLanguage
import com.example.model.ArmorCatalog
import com.example.model.ControlScheme
import com.example.model.EnemyId
import com.example.model.EnemyProfile
import com.example.model.EnemyRoster
import com.example.model.LostKid
import com.example.model.PortraitExpression
import com.example.model.RouteType
import com.example.model.WeaponCatalog
import com.example.model.WorldChest
import com.example.model.WorldContentCatalog
import com.example.model.WorldId
import com.example.model.WorldLandmark
import com.example.model.WorldNpc
import com.example.model.tr
import com.example.ui.components.ControllerPadOverlay
import com.example.ui.components.LokaPortraitCanvas
import com.example.ui.components.drawLokaOverworldSprite
import com.example.ui.components.drawOverworldAshenSaveSprite
import com.example.ui.components.drawOverworldChasingMonster
import com.example.ui.components.drawOverworldKidSprite
import com.example.ui.components.drawOverworldLandmarkSprite
import com.example.ui.components.drawOverworldNpcSprite
import com.example.ui.components.drawPixelChest
import com.example.ui.components.drawUndertaleBossPixelSprite
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EspressoSurface
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
import com.example.viewmodel.AppUiState
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorldExploreScreen(
    state: AppUiState,
    onMovePlayer: (Float, Float) -> Unit,
    onTickOverworld: (Float) -> Unit,
    onHelpKid: (String) -> Unit,
    onKillKid: (String) -> Unit,
    onCompleteLandmark: (String) -> Unit,
    onClaimNpcEvent: (String) -> Unit,
    onOpenChest: (String) -> Unit,
    onEngageBoss: (EnemyId) -> Unit,
    onEquipArmor: (String) -> Unit,
    onEquipWeapon: (String) -> Unit,
    onSaveGame: () -> Unit,
    onResetSave: () -> Unit,
    onOpenRouteFinale: (RouteType) -> Unit,
    onWatchStoryIntro: () -> Unit,
    onToggleControls: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleMute: () -> Unit
) {
    var activeLandmarkDialog by remember { mutableStateOf<WorldLandmark?>(null) }
    var activeNpcDialog by remember { mutableStateOf<WorldNpc?>(null) }
    var activeKidDialog by remember { mutableStateOf<LostKid?>(null) }
    var showBagDialog by remember { mutableStateOf(false) }
    var confirmResetDialog by remember { mutableStateOf(false) }
    var padDirX by remember { mutableFloatStateOf(0f) }
    var padDirY by remember { mutableFloatStateOf(0f) }
    var ambientClock by remember { mutableFloatStateOf(0f) }

    BackHandler(enabled = confirmResetDialog || showBagDialog || activeLandmarkDialog != null || activeNpcDialog != null || activeKidDialog != null) {
        confirmResetDialog = false
        activeLandmarkDialog = null
        activeNpcDialog = null
        activeKidDialog = null
        showBagDialog = false
    }

    LaunchedEffect(state.activeWorld, state.teleportControlMastered) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    ambientClock = (ambientClock + dt) % 600f
                    onTickOverworld(dt)
                    if (padDirX != 0f || padDirY != 0f) {
                        val walkSpeed = if (state.activeWorld == WorldId.GLITCHED_WORLD) 0.24f else 0.42f
                        onMovePlayer(padDirX * walkSpeed * dt, padDirY * walkSpeed * dt)
                    }
                }
                lastNanos = nanos
            }
        }
    }

    val lang = state.language
    val world = state.activeWorld
    val landmarks = remember(world) { WorldContentCatalog.landmarks.filter { it.worldId == world } }
    val npcs = remember(world) { WorldContentCatalog.npcs.filter { it.worldId == world } }
    val worldChests = remember(world) { WorldContentCatalog.chests.filter { it.worldId == world } }
    // Unresolved kids remain at their stranded spots in other worlds until helped or killed
    val unresolvedKids = remember(world, state.escortingKidIds, state.rescuedToNormalKidIds, state.killedKidIds) {
        WorldContentCatalog.lostKids.filter {
            it.worldId == world &&
                it.id !in state.escortingKidIds &&
                it.id !in state.rescuedToNormalKidIds &&
                it.id !in state.killedKidIds
        }
    }
    // Kids already brought safely to Normal World stay safely at the Normal World crossroad
    val safeNormalKids = remember(world, state.rescuedToNormalKidIds) {
        if (world == WorldId.NORMAL_WORLD) {
            WorldContentCatalog.lostKids.filter { it.id in state.rescuedToNormalKidIds }
        } else emptyList()
    }
    val bosses = remember(world) { EnemyRoster.forWorld(world) }

    val px = state.playerMapX
    val py = state.playerMapY
    val nearUnresolvedKid = unresolvedKids.minByOrNull { hypot(it.xNorm - px, it.yNorm - py) }
        ?.takeIf { hypot(it.xNorm - px, it.yNorm - py) < 0.13f }
    val nearSafeKidIdx = safeNormalKids.indices.firstOrNull { idx ->
        val sx = 0.36f + idx * 0.08f
        val sy = 0.56f
        hypot(sx - px, sy - py) < 0.12f
    }
    val nearKid = nearUnresolvedKid ?: nearSafeKidIdx?.let { safeNormalKids[it] }
    val nearChest: WorldChest? = worldChests.minByOrNull { hypot(it.xNorm - px, it.yNorm - py) }
        ?.takeIf { hypot(it.xNorm - px, it.yNorm - py) < 0.13f }
    val nearLandmark = landmarks.minByOrNull { hypot(it.xNorm - px, it.yNorm - py) }
        ?.takeIf { hypot(it.xNorm - px, it.yNorm - py) < 0.14f }
    val nearNpc = npcs.minByOrNull { hypot(it.xNorm - px, it.yNorm - py) }
        ?.takeIf { hypot(it.xNorm - px, it.yNorm - py) < 0.13f }
    val ashenSavePos = 0.50f to 0.31f
    val nearAshenSave = world.hasLandmarks && hypot(ashenSavePos.first - px, ashenSavePos.second - py) < 0.12f
    val worldRoamers = state.roamingMonsters.filter { it.worldId == world }
    val nearBoss: EnemyProfile? = if (world == WorldId.GLITCHED_WORLD) {
        val (ex, ey) = state.echoLokaPos
        if (hypot(ex - px, ey - py) < 0.15f) EnemyRoster.byId(EnemyId.ECHO_LOKA) else null
    } else {
        bosses.minByOrNull { hypot(it.xNorm - px, it.yNorm - py) }
            ?.takeIf { hypot(it.xNorm - px, it.yNorm - py) < 0.14f }
    }

    val unlockedRoute = state.eligibleRoute

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // LEFT: Interactive Undertale-style 2D Pixel World Map Canvas
        Box(
            modifier = Modifier
                .weight(1.75f)
                .fillMaxHeight()
                .border(3.dp, if (world == WorldId.GLITCHED_WORLD) GlitchCyan else Color.White, RoundedCornerShape(0.dp))
                .pointerInput(world, state.controlScheme) {
                    if (state.controlScheme == ControlScheme.TOUCH_DRAG) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val speedMult = if (world == WorldId.GLITCHED_WORLD) 0.55f else 1f
                            val dx = (dragAmount.x / size.width.toFloat()) * speedMult
                            val dy = (dragAmount.y / size.height.toFloat()) * speedMult
                            onMovePlayer(dx, dy)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawWorldTerrain(world, ambientClock)

                // Ambient floating particles per realm
                val particleColor = when (world) {
                    WorldId.NORMAL_WORLD -> MercyYellow.copy(alpha = 0.35f)
                    WorldId.TRI_RACE_REALM -> GlitchCyan.copy(alpha = 0.35f)
                    WorldId.SPLIT_REALM -> Color(0xFFF472B6).copy(alpha = 0.35f)
                    WorldId.GLITCHED_WORLD -> Color.White.copy(alpha = 0.40f)
                }
                for (i in 0 until 12) {
                    val fx = ((i * 0.19f + ambientClock * (0.015f + (i % 3) * 0.006f)) % 1f) * size.width
                    val fy = ((i * 0.27f + sin(ambientClock * 1.4f + i) * 0.04f + 1f) % 1f) * size.height
                    drawRect(particleColor, Offset(fx, fy), Size(3.5.dp.toPx(), 3.5.dp.toPx()))
                }

                if (world.hasLandmarks) {
                    if (!state.teleportControlMastered) {
                        val (rx, ry) = state.paradoxRiftPos
                        val rc = Offset(rx * size.width, ry * size.height)
                        val pulseR = 14.dp.toPx() + sin(ambientClock * 4f) * 3.dp.toPx()
                        drawCircle(color = GlitchMagenta.copy(alpha = 0.25f), radius = pulseR + 4.dp.toPx(), center = rc)
                        drawCircle(color = GlitchCyan, radius = 10.dp.toPx(), center = rc, style = Stroke(2.2f))
                        drawCircle(color = Color.Black, radius = 5.dp.toPx(), center = rc)
                    }

                    // Ashen is our Overworld Save Point in every standard realm!
                    val saveC = Offset(ashenSavePos.first * size.width, ashenSavePos.second * size.height)
                    drawOverworldAshenSaveSprite(
                        center = saveC,
                        sizePx = 30.dp.toPx(),
                        ambientClock = ambientClock
                    )

                    landmarks.forEach { lm ->
                        val completed = lm.id in state.completedLandmarkIds
                        val lmAccent = when (world) {
                            WorldId.NORMAL_WORLD -> SepiaAmber
                            WorldId.TRI_RACE_REALM -> Color(0xFF06B6D4)
                            WorldId.SPLIT_REALM -> if (lm.xNorm < 0.5f) Color(0xFFEC4899) else Color(0xFFEF4444)
                            WorldId.GLITCHED_WORLD -> GlitchCyan
                        }
                        drawOverworldLandmarkSprite(
                            kind = lm.kind,
                            center = Offset(lm.xNorm * size.width, lm.yNorm * size.height),
                            sizePx = 34.dp.toPx(),
                            accent = lmAccent,
                            isSecret = lm.isSecret,
                            completed = completed
                        )
                    }

                    // Drawn Chests (Image 1 — Closed & Opened shaded states)
                    worldChests.forEach { chest ->
                        drawPixelChest(
                            center = Offset(chest.xNorm * size.width, chest.yNorm * size.height),
                            sizePx = 22.dp.toPx(),
                            style = chest.style,
                            isOpen = chest.id in state.openedChestIds
                        )
                    }

                    // Crisp Overworld Pixel NPC Sprites
                    npcs.forEach { npc ->
                        val bobY = sin(ambientClock * 2.8f + npc.xNorm * 10f) * 1.5.dp.toPx()
                        val c = Offset(npc.xNorm * size.width, npc.yNorm * size.height + bobY)
                        drawOverworldNpcSprite(npc = npc, center = c, unitPx = 11.dp.toPx())
                    }

                    // Unresolved Lost Paradox Kids (Animated Pixel Kid Sprites)
                    unresolvedKids.forEach { kid ->
                        val bobY = sin(ambientClock * 3.2f + kid.xNorm * 8f) * 2.dp.toPx()
                        val c = Offset(kid.xNorm * size.width, kid.yNorm * size.height + bobY)
                        drawOverworldKidSprite(kid = kid, center = c, unitPx = 10.dp.toPx(), isRescued = false)
                    }

                    // Kids already safely brought to Normal World
                    safeNormalKids.forEachIndexed { idx, kid ->
                        val c = Offset((0.36f + idx * 0.08f) * size.width, 0.56f * size.height)
                        drawOverworldKidSprite(kid = kid, center = c, unitPx = 9.5.dp.toPx(), isRescued = true)
                    }

                    // Crisp Pixel Boss Sprites in the Overworld
                    bosses.forEach { boss ->
                        val bobY = sin(ambientClock * 2.4f + boss.yNorm * 7f) * 2.dp.toPx()
                        val c = Offset(boss.xNorm * size.width, boss.yNorm * size.height + bobY)
                        val isFriend = boss.id in state.friendAllies
                        drawCircle(
                            color = (if (isFriend) HpBarGreen else boss.accentColor).copy(alpha = 0.28f),
                            radius = 20.dp.toPx(),
                            center = c
                        )
                        drawUndertaleBossPixelSprite(
                            enemyId = boss.id,
                            center = c,
                            sizePx = 34.dp.toPx(),
                            isHitFlash = false,
                            isSpareReady = isFriend
                        )
                    }

                    // Overworld Chasing Normal Monsters (Touch = Instant Battle!)
                    worldRoamers.forEach { roamer ->
                        val profile = EnemyRoster.byId(roamer.enemyId)
                        val bobY = sin(ambientClock * if (roamer.isChasing) 9f else 3f) * 1.8.dp.toPx()
                        val mc = Offset(roamer.xNorm * size.width, roamer.yNorm * size.height + bobY)
                        drawOverworldChasingMonster(
                            enemyId = roamer.enemyId,
                            center = mc,
                            sizePx = 28.dp.toPx(),
                            accentColor = profile.accentColor,
                            canBefriend = profile.canBefriend,
                            isAlreadyFriend = roamer.enemyId in state.friendAllies,
                            isChasing = roamer.isChasing,
                            ambientClock = ambientClock
                        )
                    }
                } else {
                    // Glitched Void: disappearing ghosts + Echo-Loka (locked until journey ends!)
                    state.glitchGhosts.forEach { ghost ->
                        val gc = Offset(ghost.xNorm * size.width, ghost.yNorm * size.height)
                        val alpha = (ghost.ttlSec / 1.5f).coerceIn(0.15f, 0.85f)
                        drawCircle(color = Color.White.copy(alpha = alpha * 0.6f), radius = 8.dp.toPx(), center = Offset(gc.x, gc.y - 10.dp.toPx()))
                        drawRect(color = GlitchCyan.copy(alpha = alpha * 0.5f), topLeft = Offset(gc.x - 7.dp.toPx(), gc.y - 2.dp.toPx()), size = Size(14.dp.toPx(), 16.dp.toPx()))
                    }

                    val (ex, ey) = state.echoLokaPos
                    val ec = Offset(ex * size.width, ey * size.height)
                    val echoUnlocked = state.isJourneyCompleteForEcho
                    drawCircle(
                        color = (if (echoUnlocked) GlitchMagenta else DangerRed).copy(alpha = 0.28f),
                        radius = 22.dp.toPx(),
                        center = ec
                    )
                    if (!echoUnlocked) {
                        drawCircle(
                            color = DangerRed.copy(alpha = 0.72f),
                            radius = 24.dp.toPx(),
                            center = ec,
                            style = Stroke(2.2f)
                        )
                    }
                    drawUndertaleBossPixelSprite(
                        enemyId = EnemyId.ECHO_LOKA,
                        center = ec,
                        sizePx = 36.dp.toPx(),
                        isHitFlash = false,
                        isSpareReady = echoUnlocked && EnemyId.ECHO_LOKA in state.friendAllies
                    )
                }

                // Escorted kids currently following Loka (only until reaching Normal World!)
                state.helpedKidsList.forEachIndexed { idx, kid ->
                    val offsetX = (-20f - idx * 16f).dp.toPx()
                    val offsetY = (12f + cos(ambientClock * 5f + idx) * 2f).dp.toPx()
                    val kc = Offset(px * size.width + offsetX, py * size.height + offsetY)
                    drawOverworldKidSprite(kid = kid, center = kc, unitPx = 7.5.dp.toPx(), isRescued = false)
                }

                drawLokaOverworldSprite(
                    center = Offset(px * size.width, py * size.height),
                    unit = 11.dp.toPx(),
                    isGlitched = world == WorldId.GLITCHED_WORLD
                )
            }

            // Top Map Legend OR Glitched Void Random Past Memory Overlay!
            if (world == WorldId.GLITCHED_WORLD) {
                val mem = state.activeGlitchMemory
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(10.dp)
                        .fillMaxWidth(0.92f)
                        .background(Color(0xD90E0C12), RoundedCornerShape(6.dp))
                        .border(1.5.dp, GlitchCyan.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (lang == AppLanguage.AR) mem.tagAr else mem.tagEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = GlitchCyan
                    )
                    Text(
                        text = if (lang == AppLanguage.AR) mem.textAr else mem.textEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ParchmentInk
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tr(
                            lang,
                            "Drag to walk · Ashen ★ Save · Touching Chasing Monsters = Battle · Bosses · Rift",
                            "اسحب للمشي · آشن ★ حفظ · لمس الوحوش المطارِدة = معركة · الزعماء · الدوامة"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentInk
                    )
                }
            }

            if (state.controlScheme != ControlScheme.TOUCH_DRAG) {
                ControllerPadOverlay(
                    controlScheme = state.controlScheme,
                    accentColor = if (world == WorldId.GLITCHED_WORLD) GlitchCyan else SepiaAmber,
                    padSize = 114.dp,
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

        // RIGHT: Compact Undertale-style Black & White Info, Proximity & Bag Bar
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black, RoundedCornerShape(0.dp))
                .border(3.dp, Color.White, RoundedCornerShape(0.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LokaPortraitCanvas(
                        saveCount = if (state.saveMysteryDiscovered) state.saveCounter else 0,
                        isSickGlitched = world == WorldId.GLITCHED_WORLD,
                        isFutureTransformed = state.isFutureTransformed,
                        expression = when {
                            world == WorldId.GLITCHED_WORLD -> PortraitExpression.SURPRISED
                            state.isFutureTransformed -> PortraitExpression.DETERMINED
                            nearAshenSave -> PortraitExpression.HAPPY
                            else -> PortraitExpression.NEUTRAL
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFF0C0C10), RoundedCornerShape(0.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(0.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(world.title.uppercase(), style = MaterialTheme.typography.titleMedium, color = SepiaAmber)
                        Text(
                            text = "${tr(lang, "Soul", "الروح")}: ${world.allowedSouls.joinToString(" & ") { it.title }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = world.allowedSouls.first().color
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0A0E), RoundedCornerShape(0.dp))
                        .border(1.5.dp, if (world == WorldId.GLITCHED_WORLD) GlitchCyan else SepiaAmber, RoundedCornerShape(0.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    val warpText = when {
                        state.teleportControlMastered -> tr(lang, "TELEPORT MASTERED", "التحكم بالانتقال مفعّل")
                        world == WorldId.GLITCHED_WORLD -> tr(
                            lang,
                            "PAST MEMORIES · WARP IN ${"%.1f".format(state.glitchedCountdownSec)}s",
                            "ذكريات الماضي · انتقال خلال ${"%.1f".format(state.glitchedCountdownSec)}ث"
                        )
                        else -> tr(
                            lang,
                            "RANDOM PARADOX WARP: ${"%.0f".format(state.randomWarpCountdownSec)}s",
                            "انتقال عشوائي خلال: ${"%.0f".format(state.randomWarpCountdownSec)}ث"
                        )
                    }
                    Text(
                        text = warpText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (world == WorldId.GLITCHED_WORLD) GlitchCyan else SepiaGold
                    )
                }

                state.worldBannerMessage?.let { msg ->
                    Text(text = "* $msg", style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 2)
                }
            }

            // Proximity Interaction Box (Sharp Undertale Border)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF09090D), RoundedCornerShape(0.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(0.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when {
                    nearKid != null -> {
                        val kName = if (lang == AppLanguage.AR) nearKid.nameAr else nearKid.name
                        val isRescued = nearKid.id in state.rescuedToNormalKidIds
                        Text("$kName · ${tr(lang, "Paradox Kid", "طفل مفقود")}", style = MaterialTheme.typography.titleMedium, color = nearKid.accentColor)
                        Text(
                            text = if (isRescued) {
                                tr(lang, "Safely back in the Normal World!", "وصل بأمان إلى العالم الطبيعي!")
                            } else {
                                tr(lang, "Help (escort to Normal World) or Kill?", "تساعده للعالم الطبيعي أم تقتله؟")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isRescued) HpBarGreen else MercyYellow
                        )
                        Button(
                            onClick = { activeKidDialog = nearKid },
                            colors = ButtonDefaults.buttonColors(containerColor = nearKid.accentColor, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("explore_help_kid_button")
                        ) {
                            Text(
                                text = if (isRescued) tr(lang, "TALK", "تحدث") else tr(lang, "APPROACH KID", "اقترب من الطفل"),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    nearChest != null -> {
                        val cName = if (lang == AppLanguage.AR) nearChest.nameAr else nearChest.nameEn
                        val opened = nearChest.id in state.openedChestIds
                        Text(cName, style = MaterialTheme.typography.titleMedium, color = MercyYellow)
                        Text(
                            text = if (opened) tr(lang, "Already opened!", "تم فتح الصندوق بالفعل!") else tr(lang, "Treasure inside! Open to claim supplies.", "يحتوي على إمدادات! افتحه الآن."),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (opened) MutedSepiaInk else HpBarGreen
                        )
                        if (!opened) {
                            Button(
                                onClick = { onOpenChest(nearChest.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MercyYellow, contentColor = ObsidianBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("explore_open_chest_button")
                            ) {
                                Text(tr(lang, "OPEN CHEST", "افتح الصندوق"), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    nearBoss != null -> {
                        val isEchoLocked = nearBoss.id == EnemyId.ECHO_LOKA && !state.isJourneyCompleteForEcho
                        Text(nearBoss.name, style = MaterialTheme.typography.titleMedium, color = if (isEchoLocked) DangerRed else nearBoss.accentColor)
                        Text(
                            text = if (isEchoLocked) {
                                tr(
                                    lang,
                                    "LOCKED FINAL ENEMY (${state.resolvedBossCount}/${state.totalStandardBosses} bosses) — Finish your journey first!",
                                    "عدو أخير مقفل (${state.resolvedBossCount}/${state.totalStandardBosses} زعماء) — أنهِ رحلتك أولاً!"
                                )
                            } else {
                                nearBoss.raceTitle
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEchoLocked) DangerRed else MutedSepiaInk
                        )
                        Button(
                            onClick = { onEngageBoss(nearBoss.id) },
                            enabled = !isEchoLocked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEchoLocked) Color(0xFF27272A) else nearBoss.accentColor,
                                contentColor = ObsidianBg,
                                disabledContainerColor = Color(0xFF1F1F26),
                                disabledContentColor = Color(0xFF9CA3AF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("explore_engage_boss_button")
                        ) {
                            Text(
                                text = when {
                                    isEchoLocked -> tr(lang, "LOCKED UNTIL JOURNEY ENDS", "مقفل حتى تنتهي رحلتك")
                                    nearBoss.id == EnemyId.ECHO_LOKA -> tr(lang, "FINAL BATTLE: ECHO-LOKA", "المعركة الأخيرة: ECHO-LOKA")
                                    nearBoss.id in state.friendAllies -> tr(lang, "SPAR / VISIT", "تدريب / زيارة")
                                    else -> tr(lang, "ENCOUNTER", "مواجهة")
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    nearLandmark != null -> {
                        Text("${nearLandmark.kind.label}: ${nearLandmark.name}", style = MaterialTheme.typography.titleMedium, color = SepiaGold)
                        Text(nearLandmark.questTitle, style = MaterialTheme.typography.labelSmall, color = MutedSepiaInk)
                        Button(
                            onClick = { activeLandmarkDialog = nearLandmark },
                            colors = ButtonDefaults.buttonColors(containerColor = SepiaAmber, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("explore_enter_landmark_button")
                        ) {
                            Text(tr(lang, "ENTER", "دخول"), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    nearNpc != null -> {
                        Text("${nearNpc.name} (${nearNpc.race})", style = MaterialTheme.typography.titleMedium, color = nearNpc.accentColor)
                        Button(
                            onClick = { activeNpcDialog = nearNpc },
                            colors = ButtonDefaults.buttonColors(containerColor = SepiaAmber, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("explore_talk_npc_button")
                        ) {
                            Text(tr(lang, "TALK", "تحدث"), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    nearAshenSave -> {
                        Text(tr(lang, "ASHEN · SAVE POINT ★", "آشن · نقطة الحفظ ★"), style = MaterialTheme.typography.titleMedium, color = MercyYellow)
                        Text(
                            text = tr(
                                lang,
                                "* Ashen: \"Hey Loka! Stand by me anytime—I'll save your timeline!\"",
                                "* آشن: «أهلاً لوكا! اقترب مني في أي وقت وسأحفظ لك تقدمك!»"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentInk
                        )
                        Button(
                            onClick = onSaveGame,
                            colors = ButtonDefaults.buttonColors(containerColor = MercyYellow, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("explore_save_star_button")
                        ) {
                            Text(tr(lang, "SAVE WITH ASHEN ★", "احفظ مع آشن ★"), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    else -> {
                        Text(
                            text = if (world.hasLandmarks) {
                                tr(
                                    lang,
                                    "Bosses: ${state.resolvedBossCount}/${state.totalStandardBosses} · Friends: ${state.friendAllies.size} · Escorting: ${state.escortingKidIds.size}\nWalk near Ashen ★ to Save, dodge chasing Monsters, or meet Bosses.",
                                    "الزعماء: ${state.resolvedBossCount}/${state.totalStandardBosses} · الأصدقاء: ${state.friendAllies.size} · المرافقون: ${state.escortingKidIds.size}\nاقترب من آشن ★ للحفظ، واحذر الوحوش التي تطاردك!"
                                )
                            } else {
                                if (state.isJourneyCompleteForEcho) {
                                    tr(
                                        lang,
                                        "Journey finished! Echo-Loka awaits in the void as your FINAL battle.",
                                        "انتهت رحلتك! ينتظرك Echo-Loka هنا كآخر عدو تواجهه."
                                    )
                                } else {
                                    tr(
                                        lang,
                                        "Past memories echo here. Echo-Loka stays locked (${state.resolvedBossCount}/${state.totalStandardBosses} bosses) until your journey ends.",
                                        "ذكريات الماضي تتردد هنا. Echo-Loka مقفل (${state.resolvedBossCount}/${state.totalStandardBosses} زعماء) حتى تنهي رحلتك!"
                                    )
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSepiaInk
                        )
                    }
                }
            }

            // Bottom Row: Route Button + Bag/Armor + Language + Mute
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                if (unlockedRoute != null) {
                    Button(
                        onClick = { onOpenRouteFinale(unlockedRoute) },
                        colors = ButtonDefaults.buttonColors(containerColor = unlockedRoute.badgeColor, contentColor = ObsidianBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("explore_route_finale_button")
                    ) {
                        Text("${tr(lang, "FINALE", "النهاية")}: ${unlockedRoute.title}", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showBagDialog = true },
                        border = androidx.compose.foundation.BorderStroke(1.dp, SepiaAmber),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("explore_bag_button")
                    ) {
                        Icon(Icons.Default.Backpack, contentDescription = null, tint = SepiaAmber, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(tr(lang, "BAG", "حقيبة"), style = MaterialTheme.typography.labelSmall, color = SepiaAmber)
                    }
                    OutlinedButton(
                        onClick = onToggleControls,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MercyYellow),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("explore_controls_button")
                    ) {
                        Icon(Icons.Default.Gamepad, contentDescription = "Switch controls", tint = MercyYellow, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(if (lang == AppLanguage.AR) state.controlScheme.labelAr else state.controlScheme.label, style = MaterialTheme.typography.labelSmall, color = MercyYellow)
                    }
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SepiaDarkBorder),
                        modifier = Modifier.height(44.dp).testTag("explore_language_button")
                    ) {
                        Text(lang.buttonLabel, style = MaterialTheme.typography.labelSmall, color = SepiaGold)
                    }
                    IconButton(onClick = onToggleMute) {
                        Icon(
                            imageVector = if (state.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Mute sound",
                            tint = SepiaAmber
                        )
                    }
                }
            }
        }
    }

    // Lost Kid Choice Dialog: Help (Escort to Normal World ONLY) OR Kill
    activeKidDialog?.let { kid ->
        val alreadyRescued = kid.id in state.rescuedToNormalKidIds
        val kName = if (lang == AppLanguage.AR) kid.nameAr else kid.name
        AlertDialog(
            onDismissRequest = { activeKidDialog = null },
            containerColor = EspressoSurface,
            title = { Text(kName, style = MaterialTheme.typography.titleMedium, color = kid.accentColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (alreadyRescued) {
                        Text(
                            text = tr(
                                lang,
                                "\"Thanks for getting me back to the Normal World, Loka! I can't follow you through the rest of your dangerous journey, so stay safe!\"",
                                "«شكراً لإيصالي إلى العالم الطبيعي يا لوكا! سأبقى هنا بأمان ولن أكمل بقية الرحلة معك، اعتنِ بنفسك!»"
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = ParchmentInk
                        )
                    } else {
                        Text(
                            text = if (lang == AppLanguage.AR) kid.pleaAr else kid.pleaEn,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ParchmentInk
                        )
                        Text(
                            text = "${tr(lang, "If helped (until Normal World)", "إذا ساعدته (حتى العالم الطبيعي فقط)")}: ${if (lang == AppLanguage.AR) kid.buffAr else kid.buffEn}",
                            style = MaterialTheme.typography.labelMedium,
                            color = HpBarGreen
                        )
                    }
                }
            },
            confirmButton = {
                if (!alreadyRescued) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onKillKid(kid.id)
                                activeKidDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = ParchmentInk),
                            modifier = Modifier.testTag("kid_kill_button")
                        ) {
                            Text(tr(lang, "KILL", "اقتل"), style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = {
                                onHelpKid(kid.id)
                                activeKidDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HpBarGreen, contentColor = ObsidianBg),
                            modifier = Modifier.testTag("kid_help_button")
                        ) {
                            Text(tr(lang, "HELP & ESCORT", "ساعده واصطحبه"), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { activeKidDialog = null }) {
                    Text(tr(lang, "CLOSE", "إغلاق"), color = ParchmentInk)
                }
            }
        )
    }

    // Landmark Dialog
    activeLandmarkDialog?.let { lm ->
        val alreadyDone = lm.id in state.completedLandmarkIds
        AlertDialog(
            onDismissRequest = { activeLandmarkDialog = null },
            containerColor = EspressoSurface,
            title = {
                Text(
                    text = "${if (lm.isSecret) "[SECRET] " else ""}${lm.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (lm.isSecret) GlitchCyan else SepiaAmber
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(lm.questTitle, style = MaterialTheme.typography.titleLarge, color = ParchmentInk)
                    Text(lm.questSummary, style = MaterialTheme.typography.bodyMedium, color = ParchmentInk)
                    Text("Reward: ${lm.rewardLabel}", style = MaterialTheme.typography.labelMedium, color = SepiaGold)
                    if (alreadyDone) Text(tr(lang, "Already completed!", "مكتمل بالفعل!"), style = MaterialTheme.typography.labelSmall, color = HpBarGreen)
                }
            },
            confirmButton = {
                if (!alreadyDone) {
                    Button(
                        onClick = {
                            onCompleteLandmark(lm.id)
                            activeLandmarkDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SepiaAmber, contentColor = ObsidianBg)
                    ) {
                        Text(tr(lang, "COMPLETE QUEST", "أكمل المهمة"), style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { activeLandmarkDialog = null }) {
                    Text(tr(lang, "IGNORE / LEAVE", "تجاهل / مغادرة"), color = MutedSepiaInk)
                }
            }
        )
    }

    // NPC Dialog
    activeNpcDialog?.let { npc ->
        val claimed = npc.id in state.claimedNpcEvents
        AlertDialog(
            onDismissRequest = { activeNpcDialog = null },
            containerColor = EspressoSurface,
            title = { Text("${npc.name} · ${npc.race}", style = MaterialTheme.typography.titleMedium, color = npc.accentColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(npc.dialogue, style = MaterialTheme.typography.bodyLarge, color = ParchmentInk)
                    if (npc.eventTitle != null && !claimed) Text("Event: ${npc.eventTitle}", style = MaterialTheme.typography.labelMedium, color = SepiaGold)
                }
            },
            confirmButton = {
                if (npc.eventRewardItemId != null && !claimed) {
                    Button(
                        onClick = {
                            onClaimNpcEvent(npc.id)
                            activeNpcDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SepiaAmber, contentColor = ObsidianBg)
                    ) {
                        Text(tr(lang, "ACCEPT GIFT", "اقبل الهدية"), style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { activeNpcDialog = null }) {
                    Text(tr(lang, "CLOSE", "إغلاق"), color = ParchmentInk)
                }
            }
        )
    }

    // Bag, Armor, Lost Kid Buffs & Controls Modal
    if (showBagDialog) {
        AlertDialog(
            onDismissRequest = { showBagDialog = false },
            containerColor = EspressoSurface,
            title = { Text(tr(lang, "LOKA'S BAG & STATUS", "حقيبة وحالة لوكا"), style = MaterialTheme.typography.titleMedium, color = SepiaGold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tr(
                            lang,
                            "Friends: ${state.friendAllies.size} · Slain: ${state.defeatedEnemies.size} · Escorting: ${state.escortingKidIds.size} · Returned Safe: ${state.rescuedToNormalKidIds.size} · Killed: ${state.killedKidIds.size}",
                            "الأصدقاء: ${state.friendAllies.size} · المهزومون: ${state.defeatedEnemies.size} · المرافقون الآن: ${state.escortingKidIds.size} · عادوا بأمان: ${state.rescuedToNormalKidIds.size} · قُتلوا: ${state.killedKidIds.size}"
                        ) + if (state.saveMysteryDiscovered) " · Save: ${state.saveCounter}/8 (${state.secretTapesFound}/${state.totalSecretTapes})" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentInk
                    )
                    if (state.helpedKidsList.isNotEmpty()) {
                        Text(
                            text = tr(lang, "Active Escort Buffs (until Normal World):", "دعم المرافقين (حتى العالم الطبيعي):") + " " +
                                state.helpedKidsList.joinToString(" | ") { if (lang == AppLanguage.AR) it.buffAr else it.buffEn },
                            style = MaterialTheme.typography.labelSmall,
                            color = HpBarGreen
                        )
                    }
                    Text(
                        text = "${tr(lang, "Weapon", "السلاح")}: ${if (lang == AppLanguage.AR) state.equippedWeapon.nameAr else state.equippedWeapon.name} (+${state.equippedWeapon.attackBonus} ATK)",
                        style = MaterialTheme.typography.labelSmall,
                        color = state.equippedWeapon.accentColor
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WeaponCatalog.all.filter { it.id in state.unlockedWeaponIds }.forEach { weapon ->
                            FilterChip(
                                selected = weapon.id == state.equippedWeaponId,
                                onClick = { onEquipWeapon(weapon.id) },
                                label = { Text(if (lang == AppLanguage.AR) weapon.nameAr else weapon.name, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = weapon.accentColor.copy(alpha = 0.24f),
                                    selectedLabelColor = weapon.accentColor,
                                    labelColor = MutedSepiaInk
                                )
                            )
                        }
                    }
                    Text(
                        text = "${tr(lang, "Armor", "الدرع")}: ${state.equippedArmor.name} (+${state.equippedArmor.defenseBonus + state.kidDefBonus} DEF, +${state.equippedArmor.maxHpBonus + state.kidHpBonus} HP)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SepiaAmber
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ArmorCatalog.all.filter { it.id in state.unlockedArmorIds }.forEach { armor ->
                            FilterChip(
                                selected = armor.id == state.equippedArmorId,
                                onClick = { onEquipArmor(armor.id) },
                                label = { Text(armor.name, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SepiaAmber.copy(alpha = 0.22f),
                                    selectedLabelColor = SepiaAmber,
                                    labelColor = MutedSepiaInk
                                )
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onToggleControls) {
                            Text(
                                text = if (lang == AppLanguage.AR) state.controlScheme.labelAr else state.controlScheme.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentInk
                            )
                        }
                        OutlinedButton(onClick = {
                            showBagDialog = false
                            onWatchStoryIntro()
                        }) {
                            Text(tr(lang, "Replay Story", "إعادة القصة"), style = MaterialTheme.typography.labelSmall, color = SepiaAmber)
                        }
                        OutlinedButton(
                            onClick = {
                                showBagDialog = false
                                confirmResetDialog = true
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                            modifier = Modifier.testTag("bag_reset_save_button")
                        ) {
                            Text(tr(lang, "New Game", "لعبة جديدة"), style = MaterialTheme.typography.labelSmall, color = DangerRed)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBagDialog = false }) {
                    Text(tr(lang, "CLOSE", "إغلاق"), color = SepiaAmber)
                }
            }
        )
    }

    if (confirmResetDialog) {
        AlertDialog(
            onDismissRequest = { confirmResetDialog = false },
            containerColor = EspressoSurface,
            title = { Text(tr(lang, "START A NEW TIMELINE?", "بدء خط زمني جديد؟"), style = MaterialTheme.typography.titleMedium, color = DangerRed) },
            text = {
                Text(
                    text = tr(
                        lang,
                        "This will erase your current save file, friends, weapons, and unlocked routes and restart from the Ruins Story.",
                        "سيؤدي هذا لمسح ملف الحفظ الحالي والأصدقاء والأسلحة والمسارات المفتوحة وإعادة البداية من القصة."
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentInk
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmResetDialog = false
                        onResetSave()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = ParchmentInk),
                    modifier = Modifier.testTag("confirm_reset_save_button")
                ) {
                    Text(tr(lang, "ERASE & RESTART", "مسح وإعادة البداية"), style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmResetDialog = false }) {
                    Text(tr(lang, "CANCEL", "إلغاء"), color = ParchmentInk)
                }
            }
        )
    }
}

private fun DrawScope.drawWorldTerrain(world: WorldId, ambientClock: Float) {
    val cols = 14
    val rows = 8
    val tileW = size.width / cols
    val tileH = size.height / rows

    when (world) {
        WorldId.NORMAL_WORLD -> {
            // Undertale-style checker grass pixel tiles + paved sidewalk crossroads + pixel flower & grass tufts
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val color = if ((r + c) % 2 == 0) Color(0xFF3E6B38) else Color(0xFF365E30)
                    val tx = c * tileW
                    val ty = r * tileH
                    drawRect(color, Offset(tx, ty), Size(tileW + 0.5f, tileH + 0.5f))
                    if ((r * 7 + c * 3) % 11 == 0) {
                        drawRect(Color(0xFF16A34A), Offset(tx + tileW * 0.35f, ty + tileH * 0.45f), Size(4.dp.toPx(), 3.dp.toPx()))
                        drawRect(Color(0xFFFACC15), Offset(tx + tileW * 0.42f, ty + tileH * 0.35f), Size(4.dp.toPx(), 4.dp.toPx()))
                    } else if ((r * 3 + c * 5) % 9 == 0) {
                        drawRect(Color(0xFF4ADE80).copy(alpha = 0.35f), Offset(tx + tileW * 0.25f, ty + tileH * 0.6f), Size(5.dp.toPx(), 2.5.dp.toPx()))
                    }
                }
            }
            // Stepped stone roads + cobblestone seams
            drawRect(color = Color(0xFF525862), topLeft = Offset(0f, size.height * 0.44f), size = Size(size.width, size.height * 0.12f))
            drawRect(color = Color(0xFF525862), topLeft = Offset(size.width * 0.44f, 0f), size = Size(size.width * 0.12f, size.height))
            drawRect(color = Color(0xFF8C929C), topLeft = Offset(0f, size.height * 0.44f), size = Size(size.width, 3.dp.toPx()))
            drawRect(color = Color(0xFF8C929C), topLeft = Offset(0f, size.height * 0.56f), size = Size(size.width, 3.dp.toPx()))
            drawRect(color = Color(0xFF8C929C), topLeft = Offset(size.width * 0.44f, 0f), size = Size(3.dp.toPx(), size.height))
            drawRect(color = Color(0xFF8C929C), topLeft = Offset(size.width * 0.56f, 0f), size = Size(3.dp.toPx(), size.height))
            for (i in 0..10) {
                drawRect(Color(0xFF6E7582), Offset(size.width * (i * 0.095f + 0.02f), size.height * 0.495f), Size(10.dp.toPx(), 3.dp.toPx()))
            }

            val trees = listOf(0.10f to 0.40f, 0.35f to 0.14f, 0.64f to 0.16f, 0.88f to 0.40f, 0.36f to 0.86f, 0.64f to 0.86f)
            trees.forEach { (tx, ty) ->
                drawPixelTree(Offset(tx * size.width, ty * size.height), Color(0xFF23491F), Color(0xFF3E7B37), Color(0xFF5A3D28))
            }
        }

        WorldId.TRI_RACE_REALM -> {
            // Warm sandstone dunes + pebble clusters + vivid cyan crystalline trees
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val color = if ((r + c) % 2 == 0) Color(0xFFD9C39D) else Color(0xFFCBB186)
                    val tx = c * tileW
                    val ty = r * tileH
                    drawRect(color, Offset(tx, ty), Size(tileW + 0.5f, tileH + 0.5f))
                    if ((r * 5 + c * 9) % 7 == 0) {
                        drawRect(Color(0xFF9C7B50), Offset(tx + tileW * 0.35f, ty + tileH * 0.5f), Size(5.dp.toPx(), 4.dp.toPx()))
                        drawRect(Color(0xFFE7D6B5), Offset(tx + tileW * 0.42f, ty + tileH * 0.44f), Size(3.dp.toPx(), 2.dp.toPx()))
                    }
                }
            }
            val cyanTrees = listOf(0.12f to 0.38f, 0.38f to 0.15f, 0.60f to 0.15f, 0.88f to 0.44f, 0.42f to 0.86f, 0.62f to 0.88f)
            cyanTrees.forEach { (tx, ty) ->
                drawPixelTree(Offset(tx * size.width, ty * size.height), Color(0xFF0E7490), Color(0xFF22D3EE), Color(0xFF6B4423))
            }
        }

        WorldId.SPLIT_REALM -> {
            // Left Half: Angels' purple-pink stone tiles & emerald pixel trees
            // Right Half: Devils' obsidian-basalt wastes & crimson pixel trees
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val isAngelSide = c < cols / 2
                    val color = if (isAngelSide) {
                        if ((r + c) % 2 == 0) Color(0xFF7B409E) else Color(0xFFA84784)
                    } else {
                        if ((r + c) % 2 == 0) Color(0xFF5D402C) else Color(0xFF51555E)
                    }
                    val tx = c * tileW
                    val ty = r * tileH
                    drawRect(color, Offset(tx, ty), Size(tileW + 0.5f, tileH + 0.5f))
                    if ((r + c) % 5 == 0) {
                        drawRect(
                            color = (if (isAngelSide) Color(0xFFF9A8D4) else Color(0xFFEF4444)).copy(alpha = 0.22f),
                            topLeft = Offset(tx + tileW * 0.3f, ty + tileH * 0.4f),
                            size = Size(5.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }
            }
            // Pulsing Paradox rift barrier down the middle
            val borderPulse = 0.45f + 0.35f * sin(ambientClock * 3.5f)
            drawLine(GlitchCyan.copy(alpha = borderPulse * 0.45f), Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height), strokeWidth = 10f)
            drawLine(Color.White, Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height), strokeWidth = 3.5f)
            listOf(0.10f to 0.42f, 0.36f to 0.18f, 0.38f to 0.84f).forEach { (tx, ty) ->
                drawPixelTree(Offset(tx * size.width, ty * size.height), Color(0xFF15803D), Color(0xFF4ADE80), Color(0xFF4A2E1B))
            }
            listOf(0.64f to 0.16f, 0.90f to 0.44f, 0.62f to 0.86f).forEach { (tx, ty) ->
                drawPixelTree(Offset(tx * size.width, ty * size.height), Color(0xFF991B1B), Color(0xFFEF4444), Color(0xFF2B2523))
            }
        }

        WorldId.GLITCHED_WORLD -> {
            drawRect(color = Color.Black)
            val r = Random((ambientClock * 8f).toInt())
            repeat(14) {
                val y = r.nextFloat() * size.height
                drawRect(
                    color = if (it % 2 == 0) GlitchCyan.copy(alpha = 0.22f) else GlitchMagenta.copy(alpha = 0.18f),
                    topLeft = Offset(r.nextFloat() * size.width * 0.35f, y),
                    size = Size(size.width * 0.62f, 4.dp.toPx())
                )
            }
        }
    }
}

private fun DrawScope.drawPixelTree(
    center: Offset,
    canopyDark: Color,
    canopyLight: Color,
    trunkColor: Color
) {
    val u = 6.dp.toPx()
    // Ground shadow
    drawOval(Color.Black.copy(alpha = 0.36f), Offset(center.x - u * 1.9f, center.y + u * 1.0f), Size(u * 3.8f, u * 0.9f))
    // Shaded bark trunk
    drawRect(trunkColor, Offset(center.x - u * 0.55f, center.y - u * 0.1f), Size(u * 1.1f, u * 1.55f))
    drawRect(Color.Black.copy(alpha = 0.25f), Offset(center.x + u * 0.15f, center.y - u * 0.1f), Size(u * 0.4f, u * 1.55f))
    // Multi-tiered crisp Undertale-style pixel foliage
    drawRect(Color(0xFF140E0A), Offset(center.x - u * 2.0f, center.y - u * 1.75f), Size(u * 4.0f, u * 2.25f))
    drawRect(Color(0xFF140E0A), Offset(center.x - u * 1.4f, center.y - u * 2.45f), Size(u * 2.8f, u * 1.0f))
    drawRect(canopyDark, Offset(center.x - u * 1.85f, center.y - u * 1.6f), Size(u * 3.7f, u * 1.95f))
    drawRect(canopyDark, Offset(center.x - u * 1.25f, center.y - u * 2.3f), Size(u * 2.5f, u * 0.9f))
    // Leaf highlight clusters
    drawRect(canopyLight, Offset(center.x - u * 1.25f, center.y - u * 1.95f), Size(u * 2.1f, u * 1.15f))
    drawRect(canopyLight, Offset(center.x - u * 0.80f, center.y - u * 2.15f), Size(u * 1.4f, u * 0.45f))
}
