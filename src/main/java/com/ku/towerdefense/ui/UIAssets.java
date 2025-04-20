package com.ku.towerdefense.ui;

import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
        String basePath = System.getProperty("user.dir") + File.separator + "Asset_pack" + File.separator + "UI" + File.separator;
        
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
                "Button_Disable_3Slides.png"
            };
            
            for (String file : buttonFiles) {
                String key = file.replace(".png", "");
                loadImage(key, basePath + file);
            }
            
            // Load cursor image
            loadImage("Cursor", basePath + "01.png");
            
            // Load tower buttons
            loadImage("TowerButtons", basePath + "kutowerbuttons4.png");
            
            // Load UI elements
            loadImage("GameUI", basePath + "Coin_Health_Wave.png");
            
            // Load ribbon images for UI panels
            loadImage("RibbonBlue", basePath + "Ribbon_Blue_3Slides.png");
            loadImage("RibbonRed", basePath + "Ribbon_Red_3Slides.png");
            loadImage("RibbonYellow", basePath + "Ribbon_Yellow_3Slides.png");
            
            System.out.println("UI assets loaded successfully - " + imageCache.size() + " images");
        } catch (Exception e) {
            System.err.println("Failed to load UI assets: " + e.getMessage());
            e.printStackTrace();
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
            File imageFile = new File(path);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                if (!image.isError()) {
                    imageCache.put(name, image);
                    System.out.println("Loaded UI asset: " + name);
                } else {
                    System.err.println("Error loading image: " + name);
                }
            } else {
                System.err.println("Image file not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Failed to load image " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a custom cursor.
     */
    private static void createCustomCursor() {
        try {
            Image cursorImage = imageCache.get("Cursor");
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
     * @param type "blue" or "red"
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