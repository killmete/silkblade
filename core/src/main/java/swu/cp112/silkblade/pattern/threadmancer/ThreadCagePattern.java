package swu.cp112.silkblade.pattern.threadmancer;

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
 * ThreadCagePattern - The first attack pattern for the Threadmancer boss
 * Creates an enclosing cage of threads around the player that requires
 * precise timing and movement to escape. Features telegraphed attacks
 * with clear visual cues.
 *
 * Pattern consists of:
 * 1. Thread cages that form around the player with escape routes
 * 2. Threads that connect to form geometric patterns
 * 3. Pulsating thread intensity for visual cues
 */
public class ThreadCagePattern implements EnemyAttackPattern {
    // Pattern configuration
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        10, 13, 3.5f, 0.75f, 24, 375f, 375f,
        new Color(0.8f, 0.3f, 0.8f, 1.0f), false,
        "Thread Cage", 2.0f
    );

    // Pattern timing
    private int spawnCycle = 0;
    private int patternPhase = 0;
    private static final int PHASES = 3;
    private static final int CAGE_SPAWN_INTERVAL = 1;
    private float patternRotation = 0f;

    // Cage parameters
    private static final int CAGE_SEGMENTS = 8;
    private static final float CAGE_RADIUS = 150f;
    private static final float THREAD_SPEED = 800f;
    private static final float TELEGRAPH_TIME = 0.8f;

    // Homing parameters
    private static final float HOMING_DURATION = 1.5f;
    private static final float HOMING_STRENGTH = 500f;

    // Spiral parameters
    private static final int SPIRAL_RINGS = 4;
    private static final int THREADS_PER_RING = 8;
    private static final float MAX_SPIRAL_RADIUS = 350f;

    // Thread tracking
    private Vector2 lastPlayerPos = new Vector2();
    private Vector2 currentPlayerPos = new Vector2();
    private boolean isClockwise = true;

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

        // Update player position tracking
        lastPlayerPos.set(currentPlayerPos);
        currentPlayerPos.set(playerX, playerY);

        // Increment spawn cycle and update pattern rotation
        spawnCycle++;
        patternRotation += 0.05f;
        if (patternRotation > MathUtils.PI2) {
            patternRotation -= MathUtils.PI2;
        }

        // Switch direction occasionally to keep patterns dynamic
        if (MathUtils.randomBoolean(0.05f)) {
            isClockwise = !isClockwise;
        }

        // Generate bullets based on spawn cycle
        // Calculate damage based on enemy's attack damage
        float baseDamage = MathUtils.random(CONFIG.getMinDamage(), CONFIG.getMaxDamage());
        float scaledDamage = baseDamage * (1.0f + (enemy.getAttackDamage() * 0.1f));

        // Generate different patterns based on phase
        switch (patternPhase) {
            case 0:
                generateHomingThreadCage(bullets, enemy, playerX, playerY, scaledDamage);
                break;
            case 1:
                generateThreadConnector(bullets, enemy, playerX, playerY, scaledDamage);
                break;
            case 2:
                generateExpandingSpiral(bullets, enemy, playerX, playerY, arenaWidth, arenaHeight, scaledDamage);
                break;
        }

        // Cycle to next phase
        patternPhase = (patternPhase + 1) % PHASES;

        return bullets;
    }

    /**
     * Generates a thread cage around the player's position with strategic openings
     * and homing capability to increase threat
     */
    private void generateHomingThreadCage(List<Bullet> bullets, Enemy enemy,
                                   float playerX, float playerY, float damage) {
        // Create a cage of threads around the player with a gap for escape
        int gapSegmentStart = MathUtils.random(0, CAGE_SEGMENTS - 1);
        int gapSize = MathUtils.random(1, 2); // Size of the gap in segments

        for (int i = 0; i < CAGE_SEGMENTS; i++) {
            // Skip segments to create an escape route
            if (i >= gapSegmentStart && i < gapSegmentStart + gapSize) {
                continue;
            }

            // Calculate angle for this segment
            float angle = (i * MathUtils.PI2 / CAGE_SEGMENTS) + patternRotation;

            // Calculate spawn position on the cage perimeter
            float spawnX = playerX + MathUtils.cos(angle) * CAGE_RADIUS;
            float spawnY = playerY + MathUtils.sin(angle) * CAGE_RADIUS;

            // Direction pointing towards player (center of cage)
            float dirX = playerX - spawnX;
            float dirY = playerY - spawnY;

            // Normalize direction
            float length = (float)Math.sqrt(dirX * dirX + dirY * dirY);
            dirX /= length;
            dirY /= length;

            // Create thread bullet with homing capability and appropriate styling
            Bullet bullet = new Bullet(
                damage,
                spawnX, spawnY,
                dirX * THREAD_SPEED,
                dirY * THREAD_SPEED,
                7f,
                new Color(0.8f, 0.3f, 0.8f, 0.9f),
                false
            );

            // Set the homing target to the player
            bullet.updateTarget(playerX, playerY);

            // Style the bullet as a thread
            stylizeThreadBullet(bullet, 0.9f);
            bullet.enableTelegraphing(TELEGRAPH_TIME, 0.2f);

            bullets.add(bullet);

            // Add a second delayed homing thread for additional challenge with slight offset
            if (MathUtils.randomBoolean(0.5f)) {
                float offsetAngle = angle + MathUtils.random(-0.1f, 0.1f);
                float offsetRadius = CAGE_RADIUS * MathUtils.random(0.9f, 1.1f);

                float offsetX = playerX + MathUtils.cos(offsetAngle) * offsetRadius;
                float offsetY = playerY + MathUtils.sin(offsetAngle) * offsetRadius;

                Bullet delayedBullet = new Bullet(
                    damage,
                    offsetX, offsetY,
                    dirX * THREAD_SPEED * 1.2f,
                    dirY * THREAD_SPEED * 1.2f,
                    6f,
                    new Color(0.7f, 0.2f, 0.7f, 0.85f),
                    false,
                    true,
                    HOMING_DURATION * 1.2f,
                    HOMING_STRENGTH * 0.8f
                );

                delayedBullet.updateTarget(playerX, playerY);
                stylizeThreadBullet(delayedBullet, 0.8f);
                delayedBullet.enableTelegraphing(TELEGRAPH_TIME * 1.2f, 0.25f);

                bullets.add(delayedBullet);
            }
        }
    }

    /**
     * Generates threads that connect to form geometric patterns
     */
    private void generateThreadConnector(List<Bullet> bullets, Enemy enemy,
                                      float playerX, float playerY, float damage) {
        // Create a geometric pattern using connected threads
        int numPoints = MathUtils.random(3, 5); // Number of vertices in the pattern
        float baseRadius = CAGE_RADIUS * 0.6f;
        float angleOffset = MathUtils.random(0f, MathUtils.PI2);

        // Calculate points of the geometric pattern
        List<Vector2> patternPoints = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            float angle = (i * MathUtils.PI2 / numPoints) + angleOffset + patternRotation;
            float radius = baseRadius * (1f + MathUtils.random(-0.2f, 0.2f));
            float pointX = playerX + MathUtils.cos(angle) * radius;
            float pointY = playerY + MathUtils.sin(angle) * radius;
            patternPoints.add(new Vector2(pointX, pointY));
        }

        // Connect the points with threads
        for (int i = 0; i < numPoints; i++) {
            Vector2 startPoint = patternPoints.get(i);
            Vector2 endPoint = patternPoints.get((i + 1) % numPoints);

            // Calculate direction
            float dirX = endPoint.x - startPoint.x;
            float dirY = endPoint.y - startPoint.y;

            // Normalize and scale by speed
            float length = (float)Math.sqrt(dirX * dirX + dirY * dirY);
            float speed = THREAD_SPEED * 1.2f;
            dirX = dirX / length * speed;
            dirY = dirY / length * speed;

            // Create thread bullet
            Bullet bullet = new Bullet(
                damage,
                startPoint.x, startPoint.y,
                dirX, dirY,
                6f,
                new Color(0.5f, 0.3f, 0.9f, 0.9f),
                false
            );

            // Style the bullet as a connecting thread
            stylizeThreadBullet(bullet, 0.7f);
            bullet.enableTelegraphing(TELEGRAPH_TIME * 0.8f, 0.15f);

            // Add a potential for the thread to become homing when it reaches its endpoint
            final float[] elapsedTime = { 0f };
            final boolean[] homingInitiated = { false };
            final float travelTime = length / speed;

            // Set callback to enable homing after traveling to endpoint
            bullet.setUpdateCallback(delta -> {
                elapsedTime[0] += delta;

                // When thread has traveled approximately to the endpoint, enable homing if not already
                if (elapsedTime[0] >= travelTime * 0.9f && !homingInitiated[0] && MathUtils.randomBoolean(0.4f)) {
                    homingInitiated[0] = true;

                    // Enable homing behavior
                    bullet.enableHoming(HOMING_DURATION * 0.8f, HOMING_STRENGTH * 0.9f);
                    bullet.updateTarget(playerX, playerY);

                    // Change color slightly to indicate homing behavior
                    bullet.setColor(new Color(0.6f, 0.4f, 1.0f, 0.9f));
                    bullet.setGlowIntensity(0.9f);
                }

                return true; // Continue callback
            });

            bullets.add(bullet);
        }
    }

    /**
     * Generates an expanding spiral pattern that starts in the center and spreads outward
     */
    private void generateExpandingSpiral(List<Bullet> bullets, Enemy enemy,
                                      float playerX, float playerY,
                                      float arenaWidth, float arenaHeight, float damage) {
        // Center of the spiral (mix of player position and arena center for unpredictability)
        float centerX = (playerX + arenaWidth / 2) / 2;
        float centerY = (playerY + arenaHeight / 2) / 2;

        // Create spiral that expands outward from center
        float spiralSpacing = 0.3f; // Controls how tight the spiral is
        float angleIncrement = MathUtils.PI2 / THREADS_PER_RING;

        // Determine speed based on spiral size
        float radialSpeed = THREAD_SPEED * 0.8f;

        // Create multiple rings of spiral with different densities
        for (int ring = 0; ring < SPIRAL_RINGS; ring++) {
            float ringRadius = (MAX_SPIRAL_RADIUS / SPIRAL_RINGS) * ring;

            // Create threads for this ring
            for (int thread = 0; thread < THREADS_PER_RING; thread++) {
                // Calculate initial angle with threading effect
                float baseAngle = thread * angleIncrement + ring * spiralSpacing;
                float angle = baseAngle + patternRotation;

                // Calculate position on the spiral
                float spawnX = centerX + MathUtils.cos(angle) * ringRadius;
                float spawnY = centerY + MathUtils.sin(angle) * ringRadius;

                // Direction radially outward from center
                float dirX = MathUtils.cos(angle);
                float dirY = MathUtils.sin(angle);

                // Create bullet with outward motion
                Bullet bullet = new Bullet(
                    damage,
                    spawnX, spawnY,
                    dirX * radialSpeed,
                    dirY * radialSpeed,
                    5f + ((float) ring / SPIRAL_RINGS) * 2f, // Size increases with radius
                    new Color(0.9f, 0.2f, 0.6f, 0.9f - (0.5f * ring / SPIRAL_RINGS)), // Fade with distance
                    false
                );

                // Style as spiral thread
                bullet.setShape(Bullet.Shape.DIAMOND);
                bullet.setTrailLength(15 + (int)(10 * ring / SPIRAL_RINGS));
                bullet.setGlowing(true);
                bullet.setGlowIntensity(0.8f - (0.3f * ring / SPIRAL_RINGS));

                // Set telegraph time based on ring (outer rings telegraph faster)
                float telegraphTime = TELEGRAPH_TIME * (1f - 0.3f * ring / SPIRAL_RINGS);
                bullet.enableTelegraphing(telegraphTime, 0.1f);

                bullets.add(bullet);
            }
        }

        // Add central burst of homing threads for additional challenge
        if (MathUtils.randomBoolean(0.3f)) {
            generateCentralBurst(bullets, centerX, centerY, playerX, playerY, damage);
        }
    }

    /**
     * Generates a small burst of homing threads from the spiral center
     */
    private void generateCentralBurst(List<Bullet> bullets, float centerX, float centerY,
                                    float playerX, float playerY, float damage) {
        int numBurst = MathUtils.random(3, 5);

        for (int i = 0; i < numBurst; i++) {
            float angle = MathUtils.random(MathUtils.PI2);
            float initialSpeed = THREAD_SPEED * 0.5f;

            // Calculate initial direction
            float dirX = MathUtils.cos(angle);
            float dirY = MathUtils.sin(angle);

            // Create homing bullet with delayed activation
            Bullet bullet = new Bullet(
                damage,
                centerX, centerY,
                dirX * initialSpeed,
                dirY * initialSpeed,
                7f,
                new Color(1.0f, 0.3f, 0.7f, 0.95f),
                false // Homing will be enabled after delay
            );

            // Style the burst bullets
            bullet.setShape(Bullet.Shape.STAR);
            bullet.setTrailLength(20);
            bullet.setGlowing(true);
            bullet.setGlowIntensity(1.0f);
            bullet.enableTelegraphing(TELEGRAPH_TIME * 0.5f, 0.2f);

            // Set update callback to enable homing after a delay
            final float[] timeElapsed = { 0f };
            final float homingDelay = MathUtils.random(0.5f, 1.2f);

            bullet.setUpdateCallback(delta -> {
                timeElapsed[0] += delta;

                // Enable homing after delay
                if (timeElapsed[0] >= homingDelay && !bullet.isHoming()) {
                    bullet.enableHoming(HOMING_DURATION, HOMING_STRENGTH * 1.2f);
                    bullet.updateTarget(playerX, playerY);

                    return true;
                }

                return true;
            });

            bullets.add(bullet);
        }
    }

    /**
     * Applies consistent thread visual styling to bullets
     */
    private void stylizeThreadBullet(Bullet bullet, float intensity) {
        bullet.setShape(Bullet.Shape.DIAMOND);
        bullet.setTrailLength(15);
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
