package com.example.engine

import com.example.model.AppLanguage
import com.example.model.EnemyId
import com.example.model.EnemyRoster
import com.example.model.RouteType
import com.example.model.TimelineEcho

data class TimelineReactiveState(
    val ashenGreetingEn: String,
    val ashenGreetingAr: String,
    val echoModifierActive: Boolean,
    val bonusEchoAtk: Int
)

object TimelineManager {
    fun summarizeCurrentTimeline(
        timelineResetCount: Int,
        friendAllies: Set<EnemyId>,
        stoppedEnemies: Set<EnemyId>,
        defeatedEnemies: Set<EnemyId>,
        rescuedKids: Set<String>,
        killedKids: Set<String>,
        eligibleRoute: RouteType?
    ): TimelineEcho {
        val routeLabel = when {
            eligibleRoute != null -> eligibleRoute.title
            defeatedEnemies.isNotEmpty() && friendAllies.isEmpty() -> "Lethal Divergence"
            friendAllies.isNotEmpty() && defeatedEnemies.isEmpty() -> "Merciful Divergence"
            else -> "Fractured Run"
        }
        return TimelineEcho(
            timelineNumber = timelineResetCount + 1,
            routeLabel = routeLabel,
            friendsCount = friendAllies.size,
            stoppedCount = stoppedEnemies.size,
            slainCount = defeatedEnemies.size,
            kidsRescued = rescuedKids.size,
            kidsKilled = killedKids.size
        )
    }

    fun evaluateReactiveMemory(
        timelineResetCount: Int,
        pastTimelines: List<TimelineEcho>,
        currentKills: Int,
        currentFriends: Int,
        lang: AppLanguage
    ): TimelineReactiveState {
        val priorSlainTotal = pastTimelines.sumOf { it.slainCount }
        val priorFriendsTotal = pastTimelines.sumOf { it.friendsCount }

        val greetingEn = when {
            timelineResetCount == 0 ->
                "First recorded timeline. Every kill or mercy choice leaves a permanent echo."
            priorSlainTotal > priorFriendsTotal && currentFriends > 0 ->
                "Timeline #${timelineResetCount + 1}: Echoes remember your past violence ($priorSlainTotal slain), yet you're trying Mercy now."
            priorFriendsTotal > 0 && currentKills > 0 ->
                "Timeline #${timelineResetCount + 1}: You spared them once before... why draw your weapon in this timeline?"
            else ->
                "Timeline #${timelineResetCount + 1}: $timelineResetCount fractured loop(s) recorded across the Paradox."
        }
        val greetingAr = when {
            timelineResetCount == 0 ->
                "الخط الزمني الأول. كل قرار بالرحمة أو القتل يترك أثراً دائماً في المفارقة."
            priorSlainTotal > priorFriendsTotal && currentFriends > 0 ->
                "الخط الزمني #${timelineResetCount + 1}: الأصداء تتذكر قتالك السابق ($priorSlainTotal قتيل)، لكنك تحاول الرحمة الآن."
            priorFriendsTotal > 0 && currentKills > 0 ->
                "الخط الزمني #${timelineResetCount + 1}: لقد عفوت عنهم سابقاً... لماذا تقاتلهم الآن؟"
            else ->
                "الخط الزمني #${timelineResetCount + 1}: تم تسجيل $timelineResetCount خط(وط) زمنية منكسرة."
        }

        val echoActive = timelineResetCount >= 1 || pastTimelines.isNotEmpty()
        return TimelineReactiveState(
            ashenGreetingEn = greetingEn,
            ashenGreetingAr = greetingAr,
            echoModifierActive = echoActive,
            bonusEchoAtk = (timelineResetCount.coerceAtMost(3))
        )
    }

    fun resolveBossUnlockByDecision(
        choiceHistory: List<String>,
        timelineResetCount: Int
    ): Boolean {
        val standardCount = EnemyRoster.standardPlayableRoster().size
        return timelineResetCount >= 1 || choiceHistory.size >= (standardCount / 2)
    }
}
