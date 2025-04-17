package com.ku.towerdefense.model.map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Represents a path tile that enemies follow.
 */
public class PathTile extends Tile {
    private static final long serialVersionUID = 1L;
    private static final String IMAGE_PATH = "/Asset_pack/Tiles/Tileset 64x64.png";
    
    // For connected path rendering
    protected PathTile next;
    protected boolean selected;
    protected transient Image image;
    
    /**
     * Constructor for PathTile.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param tileSize the size of the tile in pixels (for rendering)
     */
    public PathTile(int x, int y, int tileSize) {
        super(x, y, TileType.PATH);
        this.selected = false;
        initTransientFields();
    }
    
    /**
     * Initialize transient fields after deserialization.
     */
    @Override
    public void initTransientFields() {
        try {
            // Load the tileset image
            Image tileset = new Image(getClass().getResourceAsStream(IMAGE_PATH));
            
            // Extract the path tile from the tileset
            // This assumes the path tile is at position (1,0) in the tileset
            int tileX = 64; // One tile to the right (assuming 64x64 tiles)
            int tileY = 0;
            
            this.image = new javafx.scene.image.WritableImage(
                tileset.getPixelReader(),
                tileX, tileY,
                64, 64 // Assuming the tileset has 64x64 tiles
            );
        } catch (Exception e) {
            System.err.println("Failed to load path tile image: " + e.getMessage());
        }
    }
    
    /**
     * Render the path tile on the canvas.
     * 
     * @param gc the graphics context to draw on
     * @param tileSize the size of the tile in pixels
     */
    @Override
    public void render(GraphicsContext gc, int tileSize) {
        int x = getX() * tileSize;
        int y = getY() * tileSize;
        
        // Draw base path tile appearance
        gc.setFill(Color.SANDYBROWN);
        gc.fillRect(x, y, tileSize, tileSize);
        
        // Draw selection highlight if selected
        if (selected) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, tileSize, tileSize);
        }
    }
    
    /**
     * Set the next tile in the path.
     * 
     * @param next the next tile
     */
    public void setNext(PathTile next) {
        this.next = next;
    }
    
    /**
     * Get the next tile in the path.
     * 
     * @return the next tile
     */
    public PathTile getNext() {
        return next;
    }
    
    /**
     * Check if this tile is selected.
     * 
     * @return true if selected, false otherwise
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * Set whether this tile is selected.
     * 
     * @param selected true to select, false to deselect
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * Get the row of this tile.
     * 
     * @return the row (y-coordinate)
     */
    public int getRow() {
        return getY();
    }
    
    /**
     * Get the column of this tile.
     * 
     * @return the column (x-coordinate)
     */
    public int getCol() {
        return getX();
    }
    
    /**
     * Get the tile size.
     * 
     * @return the tile size in pixels
     */
    public int getTileSize() {
        return 32; // Default tile size
    }
} 