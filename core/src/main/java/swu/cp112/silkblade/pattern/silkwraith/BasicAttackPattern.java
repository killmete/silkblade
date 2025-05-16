package swu.cp112.silkblade.pattern.silkwraith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.entity.enemy.SilkWraith;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.screen.StageSelectionScreen;
import swu.cp112.silkblade.util.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * A redesigned attack pattern for the Silk Wraith that fires bullets from arena edges
 * with behavior that changes based on the stage level:
 * - Phase 1 (Stages 1-3): Simple direct shots from arena edges
 * - Phase 2 (Stages 4-6): Zig-zag trajectory bullets from arena edges
 * - Phase 3 (Stages 7-9): Homing bullets plus healing bullets
 */
public class BasicAttackPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        4, 6, 3.0f, 0.75f, 12, 300f, 300f, Color.WHITE, true,
        "Basic Wraith Attack", 1.5f
    );

    private float spawnTimer = 0f;
    private static final float SPAWN_INTERVAL = 0.8f;

    // Chance to spawn a healing orb in Phase 3
    private static final float HEALING_ORB_CHANCE = 0.25f; // 25% chance per wave

    // Buffer for spawn distance from arena edges
    private static final float MIN_EDGE_BUFFER = 30f;
    private static final float MAX_EDGE_BUFFER = 50f;

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

        // Get the current stage number - try multiple approaches
        int stageNumber = 1; // Default to stage 1

        // First, check if this is a SilkWraith, which has direct stage info
        if (enemy instanceof SilkWraith) {
            // For SilkWraith enemies, use the name to determine stage:
            // "Golden Silk Wraith" = stages 7-9
            // "Crimson Silk Wraith" = stages 4-6
            // "Silk Wraith" = stages 1-3
            String enemyName = enemy.getName();
            if (enemyName.contains("Golden")) {
                stageNumber = 7; // Use lowest stage in range
            } else if (enemyName.contains("Crimson")) {
                stageNumber = 4; // Use lowest stage in range
            } else {
                stageNumber = 1; // Default for regular Silk Wraith
            }
        } else {
            // As a fallback, try to get the stage from the StageSelectionScreen
            stageNumber = StageSelectionScreen.getCurrentChallengingStage();
            if (stageNumber <= 0) {
                // If no stage info available, determine phase based on enemy's attack damage
                int enemyAttackDamage = enemy.getAttackDamage();
                if (enemyAttackDamage >= 7) {
                    stageNumber = 7; // Phase 3
                } else if (enemyAttackDamage >= 4) {
                    stageNumber = 4; // Phase 2
                } else {
                    stageNumber = 1; // Phase 1
                }
            }
        }

        // Determine phase based on stage number
        int phase;
        int bulletCount;
        float bulletSpeed;
        float bulletSize;

        // Phase determination based on stage number
        if (stageNumber >= 7 && stageNumber <= 9) {
            // Phase 3 (Stages 7-9)
            phase = 3;
            bulletCount = 5;
            bulletSpeed = 250f;
            bulletSize = 10f;
        } else if (stageNumber >= 4 && stageNumber <= 6) {
            // Phase 2 (Stages 4-6)
            phase = 2;
            bulletCount = 4;
            bulletSpeed = 200f;
            bulletSize = 9f;
        } else {
            // Phase 1 (Stages 1-3 or any other stage defaulting to phase 1)
            phase = 1;
            bulletCount = 3;
            bulletSpeed = 180f;
            bulletSize = 8f;
        }

        // Get enemy's attack damage for scaling bullet damage
        int enemyAttackDamage = enemy.getAttackDamage();

        // Calculate scaled damage based on enemy attack damage
        float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.1f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.15f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.15f)) * defenseMultiplier;
        float damage = MathUtils.random(scaledMinDamage, scaledMaxDamage);

        // Get enemy's primary color
        Color enemyColor = enemy.getPrimaryColor();

        // Generate attack bullets based on the current phase
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

            // Calculate direction toward player
            float dirX = playerX - spawnX;
            float dirY = playerY - spawnY;
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            dirX = dirX / length;
            dirY = dirY / length;

            // Create the bullet with velocity aimed at player
            Bullet bullet = new Bullet(
                damage,
                spawnX,
                spawnY,
                dirX * bulletSpeed,
                dirY * bulletSpeed,
                bulletSize,
                enemyColor,
                false // Not healing
            );

            // Apply phase-specific behavior
            applyPhaseBehavior(bullet, phase, playerX, playerY);

            // Stylize the bullet based on phase
            stylizeBullet(bullet, phase, enemyColor);

            bullets.add(bullet);
        }

        // In Phase 3, potentially spawn a healing orb with a 25% chance per wave
        if (phase == 3 && MathUtils.random() < HEALING_ORB_CHANCE) {
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

            // Calculate direction (slightly biased toward player but with randomness)
            float dirX = playerX - healX;
            float dirY = playerY - healY;
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            dirX = dirX / length;
            dirY = dirY / length;

            // Add some randomness to direction
            dirX += MathUtils.random(-0.3f, 0.3f);
            dirY += MathUtils.random(-0.3f, 0.3f);

            // Re-normalize direction
            length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            dirX = dirX / length;
            dirY = dirY / length;

            // Create healing bullet
            Bullet healingBullet = new Bullet(
                15f, // Amount of healing
                healX,
                healY,
                dirX * 120f, // Slower speed for healing bullets
                dirY * 120f,
                15f, // Larger size
                new Color(0.3f, 1.0f, 0.3f, 1.0f), // Bright green color
                true // Set isHealing to true
            );

            // Apply some basic styling
            healingBullet.setShape(Bullet.Shape.CIRCLE);
            healingBullet.setTrailLength(20);
            healingBullet.setGlowing(true);
            healingBullet.setGlowLayers(10);
            healingBullet.setGlowIntensity(0.2f);

            bullets.add(healingBullet);
        }

        return bullets;
    }

    /**
     * Apply behavior modifiers to bullets based on the current phase
     */
    private void applyPhaseBehavior(Bullet bullet, int phase, float playerX, float playerY) {
        switch (phase) {
            case 3: // Phase 3: Initial homing behavior
                // Use the proper homing implementation
                bullet.enableHoming(1.0f, 550f);
                bullet.updateTarget(playerX, playerY);
                break;

            case 2: // Phase 2: Zig-zag trajectory
                // Implement zig-zag using a custom update callback
                // Store original velocity values for zigzag calculation
                final float origVelX = bullet.getVelocityX();
                final float origVelY = bullet.getVelocityY();
                final float[] time = {0f}; // For tracking elapsed time
                final float zigzagAmplitude = 50f; // Strength of the zigzag
                final float zigzagFrequency = 2.0f; // How fast the zigzag oscillates

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
                break;

            case 1: // Phase 1: Direct shots (default behavior)
            default:
                // No special behavior, just direct shots
                break;
        }
    }

    /**
     * Apply visual styles to bullets based on the phase
     */
    private void stylizeBullet(Bullet bullet, int phase, Color baseColor) {
        // Base styling for all phases
        bullet.setShape(Bullet.Shape.DIAMOND); // Thread-like appearance
        bullet.setAutoRotate(true);
        bullet.setRotationSpeed(90f);

        // Phase-specific styling
        switch (phase) {
            case 3: // Phase 3 (Levels 7-9)
                bullet.setTrailLength(60);
                bullet.setGlowing(true);
                bullet.setGlowLayers(8);
                bullet.setGlowIntensity(0.3f);
                bullet.setDisco(true, true, false);
                bullet.setDiscoSpeed(1.5f);
                bullet.setDiscoColorRange(0.2f);
                bullet.enableTelegraphing(0.5f, 0.3f);
                break;

            case 2: // Phase 2 (Levels 4-6)
                bullet.setTrailLength(45);
                bullet.setGlowing(true);
                bullet.setGlowLayers(5);
                bullet.setGlowIntensity(0.2f);
                bullet.setDisco(true, false, false);
                bullet.setDiscoSpeed(1.2f);
                bullet.setDiscoColorRange(0.15f);
                break;

            case 1: // Phase 1 (Levels 1-3)
            default:
                bullet.setTrailLength(30);
                bullet.setGlowing(true);
                bullet.setGlowLayers(3);
                bullet.setGlowIntensity(0.1f);
                break;
        }
    }

    @Override
    public String getPatternName() {
        return "Wraith's Boundary Assault";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
