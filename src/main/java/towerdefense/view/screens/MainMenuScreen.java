package towerdefense.view.screens;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import towerdefense.controller.MainMenuController;
import towerdefense.model.GameModel;

/**
 * Provides the UI components for the main menu screen.
 */
public class MainMenuScreen /* extends Stage */ {

    private MainMenuController controller;
    private final GameModel model;
    private BorderPane view; // Store the root node

    /**
     * Constructor for MainMenuScreen UI Provider.
     */
    public MainMenuScreen(GameModel model) {
        this.model = model;
        // Pass only the model to the controller constructor
        this.controller = new MainMenuController(model); // Fix: Remove null argument
        initializeUI();
    }

    /**
     * Initialize the UI components and return the root node.
     */
    private void initializeUI() {
        // Create main layout
        view = new BorderPane(); // Create the root node
        view.setPadding(new Insets(50));
        view.setStyle("-fx-background-color: #ecf0f1;");

        // Create title label
        Label titleLabel = new Label("Tower Defense");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Create buttons
        Button startButton = createStyledButton("Start Game");
        Button settingsButton = createStyledButton("Settings");
        Button exitButton = createStyledButton("Exit");

        // Set button actions
        // Controller needs to be updated to not rely on view.close()
        startButton.setOnAction(e -> controller.startGame());
        settingsButton.setOnAction(e -> controller.openSettings());
        exitButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0); // Ensure application exits cleanly
        });

        // Create button container
        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(startButton, settingsButton, exitButton);

        // Add components to layout
        view.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        view.setCenter(buttonContainer);

        // Removed Scene and Stage setup
    }

    /**
     * Returns the root node of the main menu UI.
     */
    public Parent getView() {
        if (view == null) {
            initializeUI(); // Ensure UI is initialized if called before constructor finishes (unlikely)
        }
        return view;
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

    // Removed showScreen method
}