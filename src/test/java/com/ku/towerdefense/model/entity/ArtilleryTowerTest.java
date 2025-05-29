package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.model.entity.Goblin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArtilleryTowerTest {

    private ArtilleryTower artilleryTower;

    @BeforeEach
    void init() {
        artilleryTower = new ArtilleryTower(0, 0);
    }

    @Nested
    class RepOkTests {
        @Test
        void repOkFreshInstance() {
            assertTrue(artilleryTower.repOk(), "A newly created ArtilleryTower should satisfy its representation invariant.");
        }

        @Test
        void repOkAfterUpgrade() {
            artilleryTower.upgrade();
            assertTrue(artilleryTower.repOk(), "ArtilleryTower should satisfy repOk after a valid upgrade.");
        }

        @Test
        void repOkDetectsBadDamage() {
            artilleryTower.setDamage(0);
            assertFalse(artilleryTower.repOk(), "repOk should be false when damage is not positive.");
        }

        @Test
        void repOkDetectsBadLevel() {
            artilleryTower.setLevel(0); // Violate: 1 <= level
            assertFalse(artilleryTower.repOk(), "repOk should be false when level is less than 1.");
            artilleryTower.setLevel(Tower.MAX_TOWER_LEVEL + 1); // Violate: level <= MAX_TOWER_LEVEL
            assertFalse(artilleryTower.repOk(), "repOk should be false when level exceeds MAX_TOWER_LEVEL.");
        }
    }

    @Nested
    class ArtilleryTowerSpecificTests {
        @Test
        void hasCorrectName() {
            assertEquals("Artillery Tower", artilleryTower.getName());
        }

        @Test
        void hasCorrectBaseCost() {
            assertEquals(ArtilleryTower.BASE_COST, artilleryTower.getBaseCost());
            assertEquals(ArtilleryTower.BASE_COST, artilleryTower.getCost(), "Initial cost should be base cost.");
        }

        @Test
        void createsProjectileWithAoeProperties() {
            // Need a dummy enemy target
            Enemy dummyTarget = new Goblin(100, 100); // Using Goblin
            Projectile projectile = artilleryTower.createProjectile(dummyTarget);

            assertNotNull(projectile, "Projectile should not be null.");
            assertTrue(projectile.hasAoeEffect(), "Artillery projectile should have AOE effect.");
            assertEquals(ArtilleryTower.AOE_RANGE, projectile.getAoeRange(), "Projectile AOE damage range should match tower's constant.");
            assertEquals(DamageType.EXPLOSIVE, projectile.getDamageType(), "Projectile damage type should be EXPLOSIVE.");
            assertEquals(artilleryTower.getDamage(), projectile.getDamage(), "Projectile direct/AOE damage should match tower's current damage.");
        }

        @Test
        void upgradeIncreasesStatsAndMaintainsRepOk() {
            // Store initial L1 stats from base values, as tower's current stats are base stats at L1
            int l1Damage = artilleryTower.baseDamage; 
            int l1Range = artilleryTower.baseRange;
            long l1FireRate = artilleryTower.baseFireRate;

            boolean upgraded = artilleryTower.upgrade();
            assertTrue(upgraded, "Upgrade to L2 should succeed.");
            assertEquals(2, artilleryTower.getLevel(), "Level should be 2 after upgrade.");
            
            // Check L2 specific stat changes for ArtilleryTower
            // L2: +20% attack range, +20% AOE damage, fire rate same.
            assertEquals((int)(l1Range * 1.2), artilleryTower.getRange(), "L2 Range should be baseRange + 20%.");
            assertEquals((int)(l1Damage * 1.2), artilleryTower.getDamage(), "L2 Damage (used for AOE) should be baseDamage + 20%.");
            assertEquals(l1FireRate, artilleryTower.getFireRate(), "L2 Fire rate should remain base fire rate.");

            assertTrue(artilleryTower.repOk(), "ArtilleryTower should satisfy repOk after L2 upgrade.");

            // Try upgrading again (should fail as MAX_TOWER_LEVEL is 2)
            boolean secondUpgrade = artilleryTower.upgrade();
            assertFalse(secondUpgrade, "Upgrade beyond max level should fail.");
            assertEquals(2, artilleryTower.getLevel(), "Level should remain 2 after failed upgrade attempt.");
            assertTrue(artilleryTower.repOk(), "ArtilleryTower should still satisfy repOk at max level.");
        }
        
        @Test
        void sellRefundIsCorrect() {
            // Based on Tower's getSellRefund which is 75% of baseCost only
            int expectedRefund = (int) (ArtilleryTower.BASE_COST * 0.75);
            assertEquals(expectedRefund, artilleryTower.getSellRefund());
        }

        @Test
        void cloneTowerIsArtilleryTower() {
            Tower clonedTower = artilleryTower.cloneTower();
            assertNotNull(clonedTower);
            assertTrue(clonedTower instanceof ArtilleryTower, "Cloned tower should be an instance of ArtilleryTower.");
            assertNotSame(artilleryTower, clonedTower, "Cloned tower should be a different object.");
            assertEquals(artilleryTower.getX(), clonedTower.getX());
            assertEquals(artilleryTower.getY(), clonedTower.getY());
            assertEquals(artilleryTower.getBaseCost(), clonedTower.getBaseCost());
            assertTrue(clonedTower.repOk(), "Cloned tower should satisfy repOk.");
        }
    }
} 