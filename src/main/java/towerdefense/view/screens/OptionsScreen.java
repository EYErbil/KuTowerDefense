package towerdefense.view.screens;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import towerdefense.Main; // For navigation
import towerdefense.model.GameModel; // If options interact with model
import towerdefense.controller.OptionsController; // Import controller

/**
 * Provides the JavaFX UI components for the Options screen.
 */
public class OptionsScreen {

    // Keep references to controls if needed for saving/loading
    private Slider musicVolumeSlider;
    private Slider sfxVolumeSlider;
    private CheckBox fullscreenCheckbox;
    private ComboBox<String> difficultyComboBox;
    private Label musicValueLabel;
    private Label sfxValueLabel;

    private BorderPane view;
    private OptionsController controller; // Store the controller

    // Constructor accepts the controller
    public OptionsScreen(OptionsController controller) {
        this.controller = controller;
        initializeUI();
        loadInitialValues(); // Load initial/default values after UI is built
    }

    private void initializeUI() {
        view = new BorderPane();
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #f4f4f4;"); // Example background

        // Title
        Label titleLabel = new Label("Options");
        titleLabel.setFont(Font.font("Arial", 24));
        titleLabel.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        view.setTop(titleLabel);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // --- Options Grid ---
        GridPane optionsGrid = new GridPane();
        optionsGrid.setHgap(10);
        optionsGrid.setVgap(15);
        optionsGrid.setPadding(new Insets(10));

        // --- Audio Settings ---
        int rowIndex = 0;
        optionsGrid.add(createSectionLabel("Audio Settings"), 0, rowIndex++, 3, 1);

        // Music Volume
        optionsGrid.add(new Label("Music Volume:"), 0, rowIndex);
        musicVolumeSlider = new Slider(0, 100, 75);
        musicVolumeSlider.setShowTickMarks(true);
        musicVolumeSlider.setShowTickLabels(true);
        musicVolumeSlider.setMajorTickUnit(25);
        musicValueLabel = new Label(String.format("%.0f%%", musicVolumeSlider.getValue()));
        musicVolumeSlider.valueProperty().addListener(
                (obs, oldVal, newVal) -> musicValueLabel.setText(String.format("%.0f%%", newVal.doubleValue())));
        HBox musicBox = new HBox(10, musicVolumeSlider, musicValueLabel);
        musicBox.setAlignment(Pos.CENTER_LEFT);
        optionsGrid.add(musicBox, 1, rowIndex++);

        // SFX Volume
        optionsGrid.add(new Label("SFX Volume:"), 0, rowIndex);
        sfxVolumeSlider = new Slider(0, 100, 80);
        sfxVolumeSlider.setShowTickMarks(true);
        sfxVolumeSlider.setShowTickLabels(true);
        sfxVolumeSlider.setMajorTickUnit(25);
        sfxValueLabel = new Label(String.format("%.0f%%", sfxVolumeSlider.getValue()));
        sfxVolumeSlider.valueProperty().addListener(
                (obs, oldVal, newVal) -> sfxValueLabel.setText(String.format("%.0f%%", newVal.doubleValue())));
        HBox sfxBox = new HBox(10, sfxVolumeSlider, sfxValueLabel);
        sfxBox.setAlignment(Pos.CENTER_LEFT);
        optionsGrid.add(sfxBox, 1, rowIndex++);

        // --- Video Settings ---
        optionsGrid.add(createSectionLabel("Video Settings"), 0, rowIndex++, 3, 1);
        fullscreenCheckbox = new CheckBox("Fullscreen");
        optionsGrid.add(fullscreenCheckbox, 0, rowIndex++);

        // --- Gameplay Settings ---
        optionsGrid.add(createSectionLabel("Gameplay Settings"), 0, rowIndex++, 3, 1);
        optionsGrid.add(new Label("Difficulty:"), 0, rowIndex);
        difficultyComboBox = new ComboBox<>();
        difficultyComboBox.getItems().addAll("Easy", "Normal", "Hard");
        difficultyComboBox.setValue("Normal");
        optionsGrid.add(difficultyComboBox, 1, rowIndex++);

        view.setCenter(optionsGrid);

        // --- Bottom Buttons ---
        Button saveButton = new Button("Save & Close");
        saveButton.setOnAction(e -> {
            // Call controller to save current values
            controller.saveOptions(
                    musicVolumeSlider.getValue(),
                    sfxVolumeSlider.getValue(),
                    fullscreenCheckbox.isSelected(),
                    difficultyComboBox.getValue());
            Main.loadMainMenuScreen(); // Go back to main menu
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> Main.loadMainMenuScreen()); // Go back without saving

        Button defaultsButton = new Button("Restore Defaults");
        defaultsButton.setOnAction(e -> {
            // Call controller method
            controller.resetOptionsToDefaults();
            // Update UI with defaults provided by controller
            loadDefaultValues();
        });

        HBox buttonBox = new HBox(10, defaultsButton, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        view.setBottom(buttonBox);
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 16));
        label.setStyle("-fx-font-weight: bold;");
        label.setPadding(new Insets(10, 0, 5, 0));
        return label;
    }

    /**
     * Loads initial values from the controller (called after UI init).
     */
    private void loadInitialValues() {
        // TODO: Get actual loaded values from controller if loadOptions is implemented
        loadDefaultValues(); // For now, just load defaults
    }

    /**
     * Loads default values from the controller.
     */
    private void loadDefaultValues() {
        musicVolumeSlider.setValue(controller.getDefaultMusicVolume());
        sfxVolumeSlider.setValue(controller.getDefaultSfxVolume());
        fullscreenCheckbox.setSelected(controller.getDefaultFullscreen());
        difficultyComboBox.setValue(controller.getDefaultDifficulty());
    }

    /**
     * Returns the root node of the options UI.
     */
    public Parent getView() {
        if (view == null) {
            initializeUI();
        }
        return view;
    }
}