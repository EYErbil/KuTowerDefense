# Use Case: Start New Game

## ID: UC1

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to start a new game session with a selected map
- **Game System**: Needs to initialize game state correctly

## Preconditions
1. Player has launched the game application
2. At least one map is available (either default or custom-created)

## Success Guarantee (Postconditions)
1. Game is initialized with the selected map
2. Player resources (gold, hit points) are set to starting values
3. Game enters 4-second grace period before enemies appear

## Main Success Scenario
1. Player selects "New Game" from the main menu
2. System displays a list of available maps
3. Player selects a map from the list
4. System loads the selected map
5. System initializes game state:
   - Sets player's starting gold based on options
   - Sets player's hit points based on options
   - Initializes wave counters and enemy spawn timers
6. System displays the game screen with the loaded map
7. System starts the 4-second grace period
8. Game begins with first enemy wave after grace period

## Extensions (Alternative Flows)
- 2a. No maps are available:
  1. System displays message indicating no maps are available
  2. System prompts player to create a map in the editor
  3. Player can return to main menu or go to map editor

- 4a. Selected map file is corrupted or incompatible:
  1. System displays error message
  2. Return to step 2

- 7a. Player pauses game during grace period:
  1. System pauses the game
  2. Game timer stops
  3. Player unpauses the game
  4. Continue at step 7 with remaining grace period time

## Special Requirements
- Map loading should complete within 2 seconds
- UI should display countdown during grace period
- Sound effects should play during initialization

## Technology and Data Variations
- Map loaded from serialized Java object
- Game state initialization based on options configuration

## Frequency of Occurrence
- High; occurs each time player starts a new game session 