package com.example.engine

import com.example.model.EnemyId
import com.example.model.EnemyProfile
import com.example.model.GameDifficulty
import com.example.model.RelicCatalog
import com.example.model.RelicEquipment
import com.example.model.SoulType
import com.example.model.WeaponEquipment
import kotlin.math.abs

data class StrikeOutcome(
    val damage: Int,
    val isCritical: Boolean,
    val accuracy: Float
)

data class ActEvaluationResult(
    val optionFound: Boolean,
    val blockedByUnfriendable: Boolean,
    val outOfOrder: Boolean,
    val alreadyCompletedStep: Boolean,
    val mercyGained: Int,
    val newMercyProgress: Int,
    val completedActIds: Set<String>,
    val message: String
)

object BattleEngine {
    fun calculateStrikeDamage(
        cursorPos: Float,
        targetCenter: Float = 0.50f,
        attackBonus: Int,
        weapon: WeaponEquipment,
        relic: RelicEquipment = RelicCatalog.byId("golden_locket"),
        activeAllies: Set<EnemyId>,
        activeSoul: SoulType,
        difficulty: GameDifficulty = GameDifficulty.NORMAL
    ): StrikeOutcome {
        val dist = abs(cursorPos - targetCenter)
        val accuracy = (1f - (dist / 0.46f)).coerceIn(0.08f, 1f)
        val critWindow = 0.055f + (weapon.critWindowBonus * 0.45f) + relic.critBonus +
            (if (EnemyId.VEX in activeAllies) 0.015f else 0f)
        val isCrit = dist <= critWindow

        val rawDamage = 14 + (accuracy * 18).toInt() + attackBonus
        val weaponMultDamage = (rawDamage * weapon.strikeMultiplier *
            (if (isCrit) 1.35f else 1f) * difficulty.playerStrikeMult).toInt()
        val scaledDamage = (weaponMultDamage * activeSoul.damageMultiplier).toInt().coerceAtLeast(1)
        val finalDamage = activeSoul.flatDamageCap ?: scaledDamage

        return StrikeOutcome(
            damage = finalDamage,
            isCritical = isCrit,
            accuracy = accuracy
        )
    }

    fun calculateIncomingDamage(
        enemyAttackPower: Int,
        totalDefense: Int,
        activeSoul: SoulType,
        difficulty: GameDifficulty = GameDifficulty.NORMAL
    ): Int {
        val baseHit = (enemyAttackPower - totalDefense).coerceAtLeast(1)
        val scaled = (baseHit * difficulty.enemyDamageMult).toInt().coerceAtLeast(1)
        return scaled * activeSoul.incomingDamageMultiplier
    }

    /**
     * Evaluates an ACT step:
     * - Hostile monsters (cannot befriend nor stop peacefully) never gain Mercy.
     * - Required previous ACT steps must be completed in exact sequence.
     * - Bosses strictly gain 10% Mercy/Stop per ACT (user rule).
     * - Normal monsters use their per-monster unique mercy percentage (+ Relic bonus if any).
     */
    fun evaluateActStep(
        enemy: EnemyProfile,
        optionId: String,
        currentMercyProgress: Int,
        completedActIds: Set<String>,
        relic: RelicEquipment = RelicCatalog.byId("golden_locket")
    ): ActEvaluationResult {
        val act = enemy.actOptions.firstOrNull { it.id == optionId }
            ?: return ActEvaluationResult(
                optionFound = false,
                blockedByUnfriendable = false,
                outOfOrder = false,
                alreadyCompletedStep = false,
                mercyGained = 0,
                newMercyProgress = currentMercyProgress,
                completedActIds = completedActIds,
                message = ""
            )

        val allowsNonLethal = enemy.canBefriend || enemy.canStopWithoutKilling
        if (!allowsNonLethal) {
            return ActEvaluationResult(
                optionFound = true,
                blockedByUnfriendable = true,
                outOfOrder = false,
                alreadyCompletedStep = false,
                mercyGained = 0,
                newMercyProgress = currentMercyProgress,
                completedActIds = completedActIds,
                message = act.response
            )
        }

        val prereqMet = act.requiredPrevActId == null || act.requiredPrevActId in completedActIds
        if (!prereqMet) {
            return ActEvaluationResult(
                optionFound = true,
                blockedByUnfriendable = false,
                outOfOrder = true,
                alreadyCompletedStep = false,
                mercyGained = 0,
                newMercyProgress = currentMercyProgress,
                completedActIds = completedActIds,
                message = act.outOfOrderReply ?: "* Wrong order! Complete the previous ACT step first! (+0%)"
            )
        }

        val isNormalOrderedRepeat = enemy.isNormalMonster &&
            (optionId in completedActIds) &&
            enemy.actOptions.any { it.requiredPrevActId != null && it.id !in completedActIds }
        if (isNormalOrderedRepeat) {
            return ActEvaluationResult(
                optionFound = true,
                blockedByUnfriendable = false,
                outOfOrder = false,
                alreadyCompletedStep = true,
                mercyGained = 0,
                newMercyProgress = currentMercyProgress,
                completedActIds = completedActIds,
                message = "* Already completed '${act.title}'! Use the next step in the order! (+0%)"
            )
        }

        val gained = if (!enemy.isNormalMonster && enemy.id != EnemyId.RUINS_DUMMY) {
            10 // Strict 10% per ACT rule for all Bosses!
        } else {
            val relicBonus = if (enemy.isNormalMonster && act.mercyGain > 0) relic.normalMonsterMercyBonus else 0
            (act.mercyGain + relicBonus).coerceAtLeast(0)
        }

        val newMercy = (currentMercyProgress + gained).coerceAtMost(100)
        return ActEvaluationResult(
            optionFound = true,
            blockedByUnfriendable = false,
            outOfOrder = false,
            alreadyCompletedStep = false,
            mercyGained = gained,
            newMercyProgress = newMercy,
            completedActIds = completedActIds + optionId,
            message = act.response
        )
    }
}
