using Godot;
using Redesign.Core;
using Redesign.Infrastructure;

namespace Redesign.Game;

/// <summary>Opt-in integration exercise through the actual button and board signal bindings.</summary>
public partial class SmokeHarness : Node
{
    public GameRoot Game { get; init; } = null!;
    private int stage;
    private string pausedChecksum = "";
    private bool sawRally, sawEnrage;
    public override void _Process(double delta)
    {
        try
        {
            switch (stage++)
            {
                case 0:
                    ClickFoundation(1, "boltworks"); ClickFoundation(2, "prism");
                    Require(Game.Run.Towers.Count == 2 && Game.Run.Gold == 35, "opening economy and placement bindings");
                    Require(Game.Run.Towers.Single(t => t.Definition.Id == "prism").Priority == TargetPriority.Support, "priority button");
                    Game.EvolveButton.EmitSignal(Button.SignalName.Pressed);
                    Require(Game.EvolutionPanel.Visible && Game.BranchButtons.Length == 2 && Game.BranchButtons.All(b => b.Disabled), "unaffordable paths remain inspectable");
                    Game.CloseEvolutionButton.EmitSignal(Button.SignalName.Pressed);
                    Game.ChallengeButton.EmitSignal(Button.SignalName.Pressed);
                    Require(Game.Run.ChallengeEnabled && Game.Run.ChallengeCount == 2, "optional forecast challenge");
                    Game.ChallengeButton.EmitSignal(Button.SignalName.Pressed);
                    Require(!Game.Run.ChallengeEnabled, "challenge can be declined before starting");
                    Game.StartButton.EmitSignal(Button.SignalName.Pressed);
                    Game.EvolveButton.EmitSignal(Button.SignalName.Pressed);
                    Require(Game.Run.IsPaused && Game.EvolutionPanel.Visible, "path selection pauses combat");
                    Game.CloseEvolutionButton.EmitSignal(Button.SignalName.Pressed);
                    Require(Game.Run.IsPaused && !Game.EvolutionPanel.Visible, "closing paths keeps tactical pause");
                    Game._UnhandledKeyInput(new InputEventKey { Pressed = true, PhysicalKeycode = Key.I });
                    Require(Game.RewardPanel.Visible && Game.RewardButtons.Length == 0 && Game.Run.IsPaused, "owned relic review during combat");
                    Game._UnhandledKeyInput(new InputEventKey { Pressed = true, PhysicalKeycode = Key.Escape });
                    Require(!Game.RewardPanel.Visible && Game.Run.IsPaused, "closing relic review keeps tactical pause");
                    pausedChecksum = Game.Run.Checksum(); break;
                case 1:
                    Require(Game.Run.Checksum() == pausedChecksum, "pause across engine frames");
                    Game.SpeedButtons[2].EmitSignal(Button.SignalName.Pressed);
                    Require(Game.Speed == 4, "speed button");
                    Game.PauseButton.EmitSignal(Button.SignalName.Pressed);
                    CompleteRun(true); Game.RefreshInterface();
                    Require(Game.Run.Phase == RunPhase.Victory && Game.ResultPanel.Visible, "victory presentation"); break;
                case 2:
                    Game.RestartButton.EmitSignal(Button.SignalName.Pressed);
                    Require(Game.Run.Towers.Count == 0 && Game.Run.Tick == 0 && Game.Run.Gold == Game.Content.StartingGold && Game.Run.Seed == 43, "new seed and restart state");
                    Require(Game.Run.Relics.Count == 0 && Game.Run.WaveReports.Count == 0 && !Game.RewardPanel.Visible, "relic and checkpoint reset");
                    CompleteRun(false); Game.RefreshInterface();
                    Require(Game.Run.Phase == RunPhase.Defeat && Game.Run.Lives == 0 && Game.ResultPanel.Visible, "defeat presentation"); break;
                case 3:
                    Game.RestartButton.EmitSignal(Button.SignalName.Pressed);
                    Require(Game.Run.Phase == RunPhase.Planning && !Game.ResultPanel.Visible, "repeat restart");
                    ClickFoundation(1, "boltworks");
                    Game.SellButton.EmitSignal(Button.SignalName.Pressed);
                    Require(Game.Run.Towers.Count == 0 && Game.Run.Gold == Game.Content.StartingGold - 18, "sell binding and refund");
                    ClickFoundation(1, "boltworks"); Require(Game.Run.Towers.Count == 1, "rebuild sold foundation");
                    Game.Restart();
                    Require(Game.Run.Towers.Count == 0 && Game.Run.Tick == 0, "clean roster reset");
                    GD.Print("SMOKE PASS: three build cards, priorities, path affordability/pause, optional challenge toggle, three earned relic selections, boss rally/enrage, twelve-wave victory, defeat, sale, fresh seeds and restart");
                    GetTree().Quit(0); break;
            }
        }
        catch (Exception error) { GD.PushError("SMOKE FAIL: " + error); GetTree().Quit(1); }
    }
    private void ClickFoundation(int i, string type)
    {
        var index = Array.FindIndex(Game.Content.Towers, t => t.Id == type);
        Game.BuildButtons[index].EmitSignal(Button.SignalName.Pressed);
        Game.Board.EmitSignal(Control.SignalName.GuiInput, new InputEventMouseButton
        { ButtonIndex = MouseButton.Left, Pressed = true, Position = ArenaCanvas.V(Game.Content.Map.Center(Game.Content.Map.Foundations[i])) });
        if (type == "prism")
        {
            Game.PriorityButton.EmitSignal(Button.SignalName.Pressed);
            Game.PriorityButton.EmitSignal(Button.SignalName.Pressed);
        }
    }
    private void CompleteRun(bool build)
    {
        var budget = 100000;
        while (Game.Run.Phase is RunPhase.Planning or RunPhase.Wave && budget-- > 0)
        {
            if (Game.Run.Phase == RunPhase.Planning)
            {
                if (build) foreach (var command in ExpeditionStrategy.PlanningCommands(Game.Run)) ApplyThroughInterface(command);
                Game.StartButton.EmitSignal(Button.SignalName.Pressed);
            }
            Game.Run.Step();
            foreach (var e in Game.Run.DrainEvents())
            { sawRally |= e.Kind == EventKind.Rally; sawEnrage |= e.Kind == EventKind.BossEnraged; }
        }
        Require(budget > 0, "no stalled waves");
        if (build)
        {
            Require(Game.Run.Relics.Count == 3 && Game.Run.WaveReports.Count == 12 && !Game.RewardPanel.Visible, "three choices and all twelve waves");
            Require(sawRally && sawEnrage, "boss summons and phase transition reached");
            Require(Game.Run.Towers.Select(t => t.FamilyId).Distinct().Count() == 3, "all three build cards");
        }
    }

