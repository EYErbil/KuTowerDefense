package com.ku.towerdefense.model;

import java.io.Serializable;

/**
 * Represents a point on a path.
 */
public class PathPoint implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double x;
    private double y;
    
    /**
     * Constructor for a new path point.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public PathPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Get the x coordinate.
     * 
     * @return the x coordinate
     */
    public double getX() {
        return x;
    }
    
    /**
     * Get the y coordinate.
     * 
     * @return the y coordinate
     */
    public double getY() {
        return y;
    }
    
    /**
     * Set the x coordinate.
     * 
     * @param x the new x coordinate
     */
    public void setX(double x) {
        this.x = x;
    }
    
    /**
     * Set the y coordinate.
     * 
     * @param y the new y coordinate
     */
    public void setY(double y) {
        this.y = y;
    }
    
    /**
     * Calculate the distance to another point.
     * 
     * @param other the other point
     * @return the distance
     */
    public double distanceTo(PathPoint other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Get a string representation of the point.
     * 
     * @return the string representation
     */
    @Override
    public String toString() {
        return "PathPoint(" + x + ", " + y + ")";
    }
} 