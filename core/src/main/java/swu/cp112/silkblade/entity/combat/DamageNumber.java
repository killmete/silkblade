package swu.cp112.silkblade.entity.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Represents a bouncing damage number that appears when damage is dealt
 */
public class DamageNumber {
    // Position and movement
    private float x, y;
    private float velocityY;
    private float gravity;
    
    // Appearance
    private String text;
    private Color color;
    private Color strokeColor;
    private float scale;
    private float alpha;
    private boolean isEnemyDamage; // True if this damage number is for enemy damage (player attack)
    private boolean isCritical;
    
    // Animation
    private float pulseTimer = 0f;
    private static final float PULSE_SPEED = 8.0f;
    private static final float PULSE_AMPLITUDE = 0.2f;
    
    // Lifecycle
    private float lifetime;
    private float maxLifetime;
    private boolean isAlive;
    
    // Constants
    private static final float INITIAL_VELOCITY_Y = 100.0f;
    private static final float GRAVITY = -200.0f;
    private static final float DEFAULT_LIFETIME = 1.5f;
    private static final float FADE_START = 0.7f; // When to start fading (percentage of lifetime)
    private static final float ENEMY_DAMAGE_SCALE = 1.2f; // Make enemy damage numbers larger
    private static final float STROKE_THICKNESS = 2.0f; // Thickness of text stroke
    
    /**
     * Creates a damage number
     * 
     * @param value The damage value to display
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param isHealing True if this is healing (green), false if damage (red)
     * @param isCritical True if this is a critical hit (larger text)
     */
    public DamageNumber(int value, float x, float y, boolean isHealing, boolean isCritical) {
        this.text = String.valueOf(Math.abs(value));
        this.x = x;
        this.y = y;
        this.velocityY = INITIAL_VELOCITY_Y;
        this.gravity = GRAVITY;
        this.lifetime = 0;
        this.maxLifetime = DEFAULT_LIFETIME;
        this.isAlive = true;
        this.alpha = 1.0f;
        this.isCritical = isCritical;
        this.isEnemyDamage = false; // Default is player damage
        
        // Color based on damage type
        if (isHealing) {
            this.color = new Color(0.2f, 0.9f, 0.2f, 1.0f); // Green for healing
            this.strokeColor = new Color(0.0f, 0.4f, 0.0f, 1.0f); // Dark green stroke
        } else if (isCritical) {
            this.color = new Color(1.0f, 0.8f, 0.0f, 1.0f); // Gold for critical
            this.strokeColor = new Color(0.7f, 0.4f, 0.0f, 1.0f); // Dark orange stroke
        } else {
            this.color = new Color(1.0f, 0.3f, 0.3f, 1.0f); // Red for damage
            this.strokeColor = new Color(0.5f, 0.0f, 0.0f, 1.0f); // Dark red stroke
        }
        
        // Scale based on damage type
        this.scale = isCritical ? 1.5f : 1.0f;
        
        // Add random horizontal offset to prevent overlap
        this.x += MathUtils.random(-20, 20);
    }
    
    /**
     * Creates a thorn damage number (purple color)
     */
    public static DamageNumber createThornDamage(int value, float x, float y) {
        DamageNumber thornDamage = new DamageNumber(value, x, y, false, false);
        thornDamage.color = new Color(0.8f, 0.2f, 0.8f, 1.0f); // Purple for thorn damage
        thornDamage.strokeColor = new Color(0.4f, 0.0f, 0.4f, 1.0f); // Dark purple stroke
        return thornDamage;
    }
    
    /**
     * Creates a damage number specifically for enemy damage (when player attacks)
     * These are more prominent with larger scale and better visibility
     */
    public static DamageNumber createEnemyDamage(int value, float x, float y, boolean isCritical) {
        DamageNumber enemyDamage = new DamageNumber(value, x, y, false, isCritical);
        enemyDamage.isEnemyDamage = true;
        // Make enemy damage numbers bigger by default
        enemyDamage.scale *= ENEMY_DAMAGE_SCALE;
        
        // Modify colors for better visibility when attacking enemies
        if (isCritical) {
            enemyDamage.color = new Color(1.0f, 0.9f, 0.1f, 1.0f); // Brighter gold
            enemyDamage.strokeColor = new Color(0.8f, 0.4f, 0.0f, 1.0f); // Orange-red stroke
        } else {
            enemyDamage.color = new Color(1.0f, 0.4f, 0.4f, 1.0f); // Brighter red
            enemyDamage.strokeColor = new Color(0.6f, 0.0f, 0.0f, 1.0f); // Darker red stroke
        }
        
        // Give enemy damage numbers higher initial velocity for more dramatic effect
        enemyDamage.velocityY = INITIAL_VELOCITY_Y * 1.5f;
        
        return enemyDamage;
    }
    
