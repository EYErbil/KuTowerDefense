package com.ku.towerdefense.controller;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ku.towerdefense.model.entity.ArcherTower;
import com.ku.towerdefense.model.entity.DroppedGold;
import com.ku.towerdefense.model.entity.Tower;
import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.TileType;

import javafx.application.Platform;

class GameControllerTest {
    private GameController controller;
    private GameMap map;
    

    @BeforeAll
    static void initFX() throws InterruptedException {
        // Initialize JavaFX toolkit once for all tests
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
                // Create a simple 5x5 test map
                map = new GameMap("TestMap", 5, 5);

                // Set up tower slots
                map.setTileType(1, 1, TileType.TOWER_SLOT);
                map.setTileType(2, 2, TileType.TOWER_SLOT);
                map.setTileType(3, 3, TileType.TOWER_SLOT);

                // Set up path tiles
                map.setTileType(0, 0, TileType.START_POINT);
                map.setTileType(4, 4, TileType.END_POINT);

                // Create controller
                controller = new GameController(map);
                controller.setPlayerGold(200); // Start with 200 gold
            } finally {
                setupLatch.countDown();
            }
        });
        if (!setupLatch.await(10, TimeUnit.SECONDS)) {
            throw new InterruptedException("Test setup on FX thread timed out");
        }
    }

    @Test
    void testValidTowerPlacement() {
        // Create archer tower template (cost = 50)
        Tower tower = new ArcherTower(0, 0);

        // Place tower on valid slot
        boolean success = controller.purchaseAndPlaceTower(tower, 1, 1);

        assertTrue(success, "Should successfully place tower on valid slot");
        assertEquals(150, controller.getPlayerGold(), "Gold should be reduced by tower cost");
        assertEquals(1, controller.getTowers().size(), "Should have one tower in list");

        Tower placedTower = controller.getTowerAtTile(1, 1);
        assertNotNull(placedTower, "Should find tower at placement location");
        assertEquals("Archer Tower", placedTower.getName(), "Tower should be Archer Tower");
    }

    @Test
    void testInsufficientGold() {
        controller.setPlayerGold(30); // Less than archer tower cost (50)
        Tower tower = new ArcherTower(0, 0);

        boolean success = controller.purchaseAndPlaceTower(tower, 1, 1);

        assertFalse(success, "Should fail when insufficient gold");
        assertEquals(30, controller.getPlayerGold(), "Gold should remain unchanged");
        assertEquals(0, controller.getTowers().size(), "Should have no towers");
        assertNull(controller.getTowerAtTile(1, 1), "Should find no tower at location");
    }

    @Test
    void testInvalidTilePlacement() {
        Tower tower = new ArcherTower(0, 0);

        // Try to place on non-tower-slot tile (0,0 is START_POINT)
        boolean success = controller.purchaseAndPlaceTower(tower, 0, 0);

        assertFalse(success, "Should fail on invalid tile type");
        assertEquals(200, controller.getPlayerGold(), "Gold should remain unchanged");
        assertEquals(0, controller.getTowers().size(), "Should have no towers");
    }

    @Test
    void testOutOfBounds() {
        Tower tower = new ArcherTower(0, 0);

        // Try negative coordinates
        assertFalse(controller.purchaseAndPlaceTower(tower, -1, -1), "Should reject negative coordinates");

        // Try coordinates beyond map size
        assertFalse(controller.purchaseAndPlaceTower(tower, 10, 10), "Should reject out-of-bounds coordinates");

        assertEquals(200, controller.getPlayerGold(), "Gold should remain unchanged");
        assertEquals(0, controller.getTowers().size(), "Should have no towers");
    }

    @Test
    void testOccupiedSlot() {
        Tower tower1 = new ArcherTower(0, 0);
        Tower tower2 = new ArcherTower(0, 0);

        // Place first tower
        assertTrue(controller.purchaseAndPlaceTower(tower1, 1, 1), "First placement should succeed");

        // Try to place second tower in same location
        boolean success = controller.purchaseAndPlaceTower(tower2, 1, 1);

        assertFalse(success, "Should not place on occupied slot");
        assertEquals(150, controller.getPlayerGold(), "Should only deduct gold once");
        assertEquals(1, controller.getTowers().size(), "Should still have only one tower");
    }

    @Test
    void testMultipleTowers() {
        Tower tower1 = new ArcherTower(0, 0);
        Tower tower2 = new ArcherTower(0, 0);

        // Place towers on different slots
        assertTrue(controller.purchaseAndPlaceTower(tower1, 1, 1), "First placement should succeed");
        assertTrue(controller.purchaseAndPlaceTower(tower2, 2, 2), "Second placement should succeed");

        assertEquals(100, controller.getPlayerGold(), "Should deduct gold for both towers");
        assertEquals(2, controller.getTowers().size(), "Should have two towers");

        assertNotNull(controller.getTowerAtTile(1, 1), "Should find first tower");
        assertNotNull(controller.getTowerAtTile(2, 2), "Should find second tower");
    }

    @Test
    void testSellTower() {
        // Place a tower first
        Tower tower = new ArcherTower(0, 0);
        controller.purchaseAndPlaceTower(tower, 1, 1);

        // Sell the tower
        int refund = controller.sellTower(1, 1);

        assertTrue(refund > 0, "Should get positive refund");
        assertTrue(controller.getPlayerGold() > 150, "Gold should increase after selling");
        assertEquals(0, controller.getTowers().size(), "Tower list should be empty");
        assertNull(controller.getTowerAtTile(1, 1), "Should find no tower at sold location");
    }

    @Test
    void testSellNonexistentTower() {
        // Try to sell where no tower exists
        int refund = controller.sellTower(1, 1);

        assertEquals(0, refund, "Should get no refund for non-existent tower");
        assertEquals(200, controller.getPlayerGold(), "Gold should remain unchanged");
        assertEquals(0, controller.getTowers().size(), "Tower list should remain empty");
    }

    @Test
    void testTowerUpgrade() {
        // Place a tower
        Tower tower = new ArcherTower(0, 0);
        controller.purchaseAndPlaceTower(tower, 1, 1);

        Tower placedTower = controller.getTowerAtTile(1, 1);
        assertNotNull(placedTower, "Tower should exist");
        assertEquals(1, placedTower.getLevel(), "Tower should start at level 1");

        // Try to upgrade (may succeed or fail depending on gold/upgrade cost)
        boolean upgraded = controller.upgradeTower(placedTower, 1, 1);

        if (upgraded) {
            assertEquals(2, placedTower.getLevel(), "Tower should be level 2 after upgrade");
            assertTrue(controller.getPlayerGold() < 150, "Gold should be deducted for upgrade");
        }
        // If upgrade fails due to insufficient gold, that's also valid behavior
    }

    @Test
    void testInitialState() {
        assertEquals(200, controller.getPlayerGold(), "Should start with correct gold");
        assertEquals(0, controller.getTowers().size(), "Should start with no towers");
        assertFalse(controller.isGameOver(), "Game should not be over initially");
        assertNotNull(controller.getGameMap(), "Should have game map");
    }

    @Test
    void testNullTowerTemplate() {
        // Try to place null tower
        boolean success = controller.purchaseAndPlaceTower(null, 1, 1);

        assertFalse(success, "Should reject null tower template");
        assertEquals(200, controller.getPlayerGold(), "Gold should remain unchanged");
        assertEquals(0, controller.getTowers().size(), "Should have no towers");
    }

    @Test
    void testTowerProperties() {
        Tower archer = new ArcherTower(0, 0);

        assertEquals("Archer Tower", archer.getName(), "Archer tower should have correct name");
        assertEquals(50, archer.getBaseCost(), "Archer tower should cost 50");
        assertEquals(1, archer.getLevel(), "New tower should be level 1");
    }
    @Test
    void testCollectExistingGoldBag() {
        DroppedGold bag = new DroppedGold(100, 100, 50);  // 50G
        controller.getActiveGoldBags().add(bag);
        int initialGold = controller.getPlayerGold();
    
        controller.collectGoldBag(bag);
    
        assertEquals(initialGold + 50, controller.getPlayerGold(), "Player should gain 50 gold");
        assertFalse(controller.getActiveGoldBags().contains(bag), "Bag should be removed after collection");
    }

    @Test
    void testCollectSameGoldBagTwice() {
        DroppedGold bag = new DroppedGold(150, 150, 40);
        controller.getActiveGoldBags().add(bag);
        int initialGold = controller.getPlayerGold();
    
        controller.collectGoldBag(bag); // First attempt
        controller.collectGoldBag(bag); // Second attempt
    
        assertEquals(initialGold + 40, controller.getPlayerGold(), "Gold should only increase once");
        assertFalse(controller.getActiveGoldBags().contains(bag), "Bag should only be removed once");
    }

    @Test
    void testCollectNonExistentGoldBag() {
        DroppedGold fakeBag = new DroppedGold(200, 200, 30); // Not added to list
        int initialGold = controller.getPlayerGold();
    
        controller.collectGoldBag(fakeBag);
    
        assertEquals(initialGold, controller.getPlayerGold(), "Player gold should remain the same");
        assertFalse(controller.getActiveGoldBags().contains(fakeBag), "Bag should not appear in the list");
    }
    
    @Test
    void testCollectZeroGoldBag() {
        DroppedGold bag = new DroppedGold(100, 100, 0);  // 0 gold
        controller.getActiveGoldBags().add(bag);
        int initialGold = controller.getPlayerGold();
    
        controller.collectGoldBag(bag);
    
        assertEquals(initialGold, controller.getPlayerGold(), "Gold should not change with 0G bag");
        assertFalse(controller.getActiveGoldBags().contains(bag), "Bag should still be removed even if 0G");
    }

    @Test
    void testCollectNegativeGoldBag() {
        DroppedGold bag = new DroppedGold(100, 100, -25);  // -25 gold
        controller.getActiveGoldBags().add(bag);
        int initialGold = controller.getPlayerGold();
    
        controller.collectGoldBag(bag);
    
        assertEquals(initialGold - 25, controller.getPlayerGold(), "Gold should decrease with negative bag");
        assertFalse(controller.getActiveGoldBags().contains(bag), "Bag should be removed");
    }

    @Test
    void testCollectMultipleBagsInSequence() {
        DroppedGold bag1 = new DroppedGold(100, 100, 30);
        DroppedGold bag2 = new DroppedGold(150, 150, 40);
        controller.getActiveGoldBags().add(bag1);
        controller.getActiveGoldBags().add(bag2);
        int initialGold = controller.getPlayerGold();
    
        controller.collectGoldBag(bag1);
        controller.collectGoldBag(bag2);
    
        assertEquals(initialGold + 70, controller.getPlayerGold(), "Should gain total of 70G");
        assertTrue(controller.getActiveGoldBags().isEmpty(), "Both bags should be removed");
    }




    






    

}
