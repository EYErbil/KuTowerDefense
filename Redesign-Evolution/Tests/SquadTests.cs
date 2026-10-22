using Redesign.Core;
using Redesign.Infrastructure;
using Xunit;

namespace Redesign.Tests;

public class SquadTests
{
    private static GameDefinition Fixture(int count = 4) => new(2, "squad-test", 1000, 100,
        new("road", "Road", 10, 5, 32, [new(0, 1), new(9, 1)], [new(2, 0), new(4, 0), new(9, 4)]),
        [new("beam", 10, 100, 500, 60, 1, Attack: AttackKind.Beam, DamageKind: DamageKind.Energy)],
        [new("grunt", 1000, 60, 7, 1, Melee: new(10, 12, 30, 10))],
        [new("Pressure", "", [new("grunt", count, 1)])],
        Garrison: new("garrison", "Garrison", "Three guards", 120, 240, new("guard", "Guard", 30, 4, 64, 8, 30, 5)));

    private static Simulation Deploy(GameDefinition? content = null, bool reference = false)
    {
        var run = new Simulation(content ?? Fixture(), 42, reference);
        Assert.True(run.Apply(new PlaceGarrison(0)).Success);
        Assert.True(run.Apply(new StartWave()).Success);
        return run;
    }

    private static void Until(Simulation run, Func<bool> condition, int budget = 10000)
    {
        while (!condition() && run.Phase == RunPhase.Wave && budget-- > 0) run.Step();
        Assert.True(condition(), $"Condition not reached: tick {run.Tick}, phase {run.Phase}");
    }

    private static void RejectedWithoutMutation(Simulation run, GameCommand command)
    {
        run.DrainEvents();
        var before = run.Checksum();
        Assert.False(run.Apply(command).Success);
        Assert.Equal(before, run.Checksum());
        Assert.Empty(run.DrainEvents());
    }

    [Fact]
    public void Purchases_orders_and_shared_foundations_validate_before_any_mutation()
    {
        var d = Fixture();
        var empty = new Simulation(d with { Garrison = null });
        RejectedWithoutMutation(empty, new PlaceGarrison(0));
        RejectedWithoutMutation(empty, new RallySquad(new(80, 48)));
        RejectedWithoutMutation(empty, new RetreatSquad());
        var poor = new Simulation(d with { StartingGold = 119 });
        RejectedWithoutMutation(poor, new PlaceGarrison(0));
        var far = new Simulation(d with { Garrison = d.Garrison! with { CommandRadius = 40 } });
        RejectedWithoutMutation(far, new PlaceGarrison(2));

        var run = new Simulation(d);
        RejectedWithoutMutation(run, new PlaceGarrison(-1));
        RejectedWithoutMutation(run, new PlaceGarrison(3));
        Assert.True(run.Apply(new PlaceTower(1)).Success);
        RejectedWithoutMutation(run, new PlaceGarrison(1));
        var built = run.Apply(new PlaceGarrison(0));
        Assert.True(built.Success);
        Assert.Equal(3, run.Garrison!.Guards.Count);
        Assert.Equal(870, run.Gold);
        Assert.Equal(130, run.GoldSpent);
        Assert.Equal(5, run.Garrison.Guards.Select(g => g.Id).Append(built.EntityId).Append(run.Towers[0].Id).Distinct().Count());
        RejectedWithoutMutation(run, new PlaceGarrison(2));
        RejectedWithoutMutation(run, new PlaceTower(0));
        RejectedWithoutMutation(run, new SellTower(built.EntityId));
        foreach (var point in new[] { new Point(double.NaN, 48), new(double.PositiveInfinity, 48), new(80, 49), new(304, 48), new(-1, 48) })
            RejectedWithoutMutation(run, new RallySquad(point));
        var atBoundary = run.Route.At(run.Garrison.EntryProgress + 208);
        Assert.True(run.Apply(new RallySquad(atBoundary)).Success);
        RejectedWithoutMutation(run, new RallySquad(atBoundary));
    }

