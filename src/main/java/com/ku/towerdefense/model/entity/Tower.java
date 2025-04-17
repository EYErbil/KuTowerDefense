package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.util.GameSettings;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base class for all tower types in the game.
 */
public abstract class Tower extends Entity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected int damage;
    protected int range;
    protected int cost;
    protected long lastFireTime;
    protected long fireRate; // milliseconds between shots
    protected boolean selected;
    protected Image image;
    protected String imageFile;
    protected DamageType damageType;
    
    /**
     * Constructor for towers with specified properties.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param width width of the tower
     * @param height height of the tower
     * @param damage damage dealt by each shot
     * @param range range in pixels
     * @param fireRate rate of fire in milliseconds
     * @param cost gold cost to build
     * @param damageType the type of damage dealt
     */
    public Tower(double x, double y, double width, double height, int damage, int range, 
                long fireRate, int cost, DamageType damageType) {
        super(x, y, width, height);
        this.damage = damage;
        this.range = range;
        this.fireRate = fireRate;
        this.cost = cost;
        this.lastFireTime = 0;
        this.selected = false;
        this.damageType = damageType;
    }
    
    /**
     * Update the tower's state and generate projectiles if needed.
     *
     * @param deltaTime time elapsed since the last update (in seconds)
     * @param enemies list of enemies in the game
     * @return a projectile if the tower fires, or null if not
     */
    public Projectile update(double deltaTime, List<Enemy> enemies) {
        if (enemies.isEmpty()) {
            return null;
        }
        
        // Check if enough time has passed since the last shot
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireTime < fireRate) {
            return null;
        }
        
        // Find the enemy that has progressed furthest along the path within range
        Enemy target = findBestTarget(enemies);
        if (target == null) {
            return null;
        }
        
        // Fire at the target
        lastFireTime = currentTime;
        return createProjectile(target);
    }
    
    /**
     * Find the best target based on path progression.
     * The best target is the enemy that has progressed furthest along the path
     * and is within the tower's range.
     *
     * @param enemies list of all enemies
     * @return the best target enemy, or null if no enemies are in range
     */
    protected Enemy findBestTarget(List<Enemy> enemies) {
        // Get center coordinates for range calculation
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        // Filter enemies that are in range
        List<Enemy> enemiesInRange = enemies.stream()
            .filter(enemy -> isInRange(enemy, centerX, centerY))
            .collect(Collectors.toList());
        
        if (enemiesInRange.isEmpty()) {
            return null;
        }
        
        // Find the enemy with the highest path progress
        return enemiesInRange.stream()
            .max((e1, e2) -> Double.compare(e1.getPathProgress(), e2.getPathProgress()))
            .orElse(null);
    }
    
    /**
     * Check if an enemy is in range of this tower.
     *
     * @param enemy the enemy to check
     * @param centerX the x coordinate of the tower's center
     * @param centerY the y coordinate of the tower's center
     * @return true if the enemy is in range, false otherwise
     */
    protected boolean isInRange(Enemy enemy, double centerX, double centerY) {
        double enemyCenterX = enemy.getX() + enemy.getWidth() / 2;
        double enemyCenterY = enemy.getY() + enemy.getHeight() / 2;
        
        double distance = Math.sqrt(
            Math.pow(centerX - enemyCenterX, 2) + 
            Math.pow(centerY - enemyCenterY, 2)
        );
        
        return distance <= range;
    }
    
    /**
     * Create a projectile targeting the specified enemy.
     *
     * @param target the target enemy
     * @return a new projectile
     */
    protected abstract Projectile createProjectile(Enemy target);
    
    /**
     * Render the tower.
     *
     * @param gc the graphics context to render on
     */
    @Override
    public void render(GraphicsContext gc) {
        // Load image if not already loaded
        if (image == null && imageFile != null) {
            loadImage();
        }
        
        // Draw the tower image if available
        if (image != null) {
            gc.drawImage(image, x, y, width, height);
        } else {
            // Fallback to a simple shape if no image
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(x, y, width, height);
        }
        
        // Draw range circle if selected
        if (selected) {
            renderRangeCircle(gc);
        }
    }
    
    /**
     * Load the tower image from file.
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
     * Render the range circle around the tower.
     *
     * @param gc the graphics context to render on
     */
    protected void renderRangeCircle(GraphicsContext gc) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        // Draw a semi-transparent circle showing the tower's range
        gc.setGlobalAlpha(0.3);
        gc.setFill(Color.WHITE);
        gc.fillOval(centerX - range, centerY - range, range * 2, range * 2);
        
        // Draw a border for the range circle
        gc.setGlobalAlpha(0.7);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeOval(centerX - range, centerY - range, range * 2, range * 2);
        
        // Reset alpha
        gc.setGlobalAlpha(1.0);
    }
    
    /**
     * Calculate the refund amount when selling this tower.
     *
     * @return gold amount refunded
     */
    public int getSellRefund() {
        // Refund 75% of the cost
        return (int)(cost * 0.75);
    }
    
    // Getters and setters
    
    public int getDamage() {
        return damage;
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    public int getRange() {
        return range;
    }
    
    public void setRange(int range) {
        this.range = range;
    }
    
    public int getCost() {
        return cost;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public DamageType getDamageType() {
        return damageType;
    }
    
    public long getFireRate() {
        return fireRate;
    }
    
    public void setFireRate(long fireRate) {
        this.fireRate = fireRate;
    }
    
    public double getCenterX() {
        return x + width / 2;
    }
    
    public double getCenterY() {
        return y + height / 2;
    }
    
    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }
} 