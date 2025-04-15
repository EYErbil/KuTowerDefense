package towerdefense.controller;

import towerdefense.view.screens.GameScreen;

/**
 * Controller for game-related operations.
 * Manages game flow, state, and interactions.
 */
public class GameController {

    private GameScreen gameScreen;

    /**
     * Constructor for GameController.
     */
    public GameController() {
        // Initialize components
    }

    /**
     * Start a new game session.
     */
    public void startNewGame() {
        System.out.println("GameController: Starting new game");

        // Create and show game screen
        if (gameScreen == null) {
            gameScreen = new GameScreen();
        }

        // Display map selection to the player
        showMapSelection();
    }

    /**
     * Show map selection to the player.
     */
    private void showMapSelection() {
        System.out.println("GameController: Showing map selection");
        // This would load map list and display a selection dialog
        // For now, just simulate selection of a default map
        loadSelectedMap("default_map");
    }

    /**
     * Load the selected map and start the game session.
     * 
     * @param mapId Identifier for the selected map
     */
    private void loadSelectedMap(String mapId) {
        System.out.println("GameController: Loading map: " + mapId);
        // This would load the map data, initialize game session, etc.

        // For now, just show the game screen
        gameScreen.initialize();
        gameScreen.setVisible(true);
    }
}