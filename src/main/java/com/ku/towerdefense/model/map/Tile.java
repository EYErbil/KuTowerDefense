package com.ku.towerdefense.model.map;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a single tile on the map.
 */
public class Tile implements Serializable {

    /* ─────────────────────────────  Constants  ───────────────────────────── */

    private static final long serialVersionUID = 1L;
    public static final int SOURCE_TILE_SIZE  = 64;   // source atlas images are 64px
    private static final int RENDER_TILE_SIZE  = 64;   // we always draw @64 px

    /** Stores coordinate points (x, y) for tile types within the tileset **/
    private static final Map<TileType, Point2D> TILE_COORDS = new EnumMap<>(TileType.class);
    static {
        // Updated coordinates based on the tileset image
        // First row (top) in tileset
        TILE_COORDS.put(TileType.GRASS,       new Point2D(0, 0));    // Green grass (center of the circular path)
        
        // Path tiles (from the upper half of the tileset)
        TILE_COORDS.put(TileType.PATH,        new Point2D(0, 0));    // Circular path (full circle) 
        TILE_COORDS.put(TileType.PATH_H,      new Point2D(1, 0));    // Horizontal path
        TILE_COORDS.put(TileType.PATH_V,      new Point2D(2, 0));    // Vertical path
        TILE_COORDS.put(TileType.PATH_NE,     new Point2D(0, 1));    // Northeast corner path
        TILE_COORDS.put(TileType.PATH_NW,     new Point2D(1, 1));    // Northwest corner path
        TILE_COORDS.put(TileType.PATH_SE,     new Point2D(0, 2));    // Southeast corner path
        TILE_COORDS.put(TileType.PATH_SW,     new Point2D(1, 2));    // Southwest corner path
        
        // Character/start point
        TILE_COORDS.put(TileType.START_POINT, new Point2D(2, 2));    // Character on path
        
        // Trees (from the lower half of the tileset)
        TILE_COORDS.put(TileType.TREE1,       new Point2D(0, 3));    // First tree
        TILE_COORDS.put(TileType.TREE2,       new Point2D(1, 3));    // Second tree
        TILE_COORDS.put(TileType.TREE3,       new Point2D(2, 3));    // Third tree
        TILE_COORDS.put(TileType.DECORATION,  new Point2D(0, 3));    // Default decoration (first tree)
        
        // Obstacles
        TILE_COORDS.put(TileType.ROCK1,       new Point2D(3, 3));    // First rock
        TILE_COORDS.put(TileType.ROCK2,       new Point2D(3, 4));    // Second rock
        TILE_COORDS.put(TileType.OBSTACLE,    new Point2D(3, 3));    // Default obstacle (first rock)
        
        // Buildings and props
        TILE_COORDS.put(TileType.WELL,        new Point2D(2, 4));    // Water well
        TILE_COORDS.put(TileType.HOUSE,       new Point2D(2, 5));    // House
        TILE_COORDS.put(TileType.WOOD_PILE,   new Point2D(3, 5));    // Wood pile
        TILE_COORDS.put(TileType.BARREL,      new Point2D(0, 4));    // Barrel
        
        // Special tiles that use separate images
        // END_POINT (castle) and TOWER_SLOT - these use separate images, not mapped here
    }

    /* ──────────────────────────  Static Resources  ───────────────────────── */

    private static boolean  imagesLoaded = false;
    private static Image    tileset;       // The whole atlas
    private static Image    castleImage;   // Specific image for end point
    private static Image    towerSlotImage; // Specific image for tower slot
    private static final Map<TileType, Image> CACHE = new EnumMap<>(TileType.class);

    /* ──────────────────────────────  Fields  ─────────────────────────────── */

    private final int  x, y;
    private TileType   type;
    private transient Image image;

    /* ─────────────────────────────  Public API  ──────────────────────────── */

