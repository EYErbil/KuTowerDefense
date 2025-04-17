package towerdefense.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import towerdefense.Main; // Import Main for navigation
import towerdefense.model.GameModel;
// import towerdefense.view.screens.GameScreen; // No longer needed directly
// import towerdefense.view.screens.MainMenuScreen; // No longer needed directly
// import javax.swing.SwingUtilities; // No longer needed here
// import javafx.stage.Stage; // No longer needed

/**
 * Controller for the Map Selection Screen.
 * Handles listing available maps and starting the game with the selected map.
 */
public class MapSelectionController {

    @FXML
    private ListView<String> mapListView; // Placeholder for map names

    @FXML
    private Button startGameButton;

    @FXML
    private Button backButton;

    private GameModel model; // Keep a reference if needed
    // private Stage stage; // Remove stage reference

    // Initialize no longer needs the stage
    public void initialize(GameModel model, /* Stage stage */ Void unused) {
        this.model = model;
        // this.stage = stage;
        // TODO: Populate mapListView with saved map files
        mapListView.getItems().add("Placeholder Map 1");
        mapListView.getItems().add("Placeholder Map 2");
        // Disable start button until a map is selected
        startGameButton.setDisable(true); // Start disabled
        mapListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            startGameButton.setDisable(newVal == null);
        });
    }

    @FXML
    private void handleStartGame() {
        String selectedMap = mapListView.getSelectionModel().getSelectedItem();
        if (selectedMap != null) {
            System.out.println("Starting game with map: " + selectedMap);
            // Load game screen using Main's method
            Main.loadGameScreen(selectedMap);
        } else {
            // Optional: Show an alert if no map is selected
            System.out.println("No map selected!");
            // Consider using JavaFX Alert dialog here
        }
    }

    @FXML
    private void handleBackButton() {
        System.out.println("Going back to Main Menu");
        // Load main menu screen using Main's method
        Main.loadMainMenuScreen();
    }
}