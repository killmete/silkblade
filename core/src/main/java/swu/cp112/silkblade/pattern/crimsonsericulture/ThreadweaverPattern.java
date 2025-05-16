package swu.cp112.silkblade.pattern.crimsonsericulture;

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
 * Threadweaver Pattern - creates intricate webs of thread-like bullets that
 * form enclosing patterns around the player.
 */
public class ThreadweaverPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        12, 15, 2.2f, 1.75f, 16, 800f, 410f,
        new Color(0.8f, 0.1f, 0.1f, 1f), // Deep crimson color
        false,
        "Crimson threads weaving complex patterns to trap the target."
    );

    private float patternAngle = 0f;
    private int waveCounter = 0;
    private float explosionTimer = 0f;

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                         float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Get player position for targeting
        float targetX = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerX() :
            arenaX + arenaWidth / 2;
        float targetY = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerY() :
            arenaY + arenaHeight / 2;

        // Get enemy position
        float enemyX = enemy.getX();
        float enemyY = enemy.getY();

        // Calculate damage
        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 0.65f + (enemyAttackDamage * 0.02f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.1f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.1f)) * defenseMultiplier;
        float damage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));

        // Base bullet parameters
        float baseSpeed = 880f;
        float bulletSize = 10f;

        // Update explosion timer
        explosionTimer += 1.0f;
        if (explosionTimer >= 3.5f) {
            addRandomExplosionBullets(bullets, arenaX, arenaY, arenaWidth, arenaHeight, damage, enemy);
            explosionTimer = 0f;
        }

        // Rotate pattern angle for variety
        patternAngle += 20f;
        if (patternAngle >= 360f) patternAngle -= 360f;

        // Increment wave counter
        waveCounter = (waveCounter + 1) % 3;

        // Switch between different thread patterns
        if (waveCounter == 0) {
            // Pattern 1: Web enclosure (2-3 instances at different positions)
            for (int i = 0; i < 3; i++) {
                float offsetX = MathUtils.random(-350f, 350f);
                float offsetY = MathUtils.random(-350f, 350f);
                createWebEnclosurePattern(bullets, targetX + offsetX, targetY + offsetY, damage, baseSpeed, bulletSize, targetX, targetY);
            }
        } else if (waveCounter == 1) {
            // Pattern 2: Thread spiral (2 instances from different origins)
            float spiral1X = enemyX + MathUtils.random(-400f, 400f);
            float spiral1Y = enemyY + MathUtils.random(-300f, 300f);
            createThreadSpiralPattern(bullets, spiral1X, spiral1Y, targetX, targetY, damage, baseSpeed, bulletSize);

            float spiral2X = enemyX + MathUtils.random(-400f, 400f);
            float spiral2Y = enemyY + MathUtils.random(-300f, 300f);
            while (Math.abs(spiral2X - spiral1X) < 200f && Math.abs(spiral2Y - spiral1Y) < 200f) {
                spiral2X = enemyX + MathUtils.random(-400f, 400f);
                spiral2Y = enemyY + MathUtils.random(-300f, 300f);
            }
            createThreadSpiralPattern(bullets, spiral2X, spiral2Y, targetX, targetY, damage, baseSpeed, bulletSize);
        } else {
            // Pattern 3: Cross-weave (multiple instances aimed at player)
            for (int i = 0; i < 2; i++) {
                float offsetX = MathUtils.random(-350f, 350f);
                float offsetY = MathUtils.random(-350f, 350f);
                createCrossWeavePattern(bullets, targetX + offsetX, targetY + offsetY, damage, baseSpeed, bulletSize, targetX, targetY);
            }
        }

        return bullets;
    }

    private void createWebEnclosurePattern(List<Bullet> bullets, float centerX, float centerY,
                                          float damage, float baseSpeed, float bulletSize,
                                          float targetX, float targetY) {
        int numThreads = 4;
        float radius = 180f;

        // Calculate angle to player
        float dx = targetX - centerX;
        float dy = targetY - centerY;
        float angleToPlayer = MathUtils.atan2(dy, dx);

        // Create threads from points around given center, orientated towards player
        for (int i = 0; i < numThreads; i++) {
            float angle = i * (360f / numThreads) + patternAngle;
            float radians = angle * MathUtils.degreesToRadians;

            // Start position on circle surrounding center
            float startX = centerX + MathUtils.cos(radians) * radius;
            float startY = centerY + MathUtils.sin(radians) * radius;

            // End position on opposite side of circle
            float endX = centerX + MathUtils.cos(radians + MathUtils.PI) * radius;
            float endY = centerY + MathUtils.sin(radians + MathUtils.PI) * radius;

            // Create a line of bullets connecting the two points, aimed at player
            createThreadLine(bullets, startX, startY, endX, endY, damage, baseSpeed * 0.8f, bulletSize, targetX, targetY);
        }
    }

    private void createThreadSpiralPattern(List<Bullet> bullets, float originX, float originY,
                                         float targetX, float targetY, float damage,
                                         float baseSpeed, float bulletSize) {
        int numSpirals = 3;
        int bulletsPerSpiral = 10;
        float maxRadius = 160f;

        // Calculate angle to player
        float dx = targetX - originX;
        float dy = targetY - originY;
        float angleToPlayer = MathUtils.atan2(dy, dx);

        for (int spiral = 0; spiral < numSpirals; spiral++) {
            float spiralOffset = spiral * (360f / numSpirals) + patternAngle;

            for (int i = 0; i < bulletsPerSpiral; i++) {
                // Calculate position in spiral
                float progress = i / (float)bulletsPerSpiral;
                float radius = progress * maxRadius;
                float angle = spiralOffset + progress * 720f + angleToPlayer; // 2 full rotations, oriented toward player
                float radians = angle * MathUtils.degreesToRadians;

                float x = originX + MathUtils.cos(radians) * radius;
                float y = originY + MathUtils.sin(radians) * radius;

                // Calculate velocity tangential to the spiral, but biased towards player
                float tangentAngle = radians + MathUtils.PI/2;
                float velX = MathUtils.cos(tangentAngle) * baseSpeed * 0.7f + MathUtils.cos(angleToPlayer) * baseSpeed * 0.3f;
                float velY = MathUtils.sin(tangentAngle) * baseSpeed * 0.7f + MathUtils.sin(angleToPlayer) * baseSpeed * 0.3f;

                // Create thread bullet
                createThreadBullet(bullets, x, y, velX, velY, damage, bulletSize);
            }
        }
    }

    private void createCrossWeavePattern(List<Bullet> bullets, float centerX, float centerY,
                                      float damage, float baseSpeed, float bulletSize,
                                      float targetX, float targetY) {
        int linesPerDirection = 4;
        float spacing = 40f;
        float lineLength = 300f;

        // Calculate angle to player
        float dx = targetX - centerX;
        float dy = targetY - centerY;
        float angleToPlayer = MathUtils.atan2(dy, dx);

        // Create horizontal and vertical lines oriented toward player
        for (int i = -linesPerDirection/2; i <= linesPerDirection/2; i++) {
            if (i == 0) continue; // Skip center line

            // Horizontal line offset (perpendicular to player angle)
            float yOffset = i * spacing;
            float perpAngle = angleToPlayer + MathUtils.PI/2;

            // Calculate start and end points of horizontal line
            float hStartX = centerX - MathUtils.cos(angleToPlayer) * lineLength/2;
            float hStartY = centerY - MathUtils.sin(angleToPlayer) * lineLength/2;
            float hEndX = centerX + MathUtils.cos(angleToPlayer) * lineLength/2;
            float hEndY = centerY + MathUtils.sin(angleToPlayer) * lineLength/2;

            // Adjust for offset perpendicular to player angle
            hStartX += MathUtils.cos(perpAngle) * yOffset;
            hStartY += MathUtils.sin(perpAngle) * yOffset;
            hEndX += MathUtils.cos(perpAngle) * yOffset;
            hEndY += MathUtils.sin(perpAngle) * yOffset;

            createThreadLine(bullets,
                            hStartX, hStartY,
                            hEndX, hEndY,
                            damage, baseSpeed, bulletSize, targetX, targetY);

            // Vertical line offset (along player angle)
            float xOffset = i * spacing;

            // Calculate start and end points of vertical line
            float vStartX = centerX - MathUtils.cos(perpAngle) * lineLength/2;
            float vStartY = centerY - MathUtils.sin(perpAngle) * lineLength/2;
            float vEndX = centerX + MathUtils.cos(perpAngle) * lineLength/2;
            float vEndY = centerY + MathUtils.sin(perpAngle) * lineLength/2;

            // Adjust for offset along player angle
            vStartX += MathUtils.cos(angleToPlayer) * xOffset;
            vStartY += MathUtils.sin(angleToPlayer) * xOffset;
            vEndX += MathUtils.cos(angleToPlayer) * xOffset;
            vEndY += MathUtils.sin(angleToPlayer) * xOffset;

            createThreadLine(bullets,
                            vStartX, vStartY,
                            vEndX, vEndY,
                            damage, baseSpeed, bulletSize, targetX, targetY);
        }
    }

    private void createThreadLine(List<Bullet> bullets, float startX, float startY,
                                float endX, float endY, float damage, float speed,
                                float bulletSize, float targetX, float targetY) {
        int bulletsInLine = 8;

        // Calculate angle to player for directional bias
        float playerAngleX = targetX - ((startX + endX) / 2);
        float playerAngleY = targetY - ((startY + endY) / 2);
        float distToPlayer = (float)Math.sqrt(playerAngleX * playerAngleX + playerAngleY * playerAngleY);
        float normalizedPlayerDirX = distToPlayer > 0 ? playerAngleX / distToPlayer : 0;
        float normalizedPlayerDirY = distToPlayer > 0 ? playerAngleY / distToPlayer : 0;

        for (int i = 0; i < bulletsInLine; i++) {
            float progress = i / (float)(bulletsInLine - 1);
            float x = startX + (endX - startX) * progress;
            float y = startY + (endY - startY) * progress;

            // Calculate direction perpendicular to the line
            float lineAngle = MathUtils.atan2(endY - startY, endX - startX);
            float perpAngle = lineAngle + MathUtils.PI/2;

            // Add directional bias toward player (70% perpendicular, 30% toward player)
            float velX = MathUtils.cos(perpAngle) * speed * 0.7f + normalizedPlayerDirX * speed * 0.3f;
            float velY = MathUtils.sin(perpAngle) * speed * 0.7f + normalizedPlayerDirY * speed * 0.3f;

            // Alternate direction for more complex patterns
            if (i % 2 == 1) {
                velX = -velX * 0.7f + normalizedPlayerDirX * speed * 0.3f;
                velY = -velY * 0.7f + normalizedPlayerDirY * speed * 0.3f;
            }

            // Create thread bullet
            createThreadBullet(bullets, x, y, velX, velY, damage, bulletSize);
        }
    }

    private Bullet createThreadBullet(List<Bullet> bullets, float x, float y,
                                    float velX, float velY, float damage,
                                    float bulletSize) {
        // Create a thread-like bullet
        Bullet bullet = new Bullet(
            damage,
            x, y,
            velX, velY,
            bulletSize,
            new Color(0.85f, 0.1f, 0.2f, 0.9f),
            false // Never healing
        );

        // Style as a silk thread
        bullet.setShape(Bullet.Shape.CIRCLE);
        bullet.setTrailLength(30);
        bullet.enableTelegraphing(1.1f, 0.2f);
        bullet.setGlowProperties(new Color(1f, 0.2f, 0.2f, 0.7f), true);
        bullet.setGlowing(true);
        bullet.setGlowIntensity(0.35f);
        bullet.setRotationSpeed(90f);

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

            // Slow movement speed for explosion bullets
            float speed = 150f;
            float angle = MathUtils.random(0, MathUtils.PI2);
            float velX = MathUtils.cos(angle) * speed;
            float velY = MathUtils.sin(angle) * speed;

            Bullet explosionBullet = new Bullet(
                damage,
                x, y,
                velX, velY,
                14f, // Larger size
                new Color(0.9f, 0.1f, 0.3f, 0.9f), // Deep crimson
                false
            );

            explosionBullet.setShape(Bullet.Shape.DIAMOND);
            explosionBullet.enableTelegraphing(1.5f, 0.3f);
            explosionBullet.setGlowProperties(new Color(1f, 0.1f, 0.3f, 0.8f), true);
            explosionBullet.setGlowing(true);
            explosionBullet.setGlowIntensity(0.6f);
            explosionBullet.setTrailLength(15); // Shorter trail as it moves slower
            explosionBullet.setRotationSpeed(120f);

            // Set the explosion callback
            explosionBullet.setOnExplodeCallback(() -> {
                // Create explosion fragment bullets - web pattern
                int fragmentCount = 6; // Number of fragments in circle
                float fragmentSpeed = 480f;
                float fragmentDamage = damage * 0.8f;

                // Calculate center position for explosion
                float explosionX = explosionBullet.getX();
                float explosionY = explosionBullet.getY();

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
                        new Color(0.85f, 0.1f, 0.2f, 0.9f),
                        false
                    );

                    fragment.setShape(Bullet.Shape.CIRCLE);
                    fragment.setTrailLength(30);
                    fragment.setGlowProperties(new Color(1f, 0.1f, 0.2f, 0.7f), true);
                    fragment.setGlowing(true);
                    fragment.setGlowIntensity(0.5f);
                    fragment.setRotationSpeed(90f);

                    // Add to explosion bullets collection
                    explosionBullet.getExplosionBullets().add(fragment);
                }

                // Trigger screen shake if enemy is AbstractEnemy
                if (enemy instanceof AbstractEnemy) {
                    ((AbstractEnemy) enemy).triggerScreenShake(0.35f, 4.5f);
                }
            });

            // Set a timer for the explosion (2.2 seconds)
            explosionBullet.startExplosionTimer(2.2f);
            bullets.add(explosionBullet);
        }
    }

    @Override
    public String getPatternName() {
        return "Crimson Threadweaver";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
