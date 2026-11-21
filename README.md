# Loka's Paradox

**Loka's Paradox** is a 2D pixel-art RPG for Android written in **Kotlin + Jetpack Compose**, inspired by classic turn-based & bullet-hell RPGs (*Undertale* and *Deltarune*) while built from scratch with 100% original characters, storyline, synthesized PCM chiptune audio, and custom micro-pixel artwork.

> **Original Assets Notice:** All sprites, backgrounds, world rules, story dialogue, and synthesized audio (`RetroSoundEngine`) are generated natively in code as original work for this project. No external copyrighted assets or proprietary game files are included.

---

## Story & World

Four races coexist across fractured worlds: **Humans, Monsters, Angels, and Devils**. A child named **Loka** falls into a temporal paradox and awakens inside ancient Ruins before stepping out into a network of connected realms:

- **The Ruins (Tutorial Chambers):** Three connected chambers teaching movement, sprint/walk, switches, and combat basics.
- **Normal World:** Streets, hills, caves, and laboratories — home to the **Red Soul**.
- **Outlands Realm:** Beige desert ridges, cyan groves, and brown caverns — unlocks **Yellow**, **Blue**, and **Split Duo Souls**.
- **The Split Dominion:** Divided territory between Angels (pink-purple groves) and Devils (ash-brown wastes) — unlocks **Purple**, **Olive Green**, and **Split Duo Souls**.
- **The Government HQ:** The central citadel and primary objective. **Lord Cassiel (Angel Leader)** aids Loka, while **Marshal Krog (Monster Chief)** is the only Government leader who can become your friend, and **Chancellor Vance** must be stopped peacefully or by force.
- **The Glitched Void:** An isolated anomaly triggered on Ashen's **8th Save** or via hidden rifts, echoing Loka's past memories before unlocking the final **Echo-Loka** confrontation.

---

## Core Gameplay Features

- **Hybrid Combat System:** Classic timing-bar **FIGHT**, ordered **ACT** choices (strict **+10% Mercy/Stop per ACT** on bosses; unique sequences and percentages for normal monsters), **ITEM**, **ALLY**, **MERCY / STOP**, and a live bullet-hell arena with Soul abilities.
- **Walk & Run Animations:** Switch between Walk and Run (`1.6x` sprint) with animated directional stride frames and pixel dust trails.
- **Boss Archetypes:**
  - *Immediate Encounters* (Bramble, Vespera, Seraphiel, Marshal Krog)
  - *World-Completion Locked Bosses* (Sentinel Orin, Chancellor Vance, Echo-Loka)
  - *Cross-World Chasers* (Commander Vex and Malakor pursue you through World Gates)
- **Relics, Equipment & Timelines:** Equip weapons, armors, and Relics; track choices across **Multiple Timelines** where past endings leave echoes across future runs.
- **Multi-Slot Persistence & Audio Settings:** 3 Save Slots (`SharedPreferences` + JSON), difficulty selection (`Easy`, `Normal`, `Hard`), volume control, and accessibility toggles.

---

## Project Architecture

```text
app/src/main/java/com/example/
├── audio/        # RetroSoundEngine (zero-asset PCM BGM & SFX synthesizer)
├── data/         # GameSaveStorage (multi-slot save snapshots & settings)
├── engine/       # Pure Kotlin logic: LokaStats, BattleEngine, TimelineManager
├── model/        # GameModels (worlds, enemies, souls, relics, lore, bullets)
├── viewmodel/    # GameViewModel & BattleViewModel (StateFlow MVIs)
└── ui/
    ├── components/  # PixelArtRenderer, CharacterArtCanvas, BattleEffects, CodexAndSettingsDialog
    ├── screens/     # LoreIntro, RuinsTutorial, AshenMeeting, WorldExplore, Battle, RouteFinale
    └── theme/       # Material 3 typography, shapes & palette
```

---

## Building & Testing

```bash
# Run unit & Robolectric tests
gradle testDebugUnitTest

# Run Android Lint
gradle lintDebug

# Build debug APK
gradle assembleDebug
```

Automated builds run on GitHub Actions (`.github/workflows/build-apk.yml`) on every push to `main`.

---

## License

Released under the [MIT License](LICENSE).
