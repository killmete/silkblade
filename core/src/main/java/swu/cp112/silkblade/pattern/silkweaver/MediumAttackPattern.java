package swu.cp112.silkblade.pattern.silkweaver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.entity.enemy.SilkWeaver;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.screen.StageSelectionScreen;
import swu.cp112.silkblade.util.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * A medium difficulty attack pattern for the Silk Weaver that fires bullets in more complex patterns
 * with behavior that changes based on the stage level:
 * - Phase 1 (Stages 11-13): Cross pattern shots from the center
 * - Phase 2 (Stages 14-16): Spiral pattern with increased speed
 * - Phase 3 (Stages 17-19): Grid pattern plus homing bullets
 */
public class MediumAttackPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        5, 6, 3.5f, 0.95f, 15, 350f, 350f, Color.WHITE, true,
        "Medium Weaver Attack", 1.5f
    );

    private float spawnTimer = 0f;
    private static final float SPAWN_INTERVAL = 0.7f;

    // Chance to spawn a healing orb in Phase 3
    private static final float HEALING_ORB_CHANCE = 0.2f; // 20% chance per wave

    // Track rotation angle for spiral patterns
    private float rotationAngle = 0f;
    private static final float ROTATION_SPEED = 40f; // Increased for faster rotation

    // Track pattern rotation for cross pattern
    private float crossPatternRotation = 0f;
    private static final float CROSS_ROTATION_INCREMENT = 22f; // Degrees to rotate the cross pattern each spawn

    // Track spiral pattern rotation
    private float spiralPatternRotation = 0f;
    private static final float SPIRAL_ROTATION_INCREMENT = 30f; // Degrees to rotate spiral each spawn

    // Phase 3 pattern control
    private float lastGridSpawn = 0f;
    private float gridSpawnInterval = 1.5f; // Shorter interval between grid patterns
    private float lastStarSpawn = 0f;
    private float starSpawnInterval = 1.5f; // Interval between star patterns
    private boolean useGridWall = true; // Toggle between grid and stars
    private boolean useSixPointedStar = true; // Toggle between 5 and 6 pointed stars
    private float starRotation = 0f; // Rotation for star patterns
    private static final float STAR_ROTATION_INCREMENT = 15f; // Rotation increment for stars
    private boolean isFirstSpawn = true; // Track if this is the first spawn
    private float patternTimer = 0f; // Dedicated timer for patterns to ensure proper timing

    // Telegraphing timers
    private static final float TELEGRAPH_DURATION = 0.6f; // How long to telegraph - reduced to keep game flowing

    // Phase 3 crossfire pattern control
    private float lastHorizontalSpawn = 0f;
    private float lastVerticalSpawn = 0f;
    private float horizontalSpawnInterval = 0.3f;
    private float verticalSpawnInterval = 0.3f;

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

        // Get the current stage number
        int stageNumber = 11; // Default to stage 11

        // First, check if this is a SilkWeaver, which has direct stage info
        if (enemy instanceof SilkWeaver) {
            stageNumber = ((SilkWeaver) enemy).getStage();
        } else {
            // As a fallback, try to get the stage from the StageSelectionScreen
            stageNumber = StageSelectionScreen.getCurrentChallengingStage();
            if (stageNumber <= 0 || stageNumber < 11) {
                // If no stage info available, determine phase based on enemy's attack damage
                int enemyAttackDamage = enemy.getAttackDamage();
                if (enemyAttackDamage >= 15) {
                    stageNumber = 17; // Phase 3
                } else if (enemyAttackDamage >= 12) {
                    stageNumber = 14; // Phase 2
                } else {
                    stageNumber = 11; // Phase 1
                }
            }
        }

        // Determine phase based on stage number
        int phase;
        int bulletCount;
        float bulletSpeed;
        float bulletSize;

        // Phase determination based on stage number
        if (stageNumber >= 17 && stageNumber <= 19) {
            // Phase 3 (Stages 17-19)
            phase = 3;
            bulletCount = 8;
            bulletSpeed = 300f;
            bulletSize = 11f;
        } else if (stageNumber >= 14 && stageNumber <= 16) {
            // Phase 2 (Stages 14-16)
            phase = 2;
            bulletCount = 3;
            bulletSpeed = 270f;
            bulletSize = 10f;
        } else {
            // Phase 1 (Stages 11-13 or any other stage defaulting to phase 1)
            phase = 1;
            bulletCount = 4;
            bulletSpeed = 240f;
            bulletSize = 9f;
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

        // Update rotation angle - for spiral patterns
        rotationAngle += ROTATION_SPEED * Gdx.graphics.getDeltaTime();
        if (rotationAngle > 360f) {
            rotationAngle -= 360f;
        }

        // Update spawnTimer for patterns - now using proper delta time
        spawnTimer += Gdx.graphics.getDeltaTime();

        // Update dedicated pattern timer as well - redundant safety measure
        patternTimer += Gdx.graphics.getDeltaTime();

        // Generate attack bullets based on the current phase
        switch (phase) {
            case 3: // Phase 3: Advanced grid and star patterns
                generateCrossfirePattern(bullets, arenaX, arenaY, arenaWidth, arenaHeight,
                                        bulletSpeed, bulletSize, damage, enemyColor, playerX, playerY, phase);
                break;

            case 2: // Phase 2: Spiral pattern
                generateSpiralPattern(bullets, arenaX, arenaY, arenaWidth, arenaHeight,
                                     bulletSpeed, bulletSize, damage, enemyColor, bulletCount, playerX, playerY, phase);
                break;

            case 1: // Phase 1: Cross pattern
            default:
                generateCrossPattern(bullets, arenaX, arenaY, arenaWidth, arenaHeight,
                                    bulletSpeed, bulletSize, damage, enemyColor, bulletCount, playerX, playerY, phase);
                break;
        }

        // In Phase 3, potentially spawn a healing orb
        if (phase == 3 && MathUtils.random() < HEALING_ORB_CHANCE) {
            // Random position near the arena edge
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float radius = Math.min(arenaWidth, arenaHeight) * 0.9f;

            float healX = arenaX + (arenaWidth / 2) + MathUtils.cos(angle) * radius;
            float healY = arenaY + (arenaHeight / 2) + MathUtils.sin(angle) * radius;

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
                20f, // Amount of healing (more than SilkWraith)
                healX,
                healY,
                dirX * 130f, // Slower speed for healing bullets
                dirY * 130f,
                16f, // Larger size
                new Color(0.4f, 1.0f, 0.5f, 1.0f), // Brighter green color
                true // Set isHealing to true
            );

            // Apply styling
            healingBullet.setShape(Bullet.Shape.CIRCLE);
            healingBullet.setTrailLength(25);
            healingBullet.setGlowing(true);
            healingBullet.setGlowLayers(12);
            healingBullet.setGlowIntensity(0.25f);

            bullets.add(healingBullet);
        }

        return bullets;
    }

    /**
     * Generate a cross pattern of bullets aimed toward the player with randomized offset
     */
    private void generateCrossPattern(List<Bullet> bullets, float arenaX, float arenaY,
                                     float arenaWidth, float arenaHeight, float speed,
                                     float size, float damage, Color color,
                                     int count, float playerX, float playerY, int phase) {
        // Center of the arena
        float centerX = arenaX + arenaWidth / 2;
        float centerY = arenaY + arenaHeight / 2;

        // Calculate angle to player
        float playerAngle = MathUtils.atan2(playerY - centerY, playerX - centerX) * MathUtils.radiansToDegrees;
        if (playerAngle < 0) {
            playerAngle += 360f;
        }

        // Increment the rotation slightly clockwise each time
        crossPatternRotation = (crossPatternRotation + CROSS_ROTATION_INCREMENT) % 360f;

        // Create lines with specified count
        for (int i = 0; i < count; i++) {
            // Calculate the angle for this line, based on player direction plus offset
            // Each line is evenly distributed around the player angle with added crossPatternRotation
            float lineAngle = playerAngle + (i * 360f / count) + crossPatternRotation;

            // Add randomized offset to each line (±15 degrees)
            float randomOffset = MathUtils.random(-4f, 4f);
            lineAngle += randomOffset;

            float lineRadians = lineAngle * MathUtils.degreesToRadians;

            // Calculate direction vector for this line
            float dirX = MathUtils.cos(lineRadians);
            float dirY = MathUtils.sin(lineRadians);

            // Create bullets along the line
            for (int j = 1; j <= 1; j++) { // Two bullets per line, one in each direction
                // Direction multiplier (-1 or 1)
                float direction = 1;

                // Calculate spawn distance from center based on index
                float distance = Math.min(arenaWidth, arenaHeight) * 0.05f * j;

                // Calculate position
                float posX = centerX + (dirX * distance);
                float posY = centerY + (dirY * distance);

                // Calculate velocity (shooting outward in player's direction with offset)
                float velX = dirX * speed * direction;
                float velY = dirY * speed * direction;

                // Create bullet
                createBullet(bullets, posX, posY, velX, velY,
                            size, damage, color, false, phase, playerX, playerY);
            }
        }
    }

    /**
     * Generate a spiral pattern of bullets
     */
    private void generateSpiralPattern(List<Bullet> bullets, float arenaX, float arenaY,
                                      float arenaWidth, float arenaHeight, float speed,
                                      float size, float damage, Color color,
                                      int count, float playerX, float playerY, int phase) {
        // Center of the arena
        float centerX = arenaX + arenaWidth / 2;
        float centerY = arenaY + arenaHeight / 2;

        // Increment spiral pattern rotation each time this is called
        spiralPatternRotation = (spiralPatternRotation + SPIRAL_ROTATION_INCREMENT) % 360f;

        // Number of arms in the spiral
        int arms = 4;

        // Calculate angle to player for targeting
        float playerAngle = 0f;
        if (playerX != centerX || playerY != centerY) {
            // Calculate direction to player
            float dx = playerX - centerX;
            float dy = playerY - centerY;

            // Convert to angle in degrees
            playerAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
            // Ensure positive angle
            if (playerAngle < 0) {
                playerAngle += 360f;
            }
        }

        // Create spiral arms
        for (int arm = 0; arm < arms; arm++) {
            // For arm 0, use the player angle instead of uniform distribution
            float armAngle;
            if (arm == 0) {
                // This arm will target the player directly
                armAngle = playerAngle;
            } else {
                // Distribute remaining arms evenly
                // Skip the player-targeting arm and distribute the others evenly
                float baseAngle = rotationAngle + spiralPatternRotation;
                float angleStep = 360f / (arms - 1); // Distribute remaining arms
                armAngle = baseAngle + ((arm - 1) * angleStep);
            }

            for (int i = 0; i < count; i++) {
                // Calculate angle for this bullet
                float bulletAngle = armAngle + (i * 10f); // Spread bullets along the arm

                // Convert to radians
                float radians = bulletAngle * MathUtils.degreesToRadians;

                // Calculate distance from center (increases with index to create spiral)
                float distance = (i * 15f);

                // Calculate position
                float bulletX = centerX + MathUtils.cos(radians) * distance;
                float bulletY = centerY + MathUtils.sin(radians) * distance;

                // Calculate velocity - make bullets move outward from center
                float velX = MathUtils.cos(radians) * speed;
                float velY = MathUtils.sin(radians) * speed;

                // Create bullet with special effects for Phase 2
                Bullet bullet = new Bullet(
                    damage,
                    bulletX, bulletY,
                    velX, velY,
                    size,
                    arm == 0 ? new Color(1f, 0.3f, 0.3f, 1f) : color, // Red for player-targeting arm
                    false // Not healing
                );

                // Apply phase-specific behavior
                applyPhaseBehavior(bullet, phase, false, playerX, playerY);

                // Special styling for spiral pattern
                stylizeBullet(bullet, phase, arm == 0 ? new Color(1f, 0.3f, 0.3f, 1f) : color);

                // Make bullets spin around their path
                bullet.setShape(Bullet.Shape.DIAMOND);
                bullet.setAutoRotate(true);
                bullet.setRotationSpeed(360f); // Fast rotation

                // Special styling for player-targeting arm
                if (arm == 0) {
                    bullet.setGlowing(true);
                    bullet.setGlowIntensity(0.2f);
                    bullet.setTrailLength(45); // Longer trail for player-targeting arm
                }

                bullets.add(bullet);
            }
        }
    }

    /**
     * Generate a crossfire pattern similar to CrossfirePattern
     */
    private void generateCrossfirePattern(List<Bullet> bullets, float arenaX, float arenaY,
                                         float arenaWidth, float arenaHeight, float speed,
                                         float size, float damage, Color color,
                                         float playerX, float playerY, int phase) {
        // Center of the arena
        float centerX = arenaX + arenaWidth / 2;
        float centerY = arenaY + arenaHeight / 2;

        // Timers for pattern spawning with proper intervals
        lastGridSpawn += Gdx.graphics.getDeltaTime();
        lastStarSpawn += Gdx.graphics.getDeltaTime();
        starRotation += STAR_ROTATION_INCREMENT * Gdx.graphics.getDeltaTime();

        // For the first spawn, immediately generate a pattern
        if (isFirstSpawn) {
            // Generate a random pattern
            int randomPattern = MathUtils.random(0, 1);
            switch (randomPattern) {
                case 0:
                    generateGridWallPattern(bullets, arenaX, arenaY, arenaWidth, arenaHeight,
                                          speed, size, damage, color, playerX, playerY);
                    lastGridSpawn = 0f;
                    break;
                case 1:
                    generateDavidStarPattern(bullets, arenaX, arenaY, arenaWidth, arenaHeight,
                                           speed, size, damage, color, playerX, playerY);
                    lastStarSpawn = 0f;
                    break;
            }
            return;
        }

        // Generate patterns based on timers
        if (lastStarSpawn >= starSpawnInterval && lastGridSpawn >= gridSpawnInterval) {

            // Choose a pattern type randomly
            int patternType = MathUtils.random(0, 1);

            switch (patternType) {
                case 0: // Grid Wall Pattern (centered on player)
                    if (lastGridSpawn >= gridSpawnInterval * 2) {
                        generateGridWallPattern(bullets, arenaX, arenaY, arenaWidth, arenaHeight,
                                              speed, size, damage, color, playerX, playerY);
                        lastGridSpawn = 0f;
                    }
                    break;

                case 1: // David Star
                    if (lastStarSpawn >= starSpawnInterval * 2) {
                        generateDavidStarPattern(bullets, arenaX, arenaY, arenaWidth, arenaHeight,
                                               speed, size, damage, color, playerX, playerY);
                        lastStarSpawn = 0f;
                    }
                    break;
            }
        }
    }

    /**
     * Generate a combined grid wall pattern with both horizontal and vertical lines, centered on player
     */
    private void generateGridWallPattern(List<Bullet> bullets, float arenaX, float arenaY,
                                       float arenaWidth, float arenaHeight, float speed,
                                       float size, float damage, Color color,
                                       float playerX, float playerY) {
        // Number of bullets in each line
        int horizontalCount = 5;
        int verticalCount = 5;

        // Calculate spacing to create grid centered on player position
        // This ensures the grid is always centered on where the player is at the time of attack
        float playerRelativeX = playerX - arenaX;
        float playerRelativeY = playerY - arenaY;

        // Calculate offsets to center the grid on player
        float horizontalOffset = (playerRelativeX % (arenaWidth / horizontalCount));
        float verticalOffset = (playerRelativeY % (arenaHeight / verticalCount));

        // Spacing between bullets to create safe spaces
        float spacingY = arenaHeight / (horizontalCount + 1);
        float spacingX = arenaWidth / (verticalCount + 1);

        // Create horizontal lines from both left and right sides
        boolean spawnLeftToRight = MathUtils.randomBoolean();
        float startX = spawnLeftToRight ? arenaX - 50 : arenaX + arenaWidth + 50;
        float directionX = spawnLeftToRight ? 1 : -1;

        // ENSURE CORNERS ARE COVERED: Add extra bullets to cover corners
        // We'll spawn horizontal bullets at top and bottom edges, including corners
        for (int i = 0; i < horizontalCount + 2; i++) {
            // Calculate Y position - include positions at the very top and bottom edges
            float posY;
            if (i == 0) {
                posY = arenaY; // Top edge
            } else if (i == horizontalCount + 1) {
                posY = arenaY + arenaHeight; // Bottom edge
            } else {
                // Calculate Y position based on player's position (with offset)
                posY = arenaY + ((i - 0.5f) * spacingY) + verticalOffset;
                if (posY > arenaY + arenaHeight) posY -= arenaHeight;
                if (posY < arenaY) posY += arenaHeight;
            }

            // Create bullet with telegraphing
            Bullet bullet = new Bullet(
                damage,
                startX,
                posY,
                directionX * speed * 4f,
                0, // No vertical movement
                size,
                color,
                false
            );

            // Apply phase-specific behavior
            applyPhaseBehavior(bullet, 3, false, playerX, playerY);

            // Stylize bullet
            stylizeBullet(bullet, 3, color);

            // Enable telegraphing - shows path across entire arena
            bullet.enableTelegraphing(0.8f, 0.15f);

            // Add to bullets list
            bullets.add(bullet);
        }

        // Create vertical lines from both top and bottom
        boolean spawnBottomToTop = MathUtils.randomBoolean();
        float startY = spawnBottomToTop ? arenaY - 50 : arenaY + arenaHeight + 50;
        float directionY = spawnBottomToTop ? 1 : -1;

        // ENSURE CORNERS ARE COVERED: Add extra bullets to cover corners
        // We'll spawn vertical bullets at left and right edges, including corners
        for (int i = 0; i < verticalCount + 2; i++) {
            // Calculate X position - include positions at the very left and right edges
            float posX;
            if (i == 0) {
                posX = arenaX; // Left edge
            } else if (i == verticalCount + 1) {
                posX = arenaX + arenaWidth; // Right edge
            } else {
                // Calculate X position based on player's position (with offset)
                posX = arenaX + ((i - 0.5f) * spacingX) + horizontalOffset;
                if (posX > arenaX + arenaWidth) posX -= arenaWidth;
                if (posX < arenaX) posX += arenaWidth;
            }

            // Create bullet with telegraphing
            Bullet bullet = new Bullet(
                damage,
                posX,
                startY,
                0, // No horizontal movement
                directionY * speed * 4f,
                size,
                color,
                false
            );

            // Apply phase-specific behavior
            applyPhaseBehavior(bullet, 3, false, playerX, playerY);

            // Stylize bullet
            stylizeBullet(bullet, 3, color);

            // Enable telegraphing - shows path across entire arena
            bullet.enableTelegraphing(0.8f, 0.15f);

            // Add to bullets list
            bullets.add(bullet);
        }

        // CORNER SAFETY FIX: Add diagonal bullets specifically to handle corner camping
        // This will ensure no corner is safe
        if (playerX < arenaX + arenaWidth * 0.2f || playerX > arenaX + arenaWidth * 0.8f ||
            playerY < arenaY + arenaHeight * 0.2f || playerY > arenaY + arenaHeight * 0.8f) {

            // Player is near a corner, add diagonal bullets
            float cornerX, cornerY;
            float diagonalDirectionX, diagonalDirectionY;

            // Determine which corner the player is closest to
            boolean isNearLeft = playerX < arenaX + arenaWidth / 2;
            boolean isNearTop = playerY < arenaY + arenaHeight / 2;

            // Choose the opposite corner to spawn from
            cornerX = isNearLeft ? arenaX + arenaWidth + 50 : arenaX - 50;
            cornerY = isNearTop ? arenaY + arenaHeight + 50 : arenaY - 50;

            // Direction toward player's corner
            diagonalDirectionX = isNearLeft ? -1 : 1;
            diagonalDirectionY = isNearTop ? -1 : 1;

            // Spawn diagonal bullets aimed at the player's corner zone
            for (int i = 0; i < 3; i++) {
                // Add slight offset to cover more of the corner area
                float offsetX = MathUtils.random(-50f, 50f);
                float offsetY = MathUtils.random(-50f, 50f);

                Bullet diagonalBullet = new Bullet(
                    damage,
                    cornerX,
                    cornerY,
                    diagonalDirectionX * speed * 4f,
                    diagonalDirectionY * speed * 4f,
                    size,
                    new Color(1f, 0.3f, 0.3f, 1f), // Red for diagonal bullets
                    false
                );

                // Apply phase-specific behavior
                applyPhaseBehavior(diagonalBullet, 3, false, playerX, playerY);

                // Style diagonal bullets differently
                diagonalBullet.setShape(Bullet.Shape.TRIANGLE);
                diagonalBullet.setAutoRotate(true);
                diagonalBullet.setRotationSpeed(300f);
                diagonalBullet.setTrailLength(40);
                diagonalBullet.setGlowing(true);
                diagonalBullet.setGlowLayers(8);
                diagonalBullet.setGlowIntensity(0.2f);
                diagonalBullet.setDisco(true, true, true);

                // Longer telegraph for diagonal bullets
                diagonalBullet.enableTelegraphing(0.9f, 0.2f);

                // Add to bullets list
                bullets.add(diagonalBullet);
            }
        }
    }

    /**
     * Generate a David Star pattern (six-pointed star formed by two overlapping triangles)
     */
    private void generateDavidStarPattern(List<Bullet> bullets, float arenaX, float arenaY,
                                        float arenaWidth, float arenaHeight, float speed,
                                        float size, float damage, Color color,
                                        float playerX, float playerY) {
        // Center the star on the player's position instead of a random position
        // This makes the pattern more engaging and challenging
        float starCenterX = playerX;
        float starCenterY = playerY;

        // Make the star slightly larger for better visibility
        // But not too large to maintain challenge
        float radius = Math.min(arenaWidth, arenaHeight) * 0.65f; // Increased from 0.25f

        // Starting rotation for the star (use the rotation from the class variable)
        float startAngle = starRotation * MathUtils.degreesToRadians;

        // Define the 6 points of the star on a circle centered on the player
        Vector2[] starPoints = new Vector2[6];
        for (int i = 0; i < 6; i++) {
            float angle = startAngle + i * MathUtils.PI2 / 6;
            starPoints[i] = new Vector2(
                starCenterX + MathUtils.cos(angle) * radius,
                starCenterY + MathUtils.sin(angle) * radius
            );
        }

        // Choose one projectile to target the player directly
        // We'll choose a random one from the even indices (0,2,4) or odd indices (1,3,5)
        // to ensure we don't disrupt the triangle pattern too much
        int targetingBulletIndex = (MathUtils.randomBoolean() ? 0 : 1) + MathUtils.random(0, 2) * 2;

        // Create 6 projectiles that will form the Star of David
        for (int i = 0; i < 6; i++) {
            // Starting point
            float startX = starPoints[i].x;
            float startY = starPoints[i].y;

            // Direction and target calculation
            float targetX, targetY;

            if (i == targetingBulletIndex) {
                // This projectile directly targets the player's current position
                targetX = playerX;
                targetY = playerY;
            } else {
                // Each projectile connects to a point that's 2 positions away (creates the triangles)
                int targetIndex = (i + 2) % 6;
                targetX = starPoints[targetIndex].x;
                targetY = starPoints[targetIndex].y;
            }

            // Calculate direction vector
            float dirX = targetX - startX;
            float dirY = targetY - startY;

            // Normalize the direction vector
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            dirX /= length;
            dirY /= length;

            // Calculate velocity - faster speed for clear star formation
            float velX = dirX * speed * 4f;
            float velY = dirY * speed * 4f;

            // Create a bullet with telegraphing
            Bullet bullet = new Bullet(
                damage, // More damage for player-targeting bullet
                startX,
                startY,
                velX,
                velY,
                size, // Slightly larger for targeting bullet
                // Special color for the targeting bullet
                i == targetingBulletIndex ?
                    new Color(1f, 0.2f, 0.2f, 1f) : // Red for player-targeting bullet
                    (i % 2 == 0 ?
                        new Color(0.4f, 0.6f, 1f, 1f) : // Blue for one triangle
                        new Color(1f, 0.4f, 0.6f, 1f)), // Pink for other triangle
                false
            );

            // No homing behavior for Star of David
            applyPhaseBehavior(bullet, 3, false, playerX, playerY);

            // Stylize the bullet
            bullet.setShape(i == targetingBulletIndex ? Bullet.Shape.STAR : Bullet.Shape.DIAMOND);
            bullet.setAutoRotate(true);
            bullet.setRotationSpeed(i == targetingBulletIndex ? 360f : 240f);
            bullet.setTrailLength(i == targetingBulletIndex ? 75 : 60); // Longer trail for targeting bullet
            bullet.setGlowing(true);
            bullet.setGlowLayers(i == targetingBulletIndex ? 12 : 10);
            bullet.setGlowIntensity(i == targetingBulletIndex ? 0.3f : 0.2f);
            bullet.setDisco(true, true, true);
            bullet.setDiscoSpeed(i == targetingBulletIndex ? 3.5f : 2.5f);

            // Enable telegraphing - longer for the targeting bullet
            bullet.enableTelegraphing(i == targetingBulletIndex ? 1.1f : 0.9f, 0.2f);

            // Add to bullets list
            bullets.add(bullet);
        }
    }

    /**
     * Helper method to create a bullet with appropriate behavior
     */
    private void createBullet(List<Bullet> bullets, float x, float y, float velX, float velY,
                             float size, float damage, Color color, boolean homing,
                             int phase, float playerX, float playerY) {
        // Create the bullet
        Bullet bullet = new Bullet(
            damage,
            x, y,
            velX, velY,
            size,
            color,
            false // Not healing
        );

        // Apply phase-specific behavior
        applyPhaseBehavior(bullet, phase, homing, playerX, playerY);

        // Stylize the bullet based on phase
        stylizeBullet(bullet, phase, color);

        bullets.add(bullet);
    }

    /**
     * Apply behavior modifiers to bullets based on the current phase
     */
    private void applyPhaseBehavior(Bullet bullet, int phase, boolean forceHoming,
                                   float playerX, float playerY) {
        switch (phase) {
            case 3: // Phase 3: Advanced homing with acceleration
                if (forceHoming) {
                    // Stronger homing for grid pattern bullets
                    bullet.enableHoming(1.5f, 600f);
                    bullet.updateTarget(playerX, playerY);

                    // Add acceleration over time
                    final float[] time = {0f};
                    final float accelFactor = 1.3f;

                    bullet.setUpdateCallback(delta -> {
                        time[0] += delta;
                        if (time[0] > 0.5f) { // Start accelerating after 0.5 seconds
                            float currentSpeed = (float) Math.sqrt(
                                bullet.getVelocityX() * bullet.getVelocityX() +
                                bullet.getVelocityY() * bullet.getVelocityY());

                            // Increase speed
                            float newSpeed = currentSpeed * (1 + (accelFactor * delta));

                            // Calculate direction
                            float dirX = bullet.getVelocityX() / currentSpeed;
                            float dirY = bullet.getVelocityY() / currentSpeed;

                            // Set new velocity
                            bullet.setVelocity(dirX * newSpeed, dirY * newSpeed);
                        }
                        return true;
                    });
                }
                break;

            case 2: // Phase 2: Growing bullets
                // Implement growing size using a custom update callback
                final float[] time = {0f};
                final float maxSizeMultiplier = 1.5f;
                final float growDuration = 1.0f;
                final float originalSize = bullet.getSize();

                bullet.setUpdateCallback(delta -> {
                    time[0] = Math.min(time[0] + delta, growDuration);
                    float progress = time[0] / growDuration;
                    bullet.setSize(originalSize * (1 + progress * (maxSizeMultiplier - 1)));
                    return true;
                });
                break;

            case 1: // Phase 1: Simple pulsing
                // Implement pulsing using a custom update callback
                final float[] pulseTime = {0f};
                final float pulseFrequency = 5.0f;
                final float pulseAmplitude = 0.2f;
                final float originalSize1 = bullet.getSize();

                bullet.setUpdateCallback(delta -> {
                    pulseTime[0] += delta;
                    float pulse = MathUtils.sin(pulseTime[0] * pulseFrequency * MathUtils.PI) * pulseAmplitude;
                    bullet.setSize(originalSize1 * (1 + pulse));
                    return true;
                });
                break;
        }
    }

    /**
     * Apply visual styles to bullets based on the phase
     */
    private void stylizeBullet(Bullet bullet, int phase, Color baseColor) {
        // Base styling for all phases
        bullet.setShape(Bullet.Shape.SQUARE); // Web-like appearance
        bullet.setAutoRotate(true);
        bullet.setRotationSpeed(120f);

        // Phase-specific styling
        switch (phase) {
            case 3: // Phase 3 (Levels 17-19)
                bullet.setTrailLength(35);
                bullet.setGlowing(true);
                bullet.setGlowLayers(10);
                bullet.setGlowIntensity(0.1f);
                bullet.setDisco(true, true, true);
                bullet.setDiscoSpeed(2.0f);
                bullet.setDiscoColorRange(0.3f);
                break;

            case 2: // Phase 2 (Levels 14-16)
                bullet.setTrailLength(35);
                bullet.setGlowing(true);
                bullet.setGlowLayers(7);
                bullet.setGlowIntensity(0.3f);
                bullet.setDisco(true, true, false);
                bullet.setDiscoSpeed(1.5f);
                bullet.setDiscoColorRange(0.2f);
                bullet.enableTelegraphing(0.3f, 0.2f);
                break;

            case 1: // Phase 1 (Levels 11-13)
            default:
                bullet.setTrailLength(35);
                bullet.setGlowing(true);
                bullet.setGlowLayers(5);
                bullet.setGlowIntensity(0.2f);
                bullet.setDisco(true, false, false);
                bullet.setDiscoSpeed(1.0f);
                bullet.setDiscoColorRange(0.1f);
                break;
        }
    }

    @Override
    public String getPatternName() {
        return "Weaver's Geometric Assault";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