    [Fact]
    public void Command_reach_uses_road_distance_and_guards_travel_around_corners()
    {
        var d = Fixture(1);
        d = d with
        {
            Map = d.Map with { Route = [new(0, 2), new(7, 2), new(7, 0), new(2, 0)], Foundations = [new(2, 3)] },
            Enemies = [d.Enemies[0] with { Speed = .01 }]
        };
        var run = new Simulation(d); run.Apply(new PlaceGarrison(0));
        var garrison = run.Garrison!;
        // This point is physically close, but far away along the winding road.
        Assert.True((run.Route.At(run.Route.Length) - garrison.Position).Length < garrison.Definition.CommandRadius);
        RejectedWithoutMutation(run, new RallySquad(run.Route.At(run.Route.Length)));
        var destination = run.Route.At(256);
        Assert.True(run.Apply(new RallySquad(destination)).Success);
        Assert.All(garrison.Guards, g => Assert.Equal(garrison.Position, g.Position));
        run.Apply(new StartWave());
        Until(run, () => garrison.Guards.All(g => g.Position == destination));
        Assert.All(garrison.Guards, g => Assert.True(g.OnRoad));
        Assert.InRange(run.Tick, 209, 211); // 224 world units at 64 units/second, including the access spur.
        var positions = garrison.Guards.Select(g => g.Position).ToArray();
        Assert.True(run.Apply(new RetreatSquad()).Success);
        Assert.Equal(positions, garrison.Guards.Select(g => g.Position));
        while (garrison.Guards[0].OnRoad)
        {
            run.Step();
            if (garrison.Guards[0].OnRoad) Assert.True(run.Route.TryGetProgress(garrison.Guards[0].Position, out _));
        }
        Until(run, () => garrison.Guards.All(g => g.Position == garrison.Position));
        Assert.InRange(run.Tick, 419, 422);
    }

    [Fact]
    public void Three_guards_hold_three_enemies_excess_pass_and_unsupported_guards_eventually_fall()
    {
        var run = Deploy(); var squad = run.Garrison!;
        Until(run, () => squad.Guards.All(g => g.EngagedEnemyId != 0));
        Assert.Equal(3, squad.Guards.Select(g => g.EngagedEnemyId).Distinct().Count());
        Assert.Equal(squad.Guards.Select(g => g.Id), run.Enemies.Take(3).Select(e => e.EngagedGuardId));
        Assert.Equal(0, run.Enemies[3].EngagedGuardId);
        var held = run.Enemies.Take(3).Select(e => e.Progress).ToArray();
        for (var i = 0; i < 15; i++) run.Step();
        Assert.Equal(held, run.Enemies.Take(3).Select(e => e.Progress));
        Assert.True(run.Enemies[3].Progress > squad.RallyProgress);
        Until(run, () => squad.Guards.All(g => !g.Alive));
        Assert.All(run.Enemies, e => Assert.Equal(0, e.EngagedGuardId));
        Assert.All(squad.Guards, g => Assert.Equal(0, g.EngagedEnemyId));
        Assert.Equal(3, squad.Casualties);
        Until(run, () => run.Phase != RunPhase.Wave);
        Assert.Equal(RunPhase.Victory, run.Phase); Assert.Equal(4, run.Leaks); Assert.Equal(0, run.Kills);
        Assert.Equal(0, run.GoldEarned); Assert.True(squad.EngagedTicks > 0);
        Assert.Equal(squad.DamageDealt, run.DamageDealt);
        Assert.Equal(squad.Guards.Sum(g => g.DamageDealt), run.DamageDealt);
        Assert.Equal(3, run.DrainEvents().Count(e => e.Kind == EventKind.GuardFallen));
    }

