using System.Text.Json;
using Redesign.Core;
using Redesign.Infrastructure;

internal static class ExpeditionRunner
{
    public static int Run(string[] args)
    {
        var content = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "expedition.json")));
        var batch = args.Contains("--balance");
        var seedArg = Array.IndexOf(args, "--seed"); var styleArg = Array.IndexOf(args, "--strategy");
        var seeds = batch ? new ulong[] { 1, 7, 42 } : [seedArg >= 0 ? ulong.Parse(args[seedArg + 1]) : 42];
        var styles = batch ? new[] { "frost", "fire", "steel", "basic", "hold", "none" } : [args.Contains("--no-build") ? "none" : styleArg >= 0 ? args[styleArg + 1] : "frost"];
        var success = true;
        foreach (var seed in seeds) foreach (var style in styles)
        {
            var run = new Simulation(content, seed); var budget = 150000;
            while (run.Phase is RunPhase.Planning or RunPhase.Wave && budget-- > 0)
            {
                if (run.Phase == RunPhase.Planning)
                {
                    foreach (var command in ExpeditionStrategy.PlanningCommands(run, style, args.Contains("--risk")))
                    {
                        var result = run.Apply(command);
                        if (!result.Success) throw new InvalidOperationException($"Wave {run.WaveNumber + 1} {command}: {result.Message}");
                    }
                    if (!run.Apply(new StartWave()).Success) throw new InvalidOperationException("Blocked wave.");
                }
                run.Step(); run.DrainEvents();
            }
            Console.WriteLine(JsonSerializer.Serialize(new { Style = style, run.Seed, Outcome = run.Phase.ToString(),
                Wave = run.WaveNumber, run.Tick, run.Kills, run.Leaks, run.Lives, run.Gold, run.GoldSpent, run.ReactionDamage,
                Relics = run.Relics.Select(r => r.Id), Waves = run.WaveReports,
                Towers = run.Towers.Select(t => new { t.Definition.Id, t.Foundation, t.DamageDealt, t.Kills }), Checksum = run.Checksum() }));
            success &= budget > 0 && (batch || run.Phase == (style == "none" ? RunPhase.Defeat : RunPhase.Victory));
        }
        return success ? 0 : 1;
    }
}
