# Use Case: Save Map

## ID: UC6

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to save created map for future use without losing work
- **Game System**: Needs to store valid maps in persistent storage

## Preconditions
1. Player has created a map in the level editor
2. Player has placed path tiles, tower slots, and decorative elements

## Success Guarantee (Postconditions)
1. Map is validated and saved to persistent storage
2. Map becomes available for selection in the "New Game" menu

## Main Success Scenario
1. Player clicks "Save Map" button in the level editor
2. System prompts player for a map name
3. Player enters a unique name for the map
4. System validates the map according to game rules:
   - Start and end points are placed on map edges
   - Path is fully connected from start to end
   - At least 4 tower slots are placed
5. System saves the map to disk using Java serialization
6. System displays success message
7. System returns to the map editor

## Extensions (Alternative Flows)
- 3a. Player enters name of an existing map:
  1. System asks for confirmation to overwrite
  2. Player confirms overwrite
  3. Continue at step 4
  
  3a-1. Player cancels overwrite:
  1. Return to step 2

- 4a. Map validation fails:
  1. System displays specific error message (e.g., "Path is not connected", "Not enough tower slots")
  2. Player remains in editor to fix issues
  3. Player tries to save again
  4. Continue at step 4

- 5a. System encounters error during save operation:
  1. System displays error message
  2. Player tries again or cancels
  3. If player tries again, continue at step 5

## Special Requirements
- Save operation should complete within 3 seconds
- Saved maps should be accessible across game sessions

## Technology and Data Variations
- Map stored using Java Serializable interface
- Alternatively, map may be stored using custom file format with FileWriter

## Frequency of Occurrence
- Moderate; typically once per map creation session 