using System.Collections.ObjectModel;

namespace Redesign.Core;

public sealed record PlaceGarrison(int Foundation) : GameCommand;
public sealed record RallySquad(Point Position) : GameCommand;
public sealed record RetreatSquad : GameCommand;

public sealed class GuardState
{
    internal GuardState(int id, GuardDefinition definition, Point position, double entryProgress, double accessDistance)
    {
        Id = id; Definition = definition; Position = PreviousPosition = position;
        RoadProgress = entryProgress; AccessRemaining = accessDistance; Health = definition.Health;
    }

    public int Id { get; }
    public GuardDefinition Definition { get; }
    public int Health { get; internal set; }
    public bool Alive => Health > 0;
    public Point Position { get; internal set; }
    public Point PreviousPosition { get; internal set; }
    public bool OnRoad { get; internal set; }
    public double RoadProgress { get; internal set; }
    public double AccessRemaining { get; internal set; }
    public int EngagedEnemyId { get; internal set; }
    public int AttackTargetId { get; internal set; }
    public long AttackAtTick { get; internal set; }
    public long NextAttackTick { get; internal set; }
    public long DamageDealt { get; internal set; }
    public int Kills { get; internal set; }
    public long EngagedTicks { get; internal set; }
}

public sealed class GarrisonState
{
    public const int SquadSize = 3;
    internal GarrisonState(int id, int foundation, Point position, double entryProgress,
        double accessDistance, GarrisonDefinition definition, GuardState[] guards)
    {
        Id = id; Foundation = foundation; Position = position; EntryProgress = RallyProgress = entryProgress;
        AccessDistance = accessDistance; Definition = definition; Guards = Array.AsReadOnly(guards);
    }

    public int Id { get; }
    public int Foundation { get; }
    public Point Position { get; }
    public GarrisonDefinition Definition { get; }
    public double EntryProgress { get; }
    public double AccessDistance { get; }
    public double RallyProgress { get; internal set; }
    public bool Retreating { get; internal set; }
    public ReadOnlyCollection<GuardState> Guards { get; }
    public long DamageDealt { get; internal set; }
    public int Kills { get; internal set; }
    public int Casualties { get; internal set; }
    public long EngagedTicks => Guards.Sum(g => g.EngagedTicks);
}
