# Communication Diagram: Game Over

```mermaid
graph TD
    GameClock[GameClock]
    GameSession[GameSession]
    PlayerModel[Player]
    GameController[GameController]
    GameScreen[GameScreen]
    GameOverScreen[GameOverScreen]
    MainMenuScreen[MainMenuScreen]
    Player([Player])

    GameClock -->|1: tick(deltaTime)| GameSession
    GameSession -->|2: updateGameState(deltaTime)| GameSession
    
    GameSession -->|3a: adjustHitPoints(-1)| PlayerModel
    PlayerModel -->|4a: hitPoints -= 1| PlayerModel
    PlayerModel -.->|5a: updatedHitPoints| GameSession
    
    GameSession -->|6a: updateHitPointDisplay(hitPoints)| GameScreen
    GameScreen -.->|7a: display updated hit points| Player
    
    GameSession -->|8a: notifyGameOver(DEFEAT)| GameController
    GameController -->|9a: pauseGame()| GameSession
    GameSession -->|10a: setPaused(true)| GameSession
    
    GameController -->|11a: showDefeatScreen()| GameOverScreen
    GameOverScreen -->|12a: setupDefeatBanner()| GameOverScreen
    GameOverScreen -->|13a: displayGameStatistics()| GameOverScreen
    GameOverScreen -.->|14a: display defeat screen| Player
    
    GameSession -->|3b: notifyGameOver(VICTORY)| GameController
    GameController -->|4b: pauseGame()| GameSession
    GameSession -->|5b: setPaused(true)| GameSession
    
    GameController -->|6b: showVictoryScreen()| GameOverScreen
    GameOverScreen -->|7b: setupVictoryBanner()| GameOverScreen
    GameOverScreen -->|8b: displayGameStatistics()| GameOverScreen
    GameOverScreen -.->|9b: display victory screen| Player
    
    Player -->|15: clickContinue()| GameOverScreen
    GameOverScreen -->|16: continueFromGameOver()| GameController
    
    GameController -->|17: endGame()| GameSession
    GameSession -->|18: cleanup()| GameSession
    GameSession -.->|19: cleanupComplete| GameController
    
    GameController -->|20: returnToMainMenu()| MainMenuScreen
    MainMenuScreen -->|21: setupMenu()| MainMenuScreen
    MainMenuScreen -.->|22: display main menu| Player
``` 