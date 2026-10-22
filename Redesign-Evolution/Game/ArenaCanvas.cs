using Godot;
using Redesign.Core;

namespace Redesign.Game;

/// <summary>Disposable presentation. Effects never modify authoritative gameplay.</summary>
public partial class ArenaCanvas : Control
{
    public GameRoot Game { get; init; } = null!;
    private readonly List<VisualEffect> effects = [];
    private readonly HashSet<Cell> road = [];
    private int hover = -1;
    private double visualTime;
    private Font font = null!;
    private Vector2[] drawnRoute = [];
    private sealed class VisualEffect(GameEvent e) { public GameEvent Event = e; public float Age; public float RestAge; }
    public override void _Ready()
    {
        font = ThemeDB.FallbackFont;
        var route = Game.Content.Map.Route;
        drawnRoute = route.Select(c => V(Game.Content.Map.Center(c))).ToArray();
        for (var i = 1; i < route.Length; i++)
        {
            var a = route[i - 1]; var b = route[i]; var dx = Math.Sign(b.X - a.X); var dy = Math.Sign(b.Y - a.Y);
            for (var c = a; ; c = new(c.X + dx, c.Y + dy)) { road.Add(c); if (c == b) break; }
        }
        GuiInput += HandleInput;
        MouseExited += () => { hover = -1; previewPointer = null; };
    }
    private void HandleInput(InputEvent e)
    {
        if (e is InputEventMouseMotion motion)
        {
            SetPreviewPointer(motion.Position);
            var enemy = Game.Run.Enemies.FirstOrDefault(enemy => V(enemy.Position).DistanceSquaredTo(motion.Position) < 24 * 24);
            TooltipText = enemy is null ? "" : $"{enemy.Definition.Name}: {enemy.Health}/{enemy.Definition.Health} health, {enemy.EffectiveArmor} armor, {enemy.SlowPercent}% slow.\n" +
                (enemy.Definition.ShieldRadius > 0
                    ? $"Protects nearby allies from {enemy.Definition.ShieldPercent}% damage. Wardens cannot shield each other."
                    : enemy.ShieldPercent > 0 ? $"Warded: {enemy.ShieldPercent}% less damage. Defeat the nearby escort." : $"Breach costs {enemy.Definition.LeakDamage} citadel lives.");
            if (enemy is not null) foreach (var status in enemy.Statuses)
                TooltipText += $"\n{status.Definition.Name} x{status.Count}: next stack expires in {(status.Stacks.Min(s => s.ExpiresAt) - Game.Run.Tick) / 60d:0.0}s.";
            if (enemy?.Definition.Healing is { } heal)
                TooltipText += $"\nMends {heal.Amount} health in radius {heal.Radius:0} every {heal.IntervalTicks / 60d:0.0}s. Kill it before its green ring completes.";
            if (enemy?.Definition.Boss is { } boss)
                TooltipText += $"\n{(enemy.BossVulnerable ? "EXPOSED: no armor or personal guard." : $"Guarded: {boss.GuardPercent}% damage reduction.")}\nRallies {boss.SummonsPerPulse} Raiders; {boss.MaxSummons - enemy.SummonsCreated} remain. Half-health enrage doubles rally frequency.";
            var guard = GuardAt(motion.Position);
            if (guard is not null) TooltipText = $"Shieldguard: {guard.Health}/{guard.Definition.Health} HP. Holds one enemy; extra enemies pass.\nClick to select the squad. Rally [F], retreat [H]. Recovery between waves only.";
        }
        if (Game.OrdersBlocked) return;
        if (e is InputEventMouseButton { Pressed: true, ButtonIndex: MouseButton.Right })
        { Game.CancelOrders(); AcceptEvent(); return; }
        if (e is InputEventMouseButton { Pressed: true, ButtonIndex: MouseButton.Left } click)
        {
            if (Game.Rallying) Game.RallyAtPointer(click.Position);
            else
            {
                var index = FoundationAt(click.Position);
                if (index >= 0) Game.SelectFoundation(index);
                else if (GuardAt(click.Position) is not null) Game.SelectGarrison();
            }
            AcceptEvent();
        }
    }
    public int FoundationAt(Vector2 position)
    {
        for (var i = 0; i < Game.Content.Map.Foundations.Length; i++)
            if (position.DistanceSquaredTo(V(Game.Content.Map.Center(Game.Content.Map.Foundations[i]))) <= 28 * 28) return i;
        return -1;
    }
    public void Receive(GameEvent e)
    {
        ReceiveSquadEvent(e);
        if (e.Kind is EventKind.Hit or EventKind.Killed or EventKind.Leaked or EventKind.Built or EventKind.Fired or EventKind.Blast or EventKind.Reaction or EventKind.Upgraded or EventKind.Healed or EventKind.Rally or EventKind.BossEnraged)
        { if (effects.Count == 160) effects.RemoveAt(0); effects.Add(new(e)); }
    }
    public void ClearEffects() { effects.Clear(); guardPoses.Clear(); hover = -1; previewPointer = null; }
    public override void _Process(double delta)
    {
        visualTime += delta;
        foreach (var e in effects)
        {
            if (Game.Run.Phase != RunPhase.Wave) e.RestAge += (float)delta;
            e.Age = Math.Max(0, (float)(PresentationTick - e.Event.Tick) / 60) + e.RestAge;
        }
        effects.RemoveAll(e => e.Age > .85f);
    }
    public override void _Draw()
    {
        var map = Game.Content.Map;
        DrawRect(new(0, 0, 960, 640), new Color("203c37"));
        for (var y = 0; y < map.Height; y++) for (var x = 0; x < map.Width; x++)
        {
            var hash = (x * 7349 + y * 9277 + x * y * 71) & 255;
            var ground = new Color(hash % 3 == 0 ? "244239" : hash % 3 == 1 ? "213d36" : "223f38");
            DrawRect(new(x * 64, y * 64, 64, 64), ground);
            if (road.Contains(new(x, y))) continue;
            var p = new Vector2(x * 64 + 16 + hash % 31, y * 64 + 12 + hash % 37);
            DrawLine(p, p + new Vector2(2, -5), new("38604a"), 2);
            DrawLine(p + new Vector2(5, 1), p + new Vector2(5, -3), new("315340"), 2);
            if (map.Foundations.Contains(new(x, y))) continue;
            if (y is 0 or 9 || x == 0 || (hash % 7 == 0 && !road.Contains(new(x, y + 1)))) Tree(p, .65f + hash % 4 * .1f);
            else if (hash % 9 == 0) Rock(p);
        }
        DrawPolyline(drawnRoute, new("142b2b"), 62, false);
        DrawPolyline(drawnRoute, new("675d45"), 52, false);
        DrawPolyline(drawnRoute, new("847455"), 42, false);
        foreach (var cell in road)
        {
            var p = V(map.Center(cell)); var hash = cell.X * 7 + cell.Y * 13;
            DrawRect(new(p.X - 16 + hash % 9, p.Y - 12, 18, 9), new Color("91805e"));
            DrawRect(new(p.X + 3, p.Y + 8, 14, 6), new Color("776a4e"));
        }
        TerrainEffects();
        AbilityTelegraphs();
        DrawSquadOrders();
        DrawCleaveWarnings();
        DrawString(font, new(8, 292), "INBOUND", HorizontalAlignment.Left, -1, 12, GameRoot.Gold);
        DrawLine(new(10, 307), new(44, 307), GameRoot.Gold, 2);
        DrawLine(new(36, 301), new(44, 307), GameRoot.Gold, 2);
        DrawLine(new(36, 313), new(44, 307), GameRoot.Gold, 2);
        for (var i = 0; i < map.Foundations.Length; i++)
        {
            var p = V(map.Center(map.Foundations[i]));
            var occupied = Game.Run.Towers.Any(t => t.Foundation == i) || Game.Run.Garrison?.Foundation == i;
            DrawRect(new(p.X - 23, p.Y - 15, 46, 36), new Color("132a28"));
            DrawRect(new(p.X - 21, p.Y - 19, 42, 36), new Color("5b6d60"));
            DrawRect(new(p.X - 17, p.Y - 15, 34, 28), new Color("394e47"));
            var color = Game.Building && !occupied ? GameRoot.Mint : new Color("8a9c7a");
            if (!occupied)
            {
                DrawLine(p + new Vector2(-7, -1), p + new Vector2(7, -1), color, 2);
                DrawLine(p + new Vector2(0, -8), p + new Vector2(0, 6), color, 2);
            }
            foreach (var offset in new[] { new Vector2(-18, -16), new Vector2(16, -16), new Vector2(-18, 12), new Vector2(16, 12) })
                DrawRect(new(p + offset, new(3, 3)), new Color("aab28a"));
        }
        var selected = Game.Run.Towers.FirstOrDefault(t => t.Id == Game.SelectedTower);
        Vector2? range = selected is null ? null : V(selected.Position);
        var rangeSpec = selected?.Definition ?? Game.BuildDefinition;
        if (Game.Building && !Game.BuildingGarrison && hover >= 0)
        {
            range = V(map.Center(map.Foundations[hover])); rangeSpec = Game.BuildDefinition;
        }
        if (range is Vector2 center)
        {
            var color = AttackColor(rangeSpec.Attack, rangeSpec.DamageKind);
            DrawCircle(center, (float)rangeSpec.Range, new Color(color, .065f));
            DrawArc(center, (float)rangeSpec.Range, 0, Mathf.Tau, 96, new Color(color, .42f), 1, true);
        }
        foreach (var enemy in Game.Run.Enemies)
            if (enemy.Definition.ShieldRadius > 0)
            {
                var p = V(enemy.PreviousPosition).Lerp(V(enemy.Position), (float)Game.Alpha);
                DrawCircle(p, (float)enemy.Definition.ShieldRadius, new Color("988bd0", .045f));
                DrawArc(p, (float)enemy.Definition.ShieldRadius, 0, Mathf.Tau, 48, new Color("b2a3dd", .25f), 1);
            }
        Castle(V(map.Center(map.Route[^1])));
        foreach (var tower in Game.Run.Towers) Tower(tower);
        DrawGarrison();
        foreach (var enemy in Game.Run.Enemies)
            if (!enemy.IsSupport && enemy.Definition.Boss is null) Enemy(enemy);
        // Keep escort silhouettes/staves readable when fast units overlap their formation.
        foreach (var enemy in Game.Run.Enemies)
            if (enemy.IsSupport && enemy.Definition.Boss is null) Enemy(enemy);
        foreach (var enemy in Game.Run.Enemies)
            if (enemy.Definition.Boss is not null) Enemy(enemy);
        DrawGuards();
        foreach (var shot in Game.Run.Projectiles)
        {
            var p = V(shot.PreviousPosition).Lerp(V(shot.Position), (float)Game.Alpha);
            var direction = V(shot.Position - shot.PreviousPosition).Normalized();
            var color = AttackColor(shot.Definition.Attack, shot.Definition.DamageKind);
            DrawLine(p - direction * 15, p, new Color(color, .25f), 5);
            if (shot.Definition.Attack == AttackKind.Shell)
            { DrawCircle(p, 5, new("563d34")); DrawCircle(p, 3, color); }
            else { DrawLine(p - direction * 8, p, color, 2); DrawCircle(p, 2, new("fff6cf")); }
        }
        foreach (var effect in effects) Effect(effect);
        DrawBossReadout();
        if (Game.Run.IsPaused)
        {
            DrawRect(new(285, 18, 390, 46), new Color("10262a"));
            DrawRect(new(285, 18, 390, 46), GameRoot.Mint, false, 1);
            DrawString(font, new(323, 48), "TACTICAL PAUSE  ·  BUILD & PLAN", HorizontalAlignment.Left, -1, 17, GameRoot.Mint);
        }
    }
    private void Enemy(EnemyState enemy)
    {
        var p = V(enemy.PreviousPosition).Lerp(V(enemy.Position), (float)Game.Alpha);
        var spec = enemy.Definition;
        var stride = enemy.EngagedGuardId == 0 ? MathF.Sin((float)(PresentationTick * .3 + enemy.Id)) * 3 : 0;
        DrawEllipseShadow(p + new Vector2(0, 9), spec.Armor > 0 ? 20 : 15);
        DrawLine(p + new Vector2(-8, 3), p + new Vector2(-13, 12 + stride), new("262433"), 4);
        DrawLine(p + new Vector2(8, 3), p + new Vector2(13, 12 - stride), new("262433"), 4);
        if (spec.Boss is not null) DrawBellwarden(enemy, p);
        else if (spec.Healing is not null)
        {
            DrawColoredPolygon([p + new Vector2(0, -26), p + new Vector2(-15, 12), p + new Vector2(15, 12)], new("355d53"));
            DrawCircle(p + new Vector2(0, -16), 8, new("a3c1a1"));
            DrawLine(p + new Vector2(19, 10), p + new Vector2(19, -23), new("d0d9ac"), 3);
            DrawLine(p + new Vector2(13, -20), p + new Vector2(25, -20), GameRoot.Mint, 4);
            DrawLine(p + new Vector2(19, -26), p + new Vector2(19, -14), GameRoot.Mint, 4);
        }
        else if (spec.ShieldRadius > 0)
        {
            DrawColoredPolygon([p + new Vector2(0, -24), p + new Vector2(-15, 10), p + new Vector2(15, 10)], new("554b77"));
            DrawColoredPolygon([p + new Vector2(0, -23), p + new Vector2(-8, -11), p + new Vector2(0, -2), p + new Vector2(8, -11)], new("b2a3dd"));
            DrawLine(p + new Vector2(20, 12), p + new Vector2(20, -27), new("ddd1b3"), 3);
            DrawArc(p + new Vector2(20, -28), 6, 0, Mathf.Tau, 6, new("d7c9f6"), 2);
            for (var i = -1; i <= 1; i++) DrawRect(new(p.X + i * 6 - 1, p.Y - 17 - (i == 0 ? 4 : 0), 3, 6), new Color("e3dafa"));
        }
        else if (spec.Armor > 0)
        {
            DrawRect(new(p.X - 16, p.Y - 17, 32, 29), new Color("343f4b"));
            DrawRect(new(p.X - 13, p.Y - 23, 26, 21), new Color("a49c86"));
            DrawRect(new(p.X - 9, p.Y - 15, 18, 4), new Color("302a2f"));
            DrawColoredPolygon([p + new Vector2(-19, -5), p + new Vector2(1, -5), p + new Vector2(1, 14), p + new Vector2(-9, 21), p + new Vector2(-19, 14)], new("b57b58"));
            DrawLine(p + new Vector2(-9, -2), p + new Vector2(-9, 16), GameRoot.Gold, 3);
            DrawLine(p + new Vector2(-15, 5), p + new Vector2(-3, 5), GameRoot.Gold, 2);
            if (spec.LeakDamage >= 3)
            {
                DrawLine(p + new Vector2(18, 10), p + new Vector2(18, -45), new("dfc99b"), 3);
                DrawColoredPolygon([p + new Vector2(18, -45), p + new Vector2(40, -39), p + new Vector2(18, -28)], new("c97e60"));
            }
        }
        else
        {
            DrawColoredPolygon([p + new Vector2(-12, 2), p + new Vector2(-9, -10), p + new Vector2(0, -17), p + new Vector2(10, -9), p + new Vector2(13, 3), p + new Vector2(0, 12)], new("493d57"));
            DrawColoredPolygon([p + new Vector2(-9, -7), p + new Vector2(0, -14), p + new Vector2(9, -7), p + new Vector2(0, 2)], new("8c6c83"));
            DrawRect(new(p.X - 6, p.Y - 3, 4, 3), GameRoot.Red); DrawRect(new(p.X + 2, p.Y - 3, 4, 3), GameRoot.Red);
            if (spec.Speed >= 80)
            {
                DrawLine(p + new Vector2(-18, 5), p + new Vector2(-26, -7), new("eed6a9"), 2);
                DrawLine(p + new Vector2(18, 5), p + new Vector2(26, -7), new("eed6a9"), 2);
            }
        }
        if (enemy.Health < spec.Health)
        {
            DrawRect(new(p.X - 15, p.Y - 33, 30, 4), new Color("12242a"));
            DrawRect(new(p.X - 15, p.Y - 33, 30f * enemy.Health / spec.Health, 4), GameRoot.Red);
        }
        if (enemy.ShieldPercent > 0)
        {
            DrawArc(p, 22, 0, Mathf.Tau, 6, new("d2baf0"), 2);
            DrawString(font, p + new Vector2(-5, -39), "+", HorizontalAlignment.Left, -1, 14, new("e0cef7"));
        }
        EnemyStatuses(enemy, p);
        DrawEnemyMelee(enemy, p);
    }

