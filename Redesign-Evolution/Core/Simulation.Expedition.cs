using System.Collections.ObjectModel;

namespace Redesign.Core;

public sealed partial class Simulation
{
    private readonly List<RelicDefinition> rewardOffers = [], relics = [];
    private readonly List<WaveReport> waveReports = [];
    private int waveKillsAtStart, waveLeaksAtStart;
    private long waveIncomeAtStart;
    public ReadOnlyCollection<RelicDefinition> RewardOffers { get; private set; } = null!;
    public ReadOnlyCollection<RelicDefinition> Relics { get; private set; } = null!;
    public ReadOnlyCollection<WaveReport> WaveReports { get; private set; } = null!;
    public int MaxLives { get; private set; }
    public bool ChallengeEnabled { get; private set; }
    public int ChallengeCount => ChallengeCountAt(Phase == RunPhase.Planning ? WaveNumber + 1 : WaveNumber);
    private int ChallengeCountAt(int wave) => definition.Expedition is { } d ? d.ChallengeBaseCount + wave / 3 * d.ChallengeGrowthPerStage : 0;
    public int ChallengeReward => definition.Expedition is { } d ? d.ChallengeBonusGold + 2 * (Phase == RunPhase.Planning ? WaveNumber + 1 : WaveNumber) : 0;
    public TowerDefinition? PreviewUpgrade(string branchId) => branches.TryGetValue(branchId, out var branch) ? ApplyRelics(branch.Upgrade) : null;

    private void InitializeExpedition()
    {
        RewardOffers = rewardOffers.AsReadOnly(); Relics = relics.AsReadOnly(); WaveReports = waveReports.AsReadOnly();
        MaxLives = definition.StartingLives;
    }

    private void CompleteWaveRewards()
    {
        var flawless = Leaks == waveLeaksAtStart;
        var clearGold = definition.Waves[WaveNumber - 1].ClearGold + (ChallengeEnabled && flawless ? ChallengeReward : 0);
        Gold += clearGold; GoldEarned += clearGold;
        waveReports.Add(new(WaveNumber, Kills - waveKillsAtStart, Leaks - waveLeaksAtStart, Tick - waveStartTick + 1,
            GoldEarned - waveIncomeAtStart, clearGold, ChallengeEnabled, ChallengeEnabled && flawless));
        ChallengeEnabled = false;
        if (definition.Expedition is not { } expedition || !expedition.RewardWaves.Contains(WaveNumber)) return;
        var pool = expedition.Relics.Where(r => !relics.Any(owned => owned.Id == r.Id)).ToList();
        // Specified SplitMix64 shuffle. Offers depend on seed, earned checkpoint and prior choices, never rendering.
        var state = Seed ^ ((ulong)WaveNumber * 0x9E3779B97F4A7C15UL);
        ulong Next()
        {
            state = unchecked(state + 0x9E3779B97F4A7C15UL);
            var z = state; z = (z ^ (z >> 30)) * 0xBF58476D1CE4E5B9UL;
            z = (z ^ (z >> 27)) * 0x94D049BB133111EBUL; return z ^ (z >> 31);
        }
        for (var i = pool.Count - 1; i > 0; i--) { var j = (int)(Next() % (ulong)(i + 1)); (pool[i], pool[j]) = (pool[j], pool[i]); }
        // Favor one payoff for the current build, plus an economy/recovery alternative when available.
        var relevant = pool.FirstOrDefault(IsRelevantRelic);
        if (relevant is not null) { rewardOffers.Add(relevant); pool.Remove(relevant); }
        var recovery = pool.FirstOrDefault(r => r.Kind is RelicKind.ReinforcedGate or RelicKind.WarChest);
        if (recovery is not null) { rewardOffers.Add(recovery); pool.Remove(recovery); }
        rewardOffers.AddRange(pool.Take(3 - rewardOffers.Count));
    }

    private bool IsRelevantRelic(RelicDefinition relic) => relic.Kind switch
    {
        RelicKind.ExtraChill => towers.Any(t => t.Definition.OnHit?.StatusId == "chill"),
        RelicKind.LongBurn => towers.Any(t => t.Definition.DamageKind == DamageKind.Fire),
        RelicKind.BoltFork => towers.Any(t => t.Definition.Attack == AttackKind.Bolt),
        RelicKind.BeamFork => towers.Any(t => t.Definition.Attack == AttackKind.Beam),
        RelicKind.ShellEcho => towers.Any(t => t.Definition.Attack == AttackKind.Shell),
        _ => false
    };

