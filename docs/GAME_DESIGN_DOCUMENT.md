# REDESIGN: EVOLUTION — Game design

Version 0.4, 2026-10-21. Reactions, specializations and the expedition intervention are implemented; Circuits and the full-length run remain planned. Numbers below describe active `expedition.json`. The earlier six-wave `emberwatch.json` retains its own unchanged balance as a regression fixture. Numbers are design hypotheses, not inherited assignment requirements.

## Implemented expedition: Emberwatch

One authored battlefield, winding route, citadel, eight foundations, three tower families and seven enemy types. Start with 180 gold and 12 lives. Twelve waves in four stages introduce armor, healing, shielding, fast reinforcements and an exposed/guarded boss. The preview states exact composition and a counter before starting. Content uses validated format-2 JSON catalogs and overlapping spawn groups. Lower bounties and guaranteed clear supplies replace the six-wave prototype's surplus economy; early supplies deliberately fund the first specialization before the first relic.

Select a tower card and click an empty foundation. Invalid placement spends nothing. Click a tower to inspect range/damage/kills, evolve, change target priority or sell; refund is 70% of its total investment, rounded down. First targets greatest path progress, Strongest targets highest current health, Support targets shield generators and Menders, and Primed targets enemies with enough existing status stacks and a ready reaction matching this tower's damage kind. Progress then lowest entity ID break ties. Primed falls back to First when no reaction is ready; projectile travel can still change the situation before impact.

Untimed planning, explicit wave starts; building/selling/targeting/upgrading work during combat and pause. Evolve [U] opens exclusive path cards and pauses combat. Closing or choosing leaves the battle paused until the player resumes. Unaffordable paths remain inspectable. I reviews owned relics with the same pause rule. All gameplay time stops when paused. Victory requires clearing twelve waves with lives remaining. Both outcomes show totals, reaction counts/damage, relics, cleared waves, successful Overruns and seed. New expedition resets everything and advances the seed; normal application launch chooses a fresh seed, while `--seed=42` reproduces offers.

| Tower | Cost | Damage | Range | Interval | Behavior |
|---|---:|---:|---:|---:|---|
| Boltworks | 60 | 18 impact | 190 | 42 ticks | Homing bolt, speed 480; cancels if its target dies |
| Prism | 85 | 25 energy | 175 | 64 ticks | Instant beam, ignores armor; remains affected by escort protection |
| Crucible | 100 | 36 impact | 220 | 96 ticks | Shell speed 270, radius 76, half secondary damage; detonates at last tracked position if its target dies |

| Enemy | Health | Speed | Bounty | Breach | Defense |
|---|---:|---:|---:|---:|---|
| Runner | 55 | 54 | 2 | 1 | None |
| Bulwark | 180 | 38 | 5 | 2 | 14 flat armor against impact |
| Warden | 160 | 43 | 7 | 2 | Protects non-Warden allies within 100 units for 40% less damage |
| Mender | 135 | 36 | 6 | 2 | Heals nearby non-Menders; telegraphed pulse |
| Raider | 65 | 94 | 0 | 1 | Fast reinforcements; no bounty farming |
| Captain | 400 | 28 | 15 | 3 | 20 armor; 25% slow resistance |
| Bellwarden | 3,200 | 19 | 50 | 12 | Personal guard/rally/enrage; 50% slow resistance |

Health scales to 100%, 115%, 140%, 165% across stages; movement, bounty, armor and damage stay unchanged. Spawned enemies carry their scaled maximum health (boss has 5,280 in wave 12). The inspection panel reports the scale and enemy tooltips show actual values. Slow resistance reduces the strongest active slow by that percentage, rather than adding another immunity rule.

Armor is applied first, then the strongest active escort reduction, rounded down. Each damaging hit deals at least 1; credited damage is capped by remaining health. Warden auras do not stack or protect other Wardens. Killing a Warden ends its protection immediately, including later hits in the same tick. Aura radius, linked shield outline and an enemy tooltip make protection visible. Shell splash resolves by entity ID for reproducibility; beam timing and projectile travel are distinct tactical choices.

