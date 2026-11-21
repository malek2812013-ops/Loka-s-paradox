package com.example.data

import android.content.Context
import com.example.model.AppLanguage
import com.example.model.AppScreen
import com.example.model.ControlScheme
import com.example.model.EnemyId
import com.example.model.GameDifficulty
import com.example.model.RouteType
import com.example.model.TimelineEcho
import com.example.model.WorldId
import com.example.model.WorldZone
import org.json.JSONArray
import org.json.JSONObject

data class SaveSlotSummary(
    val slotIndex: Int,
    val isEmpty: Boolean,
    val activeWorld: WorldId = WorldId.NORMAL_WORLD,
    val saveCounter: Int = 0,
    val friendsCount: Int = 0,
    val stoppedCount: Int = 0,
    val slainCount: Int = 0,
    val timelineResetCount: Int = 0,
    val difficulty: GameDifficulty = GameDifficulty.NORMAL,
    val savedAtMs: Long = 0L
)

data class SavedGameSnapshot(
    val hasSavedProgress: Boolean = false,
    val language: AppLanguage = AppLanguage.EN,
    val controlScheme: ControlScheme = ControlScheme.TOUCH_DRAG,
    val isMuted: Boolean = false,
    val masterVolume: Float = 1.0f,
    val difficulty: GameDifficulty = GameDifficulty.NORMAL,
    val reduceFlash: Boolean = false,
    val resumeScreen: AppScreen = AppScreen.LORE_INTRO,
    val ruinsSwitchActivated: Boolean = false,
    val ruinsDummyPracticed: Boolean = false,
    val activeWorld: WorldId = WorldId.NORMAL_WORLD,
    val activeZone: WorldZone = WorldZone.CENTER,
    val playerMapX: Float = 0.50f,
    val playerMapY: Float = 0.50f,
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
    val unlockedRelicIds: Set<String> = setOf("golden_locket"),
    val equippedRelicId: String = "golden_locket",
    val itemCounts: Map<String, Int> = mapOf(
        "chrono_shard" to 2,
        "sepia_ration" to 1,
        "save_anchor" to 1
    ),
    val unlockedRoutes: Set<RouteType> = emptySet(),
    val activeFinaleRoute: RouteType = RouteType.MOST_EXPECTED,
    val isFutureTransformed: Boolean = false,
    val teleportControlMastered: Boolean = false,
    val worldDestroyedEnding: Boolean = false,
    val peaceSacrificeEnding: Boolean = false,
    val saveCounter: Int = 0,
    val timelineResetCount: Int = 0,
    val pastTimelines: List<TimelineEcho> = emptyList(),
    val choiceHistory: List<String> = emptyList(),
    val savedAtMs: Long = 0L
)

