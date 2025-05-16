package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.pattern.crimsonsericulture.PrecisionNeedlePattern;
import swu.cp112.silkblade.pattern.crimsonsericulture.ThreadweaverPattern;
import swu.cp112.silkblade.pattern.crimsonsericulture.PrismaticAssaultPattern;
import swu.cp112.silkblade.screen.StageSelectionScreen;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Crimson Sericulture - The boss enemy for stage 40
 * Features 2 phases:
 * - Phase 1: Precise, deadly patterns with crimson-colored bullets
 * - Phase 2 (at 50% HP): Transforms into Overlimit Sericulture with rainbow colors
 *   and gains ability to heal itself (5% of maxHP after each turn)
 */
public class CrimsonSericulture extends AbstractEnemy {
    private Player player;
    private boolean inSecondPhase;
    private float phaseTransitionTimer;
    private float rainbowTimer;
    private boolean shouldHealAfterTurn;

    // Constants
    private static final int BASE_HP = 200;
    private static final int BASE_ATTACK = 8;
    private static final int BASE_XP = 500;
    private static final int BASE_GOLD = 800;
    private static final float PHASE_2_HP_THRESHOLD = 0.5f; // 50% HP
    private static final float PHASE_TRANSITION_DURATION = 3.0f;
    private static final float HEAL_PERCENT = 0.05f; // 5% of maxHP

    // Background to use for encounters
    public static final String COMBAT_BACKGROUND = "background/Phase_4.jpeg";

    // Music to use for encounters
    public static final String COMBAT_MUSIC = "music/mus_boss4.mp3";

    // Rainbow effect constants
    private static final float RAINBOW_CYCLE_SPEED = 1.0f;
    private static final float RAINBOW_COLOR_SATURATION = 0.9f;
    private static final float RAINBOW_COLOR_VALUE = 1.0f;
    private static final float PULSE_SPEED = 1.0f;
    private static final float MIN_PULSE = 0.95f;
    private static final float MAX_PULSE = 1.05f;
    private float pulseScale = 1.0f;
    private float pulseDirection = 1.0f;

    public CrimsonSericulture() {
        super("Crimson Sericulture", BASE_HP,
                new Texture(Gdx.files.internal("enemy/crimsonSericulture.png")),
                350f, 350f);

        GameLogger.logInfo("Creating Crimson Sericulture boss for stage 40");

        // Initialize components
        this.player = Player.loadFromFile();
        this.inSecondPhase = false;
        this.phaseTransitionTimer = 0f;
        this.rainbowTimer = 0f;
        this.shouldHealAfterTurn = false;

        // Add all attack patterns - precise and deadly
        this.addAttackPattern(new PrecisionNeedlePattern());
        this.addAttackPattern(new ThreadweaverPattern());
        // Rainbow pattern is added when phase 2 begins

        // Select initial pattern explicitly
        this.currentPattern = this.patternManager.selectRandomPattern();

        // Initialize remaining properties
        initializeSericulture();
    }

    private void initializeSericulture() {
        // Set base rewards
        this.setBaseRewards(BASE_XP, BASE_GOLD);

        // Set primary color (deep crimson)
        this.primaryColor = new Color(0.8f, 0.1f, 0.1f, 1.0f);

        // Initialize dialogues
        initializeDialogues();

        // Set base attack properties (will be increased in phase 2)
        this.attackInterval = 1.2f;
        this.attackDamage = BASE_ATTACK;
        scaleToPlayerLevel(player.getLevel());
    }

    private void initializeDialogues() {
        // Initialize phase 1 dialogues
        this.encounterDialogue = "The air thickens with crimson silk strands. Crimson Sericulture appears!";
        this.attackDialogue = "The silk master weaves a deadly pattern!";
        this.defeatDialogue = "The silk master's threads dissolve into mist...";
        this.victoryDialogue = "You have been wrapped in the crimson cocoon. Your journey ends here.";

        // Add custom dialogues
        this.setTurnPassDialogue("The Crimson Sericulture watches your movements carefully.");
        this.setPlayerTurnStartDialogue("The silk master's threads quiver with anticipation.");

        // Clear random dialogues and add custom ones
        this.clearRandomTurnDialogues();
        this.addRandomTurnDialogue("Crimson threads dance in the air.");
        this.addRandomTurnDialogue("You feel the sharp gaze of the silk master upon you.");
        this.addRandomTurnDialogue("The air fills with a soft crimson glow.");
        this.addRandomTurnDialogue("The Crimson Sericulture prepares its next weave.");
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Handle phase transition
        checkPhaseTransition();

        if (inSecondPhase) {
            // Update rainbow color effect
            updateRainbowEffect(delta);

            // Update pulse effect in phase 2
            updatePulseEffect(delta);
        }
    }

