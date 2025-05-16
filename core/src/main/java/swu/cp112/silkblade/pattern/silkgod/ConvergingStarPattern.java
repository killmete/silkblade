package swu.cp112.silkblade.pattern.silkgod;

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
 * Direct Intersect Bullet Pattern - bullets spawn in a circle around the player
 * and immediately move toward the player's position.
 */
public class ConvergingStarPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        4, 9, 1.25f, 0.455f, 15, 790f, 400f, Color.YELLOW, false,
        "Stars spawning around player, immediately converging on their position."
    );

    private float rotationAngle = 0f;
    private static final float DESTROY_DISTANCE = 10f; // Distance to target when bullet should be destroyed

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

        // Generate multiple bullets
        int bulletCount = 6;  // 7 bullets for the circle
        float baseSpeed = 300;  // Speed for direct movement (slightly faster than original)
        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.15f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float damage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));
        float bulletSize = 14;  // Same size as original

        // Circle pattern - spawn bullets around player
        float radius = 150f; // Distance from player to spawn the bullets


        // Increment the rotation angle for variety in spawn positions
        rotationAngle = (rotationAngle + 35f) % 360f;

        for (int i = 0; i < bulletCount; i++) {
            // Calculate position in a circle around the player
            float angleInRadians = ((i * 360f / bulletCount) + rotationAngle) * MathUtils.degreesToRadians;
            float bulletX = targetX + radius * MathUtils.cos(angleInRadians);
            float bulletY = targetY + radius * MathUtils.sin(angleInRadians);

            // Calculate velocity directly toward the player
            float dx = targetX - bulletX;
            float dy = targetY - bulletY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            float velX = dx / distance * baseSpeed;
            float velY = dy / distance * baseSpeed;

            Color lightGold = new Color(1f, 0.95f, 0.7f, 1f);
            Color paleYellow = new Color(1f, 1f, 0.85f, 1f);
            Color warmWhite = new Color(1f, 0.98f, 0.9f, 1f);

            // Create bullet with velocity toward player
            Bullet bullet = new Bullet(
                damage,
                bulletX,
                bulletY,
                velX,
                velY,
                bulletSize,
                new Color(warmWhite),
                CONFIG.isHealing()
            );

            // Store the target position for distance checking
            final float centerX = targetX;
            final float centerY = targetY;

            bullet.setDisco(true, true, false,
                1.0f,    // Red base (full)
                0.95f,   // Green base (slightly reduced)
                0.7f     // Blue base (golden tint)
            );
            bullet.setDiscoSpeed(1.5f);
            bullet.setDiscoColorRange(0.1f);
            bullet.setTrailLength(60);
            bullet.setShape(Bullet.Shape.STAR);
            bullet.setSpinDirectionMatchesMovement(true);
            // Set custom update callback just for destruction when target is reached
            bullet.setUpdateCallback(delta -> {
                // Check if bullet should be destroyed (reached target)
                float currDx = bullet.getX() - centerX;
                float currDy = bullet.getY() - centerY;
                float distToTarget = (float) Math.sqrt(currDx * currDx + currDy * currDy);

                if (distToTarget <= DESTROY_DISTANCE) {
                    bullet.startFading(0.5f);
                    return false; // Stop callback execution
                }

                return true; // Continue with normal movement
            });
            bullet.enableTelegraphing(0.6f, 0.2f);
            bullet.setGlowProperties(Color.WHITE, true);
            bullet.setGlowing(true);  // Enable glow
            bullet.setGlowLayers(12);  // Optional: Set custom number of glow layers
            bullet.setGlowIntensity(0.3f);
            bullets.add(bullet);
        }

        return bullets;
    }

    @Override
    public String getPatternName() {
        return "Stellar Convergence";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