    private CommandResult ClaimRelic(string id)
    {
        if (Phase != RunPhase.Planning) return new(false, "Relics are chosen between waves.");
        var chosen = rewardOffers.FirstOrDefault(r => r.Id == id);
        if (chosen is null) return new(false, "Choose one of the earned relic offers.");
        relics.Add(chosen); rewardOffers.Clear();
        if (chosen.Kind == RelicKind.WarChest) { Gold += chosen.Power; GoldEarned += chosen.Power; }
        if (chosen.Kind == RelicKind.ReinforcedGate)
        { MaxLives = (int)Math.Min(int.MaxValue, (long)MaxLives + Math.Max(1, chosen.Power / 2)); Lives = (int)Math.Min(MaxLives, (long)Lives + chosen.Power); }
        foreach (var tower in towers)
            tower.Definition = ApplyRelics(tower.BranchId is { } branch ? branches[branch].Upgrade : towerDefinitions[tower.FamilyId]);
        foreach (var stats in Reactions)
        {
            var original = definition.Combat!.Reactions.First(r => r.Id == stats.Definition.Id);
            stats.Definition = original;
            foreach (var relic in relics)
                if (relic.Kind == RelicKind.LongBurn && original.Effect == ReactionEffect.BurningGround)
                    stats.Definition = stats.Definition with { Radius = stats.Definition.Radius + relic.Radius, DurationTicks = stats.Definition.DurationTicks + relic.Power };
        }
        Emit(EventKind.RelicChosen, Route.At(Route.Length), effectId: chosen.Id);
        return new(true, $"{chosen.Name} claimed. {chosen.Description}");
    }

    private TowerDefinition ApplyRelics(TowerDefinition spec)
    {
        foreach (var relic in relics)
        {
            if (relic.Kind == RelicKind.ExtraChill && spec.OnHit is { StatusId: "chill" } chill)
                spec = spec with { OnHit = chill with { Stacks = Math.Min(statusDefinitions["chill"].MaxStacks, chill.Stacks + relic.Power) } };
            if (relic.Kind == RelicKind.BeamFork && spec.Attack == AttackKind.Beam || relic.Kind == RelicKind.BoltFork && spec.Attack == AttackKind.Bolt)
                spec = spec with { ForkRadius = Math.Max(spec.ForkRadius, relic.Radius), ForkDamagePercent = relic.Power };
            if (relic.Kind == RelicKind.ShellEcho && spec.Attack == AttackKind.Shell)
                spec = spec with { Echo = new(spec.Echo?.DelayTicks ?? 30,
                    (int)Math.Min(int.MaxValue, (long)(spec.Echo?.Damage ?? 0) + relic.Power), Math.Max(spec.Echo?.Radius ?? 0, relic.Radius)) };
        }
        return spec;
    }

    private readonly List<EnemyState> forkCandidates = [];
    private void ForkAttack(int primaryId, Point center, int source, TowerDefinition attack)
    {
        if (attack.ForkRadius <= 0) return;
        QueryEnemies(center, attack.ForkRadius, forkCandidates);
        var target = forkCandidates.Where(e => e.Id != primaryId).MinBy(e => ((e.Position - center).LengthSquared, e.Id));
        if (target is null) return;
        Emit(EventKind.Fired, center, source, attack: AttackKind.Beam, destination: target.Position, damageKind: attack.DamageKind, effectId: "fork");
        Strike(target, Math.Max(1, (int)((long)attack.Damage * attack.ForkDamagePercent / 100)), source, attack);
        // A fork calls Strike, never ForkAttack: at most one secondary target per ordinary attack.
    }

    private void WriteExpedition(BinaryWriter writer)
    {
        writer.Write(MaxLives); writer.Write(ChallengeEnabled); writer.Write(waveKillsAtStart); writer.Write(waveLeaksAtStart); writer.Write(waveIncomeAtStart);
        writer.Write(relics.Count); foreach (var r in relics) writer.Write(r.Id);
        writer.Write(rewardOffers.Count); foreach (var r in rewardOffers) writer.Write(r.Id);
        writer.Write(waveReports.Count);
        foreach (var w in waveReports)
        { writer.Write(w.Wave); writer.Write(w.Kills); writer.Write(w.Leaks); writer.Write(w.DurationTicks); writer.Write(w.Income); writer.Write(w.ClearGold); writer.Write(w.Challenged); writer.Write(w.ChallengeWon); }
    }
}
