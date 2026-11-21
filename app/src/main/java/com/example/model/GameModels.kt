package com.example.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaAmber
import com.example.ui.theme.SoulAegisAmber
import com.example.ui.theme.SoulInfernalViolet
import com.example.ui.theme.SoulParadoxCrimson
import com.example.ui.theme.SoulSeraphCyan

enum class AppScreen {
    LORE_INTRO,
    RUINS_TUTORIAL,
    ASHEN_MEETING,
    WORLD_EXPLORE,
    BATTLE,
    ROUTE_FINALE
}

enum class RouteType(
    val title: String,
    val subtitle: String,
    val badgeColor: Color
) {
    NO_MERCY(
        title = "No Mercy Route",
        subtitle = "Kill everyone · Defeat Future-Loka · Destroy the world",
        badgeColor = Color(0xFFEF4444)
    ),
    MOST_EXPECTED(
        title = "The Most Expected Route",
        subtitle = "Mixed mercy & kills · Sacrifice Loka's life for 4-race peace",
        badgeColor = Color(0xFFF59E0B)
    ),
    PERFECT(
        title = "The Perfect Route",
        subtitle = "Save & spare everyone · Inherit Future-Loka's power across timelines",
        badgeColor = Color(0xFF38BDF8)
    ),
    LOST_TAPES(
        title = "Lost Tapes Route",
        subtitle = "Uncover every secret · Master random teleport · Restart the world",
        badgeColor = Color(0xFFA855F7)
    )
}

enum class ControlScheme(val label: String, val labelAr: String) {
    TOUCH_DRAG("Drag", "سحب"),
    DPAD_8WAY("Pad 1 (8-Way)", "أزرار 1"),
    JOYSTICK("Wheel 2 (Stick)", "عجلة 2")
}

enum class PortraitExpression {
    NEUTRAL,
    HAPPY,
    SURPRISED,
    ANGRY,
    DETERMINED
}

/**
 * World-specific Souls matching the user's hand-drawn designs & specifications:
 * - Normal World ONLY: Red Soul (Undertale Determination style, medium balanced speed)
 * - Outlands Realm: Yellow Soul, Blue Soul, Split Duo Soul
 * - Split Realm: Purple Soul (fast, weaker ATK), Olive-Green Soul (high DEF, slow, larger hitbox), Split Duo Soul
 * - Glitched World: White Sick Soul (slow, deals 1 DMG, takes 2x DMG)
 */
enum class SoulType(
    val title: String,
    val titleAr: String,
    val badge: String,
    val color: Color,
    val secondaryColor: Color? = null,
    val speedNormPerSec: Float,
    val abilityName: String,
    val abilityNameAr: String,
    val abilityDesc: String,
    val abilityDescAr: String,
    val cooldownSec: Float,
    val damageMultiplier: Float = 1f,
    val flatDamageCap: Int? = null,
    val incomingDamageMultiplier: Int = 1,
    val defenseBonus: Int = 0,
    val hitboxScale: Float = 1f
) {
    RED(
        title = "Red Soul",
        titleAr = "الروح الحمراء",
        badge = "Normal World ONLY · Determination",
        color = Color(0xFFE11D48),
        speedNormPerSec = 0.54f, // Medium balanced Undertale speed (not fast)
        abilityName = "[C] Chrono Snap",
        abilityNameAr = "[C] ومضة الزمن",
        abilityDesc = "Normal World only: moves freely at balanced medium speed; [C] snaps to Time Anchor.",
        abilityDescAr = "في العالم الطبيعي فقط: تتحرك في كل مكان بسرعة متوسطة تشبه اندرتيل.",
        cooldownSec = 2.4f
    ),
    YELLOW(
        title = "Yellow Soul",
        titleAr = "الروح الصفراء",
        badge = "Outlands · Solar Railgun",
        color = Color(0xFFFACC15),
        speedNormPerSec = 0.64f,
        abilityName = "[C] Solar Burst",
        abilityNameAr = "[C] طلقة الشمس",
        abilityDesc = "Fires 5-way Solar Bolts that shatter bullets AND chip Enemy HP mid-dodge!",
        abilityDescAr = "تطلق 5 أشعة شمسية تكسر الطلقات وتنقص دم العدو.",
        cooldownSec = 1.6f
    ),
    BLUE(
        title = "Blue Soul",
        titleAr = "الروح الزرقاء",
        badge = "Outlands · Tidal Orbit",
        color = Color(0xFF3B82F6),
        speedNormPerSec = 0.62f,
        abilityName = "[C] Tidal Repel",
        abilityNameAr = "[C] دفع الموجة",
        abilityDesc = "Magnetic ring bends bullets away; [C] pushes nearby bullets back.",
        abilityDescAr = "حلقة مغناطيسية تبعد الطلقات، و[C] يدفع الهجمات بعيداً.",
        cooldownSec = 2.2f
    ),
    PURPLE(
        title = "Purple Soul",
        titleAr = "الروح البنفسجية",
        badge = "Split Realm · Fast & Light Hit",
        color = Color(0xFFD946EF),
        speedNormPerSec = 0.90f, // Moves noticeably faster!
        abilityName = "[C] Rift Phase",
        abilityNameAr = "[C] عبور الأطياف",
        abilityDesc = "Moves much faster while dodging, but strikes deal weaker damage to enemies.",
        abilityDescAr = "تتحرك بشكل أسرع، لكن الضرر الذي تسجله للعدو أضعف.",
        cooldownSec = 2.8f,
        damageMultiplier = 0.62f // Weaker attack damage to enemy
    ),
    GREEN(
        title = "Olive Soul",
        titleAr = "الروح الزيتية",
        badge = "Split Realm · High DEF · Big Hitbox",
        color = Color(0xFF5E7A29), // Olive Green matching drawing
        secondaryColor = Color(0xFF84A93C),
        speedNormPerSec = 0.42f, // Slower movement
        abilityName = "[C] Olive Bastion",
        abilityNameAr = "[C] حصن الزيتي",
        abilityDesc = "Raises Defense (+2 DEF), moves slowly, and has a larger hitbox.",
        abilityDescAr = "تجعل نقاط الدفاع لديك أعلى وتتحرك ببطء والـ Hitbox الخاص بك أكبر.",
        cooldownSec = 3.2f,
        defenseBonus = 2,
        hitboxScale = 1.55f
    ),
    SPLIT_DUO(
        title = "Split Soul",
        titleAr = "الروح الانقسامية",
        badge = "Outlands & Split · [C] Split / Swap",
        color = Color(0xFFE11D48), // Left Red half
        secondaryColor = Color(0xFF2DD4BF), // Right Teal half
        speedNormPerSec = 0.58f,
        abilityName = "[C] Split / Switch",
        abilityNameAr = "[C] انقسام / تبديل",
        abilityDesc = "Press [C] to split into 2 halves. Aim toward the other half + [C] to swap. If 1 half is hit, the other becomes faster!",
        abilityDescAr = "تنقسم عند ضغط [C]. وجّه للجزء الثاني واضغط [C] للانتقال إليه. وإذا تضرر جزء يصبح الآخر أسرع!",
        cooldownSec = 0.35f
    ),
    WHITE_SICK(
        title = "Sick White Soul",
        titleAr = "الروح البيضاء المريضة",
        badge = "Glitched World · Weakened",
        color = Color(0xFFF3F4F6),
        speedNormPerSec = 0.34f,
        abilityName = "[C] Faint Pulse",
        abilityNameAr = "[C] نبضة باهتة",
        abilityDesc = "Weakened in the Glitch: slow speed, deals 1 DMG, takes 2x DMG.",
        abilityDescAr = "مُضعفة في العالم المقلتش: بطيئة وتسبب 1 ضرر وتأخذ ضعف الضرر.",
        cooldownSec = 4.5f,
        damageMultiplier = 0.1f,
        flatDamageCap = 1,
        incomingDamageMultiplier = 2
    )
}

enum class WorldId(
    val title: String,
    val subtitle: String,
    val racesPresent: String,
    val allowedSouls: List<SoulType>,
    val hasLandmarks: Boolean
) {
    NORMAL_WORLD(
        title = "Normal World",
        subtitle = "Everyday streets, green hills, caves & research labs",
        racesPresent = "Humans · Monsters · Angels · Devils",
        allowedSouls = listOf(SoulType.RED),
        hasLandmarks = true
    ),
    TRI_RACE_REALM(
        title = "Outlands Realm",
        subtitle = "Beige dunes, cyan trees & deep brown caverns",
        racesPresent = "Monsters · Angels · Devils (No Humans)",
        allowedSouls = listOf(SoulType.YELLOW, SoulType.BLUE, SoulType.SPLIT_DUO),
        hasLandmarks = true
    ),
    SPLIT_REALM(
        title = "The Split Dominion",
        subtitle = "Angels' purple-pink groves vs Devils' brown-grey wastes",
        racesPresent = "Angels (West) · Devils (East)",
        allowedSouls = listOf(SoulType.PURPLE, SoulType.GREEN, SoulType.SPLIT_DUO),
        hasLandmarks = true
    ),
    GOVERNMENT_HQ(
        title = "The Government HQ",
        subtitle = "Central Council Citadel — Stop them (Mercy or Force)! Angel Leader helps you.",
        racesPresent = "Gov. Council · Angel Leader (Ally) · Monster Chief (Only Friend)",
        allowedSouls = listOf(SoulType.RED, SoulType.YELLOW, SoulType.PURPLE, SoulType.GREEN),
        hasLandmarks = true
    ),
    GLITCHED_WORLD(
        title = "Glitched Void",
        subtitle = "No structures. Ghostly spawns. Anomaly ends in 10s...",
        racesPresent = "Corrupted Echoes Only",
        allowedSouls = listOf(SoulType.WHITE_SICK),
        hasLandmarks = false
    )
}

enum class WorldZone(val titleEn: String, val titleAr: String) {
    CENTER("Central Plaza", "الساحة المركزية"),
    NORTH("Northern Sector", "البوابة الشمالية"),
    SOUTH("Southern Sector", "البوابة الجنوبية"),
    WEST("Western Sector", "البوابة الغربية"),
    EAST("Eastern Sector", "البوابة الشرقية"),
    SANCTUM("Sanctum Vault (Door V)", "المعبد السري (الباب 5)")
}

enum class ZoneEdge {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}

data class ZoneDoorInfo(
    val edge: ZoneEdge?,
    val targetZone: WorldZone,
    val spawnX: Float,
    val spawnY: Float,
    val labelEn: String,
    val labelAr: String,
    val doorX: Float,
    val doorY: Float,
    val targetWorld: WorldId? = null
)

