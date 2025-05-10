## Operation Contract 1: Upgrade Tower

**Operation:**  
upgradeTower(towerPosition)

**Cross-References:**  
- Use Case: Upgrade Tower  
- SSD: Upgrade Tower

**Preconditions:**  
- The player has selected a valid tower at `towerPosition`.
- The tower is eligible for upgrade (not already at max level).

**Postconditions:**  
- If the player has sufficient gold for the upgrade:
    - The player's gold balance is decreased by the upgrade cost.
    - The selected tower at `towerPosition` is upgraded to the next level.
    - The tower’s attributes (range, rate of fire, etc.) are updated according to its new level.
    - The tower’s graphic is updated to reflect the new level.
- Else (insufficient gold):
    - No changes to the tower or player’s gold.
    - The system notifies the player of insufficient gold.
