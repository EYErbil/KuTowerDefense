using Redesign.Core;
using Redesign.Infrastructure;
using Xunit;

namespace Redesign.Tests;

public class ExpeditionTests
{
    private static GameDefinition Content() => ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "expedition.json")));
    private static GameDefinition Fixture(RelicKind kind = RelicKind.BeamFork)
    {
        var source = Content();
        var selected = source.Expedition!.Relics.First(r => r.Kind == kind);
        var offers = new[] { selected }.Concat(source.Expedition.Relics.Where(r => r.Id != selected.Id).Take(2)).ToArray();
        return new(2, "expedition-test", 5000, 10,
            new("road", "Road", 8, 3, 32, [new(0, 1), new(7, 1)], [new(1, 0), new(3, 0), new(5, 0), new(6, 0)]),
            [new("beam", 10, 10, 500, 1, 10000, Attack: AttackKind.Beam, DamageKind: DamageKind.Frost, OnHit: new("chill")),
             new("bolt", 10, 10, 500, 1, 10000, OnHit: new("oil")), new("shell", 10, 20, 500, 200, 10000, 60, AttackKind.Shell)],
            [new("gift", 1, .01, 1, 1), new("grunt", 1000, .01, 0, 1), new("raider", 20, 20, 0, 1)],
            [new("Reward", "", [new("gift", 1, 1)], 30), new("Combat", "", [new("grunt", 1, 1), new("grunt", 1, 1), new("grunt", 1, 1)])],
            source.Combat! with { Branches = [] }, source.Expedition with { RewardWaves = [1], Relics = offers });
    }
    private static Simulation Earn(GameDefinition d, string type = "beam", ulong seed = 42)
    {
        var run = new Simulation(d, seed); Assert.True(run.Apply(new PlaceTower(0, type)).Success);
        run.Apply(new StartWave()); run.Step(); Assert.Equal(RunPhase.Planning, run.Phase);
        return run;
    }

    [Fact]
    public void Earned_choice_is_atomic_blocks_advance_and_cannot_be_claimed_twice()
    {
        var d = Fixture(); var run = Earn(d); var checksum = run.Checksum();
        Assert.Equal(3, run.RewardOffers.Count); Assert.Equal(3, run.RewardOffers.Select(r => r.Id).Distinct().Count());
        Assert.False(run.Apply(new StartWave()).Success); Assert.False(run.Apply(new ChooseRelic("missing")).Success);
        Assert.False(run.Apply(new SetChallenge(true)).Success); Assert.Equal(checksum, run.Checksum());
        var chosen = run.RewardOffers[0].Id; Assert.True(run.Apply(new ChooseRelic(chosen)).Success);
        Assert.Single(run.Relics); Assert.Empty(run.RewardOffers); checksum = run.Checksum();
        Assert.False(run.Apply(new ChooseRelic(chosen)).Success); Assert.Equal(checksum, run.Checksum());
        Assert.True(run.Apply(new StartWave()).Success);
        Assert.Empty(new Simulation(d, 42).Relics);
    }

    [Theory]
    [InlineData(RelicKind.BeamFork, "beam")] [InlineData(RelicKind.BoltFork, "bolt")]
    public void Fork_hits_only_one_neighbor_and_carries_setup_without_recursive_forks(RelicKind kind, string type)
    {
        var run = Earn(Fixture(kind), type); var relic = run.RewardOffers.Single(r => r.Kind == kind);
        run.Apply(new ChooseRelic(relic.Id)); var before = run.DamageDealt;
        run.Apply(new StartWave()); run.Step();
        Assert.Equal(15, run.DamageDealt - before);
        Assert.Equal(new[] { 990, 995, 1000 }, run.Enemies.Select(e => e.Health));
        Assert.Equal(new[] { 1, 1, 0 }, run.Enemies.Select(e => e.Statuses.Sum(s => s.Count)));
    }

    [Fact]
    public void Relics_transform_owned_future_and_upgraded_towers_once_without_changing_cooldowns()
    {
        var d = Fixture(RelicKind.ExtraChill);
        d = d with { Combat = d.Combat! with { Branches = [new("beam", 20, d.Towers[0] with { Id = "rime-plus", Name = "Lens", Description = "A lens" })] } };
        var run = Earn(d); var next = run.Towers[0].NextFireTick;
        run.Apply(new ChooseRelic(run.RewardOffers.Single(r => r.Kind == RelicKind.ExtraChill).Id));
        Assert.Equal(2, run.Towers[0].Definition.OnHit!.Stacks); Assert.Equal(next, run.Towers[0].NextFireTick);
        Assert.True(run.Apply(new UpgradeTower(run.Towers[0].Id, "rime-plus")).Success);
        Assert.Equal(2, run.PreviewUpgrade("rime-plus")!.OnHit!.Stacks);
        Assert.Equal(2, run.Towers[0].Definition.OnHit!.Stacks);
        run.Apply(new PlaceTower(1, "beam")); Assert.Equal(2, run.Towers[1].Definition.OnHit!.Stacks);
        Assert.Equal(1, d.Towers[0].OnHit!.Stacks);
        var before = run.DamageDealt; run.Apply(new StartWave()); run.Step();
        Assert.True(run.DamageDealt > before); Assert.Contains(run.Enemies, e => e.Statuses.Any(s => s.Definition.Id == "chill" && s.Count >= 2));
    }

    [Fact]
    public void Shell_echo_and_burning_ground_rewards_change_the_existing_effects()
    {
        var shell = Earn(Fixture(RelicKind.ShellEcho), "shell");
        shell.Apply(new ChooseRelic(shell.RewardOffers.Single(r => r.Kind == RelicKind.ShellEcho).Id));
        Assert.Equal(10, shell.Towers[0].Definition.Echo!.Damage);
        Assert.Equal(30, shell.Towers[0].Definition.Echo!.DelayTicks);
        var burn = Earn(Fixture(RelicKind.LongBurn));
        var prior = burn.Reactions.Single(r => r.Definition.Effect == ReactionEffect.BurningGround).Definition;
        burn.Apply(new ChooseRelic(burn.RewardOffers.Single(r => r.Kind == RelicKind.LongBurn).Id));
        var actual = burn.Reactions.Single(r => r.Definition.Effect == ReactionEffect.BurningGround).Definition;
        Assert.Equal(prior.Radius + 24, actual.Radius); Assert.Equal(prior.DurationTicks + 60, actual.DurationTicks);
        Assert.Equal(prior.MaxAreas, actual.MaxAreas); Assert.Equal(prior.IntervalTicks, actual.IntervalTicks);
    }

    [Theory]
    [InlineData(RelicKind.WarChest)] [InlineData(RelicKind.ReinforcedGate)]
    public void Recovery_rewards_pay_once_and_reset_for_a_new_expedition(RelicKind kind)
    {
        var d = Fixture(kind); var run = Earn(d); var before = run.Gold;
        var choice = run.RewardOffers.Single(r => r.Kind == kind); run.Apply(new ChooseRelic(choice.Id));
        if (kind == RelicKind.WarChest) { Assert.Equal(before + 90, run.Gold); Assert.Equal(121, run.GoldEarned); }
        else { Assert.Equal(12, run.MaxLives); Assert.Equal(12, run.Lives); }
        var checksum = run.Checksum(); Assert.False(run.Apply(new ChooseRelic(choice.Id)).Success); Assert.Equal(checksum, run.Checksum());
        var fresh = new Simulation(d); Assert.Equal(10, fresh.MaxLives); Assert.Empty(fresh.Relics);
    }

    [Theory]
    [InlineData(true)] [InlineData(false)]
    public void Contract_forecast_matches_spawns_and_bonus_requires_zero_breaches(bool defended)
    {
        var d = Fixture() with { StartingLives = 100, Waves = [new("Overrun", "", [new("gift", 1, 1)], 30), new("Next", "", [new("gift", 1, 1)])] };
        var run = new Simulation(d); if (defended) run.Apply(new PlaceTower(0, "beam"));
        Assert.True(run.Apply(new SetChallenge(true)).Success); var forecastCount = run.ChallengeCount; var bonus = run.ChallengeReward;
        run.Apply(new StartWave()); Assert.Equal(1 + forecastCount, run.PendingSpawns);
        var before = run.Checksum(); Assert.False(run.Apply(new SetChallenge(false)).Success); Assert.Equal(before, run.Checksum());
        var budget = 100000;
        // Fast gift keeps the unarmed witness finite without changing the declared reinforcement schedule.
        if (!defended) run = UnarmedChallenge(d, forecastCount);
        else while (run.Phase == RunPhase.Wave && budget-- > 0) { run.Step(); run.DrainEvents(); }
        Assert.True(budget > 0); var report = Assert.Single(run.WaveReports);
        Assert.Equal(defended, report.ChallengeWon); Assert.Equal(30 + (defended ? bonus : 0), report.ClearGold);
        Assert.Equal(1 + forecastCount, run.Kills + run.Leaks); Assert.False(run.ChallengeEnabled);
    }
    private static Simulation UnarmedChallenge(GameDefinition d, int forecast)
    {
        d = d with { Enemies = d.Enemies.Select(e => e with { Speed = 1000 }).ToArray() };
        var run = new Simulation(d); run.Apply(new SetChallenge(true)); run.Apply(new StartWave());
        for (var i = 0; i < 1000 && run.Phase == RunPhase.Wave; i++) run.Step();
        Assert.Equal(forecast + 1, run.Leaks); return run;
    }

    [Fact]
    public void Reinforcement_forecast_stays_exact_across_reward_and_stage_boundaries()
    {
        var d = Fixture();
        d = d with { StartingLives = 100, Enemies = d.Enemies.Select(e => e with { Speed = 1000 }).ToArray(),
            Waves = Enumerable.Range(1, 7).Select(i => new WaveDefinition("Wave " + i, "", [new("gift", 1, 1)])).ToArray(),
            Expedition = d.Expedition! with { ChallengeGrowthPerStage = 3 } };
        var run = new Simulation(d);
        foreach (var expected in new[] { 2, 2, 5, 5, 5, 8, 8 })
        {
            if (run.RewardOffers.Count > 0) Assert.True(run.Apply(new ChooseRelic(run.RewardOffers[0].Id)).Success);
            Assert.Equal(expected, run.ChallengeCount); Assert.True(run.Apply(new SetChallenge(true)).Success);
            Assert.True(run.Apply(new StartWave()).Success);
            Assert.Equal(expected, run.ChallengeCount); Assert.Equal(expected + 1, run.PendingSpawns);
            for (var i = 0; i < 1000 && run.Phase == RunPhase.Wave; i++) { run.Step(); run.DrainEvents(); }
            Assert.Equal(expected + 1, run.WaveReports.Last().Leaks);
            Assert.False(run.WaveReports.Last().ChallengeWon);
        }
        Assert.Equal(RunPhase.Victory, run.Phase);
    }

    [Fact]
    public void Expedition_references_and_nested_collections_are_validated_and_isolated()
    {
        var d = Fixture(); var run = new Simulation(d); d.Expedition!.RewardWaves[0] = 99;
        run.Definition.Expedition!.Relics[0] = d.Expedition.Relics[0] with { Power = 999 };
        Assert.Equal(1, run.Definition.Expedition.RewardWaves[0]);
        Assert.NotEqual(999, run.Definition.Expedition.Relics[0].Power);
        Assert.Throws<ArgumentException>(() => d.Validate());
        d = Fixture();
        Assert.Throws<ArgumentException>(() => (d with { Expedition = d.Expedition! with { ChallengeEnemyId = "gift" } }).Validate());
        Assert.Throws<ArgumentException>(() => (d with { Expedition = d.Expedition! with { Relics = [d.Expedition.Relics[0], d.Expedition.Relics[0], d.Expedition.Relics[0]] } }).Validate());
        Assert.Throws<ArgumentException>(() => (d with { Waves = [d.Waves[0] with { HealthPercent = 0 }] }).Validate());
    }

    [Theory]
    [InlineData("frost")] [InlineData("fire")] [InlineData("steel")]
    public void Distinct_specialized_builds_finish_while_recording_three_earned_choices(string style)
    {
        foreach (var seed in new ulong[] { 1, 7, 42 })
        {
            var run = Finish(Content(), seed, style);
            Assert.Equal(RunPhase.Victory, run.Phase); Assert.Equal(12, run.WaveReports.Count);
            Assert.Equal(3, run.Relics.Count); Assert.Equal(3, run.Relics.Select(r => r.Id).Distinct().Count());
            Assert.True(run.Gold < 450);
            Assert.True(run.Towers.Count(t => t.DamageDealt > 0) >= 6, "The later encounters must use more than the opening defense.");
            Assert.Equal(run.DamageDealt, run.Towers.Sum(t => t.DamageDealt));
        }
    }
    [Theory]
    [InlineData("none", 2)] [InlineData("hold", 3)] [InlineData("basic", 11)]
    public void Passive_or_unspecialized_controls_do_not_complete_the_expedition(string style, int latestWave)
    {
        var run = Finish(Content(), 42, style);
        Assert.Equal(RunPhase.Defeat, run.Phase); Assert.InRange(run.WaveNumber, 1, latestWave);
    }

    [Fact]
    public void Reward_drafts_are_seeded_unique_and_replay_through_reference_queries_and_frame_rates()
    {
        var expected = Finish(Content(), 42, "frost");
        var frames = Finish(Content(), 42, "frost", fps: 144);
        var scan = Finish(Content(), 42, "frost", reference: true);
        Assert.Equal(expected.Checksum(), frames.Checksum()); Assert.Equal(expected.Checksum(), scan.Checksum());
        var drafts = Enumerable.Range(1, 12).Select(seed =>
        {
            var run = Earn(Fixture(), seed: (ulong)seed);
            return string.Join(",", run.RewardOffers.Select(r => r.Id));
        }).Distinct().Count();
        Assert.True(drafts > 1);
    }

    internal static Simulation Finish(GameDefinition d, ulong seed, string style, int fps = 0, bool reference = false)
    {
        var run = new Simulation(d, seed, reference); var clock = new SimulationClock(); var budget = 150000;
        while (run.Phase is RunPhase.Planning or RunPhase.Wave && budget-- > 0)
        {
            if (run.Phase == RunPhase.Planning)
            {
                foreach (var command in ExpeditionStrategy.PlanningCommands(run, style)) Assert.True(run.Apply(command).Success, command.ToString());
                Assert.True(run.Apply(new StartWave()).Success);
            }
            if (fps == 0) run.Step(); else clock.Advance(run, 1d / fps, 4);
            run.DrainEvents();
        }
        Assert.True(budget > 0); return run;
    }
}
