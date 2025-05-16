package swu.cp112.silkblade.pattern.goldencocoon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.util.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * PrecisionWebPattern - The first attack pattern for the Golden Cocoon boss
 * Features precise, targeted shots that predict player movement trajectories
 * and require constant movement to avoid.
 *
 * Pattern consists of:
 * 1. A focused web of bullets that predict player's position
 * 2. Precise laser-like threads that cut across the arena
 * 3. Occasional trap webs that form around predicted player positions
 */
public class PrecisionWebPattern implements EnemyAttackPattern {
    // Pattern configuration
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        8, 11, 4.0f, 0.85f, 17, 400f, 400f,
        new Color(1.0f, 0.85f, 0.1f, 1.0f), false,
        "Precision Web", 2.0f
    );

    // Pattern timing with spawn cycles
    private int spawnCycle = 0;
    private static final int MAIN_SPAWN_INTERVAL = 1; // Spawn every 4 cycles
    private int burstCount = 0;
    private static final int MAX_BURSTS = 3;
    private float patternPhase = 0f;

    // Tracking data for player prediction
    private Vector2 lastPlayerPos = new Vector2();
    private Vector2 playerVelocity = new Vector2();
    private int playerTrackCycle = 0;
    private static final int PLAYER_TRACK_INTERVAL = 5; // Track every 5 cycles

    // Thread parameters
    private static final float THREAD_SPEED = 320f;

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

        // Update player tracking and velocity (using cycles)
        updatePlayerTracking(playerX, playerY);

        // Increment spawn cycle
        spawnCycle++;

        // Slowly advance pattern phase (visual effect)
        patternPhase += 0.05f;
        if (patternPhase > MathUtils.PI2) {
            patternPhase -= MathUtils.PI2;
        }

        // Generate bullets based on spawn cycle
        if (spawnCycle % MAIN_SPAWN_INTERVAL == 0) {
            // Time for a new attack

            // Choose between different attack types
            float attackChoice = MathUtils.random(0f, 1f);

            if (attackChoice < 0.4f) {
                // Direct predictive shots
                generatePredictiveShots(bullets, enemy, playerX, playerY, arenaWidth, arenaHeight, arenaX, arenaY);
            } else if (attackChoice < 0.7f) {
                // Precision threads with telegraphing
                generatePrecisionThreads(bullets, enemy, playerX, playerY, arenaWidth, arenaHeight);
            } else {
                // Web trap around player's predicted position
                generateWebTrap(bullets, enemy, playerX, playerY, arenaWidth, arenaHeight);
            }

            // Increment burst counter
            burstCount++;
            if (burstCount >= MAX_BURSTS) {
                burstCount = 0;
                // Add a longer delay after a burst sequence
                spawnCycle -= MAIN_SPAWN_INTERVAL / 2; // Delay next attack by modifying the cycle
            }
        }

        return bullets;
    }

    /**
     * Updates player tracking to calculate velocity and predict movement
     */
    private void updatePlayerTracking(float playerX, float playerY) {
        playerTrackCycle++;

        if (playerTrackCycle >= PLAYER_TRACK_INTERVAL) {
            // Calculate velocity based on position change
            float velX = (playerX - lastPlayerPos.x) * 0.2f; // Scale factor for position change
            float velY = (playerY - lastPlayerPos.y) * 0.2f;

            // Update velocity with smoothing
            playerVelocity.x = playerVelocity.x * 0.5f + velX * 0.5f;
            playerVelocity.y = playerVelocity.y * 0.5f + velY * 0.5f;

            // Reset cycle and update last position
            playerTrackCycle = 0;
            lastPlayerPos.set(playerX, playerY);
        }
    }

    /**
     * Generates bullets that predict where the player will be
     */
    private void generatePredictiveShots(List<Bullet> bullets, Enemy enemy,
                                       float playerX, float playerY,
                                       float arenaWidth, float arenaHeight, float arenaX, float arenaY) {
        // Calculate damage based on enemy's attack damage
        float baseDamage = MathUtils.random(CONFIG.getMinDamage(), CONFIG.getMaxDamage());
        float scaledDamage = baseDamage * (1.0f + (enemy.getAttackDamage() * 0.1f));

        // Predict player position based on current velocity and scaling factor
        float predictionFactor = MathUtils.random(3f, 8f);
        float predictedX = playerX + (playerVelocity.x * predictionFactor);
        float predictedY = playerY + (playerVelocity.y * predictionFactor);

        // Clamp predicted position to arena bounds
        predictedX = MathUtils.clamp(predictedX, arenaX, arenaX + arenaWidth);
        predictedY = MathUtils.clamp(predictedY, arenaY, arenaY + arenaHeight);

        // Center of the arena
        float centerX = arenaX + arenaWidth / 2;
        float centerY = arenaY + arenaHeight / 2;

        // Create bullets at different angles around the predicted position
        int numBullets = 4 + MathUtils.random(2, 4);
        float angleStep = MathUtils.PI2 / numBullets;

        for (int i = 0; i < numBullets; i++) {
            // Calculate angle with some randomness
            float angle = i * angleStep + patternPhase + MathUtils.random(-0.1f, 0.1f);

            // Calculate spawn position at edge of arena
            float radius = (float)Math.sqrt(arenaWidth * arenaWidth + arenaHeight * arenaHeight) / 2;
            float spawnX = centerX + MathUtils.cos(angle) * radius;
            float spawnY = centerY + MathUtils.sin(angle) * radius;

            // Calculate direction toward predicted player position
            float dirX = predictedX - spawnX;
            float dirY = predictedY - spawnY;
            float length = (float)Math.sqrt(dirX * dirX + dirY * dirY);

            // Normalize direction
            dirX /= length;
            dirY /= length;

            // Calculate speed with some variation
            float speed = THREAD_SPEED * MathUtils.random(0.9f, 1.1f);

            // Create bullet with golden color
            Bullet bullet = new Bullet(
                scaledDamage,
                spawnX, spawnY,
                dirX * speed,
                dirY * speed,
                8f,
                new Color(1.0f, 0.85f, 0.2f, 1.0f),
                false
            );

            // Style the bullet as a thin thread
            bullet.setShape(Bullet.Shape.DIAMOND);
            bullet.setTrailLength(25);
            bullet.setGlowing(true);
            bullet.setGlowIntensity(0.3f);
            bullet.enableTelegraphing(0.5f, 0.1f);
            bullets.add(bullet);
        }
    }

    /**
     * Generate precision threads with telegraphing
     */
    private void generatePrecisionThreads(List<Bullet> bullets, Enemy enemy,
                                       float playerX, float playerY,
                                       float arenaWidth, float arenaHeight) {
        // Calculate damage based on enemy's attack damage
        float baseDamage = MathUtils.random(CONFIG.getMinDamage(), CONFIG.getMaxDamage());
        float scaledDamage = baseDamage * (1.0f + (enemy.getAttackDamage() * 0.1f));

        // Calculate predicted player position
        float predictedX = playerX + (playerVelocity.x * 4f);
        float predictedY = playerY + (playerVelocity.y * 4f);

        // Create several thread points
        int numThreads = MathUtils.random(3, 5);
        for (int i = 0; i < numThreads; i++) {
            // Random point near the player's predicted position
            float radius = MathUtils.random(50f, 150f);
            float angle = MathUtils.random(0f, MathUtils.PI2);

            float pointX = predictedX + MathUtils.cos(angle) * radius;
            float pointY = predictedY + MathUtils.sin(angle) * radius;

            // Create several threads radiating from this point
            int numRadial = MathUtils.random(3, 5);
            for (int j = 0; j < numRadial; j++) {
                float threadAngle = (j * MathUtils.PI2 / numRadial) + MathUtils.random(-0.2f, 0.2f);

                // Direction vector
                float dirX = MathUtils.cos(threadAngle);
                float dirY = MathUtils.sin(threadAngle);

                // Create bullet with telegraphing
                Bullet bullet = new Bullet(
                    scaledDamage,
                    pointX, pointY,
                    dirX * THREAD_SPEED * 1.2f,
                    dirY * THREAD_SPEED * 1.2f,
                    7f,
                    new Color(1.0f, 0.9f, 0.3f, 0.9f),
                    false
                );

                // Style the bullet as a thin thread
                bullet.setShape(Bullet.Shape.DIAMOND);
                bullet.setTrailLength(35);
                bullet.setGlowing(true);
                bullet.setGlowIntensity(0.4f);

                // Enable telegraphing
                bullet.enableTelegraphing(0.8f, 0.2f);

                bullets.add(bullet);
            }
        }
    }

    /**
     * Generate a web trap around the player's predicted position
     */
    private void generateWebTrap(List<Bullet> bullets, Enemy enemy,
                               float playerX, float playerY,
                               float arenaWidth, float arenaHeight) {
        // Calculate damage based on enemy's attack damage
        float baseDamage = MathUtils.random(CONFIG.getMinDamage(), CONFIG.getMaxDamage());
        float scaledDamage = baseDamage * (1.0f + (enemy.getAttackDamage() * 0.1f));

        // Predict further ahead for the trap
        float trapCenterX = playerX + (playerVelocity.x * 5f);
        float trapCenterY = playerY + (playerVelocity.y * 5f);

        // Create a circular pattern of bullets around the predicted position
        int numBullets = 12;
        float radius = 70f; // Initial radius
        float angleStep = MathUtils.PI2 / numBullets;

        for (int i = 0; i < numBullets; i++) {
            float angle = i * angleStep;

            // Calculate position on the circle
            float spawnX = trapCenterX + MathUtils.cos(angle) * radius;
            float spawnY = trapCenterY + MathUtils.sin(angle) * radius;

            // Calculate direction toward center of trap (contracting web)
            float dirX = trapCenterX - spawnX;
            float dirY = trapCenterY - spawnY;
            float length = (float)Math.sqrt(dirX * dirX + dirY * dirY);

            // Normalize direction
            dirX /= length;
            dirY /= length;

            // Create bullet with golden color
            Bullet bullet = new Bullet(
                scaledDamage,
                spawnX, spawnY,
                dirX * 400f, // Slower speed for the trap
                dirY * 400f,
                10f,
                new Color(1.0f, 0.9f, 0.0f, 0.8f),
                false
            );

            // Style the bullet
            bullet.setShape(Bullet.Shape.DIAMOND);
            bullet.setTrailLength(15);
            bullet.setGlowing(true);
            bullet.setGlowIntensity(0.3f);

            // Enable slight telegraphing for the trap
            bullet.enableTelegraphing(0.9f, 0.1f);

            bullets.add(bullet);
        }
    }

    @Override
    public String getPatternName() {
        return "Precision Web";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
