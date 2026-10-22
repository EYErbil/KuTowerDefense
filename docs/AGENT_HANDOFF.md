# Agent handoff — start here

Updated 2026-10-21. Read this file first, then the specific design sections needed for the assigned task. No reconstruction of the previous chat or original Java repository is necessary.

## Thirty-second status

| Item | State |
|---|---|
| Branch / session baseline | `Ege-redevelop` / `856cb2d` (`New-Direction`); session began clean |
| Latest owner instruction | Continue developing according to the plan; the earlier documentation-only pause is superseded |
| Accepted next direction | Road warriors, stronger animation, an unlockable frontier map, persistent mastery and useful results graphs |
| Completed new milestone | **FR-01 Road squad kernel**, verified in Core and headless scenarios |
| Next implementation task | **FR-02 Animated squad slice**: actual Garrison controls, reach preview, animation, boss warnings and balance on Emberwatch |
| Then | **FR-03** mission catalog/Stoneford → **FR-04** profile/map unlocks → **FR-05/06** mastery/challenges and graphs → **FR-07** combined evaluation |
| Deferred | Resonance Circuits/Resolve until the squad prototype demonstrates whether either adds a distinct decision |
| Current playable game | Twelve waves, four stages, three tower families/six paths, Shatter/Ignition, seven relics, three drafts, optional Overruns, Menders/Captains/Raiders and Bellwarden |
| Not implemented | Garrison player controls/animation, second map, world map, persistent profile/unlocks, mid-wave saves, final art or Circuits |
| Last recorded verification | 96 tests pass; existing Godot smoke passes with a sandbox certificate-store diagnostic; squad/no-squad command comparisons and earlier regressions pass |
| Current work | FR-01 source/tests/content/docs are local changes; no dependency change, new export, commit or push |

The research/handoff is committed in `856cb2d`; FR-01 source and updated documents are currently local/uncommitted. Preserve them. A separate checkout needs these changes as well as the baseline; the branch name alone does not transfer uncommitted files. The existing Windows ZIP and rendered captures predate FR-01. Normal Godot play still shows three tower cards and does not expose squads yet.

## Read in this order

1. [Road squad kernel](ROAD_SQUAD_KERNEL.md): implemented commands, road reach/travel, combat ordering, provisional values, animation/event contract and measured squad losses.
2. [Frontier campaign proposal](FRONTIER_CAMPAIGN_PROPOSAL.md): accepted direction, maps/progression, research and **FR task acceptance criteria**. FR-01 is checked; FR-02–FR-07 remain pending.
3. [Development progress](DEVELOPMENT_PROGRESS.md): latest evidence, historical package evidence, limitations and next tasks.
4. [Architecture decision](ARCHITECTURE_DECISION.md) and [game design](GAME_DESIGN_DOCUMENT.md): Core/Game boundary and existing combat/economy rules.
5. [Master roadmap](REDESIGN_EVOLUTION_ROADMAP.md): wider scope and release gates. The FR sequence supersedes Circuits-first work.

[EXPEDITION_REWORK.md](EXPEDITION_REWORK.md) contains the existing balance witnesses and their limits. [PLAYABLE_BUILD.md](PLAYABLE_BUILD.md) is the player-facing control guide. The earlier Java audit and retirement manifests are historical reference, not active requirements.

## Next reviewable slice: FR-02

The authoritative squad is implemented. Connect it to the playable game in small patches:

- Add a separate Garrison card and selection/occupancy rendering. It costs 120 gold, occupies one permanent foundation, deploys three Shieldguards and cannot be sold/rebuilt in this prototype. Do not represent it as an ordinary projectile tower.
- Add visible rally and retreat controls plus keyboard equivalents. Snap the preview to the road and call `IsSquadRallyReachable`; command radius is **access distance plus road travel**, not a circular footprint. Use `PlaceGarrison`, `RallySquad` and `RetreatSquad` without moving entities in Godot.
- Render walk, anticipation/contact/recovery, defend, hurt and fall from positions/timers/events. Pause and speed change presentation throughput, never hit rules. Explain that guards recover/regroup only during planning and otherwise have finite health.
- Show each guard's engagement and health, excess enemies passing, and clear unit selection. Basic melee intentionally preserves chill/oil for towers.
- Distinguish the Bellwarden's new 60-tick cleave warning/radius from its existing reinforcement rally warning. It never stops for guards. Captains are blockable but their 40-damage hits defeat an 80-health guard in two blows.
- Extend actual board/control/key smoke coverage and inspect 1×/2×/4× and pause. Compare deliberate Garrison and no-Garrison layouts and purchase timing; the first `--squad` witness is substantially weaker in several cases. Preserve useful alternatives before broadening the roster.

Keep Core's verified death/release, travel and cooldown rules intact. No dead actor may retain a block, attack or pay twice. Export and inspect the next coherent playable slice; no fresh squad package or animation has been verified yet.

Do not start a broad troop roster, Resolve, Circuits, terrain rerouting or an engine migration in this patch. Stoneford/profile unlocks follow this slice.

## Code entry points