object WorldZoneMap {
    fun doorsInZone(world: WorldId, zone: WorldZone): List<ZoneDoorInfo> {
        if (!world.hasLandmarks) return emptyList()
        val innerDoors = when (zone) {
            WorldZone.CENTER -> listOf(
                ZoneDoorInfo(ZoneEdge.TOP, WorldZone.NORTH, 0.50f, 0.81f, "NORTH", "الشمال", 0.50f, 0.05f),
                ZoneDoorInfo(ZoneEdge.BOTTOM, WorldZone.SOUTH, 0.50f, 0.19f, "SOUTH", "الجنوب", 0.50f, 0.95f),
                ZoneDoorInfo(ZoneEdge.LEFT, WorldZone.WEST, 0.83f, 0.50f, "WEST", "الغرب", 0.04f, 0.50f),
                ZoneDoorInfo(ZoneEdge.RIGHT, WorldZone.EAST, 0.17f, 0.50f, "EAST", "الشرق", 0.96f, 0.50f),
                ZoneDoorInfo(null, WorldZone.SANCTUM, 0.50f, 0.81f, "DOOR V", "الباب ٥", 0.84f, 0.16f)
            )
            WorldZone.NORTH -> listOf(
                ZoneDoorInfo(ZoneEdge.BOTTOM, WorldZone.CENTER, 0.50f, 0.19f, "CENTER", "المركز", 0.50f, 0.95f),
                ZoneDoorInfo(ZoneEdge.LEFT, WorldZone.WEST, 0.50f, 0.19f, "WEST", "الغرب", 0.04f, 0.50f),
                ZoneDoorInfo(ZoneEdge.RIGHT, WorldZone.EAST, 0.50f, 0.19f, "EAST", "الشرق", 0.96f, 0.50f)
            )
            WorldZone.SOUTH -> listOf(
                ZoneDoorInfo(ZoneEdge.TOP, WorldZone.CENTER, 0.50f, 0.81f, "CENTER", "المركز", 0.50f, 0.05f),
                ZoneDoorInfo(ZoneEdge.LEFT, WorldZone.WEST, 0.50f, 0.81f, "WEST", "الغرب", 0.04f, 0.50f),
                ZoneDoorInfo(ZoneEdge.RIGHT, WorldZone.EAST, 0.50f, 0.81f, "EAST", "الشرق", 0.96f, 0.50f)
            )
            WorldZone.WEST -> listOf(
                ZoneDoorInfo(ZoneEdge.RIGHT, WorldZone.CENTER, 0.17f, 0.50f, "CENTER", "المركز", 0.96f, 0.50f),
                ZoneDoorInfo(ZoneEdge.TOP, WorldZone.NORTH, 0.17f, 0.50f, "NORTH", "الشمال", 0.50f, 0.05f),
                ZoneDoorInfo(ZoneEdge.BOTTOM, WorldZone.SOUTH, 0.17f, 0.50f, "SOUTH", "الجنوب", 0.50f, 0.95f)
            )
            WorldZone.EAST -> listOf(
                ZoneDoorInfo(ZoneEdge.LEFT, WorldZone.CENTER, 0.83f, 0.50f, "CENTER", "المركز", 0.04f, 0.50f),
                ZoneDoorInfo(ZoneEdge.TOP, WorldZone.NORTH, 0.83f, 0.50f, "NORTH", "الشمال", 0.50f, 0.05f),
                ZoneDoorInfo(ZoneEdge.BOTTOM, WorldZone.SOUTH, 0.83f, 0.50f, "SOUTH", "الجنوب", 0.50f, 0.95f)
            )
            WorldZone.SANCTUM -> listOf(
                ZoneDoorInfo(ZoneEdge.BOTTOM, WorldZone.CENTER, 0.78f, 0.25f, "PLAZA", "الساحة", 0.50f, 0.95f),
                ZoneDoorInfo(ZoneEdge.TOP, WorldZone.NORTH, 0.50f, 0.19f, "NORTH", "الشمال", 0.50f, 0.05f)
            )
        }

        // Cross-World Bridge Gates linking all 4 physical worlds (Glitched Void remains anomaly-only!)
        val worldBridgeDoors = when (world) {
            WorldId.NORMAL_WORLD -> when (zone) {
                WorldZone.EAST -> listOf(
                    ZoneDoorInfo(ZoneEdge.RIGHT, WorldZone.WEST, 0.17f, 0.50f, "OUTLANDS", "عالم البراري", 0.96f, 0.50f, targetWorld = WorldId.TRI_RACE_REALM)
                )
                WorldZone.WEST -> listOf(
                    ZoneDoorInfo(ZoneEdge.LEFT, WorldZone.EAST, 0.83f, 0.50f, "SPLIT REALM", "العالم المنقسم", 0.04f, 0.50f, targetWorld = WorldId.SPLIT_REALM)
                )
                WorldZone.NORTH -> listOf(
                    ZoneDoorInfo(ZoneEdge.TOP, WorldZone.SOUTH, 0.50f, 0.81f, "GOVERNMENT", "الحكومة", 0.50f, 0.05f, targetWorld = WorldId.GOVERNMENT_HQ)
                )
                else -> emptyList()
            }

            WorldId.TRI_RACE_REALM -> when (zone) {
                WorldZone.WEST -> listOf(
                    ZoneDoorInfo(ZoneEdge.LEFT, WorldZone.EAST, 0.83f, 0.50f, "NORMAL WORLD", "العالم الطبيعي", 0.04f, 0.50f, targetWorld = WorldId.NORMAL_WORLD)
                )
                WorldZone.EAST -> listOf(
                    ZoneDoorInfo(ZoneEdge.RIGHT, WorldZone.WEST, 0.17f, 0.50f, "SPLIT REALM", "العالم المنقسم", 0.96f, 0.50f, targetWorld = WorldId.SPLIT_REALM)
                )
                WorldZone.NORTH -> listOf(
                    ZoneDoorInfo(ZoneEdge.TOP, WorldZone.EAST, 0.83f, 0.50f, "GOVERNMENT", "الحكومة", 0.50f, 0.05f, targetWorld = WorldId.GOVERNMENT_HQ)
                )
                else -> emptyList()
            }

            WorldId.SPLIT_REALM -> when (zone) {
                WorldZone.EAST -> listOf(
                    ZoneDoorInfo(ZoneEdge.RIGHT, WorldZone.WEST, 0.17f, 0.50f, "NORMAL WORLD", "العالم الطبيعي", 0.96f, 0.50f, targetWorld = WorldId.NORMAL_WORLD)
                )
                WorldZone.WEST -> listOf(
                    ZoneDoorInfo(ZoneEdge.LEFT, WorldZone.EAST, 0.83f, 0.50f, "OUTLANDS", "عالم البراري", 0.04f, 0.50f, targetWorld = WorldId.TRI_RACE_REALM)
                )
                WorldZone.NORTH -> listOf(
                    ZoneDoorInfo(ZoneEdge.TOP, WorldZone.WEST, 0.17f, 0.50f, "GOVERNMENT", "الحكومة", 0.50f, 0.05f, targetWorld = WorldId.GOVERNMENT_HQ)
                )
                else -> emptyList()
            }

            WorldId.GOVERNMENT_HQ -> when (zone) {
                WorldZone.SOUTH -> listOf(
                    ZoneDoorInfo(ZoneEdge.BOTTOM, WorldZone.NORTH, 0.50f, 0.19f, "NORMAL WORLD", "العالم الطبيعي", 0.50f, 0.95f, targetWorld = WorldId.NORMAL_WORLD)
                )
                WorldZone.EAST -> listOf(
                    ZoneDoorInfo(ZoneEdge.RIGHT, WorldZone.NORTH, 0.50f, 0.19f, "OUTLANDS", "عالم البراري", 0.96f, 0.50f, targetWorld = WorldId.TRI_RACE_REALM)
                )
                WorldZone.WEST -> listOf(
                    ZoneDoorInfo(ZoneEdge.LEFT, WorldZone.NORTH, 0.50f, 0.19f, "SPLIT REALM", "العالم المنقسم", 0.04f, 0.50f, targetWorld = WorldId.SPLIT_REALM)
                )
                WorldZone.NORTH -> listOf(
                    ZoneDoorInfo(ZoneEdge.TOP, WorldZone.SANCTUM, 0.50f, 0.81f, "DOOR V", "الباب ٥", 0.50f, 0.05f)
                )
                else -> emptyList()
            }

            WorldId.GLITCHED_WORLD -> emptyList()
        }

        return innerDoors + worldBridgeDoors
    }
}

enum class LandmarkKind(val label: String) {
    HILLS("Hills"),
    CAVE("Cave"),
    MANSION("Mansion"),
    LAB("Lab")
}

data class WorldLandmark(
    val id: String,
    val worldId: WorldId,
    val kind: LandmarkKind,
    val name: String,
    val xNorm: Float,
    val yNorm: Float,
    val questTitle: String,
    val questSummary: String,
    val isSecret: Boolean = false,
    val rewardItemId: String? = null,
    val rewardArmorId: String? = null,
    val rewardWeaponId: String? = null,
    val rewardLabel: String,
    val zone: WorldZone = WorldZone.CENTER
)

data class WorldNpc(
    val id: String,
    val worldId: WorldId,
    val name: String,
    val race: String,
    val xNorm: Float,
    val yNorm: Float,
    val accentColor: Color,
    val dialogue: String,
    val eventTitle: String? = null,
    val eventRewardItemId: String? = null,
    val eventRewardText: String? = null,
    val zone: WorldZone = WorldZone.CENTER
)

enum class ChestStyle {
    SMALL_DOT,
    SQUARE_STAR,
    WIDE_PADLOCK
}

data class WorldChest(
    val id: String,
    val worldId: WorldId,
    val style: ChestStyle,
    val nameEn: String,
    val nameAr: String,
    val xNorm: Float,
    val yNorm: Float,
    val rewardItemId: String,
    val rewardCount: Int,
    val zone: WorldZone = WorldZone.CENTER
)

data class ArmorEquipment(
    val id: String,
    val name: String,
    val defenseBonus: Int,
    val maxHpBonus: Int,
    val description: String
)

object ArmorCatalog {
    val all: List<ArmorEquipment> = listOf(
        ArmorEquipment("casual_jacket", "Loka's Grey Tee & Scarf", 0, 0, "Everyday casual clothes with a golden heart necklace."),
        ArmorEquipment("hill_cloak", "Greenhill Cloak", 1, 4, "Found in the Normal World Hills. +1 DEF, +4 Max HP."),
        ArmorEquipment("cyan_bark_vest", "Cyan Bark Mail", 2, 6, "Woven from Outlands cyan trees. +2 DEF, +6 Max HP."),
        ArmorEquipment("seraph_silk", "Pink-Silk Halo Mantle", 2, 8, "Blessed in the Angels' Mansion. +2 DEF, +8 Max HP."),
        ArmorEquipment("rift_plate", "Ash-Iron Devil Guard", 3, 6, "Forged in the Devils' Cave. +3 DEF, +6 Max HP."),
        ArmorEquipment("angel_seal_mail", "Angel Leader's Seal", 3, 8, "Gifted by Angel Leader Cassiel inside The Government. +3 DEF, +8 Max HP.")
    )

    fun byId(id: String): ArmorEquipment = all.firstOrNull { it.id == id } ?: all.first()
}

enum class WeaponStyle {
    SINGLE_BAR,
    WRENCH_SPARK,
    DUAL_BLADE,
    CHRONO_ARC
}

data class WeaponEquipment(
    val id: String,
    val name: String,
    val nameAr: String,
    val attackBonus: Int,
    val cursorSpeedMultiplier: Float,
    val critWindowBonus: Float,
    val strikeMultiplier: Float,
    val style: WeaponStyle,
    val accentColor: Color,
    val descriptionEn: String,
    val descriptionAr: String
)

object WeaponCatalog {
    val all: List<WeaponEquipment> = listOf(
        WeaponEquipment(
            id = "worn_stick",
            name = "Worn Stick",
            nameAr = "العصا البالية",
            attackBonus = 0,
            cursorSpeedMultiplier = 1.0f,
            critWindowBonus = 0f,
            strikeMultiplier = 1.0f,
            style = WeaponStyle.SINGLE_BAR,
            accentColor = Color(0xFFD6C29F),
            descriptionEn = "Balanced single strike bar.",
            descriptionAr = "شريط ضرب متوازن."
        ),
        WeaponEquipment(
            id = "paradox_wrench",
            name = "Paradox Wrench",
            nameAr = "مفتاح المفارقة",
            attackBonus = 3,
            cursorSpeedMultiplier = 1.14f,
            critWindowBonus = 0.03f,
            strikeMultiplier = 1.10f,
            style = WeaponStyle.WRENCH_SPARK,
            accentColor = Color(0xFF38BDF8),
            descriptionEn = "+3 ATK · Cyan spark impact.",
            descriptionAr = "+3 هجوم · ضربة كهربائية سماوية."
        ),
        WeaponEquipment(
            id = "seraph_blade",
            name = "Seraph Feather Blade",
            nameAr = "نصل ريشة السيراف",
            attackBonus = 5,
            cursorSpeedMultiplier = 1.24f,
            critWindowBonus = 0.06f,
            strikeMultiplier = 1.25f,
            style = WeaponStyle.DUAL_BLADE,
            accentColor = Color(0xFFFACC15),
            descriptionEn = "+5 ATK · Twin precision slashes (1.25x DMG).",
            descriptionAr = "+5 هجوم · ضربتان متزامنتان بضرر مضاعف."
        ),
        WeaponEquipment(
            id = "chrono_lance",
            name = "Chrono Lance",
            nameAr = "رمح الزمن",
            attackBonus = 7,
            cursorSpeedMultiplier = 1.32f,
            critWindowBonus = 0.09f,
            strikeMultiplier = 1.38f,
            style = WeaponStyle.CHRONO_ARC,
            accentColor = Color(0xFFD946EF),
            descriptionEn = "+7 ATK · Wide Golden Crit Arc (1.38x DMG).",
            descriptionAr = "+7 هجوم · قوس زمني ذهبي بضرر ضخم."
        )
    )

    fun byId(id: String): WeaponEquipment = all.firstOrNull { it.id == id } ?: all.first()
}