| Wave | Composition | Clear supplies | Decision |
|---|---|---:|---|
| 1 First watch | 10 Runners | 50 | Two-tower opening/coverage |
| 2 Iron footsteps | 12 Runners, 2 Bulwarks | 74 | Fund a specialization or a third tower |
| 3 Iron standard | 14 Runners, 3 Bulwarks, Captain | 32 | First elite; claim first relic |
| 4 Mending march | 14 Runners, 3 Bulwarks, 2 Menders | 24 | Interrupt healing with Support priority |
| 5 Sheltered rush | 20 Runners, 4 Bulwarks, 2 Wardens | 24 | Shields plus dense packs |
| 6 Break the convoy | 18 Runners, 4 Bulwarks, 2 Menders, 2 Wardens, Captain | 32 | Sustained escort; second relic |
| 7 Outriders | 16 Runners, 6 Raiders, 3 Bulwarks, 2 Menders | 24 | Rear coverage against fast rushes |
| 8 The long siege | 20 Runners, 6 Bulwarks, 3 Wardens, 2 Menders | 24 | Sustained damage through staggered support |
| 9 Twin standards | 20 Runners, 5 Bulwarks, 3 Menders, 2 Captains | 36 | Dual elites; final relic |
| 10 Black procession | 30 Runners, 6 Bulwarks, 3 Wardens, 3 Menders | 28 | Maintain reactions through overlapping support |
| 11 The last breach | 28 Runners, 6 Raiders, 7 Bulwarks, 3 Wardens, 3 Menders | 40 | Complete late coverage before the boss |
| 12 Bellwarden | Boss, 16 Runners, 4 Bulwarks, 2 Menders, 2 Wardens | 50 | Escort priority, exposed windows and finite rallies |

The 180-gold, one-tower, 8/12/16-Runner fixture remains runnable as `foundation.json`, with its original movement/stats/timing. The previous six-wave economy and branch values remain in `emberwatch.json`. These are regression scenarios; the active game loads only the expedition. Legacy formats are not supported. Do not display nonfunctional circuit/save buttons.

## Earned rewards and optional pressure — implemented

After clearing waves 3/6/9, choose one of three distinct relics. Starting the next wave and changing the contract are blocked until choosing; invalid/repeated claims do nothing. Owned relics are excluded from later drafts. The seeded shuffle prefers one combat reward relevant to a tower currently built and one supply/recovery option when available, then fills the remaining slots. It can also offer a reason to pivot. It does not guarantee every card matches the current build. Two runs with the same seed and commands produce identical offers and outcomes.

| Relic | Effect | Constraint/trade-off |
|---|---|---|
| Winter Glass | Chill attacks apply one extra stack | Still capped at three; requires an impact finisher to monetize setup |
| Split Lens | Beams hit one other nearby enemy for 50% damage, carrying status/reaction | 72-unit radius, one hop, no recursive forks |
| Barbed Relay | Bolts fork with the same rules | Requires nearby second target; travel still matters |
| Echo Spindle | Every shell adds a 10-damage delayed blast; existing echoes gain 10 damage | Radius at least 76; no status/reaction from echo; retained normal cadence |
| Kiln Heart | Ignition lasts 60 ticks longer and extends radius by 24 | Same six-field cap, damage amount and shared pulse cadence |
| War Chest | Gain 90 gold now | Forgo a lasting behavior change for purchase timing |
| Reinforced Gate | Heal 4, increase maximum lives by 2 | Heal clamps to the new maximum; forgo a combat modifier |

Attack modifiers apply to existing and future towers and their future specializations. Rebuild effective definitions from the unmodified content plus all owned relics so repeated purchases cannot compound effects. Preserve cooldowns, priorities and statistics. Recovery rewards pay once. Choice UI shows the build and current resources; I reviews ownership. No persistent stat bonuses are implemented.

Overrun is a per-wave, opt-in contract, reset after clearing. Add `2 + floor(wave / 3) * 3` Raiders at tick offsets 90, 135, …; show the exact count before accepting. Bonus is `20 + 2 * wave` gold only if the wave clears with zero breaches. Clear supplies still pay after nonfatal leaks. Raiders and boss summons have zero bounty. Players can change the contract before starting, never after. This is the first Countermarch prototype; it is not adaptive punishment based on the player's build.

Wave reports retain kills, breaches, duration, total income, clear/bonus payment and contract outcome. The current result UI summarizes these; the CLI includes every wave and tower. Persistent comparison analytics remain planned.

## Full run — planned

Use human observations to validate the eventual 25–40-minute pacing target. Eighteen waves/three acts was an initial hypothesis; retain the current four-stage structure if it produces stronger decisions. Expand relic diversity toward twelve, forecast two waves, add distinct contract formations and vary maps by seed. Do not pad duration with redundant waves. No hidden adaptive hard counters.

Currency creates opportunity costs between towers, branches and links. Avoid mandatory click-to-collect income. Two authored maps receive validated variations. Godot editor authoring comes first; a player map editor is optional.

## Tower evolution — implemented

