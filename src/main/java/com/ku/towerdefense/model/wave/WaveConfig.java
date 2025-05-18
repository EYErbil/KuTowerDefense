package com.ku.towerdefense.model.wave;

import com.ku.towerdefense.model.entity.Goblin;
import com.ku.towerdefense.model.entity.Knight;
import java.util.ArrayList;
import java.util.List;

public class WaveConfig {
    private static List<Wave> waves = new ArrayList<>();

    static {
        // Define waves programmatically for now. Could be loaded from a file.

        // Wave 1: Easy Goblins
        Wave wave1 = new Wave(1);
        wave1.addEnemySpawn(() -> new Goblin(0, 0), 5, 1.5, 0.5); // 5 Goblins, 1.5s interval, 0.5s initial delay
        waves.add(wave1);

        // Wave 2: More Goblins, slightly faster
        Wave wave2 = new Wave(2);
        wave2.addEnemySpawn(() -> new Goblin(0, 0), 8, 1.0, 0.5);
        waves.add(wave2);

        // Wave 3: Introduce Knights
        Wave wave3 = new Wave(3);
        wave3.addEnemySpawn(() -> new Goblin(0, 0), 5, 1.2, 0.5);
        wave3.addEnemySpawn(() -> new Knight(0, 0), 2, 3.0, 2.0); // 2 Knights, 3s interval, 2s delay after Goblins start
        waves.add(wave3);

        // Wave 4: Mixed Goblins and Knights
        Wave wave4 = new Wave(4);
        wave4.addEnemySpawn(() -> new Goblin(0, 0), 10, 0.8, 0.5);
        wave4.addEnemySpawn(() -> new Knight(0, 0), 4, 2.5, 1.0);
        waves.add(wave4);
        
        // Wave 5: Tougher wave
        Wave wave5 = new Wave(5);
        wave5.addEnemySpawn(() -> new Knight(0, 0), 6, 2.0, 0.5);
        wave5.addEnemySpawn(() -> new Goblin(0, 0), 10, 0.5, 3.0); // Goblins come after initial Knights
        waves.add(wave5);
        // Add more waves as needed
    }

    public static Wave getWave(int waveNumber) {
        if (waveNumber > 0 && waveNumber <= waves.size()) {
            return waves.get(waveNumber - 1); // 0-indexed list
        }
        return null; // Or throw exception / return an empty wave
    }

    public static int getTotalWaves() {
        return waves.size();
    }
} 