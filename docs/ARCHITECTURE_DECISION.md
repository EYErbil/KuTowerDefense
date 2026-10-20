# ADR 001 — Clean Godot/C# rebuild

Accepted by user 2026-10-19. Replaces provisional MonoGame recommendation. Course specifications do not constrain this decision.

## Comparison

| Criterion | Modernize JavaFX | C#/MonoGame | Unity/C# | Godot/C# |
|---|---|---|---|---|
| Reuse | High, with coupling to repair | Rebuild behavior/UI | Rebuild behavior/UI | Rebuild behavior/UI |
| 2D rendering | Canvas/custom effects | Direct control/batching | Strong URP tools | Native 2D/shaders/lights |
| Authoring | Repair custom editor | Build/integrate tools | Extensive integrated editor | Integrated scenes/tiles/UI/animation |
| Effort | Lower initial, high restructuring | More owned tools/UI | Larger editor/package workflow | Best fit for bounded 2D production |
| Performance | Profile after restructuring | Custom control; profile | Profile | Profile; avoid per-entity physics |
| Platforms | Desktop | Desktop and other targets | Broad desktop/mobile | Desktop; C# mobile needs evaluation |
| Asset pipeline | Custom resource loading | Content pipeline | Mature import tools | Built-in import tools |
| Maintenance | Coupling unless rebuilt | Small framework, more owned tooling | Engine/package management | Text scenes and independent rules |
| Testing | JUnit, engine coupling | Pure .NET | Pure .NET plus engine tests | Pure .NET plus headless smoke |
| Migration | M–L | L | L | L |
| Mechanics | Possible | Supported | Supported | Supported |

Choose Godot 4.7.2 .NET for presentation/authoring and pure C# for simulation. This concerns development focus, not automatic language/engine performance gains. No paid add-on is required.

Sources: [Godot releases](https://godotengine.org/download/archive/), [C# support](https://docs.godotengine.org/en/stable/tutorials/scripting/c_sharp/c_sharp_basics.html), [2D lighting](https://docs.godotengine.org/en/stable/tutorials/2d/2d_lights_and_shadows.html), [headless export](https://docs.godotengine.org/en/stable/tutorials/editor/command_line_tutorial.html), [MonoGame tooling](https://docs.monogame.net/articles/tutorials/building_2d_games/02_getting_started/), [Unity 2D](https://docs.unity3d.com/6000.0/Documentation/Manual/urp/2d-index.html). Godot's documented C# mobile limitations make mobile a separate gate.

## Toolchain and layers

Pin .NET SDK 10.0.400, Godot 4.7.2 .NET, matching templates and NuGet locks. Target engine-compatible .NET 8; development needs its runtime. Reassess framework support before production release. Downloads/outputs stay in ignored workspace directories.

Implementation verification: Debug and ExportRelease use separate `packages.<Configuration>.lock.json` files because Godot excludes editor assemblies during export. Restore is locked by default; deliberate dependency updates use `-p:UnlockPackages=true --force-evaluate` for both configurations. All projects declare the Windows runtime identifier so transitive export restores do not rewrite Debug dependency graphs. The game-local solution is required by Godot's exporter; the parent solution additionally contains tests and SimRunner. Use `dev.cmd` for supported build/export configurations. The .NET runtime bundled in the verified Windows package is 8.0.30.

- Core: stable IDs, typed state, commands, movement/combat/economy/waves; no engine or filesystem.
- Infrastructure: content validation/loading; later snapshots/profiles/analytics persistence.
- Game: Godot input/drawing/UI/audio/effects; no gameplay authority.
- SimRunner: seeded display-free runs/checksums.
- Tests: isolated simulation/content/persistence tests, complemented by engine smoke.

No general ECS, service locator, networking or DI framework. Introduce spatial optimization and pooling with measured need.

## Contracts

`Simulation.Step()` advances one 1/60-second tick while running. Commands validate before mutation. Pause stops all gameplay timers. UI delta determines tick count; 1/2/4× changes throughput, not rules. Stable entity order and explicit seeded RNG provide same-build/content reproducibility. Cross-platform floating-point bit identity is not promised.

One world-coordinate convention. Initial authored routes compile once to cumulative segment lengths; map bounds, continuity, walkability and foundations validate at load. Target greatest path progress, ID tie-break. Swept/clamped projectile travel prevents overshoot. Damage/death/reward precede leaks; death/bounty happen once. Spawn schedules use ticks. Wave completion requires empty queue and living-enemy set; victory additionally requires positive lives.

Presentation reads state views and event batches. Flashes/sound/particles cannot affect simulation. Focus loss pauses. RE1-04 now uses a bounded uniform grid with two-tile cells, rebuilt after movement; bucket storage and query buffers retain capacity. Radius checks are exact and inclusive. Target ties and splash resolution define gameplay order independently of bucket traversal. The internal reference-scan path is available to Tests/SimRunner only, with per-tick event/checksum comparisons and an alternating benchmark. Object pooling is deferred until allocation profiling justifies it.

RE1-05 extends the same kernel with simultaneous spawn groups, configurable attack/damage kinds and per-entity immutable definitions. A wave compiles to `(offset, group index, ordinal)` order when started. All due spawns resolve before movement. Shield neighbors are collected after movement; protection checks whether each source is still alive at hit time, so killing an escort removes protection immediately. Overlapping auras take the maximum percentage and exclude other shield generators. Beam hits resolve during tower updates; projectiles then resolve in stable creation order, before leaks. Shells keep their last tracked aim point when a target dies. No rendering timers drive these rules.

## Data and saves

Format-2 stable-ID JSON catalogs validate unique IDs, references, enums, positive finite values, dimensions, routes, foundations and bounded spawn groups before creating a simulation. Definitions and nested arrays are copied at the simulation boundary. Attack behavior is selected by an explicit kind, never a content ID string. The old three-wave fixture has been converted to format 2 without changing its balance. Effects/reactions/branches remain RE2-01 content work; a generalized effect framework is not claimed here. Scene authoring remains in Godot; game rules stay portable.

Future complete versioned JSON snapshots include tick, IDs, spawn queue, RNG, projectiles, cooldowns, statuses, circuits, bosses and profile modifiers, with content fingerprint and atomic replacement. Invalid saves cannot mutate the current run. No legacy importer. Save/resume is RE3-05, not a first-slice feature.

## Controlled retirement

Record baseline be87036f68d752d98d096f8f7c7f1a3ad6e082d6 and asset manifest; retire reviewed course/Java application/build files. Preserve their existing Git history. Do not switch branches, rewrite history, delete Git metadata, push or touch unrelated files. No active Java build is required.

Deliver three-wave foundation → six-wave roster → reactions/circuits → full run → polished candidate. Update progress on every consequential decision and verified milestone.
