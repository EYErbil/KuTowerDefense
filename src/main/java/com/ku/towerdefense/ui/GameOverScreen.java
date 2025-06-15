package com.ku.towerdefense.ui;

import com.ku.towerdefense.controller.GameController;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Dedicated game over screen with enhanced styling and animations.
 */
public class GameOverScreen extends VBox {
    private final Stage primaryStage;
    private final GameController gameController;
    private final boolean playerWon;
    private final int finalWave;
    private final int totalWaves;
    private final int finalGold;
    private final int finalLives;

    /**
     * Constructor for the game over screen.
     *
     * @param primaryStage   the primary stage
     * @param gameController the game controller with final game state
     */
    public GameOverScreen(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;

        // Capture final game state
        this.playerWon = gameController.getCurrentWave() >= gameController.getTotalWaves();
        this.finalWave = gameController.getCurrentWave();
        this.totalWaves = gameController.getTotalWaves();
        this.finalGold = gameController.getPlayerGold();
        this.finalLives = gameController.getPlayerLives();

        // Make the window properly resizable with minimum dimensions
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        initializeUI();
        startAnimations();

        // Bind the size of this VBox to the scene
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                prefWidthProperty().bind(newScene.widthProperty());
                prefHeightProperty().bind(newScene.heightProperty());
            }
        });
    }

    /**
     * Initialize the user interface components.
     */
    private void initializeUI() {
        // Set spacing and alignment
        setSpacing(30);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(50));
        getStyleClass().add("game-over-screen");

        // Create main content container
        VBox mainContent = createMainContent();
        getChildren().add(mainContent);
    }

    /**
     * Create the main content container with all UI elements.
     */
    private VBox createMainContent() {
        VBox content = new VBox(40);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(700);
        content.getStyleClass().add("game-over-content");

        // Victory/Defeat banner
        StackPane bannerPane = createBanner();

        // Game title with result
        Text resultTitle = createResultTitle();

        // Status message
        Label statusMessage = createStatusMessage();

        // Statistics panel
        VBox statisticsPanel = createStatisticsPanel();

        // Action buttons
        HBox buttonPanel = createButtonPanel();

        content.getChildren().addAll(bannerPane, resultTitle, statusMessage, statisticsPanel, buttonPanel);
        return content;
    }

    /**
     * Create the victory/defeat banner with ribbon styling.
     */
    private StackPane createBanner() {
        StackPane bannerPane = new StackPane();
        bannerPane.setPrefWidth(600);
        bannerPane.setPrefHeight(80);

        // Use appropriate ribbon based on result
        String ribbonImageName = playerWon ? "Ribbon_Yellow" : "Ribbon_Red";
        Image ribbonImage = UIAssets.getImage(ribbonImageName);

        if (ribbonImage != null) {
            ImageView ribbonBg = new ImageView(ribbonImage);
            ribbonBg.setFitWidth(600);
            ribbonBg.setFitHeight(80);
            ribbonBg.setPreserveRatio(false);
            bannerPane.getChildren().add(ribbonBg);
        }

        // Banner text
        Label bannerText = new Label(playerWon ? "🏆 VICTORY ACHIEVED! 🏆" : "⚔️ DEFEAT ⚔️");
        bannerText.getStyleClass().add("game-over-banner");
        bannerText.setStyle(
                "-fx-font-family: 'Serif';" +
                        "-fx-font-size: 32px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + (playerWon ? "#2E7D32" : "#C62828") + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.8), 3, 0, 1, 1);");

        bannerPane.getChildren().add(bannerText);
        return bannerPane;
    }

    /**
     * Create the result title text.
     */
    private Text createResultTitle() {
        String titleText = playerWon ? "Outstanding! The Kingdom Stands Victorious!" : "The Kingdom Has Fallen...";

        Text title = new Text(titleText);
        title.getStyleClass().add("game-over-title");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setWrappingWidth(600);

        return title;
    }

    /**
     * Create the status message label.
     */
    private Label createStatusMessage() {
        String message = playerWon
                ? "🎊 You have successfully defended the kingdom against all enemy waves! Your strategic prowess has saved the realm! 🎊"
                : "💔 The enemy forces have overwhelmed your defenses. Study their tactics and return stronger to reclaim the kingdom! 💔";

        Label statusLabel = new Label(message);
        statusLabel.getStyleClass().add("game-over-message");
        statusLabel.setWrapText(true);
        statusLabel.setTextAlignment(TextAlignment.CENTER);
        statusLabel.setMaxWidth(550);
        statusLabel.setPadding(new Insets(20));

        return statusLabel;
    }

    /**
     * Create the statistics panel with game results.
     */
    private VBox createStatisticsPanel() {
        VBox statsPanel = new VBox(20);
        statsPanel.setAlignment(Pos.CENTER);
        statsPanel.getStyleClass().add("statistics-panel");
        statsPanel.setPadding(new Insets(30));
        statsPanel.setMaxWidth(500);

        // Statistics title with medieval scroll styling
        StackPane statsHeaderPane = new StackPane();
        statsHeaderPane.setPrefWidth(450);
        statsHeaderPane.setPrefHeight(60);

        // Use blue ribbon for stats header
        Image blueRibbon = UIAssets.getImage("Ribbon_Blue");
        if (blueRibbon != null) {
            ImageView statsRibbonBg = new ImageView(blueRibbon);
            statsRibbonBg.setFitWidth(450);
            statsRibbonBg.setFitHeight(60);
            statsRibbonBg.setPreserveRatio(false);
            statsHeaderPane.getChildren().add(statsRibbonBg);
        }

        Label statsTitle = new Label("📊 BATTLE CHRONICLE 📊");
        statsTitle.setStyle(
                "-fx-font-family: 'Serif';" +
                        "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #F5DEB3;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.8), 2, 0, 1, 1);");
        statsHeaderPane.getChildren().add(statsTitle);

        // Individual statistics
        VBox statsContent = new VBox(15);
        statsContent.setAlignment(Pos.CENTER);
        statsContent.setPadding(new Insets(20));
        statsContent.setStyle(
                "-fx-background-color: rgba(139, 69, 19, 0.3);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #8B4513;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-effect: innershadow(gaussian, rgba(0, 0, 0, 0.5), 5, 0, 0, 2);");

        // Create stat items
        HBox waveStats = createStatItem("🌊", "Waves Survived", finalWave + "/" + totalWaves,
                playerWon ? "#4CAF50" : "#FF9800");
        HBox goldStats = createStatItem("💰", "Final Treasury", String.valueOf(finalGold), "#FFD700");
        HBox livesStats = createStatItem("❤️", "Lives Remaining", String.valueOf(finalLives),
                finalLives > 0 ? "#4CAF50" : "#F44336");

        // Performance rating
        String rating = calculatePerformanceRating();
        HBox ratingStats = createStatItem("⭐", "Performance", rating, "#9C27B0");

        statsContent.getChildren().addAll(waveStats, goldStats, livesStats, ratingStats);
        statsPanel.getChildren().addAll(statsHeaderPane, statsContent);

        return statsPanel;
    }

    /**
     * Create a styled statistic item.
     */
    private HBox createStatItem(String icon, String label, String value, String valueColor) {
        HBox statBox = new HBox(15);
        statBox.setAlignment(Pos.CENTER_LEFT);
        statBox.setPadding(new Insets(5));

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");
        iconLabel.setPrefWidth(30);

        Label labelText = new Label(label + ":");
        labelText.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #E0E0E0;" +
                        "-fx-font-weight: normal;" +
                        "-fx-font-family: 'Serif';");
        labelText.setPrefWidth(150);

        Label valueText = new Label(value);
        valueText.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: " + valueColor + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Serif';");

        statBox.getChildren().addAll(iconLabel, labelText, valueText);
        return statBox;
    }

    /**
     * Calculate performance rating based on game results.
     */
    private String calculatePerformanceRating() {
        if (playerWon) {
            if (finalLives >= 15)
                return "Legendary";
            else if (finalLives >= 10)
                return "Excellent";
            else if (finalLives >= 5)
                return "Good";
            else
                return "Victory";
        } else {
            double waveProgress = (double) finalWave / totalWaves;
            if (waveProgress >= 0.8)
                return "Valiant";
            else if (waveProgress >= 0.5)
                return "Decent";
            else if (waveProgress >= 0.25)
                return "Learning";
            else
                return "Novice";
        }
    }

    /**
     * Create the button panel with action buttons.
     */
    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(30);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(20, 0, 0, 0));

        // Play Again button
        Button playAgainButton = createGameOverButton("🔄 Play Again", "#1976D2", "#2196F3");
        playAgainButton.setOnAction(e -> playAgain());

        // Main Menu button
        Button mainMenuButton = createGameOverButton("🏠 Main Menu", "#F57C00", "#FF9800");
        mainMenuButton.setOnAction(e -> returnToMainMenu());

        // Quit Game button
        Button quitButton = createGameOverButton("🚪 Quit Game", "#D32F2F", "#F44336");
        quitButton.setOnAction(e -> quitGame());

        buttonPanel.getChildren().addAll(playAgainButton, mainMenuButton, quitButton);
        return buttonPanel;
    }

    /**
     * Create a styled game over button.
     */
    private Button createGameOverButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefWidth(160);
        button.setPrefHeight(55);
        button.getStyleClass().add("game-over-button");

        // Medieval button styling
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + baseColor + ", derive(" + baseColor + ", -20%));"
                        +
                        "-fx-text-fill: #F5DEB3;" +
                        "-fx-font-family: 'Serif';" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: derive(" + baseColor + ", -30%);" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 6, 0, 1, 3);");

        // Hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, " + hoverColor + ", derive(" + hoverColor
                            + ", -20%));" +
                            "-fx-text-fill: #FFFACD;" +
                            "-fx-font-family: 'Serif';" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-border-color: derive(" + hoverColor + ", -30%);" +
                            "-fx-border-width: 3px;" +
                            "-fx-border-radius: 10px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.8), 8, 0, 1, 4);" +
                            "-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, " + baseColor + ", derive(" + baseColor
                            + ", -20%));" +
                            "-fx-text-fill: #F5DEB3;" +
                            "-fx-font-family: 'Serif';" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-border-color: derive(" + baseColor + ", -30%);" +
                            "-fx-border-width: 3px;" +
                            "-fx-border-radius: 10px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 6, 0, 1, 3);" +
                            "-fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });

        return button;
    }

    /**
     * Start entrance animations for the screen.
     */
    private void startAnimations() {
        // Initial state - invisible and scaled down
        setOpacity(0.0);
        setScaleX(0.8);
        setScaleY(0.8);

        // Create entrance animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(600), this);
        scaleUp.setFromX(0.8);
        scaleUp.setFromY(0.8);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition entrance = new ParallelTransition(fadeIn, scaleUp);
        entrance.setDelay(Duration.millis(200));
        entrance.play();

        // Add subtle pulsing animation to the banner
        Timeline pulseAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(getChildren().get(0).scaleXProperty(), 1.0)),
                new KeyFrame(Duration.seconds(2), new KeyValue(getChildren().get(0).scaleXProperty(), 1.02)),
                new KeyFrame(Duration.seconds(4), new KeyValue(getChildren().get(0).scaleXProperty(), 1.0)));
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
        pulseAnimation.play();
    }

    /**
     * Helper method to transition to a new scene with fade effect.
     */
    private void transitionToScene(Scene newScene) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            try {
                String css = getClass().getResource("/css/style.css").toExternalForm();
                newScene.getStylesheets().add(css);
            } catch (Exception e) {
                System.err.println("Could not load stylesheet: " + e.getMessage());
            }

            ImageCursor customCursor = UIAssets.getCustomCursor();
            if (customCursor != null) {
                newScene.setCursor(customCursor);
            }

            Platform.runLater(() -> {
                String originalHint = primaryStage.getFullScreenExitHint();
                primaryStage.setFullScreenExitHint("");

                primaryStage.setFullScreen(false);
                primaryStage.setScene(newScene);

                UIAssets.enforceCustomCursor(newScene);
                UIAssets.startCursorEnforcement(newScene);

                Platform.runLater(() -> {
                    primaryStage.setFullScreen(true);
                    primaryStage.setFullScreenExitHint(originalHint);
                });
            });

            if (newScene.getRoot() != null) {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        });
        fadeOut.play();
    }

    /**
     * Action to play again - return to map selection.
     */
    private void playAgain() {
        MapSelectionScreen mapSelection = new MapSelectionScreen(primaryStage);
        double w = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double h = javafx.stage.Screen.getPrimary().getBounds().getHeight();
        Scene mapSelectionScene = new Scene(mapSelection, w, h);
        transitionToScene(mapSelectionScene);
    }

    /**
     * Action to return to main menu.
     */
    private void returnToMainMenu() {
        MainMenuScreen mainMenu = new MainMenuScreen(primaryStage);
        double w = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double h = javafx.stage.Screen.getPrimary().getBounds().getHeight();
        Scene mainMenuScene = new Scene(mainMenu, w, h);
        transitionToScene(mainMenuScene);
    }

    /**
     * Action to quit the game.
     */
    private void quitGame() {
        Platform.exit();
        System.exit(0);
    }
}