| Family | Base role | Branch A | Branch B |
|---|---|---|---|
| Boltworks | Precision impact | Pinning Ballista: brief pin/precision | Pitchbow: lower damage, oil |
| Prism | Elemental preparation | Rime Lens: chill/Shatter setup | Sun Lens: fire/Ignition |
| Crucible | Area coverage | Breach Mortar: armor fracture | Echo Mortar: delayed second impact |

One permanent specialization per tower in this slice. Purchase is atomic: reject invalid families, unknown paths, insufficient funds, sold towers and second choices without mutation. Preserve cooldown, priority, statistics and source ID; projectiles already in flight retain their original attack definition. Selling refunds 70% of base plus branch cost. No respec or additional tier is implemented; evaluate these against the expedition feedback before growing the tree. All branch attacks and status references are data, not tower subclasses.

| Path | Additional cost | Damage / interval | Range | Mechanical change |
|---|---:|---|---:|---|
| Pinning Ballista | 110 | 55 impact / 78 ticks | 245 | Bolt speed 650; pin slows 80% for 30 ticks; slower fire and longer reach |
| Pitchbow | 70 | 12 impact / 32 ticks | 195 | One oil stack per hit; sacrifices direct damage to prepare Ignition |
| Rime Lens | 80 | 14 frost / 36 ticks | 180 | One chill per hit; trades individual hit strength for slowing and Shatter setup |
| Sun Lens | 100 | 32 fire / 54 ticks | 200 | Consumes oil for Ignition; fire coverage with a preparation payoff |
| Breach Mortar | 110 | 44 impact / 102 ticks | 220 | Splash radius 82; fracture on primary and secondary hits, before damage |
| Echo Mortar | 120 | 34 impact / 108 ticks | 235 | Splash radius 76; one 22-impact, radius-88 echo after 30 ticks at the impact point |

All non-impact damage bypasses armor; escorts still reduce it. Mortars retain half damage to secondary targets. Echoes damage every enemy in their radius equally, retain credit after sale, and cannot apply statuses, react or create more echoes. Pin is a bounded slow, not displacement: it preserves route progress and cannot fully immobilize an enemy. Branch silhouettes, frost/fire beams, status letters/counts, ignition ground and echo countdown rings expose the mechanical changes. Exact effect values are also available in path-card tooltips.

## Reactions and terrain — implemented rules

| Status | Stack cap | Lifetime per stack | Effect |
|---|---:|---:|---|
| Chill | 3 | 180 ticks / 3 seconds | 15% slow per stack; two stacks can be shattered |
| Oil | 1 | 240 ticks / 4 seconds | Prepared for Ignition; no passive damage |
| Pin | 1 | 30 ticks / 0.5 seconds | 80% slow |
| Fracture | 2 | 180 ticks / 3 seconds | Remove 6 armor per stack, minimum zero |

Each stack owns its source ID and expiry. At capacity a new application replaces the oldest stack; consuming a reaction removes the oldest required stacks. Stacks expire before movement at `ExpiresAt <= Tick`. Different slows use the strongest effect, never their sum; the validator caps any status at 80%. Fracture adds armor reduction. Pause freezes all expiry, cooldown and delayed-effect timers.

- **Shatter:** an ordinary impact hit consumes two pre-existing chill stacks, then deals its normal hit and a 32-frost burst in radius 64. Per-enemy Shatter cooldown: 60 ticks. It still bursts if the ordinary hit kills the prepared enemy.
- **Ignition:** an ordinary fire hit consumes one oil stack, then creates burning ground at the enemy's position. Radius 72; 8 fire damage immediately and every 30 ticks for 180 ticks, with no pulse on the expiry tick. Per-enemy reaction cooldown: 120 ticks. Maximum six fields globally; creating another replaces the oldest. Each enemy has one damage cadence per reaction, so even staggered overlapping fields cannot multiply burn frequency. Entering a field takes damage on its next eligible pulse. Kiln Heart changes duration/radius as listed above.

Reactions inspect/consume existing setup before the incoming attack applies its own status. Thus an attack cannot generate and consume a new stack at once. Reaction damage and echoes never enter the reaction pipeline. Primary/splash hits can independently trigger eligible enemies; a consumed stack cannot be spent twice. Separate query buffers protect splash iteration from nested bursts. Natural hit ordering is deterministic, not simultaneous in the mathematical sense.

Credit actual reaction damage and kills to the triggering tower, including after its sale. Credit one preparation to each distinct tower contributing consumed stacks and one trigger to the finisher. A tower's damage total already includes its reaction damage: do not add it twice. An oldest eligible overlapping field owns the burn damage. Reaction statistics survive wave clears; fields and pending echoes clear when the wave ends. No persistence to disk exists yet.

