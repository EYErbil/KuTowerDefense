# Use Case: Open Options

## ID: UC3

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to access game configuration settings
- **Game System**: Needs to present current configuration and provide interface for modification

## Preconditions
1. Game application is running
2. Player is at the main menu

## Success Guarantee (Postconditions)
1. Options screen is displayed with current game settings
2. Player can modify and save configuration

## Main Success Scenario
1. Player clicks "Options" button from the main menu
2. System retrieves current configuration settings
3. System initializes the options interface with current values
4. System displays the options screen with settings organized by category:
   - Enemy settings
   - Economy settings
   - Combat settings
   - Tower settings
   - Movement settings
5. Player can now modify settings or exit to main menu

## Extensions (Alternative Flows)
- 2a. System cannot retrieve current settings:
  1. System loads default settings
  2. System logs error
  3. Continue at step 3

- 5a. Player modifies settings and saves:
  1. System validates all settings
  2. System saves settings to persistent storage
  3. System acknowledges successful save
  4. System returns to main menu

  5a-1a. Setting validation fails:
  1. System displays validation error message
  2. System highlights problematic setting(s)
  3. Player corrects setting(s)
  4. Return to step 5a

- 5b. Player decides to revert to defaults:
  1. System prompts for confirmation
  2. Player confirms reset
  3. System loads default settings
  4. System updates the options interface
  5. Continue at step 5

  5b-2a. Player cancels reset:
  1. System maintains current settings
  2. Continue at step 5

- 5c. Player exits without saving:
  1. System detects unsaved changes
  2. System asks if player wants to save changes
  3. Player chooses not to save
  4. System discards changes
  5. System returns to main menu

  5c-3a. Player chooses to save:
  1. Continue at step 5a

## Special Requirements
- Settings must be organized in a logical, easy-to-navigate layout
- Numeric settings should have min/max limits and validation
- Reset to defaults should have confirmation to prevent accidental data loss

## Technology and Data Variations
- Settings stored using Java serialization
- UI components adapt based on JavaFX or Swing implementation

## Frequency of Occurrence
- Low to medium; occurs when player wants to customize game experience 