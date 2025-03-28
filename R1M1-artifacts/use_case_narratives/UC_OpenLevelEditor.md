# Use Case: Open Level Editor

## ID: UC2

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to create or edit custom maps for gameplay
- **Game System**: Needs to provide map creation tools that generate valid game maps

## Preconditions
1. Game application is running
2. Player is at the main menu

## Success Guarantee (Postconditions)
1. Level editor is opened and ready for map creation/editing
2. All editor tools are initialized and available to the player

## Main Success Scenario
1. Player clicks "Level Editor" button from the main menu
2. System initializes the level editor interface
3. System creates a new empty map grid
4. System initializes the tile palette with all available tile types:
   - Path tiles
   - Tower slot tiles
   - Decorative tiles (grass, trees, etc.)
5. System initializes editor tools:
   - Start/end point markers
   - Clear map function
   - Save map function
6. System displays the level editor interface with empty grid
7. Player can now edit the map using the provided tools

## Extensions (Alternative Flows)
- 3a. System offers option to edit existing map:
  1. System displays list of existing maps
  2. Player selects a map to edit
  3. System loads the selected map
  4. Continue at step 4

  3a-2a. Player cancels map selection:
  1. System returns to step 3 (creates new empty map)

  3a-3a. Map loading fails:
  1. System displays error message
  2. System returns to step 3 (creates new empty map)

- 7a. Player completes map editing:
  1. Player clicks "Save" button
  2. System initiates map saving process
  3. Use case "Save Map" begins

- 7b. Player exits editor without saving:
  1. System detects unsaved changes
  2. System prompts player to save changes
  3. Player chooses not to save
  4. System discards changes
  5. System returns to main menu

  7b-3a. Player chooses to save:
  1. System initiates map saving process
  2. Use case "Save Map" begins

## Special Requirements
- Editor interface must be intuitive and responsive
- Grid size must accommodate maps of sufficient complexity
- Visual feedback must clearly indicate selected tile type and placement

## Technology and Data Variations
- Map editor implemented using same UI framework as main game (JavaFX or Swing)
- Grid-based editing with mouse controls

## Frequency of Occurrence
- Medium; occurs when player wants to create custom maps for gameplay 