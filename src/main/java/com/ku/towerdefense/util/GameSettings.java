package com.ku.towerdefense.util;

import java.io.*;
import java.util.Properties;

/**
 * Singleton class to manage game settings and preferences.
 * Handles loading, saving, and providing default values for all game parameters.
 */
public class GameSettings implements Serializable {
    private static final long serialVersionUID = 1L;
    private static GameSettings instance;
    private static final String SETTINGS_FILE = "game_settings.properties";
    
    // Wave settings
    private int totalWaves = 10;
    private int groupsPerWave = 3;
    private int enemiesPerGroup = 5;
    private int waveDelay = 10000; // 10 seconds between waves
    private int groupDelay = 3000; // 3 seconds between groups
    private int enemyDelay = 500; // 0.5 seconds between individual enemies
    
    // Enemy composition
    private int goblinPercentage = 70; // 70% goblins, 30% knights
    
    // Economy
    private int startingGold = 100;
    private int goldPerGoblin = 10;
    private int goldPerKnight = 20;
    
    // Player stats
    private int startingLives = 20;
    
    // Enemy stats
    private int goblinHealth = 100;
    private int knightHealth = 200;
    private int goblinSpeed = 4;
    private int knightSpeed = 2;
    
    // Tower costs
    private int archerTowerCost = 50;
    private int artilleryTowerCost = 100;
    private int mageTowerCost = 75;
    
    // Tower damage
    private int archerTowerDamage = 20;
    private int artilleryTowerDamage = 40;
    private int mageTowerDamage = 30;
    
    // Tower ranges
    private int archerTowerRange = 150;
    private int artilleryTowerRange = 120;
    private int mageTowerRange = 140;
    private int artilleryAOERange = 50;
    
    // Tower fire rates (milliseconds)
    private int archerTowerFireRate = 800;
    private int artilleryTowerFireRate = 2000;
    private int mageTowerFireRate = 1200;
    
    /**
     * Private constructor for singleton pattern.
     */
    private GameSettings() {
        loadSettings();
    }
    
