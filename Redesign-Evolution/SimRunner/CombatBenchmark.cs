using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Text.Json;
using Redesign.Core;

internal static class CombatBenchmark
{
    public static int Run()
    {
        var route = new List<Cell>();
        for (var y = 2; y <= 30; y += 4)
        {
            var reverse = (y - 2) / 4 % 2 != 0;
            route.Add(new(reverse ? 63 : 0, y));
            route.Add(new(reverse ? 0 : 63, y));
        }
        var foundations = (from y in new[] { 4, 8, 12, 16 }
                           from x in Enumerable.Range(0, 16)
                           select new Cell(x * 4 + 1, y)).ToArray();
        var definition = new GameDefinition(2, "spatial-stress", 100000, 10000,
            new("stress", "Stress", 64, 32, 32, [.. route], foundations),
            [new("bolt", 1, 1, 190, 30, 480, 60)],
            [new("runner", 100000, 120, 0, 1)], [new("Stress", "", [new("runner", 2500, 1)])]);
        var reports = new List<object>();
        string? expected = null;
        // Alternate order to reduce systematic warm-up/order bias. Timing is diagnostic, never a test gate.
        for (var round = 0; round < 3; round++)
            foreach (var reference in round % 2 == 0 ? new[] { true, false } : new[] { false, true })
            {
                var sim = new Simulation(definition, 42, reference);
                for (var i = 0; i < foundations.Length; i++) sim.Apply(new PlaceTower(i));
                sim.Apply(new StartWave());
                for (var i = 0; i < 1000; i++) { sim.Step(); sim.DrainEvents(); }
                var samples = new double[1500];
                var allocated = GC.GetAllocatedBytesForCurrentThread();
                for (var i = 0; i < samples.Length; i++)
                {
                    var start = Stopwatch.GetTimestamp();
                    sim.Step(); sim.DrainEvents();
                    samples[i] = Stopwatch.GetElapsedTime(start).TotalMilliseconds;
                }
                allocated = GC.GetAllocatedBytesForCurrentThread() - allocated;
                var checksum = sim.Checksum();
                expected ??= checksum;
                if (checksum != expected) throw new InvalidOperationException("Benchmark reference mismatch.");
                Array.Sort(samples);
                reports.Add(new { Round = round + 1, Mode = reference ? "scan" : "grid", sim.Tick,
                    Enemies = sim.Enemies.Count, Towers = sim.Towers.Count, Projectiles = sim.Projectiles.Count,
                    MeanMs = samples.Average(), P50Ms = samples[750], P95Ms = samples[1424],
                    AllocatedBytesPerTick = allocated / samples.Length, Checksum = checksum });
            }
        Console.WriteLine(JsonSerializer.Serialize(new
        {
            Runtime = RuntimeInformation.FrameworkDescription, OS = RuntimeInformation.OSDescription,
            Cpu = Environment.GetEnvironmentVariable("PROCESSOR_IDENTIFIER"), Configuration = "Debug",
            WarmupTicks = 1000, MeasuredTicks = 1500, Reports = reports
        }, new JsonSerializerOptions { WriteIndented = true }));
        return 0;
    }
}
