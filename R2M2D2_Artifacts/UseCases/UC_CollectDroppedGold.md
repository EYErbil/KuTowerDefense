## Use Case 2: Collect Dropped Gold from Defeated Enemies

**Title:** Collect Dropped Gold

**Primary Actor:** Player

**Goal:**  
Collect gold dropped by defeated enemies to increase the gold balance.

**Preconditions:**  
- There are enemies on the map.
- At least one enemy is defeated.

**Main Success Scenario:**  
1. An enemy is defeated.
2. The game determines, based on a probability, whether the enemy drops a bag of gold.
3. If a bag of gold is dropped, it appears at the location where the enemy was defeated.
4. The amount of gold in the bag is randomly determined (between 2 and half the cost of a Level 1 archer tower).
5. The player notices the bag of gold and clicks on it with the mouse.
6. The bag disappears, and the gold amount is added to the player's current gold balance.

**Extensions/Alternatives:**  
- 3a. If the player does not click the bag of gold within 10 seconds, the bag disappears and the gold is lost.
- 2a. If the enemy does not drop a bag of gold, nothing appears.
