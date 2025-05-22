package com.ku.towerdefense.ui;

import com.ku.towerdefense.controller.GameController;
import com.ku.towerdefense.model.entity.ArcherTower;
import com.ku.towerdefense.model.entity.ArtilleryTower;
import com.ku.towerdefense.model.entity.MageTower;
import com.ku.towerdefense.model.entity.Tower;
import com.ku.towerdefense.model.entity.DroppedGold;
import com.ku.towerdefense.model.map.Tile;
import com.ku.towerdefense.model.map.TileType;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Affine;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.ImageCursor;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.paint.ImagePattern;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.Point2D;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.NumberBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * The main game screen where the tower defense gameplay takes place.
 */

public class GameScreen extends BorderPane {
    private final Stage primaryStage;
    private final GameController gameController;
    private Canvas gameCanvas;
    private GameRenderTimer renderTimer;
    private AnimationTimer topBarUpdateTimer;
    private Tower selectedTower;
    private boolean isPaused = false;
    private final StackPane canvasRootPane = new StackPane();
    private final Pane uiOverlayPane = new Pane();
    private final Affine worldTransform = new Affine();
    private Node activePopup = null;
    private static final double POPUP_ICON_SIZE = 36.0;
    // private static final double POPUP_SPACING = 5.0; // Not currently used, can
    // be removed or kept for future

    // Game world and UI constants
    private static final double TILE_SIZE = 64.0;
    private static final double HALF_TILE_SIZE = TILE_SIZE / 2.0;
    private static final double BUILD_POPUP_RADIUS = 60.0;
    private static final double UPGRADE_SELL_POPUP_RADIUS = 60.0;

    // Zoom and Pan state
    private double currentZoomLevel = 1.0;
    private double minZoom = 0.2; // Adjusted for potentially large maps, was 0.25
    private double maxZoom = 4.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private boolean isPanning = false;
    private double lastMouseXForPan;
    private double lastMouseYForPan;
    private boolean dragOccurred = false; // New flag
    private double currentEffectiveScale = 1.0; // Added for drag handler to use renderer's scale

    private Label goldLabel;
    private Label livesLabel;
    private Label waveLabel;
    private ImageView goldIcon;
    private ImageView livesIcon;
    private ImageView waveIcon;

    // private Button pauseResumeButton; // REMOVED
    // private Button gameSpeedButton; // REMOVED
    private Button pauseButton;
    private Button playButton;
    private Button fastForwardButton;
    private Button menuButton;
    private static final String TIME_CONTROL_SELECTED_STYLE_CLASS = "time-control-selected";

    // Property to track the visual width of the map on screen
    private ReadOnlyDoubleWrapper visualMapWidthProperty = new ReadOnlyDoubleWrapper();

    // Define TowerBuildOption as a private static nested class
    private static class TowerBuildOption {
        String name;
        int cost;
        int iconCol, iconRow;
        java.util.function.Supplier<Tower> constructor;

        TowerBuildOption(String name, int cost, int iconCol, int iconRow,
                java.util.function.Supplier<Tower> constructor) {
            this.name = name;
            this.cost = cost;
            this.iconCol = iconCol;
            this.iconRow = iconRow;
            this.constructor = constructor;
        }
    }

    // Custom AnimationTimer class with additional methods
    private class GameRenderTimer extends AnimationTimer {
        private long lastTime = -1;
        private String statusMessage = "Ready to play!";
        private long statusTimestamp = 0;
        private double mouseX = 0;
        private double mouseY = 0;
        private boolean mouseInCanvas = false;

