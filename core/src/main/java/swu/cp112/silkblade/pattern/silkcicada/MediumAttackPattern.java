package swu.cp112.silkblade.pattern.silkcicada;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Medium-difficulty attack pattern for the Silk Cicada enemy type.
 * Used for stages 24-26, challenging but manageable encounters.
 * Features grid patterns, diagonal shots, and pulsing waves.
 */
public class MediumAttackPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        12, 15, 3.5f, 1.55f, 13, 380f, 330f, new Color(0.3f, 0.7f, 0.1f, 1.0f), true,
        "Cicada's Rhythmic Pulse", 1.8f
    );

    // Attack phase trackers
    private int currentPhase = 0;
    private float phaseTimer = 0f;
    private static final float PHASE_DURATION = 3.5f; // Duration of each attack phase
    private static final int TOTAL_PHASES = 3; // Total different attack phases

    // Track overall pattern time
    private float patternTimer = 0f;

    // Grid pattern properties
    private static final int GRID_ROWS = 4;
    private static final int GRID_COLS = 4;
    private static final float GRID_SPACING = 40f;

    // Wave pattern properties
    private static final int WAVE_BULLETS = 8;
    private static final float WAVE_ANGLE_SPREAD = 60f; // Degrees

    // Diagonal pattern properties
    private static final int DIAGONAL_LINES = 4;
    private static final int BULLETS_PER_LINE = 5;

    // Healing properties
    private static final float HEALING_CHANCE = 0.12f;

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
        float enemyX = enemy.getX();
        float enemyY = enemy.getY();

        // Calculate attack parameters based on enemy's state
        float damageMultiplier = 1.0f + (enemy.getAttackDamage() * 0.04f);
        float speedMultiplier = 1.0f + 0.06f; // Fixed multiplier since Enemy has no getDefense()
        float baseSpeed = 240f * speedMultiplier;

        // Get scaled damage
        float minDamage = CONFIG.getMinDamage() * damageMultiplier;
        float maxDamage = CONFIG.getMaxDamage() * damageMultiplier;

        // Get enemy color
        Color enemyColor = enemy.getPrimaryColor();

        // Execute current phase
        switch (currentPhase) {
            case 0:
                // Phase 1: Grid pattern of bullets
                createGridPattern(bullets, enemyX, enemyY, playerX, playerY,
                                 baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;

            case 1:
                // Phase 2: Wave pattern toward player
                createWavePattern(bullets, enemyX, enemyY, playerX, playerY,
                                baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;

            case 2:
                // Phase 3: Diagonal cross pattern
                createDiagonalPattern(bullets, enemyX, enemyY, playerX, playerY,
                                    baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;
        }

        // Occasionally spawn healing bullets
        if (MathUtils.random() < HEALING_CHANCE) {
            createHealingBullet(bullets, enemyX, enemyY, playerX, playerY);
        }

        return bullets;
    }

    /**
     * Creates a grid pattern of bullets that move toward the player
     */
    private void createGridPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                  float playerX, float playerY, float speed,
                                  float minDamage, float maxDamage, Color baseColor) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Calculate the direction toward player for the entire grid
        float dirX = playerX - enemyX;
        float dirY = playerY - enemyY;
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX = dirX / length;
        dirY = dirY / length;

        // Define the arena bounds approximation (assuming arena is centered on enemy)
        float arenaHalfWidth = 200f;
        float arenaHalfHeight = 200f;

        // Calculate the grid cell size
        float cellWidth = GRID_SPACING;
        float cellHeight = GRID_SPACING;

        // Create bullets in a grid formation
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                // Randomly select one of the four sides of the arena to spawn from
                int side = MathUtils.random(3); // 0 = top, 1 = right, 2 = bottom, 3 = left

                // Calculate spawn position based on the side
                float spawnX, spawnY;
                float spawnDirX = dirX;
                float spawnDirY = dirY;

                switch (side) {
                    case 0: // Top
                        spawnX = enemyX - arenaHalfWidth + MathUtils.random(arenaHalfWidth * 2);
                        spawnY = enemyY + arenaHalfHeight + MathUtils.random(50f, 150f);
                        // Adjust direction to come from top
                        spawnDirY = -Math.abs(spawnDirY);
                        break;
                    case 1: // Right
                        spawnX = enemyX + arenaHalfWidth + MathUtils.random(50f, 150f);
                        spawnY = enemyY - arenaHalfHeight + MathUtils.random(arenaHalfHeight * 2);
                        // Adjust direction to come from right
                        spawnDirX = -Math.abs(spawnDirX);
                        break;
                    case 2: // Bottom
                        spawnX = enemyX - arenaHalfWidth + MathUtils.random(arenaHalfWidth * 2);
                        spawnY = enemyY - arenaHalfHeight - MathUtils.random(50f, 150f);
                        // Adjust direction to come from bottom
                        spawnDirY = Math.abs(spawnDirY);
                        break;
                    default: // Left
                        spawnX = enemyX - arenaHalfWidth - MathUtils.random(50f, 150f);
                        spawnY = enemyY - arenaHalfHeight + MathUtils.random(arenaHalfHeight * 2);
                        // Adjust direction to come from left
                        spawnDirX = Math.abs(spawnDirX);
                        break;
                }

                // Calculate the target position (where the bullet will go after telegraphing)
                float targetX = playerX + (col - GRID_COLS/2) * cellWidth;
                float targetY = playerY + (row - GRID_ROWS/2) * cellHeight;

                // Calculate direction toward the target position
                float targetDirX = targetX - spawnX;
                float targetDirY = targetY - spawnY;
                float targetLength = (float) Math.sqrt(targetDirX * targetDirX + targetDirY * targetDirY);
                targetDirX /= targetLength;
                targetDirY /= targetLength;

                // Vary speed slightly based on position in grid
                float speedVariation = 0.8f + (0.4f * ((float)row / GRID_ROWS));
                float bulletSpeed = speed * speedVariation;

                // Create the bullet with initial zero velocity (for telegraphing)
                Bullet bullet = new Bullet(
                    damage,
                    spawnX,
                    spawnY,
                    0f, // Start with zero velocity for telegraphing
                    0f,
                    6f, // Size
                    // Vary color based on position in grid
                    new Color(baseColor).lerp(Color.YELLOW, (float)col / GRID_COLS * 0.5f),
                    false // Not healing
                );

                // Apply visual styling
                styleBullet(bullet, 0);

                // Add telegraphing effect
                bullet.enableTelegraphing(0.9f, 0.3f);

                // Add movement behavior after telegraphing
                final boolean[] hasStartedMoving = {false};
                final float telegraphDuration = 0.4f; // Match the telegraphing duration
                final float[] timeTracker = {0f};

                float finalTargetDirY = targetDirY;
                float finalTargetDirX = targetDirX;
                bullet.setUpdateCallback(delta -> {
                    timeTracker[0] += delta;

                    // Start moving after telegraphing is complete
                    if (!hasStartedMoving[0] && timeTracker[0] >= telegraphDuration) {
                        hasStartedMoving[0] = true;
                        bullet.setVelocity(
                            finalTargetDirX * bulletSpeed * 2f,
                            finalTargetDirY * bulletSpeed * 2f
                        );
                    }

                    return true; // Continue the callback
                });

                bullets.add(bullet);
            }
        }
    }

    /**
     * Creates a wave pattern of bullets that spread out toward the player
     */
    private void createWavePattern(List<Bullet> bullets, float enemyX, float enemyY,
                                  float playerX, float playerY, float speed,
                                  float minDamage, float maxDamage, Color baseColor) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Calculate direction to player
        float dirX = playerX - enemyX;
        float dirY = playerY - enemyY;
        float dirLength = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX = dirX / dirLength;
        dirY = dirY / dirLength;

        // Calculate perpendicular direction for wave spread
        float perpX = -dirY;
        float perpY = dirX;

        // Use sin wave based on time for pulsing effect
        float pulseOffset = MathUtils.sin(patternTimer * 3f) * 50f;

        // Create fan of bullets in a wave pattern
        for (int i = 0; i < WAVE_BULLETS; i++) {
            // Calculate spread factor (-0.5 to 0.5)
            float spread = (i / (float)(WAVE_BULLETS - 1)) - 0.5f;

            // Convert to angle in radians
            float angle = spread * MathUtils.degreesToRadians * WAVE_ANGLE_SPREAD;

            // Apply rotation to direction
            float cos = MathUtils.cos(angle);
            float sin = MathUtils.sin(angle);
            float rotatedDirX = dirX * cos - dirY * sin;
            float rotatedDirY = dirX * sin + dirY * cos;

            // Calculate spawn position with wave offset
            float waveOffset = MathUtils.sin(spread * MathUtils.PI * 3 + patternTimer * 4f) * 30f;
            // Use a fixed offset behind the enemy in the direction opposite to the player
            float spawnOffsetDistance = 30f; // Fixed distance behind the enemy
            float spawnX = enemyX - dirX * spawnOffsetDistance + (perpX * waveOffset);
            float spawnY = enemyY - dirY * spawnOffsetDistance + (perpY * waveOffset);

            // Create the bullet
            Bullet bullet = new Bullet(
                damage,
                spawnX,
                spawnY,
                rotatedDirX * speed * 2f,
                rotatedDirY * speed * 2f,
                10f, // Size
                // Color varies based on position in wave
                new Color(baseColor).lerp(new Color(0.8f, 0.1f, 0.8f, 1.0f), Math.abs(spread * 2)),
                false // Not healing
            );

            // Apply visual styling
            styleBullet(bullet, 1);

            // Add pulse effect by changing velocity over time
            final float waveFactor = spread; // Capture the spread value
            bullet.setUpdateCallback(delta -> {
                // Calculate pulsing factor based on time
                float pulseFactor = MathUtils.sin(patternTimer * 3f + waveFactor * MathUtils.PI) * 0.3f + 0.7f;

                // Apply pulsing to velocity
                bullet.setVelocity(
                    rotatedDirX * speed * 2f * pulseFactor,
                    rotatedDirY * speed * 2f * pulseFactor
                );

                return true; // Continue the callback
            });

            bullets.add(bullet);
        }
    }

    /**
     * Creates diagonal lines of bullets crossing the arena
     */
    private void createDiagonalPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                      float playerX, float playerY, float speed,
                                      float minDamage, float maxDamage, Color baseColor) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Create diagonal lines at different angles
        for (int line = 0; line < DIAGONAL_LINES; line++) {
            // Calculate base angle for this line
            float baseAngle = (line / (float)DIAGONAL_LINES) * MathUtils.PI2;

            // Create bullets along the diagonal
            for (int i = 0; i < BULLETS_PER_LINE; i++) {
                // Calculate spawn position along the diagonal
                float distance = 20f + (i * 40f); // Increasing distance from enemy
                float spawnX = enemyX + MathUtils.cos(baseAngle) * distance;
                float spawnY = enemyY + MathUtils.sin(baseAngle) * distance;

                // Calculate direction perpendicular to the diagonal
                float perpAngle = baseAngle + MathUtils.PI/2;
                float dirX = MathUtils.cos(perpAngle);
                float dirY = MathUtils.sin(perpAngle);

                // Create bullet
                Bullet bullet = new Bullet(
                    damage,
                    spawnX,
                    spawnY,
                    dirX * speed,
                    dirY * speed,
                    6.5f, // Size
                    // Color varies based on diagonal line
                    new Color(baseColor).lerp(new Color(0.1f, 0.5f, 0.9f, 1.0f), (float)line / DIAGONAL_LINES),
                    false // Not healing
                );

                // Apply visual styling
                styleBullet(bullet, 2);

                // Add special behavior: bullets slightly attracted to player after delay
                final float bulletIndex = i;
                final float[] timeTracker = {0f};
                final float[] attractionPhase = {0}; // 0 = slowing, 1 = accelerating toward player, 2 = continue in direction

                bullet.setUpdateCallback(delta -> {
                    timeTracker[0] += delta;

                    // After delay based on bullet index, start the attraction sequence
                    if (timeTracker[0] > 0.8f + (bulletIndex * 0.1f)) {
                        // Get current velocity
                        float velX = bullet.getVelocityX();
                        float velY = bullet.getVelocityY();
                        float currentSpeed = (float) Math.sqrt(velX * velX + velY * velY);

                        if (attractionPhase[0] == 0) {
                            // Phase 0: Slow down
                            float slowDownFactor = 0.9f; // Slow down to 90% of current speed each frame
                            float newSpeed = currentSpeed * slowDownFactor;

                            // When speed is low enough, switch to acceleration phase
                            if (newSpeed < speed * 0.4f) {
                                attractionPhase[0] = 1;
                            } else {
                                // Maintain direction but reduce speed
                                float normalizedVelX = velX / currentSpeed;
                                float normalizedVelY = velY / currentSpeed;
                                bullet.setVelocity(
                                    normalizedVelX * newSpeed,
                                    normalizedVelY * newSpeed
                                );
                            }
                        } else if (attractionPhase[0] == 1) {
                            // Phase 1: Accelerate toward player
                            // Calculate direction to player
                            float toPlayerX = playerX - bullet.getX();
                            float toPlayerY = playerY - bullet.getY();
                            float toPlayerLen = (float) Math.sqrt(toPlayerX * toPlayerX + toPlayerY * toPlayerY);

                            if (toPlayerLen > 0) {
                                toPlayerX /= toPlayerLen;
                                toPlayerY /= toPlayerLen;

                                // Accelerate toward player
                                float accelerationFactor = 2.2f; // Speed up faster than we slowed down
                                float newSpeed = currentSpeed + (speed * accelerationFactor * delta);

                                // When speed exceeds original speed by factor, stop the attraction
                                if (newSpeed > speed * 1.5f) {
                                    attractionPhase[0] = 2;
                                    // Continue in this direction at this speed
                                    bullet.setVelocity(
                                        toPlayerX * newSpeed,
                                        toPlayerY * newSpeed
                                    );
                                } else {
                                    // Continue accelerating toward player
                                    bullet.setVelocity(
                                        toPlayerX * newSpeed,
                                        toPlayerY * newSpeed
                                    );
                                }
                            }
                        }
                        // Phase 2: Continue in straight line (do nothing to the velocity)
                    }

                    return true; // Continue the callback
                });

                bullets.add(bullet);
            }
        }
    }

    /**
     * Creates a healing bullet that moves in a gentle pattern
     */
    private void createHealingBullet(List<Bullet> bullets, float enemyX, float enemyY,
                                    float playerX, float playerY) {
        // Calculate random spawn position around enemy
        float angle = MathUtils.random(MathUtils.PI2);
        float distance = MathUtils.random(40f, 90f);
        float spawnX = enemyX + MathUtils.cos(angle) * distance;
        float spawnY = enemyY + MathUtils.sin(angle) * distance;

        // Calculate direction toward player with some randomness
        float dirX = playerX - spawnX;
        float dirY = playerY - spawnY;
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX = dirX / length;
        dirY = dirY / length;

        // Add some randomness
        dirX += MathUtils.random(-0.25f, 0.25f);
        dirY += MathUtils.random(-0.25f, 0.25f);

        // Re-normalize
        length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX = dirX / length;
        dirY = dirY / length;

        // Create healing bullet
        Bullet healingBullet = new Bullet(
            15f, // Healing amount
            spawnX,
            spawnY,
            dirX * 130f, // Speed
            dirY * 130f,
            13f, // Size
            new Color(0.2f, 0.8f, 0.2f, 1.0f), // Green color
            true // Is healing
        );

        // Style the healing bullet
        healingBullet.setShape(Bullet.Shape.HEART);
        healingBullet.setTrailLength(25);
        healingBullet.setGlowing(true);
        healingBullet.setGlowLayers(6);
        healingBullet.setGlowIntensity(0.25f);
        healingBullet.setDisco(true, true, false);
        healingBullet.setDiscoSpeed(1.5f);

        bullets.add(healingBullet);
    }

    /**
     * Applies visual styling to bullets based on the phase
     */
    private void styleBullet(Bullet bullet, int phaseType) {
        // Base styling for all bullets
        bullet.setTrailLength(30);
        bullet.setGlowing(true);
        bullet.setGlowLayers(4);
        bullet.setGlowIntensity(0.2f);
        bullet.setAutoRotate(true);

        // Phase-specific styling
        switch (phaseType) {
            case 0: // Grid pattern
                bullet.setShape(Bullet.Shape.SQUARE);
                bullet.setRotationSpeed(120f);
                break;

            case 1: // Wave pattern
                bullet.setShape(Bullet.Shape.DIAMOND);
                bullet.setTrailLength(35);
                bullet.setDisco(true, false, false);
                bullet.setDiscoSpeed(1.0f);
                bullet.setDiscoColorRange(0.2f);
                break;

            case 2: // Diagonal pattern
                bullet.setShape(Bullet.Shape.DIAMOND);
                bullet.setRotationSpeed(180f);
                bullet.setGlowLayers(3);
                break;
        }
    }

    @Override
    public String getPatternName() {
        return "Cicada's Rhythmic Pulse";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
