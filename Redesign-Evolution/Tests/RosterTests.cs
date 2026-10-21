using Redesign.Core;
using Redesign.Infrastructure;
using Xunit;

namespace Redesign.Tests;

public class RosterTests
{
    private static GameDefinition Fixture() => new(2, "roster", 1000, 20,
        new("test", "Test", 10, 3, 32, [new(0, 1), new(9, 1)], [new(0, 0), new(1, 0), new(2, 0)]),
        [new("bolt", 60, 20, 500, 100, 100000), new("beam", 85, 20, 500, 100, 1, Attack: AttackKind.Beam, DamageKind: DamageKind.Energy)],
        [new("runner", 100, 1, 10, 1), new("bulwark", 200, 1, 18, 2, Armor: 9),
         new("warden", 100, 1, 22, 2, ShieldRadius: 100, ShieldPercent: 40)],
        [new("Escorts", "Focus the ward.", [new("runner", 1, 1), new("bulwark", 1, 1), new("warden", 1, 1)])]);

    [Fact]
    public void Parallel_spawn_groups_respect_delays_and_stable_authoring_order()
    {
        var d = Fixture() with { Waves = [new("Mixed", "", [new("runner", 2, 3), new("warden", 1, 1), new("bulwark", 1, 1, 5)])] };
        var sim = new Simulation(d);
        sim.Apply(new StartWave()); sim.Step();
        Assert.Equal(new[] { "runner", "warden" }, sim.Enemies.Select(e => e.Definition.Id));
        Assert.Equal(2, sim.PendingSpawns);
        sim.Apply(new Pause(true));
        for (var i = 0; i < 60; i++) sim.Step();
        Assert.Equal(1, sim.Tick);
        sim.Apply(new Pause(false)); sim.Step(); sim.Step();
        Assert.Equal(2, sim.Enemies.Count);
        sim.Step(); Assert.Equal("runner", sim.Enemies[^1].Definition.Id);
        sim.Step(); Assert.Equal(1, sim.PendingSpawns);
        sim.Step(); Assert.Equal("bulwark", sim.Enemies[^1].Definition.Id);
        Assert.Equal(0, sim.PendingSpawns);
        Assert.Equal(RunPhase.Wave, sim.Phase);
    }

    [Theory]
    [InlineData("bolt", 6)] // (20 - 9 armor) * 60%, rounded down
    [InlineData("beam", 12)] // energy ignores armor, but not an escort
    public void Armor_and_nonstacking_shields_have_different_counters(string tower, int expected)
    {
        var d = Fixture() with { Waves = [new("Shield", "", [new("bulwark", 1, 1), new("warden", 2, 1)])] };
        // Both escorts appear together, not one tick apart.
        d.Waves[0] = d.Waves[0] with { Groups = [new("bulwark", 1, 1), new("warden", 1, 1), new("warden", 1, 1)] };
        var sim = new Simulation(d);
        sim.Apply(new PlaceTower(0, tower)); sim.Apply(new StartWave()); sim.Step();
        Assert.Equal(200 - expected, sim.Enemies[0].Health);
        Assert.Equal(40, sim.Enemies[0].ShieldPercent);
        Assert.All(sim.Enemies.Where(e => e.Definition.Id == "warden"), e =>
        { Assert.Equal(0, e.ShieldPercent); Assert.Equal(100, e.Health); });
        Assert.Equal(expected, sim.DamageDealt);
    }

    [Fact]
    public void Killing_an_escort_removes_protection_before_the_next_hit_in_that_tick()
    {
        var d = Fixture();
        d = d with { Enemies = [d.Enemies[0], d.Enemies[2] with { Health = 15 }],
            Waves = [new("Escort", "", [new("runner", 1, 1), new("warden", 1, 1)])] };
        var sim = new Simulation(d);
        var focus = sim.Apply(new PlaceTower(0, "beam")).EntityId;
        sim.Apply(new SetTargetPriority(focus, TargetPriority.Support));
        sim.Apply(new PlaceTower(1, "bolt")); sim.Apply(new StartWave()); sim.Step();
        Assert.Equal(1, sim.Kills);
        Assert.Equal(80, Assert.Single(sim.Enemies).Health);
        Assert.Equal(0, sim.Enemies[0].ShieldPercent);
        Assert.Equal(35, sim.DamageDealt);
        Assert.Equal(22, sim.GoldEarned);
    }

    [Fact]
    public void Priority_modes_choose_first_highest_health_and_support_with_stable_ties()
    {
        var d = Fixture();
        d = d with { Towers = [d.Towers[0] with { ProjectileSpeed = 1 }] };
        var sim = new Simulation(d);
        var ids = Enumerable.Range(0, 3).Select(i => sim.Apply(new PlaceTower(i)).EntityId).ToArray();
        sim.Apply(new SetTargetPriority(ids[1], TargetPriority.Strongest));
        sim.Apply(new SetTargetPriority(ids[2], TargetPriority.Support));
        sim.Apply(new StartWave()); sim.Step();
        Assert.Equal(sim.Enemies[0].Id, sim.Towers[0].TargetId);
        Assert.Equal(sim.Enemies[1].Id, sim.Towers[1].TargetId);
        Assert.Equal(sim.Enemies[2].Id, sim.Towers[2].TargetId);
        var before = sim.Checksum();
        Assert.False(sim.Apply(new SetTargetPriority(ids[0], (TargetPriority)999)).Success);
        Assert.False(sim.Apply(new PlaceTower(0, "missing")).Success);
        Assert.Equal(before, sim.Checksum());
    }

