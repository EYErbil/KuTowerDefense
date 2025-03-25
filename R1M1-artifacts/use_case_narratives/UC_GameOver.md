# Use Case: Game Over

## ID: UC14

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants clear indication of game ending and options to proceed
- **Game System**: Needs to properly terminate game session and present options

## Preconditions
1. Game session is active
2. Player's hit points have reached zero OR all enemy waves have been defeated

## Success Guarantee (Postconditions)
1. Game session is properly terminated
2. Game outcome is clearly communicated to the player
3. Player is presented with options to proceed

## Main Success Scenario
1. System detects game ending condition:
   - Player's hit points reach zero (defeat)
   - All enemy waves are defeated (victory)
2. System stops all game animations and enemy spawning
3. System displays appropriate game over banner:
   - "Game Over" for defeat
   - "Victory!" for successful defense
4. System calculates and displays game statistics:
   - Total enemies defeated
   - Gold collected during game
   - Waves completed
   - Time played
5. System displays options for player:
   - Return to main menu
   - Retry same map
   - View detailed statistics
6. Player selects an option
7. System responds according to selection

## Extensions (Alternative Flows)
- 6a. Player selects "Return to main menu":
  1. System cleans up game resources
  2. System displays main menu

- 6b. Player selects "Retry same map":
  1. System cleans up game resources
  2. System reloads the same map
  3. System initializes a new game session
  4. Continue with new game flow

- 6c. Player selects "View detailed statistics":
  1. System displays detailed game statistics
  2. Player reviews statistics
  3. Player clicks "Back"
  4. Return to step 5

- 6d. Player doesn't select any option for an extended period:
  1. System automatically returns to main menu after timeout
  2. System cleans up game resources

## Special Requirements
- Game over screen must be visually distinct from normal gameplay
- Statistics should be clearly presented and readable
- Option selection should be intuitive and responsive

## Technology and Data Variations
- Mouse click for option selection
- Statistics calculated from game session data

## Frequency of Occurrence
- High; occurs at the conclusion of every game session 