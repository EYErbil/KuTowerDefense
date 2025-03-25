# Use Case: Toggle Game Speed

## ID: UC11

## Primary Actor
Player

## Level
User goal

## Stakeholders and Interests
- **Player**: Wants to control the pace of gameplay to reduce waiting or have more time for decision-making
- **Game System**: Needs to maintain consistent game mechanics while adjusting speed

## Preconditions
1. Game session is active
2. Game is not in paused state

## Success Guarantee (Postconditions)
1. Game speed is toggled between normal and accelerated modes
2. All game mechanics function correctly at the new speed
3. Visual indication of current speed mode is displayed to player

## Main Success Scenario
1. Player clicks the speed toggle button during gameplay
2. System determines current speed setting
3. System switches from current speed to alternate speed:
   - If currently at normal speed, switches to accelerated speed
   - If currently at accelerated speed, switches to normal speed
4. System updates the visual indicator to show current speed
5. System adjusts the speed of:
   - Enemy movement along paths
   - Tower attack animations
   - Projectile movements
   - Timer countdowns (wave timers, spawn delays)
6. Game continues at the new speed setting

## Extensions (Alternative Flows)
- 1a. Player uses keyboard shortcut instead of button:
  1. Player presses speed toggle key (e.g., 'S')
  2. Continue at step 2

- 5a. Player pauses game after speed toggle:
  1. System pauses all game activity
  2. Speed setting is preserved
  3. When game is resumed, it continues at the toggled speed

- 5b. Player encounters performance issues at accelerated speed:
  1. System detects frame rate drop below acceptable threshold
  2. System automatically reverts to normal speed
  3. System displays notification about speed adjustment

## Special Requirements
- Speed toggle transition must appear smooth
- Accelerated speed should target approximately 2x normal speed
- Visual indicator must clearly show current speed setting

## Technology and Data Variations
- Keyboard and mouse input for speed toggling
- Speed multiplier value may be configurable in game options

## Frequency of Occurrence
- Moderate; occurs when player wants to adjust pace of gameplay 