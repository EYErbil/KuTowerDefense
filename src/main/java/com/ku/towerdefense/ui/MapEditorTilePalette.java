package com.ku.towerdefense.ui;

import com.ku.towerdefense.model.map.Tile;
import com.ku.towerdefense.model.map.TileType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.Priority;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Manages the tile selection palette UI for the Map Editor.
 */
public class MapEditorTilePalette extends VBox {

    // Define which tile types appear in the editor palette
    private static final List<TileType> PALETTE_TILE_TYPES = Arrays.asList(
            // Base Terrain - REMOVED TileType.GRASS

            // Path Tiles - All new path types
            TileType.PATH_HORIZONTAL,
            TileType.PATH_VERTICAL,
            TileType.PATH_CIRCLE_NW,
            TileType.PATH_CIRCLE_N,
            TileType.PATH_CIRCLE_NE,
            TileType.PATH_CIRCLE_E,
            TileType.PATH_CIRCLE_SE,
            TileType.PATH_CIRCLE_S,
            TileType.PATH_CIRCLE_SW,
            TileType.PATH_CIRCLE_W,
            TileType.PATH_VERTICAL_N_DE,
            TileType.PATH_VERTICAL_S_DE,
            TileType.PATH_HORIZONTAL_W_DE,
            TileType.PATH_HORIZONTAL_E_DE,

            // Special points
            TileType.START_POINT,
            TileType.END_POINT,

            TileType.TOWER_SLOT,
            TileType.CASTLE1,
            TileType.TREE_BIG, TileType.TREE_MEDIUM, TileType.TREE_SMALL,
            TileType.HOUSE, TileType.WELL, TileType.LOG_PILE,
            TileType.ROCK_MEDIUM, TileType.ROCK_SMALL,
            TileType.TOWER_ARTILLERY, TileType.TOWER_MAGE, TileType.ARCHER_TOWER, TileType.TOWER_BARACK);

    // Groups for category headers in the palette
    private static final Map<String, List<TileType>> TILE_CATEGORIES = Map.of(
            // "Base Terrain", List.of(TileType.GRASS), // REMOVED
            "Path Tiles", List.of(
                    TileType.PATH_HORIZONTAL, TileType.PATH_VERTICAL,
                    TileType.PATH_CIRCLE_NW, TileType.PATH_CIRCLE_N, TileType.PATH_CIRCLE_NE,
                    TileType.PATH_CIRCLE_E, TileType.PATH_CIRCLE_SE, TileType.PATH_CIRCLE_S,
                    TileType.PATH_CIRCLE_SW, TileType.PATH_CIRCLE_W,
                    TileType.PATH_VERTICAL_N_DE, TileType.PATH_VERTICAL_S_DE,
                    TileType.PATH_HORIZONTAL_W_DE, TileType.PATH_HORIZONTAL_E_DE),
            "Special Points", List.of(TileType.START_POINT, TileType.END_POINT, TileType.CASTLE1),
            "Props", List.of(
                    TileType.HOUSE, TileType.WELL, TileType.LOG_PILE,
                    TileType.TREE_BIG, TileType.TREE_MEDIUM, TileType.TREE_SMALL,
                    TileType.ROCK_MEDIUM, TileType.ROCK_SMALL),
            "Towers", List.of(
                    TileType.TOWER_SLOT,
                    TileType.TOWER_ARTILLERY, TileType.TOWER_MAGE, TileType.ARCHER_TOWER, TileType.TOWER_BARACK));

    private final ToggleGroup tileToggleGroup;
    private final ObjectProperty<TileType> selectedTileTypeProperty = new SimpleObjectProperty<>(null);

    public MapEditorTilePalette() {
        super(5); // Spacing for VBox
        this.tileToggleGroup = new ToggleGroup();
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(0, 10, 0, 0));
        setAlignment(Pos.TOP_CENTER);
        setMinWidth(200);
        setPrefWidth(200);
        setMaxHeight(Double.MAX_VALUE); // Allow this VBox (TilePalette) to grow vertically

