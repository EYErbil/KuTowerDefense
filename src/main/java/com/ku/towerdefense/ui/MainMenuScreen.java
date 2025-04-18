package com.ku.towerdefense.ui;

import com.ku.towerdefense.controller.GameController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;

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
    }
    
    /**
     * Initialize the user interface components for the main menu.
     */
    private void initializeUI() {
        // Set spacing and alignment
        setSpacing(15);
        setAlignment(Pos.CENTER);
        
        // Load background image from classpath resources
        try (InputStream bgStream = getClass().getResourceAsStream("/Asset_pack/KuTowerDefence2.jpg")) {
            if (bgStream != null) {
                Image backgroundImage = new Image(bgStream);
                BackgroundImage background = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, true, true)
                );
                setBackground(new Background(background));
                System.out.println("Loaded background image from classpath: /Asset_pack/KuTowerDefence2.jpg");
            } else {
                System.err.println("Background image not found in classpath: /Asset_pack/KuTowerDefence2.jpg");
                setStyle("-fx-background-color: #333333;");
            }
        } catch (Exception e) {
            System.err.println("Failed to load background image: " + e.getMessage());
            e.printStackTrace();
            setStyle("-fx-background-color: #333333;");
        }
        
        // Game title
        Text gameTitle = new Text("KU Tower Defense");
        gameTitle.getStyleClass().add("menu-title");
        
        // Load game logo from classpath resources
        try (InputStream logoStream = getClass().getResourceAsStream("/Asset_pack/KuTowerDefence1.jpg")) {
            if (logoStream != null) {
                Image logoImage = new Image(logoStream);
                ImageView logo = new ImageView(logoImage);
                logo.setPreserveRatio(true);
                logo.fitWidthProperty().bind(this.widthProperty().multiply(0.4));
                getChildren().add(logo);
                System.out.println("Loaded game logo from classpath: /Asset_pack/KuTowerDefence1.jpg");
            } else {
                System.err.println("Logo image not found in classpath: /Asset_pack/KuTowerDefence1.jpg");
                getChildren().add(gameTitle);
            }
        } catch (Exception e) {
            System.err.println("Failed to load logo image: " + e.getMessage());
            e.printStackTrace();
            getChildren().add(gameTitle);
        }
        
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
     * @param text button text
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
     * Action to start a new game.
     */
    private void startNewGame() {
        MapSelectionScreen mapSelection = new MapSelectionScreen(primaryStage);
        Scene mapSelectionScene = new Scene(mapSelection, 800, 600);
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            mapSelectionScene.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("Could not load stylesheet /css/style.css for MapSelectionScreen");
        }
        primaryStage.setScene(mapSelectionScene);
    }
    
    /**
     * Action to open the map editor.
     */
    private void openMapEditor() {
        MapEditorScreen mapEditor = new MapEditorScreen(primaryStage);
        Scene mapEditorScene = new Scene(mapEditor, 1400, 1000);
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            mapEditorScene.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("Could not load stylesheet /css/style.css for MapEditorScreen");
        }
        primaryStage.setScene(mapEditorScene);
    }
    
    /**
     * Action to open the options screen.
     */
    private void openOptions() {
        OptionsScreen options = new OptionsScreen(primaryStage);
        Scene optionsScene = new Scene(options, 800, 600);
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            optionsScene.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("Could not load stylesheet /css/style.css for OptionsScreen");
        }
        primaryStage.setScene(optionsScene);
    }
    
    /**
     * Action to quit the game.
     */
    private void quitGame() {
        primaryStage.close();
    }
} 