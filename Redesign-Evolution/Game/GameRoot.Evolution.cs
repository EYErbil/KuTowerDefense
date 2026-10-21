using Godot;
using Redesign.Core;

namespace Redesign.Game;

public partial class GameRoot
{
    public Button EvolveButton { get; private set; } = null!;
    public Button CloseEvolutionButton { get; private set; } = null!;
    public Control EvolutionPanel { get; private set; } = null!;
    public Button[] BranchButtons { get; private set; } = [];
    public BranchDefinition[] BranchChoices { get; private set; } = [];
    private Label evolutionTitle = null!, evolutionHint = null!;
    private Control branchCards = null!;

    private void BuildEvolutionInterface()
    {
        EvolutionPanel = new Control { Size = new(1440, 900), Visible = false, MouseFilter = MouseFilterEnum.Stop };
        AddChild(EvolutionPanel);
        EvolutionPanel.AddChild(new ColorRect { Color = new(0.02f, .05f, .07f, .84f), Size = new(1440, 900), MouseFilter = MouseFilterEnum.Ignore });
        var window = new Panel { Position = new(240, 180), Size = new(960, 540) };
        window.AddThemeStyleboxOverride("panel", Box(Panel, Mint)); EvolutionPanel.AddChild(window);
        evolutionTitle = AddLabel(window, "", new(28, 24), new(904, 40), 28, Text);
        evolutionHint = AddLabel(window, "", new(28, 72), new(904, 44), 16, Muted);
        var scroll = new ScrollContainer { Position = new(28, 130), Size = new(904, 320), HorizontalScrollMode = ScrollContainer.ScrollMode.Disabled };
        window.AddChild(scroll);
        branchCards = new Control(); scroll.AddChild(branchCards);
        CloseEvolutionButton = AddButton(window, "KEEP CURRENT TOWER  [ESC]", new(28, 474), new(904, 42), CloseEvolution);
    }

    private void OpenEvolution()
    {
        var tower = Run.Towers.FirstOrDefault(t => t.Id == SelectedTower);
        if (tower is null || tower.BranchId is not null || Run.Phase is RunPhase.Victory or RunPhase.Defeat) return;
        BranchChoices = Content.Combat?.Branches.Where(b => b.TowerId == tower.FamilyId).ToArray() ?? [];
        if (BranchChoices.Length == 0) return;
        if (Run.Phase == RunPhase.Wave) Run.Apply(new Pause(true));
        Building = false;
        evolutionTitle.Text = $"EVOLVE / {tower.Definition.Name.ToUpperInvariant()}";
        evolutionHint.Text = $"{Run.Gold} gold available. Choose one permanent specialization for this tower.\n" +
            (Run.Phase == RunPhase.Wave ? "Battle paused. After choosing or closing, press Space to resume." : "Paths change how a tower fights. Pair a setup tower with a finisher.");
        foreach (var child in branchCards.GetChildren()) { branchCards.RemoveChild(child); child.QueueFree(); }
        branchCards.CustomMinimumSize = new(884, ((BranchChoices.Length + 1) / 2) * 302);
        BranchButtons = BranchChoices.Select((branch, i) =>
        {
            var spec = branch.Upgrade;
            var card = new Panel { Position = new(i % 2 * 450, i / 2 * 302), Size = new(434, 286) };
            card.TooltipText = DescribeEffects(spec);
            card.AddThemeStyleboxOverride("panel", Box(new("192e37"), Border)); branchCards.AddChild(card);
            AddLabel(card, spec.Name.ToUpperInvariant(), new(22, 18), new(390, 34), 23, Text);
            AddLabel(card, $"{spec.Damage} {spec.DamageKind.ToString().ToLowerInvariant()} / {spec.CooldownTicks / 60d:0.00}s / range {spec.Range:0}",
                new(22, 62), new(390, 26), 16, Gold);
            var description = AddLabel(card, spec.Description, new(22, 108), new(390, 94), 17, Text);
            description.AutowrapMode = TextServer.AutowrapMode.WordSmart;
            var button = AddButton(card, Run.Gold >= branch.Cost ? $"CHOOSE PATH / {branch.Cost} G" : $"NEED {branch.Cost - Run.Gold} MORE GOLD / {branch.Cost} G",
                new(22, 224), new(390, 42), () => PurchaseBranch(branch.Upgrade.Id), true);
            button.Disabled = Run.Gold < branch.Cost;
            button.TooltipText = card.TooltipText;
            return button;
        }).ToArray();
        EvolutionPanel.Visible = true; RefreshInterface();
    }

    private string DescribeEffects(TowerDefinition spec)
    {
        var lines = new List<string> { spec.Description };
        if (spec.SplashRadius > 0) lines.Add($"Splash: radius {spec.SplashRadius:0}, half damage to other enemies.");
        if (spec.OnHit is { } application && Content.Combat?.Statuses.FirstOrDefault(s => s.Id == application.StatusId) is { } status)
        {
            lines.Add($"{status.Name}: +{application.Stacks} per hit, {status.DurationTicks / 60d:0.0}s per stack, maximum {status.MaxStacks}.");
            if (status.SlowPercentPerStack > 0) lines.Add($"{status.SlowPercentPerStack}% slow per stack; only the strongest slow applies.");
            if (status.ArmorReductionPerStack > 0) lines.Add($"-{status.ArmorReductionPerStack} armor per stack, minimum zero. Applies before this hit.");
        }
        if (spec.Echo is { } echo) lines.Add($"Echo: {echo.Damage} {echo.DamageKind.ToString().ToLowerInvariant()} damage in radius {echo.Radius:0} after {echo.DelayTicks / 60d:0.0}s. Cannot trigger reactions.");
        foreach (var reaction in Content.Combat?.Reactions ?? [])
            if (reaction.Trigger == spec.DamageKind || reaction.StatusId == spec.OnHit?.StatusId)
                lines.Add($"{reaction.Name}: consumes {reaction.RequiredStacks} {reaction.StatusId} on {reaction.Trigger.ToString().ToLowerInvariant()} hit. " +
                    (reaction.Effect == ReactionEffect.Burst ? $"{reaction.Damage} damage in radius {reaction.Radius:0}." :
                        $"{reaction.Damage} damage every {reaction.IntervalTicks / 60d:0.0}s for {reaction.DurationTicks / 60d:0.0}s, radius {reaction.Radius:0}; overlapping fields share a damage timer."));
        return string.Join("\n", lines);
    }

    private void PurchaseBranch(string branchId)
    {
        var result = Run.Apply(new UpgradeTower(SelectedTower, branchId));
        Message(result.Message + (Run.IsPaused ? " Press Space to resume." : ""));
        if (result.Success) EvolutionPanel.Visible = false;
        RefreshInterface();
    }

    private void CloseEvolution()
    {
        EvolutionPanel.Visible = false;
        Message(Run.IsPaused ? "Defense paused. Press Space when your plan is ready." : "Current tower kept. Choose a path whenever you are ready.");
        RefreshInterface();
    }
}
