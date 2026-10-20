# Emberwatch — REDESIGN: EVOLUTION six-wave defense

Run `Redesign-Evolution.exe` on Windows x64. Keep the `.pck` and `data_Redesign.Game_windows_x86_64` directory beside it. No Java, Godot editor or separate .NET installation is required.

Protect the citadel for six waves. You start with 245 gold: enough for one of each tower. Select a tower card, then click a marked stone foundation. Cover several sections of road to get more attacks from each tower. Defeated enemies provide gold for more defenses. Click a tower to inspect its range/statistics, change targeting or sell it. Read the wave forecast, then start when ready. Building and selling work during combat and tactical pause.

- **Boltworks — 60 gold:** fast physical bolts for reliable coverage.
- **Prism — 85 gold:** instant energy beams ignore armor.
- **Crucible — 100 gold:** slower shells damage groups; nearby enemies take half the direct damage before defenses.
- **Runners:** light, fast pressure. **Bulwarks:** 9 armor blocks impact damage; use Prism. **Wardens:** reduce nearby allies' damage taken by 40%; prioritize Support to break the escort. Wardens cannot shield one another, and protection never stacks.

Tower priorities cycle through First (nearest the gate), Strongest (highest current health), and Support (Wardens first, then nearest the gate). Hover an enemy for its health/defenses. Selling returns 70% of the purchase price.

| Action | Control |
|---|---|
| Build Boltworks / Prism / Crucible | Respective card or B / P / C, then click a foundation |
| Inspect | Click an existing tower |
| Cycle target priority | Selected tower's Target button or T |
| Sell | Selected tower's Sell button |
| Start next wave | Button or N |
| Tactical pause/resume | Button or Space |
| Simulation speed | Buttons or 1 / 2 / 4 |
| Cancel placement/selection | Escape |
| Mute | Button or M |
| Restart after result | Button or R |

This short playable foundation has three towers, three enemy roles and one authored map. Circuit links, elemental reactions, branching upgrades, relics, save/resume, final art and complete 25–40-minute runs are later milestones. The artwork and distinct bolt/beam/shell sound cues are original procedural prototypes. No legacy art/music is shipped. Automated winning runs establish feasibility, not final difficulty balance or human playtest results.

Godot and its included component notices are in GODOT-NOTICES.txt; .NET runtime notices accompany the runtime. No license for the project's original source/art is granted by those third-party notices.
