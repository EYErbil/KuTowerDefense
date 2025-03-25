# Operation Contract: Game Over

## Operation: endGame(isVictory: Boolean): void

## Cross References: Use Case UC14 - Game Over

## Preconditions:
- A GameSession is active
- One of the game ending conditions has been met:
  - Player's hit points have reached zero (isVictory = false)
  - All enemy waves have been defeated (isVictory = true)

## Postconditions:
- All game animations and movements are stopped
- Enemy spawning is terminated
- Appropriate game over banner is displayed based on isVictory
- Game statistics are calculated and displayed:
  - Total enemies defeated
  - Gold collected
  - Waves completed
  - Game duration
- Player options are presented:
  - Return to main menu
  - Retry same map
  - View detailed statistics
- The GameSession object remains in memory until player selects an option

## Notes:
- This operation handles only the game ending sequence, not the player's subsequent choices
- Game statistics should be calculated from the current GameSession state
- Resources should be properly cleaned up when player makes a selection
- A timeout mechanism should be implemented if player makes no selection 