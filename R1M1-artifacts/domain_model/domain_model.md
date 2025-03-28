```mermaid
classDiagram
    title KU Tower Defense Domain Model


    class Player {
        currentGold
        currentHitPoints
        
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
    }

    
    class Tower {
        position   
        rangeMultiplier
        rateOfFireMultiplier
        level
        damageMultiplier
        updateCostModifier
        
    }

    
    class ArcherTower {
        range
        rateOfFire
        updateCost
        buildCost
    }
    class ArtilleryTower {
        aoeRadiusMultiplier
        range
        rateOfFire
        updateCost
        buildCost
    }
    class MageTower {
        range
        rateOfFire
        updateCost
        buildCost
    }

    Tower -- ArcherTower : has type
    Tower -- ArtilleryTower : has type
    Tower -- MageTower : has type

    
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

    ArcherTower  --  Arrow : spawns
    ArtilleryTower  -- ArtilleryShell : spawns
    MageTower  --  Spell : spawns

    
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
        
        
    }

    class Knight {
        arrowResistance
        spellResistance
        aoeResistance
        speed
        hitpoint
        
    }

    Enemy -- Goblin : has type
    Enemy -- Knight : has type

    
    class Wave {
        delayBetweenGroups
        waveNumber
        totalGroups
        
        
        
    }

    class Group {
        numberOfGoblins
        numberOfKnights
        delayBetweenEnemies
    }

    class GameSession {
        delayBetweenWaves
        map
        player
        waves
        currentWaveIndex
        aliveEnemies
        gameSpeed
        isPaused
        
        
    }

    class GameOptions {
        startingGold
        startingHitPoints
        waveSettings
        towerSettings
        enemySettings
        lootObtainedFromGoblin
        lootObtainedFromKnight
        
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
