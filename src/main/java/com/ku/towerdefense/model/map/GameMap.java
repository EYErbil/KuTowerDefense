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
     *  Enemy path generation – super simple: go horizontally then vertically
     * ------------------------------------------------------------------ */
    public void generatePath(){
        Tile s=null,e=null;
        for(Tile[] row:tiles) for(Tile t:row){
            if(t.getType()==TileType.START_POINT) s=t;
            else if(t.getType()==TileType.END_POINT) e=t;
        }
        if(s==null||e==null){ enemyPath=null; return; }

        final int TS = 32; // logic coords: 32 px per tile
        startPoint = new Point2D(s.getX()*TS+16, s.getY()*TS+16);
        endPoint   = new Point2D(e.getX()*TS+16, e.getY()*TS+16);
        startXY = new int[]{ (int)startPoint.getX(), (int)startPoint.getY() };
        endXY   = new int[]{ (int)endPoint.getX(),   (int)endPoint.getY()   };

        List<int[]> pts = new ArrayList<>();
        pts.add(new int[]{ s.getX()*TS+TS/2, s.getY()*TS+TS/2 });
        int cx = s.getX(), cy = s.getY();
        while(cx!=e.getX()){
            cx += (e.getX()>cx?1:-1);
            tiles[cx][cy].setType(TileType.PATH_HORIZONTAL);
            pts.add(new int[]{ cx*TS+TS/2, cy*TS+TS/2 });
        }
        while(cy!=e.getY()){
            cy += (e.getY()>cy?1:-1);
            tiles[cx][cy].setType(TileType.PATH_VERTICAL);
            pts.add(new int[]{ cx*TS+TS/2, cy*TS+TS/2 });
        }
        pts.add(new int[]{ e.getX()*TS+TS/2, e.getY()*TS+TS/2 });
        enemyPath = new GamePath(pts);
    }

    public GamePath getEnemyPath(){ return enemyPath; }
    public Point2D  getStartPoint(){ if(startPoint==null&&startXY!=null) startPoint=new Point2D(startXY[0],startXY[1]); return startPoint; }
    public Point2D  getEndPoint(){ if(endPoint==null&&endXY!=null) endPoint=new Point2D(endXY[0],endXY[1]); return endPoint; }

    /* ------------------------------------------------------------------
     *  Tower placement rule used by gameplay layer
     * ------------------------------------------------------------------ */
    public boolean canPlaceTower(double px,double py,List<Tower> towers){
        int tx = (int)(px/32), ty = (int)(py/32);
        Tile t = getTile(tx,ty);
        if(t==null || !t.canPlaceTower()) return false;
        return towers.stream().noneMatch(tv-> ((int)(tv.getCenterX()/32)==tx && (int)(tv.getCenterY()/32)==ty));
    }

    /* ------------------------------------------------------------------
     *  Minimal rendering (editor / preview)
     * ------------------------------------------------------------------ */
    public void render(GraphicsContext gc){
        final int TS = 32;
        gc.setFill(Color.web("#282828"));
        gc.fillRect(0,0,width*TS,height*TS);
        for(int y=0;y<height;y++) for(int x=0;x<width;x++)
            tiles[x][y].render(gc,TS);
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
        if(startXY!=null) startPoint = new Point2D(startXY[0], startXY[1]);
        if(endXY  !=null) endPoint   = new Point2D(endXY[0],   endXY[1]);
        generatePath(); // rebuild enemyPath + tile markings
    }
}
