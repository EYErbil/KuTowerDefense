# Expedition rework — player feedback, 2026-10-20/21

The six-wave slice is mechanically functional but repetitive and too generous. Its evolved witness wins without leaks, leaves 975 gold and never uses a downstream upgraded mortar. That is evidence of weak pacing, not a balanced game. The player explicitly requested more rewarding progression, content and difficulty. This work takes priority over Circuits.

Deliver a 12-wave expedition on the existing road: distinct formations and escalating acts, three earned one-of-three relic choices, optional forecast reinforcement contracts, a healing support enemy and a telegraphed two-phase Bellwarden. Lower bounty income; pay a reliable wave-clear stipend so a leak does not destroy all recovery options. Rewards should change the build or enable a deliberate recovery investment. No permanent stat grind is needed.

- [x] **EX-01 Run progression** | P0 | deps RE2-03 | L | MVP | AC: seeded nonduplicate relic offers after waves 3/6/9; atomic choice; visible ownership; wave advance blocked until choosing; existing projectile snapshots preserved; repeatable commands/checksum.
- [x] **EX-02 Encounter pressure** | P0 | deps EX-01 | L | MVP | AC: twelve distinct forecasts, new healer/capped boss summons and visible vulnerability window; optional challenges state exact threats/reward before commitment; failed challenges pay no bonus.
- [x] **EX-03 Balance witnesses** | P0 | deps EX-02 | M | MVP | AC: independently simulated frost/fire/physical builds, passive and underbuilt controls, resource/leak/wave reports across fixed seeds; document failures and limits rather than assert universal balance.
- [x] **EX-04 Playable delivery** | P0 | deps EX-03 | M | MVP | AC: reward/contract/boss UI, actual engine flow checks, readable exported captures, passing regressions, self-contained Windows build and updated handoff.

Keep the prior six-wave content and three-wave foundation as regression scenarios. The active game becomes the expedition. Keep the architecture and existing art direction; avoid a rendering rewrite while the run loop is the problem. Human playtesting decides enjoyment, while automated runs measure solvability, economic pressure and regressions. This intermediate run is not a claim that the eventual 25–40-minute campaign is finished.

## Implemented structure and tuning evidence

Four stages of three waves: Outpost, Convoy, Breach, Last Light. Each of the first three ends in a relic draft; the fourth ends with the Bellwarden. Seven relics change status setup, secondary attacks, burning terrain or purchase/recovery options. Starting income falls from 245 to 180 and bounties shrink sharply; reliable clear supplies fund early branching without requiring perfect kills. Explicit health scaling complements authored formation changes. It is not the sole source of difficulty.

The initial balance pass killed all three specialized witnesses before or near their first relic, which defeated the purpose of earned progression. Early supply payments and branch value were increased. A later fire build consistently missed its last Ballista purchase before the boss, so wave 11's supplies increased from 28 to 40. No code inspects the strategy name in actual gameplay; all witnesses use ordinary commands against the same content.

Standard-wave results, seeds 1 / 7 / 42:

| Witness | Outcomes | Remaining lives | Remaining gold | Combat seconds |
|---|---|---|---|---|
| Frost/Shatter | 3 victories | 12 / 11 / 12 | 263 / 261 / 263 | 343.2 / 363.5 / 330.9 |
| Fire/Ignition | 3 victories | 7 / 5 / 5 | 183 / 178 / 178 | 395.5 / 415.6 / 411.6 |
| Physical/fracture/echo | 3 victories | 10 / 12 / 12 | 293 / 388 / 388 | 439.1 / 447.3 / 429.2 |
| Eight base towers, no branches | Defeat wave 11 / 10 / 10 | 0 | 705 / 577 / 577 | Not a claim that all base-only strategies fail |
| Opening two towers, never expand | Defeat wave 3 in all three | 0 | 219 | Establishes that further investment matters |
| No defense | Defeat wave 2 in all three | 0 | 230 | Clear supplies cannot replace defense |

At least six towers deal damage in every specialized witness; one rear Ballista remains unused in a strong frost run. Final gold includes the final clear reward and boss bounty; it is not all spendable before the last fight. These scripts save for a fixed purchase order and do not sell or react tactically mid-wave. They are reproducible feasibility evidence, not optimal play or an exhaustive difficulty sample.

The first Overrun diagnostic was too generous to every specialized build. Raider growth was increased to three per three-wave checkpoint. With every Overrun enabled, frost finishes with 10 / 6 / 10 lives and 11 / 10 / 11 flawless contracts; fire with 5 lives and 11 contracts in each seed; physical loses in wave 5 in all three seeds. Extra gold changes purchase timing, so risk runs can sometimes improve a later outcome. Overruns need more composition variety before being treated as a finished difficulty system.

## Next decision gate

**EX-05** (full task in the roadmap): record at least three human runs. Capture time to first relic, whether the three choices feel distinct, purchases saved for or regretted, unclear breaches, and contracts declined. Frost is currently safer than fire. Reward diversity is limited by seven definitions, one map and fixed formations. Tune those observations before adding Circuits or stretching playtime. No permanent stat grind, automatic enemy adaptation or new framework was introduced.

Raw local reports: `.artifacts/expedition-balance.jsonl`, `expedition-risk.jsonl`; `SimRunner --balance` and `--balance --risk` reproduce them. Archive hashes and final delivery checks belong in DEVELOPMENT_PROGRESS.md.
