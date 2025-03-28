# Operation Contract: Pause Game

## Operation: pauseGame(): void

## Cross References: Use Case UC10 - Pause Game

## Preconditions:
- A GameSession is active
- The game is currently in a running (not paused) state

## Postconditions:
- The GameSession's isPaused attribute is set to true
- All game animation timers are suspended
- All enemy movement is halted
- All tower targeting and attack activities are suspended
- All spawn timers for enemies and waves are paused
- A visual "Game Paused" indicator is displayed on the screen
- A resume button is displayed prominently
- The game state is preserved exactly as it was at the moment of pausing

## Notes:
- The operation affects only the execution state of the game, not its logical state
- All game objects remain in memory with their properties unchanged
- When resumed, the game should continue from exactly the same state
- Pause state can be entered from either normal or accelerated game speed 