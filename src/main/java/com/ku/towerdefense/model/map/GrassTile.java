package com.ku.towerdefense.model.map;

import javafx.scene.image.Image;

/**
 * Represents a grass tile in the game map.
 */
public class GrassTile extends Tile {
    private static final long serialVersionUID = 1L;
    private static final String IMAGE_PATH = "/Asset_pack/Tiles/Tileset 64x64.png";
    
    /**
     * Constructor for GrassTile.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param tileSize the size of the tile in pixels
     */
    public GrassTile(int x, int y, int tileSize) {
        super(x, y, TileType.GRASS);
        initTransientFields();
    }
    
    @Override
    public void initTransientFields() {
        try {
            // Load the tileset image
            Image tileset = new Image(getClass().getResourceAsStream(IMAGE_PATH));
            
            // Extract the grass tile from the tileset (first tile)
            // This assumes the grass tile is at position (0,0) in the tileset
            int tileX = 0;
            int tileY = 0;
            
            this.image = new javafx.scene.image.WritableImage(
                tileset.getPixelReader(),
                tileX, tileY,
                64, 64 // Assuming the tileset has 64x64 tiles
            );
        } catch (Exception e) {
            System.err.println("Failed to load grass tile image: " + e.getMessage());
        }
    }
} 