data class LoreSlide(
    val id: Int,
    val chapterTag: String,
    val chapterTagAr: String = chapterTag,
    @param:DrawableRes val imageRes: Int?,
    val narration: String,
    val narrationAr: String = narration,
    val isGlitchFinale: Boolean = false
)

object DefaultLoreRepository {
    // Note: Echo-Loka and the 8-save secret are strictly excluded before playing!
    fun defaultSlides(): List<LoreSlide> = listOf(
        LoreSlide(
            id = 1,
            chapterTag = "ARCHIVE 01 // THE FOUR RACES",
            chapterTagAr = "الأرشيف 01 // الأجناس الأربعة",
            imageRes = R.drawable.img_pixel_four_races,
            narration = "In this world there are 4 races: Humans, Monsters, Angels, and Devils.",
            narrationAr = "في هذا العالم تعيش 4 أجناس: البشر، الوحوش، الملائكة، والشياطين."
        ),
        LoreSlide(
            id = 2,
            chapterTag = "ARCHIVE 02 // LOKA'S PARADOX",
            chapterTagAr = "الأرشيف 02 // مفارقة لوكا",
            imageRes = R.drawable.img_pixel_loka_paradox,
            narration = "A kid named Loka fell into a time paradox and ended up teleporting every 10 minutes into unknown worlds.",
            narrationAr = "وقع طفل يُدعى لوكا في مفارقة زمنية، وأصبح ينتقل فجأة كل 10 دقائق إلى عوالم مجهولة."
        ),
        LoreSlide(
            id = 3,
            chapterTag = "ARCHIVE 03 // THE FOUR-RACE WAR",
            chapterTagAr = "الأرشيف 03 // حرب الأجناس",
            imageRes = R.drawable.img_pixel_race_war,
            narration = "Because of this, he got lost in the Government's eyes. War started between all the races because of those time paradoxes.",
            narrationAr = "بسبب ذلك ضاع عن أعين الحكومة، فاشتعلت حرب كبرى بين جميع الأجناس بسبب تلك المفارقات الزمنية."
        ),
        LoreSlide(
            id = 4,
            chapterTag = "ARCHIVE 04 // YEARS LATER",
            chapterTagAr = "الأرشيف 04 // بعد سنوات",
            imageRes = R.drawable.img_pixel_more_kids,
            narration = "After a few years, more kids fell into the same paradox as Loka. And what really hides behind the warps...",
            narrationAr = "بعد بضع سنوات، وقع أطفال آخرون في نفس مفارقة لوكا. وما يختبئ خلف هذه الانتقالات..."
        ),
        LoreSlide(
            id = 5,
            chapterTag = "ERR // SIGNAL CORRUPTED",
            chapterTagAr = "خطأ // انقطاع الإشارة",
            imageRes = null,
            narration = "THERE IS STILL MORE TO SEE— THE REST OF THE RECORD WAS— [TAKEN AWAY]",
            narrationAr = "لا يزال هناك المزيد لتراه— بقية التسجيل تم— [سحبه وحذفه]",
            isGlitchFinale = true
        )
    )
}

enum class EnemyId {
    RUINS_DUMMY, // Tutorial encounter inside the starting Ruins
    VEX,
    BRAMBLE,
    VESPERA,
    ORIN,
    SERAPHIEL,
    MALAKOR,
    // Government HQ Bosses: ONLY GOV_MONSTER_CHIEF can become your friend! GOV_DIRECTOR is stopped without killing or defeated.
    GOV_MONSTER_CHIEF,
    GOV_DIRECTOR,
    // Overworld roaming normal monsters (some friendable, some hostile & non-friendable)
    NORMAL_SLIME,
    FERAL_HOUND,
    SAND_MOTH,
    IRON_STALKER,
    HALO_SCRIBE,
    ASH_IMP,
    ECHO_LOKA, // ONLY spawns inside the Glitched World & unlocked after journey ends!
    FUTURE_LOKA // Final confrontation in Route finales
}

enum class BossBehavior {
    IMMEDIATE,
    WORLD_COMPLETION,
    CROSS_WORLD_CHASER
}

data class EnemyActOption(
    val id: String,
    val title: String,
    val response: String,
    val mercyGain: Int,
    val requiredPrevActId: String? = null,
    val outOfOrderReply: String? = null
)

data class EnemyProfile(
    val id: EnemyId,
    val worldId: WorldId,
    val name: String,
    val raceTitle: String,
    val difficultyLabel: String,
    val maxHp: Int,
    val attackPower: Int,
    val accentColor: Color,
    val xNorm: Float,
    val yNorm: Float,
    val allyBonusTitle: String,
    val allyBonusDesc: String,
    val introLine: String,
    val attackQuips: List<String>,
    val spareLine: String,
    val defeatLine: String,
    val actOptions: List<EnemyActOption>,
    val isNormalMonster: Boolean = false,
    val canBefriend: Boolean = true,
    val canStopWithoutKilling: Boolean = canBefriend,
    val zone: WorldZone = WorldZone.CENTER,
    val bossBehavior: BossBehavior = BossBehavior.IMMEDIATE,
    val spareMethodHintEn: String = "",
    val spareMethodHintAr: String = ""
)

