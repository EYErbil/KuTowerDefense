package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.model.entity.Projectile;
import com.ku.towerdefense.model.entity.Goblin;
import com.ku.towerdefense.model.entity.DamageType;
import com.ku.towerdefense.model.entity.Enemy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArcherTowerTest {

    private ArcherTower archerTower;

    @BeforeEach
    void setUp() {
        archerTower = new ArcherTower(0, 0);
    }

    @Nested
    class RepOkTests {
        @Test
        void repOkFreshInstance() {
            assertTrue(archerTower.repOk(), "A newly created ArcherTower should satisfy its representation invariant.");
        }

        @Test
        void repOkAfterUpgrade() {
            archerTower.upgrade();
            assertTrue(archerTower.repOk(), "ArcherTower should satisfy repOk after a valid upgrade.");
        }
    }

    @Nested
    class FunctionalTests {
        @Test
        void testArcherTowerSpecificProperties() {
            assertEquals(20, archerTower.getDamage(), "Initial damage should be 20 for ArcherTower.");
            assertEquals(150, archerTower.getRange(), "Initial range should be 150 for ArcherTower.");
            assertEquals(1000, archerTower.getFireRate(), "Initial fire rate should be 1000ms for ArcherTower.");
            assertEquals(50, archerTower.getBaseCost(), "Base cost should be 50 for ArcherTower.");
            assertEquals("Archer Tower", archerTower.getName());
            assertEquals(DamageType.ARROW, archerTower.getDamageType(), "DamageType should be ARROW.");
            assertTrue(archerTower.repOk(), "Fresh instance should be valid.");
        }

        @Test
        void testUpgradeArcherTower() {
            int initialDamage = archerTower.getDamage();
            int initialRange = archerTower.getRange();
            long initialFireRate = archerTower.getFireRate();

            assertTrue(archerTower.canUpgrade(), "Should be able to upgrade from level 1.");
            boolean upgraded = archerTower.upgrade();
            assertTrue(upgraded, "Upgrade should succeed.");
            assertEquals(2, archerTower.getLevel(), "Level should be 2 after upgrade.");

            assertEquals(initialDamage, archerTower.getDamage(), "Damage should remain the same for L2 ArcherTower.");
            assertEquals((int)(initialRange * 1.5), archerTower.getRange(), "Range should increase by 50% for L2 ArcherTower.");
            assertEquals(initialFireRate / 2, archerTower.getFireRate(), "Fire rate (delay) should be halved (2x speed) for L2 ArcherTower.");
            
            assertTrue(archerTower.repOk(), "ArcherTower should satisfy repOk after upgrade.");

            assertFalse(archerTower.canUpgrade(), "Should not be able to upgrade from level 2 (max level).");
            boolean upgradedAgain = archerTower.upgrade();
            assertFalse(upgradedAgain, "Upgrade should fail at max level.");
            assertEquals(2, archerTower.getLevel(), "Level should remain 2 after failed upgrade attempt.");
        }

        @Test
        void testProjectileCreation() {
            Enemy dummyEnemy = new Goblin(10, 10);
            Projectile projectile = archerTower.createProjectile(dummyEnemy);
            assertNotNull(projectile, "Projectile should not be null.");
            assertEquals(archerTower.getDamage(), projectile.getDamage(), "Projectile damage should match tower damage.");
            assertEquals(DamageType.ARROW, projectile.getDamageType(), "Projectile damage type should be ARROW for ArcherTower projectile.");
        }
    }
} 