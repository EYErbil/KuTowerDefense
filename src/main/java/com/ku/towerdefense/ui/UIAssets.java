package com.ku.towerdefense.ui;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
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

            // Effect sprite sheets
            loadImage("ExplosionEffect", "/Asset_pack/Effects/Explosions.png");
            loadImage("FireEffect", "/Asset_pack/Effects/Fire.png");
            loadImage("GoldSpawnEffect", "/Asset_pack/Effects/G_Spawn.png");

            // Item Images
            loadImage("GoldBag", "/Asset_pack/Items/gold_bag.png");

            // Background Images
            loadImage("WoodBackground", "/Asset_pack/Background/wood.jpg");

            // Tower specific effects/icons
            // loadImage("ThunderEffect", "/Asset_pack/Towers/thunder_icon.png"); // Removed

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
     * Extracts a specific frame from a cached sprite sheet.
     *
     * @param sheetName   The key of the loaded sprite sheet in the cache.
     * @param frameIndex  The 0-based index of the frame to extract.
     * @param frameWidth  The width of a single frame in the sprite sheet.
     * @param frameHeight The height of a single frame in the sprite sheet.
     * @return An Image object of the specified frame, or null if an error occurs.
     */
    public static Image getSpriteFrame(String sheetName, int frameIndex, int frameWidth, int frameHeight) {
        Image spriteSheet = imageCache.get(sheetName);
        if (spriteSheet == null) {
            System.err.println("Sprite sheet not found in cache: " + sheetName);
            return null;
        }

        int sheetWidth = (int) spriteSheet.getWidth();
        // int sheetHeight = (int) spriteSheet.getHeight(); // Assuming all frames are
        // in one row for now

        int framesPerRow = sheetWidth / frameWidth;
        if (frameIndex < 0 || frameIndex >= framesPerRow) { // Basic check, assumes single row of frames
            System.err.println("Frame index " + frameIndex + " is out of bounds for sheet " + sheetName + " with "
                    + framesPerRow + " frames.");
            return null;
        }

        try {
            javafx.scene.image.PixelReader reader = spriteSheet.getPixelReader();
            if (reader == null) {
                System.err.println("PixelReader not available for sprite sheet: " + sheetName);
                return null;
            }
            // Corrected: x coordinate of the frame
            int x = frameIndex * frameWidth;
            int y = 0; // Assuming frames are in a single horizontal row

            javafx.scene.image.WritableImage frameImage = new javafx.scene.image.WritableImage(reader, x, y, frameWidth,
                    frameHeight);
            return frameImage;
        } catch (Exception e) {
            System.err.println(
                    "Error extracting frame " + frameIndex + " from sheet " + sheetName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Apply a styled button appearance.
     * 
     * @param button the button to style
     * @param type   "blue" or "red"
     */
    public static void styleButton(Button button, String type) {
        styleButton(button, type, false); // Calls the new method with isThreeSlides = false
    }

    /**
     * Apply a styled button appearance, supporting standard and 3-slides versions.
     * 
     * @param button        the button to style
     * @param type          "blue" or "red"
     * @param isThreeSlides true to use 3-slides assets, false for standard assets
     */
    public static void styleButton(Button button, String type, boolean isThreeSlides) {
        try {
            String capitalizedType = type.substring(0, 1).toUpperCase() + type.substring(1);
            final Image normalImageFinal;
            Image pressedImageInitial = null;
            final Image hoverImageFinal;
            String baseKey = "Button_" + capitalizedType;

            if (isThreeSlides) {
                normalImageFinal = imageCache.get(baseKey + "_3Slides");
                hoverImageFinal = imageCache.get("Button_Hover_3Slides");
                pressedImageInitial = imageCache.get(baseKey + "_Pressed_3Slides");
                if (pressedImageInitial == null) {
                    pressedImageInitial = hoverImageFinal;
                }
                if (pressedImageInitial == null) {
                    pressedImageInitial = normalImageFinal;
                }
            } else {
                normalImageFinal = imageCache.get(baseKey);
                pressedImageInitial = imageCache.get(baseKey + "_Pressed");
                hoverImageFinal = imageCache.get("Button_Hover");
            }

            final Image finalNormalImage = normalImageFinal;
            final Image finalHoverImage = hoverImageFinal;
            final Image finalPressedImage = (pressedImageInitial != null) ? pressedImageInitial : finalNormalImage;

            if (finalNormalImage != null && finalHoverImage != null) {
                ImageView iv = new ImageView(finalNormalImage);
                iv.setFitWidth(finalNormalImage.getWidth());
                iv.setFitHeight(finalNormalImage.getHeight());
                button.setGraphic(iv);
                button.setContentDisplay(ContentDisplay.CENTER);
                button.setStyle("-fx-background-color: transparent; -fx-background-image: none; -fx-padding: 0;");

                button.setOnMousePressed(e -> ((ImageView) button.getGraphic()).setImage(finalPressedImage));
                button.setOnMouseReleased(e -> ((ImageView) button.getGraphic()).setImage(finalNormalImage));
                button.setOnMouseEntered(e -> {
                    button.setStyle(
                            "-fx-cursor: hand; -fx-background-color: transparent; -fx-background-image: none; -fx-padding: 0;");
                    if (!button.isPressed()) {
                        ((ImageView) button.getGraphic()).setImage(finalHoverImage);
                    }
                });
                button.setOnMouseExited(e -> {
                    button.setStyle("-fx-background-color: transparent; -fx-background-image: none; -fx-padding: 0;");
                    if (!button.isPressed()) {
                        ((ImageView) button.getGraphic()).setImage(finalNormalImage);
                    }
                });
            } else {
                System.err.println("Missing button images for style: " + type + (isThreeSlides ? " (3Slides)" : ""));
                button.setStyle("-fx-base: " + (type.equals("blue") ? "#3c7fb1" : "#d14836") + ";");
            }
        } catch (Exception e) {
            System.err.println("Failed to style button: " + e.getMessage());
            e.printStackTrace();
            button.setStyle("-fx-base: " + (type.equals("blue") ? "#3c7fb1" : "#d14836") + ";");
        }
    }

    public static final double KUTOWERBUTTONS_ICON_WIDTH = 69.25;
    public static final double KUTOWERBUTTONS_ICON_HEIGHT = 66.5;

    /**
     * Creates a button with an icon from the "KUTowerButtons" sprite sheet.
     *
     * @param tooltipText     Text for the button's tooltip.
     * @param iconCol         Column of the icon in the sprite sheet (0-indexed).
     * @param iconRow         Row of the icon in the sprite sheet (0-indexed).
     * @param iconDisplaySize The desired display size (width and height) for the
     *                        icon on the button.
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
                    KUTOWERBUTTONS_ICON_HEIGHT));
            iconView.setFitWidth(iconDisplaySize);
            iconView.setFitHeight(iconDisplaySize);
            iconView.setPreserveRatio(true);
            iconView.setSmooth(true); // Or false if pixel art style is preferred

            button.setGraphic(iconView);
            button.getStyleClass().add("icon-button"); // For CSS styling
            // Basic styling for icon buttons (can be overridden/enhanced in CSS)
            // button.setStyle("-fx-background-color: transparent; -fx-padding: 3px;"); //
            // REMOVED
            // button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #e0e0e0;
            // -fx-padding: 3px; -fx-cursor: hand;")); // REMOVED
            // button.setOnMouseExited(e -> button.setStyle("-fx-background-color:
            // transparent; -fx-padding: 3px;")); // REMOVED

            // CSS (.icon-button) should handle -fx-cursor: hand;
            // Revert to custom cursor on exit if it was changed by something else (though
            // less likely now)
            button.setOnMouseExited(e -> {
                if (button.getScene() != null && button.getScene().getCursor() != UIAssets.getCustomCursor()) {
                    button.getScene().setCursor(UIAssets.getCustomCursor());
                }
            });

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