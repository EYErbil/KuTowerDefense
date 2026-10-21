namespace Redesign.Core;

public enum RunPhase { Planning, Wave, Victory, Defeat }
public enum EventKind { Built, Sold, Fired, Hit, Killed, Leaked, WaveStarted, WaveCleared, Victory, Defeat, Blast, Upgraded, Reaction }
public readonly record struct GameEvent(long Tick, EventKind Kind, Point Position, int EntityId = 0,
    long Amount = 0, AttackKind Attack = AttackKind.Bolt, Point? Destination = null,
    DamageKind DamageKind = DamageKind.Impact, string? EffectId = null);
public readonly record struct CommandResult(bool Success, string Message, int EntityId = 0);
public abstract record GameCommand;
public sealed record PlaceTower(int Foundation, string? TowerId = null) : GameCommand;
public sealed record SellTower(int TowerId) : GameCommand;
public sealed record SetTargetPriority(int TowerId, TargetPriority Priority) : GameCommand;
public sealed record UpgradeTower(int TowerId, string BranchId) : GameCommand;
public sealed record StartWave : GameCommand;
public sealed record Pause(bool Paused) : GameCommand;

public sealed class TowerState(int id, int foundation, Point position, TowerDefinition definition)
{
    public int Id { get; } = id;
    public int Foundation { get; } = foundation;
    public Point Position { get; } = position;
    public string FamilyId { get; } = definition.Id;
    public string? BranchId { get; internal set; }
    public TowerDefinition Definition { get; internal set; } = definition;
    public long Cost { get; internal set; } = definition.Cost;
    public long SellValue => Cost * 70 / 100;
    public long DamageDealt { get; internal set; }
    public int Kills { get; internal set; }
    public int TargetId { get; internal set; }
    public long NextFireTick { get; internal set; }
    public bool Sold { get; internal set; }
    public TargetPriority Priority { get; internal set; }
    public long ReactionsTriggered { get; internal set; }
    public long ReactionsPrepared { get; internal set; }
    public long ReactionDamage { get; internal set; }
}

public sealed class EnemyState(int id, Point position, EnemyDefinition definition)
{
    public EnemyDefinition Definition { get; } = definition;
    public int Id { get; } = id;
    public Point Position { get; internal set; } = position;
    public Point PreviousPosition { get; internal set; } = position;
    public double Progress { get; internal set; }
    public int Health { get; internal set; } = definition.Health;
    public bool Alive { get; internal set; } = true;
    internal StatusSet StatusSet { get; } = new();
    public IReadOnlyList<StatusState> Statuses => StatusSet;
    internal Dictionary<string, long> ReactionReady { get; } = new(StringComparer.Ordinal);
    internal Dictionary<string, long> AreaDamageReady { get; } = new(StringComparer.Ordinal);
    public int SlowPercent => StatusSet.SlowPercent;
    public int EffectiveArmor => Math.Max(0, Definition.Armor - StatusSet.ArmorReduction);
    internal List<EnemyState> Protectors { get; } = [];
    // Protection stops immediately when an escort dies; overlapping auras never stack.
    public int ShieldPercent
    {
        get
        {
            var strongest = 0;
            foreach (var escort in Protectors)
                if (escort.Alive) strongest = Math.Max(strongest, escort.Definition.ShieldPercent);
            return strongest;
        }
    }
}

public sealed class ProjectileState(int id, int sourceId, int targetId, Point position, Point aimPosition, TowerDefinition definition)
{
    public TowerDefinition Definition { get; } = definition;
    public int Id { get; } = id;
    public int SourceId { get; } = sourceId;
    public int TargetId { get; } = targetId;
    public Point Position { get; internal set; } = position;
    public Point PreviousPosition { get; internal set; } = position;
    public Point AimPosition { get; internal set; } = aimPosition;
    public bool Active { get; internal set; } = true;
}
