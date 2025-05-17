package com.ku.towerdefense.model.map;

import com.ku.towerdefense.model.GamePath;
import com.ku.towerdefense.model.entity.Tower;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable game‑map that stores a 2‑D array of {@link Tile}s plus the
 * derived enemy {@link GamePath}.  All JavaFX objects are kept <em>transient</em>
 * and rebuilt after loading so maps can safely be written with plain Java
 * serialization.
 */
public class GameMap implements Serializable {
    private static final long serialVersionUID = 1L;

    /* ------------------------------------------------------------------
     *  Core data – these are written to disk
     * ------------------------------------------------------------------ */
    private String name;
    private int width, height;
    private Tile[][] tiles;

    /* mirror of the (transient) start/end Points so they survive I/O */
    private int[] startXY;   // [px, py]
    private int[] endXY;     // [px, py]

    /* ------------------------------------------------------------------
     *  Transient caches – rebuilt on demand / after load
     * ------------------------------------------------------------------ */
    private transient Point2D startPoint;
    private transient Point2D endPoint;
    private transient GamePath enemyPath;

    /* ------------------------------------------------------------------
     *  C‑TOR
     * ------------------------------------------------------------------ */
    public GameMap(String name, int width, int height) {
        this.name   = name;
        this.width  = width;
        this.height = height;
        this.tiles  = new Tile[width][height];

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                tiles[x][y] = new Tile(x, y, TileType.GRASS);
    }

    /* ------------------------------------------------------------------
     *  Basic getters/setters that UI code relies on
     * ------------------------------------------------------------------ */
    public String getName()               { return name; }
    public void   setName(String newName) { this.name = newName; }

    public int  getWidth()  { return width; }
    public int  getHeight() { return height; }

    // Constant for tile size, used throughout the game logic for this map
    // It's important this matches the TS used in pathfinding and rendering if they are hardcoded there.
    // For now, we assume 64 based on recent fixes.
    private static final int TILE_SIZE = 64;

    public int getTileSize() { return TILE_SIZE; }

    public Tile       getTile(int x,int y)        { return inBounds(x,y) ? tiles[x][y] : null; }
    public TileType   getTileType(int x,int y)    { Tile t = getTile(x,y); return t==null ? null : t.getType(); }

    /* ------------------------------------------------------------------
     *  Map editing helpers
     * ------------------------------------------------------------------ */
    public void setTileType(int x,int y,TileType type){
        if(!inBounds(x,y)) return;
        // keep only ONE start / end on the map
        if(type==TileType.START_POINT) clearType(TileType.START_POINT);
        if(type==TileType.END_POINT)   clearType(TileType.END_POINT);
            tiles[x][y].setType(type);
        if(type==TileType.START_POINT||type==TileType.END_POINT) generatePath();
    }

    private void clearType(TileType tt){
        for(Tile[] row:tiles) for(Tile t:row)
            if(t.getType()==tt) t.setType(TileType.GRASS);
    }

    private boolean inBounds(int x,int y){ return x>=0 && x<width && y>=0 && y<height; }

    /* ------------------------------------------------------------------
     *  Enemy path generation – improved version that follows actual path tiles
     * ------------------------------------------------------------------ */
    public void generatePath() {
        // Find START_POINT and END_POINT tiles
        Tile startTile = null, endTile = null;
        for (Tile[] row : tiles) {
            for (Tile t : row) {
                if (t.getType() == TileType.START_POINT) startTile = t;
                else if (t.getType() == TileType.END_POINT) endTile = t;
            }
        }
        
        // If we don't have both start and end points, we can't generate a path
        if (startTile == null || endTile == null) {
            System.err.println("Cannot generate path: Missing " + 
                             (startTile == null ? "START_POINT" : "") +
                             (endTile == null ? "END_POINT" : ""));
            enemyPath = null;
            return;
        }

        final int TS = 32; // logic coords: 32 px per tile
        startPoint = new Point2D(startTile.getX() * TS + TS/2, startTile.getY() * TS + TS/2);
        endPoint = new Point2D(endTile.getX() * TS + TS/2, endTile.getY() * TS + TS/2);
        startXY = new int[] { (int)startPoint.getX(), (int)startPoint.getY() };
        endXY = new int[] { (int)endPoint.getX(), (int)endPoint.getY() };

        // Use BFS to find a path from start to end
        List<int[]> pathPoints = findPathBFS(startTile, endTile);
        
        // If no path found, show error and return
        if (pathPoints == null || pathPoints.isEmpty()) {
            System.err.println("No valid path found from START_POINT to END_POINT! Make sure they're connected by path tiles.");
            enemyPath = null;
            return;
        }
        
        // Create GamePath from the points
        enemyPath = new GamePath(pathPoints);
        System.out.println("Path generated successfully with " + pathPoints.size() + " points");
    }
    
    /**
     * Uses Breadth-First Search to find a path from start to end following walkable tiles.
     * @return List of [x,y] coordinates for the path in tile space
     */
    private List<int[]> findPathBFS(Tile startTile, Tile endTile) {
        final int TS = 64; // pixel size of tiles
        
        // Directions: right, down, left, up
        int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        
        // Keep track of visited tiles and their parent tiles
        boolean[][] visited = new boolean[width][height];
        int[][][] parent = new int[width][height][2]; // Store x,y of parent
        
        // Initialize queue with start tile
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.add(new int[] {startTile.getX(), startTile.getY()});
        visited[startTile.getX()][startTile.getY()] = true;
        
        // Target coordinates
        int targetX = endTile.getX();
        int targetY = endTile.getY();
        
        boolean pathFound = false;
        
        // BFS loop
        while (!queue.isEmpty() && !pathFound) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];
            
