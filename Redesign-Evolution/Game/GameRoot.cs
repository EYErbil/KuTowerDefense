using Godot;
using Redesign.Core;
using Redesign.Infrastructure;

namespace Redesign.Game;

public partial class GameRoot : Control
{
    internal static readonly Color Ink = new("0a131d"), Panel = new("111f2a"), Border = new("2b414a"),
        Text = new("e5e6d8"), Muted = new("82999d"), Mint = new("7ed9bb"), Gold = new("edc57b"), Red = new("ed8d83");
    public Simulation Run { get; private set; } = null!;
    public GameDefinition Content { get; private set; } = null!;
    public ArenaCanvas Board { get; private set; } = null!;
    public Button[] BuildButtons { get; private set; } = [];
    public Button PriorityButton { get; private set; } = null!;
    public Button SellButton { get; private set; } = null!;
    public Button StartButton { get; private set; } = null!;
    public Button PauseButton { get; private set; } = null!;
    public Button RestartButton { get; private set; } = null!;
    public Button[] SpeedButtons { get; private set; } = [];
    public Control ResultPanel { get; private set; } = null!;
    public int Speed { get; private set; } = 1;
    public bool Building { get; private set; }
    public int SelectedTower { get; private set; }
    public int BuildType { get; private set; }
    public TowerDefinition BuildDefinition => Content.Towers[BuildType];
    public double Alpha => Run.Phase == RunPhase.Wave && !Run.IsPaused ? clock.Interpolation : 1;
    private SimulationClock clock = new();
    private Label goldLabel = null!, livesLabel = null!, waveLabel = null!, phaseLabel = null!,
        detailTitle = null!, detailBody = null!, notice = null!, resultTitle = null!, resultBody = null!,
        forecastTitle = null!, forecastBody = null!, forecastTip = null!;
    private Button soundButton = null!;
    private AudioCues audio = null!;
    private bool loaded;
    private int frames;
    private string? capturePath;

    public override void _Ready()
    {
        try
        {
            var noticesPath = OS.GetCmdlineUserArgs().FirstOrDefault(a => a.StartsWith("--write-notices="))?[16..];
            if (noticesPath is not null)
            {
                System.IO.File.WriteAllText(noticesPath, Engine.GetLicenseText() + "\n\nComponent copyrights\n" +
                    Json.Stringify(Engine.GetCopyrightInfo(), "  ") + "\n\nComponent licenses\n" + Json.Stringify(Engine.GetLicenseInfo(), "  "));
                GetTree().Quit(); return;
            }
            Content = ContentLoader.Parse(Godot.FileAccess.GetFileAsString("res://Content/emberwatch.json"));
            Run = new Simulation(Content, 42);
            BuildInterface(); loaded = true;
            var args = OS.GetCmdlineUserArgs();
            capturePath = args.FirstOrDefault(a => a.StartsWith("--capture="))?[10..];
            if (args.Contains("--smoke")) AddChild(new SmokeHarness { Game = this });
            if (args.Contains("--demo"))
            {
                Run.Apply(new PlaceTower(1, "boltworks"));
                Run.Apply(new PlaceTower(2, "prism"));
                Run.Apply(new PlaceTower(4, "crucible"));
                // Reach the first escorted wave through ordinary commands for a representative capture.
                while (Run.Phase == RunPhase.Planning && Run.WaveNumber < 4)
                {
                    Run.Apply(new StartWave());
                    if (Run.WaveNumber == 4) break;
                    var budget = 20000;
                    while (Run.Phase == RunPhase.Wave && budget-- > 0) { Run.Step(); Run.DrainEvents(); }
                }
                var prism = Run.Towers.First(t => t.Definition.Id == "prism");
                Run.Apply(new SetTargetPriority(prism.Id, TargetPriority.Support));
                SelectedTower = prism.Id;
                for (var i = 0; i < 360; i++) Run.Step();
                Run.DrainEvents();
            }
            RefreshInterface();
        }
        catch (Exception error)
        {
            GD.PushError(error.ToString());
            AddLabel(this, "Unable to load Emberwatch\n" + error.Message, new(50, 100), new(1300, 500), 24, Red);
            if (OS.GetCmdlineUserArgs().Contains("--smoke")) GetTree().Quit(1);
        }
    }

