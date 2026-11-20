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
    GLITCHED_WORLD(
        title = "Glitched Void",
        subtitle = "No structures. Ghostly spawns. Random warp in 10s...",
        racesPresent = "Corrupted Echoes Only",
        allowedSouls = listOf(SoulType.WHITE_SICK),
        hasLandmarks = false
    )
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
    val rewardLabel: String
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
    val eventRewardText: String? = null
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
    val rewardCount: Int
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
        ArmorEquipment("rift_plate", "Ash-Iron Devil Guard", 3, 6, "Forged in the Devils' Cave. +3 DEF, +6 Max HP.")
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
    @DrawableRes val imageRes: Int?,
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
    ECHO_LOKA, // ONLY spawns inside the Glitched World!
    FUTURE_LOKA // Final confrontation in Route finales
}

data class EnemyActOption(
    val id: String,
    val title: String,
    val response: String,
    val mercyGain: Int
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
    val actOptions: List<EnemyActOption>
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
            raceTitle = "Human · Government Paradox Unit",
            difficultyLabel = "NORMAL WORLD",
            maxHp = 85,
            attackPower = 3,
            accentColor = SoulAegisAmber,
            xNorm = 0.72f,
            yNorm = 0.36f,
            allyBonusTitle = "Vex's Tactical Visor",
            allyBonusDesc = "+2 Attack Damage & wider Critical strike zone.",
            introLine = "Vex locks his visor. \"Loka—wait! Why did the paradox clock spike again?!\"",
            attackQuips = listOf(
                "\"Containment grid online! Stay inside the perimeter!\"",
                "\"Every 10 minutes you vanish—let me log your coordinates!\"",
                "\"We just want to find the other missing kids!\""
            ),
            spareLine = "Vex lowers his weapon and becomes your friend! \"Call me whenever the paradox flares up, Loka.\"",
            defeatLine = "Vex's scanner shorts out and he retreats down the street.",
            actOptions = listOf(
                EnemyActOption("check", "Check", "COMMANDER VEX — 85 HP. Searches for paradox kids; secretly wants to help.", 15),
                EnemyActOption("explain", "Show Necklace", "You show your golden heart necklace. Vex recognizes you and softens!", 50),
                EnemyActOption("promise", "Team Up", "You promise to share what you find in the labs. Vex smiles!", 45)
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
            xNorm = 0.28f,
            yNorm = 0.72f,
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
                EnemyActOption("check", "Check", "BRAMBLE — 95 HP. Gentle monster protecting lost children near the hills.", 15),
                EnemyActOption("calm", "Share Snacks", "You offer a warm biscuit. Bramble's horns glow happily!", 55),
                EnemyActOption("reassure", "Promise Peace", "You tell him the Red Soul seeks harmony. He nods.", 45)
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
            xNorm = 0.74f,
            yNorm = 0.68f,
            allyBonusTitle = "Vespera's Cyan Tea",
            allyBonusDesc = "Restores +1 HP at the start of every player turn.",
            introLine = "Vespera adjusts her cyan-silk fan. \"A Yellow and Blue Soul bearer in my beige courtyard?\"",
            attackQuips = listOf(
                "\"Show me the speed of your Blue Soul leap!\"",
                "\"Aim true with your Yellow Soul light!\"",
                "\"No humans walk here—yet you wear a warm heart.\""
            ),
            spareLine = "Lady Vespera curtsies warmly and becomes your ally! \"My mansion doors are always open to you, Loka.\"",
            defeatLine = "Vespera vanishes behind a curtain of cyan leaves.",
            actOptions = listOf(
                EnemyActOption("check", "Check", "LADY VESPERA — 105 HP. Tests travelers with elegant rhythmic volleys.", 15),
                EnemyActOption("bow", "Courteous Bow", "You bow respectfully. Vespera applauds your manners!", 50),
                EnemyActOption("tea", "Praise Cyan Trees", "You admire the cyan grove around her mansion. She delights!", 45)
            )
        ),
        EnemyProfile(
            id = EnemyId.ORIN,
            worldId = WorldId.TRI_RACE_REALM,
            name = "Sentinel Orin",
            raceTitle = "Monster-Angel Construct · Brown Caves",
            difficultyLabel = "OUTLANDS",
            maxHp = 115,
            attackPower = 4,
            accentColor = Color(0xFF3B82F6),
            xNorm = 0.25f,
            yNorm = 0.34f,
            allyBonusTitle = "Orin's Anchor Rune",
            allyBonusDesc = "Reduces Soul ability cooldowns by 20%.",
            introLine = "Orin's carved stone eyes ignite. \"INTRUDER OR FRIEND? PROVE IT IN MOTION.\"",
            attackQuips = listOf(
                "\"GRAVITY SHIFTS INSIDE THE BROWN CAVERN!\"",
                "\"USE YOUR YELLOW OR BLUE SOUL WISELY!\"",
                "\"THE SECRET LABS HIDE ANSWERS ABOUT YOUR PARADOX.\""
            ),
            spareLine = "Orin kneels and binds an ally rune to your necklace! \"Go safely, Loka.\"",
            defeatLine = "Orin crumbles peacefully into dormant stones.",
            actOptions = listOf(
                EnemyActOption("check", "Check", "SENTINEL ORIN — 115 HP. Ancient guardian of the brown caves.", 15),
                EnemyActOption("pulse", "Show Golden Heart", "Your necklace chimes with Orin's core. He recognizes your kindness!", 50),
                EnemyActOption("tune", "Stabilize Core", "You steady the cave crystals around him.", 45)
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
            xNorm = 0.24f,
            yNorm = 0.44f,
            allyBonusTitle = "Seraphiel's Green Grace",
            allyBonusDesc = "Start every battle with a free Guard Shield.",
            introLine = "Six wings scatter green leaves over pink-purple grass. \"Will your Purple and Green Souls bring peace?\"",
            attackQuips = listOf(
                "\"Guard with Green! Glide with Purple!\"",
                "\"The Angels fear another war with the Devils.\"",
                "\"Loka, can two divided halves ever mend?\""
            ),
            spareLine = "Seraphiel folds their wings and joins your side! \"My feathers will shield you on every world.\"",
            defeatLine = "Seraphiel ascends into the pink-purple canopy.",
            actOptions = listOf(
                EnemyActOption("check", "Check", "SERAPHIEL — 120 HP. Protects the Angels' green-tree sanctuaries.", 15),
                EnemyActOption("harmony", "Bridge Both Sides", "You promise to carry peace across to the Devils' border.", 50),
                EnemyActOption("hope", "Show Green Resolve", "You stand calm without striking. Seraphiel smiles.", 45)
            )
        ),
        EnemyProfile(
            id = EnemyId.MALAKOR,
            worldId = WorldId.SPLIT_REALM,
            name = "Malakor",
            raceTitle = "Devil · Red-Tree Border Vanguard",
            difficultyLabel = "SPLIT: DEVILS",
            maxHp = 130,
            attackPower = 5,
            accentColor = SoulInfernalViolet,
            xNorm = 0.78f,
            yNorm = 0.56f,
            allyBonusTitle = "Malakor's Ember Pact",
            allyBonusDesc = "+4 Attack Damage & bonus fight precision.",
            introLine = "Malakor steps from beneath red-leafed trees onto grey-brown earth. \"Show me what Loka's made of!\"",
            attackQuips = listOf(
                "\"Bouncing embers from the Devils' wasteland!\"",
                "\"Your Green Soul shield can't block forever—keep moving!\"",
                "\"Heh, the Angels think we're all monsters!\""
            ),
            spareLine = "Malakor laughs and fist-bumps your shoulder! \"Count me in as your ally, kid!\"",
            defeatLine = "Malakor steps back into red-ember mist, tipping his horns.",
            actOptions = listOf(
                EnemyActOption("check", "Check", "MALAKOR — 130 HP. Fierce devil warrior who respects courage.", 15),
                EnemyActOption("respect", "Stand Fearless", "You meet his gaze under the red trees. Malakor grins!", 50),
                EnemyActOption("truce", "Offer Alliance", "You invite him to join your friends across all worlds.", 45)
            )
        ),
        EnemyProfile(
            id = EnemyId.ECHO_LOKA,
            worldId = WorldId.GLITCHED_WORLD,
            name = "Echo-Loka",
            raceTitle = "Glitched Reflection · Paradox Anomaly",
            difficultyLabel = "GLITCHED VOID",
            maxHp = 12, // Sick White Soul deals 1 DMG per hit—so 12 HP or Spare!
            attackPower = 3, // Deals 2x = 6 DMG against Sick White Soul!
            accentColor = GlitchCyan,
            xNorm = 0.5f,
            yNorm = 0.35f,
            allyBonusTitle = "Echo's Stabilized Shard",
            allyBonusDesc = "+3 Attack Damage & immune to paradox stutter.",
            introLine = "A static silhouette wearing your grey shirt and golden necklace whispers... \"...why am I here...?\"",
            attackQuips = listOf(
                "\"YOUR WHITE SOUL IS SICK HERE... ONLY 1 DAMAGE...\"",
                "\"DOUBLE PAIN... TEN SECONDS LEFT...\"",
                "\"I AM THE ECHO THAT NEVER MADE IT BACK.\""
            ),
            spareLine = "Echo-Loka's static softens into warm gold. \"...thank you for remembering me, Loka.\"",
            defeatLine = "Echo-Loka shatters into eight silent white pixels.",
            actOptions = listOf(
                EnemyActOption("check", "Check", "ECHO-LOKA — 12 HP. Only exists in the Glitched World. Sick White Soul deals 1 DMG & takes 2x DMG!", 20),
                EnemyActOption("embrace", "Hold Golden Heart", "You touch your golden heart necklace. The static calms!", 45),
                EnemyActOption("remember", "Promise Return", "You promise you won't leave any lost kids behind.", 45)
            )
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
                EnemyActOption("check", "Check", "FUTURE-LOKA — 160 HP. Your future self carrying the weight of every lost timeline.", 15),
                EnemyActOption("listen", "Hear Their Plea", "You pause to hear their warning about destroying the world.", 35)
            )
        )
    )

    fun byId(id: EnemyId): EnemyProfile = all.first { it.id == id }
    fun forWorld(worldId: WorldId): List<EnemyProfile> = all.filter {
        it.worldId == worldId && it.id != EnemyId.FUTURE_LOKA && it.id != EnemyId.RUINS_DUMMY
    }
    fun standardPlayableRoster(): List<EnemyProfile> = all.filter {
        it.id != EnemyId.ECHO_LOKA && it.id != EnemyId.FUTURE_LOKA && it.id != EnemyId.RUINS_DUMMY
    }
}

