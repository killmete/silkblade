package swu.cp112.silkblade.pattern.hundredsilkogre;

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
 * Hundred-Silk Ogre's evolution attack pattern
 * A complex pattern that evolves during combat, becoming more chaotic and unpredictable
 * The patterns are based on primal, ogre-like attacks with silk-themed projectiles
 *
 * - Stage Group 1 (41-43): 2 pattern evolutions
 * - Stage Group 2 (44-46): 3 pattern evolutions
 * - Stage Group 3 (47-49): 4 pattern evolutions
 */
public class OgreEvolutionPattern implements EnemyAttackPattern {
    // Pattern config varies by stage group
    private final AttackPatternConfig CONFIG;

    // Stage group determines complexity (1-3)
    private final int stageGroup;

    // Number of pattern evolutions
    private final int evolutionCount;

    // Current evolution stage
    private int currentEvolution = 0;
    private int evolutionSpawnCounter = 0;
    private static final int SPAWNS_PER_EVOLUTION = 3;

    // Pattern phase trackers
    private int currentPhase = 0;
    private int spawnCount = 0;
    private static final int SPAWNS_PER_PHASE = 2;

    // Global pattern rotation to prevent repetitive patterns
    private float globalPatternRotation = 0f;
    private static final float BASE_PATTERN_ROTATION_INCREMENT = 15f;

    // Maximum bullets allowed per spawn cycle
    private static final int MAX_BULLETS_PER_SPAWN = 20;
    // Cooldowns for secondary patterns to prevent overlap
    private int stompPatternCooldown = 0;
    private int weavePatternCooldown = 0;
    private int chaosSurgePatternCooldown = 0;
    private static final int PATTERN_COOLDOWN_DURATION = 2; // in spawn cycles

    // Ogre Fist pattern properties
    private static final int BASE_FIST_COUNT = 1;
    private static final float FIST_SPEED = 700f;
    private static final float FIST_SIZE = 10f;

    // Ogre Slam pattern properties
    private static final int SLAM_WAVES = 1;
    private static final int SLAM_BULLETS_PER_WAVE = 4;
    private static final float SLAM_SPEED = 800f;

    // Ogre Roar pattern properties
    private static final int ROAR_RINGS = 2;
    private static final int BASE_BULLETS_PER_RING = 4;
    private static final float ROAR_SPEED = 650f;

    // Ogre Stomp pattern properties
    private static final int STOMP_LINES = 2;
    private static final int BULLETS_PER_LINE = 4;
    private static final float STOMP_SPEED = 750f;

    // Silk Weave pattern properties (for higher evolutions)
    private static final int WEAVE_STRANDS = 2;
    private static final int BULLETS_PER_STRAND = 6;
    private static final float WEAVE_SPEED = 600f;

    // Healing properties
    private static final float HEALING_CHANCE = 0.06f;

    public OgreEvolutionPattern(int stageGroup, int evolutionCount) {
        this.stageGroup = MathUtils.clamp(stageGroup, 1, 3);
        this.evolutionCount = evolutionCount;

        // Configure based on stage group
        switch (this.stageGroup) {
            case 3: // Most complex (stages 47-49)
                CONFIG = new AttackPatternConfig(
                    15, 20, 4.5f, 1.2f, 25, 450f, 450f,
                    new Color(0.9f, 0.3f, 0.1f, 1.0f), false,
                    "Transcendent Ogre Fury", 2.5f
                );
                break;
            case 2: // Medium complexity (stages 44-46)
                CONFIG = new AttackPatternConfig(
                    12, 15, 4.0f, 1.15f, 20, 420f, 420f,
                    new Color(0.8f, 0.4f, 0.1f, 1.0f), false,
                    "Greater Ogre Rampage", 2.0f
                );
                break;
            default: // Basic pattern (stages 41-43)
                CONFIG = new AttackPatternConfig(
                    8, 12, 3.0f, 0.95f, 15, 400f, 400f,
                    new Color(0.7f, 0.5f, 0.1f, 1.0f), false,
                    "Ogre Rage", 1.8f
                );
                break;
        }

        GameLogger.logInfo("Created OgreEvolutionPattern with stage group " + this.stageGroup +
                           " and " + this.evolutionCount + " evolutions");
    }

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                       float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Update spawn counters
        spawnCount++;
        evolutionSpawnCounter++;

        // Update pattern cooldowns
        if (stompPatternCooldown > 0) stompPatternCooldown--;
        if (weavePatternCooldown > 0) weavePatternCooldown--;
        if (chaosSurgePatternCooldown > 0) chaosSurgePatternCooldown--;

