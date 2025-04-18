package com.ku.towerdefense.ui;

import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.Tile;
import com.ku.towerdefense.model.map.TileType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Toggle;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Point2D;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Screen for creating and editing game maps.
 */
public class MapEditorScreen extends BorderPane {
    private final Stage primaryStage;
    private GameMap currentMap;
    private TileType selectedTileType = TileType.GRASS;
    private Canvas mapCanvas;
    private int tileSize = 64;
    private File mapsDirectory;
    private ToggleGroup tileToggleGroup;
    private VBox leftToolbar;
    
    // Define which tile types appear in the editor palette - REORDERED
    private static final List<TileType> PALETTE_TILE_TYPES = Arrays.asList(
        // Path Tiles
        TileType.PATH_V,
        TileType.PATH_H,
        TileType.PATH_NE,
        TileType.PATH_NW,
        TileType.PATH_SE,
        TileType.PATH_SW,
        // Interaction Points
        TileType.START_POINT,
        TileType.END_POINT,
        TileType.TOWER_SLOT,
        // Terrain & Obstacles
        TileType.GRASS,
        TileType.OBSTACLE,
        TileType.ROCK1,
        TileType.ROCK2,
        // Decorations
        TileType.DECORATION,
        TileType.TREE1,
        TileType.TREE2,
        TileType.TREE3
    );
    
