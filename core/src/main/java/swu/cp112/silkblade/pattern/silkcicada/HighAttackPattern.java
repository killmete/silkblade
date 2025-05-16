package swu.cp112.silkblade.pattern.silkcicada;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.screen.StageSelectionScreen;
import swu.cp112.silkblade.util.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * High-difficulty attack pattern for the Silk Cicada enemy type.
 * Used for stages 27-29, the most challenging Silk Cicada encounters.
 * Features complex bullet patterns with homing, expanding rings, and synchronized waves.
 */
public class HighAttackPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        15, 18, 4.0f, 0.9f, 20, 350f, 350f, new Color(0.1f, 0.8f, 0.2f, 1.0f), true,
        "Cicada's Harmonic Resonance", 2.0f
    );

    // Attack phase trackers
    private int currentPhase = 0;
    private float phaseTimer = 0f;
    private static final float PHASE_DURATION = 4.0f; // Duration of each attack phase
    private static final int TOTAL_PHASES = 3; // Total different attack phases

    // Track overall pattern time
    private float patternTimer = 0f;

    // Circular pattern properties
    private static final int MAX_RING_BULLETS = 18;
    private static final float RING_RADIUS = 100f;
    private static final float EXPANSION_RATE = 80f;

    // Fan pattern properties
    private static final int FAN_BULLETS = 9;
    private static final float FAN_ANGLE = 120f; // Degrees

    // Healing properties
    private static final float HEALING_CHANCE = 0.15f;

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                        float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Update timers
        float delta = Gdx.graphics.getDeltaTime();
        patternTimer += delta;
        phaseTimer += delta;

        // Check for phase transition
        if (currentPhase == TOTAL_PHASES) {
            currentPhase = 0;
        }

        // Get player position for targeting
        float playerX = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerX() :
            arenaX + arenaWidth / 2;
        float playerY = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerY() :
            arenaY + arenaHeight / 2;

        // Get enemy position from the enemy object
        float enemyX = arenaX + arenaWidth / 2;
        float enemyY = (arenaY + arenaHeight / 2) + 75;

        // Calculate attack parameters based on enemy's state
        float damageMultiplier = 1.0f + (enemy.getAttackDamage() * 0.05f);
        float speedMultiplier = 1.0f + 0.08f; // Fixed multiplier since Enemy has no getDefense()
        float baseSpeed = 280f * speedMultiplier;

        // Get scaled damage
        float minDamage = CONFIG.getMinDamage() * damageMultiplier;
        float maxDamage = CONFIG.getMaxDamage() * damageMultiplier;

        // Get enemy color
        Color enemyColor = enemy.getPrimaryColor();

        // Execute current phase
        switch (currentPhase) {
            case 0:
                // Phase 1: Expanding circular rings with homing capabilities
                createExpandingRingPattern(bullets, enemyX, enemyY, playerX, playerY,
                                          baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;

            case 1:
                // Phase 2: Spiral patterns with acceleration
                createSpiralPattern(bullets, enemyX, enemyY, playerX, playerY,
                                   baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;

            case 2:
                // Phase 3: Fan patterns with crossing streams
                createFanPattern(bullets, enemyX, enemyY, playerX, playerY,
                                baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;
        }

        // Occasionally spawn healing bullets (all phases)
        if (MathUtils.random() < HEALING_CHANCE) {
            createHealingBullet(bullets, enemyX, enemyY, playerX, playerY);
        }

        return bullets;
    }

    /**
     * Creates an expanding ring of bullets that gradually transform into homing bullets
     */
    private void createExpandingRingPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                          float playerX, float playerY, float speed,
                                          float minDamage, float maxDamage, Color baseColor) {
        int bulletCount = MAX_RING_BULLETS;
        float damage = MathUtils.random(minDamage, maxDamage);

        // Create a ring of bullets
        for (int i = 0; i < bulletCount; i++) {
            float angle = (float) i / bulletCount * MathUtils.PI2;

            // Calculate initial position (starting close to enemy)
            float startRadius = 30f;
            float spawnX = enemyX + MathUtils.cos(angle) * startRadius;
            float spawnY = enemyY + MathUtils.sin(angle) * startRadius;

            // Calculate initial velocity (outward direction)
            float dirX = MathUtils.cos(angle);
            float dirY = MathUtils.sin(angle);

            // Slightly randomize speed for visual interest
            float bulletSpeed = speed * MathUtils.random(0.85f, 1.15f);

            // Create bullet
            Bullet bullet = new Bullet(
                damage,
                spawnX,
                spawnY,
                dirX * bulletSpeed,
                dirY * bulletSpeed,
                7f, // Size
                new Color(baseColor).lerp(Color.WHITE, 0.2f),
                false // Not healing
            );

            // Apply special behavior: start with outward movement, then transition to homing
            final float[] timeTracker = {0f};
            final float homingTransitionTime = 1.2f; // Time before bullets start homing
            final float homingStrength = 1.5f;

            bullet.setUpdateCallback(delta -> {
                timeTracker[0] += delta;

                // After delay, start homing behavior
                if (timeTracker[0] >= homingTransitionTime) {
                    if (!bullet.isHoming()) {
                        bullet.enableHoming(2.0f, homingStrength);
                        bullet.updateTarget(playerX, playerY);
                    }
                }

                return true; // Continue the callback
            });

            // Apply visual styling
            styleBullet(bullet, true);

            bullets.add(bullet);
        }
    }

    /**
     * Creates a spiral pattern of bullets that accelerate outward
     */
    private void createSpiralPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                   float playerX, float playerY, float speed,
                                   float minDamage, float maxDamage, Color baseColor) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Create three spiral arms at different angles
        for (int arm = 0; arm < 3; arm++) {
            float armOffset = arm * (MathUtils.PI2 / 3);

            // Base angle that increases with time for spiral effect
            float baseAngle = patternTimer * 2.0f + armOffset;

            // Number of bullets in this wave
            int bulletCount = 5;

            for (int i = 0; i < bulletCount; i++) {
                // Create spiral by offsetting each bullet's angle
                float angle = baseAngle + (i * 0.3f);

                // Calculate spawn position
                float spawnRadius = 20f + (i * 15f); // Increasing radius for spiral effect
                float spawnX = enemyX + MathUtils.cos(angle) * spawnRadius;
                float spawnY = enemyY + MathUtils.sin(angle) * spawnRadius;

                // Calculate direction
                float dirX = MathUtils.cos(angle);
                float dirY = MathUtils.sin(angle);

                // Create bullet with increasing speed for bullets further out
                float bulletSpeed = speed * (0.6f + (i * 0.1f));

                Bullet bullet = new Bullet(
                    damage,
                    spawnX,
                    spawnY,
                    dirX * bulletSpeed,
                    dirY * bulletSpeed,
                    6f + (i * 0.5f), // Size increases slightly for outer bullets
                    new Color(baseColor).lerp(new Color(0.9f, 0.7f, 0.1f, 1.0f), (float)i/bulletCount),
                    false // Not healing
                );

                // Special behavior: accelerate outward
                final float[] timeTracker = {0f};
                final float[] speedMultiplier = {1.0f};
                final float acceleration = 0.2f;
                final float maxSpeedMultiplier = 2.0f;

                bullet.setUpdateCallback(delta -> {
                    timeTracker[0] += delta;

                    // Accelerate up to a maximum multiplier
                    if (speedMultiplier[0] < maxSpeedMultiplier) {
                        speedMultiplier[0] += acceleration * delta;
                        float newVelX = dirX * bulletSpeed * speedMultiplier[0];
                        float newVelY = dirY * bulletSpeed * speedMultiplier[0];
                        bullet.setVelocity(newVelX, newVelY);
                    }

                    return true; // Continue the callback
                });

                // Apply visual styling
                styleBullet(bullet, false);

                // Add oscillating spin for visual effect
                bullet.startOscillatingSpinning(360f, 180f);

                bullets.add(bullet);
            }
        }
    }

    /**
     * Creates a fan pattern of bullets with crossing streams
     */
    private void createFanPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                float playerX, float playerY, float speed,
                                float minDamage, float maxDamage, Color baseColor) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Calculate direction to player
        float dirX = playerX - enemyX;
        float dirY = playerY - enemyY;
        float dirLength = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX = dirX / dirLength;
        dirY = dirY / dirLength;

        // Calculate perpendicular direction for fan spread
        float perpX = -dirY;
        float perpY = dirX;

        // Create two crossing fan patterns
        for (int fanSet = 0; fanSet < 2; fanSet++) {
            // Offset the second fan set in time
            float angleOffset = fanSet * MathUtils.PI * 0.25f;

            // Create fan of bullets
            for (int i = 0; i < FAN_BULLETS; i++) {
                // Calculate spread angle
                float spread = (i / (float)(FAN_BULLETS - 1)) - 0.5f; // Range: -0.5 to 0.5
                float angle = spread * MathUtils.degreesToRadians * FAN_ANGLE;

                // Calculate the rotated direction
                float cos = MathUtils.cos(angle + angleOffset);
                float sin = MathUtils.sin(angle + angleOffset);
                float rotatedDirX = dirX * cos - dirY * sin;
                float rotatedDirY = dirX * sin + dirY * cos;

                // Create the bullet with the calculated direction
                Bullet bullet = new Bullet(
                    damage,
                    enemyX,
                    enemyY,
                    rotatedDirX * speed,
                    rotatedDirY * speed,
                    8f, // Size
                    new Color(baseColor).lerp(Color.RED, 0.3f * Math.abs(spread * 2)),
                    false // Not healing
                );

                // Add telegraphing for more visible trajectories
                bullet.enableTelegraphing(0.3f, 0.2f);

                // For middle bullets, add trailing effect
                if (Math.abs(spread) < 0.2f) {
                    bullet.setTrailLength(60);
                    bullet.setGlowing(true);
                    bullet.setGlowLayers(5);
                    bullet.setGlowIntensity(0.3f);
                }

                // Add zigzag trajectory for visual interest
                if (i % 2 == fanSet % 2) {
                    addZigzagBehavior(bullet, rotatedDirX, rotatedDirY, 40f, 2.5f);
                }

                // Apply visual styling
                styleBullet(bullet, false);

                bullets.add(bullet);
            }
        }
    }

    /**
     * Adds a zigzag behavior to a bullet
     */
    private void addZigzagBehavior(Bullet bullet, float baseVelX, float baseVelY,
                                 float zigzagAmplitude, float zigzagFrequency) {
        final float[] time = {0f};
        final float origVelX = bullet.getVelocityX();
        final float origVelY = bullet.getVelocityY();
        final boolean[] telegraphComplete = {false};
        final float telegraphDuration = 0.7f; // Should match the telegraphing duration

        bullet.setUpdateCallback(delta -> {
            time[0] += delta;

            // Only start zigzagging after telegraphing is complete
            if (!telegraphComplete[0]) {
                if (time[0] >= telegraphDuration) {
                    telegraphComplete[0] = true;
                }
                return true; // Continue the callback without zigzagging yet
            }

            // Calculate actual zigzag time (starting from when telegraphing finished)
            float zigzagTime = time[0] - telegraphDuration;

            // Calculate perpendicular direction
            float length = (float) Math.sqrt(origVelX * origVelX + origVelY * origVelY);
            float perpX = -origVelY / length;
            float perpY = origVelX / length;

            // Apply sine wave oscillation perpendicular to movement
            float oscillation = MathUtils.sin(zigzagTime * zigzagFrequency * MathUtils.PI2) * zigzagAmplitude;

            // Set new velocity
            bullet.setVelocity(
                origVelX + perpX * oscillation,
                origVelY + perpY * oscillation
            );

            return true;
        });
    }

    /**
     * Creates a healing bullet that moves in a semi-random pattern
     */
    private void createHealingBullet(List<Bullet> bullets, float enemyX, float enemyY,
                                   float playerX, float playerY, float playerSpeed) {
        // Calculate initial position (random around the enemy)
        float angle = MathUtils.random(MathUtils.PI2);
        float distance = MathUtils.random(50f, 100f);
        float spawnX = enemyX + MathUtils.cos(angle) * distance;
        float spawnY = enemyY + MathUtils.sin(angle) * distance;

        // Calculate direction toward player with some randomness
        float dirX = playerX - spawnX;
        float dirY = playerY - spawnY;
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX = dirX / length;
        dirY = dirY / length;

        // Add randomness to direction
        dirX += MathUtils.random(-0.3f, 0.3f);
        dirY += MathUtils.random(-0.3f, 0.3f);

        // Re-normalize
        length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX = dirX / length;
        dirY = dirY / length;

        // Create the healing bullet
        Bullet healingBullet = new Bullet(
            20f, // Healing amount
            spawnX,
            spawnY,
            dirX * 150f, // Slower than attack bullets
            dirY * 150f,
            15f, // Larger size
            new Color(0.2f, 0.9f, 0.2f, 1.0f), // Bright green color
            true // Set isHealing to true
        );

        // Style the healing bullet
        healingBullet.setShape(Bullet.Shape.HEART);
        healingBullet.setTrailLength(30);
        healingBullet.setGlowing(true);
        healingBullet.setGlowLayers(8);
        healingBullet.setGlowIntensity(0.3f);
        healingBullet.enableRainbow(1.8f, 0.7f, 1.0f);

        bullets.add(healingBullet);
    }

    /**
     * Applies visual styling to bullets
     */
    private void styleBullet(Bullet bullet, boolean isRingBullet) {
        // Base styling
        bullet.setTrailLength(35);
        bullet.setGlowing(true);
        bullet.setGlowLayers(5);
        bullet.setGlowIntensity(0.25f);
        bullet.setAutoRotate(true);

        // Different shapes based on type
        if (isRingBullet) {
            bullet.setShape(Bullet.Shape.DIAMOND);
            bullet.setRotationSpeed(180f);
        } else {
            bullet.setShape(Bullet.Shape.HEXAGON);
            bullet.setDisco(true, true, false);
            bullet.setDiscoSpeed(1.2f);
            bullet.setDiscoColorRange(0.3f);
        }
    }

    private void createHealingBullet(List<Bullet> bullets, float enemyX, float enemyY,
                                   float playerX, float playerY) {
        createHealingBullet(bullets, enemyX, enemyY, playerX, playerY, 0f);
    }

    @Override
    public String getPatternName() {
        return "Cicada's Harmonic Resonance";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
