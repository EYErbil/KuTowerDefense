package com.ku.towerdefense.model.map;

/**
 * Enum representing different types of tiles in the game map.
 */
public enum TileType {
    PATH_CIRCLE_NW,
    PATH_CIRCLE_N,
    PATH_CIRCLE_NE,
    PATH_VERTICAL_N_DE, // DEAD END

    PATH_CIRCLE_E,
    GRASS,
    PATH_CIRCLE_SE,
    PATH_VERTICAL,

    PATH_CIRCLE_S,
    PATH_CIRCLE_SW,
    PATH_CIRCLE_W,
    PATH_VERTICAL_S_DE, // DEAD END

    PATH_HORIZONTAL_W_DE, // DEAD END
    PATH_HORIZONTAL,
    PATH_HORIZONTAL_E_DE, // DEAD END
    TOWER_SLOT,

    TREE_BIG,
    TREE_MEDIUM,
    TREE_SMALL,
    ROCK_SMALL,

    TOWER_ARTILLERY,
    TOWER_MAGE,
    HOUSE,
    ROCK_MEDIUM,

    CASTLE1,
    CASTLE2,
    ARCHER_TOWER,
    WELL,

    CASTLE3,
    CASTLE4,
    TOWER_BARACK,
    LOG_PILE
}

;