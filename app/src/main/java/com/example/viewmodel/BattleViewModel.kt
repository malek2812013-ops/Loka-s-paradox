package com.example.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.audio.RetroSoundEngine
import com.example.engine.BattleEngine
import com.example.engine.LokaStatsCalculator
import com.example.model.ArenaBomb
import com.example.model.ArenaParticle
import com.example.model.ArmorEquipment
import com.example.model.ArrowTelegraph
import com.example.model.BattleItem
import com.example.model.BattlePhase
import com.example.model.Bullet
import com.example.model.BulletKind
import com.example.model.EnemyId
import com.example.model.EnemyProfile
import com.example.model.EnemyRoster
import com.example.model.GameDifficulty
import com.example.model.LaneTelegraph
import com.example.model.LaserBeam
import com.example.model.MenuSubTab
import com.example.model.MonsterJawAttack
import com.example.model.PortraitExpression
import com.example.model.RelicCatalog
import com.example.model.RelicEquipment
import com.example.model.SideWallShotWarning
import com.example.model.SoulType
import com.example.model.WeaponCatalog
import com.example.model.WeaponEquipment
import com.example.model.WorldContentCatalog
import com.example.model.WorldId
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaAmber
import com.example.ui.theme.SepiaGold
import com.example.ui.theme.SoulAegisAmber
import com.example.ui.theme.SoulInfernalViolet
import com.example.ui.theme.SoulParadoxCrimson
import com.example.ui.theme.SoulSeraphCyan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

data class BattleUiState(
    val encounterId: Long = 0L,
    val worldId: WorldId = WorldId.NORMAL_WORLD,
    val enemy: EnemyProfile = EnemyRoster.byId(EnemyId.VEX),
    val enemyHp: Int = 85,
    val mercyProgress: Int = 0,
    val completedActIds: Set<String> = emptySet(),
    val turnNumber: Int = 1,
    val phase: BattlePhase = BattlePhase.PLAYER_MENU,
    val subTab: MenuSubTab = MenuSubTab.ROOT,

    // Player, Armor, Friends & World-specific Souls
    val playerHp: Int = 20,
    val maxPlayerHp: Int = 20,
    val defenseBonus: Int = 0,
    val attackBonus: Int = 0,
    val speedMultiplier: Float = 1f,
    val activeAllies: Set<EnemyId> = emptySet(),
    val helpedKids: Set<String> = emptySet(),
    val allowedSouls: List<SoulType> = listOf(SoulType.RED),
    val activeSoul: SoulType = SoulType.RED,
    val soulSwitchCooldownSec: Float = 0f,
    val abilityCooldownSec: Float = 0f,

    // Original Soul-specific state fields
    val hasGreenShield: Boolean = false,
    val greenOrbitAngle: Float = 0f,
    val purpleMirageTimerSec: Float = 0f,
    val redEchoX: Float = 0.5f,
    val redEchoY: Float = 0.72f,

    // Split Duo Soul (Image 3) state
    val isSplitActive: Boolean = false,
    val controlledHalfIndex: Int = 0, // 0 = Red left half, 1 = Teal right half
    val secondHalfX: Float = 0.62f,
    val secondHalfY: Float = 0.58f,
    val splitBoostTimer0Sec: Float = 0f,
    val splitBoostTimer1Sec: Float = 0f,

    // Soul arena coordinates (normalized 0f..1f)
    val soulX: Float = 0.5f,
    val soulY: Float = 0.5f,
    val invulnTimerSec: Float = 0f,

    // Narrative & Combat feedback
    val dialogueText: String = "",
    val lastStrikeDamage: Int? = null,
    val lastStrikeCritical: Boolean = false,
    val slashFxTimerSec: Float = 0f,
    val isEnemyHitFlash: Boolean = false,
    val playerHitFlashTimerSec: Float = 0f,
    val arenaShakeTimerSec: Float = 0f,
    val arenaShakeIntensity: Float = 0f,
    val playerLastDamageTaken: Int? = null,
    val playerDamagePopTimerSec: Float = 0f,
    val shieldBlockedTimerSec: Float = 0f,
    val battleWonSpared: Boolean = false,
    val ashenHelpingThisTurn: Boolean = false,
    val equippedArmorName: String = "Loka's Grey Tee & Scarf",
    val equippedWeapon: WeaponEquipment = WeaponCatalog.byId("worn_stick"),
    val equippedRelic: RelicEquipment = RelicCatalog.byId("golden_locket"),
    val difficulty: GameDifficulty = GameDifficulty.NORMAL,
    val bossExpression: PortraitExpression = PortraitExpression.NEUTRAL,
    val speakerExpression: PortraitExpression = PortraitExpression.DETERMINED,

    // Classic Undertale Fight timing bar (sweeps back-and-forth toward center eye x = 0.50f)
    val fightCursorPos: Float = 0.08f,
    val fightCursorDir: Float = 1f,

    // Bullet-hell entities
    val phaseTimerSec: Float = 0f,
    val phaseDurationSec: Float = 6.5f,
    val spawnAccumulatorSec: Float = 0f,
    val isRewindingTime: Boolean = false,
    val bullets: List<Bullet> = emptyList(),
    val lasers: List<LaserBeam> = emptyList(),
    val laneTelegraphs: List<LaneTelegraph> = emptyList(),
    val jawAttacks: List<MonsterJawAttack> = emptyList(),
    val arrowTelegraphs: List<ArrowTelegraph> = emptyList(),
    val bombs: List<ArenaBomb> = emptyList(),
    val sideWarnings: List<SideWallShotWarning> = emptyList(),
    val particles: List<ArenaParticle> = emptyList(),

    // Ruins Dummy 2-Phase FIGHT animation state
    val dummyPhase: Int = 1,
    val dummyHatFallTimerSec: Float = 0f,

    // Inventory
    val items: List<BattleItem> = emptyList()
) {
    val canMercyOrStop: Boolean get() = (enemy.canBefriend || enemy.canStopWithoutKilling) && mercyProgress >= 100
    val canSpare: Boolean get() = canMercyOrStop
    val effectiveDefenseBonus: Int get() = defenseBonus + activeSoul.defenseBonus
    val dummyHatFallProgress: Float
        get() = if (dummyPhase >= 2) (dummyHatFallTimerSec / 0.85f).coerceIn(0f, 1f) else 0f
    val activeSplitBoosted: Boolean
        get() = isSplitActive && ((controlledHalfIndex == 0 && splitBoostTimer0Sec > 0f) ||
            (controlledHalfIndex == 1 && splitBoostTimer1Sec > 0f))
}

class BattleViewModel : ViewModel() {
    private val _state = MutableStateFlow(BattleUiState())
    val state: StateFlow<BattleUiState> = _state.asStateFlow()

    private var nextEntityId = 1L
    private var dpadInputX = 0f
    private var dpadInputY = 0f
    private var lastAimX = 0f
    private var lastAimY = 0f
    private var hitFlashTimer = 0f

    fun initEncounter(
        encounterId: Long = 1L,
        enemyId: EnemyId,
        worldId: WorldId,
        armor: ArmorEquipment,
        weapon: WeaponEquipment = WeaponCatalog.byId("worn_stick"),
        relic: RelicEquipment = RelicCatalog.byId("golden_locket"),
        difficulty: GameDifficulty = GameDifficulty.NORMAL,
        allies: Set<EnemyId>,
        helpedKidIds: Set<String>,
        itemCounts: Map<String, Int>
    ) {
        val profile = EnemyRoster.byId(enemyId)
        val allowed = worldId.allowedSouls
        val startingSoul = allowed.first()

        val computedStats = LokaStatsCalculator.compute(
            armor = armor,
            weapon = weapon,
            relic = relic,
            allies = allies,
            helpedKidIds = helpedKidIds,
            difficulty = difficulty,
            worldId = worldId
        )

        val startShield = (EnemyId.SERAPHIEL in allies) && worldId != WorldId.GLITCHED_WORLD

        val battleItems = listOf(
            BattleItem("chrono_shard", "Chrono Shard", healAmount = 8, grantsShield = false, count = itemCounts["chrono_shard"] ?: 0, description = "Restores 8 HP."),
            BattleItem("sepia_ration", "Sepia Ration", healAmount = 12, grantsShield = false, count = itemCounts["sepia_ration"] ?: 0, description = "Restores 12 HP."),
            BattleItem("save_anchor", "Time Anchor", healAmount = 5, grantsShield = true, count = itemCounts["save_anchor"] ?: 0, description = "Restores 5 HP + raises a Prism Shield.")
        )

        nextEntityId = 1L
        dpadInputX = 0f
        dpadInputY = 0f
        lastAimX = 0f
        lastAimY = 0f
        hitFlashTimer = 0f

        val sickWarning = if (startingSoul == SoulType.WHITE_SICK) {
            " [SICK WHITE SOUL: Slow, 1 DMG dealt, 2x DMG taken!]"
        } else ""

        _state.value = BattleUiState(
            encounterId = encounterId,
            worldId = worldId,
            enemy = profile,
            enemyHp = profile.maxHp,
            playerHp = computedStats.maxHp,
            maxPlayerHp = computedStats.maxHp,
            defenseBonus = computedStats.defense,
            attackBonus = computedStats.attackBonus,
            speedMultiplier = computedStats.speedMultiplier,
            activeAllies = allies,
            helpedKids = helpedKidIds,
            allowedSouls = allowed,
            activeSoul = startingSoul,
            hasGreenShield = startShield,
            equippedArmorName = armor.name,
            equippedWeapon = weapon,
            equippedRelic = relic,
            difficulty = difficulty,
            bossExpression = PortraitExpression.NEUTRAL,
            speakerExpression = if (startingSoul == SoulType.WHITE_SICK) PortraitExpression.SURPRISED else PortraitExpression.DETERMINED,
            items = battleItems,
            dialogueText = profile.introLine + sickWarning
        )
    }

    fun selectSubTab(tab: MenuSubTab) {
        _state.update { it.copy(subTab = tab) }
    }

