# REDESIGN: EVOLUTION

A new PC tower defense game about elemental reactions and connected defenses. Active implementation: Godot 4.7.2 .NET with independent C# simulation. Course requirements and legacy formats are retired.

## Play and develop

Windows x64; .NET SDK **10.0.400** and .NET 8 runtime. Setup downloads pinned Godot/tools into ignored `.tools/`, with no global engine installation.

```powershell
.\dev.cmd setup
.\dev.cmd test
.\dev.cmd run
.\dev.cmd export
```

Exports: `.artifacts/windows/`. The exported executable includes its runtime; no Java/development tools required.

`dev.cmd` invokes the repository script with a process-local execution policy; it does not change the machine's PowerShell policy. `smoke`, `sim`, `benchmark` and `capture` are additional developer commands. Export also runs the packaged game's integration check. Distribute the entire export directory, including its `.pck` and runtime folder.

Current slice: **Emberwatch Expedition**, twelve waves across four stages on one map, three tower families and six exclusive specializations. Earn a choice of three relics after waves 3, 6 and 9 from seven behavior/recovery rewards. Optional Overruns offer extra income for holding against forecast Raiders without a breach. Menders sustain formations; armored Captains test specialization; the Bellwarden rallies reinforcements and drops its guard while ringing. At half health it advances and rallies faster.

Start with 180 gold and 12 lives. Click a tower card then a foundation; select towers to inspect, evolve, change priorities or sell. B/P/C selects Boltworks/Prism/Crucible, U opens paths, T cycles targeting, N starts a wave, Space pauses, 1/2/4 sets speed, I reviews relics, Escape closes/cancels, M mutes and R starts a new expedition after a result. While choosing a relic, 1/2/3 selects its card. Path/relic review pauses combat and leaves it paused until you resume. See [play instructions](docs/PLAYABLE_BUILD.md).

Active content lives in `Redesign-Evolution/Game/Content/expedition.json` (format 2). The prior six-wave `emberwatch.json` and three-wave `foundation.json` remain unchanged as regression scenarios. Circuits, additional maps, save/resume, final art and full 25–40-minute runs remain planned. This iteration prioritizes earned progression and encounter pressure over another isolated combat system.

`dev.cmd sim` runs the expedition through ordinary commands. After building, use `dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --balance` for frost/fire/steel and passive controls across seeds 1/7/42; add `--risk` to accept every Overrun. Individual runs accept `--strategy fire --seed 42`. `--roster`, `--evolution` and `--foundation` select the earlier scenarios; `--no-build` verifies defeat. `dev.cmd benchmark` measures reference scans versus spatial queries, not graphics FPS. Automated victories establish feasibility; human difficulty/enjoyment playtesting remains necessary.

## Project

- `Redesign-Evolution/Core`: independent authoritative simulation.
- `Redesign-Evolution/Infrastructure`: content loading/validation.
- `Redesign-Evolution/Game`: Godot presentation/input/audio.
- `Redesign-Evolution/SimRunner`: seeded display-free runs.
- `Redesign-Evolution/Tests`: regression coverage.
- [Progress](docs/DEVELOPMENT_PROGRESS.md), [roadmap](docs/REDESIGN_EVOLUTION_ROADMAP.md), [design](docs/GAME_DESIGN_DOCUMENT.md), [architecture](docs/ARCHITECTURE_DECISION.md).

## History

Original Java source/assets: Git commit `be87036f68d752d98d096f8f7c7f1a3ad6e082d6`. [Inventory](docs/FEATURE_INVENTORY.md) records the audit and baseline evidence; hash/retirement manifests accompany it. No branch switch, history rewrite or remote operation is part of this rebuild. Unrelated local files are preserved.
