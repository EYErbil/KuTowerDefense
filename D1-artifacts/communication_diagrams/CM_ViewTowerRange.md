# Communication Diagram: View Tower Range

```mermaid
graph TD
    Player([Player])
    GameScreen[GameScreen]
    InputHandler[InputHandler]
    GameController[GameController]
    GameSession[GameSession]
    Tower[Tower]
    TowerRangeIndicator[TowerRangeIndicator]

    Player -->|1: mouseOverTower(position)| GameScreen
    GameScreen -->|2: handleMouseOver(position)| InputHandler
    InputHandler -->|3: showTowerRangeAt(position)| GameController
    
    GameController -->|4: getTowerAt(position)| GameSession
    GameSession -->|5: findTowerAtPosition(position)| GameSession
    GameSession -.->|6: tower| GameController
    
    GameController -->|7: getRange()| Tower
    Tower -.->|8: range| GameController
    
    GameController -->|9: showTowerRange(tower, range)| GameScreen
    GameScreen -->|10: show(tower)| TowerRangeIndicator
    TowerRangeIndicator -->|11: setRange(range)| TowerRangeIndicator
    TowerRangeIndicator -->|12: setVisible(true)| TowerRangeIndicator
    
    GameScreen -->|13: render()| TowerRangeIndicator
    TowerRangeIndicator -.->|14: display range circle| GameScreen
    GameScreen -.->|15: display tower range visualization| Player
    
    Player -->|16: mouseOutTower()| GameScreen
    GameScreen -->|17: handleMouseOut()| InputHandler
    InputHandler -->|18: hideTowerRange()| GameController
    
    GameController -->|19: hideTowerRange()| GameScreen
    GameScreen -->|20: hide()| TowerRangeIndicator
    TowerRangeIndicator -->|21: setVisible(false)| TowerRangeIndicator
    
    GameScreen -.->|22: remove range visualization| Player
``` 