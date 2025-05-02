package com.ku.towerdefense.controller;

import java.util.ArrayList;
import java.util.List;

import com.ku.towerdefense.model.GamePath;
import com.ku.towerdefense.model.entity.Enemy;
import com.ku.towerdefense.model.entity.Goblin;
import com.ku.towerdefense.model.entity.Knight;
import com.ku.towerdefense.model.entity.Projectile;
import com.ku.towerdefense.model.entity.Tower;
import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.TileType;
import com.ku.towerdefense.util.GameSettings;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Main controller for the game, handling the game loop, entities, and game state.
 */
public class GameController {
    private GameMap gameMap;
    private List<Tower> towers;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;
    private int playerGold;
    private int playerLives;
    private int currentWave;
    private boolean gameOver;
    private AnimationTimer gameLoop;

    // Time between waves in milliseconds
    private static final long WAVE_BREAK_TIME = 5000;
    private boolean betweenWaves = false;
    private long waveStartTime = 0;
    private boolean isSpawningEnemies = false;

    // Game speed control
    private boolean speedAccelerated = false;
    private static final double SPEED_MULTIPLIER = 2.0;

    // Listener for wave events
    private WaveCompletedListener onWaveCompletedListener;

    /**
     * Creates a new game controller with the specified game map.
     *
     * @param gameMap the game map to use
     */
    public GameController(GameMap gameMap) {
        this.gameMap = gameMap;
        this.towers = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.playerGold = GameSettings.getInstance().getStartingGold();
        this.playerLives = GameSettings.getInstance().getStartingLives();
        this.currentWave = 0;
        this.gameOver = false;

        // Initialize a basic path if no paths exist
        if (gameMap.getEnemyPaths().isEmpty()) {
            // Set start and end points
            gameMap.setTileType(0, 5, TileType.START_POINT);
            gameMap.setTileType(gameMap.getWidth() - 1, 5, TileType.END_POINT);

            // This will generate paths between the start and end points
            gameMap.generatePath();
        }

        // Initialize game loop
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0; // Convert to seconds
                lastUpdate = now;

                update(deltaTime);
            }
        };
    }

    /**
     * Starts the game loop.
     */
    public void startGame() {
        gameLoop.start();
    }

    /**
     * Stops the game loop.
     */
    public void stopGame() {
        gameLoop.stop();
    }
    public void pauseGame() {
        // Add any game-specific pause logic here
    }

    public void resumeGame() {
        // Add any game-specific resume logic here
    }



    /**
     * Updates the game state.
     *
     * @param deltaTime time elapsed since the last update in seconds
     */
    public void update(double deltaTime) {
        if (gameOver) {
            return;
        }

        // Apply speed multiplier if accelerated
        if (speedAccelerated) {
            deltaTime *= SPEED_MULTIPLIER;
        }

        // Update towers and collect projectiles
        for (Tower tower : towers) {
            Projectile projectile = tower.update(deltaTime, enemies);
            if (projectile != null) {
                projectiles.add(projectile);
            }
        }

        // Update projectiles and check for hits
        List<Projectile> projectilesToRemove = new ArrayList<>();
        for (Projectile projectile : projectiles) {
            boolean hit = projectile.update(deltaTime);
            if (hit || !projectile.isActive()) {
                projectilesToRemove.add(projectile);
                if (hit) {
                    // Apply damage to the target
                    Enemy target = projectile.getTarget();
                    if (target != null) {
                        target.applyDamage(projectile.getDamage(), projectile.getDamageType());

                        // Handle area of effect damage
                        if (projectile.hasAoeEffect()) {
                            for (Enemy enemy : enemies) {
                                if (enemy != target &&
                                        enemy.distanceTo(target) <= projectile.getAoeRange()) {
                                    enemy.applyDamage(projectile.getDamage() / 2, projectile.getDamageType());
                                }
                            }
                        }
                    }
                }
            }
        }
        projectiles.removeAll(projectilesToRemove);

        // Update enemies and check for ones that reached the end
        List<Enemy> enemiesToRemove = new ArrayList<>();
        for (Enemy enemy : enemies) {
            boolean reachedEnd = enemy.update(deltaTime);

            if (reachedEnd) {
                enemiesToRemove.add(enemy);
                playerLives--;

                if (playerLives <= 0) {
                    gameOver = true;
                    stopGame();
                }
            } else if (enemy.getCurrentHealth() <= 0) {
                enemiesToRemove.add(enemy);
                playerGold += enemy.getGoldReward();
            }
        }

        // Remove dead or escaped enemies
        enemies.removeAll(enemiesToRemove);

        // Check if wave is completed and all enemies are spawned
        if (enemies.isEmpty() && !isSpawningEnemies && currentWave > 0) {
            // Give player time before the next wave
            if (!betweenWaves) {
                betweenWaves = true;
                waveStartTime = System.currentTimeMillis() + WAVE_BREAK_TIME; // 5 seconds break

                // Give gold bonus for completing the wave
                int waveBonus = currentWave * 10;
                playerGold += waveBonus;

                // Notify listeners for UI update
                if (onWaveCompletedListener != null) {
                    onWaveCompletedListener.onWaveCompleted(currentWave, waveBonus);
                }
            } else if (System.currentTimeMillis() >= waveStartTime) {
                // Start the next wave after the break time has passed
                betweenWaves = false;
                startNextWave();
            }
        }
    }

    /**
     * Renders all game elements.
     *
     * @param gc the graphics context to render on
     */
    public void render(GraphicsContext gc) {
        // Render map
        gameMap.render(gc);

        // Render enemies
        for (Enemy enemy : enemies) {
            enemy.render(gc);
        }

        // Render towers
        for (Tower tower : towers) {
            tower.render(gc);
        }

        // Render projectiles
        for (Projectile projectile : projectiles) {
            projectile.render(gc);
        }

        // Additional UI rendering can be handled elsewhere
    }

    /**
     * Places a tower at the specified position if there's enough gold.
     *
     * @param tower the tower to place
     * @return true if the tower was placed, false otherwise
     */
    public boolean placeTower(Tower tower) {
        if (playerGold >= tower.getCost() && gameMap.canPlaceTower(tower.getX(), tower.getY())) {
            towers.add(tower);
            playerGold -= tower.getCost();
            return true;
        }
        return false;
    }

    /**
     * Add a test enemy to the game.
     * This is for demonstration purposes.
     */
    public void addTestEnemy() {
        // Create a new enemy and add it to the list
        Enemy enemy;
        if (Math.random() < 0.5) {
            enemy = new Goblin(50, 50);
        } else {
            enemy = new Knight(50, 50);
        }

        // Set a random path for the enemy to follow
        GamePath randomPath = gameMap.getRandomPath();
        if (randomPath != null) {
            enemy.setPath(randomPath);
            enemies.add(enemy);
            System.out.println("Added test enemy with random path");
        } else {
            System.out.println("Cannot add enemy: no paths available");
        }
    }

    /**
     * Interface for wave completed event
     */
    public interface WaveCompletedListener {
        void onWaveCompleted(int waveNumber, int goldBonus);
    }

    /**
     * Set listener for wave completed events
     *
     * @param listener the listener to set
     */
    public void setOnWaveCompletedListener(WaveCompletedListener listener) {
        this.onWaveCompletedListener = listener;
    }

    /**
     * Add a wave of enemies to the game.
     * This will spawn multiple enemies over time.
     */
    public void startNextWave() {
        currentWave++;

        // Calculate number of enemies based on wave number and settings
        int numEnemies = GameSettings.getInstance().getEnemiesPerGroup() *
                (1 + currentWave / 3); // Increase enemies as waves progress

        // Calculate goblin/knight ratio based on settings
        int goblinPercentage = GameSettings.getInstance().getGoblinPercentage();
        final int totalGoblins = (int)(numEnemies * (goblinPercentage / 100.0));
        final int totalKnights = numEnemies - totalGoblins;

        System.out.println("Starting wave " + currentWave + " with " + totalGoblins + " goblins and " + totalKnights + " knights");

        // Use JavaFX Timeline instead of Thread to prevent race conditions
        isSpawningEnemies = true;

        // Create counters outside the lambda
        final int[] remainingGoblins = {totalGoblins};
        final int[] remainingKnights = {totalKnights};

        // Create a timeline for spawning enemies
        javafx.animation.Timeline spawner = new javafx.animation.Timeline();
        spawner.setCycleCount(totalGoblins + totalKnights);

        // Base delay between enemy spawns
        double baseDelay = GameSettings.getInstance().getEnemyDelay() / 1000.0; // Convert to seconds

        // Keyframe for spawning one enemy at a time
        spawner.getKeyFrames().add(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(baseDelay), e -> {
                    // Get the start point from the map
                    Point2D startPoint = gameMap.getStartPoint();
                    if (startPoint == null) {
                        System.err.println("No start point found on map!");
                        return;
                    }

                    // Determine if we should spawn a goblin or knight
                    Enemy enemy;
                    if (remainingGoblins[0] > 0) {
                        enemy = new Goblin(startPoint.getX(), startPoint.getY());
                        remainingGoblins[0]--;
                    } else {
                        enemy = new Knight(startPoint.getX(), startPoint.getY());
                        remainingKnights[0]--;
                    }

                    // Set a random path for the enemy
                    GamePath randomPath = gameMap.getRandomPath();
                    if (randomPath != null) {
                        enemy.setPath(randomPath);
                        enemies.add(enemy);
                        System.out.println("Spawned " + enemy.getClass().getSimpleName() +
                                " at (" + startPoint.getX() + "," + startPoint.getY() + ") with random path");
                    } else {
                        System.err.println("No paths available!");
                    }
                })
        );

        // Mark spawning as complete when timeline finishes
        spawner.setOnFinished(e -> {
            isSpawningEnemies = false;
            System.out.println("Wave " + currentWave + " spawning complete");
        });

        // Start the timeline
        spawner.play();
    }

    /**
     * Sells a tower at the specified position if one exists there.
     *
     * @param x x coordinate (pixels)
     * @param y y coordinate (pixels)
     * @return the amount of gold refunded, or 0 if no tower was sold
     */
    public int sellTower(double x, double y) {
        // Convert to tile coordinates
        int tileX = (int) (x / 32);
        int tileY = (int) (y / 32);

        // Find a tower at this location
        Tower towerToSell = null;
        for (Tower tower : towers) {
            int towerTileX = (int) (tower.getCenterX() / 32);
            int towerTileY = (int) (tower.getCenterY() / 32);

            if (towerTileX == tileX && towerTileY == tileY) {
                towerToSell = tower;
                break;
            }
        }

        // If we found a tower, sell it
        if (towerToSell != null) {
            int refundAmount = towerToSell.getSellRefund();
            towers.remove(towerToSell);
            playerGold += refundAmount;
            return refundAmount;
        }

        return 0;
    }

    /**
     * Select a tower at the specified position.
     *
     * @param x x coordinate (pixels)
     * @param y y coordinate (pixels)
     * @return the selected tower or null if none exists at that position
     */
    public Tower selectTowerAt(double x, double y) {
        // Clear selection from all towers first
        for (Tower tower : towers) {
            tower.setSelected(false);
        }

        // Convert to tile coordinates
        int tileX = (int) (x / 32);
        int tileY = (int) (y / 32);

        // Find a tower at this location
        Tower selectedTower = null;
        for (Tower tower : towers) {
            int towerTileX = (int) (tower.getCenterX() / 32);
            int towerTileY = (int) (tower.getCenterY() / 32);

            if (towerTileX == tileX && towerTileY == tileY) {
                tower.setSelected(true);
                selectedTower = tower;
                break;
            }
        }

        return selectedTower;
    }

    /**
     * Check if game speed is accelerated.
     *
     * @return true if speed is accelerated, false otherwise
     */
    public boolean isSpeedAccelerated() {
        return speedAccelerated;
    }

    /**
     * Set the game speed acceleration.
     *
     * @param speedAccelerated true to accelerate, false for normal speed
     */
    public void setSpeedAccelerated(boolean speedAccelerated) {
        this.speedAccelerated = speedAccelerated;
    }

    /**
     * Getters and setters
     */
    public GameMap getGameMap() {
        return gameMap;
    }

    public List<Tower> getTowers() {
        return towers;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public int getPlayerGold() {
        return playerGold;
    }

    public int getPlayerLives() {
        return playerLives;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public boolean isGameOver() {
        return gameOver;
    }
} 