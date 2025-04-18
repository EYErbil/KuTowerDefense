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
    
    /**
     * Create a new archer tower at the specified position.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public ArcherTower(double x, double y) {
        super(x, y, 64, 64, 
              GameSettings.getInstance().getArcherTowerDamage(),
              GameSettings.getInstance().getArcherTowerRange(),
              GameSettings.getInstance().getArcherTowerFireRate(),
              GameSettings.getInstance().getArcherTowerCost(),
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
} 