    /**
     * Get the singleton instance.
     *
     * @return the singleton instance
     */
    public static synchronized GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }
    
    /**
     * Load settings from file.
     */
    private void loadSettings() {
        Properties props = new Properties();
        
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            props.load(fis);
            
            // Wave settings
            totalWaves = getIntProperty(props, "totalWaves", totalWaves);
            groupsPerWave = getIntProperty(props, "groupsPerWave", groupsPerWave);
            enemiesPerGroup = getIntProperty(props, "enemiesPerGroup", enemiesPerGroup);
            waveDelay = getIntProperty(props, "waveDelay", waveDelay);
            groupDelay = getIntProperty(props, "groupDelay", groupDelay);
            enemyDelay = getIntProperty(props, "enemyDelay", enemyDelay);
            
            // Enemy composition
            goblinPercentage = getIntProperty(props, "goblinPercentage", goblinPercentage);
            
            // Economy
            startingGold = getIntProperty(props, "startingGold", startingGold);
            goldPerGoblin = getIntProperty(props, "goldPerGoblin", goldPerGoblin);
            goldPerKnight = getIntProperty(props, "goldPerKnight", goldPerKnight);
            
            // Player stats
            startingLives = getIntProperty(props, "startingLives", startingLives);
            
            // Enemy stats
            goblinHealth = getIntProperty(props, "goblinHealth", goblinHealth);
            knightHealth = getIntProperty(props, "knightHealth", knightHealth);
            goblinSpeed = getIntProperty(props, "goblinSpeed", goblinSpeed);
            knightSpeed = getIntProperty(props, "knightSpeed", knightSpeed);
            
            // Tower costs
            archerTowerCost = getIntProperty(props, "archerTowerCost", archerTowerCost);
            artilleryTowerCost = getIntProperty(props, "artilleryTowerCost", artilleryTowerCost);
            mageTowerCost = getIntProperty(props, "mageTowerCost", mageTowerCost);
            
            // Tower damage
            archerTowerDamage = getIntProperty(props, "archerTowerDamage", archerTowerDamage);
            artilleryTowerDamage = getIntProperty(props, "artilleryTowerDamage", artilleryTowerDamage);
            mageTowerDamage = getIntProperty(props, "mageTowerDamage", mageTowerDamage);
            
            // Tower ranges
            archerTowerRange = getIntProperty(props, "archerTowerRange", archerTowerRange);
            artilleryTowerRange = getIntProperty(props, "artilleryTowerRange", artilleryTowerRange);
            mageTowerRange = getIntProperty(props, "mageTowerRange", mageTowerRange);
            artilleryAOERange = getIntProperty(props, "artilleryAOERange", artilleryAOERange);
            
            // Tower fire rates
            archerTowerFireRate = getIntProperty(props, "archerTowerFireRate", archerTowerFireRate);
            artilleryTowerFireRate = getIntProperty(props, "artilleryTowerFireRate", artilleryTowerFireRate);
            mageTowerFireRate = getIntProperty(props, "mageTowerFireRate", mageTowerFireRate);
            
            System.out.println("Settings loaded successfully from file");
        } catch (IOException e) {
            System.out.println("Settings file not found, using defaults");
        }
    }
    
    /**
     * Save settings to file.
     */
    public void saveSettings() {
        Properties props = new Properties();
        
        // Wave settings
        props.setProperty("totalWaves", String.valueOf(totalWaves));
        props.setProperty("groupsPerWave", String.valueOf(groupsPerWave));
        props.setProperty("enemiesPerGroup", String.valueOf(enemiesPerGroup));
        props.setProperty("waveDelay", String.valueOf(waveDelay));
        props.setProperty("groupDelay", String.valueOf(groupDelay));
        props.setProperty("enemyDelay", String.valueOf(enemyDelay));
        
        // Enemy composition
        props.setProperty("goblinPercentage", String.valueOf(goblinPercentage));
        
        // Economy
        props.setProperty("startingGold", String.valueOf(startingGold));
        props.setProperty("goldPerGoblin", String.valueOf(goldPerGoblin));
        props.setProperty("goldPerKnight", String.valueOf(goldPerKnight));
        
        // Player stats
        props.setProperty("startingLives", String.valueOf(startingLives));
        
        // Enemy stats
        props.setProperty("goblinHealth", String.valueOf(goblinHealth));
        props.setProperty("knightHealth", String.valueOf(knightHealth));
        props.setProperty("goblinSpeed", String.valueOf(goblinSpeed));
        props.setProperty("knightSpeed", String.valueOf(knightSpeed));
        
        // Tower costs
        props.setProperty("archerTowerCost", String.valueOf(archerTowerCost));
        props.setProperty("artilleryTowerCost", String.valueOf(artilleryTowerCost));
        props.setProperty("mageTowerCost", String.valueOf(mageTowerCost));
        
        // Tower damage
        props.setProperty("archerTowerDamage", String.valueOf(archerTowerDamage));
        props.setProperty("artilleryTowerDamage", String.valueOf(artilleryTowerDamage));
        props.setProperty("mageTowerDamage", String.valueOf(mageTowerDamage));
        
        // Tower ranges
        props.setProperty("archerTowerRange", String.valueOf(archerTowerRange));
        props.setProperty("artilleryTowerRange", String.valueOf(artilleryTowerRange));
        props.setProperty("mageTowerRange", String.valueOf(mageTowerRange));
        props.setProperty("artilleryAOERange", String.valueOf(artilleryAOERange));
        
        // Tower fire rates
        props.setProperty("archerTowerFireRate", String.valueOf(archerTowerFireRate));
        props.setProperty("artilleryTowerFireRate", String.valueOf(artilleryTowerFireRate));
        props.setProperty("mageTowerFireRate", String.valueOf(mageTowerFireRate));
        
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            props.store(fos, "KU Tower Defense Game Settings");
            System.out.println("Settings saved successfully to file");
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
    
    /**
     * Reset all settings to default values.
     */
    public void resetToDefaults() {
        // Wave settings
        totalWaves = 10;
        groupsPerWave = 3;
        enemiesPerGroup = 5;
        waveDelay = 10000;
        groupDelay = 3000;
        enemyDelay = 500;
        
        // Enemy composition
        goblinPercentage = 70;
        
        // Economy
        startingGold = 100;
        goldPerGoblin = 10;
        goldPerKnight = 20;
        
        // Player stats
        startingLives = 20;
        
        // Enemy stats
        goblinHealth = 100;
        knightHealth = 200;
        goblinSpeed = 4;
        knightSpeed = 2;
        
        // Tower costs
        archerTowerCost = 50;
        artilleryTowerCost = 100;
        mageTowerCost = 75;
        
        // Tower damage
        archerTowerDamage = 20;
        artilleryTowerDamage = 40;
        mageTowerDamage = 30;
        
        // Tower ranges
        archerTowerRange = 150;
        artilleryTowerRange = 120;
        mageTowerRange = 140;
        artilleryAOERange = 50;
        
        // Tower fire rates
        archerTowerFireRate = 800;
        artilleryTowerFireRate = 2000;
        mageTowerFireRate = 1200;
        
        // Save the defaults
        saveSettings();
        
        System.out.println("Settings reset to defaults");
    }
    
    /**
     * Helper method to get an int property with a default value.
     *
     * @param props the properties object
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the property value as an int, or the default value
     */
    private int getIntProperty(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    // Getters and Setters
    
    // Wave settings
    public int getTotalWaves() {
        return totalWaves;
    }
    
    public void setTotalWaves(int totalWaves) {
        this.totalWaves = totalWaves;
    }
    
    public int getGroupsPerWave() {
        return groupsPerWave;
    }
    
    public void setGroupsPerWave(int groupsPerWave) {
        this.groupsPerWave = groupsPerWave;
    }
    
    public int getEnemiesPerGroup() {
        return enemiesPerGroup;
    }
    
    public void setEnemiesPerGroup(int enemiesPerGroup) {
        this.enemiesPerGroup = enemiesPerGroup;
    }
    
    public int getWaveDelay() {
        return waveDelay;
    }
    
    public void setWaveDelay(int waveDelay) {
        this.waveDelay = waveDelay;
    }
    
    public int getGroupDelay() {
        return groupDelay;
    }
    
    public void setGroupDelay(int groupDelay) {
        this.groupDelay = groupDelay;
    }
    
    public int getEnemyDelay() {
        return enemyDelay;
    }
    
    public void setEnemyDelay(int enemyDelay) {
        this.enemyDelay = enemyDelay;
    }
    
    // Enemy composition
    public int getGoblinPercentage() {
        return goblinPercentage;
    }
    
    public void setGoblinPercentage(int goblinPercentage) {
        this.goblinPercentage = goblinPercentage;
    }
    
    // Economy
    public int getStartingGold() {
        return startingGold;
    }
    
    public void setStartingGold(int startingGold) {
        this.startingGold = startingGold;
    }
    
    public int getGoldPerGoblin() {
        return goldPerGoblin;
    }
    
    public void setGoldPerGoblin(int goldPerGoblin) {
        this.goldPerGoblin = goldPerGoblin;
    }
    
    public int getGoldPerKnight() {
        return goldPerKnight;
    }
    
    public void setGoldPerKnight(int goldPerKnight) {
        this.goldPerKnight = goldPerKnight;
    }
    
    // Player stats
    public int getStartingLives() {
        return startingLives;
    }
    
    public void setStartingLives(int startingLives) {
        this.startingLives = startingLives;
    }
    
    // Enemy stats
    public int getGoblinHealth() {
        return goblinHealth;
    }
    
    public void setGoblinHealth(int goblinHealth) {
        this.goblinHealth = goblinHealth;
    }
    
    public int getKnightHealth() {
        return knightHealth;
    }
    
    public void setKnightHealth(int knightHealth) {
        this.knightHealth = knightHealth;
    }
    
    public int getGoblinSpeed() {
        return goblinSpeed;
    }
    
    public void setGoblinSpeed(int goblinSpeed) {
        this.goblinSpeed = goblinSpeed;
    }
    
    public int getKnightSpeed() {
        return knightSpeed;
    }
    
    public void setKnightSpeed(int knightSpeed) {
        this.knightSpeed = knightSpeed;
    }
    
    // Tower costs
    public int getArcherTowerCost() {
        return archerTowerCost;
    }
    
    public void setArcherTowerCost(int archerTowerCost) {
        this.archerTowerCost = archerTowerCost;
    }
    
    public int getArtilleryTowerCost() {
        return artilleryTowerCost;
    }
    
    public void setArtilleryTowerCost(int artilleryTowerCost) {
        this.artilleryTowerCost = artilleryTowerCost;
    }
    
    public int getMageTowerCost() {
        return mageTowerCost;
    }
    
    public void setMageTowerCost(int mageTowerCost) {
        this.mageTowerCost = mageTowerCost;
    }
    
    // Tower damage
    public int getArcherTowerDamage() {
        return archerTowerDamage;
    }
    
    public void setArcherTowerDamage(int archerTowerDamage) {
        this.archerTowerDamage = archerTowerDamage;
    }
    
    public int getArtilleryTowerDamage() {
        return artilleryTowerDamage;
    }
    
    public void setArtilleryTowerDamage(int artilleryTowerDamage) {
        this.artilleryTowerDamage = artilleryTowerDamage;
    }
    
    public int getMageTowerDamage() {
        return mageTowerDamage;
    }
    
    public void setMageTowerDamage(int mageTowerDamage) {
        this.mageTowerDamage = mageTowerDamage;
    }
    
    // Tower ranges
    public int getArcherTowerRange() {
        return archerTowerRange;
    }
    
    public void setArcherTowerRange(int archerTowerRange) {
        this.archerTowerRange = archerTowerRange;
    }
    
    public int getArtilleryTowerRange() {
        return artilleryTowerRange;
    }
    
    public void setArtilleryTowerRange(int artilleryTowerRange) {
        this.artilleryTowerRange = artilleryTowerRange;
    }
    
    public int getMageTowerRange() {
        return mageTowerRange;
    }
    
    public void setMageTowerRange(int mageTowerRange) {
        this.mageTowerRange = mageTowerRange;
    }
    
    public int getArtilleryAOERange() {
        return artilleryAOERange;
    }
    
    public void setArtilleryAOERange(int artilleryAOERange) {
        this.artilleryAOERange = artilleryAOERange;
    }
    
    // Tower fire rates
    public int getArcherTowerFireRate() {
        return archerTowerFireRate;
    }
    
    public void setArcherTowerFireRate(int archerTowerFireRate) {
        this.archerTowerFireRate = archerTowerFireRate;
    }
    
    public int getArtilleryTowerFireRate() {
        return artilleryTowerFireRate;
    }
    
    public void setArtilleryTowerFireRate(int artilleryTowerFireRate) {
        this.artilleryTowerFireRate = artilleryTowerFireRate;
    }
    
    public int getMageTowerFireRate() {
        return mageTowerFireRate;
    }
    
    public void setMageTowerFireRate(int mageTowerFireRate) {
        this.mageTowerFireRate = mageTowerFireRate;
    }
} 