        // Add the title
        Label toolsLabel = new Label("Tile Palette");
        toolsLabel.getStyleClass().add("section-title");
        toolsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 5px;");
        getChildren().add(toolsLabel);

        // Create the inner container for the actual palette content
        VBox toolbarContainer = new VBox(5);
        toolbarContainer.setPadding(new Insets(5));
        toolbarContainer.setAlignment(Pos.TOP_CENTER);
        toolbarContainer.getStyleClass().add("tile-palette");

        // Create a ScrollPane to make the palette scrollable
        ScrollPane scrollPane = new ScrollPane(toolbarContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        // scrollPane.setPrefHeight(500); // Remove fixed preferred height

        System.out.println("--- Creating Tile Palette Buttons ---");

        Image staticTileset = Tile.getBaseImageForType(TileType.GRASS); // Get base tileset ref
        if (staticTileset == null) {
            System.err.println("CRITICAL: Failed to load base tileset for palette!");
            // Consider showing an error or using only fallbacks
        }

        // Add tiles in categories with headers
        for (Map.Entry<String, List<TileType>> category : TILE_CATEGORIES.entrySet()) {
            Label categoryLabel = new Label(category.getKey());
            categoryLabel.getStyleClass().add("category-header");
            categoryLabel.setMaxWidth(Double.MAX_VALUE);
            categoryLabel.setAlignment(Pos.CENTER);
            categoryLabel.setStyle(
                    "-fx-font-weight: bold; -fx-background-color: #444444; -fx-text-fill: white; -fx-padding: 3px;");
            toolbarContainer.getChildren().add(categoryLabel);

            TilePane tilePane = new TilePane();
            tilePane.setPadding(new Insets(5));
            tilePane.setHgap(5);
            tilePane.setVgap(5);
            tilePane.setPrefColumns(4);
            tilePane.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE); // Prevent TilePane from growing beyond its
                                                                            // preferred width (based on 4 columns)

            for (TileType type : category.getValue()) {
                ToggleButton tileButton = createTileButton(type, staticTileset);
                tilePane.getChildren().add(tileButton);
            }
            toolbarContainer.getChildren().add(tilePane);
        }

