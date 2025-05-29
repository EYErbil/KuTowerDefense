package com.ku.towerdefense.controller;
import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.TileType;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    private GameController gameController;
    private GameMap mockMap;

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
                mockMap = new GameMap("TestMap", 10, 10);
                gameController = new GameController(mockMap);
            } finally {
                setupLatch.countDown();
            }
        });
        if (!setupLatch.await(10, TimeUnit.SECONDS)) {
            throw new InterruptedException("Test setup on FX thread timed out");
        }
    }

    @Test
    void testStartNextWaveWithValidPath() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Arrange: Set start and end tiles
                mockMap.setTileType(0, 0, TileType.START_POINT);
                mockMap.setTileType(9, 9, TileType.END_POINT);

                // Create simple path from (0,0) to (9,9)
                for (int i = 1; i < 10; i++) {
                    mockMap.setTileType(i, i - 1, TileType.PATH_HORIZONTAL);
                    mockMap.setTileType(i - 1, i, TileType.PATH_VERTICAL);
                }

                int prevWave = gameController.getCurrentWave();

                // Act
                gameController.startNextWave();

                // Assert
                assertEquals(prevWave + 1, gameController.getCurrentWave(), "Wave number should increment");
                assertNotNull(mockMap.getEnemyPath(), "Enemy path should be initialized");
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
    }

    @Test
    void testStartNextWaveWithoutStartTile() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Arrange: Only end point set, no start point
                mockMap.setTileType(5, 5, TileType.END_POINT);

                // Act
                gameController.startNextWave();

                // Assert
                assertNull(mockMap.getEnemyPath(), "Enemy path should not be generated without a start point");
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
    }

    @Test
    void testStartNextWaveWithoutEndTile() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Arrange: Only start point set, no end point
                mockMap.setTileType(0, 0, TileType.START_POINT);

                // Act
                gameController.startNextWave();

                // Assert
                assertNull(mockMap.getEnemyPath(), "Enemy path should not be generated without an end point");
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
    }

    @Test
    void testStartNextWaveWithStartAndEndButDisconnectedPath() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Arrange: Set start and end tiles
                mockMap.setTileType(0, 0, TileType.START_POINT);
                mockMap.setTileType(9, 9, TileType.END_POINT);

                // Path tiles exist but do not connect start to end
                for (int x = 0; x < 9; x++) {
                    mockMap.setTileType(x, 0, TileType.PATH_HORIZONTAL);
                }

                for (int y = 0; y < 9; y++) {
                    mockMap.setTileType(9, y, TileType.PATH_VERTICAL);
                }

                // Act
                gameController.startNextWave();

                // Assert
                assertNotNull(mockMap.getEnemyPath(), "Enemy path should be generated if start and end are connected via path");
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
    }
}
