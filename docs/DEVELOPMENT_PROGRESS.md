# Development progress

Updated 2026-10-21. Branch `Ege-redevelop`. The expedition is in `3748e0d`; the direction/handoff was subsequently committed as `856cb2d`. This implementation session began clean at `856cb2d` after the owner requested development to continue. No branch switch, history rewrite, commit, push or remote operation was performed. Dependencies, lockfiles and unrelated files remain unchanged.

**Continuing agent: read [AGENT_HANDOFF.md](AGENT_HANDOFF.md) first. FR-01 is complete; FR-02 is next.** Squad combat is implemented in Core/headless scenarios. Garrison controls/animation are not yet exposed in normal Godot play.

## Latest delivery — FR-01 road squad kernel

The [frontier plan](FRONTIER_CAMPAIGN_PROPOSAL.md) remains road squad → animation → second map/profile unlocks → mastery/challenges/graphs. FR-00 and FR-01 are complete; FR-02–FR-07 remain pending. [ROAD_SQUAD_KERNEL.md](ROAD_SQUAD_KERNEL.md) records exact commands, values, tick ordering and unresolved balance questions.

- Implemented optional validated Garrison/guard/melee definitions; one permanent 120-gold foundation and three Shieldguards. Shared occupancy and invalid commands are atomic.
- Added nearest-road access, path-distance rally bounds, road travel/retreat, swept contact, finite one-to-one engagements, attack windups/cooldowns, guard deaths and immediate release. Commands preserve health/cooldowns; no mid-wave recovery. All guards fully heal/revive/regroup on entering planning, with stable identity/statistics.
- Captains are blockable and defeat full-health guards in two hits; Bellwarden moves through troops and warns before a nearby cleave. Melee uses existing armor/shield/bounty rules and separate guard/Garrison credit without consuming tower setup or triggering reactions. Events/state expose the future animation contract.
- Added checksums and 20 squad test cases, plus opt-in CLI `--squad` witnesses with contribution/casualty/engagement reports. No new library, general entity framework or UI implementation.
- Current changes are local/uncommitted. Include source and docs when handing work to another checkout. The existing Windows ZIP predates FR-01; no export or new rendered squad capture was produced.

| Current verification | Result |
|---|---|
| Baseline before edits | 76 passing tests; initial sandbox NU1900 warnings resolved by a locked restore from the official NuGet feed; audits remained enabled |
| `dev.cmd test` after FR-01 | Zero build warnings/errors; **96 passed**, zero failed |
| `dev.cmd smoke` | Existing twelve-wave UI flow passes; sandbox logs a root certificate-store diagnostic. No squad UI is claimed |
| Squad correctness | Atomic orders/occupancy, path reach/corners, fast contact/capacity, death/recovery, exact damage/bounty credit, status preservation, Captain attrition, boss warning/escape, pause/speed and grid/reference comparisons |
| Complete squad expedition | Seed-42 frost reproduces under direct ticks, reference queries and 144 FPS/4×; wave and damage/kill totals reconcile |
| Baseline balance batch | All 18 scenarios terminate; frost/fire/steel retain preceding winning life totals across seeds 1/7/42; controls retain their losses |
| Squad balance batch | All 18 terminate; frost wins seeds 1/42, fire wins 7, remaining specialized witnesses lose. This early purchase/layout is not balanced; details in kernel report |
| Prior fixtures | Three-wave: 3,004 ticks, 36 kills, zero leaks, 180 gold. Six-wave evolution: 9,060 ticks, 151 kills, zero leaks, 975 gold |
| Dense diagnostic | Six matching grid/reference digests; grid mean 1.064–1.204 ms/tick, 5,058 bytes/tick. Measures shared kernel overhead without deployed squads, not FPS or squad animation |

Current local evidence: `.artifacts/tests/foundation.trx` (now 96 cases), `.artifacts/squad-balance.jsonl`, `.artifacts/squad-no-garrison-balance.jsonl`, `.artifacts/editor-smoke.log` and `.artifacts/spatial-benchmark.json`. The historical expedition evidence below is retained separately.

## Earlier playable expedition delivery

**Phase 2: expedition intervention, EX-01 through EX-04 complete.** Player feedback identified repetition, weak rewards and superficial difficulty. The active game now loads `expedition.json`: twelve waves/four stages, three earned drafts from seven relics, optional Overruns, Menders, Raiders, armored Captains and a guarded/rallying/enraged Bellwarden. Starting economy, branch payoff and formations were retuned. Godot presentation and the independent C# simulation are retained.

The preceding self-contained Windows package and fresh extraction passed integration checks; packaged reward/boss captures were inspected. EX-05 human observations remain a parallel evaluation gate. Resonance Circuits are deferred for reassessment after the squad/map work. This is an expedition prototype, not a finished commercial game or validated 25–40-minute run. The package/capture evidence below is historical; the current kernel has not been re-exported.

## Completed work

