```mermaid
%%{init: {'theme': 'neutral'}}%%
flowchart TB
    title[KU Tower Defense - Logical Architecture]
    style title fill:none,stroke:none

    subgraph ku_towerdefense [ku.towerdefense]
        subgraph model [model]
            subgraph model_game [game]
                GameSession[GameSession]
                Player[Player]
                GameOptions[GameOptions]
            end
            
            subgraph model_entities [entities]
                Enemy[Enemy]
                Tower[Tower]
                Projectile[Projectile]
                Wave[Wave]
                Group[Group]
            end
            
            subgraph model_map [map]
                Map[Map]
                Tile[Tile]
                Path[Path]
                TowerSlot[TowerSlot]
            end
        end
        
        subgraph view [view]
            subgraph view_screens [screens]
                MainMenuScreen[MainMenuScreen]
                GameScreen[GameScreen]
                MapEditorScreen[MapEditorScreen]
                OptionsScreen[OptionsScreen]
            end
            
            subgraph view_components [components]
                TowerPanel[TowerPanel]
                GameControlPanel[GameControlPanel]
                MapGrid[MapGrid]
                EnemyRenderer[EnemyRenderer]
                TowerRenderer[TowerRenderer]
            end
            
            subgraph view_animations [animations]
                ProjectileAnimation[ProjectileAnimation]
                EnemyMovementAnimation[EnemyMovementAnimation]
                ExplosionAnimation[ExplosionAnimation]
            end
        end
        
        subgraph controller [controller]
            GameController[GameController]
            MapEditorController[MapEditorController]
            OptionsController[OptionsController]
            InputHandler[InputHandler]
        end
        
        subgraph persistence [persistence]
            MapSerializer[MapSerializer]
            OptionsSerializer[OptionsSerializer]
            FileManager[FileManager]
        end
        
        subgraph util [util]
            MathUtils[MathUtils]
            PathFinder[PathFinder]
            ResourceLoader[ResourceLoader]
            GameClock[GameClock]
        end
    end
    
    %% Dependencies
    model --> util
    view --> model
    controller --> model
    controller --> view
    controller --> persistence
    model --> persistence