    fun switchSoul(soul: SoulType, duringBulletHell: Boolean) {
        _state.update { s ->
            if (soul !in s.allowedSouls || s.activeSoul == soul) return@update s
            if (duringBulletHell && s.soulSwitchCooldownSec > 0f) return@update s
            RetroSoundEngine.playSoulSwitch(soul.ordinal)
            val switchParticles = List(8) { idx ->
                val ang = idx * (2f * PI.toFloat() / 8f)
                ArenaParticle(s.soulX, s.soulY, cos(ang) * 0.35f, sin(ang) * 0.35f, 0.35f, 0.35f, soul.color, 5f)
            }
            s.copy(
                activeSoul = soul,
                isSplitActive = false,
                controlledHalfIndex = 0,
                soulSwitchCooldownSec = if (duringBulletHell) 0.9f else 0f,
                abilityCooldownSec = minOf(s.abilityCooldownSec, 0.4f),
                particles = s.particles + switchParticles,
                dialogueText = if (!duringBulletHell) "Switched to ${soul.title}: ${soul.abilityDesc}" else s.dialogueText
            )
        }
    }

    fun startFightMinigame() {
        _state.update {
            it.copy(
                phase = BattlePhase.FIGHT_MINIGAME,
                subTab = MenuSubTab.ROOT,
                fightCursorPos = 0.08f,
                fightCursorDir = 1f,
                lastStrikeDamage = null
            )
        }
    }

    fun commitFightStrike() {
        _state.update { s ->
            if (s.phase != BattlePhase.FIGHT_MINIGAME) return@update s
            val strike = BattleEngine.calculateStrikeDamage(
                cursorPos = s.fightCursorPos,
                attackBonus = s.attackBonus,
                weapon = s.equippedWeapon,
                relic = s.equippedRelic,
                activeAllies = s.activeAllies,
                activeSoul = s.activeSoul,
                difficulty = s.difficulty
            )
            val finalDamage = strike.damage
            val isCrit = strike.isCritical
            val triggeredDummyPhase2 = s.enemy.id == EnemyId.RUINS_DUMMY && s.dummyPhase == 1
            val rawNewHp = (s.enemyHp - finalDamage).coerceAtLeast(0)
            // Guarantee Phase 2 hat-fall & cotton-spill plays out before Dummy can be defeated
            val newHp = if (triggeredDummyPhase2) rawNewHp.coerceAtLeast(1) else rawNewHp
            val nextDummyPhase = if (triggeredDummyPhase2) 2 else s.dummyPhase
            val nextHatTimer = if (triggeredDummyPhase2) 0.01f else s.dummyHatFallTimerSec
            RetroSoundEngine.playStrike(isCrit && s.activeSoul != SoulType.WHITE_SICK && s.activeSoul != SoulType.PURPLE)
            hitFlashTimer = 0.36f

            val shakeT = if (isCrit || triggeredDummyPhase2) 0.34f else 0.22f
            val shakeAmt = if (isCrit || triggeredDummyPhase2) 1.0f else 0.60f

            if (newHp <= 0) {
                s.copy(
                    enemyHp = 0,
                    dummyPhase = nextDummyPhase,
                    dummyHatFallTimerSec = nextHatTimer,
                    lastStrikeDamage = finalDamage,
                    lastStrikeCritical = isCrit,
                    slashFxTimerSec = 0.55f,
                    isEnemyHitFlash = true,
                    arenaShakeTimerSec = shakeT,
                    arenaShakeIntensity = shakeAmt,
                    bossExpression = PortraitExpression.SURPRISED,
                    speakerExpression = PortraitExpression.ANGRY,
                    phase = BattlePhase.VICTORY,
                    battleWonSpared = false,
                    dialogueText = s.enemy.defeatLine
                )
            } else {
                val quip = s.enemy.attackQuips[(s.turnNumber - 1) % s.enemy.attackQuips.size]
                val msg = when {
                    triggeredDummyPhase2 -> "Phase 2! The Dummy's hat flies off & cotton bursts everywhere! ($finalDamage DMG)  $quip"
                    s.activeSoul == SoulType.WHITE_SICK -> "Sick White Soul hit for ONLY 1 DMG!  $quip"
                    s.activeSoul == SoulType.PURPLE -> "Purple Soul swift strike ($finalDamage DMG)!  $quip"
                    isCrit -> "${s.equippedWeapon.name} CRITICAL HIT! ($finalDamage DMG!)  $quip"
                    else -> "${s.equippedWeapon.name} struck for $finalDamage DMG!  $quip"
                }
                s.copy(
                    enemyHp = newHp,
                    dummyPhase = nextDummyPhase,
                    dummyHatFallTimerSec = nextHatTimer,
                    lastStrikeDamage = finalDamage,
                    lastStrikeCritical = isCrit,
                    slashFxTimerSec = 0.48f,
                    isEnemyHitFlash = true,
                    arenaShakeTimerSec = shakeT,
                    arenaShakeIntensity = shakeAmt,
                    bossExpression = if (triggeredDummyPhase2) PortraitExpression.SURPRISED else PortraitExpression.ANGRY,
                    speakerExpression = if (isCrit) PortraitExpression.DETERMINED else PortraitExpression.ANGRY,
                    phase = BattlePhase.ENEMY_DIALOGUE,
                    dialogueText = msg
                )
            }
        }
    }

    fun performAct(optionId: String) {
        _state.update { s ->
            val eval = BattleEngine.evaluateActStep(
                enemy = s.enemy,
                optionId = optionId,
                currentMercyProgress = s.mercyProgress,
                completedActIds = s.completedActIds,
                relic = s.equippedRelic
            )
            if (!eval.optionFound) return@update s

            if (eval.blockedByUnfriendable) {
                RetroSoundEngine.playSoulSwitch(0)
                return@update s.copy(
                    subTab = MenuSubTab.ROOT,
                    bossExpression = PortraitExpression.ANGRY,
                    speakerExpression = PortraitExpression.DETERMINED,
                    phase = BattlePhase.ENEMY_DIALOGUE,
                    dialogueText = eval.message
                )
            }

            if (eval.outOfOrder) {
                RetroSoundEngine.playGlitchNoise(35)
                return@update s.copy(
                    subTab = MenuSubTab.ROOT,
                    bossExpression = PortraitExpression.ANGRY,
                    speakerExpression = PortraitExpression.SURPRISED,
                    phase = BattlePhase.ENEMY_DIALOGUE,
                    dialogueText = eval.message
                )
            }

            if (eval.alreadyCompletedStep) {
                RetroSoundEngine.playSoulSwitch(0)
                return@update s.copy(
                    subTab = MenuSubTab.ROOT,
                    bossExpression = PortraitExpression.NEUTRAL,
                    speakerExpression = PortraitExpression.SURPRISED,
                    phase = BattlePhase.ENEMY_DIALOGUE,
                    dialogueText = eval.message
                )
            }

            val newMercy = eval.newMercyProgress
            RetroSoundEngine.playSoulSwitch(2)
            s.copy(
                mercyProgress = newMercy,
                completedActIds = eval.completedActIds,
                subTab = MenuSubTab.ROOT,
                bossExpression = when {
                    newMercy >= 100 && s.enemy.canBefriend -> PortraitExpression.HAPPY
                    newMercy >= 100 -> PortraitExpression.SURPRISED
                    s.enemy.canBefriend -> PortraitExpression.SURPRISED
                    else -> PortraitExpression.ANGRY
                },
                speakerExpression = PortraitExpression.DETERMINED,
                phase = BattlePhase.ENEMY_DIALOGUE,
                dialogueText = eval.message
            )
        }
    }

    fun performSpare() {
        _state.update { s ->
            val allowsNonLethal = s.enemy.canBefriend || s.enemy.canStopWithoutKilling
            if (!allowsNonLethal) {
                return@update s.copy(
                    bossExpression = PortraitExpression.ANGRY,
                    speakerExpression = PortraitExpression.SURPRISED,
                    dialogueText = "${s.enemy.name} refuses friendship! Hostile monsters cannot be spared — FIGHT or FLEE!"
                )
            }
            if (!s.canSpare) {
                return@update s.copy(
                    bossExpression = PortraitExpression.ANGRY,
                    speakerExpression = PortraitExpression.SURPRISED,
                    dialogueText = if (s.enemy.canBefriend) {
                        "${s.enemy.name} isn't ready to become friends yet! (${s.mercyProgress}% Mercy)"
                    } else {
                        "${s.enemy.name} won't surrender yet! Use ACT to expose/disarm (${s.mercyProgress}% Stop)."
                    }
                )
            }
            RetroSoundEngine.playSpareChime()
            s.copy(
                phase = BattlePhase.VICTORY,
                battleWonSpared = true,
                bossExpression = if (s.enemy.canBefriend) PortraitExpression.HAPPY else PortraitExpression.SURPRISED,
                speakerExpression = PortraitExpression.HAPPY,
                dialogueText = s.enemy.spareLine
            )
        }
    }

    fun useItem(itemId: String) {
        _state.update { s ->
            val item = s.items.firstOrNull { it.id == itemId && it.count > 0 } ?: return@update s
            val newHp = (s.playerHp + item.healAmount).coerceAtMost(s.maxPlayerHp)
            val updatedItems = s.items.map {
                if (it.id == itemId) it.copy(count = it.count - 1) else it
            }
            RetroSoundEngine.playHealChime()
            s.copy(
                playerHp = newHp,
                hasGreenShield = s.hasGreenShield || item.grantsShield,
                items = updatedItems,
                subTab = MenuSubTab.ROOT,
                bossExpression = if (s.canSpare) PortraitExpression.HAPPY else PortraitExpression.NEUTRAL,
                speakerExpression = PortraitExpression.HAPPY,
                phase = BattlePhase.ENEMY_DIALOGUE,
                dialogueText = "Used ${item.name}! Recovered ${newHp - s.playerHp} HP."
            )
        }
    }

