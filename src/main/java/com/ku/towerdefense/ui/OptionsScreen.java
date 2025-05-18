package com.ku.towerdefense.ui;

import com.ku.towerdefense.util.GameSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.ImageCursor;

/**
 * Options screen for configuring game parameters.
 */
public class OptionsScreen extends BorderPane {
    private final Stage primaryStage;
    private final GameSettings settings;
    
    /**
     * Constructor for the options screen.
     *
     * @param primaryStage the primary stage of the application
     */
    public OptionsScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.settings = GameSettings.getInstance();
        initializeUI();
    }
    
    /**
     * Initialize the user interface components.
     */
    private void initializeUI() {
        getStyleClass().add("options-screen");
        setPadding(new Insets(20));
        
        // Create title
        Text titleText = new Text("Game Options");
        titleText.getStyleClass().add("screen-title");
        
        // Create options grid
        GridPane optionsGrid = createOptionsGrid();
        
        // Wrap in scroll pane for many options
        ScrollPane scrollPane = new ScrollPane(optionsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        // Bottom buttons
        HBox buttonBar = createButtonBar();
        
        // Set up layout
        VBox topSection = new VBox(20, titleText);
        topSection.setAlignment(Pos.CENTER);
        
        setTop(topSection);
        setCenter(scrollPane);
        setBottom(buttonBar);
    }
    
    /**
     * Create the grid of option controls.
     *
     * @return the grid pane with options
     */
    private GridPane createOptionsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        // Section: Wave Settings
        addSectionHeader(grid, "Wave Settings", 0);
        
        int row = 1;
        // Total number of waves
        addNumberOption(grid, "Total Waves:", settings.getTotalWaves(), 1, 30, 
            value -> settings.setTotalWaves((int) value), row++);
        
        // Groups per wave
        addNumberOption(grid, "Groups per Wave:", settings.getGroupsPerWave(), 1, 10, 
            value -> settings.setGroupsPerWave((int) value), row++);
        
        // Enemies per group
        addNumberOption(grid, "Enemies per Group:", settings.getEnemiesPerGroup(), 1, 20, 
            value -> settings.setEnemiesPerGroup((int) value), row++);
        
        // Wave delay
        addNumberOption(grid, "Wave Delay (ms):", settings.getWaveDelay(), 1000, 20000, 
            value -> settings.setWaveDelay((int) value), row++);
        
        // Group delay
        addNumberOption(grid, "Group Delay (ms):", settings.getGroupDelay(), 500, 10000, 
            value -> settings.setGroupDelay((int) value), row++);
        
        // Enemy delay
        addNumberOption(grid, "Enemy Delay (ms):", settings.getEnemyDelay(), 100, 2000, 
            value -> settings.setEnemyDelay((int) value), row++);
        
        // Section: Enemy Composition
        addSectionHeader(grid, "Enemy Composition", row++);
        
        // Goblin percentage
        addNumberOption(grid, "Goblin Percentage:", settings.getGoblinPercentage(), 0, 100, 
            value -> settings.setGoblinPercentage((int) value), row++);
        
        // Section: Economy
        addSectionHeader(grid, "Economy", row++);
        
        // Starting gold
        addNumberOption(grid, "Starting Gold:", settings.getStartingGold(), 50, 500, 
            value -> settings.setStartingGold((int) value), row++);
        
        // Gold per goblin
        addNumberOption(grid, "Gold per Goblin:", settings.getGoldPerGoblin(), 5, 50, 
            value -> settings.setGoldPerGoblin((int) value), row++);
        
        // Gold per knight
        addNumberOption(grid, "Gold per Knight:", settings.getGoldPerKnight(), 10, 100, 
            value -> settings.setGoldPerKnight((int) value), row++);
        
        // Section: Player Stats
        addSectionHeader(grid, "Player Stats", row++);
        
        // Starting hit points
        addNumberOption(grid, "Starting Lives:", settings.getStartingLives(), 1, 50, 
            value -> settings.setStartingLives((int) value), row++);
        
        // Section: Enemy Stats
        addSectionHeader(grid, "Enemy Stats", row++);
        
        // Goblin hit points
        addNumberOption(grid, "Goblin Health:", settings.getGoblinHealth(), 10, 300, 
            value -> settings.setGoblinHealth((int) value), row++);
        
        // Knight hit points
        addNumberOption(grid, "Knight Health:", settings.getKnightHealth(), 20, 500, 
            value -> settings.setKnightHealth((int) value), row++);
        
        // Goblin speed
        addNumberOption(grid, "Goblin Speed:", settings.getGoblinSpeed(), 1, 10, 
            value -> settings.setGoblinSpeed((int) value), row++);
        
        // Knight speed
        addNumberOption(grid, "Knight Speed:", settings.getKnightSpeed(), 1, 10, 
            value -> settings.setKnightSpeed((int) value), row++);
        
        // Section: Tower Stats
        addSectionHeader(grid, "Tower Stats", row++);
        
        // Archer tower cost
        addNumberOption(grid, "Archer Tower Cost:", settings.getArcherTowerCost(), 10, 200, 
            value -> settings.setArcherTowerCost((int) value), row++);
        
        // Artillery tower cost
        addNumberOption(grid, "Artillery Tower Cost:", settings.getArtilleryTowerCost(), 20, 300, 
            value -> settings.setArtilleryTowerCost((int) value), row++);
        
        // Mage tower cost
        addNumberOption(grid, "Mage Tower Cost:", settings.getMageTowerCost(), 15, 250, 
            value -> settings.setMageTowerCost((int) value), row++);
        
        // Archer tower damage
        addNumberOption(grid, "Archer Tower Damage:", settings.getArcherTowerDamage(), 5, 100, 
            value -> settings.setArcherTowerDamage((int) value), row++);
        
        // Artillery tower damage
        addNumberOption(grid, "Artillery Tower Damage:", settings.getArtilleryTowerDamage(), 10, 150, 
            value -> settings.setArtilleryTowerDamage((int) value), row++);
        
        // Mage tower damage
        addNumberOption(grid, "Mage Tower Damage:", settings.getMageTowerDamage(), 8, 120, 
            value -> settings.setMageTowerDamage((int) value), row++);
        
        // Archer tower range
        addNumberOption(grid, "Archer Tower Range:", settings.getArcherTowerRange(), 50, 300, 
            value -> settings.setArcherTowerRange((int) value), row++);
        
        // Artillery tower range
        addNumberOption(grid, "Artillery Tower Range:", settings.getArtilleryTowerRange(), 40, 250, 
            value -> settings.setArtilleryTowerRange((int) value), row++);
        
        // Mage tower range
        addNumberOption(grid, "Mage Tower Range:", settings.getMageTowerRange(), 45, 280, 
            value -> settings.setMageTowerRange((int) value), row++);
        
        // Artillery tower AOE range
        addNumberOption(grid, "Artillery Tower AOE Range:", settings.getArtilleryAOERange(), 10, 100, 
            value -> settings.setArtilleryAOERange((int) value), row++);
        
        // Archer tower rate of fire
        addNumberOption(grid, "Archer Tower Fire Rate (ms):", settings.getArcherTowerFireRate(), 500, 3000, 
            value -> settings.setArcherTowerFireRate((int) value), row++);
        
        // Artillery tower rate of fire
        addNumberOption(grid, "Artillery Tower Fire Rate (ms):", settings.getArtilleryTowerFireRate(), 1000, 5000, 
            value -> settings.setArtilleryTowerFireRate((int) value), row++);
        
        // Mage tower rate of fire
        addNumberOption(grid, "Mage Tower Fire Rate (ms):", settings.getMageTowerFireRate(), 800, 4000, 
            value -> settings.setMageTowerFireRate((int) value), row++);
        
        return grid;
    }
    
    /**
     * Add a section header to the grid.
     *
     * @param grid the grid to add to
     * @param title the section title
     * @param row the row to add at
     */
    private void addSectionHeader(GridPane grid, String title, int row) {
        Text headerText = new Text(title);
        headerText.getStyleClass().add("options-section");
        grid.add(headerText, 0, row, 2, 1);
    }
    
    /**
     * Add a number option with slider to the grid.
     *
     * @param grid the grid to add to
     * @param label the option label
     * @param initialValue the initial value
     * @param min the minimum value
     * @param max the maximum value
     * @param setter the setter method to call when value changes
     * @param row the row to add at
     */
    private void addNumberOption(GridPane grid, String label, int initialValue, int min, int max, 
                                SliderChangeListener setter, int row) {
        // Label
        Label optionLabel = new Label(label);
        optionLabel.getStyleClass().add("options-label");
        grid.add(optionLabel, 0, row);
        
        // Current value display
        TextField valueField = new TextField(String.valueOf(initialValue));
        valueField.setPrefWidth(80);
        
        // Slider
        Slider slider = new Slider(min, max, initialValue);
        slider.setPrefWidth(200);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit((max - min) / 5);
        slider.setBlockIncrement(1);
        
        // Update value field when slider changes
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            int rounded = (int) Math.round(newValue.doubleValue());
            valueField.setText(String.valueOf(rounded));
            setter.setValue(rounded);
        });
        
        // Update slider when value field changes
        valueField.setOnAction(e -> {
            try {
                int value = Integer.parseInt(valueField.getText());
                if (value >= min && value <= max) {
                    slider.setValue(value);
                    setter.setValue(value);
                } else {
                    // Reset to valid range if out of bounds
                    valueField.setText(String.valueOf((int) slider.getValue()));
                }
            } catch (NumberFormatException ex) {
                // Reset if not a valid number
                valueField.setText(String.valueOf((int) slider.getValue()));
            }
        });
        
        // Arrange in a horizontal box
        HBox controlBox = new HBox(10, slider, valueField);
        grid.add(controlBox, 1, row);
    }
    
    /**
     * Create the bottom button bar.
     *
     * @return the button bar container
     */
    private HBox createButtonBar() {
        Button saveButton = new Button("Save Settings");
        saveButton.getStyleClass().add("action-button");
        saveButton.setOnAction(e -> saveSettings());
        
        Button defaultsButton = new Button("Reset to Defaults");
        defaultsButton.getStyleClass().add("secondary-button");
        defaultsButton.setOnAction(e -> resetToDefaults());
        
        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> goBack());
        
        HBox buttonBar = new HBox(10, backButton, defaultsButton, saveButton);
        buttonBar.setPadding(new Insets(20, 0, 0, 0));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        return buttonBar;
    }
    
    /**
     * Save the current settings.
     */
    private void saveSettings() {
        settings.saveSettings();
        goBack();
    }
    
    /**
     * Reset all settings to default values.
     */
    private void resetToDefaults() {
        settings.resetToDefaults();
        // Refresh the screen to show defaults
        primaryStage.setScene(new Scene(new OptionsScreen(primaryStage), 800, 600));
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
    
    /**
     * Interface for slider change listeners.
     */
    private interface SliderChangeListener {
        void setValue(int value);
    }
} 