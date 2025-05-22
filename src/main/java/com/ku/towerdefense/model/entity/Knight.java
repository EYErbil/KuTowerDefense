package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.util.GameSettings;
import com.ku.towerdefense.model.map.GameMap; // For TILE_SIZE, assuming it's accessible

import java.io.File;
import java.io.Serializable;
import java.util.List; // For List<Enemy>

/**
 * A knight enemy. Slower but stronger than goblins.
 * Resistant to arrow damage but weak against magic damage.
 */
public class Knight extends Enemy implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int KNIGHT_HEALTH = GameSettings.getInstance().getKnightHealth();
    private static final double KNIGHT_BASE_SPEED = GameSettings.getInstance().getKnightSpeed(); // Renamed for clarity
    private static final int KNIGHT_GOLD_REWARD = GameSettings.getInstance().getGoldPerKnight();
    private static final double KNIGHT_WIDTH = 128;
    private static final double KNIGHT_HEIGHT = 128;

    private final double originalSpeed; // To store its non-boosted speed

    /**
     * Create a new knight at the specified position.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public Knight(double x, double y) {
        super(x, y, KNIGHT_WIDTH, KNIGHT_HEIGHT, KNIGHT_HEALTH, KNIGHT_BASE_SPEED, KNIGHT_GOLD_REWARD,
                EnemyType.KNIGHT);
        this.originalSpeed = KNIGHT_BASE_SPEED; // Store original speed
    }

    @Override
    public boolean update(double deltaTime, List<Enemy> allEnemies) {
        boolean currentlyBoosted = false;
        double closestGoblinDist = Double.MAX_VALUE;

        for (Enemy other : allEnemies) {
            if (other instanceof Goblin && other != this && other.getCurrentHealth() > 0) {
                double dist = this.distanceTo(other);
                if (dist < closestGoblinDist) {
                    closestGoblinDist = dist;
                }
            }
        }

        // GameMap.TILE_SIZE might be better sourced from GameMap instance if available,
        // or a global constant
        // For now, assuming a known tile width like 64.0
        double tileWidthThreshold = GameMap.TILE_SIZE; // Or a hardcoded 64.0 if GameMap.TILE_SIZE is not
                                                       // static/accessible

        if (closestGoblinDist < tileWidthThreshold) {
            this.speed = (this.originalSpeed + Goblin.PUBLIC_STATIC_FINAL_BASE_SPEED) / 2.0;
            currentlyBoosted = true;
        } else {
            this.speed = this.originalSpeed;
        }
        setKnightSpeedBoosted(currentlyBoosted); // Update visual flag in Enemy class

        // The actual movement and status effect application (like slow) is done in
        // super.update
        // super.update will use the this.speed we just set, and then apply slowFactor
        // if isSlowed is true.
        return super.update(deltaTime, allEnemies);
    }

    /**
     * Apply damage with type modifiers.
     * Knights take less damage from arrows but more from magic.
     *
     * @param amount     amount of damage to apply
     * @param damageType the type of damage
     * @return true if the knight was defeated, false otherwise
     */
    @Override
    public boolean applyDamage(int amount, DamageType damageType) {
        // Apply modifiers based on damage type
        int modifiedAmount = amount;

        if (damageType == DamageType.ARROW) {
            // Knights are resistant to arrows (40% less damage)
            modifiedAmount = (int) (amount * 0.6);
        } else if (damageType == DamageType.MAGIC) {
            // Knights are weak against magic (40% more damage)
            modifiedAmount = (int) (amount * 1.4);
        }

        return super.applyDamage(modifiedAmount);
    }
}