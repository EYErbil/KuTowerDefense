# Sequence Diagram: Quit Game

```mermaid
sequenceDiagram
    actor Player
    participant MainMenuScreen
    participant GameScreen
    participant MapEditorScreen
    participant GameController
    participant GameSession
    participant MapEditorController
    participant SystemResourceManager
    participant Application

    alt from main menu
        Player->>MainMenuScreen: clickQuitGame()
        activate MainMenuScreen
        MainMenuScreen->>Application: requestExit()
        activate Application
        Application->>SystemResourceManager: releaseResources()
        activate SystemResourceManager
        SystemResourceManager-->>Application: resourcesReleased
        deactivate SystemResourceManager
        Application-->>Player: closeApplication()
        deactivate MainMenuScreen
    else from active game
        Player->>GameScreen: clickQuitGame()
        activate GameScreen
        GameScreen->>GameController: requestQuit()
        activate GameController
        
        alt game has unsaved progress
            GameController->>GameSession: hasUnsavedProgress()
            activate GameSession
            GameSession-->>GameController: true
            deactivate GameSession
            GameController->>GameScreen: showConfirmationDialog()
            GameScreen-->>Player: display confirmation dialog
            
            alt Player confirms quit
                Player->>GameScreen: confirmQuit()
                GameScreen->>GameController: confirmQuit()
                GameController->>GameSession: endGame()
                activate GameSession
                GameSession->>GameSession: cleanup()
                GameSession-->>GameController: gameEnded
                deactivate GameSession
                GameController->>MainMenuScreen: returnToMainMenu()
                activate MainMenuScreen
                MainMenuScreen-->>Player: display main menu
                deactivate MainMenuScreen
            else Player cancels quit
                Player->>GameScreen: cancelQuit()
                GameScreen->>GameController: cancelQuit()
                GameController-->>GameScreen: resumeGame()
                deactivate GameController
            end
        else no unsaved progress
            GameController->>GameSession: endGame()
            activate GameSession
            GameSession->>GameSession: cleanup()
            GameSession-->>GameController: gameEnded
            deactivate GameSession
            GameController->>MainMenuScreen: returnToMainMenu()
            activate MainMenuScreen
            MainMenuScreen-->>Player: display main menu
            deactivate MainMenuScreen 
        end
    else from map editor
        Player->>MapEditorScreen: clickQuit()
        activate MapEditorScreen
        MapEditorScreen->>MapEditorController: requestQuit()
        activate MapEditorController
        
        alt map has unsaved changes
            MapEditorController->>MapEditorController: hasUnsavedChanges()
            MapEditorController-->>MapEditorScreen: true
            MapEditorScreen->>Player: showSaveChangesDialog()
            
            alt Player chooses Save
                Player->>MapEditorScreen: selectSave()
                MapEditorScreen->>MapEditorController: saveMap()
                MapEditorController-->>MapEditorScreen: mapSaved
                MapEditorController->>MainMenuScreen: returnToMainMenu()
                activate MainMenuScreen
                MainMenuScreen-->>Player: display main menu
                deactivate MainMenuScreen
            else Player chooses Discard
                Player->>MapEditorScreen: selectDiscard()
                MapEditorScreen->>MapEditorController: discardChanges()
                MapEditorController->>MainMenuScreen: returnToMainMenu()
                activate MainMenuScreen
                MainMenuScreen-->>Player: display main menu
                deactivate MainMenuScreen
            else Player chooses Cancel
                Player->>MapEditorScreen: selectCancel()
                MapEditorScreen->>MapEditorController: cancelQuit()
            end
        else no unsaved changes
            MapEditorController->>MainMenuScreen: returnToMainMenu()
            activate MainMenuScreen
            MainMenuScreen-->>Player: display main menu
            deactivate MainMenuScreen
        end
    else window close button
        Player->>Application: clickCloseWindow()
        activate Application
        Application->>Application: handleWindowClosing()
        
        alt active game or map editor open
            Application->>GameController: isGameActive()
            activate GameController
            alt game active
                GameController-->>Application: true
                Application->>GameController: requestQuit()
                GameController->>GameScreen: showConfirmationDialog()
                activate GameScreen
                GameScreen-->>Player: display confirmation dialog
                
                alt Player confirms quit
                    Player->>GameScreen: confirmQuit()
                    GameScreen->>GameController: confirmQuit()
                    GameController->>SystemResourceManager: releaseResources()
                    activate SystemResourceManager
                    SystemResourceManager-->>Application: resourcesReleased
                    deactivate SystemResourceManager
                    Application-->>Player: closeApplication()
                else Player cancels quit
                    Player->>GameScreen: cancelQuit()
                    GameScreen->>GameController: cancelQuit()
                end
            else map editor active
                GameController-->>Application: false
                deactivate GameController
                Application->>MapEditorController: hasUnsavedChanges()
                activate MapEditorController
                MapEditorController-->>Application: hasChanges
                
                alt has unsaved changes
                    Application->>MapEditorScreen: showSaveChangesDialog()
                    activate MapEditorScreen
                    MapEditorScreen-->>Player: display save dialog
                    
                    alt Player chooses Save
                        Player->>MapEditorScreen: selectSave()
                        MapEditorScreen->>MapEditorController: saveMap()
                        MapEditorController-->>Application: mapSaved
                        Application->>SystemResourceManager: releaseResources()
                        activate SystemResourceManager
                        SystemResourceManager-->>Application: resourcesReleased
                        deactivate SystemResourceManager
                        Application-->>Player: closeApplication()
                    else Player chooses Discard
                        Player->>MapEditorScreen: selectDiscard()
                        Application->>SystemResourceManager: releaseResources()
                        activate SystemResourceManager
                        SystemResourceManager-->>Application: resourcesReleased
                        deactivate SystemResourceManager
                        Application-->>Player: closeApplication()
                    else Player chooses Cancel
                        Player->>MapEditorScreen: selectCancel()
                    end
                else no unsaved changes
                    Application->>SystemResourceManager: releaseResources()
                    activate SystemResourceManager
                    SystemResourceManager-->>Application: resourcesReleased
                    deactivate SystemResourceManager
                    Application-->>Player: closeApplication()
                end
            end
        else in main menu
            Application->>SystemResourceManager: releaseResources()
            activate SystemResourceManager
            SystemResourceManager-->>Application: resourcesReleased
            deactivate SystemResourceManager
            Application-->>Player: closeApplication()
        end
    end
``` 