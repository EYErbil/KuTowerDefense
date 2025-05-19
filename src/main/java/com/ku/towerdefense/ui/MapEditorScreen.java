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
import javafx.scene.ImageCursor;

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
        getStyleClass().add("map-editor-screen");
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
            // canvasView.updateScrollPaneSize(availableWidth, availableHeight); // REMOVED
            // - CanvasView now manages its own size
        }
    }

    private HBox createBottomToolbar() {
        HBox bottomToolbar = new HBox(10);
        bottomToolbar.setPadding(new Insets(10));
        bottomToolbar.getStyleClass().add("editor-bottom-toolbar");

        Button saveButton = new Button("Save Map");
        saveButton.getStyleClass().add("button");
        saveButton.setOnAction(e -> saveMap());

        Button loadButton = new Button("Load Map");
        loadButton.getStyleClass().add("button");
        loadButton.setOnAction(e -> loadMap());

        Button validateButton = new Button("Validate Map");
        validateButton.getStyleClass().add("button");
        validateButton.setOnAction(e -> validateMap());

        Button helpButton = new Button("Help");
        helpButton.getStyleClass().add("button");
        helpButton.setOnAction(e -> showGameMechanicsHelp());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("Back to Main Menu");
        backButton.getStyleClass().add("button");
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
                        "- Must have exactly one Start Point (where enemies spawn).\n" +
                        "- The Start Point must be placed on an edge tile.\n" +
                        "- Must have exactly one End Point (enemy target).\n" +
                        "- The End Point is typically represented by a Castle structure.\n" +
                        "- There must be a valid path from Start Point to End Point.\n" +
                        "\nSpecial Tiles (Required):\n" +
                        "- START_POINT: Place this on a map edge where enemies will spawn.\n" +
                        "- END_POINT: Place this where enemies should try to reach. It automatically\n" +
                        "  places a 2x2 Castle structure.\n" +
                        "\nEditor Usage:\n" +
                        "- Select a tile from the left palette, including the new Special Tiles section.\n" +
                        "- Click on the canvas to place the selected tile.\n" +
                        "- For path creation, use various path tiles to connect Start and End points.\n" +
                        "- Tower slots can only be placed on Grass tiles.\n" +
                        "- Validate your map before saving to check all requirements are met.");
        textArea.setEditable(false);
        textArea.setWrapText(true);

        helpAlert.getDialogPane().setContent(textArea);
        helpAlert.setResizable(true);
        helpAlert.showAndWait();
    }

    private boolean validateMap() {
        boolean hasStart = false;
        boolean hasEnd = false;
        Point startPoint = null;
        Point endPointAdjacent = null;

        // First pass: Check for start and end points
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
                } else if (type == TileType.END_POINT) {
                    if (hasEnd) {
                        showAlert("Validation Error", "Multiple End Points (Castle bases) found. Only one allowed.");
                        return false;
                    }
                    if (!isCastleComplete(x, y)) {
                        showAlert("Validation Error", "Incomplete or invalid Castle structure found at (" + x + "," + y
                                + "). Ensure a full 2x2 castle is placed on Grass.");
                        return false;
                    }
                    hasEnd = true;
                    endPointAdjacent = findAdjacentWalkable(x, y);
                    if (endPointAdjacent == null) {
                        showAlert("Validation Error",
                                "Castle at (" + x + "," + y + ") must have an adjacent Path tile for the enemy route.");
                        return false;
                    }
                }
            }
        }

        if (!hasStart) {
            // Check for valid path tiles that could serve as start points
            for (int y = 0; y < currentMap.getHeight(); y++) {
                for (int x = 0; x < currentMap.getWidth(); x++) {
                    if (isValidStartTile(x, y)) {
                        hasStart = true;
                        startPoint = new Point(x, y);
                        break;
                    }
                }
                if (hasStart)
                    break;
            }
        }

        if (!hasStart) {
            showAlert("Validation Error",
                    "No valid start point found. Please place a START_POINT tile on the edge of the map.\n\n" +
                            "The START_POINT tile can be found in the 'Special Tiles' section of the palette and " +
                            "should be placed where enemies will spawn.");
            return false;
        }
        if (!hasEnd) {
            showAlert("Validation Error",
                    "No End Point (Castle) found. Please place an END_POINT tile on the map.\n\n" +
                            "The END_POINT tile can be found in the 'Special Tiles' section of the palette and " +
                            "will automatically place a 2x2 castle structure where enemies will try to reach.");
            return false;
        }
        if (startPoint == null || endPointAdjacent == null) {
            showAlert("Validation Error", "Internal error: Start or End adjacent point not determined.");
            return false;
        }

        if (!isPathConnected(startPoint, endPointAdjacent)) {
            showAlert("Validation Error", "No valid path found from Start Point to the End Point (Castle).");
            return false;
        }

        showAlert("Validation Success", "Map validation successful!");
        return true;
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
        if (!isOnEdge)
            return false;

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
     * Checks if the 2x2 castle structure is correctly placed starting at the
     * END_POINT.
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
     * Finds a walkable tile adjacent to the castle (END_POINT) at (baseX, baseY).
     * Returns the Point of the adjacent walkable tile, or null if none found.
     */
    private Point findAdjacentWalkable(int baseX, int baseY) {
        // Check all four sides of the 2x2 castle structure
        int[][] directions = {
                { -1, 0 }, { -1, 1 }, // Left side
                { 0, -1 }, { 1, -1 }, // Top side
                { 2, 0 }, { 2, 1 }, // Right side
                { 0, 2 }, { 1, 2 } // Bottom side
        };

        for (int[] offset : directions) {
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

                // Ensure all tiles reinitialize after loading
                for (int x = 0; x < currentMap.getWidth(); x++) {
                    for (int y = 0; y < currentMap.getHeight(); y++) {
                        Tile tile = currentMap.getTile(x, y);
                        if (tile != null) {
                            tile.reinitializeAfterLoad();
                        }
                    }
                }

                // Ensure path is regenerated
                currentMap.generatePath();

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
        Scene mainMenuScene = new Scene(mainMenu, primaryStage.getWidth(), primaryStage.getHeight());
        mainMenuScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Set custom cursor if available
        ImageCursor customCursor = UIAssets.getCustomCursor();
        if (customCursor != null) {
            mainMenuScene.setCursor(customCursor);
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
}
