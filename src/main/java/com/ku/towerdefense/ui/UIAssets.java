package com.ku.towerdefense.ui;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utility class for loading and managing UI assets.
 */
public class UIAssets {
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static ImageCursor customCursor;

    /**
     * Initialize all UI assets.
     */
    public static void initialize() {
        loadImages();
        createCustomCursor();
    }

    /**
     * Load images from the Asset_pack/UI folder.
     */
    private static void loadImages() {
        // Get the base path from the class loader
        String basePath = "/Asset_pack/UI/";

        try {
            // Load all button images
            String[] buttonFiles = {
                    "Button_Blue.png",
                    "Button_Blue_Pressed.png",
                    "Button_Red.png",
                    "Button_Red_Pressed.png",
                    "Button_Hover.png",
                    "Button_Disable.png",
                    "Button_Blue_3Slides.png",
                    "Button_Red_3Slides.png",
                    "Button_Hover_3Slides.png",
                    "Button_Disable_3Slides.png",
            };

            for (String file : buttonFiles) {
                String key = file.replace(".png", "");
                loadImage(key, basePath + file);
            }

            // Load other UI images
            loadImage("GameUI", basePath + "Coin_Health_Wave.png");
            loadImage("Ribbon_Blue", basePath + "Ribbon_Blue_3Slides.png");
            loadImage("Ribbon_Red", basePath + "Ribbon_Red_3Slides.png");
            loadImage("Ribbon_Yellow", basePath + "Ribbon_Yellow_3Slides.png");
            loadImage("KUTowerButtons", basePath + "kutowerbuttons4.png");
            loadImage("01", basePath + "01.png");

            System.out.println("UI assets loaded successfully - " + imageCache.size() + " images");
        } catch (Exception e) {
            System.err.println("Error loading UI assets: " + e.getMessage());
        }
    }

    /**
     * Load a single image into the cache.
     * 
     * @param name image name/key
     * @param path file path
     */
    private static void loadImage(String name, String path) {
        try {
            Image image = new Image(UIAssets.class.getResourceAsStream(path));
            if (image != null) {
                imageCache.put(name, image);
            } else {
                System.err.println("Failed to load image: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error loading image " + path + ": " + e.getMessage());
        }
    }

    /**
     * Create a custom cursor.
     */
    private static void createCustomCursor() {
        try {
            Image cursorImage = imageCache.get("01");
            if (cursorImage != null) {
                // Hotspot at center of the image
                customCursor = new ImageCursor(cursorImage, cursorImage.getWidth() / 2, cursorImage.getHeight() / 2);
                System.out.println("Created custom cursor");
            } else {
                System.err.println("Cursor image not found");
            }
        } catch (Exception e) {
            System.err.println("Failed to create custom cursor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the custom cursor.
     * 
     * @return the custom cursor, or null if not available
     */
    public static ImageCursor getCustomCursor() {
        return customCursor;
    }

    /**
     * Get an image from the cache.
     * 
     * @param name image name/key
     * @return the image, or null if not found
     */
    public static Image getImage(String name) {
        return imageCache.get(name);
    }

    /**
     * Apply a styled button appearance.
     * 
     * @param button the button to style
     * @param type   "blue" or "red"
     */
    public static void styleButton(Button button, String type) {
        try {
            String normalKey = "Button_" + type.substring(0, 1).toUpperCase() + type.substring(1);
            String pressedKey = normalKey + "_Pressed";

            Image normalImage = imageCache.get(normalKey);
            Image pressedImage = imageCache.get(pressedKey);
            Image hoverImage = imageCache.get("Button_Hover");

            if (normalImage != null && pressedImage != null && hoverImage != null) {
                // Create ImageView with fixed dimensions
                ImageView iv = new ImageView(normalImage);
                iv.setFitWidth(normalImage.getWidth());
                iv.setFitHeight(normalImage.getHeight());
                button.setGraphic(iv);
                button.setStyle("-fx-background-color: transparent; -fx-background-image: none;");

                // Handle states - reuse the same ImageView to preserve dimensions
                button.setOnMousePressed(e -> ((ImageView) button.getGraphic()).setImage(pressedImage));
                button.setOnMouseReleased(e -> ((ImageView) button.getGraphic()).setImage(normalImage));
                button.setOnMouseEntered(e -> {
                    button.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-background-image: none;");
                    if (!button.isPressed()) {
                        ((ImageView) button.getGraphic()).setImage(hoverImage);
                    }
                });
                button.setOnMouseExited(e -> {
                    button.setStyle("-fx-background-color: transparent; -fx-background-image: none;");
                    if (!button.isPressed()) {
                        ((ImageView) button.getGraphic()).setImage(normalImage);
                    }
                });
                System.out.println("Styled button as " + type);
            } else {
                System.err.println("Missing button images for style: " + type);
                // Apply a basic style as fallback
                button.setStyle("-fx-base: " + (type.equals("blue") ? "#3c7fb1" : "#d14836") + ";");
            }
        } catch (Exception e) {
            System.err.println("Failed to style button: " + e.getMessage());
            e.printStackTrace();
            // Apply a basic style as fallback
            button.setStyle("-fx-base: " + (type.equals("blue") ? "#3c7fb1" : "#d14836") + ";");
        }
    }
}