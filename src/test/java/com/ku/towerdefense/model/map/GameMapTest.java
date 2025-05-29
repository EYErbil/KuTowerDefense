package com.ku.towerdefense.model.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;

import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.Tile;
import com.ku.towerdefense.model.map.TileType;
import com.ku.towerdefense.model.GamePath;
import com.ku.towerdefense.model.entity.Tower;
import com.ku.towerdefense.model.entity.ArcherTower;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

// Assuming Tile and TileType are correctly defined and accessible
// For these tests to compile and run, Tile and TileType must be resolvable.
// Tile must have: constructor Tile(x, y, TileType), getX(), getY(), setType(TileType), getType()
// TileType must be an enum with values: GRASS, PATH, START_POINT, END_POINT
// Tile.isWalkable() should behave as expected (true for PATH, false for GRASS).

public class GameMapTest {

    private GameMap map;
    private final int TILE_PIXEL_SIZE = 64; // Match GameMap.findPathBFS internal constant

    @BeforeAll
    static void disableFxForTests() {
        Tile.isFxAvailable = false;
    }

    // Helper to create a tile, assuming Tile and TileType exist
    // This is a placeholder; your actual Tile class might have different needs.
    private Tile createMockTile(int x, int y, TileType type) {
        Tile tile = new Tile(x, y, type);
        // Mocking isWalkable based on common assumptions.
        // Adjust if your Tile.isWalkable() logic is different or set elsewhere.
        // In a real scenario with a complex Tile class, consider using a mocking
        // framework (e.g., Mockito)
        // or making Tile.isWalkable easily configurable for tests.
        if (type == TileType.PATH || type == TileType.START_POINT || type == TileType.END_POINT) {
            // Assuming START_POINT and END_POINT tiles are also considered walkable for
            // pathfinding
            // and Tile.isWalkable() would reflect this.
        } else {
            // For GRASS or other non-path types
        }
        // For this test, we assume Tile.isWalkable() is correctly implemented in the
        // Tile class itself
        // based on its TileType.
        return tile;
    }

    @BeforeEach
    void setUp() {
        // Default map for setup, individual tests will often reconfigure this.
        // Dimensions don't really matter here as findPathBFS uses the provided tiles
        // and map.tiles internally.
        map = new GameMap("TestMap", 5, 5);
    }

    private int[] p(int x, int y) {
        return new int[] { x * TILE_PIXEL_SIZE + TILE_PIXEL_SIZE / 2, y * TILE_PIXEL_SIZE + TILE_PIXEL_SIZE / 2 };
    }

    @Test
    @DisplayName("Test 1: Simple Straight Path")
    void findPathBFS_simpleStraightPath() {
        map = new GameMap("StraightPathMap", 3, 3);

        // Ensure other tiles are GRASS by default by GameMap constructor
        // Then set the specific path, start, and end points.
        map.setTileType(0, 1, TileType.START_POINT);
        map.setTileType(1, 1, TileType.PATH);
        map.setTileType(2, 1, TileType.END_POINT);

        List<int[]> actualPath = map.findPathBFS(map.getTile(0, 1), map.getTile(2, 1));

        assertNotNull(actualPath, "Path should not be null for a simple straight connection.");
        List<int[]> expectedPath = Arrays.asList(
                p(0, 1), // Start
                p(1, 1), // Middle
                p(2, 1) // End (or tile adjacent to end, in this case, the end itself)
        );

        assertEquals(expectedPath.size(), actualPath.size(), "Path length does not match.");
        for (int i = 0; i < expectedPath.size(); i++) {
            assertArrayEquals(expectedPath.get(i), actualPath.get(i),
                    "Path point " + i + " does not match.");
        }
    }

    @Test
    @DisplayName("Test 2: No Path Exists (Blocked)")
    void findPathBFS_noPathExists() {
        map = new GameMap("NoPathMap", 3, 3);

        // Default is GRASS. Set start and end points.
        // The blocker is implicitly GRASS if not set to PATH.
        map.setTileType(0, 0, TileType.START_POINT);
        map.setTileType(2, 2, TileType.END_POINT);
        // map.setTileType(1, 1, TileType.GRASS); // This is default

        List<int[]> actualPath = map.findPathBFS(map.getTile(0, 0), map.getTile(2, 2));

        assertNull(actualPath, "Path should be null when blocked.");
    }

