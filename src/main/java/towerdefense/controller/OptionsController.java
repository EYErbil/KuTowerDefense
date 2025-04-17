package towerdefense.controller;

import towerdefense.model.GameModel;
// import towerdefense.view.screens.OptionsScreen; // No longer needed

/**
 * Controller for the options screen logic.
 * Handles saving and loading options.
 */
public class OptionsController {

    private GameModel model;
    // private OptionsScreen optionsScreen; // Remove field

    public OptionsController(GameModel model) {
        this.model = model;
        // Load existing options from model or preferences
        loadOptions();
    }

    /**
     * Handles the action of saving the current options.
     * Called by the OptionsScreen view.
     * 
     * @param musicVol   Music volume level.
     * @param sfxVol     SFX volume level.
     * @param fullscreen Fullscreen state.
     * @param difficulty Selected difficulty.
     */
    public void saveOptions(double musicVol, double sfxVol, boolean fullscreen, String difficulty) {
        System.out.println("Controller: Saving options...");
        System.out.println("  Music Volume: " + musicVol);
        System.out.println("  SFX Volume: " + sfxVol);
        System.out.println("  Fullscreen: " + fullscreen);
        System.out.println("  Difficulty: " + difficulty);
        // TODO: Implement actual saving logic (e.g., save to GameModel, Preferences
        // API)
        // model.setMusicVolume(musicVol);
        // ... etc ...
    }

    /**
     * Handles loading options (e.g., when controller is created).
     */
    private void loadOptions() {
        System.out.println("Controller: Loading options...");
        // TODO: Implement loading logic
        // Example:
        // double musicVol = model.getMusicVolume();
        // ... update view references if held ...
    }

    /**
     * Handles resetting options to default values.
     * Called by the OptionsScreen view.
     * Note: This method could return default values, or directly update model/view
     * if needed.
     */
    public void resetOptionsToDefaults() {
        System.out.println("Controller: Resetting options to defaults...");
        // TODO: Implement logic to reset options in the model or provide defaults
        // Example: provide default values back to the view to update sliders etc.
    }

    // Add getters if the view needs to pull initial values from controller after
    // load
    public double getDefaultMusicVolume() {
        return 75.0;
    }

    public double getDefaultSfxVolume() {
        return 80.0;
    }

    public boolean getDefaultFullscreen() {
        return false;
    }

    public String getDefaultDifficulty() {
        return "Normal";
    }
}