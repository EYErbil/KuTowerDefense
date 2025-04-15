# Sequence Diagram: Edit Map

```mermaid
sequenceDiagram
    actor Player
    participant :MainMenuScreen
    participant MapEditorController
    participant MapEditorScreen
    participant MapSerializer
    participant Map
    participant Tile
    participant Path
    participant ValidationService

    Player->>MainMenuScreen: clickEditMap()
    MainMenuScreen->>MapEditorController: openMapEditor()
    
    alt edit existing map
        MapEditorController->>MapSerializer: loadMapList()
        MapSerializer-->>MapEditorController: availableMaps
        MapEditorController-->>Player: showMapSelection(availableMaps)
        Player->>MapEditorController: selectMap(mapId)
        MapEditorController->>MapSerializer: loadMap(mapId)
        MapSerializer-->>MapEditorController: mapData
        MapEditorController->>Map: new Map(mapData)
    else create new map
        Player->>MapEditorController: selectNewMap()
        MapEditorController->>MapEditorController: showSizeDialog()
        Player->>MapEditorController: setMapDimensions(width, height)
        MapEditorController->>Map: new Map(width, height)
    end
    
    Map->>Map: initializeGrid()
    Map-->>MapEditorController: initializedMap
    
    MapEditorController->>MapEditorScreen: initialize(initializedMap)
    MapEditorScreen->>MapEditorScreen: setupTileSelector()
    MapEditorScreen->>MapEditorScreen: setupMapGrid()
    MapEditorScreen->>MapEditorScreen: setupToolPanel()
    MapEditorScreen-->>Player: display editor screen
    
    loop until map is saved or cancelled
        Player->>MapEditorScreen: selectTileType(tileType)
        MapEditorScreen->>MapEditorController: setSelectedTileType(tileType)
        
        Player->>MapEditorScreen: clickOnGrid(position)
        MapEditorScreen->>MapEditorController: placeTile(position, selectedTileType)
        MapEditorController->>Map: setTileAt(position, selectedTileType)
        Map->>Tile: new Tile(position, selectedTileType)
        Tile-->>Map: newTile
        Map->>Map: replaceTileAt(position, newTile)
        
        MapEditorController->>MapEditorScreen: updateGridView()
        MapEditorScreen-->>Player: updated map visualization
        
        alt tile is path
            MapEditorController->>Path: updatePath()
            Path->>Path: recalculateConnectivity()
        end
    end
    
    Player->>MapEditorScreen: clickSaveMap()
    MapEditorScreen->>MapEditorController: saveMap()
    
    MapEditorController->>Player: promptMapName()
    Player->>MapEditorController: provideMapName(name)
    
    MapEditorController->>ValidationService: validateMap(map)
    ValidationService->>ValidationService: checkStartPoint()
    ValidationService->>ValidationService: checkEndPoint()
    ValidationService->>ValidationService: checkPathConnectivity()
    ValidationService->>ValidationService: checkTowerSlots()
    ValidationService-->>MapEditorController: validationResult
    
    alt validationResult is valid
        MapEditorController->>Map: setName(name)
        MapEditorController->>MapSerializer: saveMap(map)
        MapSerializer->>MapSerializer: serializeMap()
        MapSerializer-->>MapEditorController: saveSuccess
        MapEditorController-->>Player: showSaveSuccessMessage()
    else map invalid
        MapEditorController-->>Player: showValidationErrors(validationResult)
    end
``` 
