package com.ku.towerdefense.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

        // Attempt to prevent dragging the window by its content
        this.setOnMousePressed(event -> {
            if (event.getTarget() == this) {
                // System.out.println("MainMenuScreen VBox pressed, consuming event to prevent
                // potential drag.");
                event.consume();
            }
        });

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
            } catch (NullPointerException | IllegalArgumentException e) {
                System.err.println("Could not load stylesheet /css/style.css for the new scene: " + e.getMessage());
            }

            ImageCursor customCursor = UIAssets.getCustomCursor();
            if (customCursor != null) {
                newScene.setCursor(customCursor);
            }

            primaryStage.setScene(newScene);
            primaryStage.setFullScreen(true);

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
        double w = primaryStage.getScene() != null ? primaryStage.getScene().getWidth() : primaryStage.getWidth();
        double h = primaryStage.getScene() != null ? primaryStage.getScene().getHeight() : primaryStage.getHeight();
        Scene mapSelectionScene = new Scene(mapSelection, w, h);
        transitionToScene(mapSelectionScene);
    }

    /**
     * Action to open the map editor.
     */
    private void openMapEditor() {
        MapEditorScreen mapEditor = new MapEditorScreen(primaryStage);
        double w = primaryStage.getScene() != null ? primaryStage.getScene().getWidth() : primaryStage.getWidth();
        double h = primaryStage.getScene() != null ? primaryStage.getScene().getHeight() : primaryStage.getHeight();
        Scene mapEditorScene = new Scene(mapEditor, w, h);
        transitionToScene(mapEditorScene);
    }

    /**
     * Action to open the options screen.
     */
    private void openOptions() {
        OptionsScreen options = new OptionsScreen(primaryStage);
        double w = primaryStage.getScene() != null ? primaryStage.getScene().getWidth() : primaryStage.getWidth();
        double h = primaryStage.getScene() != null ? primaryStage.getScene().getHeight() : primaryStage.getHeight();
        Scene optionsScene = new Scene(options, w, h);
        transitionToScene(optionsScene);
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