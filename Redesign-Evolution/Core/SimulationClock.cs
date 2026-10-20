namespace Redesign.Core;

/// <summary>Bounded work per rendered frame; carries debt instead of changing simulation dt.</summary>
public sealed class SimulationClock
{
    private double accumulatedTicks;
    public double Interpolation => Math.Clamp(accumulatedTicks, 0, 1);
    public int Advance(Simulation simulation, double elapsedSeconds, int speed)
    {
        if (!double.IsFinite(elapsedSeconds) || elapsedSeconds < 0) throw new ArgumentOutOfRangeException(nameof(elapsedSeconds));
        if (speed is not (1 or 2 or 4)) throw new ArgumentOutOfRangeException(nameof(speed));
        if (simulation.IsPaused || simulation.Phase != RunPhase.Wave) { accumulatedTicks = 0; return 0; }
        accumulatedTicks += elapsedSeconds * Simulation.TicksPerSecond * speed;
        var count = 0;
        while (accumulatedTicks + 1e-9 >= 1 && count < 32 && simulation.Phase == RunPhase.Wave && !simulation.IsPaused)
        { simulation.Step(); accumulatedTicks -= 1; count++; }
        // Unused time belongs to the completed wave, not to a wave started by a later input.
        if (simulation.Phase != RunPhase.Wave || simulation.IsPaused) accumulatedTicks = 0;
        if (accumulatedTicks < 0) accumulatedTicks = 0;
        return count;
    }
}
