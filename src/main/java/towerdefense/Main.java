package towerdefense;

import javafx.application.Application;
import javafx.stage.Stage;
import towerdefense.model.GameModel;
import towerdefense.view.screens.MainMenuScreen;

/**
 * Main entry point for the Tower Defense game.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameModel model = new GameModel();
        MainMenuScreen mainMenu = new MainMenuScreen(model);
        mainMenu.showScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}