# Glossary: KU Tower Defense

## Game Concepts

### Tower Defense
A subgenre of strategy games where the player defends a map or territory by placing defensive structures along a path that enemies follow.

### Map
The game field on which gameplay takes place, containing a path for enemies and slots for tower placement.

### Path
A designated route on the map that enemies follow from the starting point to the exit point.

### Wave
A scheduled group of enemy units that appear on the map. The game consists of multiple waves that the player must survive.

### Group
A subset of enemies within a wave that appear together with minimal delay between them.

### Tower Slot
A designated position on the map where towers can be constructed.

### Gold
The in-game currency used to construct and upgrade towers. Earned by defeating enemies.

### Hit Points (HP)
A numerical representation of health. The player loses hit points when enemies reach the exit; enemies lose hit points when hit by tower attacks.

### Area of Effect (AOE)
An attack that affects multiple enemies within a certain radius of the impact point.

### Range
The maximum distance at which a tower can target and attack enemies, represented as a radius.

### Rate of Fire
How quickly a tower can launch attacks, measured in attacks per second or similar time unit.

### Grace Period
The initial time at the start of a game (4 seconds) during which no enemies appear, allowing the player to build initial defenses.

## Game Objects

### Tower
A defensive structure that automatically attacks enemies within its range.

#### Archer Tower
A tower type that attacks single targets with arrows at a high rate of fire.

#### Artillery Tower
A tower type that launches shells causing area of effect damage at a low rate of fire.

#### Mage Tower
A tower type that casts spells at a medium rate of fire, targeting single enemies.

### Enemy
A unit that follows the path from start to exit and must be defeated by towers.

#### Goblin
A fast enemy type with lower health, weaker against arrows but stronger against spells.

#### Knight
A slow enemy type with higher health, stronger against arrows but weaker against spells.

### Projectile
An attack object launched by a tower toward enemies.

#### Arrow
A projectile fired by Archer Towers that hits a single target.

#### Artillery Shell
A projectile fired by Artillery Towers that causes area of effect damage.

#### Spell
A projectile cast by Mage Towers that hits a single target.

## User Interface Elements

### Main Menu
The initial screen that provides access to game modes and options.

### Map Editor
A tool for creating custom maps by placing tiles on a grid.

### Options Screen
A menu for configuring game parameters such as enemy properties, tower properties, and economy settings.

### Tower Selection Menu
A popup interface that appears when clicking on an empty tower slot, allowing selection of tower type to construct.

### Tower Information Menu
A popup interface that appears when clicking on an existing tower, showing properties and actions.

### Tower Range Indicator
A visual circle showing the attack range of a tower, displayed when hovering over or selecting a tower.

### Health Bar
A visual indicator showing the current hit points of an enemy.

## Technical Terms

### Tile
A single grid cell in the map editor, which can be assigned different types (path, tower slot, decorative, etc.).

### Serialization
The process of converting game objects to a format that can be stored to disk and retrieved later.

### Frame Rate
The frequency at which the game visual display is updated, measured in frames per second (FPS).

### Targeting Algorithm
The logic that determines which enemy a tower will attack when multiple enemies are in range. 