# Use Case: Pause Game

## ID: UC10

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to temporarily halt gameplay to strategize or attend to external interruptions
- **Game System**: Needs to properly suspend all game activities without losing state

## Preconditions
1. Game session is active and running
2. Game is not already in paused state

## Success Guarantee (Postconditions)
1. All game animations, movements, and timers are suspended
2. Game state is preserved entirely
3. Visual indication of paused state is displayed
4. Game is ready to resume from exactly the same state

## Main Success Scenario
1. Player clicks the pause button during gameplay
2. System suspends all enemy movements
3. System suspends all tower targeting and attacks
4. System suspends all timers (enemy spawn, wave countdown)
5. System displays a "Game Paused" indicator on screen
6. System shows resume button prominently
7. Game remains in paused state until player action

## Extensions (Alternative Flows)
- 1a. Player uses keyboard shortcut instead of button:
  1. Player presses pause key (e.g., 'P' or ESC)
  2. Continue at step 2

- 6a. Player clicks resume button:
  1. System removes "Game Paused" indicator
  2. System resumes all enemy movements
  3. System resumes all tower targeting and attacks
  4. System resumes all timers
  5. Game continues from where it was paused

- 6b. Player exits to main menu while paused:
  1. System prompts for confirmation
  2. Player confirms exit
  3. Game session ends and returns to main menu
  4. All progress in current game is lost

## Special Requirements
- Pause/resume transition must occur within 0.5 seconds
- Pause state must be visually obvious to player
- No game state changes should occur while paused

## Technology and Data Variations
- Keyboard and mouse input for pause/resume actions

## Frequency of Occurrence
- Moderate to high; occurs whenever player needs to temporarily stop gameplay 