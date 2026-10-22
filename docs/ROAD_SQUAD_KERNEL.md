# FR-01 — road squad kernel

Implemented 2026-10-21 from `856cb2d` on `Ege-redevelop`. This is the simulation milestone. Normal Godot play still has three tower cards; Garrison selection, rally controls, animations and visible melee warnings are **FR-02**, not implemented here.

## Commands and content

`GameDefinition.Garrison` is an optional, separately validated structure, not a projectile tower. `EnemyDefinition.Melee` defines retaliation explicitly for every enemy in a squad-enabled mission. The older three/six-wave fixtures omit these fields. `expedition.json` supplies the provisional values below without changing existing tower, wave, reward or enemy movement values.

- `PlaceGarrison(foundation)`: spend 120 run gold, occupy one foundation and create three guards with stable IDs. One Garrison for the entire run; no sale/rebuild command in this prototype. Towers and Garrison cannot share a foundation. Invalid placement spends nothing and emits no event.
- `RallySquad(position)`: accept a finite point on the authored road centerline. Command reach is the foundation-to-road access distance plus route travel to the marker, at most 230 world units. It is not a circle that permits shortcuts across a winding route. The nearest road projection is the fixed entrance; ties choose earlier progress. `IsSquadRallyReachable` exposes the same rule for the future preview.
- `RetreatSquad()`: release opponents and walk along the road to the entrance, then back to the building. Rally/retreat cancels pending strikes but retains cooldowns, health and casualties. Orders never teleport or heal. Repeated identical orders are rejected without mutation.

Movement occurs during unpaused wave ticks. During planning, orders change the destination; the next wave starts deployment. The access spur is straight and the road traversal follows the immutable orthogonal route. This does not implement freeform navigation, obstacle avoidance or arbitrary rerouting.

## Provisional combat values

| Defender | Health | Impact damage | Speed | Reach | Attack interval / warning |
|---|---:|---:|---:|---:|---|
| Each Shieldguard | 80 | 8 | 100 units/s | 20 units | 60 / 15 ticks |

| Enemy | Melee damage | Reach | Interval / warning | Blocking response |
|---|---:|---:|---|---|
| Runner | 8 | 24 | 60 / 20 ticks | One guard |
| Bulwark | 20 | 24 | 90 / 30 ticks | One guard; armor reduces guard damage |
| Warden | 10 | 24 | 60 / 20 ticks | One guard; escort shielding still applies |
| Mender | 6 | 24 | 90 / 30 ticks | One guard; existing healing remains active |
| Raider | 10 | 24 | 42 / 14 ticks | One guard; excess raiders pass |
| Captain | 40 | 28 | 90 / 30 ticks | Blockable; two hits defeat a full-health guard |
| Bellwarden | 70 | 76 | 180 / 60 ticks | Never blocked; warned cleave hits every nearby living guard |

One second is 60 ticks. Guard/enemy damage is not scaled by the existing wave health multiplier. Boss enrage still changes the existing movement/rally behavior; it does not secretly shorten the new cleave warning. A cleave's area follows the advancing boss and samples actual guard positions at contact. Moving out of reach during the warning avoids the hit. Killing the boss before contact cancels it.

Each stationary guard can engage one ordinary enemy. Guards only acquire opponents after reaching their rally marker; moving/retreating/fallen guards cannot block. Swept route contact prevents fast enemies tunneling through an available guard. Excess enemies pass, including when a fourth enemy crosses three occupied guards. Stable enemy/guard IDs resolve ties. Corpses immediately release both sides.

Basic melee uses the shared armor/shield/damage/bounty resolution, with separate guard/Garrison contribution. It never enters the status/reaction pipeline: guards do not consume chill/oil, trigger Shatter, apply statuses, fork or echo. Relic attack modifiers still belong to towers. Actual damage is capped by remaining enemy health and bounty is paid once.

No healing or resurrection occurs while a wave is active, including at the building. On entering the next planning phase, all three guards fully heal/revive and regroup at the building for free. IDs, accumulated damage, kills, casualties and engagement ticks persist. The rally marker and retreat order persist. Victory/defeat releases engagements but does not revive units. These are explicit prototype recovery rules, not a save system.

