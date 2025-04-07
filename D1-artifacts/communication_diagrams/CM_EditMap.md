# Communication Diagram: Edit Map

```mermaid
graph TD
    Player([Player])
    MainMenuScreen[MainMenuScreen]
    MapEditorController[MapEditorController]
    MapEditorScreen[MapEditorScreen]
    MapSerializer[MapSerializer]
    Map[Map]
    Tile[Tile]
    Path[Path]
    ValidationService[ValidationService]

    Player -->|1: clickEditMap()| MainMenuScreen
    MainMenuScreen -->|2: openMapEditor()| MapEditorController
    
    MapEditorController -->|3a: loadMapList()| MapSerializer
    MapSerializer -.->|4a: availableMaps| MapEditorController
    MapEditorController -.->|5a: showMapSelection(availableMaps)| Player
    Player -->|6a: selectMap(mapId)| MapEditorController
    MapEditorController -->|7a: loadMap(mapId)| MapSerializer
    MapSerializer -.->|8a: mapData| MapEditorController
    MapEditorController -->|9a: new Map(mapData)| Map
    
    Player -->|3b: selectNewMap()| MapEditorController
    MapEditorController -->|4b: showSizeDialog()| MapEditorController
    Player -->|5b: setMapDimensions(width, height)| MapEditorController
    MapEditorController -->|6b: new Map(width, height)| Map
    
    Map -->|10: initializeGrid()| Map
    Map -.->|11: initializedMap| MapEditorController
    
    MapEditorController -->|12: initialize(initializedMap)| MapEditorScreen
    MapEditorScreen -->|13: setupTileSelector()| MapEditorScreen
    MapEditorScreen -->|14: setupMapGrid()| MapEditorScreen
    MapEditorScreen -->|15: setupToolPanel()| MapEditorScreen
    MapEditorScreen -.->|16: display editor screen| Player
    
    Player -->|17: selectTileType(tileType)| MapEditorScreen
    MapEditorScreen -->|18: setSelectedTileType(tileType)| MapEditorController
    
    Player -->|19: clickOnGrid(position)| MapEditorScreen
    MapEditorScreen -->|20: placeTile(position, selectedTileType)| MapEditorController
    MapEditorController -->|21: setTileAt(position, selectedTileType)| Map
    Map -->|22: new Tile(position, selectedTileType)| Tile
    Tile -.->|23: newTile| Map
    Map -->|24: replaceTileAt(position, newTile)| Map
    
    MapEditorController -->|25: updateGridView()| MapEditorScreen
    MapEditorScreen -.->|26: updated map visualization| Player
    
    MapEditorController -->|27: updatePath()| Path
    Path -->|28: recalculateConnectivity()| Path
    
    Player -->|29: clickSaveMap()| MapEditorScreen
    MapEditorScreen -->|30: saveMap()| MapEditorController
    
    MapEditorController -->|31: promptMapName()| Player
    Player -->|32: provideMapName(name)| MapEditorController
    
    MapEditorController -->|33: validateMap(map)| ValidationService
    ValidationService -->|34: checkStartPoint()| ValidationService
    ValidationService -->|35: checkEndPoint()| ValidationService
    ValidationService -->|36: checkPathConnectivity()| ValidationService
    ValidationService -->|37: checkTowerSlots()| ValidationService
    ValidationService -.->|38: validationResult| MapEditorController
    
    MapEditorController -->|39a: setName(name)| Map
    MapEditorController -->|40a: saveMap(map)| MapSerializer
    MapSerializer -->|41a: serializeMap()| MapSerializer
    MapSerializer -.->|42a: saveSuccess| MapEditorController
    MapEditorController -.->|43a: showSaveSuccessMessage()| Player
    
    MapEditorController -.->|39b: showValidationErrors(validationResult)| Player
``` 