        // Add listener to update the property when selection changes
        tileToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                selectedTileTypeProperty.set(null); // Or default back to GRASS?
            } else {
                selectedTileTypeProperty.set((TileType) newToggle.getUserData());
            }
            System.out.println("Palette selection changed: " + selectedTileTypeProperty.get());
        });

        // Add the scrollpane to this VBox
        getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS); // Make the scrollPane grow vertically within this VBox

        System.out.println("--- Finished Creating Tile Palette Buttons ---");
    }

    /**
     * Programmatically selects the toggle button corresponding to the given
     * TileType.
     * If the type is null or not found, the selection is cleared.
     * 
     * @param type The TileType to select.
     */
    public void selectTile(TileType type) {
        for (javafx.scene.control.Toggle toggle : tileToggleGroup.getToggles()) {
            if (toggle.getUserData() == type) {
                tileToggleGroup.selectToggle(toggle);
                return;
            }
        }
        // If type is null or not found, clear selection
        tileToggleGroup.selectToggle(null);
    }

    public ObjectProperty<TileType> selectedTileTypeProperty() {
        return selectedTileTypeProperty;
    }

    public TileType getSelectedTileType() {
        return selectedTileTypeProperty.get();
    }

    public ToggleGroup getToggleGroup() {
        return tileToggleGroup;
    }

    /**
     * Creates a tile button for the given tile type.
     */
    private ToggleButton createTileButton(TileType type, Image staticTileset) {
        ToggleButton tileButton = new ToggleButton();
        tileButton.setToggleGroup(tileToggleGroup);
        tileButton.setUserData(type);
        tileButton.setTooltip(new Tooltip(getTooltipForTileType(type)));

        tileButton.getStyleClass().add("tile-option");
        tileButton.setMinSize(48, 48);
        tileButton.setPrefSize(48, 48);

        Image imageToShow = null;
        Rectangle2D viewport = null;
        String logInfo = "Type: " + type;
        boolean useFullImage = false;
        boolean useCompositedFallback = false;

        // Special case for START_POINT and END_POINT buttons
        if (type == TileType.START_POINT || type == TileType.END_POINT) {
            try {
                int btnSize = 40;
                Canvas tempCanvas = new Canvas(btnSize, btnSize);
                GraphicsContext g = tempCanvas.getGraphicsContext2D();

                // Use path image for START_POINT, CASTLE image for END_POINT
                if (type == TileType.START_POINT) {
                    // Start point uses a path image with indicator
                    viewport = Tile.getSourceViewportForType(TileType.PATH_HORIZONTAL);

                    if (staticTileset != null && viewport != null) {
                        // Draw path image first
                        g.drawImage(staticTileset,
                                viewport.getMinX(), viewport.getMinY(), viewport.getWidth(), viewport.getHeight(),
                                0, 0, btnSize, btnSize);

                        // Add green direction indicator
                        g.setStroke(Color.GREEN);
                        g.setLineWidth(2);
                        g.strokeRect(2, 2, btnSize - 4, btnSize - 4);

                        // Draw small arrow
                        g.setStroke(Color.GREEN);
                        g.setLineWidth(1.5);
                        double centerX = btnSize / 2;
                        double centerY = btnSize / 2;
                        g.strokeLine(centerX - 8, centerY, centerX + 8, centerY);
                        g.strokeLine(centerX + 8, centerY, centerX + 4, centerY - 4);
                        g.strokeLine(centerX + 8, centerY, centerX + 4, centerY + 4);
                    } else {
                        // Fallback if image loading fails
                        g.setFill(Color.LIGHTGREEN);
                        g.fillRect(0, 0, btnSize, btnSize);
                        g.setFill(Color.DARKGREEN);
                        g.fillText("START", 5, btnSize / 2 + 5);
                    }
                } else { // END_POINT
                    // End point uses castle image with indicator
                    viewport = Tile.getSourceViewportForType(TileType.CASTLE1);

                    if (staticTileset != null && viewport != null) {
                        // Draw castle image first
                        g.drawImage(staticTileset,
                                viewport.getMinX(), viewport.getMinY(), viewport.getWidth(), viewport.getHeight(),
                                0, 0, btnSize, btnSize);

                        // Add red diamond indicator
                        g.setStroke(Color.RED);
                        g.setLineWidth(2);
                        double midX = btnSize / 2;
                        double midY = btnSize / 2;
                        double size = btnSize / 6;

                        g.beginPath();
                        g.moveTo(midX, midY - size);
                        g.lineTo(midX + size, midY);
                        g.lineTo(midX, midY + size);
                        g.lineTo(midX - size, midY);
                        g.closePath();
                        g.stroke();
                    } else {
                        // Fallback if image loading fails
                        g.setFill(Color.LIGHTPINK);
                        g.fillRect(0, 0, btnSize, btnSize);
                        g.setFill(Color.DARKRED);
                        g.fillText("END", 10, btnSize / 2 + 5);
                    }
                }

                // Create the image from the canvas
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                imageToShow = tempCanvas.snapshot(params, null);
                useFullImage = true;
            } catch (Exception e) {
                // Fallback to the old approach if there's an exception
                System.err.println("Error creating special tile button: " + e.getMessage());

                int btnSize = 40;
                Canvas tempCanvas = new Canvas(btnSize, btnSize);
                GraphicsContext g = tempCanvas.getGraphicsContext2D();

                if (type == TileType.START_POINT) {
                    g.setFill(Color.LIGHTGREEN);
                    g.fillRect(0, 0, btnSize, btnSize);
                    g.setFill(Color.DARKGREEN);
                    g.fillText("START", 5, btnSize / 2 + 5);
                } else {
                    g.setFill(Color.LIGHTPINK);
                    g.fillRect(0, 0, btnSize, btnSize);
                    g.setFill(Color.DARKRED);
                    g.fillText("END", 10, btnSize / 2 + 5);
                }

                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                imageToShow = tempCanvas.snapshot(params, null);
                useFullImage = true;
            }
        }
        // --- Special Case for Castle Palette Button (Composite Full Castle onto Grass)
        // ---
        else if (type == TileType.CASTLE1) {
            logInfo += " -> Special Case: Castle Palette Button (Composited)";
            try {
                Image fullCastleImage = Tile.getCastleImage();
                Rectangle2D grassViewport = Tile.getSourceViewportForType(TileType.GRASS);

                if (staticTileset != null && !staticTileset.isError() &&
                        fullCastleImage != null && !fullCastleImage.isError() &&
                        grassViewport != null) {
                    int btnSize = 40;
                    Canvas tempCanvas = new Canvas(btnSize, btnSize); // Use button size for canvas
                    GraphicsContext g = tempCanvas.getGraphicsContext2D();

                    // Draw scaled grass background from tileset
                    g.drawImage(staticTileset,
                            grassViewport.getMinX(), grassViewport.getMinY(), grassViewport.getWidth(),
                            grassViewport.getHeight(), // Source rect
                            0, 0, btnSize, btnSize); // Destination rect (scaled to button)

                    // Draw full castle image on top, scaled
                    g.drawImage(fullCastleImage, 0, 0, btnSize, btnSize);

                    // Snapshot the canvas
                    SnapshotParameters snapParams = new SnapshotParameters();
                    snapParams.setFill(Color.TRANSPARENT);
                    imageToShow = tempCanvas.snapshot(snapParams, null);

                    logInfo += ", Composited Full Castle + GRASS for button";
                    useFullImage = true; // Treat as a full image (no further viewport needed)

                } else {
                    logInfo += ", FAILED to get necessary images/viewports for castle compositing - Using Fallback";
                    imageToShow = null;
                    useCompositedFallback = true;
                }
            } catch (Exception e) {
                logInfo += ", EXCEPTION compositing full castle image: " + e.getMessage();
                e.printStackTrace();
                imageToShow = null;
                useCompositedFallback = true;
            }
        }
        // --- Special Case for Other Overlay Props (Trees, Rocks, Towers, TOWER_SLOT)
        // ---
        else if (isOverlayPropForPalette(type)) { // Helper now includes TOWER_SLOT
            logInfo += " -> Special Case: Overlay Prop Palette Button";
            try {
                Rectangle2D grassViewport = Tile.getSourceViewportForType(TileType.GRASS);
                Rectangle2D propViewport = Tile.getSourceViewportForType(type);

                if (staticTileset != null && !staticTileset.isError() && grassViewport != null
                        && propViewport != null) {
                    // --- Use Canvas for Compositing ---
                    Canvas tempCanvas = new Canvas(Tile.SOURCE_TILE_SIZE, Tile.SOURCE_TILE_SIZE); // Use source size for
                                                                                                  // drawing
                    GraphicsContext g = tempCanvas.getGraphicsContext2D();

                    // Draw grass background from tileset
                    g.drawImage(staticTileset,
                            grassViewport.getMinX(), grassViewport.getMinY(), grassViewport.getWidth(),
                            grassViewport.getHeight(), // Source rect
                            0, 0, Tile.SOURCE_TILE_SIZE, Tile.SOURCE_TILE_SIZE); // Destination rect (original size)

                    // Draw prop layer from tileset on top
                    g.drawImage(staticTileset,
                            propViewport.getMinX(), propViewport.getMinY(), propViewport.getWidth(),
                            propViewport.getHeight(), // Source rect
                            0, 0, Tile.SOURCE_TILE_SIZE, Tile.SOURCE_TILE_SIZE); // Destination rect (original size)

                    // Snapshot the canvas
                    SnapshotParameters snapParams = new SnapshotParameters();
                    snapParams.setFill(Color.TRANSPARENT);
                    imageToShow = tempCanvas.snapshot(snapParams, null); // This is the composited image
                    // --- End Canvas Compositing ---

                    logInfo += ", Composited GRASS + PROP for button";
                    useFullImage = true; // Treat as a full image (no further viewport needed)

                } else {
                    logInfo += ", FAILED to get necessary images/viewports for compositing - Using Fallback";
                    imageToShow = null;
                    useCompositedFallback = true; // Indicate we need the colored fallback
                }
            } catch (Exception e) {
                logInfo += ", EXCEPTION compositing prop image: " + e.getMessage();
                e.printStackTrace();
                imageToShow = null;
                useCompositedFallback = true; // Indicate we need the colored fallback
            }
        }
        // --- Default Logic for Other Tiles (Paths, Grass) ---
        else {
            try {
                Image baseImage = Tile.getBaseImageForType(type);
                String baseImgId = (baseImage == null) ? "NULL"
                        : Integer.toHexString(System.identityHashCode(baseImage));
                logInfo += ", BaseImgID: " + baseImgId;

                if (baseImage != null && !baseImage.isError()) {
                    if (baseImage == staticTileset) { // Is this type derived from the main tileset?
                        viewport = Tile.getSourceViewportForType(type); // Use static helper
                        if (viewport != null) {
                            imageToShow = baseImage; // Show the tileset...
                            logInfo += ", Viewport: Mapped Rect [" + viewport.getMinX() + "," + viewport.getMinY() + " "
                                    + viewport.getWidth() + "x" + viewport.getHeight() + "]";
                        } else {
                            logInfo += ", Viewport: NULL (Rect not found!) - Using Fallback";
                            imageToShow = null;
                        }
                    } else { // Should only be non-atlas images NOT handled above (if any)
                        imageToShow = baseImage; // Show the specific image...
                        logInfo += ", Viewport: N/A (Specific Image)";
                        useFullImage = true;
                    }
                } else {
                    logInfo += ", BaseImg: NULL or Error - Using Fallback";
                    imageToShow = null;
                }
            } catch (Exception e) {
                logInfo += ", EXCEPTION building standard palette button: " + e.getMessage();
                e.printStackTrace();
                imageToShow = null;
            }
        }

        System.out.println("    Palette Button - " + logInfo);

        // --- Set Button Graphic ---
        if (imageToShow != null) {
            ImageView imageView = new ImageView(imageToShow);
            // Apply viewport ONLY if needed (i.e., it's from tileset and not a special
            // full/composited image)
            if (viewport != null && !useFullImage) {
                imageView.setViewport(viewport);
            } else {
                imageView.setViewport(null); // Ensure viewport is null for full/composited images
            }
            // Scale the resulting view
            imageView.setFitWidth(40); // Scale to button size
            imageView.setFitHeight(40);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(false);
            tileButton.setGraphic(imageView);
        } else {
            // Fallback graphic (Use simple color or composited color if needed)
            javafx.scene.shape.Rectangle fallbackRect = new javafx.scene.shape.Rectangle(40, 40);
            Color fallbackColor = useCompositedFallback ? Color.DARKOLIVEGREEN : getColorForTileType(type); // Simple
                                                                                                            // color or
                                                                                                            // blended
                                                                                                            // indication
            fallbackRect.setFill(fallbackColor);

            tileButton.setGraphic(fallbackRect); // Just use the colored rectangle for now
            System.err.println("    Palette: Using fallback graphic for tile button: " + type);
        }

        // Select the initial tile type
        if (type == selectedTileTypeProperty.get()) {
            tileButton.setSelected(true);
        }

        return tileButton;
    }

    /**
     * Helper to determine if a tile type is a prop for the palette rendering.
     */
    private boolean isOverlayPropForPalette(TileType type) {
        return switch (type) {
            case TREE_BIG, TREE_MEDIUM, TREE_SMALL,
                    ROCK_SMALL, ROCK_MEDIUM,
                    HOUSE, WELL, LOG_PILE,
                    TOWER_ARTILLERY, TOWER_MAGE, ARCHER_TOWER, TOWER_BARACK,
                    TOWER_SLOT ->
                true; // Added TOWER_SLOT here
            default -> false;
        };
    }

    /**
     * Get formatted tooltip text for a tile type.
     */
    private String getTooltipForTileType(TileType type) {
        return switch (type) {
            case GRASS -> "Grass (Base terrain tile)";
            case PATH_HORIZONTAL -> "Horizontal Path";
            case PATH_VERTICAL -> "Vertical Path";
            case PATH_CIRCLE_NW -> "Path Corner (North-West)";
            case PATH_CIRCLE_N -> "Path End (North)";
            case PATH_CIRCLE_NE -> "Path Corner (North-East)";
            case PATH_CIRCLE_E -> "Path End (East)";
            case PATH_CIRCLE_SE -> "Path Corner (South-East)";
            case PATH_CIRCLE_S -> "Path End (South)";
            case PATH_CIRCLE_SW -> "Path Corner (South-West)";
            case PATH_CIRCLE_W -> "Path End (West)";
            case PATH_VERTICAL_N_DE -> "Vertical Path Dead End (North)";
            case PATH_VERTICAL_S_DE -> "Vertical Path Dead End (South)";
            case PATH_HORIZONTAL_W_DE -> "Horizontal Path Dead End (West)";
            case PATH_HORIZONTAL_E_DE -> "Horizontal Path Dead End (East)";
            case TOWER_SLOT -> "Tower Slot (Place towers here)";
            case CASTLE1 -> "Castle Base (2x2 structure, also sets END_POINT)";
            case TREE_BIG -> "Large Tree";
            case TREE_MEDIUM -> "Medium Tree";
            case TREE_SMALL -> "Small Tree";
            case HOUSE -> "House";
            case WELL -> "Well";
            case LOG_PILE -> "Log Pile";
            case ROCK_MEDIUM -> "Medium Rock";
            case ROCK_SMALL -> "Small Rock";
            case TOWER_ARTILLERY -> "Artillery Tower (Visual)";
            case TOWER_MAGE -> "Mage Tower (Visual)";
            case ARCHER_TOWER -> "Archer Tower (Visual)";
            case TOWER_BARACK -> "Barracks (Visual)";
            case START_POINT -> "Start Point (REQUIRED - Place on the map edge where enemies should spawn)";
            case END_POINT -> "End Point (REQUIRED - Usually placed with Castle, represents enemy target)";
            default -> type.toString();
        };
    }

    // Helper method to get fallback colors
    private Color getColorForTileType(TileType type) {
        return switch (type) {
            case GRASS -> Color.rgb(100, 180, 100);
            case PATH_HORIZONTAL, PATH_VERTICAL, PATH_CIRCLE_NW, PATH_CIRCLE_N, PATH_CIRCLE_NE, PATH_CIRCLE_E,
                    PATH_CIRCLE_SE, PATH_CIRCLE_S, PATH_CIRCLE_SW, PATH_CIRCLE_W, PATH_VERTICAL_N_DE,
                    PATH_VERTICAL_S_DE, PATH_HORIZONTAL_W_DE, PATH_HORIZONTAL_E_DE ->
                Color.rgb(210, 180, 140);
            case TOWER_SLOT -> Color.rgb(100, 100, 100);
            case TREE_BIG, TREE_MEDIUM, TREE_SMALL -> Color.rgb(0, 100, 0);
            case HOUSE -> Color.rgb(139, 69, 19);
            case WELL -> Color.rgb(70, 130, 180);
            case LOG_PILE -> Color.rgb(160, 82, 45);
            case ROCK_MEDIUM, ROCK_SMALL -> Color.rgb(128, 128, 128);
            case TOWER_ARTILLERY, TOWER_MAGE, ARCHER_TOWER, TOWER_BARACK -> Color.rgb(128, 0, 128);
            case START_POINT -> Color.DODGERBLUE;
            case END_POINT -> Color.INDIANRED;
            case CASTLE1, CASTLE2, CASTLE3, CASTLE4 -> Color.rgb(192, 192, 192);
            default -> Color.PINK;
        };
    }
}