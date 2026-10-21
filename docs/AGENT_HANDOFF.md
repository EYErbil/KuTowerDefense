# Agent handoff — start here

Updated 2026-10-21. Read this file first, then the specific design sections needed for the assigned task. No reconstruction of the previous chat or original Java repository is necessary.

## Thirty-second status

| Item | State |
|---|---|
| Branch / implementation baseline | `Ege-redevelop` / `3748e0d` (`better path - map - tower, and narrative`) |
| Latest owner instruction | Documentation handoff only for this session; the colleague and their agent will implement |
| Accepted next direction | Road warriors, stronger animation, an unlockable frontier map, persistent mastery and useful results graphs |
| First implementation task | **FR-01 Road squad kernel**, then **FR-02 Animated squad slice**, both on existing Emberwatch |
| Then | **FR-03** mission catalog/Stoneford → **FR-04** profile/map unlocks → **FR-05/06** mastery/challenges and graphs → **FR-07** combined evaluation |
| Deferred | Resonance Circuits/Resolve until the squad prototype demonstrates whether either adds a distinct decision |
| Current playable game | Twelve waves, four stages, three tower families/six paths, Shatter/Ignition, seven relics, three drafts, optional Overruns, Menders/Captains/Raiders and Bellwarden |
| Not implemented | Friendly troops/Garrison, second map, world map, persistent profile/unlocks, mid-wave saves, final art or Circuits |
| Last recorded verification | 76 tests pass; packaged integration and fresh ZIP extraction pass; rendered reward/boss screens inspected |
| Current handoff changes | Documentation only; no gameplay implementation, dependency changes, new build or new test run |

The research/proposal documents were already uncommitted when this handoff began. Preserve them. These handoff files also remain local/uncommitted unless a later owner/colleague action commits them. A separate checkout will need the documentation changes as well as the implementation baseline; the branch name alone does not transfer uncommitted files. No commit or push was made by this handoff session.

## Read in this order

1. [Frontier campaign proposal](FRONTIER_CAMPAIGN_PROPOSAL.md): accepted direction, concrete squad/map/progression rules, source research and **FR task acceptance criteria**. The filename says proposal because prices, timing, later content and experiments remain provisional; acceptance of the direction does not mean the features exist.
2. [Development progress](DEVELOPMENT_PROGRESS.md): implementation evidence, known limitations, artifacts and current next tasks.
3. [Architecture decision](ARCHITECTURE_DECISION.md): Core/Game boundary, tick ordering, validation, reproducibility and export setup.
4. [Game design](GAME_DESIGN_DOCUMENT.md): current combat/economy rules. Read before changing interactions or balance.
5. [Master roadmap](REDESIGN_EVOLUTION_ROADMAP.md): wider scope and release gates. The FR sequence supersedes the older instruction to implement Circuits immediately after EX-05.

[EXPEDITION_REWORK.md](EXPEDITION_REWORK.md) contains the existing balance witnesses and their limits. [PLAYABLE_BUILD.md](PLAYABLE_BUILD.md) is the player-facing control guide. The earlier Java audit and retirement manifests are historical reference, not active requirements.

## First reviewable patch: FR-01

Implement a bounded road squad in the engine-independent simulation before expanding the campaign or adding more systems:

- One purchasable Garrison per battlefield uses a foundation and run gold; it deploys three Shieldguards. Define and validate its content representation rather than pretending it is an ordinary damaging projectile tower.
- Rally to a reachable road position inside a command radius; move there over time. Retreat/reposition releases engagements and costs travel time. Reject invalid commands without spending or partial mutation.
- A guard engages at most one ordinary enemy; excess enemies pass. Enemies fight back, health is finite, deaths release both sides, and unsupported troops cannot block forever.
- Fallen guards return in planning, not through an endless mid-wave respawn loop. Specify the surviving guards' between-wave healing rule before coding it.
- Specify boss and Captain responses explicitly. The boss cannot be fully immobilized; the visible dangerous attack belongs in FR-02. Document provisional damage, health, range and timing values so they can be tuned.
- Basic melee must not consume chill/oil setup through the ordinary reaction-trigger path. Any later specialist that consumes setup must opt into that behavior explicitly.
- Extend checksums/events and meaningful tests for engagements, deaths, valid/invalid rally and retreat, pause/speed, stable ordering and wave termination. Protect the existing no-troop fixtures.

Decide the new combat tick ordering deliberately: engagement can affect movement, but no dead guard/enemy may retain a block, attack or reward twice. Preserve tower/area damage credit. Keep the change small enough to inspect and run headlessly before connecting the UI.

FR-02 adds actual selection/rally controls, a reach preview, walk/attack/defend/hurt/fall animation and boss tells. Compare useful Garrison and no-Garrison strategies. Do not start a broad troop roster, Resolve, Circuits, terrain rerouting or an engine migration in the same patch.