- Earlier RE0-01/02/03: repository/source/assets/history/build audit, independent vision/architecture, five mechanic proposals, reviewed academic/Java retirement. Hashes cover 64 original resources; 132 reviewed legacy files were retired in prior work. Source/assets remain at `be87036f68d752d98d096f8f7c7f1a3ad6e082d6`. General IDE preferences and unrelated `q` were preserved.
- Earlier RE1-01 through RE1-05: pinned tools/locks, pure 60 Hz simulation, commands/checksums, three-wave slice, spatial grid/reference comparisons, three tower families and six-wave roster. Placement/refunds, pause/speed, targeting, source credit, armor and escort shielding were verified.
- Earlier RE2-01/02/03: six exclusive branches, stack ownership/expiry, Shatter/Ignition, pin/fracture, Primed targeting, persistent source credit, path UI/silhouettes/audio, bounded echoes/ground effects and measured removal of hot-path closure allocation.
- EX-01: seeded drafts after waves 3/6/9; distinct offers excluding ownership; build/recovery preferences; atomic claim and advance block; current/future attack modifiers without compounding; collection review and fresh seeds. Recovery pays once; forks hit at most one extra target; echoes/reactions remain nonrecursive.
- EX-02: authored formations, clear supplies, health scaling, healing windup/interrupt, boss vulnerability/finite zero-bounty rallies/latched enrage, exact reinforcement forecasts and flawless-only bonuses. Silhouettes, telegraphs, boss bar and audio expose the rules. A shared count function prevents preview/spawn disagreement.
- EX-03: published frost/fire/physical command witnesses plus base-only, fixed-opening and no-defense controls. Standard and all-Overrun comparisons across seeds 1/7/42 retain wave income/leaks and tower contribution. Evidence corrected an overhard opening and overly generous contracts.
- EX-04: reward cards/keyboard choices, collection/seed review, stage progress, Overrun controls, relic-aware path tooltips and expanded result summary. Actual board/button/key integration checks full victory, rewards, boss phases, defeat and restart. Rendered inspection caught and fixed overflowing card descriptions; package portability verified.
- Added 22 tests beyond the existing 54. No new library, general ECS, modifier scripting language, object pool or service was introduced.

The prior `emberwatch.json` and `foundation.json` files are unchanged this session. They remain regression scenarios and do not constrain active design. Academic requirements and legacy formats remain retired.

## Earlier expedition verification

Toolchain: SDK 10.0.400, Godot 4.7.2 .NET, net8.0. Export bundles .NET 8.0.30; diagnostic development runtime reports 8.0.0. Review framework support before release. Locked NuGet restore/audit refreshed successfully with normal network access after initial sandbox NU1900 warnings; audits were not disabled.

| Exercise | Observed result |
|---|---|
| `.\dev.cmd test` | Zero build warnings/errors; 76 passed, zero failed/skipped |
| Engine/export integration | Actual build/priority/path/reward/contract bindings; pause across frames and relic review; twelve-wave victory; three choices; boss rally/enrage; defeat; sale/rebuild; fresh seeds/restart |
| Standard expedition, seeds 1/7/42 | Frost, fire and physical each win all three; remaining lives 11–12, 5–7 and 10–12 respectively |
| Passive controls | Fixed opening loses wave 3; no defense wave 2; tested base-only layout waves 10–11 |
| Every Overrun, seeds 1/7/42 | Frost/fire finish with breaches; physical loses wave 5; extra income changes purchase timing |
| Replay/reference | Complete expedition digests agree for direct ticks, 144 FPS/4× and reference queries; drafts vary across seeds and exclude owned relics |
| New mechanics | Atomic rewards; all seven relic kinds; bounded forks/setup; current/future/upgraded towers; recovery reset; flawless/failed contracts; stage-boundary forecast agreement; healing timing/caps/death; boss guard/pause/enrage/summon caps; invalid definitions |
| Earlier six-wave roster | Unchanged: 151 kills, 0 leaks, 12 lives, 1,420 gold, 9,260 ticks |
| Earlier six-wave evolution | Unchanged: 151 kills, 0 leaks, 12 lives, 975 gold, 9,060 ticks |
| Earlier three-wave fixture | Unchanged: 36 kills, 0 leaks, 12 lives, 180 gold, 3,004 ticks |
| Earlier no-defense controls | Six-wave defeat at tick 4,530; three-wave defeat at 4,566; both 12 leaks |
| Dense grid/reference diagnostic | Six matching digests; grid mean 1.315–1.543 ms/tick, 5,034 bytes/tick; load/method caveats in PERFORMANCE_BASELINE.md |
| Fresh final ZIP extraction | Smoke passes from a path with spaces/Unicode and unrelated working directory; packaged reward/boss captures inspected, clean engine logs |
| Rendered launch | OpenGL / NVIDIA RTX 3050 Laptop, 1440×900; reward, path, healing-windup and boss layouts reviewed |

