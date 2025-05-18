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
        -GoldBagRenderer goldBagRenderer
        -StatusEffectRenderer statusEffectRenderer
        -TowerUpgradeMenu towerUpgradeMenu
        +initialize(GameSession session)
        +update(float deltaTime)
        +handleInput(InputEvent event)
        +showTowerOptions(TowerSlot slot)
        +updateResourceDisplay()
        +updateWaveIndicator(int current, int total)
        +showGameOverScreen(boolean victory)
        +showConfirmationDialog(String message) boolean
        +handleGoldBagClick(Point position)
        +showTowerUpgradeMenu(Tower tower)
        +updateStatusEffects()
        +hideTowerUpgradeMenu()
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
        -Map<TowerType, Integer> upgradeCosts
        -JButton upgradeButton
        +initialize(List~TowerType~ availableTowers)
        +showTowerOptions(TowerSlot slot)
        +hideTowerOptions()
        +handleMouseClick(MouseEvent event) TowerType
        +renderTowerOption(TowerType type, int x, int y)
        +showUpgradeOption(Tower tower)
        +updateUpgradeButtonState(int playerGold)
        +handleUpgradeClick(Tower tower)
    }

    class TowerUpgradeMenu {
        -Tower tower
        -JPanel menuPanel
        -JButton upgradeButton
        -JLabel costLabel
        -JLabel statsLabel
        +initialize(Tower tower)
        +updateUpgradeCost(int cost)
        +updateStatsDisplay()
        +setUpgradeEnabled(boolean enabled)
        +show()
        +hide()
        +render(Graphics g)
        +handleMouseClick(MouseEvent event)
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
        -Map<TowerType, Image> level2TowerImages
        +loadTowerImages()
        +renderTower(Graphics g, Tower tower, int x, int y)
        +renderTowerOption(Graphics g, TowerType type, int x, int y)
        +getTowerImage(TowerType type, int level) Image
        +playConstructionAnimation(Tower tower)
    }

    class EnemyRenderer {
        -Map<EnemyType, Map<Direction, Animation>> enemyAnimations
        -Map<EnemyType, Image> enemyIcons
        -Map<StatusEffectType, Image> statusEffectIcons
        +loadEnemyAnimations()
        +renderEnemy(Graphics g, Enemy enemy, int x, int y)
        +renderHealthBar(Graphics g, Enemy enemy, int x, int y)
        +getEnemyAnimation(EnemyType type, Direction direction) Animation
        +renderStatusEffects(Graphics g, Enemy enemy, int x, int y)
        +loadStatusEffectIcons()
    }

    class ProjectileRenderer {
        -Map<ProjectileType, Image> projectileImages
        -Map<ProjectileType, Animation> impactAnimations
        -Map<ProjectileType, Image> level2ProjectileImages
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

    class GoldBagRenderer {
        -Image goldBagImage
        -Animation sparkleAnimation
        +loadResources()
        +renderGoldBag(Graphics g, GoldBag goldBag, int x, int y)
        +renderSparkleEffect(Graphics g, GoldBag goldBag, int x, int y)
        +update(float deltaTime)
    }

    class StatusEffectRenderer {
        -Map<StatusEffectType, Image> effectIcons
        -Map<StatusEffectType, Animation> effectAnimations
        +loadEffectResources()
        +renderEffect(Graphics g, StatusEffectType type, int x, int y)
        +renderEffectAnimation(Graphics g, StatusEffectType type, int x, int y)
        +renderMultipleEffects(Graphics g, List<StatusEffectType> effects, int x, int y)
    }

    class StatusEffectType {
        <<enumeration>>
        SLOW
        COMBAT_SYNERGY
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
    GameScreen *-- GoldBagRenderer
    GameScreen *-- StatusEffectRenderer
    GameScreen *-- TowerUpgradeMenu

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
    GoldBagRenderer *-- Animation
    StatusEffectRenderer *-- Animation