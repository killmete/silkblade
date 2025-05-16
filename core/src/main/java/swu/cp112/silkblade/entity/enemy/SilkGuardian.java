package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.pattern.EnemyAttackPatternManager;
import swu.cp112.silkblade.pattern.silkguardian.EvolutionaryAttackPattern;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Silk Guardian - The boss enemy for stage 10
 * Features 2 phases:
 * - Phase 1: Normal attack pattern with enhanced SilkWraith abilities
 * - Phase 2 (at 60% HP): Rainbow colored sprite and projectiles with increased damage
 */
public class SilkGuardian extends AbstractEnemy {
    private Player player;
    private boolean inPhase2;
    private float phaseTransitionTimer;
    private float rainbowEffectTimer;

    // Constants
    private static final int BASE_HP = 100;
    private static final int BASE_ATTACK = 6;
    private static final int BASE_XP = 50;
    private static final int BASE_GOLD = 75;
    private static final float PHASE_2_HP_THRESHOLD = 0.6f; // 60% HP
    private static final float PHASE_TRANSITION_DURATION = 2.0f;

    // Background to use for Silk Guardian encounters
    public static final String COMBAT_BACKGROUND = "background/Phase_1.jpeg";

    // Music to use for Silk Guardian encounters
    public static final String COMBAT_MUSIC = "music/mus_boss1.mp3";

    // Rainbow effect constants
    private static final float RAINBOW_CYCLE_SPEED = 0.5f;
    private static final float RAINBOW_COLOR_SATURATION = 0.9f;
    private static final float RAINBOW_COLOR_VALUE = 1.0f;

    public SilkGuardian() {
        super("Silk Guardian", BASE_HP,
                new Texture(Gdx.files.internal("enemy/silkGuardian.png")),
                300f, 300f);

        GameLogger.logInfo("Creating Silk Guardian boss for stage 10");

        // Initialize components
        this.player = Player.loadFromFile();
        this.inPhase2 = false;
        this.phaseTransitionTimer = 0f;
        this.rainbowEffectTimer = 0f;

        // Clear and add attack patterns
        this.patternManager = new EnemyAttackPatternManager(); // Reinitialize
        this.addAttackPattern(new EvolutionaryAttackPattern());

        // Select initial pattern explicitly
        this.currentPattern = this.patternManager.selectRandomPattern();

        // Initialize remaining properties
        initializeGuardian();
    }

    private void initializeGuardian() {
        // Set base rewards
        this.setBaseRewards(BASE_XP, BASE_GOLD);

        // Set primary color (will be white in phase 1, rainbow in phase 2)
        this.primaryColor = Color.WHITE;

        // Initialize dialogues
        initializeDialogues();

        // Set base attack interval (will be reduced in phase 2)
        this.attackInterval = 0.8f;
        this.attackDamage = BASE_ATTACK;
        scaleToPlayerLevel(player.getLevel());
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Check for phase transition at 60% HP
        if (!inPhase2 && (float)currentHP / maxHP <= PHASE_2_HP_THRESHOLD) {
            // Enter phase 2
            enterPhase2();
        }

        // Update transition timer if in transition
        if (phaseTransitionTimer > 0) {
            phaseTransitionTimer -= delta;
            if (phaseTransitionTimer <= 0) {
                // Transition complete
                phaseTransitionTimer = 0;
            }
        }

        // Update rainbow effect if in phase 2
        if (inPhase2) {
            rainbowEffectTimer += delta * RAINBOW_CYCLE_SPEED;
            if (rainbowEffectTimer > 1.0f) {
                rainbowEffectTimer -= 1.0f;
            }

            // Update rainbow color
            updateRainbowColor();
        }
    }

    /**
     * Transition to phase 2
     */
    private void enterPhase2() {
        if (!inPhase2) {
            inPhase2 = true;
            phaseTransitionTimer = PHASE_TRANSITION_DURATION;

            // Increase attack damage in phase 2
            attackDamage = (int)(BASE_ATTACK * 1.25f);

            // Decrease attack interval
            attackInterval = 0.6f;

            GameLogger.logInfo("Silk Guardian entered Phase 2");
        }
    }

    /**
     * Updates the rainbow color effect for phase 2
     */
    private void updateRainbowColor() {
        if (inPhase2) {
            // Create rainbow cycling effect
            float hue = rainbowEffectTimer * 360f;

            // Convert HSV to RGB
            int c = java.awt.Color.HSBtoRGB(hue / 360f, RAINBOW_COLOR_SATURATION, RAINBOW_COLOR_VALUE);
            java.awt.Color color = new java.awt.Color(c);

            // Set the primary color with a faint rainbow effect
            this.primaryColor = new Color(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                0.8f  // Slightly translucent
            );
        }
    }

    private void initializeDialogues() {
        // Set encounter dialogue
        this.encounterDialogue = "The Silk Guardian materializes before you!";

        // Set attack dialogues
        this.attackDialogue = "The Silk Guardian weaves a deadly pattern!";

        // Set turn pass dialogues
        this.clearRandomTurnDialogues();
        this.addRandomTurnDialogue("Threads of silk flow around the Guardian.");
        this.addRandomTurnDialogue("The air shimmers with silken energy.");
        this.addRandomTurnDialogue("The Guardian's form shifts and warps.");
        this.addRandomTurnDialogue("An eerie silence surrounds the Guardian.");
        this.addRandomTurnDialogue("The Guardian's gaze follows your every move.");

        // Set defeat and victory dialogues
        this.defeatDialogue = "The Silk Guardian dissolves into threads of light...";
        this.victoryDialogue = "The Silk Guardian has overwhelmed you!";

        // Set phase 2 specific dialogues (will be used in combat scene)
        this.addRandomTurnDialogue("Rainbow energy pulses through the Guardian's form!");
        this.addRandomTurnDialogue("Prismatic silk threads dance in the air!");
        this.addRandomTurnDialogue("The Guardian's form ripples with rainbow energy!");
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // Create a copy of our primary color
        Color tintWithAlpha = new Color(primaryColor);

        // Apply the alpha from AbstractEnemy class to maintain dimming effect
        tintWithAlpha.a = currentAlpha;

        // Set the batch color to our tint with proper alpha
        Color originalColor = batch.getColor().cpy();
        batch.setColor(tintWithAlpha);

        // Draw with faint glow or vibration effect during phase transition
        float drawX = x + (isShaking ? shakeX : 0);
        float drawY = y;

        if (phaseTransitionTimer > 0) {
            // Add vibration during phase transition
            drawX += MathUtils.random(-5, 5);
            drawY += MathUtils.random(-5, 5);
        }

        batch.draw(texture, drawX, drawY, width, height);

        // Reset batch color
        batch.setColor(originalColor);

        // In phase 2, draw an additional rainbow glow effect
        if (inPhase2) {
            // Save original blend function and color
            batch.setColor(1, 1, 1, 0.3f);

            // Draw a slightly larger version behind for glow effect
            float glowScale = 1.05f;
            float glowWidth = width * glowScale;
            float glowHeight = height * glowScale;
            float glowX = drawX - (glowWidth - width) / 2;
            float glowY = drawY - (glowHeight - height) / 2;

            batch.draw(texture, glowX, glowY, glowWidth, glowHeight);

            // Reset to original settings
            batch.setColor(originalColor);
        }
    }

    /**
     * Returns whether the boss is in phase 2
     */
    public boolean isInPhase2() {
        return inPhase2;
    }

    /**
     * Returns whether the boss is currently transitioning between phases
     */
    public boolean isPhaseTransitioning() {
        return phaseTransitionTimer > 0;
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
