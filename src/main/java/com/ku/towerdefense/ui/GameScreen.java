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
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * The main game screen where the tower defense gameplay takes place.
 */
public class GameScreen extends BorderPane {
    private final Stage primaryStage;
    private final GameController gameController;
    private Canvas gameCanvas;
    private GameRenderTimer renderTimer;
    private Tower selectedTower;
    
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

            // Clear the canvas
            gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

            // Render game elements
            gameController.render(gc);

            // Render tower preview if a tower is selected (follow mouse)
            if (selectedTower != null && mouseInCanvas) {
                // Convert to grid coordinates
                int tileX = (int)(mouseX / 32);
                int tileY = (int)(mouseY / 32);

                // Get center of the tile
                double centerX = tileX * 32 + 16;
                double centerY = tileY * 32 + 16;

                // Check if we can place here
                boolean canPlace = gameController.getGameMap().canPlaceTower(centerX, centerY);

                // Draw a preview circle
                gc.setGlobalAlpha(0.5);
                gc.setFill(canPlace ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
                gc.fillOval(centerX - 16, centerY - 16, 32, 32);

                // Draw range preview
                gc.setStroke(javafx.scene.paint.Color.WHITE);
                gc.setGlobalAlpha(0.2);
                if (selectedTower instanceof ArcherTower) {
                    gc.strokeOval(centerX - 150, centerY - 150, 300, 300);
                } else if (selectedTower instanceof ArtilleryTower) {
                    gc.strokeOval(centerX - 120, centerY - 120, 240, 240);
                } else if (selectedTower instanceof MageTower) {
                    gc.strokeOval(centerX - 140, centerY - 140, 280, 280);
                }

                gc.setGlobalAlpha(1.0);
            }
            if (lastTime < 0) lastTime = now;
            double deltaTime = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            gameController.update(deltaTime);


            gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

            gameController.render(gc);

            // Status message (fades after 3 seconds)
            long currentTime = System.currentTimeMillis();
            if (currentTime - statusTimestamp < 3000) {
                double alpha = 1.0 - (currentTime - statusTimestamp) / 3000.0;
                gc.setGlobalAlpha(alpha);
                gc.setFill(javafx.scene.paint.Color.WHITE);
                gc.setStroke(javafx.scene.paint.Color.BLACK);
                gc.setLineWidth(2);
                gc.fillText(statusMessage, 10, gameCanvas.getHeight() - 20);
                gc.strokeText(statusMessage, 10, gameCanvas.getHeight() - 20);
                gc.setGlobalAlpha(1.0);
            }
            
            // Debug information to see what's being rendered
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillText("Towers: " + gameController.getTowers().size(), 10, 20);
            gc.fillText("Enemies: " + gameController.getEnemies().size(), 10, 40);
            gc.fillText("Wave: " + gameController.getCurrentWave(), 10, 60);
            
            // Display workaround instructions if assets aren't loading properly
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
        
        // Create the game canvas
        int canvasWidth = gameController.getGameMap().getWidth() * 32; // 32 pixels per tile
        int canvasHeight = gameController.getGameMap().getHeight() * 32;
        gameCanvas = new Canvas(canvasWidth, canvasHeight);
        
        // Add click handler for tower placement
        gameCanvas.setOnMouseClicked(e -> {
            if (e.isPrimaryButtonDown()) {
                // Left click to place tower
                if (selectedTower != null) {
                    int x = (int) e.getX();
                    int y = (int) e.getY();
                    placeTower(x, y);
                } else {
                    // If no tower is selected for placement, try to select an existing tower
                    selectTowerAt(e.getX(), e.getY());
                }
            } else if (e.isSecondaryButtonDown()) {
                // Right click to sell tower
                int x = (int) e.getX();
                int y = (int) e.getY();
                sellTower(x, y);
            } else if (e.isMiddleButtonDown()) {
                // Middle click to select/inspect tower
                selectTowerAt(e.getX(), e.getY());
            }
        });
        
        // Create sidebar with tower options and controls
        VBox sidebar = createSidebar();
        
        // Layout
        setTop(topBar);
        setCenter(gameCanvas);
        setRight(sidebar);
        
        // Start the game controller
        gameController.startGame();
    }
    
