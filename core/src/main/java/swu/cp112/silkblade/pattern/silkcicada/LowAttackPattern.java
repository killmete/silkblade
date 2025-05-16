package swu.cp112.silkblade.pattern.silkcicada;

import com.badlogic.gdx.Gdx;
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
 * Low-difficulty attack pattern for the Silk Cicada enemy type.
 * Used for stages 21-23, the introductory Silk Cicada encounters.
 * Features simple, predictable patterns that are easier to dodge.
 */
public class LowAttackPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        10, 13, 3.0f, 0.8f, 10, 300f, 300f, new Color(0.4f, 0.6f, 0.1f, 1.0f), true,
        "Cicada's Simple Melody", 1.5f
    );

    // Attack phase trackers
    private int currentPhase = 0;
    private float phaseTimer = 0f;
    private static final float PHASE_DURATION = 3.0f; // Duration of each attack phase
    private static final int TOTAL_PHASES = 3; // Total different attack phases

    // Burst pattern properties
    private static final int BURST_BULLETS = 8;
    private static final float BURST_ANGLE_SPREAD = 80f;

    // Linear pattern properties
    private static final int LINE_BULLETS = 4;
    private static final float LINE_SPACING = 40f;

    // Circular pattern properties
    private static final int CIRCLE_BULLETS = 10;
    private float circleRotationOffset = 0f;

    // Healing properties
    private static final float HEALING_CHANCE = 0.1f;

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                       float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Update timers
        float delta = Gdx.graphics.getDeltaTime();
        phaseTimer += delta;

        // Check for phase transition
        if (currentPhase == TOTAL_PHASES) {
            currentPhase = 0;
        }

        // Get player position for targeting
        float playerX = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerX() :
            arenaX + arenaWidth / 2;
        float playerY = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerY() :
            arenaY + arenaHeight / 2;

        // Get enemy position from the enemy object
        float enemyX = enemy.getX();
        float enemyY = enemy.getY();

        // Calculate attack parameters based on enemy's state
        float damageMultiplier = 1.0f + (enemy.getAttackDamage() * 0.03f);
        float speedMultiplier = 1.0f + 0.04f; // Fixed multiplier since Enemy has no getDefense()
        float baseSpeed = 200f * speedMultiplier;

        // Get scaled damage
        float minDamage = CONFIG.getMinDamage() * damageMultiplier;
        float maxDamage = CONFIG.getMaxDamage() * damageMultiplier;

        // Get enemy color
        Color enemyColor = enemy.getPrimaryColor();

        // Execute current phase
        switch (currentPhase) {
            case 0:
                // Phase 1: Simple burst pattern toward player
                createBurstPattern(bullets, enemyX, enemyY, playerX, playerY,
                                 baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;

            case 1:
                // Phase 2: Linear shots across the arena
                createLinearPattern(bullets, enemyX, enemyY, playerX, playerY,
                                  baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;

            case 2:
                // Phase 3: Simple circular pattern
                createCircularPattern(bullets, enemyX, enemyY, playerX, playerY,
                                    baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;
        }

        // Occasionally spawn healing bullets
        if (MathUtils.random() < HEALING_CHANCE) {
            createHealingBullet(bullets, enemyX, enemyY, playerX, playerY);
        }

        return bullets;
    }

    /**
     * Creates a simple burst of bullets aimed toward the player
     */
    private void createBurstPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                  float playerX, float playerY, float speed,
                                  float minDamage, float maxDamage, Color baseColor) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Calculate direction to player for central burst
        float dirX = playerX - enemyX;
        float dirY = playerY - enemyY;
        float dirLength = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX = dirX / dirLength;
        dirY = dirY / dirLength;

        // Create a spread of bullets in a fan pattern
        for (int i = 0; i < BURST_BULLETS; i++) {
            // Calculate angle spread (using the wider BURST_ANGLE_SPREAD)
            float spreadAngle = ((i / (float)(BURST_BULLETS - 1)) - 0.5f) * MathUtils.degreesToRadians * BURST_ANGLE_SPREAD;

            // Rotate the direction vector by the spread angle
            float cos = MathUtils.cos(spreadAngle);
            float sin = MathUtils.sin(spreadAngle);
            float rotatedDirX = dirX * cos - dirY * sin;
            float rotatedDirY = dirX * sin + dirY * cos;

            // Create the bullet with slight variation in speed
            float speedVariation = 0.85f + (0.3f * ((float)i / BURST_BULLETS));

            Bullet bullet = new Bullet(
                damage,
                enemyX,
                enemyY,
                rotatedDirX * speed * speedVariation,
                rotatedDirY * speed * speedVariation,
                6f, // Size
                // Vary color slightly based on position in fan
                new Color(baseColor).lerp(Color.CYAN, Math.abs((i / (float)(BURST_BULLETS - 1)) - 0.5f)),
                false // Not healing
            );

            // Apply visual styling
            styleBullet(bullet, 0);

            // Add simple telegraphing for the wider burst pattern
            if (i == 0 || i == BURST_BULLETS - 1) {
                // Only telegraph the outer bullets to indicate the spread
                bullet.enableTelegraphing(0.25f, 0.15f);
            }

            bullets.add(bullet);
        }
    }

    /**
     * Creates simple linear patterns of bullets that target the player
     */
    private void createLinearPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                   float playerX, float playerY, float speed,
                                   float minDamage, float maxDamage, Color baseColor) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Calculate direction to player
        float dirX = playerX - enemyX;
        float dirY = playerY - enemyY;
        float dirLength = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX = dirX / dirLength;
        dirY = dirY / dirLength;

        // Calculate perpendicular direction for the line formation
        float perpX = -dirY;
        float perpY = dirX;

        // Define arena bounds approximation (assuming arena is centered on enemy)
        float arenaHalfWidth = 200f;
        float arenaHalfHeight = 200f;

        // Create bullets in horizontal and vertical lines
        for (int direction = 0; direction < 2; direction++) {
            // Direction 0 = perpendicular to player, 1 = toward player
            for (int i = 0; i < LINE_BULLETS; i++) {
                // Calculate position in the line
                float offset = (i - (LINE_BULLETS - 1) / 2.0f) * LINE_SPACING;

                float posX, posY;
                float velX, velY;

                if (direction == 0) {
                    // Line perpendicular to player direction
                    posX = enemyX + perpX * offset;
                    posY = enemyY + perpY * offset;

                    // Move toward player
                    velX = dirX * speed;
                    velY = dirY * speed;
                } else {
                    // Line along player direction
                    posX = enemyX + dirX * offset;
                    posY = enemyY + dirY * offset;

                    // Move perpendicular to player direction (sideways)
                    // Alternate left/right based on even/odd to create a scissor pattern
                    float sideDir = (i % 2 == 0) ? 1.0f : -1.0f;
                    velX = perpX * speed * sideDir;
                    velY = perpY * speed * sideDir;
                }

                // Create the bullet
                Bullet bullet = new Bullet(
                    damage,
                    posX,
                    posY,
                    velX,
                    velY,
                    5.5f, // Size
                    // Vary color based on line type
                    new Color(baseColor).lerp(direction == 0 ? Color.YELLOW : Color.ORANGE, 0.3f),
                    false // Not healing
                );

                // Apply visual styling
                styleBullet(bullet, 1);

                bullets.add(bullet);
            }
        }
    }

    /**
     * Creates a simple circular pattern of bullets with rotation
     */
    private void createCircularPattern(List<Bullet> bullets, float enemyX, float enemyY,
                                     float playerX, float playerY, float speed,
                                     float minDamage, float maxDamage, Color baseColor) {
        float damage = MathUtils.random(minDamage, maxDamage);

        // Update rotation offset for next spawn
        circleRotationOffset += MathUtils.random(MathUtils.PI / 6, MathUtils.PI / 3);
        if (circleRotationOffset > MathUtils.PI2) {
            circleRotationOffset -= MathUtils.PI2;
        }

        // Create a circle of bullets
        for (int i = 0; i < CIRCLE_BULLETS; i++) {
            // Calculate angle for this bullet with rotation offset
            float angle = (i / (float)CIRCLE_BULLETS) * MathUtils.PI2 + circleRotationOffset;

            // Calculate direction based on angle
            float dirX = MathUtils.cos(angle);
            float dirY = MathUtils.sin(angle);

            // Add slight variations in speed based on position in circle
            float speedVariation = 0.9f + MathUtils.random(0.2f);

            // Create the bullet
            Bullet bullet = new Bullet(
                damage,
                enemyX,
                enemyY,
                dirX * speed * speedVariation,
                dirY * speed * speedVariation,
                6f, // Size
                // Vary color based on position in circle
                new Color(baseColor).lerp(new Color(0.1f, 0.7f, 0.9f, 1.0f), i / (float)CIRCLE_BULLETS),
                false // Not healing
            );

            // Apply visual styling
            styleBullet(bullet, 2);

            // Add telegraphing with variation based on position in circle
            float teleTime = 0.3f + (0.1f * (i % 3)); // Vary telegraph time slightly
            bullet.enableTelegraphing(teleTime, 0.2f);

            bullets.add(bullet);
        }
    }

    /**
     * Creates a healing bullet that moves straight toward the player
     */
    private void createHealingBullet(List<Bullet> bullets, float enemyX, float enemyY,
                                   float playerX, float playerY) {
        // Calculate random spawn offset from enemy
        float offsetX = MathUtils.random(-60f, 60f);
        float offsetY = MathUtils.random(-60f, 60f);
        float spawnX = enemyX + offsetX;
        float spawnY = enemyY + offsetY;

        // Calculate direction to player
        float dirX = playerX - spawnX;
        float dirY = playerY - spawnY;
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX = dirX / length;
        dirY = dirY / length;

        // Create healing bullet
        Bullet healingBullet = new Bullet(
            10f, // Healing amount
            spawnX,
            spawnY,
            dirX * 120f, // Speed
            dirY * 120f,
            9f, // Size
            new Color(0.3f, 0.7f, 0.3f, 1.0f), // Green color
            true // Is healing
        );

        // Style the healing bullet
        healingBullet.setShape(Bullet.Shape.HEART);
        healingBullet.setTrailLength(20);
        healingBullet.setGlowing(true);
        healingBullet.setGlowLayers(5);
        healingBullet.setGlowIntensity(0.2f);

        bullets.add(healingBullet);
    }

    /**
     * Applies visual styling to bullets based on the phase
     */
    private void styleBullet(Bullet bullet, int phaseType) {
        // Base styling for all bullets
        bullet.setTrailLength(25);
        bullet.setGlowing(true);
        bullet.setGlowLayers(3);
        bullet.setGlowIntensity(0.15f);
        bullet.setAutoRotate(true);

        // Phase-specific styling
        switch (phaseType) {
            case 0: // Burst pattern
                bullet.setShape(Bullet.Shape.TRIANGLE);
                bullet.setRotationSpeed(100f);
                break;

            case 1: // Linear pattern
                bullet.setShape(Bullet.Shape.SQUARE);
                break;

            case 2: // Circular pattern
                bullet.setShape(Bullet.Shape.CIRCLE);
                bullet.setRotationSpeed(120f);
                break;
        }
    }

    @Override
    public String getPatternName() {
        return "Cicada's Simple Melody";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
