using Godot;
using Redesign.Core;

namespace Redesign.Game;

public enum GuardPose { Idle, Walk, Anticipate, Contact, Recover, Defend, Hurt, Fallen }

public partial class ArenaCanvas
{
    private Vector2? previewPointer;
    private readonly Dictionary<int, GuardFeedback> guardPoses = [];
    private sealed class GuardFeedback
    {
        public long StruckAt = -1000, HurtAt = -1000, FallenAt = -1000;
        public Vector2 Facing = Vector2.Left;
    }
    // The same interpolated clock drives movement, poses, impacts and warnings. Pause freezes all four.
    public double PresentationTick => Game.Run.Tick == 0 ? 0 : Game.Run.Tick - 1 + Game.Alpha;

    public void SetPreviewPointer(Vector2 position) { previewPointer = position; hover = FoundationAt(position); }

    private void ReceiveSquadEvent(GameEvent e)
    {
        if (e.Kind == EventKind.SquadRecovered) { guardPoses.Clear(); return; }
        if (e.Kind is not (EventKind.GuardAttackStarted or EventKind.GuardStruck or EventKind.GuardHit or EventKind.GuardFallen)) return;
        if (!guardPoses.TryGetValue(e.EntityId, out var feedback)) guardPoses[e.EntityId] = feedback = new();
        if (e.Destination is { } target && (target - e.Position).LengthSquared > .001)
            feedback.Facing = V(target - e.Position).Normalized();
        if (e.Kind == EventKind.GuardStruck) feedback.StruckAt = e.Tick;
        if (e.Kind == EventKind.GuardHit) feedback.HurtAt = e.Tick;
        if (e.Kind == EventKind.GuardFallen) feedback.FallenAt = e.Tick;
    }

    public GuardPose PoseFor(GuardState guard)
    {
        guardPoses.TryGetValue(guard.Id, out var feedback);
        if (!guard.Alive) return GuardPose.Fallen;
        if (feedback is not null && PresentationTick - feedback.HurtAt < 10) return GuardPose.Hurt;
        if ((guard.Position - guard.PreviousPosition).LengthSquared > .0001) return GuardPose.Walk;
        if (guard.AttackAtTick > Game.Run.Tick) return GuardPose.Anticipate;
        if (feedback is not null && PresentationTick - feedback.StruckAt < 6) return GuardPose.Contact;
        if (feedback is not null && PresentationTick - feedback.StruckAt < 19) return GuardPose.Recover;
        var enemy = Game.Run.Enemies.FirstOrDefault(e => e.Id == guard.EngagedEnemyId);
        if (enemy?.MeleeAtTick > Game.Run.Tick) return GuardPose.Defend;
        return GuardPose.Idle;
    }

    // Small formation offsets separate the three silhouettes. They never change collision or reach.
    public Vector2 GuardVisualPosition(GuardState guard)
    {
        var garrison = Game.Run.Garrison!;
        var slot = garrison.Guards.ToList().IndexOf(guard) - 1;
        var tangent = V(Game.Run.Route.At(guard.RoadProgress + 1) - Game.Run.Route.At(guard.RoadProgress - 1)).Normalized();
        var side = new Vector2(-tangent.Y, tangent.X);
        return V(guard.PreviousPosition).Lerp(V(guard.Position), (float)Game.Alpha) + side * slot * 19 + tangent * Math.Abs(slot) * 4;
    }

    private GuardState? GuardAt(Vector2 position) => Game.Run.Garrison?.Guards
        .OrderBy(g => GuardVisualPosition(g).DistanceSquaredTo(position))
        .FirstOrDefault(g => GuardVisualPosition(g).DistanceSquaredTo(position) < 18 * 18);

