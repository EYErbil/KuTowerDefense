using System.Collections.ObjectModel;
using System.Security.Cryptography;
using System.Text;

namespace Redesign.Core;

/// <summary>Authoritative single-threaded simulation. All gameplay timers are integer ticks.</summary>
public sealed partial class Simulation
{
    public const int TicksPerSecond = 60;
    private readonly List<TowerState> towers = [];
    private readonly List<EnemyState> enemies = [];
    private readonly List<ProjectileState> projectiles = [];
    private readonly Dictionary<int, TowerState> towerHistory = [];
    private readonly Dictionary<int, EnemyState> enemyById = [];
    private readonly List<GameEvent> events = [];
    private readonly GameDefinition definition;
    private readonly EnemySpatialIndex spatial;
    private readonly bool referenceQueries;
    private readonly List<EnemyState> candidates = [];
    private readonly Dictionary<string, TowerDefinition> towerDefinitions;
    private readonly Dictionary<string, EnemyDefinition> enemyDefinitions;
    private readonly Dictionary<string, BranchDefinition> branches;
    private int nextId = 1;
    private long waveStartTick;
    private int nextSpawn;
    private ScheduledSpawn[] spawnPlan = [];
    private readonly record struct ScheduledSpawn(long Offset, int Group, int Ordinal, EnemyDefinition Enemy);

    public GameDefinition Definition => definition.Copy();
    public Route Route { get; }
    public ReadOnlyCollection<TowerState> Towers { get; }
    public ReadOnlyCollection<EnemyState> Enemies { get; }
    public ReadOnlyCollection<ProjectileState> Projectiles { get; }
    public RunPhase Phase { get; private set; } = RunPhase.Planning;
    public bool IsPaused { get; private set; }
    public long Tick { get; private set; }
    public ulong Seed { get; }
    public long Gold { get; private set; }
    public int Lives { get; private set; }
    public int WaveNumber { get; private set; }
    public int PendingSpawns { get; private set; }
    public int Kills { get; private set; }
    public int Leaks { get; private set; }
    public long DamageDealt { get; private set; }
    public long GoldEarned { get; private set; }
    public long GoldSpent { get; private set; }

    public Simulation(GameDefinition definition, ulong seed = 1) : this(definition, seed, false) { }

    // Reference scans are accessible only to tests and the profiling executable.
    internal Simulation(GameDefinition definition, ulong seed, bool referenceQueries)
    {
        definition.Validate();
        this.definition = definition.Copy();
        towerDefinitions = this.definition.Towers.ToDictionary(t => t.Id, StringComparer.Ordinal);
        enemyDefinitions = this.definition.Enemies.ToDictionary(e => e.Id, StringComparer.Ordinal);
        branches = (this.definition.Combat?.Branches ?? []).ToDictionary(b => b.Upgrade.Id, StringComparer.Ordinal);
        InitializeEffects();
        InitializeExpedition();
        Seed = seed;
        this.referenceQueries = referenceQueries;
        spatial = new EnemySpatialIndex(this.definition.Map);
        Route = new Route(this.definition.Map);
        Gold = definition.StartingGold; Lives = definition.StartingLives;
        Towers = towers.AsReadOnly(); Enemies = enemies.AsReadOnly(); Projectiles = projectiles.AsReadOnly();
    }