class GameSaveStorage(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        migrateLegacySaveIfNeeded()
    }

    private fun migrateLegacySaveIfNeeded() {
        val legacy = prefs.getString(KEY_SAVE_JSON, null)
        if (!legacy.isNullOrBlank() && !prefs.contains(slotKey(0))) {
            prefs.edit()
                .putString(slotKey(0), legacy)
                .remove(KEY_SAVE_JSON)
                .apply()
        }
    }

    fun getActiveSlot(): Int = prefs.getInt(KEY_ACTIVE_SLOT, 0).coerceIn(0, SLOT_COUNT - 1)

    fun setActiveSlot(slotIndex: Int) {
        prefs.edit().putInt(KEY_ACTIVE_SLOT, slotIndex.coerceIn(0, SLOT_COUNT - 1)).apply()
    }

    fun loadGlobalVolume(): Float = prefs.getFloat(KEY_GLOBAL_VOLUME, 1.0f).coerceIn(0f, 1f)

    fun saveGlobalVolume(volume: Float) {
        prefs.edit().putFloat(KEY_GLOBAL_VOLUME, volume.coerceIn(0f, 1f)).apply()
    }

    fun loadGlobalDifficulty(): GameDifficulty {
        val raw = prefs.getString(KEY_GLOBAL_DIFFICULTY, GameDifficulty.NORMAL.name) ?: GameDifficulty.NORMAL.name
        return runCatching { GameDifficulty.valueOf(raw) }.getOrDefault(GameDifficulty.NORMAL)
    }

    fun saveGlobalDifficulty(difficulty: GameDifficulty) {
        prefs.edit().putString(KEY_GLOBAL_DIFFICULTY, difficulty.name).apply()
    }

    fun loadReduceFlash(): Boolean = prefs.getBoolean(KEY_GLOBAL_REDUCE_FLASH, false)

    fun saveReduceFlash(reduce: Boolean) {
        prefs.edit().putBoolean(KEY_GLOBAL_REDUCE_FLASH, reduce).apply()
    }

    fun listSlotSummaries(): List<SaveSlotSummary> = (0 until SLOT_COUNT).map { idx ->
        val snap = load(idx)
        if (!snap.hasSavedProgress) {
            SaveSlotSummary(slotIndex = idx, isEmpty = true)
        } else {
            SaveSlotSummary(
                slotIndex = idx,
                isEmpty = false,
                activeWorld = snap.activeWorld,
                saveCounter = snap.saveCounter,
                friendsCount = snap.friendAllies.size,
                stoppedCount = snap.stoppedEnemies.size,
                slainCount = snap.defeatedEnemies.size,
                timelineResetCount = snap.timelineResetCount,
                difficulty = snap.difficulty,
                savedAtMs = snap.savedAtMs
            )
        }
    }

    fun load(slotIndex: Int = getActiveSlot()): SavedGameSnapshot {
        val safeSlot = slotIndex.coerceIn(0, SLOT_COUNT - 1)
        val raw = prefs.getString(slotKey(safeSlot), null) ?: return SavedGameSnapshot(
            masterVolume = loadGlobalVolume(),
            difficulty = loadGlobalDifficulty(),
            reduceFlash = loadReduceFlash()
        )
        return try {
            val obj = JSONObject(raw)
            val resumeName = obj.optString("resumeScreen", AppScreen.LORE_INTRO.name)
            val resumeScreen = runCatching { AppScreen.valueOf(resumeName) }.getOrDefault(AppScreen.LORE_INTRO)
                .let { if (it == AppScreen.BATTLE) AppScreen.WORLD_EXPLORE else it }

            val itemObj = obj.optJSONObject("itemCounts")
            val itemMap = mutableMapOf(
                "chrono_shard" to 2,
                "sepia_ration" to 1,
                "save_anchor" to 1
            )
            if (itemObj != null) {
                itemMap.clear()
                val keys = itemObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    itemMap[k] = itemObj.optInt(k, 0)
                }
            }

            val echoesArr = obj.optJSONArray("pastTimelines")
            val echoes = buildList {
                if (echoesArr != null) {
                    for (i in 0 until echoesArr.length()) {
                        val eo = echoesArr.optJSONObject(i) ?: continue
                        add(
                            TimelineEcho(
                                timelineNumber = eo.optInt("timelineNumber", i + 1),
                                routeLabel = eo.optString("routeLabel", "Fractured Run"),
                                friendsCount = eo.optInt("friendsCount", 0),
                                stoppedCount = eo.optInt("stoppedCount", 0),
                                slainCount = eo.optInt("slainCount", 0),
                                kidsRescued = eo.optInt("kidsRescued", 0),
                                kidsKilled = eo.optInt("kidsKilled", 0)
                            )
                        )
                    }
                }
            }

            val historyArr = obj.optJSONArray("choiceHistory")
            val choices = buildList {
                if (historyArr != null) {
                    for (i in 0 until historyArr.length()) {
                        val entry = historyArr.optString(i)
                        if (entry.isNotBlank()) add(entry)
                    }
                }
            }

            SavedGameSnapshot(
                hasSavedProgress = true,
                language = runCatching { AppLanguage.valueOf(obj.optString("language", "EN")) }.getOrDefault(AppLanguage.EN),
                controlScheme = runCatching { ControlScheme.valueOf(obj.optString("controlScheme", "TOUCH_DRAG")) }.getOrDefault(ControlScheme.TOUCH_DRAG),
                isMuted = obj.optBoolean("isMuted", false),
                masterVolume = obj.optDouble("masterVolume", loadGlobalVolume().toDouble()).toFloat().coerceIn(0f, 1f),
                difficulty = runCatching { GameDifficulty.valueOf(obj.optString("difficulty", loadGlobalDifficulty().name)) }.getOrDefault(GameDifficulty.NORMAL),
                reduceFlash = obj.optBoolean("reduceFlash", loadReduceFlash()),
                resumeScreen = resumeScreen,
                ruinsSwitchActivated = obj.optBoolean("ruinsSwitchActivated", false),
                ruinsDummyPracticed = obj.optBoolean("ruinsDummyPracticed", false),
                activeWorld = runCatching { WorldId.valueOf(obj.optString("activeWorld", "NORMAL_WORLD")) }.getOrDefault(WorldId.NORMAL_WORLD),
                activeZone = runCatching { WorldZone.valueOf(obj.optString("activeZone", "CENTER")) }.getOrDefault(WorldZone.CENTER),
                playerMapX = obj.optDouble("playerMapX", 0.50).toFloat().coerceIn(0.05f, 0.95f),
                playerMapY = obj.optDouble("playerMapY", 0.50).toFloat().coerceIn(0.07f, 0.93f),
                defeatedEnemies = obj.optStringSet("defeatedEnemies").mapNotNull { runCatching { EnemyId.valueOf(it) }.getOrNull() }.toSet(),
                friendAllies = obj.optStringSet("friendAllies").mapNotNull { runCatching { EnemyId.valueOf(it) }.getOrNull() }.toSet(),
                stoppedEnemies = obj.optStringSet("stoppedEnemies").mapNotNull { runCatching { EnemyId.valueOf(it) }.getOrNull() }.toSet(),
                angelAidClaimed = obj.optBoolean("angelAidClaimed", false),
                escortingKidIds = obj.optStringSet("escortingKidIds"),
                rescuedToNormalKidIds = obj.optStringSet("rescuedToNormalKidIds"),
                killedKidIds = obj.optStringSet("killedKidIds"),
                completedLandmarkIds = obj.optStringSet("completedLandmarkIds"),
                claimedNpcEvents = obj.optStringSet("claimedNpcEvents"),
                openedChestIds = obj.optStringSet("openedChestIds"),
                unlockedArmorIds = obj.optStringSet("unlockedArmorIds").ifEmpty { setOf("casual_jacket") },
                equippedArmorId = obj.optString("equippedArmorId", "casual_jacket"),
                unlockedWeaponIds = obj.optStringSet("unlockedWeaponIds").ifEmpty { setOf("worn_stick") },
                equippedWeaponId = obj.optString("equippedWeaponId", "worn_stick"),
                unlockedRelicIds = obj.optStringSet("unlockedRelicIds").ifEmpty { setOf("golden_locket") },
                equippedRelicId = obj.optString("equippedRelicId", "golden_locket"),
                itemCounts = itemMap,
                unlockedRoutes = obj.optStringSet("unlockedRoutes").mapNotNull { runCatching { RouteType.valueOf(it) }.getOrNull() }.toSet(),
                activeFinaleRoute = runCatching { RouteType.valueOf(obj.optString("activeFinaleRoute", "MOST_EXPECTED")) }.getOrDefault(RouteType.MOST_EXPECTED),
                isFutureTransformed = obj.optBoolean("isFutureTransformed", false),
                teleportControlMastered = obj.optBoolean("teleportControlMastered", false),
                worldDestroyedEnding = obj.optBoolean("worldDestroyedEnding", false),
                peaceSacrificeEnding = obj.optBoolean("peaceSacrificeEnding", false),
                saveCounter = obj.optInt("saveCounter", 0).coerceIn(0, 8),
                timelineResetCount = obj.optInt("timelineResetCount", 0).coerceAtLeast(0),
                pastTimelines = echoes,
                choiceHistory = choices,
                savedAtMs = obj.optLong("savedAtMs", 0L)
            )
        } catch (_: Throwable) {
            SavedGameSnapshot()
        }
    }

    fun save(snapshot: SavedGameSnapshot, slotIndex: Int = getActiveSlot()) {
        val safeSlot = slotIndex.coerceIn(0, SLOT_COUNT - 1)
        val echoesArr = JSONArray().apply {
            snapshot.pastTimelines.forEach { e ->
                put(
                    JSONObject().apply {
                        put("timelineNumber", e.timelineNumber)
                        put("routeLabel", e.routeLabel)
                        put("friendsCount", e.friendsCount)
                        put("stoppedCount", e.stoppedCount)
                        put("slainCount", e.slainCount)
                        put("kidsRescued", e.kidsRescued)
                        put("kidsKilled", e.kidsKilled)
                    }
                )
            }
        }
        val obj = JSONObject().apply {
            put("language", snapshot.language.name)
            put("controlScheme", snapshot.controlScheme.name)
            put("isMuted", snapshot.isMuted)
            put("masterVolume", snapshot.masterVolume.toDouble())
            put("difficulty", snapshot.difficulty.name)
            put("reduceFlash", snapshot.reduceFlash)
            put("resumeScreen", if (snapshot.resumeScreen == AppScreen.BATTLE) AppScreen.WORLD_EXPLORE.name else snapshot.resumeScreen.name)
            put("ruinsSwitchActivated", snapshot.ruinsSwitchActivated)
            put("ruinsDummyPracticed", snapshot.ruinsDummyPracticed)
            put("activeWorld", snapshot.activeWorld.name)
            put("activeZone", snapshot.activeZone.name)
            put("playerMapX", snapshot.playerMapX.toDouble())
            put("playerMapY", snapshot.playerMapY.toDouble())
            put("defeatedEnemies", JSONArray(snapshot.defeatedEnemies.map { it.name }))
            put("friendAllies", JSONArray(snapshot.friendAllies.map { it.name }))
            put("stoppedEnemies", JSONArray(snapshot.stoppedEnemies.map { it.name }))
            put("angelAidClaimed", snapshot.angelAidClaimed)
            put("escortingKidIds", JSONArray(snapshot.escortingKidIds.toList()))
            put("rescuedToNormalKidIds", JSONArray(snapshot.rescuedToNormalKidIds.toList()))
            put("killedKidIds", JSONArray(snapshot.killedKidIds.toList()))
            put("completedLandmarkIds", JSONArray(snapshot.completedLandmarkIds.toList()))
            put("claimedNpcEvents", JSONArray(snapshot.claimedNpcEvents.toList()))
            put("openedChestIds", JSONArray(snapshot.openedChestIds.toList()))
            put("unlockedArmorIds", JSONArray(snapshot.unlockedArmorIds.toList()))
            put("equippedArmorId", snapshot.equippedArmorId)
            put("unlockedWeaponIds", JSONArray(snapshot.unlockedWeaponIds.toList()))
            put("equippedWeaponId", snapshot.equippedWeaponId)
            put("unlockedRelicIds", JSONArray(snapshot.unlockedRelicIds.toList()))
            put("equippedRelicId", snapshot.equippedRelicId)
            put("itemCounts", JSONObject(snapshot.itemCounts))
            put("unlockedRoutes", JSONArray(snapshot.unlockedRoutes.map { it.name }))
            put("activeFinaleRoute", snapshot.activeFinaleRoute.name)
            put("isFutureTransformed", snapshot.isFutureTransformed)
            put("teleportControlMastered", snapshot.teleportControlMastered)
            put("worldDestroyedEnding", snapshot.worldDestroyedEnding)
            put("peaceSacrificeEnding", snapshot.peaceSacrificeEnding)
            put("saveCounter", snapshot.saveCounter)
            put("timelineResetCount", snapshot.timelineResetCount)
            put("pastTimelines", echoesArr)
            put("choiceHistory", JSONArray(snapshot.choiceHistory.takeLast(30)))
            put("savedAtMs", if (snapshot.savedAtMs > 0L) snapshot.savedAtMs else System.currentTimeMillis())
        }
        prefs.edit().putString(slotKey(safeSlot), obj.toString()).apply()
    }

    fun clear(slotIndex: Int = getActiveSlot()) {
        val safeSlot = slotIndex.coerceIn(0, SLOT_COUNT - 1)
        prefs.edit().remove(slotKey(safeSlot)).apply()
    }

    private fun slotKey(slotIndex: Int): String = "save_slot_$slotIndex"

    private fun JSONObject.optStringSet(key: String): Set<String> {
        val arr = optJSONArray(key) ?: return emptySet()
        return buildSet {
            for (i in 0 until arr.length()) {
                val item = arr.optString(i)
                if (item.isNotBlank()) add(item)
            }
        }
    }

    companion object {
        const val SLOT_COUNT = 3
        private const val PREFS_NAME = "loka_paradox_save_v1"
        private const val KEY_SAVE_JSON = "save_snapshot"
        private const val KEY_ACTIVE_SLOT = "active_save_slot"
        private const val KEY_GLOBAL_VOLUME = "global_master_volume"
        private const val KEY_GLOBAL_DIFFICULTY = "global_difficulty"
        private const val KEY_GLOBAL_REDUCE_FLASH = "global_reduce_flash"
    }
}
