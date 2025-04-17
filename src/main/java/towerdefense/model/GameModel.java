package towerdefense.model;

/**
 * GameModel class represents the main game state and logic.
 */
public class GameModel {
    // Add game state variables and methods here
    private int lives;
    private int gold;
    private int currentWave;

    public GameModel() {
        // Initialize default game state
        this.lives = 20; // Example default
        this.gold = 100; // Example default
        this.currentWave = 0;
        System.out.println("GameModel initialized.");
    }

    /**
     * Checks if the game over condition is met (e.g., lives <= 0).
     * 
     * @return true if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        // TODO: Implement actual game over logic
        // return this.lives <= 0;
        return false; // Placeholder
    }

    /**
     * Checks if the game won condition is met (e.g., all waves defeated).
     * 
     * @return true if the game is won, false otherwise.
     */
    public boolean isGameWon() {
        // TODO: Implement actual game won logic
        // return currentWave > totalWaves && allEnemiesDefeated();
        return false; // Placeholder
    }

    // --- Placeholder Getters for UI Update Example --- //
    public int getLives() {
        return lives;
    }

    public int getGold() {
        return gold;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getTotalWaves() {
        return 10;
    } // Example placeholder
}