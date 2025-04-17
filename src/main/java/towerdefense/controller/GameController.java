package towerdefense.controller;

import javafx.animation.AnimationTimer; // For game loop
import towerdefense.model.GameModel;
// Import specific model elements if needed (Enemy, Tower, Projectile)

/**
 * Controller for the main game logic.
 * Handles game state updates, user input during gameplay, and game loop.
 */
public class GameController {

    private GameModel model;
    private String currentMapName; // Or reference to loaded map object
    private GameLoopTimer gameLoop;
    private boolean isPaused = false;
    private double gameSpeed = 1.0; // Multiplier for game time

    // Maybe hold reference to view to call update methods?
    // private towerdefense.view.screens.GameScreen gameView;

    public GameController(GameModel model, String selectedMap) {
        this.model = model;
        this.currentMapName = selectedMap;
        // TODO: Load the actual map data into the model based on selectedMap name
        // model.loadMap(selectedMap);
        System.out.println("GameController initialized for map: " + selectedMap);
        this.gameLoop = new GameLoopTimer();
    }

    /**
     * Starts the game loop.
     */
    public void startGame() {
        System.out.println("GameController: Starting game...");
        isPaused = false;
        gameSpeed = 1.0;
        // TODO: Initialize game state in the model (wave 1, starting gold, lives etc.)
        // model.initializeGame();
        gameLoop.start();
    }

    /**
     * Stops the game loop.
     */
    public void stopGame() {
        System.out.println("GameController: Stopping game...");
        gameLoop.stop();
    }

    /**
     * Handles pausing/resuming the game.
     */
    public void handlePauseToggle() {
        isPaused = !isPaused;
        System.out.println("GameController: Game " + (isPaused ? "paused" : "resumed"));
        if (isPaused) {
            // gameLoop.stop(); // Or just skip update logic inside loop
        } else {
            // gameLoop.start(); // If stopped
        }
    }

    /**
     * Handles changing the game speed.
     */
    public void handleSpeedToggle() {
        if (gameSpeed == 1.0) {
            gameSpeed = 2.0;
        } else {
            gameSpeed = 1.0;
        }
        System.out.println("GameController: Game speed set to x" + gameSpeed);
        // The game loop will use this speed multiplier
    }

    /**
     * Handles selecting a tower type to build.
     * 
     * @param towerType The identifier of the tower type.
     */
    public void handleTowerSelection(int towerType) {
        System.out.println("Controller: Selected Tower Type for placement: " + towerType);
        // TODO: Store the selected tower type, possibly change cursor, wait for click
        // on empty lot
        // model.setSelectedTowerToBuild(towerType);
    }

    /**
     * Handles clicking on an empty tower lot on the map.
     * 
     * @param lotId Identifier for the clicked empty lot.
     */
    public void handlePlaceTower(Object lotId) {
        System.out.println("Controller: Attempting to place tower at lot: " + lotId);
        // TODO: Check if a tower type is selected and player has enough gold
        // int selectedType = model.getSelectedTowerToBuild();
        // if (selectedType != null && model.canAffordTower(selectedType)) {
        // boolean success = model.buildTower(lotId, selectedType);
        // if (success) { // Update view }
        // }
    }

    // --- Game Loop --- //
    private class GameLoopTimer extends AnimationTimer {
        private long lastUpdateNanos = 0;

        @Override
        public void handle(long nowNanos) {
            if (lastUpdateNanos == 0) {
                lastUpdateNanos = nowNanos;
                return;
            }

            if (isPaused) {
                lastUpdateNanos = nowNanos; // Prevent large delta after unpausing
                return;
            }

            double elapsedSeconds = (nowNanos - lastUpdateNanos) / 1_000_000_000.0;
            lastUpdateNanos = nowNanos;

            double gameDeltaTime = elapsedSeconds * gameSpeed;

            // --- Update Game State --- //
            updateGameModel(gameDeltaTime);

            // --- Update UI --- //
            // This might involve calling methods on a view reference,
            // or the view observing the model.
            // if (gameView != null) {
            // gameView.updateUI(); // Update labels like gold, lives, wave
            // gameView.redrawGameBoard(); // Trigger redraw of enemies, towers, projectiles
            // }

            // --- Check Game Over/Win Conditions --- //
            if (model.isGameOver()) {
                System.out.println("Controller: Game Over detected!");
                stopGame();
                // TODO: Show Game Over message/screen
                // Main.loadGameOverScreen(); ??
            } else if (model.isGameWon()) {
                System.out.println("Controller: Game Won detected!");
                stopGame();
                // TODO: Show Victory message/screen
                // Main.loadVictoryScreen(); ??
            }
        }
    }

    /**
     * Updates the game model state based on elapsed time.
     * 
     * @param deltaTime Time elapsed since last update (adjusted for game speed).
     */
    private void updateGameModel(double deltaTime) {
        // TODO: Implement all game logic updates here, calling methods on the model
        // - Spawn enemies based on wave timing
        // - Move enemies along path
        // - Towers acquire targets and fire projectiles
        // - Move projectiles
        // - Handle projectile hits and enemy damage/death
        // - Handle enemies reaching the exit
        // - Update gold

        // Example calls (replace with actual model methods):
        // model.spawnEnemies(deltaTime);
        // model.moveEnemies(deltaTime);
        // model.updateTowers(deltaTime);
        // model.moveProjectiles(deltaTime);
        // model.handleCollisions();
        // model.checkEndOfPath();
    }
}