object EnemyRoster {
    val all: List<EnemyProfile> = listOf(
        EnemyProfile(
            id = EnemyId.RUINS_DUMMY,
            worldId = WorldId.NORMAL_WORLD,
            name = "Ruins Training Dummy",
            raceTitle = "Ancient Cotton Dummy · Ruins Practice",
            difficultyLabel = "RUINS PRACTICE",
            maxHp = 35,
            attackPower = 1,
            accentColor = Color(0xFFD8B4FE),
            xNorm = 0.56f,
            yNorm = 0.50f,
            allyBonusTitle = "Practice Complete",
            allyBonusDesc = "Taught Loka how to dodge, strike, and befriend.",
            introLine = "Loka: \"It's a cotton Dummy! Try FIGHT to strike the center, or ACT -> SPARE to befriend.\"",
            attackQuips = listOf(
                "Loka: \"Now drag or use D-Pad to dodge the soft cotton stars!\"",
                "Loka: \"Try tapping your Soul Ability button while dodging!\""
            ),
            spareLine = "The Dummy seems strangely happy to practice with you!",
            defeatLine = "Cotton puffs drift through the purple stone hall...",
            actOptions = listOf(
                EnemyActOption("check", "Check", "RUINS DUMMY — 35 HP. Soft practice target in the ancient Ruins.", 25),
                EnemyActOption("talk", "Talk Nicely", "Loka says hello. Even a Dummy likes kind words!", 85)
            )
        ),
        EnemyProfile(
            id = EnemyId.VEX,
            worldId = WorldId.NORMAL_WORLD,
            name = "Commander Vex",
            raceTitle = "Human · Paradox Tracker (Hunts Across Worlds)",
            difficultyLabel = "CHASER BOSS",
            maxHp = 85,
            attackPower = 3,
            accentColor = SoulAegisAmber,
            xNorm = 0.72f,
            yNorm = 0.36f,
            zone = WorldZone.NORTH,
            bossBehavior = BossBehavior.CROSS_WORLD_CHASER,
            spareMethodHintEn = "Hunts you through ALL worlds! Any ACT gives +10% Mercy.",
            spareMethodHintAr = "يطاردك في كل العوالم! كل ACT يعطي +10% رحمة.",
            allyBonusTitle = "Vex's Tactical Visor",
            allyBonusDesc = "+2 Attack Damage & wider Critical strike zone.",
            introLine = "Vex locks his visor. \"Loka—wait! I tracked your paradox signal across the gates!\"",
            attackQuips = listOf(
                "\"Containment grid online! Stay inside the perimeter!\"",
                "\"Wherever you warp, my scanner follows!\"",
                "\"We just want to find the other missing kids!\""
            ),
            spareLine = "Vex lowers his weapon and becomes your friend! \"Call me whenever the paradox flares up, Loka.\"",
            defeatLine = "Vex's scanner shorts out and he retreats down the street.",
            actOptions = listOf(
                EnemyActOption("check", "Check", "COMMANDER VEX — 85 HP. Tracks Loka through every realm! (+10% Mercy)", 10),
                EnemyActOption("explain", "Show Necklace", "You show your golden heart necklace. Vex recognizes you and softens! (+10% Mercy)", 10),
                EnemyActOption("promise", "Team Up", "You promise to share what you find in the labs. Vex smiles! (+10% Mercy)", 10)
            )
        ),
        EnemyProfile(
            id = EnemyId.BRAMBLE,
            worldId = WorldId.NORMAL_WORLD,
            name = "Bramble",
            raceTitle = "Monster · Park & Cave Keeper",
            difficultyLabel = "NORMAL WORLD",
            maxHp = 95,
            attackPower = 3,
            accentColor = Color(0xFF4ADE80),
            xNorm = 0.72f,
            yNorm = 0.66f,
            zone = WorldZone.SOUTH,
            bossBehavior = BossBehavior.IMMEDIATE,
            spareMethodHintEn = "Battle immediately · Any ACT +10% Mercy.",
            spareMethodHintAr = "قتال فوري · أي خيار ACT يعطي +10% رحمة.",
            allyBonusTitle = "Bramble's Moss Guard",
            allyBonusDesc = "+4 Max HP & +1 Defense across every battle.",
            introLine = "Bramble blocks the cave entrance nervously. \"Humans and Monsters... are we still fighting?\"",
            attackQuips = listOf(
                "\"Watch out for falling pebbles!\"",
                "\"Sorry! My hooves shake when time glitches!\"",
                "\"I'm protecting the kids hiding in this cave!\""
            ),
            spareLine = "Bramble beams with joy and joins as your friend! \"Any friend of Loka is safe by my fire!\"",
            defeatLine = "Bramble lumbers back into the shadows of the cave.",
            actOptions = listOf(
                EnemyActOption("check", "Check", "BRAMBLE — 95 HP. Gentle monster protecting lost children near the hills.", 10),
                EnemyActOption("calm", "Share Snacks", "You offer a warm biscuit. Bramble's horns glow happily!", 10),
                EnemyActOption("reassure", "Promise Peace", "You tell him the Red Soul seeks harmony. He nods.", 10)
            )
        ),
        EnemyProfile(
            id = EnemyId.VESPERA,
            worldId = WorldId.TRI_RACE_REALM,
            name = "Lady Vespera",
            raceTitle = "Monster Noble · Outlands Mansion",
            difficultyLabel = "OUTLANDS",
            maxHp = 105,
            attackPower = 4,
            accentColor = Color(0xFFFACC15),
            xNorm = 0.68f,
            yNorm = 0.66f,
            zone = WorldZone.EAST,
            bossBehavior = BossBehavior.IMMEDIATE,
            spareMethodHintEn = "Order: Check -> Bow -> Praise Trees (+10% each).",
            spareMethodHintAr = "الترتيب: فحص -> انحناء -> مدح الأشجار (+10% لكل خطوة).",
            allyBonusTitle = "Vespera's Cyan Tea",
            allyBonusDesc = "Restores +1 HP at the start of every player turn.",
            introLine = "Vespera adjusts her cyan-silk fan. \"Mind your etiquette, traveler, if you seek peace!\"",
            attackQuips = listOf(
                "\"Show me the speed of your Blue Soul leap!\"",
                "\"Aim true with your Yellow Soul light!\"",
                "\"Proper manners first, then tea!\""
            ),
            spareLine = "Lady Vespera curtsies warmly and becomes your ally! \"My mansion doors are always open to you, Loka.\"",
            defeatLine = "Vespera vanishes behind a curtain of cyan leaves.",
            actOptions = listOf(
                EnemyActOption("check", "1. Check", "LADY VESPERA — 105 HP. Demands strict royal etiquette before sparing.", 10),
                EnemyActOption(
                    "bow", "2. Courteous Bow",
                    "You bow gracefully. Vespera nods at your manners!", 10,
                    requiredPrevActId = "check",
                    outOfOrderReply = "* Vespera taps her fan! \"Check a lady's stature first before bowing!\""
                ),
                EnemyActOption(
                    "tea", "3. Praise Cyan Trees",
                    "You admire the glowing cyan grove over warm tea. She delights!", 10,
                    requiredPrevActId = "bow",
                    outOfOrderReply = "* Vespera frowns! \"Do not speak of my garden before bowing properly!\""
                )
            )
        ),
        EnemyProfile(
            id = EnemyId.ORIN,
            worldId = WorldId.TRI_RACE_REALM,
            name = "Sentinel Orin",
            raceTitle = "Monster-Angel Construct · Brown Caves",
            difficultyLabel = "OUTLANDS SEAL",
            maxHp = 115,
            attackPower = 4,
            accentColor = Color(0xFF3B82F6),
            xNorm = 0.74f,
            yNorm = 0.56f,
            zone = WorldZone.SOUTH,
            bossBehavior = BossBehavior.WORLD_COMPLETION,
            spareMethodHintEn = "Unlock by finishing ALL Outlands tasks · Order: Check -> Gold Heart -> Tune (+10%).",
            spareMethodHintAr = "يفتح بعد إنهاء كل مهام هذا العالم · الترتيب: فحص -> القلب -> ضبط (+10%).",
            allyBonusTitle = "Orin's Anchor Rune",
            allyBonusDesc = "Reduces Soul ability cooldowns by 20%.",
            introLine = "Orin's carved stone eyes ignite. \"YOU COMPLETED THE REALM TRIALS. NOW CALIBRATE MY CORE.\"",
            attackQuips = listOf(
                "\"GRAVITY SHIFTS INSIDE THE BROWN CAVERN!\"",
                "\"FOLLOW THE HARMONIC CALIBRATION SEQUENCE!\"",
                "\"THE SECRET LABS HIDE ANSWERS ABOUT YOUR PARADOX.\""
            ),
            spareLine = "Orin kneels and binds an ally rune to your necklace! \"Go safely, Loka.\"",
            defeatLine = "Orin crumbles peacefully into dormant stones.",
            actOptions = listOf(
                EnemyActOption("check", "1. Check Core", "SENTINEL ORIN — 115 HP. Ancient guardian calibrated only in strict sequence.", 10),
                EnemyActOption(
                    "pulse", "2. Show Golden Heart",
                    "Your necklace chimes cleanly with Orin's core!", 10,
                    requiredPrevActId = "check",
                    outOfOrderReply = "* Static buzz! \"SCAN CORE INTEGRITY BEFORE PRESENTING THE CHARM.\""
                ),
                EnemyActOption(
                    "tune", "3. Stabilize Crystals",
                    "You align the cavern crystals to peaceful resonance!", 10,
                    requiredPrevActId = "pulse",
                    outOfOrderReply = "* Sparks scatter! \"RESONANCE REJECTED — MATCH GOLD HEART FIRST.\""
                )
            )
        ),
        EnemyProfile(
            id = EnemyId.SERAPHIEL,
            worldId = WorldId.SPLIT_REALM,
            name = "Seraphiel",
            raceTitle = "Angel · Pink-Purple Grove Arbiter",
            difficultyLabel = "SPLIT: ANGELS",
            maxHp = 120,
            attackPower = 4,
            accentColor = Color(0xFF22C55E),
            xNorm = 0.28f,
            yNorm = 0.44f,
            zone = WorldZone.WEST,
            bossBehavior = BossBehavior.IMMEDIATE,
            spareMethodHintEn = "Order: Check -> Stand Calm -> Bridge Peace (+10% each).",
            spareMethodHintAr = "الترتيب: فحص -> صمود هادئ -> جسر السلام (+10% لكل خطوة).",
            allyBonusTitle = "Seraphiel's Green Grace",
            allyBonusDesc = "Start every battle with a free Guard Shield.",
            introLine = "Six wings scatter green leaves over pink-purple grass. \"Prove your patience first!\"",
            attackQuips = listOf(
                "\"Guard with Green! Glide with Purple!\"",
                "\"Calmness must come before promises of peace.\"",
                "\"Loka, can two divided halves ever mend?\""
            ),
            spareLine = "Seraphiel folds their wings and joins your side! \"My feathers will shield you on every world.\"",
            defeatLine = "Seraphiel ascends into the pink-purple canopy.",
            actOptions = listOf(
                EnemyActOption("check", "1. Check Halo", "SERAPHIEL — 120 HP. Tests sincerity in calm three-step order.", 10),
                EnemyActOption(
                    "hope", "2. Show Green Resolve",
                    "You lower your arms calmly. Seraphiel lowers one pair of wings!", 10,
                    requiredPrevActId = "check",
                    outOfOrderReply = "* Seraphiel shakes their head. \"Measure the tension before lowering your shield.\""
                ),
                EnemyActOption(
                    "harmony", "3. Bridge Both Sides",
                    "You vow to unite Angels & Devils without bloodshed. Seraphiel smiles!", 10,
                    requiredPrevActId = "hope",
                    outOfOrderReply = "* \"Empty words! First show calm Resolve before promising harmony.\""
                )
            )
        ),
        EnemyProfile(
            id = EnemyId.MALAKOR,
            worldId = WorldId.SPLIT_REALM,
            name = "Malakor",
            raceTitle = "Devil · Crimson Hunter (Hunts Across Worlds)",
            difficultyLabel = "CHASER BOSS",
            maxHp = 130,
            attackPower = 5,
            accentColor = SoulInfernalViolet,
            xNorm = 0.72f,
            yNorm = 0.44f,
            zone = WorldZone.EAST,
            bossBehavior = BossBehavior.CROSS_WORLD_CHASER,
            spareMethodHintEn = "Stalks Loka across ALL worlds! Any ACT gives +10% Mercy.",
            spareMethodHintAr = "يطاردك في كل العوالم حرفياً! كل ACT يعطي +10% رحمة.",
            allyBonusTitle = "Malakor's Ember Pact",
            allyBonusDesc = "+4 Attack Damage & bonus fight precision.",
            introLine = "Malakor leaps from crimson portals! \"Running won't lose me, Loka! Show me what you're made of!\"",
            attackQuips = listOf(
                "\"Across every realm gate, I caught your trail!\"",
                "\"Your Green Soul shield can't block forever—keep moving!\"",
                "\"Heh, you actually look fearless!\""
            ),
            spareLine = "Malakor laughs and fist-bumps your shoulder! \"No more hunting—count me in as your ally, kid!\"",
            defeatLine = "Malakor steps back into red-ember mist, tipping his horns.",
            actOptions = listOf(
                EnemyActOption("check", "Check", "MALAKOR — 130 HP. Crimson hunter chasing Loka through all realms!", 10),
                EnemyActOption("respect", "Stand Fearless", "You meet his gaze under any sky. Malakor grins!", 10),
                EnemyActOption("truce", "Offer Alliance", "You invite him to join your friends across all worlds.", 10)
            )
        ),
        EnemyProfile(
            id = EnemyId.NORMAL_SLIME,
            worldId = WorldId.NORMAL_WORLD,
            name = "Mossy Puff",
            raceTitle = "Slime Monster · Ordered Calm Method",
            difficultyLabel = "NORMAL MONSTER",
            maxHp = 48,
            attackPower = 2,
            accentColor = Color(0xFF86EFAC),
            xNorm = 0.18f,
            yNorm = 0.42f,
            spareMethodHintEn = "Strict Order: 1.Observe (+15%) -> 2.Cool Dew (+35%) -> 3.Pat Cap (+50%).",
            spareMethodHintAr = "ترتيب إجباري: 1.مراقبة (+15%) -> 2.ندى (+35%) -> 3.ربت (+50%).",
            allyBonusTitle = "Mossy Dew",
            allyBonusDesc = "Cheery park slime befriended by soothing its wobble!",
            introLine = "A bouncy Mossy Puff wobbles nervously! Follow the right calm order to befriend it!",
            attackQuips = listOf(
                "*bloop!* \"Watch how I bounce before touching!\"",
                "*jiggle!* \"Catch these springy dewdrops!\""
            ),
            spareLine = "Mossy Puff settles peacefully and joins your friends!",
            defeatLine = "Mossy Puff dissolves into a puddle of grass dew.",
            actOptions = listOf(
                EnemyActOption("check", "1. Observe Wobble", "MOSSY PUFF — 48 HP. Step 1 done: you learn its nervous rhythm! (+15%)", 15),
                EnemyActOption(
                    "soothe", "2. Offer Cool Dew",
                    "You sprinkle cool dew along its bounce! It slows down happily! (+35%)", 35,
                    requiredPrevActId = "check",
                    outOfOrderReply = "* Sploosh! It got startled because you didn't Observe its wobble first! (+0%)"
                ),
                EnemyActOption(
                    "pet", "3. Pat Leafy Cap",
                    "Gentle final touch! Mossy Puff purrs like a mossy kitten! (+50%)", 50,
                    requiredPrevActId = "soothe",
                    outOfOrderReply = "* Too jittery! Soothe it with Cool Dew before patting its cap! (+0%)"
                )
            ),
            isNormalMonster = true,
            canBefriend = true,
            zone = WorldZone.NORTH
        ),
        EnemyProfile(
            id = EnemyId.FERAL_HOUND,
            worldId = WorldId.NORMAL_WORLD,
            name = "Rift Hound",
            raceTitle = "Wild Hound · Pack Taming Method",
            difficultyLabel = "FIERCE MONSTER",
            maxHp = 58,
            attackPower = 3,
            accentColor = Color(0xFFEF4444),
            xNorm = 0.30f,
            yNorm = 0.58f,
            spareMethodHintEn = "Strict Order: 1.Stand Ground (+15%) -> 2.Toss Ration (+35%) -> 3.Pull Thorn (+50%).",
            spareMethodHintAr = "ترتيب إجباري: 1.ثبات (+15%) -> 2.رمي طعام (+35%) -> 3.نزع الشوكة (+50%).",
            allyBonusTitle = "Tamed Rift Fang",
            allyBonusDesc = "Feral hound calmed by standing tall & pulling its paradox thorn.",
            introLine = "A wounded Rift Hound snarls fiercely! Wait—there's a glowing paradox thorn stuck in its paw!",
            attackQuips = listOf(
                "*GRRR!* \"Stay back unless you mean no harm!\"",
                "*SNAP!* Sharp shadow fangs tear across the box!"
            ),
            spareLine = "The Rift Hound licks your boot gently and trots away as an unlikely friend!",
            defeatLine = "The Rift Hound yelps and scatters back into the bushes.",
            actOptions = listOf(
                EnemyActOption("glare", "1. Stand Ground", "RIFT HOUND — 58 HP. You stand steady without flinching. It watches closely! (+15%)", 15),
                EnemyActOption(
                    "feed", "2. Toss Warm Scrap",
                    "It sniffs the food and lowers its growl! Trust builds! (+35%)", 35,
                    requiredPrevActId = "glare",
                    outOfOrderReply = "* SNAP! It swatted the food away—Stand Ground first to show no fear! (+0%)"
                ),
                EnemyActOption(
                    "thorn", "3. Remove Thorn",
                    "Quickly and gently, you pull the paradox thorn free! The hound wags its tail! (+50%)", 50,
                    requiredPrevActId = "feed",
                    outOfOrderReply = "* Grrr! Never reach for its paw before earning trust with Step 1 & 2! (+0%)"
                )
            ),
            isNormalMonster = true,
            canBefriend = true,
            zone = WorldZone.WEST
        ),
        EnemyProfile(
            id = EnemyId.SAND_MOTH,
            worldId = WorldId.TRI_RACE_REALM,
            name = "Cyan Moth",
            raceTitle = "Winged Monster · Light Dance Method",
            difficultyLabel = "NORMAL MONSTER",
            maxHp = 56,
            attackPower = 3,
            accentColor = SoulSeraphCyan,
            xNorm = 0.28f,
            yNorm = 0.72f,
            spareMethodHintEn = "Strict Order: 1.Track Dust (+20%) -> 2.Hum Beat (+30%) -> 3.Shine Necklace (+50%).",
            spareMethodHintAr = "ترتيب إجباري: 1.تتبع الغبار (+20%) -> 2.همهمة (+30%) -> 3.إظهار القلادة (+50%).",
            allyBonusTitle = "Luminous Dust",
            allyBonusDesc = "Gentle cyan-tree moth calmed by rhythmic resonance.",
            introLine = "A glowing Cyan Moth darts around Loka! Match its wing rhythm before showing your bright necklace!",
            attackQuips = listOf(
                "*flutter!* \"Sudden bright light scares my eyes!\"",
                "*buzz!* Glowing cyan dust drifts across the arena!"
            ),
            spareLine = "Cyan Moth perches gently on your shoulder as a new friend!",
            defeatLine = "Cyan scales drift harmlessly into the beige breeze.",
            actOptions = listOf(
                EnemyActOption("check", "1. Track Wing Beat", "CYAN MOTH — 56 HP. You count its soft 4/4 wing rhythm. (+20%)", 20),
                EnemyActOption(
                    "hum", "2. Hum Matching Beat",
                    "You hum along cleanly—the moth slows down and hovers peacefully! (+30%)", 30,
                    requiredPrevActId = "check",
                    outOfOrderReply = "* Buzz! Your humming clashed—Track its Wing Beat first! (+0%)"
                ),
                EnemyActOption(
                    "shine", "3. Show Gold Heart",
                    "Warm golden light reflects off calm cyan wings! Total trust! (+50%)", 50,
                    requiredPrevActId = "hum",
                    outOfOrderReply = "* Too bright! Sudden light startled it—match its rhythm first! (+0%)"
                )
            ),
            isNormalMonster = true,
            canBefriend = true,
            zone = WorldZone.NORTH
        ),
        EnemyProfile(
            id = EnemyId.IRON_STALKER,
            worldId = WorldId.TRI_RACE_REALM,
            name = "Chrono Stalker",
            raceTitle = "Automaton · Signal Reboot Method",
            difficultyLabel = "TECH MONSTER",
            maxHp = 70,
            attackPower = 4,
            accentColor = Color(0xFFF97316),
            xNorm = 0.26f,
            yNorm = 0.72f,
            spareMethodHintEn = "Strict Order: 1.Scan Lock (+25%) -> 2.Invert Ping (+25%) -> 3.Override Peace (+50%).",
            spareMethodHintAr = "ترتيب إجباري: 1.فحص القفل (+25%) -> 2.عكس الإشارة (+25%) -> 3.برمجة السلام (+50%).",
            allyBonusTitle = "Friendly Automaton",
            allyBonusDesc = "Overrode rogue hunter firmware with peaceful protocol.",
            introLine = "A rusted Chrono Stalker locks onto your Soul! Hack its protocol in exact boot order!",
            attackQuips = listOf(
                "\"TARGET LOCKED. REBOOT SEQUENCE INCOMPLETE.\"",
                "\"DETECTING MANUAL FREQUENCY OVERRIDE...\""
            ),
            spareLine = "Its optics switch from red to calm green: \"PROTOCOL: PROTECT LOKA.\"",
            defeatLine = "Sparks erupt as the Chrono Stalker powers down in the sand.",
            actOptions = listOf(
                EnemyActOption("scan", "1. Scan Boot Port", "CHRONO STALKER — 70 HP. Port found at neck hinge! (+25%)", 25),
                EnemyActOption(
                    "invert", "2. Invert Radar Signal",
                    "You mirror its radar ping with your necklace chime! Weapons stall! (+25%)", 25,
                    requiredPrevActId = "scan",
                    outOfOrderReply = "* ACCESS DENIED! Scan its Boot Port before inverting signal! (+0%)"
                ),
                EnemyActOption(
                    "override", "3. Upload Peace Key",
                    "Firewall bypassed! Its rogue hunter drive switches off peacefully! (+50%)", 50,
                    requiredPrevActId = "invert",
                    outOfOrderReply = "* SECURITY LOCKOUT! Invert the radar signal before uploading key! (+0%)"
                )
            ),
            isNormalMonster = true,
            canBefriend = true,
            zone = WorldZone.WEST
        ),
        EnemyProfile(
            id = EnemyId.HALO_SCRIBE,
            worldId = WorldId.SPLIT_REALM,
            name = "Choir Wisp",
            raceTitle = "Musical Spirit · 4-Beat Choir Method",
            difficultyLabel = "NORMAL MONSTER",
            maxHp = 60,
            attackPower = 3,
            accentColor = Color(0xFFF472B6),
            xNorm = 0.64f,
            yNorm = 0.66f,
            spareMethodHintEn = "4-Step Varied %: Notes (+15%) -> Clap (+25%) -> Sing (+35%) -> Bow (+25%).",
            spareMethodHintAr = "4 خطوات بنسب مختلفة: نوتة (+15%) -> تصفيق (+25%) -> غناء (+35%) -> تحية (+25%).",
            allyBonusTitle = "Choir Bell",
            allyBonusDesc = "Playful pink-grove spirit befriended with 4-beat harmony.",
            introLine = "A tiny Choir Wisp floats with floating sheet music—perform the 4-part duet!",
            attackQuips = listOf(
                "♪ \"Sing the four measures cleanly, traveler!\" ♪",
                "♪ \"Watch the finale timing!\" ♪"
            ),
            spareLine = "Choir Wisp chimes happily and becomes your friend!",
            defeatLine = "Its tiny halo bell fades into shimmering pink mist.",
            actOptions = listOf(
                EnemyActOption("check", "1. Read Halo Notes", "CHOIR WISP — 60 HP. Sheet music reads: Clap -> Sing -> Bow! (+15%)", 15),
                EnemyActOption(
                    "clap", "2. Clap on Beat",
                    "Clap-clap! The tiny bells chime in sync with your hands! (+25%)", 25,
                    requiredPrevActId = "check",
                    outOfOrderReply = "* Off-beat! Read the Halo Notes before clapping! (+0%)"
                ),
                EnemyActOption(
                    "hum", "3. Sing Counter-Melody",
                    "Your voices harmonize across the pink-purple air! (+35%)", 35,
                    requiredPrevActId = "clap",
                    outOfOrderReply = "* Wait for the Clap beat cue first! (+0%)"
                ),
                EnemyActOption(
                    "bow", "4. Quiet Curtain Bow",
                    "A graceful finale! The Choir Wisp applauds warmly! (+25%)", 25,
                    requiredPrevActId = "hum",
                    outOfOrderReply = "* Wait! Don't bow before finishing the Counter-Melody! (+0%)"
                )
            ),
            isNormalMonster = true,
            canBefriend = true,
            zone = WorldZone.WEST
        ),
        EnemyProfile(
            id = EnemyId.ASH_IMP,
            worldId = WorldId.SPLIT_REALM,
            name = "Cinder Imp",
            raceTitle = "Devil-Kin · Trickster Trade Method",
            difficultyLabel = "TRICKY MONSTER",
            maxHp = 74,
            attackPower = 4,
            accentColor = SoulParadoxCrimson,
            xNorm = 0.32f,
            yNorm = 0.68f,
            spareMethodHintEn = "No fixed order for Joke (+30%) & Dare (+30%), then Finish with Trade (+40%)!",
            spareMethodHintAr = "بادر بالضحك (+30%) أو التحدي (+30%)، ثم اختم بالمقايضة (+40%)!",
            allyBonusTitle = "Imp's Lucky Coal",
            allyBonusDesc = "Befriended by matching its humor before trading.",
            introLine = "A cackling Cinder Imp juggles red embers! Laugh with it or win a stare-down, then trade!",
            attackQuips = listOf(
                "\"Hehehe! Don't be boring—play along!\"",
                "\"Trade with me AFTER you make me laugh!\""
            ),
            spareLine = "The Cinder Imp pockets the shiny pebble and winks as your new friend!",
            defeatLine = "The Cinder Imp puffs into soot and dashes back behind a red tree.",
            actOptions = listOf(
                EnemyActOption("joke", "Tell Silly Joke", "Hehehe! The Cinder Imp drops two fireballs laughing! (+30%)", 30),
                EnemyActOption("dare", "Out-Stare Imp", "You blink last! The imp cheers loudly for your nerve! (+30%)", 30),
                EnemyActOption(
                    "trade", "Trade Cool Pebble",
                    "Satisfied with the fun, the imp gladly accepts the trade! (+40%)", 40,
                    requiredPrevActId = "joke",
                    outOfOrderReply = "* \"Boring! Make me laugh with a Joke first before offering trades!\" (+0%)"
                )
            ),
            isNormalMonster = true,
            canBefriend = true,
            zone = WorldZone.EAST
        ),
        EnemyProfile(
            id = EnemyId.GOV_MONSTER_CHIEF,
            worldId = WorldId.GOVERNMENT_HQ,
            name = "Marshal Krog",
            raceTitle = "Monster Chief · Government High Command (Only Gov Friend)",
            difficultyLabel = "GOVERNMENT HQ",
            maxHp = 140,
            attackPower = 5,
            accentColor = Color(0xFF4ADE80),
            xNorm = 0.68f,
            yNorm = 0.48f,
            zone = WorldZone.EAST,
            bossBehavior = BossBehavior.IMMEDIATE,
            spareMethodHintEn = "Battle anytime! Only friendable Gov leader (+10% per ACT).",
            spareMethodHintAr = "قتال فوري! القائد الوحيد في الحكومة القابل للصداقة (+10% لكل ACT).",
            allyBonusTitle = "Monster Chief's Oath",
            allyBonusDesc = "+3 Max HP & +3 Attack Damage across every battle.",
            introLine = "Heavy armor clanks across the steel floor. \"Loka?! Wait—you carry that golden heart?\"",
            attackQuips = listOf(
                "\"I swore to protect the kids—prove your heart isn't twisted!\"",
                "\"Stand tall, kid! Show me the truth!\"",
                "\"The Angel Leader told me someone pure was coming...\""
            ),
            spareLine = "Marshal Krog salutes and joins you! \"I'm done taking orders from corrupt tyrants. I'm your friend now, Loka!\"",
            defeatLine = "Marshal Krog drops to one knee... the hall falls dead silent.",
            actOptions = listOf(
                EnemyActOption("check", "Check", "MARSHAL KROG — 140 HP. The ONLY leader in The Government who can become your true friend!", 10),
                EnemyActOption("necklace", "Show Golden Heart", "You show your necklace & mention the Angel Leader's message. Krog lowers his halberd!", 10),
                EnemyActOption("kids", "Mention Rescued Kids", "You tell him the kids are still out there. Krog agrees to stand with you!", 10)
            ),
            isNormalMonster = false,
            canBefriend = true,
            canStopWithoutKilling = true
        ),
        EnemyProfile(
            id = EnemyId.GOV_DIRECTOR,
            worldId = WorldId.GOVERNMENT_HQ,
            name = "Chancellor Vance",
            raceTitle = "Supreme Government Head · Corrupt Authority (No Friendship)",
            difficultyLabel = "GOVERNMENT SEAL",
            maxHp = 155,
            attackPower = 5,
            accentColor = Color(0xFFEF4444),
            xNorm = 0.50f,
            yNorm = 0.34f,
            zone = WorldZone.NORTH,
            bossBehavior = BossBehavior.WORLD_COMPLETION,
            spareMethodHintEn = "Finish ALL Gov tasks first · Order: Check -> Expose -> Disarm (+10% Stop).",
            spareMethodHintAr = "أنهِ كل مهام الحكومة أولاً · الترتيب: فحص -> فضح -> تعطيل (+10% إيقاف).",
            allyBonusTitle = "Corrupt Authority",
            allyBonusDesc = "Refuses friendship—must be stopped by Force or Disarm!",
            introLine = "\"So you unlocked the Citadel Seal and walked in! No treaties, no friendship—only order!\"",
            attackQuips = listOf(
                "\"Paradox seals active! You cannot alter our command!\"",
                "\"Friends? Monsters are only tools for order!\"",
                "\"Stop me if you can, Loka!\""
            ),
            spareLine = "You shut down Vance's Paradox Lock using the Angel Leader's files! Chancellor Vance is stopped and arrested WITHOUT killing!",
            defeatLine = "Chancellor Vance crashes onto the council seal, defeated for good.",
            actOptions = listOf(
                EnemyActOption("check", "1. Check Lock", "CHANCELLOR VANCE — 155 HP. Must Expose evidence before locking his console! (+10% Stop)", 10),
                EnemyActOption(
                    "expose", "2. Broadcast Angel Proof",
                    "You release Angel Leader Cassiel's proof! Vance's authority barrier cracks! (+10% Stop)", 10,
                    requiredPrevActId = "check",
                    outOfOrderReply = "* Barrier holds! Check the Paradox Lock encryption first!"
                ),
                EnemyActOption(
                    "disarm", "3. Lock Paradox Console",
                    "You override his Citadel lock without shedding blood! (+10% Stop)", 10,
                    requiredPrevActId = "expose",
                    outOfOrderReply = "* Console locked! Broadcast the Angel Proof first to drop his shield!"
                )
            ),
            isNormalMonster = false,
            canBefriend = false,
            canStopWithoutKilling = true
        ),
        EnemyProfile(
            id = EnemyId.ECHO_LOKA,
            worldId = WorldId.GLITCHED_WORLD,
            name = "Echo-Loka",
            raceTitle = "Final Anomaly · Glitched Void",
            difficultyLabel = "FINAL ENEMY",
            maxHp = 12,
            attackPower = 3,
            accentColor = GlitchCyan,
            xNorm = 0.5f,
            yNorm = 0.35f,
            bossBehavior = BossBehavior.WORLD_COMPLETION,
            spareMethodHintEn = "Unlocks after full journey · Order: Check -> Golden Heart -> Remember (+10%).",
            spareMethodHintAr = "يفتح بعد إنهاء الرحلة · الترتيب: فحص -> القلب الذهبي -> تذكّر (+10%).",
            allyBonusTitle = "Echo's Stabilized Shard",
            allyBonusDesc = "+3 Attack Damage & immune to paradox stutter.",
            introLine = "Your final reflection steps forward in the void... \"You finished the whole journey... now face yourself, Loka.\"",
            attackQuips = listOf(
                "\"YOUR WHITE SOUL IS SICK HERE... ONLY 1 DAMAGE...\"",
                "\"DOUBLE PAIN... FINAL TEST OF YOUR HEART...\"",
                "\"I AM THE ECHO THAT NEVER MADE IT BACK.\""
            ),
            spareLine = "Echo-Loka's static softens into warm gold. \"...thank you for remembering me, Loka.\"",
            defeatLine = "Echo-Loka shatters into eight silent white pixels.",
            actOptions = listOf(
                EnemyActOption("check", "1. Check Static", "ECHO-LOKA — 12 HP. Sick White Soul deals 1 DMG & takes 2x DMG! (+10%)", 10),
                EnemyActOption(
                    "embrace", "2. Hold Golden Heart",
                    "You touch your golden heart necklace. The static calms! (+10%)", 10,
                    requiredPrevActId = "check",
                    outOfOrderReply = "* The static recoils! Check your echo's heartbeat first!"
                ),
                EnemyActOption(
                    "remember", "3. Promise Return",
                    "You promise you won't leave any lost kids behind. (+10%)", 10,
                    requiredPrevActId = "embrace",
                    outOfOrderReply = "* \"Don't make blind vows... hold our Golden Heart first!\""
                )
            ),
            isNormalMonster = false,
            canBefriend = true
        ),
        EnemyProfile(
            id = EnemyId.FUTURE_LOKA,
            worldId = WorldId.NORMAL_WORLD,
            name = "Future-Loka",
            raceTitle = "Timeline Arbiter · Your Older Self",
            difficultyLabel = "FINAL ROUTE",
            maxHp = 160,
            attackPower = 5,
            accentColor = SepiaAmber,
            xNorm = 0.5f,
            yNorm = 0.25f,
            bossBehavior = BossBehavior.IMMEDIATE,
            spareMethodHintEn = "Timeline Arbiter (+10% Mercy per ACT).",
            spareMethodHintAr = "حكم الخط الزمني (+10% رحمة لكل ACT).",
            allyBonusTitle = "Future-Loka's Mantle",
            allyBonusDesc = "Full control of timelines & +6 Attack Power.",
            introLine = "Your older self steps out of golden-cyan static. \"I tried to stop this, Loka. Put the knife down.\"",
            attackQuips = listOf(
                "\"Every timeline you burn leaves nothing left to save!\"",
                "\"I remember wearing that grey shirt and golden heart...\"",
                "\"If you defeat me, you take my power—and doom this world!\""
            ),
            spareLine = "Future-Loka smiles gently through the rift. \"You chose mercy after all...\"",
            defeatLine = "Future-Loka falls to one knee. Their timeline power surges into Loka's hands...",
            actOptions = listOf(
                EnemyActOption("check", "Check", "FUTURE-LOKA — 160 HP. Your future self carrying the weight of every lost timeline.", 10),
                EnemyActOption("listen", "Hear Their Plea", "You pause to hear their warning about destroying the world.", 10)
            ),
            isNormalMonster = false,
            canBefriend = true
        )
    )

