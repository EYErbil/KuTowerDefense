using Redesign.Core;
using Redesign.Infrastructure;
using Xunit;

namespace Redesign.Tests;

public class SpatialTests
{
    [Fact]
    public void Grid_matches_reference_for_edges_exact_radii_deaths_and_movement()
    {
        var map = new MapDefinition("grid", "Grid", 64, 32, 32, [], []);
        var random = new Random(1741);
        var enemyDefinition = new EnemyDefinition("runner", 100, 48, 0, 1);
        var enemies = Enumerable.Range(1, 5000).Select(id => new EnemyState(id,
            new(random.NextDouble() * 2048, random.NextDouble() * 1024), enemyDefinition)).ToList();
        enemies.Add(new(5001, new(64, 64), enemyDefinition));
        enemies.Add(new(5002, new(128, 64), enemyDefinition));
        var index = new EnemySpatialIndex(map);
        var actual = new List<EnemyState>();
        for (var pass = 0; pass < 3; pass++)
        {
            index.Rebuild(enemies);
            // A death after indexing must be excluded without requiring another rebuild.
            enemies[pass].Alive = false;
            for (var q = 0; q < 200; q++)
            {
                var center = q == 0 ? new Point(64, 64) : new(random.Next(-100, 2148), random.Next(-100, 1124));
                var radius = q == 0 ? 64 : q == 1 ? 0 : q == 2 ? 100000d : random.Next(1, 250);
                index.Query(center, radius, actual);
                var expected = enemies.Where(e => e.Alive && (e.Position - center).LengthSquared <= radius * radius)
                    .Select(e => e.Id).Order().ToArray();
                Assert.Equal(expected, actual.Select(e => e.Id).Order().ToArray());
                Assert.Equal(actual.Count, actual.Distinct().Count());
            }
            foreach (var enemy in enemies)
                enemy.Position = new(random.NextDouble() * 2048, random.NextDouble() * 1024);
        }
        index.Rebuild([]);
        index.Query(new(0, 0), 100000, actual);
        Assert.Empty(actual);
    }

    [Theory]
    [InlineData(false)] [InlineData(true)]
    public void Spatial_combat_matches_reference_checksums_and_event_order_under_load(bool mixed)
    {
        var d = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "foundation.json")));
        d = d with
        {
            StartingGold = 10000, StartingLives = 10000,
            Enemies = [d.Enemies[0] with { Health = 200, Speed = 90 }],
            Towers = [d.Towers[0] with { Damage = 5, CooldownTicks = 9, SplashRadius = 85 }],
            Waves = [new("Stress", "", [new("runner", 1200, 1)])]
        };
        if (mixed)
        {
            var roster = ContentLoader.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "emberwatch.json")));
            d = d with
            {
                Towers = roster.Towers.Select(t => t with { Damage = 5, CooldownTicks = 9 }).ToArray(),
                Enemies = roster.Enemies.Select(e => e with { Health = 200 }).ToArray(),
                Waves = [new("Mixed stress", "", [new("runner", 1000, 1), new("bulwark", 120, 10), new("warden", 40, 30)])]
            };
        }
        var indexed = new Simulation(d, 43);
        var scanned = new Simulation(d, 43, true);
        foreach (var sim in new[] { indexed, scanned })
        {
            for (var i = 0; i < d.Map.Foundations.Length; i++)
            {
                var id = sim.Apply(new PlaceTower(i, d.Towers[i % d.Towers.Length].Id)).EntityId;
                sim.Apply(new SetTargetPriority(id, (TargetPriority)(i % 3)));
            }
            sim.Apply(new StartWave());
        }
        for (var tick = 0; tick < 2400; tick++)
        {
            indexed.Step(); scanned.Step();
            Assert.Equal(scanned.DrainEvents(), indexed.DrainEvents());
            if (tick % 60 == 0) Assert.Equal(scanned.Checksum(), indexed.Checksum());
        }
        Assert.Equal(scanned.Checksum(), indexed.Checksum());
        Assert.True(indexed.Kills > 0);
    }
}
