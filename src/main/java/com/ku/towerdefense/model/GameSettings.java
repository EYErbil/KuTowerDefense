package com.ku.towerdefense.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Singleton class for managing game settings.
 */
public class GameSettings implements Serializable {
    private static final long serialVersionUID = 1L;
    private static GameSettings instance;
    
    // Game difficulty (1-5)
    private int difficulty = 2;
    
    // Audio settings
    private int soundVolume = 70;
    private int musicVolume = 50;
    
    // Game speed (1.0 is normal)
    private double gameSpeed = 1.0;
    
    // Tower stats
    private int archerTowerDamage = 10;
    private double archerTowerFireRate = 1.0; // shots per second
    private int archerTowerRange = 150;
    private int archerTowerCost = 50;
    
    private int artilleryTowerDamage = 25;
    private double artilleryTowerFireRate = 0.5; // shots per second
    private int artilleryTowerRange = 120;
    private int artilleryTowerSplashRadius = 60;
    private int artilleryTowerCost = 100;
    private int shellDamage = 30; // Damage for artillery shells
    private int artilleryAoeRange = 60; // Splash damage radius
    
    private int mageTowerDamage = 15;
    private double mageTowerFireRate = 0.8; // shots per second
    private int mageTowerRange = 140;
    private int mageTowerCost = 75;
    
    // Enemy stats
    private int goblinHealth = 50;
    private double goblinSpeed = 20; // Reduced from 80 pixels per second
    private int goblinGoldReward = 10;
    
    private int knightHealth = 100;
    private double knightSpeed = 15; // Reduced from 50 pixels per second
    private int knightGoldReward = 20;
    
    // Damage type modifiers (for weakness/resistance system)
    private double archerVsGoblinModifier = 1.5; // Archers deal more damage to goblins
    private double archerVsKnightModifier = 0.8; // Archers deal less damage to knights
    
    private double mageVsGoblinModifier = 0.8; // Mages deal less damage to goblins
    private double mageVsKnightModifier = 1.5; // Mages deal more damage to knights
    
    // Start settings
    private int startingLives = 20;
    private int startingGold = 100;
    
    // Wave settings
    private int enemiesPerGroup = 5; // Base number of enemies per wave
    private int goblinPercentage = 70; // Percentage of goblins in each wave
    private int enemyDelay = 1000; // Milliseconds between enemy spawns
    
    /**
     * Private constructor to prevent direct instantiation.
     */
    private GameSettings() {
        // Private constructor for singleton
    }
    
    /**
     * Get the singleton instance of GameSettings.
     * 
     * @return the game settings instance
     */
    public static synchronized GameSettings getInstance() {
        if (instance == null) {
            try {
                // Try to load saved settings
                instance = loadSettings();
            } catch (Exception e) {
                // If loading fails, create new settings
                instance = new GameSettings();
            }
        }
        return instance;
    }
    
    /**
     * Reset all settings to default values.
     */
    public void resetToDefaults() {
        difficulty = 2;
        soundVolume = 70;
        musicVolume = 50;
        gameSpeed = 1.0;
        
        // Reset tower stats
        archerTowerDamage = 10;
        archerTowerFireRate = 1.0;
        archerTowerRange = 150;
        archerTowerCost = 50;
        
        artilleryTowerDamage = 25;
        artilleryTowerFireRate = 0.5;
        artilleryTowerRange = 120;
        artilleryTowerSplashRadius = 60;
        artilleryTowerCost = 100;
        
        mageTowerDamage = 15;
        mageTowerFireRate = 0.8;
        mageTowerRange = 140;
        mageTowerCost = 75;
        
        // Reset enemy stats
        goblinHealth = 50;
        goblinSpeed = 20; // Reduced speed
        goblinGoldReward = 10;
        
        knightHealth = 100;
        knightSpeed = 15; // Reduced speed
        knightGoldReward = 20;
        
        // Reset damage modifiers
        archerVsGoblinModifier = 1.5;
        archerVsKnightModifier = 0.8;
        mageVsGoblinModifier = 0.8;
        mageVsKnightModifier = 1.5;
        
        // Reset starting settings
        startingLives = 20;
        startingGold = 100;
        
        // Reset wave settings
        enemiesPerGroup = 5;
        goblinPercentage = 70;
        enemyDelay = 1000;
        
        // Save the default settings
        saveSettings();
    }
    
