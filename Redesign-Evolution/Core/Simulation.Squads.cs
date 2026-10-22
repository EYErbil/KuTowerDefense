namespace Redesign.Core;

public sealed partial class Simulation
{
    private const double RoadTolerance = 1e-6;
    private readonly Dictionary<int, GuardState> guardById = [];
    public GarrisonState? Garrison { get; private set; }

    private bool FoundationOccupied(int foundation) =>
        Garrison?.Foundation == foundation || towers.Any(t => t.Foundation == foundation);

    private CommandResult BuildGarrison(int foundation)
    {
        if (definition.Garrison is not { } spec) return new(false, "Squads are unavailable in this mission.");
        if (Garrison is not null) return new(false, "Only one Garrison can be built per battlefield.");
        if (foundation < 0 || foundation >= definition.Map.Foundations.Length) return new(false, "Choose a marked foundation.");
        if (FoundationOccupied(foundation)) return new(false, "This foundation is occupied.");
        var position = definition.Map.Center(definition.Map.Foundations[foundation]);
        var entry = Route.NearestProgress(position);
        var access = (Route.At(entry) - position).Length;
        if (access > spec.CommandRadius) return new(false, "The road is beyond this Garrison's command radius.");
        if (Gold < spec.Cost) return new(false, "Not enough gold.");

        var id = nextId++;
        var guards = new GuardState[GarrisonState.SquadSize];
        for (var i = 0; i < guards.Length; i++)
        {
            guards[i] = new(nextId++, spec.Guard, position, entry, access);
            guardById.Add(guards[i].Id, guards[i]);
        }
        Garrison = new(id, foundation, position, entry, access, spec, guards);
        Gold -= spec.Cost; GoldSpent += spec.Cost;
        Emit(EventKind.GarrisonBuilt, position, id, spec.Cost);
        return new(true, "Three Shieldguards ready. Rally on the road; fallen guards recover between waves.", id);
    }

    public bool IsSquadRallyReachable(Point position) => Garrison is { } garrison &&
        Route.TryGetProgress(position, out var progress) &&
        garrison.AccessDistance + Math.Abs(progress - garrison.EntryProgress) <= garrison.Definition.CommandRadius + RoadTolerance;

    private CommandResult RallyGuards(Point position)
    {
        if (Garrison is not { } garrison) return new(false, "Build a Garrison first.");
        if (!IsSquadRallyReachable(position)) return new(false, "Choose a road point within the squad's travel radius.");
        Route.TryGetProgress(position, out var progress);
        if (!garrison.Retreating && Math.Abs(progress - garrison.RallyProgress) <= RoadTolerance)
            return new(false, "The squad is already rallying there.");
        foreach (var guard in garrison.Guards) ReleaseGuard(guard);
        garrison.RallyProgress = progress; garrison.Retreating = false;
        Emit(EventKind.SquadRallied, position, garrison.Id);
        return new(true, "Rally ordered. Guards release enemies and travel along the road.", garrison.Id);
    }

    private CommandResult RetreatGuards()
    {
        if (Garrison is not { } garrison) return new(false, "Build a Garrison first.");
        if (garrison.Retreating) return new(false, "The squad is already retreating.");
        foreach (var guard in garrison.Guards) ReleaseGuard(guard);
        garrison.Retreating = true;
        Emit(EventKind.SquadRetreated, garrison.Position, garrison.Id);
        return new(true, "Retreat ordered. No healing or replacements until planning.", garrison.Id);
    }

    private static void CancelGuardAttack(GuardState guard)
    {
        guard.AttackTargetId = 0; guard.AttackAtTick = 0;
        // Orders and interrupted attacks never reset the cooldown.
    }

    private static void CancelEnemyMelee(EnemyState enemy)
    {
        enemy.MeleeTargetId = 0; enemy.MeleeAtTick = 0;
    }

    private void ReleaseGuard(GuardState guard)
    {
        if (enemyById.TryGetValue(guard.EngagedEnemyId, out var enemy)) ReleaseEnemyEngagement(enemy);
        guard.EngagedEnemyId = 0;
        CancelGuardAttack(guard);
    }

    private void ReleaseEnemyEngagement(EnemyState enemy)
    {
        if (enemy.EngagedGuardId == 0) return;
        if (guardById.TryGetValue(enemy.EngagedGuardId, out var guard))
        {
            guard.EngagedEnemyId = 0; CancelGuardAttack(guard);
            Emit(EventKind.Disengaged, guard.Position, guard.Id, destination: enemy.Position);
        }
        enemy.EngagedGuardId = 0; CancelEnemyMelee(enemy);
    }

    private bool ReadyToEngage(GuardState guard) => Garrison is { Retreating: false } garrison && guard.Alive &&
        guard.OnRoad && guard.EngagedEnemyId == 0 && Math.Abs(guard.RoadProgress - garrison.RallyProgress) <= RoadTolerance;

