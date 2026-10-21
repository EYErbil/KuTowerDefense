using System.Text.Json;
using Redesign.Core;
using Redesign.Infrastructure;

if (args.Contains("--benchmark")) return CombatBenchmark.Run();
var lose = args.Contains("--no-build");
var foundation = args.Contains("--foundation");
var evolution = args.Contains("--evolution") && !foundation;
var definition = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, foundation ? "foundation.json" : "emberwatch.json")));
var sim = new Simulation(definition, 42);
var plan = new (int Foundation, string Tower)[]
{
    (1, "boltworks"), (2, "prism"), (4, "crucible"), (5, "crucible"),
    (6, "prism"), (0, "boltworks"), (7, "boltworks"), (3, "prism")
};
if (!lose && foundation) foreach (var index in new[] { 1, 2, 4 }) sim.Apply(new PlaceTower(index));
var budget = 100000;
while (sim.Phase is RunPhase.Planning or RunPhase.Wave && budget-- > 0)
{
    if (sim.Phase == RunPhase.Planning)
    {
        if (!lose)
        {
            if (evolution)
            {
                foreach (var command in EvolutionShowcase.PlanningCommands(sim))
                {
                    var result = sim.Apply(command);
                    if (!result.Success) throw new InvalidOperationException($"Showcase wave {sim.WaveNumber + 1}: {command}: {result.Message}");
                }
            }
            else if (foundation) for (var i = 0; i < definition.Map.Foundations.Length; i++) sim.Apply(new PlaceTower(i));
            else foreach (var (index, type) in plan)
            {
                var result = sim.Apply(new PlaceTower(index, type));
                if (result.Success && type == "prism") sim.Apply(new SetTargetPriority(result.EntityId, TargetPriority.Support));
            }
        }
        sim.Apply(new StartWave());
    }
    sim.Step(); sim.DrainEvents();
}
Console.WriteLine(JsonSerializer.Serialize(new { sim.Phase, sim.Tick, sim.Kills, sim.Leaks, sim.Gold, sim.Lives, sim.ReactionDamage,
    Reactions = sim.Reactions.Select(r => new { r.Definition.Id, r.Triggers, r.Damage }),
    Towers = evolution ? sim.Towers.Select(t => new { t.Definition.Id, t.DamageDealt, t.ReactionsPrepared, t.ReactionsTriggered }) : null,
    Checksum = sim.Checksum() }));
return budget <= 0 || sim.Phase != (lose ? RunPhase.Defeat : RunPhase.Victory) ? 1 : 0;
