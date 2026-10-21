# Frontier campaign and road warriors — research and proposal

2026-10-21. Research/design proposal responding to the player's request for maps, unlocks, warriors, stronger presentation and a longer progression chase. These features are **not implemented** by this document. The existing expedition is the playable baseline. Repository inspection began clean at `3748e0d` on `Ege-redevelop`, which contains the previous delivery.

**Handoff status:** the owner accepted this direction and asked for documentation only while a colleague begins implementation. Start with FR-01, then FR-02; [AGENT_HANDOFF.md](AGENT_HANDOFF.md) contains the concrete first patch, code entry points and verification commands. Prices, timings, later regions and experiments remain provisional. Acceptance of the direction does not mark any gameplay task complete.

## Recommendation

Build toward a frontier campaign in which players unlock distinct battlefields and recruit small specialist squads to fight alongside their towers. Prioritize one animated road squad, a second authored battlefield and persistent first-clear/unlock progress. Reevaluate Circuits after this prototype: troop positioning may provide a more visible and understandable signature than another attack modifier.

Difficulty should increase through choices under pressure. Longer progression should reward mastery and experimentation. Whether either attracts or retains more players is a hypothesis to test; feature lists, positive store reviews and developer descriptions cannot establish that causal claim.

## Comparable games — first-party research

Sources checked 2026-10-21. These are observations about mechanics and developer intent, not measurements of retention or sales effects.

