package com.ku.towerdefense.ui;

import com.ku.towerdefense.controller.GameController;
import com.ku.towerdefense.model.entity.ArcherTower;
import com.ku.towerdefense.model.entity.ArtilleryTower;
import com.ku.towerdefense.model.entity.MageTower;
import com.ku.towerdefense.model.entity.Tower;
import com.ku.towerdefense.model.map.Tile;
import com.ku.towerdefense.model.map.TileType;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Affine;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.ImageCursor;
import javafx.scene.image.ImageView;
import javafx.scene.Node;

/**
 * The main game screen where the tower defense gameplay takes place.
 */


public class GameScreen extends BorderPane {
    private final Stage primaryStage;
    private final GameController gameController;
    private Canvas gameCanvas;
    private GameRenderTimer renderTimer;
    private Tower selectedTower;
    private boolean isPaused = false;
    private final StackPane canvasRootPane = new StackPane();
    private final Pane uiOverlayPane = new Pane();
    private final Affine worldTransform = new Affine();
    private Node activePopup = null;
    private static final double POPUP_ICON_SIZE = 36.0;
    private static final double POPUP_SPACING = 5.0;
    
    // Custom AnimationTimer class with additional methods
    private class GameRenderTimer extends AnimationTimer {
        private long lastTime = -1;
        private String statusMessage = "Ready to play!";
        private long statusTimestamp = 0;
        private double mouseX = 0;
        private double mouseY = 0;
        private boolean mouseInCanvas = false;
        