    fun byId(id: EnemyId): EnemyProfile = all.first { it.id == id }
    fun forWorld(worldId: WorldId): List<EnemyProfile> = all.filter {
        it.worldId == worldId && !it.isNormalMonster && it.id != EnemyId.FUTURE_LOKA && it.id != EnemyId.RUINS_DUMMY
    }
    fun normalMonstersForWorld(worldId: WorldId): List<EnemyProfile> = all.filter {
        it.worldId == worldId && it.isNormalMonster
    }
    fun standardPlayableRoster(): List<EnemyProfile> = all.filter {
        !it.isNormalMonster && it.id != EnemyId.ECHO_LOKA && it.id != EnemyId.FUTURE_LOKA && it.id != EnemyId.RUINS_DUMMY
    }
}

object WorldContentCatalog {
    val landmarks: List<WorldLandmark> = listOf(
        // Normal World (spread across 5 doors & Sanctum)
        WorldLandmark("nw_hills", WorldId.NORMAL_WORLD, LandmarkKind.HILLS, "Emerald Overlook Hills", 0.25f, 0.30f,
            "Mission: Lost Kite", "Retrieve Maya's kite caught on the hilltop wind.", false, rewardArmorId = "hill_cloak", rewardLabel = "Armor: Greenhill Cloak", zone = WorldZone.NORTH),
        WorldLandmark("nw_cave", WorldId.NORMAL_WORLD, LandmarkKind.CAVE, "Subway Hollow Cave", 0.24f, 0.70f,
            "Event: Glowing Stones", "Help Bramble clear fallen boulders blocking the tunnel.", false, rewardItemId = "chrono_shard", rewardLabel = "+2 Chrono Shards", zone = WorldZone.SOUTH),
        WorldLandmark("nw_mansion", WorldId.NORMAL_WORLD, LandmarkKind.MANSION, "Mayor's Old Mansion", 0.72f, 0.34f,
            "Mission: Peace Ledger", "Deliver the Four-Race Treaty page from the parlor.", false, rewardItemId = "sepia_ration", rewardWeaponId = "chrono_lance", rewardLabel = "Weapon: Chrono Lance + 2 Rations", zone = WorldZone.EAST),
        WorldLandmark("nw_lab", WorldId.NORMAL_WORLD, LandmarkKind.LAB, "Gov. Chrono Lab #4", 0.50f, 0.34f,
            "Secret Quest: Save #8 Tape", "Shocking discovery! Dr. Kestrel's tape reveals your 10-minute warp happens every 8 Ashen saves!", true, rewardItemId = "save_anchor", rewardLabel = "+2 Save Anchors", zone = WorldZone.SANCTUM),

        // Tri-Race Realm (Beige ground, Cyan trees, Brown caves across 5 doors & Sanctum)
        WorldLandmark("tr_hills", WorldId.TRI_RACE_REALM, LandmarkKind.HILLS, "Beige Ridge Hills", 0.24f, 0.32f,
            "Event: Cyan Sapling", "Water three flickering cyan saplings on the beige ridge.", false, rewardArmorId = "cyan_bark_vest", rewardLabel = "Armor: Cyan Bark Mail", zone = WorldZone.NORTH),
        WorldLandmark("tr_cave", WorldId.TRI_RACE_REALM, LandmarkKind.CAVE, "Amber-Brown Cavern", 0.24f, 0.68f,
            "Mission: Echo Crystals", "Align the humming crystals inside the brown cave.", false, rewardItemId = "chrono_shard", rewardWeaponId = "paradox_wrench", rewardLabel = "Weapon: Paradox Wrench + 2 Shards", zone = WorldZone.SOUTH),
        WorldLandmark("tr_mansion", WorldId.TRI_RACE_REALM, LandmarkKind.MANSION, "Vespera's Sand Mansion", 0.74f, 0.34f,
            "Event: Midnight Tea", "Join Lady Vespera's monster-angel-devil tea salon.", false, rewardItemId = "sepia_ration", rewardLabel = "+2 Sepia Rations", zone = WorldZone.EAST),
        WorldLandmark("tr_lab", WorldId.TRI_RACE_REALM, LandmarkKind.LAB, "Outlands Drift Lab", 0.50f, 0.34f,
            "Secret Quest: Yellow-Blue Core", "Calibrate the dual-soul capacitor and uncover another Lost Tape about your saves.", true, rewardItemId = "save_anchor", rewardLabel = "+2 Save Anchors", zone = WorldZone.SANCTUM),

        // Split Realm (Angels: purple-pink ground + green trees; Devils: brown-grey ground + red trees)
        WorldLandmark("sr_hills", WorldId.SPLIT_REALM, LandmarkKind.HILLS, "Halo Crest Hills", 0.24f, 0.34f,
            "Mission: Fallen Feather", "Return a luminous green leaf & feather to the Angels' shrine.", false, rewardItemId = "sepia_ration", rewardWeaponId = "seraph_blade", rewardLabel = "Weapon: Seraph Blade + 2 Rations", zone = WorldZone.NORTH),
        WorldLandmark("sr_mansion", WorldId.SPLIT_REALM, LandmarkKind.MANSION, "Pink-Silk Angel Mansion", 0.50f, 0.34f,
            "Secret Quest: Choir Loom", "Weave purple-pink threads and decode the hidden Lost Tape inside Aurelia's sanctuary.", true, rewardArmorId = "seraph_silk", rewardLabel = "Armor: Pink-Silk Halo Mantle", zone = WorldZone.SANCTUM),
        WorldLandmark("sr_cave", WorldId.SPLIT_REALM, LandmarkKind.CAVE, "Ash-Brown Devil Cave", 0.74f, 0.64f,
            "Mission: Ember Forge", "Cool Kaelith's forge beneath the red-leaved trees.", false, rewardArmorId = "rift_plate", rewardLabel = "Armor: Ash-Iron Devil Guard", zone = WorldZone.SOUTH),
        WorldLandmark("sr_lab", WorldId.SPLIT_REALM, LandmarkKind.LAB, "Borderline Rift Lab", 0.70f, 0.76f,
            "Event: Truce Transmitter", "Link the Angel and Devil power grids across the divide.", false, rewardItemId = "save_anchor", rewardLabel = "+2 Save Anchors", zone = WorldZone.EAST),

        // The Government HQ
        WorldLandmark("gov_vault", WorldId.GOVERNMENT_HQ, LandmarkKind.LAB, "Council Command Vault", 0.50f, 0.34f,
            "Mission: Citadel Override", "Disable the Government's surveillance array from Door V.", false, rewardItemId = "save_anchor", rewardWeaponId = "chrono_lance", rewardLabel = "Weapon: Chrono Lance + 2 Anchors", zone = WorldZone.SANCTUM)
    )

