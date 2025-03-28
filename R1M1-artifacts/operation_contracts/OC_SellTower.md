# Operation Contract: Sell Tower

## Operation: sellTower(towerPosition: Position): Integer

## Cross References: Use Case UC9 - Sell Tower

## Preconditions:
- A GameSession is active
- A Tower exists at the specified towerPosition

## Postconditions:
- The Tower at towerPosition is removed from the map
- The Tower is replaced with an empty tower slot at the same position
- A reimbursement amount is calculated (portion of the tower's original cost)
- The reimbursement amount is added to the Player's gold
- Any ongoing targeting or attack activities of the Tower are terminated
- The Tower object is removed from the game's object model
- The operation returns the reimbursement amount

## Notes:
- The reimbursement amount is typically less than the full construction cost
- Different tower types may have different reimbursement rates
- All references to the sold Tower should be cleaned up to prevent memory leaks 