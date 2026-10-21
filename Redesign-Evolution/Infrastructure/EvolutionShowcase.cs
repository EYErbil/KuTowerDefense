using Redesign.Core;

namespace Redesign.Infrastructure;

/// <summary>Authored, opt-in demonstration used by captures, CLI runs and UI verification. Never plays for the player.</summary>
public static class EvolutionShowcase
{
    public static IEnumerable<GameCommand> PlanningCommands(Simulation run)
    {
        var orders = run.WaveNumber switch
        {
            0 => new[] { (1, "boltworks", false), (2, "prism", false), (4, "crucible", false) },
            1 => [(2, "rime", true)],
            2 => [(3, "prism", false), (1, "pitchbow", true)],
            3 => [(3, "sun", true)],
            4 => [(4, "echo", true), (5, "crucible", false), (5, "breach", true)],
            5 => [(0, "boltworks", false), (0, "ballista", true)],
            _ => []
        };
        foreach (var (foundation, id, upgrade) in orders)
            yield return upgrade ? new UpgradeTower(run.Towers.Single(t => t.Foundation == foundation).Id, id) : new PlaceTower(foundation, id);
        foreach (var tower in run.Towers)
            yield return new SetTargetPriority(tower.Id, tower.Definition.DamageKind is DamageKind.Impact or DamageKind.Fire ? TargetPriority.Primed : TargetPriority.First);
    }
}
