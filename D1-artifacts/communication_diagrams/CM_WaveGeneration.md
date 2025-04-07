# Communication Diagram: Wave Generation

```mermaid
graph TD
    GameController[GameController]
    GameSession[GameSession]
    GameOptions[GameOptions]
    WaveFactory[WaveFactory]
    Wave[Wave]
    Group[Group]
    GameClock[GameClock]
    GameScreen[GameScreen]
    Player([Player])

    GameController -->|1: startNewGame(map)| GameSession
    GameSession -->|2: getWaveSettings()| GameOptions
    GameOptions -.->|3: waveSettings| GameSession
    
    GameSession -->|4: createWaves(waveSettings)| WaveFactory
    WaveFactory -->|5: determineWaveCount()| WaveFactory
    
    GameSession -->|6: createWaveConfiguration(waveNum)| WaveFactory
    WaveFactory -->|7: new Wave(waveNum, config)| Wave
    
    Wave -->|8: determineGroupCount()| Wave
    
    Wave -->|9: determineGroupComposition(groupNum)| Wave
    Wave -->|10: new Group(groupNum, composition)| Group
    Group -->|11: initializeEnemyTypes()| Group
    Group -.->|12: configuredGroup| Wave
    Wave -->|13: addGroup(configuredGroup)| Wave
    
    Wave -.->|14: configuredWave| WaveFactory
    WaveFactory -->|15: addWave(configuredWave)| WaveFactory
    
    WaveFactory -.->|16: wavesList| GameSession
    GameSession -->|17: setWaves(wavesList)| GameSession
    GameSession -->|18: setGracePeriod(4 seconds)| GameSession
    
    GameSession -->|19: updateWaveIndicator(1, totalWaves)| GameScreen
    GameScreen -.->|20: display wave indicator| Player
    
    GameClock -->|21: tick(deltaTime)| GameSession
    GameSession -->|22: updateGracePeriod(deltaTime)| GameSession
    
    GameSession -->|23: startFirstWave()| GameSession
    GameSession -->|24: activate()| Wave
    Wave -->|25: setActive(true)| Wave
    Wave -->|26: showWaveStartBanner(waveNum)| GameScreen
    GameScreen -.->|27: display wave start| Player
    
    GameClock -->|28: tick(deltaTime)| GameSession
    GameSession -->|29: update(deltaTime)| Wave
    Wave -->|30: updateWaveTimer(deltaTime)| Wave
    
    Wave -->|31: notifyWaveComplete()| GameSession
    GameSession -->|32: waveCompleted()| GameSession
    
    GameSession -->|33a: prepareNextWave()| GameSession
    GameSession -->|34a: startWaveTimer(waveDelay)| GameSession
    
    GameSession -->|35a: startNextWave()| GameSession
    GameSession -->|36a: activate()| Wave
    Wave -->|37a: setActive(true)| Wave
    
    GameSession -->|38a: updateWaveIndicator(currentWave, totalWaves)| GameScreen
    GameScreen -->|39a: showWaveStartBanner(waveNum)| GameScreen
    GameScreen -.->|40a: display wave start| Player
    
    GameSession -->|33b: notifyAllWavesDefeated()| GameController
    GameController -->|34b: showVictoryScreen()| GameScreen
    GameScreen -.->|35b: display victory| Player
``` 