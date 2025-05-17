package com.ku.towerdefense.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The main menu screen for the KU Tower Defense game.
 */
public class MainMenuScreen extends VBox {
    private final Stage primaryStage;

    /**
     * Constructor for the main menu.
     *
     * @param primaryStage the primary stage
     */
    public MainMenuScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Make the window properly resizable with minimum dimensions
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        initializeUI();

        // Bind the size of this VBox (MainMenuScreen) to the size of the Scene it's
        // placed in.
        // This ensures the VBox fills the scene, allowing its background to cover the
        // whole area.
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                prefWidthProperty().bind(newScene.widthProperty());
                prefHeightProperty().bind(newScene.heightProperty());
            }
        });
    }

    /**
     * Initialize the user interface components for the main menu.
     */
    private void initializeUI() {
        // Set spacing and alignment
        setSpacing(15);
        setAlignment(Pos.CENTER);
        // setStyle("-fx-background-color: transparent;"); // Let CSS handle the
        // background entirely
        getStyleClass().add("main-menu-layout"); // Add style class for CSS targeting

        // Game title
        Text gameTitle = new Text("KU Tower Defense");
        gameTitle.getStyleClass().add("menu-title");
        getChildren().add(gameTitle); // Add the title directly

        // Create menu buttons
        Button newGameButton = createMenuButton("New Game", this::startNewGame);
        Button mapEditorButton = createMenuButton("Map Editor", this::openMapEditor);
        Button optionsButton = createMenuButton("Options", this::openOptions);
        Button quitButton = createMenuButton("Quit", this::quitGame);

        // Add buttons to layout
        getChildren().addAll(newGameButton, mapEditorButton, optionsButton, quitButton);
    }

    /**
     * Helper method to create consistently styled menu buttons.
     * 
     * @param text   button text
     * @param action action to perform when clicked
     * @return styled button
     */
    private Button createMenuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        // Apply scale transition on click (Handled by CSS :pressed state)

        button.setOnAction(e -> action.run());
        return button;
    }

    /**
     * Helper method to transition to a new scene with a fade effect.
     * 
     * @param newScene The scene to transition to.
     */
    private void transitionToScene(Scene newScene) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            try {
                String css = getClass().getResource("/css/style.css").toExternalForm();
                newScene.getStylesheets().add(css);
            } catch (NullPointerException | IllegalArgumentException e) { // Catch potential exceptions
                System.err.println("Could not load stylesheet /css/style.css for the new scene: " + e.getMessage());
            }
            primaryStage.setScene(newScene);
            // Optional: Add a fade-in transition for the new scene's root node if desired
            if (newScene.getRoot() != null) {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        });
        fadeOut.play();
    }

    /**
     * Action to start a new game.
     */
    private void startNewGame() {
        MapSelectionScreen mapSelection = new MapSelectionScreen(primaryStage);
        Scene mapSelectionScene = new Scene(mapSelection, primaryStage.getWidth(), primaryStage.getHeight()); // Use
                                                                                                              // current
                                                                                                              // stage
                                                                                                              // size
        transitionToScene(mapSelectionScene); // Use helper method
    }

    /**
     * Action to open the map editor.
     */
    private void openMapEditor() {
        MapEditorScreen mapEditor = new MapEditorScreen(primaryStage);
        Scene mapEditorScene = new Scene(mapEditor, primaryStage.getWidth(), primaryStage.getHeight()); // Keep specific size or use current
        transitionToScene(mapEditorScene); // Use helper method
    }

    /**
     * Action to open the options screen.
     */
    private void openOptions() {
        OptionsScreen options = new OptionsScreen(primaryStage);
        Scene optionsScene = new Scene(options, primaryStage.getWidth(), primaryStage.getHeight()); // Use current stage
                                                                                                    // size
        transitionToScene(optionsScene); // Use helper method
    }

    /**
     * Action to quit the game.
     */
    private void quitGame() {
        // Optional: Add a fade-out before closing
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> primaryStage.close());
        fadeOut.play();
        // primaryStage.close(); // Original direct close
    }
}