    /**
     * Create the top bar with game information.
     *
     * @return the top bar container
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(30);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #333333;");
        
        // Load game UI image to create status indicators
        javafx.scene.image.Image gameUIImage = UIAssets.getImage("GameUI");
        
        // Lives indicator with icon
        ImageView livesIcon = null;
        if (gameUIImage != null) {
            // Extract the hearts icon from the UI image (adjust the values based on actual image)
            livesIcon = new ImageView(gameUIImage);
            livesIcon.setViewport(new javafx.geometry.Rectangle2D(0, 0, 32, 32));
        }
        
        Label livesLabel = new Label("Lives: " + gameController.getPlayerLives());
        livesLabel.getStyleClass().add("game-info-text");
        
        HBox livesBox = new HBox(5, livesIcon != null ? livesIcon : new javafx.scene.layout.Pane(), livesLabel);
        livesBox.setAlignment(Pos.CENTER_LEFT);
        
        // Gold indicator with icon
        ImageView goldIcon = null;
        if (gameUIImage != null) {
            // Extract the coin icon from the UI image (adjust the values based on actual image)
            goldIcon = new ImageView(gameUIImage);
            goldIcon.setViewport(new javafx.geometry.Rectangle2D(32, 0, 32, 32));
        }
        
        Label goldLabel = new Label("Gold: " + gameController.getPlayerGold());
        goldLabel.getStyleClass().add("game-info-text");
        
        HBox goldBox = new HBox(5, goldIcon != null ? goldIcon : new javafx.scene.layout.Pane(), goldLabel);
        goldBox.setAlignment(Pos.CENTER_LEFT);
        
        // Wave indicator with icon
        ImageView waveIcon = null;
        if (gameUIImage != null) {
            // Extract the wave icon from the UI image (adjust the values based on actual image)
            waveIcon = new ImageView(gameUIImage);
            waveIcon.setViewport(new javafx.geometry.Rectangle2D(64, 0, 32, 32));
        }
        
        Label waveLabel = new Label("Wave: " + gameController.getCurrentWave());
        waveLabel.getStyleClass().add("game-info-text");
        
        HBox waveBox = new HBox(5, waveIcon != null ? waveIcon : new javafx.scene.layout.Pane(), waveLabel);
        waveBox.setAlignment(Pos.CENTER_LEFT);
        
        // Menu button
        Button menuButton = new Button("Menu");
        UIAssets.styleButton(menuButton, "blue");
        menuButton.setOnAction(e -> openPauseMenu());
        
        topBar.getChildren().addAll(livesBox, goldBox, waveBox, menuButton);
        
        // Update the labels when values change
        AnimationTimer updateLabels = new AnimationTimer() {
            @Override
            public void handle(long now) {
                livesLabel.setText("Lives: " + gameController.getPlayerLives());
                goldLabel.setText("Gold: " + gameController.getPlayerGold());
                waveLabel.setText("Wave: " + gameController.getCurrentWave());
                
                if (gameController.isGameOver()) {
                    stop();
                    showGameOverScreen();
                }
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
    
    /**
     * Create a tower button with price information.
     *
     * @param name tower name
     * @param cost tower cost
     * @return configured button
     */
    private Button createTowerButton(String name, int cost) {
        Button button = new Button(name + " ($" + cost + ")");
        button.setPrefWidth(150);
        return button;
    }
    
    /**
     * Place the selected tower at the specified coordinates.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    private void placeTower(int x, int y) {
        if (selectedTower != null) {
            // Convert to grid coordinates
            int tileX = x / 32;
            int tileY = y / 32;
            
            // Get center of the tile
            double centerX = tileX * 32 + 16;
            double centerY = tileY * 32 + 16;
            
            // Create the appropriate tower type at the center of the tile
            Tower towerToPlace = null;
            if (selectedTower instanceof ArcherTower) {
                towerToPlace = new ArcherTower(centerX - 32, centerY - 32); // Adjust for tower size
            } else if (selectedTower instanceof ArtilleryTower) {
                towerToPlace = new ArtilleryTower(centerX - 32, centerY - 32);
            } else if (selectedTower instanceof MageTower) {
                towerToPlace = new MageTower(centerX - 32, centerY - 32);
            }
            
            // Try to place the tower
            if (towerToPlace != null) {
                boolean placed = gameController.placeTower(towerToPlace);
                if (!placed) {
                    // Show error message
                    System.out.println("Cannot place tower here!");
                    renderTimer.setStatusMessage("Cannot place tower here!");
                } else {
                    System.out.println("Tower placed at tile: " + tileX + ", " + tileY);
                    renderTimer.setStatusMessage("Tower placed at tile: " + tileX + ", " + tileY);
                }
            }
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
        gameController.stopGame();
        renderTimer.stop();
        
        // Create and show pause menu
        // In a real implementation, this would create a popup or overlay
        System.out.println("Game paused!");
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
     * Sell a tower at the specified coordinates.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    private void sellTower(int x, int y) {
        int refundAmount = gameController.sellTower(x, y);
        if (refundAmount > 0) {
            renderTimer.setStatusMessage("Tower sold for $" + refundAmount + "!");
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
}