    [Fact]
    public void Swept_contact_catches_fast_enemies_without_exceeding_guard_capacity()
    {
        var d = Fixture();
        d = d with { Enemies = [d.Enemies[0] with { Speed = 100000 }], Waves = [new("Rush", "", [new("grunt", 4, 1, 40)])] };
        var run = Deploy(d);
        Until(run, () => run.PendingSpawns == 0);
        Assert.Equal(3, run.Enemies.Count); Assert.Equal(1, run.Leaks);
        Assert.All(run.Enemies, e => Assert.Equal(56, e.Progress));
        Assert.Equal(3, run.Garrison!.Guards.Count(g => g.EngagedEnemyId != 0));
    }

    [Fact]
    public void Orders_release_both_sides_preserve_cooldowns_and_cannot_heal_or_teleport()
    {
        var run = Deploy(); var squad = run.Garrison!;
        Until(run, () => squad.Guards.All(g => g.Health < g.Definition.Health));
        var positions = squad.Guards.Select(g => g.Position).ToArray();
        var health = squad.Guards.Select(g => g.Health).ToArray();
        var cooldowns = squad.Guards.Select(g => g.NextAttackTick).ToArray();
        var enemyCooldowns = run.Enemies.Select(e => e.NextMeleeTick).ToArray();
        Assert.True(run.Apply(new RallySquad(run.Route.At(96))).Success);
        Assert.Equal(positions, squad.Guards.Select(g => g.Position));
        Assert.Equal(health, squad.Guards.Select(g => g.Health));
        Assert.Equal(cooldowns, squad.Guards.Select(g => g.NextAttackTick));
        Assert.Equal(enemyCooldowns, run.Enemies.Select(e => e.NextMeleeTick));
        Assert.All(run.Enemies, e => { Assert.Equal(0, e.EngagedGuardId); Assert.Equal(0, e.MeleeAtTick); });
        Assert.All(squad.Guards, g => { Assert.Equal(0, g.EngagedEnemyId); Assert.Equal(0, g.AttackAtTick); });
        run.Step(); Assert.NotEqual(positions[0], squad.Guards[0].Position);
        Assert.True(run.Apply(new RetreatSquad()).Success);
        RejectedWithoutMutation(run, new RetreatSquad());
        Until(run, () => squad.Guards.All(g => g.Position == squad.Position));
        Assert.Equal(health, squad.Guards.Select(g => g.Health));
        Assert.All(squad.Guards, g => Assert.False(g.OnRoad));
    }

    [Fact]
    public void Guards_recover_and_regroup_only_on_entering_planning_with_ids_and_statistics_preserved()
    {
        var d = Fixture(); d = d with { Waves = [d.Waves[0], d.Waves[0]] };
        var run = Deploy(d); var squad = run.Garrison!;
        var ids = squad.Guards.Select(g => g.Id).ToArray();
        Until(run, () => squad.Guards.All(g => !g.Alive));
        for (var i = 0; i < 30; i++) run.Step();
        Assert.All(squad.Guards, g => Assert.False(g.Alive));
        Until(run, () => run.Phase == RunPhase.Planning);
        Assert.Equal(ids, squad.Guards.Select(g => g.Id));
        Assert.Equal(3, squad.Casualties); Assert.True(squad.DamageDealt > 0);
        Assert.All(squad.Guards, g => { Assert.Equal(g.Definition.Health, g.Health); Assert.Equal(squad.Position, g.Position); });
        Assert.Single(run.DrainEvents(), e => e.Kind == EventKind.SquadRecovered);
        run.Apply(new StartWave()); run.Step();
        Assert.All(squad.Guards, g => Assert.NotEqual(squad.Position, g.Position));
    }

    [Fact]
    public void Wounded_survivors_heal_between_waves_without_a_gold_cost()
    {
        var d = Fixture(1);
        d = d with { Enemies = [d.Enemies[0] with { Health = 12 }], Waves = [d.Waves[0], d.Waves[0]] };
        var run = Deploy(d); var guard = run.Garrison!.Guards[0];
        Until(run, () => guard.Health < guard.Definition.Health);
        Assert.Equal(RunPhase.Wave, run.Phase);
        Until(run, () => run.Phase == RunPhase.Planning);
        Assert.Equal(30, guard.Health); Assert.Equal(0, run.Garrison.Casualties);
        Assert.Equal(120, run.GoldSpent); Assert.Equal(887, run.Gold);
    }

