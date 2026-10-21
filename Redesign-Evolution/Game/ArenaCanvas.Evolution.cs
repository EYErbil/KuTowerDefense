using Godot;
using Redesign.Core;

namespace Redesign.Game;

public partial class ArenaCanvas
{
    private void TerrainEffects()
    {
        foreach (var area in Game.Run.BurningAreas)
        {
            var p = V(area.Position); var radius = (float)area.Definition.Radius;
            var life = Math.Min(1f, (area.ExpiresAt - Game.Run.Tick) / 30f);
            DrawCircle(p, radius, new Color("d77b42", .17f * life));
            DrawArc(p, radius, 0, Mathf.Tau, 32, new Color("f6b36f", .5f * life), 1);
            for (var i = 0; i < 7; i++)
            {
                var ember = p + Vector2.FromAngle(i * 2.4f + area.Id) * radius * (.2f + i * .09f);
                var height = 8 + (float)Math.Sin(visualTime * 9 + i + area.Id) * 3;
                DrawColoredPolygon([ember + new Vector2(-4, 3), ember + new Vector2(0, -height), ember + new Vector2(5, 3)], new("efaa69", .8f * life));
                DrawLine(ember, ember + new Vector2(0, -height / 2), new("ffe4ae", life), 2);
            }
        }
        foreach (var echo in Game.Run.PendingImpacts)
        {
            var p = V(echo.Position); var radius = (float)echo.Definition.Radius;
            var remaining = (echo.ResolveAt - Game.Run.Tick) / (float)(echo.ResolveAt - echo.CreatedAt);
            for (var i = 0; i < 8; i++) DrawArc(p, radius, i * Mathf.Tau / 8, (i + .6f) * Mathf.Tau / 8, 6, new("c7b2eb", .6f), 2);
            DrawArc(p, 12, -Mathf.Pi / 2, -Mathf.Pi / 2 + Mathf.Tau * remaining, 24, new("e2d3ff"), 2);
            DrawString(font, p + new Vector2(-18, -radius - 5), "ECHO", HorizontalAlignment.Left, -1, 11, new("e2d3ff"));
        }
    }

    private void EnemyStatuses(EnemyState enemy, Vector2 p)
    {
        if (enemy.Statuses.Count == 0) return;
        var x = p.X - enemy.Statuses.Count * 12;
        foreach (var status in enemy.Statuses)
        {
            var color = status.Definition.Id switch
            { "chill" => new Color("b4e7ff"), "oil" => new("e4b4e9"), "fracture" => new("f1bb86"), _ => GameRoot.Mint };
            DrawRect(new(x - 2, p.Y - 58, 23, 15), new Color("14292e", .95f));
            DrawString(font, new(x, p.Y - 46), $"{status.Definition.Name[0]}{status.Count}", HorizontalAlignment.Left, -1, 12, color);
            x += 24;
        }
        if (enemy.SlowPercent > 0) DrawArc(p, 19, 0, Mathf.Tau, 6, new("b4e7ff", .65f), 1);
    }

    // Visual identities are independent of the data-driven combat rules.
    private void BranchSilhouette(TowerState tower, Vector2 top, Vector2 dir, Vector2 side)
    {
        switch (tower.BranchId)
        {
            case "ballista":
                DrawLine(top - side * 23 + dir * 4, top + side * 23 + dir * 4, new("d6c295"), 5);
                DrawLine(top - side * 21 + dir * 4, top - dir * 7, GameRoot.Gold, 1);
                DrawLine(top + side * 21 + dir * 4, top - dir * 7, GameRoot.Gold, 1);
                DrawLine(top - dir * 10, top + dir * 30, new("f0ddaa"), 4);
                break;
            case "pitchbow":
                foreach (var sign in new[] { -1, 1 })
                {
                    var tank = top + side * 15 * sign - dir * 5;
                    DrawCircle(tank, 7, new("3e2949")); DrawCircle(tank, 4, new("cb8bc8"));
                }
                break;
            case "rime":
                for (var i = -1; i <= 1; i++)
                {
                    var crystal = top + new Vector2(i * 13, Math.Abs(i) * 5);
                    DrawColoredPolygon([crystal + new Vector2(-5, 0), crystal + new Vector2(0, -33 + Math.Abs(i) * 9), crystal + new Vector2(5, 0)], new("c6efff"));
                }
                DrawArc(top, 22, 0, Mathf.Tau, 6, new("9bd5ef"), 2);
                break;
            case "sun":
                DrawCircle(top + new Vector2(0, -8), 18, new("68402b"));
                DrawCircle(top + new Vector2(0, -8), 12, new("ffc67e"));
                DrawCircle(top + new Vector2(0, -8), 6, new("fff2c9"));
                for (var i = 0; i < 8; i++)
                {
                    var ray = Vector2.FromAngle(i * Mathf.Tau / 8);
                    DrawLine(top + new Vector2(0, -8) + ray * 20, top + new Vector2(0, -8) + ray * 26, new("e5ac6a"), 2);
                }
                break;
            case "breach":
                DrawLine(top - dir * 8, top + dir * 24, new("4a6262"), 24);
                DrawLine(top - side * 9, top + side * 9, new("d3b285"), 5);
                DrawLine(top + dir * 24 - side * 10, top + dir * 24 + side * 10, new("1a2932"), 8);
                break;
            case "echo":
                foreach (var sign in new[] { -1, 1 })
                {
                    var barrel = top + side * 8 * sign;
                    DrawLine(barrel - dir * 7, barrel + dir * 21, new("504763"), 12);
                    DrawLine(barrel - dir * 5, barrel + dir * 17, new("b1a1cf"), 3);
                    DrawCircle(barrel + dir * 21, 4, new("1b2838"));
                }
                break;
        }
    }
}
