package com.ku.towerdefense.model.map;

/**
 * Enum representing different types of tiles in the game map.
 */
public enum TileType {
    /**
     * Basic grass tile where towers can be placed.
     */
    GRASS,
    
    /**
     * Path tile where enemies walk. Towers cannot be placed on paths.
     */
    PATH,
    
    /**
     * Starting point for enemies. Only one per map.
     */
    START_POINT,
    
    /**
     * Ending point where enemies try to reach. Only one per map.
     */
    END_POINT,
    
    /**
     * Decoration tile that doesn't affect gameplay (rocks, trees, etc).
     */
    DECORATION,
    
    /**
     * Tower slot where towers can be built.
     */
    TOWER_SLOT,
    
    /**
     * Obstacle that blocks enemy path and tower placement.
     */
    OBSTACLE
} 