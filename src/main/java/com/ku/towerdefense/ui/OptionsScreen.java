package com.ku.towerdefense.ui;

import com.ku.towerdefense.util.GameSettings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region; // For USE_PREF_SIZE
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.ImageCursor;
import javafx.scene.transform.Scale; // Added for scaling

/**
 * Options screen for configuring game parameters.
 */
public class OptionsScreen extends BorderPane {
    private final Stage primaryStage;
    private final GameSettings settings;
    private static final double CONTENT_SCALE = 0.85; // Scale factor for the options boards area
    private static final double PREF_WRAP_HEIGHT = 700; // Approx unscaled height for 2 rows of boards
    private static final int ICON_BUTTON_SIZE = 80; // Size for bottom icon buttons, increased from 64

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

        // --- Top Section (Title) ---
        Text titleText = new Text("Game Options");
        titleText.getStyleClass().add("screen-title");
        VBox topSection = new VBox(titleText);
        topSection.setAlignment(Pos.CENTER);
        topSection.setMaxWidth(Double.MAX_VALUE);
        topSection.setPadding(new Insets(20, 0, 0, 0)); // Added 20px top padding
        setTop(topSection);
        BorderPane.setAlignment(topSection, Pos.CENTER);

        // --- Center Section (FlowPane with Boards & Buttons below) ---
        FlowPane optionsContainer = createOptionsContainer();
        // optionsContainer.setMaxHeight(Region.USE_PREF_SIZE); // Remove,
        // prefWrapLength will guide height

        optionsContainer.setScaleX(CONTENT_SCALE);
        optionsContainer.setScaleY(CONTENT_SCALE);

        HBox buttonBar = createButtonBar(); // Buttons will be centered by the parent VBox

        VBox centerContentVBox = new VBox(20 * CONTENT_SCALE); // Scale spacing too
        centerContentVBox.setPadding(new Insets(20 * CONTENT_SCALE)); // Scale padding
        centerContentVBox.setAlignment(Pos.CENTER); // Center FlowPane and ButtonBar horizontally
        centerContentVBox.getChildren().addAll(optionsContainer, buttonBar);