    public Tile(int x, int y, TileType type) {
        this.x    = x;
        this.y    = y;
        this.type = type;
        loadImagesIfNeeded();
        initTransientFields();
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public TileType getType() { return type; }

    public void setType(TileType type) {
        if (this.type != type) {
            this.type = type;
            initTransientFields();
        }
    }

    public boolean canPlaceTower() {
        return type == TileType.TOWER_SLOT;
    }

    public boolean isWalkable() {
        return switch (type) {
            case START_POINT, END_POINT, PATH, PATH_V, PATH_H, PATH_NE,
                 PATH_NW, PATH_SE, PATH_SW -> true;
            case GRASS -> true; // Grass is walkable but allows tower placement
            case TOWER_SLOT, DECORATION, OBSTACLE, 
                 TREE1, TREE2, TREE3, ROCK1, ROCK2 -> false; // These block movement
            default -> false;
        };
    }

    public Image getImage() {
        return image;
    }

    public static Image getBaseImageForType(TileType type) {
        loadImagesIfNeeded();
        return switch (type) {
            case END_POINT -> castleImage;
            case TOWER_SLOT -> towerSlotImage;
            default -> tileset;
        };
    }

    public Rectangle2D getSourceViewport() {
        Point2D coords = TILE_COORDS.get(type);
        if (coords != null) {
            return new Rectangle2D(
                coords.getX() * SOURCE_TILE_SIZE,
                coords.getY() * SOURCE_TILE_SIZE,
                SOURCE_TILE_SIZE,
                SOURCE_TILE_SIZE
            );
        }
        return null;
    }

    public static Rectangle2D getSourceViewportForType(TileType type) {
        Point2D coords = TILE_COORDS.get(type);
        if (coords != null) {
            return new Rectangle2D(
                coords.getX() * SOURCE_TILE_SIZE,
                coords.getY() * SOURCE_TILE_SIZE,
                SOURCE_TILE_SIZE,
                SOURCE_TILE_SIZE
            );
        }
        return null;
    }

    public void render(GraphicsContext gc, int renderTileSize) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, x * renderTileSize, y * renderTileSize,
                    renderTileSize, renderTileSize);
        } else {
            gc.setFill(Color.MAGENTA);
            gc.fillRect(x * renderTileSize, y * renderTileSize,
                    renderTileSize, renderTileSize);
        }
    }

    /* ─────────────────────────  Private Helpers  ────────────────────────── */

    private void initTransientFields() {
        // Try to get pre-sliced image from cache
        image = CACHE.get(type);
        if (image == null) {
            // Get the base image based on type
            Image baseImage = getBaseImageForType(type);
            
            if (baseImage != null && !baseImage.isError()) {
                if (baseImage == tileset) { // Is this type derived from the main tileset?
                    // Slice from tileset using coordinates
                    Point2D coords = TILE_COORDS.get(type);
                    if (coords != null) {
                        ImageView view = new ImageView(baseImage);
                        view.setViewport(new Rectangle2D(
                                coords.getX() * SOURCE_TILE_SIZE,
                                coords.getY() * SOURCE_TILE_SIZE,
                                SOURCE_TILE_SIZE,
                                SOURCE_TILE_SIZE));
                                
                        // Create snapshot of the sliced image
                        SnapshotParameters params = new SnapshotParameters();
                        params.setFill(Color.TRANSPARENT);
                        image = view.snapshot(params, null);
                        
                        // Cache for future use
                        CACHE.put(type, image);
                    }
                } else {
                    // For special images like castle or tower slot, use as is
                    image = baseImage;
                }
            }
        }
    }

    private static synchronized void loadImagesIfNeeded() {
        if (imagesLoaded) return;
        System.out.println("--> Entering loadImagesIfNeeded...");
        try {
            System.out.println("    Loading tileset...");
            // Make sure we use the tileset with the character, castle and other elements
            tileset = loadPNG("/Asset_pack/Tiles/Tileset 64x64.png");
            if (tileset == null) {
                System.err.println("    -> Tileset load returned NULL, trying alternative tileset");
                // Try loading from the other tilesets as backup
                tileset = loadPNG("/Asset_pack/Tiles/Tileset 96x96.png");
                if (tileset == null) {
                    tileset = loadPNG("/Asset_pack/Tiles/Tileset 128x128.png");
                    if (tileset == null) {
                        System.err.println("    -> All tileset load attempts failed");
                    } else {
                        System.out.println("    -> Alternative tileset 128x128 loaded");
                    }
                } else {
                    System.out.println("    -> Alternative tileset 96x96 loaded");
                }
            } else {
                System.out.println("    -> Tileset loaded successfully. ID: " + Integer.toHexString(System.identityHashCode(tileset)));
            }

            System.out.println("    Loading castle image...");
            castleImage = loadPNG("/Asset_pack/Towers/Castle128.png", RENDER_TILE_SIZE);
            if (castleImage == null) System.err.println("    -> Castle image load returned NULL");
            else System.out.println("    -> Castle image loaded successfully. ID: " + Integer.toHexString(System.identityHashCode(castleImage)));

            // Try to load the tower slot with transparent background
            String towerSlotFilename = "TowerSlotwithoutbackground128.png";
            System.out.println("    Loading tower slot image (" + towerSlotFilename + ")...");
            towerSlotImage = loadPNG("/Asset_pack/Towers/" + towerSlotFilename, RENDER_TILE_SIZE);
            if (towerSlotImage == null) {
                System.err.println("    -> Tower slot image load returned NULL, trying alternate...");
                // Try an alternate image if available
                towerSlotImage = loadPNG("/Asset_pack/Towers/TowerSlot128.png", RENDER_TILE_SIZE);
                if (towerSlotImage == null) {
                    System.err.println("    -> Alternate tower slot image also failed");
                } else {
                    System.out.println("    -> Alternate tower slot image loaded successfully");
                }
            } else {
                System.out.println("    -> Tower slot image loaded successfully. ID: " + Integer.toHexString(System.identityHashCode(towerSlotImage)));
            }

            System.out.println("--> Base Tile images loading process finished.");
            imagesLoaded = true;

        } catch (Exception ex) {
            System.err.println("‼‼ UNCAUGHT EXCEPTION in loadImagesIfNeeded: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
             System.out.println("<-- Exiting loadImagesIfNeeded. imagesLoaded = " + imagesLoaded);
        }
    }

    /* ───────────────────  Static utility methods  ─────────────────── */

    private static Image loadPNG(String classpath, int target) {
        InputStream in = Tile.class.getResourceAsStream(classpath);
        if (in == null) {
            System.err.println("      -> PNG stream NULL for: " + classpath);
            return null;
        }
        try {
            Image img = new Image(in, target, target, true, true);
            if (img.isError()) {
                System.err.println("      -> Failed to load image: " + classpath);
                return null;
            }
            return img;
        } catch (Exception e) {
            System.err.println("      -> Exception loading image: " + e.getMessage());
            return null;
        } finally {
            try { in.close(); } catch (IOException e) { 
                System.err.println("      -> Exception closing stream: " + e.getMessage());
            }
        }
    }

    private static Image loadPNG(String classpath) {
        return loadPNG(classpath, 0); // 0 = use native size
    }

    @Override
    public String toString() {
        return "Tile[x=" + x + ", y=" + y + ", type=" + type + "]";
    }
}
