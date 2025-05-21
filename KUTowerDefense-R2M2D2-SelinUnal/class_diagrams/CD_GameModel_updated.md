# Class Diagram: Game Model

```mermaid
classDiagram
    class Entity {
        <<abstract>>
        #Point position
        #float health
        #boolean isActive
        +update(float deltaTime)
        +render(Graphics g)
        +isActive() boolean
        +getPosition() Point
        +setPosition(Point pos)
    }

    class Enemy {
        #float speed
        #float maxHealth
        #int goldValue
        #Path path
        #int currentPathIndex
        #List~Effect~ activeEffects
        +update(float deltaTime)
        +takeDamage(float damage, DamageType type)
        +applyEffect(Effect effect)
        +getGoldValue() int
        +hasReachedEnd() boolean
    }

    class Tower {
        <<abstract>>
        #float range
        #float damage
        #float attackSpeed
        #int cost
        #DamageType damageType
        #List~Enemy~ targetsInRange
        +update(float deltaTime)
        +attack()
        +canAfford(int gold) boolean
        +getCost() int
        +getRange() float
    }

    class Projectile {
        #Point target
        #float speed
        #float damage
        #DamageType damageType
        #List~Effect~ effects
        +update(float deltaTime)
        +hasHitTarget() boolean
        +applyEffects(Enemy target)
    }

    class GameMap {
        -Tile[][] tiles
        -int width
        -int height
        -Path path
        -List~Point~ towerSlots
        +initialize(int width, int height)
        +placeTile(int x, int y, TileType type)
        +getTile(int x, int y) Tile
        +isValidTowerPosition(int x, int y) boolean
        +getPath() Path
    }

    class Tile {
        -TileType type
        -boolean isWalkable
        -boolean isBuildable
        -boolean isPath
        +getType() TileType
        +isWalkable() boolean
        +isBuildable() boolean
        +isPath() boolean
    }

    class Wave {
        -List~Enemy~ enemies
        -float spawnInterval
        -float timeSinceLastSpawn
        -int currentEnemyIndex
        +update(float deltaTime)
        +isComplete() boolean
        +getRemainingEnemies() int
    }

    class WaveConfig {
        -List~EnemyType~ enemyTypes
        -int enemyCount
        -float spawnInterval
        +getEnemyTypes() List~EnemyType~
        +getEnemyCount() int
        +getSpawnInterval() float
    }

    class Effect {
        <<abstract>>
        #float duration
        #float remainingTime
        +update(float deltaTime)
        +apply(Entity target)
        +isExpired() boolean
    }

    class AnimatedEffect {
        -Animation animation
        -Point position
        +update(float deltaTime)
        +render(Graphics g)
    }

    class GameSettings {
        -Map<String, Object> settings
        +loadFromFile(String path)
        +saveToFile(String path)
        +getValue(String key) Object
        +setValue(String key, Object value)
    }

    Entity <|-- Enemy
    Entity <|-- Tower
    Entity <|-- Projectile

    Tower <|-- ArcherTower
    Tower <|-- ArtilleryTower
    Tower <|-- MageTower

    Enemy <|-- Goblin
    Enemy <|-- Knight

    GameMap *-- Tile
    GameMap *-- Path
    GameMap *-- Wave

    Wave *-- WaveConfig
    Wave *-- Enemy

    Enemy o-- Effect
    Projectile o-- Effect
    Effect <|-- AnimatedEffect
``` 