        // Update global pattern rotation
        globalPatternRotation += BASE_PATTERN_ROTATION_INCREMENT + (stageGroup * 6f) + (currentEvolution * 3f);
        globalPatternRotation %= 360f;

        // Check for phase transition
        if (spawnCount >= SPAWNS_PER_PHASE) {
            currentPhase = (currentPhase + 1) % 3;
            spawnCount = 0;

            // Reset all pattern cooldowns on phase change to ensure fresh patterns
            stompPatternCooldown = 0;
            weavePatternCooldown = 0;
            chaosSurgePatternCooldown = 0;
        }

        // Check for evolution transition
        if (evolutionSpawnCounter >= SPAWNS_PER_EVOLUTION && currentEvolution < evolutionCount - 1) {
            currentEvolution++;
            evolutionSpawnCounter = 0;
            GameLogger.logInfo("OgreEvolutionPattern: Advanced to evolution stage " + (currentEvolution + 1) +
                               " of " + evolutionCount);
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

        // Calculate attack parameters
        float damageMultiplier = 1.0f + (enemy.getAttackDamage() * 0.15f) + (currentEvolution * 0.35f);
        float speedMultiplier = 1.0f + (stageGroup * 0.08f) + (currentEvolution * 0.07f);

        // Get scaled damage
        float minDamage = CONFIG.getMinDamage() * damageMultiplier;
        float maxDamage = CONFIG.getMaxDamage() * damageMultiplier;

        // Generate bullets based on current evolution and phase
        generateEvolutionBullets(bullets, enemyX, enemyY, playerX, playerY,
                              speedMultiplier, minDamage, maxDamage);

        return bullets;
    }

    /**
     * Generate bullets based on current evolution stage
     */
    private void generateEvolutionBullets(List<Bullet> bullets, float enemyX, float enemyY,
                                      float playerX, float playerY, float speedMultiplier,
                                      float minDamage, float maxDamage) {
        List<Bullet> allBullets = new ArrayList<>(); // Temporary collection for all potential bullets
        List<Bullet> priorityBullets = new ArrayList<>(); // For the primary pattern
        List<Bullet> secondaryBullets = new ArrayList<>(); // For secondary patterns
        List<Bullet> healingBullets = new ArrayList<>(); // Always include healing

        // Always generate primary pattern based on current phase
        switch (currentPhase) {
            case 0:
                createOgreFistPattern(priorityBullets, enemyX, enemyY, playerX, playerY,
                                   FIST_SPEED * speedMultiplier, minDamage, maxDamage);
                break;
            case 1:
                createOgreSlamPattern(priorityBullets, enemyX, enemyY, playerX, playerY,
                                   SLAM_SPEED * speedMultiplier, minDamage, maxDamage);
                break;
            case 2:
                createOgreRoarPattern(priorityBullets, enemyX, enemyY, playerX, playerY,
                                   ROAR_SPEED * speedMultiplier, minDamage, maxDamage);
                break;
        }

        // Generate secondary patterns if available based on evolution and cooldowns
        if (currentEvolution >= 1 && stompPatternCooldown <= 0) {
            // Only add stomp pattern if we're not generating too many bullets
            createOgreStompPattern(secondaryBullets, enemyX, enemyY, playerX, playerY,
                                STOMP_SPEED * speedMultiplier, minDamage * 0.8f, maxDamage * 0.8f);
            // Set cooldown
            stompPatternCooldown = PATTERN_COOLDOWN_DURATION;
        }

        if (currentEvolution >= 2 && evolutionCount >= 3 && weavePatternCooldown <= 0) {
            // Skip weave if in same phase as stomp for highest stage groups to prevent overlap
            if (stageGroup < 3 || currentPhase != 0) {
                createSilkWeavePattern(secondaryBullets, enemyX, enemyY, playerX, playerY,
                                   WEAVE_SPEED * speedMultiplier, minDamage * 0.7f, maxDamage * 0.7f);
                // Set cooldown
                weavePatternCooldown = PATTERN_COOLDOWN_DURATION + 1; // Longer cooldown
            }
        }

        if (currentEvolution >= 3 && evolutionCount >= 4 && chaosSurgePatternCooldown <= 0) {
            // Skip chaos surge if we're already generating many bullets
            if (priorityBullets.size() + secondaryBullets.size() < MAX_BULLETS_PER_SPAWN * 0.6) {
                createOgreChaosSurgePattern(secondaryBullets, enemyX, enemyY, playerX, playerY,
                                         FIST_SPEED * speedMultiplier * 1.2f, minDamage * 0.6f, maxDamage * 0.6f);
                // Set cooldown
                chaosSurgePatternCooldown = PATTERN_COOLDOWN_DURATION + 2; // Longest cooldown
            }
        }

        // Always try to add healing bullets
        if (MathUtils.random() < (HEALING_CHANCE + (currentEvolution * 0.03f))) {
            createHealingBullet(healingBullets, enemyX, enemyY, playerX, playerY, FIST_SPEED * 0.6f);
        }

        // Add all primary pattern bullets
        allBullets.addAll(priorityBullets);

        // Calculate how many secondary bullets we can add without exceeding our limit
        int remainingCapacity = MAX_BULLETS_PER_SPAWN - allBullets.size();

        // If we have room for secondary pattern bullets
        if (remainingCapacity > 0 && !secondaryBullets.isEmpty()) {
            // If we have more secondary bullets than capacity, select a subset
            if (secondaryBullets.size() > remainingCapacity) {
                // For very high stage groups, be even more restrictive
                int actualCapacity = stageGroup == 3 ? remainingCapacity / 2 : remainingCapacity;
                // Take a sampling of secondary bullets up to our capacity
                for (int i = 0; i < actualCapacity && i < secondaryBullets.size(); i++) {
                    // Take every Nth bullet to get a distributed sample across all secondary patterns
                    int index = (i * secondaryBullets.size()) / actualCapacity;
                    if (index < secondaryBullets.size()) {
                        allBullets.add(secondaryBullets.get(index));
                    }
                }
            } else {
                // If we have fewer secondary bullets than capacity, add them all
                allBullets.addAll(secondaryBullets);
            }
        }

        // Always add healing bullets - they're good for the player
        allBullets.addAll(healingBullets);

        // Add the final set of bullets to the output list
        bullets.addAll(allBullets);
    }

