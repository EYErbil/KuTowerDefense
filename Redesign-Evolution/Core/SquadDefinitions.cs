namespace Redesign.Core;

public sealed record GuardDefinition(string Id, string Name, int Health, int Damage, double Speed,
    double Reach, int CooldownTicks, int WindupTicks);
public sealed record GarrisonDefinition(string Id, string Name, string Description, int Cost,
    double CommandRadius, GuardDefinition Guard);
public sealed record MeleeDefinition(int Damage, double Reach, int CooldownTicks, int WindupTicks,
    bool Unstoppable = false, bool Cleave = false);

internal static class SquadContent
{
    public static void Validate(GameDefinition game)
    {
        static void Require(bool valid, string field)
        {
            if (!valid) throw new ArgumentException("Invalid squad content: " + field);
        }
        static bool Distance(double value) => double.IsFinite(value) && value is > 0 and <= 4096;
        static bool Timing(int cooldown, int windup) => cooldown is >= 2 and <= 36000 && windup > 0 && windup < cooldown;

        foreach (var enemy in game.Enemies)
        {
            if (enemy.Melee is not { } melee)
            {
                Require(game.Garrison is null, $"enemy '{enemy.Id}' needs explicit melee rules when squads are enabled");
                continue;
            }
            Require(melee.Damage is > 0 and <= 10000 && Distance(melee.Reach) && Timing(melee.CooldownTicks, melee.WindupTicks),
                $"enemy '{enemy.Id}' melee values");
            Require(!melee.Cleave || melee.Unstoppable, $"enemy '{enemy.Id}' cleave requires unstoppable movement");
            Require(enemy.Boss is null || melee.Unstoppable && melee.Cleave,
                $"boss '{enemy.Id}' must be unstoppable with a cleave");
        }
        if (game.Garrison is not { } garrison) return;
        Require(!string.IsNullOrWhiteSpace(garrison.Id) && !string.IsNullOrWhiteSpace(garrison.Name) &&
            !string.IsNullOrWhiteSpace(garrison.Description) && !game.Towers.Any(t => t.Id == garrison.Id), "garrison identity");
        Require(garrison.Cost is > 0 and <= 10000 && Distance(garrison.CommandRadius), "garrison cost/command radius");
        Require(garrison.Guard is not null, "guard definition");
        if (garrison.Guard is not { } guard) return;
        Require(!string.IsNullOrWhiteSpace(guard.Id) && !string.IsNullOrWhiteSpace(guard.Name) &&
            guard.Health is > 0 and <= 10000 && guard.Damage is > 0 and <= 10000 &&
            double.IsFinite(guard.Speed) && guard.Speed is > 0 and <= 4096 && Distance(guard.Reach) &&
            Timing(guard.CooldownTicks, guard.WindupTicks), "guard identity/combat values");
        // A blocker must be close enough for its opponent to retaliate.
        Require(game.Enemies.All(e => e.Melee!.Reach >= guard.Reach), "enemy melee reach must cover guard engagement reach");
        var route = new Route(game.Map);
        Require(game.Map.Foundations.Any(c =>
            (route.At(route.NearestProgress(game.Map.Center(c))) - game.Map.Center(c)).Length <= garrison.CommandRadius),
            "no foundation can reach the road");
    }
}
