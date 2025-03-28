# Operation Contract: Construct Tower

## Operation: constructTower(slotPosition: Position, towerType: TowerType): Tower

## Cross References: Use Case UC8 - Construct Tower

## Preconditions:
- A GameSession is active
- The specified slotPosition contains an empty tower slot
- The Player has selected a tower type to construct
- The Player has sufficient gold to afford the selected tower type

## Postconditions:
- A new Tower object of the specified towerType is created
- The Tower is placed at the specified slotPosition on the map
- The empty tower slot at slotPosition is replaced with the Tower
- The Player's gold is reduced by the tower's cost
- The Tower begins targeting enemies according to its attack strategy:
  - Targets enemy furthest along path within range
  - Fires projectiles at its target at specified rate
- The Tower's range and attack properties are set according to its type and game options

## Notes:
- Tower types include Archer, Artillery, and Mage, each with different properties
- The constructed Tower object is returned to allow direct referencing
- If preconditions are not met (insufficient gold, invalid position), the operation will not execute and return null 