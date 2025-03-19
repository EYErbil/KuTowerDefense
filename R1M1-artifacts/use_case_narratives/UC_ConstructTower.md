# Use Case: Construct Tower

## ID: UC8

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to build a defense tower to stop enemies
- **Game System**: Needs to handle tower placement and gold deduction

## Preconditions
1. Game is in progress
2. Player has enough gold for at least one type of tower
3. At least one empty tower slot is available on the map

## Success Guarantee (Postconditions)
1. Tower is constructed on the selected empty slot
2. Tower starts functioning according to its type
3. Appropriate amount of gold is deducted from player's resources

## Main Success Scenario
1. Player clicks on an empty tower slot on the map
2. System displays a tower selection menu showing:
   - Archer Tower option with cost
   - Artillery Tower option with cost
   - Mage Tower option with cost
3. Player selects a tower type
4. System verifies player has enough gold for the selected tower
5. System constructs the tower on the selected slot
6. System deducts the tower cost from player's gold
7. Tower becomes active and starts targeting enemies within range
8. System closes the tower selection menu

## Extensions (Alternative Flows)
- 1a. Player clicks elsewhere on the screen:
  1. No tower selection menu appears
  2. Use case ends

- 3a. Player clicks outside the tower selection menu:
  1. System closes the tower selection menu
  2. Use case ends

- 4a. Player does not have enough gold for selected tower:
  1. System displays "Not enough gold" message
  2. Tower is not constructed
  3. No gold is deducted
  4. Tower selection menu remains open
  5. Return to step 3

## Special Requirements
- Tower selection menu should appear immediately on click
- Tower construction animation should complete within 0.5 seconds
- Tower range should be visually indicated when tower is selected

## Technology and Data Variations
- Mouse click interaction for tower placement and selection
- Different tower types have different visual representations and costs

## Frequency of Occurrence
- Very high; occurs multiple times during a single game session 