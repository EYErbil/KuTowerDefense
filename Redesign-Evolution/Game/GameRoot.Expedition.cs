using Godot;
using Redesign.Core;
using Redesign.Infrastructure;

namespace Redesign.Game;

public partial class GameRoot
{
    public Control RewardPanel { get; private set; } = null!;
    public Button[] RewardButtons { get; private set; } = [];
    public Button ChallengeButton { get; private set; } = null!;
    private Label stageLabel = null!, rewardTitle = null!, rewardHint = null!;
    private Button relicButton = null!, closeRelics = null!;
    private Control rewardCards = null!;

    private void BuildExpeditionInterface()
    {
        RewardPanel = new Control { Size = new(1440, 900), Visible = false, MouseFilter = MouseFilterEnum.Stop }; AddChild(RewardPanel);
        RewardPanel.AddChild(new ColorRect { Size = new(1440, 900), Color = new(.02f, .04f, .06f, .88f), MouseFilter = MouseFilterEnum.Ignore });
        var window = new Panel { Position = new(150, 160), Size = new(1140, 580) };
        window.AddThemeStyleboxOverride("panel", Box(Panel, Gold)); RewardPanel.AddChild(window);
        rewardTitle = AddLabel(window, "", new(28, 24), new(1084, 48), 30, Gold);
        rewardHint = AddLabel(window, "", new(28, 80), new(1084, 64), 16, Text, wrap: true);
        rewardCards = new Control { Position = new(28, 164), Size = new(1084, 330) }; window.AddChild(rewardCards);
        closeRelics = AddButton(window, "RETURN TO THE ROAD  [I / ESC]", new(28, 518), new(1084, 38), CloseRelicReview);
    }

    private void RefreshExpeditionInterface()
    {
        var shownWave = Math.Clamp(Run.WaveNumber + (Run.Phase == RunPhase.Planning ? 1 : 0), 1, 12);
        var stage = (shownWave - 1) / 3;
        stageLabel.Text = $"{new[] { "I / OUTPOST", "II / CONVOY", "III / BREACH", "IV / LAST LIGHT" }[stage]}    ·    " +
            string.Join("  ", Enumerable.Range(1, 12).Select(w => w <= Run.WaveReports.Count ? "◆" : w == shownWave ? $"[{w}]" : w.ToString()));
        relicButton.Text = $"RELICS / {Run.Relics.Count}  [I]";
        relicButton.TooltipText = $"Seed {Run.Seed}\n" + (Run.Relics.Count == 0 ? "Earn a choice after waves 3, 6 and 9." : string.Join("\n", Run.Relics.Select(r => r.Name + ": " + r.Description)));
        ChallengeButton.Disabled = Run.Phase != RunPhase.Planning || Run.RewardOffers.Count > 0;
        ChallengeButton.Text = Run.Phase == RunPhase.Wave
            ? Run.ChallengeEnabled ? "OVERRUN ACTIVE" : "STANDARD ENCOUNTER"
            : $"OVERRUN {(Run.ChallengeEnabled ? "ON" : "OFF")} / +{Run.ChallengeReward} G";
        ChallengeButton.Modulate = Run.ChallengeEnabled ? Gold : Colors.White;
        ChallengeButton.TooltipText = $"Optional: {Run.ChallengeCount} extra Raiders arrive 1.5s after the wave starts, 0.75s apart.\nEarn {Run.ChallengeReward} bonus gold only if nobody breaches. Raiders give no bounty.\nYou can change this choice until starting the wave. Normal clear reward is always paid on victory.";
        if (Run.Phase == RunPhase.Planning && Run.ChallengeEnabled)
            forecastBody.Text += $"  + {Run.ChallengeCount} Raiders (Overrun)";
        if (Run.RewardOffers.Count > 0 && !RewardPanel.Visible) ShowRelics(true);
    }

