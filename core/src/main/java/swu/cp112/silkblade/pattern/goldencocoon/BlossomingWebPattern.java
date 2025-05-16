package swu.cp112.silkblade.pattern.goldencocoon;

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
 * BlossomingWebPattern - The third attack pattern for the Golden Cocoon boss
 * Features an expanding web pattern that forces continuous movement.
 *
 * Pattern consists of:
 * 1. Web rings that expand outward from the center
 * 2. Web walls that sweep across the arena
 * 3. Golden orbs that bounce off arena boundaries
 */
public class BlossomingWebPattern implements EnemyAttackPattern {
    // Pattern configuration
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        8, 11, 3.5f, 0.65f, 22, 400f, 400f,
        new Color(1.0f, 0.9f, 0.2f, 1.0f), false,
        "Blossoming Web", 1f
    );

    // Spawn cycle tracking
    private int webRingCount = 0;
    private static final int MAX_WEB_RINGS = 6;
    private int patternPhase = 0; // 0: rings, 1: walls, 2: bouncing orbs

    // Web ring parameters
    private static final int BULLETS_PER_RING = 16;
    private static final float RING_SPEED = 350f;
    private float ringRotation = 0f;
    private boolean rotateClockwise = true;

    // Wall pattern parameters
    private static final int WALL_POINTS = 8;
    private static final float WALL_SPEED = 750f;

    // Bouncing orb parameters
    private static final int MAX_BOUNCING_ORBS = 4;
    private static final float ORB_SPEED = 400f;
    private int bouncingOrbsCreated = 0;

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

        // Arena center
        float centerX = arenaX + arenaWidth / 2;
        float centerY = arenaY + arenaHeight / 2;

        // Calculate damage based on enemy's attack damage
        float baseDamage = MathUtils.random(CONFIG.getMinDamage(), CONFIG.getMaxDamage());
        float scaledDamage = baseDamage * (1.0f + (enemy.getAttackDamage() * 0.1f));

        // Check if it's time to change pattern phase
        if (patternPhase == 0 && webRingCount >= MAX_WEB_RINGS) {
            patternPhase = 1; // Switch to wall pattern
            webRingCount = 0;
            // Randomize ring rotation direction for next time
            rotateClockwise = MathUtils.randomBoolean();
        } else if (patternPhase == 1 && webRingCount >= MAX_WEB_RINGS) {
            patternPhase = 2; // Switch to bouncing orbs
            webRingCount = 0;
            bouncingOrbsCreated = 0;
        } else if (patternPhase == 2 && bouncingOrbsCreated >= MAX_BOUNCING_ORBS) {
            patternPhase = 0; // Reset to web rings
            webRingCount = 0;
            bouncingOrbsCreated = 0;
            // Randomize ring rotation direction for next time
            rotateClockwise = MathUtils.randomBoolean();
        }

        // Generate bullets based on spawn cycle and pattern phase
        switch (patternPhase) {
            case 0: // Web rings
                generateWebRing(bullets, enemy, centerX, centerY, scaledDamage);
                generateWebRing(bullets, enemy, centerX, centerY, scaledDamage);
                webRingCount++;
                break;

            case 1: // Web walls
                generateWebWall(bullets, enemy, centerX, centerY,
                    arenaWidth, arenaHeight, scaledDamage, playerX, playerY);
                webRingCount++;
                break;

            case 2: // Bouncing orbs
                if (bouncingOrbsCreated < MAX_BOUNCING_ORBS) {
                    generateBouncingOrb(bullets, enemy, playerX, playerY,
                        arenaX, arenaY, arenaWidth, arenaHeight, scaledDamage);
                    bouncingOrbsCreated++;
                }
                break;
        }

        return bullets;
    }

    /**
     * Generate an expanding web ring pattern from the center of the arena
     * Now with rotation that changes direction randomly
     */
    private void generateWebRing(List<Bullet> bullets, Enemy enemy,
                               float centerX, float centerY, float damage) {
        // Calculate initial ring radius (increases with each subsequent ring)
        float initialRadius = 20f + (webRingCount * 10f);

        // Update ring rotation for each new ring
        if (rotateClockwise) {
            ringRotation += 5f; // Clockwise rotation
        } else {
            ringRotation -= 5f; // Counter-clockwise rotation
        }

        // Keep rotation in 0-360 range
        if (ringRotation > 360f) ringRotation -= 360f;
        if (ringRotation < 0f) ringRotation += 360f;

        // Create a ring of bullets
        for (int i = 0; i < BULLETS_PER_RING; i++) {
            // Calculate angle with rotation offset
            float angle = (i * MathUtils.PI2 / BULLETS_PER_RING) + (ringRotation * MathUtils.degreesToRadians);

            // Calculate spawn position on the ring
            float spawnX = centerX + MathUtils.cos(angle) * initialRadius;
            float spawnY = centerY + MathUtils.sin(angle) * initialRadius;

            // Calculate direction (radially outward)
            float dirX = MathUtils.cos(angle);
            float dirY = MathUtils.sin(angle);

            // Create bullet
            Bullet bullet = new Bullet(
                damage,
                spawnX, spawnY,
                dirX * RING_SPEED,
                dirY * RING_SPEED,
                7f,
                new Color(1.0f, 0.9f, 0.1f, 0.8f),
                false
            );

            // Style the bullet as a thread segment
            stylizeRingBullet(bullet);

            bullets.add(bullet);
        }
    }

    /**
     * Generate a web wall that sweeps across the arena
     * Now targets the player's actual position instead of fixed positions
     */
    private void generateWebWall(List<Bullet> bullets, Enemy enemy,
                               float centerX, float centerY,
                               float arenaWidth, float arenaHeight,
                               float damage, float playerX, float playerY) {
        // Determine wall orientation (alternating vertical and horizontal)
        boolean isVertical = webRingCount % 2 == 0;

        // Calculate player position relative to center
        float playerOffsetX = playerX - centerX;
        float playerOffsetY = playerY - centerY;

        // Determine which side the wall should come from based on player's actual position
        // This ensures the walls will actually target where the player is
        boolean fromRight, fromTop;

        // Add some randomness to prevent predictability
        // 30% chance to spawn from opposite side where player expects
        if (MathUtils.random() < 0.7f) {
            // Normal behavior - wall comes from opposite side of player's position
            fromRight = playerOffsetX < 0;  // If player is left of center, wall comes from right
            fromTop = playerOffsetY < 0;    // If player is below center, wall comes from top
        } else {
            // Surprise! Wall comes from same side as player
            fromRight = playerOffsetX >= 0; // If player is right of center, wall still comes from right
            fromTop = playerOffsetY >= 0;   // If player is above center, wall still comes from top
        }

        // Calculate the wall's starting position
        // Using a random offset from the edge to increase unpredictability
        float edgeRandomOffset = MathUtils.random(-50f, 50f);
        float startX, startY;

        if (isVertical) {
            // For vertical walls, vary the x-position but target player's y-position
            startX = fromRight ?
                    centerX + arenaWidth/2 + edgeRandomOffset :
                    centerX - arenaWidth/2 - edgeRandomOffset;
            // Target the player's Y position instead of center
            // This means vertical walls will actually aim at the player's height
            startY = playerY;
        } else {
            // For horizontal walls, vary the y-position but target player's x-position
            // Target the player's X position instead of center
            // This means horizontal walls will actually aim at the player's position
            startX = playerX;
            startY = fromTop ?
                    centerY + arenaHeight/2 + edgeRandomOffset :
                    centerY - arenaHeight/2 - edgeRandomOffset;
        }

        // Calculate the wall's moving direction (toward player's position)
        float dirX = isVertical ? (fromRight ? -1 : 1) : 0;
        float dirY = isVertical ? 0 : (fromTop ? -1 : 1);

        // Create a line of bullets for the wall
        float wallLength = isVertical ? arenaHeight : arenaWidth;
        float spacing = wallLength / WALL_POINTS;

        // Add some variation to wall length for unpredictability
        float lengthVariation = MathUtils.random(0.8f, 1.2f);
        wallLength *= lengthVariation;

        for (int i = 0; i < WALL_POINTS; i++) {
            float posOffset = i * spacing - wallLength/2;
            float bulletX = startX + (isVertical ? 0 : posOffset);
            float bulletY = startY + (isVertical ? posOffset : 0);

            // Add slight positional jitter to make walls less perfectly straight
            if (MathUtils.random() < 0.3f) {
                float jitter = MathUtils.random(-5f, 5f);
                if (isVertical) {
                    bulletX += jitter;
                } else {
                    bulletY += jitter;
                }
            }

            // Create bullet with slightly varying speeds
            float speedVariation = MathUtils.random(0.9f, 1.1f);

            Bullet bullet = new Bullet(
                damage * 0.8f, // Slightly reduced damage for wall bullets
                bulletX, bulletY,
                dirX * WALL_SPEED * speedVariation,
                dirY * WALL_SPEED * speedVariation,
                9f,
                new Color(1.0f, 0.85f, 0.1f, 0.9f),
                false
            );

            // Style the bullet
            stylizeWallBullet(bullet);

            // Add telegraphing to wall bullets
            bullet.enableTelegraphing(0.7f, 0.2f);

            bullets.add(bullet);
        }
    }

    /**
     * Generate an exploding golden orb that bursts into a cross pattern after a delay
     */
    private void generateBouncingOrb(List<Bullet> bullets, Enemy enemy,
                                   float playerX, float playerY,
                                   float arenaX, float arenaY,
                                   float arenaWidth, float arenaHeight,
                                   float damage) {
        // Determine spawn position (random edge of arena)
        float spawnX, spawnY;
        float edgeChoice = MathUtils.random(0f, 1f);

        if (edgeChoice < 0.25f) {
            // Top edge
            spawnX = arenaX + MathUtils.random(0f, arenaWidth);
            spawnY = arenaY + arenaHeight;
        } else if (edgeChoice < 0.5f) {
            // Right edge
            spawnX = arenaX + arenaWidth;
            spawnY = arenaY + MathUtils.random(0f, arenaHeight);
        } else if (edgeChoice < 0.75f) {
            // Bottom edge
            spawnX = arenaX + MathUtils.random(0f, arenaWidth);
            spawnY = arenaY;
        } else {
            // Left edge
            spawnX = arenaX;
            spawnY = arenaY + MathUtils.random(0f, arenaHeight);
        }

        // Calculate direction toward player with randomness
        float dirX = playerX - spawnX;
        float dirY = playerY - spawnY;

        // Add randomness to direction
        dirX += MathUtils.random(-50f, 50f);
        dirY += MathUtils.random(-50f, 50f);

        // Normalize direction
        float length = (float)Math.sqrt(dirX * dirX + dirY * dirY);
        dirX /= length;
        dirY /= length;

        // Create exploding orb bullet
        Bullet bullet = new Bullet(
            damage * 1.25f, // Higher damage for exploding orbs
            spawnX, spawnY,
            dirX * ORB_SPEED,
            dirY * ORB_SPEED,
            12f,
            new Color(1.0f, 0.85f, 0.0f, 1.0f),
            false
        );

        // Style the exploding orb
        stylizeBouncingOrb(bullet);

        // Add telegraphing to make it clear this is a special bullet
        bullet.enableTelegraphing(0.5f, 0.2f);

        // Randomly decide if cross pattern will be cardinal or diagonal
        final boolean useDiagonalPattern = MathUtils.randomBoolean();

        // Set up explosion callback
        bullet.setOnExplodeCallback(() -> {
            // Create explosion fragment bullets
            int fragmentCount = 4; // 4 bullets in a cross pattern
            float fragmentSpeed = 700f;
            float fragmentDamage = damage * 0.85f;

            // Clear any existing explosion bullets
            bullet.getExplosionBullets().clear();

            // Calculate center position for explosion
            float explosionX = bullet.getX();
            float explosionY = bullet.getY();

            // Create fragments in a cross pattern
            for (int j = 0; j < fragmentCount; j++) {
                // Calculate angle based on pattern type
                float fragAngle;
                if (useDiagonalPattern) {
                    // Diagonal directions (45, 135, 225, 315 degrees)
                    fragAngle = (j * 90f + 45f) * MathUtils.degreesToRadians;
                } else {
                    // Cardinal directions (0, 90, 180, 270 degrees)
                    fragAngle = j * 90f * MathUtils.degreesToRadians;
                }

                float vx = MathUtils.cos(fragAngle) * fragmentSpeed;
                float vy = MathUtils.sin(fragAngle) * fragmentSpeed;

                Bullet fragment = new Bullet(
                    fragmentDamage,
                    explosionX,
                    explosionY,
                    vx,
                    vy,
                    8f,
                    new Color(1.0f, 0.7f, 0.0f, 0.9f),
                    false
                );
                fragment.setShape(Bullet.Shape.DIAMOND);
                fragment.setAutoRotate(true);
                // Add disco effect
                fragment.setDisco(true, true, false, 1.0f, 0.7f, 0.0f);
                fragment.setDiscoSpeed(2.0f);

                // Add trailing effect
                fragment.setTrailLength(30);
                fragment.setGlowing(true);
                fragment.setGlowLayers(3);
                fragment.setGlowIntensity(0.3f);

                // Add to explosion bullets collection
                bullet.getExplosionBullets().add(fragment);
            }

            // Trigger screen shake if enemy is AbstractEnemy
            if (enemy instanceof AbstractEnemy) {
                ((AbstractEnemy) enemy).triggerScreenShake(0.2f, 3.0f);
            }
        });

        // Set a timer for the explosion
        bullet.startExplosionTimer(0.95f);

        bullets.add(bullet);
    }

    /**
     * Apply visual styling to ring bullets
     */
    private void stylizeRingBullet(Bullet bullet) {
        bullet.setShape(Bullet.Shape.DIAMOND);
        bullet.setTrailLength(15);
        bullet.setGlowing(true);
        bullet.setGlowIntensity(0.3f);
        bullet.enableTelegraphing(0.5f, 0.1f);
    }

    /**
     * Apply visual styling to wall bullets
     */
    private void stylizeWallBullet(Bullet bullet) {
        bullet.setShape(Bullet.Shape.SQUARE);
        bullet.setTrailLength(20);
        bullet.setGlowing(true);
        bullet.setGlowIntensity(0.4f);
    }

    /**
     * Apply visual styling to bouncing orbs
     */
    private void stylizeBouncingOrb(Bullet bullet) {
        bullet.setShape(Bullet.Shape.CIRCLE);
        bullet.setTrailLength(25);
        bullet.setGlowing(true);
        bullet.setGlowLayers(3);
        bullet.setGlowIntensity(0.5f);

        // Add pulsating effect to indicate it will explode
        bullet.startSpinning(180f);
        bullet.setDisco(true, true, false, 1.0f, 0.7f, 0.0f);
        bullet.setDiscoSpeed(3.0f);
    }

    @Override
    public String getPatternName() {
        return "Blossoming Web";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
