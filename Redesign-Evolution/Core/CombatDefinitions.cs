namespace Redesign.Core;

public enum ReactionEffect { Burst, BurningGround }
public sealed record StatusApplication(string StatusId, int Stacks = 1);
public sealed record StatusDefinition(string Id, string Name, int MaxStacks, int DurationTicks,
    int SlowPercentPerStack = 0, int ArmorReductionPerStack = 0);
public sealed record EchoDefinition(int DelayTicks, int Damage, double Radius, DamageKind DamageKind = DamageKind.Impact);
public sealed record ReactionDefinition(string Id, string Name, string StatusId, int RequiredStacks,
    DamageKind Trigger, ReactionEffect Effect, int Damage, double Radius, DamageKind DamageKind,
    int CooldownTicks, int DurationTicks = 0, int IntervalTicks = 0, int MaxAreas = 0);
// Upgrade.Id is the branch ID. Upgrade.Cost retains the base build price; Cost is the additional investment.
public sealed record BranchDefinition(string TowerId, int Cost, TowerDefinition Upgrade);
public sealed record CombatDefinition(StatusDefinition[] Statuses, ReactionDefinition[] Reactions, BranchDefinition[] Branches)
{
    public CombatDefinition Copy() => new([.. Statuses], [.. Reactions], [.. Branches]);
}

internal static class CombatContent
{
    public static void Validate(GameDefinition game)
    {
        static void Require(bool valid, string message)
        { if (!valid) throw new ArgumentException("Invalid combat content: " + message); }
        var combat = game.Combat ?? new([], [], []);
        Require(combat.Statuses is { Length: <= 32 } && combat.Reactions is { Length: <= 16 } &&
            combat.Branches is { Length: <= 64 }, "statuses/reactions/branches arrays and limits");
        if (combat.Statuses is null || combat.Reactions is null || combat.Branches is null) return;
        var statuses = new Dictionary<string, StatusDefinition>(StringComparer.Ordinal);
        foreach (var status in combat.Statuses)
        {
            Require(status is not null, "null status");
            if (status is null) return;
            Require(!string.IsNullOrWhiteSpace(status.Id) && !string.IsNullOrWhiteSpace(status.Name), "status identity");
            Require(statuses.TryAdd(status.Id, status), $"duplicate status '{status.Id}'");
            Require(status.MaxStacks is >= 1 and <= 8 && status.DurationTicks is >= 1 and <= 36000 &&
                status.SlowPercentPerStack >= 0 && (long)status.SlowPercentPerStack * status.MaxStacks <= 80 &&
                status.ArmorReductionPerStack is >= 0 and <= 10000, $"status '{status.Id}' duration/stacks/modifiers");
        }
        var reactions = new HashSet<string>(StringComparer.Ordinal);
        var triggers = new HashSet<(string, DamageKind)>();
        foreach (var reaction in combat.Reactions)
        {
            Require(reaction is not null, "null reaction");
            if (reaction is null) return;
            Require(!string.IsNullOrWhiteSpace(reaction.Id) && !string.IsNullOrWhiteSpace(reaction.Name) &&
                reactions.Add(reaction.Id), $"reaction '{reaction.Id}' identity/duplicate");
            Require(reaction.StatusId is not null && statuses.ContainsKey(reaction.StatusId),
                $"reaction '{reaction.Id}' unknown status '{reaction.StatusId}'");
            Require(reaction.RequiredStacks > 0 && reaction.RequiredStacks <= statuses[reaction.StatusId!].MaxStacks &&
                reaction.Damage > 0 && double.IsFinite(reaction.Radius) && reaction.Radius > 0 &&
                reaction.CooldownTicks is >= 1 and <= 36000 && Enum.IsDefined(reaction.Trigger) &&
                Enum.IsDefined(reaction.Effect) && Enum.IsDefined(reaction.DamageKind), $"reaction '{reaction.Id}' values");
            Require(triggers.Add((reaction.StatusId!, reaction.Trigger)), $"ambiguous reaction for '{reaction.StatusId}' + {reaction.Trigger}");
            Require(reaction.Effect == ReactionEffect.BurningGround
                ? reaction.DurationTicks is > 0 and <= 36000 && reaction.IntervalTicks > 0 &&
                  reaction.IntervalTicks <= reaction.DurationTicks && reaction.MaxAreas is >= 1 and <= 16
                : reaction.DurationTicks == 0 && reaction.IntervalTicks == 0 && reaction.MaxAreas == 0,
                $"reaction '{reaction.Id}' area timing/cap");
        }
        void ValidateAttack(TowerDefinition tower)
        {
            Require(double.IsFinite(tower.ForkRadius) && tower.ForkRadius >= 0 &&
                tower.ForkDamagePercent is >= 1 and <= 100 && (tower.ForkRadius == 0 || tower.Attack != AttackKind.Shell), $"tower '{tower.Id}' fork");
            if (tower.OnHit is { } onHit)
                Require(onHit.StatusId is not null && statuses.TryGetValue(onHit.StatusId, out var status) &&
                    onHit.Stacks > 0 && onHit.Stacks <= status.MaxStacks, $"tower '{tower.Id}' onHit status/stacks");
            if (tower.Echo is { } echo)
                Require(tower.Attack == AttackKind.Shell && echo.DelayTicks is >= 1 and <= 3600 &&
                    echo.Damage > 0 && double.IsFinite(echo.Radius) && echo.Radius > 0 &&
                    Enum.IsDefined(echo.DamageKind), $"tower '{tower.Id}' echo");
        }
        foreach (var tower in game.Towers) ValidateAttack(tower);
        var ids = game.Towers.Select(t => t.Id).ToHashSet(StringComparer.Ordinal);
        foreach (var branch in combat.Branches)
        {
            Require(branch is not null && branch.Upgrade is not null, "null branch/upgrade");
            if (branch is null || branch.Upgrade is null) return;
            var parent = game.Towers.FirstOrDefault(t => t.Id == branch.TowerId);
            Require(parent is not null, $"branch '{branch.Upgrade.Id}' unknown family '{branch.TowerId}'");
            var upgrade = branch.Upgrade;
            Require(!string.IsNullOrWhiteSpace(upgrade.Id) && ids.Add(upgrade.Id) &&
                !string.IsNullOrWhiteSpace(upgrade.Name) && !string.IsNullOrWhiteSpace(upgrade.Description), $"branch '{upgrade.Id}' identity/duplicate");
            Require(branch.Cost > 0 && upgrade.Cost == parent!.Cost &&
                upgrade.Damage > 0 && upgrade.CooldownTicks > 0 &&
                double.IsFinite(upgrade.Range) && upgrade.Range > 0 &&
                double.IsFinite(upgrade.ProjectileSpeed) && upgrade.ProjectileSpeed > 0 &&
                double.IsFinite(upgrade.SplashRadius) && upgrade.SplashRadius >= 0 &&
                Enum.IsDefined(upgrade.Attack) && Enum.IsDefined(upgrade.DamageKind) &&
                (upgrade.Attack != AttackKind.Beam || upgrade.SplashRadius == 0), $"branch '{upgrade.Id}' attack/cost");
            ValidateAttack(upgrade);
        }
    }
}
