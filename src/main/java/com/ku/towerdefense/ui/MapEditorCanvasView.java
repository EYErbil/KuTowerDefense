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
        if (gameMap == null) return;

        int mapWidth = gameMap.getWidth();
        int mapHeight = gameMap.getHeight();
        int pixelWidth = mapWidth * tileSize;
        int pixelHeight = mapHeight * tileSize;

        // Calculate the enlarged size needed with zoom
        double zoomedWidth = pixelWidth * zoomLevel;
        double zoomedHeight = pixelHeight * zoomLevel;

        // Resize canvas if needed to fit the zoomed map
        mapCanvas.setWidth(zoomedWidth);
        mapCanvas.setHeight(zoomedHeight);

        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, zoomedWidth, zoomedHeight);

        // Apply zoom transformation
        gc.save();
        gc.scale(zoomLevel, zoomLevel);

        // Draw the map tiles
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                Tile tile = gameMap.getTile(x, y);
                if (tile != null) {
                    tile.render(gc, tileSize);
                }
            }
        }
        
        // Add visual indicators for START_POINT and END_POINT
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                Tile tile = gameMap.getTile(x, y);
                if (tile != null) {
                    if (tile.getType() == TileType.START_POINT) {
                        // Subtle green border indicator for START_POINT
                        gc.setStroke(Color.GREEN);
                        gc.setLineWidth(2);
                        gc.strokeRect(x * tileSize + 2, y * tileSize + 2, tileSize - 4, tileSize - 4);
                        
                        // Draw direction arrow based on position (but smaller and more subtle)
                        drawDirectionArrow(gc, x, y, mapWidth, mapHeight);
                    } else if (tile.getType() == TileType.END_POINT) {
                        // Subtle red diamond indicator for END_POINT
                        double midX = x * tileSize + tileSize / 2;
                        double midY = y * tileSize + tileSize / 2;
                        double size = tileSize / 4;
                        
                        gc.setStroke(Color.RED);
                        gc.setLineWidth(2);
                        
                        // Draw a diamond
                        gc.beginPath();
                        gc.moveTo(midX, midY - size);  // Top
                        gc.lineTo(midX + size, midY);  // Right
                        gc.lineTo(midX, midY + size);  // Bottom
                        gc.lineTo(midX - size, midY);  // Left
                        gc.closePath();
                        gc.stroke();
                    }
                }
            }
        }

        // Draw grid lines
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        for (int x = 0; x <= mapWidth; x++) {
            gc.strokeLine(x * tileSize, 0, x * tileSize, mapHeight * tileSize);
        }
        for (int y = 0; y <= mapHeight; y++) {
            gc.strokeLine(0, y * tileSize, mapWidth * tileSize, y * tileSize);
        }

        // Restore transformation
        gc.restore();
    }
    
    /**
     * Draw a directional arrow from the START_POINT toward the map center
     */
    private void drawDirectionArrow(GraphicsContext gc, int x, int y, int mapWidth, int mapHeight) {
        double centerX = x * tileSize + tileSize/2;
        double centerY = y * tileSize + tileSize/2;
        double arrowLength = tileSize/4; // Make arrow shorter
        
        // Determine arrow direction based on edge position
        double dirX = 0, dirY = 0;
        
        if (x == 0) dirX = 1;
        else if (x == mapWidth-1) dirX = -1;
        
        if (y == 0) dirY = 1;
        else if (y == mapHeight-1) dirY = -1;
        
        // If at a corner, point diagonally inward
        if (dirX != 0 && dirY != 0) {
            double length = Math.sqrt(dirX*dirX + dirY*dirY);
            dirX /= length;
            dirY /= length;
        }
        
        // Calculate arrow end points
        double endX = centerX + dirX * arrowLength;
        double endY = centerY + dirY * arrowLength;
        
        // Draw arrow line
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(1.5); // Thinner line
        gc.strokeLine(centerX, centerY, endX, endY);
        
        // Draw arrowhead
        double headSize = tileSize/8; // Smaller arrowhead
        double angle = Math.atan2(dirY, dirX);
        double angle1 = angle - Math.PI/4;
        double angle2 = angle + Math.PI/4;
        
        double head1X = endX - headSize * Math.cos(angle1);
        double head1Y = endY - headSize * Math.sin(angle1);
        double head2X = endX - headSize * Math.cos(angle2);
        double head2Y = endY - headSize * Math.sin(angle2);
        
        gc.strokeLine(endX, endY, head1X, head1Y);
        gc.strokeLine(endX, endY, head2X, head2Y);
    }

    // --- Placement Logic ---

    public void activateSetStartMode(){
        currentClickMode=ClickMode.SET_START;
        if(tilePalette.getToggleGroup().getSelectedToggle()!=null)
            tilePalette.getToggleGroup().getSelectedToggle().setSelected(false);
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
        } else if (typeToPlace == TileType.START_POINT) {
            handleStartPointPlacement(x, y);
        } else if (typeToPlace == TileType.END_POINT) {
            handleEndPointPlacement(x, y);
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
        boolean onEdge = (x == 0 || x == gameMap.getWidth() - 1 || y == 0 || y == gameMap.getHeight() - 1);
        if (!onEdge) {
            showAlert("Invalid Placement", "Start Point must be placed on the edge.");
            return;
        }
        Tile targetTile = gameMap.getTile(x, y);
        if (targetTile != null && targetTile.getType() != TileType.GRASS && !targetTile.isWalkable()) {
            if (targetTile.getType() == TileType.END_POINT || targetTile.getType() == TileType.CASTLE1
                    || /* ... */ targetTile.getType() == TileType.CASTLE4) {
                showAlert("Invalid Placement", "Start Point cannot overlap the Castle/End Point.");
            } else {
                showAlert("Invalid Placement", "Start Point can only be placed on Grass or Path tiles on the edge.");
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

    private void handleCastlePlacement(int x,int y){
        if(x+1>=gameMap.getWidth() || y+1>=gameMap.getHeight()){
            showAlert("Invalid placement","Castle must fit entirely on the map.");
            return;
        }
        // validate 2×2 grass
        for(int dx=0;dx<=1;dx++) for(int dy=0;dy<=1;dy++)
            if(gameMap.getTileType(x+dx,y+dy)!=TileType.GRASS){
                showAlert("Invalid placement","Castle must be placed on 2×2 grass.");
                return;
            }
        clearExistingEndPoint();
        gameMap.setTileType(x,  y,  TileType.CASTLE1);
        gameMap.setTileType(x+1,y,  TileType.CASTLE2);
        gameMap.setTileType(x,  y+1,TileType.CASTLE3);
        gameMap.setTileType(x+1,y+1,TileType.CASTLE4);

        // choose outward‑facing End tile
        int ex=x, ey=y;
        if(x==0)                           ex = x-1;
        else if(x+2==gameMap.getWidth())   ex = x+2;
        else if(y==0)                      ey = y-1;
        else if(y+2==gameMap.getHeight())  ey = y+2;
        else                               ey = y+2; // default bottom
        if(ex>=0 && ex<gameMap.getWidth() && ey>=0 && ey<gameMap.getHeight())
            gameMap.setTileType(ex,ey,TileType.END_POINT);
        System.out.printf("Castle @(%d,%d) – End @(%d,%d)%n",x,y,ex,ey);
        renderMap();
    }

    /**
     * Handle the placement of a START_POINT tile
     */
    private void handleStartPointPlacement(int x, int y) {
        // Validate edge placement
        boolean onEdge = (x == 0 || x == gameMap.getWidth() - 1 || y == 0 || y == gameMap.getHeight() - 1);
        if (!onEdge) {
            showAlert("Invalid Placement", "Start Point must be placed on the edge of the map.");
            return;
        }
        
        // Validate not on castle
        Tile targetTile = gameMap.getTile(x, y);
        if (targetTile != null && (targetTile.getType() == TileType.END_POINT || 
                                  targetTile.getType() == TileType.CASTLE1 ||
                                  targetTile.getType() == TileType.CASTLE2 ||
                                  targetTile.getType() == TileType.CASTLE3 ||
                                  targetTile.getType() == TileType.CASTLE4)) {
            showAlert("Invalid Placement", "Start Point cannot overlap with the Castle/End Point.");
            return;
        }
        
        // Clear any existing start point
        clearExistingStartPoint();
        
        // Place the new start point
        gameMap.setTileType(x, y, TileType.START_POINT);
        System.out.println("Placed Start Point at (" + x + "," + y + ")");
    }
    
    /**
     * Handle the placement of an END_POINT tile
     */
    private void handleEndPointPlacement(int x, int y) {
        // END_POINT is always placed with a castle structure, so we just redirect to castle placement
        handleCastlePlacement(x, y);
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