# Development progress

Updated 2026-10-20. Branch `Ege-redevelop`. Historical Java baseline `be87036f68d752d98d096f8f7c7f1a3ad6e082d6`. This continuation began clean at `ec3270d` (the preceding Godot foundation had been committed); the Phase 2 changes below are uncommitted.

## Current delivery

Phase 0/1 and **RE2-01, RE2-02, RE2-03 complete**. Emberwatch now has six exclusive tower specializations, Shatter/Ignition, stack-based chill/oil/pin/fracture, burning ground, delayed Echo, Primed targeting and source-aware reaction statistics. The actual upgrade UI pauses combat, exposes costs/mechanics and preserves the chosen path. The six-wave defense and Windows package remain playable. **RE2-04 Resonance Circuits is next and is not implemented.** This remains a short prototype, not a finished roguelike or final balance pass.

## Completed

- RE0-01: original repository/source/assets/history/build audit and proven defects recorded in FEATURE_INVENTORY.md.
- RE0-02: independent vision, architecture, five mechanic proposals, signature design, game design and dependency-aware roadmap.
- RE0-03: 64 original resources hashed; 132 reviewed application/course/Java IDE/code-count files retired. Exact manifests retained; originals remain at the baseline in Git history. General IDE preferences and unrelated `q` preserved.
- RE1-01: pinned Godot 4.7.2 .NET editor/templates, five-project C# solution, game-local export solution, configuration-specific locks, workspace bootstrap, Windows CI configuration and self-contained export.
- RE1-02: fixed 60 Hz simulation, validated commands, cached route, stable ordering/checksums, clamped projectile travel, exact-once rewards, attribution, 64-bit economy, pause/speed/outcome rules and wave-transition clock debt regression.
- RE1-03: first three-wave map/tower/enemy slice, placement/range preview, inspection/selling, HUD, pause/speed, synthesized feedback, win/loss/restart.
- RE1-04: bounded two-tile spatial grid for targeting, splash and escort neighbors; reusable buckets/query buffer; exact inclusive radius checks; explicit ID resolution order. Reference comparisons and reproducible measured benchmark added.
- RE1-05: format-2 tower/enemy catalogs and parallel spawn groups; Boltworks bolts, Prism energy beams, Crucible shells; Runner/Bulwark/Warden roles; armor, non-stacking escort protection, First/Strongest/Support priorities; six teaching waves. Separate silhouettes, attack effects and sounds; shield cues/tooltips; build cards, forecasts and target controls. Shells retain a detonation point if their target dies.
- RE2-01: optional combat catalog for statuses, reactions, branches and echoes; validated references, exclusive family choices, stack/slow/area/timing limits and ambiguous-trigger rejection. No new package or tower subclass hierarchy.
- RE2-02: independent stack ownership/expiry, oldest-first consumption/replacement, strongest slow, armor fracture, nonrecursive Shatter and bounded Ignition. Staggered overlapping fire shares each enemy's damage cadence. Damage/kill credit survives tower sale; setup and trigger credit remain distinct. Separate query buffers support reactions nested inside splash resolution.
- RE2-03: Ballista/Pitchbow, Rime/Sun Lens and Breach/Echo Mortar; atomic purchases and total-investment refunds; cooldown, priority and in-flight payload preservation. Exclusive path cards, affordability/tooltips, pause-on-open/explicit resume, six branch silhouettes, frost/fire/status/ground/echo cues, synthesized upgrade/reaction sounds and post-run reaction totals. Primed targeting avoids losing prepared targets merely because chill reduces their path progress.
- Added an authored `EvolutionShowcase` for CLI/capture/integration commands and 16 tests beyond the preceding 38. The showcase purchases all six paths and exercises both reactions; it is an opt-in verification aid, not live gameplay automation.
- Fixed a measured hot-path allocation regression in status expiry/lookup with bounded iteration. No speculative pooling was introduced.
- Original three-wave balance remains in `Game/Content/foundation.json`, converted to the current schema and covered by the regression suite.
- `dev.cmd export` also creates the shareable ZIP and adjacent SHA-256 file after checking the packaged game.

No branch switch, history rewrite, commit, push or remote operation performed by this continuation. Existing history and unrelated files were preserved. No new library dependency was added.

