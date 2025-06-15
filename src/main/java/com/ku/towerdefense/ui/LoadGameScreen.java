package com.ku.towerdefense.ui;

import com.ku.towerdefense.controller.GameController;
import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.service.GameSaveService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * A dedicated, game-like screen for loading saved games.
 */
public class LoadGameScreen extends VBox {
    private final Stage primaryStage;
    private final GameSaveService saveService;
    private GameSaveService.SaveFileInfo selectedSave = null;
    private VBox saveSlotsContainer;
    private Button loadButton;

    public LoadGameScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.saveService = GameSaveService.getInstance();

        initializeUI();

        // Bind size to scene for background
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                prefWidthProperty().bind(newScene.widthProperty());
                prefHeightProperty().bind(newScene.heightProperty());
            }
        });
    }

    private void initializeUI() {
        setSpacing(25);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        getStyleClass().add("main-menu-layout"); // Reuse main menu background

        // Title
        Text title = new Text("📜 Royal Archives 📜");
        title.getStyleClass().add("menu-title");

        // Container for save slots
        saveSlotsContainer = new VBox(15);
        saveSlotsContainer.setAlignment(Pos.CENTER);
        saveSlotsContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(saveSlotsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);
        scrollPane.setMaxHeight(450);
        scrollPane.getStyleClass().add("medieval-scroll-pane"); // For custom styling
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        populateSaveSlots();

        // Action buttons
        HBox buttonPanel = createButtonPanel();

        getChildren().addAll(title, scrollPane, buttonPanel);
    }

    private void populateSaveSlots() {
        List<GameSaveService.SaveFileInfo> saves = saveService.getAvailableSaves();

        if (saves.isEmpty()) {
            Label noSavesLabel = new Label("The Royal Archives are empty.\nNo chronicles have been recorded yet.");
            noSavesLabel.getStyleClass().add("no-saves-label");
            noSavesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #F5DEB3; -fx-text-alignment: center;");
            saveSlotsContainer.getChildren().add(noSavesLabel);
        } else {
            for (GameSaveService.SaveFileInfo saveInfo : saves) {
                Node saveSlotNode = createSaveSlot(saveInfo);
                saveSlotsContainer.getChildren().add(saveSlotNode);
            }
        }
    }

    private Node createSaveSlot(GameSaveService.SaveFileInfo saveInfo) {
        HBox saveSlot = new HBox(25);
        saveSlot.setAlignment(Pos.CENTER_LEFT);
        saveSlot.getStyleClass().add("save-slot");
        saveSlot.setPadding(new Insets(15, 25, 15, 25));
        saveSlot.setMaxWidth(650);

        // Icon (using the save icon from the spritesheet)
        ImageView icon = new ImageView(UIAssets.getImage("KUTowerButtons"));
        icon.setViewport(
                new javafx.geometry.Rectangle2D(UIAssets.ICON_SAVE_COL * 64, UIAssets.ICON_SAVE_ROW * 64, 64, 64));
        icon.setFitWidth(48);
        icon.setFitHeight(48);

        // Save details
        VBox details = new VBox(8);
        Label saveNameLabel = new Label(saveInfo.saveName);
        saveNameLabel.getStyleClass().add("save-name");
        saveNameLabel.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #F5DEB3; -fx-font-family: 'Serif';");

        Label detailsLabel = new Label(
                String.format("Wave: %d | Last Saved: %s", saveInfo.currentWave, saveInfo.getFormattedTime()));
        detailsLabel.getStyleClass().add("save-details");
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc; -fx-font-family: 'Serif';");

        details.getChildren().addAll(saveNameLabel, detailsLabel);

        saveSlot.getChildren().addAll(icon, details);

        // Add click behavior for selection
        saveSlot.setOnMouseClicked(event -> {
            // Deselect others
            for (Node child : saveSlotsContainer.getChildren()) {
                child.getStyleClass().remove("selected");
                child.setStyle(
                        "-fx-background-color: rgba(0, 0, 0, 0.4); -fx-border-color: #8B4513; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");
            }
            // Select this one
            saveSlot.getStyleClass().add("selected");
            saveSlot.setStyle(
                    "-fx-background-color: rgba(100, 150, 200, 0.5); -fx-border-color: #4ca3dd; -fx-border-width: 3; -fx-background-radius: 10; -fx-border-radius: 10;");
            selectedSave = saveInfo;
            loadButton.setDisable(false); // Enable load button
        });

        // Initial style
        saveSlot.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.4); -fx-border-color: #8B4513; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");

        return saveSlot;
    }

    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(30);
        buttonPanel.setAlignment(Pos.CENTER);

        loadButton = createMenuButton("Load Chronicle", this::loadSelectedGame);
        Button backButton = createMenuButton("Return", this::returnToMainMenu);

        loadButton.setDisable(true); // Disabled until a save is selected

        buttonPanel.getChildren().addAll(loadButton, backButton);
        return buttonPanel;
    }

    private Button createMenuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        button.setOnAction(e -> action.run());
        return button;
    }

    private void loadSelectedGame() {
        if (selectedSave == null) {
            // This should not be callable if the button is disabled.
            System.out.println("Error: Load called with no save file selected.");
            return;
        }

        try {
            GameMap tempMap = new GameMap("LoadedMap", 20, 15);
            GameController gameController = new GameController(tempMap);
            boolean success = saveService.loadGame(gameController, selectedSave.filename);

            if (success) {
                GameScreen gameScreen = new GameScreen(primaryStage, gameController);
                double w = javafx.stage.Screen.getPrimary().getBounds().getWidth();
                double h = javafx.stage.Screen.getPrimary().getBounds().getHeight();
                Scene gameScene = new Scene(gameScreen, w, h);
                transitionToScene(gameScene);
            } else {
                // You can add a more user-friendly alert here
                System.out.println("Failed to load game. The save file might be corrupted.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // You can add a more user-friendly alert here
        }
    }

    private void returnToMainMenu() {
        MainMenuScreen mainMenu = new MainMenuScreen(primaryStage);
        double w = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double h = javafx.stage.Screen.getPrimary().getBounds().getHeight();
        Scene mainMenuScene = new Scene(mainMenu, w, h);
        transitionToScene(mainMenuScene);
    }

    private void transitionToScene(Scene newScene) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
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
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        });
        fadeOut.play();
    }
}