Ignition is the first temporary terrain interaction. Frozen ground, freeform walls, flooding and water/electricity are deferred. Oil currently belongs to an enemy, not a ground surface. Enemy disruption and broader terrain counterplay remain RE2-05.

## Resonance Circuits — after expedition playtesting, not implemented

Directed links: four-tile maximum, 25 gold initial cost, one outgoing/two incoming ports. Reject cycles/invalid endpoints before spending. Rewire between waves without another fee; no investment refund for rewiring. Removing towers removes their links.

A natural reaction generates at most one pulse per source per second. One-hop transmission, three-charge capacity, discard overflow. Charges persist between waves. Three charges empower the next eligible attack. Empowered attacks cannot generate pulses, though ordinary bounded effects still resolve.

Boltworks pierces one extra target; Prism spreads status locally; Crucible adds delayed secondary impact. UI shows ports, direction, charge and actual empowered behavior using text/moving markers plus color. Link layout must compete with ordinary coverage.

## Enemies and boss

| Enemy | Pressure | Counter |
|---|---|---|
| Runner | Fast leaks | Coverage/slow/progress targeting |
| Bulwark | Armor | Breach/elemental preparation |
| Warden | Shields nearby allies | Support priority/split formations |
| Mender (implemented) | Sustains non-Mender allies | Support priority; kill before the windup ends |
| Raider (implemented) | Rapid reinforcement pressure | Area coverage and rear defenses |
| Captain (implemented elite variant) | Durable armor and partial slow resistance | Fracture and sustained elemental damage |
| Sapper (planned) | Temporary circuit disruption | Telegraph and interrupt by killing |
| Broodling (planned) | Splits into weaker enemies | Area coverage; exact-once rewards |

Menders restore 12 health to eligible allies within 105 units every 150 ticks, preceded by a 45-tick green windup. Resolve after player damage, so a killed Mender cannot pulse. No self/other-Mender healing or resurrection. A target receives at most one heal on a given tick; staggered pulses can both help. Healing clamps to scaled maximum health.

**Implemented Bellwarden:** personal 45% guard and 20 armor open during a 90-tick windup before each rally. Rally every 300 ticks: two Raiders placed behind the boss, at most twelve total. At half health, enrage latches, speed rises 25%, rally interval halves and the next rally is brought closer without skipping its warning. Once reinforcements run out the personal guard/armor remain down. External Wardens can still protect it. All timers pause. No repeatable summon bounty. Kill supports, manage the fast rush, exploit exposed windows; the boss bar reports both phases.

The earlier concept of summoning supports and suppressing a circuit is superseded for this slice. Reevaluate disruption only after Circuits work; adding it is not a requirement merely because it appeared in an earlier document.

## Replayability and progression

Seven relics are implemented; expand toward twelve when each supports a distinct decision. Do not add permanent stat bonuses to compensate for weak run rewards. Future profiles should favor challenges and alternative options; any power progression needs a separate balance decision and fresh-profile victory evidence. No monetization/online services are included.

## Presentation and controls

Clear planning/running/paused/victory/defeat states; economy/lives/wave progress and actionable tower information. Every keyboard action also has a visible control. Scale UI, keep pointer world transforms correct, show disabled states. Essential cues use text/shapes, not color alone. Focus loss pauses and sound can be muted. Later settings add remapping, reduced motion and independent buses.

First slice uses original procedural shapes/PCM cues. Final art follows the vision's arcane frontier style and consistent scale/palette/silhouette. Do not confuse placeholder rendering with final commercial polish.

## Analytics and validation

Full analytics: per-tower damage/kills/investment, spending/income, enemy defeats, reaction damage, pulse generation/spending and wave lives/duration. Support value needs context beyond damage-per-gold. Record map/seed/content/difficulty/profile/relics/branches/outcome. First slice shows only tracked statistics.

Future stress target: 1080p, ten minutes, 500 enemies/64 towers/2,000 projectiles, p95 frame ≤20 ms and simulation ≤4 ms on a recorded reference machine. Not a measured claim. Three distinct builds finish standard expedition seeds 1/7/42; passive and unspecialized controls lose. Frost currently retains more lives than fire. Those command strategies demonstrate solvability, not optimal balance or human enjoyment; [EXPEDITION_REWORK.md](EXPEDITION_REWORK.md) records the comparison and next playtest gate.

## Experiments and exclusions

Battlefield Memory, Evacuation Couriers and Echo Blueprints require bounded prototypes and keep/discard reviews (see vision). Multiplayer, 3D migration, unrestricted procedural maps, mobile release and broad mod support are not MVP dependencies.
