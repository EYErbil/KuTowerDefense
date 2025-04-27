package com.ku.towerdefense.ui;

import com.ku.towerdefense.controller.GameController;
import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.TileType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen for selecting a map to play the game on.
 */
public class MapSelectionScreen extends BorderPane {
    private final Stage primaryStage;
    private final List<GameMap> availableMaps;
    private int currentMapIndex = 0;

    private Canvas mapPreviewCanvas;
    private Label mapDescLabel;

    /**
     * Constructor for the map selection screen.
     *
     * @param primaryStage the primary stage of the application
     */
    public MapSelectionScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.availableMaps = loadAvailableMaps();
        initializeUI();
    }

    /**
     * Load the available maps from resources and saved maps.
     *
     * @return list of available maps
     */
    private List<GameMap> loadAvailableMaps() {
        List<GameMap> maps = new ArrayList<>();

        // Create some default maps with different layouts
        GameMap desertMap = createDesertMap();
        GameMap forestMap = createForestMap();
        GameMap castleMap = createCastleMap();

        maps.add(desertMap);
        maps.add(forestMap);
        maps.add(castleMap);

        // Load user-created maps from the maps directory
        loadSavedMaps(maps);

        return maps;
    }

    /**
     * Load user-created maps from the maps directory.
     *
     * @param maps list to add the loaded maps to
     */
    private void loadSavedMaps(List<GameMap> maps) {
        File mapsDir = new File("maps");
        if (!mapsDir.exists() || !mapsDir.isDirectory()) {
            System.out.println("Maps directory not found, no saved maps will be loaded");
            return;
        }

        File[] mapFiles = mapsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".map"));
        if (mapFiles == null || mapFiles.length == 0) {
            System.out.println("No map files found in maps directory");
            return;
        }

        for (File mapFile : mapFiles) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mapFile))) {
                Object obj = ois.readObject();
                if (obj instanceof GameMap) {
                    GameMap map = (GameMap) obj;
                    maps.add(map);
                    System.out.println("Loaded map: " + map.getName() + " from " + mapFile.getName());
                }
            } catch (Exception e) {
                System.err.println("Error loading map from file " + mapFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Loaded " + (maps.size() - 3) + " saved maps");
    }

    /**
     * Create a desert themed map
     */
    private GameMap createDesertMap() {
        GameMap map = new GameMap("Desert Path", 20, 15);

        // Set end point
        map.setTileType(19, 7, TileType.CASTLE1);

        // Create the path
        for (int x = 0; x < 19; x++) {
            map.setTileType(x, 7, TileType.PATH_HORIZONTAL);
        }

        // Add some tower slots
        map.setTileType(5, 5, TileType.TOWER_SLOT);
        map.setTileType(5, 9, TileType.TOWER_SLOT);
        map.setTileType(10, 5, TileType.TOWER_SLOT);
        map.setTileType(10, 9, TileType.TOWER_SLOT);
        map.setTileType(15, 5, TileType.TOWER_SLOT);
        map.setTileType(15, 9, TileType.TOWER_SLOT);

        // Add some decoration
        map.setTileType(3, 3, TileType.TREE_BIG);
        map.setTileType(8, 12, TileType.ROCK_MEDIUM);
        map.setTileType(13, 2, TileType.TREE_SMALL);
        map.setTileType(17, 11, TileType.WELL);

        return map;
    }

    /**
     * Create a forest themed map
     */
    private GameMap createForestMap() {
        GameMap map = new GameMap("Forest Trail", 20, 15);

        // Set end point
        map.setTileType(19, 11, TileType.CASTLE1);

        // Create a winding path
        for (int x = 0; x < 5; x++) {
            map.setTileType(x, 3, TileType.PATH_HORIZONTAL);
        }

        for (int y = 4; y < 8; y++) {
            map.setTileType(5, y, TileType.PATH_VERTICAL);
        }

        for (int x = 6; x < 11; x++) {
            map.setTileType(x, 7, TileType.PATH_HORIZONTAL);
        }

        for (int y = 8; y < 12; y++) {
            map.setTileType(10, y, TileType.PATH_VERTICAL);
        }

        for (int x = 11; x < 19; x++) {
            map.setTileType(x, 11, TileType.PATH_HORIZONTAL);
        }

        // Connect the corners (simple connections)
        map.setTileType(5, 3, TileType.PATH_CIRCLE_SE);
        map.setTileType(5, 7, TileType.PATH_CIRCLE_NE);
        map.setTileType(10, 7, TileType.PATH_CIRCLE_SW);
        map.setTileType(10, 11, TileType.PATH_CIRCLE_NW);

        // Add some tower slots in strategic positions
        map.setTileType(3, 2, TileType.TOWER_SLOT);
        map.setTileType(3, 5, TileType.TOWER_SLOT);
        map.setTileType(7, 5, TileType.TOWER_SLOT);
        map.setTileType(11, 5, TileType.TOWER_SLOT);
        map.setTileType(11, 9, TileType.TOWER_SLOT);
        map.setTileType(15, 10, TileType.TOWER_SLOT);
        map.setTileType(17, 8, TileType.TOWER_SLOT);

        // Add decorations (trees, rocks, etc)
        map.setTileType(2, 7, TileType.TREE_MEDIUM);
        map.setTileType(5, 11, TileType.ROCK_SMALL);
        map.setTileType(9, 2, TileType.TREE_BIG);
        map.setTileType(14, 4, TileType.LOG_PILE);
        map.setTileType(16, 13, TileType.TREE_SMALL);

        return map;
    }

    /**
     * Create a castle themed map
     */
    private GameMap createCastleMap() {
        GameMap map = new GameMap("Castle Siege", 20, 15);

        // Set end point
        map.setTileType(17, 7, TileType.CASTLE1);

        // Create a path with a central area
        for (int x = 0; x < 5; x++) {
            map.setTileType(x, 7, TileType.PATH_HORIZONTAL);
        }

        for (int y = 3; y <= 11; y++) {
            if (y != 7)
                map.setTileType(5, y, TileType.PATH_VERTICAL);
        }

        for (int x = 6; x <= 16; x++) {
            if (x != 17)
                map.setTileType(x, 3, TileType.PATH_HORIZONTAL);
            if (x != 17)
                map.setTileType(x, 11, TileType.PATH_HORIZONTAL);
        }

        for (int y = 4; y <= 10; y++) {
            if (y != 7)
                map.setTileType(16, y, TileType.PATH_VERTICAL);
        }

        // Add corners
        map.setTileType(5, 3, TileType.PATH_CIRCLE_SE);
        map.setTileType(5, 11, TileType.PATH_CIRCLE_NE);
        map.setTileType(16, 3, TileType.PATH_CIRCLE_SW);
        map.setTileType(16, 11, TileType.PATH_CIRCLE_NW);

        // Path leading to end point
        map.setTileType(16, 7, TileType.PATH_HORIZONTAL);

        // Add tower slots in a defensive formation
        for (int i = 0; i < 3; i++) {
            map.setTileType(9, 5 + i * 2, TileType.TOWER_SLOT);
            map.setTileType(13, 5 + i * 2, TileType.TOWER_SLOT);
        }

        // Add more tower slots
        map.setTileType(6, 5, TileType.TOWER_SLOT);
        map.setTileType(6, 9, TileType.TOWER_SLOT);

        // Add decorations
        map.setTileType(2, 2, TileType.HOUSE);
        map.setTileType(2, 12, TileType.WELL);
        map.setTileType(19, 0, TileType.ROCK_MEDIUM);
        map.setTileType(19, 14, TileType.LOG_PILE);

        return map;
    }

    /**
     * Initialize the user interface components.
     */
    private void initializeUI() {
        setStyle("-fx-background-color: #333333;");
        setPadding(new Insets(20));

        // Title
        Text title = new Text("Select Map");
        title.getStyleClass().add("screen-title");

        // Map count
        Label mapCountLabel = new Label("Maps: " + currentMapIndex + 1 + " / " + availableMaps.size());
        mapCountLabel.setStyle("-fx-text-fill: white;");

        // Map preview and info
        VBox mapInfoContainer = new VBox(20);
        mapInfoContainer.setAlignment(Pos.CENTER);

        Label mapNameLabel = new Label(availableMaps.get(currentMapIndex).getName());
        mapNameLabel.getStyleClass().add("map-name");

        // Map preview canvas
        mapPreviewCanvas = new Canvas(400, 300);
        StackPane previewContainer = new StackPane(mapPreviewCanvas);
        previewContainer.setStyle("-fx-background-color: #555555; -fx-border-color: #888888;");

        updateMapPreview(); // Draw the initial map

        // Map navigation buttons
        Button prevMapButton = new Button("← Previous");
        UIAssets.styleButton(prevMapButton, "blue");
        prevMapButton.setOnAction(e -> {
            showPreviousMap(mapNameLabel, mapCountLabel);
            updateMapPreview();
            updateMapDescription();
        });

        Button nextMapButton = new Button("Next →");
        UIAssets.styleButton(nextMapButton, "blue");
        nextMapButton.setOnAction(e -> {
            showNextMap(mapNameLabel, mapCountLabel);
            updateMapPreview();
            updateMapDescription();
        });

        HBox mapNavigation = new HBox(20, prevMapButton, nextMapButton);
        mapNavigation.setAlignment(Pos.CENTER);

        // Map description
        mapDescLabel = new Label();
        updateMapDescription();
        mapDescLabel.setWrapText(true);
        mapDescLabel.setPrefWidth(400);
        mapDescLabel.setStyle("-fx-text-fill: white;");

        mapInfoContainer.getChildren().addAll(mapNameLabel, previewContainer, mapDescLabel, mapNavigation);

        // Action buttons
        Button startGameButton = new Button("Start Game");
        startGameButton.getStyleClass().add("action-button");
        UIAssets.styleButton(startGameButton, "red");
        startGameButton.setOnAction(e -> startGame());

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("secondary-button");
        UIAssets.styleButton(backButton, "blue");
        backButton.setOnAction(e -> goBack());

        HBox buttonContainer = new HBox(20, backButton, startGameButton);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(20, 0, 0, 0));

        // Layout
        VBox topContainer = new VBox(20, title, mapCountLabel);
        topContainer.setAlignment(Pos.CENTER);

        setTop(topContainer);
        setCenter(mapInfoContainer);
        setBottom(buttonContainer);
    }

    /**
     * Update the map description label based on the current map
     */
    private void updateMapDescription() {
        GameMap currentMap = availableMaps.get(currentMapIndex);
        int towerSlots = countTowerSlots(currentMap);

        String themeName = currentMap.getName().toLowerCase();
        if (themeName.contains("desert")) {
            themeName = "desert";
        } else if (themeName.contains("forest")) {
            themeName = "forest";
        } else if (themeName.contains("castle")) {
            themeName = "castle siege";
        }

        mapDescLabel.setText(String.format(
                "This map features a %s theme with %d tower slots. Map size: %dx%d tiles.",
                themeName, towerSlots, currentMap.getWidth(), currentMap.getHeight()));
    }

    /**
     * Count the number of tower slots in a map
     */
    private int countTowerSlots(GameMap map) {
        int count = 0;
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                if (map.getTile(x, y) != null && map.getTile(x, y).getType() == TileType.TOWER_SLOT) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Update the map preview canvas to show the current map
     */
    private void updateMapPreview() {
        GraphicsContext gc = mapPreviewCanvas.getGraphicsContext2D();

        // Clear the canvas
        gc.clearRect(0, 0, mapPreviewCanvas.getWidth(), mapPreviewCanvas.getHeight());

        // Get the current map
        GameMap map = availableMaps.get(currentMapIndex);

        // Calculate the scale factor to fit the map within the canvas
        double scaleX = mapPreviewCanvas.getWidth() / (map.getWidth() * 32);
        double scaleY = mapPreviewCanvas.getHeight() / (map.getHeight() * 32);
        double scale = Math.min(scaleX, scaleY) * 0.9; // 90% to leave some margin

        // Calculate the offset to center the map
        double offsetX = (mapPreviewCanvas.getWidth() - (map.getWidth() * 32 * scale)) / 2;
        double offsetY = (mapPreviewCanvas.getHeight() - (map.getHeight() * 32 * scale)) / 2;

        // Draw background
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, 0, mapPreviewCanvas.getWidth(), mapPreviewCanvas.getHeight());

        // Save the original transform
        gc.save();

        // Apply the scale and translation
        gc.translate(offsetX, offsetY);
        gc.scale(scale, scale);

        // Draw the map tiles
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getTile(x, y) != null) {
                    drawTile(gc, x, y, map.getTile(x, y).getType());
                } else {
                    // Draw empty/grass tiles
                    gc.setFill(Color.GREEN);
                    gc.fillRect(x * 32, y * 32, 32, 32);
                }
            }
        }

        // Restore the original transform
        gc.restore();
    }

    /**
     * Draw a single tile on the canvas.
     *
     * @param gc   the graphics context
     * @param x    x coordinate in tile units
     * @param y    y coordinate in tile units
     * @param type the tile type to draw
     */
    private void drawTile(GraphicsContext gc, int x, int y, TileType type) {
        Color color;
        switch (type) {
            case GRASS:
                color = Color.GREEN;
            case PATH_CIRCLE_NW, PATH_CIRCLE_N, PATH_CIRCLE_NE, PATH_CIRCLE_E, PATH_CIRCLE_SE, PATH_CIRCLE_S,
                    PATH_CIRCLE_SW, PATH_CIRCLE_W, PATH_VERTICAL_N_DE, PATH_VERTICAL, PATH_VERTICAL_S_DE,
                    PATH_HORIZONTAL_W_DE, PATH_HORIZONTAL, PATH_HORIZONTAL_E_DE:
                color = Color.SANDYBROWN;
                break;
            case TOWER_SLOT:
                color = Color.DARKGRAY;
            case CASTLE1, CASTLE2, CASTLE3, CASTLE4:
                color = Color.LIGHTGRAY;
                break;
            case TREE_BIG, TREE_MEDIUM, TREE_SMALL:
                color = Color.DARKGREEN;
            case ROCK_SMALL, ROCK_MEDIUM:
                color = Color.GRAY;
            case HOUSE:
                color = Color.BROWN;
            case WELL:
                color = Color.BLUE;
            case LOG_PILE:
                color = Color.SIENNA;
            case TOWER_ARTILLERY, TOWER_MAGE, ARCHER_TOWER, TOWER_BARACK:
                color = Color.PURPLE;
                break;
            case START_POINT:
                color = Color.BLUE;
            case END_POINT:
                color = Color.RED;
            default:
                color = Color.PINK;
        }

        gc.setFill(color);
        gc.fillRect(x * 32, y * 32, 32, 32);
    }

    /**
     * Show the previous map in the list.
     *
     * @param mapNameLabel  label to update with map name
     * @param mapCountLabel label to update with map count
     */
    private void showPreviousMap(Label mapNameLabel, Label mapCountLabel) {
        currentMapIndex--;
        if (currentMapIndex < 0) {
            currentMapIndex = availableMaps.size() - 1;
        }
        updateMapLabels(mapNameLabel, mapCountLabel);
    }

    /**
     * Show the next map in the list.
     *
     * @param mapNameLabel  label to update with map name
     * @param mapCountLabel label to update with map count
     */
    private void showNextMap(Label mapNameLabel, Label mapCountLabel) {
        currentMapIndex++;
        if (currentMapIndex >= availableMaps.size()) {
            currentMapIndex = 0;
        }
        updateMapLabels(mapNameLabel, mapCountLabel);
    }

    /**
     * Update the map labels with current information
     *
     * @param mapNameLabel  label to update with map name
     * @param mapCountLabel label to update with map count
     */
    private void updateMapLabels(Label mapNameLabel, Label mapCountLabel) {
        mapNameLabel.setText(availableMaps.get(currentMapIndex).getName());
        mapCountLabel.setText("Maps: " + (currentMapIndex + 1) + " / " + availableMaps.size());
    }

    /**
     * Start the game with the selected map.
     */
    private void startGame() {
        GameMap selectedMap = availableMaps.get(currentMapIndex);

        // Ensure the path is generated for the selected map
        if (selectedMap.getEnemyPath() == null) {
            selectedMap.generatePath();
        }

        GameController gameController = new GameController(selectedMap);
        GameScreen gameScreen = new GameScreen(primaryStage, gameController);

        Scene gameScene = new Scene(gameScreen, 1024, 768);
        gameScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(gameScene);

        // Start the game loop
        gameController.startGame();
    }

    /**
     * Go back to the main menu.
     */
    private void goBack() {
        MainMenuScreen mainMenu = new MainMenuScreen(primaryStage);
        Scene mainMenuScene = new Scene(mainMenu, 800, 600);
        mainMenuScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(mainMenuScene);
    }
}