## Use Case 1: Upgrade a Tower

**Title:** Upgrade a Tower

**Primary Actor:** Player

**Goal:**  
Upgrade an existing tower to Level 2 to gain enhanced abilities.

**Preconditions:**  
- The player has built at least one tower on the map.  
- The player has sufficient gold to afford the upgrade cost for the selected tower.

**Main Success Scenario:**  
1. The player clicks on a tower on the map.
2. The game displays a menu with an option to upgrade the tower, showing the upgrade cost.
3. If the player has enough gold, the upgrade option is selectable; otherwise, it is visually disabled (e.g., grayed out or marked with an X).
4. The player selects the upgrade option.
5. The game deducts the upgrade cost from the player's gold balance.
6. The tower is upgraded to Level 2, and its appearance changes to the new graphic.
7. The tower's abilities are enhanced according to its type:
    - **Archer Tower:** 50% wider attack range, 2x rate of fire, same damage per arrow.
    - **Artillery Tower:** 20% larger attack range, 20% more AOE damage, same impact radius.
    - **Mage Tower:** Same damage and rate of fire, but now applies a 20% slow for 4 seconds to hit enemies (with a snowflake icon indicator; effect is not stackable, but timer resets if hit again; spell color is different from Level 1).
8. The player can now benefit from the upgraded tower's new abilities.

**Extensions/Alternatives:**  
- 3a. If the player does not have enough gold, the upgrade option is disabled and cannot be selected.
- 4a. If the player cancels the menu, no action is taken.
