package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.pattern.EnemyAttackPatternManager;
import swu.cp112.silkblade.pattern.silkgod.ConvergingStarPattern;
import swu.cp112.silkblade.pattern.silkgod.CrossfirePattern;
import swu.cp112.silkblade.pattern.silkgod.TelegraphPattern;
import swu.cp112.silkblade.pattern.silkgod.RotatingStarPattern;
import swu.cp112.silkblade.pattern.silkgod.HomingExplosionPattern;
import swu.cp112.silkblade.pattern.silkgod.FallenStarPattern;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Silk Revenant - The boss for stage 50
 * A transcendent version of the Hundred-Silk Ogre with vibrant rainbow colors
 * and attack patterns inspired by but slightly toned down from DemoEnemy.
 * Uses a modified version of the DemoEnemy attack patterns.
 */
public class SilkRevenant extends AbstractEnemy {
    private Player player;
    private boolean isEnraged = false;

    // Base stats (higher than Hundred-Silk Ogre)
    private static final int BASE_HP = 400;
    private static final int BASE_XP = 5000;
    private static final int BASE_GOLD = 10000;

    // Music to use for this boss
    public static final String COMBAT_MUSIC = "music/mus_boss5.mp3";

    // Background to use for this boss
    public static final String COMBAT_BACKGROUND = "background/Phase_5.jpeg";

    // Visual effects for rainbow colors
    private float rainbowTimer = 0f;
    private float rainbowSpeed = 0.15f;
    private float rainbowHue = 0f;
    private float rainbowSaturation = 0.9f;
    private float rainbowValue = 1.0f;

    public SilkRevenant() {
        super("Silk Revenant", BASE_HP,
            new Texture(Gdx.files.internal("enemy/silkRevenant.png")),
            300f, 300f);

        // Initialize components
        this.player = Player.loadFromFile();

        // Add all patterns first
        // IMPORTANT: The order matters for pattern rotation
        // We're NOT calling initializePatterns() here anymore to avoid selecting a pattern too early
        // The first pattern will be selected when combat actually starts in startTurn()
        this.addAttackPattern(new TelegraphPattern());
        this.addAttackPattern(new CrossfirePattern());
        this.addAttackPattern(new ConvergingStarPattern());
        this.addAttackPattern(new RotatingStarPattern());
        this.addAttackPattern(new HomingExplosionPattern());
        this.addAttackPattern(new FallenStarPattern());
        // Don't initialize patterns yet - let combat scene handle it during startTurn

        // Initialize remaining properties
        initializeEnemy();
    }

    private void initializeEnemy() {
        // Set base rewards
        this.setBaseRewards(BASE_XP, BASE_GOLD);
        // Scale to player level
        scaleToPlayerLevel(player.getLevel());

        // Set primary color and hit sound
        this.primaryColor = Color.FIREBRICK;
        this.setHitSound("sounds/hit.wav");

        // Initialize dialogues
        initializeDialogues();
    }

    private void initializeDialogues() {
        this.encounterDialogue = "The Silk Revenant emerges from the void!";
        this.attackDialogue = "The Silk Revenant unleashes its cosmic fury!";
        this.defeatDialogue = "The Silk Revenant dissolves into prismatic threads of light...";
        this.victoryDialogue = "Your soul joins the infinite tapestry of the Silk Revenant!";
        this.rewardDialogue = "You earned " + this.xp + " XP and " + this.baht + " GOLD.";

        // Clear and add random turn dialogues
        this.clearRandomTurnDialogues();

        // Add different dialogues for variety
        this.addRandomTurnDialogue("Reality fractures around the Silk Revenant's transcendent form.");
        this.addRandomTurnDialogue("A kaleidoscope of silk tendrils dances with lethal precision.");
        this.addRandomTurnDialogue("The Revenant's rainbow aura pulses with ancient power.");
        this.addRandomTurnDialogue("Waves of cosmic energy emanate from the Silk Revenant.");
        this.addRandomTurnDialogue("The air shimmers with fragments of interdimensional silk.");
        this.addRandomTurnDialogue("The Revenant exists in multiple realities simultaneously.");
    }

    /**
     * Update method for handling visual effects like rainbow coloring
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // Update rainbow effect
        rainbowTimer += delta * rainbowSpeed;

        // Cycle through hues (0-1 range represents the full color spectrum)
        rainbowHue = (rainbowTimer % 1.0f);

        // Convert HSV to RGB for our primary color
        setHSVColor(rainbowHue, rainbowSaturation, rainbowValue);
    }

    /**
     * Set the primaryColor using HSV values for rainbow effect
     */
    private void setHSVColor(float h, float s, float v) {
        // Implement simple HSV to RGB conversion
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        float r, g, b;
        switch (i % 6) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            default:
                r = v;
                g = p;
                b = q;
                break;
        }

        this.primaryColor = new Color(r, g, b, 1.0f);
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // Get current color before drawing
        Color currentColor = batch.getColor().cpy();

        // Create a copy of our primary color with the proper alpha
        Color tintWithAlpha = new Color(primaryColor);
        tintWithAlpha.a = currentAlpha;

        // Set the batch color to our tint with proper alpha
        batch.setColor(tintWithAlpha);

        // Draw the sprite with the tint and alpha
        float drawX = x + (isShaking ? shakeX : 0);
        batch.draw(texture, drawX, y, width, height);

        // Draw an additional glow effect for the enraged state
        if (isEnraged) {
            // Pulse size for glow effect
            float pulseSize = 1.0f + 0.1f * MathUtils.sin(rainbowTimer * 10f);

            // Draw larger version behind with lower alpha for glow
            Color glowColor = new Color(tintWithAlpha);
            glowColor.a = 0.3f; // Lower alpha for glow
            batch.setColor(glowColor);

            // Calculate expanded dimensions and adjusted position
            float expandedWidth = width * pulseSize;
            float expandedHeight = height * pulseSize;
            float adjustedX = drawX - (expandedWidth - width) / 2;
            float adjustedY = y - (expandedHeight - height) / 2;

            // Draw the glow sprite
            batch.draw(texture, adjustedX, adjustedY, expandedWidth, expandedHeight);
        }

        // Reset batch color to original
        batch.setColor(currentColor);
    }
}