    private void BuildInterface()
    {
        AddLabel(this, "REDESIGN  /  EVOLUTION", new(30, 22), new(800, 24), 15, Mint);
        AddLabel(this, "EMBERWATCH", new(28, 49), new(740, 55), 40, Text);
        AddLabel(this, "Hold the old road. Keep the frontier alive.", new(30, 108), new(390, 28), 17, Muted);
        forecastTitle = AddLabel(this, "", new(440, 30), new(415, 24), 14, Gold);
        forecastBody = AddLabel(this, "", new(440, 57), new(415, 24), 16, Text);
        forecastTip = AddLabel(this, "", new(440, 88), new(415, 42), 14, Muted);
        forecastTip.AutowrapMode = TextServer.AutowrapMode.WordSmart;
        AddLabel(this, "GOLD", new(900, 30), new(130, 24), 13, Muted);
        goldLabel = AddLabel(this, "", new(900, 53), new(135, 44), 32, Gold);
        AddLabel(this, "CITADEL", new(1080, 30), new(130, 24), 13, Muted);
        livesLabel = AddLabel(this, "", new(1080, 53), new(130, 44), 32, Mint);
        AddLabel(this, "WAVE", new(1260, 30), new(130, 24), 13, Muted);
        waveLabel = AddLabel(this, "", new(1260, 53), new(155, 44), 32, Text);
        AddLabel(this, "01  /  THE OLD ROAD", new(40, 151), new(440, 24), 14, Gold);
        AddLabel(this, "SIX-WAVE DEFENSE", new(772, 151), new(230, 24), 12, Muted);
        Board = new ArenaCanvas { Game = this, Position = new(40, 185), Size = new(960, 640), ClipContents = true };
        AddChild(Board);
        phaseLabel = AddLabel(this, "", new(1052, 174), new(340, 32), 20, Mint);
        PauseButton = AddButton(this, "PAUSE  [SPACE]", new(1052, 212), new(174, 38), TogglePause);
        SpeedButtons = new[] { 1, 2, 4 }.Select((speed, i) => AddButton(this, $"{speed}×", new(1236 + i * 54, 212), new(48, 38), () => SetSpeed(speed))).ToArray();
        AddLabel(this, "BUILD  /  CHOOSE A ROLE", new(1052, 266), new(330, 22), 12, Muted);
        var shortcuts = new[] { "B", "P", "C" };
        BuildButtons = Content.Towers.Select((tower, i) =>
        {
            var button = AddButton(this, $"{tower.Name.ToUpperInvariant()}    {tower.Cost} G    [{shortcuts[i]}]\n{tower.Description}",
                new(1052, 292 + i * 62), new(336, 54), () => ChooseBuild(i));
            button.AddThemeFontSizeOverride("font_size", 14);
            button.TooltipText = $"{tower.Damage} {tower.DamageKind.ToString().ToLowerInvariant()} damage every {tower.CooldownTicks / 60d:0.00}s. Range {tower.Range:0}.";
            return button;
        }).ToArray();
        detailTitle = AddLabel(this, "", new(1052, 490), new(336, 28), 20, Text);
        detailBody = AddLabel(this, "", new(1052, 530), new(336, 72), 15, Muted);
        detailBody.AutowrapMode = TextServer.AutowrapMode.WordSmart;
        PriorityButton = AddButton(this, "TARGET: FIRST  [T]", new(1052, 612), new(336, 36), CyclePriority);
        PriorityButton.TooltipText = "First: nearest the gate. Strongest: highest current health. Support: Wardens first, then nearest the gate.";
        SellButton = AddButton(this, "SELL", new(1052, 658), new(336, 36), SellSelected);
        StartButton = AddButton(this, "START WAVE  [N]", new(1052, 720), new(336, 58), StartNextWave, true);
        soundButton = AddButton(this, "SOUND ON  [M]", new(1052, 793), new(336, 28), ToggleSound);
        notice = AddLabel(this, "Choose a tower card, then a marked foundation. Read the forecast before starting.", new(40, 848), new(1360, 34), 16, Text);
        audio = new AudioCues(); AddChild(audio);

        var result = new Panel { Position = new(340, 258), Size = new(760, 384), Visible = false };
        result.AddThemeStyleboxOverride("panel", Box(new("12252d"), Mint));
        AddChild(result); ResultPanel = result;
        AddLabel(result, "EMBERWATCH  /  AFTER ACTION", new(40, 30), new(680, 30), 14, Gold);
        resultTitle = AddLabel(result, "", new(40, 78), new(680, 66), 43, Text);
        resultBody = AddLabel(result, "", new(40, 164), new(680, 105), 20, Muted);
        RestartButton = AddButton(result, "DEFEND AGAIN  [R]", new(40, 290), new(680, 54), Restart, true);
    }

