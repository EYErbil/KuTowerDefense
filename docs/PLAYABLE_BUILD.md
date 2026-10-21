# Emberwatch — REDESIGN: EVOLUTION six-wave defense

Run `Redesign-Evolution.exe` on Windows x64. Keep the `.pck` and `data_Redesign.Game_windows_x86_64` directory beside it. No Java, Godot editor or separate .NET installation is required.

Protect the citadel for six waves. You start with 245 gold: enough for one of each tower. Select a tower card, then click a marked stone foundation. Cover several sections of road to get more attacks from each tower. Defeated enemies provide gold for more defenses and specializations. Click a tower to inspect its range/statistics, evolve, change targeting or sell it. Read the wave forecast, then start when ready. Building and selling work during combat and tactical pause.

- **Boltworks — 60 gold:** fast physical bolts for reliable coverage.
- **Prism — 85 gold:** instant energy beams ignore armor.
- **Crucible — 100 gold:** slower shells damage groups; nearby enemies take half the direct damage before defenses.
- **Runners:** light, fast pressure. **Bulwarks:** 9 armor blocks impact damage; use Prism. **Wardens:** reduce nearby allies' damage taken by 40%; prioritize Support to break the escort. Wardens cannot shield one another, and protection never stacks.

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

Priorities cycle through First (nearest the gate), Strongest (highest current health), Support (Wardens first), and **Primed** (ready for this tower's reaction, then nearest the gate). Try Primed on impact towers near Rime and on Sun Lens near Pitchbow. Slowing a target can otherwise make First switch to another enemy. Tower inspection credits both preparation and triggering; results show reaction totals.

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
| Close path choices / cancel placement or selection | Escape |
| Mute | Button or M |
| Restart after result | Button or R |

This short prototype has three tower families, six exclusive paths, two reactions, three enemy roles and one authored map. Circuit links, relics, bosses, save/resume, final art and complete 25–40-minute runs are later milestones. The artwork and attack/reaction sound cues are original procedural prototypes. No legacy art/music is shipped. Automated winning runs establish feasibility, not final difficulty balance or human playtest results.

Godot and its included component notices are in GODOT-NOTICES.txt; .NET runtime notices accompany the runtime. No license for the project's original source/art is granted by those third-party notices.
