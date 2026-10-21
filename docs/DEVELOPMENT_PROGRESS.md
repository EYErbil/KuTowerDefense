# Development progress

Updated 2026-10-21. Branch `Ege-redevelop`. This continuation began clean at `0da2b22` (`logic polish`); its changes are uncommitted. No branch switch, history rewrite, commit, push or remote operation was performed. Unrelated files, dependencies and lockfiles were preserved.

## Current delivery

**Phase 2: expedition intervention, EX-01 through EX-04 complete.** Player feedback identified repetition, weak rewards and superficial difficulty. The active game now loads `expedition.json`: twelve waves/four stages, three earned drafts from seven relics, optional Overruns, Menders, Raiders, armored Captains and a guarded/rallying/enraged Bellwarden. Starting economy, branch payoff and formations were retuned. Godot presentation and the independent C# simulation are retained.

The self-contained Windows package and fresh extraction passed integration checks; packaged reward/boss captures were inspected. **EX-05 human playtest/decision gate is next.** Resonance Circuits remain planned and unimplemented. This is an expedition prototype, not a finished commercial game or validated 25–40-minute run.

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

## Verification

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

## Artifacts

- Launch `.\dev.cmd run`, or `.artifacts/windows/Redesign-Evolution.exe` with neighboring PCK/runtime directory.
- Share `.artifacts/Redesign-Evolution-Emberwatch-Windows.zip`; extract the whole archive and run `windows/Redesign-Evolution.exe`. Current play instructions and Godot/.NET notices are included.
- Verified ZIP: **72,976,047 bytes**, SHA-256 `4634c68129b738f162650eb8ca69a558c0330b9764047109c9706ea076072adb`; adjacent `.zip.sha256` matches.
- `.artifacts/tests/foundation.trx`: 76-test report; historical filename retained.
- `.artifacts/expedition-balance.jsonl`, `expedition-risk.jsonl`: eighteen runs each with wave/resource/tower details. Reproduce with SimRunner `--balance` and `--balance --risk`.
- `.artifacts/expedition-regressions.json`: five earlier CLI scenarios and current digests.
- `.artifacts/expedition-benchmark.json`: dense combat diagnostic, not GPU/FPS measurement.
- `.artifacts/expedition-rewards.png`, `expedition-paths.png`, `expedition-combat.png`, `expedition-boss.png`: source captures. `expedition-packaged-rewards.png` / `expedition-packaged-boss.png`: inspected final extracted build.
- `.artifacts/expedition-export-build.log`, `export-smoke.log`, `expedition-extracted-smoke.log`, matching capture logs and `expedition-verified-extraction.txt`: delivery evidence.

## Findings, limitations and blockers

Full comparison: [EXPEDITION_REWORK.md](EXPEDITION_REWORK.md). Standard witnesses spend 1,320–1,415 gold and finish with 178–388 including final clear/boss income. At least six towers contribute; a rear Ballista can remain unused in a strong frost run. Frost is safer than fire. Three seeds and fixed purchase orders cannot establish universal balance. Base-only failure applies to the published layout, not every possible strategy.

Winning combat takes about 5.5–7.5 minutes at 1×, plus untimed human planning. One map, seven relics and fixed formations limit replayability. Profiles, save/resume, Circuits, Sapper disruption, a second map, accessibility/settings and final art remain open. Procedural art/audio remains prototype quality. Do not pad waves or add permanent bonuses to compensate for weak decisions.

No implementation blocker. Delivery checks are complete. Human observations are the next design input, not a permission requirement for routine fixes or already authorized work.

## Exact next work

1. **EX-05:** record at least three human runs. Note first-relic time, saved/regretted purchases, unclear deaths, repeated/ignored relic choices, priority changes and declined contracts. Tune frost dominance, reward diversity and contract value from evidence; retain useful fire/physical alternatives and meaningful losses.
2. After EX-05, **RE2-04:** implement validated directed links (four-tile range, one outgoing/two incoming, reject cycles/self/duplicates/invalid endpoints), between-wave rewiring and removal on sale. Document upgrade/link persistence. Add one-hop reaction charge, cap three, limit generation to once per source per 60 ticks; no recursive empowered generation.
3. Define useful empowered payloads for every branch, distinguishing them from Split Lens/Barbed Relay/Echo Spindle. These relics occupy part of the earlier circuit concept; revise the signature if it adds no distinct decision. Prove two useful layouts under the expedition economy before expanding it.
4. **RE2-05/RE3-03:** telegraphed disruption and a second authored map once the loop merits them. Additional content and longer runs must create new decisions. Keep the expedition playable and prior regressions passing; update this ledger after each verified slice.
5. Do not switch branches, rewrite history or push. Preserve unrelated files. No further approval is required for the already authorized milestones.

## Original baseline evidence

The original Java audit compiled 42 production classes. Full tests failed on five obsolete map API calls; 47 remaining tests passed. Probes confirmed paused spawning, projectile overshoot, duplicate splash gold, fresh-profile crash and loaded-stat loss. Menus/save rendering were inspected without a full manual playthrough/FPS claim. FEATURE_INVENTORY.md and archival manifests retain the evidence; no repeat audit is needed.
