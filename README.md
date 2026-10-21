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

Current slice: **Emberwatch**, one map, six mixed waves, three tower families, six exclusive specializations and three enemy roles. Rime Lens prepares **Shatter** for impact towers; Pitchbow oils enemies for Sun Lens **Ignition**. Ballista pins, Breach Mortar fractures armor, and Echo Mortar adds a delayed second explosion. Wardens protect nearby allies; Support targeting focuses their escort.

Click a tower card then a foundation; select towers to inspect, evolve, change priorities or sell. B/P/C selects Boltworks/Prism/Crucible, U opens the two path choices, T cycles targeting (including Primed to favor prepared reactions), N starts the next wave, Space pauses, 1/2/4 sets speed, Escape closes/cancels, M mutes and R restarts after a result. Opening a path panel pauses combat; choosing or closing leaves it paused until you resume. Essential actions have visible controls. Read the forecast before starting each wave. Circuits, relics, bosses, saves and full-length runs remain planned.

Content lives in `Game/Content/emberwatch.json` (format 2, validated catalogs and spawn groups). The original three-wave slice remains in `foundation.json` as a regression fixture. After building, run `dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --foundation` to reproduce it; add `--no-build` for the no-defense scenario. `dev.cmd benchmark` writes a reference-scan/grid comparison to `.artifacts/spatial-benchmark.json`; these are headless timings, not graphics frame rates.

`dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --evolution` runs the six-path showcase through ordinary commands and reports reaction damage, setup/trigger credit and a reproducible checksum. The default `sim` retains the base-tower strategy for comparison. Neither scripted win is a final balance claim.

## Project

- `Redesign-Evolution/Core`: independent authoritative simulation.
- `Redesign-Evolution/Infrastructure`: content loading/validation.
- `Redesign-Evolution/Game`: Godot presentation/input/audio.
- `Redesign-Evolution/SimRunner`: seeded display-free runs.
- `Redesign-Evolution/Tests`: regression coverage.
- [Progress](docs/DEVELOPMENT_PROGRESS.md), [roadmap](docs/REDESIGN_EVOLUTION_ROADMAP.md), [design](docs/GAME_DESIGN_DOCUMENT.md), [architecture](docs/ARCHITECTURE_DECISION.md).

## History

Original Java source/assets: Git commit `be87036f68d752d98d096f8f7c7f1a3ad6e082d6`. [Inventory](docs/FEATURE_INVENTORY.md) records the audit and baseline evidence; hash/retirement manifests accompany it. No branch switch, history rewrite or remote operation is part of this rebuild. Unrelated local files are preserved.
