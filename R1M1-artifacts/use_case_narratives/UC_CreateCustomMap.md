# Use Case: Create Custom Map

## ID: UC5

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to create a customized map layout for gameplay that provides an interesting challenge.
- **Game System**: Needs valid map configurations that conform to game rules.

## Preconditions
1. Player has launched the game application
2. Player has opened the level editor

## Success Guarantee (Postconditions)
1. A valid map is created with:
   - Connected path from start to end points
   - Properly placed tower slots (at least 4)
   - Valid start and end points at map edges
   - Appropriate decorative elements

## Main Success Scenario
1. Player selects the map editor from the main menu
2. System displays an empty grid and tile selection palette
3. Player selects the path tile from the palette
4. Player places path tiles on the grid to create a continuous path
5. Player marks one edge tile as the path start point
6. Player marks another edge tile as the path end point
7. Player selects the tower slot tile from the palette
8. Player places at least 4 tower slot tiles adjacent to the path
9. Player selects and places decorative tiles (grass, trees, etc.) as desired
10. Player saves the map

## Extensions (Alternative Flows)
- *a. At any time, player can clear the map and start over:
  1. Player clicks "Clear Map" button
  2. System clears the grid back to empty state
  3. Continue at step 2

- 5a. Player tries to mark start point not on the edge:
  1. System displays warning message
  2. Player must place start point on edge tile

- 6a. Player tries to mark end point not on the edge:
  1. System displays warning message
  2. Player must place end point on edge tile

- 10a. Player tries to save invalid map:
  1. System validates map design
  2. System displays specific error message explaining validation failure
  3. Player corrects the issue
  4. Continue at step 10

## Special Requirements
- UI must clearly indicate selected tile type
- Grid must show tile placement guidelines
- Start and end points must be visually distinctive

## Technology and Data Variations
- Mouse control for tile selection and placement
- Optional drag and drop placement of tiles
- Map saved as serialized Java object

## Frequency of Occurrence
- Moderate; typically once per gameplay session 