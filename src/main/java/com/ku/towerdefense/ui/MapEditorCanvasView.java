package com.ku.towerdefense.ui;

import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.Tile;
import com.ku.towerdefense.model.map.TileType;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent; // Import MouseEvent
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Manages the map canvas, rendering, zoom, and placement logic for the Map
 * Editor.
 */
public class MapEditorCanvasView extends VBox {

    private GameMap gameMap;
    private final Canvas mapCanvas;
    private final ScrollPane mapScrollPane;
    private final Label zoomLabel;
    private final MapEditorTilePalette tilePalette; // Need access to selected tile

    private int tileSize = 64; // Keep tileSize management here
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 2.0;
    private static final double ZOOM_STEP = 0.1;

    // Keep track of the current click handler mode
    private enum ClickMode {
        PALETTE, SET_START
    }

    private ClickMode currentClickMode = ClickMode.PALETTE;

    public MapEditorCanvasView(GameMap initialMap, MapEditorTilePalette palette) {
        super(10); // Spacing for VBox
        this.gameMap = initialMap;
        this.tilePalette = palette;

        setAlignment(Pos.CENTER);

        // Create zoom controls
        HBox zoomControls = createZoomControls();
        this.zoomLabel = (Label) zoomControls.getChildren().get(1); // Store label reference

        // Create canvas and group
        mapCanvas = new Canvas(gameMap.getWidth() * tileSize, gameMap.getHeight() * tileSize);
        Group canvasGroup = new Group(mapCanvas);

        // Wrap in ScrollPane
        mapScrollPane = new ScrollPane();
        mapScrollPane.setContent(canvasGroup);
        mapScrollPane.setFitToWidth(true);
        mapScrollPane.setFitToHeight(true);
        mapScrollPane.setStyle("-fx-background: #64B464; -fx-border-color: #444444;");

        getChildren().addAll(zoomControls, mapScrollPane);

        // Initial setup
        applyZoom();
        resetPlacementMode(); // Set initial click handler
        renderMap();
    }

    /**
     * Sets a new GameMap instance for the canvas view.
     * Updates the canvas size and triggers a re-render.
     * 
     * @param newMap The new GameMap object to display.
     */
    public void setGameMap(GameMap newMap) {
        System.out.println("--- CanvasView.setGameMap() called ---");
        this.gameMap = newMap;
        // Update canvas size based on the new map
        updateCanvasSize(); // This calls renderMap() internally
        // Update scrollpane viewport (important after map/canvas size change)
        // We need the available space from the parent for accurate sizing
        // For now, just use the current size as a placeholder
        updateScrollPaneSize(this.getWidth(), this.getHeight());
        System.out.println("--- CanvasView.setGameMap() finished ---");
    }

    /**
     * Adjusts the scrollpane size based on available dimensions.
     * Needs parent dimensions, so might be better called from MapEditorScreen after
     * layout.
     */
    public void updateScrollPaneSize(double availableWidth, double availableHeight) {
        mapScrollPane.setPrefViewportWidth(Math.min(availableWidth, gameMap.getWidth() * tileSize * zoomLevel));
        mapScrollPane.setPrefViewportHeight(Math.min(availableHeight, gameMap.getHeight() * tileSize * zoomLevel));
    }

    /**
     * Sets the canvas size when the map is resized.
     */
    public void updateCanvasSize() {
        mapCanvas.setWidth(gameMap.getWidth() * tileSize);
        mapCanvas.setHeight(gameMap.getHeight() * tileSize);
        applyZoom(); // Re-apply zoom/scale
        renderMap();
    }

    // --- Zoom Logic ---