Checksums changed because ability/reward/report state is included. Cross-revision equality is not a compatibility promise. Earlier spatial/effect tests retain per-tick event comparisons. Human enjoyment, final balance and a manual full playthrough are not established by the integration harness.

## Earlier expedition artifacts

- Launch `.\dev.cmd run`, or `.artifacts/windows/Redesign-Evolution.exe` with neighboring PCK/runtime directory.
- Share `.artifacts/Redesign-Evolution-Emberwatch-Windows.zip`; extract the whole archive and run `windows/Redesign-Evolution.exe`. Current play instructions and Godot/.NET notices are included.
- Verified ZIP: **72,976,047 bytes**, SHA-256 `4634c68129b738f162650eb8ca69a558c0330b9764047109c9706ea076072adb`; adjacent `.zip.sha256` matches.
- `.artifacts/tests/foundation.trx`: historical filename reused; the latest run now contains 96 cases, not the earlier 76.
- `.artifacts/expedition-balance.jsonl`, `expedition-risk.jsonl`: eighteen runs each with wave/resource/tower details. Reproduce with SimRunner `--balance` and `--balance --risk`.
- `.artifacts/expedition-regressions.json`: five earlier CLI scenarios and current digests.
- `.artifacts/expedition-benchmark.json`: dense combat diagnostic, not GPU/FPS measurement.
- `.artifacts/expedition-rewards.png`, `expedition-paths.png`, `expedition-combat.png`, `expedition-boss.png`: source captures. `expedition-packaged-rewards.png` / `expedition-packaged-boss.png`: inspected final extracted build.
- `.artifacts/expedition-export-build.log`, `export-smoke.log`, `expedition-extracted-smoke.log`, matching capture logs and `expedition-verified-extraction.txt`: delivery evidence.

## Findings, limitations and blockers

Full comparison: [EXPEDITION_REWORK.md](EXPEDITION_REWORK.md). Standard witnesses spend 1,320–1,415 gold and finish with 178–388 including final clear/boss income. At least six towers contribute; a rear Ballista can remain unused in a strong frost run. Frost is safer than fire. Three seeds and fixed purchase orders cannot establish universal balance. Base-only failure applies to the published layout, not every possible strategy.

Winning combat takes about 5.5–7.5 minutes at 1×, plus untimed human planning. One map, seven relics and fixed formations limit replayability. Profiles, save/resume, Circuits, Sapper disruption, a second map, accessibility/settings and final art remain open. Procedural art/audio remains prototype quality. Do not pad waves or add permanent bonuses to compensate for weak decisions.

No implementation blocker. FR-01 ends at the independently testable kernel boundary. UI/animation and deliberate Garrison balance are the next milestone. Human observations remain necessary design evidence; their absence does not block bounded prototypes. The sandbox certificate-store diagnostic does not fail the existing smoke flow but prevents calling its engine log clean.

## Exact next work

1. **FR-02 controls:** inspect/preserve FR-01 changes and read the kernel contract. Add Garrison card/selection/occupancy, route-based reach preview, rally/retreat controls and clear permanent-foundation/recovery information. Wire commands to Core; no duplicated combat in Godot.
2. **FR-02 animation/balance:** render walk/anticipation/contact/recovery/defend/hurt/fall and distinct cleave warnings. Inspect 1×/2×/4× and pause, extend actual UI smoke, compare deliberate Garrison/no-Garrison strategies, then export the coherent slice. Current `--squad` losses are documented evidence, not a reason to assert balance or blindly buff permanent power.
3. **FR-03 → FR-04:** introduce the mission catalog and distinct Stoneford battlefield, then the frontier UI and versioned persistent unlocks. Verify clear → unlock → application restart, recovery and duplicate-award prevention. Profile progress is separate from mid-wave saves.
4. **FR-05 / FR-06 → FR-07:** add one alternative doctrine/challenge and reconciled local result graphs; evaluate the combined slice before expanding content. Full acceptance criteria and dependencies live in the frontier checklist.
5. **EX-05 alongside these slices:** record at least three human runs. Note first-relic time, saved/regretted purchases, unclear deaths, repeated/ignored relic choices, priority changes and declined contracts. Tune frost dominance and rewards from evidence. Reassess Circuits/Resolve after squad/map evaluation; neither is the next implementation task.
6. Keep the expedition playable and prior regressions passing; update the progress, checklist and handoff after each verified slice. Stay on `Ege-redevelop`, preserve unrelated work and do not rewrite history or push. The assigned implementer can make routine decisions within this scope without another approval round.

## Original baseline evidence

The original Java audit compiled 42 production classes. Full tests failed on five obsolete map API calls; 47 remaining tests passed. Probes confirmed paused spawning, projectile overshoot, duplicate splash gold, fresh-profile crash and loaded-stat loss. Menus/save rendering were inspected without a full manual playthrough/FPS claim. FEATURE_INVENTORY.md and archival manifests retain the evidence; no repeat audit is needed.
