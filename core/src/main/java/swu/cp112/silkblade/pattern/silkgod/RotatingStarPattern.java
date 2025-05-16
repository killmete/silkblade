package swu.cp112.silkblade.pattern.silkgod;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;

/**
 * Rotating Spear Circle attack pattern - bullets spawn in a circle around the player,
 * rotate for a bit, and then curve toward the player's position before disappearing.
 */
public class RotatingStarPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        3, 6, 1.75f, 0.75f, 12, 790f, 400f, Color.CYAN, false,
        "Stars spawning around, briefly rotating before locks on position."
    );

    private float rotationAngle = 0f;
    private boolean clockwise = true;  // Controls rotation direction
    private static final float DESTROY_DISTANCE = 10f; // Distance to target when bullet should be destroyed

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                        float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Get player position for bullet targeting
        float targetX = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerX() :
            arenaX + arenaWidth / 2;
        float targetY = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerY() :
            arenaY + arenaHeight / 2;

        // Generate multiple bullets
        int bulletCount = 5;  // 7 spears for the circle
        float baseSpeed = 250;  // Speed for rotation phase
        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.15f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float damage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));
        float bulletSize = 16;  // Slightly bigger for spear-like appearance

        // Circle pattern - spawn bullets around player
        float radius = 125f; // Distance from player to spawn the bullets
        float rotationTime = 0.6f;  // Extended time to rotate before attacking

        // Toggle rotation direction each time
        clockwise = !clockwise;
        float rotationDir = clockwise ? 1f : -1f;

        // Angular velocity (degrees per second)
        float angularSpeed = 120f * rotationDir;  // Degrees per second

        for (int i = 0; i < bulletCount; i++) {
            // Calculate position in a circle around the player
            float angleInRadians = ((i * 360f / bulletCount) + rotationAngle) * MathUtils.degreesToRadians;
            float bulletX = targetX + radius * MathUtils.cos(angleInRadians);
            float bulletY = targetY + radius * MathUtils.sin(angleInRadians);

            // Initial tangential velocity (perpendicular to radius)
            float tangentVelX = -MathUtils.sin(angleInRadians) * baseSpeed;
            float tangentVelY = MathUtils.cos(angleInRadians) * baseSpeed;

            // Create bullet with initial tangential velocity
            Bullet bullet = new Bullet(
                damage,
                bulletX,
                bulletY,
                tangentVelX * rotationDir,  // Apply rotation direction
                tangentVelY * rotationDir,  // Apply rotation direction
                bulletSize,
                CONFIG.getBulletColor(),
                CONFIG.isHealing()
            );

            // Store the center and initial angle for circular motion
            final float centerX = targetX;
            final float centerY = targetY;
            final float initialAngle = angleInRadians;
            final float[] currentAngle = {initialAngle}; // Using array to make it modifiable in lambda

            // Configure cyan-ish color effect for Undertale feel
            bullet.setDisco(true, false, false, 0.5f, 0.8f, 1.0f);
            bullet.setDiscoSpeed(1.5f);
            bullet.setTrailLength(60);
            // Track elapsed time for phase transitions
            final float[] elapsedTime = {0f};
            final boolean[] homingStarted = {false};

            // Set custom update callback for circular motion and transition to homing
            bullet.setUpdateCallback(delta -> {
                elapsedTime[0] += delta;

                // Check if bullet should be destroyed (reached target during homing phase)
                if (homingStarted[0]) {
                    float dx = bullet.getX() - centerX;
                    float dy = bullet.getY() - centerY;
                    float distToTarget = (float) Math.sqrt(dx * dx + dy * dy);

                    if (distToTarget <= DESTROY_DISTANCE) {
                        bullet.startFading(0.3f);
                        return false; // Stop callback execution
                    }
                }

                // Circular motion phase
                if (elapsedTime[0] < rotationTime) {
                    // Update angle based on angular speed
                    currentAngle[0] += angularSpeed * delta * MathUtils.degreesToRadians;

                    // Calculate new position on the circle
                    float newX = centerX + radius * MathUtils.cos(currentAngle[0]);
                    float newY = centerY + radius * MathUtils.sin(currentAngle[0]);

                    // Update bullet position
                    bullet.x = newX;
                    bullet.y = newY;

                    // Update velocity to be tangential to the circle
                    float tangentX = -MathUtils.sin(currentAngle[0]) * baseSpeed * rotationDir;
                    float tangentY = MathUtils.cos(currentAngle[0]) * baseSpeed * rotationDir;
                    bullet.setVelocity(tangentX, tangentY);

                    return true; // Continue custom movement
                }
                // Transition to homing phase
                else if (!homingStarted[0]) {
                    homingStarted[0] = true;
                    bullet.enableHoming(0.85f, 2800f);
                    bullet.updateTarget(centerX, centerY);
                    return true; // Continue with homing behavior
                }

                return true; // Keep executing the callback
            });
            bullet.setShape(Bullet.Shape.STAR);
            bullet.setSpinDirectionMatchesMovement(true);
            bullet.setGlowing(true);  // Enable glow
            bullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
            bullet.setGlowIntensity(0.3f);
            bullets.add(bullet);
        }

        // Increment the rotation angle for next spawn
        rotationAngle = (rotationAngle + 45f) % 360f;

        return bullets;
    }

    @Override
    public String getPatternName() {
        return "Stellar Vortex";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
