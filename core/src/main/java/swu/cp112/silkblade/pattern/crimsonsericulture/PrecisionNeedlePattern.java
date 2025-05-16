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
 * Precision Needle Pattern - bullets spawn in precise formations and target the player
 * with calculated trajectories. Features fast, small, precise needles.
 */
public class PrecisionNeedlePattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        12, 20, 1.4f, 1.70f, 15, 800f, 410f,
        new Color(0.9f, 0.1f, 0.1f, 1f), // Deep crimson color
        false,
        "Crimson needles launching in precision formations to pierce the target."
    );

    private float rotationAngle = 0f;
    private int patternCycle = 0;
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

        // Get center of enemy
        float enemyX = enemy.getX();
        float enemyY = enemy.getY();

        // Calculate damage
        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 0.7f + (enemyAttackDamage * 0.02f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.1f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.1f)) * defenseMultiplier;
        float damage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));

        // Base bullet parameters - small, fast needles
        float baseSpeed = 960f;
        float bulletSize = 8f;

        // Update explosion timer
        explosionTimer += 1.0f;
        if (explosionTimer >= 3.0f) {
            addRandomExplosionBullets(bullets, arenaX, arenaY, arenaWidth, arenaHeight, damage, enemy);
            explosionTimer = 0f;
        }

        // Cycle through different patterns each time
        patternCycle = (patternCycle + 1) % 3;

        // Increment rotation angle for variety
        rotationAngle = (rotationAngle + 15f) % 360f;

        // Generate multiple instances of the pattern at different positions
        switch (patternCycle) {
            case 0:
                // Create multiple grid patterns around the player
                for (int i = 0; i < 2; i++) {
                    float offsetX = MathUtils.random(-350f, 350f);
                    float offsetY = MathUtils.random(-350f, 350f);
                    generateGridPattern(bullets, targetX + offsetX, targetY + offsetY, damage, baseSpeed, bulletSize, targetX, targetY);
                }
                break;
            case 1:
                // Create multiple spiral patterns
                for (int i = 0; i < 2; i++) {
                    float offsetX = MathUtils.random(-400f, 400f);
                    float offsetY = MathUtils.random(-200f, 200f);
                    generateSpiralPattern(bullets, enemyX + offsetX, enemyY + offsetY, targetX, targetY, damage, baseSpeed, bulletSize);
                }
                break;
            case 2:
                // Create multiple cross patterns
                for (int i = 0; i < 3; i++) {
                    float offsetX = MathUtils.random(-300f, 300f);
                    float offsetY = MathUtils.random(-300f, 300f);
                    generateCrossPattern(bullets, targetX + offsetX, targetY + offsetY, damage, baseSpeed, bulletSize, targetX, targetY);
                }
                break;
        }

        return bullets;
    }

    private void generateGridPattern(List<Bullet> bullets, float centerX, float centerY,
                                    float damage, float baseSpeed, float bulletSize,
                                    float targetX, float targetY) {
        int columns = 4;
        int rows = 3;
        float width = 120f;
        float height = 80f;

        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                // Position bullets in a grid centered on the given position
                float x = centerX - width/2 + (width/(columns-1)) * i;
                float y = centerY - height/2 + (height/(rows-1)) * j;

                // Calculate angle to player for directional movement
                float dx = targetX - x;
                float dy = targetY - y;
                float angle = MathUtils.atan2(dy, dx);

                float velX = MathUtils.cos(angle) * baseSpeed;
                float velY = MathUtils.sin(angle) * baseSpeed;

                // Create needle-like bullet
                createNeedleBullet(bullets, x, y, velX, velY, damage, bulletSize);
            }
        }
    }

    private void generateSpiralPattern(List<Bullet> bullets, float originX, float originY,
                                      float targetX, float targetY, float damage,
                                      float baseSpeed, float bulletSize) {
        int bulletCount = 12;
        float spiralRadius = 120f;

        // Calculate angle to player for the spiral direction
        float angleToPlayer = MathUtils.atan2(targetY - originY, targetX - originX);

        for (int i = 0; i < bulletCount; i++) {
            // Calculate position in spiral
            float angle = angleToPlayer + i * 0.5f + rotationAngle * MathUtils.degreesToRadians;
            float distance = spiralRadius * (i / (float)bulletCount);

            float x = originX + MathUtils.cos(angle) * distance;
            float y = originY + MathUtils.sin(angle) * distance;

            // Calculate velocity toward player with slight arc
            float velX = MathUtils.cos(angle) * baseSpeed;
            float velY = MathUtils.sin(angle) * baseSpeed;

            // Create needle-like bullet with spiral motion
            Bullet bullet = createNeedleBullet(bullets, x, y, velX, velY, damage, bulletSize);

            // Add spiral motion
            final float spiralSpeed = 0.15f;
            final float startAngle = angle;
            // Create a variable to track lifetime manually
            final float[] lifetime = {0f};
            bullet.setUpdateCallback(delta -> {
                // Update the lifetime counter
                lifetime[0] += delta;

                // Get current velocity
                float currVelX = bullet.getVelocityX();
                float currVelY = bullet.getVelocityY();
                float currSpeed = (float) Math.sqrt(currVelX * currVelX + currVelY * currVelY);

                // Calculate new angle with spiral motion using our tracked lifetime
                float spiralAngle = startAngle + lifetime[0] * spiralSpeed;

                // Apply new direction while maintaining speed
                bullet.setVelocity(
                    MathUtils.cos(spiralAngle) * currSpeed,
                    MathUtils.sin(spiralAngle) * currSpeed
                );

                return true;
            });
        }
    }

    private void generateCrossPattern(List<Bullet> bullets, float centerX, float centerY,
                                     float damage, float baseSpeed, float bulletSize,
                                     float targetX, float targetY) {
        int bulletsPerLine = 7;
        float spacing = 30f;

        // Calculate angle to player
        float dx = targetX - centerX;
        float dy = targetY - centerY;
        float angleToPlayer = MathUtils.atan2(dy, dx);

        // Generate cross centered on the given position and rotated toward player
        for (int i = 0; i < bulletsPerLine; i++) {
            // Horizontal line (adjusted to face player)
            if (i != bulletsPerLine / 2) { // Skip center
                float offset = (i - bulletsPerLine / 2) * spacing;
                float hX = centerX + MathUtils.cos(angleToPlayer + MathUtils.PI/2) * offset;
                float hY = centerY + MathUtils.sin(angleToPlayer + MathUtils.PI/2) * offset;

                float velX = MathUtils.cos(angleToPlayer) * baseSpeed;
                float velY = MathUtils.sin(angleToPlayer) * baseSpeed;

                createNeedleBullet(bullets, hX, hY, velX, velY, damage, bulletSize);
            }

            // Vertical line (adjusted to face player)
            if (i != bulletsPerLine / 2) { // Skip center
                float offset = (i - bulletsPerLine / 2) * spacing;
                float vX = centerX + MathUtils.cos(angleToPlayer) * offset;
                float vY = centerY + MathUtils.sin(angleToPlayer) * offset;

                float velX = MathUtils.cos(angleToPlayer + MathUtils.PI/2) * baseSpeed;
                float velY = MathUtils.sin(angleToPlayer + MathUtils.PI/2) * baseSpeed;

                createNeedleBullet(bullets, vX, vY, velX, velY, damage, bulletSize);
            }
        }

        // Add diagonal bullets
        for (int i = 1; i <= 3; i++) {
            float offset = spacing * i;

            // Top-right diagonal (adjusted for player angle)
            float tr_x = centerX + MathUtils.cos(angleToPlayer + MathUtils.PI/4) * offset;
            float tr_y = centerY + MathUtils.sin(angleToPlayer + MathUtils.PI/4) * offset;
            createNeedleBullet(bullets, tr_x, tr_y,
                              MathUtils.cos(angleToPlayer - MathUtils.PI*3/4) * baseSpeed,
                              MathUtils.sin(angleToPlayer - MathUtils.PI*3/4) * baseSpeed,
                              damage, bulletSize);

            // Top-left diagonal (adjusted for player angle)
            float tl_x = centerX + MathUtils.cos(angleToPlayer - MathUtils.PI/4) * offset;
            float tl_y = centerY + MathUtils.sin(angleToPlayer - MathUtils.PI/4) * offset;
            createNeedleBullet(bullets, tl_x, tl_y,
                              MathUtils.cos(angleToPlayer + MathUtils.PI*3/4) * baseSpeed,
                              MathUtils.sin(angleToPlayer + MathUtils.PI*3/4) * baseSpeed,
                              damage, bulletSize);

            // Bottom-right diagonal (adjusted for player angle)
            float br_x = centerX + MathUtils.cos(angleToPlayer + MathUtils.PI*3/4) * offset;
            float br_y = centerY + MathUtils.sin(angleToPlayer + MathUtils.PI*3/4) * offset;
            createNeedleBullet(bullets, br_x, br_y,
                              MathUtils.cos(angleToPlayer - MathUtils.PI/4) * baseSpeed,
                              MathUtils.sin(angleToPlayer - MathUtils.PI/4) * baseSpeed,
                              damage, bulletSize);

            // Bottom-left diagonal (adjusted for player angle)
            float bl_x = centerX + MathUtils.cos(angleToPlayer - MathUtils.PI*3/4) * offset;
            float bl_y = centerY + MathUtils.sin(angleToPlayer - MathUtils.PI*3/4) * offset;
            createNeedleBullet(bullets, bl_x, bl_y,
                              MathUtils.cos(angleToPlayer + MathUtils.PI/4) * baseSpeed,
                              MathUtils.sin(angleToPlayer + MathUtils.PI/4) * baseSpeed,
                              damage, bulletSize);
        }
    }

    private Bullet createNeedleBullet(List<Bullet> bullets, float x, float y,
                                     float velX, float velY, float damage,
                                     float bulletSize) {
        // Create a crimson needle-like bullet
        Bullet bullet = new Bullet(
            damage,
            x, y,
            velX, velY,
            bulletSize,
            new Color(0.95f, 0.1f, 0.1f, 1f),
            false // Never healing
        );

        // Style as a needle
        bullet.setShape(Bullet.Shape.CIRCLE);
        bullet.setSpinDirectionMatchesMovement(true);
        bullet.setTrailLength(20);
        bullet.enableTelegraphing(1f, 0.1f);
        bullet.setGlowProperties(new Color(1f, 0.3f, 0.3f, 0.8f), true);
        bullet.setGlowing(true);
        bullet.setGlowIntensity(0.4f);

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
            float speed = 120f;
            float angle = MathUtils.random(0, MathUtils.PI2);
            float velX = MathUtils.cos(angle) * speed;
            float velY = MathUtils.sin(angle) * speed;

            Bullet explosionBullet = new Bullet(
                damage,
                x, y,
                velX, velY,
                12f, // Larger size
                new Color(1.0f, 0.3f, 0.1f, 0.9f), // Orange-red
                false
            );

            explosionBullet.setShape(Bullet.Shape.STAR);
            explosionBullet.enableTelegraphing(1.5f, 0.3f);
            explosionBullet.setGlowProperties(Color.RED, true);
            explosionBullet.setGlowing(true);
            explosionBullet.setGlowIntensity(0.6f);
            explosionBullet.setTrailLength(12); // Shorter trail as it moves slower
            explosionBullet.setRotationSpeed(180f);

            // Set the explosion callback
            explosionBullet.setOnExplodeCallback(() -> {
                // Create explosion fragment bullets
                int fragmentCount = 5; // Number of fragments in circle
                float fragmentSpeed = 450f;
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
                        7f,
                        new Color(0.9f, 0.1f, 0.1f, 0.9f),
                        false
                    );

                    fragment.setShape(Bullet.Shape.DIAMOND);
                    fragment.setTrailLength(25);
                    fragment.setGlowProperties(new Color(1f, 0.2f, 0.1f, 0.8f), true);
                    fragment.setGlowing(true);
                    fragment.setGlowIntensity(0.5f);
                    fragment.setDiscoSpeed(2.5f);

                    // Add to explosion bullets collection
                    explosionBullet.getExplosionBullets().add(fragment);
                }

                // Trigger screen shake if enemy is AbstractEnemy
                if (enemy instanceof AbstractEnemy) {
                    ((AbstractEnemy) enemy).triggerScreenShake(0.3f, 4.0f);
                }
            });

            // Set a timer for the explosion (2.0 seconds)
            explosionBullet.startExplosionTimer(2.0f);
            bullets.add(explosionBullet);
        }
    }

    @Override
    public String getPatternName() {
        return "Precision Needles";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
