package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.util.GameSettings;

import java.io.File;
import java.io.Serializable;

/**
 * A knight enemy. Slower but stronger than goblins.
 * Resistant to arrow damage but weak against magic damage.
 */
public class Knight extends Enemy implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final int KNIGHT_HEALTH = GameSettings.getInstance().getKnightHealth();
    private static final double KNIGHT_SPEED = GameSettings.getInstance().getKnightSpeed();
    private static final int KNIGHT_GOLD_REWARD = GameSettings.getInstance().getGoldPerKnight();
    private static final double KNIGHT_WIDTH = 64;
    private static final double KNIGHT_HEIGHT = 64;

    /**
     * Create a new knight at the specified position.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public Knight(double x, double y) {
        super(x, y, KNIGHT_WIDTH, KNIGHT_HEIGHT, KNIGHT_HEALTH, KNIGHT_SPEED, KNIGHT_GOLD_REWARD, EnemyType.KNIGHT);
        
        // Note: Image is loaded from the static cache in the Enemy class
        // No need to set imageFile manually
    }
    
    /**
     * Apply damage with type modifiers.
     * Knights take less damage from arrows but more from magic.
     *
     * @param amount amount of damage to apply
     * @param damageType the type of damage
     * @return true if the knight was defeated, false otherwise
     */
    @Override
    public boolean applyDamage(int amount, DamageType damageType) {
        // Apply modifiers based on damage type
        int modifiedAmount = amount;
        
        if (damageType == DamageType.ARROW) {
            // Knights are resistant to arrows (40% less damage)
            modifiedAmount = (int)(amount * 0.6);
        } else if (damageType == DamageType.MAGIC) {
            // Knights are weak against magic (40% more damage)
            modifiedAmount = (int)(amount * 1.4);
        }
        
        return super.applyDamage(modifiedAmount);
    }
} 