    [Fact]
    public void Lethal_melee_credits_capped_damage_and_bounty_once_before_enemy_retaliation()
    {
        var d = Fixture(3); d = d with { Enemies = [d.Enemies[0] with { Health = 3 }] };
        var run = Deploy(d); Until(run, () => run.Phase != RunPhase.Wave);
        Assert.Equal(3, run.Kills); Assert.Equal(0, run.Leaks);
        Assert.Equal(9, run.DamageDealt); Assert.Equal(9, run.Garrison!.DamageDealt);
        Assert.Equal(3, run.Garrison.Kills); Assert.Equal(21, run.GoldEarned);
        Assert.Equal(901, run.Gold); Assert.All(run.Garrison.Guards, g => Assert.Equal(30, g.Health));
        var events = run.DrainEvents(); Assert.Equal(3, events.Count(e => e.Kind == EventKind.Killed));
        var contacts = events.Where(e => e.Kind == EventKind.GuardStruck).ToArray();
        Assert.Equal(3, contacts.Length); Assert.All(contacts, e => Assert.Equal(3, e.Amount));
        Assert.All(contacts, e => Assert.Contains(run.Garrison.Guards, g => g.Id == e.EntityId));
        Assert.DoesNotContain(events, e => e.Kind is EventKind.GuardHit or EventKind.Leaked);
        RejectedWithoutMutation(run, new PlaceGarrison(1));
    }

    [Fact]
    public void Tower_kill_releases_a_duel_before_due_melee_without_double_credit()
    {
        var run = Deploy(Fixture(1));
        Until(run, () => run.Garrison!.Guards[0].EngagedEnemyId != 0);
        var guard = run.Garrison!.Guards[0]; var enemy = run.Enemies[0];
        enemy.Health = 1; guard.AttackAtTick = enemy.MeleeAtTick = run.Tick + 1;
        var health = guard.Health;
        run.Apply(new PlaceTower(1)); run.DrainEvents(); run.Step();
        Assert.Equal(health, guard.Health); Assert.Equal(0, guard.EngagedEnemyId); Assert.Equal(0, enemy.EngagedGuardId);
        Assert.Equal(1, run.Kills); Assert.Equal(1, run.Towers[0].Kills); Assert.Equal(0, run.Garrison.Kills);
        Assert.Equal(7, run.GoldEarned); Assert.Equal(1, run.Towers[0].DamageDealt);
        var events = run.DrainEvents();
        Assert.Single(events, e => e.Kind == EventKind.Killed);
        Assert.DoesNotContain(events, e => e.Kind == EventKind.GuardStruck);
    }

