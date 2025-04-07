# Communication Diagram: Open Options

```mermaid
graph TD
    Player([Player])
    MainMenuScreen[MainMenuScreen]
    OptionsController[OptionsController]
    OptionsScreen[OptionsScreen]
    OptionsSerializer[OptionsSerializer]
    GameOptions[GameOptions]
    ValidationService[ValidationService]

    Player -->|1: clickOptions()| MainMenuScreen
    MainMenuScreen -->|2: openOptions()| OptionsController
    
    OptionsController -->|3: loadOptions()| OptionsSerializer
    OptionsSerializer -->|4a: deserializeOptions()| OptionsSerializer
    OptionsSerializer -.->|5a: savedOptions| OptionsController
    
    OptionsController -->|4b: createDefaultOptions()| GameOptions
    GameOptions -.->|5b: defaultOptions| OptionsController
    
    OptionsController -->|6: initialize(options)| OptionsScreen
    OptionsScreen -->|7: createEnemyOptionsPanel()| OptionsScreen
    OptionsScreen -->|8: createTowerOptionsPanel()| OptionsScreen
    OptionsScreen -->|9: createEconomyOptionsPanel()| OptionsScreen
    OptionsScreen -->|10: createWaveOptionsPanel()| OptionsScreen
    OptionsScreen -->|11: createGameplayOptionsPanel()| OptionsScreen
    OptionsScreen -.->|12: display options screen| Player
    
    Player -->|13: modifyOption(category, name, value)| OptionsScreen
    OptionsScreen -->|14: updateOption(category, name, value)| OptionsController
    OptionsController -->|15: validateValue(category, name, value)| ValidationService
    
    ValidationService -.->|16a: valid| OptionsController
    OptionsController -->|17a: setValue(category, name, value)| GameOptions
    OptionsController -.->|18a: updateSuccessful| OptionsScreen
    
    ValidationService -.->|16b: invalid, reason| OptionsController
    OptionsController -.->|17b: showValidationError(reason)| OptionsScreen
    OptionsScreen -.->|18b: display error message| Player
    
    Player -->|19a: clickSave()| OptionsScreen
    OptionsScreen -->|20a: saveOptions()| OptionsController
    OptionsController -->|21a: getAllOptions()| GameOptions
    GameOptions -.->|22a: currentOptions| OptionsController
    OptionsController -->|23a: saveOptions(currentOptions)| OptionsSerializer
    OptionsSerializer -->|24a: serializeOptions()| OptionsSerializer
    OptionsSerializer -.->|25a: saveSuccess| OptionsController
    OptionsController -.->|26a: showSaveConfirmation()| OptionsScreen
    OptionsScreen -.->|27a: display save confirmation| Player
    OptionsController -->|28a: returnToMainMenu()| MainMenuScreen
    
    Player -->|19b: clickResetToDefault()| OptionsScreen
    OptionsScreen -->|20b: resetToDefault()| OptionsController
    OptionsController -->|21b: createDefaultOptions()| GameOptions
    GameOptions -.->|22b: defaultOptions| OptionsController
    OptionsController -->|23b: resetAllFields(defaultOptions)| OptionsScreen
    OptionsScreen -.->|24b: display default values| Player
    
    Player -->|19c: clickCancel()| OptionsScreen
    OptionsScreen -->|20c: cancelChanges()| OptionsController
    OptionsController -->|21c: returnToMainMenu()| MainMenuScreen
``` 