package swu.cp112.silkblade.entity.combat;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;


public class Bullet {
    // Constants
    private static final int TRAIL_LENGTH = 40;
    private static final float TRAIL_UPDATE_INTERVAL = 0.004f;
    private static final float DISCO_COLOR_SPEED = 1.0f;
    private static final float DEFAULT_TELEGRAPH_LENGTH = 2000f;
    private static final float TELEGRAPH_FADE_TIME = 2.0f;
    private static final int DEFAULT_GLOW_LAYERS = 3;
    private static final float GLOW_ALPHA_DECAY = 0.3f;
    private static final float GLOW_SIZE_MULTIPLIER = 1.3f;
    private static final float TRAIL_ALPHA = 0.3f;

    // Core properties
    private float damage;
    private boolean isHeal;
    private boolean active = true;
    private float size;
    private float width;
    private float height;
    private float alpha = 1.0f;

    // Position and movement
    public float x, y;
    public float velocityX, velocityY;
    public float originX, originY;
    private float originalSpeedX, originalSpeedY;
    private float slowdownFactor = 1.0f;
    private float prevX, prevY;

    // Visual properties
    private Color color;
    private float[][] trailPositions;
    private float trailTimer = 0f;

    // Disco properties
    private boolean discoR, discoG, discoB;
    private float discoTimer = 0f;
    private float discoSpeed = DISCO_COLOR_SPEED;
    private float baseR, baseG, baseB;

    // Homing properties
    private boolean isHoming;
    private float homingDuration;
    private float homingTimer;
    private float homingStrength;
    private float targetX, targetY;

    // Telegraph properties
    private boolean isTelegraphing = false;
    private float telegraphDuration = 0.0f;
    private float telegraphTimer = 0.0f;
    private float telegraphFadeTime = 0.0f;
    private float telegraphFadeTimer = 0.0f;
    private float telegraphLength = DEFAULT_TELEGRAPH_LENGTH;
    private float telegraphStartX, telegraphStartY;
    private float telegraphDirectionX, telegraphDirectionY;
    private float distanceTraveled = 0f;
    private float telegraphTotalLength = DEFAULT_TELEGRAPH_LENGTH;

    // Explosion properties
    private boolean hasPassedPlayer = false;
    private float explosionTimer = -1f;
    private float lastY;
    private List<Bullet> spawnedBullets = new ArrayList<>();

    // Callbacks
    private float actionDelayTimer = 0f;
    private float actionDelays;
    private Runnable onDestroyCallback;
    private Runnable onPassPlayerCallback;
    private Runnable onExplodeCallback;
    private Runnable delayedActions;

    // Add these new fields
    private BulletUpdateCallback updateCallback;
    private boolean hasUpdateCallback = false;

    // New fade-related properties
    private boolean isFading = false;
    private float fadeDuration = 1.0f;  // Default fade duration
    private float fadeTimer = 0f;

    // Add these properties with other instance variables
    private boolean isGlowing = false;
    private int glowLayers = DEFAULT_GLOW_LAYERS;
    private float glowIntensity = 1.0f;

    // Add these new fields near other disco-related properties
    private float discoColorRange = 0.5f; // Default range of color variation
    private boolean isRainbow = false;
    private float rainbowSpeed = 1.0f;
    private float rainbowHue = 0f;
    private float rainbowSaturation = 1.0f;
    private float rainbowValue = 1.0f;

    // Add these new fields near other properties
    private float rotation = 0f; // Current rotation in radians
    private boolean autoRotate = true; // Whether bullet should automatically rotate based on velocity

    // Add these new fields near other rotation-related properties
    private float rotationSpeed = 0f;         // Degrees per second
    private float targetRotationSpeed = 0f;   // Target rotation speed for acceleration/deceleration
    private float rotationAcceleration = 0f;  // How fast to change rotation speed
    private boolean useCustomRotation = false; // Whether to use custom rotation instead of auto-rotate

    // Add this new field near other rotation properties
    private float spinSpeedMultiplier = 1.0f;
    private boolean spinDirectionMatchesMovement = false;

    // Add these fields near other rotation variables
    private float maxSpinSpeed = Float.MAX_VALUE; // Default to no limit
    private boolean useMaxSpinSpeed = false;

    // Add this enum near the top of the class with other constants
    public enum Shape {
        CIRCLE,
        STAR,
        TRIANGLE,
        SQUARE,
        HEXAGON,     // New
        DIAMOND,     // New
        HEART,       // New
        CRESCENT     // New
    }

    // Add these instance variables with other properties
    private Shape shape = Shape.CIRCLE;
    private int numPoints = 5; // For star/polygon shapes

    // Add this interface
    public interface BulletUpdateCallback {
        boolean update(float deltaTime);
    }

    // Add these new fields
    private Color glowColor;
    private boolean rainbowGlow;

    // Add these fields with other instance variables
    private Sprite bulletSprite;
    private Sprite glowSprite;
    private boolean useTextures = true;

    // Constructors
    public Bullet(float damage, float x, float y, float velocityX, float velocityY,
                  float size, Color color, boolean isHealing) {
        this(damage, x, y, velocityX, velocityY, size, size, color, isHealing);
    }

    public Bullet(float damage, float x, float y, float velocityX, float velocityY,
                  float width, float height, Color color, boolean isHealing) {
        this(damage, x, y, velocityX, velocityY, width, height, color, isHealing, false, 0, 0);
    }

    public Bullet(float damage, float x, float y, float velocityX, float velocityY,
                  float size, Color color, boolean isHealing,
                  boolean isHoming, float homingDuration, float homingStrength) {
        this(damage, x, y, velocityX, velocityY, size, size, color, isHealing, isHoming, homingDuration, homingStrength);
    }

    public Bullet(float damage, float x, float y, float velocityX, float velocityY,
                  float width, float height, Color color, boolean isHealing,
                  boolean isHoming, float homingDuration, float homingStrength) {
        initializeBasicProperties(damage, x, y, velocityX, velocityY, width, height, color, isHealing);
        initializeHomingProperties(isHoming, homingDuration, homingStrength);
        initializeTrail(TRAIL_LENGTH);
    }

    // Initialization helpers
    private void initializeBasicProperties(float damage, float x, float y, float velocityX, float velocityY,
                                           float width, float height, Color color, boolean isHealing) {
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.size = width; // Keep size for backward compatibility
        this.width = width * 2; // Double the width to match original behavior
        this.height = height * 2; // Double the height to match original behavior
        this.color = color;
        this.isHeal = isHealing;
        this.baseR = color.r;
        this.baseG = color.g;
        this.baseB = color.b;

        // Calculate initial rotation based on velocity
        if (velocityX != 0 || velocityY != 0) {
            this.rotation = (float) Math.atan2(velocityY, velocityX) - MathUtils.PI/2; // Subtract PI/2 to make shapes point in direction of movement
        }

        // Initialize sprites for texture-based rendering
        if (useTextures) {
            // Get bullet texture from texture manager
            bulletSprite = new Sprite(BulletTextures.getInstance().getBulletTexture(shape));

            // Set size based on width and height
            bulletSprite.setSize(width, height);
            bulletSprite.setOriginCenter();

            // Properly center the sprite on the bullet position
            bulletSprite.setPosition(x - width/2, y - height/2);
            bulletSprite.setColor(color);

            // Initialize glow sprite if needed
            if (isGlowing) {
                glowSprite = new Sprite(BulletTextures.getInstance().getGlowTexture(shape));

                // Calculate glow size based on the multiplier
                float glowWidth = width * GLOW_SIZE_MULTIPLIER;
                float glowHeight = height * GLOW_SIZE_MULTIPLIER;
                glowSprite.setSize(glowWidth, glowHeight);
                glowSprite.setOriginCenter();

                // Properly center the glow sprite on the bullet position
                glowSprite.setPosition(x - glowWidth/2, y - glowHeight/2);
                glowSprite.setColor(glowColor != null ? glowColor : color);
            }
        }
    }

