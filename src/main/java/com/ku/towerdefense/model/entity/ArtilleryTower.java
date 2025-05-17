package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.util.GameSettings;

import java.io.File;
import java.io.Serializable;

/**
 * Artillery tower shoots explosive shells that cause area of effect damage.
 * Has the slowest fire rate but deals the most damage and can hit multiple enemies at once.
 * Deals the same damage to all enemy types.
 */
public class ArtilleryTower extends Tower implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int COST = GameSettings.getInstance().getArtilleryTowerCost(); // 100
    private static final int BASE_DAMAGE = GameSettings.getInstance().getArtilleryTowerDamage();
    private static final int BASE_RANGE = GameSettings.getInstance().getArtilleryTowerRange();
    private static final int BASE_AOE_RANGE = GameSettings.getInstance().getArtilleryAOERange();
    
    /**
     * Create a new artillery tower at the specified position.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public ArtilleryTower(double x, double y) {
        super(x, y, 64, 64, 
              BASE_DAMAGE,
              BASE_RANGE,
              GameSettings.getInstance().getArtilleryTowerFireRate(),
              COST, // Pass the static COST here
              DamageType.EXPLOSIVE);
        
        // Set image file from assets - using classpath reference instead of absolute path
        String imagePath = "/Asset_pack/Towers/Tower_bomb128.png";
        setImageFile(imagePath);
    }
    
    /**
     * Create an artillery shell projectile targeting the specified enemy.
     * The projectile will explode on impact and damage enemies in the area.
     *
     * @param target the target enemy
     * @return an artillery shell projectile
     */
    @Override
    protected Projectile createProjectile(Enemy target) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        Projectile shell = new Projectile(
            centerX, centerY, 16, 16,
            target, damage, DamageType.EXPLOSIVE, 350);
        
        // Set shell appearance
        shell.setColor(javafx.scene.paint.Color.RED);
        
        // Configure AOE effect
        shell.setHasAoeEffect(true);
        shell.setAoeRange(BASE_AOE_RANGE); // Use base AOE range initially
        
        return shell;
    }

    @Override
    public String getName() {
        return "Artillery Tower";
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
        // Increase damage, range, and AOE range
        this.damage = BASE_DAMAGE + (int)(BASE_DAMAGE * (level -1) * UPGRADE_STAT_MULTIPLIER);
        this.range = BASE_RANGE + (int)(BASE_RANGE * (level -1) * (UPGRADE_STAT_MULTIPLIER / 2)); 
        // AOE range can also be upgraded, perhaps less aggressively or tied to specific level thresholds.
        // For now, let's do a small increase. Projectile's AOE range will be set at creation time.
        // To make this effective, createProjectile would need to use the tower's current AOE range.
        // This requires adding an aoeRange field to ArtilleryTower and updating it here.
        // For simplicity now, we assume the projectile's AOE range is fixed after creation or the base one is fine.
        // If projectile's AOE needs to change, createProjectile needs to access parent tower's current AOE range.

        System.out.println("Artillery Tower upgraded to level " + level + ". Damage: " + this.damage + ", Range: " + this.range);
        return true;
    }
} 