    private void Tower(TowerState tower)
    {
        var p = V(tower.Position); DrawEllipseShadow(p + new Vector2(0, 17), 24);
        DrawRect(new(p.X - 17, p.Y - 2, 34, 26), new Color("263e40"));
        DrawRect(new(p.X - 15, p.Y - 7, 30, 26), new Color("8c8f73"));
        DrawRect(new(p.X - 12, p.Y - 5, 24, 17), new Color("5f7265"));
        DrawRect(new(p.X - 3, p.Y + 6, 6, 9), GameRoot.Mint);
        var top = p + new Vector2(0, -10);
        var enemy = Game.Run.Enemies.FirstOrDefault(e => e.Id == tower.TargetId);
        var dir = enemy is null ? new Vector2(0, -1) : (V(enemy.Position) - top).Normalized();
        var side = new Vector2(-dir.Y, dir.X);
        if (tower.Definition.Attack == AttackKind.Beam)
        {
            DrawCircle(top, 25, new Color("8bdfec", .08f));
            DrawArc(top, 18, 0, Mathf.Tau, 8, new("7cbdc7"), 2);
            DrawColoredPolygon([top + new Vector2(0, -26), top + new Vector2(-10, -8), top + new Vector2(0, 9), top + new Vector2(10, -8)], new("65b9cf"));
            DrawColoredPolygon([top + new Vector2(0, -26), top + new Vector2(0, 9), top + new Vector2(10, -8)], new("c4f1e9"));
        }
        else if (tower.Definition.Attack == AttackKind.Shell)
        {
            DrawCircle(top, 17, new("8a5c49")); DrawCircle(top, 13, new("c39566"));
            DrawLine(top - dir * 5, top + dir * 19, new("293f46"), 18);
            DrawLine(top - dir * 4 - side * 4, top + dir * 18 - side * 4, new("728b88"), 3);
            DrawCircle(top + dir * 19, 7, new("172930")); DrawCircle(top + dir * 19, 3, new("ea9c69"));
            DrawRect(new(p.X - 8, p.Y + 5, 16, 8), new Color("ea9c69"));
            for (var x = -6; x <= 6; x += 6) DrawLine(p + new Vector2(x, 4), p + new Vector2(x, 14), new("273d40"), 2);
        }
        else
        {
            DrawCircle(top, 12, new("20363a")); DrawCircle(top, 9, new("c4a766"));
            DrawLine(top - dir * 8, top + dir * 23, new("1b3034"), 8);
            DrawLine(top - dir * 6, top + dir * 22, GameRoot.Gold, 3);
            DrawLine(top - side * 15 + dir * 7, top + side * 15 + dir * 7, new("9ba589"), 4);
            DrawLine(top - side * 15 + dir * 7, top - dir * 3, new("d2d1a4"), 1);
            DrawLine(top + side * 15 + dir * 7, top - dir * 3, new("d2d1a4"), 1);
        }
        BranchSilhouette(tower, top, dir, side);
        if (tower.Id == Game.SelectedTower) DrawArc(p, 29, 0, Mathf.Tau, 40, GameRoot.Mint, 2);
    }
    private void Castle(Vector2 p)
    {
        DrawEllipseShadow(p + new Vector2(-4, 15), 33);
        DrawRect(new(p.X - 29, p.Y - 43, 56, 58), new Color("344d50"));
        DrawRect(new(p.X - 25, p.Y - 40, 48, 51), new Color("849488"));
        DrawRect(new(p.X - 22, p.Y - 35, 42, 16), new Color("a5ad94"));
        DrawColoredPolygon([p + new Vector2(-34, -44), p + new Vector2(-3, -71), p + new Vector2(32, -44)], new("385761"));
        DrawLine(p + new Vector2(-34, -44), p + new Vector2(-3, -71), GameRoot.Mint, 2);
        DrawRect(new(p.X - 12, p.Y - 12, 20, 29), new Color("182e32"));
        DrawRect(new(p.X - 6, p.Y - 7, 7, 14), GameRoot.Mint);
        DrawLine(p + new Vector2(-3, -71), p + new Vector2(-3, -101), new("c1ba86"), 2);
        var flutter = (float)Math.Sin(visualTime * 2) * 3;
        DrawColoredPolygon([p + new Vector2(-2, -100), p + new Vector2(21, -97 + flutter), p + new Vector2(17, -84 + flutter), p + new Vector2(-2, -86)], GameRoot.Mint);
        DrawCircle(p + new Vector2(-27, -16), 9, new Color(GameRoot.Gold, .08f));
        DrawRect(new(p.X - 29, p.Y - 20, 4, 7), GameRoot.Gold);
    }
    private void Effect(VisualEffect effect)
    {
        var p = V(effect.Event.Position); var fade = 1 - effect.Age / .85f;
        var color = effect.Event.Kind == EventKind.Leaked ? GameRoot.Red : effect.Event.EffectId == "echo" ? new Color("c3acf3") : AttackColor(effect.Event.Attack, effect.Event.DamageKind);
        if (effect.Event.Kind == EventKind.Healed)
        {
            DrawString(font, p + new Vector2(-15, -30 - effect.Age * 24), $"+{effect.Event.Amount} HP", HorizontalAlignment.Left, -1, 12, new Color(GameRoot.Mint, fade));
        }
        else if (effect.Event.Kind is EventKind.Rally or EventKind.BossEnraged)
        {
            DrawArc(p, 30 + effect.Age * 110, 0, Mathf.Tau, 12, new Color(GameRoot.Red, fade), 3);
            DrawString(font, p + new Vector2(-28, -65 - effect.Age * 15), effect.Event.Kind == EventKind.Rally ? "RALLY" : "ENRAGED", HorizontalAlignment.Left, -1, 18, new Color(GameRoot.Red, fade));
        }
        else if (effect.Event.Kind == EventKind.Reaction)
        {
            var radius = 12 + Math.Min(effect.Event.Amount, 75) * Math.Min(1, effect.Age / .4f);
            DrawArc(p, radius, 0, Mathf.Tau, effect.Event.DamageKind == DamageKind.Frost ? 6 : 32, new Color(color, fade * .8f), 2);
            for (var i = 0; i < 8; i++)
            {
                var d = Vector2.FromAngle(i * Mathf.Tau / 8);
                DrawLine(p + d * radius * .4f, p + d * radius * .85f, new Color(color, fade), 3);
            }
            var name = Game.Content.Combat?.Reactions.FirstOrDefault(r => r.Id == effect.Event.EffectId)?.Name ?? "REACTION";
            DrawString(font, p + new Vector2(-32, -42 - effect.Age * 22), name.ToUpperInvariant(), HorizontalAlignment.Left, -1, 13, new Color(color, fade));
        }
        else if (effect.Event.Kind == EventKind.Fired && effect.Event.Attack == AttackKind.Beam)
        {
            if (effect.Age > .18f || effect.Event.Destination is not { } destination) return;
            var end = V(destination); var start = p + new Vector2(0, -18);
            DrawLine(start, end, new Color(color, (1 - effect.Age / .18f) * .24f), 10, true);
            DrawLine(start, end, new Color(color, 1 - effect.Age / .18f), 2, true);
        }
        else if (effect.Event.Kind == EventKind.Blast)
        {
            if (effect.Age > .35f) return;
            var radius = effect.Event.Amount * Math.Min(1, effect.Age / .18f);
            DrawCircle(p, radius, new Color(color, .10f * (1 - effect.Age / .35f)));
            DrawArc(p, radius, 0, Mathf.Tau, 32, new Color(color, .65f * (1 - effect.Age / .35f)), 2);
        }
        else if (effect.Event.Kind == EventKind.Killed)
        {
            for (var i = 0; i < 6; i++)
            {
                var d = Vector2.FromAngle(i * Mathf.Tau / 6);
                DrawRect(new(p + d * (8 + effect.Age * 35), new(3, 3)), new Color(color, fade));
            }
            DrawString(font, p + new Vector2(-10, -25 - effect.Age * 25), $"+{effect.Event.Amount}", HorizontalAlignment.Left, -1, 14, new Color(color, fade));
        }
        else if (effect.Event.Kind is EventKind.Built or EventKind.Upgraded)
            DrawArc(p, 20 + effect.Age * 28, 0, Mathf.Tau, 32, new Color(GameRoot.Mint, fade), 2);
        else if (effect.Age < .22f)
        {
            var size = 5 + effect.Age * 35;
            DrawLine(p + new Vector2(-size, -size), p + new Vector2(size, size), new Color(color, fade), 2);
            DrawLine(p + new Vector2(-size, size), p + new Vector2(size, -size), new Color(color, fade), 2);
        }
    }
    private void Tree(Vector2 p, float scale)
    {
        DrawEllipseShadow(p + new Vector2(0, 7), 18 * scale);
        DrawRect(new(p.X - 3, p.Y - 8, 6, 20), new Color("3e4635"));
        for (var i = 0; i < 3; i++)
        {
            var width = (24 - i * 5) * scale; var y = p.Y - i * 13 * scale;
            DrawColoredPolygon([new(p.X - width, y), new(p.X, y - 32 * scale), new(p.X + width, y)], new(i % 2 == 0 ? "18382f" : "214b3b"));
            DrawLine(new(p.X, y - 30 * scale), new(p.X + width - 2, y - 1), new("315a41"), 2);
        }
    }
    private void Rock(Vector2 p)
    {
        DrawColoredPolygon([p + new Vector2(-12, 7), p + new Vector2(-9, -6), p + new Vector2(3, -11), p + new Vector2(14, -2), p + new Vector2(11, 7)], new("52635a"));
        DrawLine(p + new Vector2(-9, -6), p + new Vector2(3, -11), new("80907a"), 3);
    }
    private void DrawEllipseShadow(Vector2 p, float radius) => DrawRect(new(p.X - radius, p.Y - 4, radius * 2, 9), new Color(0, .05f, .06f, .25f));
    internal static Vector2 V(Redesign.Core.Point p) => new((float)p.X, (float)p.Y);
    private static Color AttackColor(AttackKind attack, DamageKind kind = DamageKind.Impact) => kind switch
    {
        DamageKind.Frost => new("b4e7ff"), DamageKind.Fire => new("ffae64"),
        _ => attack switch { AttackKind.Beam => new("95dfeb"), AttackKind.Shell => new("eeaa76"), _ => GameRoot.Gold }
    };
}
