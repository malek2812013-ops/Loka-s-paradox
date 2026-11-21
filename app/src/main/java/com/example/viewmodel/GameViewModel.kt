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
import com.example.model.WorldZone
import com.example.model.WorldZoneMap
import com.example.model.ZoneEdge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.hypot
import kotlin.random.Random

private const val DEFAULT_WARP_SECONDS = 600f // 10 full minutes
private const val ASHEN_UNDERGROUND_SECONDS = 75f // ~1m 15s underground after saving

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
    val zone: WorldZone,
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

    // Ruins Expanded 3-Chamber Opening Area (Flowerbed -> Pillar & Switch Hall -> Dummy & Exit Gate)
    val ruinsRoomIndex: Int = 0,
    val ruinsPlayerX: Float = 0.22f,
    val ruinsPlayerY: Float = 0.56f,
    val ruinsLoreRead: Boolean = false,
    val ruinsCandyClaimed: Boolean = false,
    val ruinsSwitchActivated: Boolean = false,
    val ruinsDummyPracticed: Boolean = false,

    // World, 5-Door Zones & Paradox Exploration
    val activeWorld: WorldId = WorldId.NORMAL_WORLD,
    val activeZone: WorldZone = WorldZone.CENTER,
    val zoneCooldownSec: Float = 0f,
    val playerMapX: Float = 0.50f,
    val playerMapY: Float = 0.50f,
    val paradoxRiftPos: Pair<Float, Float> = 0.18f to 0.22f,
    val randomWarpCountdownSec: Float = DEFAULT_WARP_SECONDS,
    val glitchedCountdownSec: Float = 10f,
    val ashenHiddenTimerSec: Float = 0f,
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
    val stoppedEnemies: Set<EnemyId> = emptySet(),
    val angelAidClaimed: Boolean = false,
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
    val saveCounter: Int = 0,
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

    val governmentBossIds: Set<EnemyId>
        get() = setOf(EnemyId.GOV_MONSTER_CHIEF, EnemyId.GOV_DIRECTOR)

    val governmentStoppedCount: Int
        get() = governmentBossIds.count { it in friendAllies || it in stoppedEnemies || it in defeatedEnemies }

    val totalGovernmentTargets: Int
        get() = governmentBossIds.size

    val isGovernmentStopped: Boolean
        get() = governmentStoppedCount >= totalGovernmentTargets

    val totalStandardBosses: Int
        get() = EnemyRoster.standardPlayableRoster().size

    val resolvedBossCount: Int
        get() {
            val standardIds = EnemyRoster.standardPlayableRoster().map { it.id }.toSet()
            return standardIds.count { it in friendAllies || it in stoppedEnemies || it in defeatedEnemies }
        }

    val isJourneyCompleteForEcho: Boolean
        get() = resolvedBossCount >= totalStandardBosses || teleportControlMastered || unlockedRoutes.isNotEmpty()

    val eligibleRoute: RouteType?
        get() {
            val standardIds = EnemyRoster.standardPlayableRoster().map { it.id }.toSet()
            val killedStd = defeatedEnemies.intersect(standardIds)
            val peacefulStd = (friendAllies + stoppedEnemies).intersect(standardIds)
            val anyKidKilled = killedKidIds.isNotEmpty()
            val anyKidHelped = escortingKidIds.isNotEmpty() || rescuedToNormalKidIds.isNotEmpty()
            return when {
                secretTapesFound >= totalSecretTapes -> RouteType.LOST_TAPES
                peacefulStd.size == standardIds.size && killedStd.isEmpty() && !anyKidKilled -> RouteType.PERFECT
                killedStd.size == standardIds.size && peacefulStd.isEmpty() && !anyKidHelped -> RouteType.NO_MERCY
                (killedStd.size + peacefulStd.size) >= 2 ||
                    (killedStd.size == standardIds.size && anyKidHelped) ||
                    (peacefulStd.size == standardIds.size && anyKidKilled) -> RouteType.MOST_EXPECTED
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
                activeZone = snap.activeZone,
                playerMapX = snap.playerMapX,
                playerMapY = snap.playerMapY,
                randomWarpCountdownSec = DEFAULT_WARP_SECONDS,
                defeatedEnemies = snap.defeatedEnemies,
                friendAllies = snap.friendAllies,
                stoppedEnemies = snap.stoppedEnemies,
                angelAidClaimed = snap.angelAidClaimed,
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
                zone = enemy.zone,
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
                activeZone = state.activeZone,
                playerMapX = state.playerMapX,
                playerMapY = state.playerMapY,
                defeatedEnemies = state.defeatedEnemies,
                friendAllies = state.friendAllies,
                stoppedEnemies = state.stoppedEnemies,
                angelAidClaimed = state.angelAidClaimed,
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
            val rawX = state.ruinsPlayerX + dx * state.kidSpeedMultiplier
            val ny = (state.ruinsPlayerY + dy * state.kidSpeedMultiplier).coerceIn(0.27f, 0.85f)

            // Smooth doorway transitions across the 3 Ruins chambers along the doorway corridor Y-band
            if (rawX >= 0.92f && state.ruinsRoomIndex < 2 && ny in 0.36f..0.74f) {
                RetroSoundEngine.playTypeBlip(310f)
                return@update state.copy(
                    ruinsRoomIndex = state.ruinsRoomIndex + 1,
                    ruinsPlayerX = 0.14f,
                    ruinsPlayerY = ny
                )
            }
            if (rawX <= 0.08f && state.ruinsRoomIndex > 0 && ny in 0.36f..0.74f) {
                RetroSoundEngine.playTypeBlip(275f)
                return@update state.copy(
                    ruinsRoomIndex = state.ruinsRoomIndex - 1,
                    ruinsPlayerX = 0.86f,
                    ruinsPlayerY = ny
                )
            }

            state.copy(
                ruinsPlayerX = rawX.coerceIn(0.08f, 0.92f),
                ruinsPlayerY = ny
            )
        }
    }

    fun inspectRuinsLore() {
        RetroSoundEngine.playTypeBlip(360f)
        _uiState.update { it.copy(ruinsLoreRead = true) }
    }

    fun claimRuinsCandy() {
        _uiState.update { state ->
            if (state.ruinsCandyClaimed) return@update state
            RetroSoundEngine.playSaveChime()
            val updatedItems = state.itemCounts.toMutableMap()
            updatedItems["chrono_shard"] = (updatedItems["chrono_shard"] ?: 0) + 1
            state.copy(ruinsCandyClaimed = true, itemCounts = updatedItems)
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
     * After meeting Ashen, Loka begins at the connected Normal World hub with a clear mission to reach & stop The Government!
     */
    fun finishAshenMeetingToRandomWorld() {
        val startWorld = WorldId.NORMAL_WORLD
        RetroSoundEngine.playSoulSwitch(0)
        _uiState.update { state ->
            state.copy(
                activeWorld = startWorld,
                activeZone = WorldZone.CENTER,
                zoneCooldownSec = 0.65f,
                currentScreen = AppScreen.WORLD_EXPLORE,
                playerMapX = 0.50f,
                playerMapY = 0.54f,
                paradoxRiftPos = 0.18f to 0.22f,
                randomWarpCountdownSec = DEFAULT_WARP_SECONDS,
                glitchedCountdownSec = 10f,
                ashenHiddenTimerSec = 0f,
                glitchGhosts = emptyList(),
                roamingMonsters = roamingForWorld(startWorld),
                roamGraceTimerSec = 2.6f,
                worldBannerMessage = if (state.language == AppLanguage.AR) {
                    "الهدف: أوصل إلى الحكومة (شمالاً عبر البوابات) وأوقفهم! العوالم متصلة معاً، والعالم المقلتش شاذ."
                } else {
                    "GOAL: Travel via World Gates to The Government (North) and stop them! Worlds are connected; Glitched Void is an anomaly."
                }
            ).also(::persist)
        }
    }

    fun saveProgressExplicitly(fromAshen: Boolean = false) {
        _uiState.update { state ->
            val inOverworld = state.currentScreen == AppScreen.WORLD_EXPLORE
            if (fromAshen && inOverworld && state.ashenHiddenTimerSec > 0f) {
                return@update state
            }
            RetroSoundEngine.playSaveChime()
            val incremented = state.saveCounter + 1

            // 8 Ashen saves pull Loka specifically into the Glitched Void anomaly (unless Teleport is mastered)!
            if (fromAshen && inOverworld && incremented >= 8 && !state.teleportControlMastered) {
                val warped = warpToGlitchedVoid(
                    state.copy(
                        saveCounter = 0,
                        ashenHiddenTimerSec = ASHEN_UNDERGROUND_SECONDS,
                        hasSavedFile = true
                    )
                )
                return@update warped.copy(
                    worldBannerMessage = if (state.language == AppLanguage.AR) {
                        "الحفظ رقم 8! نزل آشن تحت الأرض وسحبك الشذوذ إلى العالم المقلتش!"
                    } else {
                        "Save #8! Ashen burrowed underground & a Glitch Anomaly pulled you into the Glitched Void!"
                    }
                ).also(::persist)
            }

            val nextSave = if (incremented >= 8) 0 else incremented
            val nextHidden = if (fromAshen && inOverworld) ASHEN_UNDERGROUND_SECONDS else state.ashenHiddenTimerSec
            val msg = if (fromAshen && inOverworld) {
                if (state.language == AppLanguage.AR) {
                    "حفظ آشن تقدمك (#$incremented) ونزل تحت الأرض! سيظهر مجدداً بعد دقيقة وربع ★"
                } else {
                    "Ashen saved (#$incremented) & burrowed underground! Back in ~1m 15s ★"
                }
            } else if (fromAshen) {
                if (state.language == AppLanguage.AR) "حفظ آشن تقدمك على الجهاز بنجاح! ★" else "Ashen saved your timeline to device! ★"
            } else {
                if (state.language == AppLanguage.AR) "تم حفظ ملف اللعبة! ★" else "File Saved! ★"
            }

            state.copy(
                saveCounter = nextSave,
                ashenHiddenTimerSec = nextHidden,
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
            val nx = (state.playerMapX + dx * speed).coerceIn(0.03f, 0.97f)
            val ny = (state.playerMapY + dy * speed).coerceIn(0.04f, 0.96f)

            // 5-Door edge & Door V transitions across connected world zones!
            if (state.activeWorld.hasLandmarks && state.zoneCooldownSec <= 0f) {
                val doors = WorldZoneMap.doorsInZone(state.activeWorld, state.activeZone)
                val crossedDoor = doors.firstOrNull { d ->
                    when (d.edge) {
                        ZoneEdge.TOP -> ny <= 0.085f && nx in 0.34f..0.66f
                        ZoneEdge.BOTTOM -> ny >= 0.915f && nx in 0.34f..0.66f
                        ZoneEdge.LEFT -> nx <= 0.065f && ny in 0.34f..0.66f
                        ZoneEdge.RIGHT -> nx >= 0.935f && ny in 0.34f..0.66f
                        null -> hypot(nx - d.doorX, ny - d.doorY) < 0.075f
                    }
                }
                if (crossedDoor != null) {
                    val destWorld = crossedDoor.targetWorld ?: state.activeWorld
                    val changedWorld = destWorld != state.activeWorld
                    RetroSoundEngine.playSoulSwitch(if (changedWorld) destWorld.ordinal else 0)
                    val movedState = state.copy(
                        activeWorld = destWorld,
                        activeZone = crossedDoor.targetZone,
                        playerMapX = crossedDoor.spawnX,
                        playerMapY = crossedDoor.spawnY,
                        zoneCooldownSec = 0.65f,
                        roamingMonsters = if (changedWorld) roamingForWorld(destWorld) else state.roamingMonsters,
                        roamGraceTimerSec = 1.8f,
                        worldBannerMessage = if (changedWorld) {
                            if (state.language == AppLanguage.AR) {
                                "🌐 عبرت بوابة العوالم إلى ${destWorld.title} — ${crossedDoor.targetZone.titleAr}"
                            } else {
                                "🌐 Crossed World Gate to ${destWorld.title} — ${crossedDoor.targetZone.titleEn}"
                            }
                        } else {
                            if (state.language == AppLanguage.AR) {
                                "${destWorld.title} — ${crossedDoor.targetZone.titleAr}"
                            } else {
                                "${destWorld.title} — ${crossedDoor.targetZone.titleEn}"
                            }
                        }
                    )
                    return@update applyNormalWorldDropOff(movedState).also(::persist)
                }
            }

            state.copy(playerMapX = nx, playerMapY = ny)
        }
    }

    fun triggerManualRiftJump() {
        _uiState.update { state ->
            if (state.currentScreen != AppScreen.WORLD_EXPLORE) return@update state
            warpToGlitchedVoid(state).also(::persist)
        }
    }

    fun tickOverworld(dtRaw: Float) {
        val dt = dtRaw.coerceIn(0.001f, 0.05f)
        _uiState.update { state ->
            if (state.currentScreen != AppScreen.WORLD_EXPLORE) return@update state

            val nextZoneCooldown = (state.zoneCooldownSec - dt).coerceAtLeast(0f)
            val prevHidden = state.ashenHiddenTimerSec
            val nextHidden = (prevHidden - dt).coerceAtLeast(0f)
            val ashenJustReturned = prevHidden > 0f && nextHidden <= 0f
            if (ashenJustReturned) {
                RetroSoundEngine.playHealChime()
            }
            val returnNotice = if (ashenJustReturned && state.activeWorld != WorldId.GLITCHED_WORLD) {
                if (state.language == AppLanguage.AR) "ظهر آشن مجدداً من تحت الأرض في الساحة المركزية!"
                else "Ashen popped back out of the ground in the Central Plaza!"
            } else state.worldBannerMessage

            if (state.activeWorld == WorldId.GLITCHED_WORLD) {
                val nextTime = if (state.teleportControlMastered) state.glitchedCountdownSec else state.glitchedCountdownSec - dt
                if (!state.teleportControlMastered && nextTime <= 0f) {
                    val dest = WorldId.NORMAL_WORLD
                    RetroSoundEngine.playGlitchNoise(120)
                    val base = state.copy(
                        activeWorld = dest,
                        activeZone = WorldZone.CENTER,
                        zoneCooldownSec = 0.65f,
                        playerMapX = 0.50f,
                        playerMapY = 0.54f,
                        paradoxRiftPos = 0.18f to 0.22f,
                        randomWarpCountdownSec = DEFAULT_WARP_SECONDS,
                        glitchedCountdownSec = 10f,
                        ashenHiddenTimerSec = nextHidden,
                        glitchGhosts = emptyList(),
                        roamingMonsters = roamingForWorld(dest),
                        roamGraceTimerSec = 2.6f,
                        worldBannerMessage = if (state.language == AppLanguage.AR) {
                            "انتهى الشذوذ! عدت إلى ${dest.title}."
                        } else {
                            "Glitch anomaly ended — returned to ${dest.title}."
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
                    zoneCooldownSec = nextZoneCooldown,
                    ashenHiddenTimerSec = nextHidden,
                    glitchedCountdownSec = nextTime,
                    glitchGhosts = updatedGhosts,
                    activeGlitchMemory = nextMem,
                    glitchMemoryCycleSec = nextMemCycle,
                    worldBannerMessage = returnNotice
                )
            } else {
                val nextGrace = (state.roamGraceTimerSec - dt).coerceAtLeast(0f)
                val px = state.playerMapX
                val py = state.playerMapY
                val currentRoamers = state.roamingMonsters.ifEmpty { roamingForWorld(state.activeWorld) }
                var collidedEnemy: RoamingMonster? = null
                val movedRoamers = currentRoamers.map { m ->
                    if (m.worldId != state.activeWorld || m.zone != state.activeZone) return@map m
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
                    return@update state.copy(
                        zoneCooldownSec = nextZoneCooldown,
                        ashenHiddenTimerSec = nextHidden,
                        roamingMonsters = remaining,
                        roamGraceTimerSec = 2.8f,
                        selectedEnemyId = caught.enemyId,
                        battleSessionId = state.battleSessionId + 1L,
                        battleFromRuins = false,
                        currentScreen = AppScreen.BATTLE,
                        worldBannerMessage = returnNotice
                    )
                }

                // Physical worlds stay connected via World Gates — no forced 10-minute random warp!
                return@update state.copy(
                    zoneCooldownSec = nextZoneCooldown,
                    ashenHiddenTimerSec = nextHidden,
                    roamGraceTimerSec = nextGrace,
                    roamingMonsters = movedRoamers,
                    worldBannerMessage = returnNotice
                )
            }
        }
    }

    private fun warpToGlitchedVoid(state: AppUiState): AppUiState {
        val dest = WorldId.GLITCHED_WORLD
        val firstMem = WorldContentCatalog.glitchedMemories.random()
        RetroSoundEngine.playGlitchNoise(130)
        RetroSoundEngine.playWhisper()
        val echoPos = (0.24f + Random.nextFloat() * 0.52f) to (0.24f + Random.nextFloat() * 0.45f)

        return state.copy(
            activeWorld = dest,
            activeZone = WorldZone.CENTER,
            zoneCooldownSec = 0.65f,
            playerMapX = 0.50f,
            playerMapY = 0.54f,
            paradoxRiftPos = 0.18f to 0.22f,
            randomWarpCountdownSec = DEFAULT_WARP_SECONDS,
            glitchedCountdownSec = 10f,
            glitchGhosts = emptyList(),
            roamingMonsters = emptyList(),
            roamGraceTimerSec = 2.6f,
            activeGlitchMemory = firstMem,
            glitchMemoryCycleSec = 0f,
            echoLokaPos = echoPos,
            worldBannerMessage = if (state.isJourneyCompleteForEcho) {
                if (state.language == AppLanguage.AR) "العالم المقلتش — اكتملت رحلتك! Echo-Loka ينتظرك كآخر عدو!" else "GLITCHED VOID — Journey complete! Echo-Loka awaits as your FINAL battle!"
            } else {
                if (state.language == AppLanguage.AR) "العالم المقلتش (شذوذ معزول) — 10 ثوانٍ وسط ذكريات الماضي!" else "GLITCHED VOID (Disconnected Anomaly) — 10s among fractured past memories!"
            }
        )
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
            if (npcId == "npc_angel_leader") {
                RetroSoundEngine.playSaveChime()
                val newItems = state.itemCounts.toMutableMap()
                newItems["sepia_ration"] = (newItems["sepia_ration"] ?: 0) + 2
                newItems["chrono_shard"] = (newItems["chrono_shard"] ?: 0) + 2
                return@update state.copy(
                    claimedNpcEvents = state.claimedNpcEvents + npcId,
                    angelAidClaimed = true,
                    unlockedArmorIds = state.unlockedArmorIds + "angel_seal_mail",
                    equippedArmorId = "angel_seal_mail",
                    itemCounts = newItems,
                    worldBannerMessage = if (state.language == AppLanguage.AR) {
                        "★ زعيم الملائكة كاسييل ساعدك! لبست درع الختم (+3 دفاع، +8 دم) وحصلت على +2 شفاء و+2 شظية!"
                    } else {
                        "★ Angel Leader Cassiel aids you! Equipped Angel Leader's Seal (+3 DEF, +8 HP) & +2 Rations, +2 Shards!"
                    }
                ).also(::persist)
            }
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
            state.copy(
                activeWorld = targetWorld,
                selectedEnemyId = enemyId,
                battleSessionId = state.battleSessionId + 1L,
                battleFromRuins = enemyId == EnemyId.RUINS_DUMMY,
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
                val safeWorld = WorldId.NORMAL_WORLD
                val nextState = state.copy(
                    activeWorld = safeWorld,
                    activeZone = WorldZone.CENTER,
                    zoneCooldownSec = 0.65f,
                    playerMapX = 0.50f,
                    playerMapY = 0.54f,
                    randomWarpCountdownSec = DEFAULT_WARP_SECONDS,
                    roamingMonsters = roamingForWorld(safeWorld),
                    roamGraceTimerSec = 2.6f,
                    currentScreen = AppScreen.WORLD_EXPLORE,
                    worldBannerMessage = if (state.language == AppLanguage.AR) {
                        "عدت من العالم المقلتش إلى ${safeWorld.title}."
                    } else {
                        "Returned from Glitched Void to ${safeWorld.title}."
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
            val profile = EnemyRoster.byId(enemyId)
            val isNonFriendStop = spared && !profile.canBefriend && profile.canStopWithoutKilling
            state.copy(
                friendAllies = if (spared && !isNonFriendStop) state.friendAllies + enemyId else state.friendAllies,
                stoppedEnemies = if (isNonFriendStop) state.stoppedEnemies + enemyId else state.stoppedEnemies,
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
            val newFriends = if (targetPath == RouteType.PERFECT) {
                allStd.filter { EnemyRoster.byId(it).canBefriend }.toSet()
            } else emptySet()
            val newStopped = if (targetPath == RouteType.PERFECT) {
                allStd.filter { !EnemyRoster.byId(it).canBefriend && EnemyRoster.byId(it).canStopWithoutKilling }.toSet()
            } else emptySet()
            val newKills = if (targetPath == RouteType.NO_MERCY) allStd else emptySet()
            state.copy(
                unlockedRoutes = state.unlockedRoutes + RouteType.LOST_TAPES,
                teleportControlMastered = true,
                worldDestroyedEnding = false,
                peaceSacrificeEnding = false,
                isFutureTransformed = (targetPath == RouteType.PERFECT),
                friendAllies = newFriends,
                stoppedEnemies = newStopped,
                defeatedEnemies = newKills,
                angelAidClaimed = false,
                escortingKidIds = emptySet(),
                rescuedToNormalKidIds = emptySet(),
                killedKidIds = emptySet(),
                activeWorld = WorldId.NORMAL_WORLD,
                activeZone = WorldZone.CENTER,
                zoneCooldownSec = 0.65f,
                playerMapX = 0.50f,
                playerMapY = 0.54f,
                saveCounter = 0,
                ashenHiddenTimerSec = 0f,
                randomWarpCountdownSec = DEFAULT_WARP_SECONDS,
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
