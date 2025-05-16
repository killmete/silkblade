package swu.cp112.silkblade.pattern.silkguardian;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.entity.enemy.SilkGuardian;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.util.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * An evolved attack pattern for the Silk Guardian boss (Stage 10)
 * This pattern enhances the SilkWraith patterns with:
 * - More projectiles
 * - Decreased attack intervals
 * - Deadlier and more precise attacks
 * - Phase 2 activation at 60% HP (rainbow colored projectiles)
 */
public class EvolutionaryAttackPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        5, 8, 2.0f, 0.85f, 20, 650f, 450f, Color.WHITE, true,
        "Evolutionary Guardian Attack", 1.2f
    );

    private static final float SPAWN_INTERVAL = 0.6f; // Decreased from 0.8f in SilkWraith

    // Chance to spawn a healing orb in phase 2
    private static final float HEALING_ORB_CHANCE = 0.35f; // 35% chance, increased from 25%

    // Buffer for spawn distance from arena edges
    private static final float MIN_EDGE_BUFFER = 30f;
    private static final float MAX_EDGE_BUFFER = 50f;

    // Phase 2 begins at 60% HP
    private static final float PHASE_2_HP_THRESHOLD = 0.6f;

    // Enum to represent which side of the arena to spawn bullet from
    private enum SpawnSide {
        TOP, RIGHT, BOTTOM, LEFT
    }

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                        float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Get player position for targeting
        float playerX = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerX() :
            arenaX + arenaWidth / 2;
        float playerY = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerY() :
            arenaY + arenaHeight / 2;

        // Check if the enemy is in phase 2
        boolean isPhase2 = false;
        if (enemy instanceof SilkGuardian) {
            SilkGuardian guardian = (SilkGuardian) enemy;
            isPhase2 = guardian.isInPhase2();
        } else {
            // Fallback calculation if for some reason pattern is used with another enemy
            float hpRatio = (float) enemy.getCurrentHP() / enemy.getMaxHP();
            isPhase2 = hpRatio <= PHASE_2_HP_THRESHOLD;
        }

        // Configure attack parameters based on phase
        int bulletCount = 5;  // More bullets than SilkWraith (was 5/3)
        float bulletSpeed = isPhase2 ? 550f : 450f; // Fast at start, slightly faster in phase 2
        float bulletSize = 8f;   // Larger than SilkWraith

        // Get enemy's attack damage for scaling bullet damage
        int enemyAttackDamage = enemy.getAttackDamage();

        // Calculate scaled damage based on enemy attack damage
        float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.12f); // Higher scaling than SilkWraith
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.18f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.18f)) * defenseMultiplier;
        float damage = MathUtils.random(scaledMinDamage, scaledMaxDamage);

        // Get enemy's primary color
        Color enemyColor = enemy.getPrimaryColor();

        // Generate attack bullets
        for (int i = 0; i < bulletCount; i++) {
            // Choose a random side of the arena to spawn the bullet
            SpawnSide side = SpawnSide.values()[MathUtils.random(0, 3)];

            // Random buffer distance from edge
            float edgeBuffer = MathUtils.random(MIN_EDGE_BUFFER, MAX_EDGE_BUFFER);

            // Calculate spawn position based on the chosen side
            float spawnX, spawnY;
            switch (side) {
                case TOP:
                    spawnX = arenaX + MathUtils.random(0, arenaWidth);
                    spawnY = arenaY + arenaHeight + edgeBuffer; // Spawn outside the top edge
                    break;
                case RIGHT:
                    spawnX = arenaX + arenaWidth + edgeBuffer; // Spawn outside the right edge
                    spawnY = arenaY + MathUtils.random(0, arenaHeight);
                    break;
                case BOTTOM:
                    spawnX = arenaX + MathUtils.random(0, arenaWidth);
                    spawnY = arenaY - edgeBuffer; // Spawn outside the bottom edge
                    break;
                case LEFT:
                default:
                    spawnX = arenaX - edgeBuffer; // Spawn outside the left edge
                    spawnY = arenaY + MathUtils.random(0, arenaHeight);
                    break;
            }

            // Calculate direction toward player with improved precision
            float dirX = playerX - spawnX;
            float dirY = playerY - spawnY;
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            dirX = dirX / length;
            dirY = dirY / length;

            // Less randomness in trajectory for more precise targeting
            if (!isPhase2) {
                // Add slight randomness in phase 1
                dirX += MathUtils.random(-0.05f, 0.05f);
                dirY += MathUtils.random(-0.05f, 0.05f);
                // Re-normalize
                float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                dirX /= len;
                dirY /= len;
            }

            // Create the bullet with velocity aimed at player
            Bullet bullet = new Bullet(
                isPhase2 ? damage * 1.35f : damage,
                spawnX,
                spawnY,
                dirX * bulletSpeed,
                dirY * bulletSpeed,
                bulletSize,
                isPhase2 ? getRainbowColor() : enemyColor, // Use rainbow color in phase 2
                false // Not healing
            );

            // Apply phase-specific behavior
            applyPhaseBehavior(bullet, isPhase2, playerX, playerY);

            // Stylize the bullet based on phase
            stylizeBullet(bullet, isPhase2, enemyColor);

            bullets.add(bullet);
        }

        // In phase 2, potentially spawn a healing orb with a higher chance
        if (isPhase2 && MathUtils.random() < HEALING_ORB_CHANCE) {
            // Select a random side to spawn from
            SpawnSide side = SpawnSide.values()[MathUtils.random(0, 3)];

            // Random buffer distance from edge
            float edgeBuffer = MathUtils.random(MIN_EDGE_BUFFER, MAX_EDGE_BUFFER);

            // Calculate spawn position
            float healX, healY;
            switch (side) {
                case TOP:
                    healX = arenaX + MathUtils.random(0, arenaWidth);
                    healY = arenaY + arenaHeight + edgeBuffer; // Outside top edge
                    break;
                case RIGHT:
                    healX = arenaX + arenaWidth + edgeBuffer; // Outside right edge
                    healY = arenaY + MathUtils.random(0, arenaHeight);
                    break;
                case BOTTOM:
                    healX = arenaX + MathUtils.random(0, arenaWidth);
                    healY = arenaY - edgeBuffer; // Outside bottom edge
                    break;
                case LEFT:
                default:
                    healX = arenaX - edgeBuffer; // Outside left edge
                    healY = arenaY + MathUtils.random(0, arenaHeight);
                    break;
            }

            // Calculate direction (biased toward enemy for healing)
            float dirX = enemy.getX() - healX;
            float dirY = enemy.getY() - healY;
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            dirX = dirX / length;
            dirY = dirY / length;

            // Add some randomness to direction
            dirX += MathUtils.random(-0.2f, 0.2f);
            dirY += MathUtils.random(-0.2f, 0.2f);

            // Re-normalize direction
            length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            dirX = dirX / length;
            dirY = dirY / length;

            // Create healing bullet (for the boss)
            Bullet healingBullet = new Bullet(
                20f, // Increased healing amount from SilkWraith
                healX,
                healY,
                dirX * 150f, // Faster healing bullets
                dirY * 150f,
                18f, // Larger size
                getRainbowColor(0.3f, 1.0f, 0.3f), // Rainbow-green color for healing
                true // Set isHealing to true
            );

            // Apply some enhanced styling to healing orbs
            healingBullet.setShape(Bullet.Shape.CIRCLE);
            healingBullet.setTrailLength(30);
            healingBullet.setGlowing(true);
            healingBullet.setGlowLayers(15);
            healingBullet.setGlowIntensity(0.3f);
            healingBullet.setDisco(true, true, false);
            healingBullet.setDiscoSpeed(2.0f);

            bullets.add(healingBullet);
        }

        return bullets;
    }

    /**
     * Apply behavior modifiers to bullets based on the current phase
     */
    private void applyPhaseBehavior(Bullet bullet, boolean isPhase2, float playerX, float playerY) {
        if (isPhase2) {
            // Phase 2: Enhanced homing with acceleration
            bullet.enableHoming(1.5f, 650f); // Stronger homing than SilkWraith
            bullet.updateTarget(playerX, playerY);

            // Add acceleration over time
            final float[] time = {0f};
            final float initialSpeed = new Vector2(bullet.getVelocityX(), bullet.getVelocityY()).len();
            final float maxSpeedMultiplier = 1.2f; // Lower speed increase over time (20% vs 40%)

            bullet.setUpdateCallback(delta -> {
                time[0] += delta;
                float speedMultiplier = 1.0f + (time[0] * 0.05f); // Slower acceleration rate
                if (speedMultiplier > maxSpeedMultiplier) {
                    speedMultiplier = maxSpeedMultiplier;
                }

                Vector2 velocity = new Vector2(bullet.getVelocityX(), bullet.getVelocityY());
                velocity.nor().scl(initialSpeed * speedMultiplier);
                bullet.setVelocity(velocity.x, velocity.y);

                // Always update target position for better homing
                bullet.updateTarget(playerX, playerY);

                return true; // Continue the callback
            });
        } else {
            // Phase 1: Zigzag with improved precision
            // Implement zigzag using a custom update callback
            // Store original velocity values for zigzag calculation
            final float origVelX = bullet.getVelocityX();
            final float origVelY = bullet.getVelocityY();
            final float[] time = {0f}; // For tracking elapsed time
            final float zigzagAmplitude = 60f; // Stronger zigzag than SilkWraith
            final float zigzagFrequency = 2.5f; // Faster oscillation than SilkWraith

            // Using a custom callback to implement zigzag movement
            bullet.setUpdateCallback(delta -> {
                time[0] += delta; // Increment time

                // Calculate perpendicular direction
                float length = (float) Math.sqrt(origVelX * origVelX + origVelY * origVelY);
                float perpX = -origVelY / length;
                float perpY = origVelX / length;

                // Apply sine wave to create zigzag
                float oscillation = MathUtils.sin(time[0] * zigzagFrequency * MathUtils.PI2) * zigzagAmplitude;

                // Set new velocity that combines original direction with perpendicular oscillation
                bullet.setVelocity(
                    origVelX + perpX * oscillation,
                    origVelY + perpY * oscillation
                );

                return true; // Continue the callback
            });
        }
    }

    /**
     * Apply visual styles to bullets based on the phase
     */
    private void stylizeBullet(Bullet bullet, boolean isPhase2, Color baseColor) {
        // Base styling
        bullet.setShape(Bullet.Shape.DIAMOND); // Thread-like appearance
        bullet.setAutoRotate(true);
        bullet.setRotationSpeed(120f); // Faster rotation than SilkWraith

        if (isPhase2) {
            // Phase 2 styling - rainbow effects
            bullet.setTrailLength(80);
            bullet.setGlowing(true);
            bullet.setGlowLayers(12);
            bullet.setGlowIntensity(0.4f);
            bullet.setDisco(true, true, true); // Full disco effect
            bullet.setDiscoSpeed(2.0f);
            bullet.setDiscoColorRange(0.5f); // Wide color range for rainbow effect
            bullet.enableTelegraphing(0.6f, 0.2f); // Faster telegraphing
        } else {
            // Phase 1 styling - enhanced from SilkWraith phase 3
            bullet.setTrailLength(70);
            bullet.setGlowing(true);
            bullet.setGlowLayers(10);
            bullet.setGlowIntensity(0.35f);
            bullet.setDisco(true, true, false);
            bullet.setDiscoSpeed(1.8f);
            bullet.setDiscoColorRange(0.3f);
        }
    }

    /**
     * Generates a rainbow color for phase 2 bullets
     */
    private Color getRainbowColor() {
        return getRainbowColor(1f, 1f, 1f);
    }

    /**
     * Generates a rainbow color with a base tint
     */
    private Color getRainbowColor(float r, float g, float b) {
        float time = (System.currentTimeMillis() % 3000) / 3000f;
        float hue = (time * 360f) % 360f;

        // Convert HSV to RGB
        int c = java.awt.Color.HSBtoRGB(hue / 360f, 0.8f, 0.9f);
        java.awt.Color color = new java.awt.Color(c);

        // Mix with the base tint
        return new Color(
            color.getRed() / 255f * r,
            color.getGreen() / 255f * g,
            color.getBlue() / 255f * b,
            1f
        );
    }

    @Override
    public String getPatternName() {
        return "Guardian's Evolved Assault";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
