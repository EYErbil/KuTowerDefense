# Sequence Diagram: Wave Generation

```mermaid
sequenceDiagram
    participant GameController
    participant GameSession
    participant GameOptions
    participant WaveFactory
    participant Wave
    participant Group
    participant GameClock
    participant GameScreen
    participant Player

    GameController->>GameSession: startNewGame(map)
    GameSession->>GameOptions: getWaveSettings()
    GameOptions-->>GameSession: waveSettings
    
    GameSession->>WaveFactory: createWaves(waveSettings)
    WaveFactory->>WaveFactory: determineWaveCount()
    
    loop for each wave
        WaveFactory->>WaveFactory: createWaveConfiguration(waveNum)
        WaveFactory->>Wave: new Wave(waveNum, config)
        
        Wave->>Wave: determineGroupCount()
        
        loop for each group in wave
            Wave->>Wave: determineGroupComposition(groupNum)
            Wave->>Group: new Group(groupNum, composition)
            Group->>Group: initializeEnemyTypes()
            Group-->>Wave: configuredGroup
            Wave->>Wave: addGroup(configuredGroup)
        end
        
        Wave-->>WaveFactory: configuredWave
        WaveFactory->>WaveFactory: addWave(configuredWave)
    end
    
    WaveFactory-->>GameSession: wavesList
    GameSession->>GameSession: setWaves(wavesList)
    GameSession->>GameSession: setGracePeriod(4 seconds)
    
    GameSession->>GameScreen: updateWaveIndicator(1, totalWaves)
    GameScreen-->>Player: display wave indicator
    
    GameClock->>GameSession: tick(deltaTime)
    GameSession->>GameSession: updateGracePeriod(deltaTime)
    
    alt grace period over
        GameSession->>GameSession: startFirstWave()
        GameSession->>Wave: activate()
        Wave->>Wave: setActive(true)
        Wave->>GameScreen: showWaveStartBanner(waveNum)
        GameScreen-->>Player: display wave start
    end
    
    loop game continues
        GameClock->>GameSession: tick(deltaTime)
        GameSession->>Wave: update(deltaTime)
        Wave->>Wave: updateWaveTimer(deltaTime)
        
        alt current wave complete
            Wave->>GameSession: notifyWaveComplete()
            GameSession->>GameSession: waveCompleted()
            
            alt more waves exist
                GameSession->>GameSession: prepareNextWave()
                GameSession->>GameSession: startWaveTimer(waveDelay)
                
                alt wave timer expired
                    GameSession->>GameSession: startNextWave()
                    GameSession->>Wave: activate()
                    Wave->>Wave: setActive(true)
                    
                    GameSession->>GameScreen: updateWaveIndicator(currentWave, totalWaves)
                    GameScreen->>GameScreen: showWaveStartBanner(waveNum)
                    GameScreen-->>Player: display wave start
                end
            else all waves complete
                GameSession->>GameController: notifyAllWavesDefeated()
                GameController->>GameScreen: showVictoryScreen()
                GameScreen-->>Player: display victory
            end
        end
    end
``` 