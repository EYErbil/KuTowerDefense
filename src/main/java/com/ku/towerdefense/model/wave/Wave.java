package com.ku.towerdefense.model.wave;

import com.ku.towerdefense.model.entity.Enemy;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

public class Wave {
    private int waveNumber;
    private List<EnemySpawnDetail> enemySpawns;
    private int totalEnemies; // For tracking completion

    public Wave(int waveNumber) {
        this.waveNumber = waveNumber;
        this.enemySpawns = new ArrayList<>();
        this.totalEnemies = 0;
    }

    public void addEnemySpawn(Supplier<Enemy> enemySupplier, int count, double intervalSeconds, double initialDelaySeconds) {
        this.enemySpawns.add(new EnemySpawnDetail(enemySupplier, count, intervalSeconds, initialDelaySeconds));
        this.totalEnemies += count;
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public List<EnemySpawnDetail> getEnemySpawns() {
        return enemySpawns;
    }

    public int getTotalEnemies() {
        return totalEnemies;
    }

    // Inner class to define details for spawning a group of a specific enemy type
    public static class EnemySpawnDetail {
        private Supplier<Enemy> enemySupplier; // e.g., () -> new Goblin(0,0)
        private int count; // Number of enemies of this type to spawn
        private double intervalSeconds; // Time between spawns of this enemy type
        private double initialDelaySeconds; // Delay before this group starts spawning after wave begins

        public EnemySpawnDetail(Supplier<Enemy> enemySupplier, int count, double intervalSeconds, double initialDelaySeconds) {
            this.enemySupplier = enemySupplier;
            this.count = count;
            this.intervalSeconds = intervalSeconds;
            this.initialDelaySeconds = initialDelaySeconds;
        }

        public Supplier<Enemy> getEnemySupplier() {
            return enemySupplier;
        }

        public int getCount() {
            return count;
        }

        public double getIntervalSeconds() {
            return intervalSeconds;
        }

        public double getInitialDelaySeconds() {
            return initialDelaySeconds;
        }
    }
} 