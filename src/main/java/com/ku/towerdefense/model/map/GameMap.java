package com.ku.towerdefense.model.map;

import com.ku.towerdefense.model.Path;
import com.ku.towerdefense.model.PathPoint;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the game map with tiles and paths.
 */
public class GameMap implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private int width;
    private int height;
    private Tile[][] tiles;
    private Path enemyPath;
    private List<int[]> pathPoints;
    
    // For caching the start and end points
    private transient Point2D startPoint;
    private transient Point2D endPoint;
    
    /**
     * Constructor for a new game map.
     * 
     * @param name map name
     * @param width width in tiles
     * @param height height in tiles
     */
    public GameMap(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
        initializeTiles();
    }
    
    /**
     * Initialize the map tiles with grass.
     */
    private void initializeTiles() {
        tiles = new Tile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile(x, y, TileType.GRASS);
            }
        }
    }
    
    /**
     * Get a tile at the specified coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return the tile at the coordinates, or null if out of bounds
     */
    public Tile getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return null;
    }
    
    /**
     * Set the type of a tile at the specified coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param type the new tile type
     */
    public void setTileType(int x, int y, TileType type) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            // Clear previous start/end points if setting a new one
            if (type == TileType.START_POINT) {
                clearTileTypeInMap(TileType.START_POINT);
                startPoint = null;
            } else if (type == TileType.END_POINT) {
                clearTileTypeInMap(TileType.END_POINT);
                endPoint = null;
            }
            
            tiles[x][y].setType(type);
            
            // If we're changing start or end points, we need to regenerate the path
            if (type == TileType.START_POINT || type == TileType.END_POINT) {
                generatePath();
            }
        }
    }
    
    /**
     * Clear all tiles of a specific type in the map
     * 
     * @param typeToRemove the tile type to remove
     */
    private void clearTileTypeInMap(TileType typeToRemove) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (tiles[x][y].getType() == typeToRemove) {
                    tiles[x][y].setType(TileType.GRASS);
                }
            }
        }
    }
    
    /**
     * Get the type of a tile at the specified coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return the tile type, or null if out of bounds
     */
    public TileType getTileType(int x, int y) {
        Tile tile = getTile(x, y);
        return tile != null ? tile.getType() : null;
    }
    
    /**
     * Generate a path from start to end points.
     */
    public void generatePath() {
        // Find start and end tiles
        Tile startTile = null;
        Tile endTile = null;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (tiles[x][y].getType() == TileType.START_POINT) {
                    startTile = tiles[x][y];
                } else if (tiles[x][y].getType() == TileType.END_POINT) {
                    endTile = tiles[x][y];
                }
            }
        }
        
        // Reset path if start or end is missing
        if (startTile == null || endTile == null) {
            enemyPath = null;
            return;
        }
        
        // Update cached start and end points
        startPoint = new Point2D(startTile.getX() * 32 + 16, startTile.getY() * 32 + 16);
        endPoint = new Point2D(endTile.getX() * 32 + 16, endTile.getY() * 32 + 16);
        
        // Initialize pathPoints list
        List<int[]> pathPoints = new ArrayList<>();
        
        // Add the start point (in pixels)
        final int ts = 32; // tileSize
        pathPoints.add(new int[]{startTile.getX() * ts + ts / 2, startTile.getY() * ts + ts / 2});
        
        // Generate horizontal path
        int currentX = startTile.getX();
        int currentY = startTile.getY();
        int endX = endTile.getX();
        int endY = endTile.getY();
        
        // First go horizontally
        while (currentX != endX) {
            currentX += (endX > currentX) ? 1 : -1;
            
            // Skip if tile is an obstacle
            if (getTile(currentX, currentY) != null && getTile(currentX, currentY).getType() == TileType.OBSTACLE) {
                continue;
            }
            
            // Add point to path (in pixels)
            pathPoints.add(new int[]{currentX * ts + ts / 2, currentY * ts + ts / 2});
            
            // Mark as path tile
            if (getTile(currentX, currentY) != null && 
                getTile(currentX, currentY).getType() != TileType.START_POINT && 
                getTile(currentX, currentY).getType() != TileType.END_POINT) {
                getTile(currentX, currentY).setType(TileType.PATH);
            }
        }
        
        // Then go vertically
        while (currentY != endY) {
            currentY += (endY > currentY) ? 1 : -1;
            
            // Skip if tile is an obstacle
            if (getTile(currentX, currentY) != null && getTile(currentX, currentY).getType() == TileType.OBSTACLE) {
                continue;
            }
            
            // Add point to path (in pixels)
            pathPoints.add(new int[]{currentX * ts + ts / 2, currentY * ts + ts / 2});
            
            // Mark as path tile
            if (getTile(currentX, currentY) != null && 
                getTile(currentX, currentY).getType() != TileType.START_POINT && 
                getTile(currentX, currentY).getType() != TileType.END_POINT) {
                getTile(currentX, currentY).setType(TileType.PATH);
            }
        }
        
        // Create path object
        this.enemyPath = new Path(pathPoints);
        System.out.println("Generated path with " + pathPoints.size() + " points");
    }
    
    /**
     * Get the start point for enemies
     * 
     * @return the start point coordinates
     */
    public Point2D getStartPoint() {
        if (startPoint == null) {
            // Find the start tile if not cached
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (tiles[x][y].getType() == TileType.START_POINT) {
                        startPoint = new Point2D(x * 32 + 16, y * 32 + 16);
                        break;
                    }
                }
                if (startPoint != null) break;
            }
            
            // Create a default start point if none exists
            if (startPoint == null) {
                startPoint = new Point2D(16, height * 16);
            }
        }
        
        return startPoint;
    }
    
    /**
     * Get the end point for enemies
     * 
     * @return the end point coordinates
     */
    public Point2D getEndPoint() {
        if (endPoint == null) {
            // Find the end tile if not cached
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (tiles[x][y].getType() == TileType.END_POINT) {
                        endPoint = new Point2D(x * 32 + 16, y * 32 + 16);
                        break;
                    }
                }
                if (endPoint != null) break;
            }
            
            // Create a default end point if none exists
            if (endPoint == null) {
                endPoint = new Point2D(width * 32 - 16, height * 16);
            }
        }
        
        return endPoint;
    }
    
    /**
     * Check if a tower can be placed at the specified coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return true if a tower can be placed, false otherwise
     */
    public boolean canPlaceTower(double x, double y) {
        // Convert pixel coordinates to tile coordinates
        int tileX = (int) (x / 32);
        int tileY = (int) (y / 32);
        
        Tile tile = getTile(tileX, tileY);
        return tile != null && tile.canPlaceTower();
    }
    
    /**
     * Render the map on the canvas.
     * 
     * @param gc the graphics context to draw on
     */
    public void render(GraphicsContext gc) {
        int tileSize = 32; // 32 pixels per tile
        
        // Draw background
        gc.setFill(javafx.scene.paint.Color.rgb(40, 40, 40));
        gc.fillRect(0, 0, width * tileSize, height * tileSize);
        
        // Draw all tiles using their render methods
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Tile tile = tiles[x][y];
                tile.render(gc, tileSize);
            }
        }
        
        // Draw path line to visualize enemy path
        if (enemyPath != null) {
            List<Point2D> pathPoints = enemyPath.getPoints();
            if (pathPoints.size() > 1) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(2);
                gc.setGlobalAlpha(0.4);
                
                for (int i = 0; i < pathPoints.size() - 1; i++) {
                    Point2D start = pathPoints.get(i);
                    Point2D end = pathPoints.get(i + 1);
                    
                    // Points are already in pixel coordinates
                    gc.strokeLine(
                        start.getX(), start.getY(),
                        end.getX(), end.getY()
                    );
                }
                
                gc.setGlobalAlpha(1.0);
            }
        }
        
        // Draw subtle grid lines
        gc.setStroke(javafx.scene.paint.Color.rgb(0, 0, 0, 0.2)); // Semi-transparent
        gc.setLineWidth(0.5);
        
        for (int x = 0; x <= width; x++) {
            gc.strokeLine(x * tileSize, 0, x * tileSize, height * tileSize);
        }
        
        for (int y = 0; y <= height; y++) {
            gc.strokeLine(0, y * tileSize, width * tileSize, y * tileSize);
        }
        
        // Draw a border around the entire map
        gc.setStroke(javafx.scene.paint.Color.rgb(80, 80, 80));
        gc.setLineWidth(2.0);
        gc.strokeRect(0, 0, width * tileSize, height * tileSize);
    }
    
    /**
     * Get the enemy path for this map.
     * 
     * @return the enemy path
     */
    public Path getEnemyPath() {
        return enemyPath;
    }
    
    /**
     * Set the enemy path for this map.
     * 
     * @param enemyPath the enemy path to set
     */
    public void setEnemyPath(Path enemyPath) {
        this.enemyPath = enemyPath;
    }
    
    /**
     * Get the map name.
     * 
     * @return the map name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the map name.
     * 
     * @param name the map name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the map width in tiles.
     * 
     * @return the map width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Get the map height in tiles.
     * 
     * @return the map height
     */
    public int getHeight() {
        return height;
    }
} 