    fun callFriendAid(allyId: EnemyId) {
        _state.update { s ->
            if (allyId !in s.activeAllies) return@update s
            val ally = EnemyRoster.byId(allyId)
            RetroSoundEngine.playHealChime()
            when (allyId) {
                EnemyId.VESPERA, EnemyId.BRAMBLE -> {
                    val healed = (s.playerHp + 5).coerceAtMost(s.maxPlayerHp)
                    s.copy(
                        playerHp = healed,
                        subTab = MenuSubTab.ROOT,
                        speakerExpression = PortraitExpression.HAPPY,
                        phase = BattlePhase.ENEMY_DIALOGUE,
                        dialogueText = "${ally.name} steps in to help! Restored +${healed - s.playerHp} HP!"
                    )
                }
                EnemyId.SERAPHIEL -> {
                    s.copy(
                        hasGreenShield = true,
                        subTab = MenuSubTab.ROOT,
                        speakerExpression = PortraitExpression.DETERMINED,
                        phase = BattlePhase.ENEMY_DIALOGUE,
                        dialogueText = "Seraphiel spreads golden feathers! Prism Shield raised!"
                    )
                }
                else -> {
                    if (s.enemy.canBefriend || s.enemy.canStopWithoutKilling) {
                        val bonusMercy = (s.mercyProgress + 25).coerceAtMost(100)
                        s.copy(
                            mercyProgress = bonusMercy,
                            subTab = MenuSubTab.ROOT,
                            bossExpression = if (bonusMercy >= 100 && s.enemy.canBefriend) PortraitExpression.HAPPY else PortraitExpression.SURPRISED,
                            speakerExpression = PortraitExpression.HAPPY,
                            phase = BattlePhase.ENEMY_DIALOGUE,
                            dialogueText = if (s.enemy.canBefriend) {
                                "${ally.name} speaks up for Loka! Enemy Mercy +25%!"
                            } else {
                                "${ally.name} helps expose Citadel corruption! +25% Stop progress!"
                            }
                        )
                    } else {
                        val healed = (s.playerHp + 4).coerceAtMost(s.maxPlayerHp)
                        s.copy(
                            playerHp = healed,
                            subTab = MenuSubTab.ROOT,
                            bossExpression = PortraitExpression.ANGRY,
                            speakerExpression = PortraitExpression.DETERMINED,
                            phase = BattlePhase.ENEMY_DIALOGUE,
                            dialogueText = "${ally.name} guards Loka (+${healed - s.playerHp} HP)! ${s.enemy.name} refuses mercy!"
                        )
                    }
                }
            }
        }
    }

    fun callLostKidAid(kidId: String) {
        _state.update { s ->
            if (kidId !in s.helpedKids) return@update s
            val kid = WorldContentCatalog.lostKids.firstOrNull { it.id == kidId } ?: return@update s
            RetroSoundEngine.playHealChime()
            val heal = if (kid.healAmount > 0) kid.healAmount else 4
            val healedHp = (s.playerHp + heal).coerceAtMost(s.maxPlayerHp)
            s.copy(
                playerHp = healedHp,
                subTab = MenuSubTab.ROOT,
                speakerExpression = PortraitExpression.HAPPY,
                phase = BattlePhase.ENEMY_DIALOGUE,
                dialogueText = "${kid.name} cheers Loka on and heals +${healedHp - s.playerHp} HP!"
            )
        }
    }

    fun advanceFromDialogueToDodging() {
        _state.update { s ->
            if (s.phase != BattlePhase.ENEMY_DIALOGUE) return@update s
            s.copy(
                phase = BattlePhase.BULLET_HELL,
                ashenHelpingThisTurn = false,
                phaseTimerSec = 0f,
                phaseDurationSec = when (s.enemy.id) {
                    EnemyId.RUINS_DUMMY,
                    EnemyId.NORMAL_SLIME,
                    EnemyId.SAND_MOTH,
                    EnemyId.HALO_SCRIBE,
                    EnemyId.FERAL_HOUND,
                    EnemyId.IRON_STALKER,
                    EnemyId.ASH_IMP,
                    EnemyId.HUMAN_DRIFTER -> 45.0f

                    EnemyId.VEX,
                    EnemyId.BRAMBLE,
                    EnemyId.VESPERA,
                    EnemyId.ORIN,
                    EnemyId.GOV_MONSTER_CHIEF,
                    EnemyId.SERAPHIEL,
                    EnemyId.MALAKOR,
                    EnemyId.GOV_DIRECTOR,
                    EnemyId.ECHO_LOKA,
                    EnemyId.FUTURE_LOKA -> 60.0f
                },
                spawnAccumulatorSec = 0.35f,
                isRewindingTime = false,
                purpleMirageTimerSec = 0f,
                isSplitActive = false,
                controlledHalfIndex = 0,
                splitBoostTimer0Sec = 0f,
                splitBoostTimer1Sec = 0f,
                soulX = 0.5f,
                soulY = 0.58f,
                secondHalfX = 0.62f,
                secondHalfY = 0.58f,
                redEchoX = 0.5f,
                redEchoY = 0.72f,
                bullets = emptyList(),
                lasers = emptyList(),
                laneTelegraphs = emptyList(),
                jawAttacks = emptyList(),
                arrowTelegraphs = emptyList(),
                bombs = emptyList(),
                sideWarnings = emptyList(),
                particles = emptyList()
            )
        }
    }

    fun moveSoulByDelta(dxNorm: Float, dyNorm: Float) {
        if (hypot(dxNorm, dyNorm) > 0.002f) {
            lastAimX = dxNorm
            lastAimY = dyNorm
        }
        _state.update { s ->
            if (s.phase != BattlePhase.BULLET_HELL) return@update s
            val margin = 0.055f
            val baseDrag = when (s.activeSoul) {
                SoulType.RED -> 0.85f
                SoulType.WHITE_SICK -> 0.52f
                SoulType.GREEN -> 0.68f
                SoulType.PURPLE -> if (s.purpleMirageTimerSec > 0f) 1.45f else 1.28f
                SoulType.SPLIT_DUO -> if (s.activeSplitBoosted) 1.45f else 0.95f
                else -> 1f
            }
            val dragFactor = baseDrag * s.speedMultiplier
            s.copy(
                soulX = (s.soulX + dxNorm * dragFactor).coerceIn(margin, 1f - margin),
                soulY = (s.soulY + dyNorm * dragFactor).coerceIn(margin, 1f - margin)
            )
        }
    }

    fun setDpadDirection(dx: Float, dy: Float) {
        dpadInputX = dx
        dpadInputY = dy
        if (hypot(dx, dy) > 0.05f) {
            lastAimX = dx
            lastAimY = dy
        }
    }

    fun triggerSoulAbility() {
        _state.update { s ->
            if (s.phase != BattlePhase.BULLET_HELL || s.abilityCooldownSec > 0f) return@update s
            RetroSoundEngine.playAbility()
            val cdMult = if (EnemyId.ORIN in s.activeAllies) 0.8f else 1f
            val nextCd = s.activeSoul.cooldownSec * cdMult

            when (s.activeSoul) {
                SoulType.RED -> {
                    val oldX = s.soulX
                    val oldY = s.soulY
                    val targetX = s.redEchoX.coerceIn(0.07f, 0.93f)
                    val targetY = s.redEchoY.coerceIn(0.07f, 0.93f)
                    val trail = listOf(
                        ArenaParticle(oldX, oldY, 0f, 0f, 0.42f, 0.42f, SoulType.RED.color, 12f),
                        ArenaParticle((oldX + targetX) * 0.5f, (oldY + targetY) * 0.5f, 0f, 0f, 0.35f, 0.35f, Color.White, 9f)
                    )
                    s.copy(
                        soulX = targetX,
                        soulY = targetY,
                        redEchoX = oldX,
                        redEchoY = oldY,
                        invulnTimerSec = maxOf(s.invulnTimerSec, 0.50f),
                        abilityCooldownSec = nextCd,
                        particles = s.particles + trail
                    )
                }

                SoulType.YELLOW -> {
                    val shots = listOf(-0.34f, -0.17f, 0f, 0.17f, 0.34f).map { vx ->
                        Bullet(
                            id = nextEntityId++,
                            x = s.soulX,
                            y = s.soulY - 0.04f,
                            vx = vx,
                            vy = -1.20f,
                            radiusNorm = 0.025f,
                            kind = BulletKind.PLAYER_SHOT,
                            color = SoulType.YELLOW.color
                        )
                    }
                    s.copy(bullets = s.bullets + shots, abilityCooldownSec = nextCd)
                }

                SoulType.BLUE -> {
                    val repelled = s.bullets.map { b ->
                        if (b.kind == BulletKind.PLAYER_SHOT) b
                        else {
                            val dx = b.x - s.soulX
                            val dy = b.y - s.soulY
                            val dist = hypot(dx, dy).coerceAtLeast(0.02f)
                            if (dist < 0.42f) {
                                b.copy(vx = (dx / dist) * 0.65f, vy = (dy / dist) * 0.65f)
                            } else b
                        }
                    }
                    val ring = List(10) { i ->
                        val a = i * (2f * PI.toFloat() / 10f)
                        ArenaParticle(s.soulX, s.soulY, cos(a) * 0.55f, sin(a) * 0.55f, 0.38f, 0.38f, SoulType.BLUE.color, 7f)
                    }
                    s.copy(
                        bullets = repelled,
                        invulnTimerSec = maxOf(s.invulnTimerSec, 0.32f),
                        abilityCooldownSec = nextCd,
                        particles = s.particles + ring
                    )
                }

                SoulType.PURPLE -> {
                    s.copy(
                        purpleMirageTimerSec = 1.35f,
                        abilityCooldownSec = nextCd,
                        particles = s.particles + ArenaParticle(s.soulX, s.soulY, 0f, 0f, 0.5f, 0.5f, SoulType.PURPLE.color, 15f)
                    )
                }

                SoulType.GREEN -> {
                    s.copy(
                        hasGreenShield = true,
                        abilityCooldownSec = nextCd,
                        particles = s.particles + ArenaParticle(s.soulX, s.soulY, 0f, 0f, 0.4f, 0.4f, SoulType.GREEN.color, 14f)
                    )
                }

                SoulType.SPLIT_DUO -> {
                    if (!s.isSplitActive) {
                        // First [C] press: Split into Red half (left) and Teal half (right)!
                        val leftX = (s.soulX - 0.11f).coerceIn(0.08f, 0.85f)
                        val rightX = (s.soulX + 0.11f).coerceIn(0.15f, 0.92f)
                        val burst = listOf(
                            ArenaParticle(leftX, s.soulY, -0.35f, 0f, 0.35f, 0.35f, SoulType.SPLIT_DUO.color, 8f),
                            ArenaParticle(rightX, s.soulY, 0.35f, 0f, 0.35f, 0.35f, SoulType.SPLIT_DUO.secondaryColor ?: GlitchCyan, 8f)
                        )
                        s.copy(
                            isSplitActive = true,
                            controlledHalfIndex = 0,
                            soulX = leftX,
                            secondHalfX = rightX,
                            secondHalfY = s.soulY,
                            abilityCooldownSec = nextCd,
                            particles = s.particles + burst
                        )
                    } else {
                        // Already split: check directional input aiming toward the second half -> switch control!
                        val aimX = if (dpadInputX != 0f || dpadInputY != 0f) dpadInputX else lastAimX
                        val aimY = if (dpadInputX != 0f || dpadInputY != 0f) dpadInputY else lastAimY
                        val toOtherX = s.secondHalfX - s.soulX
                        val toOtherY = s.secondHalfY - s.soulY
                        val dot = aimX * toOtherX + aimY * toOtherY
                        val aimedTowardOther = (aimX == 0f && aimY == 0f) || dot >= -0.02f
                        if (aimedTowardOther) {
                            val nextHalf = 1 - s.controlledHalfIndex
                            val swapFx = listOf(
                                ArenaParticle(s.soulX, s.soulY, 0f, 0f, 0.3f, 0.3f, Color.White, 9f),
                                ArenaParticle(s.secondHalfX, s.secondHalfY, 0f, 0f, 0.35f, 0.35f,
                                    if (nextHalf == 0) SoulType.SPLIT_DUO.color else (SoulType.SPLIT_DUO.secondaryColor ?: GlitchCyan), 12f)
                            )
                            s.copy(
                                controlledHalfIndex = nextHalf,
                                soulX = s.secondHalfX,
                                soulY = s.secondHalfY,
                                secondHalfX = s.soulX,
                                secondHalfY = s.soulY,
                                abilityCooldownSec = nextCd,
                                particles = s.particles + swapFx
                            )
                        } else {
                            // Aimed directly away: quick dash nudge on active half
                            s.copy(abilityCooldownSec = nextCd)
                        }
                    }
                }

                SoulType.WHITE_SICK -> {
                    s.copy(
                        invulnTimerSec = maxOf(s.invulnTimerSec, 0.22f),
                        abilityCooldownSec = nextCd,
                        particles = s.particles + ArenaParticle(s.soulX, s.soulY, 0f, 0f, 0.3f, 0.3f, Color.White, 8f)
                    )
                }
            }
        }
    }

