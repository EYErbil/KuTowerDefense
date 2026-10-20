# REDESIGN: EVOLUTION — Game design

Version 0.2, 2026-10-20. Distinguish the implemented foundation from the planned MVP. Numbers are starting balance hypotheses, not inherited assignment requirements.

## Implemented foundation: Emberwatch

One authored battlefield, winding route, citadel, eight foundations, three towers and three enemy roles. Start with 245 gold and 12 lives. Six waves teach coverage, armor, dense packs, then escort shields before combining threats. The preview states exact composition and a counter before the player starts. Content uses validated format-2 JSON catalogs and overlapping spawn groups.

Select a tower card and click an empty foundation. Invalid placement spends nothing. Click a tower to inspect range/damage/kills, change target priority or sell; refund is 70% of actual cost. First targets greatest path progress, Strongest targets highest current health, Support targets shield generators before other enemies. Progress then lowest entity ID break ties. Untimed planning, explicit wave starts; building/selling/targeting permitted during combat and pause. All gameplay time stops when paused. Victory requires clearing six waves with lives remaining. Both outcomes show totals and offer a clean restart.

| Tower | Cost | Damage | Range | Interval | Behavior |
|---|---:|---:|---:|---:|---|
| Boltworks | 60 | 18 impact | 190 | 42 ticks | Homing bolt, speed 480; cancels if its target dies |
| Prism | 85 | 25 energy | 175 | 64 ticks | Instant beam, ignores armor; remains affected by escort protection |
| Crucible | 100 | 36 impact | 220 | 96 ticks | Shell speed 270, radius 76, half secondary damage; detonates at last tracked position if its target dies |

| Enemy | Health | Speed | Bounty | Breach | Defense |
|---|---:|---:|---:|---:|---|
| Runner | 44 | 48 | 10 | 1 | None |
| Bulwark | 130 | 32 | 18 | 2 | 9 flat armor against impact |
| Warden | 95 | 38 | 22 | 2 | Protects non-Warden allies within 100 units for 40% less damage |

Armor is applied first, then the strongest active escort reduction, rounded down. Each damaging hit deals at least 1; credited damage is capped by remaining health. Warden auras do not stack or protect other Wardens. Killing a Warden ends its protection immediately, including later hits in the same tick. Aura radius, linked shield outline and an enemy tooltip make protection visible. Shell splash resolves by entity ID for reproducibility; beam timing and projectile travel are distinct tactical choices.

| Wave | Composition | Lesson |
|---|---|---|
| First watch | 8 Runners | Cover bends |
| Iron footsteps | 12 Runners, 3 Bulwarks | Energy bypasses armor |
| The rush | 18 tightly spaced Runners | Area attacks |
| Under the ward | 16 Runners, 4 Bulwarks, 2 Wardens | Support targeting |
| Braided assault | 28 Runners, 7 Bulwarks, 3 Wardens | Combined threats |
| Last light | 36 Runners, 10 Bulwarks, 4 Wardens | Final mixed defense |

These are short teaching waves, not the eventual 25–40-minute run. The 180-gold, one-tower, 8/12/16-Runner fixture remains runnable as `foundation.json`, with its original movement/stats/timing. Its content format was upgraded along with the new simulation; legacy formats are not supported. Do not display nonfunctional upgrade/circuit/save buttons.

## Full run

18 waves, three acts, relic selections and optional contracts at authored boundaries, final two-phase boss. Forecast two waves and explain new threats. Use encounter budgets and documented composition constraints. No hidden adaptive hard counters. Countermarch Contracts explicitly exchange additional forecast threats for known rewards. Map variation, offers and contracts use a run seed.

Currency creates opportunity costs between towers, branches and links. Avoid mandatory click-to-collect income. Two authored maps receive validated variations. Godot editor authoring comes first; a player map editor is optional.

## Towers

| Family | Base role | Branch A | Branch B |
|---|---|---|---|
| Boltworks | Precision impact | Pinning Ballista: bounded displacement/precision | Pitchbow: lower damage, oil |
| Prism | Elemental preparation | Rime Lens: chill/Shatter setup | Sun Lens: fire/Ignition |
| Crucible | Area coverage | Breach Mortar: armor fracture | Echo Mortar: delayed impacts/resonance timing |

