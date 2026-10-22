using Godot;
using Redesign.Core;
using Redesign.Infrastructure;

namespace Redesign.Game;

public partial class GameRoot
{
    public Button GarrisonButton { get; private set; } = null!;
    public Button RallyButton { get; private set; } = null!;
    public Button RetreatButton { get; private set; } = null!;
    public bool BuildingGarrison { get; private set; }
    public bool GarrisonSelected { get; private set; }
    public bool Rallying { get; private set; }
    public bool OrdersBlocked => EvolutionPanel.Visible || RewardPanel.Visible || Run.Phase is RunPhase.Victory or RunPhase.Defeat;

    private void BuildSquadInterface()
    {
        GarrisonButton = AddButton(this, "GARRISON [G]", new(1224, 374), new(164, 72), ChooseGarrison);
        GarrisonButton.AddThemeFontSizeOverride("font_size", 13);
        RallyButton = AddButton(this, "RALLY ON ROAD  [F]", new(1052, 612), new(336, 36), BeginRally);
        RetreatButton = AddButton(this, "RETREAT TO GARRISON  [H]", new(1052, 658), new(336, 36), OrderRetreat);
        RallyButton.TooltipText = "Choose a reachable road point. Travel releases enemies and takes time. Orders can be issued while paused.";
        RetreatButton.TooltipText = "Release enemies and return to the Garrison. No healing or replacements during a wave. Rally again to redeploy.";
    }

    private void RefreshSquadInterface(bool ended)
    {
        var garrison = Run.Garrison;
        GarrisonButton.Visible = Content.Garrison is not null;
        GarrisonButton.Disabled = ended;
        var living = garrison?.Guards.Count(g => g.Alive) ?? 0;
        GarrisonButton.Text = garrison is null ? $"GARRISON [G]\n{Content.Garrison?.Cost} G / Hold the road" : $"SHIELDGUARDS [G]\n{living}/3 ready / Select";
        GarrisonButton.Modulate = GarrisonSelected || BuildingGarrison ? Mint : garrison is not null || Run.Gold >= Content.Garrison?.Cost ? Colors.White : Muted;
        GarrisonButton.TooltipText = $"One permanent foundation, {Content.Garrison?.Cost} gold; cannot be sold. Three Shieldguards, one enemy each.\n" +
            "Extra enemies pass. Full recovery between waves, never during combat. Rally under tower coverage. Melee preserves chill/oil for towers.";
        RallyButton.Visible = RetreatButton.Visible = GarrisonSelected && garrison is not null && !ended;
        RallyButton.Text = Rallying ? "CHOOSE ROAD / ESC TO CANCEL" : "RALLY ON ROAD  [F]";
        RetreatButton.Disabled = garrison?.Retreating == true;
        if (BuildingGarrison)
        {
            detailTitle.Text = $"GARRISON / {Content.Garrison!.Cost} G";
            detailBody.Text = "3 guards / one enemy held per guard\nOne permanent foundation. Cannot be sold.\nHover to preview reachable road.\nRecover only between waves.";
        }
        else if (GarrisonSelected && garrison is not null)
        {
            detailTitle.Text = $"SHIELDGUARDS / {living} OF 3 READY";
            var held = garrison.Guards.Count(g => g.EngagedEnemyId != 0);
            var state = Run.Phase == RunPhase.Planning ? "Deploy at wave start" : garrison.Retreating ? "Returning to Garrison" : held > 0 ? $"Holding {held} / 3 enemies" : "Following rally orders";
            detailBody.Text = state + "\n" + string.Join("   ", garrison.Guards.Select((g, i) => $"{i + 1}: {(g.Alive ? g.Health + " HP" : "FALLEN")}")) +
                $"\n{garrison.DamageDealt} damage / {garrison.Kills} stopped\nRecovery between waves. Extra enemies pass.";
        }
        if (ended) { Building = false; BuildingGarrison = false; Rallying = false; }
    }

    private void ClearSquadSelection() { BuildingGarrison = false; GarrisonSelected = false; Rallying = false; }

