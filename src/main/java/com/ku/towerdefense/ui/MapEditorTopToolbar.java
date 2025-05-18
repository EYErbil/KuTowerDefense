package com.ku.towerdefense.ui;

import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.TileType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Manages the top toolbar UI for the Map Editor.
 */
public class MapEditorTopToolbar extends HBox {

    private GameMap gameMap;
    private final TextField mapNameField;
    private final TextField widthField;
    private final TextField heightField;

    private EventHandler<ActionEvent> onSetStartHandler;
    private EventHandler<ResizeEventArgs> onResizeHandler;

    public MapEditorTopToolbar(GameMap initialMap) {
        super(10); // Spacing for HBox
        this.gameMap = initialMap;
        getStyleClass().add("editor-top-toolbar"); // Added style class

        setPadding(new Insets(10, 15, 15, 15));
        setAlignment(Pos.CENTER_LEFT);

        // Background styling will be handled by CSS via the "editor-top-toolbar" class
        // try {
        //     Image ribbonImage = new Image(getClass().getResourceAsStream("/Asset_pack/UI/Ribbon_Blue_3Slides.png"));
        //     BackgroundImage bgImage = new BackgroundImage(
        //             ribbonImage,
        //             BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
        //             BackgroundPosition.DEFAULT,
        //             new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true));
        //     setBackground(new Background(bgImage));
        // } catch (Exception e) {
        //     System.err.println("Failed to load ribbon background for top toolbar: " + e.getMessage());
        //     setStyle("-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); -fx-background-radius: 5px;");
        // }

        // --- Map Name ---
        VBox nameBox = new VBox(5);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label mapNameLabel = new Label("Map Name:");
        // mapNameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); // Removed inline style
        mapNameLabel.getStyleClass().add("toolbar-label"); // Added style class
        mapNameField = new TextField(gameMap.getName());
        mapNameField.textProperty().addListener((obs, old, newName) -> gameMap.setName(newName));
        mapNameField.setPrefWidth(200);
        nameBox.getChildren().addAll(mapNameLabel, mapNameField);

        // --- Size Controls ---
        VBox sizeBox = new VBox(5);
        sizeBox.setAlignment(Pos.CENTER_LEFT);
        HBox dimensionLabels = new HBox(10);
        dimensionLabels.setAlignment(Pos.CENTER_LEFT);
        Label widthLabel = new Label("Width:");
        // widthLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); // Removed inline style
        widthLabel.getStyleClass().add("toolbar-label"); // Added style class
        Label heightLabel = new Label("Height:");
        // heightLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); // Removed inline style
        heightLabel.getStyleClass().add("toolbar-label"); // Added style class
        dimensionLabels.getChildren().addAll(widthLabel, heightLabel);

        HBox dimensionFields = new HBox(10);
        dimensionFields.setAlignment(Pos.CENTER_LEFT);
        widthField = new TextField(String.valueOf(gameMap.getWidth()));
        widthField.setPrefWidth(80);
        heightField = new TextField(String.valueOf(gameMap.getHeight()));
        heightField.setPrefWidth(80);
        dimensionFields.getChildren().addAll(widthField, heightField);
        sizeBox.getChildren().addAll(dimensionLabels, dimensionFields);

        // --- Resize Button ---
        VBox resizeBox = new VBox();
        resizeBox.setAlignment(Pos.CENTER);
        resizeBox.setPadding(new Insets(0, 0, 0, 10));
        Button resizeButton = new Button("Resize Map");
        resizeButton.getStyleClass().add("action-button"); // This class exists in style.css
        resizeButton.setOnAction(e -> {
            if (onResizeHandler != null) {
                try {
                    int width = Integer.parseInt(widthField.getText());
                    int height = Integer.parseInt(heightField.getText());
                    if (width > 0 && height > 0) {
                        onResizeHandler.handle(new ResizeEventArgs(width, height));
                    } else {
                        System.err.println("Resize dimensions must be positive.");
                        // Maybe show an alert?
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid resize dimensions: " + ex.getMessage());
                    // Maybe show an alert?
                }
            }
        });
        resizeBox.getChildren().add(resizeButton);

        // --- Set Start Button ---
        Button setStartButton = new Button("Set Start");
        setStartButton.getStyleClass().addAll("button", "secondary-button"); // Added style classes
        setStartButton.setTooltip(new Tooltip("Click on the map edge to set the enemy spawn point."));
        setStartButton.setOnAction(e -> {
            if (onSetStartHandler != null) {
                onSetStartHandler.handle(e);
            }
        });

        // --- Layout ---
        Region spacer1 = new Region();
        spacer1.setPrefWidth(20);
        Region spacer2 = new Region();
        spacer2.setPrefWidth(20);
        Region spacer3 = new Region();
        spacer3.setPrefWidth(20);

        getChildren().addAll(nameBox, spacer1, sizeBox, spacer2, resizeBox, spacer3, setStartButton);
    }

    // --- Update Methods ---

    /**
     * Updates the map reference used by the toolbar and refreshes the UI fields.
     * 
     * @param newMap The new GameMap object.
     */
    public void setGameMap(GameMap newMap) {
        this.gameMap = newMap;
        // It's important to update the UI fields as well
        updateMapName(this.gameMap.getName());
        updateDimensions(this.gameMap.getWidth(), this.gameMap.getHeight());
        // Also update the listener on the map name field to affect the *new* map object
        mapNameField.textProperty().unbind(); // Remove old listener if any
        mapNameField.textProperty().addListener((obs, old, newName) -> this.gameMap.setName(newName));
    }

    /**
     * Updates the displayed map name in the text field.
     * 
     * @param name The new map name.
     */
    public void updateMapName(String name) {
        mapNameField.setText(name);
    }

    /**
     * Updates the displayed map dimensions in the text fields.
     * 
     * @param width  The new width.
     * @param height The new height.
     */
    public void updateDimensions(int width, int height) {
        widthField.setText(String.valueOf(width));
        heightField.setText(String.valueOf(height));
    }

    // --- Event Handlers ---

    public void setOnSetStart(EventHandler<ActionEvent> handler) {
        this.onSetStartHandler = handler;
    }

    public void setOnResize(EventHandler<ResizeEventArgs> handler) {
        this.onResizeHandler = handler;
    }

    // --- Inner class for Resize Event Arguments ---
    public static class ResizeEventArgs extends ActionEvent {
        private final int newWidth;
        private final int newHeight;

        public ResizeEventArgs(int newWidth, int newHeight) {
            super();
            this.newWidth = newWidth;
            this.newHeight = newHeight;
        }

        public int getNewWidth() {
            return newWidth;
        }

        public int getNewHeight() {
            return newHeight;
        }
    }
}