    fun tick(dtRaw: Float) {
        val dt = dtRaw.coerceIn(0.001f, 0.05f)
        if (hitFlashTimer > 0f) {
            hitFlashTimer = (hitFlashTimer - dt).coerceAtLeast(0f)
        }

        _state.update { s ->
            val flashState = hitFlashTimer > 0f
            val nextSlash = (s.slashFxTimerSec - dt).coerceAtLeast(0f)
            val nextPlayerHit = (s.playerHitFlashTimerSec - dt).coerceAtLeast(0f)
            val nextShake = (s.arenaShakeTimerSec - dt).coerceAtLeast(0f)
            val nextShakeIntensity = if (nextShake > 0f) s.arenaShakeIntensity else 0f
            val nextDmgPop = (s.playerDamagePopTimerSec - dt).coerceAtLeast(0f)
            val nextShieldPop = (s.shieldBlockedTimerSec - dt).coerceAtLeast(0f)
            val nextHatTimer = if (s.dummyPhase >= 2 && s.dummyHatFallTimerSec < 0.85f) {
                (s.dummyHatFallTimerSec + dt).coerceAtMost(0.85f)
            } else s.dummyHatFallTimerSec
            when (s.phase) {
                BattlePhase.FIGHT_MINIGAME -> {
                    // Classic Undertale Fight Bar sweeps across the target eye [0.06 .. 0.94] toward center x = 0.50f
                    var nextPos = s.fightCursorPos + s.fightCursorDir * dt * 0.95f * s.equippedWeapon.cursorSpeedMultiplier
                    var nextDir = s.fightCursorDir
                    if (nextPos >= 0.94f) {
                        nextPos = 0.94f
                        nextDir = -1f
                    } else if (nextPos <= 0.06f) {
                        nextPos = 0.06f
                        nextDir = 1f
                    }
                    s.copy(
                        fightCursorPos = nextPos,
                        fightCursorDir = nextDir,
                        dummyHatFallTimerSec = nextHatTimer,
                        isEnemyHitFlash = flashState,
                        slashFxTimerSec = nextSlash,
                        playerHitFlashTimerSec = nextPlayerHit,
                        arenaShakeTimerSec = nextShake,
                        arenaShakeIntensity = nextShakeIntensity,
                        playerDamagePopTimerSec = nextDmgPop,
                        shieldBlockedTimerSec = nextShieldPop
                    )
                }

                BattlePhase.BULLET_HELL -> simulateBulletHell(
                    s = s.copy(
                        dummyHatFallTimerSec = nextHatTimer,
                        playerHitFlashTimerSec = nextPlayerHit,
                        arenaShakeTimerSec = nextShake,
                        arenaShakeIntensity = nextShakeIntensity,
                        playerDamagePopTimerSec = nextDmgPop,
                        shieldBlockedTimerSec = nextShieldPop
                    ),
                    dt = dt,
                    flashState = flashState
                ).copy(slashFxTimerSec = nextSlash)

                else -> if (
                    s.dummyHatFallTimerSec != nextHatTimer ||
                    s.isEnemyHitFlash != flashState ||
                    s.slashFxTimerSec != nextSlash ||
                    s.playerHitFlashTimerSec != nextPlayerHit ||
                    s.arenaShakeTimerSec != nextShake ||
                    s.playerDamagePopTimerSec != nextDmgPop ||
                    s.shieldBlockedTimerSec != nextShieldPop
                ) {
                    s.copy(
                        dummyHatFallTimerSec = nextHatTimer,
                        isEnemyHitFlash = flashState,
                        slashFxTimerSec = nextSlash,
                        playerHitFlashTimerSec = nextPlayerHit,
                        arenaShakeTimerSec = nextShake,
                        arenaShakeIntensity = nextShakeIntensity,
                        playerDamagePopTimerSec = nextDmgPop,
                        shieldBlockedTimerSec = nextShieldPop
                    )
                } else s
            }
        }
    }