    private void DrawSquadOrders()
    {
        var garrison = Game.Run.Garrison;
        var placing = Game.BuildingGarrison && hover >= 0 && !Game.Run.Towers.Any(t => t.Foundation == hover) && garrison is null;
        if (!placing && !(Game.GarrisonSelected && garrison is not null)) return;
        var position = placing ? Game.Content.Map.Center(Game.Content.Map.Foundations[hover]) : garrison!.Position;
        var entry = Game.Run.Route.NearestProgress(position);
        var access = (Game.Run.Route.At(entry) - position).Length;
        var budget = Game.Content.Garrison!.CommandRadius - access;
        if (budget >= 0)
        {
            var start = Math.Max(0, entry - budget); var end = Math.Min(Game.Run.Route.Length, entry + budget);
            // Include each bend exactly; sampling alone would visually cut road corners.
            var marks = Game.Content.Map.Route.Select(Game.Content.Map.Center).Select(Game.Run.Route.NearestProgress)
                .Where(d => d > start && d < end).Prepend(start).Append(end);
            var path = marks.Select(d => V(Game.Run.Route.At(d))).ToArray();
            DrawPolyline(path, new Color(GameRoot.Mint, .12f), 48, true);
            DrawPolyline(path, new Color(GameRoot.Mint, .85f), 2, true);
            foreach (var d in new[] { start, end }) DrawCircle(V(Game.Run.Route.At(d)), 5, GameRoot.Mint);
        }
        DrawDashedLine(V(position), V(Game.Run.Route.At(entry)), new Color(GameRoot.Mint, .7f), 2, 7);
        if (garrison is not null)
        {
            var rally = V(Game.Run.Route.At(garrison.RallyProgress));
            var tint = garrison.Retreating ? GameRoot.Muted : GameRoot.Mint;
            DrawArc(rally, 14, 0, Mathf.Tau, 24, tint, 2, true);
            DrawLine(rally, rally + new Vector2(0, -35), tint, 2, true);
            DrawColoredPolygon([rally + new Vector2(1, -35), rally + new Vector2(18, -29), rally + new Vector2(1, -23)], tint);
        }
        if (Game.Rallying && previewPointer is { } pointer)
        {
            var onRoad = Game.TryRoadPointer(pointer, out var destination);
            var reachable = onRoad && Game.Run.IsSquadRallyReachable(destination);
            var p = onRoad ? V(destination) : pointer;
            var tint = reachable ? GameRoot.Mint : GameRoot.Red;
            DrawArc(p, 22, 0, Mathf.Tau, 32, tint, 2, true);
            DrawLine(p + new Vector2(-7, -7), p + new Vector2(7, 7), tint, 2);
            DrawLine(p + new Vector2(-7, 7), p + new Vector2(7, -7), tint, 2);
            var label = reachable ? "RALLY HERE" : onRoad ? "BEYOND REACH" : "CHOOSE THE ROAD";
            var labelAt = new Vector2(Math.Clamp(p.X - 70, 8, 800), Math.Clamp(p.Y + 40, 90, 550));
            DrawRect(new(labelAt + new Vector2(-7, -18), new(158, 25)), new Color(GameRoot.Ink, .95f));
            DrawString(font, labelAt, label, HorizontalAlignment.Left, -1, 13, tint);
        }
    }

    private void DrawGarrison()
    {
        if (Game.Run.Garrison is not { } garrison) return;
        var p = V(garrison.Position);
        DrawEllipseShadow(p + new Vector2(0, 13), 31);
        DrawRect(new(p.X - 25, p.Y - 12, 50, 30), new Color("596b62"));
        DrawRect(new(p.X - 20, p.Y - 9, 40, 26), new Color("a8ac88"));
        DrawColoredPolygon([p + new Vector2(-30, -12), p + new Vector2(0, -35), p + new Vector2(30, -12)], new("264b50"));
        DrawLine(p + new Vector2(-30, -12), p + new Vector2(0, -35), GameRoot.Mint, 3);
        DrawRect(new(p.X - 8, p.Y - 5, 16, 24), new Color("172e32"));
        DrawLine(p + new Vector2(21, 5), p + new Vector2(21, -40), new("d4c998"), 2);
        DrawColoredPolygon([p + new Vector2(22, -40), p + new Vector2(36, -35), p + new Vector2(22, -25)], GameRoot.Mint);
        DrawString(font, p + new Vector2(-6, 10), "G", HorizontalAlignment.Left, -1, 13, GameRoot.Mint);
        if (Game.GarrisonSelected) DrawArc(p, 35, 0, Mathf.Tau, 40, GameRoot.Mint, 2, true);
    }