    /**
     * Save settings to a file.
     * 
     * @return true if settings were saved successfully
     */
    public boolean saveSettings() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("gamesettings.dat"))) {
            out.writeObject(this);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load settings from a file.
     * 
     * @return the loaded settings or a new instance if loading fails
     */
    private static GameSettings loadSettings() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("gamesettings.dat"))) {
            return (GameSettings) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Creating new settings: " + e.getMessage());
            return new GameSettings();
        }
    }
    
    /**
     * Adjust stats based on difficulty level.
     * This should be called when difficulty is changed.
     */
    private void adjustStatsForDifficulty() {
        // Base multipliers (1.0 at difficulty 2)
        double enemyHealthMultiplier = 0.8 + (difficulty * 0.2); // 1.0, 1.2, 1.4, 1.6, 1.8
        double enemySpeedMultiplier = 0.9 + (difficulty * 0.1);  // 1.0, 1.1, 1.2, 1.3, 1.4
        double goldRewardMultiplier = 1.2 - (difficulty * 0.1);  // 1.0, 0.9, 0.8, 0.7, 0.6
        
        // Adjust enemy stats based on difficulty
        goblinHealth = (int)(50 * enemyHealthMultiplier);
        goblinSpeed = 20 * enemySpeedMultiplier; // Reduced from 80
        goblinGoldReward = (int)(10 * goldRewardMultiplier);
        
        knightHealth = (int)(100 * enemyHealthMultiplier);
        knightSpeed = 15 * enemySpeedMultiplier; // Reduced from 50
        knightGoldReward = (int)(20 * goldRewardMultiplier);
    }
    
    // Getters and setters
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        if (difficulty >= 1 && difficulty <= 5) {
            this.difficulty = difficulty;
            adjustStatsForDifficulty();
        }
    }
    
    public int getSoundVolume() {
        return soundVolume;
    }
    
    public void setSoundVolume(int soundVolume) {
        if (soundVolume >= 0 && soundVolume <= 100) {
            this.soundVolume = soundVolume;
        }
    }
    
    public int getMusicVolume() {
        return musicVolume;
    }
    
    public void setMusicVolume(int musicVolume) {
        if (musicVolume >= 0 && musicVolume <= 100) {
            this.musicVolume = musicVolume;
        }
    }
    
    public double getGameSpeed() {
        return gameSpeed;
    }
    
    public void setGameSpeed(double gameSpeed) {
        if (gameSpeed >= 0.5 && gameSpeed <= 2.0) {
            this.gameSpeed = gameSpeed;
        }
    }
    
    // Tower stats getters
    public int getArcherTowerDamage() {
        return archerTowerDamage;
    }
    
    /**
     * Get the arrow damage for archer towers.
     * 
     * @return arrow damage amount
     */
    public int getArrowDamage() {
        return archerTowerDamage; // Using the same value as archer tower damage
    }
    
    public double getArcherTowerFireRate() {
        return archerTowerFireRate;
    }
    
    public int getArcherTowerRange() {
        return archerTowerRange;
    }
    
    public int getArcherTowerCost() {
        return archerTowerCost;
    }
    
    public int getArtilleryTowerDamage() {
        return artilleryTowerDamage;
    }
    
    public double getArtilleryTowerFireRate() {
        return artilleryTowerFireRate;
    }
    
    public int getArtilleryTowerRange() {
        return artilleryTowerRange;
    }
    
    public int getArtilleryTowerSplashRadius() {
        return artilleryTowerSplashRadius;
    }
    
    public int getArtilleryTowerCost() {
        return artilleryTowerCost;
    }
    
    public int getMageTowerDamage() {
        return mageTowerDamage;
    }
    
    /**
     * Get the spell damage for mage towers.
     * 
     * @return spell damage amount
     */
    public int getSpellDamage() {
        return mageTowerDamage; // Using the same value as mage tower damage
    }
    
    public double getMageTowerFireRate() {
        return mageTowerFireRate;
    }
    
    public int getMageTowerRange() {
        return mageTowerRange;
    }
    
    public int getMageTowerCost() {
        return mageTowerCost;
    }
    
    // Enemy stats getters
    public int getGoblinHealth() {
        return goblinHealth;
    }
    
    public double getGoblinSpeed() {
        return goblinSpeed;
    }
    
    public int getGoblinGoldReward() {
        return goblinGoldReward;
    }
    
    public int getKnightHealth() {
        return knightHealth;
    }
    
    public double getKnightSpeed() {
        return knightSpeed;
    }
    
    public int getKnightGoldReward() {
        return knightGoldReward;
    }
    
    // Damage modifiers getters
    public double getArcherVsGoblinModifier() {
        return archerVsGoblinModifier;
    }
    
    public double getArcherVsKnightModifier() {
        return archerVsKnightModifier;
    }
    
    public double getMageVsGoblinModifier() {
        return mageVsGoblinModifier;
    }
    
    public double getMageVsKnightModifier() {
        return mageVsKnightModifier;
    }
    
    // Game start settings
    public int getStartingLives() {
        return startingLives;
    }
    
    public int getStartingGold() {
        return startingGold;
    }
    
    /**
     * Get the damage for artillery shells.
     * 
     * @return shell damage amount
     */
    public int getShellDamage() {
        return shellDamage;
    }
    
    /**
     * Get the area of effect range for artillery towers.
     * 
     * @return artillery AoE range in pixels
     */
    public int getArtilleryAoeRange() {
        return artilleryAoeRange;
    }
    
    // New getters for wave settings
    
    /**
     * Get the base number of enemies per wave.
     * 
     * @return number of enemies per group
     */
    public int getEnemiesPerGroup() {
        return enemiesPerGroup;
    }
    
    /**
     * Set the base number of enemies per wave.
     * 
     * @param enemiesPerGroup number of enemies per group
     */
    public void setEnemiesPerGroup(int enemiesPerGroup) {
        if (enemiesPerGroup > 0) {
            this.enemiesPerGroup = enemiesPerGroup;
        }
    }
    
    /**
     * Get the percentage of goblins in each wave.
     * 
     * @return goblin percentage (0-100)
     */
    public int getGoblinPercentage() {
        return goblinPercentage;
    }
    
    /**
     * Set the percentage of goblins in each wave.
     * 
     * @param goblinPercentage goblin percentage (0-100)
     */
    public void setGoblinPercentage(int goblinPercentage) {
        if (goblinPercentage >= 0 && goblinPercentage <= 100) {
            this.goblinPercentage = goblinPercentage;
        }
    }
    
    /**
     * Get the delay between enemy spawns in milliseconds.
     * 
     * @return enemy spawn delay in ms
     */
    public int getEnemyDelay() {
        return enemyDelay;
    }
    
    /**
     * Set the delay between enemy spawns in milliseconds.
     * 
     * @param enemyDelay enemy spawn delay in ms
     */
    public void setEnemyDelay(int enemyDelay) {
        if (enemyDelay > 0) {
            this.enemyDelay = enemyDelay;
        }
    }
} 