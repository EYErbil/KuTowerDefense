namespace Redesign.Core;

public readonly record struct Cell(int X, int Y);

public readonly record struct Point(double X, double Y)
{
    public static Point operator +(Point a, Point b) => new(a.X + b.X, a.Y + b.Y);
    public static Point operator -(Point a, Point b) => new(a.X - b.X, a.Y - b.Y);
    public static Point operator *(Point a, double scale) => new(a.X * scale, a.Y * scale);
    public double LengthSquared => X * X + Y * Y;
    public double Length => Math.Sqrt(LengthSquared);
}

public enum AttackKind { Bolt, Beam, Shell }
public enum DamageKind { Impact, Energy, Frost, Fire }
public enum TargetPriority { First, Strongest, Support, Primed }

public sealed record TowerDefinition(string Id, int Cost, int Damage, double Range,
    int CooldownTicks, double ProjectileSpeed, double SplashRadius = 0,
    AttackKind Attack = AttackKind.Bolt, DamageKind DamageKind = DamageKind.Impact,
    string Name = "", string Description = "", StatusApplication? OnHit = null, EchoDefinition? Echo = null,
    double ForkRadius = 0, int ForkDamagePercent = 50);
public sealed record EnemyDefinition(string Id, int Health, double Speed, int Bounty, int LeakDamage,
    int Armor = 0, double ShieldRadius = 0, int ShieldPercent = 0, string Name = "",
    HealingDefinition? Healing = null, BossDefinition? Boss = null, int SlowResistancePercent = 0,
    MeleeDefinition? Melee = null);
public sealed record SpawnGroup(string EnemyId, int Count, int IntervalTicks, int DelayTicks = 0);
public sealed record WaveDefinition(string Title, string Briefing, SpawnGroup[] Groups, int ClearGold = 0, int HealthPercent = 100);
public sealed record MapDefinition(string Id, string Title, int Width, int Height, int TileSize,
    Cell[] Route, Cell[] Foundations)
{
    public Point Center(Cell cell) => new((cell.X + .5) * TileSize, (cell.Y + .5) * TileSize);
}

