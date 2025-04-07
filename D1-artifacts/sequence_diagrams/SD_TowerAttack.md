# Sequence Diagram: Tower Attack

```mermaid
sequenceDiagram
    participant GameClock
    participant GameSession
    participant Tower
    participant Enemy
    participant Projectile
    participant ProjectileAnimation
    participant GameScreen

    GameClock->>GameSession: tick()
    GameSession->>GameSession: updateGameState()
    
    loop for each active Tower
        GameSession->>Tower: update(deltaTime)
        Tower->>Tower: decrementCooldown(deltaTime)
        
        alt cooldown <= 0
            Tower->>Tower: findTarget()
            
            loop for each Enemy
                Tower->>Enemy: getPosition()
                Enemy-->>Tower: enemyPosition
                Tower->>Tower: calculateDistance(enemyPosition)
                
                alt enemy in range && farthest along path
                    Tower->>Tower: setTarget(enemy)
                end
            end
            
            alt hasTarget
                Tower->>Tower: resetCooldown()
                Tower->>Projectile: createProjectile(target)
                Projectile->>Projectile: initializeProperties()
                Projectile-->>Tower: newProjectile
                
                Tower->>GameSession: addProjectile(newProjectile)
                GameSession->>ProjectileAnimation: createAnimation(newProjectile)
                ProjectileAnimation-->>GameScreen: visualize projectile launch
            end
        end
    end
    
    loop for each active Projectile
        GameSession->>Projectile: update(deltaTime)
        Projectile->>Projectile: moveTowardTarget(deltaTime)
        
        alt reached target
            Projectile->>Enemy: takeDamage(damage)
            
            alt projectile is ArtilleryShell
                Projectile->>GameSession: applyAreaDamage(position, radius)
                
                loop for each Enemy in radius
                    GameSession->>Enemy: takeDamage(areaDamage)
                    Enemy->>Enemy: hitPoints -= damage
                    
                    alt hitPoints <= 0
                        Enemy->>Enemy: markAsDefeated()
                        Enemy->>GameSession: notifyDefeated()
                        GameSession->>GameSession: removeEnemy(enemy)
                        GameSession->>GameSession: rewardPlayer(enemy.getValue())
                    end
                end
                
                Projectile->>ProjectileAnimation: playExplosionAnimation()
                ProjectileAnimation-->>GameScreen: visualize explosion
            else standard projectile
                Enemy->>Enemy: hitPoints -= damage
                
                alt hitPoints <= 0
                    Enemy->>Enemy: markAsDefeated()
                    Enemy->>GameSession: notifyDefeated()
                    GameSession->>GameSession: removeEnemy(enemy)
                    GameSession->>GameSession: rewardPlayer(enemy.getValue())
                end
            end
            
            Projectile->>GameSession: notifyHit()
            GameSession->>GameSession: removeProjectile(projectile)
        end
    end
``` 