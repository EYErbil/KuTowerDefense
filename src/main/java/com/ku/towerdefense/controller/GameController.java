package com.ku.towerdefense.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.ArrayDeque;

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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.animation.Animation;

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
        if (playerGold >= tower.getCost() && gameMap.canPlaceTower(tower.getX(), tower.getY(), towers)) {
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

        // Set the path for the enemy to follow
        if (gameMap.getEnemyPath() != null) {
            enemy.setPath(gameMap.getEnemyPath());
            enemies.add(enemy);
            System.out.println("Added test enemy");
        } else {
            System.out.println("Cannot add enemy: no path available");
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
     * Starts the next wave of enemies.
     */
    public void startNextWave() {
        // Enhanced debugging for enemy path issues
        System.out.println("startNextWave called - attempting to start wave " + (currentWave + 1));
        
        // Check if path exists
        if (gameMap.getEnemyPath() == null) {
             System.err.println("ERROR: Cannot start wave - No path defined on the map. Place Start and End tiles.");
             
             // Debug extra information to help diagnose
             boolean hasStartTile = false;
             boolean hasEndTile = false;
             for (int x = 0; x < gameMap.getWidth(); x++) {
                 for (int y = 0; y < gameMap.getHeight(); y++) {
                     if (gameMap.getTileType(x, y) == TileType.START_POINT) {
                         hasStartTile = true;
                         System.out.println("Found START_POINT at (" + x + "," + y + ")");
                     }
                     if (gameMap.getTileType(x, y) == TileType.END_POINT) {
                         hasEndTile = true;
                         System.out.println("Found END_POINT at (" + x + "," + y + ")");
                     }
                 }
             }

             if (!hasStartTile) System.err.println("Missing START_POINT tile on map");
             if (!hasEndTile) System.err.println("Missing END_POINT tile on map");
             
             // Try to force path generation if we have start and end points but no path
             if (hasStartTile && hasEndTile) {
                 System.out.println("Trying to force path generation since start and end points exist...");
                 gameMap.generatePath();
                 
                 // Check if it worked
                 if (gameMap.getEnemyPath() == null) {
                     System.err.println("Path generation failed. Please check map configuration.");
                     return;
                 } else {
                     System.out.println("Path was successfully generated!");
                 }
             } else {
                 return; // Exit if we don't have both required points
             }
        }

        // Increment wave counter
        currentWave++;
        System.out.println("Starting wave " + currentWave);
        
        // Calculate enemy numbers
        int num = GameSettings.getInstance().getEnemiesPerGroup() * (1 + currentWave/3); // Example scaling
        int goblins = (int)(num * GameSettings.getInstance().getGoblinPercentage() / 100.0);
        int knights  = num - goblins;
        System.out.println("Wave " + currentWave + " will have " + goblins + " goblins and " + knights + " knights");

        // Find start point
        Point2D start = gameMap.getStartPoint();
        if (start == null) {
             System.err.println("ERROR: Cannot start wave - Start point not found on map.");
             return;
        }
        System.out.println("Using start point at: (" + start.getX() + ", " + start.getY() + ")");

        // Create enemy queue
        Queue<Enemy> queue = new ArrayDeque<>();
        for (int i=0;i<goblins;i++)  queue.add(new Goblin(start.getX(), start.getY()));
        for (int i=0;i<knights;i++)  queue.add(new Knight(start.getX(), start.getY()));
        System.out.println("Created queue with " + queue.size() + " enemies");

        // Setting up spawning
        isSpawningEnemies = true;
        double delay = GameSettings.getInstance().getEnemyDelay() / 1000.0; // Convert ms to seconds
        System.out.println("Enemy spawn delay: " + delay + " seconds");

        // Create and start Timeline for enemy spawning using AtomicReference to avoid initialization issues
        final Timeline[] spawnerRef = new Timeline[1];
        Timeline spawner = new Timeline(
            new KeyFrame(Duration.seconds(delay), e -> {
                Enemy next = queue.poll();
                if (next == null) {
                    isSpawningEnemies = false;
                    spawnerRef[0].stop();  // Use the reference instead of direct variable
                    System.out.println("Wave " + currentWave + " spawning complete.");
                    return;
                }
                
                // Ensure the enemy has the path reference
                GamePath path = gameMap.getEnemyPath();
                if (path != null) {
                    next.setPath(path);
                    enemies.add(next);
                    System.out.println("Spawned " + (next instanceof Goblin ? "Goblin" : "Knight") + 
                                     " at (" + next.getX() + "," + next.getY() + ")");
                } else {
                    System.err.println("ERROR: Enemy path disappeared during spawning!");
                }
            })
        );
        spawnerRef[0] = spawner;  // Store the Timeline in the array reference
        spawner.setCycleCount(Animation.INDEFINITE);
        spawner.play();

        System.out.println("Wave " + currentWave + " spawning started!");
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

    /**
     * Reinitialize all entities after loading a saved game.
     * This ensures that images and other transient fields are properly reloaded.
     */
    public void reinitializeAfterLoad() {
        System.out.println("GameController: Reinitializing all entities after loading saved game");
        
        // First, ensure the map path is properly initialized
        if (gameMap != null) {
            // Force map to regenerate tiles and paths
            if (gameMap.getEnemyPath() == null) {
                System.out.println("Regenerating enemy path in map...");
                gameMap.generatePath();
            }
            
            // Reload map tile images if needed
            for (int x = 0; x < gameMap.getWidth(); x++) {
                for (int y = 0; y < gameMap.getHeight(); y++) {
                    if (gameMap.getTile(x, y) != null) {
                        gameMap.getTile(x, y).reinitializeAfterLoad();
                    }
                }
            }
        }
        
        // Reload enemy images
        System.out.println("Reinitializing " + enemies.size() + " enemies");
        for (Enemy enemy : enemies) {
            enemy.reinitializeAfterLoad();
        }
        
        // Reload projectile images
        System.out.println("Reinitializing " + projectiles.size() + " projectiles");
        for (Projectile projectile : projectiles) {
            projectile.reinitializeAfterLoad();
        }
        
        // Reload tower images
        System.out.println("Reinitializing " + towers.size() + " towers");
        for (Tower tower : towers) {
            tower.reinitializeAfterLoad();
        }
        
        System.out.println("GameController: Reinitialization complete");
    }
} 