public sealed record GameDefinition(int FormatVersion, string Id, int StartingGold, int StartingLives,
    MapDefinition Map, TowerDefinition[] Towers, EnemyDefinition[] Enemies, WaveDefinition[] Waves,
    CombatDefinition? Combat = null, ExpeditionDefinition? Expedition = null, GarrisonDefinition? Garrison = null)
{
    public GameDefinition Copy() => this with
    {
        Map = Map with { Route = [.. Map.Route], Foundations = [.. Map.Foundations] },
        Towers = [.. Towers], Enemies = [.. Enemies],
        Waves = Waves.Select(w => w with { Groups = [.. w.Groups] }).ToArray(),
        Combat = Combat?.Copy(), Expedition = Expedition?.Copy()
    };

    public void Validate()
    {
        void Require(bool valid, string field) { if (!valid) throw new ArgumentException($"Invalid content: {field}"); }
        static bool Positive(double value) => double.IsFinite(value) && value > 0;
        Require(FormatVersion == 2, "formatVersion must be 2");
        Require(!string.IsNullOrWhiteSpace(Id), "id");
        Require(StartingGold >= 0 && StartingLives > 0, "starting resources");
        Require(Map is not null && Towers is { Length: > 0 and <= 64 } &&
            Enemies is { Length: > 0 and <= 128 } && Waves is not null, "required map/towers/enemies/waves definitions");
        if (Map is null || Towers is null || Enemies is null || Waves is null) return;
        Require(!string.IsNullOrWhiteSpace(Map.Id) && !string.IsNullOrWhiteSpace(Map.Title), "map identity");
        Require(Map.Width is > 1 and <= 128 && Map.Height is > 1 and <= 128 && Map.TileSize is >= 8 and <= 256, "map dimensions");
        Require(Map.Route is { Length: >= 2 } && Map.Foundations is { Length: > 0 }, "route/foundations");
        if (Map.Route is null || Map.Foundations is null) return;
        bool Inside(Cell c) => c.X >= 0 && c.Y >= 0 && c.X < Map.Width && c.Y < Map.Height;
        Require(Map.Route.All(Inside) && Map.Foundations.All(Inside), "map cell bounds");
        var pathCells = new HashSet<Cell>();
        pathCells.Add(Map.Route[0]);
        for (var i = 1; i < Map.Route.Length; i++)
        {
            var a = Map.Route[i - 1]; var b = Map.Route[i];
            Require(a != b && (a.X == b.X || a.Y == b.Y), "route must have nonzero orthogonal segments");
            var dx = Math.Sign(b.X - a.X); var dy = Math.Sign(b.Y - a.Y);
            for (var c = new Cell(a.X + dx, a.Y + dy); ; c = new Cell(c.X + dx, c.Y + dy))
            {
                Require(pathCells.Add(c), "route must not overlap itself");
                if (c == b) break;
            }
        }
        Require(Map.Foundations.Distinct().Count() == Map.Foundations.Length, "duplicate foundations");
        Require(Map.Foundations.All(c => !pathCells.Contains(c)), "foundation overlaps route");
        foreach (var tower in Towers)
        {
            Require(tower is not null, "null tower");
            if (tower is null) return;
            Require(!string.IsNullOrWhiteSpace(tower.Id) && tower.Cost > 0 && tower.Damage > 0 &&
                tower.CooldownTicks > 0 && Positive(tower.Range) && Positive(tower.ProjectileSpeed) &&
                double.IsFinite(tower.SplashRadius) && tower.SplashRadius >= 0 &&
                Enum.IsDefined(tower.Attack) && Enum.IsDefined(tower.DamageKind), $"tower '{tower.Id}' values");
            Require(tower.Attack != AttackKind.Beam || tower.SplashRadius == 0, $"tower '{tower.Id}': beams cannot splash");
        }
        Require(Towers.Select(t => t.Id).Distinct(StringComparer.Ordinal).Count() == Towers.Length, "duplicate tower id");
        CombatContent.Validate(this);
        foreach (var enemy in Enemies)
        {
            Require(enemy is not null, "null enemy");
            if (enemy is null) return;
            Require(!string.IsNullOrWhiteSpace(enemy.Id) && enemy.Health > 0 && Positive(enemy.Speed) &&
                enemy.Bounty >= 0 && enemy.LeakDamage > 0 && enemy.Armor >= 0 &&
                double.IsFinite(enemy.ShieldRadius) && enemy.ShieldRadius >= 0 &&
                enemy.ShieldPercent is >= 0 and <= 80 &&
                (enemy.ShieldRadius > 0) == (enemy.ShieldPercent > 0), $"enemy '{enemy.Id}' values");
        }
        var enemyIds = Enemies.Select(e => e.Id).ToHashSet(StringComparer.Ordinal);
        Require(enemyIds.Count == Enemies.Length, "duplicate enemy id");
        Require(Waves.Length is > 0 and <= 1000, "waves");
        for (var i = 0; i < Waves.Length; i++)
        {
            var wave = Waves[i];
            Require(wave is not null && !string.IsNullOrWhiteSpace(wave.Title) &&
                wave.Groups is { Length: > 0 and <= 32 }, $"wave {i + 1}: title/groups");
            if (wave is null || wave.Groups is null) return;
            foreach (var group in wave.Groups)
            {
                Require(group is not null, $"wave {i + 1}: null spawn group");
                if (group is null) return;
                Require(enemyIds.Contains(group.EnemyId), $"wave {i + 1}: unknown enemy '{group.EnemyId}'");
                Require(group.Count is > 0 and <= 10000 && group.IntervalTicks is > 0 and <= 36000 &&
                    group.DelayTicks is >= 0 and <= 216000, $"wave {i + 1}: spawn count/timing");
            }
            Require(wave.Groups.Sum(g => g.Count) <= 10000, $"wave {i + 1}: maximum 10000 spawns");
            Require(wave.ClearGold is >= 0 and <= 10000, $"wave {i + 1}: clear reward");
            Require(wave.HealthPercent is >= 25 and <= 300, $"wave {i + 1}: health percentage");
        }
        ExpeditionContent.Validate(this);
        SquadContent.Validate(this);
    }
}
