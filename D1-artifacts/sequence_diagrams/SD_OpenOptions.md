# Sequence Diagram: Open Options

```mermaid
sequenceDiagram
    actor Player
    participant MainMenuScreen
    participant OptionsController
    participant OptionsScreen
    participant OptionsSerializer
    participant GameOptions
    participant ValidationService

    Player->>MainMenuScreen: clickOptions()
    MainMenuScreen->>OptionsController: openOptions()
    
    OptionsController->>OptionsSerializer: loadOptions()
    alt options file exists
        OptionsSerializer->>OptionsSerializer: deserializeOptions()
        OptionsSerializer-->>OptionsController: savedOptions
    else no options file
        OptionsController->>GameOptions: createDefaultOptions()
        GameOptions-->>OptionsController: defaultOptions
    end
    
    OptionsController->>OptionsScreen: initialize(options)
    OptionsScreen->>OptionsScreen: createEnemyOptionsPanel()
    OptionsScreen->>OptionsScreen: createTowerOptionsPanel()
    OptionsScreen->>OptionsScreen: createEconomyOptionsPanel()
    OptionsScreen->>OptionsScreen: createWaveOptionsPanel()
    OptionsScreen->>OptionsScreen: createGameplayOptionsPanel()
    OptionsScreen-->>Player: display options screen
    
    loop until save or cancel
        Player->>OptionsScreen: modifyOption(category, name, value)
        OptionsScreen->>OptionsController: updateOption(category, name, value)
        OptionsController->>ValidationService: validateValue(category, name, value)
        
        alt value is valid
            ValidationService-->>OptionsController: valid
            OptionsController->>GameOptions: setValue(category, name, value)
            OptionsController-->>OptionsScreen: updateSuccessful
        else value is invalid
            ValidationService-->>OptionsController: invalid, reason
            OptionsController-->>OptionsScreen: showValidationError(reason)
            OptionsScreen-->>Player: display error message
        end
    end
    
    alt Player clicks Save
        Player->>OptionsScreen: clickSave()
        OptionsScreen->>OptionsController: saveOptions()
        OptionsController->>GameOptions: getAllOptions()
        GameOptions-->>OptionsController: currentOptions
        OptionsController->>OptionsSerializer: saveOptions(currentOptions)
        OptionsSerializer->>OptionsSerializer: serializeOptions()
        OptionsSerializer-->>OptionsController: saveSuccess
        OptionsController-->>OptionsScreen: showSaveConfirmation()
        OptionsScreen-->>Player: display save confirmation
        OptionsController->>MainMenuScreen: returnToMainMenu()
    else Player clicks Reset to Default
        Player->>OptionsScreen: clickResetToDefault()
        OptionsScreen->>OptionsController: resetToDefault()
        OptionsController->>GameOptions: createDefaultOptions()
        GameOptions-->>OptionsController: defaultOptions
        OptionsController->>OptionsScreen: resetAllFields(defaultOptions)
        OptionsScreen-->>Player: display default values
    else Player clicks Cancel
        Player->>OptionsScreen: clickCancel()
        OptionsScreen->>OptionsController: cancelChanges()
        OptionsController->>MainMenuScreen: returnToMainMenu()
    end
``` 