object WorldContentCatalog {
    val landmarks: List<WorldLandmark> = listOf(
        // Normal World
        WorldLandmark("nw_hills", WorldId.NORMAL_WORLD, LandmarkKind.HILLS, "Emerald Overlook Hills", 0.22f, 0.22f,
            "Mission: Lost Kite", "Retrieve Maya's kite caught on the hilltop wind.", false, rewardArmorId = "hill_cloak", rewardLabel = "Armor: Greenhill Cloak"),
        WorldLandmark("nw_cave", WorldId.NORMAL_WORLD, LandmarkKind.CAVE, "Subway Hollow Cave", 0.18f, 0.78f,
            "Event: Glowing Stones", "Help Bramble clear fallen boulders blocking the tunnel.", false, rewardItemId = "chrono_shard", rewardLabel = "+2 Chrono Shards"),
        WorldLandmark("nw_mansion", WorldId.NORMAL_WORLD, LandmarkKind.MANSION, "Mayor's Old Mansion", 0.80f, 0.24f,
            "Mission: Peace Ledger", "Deliver the Four-Race Treaty page from the parlor.", false, rewardItemId = "sepia_ration", rewardWeaponId = "chrono_lance", rewardLabel = "Weapon: Chrono Lance + 2 Rations"),
        WorldLandmark("nw_lab", WorldId.NORMAL_WORLD, LandmarkKind.LAB, "Gov. Chrono Lab #4", 0.80f, 0.78f,
            "Secret Quest: Save #8 Tape", "Shocking discovery! Dr. Kestrel's tape reveals your 10-minute warp happens every 8 saves!", true, rewardItemId = "save_anchor", rewardLabel = "+2 Save Anchors"),

        // Tri-Race Realm (Beige ground, Cyan trees, Brown caves)
        WorldLandmark("tr_hills", WorldId.TRI_RACE_REALM, LandmarkKind.HILLS, "Beige Ridge Hills", 0.22f, 0.20f,
            "Event: Cyan Sapling", "Water three flickering cyan saplings on the beige ridge.", false, rewardArmorId = "cyan_bark_vest", rewardLabel = "Armor: Cyan Bark Mail"),
        WorldLandmark("tr_cave", WorldId.TRI_RACE_REALM, LandmarkKind.CAVE, "Amber-Brown Cavern", 0.22f, 0.76f,
            "Mission: Echo Crystals", "Align the humming crystals inside the brown cave.", false, rewardItemId = "chrono_shard", rewardWeaponId = "paradox_wrench", rewardLabel = "Weapon: Paradox Wrench + 2 Shards"),
        WorldLandmark("tr_mansion", WorldId.TRI_RACE_REALM, LandmarkKind.MANSION, "Vespera's Sand Mansion", 0.78f, 0.76f,
            "Event: Midnight Tea", "Join Lady Vespera's monster-angel-devil tea salon.", false, rewardItemId = "sepia_ration", rewardLabel = "+2 Sepia Rations"),
        WorldLandmark("tr_lab", WorldId.TRI_RACE_REALM, LandmarkKind.LAB, "Outlands Drift Lab", 0.78f, 0.22f,
            "Secret Quest: Yellow-Blue Core", "Calibrate the dual-soul capacitor and uncover another Lost Tape about your saves.", true, rewardItemId = "save_anchor", rewardLabel = "+2 Save Anchors"),

        // Split Realm (Angels: purple-pink ground + green trees; Devils: brown-grey ground + red trees)
        WorldLandmark("sr_hills", WorldId.SPLIT_REALM, LandmarkKind.HILLS, "Halo Crest Hills", 0.20f, 0.20f,
            "Mission: Fallen Feather", "Return a luminous green leaf & feather to the Angels' shrine.", false, rewardItemId = "sepia_ration", rewardWeaponId = "seraph_blade", rewardLabel = "Weapon: Seraph Blade + 2 Rations"),
        WorldLandmark("sr_mansion", WorldId.SPLIT_REALM, LandmarkKind.MANSION, "Pink-Silk Angel Mansion", 0.22f, 0.78f,
            "Secret Quest: Choir Loom", "Weave purple-pink threads and decode the hidden Lost Tape inside Aurelia's sanctuary.", true, rewardArmorId = "seraph_silk", rewardLabel = "Armor: Pink-Silk Halo Mantle"),
        WorldLandmark("sr_cave", WorldId.SPLIT_REALM, LandmarkKind.CAVE, "Ash-Brown Devil Cave", 0.80f, 0.24f,
            "Mission: Ember Forge", "Cool Kaelith's forge beneath the red-leaved trees.", false, rewardArmorId = "rift_plate", rewardLabel = "Armor: Ash-Iron Devil Guard"),
        WorldLandmark("sr_lab", WorldId.SPLIT_REALM, LandmarkKind.LAB, "Borderline Rift Lab", 0.78f, 0.78f,
            "Event: Truce Transmitter", "Link the Angel and Devil power grids across the divide.", false, rewardItemId = "save_anchor", rewardLabel = "+2 Save Anchors")
    )