    private HBox createZoomControls() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        Button zoomOutButton = new Button("-");
        zoomOutButton.setOnAction(e -> zoomChange(-ZOOM_STEP));
        Label label = new Label(); // Temporary label, stored in field
        label.setTextFill(Color.WHITE);
        Button zoomInButton = new Button("+");
        zoomInButton.setOnAction(e -> zoomChange(ZOOM_STEP));
        Button resetZoomButton = new Button("Reset Zoom");
        resetZoomButton.setOnAction(e -> {
            zoomLevel = 1.0;
            applyZoom();
        });
        controls.getChildren().addAll(zoomOutButton, label, zoomInButton, resetZoomButton);
        return controls;
    }

    private void zoomChange(double delta) {
        double newZoom = zoomLevel + delta;
        if (newZoom >= MIN_ZOOM && newZoom <= MAX_ZOOM) {
            zoomLevel = newZoom;
            applyZoom();
        }
    }

    private void applyZoom() {
        mapCanvas.getTransforms().clear();
        mapCanvas.getTransforms().add(new Scale(zoomLevel, zoomLevel));
        zoomLabel.setText(String.format("Zoom: %.0f%%", zoomLevel * 100));
        // Adjust scrollpane view based on new canvas scale
        // This requires knowing available space, better handled by calling
        // updateScrollPaneSize from parent
    }

    // --- Rendering ---

    public void renderMap() {
        System.out.println("--- CanvasView.renderMap() called ---");
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        for (int y = 0; y < gameMap.getHeight(); y++) {
            for (int x = 0; x < gameMap.getWidth(); x++) {
                Tile tile = gameMap.getTile(x, y);
                if (tile != null) {
                    tile.render(gc, tileSize);
                }
            }
        }

        // Draw grid overlay
        gc.setStroke(Color.rgb(0, 0, 0, 0.2));
        gc.setLineWidth(0.5);
        for (int x = 0; x <= gameMap.getWidth(); x++) {
            gc.strokeLine(x * tileSize, 0, x * tileSize, gameMap.getHeight() * tileSize);
        }
        for (int y = 0; y <= gameMap.getHeight(); y++) {
            gc.strokeLine(0, y * tileSize, gameMap.getWidth() * tileSize, y * tileSize);
        }
        System.out.println("--- CanvasView.renderMap() finished ---");
    }

    // --- Placement Logic ---

    public void activateSetStartMode() {
        currentClickMode = ClickMode.SET_START;
        // Deselect palette toggle (optional, but good UX)
        if (tilePalette.getToggleGroup().getSelectedToggle() != null) {
            tilePalette.getToggleGroup().getSelectedToggle().setSelected(false);
        }
        System.out.println("CanvasView: Set Start mode activated. Click on the map edge.");
        mapCanvas.setOnMouseClicked(this::handleCanvasClick);
    }

    // Reset placement mode back to using the palette selection
    // Made public to be called after loading a map
    public void resetPlacementMode() {
        currentClickMode = ClickMode.PALETTE;
        mapCanvas.setOnMouseClicked(this::handleCanvasClick);
        TileType currentSelection = tilePalette.getSelectedTileType();
        System.out.println("CanvasView: Placement mode reset to palette selection. Current: " + currentSelection);
    }

    // Single click handler routes based on mode
    private void handleCanvasClick(MouseEvent e) {
        if (currentClickMode == ClickMode.SET_START) {
            handleSpecialPlacementClick(e, TileType.START_POINT);
            resetPlacementMode(); // Automatically switch back after attempting placement
        } else {
            handlePaletteClick(e);
        }
    }

    // Handles clicks when in standard palette mode
    private void handlePaletteClick(MouseEvent e) {
        double zoomedX = e.getX() / zoomLevel;
        double zoomedY = e.getY() / zoomLevel;
        int x = (int) (zoomedX / tileSize);
        int y = (int) (zoomedY / tileSize);

        TileType typeToPlace = tilePalette.getSelectedTileType();

        if (typeToPlace == null)
            return; // Nothing selected
        if (x < 0 || x >= gameMap.getWidth() || y < 0 || y >= gameMap.getHeight())
            return; // Out of bounds

        if (typeToPlace == TileType.CASTLE1) {
            handleCastlePlacement(x, y);
        } else {
            handleSingleTilePlacement(x, y, typeToPlace);
        }
        renderMap();
    }

    // Handles clicks ONLY when in Set Start mode
    private void handleSpecialPlacementClick(MouseEvent e, TileType typeToPlace) {
        if (typeToPlace != TileType.START_POINT) {
            System.err.println("Error: handleSpecialPlacementClick called for non-START_POINT");
            return;
        }

        double zoomedX = e.getX() / zoomLevel;
        double zoomedY = e.getY() / zoomLevel;
        int x = (int) (zoomedX / tileSize);
        int y = (int) (zoomedY / tileSize);

        if (x < 0 || x >= gameMap.getWidth() || y < 0 || y >= gameMap.getHeight()) {
            showAlert("Invalid Placement", "Placement out of bounds.");
            return;
        }

        Tile targetTile = gameMap.getTile(x, y);
        if (targetTile != null && targetTile.getType() != TileType.GRASS && !targetTile.isWalkable()) {
            if (targetTile.getType() == TileType.END_POINT || targetTile.getType() == TileType.CASTLE1
                    || targetTile.getType() == TileType.CASTLE2 || targetTile.getType() == TileType.CASTLE3
                    || targetTile.getType() == TileType.CASTLE4) {
                showAlert("Invalid Placement", "Start Point cannot overlap the Castle/End Point.");
            } else {
                showAlert("Invalid Placement", "Start Point can only be placed on Grass or Path tiles.");
            }
            return;
        }

        clearExistingStartPoint();
        gameMap.setTileType(x, y, TileType.START_POINT);
        System.out.println("Placed Start Point at (" + x + "," + y + ")");
        renderMap();
    }

    private void handleSingleTilePlacement(int x, int y, TileType typeToPlace) {
        if (typeToPlace == TileType.START_POINT || typeToPlace == TileType.END_POINT ||
                typeToPlace == TileType.CASTLE1 || typeToPlace == TileType.CASTLE2 ||
                typeToPlace == TileType.CASTLE3 || typeToPlace == TileType.CASTLE4) {
            return; // Should not be placed via this method
        }

        Tile existingTile = gameMap.getTile(x, y);
        if (existingTile != null) {
            TileType existingType = existingTile.getType();
            if (existingType == TileType.START_POINT || existingType == TileType.END_POINT ||
                    existingType == TileType.CASTLE1 || existingType == TileType.CASTLE2 ||
                    existingType == TileType.CASTLE3 || existingType == TileType.CASTLE4) {
                if (typeToPlace != TileType.GRASS) {
                    showAlert("Action Needed", "Cannot overwrite " + existingType + ". Place GRASS first.");
                    return;
                } else {
                    if (existingType == TileType.START_POINT)
                        clearExistingStartPoint();
                    if (existingType == TileType.END_POINT || existingType == TileType.CASTLE1 ||
                            existingType == TileType.CASTLE2 || existingType == TileType.CASTLE3 ||
                            existingType == TileType.CASTLE4) {
                        clearExistingEndPoint();
                        clearCastleRemnants(x, y, existingType); // Still need remnant clearing
                    }
                }
            }
        }
        gameMap.setTileType(x, y, typeToPlace);
        System.out.println("Placed single tile: " + typeToPlace + " at (" + x + "," + y + ")");
    }

    private void handleCastlePlacement(int x, int y) {
        int x1 = x, y1 = y;
        int x2 = x + 1, y2 = y;
        int x3 = x, y3 = y + 1;
        int x4 = x + 1, y4 = y + 1;

        if (x2 >= gameMap.getWidth() || y3 >= gameMap.getHeight()) {
            showAlert("Invalid Placement", "Castle placement out of bounds.");
            return;
        }

        // Check if castle is at edge
        boolean isAtEdge = (x1 == 0 || x2 == gameMap.getWidth() - 1 || y1 == 0 || y3 == gameMap.getHeight() - 1);
        if (!isAtEdge) {
            showAlert("Invalid Placement", "Castle must be placed at the edge of the map.");
            return;
        }

        TileType[] targetTypes = { gameMap.getTileType(x1, y1), gameMap.getTileType(x2, y2),
                gameMap.getTileType(x3, y3), gameMap.getTileType(x4, y4) };
        for (TileType type : targetTypes) {
            if (type != TileType.GRASS) {
                if (type == TileType.START_POINT) {
                    showAlert("Invalid Placement", "Cannot place castle overlapping Start Point.");
                } else {
                    showAlert("Invalid Placement", "Castle must be placed on 2x2 Grass.");
                }
                return;
            }
        }
        clearExistingEndPoint();
        gameMap.setTileType(x1, y1, TileType.CASTLE1);
        gameMap.setTileType(x2, y2, TileType.CASTLE2);
        gameMap.setTileType(x3, y3, TileType.CASTLE3);
        gameMap.setTileType(x4, y4, TileType.CASTLE4);
        gameMap.setTileType(x1, y1, TileType.END_POINT);
        System.out.println("Placed 2x2 Castle and logical END_POINT at (" + x1 + "," + y1 + ")");
    }

    // --- Clearing Logic (Moved from MapEditorScreen) ---

    private void clearExistingStartPoint() {
        for (int x = 0; x < gameMap.getWidth(); x++) {
            for (int y = 0; y < gameMap.getHeight(); y++) {
                if (gameMap.getTileType(x, y) == TileType.START_POINT) {
                    gameMap.setTileType(x, y, TileType.GRASS);
                    System.out.println("Cleared existing Start Point at (" + x + "," + y + ")");
                    return;
                }
            }
        }
    }

    private void clearExistingEndPoint() {
        for (int x = 0; x < gameMap.getWidth(); x++) {
            for (int y = 0; y < gameMap.getHeight(); y++) {
                if (gameMap.getTileType(x, y) == TileType.END_POINT) {
                    System.out.println("Clearing existing End Point / Castle structure at (" + x + "," + y + ")");
                    int tx = x, ty = y;
                    gameMap.setTileType(tx, ty, TileType.GRASS);
                    if (tx + 1 < gameMap.getWidth())
                        gameMap.setTileType(tx + 1, ty, TileType.GRASS);
                    if (ty + 1 < gameMap.getHeight())
                        gameMap.setTileType(tx, ty + 1, TileType.GRASS);
                    if (tx + 1 < gameMap.getWidth() && ty + 1 < gameMap.getHeight())
                        gameMap.setTileType(tx + 1, ty + 1, TileType.GRASS);
                    return;
                }
            }
        }
    }

    private void clearCastleRemnants(int x, int y, TileType overwrittenType) {
        int topLeftX = x, topLeftY = y;
        if (overwrittenType == TileType.CASTLE2)
            topLeftX = x - 1;
        else if (overwrittenType == TileType.CASTLE3)
            topLeftY = y - 1;
        else if (overwrittenType == TileType.CASTLE4) {
            topLeftX = x - 1;
            topLeftY = y - 1;
        } else if (overwrittenType == TileType.END_POINT) {
            /* Already top-left */ }

        int[] xs = { topLeftX, topLeftX + 1, topLeftX, topLeftX + 1 };
        int[] ys = { topLeftY, topLeftY, topLeftY + 1, topLeftY + 1 };

        for (int i = 0; i < 4; i++) {
            if (xs[i] == x && ys[i] == y)
                continue;
            if (xs[i] >= 0 && xs[i] < gameMap.getWidth() && ys[i] >= 0 && ys[i] < gameMap.getHeight()) {
                TileType type = gameMap.getTileType(xs[i], ys[i]);
                if (type == TileType.CASTLE1 || type == TileType.CASTLE2 ||
                        type == TileType.CASTLE3 || type == TileType.CASTLE4 ||
                        type == TileType.END_POINT) {
                    gameMap.setTileType(xs[i], ys[i], TileType.GRASS);
                }
            }
        }
        System.out.println("Cleared castle remnants around (" + x + "," + y + ")");
    }

    // Simple alert helper
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}