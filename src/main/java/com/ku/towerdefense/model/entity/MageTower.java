package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.util.GameSettings;

import java.io.File;
import java.io.Serializable;

/**
 * Mage tower shoots magical spells at enemies.
 * Has a balanced fire rate and damage between archer and artillery towers.
 * More effective against knights, less effective against goblins.
 */
public class MageTower extends Tower implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Create a new mage tower at the specified position.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public MageTower(double x, double y) {
        super(x, y, 64, 64, 
              GameSettings.getInstance().getMageTowerDamage(),
              GameSettings.getInstance().getMageTowerRange(),
              GameSettings.getInstance().getMageTowerFireRate(),
              GameSettings.getInstance().getMageTowerCost(),
              DamageType.MAGIC);
        
        // Set image file from assets
        String imagePath = System.getProperty("user.dir") + File.separator + 
                          "Asset_pack" + File.separator + "Towers" + File.separator + 
                          "Tower_spell128.png";
        setImageFile(imagePath);
    }
    
    /**
     * Create a magical spell projectile targeting the specified enemy.
     *
     * @param target the target enemy
     * @return a spell projectile
     */
    @Override
    protected Projectile createProjectile(Enemy target) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        Projectile spell = new Projectile(
            centerX, centerY, 20, 20,
            target, damage, DamageType.MAGIC, 400);
        
        // Set spell appearance
        spell.setColor(javafx.scene.paint.Color.PURPLE);
        
        return spell;
    }
} 