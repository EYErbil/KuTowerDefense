package com.ku.towerdefense.model;

import javafx.geometry.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable poly‑line path the enemies follow.
 */
public class GamePath implements Serializable {
    private static final long serialVersionUID = 1L;

    /*
     * JavaFX Point2D is **not** Serializable → mark it transient and store a
     * primitive mirror that Java can write to an ObjectStream.
     */
    private transient List<Point2D> points = new ArrayList<>();
    private List<double[]> rawPoints = new ArrayList<>();   // [x,y] pairs

    private transient double totalLength;   // cached after load

    public GamePath() {}
    public GamePath(List<int[]> pathPoints) {
        pathPoints.forEach(p -> addPoint(p[0], p[1]));
    }

    public void addPoint(double x, double y) {
        points.add(new Point2D(x, y));
        rawPoints.add(new double[]{x, y});
        calculateTotalLength();
    }

    public List<Point2D> getPoints() { return points; }

    // ---------------------------------------------------------------------
    // Geometry helpers
    // ---------------------------------------------------------------------
    public double calculateTotalLength() {
        totalLength = 0;
        for (int i = 0; i < points.size() - 1; i++)
            totalLength += points.get(i).distance(points.get(i + 1));
        return totalLength;
    }

    /** Return x,y along the poly‑line where {@code progress}=0.0…1.0. */
    public double[] getPositionAt(double progress) {
        if (points.size() < 2) return new double[]{0,0};
        progress = Math.max(0, Math.min(1, progress));
        double target = progress * totalLength;
        double walked = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D a = points.get(i), b = points.get(i + 1);
            double segLen = a.distance(b);
            if (walked + segLen >= target) {
                double t = (target - walked) / segLen;
                return new double[]{ a.getX() + t*(b.getX()-a.getX()),
                        a.getY() + t*(b.getY()-a.getY()) };
            }
            walked += segLen;
        }
        Point2D end = points.get(points.size()-1);
        return new double[]{end.getX(), end.getY()};
    }

    // ---------------------------------------------------------------------
    // Custom serialisation – Point2D ➟ double[2]
    // ---------------------------------------------------------------------
    private void writeObject(ObjectOutputStream out) throws IOException {
        rawPoints = new ArrayList<>();
        for (Point2D p : points) rawPoints.add(new double[]{p.getX(), p.getY()});
        out.defaultWriteObject();
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        points = new ArrayList<>();
        for (double[] arr : rawPoints) points.add(new Point2D(arr[0], arr[1]));
        calculateTotalLength();
    }
}