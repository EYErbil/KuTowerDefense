package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.model.entity.Goblin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MageTowerTest {

    private MageTower mageTower;

    @BeforeEach
    void init() {
        mageTower = new MageTower(0, 0);
    }

    @Nested
    class RepOkTests {
        @Test
        void repOkFreshInstance() {
            assertTrue(mageTower.repOk(), "A newly created MageTower should satisfy its representation invariant.");
        }

        @Test
        void repOkAfterUpgrade() {
            // Assuming MageTower's upgrade method correctly calls super.upgrade() and maintains RI
            // If MageTower had specific L2 stat changes, they would be implicitly tested here too.
            mageTower.upgrade(); 
            assertTrue(mageTower.repOk(), "MageTower should satisfy repOk after a valid upgrade.");
        }

        @Test
        void repOkDetectsBadDamage() {
            mageTower.setDamage(0);
            assertFalse(mageTower.repOk(), "repOk should be false when damage is not positive.");
        }

        @Test
        void repOkDetectsBadLevel() {
            mageTower.setLevel(0); 
            assertFalse(mageTower.repOk(), "repOk should be false when level is less than 1.");
            mageTower.setLevel(Tower.MAX_TOWER_LEVEL + 1); 
            assertFalse(mageTower.repOk(), "repOk should be false when level exceeds MAX_TOWER_LEVEL.");
        }
    }

    @Nested
    class MageTowerSpecificTests {
        @Test
        void hasCorrectName() {
            assertEquals("Mage Tower", mageTower.getName());
        }

        @Test
        void hasCorrectBaseCost() {
            assertEquals(MageTower.BASE_COST, mageTower.getBaseCost());
            assertEquals(MageTower.BASE_COST, mageTower.getCost(), "Initial cost should be base cost.");
        }

        @Test
        void createsProjectileWithCorrectProperties() {
            Enemy dummyTarget = new Goblin(100, 100);
            Projectile projectileL1 = mageTower.createProjectile(dummyTarget);

            assertNotNull(projectileL1, "L1 Projectile should not be null.");
            assertEquals(DamageType.MAGIC, projectileL1.getDamageType(), "L1 Projectile damage type should be MAGIC.");
            assertEquals(mageTower.getDamage(), projectileL1.getDamage(), "L1 Projectile damage should match tower's current damage.");
            // Check L1 projectile image file (MageTower.L1_PROJECTILE_IMAGE_FILE should be public or have a getter)
            // assertEquals(MageTower.L1_PROJECTILE_IMAGE_FILE, projectileL1.getImageFile(), "L1 Projectile should use L1 image file.");

            // Test L2 projectile image
            if (mageTower.canUpgrade()) {
                mageTower.upgrade(); // This should change level to 2
                assertEquals(2, mageTower.getLevel(), "Tower should be level 2 after upgrade.");
                Projectile projectileL2 = mageTower.createProjectile(dummyTarget);
                assertNotNull(projectileL2, "L2 Projectile should not be null.");
                assertEquals(DamageType.MAGIC, projectileL2.getDamageType(), "L2 Projectile damage type should be MAGIC.");
                assertEquals(mageTower.getDamage(), projectileL2.getDamage(), "L2 Projectile damage should match L2 tower's current damage.");
                // Check L2 projectile image file (MageTower.L2_PROJECTILE_IMAGE_FILE should be public or have a getter)
                // assertEquals(MageTower.L2_PROJECTILE_IMAGE_FILE, projectileL2.getImageFile(), "L2 Projectile should use L2 image file.");
                // assertNotEquals(projectileL1.getImageFile(), projectileL2.getImageFile(), "L1 and L2 projectile images should differ.");
            }
        }
        
        @Test
        void testUpgradeMageTowerLevel2Behavior() {
            int initialDamage = mageTower.getDamage();
            long initialFireRate = mageTower.getFireRate();
            // String l1ProjectileImage = mageTower.L1_PROJECTILE_IMAGE_FILE; // Requires public field or getter

            assertTrue(mageTower.canUpgrade(), "Should be able to upgrade Mage Tower from L1.");
            mageTower.upgrade();
            assertEquals(2, mageTower.getLevel(), "Mage Tower level should be 2 after upgrade.");

            // L2 Mage towers deal the same amount of damage as Level 1 and fire at the same rate of fire.
            assertEquals(initialDamage, mageTower.getDamage(), "L2 Mage Tower damage should be same as L1.");
            assertEquals(initialFireRate, mageTower.getFireRate(), "L2 Mage Tower fire rate should be same as L1.");

            // Color of the spell fired by a level 2 tower should be different.
            // This is handled by MageTower.createProjectile setting a different image file.
            // We can verify this by checking the projectile's imageFile property if accessible,
            // or by checking the tower's projectile creation logic output.
            Enemy dummyTarget = new Goblin(50, 50);
            Projectile projectileL2 = mageTower.createProjectile(dummyTarget);
            assertNotNull(projectileL2, "L2 Mage Tower should create a projectile.");
            //assertEquals(MageTower.L2_PROJECTILE_IMAGE_FILE, projectileL2.getImageFile(), "L2 Projectile should use L2 image file.");
            // Placeholder: Actual check for different projectile appearance/color might need access to Projectile.imageFile or Projectile.color
            // For now, we rely on MageTower.createProjectile correctly setting L2_PROJECTILE_IMAGE_FILE.

            // The slow effect is applied by GameController upon hit. Testing the application of slow is complex here.
            // We can ensure the projectile is of a type that *could* cause slow, if such a distinction exists.
            // Currently, MageTower.createProjectile does not add a specific flag for slow to the Projectile itself.

            assertTrue(mageTower.repOk(), "MageTower should satisfy repOk after L2 upgrade and stat checks.");
        }

        @Test
        void upgradeChangesImageAndMaintainsRepOk() {
            // MageTower's current code doesn't override upgrade() to change stats,
            // it only relies on super.upgrade() for level and potentially image in superclass.
            // The createProjectile method *does* change projectile image based on level.
            
            String initialImageFile = mageTower.imageFile; // Assuming imageFile is accessible (it's protected)

            boolean upgraded = mageTower.upgrade();
            assertTrue(upgraded, "Upgrade to L2 should succeed.");
            assertEquals(2, mageTower.getLevel(), "Level should be 2 after upgrade.");
            
            // Superclass Tower.upgrade() should set imageFile to getUpgradedImageName()
            assertEquals(mageTower.getUpgradedImageName(), mageTower.imageFile, "Image file should change after L2 upgrade.");
            assertNotEquals(initialImageFile, mageTower.imageFile, "Image file should be different after upgrade.");

            assertTrue(mageTower.repOk(), "MageTower should satisfy repOk after L2 upgrade.");
        }

        @Test
        void sellRefundIsCorrect() {
            int expectedRefund = (int) (MageTower.BASE_COST * 0.75);
            assertEquals(expectedRefund, mageTower.getSellRefund());
        }

        @Test
        void cloneTowerIsMageTower() {
            Tower clonedTower = mageTower.cloneTower();
            assertNotNull(clonedTower);
            assertTrue(clonedTower instanceof MageTower, "Cloned tower should be an instance of MageTower.");
            assertNotSame(mageTower, clonedTower, "Cloned tower should be a different object.");
            assertEquals(mageTower.getX(), clonedTower.getX());
            assertEquals(mageTower.getY(), clonedTower.getY());
            assertEquals(mageTower.getBaseCost(), clonedTower.getBaseCost());
            assertTrue(clonedTower.repOk(), "Cloned tower should satisfy repOk.");
        }
    }
} 