# Logical Architecture - Package Diagram

```mermaid
%%{init: {'theme': 'neutral'}}%%
classDiagram
    title KU Tower Defense - Logical Architecture

    package "ku.towerdefense" {
        package "model" {
            package "game" {
                class GameSession
                class Player
                class GameOptions
            }
            
            package "entities" {
                class Enemy
                class Tower
                class Projectile
                class Wave
                class Group
            }
            
            package "map" {
                class Map
                class Tile
                class Path
                class TowerSlot
            }
        }
        
        package "view" {
            package "screens" {
                class MainMenuScreen
                class GameScreen
                class MapEditorScreen
                class OptionsScreen
            }
            
            package "components" {
                class TowerPanel
                class GameControlPanel
                class MapGrid
                class EnemyRenderer
                class TowerRenderer
            }
            
            package "animations" {
                class ProjectileAnimation
                class EnemyMovementAnimation
                class ExplosionAnimation
            }
        }
        
        package "controller" {
            class GameController
            class MapEditorController
            class OptionsController
            class InputHandler
        }
        
        package "persistence" {
            class MapSerializer
            class OptionsSerializer
            class FileManager
        }
        
        package "util" {
            class MathUtils
            class PathFinder
            class ResourceLoader
            class GameClock
        }
    }
    
    %% Package Dependencies
    model ..> util : uses
    view ..> model : observes
    controller ..> model : manipulates
    controller ..> view : updates
    controller ..> persistence : uses
    model ..> persistence : uses for loading/saving
``` 