        @Override
        public void handle(long now) {
            GraphicsContext gc = gameCanvas.getGraphicsContext2D();
            double canvasWidth = gameCanvas.getWidth();
            double canvasHeight = gameCanvas.getHeight();

            // Calculate desired game world size (original fixed size)
            double worldWidth = gameController.getGameMap().getWidth() * 64.0;
            double worldHeight = gameController.getGameMap().getHeight() * 64.0;

            // --- Calculate Scaling and Centering ---
            double scaleX = canvasWidth / worldWidth;
            double scaleY = canvasHeight / worldHeight;
            double scale = Math.min(scaleX, scaleY); // Maintain aspect ratio

            // Calculate translation to center the scaled world
            double scaledWorldWidth = worldWidth * scale;
            double scaledWorldHeight = worldHeight * scale;
            double translateX = (canvasWidth - scaledWorldWidth) / 2.0;
            double translateY = (canvasHeight - scaledWorldHeight) / 2.0;

            // Update the transformation matrix
            worldTransform.setToIdentity();
            worldTransform.appendTranslation(translateX, translateY);
            worldTransform.appendScale(scale, scale);
            // --- End Scaling and Centering ---


            // --- Rendering ---
            gc.save(); // Save default transform state

            // Clear the entire canvas (background)
            gc.setFill(javafx.scene.paint.Color.web("#111111")); // Match background color
            gc.fillRect(0, 0, canvasWidth, canvasHeight);

            // Apply the world transformation (scale and center)
            gc.setTransform(worldTransform);

            // Render game elements using original world coordinates
            // The transform handles scaling them correctly onto the canvas
            gameController.render(gc);

            // Render tower preview (using transformed mouse coords - see setOnMouseClicked)
            if (selectedTower != null && mouseInCanvas) {
                // Transform mouse coordinates from canvas space to world space
                javafx.geometry.Point2D worldMouse = transformMouseCoords(mouseX, mouseY);

                if (worldMouse != null) {
                    // Convert world coordinates to grid coordinates
                    int tileX = (int)(worldMouse.getX() / 64);
                    int tileY = (int)(worldMouse.getY() / 64);

                    // Get center of the tile in world coordinates
                    double centerX = tileX * 64 + 32;
                    double centerY = tileY * 64 + 32;

                    // Check if we can place here (uses world coordinates)
                    boolean canPlace = gameController.getGameMap().canPlaceTower(centerX, centerY, gameController.getTowers());

                    // Draw preview circle in world coordinates
                    gc.setGlobalAlpha(0.5);
                    gc.setFill(canPlace ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
                    // Use world coordinates for drawing - transform handles canvas placement
                    gc.fillOval(centerX - 32, centerY - 32, 64, 64);

                    // Draw range preview in world coordinates
                    gc.setStroke(javafx.scene.paint.Color.WHITE);
                    gc.setGlobalAlpha(0.2);
                    double range = 0;
                    if (selectedTower instanceof ArcherTower) range = ((ArcherTower)selectedTower).getRange();
                    else if (selectedTower instanceof ArtilleryTower) range = ((ArtilleryTower)selectedTower).getRange();
                    else if (selectedTower instanceof MageTower) range = ((MageTower)selectedTower).getRange();

                    if (range > 0) {
                       gc.strokeOval(centerX - range, centerY - range, range * 2, range * 2);
                    }
                    gc.setGlobalAlpha(1.0);
                }
            }

            gc.restore(); // Restore default transform for drawing UI overlays

            // --- Update game logic ---
            if (!isPaused) {
                if (lastTime < 0) lastTime = now;
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                gameController.update(deltaTime);
            } else {
                 lastTime = -1; // Reset delta time calculation when paused
            }


            // --- UI Overlays (drawn directly on canvas, not scaled) ---
            // Status message (bottom-left)
            long currentTime = System.currentTimeMillis();
            if (currentTime - statusTimestamp < 3000) {
                double alpha = 1.0 - (currentTime - statusTimestamp) / 3000.0;
                gc.setGlobalAlpha(alpha);
                gc.setFill(javafx.scene.paint.Color.WHITE);
                gc.setStroke(javafx.scene.paint.Color.BLACK);
                gc.setLineWidth(1); // Reduced thickness slightly
                gc.strokeText(statusMessage, 10, canvasHeight - 10); // Anchor bottom-left
                gc.fillText(statusMessage, 10, canvasHeight - 10);
                gc.setGlobalAlpha(1.0);
            }

            // Debug information (top-left)
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillText("Towers: " + gameController.getTowers().size(), 10, 20);
            gc.fillText("Enemies: " + gameController.getEnemies().size(), 10, 40);
            gc.fillText("Wave: " + gameController.getCurrentWave(), 10, 60);
            // Add FPS counter? (Optional)

            // Asset loading issue message
            if (!gameController.getTowers().isEmpty() && gameController.getTowers().get(0).getImage() == null) {
                gc.setFill(javafx.scene.paint.Color.RED);
                gc.fillText("Asset loading issue detected!", 10, 80);
                gc.fillText("Using fallback rendering instead", 10, 100);
            }
        }
        
        // Method to set mouse position
        public void setMousePosition(double x, double y, boolean inCanvas) {
            this.mouseX = x;
            this.mouseY = y;
            this.mouseInCanvas = inCanvas;
        }
        
        // Method to set status message
        public void setStatusMessage(String message) {
            this.statusMessage = message;
            this.statusTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Constructor for the game screen.
     *
     * @param primaryStage the primary stage of the application
     * @param gameController the game controller
     */
    public GameScreen(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        initializeUI();
        startRenderLoop();
    }
    
    /**
     * Initialize the user interface components.
     */
    private void initializeUI() {
        getStyleClass().add("game-screen");
        
        HBox topBar = createTopBar();
        gameCanvas = new Canvas();

        // canvasContainer will hold just the canvas for transformations
        StackPane canvasTransformContainer = new StackPane(gameCanvas);
        canvasTransformContainer.getStyleClass().add("game-canvas-container");

        gameCanvas.widthProperty().bind(canvasTransformContainer.widthProperty());
        gameCanvas.heightProperty().bind(canvasTransformContainer.heightProperty());

        // uiOverlayPane should not interfere with mouse events on the canvas by default
        uiOverlayPane.setPickOnBounds(false);

        // canvasRootPane holds the game canvas (via canvasTransformContainer) and the UI overlay
        canvasRootPane.getChildren().addAll(canvasTransformContainer, uiOverlayPane);

        // Mouse event handling on gameCanvas (remains the same)
        gameCanvas.setOnMouseMoved(e -> renderTimer.setMousePosition(e.getX(), e.getY(), true));
        gameCanvas.setOnMouseExited(e -> renderTimer.setMousePosition(e.getX(), e.getY(), false));

        gameCanvas.setOnMouseClicked(e -> {
            clearActivePopup();

             // Transform click coordinates from canvas space to world space
            javafx.geometry.Point2D worldCoord = transformMouseCoords(e.getX(), e.getY());

            if (worldCoord == null) return; // Click was outside the scaled world area

            double worldX = worldCoord.getX();
            double worldY = worldCoord.getY();
            int tileX = (int) (worldX / 64.0);
            int tileY = (int) (worldY / 64.0);

            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                if (selectedTower != null) {
                    placeTower(worldX, worldY);
                } else {
                    Tower existingTower = gameController.getTowerAt(worldX, worldY);
                    
                    if (existingTower != null) {
                        createUpgradeSellPopup(e.getScreenX(), e.getSceneY(), existingTower, tileX, tileY); 
                    } else {
                        Tile clickedTile = gameController.getGameMap().getTile(tileX, tileY);
                        if (clickedTile != null && clickedTile.getType() == TileType.TOWER_SLOT) {
                            createBuildTowerPopup(e.getScreenX(), e.getSceneY(), tileX, tileY);
                        }
                    }
                }
            } else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                 sellTower(worldX, worldY);
            } else if (e.getButton() == javafx.scene.input.MouseButton.MIDDLE) {
                 selectTowerAt(worldX, worldY);
            }
        });
        
        // Create sidebar with tower options and controls
        VBox sidebar = createSidebar();
        
        // Layout
        setTop(topBar);
        setCenter(canvasRootPane); // Center the StackPane containing canvas and overlay
        setRight(sidebar);
        
        canvasRootPane.requestFocus();
    }
    
