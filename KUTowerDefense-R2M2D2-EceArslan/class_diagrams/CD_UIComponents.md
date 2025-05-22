# Class Diagram: UI Components

```mermaid
classDiagram
    class Screen {
        <<abstract>>
        #JPanel mainPanel
        +initialize()
        +update()
        +render()
        +handleInput(InputEvent event)
        +getMainPanel() JPanel
    }

    class MainMenuScreen {
        -JButton newGameButton
        -JButton editMapButton
        -JButton optionsButton
        -JButton quitButton
        -JLabel titleLabel
        +initialize()
        +setupButtonListeners()
        +handleInput(InputEvent event)
    }

    class GameScreen {
        -GameSession gameSession
        -MapGrid mapGrid
        -GameControlPanel controlPanel
        -ResourcePanel resourcePanel
        -TowerPanel towerPanel
        -WaveIndicator waveIndicator
        -TowerRangeIndicator rangeIndicator
        +initialize(GameSession session)
        +update(float deltaTime)
        +handleInput(InputEvent event)
        +showTowerOptions(TowerSlot slot)
        +updateResourceDisplay()
        +updateWaveIndicator(int current, int total)
        +showGameOverScreen(boolean victory)
        +showConfirmationDialog(String message) boolean
    }

    class MapEditorScreen {
        -Map map
        -MapGrid mapGrid
        -TileSelector tileSelector
        -EditorToolPanel toolPanel
        -JButton saveButton
        -JButton validateButton
        -JButton clearButton
        -JButton exitButton
        -TileType selectedTileType
        +initialize(Map map)
        +update()
        +handleInput(InputEvent event)
        +selectTile(TileType type)
        +placeTile(int x, int y, TileType type)
        +showSaveDialog() String
        +showValidationErrors(List~String~ errors)
        +showSaveSuccessMessage()
        +showSaveChangesDialog() int
    }

    class OptionsScreen {
        -GameOptions options
        -JTabbedPane categoryTabs
        -JPanel enemyOptionsPanel
        -JPanel towerOptionsPanel
        -JPanel economyOptionsPanel
        -JPanel waveOptionsPanel
        -JPanel gameplayOptionsPanel
        -JButton saveButton
        -JButton resetButton
        -JButton cancelButton
        +initialize(GameOptions options)
        +createEnemyOptionsPanel()
        +createTowerOptionsPanel()
        +createEconomyOptionsPanel()
        +createWaveOptionsPanel()
        +createGameplayOptionsPanel()
        +handleOptionChange(String category, String name, Object value)
        +resetAllFields(GameOptions defaultOptions)
        +showValidationError(String errorMessage)
        +showSaveConfirmation()
    }

    class GameOverScreen {
        -boolean isVictory
        -Map<String, Object> gameStatistics
        -JLabel resultBanner
        -JPanel statisticsPanel
        -JButton continueButton
        +initialize(boolean victory, Map<String, Object> statistics)
        +setupDefeatBanner()
        +setupVictoryBanner()
        +displayGameStatistics()
        +handleInput(InputEvent event)
    }

    class MapGrid {
        -Map map
        -boolean isEditorMode
        -TileRenderer tileRenderer
        -TowerRenderer towerRenderer
        -EnemyRenderer enemyRenderer
        -ProjectileRenderer projectileRenderer
        -int cellSize
        +initialize(Map map, boolean editorMode)
        +render()
        +handleMouseClick(MouseEvent event)
        +convertScreenToGrid(Point screenPosition) Point
        +convertGridToScreen(Point gridPosition) Point
        +highlightCell(int x, int y)
        +setupEditorMode()
        +setupGameMode()
    }

    class TileSelector {
        -List~TileType~ availableTiles
        -TileType selectedTile
        -TileRenderer tileRenderer
        +initialize(List~TileType~ availableTiles)
        +render()
        +handleMouseClick(MouseEvent event)
        +getSelectedTile() TileType
    }

    class EditorToolPanel {
        -JButton validateButton
        -JButton clearButton
        -JButton setStartButton
        -JButton setEndButton
        -JButton pathToolButton
        -JButton towerSlotButton
        +initialize()
        +setupButtonListeners()
        +enableValidationButton(boolean enable)
    }

    class GameControlPanel {
        -JButton pauseButton
        -JButton speedButton
        -JButton quitButton
        -boolean isPaused
        -boolean isAccelerated
        +initialize()
        +setupButtonListeners()
        +updatePauseButton(boolean isPaused)
        +updateSpeedButton(boolean isAccelerated)
    }

    class ResourcePanel {
        -JLabel goldLabel
        -JLabel hitPointsLabel
        -ImageIcon goldIcon
        -ImageIcon heartIcon
        +initialize()
        +updateGold(int amount)
        +updateHitPoints(int amount)
    }

    class TowerPanel {
        -List~TowerType~ availableTowers
        -TowerRenderer towerRenderer
        -TowerSlot selectedSlot
        +initialize(List~TowerType~ availableTowers)
        +showTowerOptions(TowerSlot slot)
        +hideTowerOptions()
        +handleMouseClick(MouseEvent event) TowerType
        +renderTowerOption(TowerType type, int x, int y)
    }

    class WaveIndicator {
        -JLabel waveLabel
        -int currentWave
        -int totalWaves
        +initialize()
        +updateWaveInfo(int current, int total)
        +showWaveStartBanner(int waveNumber)
    }

    class TowerRangeIndicator {
        -Tower selectedTower
        -float range
        -boolean isVisible
        +show(Tower tower)
        +hide()
        +render()
        +isVisible() boolean
        +getSelectedTower() Tower
    }

    class TileRenderer {
        -Map<TileType, Image> tileImages
        +loadTileImages()
        +renderTile(Graphics g, TileType type, int x, int y, int size)
        +getTileImage(TileType type) Image
    }

    class TowerRenderer {
        -Map<TowerType, Image> towerImages
        -Map<TowerType, Animation> constructionAnimations
        +loadTowerImages()
        +renderTower(Graphics g, Tower tower, int x, int y)
        +renderTowerOption(Graphics g, TowerType type, int x, int y)
        +getTowerImage(TowerType type) Image
        +playConstructionAnimation(Tower tower)
    }

    class EnemyRenderer {
        -Map<EnemyType, Map<Direction, Animation>> enemyAnimations
        -Map<EnemyType, Image> enemyIcons
        +loadEnemyAnimations()
        +renderEnemy(Graphics g, Enemy enemy, int x, int y)
        +renderHealthBar(Graphics g, Enemy enemy, int x, int y)
        +getEnemyAnimation(EnemyType type, Direction direction) Animation
    }

    class ProjectileRenderer {
        -Map<ProjectileType, Image> projectileImages
        -Map<ProjectileType, Animation> impactAnimations
        +loadProjectileImages()
        +renderProjectile(Graphics g, Projectile projectile, int x, int y)
        +playImpactAnimation(Projectile projectile, Point position)
    }

    class Animation {
        -List~Image~ frames
        -float frameDuration
        -float elapsedTime
        -int currentFrame
        -boolean isLooping
        -boolean isFinished
        +Animation(List~Image~ frames, float frameDuration, boolean looping)
        +update(float deltaTime)
        +reset()
        +getCurrentFrame() Image
        +isFinished() boolean
        +setLooping(boolean looping)
    }

    class Direction {
        <<enumeration>>
        UP
        DOWN
        LEFT
        RIGHT
        UP_LEFT
        UP_RIGHT
        DOWN_LEFT
        DOWN_RIGHT
    }

    Screen <|-- MainMenuScreen
    Screen <|-- GameScreen
    Screen <|-- MapEditorScreen
    Screen <|-- OptionsScreen
    Screen <|-- GameOverScreen
    
    GameScreen *-- MapGrid
    GameScreen *-- GameControlPanel
    GameScreen *-- ResourcePanel
    GameScreen *-- TowerPanel
    GameScreen *-- WaveIndicator
    GameScreen *-- TowerRangeIndicator

    MapEditorScreen *-- MapGrid
    MapEditorScreen *-- TileSelector
    MapEditorScreen *-- EditorToolPanel

    MapGrid o-- TileRenderer
    MapGrid o-- TowerRenderer
    MapGrid o-- EnemyRenderer
    MapGrid o-- ProjectileRenderer

    TowerRenderer *-- Animation
    EnemyRenderer *-- Animation
    ProjectileRenderer *-- Animation