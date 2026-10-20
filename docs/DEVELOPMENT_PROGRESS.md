# Development progress

Updated 2026-10-20. Branch `Ege-redevelop`. Historical baseline `be87036f68d752d98d096f8f7c7f1a3ad6e082d6`.

## Current delivery

Phase 0 and Phase 1 through **RE1-05 complete**. Emberwatch has three base tower families, three enemy roles, six authored waves, target priorities, threat forecasts and a self-contained Windows package. Phase 2 is next; reactions, branches and Resonance Circuits are not implemented. This remains a short prototype, not a finished roguelike or final balance pass.

## Completed

- RE0-01: original repository/source/assets/history/build audit and proven defects recorded in FEATURE_INVENTORY.md.
- RE0-02: independent vision, architecture, five mechanic proposals, signature design, game design and dependency-aware roadmap.
- RE0-03: 64 original resources hashed; 132 reviewed application/course/Java IDE/code-count files retired. Exact manifests retained; originals remain at the baseline in Git history. General IDE preferences and unrelated `q` preserved.
- RE1-01: pinned Godot 4.7.2 .NET editor/templates, five-project C# solution, game-local export solution, configuration-specific locks, workspace bootstrap, Windows CI configuration and self-contained export.
- RE1-02: fixed 60 Hz simulation, validated commands, cached route, stable ordering/checksums, clamped projectile travel, exact-once rewards, attribution, 64-bit economy, pause/speed/outcome rules and wave-transition clock debt regression.
- RE1-03: first three-wave map/tower/enemy slice, placement/range preview, inspection/selling, HUD, pause/speed, synthesized feedback, win/loss/restart.
- RE1-04: bounded two-tile spatial grid for targeting, splash and escort neighbors; reusable buckets/query buffer; exact inclusive radius checks; explicit ID resolution order. Reference comparisons and reproducible measured benchmark added.
- RE1-05: format-2 tower/enemy catalogs and parallel spawn groups; Boltworks bolts, Prism energy beams, Crucible shells; Runner/Bulwark/Warden roles; armor, non-stacking escort protection, First/Strongest/Support priorities; six teaching waves. Separate silhouettes, attack effects and sounds; shield cues/tooltips; build cards, forecasts and target controls. Shells retain a detonation point if their target dies.
- Original three-wave balance remains in `Game/Content/foundation.json`, converted to the current schema and covered by the regression suite.
- `dev.cmd export` also creates the shareable ZIP and adjacent SHA-256 file after checking the packaged game.

No branch switch, history rewrite, commit, push or remote operation performed. This continuation preserved the existing uncommitted rebuild and unrelated files. No new library dependency was added.

## Original baseline evidence

42 Java production classes compiled. Full tests failed at five old map API calls; map tests were not executed. 47 remaining tests passed. Runtime probes confirmed paused spawning, projectile overshoot, duplicate splash gold, fresh-profile crash and loaded-stat loss. Menu/save rendering inspected; no full original-game manual playthrough or FPS claim.

## Current artifacts and verification

- Launch `.\dev.cmd run`, or `.artifacts/windows/Redesign-Evolution.exe` with its neighboring PCK/runtime directory.
- Share `.artifacts/Redesign-Evolution-Emberwatch-Windows.zip`; extract the whole archive and run `windows/Redesign-Evolution.exe`. Its exact current hash is generated in the adjacent `.zip.sha256`.
- Verified delivery archive: 72,925,402 bytes; SHA-256 `ed1c3185dd6b08e87e6a1f70543d2a96f93ccbd0270b34a4e9f106f0f4bef5e4`.
- `.artifacts/emberwatch.png`: screenshot of the actual packaged mixed-enemy encounter, inspected for layout and threat readability.
- `.artifacts/tests/foundation.trx`: current **38-test** report.
- `.artifacts/run-verification.json`: both encounters' winning and no-defense losing outcomes/checksums.
- `.artifacts/spatial-benchmark.json`: raw timing/allocation report; methodology and durable results in [PERFORMANCE_BASELINE.md](PERFORMANCE_BASELINE.md).
- `.artifacts/export-build.log`, `export-smoke.log`, `export-render.log`: final package and graphical verification.
- Package contains play instructions, Godot/component notices and .NET runtime notices.