    /**
     * Updates the damage number's position and lifetime
     * 
     * @param delta Time elapsed since last update
     * @return True if the damage number is still alive, false if it should be removed
     */
    public boolean update(float delta) {
        if (!isAlive) return false;
        
        // Update lifetime
        lifetime += delta;
        if (lifetime >= maxLifetime) {
            isAlive = false;
            return false;
        }
        
        // Update position with bouncing physics
        velocityY += gravity * delta;
        y += velocityY * delta;
        
        // If number hits the "ground", bounce with reduced velocity
        if (y < 0 && velocityY < 0) {
            y = 0;
            velocityY = -velocityY * 0.5f;
            
            // Stop bouncing if velocity becomes too small
            if (Math.abs(velocityY) < 20) {
                velocityY = 0;
            }
        }
        
        // Update pulsing effect for critical hits
        if (isCritical) {
            pulseTimer += delta * PULSE_SPEED;
        }
        
        // Update alpha for fade out
        float lifetimePercentage = lifetime / maxLifetime;
        if (lifetimePercentage > FADE_START) {
            float fadeProgress = (lifetimePercentage - FADE_START) / (1.0f - FADE_START);
            alpha = 1.0f - fadeProgress;
        }
        
        return true;
    }
    
    /**
     * Draws the damage number with stroke effect for better visibility
     * 
     * @param batch The SpriteBatch to draw with
     * @param font The font to use for rendering
     */
    public void draw(SpriteBatch batch, BitmapFont font) {
        if (!isAlive) return;
        
        // Save original font properties
        Color originalColor = font.getColor();
        float originalScale = font.getData().scaleX;
        
        // Calculate current scale with pulsing effect for critical hits
        float currentScale = scale;
        if (isCritical) {
            // Add pulsing effect for critical hits
            currentScale = scale * (1.0f + MathUtils.sin(pulseTimer) * PULSE_AMPLITUDE);
        }
        
        // Set font scale
        font.getData().setScale(currentScale);
        
        // Calculate text width for centering
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x - layout.width / 2;
        
        // Draw stroke first (draw text multiple times with slight offsets)
        font.setColor(strokeColor.r, strokeColor.g, strokeColor.b, alpha);
        
        // Draw in 8 directions for a complete stroke
        float stroke = STROKE_THICKNESS * (isEnemyDamage ? 1.5f : 1.0f); 
        for (int i = 0; i < 8; i++) {
            float angle = i * MathUtils.PI / 4.0f;
            float offsetX = MathUtils.cos(angle) * stroke;
            float offsetY = MathUtils.sin(angle) * stroke;
            font.draw(batch, text, textX + offsetX, y + offsetY);
        }
        
        // Then draw the text on top
        font.setColor(color.r, color.g, color.b, alpha);
        font.draw(batch, text, textX, y);
        
        // Restore original font properties
        font.setColor(originalColor);
        font.getData().setScale(originalScale);
    }
    
    /**
     * Checks if the damage number is still active
     */
    public boolean isAlive() {
        return isAlive;
    }
    
    /**
     * Sets a custom text to display instead of the damage number
     * @param customText The text to display
     */
    public void setCustomText(String customText) {
        this.text = customText;
    }
    
    /**
     * Sets a custom lifetime for the damage number
     * @param lifetime The lifetime in seconds
     */
    public void setLifetime(float lifetime) {
        this.maxLifetime = lifetime;
    }
    
    /**
     * Sets the color of the damage number
     * @param color The color to set
     */
    public void setColor(Color color) {
        this.color = new Color(color);
        
        // Create an appropriate stroke color based on the main color
        float r = Math.max(0, color.r - 0.3f);
        float g = Math.max(0, color.g - 0.3f);
        float b = Math.max(0, color.b - 0.3f);
        this.strokeColor = new Color(r, g, b, color.a);
    }
} 