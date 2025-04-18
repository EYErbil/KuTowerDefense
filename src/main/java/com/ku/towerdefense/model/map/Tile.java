package com.ku.towerdefense.model.map;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
// import javafx.scene.SnapshotParameters; // No longer needed
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
// import javafx.scene.image.ImageView; // No longer needed in slice
// import javafx.scene.image.WritableImage; // No longer needed
import javafx.scene.paint.Color;
// import javafx.scene.image.PixelReader; // No longer needed
// import javafx.scene.image.PixelWriter; // No longer needed

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
    public static final int SOURCE_TILE_SIZE  = 64;   // size in the atlas
    private static final int RENDER_TILE_SIZE  = 64;   // we always draw @64 px

    /** Row / column of every sprite in the tileset */
    private static final Map<TileType, Point2D> TILE_COORDS = new EnumMap<>(TileType.class);
    static {
        // TILE_COORDS are likely incorrect given the actual asset sheet layout
        // Remove specific GRASS mapping as it was wrong
        // TILE_COORDS.put(TileType.GRASS,       new Point2D(0, 0)); 

        // Keep other mappings for now, though they are also likely wrong
        TILE_COORDS.put(TileType.PATH,        new Point2D(1, 1)); 
        TILE_COORDS.put(TileType.PATH_V,      new Point2D(0, 1));
        TILE_COORDS.put(TileType.PATH_H,      new Point2D(2, 1));
        TILE_COORDS.put(TileType.PATH_NE,     new Point2D(1, 0));
        TILE_COORDS.put(TileType.PATH_NW,     new Point2D(1, 2));
        TILE_COORDS.put(TileType.PATH_SE,     new Point2D(0, 2));
        TILE_COORDS.put(TileType.PATH_SW,     new Point2D(0, 0));
        TILE_COORDS.put(TileType.DECORATION,  new Point2D(0, 3)); // Assumed Tree
        TILE_COORDS.put(TileType.OBSTACLE,    new Point2D(0, 4)); // Assumed Rock
        TILE_COORDS.put(TileType.START_POINT, new Point2D(2, 2)); // Needs clarification
    }

    /* ──────────────────────────  Static Resources  ───────────────────────── */

    private static boolean  imagesLoaded = false;
    private static Image    tileset;       // The whole atlas
    private static Image    castleImage;   // Specific image for end point
    private static Image    towerSlotImage; // Specific image for tower slot
    // private static final Map<TileType, Image> CACHE = new EnumMap<>(TileType.class); // REMOVED CACHE

    /* ──────────────────────────────  Fields  ─────────────────────────────── */

    private final int  x, y;
    private TileType   type;
    // No longer need transient image instance field, will draw directly using static images + viewport
    // private transient Image image;

    /* ─────────────────────────────  Public API  ──────────────────────────── */

    public Tile(int x, int y, TileType type) {
        this.x    = x;
        this.y    = y;
        this.type = type;
        loadImagesIfNeeded(); // Ensure static images are loaded when a tile is created
        // initTransientFields(); // No longer needed
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public TileType getType() { return type; }

    public void setType(TileType type) {
        if (this.type != type) {
            this.type = type;
            // No transient fields to init
        }
    }

    public boolean canPlaceTower() {
        return type == TileType.TOWER_SLOT;
    }

    public boolean isWalkable() {
        return switch (type) {
            case START_POINT, END_POINT, PATH, PATH_V, PATH_H, PATH_NE,
                 PATH_NW, PATH_SE, PATH_SW -> true;
            default -> false;
        };
    }

    /** Gets the correct static Image object for this tile's type */
    public Image getBaseImage() {
         loadImagesIfNeeded(); 
         return getBaseImageForType(this.type);
    }

    /** Gets the source viewport Rectangle2D within the atlas for this tile's type */
    public Rectangle2D getSourceViewport() {
        Point2D coords = getCoordsForType(this.type);
        if (coords != null && type != TileType.END_POINT && type != TileType.TOWER_SLOT) {
            int col = (int) coords.getX();
            int row = (int) coords.getY();
            return new Rectangle2D(col * SOURCE_TILE_SIZE, row * SOURCE_TILE_SIZE,
                                   SOURCE_TILE_SIZE, SOURCE_TILE_SIZE);
        } else {
            return null; 
        }
    }

    /** 
     * Gets the correct base static Image object for a given tile type.
     * Ensures images are loaded.
     */
    public static Image getBaseImageForType(TileType type) {
        loadImagesIfNeeded(); // Ensure images are loaded
        return switch (type) {
            case END_POINT   -> castleImage;
            case TOWER_SLOT  -> towerSlotImage;
            default          -> tileset; // Assume others use tileset
        };
    }

    /** Gets the defined Point2D coordinates (col, row) for a TileType from the TILE_COORDS map. */
    public static Point2D getCoordsForType(TileType type) {
         return TILE_COORDS.get(type); // Return null if not mapped
    }

    public void render(GraphicsContext gc, int renderTileSize) {
        Image baseImage = getBaseImage(); // Get castle, tower slot, or main tileset

        if (baseImage != null && !baseImage.isError()) {
            Rectangle2D viewport = getSourceViewport(); // Get viewport if applicable

            if (viewport != null) {
                // Draw from tileset using source viewport
                gc.drawImage(baseImage, 
                             viewport.getMinX(), viewport.getMinY(), // Source X, Y
                             viewport.getWidth(), viewport.getHeight(), // Source W, H
                             x * renderTileSize, y * renderTileSize, // Dest X, Y
                             renderTileSize, renderTileSize); // Dest W, H
            } else {
                // Draw the whole image (castle, tower slot)
                gc.drawImage(baseImage, x * renderTileSize, y * renderTileSize,
                                     renderTileSize, renderTileSize);
            }
        } else {             // fail‑safe fallback
            gc.setFill(Color.MAGENTA);
            gc.fillRect(x * renderTileSize, y * renderTileSize,
                    renderTileSize, renderTileSize);
        }
    }

    /* ─────────────────────────  Private Helpers  ────────────────────────── */
    // Removed initTransientFields

    private static synchronized void loadImagesIfNeeded() {
        if (imagesLoaded) return;
        System.out.println("--> Entering loadImagesIfNeeded...");
        try {
            System.out.println("    Loading tileset...");
            tileset = loadPNG("/Asset_pack/Tiles/Tileset 64x64.png"); // Load at native size
            if (tileset == null) System.err.println("    -> Tileset load returned NULL");
            else System.out.println("    -> Tileset loaded successfully. ID: " + Integer.toHexString(System.identityHashCode(tileset)));

            System.out.println("    Loading castle image...");
            castleImage = loadPNG("/Asset_pack/Towers/Castle128.png", RENDER_TILE_SIZE);
            if (castleImage == null) System.err.println("    -> Castle image load returned NULL");
            else System.out.println("    -> Castle image loaded successfully. ID: " + Integer.toHexString(System.identityHashCode(castleImage)));

            String towerSlotFilename = "TowerSlotwithoutbackground128.png"; 
            System.out.println("    Loading tower slot image (" + towerSlotFilename + ")...");
            towerSlotImage = loadPNG("/Asset_pack/Towers/" + towerSlotFilename, RENDER_TILE_SIZE);
            if (towerSlotImage == null) System.err.println("    -> Tower slot image load returned NULL");
            else System.out.println("    -> Tower slot image loaded successfully. ID: " + Integer.toHexString(System.identityHashCode(towerSlotImage)));
            
            // NO SLICING OR CACHING NEEDED HERE ANYMORE
            
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
        // System.out.println("      Attempting loadPNG: " + classpath); // Optional: verbose
        InputStream in = Tile.class.getResourceAsStream(classpath);
        if (in == null) {
            System.err.println("      -> PNG stream NULL for: " + classpath);
            return null;
        }
        try {
            // Load image, optionally resizing if target != source size (though here target is likely render size)
            Image img = new Image(in, target, target, true, true); 
            if (img.isError()) {
                 System.err.println("      -> Image error after loading stream: " + classpath + " Error: " + img.getException());
                 img.getException().printStackTrace();
                 return null; 
            }
            // System.out.println("      -> loadPNG successful: " + classpath); // Optional: verbose
            return img;
        } catch (Exception e) {
             System.err.println("      -> EXCEPTION during new Image() for: " + classpath + " Error: " + e.getMessage());
             e.printStackTrace();
             return null;
        } finally {
            try { in.close(); } catch (IOException ioex) { /* ignore close exception */ }
        }
    }
    // Load PNG at its native size unless target is specified
    private static Image loadPNG(String classpath) { 
         InputStream in = Tile.class.getResourceAsStream(classpath);
        if (in == null) {
            System.err.println("      -> PNG stream NULL for: " + classpath);
            return null;
        }
        try {
            Image img = new Image(in); // Load native size
            if (img.isError()) {
                 System.err.println("      -> Image error after loading stream: " + classpath + " Error: " + img.getException());
                 img.getException().printStackTrace();
                 return null; 
            }
            return img;
        } catch (Exception e) {
             System.err.println("      -> EXCEPTION during new Image() for: " + classpath + " Error: " + e.getMessage());
             e.printStackTrace();
             return null;
        } finally {
            try { in.close(); } catch (IOException ioex) { /* ignore close exception */ }
        }
    }

    /* ───────────────────────────── toString()  ───────────────────────────── */

    @Override public String toString() {
        return "Tile{" + type + " (" + x + "," + y + ")}";
    }
}