    private fun simulateBulletHell(s: BattleUiState, dt: Float, flashState: Boolean): BattleUiState {
        val nextPhaseTimer = s.phaseTimerSec + dt
        if (nextPhaseTimer >= s.phaseDurationSec) {
            val nextTurn = s.turnNumber + 1
            val vesperaRegen = if (EnemyId.VESPERA in s.activeAllies && s.playerHp < s.maxPlayerHp) 1 else 0
            val baseHp = (s.playerHp + vesperaRegen).coerceAtMost(s.maxPlayerHp)
            val canAshenJoin = s.enemy.id != EnemyId.RUINS_DUMMY &&
                s.worldId != WorldId.GLITCHED_WORLD &&
                (baseHp <= (s.maxPlayerHp * 0.45f).toInt() || Random.nextFloat() < 0.35f)
            val finalHp = if (canAshenJoin) (baseHp + 5).coerceAtMost(s.maxPlayerHp) else baseHp
            val finalShield = s.hasGreenShield || canAshenJoin
            val allowsNonLethal = s.enemy.canBefriend || s.enemy.canStopWithoutKilling
            val finalMercy = if (canAshenJoin && allowsNonLethal) (s.mercyProgress + 12).coerceAtMost(100) else s.mercyProgress
            if (canAshenJoin) {
                RetroSoundEngine.playAbility()
            }
            return s.copy(
                phase = BattlePhase.PLAYER_MENU,
                subTab = MenuSubTab.ROOT,
                turnNumber = nextTurn,
                playerHp = finalHp,
                hasGreenShield = finalShield,
                mercyProgress = finalMercy,
                ashenHelpingThisTurn = canAshenJoin,
                bullets = emptyList(),
                lasers = emptyList(),
                laneTelegraphs = emptyList(),
                jawAttacks = emptyList(),
                arrowTelegraphs = emptyList(),
                bombs = emptyList(),
                sideWarnings = emptyList(),
                particles = emptyList(),
                isRewindingTime = false,
                purpleMirageTimerSec = 0f,
                isSplitActive = false,
                splitBoostTimer0Sec = 0f,
                splitBoostTimer1Sec = 0f,
                isEnemyHitFlash = false,
                playerHitFlashTimerSec = 0f,
                arenaShakeTimerSec = 0f,
                arenaShakeIntensity = 0f,
                playerDamagePopTimerSec = 0f,
                shieldBlockedTimerSec = 0f,
                dialogueText = when {
                    canAshenJoin && s.enemy.canBefriend -> "* Ashen joined the fight to help! +5 HP, Shield raised & +12% Mercy!"
                    canAshenJoin && s.enemy.canStopWithoutKilling -> "* Ashen helps! +5 HP, Shield & +12% Stop progress! (Stop without killing or FIGHT)"
                    canAshenJoin -> "* Ashen helps! +5 HP & Shield raised! (${s.enemy.name} won't accept Mercy—FIGHT or FLEE!)"
                    !allowsNonLethal -> "Turn $nextTurn — Hostile monster! FIGHT, ITEM, or FLEE!"
                    finalMercy >= 100 && !s.enemy.canBefriend -> "${s.enemy.name} is cornered! Press STOP (MERCY) to disarm without killing!"
                    finalMercy >= 100 -> "${s.enemy.name} is SPARE-ready! Press MERCY to befriend!"
                    !s.enemy.canBefriend -> "Turn $nextTurn — Cannot befriend! Use FIGHT, or ACT + STOP without killing."
                    else -> "Turn $nextTurn — Choose FIGHT, ACT, ITEM, or MERCY."
                }
            )
        }

        val margin = 0.055f
        val splitBoostMult = if (s.activeSplitBoosted) 1.62f else 1f
        val speed = s.activeSoul.speedNormPerSec *
            (if (s.purpleMirageTimerSec > 0f) 1.25f else 1f) *
            splitBoostMult *
            s.speedMultiplier
        var sx = s.soulX
        var sy = s.soulY
        if (dpadInputX != 0f || dpadInputY != 0f) {
            val mag = hypot(dpadInputX, dpadInputY).coerceAtLeast(1f)
            sx = (sx + (dpadInputX / mag) * speed * dt).coerceIn(margin, 1f - margin)
            sy = (sy + (dpadInputY / mag) * speed * dt).coerceIn(margin, 1f - margin)
        }

        if (s.activeSoul == SoulType.WHITE_SICK && Random.nextFloat() < dt * 2.5f) {
            sx = (sx + (Random.nextFloat() - 0.5f) * 0.024f).coerceIn(margin, 1f - margin)
        }

        val rewinding = s.enemy.id == EnemyId.ECHO_LOKA && (nextPhaseTimer % 15f) in 6.5f..8.2f
        val timeScale = if (rewinding) -0.85f else 1f

        var spawnAcc = s.spawnAccumulatorSec + dt
        val spawnedBullets = mutableListOf<Bullet>()
        val spawnedLasers = mutableListOf<LaserBeam>()
        val spawnedLanes = mutableListOf<LaneTelegraph>()
        val spawnedJaws = mutableListOf<MonsterJawAttack>()
        val spawnedArrows = mutableListOf<ArrowTelegraph>()
        val spawnedBombs = mutableListOf<ArenaBomb>()
        val spawnedSideWarn = mutableListOf<SideWallShotWarning>()

        val spawnInterval = when (s.enemy.id) {
            EnemyId.RUINS_DUMMY -> 1.15f
            EnemyId.NORMAL_SLIME, EnemyId.SAND_MOTH, EnemyId.HALO_SCRIBE, EnemyId.HUMAN_DRIFTER -> 0.88f
            EnemyId.FERAL_HOUND, EnemyId.IRON_STALKER, EnemyId.ASH_IMP -> 0.78f
            EnemyId.VEX, EnemyId.BRAMBLE -> 0.72f
            EnemyId.VESPERA, EnemyId.ORIN, EnemyId.GOV_MONSTER_CHIEF -> 0.66f
            EnemyId.SERAPHIEL, EnemyId.MALAKOR, EnemyId.GOV_DIRECTOR, EnemyId.ECHO_LOKA -> 0.60f
            EnemyId.FUTURE_LOKA -> 0.55f
        }

        if (!rewinding && spawnAcc >= spawnInterval) {
            spawnAcc -= spawnInterval
            spawnEnemyWave(
                enemyId = s.enemy.id,
                turn = s.turnNumber,
                phaseTime = nextPhaseTimer,
                soulX = sx,
                soulY = sy,
                outBullets = spawnedBullets,
                outLasers = spawnedLasers,
                outLanes = spawnedLanes,
                outJaws = spawnedJaws,
                outArrows = spawnedArrows,
                outBombs = spawnedBombs,
                outSideWarnings = spawnedSideWarn
            )
        }

        if (s.activeSoul == SoulType.YELLOW && ((nextPhaseTimer * 10f).toInt() % 9 == 0) && Random.nextFloat() < dt * 12f) {
            spawnedBullets += Bullet(nextEntityId++, sx, sy - 0.04f, 0f, -1.05f, 0.022f, BulletKind.PLAYER_SHOT, SoulType.YELLOW.color)
        }

        // Update Arrow Telegraphs -> convert ready arrows into actual projectiles! (Image 2 Attack #3)
        val nextArrows = mutableListOf<ArrowTelegraph>()
        for (arw in (s.arrowTelegraphs + spawnedArrows)) {
            val updated = arw.copy(elapsedSec = arw.elapsedSec + dt)
            if (updated.readyToFire) {
                spawnedBullets += Bullet(
                    id = nextEntityId++,
                    x = updated.originX,
                    y = updated.originY,
                    vx = updated.dirX * 0.54f,
                    vy = updated.dirY * 0.54f,
                    radiusNorm = 0.032f,
                    kind = BulletKind.LANCE,
                    color = updated.color
                )
            } else {
                nextArrows += updated
            }
        }

        // Update Side Wall Warnings -> fire side bullets across arena! (Image 2 Attack #5)
        val nextSideWarn = mutableListOf<SideWallShotWarning>()
        for (sw in (s.sideWarnings + spawnedSideWarn)) {
            val updated = sw.copy(elapsedSec = sw.elapsedSec + dt)
            if (updated.readyToShoot) {
                val startX = if (updated.fromRightEdge) 0.99f else 0.01f
                val vx = if (updated.fromRightEdge) -0.58f else 0.58f
                spawnedBullets += Bullet(
                    id = nextEntityId++,
                    x = startX,
                    y = updated.yNorm,
                    vx = vx,
                    vy = 0f,
                    radiusNorm = 0.030f,
                    kind = BulletKind.LANCE,
                    color = updated.color
                )
            } else {
                nextSideWarn += updated
            }
        }

        val extraParticles = mutableListOf<ArenaParticle>()

        // Update Fuse Bombs -> explode into radial shrapnel! (Image 2 Attack #4)
        val nextBombs = mutableListOf<ArenaBomb>()
        for (bomb in (s.bombs + spawnedBombs)) {
            val nx = (bomb.x + bomb.vx * dt).coerceIn(0.10f, 0.90f)
            val ny = (bomb.y + bomb.vy * dt).coerceIn(0.12f, 0.88f)
            val updated = bomb.copy(x = nx, y = ny, vx = bomb.vx * 0.92f, vy = bomb.vy * 0.92f, elapsedSec = bomb.elapsedSec + dt)
            if (updated.shouldExplode) {
                RetroSoundEngine.playGlitchNoise(45)
                for (i in 0 until 8) {
                    val ang = i * (2f * PI.toFloat() / 8f)
                    spawnedBullets += Bullet(
                        id = nextEntityId++,
                        x = nx,
                        y = ny,
                        vx = cos(ang) * 0.44f,
                        vy = sin(ang) * 0.44f,
                        radiusNorm = 0.026f,
                        kind = BulletKind.ORB,
                        color = updated.color
                    )
                }
                repeat(8) {
                    val ang = Random.nextFloat() * 2f * PI.toFloat()
                    extraParticles += ArenaParticle(nx, ny, cos(ang) * 0.6f, sin(ang) * 0.6f, 0.35f, 0.35f, Color(0xFFF97316), 7f)
                }
            } else {
                nextBombs += updated
            }
        }

        val allCurrent = s.bullets + spawnedBullets
        val playerShots = allCurrent.filter { it.kind == BulletKind.PLAYER_SHOT }
        val enemyBulletsRaw = allCurrent.filter { it.kind != BulletKind.PLAYER_SHOT }

        val destroyedEnemyIds = mutableSetOf<Long>()
        val nextPlayerShots = mutableListOf<Bullet>()
        var enemyHpNow = s.enemyHp
        var enemyFlashedNow = flashState

        for (ps in playerShots) {
            val nx = ps.x + ps.vx * dt
            val ny = ps.y + ps.vy * dt
            if (ny < 0.0f) {
                if (enemyHpNow > 1) {
                    enemyHpNow = (enemyHpNow - 2).coerceAtLeast(1)
                    hitFlashTimer = 0.14f
                    enemyFlashedNow = true
                }
                extraParticles += ArenaParticle(nx.coerceIn(0.05f, 0.95f), 0.04f, 0f, 0.15f, 0.3f, 0.3f, ps.color, 7f)
                continue
            }
            if (nx < -0.1f || nx > 1.1f || ny > 1.1f) continue
            var consumed = false
            for (eb in enemyBulletsRaw) {
                if (eb.id !in destroyedEnemyIds && hypot(nx - eb.x, ny - eb.y) < 0.06f) {
                    destroyedEnemyIds += eb.id
                    consumed = true
                    extraParticles += ArenaParticle(eb.x, eb.y, 0f, 0f, 0.28f, 0.28f, ps.color, 6f)
                    break
                }
            }
            if (!consumed) nextPlayerShots += ps.copy(x = nx, y = ny)
        }

        val nextBullets = mutableListOf<Bullet>()
        nextBullets += nextPlayerShots

        var hp = s.playerHp
        var invuln = (s.invulnTimerSec - dt).coerceAtLeast(0f)
        val mirageActive = s.purpleMirageTimerSec > 0f
        val nextMirage = (s.purpleMirageTimerSec - dt).coerceAtLeast(0f)
        var shield = s.hasGreenShield
        var boost0 = (s.splitBoostTimer0Sec - dt).coerceAtLeast(0f)
        var boost1 = (s.splitBoostTimer1Sec - dt).coerceAtLeast(0f)

        val splitScale = if (s.isSplitActive) 0.85f else 1f
        val hitRadius = 0.032f * s.activeSoul.hitboxScale * splitScale

        val totalDefense = s.defenseBonus + s.activeSoul.defenseBonus
        val actualIncoming = BattleEngine.calculateIncomingDamage(
            enemyAttackPower = s.enemy.attackPower,
            totalDefense = totalDefense,
            activeSoul = s.activeSoul,
            difficulty = s.difficulty
        )

        var playerHitTimer = s.playerHitFlashTimerSec
        var shakeTimer = s.arenaShakeTimerSec
        var shakeIntensity = s.arenaShakeIntensity
        var lastPlayerDmg = s.playerLastDamageTaken
        var dmgPopTimer = s.playerDamagePopTimerSec
        var shieldPopTimer = s.shieldBlockedTimerSec

        fun applyDamageAt(hitX: Float, hitY: Float, hitOnControlledHalf: Boolean) {
            if (shield) {
                shield = false
                invuln = 0.65f
                shieldPopTimer = 0.75f
                shakeTimer = maxOf(shakeTimer, 0.16f)
                shakeIntensity = maxOf(shakeIntensity, 0.45f)
                RetroSoundEngine.playAbility()
                if (s.activeSoul == SoulType.GREEN) {
                    if (hp < s.maxPlayerHp) hp++
                    nextBullets += Bullet(nextEntityId++, sx, sy - 0.04f, 0f, -1.25f, 0.03f, BulletKind.PLAYER_SHOT, SoulType.GREEN.color)
                }
                extraParticles += List(6) { i ->
                    val a = i * (2f * PI.toFloat() / 6f)
                    ArenaParticle(hitX, hitY, cos(a) * 0.42f, sin(a) * 0.42f, 0.36f, 0.36f, SoulType.GREEN.color, 8f)
                }
            } else {
                hp = (hp - actualIncoming).coerceAtLeast(0)
                invuln = 0.95f
                playerHitTimer = 0.32f
                shakeTimer = maxOf(shakeTimer, 0.28f)
                shakeIntensity = maxOf(shakeIntensity, 0.90f)
                lastPlayerDmg = actualIncoming
                dmgPopTimer = 0.85f
                RetroSoundEngine.playHurt()
                // Split Soul rule: when 1 half takes damage, the OTHER half becomes faster!
                if (s.activeSoul == SoulType.SPLIT_DUO && s.isSplitActive) {
                    val damagedHalf = if (hitOnControlledHalf) s.controlledHalfIndex else (1 - s.controlledHalfIndex)
                    if (damagedHalf == 0) {
                        boost1 = 4.5f
                    } else {
                        boost0 = 4.5f
                    }
                }
                extraParticles += List(10) {
                    val col = if (it % 3 == 0) Color.White else Color.Red
                    ArenaParticle(hitX, hitY, (Random.nextFloat() - 0.5f) * 0.85f, (Random.nextFloat() - 0.5f) * 0.85f, 0.38f, 0.38f, col, 6.5f)
                }
            }
        }

        val bulletTimeScale = timeScale * s.difficulty.enemyBulletSpeedMult
        for (b in enemyBulletsRaw) {
            if (b.id in destroyedEnemyIds) continue
            var nvx = b.vx
            var nvy = b.vy

            if (s.activeSoul == SoulType.BLUE) {
                val dx = b.x - sx
                val dy = b.y - sy
                val d = hypot(dx, dy)
                if (d in 0.05f..0.22f) {
                    nvx += (dx / d) * dt * 0.48f
                    nvy += (dy / d) * dt * 0.48f
                }
            }

            var nx = b.x + nvx * dt * bulletTimeScale
            var ny = b.y + nvy * dt * bulletTimeScale
            var bounces = b.bouncesLeft

            if (bounces > 0) {
                if (nx < 0.03f || nx > 0.97f) {
                    nvx = -nvx; nx = nx.coerceIn(0.03f, 0.97f); bounces--
                }
                if (ny < 0.03f || ny > 0.97f) {
                    nvy = -nvy; ny = ny.coerceIn(0.03f, 0.97f); bounces--
                }
            }
            if (nx < -0.15f || nx > 1.15f || ny < -0.15f || ny > 1.15f) continue

            val distMain = hypot(nx - sx, ny - sy)
            val distSecond = if (s.isSplitActive) hypot(nx - s.secondHalfX, ny - s.secondHalfY) else 99f
            val hitThresh = hitRadius + b.radiusNorm * 0.72f
            val hitMain = !mirageActive && distMain < hitThresh
            val hitSecond = !mirageActive && s.isSplitActive && distSecond < hitThresh

            if ((hitMain || hitSecond) && invuln <= 0f) {
                val blocked = shield
                applyDamageAt(if (hitMain) sx else s.secondHalfX, if (hitMain) sy else s.secondHalfY, hitMain)
                if (blocked) continue
            }

            nextBullets += b.copy(x = nx, y = ny, vx = nvx, vy = nvy, bouncesLeft = bounces, ageSec = b.ageSec + dt)
        }

        val nextLasers = mutableListOf<LaserBeam>()
        for (l in (s.lasers + spawnedLasers)) {
            val updated = l.copy(elapsedSec = l.elapsedSec + dt)
            if (!updated.isExpired) {
                if (updated.isFiring && invuln <= 0f) {
                    val distMain = if (updated.isHorizontal) abs(sy - updated.positionNorm) else abs(sx - updated.positionNorm)
                    val distSec = if (s.isSplitActive) {
                        if (updated.isHorizontal) abs(s.secondHalfY - updated.positionNorm) else abs(s.secondHalfX - updated.positionNorm)
                    } else 99f
                    val thresh = updated.thicknessNorm * 0.5f + 0.022f * s.activeSoul.hitboxScale
                    if (distMain < thresh) {
                        applyDamageAt(sx, sy, true)
                    } else if (distSec < thresh) {
                        applyDamageAt(s.secondHalfX, s.secondHalfY, false)
                    }
                }
                nextLasers += updated
            }
        }

        // Update Rectangular Lane Telegraphs (Image 2 Attack #1)
        val nextLanes = mutableListOf<LaneTelegraph>()
        for (ln in (s.laneTelegraphs + spawnedLanes)) {
            val updated = ln.copy(elapsedSec = ln.elapsedSec + dt)
            if (!updated.isExpired) {
                if (updated.isFiring && invuln <= 0f) {
                    val thresh = updated.widthNorm * 0.48f + 0.018f * s.activeSoul.hitboxScale
                    if (abs(sx - updated.xNorm) < thresh) {
                        applyDamageAt(sx, sy, true)
                    } else if (s.isSplitActive && abs(s.secondHalfX - updated.xNorm) < thresh) {
                        applyDamageAt(s.secondHalfX, s.secondHalfY, false)
                    }
                }
                nextLanes += updated
            }
        }

        // Update Boss Chomp Jaw + Claw Attacks (Image 2 Attack #2)
        val nextJaws = mutableListOf<MonsterJawAttack>()
        for (jaw in (s.jawAttacks + spawnedJaws)) {
            val updated = jaw.copy(elapsedSec = jaw.elapsedSec + dt)
            if (!updated.isExpired) {
                if (updated.isChomping && invuln <= 0f) {
                    val jawHit = hypot(sx - updated.xNorm, sy - updated.yNorm) < updated.radiusNorm + hitRadius
                    val clawHit = hypot(sx - (updated.xNorm + updated.clawOffsetX), sy - updated.yNorm) < updated.radiusNorm * 0.85f + hitRadius
                    val secHit = s.isSplitActive && (
                        hypot(s.secondHalfX - updated.xNorm, s.secondHalfY - updated.yNorm) < updated.radiusNorm + hitRadius ||
                            hypot(s.secondHalfX - (updated.xNorm + updated.clawOffsetX), s.secondHalfY - updated.yNorm) < updated.radiusNorm * 0.85f + hitRadius
                        )
                    if (jawHit || clawHit) {
                        applyDamageAt(sx, sy, true)
                    } else if (secHit) {
                        applyDamageAt(s.secondHalfX, s.secondHalfY, false)
                    }
                }
                nextJaws += updated
            }
        }

        val nextParticles = (s.particles + extraParticles).mapNotNull { p ->
            val rem = p.life - dt
            if (rem <= 0f) null else p.copy(x = p.x + p.vx * dt, y = p.y + p.vy * dt, life = rem)
        }

        if (hp <= 0) {
            return s.copy(
                playerHp = 0,
                phase = BattlePhase.DEFEAT,
                bullets = emptyList(),
                lasers = emptyList(),
                laneTelegraphs = emptyList(),
                jawAttacks = emptyList(),
                arrowTelegraphs = emptyList(),
                bombs = emptyList(),
                sideWarnings = emptyList(),
                particles = nextParticles,
                dialogueText = "Your Soul shattered... Time pulls you right back. Try again!"
            )
        }

        return s.copy(
            enemyHp = enemyHpNow,
            soulX = sx,
            soulY = sy,
            playerHp = hp,
            invulnTimerSec = invuln,
            hasGreenShield = shield,
            greenOrbitAngle = (s.greenOrbitAngle + dt * 210f) % 360f,
            purpleMirageTimerSec = nextMirage,
            splitBoostTimer0Sec = boost0,
            splitBoostTimer1Sec = boost1,
            soulSwitchCooldownSec = (s.soulSwitchCooldownSec - dt).coerceAtLeast(0f),
            abilityCooldownSec = (s.abilityCooldownSec - dt).coerceAtLeast(0f),
            phaseTimerSec = nextPhaseTimer,
            spawnAccumulatorSec = spawnAcc,
            isRewindingTime = rewinding,
            bullets = nextBullets,
            lasers = nextLasers,
            laneTelegraphs = nextLanes,
            jawAttacks = nextJaws,
            arrowTelegraphs = nextArrows,
            bombs = nextBombs,
            sideWarnings = nextSideWarn,
            particles = nextParticles,
            isEnemyHitFlash = enemyFlashedNow,
            playerHitFlashTimerSec = playerHitTimer,
            arenaShakeTimerSec = shakeTimer,
            arenaShakeIntensity = shakeIntensity,
            playerLastDamageTaken = lastPlayerDmg,
            playerDamagePopTimerSec = dmgPopTimer,
            shieldBlockedTimerSec = shieldPopTimer
        )
    }

