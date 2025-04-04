package swu.cp112.silkblade.pattern.silkgod;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;

public class CrossfirePattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        3, 5, 1f, 0.09f,
        35, 600f, 500f, Color.WHITE, false,
        "Bullets fire continuously in horizontal and vertical lines", 2.5f
    );

    // Timing control variables - reduced intervals for more frequent spawns
    private float spawnTimer = 0f;
    private float horizontalSpawnInterval = 0.055f;  // Twice as fast
    private float verticalSpawnInterval = 0.055f;    // Twice as fast
    private float lastHorizontalSpawn = 0f;
    private float lastVerticalSpawn = 0f;

    // Track last spawn positions
    private float lastHorizontalY = 0f;
    private float lastVerticalX = 0f;
    private static final float MIN_SPAWN_SPACING = MathUtils.random(5, 20); // Reduced spacing
    private static final float MAX_BULLET_SPEED = 300f;

    // Add bullet count control variables
    private int activeHorizontalBullets = 0;
    private int activeVerticalBullets = 0;
    private static final int MAX_HORIZONTAL_BULLETS = 150;
    private static final int MAX_VERTICAL_BULLETS = 150;
    private static final float SCREEN_BUFFER = 100f; // Distance beyond screen to consider bullet destroyed

    // Add explosion control variables
    private float explosionSpawnInterval = 0.85f;  // Spawn exploding bullet every 1 second
    private float lastExplosionSpawn = 0f;
    private List<Bullet> activeExplosions = new ArrayList<>();

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                        float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Constants for bullet properties
        float horizontalSpeed = 250f;
        float verticalSpeed = 250f;
        float bulletSize = 12f;  // Slightly smaller bullets

        // Scale damage based on enemy's attack damage with defense penetration
        float enemyAttackDamage = enemy.getAttackDamage();
        float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.15f); // Increases with enemy attack
        float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
        float realDamage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));
        // Update timers
        spawnTimer += Gdx.graphics.getDeltaTime();

        // Spawn exploding bullet periodically
        if (spawnTimer - lastExplosionSpawn >= explosionSpawnInterval) {
            lastExplosionSpawn = spawnTimer;

            // Create main homing explosive bullet
            float spawnX = arenaX + arenaWidth / 2;
            float spawnY = arenaY + arenaHeight + 50;
            float initialSpeed = 200f;
            float explosiveBulletSize = 20f;

            Bullet explosiveBullet = new Bullet(
                realDamage,
                spawnX,
                spawnY,
                0,
                -initialSpeed,
                explosiveBulletSize,
                Color.PURPLE,
                false,
                true,  // Enable homing
                5.0f,  // Homing duration
                800f   // Homing strength
            ) {
                private float existenceTime = 0f;
                private final float maxExistenceTime = 3f;
                private boolean hasTriggeredExplosion = false;

                @Override
                public void update(float delta, float playerX, float playerY) {
                    updateTarget(playerX, playerY);
                    super.update(delta, playerX, playerY);
                    existenceTime += delta;

                    // Check if bullet should explode
                    if (!hasTriggeredExplosion &&
                        (existenceTime >= maxExistenceTime ||
                         y < arenaY || // Below arena
                         y > arenaY + arenaHeight + 100)) { // Above arena + buffer
                        hasTriggeredExplosion = true;
                        startExplosionTimer(0.5f);
                    }
                }
            };

            // Update the pass player callback
            explosiveBullet.setOnPassPlayerCallback(() -> {
                if (explosiveBullet.getOnExplodeCallback() != null && !explosiveBullet.hasExplosionTimer()) {
                    explosiveBullet.startExplosionTimer(0.5f);
                }
            });

            // Set explosion effect
            explosiveBullet.setOnExplodeCallback(() -> {
                // Trigger screen shake through the enemy
                if (enemy instanceof AbstractEnemy) {
                    ((AbstractEnemy) enemy).triggerScreenShake(0.4f, 6.0f); // Duration: 0.2s, Intensity: 6.0
                }

                int explosionBullets = 16;
                float smallBulletDamage = realDamage * 0.5f;
                float explosionSpeed = 200f;

                // Calculate center position for explosion
                float explosionX = explosiveBullet.getX();
                float explosionY = explosiveBullet.getY();

                // Create ring of bullets
                for (int i = 0; i < explosionBullets; i++) {
                    final int bulletIndex = i; // Create final variable to use in inner class
                    float angle = (i * (360f / explosionBullets)) * MathUtils.degreesToRadians;
                    float vx = MathUtils.cos(angle) * explosionSpeed;
                    float vy = MathUtils.sin(angle) * explosionSpeed;

                    Bullet smallBullet = new Bullet(
                        smallBulletDamage,
                        explosionX,
                        explosionY,
                        vx,
                        vy,
                        12f,
                        Color.WHITE,
                        false
                    ) {
                        private float currentSpeed = explosionSpeed;
                        private float rotationAngle = angle;
                        private float rotationSpeed = 6f;  // Increased from 2f for faster rotation
                        private float arcRadius = 120f;    // Increased from 50f for wider arcs
                        private final float rotationDirection = ((bulletIndex / 2) % 2 == 0) ? 1f : -1f;
                        private final float originalAngle = angle;
                        private float time = 0f;  // Add time tracking for arc decay

                        @Override
                        public void update(float delta, float playerX, float playerY) {
                            time += delta;
                            currentSpeed = Math.min(currentSpeed * 1.015f, MAX_BULLET_SPEED);

                            // Update rotation angle
                            rotationAngle += rotationSpeed * rotationDirection * delta;

                            // Use the original angle for base motion to maintain direction
                            float spiralX = MathUtils.cos(originalAngle) * currentSpeed;
                            float spiralY = MathUtils.sin(originalAngle) * currentSpeed;

                            // Add circular motion with gradual decay
                            float arcDecay = Math.max(0, 1f - (time * 0.5f)); // Slower decay
                            float currentArcRadius = arcRadius * arcDecay;
                            float circularX = MathUtils.cos(rotationAngle) * currentArcRadius;
                            float circularY = MathUtils.sin(rotationAngle) * currentArcRadius;

                            // Combine motions
                            velocityX = spiralX + circularX;
                            velocityY = spiralY + circularY;

                            super.update(delta, playerX, playerY);
                            x += velocityX * delta;
                            y += velocityY * delta;
                        }
                    };
                    smallBullet.enableRainbow(0.7f, 0.7f, 1.0f);
                    smallBullet.setShape(Bullet.Shape.STAR);
                    smallBullet.setSpinDirectionMatchesMovement(true);
                    smallBullet.setGlowing(true);  // Enable glow
                    smallBullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
                    smallBullet.setGlowIntensity(0.3f);
                    explosiveBullet.getExplosionBullets().add(smallBullet);
                }
            });
            explosiveBullet.setShape(Bullet.Shape.STAR);
            explosiveBullet.setSpinDirectionMatchesMovement(true);
            explosiveBullet.setGlowing(true);  // Enable glow
            explosiveBullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
            explosiveBullet.setGlowIntensity(0.3f);
            bullets.add(explosiveBullet);
        }

        // Spawn horizontal bullets if under limit
        if (spawnTimer - lastHorizontalSpawn >= horizontalSpawnInterval
            && activeHorizontalBullets < MAX_HORIZONTAL_BULLETS) {
            lastHorizontalSpawn = spawnTimer;

            // Generate different Y positions for left and right bullets
            float leftY, rightY;
            do {
                leftY = MathUtils.random(arenaY + MIN_SPAWN_SPACING,
                                     arenaY + arenaHeight - MIN_SPAWN_SPACING);
            } while (Math.abs(leftY - lastHorizontalY) < MIN_SPAWN_SPACING);

            do {
                rightY = MathUtils.random(arenaY + MIN_SPAWN_SPACING,
                                      arenaY + arenaHeight - MIN_SPAWN_SPACING);
            } while (Math.abs(rightY - lastHorizontalY) < MIN_SPAWN_SPACING
                    || Math.abs(rightY - leftY) < MIN_SPAWN_SPACING);

            lastHorizontalY = leftY; // Update last spawn position

            // Left side bullet
            Bullet leftBullet = new Bullet(
                realDamage,
                arenaX - MathUtils.random(20, 40),
                leftY,  // Use leftY for left bullet
                horizontalSpeed,
                0f,
                bulletSize,
                Color.WHITE,
                false
            ) {
                private float currentSpeed = horizontalSpeed;

                @Override
                public void update(float delta, float playerX, float playerY) {
                    super.update(delta, playerX, playerY);
                    currentSpeed = Math.min(currentSpeed * 1.015f, MAX_BULLET_SPEED);
                    velocityX = currentSpeed;

                    // Decrease counter when bullet is off screen
                    if (x > arenaX + arenaWidth + SCREEN_BUFFER) {
                        activeHorizontalBullets--;
                    }
                }
            };
            leftBullet.setShape(Bullet.Shape.STAR);
            leftBullet.setSpinDirectionMatchesMovement(true);
            leftBullet.setGlowing(true);
            leftBullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
            leftBullet.setGlowIntensity(0.3f);
            bullets.add(leftBullet);
            activeHorizontalBullets++;

            // Right side bullet
            Bullet rightBullet = new Bullet(
                realDamage,
                arenaX + arenaWidth + MathUtils.random(20, 40),
                rightY,  // Use rightY for right bullet
                -horizontalSpeed,
                0f,
                bulletSize,
                Color.WHITE,
                false
            ) {
                private float currentSpeed = horizontalSpeed;

                @Override
                public void update(float delta, float playerX, float playerY) {
                    super.update(delta, playerX, playerY);
                    currentSpeed = Math.min(currentSpeed * 1.015f, MAX_BULLET_SPEED);
                    velocityX = -currentSpeed;

                    // Decrease counter when bullet is off screen
                    if (x < arenaX - SCREEN_BUFFER) {
                        activeHorizontalBullets--;
                    }
                }
            };
            rightBullet.setShape(Bullet.Shape.STAR);
            rightBullet.setSpinDirectionMatchesMovement(true);
            rightBullet.setGlowing(true);  // Enable glow
            rightBullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
            rightBullet.setGlowIntensity(0.3f);
            bullets.add(rightBullet);
            activeHorizontalBullets++;
        }

        // Spawn vertical bullets if under limit
        if (spawnTimer - lastVerticalSpawn >= verticalSpawnInterval
            && activeVerticalBullets < MAX_VERTICAL_BULLETS) {
            lastVerticalSpawn = spawnTimer;

            float newX;
            do {
                newX = MathUtils.random(arenaX + MIN_SPAWN_SPACING,
                                      arenaX + arenaWidth - MIN_SPAWN_SPACING);
            } while (Math.abs(newX - lastVerticalX) < MIN_SPAWN_SPACING);

            lastVerticalX = newX;

            Bullet verticalBullet = new Bullet(
                realDamage,
                newX,
                arenaY - MathUtils.random(20, 40),
                0f,
                verticalSpeed,
                bulletSize,
                Color.WHITE,
                false
            ) {
                private float currentSpeed = verticalSpeed;

                @Override
                public void update(float delta, float playerX, float playerY) {
                    super.update(delta, playerX, playerY);
                    currentSpeed = Math.min(currentSpeed * 1.015f, MAX_BULLET_SPEED);
                    velocityY = currentSpeed;

                    // Decrease counter when bullet is off screen
                    if (y > arenaY + arenaHeight + SCREEN_BUFFER) {
                        activeVerticalBullets--;
                    }
                }
            };
            verticalBullet.setShape(Bullet.Shape.STAR);
            verticalBullet.setSpinDirectionMatchesMovement(true);
            verticalBullet.setGlowing(true);  // Enable glow
            verticalBullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
            verticalBullet.setGlowIntensity(0.3f);
            bullets.add(verticalBullet);
            activeVerticalBullets++;
        }

        // Add any active explosion bullets
        bullets.addAll(activeExplosions);
        activeExplosions.clear();

        return bullets;
    }

    @Override
    public String getPatternName() {
        return "Galactic Blaze";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
