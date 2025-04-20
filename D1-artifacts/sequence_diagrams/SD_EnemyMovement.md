# Sequence Diagram: Enemy Movement

```mermaid
sequenceDiagram
    participant GameClock
    participant GameSession
    participant GameController
    participant Wave
    participant Group
    participant Enemy
    participant Path
    participant EnemyMovementAnimation
    participant GameScreen
    participant Player

    GameClock->>GameSession: tick(deltaTime)
    activate GameSession
    GameSession->>GameSession: updateGameState(deltaTime)
    
    alt wave in progress
        GameSession->>Wave: update(deltaTime)
        activate Wave
        Wave->>Wave: updateSpawnTimer(deltaTime)
        
        alt spawn timer expired
            Wave->>Group: spawnNextEnemy()
            activate Group
            Group->>Enemy: new Enemy(type, startPoint)
            activate Enemy
            Enemy->>Enemy: initializeProperties()
            Enemy-->>Group: newEnemy
            deactivate Enemy
            Group->>GameSession: addEnemy(newEnemy)
            GameSession->>EnemyMovementAnimation: createAnimation(newEnemy)
            activate EnemyMovementAnimation
            EnemyMovementAnimation-->>GameScreen: visualize enemy spawn
            deactivate EnemyMovementAnimation
            deactivate Group
        end
        deactivate Wave
    end
    
    loop for each active Enemy
        GameSession->>Enemy: update(deltaTime)
        activate Enemy
        Enemy->>Enemy: updatePosition(deltaTime)
        
        Enemy->>Enemy: position += direction * speed * deltaTime
        Enemy->>Enemy: pathProgress += speed * deltaTime
        
        alt reached path waypoint
            Enemy->>Path: getNextWaypoint(currentWaypoint)
            activate Path
            Path-->>Enemy: nextWaypoint
            deactivate Path
            Enemy->>Enemy: updateDirection(nextWaypoint)
        end
        
        alt reached path end
            Enemy->>GameSession: notifyReachedEnd()
            GameSession->>GameSession: removeEnemy(enemy)
            GameSession->>Player: adjustHitPoints(-1)
            
            GameSession->>GameController: checkGameOver()
            activate GameController
            
            alt player hitPoints <= 0
                GameController->>GameSession: endGame()
                GameController->>GameScreen: showGameOverScreen()
                activate GameScreen
                GameScreen-->>Player: display game over
                deactivate GameScreen
            end
            deactivate GameController
        end
        
        Enemy->>EnemyMovementAnimation: updateAnimation(position)
        activate EnemyMovementAnimation
        EnemyMovementAnimation->>EnemyMovementAnimation: selectAnimationFrame()
        EnemyMovementAnimation-->>GameScreen: update enemy visualization
        deactivate EnemyMovementAnimation
        deactivate Enemy
    end
    
    GameSession->>GameScreen: updateGameState()
    activate GameScreen
    GameScreen->>GameScreen: redrawEnemies()
    GameScreen->>GameScreen: updateHUDValues()
    GameScreen-->>Player: updated game view
    deactivate GameScreen
    deactivate GameSession
``` 