    /**
     * Create the top bar with game information.
     *
     * @return the top bar container
     */
    private HBox createTopBar() {
    HBox topBar = new HBox(10); // Reduced spacing a bit to accommodate icons
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(10));
    topBar.getStyleClass().add("game-top-bar");

    Image uiSpriteSheet = UIAssets.getImage("GameUI");

    // --- Lives indicator ---
    ImageView livesIcon = null;
    if (uiSpriteSheet != null) {
        try {
            // Assuming Health/Lives icon is the second 32x32 sprite in Coin_Health_Wave.png
            javafx.geometry.Rectangle2D livesViewport = new javafx.geometry.Rectangle2D(32, 0, 32, 32); 
            livesIcon = new ImageView(uiSpriteSheet);
            livesIcon.setViewport(livesViewport);
            livesIcon.setFitWidth(24); // Adjust display size as needed
            livesIcon.setFitHeight(24);
        } catch (Exception e) { System.err.println("Error creating lives icon: " + e.getMessage()); }
    }
    Label livesLabel = new Label("" + gameController.getPlayerLives());
    livesLabel.getStyleClass().add("game-info-text");
    HBox livesDisplay = new HBox(5, (livesIcon != null ? livesIcon : new Label("❤️")), livesLabel);
    livesDisplay.setAlignment(Pos.CENTER_LEFT);

    // --- Gold indicator ---
    ImageView goldIcon = null;
    if (uiSpriteSheet != null) {
        try {
            // Assuming Coin/Gold icon is the first 32x32 sprite in Coin_Health_Wave.png
            javafx.geometry.Rectangle2D goldViewport = new javafx.geometry.Rectangle2D(0, 0, 32, 32);
            goldIcon = new ImageView(uiSpriteSheet);
            goldIcon.setViewport(goldViewport);
            goldIcon.setFitWidth(24);
            goldIcon.setFitHeight(24);
        } catch (Exception e) { System.err.println("Error creating gold icon: " + e.getMessage()); }
    }
    Label goldLabel = new Label("" + gameController.getPlayerGold());
    goldLabel.getStyleClass().add("game-info-text");
    HBox goldDisplay = new HBox(5, (goldIcon != null ? goldIcon : new Label("💰")), goldLabel);
    goldDisplay.setAlignment(Pos.CENTER_LEFT);

