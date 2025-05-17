package com.ku.towerdefense.model.effects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class AnimatedEffect {
    private final Image spriteSheet;
    private final double x, y; // Center position for the effect
    private final int frameWidth, frameHeight, totalFrames;
    private final double frameDuration;
    private final double displayWidth, displayHeight; // Desired rendering size

    private int currentFrame = 0;
    private double timeAccum = 0;
    private boolean active = true;
    private Runnable onCompletionCallback;

    public AnimatedEffect(Image spriteSheet,
                          double x, double y, // World coordinates for the center of the effect
                          int frameWidth, int frameHeight, // Source frame dimensions from sprite sheet
                          int totalFrames,
                          double frameDurationSeconds,
                          double displayWidth, double displayHeight) { // Display dimensions
        this.spriteSheet = spriteSheet;
        this.x = x;
        this.y = y;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.totalFrames = totalFrames;
        this.frameDuration = frameDurationSeconds;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    // Overloaded constructor for effects that render at their native frame size
    public AnimatedEffect(Image spriteSheet,
                          double x, double y,
                          int frameWidth, int frameHeight,
                          int totalFrames,
                          double frameDurationSeconds) {
        this(spriteSheet, x, y, frameWidth, frameHeight, totalFrames, frameDurationSeconds, frameWidth, frameHeight);
    }

    public void setOnCompletion(Runnable callback) {
        this.onCompletionCallback = callback;
    }

    public void update(double dt) {
        if (!active) return;
        timeAccum += dt;
        if (timeAccum >= frameDuration) {
            timeAccum -= frameDuration;
            currentFrame++;
            if (currentFrame >= totalFrames) {
                active = false;
                if (onCompletionCallback != null) {
                    onCompletionCallback.run();
                }
            }
        }
    }

    public void render(GraphicsContext gc) {
        if (!active || spriteSheet == null) return;
        // assume horizontal strip of frames
        double sx = currentFrame * frameWidth;
        gc.drawImage(spriteSheet,
                     sx, 0, frameWidth, frameHeight,           // Source rectangle from sprite sheet
                     x - displayWidth / 2.0, y - displayHeight / 2.0, // Destination top-left on canvas
                     displayWidth, displayHeight);                     // Destination width & height on canvas
    }

    public boolean isActive() {
        return active;
    }
} 