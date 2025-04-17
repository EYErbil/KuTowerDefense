package towerdefense.view.screens;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    private final String buttonStyle = "-fx-background-color: #5a3d2b; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-background-radius: 3; -fx-border-color: #3e2c1d; -fx-border-width: 1; -fx-border-radius: 3;";
    private final String buttonHoverStyle = "-fx-background-color: #7c553f; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-background-radius: 3; -fx-border-color: #3e2c1d; -fx-border-width: 1; -fx-border-radius: 3;";

    // Constructor accepts the controller
    public OptionsScreen(OptionsController controller) {
        this.controller = controller;
        initializeUI();
        loadInitialValues(); // Load initial/default values after UI is built
    }

    private void initializeUI() {
        view = new BorderPane();
        view.setPadding(new Insets(20));
        // Use a slightly lighter parchment/wood color
        view.setStyle("-fx-background-color: #b89d7a; -fx-border-color: #4a3b2a; -fx-border-width: 5;");

        // Title
        Label titleLabel = new Label("Game Options");
        titleLabel.setFont(Font.font("Times New Roman", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: #4a3b2a;");
        titleLabel.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        view.setTop(titleLabel);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 30, 0));

        // --- Options Grid ---
        GridPane optionsGrid = new GridPane();
        optionsGrid.setHgap(15);
        optionsGrid.setVgap(18);
        optionsGrid.setPadding(new Insets(15));
        // Style the grid background like a panel
        optionsGrid.setStyle(
                "-fx-background-color: #d4c0a1; -fx-background-radius: 8; -fx-border-color: #7a5c3a; -fx-border-radius: 8; -fx-border-width: 2;");

        // Style Labels within the grid
        String labelStyle = "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 14px; -fx-text-fill: #3e2c1d;";

        // --- Audio Settings ---
        int rowIndex = 0;
        optionsGrid.add(createSectionLabel("Audio Settings"), 0, rowIndex++, 3, 1);
        Label musicL = new Label("Music Volume:");
        musicL.setStyle(labelStyle);
        optionsGrid.add(musicL, 0, rowIndex);
        musicVolumeSlider = new Slider(0, 100, 75);
        musicVolumeSlider.setShowTickMarks(true);
        musicVolumeSlider.setShowTickLabels(true);
        musicVolumeSlider.setMajorTickUnit(25);
        musicValueLabel = new Label(String.format("%.0f%%", musicVolumeSlider.getValue()));
        musicVolumeSlider.valueProperty().addListener(
                (obs, oldVal, newVal) -> musicValueLabel.setText(String.format("%.0f%%", newVal.doubleValue())));
        musicVolumeSlider.setStyle("-fx-base: #7c553f;"); // Style slider base
        HBox musicBox = new HBox(10, musicVolumeSlider, musicValueLabel);
        musicBox.setAlignment(Pos.CENTER_LEFT);
        optionsGrid.add(musicBox, 1, rowIndex++, 2, 1); // Span slider across 2 columns

        Label sfxL = new Label("SFX Volume:");
        sfxL.setStyle(labelStyle);
        optionsGrid.add(sfxL, 0, rowIndex);
        sfxVolumeSlider = new Slider(0, 100, 80);
        sfxVolumeSlider.setShowTickMarks(true);
        sfxVolumeSlider.setShowTickLabels(true);
        sfxVolumeSlider.setMajorTickUnit(25);
        sfxValueLabel = new Label(String.format("%.0f%%", sfxVolumeSlider.getValue()));
        sfxVolumeSlider.valueProperty().addListener(
                (obs, oldVal, newVal) -> sfxValueLabel.setText(String.format("%.0f%%", newVal.doubleValue())));
        sfxVolumeSlider.setStyle("-fx-base: #7c553f;");
        HBox sfxBox = new HBox(10, sfxVolumeSlider, sfxValueLabel);
        sfxBox.setAlignment(Pos.CENTER_LEFT);
        optionsGrid.add(sfxBox, 1, rowIndex++, 2, 1);

        // --- Video Settings ---
        optionsGrid.add(createSectionLabel("Video Settings"), 0, rowIndex++, 3, 1);
        fullscreenCheckbox = new CheckBox("Fullscreen");
        fullscreenCheckbox.setStyle(labelStyle); // Use label style for checkbox text
        optionsGrid.add(fullscreenCheckbox, 0, rowIndex++, 2, 1);

        // --- Gameplay Settings ---
        optionsGrid.add(createSectionLabel("Gameplay Settings"), 0, rowIndex++, 3, 1);
        Label diffL = new Label("Difficulty:");
        diffL.setStyle(labelStyle);
        optionsGrid.add(diffL, 0, rowIndex);
        difficultyComboBox = new ComboBox<>();
        difficultyComboBox.getItems().addAll("Easy", "Normal", "Hard");
        difficultyComboBox.setValue("Normal");
        difficultyComboBox.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-background-color: #e4d8c4; -fx-border-color: #7a5c3a;");
        optionsGrid.add(difficultyComboBox, 1, rowIndex++);

        view.setCenter(optionsGrid);

        // --- Bottom Buttons ---
        Button saveButton = createStyledButton("Save & Close");
        saveButton.setOnAction(e -> {
            // Call controller to save current values
            controller.saveOptions(
                    musicVolumeSlider.getValue(),
                    sfxVolumeSlider.getValue(),
                    fullscreenCheckbox.isSelected(),
                    difficultyComboBox.getValue());
            Main.loadMainMenuScreen(); // Go back to main menu
        });

        Button cancelButton = createStyledButton("Cancel");
        cancelButton.setOnAction(e -> Main.loadMainMenuScreen()); // Go back without saving

        Button defaultsButton = createStyledButton("Defaults");
        defaultsButton.setOnAction(e -> {
            // Call controller method
            controller.resetOptionsToDefaults();
            // Update UI with defaults provided by controller
            loadDefaultValues();
        });

        HBox buttonBox = new HBox(15, defaultsButton, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(30, 0, 0, 0));
        view.setBottom(buttonBox);
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial Black", 18));
        label.setStyle("-fx-text-fill: #5a3d2b;");
        label.setPadding(new Insets(15, 0, 5, 0));
        return label;
    }

    // Helper for styled buttons
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle(buttonStyle);
        button.setOnMouseEntered(e -> button.setStyle(buttonHoverStyle));
        button.setOnMouseExited(e -> button.setStyle(buttonStyle));
        button.setPrefHeight(35);
        return button;
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