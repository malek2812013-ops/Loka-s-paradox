package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.audio.RetroSoundEngine
import com.example.data.GameSaveStorage
import com.example.data.SavedGameSnapshot
import com.example.model.AppLanguage
import com.example.model.AppScreen
import com.example.model.ArmorCatalog
import com.example.model.ArmorEquipment
import com.example.model.ControlScheme
import com.example.model.DefaultLoreRepository
import com.example.model.EnemyId
import com.example.model.EnemyRoster
import com.example.model.GlitchedMemory
import com.example.model.LoreSlide
import com.example.model.RouteType
import com.example.model.WeaponCatalog
import com.example.model.WeaponEquipment
import com.example.model.WorldContentCatalog
import com.example.model.WorldId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.hypot
import kotlin.random.Random

data class GlitchGhost(
    val id: Int,
    val xNorm: Float,
    val yNorm: Float,
    val ttlSec: Float
)

data class RoamingMonster(
    val uid: String,
    val enemyId: EnemyId,
    val worldId: WorldId,
    val xNorm: Float,
    val yNorm: Float,
    val homeX: Float,
    val homeY: Float,
    val isChasing: Boolean = false
)

data class AppUiState(
    val language: AppLanguage = AppLanguage.EN,
    val currentScreen: AppScreen = AppScreen.LORE_INTRO,
    val loreSlides: List<LoreSlide> = DefaultLoreRepository.defaultSlides(),
    val sepiaIntensity: Float = 0.85f,
    val glitchIntensity: Float = 0.90f,
    val controlScheme: ControlScheme = ControlScheme.TOUCH_DRAG,
    val isMuted: Boolean = false,

    // Ruins Opening Tutorial Area
    val ruinsPlayerX: Float = 0.14f,
    val ruinsPlayerY: Float = 0.54f,
    val ruinsSwitchActivated: Boolean = false,
    val ruinsDummyPracticed: Boolean = false,

    // World & Random Paradox Exploration (Zero player control over destination!)
    val activeWorld: WorldId = WorldId.NORMAL_WORLD,
    val playerMapX: Float = 0.50f,
    val playerMapY: Float = 0.50f,
    val paradoxRiftPos: Pair<Float, Float> = 0.50f to 0.48f,
    val randomWarpCountdownSec: Float = 45f,
    val glitchedCountdownSec: Float = 10f,
    val glitchGhosts: List<GlitchGhost> = emptyList(),
    val activeGlitchMemory: GlitchedMemory = WorldContentCatalog.glitchedMemories.first(),
    val glitchMemoryCycleSec: Float = 0f,
    val echoLokaPos: Pair<Float, Float> = 0.50f to 0.28f,
    val roamingMonsters: List<RoamingMonster> = emptyList(),
    val roamGraceTimerSec: Float = 2.5f,
    val worldBannerMessage: String? = null,

    // Progression, Friends/Allies, Lost Kids (Escorted until Normal World vs Rescued vs Killed), Quests, Armors & Inventory
    val selectedEnemyId: EnemyId = EnemyId.VEX,
    val battleSessionId: Long = 0L,
    val battleFromRuins: Boolean = false,
    val defeatedEnemies: Set<EnemyId> = emptySet(),
    val friendAllies: Set<EnemyId> = emptySet(),
    val escortingKidIds: Set<String> = emptySet(),
    val rescuedToNormalKidIds: Set<String> = emptySet(),
    val killedKidIds: Set<String> = emptySet(),
    val completedLandmarkIds: Set<String> = emptySet(),
    val claimedNpcEvents: Set<String> = emptySet(),
    val openedChestIds: Set<String> = emptySet(),
    val unlockedArmorIds: Set<String> = setOf("casual_jacket"),
    val equippedArmorId: String = "casual_jacket",
    val unlockedWeaponIds: Set<String> = setOf("worn_stick"),
    val equippedWeaponId: String = "worn_stick",
    val itemCounts: Map<String, Int> = mapOf(
        "chrono_shard" to 2,
        "sepia_ration" to 1,
        "save_anchor" to 1
    ),

    // 4 Routes & Finale State
    val activeFinaleRoute: RouteType = RouteType.MOST_EXPECTED,
    val unlockedRoutes: Set<RouteType> = emptySet(),
    val isFutureTransformed: Boolean = false,
    val teleportControlMastered: Boolean = false,
    val worldDestroyedEnding: Boolean = false,
    val peaceSacrificeEnding: Boolean = false,

    // Hidden Save mystery counter & Persistence flag
    val saveCounter: Int = 1,
    val hasSavedFile: Boolean = false
) {
    val equippedArmor: ArmorEquipment get() = ArmorCatalog.byId(equippedArmorId)
    val equippedWeapon: WeaponEquipment get() = WeaponCatalog.byId(equippedWeaponId)

    // Only kids currently escorted with Loka (before reaching Normal World) actively grant journey buffs!
    val helpedKidsList get() = WorldContentCatalog.lostKids.filter { it.id in escortingKidIds }
    val kidHpBonus: Int get() = helpedKidsList.sumOf { it.hpBonus }
    val kidDefBonus: Int get() = helpedKidsList.sumOf { it.defBonus }
    val kidAtkBonus: Int get() = helpedKidsList.sumOf { it.atkBonus }
    val kidSpeedMultiplier: Float get() = 1f + helpedKidsList.sumOf { it.speedBonus.toDouble() }.toFloat()

    val secretTapesFound: Int
        get() = WorldContentCatalog.landmarks.count { it.isSecret && it.id in completedLandmarkIds }

    val totalSecretTapes: Int
        get() = WorldContentCatalog.landmarks.count { it.isSecret }

    val saveMysteryDiscovered: Boolean
        get() = secretTapesFound > 0 || teleportControlMastered

    val totalStandardBosses: Int
        get() = EnemyRoster.standardPlayableRoster().size

    val resolvedBossCount: Int
        get() {
            val standardIds = EnemyRoster.standardPlayableRoster().map { it.id }.toSet()
            return standardIds.count { it in friendAllies || it in defeatedEnemies }
        }

    val isJourneyCompleteForEcho: Boolean
        get() = resolvedBossCount >= totalStandardBosses || teleportControlMastered || unlockedRoutes.isNotEmpty()

    val eligibleRoute: RouteType?
        get() {
            val standardIds = EnemyRoster.standardPlayableRoster().map { it.id }.toSet()
            val killedStd = defeatedEnemies.intersect(standardIds)
            val sparedStd = friendAllies.intersect(standardIds)
            val anyKidKilled = killedKidIds.isNotEmpty()
            val anyKidHelped = escortingKidIds.isNotEmpty() || rescuedToNormalKidIds.isNotEmpty()
            return when {
                secretTapesFound >= totalSecretTapes -> RouteType.LOST_TAPES
                sparedStd.size == standardIds.size && killedStd.isEmpty() && !anyKidKilled -> RouteType.PERFECT
                killedStd.size == standardIds.size && sparedStd.isEmpty() && !anyKidHelped -> RouteType.NO_MERCY
                (killedStd.size + sparedStd.size) >= 2 ||
                    (killedStd.size == standardIds.size && anyKidHelped) ||
                    (sparedStd.size == standardIds.size && anyKidKilled) -> RouteType.MOST_EXPECTED
                else -> null
            }
        }
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val saveStorage = GameSaveStorage(application.applicationContext)
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var ghostIdSeq = 1

    init {
        val snap = saveStorage.load()
        if (snap.hasSavedProgress) {
            RetroSoundEngine.isMuted = snap.isMuted
            _uiState.value = AppUiState(
                language = snap.language,
                controlScheme = snap.controlScheme,
                isMuted = snap.isMuted,
                currentScreen = snap.resumeScreen,
                ruinsSwitchActivated = snap.ruinsSwitchActivated,
                ruinsDummyPracticed = snap.ruinsDummyPracticed,
                activeWorld = snap.activeWorld,
                playerMapX = snap.playerMapX,
                playerMapY = snap.playerMapY,
                defeatedEnemies = snap.defeatedEnemies,
                friendAllies = snap.friendAllies,
                escortingKidIds = snap.escortingKidIds,
                rescuedToNormalKidIds = snap.rescuedToNormalKidIds,
                killedKidIds = snap.killedKidIds,
                completedLandmarkIds = snap.completedLandmarkIds,
                claimedNpcEvents = snap.claimedNpcEvents,
                openedChestIds = snap.openedChestIds,
                unlockedArmorIds = snap.unlockedArmorIds,
                equippedArmorId = snap.equippedArmorId,
                unlockedWeaponIds = snap.unlockedWeaponIds,
                equippedWeaponId = snap.equippedWeaponId,
                itemCounts = snap.itemCounts,
                unlockedRoutes = snap.unlockedRoutes,
                activeFinaleRoute = snap.activeFinaleRoute,
                isFutureTransformed = snap.isFutureTransformed,
                teleportControlMastered = snap.teleportControlMastered,
                worldDestroyedEnding = snap.worldDestroyedEnding,
                peaceSacrificeEnding = snap.peaceSacrificeEnding,
                saveCounter = snap.saveCounter,
                roamingMonsters = roamingForWorld(snap.activeWorld),
                roamGraceTimerSec = 2.5f,
                hasSavedFile = true
            )
        }
    }

    private fun roamingForWorld(worldId: WorldId): List<RoamingMonster> {
        return EnemyRoster.normalMonstersForWorld(worldId).map { enemy ->
            RoamingMonster(
                uid = "${worldId.name}_${enemy.id.name}",
                enemyId = enemy.id,
                worldId = worldId,
                xNorm = enemy.xNorm,
                yNorm = enemy.yNorm,
                homeX = enemy.xNorm,
                homeY = enemy.yNorm
            )
        }
    }

    private fun persist(state: AppUiState) {
        saveStorage.save(
            SavedGameSnapshot(
                hasSavedProgress = true,
                language = state.language,
                controlScheme = state.controlScheme,
                isMuted = state.isMuted,
                resumeScreen = state.currentScreen,
                ruinsSwitchActivated = state.ruinsSwitchActivated,
                ruinsDummyPracticed = state.ruinsDummyPracticed,
                activeWorld = state.activeWorld,
                playerMapX = state.playerMapX,
                playerMapY = state.playerMapY,
                defeatedEnemies = state.defeatedEnemies,
                friendAllies = state.friendAllies,
                escortingKidIds = state.escortingKidIds,
                rescuedToNormalKidIds = state.rescuedToNormalKidIds,
                killedKidIds = state.killedKidIds,
                completedLandmarkIds = state.completedLandmarkIds,
                claimedNpcEvents = state.claimedNpcEvents,
                openedChestIds = state.openedChestIds,
                unlockedArmorIds = state.unlockedArmorIds,
                equippedArmorId = state.equippedArmorId,
                unlockedWeaponIds = state.unlockedWeaponIds,
                equippedWeaponId = state.equippedWeaponId,
                itemCounts = state.itemCounts,
                unlockedRoutes = state.unlockedRoutes,
                activeFinaleRoute = state.activeFinaleRoute,
                isFutureTransformed = state.isFutureTransformed,
                teleportControlMastered = state.teleportControlMastered,
                worldDestroyedEnding = state.worldDestroyedEnding,
                peaceSacrificeEnding = state.peaceSacrificeEnding,
                saveCounter = state.saveCounter
            )
        )
    }

    fun toggleLanguage() {
        _uiState.update { state ->
            val next = if (state.language == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN
            state.copy(language = next).also(::persist)
        }
    }

    fun openLoreIntro() {
        _uiState.update { it.copy(currentScreen = AppScreen.LORE_INTRO) }
    }

    fun finishLoreIntro() {
        _uiState.update { state ->
            state.copy(currentScreen = AppScreen.RUINS_TUTORIAL, hasSavedFile = true).also(::persist)
        }
    }

    fun moveRuinsPlayer(dx: Float, dy: Float) {
        _uiState.update { state ->
            if (state.currentScreen != AppScreen.RUINS_TUTORIAL) return@update state
            val nx = (state.ruinsPlayerX + dx * state.kidSpeedMultiplier).coerceIn(0.08f, 0.92f)
            val ny = (state.ruinsPlayerY + dy * state.kidSpeedMultiplier).coerceIn(0.20f, 0.85f)
            state.copy(ruinsPlayerX = nx, ruinsPlayerY = ny)
        }
    }

    fun activateRuinsSwitch() {
        _uiState.update { state ->
            if (state.ruinsSwitchActivated) return@update state
            RetroSoundEngine.playAbility()
            state.copy(ruinsSwitchActivated = true)
        }
    }

    fun startRuinsDummyBattle() {
        _uiState.update { state ->
            state.copy(
                selectedEnemyId = EnemyId.RUINS_DUMMY,
                battleSessionId = state.battleSessionId + 1L,
                battleFromRuins = true,
                activeWorld = WorldId.NORMAL_WORLD,
                currentScreen = AppScreen.BATTLE
            )
        }
    }

    /**
     * Right after exiting the Ruins, Loka encounters the black-and-grey Flowey-like character (Ashen)!
     */
    fun exitRuinsToAshen() {
        RetroSoundEngine.playSoulSwitch(0)
        _uiState.update { it.copy(currentScreen = AppScreen.ASHEN_MEETING) }
    }

    /**
     * After meeting Ashen, the first uncontrollable random paradox warp pulls Loka into a random world!
     */
    fun finishAshenMeetingToRandomWorld() {
        val candidates = listOf(WorldId.NORMAL_WORLD, WorldId.TRI_RACE_REALM, WorldId.SPLIT_REALM)
        val randomFirstWorld = candidates.random()
        RetroSoundEngine.playSoulSwitch(2)
        _uiState.update { state ->
            state.copy(
                activeWorld = randomFirstWorld,
                currentScreen = AppScreen.WORLD_EXPLORE,
                playerMapX = 0.50f,
                playerMapY = 0.54f,
                paradoxRiftPos = randomRiftPosition(),
                randomWarpCountdownSec = 45f,
                glitchedCountdownSec = 10f,
                glitchGhosts = emptyList(),
                roamingMonsters = roamingForWorld(randomFirstWorld),
                roamGraceTimerSec = 2.6f,
                worldBannerMessage = if (state.language == AppLanguage.AR) {
                    "انتقال مفاجئ! سحبتك المفارقة عشوائياً إلى ${randomFirstWorld.title}"
                } else {
                    "Paradox Jump! Pulled randomly into ${randomFirstWorld.title}."
                }
            ).also(::persist)
        }
    }

    fun saveProgressExplicitly(fromAshen: Boolean = false) {
        RetroSoundEngine.playSaveChime()
        _uiState.update { state ->
            val nextSave = if (state.saveCounter >= 8) 1 else state.saveCounter + 1
            val msg = if (fromAshen) {
                if (state.language == AppLanguage.AR) "حفظ آشن تقدمك على الجهاز بنجاح! ★" else "Ashen saved your timeline to device! ★"
            } else {
                if (state.language == AppLanguage.AR) "تم حفظ ملف اللعبة! نجمة المفارقة تملؤك بالعزيمة ★" else "File Saved! The Paradox Star fills you with determination ★"
            }
            state.copy(
                saveCounter = nextSave,
                hasSavedFile = true,
                worldBannerMessage = msg
            ).also(::persist)
        }
    }

    fun resetAllProgress() {
        saveStorage.clear()
        RetroSoundEngine.playGlitchNoise(110)
        _uiState.update { current ->
            AppUiState(
                language = current.language,
                controlScheme = current.controlScheme,
                isMuted = current.isMuted,
                currentScreen = AppScreen.LORE_INTRO
            )
        }
    }

    fun moveOverworldPlayer(dx: Float, dy: Float) {
        _uiState.update { state ->
            if (state.currentScreen != AppScreen.WORLD_EXPLORE) return@update state
            val speed = if (state.activeWorld == WorldId.GLITCHED_WORLD) 1f else state.kidSpeedMultiplier
            val nx = (state.playerMapX + dx * speed).coerceIn(0.07f, 0.93f)
            val ny = (state.playerMapY + dy * speed).coerceIn(0.10f, 0.90f)
            val updated = state.copy(playerMapX = nx, playerMapY = ny)

            // Stepping into the unstable Paradox Rift warps Loka randomly (zero player choice!)
            if (state.activeWorld != WorldId.GLITCHED_WORLD && !state.teleportControlMastered) {
                val (rx, ry) = state.paradoxRiftPos
                if (hypot(nx - rx, ny - ry) < 0.065f) {
                    return@update warpToRandomWorld(updated)
                }
            }
            updated
        }
    }

    fun tickOverworld(dtRaw: Float) {
        val dt = dtRaw.coerceIn(0.001f, 0.05f)
        _uiState.update { state ->
            if (state.currentScreen != AppScreen.WORLD_EXPLORE) return@update state

            if (state.activeWorld == WorldId.GLITCHED_WORLD) {
                val nextTime = if (state.teleportControlMastered) state.glitchedCountdownSec else state.glitchedCountdownSec - dt
                if (!state.teleportControlMastered && nextTime <= 0f) {
                    val otherWorlds = listOf(WorldId.NORMAL_WORLD, WorldId.TRI_RACE_REALM, WorldId.SPLIT_REALM)
                    val dest = otherWorlds.random()
                    RetroSoundEngine.playGlitchNoise(120)
                    val base = state.copy(
                        activeWorld = dest,
                        playerMapX = 0.50f,
                        playerMapY = 0.54f,
                        paradoxRiftPos = randomRiftPosition(),
                        randomWarpCountdownSec = 45f,
                        glitchedCountdownSec = 10f,
                        glitchGhosts = emptyList(),
                        roamingMonsters = roamingForWorld(dest),
                        roamGraceTimerSec = 2.6f,
                        worldBannerMessage = if (state.language == AppLanguage.AR) {
                            "انتقال مفاجئ! انتقلت عشوائياً إلى ${dest.title}."
                        } else {
                            "Paradox warp! Thrown randomly into ${dest.title}."
                        }
                    )
                    return@update applyNormalWorldDropOff(base)
                }

                // Cycle random fragments of Loka's past with whispering sounds every ~2.7s!
                var nextMemCycle = state.glitchMemoryCycleSec + dt
                var nextMem = state.activeGlitchMemory
                if (nextMemCycle >= 2.7f) {
                    nextMemCycle = 0f
                    val others = WorldContentCatalog.glitchedMemories.filter { it.id != state.activeGlitchMemory.id }
                    nextMem = others.random()
                    RetroSoundEngine.playWhisper()
                }

                // Random disappearing ghostly silhouettes
                val updatedGhosts = state.glitchGhosts.mapNotNull { g ->
                    val rem = g.ttlSec - dt
                    if (rem <= 0f) null else g.copy(ttlSec = rem)
                }.toMutableList()

                if (updatedGhosts.size < 5 && Random.nextFloat() < dt * 2.2f) {
                    updatedGhosts += GlitchGhost(
                        id = ghostIdSeq++,
                        xNorm = 0.12f + Random.nextFloat() * 0.76f,
                        yNorm = 0.15f + Random.nextFloat() * 0.70f,
                        ttlSec = 0.7f + Random.nextFloat() * 1.1f
                    )
                }

                return@update state.copy(
                    glitchedCountdownSec = nextTime,
                    glitchGhosts = updatedGhosts,
                    activeGlitchMemory = nextMem,
                    glitchMemoryCycleSec = nextMemCycle
                )
            } else {
                val nextGrace = (state.roamGraceTimerSec - dt).coerceAtLeast(0f)
                val px = state.playerMapX
                val py = state.playerMapY
                val currentRoamers = state.roamingMonsters.ifEmpty { roamingForWorld(state.activeWorld) }
                var collidedEnemy: RoamingMonster? = null
                val movedRoamers = currentRoamers.map { m ->
                    if (m.worldId != state.activeWorld) return@map m
                    val isAlreadyFriend = m.enemyId in state.friendAllies
                    val dx = px - m.xNorm
                    val dy = py - m.yNorm
                    val dist = hypot(dx, dy)

                    if (!isAlreadyFriend && nextGrace <= 0f && dist < 0.056f && collidedEnemy == null) {
                        collidedEnemy = m
                        m.copy(isChasing = true)
                    } else if (!isAlreadyFriend && dist < 0.36f && dist > 0.001f) {
                        val profile = EnemyRoster.byId(m.enemyId)
                        val chaseSpeed = if (profile.canBefriend) 0.130f else 0.158f
                        val nx = (m.xNorm + (dx / dist) * chaseSpeed * dt).coerceIn(0.08f, 0.92f)
                        val ny = (m.yNorm + (dy / dist) * chaseSpeed * dt).coerceIn(0.12f, 0.88f)
                        m.copy(xNorm = nx, yNorm = ny, isChasing = true)
                    } else {
                        val hx = m.homeX - m.xNorm
                        val hy = m.homeY - m.yNorm
                        val hd = hypot(hx, hy)
                        if (hd > 0.015f) {
                            val returnSpeed = 0.08f
                            m.copy(
                                xNorm = m.xNorm + (hx / hd) * returnSpeed * dt,
                                yNorm = m.yNorm + (hy / hd) * returnSpeed * dt,
                                isChasing = false
                            )
                        } else {
                            m.copy(isChasing = false)
                        }
                    }
                }

                if (collidedEnemy != null) {
                    val caught = collidedEnemy!!
                    RetroSoundEngine.playGlitchNoise(55)
                    val remaining = movedRoamers.filterNot { it.uid == caught.uid }
                    val nextSave = if (state.saveCounter >= 8) 1 else state.saveCounter + 1
                    return@update state.copy(
                        roamingMonsters = remaining,
                        roamGraceTimerSec = 2.8f,
                        selectedEnemyId = caught.enemyId,
                        battleSessionId = state.battleSessionId + 1L,
                        battleFromRuins = false,
                        saveCounter = nextSave,
                        currentScreen = AppScreen.BATTLE
                    )
                }

                if (state.teleportControlMastered) {
                    return@update state.copy(
                        roamGraceTimerSec = nextGrace,
                        roamingMonsters = movedRoamers
                    )
                }
                val nextWarp = state.randomWarpCountdownSec - dt
                if (nextWarp <= 0f) {
                    return@update warpToRandomWorld(state.copy(roamGraceTimerSec = nextGrace, roamingMonsters = movedRoamers))
                }
                return@update state.copy(
                    randomWarpCountdownSec = nextWarp,
                    roamGraceTimerSec = nextGrace,
                    roamingMonsters = movedRoamers
                )
            }
        }
    }

    private fun warpToRandomWorld(state: AppUiState): AppUiState {
        val candidates = WorldId.entries.filter { it != state.activeWorld }
        val echoPending = state.isJourneyCompleteForEcho &&
            EnemyId.ECHO_LOKA !in state.friendAllies &&
            EnemyId.ECHO_LOKA !in state.defeatedEnemies
        val dest = if (echoPending && WorldId.GLITCHED_WORLD in candidates && Random.nextFloat() < 0.55f) {
            WorldId.GLITCHED_WORLD
        } else {
            candidates.random()
        }
        val firstMem = WorldContentCatalog.glitchedMemories.random()
        if (dest == WorldId.GLITCHED_WORLD) {
            RetroSoundEngine.playGlitchNoise(130)
            RetroSoundEngine.playWhisper()
        } else {
            RetroSoundEngine.playSoulSwitch(dest.ordinal)
        }
        val echoPos = if (dest == WorldId.GLITCHED_WORLD) {
            (0.24f + Random.nextFloat() * 0.52f) to (0.24f + Random.nextFloat() * 0.45f)
        } else state.echoLokaPos

        val warped = state.copy(
            activeWorld = dest,
            playerMapX = 0.50f,
            playerMapY = 0.54f,
            paradoxRiftPos = randomRiftPosition(),
            randomWarpCountdownSec = 45f,
            glitchedCountdownSec = 10f,
            glitchGhosts = emptyList(),
            roamingMonsters = roamingForWorld(dest),
            roamGraceTimerSec = 2.6f,
            activeGlitchMemory = firstMem,
            glitchMemoryCycleSec = 0f,
            echoLokaPos = echoPos,
            worldBannerMessage = if (dest == WorldId.GLITCHED_WORLD) {
                if (state.isJourneyCompleteForEcho) {
                    if (state.language == AppLanguage.AR) "العالم المقلتش — اكتملت رحلتك! Echo-Loka ينتظرك كآخر عدو!" else "GLITCHED VOID — Journey complete! Echo-Loka awaits as your FINAL battle!"
                } else {
                    if (state.language == AppLanguage.AR) "العالم المقلتش — 10 ثوانٍ وسط ذكريات الماضي!" else "GLITCHED VOID — 10s among fractured past memories!"
                }
            } else {
                if (state.language == AppLanguage.AR) "انتقال زمني عشوائي إلى ${dest.title}!" else "Random Paradox Warp! Pulled into ${dest.title}."
            }
        )
        return applyNormalWorldDropOff(warped)
    }

    /**
     * When Loka reaches the Normal World, any kids he helped & is escorting are left safely
     * in the Normal World and do NOT continue the rest of the journey with him.
     */
    private fun applyNormalWorldDropOff(state: AppUiState): AppUiState {
        if (state.activeWorld != WorldId.NORMAL_WORLD || state.escortingKidIds.isEmpty()) return state
        val arrivedKids = WorldContentCatalog.lostKids.filter { it.id in state.escortingKidIds }
        val names = arrivedKids.joinToString(", ") {
            if (state.language == AppLanguage.AR) it.nameAr else it.name
        }
        return state.copy(
            rescuedToNormalKidIds = state.rescuedToNormalKidIds + state.escortingKidIds,
            escortingKidIds = emptySet(),
            worldBannerMessage = if (state.language == AppLanguage.AR) {
                "وصلت للعالم الطبيعي! نزل ($names) بأمان ولن يكملوا بقية الرحلة مع لوكا."
            } else {
                "Reached Normal World! $names returned safely and parted ways with Loka."
            }
        )
    }

    private fun randomRiftPosition(): Pair<Float, Float> {
        val spots = listOf(0.50f to 0.48f, 0.34f to 0.50f, 0.66f to 0.48f, 0.50f to 0.36f)
        return spots.random()
    }

    fun helpLostKid(kidId: String) {
        val kid = WorldContentCatalog.lostKids.firstOrNull { it.id == kidId } ?: return
        _uiState.update { state ->
            if (kidId in state.escortingKidIds || kidId in state.rescuedToNormalKidIds || kidId in state.killedKidIds) {
                return@update state
            }
            RetroSoundEngine.playHealChime()
            state.copy(
                escortingKidIds = state.escortingKidIds + kidId,
                worldBannerMessage = if (state.language == AppLanguage.AR) {
                    "ساعدت ${kid.nameAr}! سيبقى معك ويدعمك حتى تصل للعالم الطبيعي فقط."
                } else {
                    "Helped ${kid.name}! Traveling with Loka until reaching the Normal World."
                }
            ).also(::persist)
        }
    }

    fun killLostKid(kidId: String) {
        val kid = WorldContentCatalog.lostKids.firstOrNull { it.id == kidId } ?: return
        _uiState.update { state ->
            if (kidId in state.escortingKidIds || kidId in state.rescuedToNormalKidIds || kidId in state.killedKidIds) {
                return@update state
            }
            RetroSoundEngine.playStrike(true)
            RetroSoundEngine.playHurt()
            state.copy(
                killedKidIds = state.killedKidIds + kidId,
                worldBannerMessage = if (state.language == AppLanguage.AR) {
                    "قتلت ${kid.nameAr}... الصمت يزداد برودة حول قلادتك."
                } else {
                    "Killed ${kid.name}... A cold silence settles over the paradox."
                }
            ).also(::persist)
        }
    }

    fun completeLandmarkQuest(landmarkId: String) {
        val landmark = WorldContentCatalog.landmarks.firstOrNull { it.id == landmarkId } ?: return
        _uiState.update { state ->
            if (landmarkId in state.completedLandmarkIds) return@update state
            RetroSoundEngine.playAbility()
            val newItems = state.itemCounts.toMutableMap()
            landmark.rewardItemId?.let { itemId ->
                newItems[itemId] = (newItems[itemId] ?: 0) + 2
            }
            val newArmors = if (landmark.rewardArmorId != null) {
                state.unlockedArmorIds + landmark.rewardArmorId
            } else state.unlockedArmorIds
            val newWeapons = if (landmark.rewardWeaponId != null) {
                state.unlockedWeaponIds + landmark.rewardWeaponId
            } else state.unlockedWeaponIds

            state.copy(
                completedLandmarkIds = state.completedLandmarkIds + landmarkId,
                itemCounts = newItems,
                unlockedArmorIds = newArmors,
                equippedArmorId = landmark.rewardArmorId ?: state.equippedArmorId,
                unlockedWeaponIds = newWeapons,
                equippedWeaponId = landmark.rewardWeaponId ?: state.equippedWeaponId,
                worldBannerMessage = "Completed ${landmark.questTitle}! Earned ${landmark.rewardLabel}."
            ).also(::persist)
        }
    }

    fun claimNpcEvent(npcId: String) {
        val npc = WorldContentCatalog.npcs.firstOrNull { it.id == npcId } ?: return
        val rewardItem = npc.eventRewardItemId ?: return
        _uiState.update { state ->
            if (npcId in state.claimedNpcEvents) return@update state
            RetroSoundEngine.playSoulSwitch(1)
            val newItems = state.itemCounts.toMutableMap()
            newItems[rewardItem] = (newItems[rewardItem] ?: 0) + 1
            state.copy(
                claimedNpcEvents = state.claimedNpcEvents + npcId,
                itemCounts = newItems,
                worldBannerMessage = npc.eventRewardText ?: "Received gift from ${npc.name}!"
            ).also(::persist)
        }
    }

    fun openWorldChest(chestId: String) {
        val chest = WorldContentCatalog.chests.firstOrNull { it.id == chestId } ?: return
        _uiState.update { state ->
            if (chestId in state.openedChestIds) return@update state
            RetroSoundEngine.playChestOpen()
            val newItems = state.itemCounts.toMutableMap()
            newItems[chest.rewardItemId] = (newItems[chest.rewardItemId] ?: 0) + chest.rewardCount
            val itemName = when (chest.rewardItemId) {
                "chrono_shard" -> if (state.language == AppLanguage.AR) "شظايا زمن" else "Chrono Shard"
                "sepia_ration" -> if (state.language == AppLanguage.AR) "حصص شفاء" else "Sepia Ration"
                else -> if (state.language == AppLanguage.AR) "مرساة حفظ" else "Time Anchor"
            }
            state.copy(
                openedChestIds = state.openedChestIds + chestId,
                itemCounts = newItems,
                worldBannerMessage = if (state.language == AppLanguage.AR) {
                    "فتحت ${chest.nameAr}! حصلت على +${chest.rewardCount} $itemName."
                } else {
                    "Opened ${chest.nameEn}! Found +${chest.rewardCount} $itemName."
                }
            ).also(::persist)
        }
    }

    fun equipArmor(armorId: String) {
        _uiState.update { state ->
            if (armorId !in state.unlockedArmorIds) return@update state
            RetroSoundEngine.playSoulSwitch(0)
            state.copy(equippedArmorId = armorId).also(::persist)
        }
    }

    fun equipWeapon(weaponId: String) {
        _uiState.update { state ->
            if (weaponId !in state.unlockedWeaponIds) return@update state
            RetroSoundEngine.playSoulSwitch(2)
            state.copy(equippedWeaponId = weaponId).also(::persist)
        }
    }

    fun startBattle(enemyId: EnemyId, worldOverride: WorldId? = null) {
        _uiState.update { state ->
            if (enemyId == EnemyId.ECHO_LOKA && !state.isJourneyCompleteForEcho) {
                RetroSoundEngine.playGlitchNoise(85)
                return@update state.copy(
                    worldBannerMessage = if (state.language == AppLanguage.AR) {
                        "Echo-Loka هو العدو الأخير! أنهِ رحلتك عبر العوالم أولاً (${state.resolvedBossCount}/${state.totalStandardBosses} زعماء)."
                    } else {
                        "Echo-Loka is your FINAL battle! Finish your journey across the realms first (${state.resolvedBossCount}/${state.totalStandardBosses} bosses)."
                    }
                )
            }
            val targetWorld = worldOverride ?: state.activeWorld
            val nextSave = if (state.saveCounter >= 8) 1 else state.saveCounter + 1
            state.copy(
                activeWorld = targetWorld,
                selectedEnemyId = enemyId,
                battleSessionId = state.battleSessionId + 1L,
                battleFromRuins = enemyId == EnemyId.RUINS_DUMMY,
                saveCounter = nextSave,
                roamGraceTimerSec = 2.8f,
                currentScreen = AppScreen.BATTLE
            )
        }
    }

    fun returnAfterBattle() {
        _uiState.update { state ->
            if (state.battleFromRuins) {
                return@update state.copy(
                    battleFromRuins = false,
                    currentScreen = AppScreen.RUINS_TUTORIAL
                )
            }
            if (state.activeWorld == WorldId.GLITCHED_WORLD) {
                val safeWorld = listOf(WorldId.NORMAL_WORLD, WorldId.TRI_RACE_REALM, WorldId.SPLIT_REALM).random()
                val nextState = state.copy(
                    activeWorld = safeWorld,
                    playerMapX = 0.50f,
                    playerMapY = 0.54f,
                    roamingMonsters = roamingForWorld(safeWorld),
                    roamGraceTimerSec = 2.6f,
                    currentScreen = AppScreen.WORLD_EXPLORE,
                    worldBannerMessage = if (state.language == AppLanguage.AR) {
                        "انتقلت عشوائياً إلى ${safeWorld.title}."
                    } else {
                        "Warped randomly into ${safeWorld.title}."
                    }
                )
                return@update applyNormalWorldDropOff(nextState)
            }
            state.copy(
                roamGraceTimerSec = 2.6f,
                currentScreen = AppScreen.WORLD_EXPLORE
            )
        }
    }

    fun recordBattleOutcome(enemyId: EnemyId, spared: Boolean, remainingItems: Map<String, Int>) {
        if (spared) RetroSoundEngine.playSpareChime()
        _uiState.update { state ->
            if (enemyId == EnemyId.RUINS_DUMMY) {
                return@update state.copy(
                    ruinsDummyPracticed = true,
                    itemCounts = remainingItems
                ).also(::persist)
            }
            if (enemyId == EnemyId.FUTURE_LOKA && !spared) {
                return@update state.copy(
                    defeatedEnemies = state.defeatedEnemies + enemyId,
                    itemCounts = remainingItems,
                    activeFinaleRoute = RouteType.NO_MERCY,
                    unlockedRoutes = state.unlockedRoutes + RouteType.NO_MERCY,
                    worldDestroyedEnding = true,
                    currentScreen = AppScreen.ROUTE_FINALE
                ).also(::persist)
            }
            state.copy(
                friendAllies = if (spared) state.friendAllies + enemyId else state.friendAllies,
                defeatedEnemies = if (!spared) state.defeatedEnemies + enemyId else state.defeatedEnemies,
                itemCounts = remainingItems
            ).also(::persist)
        }
    }

    fun openRouteFinale(route: RouteType) {
        _uiState.update { state ->
            state.copy(
                activeFinaleRoute = route,
                currentScreen = AppScreen.ROUTE_FINALE
            ).also(::persist)
        }
    }

    fun startFutureLokaNoMercyFight() {
        startBattle(EnemyId.FUTURE_LOKA, WorldId.NORMAL_WORLD)
    }

    fun choosePeaceSacrifice() {
        _uiState.update { state ->
            RetroSoundEngine.playAbility()
            state.copy(
                unlockedRoutes = state.unlockedRoutes + RouteType.MOST_EXPECTED,
                peaceSacrificeEnding = true
            ).also(::persist)
        }
    }

    fun acceptPerfectFuturePower() {
        _uiState.update { state ->
            RetroSoundEngine.playAbility()
            state.copy(
                unlockedRoutes = state.unlockedRoutes + RouteType.PERFECT,
                isFutureTransformed = true,
                friendAllies = state.friendAllies + EnemyId.FUTURE_LOKA
            ).also(::persist)
        }
    }

    fun masterTeleportAndRestart(targetPath: RouteType) {
        _uiState.update { state ->
            RetroSoundEngine.playSoulSwitch(3)
            val allStd = EnemyRoster.standardPlayableRoster().map { it.id }.toSet()
            val newFriends = if (targetPath == RouteType.PERFECT) allStd else emptySet()
            val newKills = if (targetPath == RouteType.NO_MERCY) allStd else emptySet()
            state.copy(
                unlockedRoutes = state.unlockedRoutes + RouteType.LOST_TAPES,
                teleportControlMastered = true,
                worldDestroyedEnding = false,
                peaceSacrificeEnding = false,
                isFutureTransformed = (targetPath == RouteType.PERFECT),
                friendAllies = newFriends,
                defeatedEnemies = newKills,
                escortingKidIds = emptySet(),
                rescuedToNormalKidIds = emptySet(),
                killedKidIds = emptySet(),
                activeWorld = WorldId.NORMAL_WORLD,
                currentScreen = AppScreen.WORLD_EXPLORE,
                worldBannerMessage = if (targetPath == RouteType.PERFECT) {
                    "World Restarted: Teleport mastered — Perfect Mercy Route!"
                } else {
                    "World Restarted: Teleport mastered — No Mercy Path!"
                }
            ).also(::persist)
        }
    }

    fun returnToExploreFromFinale() {
        _uiState.update { it.copy(currentScreen = AppScreen.WORLD_EXPLORE).also(::persist) }
    }

    fun toggleControlScheme() {
        _uiState.update { state ->
            val values = ControlScheme.entries
            val next = values[(state.controlScheme.ordinal + 1) % values.size]
            state.copy(controlScheme = next).also(::persist)
        }
    }

    fun toggleMute() {
        _uiState.update { state ->
            val muted = !state.isMuted
            RetroSoundEngine.isMuted = muted
            state.copy(isMuted = muted).also(::persist)
        }
    }
}