    private void initializeHomingProperties(boolean isHoming, float homingDuration, float homingStrength) {
        this.isHoming = isHoming;
        this.homingDuration = homingDuration;
        this.homingTimer = homingDuration;
        this.homingStrength = homingStrength;
    }

    private void initializeTrail(int length) {
        this.trailPositions = new float[length][2];
        for (int i = 0; i < length; i++) {
            trailPositions[i][0] = x;
            trailPositions[i][1] = y;
        }
    }

    // Main update method
    public void update(float delta, float playerX, float playerY) {
        // Existing update logic
        updateDiscoColors(delta);
        updateDelayedActions(delta);

        if (hasUpdateCallback && updateCallback != null) {
            boolean continueUpdating = updateCallback.update(delta);
            if (!continueUpdating) {
                hasUpdateCallback = false;
                updateCallback = null;
            }
        }

        // Handle rotation
        if (spinDirectionMatchesMovement) {
            float baseSpinSpeed = 360f; // Base rotation speed in degrees per second

            // Calculate overall movement speed and direction
            float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (speed > 0) {
                // Determine spin direction based on movement direction
                // For diagonal/any direction movement, use the dominant direction
                // or combine both directions for smooth transitions
                float directionFactor;
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    // Horizontal movement is dominant
                    directionFactor = -Math.signum(velocityX);
                } else if (Math.abs(velocityY) > Math.abs(velocityX)) {
                    // Vertical movement is dominant
                    directionFactor = Math.signum(velocityY);
                } else {
                    // Diagonal movement - combine both directions
                    directionFactor = (-Math.signum(velocityX) + Math.signum(velocityY)) * 0.5f;
                }

                // Calculate normalized speed factor (0.1-1.0 range for more control)
                float normalizedSpeed = Math.min(speed / 500f, 1.0f);

                // Calculate desired rotation speed (in degrees per second)
                float desiredRotationSpeed = directionFactor * baseSpinSpeed * normalizedSpeed * spinSpeedMultiplier;

                // Apply max spin speed limit if enabled
                if (useMaxSpinSpeed) {
                    // Convert maxSpinSpeed from radians to degrees for consistent units
                    float maxSpeedDegrees = maxSpinSpeed * MathUtils.radiansToDegrees;

                    // Clamp the rotation speed to our maximum
                    if (Math.abs(desiredRotationSpeed) > maxSpeedDegrees) {
                        desiredRotationSpeed = Math.signum(desiredRotationSpeed) * maxSpeedDegrees;
                    }
                }

                // Apply the final rotation speed
                rotationSpeed = desiredRotationSpeed;
                rotation += rotationSpeed * delta * MathUtils.degreesToRadians;
            }
        } else if (useCustomRotation) {
            // Existing custom rotation logic
            if (rotationAcceleration != 0 && rotationSpeed != targetRotationSpeed) {
                if (rotationSpeed < targetRotationSpeed) {
                    rotationSpeed = Math.min(rotationSpeed + rotationAcceleration * delta, targetRotationSpeed);
                } else {
                    rotationSpeed = Math.max(rotationSpeed - rotationAcceleration * delta, targetRotationSpeed);
                }
            }
            rotation += rotationSpeed * delta * MathUtils.degreesToRadians;
        } else if (autoRotate && (velocityX != 0 || velocityY != 0)) {
            // Calculate rotation based on velocity direction
            float targetRotation = (float) Math.atan2(velocityY, velocityX) - MathUtils.PI/2;

            // Smoothly interpolate to target rotation
            float rotationDiff = targetRotation - rotation;

            // Normalize rotation difference to [-PI, PI]
            while (rotationDiff > MathUtils.PI) rotationDiff -= 2 * MathUtils.PI;
            while (rotationDiff < -MathUtils.PI) rotationDiff += 2 * MathUtils.PI;

            // Apply rotation with speed based on velocity magnitude
            float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            float rotationSpeed = Math.min(speed / 100f, 1f) * 720f; // Max 720 degrees per second
            rotation += rotationDiff * Math.min(rotationSpeed * delta, 1f);
        }

        // Normalize rotation to keep it between 0 and 2π
        rotation = rotation % (2 * MathUtils.PI);

        // Handle fading logic
        if (isIsFading()) {
            updateFading(delta);
        }

        updatePosition(delta, playerX, playerY);
        updateTelegraph(delta);
        updateExplosion(delta);
        updateTrailSystem(delta);