        setCenter(centerContentVBox);
    }

    /**
     * Create the FlowPane container for option group boards.
     *
     * @return the FlowPane with option boards
     */
    private FlowPane createOptionsContainer() {
        FlowPane flowPane = new FlowPane(Orientation.VERTICAL);
        flowPane.setHgap(20); // Original Hgap, will be scaled visually
        flowPane.setVgap(20); // Original Vgap, will be scaled visually
        flowPane.setAlignment(Pos.TOP_CENTER); // Center columns of boards if they don't fill width
        flowPane.getStyleClass().add("options-container-flowpane");
        flowPane.setPrefWrapLength(PREF_WRAP_HEIGHT); // Set preferred height for wrapping columns
        // flowPane.setPrefWrapLength(600); // Example: hint for wrapping height if
        // needed

        // --- Wave Settings Board ---
        VBox waveSettingsBoard = createOptionGroupBoard("Wave Settings");
        addNumberOption(waveSettingsBoard, "Total Waves:", settings.getTotalWaves(), 1, 30,
                v -> settings.setTotalWaves(v));
        addNumberOption(waveSettingsBoard, "Groups per Wave:", settings.getGroupsPerWave(), 1, 10,
                v -> settings.setGroupsPerWave(v));
        addNumberOption(waveSettingsBoard, "Enemies per Group:", settings.getEnemiesPerGroup(), 1, 20,
                v -> settings.setEnemiesPerGroup(v));
        addNumberOption(waveSettingsBoard, "Wave Delay (ms):", settings.getWaveDelay(), 1000, 20000,
                v -> settings.setWaveDelay(v));
        addNumberOption(waveSettingsBoard, "Group Delay (ms):", settings.getGroupDelay(), 500, 10000,
                v -> settings.setGroupDelay(v));
        addNumberOption(waveSettingsBoard, "Enemy Delay (ms):", settings.getEnemyDelay(), 100, 2000,
                v -> settings.setEnemyDelay(v));
        flowPane.getChildren().add(waveSettingsBoard);

        // --- Enemy Composition Board ---
        VBox enemyCompBoard = createOptionGroupBoard("Enemy Composition");
        addNumberOption(enemyCompBoard, "Goblin Percentage:", settings.getGoblinPercentage(), 0, 100,
                v -> settings.setGoblinPercentage(v));
        flowPane.getChildren().add(enemyCompBoard);

        // --- Economy Board ---
        VBox economyBoard = createOptionGroupBoard("Economy");
        addNumberOption(economyBoard, "Starting Gold:", settings.getStartingGold(), 50, 500,
                v -> settings.setStartingGold(v));
        addNumberOption(economyBoard, "Gold per Goblin:", settings.getGoldPerGoblin(), 5, 50,
                v -> settings.setGoldPerGoblin(v));
        addNumberOption(economyBoard, "Gold per Knight:", settings.getGoldPerKnight(), 10, 100,
                v -> settings.setGoldPerKnight(v));
        flowPane.getChildren().add(economyBoard);

        // --- Player Stats Board ---
        VBox playerStatsBoard = createOptionGroupBoard("Player Stats");
        addNumberOption(playerStatsBoard, "Starting Lives:", settings.getStartingLives(), 1, 50,
                v -> settings.setStartingLives(v));
        flowPane.getChildren().add(playerStatsBoard);

        // --- Enemy Stats Board ---
        VBox enemyStatsBoard = createOptionGroupBoard("Enemy Stats");
        addNumberOption(enemyStatsBoard, "Goblin Health:", settings.getGoblinHealth(), 10, 300,
                v -> settings.setGoblinHealth(v));
        addNumberOption(enemyStatsBoard, "Knight Health:", settings.getKnightHealth(), 20, 500,
                v -> settings.setKnightHealth(v));
        addNumberOption(enemyStatsBoard, "Goblin Speed:", settings.getGoblinSpeed(), 1, 10,
                v -> settings.setGoblinSpeed(v));
        addNumberOption(enemyStatsBoard, "Knight Speed:", settings.getKnightSpeed(), 1, 10,
                v -> settings.setKnightSpeed(v));
        flowPane.getChildren().add(enemyStatsBoard);

        // --- Tower Costs Board ---
        VBox towerCostsBoard = createOptionGroupBoard("Tower Costs");
        addNumberOption(towerCostsBoard, "Archer Tower Cost:", settings.getArcherTowerCost(), 10, 200,
                v -> settings.setArcherTowerCost(v));
        addNumberOption(towerCostsBoard, "Artillery Tower Cost:", settings.getArtilleryTowerCost(), 20, 300,
                v -> settings.setArtilleryTowerCost(v));
        addNumberOption(towerCostsBoard, "Mage Tower Cost:", settings.getMageTowerCost(), 15, 250,
                v -> settings.setMageTowerCost(v));
        flowPane.getChildren().add(towerCostsBoard);

        // --- Tower Damage Board ---
        VBox towerDamageBoard = createOptionGroupBoard("Tower Damage");
        addNumberOption(towerDamageBoard, "Archer Tower Damage:", settings.getArcherTowerDamage(), 5, 100,
                v -> settings.setArcherTowerDamage(v));
        addNumberOption(towerDamageBoard, "Artillery Tower Damage:", settings.getArtilleryTowerDamage(), 10, 150,
                v -> settings.setArtilleryTowerDamage(v));
        addNumberOption(towerDamageBoard, "Mage Tower Damage:", settings.getMageTowerDamage(), 8, 120,
                v -> settings.setMageTowerDamage(v));
        flowPane.getChildren().add(towerDamageBoard);

        // --- Tower Range Board ---
        VBox towerRangeBoard = createOptionGroupBoard("Tower Range");
        addNumberOption(towerRangeBoard, "Archer Tower Range:", settings.getArcherTowerRange(), 50, 300,
                v -> settings.setArcherTowerRange(v));
        addNumberOption(towerRangeBoard, "Artillery Tower Range:", settings.getArtilleryTowerRange(), 40, 250,
                v -> settings.setArtilleryTowerRange(v));
        addNumberOption(towerRangeBoard, "Mage Tower Range:", settings.getMageTowerRange(), 45, 280,
                v -> settings.setMageTowerRange(v));
        addNumberOption(towerRangeBoard, "Artillery Tower AOE Range:", settings.getArtilleryAOERange(), 10, 100,
                v -> settings.setArtilleryAOERange(v));
        flowPane.getChildren().add(towerRangeBoard);

        // --- Tower Fire Rate Board ---
        VBox towerFireRateBoard = createOptionGroupBoard("Tower Fire Rate");
        addNumberOption(towerFireRateBoard, "Archer Tower Fire Rate (ms):", settings.getArcherTowerFireRate(), 500,
                3000, v -> settings.setArcherTowerFireRate(v));
        addNumberOption(towerFireRateBoard, "Artillery Tower Fire Rate (ms):", settings.getArtilleryTowerFireRate(),
                1000, 5000, v -> settings.setArtilleryTowerFireRate(v));
        addNumberOption(towerFireRateBoard, "Mage Tower Fire Rate (ms):", settings.getMageTowerFireRate(), 800, 4000,
                v -> settings.setMageTowerFireRate(v));
        flowPane.getChildren().add(towerFireRateBoard);

        return flowPane;
    }

    /**
     * Creates a VBox styled as an option group board with a title.
     * 
     * @param title The title of the option group.
     * @return A VBox configured as an option board.
     */
    private VBox createOptionGroupBoard(String title) {
        VBox board = new VBox(15); // Original spacing, will be scaled visually
        board.getStyleClass().add("options-group-board");

        Text headerText = new Text(title);
        headerText.getStyleClass().add("options-group-title");

        VBox.setMargin(headerText, new Insets(0, 0, 10, 0)); // Original margin
        board.getChildren().add(headerText);
        return board;
    }

    /**
     * Add a number option with slider to the parent VBox (option board).
     *
     * @param parentBoard  the VBox board to add to
     * @param labelText    the option label
     * @param initialValue the initial value
     * @param min          the minimum value
     * @param max          the maximum value
     * @param setter       the setter method to call when value changes
     */
    private void addNumberOption(VBox parentBoard, String labelText, int initialValue, int min, int max,
            SliderChangeListener setter) {
        Label optionLabel = new Label(labelText);
        optionLabel.getStyleClass().add("options-label");

        TextField valueField = new TextField(String.valueOf(initialValue));
        valueField.setPrefWidth(60); // Original, will scale
        valueField.getStyleClass().add("options-value-field");

        Slider slider = new Slider(min, max, initialValue);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(Math.max(1, (max - min) / 4.0)); // Original
        slider.setBlockIncrement(1);
        slider.getStyleClass().add("options-slider");

        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            int rounded = (int) Math.round(newValue.doubleValue());
            valueField.setText(String.valueOf(rounded));
            setter.setValue(rounded);
        });

        valueField.setOnAction(e -> {
            try {
                int value = Integer.parseInt(valueField.getText());
                if (value >= min && value <= max) {
                    slider.setValue(value);
                    setter.setValue(value);
                } else {
                    valueField.setText(String.valueOf((int) slider.getValue()));
                }
            } catch (NumberFormatException ex) {
                valueField.setText(String.valueOf((int) slider.getValue()));
            }
        });

        HBox controlBox = new HBox(10, slider, valueField); // Original spacing
        controlBox.setAlignment(Pos.CENTER_LEFT);

        VBox optionLayout = new VBox(5, optionLabel, controlBox); // Original spacing
        optionLayout.setPadding(new Insets(0, 0, 10, 0)); // Original padding
        parentBoard.getChildren().add(optionLayout);
    }

    private HBox createButtonBar() {
        // Tooltip text, icon column, icon row, icon display size
        Button backButton = UIAssets.createIconButton("Back to Menu", 3, 0, ICON_BUTTON_SIZE);
        // backButton.getStyleClass().addAll("secondary-button"); // Let UIAssets handle
        // styling
        backButton.setOnAction(e -> goBack());

        Button defaultsButton = UIAssets.createIconButton("Reset to Defaults", 1, 0, ICON_BUTTON_SIZE);
        // defaultsButton.getStyleClass().addAll("secondary-button");
        defaultsButton.setOnAction(e -> resetToDefaults());

        Button saveButton = UIAssets.createIconButton("Save Settings", 2, 0, ICON_BUTTON_SIZE);
        // saveButton.getStyleClass().addAll("action-button");
        saveButton.setOnAction(e -> saveSettings());

        HBox buttonBar = new HBox(15 * CONTENT_SCALE, backButton, defaultsButton, saveButton);
        buttonBar.setAlignment(Pos.CENTER);
        return buttonBar;
    }

    private void saveSettings() {
        settings.saveSettings();
        goBack();
    }

    private void resetToDefaults() {
        settings.resetToDefaults();
        // Reload the options screen to reflect default values
        OptionsScreen newOptionsScreen = new OptionsScreen(primaryStage);
        Scene newScene = new Scene(newOptionsScreen, primaryStage.getScene().getWidth(),
                primaryStage.getScene().getHeight());
        newScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        ImageCursor customCursor = UIAssets.getCustomCursor();
        if (customCursor != null) {
            newScene.setCursor(customCursor);
        }
        primaryStage.setScene(newScene);
    }

    private void goBack() {
        MainMenuScreen mainMenu = new MainMenuScreen(primaryStage);
        Scene mainMenuScene = new Scene(mainMenu, primaryStage.getWidth(), primaryStage.getHeight());
        mainMenuScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        ImageCursor customCursor = UIAssets.getCustomCursor();
        if (customCursor != null) {
            mainMenuScene.setCursor(customCursor);
        }

        primaryStage.setScene(mainMenuScene);
    }

    @FunctionalInterface
    private interface SliderChangeListener {
        void setValue(int value);
    }
}