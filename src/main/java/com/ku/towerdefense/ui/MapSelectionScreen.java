package com.ku.towerdefense.ui;

import com.ku.towerdefense.controller.GameController;
import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.TileType;
import com.ku.towerdefense.model.map.Tile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.ImageCursor;

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

        // Create some default maps with different layouts - REMOVED
        // GameMap desertMap = createDesertMap();
        // GameMap forestMap = createForestMap();
        // GameMap castleMap = createCastleMap();

        // maps.add(desertMap);
        // maps.add(forestMap);
        // maps.add(castleMap);

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
        String userHome = System.getProperty("user.home");
        File mapsDir = new File(userHome, "KUTowerDefenseMaps");
        if (!mapsDir.exists() || !mapsDir.isDirectory()) {
            System.out.println(
                    "Maps directory not found at " + mapsDir.getAbsolutePath() + ", no saved maps will be loaded");
            return;
        }

        File[] mapFiles = mapsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".map"));
        if (mapFiles == null || mapFiles.length == 0) {
            System.out.println("No map files found in maps directory: " + mapsDir.getAbsolutePath());
            return;
        }

        for (File mapFile : mapFiles) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mapFile))) {
                Object obj = ois.readObject();
                if (obj instanceof GameMap) {
                    GameMap map = (GameMap) obj;
                    maps.add(map);
                    System.out.println("Loaded map: " + map.getName() + " from " + mapFile.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Error loading map from file " + mapFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Loaded " + (maps.size() - 3) + " saved maps from " + mapsDir.getAbsolutePath());
    }

    /**
     * Initialize the user interface components.
     */
    private void initializeUI() {
        getStyleClass().add("map-selection-screen");
        setPadding(new Insets(20, 20, 40, 20));

        // Title
        Text title = new Text("Select Map");
        title.getStyleClass().add("screen-title");

        // Map count
        Label mapCountLabel = new Label("Maps: " + (currentMapIndex + 1) + " / " + availableMaps.size());
        mapCountLabel.getStyleClass().add("info-label");

        VBox titleAndCountBox = new VBox(5, title, mapCountLabel); // Reduced spacing for tighter group
        titleAndCountBox.setAlignment(Pos.CENTER);

        // Back Button (to be styled via CSS)
        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("secondary-button", "map-select-back-button"); // Added new style class
        backButton.setOnAction(e -> goBack());

        // Top area layout with StackPane for precise positioning
        StackPane topAreaPane = new StackPane();
        topAreaPane.getChildren().addAll(titleAndCountBox, backButton);
        StackPane.setAlignment(backButton, Pos.CENTER_LEFT); // Align back button to top-left (or center-left if padding
                                                             // allows)
        StackPane.setAlignment(titleAndCountBox, Pos.CENTER); // Center title/count
        topAreaPane.setPadding(new Insets(0, 0, 20, 0)); // Add some bottom padding to separate from content below

        // Map preview and info
        VBox mapInfoContainer = new VBox(20);
        mapInfoContainer.setAlignment(Pos.CENTER);

        Label mapNameLabel = new Label(availableMaps.get(currentMapIndex).getName());
        mapNameLabel.getStyleClass().add("map-name");

        // Map preview canvas
        mapPreviewCanvas = new Canvas(800, 600);
        StackPane previewContainer = new StackPane(mapPreviewCanvas);
        previewContainer.getStyleClass().add("map-preview-container");
        previewContainer
                .setStyle("-fx-padding: 10; -fx-background-color: rgba(0, 0, 0, 0.3); -fx-background-radius: 10;");

        updateMapPreview(); // Draw the initial map

        // New Icon Buttons for Map navigation
        Button prevIconButton = UIAssets.createIconButton("Previous Map", 0, 1, 80); // Increased size to 80
        if (prevIconButton.getGraphic() instanceof ImageView) {
            ((ImageView) prevIconButton.getGraphic()).setScaleX(-1); // Flip horizontally
        }
        prevIconButton.setOnAction(e -> {
            showPreviousMap(mapNameLabel, mapCountLabel);
            updateMapPreview();
            updateMapDescription();
        });

        Button nextIconButton = UIAssets.createIconButton("Next Map", 0, 1, 80); // Increased size to 80
        nextIconButton.setOnAction(e -> {
            showNextMap(mapNameLabel, mapCountLabel);
            updateMapPreview();
            updateMapDescription();
        });

        HBox previewNavigationLayout = new HBox(20, prevIconButton, previewContainer, nextIconButton);
        previewNavigationLayout.setAlignment(Pos.CENTER);

        // Map description
        mapDescLabel = new Label();
        updateMapDescription();
        mapDescLabel.setWrapText(true);
        mapDescLabel.setPrefWidth(400); // Keep description width reasonable
        mapDescLabel.getStyleClass().add("info-label");
        mapDescLabel.setAlignment(Pos.CENTER); // Center align text in label

        mapInfoContainer.getChildren().addAll(mapNameLabel, previewNavigationLayout, mapDescLabel);

        // Action buttons
        Button startGameButton = new Button("Start Game");
        startGameButton.getStyleClass().addAll("action-button", "start-game-button");
        startGameButton.setOnAction(e -> startGame());

        // Update bottom button container for only Start Game button
        HBox bottomButtonContainer = new HBox(startGameButton); // Only start game button now
        bottomButtonContainer.setAlignment(Pos.CENTER); // Center the single button
        bottomButtonContainer.setPadding(new Insets(20, 0, 0, 0));

        // Layout
        setTop(topAreaPane); // Set the new top area
        setCenter(mapInfoContainer);
        setBottom(bottomButtonContainer); // Set the updated bottom container
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
        if (map == null)
            return;

        // Calculate the scale factor to fit the map (based on source tile size) onto
        // the canvas
        double sourceWorldWidth = map.getWidth() * Tile.SOURCE_TILE_SIZE;
        double sourceWorldHeight = map.getHeight() * Tile.SOURCE_TILE_SIZE;

        double scaleX = mapPreviewCanvas.getWidth() / sourceWorldWidth;
        double scaleY = mapPreviewCanvas.getHeight() / sourceWorldHeight;
        double finalScale = Math.min(scaleX, scaleY) * 0.95; // Use 95% to leave a small margin

        // Calculate the offset to center the map
        double scaledWorldWidth = sourceWorldWidth * finalScale;
        double scaledWorldHeight = sourceWorldHeight * finalScale;
        double offsetX = (mapPreviewCanvas.getWidth() - scaledWorldWidth) / 2.0;
        double offsetY = (mapPreviewCanvas.getHeight() - scaledWorldHeight) / 2.0;

        // Draw background for the preview area
        gc.setFill(Color.web("#3c3c3c")); // A slightly darker gray, can be adjusted
        gc.fillRect(0, 0, mapPreviewCanvas.getWidth(), mapPreviewCanvas.getHeight());

        // Save the original transform
        gc.save();

        // Apply the scale and translation to the graphics context
        gc.translate(offsetX, offsetY);
        gc.scale(finalScale, finalScale);

        // Draw the map tiles using Tile.render()
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                com.ku.towerdefense.model.map.Tile tile = map.getTile(x, y); // Use fully qualified name or ensure
                                                                             // import
                if (tile != null) {
                    // Tile.render expects tile coordinates (x,y) and the size to render each tile
                    // at.
                    // Since our gc is already scaled to fit the whole map,
                    // the effective "tileSize" for rendering within this scaled context
                    // is the original SOURCE_TILE_SIZE.
                    tile.render(gc, x, y, Tile.SOURCE_TILE_SIZE, false);
                } else {
                    // Fallback for null tiles (e.g. if map data is incomplete)
                    gc.setFill(Color.BLACK);
                    gc.fillRect(x * Tile.SOURCE_TILE_SIZE, y * Tile.SOURCE_TILE_SIZE, Tile.SOURCE_TILE_SIZE,
                            Tile.SOURCE_TILE_SIZE);
                }
            }
        }

        // Restore the original transform
        gc.restore();
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

        // Use current stage dimensions
        Scene gameScene = new Scene(gameScreen, primaryStage.getWidth(), primaryStage.getHeight());
        gameScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Set custom cursor if available
        ImageCursor customCursor = UIAssets.getCustomCursor();
        if (customCursor != null) {
            gameScene.setCursor(customCursor);
        }

        primaryStage.setScene(gameScene);
        primaryStage.setFullScreen(true); // Ensure fullscreen

        // Start the game loop
        gameController.startGame();
    }

    /**
     * Go back to the main menu.
     */
    private void goBack() {
        MainMenuScreen mainMenu = new MainMenuScreen(primaryStage);
        // Use current stage dimensions
        Scene mainMenuScene = new Scene(mainMenu, primaryStage.getWidth(), primaryStage.getHeight());
        mainMenuScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Set custom cursor if available
        ImageCursor customCursor = UIAssets.getCustomCursor(); // Re-fetch or ensure it's available
        if (customCursor != null) {
            mainMenuScene.setCursor(customCursor);
        }

        primaryStage.setScene(mainMenuScene);
        primaryStage.setFullScreen(true); // Ensure fullscreen
    }
}