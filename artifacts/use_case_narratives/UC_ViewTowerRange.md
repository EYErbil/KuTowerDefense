# Use Case: View Tower Range

## ID: UC12

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to see the attack range of a tower for strategic placement and planning
- **Game System**: Needs to accurately visualize tower attack ranges

## Preconditions
1. Game is in progress
2. At least one tower exists on the map or player is in tower construction mode

## Success Guarantee (Postconditions)
1. Tower's attack range is visually displayed on the game screen
2. Player can make informed decisions about tower positioning or enemy path coverage

## Main Success Scenario
1. Player hovers mouse cursor over an existing tower
2. System detects the hover action
3. System calculates the tower's attack range based on tower type and properties
4. System displays a circular visual indicator showing the tower's attack radius
5. The indicator remains visible as long as the cursor remains over the tower
6. Player moves cursor away from the tower
7. System removes the range indicator

## Extensions (Alternative Flows)
- 1a. Player hovers over an empty tower slot while in construction mode:
  1. System displays preview of selected tower type's range
  2. Continue at step 3

- 1b. Player clicks on a tower (instead of hovering):
  1. System displays tower information menu
  2. System displays tower range indicator
  3. Indicator remains visible until menu is closed
  4. Continue at step 3

- 5a. Player clicks on the tower while range is displayed:
  1. Tower information menu appears
  2. Range indicator remains visible
  3. Continue at step 5

## Special Requirements
- Range indicator should be semi-transparent to not obstruct view of the game field
- Different tower types may have different range indicator colors
- Range indicator should appear immediately on hover (< 0.1 seconds)

## Technology and Data Variations
- Mouse position tracking for hover detection
- Range displayed based on tower properties from game state

## Frequency of Occurrence
- Very high; occurs frequently during a game session when player is planning strategy 