    // --- Wave indicator ---
    ImageView waveIcon = null;
    if (uiSpriteSheet != null) {
        try {
            // Assuming Wave icon is the third 32x32 sprite in Coin_Health_Wave.png
            javafx.geometry.Rectangle2D waveViewport = new javafx.geometry.Rectangle2D(64, 0, 32, 32); 
            waveIcon = new ImageView(uiSpriteSheet);
            waveIcon.setViewport(waveViewport);
            waveIcon.setFitWidth(24);
            waveIcon.setFitHeight(24);
        } catch (Exception e) { System.err.println("Error creating wave icon: " + e.getMessage()); }
    }
    Label waveLabel = new Label("" + gameController.getCurrentWave());
    waveLabel.getStyleClass().add("game-info-text");
    HBox waveDisplay = new HBox(5, (waveIcon != null ? waveIcon : new Label("🌊")), waveLabel);
    waveDisplay.setAlignment(Pos.CENTER_LEFT);

    // Spacer to push controls to the right
    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

    // Pause/Resume button
    Button pauseButton = new Button("⏸️ Pause");
    UIAssets.styleButton(pauseButton, "blue");
    pauseButton.setOnAction(e -> togglePause());

    // Menu button
    Button menuButton = new Button("Menu");
    UIAssets.styleButton(menuButton, "blue");
    menuButton.setOnAction(e -> openPauseMenu());

    // Update the labels when values change
    AnimationTimer updateLabels = new AnimationTimer() {
        @Override
        public void handle(long now) {
            livesLabel.setText("" + gameController.getPlayerLives());
            goldLabel.setText("" + gameController.getPlayerGold());
            waveLabel.setText("" + gameController.getCurrentWave());
            
            // Update pause button text based on game state
            pauseButton.setText(isPaused ? "▶️ Resume" : "⏸️ Pause");
        }
    };
    updateLabels.start();

    // Add all elements to the top bar
    topBar.getChildren().addAll(livesDisplay, goldDisplay, waveDisplay, spacer, pauseButton, menuButton);

    return topBar;
}
    
    /**
     * Create the sidebar with tower selection and game controls.
     *
     * @return the sidebar container
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setMinWidth(200);
        sidebar.setAlignment(Pos.TOP_CENTER);
        // sidebar.setStyle("-fx-background-color: #333333;"); // Removed inline style
        sidebar.getStyleClass().add("game-sidebar"); // Added style class
        
        // Create heading for towers section
        Text towersTitle = new Text("Towers");
        towersTitle.getStyleClass().add("sidebar-title");
        
        // Archer Tower button
        Button archerTowerButton = createTowerButton("Archer Tower", 50);
        UIAssets.styleButton(archerTowerButton, "blue");
        archerTowerButton.setOnAction(e -> selectedTower = new ArcherTower(0, 0));
        
        // Artillery Tower button
        Button artilleryTowerButton = createTowerButton("Artillery Tower", 100);
        UIAssets.styleButton(artilleryTowerButton, "blue");
        artilleryTowerButton.setOnAction(e -> selectedTower = new ArtilleryTower(0, 0));
        
        // Mage Tower button
        Button mageTowerButton = createTowerButton("Mage Tower", 75);
        UIAssets.styleButton(mageTowerButton, "blue");
        mageTowerButton.setOnAction(e -> selectedTower = new MageTower(0, 0));
        
        // Create heading for game controls section
        Text controlsTitle = new Text("Game Controls");
        controlsTitle.getStyleClass().add("sidebar-title");
        
        // Start Wave button
        Button startWaveButton = new Button("Start Next Wave");
        startWaveButton.setPrefWidth(150);
        UIAssets.styleButton(startWaveButton, "red");
        startWaveButton.setOnAction(e -> startNextWave());
        
        // Test Enemy button
        Button addEnemyButton = new Button("Add Test Enemy");
        addEnemyButton.setPrefWidth(150);
        UIAssets.styleButton(addEnemyButton, "red");
        addEnemyButton.setOnAction(e -> {
            gameController.addTestEnemy();
            renderTimer.setStatusMessage("Added new enemy!");
        });
        
        // Speed toggle button
        Button speedButton = new Button("Toggle Speed (1x)");
        speedButton.setPrefWidth(150);
        UIAssets.styleButton(speedButton, "blue");
        speedButton.setOnAction(e -> toggleGameSpeed());
        
        // Add all elements to sidebar
        sidebar.getChildren().addAll(
            towersTitle,
            archerTowerButton,
            artilleryTowerButton,
            mageTowerButton,
            controlsTitle,
            startWaveButton,
            addEnemyButton,
            speedButton
        );
        
        return sidebar;
    }
    private void togglePause() {
        isPaused = !isPaused;
        
        if (isPaused) {
            // Pause the game
            renderTimer.stop();
            gameController.pauseGame();
        } else {
            // Resume the game
            renderTimer.start();
            gameController.resumeGame();
        }
    }
    
    /**
     * Create a tower button with price information.
     *
     * @param name tower name
     * @param cost tower cost
     * @return configured button
     */
    private Button createTowerButton(String name, int cost) {
        Button button = new Button(name + " ($ " + cost + ")");
        button.setPrefWidth(150);
        return button;
    }
    
