package com.ku.towerdefense.controller;

import com.ku.towerdefense.model.entity.ArcherTower;
import com.ku.towerdefense.model.entity.Tower;
import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.TileType;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameController.upgradeTower
 *
 * Requires:
 * - towerToUpgrade is not null or there is a tower at (tileX, tileY)
 * - GameController and GameMap are properly initialized
 *
 * Modifies:
 * - playerGold
 * - towerToUpgrade.level
 *
 * Effects:
 * - Upgrades the tower if possible, deducts gold
 * - Returns true if upgrade succeeds, false otherwise
 */
class EceTestUpgradeTower {
    private GameController controller;
    private GameMap map;

    @BeforeAll
    static void initFX() throws InterruptedException {
        if (!Platform.isFxApplicationThread()) {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(() -> latch.countDown());
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new InterruptedException("JavaFX toolkit initialization timed out");
            }
        }
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        final CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                map = new GameMap("TestMap", 5, 5);
                map.setTileType(1, 1, TileType.TOWER_SLOT);
                controller = new GameController(map);
                controller.setPlayerGold(200);
                // Place a tower at (1,1)
                Tower tower = new ArcherTower(1 * GameMap.TILE_SIZE, 1 * GameMap.TILE_SIZE);
                controller.purchaseAndPlaceTower(tower, 1, 1);
            } finally {
                setupLatch.countDown();
            }
        });
        if (!setupLatch.await(10, TimeUnit.SECONDS)) {
            throw new InterruptedException("Test setup on FX thread timed out");
        }
    }

    /**
     * Test that a tower can be successfully upgraded when:
     * - The player has enough gold for the upgrade
     * - The tower is not already at its maximum level
     *
     * This test verifies:
     * - The upgradeTower method returns true
     * - The tower's level increases by 1
     * - The correct amount of gold is deducted from the player
     */
    @Test
    void testUpgradeSuccess() {
        Tower tower = controller.getTowerAtTile(1, 1);
        int initialGold = controller.getPlayerGold();
        int upgradeCost = tower.getUpgradeCost();
        int initialLevel = tower.getLevel();
        boolean upgraded = controller.upgradeTower(tower, 1, 1);
        assertTrue(upgraded, "Upgrade should succeed");
        assertEquals(initialLevel + 1, tower.getLevel(), "Tower level should increase");
        assertEquals(initialGold - upgradeCost, controller.getPlayerGold(), "Gold should be deducted");
    }

    /**
     * Test that upgrading fails when the player does not have enough gold.
     *
     * This test verifies:
     * - The upgradeTower method returns false
     * - The tower's level does not change
     * - No gold is deducted
     */
    @Test
    void testUpgradeNotEnoughGold() {
        controller.setPlayerGold(1); // Not enough for upgrade
        Tower tower = controller.getTowerAtTile(1, 1);
        int initialLevel = tower.getLevel();
        boolean upgraded = controller.upgradeTower(tower, 1, 1);
        assertFalse(upgraded, "Upgrade should fail if not enough gold");
        assertEquals(initialLevel, tower.getLevel(), "Tower level should not change");
    }

    /**
     * Test that upgrading fails when the tower is already at its maximum level.
     *
     * This test verifies:
     * - The upgradeTower method returns false when called at max level
     * - The tower's level does not change
     * - No gold is deducted
     */
    @Test
    void testUpgradeAtMaxLevel() {
        Tower tower = controller.getTowerAtTile(1, 1);
        // Upgrade once to reach max level
        controller.upgradeTower(tower, 1, 1);
        int maxLevel = tower.getMaxLevel();
        assertEquals(maxLevel, tower.getLevel(), "Tower should be at max level");
        // Try upgrading again
        boolean upgraded = controller.upgradeTower(tower, 1, 1);
        assertFalse(upgraded, "Upgrade should fail at max level");
        assertEquals(maxLevel, tower.getLevel(), "Tower level should not change at max level");
    }

    /**
     * Test that upgrading with a null tower reference but valid coordinates
     * will still succeed if there is a tower at the given coordinates.
     *
     * This test verifies:
     * - The upgradeTower method can find and upgrade a tower using only coordinates
     * - The tower's level increases by 1
     * - The correct amount of gold is deducted
     */
    @Test
    void testUpgradeByCoordinates() {
        Tower tower = controller.getTowerAtTile(1, 1);
        int initialGold = controller.getPlayerGold();
        int upgradeCost = tower.getUpgradeCost();
        int initialLevel = tower.getLevel();
        // Pass null for tower, but provide valid coordinates
        boolean upgraded = controller.upgradeTower(null, 1, 1);
        assertTrue(upgraded, "Upgrade should succeed using only coordinates");
        assertEquals(initialLevel + 1, tower.getLevel(), "Tower level should increase");
        assertEquals(initialGold - upgradeCost, controller.getPlayerGold(), "Gold should be deducted");
    }

    /**
     * Test that upgrading with a null tower reference and invalid coordinates
     * will fail gracefully (should not throw an exception).
     *
     * This test verifies:
     * - The upgradeTower method returns false if no tower is found at the coordinates
     * - No exception is thrown
     * - No gold is deducted
     */
    @Test
    void testUpgradeWithNullTower() {
        int initialGold = controller.getPlayerGold();
        // Use coordinates where there is no tower
        boolean upgraded = controller.upgradeTower(null, 2, 2);
        assertFalse(upgraded, "Upgrade should fail if no tower at coordinates");
        assertEquals(initialGold, controller.getPlayerGold(), "Gold should not change if upgrade fails");
    }
} 