## Original baseline evidence

42 Java production classes compiled. Full tests failed at five old map API calls; map tests were not executed. 47 remaining tests passed. Runtime probes confirmed paused spawning, projectile overshoot, duplicate splash gold, fresh-profile crash and loaded-stat loss. Menu/save rendering inspected; no full original-game manual playthrough or FPS claim.

## Current artifacts and verification

- Launch `.\dev.cmd run`, or `.artifacts/windows/Redesign-Evolution.exe` with its neighboring PCK/runtime directory.
- Share `.artifacts/Redesign-Evolution-Emberwatch-Windows.zip`; extract the whole archive and run `windows/Redesign-Evolution.exe`. Its exact current hash is generated in the adjacent `.zip.sha256`.
- Verified delivery archive: 72,950,901 bytes; SHA-256 `dff8383f946cad11b648be31eacb9811779a0c9a0a91f1feabee2c4f36950d85`.
- `.artifacts/emberwatch.png` and `evolution-paths.png`: screenshots from the freshly extracted package, inspected for combat cues and path-card layout. The demonstration advances ordinary commands and shows only the current tick's effects, not a backlog from fast-forwarding.
- `.artifacts/tests/foundation.trx`: current **54-test** report.
- `.artifacts/run-verification.json`: base-roster, evolution and foundation wins, plus both no-defense losses/checksums.
- `.artifacts/spatial-benchmark.json`: raw timing/allocation report; methodology and durable results in [PERFORMANCE_BASELINE.md](PERFORMANCE_BASELINE.md).
- `.artifacts/export-build.log`, `export-smoke.log`, `extracted-smoke.log`, `export-demo.log`, `export-paths-demo.log`: final package and graphical verification.
- Package contains play instructions, Godot/component notices and .NET runtime notices.

| Exercise | Observed result |
|---|---|
| `.\dev.cmd test` | Build succeeds with zero warnings/errors; 54 tests pass, zero failed/skipped |
| Godot scene integration | Actual button/board signal bindings exercise all build cards, all four priorities, unaffordable path inspection, open/close tactical pause across engine frames, six branch purchases, both reactions, speed, six-wave victory, defeat, sell/rebuild and repeated restart |
| Six-wave base-tower SimRunner, seed 42 | Unchanged outcome: 151 kills, zero leaks, 12 lives, 1,420 gold, 9,260 ticks; checksum `8729afa8b97330403914c8db6b1f123a3217da1ff081ec81d6dfd82b2087dfbe` |
| `SimRunner --evolution`, seed 42 | 151 kills, zero leaks, 12 lives, 975 gold, 9,060 ticks; 25 Shatters / 1,210 damage and 35 Ignitions / 2,339 damage; checksum `d3978db7a88ff273cc6bb019c49bf387b0e8454a140460362c23fd7e517635a2` |
| Six-wave no-defense scenario | Defeat at tick 4,530, zero lives, 12 leaks |
| Three-wave fixture | Unchanged outcome: 36 kills, zero leaks, 12 lives, 180 gold, 3,004 ticks; checksum `bf4c72689c8d45acc749926ef307a798929ac128946a565afadd6b484c241d16` |
| Three-wave no-defense scenario | Defeat at tick 4,566, 12 leaks |
| Spatial/reaction correctness | 600 query/reference cases over 5,002 enemies; two 2,400-tick reference combats plus 1,800 ticks of nested reactions/areas/echoes compare events and checksums; full evolved runs agree at 30 FPS/1× and 144 FPS/4× |
| Headless benchmark | 64 towers, 1,001–2,500 enemies; scan mean 3.93–4.05 ms/tick versus grid 0.94–1.01 ms/tick, identical results. Grid allocation reduced from 61,005 to 4,989 bytes/tick during this session |
| `.\dev.cmd export` | Windows package and packaged-game integration pass; ZIP/hash produced |
| Fresh extraction of the final ZIP | Same smoke passes from a path with spaces/Unicode and an unrelated working directory; graphical capture also uses this extracted copy |
| Actual graphical packaged launch | OpenGL / NVIDIA RTX 3050 Laptop; mixed-wave screenshot inspected; clean engine log |