    public override void _Process(double delta)
    {
        if (!loaded) return;
        clock.Advance(Run, delta, Speed);
        foreach (var e in Run.DrainEvents()) { Board.Receive(e); audio.Receive(e); }
        RefreshInterface(); Board.QueueRedraw(); QueueRedraw();
        if (capturePath is not null && ++frames == 25)
        {
            var error = GetViewport().GetTexture().GetImage().SavePng(capturePath);
            GD.Print($"CAPTURE {error}: {capturePath}"); GetTree().Quit(error == Error.Ok ? 0 : 1);
        }
    }

    public override void _Draw()
    {
        DrawRect(new(0, 0, 1440, 900), Ink);
        DrawLine(new(30, 137), new(1410, 137), Border, 1);
        DrawRect(new(28, 178, 984, 658), new Color("15262b"));
        DrawRect(new(28, 178, 984, 658), Border, false, 1);
        DrawRect(new(1032, 156, 378, 680), Panel);
        DrawRect(new(1032, 156, 378, 680), Border, false, 1);
        DrawLine(new(1052, 478), new(1388, 478), Border, 1);
        DrawLine(new(870, 31), new(870, 100), Border, 1);
        DrawLine(new(1050, 31), new(1050, 100), Border, 1);
        DrawLine(new(1230, 31), new(1230, 100), Border, 1);
    }

