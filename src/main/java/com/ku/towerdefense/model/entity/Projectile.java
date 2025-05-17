package com.ku.towerdefense.model.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.Serializable;

/**
 * Represents a projectile fired by a tower.
 */
public class Projectile extends Entity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum ImpactEffect { NONE, EXPLOSION, FIRE }
    private ImpactEffect impactEffect = ImpactEffect.NONE;
    
    private Enemy target;
    private int damage;
    private DamageType damageType;
    private double speed;
    private boolean active;
    private boolean hasHit;
    private Tower sourceTower;
    
    // AOE properties
    private boolean hasAoeEffect;
    private int aoeRange;
    
    // Visuals
    private Color color;
    private Image image;
    private String imageFile;
    
    /**
     * Create a new projectile.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param width width of the projectile
     * @param height height of the projectile
     * @param target target enemy
     * @param damage damage amount
     * @param damageType type of damage
     * @param speed speed in pixels per second
     */
    public Projectile(double x, double y, double width, double height, 
                      Enemy target, int damage, DamageType damageType, double speed,
                      Tower sourceTower) {
        super(x, y, width, height);
        this.target = target;
        this.damage = damage;
        this.damageType = damageType;
        this.speed = speed;
        this.active = true;
        this.hasHit = false;
        this.hasAoeEffect = false;
        this.aoeRange = 0;
        this.sourceTower = sourceTower;
        
        // Default appearance based on damage type
        switch (damageType) {
            case ARROW:
                this.color = Color.DARKGREEN;
                break;
            case MAGIC:
                this.color = Color.PURPLE;
                break;
            case EXPLOSIVE:
                this.color = Color.RED;
                break;
            default:
                this.color = Color.GRAY;
        }
    }
    
    /**
     * Update the projectile position and check for collision with target.
     *
     * @param deltaTime time elapsed since last update in seconds
     * @return true if the projectile hit its target, false otherwise
     */
    public boolean update(double deltaTime) {
        if (!active || hasHit) {
            return hasHit;
        }
        
        // Check if target is still valid
        if (target == null || target.getCurrentHealth() <= 0) {
            active = false;
            return false;
        }
        
        // Calculate direction vector to target
        double targetCenterX = target.getX() + target.getWidth() / 2;
        double targetCenterY = target.getY() + target.getHeight() / 2;
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        double deltaX = targetCenterX - centerX;
        double deltaY = targetCenterY - centerY;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        
        // If we're close enough to hit the target
        if (distance < 10) {
            hasHit = true;
            active = false;
            return true;
        }
        
        // Otherwise move toward the target
        double dirX = deltaX / distance;
        double dirY = deltaY / distance;
        
        double moveDistance = speed * deltaTime;
        x += dirX * moveDistance;
        y += dirY * moveDistance;
        
        // Rotate the projectile in the direction of movement
        if (damageType == DamageType.ARROW) {
            rotation = Math.atan2(dirY, dirX) * 180 / Math.PI;
        }
        
        return false;
    }
    
    /**
     * Render the projectile.
     *
     * @param gc the graphics context to render on
     */
    @Override
    public void render(GraphicsContext gc) {
        // Load image if specified and not already loaded
        if (image == null && imageFile != null) {
            loadImage();
        }
        
        // Get the center point for drawing
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        gc.save();
        
        if (image != null) {
            // Draw the image, possibly rotated
            gc.translate(centerX, centerY);
            gc.rotate(rotation);
            gc.drawImage(image, -width / 2, -height / 2, width, height);
        } else {
            // Draw a simple shape based on the damage type
            switch (damageType) {
                case ARROW:
                    // Draw an arrow shape
                    gc.translate(centerX, centerY);
                    gc.rotate(rotation);
                    gc.setFill(color);
                    
                    double[] xPoints = {-width / 2, width / 2, width / 4, width / 4, -width / 4, -width / 4};
                    double[] yPoints = {0, 0, height / 4, height / 2, height / 2, height / 4};
                    gc.fillPolygon(xPoints, yPoints, 6);
                    
                    break;
                    
                case MAGIC:
                    // Draw a glowing orb
                    gc.setGlobalAlpha(0.7);
                    gc.setFill(color);
                    gc.fillOval(x, y, width, height);
                    
                    // Draw a brighter core
                    gc.setGlobalAlpha(1.0);
                    gc.setFill(color.brighter());
                    gc.fillOval(x + width * 0.25, y + height * 0.25, width * 0.5, height * 0.5);
                    
                    break;
                    
                case EXPLOSIVE:
                    // Draw a bomb-like shape
                    gc.setFill(color);
                    gc.fillOval(x, y, width, height);
                    
                    // Draw a fuse
                    gc.setStroke(Color.YELLOW);
                    gc.setLineWidth(2);
                    gc.strokeLine(x + width * 0.7, y - height * 0.2, x + width * 0.5, y + height * 0.2);
                    
                    break;
                    
                default:
                    // Simple circle for other types
                    gc.setFill(color);
                    gc.fillOval(x, y, width, height);
            }
        }
        
        gc.restore();
    }
    
    /**
     * Load the projectile image from file if available.
     */
    private void loadImage() {
        try {
            // Try loading from classpath first
            if (imageFile != null && !imageFile.isEmpty()) {
                // Handle both situations - when imageFile is an absolute path or just a filename
                String resourcePath;
                if (imageFile.contains("Asset_pack") || imageFile.contains("assets")) {
                    // Extract just the file name from the path
                    int lastSlash = Math.max(imageFile.lastIndexOf('\\'), imageFile.lastIndexOf('/'));
                    if (lastSlash >= 0 && lastSlash < imageFile.length() - 1) {
                        String fileName = imageFile.substring(lastSlash + 1);
                        // Try to load using the extracted file name
                        resourcePath = "/Asset_pack/Projectiles/" + fileName;
                    } else {
                        resourcePath = "/Asset_pack/Projectiles/" + imageFile;
                    }
                } else {
                    resourcePath = "/Asset_pack/Projectiles/" + imageFile;
                }
                
                // Try to load from the classpath
                try {
                    image = new Image(getClass().getResourceAsStream(resourcePath));
                    if (image != null && !image.isError()) {
                        System.out.println("Loaded projectile image from classpath: " + resourcePath);
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Could not load projectile image from classpath: " + resourcePath);
                }
                
                // Fallback to file system only if absolutely necessary
                try {
                    File file = new File(imageFile);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString());
                        System.out.println("Loaded projectile image from file: " + imageFile);
                    } else {
                        System.err.println("Projectile image file not found: " + imageFile);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading from file system: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading projectile image " + imageFile + ": " + e.getMessage());
        }
    }
    
    /**
     * Reinitialize after deserialization to reload images
     */
    public void reinitializeAfterLoad() {
        if (image == null && imageFile != null) {
            loadImage();
        }
        
        // If image still couldn't be loaded, ensure color is set for fallback rendering
        if (image == null && color == null) {
            // Set a default color based on damage type
            switch (damageType) {
                case ARROW:
                    color = Color.BROWN;
                    break;
                case MAGIC:
                    color = Color.PURPLE;
                    break;
                case EXPLOSIVE:
                    color = Color.ORANGE;
                    break;
                default:
                    color = Color.GRAY;
            }
        }
    }
    
    // Getters and setters
    
    public Enemy getTarget() {
        return target;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public DamageType getDamageType() {
        return damageType;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isHasHit() {
        return hasHit;
    }
    
    public boolean hasAoeEffect() {
        return hasAoeEffect;
    }
    
    public void setHasAoeEffect(boolean hasAoeEffect) {
        this.hasAoeEffect = hasAoeEffect;
    }
    
    public int getAoeRange() {
        return aoeRange;
    }
    
    public void setAoeRange(int aoeRange) {
        this.aoeRange = aoeRange;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
        this.image = null; // Force reload
    }
    
    public void setImpactEffect(ImpactEffect e) { this.impactEffect = e; }
    public ImpactEffect getImpactEffect() { return impactEffect; }

    public Tower getSourceTower() {
        return sourceTower;
    }
} 