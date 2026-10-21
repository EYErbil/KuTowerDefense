using Redesign.Core;
using Xunit;

namespace Redesign.Tests;

public class EnemyAbilityTests
{
    private static GameDefinition Fixture() => new(2, "abilities", 1000, 100,
        new("road", "Road", 10, 3, 32, [new(0, 1), new(9, 1)], [new(1, 0)]),
        [new("beam", 10, 40, 500, 1000, 1, Attack: AttackKind.Beam)],
        [new("grunt", 200, .01, 0, 1), new("mender", 100, .01, 0, 1, Healing: new(12, 100, 60, 20)),
         new("boss", 1000, .01, 0, 12, Armor: 20, Boss: new("grunt", 2, 4, 120, 30, 50, 50, 25), SlowResistancePercent: 50)],
        [new("Abilities", "", [new("grunt", 1, 1), new("mender", 1, 1), new("mender", 1, 1)])]);

    [Fact]
    public void Healing_is_telegraphed_capped_nonresurrecting_and_does_not_multiply_simultaneous_pulses()
    {
        var run = new Simulation(Fixture()); run.Apply(new StartWave()); run.Step();
        var grunt = run.Enemies[0]; grunt.Health = 150; run.Enemies[1].Health = 10;
        while (run.Tick < 41) run.Step();
        Assert.All(run.Enemies.Skip(1), e => Assert.True(e.WindingUp));
        while (run.Tick < 61) run.Step();
        Assert.Equal(162, grunt.Health); Assert.Equal(10, run.Enemies[1].Health);
        Assert.Single(run.DrainEvents(), e => e.Kind == EventKind.Healed);
        grunt.Health = 199; while (run.Tick < 121) run.Step(); Assert.Equal(200, grunt.Health);
        grunt.Alive = false; while (run.Tick < 181) run.Step();
        Assert.DoesNotContain(run.Enemies, e => e.Id == grunt.Id);
    }

    [Fact]
    public void Support_priority_targets_menders_and_a_dead_mender_never_pulses()
    {
        var run = new Simulation(Fixture()); var id = run.Apply(new PlaceTower(0)).EntityId;
        run.Apply(new SetTargetPriority(id, TargetPriority.Support)); run.Apply(new StartWave()); run.Step();
        Assert.Equal(run.Enemies[1].Id, run.Towers[0].TargetId);
        foreach (var e in run.Enemies.Where(e => e.Definition.Healing is not null)) e.Alive = false;
        while (run.Tick < 80) run.Step(); Assert.DoesNotContain(run.DrainEvents(), e => e.Kind == EventKind.Healed);
    }

    [Fact]
    public void Boss_guard_opens_during_windup_enrage_latches_and_summons_are_bounded()
    {
        var d = Fixture() with { Waves = [new("Boss", "", [new("boss", 1, 1)])] };
        var run = new Simulation(d); run.Apply(new PlaceTower(0)); run.Apply(new StartWave()); run.Step();
        var boss = run.Enemies[0]; Assert.Equal(990, boss.Health); Assert.Equal(50, boss.ShieldPercent);
        while (run.Tick < 90) run.Step(); run.Towers[0].NextFireTick = 91; run.Step();
        Assert.True(boss.BossVulnerable); Assert.Equal(0, boss.EffectiveArmor); Assert.Equal(0, boss.ShieldPercent);
        Assert.Equal(950, boss.Health);
        run.Apply(new Pause(true)); var paused = run.Checksum(); for (var i = 0; i < 200; i++) run.Step();
        Assert.Equal(paused, run.Checksum()); run.Apply(new Pause(false));
        boss.Health = 490; run.Step(); Assert.True(boss.Enraged); boss.Health = 800;
        while (run.Tick < 400) run.Step();
        Assert.True(boss.Enraged); Assert.Equal(4, boss.SummonsCreated); Assert.Equal(5, run.Enemies.Count);
        Assert.True(boss.BossVulnerable); Assert.Equal(0, boss.ShieldPercent); Assert.Equal(0, boss.EffectiveArmor);
        var events = run.DrainEvents(); Assert.Equal(2, events.Count(e => e.Kind == EventKind.Rally));
        Assert.Single(events, e => e.Kind == EventKind.BossEnraged); Assert.Equal(0, run.GoldEarned);
    }

    [Fact]
    public void Boss_cannot_summon_bosses_supports_or_bounty_farms()
    {
        var d = Fixture(); var boss = d.Enemies[2];
        foreach (var target in new[] { "boss", "mender", "missing" })
            Assert.Throws<ArgumentException>(() => (d with { Enemies = [d.Enemies[0], d.Enemies[1], boss with { Boss = boss.Boss! with { SummonEnemyId = target } }] }).Validate());
        Assert.Throws<ArgumentException>(() => (d with { Enemies = [d.Enemies[0] with { Bounty = 10 }, boss] }).Validate());
    }
}