| Location | Relevance |
|---|---|
| `Redesign-Evolution/Core/Definitions.cs` / `State.cs` | Validated content, commands, entity state, enemy movement and wave report contracts |
| `Core/Simulation.cs` | Command validation/mutation, authoritative 60 Hz tick, combat, outcomes and checksum |
| `Core/Simulation.Squads.cs` / `SquadDefinitions.cs` / `SquadState.cs` | Garrison/rally/retreat, movement/contact, melee/recovery, events/checksum and validated rules |
| `Core/Simulation.Enemies.cs` | Mender healing, boss warnings/rallies/enrage and scaled enemy definitions |
| `Core/Simulation.Effects.cs` / `EffectState.cs` | Status ownership, reaction consumption, damage attribution, ground/echo rules |
| `Core/Simulation.Expedition.cs` / `ExpeditionDefinitions.cs` | Relic offers/modifiers, contract forecasts and wave rewards |
| `Core/Route.cs` / `EnemySpatialIndex.cs` | One immutable authored route, projection/road reach for guards, bounded enemy queries; no arbitrary rerouting |
| `Game/GameRoot.cs` and partials | Current scene/controller/UI. Loads `expedition.json` directly; stage/reward display assumes the current expedition |
| `Game/ArenaCanvas.cs` and partials / `AudioCues.cs` | Current procedural rendering/events and synthesized audio; presentation has no gameplay authority |
| `Game/Content/expedition.json` | Active balance data. `emberwatch.json` and `foundation.json` are earlier regression inputs |
| `Infrastructure/ExpeditionStrategy.cs` / `SimRunner/ExpeditionRunner.cs` | Published automated command witnesses; not live autoplay |
| `Tests/SquadTests.cs` / `ExpeditionTests.cs` / `EnemyAbilityTests.cs` | Squad kernel cases, full-run reproducibility and prior encounter coverage |
| `Game/SmokeHarness.cs` / repository-root `scripts/dev.ps1` | Actual UI integration and supported build/export workflow |

Paths abbreviated after the first row are relative to `Redesign-Evolution/`, except the explicitly marked repository-root script.

## Commands and environment

Windows/PowerShell. SDK pinned to **10.0.400**, Godot **4.7.2 .NET**, target **net8.0**. Use the repository wrappers; do not change the machine's execution policy. Setup requires the SDK/runtime prerequisites and network access to fetch the pinned engine/templates/packages. It does not install the SDK itself.

```powershell
git status --short
git branch --show-current
.\dev.cmd setup   # only if the workspace-local toolchain is absent
.\dev.cmd test
.\dev.cmd smoke
.\dev.cmd run
```

After building, reproduce the existing strategies:

```powershell
dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --balance
dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --balance --risk
dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --balance --squad
dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --foundation
dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --evolution
```

`--balance` covers seeds 1/7/42 and specialized/passive controls. It returns success when the batch terminates; **inspect individual outcomes**, since controls are meant to lose. `--squad` reserves foundation 3 and attempts recruitment before wave 3, then uses the existing purchase priorities. This opt-in witness reports squad damage/kills/casualties/engagement ticks. Frost wins seeds 1/42, fire wins 7; the other specialized squad witnesses lose. Refine strategies and balance deliberately rather than treating batch completion as a balance pass.

Run `.\dev.cmd benchmark` when combat/query changes warrant profiling. Run `.\dev.cmd export` after the next coherent playable slice; it builds, tests the packaged game and creates the Windows ZIP with notices/play instructions. Inspect actual rendered UI in addition to headless checks. Existing demo/capture flags are implemented in `GameRoot`: `--demo`, `--rewards-demo`, `--boss-demo`, `--paths-demo`, `--capture=<absolute-path>` after the engine's `--` separator. When launching background verification processes on Windows, use hidden windows and bounded timeouts.

Outputs are ignored/local and may not exist in a fresh checkout. `.artifacts/tests/foundation.trx` now records 96 tests; `squad-balance.jsonl`, `squad-no-garrison-balance.jsonl`, `editor-smoke.log` and `spatial-benchmark.json` record this delivery. `.artifacts/windows/` and `Redesign-Evolution-Emberwatch-Windows.zip` are older exports; the progress ledger labels their historical hash/evidence. If a sandbox prevents NuGet auditing, use the environment's permission mechanism or report the limitation; do not suppress audits. Current smoke passed despite a sandbox root certificate-store diagnostic.

## Important design limits

- Blocking interacts with slow/area damage, but the first squad purchase plan loses more often than its no-Garrison equivalents. Automatic wins establish solvability, not enjoyment or universal balance. Review the recorded losses before tuning or claiming FR-02 complete.
- Current combat witnesses last roughly 5.5–7.5 minutes plus planning. The 25–40-minute full-run target is not achieved. Do not pad the campaign by chaining duplicate twelve-wave encounters.
- Profile/unlock persistence does not exist. Before advertising it, add versioning, atomic writes, recovery and exact-once award handling. Profile progress and mid-wave saves are separate features.
- A second authored single-route map is feasible without general pathfinding. Multiple entrances, player-built walls and arbitrary rerouting need more design.
- Mastery/Renown should create new options and clear goals. The claim that grind attracts more players remains unproven; no infinite stat treadmill or player-growth promise is part of the accepted plan.
- Field Standards/Resolve, extra regions, more squad classes and online challenges are experiments or later candidates. Do not silently promote them into the first deliverable.
- Collect EX-05 human observations alongside development. Their absence does not block bounded prototypes, but it does prevent claiming the next design is proven fun or balanced.

## Paste this to the next implementation agent

> Continue REDESIGN: EVOLUTION on `Ege-redevelop`. Read `AGENTS.md`, `docs/AGENT_HANDOFF.md`, `docs/ROAD_SQUAD_KERNEL.md` and the FR checklist. FR-01 is implemented with 96 passing tests; normal Godot play still lacks squad controls/animation. Preserve the current uncommitted changes and verify the baseline, then implement FR-02 Garrison selection/rally/retreat UI, route-based reach preview, state-driven animation and readable boss cleave warnings. Compare deliberate squad/no-squad builds; current headless squad purchase experiments have documented losses. Extend engine smoke, inspect actual rendering/speed/pause and export the coherent slice. Keep gameplay in Core and update progress/handoff/checklist with actual results. Stoneford/profile unlocks follow; Circuits remain deferred. Do not switch branches, rewrite history or push.