| Exercise | Observed result |
|---|---|
| `.\dev.cmd test`, including Debug restore after export | Build succeeds with zero warnings/errors; 38 tests pass, zero failed/skipped |
| Godot scene integration | Actual button/board signal bindings exercise all three cards, target priorities, wave start, pause across engine frames, speed, six-wave victory, defeat, sell/rebuild and repeated restart |
| Six-wave SimRunner, seed 42 | 151 kills, zero leaks, 12 lives, 1,420 gold remaining, 9,260 ticks; checksum `123eda6a556560c4ce12a953d9d421b8eabbfdad88b863990e951422d35c0dd7` |
| Six-wave no-defense scenario | Defeat at tick 4,530, zero lives, 12 leaks |
| Three-wave fixture | 36 kills, zero leaks, 12 lives, 180 gold, 3,004 ticks; checksum `c8437efb5b25b07c6c25d3afe8042573f65ff75915e66e92db4bb78b77fe1eef` |
| Three-wave no-defense scenario | Defeat at tick 4,566, 12 leaks |
| Spatial correctness | 600 query/reference cases over 5,002 enemies; two 2,400-tick reference combats compare events and checksums |
| Headless benchmark | 64 towers, 1,001-2,500 enemies; scan mean 4.11-4.36 ms/tick versus grid 0.88-0.92 ms/tick, identical results |
| `.\dev.cmd export` | Windows package and packaged-game integration pass; ZIP/hash produced |
| Fresh extraction of the final ZIP | Same smoke passes from a path with spaces/Unicode and an unrelated working directory; graphical capture also uses this extracted copy |
| Actual graphical packaged launch | OpenGL / NVIDIA RTX 3050 Laptop; mixed-wave screenshot inspected; clean engine log |

The three-wave end state/timing is preserved. Its checksum changed from the original delivery because the digest now includes catalog IDs, spawn-group position and priority state; hashes across schema/code revisions are not a compatibility promise.

New tests cover simultaneous/delayed spawns and paused schedules, armor versus energy, non-stacking shields, exclusion of Warden-to-Warden protection, immediate shield removal on death, target modes and tie-breaking, catalog/reference errors, nested definition isolation, sold-tower/lost-target shell detonation and complete mixed-roster outcomes. Original pause/projectile/economy/reward/route/frame-rate tests remain.

## Decisions, limitations and blockers

No implementation blocker. Godot remains the presentation layer; the simulation is engine-independent C#. SDK 10.0.400 targets .NET 8; Windows export bundles runtime 8.0.30. Benchmark development runtime is 8.0.0. Review framework support before production release.

The bounded grid is justified by measurements; object pooling and broader engine architecture were not added speculatively. Damage behavior uses explicit attack/damage kinds, not content ID switches. Armor is applied before the strongest live escort reduction, with a minimum of one damage. Tick order and exact rules are in GAME_DESIGN_DOCUMENT.md and ARCHITECTURE_DECISION.md.

The winning witness is about 154 seconds of simulated combat, plus untimed planning in actual play. It does not demonstrate a 25-40-minute run. The economy is intentionally generous while branches/circuits do not exist; the witness's unspent gold is a tuning concern for Phase 2. A successful scripted strategy proves feasibility, not fun, balance or strategy diversity.

Verification uses automated UI signals and actual screenshots, not a full human mouse/keyboard playthrough or subjective audio listening. Shield silhouettes were adjusted after graphical review so overlapping runners cannot hide the escort. No rendered heavy-load FPS, final accessibility, cross-platform or save/resume claim. Art and audio remain original procedural prototypes. CI exists but has not been run by GitHub.

Sandbox-only engine runs can emit a certificate-store access error; normal-access package/render checks complete without it. The initial missing game-local solution and configuration-dependent NuGet lock drift were fixed in the previous delivery. Tools/packages stay ignored and workspace-local. No old map/save importer or course constraint is active.

## Next exact tasks

1. **RE2-01:** extend `Core/Definitions.cs` and the content validator with effect/status, branch and reaction catalogs. Define duration/stack/cap/source/removal policies, validate every reference, and reject invalid content before mutating a run. Keep the current catalogs/three-wave fixture and tests working.
2. **RE2-02:** add a tick-based status store and centralized reaction resolution around the damage pipeline. Implement/test Shatter first (chill consumed by impact), then Ignition (oil consumed by fire, bounded burning area). Test consumption, stacking, source attribution, simultaneous hits and limits against recursive triggers. Do not bury reactions inside tower subclasses.
3. **RE2-03:** implement exclusive branch purchase commands for Ballista/Pitchbow, Rime/Sun Lens and Breach/Echo Mortar; expose only working branches with actual behavior, costs, effects and readable selection UI. Extend engine smoke through purchases and combat.
4. **RE2-04/05:** add the documented acyclic, bounded Resonance Circuits and telegraphed counterplay after reactions/branches pass. See the design document for ports, costs, pulse/charge caps and no-recursion rules.
5. At each slice, run `dev.cmd test`, `smoke`, `sim` and `export`; update this ledger from results. Preserve `foundation.json` as a regression fixture. Use the benchmark when query/combat performance changes, not as an unconditional timing gate.
6. Collect human playtest observations on tower roles, target switching, threat readability and planning decisions; tune the six-wave economy with the new purchase options before expanding into the full run.
