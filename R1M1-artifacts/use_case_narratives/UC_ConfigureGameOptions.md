# Use Case: Configure Game Options

## ID: UC13

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to customize game parameters for desired difficulty and experience
- **Game System**: Needs to store and apply configuration settings across game sessions

## Preconditions
1. Player has launched the game application
2. Player has accessed the options screen from the main menu

## Success Guarantee (Postconditions)
1. Game options are configured according to player's preferences
2. Options are saved and persist across game sessions
3. New game instances will use the configured options

## Main Success Scenario
1. Player selects "Options" from the main menu
2. System displays the options screen with current settings
3. Player adjusts enemy-related parameters:
   - Number of waves, groups per wave, enemies per group
   - Delay between waves, groups, and enemies
   - Composition of enemy types per group/wave
4. Player adjusts economy-related parameters:
   - Starting gold amount
   - Gold earned from defeating enemies
5. Player adjusts combat-related parameters:
   - Player's starting hit points
   - Enemy hit points
   - Tower damage values
6. Player adjusts tower-related parameters:
   - Construction costs
   - Range and rate of fire
   - AOE damage radius for artillery
7. Player adjusts movement speeds for different enemy types
8. Player clicks "Save" to apply changes
9. System saves configuration to persistent storage
10. System returns to the main menu

## Extensions (Alternative Flows)
- 8a. Player clicks "Reset to Defaults":
  1. System reverts all settings to default values
  2. Continue at step 3
  
- 8b. Player clicks "Cancel":
  1. System discards changes
  2. System returns to the main menu
  3. Use case ends

- 9a. System encounters error while saving:
  1. System displays error message
  2. Player acknowledges error
  3. Return to step 8

## Special Requirements
- UI should provide sliders or input fields for numeric values
- Changes should be visually reflected in the UI immediately
- Save operation should complete within 2 seconds

## Technology and Data Variations
- Options stored using Java properties or serialization
- UI components may vary between Swing and JavaFX implementations

## Frequency of Occurrence
- Low to moderate; typically occurs when player first starts using the application or wants to adjust difficulty 