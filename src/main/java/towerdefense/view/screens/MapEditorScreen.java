package towerdefense.view.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import towerdefense.Main; // For navigation
import towerdefense.controller.MapEditorController; // Import controller
import javafx.application.Platform; // For UI updates

/**
 * Provides the JavaFX UI components for the Map Editor screen.
 */
public class MapEditorScreen {

    private BorderPane view;
    private Label statusLabel;
    private Label positionLabel;
    private GridPane mapGridPane; // Keep reference to update cells
    private MapEditorController controller; // Store the controller
    // Add references to map grid pane, selected tile, etc. as needed

    public MapEditorScreen(MapEditorController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        view = new BorderPane();
        view.setPadding(new Insets(10));

        // --- Toolbar ---
        ToolBar toolBar = new ToolBar();
        Button newButton = new Button("New");
        Button openButton = new Button("Open");
        Button saveButton = new Button("Save");
        Button validateButton = new Button("Validate");
        Button exitButton = new Button("Exit Editor");

        // Wire actions to controller
        newButton.setOnAction(e -> controller.handleNewMap());
        openButton.setOnAction(e -> controller.handleOpenMap());
        saveButton.setOnAction(e -> controller.handleSaveMap());
        validateButton.setOnAction(e -> controller.handleValidateMap());
        exitButton.setOnAction(e -> {
            // TODO: Add confirmation if unsaved changes exist
            Main.loadMainMenuScreen(); // Go back to main menu
        });

        toolBar.getItems().addAll(
                newButton, new Separator(),
                openButton, new Separator(),
                saveButton, new Separator(),
                validateButton, new Separator(),
                exitButton);
        view.setTop(toolBar);

        // --- Map Grid Placeholder ---
        mapGridPane = new GridPane(); // Assign to field
        mapGridPane.setGridLinesVisible(true); // For visual aid during development
        mapGridPane.setAlignment(Pos.CENTER);
        // TODO: Populate with map cells/tiles based on controller.currentMap
        buildMapGrid(); // Initial build

        ScrollPane mapScrollPane = new ScrollPane(mapGridPane);
        mapScrollPane.setFitToWidth(true);
        mapScrollPane.setFitToHeight(true);
        view.setCenter(mapScrollPane);
        BorderPane.setMargin(mapScrollPane, new Insets(10));

        // --- Tile Selector Panel ---
        VBox tileSelectorPanel = new VBox(10);
        tileSelectorPanel.setPadding(new Insets(10));
        tileSelectorPanel.setAlignment(Pos.TOP_CENTER);
        tileSelectorPanel.setStyle("-fx-border-color: grey; -fx-border-width: 1;");
        tileSelectorPanel.setPrefWidth(150);

        tileSelectorPanel.getChildren().add(new Label("Tile Types"));

        ToggleGroup tileToggleGroup = new ToggleGroup();
        String[] tileTypes = { "Path", "Tower Slot", "Start", "End", "Obstacle", "Erase" };
        for (String type : tileTypes) {
            ToggleButton tileButton = new ToggleButton(type);
            tileButton.setToggleGroup(tileToggleGroup);
            tileButton.setPrefWidth(120);
            tileButton.setOnAction(e -> {
                if (tileButton.isSelected()) {
                    controller.handleTileSelection(type); // Call controller
                    updateStatusLabel("Selected: " + type);
                }
            });
            tileSelectorPanel.getChildren().add(tileButton);
        }
        view.setRight(tileSelectorPanel);

        // --- Status Bar ---
        BorderPane statusPane = new BorderPane();
        statusPane.setPadding(new Insets(5));
        statusLabel = new Label("Ready.");
        positionLabel = new Label("Position: -, -");
        statusPane.setLeft(statusLabel);
        statusPane.setRight(positionLabel);
        view.setBottom(statusPane);
    }

    /**
     * Builds or rebuilds the visual map grid based on controller data.
     */
    private void buildMapGrid() {
        mapGridPane.getChildren().clear(); // Clear previous grid
        // TODO: Get dimensions from controller.currentMap or defaults
        int numRows = 15; // Example
        int numCols = 20; // Example

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(32, 32);
                // TODO: Set cell style/content based on controller.currentMap.getTile(row, col)
                cell.setStyle("-fx-border-color: lightgrey;");
                final int r = row;
                final int c = col;
                cell.setOnMouseClicked(event -> {
                    controller.handleMapGridClick(r, c); // Call controller
                    updatePositionLabel(c, r);
                    // TODO: Update cell visual based on successful placement (view might query
                    // controller again)
                });
                mapGridPane.add(cell, col, row);
            }
        }
    }

    // --- Status Update Methods ---
    private void updateStatusLabel(String status) {
        Platform.runLater(() -> statusLabel.setText(status)); // Ensure UI update on FX thread
    }

    private void updatePositionLabel(int col, int row) {
        Platform.runLater(() -> positionLabel.setText(String.format("Position: %d, %d", col, row)));
    }

    /**
     * Returns the root node of the map editor UI.
     */
    public Parent getView() {
        if (view == null) {
            initializeUI();
        }
        return view;
    }
}