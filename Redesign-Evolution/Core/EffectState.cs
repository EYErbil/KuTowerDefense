using System.Collections;
using System.Collections.ObjectModel;

namespace Redesign.Core;

public readonly record struct StatusStack(int SourceId, long ExpiresAt);
public sealed class StatusState
{
    public StatusDefinition Definition { get; }
    internal List<StatusStack> MutableStacks { get; } = [];
    public ReadOnlyCollection<StatusStack> Stacks { get; }
    public int Count => MutableStacks.Count;
    internal StatusState(StatusDefinition definition)
    { Definition = definition; Stacks = MutableStacks.AsReadOnly(); }
}

/// <summary>Independent stack expiry; at capacity, replace the oldest stack. Consumption is oldest-first.</summary>
internal sealed class StatusSet : IReadOnlyList<StatusState>
{
    private readonly List<StatusState> states = [];
    public int Count => states.Count;
    public StatusState this[int index] => states[index];
    public IEnumerator<StatusState> GetEnumerator() => states.GetEnumerator();
    IEnumerator IEnumerable.GetEnumerator() => GetEnumerator();
    public int SlowPercent
    {
        get
        {
            var strongest = 0;
            foreach (var status in states) strongest = Math.Max(strongest, status.Definition.SlowPercentPerStack * status.Count);
            return strongest; // Validated at <=80%; independent slowing effects never add together.
        }
    }
    public int ArmorReduction
    {
        get
        {
            var total = 0;
            foreach (var status in states) total += status.Definition.ArmorReductionPerStack * status.Count;
            return total; // Catalog caps bound the sum well below int.MaxValue.
        }
    }
    public StatusState? Find(string id)
    {
        foreach (var status in states) if (status.Definition.Id == id) return status;
        return null;
    }
    public void Apply(StatusDefinition definition, int count, int source, long tick)
    {
        var status = Find(definition.Id);
        if (status is null) { status = new(definition); states.Add(status); }
        for (var i = 0; i < count; i++)
        {
            if (status.Count == definition.MaxStacks) status.MutableStacks.RemoveAt(0);
            status.MutableStacks.Add(new(source, tick + definition.DurationTicks));
        }
    }
    public void Expire(long tick)
    {
        for (var i = states.Count - 1; i >= 0; i--)
        {
            var stacks = states[i].MutableStacks;
            // This runs for every enemy every tick; avoid a captured predicate even for empty sets.
            for (var stack = stacks.Count - 1; stack >= 0; stack--)
                if (stacks[stack].ExpiresAt <= tick) stacks.RemoveAt(stack);
            if (states[i].Count == 0) states.RemoveAt(i);
        }
    }
    public int[] Consume(StatusState status, int count)
    {
        var sources = status.MutableStacks.Take(count).Select(s => s.SourceId).Distinct().Order().ToArray();
        status.MutableStacks.RemoveRange(0, count);
        if (status.Count == 0) states.Remove(status);
        return sources;
    }
}

public sealed class ReactionStatistics(ReactionDefinition definition)
{
    public ReactionDefinition Definition { get; } = definition;
    public long Triggers { get; internal set; }
    public long Damage { get; internal set; }
}

public sealed class BurningAreaState(int id, Point position, int sourceId, ReactionDefinition definition, long tick)
{
    public int Id { get; } = id;
    public Point Position { get; } = position;
    public int SourceId { get; } = sourceId;
    public ReactionDefinition Definition { get; } = definition;
    public long ExpiresAt { get; } = tick + definition.DurationTicks;
    public long NextPulseTick { get; internal set; } = tick;
}

public sealed record PendingImpactState(int Id, Point Position, int SourceId, EchoDefinition Definition,
    long CreatedAt, long ResolveAt);
