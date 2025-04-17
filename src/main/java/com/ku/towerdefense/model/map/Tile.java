package com.ku.towerdefense.model.map;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single tile in the game map.
 */
public class Tile implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private TileType type;
    private final int x;
    private final int y;
    protected transient Image image;
    
    // Static cache for tile images
    private static Map<TileType, Image> tileImages = new HashMap<>();
    private static Image castleImage; // Special image for the end point
    
    // Static initialization of tile images
    static {
        loadTileImages();
    }
    
    /**
     * Load all tile images from the asset pack
     */
    private static void loadTileImages() {
        try {
            String basePath = System.getProperty("user.dir") + File.separator + "Asset_pack" + File.separator;
            String tilesPath = basePath + "Tiles" + File.separator;
            String towersPath = basePath + "Towers" + File.separator;
            
            // Load tileset
            File tilesetFile = new File(tilesPath + "Tileset 64x64.png");
            
            // Load castle for end point
            File castleFile = new File(towersPath + "Castle128.png");
            if (castleFile.exists()) {
                castleImage = new Image(castleFile.toURI().toString(), 32, 32, true, true);
                System.out.println("Loaded castle image for end point");
            } else {
                System.err.println("Castle image not found at: " + castleFile.getAbsolutePath());
            }
            
            if (tilesetFile.exists()) {
                Image tileset = new Image(tilesetFile.toURI().toString());
                
                // Define viewport coordinates for each tile type from the tileset
                // These coordinates are based on a 64x64 tileset grid
                
                // GRASS - use green grass tile (row 0, column 0)
                extractTileFromTileset(tileset, TileType.GRASS, 0, 0);
                
                // PATH - use dirt/road tile (row 1, column 1)
                extractTileFromTileset(tileset, TileType.PATH, 1, 1);
                
                // START_POINT - use a special blue tile (row 2, column 2)
                extractTileFromTileset(tileset, TileType.START_POINT, 2, 2);
                
                // We'll use the castle image for END_POINT instead of a tileset extraction
                
                // TOWER_SLOT - use stone platform tile (row 3, column 1)
                extractTileFromTileset(tileset, TileType.TOWER_SLOT, 3, 1);
                
                // DECORATION - use decorative tile (row 0, column 3)
                extractTileFromTileset(tileset, TileType.DECORATION, 0, 3);
                
                System.out.println("Loaded tileset images successfully");
            } else {
                System.err.println("Tileset file not found at: " + tilesetFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Failed to load tile images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extract a tile from the tileset based on row and column
     * 
     * @param tileset the source tileset image
     * @param tileType the tile type to associate with this image
     * @param row the row in the tileset (0-based)
     * @param col the column in the tileset (0-based)
     */
    private static void extractTileFromTileset(Image tileset, TileType tileType, int row, int col) {
        try {
            // Each tile in the tileset is 64x64 pixels
            int tileSize = 64;
            
            // Calculate the viewport (the section of the tileset to use)
            Rectangle2D viewport = new Rectangle2D(
                col * tileSize,     // x position in the tileset
                row * tileSize,     // y position in the tileset
                tileSize,           // width of the tile
                tileSize            // height of the tile
            );
            
            // Create image view to extract the specific tile
            ImageView imageView = new ImageView(tileset);
            imageView.setViewport(viewport);
            imageView.setFitWidth(32);  // Scale down to 32x32 for our game
            imageView.setFitHeight(32);
            imageView.setSmooth(true);  // Enable smoother scaling
            
            // Create a snapshot of just this part of the tileset
            WritableImage extractedTile = new WritableImage(32, 32);
            imageView.snapshot(null, extractedTile);
            
            // Store in our image cache
            tileImages.put(tileType, extractedTile);
            
            System.out.println("Extracted tile for type " + tileType);
        } catch (Exception e) {
            System.err.println("Error extracting tile for type " + tileType + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Constructor for a new tile.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param type type of the tile
     */
    public Tile(int x, int y, TileType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.image = (type == TileType.END_POINT) ? castleImage : tileImages.get(type);
    }
    
    /**
     * Render the tile on the canvas.
     * 
     * @param gc the graphics context to draw on
     * @param tileSize the size of the tile in pixels
     */
    public void render(GraphicsContext gc, int tileSize) {
        Image tileImage = (type == TileType.END_POINT) ? castleImage : tileImages.get(type);
        
        if (tileImage != null && !tileImage.isError()) {
            // Draw the tile image
            gc.drawImage(tileImage, x * tileSize, y * tileSize, tileSize, tileSize);
            
            // Add a visual indicator for special tiles
            if (type == TileType.START_POINT) {
                gc.setFill(Color.BLUE);
                gc.setGlobalAlpha(0.3);
                gc.fillOval(x * tileSize + 8, y * tileSize + 8, tileSize - 16, tileSize - 16);
                gc.setGlobalAlpha(1.0);
            } else if (type == TileType.TOWER_SLOT) {
                gc.setStroke(Color.YELLOW);
                gc.setGlobalAlpha(0.4);
                gc.strokeRect(x * tileSize + 4, y * tileSize + 4, tileSize - 8, tileSize - 8);
                gc.setGlobalAlpha(1.0);
            }
        } else {
            // Fallback rendering based on tile type with better colors
            switch (type) {
                case GRASS:
                    // Draw a nice grass texture
                    gc.setFill(Color.rgb(100, 180, 100));
                    gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    
                    // Add some variation with little dots
                    gc.setFill(Color.rgb(80, 160, 80));
                    for (int i = 0; i < 5; i++) {
                        double dotX = x * tileSize + Math.random() * tileSize;
                        double dotY = y * tileSize + Math.random() * tileSize;
                        gc.fillOval(dotX, dotY, 2, 2);
                    }
                    break;
                    
                case PATH:
                    // Draw a dirt path
                    gc.setFill(Color.rgb(180, 140, 100));
                    gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    
                    // Add some path texture
                    gc.setStroke(Color.rgb(160, 120, 80));
                    gc.setLineWidth(0.5);
                    gc.strokeLine(x * tileSize, y * tileSize, (x + 1) * tileSize, (y + 1) * tileSize);
                    gc.strokeLine((x + 1) * tileSize, y * tileSize, x * tileSize, (y + 1) * tileSize);
                    break;
                    
                case START_POINT:
                    // Draw a nicer start point
                    gc.setFill(Color.rgb(80, 180, 250));
                    gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    
                    // Add an arrow or symbol
                    gc.setFill(Color.WHITE);
                    double[] xPoints = {
                        x * tileSize + tileSize * 0.2, 
                        x * tileSize + tileSize * 0.8, 
                        x * tileSize + tileSize * 0.5
                    };
                    double[] yPoints = {
                        y * tileSize + tileSize * 0.2, 
                        y * tileSize + tileSize * 0.2, 
                        y * tileSize + tileSize * 0.8
                    };
                    gc.fillPolygon(xPoints, yPoints, 3);
                    break;
                    
                case END_POINT:
                    // Draw a nicer end point with castle-like appearance
                    gc.setFill(Color.rgb(250, 80, 80));
                    gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    
                    // Draw a simple castle
                    gc.setFill(Color.DARKGRAY);
                    gc.fillRect(x * tileSize + 6, y * tileSize + 10, tileSize - 12, tileSize - 14);
                    
                    // Castle towers
                    gc.fillRect(x * tileSize + 4, y * tileSize + 6, 6, 8);
                    gc.fillRect(x * tileSize + tileSize - 10, y * tileSize + 6, 6, 8);
                    
                    // Castle door
                    gc.setFill(Color.BLACK);
                    gc.fillRect(x * tileSize + (tileSize/2) - 3, y * tileSize + tileSize - 12, 6, 8);
                    break;
                    
                case DECORATION:
                    // Decoration tiles (rocks, trees, etc)
                    gc.setFill(Color.rgb(200, 200, 200));
                    gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    
                    // Add some rock-like texture
                    gc.setFill(Color.rgb(150, 150, 150));
                    gc.fillOval(x * tileSize + tileSize * 0.25, y * tileSize + tileSize * 0.25, tileSize * 0.5, tileSize * 0.5);
                    break;
                    
                case TOWER_SLOT:
                    // Tower placement slots
                    gc.setFill(Color.rgb(150, 150, 200));
                    gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    
                    // Add a platform-like pattern
                    gc.setStroke(Color.rgb(100, 100, 150));
                    gc.setLineWidth(1);
                    gc.strokeRect(x * tileSize + 2, y * tileSize + 2, tileSize - 4, tileSize - 4);
                    break;
                    
                default:
                    // Unknown tile types
                    gc.setFill(Color.BLACK);
                    gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }
    }
    
    /**
     * Initialize any transient fields after deserialization.
     */
    public void initTransientFields() {
        if (tileImages.isEmpty()) {
            loadTileImages();
        }
        this.image = tileImages.get(type);
    }
    
    /**
     * Get the tile image.
     * 
     * @return the tile image
     */
    public Image getImage() {
        return image;
    }
    
    /**
     * Set the tile image.
     * 
     * @param image the image to set
     */
    public void setImage(Image image) {
        this.image = image;
    }
    
    /**
     * Get the x coordinate.
     * 
     * @return x coordinate
     */
    public int getX() {
        return x;
    }
    
    /**
     * Get the y coordinate.
     * 
     * @return y coordinate
     */
    public int getY() {
        return y;
    }
    
    /**
     * Get the tile type.
     * 
     * @return the type of this tile
     */
    public TileType getType() {
        return type;
    }
    
    /**
     * Set the tile type.
     * 
     * @param type the new type to set
     */
    public void setType(TileType type) {
        this.type = type;
    }
    
    /**
     * Check if a tower can be placed on this tile.
     * 
     * @return true if a tower can be placed, false otherwise
     */
    public boolean canPlaceTower() {
        // Towers can only be placed on grass tiles
        return type == TileType.GRASS;
    }
    
    /**
     * Check if enemies can walk on this tile.
     * 
     * @return true if enemies can walk on this tile, false otherwise
     */
    public boolean isWalkable() {
        // Enemies can walk on path, start point, and end point tiles
        return type == TileType.PATH || 
               type == TileType.START_POINT || 
               type == TileType.END_POINT;
    }
    
    /**
     * Check if this tile is a start point.
     * 
     * @return true if this is a start point, false otherwise
     */
    public boolean isStartPoint() {
        return type == TileType.START_POINT;
    }
    
    /**
     * Check if this tile is an end point.
     * 
     * @return true if this is an end point, false otherwise
     */
    public boolean isEndPoint() {
        return type == TileType.END_POINT;
    }
    
    @Override
    public String toString() {
        return "Tile{" +
                "type=" + type +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
} 