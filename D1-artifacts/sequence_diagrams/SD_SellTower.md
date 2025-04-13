# Sequence Diagram: Sell Tower

```mermaid
sequenceDiagram
    actor Player
    participant GameScreen
    participant InputHandler
    participant GameController
    participant GameSession
    participant Tower
    participant TowerSlot
    participant PlayerModel as Player Model
    participant TowerEffectManager
    participant AudioManager

    Player->>GameScreen: 1: clickOnTower(position)
    GameScreen->>InputHandler: 2: handleTowerClick(position)
    InputHandler->>GameController: 3: selectTower(position)
    
    GameController->>GameSession: 4: findTowerAt(position)
    GameSession->>GameSession: 5: getTowerAt(position)
    GameSession-->>GameController: 6: selectedTower
    
    GameController->>GameScreen: 7: showTowerMenu(selectedTower)
    GameScreen-->>Player: 8: display tower options
    
    Player->>GameScreen: 9: clickSellTower()
    GameScreen->>GameController: 10: sellSelectedTower()
    
    GameController->>Tower: 11: getSellValue(selectedTower)
    Tower-->>GameController: 12: sellValue
    
    GameController->>GameSession: 13: sellTower(selectedTower)
    GameSession->>TowerSlot: 14: removeTower(selectedTower)
    TowerSlot->>TowerSlot: 15: isEmpty = true
    
    GameSession->>PlayerModel: 16: addGold(sellValue)
    PlayerModel->>PlayerModel: 17: gold += sellValue
    
    GameController->>TowerEffectManager: 18: removeEffects(selectedTower)
    TowerEffectManager->>TowerEffectManager: 19: cleanupEffects()
    
    GameController->>AudioManager: 20: playSellSound()
    AudioManager-->>Player: 21: play sound effect
    
    GameController->>GameScreen: 22: hideTowerMenu()
    GameScreen-->>Player: 23: hide tower menu
    
    GameController->>GameScreen: 24: updateResourceDisplay()
    GameScreen-->>Player: 25: updated gold amount
    
    GameController->>GameScreen: 26: updateMapView()
    GameScreen-->>Player: 27: tower removed visualization
``` 