| Reference | Verified feature | Design lesson for this project |
|---|---|---|
| [Kingdom Rush Battles: Paladin Barracks, Ironhide support](https://support.ironhidegames.com/support/solutions/articles/4000223657-paladin-barracks-skills-breakdown-defend-the-lines-in-kingdom-rush-battles) | Soldiers engage and block enemies, extending the time other defenses can attack | A road squad can multiply positioning value while having limited damage and survivability. This comparison does not imply migrating our game to PvP. |
| [Thronefall, developer/publisher Steam description](https://store.steampowered.com/app/2239150/Thronefall/) | Different maps, recruitable troops, unlockable weapons/perks and optional difficulty mutators | Combine places to discover, loadout choices and voluntary challenge. Do not attempt its full city-building/action scope. |
| [Emberward, developer Steam description](https://store.steampowered.com/app/2459550/Emberward/) | Player-shaped mazes, spatially demanding towers, run-specific maps/equipment and persistent talents | Changing the spatial problem can refresh the same tower roster. We should author different layouts before considering unrestricted maze construction. |
| [The Last Spell, developer posts from May–June 2021](https://store.steampowered.com/news/posts/?appids=1105670&enddate=1623158663&feed=steam_community_announcements) | The developers distinguish direct power growth from unlocks that introduce new play styles; they describe progression as onboarding and a source of objectives after losses | Persistent rewards can support both struggling and experienced players. The historical post explicitly discusses tuning unlock pace; it is not evidence of current balance or a universal progression formula. |
| [Kingdom Rush Genesis: Knights Order devlog, 2026-05-14](https://www.ironhidegames.com/News/Details/472) | The described unit upgrades change armor, helmets and banners | Make growth visible in the battlefield silhouette, not solely in a menu. This is a development preview, not a claim about released content. |

## The player experience

Choose an unlocked frontier location → inspect its threats and squad loadout → build, rally and defend → earn relic choices → win or fall → see what worked and what unlocked → choose the next destination or a harder contract.

Keep three layers distinct:

- **Profile:** unlocked maps, squad variants, mastery records and cosmetic honors persist.
- **Expedition:** gold, tower specializations, chosen relics, lives and temporary squad development reset.
- **Battle:** rally positions, engagements, wounds and attack cooldowns change moment to moment.

Initially, each campaign location launches a complete expedition. A continuous journey carrying state across several battlefields is a separate, larger feature. Do not accidentally multiply the current run length by attaching multiple twelve-wave maps end to end.

## 1. A frontier map with useful unlocks

The campaign map should show settlements, linked roads, an explicit unlock requirement, first-clear rewards and difficulty badges. A locked location promises something specific: a different battlefield or squad option. It should not be an unexplained padlock.

First delivery: Emberwatch and **Stoneford**, a bridge defense with fewer overlapping tower positions and authored fast/armored formations. Complete Emberwatch to open Stoneford. Use map-specific wave composition and planning geometry; a recolored copy is not enough. Keep an unlocked location available permanently. Replay any available location.

Later candidate branches: **Glassfen**, testing limited safe rally positions and mobile supports, and **Iron Quarry**, testing separated coverage and siege pressure. These are proposals, not an approved four-map production commitment. Branching between locations should follow the two-map proof.

Preserve access to the current tower families and six paths. Give the player the starter squad immediately when this system is introduced. Subsequent unlocks add choices rather than withholding the mechanic that makes the new game interesting.

## 2. Warriors on the road

Prototype one **Garrison**, limited to one per battlefield, occupying a tower foundation and costing the same run currency. It deploys three Shieldguards. The player selects the squad and places a rally marker on a reachable road segment within the Garrison's command radius. Warriors move there, engage and fight; they do not teleport.

Initial rules to validate:

- Each living guard can engage one ordinary enemy. Unengaged enemies can continue past: three units cannot stop an infinite column.
- Engaged enemies attack their guard. Finite health and enemy attack cadence make unsupported blocking fail. No invulnerable body wall.
- Moving the rally point releases engagements and costs travel time. A retreat order returns the squad toward its Garrison; relocating is a tactical decision, not an instant escape-and-reset exploit.
- Fallen guards return during the next planning phase. No endless mid-wave replacement loop, roster permadeath or repeated gold tax in the first prototype.
- Bosses have an explicit readable response: they cannot be fully immobilized and have a telegraphed attack dangerous to clustered troops. Validate the rule separately for Captains; do not hide immunity behind a stat.
- Starting melee attacks must not automatically consume chill/oil setup and steal the tower finisher's role. Status-consuming specialist attacks are a later explicit choice.

The first interesting interaction already exists in our combat system: hold a group inside an Ignition field or Shatter radius, then decide whether to retreat before the guard line breaks. Support-targeting towers remove Menders while the squad contains ordinary attackers. This links troops to the current investment instead of adding a detached minigame.

Later options, only after the starter works: Pikemen with stronger anti-Raider engagement; Runeguards that improve elemental setups; a medic with low blocking capacity. Unlock/select alternatives with explicit trade-offs. Avoid a full RTS roster and individual skill-bar micro.

**Signature experiment — Field Standards:** a squad's rally banner marks a limited area where troops and tower reactions cooperate. First test the basic positioning benefit. Only if it needs another decision, prototype bounded reaction-earned Resolve spent on a brief brace or reposition benefit. Do not ship Resolve and Resonance Circuits together without evidence that they solve different problems. Infinite stall, reaction feedback loops and mandatory Garrison purchases are the main balance risks.

## 3. Progression with a worthwhile chase

Use one clearly explained persistent resource, provisionally **Renown**, earned at encounter milestones with extra first-clear rewards. Defeat retains earned progress. Awards must be idempotent: replaying a result screen or restarting after a crash cannot pay twice. Pay for progress through encounters, not raw spawned-enemy counts, which would reward farming reinforcements.

Spend Renown on alternative squad doctrines and additional starting options. First wins open locations and challenge tiers. Mastery objectives should teach useful interactions: hold a wave with a mixed defense, defeat an escorted Captain, or complete a map with a different squad. Show progress and the next reward. Avoid objectives that require intentionally poor play or thousands of trivial kills.

Longer optional goals can award armor appearances, banners, map medals and specialist variants. Players who enjoy a substantial completion chase can pursue these while essential tools remain available. No energy timer or daily-login requirement is needed.

Initial pacing hypotheses to test: demonstrate the starter squad in the first battle; show meaningful mastery progress in the first session; provide the first new option within one or two completed attempts. These are test targets, not measured promises. Do not require several hours before the core strategy becomes available.

Small permanent stat upgrades remain an optional later experiment. If used, keep a finite cap and a fresh-profile victory benchmark; separate standardized challenge records from profile power. Increasing enemy health to cancel an unlimited upgrade treadmill adds tuning cost without ensuring new decisions.

## 4. Harder encounters players can understand

Offer Standard, Veteran and authored challenge contracts, eventually extending into selectable threat modifiers. Keep modifiers visible before a run or wave. Examples: Raiders arrive in a second staggered push; an announced siege unit threatens a Garrison; two supports appear in separated formations. Introduce one new behavior safely before combining it with old pressures.

Tune the ordinary expedition after adding troops: holding enemies amplifies area damage and existing slows. Compare Garrison and no-Garrison builds, not only the three earlier scripted strategies. Aim for more than one viable investment plan. Stronger play should overcome pressure without a mandatory profile-level threshold.

Shared seed challenges could later support friendly competition. Begin with offline challenge codes and local records; online leaderboards, servers and PvP are separate product decisions. Do not infer multiplayer demand from the phrase 'competitive games.'

## 5. Presentation and useful graphs

The visual target is a coherent stylized 2D frontier with animated units and readable impacts. Keep Godot/C#; neither a language migration nor a 3D rewrite solves the current content problem.

Build a polished animation sample around one squad and one battlefield: walk, idle, attack anticipation/contact/recovery, defend, stagger and fall. Give towers aim/recoil/charge and projectiles clear trails/impacts. Use consistent ground contact, shadows, scale, palette and upgrade silhouettes. Environmental motion and restrained lighting should support rather than obscure the battlefield. Add density limits and reduced-motion controls before broadening effects. Original or appropriately licensed assets need provenance.

Gameplay timing remains authoritative in Core. Animation follows simulation state/events and must not determine hit timing. Preview attacks at 1×/2×/4× and pause, and test at smaller window sizes. No final-art quality is claimed until an actual rendered slice is reviewed.

Post-run graphs should explain decisions: gold earned/spent by wave; damage contributions; leaks and life loss by wave; squad engagement time and casualties. Do not invent 'damage prevented' from unblocked travel time. Compare records with map, content version, seed, difficulty, profile modifiers, relics and squad attached. Store local summaries first; external analytics is unnecessary for the prototype.

## Fit with the repository

- `GameRoot` currently loads one content file directly; stage/relic presentation assumes twelve waves and fixed reward checkpoints. A mission catalog and configuration-driven HUD must precede new mission lengths.
- `MapDefinition` and `Route` support one immutable orthogonal path and authored foundations. A second single-route map is practical now. Multiple simultaneous entrances, freeform walls and rerouting need additional navigation/target-progress design.
- No friendly combatant, enemy melee attack or engagement state exists. Add bounded squad/engagement state and commands in Core, not Godot physics callbacks. Stable IDs and tick ordering must resolve deaths, release engagements and prevent corpses blocking movement.
- Infrastructure currently loads content and provides command witnesses; there is no profile persistence. Versioned profile writes, backup/recovery and exact-once awards are prerequisites for promising permanent unlocks. This is smaller than full mid-wave save/resume, which remains separate.
- Existing tower statistics and `WaveReport` support part of a results dashboard. Per-wave spending/damage and troop utility need new explicit recording. Avoid retaining every combat event indefinitely.
- The current 76-test report and Windows build are prior-delivery evidence. This research-only change does not rerun or revalidate them.

## Dependency-aware implementation checklist

P0 = first prototype, P1 = follow-on; S/M/L/XL = relative complexity. These are planned tasks for the accepted direction, not completed game features. MVP candidate scope remains subject to prototype evidence. The human observations in EX-05 remain an evaluation gate, not a reason to freeze bounded prototypes. This sequence takes priority over the earlier Circuits-first plan; the current handoff session implements none of it.

- [x] **FR-00 Research/design** | P0 | deps EX-04 | S | MVP planning | AC: first-party comparisons, concrete troop/map/progression rules, code constraints and testable candidate tasks recorded.
- [ ] **FR-01 Road squad kernel** | P0 | deps FR-00 | L | MVP candidate | AC: one Garrison/three guards; valid rally/retreat; finite engagements; enemy melee/death/release rules; no permanent stall; pause/speed/reference determinism; unchanged earlier fixtures.
- [ ] **FR-02 Animated squad slice** | P0 | deps FR-01 | M | MVP candidate | AC: actual selection/rally UI, reachable-position preview, readable attack/hurt/fall states, boss counter telegraph, 1×/4× and paused render inspection; useful Garrison and no-Garrison witnesses on Emberwatch.
- [ ] **FR-03 Mission catalog/Stoneford** | P0 | deps FR-00 | L | MVP candidate | AC: select either of two authored maps with distinct formations; no hardcoded twelve-wave HUD assumptions; valid route/foundations; both maps terminate and have at least two viable builds.
- [ ] **FR-04 Frontier/profile unlocks** | P0 | deps FR-03 | L | MVP candidate | AC: clear Emberwatch → visible Stoneford unlock → restart application → unlock retained; atomic versioned profile/backup recovery; repeated completion cannot repay; failure keeps earned milestone progress; resetting a run does not erase profile.
- [ ] **FR-05 Mastery/challenges** | P1 | deps FR-02, FR-04 | M | MVP candidate | AC: one alternative squad doctrine and one authored higher-difficulty modifier; visible criteria/progress/rewards; new profile can finish Standard; no raw-kill farming incentive.
- [ ] **FR-06 Results graphs** | P1 | deps FR-02, FR-04 | M | MVP candidate | AC: two local run summaries compare wave economy, damage and squad utility with full context; amounts reconcile with authoritative totals; no unbounded event history.
- [ ] **FR-07 Combined evaluation** | P0 | deps FR-02, FR-04, FR-05, FR-06 | L | MVP candidate | AC: regression/build/export checks pass; observe at least three human runs; document repeated choices, first unlock time, squad clarity, losses and desire to replay; explicit keep/change/discard decision before expanding content.

Playable slices: A = FR-01 → FR-02 on existing Emberwatch; B = FR-03 → FR-04 with persistent destination progress; C = FR-05 → FR-06 → FR-07. FR-03 can be developed independently of the squad once its content/HUD contract is defined. Every slice must leave the previous expedition playable.

## Open design risks

Blocking can make frost/area damage even stronger; troop weakness must be readable rather than arbitrary. Relic dilution can make new unlocks feel like a penalty; offer pools need compatibility rules. A world map can become a cosmetic menu if its locations play alike. Repeated Renown collection can become an optimal low-risk farm. Animation and art quantity can outgrow the production budget. Resolve/Circuits can duplicate one another. Evaluate these in the small slices above before commissioning a broad roster or campaign.
