```mermaid
sequenceDiagram
    participant Player
    participant UI
    participant GameSystem

    Player->>UI: clickTower(towerPosition)
    UI->>GameSystem: requestTowerInfo(towerPosition)
    GameSystem-->>UI: towerInfo(level, upgradeCost, canUpgrade)
    UI-->>Player: showUpgradeMenu(canUpgrade, upgradeCost)
    Player->>UI: selectUpgrade()
    UI->>GameSystem: requestUpgradeTower(towerPosition)
    alt Player has enough gold
        GameSystem->>GameSystem: upgradeTower(towerPosition)
        GameSystem-->>UI: upgradeSuccess(newLevel)
        UI-->>Player: showUpgradeSuccess(newLevel)
    else Not enough gold
        GameSystem-->>UI: upgradeFailed("Insufficient gold")
        UI-->>Player: showUpgradeFailed()
    end