Both preceding strategies' end states/timing are preserved. Their checksums changed because the digest now includes investment/branch identity, status ownership/expiry, reaction counters/cooldowns and persistent effects. Hashes across code revisions are not a compatibility promise.

New tests cover independent expiry/stack-cap ownership, consuming existing setup, simultaneous/lethal hits, nonrecursive reaction damage, setup/finisher attribution, capped fields and staggered overlaps, paused effects, armor recovery, strongest slow, delayed echoes after sale, atomic/exclusive purchases/refunds, unchanged in-flight attacks, invalid catalogs, complete evolved runs and reference-grid agreement. The original 38 regression cases remain.

## Decisions, limitations and blockers

No implementation blocker. Godot remains the presentation layer; the simulation is engine-independent C#. SDK 10.0.400 targets .NET 8; Windows export bundles runtime 8.0.30. Benchmark development runtime is 8.0.0. Review framework support before production release.

The bounded grid is justified by measurements; object pooling and broader engine architecture were not added speculatively. Damage behavior uses explicit attack/damage kinds, not content ID switches. Armor is applied before the strongest live escort reduction, with a minimum of one damage. Tick order and exact rules are in GAME_DESIGN_DOCUMENT.md and ARCHITECTURE_DECISION.md.

The evolved witness has 151 seconds of simulated combat, plus untimed planning in actual play. It does not demonstrate a 25–40-minute run. Its 975 unspent gold and a downstream Breach Mortar dealing zero damage reveal surplus economy and excessive upstream coverage in this showcase. All branch mechanics have isolated/stress coverage, but this layout is not evidence that every purchase is useful. Tune costs/waves/placement through playtests, including a fracture-focused strategy, before expanding content. A successful scripted strategy proves feasibility, not fun, balance or strategy diversity.

Verification uses automated UI signals and actual screenshots, not a full human mouse/keyboard playthrough or subjective audio listening. No rendered heavy-load FPS, final accessibility, cross-platform or save/resume claim. The benchmark measures new-kernel overhead on the original dense targeting scenario, not heavy active-field rendering. Art and audio remain original procedural prototypes. CI exists but has not been run by GitHub.

The initial sandbox restore could not refresh the NuGet vulnerability feed (NU1900). A normal-access locked restore completed successfully; subsequent build/test/export completed without those warnings. Normal-access packaged/render checks also avoid the sandbox certificate-store warning. The game-local solution and configuration-specific locks from the preceding delivery remain intact. Tools/packages stay ignored and workspace-local. No old map/save importer or course constraint is active.

## Next exact tasks

1. **RE2-04 / link model:** add explicit link commands/state with four-tile range, 25-gold initial investment, one outgoing/two incoming ports, cycle rejection, removal on sale and no-cost rewiring between waves. Validate before mutation; cover duplicates, range boundaries, occupied ports, cycles and sold endpoints. Resolve whether initial link construction is also restricted to planning and document the rule.
2. **RE2-04 / charge flow:** consume natural reaction events at most once per source per 60 ticks; transmit one hop, cap stored charge at three, retain charge between waves, discard overflow. Empowered attacks cannot generate pulses. Preserve source credit and include links/charge/timers/empowered in-flight flags in checksums.
3. **RE2-04 / empowered payloads and UI:** define a useful payload for every base/branch, particularly Prism/Sun without an on-hit status, and distinguish circuit empowerment from Echo Mortar's existing echo. Implement only after those rules are explicit. Expose link direction/ports/charge/cost and two useful playable circuit layouts; extend smoke and replay/reference tests.
4. **RE2-05:** add telegraphed Sapper disruption and counterplay after Circuits pass. Keep the current burning-ground behavior; further terrain changes need an observable tactical benefit and route-safety tests.
5. Use `dev.cmd test`, engine smoke, base/evolution/foundation CLI scenarios and export at each slice; update this ledger from actual results. Run the benchmark when combat/query changes warrant it. Do not switch branches or push. Preserve the current regression fixture and unrelated files.
6. Collect human observations on path clarity, Primed versus Support, setup/finisher positioning and spending. Tune the six-wave economy and prove a useful Breach-centered build before expanding the full run. No further approval is required for these already authorized milestones.
