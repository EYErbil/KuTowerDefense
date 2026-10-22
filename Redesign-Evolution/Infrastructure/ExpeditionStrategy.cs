using Redesign.Core;

namespace Redesign.Infrastructure;

/// <summary>A published purchase/deployment experiment, not live AI or a rule of the game.</summary>
public sealed record SquadDeployment(int Foundation, int RecruitBeforeWave, double RallyOffset = 0);

/// <summary>Deliberately simple, published balance witnesses. They use the same commands as the player.</summary>
public static class ExpeditionStrategy
{
    public static IEnumerable<GameCommand> PlanningCommands(Simulation run, string style = "frost", bool risk = false, bool squad = false, SquadDeployment? deployment = null)
    {
        if (run.RewardOffers.Count > 0)
        {
            var priorities = style switch
            {
                "fire" => new[] { RelicKind.LongBurn, RelicKind.BeamFork, RelicKind.BoltFork, RelicKind.ShellEcho, RelicKind.WarChest, RelicKind.ReinforcedGate, RelicKind.ExtraChill },
                "steel" => [RelicKind.ShellEcho, RelicKind.BoltFork, RelicKind.WarChest, RelicKind.BeamFork, RelicKind.ReinforcedGate, RelicKind.ExtraChill, RelicKind.LongBurn],
                _ => [RelicKind.ExtraChill, RelicKind.BeamFork, RelicKind.ShellEcho, RelicKind.BoltFork, RelicKind.WarChest, RelicKind.ReinforcedGate, RelicKind.LongBurn]
            };
            var chosen = run.RewardOffers.OrderBy(r => run.Lives <= 4 && r.Kind == RelicKind.ReinforcedGate ? -1 : Array.IndexOf(priorities, r.Kind)).First();
            yield return new ChooseRelic(chosen.Id);
        }
        var content = run.Definition;
        deployment ??= new(3, 3);
        if (squad && style != "none" && run.Garrison is null && run.WaveNumber + 1 >= deployment.RecruitBeforeWave &&
            content.Garrison is { } garrison && run.Gold >= garrison.Cost)
            yield return new PlaceGarrison(deployment.Foundation);
        if (squad && run.Garrison is { } guards)
        {
            var progress = Math.Clamp(guards.EntryProgress + deployment.RallyOffset, 0, run.Route.Length);
            if (progress != guards.RallyProgress || guards.Retreating) yield return new RallySquad(run.Route.At(progress));
        }
        var plan = style switch
        {
            "fire" => new[] { (1, "boltworks", false), (2, "prism", false), (0, "crucible", false), (1, "pitchbow", true), (2, "sun", true),
                (3, "prism", false), (3, "sun", true), (4, "crucible", false), (0, "breach", true),
                (4, "echo", true), (5, "boltworks", false), (5, "pitchbow", true), (6, "prism", false), (6, "sun", true), (7, "boltworks", false), (7, "ballista", true) },
            "steel" => [(1, "boltworks", false), (0, "crucible", false), (2, "prism", false), (0, "breach", true),
                (1, "ballista", true), (4, "crucible", false), (4, "echo", true), (3, "boltworks", false), (3, "ballista", true),
                (5, "crucible", false), (5, "breach", true), (6, "prism", false), (7, "boltworks", false), (7, "ballista", true)],
            "basic" => [(1, "boltworks", false), (2, "prism", false), (0, "crucible", false), (3, "prism", false),
                (4, "crucible", false), (5, "boltworks", false), (6, "prism", false), (7, "boltworks", false)],
            _ => [(1, "boltworks", false), (2, "prism", false), (0, "crucible", false), (2, "rime", true),
                (1, "ballista", true), (4, "crucible", false), (0, "breach", true), (3, "prism", false), (3, "rime", true),
                (4, "echo", true), (5, "boltworks", false), (5, "ballista", true), (6, "prism", false), (7, "boltworks", false), (7, "ballista", true)]
        };
        if (style == "hold") plan = plan.Take(2).ToArray();
        if (style == "none") plan = [];
        foreach (var (foundation, id, upgrade) in plan)
        {
            if (squad && foundation == deployment.Foundation) continue;
            var tower = run.Towers.FirstOrDefault(t => t.Foundation == foundation);
            if (!upgrade && tower is not null || upgrade && tower?.BranchId is not null) continue;
            var cost = upgrade ? content.Combat!.Branches.Single(b => b.Upgrade.Id == id).Cost : content.Towers.Single(t => t.Id == id).Cost;
            if (run.Gold < cost) break; // Save toward the next deliberate purchase instead of spending on filler.
            yield return upgrade ? new UpgradeTower(tower!.Id, id) : new PlaceTower(foundation, id);
        }
        foreach (var tower in run.Towers)
            yield return new SetTargetPriority(tower.Id, tower.Definition.DamageKind is DamageKind.Fire or DamageKind.Frost ?
                (tower.Definition.DamageKind == DamageKind.Fire ? TargetPriority.Primed : TargetPriority.Strongest) :
                tower.Definition.DamageKind == DamageKind.Energy ? TargetPriority.Support : TargetPriority.Primed);
        yield return new SetChallenge(risk);
    }
}