    public void RefreshInterface()
    {
        if (!loaded) return;
        goldLabel.Text = Run.Gold.ToString(); livesLabel.Text = $"{Run.Lives} / {Content.StartingLives}";
        waveLabel.Text = $"{Run.WaveNumber:00} / {Content.Waves.Length:00}";
        var ended = Run.Phase is RunPhase.Victory or RunPhase.Defeat;
        phaseLabel.Text = Run.Phase switch
        {
            RunPhase.Planning => "PLAN YOUR DEFENSE", RunPhase.Wave when Run.IsPaused => "TACTICAL PAUSE",
            RunPhase.Wave => Run.Enemies.Count + Run.PendingSpawns == 1 ? "1 ENEMY REMAINS" : $"{Run.Enemies.Count + Run.PendingSpawns} ENEMIES REMAIN", RunPhase.Victory => "FRONTIER SECURED", _ => "CITADEL LOST"
        };
        var forecastIndex = Math.Clamp(Run.Phase == RunPhase.Planning ? Run.WaveNumber : Run.WaveNumber - 1, 0, Content.Waves.Length - 1);
        var forecast = Content.Waves[forecastIndex];
        forecastTitle.Text = $"{(Run.Phase == RunPhase.Planning ? "NEXT" : "WAVE")} / {forecastIndex + 1:00}  {forecast.Title.ToUpperInvariant()}";
        forecastBody.Text = string.Join("  ·  ", forecast.Groups.GroupBy(g => g.EnemyId).Select(group =>
        {
            var count = group.Sum(g => g.Count);
            return $"{count} {Content.Enemies.First(e => e.Id == group.Key).Name}{(count == 1 ? "" : "s")}";
        }));
        forecastTip.Text = forecast.Briefing;
        PauseButton.Disabled = Run.Phase != RunPhase.Wave;
        PauseButton.Text = Run.IsPaused ? "RESUME  [SPACE]" : "PAUSE  [SPACE]";
        StartButton.Disabled = Run.Phase != RunPhase.Planning;
        StartButton.Text = Run.Phase == RunPhase.Planning ? $"START WAVE {Run.WaveNumber + 1}  [N]" : "WAVE IN PROGRESS";
        for (var i = 0; i < BuildButtons.Length; i++)
        {
            BuildButtons[i].Disabled = ended;
            BuildButtons[i].Modulate = Building && BuildType == i ? Mint : Run.Gold >= Content.Towers[i].Cost ? Colors.White : Muted;
        }
        for (var i = 0; i < SpeedButtons.Length; i++) SpeedButtons[i].Modulate = Speed == (1 << i) ? Mint : Colors.White;
        var tower = Run.Towers.FirstOrDefault(t => t.Id == SelectedTower);
        SellButton.Visible = PriorityButton.Visible = tower is not null && !ended;
        if (tower is not null)
        {
            var spec = tower.Definition;
            detailTitle.Text = $"{spec.Name.ToUpperInvariant()} #{tower.Id}";
            detailBody.Text = $"{spec.Damage} {spec.DamageKind.ToString().ToLowerInvariant()}  /  {spec.CooldownTicks / 60d:0.00}s  /  range {spec.Range:0}\n{tower.DamageDealt} damage dealt  ·  {tower.Kills} stopped\n{PriorityDescription(tower.Priority)}";
            SellButton.Text = $"SELL  /  +{tower.SellValue} GOLD";
            PriorityButton.Text = $"TARGET: {tower.Priority.ToString().ToUpperInvariant()}  [T]";
        }
        else
        {
            detailTitle.Text = Building ? $"{BuildDefinition.Name.ToUpperInvariant()}  /  {BuildDefinition.Cost} G" : "READ THE BATTLEFIELD";
            detailBody.Text = Building
                ? $"{BuildDefinition.Damage} {BuildDefinition.DamageKind.ToString().ToLowerInvariant()}  /  {BuildDefinition.CooldownTicks / 60d:0.00}s  /  range {BuildDefinition.Range:0}\nHover a foundation to preview range.\nEscape cancels placement."
                : "Boltworks covers the road. Prism defeats armor. Crucible breaks groups.\nSelect a tower to set its target priority.";
        }
        ResultPanel.Visible = ended;
        if (ended)
        {
            resultTitle.Text = Run.Phase == RunPhase.Victory ? "THE FRONTIER HOLDS." : "THE CITADEL HAS FALLEN.";
            resultTitle.AddThemeColorOverride("font_color", Run.Phase == RunPhase.Victory ? Mint : Red);
            resultBody.Text = $"{Run.Kills} stopped    /    {Run.Leaks} breached    /    {Run.Lives} lives remain\n{Run.DamageDealt} damage dealt    ·    {Run.GoldEarned} gold earned\n{Run.WaveNumber} waves faced    ·    {Run.Tick / 60d:0.0}s simulated";
        }
    }

    private static string PriorityDescription(TargetPriority priority) => priority switch
    {
        TargetPriority.Strongest => "Focus the highest current health.",
        TargetPriority.Support => "Focus Wardens; otherwise nearest the gate.",
        _ => "Focus the enemy nearest the gate."
    };

