package towerdefense.controller;

import towerdefense.model.GameModel;
import towerdefense.view.screens.MainMenuScreen;

/**
 * Controller for the main menu screen.
 */
public class MainMenuController {
    private final MainMenuScreen view;
    private final GameModel model;

    public MainMenuController(MainMenuScreen view, GameModel model) {
        this.view = view;
        this.model = model;
    }

    public void startGame() {
        // Implement game start logic
    }

    public void openSettings() {
        // Implement settings opening logic
    }
}