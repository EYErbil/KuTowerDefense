namespace Redesign.Core;

/// <summary>
/// Bounded broad phase rebuilt after movement. Buckets retain capacity across ticks.
/// Queries include the radius boundary and filter deaths occurring after the rebuild.
/// Callers must define resolution order; bucket order is not gameplay order.
/// </summary>
internal sealed class EnemySpatialIndex
{
    private readonly List<EnemyState>[] buckets;
    private readonly int columns, rows, cellSize;

    public EnemySpatialIndex(MapDefinition map)
    {
        cellSize = map.TileSize * 2;
        columns = (map.Width * map.TileSize + cellSize - 1) / cellSize;
        rows = (map.Height * map.TileSize + cellSize - 1) / cellSize;
        buckets = Enumerable.Range(0, columns * rows).Select(_ => new List<EnemyState>()).ToArray();
    }

    public void Rebuild(IReadOnlyList<EnemyState> enemies)
    {
        foreach (var bucket in buckets) bucket.Clear();
        foreach (var enemy in enemies)
        {
            if (!enemy.Alive) continue;
            var x = Coordinate(enemy.Position.X, columns);
            var y = Coordinate(enemy.Position.Y, rows);
            buckets[y * columns + x].Add(enemy);
        }
    }

    public void Query(Point center, double radius, List<EnemyState> result)
    {
        result.Clear();
        var minX = Coordinate(center.X - radius, columns);
        var maxX = Coordinate(center.X + radius, columns);
        var minY = Coordinate(center.Y - radius, rows);
        var maxY = Coordinate(center.Y + radius, rows);
        var squared = radius * radius;
        for (var y = minY; y <= maxY; y++)
            for (var x = minX; x <= maxX; x++)
                foreach (var enemy in buckets[y * columns + x])
                    if (enemy.Alive && (enemy.Position - center).LengthSquared <= squared)
                        result.Add(enemy);
    }

    private int Coordinate(double value, int count) =>
        (int)Math.Clamp(Math.Floor(value / cellSize), 0, count - 1);
}