        // Update sprite properties if using textures
        if (useTextures) {
            updateSpriteProperties(delta);
        }
    }

    private boolean isIsFading() {
        return isFading;
    }

    private void updateFading(float delta) {
        if (fadeTimer > 0) {
            fadeTimer -= delta;

            // Calculate alpha based on remaining fade time
            alpha = fadeTimer / fadeDuration;

            // Optionally slow down bullet during fade
            slowdownFactor = alpha;

            // Destroy bullet when fade is complete
            if (fadeTimer <= 0) {
                destroy();
            }
        }
    }

    // Update helper methods
    private void updatePosition(float delta, float playerX, float playerY) {
        lastY = y;
        prevX = x;
        prevY = y;

        if (shouldMove()) {
            updateHomingBehavior(delta, playerX, playerY);
            updateBulletPosition(delta);
            checkPlayerPassing(playerY);
        }
    }

    public boolean shouldMove() {
        return !isTelegraphing || telegraphTimer >= telegraphDuration;
    }

    private void updateHomingBehavior(float delta, float playerX, float playerY) {
        if (isHoming && homingTimer > 0) {
            applyHomingMovement(delta);
            homingTimer -= delta;
        }
    }

    private void applyHomingMovement(float delta) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            adjustVelocityForHoming(delta, dx / distance, dy / distance);
        }
    }

    private void adjustVelocityForHoming(float delta, float dirX, float dirY) {
        float homingFactor = homingStrength * delta;
        velocityX += dirX * homingFactor;
        velocityY += dirY * homingFactor;

        normalizeVelocity();
    }

    private void normalizeVelocity() {
        float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (speed > 0) {
            float baseSpeed = 300 * (explosionTimer > 0 ?
                MathUtils.lerp(1.0f, 0.2f, 1 - (explosionTimer / 1.5f)) : 1.0f);
            velocityX = (velocityX / speed) * baseSpeed;
            velocityY = (velocityY / speed) * baseSpeed;
        }
    }

    private void updateBulletPosition(float delta) {
        x += velocityX * delta;
        y += velocityY * delta;
        distanceTraveled += (float) Math.sqrt((x - prevX) * (x - prevX) + (y - prevY) * (y - prevY));
    }

    // Telegraph methods
    public void enableTelegraphing(float duration, float fadeTime) {
        this.isTelegraphing = true;
        this.telegraphDuration = duration;
        this.telegraphTimer = 0.0f;
        this.telegraphFadeTime = fadeTime;
        this.telegraphFadeTimer = 0.0f;
        this.telegraphLength = DEFAULT_TELEGRAPH_LENGTH;
        this.telegraphTotalLength = DEFAULT_TELEGRAPH_LENGTH;

        initializeTelegraphDirection();
    }

    private void initializeTelegraphDirection() {
        this.telegraphStartX = this.x;
        this.telegraphStartY = this.y;

        float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (speed > 0) {
            this.telegraphDirectionX = velocityX / speed;
            this.telegraphDirectionY = velocityY / speed;
        } else {
            this.telegraphDirectionX = 0;
            this.telegraphDirectionY = 1;
        }

        this.prevX = this.x;
        this.prevY = this.y;
        this.distanceTraveled = 0f;
    }

    private void updateTelegraph(float delta) {
        if (isTelegraphing) {
            telegraphTimer += delta;

            // Start fading as soon as the bullet starts moving
            if (telegraphTimer >= telegraphDuration) {
                // Initiate fade if it hasn't started yet
                if (telegraphFadeTimer <= 0) {
                    telegraphFadeTimer = telegraphFadeTime;
                }

                // Update fade timer to reduce alpha over time
                if (telegraphFadeTimer > 0) {
                    telegraphFadeTimer -= delta;
                }
            }

            // Stop telegraphing once fade is complete
            if (telegraphFadeTimer <= 0 && telegraphTimer >= telegraphDuration) {
                isTelegraphing = false;
            }
        }
    }

    // Add method to start fading
    public void startFading() {
        startFading(1.0f);  // Default 1-second fade
    }

    public void startFading(float duration) {
        this.isFading = true;
        this.fadeDuration = duration;
        this.fadeTimer = duration;
    }

    // Existing methods
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
    }

    public Color getColor() {
        if (hasExplosionTimer()) {
            // Flash between original color and white when about to explode
            // Make the flashing faster as the explosion approaches
            float flashSpeed = 100 + (1 - slowdownFactor) * 300; // Faster flashing as it slows down
            boolean isWhiteFrame = ((System.currentTimeMillis() / (long) flashSpeed) % 2 == 0);

            // Return white for the flash frames, original color otherwise
            Color result = isWhiteFrame ? Color.WHITE.cpy() : color.cpy();
            result.a = alpha; // Maintain the current alpha
            return result;
        }

        // Normal case - just return the color with current alpha
        Color c = color.cpy();
        c.a = alpha;
        return c;
    }
    public float getAlpha() {
        return alpha;
    }
    public float getDamage() {
        return isHeal ? -damage : damage;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFading() {
        return isFading;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Rectangle getHitbox() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }

    public void destroy() {
        if (onDestroyCallback != null) {
            onDestroyCallback.run();
        }
        this.active = false;
    }

    public void enableHoming(float duration, float strength) {
        this.isHoming = true;
        this.homingDuration = duration;
        this.homingTimer = duration;
        this.homingStrength = strength;

        // Store initial velocities
        this.originalSpeedX = this.velocityX;
        this.originalSpeedY = this.velocityY;
    }

    public void updateTarget(float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    private void updateDelayedActions(float delta) {
        if (delayedActions != null && actionDelays > 0) {
            actionDelayTimer += delta;
            if (actionDelayTimer >= actionDelays) {
                delayedActions.run();
                delayedActions = null;  // Set to null after running
                actionDelays = 0;      // Reset the delay
            }
        }
    }

    // Fix your addActionAfterDelay method to reset the timer
    public void addActionAfterDelay(float delay, Runnable action) {
        this.delayedActions = action;
        this.actionDelays = delay;
        this.actionDelayTimer = 0f;  // Reset the timer when setting a new action
    }

    public void setOnPassPlayerCallback(Runnable callback) {
        this.onPassPlayerCallback = callback;
    }

    public void setOnExplodeCallback(Runnable callback) {
        this.onExplodeCallback = callback;
    }

    public void startExplosionTimer(float duration) {
        this.explosionTimer = duration;
        // Store original velocities when explosion timer starts
        this.originalSpeedX = this.velocityX;
        this.originalSpeedY = this.velocityY;

        // Immediately trigger explosion if duration is 0
        if (duration <= 0) {
            if (onExplodeCallback != null) {
                onExplodeCallback.run();
            }
            destroy();
        }
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public void setVelocity(float vx, float vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }

    public void setColor(Color color) {
        this.color = color;
        // Update base colors for non-animated components
        this.baseR = color.r;
        this.baseG = color.g;
        this.baseB = color.b;
    }

    public void setColor(float r, float g, float b) {
        setColor(new Color(r, g, b, this.color.a));
    }

    public void setSize(float size) {
        this.size = size;
    }

    public Runnable getOnExplodeCallback() {
        return onExplodeCallback;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    // Update the trail tracking system
    private void updateTrailSystem(float delta) {
        trailTimer += delta;
        if (trailTimer >= TRAIL_UPDATE_INTERVAL) {
            updateTrail();
            trailTimer = 0;
        }
    }

    // Modified updateTrail method to store direction and velocity information
    protected void updateTrail() {
        // Get actual length of trail array
        int trailLength = trailPositions.length;

        // Shift all trail positions
        for (int i = trailLength - 1; i > 0; i--) {
            trailPositions[i][0] = trailPositions[i - 1][0];
            trailPositions[i][1] = trailPositions[i - 1][1];
        }

        // Set current position as first trail position
        trailPositions[0][0] = x;
        trailPositions[0][1] = y;
    }

    public float[][] getTrailPositions() {
        return trailPositions;
    }

    public void setTrailLength(int length) {
        float[][] newTrail = new float[length][2];
        float currentX = x;
        float currentY = y;
        for (int i = 0; i < length; i++) {
            newTrail[i][0] = currentX;
            newTrail[i][1] = currentY;
        }
        this.trailPositions = newTrail;
    }

    public boolean isDiscoR() {
        return discoR;
    }

    public void setDiscoR(boolean discoR) {
        this.discoR = discoR;
    }

    public boolean isDiscoG() {
        return discoG;
    }

    public void setDiscoG(boolean discoG) {
        this.discoG = discoG;
    }

    public boolean isDiscoB() {
        return discoB;
    }

    public void setDiscoB(boolean discoB) {
        this.discoB = discoB;
    }

    public void setDisco(boolean r, boolean g, boolean b) {
        this.discoR = r;
        this.discoG = g;
        this.discoB = b;
    }

    public void setDisco(boolean animateR, boolean animateG, boolean animateB, float baseR, float baseG, float baseB) {
        this.discoR = animateR;
        this.discoG = animateG;
        this.discoB = animateB;

        // Update base color values for non-animated components
        this.baseR = baseR;
        this.baseG = baseG;
        this.baseB = baseB;

        // Initialize color with the base values
        Color newColor = new Color(
            animateR ? (MathUtils.sin(discoTimer * (2f * MathUtils.PI) / discoSpeed) + 1f) * 0.5f : baseR,
            animateG ? (MathUtils.sin((discoTimer + discoSpeed / 3f) * (2f * MathUtils.PI) / discoSpeed) + 1f) * 0.5f : baseG,
            animateB ? (MathUtils.sin((discoTimer + 2f * discoSpeed / 3f) * (2f * MathUtils.PI) / discoSpeed) + 1f) * 0.5f : baseB,
            color.a
        );
        this.color = newColor;
    }

    public void enableRainbow() {
        setDisco(true, true, true);
    }

    public void setDiscoSpeed(float speedInSeconds) {
        if (speedInSeconds > 0) {
            this.discoSpeed = speedInSeconds;
        }
    }

    public boolean isTelegraphing() {
        // Only return true if:
        // 1. Telegraphing is enabled AND
        // 2. Either we're still within the telegraph duration OR the fade timer is still active
        // 3. AND the alpha value is greater than 0
        return isTelegraphing &&
            (telegraphTimer < telegraphDuration || telegraphFadeTimer > 0) &&
            getTelegraphAlpha() > 0;
    }

    public float getTelegraphAlpha() {
        // Full opacity during the telegraph phase
        if (telegraphTimer < telegraphDuration) {
            return 1.0f;
        }

        // During the fade, gradually reduce the alpha over time
        return Math.max(0.0f, telegraphFadeTimer / telegraphFadeTime);
    }

    public float[] getTelegraphEndPoint() {
        return new float[]{
            telegraphStartX + telegraphDirectionX * telegraphLength,
            telegraphStartY + telegraphDirectionY * telegraphLength
        };
    }

    public float[] getTelegraphStartPoint() {
        return new float[]{x, y};
    }

    public float[] getRemainingTelegraphEndPoint() {
        float[] endpoint = new float[2];
        float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);

        if (speed > 0) {
            float dirX = velocityX / speed;
            float dirY = velocityY / speed;
            float remainingLength = Math.max(0, telegraphTotalLength - distanceTraveled);

            endpoint[0] = x + dirX * remainingLength;
            endpoint[1] = y + dirY * remainingLength;
        } else {
            endpoint[0] = x;
            endpoint[1] = y;
        }

        return endpoint;
    }

    public float getTelegraphFadeTimer() {
        return telegraphFadeTimer;
    }

    public float getTelegraphFadeTime() {
        return telegraphFadeTime;
    }

    public float getDistanceTraveled() {
        return distanceTraveled;
    }

    public float getTelegraphTotalLength() {
        return telegraphTotalLength;
    }

    public float getTelegraphTimer() {
        return telegraphTimer;
    }

    public float getTelegraphDuration() {
        return telegraphDuration;
    }

    public boolean hasExplosionTimer() {
        return explosionTimer > 0;
    }

    public List<Bullet> getExplosionBullets() {
        return spawnedBullets;
    }

    private void updateDiscoColors(float delta) {
        if (isRainbow) {
            // Update rainbow hue
            rainbowHue = (rainbowHue + delta * rainbowSpeed) % 1f;
            // Convert HSV to RGB and update color
            this.color = hsvToRGB(rainbowHue, rainbowSaturation, rainbowValue);
            return;
        }

        if (!discoR && !discoG && !discoB) {
            return; // No disco animation needed
        }

        discoTimer += delta;

        // Create a copy of the original color to modify
        Color updatedColor = new Color(baseR, baseG, baseB, color.a);

        // Update the color components that should be animated
        if (discoR) {
            float variation = (MathUtils.sin(discoTimer * (2f * MathUtils.PI) / discoSpeed)) * discoColorRange;
            updatedColor.r = MathUtils.clamp(baseR + variation, 0f, 1f);
        }
        if (discoG) {
            float variation = (MathUtils.sin((discoTimer + discoSpeed / 3f) * (2f * MathUtils.PI) / discoSpeed)) * discoColorRange;
            updatedColor.g = MathUtils.clamp(baseG + variation, 0f, 1f);
        }
        if (discoB) {
            float variation = (MathUtils.sin((discoTimer + 2f * discoSpeed / 3f) * (2f * MathUtils.PI) / discoSpeed)) * discoColorRange;
            updatedColor.b = MathUtils.clamp(baseB + variation, 0f, 1f);
        }

        // Apply the updated color
        this.color = updatedColor;
    }

    public void updateExplosion(float delta) {
        if (explosionTimer > 0) {
            explosionTimer -= delta;
            if (explosionTimer <= 0) {
                if (onExplodeCallback != null) {
                    onExplodeCallback.run();
                }
                destroy();
            }
        }
    }

    private void checkPlayerPassing(float playerY) {
        if (!hasPassedPlayer) {
            if ((lastY > playerY && y <= playerY) ||
                (lastY < playerY && y >= playerY)) {
                hasPassedPlayer = true;
                if (onPassPlayerCallback != null) {
                    onPassPlayerCallback.run();
                }
            }
        }
    }

    // Add this method
    public void setUpdateCallback(BulletUpdateCallback callback) {
        this.updateCallback = callback;
        this.hasUpdateCallback = (callback != null);
    }

    public void setGlowing(boolean glowing) {
        boolean wasGlowing = this.isGlowing;
        this.isGlowing = glowing;

        if (useTextures && glowing != wasGlowing) {
            if (glowing && glowSprite == null) {
                // Create glow sprite if it doesn't exist
                glowSprite = new Sprite(BulletTextures.getInstance().getGlowTexture(shape));
                glowSprite.setSize(width * 2 * GLOW_SIZE_MULTIPLIER, height * 2 * GLOW_SIZE_MULTIPLIER);
                glowSprite.setOriginCenter();
                glowSprite.setPosition(x - width * GLOW_SIZE_MULTIPLIER, y - height * GLOW_SIZE_MULTIPLIER);
                glowSprite.setColor(glowColor != null ? glowColor : color);
            } else if (!glowing) {
                // Remove reference to glow sprite
                glowSprite = null;
            }
        }
    }

    public void setGlowLayers(int layers) {
        this.glowLayers = layers;
    }

    public void setGlowIntensity(float intensity) {
        this.glowIntensity = MathUtils.clamp(intensity, 0f, 1f);
    }

    public boolean isGlowing() {
        return isGlowing;
    }

    public int getGlowLayers() {
        return glowLayers;
    }

    public float getGlowIntensity() {
        return glowIntensity;
    }

    // Add this new method
    public void setDiscoColorRange(float range) {
        this.discoColorRange = MathUtils.clamp(range, 0f, 1f);
    }

    // Add these methods
    public void setShape(Shape shape) {
        this.shape = shape;

        if (useTextures) {
            // Update textures for the new shape
            if (bulletSprite != null) {
                bulletSprite.setTexture(BulletTextures.getInstance().getBulletTexture(shape));
            }

            if (isGlowing && glowSprite != null) {
                glowSprite.setTexture(BulletTextures.getInstance().getGlowTexture(shape));
            }
        }
    }

    public void setNumPoints(int points) {
        this.numPoints = Math.max(3, points); // Minimum 3 points
    }

    public Shape getShape() {
        return shape;
    }

    // Helper method to draw a star shape
    private void drawStar(ShapeRenderer shapeRenderer, float x, float y, float size) {
        // Calculate all star points
        float[] vertices = new float[numPoints * 4]; // Each point needs 2 coordinates (x,y)

        float outerRadius = size;
        float innerRadius = size * 0.4f;

        // Build the vertices array for a proper filled polygon
        for (int i = 0; i < numPoints; i++) {
            // Outer point
            float outerAngle = (i * 2 * MathUtils.PI / numPoints - MathUtils.PI / 2);
            int outerIndex = i * 4;
            vertices[outerIndex] = x + outerRadius * MathUtils.cos(outerAngle);
            vertices[outerIndex + 1] = y + outerRadius * MathUtils.sin(outerAngle);

            // Inner point
            float innerAngle = ((i + 0.5f) * 2 * MathUtils.PI / numPoints - MathUtils.PI / 2);
            vertices[outerIndex + 2] = x + innerRadius * MathUtils.cos(innerAngle);
            vertices[outerIndex + 3] = y + innerRadius * MathUtils.sin(innerAngle);
        }

        // Draw triangles to form a filled star
        if (shapeRenderer.getCurrentType() == ShapeRenderer.ShapeType.Filled) {
            for (int i = 0; i < numPoints; i++) {
                int nextI = (i + 1) % numPoints;

                // Draw triangle from center to current outer point to next inner point
                shapeRenderer.triangle(
                    x, y,
                    vertices[i * 4], vertices[i * 4 + 1],
                    vertices[i * 4 + 2], vertices[i * 4 + 3]
                );

                // Draw triangle from center to current inner point to next outer point
                shapeRenderer.triangle(
                    x, y,
                    vertices[i * 4 + 2], vertices[i * 4 + 3],
                    vertices[nextI * 4], vertices[nextI * 4 + 1]
                );
            }
        } else {
            // For line drawing, connect the points in a star pattern
            for (int i = 0; i < numPoints; i++) {
                int nextI = (i + 1) % numPoints;

                // Draw from outer point to next inner point
                shapeRenderer.line(
                    vertices[i * 4], vertices[i * 4 + 1],
                    vertices[i * 4 + 2], vertices[i * 4 + 3]
                );

                // Draw from inner point to next outer point
                shapeRenderer.line(
                    vertices[i * 4 + 2], vertices[i * 4 + 3],
                    vertices[nextI * 4], vertices[nextI * 4 + 1]
                );
            }
        }
    }

    // Helper method to draw a triangle
    private void drawTriangle(ShapeRenderer shapeRenderer, float x, float y, float size) {
        float height = size * 2;
        float halfWidth = size;

        shapeRenderer.triangle(
            x, y + height / 2,           // top
            x - halfWidth, y - height / 2, // bottom left
            x + halfWidth, y - height / 2  // bottom right
        );
    }

    // Helper method to draw shapes
    private void drawHexagon(ShapeRenderer shapeRenderer, float x, float y, float size) {
        int sides = 6;
        float[] vertices = new float[sides * 2];

        for (int i = 0; i < sides; i++) {
            float angle = i * 2 * MathUtils.PI / sides - MathUtils.PI / 2;
            vertices[i * 2] = x + size * MathUtils.cos(angle);
            vertices[i * 2 + 1] = y + size * MathUtils.sin(angle);
        }

        // Draw filled hexagon
        if (shapeRenderer.getCurrentType() == ShapeRenderer.ShapeType.Filled) {
            for (int i = 0; i < sides - 2; i++) {
                shapeRenderer.triangle(
                    vertices[0], vertices[1],
                    vertices[(i + 1) * 2], vertices[(i + 1) * 2 + 1],
                    vertices[(i + 2) * 2], vertices[(i + 2) * 2 + 1]
                );
            }
        } else {
            // Draw hexagon outline
            for (int i = 0; i < sides; i++) {
                int nextI = (i + 1) % sides;
                shapeRenderer.line(
                    vertices[i * 2], vertices[i * 2 + 1],
                    vertices[nextI * 2], vertices[nextI * 2 + 1]
                );
            }
        }
    }

    // Helper method to draw a diamond
    private void drawDiamond(ShapeRenderer shapeRenderer, float x, float y, float size) {
        shapeRenderer.triangle(
            x, y + size,      // top
            x - size, y,      // left
            x, y - size       // bottom
        );
        shapeRenderer.triangle(
            x, y + size,      // top
            x + size, y,      // right
            x, y - size       // bottom
        );
    }

    // Helper method to draw a heart
    private void drawHeart(ShapeRenderer shapeRenderer, float x, float y, float size) {
        float width = size * 2;
        float height = size * 2;

        if (shapeRenderer.getCurrentType() == ShapeRenderer.ShapeType.Filled) {
            // Top left circle
            shapeRenderer.circle(x - width/4, y + height/4, width/4);

            // Top right circle
            shapeRenderer.circle(x + width/4, y + height/4, width/4);

            // Bottom triangle
            shapeRenderer.triangle(
                x - width/2, y + height/4,
                x + width/2, y + height/4,
                x, y - height/2
            );
        } else {
            // Drawing a heart with lines is complex, we'll use a simplified approach
            // Draw an approximation with bezier curves or connected lines
            float[] vertices = new float[10];

            // Top left curve points
            vertices[0] = x - width/4;
            vertices[1] = y + height/2;

            vertices[2] = x - width/2;
            vertices[3] = y + height/4;

            // Bottom point
            vertices[4] = x;
            vertices[5] = y - height/2;

            // Top right curve points
            vertices[6] = x + width/2;
            vertices[7] = y + height/4;

            vertices[8] = x + width/4;
            vertices[9] = y + height/2;

            // Connect the points
            for (int i = 0; i < vertices.length - 2; i += 2) {
                shapeRenderer.line(
                    vertices[i], vertices[i+1],
                    vertices[i+2], vertices[i+3]
                );
            }

            // Connect back to the first point
            shapeRenderer.line(
                vertices[vertices.length-2], vertices[vertices.length-1],
                vertices[0], vertices[1]
            );
        }
    }

    // Helper method to draw a crescent
    private void drawCrescent(ShapeRenderer shapeRenderer, float x, float y, float size) {
        if (shapeRenderer.getCurrentType() == ShapeRenderer.ShapeType.Filled) {
            // Draw a filled crescent by subtracting one circle from another
            Color originalColor = new Color(shapeRenderer.getColor());

            // Draw the main circle
            shapeRenderer.circle(x, y, size);

            // Set color to transparent/background and draw the offset circle to create crescent
            shapeRenderer.setColor(0, 0, 0, 0);
            shapeRenderer.circle(x + size * 0.5f, y, size * 0.8f);

            // Restore original color
            shapeRenderer.setColor(originalColor);
        } else {
            // Draw crescent outline using points along two arcs
            int points = 12;
            float outerRadius = size;
            float innerRadius = size * 0.8f;
            float offset = size * 0.5f;

            // Draw outer arc
            for (int i = 0; i < points; i++) {
                float angle1 = MathUtils.PI * 0.75f + i * MathUtils.PI * 1.5f / (points - 1);
                float angle2 = MathUtils.PI * 0.75f + (i + 1) * MathUtils.PI * 1.5f / (points - 1);

                shapeRenderer.line(
                    x + outerRadius * MathUtils.cos(angle1),
                    y + outerRadius * MathUtils.sin(angle1),
                    x + outerRadius * MathUtils.cos(angle2),
                    y + outerRadius * MathUtils.sin(angle2)
                );
            }

            // Draw inner arc
            for (int i = 0; i < points; i++) {
                float angle1 = MathUtils.PI * 0.75f + i * MathUtils.PI * 1.5f / (points - 1);
                float angle2 = MathUtils.PI * 0.75f + (i + 1) * MathUtils.PI * 1.5f / (points - 1);

                shapeRenderer.line(
                    x + offset + innerRadius * MathUtils.cos(angle1 + MathUtils.PI),
                    y + innerRadius * MathUtils.sin(angle1 + MathUtils.PI),
                    x + offset + innerRadius * MathUtils.cos(angle2 + MathUtils.PI),
                    y + innerRadius * MathUtils.sin(angle2 + MathUtils.PI)
                );
            }
        }
    }

    // Update your drawShape method to include the new shapes
    public void drawShape(ShapeRenderer shapeRenderer) {
        // Create a new transformation matrix
        Matrix4 transform = new Matrix4();

        // Translate to bullet position
        transform.translate(x, y, 0);

        // Apply rotation (subtract PI/2 to make shapes point in direction of movement)
        transform.rotate(0, 0, 1, (rotation - MathUtils.PI/2) * MathUtils.radiansToDegrees);

        // Apply the transformation
        shapeRenderer.setTransformMatrix(transform);

        // Draw the shape centered at origin (0,0)
        switch (shape) {
            case STAR:
                drawStar(shapeRenderer, 0, 0, size);
                break;
            case TRIANGLE:
                drawTriangle(shapeRenderer, 0, 0, size);
                break;
            case SQUARE:
                shapeRenderer.rect(-size / 2, -size / 2, size, size);
                break;
            case HEXAGON:
                drawHexagon(shapeRenderer, 0, 0, size);
                break;
            case DIAMOND:
                drawDiamond(shapeRenderer, 0, 0, size);
                break;
            case HEART:
                drawHeart(shapeRenderer, 0, 0, size);
                break;
            case CRESCENT:
                drawCrescent(shapeRenderer, 0, 0, size);
                break;
            case CIRCLE:
            default:
                shapeRenderer.circle(0, 0, size);
                break;
        }
    }

    public void drawShapeAtOrigin(ShapeRenderer shapeRenderer) {
        // Draw the shape centered at origin (0,0)
        switch (shape) {
            case STAR:
                drawStar(shapeRenderer, 0, 0, size);
                break;
            case TRIANGLE:
                drawTriangle(shapeRenderer, 0, 0, size);
                break;
            case SQUARE:
                shapeRenderer.rect(-size / 2, -size / 2, size, size);
                break;
            case HEXAGON:
                drawHexagon(shapeRenderer, 0, 0, size);
                break;
            case DIAMOND:
                drawDiamond(shapeRenderer, 0, 0, size);
                break;
            case HEART:
                drawHeart(shapeRenderer, 0, 0, size);
                break;
            case CRESCENT:
                drawCrescent(shapeRenderer, 0, 0, size);
                break;
            case CIRCLE:
            default:
                shapeRenderer.circle(0, 0, size);
                break;
        }
    }

    // Add these methods after other getters/setters
    public void setRotation(float radians) {
        this.rotation = radians;
    }

    public float getRotation() {
        return rotation;
    }

    public void setAutoRotate(boolean autoRotate) {
        this.autoRotate = autoRotate;
    }

    public void setRotationSpeed(float degreesPerSecond) {
        this.rotationSpeed = degreesPerSecond;
        this.targetRotationSpeed = degreesPerSecond;
        this.useCustomRotation = true;
        this.autoRotate = false;
    }

    public void setRotationWithAcceleration(float targetDegreesPerSecond, float acceleration) {
        this.targetRotationSpeed = targetDegreesPerSecond;
        this.rotationAcceleration = acceleration;
        this.useCustomRotation = true;
        this.autoRotate = false;
    }

    public void smoothRotationTransition(float newTargetSpeed, float acceleration) {
        this.targetRotationSpeed = newTargetSpeed;
        this.rotationAcceleration = acceleration;
    }

    // Add these convenience methods for common rotation patterns
    public void startSpinning(float speed) {
        setRotationSpeed(speed);
    }

    public void startAcceleratingSpinning(float targetSpeed, float acceleration) {
        setRotationWithAcceleration(targetSpeed, acceleration);
    }

    public void startOscillatingSpinning(float maxSpeed, float acceleration) {
        setRotationWithAcceleration(maxSpeed, acceleration);

        // Add oscillation behavior through the update callback
        setUpdateCallback(delta -> {
            if (Math.abs(rotationSpeed) >= Math.abs(maxSpeed)) {
                smoothRotationTransition(-maxSpeed, acceleration);
            } else if (rotationSpeed <= -Math.abs(maxSpeed)) {
                smoothRotationTransition(maxSpeed, acceleration);
            }
            return true; // Continue updating
        });
    }

    // Add this method to enable/disable the feature
    public void setSpinDirectionMatchesMovement(boolean enabled) {
        this.spinDirectionMatchesMovement = enabled;
        this.useCustomRotation = enabled;
        this.autoRotate = false;
    }

    // Add this convenience method
    public void startDirectionalSpinning() {
        setSpinDirectionMatchesMovement(true);
    }

    public void setSpinSpeedMultiplier(float multiplier) {
        this.spinSpeedMultiplier = multiplier;
    }

    // Add to constructor or create a separate method to set glow properties
    public void setGlowProperties(Color glowColor, boolean rainbowGlow) {
        this.glowColor = glowColor;
        this.rainbowGlow = rainbowGlow;
    }

    public Color getGlowColor() {
        return glowColor != null ? glowColor : getColor();
    }

    public boolean hasRainbowGlow() {
        return rainbowGlow;
    }

    // New method to update sprite properties
    private void updateSpriteProperties(float delta) {
        if (bulletSprite != null) {
            // Update position - ensure proper centering
            bulletSprite.setPosition(x - width/2, y - height/2);
            bulletSprite.setSize(width, height);
            bulletSprite.setOriginCenter();

            // Update color
            bulletSprite.setColor(color.r, color.g, color.b, alpha);

            // Update rotation
            bulletSprite.setRotation(rotation * MathUtils.radiansToDegrees);

            // Update glow sprite if it exists
            if (isGlowing && glowSprite != null) {
                float glowWidth = width * GLOW_SIZE_MULTIPLIER;
                float glowHeight = height * GLOW_SIZE_MULTIPLIER;

                // Ensure glow is perfectly centered on bullet
                glowSprite.setPosition(x - glowWidth/2, y - glowHeight/2);
                glowSprite.setSize(glowWidth, glowHeight);
                glowSprite.setOriginCenter();
                glowSprite.setRotation(rotation * MathUtils.radiansToDegrees);

                if (rainbowGlow) {
                    float hue = (getHue(glowColor != null ? glowColor : color) +
                        (System.currentTimeMillis() % 2000) / 2000f) % 1.0f;
                    Color rainbowColor = hsvToRGB(hue, 1.0f, 1.0f);
                    rainbowColor.a = alpha * GLOW_ALPHA_DECAY * glowIntensity;
                    glowSprite.setColor(rainbowColor);
                } else {
                    Color spriteGlowColor = glowColor != null ? glowColor : color;
                    glowSprite.setColor(
                        spriteGlowColor.r,
                        spriteGlowColor.g,
                        spriteGlowColor.b,
                        alpha * GLOW_ALPHA_DECAY * glowIntensity
                    );
                }
            }
        }
    }

    // Add this method to get the hue component of a color
    private float getHue(Color color) {
        float r = color.r;
        float g = color.g;
        float b = color.b;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));

        if (max == min) return 0;

        float hue;
        if (max == r) {
            hue = (g - b) / (max - min);
        } else if (max == g) {
            hue = 2f + (b - r) / (max - min);
        } else {
            hue = 4f + (r - g) / (max - min);
        }

        hue = hue / 6f;
        if (hue < 0) hue += 1;
        return hue;
    }

    // Add this method to convert HSV to RGB
    private Color hsvToRGB(float h, float s, float v) {
        float r, g, b;
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            default: r = v; g = p; b = q; break;
        }
        return new Color(r, g, b, color.a);
    }

    // Add this method to enable rainbow effect
    public void enableRainbow(float speed, float saturation, float value) {
        isRainbow = true;
        rainbowSpeed = speed;
        rainbowSaturation = saturation;
        rainbowValue = value;
        rainbowHue = 0f;
    }

    // Add method to render with SpriteBatch
    public void drawWithSpriteBatch(SpriteBatch batch) {
        if (!useTextures) return;

        Color bulletDisplayColor = getColor(); // Use getColor() which handles explosion blinking

        // Draw glow first (underneath)
        if (isGlowing && glowSprite != null) {
            // Get the appropriate glow color
            Color spriteGlowColor;

            // For rainbow glow, update the color on each frame
            if (rainbowGlow) {
                float hue = (getHue(glowColor != null ? glowColor : color) +
                    (System.currentTimeMillis() % 2000) / 2000f) % 1.0f;
                spriteGlowColor = hsvToRGB(hue, 0.8f, 1.0f);
            } else if (hasExplosionTimer()) {
                // Make explosion glow flash with the bullet
                spriteGlowColor = bulletDisplayColor.cpy();
            } else {
                spriteGlowColor = glowColor != null ? glowColor : color.cpy();
            }

            // Base alpha for glow effect - used for both bullet and trail
            float baseGlowAlpha = Math.min(0.7f, alpha * 0.6f * glowIntensity);
            spriteGlowColor.a = baseGlowAlpha;

            // Calculate glow size - larger but more transparent for diffused effect
            float glowSizeMultiplier = 2.0f;
            float currentGlowWidth = width * glowSizeMultiplier;
            float currentGlowHeight = height * glowSizeMultiplier;

            // Draw multiple layers of increasingly transparent glow
            for (int i = 0; i < 4; i++) {
                // Each layer gets progressively larger but more transparent
                float layerWidth = currentGlowWidth * (1.0f + (i * 0.25f));
                float layerHeight = currentGlowHeight * (1.0f + (i * 0.25f));
                // Much lower alpha values for a subtle glow
                float layerAlpha = spriteGlowColor.a * (0.7f - (i * 0.15f));

                // Create temporary sprite for this layer
                Sprite layerSprite = new Sprite(glowSprite);
                layerSprite.setColor(spriteGlowColor.r, spriteGlowColor.g, spriteGlowColor.b, layerAlpha);

                // Perfectly center the glow layer
                float glowX = x - layerWidth/2;
                float glowY = y - layerHeight/2;

                layerSprite.setSize(layerWidth, layerHeight);
                layerSprite.setPosition(glowX, glowY);
                layerSprite.setOriginCenter();
                layerSprite.setRotation(rotation * MathUtils.radiansToDegrees);

                // Draw this glow layer
                layerSprite.draw(batch);
            }
        }

        // Then draw the bullet
        if (bulletSprite != null) {
            // Set the bullet color (handling explosion flashing)
            bulletSprite.setColor(
                bulletDisplayColor.r,
                bulletDisplayColor.g,
                bulletDisplayColor.b,
                alpha
            );

            // Set proper size and centered position
            bulletSprite.setSize(width, height);
            bulletSprite.setPosition(x - width/2, y - height/2);
            bulletSprite.setOriginCenter();
            bulletSprite.setRotation(rotation * MathUtils.radiansToDegrees);

            // Draw the bullet
            bulletSprite.draw(batch);
        }
    }

    // Add method to render trail with SpriteBatch
    public void drawTrailWithSpriteBatch(SpriteBatch batch) {
        if (!useTextures) return;

        float[][] trail = getTrailPositions();
        if (trail == null) return;

        // Create a temporary sprite for trail rendering
        Sprite trailSprite = new Sprite(bulletSprite);
        Sprite glowTrailSprite = null;

        // If glowing, also create a glow trail sprite
        if (isGlowing) {
            glowTrailSprite = new Sprite(BulletTextures.getInstance().getGlowTexture(shape));
        }

        // Store the bullet's rotation to keep consistent rotation along trail
        float bulletRotationDegrees = rotation * MathUtils.radiansToDegrees;

        // Calculate movement direction once
        float movementRotation = bulletRotationDegrees;
        if (velocityX != 0 || velocityY != 0) {
            movementRotation = MathUtils.atan2(velocityY, velocityX) * MathUtils.radiansToDegrees - 90;
        }

        for (int i = 0; i < trail.length; i++) {
            float progress = (float)i / trail.length;
            // Base alpha for trail - uses same formula as standard TRAIL_ALPHA
            float trailAlpha = (1.0f - progress) * TRAIL_ALPHA * alpha;

            // Calculate trail segment size (shrink as it gets further back)
            float trailWidth = width * (1.0f - (progress * 0.5f));
            float trailHeight = height * (1.0f - (progress * 0.5f));

            // Precisely position the trail segment
            float segmentX = trail[i][0] - trailWidth/2;
            float segmentY = trail[i][1] - trailHeight/2;

            trailSprite.setSize(trailWidth, trailHeight);
            trailSprite.setPosition(segmentX, segmentY);
            trailSprite.setOriginCenter();

            // Determine which rotation to use
            float trailRotation;

            // When auto-rotating or not specifically rotating, follow movement direction
            if (autoRotate || (!useCustomRotation && !spinDirectionMatchesMovement)) {
                trailRotation = movementRotation;
            } else {
                // For custom rotation, use bullet's rotation for consistency
                trailRotation = bulletRotationDegrees;
            }

            // Set the rotation
            trailSprite.setRotation(trailRotation);

            // Get the trail segment's color - this is independent of glow color
            Color trailColor = getTrailColor(progress);

            // Handle explosion blinking for trail
            if (hasExplosionTimer()) {
                Color flashingColor = getColor();
                trailColor.r = flashingColor.r;
                trailColor.g = flashingColor.g;
                trailColor.b = flashingColor.b;
            }

            // If glowing, render the glow effect first
            if (isGlowing && glowTrailSprite != null) {
                // Handle glow trail color differently based on bullet type
                Color glowTrailColor;

                if (hasExplosionTimer()) {
                    // Make trail glow flash with the bullet
                    glowTrailColor = getColor().cpy();
                } else if (rainbowGlow) {
                    // Rainbow effect - shift hue over time and along trail
                    float baseHue = getHue(glowColor != null ? glowColor : color);
                    float timeOffset = (System.currentTimeMillis() % 2000) / 2000f;
                    float trailOffset = progress * 0.2f;
                    float hue = (baseHue + timeOffset + trailOffset) % 1.0f;
                    glowTrailColor = hsvToRGB(hue, 0.8f, 1.0f);
                } else if (color.r > 0.9f && color.g > 0.9f && color.b > 0.9f) {
                    // Special case: Blue comet effect for white bullets
                    // Use the trail color for the glow too
                    glowTrailColor = trailColor.cpy();
                } else if (glowColor != null) {
                    // Use the specified glow color if one was set
                    glowTrailColor = glowColor.cpy();
                } else {
                    // Fallback to bullet color if no glow color specified and not a special effect
                    glowTrailColor = color.cpy();
                }

                // Use same glow size multiplier as bullet for consistency
                float glowSizeMultiplier = 2.0f + (1.0f - progress) * 0.3f; // Slightly larger at beginning of trail
                float glowTrailWidth = trailWidth * glowSizeMultiplier;
                float glowTrailHeight = trailHeight * glowSizeMultiplier;

                // Use the same base glow alpha as the bullet for consistency
                float baseTrailGlowAlpha = trailAlpha * 0.6f * glowIntensity;

                // Draw multiple diffused layers
                for (int j = 0; j < 3; j++) {
                    float layerWidth = glowTrailWidth * (1.0f + (j * 0.25f));
                    float layerHeight = glowTrailHeight * (1.0f + (j * 0.25f));
                    float layerAlpha = baseTrailGlowAlpha * (0.7f - (j * 0.15f));

                    // Create temporary sprite for this layer
                    Sprite layerSprite = new Sprite(glowTrailSprite);

                    // Set the color with proper alpha
                    layerSprite.setColor(
                        glowTrailColor.r,
                        glowTrailColor.g,
                        glowTrailColor.b,
                        layerAlpha
                    );

                    // Ensure glow is perfectly centered on trail point
                    float glowX = trail[i][0] - layerWidth/2;
                    float glowY = trail[i][1] - layerHeight/2;

                    layerSprite.setSize(layerWidth, layerHeight);
                    layerSprite.setPosition(glowX, glowY);
                    layerSprite.setOriginCenter();
                    layerSprite.setRotation(trailRotation);

                    // Draw this glow layer
                    layerSprite.draw(batch);
                }
            }

            // Apply trail alpha
            trailColor.a = trailAlpha;
            trailSprite.setColor(trailColor);

            // Draw the trail segment
            trailSprite.draw(batch);
        }
    }

    /**
     * Draws an additional glow pass for enhanced bloom effect
     * This is called only for glowing bullets in the second render pass
     */
    public void drawAdditionalGlowPass(SpriteBatch batch) {
        if (!useTextures || !isGlowing) return;

        // Create an extra sprite for the bloom effect
        Sprite extraGlowSprite = new Sprite(BulletTextures.getInstance().getGlowTexture(shape));

        // Get the appropriate bloom color
        Color bloomColor;
        if (rainbowGlow) {
            // For rainbow glow, use a slowly shifting hue
            float hue = (getHue(glowColor != null ? glowColor : color) +
                (System.currentTimeMillis() % 3000) / 3000f) % 1.0f;
            bloomColor = hsvToRGB(hue, 0.6f, 0.9f);
        } else if (hasExplosionTimer()) {
            // Make explosion bloom flash with the bullet
            bloomColor = getColor().cpy();
        } else {
            bloomColor = glowColor != null ? glowColor.cpy() : color.cpy();
        }

        // Calculate a size larger than the main glow
        float bloomSize = size * 2 * 3.0f;  // 3.0x larger for wide bloom effect

        // Use consistent alpha with the same multiplier as regular glow
        bloomColor.a = 0.15f * alpha * glowIntensity;

        // Create a few bloom layers with very large size differences for a more diffused effect
        for (int i = 0; i < 3; i++) {
            float layerSize = bloomSize * (1.0f + (i * 0.4f));
            float layerAlpha = bloomColor.a * (0.8f - (i * 0.25f));

            // Set position (perfectly centered on bullet)
            float bloomX = x - layerSize/2;
            float bloomY = y - layerSize/2;

            // Create temporary sprite for this layer
            Sprite layerSprite = new Sprite(extraGlowSprite);
            layerSprite.setColor(bloomColor.r, bloomColor.g, bloomColor.b, layerAlpha);
            layerSprite.setSize(layerSize, layerSize);
            layerSprite.setPosition(bloomX, bloomY);
            layerSprite.setOriginCenter();
            layerSprite.setRotation(rotation * MathUtils.radiansToDegrees);

            // Draw this bloom layer
            layerSprite.draw(batch);
        }
    }

    // Methods to enable/disable texture rendering
    public void setUseTextures(boolean useTextures) {
        this.useTextures = useTextures;
        if (useTextures && bulletSprite == null) {
            // Initialize sprites if they don't exist
            bulletSprite = new Sprite(BulletTextures.getInstance().getBulletTexture(shape));

            // Set size based on width and height
            bulletSprite.setSize(width, height);
            bulletSprite.setOriginCenter();

            // Properly center the sprite on the bullet position
            bulletSprite.setPosition(x - width/2, y - height/2);
            bulletSprite.setColor(color);

            if (isGlowing) {
                glowSprite = new Sprite(BulletTextures.getInstance().getGlowTexture(shape));

                // Calculate glow size based on the multiplier
                float glowWidth = width * GLOW_SIZE_MULTIPLIER;
                float glowHeight = height * GLOW_SIZE_MULTIPLIER;
                glowSprite.setSize(glowWidth, glowHeight);
                glowSprite.setOriginCenter();

                // Properly center the glow sprite on the bullet position
                glowSprite.setPosition(x - glowWidth/2, y - glowHeight/2);
                glowSprite.setColor(glowColor != null ? glowColor : color);
            }
        }
    }

    public boolean isUsingTextures() {
        return useTextures;
    }

    // Helper method to calculate trail color based on progress
    private Color getTrailColor(float progress) {
        if (isRainbow) {
            // For rainbow bullets, use the same rainbow effect but with slight hue shift
            float shiftedHue = (rainbowHue + 0.1f * progress) % 1.0f;
            return hsvToRGB(shiftedHue, rainbowSaturation, rainbowValue);
        } else if (color.r > 0.9f && color.g > 0.9f && color.b > 0.9f) {
            // For white bullets - blue comet effect
            if (progress < 0.2f) {
                return new Color(
                    MathUtils.lerp(1f, 0.6f, progress / 0.2f),
                    MathUtils.lerp(1f, 0.8f, progress / 0.2f),
                    1f,
                    1.0f
                );
            } else if (progress < 0.4f) {
                float p = (progress - 0.2f) / 0.2f;
                return new Color(
                    MathUtils.lerp(0.6f, 0.3f, p),
                    MathUtils.lerp(0.8f, 0.5f, p),
                    1f,
                    1.0f
                );
            } else if (progress < 0.7f) {
                float p = (progress - 0.4f) / 0.3f;
                return new Color(
                    MathUtils.lerp(0.3f, 0.4f, p),
                    MathUtils.lerp(0.5f, 0.2f, p),
                    MathUtils.lerp(1f, 0.8f, p),
                    1.0f
                );
            } else {
                float p = (progress - 0.7f) / 0.3f;
                return new Color(
                    MathUtils.lerp(0.4f, 0.5f, p),
                    MathUtils.lerp(0.2f, 0.1f, p),
                    MathUtils.lerp(0.8f, 0.9f, p),
                    1.0f
                );
            }
        } else if (damage < 0) {
            // Healing bullets - green trail effect
            return new Color(
                MathUtils.lerp(color.r, 0, progress),
                MathUtils.lerp(color.g, 1, progress),
                MathUtils.lerp(color.b, 0, progress),
                1.0f
            );
        } else if (discoR || discoG || discoB) {
            // For disco bullets, maintain the disco effect in trail with slight hue shift
            float hue = getHue(color);
            float shiftedHue = (hue + 0.1f * progress) % 1.0f;
            float saturation = Math.min(0.8f, color.r != 0 || color.g != 0 || color.b != 0 ?
                               Math.max(Math.max(color.r, color.g), color.b) : 0.5f);
            float value = Math.min(1.0f, Math.max(Math.max(color.r, color.g), color.b) + 0.1f);
            return hsvToRGB(shiftedHue, saturation, value);
        } else {
            // For normal colored bullets - maintain color with slight hue shift
            float hue = getHue(color);
            float saturation = Math.max(0.7f, Math.min(1.0f,
                              (color.r != 0 || color.g != 0 || color.b != 0) ?
                              Math.max(Math.max(color.r, color.g), color.b) : 0.8f));

            // Keep value close to original color's brightness
            float originalValue = Math.max(Math.max(color.r, color.g), color.b);
            float value = Math.min(1.0f, originalValue + 0.05f * (1 - progress));

            // Apply subtle hue shift based on progress
            float shiftedHue = (hue + 0.05f * progress) % 1.0f;
            return hsvToRGB(shiftedHue, saturation, value);
        }
    }

    /**
     * Sets a maximum limit on spin speed when spinDirectionMatchesMovement is enabled.
     * This prevents bullets from spinning too fast when moving at high velocities.
     *
     * @param maxSpeed Maximum rotation speed in radians per second
     */
    public void setMaxSpinSpeed(float maxSpeed) {
        this.maxSpinSpeed = Math.abs(maxSpeed); // Ensure positive value
        this.useMaxSpinSpeed = true;
    }

    /**
     * Disables the maximum spin speed limit, allowing unlimited spin speed.
     */
    public void disableMaxSpinSpeed() {
        this.useMaxSpinSpeed = false;
    }
}
