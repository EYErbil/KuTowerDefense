package com.ku.towerdefense.model.entity;

import java.io.Serializable;

/**
 * Represents different types of damage that can be inflicted by towers.
 */
public enum DamageType implements Serializable {
    /**
     * Physical damage from arrows, usually dealt by archer towers.
     * More effective against goblins, less effective against knights.
     */
    ARROW,
    
    /**
     * Magical damage from spells, usually dealt by mage towers.
     * More effective against knights, less effective against goblins.
     */
    MAGIC,
    
    /**
     * Explosive damage from artillery, usually dealt by artillery towers.
     * Generally has an area of effect and deals the same damage to all enemy types.
     */
    EXPLOSIVE
} 