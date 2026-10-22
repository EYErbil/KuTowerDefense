# Simulation performance baseline

## Current FR-01 kernel check

Rechecked 2026-10-21 after road squad state/commands/combat were added. The unchanged dense workload has no Garrison, so this measures shared kernel overhead rather than deployed squad performance. All six runs retain checksum `2069a65603354f4bea4bab800bc2b86050d2a4248587be48699b0e7c213c7929`. Raw report: `.artifacts/spatial-benchmark.json`. Same Debug/runtime/machine/method below; timing is diagnostic, not a controlled speedup claim.

| Round | Query method | Mean ms/tick | Median ms/tick | p95 ms/tick | Allocated bytes/tick |
|---|---|---:|---:|---:|---:|
| 1 | Reference scan | 4.246 | 4.183 | 6.497 | 4,999 |
| 1 | Spatial grid | 1.204 | 1.170 | 2.012 | 5,058 |
| 2 | Spatial grid | 1.111 | 1.067 | 1.860 | 5,058 |
| 2 | Reference scan | 4.235 | 4.219 | 6.171 | 4,999 |
| 3 | Reference scan | 4.180 | 4.123 | 6.253 | 4,999 |
| 3 | Spatial grid | 1.064 | 1.038 | 1.755 | 5,058 |

Enemy melee state adds 24 bytes to each newly spawned enemy in this one-spawn-per-tick workload. The squad is bounded to three members, but active squad allocation/timing is not measured by this diagnostic. It does not establish rendered performance or eliminate the later need to profile active troops/effects. A separate complete squad expedition reproduces across grid/reference queries and 144 FPS/4× as correctness evidence.

## Historical expedition kernel check

Rechecked 2026-10-21 after relics, healing and boss state were added. The existing 64-tower/1,001–2,500-enemy workload still agrees across all six grid/reference runs: checksum `2069a65603354f4bea4bab800bc2b86050d2a4248587be48699b0e7c213c7929`. Raw report: `.artifacts/expedition-benchmark.json`. Same Debug/runtime/machine/method described below; graphical capture also ran during part of this diagnostic, so timings are not a controlled before/after comparison.

| Round | Query method | Mean ms/tick | Median ms/tick | p95 ms/tick | Allocated bytes/tick |
|---|---|---:|---:|---:|---:|
| 1 | Reference scan | 5.417 | 5.133 | 9.090 | 4,975 |
| 1 | Spatial grid | 1.315 | 1.156 | 2.339 | 5,034 |
| 2 | Spatial grid | 1.543 | 1.273 | 3.330 | 5,034 |
| 2 | Reference scan | 6.604 | 5.414 | 14.773 | 4,975 |
| 3 | Reference scan | 4.634 | 4.605 | 6.615 | 4,975 |
| 3 | Spatial grid | 1.360 | 1.189 | 2.657 | 5,034 |

The workload measures general kernel overhead, not mass Menders/forks/bosses or rendered performance. A full expedition additionally compares final digests under grid/reference queries and direct ticks versus 144 FPS/4×. No allocation explosion or correctness mismatch was observed; no pooling or broader architecture change is justified by this diagnostic. Production effect-density/GPU/lifecycle gates remain open.

## Historical Phase 2 measurement

Rechecked 2026-10-20 after RE2-01/02/03. Same 64-tower, 1,001–2,500-enemy workload and runtime described below. `.\dev.cmd benchmark` runs this workload against the current kernel. These historical six runs agree on checksum `43eb99888f7ede025a0fe41730f7a16de7dca8752ea274e57509184571fc0af4`.

| Round | Query method | Mean ms/tick | Median ms/tick | p95 ms/tick | Allocated bytes/tick |
|---|---|---:|---:|---:|---:|
| 1 | Reference scan | 3.928 | 3.871 | 5.740 | 4,930 |
| 1 | Spatial grid | 0.977 | 0.970 | 1.493 | 4,989 |
| 2 | Spatial grid | 1.006 | 0.997 | 1.775 | 4,989 |
| 2 | Reference scan | 3.951 | 3.927 | 5.796 | 4,930 |
| 3 | Reference scan | 4.054 | 4.033 | 5.848 | 4,930 |
| 3 | Spatial grid | 0.942 | 0.917 | 1.466 | 4,989 |

The first Phase 2 check exposed 61,005 allocated bytes/tick in the grid mode. A captured stack-expiry predicate allocated once per enemy even when it had no status. Bounded loops removed that closure and captured lookup predicates, reducing this workload to 4,989 bytes/tick with identical checksums. Before/after raw reports are `.artifacts/phase2-benchmark-before.json` and `.artifacts/spatial-benchmark.json`. Remaining allocations include status containers on newly spawned enemies and larger event arrays. Timing varies with host load; this is not a regression threshold.

This measures the overhead of the new kernel on the existing dense targeting/splash scenario, **not** heavy active status/field density. An additional 1,800-tick mixed-reaction stress test compares all events and periodic digests between grid and reference scans, including Shatter, six-field cap/replacement, Ignition overlap, Primed, nested splash and delayed Echo. It is correctness coverage, not a rendered performance claim.

## Historical Phase 1 measurement and method

Measured 2026-10-20 for RE1-04/05. Machine-reported runtime: .NET 8.0.0, Windows 10.0.19045, `Intel64 Family 6 Model 140 Stepping 1, GenuineIntel`. Debug build, no debugger. This does not measure the exported runtime, rendered frame time or GPU performance.

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

The table retains the historical Phase 1 figures; the raw report path now holds the current measurement above. Timing is diagnostic, never a flaky test gate. Results justify the bounded grid and retained bucket/query capacity; they do not justify pooling every object or claim the future graphics/stress budget is met. Allocations include event arrays and new entities/projectiles.

Correctness checks are independent of timing:

- 600 seeded radius queries against a separate reference expression over 5,002 enemies: exact boundaries, outside-map centers, zero/large radii, deaths after indexing, movement and empty rebuilds.
- Two 2,400-tick combats compare every event batch and periodic/final checksums between grid and scans, covering splash and a mixed roster with armor, shields, beams and all targeting priorities.
- Both shipped encounters terminate in winning and no-defense losing scenarios. The three-wave fixture retains its original 3,004-tick winning result.

The later production gate still requires a recorded reference PC, optimized/exported runtime, ten-minute rendered tests, 500 enemies/64 towers/2,000 projectiles, lifecycle/memory observation and representative effect/aura density.
