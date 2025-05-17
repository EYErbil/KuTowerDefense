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
        String basePathUI = "/Asset_pack/UI/";
        String basePathEffects = "/Asset_pack/Effects/";

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
                loadImage(key, basePathUI + file);
            }

            // Load other UI images
            loadImage("GameUI", basePathUI + "Coin_Health_Wave.png");
            loadImage("Ribbon_Blue", basePathUI + "Ribbon_Blue_3Slides.png");
            loadImage("Ribbon_Red", basePathUI + "Ribbon_Red_3Slides.png");
            loadImage("Ribbon_Yellow", basePathUI + "Ribbon_Yellow_3Slides.png");
            loadImage("KUTowerButtons", basePathUI + "kutowerbuttons4.png");
            loadImage("01", basePathUI + "01.png");

            // Load effect animations
            loadImage("ExplosionEffect", basePathEffects + "Explosions.png");
            loadImage("FireEffect", basePathEffects + "Fire.png");

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

    public static final double KUTOWERBUTTONS_ICON_WIDTH = 69.25;
    public static final double KUTOWERBUTTONS_ICON_HEIGHT = 66.5;

    /**
     * Creates a button with an icon from the "KUTowerButtons" sprite sheet.
     *
     * @param tooltipText Text for the button's tooltip.
     * @param iconCol Column of the icon in the sprite sheet (0-indexed).
     * @param iconRow Row of the icon in the sprite sheet (0-indexed).
     * @param iconDisplaySize The desired display size (width and height) for the icon on the button.
     * @return A new Button configured with the specified icon and tooltip.
     */
    public static Button createIconButton(String tooltipText, int iconCol, int iconRow, double iconDisplaySize) {
        Button button = new Button();
        Image spriteSheet = getImage("KUTowerButtons");

        if (spriteSheet != null) {
            ImageView iconView = new ImageView(spriteSheet);
            iconView.setViewport(new javafx.geometry.Rectangle2D(
                iconCol * KUTOWERBUTTONS_ICON_WIDTH,
                iconRow * KUTOWERBUTTONS_ICON_HEIGHT,
                KUTOWERBUTTONS_ICON_WIDTH,
                KUTOWERBUTTONS_ICON_HEIGHT
            ));
            iconView.setFitWidth(iconDisplaySize);
            iconView.setFitHeight(iconDisplaySize);
            iconView.setPreserveRatio(true);
            iconView.setSmooth(true); // Or false if pixel art style is preferred

            button.setGraphic(iconView);
            button.getStyleClass().add("icon-button"); // For CSS styling
             // Basic styling for icon buttons (can be overridden/enhanced in CSS)
            button.setStyle("-fx-background-color: transparent; -fx-padding: 3px;");
            button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 3px; -fx-cursor: hand;")); // Light grey on hover
            button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-padding: 3px;"));


        } else {
            // Fallback if the sprite sheet isn't loaded
            button.setText("?"); // Placeholder for missing icon
            System.err.println("KUTowerButtons spritesheet not found for icon button.");
        }

        if (tooltipText != null && !tooltipText.isEmpty()) {
            button.setTooltip(new javafx.scene.control.Tooltip(tooltipText));
        }

        return button;
    }
}