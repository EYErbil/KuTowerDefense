package com.ku.towerdefense;

import com.ku.towerdefense.ui.MainMenuScreen;
import com.ku.towerdefense.ui.UIAssets;
import javafx.application.Application;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the KU Tower Defense game.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize UI assets
        UIAssets.initialize();
        
        // Set up the primary stage
        primaryStage.setTitle("KU Tower Defense");
        primaryStage.setResizable(false);
        
        // Create and show main menu
        MainMenuScreen mainMenu = new MainMenuScreen(primaryStage);
        Scene scene = new Scene(mainMenu, 800, 600);
        
        // Add CSS if needed
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        
        // Set custom cursor if available
        ImageCursor customCursor = UIAssets.getCustomCursor();
        if (customCursor != null) {
            scene.setCursor(customCursor);
        }
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Main method that launches the application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
} 