    public CommandResult Apply(GameCommand command)
    {
        if (Phase is RunPhase.Victory or RunPhase.Defeat) return new(false, "The run has ended. Start a new defense.");
        switch (command)
        {
            case PlaceTower placement:
                if (placement.Foundation < 0 || placement.Foundation >= definition.Map.Foundations.Length)
                    return new(false, "Choose a marked foundation.");
                if (towers.Any(t => t.Foundation == placement.Foundation)) return new(false, "This foundation is occupied.");
                if (!towerDefinitions.TryGetValue(placement.TowerId ?? definition.Towers[0].Id, out var spec))
                    return new(false, "Unknown tower type.");
                if (Gold < spec.Cost) return new(false, "Not enough gold.");
                var tower = new TowerState(nextId++, placement.Foundation, definition.Map.Center(definition.Map.Foundations[placement.Foundation]), ApplyRelics(spec));
                Gold -= tower.Cost; GoldSpent += tower.Cost;
                towers.Add(tower); towerHistory.Add(tower.Id, tower);
                Emit(EventKind.Built, tower.Position, tower.Id, tower.Cost);
                return new(true, $"{(string.IsNullOrWhiteSpace(spec.Name) ? spec.Id : spec.Name)} ready.", tower.Id);
            case SellTower sell:
                if (!towerHistory.TryGetValue(sell.TowerId, out var sold) || sold.Sold) return new(false, "Tower no longer available.");
                sold.Sold = true; towers.Remove(sold);
                var refund = sold.SellValue;
                Gold += refund; Emit(EventKind.Sold, sold.Position, sold.Id, refund);
                return new(true, $"Recovered {refund} gold.");
            case SetTargetPriority targeting:
                if (!towerHistory.TryGetValue(targeting.TowerId, out var selected) || selected.Sold ||
                    !Enum.IsDefined(targeting.Priority)) return new(false, "Choose an active tower and valid priority.");
                selected.Priority = targeting.Priority;
                return new(true, $"Target priority: {targeting.Priority}.");
            case UpgradeTower upgrade:
                if (!towerHistory.TryGetValue(upgrade.TowerId, out var evolving) || evolving.Sold)
                    return new(false, "Choose an active tower.");
                if (evolving.BranchId is not null) return new(false, "This tower has already chosen its path.");
                if (string.IsNullOrWhiteSpace(upgrade.BranchId) || !branches.TryGetValue(upgrade.BranchId, out var branch) ||
                    branch.TowerId != evolving.FamilyId) return new(false, "That path does not belong to this tower.");
                if (Gold < branch.Cost) return new(false, "Not enough gold for this path.");
                Gold -= branch.Cost; GoldSpent += branch.Cost; evolving.Cost += branch.Cost;
                evolving.BranchId = branch.Upgrade.Id; evolving.Definition = ApplyRelics(branch.Upgrade);
                // Preserve cooldown, target priority, statistics and in-flight attack snapshots.
                Emit(EventKind.Upgraded, evolving.Position, evolving.Id, branch.Cost, effectId: evolving.BranchId);
                return new(true, $"{branch.Upgrade.Name} ready.", evolving.Id);
            case StartWave:
                if (Phase != RunPhase.Planning || WaveNumber >= definition.Waves.Length) return new(false, "A wave is already active.");
                if (rewardOffers.Count > 0) return new(false, "Choose your earned relic before the next wave.");
                waveKillsAtStart = Kills; waveLeaksAtStart = Leaks; waveIncomeAtStart = GoldEarned;
                WaveNumber++;
                var wave = definition.Waves[WaveNumber - 1];
                var groups = wave.Groups.AsEnumerable();
                if (ChallengeEnabled && definition.Expedition is { } expedition)
                    groups = groups.Append(new(expedition.ChallengeEnemyId, ChallengeCountAt(WaveNumber), 45, 90));
                spawnPlan = groups.SelectMany((g, group) =>
                    {
                        var enemy = WaveEnemy(enemyDefinitions[g.EnemyId]);
                        return Enumerable.Range(0, g.Count).Select(ordinal => new ScheduledSpawn(g.DelayTicks + (long)ordinal * g.IntervalTicks, group, ordinal, enemy));
                    })
                    .OrderBy(s => s.Offset).ThenBy(s => s.Group).ThenBy(s => s.Ordinal).ToArray();
                PendingSpawns = spawnPlan.Length; nextSpawn = 0;
                waveStartTick = Tick + 1; Phase = RunPhase.Wave; IsPaused = false;
                Emit(EventKind.WaveStarted, Route.At(0));
                return new(true, $"Wave {WaveNumber} approaching.");
            case Pause pause:
                if (Phase != RunPhase.Wave) return new(false, "Pause is available during a wave.");
                IsPaused = pause.Paused;
                return new(true, IsPaused ? "Tactical pause — you can still build." : "Defense resumed.");
            case ChooseRelic choice: return ClaimRelic(choice.RelicId);
            case SetChallenge challenge:
                if (definition.Expedition is null || Phase != RunPhase.Planning || rewardOffers.Count > 0)
                    return new(false, "Choose reinforcements during planning, after your relic.");
                ChallengeEnabled = challenge.Enabled;
                return new(true, ChallengeEnabled ? $"Overrun accepted: {ChallengeCount} extra raiders; +{ChallengeReward} gold only with zero breaches." : "Standard encounter selected.");
            default: return new(false, "Unknown command.");
        }
    }

