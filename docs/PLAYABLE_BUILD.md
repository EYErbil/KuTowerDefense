# Emberwatch Expedition — REDESIGN: EVOLUTION

Run `Redesign-Evolution.exe` on Windows x64. Keep the `.pck` and `data_Redesign.Game_windows_x86_64` directory beside it. No Java, Godot editor or separate .NET installation is required.

Protect the citadel through twelve waves, from the Outpost to Last Light. You start with 180 gold and 12 lives: choose two towers with overlapping coverage. Select a tower card, then click a marked stone foundation. Read the next forecast and use the guaranteed wave-clear supplies to expand and specialize. Early supply payments help you establish a build; later rewards demand more careful spending. Building, selling and evolving work during combat and tactical pause.

After waves **3, 6 and 9**, choose one of three relics. Winter Glass accelerates chill setup; Split Lens and Barbed Relay fork attacks; Echo Spindle adds delayed shell impacts; Kiln Heart expands burning ground. War Chest funds an immediate purchase; Reinforced Gate repairs the citadel and increases its maximum lives. Choices last for this run. A new expedition has a new seed and different offers; I opens your collection.

**Overrun is optional.** Before a wave, its button/tooltip states the exact extra Raiders and bonus. They arrive 1.5 seconds after the wave begins, 0.75 seconds apart. Earn the bonus only if nobody breaches; the ordinary clear reward remains available even after leaks. Raiders give no kill gold. These reinforcements escalate throughout the run: taking every contract can defeat a defense that survives standard waves. Leave it off when your coverage is strained.

- **Boltworks — 60 gold:** fast physical bolts for reliable coverage.
- **Prism — 85 gold:** instant energy beams ignore armor.
- **Crucible — 100 gold:** slower shells damage groups; nearby enemies take half the direct damage before defenses.
- **Runners:** light pressure. **Raiders:** faster rushes without bounty. **Bulwarks:** 14 armor reduces impact; use energy/fire/frost or fracture.
- **Wardens:** reduce nearby allies' damage taken by 40%. **Menders:** heal nearby non-Mender allies; a green ring warns of the pulse. Support targeting helps kill these units before their escorts overwhelm you.
- **Captains:** 20 armor, high health, reduced slowing. Specialize before their standards arrive.
- **Bellwarden, wave 12:** 45% personal guard and 20 armor, both removed during its 1.5-second rally windup. It summons pairs of Raiders, at most twelve. At half health it moves 25% faster and rallies twice as often. Once its reinforcements are exhausted it stays exposed. Wardens can still shield it: kill the escort. The boss bar reports its phase and vulnerability. A boss breach costs 12 lives.

Select a tower and choose **Evolve [U]** for two permanent paths. The panel pauses combat and keeps it paused after choosing or closing; press Space when ready. Hover a path card for precise effect values. Selling returns 70% of the base cost plus specialization investment.

| Family | Path | Extra gold | Role |
|---|---|---:|---|
| Boltworks | Pinning Ballista | 110 | Longer reach and heavy bolts briefly pin enemies |
| Boltworks | Pitchbow | 70 | Lower damage, faster bolts apply oil |
| Prism | Rime Lens | 80 | Chill slows enemies and prepares Shatter |
| Prism | Sun Lens | 100 | Fire consumes oil to ignite the road |
| Crucible | Breach Mortar | 110 | Splash applies armor fracture |
| Crucible | Echo Mortar | 120 | Lower initial damage, delayed second blast |

**Two chill stacks + impact = Shatter:** a frost burst damages nearby enemies. **Oil + fire = Ignition:** burning ground damages passing enemies for three seconds. Overlapping fire shares a damage timer; stacking fields does not multiply burn frequency. Pair preparation and payoff towers with overlapping range. Status letters/counts identify chill (C), oil (O), pin (P), and fracture (F); hover enemies for names, expiry, effective armor and slow. Dashed Echo rings show the delayed impact point.

Priorities cycle through First (nearest the gate), Strongest (highest current health), Support (Wardens and Menders first), and **Primed** (ready for this tower's reaction, then nearest the gate). Try Primed on impact towers near Rime and on Sun Lens near Pitchbow. Slowing a target can otherwise make First switch to another enemy. Tower inspection credits both preparation and triggering; results show reaction totals, chosen relics, cleared waves and contracts.

| Action | Control |
|---|---|
| Build Boltworks / Prism / Crucible | Respective card or B / P / C, then click a foundation |
| Inspect | Click an existing tower |
| Evolve | Selected tower's Evolve button or U |
| Cycle target priority | Selected tower's Target button or T |
| Sell | Selected tower's Sell button |
| Start next wave | Button or N |
| Tactical pause/resume | Button or Space |
| Simulation speed | Buttons or 1 / 2 / 4 |
| Claim earned relic | Card or 1 / 2 / 3 while choosing |
| Review owned relics | Relics button or I |
| Accept/decline optional reinforcements | Overrun button before starting a wave |
| Close review/path choices / cancel placement or selection | Escape; earned rewards must be chosen |
| Mute | Button or M |
| New expedition after result | Button or R |

This expedition prototype has three tower families, six exclusive paths, two reactions, seven relics, seven enemy types including one boss, and one authored map. Enemy health grows at stage boundaries and is shown in battlefield inspection and enemy tooltips. Scripted victories take about 5.5–7.5 minutes of simulated combat plus human planning; this is not yet the complete 25–40-minute game. Circuit links, additional maps, save/resume and final art remain later milestones. The artwork and sound cues are original procedural prototypes. Automated wins do not establish final difficulty or enjoyment. There is no permanent power grind.

For a repeatable run, launch `Redesign-Evolution.exe -- --seed=42`. The seed is also shown in relic review and results. New expedition advances the seed; restarting the application normally chooses a fresh one.

Godot and its included component notices are in GODOT-NOTICES.txt; .NET runtime notices accompany the runtime. No license for the project's original source/art is granted by those third-party notices.
