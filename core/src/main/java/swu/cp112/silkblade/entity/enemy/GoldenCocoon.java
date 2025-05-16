    package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.pattern.goldencocoon.PrecisionWebPattern;
import swu.cp112.silkblade.pattern.goldencocoon.BlossomingWebPattern;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Golden Cocoon - The boss enemy for stage 20
 * A more advanced form of the Silk Weaver family with deadlier and more precise attacks.
 * Uses 3 unique attack patterns that require dynamic movement to dodge.
 * Features a golden aura effect with pulsing intensity.
 */
public class GoldenCocoon extends AbstractEnemy {
    private Player player;
    private float pulseTimer = 0f;
    private float glowIntensity = 0.8f;
    private int currentPatternIndex = 0;

    // Constants
    private static final int BASE_HP = 150;
    private static final int BASE_ATTACK = 9;
    private static final int BASE_XP = 100;
    private static final int BASE_GOLD = 150;

    // Visual effects
    private static final float PULSE_SPEED = 1.5f;
    private static final float MIN_GLOW = 0.6f;
    private static final float MAX_GLOW = 1.0f;

    // Background to use for Golden Cocoon encounters
    public static final String COMBAT_BACKGROUND = "background/Phase_2.jpeg";

    // Music to use for Golden Cocoon encounters
    public static final String COMBAT_MUSIC = "music/mus_boss2.mp3";

    public GoldenCocoon() {
        super("Golden Cocoon", BASE_HP,
                new Texture(Gdx.files.internal("enemy/goldenCocoon.png")),
                300f, 300f);

        GameLogger.logInfo("Creating Golden Cocoon boss for stage 20");

        // Initialize components
        this.player = Player.loadFromFile();
        this.pulseTimer = 0f;

        // Add all patterns to the manager
        this.addAttackPattern(new PrecisionWebPattern()); // Pattern 1: Precise web shots that require movement
        this.addAttackPattern(new BlossomingWebPattern());// Pattern 2: Web that expands from center

        // Initialize remaining properties
        initializeCocoon();
    }

    private void initializeCocoon() {
        // Set base rewards
        this.setBaseRewards(BASE_XP, BASE_GOLD);
        this.attackDamage = BASE_ATTACK;
        // Set primary color (golden)
        this.primaryColor = new Color(1.0f, 0.85f, 0.1f, 1.0f);
        // Scale to player level
        scaleToPlayerLevel(player.getLevel());
        // Initialize dialogues
        initializeDialogues();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update pulse effect
        pulseTimer += delta * PULSE_SPEED;
        if (pulseTimer > MathUtils.PI2) {
            pulseTimer -= MathUtils.PI2;
        }

        // Calculate glow intensity based on sine wave
        glowIntensity = MIN_GLOW + (MAX_GLOW - MIN_GLOW) * (0.5f + 0.5f * MathUtils.sin(pulseTimer));
    }

    private void initializeDialogues() {
        // Set encounter dialogue
        this.encounterDialogue = "The Golden Cocoon shimmers with deadly beauty!";

        // Set attack dialogues
        this.attackDialogue = "The Golden Cocoon spins a web of destruction!";

        // Clear and add random turn dialogues
        this.clearRandomTurnDialogues();
        this.addRandomTurnDialogue("Golden threads pulse with arcane power.");
        this.addRandomTurnDialogue("The Cocoon's shell radiates with blinding light.");
        this.addRandomTurnDialogue("You can feel the precision of its movements.");
        this.addRandomTurnDialogue("The air around you bristles with fine silken threads.");
        this.addRandomTurnDialogue("The Cocoon's form shifts as it prepares another attack.");

        // Set defeat and victory dialogues
        this.defeatDialogue = "The Golden Cocoon cracks open, releasing a shower of golden dust...";
        this.victoryDialogue = "The Golden Cocoon has ensnared you in its deadly web!";
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // Save original color
        Color originalColor = batch.getColor().cpy();

        // Create a copy of our primary color
        Color tintWithAlpha = new Color(primaryColor);

        // Apply the alpha from AbstractEnemy class to maintain dimming effect
        tintWithAlpha.a = currentAlpha;

        // Draw the main sprite
        batch.setColor(tintWithAlpha);
        float drawX = x + (isShaking ? shakeX : 0);
        batch.draw(texture, drawX, y, width, height);

        // Draw golden glow effect
        drawGoldenGlow(batch, drawX, y);

        // Reset batch color
        batch.setColor(originalColor);
    }

    /**
     * Draws a golden glow effect around the cocoon
     */
    private void drawGoldenGlow(SpriteBatch batch, float x, float y) {
        // Create a golden glow color with pulsing alpha
        Color glowColor = new Color(1.0f, 0.9f, 0.2f, glowIntensity * 0.4f);
        batch.setColor(glowColor);

        // Draw outer glow (slightly larger sprite)
        float glowScale = 1.15f;
        float glowWidth = width * glowScale;
        float glowHeight = height * glowScale;
        float glowX = x - (glowWidth - width) / 2;
        float glowY = y - (glowHeight - height) / 2;

        batch.draw(texture, glowX, glowY, glowWidth, glowHeight);

        // Draw inner brighter glow (slightly smaller than outer glow)
        glowColor = new Color(1.0f, 0.95f, 0.3f, glowIntensity * 0.6f);
        batch.setColor(glowColor);

        glowScale = 1.07f;
        glowWidth = width * glowScale;
        glowHeight = height * glowScale;
        glowX = x - (glowWidth - width) / 2;
        glowY = y - (glowHeight - height) / 2;

        batch.draw(texture, glowX, glowY, glowWidth, glowHeight);
    }

    @Override
    public String getCombatBackground() {
        return COMBAT_BACKGROUND;
    }

    @Override
    public String getCombatMusic() {
        return COMBAT_MUSIC;
    }

}
