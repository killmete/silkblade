package swu.cp112.silkblade.pattern.spiritoftheloom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.util.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Spirit of the Loom's attack pattern
 * A single pattern that evolves in complexity based on stage group:
 * - Stage Group 1 (31-33): Basic thread weaving pattern
 * - Stage Group 2 (34-36): Adds intersecting threads and thread convergence
 * - Stage Group 3 (37-39): Adds thread binding and cosmic weave mechanics
 */
public class LoomPattern implements EnemyAttackPattern {
    // Pattern config varies by stage group
    private final AttackPatternConfig CONFIG;

    // Stage group determines pattern complexity (1-3)
    private final int stageGroup;

    // Pattern phase trackers
    private int currentPhase = 0;
    private int spawnCount = 0;
    private static final int SPAWNS_PER_PHASE = 2; // Number of spawns before changing phase
    private float patternTimer = 0f;

    // Thread pattern properties
    private static final int BASE_THREAD_COUNT = 5;
    private static final float THREAD_SPEED = 600f;

    // Thread weaving properties
    private float weavingAngle = 0f;
    private float weavingSpeed = 30f; // degrees per second

    // Pattern rotation to prevent repetitive patterns
    private float globalPatternRotation = 0f;
    private static final float BASE_PATTERN_ROTATION_INCREMENT = 23f; // Prime-ish number for less obvious repetition

    // Thread convergence properties (group 2+)
    private boolean isConverging = false;
    private float convergenceTimer = 0f;
    private static final float CONVERGENCE_DELAY = 1.5f;

    // Thread binding properties (group 3)
    private boolean isBoundingActive = false;
    private float boundingTimer = 0f;
    private static final float BINDING_DURATION = 2.5f;

    // Cosmic weave properties (group 3)
    private boolean isCosmicWeaveActive = false;
    private float cosmicWeaveTimer = 0f;
    private float cosmicRotation = 0f;
    private static final float COSMIC_WEAVE_DURATION = 3.0f;

    // Healing properties
    private static final float HEALING_CHANCE = 0.08f;

    public LoomPattern(int stageGroup) {
        this.stageGroup = MathUtils.clamp(stageGroup, 1, 3);

        // Configure based on stage group
        switch (this.stageGroup) {
            case 3: // Most complex (stages 37-39)
                CONFIG = new AttackPatternConfig(
                    14, 17, 3.2f, 1.25f, 14, 400f, 400f,
                    new Color(0.7f, 0.7f, 1.0f, 1.0f), false,
                    "Cosmic Loom Weave", 2.0f
                );
                break;
            case 2: // Medium complexity (stages 34-36)
                CONFIG = new AttackPatternConfig(
                    11, 14, 2.8f, 1.25f, 14, 380f, 380f,
                    new Color(0.6f, 0.8f, 1.0f, 1.0f), false,
                    "Converging Thread Pattern", 1.8f
                );
                break;
            default: // Basic pattern (stages 31-33)
                CONFIG = new AttackPatternConfig(
                    9, 11, 2.4f, 0.85f, 12, 350f, 350f,
                    new Color(0.5f, 0.9f, 1.0f, 1.0f), false,
                    "Thread Weaving Pattern", 1.5f
                );
                break;
        }

        GameLogger.logInfo("Created LoomPattern with stage group " + this.stageGroup);
    }

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                       float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Update timers
        float delta = Gdx.graphics.getDeltaTime();
        patternTimer += delta;

        // Increment spawn count and check for phase transition
        spawnCount++;

        // Update global pattern rotation to prevent repetitive patterns
        // Add a stage-dependent increment to create different rotation speeds for different difficulties
        globalPatternRotation += BASE_PATTERN_ROTATION_INCREMENT + (stageGroup * 4.5f);
        globalPatternRotation %= 360f; // Keep it within 0-360 range

        if (spawnCount >= SPAWNS_PER_PHASE) {
            currentPhase = (currentPhase + 1) % 3;
            spawnCount = 0;

            // Reset convergence with phase transition (for group 2+)
            if (stageGroup >= 2) {
                isConverging = false;
                convergenceTimer = 0f;
            }
        }

