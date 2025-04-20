# Sequence Diagram: Save and Load Map

```mermaid
sequenceDiagram
    actor Player
    participant MapEditorScreen
    participant MapEditorController
    participant ValidationService
    participant Map
    participant Path
    participant MapSerializer
    participant FileManager
    participant GameController
    participant MainMenuScreen

    alt Save Map
        Player->>MapEditorScreen: clickSaveButton()
        activate MapEditorScreen
        MapEditorScreen->>MapEditorController: saveMap()
        activate MapEditorController
        
        alt new map
            MapEditorController->>Player: promptForMapName()
            Player->>MapEditorController: provideMapName(name)
            MapEditorController->>Map: setName(name)
            activate Map
            Map-->>MapEditorController: nameSet
            deactivate Map
        end
        
        MapEditorController->>ValidationService: validateMap(map)
        activate ValidationService
        ValidationService->>ValidationService: checkStartPoint()
        ValidationService->>ValidationService: checkEndPoint()
        ValidationService->>Path: isConnected()
        activate Path
        Path-->>ValidationService: pathConnectivity
        deactivate Path
        ValidationService->>ValidationService: checkTowerSlots()
        ValidationService-->>MapEditorController: validationResult
        deactivate ValidationService
        
        alt map is valid
            MapEditorController->>MapSerializer: saveMap(map)
            activate MapSerializer
            MapSerializer->>MapSerializer: serializeMap()
            MapSerializer->>FileManager: writeFile(fileName, data)
            activate FileManager
            FileManager-->>MapSerializer: fileWriteResult
            deactivate FileManager
            MapSerializer-->>MapEditorController: saveSuccess
            deactivate MapSerializer
            MapEditorController->>MapEditorScreen: showSaveSuccessMessage()
            MapEditorScreen-->>Player: display success message
        else map is invalid
            MapEditorController->>MapEditorScreen: showValidationErrors(validationResult)
            MapEditorScreen-->>Player: display validation errors
        end
        deactivate MapEditorController
        deactivate MapEditorScreen
    else Load Map
        Player->>MainMenuScreen: clickEditMap()
        activate MainMenuScreen
        MainMenuScreen->>GameController: editMap()
        activate GameController
        GameController->>MapSerializer: loadMapList()
        activate MapSerializer
        MapSerializer->>FileManager: listFiles(mapDirectory)
        activate FileManager
        FileManager-->>MapSerializer: filesList
        deactivate FileManager
        MapSerializer-->>GameController: availableMaps
        deactivate MapSerializer
        GameController-->>Player: showMapSelectionDialog(availableMaps)
        
        Player->>GameController: selectMap(mapId)
        GameController->>MapSerializer: loadMap(mapId)
        activate MapSerializer
        MapSerializer->>FileManager: readFile(fileName)
        activate FileManager
        FileManager-->>MapSerializer: fileData
        deactivate FileManager
        MapSerializer->>MapSerializer: deserializeMap(fileData)
        MapSerializer-->>GameController: loadedMap
        deactivate MapSerializer
        
        GameController->>MapEditorController: openMapEditor(loadedMap)
        activate MapEditorController
        MapEditorController->>MapEditorScreen: initialize(loadedMap)
        activate MapEditorScreen
        MapEditorScreen->>MapEditorScreen: setupTileSelector()
        MapEditorScreen->>MapEditorScreen: setupMapGrid()
        MapEditorScreen->>MapEditorScreen: populateGrid(loadedMap)
        MapEditorScreen-->>Player: display map editor with loaded map
        deactivate MapEditorScreen
        deactivate MapEditorController
        deactivate GameController
        deactivate MainMenuScreen
    end
``` 