# Communication Diagram: Enemy Movement

```mermaid
graph TD
    GameClock[GameClock]
    GameSession[GameSession]
    GameController[GameController]
    Wave[Wave]
    Group[Group]
    Enemy[Enemy]
    Path[Path]
    EnemyMovementAnimation[EnemyMovementAnimation]
    GameScreen[GameScreen]
    Player[Player]

    GameClock -->|1: tick(deltaTime)| GameSession
    GameSession -->|2: updateGameState(deltaTime)| GameSession
    
    GameSession -->|3: update(deltaTime)| Wave
    Wave -->|4: updateSpawnTimer(deltaTime)| Wave
    
    Wave -->|5: spawnNextEnemy()| Group
    Group -->|6: new Enemy(type, startPoint)| Enemy
    Enemy -->|7: initializeProperties()| Enemy
    Enemy -.->|8: newEnemy| Group
    Group -->|9: addEnemy(newEnemy)| GameSession
    GameSession -->|10: createAnimation(newEnemy)| EnemyMovementAnimation
    EnemyMovementAnimation -.->|11: visualize enemy spawn| GameScreen
    
    GameSession -->|12: update(deltaTime)| Enemy
    Enemy -->|13: updatePosition(deltaTime)| Enemy
    
    Enemy -->|14: position += direction * speed * deltaTime| Enemy
    Enemy -->|15: pathProgress += speed * deltaTime| Enemy
    
    Enemy -->|16: getNextWaypoint(currentWaypoint)| Path
    Path -.->|17: nextWaypoint| Enemy
    Enemy -->|18: updateDirection(nextWaypoint)| Enemy
    
    Enemy -->|19: notifyReachedEnd()| GameSession
    GameSession -->|20: removeEnemy(enemy)| GameSession
    GameSession -->|21: adjustHitPoints(-1)| Player
    
    GameSession -->|22: checkGameOver()| GameController
    
    GameController -->|23: endGame()| GameSession
    GameController -->|24: showGameOverScreen()| GameScreen
    GameScreen -.->|25: display game over| Player
    
    Enemy -->|26: updateAnimation(position)| EnemyMovementAnimation
    EnemyMovementAnimation -->|27: selectAnimationFrame()| EnemyMovementAnimation
    EnemyMovementAnimation -.->|28: update enemy visualization| GameScreen
    
    GameSession -->|29: updateGameState()| GameScreen
    GameScreen -->|30: redrawEnemies()| GameScreen
    GameScreen -->|31: updateHUDValues()| GameScreen
    GameScreen -.->|32: updated game view| Player
``` 