    private void checkPhaseTransition() {
        if (!inSecondPhase && this.currentHP <= this.maxHP * PHASE_2_HP_THRESHOLD) {
            // Trigger phase 2 transition
            startPhaseTransition();
        }

        // Update transition animation
        if (phaseTransitionTimer > 0) {
            phaseTransitionTimer -= Gdx.graphics.getDeltaTime();

            // If transition complete, finalize phase 2
            if (phaseTransitionTimer <= 0) {
                finalizePhaseTransition();
            }
        }
    }

    private void startPhaseTransition() {
        inSecondPhase = true;
        phaseTransitionTimer = PHASE_TRANSITION_DURATION;

        // Set special transition visuals
        this.setAlpha(0.5f);

        // Log the phase change
        GameLogger.logInfo("Crimson Sericulture entering phase 2 transition");

        // Trigger screen shake for impact
        this.triggerScreenShake(1.0f, 5.0f);

        // Phase transition dialog
        this.setTurnPassDialogue("The Crimson Sericulture begins to pulsate with rainbow energy!");
    }

    private void finalizePhaseTransition() {
        // Change name
        this.name = "Overlimit Sericulture";

        // Restore alpha
        this.setAlpha(1.0f);

        // Enable healing after turn
        this.shouldHealAfterTurn = true;

        // Add the rainbow pattern that includes healing abilities
        this.addAttackPattern(new PrismaticAssaultPattern());

        // Increase attack speed and damage for phase 2
        this.attackInterval *= 0.8f;
        this.attackDamage = (int)(this.attackDamage * 1.1f);

        // Log completion
        GameLogger.logInfo("Crimson Sericulture phase 2 transition complete");

        // Phase 2 dialogue updates
        this.setTurnPassDialogue("The Overlimit Sericulture's rainbow threads pulse with power!");
        this.setPlayerTurnStartDialogue("The rainbow silk master awaits your move.");
        this.clearRandomTurnDialogues();
        this.addRandomTurnDialogue("Rainbow energy flows through the silk master.");
        this.addRandomTurnDialogue("The air crackles with prismatic energy.");
        this.addRandomTurnDialogue("The Overlimit Sericulture's threads shimmer with healing light.");
    }

    private void updateRainbowEffect(float delta) {
        // Update rainbow timer
        rainbowTimer += delta * RAINBOW_CYCLE_SPEED;
        if (rainbowTimer > 1.0f) {
            rainbowTimer -= 1.0f;
        }

        // Calculate rainbow color
        float hue = rainbowTimer;
        Color rainbowColor = new Color();
        rainbowColor.fromHsv(hue * 360f, RAINBOW_COLOR_SATURATION, RAINBOW_COLOR_VALUE);

        // Apply rainbow color
        this.primaryColor = rainbowColor;
    }

    private void updatePulseEffect(float delta) {
        // Update pulse scale
        pulseScale += pulseDirection * delta * PULSE_SPEED;

        // Reverse direction at limits
        if (pulseScale > MAX_PULSE) {
            pulseScale = MAX_PULSE;
            pulseDirection = -1.0f;
        } else if (pulseScale < MIN_PULSE) {
            pulseScale = MIN_PULSE;
            pulseDirection = 1.0f;
        }
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // Save original color
        Color originalColor = batch.getColor().cpy();

        if (inSecondPhase) {
            // Apply scaling for pulse effect
            float originalWidth = this.width;
            float originalHeight = this.height;
            this.width *= pulseScale;
            this.height *= pulseScale;

            // Adjust position to maintain center with scaling
            float adjustedX = x - (this.width - originalWidth) / 2;
            float adjustedY = y - (this.height - originalHeight) / 2;

            // Draw with rainbow color
            batch.setColor(this.primaryColor);
            drawWithEffects(batch, adjustedX, adjustedY);

            // Restore original dimensions
            this.width = originalWidth;
            this.height = originalHeight;
        } else {
            // Standard drawing for phase 1
            batch.setColor(this.primaryColor);
            drawWithEffects(batch, x, y);
        }

        // Restore original color
        batch.setColor(originalColor);
    }

    private void drawWithEffects(SpriteBatch batch, float x, float y) {
        // Apply shake effect if active
        float drawX = x;
        if (isShaking()) {
            drawX += shakeX;
        }

        // Draw with current alpha
        Color color = batch.getColor();
        batch.setColor(color.r, color.g, color.b, currentAlpha);
        batch.draw(texture, drawX, y, width, height);
    }

    @Override
    public void endTurn() {
        super.endTurn();

        // Apply healing after turn if in phase 2
        if (inSecondPhase && shouldHealAfterTurn) {
            int healAmount = (int)(maxHP * HEAL_PERCENT);
            int newHP = Math.min(currentHP + healAmount, maxHP);
            int actualHealAmount = newHP - currentHP;

            if (actualHealAmount > 0) {
                this.currentHP = newHP;
                GameLogger.logInfo("Overlimit Sericulture healed for " + actualHealAmount + " HP");
            }
        }
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
