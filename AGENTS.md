# Start here — REDESIGN: EVOLUTION

Read [docs/AGENT_HANDOFF.md](docs/AGENT_HANDOFF.md) before editing. It is the current continuation entry point; the conversation history is not required.

## Current assignment boundary

On 2026-10-21 the owner accepted the frontier/squad direction and asked this session to prepare documentation only. Implementation is being handed to a colleague and their agent. Do not infer an implementation assignment merely from reading this file. When assigned to continue implementation, start with **FR-01 Road squad kernel**, followed by **FR-02 Animated squad slice**. This handoff does not require the assigned implementer to ask again for routine implementation choices.

## Project rules

- Work on `Ege-redevelop`. Inspect Git status/branch first; preserve existing uncommitted work. Do not switch or rewrite the branch, delete Git metadata, or push/delete remote branches without the owner's explicit instruction.
- Active game: `Redesign-Evolution/`, Godot 4.7.2 .NET presentation with an independent C# simulation. Course restrictions and legacy compatibility requirements are retired. Do not restart the Java audit or engine comparison.
- Current shipped slice: twelve-wave Emberwatch Expedition. Road squads, Stoneford, profile unlocks and the frontier map are planned, not implemented. Circuits are deferred for reassessment after the squad/map work.
- Source of truth for the next tasks: FR-01–FR-07 in [docs/FRONTIER_CAMPAIGN_PROPOSAL.md](docs/FRONTIER_CAMPAIGN_PROPOSAL.md). The larger roadmap remains in [docs/REDESIGN_EVOLUTION_ROADMAP.md](docs/REDESIGN_EVOLUTION_ROADMAP.md).
- Keep gameplay authority in Core; Godot owns presentation/input/audio. Preserve deterministic tick behavior, atomic commands, pause/speed semantics and the earlier regression scenarios. Do not let animation callbacks determine combat outcomes.
- Deliver small playable slices. Update `docs/DEVELOPMENT_PROGRESS.md`, the task checklist and `docs/AGENT_HANDOFF.md` when a milestone, limitation or next step changes. Distinguish implemented, tested and proposed features.
- Use `dev.cmd` for the documented Windows build/test/run/smoke/export workflow. `.tools/` and `.artifacts/` are ignored local directories and may be absent in another checkout.
- The last recorded baseline was 76 passing tests and a verified Windows export. That is historical evidence; verify the baseline when beginning implementation and report actual new results.

See the handoff for the first patch scope, code entry points, known balance risks, verification commands and a continuation prompt.