        @Override
        public void handle(long now) {
            GraphicsContext gc = gameCanvas.getGraphicsContext2D();
            double canvasWidth = gameCanvas.getWidth();
            double canvasHeight = gameCanvas.getHeight();

            // Calculate desired game world size
            double worldWidth = gameController.getGameMap().getWidth() * TILE_SIZE;
            double worldHeight = gameController.getGameMap().getHeight() * TILE_SIZE;

            // --- Calculate Scaling and Centering ---
            double baseScaleX = canvasWidth / worldWidth;
            double baseScaleY = canvasHeight / worldHeight;
            double baseScale = Math.min(baseScaleX, baseScaleY);

            double effectiveScale = baseScale * currentZoomLevel;
            GameScreen.this.currentEffectiveScale = effectiveScale;
            GameScreen.this.visualMapWidthProperty.set(worldWidth * effectiveScale); // Update property

            // Update the transformation matrix
            worldTransform.setToIdentity();
            // 1. Translate to center of canvas (this will be the view's center)
            worldTransform.appendTranslation(canvasWidth / 2.0, canvasHeight / 2.0);
            // 2. Apply zoom
            worldTransform.appendScale(effectiveScale, effectiveScale);
            // 3. Apply pan (panX and panY are world coordinates that should be at the
            // center)
            worldTransform.appendTranslation(-panX, -panY);
            // --- End Scaling and Centering ---

            // --- Rendering ---
            gc.save(); // Save default transform state

            // Clear the entire canvas (background)
            // gc.setFill(javafx.scene.paint.Color.web("#654321")); // Old color fill
            Image backgroundImage = UIAssets.getImage("WoodBackground");
            if (backgroundImage != null && !backgroundImage.isError()) {
                // Make the pattern tile smaller to show more repetitions
                double tileWidth = backgroundImage.getWidth() / 1.0;
                double tileHeight = backgroundImage.getHeight() / 1.0;
                ImagePattern pattern = new ImagePattern(backgroundImage, 0, 0, tileWidth, tileHeight, false);
                gc.setFill(pattern);
            } else {
                // Fallback to color if image fails to load
                gc.setFill(javafx.scene.paint.Color.web("#654321"));
            }
            gc.fillRect(0, 0, canvasWidth, canvasHeight);

            // Apply the world transformation (scale and center)
            gc.setTransform(worldTransform);

            // ---- Draw border around the map ---- START
            gc.setStroke(javafx.scene.paint.Color.web("#3B270E")); // Dark brown border
            gc.setLineWidth(12.0); // Border thickness in world units (will scale with zoom)
            gc.strokeRect(0, 0, worldWidth, worldHeight);
            // ---- Draw border around the map ---- END

            // Render game elements using original world coordinates
            // The transform handles scaling them correctly onto the canvas
            gameController.render(gc);

            // Render tower preview (using transformed mouse coords - see setOnMouseClicked)
            if (selectedTower != null && mouseInCanvas) {
                // Transform mouse coordinates from canvas space to world space
                javafx.geometry.Point2D worldMouse = transformMouseCoords(mouseX, mouseY);

                if (worldMouse != null) {
                    // Convert world coordinates to grid coordinates
                    int tileX = (int) (worldMouse.getX() / TILE_SIZE);
                    int tileY = (int) (worldMouse.getY() / TILE_SIZE);

                    // Get center of the tile in world coordinates
                    double centerX = tileX * TILE_SIZE + HALF_TILE_SIZE;
                    double centerY = tileY * TILE_SIZE + HALF_TILE_SIZE;

                    // Check if we can place here (uses world coordinates)
                    boolean canPlace = gameController.getGameMap().canPlaceTower(centerX, centerY,
                            gameController.getTowers());

                    // Draw preview circle in world coordinates
                    gc.setGlobalAlpha(0.5);
                    gc.setFill(canPlace ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
                    // Use world coordinates for drawing - transform handles canvas placement
                    gc.fillOval(centerX - HALF_TILE_SIZE, centerY - HALF_TILE_SIZE, TILE_SIZE, TILE_SIZE);

                    // Draw range preview in world coordinates
                    gc.setStroke(javafx.scene.paint.Color.WHITE);
                    gc.setGlobalAlpha(0.2);
                    double range = 0;
                    if (selectedTower instanceof ArcherTower)
                        range = ((ArcherTower) selectedTower).getRange();
                    else if (selectedTower instanceof ArtilleryTower)
                        range = ((ArtilleryTower) selectedTower).getRange();
                    else if (selectedTower instanceof MageTower)
                        range = ((MageTower) selectedTower).getRange();

                    if (range > 0) {
                        gc.strokeOval(centerX - range, centerY - range, range * 2, range * 2);
                    }
                    gc.setGlobalAlpha(1.0);
                }
            }

            gc.restore(); // Restore default transform for drawing UI overlays

            // --- Update game logic ---
            if (!isPaused) {
                if (lastTime < 0) {
                    lastTime = now;
                }
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                gameController.update(deltaTime);
            } else {
                lastTime = -1; // Reset delta time calculation when paused
            }

            // --- UI Overlays (drawn directly on canvas, not scaled) ---
            // Status message (bottom-left)
            long currentTime = System.currentTimeMillis();
            if (currentTime - statusTimestamp < 3000) {
                double alpha = 1.0 - (currentTime - statusTimestamp) / 3000.0;
                gc.setGlobalAlpha(alpha);
                gc.setFill(javafx.scene.paint.Color.WHITE);
                gc.setStroke(javafx.scene.paint.Color.BLACK);
                gc.setLineWidth(1); // Reduced thickness slightly
                gc.strokeText(statusMessage, 10, canvasHeight - 10); // Anchor bottom-left
                gc.fillText(statusMessage, 10, canvasHeight - 10);
                gc.setGlobalAlpha(1.0);
            }

            // Debug information (top-left) - REMOVE HUD ELEMENTS FROM HERE
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillText("Towers: " + gameController.getTowers().size(), 10, 20); // Keep for debug
            gc.fillText("Enemies: " + gameController.getEnemies().size(), 10, 40); // Keep for debug
            // gc.fillText("Wave: " + gameController.getCurrentWave(), 10, 60); // MOVED TO
            // TOP BAR

            // Asset loading issue message
            if (!gameController.getTowers().isEmpty() && gameController.getTowers().get(0).getImage() == null) {
                gc.setFill(javafx.scene.paint.Color.RED);
                gc.fillText("Asset loading issue detected!", 10, 80);
                gc.fillText("Using fallback rendering instead", 10, 100);
            }
        }

        // Method to set mouse position
        public void setMousePosition(double x, double y, boolean inCanvas) {
            this.mouseX = x;
            this.mouseY = y;
            this.mouseInCanvas = inCanvas;
        }

        // Method to set status message
        public void setStatusMessage(String message) {
            this.statusMessage = message;
            this.statusTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Constructor for the game screen.
     *
     * @param primaryStage   the primary stage of the application
     * @param gameController the game controller
     */
    public GameScreen(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;

        // Initialize panX and panY to the center of the map for initial full view
        if (gameController != null && gameController.getGameMap() != null) {
            double worldWidth = gameController.getGameMap().getWidth() * TILE_SIZE;
            double worldHeight = gameController.getGameMap().getHeight() * TILE_SIZE;
            this.panX = worldWidth / 2.0;
            this.panY = worldHeight / 2.0;
        } else {
            // Fallback if map is not ready, though it should be
            this.panX = 0;
            this.panY = 0;
        }

        initializeUI();
        startRenderLoop();
    }

    /**
     * Initialize the user interface components.
     */
    private void initializeUI() {
        getStyleClass().add("game-screen");

        gameCanvas = new Canvas(); // Canvas takes available space

        uiOverlayPane.setPickOnBounds(false);
        uiOverlayPane.prefWidthProperty().bind(gameCanvas.widthProperty());
        uiOverlayPane.prefHeightProperty().bind(gameCanvas.heightProperty());

        uiOverlayPane.setOnMouseClicked(event -> {
            if (activePopup != null && !event.isConsumed()) {
                boolean clickOutsidePopup = true;
                if (activePopup.getBoundsInParent().contains(event.getX(), event.getY())) {
                    clickOutsidePopup = false;
                }
                if (event.getTarget() == uiOverlayPane && clickOutsidePopup) {
                    clearActivePopup();
                }
            }
        });

        canvasRootPane.getChildren().addAll(gameCanvas, uiOverlayPane);

        setCenter(canvasRootPane);

        gameCanvas.widthProperty().bind(canvasRootPane.widthProperty());
        gameCanvas.heightProperty().bind(canvasRootPane.heightProperty());

        // ---- Create Game Info Display (Top-Left) ----
        VBox gameInfoPane = new VBox(8); // Spacing between elements
        gameInfoPane.setPadding(new Insets(15)); // Padding around the pane
        gameInfoPane.setAlignment(Pos.TOP_LEFT);
        // Make it transparent to mouse events so clicks go through to canvas/popups
        // unless an element inside it consumes the event.
        gameInfoPane.setPickOnBounds(false);

        Image hudIconsSheet = UIAssets.getImage("GameUI");
        double iconSheetEntryWidth = 79;
        double iconSheetEntryHeight = 218.0 / 3.0;
        double displayIconSize = 36;

        // Gold Display
        goldIcon = new ImageView(hudIconsSheet);
        goldIcon.setViewport(new javafx.geometry.Rectangle2D(0, 0, iconSheetEntryWidth, iconSheetEntryHeight));
        goldIcon.setFitWidth(displayIconSize);
        goldIcon.setFitHeight(displayIconSize);
        goldIcon.setPreserveRatio(true);
        goldIcon.setSmooth(true);
        goldLabel = new Label();
        goldLabel.getStyleClass().add("game-info-text");
        HBox goldDisplay = new HBox(8, goldIcon, goldLabel);
        goldDisplay.setAlignment(Pos.CENTER_LEFT);

        // Lives Display
        livesIcon = new ImageView(hudIconsSheet);
        livesIcon.setViewport(
                new javafx.geometry.Rectangle2D(0, iconSheetEntryHeight, iconSheetEntryWidth, iconSheetEntryHeight));
        livesIcon.setFitWidth(displayIconSize);
        livesIcon.setFitHeight(displayIconSize);
        livesIcon.setPreserveRatio(true);
        livesIcon.setSmooth(true);
        livesLabel = new Label();
        livesLabel.getStyleClass().add("game-info-text");
        HBox livesDisplay = new HBox(8, livesIcon, livesLabel);
        livesDisplay.setAlignment(Pos.CENTER_LEFT);

        // Wave Display
        waveIcon = new ImageView(hudIconsSheet);
        waveIcon.setViewport(new javafx.geometry.Rectangle2D(0, iconSheetEntryHeight * 2, iconSheetEntryWidth,
                iconSheetEntryHeight));
        waveIcon.setFitWidth(displayIconSize);
        waveIcon.setFitHeight(displayIconSize);
        waveIcon.setPreserveRatio(true);
        waveIcon.setSmooth(true);
        waveLabel = new Label();
        waveLabel.getStyleClass().add("game-info-text");
        HBox waveDisplay = new HBox(8, waveIcon, waveLabel);
        waveDisplay.setAlignment(Pos.CENTER_LEFT);

        gameInfoPane.getChildren().addAll(goldDisplay, livesDisplay, waveDisplay);
        uiOverlayPane.getChildren().add(gameInfoPane); // Add to overlay

        // ---- Create Control Buttons (Top-Right) ----
        VBox controlButtonsPane = new VBox(10); // Spacing between buttons
        controlButtonsPane.setPadding(new Insets(15));
        controlButtonsPane.setAlignment(Pos.TOP_CENTER); // Changed from TOP_RIGHT to TOP_CENTER
        controlButtonsPane.setPickOnBounds(false); // Allow clicks to pass through empty areas

        final double controlButtonIconSize = 108.0;

        pauseButton = UIAssets.createIconButton(UIAssets.LABEL_PAUSE, UIAssets.ICON_PAUSE_COL, UIAssets.ICON_PAUSE_ROW,
                controlButtonIconSize);
        pauseButton.setOnAction(e -> {
            isPaused = true;
            gameController.setPaused(true);
            updateTimeControlStates();
            e.consume();
        });

        playButton = UIAssets.createIconButton(UIAssets.LABEL_PLAY, UIAssets.ICON_PLAY_COL, UIAssets.ICON_PLAY_ROW,
                controlButtonIconSize);
        playButton.setOnAction(e -> {
            isPaused = false;
            gameController.setPaused(false);
            gameController.setSpeedAccelerated(false);
            updateTimeControlStates();
            e.consume();
        });

        fastForwardButton = UIAssets.createIconButton(UIAssets.LABEL_FAST_FORWARD, UIAssets.ICON_FAST_FORWARD_COL,
                UIAssets.ICON_FAST_FORWARD_ROW, controlButtonIconSize);
        fastForwardButton.setOnAction(e -> {
            isPaused = false;
            gameController.setPaused(false);
            gameController.setSpeedAccelerated(true);
            updateTimeControlStates();
            e.consume();
        });

        menuButton = UIAssets.createIconButton(UIAssets.LABEL_SETTINGS, UIAssets.ICON_SETTINGS_COL,
                UIAssets.ICON_SETTINGS_ROW, controlButtonIconSize);
        menuButton.setOnAction(e -> {
            showGameSettingsPopup();
            e.consume();
        });

        // Remove HBox for timeControls, add buttons directly to VBox for vertical
        // layout
        controlButtonsPane.getChildren().addAll(pauseButton, playButton, fastForwardButton, menuButton);
        uiOverlayPane.getChildren().add(controlButtonsPane);

        // Position controlButtonsPane at top-right, conditionally centered in right
        // band
        BooleanBinding mapIsNarrowerThanScreen = visualMapWidthProperty().lessThan(uiOverlayPane.widthProperty());

        NumberBinding layoutXWhenBandExists = uiOverlayPane.widthProperty().multiply(3)
                .add(visualMapWidthProperty())
                .divide(4)
                .subtract(controlButtonsPane.widthProperty().divide(2));

        NumberBinding layoutXWhenNoBand = uiOverlayPane.widthProperty()
                .subtract(controlButtonsPane.widthProperty())
                .subtract(15); // 15px padding from far right

        controlButtonsPane.layoutXProperty().bind(
                Bindings.when(mapIsNarrowerThanScreen)
                        .then(layoutXWhenBandExists)
                        .otherwise(layoutXWhenNoBand));
        controlButtonsPane.setLayoutY(15.0); // Set to fixed top padding

        // Initial state for time controls
        isPaused = false;
        gameController.setPaused(false);
        gameController.setSpeedAccelerated(false);
        updateTimeControlStates();

        // Mouse event handling on gameCanvas (remains the same)
        gameCanvas.setOnMouseMoved(e -> renderTimer.setMousePosition(e.getX(), e.getY(), true));
        gameCanvas.setOnMouseExited(e -> renderTimer.setMousePosition(e.getX(), e.getY(), false));
        gameCanvas.setOnScroll(event -> {
            /* ... existing zoom logic ... */ });
        gameCanvas.setOnMousePressed(event -> {
            /* ... existing pan logic ... */ });
        gameCanvas.setOnMouseDragged(event -> {
            /* ... existing pan logic ... */ });
        gameCanvas.setOnMouseReleased(event -> {
            /* ... existing pan logic ... */ });

        gameCanvas.setOnMouseClicked(e -> {
            if (dragOccurred) {
                dragOccurred = false;
                e.consume();
                System.out.println("[GameScreen] Click ignored due to drag.");
                return;
            }
            System.out.println("[GameScreen] Canvas clicked. Screen Coords: (" + e.getX() + "," + e.getY()
                    + ") Button: " + e.getButton());

            javafx.geometry.Point2D worldCoord = transformMouseCoords(e.getX(), e.getY());
            if (worldCoord == null) {
                System.out.println("[GameScreen] World coordinate transformation failed.");
                return;
            }

            double worldX = worldCoord.getX();
            double worldY = worldCoord.getY();
            int tileX = (int) (worldX / TILE_SIZE);
            int tileY = (int) (worldY / TILE_SIZE);
            System.out.println("[GameScreen] World Coords: (" + worldX + "," + worldY + ") -> Tile: (" + tileX + ","
                    + tileY + ")");

            Point2D tileCenterWorld = new Point2D(tileX * TILE_SIZE + HALF_TILE_SIZE,
                    tileY * TILE_SIZE + HALF_TILE_SIZE);
            Point2D tileCenterScreen = worldTransform.transform(tileCenterWorld);

            boolean actionTaken = false;

            // --- Check for Gold Bag Click FIRST ---
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                List<DroppedGold> bags = gameController.getActiveGoldBags();
                // Iterate in reverse to allow safe removal if multiple bags overlap
                for (int i = bags.size() - 1; i >= 0; i--) {
                    DroppedGold bag = bags.get(i);
                    // Check if click (worldX, worldY) is within bag's bounds
                    if (worldX >= bag.getX() && worldX <= (bag.getX() + bag.getWidth()) &&
                            worldY >= bag.getY() && worldY <= (bag.getY() + bag.getHeight())) {

                        gameController.collectGoldBag(bag); // Controller handles adding gold and removing bag
                        renderTimer.setStatusMessage("Collected " + bag.getGoldAmount() + " Gold!");
                        actionTaken = true;
                        break; // Stop checking other bags if one is clicked
                    }
                }
            }
            // --- End Gold Bag Click Check ---

            if (actionTaken) {
                System.out.println("[GameScreen] Consuming click event because gold bag was collected.");
                e.consume();
                return; // Do not process tower/tile clicks if a bag was collected
            }

            // --- Tower/Tile Click Logic (existing) ---
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                System.out.println("[GameScreen] Primary click. Attempting to find tower at world: (" + worldX + ","
                        + worldY + ")");
                Tower existingTower = gameController.getTowerAt(worldX, worldY);
                System.out.println("[GameScreen] gameController.getTowerAt returned: "
                        + (existingTower != null ? existingTower.getName() : "null"));

                if (existingTower != null) {
                    System.out.println("[GameScreen] Existing tower found: " + existingTower.getName()
                            + ". Showing upgrade/sell popup.");
                    clearActivePopup();
                    createUpgradeSellPopup(tileCenterScreen.getX(), tileCenterScreen.getY(), existingTower, tileX,
                            tileY);
                    actionTaken = true;
                } else {
                    System.out.println("[GameScreen] No existing tower found by getTowerAt. Checking tile type.");
                    Tile clickedTile = gameController.getGameMap().getTile(tileX, tileY);
                    if (clickedTile != null) {
                        System.out.println("[GameScreen] Clicked tile type: " + clickedTile.getType());
                        if (clickedTile.getType() == TileType.TOWER_SLOT) {
                            System.out.println("[GameScreen] Tile is TOWER_SLOT. Showing build popup.");
                            clearActivePopup();
                            createBuildTowerPopup(tileCenterScreen.getX(), tileCenterScreen.getY(), tileX, tileY);
                            actionTaken = true;
                        } else {
                            System.out.println("[GameScreen] Tile is not TOWER_SLOT. Clearing active popup.");
                            clearActivePopup(); // Clears if clicked on non-actionable tile like PATH or GRASS
                        }
                    } else {
                        System.out.println("[GameScreen] Clicked tile is null. Clearing active popup.");
                        clearActivePopup();
                    }
                }
            } else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                System.out.println("[GameScreen] Secondary click. Clearing active popup.");
                clearActivePopup();
            }

            // No explicit consume here, let existing logic decide or consume at the end if
            // needed
            // if (actionTakenOnTowerOrTile) { e.consume(); }
        });

