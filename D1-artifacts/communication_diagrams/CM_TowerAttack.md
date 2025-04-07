# Communication Diagram: Tower Attack

```mermaid
graph TD
    GameClock[GameClock]
    GameSession[GameSession]
    Tower[Tower]
    Enemy[Enemy]
    Projectile[Projectile]
    ArtilleryShell[ArtilleryShell]
    ProjectileAnimation[ProjectileAnimation]
    GameScreen[GameScreen]

    GameClock -->|1: tick()| GameSession
    GameSession -->|2: updateGameState()| GameSession
    
    GameSession -->|3: update(deltaTime)| Tower
    Tower -->|4: decrementCooldown(deltaTime)| Tower
    
    Tower -->|5: findTarget()| Tower
    Tower -->|6: getPosition()| Enemy
    Enemy -.->|7: enemyPosition| Tower
    Tower -->|8: calculateDistance(enemyPosition)| Tower
    Tower -->|9: setTarget(enemy)| Tower
    
    Tower -->|10: resetCooldown()| Tower
    Tower -->|11: createProjectile(target)| Projectile
    Projectile -->|12: initializeProperties()| Projectile
    Projectile -.->|13: newProjectile| Tower
    
    Tower -->|14: addProjectile(newProjectile)| GameSession
    GameSession -->|15: createAnimation(newProjectile)| ProjectileAnimation
    ProjectileAnimation -.->|16: visualize projectile launch| GameScreen
    
    GameSession -->|17: update(deltaTime)| Projectile
    Projectile -->|18: moveTowardTarget(deltaTime)| Projectile
    
    Projectile -->|19: takeDamage(damage)| Enemy
    
    Projectile -->|20a: applyAreaDamage(position, radius)| GameSession
    GameSession -->|21: takeDamage(areaDamage)| Enemy
    Enemy -->|22: hitPoints -= damage| Enemy
    
    Enemy -->|23: markAsDefeated()| Enemy
    Enemy -->|24: notifyDefeated()| GameSession
    GameSession -->|25: removeEnemy(enemy)| GameSession
    GameSession -->|26: rewardPlayer(enemy.getValue())| GameSession
    
    Projectile -->|27: playExplosionAnimation()| ProjectileAnimation
    ProjectileAnimation -.->|28: visualize explosion| GameScreen
    
    Projectile -->|20b: hitPoints -= damage| Enemy
    
    Projectile -->|29: notifyHit()| GameSession
    GameSession -->|30: removeProjectile(projectile)| GameSession
``` 