package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.pattern.EnemyAttackPatternManager;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.pattern.silkwraith.BasicAttackPattern;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Silk Wraith - An early game enemy for stages 1-9
 * Features different visual tints based on stage range
 * Uses Phase_1.jpeg as the combat background for all encounters
 */
public class SilkWraith extends AbstractEnemy {
    private Player player;
    private int stage;

    // Base stats
    private static final int BASE_HP = 30;
    private static final int BASE_ATTACK = 5;
    private static final int BASE_XP = 15;
    private static final int BASE_GOLD = 20;

    // Background to use for all Silk Wraith encounters
    public static final String COMBAT_BACKGROUND = "background/Phase_1.jpeg";

    // Music to use for all Silk Wraith encounters
    public static final String COMBAT_MUSIC = "music/Phase_1.mp3";

    public SilkWraith(int stage) {
        super("Silk Wraith", BASE_HP,
                new Texture(Gdx.files.internal("enemy/baseWraith.png")),
                250f, 250f);

        // Ensure stage is between 1-9
        this.stage = Math.max(1, Math.min(9, stage));

        // Log the stage being created
        GameLogger.logInfo("Creating SilkWraith for stage " + this.stage);

        // Initialize components
        this.player = Player.loadFromFile();

        // Clear and add attack patterns
        this.patternManager = new EnemyAttackPatternManager(); // Reinitialize
        this.addAttackPattern(new BasicAttackPattern());

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

        // Note: The background "background/Phase_1.jpeg" should be set in the CombatScene
        // when this enemy is encountered

        // Initialize dialogues
        initializeDialogues();
    }

    /**
     * Scale enemy stats based on the current stage number (1-9)
     */
    private void scaleToStage(int stage) {
        float stageMultiplier = 1.0f + ((stage - 1) * 0.25f);

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
        if (stage >= 7 && stage <= 9) {
            // Stages 7-9: Gold tint
            this.primaryColor = new Color(1.0f, 0.85f, 0.0f, 1.0f);
            // Update name to reflect appearance
            this.name = "Golden Silk Wraith";
            GameLogger.logInfo("SilkWraith: Applied gold tint for stage " + stage);
        } else if (stage >= 4 && stage <= 6) {
            // Stages 4-6: Red tint
            this.primaryColor = new Color(1.0f, 0.3f, 0.3f, 1.0f);
            // Update name to reflect appearance
            this.name = "Crimson Silk Wraith";
            GameLogger.logInfo("SilkWraith: Applied red tint for stage " + stage);
        } else {
            // Stages 1-3: No tint (white)
            this.primaryColor = Color.WHITE;
            // Keep original name
            this.name = "Silk Wraith";
            GameLogger.logInfo("SilkWraith: Applied no tint (white) for stage " + stage);
        }
    }

    private void initializeDialogues() {
        // Set encounter dialogue
        this.encounterDialogue = "A " + this.getName() + " appears!";

        // Set attack dialogue based on stage range
        if (stage >= 7) {
            this.attackDialogue = "The Golden Silk Wraith attacks with deadly precision!";
        } else if (stage >= 4) {
            this.attackDialogue = "The Crimson Silk Wraith attacks with fierce intent!";
        } else {
            this.attackDialogue = "The Silk Wraith attacks!";
        }

        // Clear and add random turn dialogues
        this.clearRandomTurnDialogues();

        // Add different dialogues based on stage range
        if (stage >= 7) {
            this.addRandomTurnDialogue("The Golden Silk Wraith shimmers menacingly.");
            this.addRandomTurnDialogue("Golden threads dance in the air.");
            this.addRandomTurnDialogue("The Wraith's gilded form twists unnaturally.");
        } else if (stage >= 4) {
            this.addRandomTurnDialogue("The Crimson Silk Wraith pulses with energy.");
            this.addRandomTurnDialogue("Blood-red threads float around you.");
            this.addRandomTurnDialogue("The Wraith's crimson form contorts violently.");
        } else {
            this.addRandomTurnDialogue("The Silk Wraith floats ominously.");
            this.addRandomTurnDialogue("Silken threads drift in the air.");
            this.addRandomTurnDialogue("The Wraith's form shifts and warps.");
        }

        // Set defeat and victory dialogues
        this.defeatDialogue = "The Silk Wraith dissolves into threads...";
        this.victoryDialogue = "The Silk Wraith has defeated you!";
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
     * Returns the current stage of this Silk Wraith
     * @return the stage number (1-9)
     */
    public int getStage() {
        return this.stage;
    }
}