## Code entry points

| Location | Relevance |
|---|---|
| `Redesign-Evolution/Core/Definitions.cs` / `State.cs` | Validated content, commands, entity state, enemy movement and wave report contracts |
| `Core/Simulation.cs` | Command validation/mutation, authoritative 60 Hz tick, combat, outcomes and checksum |
| `Core/Simulation.Enemies.cs` | Mender healing, boss warnings/rallies/enrage and scaled enemy definitions |
| `Core/Simulation.Effects.cs` / `EffectState.cs` | Status ownership, reaction consumption, damage attribution, ground/echo rules |
| `Core/Simulation.Expedition.cs` / `ExpeditionDefinitions.cs` | Relic offers/modifiers, contract forecasts and wave rewards |
| `Core/Route.cs` / `EnemySpatialIndex.cs` | One immutable authored route and bounded enemy queries; no friendly navigation/engagement system yet |
| `Game/GameRoot.cs` and partials | Current scene/controller/UI. Loads `expedition.json` directly; stage/reward display assumes the current expedition |
| `Game/ArenaCanvas.cs` and partials / `AudioCues.cs` | Current procedural rendering/events and synthesized audio; presentation has no gameplay authority |
| `Game/Content/expedition.json` | Active balance data. `emberwatch.json` and `foundation.json` are earlier regression inputs |
| `Infrastructure/ExpeditionStrategy.cs` / `SimRunner/ExpeditionRunner.cs` | Published automated command witnesses; not live autoplay |
| `Tests/ExpeditionTests.cs` / `EnemyAbilityTests.cs` | Newest behavior/complete-run tests; other test files protect the prior kernel |
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
dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --foundation
dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --evolution
```

`--balance` covers seeds 1/7/42 and specialized/passive controls. It returns success when the batch terminates; **inspect individual outcomes**, since controls are meant to lose. Add troop witnesses for new behavior rather than calling an unchanged batch a test of troops.

Run `.\dev.cmd benchmark` when combat/query changes warrant profiling. Run `.\dev.cmd export` after the next coherent playable slice; it builds, tests the packaged game and creates the Windows ZIP with notices/play instructions. Inspect actual rendered UI in addition to headless checks. Existing demo/capture flags are implemented in `GameRoot`: `--demo`, `--rewards-demo`, `--boss-demo`, `--paths-demo`, `--capture=<absolute-path>` after the engine's `--` separator. When launching background verification processes on Windows, use hidden windows and bounded timeouts.

Outputs are ignored/local: `.artifacts/windows/`, `.artifacts/Redesign-Evolution-Emberwatch-Windows.zip`, `.artifacts/tests/foundation.trx`. They may not exist in a fresh checkout. The detailed progress ledger records the last package hash and measured results; do not claim those checks were rerun after a new change. If a sandbox prevents the NuGet audit/network restore, use the environment's permission mechanism or report the actual limitation; do not suppress audits to claim a clean build.

## Important design limits

- Frost is safer than fire in the existing three-seed sample. Blocking may amplify that imbalance. Automatic wins establish solvability, not enjoyment or universal balance.
- Current combat witnesses last roughly 5.5–7.5 minutes plus planning. The 25–40-minute full-run target is not achieved. Do not pad the campaign by chaining duplicate twelve-wave encounters.
- Profile/unlock persistence does not exist. Before advertising it, add versioning, atomic writes, recovery and exact-once award handling. Profile progress and mid-wave saves are separate features.
- A second authored single-route map is feasible without general pathfinding. Multiple entrances, player-built walls and arbitrary rerouting need more design.
- Mastery/Renown should create new options and clear goals. The claim that grind attracts more players remains unproven; no infinite stat treadmill or player-growth promise is part of the accepted plan.
- Field Standards/Resolve, extra regions, more squad classes and online challenges are experiments or later candidates. Do not silently promote them into the first deliverable.
- Collect EX-05 human observations alongside development. Their absence does not block bounded prototypes, but it does prevent claiming the next design is proven fun or balanced.

## Paste this to the next implementation agent

> Continue REDESIGN: EVOLUTION on `Ege-redevelop`. Read `AGENTS.md`, `docs/AGENT_HANDOFF.md` and the FR checklist in `docs/FRONTIER_CAMPAIGN_PROPOSAL.md`. The twelve-wave expedition is implemented; the frontier/squad direction is accepted but unimplemented. Inspect and preserve current work, verify the baseline, then implement FR-01 Road squad kernel as a small independently testable change. Follow with FR-02 animated squad controls on Emberwatch; Stoneford/profile unlocks are the next slice. Keep gameplay in Core, preserve deterministic behavior and existing fixtures, document provisional balance choices, and update the progress/handoff/checklist with actual results. Reevaluate Circuits later. Do not switch branches, rewrite history or push.
