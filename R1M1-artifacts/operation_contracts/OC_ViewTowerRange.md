# Operation Contract: View Tower Range

## Operation: displayTowerRange(towerPosition: Position): void

## Cross References: Use Case UC12 - View Tower Range

## Preconditions:
- A GameSession is active
- The mouse cursor is positioned over a Tower or empty tower slot in construction mode
- The Tower or preview tower type has a defined attack range

## Postconditions:
- The visual representation of the Tower's attack range is calculated
- A circular range indicator is displayed on the game screen centered on the Tower
- The range indicator shows the exact area within which the Tower can target enemies
- The range indicator remains visible as long as the mouse cursor is over the Tower
- The range indicator is removed when the mouse cursor moves away from the Tower
- The visual state of other game elements remains unchanged

## Notes:
- Range indicators should be semi-transparent to not obscure the game field
- Different tower types may use different colors or styles for their range indicators
- The operation does not return a value as it only affects the visual representation 