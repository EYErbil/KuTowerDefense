# Communication Diagram: Place Tower

```mermaid
graph TD
    Player([Player])
    GameScreen[GameScreen]
    TowerPanel[TowerPanel]
    InputHandler[InputHandler]
    GameController[GameController]
    GameSession[GameSession]
    PlayerModel[Player]
    TowerSlot[TowerSlot]
    Tower[Tower]
    TowerRenderer[TowerRenderer]

    Player -->|1: clickOnTowerSlot(position)| GameScreen
    GameScreen -->|2: handleTowerSlotClick(position)| InputHandler
    InputHandler -->|3: selectTowerSlot(position)| GameController
    GameController -->|4: getTowerSlotAt(position)| GameSession
    GameSession -->|5: findTowerSlot(position)| GameSession
    GameSession -.->|6: selectedTowerSlot| GameController
    
    GameController -->|7: showTowerOptions(selectedTowerSlot)| TowerPanel
    TowerPanel -.->|8: display tower type options| Player
    
    Player -->|9: selectTowerType(towerType)| TowerPanel
    TowerPanel -->|10: placeTower(selectedTowerSlot, towerType)| GameController
    
    GameController -->|11: getTowerCost(towerType)| GameSession
    GameSession -.->|12: towerCost| GameController
    
    GameController -->|13: getPlayerGold()| GameSession
    GameSession -->|14: getGold()| PlayerModel
    PlayerModel -.->|15: currentGold| GameSession
    GameSession -.->|16: currentGold| GameController
    
    GameController -->|17a: createTower(selectedTowerSlot, towerType)| GameSession
    GameSession -->|18: isEmpty()| TowerSlot
    TowerSlot -.->|19: true| GameSession
    
    GameSession -->|20: new Tower(towerType, position)| Tower
    Tower -->|21: initializeProperties()| Tower
    Tower -.->|22: newTower| GameSession
    
    GameSession -->|23: placeTower(newTower)| TowerSlot
    TowerSlot -->|24: isEmpty = false| TowerSlot
    
    GameSession -->|25: adjustGold(-towerCost)| PlayerModel
    PlayerModel -->|26: gold -= towerCost| PlayerModel
    
    GameSession -.->|27: placementSuccessful| GameController
    GameController -->|28: renderNewTower(newTower)| TowerRenderer
    TowerRenderer -.->|29: update display| GameScreen
    GameScreen -.->|30: tower placed visualization| Player
    
    GameController -->|31: updateResourceDisplay()| GameScreen
    GameScreen -.->|32: updated gold amount| Player
    
    GameController -->|17b: showInsufficientFundsMessage()| GameScreen
    GameScreen -.->|33: display error message| Player
``` 