    /**
     * Constructor for the map editor screen.
     *
     * @param primaryStage the primary stage of the application
     */
    public MapEditorScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.currentMap = new GameMap("New Map", 20, 15);
        initializeMapsDirectory();
        initializeUI();
    }
    
    /**
     * Initialize the maps directory.
     */
    private void initializeMapsDirectory() {
        // Create maps directory if it doesn't exist
        mapsDirectory = new File("maps");
        if (!mapsDirectory.exists()) {
            boolean created = mapsDirectory.mkdirs();
            if (created) {
                System.out.println("Created maps directory: " + mapsDirectory.getAbsolutePath());
            } else {
                System.err.println("Failed to create maps directory");
            }
        }
    }
    
    /**
     * Initialize the user interface components.
     */
    private void initializeUI() {
        setStyle("-fx-background-color: #333333;");
        setPadding(new Insets(20));
        
        // Top toolbar with map name and size inputs
        HBox topToolbar = createTopToolbar();
        
        // Left toolbar with tile selection
        leftToolbar = createTileSelectionToolbar();
        
        // Canvas for map editing
        mapCanvas = new Canvas(currentMap.getWidth() * tileSize, currentMap.getHeight() * tileSize);
        mapCanvas.setOnMouseClicked(e -> {
            int x = (int) (e.getX() / tileSize);
            int y = (int) (e.getY() / tileSize);
            ToggleButton selectedToggle = (ToggleButton) tileToggleGroup.getSelectedToggle();
            if (selectedToggle != null && x >= 0 && x < currentMap.getWidth() && y >= 0 && y < currentMap.getHeight()) {
                TileType typeToPlace = (TileType) selectedToggle.getUserData();
                
                if (typeToPlace == TileType.START_POINT) {
                    clearExistingStartPoint();
                } else if (typeToPlace == TileType.END_POINT) {
                    clearExistingEndPoint();
                }
                
                currentMap.setTileType(x, y, typeToPlace);
                renderMap();
            }
        });
        
        // Wrap Canvas in ScrollPane
        ScrollPane mapScrollPane = new ScrollPane();
        mapScrollPane.setContent(mapCanvas);
        mapScrollPane.setFitToWidth(true); // Allows horizontal scroll if needed
        mapScrollPane.setFitToHeight(true); // Allows vertical scroll if needed
        // Set background color to a grassy green
        mapScrollPane.setStyle("-fx-background: #64B464; -fx-border-color: #444444;"); // Example green, adjust hex code as needed
        
        // Render initial map
        renderMap();
        
        // Bottom toolbar with save/load buttons
        HBox bottomToolbar = createBottomToolbar();
        
        // Set up layout
        setTop(topToolbar);
        setLeft(leftToolbar);
        setCenter(mapScrollPane);
        setBottom(bottomToolbar);
    }
    
    /**
     * Clear any existing start point on the map.
     */
    private void clearExistingStartPoint() {
        for (int y = 0; y < currentMap.getHeight(); y++) {
            for (int x = 0; x < currentMap.getWidth(); x++) {
                Tile tile = currentMap.getTile(x, y);
                if (tile != null && tile.getType() == TileType.START_POINT) {
                    currentMap.setTileType(x, y, TileType.GRASS);
                }
            }
        }
    }
    
    /**
     * Clear any existing end point on the map.
     */
    private void clearExistingEndPoint() {
        for (int y = 0; y < currentMap.getHeight(); y++) {
            for (int x = 0; x < currentMap.getWidth(); x++) {
                Tile tile = currentMap.getTile(x, y);
                if (tile != null && tile.getType() == TileType.END_POINT) {
                    currentMap.setTileType(x, y, TileType.GRASS);
                }
            }
        }
    }
    
    /**
     * Create the top toolbar with map settings.
     *
     * @return the top toolbar container
     */
    private HBox createTopToolbar() {
        Label mapNameLabel = new Label("Map Name:");
        TextField mapNameField = new TextField(currentMap.getName());
        mapNameField.textProperty().addListener((obs, old, newName) -> 
            currentMap.setName(newName));
        
        Label widthLabel = new Label("Width:");
        TextField widthField = new TextField(String.valueOf(currentMap.getWidth()));
        
        Label heightLabel = new Label("Height:");
        TextField heightField = new TextField(String.valueOf(currentMap.getHeight()));
        
        Button resizeButton = new Button("Resize");
        resizeButton.setOnAction(e -> {
            try {
                int width = Integer.parseInt(widthField.getText());
                int height = Integer.parseInt(heightField.getText());
                if (width > 0 && height > 0) {
                    currentMap = new GameMap(currentMap.getName(), width, height);
                    mapCanvas.setWidth(width * tileSize);
                    mapCanvas.setHeight(height * tileSize);
                    renderMap();
                }
            } catch (NumberFormatException ex) {
                // Invalid input, ignore
            }
        });
        
        HBox toolbar = new HBox(10, 
            mapNameLabel, mapNameField, 
            widthLabel, widthField, 
            heightLabel, heightField,
            resizeButton);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        return toolbar;
    }
    
    /**
     * Create the tile selection toolbar with images.
     *
     * @return the tile selection container
     */
    private VBox createTileSelectionToolbar() {
        VBox toolbarContainer = new VBox(10);
        toolbarContainer.setPadding(new Insets(0, 10, 0, 0));
        toolbarContainer.setAlignment(Pos.TOP_CENTER);
        toolbarContainer.getStyleClass().add("tile-palette");

        Label toolsLabel = new Label("Tile Palette");
        toolsLabel.getStyleClass().add("section-title");
        toolbarContainer.getChildren().add(toolsLabel);

        tileToggleGroup = new ToggleGroup();
        
        TilePane tilePane = new TilePane();
        tilePane.setPadding(new Insets(5));
        tilePane.setHgap(5);
        tilePane.setVgap(5);
        tilePane.setPrefColumns(3);

        System.out.println("--- Creating Tile Palette Buttons (Precise Viewport Method v2) ---");

        Image staticTileset = Tile.getBaseImageForType(TileType.GRASS); // Get base tileset ref
        if (staticTileset == null) {
            System.err.println("CRITICAL: Failed to load base tileset for palette!");
            // Optionally handle this error more gracefully
        }

        for (TileType type : PALETTE_TILE_TYPES) {
            ToggleButton tileButton = new ToggleButton();
            tileButton.setToggleGroup(tileToggleGroup);
            tileButton.setUserData(type);
            tileButton.setTooltip(new Tooltip(type.toString()));
            tileButton.getStyleClass().add("tile-option");

            Image imageToShow = null;    
            Rectangle2D viewport = null; 
            String logInfo = "Type: " + type;

            try {
                Image baseImage = Tile.getBaseImageForType(type);
                String baseImgId = (baseImage == null) ? "NULL" : Integer.toHexString(System.identityHashCode(baseImage));
                logInfo += ", BaseImgID: " + baseImgId;

                if (baseImage != null && !baseImage.isError()) {
                    if (baseImage == staticTileset) { // Is this type derived from the main tileset?
                        // Get the precise viewport rectangle directly from the static map
                        viewport = Tile.getSourceViewportForType(type); // Use new static helper
                        if (viewport != null) {
                            imageToShow = baseImage; // Show the tileset...
                            logInfo += ", Viewport: Mapped Rect [" + viewport.getMinX() + "," + viewport.getMinY() + " "+ viewport.getWidth() + "x" + viewport.getHeight() + "]";
                        } else {
                             logInfo += ", Viewport: NULL (Rect not found!) - Using Fallback";
                             imageToShow = null; // Trigger fallback graphic
                        }
                    } else { // Castle or Tower Slot
                        imageToShow = baseImage; // Show the specific image...
                        logInfo += ", Viewport: N/A (Specific Image)";
                        // viewport remains null
                    }
                } else {
                     logInfo += ", BaseImg: NULL or Error - Using Fallback";
                     imageToShow = null; // Trigger fallback graphic
                }

            } catch (Exception e) {
                 logInfo += ", EXCEPTION building palette button: " + e.getMessage();
                 e.printStackTrace();
                 imageToShow = null; // Ensure fallback on error
            }
            
            System.out.println("    Palette Button - " + logInfo);

            // --- Set Button Graphic --- 
            if (imageToShow != null) { 
                ImageView imageView = new ImageView(imageToShow);
                if (viewport != null) { // Apply viewport ONLY if one was retrieved from the map
                    imageView.setViewport(viewport); 
                } else {
                    imageView.setViewport(null); // Ensure viewport is null for non-atlas images
                }
                // Scale the resulting view 
                imageView.setFitWidth(tileSize); 
                imageView.setFitHeight(tileSize);
                imageView.setPreserveRatio(true); // Keep aspect ratio
                imageView.setSmooth(false); 
                tileButton.setGraphic(imageView);
            } else {
                 // Fallback graphic 
                 javafx.scene.shape.Rectangle fallbackRect = new javafx.scene.shape.Rectangle(tileSize, tileSize);
                 fallbackRect.setFill(getColorForTileType(type)); 
                 javafx.scene.text.Text fallbackText = new javafx.scene.text.Text(type.name().substring(0, 1));
                 fallbackText.setFill(Color.WHITE);
                 javafx.scene.layout.StackPane fallbackGraphic = new javafx.scene.layout.StackPane(fallbackRect, fallbackText);
                 tileButton.setGraphic(fallbackGraphic);
                 System.err.println("    Palette: Using fallback graphic for tile button: " + type);
            }
            // --- End Set Button Graphic --- 
            
            // Select the initial tile type (e.g., GRASS)
            if (type == selectedTileType) {
                tileButton.setSelected(true);
            }

            tilePane.getChildren().add(tileButton);
        }
        
        System.out.println("--- Finished Creating Tile Palette Buttons ---");
        toolbarContainer.getChildren().add(tilePane);
        return toolbarContainer;
    }
    
    // Helper method to get fallback colors
    private Color getColorForTileType(TileType type) {
        return switch (type) {
            case GRASS -> Color.LIMEGREEN;
            case PATH, PATH_V, PATH_H, PATH_NE, PATH_NW, PATH_SE, PATH_SW -> Color.SANDYBROWN;
            case START_POINT -> Color.DODGERBLUE;
            case END_POINT -> Color.INDIANRED;
            case TOWER_SLOT -> Color.SLATEGRAY;
            case DECORATION, TREE1, TREE2, TREE3 -> Color.FORESTGREEN;
            case OBSTACLE, ROCK1, ROCK2 -> Color.DARKGRAY;
            default -> Color.VIOLET;
        };
    }
    
    /**
     * Create the bottom toolbar with save/load buttons.
     *
     * @return the bottom toolbar container
     */
    private HBox createBottomToolbar() {
        Button saveButton = new Button("Save Map");
        saveButton.getStyleClass().add("action-button");
        saveButton.setOnAction(e -> saveMap());
        
        Button validateButton = new Button("Validate Map");
        validateButton.getStyleClass().add("action-button");
        validateButton.setOnAction(e -> validateMap(true));
        
        Button loadButton = new Button("Load Map");
        loadButton.getStyleClass().add("secondary-button");
        loadButton.setOnAction(e -> loadMap());
        
        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> goBack());
        
        HBox toolbar = new HBox(10, backButton, loadButton, validateButton, saveButton);
        toolbar.setPadding(new Insets(10, 0, 0, 0));
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        
        return toolbar;
    }
    
    /**
     * Render the current map on the canvas.
     */
    private void renderMap() {
        System.out.println("--- renderMap() called ---"); // Log entry
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        
        // Draw tiles using Tile.render()
        for (int y = 0; y < currentMap.getHeight(); y++) {
            for (int x = 0; x < currentMap.getWidth(); x++) {
                Tile tile = currentMap.getTile(x, y);
                if (tile != null) {
                    tile.render(gc, tileSize);
                }
            }
        }
        
        // Draw grid overlay (optional, can be toggled)
        gc.setStroke(Color.rgb(0, 0, 0, 0.2)); // Make grid less prominent
        gc.setLineWidth(0.5);
        for (int x = 0; x <= currentMap.getWidth(); x++) {
            gc.strokeLine(x * tileSize, 0, x * tileSize, currentMap.getHeight() * tileSize);
        }
        for (int y = 0; y <= currentMap.getHeight(); y++) {
            gc.strokeLine(0, y * tileSize, currentMap.getWidth() * tileSize, y * tileSize);
        }
        System.out.println("--- renderMap() finished ---"); // Log exit
    }
    
    /**
     * Validate the map for saving.
     * 
     * @param showAlert whether to show an alert with validation results
     * @return true if the map is valid, false otherwise
     */
    private boolean validateMap(boolean showAlert) {
        List<String> errors = new ArrayList<>();
        
        // Check for start point
        boolean hasStartPoint = false;
        boolean startPointAtEdge = false;
        int startX = -1;
        int startY = -1;
        
        // Check for end point
        boolean hasEndPoint = false;
        boolean endPointAtEdge = false;
        
        // Count tower slots
        int towerSlotCount = 0;
        
        // Scan the map for these elements
        for (int y = 0; y < currentMap.getHeight(); y++) {
            for (int x = 0; x < currentMap.getWidth(); x++) {
                Tile tile = currentMap.getTile(x, y);
                if (tile == null) continue;
                
                if (tile.getType() == TileType.START_POINT) {
                    hasStartPoint = true;
                    startX = x;
                    startY = y;
                    
                    // Check if at edge
                    if (x == 0 || x == currentMap.getWidth() - 1 || 
                        y == 0 || y == currentMap.getHeight() - 1) {
                        startPointAtEdge = true;
                    }
                } else if (tile.getType() == TileType.END_POINT) {
                    hasEndPoint = true;
                    
                    // Check if at edge
                    if (x == 0 || x == currentMap.getWidth() - 1 || 
                        y == 0 || y == currentMap.getHeight() - 1) {
                        endPointAtEdge = true;
                    }
                } else if (tile.getType() == TileType.TOWER_SLOT) {
                    towerSlotCount++;
                }
            }
        }
        
        // Validate the start point
        if (!hasStartPoint) {
            errors.add("Map has no start point");
        } else if (!startPointAtEdge) {
            errors.add("Start point must be at the edge of the map");
        }
        
        // Validate the end point
        if (!hasEndPoint) {
            errors.add("Map has no end point");
        } else if (!endPointAtEdge) {
            errors.add("End point must be at the edge of the map");
        }
        
        // Validate tower slots
        if (towerSlotCount < 4) {
            errors.add("Map must have at least 4 tower slots, but has only " + towerSlotCount);
        }
        
        // Validate path connectivity (only if we have both start and end)
        if (hasStartPoint && hasEndPoint) {
            boolean pathConnected = checkPathConnectivity(startX, startY);
            if (!pathConnected) {
                errors.add("Path from start to end is not fully connected");
            }
        }
        
        // Show validation results if requested
        if (showAlert) {
            if (errors.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Map Validation");
                alert.setHeaderText("Map Validation Successful");
                alert.setContentText("Map is valid and ready to save!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Map Validation");
                alert.setHeaderText("Map Validation Failed");
                
                StringBuilder sb = new StringBuilder();
                sb.append("Please fix the following issues:\n\n");
                for (String error : errors) {
                    sb.append("• ").append(error).append("\n");
                }
                
                alert.setContentText(sb.toString());
                alert.showAndWait();
            }
        }
        
        return errors.isEmpty();
    }
    
    /**
     * Check if there is a valid path from start to end.
     * Uses a simple flood fill algorithm to check connectivity.
     * 
     * @param startX starting X position
     * @param startY starting Y position
     * @return true if there is a connected path from start to end
     */
    private boolean checkPathConnectivity(int startX, int startY) {
        boolean[][] visited = new boolean[currentMap.getWidth()][currentMap.getHeight()];
        return floodFillPath(startX, startY, visited);
    }
    
    /**
     * Recursive flood fill to check path connectivity.
     * 
     * @param x current X position
     * @param y current Y position
     * @param visited visited cells
     * @return true if end point is reached
     */
    private boolean floodFillPath(int x, int y, boolean[][] visited) {
        // Check bounds
        if (x < 0 || x >= currentMap.getWidth() || y < 0 || y >= currentMap.getHeight()) {
            return false;
        }
        
        // Check if already visited
        if (visited[x][y]) {
            return false;
        }
        
        // Mark as visited
        visited[x][y] = true;
        
        // Get current tile
        Tile tile = currentMap.getTile(x, y);
        if (tile == null) return false;
        
        // Check if we reached the end
        if (tile.getType() == TileType.END_POINT) {
            return true;
        }
        
        // Check if the tile is walkable (using the Tile's own method)
        if (!tile.isWalkable()) { 
            return false;
        }
        
        // Try all four directions
        return floodFillPath(x+1, y, visited) ||
               floodFillPath(x-1, y, visited) ||
               floodFillPath(x, y+1, visited) ||
               floodFillPath(x, y-1, visited);
    }
    
    /**
     * Save the current map.
     */
    private void saveMap() {
        // Validate map before saving
        if (!validateMap(false)) {
            // Show validation errors
            validateMap(true);
            return;
        }
        
        // Prompt for file name if needed
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
                return; // User cancelled
            }
        }
        
        // Create file name (replace spaces with underscores)
        String fileName = mapName.replaceAll("\\s+", "_") + ".map";
        File mapFile = new File(mapsDirectory, fileName);
        
        // Serialize the map to file
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
     * Load a map from file.
     */
    private void loadMap() {
        // This would be implemented in the MapSelectionScreen class
        System.out.println("Load map functionality not yet implemented");
        
        // After loading 'currentMap':
        if (currentMap != null) {
             // ... resize canvas ...
             renderMap();
             // Update map name field
             // Potentially update selected tile in palette if needed?
             // Maybe default back to GRASS after load?
             selectedTileType = TileType.GRASS; 
             for (Toggle toggle : tileToggleGroup.getToggles()) {
                 ToggleButton tb = (ToggleButton) toggle;
                 if (tb.getUserData() == selectedTileType) {
                     tb.setSelected(true);
                     break;
                 }
             }
        }
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
