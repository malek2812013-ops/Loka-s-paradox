package com.example.data

import android.content.Context
import com.example.model.AppLanguage
import com.example.model.AppScreen
import com.example.model.ControlScheme
import com.example.model.EnemyId
import com.example.model.RouteType
import com.example.model.WorldId
import com.example.model.WorldZone
import org.json.JSONArray
import org.json.JSONObject

data class SavedGameSnapshot(
    val hasSavedProgress: Boolean = false,
    val language: AppLanguage = AppLanguage.EN,
    val controlScheme: ControlScheme = ControlScheme.TOUCH_DRAG,
    val isMuted: Boolean = false,
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
    val saveCounter: Int = 0
)

class GameSaveStorage(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): SavedGameSnapshot {
        val raw = prefs.getString(KEY_SAVE_JSON, null) ?: return SavedGameSnapshot()
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

            SavedGameSnapshot(
                hasSavedProgress = true,
                language = runCatching { AppLanguage.valueOf(obj.optString("language", "EN")) }.getOrDefault(AppLanguage.EN),
                controlScheme = runCatching { ControlScheme.valueOf(obj.optString("controlScheme", "TOUCH_DRAG")) }.getOrDefault(ControlScheme.TOUCH_DRAG),
                isMuted = obj.optBoolean("isMuted", false),
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
                itemCounts = itemMap,
                unlockedRoutes = obj.optStringSet("unlockedRoutes").mapNotNull { runCatching { RouteType.valueOf(it) }.getOrNull() }.toSet(),
                activeFinaleRoute = runCatching { RouteType.valueOf(obj.optString("activeFinaleRoute", "MOST_EXPECTED")) }.getOrDefault(RouteType.MOST_EXPECTED),
                isFutureTransformed = obj.optBoolean("isFutureTransformed", false),
                teleportControlMastered = obj.optBoolean("teleportControlMastered", false),
                worldDestroyedEnding = obj.optBoolean("worldDestroyedEnding", false),
                peaceSacrificeEnding = obj.optBoolean("peaceSacrificeEnding", false),
                saveCounter = obj.optInt("saveCounter", 0).coerceIn(0, 8)
            )
        } catch (_: Throwable) {
            SavedGameSnapshot()
        }
    }

    fun save(snapshot: SavedGameSnapshot) {
        val obj = JSONObject().apply {
            put("language", snapshot.language.name)
            put("controlScheme", snapshot.controlScheme.name)
            put("isMuted", snapshot.isMuted)
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
            put("itemCounts", JSONObject(snapshot.itemCounts))
            put("unlockedRoutes", JSONArray(snapshot.unlockedRoutes.map { it.name }))
            put("activeFinaleRoute", snapshot.activeFinaleRoute.name)
            put("isFutureTransformed", snapshot.isFutureTransformed)
            put("teleportControlMastered", snapshot.teleportControlMastered)
            put("worldDestroyedEnding", snapshot.worldDestroyedEnding)
            put("peaceSacrificeEnding", snapshot.peaceSacrificeEnding)
            put("saveCounter", snapshot.saveCounter)
        }
        prefs.edit().putString(KEY_SAVE_JSON, obj.toString()).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_SAVE_JSON).apply()
    }

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
        private const val PREFS_NAME = "loka_paradox_save_v1"
        private const val KEY_SAVE_JSON = "save_snapshot"
    }
}
