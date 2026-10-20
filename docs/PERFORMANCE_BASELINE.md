# Simulation performance baseline

Measured 2026-10-20 for RE1-04/05. Reproduce with `.\dev.cmd benchmark`. Machine-reported runtime: .NET 8.0.0, Windows 10.0.19045, `Intel64 Family 6 Model 140 Stepping 1, GenuineIntel`. Debug build, no debugger. This does not measure the exported runtime, rendered frame time or GPU performance.

The stress scenario has 64 towers on a 64×32 map, a cached serpentine route and one spawn per tick. Towers deal 1 damage, with radius-60 splash, range 190, 30-tick intervals and projectile speed 480. Enemies have 100,000 health and speed 120, so they remain alive throughout measurement. This isolates dense targeting/splash; it is not a balance scenario or a mass-Warden test.

Each run warms up for 1,000 ticks, then measures 1,500 ticks, growing from 1,001 to 2,500 enemies. The last tick has 36 projectiles in flight. Samples include `Simulation.Step()` and event draining. Scan/grid order alternates over three rounds. All six runs produce the same gameplay checksum:

`b21d0cc8066a13275245727eaa7b0df4d169a4023cb7e703b860aa453b79186f`

| Round | Query method | Mean ms/tick | Median ms/tick | p95 ms/tick | Allocated bytes/tick |
|---|---|---:|---:|---:|---:|
| 1 | Reference scan | 4.362 | 4.315 | 6.731 | 4,138 |
| 1 | Spatial grid | 0.920 | 0.864 | 1.568 | 4,197 |
| 2 | Spatial grid | 0.884 | 0.876 | 1.344 | 4,197 |
| 2 | Reference scan | 4.156 | 4.062 | 6.273 | 4,138 |
| 3 | Reference scan | 4.112 | 4.127 | 6.143 | 4,138 |
| 3 | Spatial grid | 0.885 | 0.870 | 1.349 | 4,197 |

Raw report: `.artifacts/spatial-benchmark.json`. Timing is diagnostic, never a flaky test gate. Results justify the bounded grid and retained bucket/query capacity; they do not justify pooling every object or claim the future graphics/stress budget is met. Allocations include event arrays and new entities/projectiles.

Correctness checks are independent of timing:

- 600 seeded radius queries against a separate reference expression over 5,002 enemies: exact boundaries, outside-map centers, zero/large radii, deaths after indexing, movement and empty rebuilds.
- Two 2,400-tick combats compare every event batch and periodic/final checksums between grid and scans, covering splash and a mixed roster with armor, shields, beams and all targeting priorities.
- Both shipped encounters terminate in winning and no-defense losing scenarios. The three-wave fixture retains its original 3,004-tick winning result.

The later production gate still requires a recorded reference PC, optimized/exported runtime, ten-minute rendered tests, 500 enemies/64 towers/2,000 projectiles, lifecycle/memory observation and representative effect/aura density.