    public void ChooseBuild(int type = 0)
    {
        if (Run.Phase is RunPhase.Victory or RunPhase.Defeat || type < 0 || type >= Content.Towers.Length) return;
        BuildType = type; Building = true; SelectedTower = 0;
        Message($"{BuildDefinition.Name}: {BuildDefinition.Description} Choose a foundation."); RefreshInterface();
    }
    public void SelectFoundation(int index)
    {
        if (Run.Phase is RunPhase.Victory or RunPhase.Defeat) return;
        var tower = Run.Towers.FirstOrDefault(t => t.Foundation == index);
        if (tower is not null) { SelectedTower = tower.Id; Building = false; Message("Tower selected. Its range is shown on the battlefield."); }
        else if (Building)
        {
            var result = Run.Apply(new PlaceTower(index, BuildDefinition.Id)); Message(result.Message);
            if (result.Success) { SelectedTower = result.EntityId; Building = false; }
        }
        else Message("Choose Boltworks [B], Prism [P] or Crucible [C], then an empty foundation.");
        RefreshInterface();
    }
    private void SellSelected() { Message(Run.Apply(new SellTower(SelectedTower)).Message); SelectedTower = 0; RefreshInterface(); }
    private void CyclePriority()
    {
        var tower = Run.Towers.FirstOrDefault(t => t.Id == SelectedTower);
        if (tower is null) return;
        Message(Run.Apply(new SetTargetPriority(tower.Id, (TargetPriority)(((int)tower.Priority + 1) % 3))).Message);
        RefreshInterface();
    }
    private void StartNextWave() { Message(Run.Apply(new StartWave()).Message); RefreshInterface(); }
    private void TogglePause() { Message(Run.Apply(new Pause(!Run.IsPaused)).Message); RefreshInterface(); }
    private void SetSpeed(int speed) { Speed = speed; Message($"Simulation speed: {speed}×"); }
    private void ToggleSound() { audio.Muted = !audio.Muted; if (audio.Muted) audio.StopAll(); soundButton.Text = audio.Muted ? "SOUND OFF  [M]" : "SOUND ON  [M]"; }
    private void Message(string text) => notice.Text = text;
    public void Restart()
    {
        Run = new Simulation(Content, 42); clock = new(); Speed = 1; Building = false; SelectedTower = 0; BuildType = 0;
        Board.ClearEffects(); audio.StopAll(); Message("A fresh defense. Read the forecast and choose your tower mix."); RefreshInterface();
    }
    public override void _UnhandledKeyInput(InputEvent @event)
    {
        if (!loaded || @event is not InputEventKey { Pressed: true, Echo: false } key) return;
        switch (key.PhysicalKeycode)
        {
            case Key.B: ChooseBuild(0); break;
            case Key.P: ChooseBuild(1); break;
            case Key.C: ChooseBuild(2); break;
            case Key.T: CyclePriority(); break;
            case Key.N: StartNextWave(); break;
            case Key.Space: TogglePause(); break;
            case Key.Key1: SetSpeed(1); break;
            case Key.Key2: SetSpeed(2); break;
            case Key.Key4: SetSpeed(4); break;
            case Key.M: ToggleSound(); break;
            case Key.Escape: Building = false; SelectedTower = 0; Message("Placement cancelled."); break;
            case Key.R: if (Run.Phase is RunPhase.Victory or RunPhase.Defeat) Restart(); break;
            default: return;
        }
        GetViewport().SetInputAsHandled();
    }
    public override void _Notification(int what)
    {
        if (loaded && what == NotificationWMWindowFocusOut && Run.Phase == RunPhase.Wave)
        { Run.Apply(new Pause(true)); Message("Paused while the window is inactive."); }
    }
    internal static Label AddLabel(Control parent, string text, Vector2 position, Vector2 size, int fontSize, Color color)
    {
        var label = new Label { Text = text, Position = position, Size = size, MouseFilter = MouseFilterEnum.Ignore };
        label.AddThemeFontSizeOverride("font_size", fontSize); label.AddThemeColorOverride("font_color", color); parent.AddChild(label); return label;
    }
    private static Button AddButton(Control parent, string text, Vector2 position, Vector2 size, Action action, bool primary = false)
    {
        var button = new Button { Text = text, Position = position, Size = size, FocusMode = FocusModeEnum.None, MouseDefaultCursorShape = CursorShape.PointingHand };
        button.AddThemeFontSizeOverride("font_size", 16);
        button.AddThemeColorOverride("font_color", primary ? Ink : Text);
        button.AddThemeColorOverride("font_hover_color", primary ? Ink : Text);
        button.AddThemeColorOverride("font_pressed_color", primary ? Ink : Text);
        button.AddThemeStyleboxOverride("normal", Box(primary ? Mint : new("1a3039"), primary ? Mint : Border));
        button.AddThemeStyleboxOverride("hover", Box(primary ? new("a3ecd2") : new("28444c"), Mint));
        button.AddThemeStyleboxOverride("pressed", Box(primary ? new("65bfa5") : new("12252b"), Mint));
        button.AddThemeStyleboxOverride("disabled", Box(new("122129"), new("1c3037")));
        button.Pressed += action; parent.AddChild(button); return button;
    }
    private static StyleBoxFlat Box(Color fill, Color edge) => new()
    {
        BgColor = fill, BorderColor = edge, BorderWidthLeft = 1, BorderWidthRight = 1, BorderWidthTop = 1, BorderWidthBottom = 1,
        CornerRadiusTopLeft = 3, CornerRadiusTopRight = 3, CornerRadiusBottomLeft = 3, CornerRadiusBottomRight = 3,
        ContentMarginLeft = 12, ContentMarginRight = 12
    };
}
