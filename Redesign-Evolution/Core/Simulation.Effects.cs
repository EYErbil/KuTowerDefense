using System.Collections.ObjectModel;

namespace Redesign.Core;

public sealed partial class Simulation
{
    private Dictionary<string, StatusDefinition> statusDefinitions = null!;
    private Dictionary<string, ReactionStatistics> reactionStatistics = null!;
    private readonly List<BurningAreaState> burningAreas = [];
    private readonly List<PendingImpactState> pendingImpacts = [];
    private readonly List<EnemyState> impactCandidates = [], reactionCandidates = [], areaCandidates = [];
    public ReadOnlyCollection<BurningAreaState> BurningAreas { get; private set; } = null!;
    public ReadOnlyCollection<PendingImpactState> PendingImpacts { get; private set; } = null!;
    public ReadOnlyCollection<ReactionStatistics> Reactions { get; private set; } = null!;
    public long ReactionDamage { get; private set; }

    private void InitializeEffects()
    {
        statusDefinitions = (definition.Combat?.Statuses ?? []).ToDictionary(s => s.Id, StringComparer.Ordinal);
        var stats = (definition.Combat?.Reactions ?? []).Select(r => new ReactionStatistics(r)).ToArray();
        reactionStatistics = stats.ToDictionary(s => s.Definition.Id, StringComparer.Ordinal);
        Reactions = Array.AsReadOnly(stats);
        BurningAreas = burningAreas.AsReadOnly(); PendingImpacts = pendingImpacts.AsReadOnly();
    }

    private void Strike(EnemyState enemy, int amount, int source, TowerDefinition attack)
    {
        if (!enemy.Alive) return;
        // Consume existing setup before this hit can apply new stacks. Lethal hits still produce the reaction.
        List<ReactionStatistics>? triggered = null;
        foreach (var stats in Reactions)
        {
            var reaction = stats.Definition;
            if (reaction.Trigger != attack.DamageKind ||
                enemy.ReactionReady.GetValueOrDefault(reaction.Id) > Tick) continue;
            var status = enemy.StatusSet.Find(reaction.StatusId);
            if (status is null || status.Count < reaction.RequiredStacks) continue;
            var providers = enemy.StatusSet.Consume(status, reaction.RequiredStacks);
            enemy.ReactionReady[reaction.Id] = Tick + reaction.CooldownTicks;
            stats.Triggers++; towerHistory[source].ReactionsTriggered++;
            foreach (var provider in providers)
                if (towerHistory.TryGetValue(provider, out var setup)) setup.ReactionsPrepared++;
            (triggered ??= []).Add(stats);
        }
        if (attack.OnHit is { } application)
            enemy.StatusSet.Apply(statusDefinitions[application.StatusId], application.Stacks, source, Tick);
        DealDamage(enemy, amount, source, attack.DamageKind, attack.Attack);
        if (triggered is null) return;
        foreach (var stats in triggered)
        {
            var reaction = stats.Definition;
            Emit(EventKind.Reaction, enemy.Position, source, (int)Math.Min(reaction.Radius, int.MaxValue),
                damageKind: reaction.DamageKind, effectId: reaction.Id);
            if (reaction.Effect == ReactionEffect.Burst)
            {
                QueryEnemies(enemy.Position, reaction.Radius, reactionCandidates);
                reactionCandidates.Sort(static (a, b) => a.Id.CompareTo(b.Id));
                foreach (var victim in reactionCandidates)
                    DealDamage(victim, reaction.Damage, source, reaction.DamageKind, reaction: stats);
            }
            else
            {
                // Recycle the oldest area of this reaction at the configured cap.
                if (burningAreas.Count(a => a.Definition.Id == reaction.Id) >= reaction.MaxAreas)
                    burningAreas.RemoveAt(burningAreas.FindIndex(a => a.Definition.Id == reaction.Id));
                burningAreas.Add(new(nextId++, enemy.Position, source, reaction, Tick));
            }
        }
    }