    public void Step()
    {
        if (Phase != RunPhase.Wave || IsPaused) return;
        Tick++;
        while (PendingSpawns > 0 && Tick >= waveStartTick + spawnPlan[nextSpawn].Offset)
        {
            var enemy = new EnemyState(nextId++, Route.At(0), spawnPlan[nextSpawn++].Enemy, Tick);
            enemies.Add(enemy); enemyById.Add(enemy.Id, enemy);
            PendingSpawns--;
        }
        foreach (var enemy in enemies)
        {
            PrepareAbility(enemy);
            enemy.StatusSet.Expire(Tick);
            enemy.PreviousPosition = enemy.Position;
            var speed = enemy.Definition.Speed * (enemy.Enraged ? 1 + (enemy.Definition.Boss?.EnrageSpeedPercent ?? 0) / 100d : 1);
            enemy.Progress = Math.Min(Route.Length, enemy.Progress + speed * (100 - enemy.SlowPercent) / (100d * TicksPerSecond));
            enemy.Position = Route.At(enemy.Progress);
        }
        if (!referenceQueries) spatial.Rebuild(enemies);
        foreach (var enemy in enemies) enemy.Protectors.Clear();
        foreach (var escort in enemies)
        {
            if (escort.Definition.ShieldRadius <= 0) continue;
            QueryEnemies(escort.Position, escort.Definition.ShieldRadius);
            foreach (var ally in candidates)
                if (ally.Definition.ShieldRadius == 0) ally.Protectors.Add(escort);
        }
        foreach (var tower in towers)
        {
            var attack = tower.Definition;
            EnemyState? target = null;
            QueryEnemies(tower.Position, attack.Range);
            foreach (var enemy in candidates)
            {
                if (target is null || Preferred(enemy, target, tower.Priority, attack.DamageKind)) target = enemy;
            }
            tower.TargetId = target?.Id ?? 0;
            if (target is null || Tick < tower.NextFireTick) continue;
            Emit(EventKind.Fired, tower.Position, tower.Id, attack: attack.Attack, destination: target.Position, damageKind: attack.DamageKind);
            if (attack.Attack == AttackKind.Beam)
            {
                Strike(target, attack.Damage, tower.Id, attack);
                ForkAttack(target.Id, target.Position, tower.Id, attack);
            }
            else projectiles.Add(new ProjectileState(nextId++, tower.Id, target.Id, tower.Position, target.Position, attack));
            tower.NextFireTick = Tick + attack.CooldownTicks;
        }
        foreach (var projectile in projectiles)
        {
            projectile.PreviousPosition = projectile.Position;
            var attack = projectile.Definition;
            enemyById.TryGetValue(projectile.TargetId, out var target);
            if (target is { Alive: true }) projectile.AimPosition = target.Position;
            else if (attack.Attack != AttackKind.Shell) { projectile.Active = false; continue; }
            // A shell still detonates at its last tracked position if another tower kills its target.
            var difference = projectile.AimPosition - projectile.Position;
            var travel = attack.ProjectileSpeed / TicksPerSecond;
            if (difference.LengthSquared <= travel * travel)
            {
                projectile.Position = projectile.AimPosition; projectile.Active = false;
                if (target is { Alive: true }) Strike(target, attack.Damage, projectile.SourceId, attack);
                ForkAttack(projectile.TargetId, projectile.Position, projectile.SourceId, attack);
                if (attack.SplashRadius > 0)
                {
                    Emit(EventKind.Blast, projectile.Position, projectile.SourceId,
                        (int)Math.Min(attack.SplashRadius, int.MaxValue), attack.Attack, damageKind: attack.DamageKind);
                    QueryEnemies(projectile.Position, attack.SplashRadius, impactCandidates);
                    impactCandidates.Sort(static (a, b) => a.Id.CompareTo(b.Id));
                    foreach (var secondary in impactCandidates)
                        if (secondary.Id != projectile.TargetId)
                            Strike(secondary, Math.Max(1, attack.Damage / 2), projectile.SourceId, attack);
                }
                if (attack.Echo is { } echo)
                    pendingImpacts.Add(new(nextId++, projectile.Position, projectile.SourceId, echo, Tick, Tick + echo.DelayTicks));
            }
            else projectile.Position += difference * (travel / difference.Length);
        }
        ResolvePersistentEffects();
        ResolveEnemyAbilities();
        // Resolve deaths first: a killed enemy at the gate cannot also leak.
        foreach (var enemy in enemies)
        {
            if (!enemy.Alive || enemy.Progress < Route.Length) continue;
            enemy.Alive = false; Leaks++;
            Lives = Math.Max(0, Lives - enemy.Definition.LeakDamage);
            Emit(EventKind.Leaked, enemy.Position, enemy.Id, enemy.Definition.LeakDamage);
        }
        foreach (var enemy in enemies) if (!enemy.Alive) enemyById.Remove(enemy.Id);
        enemies.RemoveAll(e => !e.Alive); projectiles.RemoveAll(p => !p.Active);
        if (Lives == 0)
        {
            Phase = RunPhase.Defeat; IsPaused = false;
            Emit(EventKind.Defeat, Route.At(Route.Length));
        }
        else if (PendingSpawns == 0 && enemies.Count == 0)
        {
            projectiles.Clear(); burningAreas.Clear(); pendingImpacts.Clear();
            CompleteWaveRewards();
            Phase = WaveNumber == definition.Waves.Length ? RunPhase.Victory : RunPhase.Planning;
            Emit(Phase == RunPhase.Victory ? EventKind.Victory : EventKind.WaveCleared, Route.At(Route.Length));
        }
    }

