package swu.cp112.silkblade.pattern.threadmancer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * PhantomNeedlePattern - The second attack pattern for the Threadmancer boss
 * Features precisely aimed needles that appear with clear telegraphing,
 * creating a dance of deadly precision that rewards careful observation.
 *
 * Pattern consists of:
 * 1. Phantom needles that appear with clear telegraphing
 * 2. Needle barrages that follow player movement
 * 3. Cross-stitch patterns that create danger zones
 */
public class PhantomNeedlePattern implements EnemyAttackPattern {
    // Pattern configuration
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        9, 14, 4.0f, 0.7f, 20, 400f, 400f,
        new Color(0.2f, 0.6f, 0.9f, 1.0f), false,
        "Phantom Needle", 2.0f
    );

    // Pattern timing
    private int spawnCycle = 0;
    private int patternPhase = 0;
    private static final int PHASES = 3;
    private static final int NEEDLE_SPAWN_INTERVAL = 1;
    private float patternTimer = 0f;

    // Player tracking for prediction
    private Vector2 lastPlayerPos = new Vector2();
    private Vector2 playerVelocity = new Vector2();
    private int playerTrackCycle = 0;

    // Needle parameters
    private static final float NEEDLE_SPEED = 750f;
    private static final float TELEGRAPH_TIME = 1.0f;
    private static final float NEEDLE_SIZE = 6.5f;

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

        // Update player tracking
        updatePlayerTracking(playerX, playerY);

        // Increment spawn cycle
        spawnCycle++;
        patternTimer += 0.05f;

        // Generate bullets based on spawn cycle
        // Calculate damage based on enemy's attack damage
        float baseDamage = MathUtils.random(CONFIG.getMinDamage(), CONFIG.getMaxDamage());
        float scaledDamage = baseDamage * (1.0f + (enemy.getAttackDamage() * 0.1f));

        // Generate different patterns based on phase
        switch (patternPhase) {
            case 0:
                generatePhantomNeedles(bullets, enemy, playerX, playerY, arenaWidth, arenaHeight, scaledDamage);
                break;
            case 1:
                generateNeedleBarrage(bullets, enemy, playerX, playerY, arenaWidth, arenaHeight, scaledDamage);
                break;
            case 2:
                generateCrossStitch(bullets, enemy, playerX, playerY, arenaWidth, arenaHeight, scaledDamage);
                break;
        }

        // Cycle to next phase
        patternPhase = (patternPhase + 1) % PHASES;

        return bullets;
    }

    /**
     * Updates player tracking for predictive aiming
     */
    private void updatePlayerTracking(float playerX, float playerY) {
        playerTrackCycle++;

        if (playerTrackCycle >= 5) {
            // Calculate velocity based on position change
            float velX = (playerX - lastPlayerPos.x) * 0.2f;
            float velY = (playerY - lastPlayerPos.y) * 0.2f;

            // Update velocity with smoothing
            playerVelocity.x = playerVelocity.x * 0.7f + velX * 0.3f;
            playerVelocity.y = playerVelocity.y * 0.7f + velY * 0.3f;

            // Reset tracking cycle
            playerTrackCycle = 0;
            lastPlayerPos.set(playerX, playerY);
        }
    }

    /**
     * Generates phantom needles that appear with clear telegraphing
     */
    private void generatePhantomNeedles(List<Bullet> bullets, Enemy enemy,
                                      float playerX, float playerY,
                                      float arenaWidth, float arenaHeight,
                                      float damage) {
        // Create phantom needles that appear suddenly with telegraphing
        int numNeedles = MathUtils.random(4, 7);

        // Predict player position
        float predictedX = playerX + (playerVelocity.x);
        float predictedY = playerY + (playerVelocity.y);

        for (int i = 0; i < numNeedles; i++) {
            // Calculate random position around the predicted player position
            float offset = MathUtils.random(100f, 200f);
            float angle = MathUtils.random(MathUtils.PI2);

            float spawnX = predictedX + MathUtils.cos(angle) * offset;
            float spawnY = predictedY + MathUtils.sin(angle) * offset;

            // Direction pointing to predicted player position
            float dirX = predictedX - spawnX;
            float dirY = predictedY - spawnY;

            // Normalize direction
            float length = (float)Math.sqrt(dirX * dirX + dirY * dirY);
            dirX /= length;
            dirY /= length;

            // Create phantom needle bullet
            Bullet bullet = new Bullet(
                damage,
                spawnX, spawnY,
                dirX * NEEDLE_SPEED,
                dirY * NEEDLE_SPEED,
                NEEDLE_SIZE,
                new Color(0.2f, 0.6f, 0.9f, 0.95f),
                false
            );

            // Style the bullet as a needle
            stylizeNeedleBullet(bullet, 0.7f + (i % 2) * 0.2f);

            // Add longer telegraph time for visibility
            bullet.enableTelegraphing(TELEGRAPH_TIME * 0.5f, 0.25f);

            bullets.add(bullet);
        }
    }

    /**
     * Generates a barrage of needles that form a grid wall around the player,
     * with both horizontal and vertical needles creating a challenging barrier
     */
    private void generateNeedleBarrage(List<Bullet> bullets, Enemy enemy,
                                     float playerX, float playerY,
                                     float arenaWidth, float arenaHeight,
                                     float damage) {
        // Grid parameters - always centered on player
        int gridLines = MathUtils.random(5, 7);
        float gridSize = 350f; // Size of the grid
        float lineSpacing = gridSize / (gridLines - 1);

        // Add slight offset to grid center for unpredictability
        float offsetX = MathUtils.random(-30f, 30f);
        float offsetY = MathUtils.random(-30f, 30f);

        // Grid center based on player position with offset
        float gridCenterX = playerX + offsetX;
        float gridCenterY = playerY + offsetY;

        // Calculate grid boundaries
        float gridStartX = gridCenterX - gridSize / 2;
        float gridStartY = gridCenterY - gridSize / 2;

        // Calculate predicted player position for targeting
        float predictedX = playerX + (playerVelocity.x * 2f);
        float predictedY = playerY + (playerVelocity.y * 2f);

        // Calculate direction from grid center to predicted player position
        float dirToPredictedX = predictedX - gridCenterX;
        float dirToPredictedY = predictedY - gridCenterY;
        float distToPredicted = (float)Math.sqrt(dirToPredictedX * dirToPredictedX + dirToPredictedY * dirToPredictedY);

        // Normalize direction if distance is not zero
        if (distToPredicted > 0.001f) {
            dirToPredictedX /= distToPredicted;
            dirToPredictedY /= distToPredicted;
        } else {
            // Default direction if player is at grid center
            dirToPredictedX = 0;
            dirToPredictedY = 1;
        }

        // Create horizontal grid lines
        for (int i = 0; i < gridLines; i++) {
            float lineY = gridStartY + (i * lineSpacing);

            // Skip a random line to create a potential escape route
            if (MathUtils.randomBoolean(0.3f) && i > 0 && i < gridLines - 1) {
                continue;
            }

            // Calculate perpendicular direction to player movement for horizontal line
            float dirX = dirToPredictedY; // Perpendicular to Y component of player direction
            // Normalize the direction to ensure consistent speed (-1 or 1)
            dirX = dirX > 0 ? 1 : -1;

            // Spawn position on either left or right side depending on player position
            float spawnX = dirX > 0 ? gridStartX : gridStartX + gridSize;

            // Create horizontal needle
            Bullet bullet = new Bullet(
                damage,
                spawnX, lineY,
                dirX * NEEDLE_SPEED * 1.2f, // Multiply speed by 1.2 for consistent fast movement
                0,
                NEEDLE_SIZE,
                new Color(0.3f, 0.7f, 0.9f, 0.9f),
                false
            );

            // Style and telegraph
            stylizeNeedleBullet(bullet, 0.8f);
            bullet.enableTelegraphing(TELEGRAPH_TIME, 0.2f);

            bullets.add(bullet);
        }

        // Create vertical grid lines
        for (int i = 0; i < gridLines; i++) {
            float lineX = gridStartX + (i * lineSpacing);

            // Skip a random line to create a potential escape route
            // Don't skip the same index as horizontal to avoid too large gaps
            if (MathUtils.randomBoolean(0.3f) && i > 0 && i < gridLines - 1) {
                continue;
            }

            // Calculate perpendicular direction to player movement for vertical line
            float dirY = -dirToPredictedX; // Perpendicular to X component of player direction
            // Normalize the direction to ensure consistent speed (-1 or 1)
            dirY = dirY > 0 ? 1 : -1;

            // Spawn position on either top or bottom side depending on player position
            float spawnY = dirY > 0 ? gridStartY : gridStartY + gridSize;

            // Create vertical needle
            Bullet bullet = new Bullet(
                damage,
                lineX, spawnY,
                0,
                dirY * NEEDLE_SPEED * 1.2f, // Multiply speed by 1.2 for consistent fast movement
                NEEDLE_SIZE,
                new Color(0.2f, 0.6f, 0.95f, 0.9f), // Slightly different color
                false
            );

            // Style and telegraph
            stylizeNeedleBullet(bullet, 0.8f);
            bullet.enableTelegraphing(TELEGRAPH_TIME * 0.7f, 0.2f);

            bullets.add(bullet);
        }

        // Add a few diagonal needles for additional challenge
        if (MathUtils.randomBoolean(0.6f)) {
            addDiagonalNeedles(bullets, gridCenterX, gridCenterY, gridSize, damage);
        }
    }

    /**
     * Adds diagonal needles to the grid pattern for additional challenge
     */
    private void addDiagonalNeedles(List<Bullet> bullets, float centerX, float centerY,
                                  float gridSize, float damage) {
        int diagonalCount = MathUtils.random(2, 4);
        float radius = gridSize / 2;

        for (int i = 0; i < diagonalCount; i++) {
            // Calculate random angle for diagonal
            float angle = MathUtils.random(MathUtils.PI2);

            // Spawn on the perimeter of the grid
            float spawnX = centerX + MathUtils.cos(angle) * radius;
            float spawnY = centerY + MathUtils.sin(angle) * radius;

            // Direction pointing through the grid center
            float dirX = -MathUtils.cos(angle);
            float dirY = -MathUtils.sin(angle);

            // Create diagonal needle
            Bullet bullet = new Bullet(
                damage,
                spawnX, spawnY,
                dirX * NEEDLE_SPEED * 1.9f,
                dirY * NEEDLE_SPEED * 1.9f,
                NEEDLE_SIZE - 0.5f,
                new Color(0.4f, 0.5f, 1.0f, 0.9f),
                false
            );

            // Style the bullet
            stylizeNeedleBullet(bullet, 0.9f);
            bullet.enableTelegraphing(TELEGRAPH_TIME * 0.8f, 0.15f);

            bullets.add(bullet);
        }
    }

    /**
     * Generates a cross-stitch pattern of needles that create danger zones
     * oriented towards player movement to make them harder to dodge
     */
    private void generateCrossStitch(List<Bullet> bullets, Enemy enemy,
                                   float playerX, float playerY,
                                   float arenaWidth, float arenaHeight,
                                   float damage) {
        // Create a cross-stitch pattern centered near player
        int numCrosses = MathUtils.random(3, 5);
        float patternRadius = 150f;

        // Predict player's future position based on velocity for targeting
        float predictedX = playerX + (playerVelocity.x * 3f);
        float predictedY = playerY + (playerVelocity.y * 3f);

        // Calculate player's movement direction (if any)
        float playerDirX = playerVelocity.x;
        float playerDirY = playerVelocity.y;
        float playerSpeed = (float)Math.sqrt(playerDirX * playerDirX + playerDirY * playerDirY);

        // Default rotation based on player movement, or use pattern timer if player isn't moving
        float baseRotation;
        if (playerSpeed > 0.05f) {
            // Normalize player direction and get angle
            playerDirX /= playerSpeed;
            playerDirY /= playerSpeed;
            baseRotation = (float)Math.atan2(playerDirY, playerDirX);
        } else {
            // Use pattern timer for rotation if player isn't moving much
            baseRotation = patternTimer;
        }

        // Slight offset from predicted player position for slight variation
        float patternCenterX = predictedX + MathUtils.random(-30f, 30f);
        float patternCenterY = predictedY + MathUtils.random(-30f, 30f);

        for (int i = 0; i < numCrosses; i++) {
            // Calculate position for this cross - biased toward player's movement direction
            float angleOffset = (i * MathUtils.PI2 / numCrosses);
            float angle;

            // Bias cross positions toward player's movement corridor
            if (playerSpeed > 0.05f) {
                // Create a narrower spread of crosses in player's movement direction
                // This makes some crosses appear in front of and behind the player's path
                angle = baseRotation + angleOffset * 0.7f;
            } else {
                angle = baseRotation + angleOffset;
            }

            float crossX = patternCenterX + MathUtils.cos(angle) * patternRadius * 0.7f;
            float crossY = patternCenterY + MathUtils.sin(angle) * patternRadius * 0.7f;

            // Determine cross orientation - some aligned with player movement, some perpendicular
            float crossRotation;
            if (i % 2 == 0 && playerSpeed > 0.05f) {
                // Align cross with player movement direction
                crossRotation = baseRotation;
            } else {
                // Rotate cross to be more challenging to dodge
                crossRotation = baseRotation + MathUtils.PI / 4 + (i * MathUtils.PI / 8);
            }

            // Create two needles in a cross pattern with specific orientation
            generateOrientedCross(bullets, crossX, crossY, crossRotation, playerX, playerY, damage);
        }

        // Add extra crosses directly in player's path if they're moving
        if (playerSpeed > 0.05f) {
            // Calculate positions directly in front of player's predicted path
            float leadDistance = patternRadius * 1.2f;
            float frontX = predictedX + playerDirX * leadDistance;
            float frontY = predictedY + playerDirY * leadDistance;

            // Add 1-2 crosses directly in player's path
            int extraCrosses = MathUtils.random(1, 2);
            for (int i = 0; i < extraCrosses; i++) {
                float offsetX = frontX + MathUtils.random(-30f, 30f);
                float offsetY = frontY + MathUtils.random(-30f, 30f);

                // Orient these crosses to be harder to dodge (45° to player direction)
                float rotationOffset = MathUtils.PI / 4 * (i % 2 == 0 ? 1 : -1);
                generateOrientedCross(bullets, offsetX, offsetY, baseRotation + rotationOffset, playerX, playerY, damage);
            }
        }
    }

    /**
     * Helper method to generate a cross pattern with specific orientation
     */
    private void generateOrientedCross(List<Bullet> bullets, float centerX, float centerY,
                                    float rotation, float playerX, float playerY, float damage) {
        // Create two crossed lines at the specified rotation
        for (int j = 0; j < 2; j++) {
            float armAngle = rotation + (j * MathUtils.PI / 2); // Perpendicular arms

            // Two spawn positions for the cross arm
            for (int k = 0; k < 2; k++) {
                float spawnDistance = 50f;
                float spawnAngle = armAngle + k * MathUtils.PI; // Opposite sides

                // Calculate spawn position
                float spawnX = centerX + MathUtils.cos(spawnAngle) * spawnDistance;
                float spawnY = centerY + MathUtils.sin(spawnAngle) * spawnDistance;

                // Direction through the center of the cross
                float dirX = MathUtils.cos(spawnAngle);
                float dirY = MathUtils.sin(spawnAngle);

                // Create cross-stitch needle
                Bullet bullet = new Bullet(
                    damage,
                    spawnX, spawnY,
                    dirX * NEEDLE_SPEED * 0.8f,
                    dirY * NEEDLE_SPEED * 0.8f,
                    NEEDLE_SIZE - 0.5f,
                    new Color(0.1f, 0.5f, 0.95f, 0.9f),
                    false
                );

                // Style the bullet as a cross-stitch needle
                stylizeNeedleBullet(bullet, 0.9f);

                // Telegraph cross-stitch pattern
                bullet.enableTelegraphing(TELEGRAPH_TIME * 0.9f, 0.15f);

                bullets.add(bullet);
            }
        }
    }

    /**
     * Applies consistent needle visual styling to bullets
     */
    private void stylizeNeedleBullet(Bullet bullet, float intensity) {
        bullet.setShape(Bullet.Shape.SQUARE);
        bullet.setTrailLength(12);
        bullet.setGlowing(true);
        bullet.setGlowIntensity(intensity);
    }

    @Override
    public String getPatternName() {
        return CONFIG.getPatternDescription();
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
