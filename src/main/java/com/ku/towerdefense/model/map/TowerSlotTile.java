package com.ku.towerdefense.model.map;

import com.ku.towerdefense.model.entity.Tower;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Represents a tower slot tile where towers can be built.
 */
public class TowerSlotTile extends Tile {
    private static final long serialVersionUID = 1L;
    private static final String IMAGE_PATH = "/Asset_pack/Towers/TowerSlotwithoutbackground128.png";
    
    private transient Tower tower;
    private transient Image image;
    private boolean selected;
    
    /**
     * Constructor for TowerSlotTile.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param tileSize the size of the tile in pixels
     */
    public TowerSlotTile(int x, int y, int tileSize) {
        super(x, y, TileType.TOWER_SLOT);
        this.tower = null;
        this.selected = false;
        initTransientFields();
    }
    
    @Override
    public void initTransientFields() {
        try {
            this.image = new Image(getClass().getResourceAsStream(IMAGE_PATH));
        } catch (Exception e) {
            System.err.println("Failed to load tower slot image: " + e.getMessage());
        }
    }
    
    @Override
    public void render(GraphicsContext gc, int tileSize) {
        // Calculate screen position
        int x = getX() * tileSize;
        int y = getY() * tileSize;
        
        // Draw the underlying tile (grass)
        // This would be more efficient if we had a tileset manager
        try {
            if (image != null) {
                gc.drawImage(image, x, y, tileSize, tileSize);
            }
            
            // Draw the tower if there is one
            if (tower != null) {
                tower.render(gc);
            }
            
            // Draw selection highlight if selected
            if (isSelected()) {
                gc.setStroke(javafx.scene.paint.Color.YELLOW);
                gc.setLineWidth(2);
                gc.strokeRect(x, y, tileSize, tileSize);
            }
        } catch (Exception e) {
            System.err.println("Error rendering tower slot: " + e.getMessage());
        }
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
     * Check if the tower slot has a tower built on it.
     * 
     * @return true if a tower is built, false otherwise
     */
    public boolean hasTower() {
        return tower != null;
    }
    
    /**
     * Get the tower built on this slot.
     * 
     * @return the tower, or null if no tower is built
     */
    public Tower getTower() {
        return tower;
    }
    
    /**
     * Set the tower for this slot.
     * 
     * @param tower the tower to build, or null to clear
     */
    public void setTower(Tower tower) {
        this.tower = tower;
    }
} 