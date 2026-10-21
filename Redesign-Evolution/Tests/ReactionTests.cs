using Redesign.Core;
using Redesign.Infrastructure;
using Xunit;

namespace Redesign.Tests;

public class ReactionTests
{
    private static GameDefinition Fixture(int enemies = 1) => new(2, "effects", 5000, 10,
        new("test", "Test", 8, 3, 32, [new(0, 1), new(7, 1)],
            [new(0, 0), new(1, 0), new(2, 0), new(3, 0)]),
        [new("prep", 10, 1, 500, 1, 10000, Attack: AttackKind.Beam, DamageKind: DamageKind.Frost, OnHit: new("chill")),
         new("impact", 10, 1, 500, 1, 10000, Attack: AttackKind.Beam),
         new("fire", 10, 1, 500, 1, 10000, Attack: AttackKind.Beam, DamageKind: DamageKind.Fire)],
        [new("runner", 1000, 6, 10, 1)],
        [new("Test", "", Enumerable.Range(0, enemies).Select(_ => new SpawnGroup("runner", 1, 1)).ToArray())],
        new([new("chill", "Chill", 3, 5, SlowPercentPerStack: 10), new("oil", "Oil", 1, 8),
             new("fracture", "Fracture", 2, 6, ArmorReductionPerStack: 6)],
            [new("shatter", "Shatter", "chill", 2, DamageKind.Impact, ReactionEffect.Burst, 20, 80, DamageKind.Frost, 3),
             new("ignition", "Ignition", "oil", 1, DamageKind.Fire, ReactionEffect.BurningGround, 6, 70, DamageKind.Fire, 4, 6, 2, 2)], []));

    [Fact]
    public void Stacks_keep_independent_expiry_source_and_oldest_first_consumption_at_the_cap()
    {
        var status = Fixture().Combat!.Statuses[0]; var set = new StatusSet();
        set.Apply(status, 2, 11, 0); set.Apply(status, 2, 22, 2);
        Assert.Equal(new[] { 11, 22, 22 }, set[0].Stacks.Select(s => s.SourceId));
        Assert.Equal(new long[] { 5, 7, 7 }, set[0].Stacks.Select(s => s.ExpiresAt));
        Assert.Equal(30, set.SlowPercent);
        set.Expire(5); Assert.Equal(2, set[0].Count);
        Assert.Equal(new[] { 22 }, set.Consume(set[0], 1));
        set.Expire(7); Assert.Empty(set);
    }

    [Fact]
    public void Shatter_consumes_two_stacks_credits_setup_and_trigger_and_obeys_cooldown()
    {
        var sim = new Simulation(Fixture());
        sim.Apply(new PlaceTower(0, "prep")); sim.Apply(new PlaceTower(1, "impact")); sim.Apply(new StartWave());
        sim.Step(); Assert.Equal(0, sim.Reactions[0].Triggers);
        sim.Step(); Assert.Equal(1, sim.Reactions[0].Triggers); Assert.Empty(sim.Enemies[0].Statuses);
        Assert.Equal(20, sim.ReactionDamage); Assert.Equal(1, sim.Towers[0].ReactionsPrepared);
        Assert.Equal(1, sim.Towers[1].ReactionsTriggered);
        sim.Step(); sim.Step(); Assert.Equal(1, sim.Reactions[0].Triggers);
        sim.Step(); Assert.Equal(2, sim.Reactions[0].Triggers);
        Assert.Equal(40, sim.ReactionDamage); Assert.Equal(950, sim.Enemies[0].Health);
        Assert.Equal(1, Assert.Single(sim.Enemies[0].Statuses).Count);
    }

    [Fact]
    public void Reaction_damage_does_not_consume_neighbor_setup_or_trigger_recursive_bursts()
    {
        var d = Fixture(2);
        // Deliberately use Impact reaction damage: it would recursively match Shatter if routed through Strike.
        d = d with { Combat = d.Combat! with { Reactions = [d.Combat.Reactions[0] with { DamageKind = DamageKind.Impact }] } };
        var sim = new Simulation(d); var id = sim.Apply(new PlaceTower(0, "impact")).EntityId;
        sim.Apply(new StartWave()); sim.Step();
        foreach (var enemy in sim.Enemies) enemy.StatusSet.Apply(d.Combat.Statuses[0], 2, id, sim.Tick);
        sim.Step();
        Assert.Equal(1, sim.Reactions[0].Triggers); Assert.Equal(40, sim.ReactionDamage);
        Assert.Equal(2, Assert.Single(sim.Enemies[1].Statuses).Count);
        Assert.Single(sim.DrainEvents(), e => e.Kind == EventKind.Reaction);
    }

