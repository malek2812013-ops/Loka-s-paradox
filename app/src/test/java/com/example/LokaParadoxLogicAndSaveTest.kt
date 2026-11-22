package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameSaveStorage
import com.example.data.SavedGameSnapshot
import com.example.engine.BattleEngine
import com.example.engine.LokaStatsCalculator
import com.example.engine.TimelineManager
import com.example.model.AppLanguage
import com.example.model.ArmorCatalog
import com.example.model.BattlePhase
import com.example.model.EnemyId
import com.example.model.EnemyRoster
import com.example.model.GameDifficulty
import com.example.model.RelicCatalog
import com.example.model.SoulType
import com.example.model.WeaponCatalog
import com.example.model.WorldId
import com.example.ui.components.PixelSprites
import com.example.ui.components.UltraPixelArtEngine
import com.example.viewmodel.BattleViewModel
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LokaParadoxLogicAndSaveTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        val storage = GameSaveStorage(app)
        for (slot in 0 until GameSaveStorage.SLOT_COUNT) {
            storage.clear(slot)
        }
        storage.setActiveSlot(0)
        storage.saveGlobalDifficulty(GameDifficulty.NORMAL)
        storage.saveGlobalVolume(1.0f)
        storage.saveReduceFlash(false)
    }

    @Test
    fun battleViewModel_entersFightAndDamagesEnemyVsMercySpare() {
        val battleVm = BattleViewModel()
        val armor = ArmorCatalog.byId("casual_jacket")
        val weapon = WeaponCatalog.byId("worn_stick")
        val relic = RelicCatalog.byId("golden_locket")

        // 1. Enter FIGHT minigame and confirm enemy HP decreases
        battleVm.initEncounter(
            encounterId = 1L,
            enemyId = EnemyId.FERAL_HOUND,
            worldId = WorldId.NORMAL_WORLD,
            armor = armor,
            weapon = weapon,
            relic = relic,
            difficulty = GameDifficulty.NORMAL,
            allies = emptySet(),
            helpedKidIds = emptySet(),
            itemCounts = emptyMap()
        )
        val initialHp = battleVm.state.value.enemyHp
        battleVm.startFightMinigame()
        assertEquals(BattlePhase.FIGHT_MINIGAME, battleVm.state.value.phase)

        battleVm.commitFightStrike()
        assertTrue(battleVm.state.value.enemyHp < initialHp)
        assertTrue((battleVm.state.value.lastStrikeDamage ?: 0) > 0)

        // 2. Enter ordered ACT -> Mercy / Spare flow and verify victory via Spare
        battleVm.initEncounter(
            encounterId = 2L,
            enemyId = EnemyId.FERAL_HOUND,
            worldId = WorldId.NORMAL_WORLD,
            armor = armor,
            weapon = weapon,
            relic = relic,
            difficulty = GameDifficulty.NORMAL,
            allies = emptySet(),
            helpedKidIds = emptySet(),
            itemCounts = emptyMap()
        )
        battleVm.performAct("glare")
        battleVm.performAct("feed")
        battleVm.performAct("thorn")
        assertEquals(100, battleVm.state.value.mercyProgress)
        assertTrue(battleVm.state.value.canSpare)

        battleVm.performSpare()
        assertEquals(BattlePhase.VICTORY, battleVm.state.value.phase)
        assertTrue(battleVm.state.value.battleWonSpared)
    }

    @Test
    fun battleEngine_calculatesAccurateStrikeAndIncomingDamage() {
        val weapon = WeaponCatalog.byId("iron_pipe")
        val relic = RelicCatalog.byId("chrono_pendulum")

        val centerStrike = BattleEngine.calculateStrikeDamage(
            cursorPos = 0.50f,
            attackBonus = 5,
            weapon = weapon,
            relic = relic,
            activeAllies = setOf(EnemyId.VEX),
            activeSoul = SoulType.RED,
            difficulty = GameDifficulty.NORMAL
        )
        val edgeStrike = BattleEngine.calculateStrikeDamage(
            cursorPos = 0.92f,
            attackBonus = 5,
            weapon = weapon,
            relic = relic,
            activeAllies = setOf(EnemyId.VEX),
            activeSoul = SoulType.RED,
            difficulty = GameDifficulty.NORMAL
        )
        assertTrue(centerStrike.isCritical)
        assertFalse(edgeStrike.isCritical)
        assertTrue(centerStrike.damage > edgeStrike.damage)

        // Sick White Soul caps outgoing damage at 1 and doubles incoming damage
        val sickStrike = BattleEngine.calculateStrikeDamage(
            cursorPos = 0.50f,
            attackBonus = 20,
            weapon = weapon,
            relic = relic,
            activeAllies = emptySet(),
            activeSoul = SoulType.WHITE_SICK,
            difficulty = GameDifficulty.NORMAL
        )
        assertEquals(1, sickStrike.damage)

        val normalIncoming = BattleEngine.calculateIncomingDamage(
            enemyAttackPower = 5,
            totalDefense = 2,
            activeSoul = SoulType.RED,
            difficulty = GameDifficulty.NORMAL
        )
        val hardIncoming = BattleEngine.calculateIncomingDamage(
            enemyAttackPower = 5,
            totalDefense = 2,
            activeSoul = SoulType.RED,
            difficulty = GameDifficulty.HARD
        )
        val sickIncoming = BattleEngine.calculateIncomingDamage(
            enemyAttackPower = 5,
            totalDefense = 2,
            activeSoul = SoulType.WHITE_SICK,
            difficulty = GameDifficulty.NORMAL
        )
        assertEquals(3, normalIncoming)
        assertTrue(hardIncoming >= normalIncoming)
        assertEquals(normalIncoming * 2, sickIncoming)
    }

    @Test
    fun battleEngine_enforcesBossStrict10PercentAndOrderedSteps() {
        val boss = EnemyRoster.byId(EnemyId.VESPERA)
        val baseRelic = RelicCatalog.byId("golden_locket")

        // Out of order ACT must yield 0% mercy and flag outOfOrder
        val wrongOrder = BattleEngine.evaluateActStep(
            enemy = boss,
            optionId = "bow",
            currentMercyProgress = 0,
            completedActIds = emptySet(),
            relic = baseRelic
        )
        assertTrue(wrongOrder.outOfOrder)
        assertEquals(0, wrongOrder.mercyGained)

        // Correct order on boss strictly gives 10% per ACT even with a Mercy-boosting relic
        val mercyRelic = RelicCatalog.byId("seraph_feather")
        val step1 = BattleEngine.evaluateActStep(
            enemy = boss,
            optionId = "check",
            currentMercyProgress = 0,
            completedActIds = emptySet(),
            relic = mercyRelic
        )
        assertFalse(step1.outOfOrder)
        assertEquals(10, step1.mercyGained)
        assertEquals(10, step1.newMercyProgress)

        val step2 = BattleEngine.evaluateActStep(
            enemy = boss,
            optionId = "bow",
            currentMercyProgress = step1.newMercyProgress,
            completedActIds = step1.completedActIds,
            relic = mercyRelic
        )
        assertEquals(10, step2.mercyGained)
        assertEquals(20, step2.newMercyProgress)

        // Normal monster uses varied percentages + Mercy Bell bonus and strict order where defined
        val hound = EnemyRoster.byId(EnemyId.FERAL_HOUND)
        val houndBad = BattleEngine.evaluateActStep(
            enemy = hound,
            optionId = "feed",
            currentMercyProgress = 0,
            completedActIds = emptySet(),
            relic = mercyRelic
        )
        assertTrue(houndBad.outOfOrder)
        assertEquals(0, houndBad.mercyGained)

        val houndStep1 = BattleEngine.evaluateActStep(
            enemy = hound,
            optionId = "glare",
            currentMercyProgress = 0,
            completedActIds = emptySet(),
            relic = mercyRelic
        )
        assertEquals(15 + mercyRelic.normalMonsterMercyBonus, houndStep1.mercyGained)
    }

    @Test
    fun gameViewModel_updatesRelationshipsAndGovernmentStopOutcomes() {
        val vm = GameViewModel(app)
        vm.recordBattleOutcome(EnemyId.VESPERA, spared = true, remainingItems = emptyMap())
        vm.recordBattleOutcome(EnemyId.GOV_DIRECTOR, spared = true, remainingItems = emptyMap())
        vm.recordBattleOutcome(EnemyId.ASH_IMP, spared = false, remainingItems = emptyMap())

        val state = vm.uiState.value
        assertTrue(EnemyId.VESPERA in state.friendAllies)
        assertFalse(EnemyId.GOV_DIRECTOR in state.friendAllies)
        assertTrue(EnemyId.GOV_DIRECTOR in state.stoppedEnemies)
        assertTrue(EnemyId.ASH_IMP in state.defeatedEnemies)
        assertTrue(state.choiceHistory.any { it.contains("Befriended") })
        assertTrue(state.choiceHistory.any { it.contains("Stopped") })
        assertTrue(state.choiceHistory.any { it.contains("Defeated") })
    }

    @Test
    fun saveSlots_andNewTimelineResetPreservePastEchoes() {
        val storage = GameSaveStorage(app)
        storage.save(
            SavedGameSnapshot(
                hasSavedProgress = true,
                friendAllies = setOf(EnemyId.VESPERA),
                saveCounter = 3
            ),
            slotIndex = 0
        )
        storage.save(
            SavedGameSnapshot(
                hasSavedProgress = true,
                defeatedEnemies = setOf(EnemyId.BRAMBLE),
                saveCounter = 1
            ),
            slotIndex = 1
        )

        val slot0 = storage.load(0)
        val slot1 = storage.load(1)
        assertTrue(EnemyId.VESPERA in slot0.friendAllies)
        assertTrue(slot1.friendAllies.isEmpty())
        assertTrue(EnemyId.BRAMBLE in slot1.defeatedEnemies)

        // Start ViewModel on Slot 0, kill one enemy, then open a new Timeline preserving echoes
        val vm = GameViewModel(app)
        vm.recordBattleOutcome(EnemyId.FERAL_HOUND, spared = false, remainingItems = emptyMap())
        vm.startNewTimelinePreservingEchoes()

        val afterTimelineReset = vm.uiState.value
        assertEquals(1, afterTimelineReset.timelineResetCount)
        assertEquals(1, afterTimelineReset.pastTimelines.size)
        assertTrue(afterTimelineReset.defeatedEnemies.isEmpty())
        assertTrue(afterTimelineReset.friendAllies.isEmpty())
        assertTrue(afterTimelineReset.timelineMemory.echoModifierActive)
        assertTrue(afterTimelineReset.isParadoxTrialUnlocked)

        // Verify computed stats react to relics & allies
        val boostedStats = LokaStatsCalculator.compute(
            armor = ArmorCatalog.byId("casual_jacket"),
            weapon = WeaponCatalog.byId("worn_stick"),
            relic = RelicCatalog.byId("echo_prism"),
            allies = setOf(EnemyId.BRAMBLE),
            helpedKidIds = emptySet(),
            difficulty = GameDifficulty.EASY,
            worldId = WorldId.NORMAL_WORLD
        )
        assertTrue(boostedStats.maxHp > 20)
        assertTrue(boostedStats.defense >= 3)

        // Verify TimelineManager message notes past kills when now choosing mercy
        val reactive = TimelineManager.evaluateReactiveMemory(
            timelineResetCount = afterTimelineReset.timelineResetCount,
            pastTimelines = afterTimelineReset.pastTimelines,
            currentKills = 0,
            currentFriends = 1,
            lang = AppLanguage.EN
        )
        assertTrue(reactive.echoModifierActive)
    }

    @Test
    fun pixelEngine_snapsIntegerExtentsAndSculptsBossesTo64() {
        val scaled = UltraPixelArtEngine.computeIntegerScaledSize(gridW = 64, gridH = 64, targetSizePx = 290f)
        // Floor integer factor: 290 / 64 = 4 -> 256x256 (exact multiple of 64)
        assertEquals(256, scaled.width)
        assertEquals(256, scaled.height)
        assertEquals(0, scaled.width % 64)

        val nonSquareScaled = UltraPixelArtEngine.computeIntegerScaledSize(gridW = 13, gridH = 12, targetSizePx = 58f)
        // Floor factor: 58 / 13 = 4 -> 52x48
        assertEquals(52, nonSquareScaled.width)
        assertEquals(48, nonSquareScaled.height)

        val sculptedVex = PixelSprites.sculptGridTo64(PixelSprites.vexBossGrid)
        assertEquals(64, sculptedVex.size)
        assertEquals(64, sculptedVex.first().length)
    }

    @Test
    fun battleViewModel_triggersAndDecaysStrikeShakeFeedback() {
        val battleVm = BattleViewModel()
        battleVm.initEncounter(
            encounterId = 99L,
            enemyId = EnemyId.VEX,
            worldId = WorldId.NORMAL_WORLD,
            armor = ArmorCatalog.byId("casual_jacket"),
            weapon = WeaponCatalog.byId("worn_stick"),
            relic = RelicCatalog.byId("golden_locket"),
            difficulty = GameDifficulty.NORMAL,
            allies = emptySet(),
            helpedKidIds = emptySet(),
            itemCounts = emptyMap()
        )
        battleVm.startFightMinigame()
        battleVm.commitFightStrike()

        val afterStrike = battleVm.state.value
        assertTrue(afterStrike.arenaShakeTimerSec > 0f)
        assertTrue(afterStrike.arenaShakeIntensity > 0f)

        // Advance clock by 0.50s in small steps so shake & slash timers cleanly decay to zero
        repeat(15) { battleVm.tick(0.04f) }
        val settled = battleVm.state.value
        assertEquals(0f, settled.arenaShakeTimerSec, 0.0001f)
        assertEquals(0f, settled.arenaShakeIntensity, 0.0001f)
    }

    @Test
    fun battleViewModel_sets45SecDodgeForMonstersAnd60SecForBosses() {
        val battleVm = BattleViewModel()
        val armor = ArmorCatalog.byId("casual_jacket")
        val weapon = WeaponCatalog.byId("worn_stick")
        val relic = RelicCatalog.byId("golden_locket")

        // Normal monster / dummy -> ~45 seconds dodge phase
        battleVm.initEncounter(
            encounterId = 101L,
            enemyId = EnemyId.RUINS_DUMMY,
            worldId = WorldId.NORMAL_WORLD,
            armor = armor,
            weapon = weapon,
            relic = relic,
            difficulty = GameDifficulty.NORMAL,
            allies = emptySet(),
            helpedKidIds = emptySet(),
            itemCounts = emptyMap()
        )
        battleVm.performAct("check")
        battleVm.advanceFromDialogueToDodging()
        assertEquals(BattlePhase.BULLET_HELL, battleVm.state.value.phase)
        assertEquals(45.0f, battleVm.state.value.phaseDurationSec, 0.01f)

        // Boss encounter -> 60 seconds dodge phase
        battleVm.initEncounter(
            encounterId = 102L,
            enemyId = EnemyId.SERAPHIEL,
            worldId = WorldId.SPLIT_REALM,
            armor = armor,
            weapon = weapon,
            relic = relic,
            difficulty = GameDifficulty.NORMAL,
            allies = emptySet(),
            helpedKidIds = emptySet(),
            itemCounts = emptyMap()
        )
        battleVm.performAct("check")
        battleVm.advanceFromDialogueToDodging()
        assertEquals(BattlePhase.BULLET_HELL, battleVm.state.value.phase)
        assertEquals(60.0f, battleVm.state.value.phaseDurationSec, 0.01f)
    }
}