    [Fact]
    public void Shell_still_splashes_after_its_target_dies_and_its_tower_is_sold()
    {
        var d = Fixture();
        d = d with
        {
            Towers = [new("mortar", 100, 20, 500, 1000, 60, 70, AttackKind.Shell),
                d.Towers[1] with { Damage = 100, CooldownTicks = 1000 }],
            Waves = [new("Pair", "", [new("runner", 1, 1), new("runner", 1, 1)])]
        };
        var sim = new Simulation(d);
        var mortar = sim.Apply(new PlaceTower(0, "mortar")).EntityId;
        sim.Apply(new PlaceTower(1, "beam")); sim.Apply(new StartWave()); sim.Step();
        Assert.Equal(1, sim.Kills); Assert.Single(sim.Projectiles);
        sim.Apply(new SellTower(mortar));
        for (var i = 0; i < 60; i++) sim.Step();
        Assert.Empty(sim.Projectiles);
        Assert.Equal(90, Assert.Single(sim.Enemies).Health);
        Assert.Equal(110, sim.DamageDealt);
        Assert.Equal(10, sim.GoldEarned);
        Assert.Single(sim.DrainEvents(), e => e.Kind == EventKind.Blast);
    }

    [Fact]
    public void Unknown_catalog_ids_duplicate_ids_and_invalid_spawn_data_fail_with_context()
    {
        var d = Fixture();
        var badWave = new WaveDefinition("Invalid", "", [new("typo", 1, 1)]);
        Assert.Contains("wave 1: unknown enemy 'typo'", Assert.Throws<ArgumentException>(() => (d with { Waves = [badWave] }).Validate()).Message);
        Assert.Contains("duplicate tower", Assert.Throws<ArgumentException>(() => (d with { Towers = [d.Towers[0], d.Towers[0]] }).Validate()).Message);
        Assert.Throws<ArgumentException>(() => (d with { Waves = [new("Invalid", "", [new("runner", 1, 0)])] }).Validate());
        Assert.Throws<ArgumentException>(() => (d with { Enemies = [d.Enemies[0] with { ShieldPercent = 100 }] }).Validate());
        var sim = new Simulation(d); var before = sim.Checksum();
        Assert.False(sim.Apply(new PlaceTower(0, "missing")).Success);
        Assert.Equal(before, sim.Checksum());
    }

    [Fact]
    public void Caller_cannot_change_catalogs_or_spawn_groups_of_a_running_simulation()
    {
        var d = Fixture(); var sim = new Simulation(d);
        d.Enemies[0] = d.Enemies[0] with { Health = 1 };
        d.Waves[0].Groups[0] = new("missing", 100, 1);
        sim.Definition.Waves[0].Groups[0] = new("missing", 100, 1);
        sim.Apply(new StartWave()); sim.Step();
        Assert.Equal(3, sim.Enemies.Count); Assert.Equal(100, sim.Enemies[0].Health);
    }

    [Fact]
    public void Six_wave_encounter_is_completable_with_mixed_defenses_and_loses_without_them()
    {
        var d = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "emberwatch.json")));
        var plan = new (int Foundation, string Tower)[]
            { (1, "boltworks"), (2, "prism"), (4, "crucible"), (5, "crucible"), (6, "prism"), (0, "boltworks"), (7, "boltworks"), (3, "prism") };
        foreach (var defend in new[] { true, false })
        {
            var sim = new Simulation(d, 42); var remaining = 100000;
            while (sim.Phase is RunPhase.Wave or RunPhase.Planning && remaining-- > 0)
            {
                if (sim.Phase == RunPhase.Planning)
                {
                    if (defend) foreach (var (foundation, type) in plan)
                    {
                        var result = sim.Apply(new PlaceTower(foundation, type));
                        if (result.Success && type == "prism") sim.Apply(new SetTargetPriority(result.EntityId, TargetPriority.Support));
                    }
                    sim.Apply(new StartWave());
                }
                sim.Step(); sim.DrainEvents();
            }
            Assert.True(remaining > 0);
            Assert.Equal(defend ? RunPhase.Victory : RunPhase.Defeat, sim.Phase);
            if (defend)
            {
                Assert.Equal(d.Waves.Sum(w => w.Groups.Sum(g => g.Count)), sim.Kills + sim.Leaks);
                Assert.All(sim.Towers.GroupBy(t => t.Definition.Id), family => Assert.True(family.Sum(t => t.DamageDealt) > 0));
            }
        }
    }
}
