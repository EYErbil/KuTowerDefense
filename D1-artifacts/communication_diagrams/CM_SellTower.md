# Communication Diagram: Sell Tower

```mermaid
graph TD
    Player([Player])
    GameScreen[GameScreen]
    InputHandler[InputHandler]
    GameController[GameController]
    GameSession[GameSession]
    TowerSlot[TowerSlot]
    Tower[Tower]
    PlayerModel[Player]

    Player -->|1: rightClickOnTower(position)| GameScreen
    GameScreen -->|2: handleTowerRightClick(position)| InputHandler
    InputHandler -->|3: sellTowerAt(position)| GameController
    
    GameController -->|4: getTowerAt(position)| GameSession
    GameSession -->|5: findTowerAtPosition(position)| GameSession
    GameSession -->|6: findTowerSlotAt(position)| GameSession
    GameSession -.->|7: towerSlot| GameController
    
    GameController -->|8: showSellConfirmationDialog()| GameScreen
    GameScreen -.->|9: display confirmation dialog| Player
    
    Player -->|10: confirmSellTower()| GameScreen
    GameScreen -->|11: confirmSellTower()| GameController
    
    GameController -->|12: sellTower(towerSlot)| GameSession
    GameSession -->|13: getTower()| TowerSlot
    TowerSlot -.->|14: tower| GameSession
    
    GameSession -->|15: calculateRefundAmount(tower)| GameSession
    GameSession -->|16: removeTower()| TowerSlot
    TowerSlot -->|17: tower = null| TowerSlot
    TowerSlot -->|18: isEmpty = true| TowerSlot
    
    GameSession -->|19: adjustGold(refundAmount)| PlayerModel
    PlayerModel -->|20: gold += refundAmount| PlayerModel
    
    GameSession -.->|21: sellComplete| GameController
    GameController -->|22: updateGameView()| GameScreen
    GameScreen -->|23: updateResourceDisplay()| GameScreen
    GameScreen -.->|24: display updated gold and removed tower| Player
``` 