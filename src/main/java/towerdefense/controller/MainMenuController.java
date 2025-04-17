package towerdefense.controller;

import towerdefense.Main; // Import Main for navigation
import towerdefense.model.GameModel;
// import towerdefense.view.screens.MainMenuScreen; // No longer needed directly
// import towerdefense.view.screens.MapSelectionScreen; // No longer needed directly

/**
 * Controller for the main menu screen.
 */
public class MainMenuController {
    // private final MainMenuScreen view; // Remove view reference
    private final GameModel model;

    // Constructor no longer needs the view
    public MainMenuController(/* MainMenuScreen view */ GameModel model) {
        // this.view = view;
        this.model = model;
    }

    public void startGame() {
        System.out.println("Start Game button clicked. Opening Map Selection...");
        // Close the main menu view - No longer needed here
        // if (view != null) {
        // view.close(); // Close the Stage
        // }

        // Open the Map Selection screen using Main's method
        Main.loadMapSelectionScreen();
    }

    public void openSettings() {
        System.out.println("Settings button clicked.");
        // Open the Options screen using Main's method
        Main.loadOptionsScreen();
    }

    // Add methods for other main menu buttons if needed (e.g., openMapEditor,
    // quitGame)
    public void openMapEditor() {
        System.out.println("Map Editor button clicked.");
        // Open the Map Editor screen using Main's method
        Main.loadMapEditorScreen();
    }

    // Add Quit Game method if needed (usually handled by Platform.exit() in view)
    // public void quitGame() {
    // System.out.println("Quit button clicked.");
    // Platform.exit();
    // System.exit(0);
    // }
}