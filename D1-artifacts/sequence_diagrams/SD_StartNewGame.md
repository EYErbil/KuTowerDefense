# Sequence Diagram: Start New Game

```mermaid
sequenceDiagram
    actor Player
    participant MainMenuScreen
    participant GameController
    participant MapSerializer
    participant Map
    participant GameSession
    participant GameOptions
    participant GameScreen

    Player->>MainMenuScreen: clickNewGame()
    activate MainMenuScreen
    MainMenuScreen->>GameController: startNewGame()
    activate GameController
    GameController->>MapSerializer: loadMapList()
    activate MapSerializer
    MapSerializer-->>GameController: availableMaps
    deactivate MapSerializer
    GameController-->>MainMenuScreen: showMapSelection(availableMaps)
    MainMenuScreen-->>Player: display map options
    
    Player->>MainMenuScreen: selectMap(mapId)
    MainMenuScreen->>GameController: loadSelectedMap(mapId)
    GameController->>MapSerializer: loadMap(mapId)
    activate MapSerializer
    MapSerializer->>MapSerializer: deserializeMap()
    MapSerializer-->>GameController: mapData
    deactivate MapSerializer
    
    GameController->>Map: new Map(mapData)
    activate Map
    Map->>Map: initializeGrid()
    Map->>Map: validateMap()
    Map-->>GameController: validatedMap
    deactivate Map
    
    GameController->>GameOptions: getCurrentOptions()
    activate GameOptions
    GameOptions-->>GameController: gameOptions
    deactivate GameOptions
    
    GameController->>GameSession: new GameSession(validatedMap, gameOptions)
    activate GameSession
    GameSession->>GameSession: initializePlayer()
    GameSession->>GameSession: setupWaves()
    GameSession->>GameSession: setGracePeriod(4 seconds)
    GameSession-->>GameController: newGameSession
    deactivate GameSession
    
    GameController->>GameScreen: initialize(newGameSession)
    activate GameScreen
    GameScreen->>GameScreen: setupUI()
    GameScreen->>GameScreen: setupEventHandlers()
    GameScreen-->>Player: display game screen
    deactivate GameScreen
    deactivate MainMenuScreen
    
    GameController->>GameSession: start()
    activate GameSession
    note over GameSession: Game timer begins
    deactivate GameSession
    deactivate GameController
``` 