        // Update phase-specific timers
        if (stageGroup >= 2) {
            convergenceTimer += delta;
        }

        if (stageGroup >= 3) {
            boundingTimer += delta;
            cosmicWeaveTimer += delta;
        }

        // Get player position for targeting
        float playerX = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerX() :
            arenaX + arenaWidth / 2;
        float playerY = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerY() :
            arenaY + arenaHeight / 2;

        // Get arena center coordinates instead of enemy position
        float centerX = arenaX + arenaWidth / 2;
        float centerY = arenaY + arenaHeight / 2;

        // Calculate player direction vector for orientation
        float playerDirX = playerX - centerX;
        float playerDirY = playerY - centerY;
        float playerDist = (float)Math.sqrt(playerDirX * playerDirX + playerDirY * playerDirY);

        // Normalize the player direction vector
        if (playerDist > 0) {
            playerDirX /= playerDist;
            playerDirY /= playerDist;
        } else {
            playerDirX = 0;
            playerDirY = 1;
        }

        // Calculate player angle for pattern orientation
        float playerAngle = MathUtils.atan2(playerDirY, playerDirX) * MathUtils.radiansToDegrees;

        // Calculate attack parameters based on enemy's state
        float damageMultiplier = 1.0f + (enemy.getAttackDamage() * 0.25f);
        float speedMultiplier = 1.0f + (stageGroup * 0.08f); // Higher stages get faster bullets
        float baseSpeed = THREAD_SPEED * speedMultiplier;

        // Get scaled damage
        float minDamage = CONFIG.getMinDamage() * damageMultiplier;
        float maxDamage = CONFIG.getMaxDamage() * damageMultiplier;

        // Update weaving angle
        weavingAngle += weavingSpeed * delta;
        if (weavingAngle >= 360f) {
            weavingAngle -= 360f;
        }

        // Update cosmic rotation (if in stage group 3)
        if (stageGroup >= 3) {
            cosmicRotation += 20f * delta;
            if (cosmicRotation >= 360f) {
                cosmicRotation -= 360f;
            }
        }

        // Check for convergence activation (stage group 2+)
        if (stageGroup >= 2 && !isConverging && convergenceTimer >= CONVERGENCE_DELAY) {
            isConverging = true;
            convergenceTimer = 0f;
        }

        // Check for binding activation (stage group 3)
        if (stageGroup >= 3 && !isBoundingActive && boundingTimer >= BINDING_DURATION) {
            isBoundingActive = true;
            boundingTimer = 0f;
        } else if (stageGroup >= 3 && isBoundingActive && boundingTimer >= BINDING_DURATION / 2) {
            isBoundingActive = false;
        }

        // Check for cosmic weave activation (stage group 3)
        if (stageGroup >= 3 && !isCosmicWeaveActive && cosmicWeaveTimer >= COSMIC_WEAVE_DURATION) {
            isCosmicWeaveActive = true;
            cosmicWeaveTimer = 0f;
        } else if (stageGroup >= 3 && isCosmicWeaveActive && cosmicWeaveTimer >= COSMIC_WEAVE_DURATION / 3) {
            isCosmicWeaveActive = false;
        }

