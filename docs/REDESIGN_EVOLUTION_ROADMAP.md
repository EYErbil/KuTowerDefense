# REDESIGN: EVOLUTION — Roadmap

Approved 2026-10-19; updated 2026-10-20. P0=critical, P1=required MVP, P2=optional. Complexity S/M/L/XL is relative effort, not a calendar promise. Unchecked means pending unless stated. Completion requires evidence in DEVELOPMENT_PROGRESS.md.

## Phase 0 — Audit, vision, retirement

- [x] **RE0-01 Audit/baseline** | P0 | deps none | M | MVP | AC: source/assets/history/build/test inventory and confirmed defects recorded.
- [x] **RE0-02 Design package** | P0 | deps RE0-01 | M | MVP | AC: six documents cover independent vision, decisions, rules, tasks and evidence.
- [x] **RE0-03 Retire academic scaffolding** | P0 | deps RE0-02 | S | MVP | AC: baseline/hash manifest recorded; reviewed old app/course files retired; branch/unrelated files preserved.

Milestone: independent project without academic constraints.

## Phase 1 — Excellent foundation

- [x] **RE1-01 Bootstrap** | P0 | deps RE0-02 | M | MVP | AC: pinned tools/locks, clean build, tests, engine launch and Windows export succeed.
- [x] **RE1-02 Combat kernel** | P0 | deps RE1-01 | M | MVP | AC: movement/targeting/projectiles, exact-once rewards, pause/speed/leak/outcome regression tests pass.
- [x] **RE1-03 First playable defense** | P0 | deps RE1-02 | L | MVP | AC: bundled map/tower/enemy, three waves, preview/HUD/pause/speed/win/loss/restart work in export.
- [x] **RE1-04 Wave/spatial systems** | P0 | deps RE1-03 | M | MVP | AC: stable schedules/targeting, no stalls, cached routes and spatial queries matching reference scans under stress.
- [x] **RE1-05 Roster/feedback** | P1 | deps RE1-04 | L | MVP | AC: three base tower families/three enemy roles finish six waves with distinct attacks/sound/readable controls.

Milestone / Slice A: six-wave foundation implemented and automatically verified. Three-wave regression remains runnable. Human enjoyment/difficulty playtesting remains necessary; final balance is not implied by this gate.

## Phase 2 — Signature gameplay

- [x] **RE2-01 Effect/branch/reaction definitions** | P0 | deps RE1-05 | M | MVP | AC: extend the implemented roster/spawn catalogs so effects, exclusive branches and reactions are tunable without tower-code edits; invalid values/IDs/references produce actionable errors.
- [x] **RE2-02 Reactions** | P0 | deps RE2-01 | L | MVP | AC: Shatter/Ignition consumption, stacking, attribution and trigger limits tested.
- [x] **RE2-03 Specialization** | P1 | deps RE2-02 | L | MVP | AC: six exclusive branches change mechanics/visual identity and preserve choices.
- [ ] **RE2-04 Circuits** | P0 | deps RE2-03 | L | MVP | AC: valid links, charge, empowered attacks/removal/UI; no cycles or recursive generation.
- [ ] **RE2-05 Terrain/counterplay** | P1 | deps RE2-04 | M | MVP | AC: overlays and telegraphed Sapper disruption create counterable decisions without invalid routes.

Milestone / Slice B: six-wave encounter combines specializations, reactions and circuits. Reactions/specializations now form a playable intermediate slice; the milestone remains open until Circuits and counterplay are verified.

## Phase 3 — Depth and replayability

- [ ] **RE3-01 Run structure** | P0 | deps RE2-05 | L | MVP | AC: 18 waves/three acts, twelve relics, forecasts/contracts reproduce by seed.
- [ ] **RE3-02 Enemies/boss** | P1 | deps RE3-01 | L | MVP | AC: six roles and two Bellwarden phases have visible tells, counters and behavior coverage.
- [ ] **RE3-03 Maps/authoring** | P1 | deps RE2-05 | M | MVP | AC: two maps/variations validate entrances/exits/foundations and useful circuit layouts.
- [ ] **RE3-04 Profiles** | P1 | deps RE3-01 | M | MVP | AC: capped bonuses/unlocks/fresh profiles/standardized challenges; no grind needed for standard victory.
- [ ] **RE3-05 Saves** | P0 | deps RE3-02, RE3-04 | L | MVP | AC: mid-wave load yields identical continuation including projectiles/status/boss/circuit/RNG; corrupt saves preserve current run.
- [ ] **RE3-06 Analytics** | P1 | deps RE3-05 | M | MVP | AC: tower efficiency/resources/defeats/reactions/wave results, with seed/content/difficulty/profile context.

Milestone / Slice C: replayable 25–40-minute complete run.

## Phase 4 — Presentation and advanced systems

- [ ] **RE4-01 Art/audio** | P1 | deps RE3-02, RE3-03 | L | MVP | AC: shared style, distinguishable branches/threats, complete asset provenance.
- [ ] **RE4-02 Usability/accessibility** | P1 | deps RE3-06 | M | MVP | AC: scaling/remapping/audio/reduced motion/tooltips/non-color-only cues pass review.
- [ ] **RE4-03 Performance/lifecycle** | P0 | deps RE4-01, RE4-02 | L | MVP | AC: stress budgets measured and met; repeated runs retain no scenes/entities/handlers.
- [ ] **RE4-X1 Experiments** | P2 | deps RE2-05 | M each | Stretch | AC: bounded prototype/player observations/explicit keep or discard for each candidate.
- [ ] **RE4-X2 Mobile feasibility** | P2 | deps RE4-03 | M | Stretch | AC: actual-device export, touch/readability/performance measured before commitment.

Milestone: coherent external-playtest build. Multiplayer excluded.

## Phase 5 — Test, balance, release

- [ ] **RE5-01 Simulation campaign** | P0 | deps RE3-06 | L | MVP | AC: 1,000 seeds terminate; mismatches/invalid states investigated; interaction matrix covered.
- [ ] **RE5-02 Balance/playtests** | P0 | deps RE4-03, RE5-01 | L | MVP | AC: three strategy families win; observed durations/player feedback guide tuning.
- [ ] **RE5-03 Release candidate** | P0 | deps RE5-02 | M | MVP | AC: clean build/package/fresh profile/save recovery/settings/credits/known issues pass checks.

Milestone / Slice D: reproducible tested release candidate.

## Order and creative experiments

Completed: RE0-02 → RE0-03 → RE1-01 → RE1-02 → RE1-03 → RE1-04 → RE1-05 → RE2-01 → RE2-02 → RE2-03. Next: RE2-04 → RE2-05. Keep the six-wave export playable and the three-wave fixture passing at each step. RE3-03 can proceed after RE2-05; RE5-01 can overlap presentation work.

Battlefield Memory, Evacuation Couriers and Echo Blueprints are not hidden release requirements. Limit first prototypes to three focused engineering days; retain only observable new decisions surviving readability/playtest review. Document discarded findings.
