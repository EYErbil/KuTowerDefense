# Class Diagram: Controller Components

```mermaid
classDiagram
    class Application {
        -Screen currentScreen
        -GameController gameController
        -MapEditorController mapEditorController
        -OptionsController optionsController
        -Map<String, Screen> screens
        -ScreenFactory screenFactory
        -ResourceManager resourceManager
        -boolean isRunning
        +Application()
        +initialize()
        +start()
        +stop()
        +run()
        +switchToScreen(ScreenType screenType)
        +handleWindowClosing()
        +isGameActive() boolean
        +isMapEditorActive() boolean
        +requestExit()
        +changeScreen(Screen screen)
        +getResourceManager() ResourceManager
    }

    class GameController {
        -GameSession gameSession
        -GameScreen gameScreen
        -MapSerializer mapSerializer
        -GameClock gameClock
        -InputHandler inputHandler
        -GameOptions gameOptions
        +initialize()
        +startNewGame()
        +loadSelectedMap(String mapId) Map
        +getCurrentOptions() GameOptions
        +createGameSession(Map map, GameOptions options) GameSession
        +getTowerSlotAt(Point position) TowerSlot
        +placeTower(TowerSlot slot, TowerType type) boolean
        +getTowerCost(TowerType type) int
        +getPlayerGold() int
        +pauseGame()
        +resumeGame()
        +toggleGameSpeed()
        +requestQuit()
        +confirmQuit()
        +cancelQuit()
        +checkGameOver()
        +showGameOverScreen(boolean victory)
        +isGameActive() boolean
        +editMap()
        +returnToMainMenu()
    }

    class MapEditorController {
        -Map map
        -MapEditorScreen mapEditorScreen
        -MapSerializer mapSerializer
        -ValidationService validationService
        -boolean hasUnsavedChanges
        +initialize()
        +openMapEditor()
        +newMap(int width, int height)
        +loadMap(String mapId)
        +setSelectedTileType(TileType type)
        +placeTile(Point position, TileType type)
        +updateGridView()
        +saveMap()
        +validateMap(Map map) List~String~
        +saveMap(Map map) boolean
        +requestQuit()
        +discardChanges()
        +cancelQuit()
        +hasUnsavedChanges() boolean
        +returnToMainMenu()
    }

    class OptionsController {
        -GameOptions gameOptions
        -OptionsScreen optionsScreen
        -OptionsSerializer optionsSerializer
        -ValidationService validationService
        +initialize()
        +openOptions()
        +loadOptions() GameOptions
        +updateOption(String category, String name, Object value)
        +validateValue(String category, String name, Object value) boolean
        +saveOptions()
        +resetToDefault()
        +cancelChanges()
        +returnToMainMenu()
        +showValidationError(String errorMessage)
        +showSaveConfirmation()
    }

    class InputHandler {
        -Map<Integer, Runnable> keyBindings
        -Map<MouseEvent.Type, Consumer<MouseEvent>> mouseHandlers
        -boolean isEnabled
        +initialize()
        +handleKeyEvent(KeyEvent event)
        +handleMouseEvent(MouseEvent event)
        +registerKeyBinding(int keyCode, Runnable action)
        +registerMouseHandler(MouseEvent.Type type, Consumer<MouseEvent> handler)
        +setEnabled(boolean enabled)
        +isEnabled() boolean
        +handleTowerSlotClick(Point position)
    }

    class GameClock {
        -float targetFPS
        -float deltaTime
        -boolean isPaused
        -float timeScale
        -List~Updateable~ updateables
        +GameClock(float fps)
        +start()
        +stop()
        +pause()
        +resume()
        +setTimeScale(float scale)
        +getDeltaTime() float
        +tick()
        +registerUpdateable(Updateable updateable)
        +unregisterUpdateable(Updateable updateable)
    }

    class Updateable {
        <<interface>>
        +update(float deltaTime)
    }

    class MapSerializer {
        -String mapDirectory
        -FileManager fileManager
        +loadMapList() List~String~
        +loadMap(String mapId) Map
        +saveMap(Map map) boolean
        +deserializeMap(String mapData) Map
        +serializeMap(Map map) String
    }

    class OptionsSerializer {
        -String optionsFilePath
        -FileManager fileManager
        +loadOptions() GameOptions
        +saveOptions(GameOptions options) boolean
        +deserializeOptions(String optionsData) GameOptions
        +serializeOptions(GameOptions options) String
    }

    class FileManager {
        -String basePath
        +writeFile(String fileName, String content) boolean
        +readFile(String fileName) String
        +fileExists(String fileName) boolean
        +listFiles(String directory) List~String~
        +getBasePath() String
    }

    class ValidationService {
        +validateMap(Map map) List~String~
        +checkStartPoint(Map map) boolean
        +checkEndPoint(Map map) boolean
        +checkPathConnectivity(Map map) boolean
        +checkTowerSlots(Map map) boolean
        +validateOptionValue(String category, String name, Object value) boolean
        +getValidationErrors() List~String~
    }

    class ResourceManager {
        -Map<String, Image> images
        -Map<String, Sound> sounds
        -Map<String, Font> fonts
        -String resourceBasePath
        +loadResources()
        +getImage(String key) Image
        +getSound(String key) Sound
        +getFont(String key) Font
        +loadImage(String path) Image
        +loadSound(String path) Sound
        +loadFont(String path, float size) Font
        +releaseResources()
    }

    class ScreenFactory {
        -ResourceManager resourceManager
        +createScreen(ScreenType type) Screen
        +createMainMenuScreen() MainMenuScreen
        +createGameScreen() GameScreen
        +createMapEditorScreen() MapEditorScreen
        +createOptionsScreen() OptionsScreen
        +createGameOverScreen() GameOverScreen
    }

    class ScreenType {
        <<enumeration>>
        MAIN_MENU
        GAME
        MAP_EDITOR
        OPTIONS
        GAME_OVER
    }

    class WaveFactory {
        -GameOptions gameOptions
        +createWaves(Map<String, Object> waveSettings) List~Wave~
        +determineWaveCount() int
        +createWaveConfiguration(int waveNum) WaveConfiguration
        +determineGroupCount(int waveNum) int
        +determineGroupComposition(int waveNum, int groupNum) GroupConfiguration
    }

    class WaveConfiguration {
        -int waveNumber
        -int groupCount
        -float delayBetweenGroups
        -Map<Integer, GroupConfiguration> groupConfigurations
        +WaveConfiguration(int waveNumber)
        +setGroupCount(int count)
        +setDelayBetweenGroups(float delay)
        +addGroupConfiguration(int groupNumber, GroupConfiguration config)
        +getWaveNumber() int
        +getGroupCount() int
        +getDelayBetweenGroups() float
        +getGroupConfigurations() Map<Integer, GroupConfiguration>
    }

    class GroupConfiguration {
        -int groupNumber
        -List~EnemyType~ enemyComposition
        -float delayBetweenEnemies
        +GroupConfiguration(int groupNumber)
        +setEnemyComposition(List~EnemyType~ composition)
        +setDelayBetweenEnemies(float delay)
        +getGroupNumber() int
        +getEnemyComposition() List~EnemyType~
        +getDelayBetweenEnemies() float
    }

    Application o-- GameController
    Application o-- MapEditorController
    Application o-- OptionsController
    Application o-- ResourceManager
    Application o-- ScreenFactory

    GameController o-- GameSession
    GameController o-- GameScreen
    GameController o-- MapSerializer
    GameController o-- GameClock
    GameController o-- InputHandler
    GameController o-- GameOptions

    MapEditorController o-- Map
    MapEditorController o-- MapEditorScreen
    MapEditorController o-- MapSerializer
    MapEditorController o-- ValidationService

    OptionsController o-- GameOptions
    OptionsController o-- OptionsScreen
    OptionsController o-- OptionsSerializer
    OptionsController o-- ValidationService

    GameSession o-- WaveFactory
    WaveFactory ..> WaveConfiguration
    WaveFactory ..> GroupConfiguration

    GameClock o-- Updateable

    MapSerializer o-- FileManager
    OptionsSerializer o-- FileManager