        // Execute patterns based on current phase and stage group
        switch (currentPhase) {
            case 0:
                // Phase 1: Thread Spiral Pattern (base pattern for all phases)
                createThreadSpiralPattern(bullets, centerX, centerY, playerX, playerY, playerAngle,
                                      baseSpeed, minDamage, maxDamage);
                break;

            case 1:
                // Phase 2: Combines Phase 1 and 2 patterns
                if (stageGroup >= 2) {
                    // For stage group 2+, include spiral pattern too
                    createThreadSpiralPattern(bullets, centerX, centerY, playerX, playerY, playerAngle,
                                          baseSpeed * 0.9f, minDamage, maxDamage);
                }

                // Then add parallel pattern
                createParallelThreadPattern(bullets, centerX, centerY, playerX, playerY, playerAngle,
                                         baseSpeed, minDamage, maxDamage);
                break;

            case 2:
                // Phase 3: Combines all patterns based on stage group
                if (stageGroup >= 3) {
                    // For stage group 3, include all patterns
                    createThreadSpiralPattern(bullets, centerX, centerY, playerX, playerY, playerAngle,
                                          baseSpeed * 0.85f, minDamage * 0.85f, maxDamage * 0.85f);

                    createParallelThreadPattern(bullets, centerX, centerY, playerX, playerY, playerAngle,
                                             baseSpeed * 0.85f, minDamage * 0.85f, maxDamage * 0.85f);
                } else if (stageGroup == 2) {
                    // For stage group 2, include spiral pattern and lighter parallel pattern
                    createThreadSpiralPattern(bullets, centerX, centerY, playerX, playerY, playerAngle,
                                          baseSpeed * 0.9f, minDamage * 0.9f, maxDamage * 0.9f);
                }

                // All stage groups have radial pattern in phase 3
                createRadialThreadPattern(bullets, centerX, centerY, playerX, playerY, playerAngle,
                                       baseSpeed, minDamage, maxDamage);

                // Add cosmic weave in stage group 3
                if (stageGroup >= 3 && isCosmicWeaveActive) {
                    createCosmicWeavePattern(bullets, centerX, centerY, playerX, playerY, playerAngle,
                                           baseSpeed * 1.2f, minDamage * 1.5f, maxDamage * 1.5f);
                }
                break;
        }

        // Occasionally spawn healing thread
        if (MathUtils.random() < HEALING_CHANCE) {
            createHealingThread(bullets, centerX, centerY, playerX, playerY, baseSpeed * 0.7f);
        }