    @Test
    @DisplayName("Test 3: Complex Path with Turns")
    void findPathBFS_complexPathWithTurns() {
        map = new GameMap("ComplexPathMap", 5, 5);

        // Define path tiles using setTileType
        // Default is GRASS
        map.setTileType(0, 2, TileType.START_POINT);
        map.setTileType(1, 2, TileType.PATH);
        map.setTileType(1, 1, TileType.PATH);
        map.setTileType(2, 1, TileType.PATH);
        map.setTileType(3, 1, TileType.PATH);
        map.setTileType(3, 2, TileType.PATH);
        map.setTileType(4, 2, TileType.END_POINT);

        List<int[]> actualPath = map.findPathBFS(map.getTile(0, 2), map.getTile(4, 2));

        assertNotNull(actualPath, "Path should not be null for a complex valid path.");

        List<int[]> expectedPath = Arrays.asList(
                p(0, 2), // Start
                p(1, 2),
                p(1, 1),
                p(2, 1),
                p(3, 1),
                p(3, 2),
                p(4, 2) // End point itself
        );

        assertEquals(expectedPath.size(), actualPath.size(), "Path length does not match for complex path.");
        for (int i = 0; i < expectedPath.size(); i++) {
            assertArrayEquals(expectedPath.get(i), actualPath.get(i),
                    "Path point " + i + " does not match for complex path.");
        }
    }

    @Test
    @DisplayName("Test 4: Path to End Point Itself (End Point is Walkable for BFS Target)")
    void findPathBFS_pathToEndpointItself() {
        map = new GameMap("EndpointWalkableMap", 3, 1);
        // Start (0,0), End (2,0). Path (1,0)
        map.setTileType(0, 0, TileType.START_POINT);
        map.setTileType(1, 0, TileType.PATH);
        map.setTileType(2, 0, TileType.END_POINT);

        List<int[]> actualPath = map.findPathBFS(map.getTile(0, 0), map.getTile(2, 0));

        assertNotNull(actualPath, "Path should not be null.");
        List<int[]> expectedPath = Arrays.asList(p(0, 0), p(1, 0), p(2, 0));

        assertEquals(expectedPath.size(), actualPath.size(), "Path length mismatch.");
        for (int i = 0; i < expectedPath.size(); i++) {
            assertArrayEquals(expectedPath.get(i), actualPath.get(i), "Path point " + i + " mismatch.");
        }
    }

    @Test
    @DisplayName("Test 5: Start and End are the Same Tile")
    void findPathBFS_startAndEndSameTile() {
        map = new GameMap("SameTileMap", 1, 1);
        // GameMap.setTileType ensures only one start/end.
        // If we set START then END on the same tile, it becomes END.
        map.setTileType(0, 0, TileType.START_POINT);
        map.setTileType(0, 0, TileType.END_POINT); // This will make tile (0,0) an END_POINT

        // For findPathBFS to work as intended when start and end are the same,
        // the startTile passed should be the conceptual start, and endTile the
        // conceptual end.
        // GameMap.generatePath() internally finds START_POINT and END_POINT tiles.
        // Here, we are directly testing findPathBFS.
        // The current findPathBFS logic might struggle if start and end are identical
        // *and* one is START and other is END type
        // because it looks for a tile *adjacent* to end OR the end tile itself *if it's
        // walkable*.
        // Let's make the tile a START_POINT for the start argument, and an END_POINT
        // for the end argument.
        // However, map.getTile(0,0) will reflect the latest setType, which is
        // END_POINT.

        // To test this scenario robustly for findPathBFS specifically:
        // We want a situation where the BFS algorithm is given a start tile and an end
        // tile that are at the same coordinates.
        // The internal logic of findPathBFS should handle this.
        // `(Math.abs(cx - targetX) <= 1 && cy == targetY) || (Math.abs(cy - targetY) <=
        // 1 && cx == targetX)`
        // If cx=targetX and cy=targetY, then this is true.

        // Re-setup for clarity:
        map = new GameMap("SameTileMap", 1, 1);
        map.setTileType(0, 0, TileType.START_POINT); // Now tile (0,0) is START_POINT
        // We need the end tile to be distinct for the method signature, even if it's
        // the same coords.
        // And it needs to be END_POINT type for the BFS termination condition as
        // designed in generatePath context.
        // This specific test is a bit artificial for findPathBFS alone, as generatePath
        // sets up specific start/end tiles.

        // Let's use the map's state after setting a single tile to be both logically.
        // GameMap will make it an END_POINT after the two setTileType calls.
        // The path should be to itself if it's also considered the target destination.
        Tile aTile = map.getTile(0, 0); // This tile is now END_POINT

        // To correctly test findPathBFS with start=end, they should be the same tile
        // object or logically identical.
        // And the tile should be considered 'walkable' or the target itself for BFS to
        // succeed.
        // The test setup for GameMap usually means start and end points are distinct.
        // If we set one tile to START, then END, it becomes END.
        // Let's simplify: set it to START, and pass it as both start and end to
        // findPathBFS.
        // The BFS should find it immediately if the termination allows current ==
        // target.

        map = new GameMap("SameTileMapForBFS", 1, 1);
        map.setTileType(0, 0, TileType.START_POINT);
        Tile aStartTile = map.getTile(0, 0);
        // For the end tile, we conceptually want the same location.
        // The findPathBFS target check is based on coordinates of endTile.
        // If we pass aStartTile as endTile, it's fine.

        List<int[]> actualPath = map.findPathBFS(aStartTile, aStartTile);

        assertNotNull(actualPath, "Path should exist if start and end are the same valid tile.");
        assertEquals(1, actualPath.size(), "Path should have one point if start and end are the same.");
        assertArrayEquals(p(0, 0), actualPath.get(0), "Path point should be the start/end tile itself.");
    }