    public void CancelOrders()
    {
        Building = false; SelectedTower = 0; ClearSquadSelection();
        Message("Orders cancelled. Select a defense or choose a build card."); RefreshInterface();
    }

    public void ChooseGarrison()
    {
        if (OrdersBlocked || Content.Garrison is null) return;
        if (Run.Garrison is not null) { SelectGarrison(); return; }
        Building = BuildingGarrison = true; SelectedTower = 0; GarrisonSelected = Rallying = false;
        Message("Garrison: three guards. Choose a permanent foundation near tower coverage; it cannot be sold."); RefreshInterface();
    }

    public void SelectGarrison()
    {
        if (OrdersBlocked || Run.Garrison is null) return;
        Building = BuildingGarrison = Rallying = false; SelectedTower = 0; GarrisonSelected = true;
        Message("Shieldguards selected. Rally [F], then click the road. Retreat [H] releases enemies; recovery is between waves."); RefreshInterface();
    }

    public void BeginRally()
    {
        if (OrdersBlocked || Run.Garrison is null) return;
        SelectGarrison(); Rallying = true;
        Message("Rally: click the highlighted road. Distant or off-road orders are rejected. Escape or right-click cancels."); RefreshInterface();
    }

    public void OrderRetreat()
    {
        if (OrdersBlocked || Run.Garrison is null) return;
        SelectGarrison(); Message(Run.Apply(new RetreatSquad()).Message); RefreshInterface();
    }

    public bool TryRoadPointer(Vector2 pointer, out Redesign.Core.Point position)
    {
        var p = new Redesign.Core.Point(pointer.X, pointer.Y);
        position = Run.Route.At(Run.Route.NearestProgress(p));
        return pointer.X >= 0 && pointer.Y >= 0 && pointer.X <= Board.Size.X && pointer.Y <= Board.Size.Y &&
            (position - p).LengthSquared <= 26 * 26;
    }

    public void RallyAtPointer(Vector2 pointer)
    {
        if (OrdersBlocked || !Rallying) return;
        if (!TryRoadPointer(pointer, out var position)) { Message("Choose a point on the highlighted road, not the grass."); return; }
        var result = Run.Apply(new RallySquad(position)); Message(result.Message);
        if (result.Success || Run.Garrison is { Retreating: false } g && (position - Run.Route.At(g.RallyProgress)).LengthSquared < .001)
            Rallying = false;
        RefreshInterface();
    }

    private void PrepareSquadDemo(string[] args)
    {
        if (!args.Any(a => a is "--squad-demo" or "--squad-boss-demo" or "--squad-placement-demo")) return;
        if (args.Contains("--squad-placement-demo")) { ChooseGarrison(); Board.SetPreviewPointer(ArenaCanvas.V(Content.Map.Center(Content.Map.Foundations[3]))); return; }
        var bossDemo = args.Contains("--squad-boss-demo");
        var budget = 150000;
        while (budget-- > 0 && Run.Phase is RunPhase.Planning or RunPhase.Wave)
        {
            if (Run.Phase == RunPhase.Planning)
            {
                foreach (var command in ExpeditionStrategy.PlanningCommands(Run, squad: true))
                    if (!Run.Apply(command).Success) throw new InvalidOperationException("Squad demo command failed: " + command);
                Run.Apply(new StartWave());
            }
            Run.Step();
            foreach (var e in Run.DrainEvents()) Board.Receive(e);
            var ready = bossDemo ? Run.Enemies.Any(e => e.Definition.Boss is not null && e.MeleeAtTick > Run.Tick + 15) :
                Run.Garrison?.Guards.Count(g => g.EngagedEnemyId != 0) >= 2;
            if (ready)
            {
                Run.Apply(new Pause(true)); SelectGarrison();
                var speed = args.Contains("--speed=4") ? 4 : args.Contains("--speed=2") ? 2 : 1;
                SetSpeed(speed);
                if (args.Contains("--animate")) Run.Apply(new Pause(false));
                return;
            }
        }
        throw new InvalidOperationException("Squad demo could not reach its encounter.");
    }
}
