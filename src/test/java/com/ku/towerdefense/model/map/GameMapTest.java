package com.ku.towerdefense.model.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

import com.ku.towerdefense.model.map.GameMap;
import com.ku.towerdefense.model.map.Tile;
import com.ku.towerdefense.model.map.TileType;

import java.util.List;
import java.util.Arrays;

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

}