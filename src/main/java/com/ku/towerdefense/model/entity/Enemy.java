package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.model.Path;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for all enemy types in the game.
 */
public abstract class Enemy extends Entity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Static image cache for enemy types
    private static final Map<EnemyType, Image> ENEMY_IMAGES = new HashMap<>();
    
    // Static initializer
    static {
        loadEnemyImages();
    }
    
    protected int maxHealth;
    protected int currentHealth;
    protected double speed; // pixels per second
    protected int goldReward;
    protected EnemyType type;
    
    protected Path path;
    protected double pathProgress; // 0.0 to 1.0
    protected double distanceTraveled;
    protected double totalPathDistance;
    
    protected Image image;
    protected String imageFile;
    
    /**
     * Constructor for the Enemy class.
     *
     * @param x initial x position
     * @param y initial y position
     * @param width width of the enemy
     * @param height height of the enemy
     * @param health the enemy's health
     * @param speed the movement speed
     * @param goldReward the gold rewarded when defeated
     */
    public Enemy(double x, double y, double width, double height, int health, double speed, int goldReward) {
        super(x, y, width, height);
        this.maxHealth = health;
        this.currentHealth = health;
        this.speed = speed;
        this.pathProgress = 0.0;
        this.goldReward = goldReward;
        this.distanceTraveled = 0;
        this.totalPathDistance = 0;
    }
    
    /**
     * Constructor for the Enemy class with size 64x64.
     *
     * @param x initial x position
     * @param y initial y position
     * @param health the enemy's health
     * @param speed the movement speed
     * @param goldReward the gold rewarded when defeated
     */
    public Enemy(double x, double y, int health, double speed, int goldReward) {
        this(x, y, 32, 32, health, speed, goldReward);
    }
    
    /**
     * Constructor for an enemy.
     * 
     * @param x initial x position
     * @param y initial y position
     * @param width width
     * @param height height
     * @param health health points
     * @param speed movement speed in pixels per second
     * @param goldReward gold reward when defeated
     * @param type the type of enemy
     */
    public Enemy(double x, double y, double width, double height, 
                int health, double speed, int goldReward, EnemyType type) {
        super(x, y, width, height);
        this.maxHealth = health;
        this.currentHealth = health;
        this.speed = speed;
        this.goldReward = goldReward;
        this.type = type;
        this.distanceTraveled = 0;
        this.totalPathDistance = 0;
        this.pathProgress = 0.0;
        
        // Set image from cache
        this.image = ENEMY_IMAGES.get(type);
    }
    
    /**
     * Load all enemy images into the static cache
     */
    private static void loadEnemyImages() {
        String basePath = System.getProperty("user.dir") + File.separator + "Asset_pack" + File.separator + "Enemies" + File.separator;
        
        try {
            // Goblin
            File goblinFile = new File(basePath + "Goblin_Red.png");
            if (goblinFile.exists()) {
                ENEMY_IMAGES.put(EnemyType.GOBLIN, new Image(goblinFile.toURI().toString()));
                System.out.println("Loaded Goblin image");
            }
            
            // Knight
            File knightFile = new File(basePath + "Warrior_Blue.png");
            if (knightFile.exists()) {
                ENEMY_IMAGES.put(EnemyType.KNIGHT, new Image(knightFile.toURI().toString()));
                System.out.println("Loaded Knight image");
            }
        } catch (Exception e) {
            System.err.println("Error loading enemy images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set the path for this enemy to follow.
     * 
     * @param path the path to follow
     */
    public void setPath(Path path) {
        this.path = path;
        this.totalPathDistance = path.calculateTotalLength();
        
        // Set the initial position to the start of the path
        double[] startPos = path.getPositionAt(0);
        this.x = startPos[0] - width / 2;
        this.y = startPos[1] - height / 2;
    }
    
    /**
     * Updates the enemy state.
     *
     * @param deltaTime time elapsed since last update in seconds
     * @return true if the enemy reached the end of the path
     */
    public boolean update(double deltaTime) {
        if (path == null) {
            return false;
        }
        
        // Calculate the distance to move based on speed and time
        double distanceToMove = speed * deltaTime;
        
        // Convert to path progress (0.0 to 1.0)
        double progressIncrement = distanceToMove / totalPathDistance;
        pathProgress += progressIncrement;
        
        // Cap progress at 1.0 (end of path)
        if (pathProgress > 1.0) {
            pathProgress = 1.0;
            return true; // Reached the end
        }
        
        // Calculate new position based on path progress
        double[] newPosition = path.getPositionAt(pathProgress);
        
        // Update position (centered on the path)
        this.x = newPosition[0] - width / 2;
        this.y = newPosition[1] - height / 2;
        
        return false;
    }
    
    /**
     * Render the enemy and its health bar.
     *
     * @param gc the graphics context to render on
     */
    @Override
    public void render(GraphicsContext gc) {
        // Load image if not already loaded
        if (image == null && imageFile != null) {
            loadImage();
        }
        
        // Draw the enemy image if available
        if (image != null) {
            gc.drawImage(image, x, y, width, height);
        } else {
            // Fallback to a simple shape if no image
            gc.setFill(Color.RED);
            gc.fillOval(x, y, width, height);
        }
        
        // Draw health bar
        renderHealthBar(gc);
    }
    
    /**
     * Load the enemy image from file.
     */
    protected void loadImage() {
        try {
            File file = new File(imageFile);
            if (file.exists()) {
                image = new Image(file.toURI().toString());
                System.out.println("Loaded image for " + getClass().getSimpleName() + ": " + imageFile);
            } else {
                System.err.println("Image file not found: " + imageFile);
            }
        } catch (Exception e) {
            System.err.println("Error loading image " + imageFile + ": " + e.getMessage());
        }
    }
    
    /**
     * Render the health bar above the enemy.
     *
     * @param gc the graphics context to render on
     */
    private void renderHealthBar(GraphicsContext gc) {
        double barWidth = width * 0.8;
        double barHeight = 5;
        double barY = y - 10;
        double barX = x + (width - barWidth) / 2;
        
        // Draw background (full health bar)
        gc.setFill(Color.RED);
        gc.fillRect(barX, barY, barWidth, barHeight);
        
        // Draw current health
        double healthWidth = barWidth * ((double) currentHealth / maxHealth);
        gc.setFill(Color.GREEN);
        gc.fillRect(barX, barY, healthWidth, barHeight);
        
        // Draw border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barWidth, barHeight);
    }
    
    /**
     * Apply damage to the enemy.
     *
     * @param amount amount of damage to apply
     * @return true if the enemy was defeated
     */
    public boolean applyDamage(int amount) {
        currentHealth -= amount;
        return currentHealth <= 0;
    }
    
    /**
     * Apply damage to the enemy with a specific damage type.
     *
     * @param amount amount of damage to apply
     * @param damageType type of damage
     * @return true if the enemy was defeated
     */
    public abstract boolean applyDamage(int amount, DamageType damageType);
    
    /**
     * Calculate the distance traveled along the path.
     *
     * @return distance in pixels
     */
    public double getDistanceTraveled() {
        if (path == null) {
            return 0.0;
        }
        return path.calculateTotalLength() * pathProgress;
    }
    
    /**
     * Calculate the distance to another entity.
     *
     * @param other the other entity
     * @return the distance in pixels
     */
    public double distanceTo(Entity other) {
        double centerX = this.x + this.width / 2;
        double centerY = this.y + this.height / 2;
        double otherCenterX = other.getX() + other.getWidth() / 2;
        double otherCenterY = other.getY() + other.getHeight() / 2;
        
        double deltaX = centerX - otherCenterX;
        double deltaY = centerY - otherCenterY;
        
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    
    /**
     * Get the enemy's path progress as a percentage (0.0 to 1.0).
     * 
     * @return path progress percentage
     */
    public double getPathProgressPercentage() {
        if (totalPathDistance <= 0) {
            return 0;
        }
        return Math.min(pathProgress, 1.0);
    }
    
    // Getters and setters
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
    
    public int getCurrentHealth() {
        return currentHealth;
    }
    
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public int getGoldReward() {
        return goldReward;
    }
    
    public void setGoldReward(int goldReward) {
        this.goldReward = goldReward;
    }
    
    public EnemyType getType() {
        return type;
    }
    
    public double getPathProgress() {
        return pathProgress;
    }
    
    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }
    
    /**
     * Enum of enemy types.
     */
    public enum EnemyType {
        GOBLIN,
        KNIGHT
    }
} 