    val npcs: List<WorldNpc> = listOf(
        WorldNpc("npc_maya", WorldId.NORMAL_WORLD, "Maya", "Human", 0.45f, 0.25f, Color(0xFFF59E0B),
            "\"Hey Loka! Love the golden heart necklace—no hat today? Watch out near the Lab!\"" ,
            "Neighborhood Gift", "sepia_ration", "Maya handed you +1 Sepia Ration!"),
        WorldNpc("npc_pip", WorldId.NORMAL_WORLD, "Pip", "Monster", 0.48f, 0.72f, Color(0xFF4ADE80),
            "\"My big brother says Humans, Monsters, Angels, and Devils used to hang out before the paradox war!\""),
        WorldNpc("npc_kestrel", WorldId.NORMAL_WORLD, "Dr. Kestrel", "Angel-Human", 0.65f, 0.64f, SoulSeraphCyan,
            "\"Your paradox warps seem completely random, Loka... Check the secret Labs if you dare learn why!\""),

        WorldNpc("npc_lyra", WorldId.TRI_RACE_REALM, "Lyra", "Angel Scout", 0.46f, 0.24f, SoulSeraphCyan,
            "\"No humans live in this realm—only Monsters, Angels, and Devils. Your Yellow & Blue Souls resonate here!\""),
        WorldNpc("npc_zor", WorldId.TRI_RACE_REALM, "Zor", "Devil Merchant", 0.56f, 0.74f, SoulInfernalViolet,
            "\"Beige sand and cyan trees look strange, huh? Take this shard for the brown caves!\"",
            "Zor's Bargain", "chrono_shard", "Zor tossed you +1 Chrono Shard!"),

        WorldNpc("npc_aurelia", WorldId.SPLIT_REALM, "Aurelia", "Angel", 0.36f, 0.58f, Color(0xFFEC4899),
            "\"The Angels' side has purple-pink earth and green trees. Hold steady with your Green Soul!\""),
        WorldNpc("npc_kaelith", WorldId.SPLIT_REALM, "Kaelith", "Devil", 0.64f, 0.36f, Color(0xFFEF4444),
            "\"Devils' turf: brown-grey dirt and red trees. Tell Malakor we're rooting for peace, Loka!\"",
            "Red-Tree Ration", "chrono_shard", "Kaelith gave you +1 Chrono Shard!")
    )