    private void MoveGuards()
    {
        if (Garrison is not { } garrison) return;
        var entrance = Route.At(garrison.EntryProgress);
        foreach (var guard in garrison.Guards)
        {
            guard.PreviousPosition = guard.Position;
            if (!guard.Alive) continue;
            if (guard.EngagedEnemyId != 0 && (!enemyById.TryGetValue(guard.EngagedEnemyId, out var opponent) || !opponent.Alive))
                ReleaseGuard(guard);
            if (guard.EngagedEnemyId != 0) continue;
            var travel = guard.Definition.Speed / TicksPerSecond;
            if (garrison.Retreating)
            {
                if (guard.OnRoad)
                {
                    var distance = Math.Abs(guard.RoadProgress - garrison.EntryProgress);
                    var step = Math.Min(travel, distance);
                    guard.RoadProgress += Math.Sign(garrison.EntryProgress - guard.RoadProgress) * step;
                    travel -= step;
                    if (distance <= step) { guard.RoadProgress = garrison.EntryProgress; guard.OnRoad = false; }
                }
                if (!guard.OnRoad) guard.AccessRemaining = Math.Min(garrison.AccessDistance, guard.AccessRemaining + travel);
            }
            else
            {
                if (!guard.OnRoad)
                {
                    var step = Math.Min(travel, guard.AccessRemaining);
                    guard.AccessRemaining -= step; travel -= step;
                    if (guard.AccessRemaining <= 0) { guard.OnRoad = true; guard.RoadProgress = garrison.EntryProgress; }
                }
                if (guard.OnRoad)
                {
                    var step = Math.Min(travel, Math.Abs(garrison.RallyProgress - guard.RoadProgress));
                    guard.RoadProgress += Math.Sign(garrison.RallyProgress - guard.RoadProgress) * step;
                }
            }
            guard.Position = guard.OnRoad ? Route.At(guard.RoadProgress) :
                entrance + (garrison.Position - entrance) * (guard.AccessRemaining / garrison.AccessDistance);
        }
    }

    // Resolve swept road contact before committing movement so even fast enemies cannot tunnel through a free guard.
    private double MoveAgainstGuards(EnemyState enemy, double nextProgress)
    {
        if (!enemy.Alive || Garrison is not { Retreating: false } garrison || enemy.Definition.Melee is not { Unstoppable: false }) return nextProgress;
        if (enemy.EngagedGuardId != 0) return enemy.Progress;
        GuardState? selected = null;
        var contact = nextProgress;
        foreach (var guard in garrison.Guards)
        {
            if (!ReadyToEngage(guard)) continue;
            var reach = guard.Definition.Reach;
            if (enemy.Progress > guard.RoadProgress + reach || nextProgress < guard.RoadProgress - reach) continue;
            var encounter = Math.Max(enemy.Progress, guard.RoadProgress - reach);
            if (selected is null || encounter < contact || encounter == contact && guard.Id < selected.Id)
            { selected = guard; contact = encounter; }
        }
        if (selected is null) return nextProgress;
        enemy.EngagedGuardId = selected.Id; selected.EngagedEnemyId = enemy.Id;
        Emit(EventKind.Engaged, selected.Position, selected.Id, destination: Route.At(contact));
        return contact;
    }

    private static bool InMeleeReach(Point a, Point b, double radius) => (a - b).LengthSquared <= radius * radius + RoadTolerance;

