package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.pattern.EnemyAttackPatternManager;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.pattern.silkcicada.HighAttackPattern;
import swu.cp112.silkblade.pattern.silkcicada.MediumAttackPattern;
import swu.cp112.silkblade.pattern.silkcicada.LowAttackPattern;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Silk Cicada - A late-game enemy for stages 21-29
 * Features different visual tints based on stage range
 * Uses Phase_3.jpeg as the combat background for all encounters
 */
public class SilkCicada extends AbstractEnemy {
    private Player player;
    private int stage;

    // Base stats (higher than Silk Weaver)
    private static final int BASE_HP = 180;
    private static final int BASE_ATTACK = 25;
    private static final int BASE_XP = 50;
    private static final int BASE_GOLD = 60;

    // Background to use for all Silk Cicada encounters
    public static final String COMBAT_BACKGROUND = "background/Phase_3.jpeg";

    // Music to use for all Silk Cicada encounters
    public static final String COMBAT_MUSIC = "music/Phase_3.mp3";

    public SilkCicada(int stage) {
        super("Silk Cicada", BASE_HP,
                new Texture(Gdx.files.internal("enemy/baseCicada.png")),
                300f, 300f);

        // Ensure stage is between 21-29
        this.stage = Math.max(21, Math.min(29, stage));

        // Log the stage being created
        GameLogger.logInfo("Creating SilkCicada for stage " + this.stage);

        // Initialize components
        this.player = Player.loadFromFile();

        // Clear and add attack patterns based on stage range
        this.patternManager = new EnemyAttackPatternManager(); // Reinitialize

        // Select appropriate attack patterns based on stage range
        if (stage >= 27 && stage <= 29) {
            // Stages 27-29: High difficulty patterns
            this.addAttackPattern(new HighAttackPattern());
            GameLogger.logInfo("SilkCicada: Using High Attack Pattern for stage " + stage);
        } else if (stage >= 24 && stage <= 26) {
            // Stages 24-26: Medium difficulty patterns
            this.addAttackPattern(new MediumAttackPattern());
            GameLogger.logInfo("SilkCicada: Using Medium Attack Pattern for stage " + stage);
        } else {
            // Stages 21-23: Low difficulty patterns
            this.addAttackPattern(new LowAttackPattern());
            GameLogger.logInfo("SilkCicada: Using Low Attack Pattern for stage " + stage);
        }

        // Select initial pattern explicitly
        this.currentPattern = this.patternManager.selectRandomPattern();

        // Initialize remaining properties
        initializeEnemy();
    }

    private void initializeEnemy() {
        // Set base rewards
        this.setBaseRewards(BASE_XP, BASE_GOLD);
        scaleToPlayerLevel(player.getLevel());
        // Scale stats based on stage
        scaleToStage(this.stage);

        // Set tint color based on stage range
        updateVisualTint();

        // Initialize dialogues
        initializeDialogues();
    }

    /**
     * Scale enemy stats based on the current stage number (21-29)
     */
    private void scaleToStage(int stage) {
        float stageMultiplier = 1.0f + ((stage - 21) * 0.25f);

        // Scale HP and attack with stage
        this.maxHP = (int)(BASE_HP * stageMultiplier);
        this.currentHP = this.maxHP;
        this.attackDamage = (int)(BASE_ATTACK * stageMultiplier);

        // Scale rewards with stage
        this.xp = (int)(BASE_XP * stageMultiplier);
        this.baht = (int)(BASE_GOLD * stageMultiplier);

        // Update reward dialogue
        this.rewardDialogue = "You earned " + this.xp + " XP and " + this.baht + " GOLD.";
    }

    /**
     * Update the visual tint based on stage range
     */
    private void updateVisualTint() {
        if (stage >= 27 && stage <= 29) {
            // Stages 27-29: Red tint
            this.primaryColor = new Color(1.0f, 0.3f, 0.3f, 1.0f);
            // Update name to reflect appearance
            this.name = "Crimson Silk Cicada";
            GameLogger.logInfo("SilkCicada: Applied red tint for stage " + stage);
        } else if (stage >= 24 && stage <= 26) {
            // Stages 24-26: Green tint
            this.primaryColor = new Color(0.3f, 1.0f, 0.5f, 1.0f);
            // Update name to reflect appearance
            this.name = "Emerald Silk Cicada";
            GameLogger.logInfo("SilkCicada: Applied green tint for stage " + stage);
        } else {
            // Stages 21-23: No tint (white)
            this.primaryColor = Color.WHITE;
            // Keep original name
            this.name = "Silk Cicada";
            GameLogger.logInfo("SilkCicada: Applied no tint (white) for stage " + stage);
        }
    }

    private void initializeDialogues() {
        // Set encounter dialogue
        this.encounterDialogue = "A " + this.getName() + " appears!";

        // Set attack dialogue based on stage range
        if (stage >= 27) {
            this.attackDialogue = "The Crimson Silk Cicada unleashes a blazing strike!";
        } else if (stage >= 24) {
            this.attackDialogue = "The Emerald Silk Cicada launches a venomous assault!";
        } else {
            this.attackDialogue = "The Silk Cicada attacks!";
        }

        // Clear and add random turn dialogues
        this.clearRandomTurnDialogues();

        // Add different dialogues based on stage range
        if (stage >= 27) {
            this.addRandomTurnDialogue("The Crimson Silk Cicada's wings pulse with fiery energy.");
            this.addRandomTurnDialogue("Burning chitin crackles with intense heat.");
            this.addRandomTurnDialogue("The Cicada's eyes glow with deep crimson malice.");
        } else if (stage >= 24) {
            this.addRandomTurnDialogue("The Emerald Silk Cicada's wings shimmer with toxic energy.");
            this.addRandomTurnDialogue("Emerald scales flutter, releasing a subtle poison mist.");
            this.addRandomTurnDialogue("The Cicada's razor-sharp limbs slice through the air.");
        } else {
            this.addRandomTurnDialogue("The Silk Cicada's wings vibrate with a deafening hum.");
            this.addRandomTurnDialogue("Iridescent wings catch the light as the Cicada shifts position.");
            this.addRandomTurnDialogue("The Cicada's exoskeleton hardens as it prepares to strike.");
        }

        // Set defeat and victory dialogues
        this.defeatDialogue = "The Silk Cicada shatters into crystalline fragments...";
        this.victoryDialogue = "The Silk Cicada has defeated you!";
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // Get current color before drawing
        Color currentColor = batch.getColor().cpy();

        // Create a copy of our primary color
        Color tintWithAlpha = new Color(primaryColor);

        // Apply the alpha from AbstractEnemy class to maintain dimming effect
        tintWithAlpha.a = currentAlpha;

        // Set the batch color to our tint with proper alpha
        batch.setColor(tintWithAlpha);

        // Draw the sprite with the tint and alpha
        float drawX = x + (isShaking ? shakeX : 0);
        batch.draw(texture, drawX, y, width, height);

        // Reset batch color to original
        batch.setColor(currentColor);
    }

    @Override
    public String getCombatBackground() {
        return COMBAT_BACKGROUND;
    }

    @Override
    public String getCombatMusic() {
        return COMBAT_MUSIC;
    }

    /**
     * Returns the current stage of this Silk Cicada
     * @return the stage number (21-29)
     */
    public int getStage() {
        return this.stage;
    }
}
