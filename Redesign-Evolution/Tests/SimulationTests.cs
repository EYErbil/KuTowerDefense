using Redesign.Core;
using Redesign.Infrastructure;
using Xunit;

namespace Redesign.Tests;

public class SimulationTests
{
    private static GameDefinition Fixture() => new(2, "test", 500, 5,
        new("test-map", "Test", 5, 3, 32, [new(0, 1), new(4, 1)], [new(1, 0), new(3, 0)]),
        [new("bolt", 60, 18, 250, 42, 480)], [new("runner", 44, 48, 10, 1)], [W(3, 60)]);
    private static WaveDefinition W(int count, int interval) => new("Test wave", "", [new("runner", count, interval)]);
    private static Simulation Armed(GameDefinition definition)
    {
        var sim = new Simulation(definition, 42);
        Assert.True(sim.Apply(new PlaceTower(0)).Success);
        Assert.True(sim.Apply(new StartWave()).Success);
        return sim;
    }
    private static void Finish(Simulation sim, int limit = 20000)
    {
        while (sim.Phase == RunPhase.Wave && limit-- > 0) sim.Step();
        Assert.NotEqual(RunPhase.Wave, sim.Phase);
    }

    [Fact] public void Crossing_a_near_target_hits_instead_of_oscillating()
    {
        var d = Fixture();
        var sim = Armed(d with { Enemies = [d.Enemies[0] with { Health = 1 }], Towers = [d.Towers[0] with { ProjectileSpeed = 100000 }], Waves = [W(1, 1)] });
        sim.Step();
        Assert.Equal(1, sim.Kills); Assert.Equal(RunPhase.Victory, sim.Phase);
        Assert.Empty(sim.Projectiles); Assert.Equal(1, sim.DamageDealt);
    }
    [Fact] public void Splash_kills_each_enemy_and_rewards_exactly_once()
    {
        var d = Fixture();
        // Slow projectile allows both targets to spawn before impact; both are in splash range.
        var sim = Armed(d with { Enemies = [d.Enemies[0] with { Health = 1, Speed = .01, Bounty = 50 }], Towers = [d.Towers[0] with { ProjectileSpeed = 60, SplashRadius = 50 }], Waves = [W(2, 1)] });
        Finish(sim);
        Assert.Equal(2, sim.Kills); Assert.Equal(100, sim.GoldEarned); Assert.Equal(540, sim.Gold);
        Assert.Equal(2, sim.DrainEvents().Count(e => e.Kind == EventKind.Killed));
        var digest = sim.Checksum(); for (var i = 0; i < 100; i++) sim.Step();
        Assert.Equal(digest, sim.Checksum());
    }
    [Fact] public void Killed_enemy_at_exit_does_not_leak()
    {
        var d = Fixture();
        var sim = Armed(d with { Enemies = [d.Enemies[0] with { Health = 1, Speed = 100000 }], Towers = [d.Towers[0] with { ProjectileSpeed = 100000 }], Waves = [W(1, 1)] });
        sim.Step(); Assert.Equal(0, sim.Leaks); Assert.Equal(5, sim.Lives); Assert.Equal(RunPhase.Victory, sim.Phase);
    }
    [Fact] public void Final_wave_leak_is_defeat_not_victory()
    {
        var d = Fixture();
        var sim = new Simulation(d with { StartingLives = 1, Enemies = [d.Enemies[0] with { Speed = 100000 }], Waves = [W(1, 1)] });
        sim.Apply(new StartWave()); sim.Step();
        Assert.Equal(RunPhase.Defeat, sim.Phase); Assert.Equal(0, sim.Lives);
        Assert.DoesNotContain(sim.DrainEvents(), e => e.Kind == EventKind.Victory);
    }
    [Fact] public void Pause_freezes_spawns_movement_projectiles_and_cooldowns()
    {
        var sim = Armed(Fixture()); sim.Step(); sim.Apply(new Pause(true));
        var checksum = sim.Checksum(); var clock = new SimulationClock();
        for (var i = 0; i < 200; i++) { sim.Step(); clock.Advance(sim, 1, 4); }
        Assert.Equal(checksum, sim.Checksum());
        sim.Apply(new Pause(false)); sim.Step(); Assert.Equal(2, sim.Tick);
    }
    [Fact] public void Pausing_before_first_tick_prevents_initial_spawn()
    {
        var sim = Armed(Fixture()); sim.Apply(new Pause(true));
        for (var i = 0; i < 1000; i++) sim.Step();
        Assert.Empty(sim.Enemies); Assert.Equal(3, sim.PendingSpawns); Assert.Equal(0, sim.Tick);
    }
    [Theory]
    [InlineData(30, 1)] [InlineData(60, 1)] [InlineData(144, 1)]
    [InlineData(30, 2)] [InlineData(144, 4)]
    public void Render_rate_and_speed_do_not_change_results(int fps, int speed)
    {
        var expected = Armed(Fixture()); Finish(expected);
        var actual = Armed(Fixture()); var clock = new SimulationClock(); var frames = 100000;
        while (actual.Phase == RunPhase.Wave && frames-- > 0) { clock.Advance(actual, 1d / fps, speed); actual.DrainEvents(); }
        Assert.True(frames > 0); Assert.Equal(expected.Checksum(), actual.Checksum());
    }
    [Fact] public void Catchup_is_bounded_but_retains_tick_debt()
    {
        var d = Fixture(); var sim = Armed(d with { Enemies = [d.Enemies[0] with { Health = 10000, Speed = .01 }] });
        var clock = new SimulationClock(); Assert.Equal(32, clock.Advance(sim, 1, 1));
        Assert.Equal(28, clock.Advance(sim, 0, 1)); Assert.Equal(60, sim.Tick);
    }
    [Fact] public void Pending_spawn_prevents_early_wave_completion()
    {
        var d = Fixture(); var sim = Armed(d with { Enemies = [d.Enemies[0] with { Health = 1 }], Towers = [d.Towers[0] with { ProjectileSpeed = 100000 }], Waves = [W(2, 1000)] });
        sim.Step(); Assert.Empty(sim.Enemies); Assert.Equal(1, sim.PendingSpawns); Assert.Equal(RunPhase.Wave, sim.Phase);
    }
    [Fact] public void Finishing_a_wave_does_not_carry_time_into_next_planning_action()
    {
        var d = Fixture();
        var sim = Armed(d with { Enemies = [d.Enemies[0] with { Health = 1 }], Towers = [d.Towers[0] with { ProjectileSpeed = 100000 }], Waves = [W(1, 1), W(1, 1)] });
        var clock = new SimulationClock(); clock.Advance(sim, 1, 1);
        Assert.Equal(RunPhase.Planning, sim.Phase); Assert.Equal(1, sim.Tick);
        sim.Apply(new StartWave()); Assert.Equal(0, clock.Advance(sim, 0, 1)); Assert.Equal(1, sim.Tick);
    }
    [Fact] public void Start_cannot_duplicate_a_spawn_queue()
    {
        var sim = Armed(Fixture()); var checksum = sim.Checksum();
        Assert.False(sim.Apply(new StartWave()).Success); Assert.Equal(checksum, sim.Checksum());
    }
    [Fact] public void Invalid_and_occupied_placements_are_atomic()
    {
        var sim = new Simulation(Fixture()); var before = sim.Checksum();
        Assert.False(sim.Apply(new PlaceTower(-1)).Success); Assert.False(sim.Apply(new PlaceTower(99)).Success);
        Assert.Equal(before, sim.Checksum()); Assert.True(sim.Apply(new PlaceTower(0)).Success);
        before = sim.Checksum(); Assert.False(sim.Apply(new PlaceTower(0)).Success); Assert.Equal(before, sim.Checksum());
    }
    [Fact] public void Unaffordable_placement_spends_nothing()
    {
        var sim = new Simulation(Fixture() with { StartingGold = 0 }); var before = sim.Checksum();
        Assert.False(sim.Apply(new PlaceTower(0)).Success); Assert.Equal(before, sim.Checksum());
    }
    [Fact] public void Selling_twice_cannot_create_gold_or_remove_another_tower()
    {
        var sim = new Simulation(Fixture()); var id = sim.Apply(new PlaceTower(0)).EntityId;
        Assert.True(sim.Apply(new SellTower(id)).Success); Assert.Equal(482, sim.Gold);
        Assert.False(sim.Apply(new SellTower(id)).Success); Assert.Equal(482, sim.Gold);
        Assert.True(sim.Apply(new PlaceTower(0)).Success);
    }
    [Fact] public void Configurable_economy_does_not_overflow_32_bit_currency()
    {
        var d = Fixture();
        var sim = Armed(d with { StartingGold = int.MaxValue, Enemies = [d.Enemies[0] with { Health = 1, Bounty = int.MaxValue }], Towers = [d.Towers[0] with { ProjectileSpeed = 100000 }], Waves = [W(1, 1)] });
        sim.Step(); Assert.Equal(2L * int.MaxValue - d.Towers[0].Cost, sim.Gold);
        var rich = new Simulation(d with { StartingGold = int.MaxValue, Towers = [d.Towers[0] with { Cost = int.MaxValue }] });
        var id = rich.Apply(new PlaceTower(0)).EntityId;
        Assert.True(rich.Apply(new SellTower(id)).Success); Assert.Equal((long)int.MaxValue * 70 / 100, rich.Gold);
    }
    [Fact] public void Inflight_projectile_retains_attribution_after_tower_sale()
    {
        var d = Fixture(); var sim = Armed(d with { Enemies = [d.Enemies[0] with { Health = 1, Speed = .01 }], Towers = [d.Towers[0] with { ProjectileSpeed = 60 }], Waves = [W(1, 1)] });
        sim.Step(); Assert.Single(sim.Projectiles); sim.Apply(new SellTower(sim.Towers[0].Id));
        Finish(sim); Assert.Equal(1, sim.Kills); Assert.Equal(10, sim.GoldEarned);
    }
    [Fact] public void Targeting_chooses_furthest_progress()
    {
        var d = Fixture(); var sim = Armed(d with { Towers = [d.Towers[0] with { ProjectileSpeed = 1 }], Waves = [W(2, 1)] });
        sim.Step(); var first = sim.Enemies[0].Id; sim.Step();
        Assert.Equal(first, sim.Towers[0].TargetId);
    }
    [Fact] public void Route_interpolates_turns_and_clamps_ends()
    {
        var d = Fixture(); var route = new Route(d.Map with { Route = [new(0, 1), new(2, 1), new(2, 2)] });
        Assert.Equal(new Point(16, 48), route.At(-1)); Assert.Equal(new Point(80, 48), route.At(64));
        Assert.Equal(new Point(80, 64), route.At(80)); Assert.Equal(new Point(80, 80), route.At(999));
    }
    [Fact] public void Restart_is_a_clean_instance_and_does_not_reuse_state()
    {
        var first = Armed(Fixture()); Finish(first);
        var fresh = new Simulation(Fixture(), 42); var reference = new Simulation(Fixture(), 42);
        Assert.Equal(reference.Checksum(), fresh.Checksum()); Assert.Empty(fresh.Towers); Assert.Equal(500, fresh.Gold);
    }
    [Fact] public void Caller_cannot_mutate_a_running_map_through_definition_arrays()
    {
        var d = Fixture(); var sim = new Simulation(d); var origin = sim.Route.At(0);
        d.Map.Route[0] = new(99, 99); sim.Definition.Map.Route[0] = new(90, 90);
        Assert.Equal(origin, sim.Route.At(0)); Assert.Equal(new Cell(0, 1), sim.Definition.Map.Route[0]);
    }
    [Fact] public void Shipped_content_has_winnable_and_losing_complete_runs()
    {
        var d = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "foundation.json")));
        var win = new Simulation(d);
        foreach (var index in new[] { 1, 2, 4 }) Assert.True(win.Apply(new PlaceTower(index)).Success);
        while (win.Phase == RunPhase.Planning)
        {
            for (var i = 0; i < d.Map.Foundations.Length; i++) win.Apply(new PlaceTower(i));
            win.Apply(new StartWave()); Finish(win);
        }
        Assert.Equal(RunPhase.Victory, win.Phase); Assert.Equal(36, win.Kills + win.Leaks);
        var lose = new Simulation(d);
        while (lose.Phase == RunPhase.Planning) { lose.Apply(new StartWave()); Finish(lose); }
        Assert.Equal(RunPhase.Defeat, lose.Phase);
    }
    [Fact] public void Invalid_content_is_rejected_with_actionable_context()
    {
        var d = Fixture();
        Assert.Contains("foundation", Assert.Throws<ArgumentException>(() => (d with { Map = d.Map with { Foundations = [new(0, 1)] } }).Validate()).Message);
        Assert.Throws<ArgumentException>(() => (d with { Towers = [d.Towers[0] with { Range = double.NaN }] }).Validate());
        Assert.Throws<ArgumentException>(() => (d with { Map = d.Map with { Route = [new(0, 0), new(1, 1)] } }).Validate());
        Assert.Throws<ArgumentException>(() => (d with { Waves = [W(0, 1)] }).Validate());
        Assert.Throws<System.Text.Json.JsonException>(() => ContentLoader.Parse("{\"unknownField\":1}"));
        Assert.Throws<ArgumentException>(() => ContentLoader.Parse("null"));
    }
}