    /**
     * Place the selected tower at the specified world coordinates.
     *
     * @param worldX the x-coordinate in the game world
     * @param worldY the y-coordinate in the game world
     */
    private void placeTower(double worldX, double worldY) {
        if (selectedTower == null) return;

        // Convert world coordinates to tile coordinates based on a 64x64 grid
        int tileX = (int) (worldX / 64.0);
        int tileY = (int) (worldY / 64.0);

        // Calculate the top-left position for the tower in world coordinates
        double towerPlaceX = tileX * 64.0;
        double towerPlaceY = tileY * 64.0;

        // Check if the tower can be placed at the calculated world coordinates
        if (gameController.getGameMap().canPlaceTower(towerPlaceX + 32.0, towerPlaceY + 32.0, gameController.getTowers())) { // Check center of tile
            Tower newTower = null;
            if (selectedTower instanceof ArcherTower) {
                newTower = new ArcherTower(towerPlaceX, towerPlaceY);
            } else if (selectedTower instanceof ArtilleryTower) {
                newTower = new ArtilleryTower(towerPlaceX, towerPlaceY);
            } else if (selectedTower instanceof MageTower) {
                newTower = new MageTower(towerPlaceX, towerPlaceY);
            }

            if (newTower != null && gameController.getPlayerGold() >= newTower.getCost()) {
                boolean placed = gameController.placeTower(newTower);
                if (placed) {
                    renderTimer.setStatusMessage(newTower.getClass().getSimpleName() + " placed!");
                    selectedTower = null; // Clear selection after successful placement
                } else {
                    renderTimer.setStatusMessage("Cannot place tower here (blocked or path)!");
                }
            } else {
                renderTimer.setStatusMessage("Not enough gold! Need $" + newTower.getCost());
            }
        } else {
            renderTimer.setStatusMessage("Cannot place tower here (blocked or path)!");
        }
    }
    
    /**
     * Start the next wave of enemies.
     */
    private void startNextWave() {
        // Call the game controller to start the next wave
        gameController.startNextWave();
        renderTimer.setStatusMessage("Starting wave " + gameController.getCurrentWave() + "!");
    }
    
    /**
     * Toggle the game speed.
     */
    private void toggleGameSpeed() {
        // Get the button reference
        Button speedButton = null;
        for (javafx.scene.Node node : getChildren()) {
            if (node instanceof VBox) {
                VBox sidebar = (VBox)node;
                for (javafx.scene.Node child : sidebar.getChildren()) {
                    if (child instanceof Button && ((Button)child).getText().contains("Toggle Speed")) {
                        speedButton = (Button)child;
                        break;
                    }
                }
            }
            if (speedButton != null) break;
        }
        
        // Update game speed in the controller
        if (gameController.isSpeedAccelerated()) {
            gameController.setSpeedAccelerated(false);
            if (speedButton != null) {
                speedButton.setText("Toggle Speed (1x)");
            }
            renderTimer.setStatusMessage("Game speed set to normal (1x)");
        } else {
            gameController.setSpeedAccelerated(true);
            if (speedButton != null) {
                speedButton.setText("Toggle Speed (2x)");
            }
            renderTimer.setStatusMessage("Game speed set to fast (2x)");
        }
    }
    