## Simulation and presentation contract

Tick order: due spawns → guard travel → existing enemy preparation/status expiry and swept engagement/movement → spatial/escort rebuild → towers/projectiles/echoes/areas → guard melee → enemy melee → existing enemy healing/summons → leaks and outcome → planning recovery if applicable.

Tower/effect damage can kill an opponent before its due retaliation. Guard damage also precedes enemy retaliation. Both windup and cooldown are integer tick state, frozen by pause. Repositioning cancels a windup but cannot reset its cooldown. Wave completion still depends only on pending spawns and living enemies, never on fallen or traveling guards.

`GarrisonState.Guards` exposes position/previous position, travel state, health, engagement, attack target/contact tick and cooldown. `EnemyState` exposes engaged guard, melee target/contact tick and cooldown. New events cover building, rally/retreat, engagements/releases, both windups, guard hits/falls and recovery. Ordinary enemy `Hit` events from guards have `EffectId = "melee"`. FR-02 must render these states/events; animation must never decide hit timing.

Checksums include squad placement/orders, positions, health, IDs, engagement pairs, attack timers and contributions. Content without a Garrison retains the prior checksum representation. No persistence format or cross-version checksum compatibility is promised for the active expedition.

## Verification and balance evidence

- `dev.cmd test`: **96 passed**, including 20 new squad cases; zero build warnings/errors. Cases cover atomic commands, route reach/corners, fast contact/capacity, deaths, recovery, exact bounty/credit, setup preservation, Captain attrition, boss escape, pause, different speeds/render rates and grid/reference reproducibility. A complete seed-42 frost squad expedition reproduces across direct ticks, reference queries and 144 FPS/4×.
- `dev.cmd smoke`: existing twelve-wave Godot UI flow passes. The sandbox printed a root certificate-store diagnostic; this is recorded rather than treated as a clean engine log. This check covers the existing UI, not squad controls or artwork.
- Existing no-squad frost/fire/steel witnesses still win seeds 1/7/42 with the preceding life totals. Three-wave fixture: 3,004 ticks, 36 kills, no leaks. Six-wave evolution: 9,060 ticks, 151 kills, no leaks.
- A new opt-in `--squad` witness reserves foundation 3, attempts recruitment from the planning phase before wave 3, then follows the existing purchase/relic priorities while skipping that foundation's tower. It uses ordinary commands and is never enabled in normal play.

After `dev.cmd test`, reproduce the experiment:

```powershell
dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --balance --squad
dotnet Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll --strategy frost --seed 42 --squad
```

| Strategy | Seed 1 | Seed 7 | Seed 42 |
|---|---|---|---|
| Frost + squad | Win, 5 lives | Lose, wave 6 | Win, 6 lives |
| Fire + squad | Lose, wave 12 | Win, 7 lives | Lose, wave 12 |
| Steel + squad | Lose, wave 6 | Lose, wave 6 | Lose, wave 6 |

All 18 squad-batch scenarios terminate, including basic/hold/none controls. Squad damage, kills, casualties and summed guard engagement ticks appear in the CLI report; engagement time is not a measurement of damage prevented. Raw local reports: `.artifacts/squad-balance.jsonl` and `.artifacts/squad-no-garrison-balance.jsonl`.

This witness spends money earlier and removes a tower slot; it does not isolate the value of blocking or establish an optimal squad layout. The frequent losses identify purchase timing, location and survivability as FR-02 balance questions. Do not claim general balance or enjoyment from the three winning cases. Keep strong no-Garrison alternatives and compare deliberate squad strategies before increasing permanent power or broadening the roster.

## Next: FR-02

Add the Garrison card/selection and occupied-foundation rendering, road-marker reach preview, rally/retreat controls and paused orders. Derive walk, anticipation, attack, defend, hurt and fall animation from Core. Show boss cleave reach/warning distinctly from its existing rally windup. Explain permanent foundation commitment and planning recovery. Extend the actual UI smoke harness, inspect 1×/2×/4× and pause, then tune and compare useful Garrison/no-Garrison builds. Export only after that coherent playable slice; the existing Windows ZIP predates FR-01.
