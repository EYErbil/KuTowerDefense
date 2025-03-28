# Operation Contract: Start New Game

## Operation: startNewGame(mapId: String, difficultyLevel: Enum): void

## Cross References: Use Case UC1 - Start New Game

## Preconditions:
- The application is running
- The Player has selected "New Game" from the main menu
- The map with the specified mapId exists in the system
- difficultyLevel is one of the predefined game difficulty settings

## Postconditions:
- The specified map is loaded and initialized
- Game entities are created according to the map definition:
  - Entry and exit points are established
  - Path network is constructed
  - Buildable areas are marked
  - Terrain features are rendered
- Game state is initialized based on difficultyLevel:
  - Player's starting resources (gold, lives)
  - Enemy wave composition and timing
  - Tower costs and upgrade paths
- Game UI is displayed with:
  - Game map as the central element
  - Resource displays (gold, lives)
  - Tower selection interface
  - Wave/progress indicators
  - Game control buttons (pause, speed, quit)
- Game time begins running (unless auto-paused at start)
- The first wave countdown is initiated

## Notes:
- Different difficulty levels should modify game parameters without changing map layout
- Loading large maps may require a progress indicator
- The system should validate map integrity before starting
- Player's previous game statistics should be available but not affect the new game
- Game state should be periodically auto-saved for crash recovery
- Real-time performance monitoring may adjust visual effects to maintain frame rate 