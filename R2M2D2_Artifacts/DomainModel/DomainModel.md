### KU Tower Defense Domain Model (Phase II)
```mermaid
classDiagram
    %% --- Player and GameSession ---
    class Player {
        currentGold
        currentHitPoints
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
        droppedItems
    }

    Player "1" -- "1" GameSession : participates in

    %% --- Map, Path, Tile, TowerSlot ---
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

    Map "1" -- "*" Tile : contains
    Map "1" -- "1" Path : has
    Map "1" -- "*" TowerSlot : has
    Path "1" -- "*" Tile : consists of
    TowerSlot "0..1" -- "1" Tower : contains
    GameSession "1" -- "1" Map : uses

    %% --- Towers and Projectiles ---
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
        slowEffect
        spellColor
    }

    ArcherTower  --  Arrow : spawns
    ArtilleryTower  -- ArtilleryShell : spawns
    MageTower  --  Spell : spawns

    %% --- Enemies and Effects ---
    class Enemy {
        position
        pathProgress
        statusEffects
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
        synergyActive
    }

    Enemy -- Goblin : has type
    Enemy -- Knight : has type

    %% --- Status Effects ---
    class StatusEffect {
        type
        duration
        icon
    }
    Enemy "1" -- "*" StatusEffect : has

    %% --- Dropped Items ---
    class DroppedItem {
        position
        goldAmount
        timeToLive
        isCollected
    }
    GameSession "1" -- "*" DroppedItem : manages

    %% --- Waves, Groups, Options ---
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
    class GameOptions {
        startingGold
        startingHitPoints
        waveSettings
        towerSettings
        enemySettings
        lootObtainedFromGoblin
        lootObtainedFromKnight
    }

    GameSession "1" -- "*" Wave : contains
    Wave "1" -- "*" Group : contains
    Group "1" -- "*" Enemy : spawns
    GameSession "1" -- "1" GameOptions : configuredBy
