package towerdefense.view.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import towerdefense.Main; // For navigation
import towerdefense.controller.MapEditorController; // Import controller
import javafx.application.Platform; // For UI updates
import javafx.scene.paint.Color; // Import Color

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
    private final String buttonStyle = "-fx-background-color: #6f4f2f; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 12px; -fx-background-radius: 3; -fx-border-color: #4a3b2a; -fx-border-width: 1; -fx-border-radius: 3;";
    private final String buttonHoverStyle = "-fx-background-color: #8a6e4b; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 12px; -fx-background-radius: 3; -fx-border-color: #4a3b2a; -fx-border-width: 1; -fx-border-radius: 3;";
    private final String toggleSelectedStyle = "-fx-background-color: #a08664; -fx-text-fill: black; -fx-font-family: 'Arial'; -fx-font-size: 12px; -fx-background-radius: 3; -fx-border-color: black; -fx-border-width: 1; -fx-border-radius: 3;";

    public MapEditorScreen(MapEditorController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        view = new BorderPane();
        view.setPadding(new Insets(10));
        view.setStyle("-fx-background-color: #9e8a70;"); // Dusty brown background

        // --- Toolbar ---
        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #c1a98a;");
        Button newButton = createStyledButton("New", buttonStyle, buttonHoverStyle);
        Button openButton = createStyledButton("Open", buttonStyle, buttonHoverStyle);
        Button saveButton = createStyledButton("Save", buttonStyle, buttonHoverStyle);
        Button validateButton = createStyledButton("Validate", buttonStyle, buttonHoverStyle);
        Button exitButton = createStyledButton("Exit Editor", buttonStyle, buttonHoverStyle);

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
        mapGridPane = new GridPane();
        mapGridPane.setStyle("-fx-background-color: #708090; -fx-grid-lines-visible: true;"); // Slate gray background
        mapGridPane.setAlignment(Pos.CENTER);
        // TODO: Populate with map cells/tiles based on controller.currentMap
        buildMapGrid(); // Initial build

        ScrollPane mapScrollPane = new ScrollPane(mapGridPane);
        mapScrollPane.setFitToWidth(true);
        mapScrollPane.setFitToHeight(true);
        view.setCenter(mapScrollPane);
        BorderPane.setMargin(mapScrollPane, new Insets(10));

        // --- Tile Selector Panel ---
        VBox tileSelectorPanel = new VBox(8);
        tileSelectorPanel.setPadding(new Insets(10));
        tileSelectorPanel.setAlignment(Pos.TOP_CENTER);
        tileSelectorPanel.setStyle(
                "-fx-background-color: #d4c0a1; -fx-border-color: #7a5c3a; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5;");
        tileSelectorPanel.setPrefWidth(150);
        Label tileSelectorTitle = new Label("Tile Palette");
        tileSelectorTitle.setFont(Font.font("Arial Black", FontWeight.BOLD, 14));
        tileSelectorTitle.setStyle("-fx-text-fill: #4a3b2a;");
        tileSelectorPanel.getChildren().add(tileSelectorTitle);
        tileSelectorPanel.getChildren().add(new Separator(javafx.geometry.Orientation.HORIZONTAL));

        ToggleGroup tileToggleGroup = new ToggleGroup();
        String[] tileTypes = { "Path", "Tower Slot", "Start", "End", "Obstacle", "Erase" };
        for (String type : tileTypes) {
            ToggleButton tileButton = createStyledToggleButton(type, tileToggleGroup, buttonStyle, toggleSelectedStyle,
                    buttonHoverStyle);
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
        statusPane.setPadding(new Insets(5, 10, 5, 10));
        statusPane.setStyle("-fx-background-color: #c1a98a;");
        statusLabel = new Label("Ready.");
        statusLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #3e2c1d;");
        positionLabel = new Label("Position: -, -");
        positionLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-text-fill: #3e2c1d;");
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
                // Example default cell style
                cell.setStyle("-fx-background-color: #aeb8c2; -fx-border-color: #5a6870;");
                // TODO: Set style based on actual map tile data
                final int r = row;
                final int c = col;
                cell.setOnMouseEntered(e -> cell.setStyle("-fx-background-color: #c8d0d8; -fx-border-color: #5a6870;")); // Hover
                                                                                                                         // effect
                cell.setOnMouseExited(e -> cell.setStyle("-fx-background-color: #aeb8c2; -fx-border-color: #5a6870;")); // TODO:
                                                                                                                        // Reset
                                                                                                                        // to
                                                                                                                        // actual
                                                                                                                        // tile
                                                                                                                        // style
                cell.setOnMouseClicked(event -> {
                    controller.handleMapGridClick(r, c); // Call controller
                    updatePositionLabel(c, r);
                    // TODO: Update cell visual permanently based on placed tile
                    // cell.setStyle(getStyleForTile(controller.getTileAt(r,c)));
                });
                mapGridPane.add(cell, col, row);
            }
        }
    }

    // Helper for styled Buttons
    private Button createStyledButton(String text, String defaultStyle, String hoverStyle) {
        Button button = new Button(text);
        button.setStyle(defaultStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(defaultStyle));
        return button;
    }

    // Helper for styled ToggleButtons
    private ToggleButton createStyledToggleButton(String text, ToggleGroup group, String defaultStyle,
            String selectedStyle, String hoverStyle) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(group);
        button.setStyle(defaultStyle);
        button.setPrefWidth(120);
        button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            button.setStyle(isSelected ? selectedStyle : defaultStyle);
        });
        button.setOnMouseEntered(e -> {
            if (!button.isSelected())
                button.setStyle(hoverStyle);
        });
        button.setOnMouseExited(e -> {
            if (!button.isSelected())
                button.setStyle(defaultStyle);
        });
        return button;
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