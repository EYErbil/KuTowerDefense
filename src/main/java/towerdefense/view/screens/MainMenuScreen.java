package towerdefense.view.screens;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
    private final String defaultButtonStyle = "-fx-background-color: #5a3d2b; -fx-text-fill: white; -fx-font-family: 'Arial Black'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: #3e2c1d; -fx-border-width: 2; -fx-border-radius: 5;";
    private final String hoverButtonStyle = "-fx-background-color: #7c553f; -fx-text-fill: white; -fx-font-family: 'Arial Black'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: #3e2c1d; -fx-border-width: 2; -fx-border-radius: 5;";

    /**
     * Constructor for MainMenuScreen UI Provider.
     */
    public MainMenuScreen(GameModel model) {
        this.model = model;
        this.controller = new MainMenuController(model);
        initializeUI();
    }

    /**
     * Initialize the UI components and return the root node.
     */
    private void initializeUI() {
        view = new BorderPane();
        view.setPadding(new Insets(50));
        // Dark wood/parchment background
        view.setStyle("-fx-background-color: #8a6e4b; -fx-border-color: #3e2c1d; -fx-border-width: 5;");

        // Game Title Label
        Label titleLabel = new Label("KU Tower Defense");
        // Use a more thematic font if available, fallback to bold Arial/similar
        titleLabel.setFont(Font.font("Times New Roman", FontWeight.BOLD, 48));
        titleLabel.setStyle("-fx-text-fill: #3e2c1d;"); // Dark brown text

        // Create buttons using helper
        Button startButton = createStyledButton("Start Game");
        Button mapEditorButton = createStyledButton("Map Editor"); // Added Map Editor Button
        Button settingsButton = createStyledButton("Settings");
        Button exitButton = createStyledButton("Exit Game");

        // Set button actions
        startButton.setOnAction(e -> controller.startGame());
        mapEditorButton.setOnAction(e -> controller.openMapEditor()); // Action for Map Editor
        settingsButton.setOnAction(e -> controller.openSettings());
        exitButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        // Button container
        VBox buttonContainer = new VBox(25); // Increased spacing
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(startButton, mapEditorButton, settingsButton, exitButton);

        // Add components to layout
        view.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 40, 0)); // Add margin below title
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
        button.setPrefWidth(250); // Slightly wider buttons
        button.setPrefHeight(55);
        button.setStyle(defaultButtonStyle);
        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(hoverButtonStyle));
        button.setOnMouseExited(e -> button.setStyle(defaultButtonStyle));
        // TODO: Add pressed effect

        return button;
    }

    // Removed showScreen method
}