    val npcs: List<WorldNpc> = listOf(
        WorldNpc("npc_maya", WorldId.NORMAL_WORLD, "Maya", "Human", 0.34f, 0.25f, Color(0xFFF59E0B),
            "\"Hey Loka! All the worlds are connected through the North, East, and West gates now! Head North toward The Government when you're ready!\"",
            "Neighborhood Gift", "sepia_ration", "Maya handed you +1 Sepia Ration!", zone = WorldZone.CENTER),
        WorldNpc("npc_pip", WorldId.NORMAL_WORLD, "Pip", "Monster", 0.42f, 0.30f, Color(0xFF4ADE80),
            "\"My big brother says Humans, Monsters, Angels, and Devils used to hang out before the paradox war!\"", zone = WorldZone.SOUTH),
        WorldNpc("npc_kestrel", WorldId.NORMAL_WORLD, "Dr. Kestrel", "Angel-Human", 0.48f, 0.30f, SoulSeraphCyan,
            "\"Heard the Angel Leader inside The Government wants to help us! Beware though: your 8th Save with Ashen still pulls you into the Glitched Void!\"", zone = WorldZone.WEST),

        WorldNpc("npc_lyra", WorldId.TRI_RACE_REALM, "Lyra", "Angel Scout", 0.35f, 0.28f, SoulSeraphCyan,
            "\"From here: West returns to Normal World, East crosses to Split Realm, and North marches straight to The Government!\"", zone = WorldZone.CENTER),
        WorldNpc("npc_zor", WorldId.TRI_RACE_REALM, "Zor", "Devil Merchant", 0.46f, 0.26f, SoulInfernalViolet,
            "\"Beige sand and cyan trees look strange, huh? Take this shard for the brown caves!\"",
            "Zor's Bargain", "chrono_shard", "Zor tossed you +1 Chrono Shard!", zone = WorldZone.SOUTH),

        WorldNpc("npc_aurelia", WorldId.SPLIT_REALM, "Aurelia", "Angel", 0.32f, 0.56f, Color(0xFFEC4899),
            "\"West leads deep into the Angels' pink-purple sanctuary, and North leads up toward The Government Citadel!\"", zone = WorldZone.CENTER),
        WorldNpc("npc_kaelith", WorldId.SPLIT_REALM, "Kaelith", "Devil", 0.68f, 0.56f, Color(0xFFEF4444),
            "\"East connects back to Normal World. Tell the Angel Leader in The Government we won't bow to Chancellor Vance!\"",
            "Red-Tree Ration", "chrono_shard", "Kaelith gave you +1 Chrono Shard!", zone = WorldZone.CENTER),

        // Inside The Government: Angel Leader helps Loka!
        WorldNpc("npc_angel_leader", WorldId.GOVERNMENT_HQ, "Lord Cassiel", "Angel Leader · Ally", 0.50f, 0.62f, SoulSeraphCyan,
            "\"Welcome to The Government, Loka! As Angel Leader, I stand with you! Take my Seal & Supplies. Remember: ONLY Marshal Krog (Monster Chief, East) can become your friend. Chancellor Vance (North) refuses friendship—stop him peacefully via ACT+STOP or defeat him by force!\"",
            "Angel Leader's Aid", "sepia_ration", "Lord Cassiel gifted Angel Leader's Seal (+3 DEF, +8 HP) & supplies!", zone = WorldZone.CENTER)
    )