    private void QueryEnemies(Point position, double radius, List<EnemyState>? result = null)
    {
        result ??= candidates;
        if (!referenceQueries) { spatial.Query(position, radius, result); return; }
        result.Clear();
        foreach (var enemy in enemies)
            if (enemy.Alive && (enemy.Position - position).LengthSquared <= radius * radius)
                result.Add(enemy);
    }

    private bool Preferred(EnemyState candidate, EnemyState current, TargetPriority priority, DamageKind kind)
    {
        var rank = priority switch
        {
            TargetPriority.Strongest => candidate.Health.CompareTo(current.Health),
            TargetPriority.Support => candidate.IsSupport.CompareTo(current.IsSupport),
            TargetPriority.Primed => IsPrimed(candidate, kind).CompareTo(IsPrimed(current, kind)),
            _ => 0
        };
        return rank != 0 ? rank > 0 : candidate.Progress > current.Progress ||
            (candidate.Progress == current.Progress && candidate.Id < current.Id);
    }

    private void DealDamage(EnemyState enemy, int amount, int towerId, DamageKind kind,
        AttackKind attack = AttackKind.Bolt, ReactionStatistics? reaction = null)
    {
        if (!enemy.Alive) return;
        var afterArmor = kind != DamageKind.Impact ? amount : Math.Max(1, amount - enemy.EffectiveArmor);
        var afterShield = Math.Max(1, (int)((long)afterArmor * (100 - enemy.ShieldPercent) / 100));
        var damage = Math.Min(afterShield, enemy.Health);
        enemy.Health -= damage; DamageDealt += damage;
        var tower = towerHistory[towerId]; tower.DamageDealt += damage;
        if (reaction is not null) { reaction.Damage += damage; tower.ReactionDamage += damage; ReactionDamage += damage; }
        Emit(EventKind.Hit, enemy.Position, enemy.Id, damage, attack, damageKind: kind);
        if (enemy.Health > 0) return;
        enemy.Alive = false; Kills++; tower.Kills++;
        Gold += enemy.Definition.Bounty; GoldEarned += enemy.Definition.Bounty;
        Emit(EventKind.Killed, enemy.Position, enemy.Id, enemy.Definition.Bounty);
    }

    private void Emit(EventKind kind, Point position, int id = 0, long amount = 0,
        AttackKind attack = AttackKind.Bolt, Point? destination = null,
        DamageKind damageKind = DamageKind.Impact, string? effectId = null) =>
        events.Add(new(Tick, kind, position, id, amount, attack, destination, damageKind, effectId));
    public GameEvent[] DrainEvents() { var result = events.ToArray(); events.Clear(); return result; }

    /// <summary>Gameplay-only digest: event consumption/rendering cannot change it.</summary>
    public string Checksum()
    {
        using var stream = new MemoryStream();
        using (var writer = new BinaryWriter(stream, Encoding.UTF8, true))
        {
            writer.Write(Seed); writer.Write(Tick); writer.Write((int)Phase); writer.Write(IsPaused);
            writer.Write(Gold); writer.Write(Lives); writer.Write(WaveNumber); writer.Write(PendingSpawns);
            writer.Write(nextId); writer.Write(waveStartTick); writer.Write(nextSpawn); writer.Write(Kills); writer.Write(Leaks);
            writer.Write(DamageDealt); writer.Write(GoldEarned); writer.Write(GoldSpent);
            writer.Write(towerHistory.Count);
            foreach (var t in towerHistory.Values.OrderBy(t => t.Id))
            { writer.Write(t.Id); writer.Write(t.Definition.Id); writer.Write((int)t.Priority); writer.Write(t.Foundation); writer.Write(t.Sold); writer.Write(t.NextFireTick); writer.Write(t.TargetId); writer.Write(t.DamageDealt); writer.Write(t.Kills);
              writer.Write(t.FamilyId); writer.Write(t.BranchId ?? ""); writer.Write(t.Cost); writer.Write(t.ReactionsTriggered); writer.Write(t.ReactionsPrepared); writer.Write(t.ReactionDamage); }
            writer.Write(enemies.Count);
            foreach (var e in enemies)
            { writer.Write(e.Id); writer.Write(e.Definition.Id); writer.Write(e.Progress); writer.Write(e.Health); WriteEnemyEffects(writer, e); }
            writer.Write(projectiles.Count);
            foreach (var p in projectiles)
            { writer.Write(p.Id); writer.Write(p.SourceId); writer.Write(p.TargetId); writer.Write(p.Position.X); writer.Write(p.Position.Y); writer.Write(p.AimPosition.X); writer.Write(p.AimPosition.Y); writer.Write(p.Definition.Id); }
            WriteEffects(writer);
            WriteExpedition(writer);
        }
        return Convert.ToHexString(SHA256.HashData(stream.ToArray())).ToLowerInvariant();
    }
}