    /**
     * Open the pause menu.
     */
    private void openPauseMenu() {
        // Pause the game controller
        // gameController.stopGame(); // Maybe just pause?
        gameController.pauseGame(); // Using pauseGame instead
        renderTimer.stop();

        // Create and show pause menu
        // In a real implementation, this would create a popup or overlay
        // System.out.println("Game paused!"); // No longer just prints

        // --- Navigate back to Main Menu --- 
        try {
             System.out.println("Returning to Main Menu...");
             MainMenuScreen mainMenu = new MainMenuScreen(primaryStage);
             Scene currentScene = primaryStage.getScene();
             if (currentScene != null) {
                 currentScene.setRoot(mainMenu);
             } else {
                 // Fallback if scene is somehow null
                 Scene mainMenuScene = new Scene(mainMenu, 800, 600); // Use default size
                 // Re-apply CSS if needed
                 mainMenuScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                 // Re-apply cursor if needed
                 ImageCursor customCursor = UIAssets.getCustomCursor();
                 if (customCursor != null) {
                     mainMenuScene.setCursor(customCursor);
                 }
                 primaryStage.setScene(mainMenuScene);
            }
        } catch (Exception ex) {
             System.err.println("Error navigating back to main menu: " + ex.getMessage());
             ex.printStackTrace();
             // Potentially show an error dialog to the user
        }
         // --- End Navigation Logic ---
    }
    
    /**
     * Show the game over screen.
     */
    private void showGameOverScreen() {
        // In a real implementation, this would show a game over screen
        System.out.println("Game over!");
        
        // For now, go back to main menu
        MainMenuScreen mainMenu = new MainMenuScreen(primaryStage);
        Scene mainMenuScene = new Scene(mainMenu, 800, 600);
        mainMenuScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(mainMenuScene);
    }
    
    /**
     * Sell a tower at the specified world coordinates.
     *
     * @param worldX the x-coordinate in the game world
     * @param worldY the y-coordinate in the game world
     */
    private void sellTower(double worldX, double worldY) {
        // GameController.sellTower will find the tower based on worldX, worldY using 64x64 grid
        int refund = gameController.sellTower(worldX, worldY);
        if (refund > 0) {
            renderTimer.setStatusMessage("Tower sold for " + refund + " gold.");
        } else {
            renderTimer.setStatusMessage("No tower to sell at this location.");
        }
    }
    
    /**
     * Select a tower at the specified coordinates.
     *
     * @param worldX the x-coordinate in the game world
     * @param worldY the y-coordinate in the game world
     */
    private void selectTowerAt(double worldX, double worldY) {
        // GameController.selectTowerAt will find and select the tower based on worldX, worldY
        Tower newlySelectedTower = gameController.selectTowerAt(worldX, worldY);
        this.selectedTower = newlySelectedTower; // Update GameScreen's selectedTower reference

        if (newlySelectedTower != null) {
            String towerType = "";
            if (newlySelectedTower instanceof ArcherTower) {
                towerType = "Archer";
            } else if (newlySelectedTower instanceof ArtilleryTower) {
                towerType = "Artillery";
            } else if (newlySelectedTower instanceof MageTower) {
                towerType = "Mage";
            }
            renderTimer.setStatusMessage(
                towerType + " Tower | Damage: " + newlySelectedTower.getDamage() + 
                " | Range: " + newlySelectedTower.getRange() + 
                " | Sell: $" + newlySelectedTower.getSellRefund()
            );
        } else {
            // If no tower was found, clear the status or set a default message
            // renderTimer.setStatusMessage("No tower selected."); 
        }
    }
    
    /**
     * Start the render loop for the game canvas.
     */
    private void startRenderLoop() {
        renderTimer = new GameRenderTimer();
        renderTimer.start();
        
        // Add mouse moved listener to track position
        gameCanvas.setOnMouseMoved(e -> {
            renderTimer.setMousePosition(e.getX(), e.getY(), true);
        });
        
        // Track when mouse exits canvas
        gameCanvas.setOnMouseExited(e -> {
            renderTimer.setMousePosition(0, 0, false);
        });
    }