        // Initialize and start the top bar update timer
        topBarUpdateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGameInfoLabels();
            }
        };
        topBarUpdateTimer.start();
    }

    private void updateTimeControlStates() {
        // Remove selected style from all buttons first
        if (pauseButton != null)
            pauseButton.getStyleClass().remove(TIME_CONTROL_SELECTED_STYLE_CLASS);
        if (playButton != null)
            playButton.getStyleClass().remove(TIME_CONTROL_SELECTED_STYLE_CLASS);
        if (fastForwardButton != null)
            fastForwardButton.getStyleClass().remove(TIME_CONTROL_SELECTED_STYLE_CLASS);

        if (isPaused) {
            if (pauseButton != null)
                pauseButton.getStyleClass().add(TIME_CONTROL_SELECTED_STYLE_CLASS);
            if (renderTimer != null) {
                renderTimer.stop();
                renderTimer.lastTime = -1; // Explicitly reset lastTime here
            }
            // The renderTimer's internal lastTime is reset to -1 when
            // GameScreen.this.isPaused is true in its handle() method.
        } else {
            if (gameController.isSpeedAccelerated()) {
                if (fastForwardButton != null)
                    fastForwardButton.getStyleClass().add(TIME_CONTROL_SELECTED_STYLE_CLASS);
            } else {
                if (playButton != null)
                    playButton.getStyleClass().add(TIME_CONTROL_SELECTED_STYLE_CLASS);
            }
            if (renderTimer != null) {
                // Ensure renderTimer's internal lastTime is reset if it was previously stopped.
                // The timer's handle() method already does this if its lastTime < 0.
                // Calling start() ensures it runs.
                renderTimer.start();
            }
        }
    }

    private void updateGameInfoLabels() {
        if (goldLabel != null) {
            goldLabel.setText("" + gameController.getPlayerGold());
        }
        if (livesLabel != null) {
            livesLabel.setText("" + gameController.getPlayerLives());
        }
        if (waveLabel != null) {
            waveLabel.setText("Wave: " + gameController.getCurrentWave());
        }
        // Pause/Speed button text/icon updates are now handled by
        // updateTimeControlStates()
    }

    /**
     * Start the render loop for the game canvas.
     */
    private void startRenderLoop() {
        renderTimer = new GameRenderTimer();
        renderTimer.start();

        // Add mouse moved listener to track position
        // gameCanvas.setOnMouseMoved(e -> { // REMOVED - Already set in initializeUI
        // renderTimer.setMousePosition(e.getX(), e.getY(), true);
        // });

        // Track when mouse exits canvas
        // gameCanvas.setOnMouseExited(e -> { // REMOVED - Already set in initializeUI
        // renderTimer.setMousePosition(0, 0, false);
        // });
    }

    /**
     * Transforms mouse coordinates from Canvas space to World space.
     * 
     * @param canvasX Mouse X relative to canvas.
     * @param canvasY Mouse Y relative to canvas.
     * @return Point2D in world coordinates, or null if transform is invalid.
     */
    private javafx.geometry.Point2D transformMouseCoords(double canvasX, double canvasY) {
        try {
            // Use the inverse of the world transform to go from canvas -> world
            return worldTransform.inverseTransform(canvasX, canvasY);
        } catch (javafx.scene.transform.NonInvertibleTransformException e) {
            System.err.println("Warning: Could not invert world transform for mouse input.");
            return null; // Return null if transform is broken
        }
    }

    private void clearActivePopup() {
        if (activePopup != null) {
            final Node popupNodeBeingCleared = activePopup; // Final for use in lambda
            activePopup = null; // Crucial: mark no popup as active *now*

            // Make the outgoing popup non-interactive immediately
            popupNodeBeingCleared.setMouseTransparent(true);

            FadeTransition ft = new FadeTransition(Duration.millis(150), popupNodeBeingCleared);
            // ft.setFromValue(1.0); // Assuming opacity is 1.0 when clearing
            ft.setFromValue(popupNodeBeingCleared.getOpacity()); // Fade from current opacity, good if it could be
                                                                 // non-1.0
            ft.setToValue(0.0);
            ft.setOnFinished(event -> {
                uiOverlayPane.getChildren().remove(popupNodeBeingCleared);
                // Optional: Reset mouseTransparent if the node were to be reused,
                // but it's being removed from the scene graph, so not strictly necessary.
                // popupNodeBeingCleared.setMouseTransparent(false);
            });
            ft.play();
        }
    }

    private void createBuildTowerPopup(double centerXScreen, double centerYScreen, int tileX, int tileY) {
        clearActivePopup();
        Pane popupPane = new Pane();
        popupPane.setPickOnBounds(false);

        List<TowerBuildOption> options = new ArrayList<>();
        // Ensure ArcherTower, MageTower, ArtilleryTower have public static int
        // BASE_COST;
        options.add(new TowerBuildOption("Archer Tower", ArcherTower.BASE_COST, 0, 2, () -> new ArcherTower(0, 0)));
        options.add(new TowerBuildOption("Mage Tower", MageTower.BASE_COST, 2, 2, () -> new MageTower(0, 0)));
        options.add(new TowerBuildOption("Artillery Tower", ArtilleryTower.BASE_COST, 3, 2,
                () -> new ArtilleryTower(0, 0)));
        options.add(new TowerBuildOption("Close", 0, 3, 0, null));

        int numOptions = options.size();
        double angleStep = 360.0 / numOptions;

        for (int i = 0; i < numOptions; i++) {
            TowerBuildOption opt = options.get(i);
            double angle = (i * angleStep) - 90; // Start at top (-90 degrees)
            double buttonX = centerXScreen + BUILD_POPUP_RADIUS * Math.cos(Math.toRadians(angle))
                    - POPUP_ICON_SIZE / 2.0;
            double buttonY = centerYScreen + BUILD_POPUP_RADIUS * Math.sin(Math.toRadians(angle))
                    - POPUP_ICON_SIZE / 2.0;

            Button button = UIAssets.createIconButton(opt.name + (opt.cost > 0 ? " (Cost: " + opt.cost + ")" : ""),
                    opt.iconCol, opt.iconRow, POPUP_ICON_SIZE);
            button.setLayoutX(buttonX);
            button.setLayoutY(buttonY);

            if (opt.constructor != null) {
                button.setOnAction(e -> {
                    Tower towerToBuild = opt.constructor.get(); // Creates a template tower
                    gameController.purchaseAndPlaceTower(towerToBuild, tileX, tileY); // Pass tile coords
                    clearActivePopup();
                    e.consume();
                });
            } else { // Close button
                button.setOnAction(e -> {
                    clearActivePopup();
                    e.consume();
                });
            }
            popupPane.getChildren().add(button);
        }

        activePopup = popupPane;
        uiOverlayPane.getChildren().add(activePopup);
        // Apply animation
        FadeTransition ft = new FadeTransition(Duration.millis(200), activePopup);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), activePopup);
        st.setFromX(0.7);
        st.setFromY(0.7);
        st.setToX(1.0);
        st.setToY(1.0);
        ParallelTransition pt = new ParallelTransition(ft, st);
        pt.play();
    }

    private void createUpgradeSellPopup(double centerXScreen, double centerYScreen, Tower existingTower, int tileX,
            int tileY) {
        clearActivePopup();
        Pane popupPane = new Pane();
        popupPane.setPickOnBounds(false);

        List<Button> buttons = new ArrayList<>();
        double angleStart = -90; // Top
        double radius = UPGRADE_SELL_POPUP_RADIUS;

        // Upgrade Button
        if (existingTower.canUpgrade()) {
            int upgradeCost = existingTower.getUpgradeCost();
            boolean canAfford = gameController.getPlayerGold() >= upgradeCost;
            String upgradeText = "Upgrade (";
            if (upgradeCost == Integer.MAX_VALUE) { // Should not happen if canUpgrade is true, but good check
                upgradeText += "N/A)";
            } else {
                upgradeText += upgradeCost + "G)";
            }

            Button upgradeButton = UIAssets.createIconButton(upgradeText, 1, 2, POPUP_ICON_SIZE); // Upgrade icon (1,2)

            if (canAfford) {
                upgradeButton.setOnAction(e -> {
                    boolean upgraded = gameController.upgradeTower(existingTower, tileX, tileY);
                    // if (upgraded) { // Effect removed
                    // }
                    clearActivePopup();
                    e.consume();
                });
            } else {
                upgradeButton.setDisable(true);
                // Optionally add a specific style class for better visual indication
                // e.g., upgradeButton.getStyleClass().add("disabled-upgrade-button");
                // Tooltip could also indicate why it's disabled
                upgradeButton
                        .setTooltip(new javafx.scene.control.Tooltip("Not enough gold! Needs: " + upgradeCost + "G"));
            }
            buttons.add(upgradeButton);
        }
        // Sell Button
        Button sellButton = UIAssets.createIconButton("Sell (+" + existingTower.getSellRefund() + "G)", 1, 0,
                POPUP_ICON_SIZE); // Sell icon (1,0)
        sellButton.setOnAction(e -> {
            gameController.sellTower(tileX, tileY);
            clearActivePopup();
            e.consume();
        });
        buttons.add(sellButton);

        // Close button
        Button closeButton = UIAssets.createIconButton("Close", 3, 0, POPUP_ICON_SIZE); // Close icon (3,0)
        closeButton.setOnAction(e -> {
            clearActivePopup();
            e.consume();
        });
        buttons.add(closeButton);

        int numButtons = buttons.size();
        double angleStep = numButtons > 1 ? ((numButtons == 2) ? 60 : 360.0 / numButtons) : 0; // Adjust for few buttons
        if (numButtons == 2)
            angleStart = -120; // Adjust start angle for 2 buttons to be bottom-ish
        if (numButtons == 3 && buttons.get(0).getTooltip().getText().startsWith("Upgrade"))
            angleStart = -90; // Standard for 3
        else if (numButtons == 2 && buttons.get(0).getTooltip().getText().startsWith("Sell"))
            angleStart = -60; // Only sell and close, put them side by side nicely

        for (int i = 0; i < numButtons; i++) {
            Button button = buttons.get(i);
            double angle = angleStart + (i * angleStep);
            double buttonX = centerXScreen + radius * Math.cos(Math.toRadians(angle)) - POPUP_ICON_SIZE / 2.0;
            double buttonY = centerYScreen + radius * Math.sin(Math.toRadians(angle)) - POPUP_ICON_SIZE / 2.0;
            button.setLayoutX(buttonX);
            button.setLayoutY(buttonY);
            popupPane.getChildren().add(button);
        }

        activePopup = popupPane;
        uiOverlayPane.getChildren().add(activePopup);
        // Apply animation
        FadeTransition ft = new FadeTransition(Duration.millis(150), activePopup);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(150), activePopup);
        st.setFromX(0.7);
        st.setFromY(0.7);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        ParallelTransition pt = new ParallelTransition(ft, st);
        pt.play();
    }

    public void stop() { // Assuming this method exists or should be added for cleanup
        if (renderTimer != null) {
            renderTimer.stop();
        }
        if (topBarUpdateTimer != null) {
            topBarUpdateTimer.stop();
        }
        gameController.stopGame(); // Ensure controller's game loop is also stopped
    }

    private void showGameSettingsPopup() {
        clearActivePopup(); // Clear any existing popups like tower build/upgrade

        VBox settingsPopup = new VBox(10);
        settingsPopup.setPadding(new Insets(20));
        settingsPopup.setAlignment(Pos.CENTER);
        settingsPopup.getStyleClass().add("options-section"); // Reuse style for consistent look
        settingsPopup
                .setStyle("-fx-background-color: rgba(30, 30, 30, 0.95); -fx-border-color: #555; -fx-border-width: 2;"); // More
                                                                                                                         // distinct
                                                                                                                         // popup
                                                                                                                         // style

        Label title = new Label("Game Menu");
        title.getStyleClass().add("options-title"); // Reuse style

        Button saveButton = new Button("Save Game");
        saveButton.getStyleClass().addAll("button", "action-button");
        saveButton.setPrefWidth(200);
        saveButton.setOnAction(e -> {
            System.out.println("Save Game clicked (Not implemented yet)");
            // TODO: Implement save game logic
            // gameController.saveGame("savefile.dat");
            // renderTimer.setStatusMessage("Game Saved!");
            clearActivePopup();
            e.consume();
        });

        Button loadButton = new Button("Load Game");
        loadButton.getStyleClass().addAll("button", "action-button");
        loadButton.setPrefWidth(200);
        loadButton.setOnAction(e -> {
            System.out.println("Load Game clicked (Not implemented yet)");
            // TODO: Implement load game logic
            // gameController.loadGame("savefile.dat");
            // renderTimer.setStatusMessage("Game Loaded!");
            // Need to refresh UI elements based on loaded state
            clearActivePopup();
            e.consume();
        });

        Button resumeButton = new Button("Resume Game");
        resumeButton.getStyleClass().addAll("button", "secondary-button");
        resumeButton.setPrefWidth(200);
        resumeButton.setOnAction(e -> {
            clearActivePopup();
            e.consume();
        });

        Button mainMenuButton = new Button("Back to Main Menu");
        mainMenuButton.getStyleClass().addAll("button", "cancel-button");
        mainMenuButton.setPrefWidth(200);
        mainMenuButton.setOnAction(e -> {
            stop(); // Stop game screen timers and controller game loop
            MainMenuScreen mainMenu = new MainMenuScreen(primaryStage);

            double targetWidth = primaryStage.getScene() != null ? primaryStage.getScene().getWidth()
                    : primaryStage.getWidth();
            double targetHeight = primaryStage.getScene() != null ? primaryStage.getScene().getHeight()
                    : primaryStage.getHeight();
            Scene scene = new Scene(mainMenu, targetWidth, targetHeight);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            ImageCursor customCursor = UIAssets.getCustomCursor();
            if (customCursor != null)
                scene.setCursor(customCursor);
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(true); // Always set/maintain fullscreen

            e.consume();
        });

        settingsPopup.getChildren().addAll(title, saveButton, loadButton, resumeButton, mainMenuButton);

        // Position in center of screen (relative to uiOverlayPane)
        settingsPopup.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            settingsPopup.setLayoutX((uiOverlayPane.getWidth() - newVal.getWidth()) / 2);
            settingsPopup.setLayoutY((uiOverlayPane.getHeight() - newVal.getHeight()) / 2);
        });

        activePopup = settingsPopup;
        uiOverlayPane.getChildren().add(activePopup);

        // Apply animation
        FadeTransition ft = new FadeTransition(Duration.millis(200), activePopup);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), activePopup);
        st.setFromX(0.7);
        st.setFromY(0.7);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        ParallelTransition pt = new ParallelTransition(ft, st);
        pt.play();
    }

    // Getter for visualMapWidth (optional, but good practice)
    public double getVisualMapWidth() {
        return visualMapWidthProperty.get();
    }

    // Property getter for visualMapWidth (needed for bindings)
    public ReadOnlyDoubleProperty visualMapWidthProperty() {
        return visualMapWidthProperty.getReadOnlyProperty();
    }
}
