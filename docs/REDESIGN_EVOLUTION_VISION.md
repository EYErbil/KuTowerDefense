# REDESIGN: EVOLUTION — Vision

Approved direction, 2026-10-19. This is an independent game. Course technology rules, grading criteria, legacy APIs, original content and file formats impose no constraints.

## Identity and audience

Engineer an arcane frontier defense. Towers do more than attack: their elemental reactions produce power that travels through player-built **Resonance Circuits**. A successful defense is a thoughtfully arranged system, with distinct ways to win.

Target PC strategy players seeking understandable decisions, satisfying combat and a new build each run. Windows, keyboard and mouse, single-player/offline first. Complete runs target 25–40 minutes including planning. Mobile is a later feasibility experiment; multiplayer is outside the committed roadmap.

## Pillars

1. Placement serves path coverage, reactions and tower connections simultaneously.
2. Specialization changes attacks, timing, target incentives and visual identity, not merely damage.
3. Forecast threats and telegraph disruption. No hidden hard counters to successful play.
4. Distinct silhouettes and controlled effects preserve readable spectacle.
5. Relics and optional contracts create different plans; statistics explain outcomes.
6. One simulation clock, reproducible seeds, validated content and exact-once outcomes make experimentation reliable.

## Loop and scope

Choose map/doctrine → forecast threats → build/connect → defend → choose relic/contract → specialize → defeat act encounter → inspect results and unlock progression.

The complete MVP targets three tower families with two branches each, two reactions, distinct enemy behaviors, one two-phase boss, two authored maps with seeded variation and roughly twelve relics. Eighteen waves in three acts is a pacing hypothesis, not a reason to pad a successful shorter structure. The current expedition has twelve waves in four stages, three earned relic choices from seven definitions, optional Overruns, Menders, Captains, Raiders and a rallying Bellwarden alongside the earlier roster and specializations. It remains one map. Combat witnesses last 5.5–7.5 minutes plus planning; full-length pacing is not achieved.

Player feedback on repetition changed the implementation order on 2026-10-20/21: earned choices, a tighter economy and distinct encounters precede Circuits. A player should reach a build-changing reward early, face a reason to reconsider target priorities, and choose whether extra risk is affordable. More enemy health alone does not satisfy this gate. The expedition is implemented; human observations must still establish whether its decisions are rewarding. Circuits remain the proposed signature, not a shipped feature.

## Signature

Reactions generate limited pulses to connected towers. Three stored charges empower a shot with a family-specific behavior. Generator placement competes with ordinary path coverage. Link costs, limited ports, finite capacity and non-recursive generation bound power. Initial directed acyclic links transmit one hop; no electrical solver is needed. Exact rules live in GAME_DESIGN_DOCUMENT.md.

## Presentation

Arcane pixel frontier: weathered stone, conifers, copper mechanisms, warm lanterns, cool rune light and a threatened citadel. Use consistent scale, palette, outlines and animation cadence. Old assets are optional and require individual provenance review.

The first slice establishes composition and feedback through original code-drawn geometric artwork. This is prototype presentation, not final production art. Later sprites follow the style guide rather than dictating it. Use wind-up, trails, brief impact flashes, localized particles and distinct audio cues; avoid full-screen flashes and default shake. Essential cues use text/shape as well as color.

## Five differentiating proposals

Scores are design hypotheses, not playtest results. O=originality, D=depth, F=expected fun, S=synergy, T=technical feasibility; 5 is highest.

| Idea | O/D/F/S/T | Cost | Benefit | Risk | Decision |
|---|---|---|---|---|---|
| Resonance Circuits | 4/5/4/5/4 | L | Reactions become a placement/resource network | Recursive escalation | Signature; committed |
| Battlefield Memory | 4/4/4/5/3 | L | Combat leaves consumable terrain imprints for later waves | Snowballing, visual noise | Experiment |
| Countermarch Contracts | 3/4/4/4/5 | M | Accept a forecast formation for a known reward | Always-correct reward choice | Overrun subset implemented; broaden only after playtesting |
| Evacuation Couriers | 4/4/4/3/3 | L | Protect optional friendly reverse-path convoys | Attention overload, unfair losses | Experiment |
| Echo Blueprints | 4/5/4/5/4 | M | Retire a tower between acts for a limited support imprint | Permanent stacking exploits | Experiment |

Each optional prototype gets at most three focused engineering days before a keep/discard review. It must create an observable decision, be understandable without repeated explanation and preserve readability. No experiment is a release requirement.

## Quality gates

An enjoyable foundation precedes progression. The signature slice needs at least two useful reaction/circuit layouts before expanding the interaction catalog. The complete loop needs three strategy families before more content. External playtests require reliable saves, readable controls, measured performance and recorded provenance.
