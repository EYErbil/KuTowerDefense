package towerdefense.model;

/**
 * Placeholder class representing the game map data.
 * TODO: Implement methods for setting/getting tiles, dimensions, validation
 * etc.
 */
public class GameMap {

    private int width;
    private int height;
    // private Object[][] tiles; // Example data structure

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        // Initialize tiles array or other structure
        System.out.println("Placeholder GameMap created: " + width + "x" + height);
    }

    // TODO: Add methods like getTile(row, col), setTile(row, col, type),
    // validate(), etc.

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}