    private bool IsPrimed(EnemyState enemy, DamageKind kind)
    {
        foreach (var stats in Reactions)
        {
            var reaction = stats.Definition;
            if (reaction.Trigger == kind && enemy.ReactionReady.GetValueOrDefault(reaction.Id) <= Tick &&
                enemy.StatusSet.Find(reaction.StatusId) is { } status && status.Count >= reaction.RequiredStacks) return true;
        }
        return false;
    }

    private void ResolvePersistentEffects()
    {
        foreach (var impact in pendingImpacts)
        {
            if (impact.ResolveAt > Tick) continue;
            QueryEnemies(impact.Position, impact.Definition.Radius, impactCandidates);
            impactCandidates.Sort(static (a, b) => a.Id.CompareTo(b.Id));
            foreach (var enemy in impactCandidates)
                DealDamage(enemy, impact.Definition.Damage, impact.SourceId, impact.Definition.DamageKind, AttackKind.Shell);
            Emit(EventKind.Blast, impact.Position, impact.SourceId, (int)Math.Min(impact.Definition.Radius, int.MaxValue),
                AttackKind.Shell, damageKind: impact.Definition.DamageKind, effectId: "echo");
        }
        pendingImpacts.RemoveAll(i => i.ResolveAt <= Tick);
        burningAreas.RemoveAll(a => a.ExpiresAt <= Tick);
        foreach (var area in burningAreas)
        {
            if (area.NextPulseTick > Tick) continue;
            area.NextPulseTick += area.Definition.IntervalTicks;
            QueryEnemies(area.Position, area.Definition.Radius, areaCandidates);
            areaCandidates.Sort(static (a, b) => a.Id.CompareTo(b.Id));
            foreach (var enemy in areaCandidates)
            {
                // Per-enemy cadence also prevents staggered, overlapping fields from multiplying damage.
                if (enemy.AreaDamageReady.GetValueOrDefault(area.Definition.Id) > Tick) continue;
                enemy.AreaDamageReady[area.Definition.Id] = Tick + area.Definition.IntervalTicks;
                DealDamage(enemy, area.Definition.Damage, area.SourceId, area.Definition.DamageKind,
                    reaction: reactionStatistics[area.Definition.Id]);
            }
        }
        // Reaction damage and echoes use DealDamage directly: no new status, reaction or delayed effect.
    }

    private static void WriteEnemyEffects(BinaryWriter writer, EnemyState enemy)
    {
        writer.Write(enemy.Statuses.Count);
        foreach (var status in enemy.Statuses.OrderBy(s => s.Definition.Id, StringComparer.Ordinal))
        {
            writer.Write(status.Definition.Id); writer.Write(status.Count);
            foreach (var stack in status.Stacks) { writer.Write(stack.SourceId); writer.Write(stack.ExpiresAt); }
        }
        foreach (var cooldowns in new[] { enemy.ReactionReady, enemy.AreaDamageReady })
        {
            writer.Write(cooldowns.Count);
            foreach (var entry in cooldowns.OrderBy(e => e.Key, StringComparer.Ordinal))
            { writer.Write(entry.Key); writer.Write(entry.Value); }
        }
    }
    private void WriteEffects(BinaryWriter writer)
    {
        writer.Write(ReactionDamage); writer.Write(Reactions.Count);
        foreach (var stats in Reactions)
        { writer.Write(stats.Definition.Id); writer.Write(stats.Triggers); writer.Write(stats.Damage); }
        writer.Write(burningAreas.Count);
        foreach (var area in burningAreas)
        {
            writer.Write(area.Id); writer.Write(area.Definition.Id); writer.Write(area.SourceId);
            writer.Write(area.Position.X); writer.Write(area.Position.Y);
            writer.Write(area.ExpiresAt); writer.Write(area.NextPulseTick);
        }
        writer.Write(pendingImpacts.Count);
        foreach (var impact in pendingImpacts)
        {
            writer.Write(impact.Id); writer.Write(impact.SourceId); writer.Write(impact.Position.X); writer.Write(impact.Position.Y);
            writer.Write(impact.CreatedAt); writer.Write(impact.ResolveAt);
            writer.Write(impact.Definition.Damage); writer.Write(impact.Definition.Radius); writer.Write((int)impact.Definition.DamageKind);
        }
    }
}
