package com.ku.towerdefense.model.entity;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

/**
 * Base class for all game entities.
 */
public abstract class Entity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Position and dimension
    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected double rotation;
    
    // Transient fields (not serialized)
    protected transient Image image;
    
    /**
     * Constructor for Entity.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param width width of entity
     * @param height height of entity
     */
    public Entity(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = 0;
    }
    
    /**
     * Initialize transient fields after deserialization.
     */
    public void initTransientFields() {
        // Implemented by subclasses to load images and other transient resources
    }
    
    /**
     * Render the entity on the canvas.
     * 
     * @param gc the graphics context to draw on
     */
    public abstract void render(GraphicsContext gc);
    
    /**
     * Calculate distance to another entity.
     * 
     * @param other the other entity
     * @return the distance between entities (center to center)
     */
    public double distanceTo(Entity other) {
        double dx = other.getCenterX() - this.getCenterX();
        double dy = other.getCenterY() - this.getCenterY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Check if this entity collides with another entity.
     * 
     * @param other the other entity
     * @return true if entities collide, false otherwise
     */
    public boolean collidesWith(Entity other) {
        return x < other.x + other.width &&
               x + width > other.x &&
               y < other.y + other.height &&
               y + height > other.y;
    }
    
    /**
     * Get the center x coordinate.
     * 
     * @return center x coordinate
     */
    public double getCenterX() {
        return x + width / 2;
    }
    
    /**
     * Get the center y coordinate.
     * 
     * @return center y coordinate
     */
    public double getCenterY() {
        return y + height / 2;
    }
    
    /**
     * Get the entity image.
     * 
     * @return the image for this entity
     */
    public Image getImage() {
        return image;
    }
    
    /**
     * Set the entity image.
     * 
     * @param image the image to set
     */
    public void setImage(Image image) {
        this.image = image;
    }
    
    /**
     * Get the entity's position as a Point2D.
     * 
     * @return the position
     */
    public Point2D getPosition() {
        return new Point2D(x, y);
    }
    
    /**
     * Set the entity's position.
     * 
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Check if this entity contains a point.
     * 
     * @param pointX the x-coordinate of the point
     * @param pointY the y-coordinate of the point
     * @return true if the entity contains the point, false otherwise
     */
    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX <= x + width && 
               pointY >= y && pointY <= y + height;
    }
    
    // Getters and setters
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public double getRotation() {
        return rotation;
    }
    
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }
} 