    /**
     * Each monster/boss blends signature bullet patterns with the 5 new hand-drawn monster attacks!
     */
    private fun spawnEnemyWave(
        enemyId: EnemyId,
        turn: Int,
        phaseTime: Float,
        soulX: Float,
        soulY: Float,
        outBullets: MutableList<Bullet>,
        outLasers: MutableList<LaserBeam>,
        outLanes: MutableList<LaneTelegraph>,
        outJaws: MutableList<MonsterJawAttack>,
        outArrows: MutableList<ArrowTelegraph>,
        outBombs: MutableList<ArenaBomb>,
        outSideWarnings: MutableList<SideWallShotWarning>
    ) {
        val pattern = (turn + (phaseTime * 0.55f).toInt()) % 4
        when (enemyId) {
            EnemyId.RUINS_DUMMY -> when (pattern) {
                0 -> {
                    // Attack #3 (Gentle Arrow Telegraph -> slow cotton lance)
                    outArrows += ArrowTelegraph(nextEntityId++, 0.25f, 0.14f, 0.55f, 0.82f, warmupSec = 0.85f, color = Color(0xFFD8B4FE))
                    outArrows += ArrowTelegraph(nextEntityId++, 0.75f, 0.14f, -0.55f, 0.82f, warmupSec = 0.85f, color = Color(0xFFD8B4FE))
                }
                1 -> {
                    // Attack #5 (Slow Side Shots)
                    outSideWarnings += SideWallShotWarning(nextEntityId++, Random.nextBoolean(), 0.30f + Random.nextFloat() * 0.45f, warmupSec = 0.82f, color = Color.White)
                }
                else -> {
                    outBullets += Bullet(nextEntityId++, 0.22f + Random.nextFloat() * 0.56f, -0.04f, 0f, 0.26f, color = Color(0xFFFDE047))
                }
            }

            EnemyId.VEX -> when (pattern) {
                0 -> {
                    // Attack #1 (Vertical Rectangular Up/Down Lane Telegraphs)
                    outLanes += LaneTelegraph(nextEntityId++, soulX.coerceIn(0.14f, 0.86f), isUpward = true, color = SoulAegisAmber)
                    outLanes += LaneTelegraph(nextEntityId++, (soulX + 0.26f).let { if (it > 0.86f) soulX - 0.26f else it }, isUpward = false, color = SoulAegisAmber)
                }
                1 -> {
                    // Attack #5 (Side Wall "! <-" Rapid Shots)
                    val fromRight = Random.nextBoolean()
                    for (slot in listOf(0.24f, 0.52f, 0.78f)) {
                        outSideWarnings += SideWallShotWarning(nextEntityId++, fromRight, slot, color = SoulAegisAmber)
                    }
                }
                2 -> {
                    // Attack #4 (Fuse Bomb tossed toward arena)
                    outBombs += ArenaBomb(nextEntityId++, 0.50f, 0.14f, (soulX - 0.50f) * 0.8f, (soulY - 0.14f) * 0.8f, color = SoulAegisAmber)
                }
                else -> {
                    outLasers += LaserBeam(nextEntityId++, true, soulY.coerceIn(0.18f, 0.82f), color = SoulAegisAmber)
                }
            }

            EnemyId.BRAMBLE -> when (pattern) {
                0 -> {
                    // Attack #2 (Boss-shaped Monster Chomp Head + Claw!)
                    outJaws += MonsterJawAttack(
                        id = nextEntityId++,
                        xNorm = soulX.coerceIn(0.20f, 0.80f),
                        yNorm = soulY.coerceIn(0.24f, 0.78f),
                        clawOffsetX = if (soulX > 0.5f) -0.17f else 0.17f,
                        color = Color(0xFF4ADE80)
                    )
                }
                1 -> {
                    // Attack #4 (Moss Fuse Bomb)
                    outBombs += ArenaBomb(nextEntityId++, Random.nextFloat() * 0.6f + 0.2f, 0.20f, (Random.nextFloat() - 0.5f) * 0.3f, 0.35f, color = Color(0xFF84CC16))
                }
                2 -> {
                    // Rolling Moss Boulders
                    val fromLeft = Random.nextBoolean()
                    outBullets += Bullet(nextEntityId++, if (fromLeft) -0.05f else 1.05f, 0.72f + Random.nextFloat() * 0.16f, if (fromLeft) 0.38f else -0.38f, -0.14f, radiusNorm = 0.052f, kind = BulletKind.BOUNCER, color = Color(0xFF84CC16), bouncesLeft = 2)
                }
                else -> {
                    outLanes += LaneTelegraph(nextEntityId++, 0.16f + Random.nextFloat() * 0.68f, isUpward = false, color = Color(0xFF22C55E))
                }
            }

            EnemyId.VESPERA -> when (pattern) {
                0 -> {
                    // Attack #3 (Crossed Diagonal Arrows replaced by silk shots!)
                    outArrows += ArrowTelegraph(nextEntityId++, 0.20f, 0.20f, 0.707f, 0.707f, color = Color(0xFFFACC15))
                    outArrows += ArrowTelegraph(nextEntityId++, 0.80f, 0.20f, -0.707f, 0.707f, color = SoulSeraphCyan)
                }
                1 -> {
                    // Attack #2 (Vespera Chomp + Claw Fan Strike)
                    outJaws += MonsterJawAttack(nextEntityId++, soulX.coerceIn(0.2f, 0.8f), soulY.coerceIn(0.25f, 0.75f), -0.16f, color = Color(0xFFFACC15))
                }
                2 -> {
                    val py = 0.5f + sin(phaseTime * 3.8f) * 0.34f
                    outBullets += Bullet(nextEntityId++, -0.04f, py, 0.46f, 0f, color = SoulSeraphCyan)
                    outBullets += Bullet(nextEntityId++, 1.04f, 1f - py, -0.46f, 0f, color = Color(0xFFFACC15))
                }
                else -> {
                    val baseAng = phaseTime * 1.8f
                    for (i in 0..5) {
                        val a = baseAng + i * (PI.toFloat() / 3f)
                        outBullets += Bullet(nextEntityId++, 0.5f + cos(a) * 0.48f, 0.5f + sin(a) * 0.48f, -cos(a) * 0.34f, -sin(a) * 0.34f, color = Color(0xFFFACC15))
                    }
                }
            }

            EnemyId.ORIN -> when (pattern) {
                0 -> {
                    // Attack #1 (Upward & Downward Pillar Lanes)
                    for (i in 0..1) {
                        outLanes += LaneTelegraph(nextEntityId++, 0.22f + i * 0.52f, isUpward = (i % 2 == 0), color = Color(0xFF3B82F6))
                    }
                }
                1 -> {
                    // Attack #5 (Side Cavern Shots)
                    outSideWarnings += SideWallShotWarning(nextEntityId++, false, 0.28f, color = Color(0xFF60A5FA))
                    outSideWarnings += SideWallShotWarning(nextEntityId++, true, 0.70f, color = Color(0xFF60A5FA))
                }
                2 -> {
                    for (i in 0..7) {
                        val a = i * (2f * PI.toFloat() / 8f) + phaseTime * 0.4f
                        outBullets += Bullet(nextEntityId++, 0.5f, 0.5f, cos(a) * 0.36f, sin(a) * 0.36f, color = Color(0xFF60A5FA))
                    }
                }
                else -> {
                    outLasers += LaserBeam(nextEntityId++, true, soulY.coerceIn(0.18f, 0.82f), warmupSec = 0.72f, color = Color(0xFF3B82F6))
                }
            }

            EnemyId.SERAPHIEL -> when (pattern) {
                0 -> {
                    // Attack #3 (Crossed Halo Arrows)
                    outArrows += ArrowTelegraph(nextEntityId++, 0.16f, 0.84f, 0.707f, -0.707f, color = SoulSeraphCyan)
                    outArrows += ArrowTelegraph(nextEntityId++, 0.84f, 0.84f, -0.707f, -0.707f, color = SepiaAmber)
                    outArrows += ArrowTelegraph(nextEntityId++, 0.50f, 0.12f, 0f, 1f, color = Color(0xFF22C55E))
                }
                1 -> {
                    // Attack #1 (Ascending/Descending Angelic Light Lanes)
                    outLanes += LaneTelegraph(nextEntityId++, soulX.coerceIn(0.16f, 0.84f), isUpward = true, color = SepiaAmber)
                }
                2 -> {
                    val originX = 0.25f + Random.nextFloat() * 0.5f
                    for (i in 0 until 6) {
                        val angle = (PI * 0.18 + i * (PI * 0.64 / 5)).toFloat()
                        outBullets += Bullet(nextEntityId++, originX, -0.04f, cos(angle) * 0.40f, sin(angle) * 0.42f, kind = BulletKind.LANCE, color = SoulSeraphCyan)
                    }
                }
                else -> {
                    outJaws += MonsterJawAttack(nextEntityId++, soulX.coerceIn(0.2f, 0.8f), soulY.coerceIn(0.25f, 0.75f), 0.16f, color = SepiaAmber)
                }
            }

            EnemyId.MALAKOR -> when (pattern) {
                0 -> {
                    // Attack #4 (Hellfire Fuse Bomb)
                    outBombs += ArenaBomb(nextEntityId++, 0.5f, 0.16f, (soulX - 0.5f) * 0.9f, (soulY - 0.16f) * 0.9f, fuseSec = 0.95f, color = SoulParadoxCrimson)
                }
                1 -> {
                    // Attack #2 (Devil Chomp Head + Claw)
                    outJaws += MonsterJawAttack(nextEntityId++, soulX.coerceIn(0.22f, 0.78f), soulY.coerceIn(0.24f, 0.78f), -0.17f, color = SoulInfernalViolet)
                }
                2 -> {
                    // Attack #5 (Infernal Side Shots)
                    val side = Random.nextBoolean()
                    outSideWarnings += SideWallShotWarning(nextEntityId++, side, soulY.coerceIn(0.18f, 0.82f), color = SoulParadoxCrimson)
                    outSideWarnings += SideWallShotWarning(nextEntityId++, side, (soulY + 0.24f).coerceAtMost(0.88f), color = SoulInfernalViolet)
                }
                else -> {
                    val angle = Random.nextFloat() * 2f * PI.toFloat()
                    outBullets += Bullet(nextEntityId++, if (Random.nextBoolean()) 0.08f else 0.92f, 0.12f, cos(angle) * 0.46f, abs(sin(angle)) * 0.46f + 0.12f, kind = BulletKind.BOUNCER, color = SoulInfernalViolet, bouncesLeft = 2)
                }
            }

            EnemyId.NORMAL_SLIME -> when (pattern) {
                0, 1 -> {
                    outBullets += Bullet(nextEntityId++, 0.16f + Random.nextFloat() * 0.68f, -0.04f, (Random.nextFloat() - 0.5f) * 0.25f, 0.34f, kind = BulletKind.BOUNCER, color = Color(0xFF4ADE80), bouncesLeft = 1)
                }
                else -> {
                    outArrows += ArrowTelegraph(nextEntityId++, 0.20f, 0.22f, 0.65f, 0.65f, color = Color(0xFF86EFAC))
                }
            }

            EnemyId.FERAL_HOUND -> when (pattern) {
                0 -> {
                    outJaws += MonsterJawAttack(nextEntityId++, soulX.coerceIn(0.22f, 0.78f), soulY.coerceIn(0.24f, 0.76f), -0.15f, color = Color(0xFFEF4444))
                }
                1 -> {
                    outSideWarnings += SideWallShotWarning(nextEntityId++, Random.nextBoolean(), soulY.coerceIn(0.20f, 0.80f), color = Color(0xFFEF4444))
                }
                else -> {
                    outLanes += LaneTelegraph(nextEntityId++, soulX.coerceIn(0.16f, 0.84f), isUpward = false, color = Color(0xFFEF4444))
                }
            }

            EnemyId.SAND_MOTH -> when (pattern) {
                0, 1 -> {
                    val py = 0.25f + Random.nextFloat() * 0.50f
                    outBullets += Bullet(nextEntityId++, -0.04f, py, 0.40f, 0.08f, color = SoulSeraphCyan)
                    outBullets += Bullet(nextEntityId++, 1.04f, 1f - py, -0.40f, -0.08f, color = Color(0xFFFACC15))
                }
                else -> {
                    outArrows += ArrowTelegraph(nextEntityId++, 0.78f, 0.18f, -0.7f, 0.7f, color = SoulSeraphCyan)
                }
            }

            EnemyId.IRON_STALKER -> when (pattern) {
                0 -> {
                    outLasers += LaserBeam(nextEntityId++, Random.nextBoolean(), soulY.coerceIn(0.20f, 0.80f), color = Color(0xFFF97316))
                }
                1 -> {
                    outBombs += ArenaBomb(nextEntityId++, 0.5f, 0.16f, (soulX - 0.5f) * 0.75f, (soulY - 0.16f) * 0.75f, color = Color(0xFFF97316))
                }
                else -> {
                    outSideWarnings += SideWallShotWarning(nextEntityId++, false, 0.34f, color = Color(0xFFF97316))
                    outSideWarnings += SideWallShotWarning(nextEntityId++, true, 0.66f, color = Color(0xFFF97316))
                }
            }

            EnemyId.HALO_SCRIBE -> when (pattern) {
                0, 1 -> {
                    val baseAng = phaseTime * 1.5f
                    for (i in 0..3) {
                        val a = baseAng + i * (PI.toFloat() / 2f)
                        outBullets += Bullet(nextEntityId++, 0.5f + cos(a) * 0.44f, 0.5f + sin(a) * 0.44f, -cos(a) * 0.30f, -sin(a) * 0.30f, color = Color(0xFFF472B6))
                    }
                }
                else -> {
                    outArrows += ArrowTelegraph(nextEntityId++, 0.22f, 0.80f, 0.65f, -0.65f, color = Color(0xFFFACC15))
                }
            }

            EnemyId.ASH_IMP -> when (pattern) {
                0 -> {
                    outBombs += ArenaBomb(nextEntityId++, Random.nextFloat() * 0.5f + 0.25f, 0.18f, (soulX - 0.5f) * 0.7f, 0.35f, fuseSec = 0.95f, color = SoulParadoxCrimson)
                }
                1 -> {
                    outJaws += MonsterJawAttack(nextEntityId++, soulX.coerceIn(0.22f, 0.78f), soulY.coerceIn(0.22f, 0.78f), 0.15f, color = SoulParadoxCrimson)
                }
                else -> {
                    val ang = Random.nextFloat() * PI.toFloat()
                    outBullets += Bullet(nextEntityId++, 0.5f, 0.10f, cos(ang) * 0.44f, sin(ang) * 0.44f + 0.14f, kind = BulletKind.BOUNCER, color = Color(0xFFF59E0B), bouncesLeft = 2)
                }
            }

            EnemyId.HUMAN_DRIFTER -> when (pattern) {
                0 -> {
                    val fromRight = Random.nextBoolean()
                    outSideWarnings += SideWallShotWarning(nextEntityId++, fromRight, soulY.coerceIn(0.22f, 0.78f), color = SepiaGold)
                }
                1 -> {
                    outArrows += ArrowTelegraph(nextEntityId++, 0.20f, 0.20f, 0.68f, 0.68f, color = SepiaGold)
                }
                else -> {
                    outBullets += Bullet(nextEntityId++, 0.2f + Random.nextFloat() * 0.6f, -0.04f, (soulX - 0.5f) * 0.25f, 0.36f, kind = BulletKind.ORB, color = Color(0xFFFDE047))
                }
            }

            EnemyId.ECHO_LOKA -> when (pattern) {
                0 -> {
                    // Attack #2 (Corrupted Echo Chomp Head)
                    outJaws += MonsterJawAttack(nextEntityId++, soulX.coerceIn(0.2f, 0.8f), soulY.coerceIn(0.22f, 0.78f), 0.16f, color = GlitchCyan)
                }
                1 -> {
                    // Attack #3 (Crossed Glitch Arrows)
                    outArrows += ArrowTelegraph(nextEntityId++, 0.18f, 0.18f, 0.707f, 0.707f, color = GlitchCyan)
                    outArrows += ArrowTelegraph(nextEntityId++, 0.82f, 0.18f, -0.707f, 0.707f, color = Color.White)
                }
                2 -> {
                    // Attack #5 (Static Side Shots)
                    outSideWarnings += SideWallShotWarning(nextEntityId++, true, 0.30f, color = GlitchCyan)
                    outSideWarnings += SideWallShotWarning(nextEntityId++, false, 0.68f, color = Color.White)
                }
                else -> {
                    outLasers += LaserBeam(nextEntityId++, Random.nextBoolean(), 0.2f + Random.nextFloat() * 0.6f, warmupSec = 0.7f, color = GlitchCyan)
                }
            }

            EnemyId.GOV_MONSTER_CHIEF -> when (pattern) {
                0 -> {
                    outJaws += MonsterJawAttack(nextEntityId++, soulX.coerceIn(0.22f, 0.78f), soulY.coerceIn(0.24f, 0.76f), -0.16f, color = Color(0xFF4ADE80))
                }
                1 -> {
                    outArrows += ArrowTelegraph(nextEntityId++, 0.18f, 0.18f, 0.707f, 0.707f, color = Color(0xFF4ADE80))
                    outArrows += ArrowTelegraph(nextEntityId++, 0.82f, 0.18f, -0.707f, 0.707f, color = MercyYellow)
                }
                2 -> {
                    outBombs += ArenaBomb(nextEntityId++, 0.5f, 0.18f, (soulX - 0.5f) * 0.8f, (soulY - 0.18f) * 0.8f, color = Color(0xFF4ADE80))
                }
                else -> {
                    outSideWarnings += SideWallShotWarning(nextEntityId++, Random.nextBoolean(), soulY.coerceIn(0.20f, 0.80f), color = Color(0xFF4ADE80))
                }
            }

            EnemyId.GOV_DIRECTOR -> when (pattern) {
                0 -> {
                    outLanes += LaneTelegraph(nextEntityId++, soulX.coerceIn(0.16f, 0.84f), isUpward = true, color = Color(0xFFEF4444))
                    outLanes += LaneTelegraph(nextEntityId++, (1f - soulX).coerceIn(0.16f, 0.84f), isUpward = false, color = MercyYellow)
                }
                1 -> {
                    val fromRight = Random.nextBoolean()
                    for (slot in listOf(0.25f, 0.50f, 0.75f)) {
                        outSideWarnings += SideWallShotWarning(nextEntityId++, fromRight, slot, warmupSec = 0.60f, color = Color(0xFFEF4444))
                    }
                }
                2 -> {
                    outLasers += LaserBeam(nextEntityId++, Random.nextBoolean(), if (Random.nextBoolean()) soulY.coerceIn(0.2f, 0.8f) else soulX.coerceIn(0.2f, 0.8f), warmupSec = 0.68f, color = Color(0xFFEF4444))
                }
                else -> {
                    val baseAng = phaseTime * 1.9f
                    for (i in 0 until 6) {
                        val a = baseAng + i * (2f * PI.toFloat() / 6f)
                        outBullets += Bullet(nextEntityId++, 0.5f + cos(a) * 0.45f, 0.5f + sin(a) * 0.45f, -cos(a) * 0.36f, -sin(a) * 0.36f, kind = BulletKind.LANCE, color = Color(0xFFEF4444))
                    }
                }
            }

            EnemyId.FUTURE_LOKA -> when (pattern) {
                0 -> {
                    // Attack #1 + #4 (Chrono Lanes + Timeline Bomb!)
                    outLanes += LaneTelegraph(nextEntityId++, soulX.coerceIn(0.16f, 0.84f), isUpward = true, color = SepiaAmber)
                    outBombs += ArenaBomb(nextEntityId++, 0.5f, 0.18f, (soulX - 0.5f) * 0.85f, (soulY - 0.18f) * 0.85f, fuseSec = 1.0f, color = SoulSeraphCyan)
                }
                1 -> {
                    // Attack #2 + #3 (Future-Loka Rift Jaw + Crossed Arrows)
                    outJaws += MonsterJawAttack(nextEntityId++, soulX.coerceIn(0.22f, 0.78f), soulY.coerceIn(0.24f, 0.76f), -0.17f, color = SepiaAmber)
                    outArrows += ArrowTelegraph(nextEntityId++, 0.15f, 0.85f, 0.707f, -0.707f, color = SoulParadoxCrimson)
                }
                2 -> {
                    // Attack #5 (Triple Side Barrage)
                    val fromRight = Random.nextBoolean()
                    for (y in listOf(0.22f, 0.50f, 0.78f)) {
                        outSideWarnings += SideWallShotWarning(nextEntityId++, fromRight, y, warmupSec = 0.58f, color = SoulSeraphCyan)
                    }
                }
                else -> {
                    val baseAng = phaseTime * 2.0f
                    for (i in 0 until 6) {
                        val a = baseAng + i * (2f * PI.toFloat() / 6f)
                        outBullets += Bullet(nextEntityId++, 0.5f + cos(a) * 0.46f, 0.5f + sin(a) * 0.46f, -cos(a) * 0.38f, -sin(a) * 0.38f, kind = BulletKind.TIME_SHARD, color = SepiaAmber)
                    }
                }
            }
        }
    }
}