    private void ToggleChallenge()
    { Message(Run.Apply(new SetChallenge(!Run.ChallengeEnabled)).Message); RefreshInterface(); }
    private void OpenRelicReview()
    {
        if (EvolutionPanel.Visible) return;
        if (Run.Phase == RunPhase.Wave) Run.Apply(new Pause(true));
        ShowRelics(Run.RewardOffers.Count > 0);
    }
    private void ShowRelics(bool choosing)
    {
        EvolutionPanel.Visible = false; Building = false; ClearSquadSelection();
        var offers = choosing ? Run.RewardOffers : Run.Relics;
        rewardTitle.Text = choosing ? $"WAVE {Run.WaveNumber} HELD / CLAIM A RELIC" : "YOUR EXPEDITION / RELICS";
        rewardHint.Text = choosing ? "Choose one. It lasts for this run and changes existing and future towers. The other two are left behind.\n" +
            $"Your defense: {string.Join(", ", Run.Towers.Select(t => t.Definition.Name).Distinct())}.  {Run.Lives}/{Run.MaxLives} lives · {Run.Gold} gold."
            : $"Seed {Run.Seed} · Rewards after waves 3, 6 and 9. A new expedition brings different offers.\n" +
              (Run.IsPaused ? "Combat is paused. After closing, press Space to resume." : "Combine relics with your paths. Recovering the citadel can be worth more than another attack effect.");
        foreach (var child in rewardCards.GetChildren()) { rewardCards.RemoveChild(child); child.QueueFree(); }
        var buttons = new List<Button>();
        for (var i = 0; i < 3; i++)
        {
            var card = new Panel { Position = new(i * 366, 0), Size = new(350, 324) };
            card.AddThemeStyleboxOverride("panel", Box(new("192c35"), i < offers.Count ? Gold : Border)); rewardCards.AddChild(card);
            if (i >= offers.Count)
            {
                AddLabel(card, $"RELIC {i + 1}", new(22, 24), new(306, 30), 21, Muted);
                AddLabel(card, $"Earned after wave {(i + 1) * 3}", new(22, 120), new(306, 40), 16, Muted); continue;
            }
            var relic = offers[i]; var index = i;
            AddLabel(card, relic.Kind is RelicKind.WarChest or RelicKind.ReinforcedGate ? "SUPPLIES / RECOVERY" : "BUILD EVOLUTION", new(22, 20), new(306, 24), 12, Gold);
            AddLabel(card, relic.Name.ToUpperInvariant(), new(22, 58), new(306, 64), 24, Text, wrap: true);
            AddLabel(card, relic.Description, new(22, 136), new(306, 104), 17, Text, wrap: true);
            if (choosing) buttons.Add(AddButton(card, $"CLAIM RELIC [{i + 1}]", new(22, 264), new(306, 40), () => ClaimReward(index), true));
            else AddLabel(card, "ACTIVE FOR THIS RUN", new(22, 268), new(306, 30), 14, Mint);
        }
        RewardButtons = buttons.ToArray(); closeRelics.Visible = !choosing;
        RewardPanel.Visible = true;
    }
    private void ClaimReward(int index)
    {
        if (index < 0 || index >= Run.RewardOffers.Count) return;
        var result = Run.Apply(new ChooseRelic(Run.RewardOffers[index].Id));
        if (result.Success) RewardPanel.Visible = false;
        Message(result.Message); RefreshInterface();
    }
    private void CloseRelicReview()
    {
        if (Run.RewardOffers.Count > 0) return;
        RewardPanel.Visible = false; RefreshInterface();
    }

    private void PrepareExpeditionDemo(string[] args)
    {
        if (!args.Any(a => a is "--demo" or "--rewards-demo" or "--boss-demo")) return;
        var reward = args.Contains("--rewards-demo"); var targetWave = args.Contains("--boss-demo") ? 12 : 8;
        var budget = 100000;
        while (budget-- > 0 && Run.Phase is RunPhase.Planning or RunPhase.Wave)
        {
            if (reward && Run.RewardOffers.Count > 0) break;
            if (Run.Phase == RunPhase.Planning)
            {
                foreach (var command in ExpeditionStrategy.PlanningCommands(Run))
                    if (!Run.Apply(command).Success) throw new InvalidOperationException("Demo command failed: " + command);
                Run.Apply(new StartWave());
            }
            Run.Step(); var events = Run.DrainEvents();
            var ready = targetWave == 12 ? Run.Enemies.Any(e => e.Definition.Boss is not null && e.WindingUp) : Run.Enemies.Count >= 12 && Run.Enemies.Any(e => e.Definition.Healing is not null && e.WindingUp);
            if (!reward && Run.WaveNumber == targetWave && ready)
            {
                foreach (var e in events) Board.Receive(e);
                Run.Apply(new Pause(true)); SelectedTower = Run.Towers.First(t => t.Foundation == 0).Id; break;
            }
        }
        if (budget <= 0 || Run.Phase is RunPhase.Defeat or RunPhase.Victory) throw new InvalidOperationException("Demo could not reach its encounter.");
    }
}
