# Use Case: Sell Tower

## ID: UC9

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to remove a tower and recover some gold for strategic adjustments
- **Game System**: Needs to handle tower removal and gold reimbursement

## Preconditions
1. Game is in progress
2. At least one tower exists on the map

## Success Guarantee (Postconditions)
1. Selected tower is removed from the map
2. Tower slot becomes available for new construction
3. Player receives a portion of the tower's cost as gold

## Main Success Scenario
1. Player clicks on an existing tower
2. System displays tower information and action menu
3. Player selects "Sell" option from the menu
4. System calculates reimbursement amount (portion of original tower cost)
5. System removes the tower from the map
6. System adds the reimbursement gold to player's resources
7. Tower slot returns to empty state
8. System closes the tower action menu

## Extensions (Alternative Flows)
- 1a. Player clicks elsewhere on the screen:
  1. No tower information menu appears
  2. Use case ends

- 3a. Player clicks outside the tower action menu:
  1. System closes the tower action menu
  2. Use case ends

- 3b. Player selects a different option from the menu:
  1. System processes the different action
  2. This use case ends

## Special Requirements
- Tower removal animation should complete within 0.5 seconds
- Gold reimbursement amount should be clearly displayed
- Empty tower slot should be immediately available for new construction

## Technology and Data Variations
- Mouse click interaction for tower selection and selling
- Different tower types may have different reimbursement rates

## Frequency of Occurrence
- Moderate; occurs occasionally during a game session when player adjusts strategy 