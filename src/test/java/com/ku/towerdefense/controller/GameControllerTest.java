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


import com.ku.towerdefense.model.entity.DroppedGold;

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
                map = new GameMap("TestMap", 5, 5);
                map.setTileType(0, 0, TileType.START_POINT);
                map.setTileType(4, 4, TileType.END_POINT);
                controller = new GameController(map);
                controller.setPlayerGold(200);
            } finally {
                setupLatch.countDown();
            }
        });
        if (!setupLatch.await(10, TimeUnit.SECONDS)) {
            throw new InterruptedException("Test setup on FX thread timed out");
        }
    }

    //Collecting a valid gold bag
    //Tests if a valid bag correctly adds gold and is removed.
    @Test
    void testCollectExistingGoldBag() {
        DroppedGold bag = new DroppedGold(100, 100, 50);  // 50G
        controller.getActiveGoldBags().add(bag);
        int initialGold = controller.getPlayerGold();
    
        controller.collectGoldBag(bag);
    
        assertEquals(initialGold + 50, controller.getPlayerGold(), "Player should gain 50 gold");
        assertFalse(controller.getActiveGoldBags().contains(bag), "Bag should be removed after collection");
    }
    //Trying to collect the same bag twice
    //Ensures gold is not added twice and the method behaves safely on repeated calls
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
    //Trying to collect a bag that doesn’t exist.
    //Verifies that collecting a bag not in the list does not affect the player’s gold.
    @Test
    void testCollectNonExistentGoldBag() {
        DroppedGold fakeBag = new DroppedGold(200, 200, 30); // Not added to list
        int initialGold = controller.getPlayerGold();
    
        controller.collectGoldBag(fakeBag);
    
        assertEquals(initialGold, controller.getPlayerGold(), "Player gold should remain the same");
        assertFalse(controller.getActiveGoldBags().contains(fakeBag), "Bag should not appear in the list");
    }
    //Collecting a gold bag with 0 gold.
    //Tests if collecting a 0G bag behaves correctly (gold doesn’t change, bag is removed)
    @Test
    void testCollectZeroGoldBag() {
        DroppedGold bag = new DroppedGold(100, 100, 0);  // 0 gold
        controller.getActiveGoldBags().add(bag);
        int initialGold = controller.getPlayerGold();
    
        controller.collectGoldBag(bag);
    
        assertEquals(initialGold, controller.getPlayerGold(), "Gold should not change with 0G bag");
        assertFalse(controller.getActiveGoldBags().contains(bag), "Bag should still be removed even if 0G");
    }
    //Collecting a gold bag with negative value
    //Edge case: tests behavior if a bag with a negative value is collected (it reduces player gold)
    @Test
    void testCollectNegativeGoldBag() {
        DroppedGold bag = new DroppedGold(100, 100, -25);  // -25 gold
        controller.getActiveGoldBags().add(bag);
        int initialGold = controller.getPlayerGold();
    
        controller.collectGoldBag(bag);
    
        assertEquals(initialGold - 25, controller.getPlayerGold(), "Gold should decrease with negative bag");
        assertFalse(controller.getActiveGoldBags().contains(bag), "Bag should be removed");
    }
    //Collecting multiple bags in sequence.
    //Simulates collecting more than one bag and verifies cumulative effects
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