package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.BgmTrack
import com.example.audio.RetroSoundEngine
import com.example.model.AppScreen
import com.example.model.EnemyId
import com.example.model.WorldId
import com.example.ui.screens.AshenMeetingScreen
import com.example.ui.screens.BattleScreen
import com.example.ui.screens.LoreIntroScreen
import com.example.ui.screens.RouteFinaleScreen
import com.example.ui.screens.RuinsTutorialScreen
import com.example.ui.screens.WorldExploreScreen
import com.example.ui.theme.LokaParadoxTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LokaParadoxTheme {
                val gameViewModel: GameViewModel = viewModel()
                val state by gameViewModel.uiState.collectAsState()

                LaunchedEffect(state.currentScreen, state.activeWorld, state.selectedEnemyId) {
                    val track = when (state.currentScreen) {
                        AppScreen.LORE_INTRO -> BgmTrack.INTRO_STORY
                        AppScreen.RUINS_TUTORIAL -> BgmTrack.RUINS
                        AppScreen.ASHEN_MEETING -> BgmTrack.ASHEN
                        AppScreen.WORLD_EXPLORE -> when (state.activeWorld) {
                            WorldId.NORMAL_WORLD -> BgmTrack.WORLD_NORMAL
                            WorldId.TRI_RACE_REALM -> BgmTrack.WORLD_OUTLANDS
                            WorldId.SPLIT_REALM -> BgmTrack.WORLD_SPLIT
                            WorldId.GLITCHED_WORLD -> BgmTrack.GLITCHED_VOID
                        }
                        AppScreen.BATTLE -> when (state.selectedEnemyId) {
                            EnemyId.RUINS_DUMMY -> BgmTrack.BATTLE_DUMMY
                            EnemyId.VEX, EnemyId.BRAMBLE, EnemyId.NORMAL_SLIME, EnemyId.FERAL_HOUND -> BgmTrack.BATTLE
                            EnemyId.VESPERA, EnemyId.ORIN, EnemyId.SAND_MOTH, EnemyId.IRON_STALKER -> BgmTrack.BATTLE_OUTLANDS
                            EnemyId.SERAPHIEL, EnemyId.MALAKOR, EnemyId.HALO_SCRIBE, EnemyId.ASH_IMP -> BgmTrack.BATTLE_SPLIT
                            EnemyId.ECHO_LOKA -> BgmTrack.BATTLE_GLITCH
                            EnemyId.FUTURE_LOKA -> BgmTrack.BATTLE_FUTURE
                        }
                        AppScreen.ROUTE_FINALE -> BgmTrack.FINALE
                    }
                    RetroSoundEngine.setMusicTrack(track)
                }

                when (state.currentScreen) {
                    AppScreen.LORE_INTRO -> LoreIntroScreen(
                        language = state.language,
                        slides = state.loreSlides,
                        sepiaIntensity = state.sepiaIntensity,
                        glitchIntensity = state.glitchIntensity,
                        isMuted = state.isMuted,
                        onToggleLanguage = gameViewModel::toggleLanguage,
                        onToggleMute = gameViewModel::toggleMute,
                        onFinishIntro = gameViewModel::finishLoreIntro
                    )

                    AppScreen.RUINS_TUTORIAL -> RuinsTutorialScreen(
                        state = state,
                        onMovePlayer = gameViewModel::moveRuinsPlayer,
                        onInspectLore = gameViewModel::inspectRuinsLore,
                        onClaimCandy = gameViewModel::claimRuinsCandy,
                        onActivateSwitch = gameViewModel::activateRuinsSwitch,
                        onPracticeDummy = gameViewModel::startRuinsDummyBattle,
                        onToggleControls = gameViewModel::toggleControlScheme,
                        onToggleLanguage = gameViewModel::toggleLanguage,
                        onToggleMute = gameViewModel::toggleMute,
                        onExitRuins = gameViewModel::exitRuinsToAshen
                    )

                    AppScreen.ASHEN_MEETING -> AshenMeetingScreen(
                        language = state.language,
                        onToggleLanguage = gameViewModel::toggleLanguage,
                        onSaveWithAshen = { gameViewModel.saveProgressExplicitly(fromAshen = true) },
                        onContinueToRandomWorld = gameViewModel::finishAshenMeetingToRandomWorld
                    )

                    AppScreen.WORLD_EXPLORE -> WorldExploreScreen(
                        state = state,
                        onMovePlayer = gameViewModel::moveOverworldPlayer,
                        onTickOverworld = gameViewModel::tickOverworld,
                        onHelpKid = gameViewModel::helpLostKid,
                        onKillKid = gameViewModel::killLostKid,
                        onCompleteLandmark = gameViewModel::completeLandmarkQuest,
                        onClaimNpcEvent = gameViewModel::claimNpcEvent,
                        onOpenChest = gameViewModel::openWorldChest,
                        onEngageBoss = { enemyId -> gameViewModel.startBattle(enemyId) },
                        onEquipArmor = gameViewModel::equipArmor,
                        onEquipWeapon = gameViewModel::equipWeapon,
                        onSaveGame = { gameViewModel.saveProgressExplicitly(fromAshen = true) },
                        onManualRiftJump = gameViewModel::triggerManualRiftJump,
                        onResetSave = gameViewModel::resetAllProgress,
                        onOpenRouteFinale = gameViewModel::openRouteFinale,
                        onWatchStoryIntro = gameViewModel::openLoreIntro,
                        onToggleControls = gameViewModel::toggleControlScheme,
                        onToggleLanguage = gameViewModel::toggleLanguage,
                        onToggleMute = gameViewModel::toggleMute
                    )

                    AppScreen.BATTLE -> BattleScreen(
                        language = state.language,
                        battleSessionId = state.battleSessionId,
                        enemyId = state.selectedEnemyId,
                        worldId = state.activeWorld,
                        equippedArmor = state.equippedArmor,
                        equippedWeapon = state.equippedWeapon,
                        friendAllies = state.friendAllies,
                        helpedKidIds = state.escortingKidIds,
                        itemCounts = state.itemCounts,
                        controlScheme = state.controlScheme,
                        onToggleControls = gameViewModel::toggleControlScheme,
                        onToggleLanguage = gameViewModel::toggleLanguage,
                        onBattleFinished = gameViewModel::recordBattleOutcome,
                        onExitBattle = gameViewModel::returnAfterBattle
                    )

                    AppScreen.ROUTE_FINALE -> RouteFinaleScreen(
                        state = state,
                        onToggleLanguage = gameViewModel::toggleLanguage,
                        onFightFutureLoka = gameViewModel::startFutureLokaNoMercyFight,
                        onChoosePeaceSacrifice = gameViewModel::choosePeaceSacrifice,
                        onAcceptPerfectPower = gameViewModel::acceptPerfectFuturePower,
                        onRestartWorldFromTapes = gameViewModel::masterTeleportAndRestart,
                        onReturnToWorlds = gameViewModel::returnToExploreFromFinale
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        RetroSoundEngine.setMusicTrack(BgmTrack.NONE)
        super.onDestroy()
    }
}
