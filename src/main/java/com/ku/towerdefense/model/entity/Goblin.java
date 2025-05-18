package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.util.GameSettings;

import java.io.File;
import java.io.Serializable;

/**
 * A goblin enemy. Faster but weaker than knights.
 * Weak against arrow damage but resistant to magic damage.
 */
public class Goblin extends Enemy implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final int GOBLIN_HEALTH = GameSettings.getInstance().getGoblinHealth();
    private static final double GOBLIN_SPEED = GameSettings.getInstance().getGoblinSpeed(); // Example: 1.5 tiles per second
    public static final double PUBLIC_STATIC_FINAL_BASE_SPEED = GameSettings.getInstance().getGoblinSpeed(); // For Knight synergy
    private static final int GOBLIN_GOLD_REWARD = GameSettings.getInstance().getGoldPerGoblin();
    private static final double GOBLIN_WIDTH = 64; // Increased from 32
    private static final double GOBLIN_HEIGHT = 64; // Increased from 32
    
    /**
     * Create a new goblin at the specified position.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public Goblin(double x, double y) {
        super(x, y, GOBLIN_WIDTH, GOBLIN_HEIGHT, GOBLIN_HEALTH, GOBLIN_SPEED, GOBLIN_GOLD_REWARD, EnemyType.GOBLIN);
        
        // Note: Image is loaded from the static cache in the Enemy class
        // No need to set imageFile manually
    }
    
    /**
     * Apply damage with type modifiers.
     * Goblins take more damage from arrows but less from magic.
     *
     * @param amount amount of damage to apply
     * @param damageType the type of damage
     * @return true if the goblin was defeated, false otherwise
     */
    @Override
    public boolean applyDamage(int amount, DamageType damageType) {
        // Apply modifiers based on damage type
        int modifiedAmount = amount;
        
        if (damageType == DamageType.ARROW) {
            // Goblins are weak against arrows (50% more damage)
            modifiedAmount = (int)(amount * 1.5);
        } else if (damageType == DamageType.MAGIC) {
            // Goblins are resistant to magic (30% less damage)
            modifiedAmount = (int)(amount * 0.7);
        }
        
        return super.applyDamage(modifiedAmount);
    }
} 