    /**
     * Transforms mouse coordinates from Canvas space to World space.
     * @param canvasX Mouse X relative to canvas.
     * @param canvasY Mouse Y relative to canvas.
     * @return Point2D in world coordinates, or null if transform is invalid.
     */
    private javafx.geometry.Point2D transformMouseCoords(double canvasX, double canvasY) {
        try {
            // Use the inverse of the world transform to go from canvas -> world
            return worldTransform.inverseTransform(canvasX, canvasY);
        } catch (javafx.scene.transform.NonInvertibleTransformException e) {
            System.err.println("Warning: Could not invert world transform for mouse input.");
            return null; // Return null if transform is broken
        }
    }

    private void clearActivePopup() {
        if (activePopup != null) {
            uiOverlayPane.getChildren().remove(activePopup);
            activePopup = null;
        }
    }

    private void createBuildTowerPopup(double screenX, double screenY, int tileX, int tileY) {
        clearActivePopup();
        HBox popup = new HBox(POPUP_SPACING);
        popup.setPadding(new Insets(5));
        popup.setStyle("-fx-background-color: rgba(50, 50, 50, 0.85); -fx-background-radius: 5; -fx-border-color: grey; -fx-border-radius: 5;");

        // Archer Tower Button
        Button archerButton = UIAssets.createIconButton("Archer Tower (Cost: " + ArcherTower.COST + ")", 0, 2, POPUP_ICON_SIZE);
        archerButton.setOnAction(e -> {
            if (gameController.getPlayerGold() >= ArcherTower.COST) { // Initial check for immediate feedback, controller does final check
                Tower tower = new ArcherTower(tileX * 64.0, tileY * 64.0);
                if (gameController.purchaseAndPlaceTower(tower)) { // Use new method
                    renderTimer.setStatusMessage("Archer Tower placed!");
                    clearActivePopup();
                } else {
                    renderTimer.setStatusMessage("Cannot place Archer Tower. Gold: " + gameController.getPlayerGold());
                }
            } else {
                renderTimer.setStatusMessage("Not enough gold for Archer Tower.");
            }
        });

        // Mage Tower Button
        Button mageButton = UIAssets.createIconButton("Mage Tower (Cost: " + MageTower.COST + ")", 2, 2, POPUP_ICON_SIZE);
        mageButton.setOnAction(e -> {
            if (gameController.getPlayerGold() >= MageTower.COST) {
                Tower tower = new MageTower(tileX * 64.0, tileY * 64.0);
                if (gameController.purchaseAndPlaceTower(tower)) { // Use new method
                    renderTimer.setStatusMessage("Mage Tower placed!");
                    clearActivePopup();
                } else {
                    renderTimer.setStatusMessage("Cannot place Mage Tower. Gold: " + gameController.getPlayerGold());
                }
            } else {
                renderTimer.setStatusMessage("Not enough gold for Mage Tower.");
            }
        });

        // Artillery Tower Button
        Button artilleryButton = UIAssets.createIconButton("Artillery Tower (Cost: " + ArtilleryTower.COST + ")", 3, 2, POPUP_ICON_SIZE);
        artilleryButton.setOnAction(e -> {
            if (gameController.getPlayerGold() >= ArtilleryTower.COST) {
                Tower tower = new ArtilleryTower(tileX * 64.0, tileY * 64.0);
                if (gameController.purchaseAndPlaceTower(tower)) { // Use new method
                    renderTimer.setStatusMessage("Artillery Tower placed!");
                    clearActivePopup();
                } else {
                    renderTimer.setStatusMessage("Cannot place Artillery Tower. Gold: " + gameController.getPlayerGold());
                }
            } else {
                renderTimer.setStatusMessage("Not enough gold for Artillery Tower.");
            }
        });
        
        // Close Button
        Button closeButton = UIAssets.createIconButton("Close", 3, 0, POPUP_ICON_SIZE * 0.8); // Slightly smaller close button
        closeButton.setOnAction(e -> clearActivePopup());

        popup.getChildren().addAll(archerButton, mageButton, artilleryButton, closeButton);
        positionAndShowPopup(popup, screenX, screenY);
    }

