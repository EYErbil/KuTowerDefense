namespace Redesign.Core;

public sealed partial class Simulation
{
    private EnemyDefinition WaveEnemy(EnemyDefinition enemy)
    {
        var percent = definition.Waves[WaveNumber - 1].HealthPercent;
        return percent == 100 ? enemy : enemy with { Health = Math.Max(1, (int)Math.Min(int.MaxValue, (long)enemy.Health * percent / 100)) };
    }
    private readonly List<EnemyState> healingCandidates = [];
    private readonly HashSet<int> healedThisTick = [];

    private void PrepareAbility(EnemyState enemy)
    {
        var windup = enemy.Definition.Healing?.WindupTicks ?? enemy.Definition.Boss?.WindupTicks ?? 0;
        enemy.WindingUp = windup > 0 && Tick >= enemy.NextAbilityTick - windup;
        if (enemy.Definition.Boss is not { } boss) return;
        if (!enemy.Enraged && (long)enemy.Health * 100 <= (long)enemy.Definition.Health * boss.EnrageHealthPercent)
        {
            enemy.Enraged = true;
            // The phase transition never skips the advertised warning window.
            enemy.NextAbilityTick = Math.Min(enemy.NextAbilityTick, Tick + boss.WindupTicks + 1);
            Emit(EventKind.BossEnraged, enemy.Position, enemy.Id);
        }
        enemy.WindingUp = enemy.SummonsCreated < boss.MaxSummons && Tick >= enemy.NextAbilityTick - boss.WindupTicks;
    }

    private void ResolveEnemyAbilities()
    {
        healedThisTick.Clear();
        // Heal before adding summons so grid and reference scans see the same population.
        foreach (var healer in enemies)
        {
            if (!healer.Alive || healer.Definition.Healing is not { } heal || Tick < healer.NextAbilityTick) continue;
            healer.NextAbilityTick = Tick + heal.IntervalTicks; healer.WindingUp = false;
            QueryEnemies(healer.Position, heal.Radius, healingCandidates);
            healingCandidates.Sort(static (a, b) => a.Id.CompareTo(b.Id));
            foreach (var target in healingCandidates)
            {
                // Cannot resurrect or heal itself/other healers. Simultaneous pulses do not multiply healing.
                if (target.Definition.Healing is not null || !healedThisTick.Add(target.Id)) continue;
                var restored = Math.Min(heal.Amount, target.Definition.Health - target.Health);
                if (restored <= 0) continue;
                target.Health += restored;
                Emit(EventKind.Healed, target.Position, healer.Id, restored);
            }
        }
        var countBeforeSummons = enemies.Count;
        for (var i = 0; i < countBeforeSummons; i++)
        {
            var leader = enemies[i];
            if (!leader.Alive || leader.Definition.Boss is not { } boss || leader.SummonsCreated >= boss.MaxSummons || Tick < leader.NextAbilityTick) continue;
            var count = Math.Min(boss.SummonsPerPulse, boss.MaxSummons - leader.SummonsCreated);
            for (var j = 0; j < count; j++)
            {
                var progress = Math.Max(0, leader.Progress - 24 * (j + 1));
                var minion = new EnemyState(nextId++, Route.At(progress), WaveEnemy(enemyDefinitions[boss.SummonEnemyId]), Tick) { Progress = progress };
                enemies.Add(minion); enemyById.Add(minion.Id, minion);
            }
            leader.SummonsCreated += count;
            leader.NextAbilityTick = Tick + (leader.Enraged ? boss.IntervalTicks / 2 : boss.IntervalTicks);
            leader.WindingUp = false;
            Emit(EventKind.Rally, leader.Position, leader.Id, count);
        }
    }
}
