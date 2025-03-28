# Use Case: Quit Game

## ID: UC4

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to safely exit the application without data loss
- **Game System**: Needs to properly clean up resources and save any necessary state

## Preconditions
1. Game application is running
2. Player is on any screen (main menu, game session, options, map editor)

## Success Guarantee (Postconditions)
1. All unsaved data is handled appropriately (saved or discarded with confirmation)
2. All system resources are properly released
3. Application terminates cleanly

## Main Success Scenario
1. Player clicks "Quit Game" button from the main menu
2. System verifies there is no unsaved data
3. System releases all resources
4. System terminates the application

## Extensions (Alternative Flows)
- 1a. Player is in an active game session:
  1. Player clicks "Exit to Main Menu" button
  2. System prompts for confirmation since progress will be lost
  3. Player confirms exit
  4. System ends game session and returns to main menu
  5. Continue with main success scenario

- 1b. Player is in map editor with unsaved changes:
  1. Player clicks "Exit to Main Menu" button
  2. System detects unsaved changes
  3. System prompts player to save changes
  4. Player chooses to save changes
  5. System saves the map
  6. System returns to main menu
  7. Continue with main success scenario

  1b-4a. Player chooses not to save changes:
  1. System discards unsaved changes
  2. System returns to main menu
  3. Continue with main success scenario

  1b-4b. Player cancels the exit action:
  1. System returns player to map editor
  2. Use case ends

- 1c. Player uses the operating system's window close button:
  1. System intercepts the window closing event
  2. Continue at step 2

## Special Requirements
- Confirmation prompts must clearly explain consequences of actions
- Application must terminate without leaving orphaned processes

## Technology and Data Variations
- Window system events for detecting application close requests
- Dialog windows for confirmation prompts

## Frequency of Occurrence
- Low; occurs once per application session 