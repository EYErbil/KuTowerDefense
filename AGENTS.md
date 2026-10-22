# Start here — REDESIGN: EVOLUTION

Read [docs/AGENT_HANDOFF.md](docs/AGENT_HANDOFF.md) before editing. It is the current continuation entry point; the conversation history is not required.

## Current assignment boundary

The owner resumed implementation after the documentation handoff. **FR-01 Road squad kernel is complete in Core and headless scenarios; FR-02 Animated squad slice is next.** Normal Godot play does not yet expose Garrison controls or animation. When assigned to continue, read the kernel contract and add that playable integration. Routine implementation choices within this plan do not require another approval round.

## Project rules

- Work on `Ege-redevelop`. Inspect Git status/branch first; preserve existing uncommitted work. Do not switch or rewrite the branch, delete Git metadata, or push/delete remote branches without the owner's explicit instruction.
- Active game: `Redesign-Evolution/`, Godot 4.7.2 .NET presentation with an independent C# simulation. Course restrictions and legacy compatibility requirements are retired. Do not restart the Java audit or engine comparison.
- Current playable slice: twelve-wave Emberwatch Expedition. Road squad simulation exists; its UI/animation, Stoneford, profile unlocks and the frontier map remain pending. The Windows ZIP predates FR-01. Circuits are deferred for reassessment after the squad/map work.
- Source of truth for the next tasks: FR-01–FR-07 in [docs/FRONTIER_CAMPAIGN_PROPOSAL.md](docs/FRONTIER_CAMPAIGN_PROPOSAL.md). The larger roadmap remains in [docs/REDESIGN_EVOLUTION_ROADMAP.md](docs/REDESIGN_EVOLUTION_ROADMAP.md).
- Keep gameplay authority in Core; Godot owns presentation/input/audio. Preserve deterministic tick behavior, atomic commands, pause/speed semantics and the earlier regression scenarios. Do not let animation callbacks determine combat outcomes.
- Deliver small playable slices. Update `docs/DEVELOPMENT_PROGRESS.md`, the task checklist and `docs/AGENT_HANDOFF.md` when a milestone, limitation or next step changes. Distinguish implemented, tested and proposed features.
- Use `dev.cmd` for the documented Windows build/test/run/smoke/export workflow. `.tools/` and `.artifacts/` are ignored local directories and may be absent in another checkout.
- Latest verification: 96 passing tests, existing Godot UI smoke, squad/no-squad command witnesses and earlier regression scenarios. The smoke log contains a sandbox certificate-store diagnostic. Verify the baseline when beginning further implementation; do not claim the older Windows export contains squad work.

See the handoff for the first patch scope, code entry points, known balance risks, verification commands and a continuation prompt.