    /**
     * Ogre Fist pattern - Direct projectiles thrown at the player like punches
     * Fires large, powerful projectiles in the player's direction with slight spread
     */
    private void createOgreFistPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                   float playerX, float playerY, float speed,
                                   float minDamage, float maxDamage) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Calculate fist count based on evolution and stage group
        int fistCount = BASE_FIST_COUNT + stageGroup + currentEvolution;
        // Cap the maximum number of fists to prevent excessive difficulty
        fistCount = Math.min(fistCount, 5);

        // Calculate direction to player
        float dirX = playerX - enemyX;
        float dirY = playerY - enemyY;
        float distance = (float) Math.sqrt(dirX * dirX + dirY * dirY);

        if (distance > 0) {
            dirX /= distance;
            dirY /= distance;
        } else {
            dirX = 0;
            dirY = 1;
        }

        // Create a spread of fist projectiles
        float spreadAngle = 30f + (currentEvolution * 10f); // Reduced spread (was 40f + 20f)

        for (int i = 0; i < fistCount; i++) {
            // Calculate spread offset - center bullet at index fistCount/2 aims directly at player
            float angleOffset = ((i / (float)(fistCount > 1 ? fistCount - 1 : 1)) - 0.5f) * spreadAngle;
            float offsetRadians = angleOffset * MathUtils.degreesToRadians;

            // Add global rotation for variety but with reduced effect
            float rotationRadians = (globalPatternRotation * 0.3f) * MathUtils.degreesToRadians;

            // Combine offset with global rotation
            float totalRadians = offsetRadians + rotationRadians;

            // Rotate direction while maintaining the primary direction toward player
            float rotatedDirX = dirX * MathUtils.cos(totalRadians) - dirY * MathUtils.sin(totalRadians);
            float rotatedDirY = dirX * MathUtils.sin(totalRadians) + dirY * MathUtils.cos(totalRadians);

            // Calculate size and speed variations
            float sizeVariation = 0.8f + (MathUtils.random(0.4f));
            float speedVariation = 0.85f + (MathUtils.random(0.3f));

            // Create fist projectile with ogre-themed color
            Color fistColor = new Color(
                0.7f + MathUtils.random(0.3f), // Red component strong
                0.2f + MathUtils.random(0.3f), // Green component moderate
                0.05f + MathUtils.random(0.15f), // Blue component weak
                1.0f
            );

            Bullet fist = new Bullet(
                damage,
                enemyX,
                enemyY,
                rotatedDirX * speed * speedVariation,
                rotatedDirY * speed * speedVariation,
                FIST_SIZE * sizeVariation,
                fistColor,
                false
            );

            // Apply visual styling
            fist.setShape(Bullet.Shape.CIRCLE);
            fist.setTrailLength(30);
            fist.setGlowing(true);
            fist.setGlowLayers(4);
            fist.startSpinning(150f);

            // Add telegraphing with longer warning time
            fist.enableTelegraphing(0.9f, 0.3f);

            bullets.add(fist);
        }
    }

    /**
     * Ogre Slam pattern - Shockwave of bullets that spreads outward from impact points
     * Creates waves of bullets that spread outward in rings
     */
    private void createOgreSlamPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                   float playerX, float playerY, float speed,
                                   float minDamage, float maxDamage) {
        float damage = MathUtils.random(minDamage * 0.9f, maxDamage * 0.9f);

        // Calculate slam waves based on evolution
        int waveCount = SLAM_WAVES + (currentEvolution / 2);

        // Calculate bullet count per wave based on stage group
        int bulletsPerWave = SLAM_BULLETS_PER_WAVE + (stageGroup * 2);

        // Add global pattern rotation
        float rotationOffset = globalPatternRotation;

        // Create slam impact points
        int impactPoints = 1 + currentEvolution;
        float impactRadius = 80f + (currentEvolution * 40f);

        for (int impact = 0; impact < impactPoints; impact++) {
            // Calculate impact position around the enemy
            float impactAngle = (360f / impactPoints) * impact + rotationOffset;
            float impactRadians = impactAngle * MathUtils.degreesToRadians;

            float impactX = enemyX + MathUtils.cos(impactRadians) * impactRadius;
            float impactY = enemyY + MathUtils.sin(impactRadians) * impactRadius;

            // Create waves from each impact point
            for (int wave = 0; wave < waveCount; wave++) {
                // Delay each wave
                float waveDelay = 1.0f + (wave * 0.2f); // Increased from 0.75f
                float waveSpeed = speed * (0.8f + (wave * 0.1f)); // Each wave slightly faster

                for (int i = 0; i < bulletsPerWave; i++) {
                    // Calculate angle for this bullet with offset for this wave
                    float bulletAngle = (360f / bulletsPerWave) * i + (wave * (360f / bulletsPerWave / 2)) + rotationOffset;
                    float bulletRadians = bulletAngle * MathUtils.degreesToRadians;

                    // Calculate direction based on angle
                    float dirX = MathUtils.cos(bulletRadians);
                    float dirY = MathUtils.sin(bulletRadians);

                    // Create slam wave bullet with earth-toned color
                    Color waveColor = new Color(
                        0.6f + (wave * 0.1f), // Higher red for later waves
                        0.3f + (wave * 0.1f),
                        0.1f,
                        1.0f
                    );

                    // Create the bullet
                    Bullet waveBullet = new Bullet(
                        damage,
                        impactX,
                        impactY,
                        dirX * waveSpeed,
                        dirY * waveSpeed,
                        5.0f + (wave * 0.8f), // Larger bullets in later waves
                        waveColor,
                        false
                    );

                    // Visual styling
                    waveBullet.setShape(Bullet.Shape.DIAMOND);
                    waveBullet.setTrailLength(20 + (wave * 5));
                    waveBullet.setGlowing(true);
                    waveBullet.setGlowLayers(3);
                    waveBullet.setAutoRotate(true);
                    waveBullet.enableTelegraphing(waveDelay, 0.15f);

                    bullets.add(waveBullet);
                }
            }
        }
    }

    /**
     * Ogre Roar pattern - Concentric rings of bullets that expand outward
     * Bullets spread out in multiple rings simultaneously
     */
    private void createOgreRoarPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                  float playerX, float playerY, float speed,
                                  float minDamage, float maxDamage) {
        float damage = MathUtils.random(minDamage * 0.8f, maxDamage * 0.8f);

        // Calculate the number of rings based on evolution - with limit for highest stage group
        int ringCount = stageGroup == 3 ?
                       ROAR_RINGS : // Capped for stage group 3
                       ROAR_RINGS + (currentEvolution);

        // Calculate bullets per ring - with limit for highest stage group
        int bulletsPerRing = stageGroup == 3 ?
                            BASE_BULLETS_PER_RING + stageGroup : // Reduced for stage group 3
                            BASE_BULLETS_PER_RING + (stageGroup * 2) + (currentEvolution * 2);

        // Add global pattern rotation
        float rotationOffset = globalPatternRotation;

        // For each ring
        for (int ring = 0; ring < ringCount; ring++) {
            // Calculate ring-specific parameters
            float ringDelay = 1.0f + (ring * 0.15f); // Increased from 0.7f
            float ringSpeed = speed * (0.7f + (ring * 0.1f));
            float ringOffset = (ring * 360f / ringCount / 2) + rotationOffset;

            // Create bullets for this ring
            for (int i = 0; i < bulletsPerRing; i++) {
                // Calculate angle for this bullet
                float bulletAngle = (360f / bulletsPerRing) * i + ringOffset;
                float bulletRadians = bulletAngle * MathUtils.degreesToRadians;

                // Calculate direction based on angle
                float dirX = MathUtils.cos(bulletRadians);
                float dirY = MathUtils.sin(bulletRadians);

                // Create roar bullet with reddish color (representing anger)
                Color roarColor = new Color(
                    0.8f + (ring * 0.05f),
                    0.2f + MathUtils.sin(bulletRadians) * 0.2f,
                    0.2f + MathUtils.cos(bulletRadians) * 0.2f,
                    1.0f
                );

                // Create the bullet
                Bullet roarBullet = new Bullet(
                    damage,
                    enemyX,
                    enemyY,
                    dirX * ringSpeed,
                    dirY * ringSpeed,
                    4.5f + (ring * 0.5f),
                    roarColor,
                    false
                );

                // Visual styling
                roarBullet.setShape(Bullet.Shape.TRIANGLE);
                roarBullet.setTrailLength(25);
                roarBullet.setGlowing(true);
                roarBullet.setGlowLayers(3);
                roarBullet.startSpinning(200f);
                roarBullet.enableTelegraphing(ringDelay, 0.1f);

                bullets.add(roarBullet);
            }
        }
    }

    /**
     * Ogre Stomp pattern - Creates impact lines that ripple across the arena
     * Powerful shockwaves that move in straight lines from stomp locations
     */
    private void createOgreStompPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                    float playerX, float playerY, float speed,
                                    float minDamage, float maxDamage) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Scale line count based on evolution
        int lineCount = STOMP_LINES + currentEvolution;

        // Bullets per line adjusted by stage group
        int bulletsPerLine = BULLETS_PER_LINE + stageGroup;

        // Add global pattern rotation
        float baseRotation = globalPatternRotation * MathUtils.degreesToRadians;

        // Create stomp lines in multiple directions
        for (int line = 0; line < lineCount; line++) {
            // Calculate line angle with global rotation offset
            float lineAngle = (360f / lineCount) * line + globalPatternRotation;
            float lineRadians = lineAngle * MathUtils.degreesToRadians;

            // Calculate direction vector
            float dirX = MathUtils.cos(lineRadians);
            float dirY = MathUtils.sin(lineRadians);

            // Calculate stomp start position
            float startDistance = 50f + (currentEvolution * 20f);
            float startX = enemyX + dirX * startDistance;
            float startY = enemyY + dirY * startDistance;

            // Create bullets along each stomp line
            for (int i = 0; i < bulletsPerLine; i++) {
                // Calculate bullet position
                float bulletDistance = (i + 1) * 10f;
                float bulletX = startX + dirX * bulletDistance;
                float bulletY = startY + dirY * bulletDistance;

                // Calculate bullet delay based on distance
                float bulletDelay = 0.9f + (i * 0.1f); // Increased from 0.35f

                // Create stomp bullet with brown/earth tones
                Color stompColor = new Color(
                    0.6f + (i / (float)bulletsPerLine) * 0.3f,
                    0.4f - (i / (float)bulletsPerLine) * 0.2f,
                    0.1f,
                    1.0f
                );

                // Create the bullet
                Bullet stompBullet = new Bullet(
                    damage,
                    bulletX,
                    bulletY,
                    dirX * speed * 0.6f, // Slower than other patterns
                    dirY * speed * 0.6f,
                    7.0f, // Larger bullets
                    stompColor,
                    false
                );

                // Visual styling
                stompBullet.setShape(Bullet.Shape.SQUARE);
                stompBullet.setTrailLength(15);
                stompBullet.setGlowing(true);
                stompBullet.setGlowLayers(2);
                stompBullet.setAutoRotate(true);
                stompBullet.setRotationSpeed(90f);
                stompBullet.enableTelegraphing(bulletDelay, 0.1f);

                bullets.add(stompBullet);
            }
        }

        // Add perpendicular ripple effects for higher evolutions
        if (currentEvolution >= 2) {
            // Perpendicular lines for visual effect
            int rippleLines = 2 + currentEvolution;

            for (int line = 0; line < rippleLines; line++) {
                // Calculate perpendicular angle with offset
                float lineAngle = (360f / rippleLines) * line + globalPatternRotation + 45f;
                float lineRadians = lineAngle * MathUtils.degreesToRadians;

                // Create shorter ripple lines
                float dirX = MathUtils.cos(lineRadians);
                float dirY = MathUtils.sin(lineRadians);

                int rippleBullets = BULLETS_PER_LINE / 2;

                for (int i = 0; i < rippleBullets; i++) {
                    // Calculate bullet position
                    float bulletDistance = (i + 1) * 40f;
                    float bulletX = enemyX + dirX * bulletDistance;
                    float bulletY = enemyY + dirY * bulletDistance;

                    // Calculate bullet delay based on distance
                    float bulletDelay = 0.9f + (i * 0.1f); // Increased from 0.4f

                    // Create ripple bullet with blue-green color
                    Color rippleColor = new Color(
                        0.1f,
                        0.5f,
                        0.5f + (i / (float)rippleBullets) * 0.3f,
                        1.0f
                    );

                    // Create the bullet
                    Bullet rippleBullet = new Bullet(
                        damage * 0.7f, // Less damage
                        bulletX,
                        bulletY,
                        dirX * speed * 0.5f, // Slower
                        dirY * speed * 0.5f,
                        5.0f, // Smaller
                        rippleColor,
                        false
                    );

                    // Visual styling
                    rippleBullet.setShape(Bullet.Shape.DIAMOND);
                    rippleBullet.setTrailLength(15);
                    rippleBullet.setGlowing(true);
                    rippleBullet.setGlowLayers(2);
                    rippleBullet.enableTelegraphing(bulletDelay, 0.1f);

                    bullets.add(rippleBullet);
                }
            }
        }
    }

    /**
     * Silk Weave pattern - Creates a web of silk-themed bullets
     * Crisscrossing strands of silk bullets that create a complex maze
     */
    private void createSilkWeavePattern(List<Bullet> bullets, float enemyX, float enemyY,
                                    float playerX, float playerY, float speed,
                                    float minDamage, float maxDamage) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Scale strand count based on evolution - but cap for highest stage group
        int strandCount = stageGroup == 3 ?
                          WEAVE_STRANDS : // Capped for stage group 3
                          WEAVE_STRANDS + (currentEvolution - 1);

        // Calculate bullets per strand - capped for highest stage group
        int bulletsPerStrand = stageGroup == 3 ?
                              BULLETS_PER_STRAND : // Capped for stage group 3
                              BULLETS_PER_STRAND + (stageGroup * 2);

        // Create weaves from multiple point-pairs around the arena
        for (int strand = 0; strand < strandCount; strand++) {
            // Calculate start and end positions for this strand
            float startAngle = (360f / strandCount) * strand + globalPatternRotation;
            float endAngle = startAngle + 180f; // Opposite side

            float startRadians = startAngle * MathUtils.degreesToRadians;
            float endRadians = endAngle * MathUtils.degreesToRadians;

            // Calculate positions at edges of arena
            float edgeDistance = 200f;
            float startX = enemyX + MathUtils.cos(startRadians) * edgeDistance;
            float startY = enemyY + MathUtils.sin(startRadians) * edgeDistance;
            float endX = enemyX + MathUtils.cos(endRadians) * edgeDistance;
            float endY = enemyY + MathUtils.sin(endRadians) * edgeDistance;

            // Calculate bullet spacing along strand
            float dx = endX - startX;
            float dy = endY - startY;

            // Create bullets along strand
            for (int i = 0; i < bulletsPerStrand; i++) {
                // Calculate position along strand
                float t = i / (float)(bulletsPerStrand - 1);
                float bulletX = startX + dx * t;
                float bulletY = startY + dy * t;

                // Calculate direction perpendicular to strand (for movement)
                float perpX = -dy;
                float perpY = dx;
                float perpLen = (float)Math.sqrt(perpX * perpX + perpY * perpY);

                if (perpLen > 0) {
                    perpX /= perpLen;
                    perpY /= perpLen;
                }

                // Alternate direction for crisscross effect
                if (strand % 2 == 0) {
                    perpX = -perpX;
                    perpY = -perpY;
                }

                // Calculate bullet delay based on position along strand
                float bulletDelay = 0.8f + (t * 0.2f); // Increased from 0.35f

                // Create silk strand bullet with color gradients
                Color silkColor = new Color(
                    0.4f + t * 0.4f, // Red component
                    0.7f - t * 0.3f, // Green component
                    0.8f,            // Blue component
                    1.0f
                );

                // Create the bullet
                Bullet silkBullet = new Bullet(
                    damage,
                    bulletX,
                    bulletY,
                    perpX * speed * 0.7f,
                    perpY * speed * 0.7f,
                    3.5f, // Thinner silk threads
                    silkColor,
                    false
                );

                // Visual styling
                silkBullet.setShape(Bullet.Shape.DIAMOND);
                silkBullet.setTrailLength(40); // Long trails for silk effect
                silkBullet.setGlowing(true);
                silkBullet.setGlowLayers(4);
                silkBullet.enableTelegraphing(bulletDelay, 0.1f);

                bullets.add(silkBullet);
            }
        }

        // For highest evolution, add connecting strands - but skip for highest stage group
        if (currentEvolution >= 3 && stageGroup < 3) {
            // Add connecting strands between main strands
            int connectingStrands = strandCount;

            for (int strand = 0; strand < connectingStrands; strand++) {
                // Calculate angles with offset
                float startAngle = (360f / connectingStrands) * strand + globalPatternRotation + 90f;
                float endAngle = startAngle + 180f;

                float startRadians = startAngle * MathUtils.degreesToRadians;
                float endRadians = endAngle * MathUtils.degreesToRadians;

                // Calculate positions at shorter distance
                float edgeDistance = 150f;
                float startX = enemyX + MathUtils.cos(startRadians) * edgeDistance;
                float startY = enemyY + MathUtils.sin(startRadians) * edgeDistance;
                float endX = enemyX + MathUtils.cos(endRadians) * edgeDistance;
                float endY = enemyY + MathUtils.sin(endRadians) * edgeDistance;

                // Calculate bullet spacing
                float dx = endX - startX;
                float dy = endY - startY;

                // Fewer bullets for connecting strands
                int connectingBullets = bulletsPerStrand / 2;

                for (int i = 0; i < connectingBullets; i++) {
                    // Calculate position along strand
                    float t = i / (float)(connectingBullets - 1);
                    float bulletX = startX + dx * t;
                    float bulletY = startY + dy * t;

                    // Calculate direction perpendicular to strand
                    float perpX = -dy;
                    float perpY = dx;
                    float perpLen = (float)Math.sqrt(perpX * perpX + perpY * perpY);

                    if (perpLen > 0) {
                        perpX /= perpLen;
                        perpY /= perpLen;
                    }

                    // Alternate direction
                    if (strand % 2 == 1) {
                        perpX = -perpX;
                        perpY = -perpY;
                    }

                    // Delay based on position
                    float bulletDelay = 0.9f + (t * 0.2f); // Increased from 0.45f

                    // Create connecting strand with different color
                    Color connectColor = new Color(
                        0.2f,            // Red component
                        0.5f + t * 0.4f, // Green component
                        0.8f - t * 0.2f, // Blue component
                        1.0f
                    );

                    // Create the bullet
                    Bullet connectBullet = new Bullet(
                        damage * 0.7f, // Less damage
                        bulletX,
                        bulletY,
                        perpX * speed * 0.6f,
                        perpY * speed * 0.6f,
                        3.0f, // Thinner
                        connectColor,
                        false
                    );

                    // Visual styling
                    connectBullet.setShape(Bullet.Shape.DIAMOND);
                    connectBullet.setTrailLength(30);
                    connectBullet.setGlowing(true);
                    connectBullet.setGlowLayers(3);
                    connectBullet.enableTelegraphing(bulletDelay, 0.1f);
                    bullets.add(connectBullet);
                }
            }
        }
    }

    /**
     * Ogre Chaos Surge pattern - Chaotic burst of bullets in all directions
     * Only appears in the highest evolution stage
     */
    private void createOgreChaosSurgePattern(List<Bullet> bullets, float enemyX, float enemyY,
                                         float playerX, float playerY, float speed,
                                         float minDamage, float maxDamage) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Create a large number of chaotic bullets - but reduced significantly for highest stages
        int chaosBulletCount = stageGroup == 3 ? 4 + stageGroup : 6 + (stageGroup * 3);

        // Create multiple spawn points around the arena
        int spawnPoints = stageGroup == 3 ? 2 : 2 + stageGroup;

        for (int spawn = 0; spawn < spawnPoints; spawn++) {
            // Calculate spawn position
            float spawnAngle = (360f / spawnPoints) * spawn + globalPatternRotation;
            float spawnRadians = spawnAngle * MathUtils.degreesToRadians;

            float spawnDistance = 100f + (MathUtils.random(50f));
            float spawnX = enemyX + MathUtils.cos(spawnRadians) * spawnDistance;
            float spawnY = enemyY + MathUtils.sin(spawnRadians) * spawnDistance;

            int bulletsPerSpawn = chaosBulletCount / spawnPoints;

            // Create chaos bullets from each spawn point
            for (int i = 0; i < bulletsPerSpawn; i++) {
                // Calculate random direction
                float angle = MathUtils.random(360f);
                float radians = angle * MathUtils.degreesToRadians;

                float dirX = MathUtils.cos(radians);
                float dirY = MathUtils.sin(radians);

                // Random speed, size, and color variations
                float speedMultiplier = 0.7f + MathUtils.random(0.6f);
                float size = 4.0f + MathUtils.random(4.0f);

                // Create chaos surge bullet with random color
                Color chaosColor = new Color(
                    MathUtils.random(0.5f, 1.0f), // Random components
                    MathUtils.random(0.3f, 0.8f),
                    MathUtils.random(0.3f, 0.8f),
                    1.0f
                );

                // Create the bullet
                Bullet chaosBullet = new Bullet(
                    damage,
                    spawnX,
                    spawnY,
                    dirX * speed * speedMultiplier,
                    dirY * speed * speedMultiplier,
                    size,
                    chaosColor,
                    false
                );

                // Random visual styling
                Bullet.Shape[] shapes = {
                    Bullet.Shape.CIRCLE,
                    Bullet.Shape.DIAMOND,
                    Bullet.Shape.TRIANGLE,
                    Bullet.Shape.SQUARE,
                    Bullet.Shape.STAR
                };
                chaosBullet.setShape(shapes[MathUtils.random(shapes.length - 1)]);
                chaosBullet.setTrailLength(20 + MathUtils.random(20));
                chaosBullet.setGlowing(true);
                chaosBullet.setGlowLayers(MathUtils.random(2, 5));

                // Random rotation speed
                chaosBullet.startSpinning(MathUtils.random(80f, 250f));

                // Random delay for chaotic appearance
                chaosBullet.enableTelegraphing(1.5f, 0.2f); // Increased from 1.0f, 0.1f
                bullets.add(chaosBullet);
            }
        }
    }

    /**
     * Creates a healing bullet that moves toward the player
     */
    private void createHealingBullet(List<Bullet> bullets, float enemyX, float enemyY,
                                float playerX, float playerY, float speed) {
        // Choose random position around the arena at larger distance
        float angle = MathUtils.random(360f) * MathUtils.degreesToRadians;
        float distance = 180f + MathUtils.random(50f);

        float spawnX = enemyX + MathUtils.cos(angle) * distance;
        float spawnY = enemyY + MathUtils.sin(angle) * distance;

        // Calculate direction toward player
        float dx = playerX - spawnX;
        float dy = playerY - spawnY;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);

        // Normalize direction
        float dirX = dx / dist;
        float dirY = dy / dist;

        // Create healing amount based on evolution stage
        float healAmount = 12f + (stageGroup * 3f) + (currentEvolution * 5f);

        // Create the healing bullet
        Bullet healingBullet = new Bullet(
            healAmount, // Healing amount (positive for healing)
            spawnX,
            spawnY,
            dirX * speed * 0.6f,
            dirY * speed * 0.6f,
            6.0f + (currentEvolution * 0.5f), // Larger at higher evolutions
            new Color(0.3f, 0.9f, 0.3f, 1.0f), // Green for healing
            true // Is healing
        );

        // Visual styling
        healingBullet.setShape(Bullet.Shape.HEART);
        healingBullet.setTrailLength(30);
        healingBullet.setGlowing(true);
        healingBullet.setGlowLayers(5);
        healingBullet.setRotation(45f); // Orient the heart

        // Make it home toward player for improved chance of collection
        healingBullet.updateTarget(playerX, playerY);

        bullets.add(healingBullet);
    }

    @Override
    public String getPatternName() {
        switch (stageGroup) {
            case 3:
                return "Transcendent Ogre Fury";
            case 2:
                return "Greater Ogre Rampage";
            default:
                return "Ogre Rage";
        }
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
