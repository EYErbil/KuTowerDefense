package com.ku.towerdefense.ui;

import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.Tile;
import com.ku.towerdefense.model.map.TileType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        // Terrain & Base
        TileType.GRASS,
        
        // Game Elements - critical gameplay elements
        TileType.START_POINT,
        TileType.END_POINT,
        TileType.TOWER_SLOT,
        
        // Path Tiles
        TileType.PATH_H,
        TileType.PATH_V,
        TileType.PATH_NE,
        TileType.PATH_NW,
        TileType.PATH_SE,
        TileType.PATH_SW,
        
        // Decorations and Obstacles
        TileType.DECORATION,
        TileType.TREE1,
        TileType.TREE2,
        TileType.TREE3,
        TileType.OBSTACLE,
        TileType.ROCK1,
        TileType.ROCK2,
        TileType.WELL,
        TileType.HOUSE,
        TileType.WOOD_PILE,
        TileType.BARREL
    );
    
    // Groups for category headers in the palette
    private static final Map<String, List<TileType>> TILE_CATEGORIES = Map.of(
        "Game Elements", List.of(TileType.START_POINT, TileType.END_POINT, TileType.TOWER_SLOT),
        "Path Tiles", List.of(TileType.PATH_H, TileType.PATH_V, TileType.PATH_NE, TileType.PATH_NW, TileType.PATH_SE, TileType.PATH_SW, TileType.PATH),
        "Base Terrain", List.of(TileType.GRASS),
        "Trees", List.of(TileType.TREE1, TileType.TREE2, TileType.TREE3), 
        "Buildings & Props", List.of(TileType.WELL, TileType.HOUSE, TileType.WOOD_PILE, TileType.BARREL),
        "Obstacles", List.of(TileType.ROCK1, TileType.ROCK2, TileType.OBSTACLE)
    );
    
    // Default and minimum window dimensions
    private static final double MIN_WINDOW_WIDTH = 800;
    private static final double MIN_WINDOW_HEIGHT = 600;
    
    // Map view settings
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 2.0;
    private static final double ZOOM_STEP = 0.1;
    
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
        setPadding(new Insets(10));
        
        // Make sure the window can be resized
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setResizable(true);
        
        // Top toolbar with map name and size inputs
        HBox topToolbar = createTopToolbar();
        
        // Left toolbar with tile selection
        leftToolbar = createTileSelectionToolbar();
        
        // Map container with zoom controls
        VBox mapContainer = new VBox(10);
        mapContainer.setAlignment(Pos.CENTER);
        
        // Add zoom controls
        HBox zoomControls = createZoomControls();
        
        // Create a group to hold the canvas for zooming
        Group canvasGroup = new Group();
        
        // Canvas for map editing
        mapCanvas = new Canvas(currentMap.getWidth() * tileSize, currentMap.getHeight() * tileSize);
        mapCanvas.setOnMouseClicked(e -> {
            // Convert coordinates based on zoom level
            double zoomedX = e.getX() / zoomLevel;
            double zoomedY = e.getY() / zoomLevel;
            
            int x = (int) (zoomedX / tileSize);
            int y = (int) (zoomedY / tileSize);
            
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
        
        // Apply initial scaling transform to canvas
        mapCanvas.getTransforms().clear();
        mapCanvas.getTransforms().add(new Scale(zoomLevel, zoomLevel));
        
        canvasGroup.getChildren().add(mapCanvas);
        
        // Wrap Canvas in a ScrollPane with managed size
        ScrollPane mapScrollPane = new ScrollPane();
        mapScrollPane.setContent(canvasGroup);
        mapScrollPane.setFitToWidth(true);
        mapScrollPane.setFitToHeight(true);
        mapScrollPane.setPrefViewportHeight(Math.min(500, currentMap.getHeight() * tileSize));
        mapScrollPane.setPrefViewportWidth(Math.min(700, currentMap.getWidth() * tileSize));
        
        // Set background color to a grassy green
        mapScrollPane.setStyle("-fx-background: #64B464; -fx-border-color: #444444;");
        
        mapContainer.getChildren().addAll(zoomControls, mapScrollPane);
        
        // Render initial map
        renderMap();
        
        // Bottom toolbar with save/load buttons
        HBox bottomToolbar = createBottomToolbar();
        
        // Set up layout with resize handling - do this BEFORE applying zoom fully
        setTop(topToolbar);
        setLeft(leftToolbar);
        setCenter(mapContainer);
        setBottom(bottomToolbar);
        
        // Now that we have set the center component, update the zoom label
        Label zoomLabel = (Label) zoomControls.getChildren().get(1);
        zoomLabel.setText(String.format("Zoom: %.0f%%", zoomLevel * 100));
        
        // Handle any resize needs
        handleWindowResize();
        
        // Add window resize listener
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> handleWindowResize());
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> handleWindowResize());
    }
    
    /**
     * Handle window resizing by adjusting the map view.
     */
    private void handleWindowResize() {
        // Adjust the ScrollPane preferred size based on available space
        double availableWidth = primaryStage.getWidth() - leftToolbar.getWidth() - 40;
        double availableHeight = primaryStage.getHeight() - 200; // Adjust for top and bottom toolbars
        
        ScrollPane mapScrollPane = (ScrollPane) ((VBox) getCenter()).getChildren().get(1);
        mapScrollPane.setPrefViewportWidth(Math.min(availableWidth, currentMap.getWidth() * tileSize * zoomLevel));
        mapScrollPane.setPrefViewportHeight(Math.min(availableHeight, currentMap.getHeight() * tileSize * zoomLevel));
    }
    
    /**
     * Create zoom controls for the map.
     * 
     * @return HBox containing zoom controls
     */
    private HBox createZoomControls() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        
        Button zoomOutButton = new Button("-");
        zoomOutButton.setOnAction(e -> {
            if (zoomLevel > MIN_ZOOM) {
                zoomLevel -= ZOOM_STEP;
                applyZoom();
            }
        });
        
        Label zoomLabel = new Label(String.format("Zoom: %.0f%%", zoomLevel * 100));
        zoomLabel.setTextFill(Color.WHITE);
        
        Button zoomInButton = new Button("+");
        zoomInButton.setOnAction(e -> {
            if (zoomLevel < MAX_ZOOM) {
                zoomLevel += ZOOM_STEP;
                applyZoom();
            }
        });
        
        Button resetZoomButton = new Button("Reset Zoom");
        resetZoomButton.setOnAction(e -> {
            zoomLevel = 1.0;
            applyZoom();
        });
        
        controls.getChildren().addAll(zoomOutButton, zoomLabel, zoomInButton, resetZoomButton);
        return controls;
    }
    
    /**
     * Apply the current zoom level to the map canvas.
     */
    private void applyZoom() {
        // Apply zoom to canvas
        mapCanvas.getTransforms().clear();
        mapCanvas.getTransforms().add(new Scale(zoomLevel, zoomLevel));
        
        // Get the center component and update zoom label if it exists
        VBox centerVBox = (VBox) getCenter();
        if (centerVBox != null && !centerVBox.getChildren().isEmpty()) {
            try {
                HBox zoomControls = (HBox) centerVBox.getChildren().get(0);
                if (zoomControls != null && zoomControls.getChildren().size() > 1) {
                    Label zoomLabel = (Label) zoomControls.getChildren().get(1);
                    zoomLabel.setText(String.format("Zoom: %.0f%%", zoomLevel * 100));
                }
            } catch (ClassCastException | IndexOutOfBoundsException e) {
                // Ignore errors during initialization
                System.out.println("Note: Zoom controls not fully initialized yet");
            }
        }
        
        // Handle resize to update the scroll pane if center exists
        if (getCenter() != null) {
            handleWindowResize();
        }
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
        // Create a stylish header using UI assets
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10, 15, 15, 15));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        // Try to use a UI asset as background
        try {
            Image ribbonImage = new Image(getClass().getResourceAsStream("/Asset_pack/UI/Ribbon_Blue_3Slides.png"));
            BackgroundImage bgImage = new BackgroundImage(
                ribbonImage,
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true)
            );
            toolbar.setBackground(new Background(bgImage));
        } catch (Exception e) {
            System.err.println("Failed to load ribbon background: " + e.getMessage());
            toolbar.setStyle("-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); -fx-background-radius: 5px;");
        }
        
        // Map name with label and field
        VBox nameBox = new VBox(5);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        
        Label mapNameLabel = new Label("Map Name:");
        mapNameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        TextField mapNameField = new TextField(currentMap.getName());
        mapNameField.textProperty().addListener((obs, old, newName) -> 
            currentMap.setName(newName));
        mapNameField.setPrefWidth(200);
        
        nameBox.getChildren().addAll(mapNameLabel, mapNameField);
        
        // Size controls
        VBox sizeBox = new VBox(5);
        sizeBox.setAlignment(Pos.CENTER_LEFT);
        
        HBox dimensionLabels = new HBox(10);
        dimensionLabels.setAlignment(Pos.CENTER_LEFT);
        
        Label widthLabel = new Label("Width:");
        widthLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Label heightLabel = new Label("Height:");
        heightLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        dimensionLabels.getChildren().addAll(widthLabel, heightLabel);
        
        HBox dimensionFields = new HBox(10);
        dimensionFields.setAlignment(Pos.CENTER_LEFT);
        
        TextField widthField = new TextField(String.valueOf(currentMap.getWidth()));
        widthField.setPrefWidth(80);
        
        TextField heightField = new TextField(String.valueOf(currentMap.getHeight()));
        heightField.setPrefWidth(80);
        
        dimensionFields.getChildren().addAll(widthField, heightField);
        sizeBox.getChildren().addAll(dimensionLabels, dimensionFields);
        
        // Resize button
        VBox resizeBox = new VBox();
        resizeBox.setAlignment(Pos.CENTER);
        resizeBox.setPadding(new Insets(0, 0, 0, 10));
        
        Button resizeButton = new Button("Resize Map");
        resizeButton.getStyleClass().add("action-button");
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
        
        resizeBox.getChildren().add(resizeButton);
        
        // Add sections to toolbar with separators
        Region spacer1 = new Region();
        spacer1.setPrefWidth(20);
        
        Region spacer2 = new Region();
        spacer2.setPrefWidth(20);
        
        toolbar.getChildren().addAll(nameBox, spacer1, sizeBox, spacer2, resizeBox);
        
        return toolbar;
    }
    
    /**
     * Create the tile selection toolbar with images.
     *
     * @return the tile selection container
     */
    private VBox createTileSelectionToolbar() {
        // Create the outer container that will hold the title and scrollpane
        VBox outerContainer = new VBox(5);
        outerContainer.setPadding(new Insets(0, 10, 0, 0));
        outerContainer.setAlignment(Pos.TOP_CENTER);
        outerContainer.setMinWidth(200);
        
        // Add the title
        Label toolsLabel = new Label("Tile Palette");
        toolsLabel.getStyleClass().add("section-title");
        toolsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 5px;");
        outerContainer.getChildren().add(toolsLabel);
        
        // Create the inner container for the actual palette content
        VBox toolbarContainer = new VBox(5);
        toolbarContainer.setPadding(new Insets(5));
        toolbarContainer.setAlignment(Pos.TOP_CENTER);
        toolbarContainer.getStyleClass().add("tile-palette");
        
        // Create a ScrollPane to make the palette scrollable
        ScrollPane scrollPane = new ScrollPane(toolbarContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefHeight(500); // Set a reasonable height
        
        tileToggleGroup = new ToggleGroup();
        
        System.out.println("--- Creating Tile Palette Buttons (Precise Viewport Method v2) ---");

        Image staticTileset = Tile.getBaseImageForType(TileType.GRASS); // Get base tileset ref
        if (staticTileset == null) {
            System.err.println("CRITICAL: Failed to load base tileset for palette!");
        }
        
        // Add tiles in categories with headers
        for (Map.Entry<String, List<TileType>> category : TILE_CATEGORIES.entrySet()) {
            // Add category header
            Label categoryLabel = new Label(category.getKey());
            categoryLabel.getStyleClass().add("category-header");
            categoryLabel.setMaxWidth(Double.MAX_VALUE);
            categoryLabel.setAlignment(Pos.CENTER);
            categoryLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #444444; -fx-text-fill: white; -fx-padding: 3px;");
            toolbarContainer.getChildren().add(categoryLabel);
            
            // Create tile pane for this category
            TilePane tilePane = new TilePane();
            tilePane.setPadding(new Insets(5));
            tilePane.setHgap(5);
            tilePane.setVgap(5);
            tilePane.setPrefColumns(3);
            
            // Add tile buttons for this category
            for (TileType type : category.getValue()) {
                ToggleButton tileButton = createTileButton(type, staticTileset);
                tilePane.getChildren().add(tileButton);
            }
            
            toolbarContainer.getChildren().add(tilePane);
        }
        
        // Add the scrollpane to the outer container
        outerContainer.getChildren().add(scrollPane);
        
        System.out.println("--- Finished Creating Tile Palette Buttons ---");
        return outerContainer;
    }
    
    /**
     * Creates a tile button for the given tile type.
     * 
     * @param type The tile type
     * @param staticTileset The tileset image
     * @return A configured toggle button
     */
    private ToggleButton createTileButton(TileType type, Image staticTileset) {
        ToggleButton tileButton = new ToggleButton();
        tileButton.setToggleGroup(tileToggleGroup);
        tileButton.setUserData(type);
        tileButton.setTooltip(new Tooltip(getTooltipForTileType(type)));
        
        // Make special tiles stand out
        if (type == TileType.START_POINT || type == TileType.END_POINT) {
            tileButton.setStyle("-fx-border-color: gold; -fx-border-width: 2px;");
        }
        
        tileButton.getStyleClass().add("tile-option");
        tileButton.setMinSize(48, 48);
        tileButton.setPrefSize(48, 48);

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
            imageView.setFitWidth(40); 
            imageView.setFitHeight(40);
            imageView.setPreserveRatio(true); // Keep aspect ratio
            imageView.setSmooth(false); 
            tileButton.setGraphic(imageView);
        } else {
             // Fallback graphic 
             javafx.scene.shape.Rectangle fallbackRect = new javafx.scene.shape.Rectangle(40, 40);
             fallbackRect.setFill(getColorForTileType(type)); 
             javafx.scene.text.Text fallbackText = new javafx.scene.text.Text(type.name().substring(0, 1));
             fallbackText.setFill(Color.WHITE);
             javafx.scene.layout.StackPane fallbackGraphic = new javafx.scene.layout.StackPane(fallbackRect, fallbackText);
             tileButton.setGraphic(fallbackGraphic);
             System.err.println("    Palette: Using fallback graphic for tile button: " + type);
        }
        
        // Select the initial tile type
            if (type == selectedTileType) {
            tileButton.setSelected(true);
        }
        
        return tileButton;
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
            case WELL, HOUSE, WOOD_PILE, BARREL -> Color.BROWN;
            default -> Color.VIOLET;
        };
    }
    
    // Tooltips with better descriptions
    private String getTooltipForTileType(TileType type) {
        return switch (type) {
            case GRASS -> "Grass - Basic terrain tile";
            case START_POINT -> "Start Point - Where enemies will spawn (place at map edge)";
            case END_POINT -> "End Point/Castle - Destination for enemies (place at map edge)";
            case TOWER_SLOT -> "Tower Slot - Where towers can be built";
            case PATH_V -> "Vertical Path - For enemy movement";
            case PATH_H -> "Horizontal Path - For enemy movement";
            case PATH_NE -> "Northeast Corner Path";
            case PATH_NW -> "Northwest Corner Path";
            case PATH_SE -> "Southeast Corner Path";
            case PATH_SW -> "Southwest Corner Path";
            case OBSTACLE, ROCK1, ROCK2 -> "Rock - Blocks enemy movement and tower placement";
            case DECORATION, TREE1, TREE2, TREE3 -> "Tree - Decorative element";
            case WELL -> "Water Well - Decorative structure";
            case HOUSE -> "House - Decorative building";
            case WOOD_PILE -> "Wood Pile - Decorative element";
            case BARREL -> "Barrel - Decorative prop";
            default -> type.toString();
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
        
        Button helpButton = new Button("Game Rules");
        helpButton.getStyleClass().add("secondary-button");
        helpButton.setOnAction(e -> showGameMechanicsHelp());
        
        Button loadButton = new Button("Load Map");
        loadButton.getStyleClass().add("secondary-button");
        loadButton.setOnAction(e -> loadMap());
        
        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> goBack());
        
        HBox toolbar = new HBox(10, backButton, loadButton, helpButton, validateButton, saveButton);
        toolbar.setPadding(new Insets(10, 0, 0, 0));
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        
        return toolbar;
    }
    
    /**
     * Display a help window explaining the game mechanics and tile functions.
     */
    private void showGameMechanicsHelp() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Tower Defense Game Rules");
        helpAlert.setHeaderText("How the Game Works");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Game mechanics explanation
        Label howToPlayLabel = new Label("How to Play:");
        howToPlayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label mechanicsLabel = new Label(
            "1. Enemies spawn at the Start Point\n" +
            "2. They follow the path toward the End Point (Castle)\n" +
            "3. If enemies reach the End Point, you lose health\n" +
            "4. Place towers on Tower Slots to defend your castle\n" +
            "5. Different tower types have different strengths against enemy types"
        );
        
        // Tile explanations
        Label tilesLabel = new Label("Tile Types:");
        tilesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label startPointLabel = new Label("• Start Point: Where enemies spawn. Must be at the edge of the map.");
        Label endPointLabel = new Label("• End Point (Castle): Where enemies try to reach. Must be at the edge of the map.");
        Label towerSlotLabel = new Label("• Tower Slot: Where you can place defensive towers.");
        Label pathLabel = new Label("• Path Tiles: Connect Start Point to End Point for enemies to follow.");
        Label obstacleLabel = new Label("• Obstacles: Block both enemy movement and tower placement.");
        Label decorationLabel = new Label("• Decorations: Visual elements that don't affect gameplay.");
        
        // Map editor tips
        Label tipsLabel = new Label("Map Editor Tips:");
        tipsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label tip1 = new Label("• Your map must have exactly 1 Start Point and 1 End Point");
        Label tip2 = new Label("• Ensure there is a valid path connecting Start Point to End Point");
        Label tip3 = new Label("• Place at least 4 Tower Slots for an enjoyable game");
        Label tip4 = new Label("• Use Validate Map to check if your map is playable");
        
        content.getChildren().addAll(
            howToPlayLabel, mechanicsLabel, 
            new Separator(), 
            tilesLabel, startPointLabel, endPointLabel, towerSlotLabel, pathLabel, obstacleLabel, decorationLabel,
            new Separator(),
            tipsLabel, tip1, tip2, tip3, tip4
        );
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        
        helpAlert.getDialogPane().setContent(scrollPane);
        helpAlert.showAndWait();
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
