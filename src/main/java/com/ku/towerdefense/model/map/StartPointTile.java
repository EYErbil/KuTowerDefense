package com.ku.towerdefense.model.map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a start point tile where enemies enter the map.
 */
public class StartPointTile extends PathTile {
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor for StartPointTile.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param tileSize the size of the tile in pixels
     */
    public StartPointTile(int x, int y, int tileSize) {
        super(x, y, tileSize);
        setType(TileType.START_POINT);
    }
    
    @Override
    public void render(GraphicsContext gc, int tileSize) {
        int x = getCol() * tileSize;
        int y = getRow() * tileSize;
        
        // Draw base path tile appearance
        gc.setFill(Color.SANDYBROWN);
        gc.fillRect(x, y, tileSize, tileSize);
        
        // Draw border
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, tileSize, tileSize);
        
        // Draw start point indicator (green circle)
        gc.setFill(Color.GREEN);
        double centerX = x + tileSize / 2;
        double centerY = y + tileSize / 2;
        double radius = tileSize / 4;
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw direction arrow
        gc.setFill(Color.BLACK);
        double arrowSize = tileSize / 5;
        
        // Determine arrow direction based on next tile
        if (getNext() != null) {
            int nextRow = getNext().getRow();
            int nextCol = getNext().getCol();
            
            if (nextRow < getRow()) { // Up
                drawArrow(gc, centerX, centerY, 0, arrowSize);
            } else if (nextRow > getRow()) { // Down
                drawArrow(gc, centerX, centerY, Math.PI, arrowSize);
            } else if (nextCol < getCol()) { // Left
                drawArrow(gc, centerX, centerY, -Math.PI/2, arrowSize);
            } else if (nextCol > getCol()) { // Right
                drawArrow(gc, centerX, centerY, Math.PI/2, arrowSize);
            }
        }
        
        // Draw selection highlight if selected
        if (isSelected()) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, tileSize, tileSize);
        }
    }
    
    /**
     * Draw an arrow pointing in the specified direction.
     * 
     * @param gc the graphics context
     * @param x the center x-coordinate
     * @param y the center y-coordinate
     * @param angle the angle in radians
     * @param size the size of the arrow
     */
    private void drawArrow(GraphicsContext gc, double x, double y, double angle, double size) {
        double[] xPoints = new double[3];
        double[] yPoints = new double[3];
        
        // Arrow tip
        xPoints[0] = x + size * Math.cos(angle);
        yPoints[0] = y + size * Math.sin(angle);
        
        // Arrow left corner
        xPoints[1] = x + size * 0.6 * Math.cos(angle + 2.5);
        yPoints[1] = y + size * 0.6 * Math.sin(angle + 2.5);
        
        // Arrow right corner
        xPoints[2] = x + size * 0.6 * Math.cos(angle - 2.5);
        yPoints[2] = y + size * 0.6 * Math.sin(angle - 2.5);
        
        gc.fillPolygon(xPoints, yPoints, 3);
    }
} 