    val lostKids: List<LostKid> = listOf(
        LostKid(
            id = "kid_sora",
            worldId = WorldId.TRI_RACE_REALM,
            name = "Sora",
            nameAr = "سورا",
            xNorm = 0.66f,
            yNorm = 0.28f,
            accentColor = Color(0xFFF472B6),
            pleaEn = "\"Loka?! I fell into this cyan forest! Please help me stay safe until we warp back to the Normal World!\"",
            pleaAr = "\"لوكا؟! سقطت في هذه الغابة السماوية! ساعدني وخذني معك حتى نصل إلى العالم الطبيعي فقط!\"",
            thanksEn = "\"I'll stay with you until we reach the Normal World—boosting your Max HP (+5) and healing you in battle!\"",
            thanksAr = "\"سأبقى معك حتى توصلني للعالم الطبيعي فقط—سأرفع دمك (+5 HP) وأعالجك في القتال!\"",
            buffEn = "+5 Max HP & Battle Heal (+6 HP) while escorted",
            buffAr = "+5 دم أقصى وعلاج (+6 HP) حتى الوصول للعالم الطبيعي",
            hpBonus = 5,
            healAmount = 6
        ),
        LostKid(
            id = "kid_taro",
            worldId = WorldId.TRI_RACE_REALM,
            name = "Taro",
            nameAr = "تارو",
            xNorm = 0.44f,
            yNorm = 0.52f,
            accentColor = Color(0xFF38BDF8),
            pleaEn = "\"I got pulled away from the other kids... this beige desert wind is freezing!\"",
            pleaAr = "\"انفصلت عن باقي الأطفال... رياح هذه الصحراء البيج باردة جداً!\"",
            thanksEn = "\"You helped me breathe again! Take this Cyan Shield weave: +2 Defense & +2 HP!\"",
            thanksAr = "\"ساعدتني على الهدوء! خذ هذا الدرع السماوي: +2 دفاع و+2 دم أقصى!\"",
            buffEn = "+2 Defense & +2 Max HP",
            buffAr = "+2 دفاع و+2 دم أقصى",
            hpBonus = 2,
            defBonus = 2
        ),
        LostKid(
            id = "kid_nora",
            worldId = WorldId.SPLIT_REALM,
            name = "Nora",
            nameAr = "نورا",
            xNorm = 0.32f,
            yNorm = 0.36f,
            accentColor = Color(0xFFFACC15),
            pleaEn = "\"I'm trapped between the pink-purple grass and the border! Help me break the seal!\"",
            pleaAr = "\"أنا عالقة عند عشب الملائكة الوردي-البنفسجي! ساعدني في كسر الختم!\"",
            thanksEn = "\"I'm free! My light will power up your strikes: +4 Attack & Battle Heal (+4 HP)!\"",
            thanksAr = "\"تحررت! سأزيد قوة ضرباتك (+4 هجوم) وأعالجك في القتال (+4 HP)!\"",
            buffEn = "+4 Attack Power & +4 HP Heal",
            buffAr = "+4 قوة هجوم وعلاج (+4 HP)",
            atkBonus = 4,
            healAmount = 4
        ),
        LostKid(
            id = "kid_zayn",
            worldId = WorldId.SPLIT_REALM,
            name = "Zayn",
            nameAr = "زين",
            xNorm = 0.68f,
            yNorm = 0.66f,
            accentColor = Color(0xFF4ADE80),
            pleaEn = "\"My paradox boots froze on the Devils' grey-brown ash! Give me a pull, Loka!\"",
            pleaAr = "\"حذائي الزمني تجمد فوق تراب الشياطين البني-الرمادي! اسحبني يا لوكا!\"",
            thanksEn = "\"Awesome! I tuned your boots & Soul: +28% Faster Speed & +2 Attack!\"",
            thanksAr = "\"ممتاز! عدّلت سرعتك: أصبحت أسرع بنسبة 28% مع +2 هجوم!\"",
            buffEn = "+28% Speed & +2 Attack",
            buffAr = "+28% سرعة حركة و+2 هجوم",
            atkBonus = 2,
            speedBonus = 0.28f
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
        WorldChest("chest_nw_dot", WorldId.NORMAL_WORLD, ChestStyle.SMALL_DOT, "Park Supply Box", "صندوق الحديقة الصغير", 0.30f, 0.30f, "chrono_shard", 1),
        WorldChest("chest_nw_lock", WorldId.NORMAL_WORLD, ChestStyle.WIDE_PADLOCK, "Sealed Street Chest", "صندوق الشارع المقفل", 0.68f, 0.80f, "sepia_ration", 2),
        WorldChest("chest_tr_star", WorldId.TRI_RACE_REALM, ChestStyle.SQUARE_STAR, "Star-Clasp Crate", "صندوق النجمة السماوي", 0.32f, 0.66f, "save_anchor", 1),
        WorldChest("chest_tr_lock", WorldId.TRI_RACE_REALM, ChestStyle.WIDE_PADLOCK, "Outlands Vault Chest", "صندوق البراري المقفل", 0.68f, 0.42f, "sepia_ration", 2),
        WorldChest("chest_sr_dot", WorldId.SPLIT_REALM, ChestStyle.SMALL_DOT, "Angel Grove Chest", "صندوق الملائكة الصغير", 0.32f, 0.24f, "save_anchor", 2),
        WorldChest("chest_sr_star", WorldId.SPLIT_REALM, ChestStyle.SQUARE_STAR, "Ash-Border Star Chest", "صندوق النجمة المشتعل", 0.66f, 0.78f, "chrono_shard", 2)
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
    val healAmount: Int = 0
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

