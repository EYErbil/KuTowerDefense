# REDESIGN: EVOLUTION — Roadmap

Approved 2026-10-19; updated 2026-10-21. P0=critical, P1=required MVP, P2=optional. Complexity S/M/L/XL is relative effort, not a calendar promise. Unchecked means pending unless stated. Completion requires evidence in DEVELOPMENT_PROGRESS.md. Player feedback reprioritized the expedition loop ahead of Circuits; [EXPEDITION_REWORK.md](EXPEDITION_REWORK.md) records that decision.

**Current continuation:** implementation resumed from `856cb2d`. FR-01 is complete in Core/headless scenarios; **FR-02 animated squad controls and balance on Emberwatch are next**. Read [AGENT_HANDOFF.md](AGENT_HANDOFF.md), the [kernel contract](ROAD_SQUAD_KERNEL.md) and the [frontier checklist](FRONTIER_CAMPAIGN_PROPOSAL.md). The FR slices supersede the earlier instruction to implement Circuits immediately after EX-05.

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
- [ ] **RE2-04 Circuits** | P2 | deps RE2-03, EX-05, FR-07 | L | Stretch; deferred pending reassessment | AC if retained: valid links, charge, empowered attacks/removal/UI; no cycles or recursive generation; at least two useful layouts that add decisions distinct from relics and squad positioning. Record keep/change/discard before implementation.
- [ ] **RE2-05 Terrain/counterplay** | P1 | deps FR-02, EX-05 | M | MVP | AC: overlays and telegraphed Sapper disruption create counterable decisions without invalid routes; verify tower and squad responses.

Milestone / Slice B: expedition combines specializations, reactions and useful squad positioning with readable counterplay. The playable expedition below is an intermediate delivery; FR-07 and RE2-05 supply the remaining evidence. Circuits are an optional experiment, not a prerequisite for this milestone.

## Expedition intervention — before additional systems

- [x] **EX-01 Run progression** | P0 | deps RE2-03 | L | MVP | AC: seeded nonduplicate relic offers after waves 3/6/9; one atomic choice; owned-relic review; wave advance blocked until choosing; modifiers affect current/future specializations without compounding.
- [x] **EX-02 Encounter pressure** | P0 | deps EX-01 | L | MVP | AC: twelve forecasts across four stages; healer windup and bounded boss rallies/vulnerability; exact optional reinforcement count/reward; bonus paid only without breaches.
- [x] **EX-03 Balance witnesses** | P0 | deps EX-02 | M | MVP | AC: frost/fire/steel and passive/unspecialized controls measured across fixed seeds, with wave/resource/leak reports; limits and unsuccessful witnesses documented.
- [x] **EX-04 Playable delivery** | P0 | deps EX-03 | M | MVP | AC: reward/contract/boss UI; actual engine flows; inspected export captures; regression tests and self-contained Windows package; updated handoff. Verified export and fresh extraction, including Unicode path and unrelated working directory.
- [ ] **EX-05 Decision/playtest gate** | P0 | deps EX-04 | M | MVP | AC: record at least three human runs with first-relic time, spending pauses, unclear deaths, declined/accepted contracts and repeated choices; tune dominant frost/reward patterns; rerun the published witnesses. Do not claim enjoyment from bot wins.

Milestone / Slice B0: an escalating expedition with earned build changes and readable final encounter. No additional tower catalog is needed to pass this gate.

## Phase 3 — Depth and replayability

- [ ] **RE3-01 Run structure** | P0 | deps RE2-05, EX-05, FR-07 | L | MVP | AC: full-run pacing validated toward 25–40 minutes including planning; about twelve useful relics and varied forecast contracts reproduce by seed. Partial: twelve waves/seven relics/Overruns implemented; expand only when additional decisions justify length.
- [ ] **RE3-02 Enemies/boss** | P1 | deps RE3-01 | L | MVP | AC: six distinct behavioral roles and two Bellwarden phases have visible tells, counters and behavior coverage within the tower/squad economy. Partial: healing, rally/guard/enrage implemented; Sapper and broader encounter coverage pending.
- [ ] **RE3-03 Maps/authoring** | P1 | deps FR-03, FR-04 | M | MVP | AC: two maps/variations validate entrances/exits/foundations and useful tower/squad layouts; shared authoring checks cover both. FR-03/04 supply the initial map/unlock slice; this task completes the wider authoring gate.
- [ ] **RE3-04 Profiles** | P1 | deps FR-04, FR-05 | M | MVP | AC: challenge unlocks/alternative starting options/fresh profiles/standardized challenges; no grind needed for standard victory; permanent power bonuses require a separate balance decision. Verify the FR profile prototype across the full content set.
- [ ] **RE3-05 Saves** | P0 | deps RE3-02, RE3-04 | L | MVP | AC: mid-wave load yields identical continuation including projectiles/status/boss/squad engagements/RNG and every retained gameplay system; corrupt saves preserve current run. Separate from FR-04 profile persistence.
- [ ] **RE3-06 Analytics** | P1 | deps FR-06 | M | MVP | AC: tower efficiency/resources/defeats/reactions/wave results, with seed/content/difficulty/profile context. Extend the local FR-06 summaries; mid-wave saves are not a prerequisite for result graphs.

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

Accepted direction, 2026-10-21: [Frontier campaign and road warriors](FRONTIER_CAMPAIGN_PROPOSAL.md) records first-party references, persistent map unlocks, a small controllable squad, mastery, animation and results graphs. FR-00 research and FR-01 simulation are complete; FR-02 through FR-07 remain pending. That checklist owns their identifiers, priorities, dependencies, complexity, acceptance criteria and status. The owner resumed implementation after the documentation handoff.

Completed: RE0-02 → RE0-03 → RE1-01 → RE1-02 → RE1-03 → RE1-04 → RE1-05 → RE2-01 → RE2-02 → RE2-03 → EX-01 → EX-02 → EX-03 → EX-04 → FR-00 → FR-01. Next playable slices: **FR-02** on Emberwatch; **FR-03 → FR-04** for Stoneford/persistent destinations; **FR-05 / FR-06 → FR-07** for mastery, graphs and combined evaluation. FR-03 can proceed independently once its content/HUD contract is defined, but playable squad integration is first. Collect EX-05 observations alongside development. Keep the expedition playable and prior three/six-wave fixtures passing at each step. FR tasks deliver portions of the broader phase gates; do not mark those gates complete without their remaining acceptance evidence.

Reassess Circuits after FR-07; it no longer blocks maps, profile unlocks or the signature slice. Field Standards/Resolve, Battlefield Memory, Evacuation Couriers and Echo Blueprints are experiments, not hidden release requirements. Limit first experiments to three focused engineering days; retain only observable new decisions surviving readability/playtest review. Document discarded findings. Later regions, broad squad rosters and online competition also remain outside the first handoff milestone.