    @Test
    @DisplayName("Test Constructor and Basic Getters")
    void constructorAndBasicGetters() {
        String mapName = "GetterMap";
        int mapWidth = 10;
        int mapHeight = 15;
        map = new GameMap(mapName, mapWidth, mapHeight);

        assertEquals(mapName, map.getName(), "Map name should match constructor argument.");
        assertEquals(mapWidth, map.getWidth(), "Map width should match constructor argument.");
        assertEquals(mapHeight, map.getHeight(), "Map height should match constructor argument.");
        assertEquals(GameMap.TILE_SIZE, map.getTileSize(), "Tile size should match static constant.");

        // Check default tile type
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                assertNotNull(map.getTile(x, y), "Tile at (" + x + "," + y + ") should not be null.");
                assertEquals(TileType.GRASS, map.getTileType(x, y), "Default tile type should be GRASS.");
            }
        }
    }

    @Test
    @DisplayName("Test getTile and getTileType with bounds")
    void getTile_Bounds() {
        map = new GameMap("BoundsMap", 3, 3);
        map.setTileType(1, 1, TileType.PATH);

        assertNotNull(map.getTile(1, 1), "Tile(1,1) should be retrievable.");
        assertEquals(TileType.PATH, map.getTileType(1, 1), "TileType at (1,1) should be PATH.");

        // Out of bounds
        assertNull(map.getTile(-1, 1), "Tile at (-1,1) should be null (out of bounds).");
        assertNull(map.getTile(1, -1), "Tile at (1,-1) should be null (out of bounds).");
        assertNull(map.getTile(3, 1), "Tile at (3,1) should be null (out of bounds).");
        assertNull(map.getTile(1, 3), "Tile at (1,3) should be null (out of bounds).");

        assertNull(map.getTileType(-1, 1), "TileType at (-1,1) should be null (out of bounds).");
        assertNull(map.getTileType(1, -1), "TileType at (1,-1) should be null (out of bounds).");
        assertNull(map.getTileType(3, 1), "TileType at (3,1) should be null (out of bounds).");
        assertNull(map.getTileType(1, 3), "TileType at (1,3) should be null (out of bounds).");
    }

    @Test
    @DisplayName("Test setName")
    void setName_ChangesName() {
        map = new GameMap("InitialName", 5, 5);
        String newName = "UpdatedName";
        map.setName(newName);
        assertEquals(newName, map.getName(), "setName should update the map's name.");
    }

    @Nested
    @DisplayName("setTileType Tests")
    class SetTileTypeTests {
        @BeforeEach
        void setUpMap() {
            map = new GameMap("SetTileTypeMap", 5, 5);
        }

        @Test
        @DisplayName("Changes tile type correctly")
        void setTileType_ChangesType() {
            map.setTileType(1, 1, TileType.PATH);
            assertEquals(TileType.PATH, map.getTileType(1, 1), "Tile type should be updated to PATH.");
            map.setTileType(1, 1, TileType.TOWER_SLOT);
            assertEquals(TileType.TOWER_SLOT, map.getTileType(1, 1), "Tile type should be updated to TOWER_SLOT.");
        }

        @Test
        @DisplayName("Setting START_POINT clears previous START_POINT")
        void setTileType_OneStartPoint() {
            map.setTileType(0, 0, TileType.START_POINT); // First start
            assertEquals(TileType.START_POINT, map.getTileType(0, 0));

            map.setTileType(1, 1, TileType.START_POINT); // New start
            assertEquals(TileType.GRASS, map.getTileType(0, 0), "Old START_POINT should become GRASS.");
            assertEquals(TileType.START_POINT, map.getTileType(1, 1), "New tile should be START_POINT.");
        }

        @Test
        @DisplayName("Setting END_POINT clears previous END_POINT")
        void setTileType_OneEndPoint() {
            map.setTileType(4, 4, TileType.END_POINT); // First end
            assertEquals(TileType.END_POINT, map.getTileType(4, 4));

            map.setTileType(3, 3, TileType.END_POINT); // New end
            assertEquals(TileType.GRASS, map.getTileType(4, 4), "Old END_POINT should become GRASS.");
            assertEquals(TileType.END_POINT, map.getTileType(3, 3), "New tile should be END_POINT.");
        }

        @Test
        @DisplayName("setTileType calls generatePath for START_POINT")
        void setTileType_CallsGeneratePathForStart() {
            map.setTileType(1, 1, TileType.END_POINT); // Ensure an END_POINT exists elsewhere
            assertNull(map.getEnemyPath(), "EnemyPath should initially be null or not set if start is missing.");

            map.setTileType(0, 0, TileType.START_POINT); // This should trigger generatePath
            // With both START_POINT (0,0) and END_POINT (1,1) now present, generatePath
            // should run.
            // Path may or may not exist depending on PATH tiles, but points should be set.
            assertNotNull(map.getStartPoint(),
                    "StartPoint field should be set after setting START_POINT tile and generatePath runs with an EndPoint present.");
            assertNotNull(map.getEndPoint(), // Also check EndPoint to confirm generatePath's effect
                    "EndPoint field should also be set/reconfirmed by generatePath.");
        }

        @Test
        @DisplayName("setTileType calls generatePath for END_POINT")
        void setTileType_CallsGeneratePathForEnd() {
            map.setTileType(1, 1, TileType.START_POINT); // Ensure a START_POINT exists elsewhere
            assertNull(map.getEnemyPath(), "EnemyPath should initially be null if end is missing.");

            map.setTileType(0, 0, TileType.END_POINT); // This should trigger generatePath
            // With both START_POINT (1,1) and END_POINT (0,0) now present, generatePath
            // should run.
            assertNotNull(map.getEndPoint(),
                    "EndPoint field should be set after setting END_POINT tile and generatePath runs with a StartPoint present.");
            assertNotNull(map.getStartPoint(), // Also check StartPoint
                    "StartPoint field should also be set/reconfirmed by generatePath.");
        }

        @Test
        @DisplayName("setTileType calls generatePath for PATH tile")
        void setTileType_CallsGeneratePathForPath() {
            map.setTileType(0, 0, TileType.START_POINT);
            map.setTileType(0, 2, TileType.END_POINT);
            // Path gen would have run once for START, once for END.
            // To test the PATH trigger, create a situation where path doesn't exist, then
            // add a PATH tile.
            map.setTileType(0, 1, TileType.GRASS); // Ensure (0,1) is not a path initially.
            map.generatePath(); // Recalculate: path from (0,0) to (0,2) should be null now.
            assertNull(map.getEnemyPath(), "Path should be null after breaking it with a GRASS tile.");

            map.setTileType(0, 1, TileType.PATH); // This should make a path and call generatePath.
            assertNotNull(map.getEnemyPath(), "Path should be generated after adding a connecting PATH tile.");
            assertFalse(map.getEnemyPath().getPoints().isEmpty(), "Generated path should not be empty.");
        }

        @Test
        @DisplayName("setTileType with out-of-bounds coordinates does nothing")
        void setTileType_OutOfBounds() {
            map.setTileType(-1, 0, TileType.PATH);
            assertNull(map.getTileType(-1, 0), "Setting out of bounds X (-1,0) should not change anything.");

            map.setTileType(0, -1, TileType.PATH);
            assertNull(map.getTileType(0, -1), "Setting out of bounds Y (0,-1) should not change anything.");

            map.setTileType(map.getWidth(), 0, TileType.PATH);
            assertNull(map.getTileType(map.getWidth(), 0),
                    "Setting out of bounds X (width,0) should not change anything.");

            map.setTileType(0, map.getHeight(), TileType.PATH);
            assertNull(map.getTileType(0, map.getHeight()),
                    "Setting out of bounds Y (0,height) should not change anything.");
        }
    }

    @Nested
    @DisplayName("generatePath Tests")
    class GeneratePathTests {
        @BeforeEach
        void setUpMap() {
            map = new GameMap("GeneratePathMap", 5, 5);
            // Ensure FX is disabled as generatePath might be called by setTileType, which
            // initializes tiles
            Tile.isFxAvailable = false;
        }

        @Test
        @DisplayName("generatePath does nothing if no START_POINT")
        void generatePath_NoStartPoint() {
            map.setTileType(2, 2, TileType.END_POINT);
            map.generatePath(); // Call directly
            assertNull(map.getEnemyPath(), "EnemyPath should be null if no START_POINT.");
            assertNull(map.getStartPoint(), "StartPoint field should be null.");
        }

        @Test
        @DisplayName("generatePath does nothing if no END_POINT")
        void generatePath_NoEndPoint() {
            map.setTileType(0, 0, TileType.START_POINT);
            map.generatePath(); // Call directly
            assertNull(map.getEnemyPath(), "EnemyPath should be null if no END_POINT.");
            assertNull(map.getEndPoint(), "EndPoint field should be null.");
        }

        @Test
        @DisplayName("generatePath sets null path if START and END are not connected")
        void generatePath_NoConnection() {
            map.setTileType(0, 0, TileType.START_POINT);
            map.setTileType(4, 4, TileType.END_POINT);
            // No PATH tiles connecting them
            map.generatePath();
            assertNull(map.getEnemyPath(), "EnemyPath should be null if START and END are not connected.");
        }

        @Test
        @DisplayName("generatePath succeeds with a valid connection")
        void generatePath_Success() {
            map.setTileType(0, 1, TileType.START_POINT);
            map.setTileType(1, 1, TileType.PATH);
            map.setTileType(2, 1, TileType.END_POINT);
            // setTileType for END_POINT would have called generatePath
            // map.generatePath(); // Explicit call to ensure direct test if needed

            assertNotNull(map.getEnemyPath(), "EnemyPath should not be null for a valid setup.");
            assertNotNull(map.getStartPoint(), "StartPoint field should be set.");
            assertNotNull(map.getEndPoint(), "EndPoint field should be set.");
            //
            assertFalse(map.getEnemyPath().getPoints().isEmpty(), "EnemyPath should contain points.");

            //
            // Verify coordinates are pixel centers based on 32px logic size in generatePath
            // for points
            // and TILE_SIZE (64) for path segment calculations in findPathBFS
            // The startPoint/endPoint in GameMap are based on a 32px tile size assumption
            // from generatePath itself.
            final int LOGIC_TS = 32;
            assertEquals(0 * LOGIC_TS + LOGIC_TS / 2, map.getStartPoint().getX(), "StartPoint X coord mismatch.");
            assertEquals(1 * LOGIC_TS + LOGIC_TS / 2, map.getStartPoint().getY(), "StartPoint Y coord mismatch.");
            assertEquals(2 * LOGIC_TS + LOGIC_TS / 2, map.getEndPoint().getX(), "EndPoint X coord mismatch.");
            assertEquals(1 * LOGIC_TS + LOGIC_TS / 2, map.getEndPoint().getY(), "EndPoint Y coord mismatch.");
        }
    }

    @Nested
    @DisplayName("canPlaceTower Tests")
    class CanPlaceTowerTests {
        private List<Tower> towers; // Use actual Tower objects if needed, or mocks

        @BeforeEach
        void setUpMapAndTowers() {
            map = new GameMap("TowerPlaceMap", 5, 5);
            // Ensure FX is disabled as Tile initialization might occur
            Tile.isFxAvailable = false;
            towers = new ArrayList<>();
            // Setup a TOWER_SLOT for valid placement tests
            map.setTileType(2, 2, TileType.TOWER_SLOT);
            map.setTileType(1, 1, TileType.GRASS); // For invalid placement
            map.setTileType(0, 0, TileType.PATH); // For invalid placement
        }

        @Test
        @DisplayName("Can place on an empty TOWER_SLOT")
        void canPlaceTower_ValidEmptySlot() {
            assertTrue(map.canPlaceTower(2 * map.getTileSize() + 5, 2 * map.getTileSize() + 5, towers),
                    "Should be able to place on an empty TOWER_SLOT.");
        }

        @Test
        @DisplayName("Cannot place on GRASS tile")
        void canPlaceTower_InvalidGrass() {
            assertFalse(map.canPlaceTower(1 * map.getTileSize() + 5, 1 * map.getTileSize() + 5, towers),
                    "Should not be able to place on a GRASS tile.");
        }

        @Test
        @DisplayName("Cannot place on PATH tile")
        void canPlaceTower_InvalidPath() {
            assertFalse(map.canPlaceTower(0 * map.getTileSize() + 5, 0 * map.getTileSize() + 5, towers),
                    "Should not be able to place on a PATH tile.");
        }

        @Test
        @DisplayName("Cannot place on an occupied TOWER_SLOT")
        void canPlaceTower_OccupiedSlot() {
            // Simulate placing a tower by adding its coordinates to the list
            // The Tower class itself isn't strictly needed for this GameMap logic test if
            // we mock its position
            // For simplicity, we use a dummy tower-like object or just its coords.
            // Actual Tower object would be needed if constructor/getters were complex.
            // We need a way to represent a tower at tile (2,2)
            // Assuming Tower has getX(), getY() that return top-left pixel coords
            Tower existingTower = new ArcherTower(2 * map.getTileSize(), 2 * map.getTileSize()); // Mock or use real
            towers.add(existingTower);

            assertFalse(map.canPlaceTower(2 * map.getTileSize() + 5, 2 * map.getTileSize() + 5, towers),
                    "Should not be able to place on an already occupied TOWER_SLOT.");
        }

        @Test
        @DisplayName("Cannot place tower out of bounds")
        void canPlaceTower_OutOfBounds() {
            assertFalse(map.canPlaceTower(-10, 2 * map.getTileSize() + 5, towers),
                    "Should not place out of bounds (negative X).");
            assertFalse(map.canPlaceTower(2 * map.getTileSize() + 5, -10, towers),
                    "Should not place out of bounds (negative Y).");
            assertFalse(map.canPlaceTower(10 * map.getTileSize(), 2 * map.getTileSize() + 5, towers),
                    "Should not place out of bounds (large X).");
            assertFalse(map.canPlaceTower(2 * map.getTileSize() + 5, 10 * map.getTileSize(), towers),
                    "Should not place out of bounds (large Y).");
        }

        @Test
        @DisplayName("Can place on TOWER_SLOT even if other towers exist elsewhere")
        void canPlaceTower_ValidSlotWithOtherTowers() {
            Tower otherTower = new ArcherTower(0 * map.getTileSize(), 0 * map.getTileSize()); // Placed on a different
                                                                                              // tile
            towers.add(otherTower);
            assertTrue(map.canPlaceTower(2 * map.getTileSize() + 5, 2 * map.getTileSize() + 5, towers),
                    "Should be able to place on TOWER_SLOT if other towers are on different tiles.");
        }
    }

    @Nested
    @DisplayName("setTileAsOccupiedByTower Tests")
    class SetTileAsOccupiedByTowerTests {
        @BeforeEach
        void setUpMap() {
            map = new GameMap("OccupyMap", 5, 5);
            Tile.isFxAvailable = false;
            map.setTileType(2, 2, TileType.TOWER_SLOT);
            map.setTileType(1, 1, TileType.GRASS); // Non-tower slot for testing invalid cases
        }

        @Test
        @DisplayName("Occupying a TOWER_SLOT changes its type to GRASS")
        void occupyTowerSlot_ChangesTypeToGrass() {
            map.setTileAsOccupiedByTower(2, 2, true);
            assertEquals(TileType.GRASS, map.getTileType(2, 2),
                    "Occupied TOWER_SLOT should change to GRASS.");
        }

        @Test
        @DisplayName("Unoccupying a previously GRASS (occupied) tile changes it back to TOWER_SLOT")
        void unoccupy_ChangesTypeToTowerSlot() {
            map.setTileAsOccupiedByTower(2, 2, true); // Occupy first, becomes GRASS
            assertEquals(TileType.GRASS, map.getTileType(2, 2));

            map.setTileAsOccupiedByTower(2, 2, false); // Unoccupy
            assertEquals(TileType.TOWER_SLOT, map.getTileType(2, 2),
                    "Unoccupied tile should revert to TOWER_SLOT.");
        }

        @Test
        @DisplayName("Attempting to occupy a non-TOWER_SLOT tile does not change its type")
        void occupyNonTowerSlot_DoesNothing() {
            assertEquals(TileType.GRASS, map.getTileType(1, 1));
            map.setTileAsOccupiedByTower(1, 1, true); // Attempt to occupy GRASS
            assertEquals(TileType.GRASS, map.getTileType(1, 1),
                    "Attempting to occupy GRASS tile should not change its type.");
        }

        @Test
        @DisplayName("setTileAsOccupiedByTower with out-of-bounds coordinates does nothing")
        void setTileAsOccupied_OutOfBounds() {
            TileType initialTypeAtValidSlot = map.getTileType(2, 2);
            map.setTileAsOccupiedByTower(-1, 0, true);
            assertEquals(initialTypeAtValidSlot, map.getTileType(2, 2),
                    "Out of bounds occupy should not affect other tiles.");

            map.setTileAsOccupiedByTower(0, map.getHeight(), false);
            assertEquals(initialTypeAtValidSlot, map.getTileType(2, 2),
                    "Out of bounds unoccupy should not affect other tiles.");
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {
        @Test
        @DisplayName("Serializing and Deserializing GameMap reconstructs transient fields")
        void serializeDeserialize_ReconstructsTransients() throws IOException, ClassNotFoundException {
            String mapName = "SerializableMap";
            GameMap originalMap = new GameMap(mapName, 5, 5);
            // Tile.isFxAvailable is set to false by @BeforeAll in this test class
            originalMap.setTileType(0, 1, TileType.START_POINT);
            originalMap.setTileType(1, 1, TileType.PATH);
            originalMap.setTileType(2, 1, TileType.END_POINT); // This triggers generatePath

            // Ensure path and points are generated before serialization
            assertNotNull(originalMap.getEnemyPath(), "Original map should have enemy path.");
            assertNotNull(originalMap.getStartPoint(), "Original map should have start point.");
            assertNotNull(originalMap.getEndPoint(), "Original map should have end point.");
            // Since Tile.isFxAvailable = false, image should be null
            assertNull(originalMap.getTile(0, 1).getImage(),
                    "Original map tile image should be null as FX is disabled.");

            // Serialize
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(originalMap);
            oos.close();

            // Deserialize
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            GameMap deserializedMap = (GameMap) ois.readObject();
            ois.close();

            // Assertions
            assertEquals(mapName, deserializedMap.getName(), "Deserialized map name should match.");
            assertEquals(originalMap.getWidth(), deserializedMap.getWidth(), "Width should match.");
            assertEquals(originalMap.getHeight(), deserializedMap.getHeight(), "Height should match.");

            assertNotNull(deserializedMap.getEnemyPath(), "Deserialized map should reconstruct enemy path.");
            assertNotNull(deserializedMap.getStartPoint(), "Deserialized map should reconstruct start point.");
            assertNotNull(deserializedMap.getEndPoint(), "Deserialized map should reconstruct end point.");

            // Check path details (simple check on point count)
            assertEquals(originalMap.getEnemyPath().getPoints().size(),
                    deserializedMap.getEnemyPath().getPoints().size(),
                    "Path point count should match.");

            // Check if tile images are re-initialized (transient in Tile)
            // This relies on Tile.reinitializeAfterLoad() and assumes FX is available for
            // image loading part of the test.
            // If FX is strictly disabled for all tests via a global @BeforeAll, this image
            // check might need adjustment
            // or run in a context where Tile.isFxAvailable can be true for this specific
            // test.
            // For now, assuming the @BeforeAll for GameMapTest sets Tile.isFxAvailable =
            // false,
            // then getImage() would be null. If we want to test re-initialization WITH
            // images,
            // Tile.isFxAvailable would need to be true here.
            if (Tile.isFxAvailable) { // Only check image if FX is supposed to be available
                assertNotNull(deserializedMap.getTile(0, 1).getImage(),
                        "Deserialized map tile should reinitialize its image if FX available.");
            } else {
                assertNull(deserializedMap.getTile(0, 1).getImage(),
                        "Deserialized map tile image should be null if FX is disabled.");
            }
            assertEquals(TileType.START_POINT, deserializedMap.getTileType(0, 1), "Tile type should be preserved.");
        }
    }
}