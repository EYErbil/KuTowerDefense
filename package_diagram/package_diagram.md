@startuml KUDefensePackageDiagram

' Layer 1 - UI
package "UI" {
  package "view.screens" {
    class GameScreen
    class MapEditorScreen
    class OptionScreen
    class MainMenuScreen
  }

  package "view.components" {
    class TowerPanel
    class MapGrid
    class GameControlPanel
  }

  package "view.animations" {
    class ProjectileAnimation
    class ExplosionAnimation
  }
}

' Layer 2 - Domain
package "Domain" {
  package "model.map" {
    class Map
    class Tile
    class Path
    class TowerSlot
  }

  package "model.entities" {
    class Enemy
    class Goblin
    class Knight
    class Wave
    class Group
    class Projectile
    class Tower
    class ArcherTower
    class ArtilleryTower
    class MageTower
    class TowerType
  }

  package "model.game" {
    class GameSession
    class Player
    class GameOptions
  }

  package "controller" {
    class GameController
    class MapEditorController
    class OptionsController
  }
}

' Layer 3 - Technical Services
package "Technical Services" {
  package "persistence" {
    class MapSerializer
    class OptionsSerializer
    class FileManager
  }

  package "util" {
    class PathFinder
    class MathUtils
    class GameClock
    class ResourceLoader
  }
}

' Dependencies
"UI" ..> "Domain" : observes
"Domain" ..> "Technical Services" : uses

@enduml
