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
    public static final int BASE_COST = 100; // Added base cost
    private static final int BASE_DAMAGE = GameSettings.getInstance().getArtilleryTowerDamage();
    private static final int BASE_RANGE = GameSettings.getInstance().getArtilleryTowerRange();
    private static final int BASE_AOE_RANGE = GameSettings.getInstance().getArtilleryAOERange();
    private static final long BASE_FIRE_RATE = GameSettings.getInstance().getArtilleryTowerFireRate();
    private static final String BASE_IMAGE_FILENAME = "Tower_bomb128.png";
    private static final String UPGRADED_IMAGE_FILENAME = "artillery_up.png";

    private static final double PROJECTILE_WIDTH = 20;
    private static final double PROJECTILE_HEIGHT = 20;
    private static final double PROJECTILE_SPEED = 200;
    private static final String PROJECTILE_IMAGE_FILE = "cannon_ball.png"; // Placeholder
    private static final int AOE_RANGE = 50; // Example AOE range
    
    /**
     * Create a new artillery tower at the specified position.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public ArtilleryTower(double x, double y) {
        super(x, y, 64, 64, BASE_DAMAGE, BASE_RANGE, BASE_FIRE_RATE, BASE_COST, DamageType.EXPLOSIVE);
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
        double projectileX = getCenterX() - PROJECTILE_WIDTH / 2;
        double projectileY = getCenterY() - PROJECTILE_HEIGHT / 2;
        Projectile projectile = new Projectile(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT, target, this.damage, DamageType.EXPLOSIVE, PROJECTILE_SPEED);
        projectile.setImageFile(PROJECTILE_IMAGE_FILE);
        projectile.setImpactEffect(Projectile.ImpactEffect.EXPLOSION);
        projectile.setHasAoeEffect(true);
        projectile.setAoeRange(AOE_RANGE);
        return projectile;
    }

    @Override
    public String getName() {
        return "Artillery Tower";
    }

    @Override
    public int getBaseCost() {
        return BASE_COST;
    }

    @Override
    protected String getBaseImageName() {
        return "Asset_pack/Towers/" + BASE_IMAGE_FILENAME;
    }

    @Override
    protected String getUpgradedImageName() {
        return "Asset_pack/Towers/" + UPGRADED_IMAGE_FILENAME;
    }

    @Override
    public Tower cloneTower() {
        ArtilleryTower clone = new ArtilleryTower(this.x, this.y);
        return clone;
    }
} 