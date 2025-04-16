# Sequence Diagram: Game Over

```mermaid
sequenceDiagram
    participant GameClock
    participant GameSession
    participant PlayerModel
    participant GameController
    participant GameScreen
    participant GameOverScreen
    participant MainMenuScreen
    actor Player

    GameClock->>GameSession: tick(deltaTime)
    activate GameSession
    GameSession->>GameSession: updateGameState(deltaTime)
    
    alt enemy reaches exit
        GameSession->>PlayerModel: adjustHitPoints(-1)
        activate PlayerModel
        PlayerModel->>PlayerModel: hitPoints -= 1
        PlayerModel-->>GameSession: updatedHitPoints
        deactivate PlayerModel
        
        GameSession->>GameScreen: updateHitPointDisplay(hitPoints)
        activate GameScreen
        GameScreen-->>Player: display updated hit points
        deactivate GameScreen
        
        alt hitPoints <= 0
            GameSession->>GameController: notifyGameOver(DEFEAT)
            activate GameController
            GameController->>GameSession: pauseGame()
            GameSession->>GameSession: setPaused(true)
            
            GameController->>GameOverScreen: showDefeatScreen()
            activate GameOverScreen
            GameOverScreen->>GameOverScreen: setupDefeatBanner()
            GameOverScreen->>GameOverScreen: displayGameStatistics()
            GameOverScreen-->>Player: display defeat screen
        end
    else all waves defeated
        GameSession->>GameController: notifyGameOver(VICTORY)
        activate GameController
        GameController->>GameSession: pauseGame()
        GameSession->>GameSession: setPaused(true)
        
        GameController->>GameOverScreen: showVictoryScreen()
        activate GameOverScreen
        GameOverScreen->>GameOverScreen: setupVictoryBanner()
        GameOverScreen->>GameOverScreen: displayGameStatistics()
        GameOverScreen-->>Player: display victory screen
    end
    deactivate GameSession
    
    Player->>GameOverScreen: clickContinue()
    GameOverScreen->>GameController: continueFromGameOver()
    
    GameController->>GameSession: endGame()
    activate GameSession
    GameSession->>GameSession: cleanup()
    GameSession-->>GameController: cleanupComplete
    deactivate GameSession
    
    GameController->>MainMenuScreen: returnToMainMenu()
    activate MainMenuScreen
    MainMenuScreen->>MainMenuScreen: setupMenu()
    MainMenuScreen-->>Player: display main menu
    deactivate MainMenuScreen
    deactivate GameController
    deactivate GameOverScreen
``` 