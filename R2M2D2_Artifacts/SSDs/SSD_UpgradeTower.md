```mermaid
sequenceDiagram
    actor Player
    participant System

    Player->>System: selectTower(towerPosition)
    Player->>System: requestUpgradeTower(towerPosition)
    alt Player has enough gold
        System-->>Player: towerUpgraded(newLevel, newStats)
    else Not enough gold
        System-->>Player: upgradeFailed("Insufficient gold")
    end
