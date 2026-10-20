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

Current slice: **Emberwatch**, one map, six mixed waves, three tower families and three enemy roles. Boltworks covers the road, Prism beams bypass Bulwark armor, and Crucible shells damage groups. Wardens protect nearby allies; select a tower and switch to Support targeting to focus them.

Click a tower card then a foundation; select towers to inspect, change priorities or sell. B/P/C selects Boltworks/Prism/Crucible, T cycles targeting, N starts the next wave, Space pauses, 1/2/4 sets speed, Escape cancels, M mutes and R restarts after a result. Essential actions have visible controls. Read the forecast before starting each wave. Reactions/circuits/upgrades/saves are planned, not implemented here.

Content lives in `Game/Content/emberwatch.json` (format 2, validated catalogs and spawn groups). The original three-wave slice remains in `foundation.json` as a regression fixture. After building, run `dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --foundation` to reproduce it; add `--no-build` for the no-defense scenario. `dev.cmd benchmark` writes a reference-scan/grid comparison to `.artifacts/spatial-benchmark.json`; these are headless timings, not graphics frame rates.

## Project

- `Redesign-Evolution/Core`: independent authoritative simulation.
- `Redesign-Evolution/Infrastructure`: content loading/validation.
- `Redesign-Evolution/Game`: Godot presentation/input/audio.
- `Redesign-Evolution/SimRunner`: seeded display-free runs.
- `Redesign-Evolution/Tests`: regression coverage.
- [Progress](docs/DEVELOPMENT_PROGRESS.md), [roadmap](docs/REDESIGN_EVOLUTION_ROADMAP.md), [design](docs/GAME_DESIGN_DOCUMENT.md), [architecture](docs/ARCHITECTURE_DECISION.md).

## History

Original Java source/assets: Git commit `be87036f68d752d98d096f8f7c7f1a3ad6e082d6`. [Inventory](docs/FEATURE_INVENTORY.md) records the audit and baseline evidence; hash/retirement manifests accompany it. No branch switch, history rewrite or remote operation is part of this rebuild. Unrelated local files are preserved.
