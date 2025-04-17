package towerdefense.view.screens;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import towerdefense.Main; // For navigation
import towerdefense.controller.GameController; // Import controller
import towerdefense.model.GameModel; // If game state is needed

/**
 * Provides the JavaFX UI components for the Game screen.
 */
public class GameScreen {

    private BorderPane view;
    private Pane gameBoardPane; // Placeholder for game rendering area
    private Label waveLabel;
    private Label goldLabel;
    private Label livesLabel;
    // private GameModel model; // Controller likely manages model interaction
    private GameController controller; // Store the controller

    public GameScreen(GameController controller) {
        this.controller = controller;
        initializeUI();
        // Controller should be started externally (e.g., by Main after screen load)
    }

    private void initializeUI() {
        view = new BorderPane();
        view.setPadding(new Insets(10));
        view.setStyle("-fx-background-color: #e0e0e0;"); // Example background

        // --- Game Board Area (Center) ---
        gameBoardPane = new Pane(); // Use Pane for flexible element positioning
        gameBoardPane.setStyle("-fx-background-color: lightgreen; -fx-border-color: black;"); // Placeholder appearance
        gameBoardPane.setPrefSize(800, 600); // Example size
        // TODO: Add game elements (towers, enemies, path) to this pane
        // TODO: Implement game loop / animation timer to update this pane

        Label gamePlaceholder = new Label("Game Board Area (JavaFX)");
        gamePlaceholder.setFont(Font.font(24));
        gameBoardPane.getChildren().add(gamePlaceholder); // Add label temporarily
        // Center the placeholder label (simple centering)
        gamePlaceholder.layoutXProperty()
                .bind(gameBoardPane.widthProperty().subtract(gamePlaceholder.widthProperty()).divide(2));
        gamePlaceholder.layoutYProperty()
                .bind(gameBoardPane.heightProperty().subtract(gamePlaceholder.heightProperty()).divide(2));

        view.setCenter(gameBoardPane);
        BorderPane.setMargin(gameBoardPane, new Insets(0, 10, 0, 0));

        // --- Control Panel (Right) ---
        VBox controlPanel = new VBox(15);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setStyle("-fx-border-color: grey; -fx-border-width: 1; -fx-background-color: #f0f0f0;");
        controlPanel.setPrefWidth(180);

        Label controlsTitle = new Label("Controls");
        controlsTitle.setFont(Font.font("Arial", 16));
        controlsTitle.setUnderline(true);

        waveLabel = new Label("Wave: 0/0");
        goldLabel = new Label("Gold: 0");
        livesLabel = new Label("Lives: 0");

        Button pauseButton = new Button("Pause");
        Button speedButton = new Button("Speed x1"); // Toggle speed
        Button optionsButton = new Button("Options");
        Button quitButton = new Button("Quit to Menu");

        // Wire actions to controller
        pauseButton.setOnAction(e -> controller.handlePauseToggle());
        speedButton.setOnAction(e -> {
            controller.handleSpeedToggle();
            // TODO: Update speedButton text based on controller state
        });
        optionsButton.setOnAction(e -> Main.loadOptionsScreen()); // Navigate to options
        quitButton.setOnAction(e -> {
            controller.stopGame(); // Stop game loop before leaving
            Main.loadMainMenuScreen();
        });

        controlPanel.getChildren().addAll(
                controlsTitle, waveLabel, goldLabel, livesLabel,
                new Separator(),
                pauseButton, speedButton, optionsButton,
                new Separator(),
                quitButton);
        view.setRight(controlPanel);

        // --- Tower Selection Panel (Bottom) ---
        HBox towerPanel = new HBox(10);
        towerPanel.setPadding(new Insets(10));
        towerPanel.setAlignment(Pos.CENTER);
        towerPanel.setStyle("-fx-border-color: grey; -fx-border-width: 1; -fx-background-color: #f0f0f0;");

        towerPanel.getChildren().add(new Label("Towers:"));
        // TODO: Add actual tower buttons/icons
        for (int i = 1; i <= 3; i++) {
            Button towerButton = new Button("Tower " + i);
            towerButton.setPrefSize(80, 50);
            final int towerType = i;
            // Wire action to controller
            towerButton.setOnAction(e -> controller.handleTowerSelection(towerType));
            towerPanel.getChildren().add(towerButton);
        }

        view.setBottom(towerPanel);

        // Initial UI update is now handled by controller/game loop
        // updateUI(); // Remove initial call from here
    }

    /**
     * Updates UI elements like labels (call this from controller or game loop).
     * Ensures update happens on JavaFX Application Thread.
     */
    public void updateInfoLabels(int currentWave, int totalWaves, int gold, int lives) {
        Platform.runLater(() -> {
            waveLabel.setText(String.format("Wave: %d/%d", currentWave, totalWaves));
            goldLabel.setText(String.format("Gold: %d", gold));
            livesLabel.setText(String.format("Lives: %d", lives));
        });
    }

    /**
     * Placeholder method for redrawing the game board (enemies, towers, etc.).
     * This should be called by the controller/game loop.
     */
    public void redrawGameBoard(/* Potentially pass necessary game state */) {
        Platform.runLater(() -> {
            // TODO: Clear gameBoardPane and redraw all elements based on model state
            // - Draw map background/path
            // - Draw enemies at their current positions
            // - Draw towers
            // - Draw projectiles
            System.out.println("View: Redrawing game board (placeholder)");
        });
    }

    /**
     * Returns the root node of the game screen UI.
     */
    public Parent getView() {
        if (view == null) {
            initializeUI();
        }
        return view;
    }
}