    [Fact]
    public void A_lethal_trigger_still_reacts_but_damage_and_rewards_only_count_actual_health()
    {
        var d = Fixture(2); d = d with { Enemies = [d.Enemies[0] with { Health = 2 }] };
        var sim = new Simulation(d); var id = sim.Apply(new PlaceTower(0, "impact")).EntityId;
        sim.Apply(new StartWave()); sim.Step();
        sim.Enemies[0].StatusSet.Apply(d.Combat!.Statuses[0], 2, id, sim.Tick);
        // Chill makes this enemy fall behind; Primed must find it instead of switching to the unprepared leader.
        sim.Apply(new SetTargetPriority(id, TargetPriority.Primed)); sim.Step();
        Assert.Equal(2, sim.Kills); Assert.Equal(20, sim.GoldEarned);
        Assert.Equal(4, sim.DamageDealt); Assert.Equal(2, sim.ReactionDamage);
        Assert.Equal(RunPhase.Victory, sim.Phase);
    }

    [Theory] [InlineData(1)] [InlineData(2)]
    public void Ignition_caps_areas_and_staggered_overlap_cannot_bypass_damage_cadence(int cap)
    {
        var d = Fixture(2);
        d = d with { Combat = d.Combat! with { Reactions = [d.Combat.Reactions[1] with { MaxAreas = cap }] } };
        var sim = new Simulation(d); var id = sim.Apply(new PlaceTower(0, "fire")).EntityId;
        sim.Apply(new SetTargetPriority(id, TargetPriority.Strongest));
        sim.Apply(new StartWave()); sim.Step();
        foreach (var enemy in sim.Enemies) enemy.StatusSet.Apply(d.Combat.Statuses[1], 1, id, sim.Tick);
        sim.Step(); Assert.Equal(12, sim.ReactionDamage);
        var oldest = Assert.Single(sim.BurningAreas).Id;
        sim.Step(); Assert.Equal(2, sim.Reactions[0].Triggers); Assert.Equal(cap, sim.BurningAreas.Count);
        Assert.Equal(12, sim.ReactionDamage);
        if (cap == 1) Assert.DoesNotContain(sim.BurningAreas, a => a.Id == oldest);
        sim.Apply(new Pause(true)); var paused = sim.Checksum();
        for (var i = 0; i < 100; i++) sim.Step();
        Assert.Equal(paused, sim.Checksum());
        sim.Apply(new Pause(false)); sim.Apply(new SellTower(id));
        while (sim.Tick < 9) sim.Step();
        Assert.Empty(sim.BurningAreas); Assert.True(sim.ReactionDamage > 12);
        Assert.Equal(sim.ReactionDamage, sim.Reactions[0].Damage);
    }

    [Fact]
    public void Expiry_occurs_before_movement_and_pause_does_not_age_statuses()
    {
        var d = Fixture(); var sim = new Simulation(d);
        var id = sim.Apply(new PlaceTower(0, "prep")).EntityId;
        sim.Apply(new StartWave()); sim.Step(); sim.Apply(new SellTower(id));
        var enemy = Assert.Single(sim.Enemies); var previous = enemy.Progress;
        sim.Step(); Assert.Equal(.09, enemy.Progress - previous, 10);
        sim.Apply(new Pause(true)); var paused = sim.Checksum();
        for (var i = 0; i < 100; i++) sim.Step();
        Assert.Equal(paused, sim.Checksum());
        sim.Apply(new Pause(false));
        while (sim.Tick < 5) sim.Step();
        previous = enemy.Progress; sim.Step();
        Assert.Empty(enemy.Statuses); Assert.Equal(.1, enemy.Progress - previous, 10);
    }

    [Fact]
    public void Fracture_applies_before_damage_and_armor_recovers_as_each_stack_expires()
    {
        var d = Fixture();
        d = d with { Towers = [d.Towers[1] with { Damage = 20, OnHit = new("fracture") }],
            Enemies = [d.Enemies[0] with { Armor = 9 }] };
        var sim = new Simulation(d); var id = sim.Apply(new PlaceTower(0)).EntityId;
        sim.Apply(new StartWave()); sim.Step(); Assert.Equal(17, sim.DamageDealt);
        sim.Step(); Assert.Equal(37, sim.DamageDealt); Assert.Equal(0, sim.Enemies[0].EffectiveArmor);
        sim.Apply(new SellTower(id));
        while (sim.Tick < 7) sim.Step();
        Assert.Equal(3, sim.Enemies[0].EffectiveArmor);
        sim.Step(); Assert.Equal(9, sim.Enemies[0].EffectiveArmor);
    }

    [Fact]
    public void Slows_use_the_strongest_effect_instead_of_stacking_to_permanent_immobility()
    {
        var set = new StatusSet();
        set.Apply(new("ice", "Ice", 3, 60, 15), 3, 1, 0);
        set.Apply(new("pin", "Pin", 1, 30, 80), 1, 2, 0);
        Assert.Equal(80, set.SlowPercent); set.Expire(30); Assert.Equal(45, set.SlowPercent);
    }

