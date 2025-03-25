# Operation Contract: Toggle Game Speed

## Operation: toggleGameSpeed(): void

## Cross References: Use Case UC11 - Toggle Game Speed

## Preconditions:
- A GameSession is active
- The game is not in paused state

## Postconditions:
- The GameSession's gameSpeed attribute is toggled between normal and accelerated values
- If previously normal speed, game is now running at accelerated speed:
  - Enemy movement speed is increased
  - Tower attack rate is increased
  - Projectile movement speed is increased
  - Timer countdowns are accelerated
- If previously accelerated speed, game is now running at normal speed:
  - Enemy movement speed is reset to normal
  - Tower attack rate is reset to normal
  - Projectile movement speed is reset to normal
  - Timer countdowns run at normal rate
- The visual speed indicator is updated to reflect the current speed setting
- Game mechanics continue to function correctly at the new speed

## Notes:
- The operation only affects the rate at which the game progresses
- Game logic and rules remain unchanged regardless of speed setting
- Speed multiplier is typically 2x for accelerated mode
- If performance issues are detected in accelerated mode, the system may automatically revert to normal speed 