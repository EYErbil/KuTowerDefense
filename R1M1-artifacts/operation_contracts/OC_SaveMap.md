# Operation Contract: Save Map

## Operation: saveMap(mapName: String): Boolean

## Cross References: Use Case UC6 - Save Map

## Preconditions:
- A Map object exists in the system
- The Map has been created or modified in the map editor
- The Player has provided a name for the map

## Postconditions:
- The Map has been validated for:
  - Start and end points at map edges
  - Fully connected path from start to end
  - At least 4 tower slot locations
- If the Map is valid, it is serialized and stored to disk
- If a Map with the given name already exists, it is overwritten (with player confirmation)
- The operation returns true if save was successful, false otherwise
- The Map becomes available for selection in the "New Game" flow

## Notes:
- The system uses Java's Serializable interface for persistent storage
- The validation step is critical to prevent invalid maps from being saved 