    [Fact]
    public void Echo_survives_sale_pauses_and_resolves_once_without_creating_more_echoes()
    {
        var d = Fixture();
        d = d with { Towers = [new("mortar", 10, 1, 500, 100, 10000, Attack: AttackKind.Shell, Echo: new(2, 10, 70))] };
        var sim = new Simulation(d); var id = sim.Apply(new PlaceTower(0)).EntityId;
        sim.Apply(new StartWave()); sim.Step(); Assert.Single(sim.PendingImpacts);
        sim.Apply(new SellTower(id)); sim.Apply(new Pause(true)); var paused = sim.Checksum();
        for (var i = 0; i < 10; i++) sim.Step();
        Assert.Equal(paused, sim.Checksum());
        sim.Apply(new Pause(false)); sim.Step(); Assert.Equal(1, sim.DamageDealt);
        sim.Step(); Assert.Equal(11, sim.DamageDealt); Assert.Empty(sim.PendingImpacts);
        sim.Step(); Assert.Equal(11, sim.DamageDealt);
    }

    [Fact]
    public void Branch_purchase_is_exclusive_atomic_and_refunds_total_investment()
    {
        var d = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "emberwatch.json"))) with { StartingGold = 1000 };
        var sim = new Simulation(d); var id = sim.Apply(new PlaceTower(1, "boltworks")).EntityId;
        var before = sim.Checksum();
        Assert.False(sim.Apply(new UpgradeTower(id, "sun")).Success);
        Assert.False(sim.Apply(new UpgradeTower(id, "missing")).Success);
        Assert.Equal(before, sim.Checksum());
        Assert.True(sim.Apply(new UpgradeTower(id, "ballista")).Success);
        Assert.Equal(830, sim.Gold); Assert.Equal(170, sim.Towers[0].Cost); Assert.Equal(110 + 60, sim.GoldSpent);
        before = sim.Checksum();
        Assert.False(sim.Apply(new UpgradeTower(id, "pitchbow")).Success);
        Assert.Equal(before, sim.Checksum());
        sim.Apply(new SellTower(id)); Assert.Equal(949, sim.Gold);
        Assert.False(sim.Apply(new UpgradeTower(id, "ballista")).Success);
        var poor = new Simulation(d with { StartingGold = 60 });
        var poorId = poor.Apply(new PlaceTower(0)).EntityId; before = poor.Checksum();
        Assert.False(poor.Apply(new UpgradeTower(poorId, "pitchbow")).Success); Assert.Equal(before, poor.Checksum());
    }

    [Fact]
    public void Upgrade_preserves_cooldown_priority_and_inflight_attack_snapshot()
    {
        var d = Fixture();
        var original = new TowerDefinition("bolt", 10, 1, 500, 100, 60);
        d = d with { Towers = [original], Enemies = [d.Enemies[0] with { Speed = .01 }],
            Combat = d.Combat! with { Branches = [new("bolt", 20, original with { Id = "new", Name = "New", Description = "Strong", Damage = 100, OnHit = new("chill") })] } };
        var sim = new Simulation(d); var id = sim.Apply(new PlaceTower(0)).EntityId;
        sim.Apply(new SetTargetPriority(id, TargetPriority.Support)); sim.Apply(new StartWave()); sim.Step();
        var ready = sim.Towers[0].NextFireTick;
        Assert.True(sim.Apply(new UpgradeTower(id, "new")).Success);
        Assert.Equal(ready, sim.Towers[0].NextFireTick); Assert.Equal(TargetPriority.Support, sim.Towers[0].Priority);
        Assert.Equal(1, Assert.Single(sim.Projectiles).Definition.Damage);
        sim.Apply(new SellTower(id));
        for (var i = 0; i < 40; i++) sim.Step();
        Assert.Equal(1, sim.DamageDealt); Assert.Empty(sim.Enemies[0].Statuses);
    }

    [Fact]
    public void Effect_references_and_ambiguous_reactions_are_rejected_and_catalog_arrays_are_isolated()
    {
        var d = Fixture();
        Assert.Throws<ArgumentException>(() => (d with { Combat = null }).Validate());
        Assert.Contains("onHit", Assert.Throws<ArgumentException>(() =>
            (d with { Towers = [d.Towers[0] with { OnHit = new("typo") }] }).Validate()).Message);
        Assert.Contains("ambiguous", Assert.Throws<ArgumentException>(() =>
            (d with { Combat = d.Combat! with { Reactions = [d.Combat.Reactions[0], d.Combat.Reactions[0] with { Id = "duplicate-condition" }] } }).Validate()).Message);
        Assert.Throws<ArgumentException>(() => (d with { Combat = d.Combat! with { Statuses = [d.Combat.Statuses[0] with { SlowPercentPerStack = 50 }] } }).Validate());
        var sim = new Simulation(d); d.Combat!.Statuses[0] = d.Combat.Statuses[0] with { DurationTicks = 999 };
        sim.Definition.Combat!.Statuses[0] = d.Combat.Statuses[0];
        Assert.Equal(5, sim.Definition.Combat.Statuses[0].DurationTicks);
    }
}
