# Logical Architecture - Package Diagram

```mermaid
classDiagram
    title KU Tower Defense - Logical Architecture
    
    namespace ku.towerdefense.model.game {
        class GameSession
        class Player
        class GameOptions
    }
            
    namespace ku.towerdefense.model.entities {
        class Enemy
        class Tower
        class Projectile
        class Wave
        class Group
    }
            
    namespace ku.towerdefense.model.map {
        class Map
        class Tile
        class Path
        class TowerSlot
    }
        
    namespace ku.towerdefense.view.screens {
        class MainMenuScreen
        class GameScreen
        class MapEditorScreen
        class OptionsScreen
    }
            
    namespace ku.towerdefense.view.components {
        class TowerPanel
        class GameControlPanel
        class MapGrid
        class EnemyRenderer
        class TowerRenderer
    }
            
    namespace ku.towerdefense.view.animations {
        class ProjectileAnimation
        class EnemyMovementAnimation
        class ExplosionAnimation
    }
        
    namespace ku.towerdefense.controller {
        class GameController
        class MapEditorController
        class OptionsController
        class InputHandler
    }
        
    namespace ku.towerdefense.persistence {
        class MapSerializer
        class OptionsSerializer
        class FileManager
    }
        
    namespace ku.towerdefense.util {
        class MathUtils
        class PathFinder
        class ResourceLoader
        class GameClock
    }
    
    %% Package Dependencies
    ku.towerdefense.model.game --> ku.towerdefense.util : uses
    ku.towerdefense.model.entities --> ku.towerdefense.util : uses
    ku.towerdefense.model.map --> ku.towerdefense.util : uses
    
    ku.towerdefense.view.screens --> ku.towerdefense.model.game : observes
    ku.towerdefense.view.screens --> ku.towerdefense.model.entities : observes
    ku.towerdefense.view.screens --> ku.towerdefense.model.map : observes
    
    ku.towerdefense.controller --> ku.towerdefense.model.game : manipulates
    ku.towerdefense.controller --> ku.towerdefense.model.entities : manipulates
    ku.towerdefense.controller --> ku.towerdefense.model.map : manipulates
    
    ku.towerdefense.controller --> ku.towerdefense.view.screens : updates
    ku.towerdefense.controller --> ku.towerdefense.view.components : updates
    
    ku.towerdefense.controller --> ku.towerdefense.persistence : uses
    ku.towerdefense.model.game --> ku.towerdefense.persistence : uses for loading/saving
    ku.towerdefense.model.map --> ku.towerdefense.persistence : uses for loading/saving
``` 