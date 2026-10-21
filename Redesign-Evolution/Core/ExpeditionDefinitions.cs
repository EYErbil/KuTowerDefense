namespace Redesign.Core;

public enum RelicKind { ExtraChill, BeamFork, BoltFork, ShellEcho, LongBurn, WarChest, ReinforcedGate }
public sealed record RelicDefinition(string Id, string Name, string Description, RelicKind Kind, int Power, double Radius = 0);
public sealed record ExpeditionDefinition(int[] RewardWaves, RelicDefinition[] Relics, string ChallengeEnemyId,
    int ChallengeBaseCount, int ChallengeBonusGold, int ChallengeGrowthPerStage = 1)
{
    public ExpeditionDefinition Copy() => this with { RewardWaves = [.. RewardWaves], Relics = [.. Relics] };
}
public sealed record HealingDefinition(int Amount, double Radius, int IntervalTicks, int WindupTicks);
public sealed record BossDefinition(string SummonEnemyId, int SummonsPerPulse, int MaxSummons,
    int IntervalTicks, int WindupTicks, int GuardPercent, int EnrageHealthPercent, int EnrageSpeedPercent);

internal static class ExpeditionContent
{
    public static void Validate(GameDefinition game)
    {
        static void Require(bool ok, string message) { if (!ok) throw new ArgumentException("Invalid expedition content: " + message); }
        foreach (var enemy in game.Enemies)
        {
            Require(enemy.SlowResistancePercent is >= 0 and <= 80, $"'{enemy.Id}' slow resistance");
            Require(enemy.Healing is null || enemy.Boss is null, $"'{enemy.Id}' cannot combine healing and boss abilities");
            if (enemy.Healing is { } heal)
                Require(heal.Amount is > 0 and <= 10000 && double.IsFinite(heal.Radius) && heal.Radius > 0 &&
                    heal.IntervalTicks is >= 30 and <= 36000 && heal.WindupTicks > 0 && heal.WindupTicks < heal.IntervalTicks,
                    $"'{enemy.Id}' healing values");
            if (enemy.Boss is { } boss)
            {
                var summon = game.Enemies.FirstOrDefault(e => e.Id == boss.SummonEnemyId);
                Require(summon is not null && summon.Boss is null && summon.Healing is null && summon.Bounty == 0,
                    $"'{enemy.Id}' summons must reference a non-support enemy with zero bounty");
                Require(boss.SummonsPerPulse is >= 1 and <= 8 && boss.MaxSummons >= boss.SummonsPerPulse && boss.MaxSummons <= 64 &&
                    boss.IntervalTicks is >= 120 and <= 36000 && boss.WindupTicks >= 30 && boss.WindupTicks < boss.IntervalTicks / 2 &&
                    boss.GuardPercent is >= 0 and <= 80 && boss.EnrageHealthPercent is > 0 and < 100 && boss.EnrageSpeedPercent is >= 0 and <= 100,
                    $"'{enemy.Id}' boss values");
            }
        }
        if (game.Expedition is not { } expedition) return;
        Require(expedition.RewardWaves is { Length: > 0 and <= 8 } && expedition.Relics is { Length: >= 3 and <= 32 }, "reward waves/relic catalogs");
        if (expedition.RewardWaves is null || expedition.Relics is null) return;
        Require(expedition.RewardWaves.All(w => w > 0 && w < game.Waves.Length) &&
            expedition.RewardWaves.SequenceEqual(expedition.RewardWaves.Distinct().Order()), "reward waves must be ordered, unique and before the final wave");
        Require(expedition.Relics.Length >= expedition.RewardWaves.Length + 2, "not enough distinct relics for three offers at every reward");
        var ids = new HashSet<string>(StringComparer.Ordinal);
        foreach (var relic in expedition.Relics)
        {
            Require(relic is not null, "null relic"); if (relic is null) return;
            Require(!string.IsNullOrWhiteSpace(relic.Id) && ids.Add(relic.Id) && !string.IsNullOrWhiteSpace(relic.Name) &&
                !string.IsNullOrWhiteSpace(relic.Description) && Enum.IsDefined(relic.Kind) && relic.Power is > 0 and <= 1000 &&
                double.IsFinite(relic.Radius) && relic.Radius >= 0 && relic.Radius <= 512, $"relic '{relic.Id}' identity/values");
            if (relic.Kind is RelicKind.BeamFork or RelicKind.BoltFork)
                Require(relic.Power <= 100 && relic.Radius > 0, $"relic '{relic.Id}' fork values");
            if (relic.Kind == RelicKind.ExtraChill)
                Require(relic.Power <= 3 && (game.Combat?.Statuses.Any(s => s.Id == "chill") ?? false), $"relic '{relic.Id}' requires chill");
            if (relic.Kind == RelicKind.LongBurn)
                Require(game.Combat?.Reactions.Any(r => r.Effect == ReactionEffect.BurningGround) ?? false, $"relic '{relic.Id}' requires burning ground");
        }
        Require(game.Enemies.Any(e => e.Id == expedition.ChallengeEnemyId && e.Bounty == 0 && e.Boss is null && e.Healing is null) &&
            expedition.ChallengeBaseCount is > 0 and <= 20 && expedition.ChallengeBonusGold is > 0 and <= 1000 &&
            expedition.ChallengeGrowthPerStage is >= 0 and <= 8, "challenge enemy/count/reward");
    }
}
