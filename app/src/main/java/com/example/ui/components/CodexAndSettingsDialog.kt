package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.ArmorCatalog
import com.example.model.EnemyRoster
import com.example.model.GameDifficulty
import com.example.model.LoreEncyclopediaCatalog
import com.example.model.RelicCatalog
import com.example.model.WeaponCatalog
import com.example.model.tr
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GlitchCyan
import com.example.ui.theme.HpBarGreen
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.MutedSepiaInk
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.SepiaAmber
import com.example.ui.theme.SepiaDarkBorder
import com.example.ui.theme.SepiaGold
import com.example.viewmodel.AppUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CodexAndSettingsDialog(
    state: AppUiState,
    initialTab: Int = 0,
    onDismiss: () -> Unit,
    onEquipArmor: (String) -> Unit,
    onEquipWeapon: (String) -> Unit,
    onEquipRelic: (String) -> Unit,
    onSetVolume: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onSetDifficulty: (GameDifficulty) -> Unit,
    onToggleReduceFlash: () -> Unit,
    onToggleControls: () -> Unit,
    onSaveToSlot: (Int) -> Unit,
    onSwitchSlot: (Int) -> Unit,
    onClearSlot: (Int) -> Unit,
    onStartNewTimeline: () -> Unit,
    onWatchStoryIntro: () -> Unit,
    onRequestFullReset: () -> Unit
) {
    val lang = state.language
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }

    val tabLabels = listOf(
        tr(lang, "STATUS", "الحالة"),
        tr(lang, "GEAR/RELIC", "العتاد"),
        tr(lang, "TIMELINE", "الزمن"),
        tr(lang, "LORE/GUIDE", "الدليل"),
        tr(lang, "SLOTS/SET", "الحفظ والإعداد")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(0.dp),
        containerColor = Color.Black,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = tr(
                        lang,
                        "LOKA'S CODEX · SLOT ${state.activeSaveSlot + 1} · TIMELINE #${state.timelineResetCount + 1}",
                        "سجل لوكا · خانة ${state.activeSaveSlot + 1} · خط زمني #${state.timelineResetCount + 1}"
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = SepiaGold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabLabels.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            shape = RoundedCornerShape(0.dp),
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MercyYellow.copy(alpha = 0.24f),
                                selectedLabelColor = MercyYellow,
                                labelColor = MutedSepiaInk
                            ),
                            modifier = Modifier.testTag("codex_tab_$idx")
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTab) {
                    // 0: STATUS & RELATIONSHIPS
                    0 -> {
                        val stats = state.computedStats
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, SepiaAmber, RoundedCornerShape(0.dp))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "HP ${stats.maxHp} · ATK +${stats.attackBonus} · DEF +${stats.defense} · SPD ${(stats.speedMultiplier * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MercyYellow
                                )
                                Text(
                                    text = tr(
                                        lang,
                                        "Difficulty: ${state.difficulty.labelEn} · Sprint: ${if (state.isRunningMode) "RUN (1.6x)" else "WALK"}",
                                        "الصعوبة: ${state.difficulty.labelAr} · الحركة: ${if (state.isRunningMode) "ركض (1.6x)" else "مشي"}"
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GlitchCyan
                                )
                                Text(
                                    text = tr(
                                        lang,
                                        "Friends: ${state.friendAllies.size} · Stopped: ${state.stoppedEnemies.size} · Slain: ${state.defeatedEnemies.size}",
                                        "الأصدقاء: ${state.friendAllies.size} · الموقَفون: ${state.stoppedEnemies.size} · القتلى: ${state.defeatedEnemies.size}"
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentInk
                                )
                                Text(
                                    text = tr(
                                        lang,
                                        "Kids Escorted: ${state.escortingKidIds.size} · Returned Safe: ${state.rescuedToNormalKidIds.size} · Killed: ${state.killedKidIds.size}",
                                        "أطفال معك: ${state.escortingKidIds.size} · وصلوا بأمان: ${state.rescuedToNormalKidIds.size} · قُتلوا: ${state.killedKidIds.size}"
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSepiaInk
                                )
                            }
                        }

                        if (state.helpedKidsList.isNotEmpty()) {
                            Text(
                                text = tr(lang, "Active Escort Buffs:", "دعم الأطفال المرافقين:") + " " +
                                    state.helpedKidsList.joinToString(" | ") { if (lang == AppLanguage.AR) it.buffAr else it.buffEn },
                                style = MaterialTheme.typography.labelSmall,
                                color = HpBarGreen
                            )
                        }

                        Text(
                            text = tr(lang, "Boss Relationship Ledger", "سجل علاقات الزعماء"),
                            style = MaterialTheme.typography.labelMedium,
                            color = SepiaGold
                        )
                        EnemyRoster.standardPlayableRoster().forEach { boss ->
                            val status = when {
                                boss.id in state.friendAllies -> tr(lang, "♥ FRIEND", "♥ صديق") to HpBarGreen
                                boss.id in state.stoppedEnemies -> tr(lang, "⚖ STOPPED", "⚖ مُوقَف") to GlitchCyan
                                boss.id in state.defeatedEnemies -> tr(lang, "✕ SLAIN", "✕ مقتول") to DangerRed
                                else -> tr(lang, "• UNRESOLVED", "• لم يُحسم") to MutedSepiaInk
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(boss.name, style = MaterialTheme.typography.labelSmall, color = ParchmentInk)
                                Text(status.first, style = MaterialTheme.typography.labelSmall, color = status.second)
                            }
                        }
                    }

                    // 1: GEAR, RELICS & ITEMS
                    1 -> {
                        Text(
                            text = "${tr(lang, "Weapon", "السلاح")}: ${if (lang == AppLanguage.AR) state.equippedWeapon.nameAr else state.equippedWeapon.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = state.equippedWeapon.accentColor
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            WeaponCatalog.all.filter { it.id in state.unlockedWeaponIds }.forEach { weapon ->
                                FilterChip(
                                    selected = weapon.id == state.equippedWeaponId,
                                    onClick = { onEquipWeapon(weapon.id) },
                                    shape = RoundedCornerShape(0.dp),
                                    label = { Text(if (lang == AppLanguage.AR) weapon.nameAr else weapon.name, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = weapon.accentColor.copy(alpha = 0.24f),
                                        selectedLabelColor = weapon.accentColor,
                                        labelColor = MutedSepiaInk
                                    )
                                )
                            }
                        }

                        Text(
                            text = "${tr(lang, "Armor", "الدرع")}: ${state.equippedArmor.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SepiaAmber
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ArmorCatalog.all.filter { it.id in state.unlockedArmorIds }.forEach { armor ->
                                FilterChip(
                                    selected = armor.id == state.equippedArmorId,
                                    onClick = { onEquipArmor(armor.id) },
                                    shape = RoundedCornerShape(0.dp),
                                    label = { Text(armor.name, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SepiaAmber.copy(alpha = 0.22f),
                                        selectedLabelColor = SepiaAmber,
                                        labelColor = MutedSepiaInk
                                    )
                                )
                            }
                        }

                        Text(
                            text = "${tr(lang, "Equipped Relic", "الأثر المجهّز")}: ${if (lang == AppLanguage.AR) state.equippedRelic.nameAr else state.equippedRelic.nameEn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = state.equippedRelic.accentColor
                        )
                        Text(
                            text = if (lang == AppLanguage.AR) state.equippedRelic.descriptionAr else state.equippedRelic.descriptionEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentInk
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            RelicCatalog.all.filter { it.id in state.unlockedRelicIds }.forEach { relic ->
                                FilterChip(
                                    selected = relic.id == state.equippedRelicId,
                                    onClick = { onEquipRelic(relic.id) },
                                    shape = RoundedCornerShape(0.dp),
                                    label = { Text(if (lang == AppLanguage.AR) relic.nameAr else relic.nameEn, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = relic.accentColor.copy(alpha = 0.24f),
                                        selectedLabelColor = relic.accentColor,
                                        labelColor = MutedSepiaInk
                                    ),
                                    modifier = Modifier.testTag("equip_relic_${relic.id}")
                                )
                            }
                        }

                        val cShards = state.itemCounts["chrono_shard"] ?: 0
                        val sRations = state.itemCounts["sepia_ration"] ?: 0
                        val sAnchors = state.itemCounts["save_anchor"] ?: 0
                        Text(
                            text = tr(
                                lang,
                                "Bag Supplies: Chrono Shards ($cShards) · Sepia Rations ($sRations) · Time Anchors ($sAnchors)",
                                "الإمدادات: شظايا زمن ($cShards) · حصص شفاء ($sRations) · مراسي ($sAnchors)"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSepiaInk
                        )
                    }

                    // 2: MULTIPLE TIMELINES & REACTIVE CHOICES
                    2 -> {
                        val mem = state.timelineMemory
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, GlitchCyan, RoundedCornerShape(0.dp))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (lang == AppLanguage.AR) mem.ashenGreetingAr else mem.ashenGreetingEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GlitchCyan
                                )
                                Text(
                                    text = if (state.isParadoxTrialUnlocked) {
                                        tr(lang, "★ Paradox Echo Trial UNLOCKED (+Echo Prism Relic upon timeline loop!)", "★ تحدي صدى المفارقة مفتوح!")
                                    } else {
                                        tr(lang, "• Make more boss/kid choices to awaken Timeline Echoes.", "• اتخذ قرارات أكثر لإيقاظ أصداء الخطوط الزمنية.")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MercyYellow
                                )
                            }
                        }

                        if (state.pastTimelines.isNotEmpty()) {
                            Text(tr(lang, "Recorded Past Timelines", "الخطوط الزمنية السابقة"), style = MaterialTheme.typography.labelMedium, color = SepiaGold)
                            state.pastTimelines.forEach { echo ->
                                Text(
                                    text = "#${echo.timelineNumber}: ${echo.routeLabel} (♥${echo.friendsCount} ⚖${echo.stoppedCount} ✕${echo.slainCount})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentInk
                                )
                            }
                        }

                        if (state.choiceHistory.isNotEmpty()) {
                            Text(tr(lang, "Recent Decisions", "أحدث قراراتك"), style = MaterialTheme.typography.labelMedium, color = SepiaAmber)
                            state.choiceHistory.takeLast(6).reversed().forEach { entry ->
                                Text("• $entry", style = MaterialTheme.typography.labelSmall, color = MutedSepiaInk)
                            }
                        }

                        Button(
                            onClick = {
                                onStartNewTimeline()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GlitchCyan, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("codex_new_timeline_button")
                        ) {
                            Text(
                                text = tr(lang, "NEW TIMELINE (KEEP ECHOES & RELIC)", "خط زمني جديد (احتفظ بالأصداء)"),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    // 3: LORE ENCYCLOPEDIA & BEGINNER GUIDE
                    3 -> {
                        LoreEncyclopediaCatalog.entries.forEach { entry ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, entry.accentColor.copy(alpha = 0.75f), RoundedCornerShape(0.dp))
                                    .padding(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "${if (lang == AppLanguage.AR) entry.categoryAr else entry.categoryEn} · ${if (lang == AppLanguage.AR) entry.titleAr else entry.titleEn}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = entry.accentColor
                                    )
                                    Text(
                                        text = if (lang == AppLanguage.AR) entry.bodyAr else entry.bodyEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ParchmentInk
                                    )
                                }
                            }
                        }
                    }

                    // 4: SETTINGS & 3 SAVE SLOTS
                    4 -> {
                        Text(
                            text = "${tr(lang, "Master Volume", "مستوى الصوت")}: ${(state.masterVolume * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = SepiaGold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Slider(
                                value = state.masterVolume,
                                onValueChange = onSetVolume,
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MercyYellow,
                                    activeTrackColor = MercyYellow,
                                    inactiveTrackColor = SepiaDarkBorder
                                ),
                                modifier = Modifier.weight(1f).testTag("settings_volume_slider")
                            )
                            OutlinedButton(onClick = onToggleMute, shape = RoundedCornerShape(0.dp)) {
                                Text(
                                    text = if (state.isMuted) tr(lang, "UNMUTE", "تفعيل") else tr(lang, "MUTE", "كتم"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SepiaAmber
                                )
                            }
                        }

                        Text(tr(lang, "Difficulty", "مستوى الصعوبة"), style = MaterialTheme.typography.labelMedium, color = SepiaGold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GameDifficulty.entries.forEach { diff ->
                                FilterChip(
                                    selected = state.difficulty == diff,
                                    onClick = { onSetDifficulty(diff) },
                                    shape = RoundedCornerShape(0.dp),
                                    label = { Text(if (lang == AppLanguage.AR) diff.labelAr else diff.labelEn, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MercyYellow.copy(alpha = 0.25f),
                                        selectedLabelColor = MercyYellow,
                                        labelColor = MutedSepiaInk
                                    ),
                                    modifier = Modifier.testTag("difficulty_chip_${diff.name}")
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = onToggleControls,
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (lang == AppLanguage.AR) state.controlScheme.labelAr else state.controlScheme.label, style = MaterialTheme.typography.labelSmall, color = ParchmentInk)
                            }
                            OutlinedButton(
                                onClick = onToggleReduceFlash,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.dp, if (state.reduceFlash) HpBarGreen else SepiaDarkBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (state.reduceFlash) tr(lang, "Flash: Reduced", "الوميض: مخفف") else tr(lang, "Flash: Full", "الوميض: كامل"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.reduceFlash) HpBarGreen else MutedSepiaInk
                                )
                            }
                        }

                        Text(tr(lang, "3 Save Slots (Auto + Manual)", "٣ خانات حفظ مستقلة"), style = MaterialTheme.typography.labelMedium, color = SepiaGold)
                        state.saveSlotSummaries.forEach { slot ->
                            val isActive = slot.slotIndex == state.activeSaveSlot
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, if (isActive) MercyYellow else SepiaDarkBorder, RoundedCornerShape(0.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (slot.isEmpty) {
                                            tr(lang, "Slot ${slot.slotIndex + 1} · Empty", "خانة ${slot.slotIndex + 1} · فارغة")
                                        } else {
                                            tr(
                                                lang,
                                                "Slot ${slot.slotIndex + 1}${if (isActive) " ★" else ""} · ${slot.activeWorld.title}",
                                                "خانة ${slot.slotIndex + 1}${if (isActive) " ★" else ""} · ${slot.activeWorld.title}"
                                            )
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isActive) MercyYellow else ParchmentInk
                                    )
                                    if (!slot.isEmpty) {
                                        Text(
                                            text = "TL #${slot.timelineResetCount + 1} · ♥${slot.friendsCount} ⚖${slot.stoppedCount} ✕${slot.slainCount} · ${slot.difficulty.labelEn}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedSepiaInk
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = { onSaveToSlot(slot.slotIndex) },
                                        shape = RoundedCornerShape(0.dp),
                                        modifier = Modifier.testTag("save_slot_btn_${slot.slotIndex}")
                                    ) {
                                        Text(tr(lang, "SAVE", "حفظ"), style = MaterialTheme.typography.labelSmall, color = HpBarGreen)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            onSwitchSlot(slot.slotIndex)
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(0.dp),
                                        modifier = Modifier.testTag("load_slot_btn_${slot.slotIndex}")
                                    ) {
                                        Text(tr(lang, "LOAD", "فتح"), style = MaterialTheme.typography.labelSmall, color = GlitchCyan)
                                    }
                                    if (!slot.isEmpty) {
                                        OutlinedButton(
                                            onClick = { onClearSlot(slot.slotIndex) },
                                            shape = RoundedCornerShape(0.dp),
                                            modifier = Modifier.testTag("clear_slot_btn_${slot.slotIndex}")
                                        ) {
                                            Text("✕", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                                        }
                                    }
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onWatchStoryIntro()
                                },
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(tr(lang, "Replay Story", "إعادة القصة"), style = MaterialTheme.typography.labelSmall, color = SepiaAmber)
                            }
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onRequestFullReset()
                                },
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.dp, DangerRed),
                                modifier = Modifier.weight(1f).testTag("bag_reset_save_button")
                            ) {
                                Text(tr(lang, "Erase Slot", "مسح الخانة"), style = MaterialTheme.typography.labelSmall, color = DangerRed)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(0.dp)) {
                Text(tr(lang, "CLOSE", "إغلاق"), color = SepiaAmber)
            }
        }
    )
}
