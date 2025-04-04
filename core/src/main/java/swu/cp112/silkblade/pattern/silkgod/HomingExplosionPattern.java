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

public class HomingExplosionPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        4, 6, 2.0f, 0.75f, 4, 600f, 500f, Color.YELLOW, false,
        "A large homing bullet that explodes into smaller bullets", 4f // Longer delay to allow explosions to play out
    );

    private List<Bullet> activeExplosions = new ArrayList<>();

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                        float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Get player's position
        float targetX = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerX() :
            arenaX + arenaWidth / 2;
        float targetY = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerY() :
            arenaY + arenaHeight / 2;

        // Create main homing bullet
        float spawnX = arenaX + CONFIG.getArenaWidth() / 2;
        float spawnY = arenaY + CONFIG.getArenaHeight() + 50;
        float initialSpeed = 200f;

        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.15f);
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float mainBulletDamage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));
        Bullet mainBullet = new Bullet(
            mainBulletDamage,
            spawnX,
            spawnY,
            0,
            -initialSpeed,
            20f,
            new Color(0.95f, 0.95f, 1f, 1f),
            false,
            true,  // Enable homing
            12.0f,  // Homing duration
            1000f  // Homing strength
        ) {
            private float existenceTime = 0f;
            private final float maxExistenceTime = 5f; // Explode after 5 seconds

            @Override
            public void update(float delta, float playerX, float playerY) {
                // Update the target to the current player position
                updateTarget(playerX, playerY);

                super.update(delta, playerX, playerY);
                existenceTime += delta;

                // Automatically explode if existed for too long
                if (existenceTime >= maxExistenceTime && getOnExplodeCallback() == null) {
                    startExplosionTimer(1f);
                }
            }
        };

        // Initial update of target position
        mainBullet.updateTarget(targetX, targetY);

        // Set explosion callback
        mainBullet.setOnPassPlayerCallback(() -> {
            mainBullet.startExplosionTimer(1f);
        });

        // Set explosion effect
        mainBullet.setOnExplodeCallback(() -> {
            // Trigger screen shake through the enemy
            if (enemy instanceof AbstractEnemy) {
                ((AbstractEnemy) enemy).triggerScreenShake(1f, 10.0f); // Duration: 0.3s, Intensity: 8.0
            }

            int innerRingBullets = 8;  // Number of bullets in the inner ring
            int outerRingBullets = 12;  // Number of bullets in the outer ring
            float smallBulletDamage = mainBulletDamage * 0.5f;
            float innerRingSpeed = 50f;
            float outerRingSpeed = 150f;  // Slightly faster outer ring
            float outerRingOffset = 360f / (outerRingBullets * 2); // Offset by half the angle between bullets

            // Clear any existing explosion bullets
            mainBullet.getExplosionBullets().clear();

            // Calculate center position for explosion
            float explosionX = mainBullet.getX();
            float explosionY = mainBullet.getY();

            // Create inner ring with slow yellow disco effect
            createBulletRing(mainBullet, innerRingBullets, explosionX, explosionY,
                           innerRingSpeed, smallBulletDamage, 8f, Color.YELLOW, 0f);

            // Create outer ring with offset - also with slow yellow disco effect
            createBulletRing(mainBullet, outerRingBullets, explosionX, explosionY,
                           outerRingSpeed, smallBulletDamage, 6f, Color.YELLOW, outerRingOffset);

//            System.out.println("Bullet exploded! Created " + (innerRingBullets + outerRingBullets) + " accelerating bullets");
        });
        // Set slow yellow disco effect for main bullet
        mainBullet.setShape(Bullet.Shape.HEXAGON);
        mainBullet.setDisco(true, false, true, 0.3f, 0.8f, 1.0f);
        mainBullet.setSpinDirectionMatchesMovement(true);
        mainBullet.setDiscoSpeed(1.5f);
        mainBullet.setGlowing(true);  // Enable glow
        mainBullet.setGlowLayers(16);  // Optional: Set custom number of glow layers
        mainBullet.setGlowIntensity(0.1f);
        bullets.add(mainBullet);
        bullets.addAll(activeExplosions); // This will add any explosion bullets that were created
        activeExplosions.clear(); // Clear the list for the next update

        return bullets;
    }

    // Updated helper method with offset parameter
    private void createBulletRing(Bullet mainBullet, int numBullets, float centerX, float centerY,
                                float baseSpeed, float damage, float radius, Color color, float angleOffset) {
        // Determine rotation direction for all bullets in this ring
        float ringRotationDirection = MathUtils.randomBoolean() ? 1f : -1f;

        for (int i = 0; i < numBullets; i++) {
            float initialAngle = ((i * (360f / numBullets)) + angleOffset) * MathUtils.degreesToRadians;
            float vx = MathUtils.cos(initialAngle) * baseSpeed;
            float vy = MathUtils.sin(initialAngle) * baseSpeed;

            Bullet smallBullet = new Bullet(
                damage,
                centerX,
                centerY,
                vx,
                vy,
                radius,
                color,
                false
            ) {
                private float currentSpeed = baseSpeed;
                private float rotationAngle = initialAngle;
                private float rotationSpeed = 2f;
                private float arcRadius = 50f;
                private float lastTrailX = x;
                private float lastTrailY = y;
                private static final float MIN_TRAIL_DISTANCE = 8f;
                private final float rotationDirection = ringRotationDirection;

                @Override
                public void update(float delta, float playerX, float playerY) {
                    currentSpeed = Math.min(currentSpeed * 1.055f, 1200f);

                    rotationAngle += rotationSpeed * rotationDirection * delta;

                    float baseAngle = MathUtils.atan2(velocityY, velocityX);
                    float spiralX = MathUtils.cos(baseAngle) * currentSpeed;
                    float spiralY = MathUtils.sin(baseAngle) * currentSpeed;

                    float circularX = MathUtils.cos(rotationAngle) * arcRadius;
                    float circularY = MathUtils.sin(rotationAngle) * arcRadius;

                    velocityX = spiralX + circularX;
                    velocityY = spiralY + circularY;

                    float distanceFromLastTrail = (float) Math.sqrt(
                        (x - lastTrailX) * (x - lastTrailX) +
                        (y - lastTrailY) * (y - lastTrailY)
                    );

                    if (distanceFromLastTrail >= MIN_TRAIL_DISTANCE) {
                        updateTrail();
                        lastTrailX = x;
                        lastTrailY = y;
                    }

                    super.update(delta, playerX, playerY);
                }
            };

            // Enable telegraphing with trajectory prediction

            smallBullet.setTrailLength(28);

            // Configure slow yellow disco effect for all bullets
            smallBullet.setDisco(true, true, false, 0.8f, 0.8f, 0.2f);
            smallBullet.setShape(Bullet.Shape.STAR);
            smallBullet.setSpinDirectionMatchesMovement(true);
            // Slightly vary the disco speed, but keep it slow (2.0-3.0 seconds per cycle)
            smallBullet.setDiscoSpeed(2.0f + MathUtils.random(0.0f, 1.0f));
            smallBullet.setGlowing(true);  // Enable glow
            smallBullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
            smallBullet.setGlowIntensity(0.3f);
            mainBullet.getExplosionBullets().add(smallBullet);
        }
    }

    @Override
    public String getPatternName() {
        return "Singularity Burst";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