    [Fact]
    public void Basic_melee_respects_armor_and_preserves_chill_and_oil_for_towers()
    {
        var d = Fixture(1);
        var source = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "expedition.json")));
        d = d with { Combat = source.Combat! with { Branches = [] }, Enemies = [d.Enemies[0] with { Armor = 2 }] };
        var run = Deploy(d); Until(run, () => run.Garrison!.Guards[0].EngagedEnemyId != 0);
        var enemy = run.Enemies[0];
        enemy.StatusSet.Apply(d.Combat.Statuses.Single(s => s.Id == "chill"), 2, 999, run.Tick);
        enemy.StatusSet.Apply(d.Combat.Statuses.Single(s => s.Id == "oil"), 1, 999, run.Tick);
        Until(run, () => run.Garrison!.DamageDealt > 0);
        Assert.Equal(2, run.Garrison!.DamageDealt);
        Assert.Equal(2, enemy.Statuses.Single(s => s.Definition.Id == "chill").Count);
        Assert.Equal(1, enemy.Statuses.Single(s => s.Definition.Id == "oil").Count);
        Assert.All(run.Reactions, r => Assert.Equal(0, r.Triggers));
        Assert.Equal(0, run.ReactionDamage);
    }

    [Theory]
    [InlineData(false)] [InlineData(true)]
    public void Moving_boss_warns_before_cleave_and_retreat_can_escape(bool retreat)
    {
        var d = Fixture(1);
        var boss = new EnemyDefinition("boss", 1000, 12, 0, 12,
            Boss: new("grunt", 1, 1, 600, 30, 0, 50, 25), Melee: new(30, 48, 90, 20, true, true));
        d = d with { Enemies = [d.Enemies[0] with { Bounty = 0 }, boss], Waves = [new("Boss", "", [new("boss", 1, 1)])] };
        var run = Deploy(d); var squad = run.Garrison!;
        Until(run, () => run.Enemies.Count > 0 && run.Enemies[0].MeleeAtTick > 0);
        var enemy = run.Enemies[0]; var contact = enemy.MeleeAtTick; var progress = enemy.Progress;
        Assert.Equal(0, enemy.EngagedGuardId); Assert.All(squad.Guards, g => Assert.Equal(30, g.Health));
        Assert.Single(run.DrainEvents(), e => e.Kind == EventKind.EnemyMeleeStarted);
        if (retreat) Assert.True(run.Apply(new RetreatSquad()).Success);
        Until(run, () => run.Tick == contact - 1);
        Assert.All(squad.Guards, g => Assert.Equal(30, g.Health));
        run.Step();
        Assert.True(enemy.Progress > progress);
        Assert.All(squad.Guards, g => Assert.Equal(retreat ? 30 : 0, g.Health));
        Assert.Equal(retreat ? 0 : 3, squad.Casualties);
    }

    [Fact]
    public void Captain_can_be_held_but_defeats_unsupported_guards_and_cannot_stall_forever()
    {
        var d = Fixture(1);
        d = d with { Enemies = [d.Enemies[0] with { Health = 400, Armor = 20, Melee = new(40, 28, 90, 30) }],
            Garrison = d.Garrison! with { Guard = d.Garrison.Guard with { Health = 80 } } };
        var run = Deploy(d);
        Until(run, () => run.Garrison!.Guards[0].Health == 40);
        Assert.NotEqual(0, run.Enemies[0].EngagedGuardId);
        Until(run, () => run.Phase != RunPhase.Wave);
        Assert.Equal(3, run.Garrison!.Casualties); Assert.Equal(1, run.Leaks); Assert.Equal(0, run.Kills);
    }

    [Fact]
    public void Squad_pause_freezes_travel_windups_health_and_engagements()
    {
        var run = Deploy();
        Until(run, () => run.Garrison!.Guards[0].AttackAtTick > 0);
        run.DrainEvents(); run.Apply(new Pause(true)); var digest = run.Checksum();
        var clock = new SimulationClock();
        for (var i = 0; i < 200; i++) { run.Step(); clock.Advance(run, 1d / 60, 4); }
        Assert.Equal(digest, run.Checksum()); Assert.Empty(run.DrainEvents());
        run.Apply(new Pause(false)); run.Step(); Assert.NotEqual(digest, run.Checksum());
    }

    [Fact]
    public void Squad_combat_and_orders_match_reference_queries_tick_by_tick()
    {
        var d = Fixture(12);
        var a = Deploy(d); var b = Deploy(d, true);
        foreach (var run in new[] { a, b }) run.Apply(new PlaceTower(1));
        for (var i = 0; i < 3000 && a.Phase == RunPhase.Wave; i++)
        {
            foreach (var run in new[] { a, b })
            {
                if (i == 80) run.Apply(new RallySquad(run.Route.At(128)));
                if (i == 100) run.Apply(new RetreatSquad());
                if (i == 160) run.Apply(new RallySquad(run.Route.At(96)));
                run.Step();
            }
            Assert.Equal(a.Checksum(), b.Checksum()); Assert.Equal(a.DrainEvents(), b.DrainEvents());
        }
        Assert.Equal(RunPhase.Victory, a.Phase);
        Assert.True(a.Garrison!.DamageDealt > 0);
    }

    [Theory]
    [InlineData(30, 1)] [InlineData(144, 2)] [InlineData(144, 4)]
    public void Render_rate_and_speed_do_not_change_squad_combat(int fps, int speed)
    {
        var a = Deploy(); var b = Deploy();
        Until(a, () => a.Phase != RunPhase.Wave);
        var clock = new SimulationClock(); var budget = 20000;
        while (b.Phase == RunPhase.Wave && budget-- > 0) clock.Advance(b, 1d / fps, speed);
        Assert.Equal(a.Checksum(), b.Checksum()); Assert.Equal(a.DrainEvents(), b.DrainEvents());
    }

    [Fact]
    public void Full_expedition_squad_witness_reproduces_with_healing_boss_relics_and_reference_queries()
    {
        var content = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "expedition.json")));
        Simulation Finish(bool reference = false, bool frames = false)
        {
            var run = new Simulation(content, 42, reference); var clock = new SimulationClock(); var budget = 150000;
            while (run.Phase is RunPhase.Wave or RunPhase.Planning && budget-- > 0)
            {
                if (run.Phase == RunPhase.Planning)
                {
                    foreach (var command in ExpeditionStrategy.PlanningCommands(run, squad: true))
                        Assert.True(run.Apply(command).Success, command.ToString());
                    Assert.True(run.Apply(new StartWave()).Success);
                }
                if (frames) clock.Advance(run, 1d / 144, 4); else run.Step();
                run.DrainEvents();
            }
            Assert.True(budget > 0); Assert.Equal(RunPhase.Victory, run.Phase);
            return run;
        }
        var expected = Finish(); var reference = Finish(reference: true); var frames = Finish(frames: true);
        Assert.Equal(expected.Checksum(), reference.Checksum()); Assert.Equal(expected.Checksum(), frames.Checksum());
        Assert.Equal(12, expected.WaveReports.Count); Assert.True(expected.Garrison!.DamageDealt > 0);
        Assert.True(expected.Garrison.Casualties > 0); Assert.True(expected.Garrison.EngagedTicks > 0);
        Assert.Equal(expected.DamageDealt, expected.Towers.Sum(t => t.DamageDealt) + expected.Garrison.DamageDealt);
        Assert.Equal(expected.Kills, expected.Towers.Sum(t => t.Kills) + expected.Garrison.Kills);
    }

    [Fact]
    public void Invalid_squad_definitions_fail_at_load_and_optional_content_preserves_old_missions()
    {
        var d = Fixture(); var spec = d.Garrison!;
        foreach (var invalid in new[] { spec with { Cost = 0 }, spec with { CommandRadius = double.NaN },
            spec with { Id = "beam" }, spec with { Guard = spec.Guard with { WindupTicks = 30 } },
            spec with { Guard = spec.Guard with { Health = 0 } }, spec with { Guard = spec.Guard with { Reach = 13 } } })
            Assert.Throws<ArgumentException>(() => (d with { Garrison = invalid }).Validate());
        foreach (var invalid in new MeleeDefinition?[] { null, new(1, double.PositiveInfinity, 30, 5), new(0, 12, 30, 5),
            new(1, 12, 30, 30), new(1, 12, 30, 5, Cleave: true) })
            Assert.Throws<ArgumentException>(() => (d with { Enemies = [d.Enemies[0] with { Melee = invalid }] }).Validate());
        var active = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "expedition.json")));
        Assert.Throws<ArgumentException>(() => (active with { Enemies = active.Enemies.Select(e => e.Boss is null ? e : e with { Melee = new(40, 80, 90, 30) }).ToArray() }).Validate());
        foreach (var file in new[] { "foundation.json", "emberwatch.json" })
        {
            var old = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, file)));
            Assert.Null(old.Garrison); Assert.All(old.Enemies, e => Assert.Null(e.Melee));
            RejectedWithoutMutation(new Simulation(old), new PlaceGarrison(0));
        }
    }
}
