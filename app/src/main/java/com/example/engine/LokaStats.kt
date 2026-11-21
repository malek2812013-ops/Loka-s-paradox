package com.example.engine

import com.example.model.ArmorEquipment
import com.example.model.EnemyId
import com.example.model.GameDifficulty
import com.example.model.RelicCatalog
import com.example.model.RelicEquipment
import com.example.model.WeaponEquipment
import com.example.model.WorldContentCatalog
import com.example.model.WorldId

data class LokaComputedStats(
    val maxHp: Int,
    val defense: Int,
    val attackBonus: Int,
    val speedMultiplier: Float,
    val critWindowBonus: Float
)

object LokaStatsCalculator {
    fun compute(
        armor: ArmorEquipment,
        weapon: WeaponEquipment,
        relic: RelicEquipment = RelicCatalog.byId("golden_locket"),
        allies: Set<EnemyId>,
        helpedKidIds: Set<String>,
        difficulty: GameDifficulty = GameDifficulty.NORMAL,
        worldId: WorldId = WorldId.NORMAL_WORLD
    ): LokaComputedStats {
        val kidAllies = WorldContentCatalog.lostKids.filter { it.id in helpedKidIds }
        val kidHp = kidAllies.sumOf { it.hpBonus }
        val kidDef = kidAllies.sumOf { it.defBonus }
        val kidAtk = kidAllies.sumOf { it.atkBonus }
        val kidSpeed = kidAllies.sumOf { it.speedBonus.toDouble() }.toFloat()

        val brambleHp = if (EnemyId.BRAMBLE in allies) 4 else 0
        val krogHp = if (EnemyId.GOV_MONSTER_CHIEF in allies) 3 else 0
        val brambleDef = if (EnemyId.BRAMBLE in allies) 1 else 0

        val diffHpBonus = when (difficulty) {
            GameDifficulty.EASY -> 4
            GameDifficulty.NORMAL -> 0
            GameDifficulty.HARD -> -2
        }

        val maxHp = (20 + armor.maxHpBonus + relic.hpBonus + brambleHp + krogHp + kidHp + diffHpBonus)
            .coerceAtLeast(10)
        val totalDef = (armor.defenseBonus + relic.defBonus + brambleDef + kidDef).coerceAtLeast(0)

        var atkBonus = kidAtk + weapon.attackBonus + relic.atkBonus
        if (EnemyId.VEX in allies) atkBonus += 2
        if (EnemyId.MALAKOR in allies) atkBonus += 4
        if (EnemyId.GOV_MONSTER_CHIEF in allies) atkBonus += 3
        if (EnemyId.ECHO_LOKA in allies) atkBonus += 3

        val speedMult = if (worldId == WorldId.GLITCHED_WORLD) {
            1f
        } else {
            (1f + kidSpeed + relic.speedBonus).coerceIn(0.6f, 2.2f)
        }

        val critBonus = weapon.critWindowBonus + relic.critBonus +
            (if (EnemyId.VEX in allies) 0.015f else 0f)

        return LokaComputedStats(
            maxHp = maxHp,
            defense = totalDef,
            attackBonus = atkBonus,
            speedMultiplier = speedMult,
            critWindowBonus = critBonus
        )
    }
}
