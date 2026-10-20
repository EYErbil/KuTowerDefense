namespace Redesign.Core;

/// <summary>Immutable authored route; rendering never calculates or mutates navigation.</summary>
public sealed class Route
{
    private readonly Point[] points;
    private readonly double[] ends;
    public double Length => ends[^1];

    public Route(MapDefinition map)
    {
        points = map.Route.Select(map.Center).ToArray();
        ends = new double[points.Length];
        for (var i = 1; i < points.Length; i++) ends[i] = ends[i - 1] + (points[i] - points[i - 1]).Length;
    }

    public Point At(double distance)
    {
        if (distance <= 0) return points[0];
        if (distance >= Length) return points[^1];
        var i = Array.BinarySearch(ends, distance);
        if (i >= 0) return points[i];
        i = ~i;
        return points[i - 1] + (points[i] - points[i - 1]) * ((distance - ends[i - 1]) / (ends[i] - ends[i - 1]));
    }
}
