using Redesign.Core;
using Redesign.Infrastructure;
using Xunit;

namespace Redesign.Tests;

public class EvolutionTests
{
    private static GameDefinition Content() => ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "emberwatch.json")));

    [Theory]
    [InlineData(30, 1)] [InlineData(144, 4)]
    public void Six_paths_and_both_reactions_complete_a_repeatable_run_at_different_render_rates(int fps, int speed)
    {
        var content = Content();
        var expected = Complete(content, 0, 1);
        var actual = Complete(content, fps, speed);
        Assert.Equal(expected.Checksum(), actual.Checksum());
        Assert.Equal(RunPhase.Victory, actual.Phase);
        Assert.Equal(151, actual.Kills); Assert.Equal(0, actual.Leaks);
        Assert.Equal(content.Combat!.Branches.Select(b => b.Upgrade.Id).Order(), actual.Towers.Select(t => t.BranchId).Order());
        Assert.All(actual.Reactions, r => { Assert.True(r.Triggers > 0); Assert.True(r.Damage > 0); });
        Assert.Equal(actual.ReactionDamage, actual.Reactions.Sum(r => r.Damage));
        Assert.Equal(actual.DamageDealt, actual.Towers.Sum(t => t.DamageDealt));
        Assert.Equal(content.StartingGold + actual.GoldEarned - actual.GoldSpent, actual.Gold);
        Assert.Empty(actual.BurningAreas); Assert.Empty(actual.PendingImpacts);
    }

    private static Simulation Complete(GameDefinition content, int fps, int speed)
    {
        var run = new Simulation(content, 42); var clock = new SimulationClock(); var limit = 100000;
        while (run.Phase is RunPhase.Wave or RunPhase.Planning && limit-- > 0)
        {
            if (run.Phase == RunPhase.Planning)
            {
                foreach (var command in EvolutionShowcase.PlanningCommands(run)) Assert.True(run.Apply(command).Success, command.ToString());
                Assert.True(run.Apply(new StartWave()).Success);
            }
            if (fps == 0) run.Step(); else clock.Advance(run, 1d / fps, speed);
            run.DrainEvents();
        }
        Assert.True(limit > 0); return run;
    }

    [Fact]
    public void Nested_splash_reactions_fields_and_echoes_match_reference_queries_under_load()
    {
        var content = Content();
        content = content with
        {
            StartingGold = 10000, StartingLives = 10000,
            Enemies = content.Enemies.Select(e => e with { Health = 500 }).ToArray(),
            Combat = content.Combat! with
            {
                Branches = content.Combat.Branches.Select(b => b with { Upgrade = b.Upgrade with { Range = 500, CooldownTicks = 9 } }).ToArray()
            },
            Waves = [new("Reactions under load", "", [new("runner", 300, 1), new("bulwark", 30, 8), new("warden", 12, 30)])]
        };
        var indexed = new Simulation(content, 42); var scanned = new Simulation(content, 42, true);
        foreach (var run in new[] { indexed, scanned })
        {
            for (var i = 0; i < content.Combat.Branches.Length; i++)
            {
                var branch = content.Combat.Branches[i];
                var tower = run.Apply(new PlaceTower(i, branch.TowerId)).EntityId;
                Assert.True(run.Apply(new UpgradeTower(tower, branch.Upgrade.Id)).Success);
                run.Apply(new SetTargetPriority(tower, TargetPriority.Primed));
            }
            run.Apply(new StartWave());
        }
        var echoes = 0; var sawCappedAreas = false;
        for (var tick = 0; tick < 1800; tick++)
        {
            indexed.Step(); scanned.Step();
            var events = indexed.DrainEvents();
            Assert.Equal(scanned.DrainEvents(), events);
            echoes += events.Count(e => e.Kind == EventKind.Blast && e.EffectId == "echo");
            sawCappedAreas |= indexed.BurningAreas.Count == 6;
            Assert.InRange(indexed.BurningAreas.Count, 0, 6);
            if (tick % 60 == 0) Assert.Equal(scanned.Checksum(), indexed.Checksum());
        }
        Assert.Equal(scanned.Checksum(), indexed.Checksum());
        Assert.All(indexed.Reactions, r => Assert.True(r.Triggers > 0));
        Assert.True(echoes > 0); Assert.True(sawCappedAreas); Assert.True(indexed.Kills > 0);
    }
}
