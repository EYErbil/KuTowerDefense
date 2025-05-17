package com.ku.towerdefense.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a path for enemies to follow.
 */
public class Path implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Note: JavaFX classes are not serializable, so they will be lost during serialization
    private transient List<Point2D> points = new ArrayList<>();
    private double totalLength;
    
    // Store raw points for serialization
    private List<double[]> rawPoints = new ArrayList<>();
    
    /**
     * Constructor for a new path.
     * 
     * @param pathPoints list of path points as [x, y] coordinate arrays
     */
    public Path(List<int[]> pathPoints) {
        this.points = new ArrayList<>();
        this.rawPoints = new ArrayList<>();
        
        // Convert int arrays to Point2D objects and store raw points
        for (int[] point : pathPoints) {
            points.add(new Point2D(point[0], point[1]));
            rawPoints.add(new double[]{point[0], point[1]});
        }
        
        // Calculate total path length
        calculateTotalLength();
    }
    
    /**
     * Calculate the total length of the path.
     * 
     * @return the total length in pixels
     */
    public double calculateTotalLength() {
        totalLength = 0;
        
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D start = points.get(i);
            Point2D end = points.get(i + 1);
            
            // Use distance between points
            totalLength += start.distance(end);
        }
        
        return totalLength;
    }
    
    /**
     * Get a position along the path based on progress (0.0 to 1.0).
     * 
     * @param progress the progress along the path (0.0 to 1.0)
     * @return the [x, y] position at that progress point
     */
    public double[] getPositionAt(double progress) {
        if (points.size() < 2) {
            return new double[]{0, 0}; // Default if no valid path
        }
        
        if (progress <= 0) {
            // Return the start point
            Point2D start = points.get(0);
            return new double[]{start.getX(), start.getY()};
        }
        
        if (progress >= 1) {
            // Return the end point
            Point2D end = points.get(points.size() - 1);
            return new double[]{end.getX(), end.getY()};
        }
        
        // Find which segment we're on based on progress
        double targetDistance = progress * totalLength;
        double currentDistance = 0;
        
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D start = points.get(i);
            Point2D end = points.get(i + 1);
            
            double segmentLength = start.distance(end);
            
            if (currentDistance + segmentLength >= targetDistance) {
                // We found our segment, now interpolate
                double segmentProgress = (targetDistance - currentDistance) / segmentLength;
                
                // Linear interpolation
                double x = start.getX() + (end.getX() - start.getX()) * segmentProgress;
                double y = start.getY() + (end.getY() - start.getY()) * segmentProgress;
                
                return new double[]{x, y};
            }
            
            currentDistance += segmentLength;
        }
        
        // Fallback to end point
        Point2D end = points.get(points.size() - 1);
        return new double[]{end.getX(), end.getY()};
    }

    /**
     * Render the path for debugging.
     *
     * @param gc graphics context to render on
     */
    public void render(GraphicsContext gc) {
        if (points.isEmpty()) return;
        
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2.0);
        
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D start = points.get(i);
            Point2D end = points.get(i + 1);
            
            // Points are now in pixel coordinates, so no need to multiply by tile size
            gc.strokeLine(start.getX(), start.getY(), 
                         end.getX(), end.getY());
        }
    }
    
    /**
     * Get the list of path points.
     * 
     * @return the list of path points
     */
    public List<Point2D> getPoints() {
        // If points is empty but we have raw points, reconstruct Point2D objects
        if (points.isEmpty() && !rawPoints.isEmpty()) {
            for (double[] rawPoint : rawPoints) {
                points.add(new Point2D(rawPoint[0], rawPoint[1]));
            }
            calculateTotalLength();
        }
        return points;
    }
    
    /**
     * Handle deserialization by recreating Point2D objects.
     */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Recreate points list from rawPoints
        this.points = new ArrayList<>();
        if (rawPoints != null) {
            for (double[] rawPoint : rawPoints) {
                points.add(new Point2D(rawPoint[0], rawPoint[1]));
            }
        } else {
            rawPoints = new ArrayList<>();
        }
    }
} 