    private void ResolveSquadCombat()
    {
        if (Garrison is not { } garrison) return;
        // Tower, projectile and persistent-effect kills already resolved. Their victims cannot retaliate.
        foreach (var guard in garrison.Guards)
        {
            if (!guard.Alive) continue;
            if (guard.EngagedEnemyId != 0) guard.EngagedTicks++;
            if (guard.AttackAtTick > 0 && Tick >= guard.AttackAtTick)
            {
                enemyById.TryGetValue(guard.AttackTargetId, out var victim);
                CancelGuardAttack(guard);
                if (victim is { Alive: true } && !garrison.Retreating &&
                    InMeleeReach(guard.Position, victim.Position, guard.Definition.Reach))
                {
                    var healthBefore = victim.Health;
                    DealDamage(victim, guard.Definition.Damage, guard.Id, DamageKind.Impact);
                    Emit(EventKind.GuardStruck, guard.Position, guard.Id, healthBefore - victim.Health, destination: victim.Position);
                }
            }
            if (guard.AttackAtTick > 0 || Tick < guard.NextAttackTick || garrison.Retreating || !guard.OnRoad ||
                Math.Abs(guard.RoadProgress - garrison.RallyProgress) > RoadTolerance) continue;
            enemyById.TryGetValue(guard.EngagedEnemyId, out var target);
            if (target is null)
            {
                // Unstoppable enemies can be struck as they pass, but never held.
                foreach (var enemy in enemies)
                {
                    if (!enemy.Alive || enemy.Definition.Melee is not { Unstoppable: true } ||
                        !InMeleeReach(guard.Position, enemy.Position, guard.Definition.Reach)) continue;
                    if (target is null || Preferred(enemy, target, TargetPriority.First, DamageKind.Impact)) target = enemy;
                }
            }
            if (target is not { Alive: true }) continue;
            guard.AttackTargetId = target.Id; guard.AttackAtTick = Tick + guard.Definition.WindupTicks;
            guard.NextAttackTick = Tick + guard.Definition.CooldownTicks;
            Emit(EventKind.GuardAttackStarted, guard.Position, guard.Id, guard.Definition.Damage, destination: target.Position);
        }
        foreach (var enemy in enemies)
        {
            if (!enemy.Alive || enemy.Definition.Melee is not { } melee) continue;
            if (enemy.MeleeAtTick > 0 && Tick >= enemy.MeleeAtTick)
            {
                guardById.TryGetValue(enemy.MeleeTargetId, out var victim);
                CancelEnemyMelee(enemy);
                if (melee.Cleave)
                {
                    // Warning follows the advancing boss; retreat out of its radius before contact.
                    foreach (var guard in garrison.Guards)
                        if (guard.Alive && InMeleeReach(guard.Position, enemy.Position, melee.Reach)) HurtGuard(guard, enemy, melee.Damage);
                }
                else if (victim is { Alive: true } && InMeleeReach(victim.Position, enemy.Position, melee.Reach))
                    HurtGuard(victim, enemy, melee.Damage);
            }
            if (enemy.MeleeAtTick > 0 || Tick < enemy.NextMeleeTick) continue;
            guardById.TryGetValue(enemy.EngagedGuardId, out var target);
            if (melee.Unstoppable)
            {
                foreach (var guard in garrison.Guards)
                    if (guard.Alive && InMeleeReach(guard.Position, enemy.Position, melee.Reach)) { target = guard; break; }
            }
            if (target is not { Alive: true }) continue;
            enemy.MeleeTargetId = target.Id; enemy.MeleeAtTick = Tick + melee.WindupTicks;
            enemy.NextMeleeTick = Tick + melee.CooldownTicks;
            Emit(EventKind.EnemyMeleeStarted, enemy.Position, enemy.Id, melee.Damage, destination: target.Position, effectId: melee.Cleave ? "cleave" : null);
        }
    }

    private void HurtGuard(GuardState guard, EnemyState attacker, int amount)
    {
        var damage = Math.Min(guard.Health, amount);
        guard.Health -= damage;
        Emit(EventKind.GuardHit, guard.Position, guard.Id, damage, destination: attacker.Position);
        if (guard.Alive) return;
        ReleaseGuard(guard); Garrison!.Casualties++;
        Emit(EventKind.GuardFallen, guard.Position, guard.Id);
    }

    private void FinishSquadWave(bool recover)
    {
        if (Garrison is not { } garrison) return;
        foreach (var guard in garrison.Guards)
        {
            ReleaseGuard(guard);
            if (!recover) continue;
            guard.Health = guard.Definition.Health;
            guard.Position = guard.PreviousPosition = garrison.Position;
            guard.OnRoad = false; guard.RoadProgress = garrison.EntryProgress; guard.AccessRemaining = garrison.AccessDistance;
            guard.NextAttackTick = 0;
        }
        foreach (var enemy in enemies) CancelEnemyMelee(enemy);
        if (recover) Emit(EventKind.SquadRecovered, garrison.Position, garrison.Id, GarrisonState.SquadSize);
    }

    private void WriteSquad(BinaryWriter writer)
    {
        // Old fixtures without squad content retain their exact digest representation.
        if (definition.Garrison is null) return;
        writer.Write(Garrison is not null);
        if (Garrison is not { } garrison) return;
        writer.Write(garrison.Id); writer.Write(garrison.Definition.Id); writer.Write(garrison.Foundation);
        writer.Write(garrison.EntryProgress); writer.Write(garrison.AccessDistance); writer.Write(garrison.RallyProgress); writer.Write(garrison.Retreating);
        writer.Write(garrison.DamageDealt); writer.Write(garrison.Kills); writer.Write(garrison.Casualties);
        foreach (var guard in garrison.Guards)
        {
            writer.Write(guard.Id); writer.Write(guard.Health); writer.Write(guard.OnRoad);
            writer.Write(guard.RoadProgress); writer.Write(guard.AccessRemaining); writer.Write(guard.Position.X); writer.Write(guard.Position.Y);
            writer.Write(guard.EngagedEnemyId); writer.Write(guard.AttackTargetId); writer.Write(guard.AttackAtTick); writer.Write(guard.NextAttackTick);
            writer.Write(guard.DamageDealt); writer.Write(guard.Kills); writer.Write(guard.EngagedTicks);
        }
        foreach (var enemy in enemies)
        {
            writer.Write(enemy.Id); writer.Write(enemy.EngagedGuardId); writer.Write(enemy.MeleeTargetId);
            writer.Write(enemy.MeleeAtTick); writer.Write(enemy.NextMeleeTick);
        }
    }
}
