# Operation Contract: Start New Game

## Operation: startNewGame(mapName: String): GameSession

## Cross References: Use Case UC1 - Start New Game

## Preconditions:
- The application is running
- At least one Map has been saved in the system
- The Player has selected a map to load

## Postconditions:
- A new GameSession object is created
- The selected Map is loaded into the GameSession
- Player's starting resources are initialized:
  - Gold is set to starting value from options
  - Hit points are set to starting value from options
- Enemy wave data is initialized according to options:
  - Number of waves
  - Groups per wave
  - Enemies per group
  - Delays between waves, groups, and enemies
- Grace period timer (4 seconds) is started
- Game UI elements are displayed:
  - Map with path and tower slots
  - Player resources (gold, hit points)
  - Wave counter
  - Game control buttons (pause, speed toggle)

## Notes:
- The GameSession maintains all state for the current game
- Enemy spawn scheduling begins after the grace period expires
- The GameSession object is returned to allow referencing the current game 