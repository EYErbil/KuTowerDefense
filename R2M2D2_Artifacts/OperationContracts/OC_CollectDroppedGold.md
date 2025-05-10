# Operation Contract 2: Collect Dropped Gold

**Operation:**  
collectDroppedGold(position)

**Cross-References:**  
- Use Case: Collect Dropped Gold  
- SSD: Collect Dropped Gold

**Preconditions:**  
- A gold bag exists at the specified `position`.
- The gold bag has not expired (i.e., less than 10 seconds have passed since it was dropped).
- The gold bag has not already been collected.

**Postconditions:**  
- The gold bag at `position` is removed from the map.
- The player’s gold balance is increased by the amount contained in the gold bag.
- The system updates the display to reflect the new gold balance.
- If the gold bag does not exist or has expired, no changes are made and the player is notified.
