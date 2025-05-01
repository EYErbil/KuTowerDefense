package com.ku.towerdefense.model;

import javafx.geometry.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a path that enemies follow in the game.
 */
public class GamePath implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Point2D> points;
    private double totalLength;

    public GamePath() {
        this.points = new ArrayList<>();
        this.totalLength = 0;
    }

    /**
     * Constructor that takes a list of path points as [x, y] coordinate arrays.
     *
     * @param pathPoints list of path points as [x, y] coordinate arrays
     */
    public GamePath(List<int[]> pathPoints) {
        this.points = new ArrayList<>();
        for (int[] point : pathPoints) {
            this.points.add(new Point2D(point[0], point[1]));
        }
        calculateTotalLength();
    }

    /**
     * Add a point to the path.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addPoint(double x, double y) {
        points.add(new Point2D(x, y));
        calculateTotalLength();
    }

    /**
     * Get all points in the path.
     *
     * @return list of points
     */
    public List<Point2D> getPoints() {
        return points;
    }

    /**
     * Calculate the total length of the path.
     *
     * @return the total length in pixels
     */
    public double calculateTotalLength() {
        totalLength = 0;

        if (points.size() < 2) {
            System.err.println("Path has less than 2 points!");
            return 0;
        }

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D start = points.get(i);
            Point2D end = points.get(i + 1);

            double segmentLength = start.distance(end);
            totalLength += segmentLength;

            System.out.println("Path segment " + i + ": " + segmentLength + " pixels");
        }

        System.out.println("Total path length: " + totalLength + " pixels");
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
            System.err.println("Path has less than 2 points!");
            return null;
        }

        if (progress <= 0) {
            Point2D start = points.get(0);
            return new double[]{start.getX(), start.getY()};
        }

        if (progress >= 1) {
            Point2D end = points.get(points.size() - 1);
            return new double[]{end.getX(), end.getY()};
        }

        double targetDistance = progress * totalLength;
        double currentDistance = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D start = points.get(i);
            Point2D end = points.get(i + 1);

            double segmentLength = start.distance(end);

            if (currentDistance + segmentLength >= targetDistance) {
                double segmentProgress = (targetDistance - currentDistance) / segmentLength;
                double x = start.getX() + (end.getX() - start.getX()) * segmentProgress;
                double y = start.getY() + (end.getY() - start.getY()) * segmentProgress;
                return new double[]{x, y};
            }

            currentDistance += segmentLength;
        }

        Point2D end = points.get(points.size() - 1);
        return new double[]{end.getX(), end.getY()};
    }
} 