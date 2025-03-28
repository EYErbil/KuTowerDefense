```mermaid
classDiagram
    title KU Tower Defense Domain Model


    class Player {
        gold
        hitPoints
    }

    
    class Map {
        name
        tiles  
        path
    }

    class Tile {
        position
        isWalkable
        isPlaceable
    }

    class Path {
        tiles
        startPoint
        endPoint
    }

   
    class TowerSlot {
        position
        isEmpty
    }

    
    class Tower {
        position   
        rangeMultiplier
        rateOfFireMultiplier
        cost
        level
        damageMultiplier
        
    }

    
    class ArcherTower {
        range
        rateOfFire
    }
    class ArtilleryTower {
        aoeRadiusMultiplier
        range
        rateOfFire
    }
    class MageTower {
        range
        rateOfFire
    }

    Tower <|-- ArcherTower
    Tower <|-- ArtilleryTower
    Tower <|-- MageTower

    
    class Arrow {
        damage
        damageType
        speed
        position
        targetEnemy
        
    }

    class ArtilleryShell {
        damage
        damageType  
        speed
        aoeRadius
        position
        targetEnemy
        
    }

    class Spell {
        damage
        damageType
        speed
        effectDuration
        position
        targetEnemy
        
    }

    ArcherTower "1" -- "*" Arrow : fires
    ArtilleryTower "1" -- "*" ArtilleryShell : fires
    MageTower "1" -- "*" Spell : fires

    
    class Enemy {
        
        
        position
        pathProgress
        
    }

    class Goblin {
        arrowResistance
        spellResistance
        aoeResistance
        hitpoint
        speed
        loot
        
    }

    class Knight {
        arrowResistance
        spellResistance
        aoeResistance
        speed
        hitpoint
        loot
    }

    Enemy <|-- Goblin
    Enemy <|-- Knight

    
    class Wave {
        number
        totalGroups
        currentGroup
        
    }

    class Group {
        enemies
        delay
    }

    class GameSession {
        map
        player
        waves
        towers
        enemies
        gameSpeed
        isPaused
        
    }

    class GameOptions {
        startingGold
        startingHitPoints
        waveSettings
        towerSettings
        enemySettings
        
    }

    
    Player "1" -- "1" GameSession : participates in
    Map "1" -- "*" Tile : contains
    Map "1" -- "1" Path : has
    Map "1" -- "*" TowerSlot : has
    Path "1" -- "*" Tile : consists of
    TowerSlot "0..1" -- "1" Tower : contains
    GameSession "1" -- "1" Map : uses
    GameSession "1" -- "*" Wave : contains
    Wave "1" -- "*" Group : contains
    Group "1" -- "*" Enemy : spawns
    GameSession "1" -- "1" GameOptions : configuredBy