    private void createUpgradeSellPopup(double screenX, double screenY, Tower existingTower, int tileX, int tileY) {
        clearActivePopup();
        HBox popup = new HBox(POPUP_SPACING);
        popup.setPadding(new Insets(5));
        popup.setStyle("-fx-background-color: rgba(50, 50, 50, 0.85); -fx-background-radius: 5; -fx-border-color: grey; -fx-border-radius: 5;");

        // Upgrade Button (Star Icon: 1,2)
        // Assuming Tower class has getUpgradeCost() and canUpgrade() methods
        if (existingTower.canUpgrade() && gameController.getPlayerGold() >= existingTower.getUpgradeCost()) {
            Button upgradeButton = UIAssets.createIconButton("Upgrade Tower (Cost: " + existingTower.getUpgradeCost() + ")", 1, 2, POPUP_ICON_SIZE);
            upgradeButton.setOnAction(e -> {
                if (gameController.upgradeTower(existingTower)) { // upgradeTower in GameController should handle gold deduction
                    renderTimer.setStatusMessage(existingTower.getName() + " upgraded!");
                    clearActivePopup();
                } else {
                    renderTimer.setStatusMessage("Upgrade failed or not enough gold."); // Or more specific message from controller
                }
            });
            popup.getChildren().add(upgradeButton);
        } else if (existingTower.canUpgrade()) {
             Button upgradeButtonDisabled = UIAssets.createIconButton("Upgrade (Need: "+ existingTower.getUpgradeCost() +"g)", 1, 2, POPUP_ICON_SIZE);
             upgradeButtonDisabled.setDisable(true);
             popup.getChildren().add(upgradeButtonDisabled);
        } // Else: cannot upgrade further, don't show button

        // Sell Button (Garbage Icon: 1,0)
        Button sellButton = UIAssets.createIconButton("Sell Tower (Refund: " + existingTower.getSellRefund() + ")", 1, 0, POPUP_ICON_SIZE);
        sellButton.setOnAction(e -> {
            int refund = gameController.sellTower(existingTower.getX(), existingTower.getY());
            if (refund > 0) {
                renderTimer.setStatusMessage("Tower sold for " + refund + " gold.");
            } else {
                renderTimer.setStatusMessage("Could not sell tower."); // Should not happen if button is shown for existing tower
            }
            clearActivePopup();
        });
        popup.getChildren().add(sellButton);
        
        // Close Button
        Button closeButton = UIAssets.createIconButton("Close", 3, 0, POPUP_ICON_SIZE * 0.8);
        closeButton.setOnAction(e -> clearActivePopup());
        popup.getChildren().add(closeButton);

        if (popup.getChildren().isEmpty()) { // Should not happen if called for existing tower, but as safeguard
            if(!closeButton.isArmed()) popup.getChildren().add(closeButton); // at least show a close button
            else return; // No actions possible and no close button
        }
        
        positionAndShowPopup(popup, screenX, screenY);
    }

    private void positionAndShowPopup(Node popupNode, double clickScreenX, double clickScreenY) {
        // Position the popup near the mouse click
        // Adjustments might be needed to ensure it's fully on screen
        double popupX = clickScreenX;
        double popupY = clickScreenY;

        // Add to overlay pane
        uiOverlayPane.getChildren().add(popupNode);
        activePopup = popupNode;

        // Perform layout pass to get actual dimensions of the popup
        popupNode.applyCss();
        if (popupNode instanceof javafx.scene.layout.Region) {
            ((javafx.scene.layout.Region) popupNode).autosize();
        }

        // Adjust position to keep popup on screen
        // Get bounds of the uiOverlayPane (which should be same size as canvas/scene)
        double sceneWidth = uiOverlayPane.getWidth();
        double sceneHeight = uiOverlayPane.getHeight();
        
        double popupWidth = popupNode.getBoundsInParent().getWidth();
        double popupHeight = popupNode.getBoundsInParent().getHeight();

        if (popupX + popupWidth > sceneWidth) {
            popupX = sceneWidth - popupWidth - POPUP_SPACING; // Move left
        }
        if (popupY + popupHeight > sceneHeight) {
            popupY = sceneHeight - popupHeight - POPUP_SPACING; // Move up
        }
        if (popupX < 0) {
            popupX = POPUP_SPACING;
        }
        if (popupY < 0) {
            popupY = POPUP_SPACING;
        }

        popupNode.setLayoutX(popupX);
        popupNode.setLayoutY(popupY);
    }
}
