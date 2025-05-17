package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.util.GameSettings;

import java.io.File;
import java.io.Serializable;

/**
 * Archer tower shoots arrows at enemies.
 * Has the fastest fire rate of all tower types, but deals the least damage per shot.
 * More effective against goblins, less effective against knights.
 */
public class ArcherTower extends Tower implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int COST = GameSettings.getInstance().getArcherTowerCost(); // 50
    private static final int BASE_DAMAGE = GameSettings.getInstance().getArcherTowerDamage();
    private static final int BASE_RANGE = GameSettings.getInstance().getArcherTowerRange();
    
    /**
     * Create a new archer tower at the specified position.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public ArcherTower(double x, double y) {
        super(x, y, 64, 64, 
              BASE_DAMAGE,
              BASE_RANGE,
              GameSettings.getInstance().getArcherTowerFireRate(),
              COST, // Pass the static COST here
              DamageType.ARROW);
        
        // Set image file from assets - using classpath reference instead of absolute path
        String imagePath = "/Asset_pack/Towers/Tower_archer128.png";
        setImageFile(imagePath);
    }
    
    /**
     * Create an arrow projectile targeting the specified enemy.
     *
     * @param target the target enemy
     * @return an arrow projectile
     */
    @Override
    protected Projectile createProjectile(Enemy target) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        Projectile arrow = new Projectile(
            centerX, centerY, 16, 8,
            target, damage, DamageType.ARROW, 450);
        
        // Set arrow appearance
        arrow.setColor(javafx.scene.paint.Color.DARKGREEN);
        
        return arrow;
    }

    @Override
    public String getName() {
        return "Archer Tower";
    }

    @Override
    public int getBaseCost() {
        return COST;
    }

    @Override
    public boolean upgrade() {
        if (!super.upgrade()) {
            return false;
        }
        // Increase damage and range based on level and base stats
        this.damage = BASE_DAMAGE + (int)(BASE_DAMAGE * (level -1) * UPGRADE_STAT_MULTIPLIER);
        this.range = BASE_RANGE + (int)(BASE_RANGE * (level -1) * (UPGRADE_STAT_MULTIPLIER / 2)); // Range upgrades slower
        // Fire rate could also be adjusted: this.fireRate = (long) (BASE_FIRE_RATE * (1 - (level-1) * 0.1)); e.g. 10% faster per level
        System.out.println("Archer Tower upgraded to level " + level + ". Damage: " + this.damage + ", Range: " + this.range);
        return true;
    }
} 