    val lostKids: List<LostKid> = listOf(
        LostKid(
            id = "kid_sora",
            worldId = WorldId.TRI_RACE_REALM,
            name = "Sora",
            nameAr = "سورا",
            xNorm = 0.68f,
            yNorm = 0.36f,
            accentColor = Color(0xFFF472B6),
            pleaEn = "\"Loka?! I fell into this cyan forest! Please help me stay safe until we warp back to the Normal World!\"",
            pleaAr = "\"لوكا؟! سقطت في هذه الغابة السماوية! ساعدني وخذني معك حتى نصل إلى العالم الطبيعي فقط!\"",
            thanksEn = "\"I'll stay with you until we reach the Normal World—boosting your Max HP (+5) and healing you in battle!\"",
            thanksAr = "\"سأبقى معك حتى توصلني للعالم الطبيعي فقط—سأرفع دمك (+5 HP) وأعالجك في القتال!\"",
            buffEn = "+5 Max HP & Battle Heal (+6 HP) while escorted",
            buffAr = "+5 دم أقصى وعلاج (+6 HP) حتى الوصول للعالم الطبيعي",
            hpBonus = 5,
            healAmount = 6,
            zone = WorldZone.NORTH
        ),
        LostKid(
            id = "kid_taro",
            worldId = WorldId.TRI_RACE_REALM,
            name = "Taro",
            nameAr = "تارو",
            xNorm = 0.34f,
            yNorm = 0.40f,
            accentColor = Color(0xFF38BDF8),
            pleaEn = "\"I got pulled away from the other kids... this beige desert wind is freezing!\"",
            pleaAr = "\"انفصلت عن باقي الأطفال... رياح هذه الصحراء البيج باردة جداً!\"",
            thanksEn = "\"You helped me breathe again! Take this Cyan Shield weave: +2 Defense & +2 HP!\"",
            thanksAr = "\"ساعدتني على الهدوء! خذ هذا الدرع السماوي: +2 دفاع و+2 دم أقصى!\"",
            buffEn = "+2 Defense & +2 Max HP",
            buffAr = "+2 دفاع و+2 دم أقصى",
            hpBonus = 2,
            defBonus = 2,
            zone = WorldZone.WEST
        ),
        LostKid(
            id = "kid_nora",
            worldId = WorldId.SPLIT_REALM,
            name = "Nora",
            nameAr = "نورا",
            xNorm = 0.34f,
            yNorm = 0.66f,
            accentColor = Color(0xFFFACC15),
            pleaEn = "\"I'm trapped between the pink-purple grass and the border! Help me break the seal!\"",
            pleaAr = "\"أنا عالقة عند عشب الملائكة الوردي-البنفسجي! ساعدني في كسر الختم!\"",
            thanksEn = "\"I'm free! My light will power up your strikes: +4 Attack & Battle Heal (+4 HP)!\"",
            thanksAr = "\"تحررت! سأزيد قوة ضرباتك (+4 هجوم) وأعالجك في القتال (+4 HP)!\"",
            buffEn = "+4 Attack Power & +4 HP Heal",
            buffAr = "+4 قوة هجوم وعلاج (+4 HP)",
            atkBonus = 4,
            healAmount = 4,
            zone = WorldZone.NORTH
        ),
        LostKid(
            id = "kid_zayn",
            worldId = WorldId.SPLIT_REALM,
            name = "Zayn",
            nameAr = "زين",
            xNorm = 0.60f,
            yNorm = 0.32f,
            accentColor = Color(0xFF4ADE80),
            pleaEn = "\"My paradox boots froze on the Devils' grey-brown ash! Give me a pull, Loka!\"",
            pleaAr = "\"حذائي الزمني تجمد فوق تراب الشياطين البني-الرمادي! اسحبني يا لوكا!\"",
            thanksEn = "\"Awesome! I tuned your boots & Soul: +28% Faster Speed & +2 Attack!\"",
            thanksAr = "\"ممتاز! عدّلت سرعتك: أصبحت أسرع بنسبة 28% مع +2 هجوم!\"",
            buffEn = "+28% Speed & +2 Attack",
            buffAr = "+28% سرعة حركة و+2 هجوم",
            atkBonus = 2,
            speedBonus = 0.28f,
            zone = WorldZone.SOUTH
        )
    )

    val glitchedMemories: List<GlitchedMemory> = listOf(
        GlitchedMemory(
            id = 1,
            tagEn = "MEMORY FRAGMENT // BEFORE THE WAR",
            tagAr = "شظية ذاكرة // قبل الحرب",
            textEn = "Warm sunlight on your grey shirt... Someone clasped the golden heart necklace around you and whispered: 'Never forget who you are.'",
            textAr = "شمس دافئة فوق قميصك الرمادي... أحدهم علّق قلادة القلب الذهبي حول عنقك وهمس: «لا تنسَ أبداً من أنت.»"
        ),
        GlitchedMemory(
            id = 2,
            tagEn = "MEMORY FRAGMENT // MINUTE 10:00",
            tagAr = "شظية ذاكرة // الدقيقة 10:00",
            textEn = "The kitchen clock struck ten minutes. Your hands turned translucent—and the floor dissolved into silent static.",
            textAr = "توقفت الساعة عند عشر دقائق. تحولت يداك إلى ضوء متقطع—واختفت الأرض من تحت قدميك لأول مرة."
        ),
        GlitchedMemory(
            id = 3,
            tagEn = "MEMORY FRAGMENT // SIRENS IN THE NIGHT",
            tagAr = "شظية ذاكرة // صفارات الإنذار",
            textEn = "'He vanished right out of the Government sector!' Boots echoed down the alley while Loka hid behind rusted bins.",
            textAr = "«لقد اختفى أمام أعيننا من قطاع الحكومة!» دوّت خطوات الجنود في الزقاق بينما اختبأ لوكا مرتجفاً."
        ),
        GlitchedMemory(
            id = 4,
            tagEn = "MEMORY FRAGMENT // OTHER FOOTSTEPS",
            tagAr = "شظية ذاكرة // خطوات أطفال آخرين",
            textEn = "Inside a narrow crack between worlds, three small hands reached out for yours before time ripped you apart.",
            textAr = "داخل شق ضيق بين العوالم، امتدت أيدي أطفال صغار نحوك قبل أن يمزقكم الزمن إلى عوالم متفرقة."
        ),
        GlitchedMemory(
            id = 5,
            tagEn = "MEMORY FRAGMENT // CORRUPTED ECHO",
            tagAr = "شظية ذاكرة // همس مشوّه",
            textEn = "A faint whisper echoes through the black void: '...Loka... don't trust the silence... keep counting...'",
            textAr = "همس خافت يتردد في الفراغ الأسود: «...لوكا... لا تثق بالصمت... استمر في الصمود...»"
        )
    )

    val chests: List<WorldChest> = listOf(
        WorldChest("chest_nw_dot", WorldId.NORMAL_WORLD, ChestStyle.SMALL_DOT, "Park Supply Box", "صندوق الحديقة الصغير", 0.68f, 0.72f, "chrono_shard", 1, zone = WorldZone.CENTER),
        WorldChest("chest_nw_star", WorldId.NORMAL_WORLD, ChestStyle.SQUARE_STAR, "Fountain Plaza Crate", "صندوق الساحة المرصع", 0.70f, 0.70f, "chrono_shard", 2, zone = WorldZone.WEST),
        WorldChest("chest_nw_lock", WorldId.NORMAL_WORLD, ChestStyle.WIDE_PADLOCK, "Sealed Street Chest", "صندوق الشارع المقفل", 0.64f, 0.74f, "sepia_ration", 2, zone = WorldZone.EAST),
        WorldChest("chest_nw_sanctum", WorldId.NORMAL_WORLD, ChestStyle.WIDE_PADLOCK, "Secret Lab Strongbox", "خزنة المختبر السري", 0.26f, 0.68f, "save_anchor", 1, zone = WorldZone.SANCTUM),

        WorldChest("chest_tr_star", WorldId.TRI_RACE_REALM, ChestStyle.SQUARE_STAR, "Star-Clasp Crate", "صندوق النجمة السماوي", 0.68f, 0.72f, "save_anchor", 1, zone = WorldZone.CENTER),
        WorldChest("chest_tr_lock", WorldId.TRI_RACE_REALM, ChestStyle.WIDE_PADLOCK, "Outlands Vault Chest", "صندوق البراري المقفل", 0.70f, 0.72f, "sepia_ration", 2, zone = WorldZone.WEST),
        WorldChest("chest_tr_east", WorldId.TRI_RACE_REALM, ChestStyle.SMALL_DOT, "Cyan Courtyard Box", "صندوق البلاط السماوي", 0.28f, 0.28f, "chrono_shard", 2, zone = WorldZone.EAST),
        WorldChest("chest_tr_sanctum", WorldId.TRI_RACE_REALM, ChestStyle.SQUARE_STAR, "Drift Vault Box", "صندوق القبو الزمني", 0.74f, 0.68f, "save_anchor", 1, zone = WorldZone.SANCTUM),

        WorldChest("chest_sr_dot", WorldId.SPLIT_REALM, ChestStyle.SMALL_DOT, "Angel Grove Chest", "صندوق الملائكة الصغير", 0.72f, 0.34f, "save_anchor", 2, zone = WorldZone.NORTH),
        WorldChest("chest_sr_star", WorldId.SPLIT_REALM, ChestStyle.SQUARE_STAR, "Ash-Border Star Chest", "صندوق النجمة المشتعل", 0.28f, 0.68f, "chrono_shard", 2, zone = WorldZone.SOUTH),
        WorldChest("chest_sr_west", WorldId.SPLIT_REALM, ChestStyle.SMALL_DOT, "Seraph Garden Box", "صندوق الحديقة الوردية", 0.56f, 0.24f, "sepia_ration", 2, zone = WorldZone.WEST),
        WorldChest("chest_sr_sanctum", WorldId.SPLIT_REALM, ChestStyle.WIDE_PADLOCK, "Sanctuary Silk Chest", "صندوق الحرير السري", 0.26f, 0.68f, "save_anchor", 1, zone = WorldZone.SANCTUM),

        WorldChest("chest_gov_center", WorldId.GOVERNMENT_HQ, ChestStyle.SQUARE_STAR, "Citadel Armory Box", "صندوق ترسانة الحكومة", 0.72f, 0.70f, "sepia_ration", 2, zone = WorldZone.CENTER),
        WorldChest("chest_gov_west", WorldId.GOVERNMENT_HQ, ChestStyle.WIDE_PADLOCK, "Angel Locker Supply", "خزنة إمدادات الملاك", 0.30f, 0.34f, "save_anchor", 2, zone = WorldZone.WEST)
    )
}

data class LostKid(
    val id: String,
    val worldId: WorldId,
    val name: String,
    val nameAr: String,
    val xNorm: Float,
    val yNorm: Float,
    val accentColor: Color,
    val pleaEn: String,
    val pleaAr: String,
    val thanksEn: String,
    val thanksAr: String,
    val buffEn: String,
    val buffAr: String,
    val hpBonus: Int = 0,
    val defBonus: Int = 0,
    val atkBonus: Int = 0,
    val speedBonus: Float = 0f,
    val healAmount: Int = 0,
    val zone: WorldZone = WorldZone.CENTER
)

