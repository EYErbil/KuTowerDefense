package com.ku.towerdefense.ui;

import com.ku.towerdefense.controller.GameController;
import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.Tile;
import com.ku.towerdefense.model.map.TileType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.awt.Point;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import java.io.*;

/**
 * Screen for creating and editing game maps.
 */
public class MapEditorScreen extends BorderPane {
    private final Stage primaryStage;
    private GameMap currentMap;
    private File mapsDirectory;
    private MapEditorTilePalette tilePalette;
    private MapEditorTopToolbar topToolbar;
    private MapEditorCanvasView canvasView;

    // Default and minimum window dimensions
    private static final double MIN_WINDOW_WIDTH = 800;
    private static final double MIN_WINDOW_HEIGHT = 600;

    public MapEditorScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.currentMap = new GameMap("New Map", 20, 15);
        initializeMapsDirectory();
        initializeUI();
        setupBindings();
    }

    private void initializeMapsDirectory() {
        String userHome = System.getProperty("user.home");
        mapsDirectory = new File(userHome, "KUTowerDefenseMaps");
        if (!mapsDirectory.exists()) {
            if (mapsDirectory.mkdirs()) {
                System.out.println("Maps directory created at: " + mapsDirectory.getAbsolutePath());
            } else {
                System.err.println("Failed to create maps directory. Saving/Loading disabled.");
                mapsDirectory = null;
            }
        } else {
            System.out.println("Using existing maps directory: " + mapsDirectory.getAbsolutePath());
        }
    }

    private void initializeUI() {
        setStyle("-fx-background-color: #333333;");
        setPadding(new Insets(10));

        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setResizable(true);

        topToolbar = new MapEditorTopToolbar(currentMap);
        tilePalette = new MapEditorTilePalette();
        canvasView = new MapEditorCanvasView(currentMap, tilePalette);

        HBox bottomToolbar = createBottomToolbar();

        setTop(topToolbar);
        setLeft(tilePalette);
        setCenter(canvasView);
        setBottom(bottomToolbar);

        primaryStage.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        ((Stage) newWindow).widthProperty().addListener((obs, oldVal,
                                newVal) -> javafx.application.Platform.runLater(this::handleWindowResize));
                        ((Stage) newWindow).heightProperty().addListener((obs, oldVal,
                                newVal) -> javafx.application.Platform.runLater(this::handleWindowResize));
                        ((Stage) newWindow)
                                .setOnShown(e -> javafx.application.Platform.runLater(this::handleWindowResize));
                    }
                });
            }
        });
    }

    private void setupBindings() {
        topToolbar.setOnSetStart(e -> {
            System.out.println("Set Start mode triggered from toolbar");
            canvasView.activateSetStartMode();
        });

        topToolbar.setOnResize(e -> {
            System.out.println("Resize requested: " + e.getNewWidth() + "x" + e.getNewHeight());
            resizeMap(e.getNewWidth(), e.getNewHeight());
        });
    }

    private void handleWindowResize() {
        if (primaryStage.getScene() == null || getScene().getRoot() == null)
            return;

        double leftWidth = (tilePalette != null && tilePalette.isVisible()) ? tilePalette.getWidth() : 0;
        double topHeight = (topToolbar != null && topToolbar.isVisible()) ? topToolbar.getHeight() : 0;
        double bottomHeight = 50;

        double sceneWidth = getScene() != null ? getScene().getWidth() : primaryStage.getWidth();
        double sceneHeight = getScene() != null ? getScene().getHeight() : primaryStage.getHeight();

        double availableWidth = sceneWidth - leftWidth - getPadding().getLeft() - getPadding().getRight();
        double availableHeight = sceneHeight - topHeight - bottomHeight - getPadding().getTop()
                - getPadding().getBottom();

        if (canvasView != null) {
            canvasView.updateScrollPaneSize(availableWidth, availableHeight);
        }
    }

    private HBox createBottomToolbar() {
        HBox bottomToolbar = new HBox(10);
        bottomToolbar.setPadding(new Insets(10));
        bottomToolbar.setStyle("-fx-background-color: #444444; -fx-border-color: #555555; -fx-border-width: 1 0 0 0;");

        Button saveButton = new Button("Save Map");
        saveButton.setOnAction(e -> saveMap());

        Button loadButton = new Button("Load Map");
        loadButton.setOnAction(e -> loadMap());

        Button validateButton = new Button("Validate Map");
        validateButton.setOnAction(e -> validateMap());

        Button helpButton = new Button("Help");
        helpButton.setOnAction(e -> showGameMechanicsHelp());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("Back to Main Menu");
        backButton.setOnAction(e -> goBack());

        bottomToolbar.getChildren().addAll(saveButton, loadButton, validateButton, helpButton, spacer, backButton);
        return bottomToolbar;
    }

    private void showGameMechanicsHelp() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Map Editor Help");
        helpAlert.setHeaderText("Map Requirements & Editor Usage");

        TextArea textArea = new TextArea(
                "Map Requirements:\n" +
                        "- Must have exactly one Start Point (Path Start).\n" +
                        "- Must have exactly one End Point (Castle base - bottom-left tile).\n" +
                        "- Start and End points must be the only points connected to the edge of the map through walkable tiles.\n" +
                        "- End Point must be represented by a full 2x2 Castle structure placed on Grass.\n" +
                        "- There must be a valid path (using Path tiles) from the Start Point to a tile adjacent to the End Point (Castle).\n" +
                        "- The path must be walkable (Path tiles).\n" +
                        "- Tower Slots can only be placed on Grass tiles.\n" +
                        "- Map must have at least 4 tower slots.\n\n" +
                        "Editor Usage:\n" +
                        "- Select a tile from the left palette.\n" +
                        "- Click on the canvas to place the selected tile.\n" +
                        "- Use the 'Set Start' button in the top toolbar, then click on a tile to place the Start Point.\n" +
                        "- To place the Castle (End Point), select the Castle icon (bottom-left part) and click on the desired top-left Grass tile for the 2x2 structure.\n" +
                        "- Use zoom controls (+/-) or Reset Zoom.\n" +
                        "- Use 'Resize Map' in the top toolbar to change map dimensions (clears Start/End/Castle).\n" +
                        "- Save/Load maps using the buttons below.\n" +
                        "- Validate Map checks if the current map meets all requirements.");
        textArea.setEditable(false);
        textArea.setWrapText(true);

        helpAlert.getDialogPane().setContent(textArea);
        helpAlert.setResizable(true);
        helpAlert.showAndWait();
    }

    private boolean hasDisconnectedTiles() {
        boolean[][] visited = new boolean[currentMap.getWidth()][currentMap.getHeight()];
        List<Point> pathTiles = new ArrayList<>();

        // First, find all path tiles
        for (int y = 0; y < currentMap.getHeight(); y++) {
            for (int x = 0; x < currentMap.getWidth(); x++) {
                TileType type = currentMap.getTileType(x, y);
                if (type == TileType.PATH_HORIZONTAL || type == TileType.PATH_VERTICAL ||
                    type == TileType.PATH_CIRCLE_N || type == TileType.PATH_CIRCLE_S ||
                    type == TileType.PATH_CIRCLE_W || type == TileType.PATH_CIRCLE_E ||
                    type == TileType.PATH_CIRCLE_NW || type == TileType.PATH_CIRCLE_NE ||
                    type == TileType.PATH_CIRCLE_SW || type == TileType.PATH_CIRCLE_SE ||
                    type == TileType.START_POINT) {
                    pathTiles.add(new Point(x, y));
                }
            }
        }

        if (pathTiles.isEmpty()) {
            return false; // No path tiles means no disconnected ones
        }

        // Start BFS from the first path tile
        List<Point> queue = new ArrayList<>();
        Point start = pathTiles.get(0);
        queue.add(start);
        visited[start.x][start.y] = true;
        int connectedCount = 1;

        // BFS to find all connected path tiles
        while (!queue.isEmpty()) {
            Point current = queue.remove(0);
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Left, Right, Up, Down

            for (int[] dir : directions) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                if (nx >= 0 && nx < currentMap.getWidth() && ny >= 0 && ny < currentMap.getHeight() && !visited[nx][ny]) {
                    TileType type = currentMap.getTileType(nx, ny);
                    if (type == TileType.PATH_HORIZONTAL || type == TileType.PATH_VERTICAL ||
                        type == TileType.PATH_CIRCLE_N || type == TileType.PATH_CIRCLE_S ||
                        type == TileType.PATH_CIRCLE_W || type == TileType.PATH_CIRCLE_E ||
                        type == TileType.PATH_CIRCLE_NW || type == TileType.PATH_CIRCLE_NE ||
                        type == TileType.PATH_CIRCLE_SW || type == TileType.PATH_CIRCLE_SE ||
                        type == TileType.START_POINT) {
                        visited[nx][ny] = true;
                        queue.add(new Point(nx, ny));
                        connectedCount++;
                    }
                }
            }
        }

        // If we didn't visit all path tiles, there are disconnected ones
        return connectedCount != pathTiles.size();
    }

    private boolean validateMap() {
        boolean hasStart = false;
        Point startPoint = null;
        Point endPoint = null;
        int towerSlotCount = 0;
        List<Point> validStartPoints = new ArrayList<>();

        // First find all valid start points (edge-connected path tiles)
        for (int y = 0; y < currentMap.getHeight(); y++) {
            for (int x = 0; x < currentMap.getWidth(); x++) {
                TileType type = currentMap.getTileType(x, y);
                if (type == TileType.START_POINT) {
                    if (hasStart) {
                        showAlert("Validation Error", "Multiple Start Points found. Only one allowed.");
                        return false;
                    }
                    hasStart = true;
                    startPoint = new Point(x, y);
                } else if (type == TileType.TOWER_SLOT) {
                    towerSlotCount++;
                }

                if (isValidStartTile(x, y)) {
                    validStartPoints.add(new Point(x, y));
                }
            }
        }

        // Check if there are exactly two valid start points
        if (validStartPoints.size() != 2) {
            showAlert("Validation Error", "There must be exactly two valid start points (edge-connected path tiles). Found " + validStartPoints.size() + ".");
            return false;
        }

        if (!hasStart) {
            showAlert("Validation Error", "No Start Point found.");
            return false;
        }

        // Verify that the start point is not a grass tile
        if (currentMap.getTileType(startPoint.x, startPoint.y) == TileType.GRASS) {
            showAlert("Validation Error", "Start Point cannot be placed on a grass tile.");
            return false;
        }

        // Verify that the start point is one of the valid start points
        boolean startIsValid = false;
        for (Point p : validStartPoints) {
            if (p.x == startPoint.x && p.y == startPoint.y) {
                startIsValid = true;
                break;
            }
        }

        if (!startIsValid) {
            showAlert("Validation Error", "Start Point must be placed on one of the two valid start points (edge-connected path tiles).");
            return false;
        }

        // Find the other valid start point (which becomes the end point)
        for (Point p : validStartPoints) {
            if (p.x != startPoint.x || p.y != startPoint.y) {
                endPoint = p;
                break;
            }
        }

        if (endPoint == null) {
            showAlert("Validation Error", "Could not determine End Point from valid start points.");
            return false;
        }

        // Check for disconnected tiles
        if (hasDisconnectedTiles()) {
            showAlert("Validation Error", "Map contains disconnected path tiles. All path tiles must be connected.");
            return false;
        }

        // Check minimum tower slots
        if (towerSlotCount < 4) {
            showAlert("Validation Error", "Map must have at least 4 tower slots. Currently has " + towerSlotCount + ".");
            return false;
        }

        // Check path connectivity between start and end points
        if (!isPathConnected(startPoint, endPoint)) {
            showAlert("Validation Error", "No valid path found from Start Point to End Point.");
            return false;
        }

        showAlert("Validation Success", "Map validation successful!");
        return true;
    }

    private boolean canSetAsStartPoint(int x, int y) {
        // Check if it's a grass tile
        if (currentMap.getTileType(x, y) == TileType.GRASS) {
            showAlert("Invalid Start Point", "Start Point cannot be placed on a grass tile.");
            return false;
        }

        // Check if it's a valid start tile (edge-connected path tile)
        if (!isValidStartTile(x, y)) {
            showAlert("Invalid Start Point", "Start Point must be placed on an edge-connected path tile.");
            return false;
        }

        // Count valid start points
        List<Point> validStartPoints = new ArrayList<>();
        for (int ty = 0; ty < currentMap.getHeight(); ty++) {
            for (int tx = 0; tx < currentMap.getWidth(); tx++) {
                if (isValidStartTile(tx, ty)) {
                    validStartPoints.add(new Point(tx, ty));
                }
            }
        }

        // Check if there are exactly two valid start points
        if (validStartPoints.size() != 2) {
            showAlert("Invalid Start Point", "There must be exactly two valid start points (edge-connected path tiles). Found " + validStartPoints.size() + ".");
            return false;
        }

        // Check if the selected point is one of the valid start points
        boolean isOneOfValidPoints = false;
        for (Point p : validStartPoints) {
            if (p.x == x && p.y == y) {
                isOneOfValidPoints = true;
                break;
            }
        }

        if (!isOneOfValidPoints) {
            showAlert("Invalid Start Point", "Start Point must be placed on one of the two valid start points (edge-connected path tiles).");
            return false;
        }

        return true;
    }

    private void handleStartPointPlacement(int x, int y) {
        if (!canSetAsStartPoint(x, y)) {
            return;
        }

        // Clear any existing start point
        for (int ty = 0; ty < currentMap.getHeight(); ty++) {
            for (int tx = 0; tx < currentMap.getWidth(); tx++) {
                if (currentMap.getTileType(tx, ty) == TileType.START_POINT) {
                    currentMap.setTileType(tx, ty, TileType.PATH_HORIZONTAL); // Or whatever the original tile type was
                }
            }
        }

        // Set the new start point
        currentMap.setTileType(x, y, TileType.START_POINT);
        canvasView.renderMap();
    }

    private boolean isOnEdge(int x, int y) {
        return x == 0 || x == currentMap.getWidth() - 1 || y == 0 || y == currentMap.getHeight() - 1;
    }

    /**
     * Checks if a tile at (x,y) is a valid start point.
     * A valid start point is either:
     * 1. A START_POINT tile
     * 2. A path tile at the edge of the map that is not a dead end
     * Valid configurations:
     * - Vertical path tiles at the top or bottom edge
     * - Horizontal path tiles at the left or right edge
     * - Circular path tiles in the corners
     */
    private boolean isValidStartTile(int x, int y) {
        TileType type = currentMap.getTileType(x, y);
        
        // Must be on the edge of the map
        boolean isOnEdge = x == 0 || x == currentMap.getWidth() - 1 || y == 0 || y == currentMap.getHeight() - 1;
        if (!isOnEdge) return false;

        // Check if it's a valid path tile
        switch (type) {
            case PATH_HORIZONTAL:
                // Valid if on left or right edge
                return x == 0 || x == currentMap.getWidth() - 1;
            case PATH_VERTICAL:
                // Valid if on top or bottom edge
                return y == 0 || y == currentMap.getHeight() - 1;
            case PATH_CIRCLE_N:
            case PATH_CIRCLE_S:
                // Treated as horizontal - valid if on left or right edge
                return x == 0 || x == currentMap.getWidth() - 1;
            case PATH_CIRCLE_W:
            case PATH_CIRCLE_E:
                // Treated as vertical - valid if on top or bottom edge
                return y == 0 || y == currentMap.getHeight() - 1;
            case PATH_CIRCLE_NW:
                // Valid if on right edge or bottom edge
                return x == currentMap.getWidth() - 1 || y == currentMap.getHeight() - 1;
            case PATH_CIRCLE_NE:
                // Valid if on left edge or bottom edge
                return x == 0 || y == currentMap.getHeight() - 1;
            case PATH_CIRCLE_SW:
                // Valid if on right edge or top edge
                return x == currentMap.getWidth() - 1 || y == 0;
            case PATH_CIRCLE_SE:
                // Valid if on left edge or top edge
                return x == 0 || y == 0;
            case START_POINT:
                // Always valid if it's a START_POINT tile
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if the 2x2 castle structure is correctly placed starting with the
     * bottom-left at (x, y).
     * Assumes (x, y) is TileType.END_POINT.
     */
    private boolean isCastleComplete(int baseX, int baseY) {
        if (baseX + 1 >= currentMap.getWidth() || baseY + 1 >= currentMap.getHeight()) {
            System.err.println("Castle bounds check failed: Base(" + baseX + "," + baseY + "), Map("
                    + currentMap.getWidth() + "," + currentMap.getHeight() + ")");
            return false;
        }

        // Check if the castle structure is complete
        boolean structureOk = currentMap.getTileType(baseX, baseY) == TileType.END_POINT &&
                currentMap.getTileType(baseX + 1, baseY) == TileType.CASTLE2 &&
                currentMap.getTileType(baseX, baseY + 1) == TileType.CASTLE3 &&
                currentMap.getTileType(baseX + 1, baseY + 1) == TileType.CASTLE4;

        if (!structureOk) {
            System.err.println("Castle structure check failed at (" + baseX + "," + baseY + ")");
            return false;
        }

        return true;
    }

    /**
     * Finds a walkable tile adjacent to the castle base (END_POINT) at (baseX,
     * baseY).
     * Returns the Point of the adjacent walkable tile, or null if none found.
     */
    private Point findAdjacentWalkable(int baseX, int baseY) {
        int[][] neighbors = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] offset : neighbors) {
            int nx = baseX + offset[0];
            int ny = baseY + offset[1];
            if (nx >= 0 && nx < currentMap.getWidth() && ny >= 0 && ny < currentMap.getHeight()) {
                Tile neighbor = currentMap.getTile(nx, ny);
                if (neighbor != null && neighbor.isWalkable()) {
                    return new Point(nx, ny);
                }
            }
        }
        return null;
    }

    /**
     * Checks if a path exists from the start point to the end point using BFS.
     * 
     * @param start       The starting Point (usually START_POINT).
     * @param endAdjacent The target Point adjacent to the castle base (must be
     *                    walkable).
     * @return true if a path exists, false otherwise.
     */
    private boolean isPathConnected(Point start, Point endAdjacent) {
        if (start == null || endAdjacent == null)
            return false;
        Tile endAdjacentTile = currentMap.getTile(endAdjacent.x, endAdjacent.y);
        if (endAdjacentTile == null || !endAdjacentTile.isWalkable()) {
            System.err.println("isPathConnected Error: Target adjacent point (" + endAdjacent.x + "," + endAdjacent.y
                    + ") is not walkable.");
            return false;
        }

        boolean[][] visited = new boolean[currentMap.getWidth()][currentMap.getHeight()];
        List<Point2D> queue = new ArrayList<>();

        queue.add(new Point2D(start.x, start.y));
        visited[start.x][start.y] = true;

        int head = 0;
        while (head < queue.size()) {
            Point2D current = queue.get(head++);
            int x = (int) current.getX();
            int y = (int) current.getY();

            if (x == endAdjacent.x && y == endAdjacent.y) {
                return true;
            }

            int[] dx = { 0, 0, -1, 1 };
            int[] dy = { -1, 1, 0, 0 };

            for (int i = 0; i < 4; i++) {
                int nextX = x + dx[i];
                int nextY = y + dy[i];

                if (nextX >= 0 && nextX < currentMap.getWidth() && nextY >= 0 && nextY < currentMap.getHeight()) {
                    Tile neighbor = currentMap.getTile(nextX, nextY);
                    if (!visited[nextX][nextY] && neighbor != null && neighbor.isWalkable()) {
                        visited[nextX][nextY] = true;
                        queue.add(new Point2D(nextX, nextY));
                    }
                }
            }
        }

        return false;
    }

    /**
     * Save the current map.
     */
    private void saveMap() {
        if (!validateMap()) {
            validateMap();
            return;
        }

        String mapName = currentMap.getName();
        if (mapName == null || mapName.trim().isEmpty() || mapName.equals("New Map")) {
            TextInputDialog dialog = new TextInputDialog("MyMap");
            dialog.setTitle("Save Map");
            dialog.setHeaderText("Please enter a name for your map");
            dialog.setContentText("Map name:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                mapName = result.get().trim();
                currentMap.setName(mapName);
            } else {
                return;
            }
        }

        String fileName = mapName.replaceAll("\\s+", "_") + ".map";
        File mapFile = new File(mapsDirectory, fileName);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mapFile))) {
            oos.writeObject(currentMap);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Map Saved");
            alert.setHeaderText("Map saved successfully");
            alert.setContentText("The map was saved to:\n" + mapFile.getAbsolutePath());
            alert.showAndWait();

            System.out.println("Map saved to: " + mapFile.getAbsolutePath());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to save map");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();

            System.err.println("Error saving map: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load a map from a file.
     */
    private void loadMap() {
        if (mapsDirectory == null || !mapsDirectory.exists()) {
            showAlert("Load Error", "Cannot find the maps directory: "
                    + (mapsDirectory != null ? mapsDirectory.getAbsolutePath() : "path not set"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Map File");
        fileChooser.setInitialDirectory(mapsDirectory);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Map Files", "*.map"));

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile))) {
                GameMap loadedMap = (GameMap) ois.readObject();
                this.currentMap = loadedMap;

                topToolbar.setGameMap(this.currentMap);
                canvasView.setGameMap(this.currentMap);

                handleWindowResize();
                canvasView.renderMap();

                tilePalette.selectTile(TileType.GRASS);
                canvasView.resetPlacementMode();

                showAlert("Load Success", "Map '" + currentMap.getName() + "' loaded successfully.");

            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                e.printStackTrace();
                showAlert("Load Error", "Failed to load map file: " + e.getMessage());
            }
        }
    }

    /**
     * Go back to the main menu.
     */
    private void goBack() {
        MainMenuScreen mainMenu = new MainMenuScreen(primaryStage);
        Scene mainMenuScene = new Scene(mainMenu, 800, 600);
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            mainMenuScene.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("Warning: Could not load stylesheet /css/style.css");
        }
        primaryStage.setScene(mainMenuScene);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void resizeMap(int newWidth, int newHeight) {
        if (newWidth <= 0 || newHeight <= 0) {
            showAlert("Resize Error", "Map dimensions must be positive.");
            return;
        }

        String mapName = currentMap.getName();
        this.currentMap = new GameMap(mapName, newWidth, newHeight);

        topToolbar.setGameMap(this.currentMap);
        canvasView.setGameMap(this.currentMap);

        handleWindowResize();
        canvasView.renderMap();

        tilePalette.selectTile(TileType.GRASS);
        canvasView.resetPlacementMode();

        System.out.println("Map resized to: " + newWidth + "x" + newHeight);
        showAlert("Map Resized",
                "Map resized to " + newWidth + "x" + newHeight + ". Start/End points may need resetting.");
    }

    private boolean isConnectedToEdge(int x, int y) {
        boolean[][] visited = new boolean[currentMap.getWidth()][currentMap.getHeight()];
        List<Point2D> queue = new ArrayList<>();
        
        queue.add(new Point2D(x, y));
        visited[x][y] = true;

        int head = 0;
        while (head < queue.size()) {
            Point2D current = queue.get(head++);
            int cx = (int) current.getX();
            int cy = (int) current.getY();

            // If we reach the edge, return true
            if (cx == 0 || cx == currentMap.getWidth() - 1 || cy == 0 || cy == currentMap.getHeight() - 1) {
                return true;
            }

            // Check all four directions
            int[] dx = { 0, 0, -1, 1 };
            int[] dy = { -1, 1, 0, 0 };

            for (int i = 0; i < 4; i++) {
                int nextX = cx + dx[i];
                int nextY = cy + dy[i];

                if (nextX >= 0 && nextX < currentMap.getWidth() && nextY >= 0 && nextY < currentMap.getHeight()) {
                    Tile neighbor = currentMap.getTile(nextX, nextY);
                    if (!visited[nextX][nextY] && neighbor != null && neighbor.isWalkable()) {
                        visited[nextX][nextY] = true;
                        queue.add(new Point2D(nextX, nextY));
                    }
                }
            }
        }

        return false;
    }
}
