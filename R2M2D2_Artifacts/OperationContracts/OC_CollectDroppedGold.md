# Operation Contract 2: Collect Dropped Gold

**Operation:**  
collectDroppedGold(position: Position): Boolean

**Cross-References:**  
- Use Case: Collect Dropped Gold  
- SSD: Collect Dropped Gold

**Preconditions:**  
- The application is running.
- A gold bag exists at the specified `position`.

**Postconditions:**  
- The gold bag at `position` is removed from the map.
- The player’s gold balance is increased by the amount contained in the gold bag.
- The system updates the display to reflect the new gold balance.

**Notes:**
- The amount of gold in the bag is randomly determined when the bag is dropped (between 2 and half the cost of a Level 1 archer tower).
- Player can collect a given gold bag only once.
- The UI should be updated to reflect the result of the operation.
- Gold bags are only available for collection for 10 seconds after being dropped and can be collected once.
