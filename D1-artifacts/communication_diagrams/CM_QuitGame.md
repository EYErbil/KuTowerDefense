# Communication Diagram: Quit Game

```mermaid
graph TD
    Player([Player])
    MainMenuScreen[MainMenuScreen]
    GameScreen[GameScreen]
    MapEditorScreen[MapEditorScreen]
    GameController[GameController]
    GameSession[GameSession]
    MapEditorController[MapEditorController]
    SystemResourceManager[SystemResourceManager]
    Application[Application]

    Player -->|1a: clickQuitGame()| MainMenuScreen
    MainMenuScreen -->|2a: requestExit()| Application
    Application -->|3a: releaseResources()| SystemResourceManager
    SystemResourceManager -.->|4a: resourcesReleased| Application
    Application -.->|5a: closeApplication()| Player
    
    Player -->|1b: clickQuitGame()| GameScreen
    GameScreen -->|2b: requestQuit()| GameController
    
    GameController -->|3b: hasUnsavedProgress()| GameSession
    GameSession -.->|4b: true| GameController
    GameController -->|5b: showConfirmationDialog()| GameScreen
    GameScreen -.->|6b: display confirmation dialog| Player
    
    Player -->|7b1: confirmQuit()| GameScreen
    GameScreen -->|8b1: confirmQuit()| GameController
    GameController -->|9b1: endGame()| GameSession
    GameSession -->|10b1: cleanup()| GameSession
    GameController -->|11b1: returnToMainMenu()| MainMenuScreen
    MainMenuScreen -.->|12b1: display main menu| Player
    
    Player -->|7b2: cancelQuit()| GameScreen
    GameScreen -->|8b2: cancelQuit()| GameController
    GameController -.->|9b2: resumeGame()| GameScreen
    
    Player -->|1c: clickQuit()| MapEditorScreen
    MapEditorScreen -->|2c: requestQuit()| MapEditorController
    
    MapEditorController -->|3c: hasUnsavedChanges()| MapEditorController
    MapEditorController -.->|4c: true| MapEditorScreen
    MapEditorScreen -->|5c: showSaveChangesDialog()| Player
    
    Player -->|6c1: selectSave()| MapEditorScreen
    MapEditorScreen -->|7c1: saveMap()| MapEditorController
    MapEditorController -.->|8c1: mapSaved| MapEditorScreen
    MapEditorController -->|9c1: returnToMainMenu()| MainMenuScreen
    MainMenuScreen -.->|10c1: display main menu| Player
    
    Player -->|6c2: selectDiscard()| MapEditorScreen
    MapEditorScreen -->|7c2: discardChanges()| MapEditorController
    MapEditorController -->|8c2: returnToMainMenu()| MainMenuScreen
    MainMenuScreen -.->|9c2: display main menu| Player
    
    Player -->|6c3: selectCancel()| MapEditorScreen
    MapEditorScreen -->|7c3: cancelQuit()| MapEditorController
    
    Player -->|1d: clickCloseWindow()| Application
    Application -->|2d: handleWindowClosing()| Application
    
    Application -->|3d: isGameActive()| GameController
    GameController -.->|4d: true| Application
    Application -->|5d: requestQuit()| GameController
    GameController -->|6d: showConfirmationDialog()| GameScreen
    GameScreen -.->|7d: display confirmation dialog| Player
    
    Player -->|8d1: confirmQuit()| GameScreen
    GameScreen -->|9d1: confirmQuit()| GameController
    GameController -->|10d1: releaseResources()| SystemResourceManager
    SystemResourceManager -.->|11d1: resourcesReleased| Application
    Application -.->|12d1: closeApplication()| Player
    
    Player -->|8d2: cancelQuit()| GameScreen
    GameScreen -->|9d2: cancelQuit()| GameController