package com.ku.towerdefense.model.map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents an end point tile where enemies exit the map.
 */
public class EndPointTile extends PathTile {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for EndPointTile.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param tileSize the size of the tile in pixels
     */
    public EndPointTile(int x, int y, int tileSize) {
        super(x, y, tileSize);
        setType(TileType.END_POINT);
    }

    @Override
    public void render(GraphicsContext gc, int tileSize) {
        int x = getCol() * tileSize;
        int y = getRow() * tileSize;
        
        // Draw base path tile appearance
        gc.setFill(Color.SANDYBROWN);
        gc.fillRect(x, y, tileSize, tileSize);
        
        // Draw border
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, tileSize, tileSize);
        
        // Draw end point indicator (red circle)
        gc.setFill(Color.RED);
        double centerX = x + tileSize / 2;
        double centerY = y + tileSize / 2;
        double radius = tileSize / 4;
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw an X shape inside the circle
        double xSize = radius * 0.8;
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeLine(centerX - xSize, centerY - xSize, centerX + xSize, centerY + xSize);
        gc.strokeLine(centerX + xSize, centerY - xSize, centerX - xSize, centerY + xSize);
        
        // Draw selection highlight if selected
        if (isSelected()) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, tileSize, tileSize);
        }
    }
} 