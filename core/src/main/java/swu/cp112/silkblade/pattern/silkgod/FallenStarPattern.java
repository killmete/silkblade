package swu.cp112.silkblade.pattern.silkgod;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;

import java.util.ArrayList;
import java.util.List;

public class FallenStarPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        5, 10, 1.0f, 0.45f, 25, 500f, 250f, Color.WHITE, false,
        "Swarm of stars fall from heaven, exploding into shower of brilliance.", 4.25f
    );

    private List<Bullet> activeExplosions = new ArrayList<>();
    private int spiralDirectionCounter = 0; // Counter to alternate spiral directions
    private int countBullets = 0;
    private int maxRegularBullets = CONFIG.getMaxBullets() - 1; // Maximum number of regular bullets before massive star
    private boolean patternComplete = false; // Flag to indicate if the pattern is complete

    // Sound related fields
    private Sound starSpawnSound;
    private Sound starExplosionSound;
    private boolean soundsInitialized = false;

    // Initialize sounds
    private void initializeSounds() {
        if (!soundsInitialized) {
            starSpawnSound = Gdx.audio.newSound(Gdx.files.internal("sounds/star_spawn.wav"));
            starExplosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/starsplosion.wav"));
            soundsInitialized = true;
        }
    }

    // Dispose sounds when no longer needed
    public void dispose() {
        if (soundsInitialized) {
            if (starSpawnSound != null) starSpawnSound.dispose();
            if (starExplosionSound != null) starExplosionSound.dispose();
            soundsInitialized = false;
        }
    }

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                        float arenaWidth, float arenaHeight) {
        // Initialize sounds if not already done
        initializeSounds();

        List<Bullet> bullets = new ArrayList<>();

        // If pattern is complete, reset counters and start again
        if (patternComplete) {
            countBullets = 0;
            patternComplete = false;
        }

        // Generate regular stars until we reach maxRegularBullets
        if (countBullets < maxRegularBullets) {
            // Regular star bullet code
            float spawnX = arenaX + (CONFIG.getArenaWidth() * 0.5f) + MathUtils.random(0, CONFIG.getArenaWidth() * 2.3f);
            float spawnY = arenaY + CONFIG.getArenaHeight() * 3.2f;
            float initialSpeed = MathUtils.random(400, 500);
            float enemyAttackDamage = enemy.getAttackDamage();
            float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.15f);
            float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
            float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
            float mainBulletDamage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));

            // Play star spawn sound
            if (starSpawnSound != null) {
                long soundId = starSpawnSound.play(0.15f);
                // Add slight random pitch variation for each star (0.9-1.1 range)
                float randomPitch = 0.9f + MathUtils.random(0.0f, 0.3f);
                starSpawnSound.setPitch(soundId, randomPitch);
            }

            Bullet mainBullet = new Bullet(
                mainBulletDamage,
                spawnX,
                spawnY,
                -initialSpeed,
                -initialSpeed,
                70f,
                new Color(0.95f, 0.95f, 1f, 1f),
                false
            ) {
                private float existenceTime = 0f;
                private final float maxExistenceTime = 5f; // Explode after 5 seconds
                private boolean hasTriggeredExplosion = false;
                private final float explosionAreaTop = arenaY + arenaHeight; // Area starts at bottom of arena
                private final float explosionAreaBottom = arenaY - arenaHeight * 0.5f; // Area ends below arena

                @Override
                public void update(float delta, float playerX, float playerY) {
                    super.update(delta, playerX, playerY);
                    existenceTime += delta;

                    // Check if bullet is in the explosion area
                    if (!hasTriggeredExplosion && y <= explosionAreaTop && y >= explosionAreaBottom) {
                        hasTriggeredExplosion = true;
                        if (getOnExplodeCallback() != null) {
                            startExplosionTimer(0f);
                        }
                        return;
                    }

                    // Automatically explode if existed for too long
                    if (!hasTriggeredExplosion && existenceTime >= maxExistenceTime && getOnExplodeCallback() != null) {
                        hasTriggeredExplosion = true;
                        startExplosionTimer(0.5f);
                    }
                }
            };

            // Set explosion effect
            mainBullet.setOnExplodeCallback(() -> {
                // Play explosion sound
                if (starExplosionSound != null) {
                    long soundId = starExplosionSound.play(0.12f);
                    starExplosionSound.setPitch(soundId, 1.7f);
                }

                // Trigger screen shake through the enemy
                if (enemy instanceof AbstractEnemy) {
                    ((AbstractEnemy) enemy).triggerScreenShake(0.45f, 5.0f); // Duration: 0.3s, Intensity: 8.0
                }

                int outerRingBullets = 7;  // Number of bullets in the outer ring
                float smallBulletDamage = mainBulletDamage * 0.95f;
                float outerRingSpeed = 150f;  // Slightly faster outer ring
                float outerRingOffset = 360f / (outerRingBullets * 2); // Offset by half the angle between bullets

                // Clear any existing explosion bullets
                mainBullet.getExplosionBullets().clear();

                // Calculate center position for explosion
                float explosionX = mainBullet.getX();
                float explosionY = mainBullet.getY();

                // Create outer ring with offset - also with slow yellow disco effect
                createBulletRing(mainBullet, outerRingBullets, explosionX, explosionY,
                    outerRingSpeed, smallBulletDamage, 15f, Color.YELLOW, outerRingOffset);
            });
            // Set slow yellow disco effect for main bullet
            mainBullet.setShape(Bullet.Shape.STAR);
            mainBullet.enableRainbow(1f, 0.5f, 1.0f);  // Low saturation (0.3) for pastel effect, full value for brightness
            mainBullet.setRotationSpeed(300f);  // Set to 360 degrees per second for one full rotation per second
            mainBullet.setGlowing(true);  // Enable glow
            mainBullet.setGlowLayers(16);  // Optional: Set custom number of glow layers
            mainBullet.setGlowIntensity(0.05f);
            mainBullet.setTrailLength(50);
            bullets.add(mainBullet);

            countBullets++;
        }
        // Generate massive star only if we have fired all regular bullets
        else if (countBullets == maxRegularBullets) {
            // Massive star bullet code
            float spawnX = arenaX + (CONFIG.getArenaWidth() * 1.425f); // Spawn further right to ensure diagonal path to center
            float spawnY = arenaY + CONFIG.getArenaHeight() * 3.5f;
            float initialSpeed = 250f;
            float enemyAttackDamage = enemy.getAttackDamage();
            float defenseMultiplier = 1.0f + (enemyAttackDamage * 0.15f);
            float scaledMinDamage = (CONFIG.getMinDamage() + (CONFIG.getMinDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
            float scaledMaxDamage = (CONFIG.getMaxDamage() + (CONFIG.getMaxDamage() * (enemyAttackDamage - 1) * 0.2f)) * defenseMultiplier;
            float mainBulletDamage = Math.abs(MathUtils.random(scaledMinDamage, scaledMaxDamage));

            // Play massive star spawn sound (using regular spawn sound with lower pitch)
            if (starSpawnSound != null) {
                long soundId = starSpawnSound.play(0.25f);
                starSpawnSound.setPitch(soundId, 0.465f); // Lower pitch for massive star
                starSpawnSound.setVolume(soundId, 0.35f); // Slightly louder
            }

            Bullet mainBullet = new Bullet(
                mainBulletDamage,
                spawnX,
                spawnY,
                -initialSpeed,
                -initialSpeed,
                130f,
                new Color(0.95f, 0.95f, 1f, 1f),
                false
            ) {
                private float existenceTime = 0f;
                private final float maxExistenceTime = 5f; // Explode after 5 seconds
                private boolean hasTriggeredExplosion = false;
                private final float explosionAreaTop = arenaY + arenaHeight * 1.7f; // Area starts at bottom of arena
                private final float explosionAreaBottom = arenaY - arenaHeight * 0.5f; // Area ends below arena

                @Override
                public void update(float delta, float playerX, float playerY) {
                    super.update(delta, playerX, playerY);
                    existenceTime += delta;

                    // Check if bullet is in the explosion area
                    if (!hasTriggeredExplosion && y <= explosionAreaTop && y >= explosionAreaBottom) {
                        hasTriggeredExplosion = true;
                        if (getOnExplodeCallback() != null) {
                            startExplosionTimer(0f);
                        }
                        return;
                    }

                    // Automatically explode if existed for too long
                    if (!hasTriggeredExplosion && existenceTime >= maxExistenceTime && getOnExplodeCallback() != null) {
                        hasTriggeredExplosion = true;
                        startExplosionTimer(0.5f);
                    }
                }
            };

            // Set explosion effect
            mainBullet.setOnExplodeCallback(() -> {
                // Play massive star explosion sound (using regular explosion sound with lower pitch)
                if (starExplosionSound != null) {
                    long soundId = starExplosionSound.play(0.15f);
                    starExplosionSound.setPitch(soundId, 0.85f);
                }

                // Trigger screen shake through the enemy
                if (enemy instanceof AbstractEnemy) {
                    ((AbstractEnemy) enemy).triggerScreenShake(1.25f, 15.0f); // Duration: 0.3s, Intensity: 8.0
                }

                // Clear any existing explosion bullets
                mainBullet.getExplosionBullets().clear();

                // Calculate center position for explosion
                float explosionX = mainBullet.getX();
                float explosionY = mainBullet.getY();
                float ringRotationDirection = MathUtils.randomBoolean() ? 1.0f : -1.0f;
                for(int i = 0; i < 5; i++) {
                    int outerRingBullets = 32 - (i * 4);  // Number of bullets in the outer ring
                    float smallBulletDamage = mainBulletDamage * 0.95f;
                    float outerRingSpeed = 200f;  // Constant base speed for all rings
                    float outerRingOffset = 360f / (outerRingBullets * 2); // Offset by half the angle between bullets
                    float expansionMultiplier = 1.0f + (i * 0.2f); // Different expansion rate for each ring

                    // Create outer ring with offset - also with slow yellow disco effect
                    createMassiveRing(mainBullet, outerRingBullets, explosionX, explosionY,
                        outerRingSpeed, smallBulletDamage, 14f, Color.YELLOW, outerRingOffset, ringRotationDirection, expansionMultiplier);
                }
            });
            // Set slow yellow disco effect for main bullet
            mainBullet.setShape(Bullet.Shape.STAR);
            mainBullet.enableRainbow(1f, 0.7f, 1.0f);  // Low saturation (0.3) for pastel effect, full value for brightness
            mainBullet.setRotationSpeed(170f);  // Set to 360 degrees per second for one full rotation per second
            mainBullet.setGlowing(true);  // Enable glow
            mainBullet.setGlowLayers(16);  // Optional: Set custom number of glow layers
            mainBullet.setGlowIntensity(0.05f);
            mainBullet.setTrailLength(50);
            bullets.add(mainBullet);

            // Mark the pattern as complete to reset for the next cycle
            patternComplete = true;
        }

        // Add any explosion bullets that were created
        bullets.addAll(activeExplosions);
        activeExplosions.clear();

        return bullets;
    }
    private void createMassiveRing(Bullet mainBullet, int numBullets, float centerX, float centerY,
                                  float baseSpeed, float damage, float radius, Color color, float angleOffset, float ringRotationDirection, float expansionMultiplier) {
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
                private float lastTrailX = x;
                private float lastTrailY = y;
                private static final float MIN_TRAIL_DISTANCE = 8f;
                private float angle = initialAngle; // Store the initial angle
                private float spiralFactor = 0.45f; // Controls the tightness of the spiral
                private float spiralRadius = 0; // Starting radius of the spiral (all start from center)
                private float expansionRate = expansionMultiplier; // Different expansion rate for each ring

                @Override
                public void update(float delta, float playerX, float playerY) {
                    // Call super update first for lifetime and other internal logic
                    super.update(delta, playerX, playerY);

                    // Increase the spiral radius at different rates for each ring
                    spiralRadius += currentSpeed * delta * expansionRate;

                    // Rotate the angle for spiral movement
                    angle += spiralFactor * delta * ringRotationDirection;

                    // Calculate new position directly based on spiral formula
                    x = centerX + spiralRadius * MathUtils.cos(angle);
                    y = centerY + spiralRadius * MathUtils.sin(angle);

                    // Update velocity direction for proper orientation
                    velocityX = currentSpeed * MathUtils.cos(angle);
                    velocityY = currentSpeed * MathUtils.sin(angle);

                    float distanceFromLastTrail = (float) Math.sqrt(
                        (x - lastTrailX) * (x - lastTrailX) +
                            (y - lastTrailY) * (y - lastTrailY)
                    );

                    if (distanceFromLastTrail >= MIN_TRAIL_DISTANCE) {
                        updateTrail();
                        lastTrailX = x;
                        lastTrailY = y;
                    }
                }
            };

            // Enable telegraphing with trajectory prediction

            smallBullet.setTrailLength(12);

            // Configure slow yellow disco effect for all bullets
            smallBullet.enableRainbow(0.9f, 0.6f, 1.0f);  // Even lower saturation (0.25) for softer pastel effect
            smallBullet.setShape(Bullet.Shape.STAR);
            smallBullet.setSpinDirectionMatchesMovement(true);
            smallBullet.setGlowing(true);  // Enable glow
            smallBullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
            smallBullet.setGlowIntensity(0.2f);
            mainBullet.getExplosionBullets().add(smallBullet);
        }
    }
    // Updated helper method with offset parameter
    private void createBulletRing(Bullet mainBullet, int numBullets, float centerX, float centerY,
                                float baseSpeed, float damage, float radius, Color color, float angleOffset) {
        // Determine rotation direction for all bullets in this ring
        spiralDirectionCounter++;
        float ringRotationDirection = (spiralDirectionCounter % 2 == 0) ? 1.0f : -1.0f;

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
                private float lastTrailX = x;
                private float lastTrailY = y;
                private static final float MIN_TRAIL_DISTANCE = 8f;
                private float angle = initialAngle; // Store the initial angle
                private float spiralFactor = 0.65f; // Controls the tightness of the spiral
                private float spiralRadius = 0; // Starting radius of the spiral

                @Override
                public void update(float delta, float playerX, float playerY) {
                    // Continuously increase speed up to a maximum
                    currentSpeed = Math.min(currentSpeed * 1.055f, 400f);

                    // Store current position to restore after super.update
                    float oldX = x;
                    float oldY = y;

                    // Call super update first for lifetime and other internal logic
                    super.update(delta, playerX, playerY);

                    // Increase the spiral radius as the bullet moves outward
                    spiralRadius += currentSpeed * delta;

                    // Rotate the angle for spiral movement
                    angle += spiralFactor * delta * ringRotationDirection;

                    // Calculate new position directly based on spiral formula
                    x = centerX + spiralRadius * MathUtils.cos(angle);
                    y = centerY + spiralRadius * MathUtils.sin(angle);

                    // Update velocity direction for proper orientation
                    velocityX = currentSpeed * MathUtils.cos(angle);
                    velocityY = currentSpeed * MathUtils.sin(angle);

                    float distanceFromLastTrail = (float) Math.sqrt(
                        (x - lastTrailX) * (x - lastTrailX) +
                        (y - lastTrailY) * (y - lastTrailY)
                    );

                    if (distanceFromLastTrail >= MIN_TRAIL_DISTANCE) {
                        updateTrail();
                        lastTrailX = x;
                        lastTrailY = y;
                    }
                }
            };

            // Enable telegraphing with trajectory prediction

            smallBullet.setTrailLength(28);

            // Configure slow yellow disco effect for all bullets
            smallBullet.enableRainbow(0.9f, 0.4f, 1.0f);  // Even lower saturation (0.25) for softer pastel effect
            smallBullet.setShape(Bullet.Shape.STAR);
            smallBullet.setSpinDirectionMatchesMovement(true);
            smallBullet.setGlowing(true);  // Enable glow
            smallBullet.setGlowLayers(8);  // Optional: Set custom number of glow layers
            smallBullet.setGlowIntensity(0.3f);
            mainBullet.getExplosionBullets().add(smallBullet);
        }
    }

    @Override
    public String getPatternName() {
        return "Tears of Heaven";
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
