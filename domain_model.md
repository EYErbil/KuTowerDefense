classDiagram
    title KU Tower Defense Domain Model (Revised)

    %% ==========================
    %% Player
    %% ==========================
    class Player {
        gold
        hitPoints
        adjustGold(amount)
        adjustHitPoints(amount)
    }

    %% ==========================
    %% Map, Path, Tile
    %% ==========================
    class Map {
        name
        tiles  <-- could be a 2D array or list
        path
        validateMap()
    }

    class Tile {
        position
        isWalkable
        isPlaceable
        % optional: tileVariant or tileType for visuals
    }

    class Path {
        tiles
        startPoint
        endPoint
        isConnected()
    }

    %% ==========================
    %% TowerSlot
    %% ==========================
    class TowerSlot {
        position
        tower  <-- null if no tower. (Remove isEmpty)
    }

    %% ==========================
    %% Tower (parent)
    %% ==========================
    class Tower {
        position   <-- or rely on TowerSlot
        range
        rateOfFire
        cost
        level
        damageMultiplier
        fire()
    }

    %% Tower Subclasses
    class ArcherTower {
        % specialized logic for firing arrows
    }
    class ArtilleryTower {
        aoeRadius   <-- if the tower itself has an AOE property
    }
    class MageTower {
        % specialized logic for firing spells
    }

    Tower <|-- ArcherTower
    Tower <|-- ArtilleryTower
    Tower <|-- MageTower

    %% ==========================
    %% Projectiles
    %% ==========================
    class Arrow {
        damage
        damageType  <-- e.g. "pierce"
        speed
        position
        targetEnemy
        move()
        hit()
    }

    class ArtilleryShell {
        damage
        damageType  <-- e.g. "explosion"
        speed
        aoeRadius
        position
        targetEnemy
        move()
        hit()
    }

    class Spell {
        damage
        damageType  <-- e.g. "arcane"
        speed
        effectDuration
        position
        targetEnemy
        move()
        hit()
    }

    %% Each tower class fires its specific projectile
    ArcherTower "1" -- "*" Arrow : fires
    ArtilleryTower "1" -- "*" ArtilleryShell : fires
    MageTower "1" -- "*" Spell : fires

    %% ==========================
    %% Enemies
    %% ==========================
    class Enemy {
        hitPoints
        speed
        position
        pathProgress
        move()
        takeDamage(amount)
        isDefeated()
    }

    class Goblin {
        arrowResistance
        spellResistance
        aoeResistance
        % or any combination of special weaknesses
        % speed override if needed
    }

    class Knight {
        arrowResistance
        spellResistance
        aoeResistance
        % speed override if needed
    }

    Enemy <|-- Goblin
    Enemy <|-- Knight

    %% ==========================
    %% Wave, Group, GameSession, GameOptions
    %% ==========================
    class Wave {
        number
        totalGroups
        currentGroup
        groups
        spawnNextGroup()
    }

    class Group {
        enemies
        delay
        spawnNextEnemy()
    }

    class GameSession {
        map
        player
        waves
        towers
        enemies
        gameSpeed
        isPaused
        startWave()
        updateGameState()
    }

    class GameOptions {
        startingGold
        startingHitPoints
        waveSettings
        towerSettings
        enemySettings
        saveOptions()
        loadOptions()
        resetToDefaults()
    }

    %% ==========================
    %% Relationships
    %% ==========================
    Player "1" -- "1" GameSession : participates in
    Map "1" -- "*" Tile : contains
    Map "1" -- "1" Path : has
    Map "1" -- "*" TowerSlot : has
    Path "1" -- "*" Tile : consists of
    TowerSlot "0..1" -- "1" Tower : contains
    GameSession "1" -- "1" Map : uses
    GameSession "1" -- "*" Tower : manages
    GameSession "1" -- "*" Enemy : manages
    GameSession "1" -- "*" Wave : contains
    Wave "1" -- "*" Group : contains
    Group "1" -- "*" Enemy : spawns
    GameSession "1" -- "1" GameOptions : configuredBy
