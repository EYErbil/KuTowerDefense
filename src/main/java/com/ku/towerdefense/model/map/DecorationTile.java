package com.ku.towerdefense.model.map;

import java.io.File;
import java.io.IOException;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

/**
 * Represents a decoration tile in the game map.
 * These tiles are for visual enhancement only and don't affect gameplay.
 */
public class DecorationTile extends Tile {
    private static final long serialVersionUID = 1L;
    private static final String IMAGE_PATH = "/Asset_pack/Tiles/Tileset 64x64.png";
    
    private int tilesetRow;
    private int tilesetCol;
    private transient Image tileImage;
    
    /**
     * Constructor for DecorationTile.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param tileSize the size of the tile in pixels
     * @param tilesetRow the row in the tileset
     * @param tilesetCol the column in the tileset
     */
    public DecorationTile(int x, int y, int tileSize, int tilesetRow, int tilesetCol) {
        super(x, y, TileType.DECORATION);
        this.tilesetRow = tilesetRow;
        this.tilesetCol = tilesetCol;
        initTransientFields();
    }
    
    @Override
    public void initTransientFields() {
        try {
            // Load the tileset image
            Image tileset = new Image(getClass().getResourceAsStream(IMAGE_PATH));
            
            // Extract the specific tile based on tilesetRow and tilesetCol
            // Assuming 64x64 tile size in the tileset
            int tilesetTileSize = 64;
            int sourceX = tilesetCol * tilesetTileSize;
            int sourceY = tilesetRow * tilesetTileSize;
            
            // Create a sub-image for this specific decoration tile
            this.tileImage = new javafx.scene.image.WritableImage(
                tileset.getPixelReader(),
                sourceX, sourceY,
                tilesetTileSize, tilesetTileSize
            );
        } catch (Exception e) {
            System.err.println("Failed to load decoration tile image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void render(GraphicsContext gc, int tileSize) {
        int x = getX() * tileSize;
        int y = getY() * tileSize;
        
        // Draw the grass as background
        gc.drawImage(new GrassTile(getX(), getY(), tileSize).getImage(), x, y, tileSize, tileSize);
        
        // Draw the decoration on top if image is loaded
        if (tileImage != null) {
            gc.drawImage(tileImage, x, y, tileSize, tileSize);
        }
        
        // Draw selection highlight if selected
        if (isSelected()) {
            gc.setStroke(javafx.scene.paint.Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, tileSize, tileSize);
        }
    }
    
    /**
     * Check if this tile is selected.
     * 
     * @return true if selected, false otherwise
     */
    public boolean isSelected() {
        return false; // Default implementation, can be overridden
    }
    
    /**
     * Get the decoration tile image.
     * 
     * @return the decoration tile image
     */
    public Image getImage() {
        return tileImage;
    }
    
    /**
     * Get the row position in the tileset.
     * 
     * @return the tileset row
     */
    public int getTilesetRow() {
        return tilesetRow;
    }
    
    /**
     * Get the column position in the tileset.
     * 
     * @return the tileset column
     */
    public int getTilesetCol() {
        return tilesetCol;
    }
} 