            // Check if we've reached the end tile or one of its neighbors
            // (since the end is typically not walkable itself)
            if ((Math.abs(cx - targetX) <= 1 && cy == targetY) || 
                (Math.abs(cy - targetY) <= 1 && cx == targetX)) {
                // Found the end or a tile next to it
                pathFound = true;
                // Update target position to the last walkable tile
                targetX = cx;
                targetY = cy;
                    break;
            }

            // Check all four directions
            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                
                // Skip if out of bounds
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                
                // Skip if already visited
                if (visited[nx][ny]) continue;
                
                // Only consider walkable tiles (including the end point's location)
                Tile nextTile = tiles[nx][ny];
                if (!nextTile.isWalkable() && !(nx == endTile.getX() && ny == endTile.getY())) continue;
                
                // Mark as visited and save parent
                visited[nx][ny] = true;
                parent[nx][ny] = new int[] {cx, cy};
                
                // Add to queue
                queue.add(new int[] {nx, ny});
            }
        }
        
        // If we didn't find a path
        if (!pathFound) {
            return null;
        }
        
        // Reconstruct the path from end to start
        List<int[]> reversePath = new ArrayList<>();
        int cx = targetX;
        int cy = targetY;
        
        // Start with the end point
        reversePath.add(new int[] {cx * TS + TS/2, cy * TS + TS/2});
        
        // Work backwards to the start
        while (!(cx == startTile.getX() && cy == startTile.getY())) {
            int[] p = parent[cx][cy];
            cx = p[0];
            cy = p[1];
            reversePath.add(new int[] {cx * TS + TS/2, cy * TS + TS/2});
        }
        
        // Reverse the path to get start-to-end order
        List<int[]> forwardPath = new ArrayList<>();
        for (int i = reversePath.size() - 1; i >= 0; i--) {
            forwardPath.add(reversePath.get(i));
        }
        
        return forwardPath;
    }

    public GamePath getEnemyPath(){ return enemyPath; }
    public Point2D  getStartPoint(){ if(startPoint==null&&startXY!=null) startPoint=new Point2D(startXY[0],startXY[1]); return startPoint; }
    public Point2D  getEndPoint(){ if(endPoint==null&&endXY!=null) endPoint=new Point2D(endXY[0],endXY[1]); return endPoint; }

    /* ------------------------------------------------------------------
     *  Tower placement rule used by gameplay layer
     * ------------------------------------------------------------------ */
    public boolean canPlaceTower(double px,double py,List<Tower> towers){
        int tx = (int)(px/getTileSize()), ty = (int)(py/getTileSize());
        Tile t = getTile(tx,ty);
        if(t==null || !t.canPlaceTower()) return false;
        // Check if any existing tower's center falls into the target tile
        // This logic assumes tower.getX() and .getY() are top-left of the tile.
        return towers.stream().noneMatch(existingTower -> {
            int existingTowerTileX = (int)(existingTower.getX() / getTileSize());
            int existingTowerTileY = (int)(existingTower.getY() / getTileSize());
            return existingTowerTileX == tx && existingTowerTileY == ty;
        });
    }

    /* ------------------------------------------------------------------
     *  Minimal rendering (editor / preview)
     * ------------------------------------------------------------------ */
    public void render(GraphicsContext gc){
        gc.setFill(Color.web("#282828"));
        gc.fillRect(0,0,width*getTileSize(),height*getTileSize());
        for(int y=0;y<height;y++) for(int x=0;x<width;x++)
            tiles[x][y].render(gc, x, y, getTileSize(), false);
        if(enemyPath!=null){
            var pts = enemyPath.getPoints();
            gc.setStroke(Color.YELLOW); gc.setLineWidth(2); gc.setGlobalAlpha(0.35);
            for(int i=0;i<pts.size()-1;i++) gc.strokeLine(pts.get(i).getX(),pts.get(i).getY(), pts.get(i+1).getX(),pts.get(i+1).getY());
            gc.setGlobalAlpha(1);
        }
    }

    /* ------------------------------------------------------------------
     *  Custom serialisation so transient fields get rebuilt
     * ------------------------------------------------------------------ */
    private void writeObject(ObjectOutputStream out) throws IOException {
        if(startPoint!=null) startXY = new int[]{ (int)startPoint.getX(), (int)startPoint.getY() };
        if(endPoint  !=null) endXY   = new int[]{ (int)endPoint.getX(),   (int)endPoint.getY()   };
        out.defaultWriteObject();
    }
    private void readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
        in.defaultReadObject();
        
        // Properly reinitialize all tiles after loading
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (tiles[x][y] != null) {
                    // Force tile to reload its image
                    tiles[x][y].reinitializeAfterLoad();
                }
            }
        }
        
        // Rebuild path and endpoint/startpoint
        if(startXY!=null) startPoint = new Point2D(startXY[0], startXY[1]);
        if(endXY!=null) endPoint = new Point2D(endXY[0], endXY[1]);
        generatePath(); // rebuild enemyPath + tile markings
    }
}