Branches are exclusive choices changing attacks, silhouette, timing, effects and placement incentives. Definitions support extensible trees without an inherited two-level hard limit.

## Reactions and terrain

Status definitions specify duration, stacking, cap, source and removal. Reaction definitions match damage tags/statuses outside tower code. Shatter consumes sufficient chill on impact for a bounded burst. Ignition consumes oil on fire damage and creates a temporary burning area. Define overlap caps; a consumed stack cannot trigger twice. Temporary oil/burning/frozen overlays use simulation ticks. Freeform walls, flooding and water/electricity are deferred.

Attribute damage/reactions to sources. Limit reaction depth and prohibit infinite generation. No effect may bypass cooldown/cap rules.

## Resonance Circuits — Phase 2

Directed links: four-tile maximum, 25 gold initial cost, one outgoing/two incoming ports. Reject cycles/invalid endpoints before spending. Rewire between waves without another fee; no investment refund for rewiring. Removing towers removes their links.

A natural reaction generates at most one pulse per source per second. One-hop transmission, three-charge capacity, discard overflow. Charges persist between waves. Three charges empower the next eligible attack. Empowered attacks cannot generate pulses, though ordinary bounded effects still resolve.

Boltworks pierces one extra target; Prism spreads status locally; Crucible adds delayed secondary impact. UI shows ports, direction, charge and actual empowered behavior using text/moving markers plus color. Link layout must compete with ordinary coverage.

## Enemies and boss

| Enemy | Pressure | Counter |
|---|---|---|
| Runner | Fast leaks | Coverage/slow/progress targeting |
| Bulwark | Armor | Breach/elemental preparation |
| Warden | Shields nearby allies | Support priority/split formations |
| Mender | Sustains allies | Focus fire |
| Sapper | Temporary circuit disruption | Telegraph and interrupt by killing |
| Broodling | Splits into weaker enemies | Area coverage; exact-once rewards |

Bellwarden phase one summons supports; phase two telegraphs one-link suppression (initial three-second warning, eight-second suppression). Ordinary attacks remain functional. Counter with distributed networks, escort priority and timing. Teach before escalating mechanics.

## Replayability and progression

Twelve curated relics initially; behavior changes and meaningful trade-offs. Avoid unusable offers and display compatibility. Permanent bonuses capped initially at +5% damage, +10% starting gold, +2 lives. Fresh profiles must be able to finish standard difficulty. Standardized challenges disable power bonuses. No monetization/online services are included.

## Presentation and controls

Clear planning/running/paused/victory/defeat states; economy/lives/wave progress and actionable tower information. Every keyboard action also has a visible control. Scale UI, keep pointer world transforms correct, show disabled states. Essential cues use text/shapes, not color alone. Focus loss pauses and sound can be muted. Later settings add remapping, reduced motion and independent buses.

First slice uses original procedural shapes/PCM cues. Final art follows the vision's arcane frontier style and consistent scale/palette/silhouette. Do not confuse placeholder rendering with final commercial polish.

## Analytics and validation

Full analytics: per-tower damage/kills/investment, spending/income, enemy defeats, reaction damage, pulse generation/spending and wave lives/duration. Support value needs context beyond damage-per-gold. Record map/seed/content/difficulty/profile/relics/branches/outcome. First slice shows only tracked statistics.

Future stress target: 1080p, ten minutes, 500 enemies/64 towers/2,000 projectiles, p95 frame ≤20 ms and simulation ≤4 ms on a recorded reference machine. Not a measured claim. Three distinct builds must win standard difficulty; measured runs target 25–40 minutes. Automated checks support rather than replace human playtests.

## Experiments and exclusions

Battlefield Memory, Evacuation Couriers and Echo Blueprints require bounded prototypes and keep/discard reviews (see vision). Multiplayer, 3D migration, unrestricted procedural maps, mobile release and broad mod support are not MVP dependencies.
