@startuml KUDefenseTower_PackageDiagram

skinparam packageStyle rectangle
title KUDefenseTower - Logical Architecture (Package Diagram)

' ---------------- UI Layer ----------------
package "UI" {
  package "Swing" {
    [MainMenuScreen]
    [OptionsScreen]
    [MapEditorScreen]
    [GameScreen]
    [GameOverScreen]
    [MapGrid]
    [TileSelector]
    [EditorToolPanel]
    [TowerPanel]
    [WaveIndicator]
    [TowerRangeIndicator]
    [GameControlPanel]
    [ResourcePanel]
  }

  package "Renderer" {
    [TileRenderer]
    [TowerRenderer]
    [EnemyRenderer]
    [ProjectileRenderer]
    [Animation]
    [Direction]
  }

  [ScreenFactory]
  [ScreenType]
}

' ---------------- Domain Layer ----------------
package "Domain" {
  package "model.map" {
    [Map]
    [Tile]
    [Path]
    [TowerSlot]
    [TileType]
  }

  package "model.entities" {
    [Enemy]
    [Goblin]
    [Knight]
    [EnemyType]
    [Wave]
    [Group]
  }

  package "model.projectiles" {
    [Projectile]
    [Arrow]
    [ArtilleryShell]
    [Spell]
    [ProjectileType]
  }

  package "model.towers" {
    [Tower]
    [ArcherTower]
    [MageTower]
    [ArtilleryTower]
    [TowerType]
  }

  package "model.game" {
    [GameSession]
    [Player]
    [GameOptions]
  }

  package "controller" {
    [Application]
    [GameController]
    [MapEditorController]
    [OptionsController]
    [InputHandler]
  }
}

' ---------------- Technical Services Layer ----------------
package "Technical Service" {
  [MapSerializer]
  [OptionsSerializer]
  [FileManager]
  [PathFinder]
  [GameClock]
  [MathUtils]
  [ResourceLoader]
  [ValidationService]
  [WaveFactory]
  [WaveConfiguration]
  [GroupConfiguration]
}

' ---------------- Dependencies ----------------
"UI" ..> "Domain"
"Domain" ..> "Technical Service"

@enduml