        return bullets;
    }

    /**
     * Creates a spiral pattern of thread bullets
     */
    private void createThreadSpiralPattern(List<Bullet> bullets, float centerX, float centerY,
                                      float playerX, float playerY, float playerAngle, float speed,
                                      float minDamage, float maxDamage) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Calculate thread count based on stage group
        int threadCount = BASE_THREAD_COUNT + (stageGroup * 2);

        // Add a randomized angle offset for variation between spawns (12-25 degrees)
        float randomOffset = MathUtils.random(12f, 25f);

        // Include the global pattern rotation to prevent repetitive patterns
        // Now also include player angle for player-oriented direction
        float totalRotation = weavingAngle + randomOffset + globalPatternRotation + playerAngle;

        // Create threads in a spiral pattern
        for (int i = 0; i < threadCount; i++) {
            // Calculate angle with weaving effect and global rotation
            float angle = (360f / threadCount) * i;
            angle += totalRotation; // Apply combined rotation

            // Convert angle to radians
            float radians = angle * MathUtils.degreesToRadians;

            // Calculate direction
            float dirX = MathUtils.cos(radians);
            float dirY = MathUtils.sin(radians);

            // Create the bullet
            float bulletSize = 4.5f + (stageGroup * 0.5f);
            Color threadColor = new Color(CONFIG.getBulletColor());

            // Vary color slightly based on angle
            threadColor.r += MathUtils.sin(radians * 2) * 0.3f;
            threadColor.g += MathUtils.cos(radians * 3) * 0.3f;
            threadColor.b += MathUtils.sin(radians * 1.5f) * 0.3f;

            Bullet bullet = new Bullet(
                damage,
                centerX + dirX * bulletSize,
                centerY + dirY * bulletSize,
                dirX * speed,
                dirY * speed,
                bulletSize,
                threadColor,
                false
            );

            // Apply visual styling
            styleBullet(bullet, 0);

            // Add telegraphing for better player experience
            bullet.enableTelegraphing(0.8f, 0.2f);

            // Add homing behavior for stage group 2+ if converging
            if (stageGroup >= 2 && isConverging) {
                // Create bullets that will home in after a delay
                bullet = new Bullet(
                    damage,
                    centerX + dirX * bulletSize,
                    centerY + dirY * bulletSize,
                    dirX * speed,
                    dirY * speed,
                    bulletSize,
                    threadColor,
                    false,
                    true, // Homing
                    1.5f, // Duration
                    200f  // Strength
                );

                // Set the homing target
                bullet.updateTarget(playerX, playerY);

                // Still apply visual styling
                styleBullet(bullet, 0);
                bullet.enableTelegraphing(0.7f, 0.2f);
            }

            bullets.add(bullet);
        }
    }

    /**
     * Creates parallel thread patterns that move across the arena
     */
    private void createParallelThreadPattern(List<Bullet> bullets, float centerX, float centerY,
                                        float playerX, float playerY, float playerAngle, float speed,
                                        float minDamage, float maxDamage) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Calculate number of thread lines based on stage group
        int threadLines = 1 + stageGroup;
        int threadsPerLine = 2 + stageGroup;
        float threadSpacing = 30f - (stageGroup * 2); // Closer spacing for higher stages

        // Add a randomized angle offset for variation between spawns (12-25 degrees)
        float randomOffset = MathUtils.random(12f, 25f) * MathUtils.degreesToRadians;

        // Include the global pattern rotation to keep patterns evolving
        float globalRotation = globalPatternRotation * MathUtils.degreesToRadians;
        // Include player angle for orientation
        float playerRotation = playerAngle * MathUtils.degreesToRadians;
        float totalRotation = randomOffset + globalRotation + playerRotation;

        // Create two sets of thread lines (horizontal and vertical)
        for (int orientation = 0; orientation < 2; orientation++) {
            boolean isHorizontal = (orientation == 0);

            for (int lineIndex = 0; lineIndex < threadLines; lineIndex++) {
                // Calculate line offset with global pattern rotation
                float baseLineOffset = ((lineIndex / (float)(threadLines - 1)) - 0.5f) * 200f;

                // Rotate the positions around the center based on totalRotation
                float rotatedLineX, rotatedLineY;
                if (isHorizontal) {
                    rotatedLineX = 0;
                    rotatedLineY = baseLineOffset;
                } else {
                    rotatedLineX = baseLineOffset;
                    rotatedLineY = 0;
                }

                // Apply rotation to line position
                float rotatedX = rotatedLineX * MathUtils.cos(totalRotation) - rotatedLineY * MathUtils.sin(totalRotation);
                float rotatedY = rotatedLineX * MathUtils.sin(totalRotation) + rotatedLineY * MathUtils.cos(totalRotation);

                for (int threadIndex = 0; threadIndex < threadsPerLine; threadIndex++) {
                    // Calculate thread position along the line
                    float threadOffset = ((threadIndex / (float)(threadsPerLine - 1)) - 0.5f) * threadsPerLine * threadSpacing;

                    float threadPosX, threadPosY;
                    if (isHorizontal) {
                        threadPosX = threadOffset;
                        threadPosY = 0;
                    } else {
                        threadPosX = 0;
                        threadPosY = threadOffset;
                    }

                    // Rotate the thread position
                    float rotatedThreadX = threadPosX * MathUtils.cos(totalRotation) - threadPosY * MathUtils.sin(totalRotation);
                    float rotatedThreadY = threadPosX * MathUtils.sin(totalRotation) + threadPosY * MathUtils.cos(totalRotation);

                    // Calculate final positions
                    float startX = centerX + rotatedX + rotatedThreadX;
                    float startY = centerY + rotatedY + rotatedThreadY;

                    // Calculate base velocities with alternating directions
                    float baseVelX, baseVelY;
                    if (isHorizontal) {
                        // Perpendicular to horizontal line
                        baseVelX = 0;
                        baseVelY = speed * (lineIndex % 2 == 0 ? 1 : -1); // Alternate directions
                    } else {
                        // Perpendicular to vertical line
                        baseVelX = speed * (lineIndex % 2 == 0 ? 1 : -1); // Alternate directions
                        baseVelY = 0;
                    }

                    // Apply rotation to velocity
                    float velX = baseVelX * MathUtils.cos(totalRotation) - baseVelY * MathUtils.sin(totalRotation);
                    float velY = baseVelX * MathUtils.sin(totalRotation) + baseVelY * MathUtils.cos(totalRotation);

                    // Create thread bullet
                    float bulletSize = 4.0f + (stageGroup * 0.5f);
                    Color threadColor = new Color(CONFIG.getBulletColor());

                    // Vary color based on line
                    threadColor.r += (lineIndex / (float)threadLines) * 0.4f;
                    threadColor.g -= (lineIndex / (float)threadLines) * 0.3f;
                    threadColor.b += (threadIndex / (float)threadsPerLine) * 0.4f;

                    Bullet bullet = new Bullet(
                        damage,
                        startX,
                        startY,
                        velX,
                        velY,
                        bulletSize,
                        threadColor,
                        false
                    );

                    // Apply visual styling
                    styleBullet(bullet, 1);

                    // Add homing behavior for stage group 2+ if converging
                    if (stageGroup >= 2 && isConverging) {
                        // Create bullets that will home in
                        bullet = new Bullet(
                            damage,
                            startX,
                            startY,
                            velX,
                            velY,
                            bulletSize,
                            threadColor,
                            false,
                            true, // Homing
                            2.0f, // Duration
                            100f  // Strength
                        );

                        // Set the homing target
                        bullet.updateTarget(playerX, playerY);

                        // Still apply visual styling
                        styleBullet(bullet, 1);
                    }
                    bullet.enableTelegraphing(0.7f, 0.15f);
                    bullets.add(bullet);
                }
            }
        }
    }

    /**
     * Creates a radial pattern of threads emanating from the arena edges
     */
    private void createRadialThreadPattern(List<Bullet> bullets, float centerX, float centerY,
                                      float playerX, float playerY, float playerAngle, float speed,
                                      float minDamage, float maxDamage) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Calculate thread count based on stage group
        int threadCount = 3 + (stageGroup * 2);

        // Add a randomized angle offset for variation between spawns (12-25 degrees)
        float randomOffset = MathUtils.random(12f, 25f);

        // Include the global pattern rotation to prevent repetitive patterns
        // Now include player angle to orient the pattern toward player
        float totalRotation = randomOffset + globalPatternRotation + playerAngle;

        // Define edge positions to spawn threads from, include rotation
        float edgeDistance = 180f;
        float rotationRadians = totalRotation * MathUtils.degreesToRadians;

        // Calculate rotated positions for the edges
        float[][] edgePositions = {
            {centerX - edgeDistance * MathUtils.cos(rotationRadians),
             centerY - edgeDistance * MathUtils.sin(rotationRadians)}, // Rotated Left
            {centerX + edgeDistance * MathUtils.cos(rotationRadians),
             centerY + edgeDistance * MathUtils.sin(rotationRadians)}, // Rotated Right
            {centerX + edgeDistance * MathUtils.sin(rotationRadians),
             centerY - edgeDistance * MathUtils.cos(rotationRadians)}, // Rotated Bottom
            {centerX - edgeDistance * MathUtils.sin(rotationRadians),
             centerY + edgeDistance * MathUtils.cos(rotationRadians)}, // Rotated Top
        };

        // For each edge position
        for (int edge = 0; edge < 4; edge++) {
            for (int i = 0; i < threadCount; i++) {
                // Calculate spawn position on the edge
                float spawnX = edgePositions[edge][0];
                float spawnY = edgePositions[edge][1];

                // Calculate angle with variation, incorporating total rotation
                float baseAngle;
                if (edge == 0) baseAngle = 0f + totalRotation; // From rotated left, aim right
                else if (edge == 1) baseAngle = 180f + totalRotation; // From rotated right, aim left
                else if (edge == 2) baseAngle = 90f + totalRotation; // From rotated bottom, aim up
                else baseAngle = 270f + totalRotation; // From rotated top, aim down

                // Add variation to the angle
                float angleSpread = 30f + (stageGroup * 10f);
                float angle = baseAngle + ((i / (float)(threadCount - 1)) - 0.5f) * angleSpread;

                // Add weaving effect to the angle
                angle += MathUtils.sin(weavingAngle * MathUtils.degreesToRadians + i * 0.5f) * 15f;

                // Convert angle to radians
                float radians = angle * MathUtils.degreesToRadians;

                // Calculate direction
                float dirX = MathUtils.cos(radians);
                float dirY = MathUtils.sin(radians);

                // Create the bullet
                float bulletSize = 4.0f + (stageGroup * 0.4f);
                Color threadColor = new Color(CONFIG.getBulletColor());

                // Vary color based on edge and index
                threadColor.r += (edge / 4f) * 0.4f;
                threadColor.g += (i / (float)threadCount) * 0.3f;
                threadColor.b += MathUtils.sin(angle * MathUtils.degreesToRadians) * 0.3f;

                Bullet bullet = new Bullet(
                    damage,
                    spawnX,
                    spawnY,
                    dirX * speed,
                    dirY * speed,
                    bulletSize,
                    threadColor,
                    false
                );

                // Apply visual styling
                styleBullet(bullet, 2);

                // Add telegraphing
                bullet.enableTelegraphing(0.6f, 0.15f);

                // Add homing behavior for stage group 2+ if converging
                if (stageGroup >= 2 && isConverging) {
                    // Create bullets that will home in
                    bullet = new Bullet(
                        damage,
                        spawnX,
                        spawnY,
                        dirX * speed,
                        dirY * speed,
                        bulletSize,
                        threadColor,
                        false,
                        true, // Homing
                        1.0f, // Duration
                        150f  // Strength
                    );

                    // Set the homing target
                    bullet.updateTarget(playerX, playerY);

                    // Still apply visual styling
                    styleBullet(bullet, 2);
                    bullet.enableTelegraphing(0.7f, 0.15f);
                }

                bullets.add(bullet);
            }
        }
    }

    /**
     * Creates a complex cosmic weave pattern (only for stage group 3)
     */
    private void createCosmicWeavePattern(List<Bullet> bullets, float centerX, float centerY,
                                     float playerX, float playerY, float playerAngle, float speed,
                                     float minDamage, float maxDamage) {
        if (stageGroup < 3) return; // Only available in stage group 3

        float damage = MathUtils.random(minDamage, maxDamage);

        // Create two intersecting rings of threads
        int threadCount = 8;
        float radius = 80f;

        // Add a randomized angle offset for variation between spawns (12-25 degrees)
        float randomOffset = MathUtils.random(12f, 25f);

        // Include the global pattern rotation to prevent repetitive patterns
        // Now include player angle in the rotation to orient toward player
        float totalRotation = cosmicRotation + randomOffset + globalPatternRotation + playerAngle;

        // Create the first ring centered on the enemy
        for (int i = 0; i < threadCount; i++) {
            float angle = (360f / threadCount) * i + totalRotation;
            float radians = angle * MathUtils.degreesToRadians;

            // Calculate spawn position in a circle around the enemy
            float spawnX = centerX + MathUtils.cos(radians) * radius;
            float spawnY = centerY + MathUtils.sin(radians) * radius;

            // Calculate direction toward the player
            float dx = playerX - spawnX;
            float dy = playerY - spawnY;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);

            float dirX, dirY;
            if (dist > 0) {
                dirX = dx / dist;
                dirY = dy / dist;
            } else {
                dirX = MathUtils.cos(radians);
                dirY = MathUtils.sin(radians);
            }

            // Create the bullet
            Color threadColor = new Color(0.5f, 0.5f, 1.0f, 1.0f);

            // Vary color based on angle
            threadColor.r = 0.3f + MathUtils.cos(radians * 2) * 0.3f;
            threadColor.g = 0.3f + MathUtils.sin(radians * 3) * 0.3f;
            threadColor.b = 0.8f + MathUtils.cos(radians) * 0.2f;

            // Use the homing constructor to create bullets that will explode
            Bullet bullet = new Bullet(
                damage,
                spawnX,
                spawnY,
                dirX * speed * 1.2f,
                dirY * speed * 1.2f,
                6.0f,
                threadColor,
                false,
                true,  // Enable homing
                1.2f,  // Homing duration
                300f   // Homing strength
            );

            // Set shape and styling
            bullet.setShape(Bullet.Shape.STAR);
            bullet.setTrailLength(35);
            bullet.setGlowing(true);
            bullet.setGlowLayers(4);
            bullet.startSpinning(180f);

            // Set the homing target
            bullet.updateTarget(playerX, playerY);

            // Simple telegraph
            bullet.enableTelegraphing(1.2f, 0.2f);

            bullets.add(bullet);

            // For the highest stage group, create additional bullets
            if (stageGroup == 3) {
                // Add 4 smaller bullets for stage group 3
                for (int j = 0; j < 4; j++) {
                    // Use player angle to orient the pattern toward player
                    float subAngle = (360f / 4) * j + totalRotation * 0.7f;
                    float subRadians = subAngle * MathUtils.degreesToRadians;

                    float subDirX = MathUtils.cos(subRadians);
                    float subDirY = MathUtils.sin(subRadians);

                    Bullet subBullet = new Bullet(
                        damage * 0.7f,
                        spawnX,
                        spawnY,
                        subDirX * speed * 0.8f,
                        subDirY * speed * 0.8f,
                        4.0f,
                        new Color(threadColor).lerp(Color.WHITE, 0.3f),
                        false
                    );

                    // Style the sub-bullets
                    subBullet.setShape(Bullet.Shape.DIAMOND);
                    subBullet.setTrailLength(20);
                    subBullet.setGlowing(true);
                    subBullet.startSpinning(220f);

                    bullets.add(subBullet);
                }
            }
        }
    }

    /**
     * Creates a healing thread that moves toward the player
     */
    private void createHealingThread(List<Bullet> bullets, float centerX, float centerY,
                                float playerX, float playerY, float speed) {
        // Calculate player direction angle
        float playerDirX = playerX - centerX;
        float playerDirY = playerY - centerY;
        float playerDist = (float)Math.sqrt(playerDirX * playerDirX + playerDirY * playerDirY);
        float playerAngle = 0f;

        // Get player angle for consistent spawning
        if (playerDist > 0) {
            playerAngle = MathUtils.atan2(playerDirY, playerDirX) * MathUtils.radiansToDegrees;
        }

        // Choose random position near the arena edge
        // Incorporate player angle to ensure healing threads spawn around player orientation
        float angle = (MathUtils.random(360f) + playerAngle) * MathUtils.degreesToRadians;
        float distance = 150f;

        float spawnX = centerX + MathUtils.cos(angle) * distance;
        float spawnY = centerY + MathUtils.sin(angle) * distance;

        // Calculate direction toward player
        float dx = playerX - spawnX;
        float dy = playerY - spawnY;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);

        float dirX = dx / dist;
        float dirY = dy / dist;

        // Create the healing thread
        Bullet healingThread = new Bullet(
            10f, // Healing amount (positive for healing)
            spawnX,
            spawnY,
            dirX * speed * 0.6f,
            dirY * speed * 0.6f,
            5.0f,
            new Color(0.3f, 0.9f, 0.3f, 1.0f),
            true // Is healing
        );

        // Add special effects for healing thread
        healingThread.setShape(Bullet.Shape.HEART);
        healingThread.setTrailLength(20);
        healingThread.setGlowing(true);
        healingThread.setGlowLayers(5);
        healingThread.setRotation(45f); // Orient the heart

        // Make healing thread home toward player
        healingThread.updateTarget(playerX, playerY);

        bullets.add(healingThread);
    }

    /**
     * Applies visual styling to bullets based on the phase
     */
    private void styleBullet(Bullet bullet, int phaseType) {
        // Base styling for all bullets
        bullet.setTrailLength(25);
        bullet.setGlowing(true);
        bullet.setGlowLayers(3);
        bullet.setAutoRotate(true);

        // Phase-specific styling
        switch (phaseType) {
            case 0: // Spiral pattern
                bullet.setShape(Bullet.Shape.DIAMOND);
                bullet.setRotationSpeed(100f);
                break;

            case 1: // Parallel pattern
                bullet.setShape(Bullet.Shape.SQUARE);
                break;

            case 2: // Radial pattern
                bullet.setShape(Bullet.Shape.TRIANGLE);
                bullet.setRotationSpeed(120f);
                break;
        }
    }

    @Override
    public String getPatternName() {
        switch (stageGroup) {
            case 3:
                return "Cosmic Loom Weave";
            case 2:
                return "Converging Thread Pattern";
            default:
                return "Thread Weaving Pattern";
        }
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
