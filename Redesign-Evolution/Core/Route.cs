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

    /// <summary>Closest authored road point; equal distances prefer earlier route progress.</summary>
    public double NearestProgress(Point position)
    {
        var bestDistance = double.PositiveInfinity;
        var progress = 0d;
        for (var i = 1; i < points.Length; i++)
        {
            var segment = points[i] - points[i - 1];
            var offset = position - points[i - 1];
            var fraction = Math.Clamp((offset.X * segment.X + offset.Y * segment.Y) / segment.LengthSquared, 0, 1);
            var distance = (points[i - 1] + segment * fraction - position).LengthSquared;
            if (distance >= bestDistance) continue;
            bestDistance = distance;
            progress = ends[i - 1] + fraction * (ends[i] - ends[i - 1]);
        }
        return progress;
    }

    public bool TryGetProgress(Point position, out double progress)
    {
        progress = 0;
        if (!double.IsFinite(position.X) || !double.IsFinite(position.Y)) return false;
        progress = NearestProgress(position);
        return (At(progress) - position).LengthSquared <= 1e-12;
    }
}
