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

        // Define the size to draw the tower visually (e.g., fit in a 32x32 tile)
        double drawSize = 32.0;
        // Adjust drawing coordinates to center the visual representation if needed
        // The entity's x,y usually refer to the top-left. Center 32x32 within logical bounds?
        // Or just draw at x,y with size 32x32?
        // Let's assume x,y is top-left and draw a 32x32 image there for now.
        double drawX = x;
        double drawY = y;

        // Draw the tower image if available, scaled to drawSize
        if (image != null) {
            // Use the drawImage overload that specifies destination width/height
            gc.drawImage(image, drawX, drawY, drawSize, drawSize);
        } else {
            // Fallback to a simple shape if no image, using drawSize
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(drawX, drawY, drawSize, drawSize); // Draw fallback in the same size
        }

        // Draw range circle if selected (using original center logic)
        if (selected) {
            renderRangeCircle(gc);
        }
    }
    
    /**
     * Load the tower image from file.
     */
    protected void loadImage() {
        try {
            // First try loading from file path
            // File file = new File(imageFile);
            // if (file.exists()) {
            //    image = new Image(file.toURI().toString());
            //    System.out.println("Loaded image for " + getClass().getSimpleName() + ": " + imageFile);
            // } else {
            //    // If not found, try as a classpath resource
            //    String resourcePath = imageFile;
            //    if (!resourcePath.startsWith("/")) {
            //        resourcePath = "/" + resourcePath;
            //    }
            //    
            //    // Try to load as classpath resource
            //    if (loadFromClasspath(resourcePath)) {
            //        System.out.println("Loaded image for " + getClass().getSimpleName() + " from classpath: " + resourcePath);
            //    } else {
            //        System.err.println("Image not found in file path or classpath: " + imageFile);
            //    }
            // }

            // --- Simplified Loading: Assume imageFile is a classpath resource --- 
            String resourcePath = imageFile;
            // Ensure it starts with / for absolute classpath lookup
            if (resourcePath != null && !resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }

            if (resourcePath != null) {
                 if (loadFromClasspath(resourcePath)) {
                     System.out.println("Loaded image for " + getClass().getSimpleName() + " from classpath: " + resourcePath);
                 } else {
                     System.err.println("Failed to load image from classpath: " + resourcePath);
                     image = null; // Ensure image is null if loading failed
                 }
            } else {
                 System.err.println("Image file path is null for " + getClass().getSimpleName());
                 image = null;
            }
             // --- End Simplified Loading ---

        } catch (Exception e) {
            System.err.println("Error loading image " + imageFile + ": " + e.getMessage());
            image = null; // Ensure image is null on error
            // e.printStackTrace(); // Uncomment for detailed stack trace if needed
        }
    }
    
    /**
     * Load an image from classpath
     * 
     * @param resourcePath the classpath resource path
     * @return true if loaded successfully, false otherwise
     */
    private boolean loadFromClasspath(String resourcePath) {
        try {
            java.io.InputStream in = getClass().getResourceAsStream(resourcePath);
            if (in != null) {
                image = new Image(in);
                in.close();
                return !image.isError();
            }
        } catch (Exception e) {
            System.err.println("Error loading image from classpath " + resourcePath + ": " + e.getMessage());
        }
        return false;
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

    public Image getImage() {
        // Ensure the image is loaded if needed before returning
        if (this.image == null && this.imageFile != null) {
            loadImage();
        }
        return this.image;
    }
} 