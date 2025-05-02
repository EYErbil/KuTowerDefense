package com.ku.towerdefense.ui;

import com.ku.towerdefense.controller.GameController;
import com.ku.towerdefense.model.entity.ArcherTower;
import com.ku.towerdefense.model.entity.ArtilleryTower;
import com.ku.towerdefense.model.entity.MageTower;
import com.ku.towerdefense.model.entity.Tower;

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
import javafx.scene.transform.Affine;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.ImageCursor;

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
    private final StackPane canvasContainer = new StackPane();
    private final Affine worldTransform = new Affine();
    
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
            double worldWidth = gameController.getGameMap().getWidth() * 32.0;
            double worldHeight = gameController.getGameMap().getHeight() * 32.0;

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
                    // Convert world coordinates to grid coordinates (original logic)
                    int tileX = (int)(worldMouse.getX() / 32);
                    int tileY = (int)(worldMouse.getY() / 32);

                    // Get center of the tile in world coordinates
                    double centerX = tileX * 32 + 16;
                    double centerY = tileY * 32 + 16;

                    // Check if we can place here (uses world coordinates)
                    boolean canPlace = gameController.getGameMap().canPlaceTower(centerX, centerY, gameController.getTowers());

                    // Draw preview circle in world coordinates
                    gc.setGlobalAlpha(0.5);
                    gc.setFill(canPlace ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
                    // Use world coordinates for drawing - transform handles canvas placement
                    gc.fillOval(centerX - 16, centerY - 16, 32, 32);

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
        setStyle("-fx-background-color: #111111;");
        
        // Create top bar with game info
        HBox topBar = createTopBar();
        
        // Create the game canvas (no initial size)
        gameCanvas = new Canvas();

        // Place canvas in a container that allows resizing
        canvasContainer.getChildren().add(gameCanvas);
        canvasContainer.setStyle("-fx-background-color: #222222;"); // Background for letterboxing

        // Bind canvas size to its container's size
        gameCanvas.widthProperty().bind(canvasContainer.widthProperty());
        gameCanvas.heightProperty().bind(canvasContainer.heightProperty());

        // --- Mouse Event Handling ---
        // Update mouse position for preview rendering
        gameCanvas.setOnMouseMoved(e -> renderTimer.setMousePosition(e.getX(), e.getY(), true));
        gameCanvas.setOnMouseExited(e -> renderTimer.setMousePosition(e.getX(), e.getY(), false));

        // Add click handler for tower placement/selection/selling
        gameCanvas.setOnMouseClicked(e -> {
             // Transform click coordinates from canvas space to world space
            javafx.geometry.Point2D worldCoord = transformMouseCoords(e.getX(), e.getY());

            if (worldCoord == null) return; // Click was outside the scaled world area

            double worldX = worldCoord.getX();
            double worldY = worldCoord.getY();

            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                if (selectedTower != null) {
                    // Pass world coordinates to placeTower
                    placeTower(worldX, worldY);
                } else {
                    // Pass world coordinates to selectTowerAt
                    selectTowerAt(worldX, worldY);
                }
            } else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                 // Pass world coordinates to sellTower
                 sellTower(worldX, worldY);
            } else if (e.getButton() == javafx.scene.input.MouseButton.MIDDLE) {
                 // Pass world coordinates to selectTowerAt (consistent with left-click select)
                 selectTowerAt(worldX, worldY);
            }
        });
        
        // Create sidebar with tower options and controls
        VBox sidebar = createSidebar();
        
        // Layout
        setTop(topBar);
        setCenter(canvasContainer); // Put the container in the center
        setRight(sidebar);
        
        // Request focus on the canvas container so keyboard events might work if needed later
        canvasContainer.requestFocus();
    }
    
    /**
     * Create the top bar with game information.
     *
     * @return the top bar container
     */
    private HBox createTopBar() {
    HBox topBar = new HBox(30);  // 30 pixels spacing between elements
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(10));
    topBar.setStyle("-fx-background-color: #333333;");

    // Lives indicator
    Label livesLabel = new Label("❤️ Lives: " + gameController.getPlayerLives());
    livesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

    // Gold indicator
    Label goldLabel = new Label("💰 Gold: " + gameController.getPlayerGold());
    goldLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

    // Wave indicator
    Label waveLabel = new Label("🌊 Wave: " + gameController.getCurrentWave());
    waveLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

    // Pause/Resume button
    Button pauseButton = new Button("⏸️ Pause");
    UIAssets.styleButton(pauseButton, "blue");
    pauseButton.setOnAction(e -> togglePause());

    // Menu button
    Button menuButton = new Button("Menu");
    UIAssets.styleButton(menuButton, "blue");
    menuButton.setOnAction(e -> openPauseMenu());

    // Add all elements to the top bar
    topBar.getChildren().addAll(livesLabel, goldLabel, waveLabel, pauseButton, menuButton);

    // Update the labels when values change
    AnimationTimer updateLabels = new AnimationTimer() {
        @Override
        public void handle(long now) {
            livesLabel.setText("❤️ Lives: " + gameController.getPlayerLives());
            goldLabel.setText("💰 Gold: " + gameController.getPlayerGold());
            waveLabel.setText("🌊 Wave: " + gameController.getCurrentWave());
            
            // Update pause button text based on game state
            pauseButton.setText(isPaused ? "▶️ Resume" : "⏸️ Pause");
        }
    };
    updateLabels.start();

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
        sidebar.setStyle("-fx-background-color: #333333;");
        
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
        if (selectedTower == null) {
            renderTimer.setStatusMessage("No tower type selected!");
            return;
        }

        // Determine tile based on world coordinates
        int tileX = (int)(worldX / 32);
        int tileY = (int)(worldY / 32);

        // Calculate center of the target tile 
        double centerX = tileX * 32 + 16;
        double centerY = tileY * 32 + 16;

        // Create the appropriate tower type instance
        Tower towerToPlace = null;
        int cost = 0;
        try {
             if (selectedTower instanceof ArcherTower) {
                 towerToPlace = new ArcherTower(0, 0);
                 cost = ((ArcherTower)selectedTower).getCost();
             } else if (selectedTower instanceof ArtilleryTower) {
                 towerToPlace = new ArtilleryTower(0, 0);
                 cost = ((ArtilleryTower)selectedTower).getCost();
             } else if (selectedTower instanceof MageTower) {
                 towerToPlace = new MageTower(0, 0);
                 cost = ((MageTower)selectedTower).getCost();
             }
        } catch (Exception e) {
             System.err.println("Error creating tower instance: " + e.getMessage());
             renderTimer.setStatusMessage("Error creating tower!");
             return;
        }

        // Try to place the tower via the controller
        if (towerToPlace != null) {
            // Set the position to match tile (we draw at 32x32 px now)
            // Since the rendering now directly uses the entity's top-left corner (x,y) to draw,
            // we need to position the tower at the top-left of the tile, not the center
            towerToPlace.setX(tileX * 32); // Align to top-left corner of the tile
            towerToPlace.setY(tileY * 32);
            
            // For range calculations, ensure the tower knows its correct center
            // (The entity's getCenterX/Y methods will calculate correctly by adding width/2, height/2)

            // Check cost again before placing
             if (gameController.getPlayerGold() >= cost) {
                 boolean placed = gameController.placeTower(towerToPlace);
                 if (placed) {
                     renderTimer.setStatusMessage(towerToPlace.getClass().getSimpleName() + " placed!");
                     selectedTower = null; // Clear selection after successful placement
                 } else {
                     renderTimer.setStatusMessage("Cannot place tower here (blocked or path)!");
                 }
             } else {
                 renderTimer.setStatusMessage("Not enough gold! Need $" + cost);
             }
        } else {
             renderTimer.setStatusMessage("Failed to create tower instance.");
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
        // Call controller method that accepts doubles
        int refundAmount = gameController.sellTower(worldX, worldY);
        if (refundAmount > 0) {
            renderTimer.setStatusMessage("Tower sold for " + refundAmount + " gold.");
        } else {
             // sellTower returns 0 if no tower found or couldn't sell
             renderTimer.setStatusMessage("No tower found at this location to sell.");
        }
    }
    
    /**
     * Select a tower at the specified coordinates.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    private void selectTowerAt(double x, double y) {
        Tower tower = gameController.selectTowerAt(x, y);
        if (tower != null) {
            String towerType = "";
            if (tower instanceof ArcherTower) {
                towerType = "Archer";
            } else if (tower instanceof ArtilleryTower) {
                towerType = "Artillery";
            } else if (tower instanceof MageTower) {
                towerType = "Mage";
            }
            
            renderTimer.setStatusMessage(
                towerType + " Tower | Damage: " + tower.getDamage() + 
                " | Range: " + tower.getRange() + 
                " | Sell: $" + tower.getSellRefund()
            );
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
}