data class GlitchedMemory(
    val id: Int,
    val tagEn: String,
    val tagAr: String,
    val textEn: String,
    val textAr: String
)

enum class BattlePhase {
    PLAYER_MENU,
    FIGHT_MINIGAME,
    ENEMY_DIALOGUE,
    BULLET_HELL,
    VICTORY,
    DEFEAT
}

enum class MenuSubTab {
    ROOT,
    SOUL_SELECT,
    ACT_LIST,
    ITEM_LIST,
    ALLY_LIST
}

data class BattleItem(
    val id: String,
    val name: String,
    val healAmount: Int,
    val grantsShield: Boolean,
    val count: Int,
    val description: String
)

enum class BulletKind {
    ORB,
    LANCE,
    BOUNCER,
    TIME_SHARD,
    PLAYER_SHOT // Fired by Yellow Soul!
}

data class Bullet(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radiusNorm: Float = 0.032f,
    val kind: BulletKind = BulletKind.ORB,
    val color: Color = Color.White,
    val ageSec: Float = 0f,
    val bouncesLeft: Int = 0
)

data class LaserBeam(
    val id: Long,
    val isHorizontal: Boolean,
    val positionNorm: Float,
    val thicknessNorm: Float = 0.075f,
    val warmupSec: Float = 0.75f,
    val activeSec: Float = 0.35f,
    val elapsedSec: Float = 0f,
    val color: Color
) {
    val isFiring: Boolean get() = elapsedSec >= warmupSec && elapsedSec < (warmupSec + activeSec)
    val isExpired: Boolean get() = elapsedSec >= (warmupSec + activeSec)
}

data class ArenaParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val life: Float,
    val maxLife: Float,
    val color: Color,
    val sizeDp: Float = 4f
)

/**
 * Attack Type 1 (Image 2 Top-Left): Vertical rectangular danger zone with Up/Down arrow telegraph.
 */
data class LaneTelegraph(
    val id: Long,
    val xNorm: Float,
    val widthNorm: Float = 0.14f,
    val isUpward: Boolean,
    val warmupSec: Float = 0.82f,
    val activeSec: Float = 0.36f,
    val elapsedSec: Float = 0f,
    val color: Color
) {
    val isFiring: Boolean get() = elapsedSec >= warmupSec && elapsedSec < (warmupSec + activeSec)
    val isExpired: Boolean get() = elapsedSec >= (warmupSec + activeSec)
}

/**
 * Attack Type 2 (Image 2 Top-Right): Boss-shaped chomp head + claw slash!
 */
data class MonsterJawAttack(
    val id: Long,
    val xNorm: Float,
    val yNorm: Float,
    val clawOffsetX: Float,
    val warmupSec: Float = 0.78f,
    val activeSec: Float = 0.34f,
    val elapsedSec: Float = 0f,
    val radiusNorm: Float = 0.11f,
    val color: Color
) {
    val isChomping: Boolean get() = elapsedSec >= warmupSec && elapsedSec < (warmupSec + activeSec)
    val isExpired: Boolean get() = elapsedSec >= (warmupSec + activeSec)
}

/**
 * Attack Type 3 (Image 2 Middle-Left): Crossed directional arrows telegraph -> replaced by actual attack shots!
 */
data class ArrowTelegraph(
    val id: Long,
    val originX: Float,
    val originY: Float,
    val dirX: Float,
    val dirY: Float,
    val warmupSec: Float = 0.72f,
    val elapsedSec: Float = 0f,
    val color: Color
) {
    val readyToFire: Boolean get() = elapsedSec >= warmupSec
}

/**
 * Attack Type 4 (Image 2 Middle-Right): Fuse bomb that lands, burns fuse, and bursts into radial shrapnel!
 */
data class ArenaBomb(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val fuseSec: Float = 1.10f,
    val elapsedSec: Float = 0f,
    val color: Color
) {
    val shouldExplode: Boolean get() = elapsedSec >= fuseSec
}

/**
 * Attack Type 5 (Image 2 Bottom-Right): Side wall "! <-" / "-> !" shot warnings before shooting across!
 */
data class SideWallShotWarning(
    val id: Long,
    val fromRightEdge: Boolean,
    val yNorm: Float,
    val warmupSec: Float = 0.65f,
    val elapsedSec: Float = 0f,
    val color: Color
) {
    val readyToShoot: Boolean get() = elapsedSec >= warmupSec
}

enum class GameDifficulty(
    val labelEn: String,
    val labelAr: String,
    val enemyDamageMult: Float,
    val enemyBulletSpeedMult: Float,
    val playerStrikeMult: Float
) {
    EASY("Easy", "سهل", 0.75f, 0.85f, 1.15f),
    NORMAL("Normal", "عادي", 1.0f, 1.0f, 1.0f),
    HARD("Hard", "صعب", 1.35f, 1.18f, 0.92f)
}

data class RelicEquipment(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val hpBonus: Int,
    val defBonus: Int,
    val atkBonus: Int,
    val speedBonus: Float,
    val critBonus: Float,
    val normalMonsterMercyBonus: Int,
    val accentColor: Color,
    val descriptionEn: String,
    val descriptionAr: String
)

object RelicCatalog {
    val all: List<RelicEquipment> = listOf(
        RelicEquipment(
            id = "golden_locket",
            nameEn = "Golden Heart Locket",
            nameAr = "قلادة القلب الذهبي",
            hpBonus = 2,
            defBonus = 0,
            atkBonus = 1,
            speedBonus = 0f,
            critBonus = 0.01f,
            normalMonsterMercyBonus = 0,
            accentColor = Color(0xFFFACC15),
            descriptionEn = "Loka's keepsake. +2 Max HP, +1 ATK across timelines.",
            descriptionAr = "تذكار لوكا الذهبي. +2 دم أقصى و+1 هجوم."
        ),
        RelicEquipment(
            id = "chrono_pendulum",
            nameEn = "Chrono Pendulum",
            nameAr = "بندول الزمن",
            hpBonus = 0,
            defBonus = 0,
            atkBonus = 1,
            speedBonus = 0.16f,
            critBonus = 0.04f,
            normalMonsterMercyBonus = 0,
            accentColor = Color(0xFF38BDF8),
            descriptionEn = "+16% Movement Speed & +4% Crit Strike Zone.",
            descriptionAr = "+16% سرعة حركة و+4% مساحة ضربة حرجة."
        ),
        RelicEquipment(
            id = "seraph_feather",
            nameEn = "Cassiel's Choir Feather",
            nameAr = "ريشة جوقة كاسييل",
            hpBonus = 6,
            defBonus = 2,
            atkBonus = 0,
            speedBonus = 0.05f,
            critBonus = 0f,
            normalMonsterMercyBonus = 10,
            accentColor = Color(0xFFF472B6),
            descriptionEn = "+6 Max HP, +2 DEF & +10% Mercy vs normal monsters (bosses remain 10%).",
            descriptionAr = "+6 دم و+2 دفاع و+10% رحمة للوحوش العادية (الزعماء 10%)."
        ),
        RelicEquipment(
            id = "infernal_core",
            nameEn = "Paradox Ember Core",
            nameAr = "نواة جمر المفارقة",
            hpBonus = 0,
            defBonus = 1,
            atkBonus = 5,
            speedBonus = 0.08f,
            critBonus = 0.05f,
            normalMonsterMercyBonus = 0,
            accentColor = Color(0xFFEF4444),
            descriptionEn = "+5 ATK, +1 DEF & sharp crimson critical window.",
            descriptionAr = "+5 هجوم و+1 دفاع وفرصة حرجة أعلى."
        ),
        RelicEquipment(
            id = "echo_prism",
            nameEn = "Echo Stabilizer Prism",
            nameAr = "منشور موازنة الصدى",
            hpBonus = 8,
            defBonus = 2,
            atkBonus = 3,
            speedBonus = 0.12f,
            critBonus = 0.04f,
            normalMonsterMercyBonus = 15,
            accentColor = Color(0xFF22D3EE),
            descriptionEn = "Relic from fractured timelines: +8 HP, +2 DEF, +3 ATK, +12% Speed.",
            descriptionAr = "أثر الخطوط الزمنية: +8 دم، +2 دفاع، +3 هجوم، +12% سرعة."
        )
    )

    fun byId(id: String): RelicEquipment = all.firstOrNull { it.id == id } ?: all.first()
}

data class TimelineEcho(
    val timelineNumber: Int,
    val routeLabel: String,
    val friendsCount: Int,
    val stoppedCount: Int,
    val slainCount: Int,
    val kidsRescued: Int,
    val kidsKilled: Int
)

data class LoreEncyclopediaEntry(
    val id: String,
    val categoryEn: String,
    val categoryAr: String,
    val titleEn: String,
    val titleAr: String,
    val bodyEn: String,
    val bodyAr: String,
    val accentColor: Color
)

object LoreEncyclopediaCatalog {
    val entries: List<LoreEncyclopediaEntry> = listOf(
        LoreEncyclopediaEntry(
            id = "lore_races",
            categoryEn = "WORLD LORE",
            categoryAr = "تاريخ العالم",
            titleEn = "The Four Coexisting Races",
            titleAr = "الأجناس الأربعة المتعايشة",
            bodyEn = "Humans, Monsters, Angels, and Devils shared one sky until time-paradox rifts fractured borders and sparked the Citadel security decrees.",
            bodyAr = "عاش البشر والوحوش والملائكة والشياطين تحت سماء واحدة حتى مزقت صدوع المفارقة الحدود وأشعلت قرارات الحكومة.",
            accentColor = SepiaAmber
        ),
        LoreEncyclopediaEntry(
            id = "lore_worlds",
            categoryEn = "REALM MAP",
            categoryAr = "خريطة العوالم",
            titleEn = "Connected World Gates vs Void Anomaly",
            titleAr = "بوابات العوالم المتصلة مقابل الشذوذ",
            bodyEn = "Normal World, Outlands Realm, Split Dominion, and Government HQ connect directly via colored World Gates. Only Glitched Void exists outside normal space.",
            bodyAr = "ترتبط العوالم الأربعة مباشرة عبر بوابات العوالم، بينما يبقى العالم المقلتش شذوذاً منفصلاً يظهر فيه الصدى.",
            accentColor = GlitchCyan
        ),
        LoreEncyclopediaEntry(
            id = "lore_government",
            categoryEn = "MAIN GOAL",
            categoryAr = "الهدف الرئيسي",
            titleEn = "Stopping The Government & Allies",
            titleAr = "إيقاف الحكومة والحلفاء",
            bodyEn = "Lord Cassiel (Angel Leader) aids Loka freely. Marshal Krog (Monster Chief) is the ONLY Government boss who can become your friend; Chancellor Vance must be disarmed via ACT+STOP or defeated.",
            bodyAr = "زعيم الملائكة كاسييل يساعد لوكا داخل الحكومة، والمارشال كروغ (زعيم الوحوش) هو الوحيد الذي يمكن مصادقته هناك، بينما فانس يجب إيقافه بالـ ACT أو القتال.",
            accentColor = SoulSeraphCyan
        ),
        LoreEncyclopediaEntry(
            id = "lore_ashen",
            categoryEn = "SECRET",
            categoryAr = "سر الحفظ",
            titleEn = "Ashen & The 8th Save Curse",
            titleAr = "آشن ولعنة الحفظ الثامن",
            bodyEn = "Saving with Ashen hides him underground for ~75 seconds. Every 8th save pulls Loka into the Glitched Void unless Lost Tapes unlock full teleport mastery.",
            bodyAr = "بعد كل حفظ ينزل آشن تحت الأرض لحوالي 75 ثانية. وعند الحفظ رقم 8 ينسحب لوكا إلى العالم المقلتش.",
            accentColor = Color(0xFFFACC15)
        ),
        LoreEncyclopediaEntry(
            id = "lore_combat",
            categoryEn = "GUIDE",
            categoryAr = "دليل اللعب",
            titleEn = "Combat, Ordered ACTs & Souls",
            titleAr = "القتال وترتيب الـ ACT والأرواح",
            bodyEn = "Boss ACT options strictly grant +10% Mercy/Stop each step, while normal monsters have unique sequences and mercy percentages. Switch Soul modes mid-fight to adapt!",
            bodyAr = "خيارات ACT للزعماء تمنح 10% بالضبط لكل خطوة، أما الوحوش العادية فلها ترتيب ونسب مختلفة. بدّل الروح المناسبة أثناء المراوغة!",
            accentColor = SoulParadoxCrimson
        )
    )
}


