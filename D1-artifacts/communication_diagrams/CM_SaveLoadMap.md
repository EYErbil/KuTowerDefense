# Communication Diagram: Save and Load Map

```mermaid
graph TD
    Player([Player])
    MapEditorScreen[MapEditorScreen]
    MapEditorController[MapEditorController]
    ValidationService[ValidationService]
    Map[Map]
    Path[Path]
    MapSerializer[MapSerializer]
    FileManager[FileManager]
    GameController[GameController]
    MainMenuScreen[MainMenuScreen]

    Player -->|1a: clickSaveButton()| MapEditorScreen
    MapEditorScreen -->|2a: saveMap()| MapEditorController
    
    MapEditorController -->|3a: promptForMapName()| Player
    Player -->|4a: provideMapName(name)| MapEditorController
    MapEditorController -->|5a: setName(name)| Map
    
    MapEditorController -->|6a: validateMap(map)| ValidationService
    ValidationService -->|7a: checkStartPoint()| ValidationService
    ValidationService -->|8a: checkEndPoint()| ValidationService
    ValidationService -->|9a: isConnected()| Path
    Path -.->|10a: pathConnectivity| ValidationService
    ValidationService -->|11a: checkTowerSlots()| ValidationService
    ValidationService -.->|12a: validationResult| MapEditorController
    
    MapEditorController -->|13a1: saveMap(map)| MapSerializer
    MapSerializer -->|14a1: serializeMap()| MapSerializer
    MapSerializer -->|15a1: writeFile(fileName, data)| FileManager
    FileManager -.->|16a1: fileWriteResult| MapSerializer
    MapSerializer -.->|17a1: saveSuccess| MapEditorController
    MapEditorController -->|18a1: showSaveSuccessMessage()| MapEditorScreen
    MapEditorScreen -.->|19a1: display success message| Player
    
    MapEditorController -->|13a2: showValidationErrors(validationResult)| MapEditorScreen
    MapEditorScreen -.->|14a2: display validation errors| Player
    
    Player -->|1b: clickEditMap()| MainMenuScreen
    MainMenuScreen -->|2b: editMap()| GameController
    GameController -->|3b: loadMapList()| MapSerializer
    MapSerializer -->|4b: listFiles(mapDirectory)| FileManager
    FileManager -.->|5b: filesList| MapSerializer
    MapSerializer -.->|6b: availableMaps| GameController
    GameController -.->|7b: showMapSelectionDialog(availableMaps)| Player
    
    Player -->|8b: selectMap(mapId)| GameController
    GameController -->|9b: loadMap(mapId)| MapSerializer
    MapSerializer -->|10b: readFile(fileName)| FileManager
    FileManager -.->|11b: fileData| MapSerializer
    MapSerializer -->|12b: deserializeMap(fileData)| MapSerializer
    MapSerializer -.->|13b: loadedMap| GameController
    
    GameController -->|14b: openMapEditor(loadedMap)| MapEditorController
    MapEditorController -->|15b: initialize(loadedMap)| MapEditorScreen
    MapEditorScreen -->|16b: setupTileSelector()| MapEditorScreen
    MapEditorScreen -->|17b: setupMapGrid()| MapEditorScreen
    MapEditorScreen -->|18b: populateGrid(loadedMap)| MapEditorScreen
    MapEditorScreen -.->|19b: display map editor with loaded map| Player
``` 