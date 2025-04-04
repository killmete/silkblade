package swu.cp112.silkblade.pattern.silkgod;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;

public class TelegraphPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        4, 6, 3.0f, 0.85f, 15, 790f, 400f, Color.YELLOW, false,
        "Divine judgment will pierce your soul", 3.5f // Longer delay of 3.5 seconds for this complex pattern
    );

    private static final Color[] EMPRESS_COLORS = {
        new Color(1f, 0.4f, 0.7f, 1f), // Pink
        new Color(0.5f, 0.3f, 1f, 1f),  // Purple
        new Color(0.3f, 0.7f, 1f, 1f),  // Blue
        new Color(0.2f, 1f, 0.5f, 1f),  // Green
        new Color(1f, 0.8f, 0.2f, 1f)   // Gold
    };
    private float rotationOffset = 0f; // Add this as a class-level variable to track rotation
    private int patternPhase = 0;
    private float spiralTimer = 0f; // Add this to track when to spawn expanding spiral

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                        float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Get player position for bullet targeting
        float targetX = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerX() :
            arenaX + arenaWidth / 2;
        float targetY = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerY() :
            arenaY + arenaHeight / 2;

        // Increment spiral timer
        spiralTimer += 1.0f;

        // Spawn expanding spiral every 1/3 of attack interval
        if (spiralTimer >= 4.0f) {
            generateExpandingSpiral(bullets, arenaX, arenaY, arenaWidth, arenaHeight, enemy);
            spiralTimer = 0f; // Reset timer
        }

        // Cycle through different attack patterns like Empress of Light
        switch (patternPhase % 3) { // Changed from 4 to 3 since spiral is now independent
            case 0:
                // Prismatic Bolts - radial pattern with multiple colors
                generatePrismaticBolts(bullets, arenaX, arenaY, arenaWidth, arenaHeight, targetX, targetY, enemy);
                break;
            case 1:
                // Ethereal Lances - precisely aimed piercing shots
                generateEtherealLances(bullets, arenaX, arenaY, arenaWidth, arenaHeight, targetX, targetY, enemy);
                break;
            case 2:
                // Prismatic Dance - spiral and sun-ray pattern
                generatePrismaticDance(bullets, arenaX, arenaY, arenaWidth, arenaHeight, enemy);
                break;
        }

        patternPhase++;
        return bullets;
    }

    private void generatePrismaticBolts(List<Bullet> bullets, float arenaX, float arenaY,
                                  float arenaWidth, float arenaHeight,
                                  float targetX, float targetY, Enemy enemy) {
        // Create a bullet wall that moves either horizontally, vertically, or diagonally toward the player
        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.15f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float damage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));
        // Decide which pattern to use (horizontal, vertical, or diagonal)
        int patternType = patternPhase % 4; // 0=horizontal, 1=vertical, 2=diagonal top-left to bottom-right, 3=diagonal top-right to bottom-left

        // Number of bullets in the wall
        int bulletCount = 8;
        // Spacing between bullets
        float spacing = 20f;

        // Calculate wall placement (opposite side from the player)
        float wallX, wallY;
        float dirX, dirY;

        if (patternType == 0) { // Horizontal
            // Spawn horizontally arranged wall (place it on left or right of player)
            boolean spawnOnLeft = (targetX > arenaX + arenaWidth / 2);
            wallX = spawnOnLeft ? arenaX : arenaX + arenaWidth;

            // Direction of movement
            dirX = spawnOnLeft ? 1 : -1;
            dirY = 0;

            // Create bullets along the vertical axis
            for (int i = 0; i < bulletCount; i++) {
                // Calculate Y position based on player's Y position with some distribution
                float spawnY = targetY + (i - bulletCount/2) * spacing;

                // Make sure bullets stay within arena bounds
//                spawnY = MathUtils.clamp(spawnY, arenaY + spacing, arenaY + arenaHeight - spacing);

                // Speed of the bullets
                float speed = 2000f;
                float velX = dirX * speed;
                float velY = 0;

                // Select color (using different colors for visual effect)
                Color bulletColor = EMPRESS_COLORS[i % EMPRESS_COLORS.length];

                // Create bullet
                Bullet bolt = new Bullet(
                    damage,
                    wallX,
                    spawnY,
                    velX,
                    velY,
                    10f, // Slightly larger bullets for the wall
                    bulletColor,
                    false
                );
                bolt.setShape(Bullet.Shape.DIAMOND);
                bolt.setAutoRotate(true);
                bolt.setMaxSpinSpeed(5f);
                // Add visual effects
                bolt.setDisco(true, true, false, bulletColor.r, bulletColor.g, bulletColor.b);
                bolt.setDiscoSpeed(4.0f);

                // Enable telegraphing
                bolt.enableTelegraphing(0.62f, 0.3f);

                // Add trail effect
                bolt.setTrailLength(48);
                bolt.setGlowing(true);  // Enable glow
                bolt.setGlowLayers(8);  // Optional: Set custom number of glow layers
                bolt.setGlowIntensity(0.3f);
                bullets.add(bolt);
            }
        } else if (patternType == 1) { // Vertical
            // Spawn vertically arranged wall (place it above or below player)
            boolean spawnOnTop = (targetY > arenaY + arenaHeight / 2);
            wallY = spawnOnTop ? arenaY : arenaY + arenaHeight;

            // Direction of movement
            dirX = 0;
            dirY = spawnOnTop ? 1 : -1;

            // Create bullets along the horizontal axis
            for (int i = 0; i < bulletCount; i++) {
                // Calculate X position based on player's X position with some distribution
                float spawnX = targetX + (i - bulletCount/2) * spacing;

                // Make sure bullets stay within arena bounds
//                spawnX = MathUtils.clamp(spawnX, arenaX + spacing, arenaX + arenaWidth - spacing);

                // Speed of the bullets
                float speed = 2000f;
                float velX = 0;
                float velY = dirY * speed;

                // Select color
                Color bulletColor = EMPRESS_COLORS[i % EMPRESS_COLORS.length];

                // Create bullet
                Bullet bolt = new Bullet(
                    damage,
                    spawnX,
                    wallY,
                    velX,
                    velY,
                    10f,
                    bulletColor,
                    false
                );
                bolt.setShape(Bullet.Shape.DIAMOND);
                bolt.setAutoRotate(true);
                bolt.setMaxSpinSpeed(5f);
                // Add visual effects
                bolt.setDisco(true, true, false, bulletColor.r, bulletColor.g, bulletColor.b);
                bolt.setDiscoSpeed(4.0f);

                // Enable telegraphing
                bolt.enableTelegraphing(0.62f, 0.3f);

                // Add trail effect
                bolt.setTrailLength(48);
                bolt.setGlowing(true);  // Enable glow
                bolt.setGlowLayers(8);  // Optional: Set custom number of glow layers
                bolt.setGlowIntensity(0.3f);
                bullets.add(bolt);
            }
        } else { // Diagonal (patternType 2 or 3)
            boolean topLeftToBottomRight = (patternType == 2);

            // Determine which corners to use based on player position
            boolean useTopCorner, useLeftCorner;

            if (topLeftToBottomRight) {
                // For diagonal top-left to bottom-right
                useTopCorner = (targetY < arenaY + arenaHeight / 2); // If player is in bottom half, spawn at top
                useLeftCorner = (targetX > arenaX + arenaWidth / 2);  // If player is in right half, spawn at left
            } else {
                // For diagonal top-right to bottom-left
                useTopCorner = (targetY < arenaY + arenaHeight / 2); // If player is in bottom half, spawn at top
                useLeftCorner = (targetX < arenaX + arenaWidth / 2);  // If player is in left half, spawn at right
            }

            // Calculate the starting position for the bullet wall
            float startX, startY;
            if (useLeftCorner) {
                startX = arenaX;
            } else {
                startX = arenaX + arenaWidth;
            }

            if (useTopCorner) {
                startY = arenaY;
            } else {
                startY = arenaY + arenaHeight;
            }

            // Create bullets along the diagonal line
            for (int i = 0; i < bulletCount; i++) {
                float t = i / (float)(bulletCount - 1); // Parameter from 0 to 1

                // Calculate spawn position along the wall edge
                float wallLength = Math.min(arenaWidth, arenaHeight) * 0.8f;
                float offsetX = t * wallLength * (topLeftToBottomRight ? 1 : -1);
                float offsetY = t * wallLength;

                float spawnX = startX + (useLeftCorner ? offsetX : -offsetX);
                float spawnY = startY + (useTopCorner ? offsetY : -offsetY);

                // Ensure bullets stay within bounds
                spawnX = MathUtils.clamp(spawnX, arenaX + spacing, arenaX + arenaWidth - spacing);
                spawnY = MathUtils.clamp(spawnY, arenaY + spacing, arenaY + arenaHeight - spacing);

                // Speed of bullets
                float speed = 1500f;

                // Calculate direction toward player instead of using fixed directions
                dirX = targetX - spawnX;
                dirY = targetY - spawnY;

                // Normalize the direction vector
                float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                float normalizedDirX = dirX / length;
                float normalizedDirY = dirY / length;

                float velX = normalizedDirX * speed;
                float velY = normalizedDirY * speed;

                // Select color
                Color bulletColor = EMPRESS_COLORS[i % EMPRESS_COLORS.length];

                // Create bullet
                Bullet bolt = new Bullet(
                    damage,
                    spawnX,
                    spawnY,
                    velX,
                    velY,
                    10f,
                    bulletColor,
                    false
                );
                bolt.setShape(Bullet.Shape.DIAMOND);
                bolt.setAutoRotate(true);
                bolt.setMaxSpinSpeed(5f);
                // Add visual effects
                bolt.setDisco(true, true, false, bulletColor.r, bulletColor.g, bulletColor.b);
                bolt.setDiscoSpeed(4.0f);

                // Enable telegraphing
                bolt.enableTelegraphing(0.62f, 0.3f);

                // Add trail effect
                bolt.setTrailLength(48);
                bolt.setGlowing(true);  // Enable glow
                bolt.setGlowLayers(8);  // Optional: Set custom number of glow layers
                bolt.setGlowIntensity(0.3f);
                bullets.add(bolt);
            }
        }
    }

    private void generateEtherealLances(List<Bullet> bullets, float arenaX, float arenaY,
                                  float arenaWidth, float arenaHeight,
                                  float targetX, float targetY, Enemy enemy) {
        // Create 3 precise beams aimed at the player's position
        float centerX = arenaX + arenaWidth / 2;
        float centerY = targetY + arenaHeight / 5 * 2;  // Spawn from upper part of arena
        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.15f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float damage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));

        for (int i = 0; i < 10; i++) {
            // Calculate position with offset
            float offsetX = MathUtils.random(-120f, 120f);
            float offsetY = MathUtils.random(-30f, 50f);
            float spawnX = centerX + offsetX;
            float spawnY = centerY + offsetY;

            // Calculate direction toward player
            float dirX = targetX - spawnX;
            float dirY = targetY - spawnY;
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);

            // Higher speed than regular bullets
            float speed = 1250f;
            float velX = (dirX / length) * speed;
            float velY = (dirY / length) * speed;

            // Create larger, more damaging projectile
            Bullet lance = new Bullet(
                damage,
                spawnX,
                spawnY,
                velX,
                velY,
                8f,  // Larger size
                EMPRESS_COLORS[2], // Blue
                false
            );
            lance.setShape(Bullet.Shape.HEXAGON);
            lance.setAutoRotate(true);
            lance.setMaxSpinSpeed(5f);
            // Golden particle effect
            lance.setDisco(true, true, false, 1f, 0.85f, 0.2f);
            lance.setDiscoSpeed(3.0f);

            // Longer telegraph for more powerful attack
            lance.enableTelegraphing(1.0f, 0.2f);

            // Add longer trail effect for the lances (they're faster)
            lance.setTrailLength(48);

            // Set bullet to pierce through player (not get destroyed on hit)
            lance.setGlowing(true);  // Enable glow
            lance.setGlowLayers(8);  // Optional: Set custom number of glow layers
            lance.setGlowIntensity(0.3f);
            bullets.add(lance);
        }
    }

    private void generatePrismaticDance(List<Bullet> bullets, float arenaX, float arenaY,
                                        float arenaWidth, float arenaHeight, Enemy enemy) {
        float centerX = arenaX + arenaWidth / 2;
        float centerY = arenaY + arenaHeight / 2;
        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.15f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float damage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));
        // Sun-ray pattern - 12 rays from center
        int numRays = 12;
        float baseSpeed = 720f;

        // Increment rotation offset (e.g., by 15 degrees each time)
        rotationOffset += 15f * MathUtils.degreesToRadians;

        // Create continuously rotating spiral patterns
        for (int i = 0; i < numRays; i++) {
            // Add rotation offset to the original angle calculation
            float angle = (i * 360f / numRays) * MathUtils.degreesToRadians + rotationOffset;
            float velX = MathUtils.cos(angle) * baseSpeed;
            float velY = MathUtils.sin(angle) * baseSpeed;

            // Use alternating colors
            Color rayColor = EMPRESS_COLORS[(i % 5)];

            Bullet ray = new Bullet(
                damage,
                centerX,
                centerY,
                velX,
                velY,
                8f,
                rayColor,
                false
            );
            ray.setShape(Bullet.Shape.CIRCLE);
            ray.startSpinning(550f);
            // Rainbow effect
            ray.setDiscoSpeed(1.5f);

            // Telegraph
            ray.enableTelegraphing(0.75f, 0.3f);

            // Add trail for rays
            ray.setTrailLength(36);
            ray.setGlowing(true);  // Enable glow
            ray.setGlowLayers(8);  // Optional: Set custom number of glow layers
            ray.setGlowIntensity(0.3f);
            bullets.add(ray);
        }

        // Add some smaller trailing bullets with explosion effect
        for (int i = 0; i < 3; i++) {
            float angle = MathUtils.random(0, MathUtils.PI2);
            float dist = MathUtils.random(20, 50);

            Bullet trailBullet = new Bullet(
                damage,
                centerX + MathUtils.cos(angle) * dist,
                centerY + MathUtils.sin(angle) * dist,
                MathUtils.cos(angle) * baseSpeed * 0.8f,
                MathUtils.sin(angle) * baseSpeed * 0.8f,
                7f,
                EMPRESS_COLORS[1], // Purple
                false
            );

            trailBullet.enableTelegraphing(0.7f, 0.3f);

            // Add trail for trailing bullets
            trailBullet.setTrailLength(36);

            // Set explosion effect following the HomingExplosionPattern style
            trailBullet.setOnExplodeCallback(() -> {
                // Create explosion fragment bullets
                int fragmentCount = 8; // Number of fragments per explosion
                float fragmentSpeed = 450f;
                float fragmentDamage = damage * 0.4f;

                // Clear any existing explosion bullets
                trailBullet.getExplosionBullets().clear();

                // Calculate center position for explosion
                float explosionX = trailBullet.getX();
                float explosionY = trailBullet.getY();

                // Create fragments in a circular pattern
                for (int j = 0; j < fragmentCount; j++) {
                    float fragAngle = j * (360f / fragmentCount) * MathUtils.degreesToRadians;
                    float vx = MathUtils.cos(fragAngle) * fragmentSpeed;
                    float vy = MathUtils.sin(fragAngle) * fragmentSpeed;

                    Bullet fragment = new Bullet(
                        fragmentDamage,
                        explosionX,
                        explosionY,
                        vx,
                        vy,
                        8f,
                        14f,
                        EMPRESS_COLORS[3], // Green
                        false
                    );
                    fragment.setShape(Bullet.Shape.HEXAGON);
                    fragment.setAutoRotate(true);
                    // Add disco effect
                    fragment.setDisco(true, true, false, 0.2f, 0.8f, 0.3f);
                    fragment.setDiscoSpeed(2.0f + MathUtils.random(0.0f, 1.0f));

                    // Add trailing effect
                    fragment.setTrailLength(48);
                    fragment.setGlowing(true);  // Enable glow
                    fragment.setGlowLayers(8);  // Optional: Set custom number of glow layers
                    fragment.setGlowIntensity(0.3f);
                    // Add to explosion bullets collection
                    trailBullet.getExplosionBullets().add(fragment);
                }

                // Trigger screen shake if enemy is AbstractEnemy
                if (enemy instanceof AbstractEnemy) {
                    ((AbstractEnemy) enemy).triggerScreenShake(0.3f, 5.0f);
                }
            });

            // Set a timer for the explosion
            trailBullet.startExplosionTimer(1.75f);
            trailBullet.setGlowing(true);  // Enable glow
            trailBullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
            trailBullet.setGlowIntensity(0.3f);
            bullets.add(trailBullet);
        }
    }

    private void generateExpandingSpiral(List<Bullet> bullets, float arenaX, float arenaY,
                                        float arenaWidth, float arenaHeight, Enemy enemy) {
        float centerX = arenaX + arenaWidth / 2;
        float centerY = arenaY + arenaHeight / 2;
        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.15f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float damage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));
        // Number of bullets in each ring
        int bulletsPerRing = 7;

        // Number of rings to spawn at once (can be adjusted)
        int ringsToSpawn = 2;

        // Base speed for outward expansion
        float baseSpeed = 1450f;

        // Speed increment for each ring (outer rings move faster)
        float speedIncrement = 250f;

        // Initial radius for the first ring
        float initialRadius = 50f;

        // Radius increment for each ring
        float radiusIncrement = 75f;

        // Use rotationOffset (the class variable) to make the entire spiral rotate each time it's generated
        // Also include the pattern phase for additional variation
        float initialAngleOffset = rotationOffset + (patternPhase / 4) * MathUtils.PI / 6;

        // Increment rotation offset for next time (similar to generatePrismaticDance)
        rotationOffset += 20f * MathUtils.degreesToRadians;

        for (int ring = 0; ring < ringsToSpawn; ring++) {
            // Calculate this ring's radius and speed
            float ringRadius = initialRadius + (ring * radiusIncrement);
            float ringSpeed = baseSpeed + (ring * speedIncrement);

            // Calculate angle offset for this specific ring
            float ringAngleOffset = initialAngleOffset + (ring * MathUtils.PI / bulletsPerRing);

            // Rotation speed will alternate direction by ring (radians per second)
            // Use a smaller rotation speed to make the spiral effect more subtle
            float rotationSpeed = 0.8f * (ring % 2 == 0 ? 1 : -1);

            for (int i = 0; i < bulletsPerRing; i++) {
                // Calculate bullet position in the ring
                float angle = (i * MathUtils.PI2 / bulletsPerRing) + ringAngleOffset;
                float spawnX = centerX + MathUtils.cos(angle) * ringRadius;
                float spawnY = centerY + MathUtils.sin(angle) * ringRadius;

                // Initially set velocity in the outward direction
                float outwardX = MathUtils.cos(angle) * ringSpeed;
                float outwardY = MathUtils.sin(angle) * ringSpeed;

                // Create the bullet
                Color bulletColor = EMPRESS_COLORS[ring % EMPRESS_COLORS.length];

                Bullet spiralBullet = new Bullet(
                    damage,
                    spawnX,
                    spawnY,
                    outwardX,
                    outwardY,
                    9f,
                    bulletColor,
                    false,
                    true,  // Enable homing
                    2.5f,  // Homing duration
                    550f  // Homing strength
                ) {
                    @Override
                    public void update(float delta, float playerX, float playerY) {
                        // Update the target to the current player position
                        updateTarget(playerX, playerY);
                        super.update(delta, playerX, playerY);
                    }
                };
                spiralBullet.setShape(Bullet.Shape.DIAMOND);
                spiralBullet.setAutoRotate(true);
                // Add visual effects
                spiralBullet.setDisco(true, true, false, bulletColor.r, bulletColor.g, bulletColor.b);
                spiralBullet.setDiscoSpeed(3.0f);

                // Enable telegraph effect
                spiralBullet.enableTelegraphing(0.75f, 0.15f);
                // Add trail for visual effect
                spiralBullet.setTrailLength(40);
                spiralBullet.setGlowing(true);  // Enable glow
                spiralBullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
                spiralBullet.setGlowIntensity(0.3f);
                bullets.add(spiralBullet);
            }
        }
    }

    @Override
    public String getPatternName() {
        return "Galactic Onslaught";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
