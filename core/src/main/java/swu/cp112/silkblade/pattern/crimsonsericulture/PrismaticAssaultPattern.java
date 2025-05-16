package swu.cp112.silkblade.pattern.crimsonsericulture;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.screen.StageSelectionScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Prismatic Assault Pattern - creates rainbow colored silk projectiles in
 * complex patterns that target the player.
 * Used in the second phase of the Crimson Sericulture boss battle.
 */
public class PrismaticAssaultPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        16, 22, 1.3f, 1.75f, 18, 800f, 410f,
        new Color(1f, 0.5f, 0.5f, 1f), // Light pink base color before rainbow effect
        false, // No healing for bullets
        "Rainbow projectiles that overwhelm and assault the player from multiple angles.",
        3.0f // Longer end phase delay due to complex patterns
    );

    private float rainbowTimer = 0f;
    private int patternState = 0;
    private float explosionTimer = 0f;

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                         float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Get player position
        float targetX = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerX() :
            arenaX + arenaWidth / 2;
        float targetY = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerY() :
            arenaY + arenaHeight / 2;

        // Get enemy position
        float enemyX = enemy.getX();
        float enemyY = enemy.getY();

        // Calculate damage based on current stage difficulty
        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 0.6f + (enemyAttackDamage * 0.02f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.08f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.08f)) * defenseMultiplier;
        float damage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));

        // Base parameters
        float baseSpeed = 880f;
        float bulletSize = 12f;

        // Increment rainbow timer
        rainbowTimer += 0.1f;
        if (rainbowTimer > 1.0f) rainbowTimer -= 1.0f;

        // Update explosion timer
        explosionTimer += 1.0f;
        if (explosionTimer >= 3.0f) {
            addRandomExplosionBullets(bullets, arenaX, arenaY, arenaWidth, arenaHeight, damage, enemy);
            explosionTimer = 0f;
        }

        // Rotate through pattern states
        patternState = (patternState + 1) % 4;

        // Create attacking bullet patterns
        switch (patternState) {
            case 0:
                // Pattern 1: Prismatic pillars - multiple instances
                for (int i = 0; i < 2; i++) {
                    float offsetX = MathUtils.random(-400f, 400f);
                    float offsetY = MathUtils.random(-350f, 350f);
                    createPrismaticPillarsPattern(bullets, enemyX + offsetX, enemyY + offsetY, targetX, targetY, damage, baseSpeed, bulletSize);
                }
                break;
            case 1:
                // Pattern 2: Rainbow spiral - multiple instances from different origins with wide spread
                float spiral1X = enemyX + MathUtils.random(-400f, 400f);
                float spiral1Y = enemyY + MathUtils.random(-300f, 300f);
                createRainbowSpiralPattern(bullets, spiral1X, spiral1Y, targetX, targetY, damage, baseSpeed, bulletSize);

                // Ensure second spiral is far from first
                float spiral2X = enemyX + MathUtils.random(-400f, 400f);
                float spiral2Y = enemyY + MathUtils.random(-300f, 300f);
                // Ensure some minimum distance between patterns
                while (Math.abs(spiral2X - spiral1X) < 250f && Math.abs(spiral2Y - spiral1Y) < 250f) {
                    spiral2X = enemyX + MathUtils.random(-400f, 400f);
                    spiral2Y = enemyY + MathUtils.random(-300f, 300f);
                }
                createRainbowSpiralPattern(bullets, spiral2X, spiral2Y, targetX, targetY, damage, baseSpeed, bulletSize);
                break;
            case 2:
                // Pattern 3: Prismatic burst - multiple origins with wide distribution
                for (int i = 0; i < 3; i++) {
                    float offsetX = MathUtils.random(-400f, 400f);
                    float offsetY = MathUtils.random(-350f, 350f);
                    createPrismaticBurstPattern(bullets, enemyX + offsetX, enemyY + offsetY, damage, baseSpeed, bulletSize, targetX, targetY);
                }
                break;
            case 3:
                // Pattern 4: Rainbow web - multiple instances aimed at player with wide distribution
                for (int i = 0; i < 2; i++) {
                    float offsetX = MathUtils.random(-350f, 350f);
                    float offsetY = MathUtils.random(-350f, 350f);
                    createRainbowWebPattern(bullets, targetX + offsetX, targetY + offsetY, damage, baseSpeed, bulletSize, targetX, targetY);
                }
                break;
        }

        return bullets;
    }

    private void createPrismaticPillarsPattern(List<Bullet> bullets, float originX, float originY,
                                           float targetX, float targetY, float damage,
                                           float baseSpeed, float bulletSize) {
        int numPillars = 4;
        int bulletsPerPillar = 4;
        float pillarRadius = 140f;

        // Calculate angle to player
        float dx = targetX - originX;
        float dy = targetY - originY;
        float angleToPlayer = MathUtils.atan2(dy, dx);

        for (int i = 0; i < numPillars; i++) {
            float angle = i * (360f / numPillars) + rainbowTimer * 360f;
            float radians = angle * MathUtils.degreesToRadians;

            // Position pillar around the origin point
            float pillarX = originX + MathUtils.cos(radians) * pillarRadius;
            float pillarY = originY + MathUtils.sin(radians) * pillarRadius;

            for (int j = 0; j < bulletsPerPillar; j++) {
                // Create a vertical line of bullets for each pillar
                float offsetY = (j - bulletsPerPillar/2) * 30f;

                // Calculate direction toward player with spread
                float bulletRadians = radians + MathUtils.random(-0.2f, 0.2f);
                float bulletAngleToPlayer = angleToPlayer + MathUtils.random(-0.3f, 0.3f);

                // Mix between direction from pillar and toward player (80% toward player)
                float velX = MathUtils.cos(bulletRadians) * baseSpeed * 0.2f + MathUtils.cos(bulletAngleToPlayer) * baseSpeed * 0.8f;
                float velY = MathUtils.sin(bulletRadians) * baseSpeed * 0.2f + MathUtils.sin(bulletAngleToPlayer) * baseSpeed * 0.8f;

                // Create prismatic bullet
                createRainbowBullet(bullets, pillarX, pillarY + offsetY, velX, velY,
                                 damage, bulletSize + 2, false);
            }
        }
    }

    private void createRainbowSpiralPattern(List<Bullet> bullets, float originX, float originY,
                                         float targetX, float targetY, float damage,
                                         float baseSpeed, float bulletSize) {
        int numSpirals = 2;
        int bulletsPerSpiral = 8;
        float maxRadius = 250f;

        // Calculate angle to player
        float dx = targetX - originX;
        float dy = targetY - originY;
        float angleToPlayer = MathUtils.atan2(dy, dx);

        for (int spiral = 0; spiral < numSpirals; spiral++) {
            float spiralOffset = spiral * 180f + rainbowTimer * 360f;

            for (int i = 0; i < bulletsPerSpiral; i++) {
                // Calculate position in spiral
                float progress = i / (float)bulletsPerSpiral;
                float radius = progress * maxRadius;
                float angle = spiralOffset + progress * 720f; // 2 full rotations
                float radians = angle * MathUtils.degreesToRadians;

                // Mix positions between enemy/player-centered
                float centerX = (spiral % 2 == 0) ? originX : targetX;
                float centerY = (spiral % 2 == 0) ? originY : targetY;

                float x = centerX + MathUtils.cos(radians) * radius;
                float y = centerY + MathUtils.sin(radians) * radius;

                // Aim toward player with tangential component
                float tangentialAngle = radians + MathUtils.PI/2;
                float velX = MathUtils.cos(tangentialAngle) * baseSpeed * 0.3f + MathUtils.cos(angleToPlayer) * baseSpeed * 0.7f;
                float velY = MathUtils.sin(tangentialAngle) * baseSpeed * 0.3f + MathUtils.sin(angleToPlayer) * baseSpeed * 0.7f;

                // Create prismatic bullet
                createRainbowBullet(bullets, x, y, velX, velY, damage, bulletSize, false);
            }
        }
    }

    private void createPrismaticBurstPattern(List<Bullet> bullets, float originX, float originY,
                                        float damage, float baseSpeed, float bulletSize,
                                        float targetX, float targetY) {
        int numBullets = 12;
        float radius = 80f;

        // Calculate angle to player
        float dx = targetX - originX;
        float dy = targetY - originY;
        float angleToPlayer = MathUtils.atan2(dy, dx);

        // Create a burst of bullets aimed toward player
        for (int i = 0; i < numBullets; i++) {
            float angle = i * (360f / numBullets) + rainbowTimer * 180f;
            float radians = angle * MathUtils.degreesToRadians;

            // Position on circle around origin
            float x = originX + MathUtils.cos(radians) * radius;
            float y = originY + MathUtils.sin(radians) * radius;

            // Mix outward motion with targeting player (70% toward player)
            float velX = MathUtils.cos(radians) * baseSpeed * 0.3f + MathUtils.cos(angleToPlayer) * baseSpeed * 0.7f;
            float velY = MathUtils.sin(radians) * baseSpeed * 0.3f + MathUtils.sin(angleToPlayer) * baseSpeed * 0.7f;

            // Create rainbow bullet
            createRainbowBullet(bullets, x, y, velX, velY, damage, bulletSize, false);
        }
    }

    private void createRainbowWebPattern(List<Bullet> bullets, float centerX, float centerY,
                                     float damage, float baseSpeed, float bulletSize,
                                     float targetX, float targetY) {
        int numThreads = 3;
        float innerRadius = 50f;
        float outerRadius = 200f;

        // Calculate angle to player
        float dx = targetX - centerX;
        float dy = targetY - centerY;
        float angleToPlayer = MathUtils.atan2(dy, dx);

        // Create threads connecting inner and outer circles
        for (int i = 0; i < numThreads; i++) {
            float angle = i * (360f / numThreads) + rainbowTimer * 120f;
            float radians = angle * MathUtils.degreesToRadians;

            // Inner point
            float innerX = centerX + MathUtils.cos(radians) * innerRadius;
            float innerY = centerY + MathUtils.sin(radians) * innerRadius;

            // Outer point
            float outerX = centerX + MathUtils.cos(radians) * outerRadius;
            float outerY = centerY + MathUtils.sin(radians) * outerRadius;

            // Create thread line connecting inner and outer
            int bulletsInLine = 6;
            for (int j = 0; j < bulletsInLine; j++) {
                float progress = j / (float)(bulletsInLine - 1);
                float x = innerX + (outerX - innerX) * progress;
                float y = innerY + (outerY - innerY) * progress;

                // Calculate perpendicular direction, MORE biased toward player (70% toward player now)
                float perpAngle = radians + MathUtils.PI/2;
                float velX = MathUtils.cos(perpAngle) * baseSpeed * 0.3f + MathUtils.cos(angleToPlayer) * baseSpeed * 0.7f;
                float velY = MathUtils.sin(perpAngle) * baseSpeed * 0.3f + MathUtils.sin(angleToPlayer) * baseSpeed * 0.7f;

                // Alternate direction but maintain strong player bias
                if (j % 2 == 0) {
                    velX = -MathUtils.cos(perpAngle) * baseSpeed * 0.3f + MathUtils.cos(angleToPlayer) * baseSpeed * 0.7f;
                    velY = -MathUtils.sin(perpAngle) * baseSpeed * 0.3f + MathUtils.sin(angleToPlayer) * baseSpeed * 0.7f;
                }

                // Ensure minimum velocity
                float speed = (float)Math.sqrt(velX * velX + velY * velY);
                float minSpeed = baseSpeed * 0.5f;
                if (speed < minSpeed) {
                    float scaleFactor = minSpeed / speed;
                    velX *= scaleFactor;
                    velY *= scaleFactor;
                }

                // Create rainbow bullet
                createRainbowBullet(bullets, x, y, velX, velY, damage, bulletSize - 2, false);
            }
        }
    }

    private Bullet createRainbowBullet(List<Bullet> bullets, float x, float y,
                                    float velX, float velY, float damage,
                                    float bulletSize, boolean isHealing) {
        // Always false now as we don't want healing bullets
        isHealing = false;
        
        // Ensure the bullet has a minimum velocity to prevent extremely slow movement
        // Calculate current speed
        float speed = (float)Math.sqrt(velX * velX + velY * velY);
        float baseSpeed = 880f; // Using the base speed value
        float minSpeed = baseSpeed * 0.4f; // Set a minimum speed threshold
        
        // If the speed is too low, scale it up to the minimum
        if (speed < minSpeed && speed > 0) { // Protect against division by zero
            float scaleFactor = minSpeed / speed;
            velX *= scaleFactor;
            velY *= scaleFactor;
        }
        
        // Base color for rainbow effect (shifted to be more vibrant)
        Color baseColor = new Color(
            MathUtils.random(0.7f, 1.0f),
            MathUtils.random(0.2f, 0.8f),
            MathUtils.random(0.4f, 1.0f),
            0.9f
        );
        
        // Create the bullet
        Bullet bullet = new Bullet(
            damage,
            x, y,
            velX, velY,
            bulletSize,
            baseColor,
            false // Never healing
        );
        
        // Apply rainbow disco effect with appropriate base color
        bullet.setDisco(true, true, true, 
            baseColor.r, baseColor.g, baseColor.b);
        bullet.setDiscoSpeed(2.5f);
        bullet.setDiscoColorRange(0.4f);
        
        // Set appropriate shape (randomly choose between diamond and circle for variety)
        bullet.setShape(MathUtils.randomBoolean() ? Bullet.Shape.DIAMOND : Bullet.Shape.CIRCLE);
        bullet.setTrailLength(25);
        bullet.enableTelegraphing(0.8f, 0.2f); // Reduced telegraph time from 1.1f to 0.8f
        bullet.setGlowProperties(Color.WHITE, true);
        bullet.setGlowing(true);
        bullet.setGlowIntensity(0.5f);
        bullet.setRotationSpeed(120f);
        
        bullets.add(bullet);
        return bullet;
    }

    private void addRandomExplosionBullets(List<Bullet> bullets, float arenaX, float arenaY,
                                           float arenaWidth, float arenaHeight,
                                           float damage, Enemy enemy) {
        // Create 2-3 random explosion bullets
        int explodingBulletsCount = MathUtils.random(2, 3);

        for (int i = 0; i < explodingBulletsCount; i++) {
            // Random position within arena
            float x = arenaX + MathUtils.random(0.1f, 0.9f) * arenaWidth;
            float y = arenaY + MathUtils.random(0.1f, 0.9f) * arenaHeight;

            // Increase speed for explosion bullets to prevent them from being too slow
            float speed = 280f; // Increased from 150f
            float angle = MathUtils.random(0, MathUtils.PI2);
            float velX = MathUtils.cos(angle) * speed;
            float velY = MathUtils.sin(angle) * speed;

            // Rainbow color for the explosion bullet
            Color explosionColor = new Color();
            explosionColor.fromHsv(rainbowTimer * 360f, 0.9f, 1.0f);

            Bullet explosionBullet = new Bullet(
                damage,
                x, y,
                velX, velY,
                15f, // Larger size
                explosionColor,
                false
            );

            explosionBullet.setShape(Bullet.Shape.STAR);
            explosionBullet.enableTelegraphing(1.2f, 0.3f); // Reduced from 1.6f
            explosionBullet.setGlowProperties(Color.WHITE, true);
            explosionBullet.setGlowing(true);
            explosionBullet.setGlowIntensity(0.7f);
            explosionBullet.setTrailLength(15);
            explosionBullet.setRotationSpeed(180f); // Increased from 150f
            explosionBullet.setDisco(true, true, true,
                explosionColor.r, explosionColor.g, explosionColor.b);
            explosionBullet.setDiscoSpeed(3.0f);
            explosionBullet.setDiscoColorRange(0.6f);

            // Set the explosion callback
            explosionBullet.setOnExplodeCallback(() -> {
                // Create explosion fragment bullets
                int fragmentCount = 6; // Number of fragments in circle
                float fragmentSpeed = 580f; // Increased from 500f for faster fragments
                float fragmentDamage = damage * 0.7f;

                // Calculate center position for explosion
                float explosionX = explosionBullet.getX();
                float explosionY = explosionBullet.getY();

                // Create fragments in a circular pattern with prismatic colors
                for (int j = 0; j < fragmentCount; j++) {
                    float fragAngle = j * (360f / fragmentCount) * MathUtils.degreesToRadians;
                    float vx = MathUtils.cos(fragAngle) * fragmentSpeed;
                    float vy = MathUtils.sin(fragAngle) * fragmentSpeed;

                    // Rainbow color for each fragment
                    Color fragColor = new Color();
                    fragColor.fromHsv((rainbowTimer * 360f + j * 360f / fragmentCount) % 360f, 0.9f, 1.0f);

                    Bullet fragment = new Bullet(
                        fragmentDamage,
                        explosionX,
                        explosionY,
                        vx,
                        vy,
                        8f,
                        fragColor,
                        false
                    );

                    fragment.setShape(j % 2 == 0 ? Bullet.Shape.DIAMOND : Bullet.Shape.CIRCLE);
                    fragment.setTrailLength(25);
                    fragment.setGlowProperties(Color.WHITE, true);
                    fragment.setGlowing(true);
                    fragment.setGlowIntensity(0.6f);
                    fragment.setDisco(true, true, true,
                        fragColor.r, fragColor.g, fragColor.b);
                    fragment.setDiscoSpeed(2.0f);

                    // Add to explosion bullets collection
                    explosionBullet.getExplosionBullets().add(fragment);
                }

                // Trigger screen shake if enemy is AbstractEnemy
                if (enemy instanceof AbstractEnemy) {
                    ((AbstractEnemy) enemy).triggerScreenShake(0.4f, 5.0f);
                }
            });

            // Set a timer for the explosion (1.8 seconds) - reduced from 2.2
            explosionBullet.startExplosionTimer(1.8f);
            bullets.add(explosionBullet);
        }
    }

    @Override
    public String getPatternName() {
        return "Prismatic Assault";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
