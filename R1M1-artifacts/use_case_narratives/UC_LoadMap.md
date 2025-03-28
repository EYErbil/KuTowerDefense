# Use Case: Load Map

## ID: UC7

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to select and load a previously saved map for gameplay
- **Game System**: Needs to accurately retrieve and render a saved map

## Preconditions
1. Player has selected "New Game" from the main menu
2. At least one map has been saved in the system

## Success Guarantee (Postconditions)
1. Selected map is loaded into the game session
2. Map is rendered correctly with all components (path, tower slots, decorative elements)
3. Game is ready to begin with the loaded map

## Main Success Scenario
1. System displays a list of available saved maps
2. For each map, system displays:
   - Map name
   - Preview thumbnail (if available)
   - Creation/modification date
3. Player selects a map from the list
4. System loads the map file from storage
5. System deserializes the map data
6. System verifies map integrity and compatibility
7. System renders the map on the game screen
8. System initializes game state based on the loaded map
9. System begins game session with the loaded map

## Extensions (Alternative Flows)
- 1a. No maps are available:
  1. System displays message indicating no maps are available
  2. System provides options to create a new map or return to main menu
  3. Use case ends

- 4a. Map file cannot be found:
  1. System displays error message
  2. System removes the missing map from the list
  3. Return to step 1

- 5a. Map data is corrupted or incompatible:
  1. System displays error message
  2. System provides option to delete the corrupted map
  3. Return to step 1

- 3a. Player cancels map selection:
  1. System returns to main menu
  2. Use case ends

## Special Requirements
- Map loading must complete within 2 seconds
- Error messages must clearly indicate the nature of the problem
- Map preview should accurately represent the actual map

## Technology and Data Variations
- Maps loaded from Java serialized objects
- Maps may alternatively be loaded from custom file format

## Frequency of Occurrence
- High; occurs each time player starts a new game with an existing map 