    private void DrawGuards()
    {
        if (Game.Run.Garrison is not { } garrison) return;
        foreach (var guard in garrison.Guards.OrderBy(g => GuardVisualPosition(g).Y))
        {
            var p = GuardVisualPosition(guard); var pose = PoseFor(guard);
            guardPoses.TryGetValue(guard.Id, out var feedback);
            var enemy = Game.Run.Enemies.FirstOrDefault(e => e.Id == guard.EngagedEnemyId);
            var facing = enemy is null ? feedback?.Facing ?? Vector2.Left : (V(enemy.Position) - p).Normalized();
            if (pose == GuardPose.Walk) facing = V(guard.Position - guard.PreviousPosition).Normalized();
            DrawEllipseShadow(p + new Vector2(0, 9), 12);
            if (pose == GuardPose.Fallen)
            {
                var fall = feedback is null ? 1 : Math.Clamp((float)(PresentationTick - feedback.FallenAt) / 16, 0, 1);
                DrawLine(p + new Vector2(-8 * fall, -13 * (1 - fall)), p + new Vector2(10, 5), new("688181"), 7);
                DrawCircle(p + new Vector2(-11 * fall, -14 * (1 - fall)), 5, new("8a9c95"));
                DrawLine(p + new Vector2(2, -7), p + new Vector2(13, -11), new("a7b6ac"), 2);
                DrawString(font, p + new Vector2(-5, 22), "+", HorizontalAlignment.Left, -1, 14, GameRoot.Muted);
                continue;
            }
            if (Game.GarrisonSelected) DrawArc(p + new Vector2(0, 8), 14, 0, Mathf.Tau, 24, new Color(GameRoot.Mint, .7f), 1, true);
            if (enemy is not null) DrawLine(p + new Vector2(0, 8), V(enemy.Position) + new Vector2(0, 8), new Color(GameRoot.Gold, .6f), 1, true);
            var stride = pose == GuardPose.Walk ? MathF.Sin((float)PresentationTick * .35f + guard.Id) * 4 : 0;
            var lunge = pose == GuardPose.Contact ? facing * 6 : pose == GuardPose.Hurt ? -facing * 3 : Vector2.Zero;
            p += lunge;
            DrawLine(p + new Vector2(-4, 0), p + new Vector2(-5, 10 + stride), new("1c2e37"), 4);
            DrawLine(p + new Vector2(4, 0), p + new Vector2(5, 10 - stride), new("1c2e37"), 4);
            DrawColoredPolygon([p + new Vector2(-7, -13), p + new Vector2(7, -13), p + new Vector2(10, 5), p + new Vector2(-10, 5)], new("327b74"));
            DrawRect(new(p.X - 6, p.Y - 12, 12, 13), pose == GuardPose.Hurt ? GameRoot.Red : new Color("c5d6cc"));
            DrawCircle(p + new Vector2(0, -19), 7, new("748f91"));
            DrawLine(p + new Vector2(-5, -20), p + new Vector2(5, -20), new("d8e5db"), 2);
            DrawRect(new(p.X - 4, p.Y - 17, 8, 3), new Color("173237"));
            var shield = p + new Vector2(facing.X >= 0 ? 9 : -9, pose == GuardPose.Defend ? -13 : -5);
            DrawColoredPolygon([shield + new Vector2(-6, -7), shield + new Vector2(6, -7), shield + new Vector2(5, 5), shield + new Vector2(0, 9), shield + new Vector2(-5, 5)], new("1c4b51"));
            DrawLine(shield + new Vector2(0, -5), shield + new Vector2(0, 5), GameRoot.Mint, 2);
            var hand = p + new Vector2(facing.X >= 0 ? -6 : 6, -7);
            var blade = pose == GuardPose.Anticipate ? new Vector2(-facing.X * 6, -22) : pose == GuardPose.Contact ? facing * 27 : new Vector2(facing.X * 12, -14);
            DrawLine(hand, hand + blade, new("1b3038"), 5, true);
            DrawLine(hand, hand + blade, new("e5e9d0"), 2, true);
            if (pose == GuardPose.Contact) DrawArc(p, 27, facing.Angle() - .6f, facing.Angle() + .6f, 12, new Color(GameRoot.Gold, .8f), 2, true);
            DrawRect(new(p.X - 12, p.Y - 33, 24, 5), GameRoot.Ink);
            DrawRect(new(p.X - 11, p.Y - 32, 22f * guard.Health / guard.Definition.Health, 3), guard.Health <= guard.Definition.Health / 3 ? GameRoot.Red : GameRoot.Mint);
            if (Game.Run.Enemies.Any(e => e.Definition.Melee is { Cleave: true } m && e.MeleeAtTick > Game.Run.Tick && (e.Position - guard.Position).LengthSquared <= m.Reach * m.Reach))
                DrawString(font, p + new Vector2(-3, -38), "!", HorizontalAlignment.Left, -1, 18, GameRoot.Red);
        }
    }

    private void DrawCleaveWarnings()
    {
        foreach (var enemy in Game.Run.Enemies)
        {
            if (enemy.Definition.Melee is not { Cleave: true } melee || enemy.MeleeAtTick <= Game.Run.Tick) continue;
            var p = V(enemy.PreviousPosition).Lerp(V(enemy.Position), (float)Game.Alpha);
            var radius = (float)melee.Reach;
            var progress = Math.Clamp(1 - (float)(enemy.MeleeAtTick - PresentationTick) / melee.WindupTicks, 0, 1);
            DrawCircle(p, radius, new Color(GameRoot.Red, .12f + progress * .12f));
            DrawArc(p, radius, 0, Mathf.Tau, 64, GameRoot.Red, 2, true);
            DrawArc(p, radius - 5, -Mathf.Pi / 2, -Mathf.Pi / 2 + Mathf.Tau * progress, 48, new("fff2cd"), 3, true);
            for (var i = 0; i < 12; i++)
            {
                var d = Vector2.FromAngle(i * Mathf.Tau / 12);
                DrawLine(p + d * (radius - 12), p + d * radius, new Color(GameRoot.Red, .8f), 2, true);
            }
            DrawRect(new(270, 76, 420, 31), new Color(GameRoot.Ink, .97f));
            DrawString(font, new(283, 98), $"CLEAVE IN {(enemy.MeleeAtTick - Game.Run.Tick) / 60d:0.0}s / RETREAT [H]", HorizontalAlignment.Left, -1, 17, GameRoot.Red);
        }
    }

    private void DrawEnemyMelee(EnemyState enemy, Vector2 p)
    {
        if (enemy.Definition.Melee is not { Cleave: false } melee || enemy.MeleeAtTick <= Game.Run.Tick) return;
        var progress = Math.Clamp(1 - (float)(enemy.MeleeAtTick - PresentationTick) / melee.WindupTicks, 0, 1);
        DrawArc(p + new Vector2(0, 3), 24, -Mathf.Pi / 2, -Mathf.Pi / 2 + Mathf.Tau * progress, 24, new Color(GameRoot.Red, .9f), 2, true);
    }
}
