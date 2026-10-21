using Godot;
using Redesign.Core;

namespace Redesign.Game;

public partial class ArenaCanvas
{
    private void AbilityTelegraphs()
    {
        foreach (var enemy in Game.Run.Enemies)
        {
            if (!enemy.WindingUp) continue;
            var p = V(enemy.PreviousPosition).Lerp(V(enemy.Position), (float)Game.Alpha);
            var windup = enemy.Definition.Healing?.WindupTicks ?? enemy.Definition.Boss!.WindupTicks;
            var progress = 1 - Math.Clamp((enemy.NextAbilityTick - Game.Run.Tick) / (float)windup, 0, 1);
            var radius = (float)(enemy.Definition.Healing?.Radius ?? 70);
            var color = enemy.Definition.Healing is not null ? GameRoot.Mint : GameRoot.Gold;
            DrawCircle(p, radius, new Color(color, .045f));
            DrawArc(p, radius, -Mathf.Pi / 2, -Mathf.Pi / 2 + Mathf.Tau * progress, 48, new Color(color, .8f), 2);
            DrawString(font, p + new Vector2(-27, -radius - 7), enemy.Definition.Healing is not null ? "MENDING" : "EXPOSED", HorizontalAlignment.Left, -1, 12, color);
        }
    }

    private void DrawBellwarden(EnemyState enemy, Vector2 p)
    {
        var trim = enemy.Enraged ? GameRoot.Red : GameRoot.Gold;
        DrawEllipseShadow(p + new Vector2(0, 14), 33);
        DrawColoredPolygon([p + new Vector2(-24, -27), p + new Vector2(0, -42), p + new Vector2(24, -27), p + new Vector2(29, 17), p + new Vector2(-29, 17)], new("453b4f"));
        DrawColoredPolygon([p + new Vector2(-17, -25), p + new Vector2(17, -25), p + new Vector2(24, 10), p + new Vector2(-24, 10)], new("93755e"));
        DrawLine(p + new Vector2(-28, 11), p + new Vector2(28, 11), trim, 5);
        DrawCircle(p + new Vector2(0, 13), 5, trim);
        DrawLine(p + new Vector2(-18, -21), p + new Vector2(18, -21), trim, 4);
        DrawCircle(p + new Vector2(0, -35), 7, new("d8bd8e"));
        DrawLine(p + new Vector2(-11, -5), p + new Vector2(11, -5), new("2c263b"), 5);
        if (enemy.BossVulnerable) DrawCircle(p + new Vector2(0, -5), 6, new("fff0be"));
        else DrawArc(p, 34, 0, Mathf.Tau, 8, new Color(trim, .8f), 3);
    }

    private void DrawBossReadout()
    {
        var boss = Game.Run.Enemies.FirstOrDefault(e => e.Definition.Boss is not null);
        if (boss is null) return;
        var ability = boss.Definition.Boss!;
        DrawRect(new(18, 564, 924, 60), new Color("101c26", .97f));
        var color = boss.BossVulnerable ? GameRoot.Gold : boss.Enraged ? GameRoot.Red : new Color("c7b1de");
        DrawString(font, new(30, 588), $"BELLWARDEN  /  {(boss.Enraged ? "PHASE II" : "PHASE I")}    {boss.Health} / {boss.Definition.Health}", HorizontalAlignment.Left, -1, 17, color);
        var state = boss.SummonsCreated >= ability.MaxSummons ? "RALLIES EXHAUSTED / EXPOSED" : boss.WindingUp
            ? $"EXPOSED / RALLY IN {(boss.NextAbilityTick - Game.Run.Tick) / 60d:0.0}s" : $"GUARDED / {ability.MaxSummons - boss.SummonsCreated} RAIDERS REMAIN";
        DrawString(font, new(490, 588), state, HorizontalAlignment.Left, -1, 15, color);
        DrawRect(new(30, 601, 900, 10), new Color("29323b"));
        DrawRect(new(30, 601, 900f * boss.Health / boss.Definition.Health, 10), color);
    }
}
