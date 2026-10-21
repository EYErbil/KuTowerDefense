# REDESIGN: EVOLUTION — Game design

Version 0.3, 2026-10-20. Phase 2 reactions and specializations are implemented; Circuits and the full run remain planned. Numbers are starting balance hypotheses, not inherited assignment requirements.

## Implemented foundation: Emberwatch

One authored battlefield, winding route, citadel, eight foundations, three towers and three enemy roles. Start with 245 gold and 12 lives. Six waves teach coverage, armor, dense packs, then escort shields before combining threats. The preview states exact composition and a counter before the player starts. Content uses validated format-2 JSON catalogs and overlapping spawn groups.

Select a tower card and click an empty foundation. Invalid placement spends nothing. Click a tower to inspect range/damage/kills, evolve, change target priority or sell; refund is 70% of its total investment, rounded down. First targets greatest path progress, Strongest targets highest current health, Support targets shield generators, and Primed targets enemies with enough existing status stacks and a ready reaction matching this tower's damage kind. Progress then lowest entity ID break ties. Primed falls back to First when no reaction is ready; projectile travel can still change the situation before impact.

Untimed planning, explicit wave starts; building/selling/targeting/upgrading work during combat and pause. Evolve [U] opens exclusive path cards and pauses combat. Closing or choosing leaves the battle paused until the player resumes. Unaffordable paths remain inspectable. All gameplay time stops when paused. Victory requires clearing six waves with lives remaining. Both outcomes show totals, reaction counts/damage and a clean restart.

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

## Full run — planned

18 waves, three acts, relic selections and optional contracts at authored boundaries, final two-phase boss. Forecast two waves and explain new threats. Use encounter budgets and documented composition constraints. No hidden adaptive hard counters. Countermarch Contracts explicitly exchange additional forecast threats for known rewards. Map variation, offers and contracts use a run seed.

Currency creates opportunity costs between towers, branches and links. Avoid mandatory click-to-collect income. Two authored maps receive validated variations. Godot editor authoring comes first; a player map editor is optional.

## Tower evolution — implemented

| Family | Base role | Branch A | Branch B |
|---|---|---|---|
| Boltworks | Precision impact | Pinning Ballista: brief pin/precision | Pitchbow: lower damage, oil |
| Prism | Elemental preparation | Rime Lens: chill/Shatter setup | Sun Lens: fire/Ignition |
| Crucible | Area coverage | Breach Mortar: armor fracture | Echo Mortar: delayed second impact |

One permanent specialization per tower in this slice. Purchase is atomic: reject invalid families, unknown paths, insufficient funds, sold towers and second choices without mutation. Preserve cooldown, priority, statistics and source ID; projectiles already in flight retain their original attack definition. Selling refunds 70% of base plus branch cost. No respec or additional tier is implemented; expand depth only after testing Circuits. All branch attacks and status references are data, not tower subclasses.

| Path | Additional cost | Damage / interval | Range | Mechanical change |
|---|---:|---|---:|---|
| Pinning Ballista | 110 | 38 impact / 78 ticks | 245 | Bolt speed 650; pin slows 80% for 30 ticks; slower fire and longer reach |
| Pitchbow | 70 | 8 impact / 32 ticks | 195 | One oil stack per hit; sacrifices direct damage to prepare Ignition |
| Rime Lens | 80 | 10 frost / 36 ticks | 180 | One chill per hit; trades Prism damage for slowing and Shatter setup |
| Sun Lens | 100 | 24 fire / 64 ticks | 200 | Consumes oil for Ignition; without preparation it gains reach but loses one base damage |
| Breach Mortar | 110 | 34 impact / 102 ticks | 220 | Splash radius 82; fracture on primary and secondary hits, before damage |
| Echo Mortar | 120 | 28 impact / 108 ticks | 235 | Splash radius 76; one 16-impact, radius-76 echo after 30 ticks at the impact point |

All non-impact damage bypasses armor; escorts still reduce it. Mortars retain half damage to secondary targets. Echoes damage every enemy in their radius equally, retain credit after sale, and cannot apply statuses, react or create more echoes. Pin is a bounded slow, not displacement: it preserves route progress and cannot fully immobilize an enemy. Branch silhouettes, frost/fire beams, status letters/counts, ignition ground and echo countdown rings expose the mechanical changes. Exact effect values are also available in path-card tooltips.

## Reactions and terrain — implemented rules

| Status | Stack cap | Lifetime per stack | Effect |
|---|---:|---:|---|
| Chill | 3 | 180 ticks / 3 seconds | 15% slow per stack; two stacks can be shattered |
| Oil | 1 | 240 ticks / 4 seconds | Prepared for Ignition; no passive damage |
| Pin | 1 | 30 ticks / 0.5 seconds | 80% slow |
| Fracture | 2 | 180 ticks / 3 seconds | Remove 6 armor per stack, minimum zero |

Each stack owns its source ID and expiry. At capacity a new application replaces the oldest stack; consuming a reaction removes the oldest required stacks. Stacks expire before movement at `ExpiresAt <= Tick`. Different slows use the strongest effect, never their sum; the validator caps any status at 80%. Fracture adds armor reduction. Pause freezes all expiry, cooldown and delayed-effect timers.

- **Shatter:** an ordinary impact hit consumes two pre-existing chill stacks, then deals its normal hit and a 24-frost burst in radius 64. Per-enemy Shatter cooldown: 60 ticks. It still bursts if the ordinary hit kills the prepared enemy.
- **Ignition:** an ordinary fire hit consumes one oil stack, then creates burning ground at the enemy's position. Radius 72; 6 fire damage immediately and every 30 ticks for 180 ticks, with no pulse on the expiry tick. Per-enemy reaction cooldown: 120 ticks. Maximum six fields globally; creating another replaces the oldest. Each enemy has one damage cadence per reaction, so even staggered overlapping fields cannot multiply burn frequency. Entering a field takes damage on its next eligible pulse.

Reactions inspect/consume existing setup before the incoming attack applies its own status. Thus an attack cannot generate and consume a new stack at once. Reaction damage and echoes never enter the reaction pipeline. Primary/splash hits can independently trigger eligible enemies; a consumed stack cannot be spent twice. Separate query buffers protect splash iteration from nested bursts. Natural hit ordering is deterministic, not simultaneous in the mathematical sense.

Credit actual reaction damage and kills to the triggering tower, including after its sale. Credit one preparation to each distinct tower contributing consumed stacks and one trigger to the finisher. A tower's damage total already includes its reaction damage: do not add it twice. An oldest eligible overlapping field owns the burn damage. Reaction statistics survive wave clears; fields and pending echoes clear when the wave ends. No persistence to disk exists yet.

Ignition is the first temporary terrain interaction. Frozen ground, freeform walls, flooding and water/electricity are deferred. Oil currently belongs to an enemy, not a ground surface. Enemy disruption and broader terrain counterplay remain RE2-05.

## Resonance Circuits — next Phase 2 milestone, not implemented

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
