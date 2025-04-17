package com.ku.towerdefense.ui;

import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.Tile;
import com.ku.towerdefense.model.map.TileType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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
    private int tileSize = 32;
    private File mapsDirectory;
    
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
        VBox leftToolbar = createTileSelectionToolbar();
        
        // Canvas for map editing
        mapCanvas = new Canvas(currentMap.getWidth() * tileSize, currentMap.getHeight() * tileSize);
        mapCanvas.setOnMouseClicked(e -> {
            int x = (int) (e.getX() / tileSize);
            int y = (int) (e.getY() / tileSize);
            if (x >= 0 && x < currentMap.getWidth() && y >= 0 && y < currentMap.getHeight()) {
                // Handle special cases for start and end points (only one of each allowed)
                if (selectedTileType == TileType.START_POINT) {
                    clearExistingStartPoint();
                } else if (selectedTileType == TileType.END_POINT) {
                    clearExistingEndPoint();
                }
                
                // Set the new tile
                currentMap.setTileType(x, y, selectedTileType);
                renderMap();
            }
        });
        
        // Render initial map
        renderMap();
        
        // Bottom toolbar with save/load buttons
        HBox bottomToolbar = createBottomToolbar();
        
        // Set up layout
        setTop(topToolbar);
        setLeft(leftToolbar);
        setCenter(mapCanvas);
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
     * Create the tile selection toolbar.
     *
     * @return the tile selection container
     */
    private VBox createTileSelectionToolbar() {
        VBox tileToolbar = new VBox(10);
        tileToolbar.setPadding(new Insets(0, 10, 0, 0));
        tileToolbar.setAlignment(Pos.TOP_CENTER);
        
        Label toolsLabel = new Label("Tile Tools");
        toolsLabel.getStyleClass().add("section-title");
        
        // Create a button for each tile type
        for (TileType type : TileType.values()) {
            Button tileButton = new Button(type.toString());
            tileButton.setPrefWidth(120);
            tileButton.setOnAction(e -> {
                selectedTileType = type;
                updateTileButtonStyles(tileToolbar);
            });
            
            // Add a style class to show the selected tile type
            if (type == selectedTileType) {
                tileButton.getStyleClass().add("selected-tile");
            }
            
            tileToolbar.getChildren().add(tileButton);
        }
        
        return tileToolbar;
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
     * Update the visual styles of tile buttons to show selection.
     *
     * @param toolbar the toolbar containing the buttons
     */
    private void updateTileButtonStyles(VBox toolbar) {
        for (int i = 1; i < toolbar.getChildren().size(); i++) { // Skip label
            Button button = (Button) toolbar.getChildren().get(i);
            button.getStyleClass().remove("selected-tile");
            
            TileType buttonType = TileType.valueOf(button.getText());
            if (buttonType == selectedTileType) {
                button.getStyleClass().add("selected-tile");
            }
        }
    }
    
    /**
     * Render the current map on the canvas.
     */
    private void renderMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        
        // Draw background
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        
        // Draw tiles
        for (int y = 0; y < currentMap.getHeight(); y++) {
            for (int x = 0; x < currentMap.getWidth(); x++) {
                Tile tile = currentMap.getTile(x, y);
                if (tile != null) {
                    drawTile(gc, x, y, tile.getType());
                }
            }
        }
        
        // Draw grid
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.5);
        for (int x = 0; x <= currentMap.getWidth(); x++) {
            gc.strokeLine(x * tileSize, 0, x * tileSize, currentMap.getHeight() * tileSize);
        }
        for (int y = 0; y <= currentMap.getHeight(); y++) {
            gc.strokeLine(0, y * tileSize, currentMap.getWidth() * tileSize, y * tileSize);
        }
    }
    
    /**
     * Draw a single tile on the canvas.
     *
     * @param gc the graphics context
     * @param x x coordinate in tile units
     * @param y y coordinate in tile units
     * @param type the tile type to draw
     */
    private void drawTile(GraphicsContext gc, int x, int y, TileType type) {
        switch (type) {
            case GRASS:
                gc.setFill(Color.GREEN);
                break;
            case PATH:
                gc.setFill(Color.SANDYBROWN);
                break;
            case START_POINT:
                gc.setFill(Color.BLUE);
                break;
            case END_POINT:
                gc.setFill(Color.RED);
                break;
            case TOWER_SLOT:
                gc.setFill(Color.GRAY);
                break;
            case DECORATION:
                gc.setFill(Color.LIGHTGRAY);
                break;
            default:
                gc.setFill(Color.BLACK);
        }
        
        gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
        
        // Draw icon or symbol based on tile type
        if (type == TileType.TOWER_SLOT) {
            // Draw a simple tower symbol
            gc.setFill(Color.DARKGRAY);
            double centerX = x * tileSize + tileSize / 2;
            double centerY = y * tileSize + tileSize / 2;
            double radius = tileSize / 4;
            gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        } else if (type == TileType.START_POINT) {
            // Draw an arrow pointing right
            gc.setFill(Color.WHITE);
            double[] xPoints = {
                x * tileSize + tileSize * 0.2, 
                x * tileSize + tileSize * 0.8, 
                x * tileSize + tileSize * 0.5
            };
            double[] yPoints = {
                y * tileSize + tileSize * 0.3,
                y * tileSize + tileSize * 0.5, 
                y * tileSize + tileSize * 0.7
            };
            gc.fillPolygon(xPoints, yPoints, 3);
        } else if (type == TileType.END_POINT) {
            // Draw an X
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeLine(
                x * tileSize + tileSize * 0.2, 
                y * tileSize + tileSize * 0.2, 
                x * tileSize + tileSize * 0.8, 
                y * tileSize + tileSize * 0.8
            );
            gc.strokeLine(
                x * tileSize + tileSize * 0.8, 
                y * tileSize + tileSize * 0.2, 
                x * tileSize + tileSize * 0.2, 
                y * tileSize + tileSize * 0.8
            );
        }
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
        
        // Only follow path tiles or the start point
        if (tile.getType() != TileType.PATH && tile.getType() != TileType.START_POINT) {
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