## Operation Contract: Upgrade Tower

**Operation:**  
upgradeTower(towerPosition: Position): Boolean

**Cross-References:**  
- Use Case: Upgrade Tower  
- SSD: Upgrade Tower

**Preconditions:**  
- The application is running.
- The Player has selected a valid tower at `towerPosition`.
- The tower is eligible for upgrade (not already at max level).

**Postconditions:**  
- If the Player has sufficient gold for the upgrade:
  - The Player's gold balance is decreased by the upgrade cost.
  - The selected tower at `towerPosition` is upgraded to the next level.
  - The tower’s attributes (range, rate of fire, etc.) are updated according to its new level.
  - The tower’s graphic is updated to reflect the new level.
  - The operation returns true.
- Else (insufficient gold):
  - No changes to the tower or Player’s gold.
  - The system notifies the Player of insufficient gold.
  - The operation returns false.

**Notes:**
- The upgrade cost and new attributes are determined by the tower type and its current level.
- The operation should be atomic: either all changes are applied, or none are.
- The UI should be updated to reflect the result of the operation.
- If the Player cancels the upgrade, no changes are made and the operation is not invoked.
- Tower upgrades are persistent for the current game session only.
