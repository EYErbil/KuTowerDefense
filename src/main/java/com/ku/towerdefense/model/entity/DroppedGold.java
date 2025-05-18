package com.ku.towerdefense.model.entity;

import com.ku.towerdefense.ui.UIAssets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.Serializable;

public class DroppedGold extends Entity implements Serializable {
    private static final long serialVersionUID = 1L;
    // private static final String IMAGE_FILE = "gold_bag.png"; // No longer a separate bag image
    private static final String GOLD_SPAWN_SHEET_KEY = "GoldSpawnEffect";
    private static final int GOLD_SPAWN_FRAME_WIDTH = 128;
    private static final int GOLD_SPAWN_FRAME_HEIGHT = 128;
    private static final int GOLD_SPAWN_TOTAL_FRAMES = 7;
    private static final int STATIC_GOLD_FRAME_INDEX = GOLD_SPAWN_TOTAL_FRAMES - 1; // Use the last frame

    private static final long LIFESPAN_MS = 10000; // 10 seconds

    private final int goldAmount;
    private final long creationTimeMs;
    private transient Image staticGoldImage; // Will hold the last frame of G_Spawn.png

    public DroppedGold(double worldX, double worldY, int goldAmount) {
        // Position is center of the gold pile, width/height define its clickable area / visual size (128x128)
        super(worldX - (GOLD_SPAWN_FRAME_WIDTH / 2.0), 
              worldY - (GOLD_SPAWN_FRAME_HEIGHT / 2.0), 
              GOLD_SPAWN_FRAME_WIDTH, 
              GOLD_SPAWN_FRAME_HEIGHT);
        this.goldAmount = goldAmount;
        this.creationTimeMs = System.currentTimeMillis();
        loadStaticImage();
    }

    private void loadStaticImage() {
        if (this.staticGoldImage == null) { 
            this.staticGoldImage = UIAssets.getSpriteFrame(
                GOLD_SPAWN_SHEET_KEY, 
                STATIC_GOLD_FRAME_INDEX, 
                GOLD_SPAWN_FRAME_WIDTH, 
                GOLD_SPAWN_FRAME_HEIGHT
            );
            if (this.staticGoldImage == null) {
                System.err.println("DroppedGold: Failed to load static frame (" + STATIC_GOLD_FRAME_INDEX + 
                                   ") from sheet " + GOLD_SPAWN_SHEET_KEY);
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (staticGoldImage == null) { 
            loadStaticImage(); // Attempt to reload if null (e.g., after deserialization)
        }

        if (staticGoldImage != null) {
            gc.drawImage(staticGoldImage, getX(), getY(), getWidth(), getHeight());
        } else {
            // Fallback rendering if image is still null
            gc.setFill(javafx.scene.paint.Color.GOLD);
            gc.fillRect(getX(), getY(), getWidth(), getHeight()); // Square for placeholder
            gc.setStroke(javafx.scene.paint.Color.DARKGOLDENROD);
            gc.strokeRect(getX(), getY(), getWidth(), getHeight());
        }
    }

    // Update method is not strictly needed if GameController manages lifespan check based on isExpired()
    // public void update(double deltaTime) { /* No per-frame logic needed for static bag */ }

    public boolean isExpired() {
        return System.currentTimeMillis() > creationTimeMs + LIFESPAN_MS;
    }

    public int getGoldAmount() {
        return goldAmount;
    }
    
    public void reinitializeAfterLoad() {
        loadStaticImage(); // Reload transient image
    }
} 