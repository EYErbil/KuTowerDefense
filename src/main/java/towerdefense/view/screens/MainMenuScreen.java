package towerdefense.view.screens;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import towerdefense.controller.MainMenuController;
import towerdefense.model.GameModel;

/**
 * Main menu screen for Tower Defense.
 * Provides options to start a new game, edit maps, access options, and quit the
 * game.
 */
public class MainMenuScreen extends Stage {

    private final MainMenuController controller;
    private final GameModel model;

    /**
     * Constructor for MainMenuScreen.
     */
    public MainMenuScreen(GameModel model) {
        this.model = model;
        this.controller = new MainMenuController(this, model);
        initializeUI();
    }

    /**
     * Initialize the UI components.
     */
    private void initializeUI() {
        // Create title label
        Label titleLabel = new Label("Tower Defense");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Create buttons
        Button startButton = createStyledButton("Start Game");
        Button settingsButton = createStyledButton("Settings");
        Button exitButton = createStyledButton("Exit");

        // Set button actions
        startButton.setOnAction(e -> controller.startGame());
        settingsButton.setOnAction(e -> controller.openSettings());
        exitButton.setOnAction(e -> Platform.exit());

        // Create button container
        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(startButton, settingsButton, exitButton);

        // Create main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(50));
        mainLayout.setStyle("-fx-background-color: #ecf0f1;");

        // Add components to layout
        mainLayout.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        mainLayout.setCenter(buttonContainer);

        // Set up the scene
        Scene scene = new Scene(mainLayout, 800, 600);
        setScene(scene);
        setTitle("Tower Defense - Main Menu");
        setResizable(false);
    }

    /**
     * Helper method to create styled menu buttons.
     */
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        button.setStyle("-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #2980b9; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5;"));
        return button;
    }

    public void showScreen() {
        show();
    }
}