    private void ApplyThroughInterface(GameCommand command)
    {
        if (command is ChooseRelic choice)
        {
            Game.RefreshInterface();
            Require(Game.RewardPanel.Visible && Game.RewardButtons.Length == 3 && Game.StartButton.Disabled, "earned choice blocks wave advance");
            var index = Game.Run.RewardOffers.ToList().FindIndex(r => r.Id == choice.RelicId);
            Game.RewardButtons[index].EmitSignal(Button.SignalName.Pressed);
            Require(Game.Run.Relics.Any(r => r.Id == choice.RelicId) && !Game.RewardPanel.Visible && !Game.StartButton.Disabled, "relic button applies and resumes planning");
            return;
        }
        if (command is SetChallenge challenge)
        {
            if (Game.Run.ChallengeEnabled != challenge.Enabled) Game.ChallengeButton.EmitSignal(Button.SignalName.Pressed);
            return;
        }
        if (command is PlaceTower place) { ClickFoundation(place.Foundation, place.TowerId!); return; }
        var towerId = command is UpgradeTower upgrade ? upgrade.TowerId : ((SetTargetPriority)command).TowerId;
        var tower = Game.Run.Towers.Single(t => t.Id == towerId);
        Game.Board.EmitSignal(Control.SignalName.GuiInput, new InputEventMouseButton
        { ButtonIndex = MouseButton.Left, Pressed = true, Position = ArenaCanvas.V(tower.Position) });
        if (command is UpgradeTower evolution)
        {
            Game.EvolveButton.EmitSignal(Button.SignalName.Pressed);
            var index = Array.FindIndex(Game.BranchChoices, b => b.Upgrade.Id == evolution.BranchId);
            Require(Game.EvolutionPanel.Visible && index >= 0 && !Game.BranchButtons[index].Disabled, "branch is affordable and selectable");
            Game.BranchButtons[index].EmitSignal(Button.SignalName.Pressed);
            Require(!Game.EvolutionPanel.Visible && tower.BranchId == evolution.BranchId && !Game.EvolveButton.Visible, "exclusive upgrade closes panel");
        }
        else if (command is SetTargetPriority targeting)
        {
            var attempts = 4;
            while (tower.Priority != targeting.Priority && attempts-- > 0) Game.PriorityButton.EmitSignal(Button.SignalName.Pressed);
            Require(tower.Priority == targeting.Priority, "all priorities reachable");
        }
    }
    private static void Require(bool condition, string scenario) { if (!condition) throw new InvalidOperationException(scenario); }
}
