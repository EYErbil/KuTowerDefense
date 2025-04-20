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
    
    /**
     * Create a new artillery tower at the specified position.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public ArtilleryTower(double x, double y) {
        super(x, y, 64, 64, 
              GameSettings.getInstance().getArtilleryTowerDamage(),
              GameSettings.getInstance().getArtilleryTowerRange(),
              GameSettings.getInstance().getArtilleryTowerFireRate(),
              GameSettings.getInstance().getArtilleryTowerCost(),
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
        shell.setAoeRange(GameSettings.getInstance().getArtilleryAOERange());
        
        return shell;
    }
} 