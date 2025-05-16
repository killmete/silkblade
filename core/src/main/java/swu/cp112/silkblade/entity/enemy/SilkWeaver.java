package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.pattern.EnemyAttackPatternManager;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.pattern.silkweaver.MediumAttackPattern;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Silk Weaver - A mid-game enemy for stages 11-19
 * Features different visual tints based on stage range
 * Uses Phase_2.jpeg as the combat background for all encounters
 */
public class SilkWeaver extends AbstractEnemy {
    private Player player;
    private int stage;

    // Base stats (higher than Silk Wraith)
    private static final int BASE_HP = 120;
    private static final int BASE_ATTACK = 7;
    private static final int BASE_XP = 30;
    private static final int BASE_GOLD = 40;

    // Background to use for all Silk Weaver encounters
    public static final String COMBAT_BACKGROUND = "background/Phase_2.jpeg";

    // Music to use for all Silk Weaver encounters
    public static final String COMBAT_MUSIC = "music/Phase_2.mp3";

    public SilkWeaver(int stage) {
        super("Silk Weaver", BASE_HP,
                new Texture(Gdx.files.internal("enemy/baseWeaver.png")),
                300f, 300f);

        // Ensure stage is between 11-19
        this.stage = Math.max(11, Math.min(19, stage));

        // Log the stage being created
        GameLogger.logInfo("Creating SilkWeaver for stage " + this.stage);

        // Initialize components
        this.player = Player.loadFromFile();

        // Clear and add attack patterns
        this.patternManager = new EnemyAttackPatternManager(); // Reinitialize
        this.addAttackPattern(new MediumAttackPattern());

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
     * Scale enemy stats based on the current stage number (11-19)
     */
    private void scaleToStage(int stage) {
        float stageMultiplier = 1.0f + ((stage - 11) * 0.25f);

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
        if (stage >= 17 && stage <= 19) {
            // Stages 17-19: Purple tint
            this.primaryColor = new Color(0.8f, 0.3f, 1.0f, 1.0f);
            // Update name to reflect appearance
            this.name = "Arcane Silk Weaver";
            GameLogger.logInfo("SilkWeaver: Applied purple tint for stage " + stage);
        } else if (stage >= 14 && stage <= 16) {
            // Stages 14-16: Blue tint
            this.primaryColor = new Color(0.3f, 0.5f, 1.0f, 1.0f);
            // Update name to reflect appearance
            this.name = "Azure Silk Weaver";
            GameLogger.logInfo("SilkWeaver: Applied blue tint for stage " + stage);
        } else {
            // Stages 11-13: No tint (white)
            this.primaryColor = Color.WHITE;
            // Keep original name
            this.name = "Silk Weaver";
            GameLogger.logInfo("SilkWeaver: Applied no tint (white) for stage " + stage);
        }
    }

    private void initializeDialogues() {
        // Set encounter dialogue
        this.encounterDialogue = "A " + this.getName() + " appears!";

        // Set attack dialogue based on stage range
        if (stage >= 17) {
            this.attackDialogue = "The Arcane Silk Weaver unleashes a mystical barrage!";
        } else if (stage >= 14) {
            this.attackDialogue = "The Azure Silk Weaver launches a flurry of attacks!";
        } else {
            this.attackDialogue = "The Silk Weaver attacks!";
        }

        // Clear and add random turn dialogues
        this.clearRandomTurnDialogues();

        // Add different dialogues based on stage range
        if (stage >= 17) {
            this.addRandomTurnDialogue("The Arcane Silk Weaver pulses with otherworldly energy.");
            this.addRandomTurnDialogue("Mystic threads weave intricate patterns in the air.");
            this.addRandomTurnDialogue("The Weaver's tendrils glow with arcane power.");
        } else if (stage >= 14) {
            this.addRandomTurnDialogue("The Azure Silk Weaver's webbing crackles with energy.");
            this.addRandomTurnDialogue("Sapphire strands dance around the battlefield.");
            this.addRandomTurnDialogue("The Weaver's blue tendrils reach outward.");
        } else {
            this.addRandomTurnDialogue("The Silk Weaver's threads shimmer menacingly.");
            this.addRandomTurnDialogue("Silken strands create an intricate web around you.");
            this.addRandomTurnDialogue("The Weaver's form shifts as it prepares to strike.");
        }

        // Set defeat and victory dialogues
        this.defeatDialogue = "The Silk Weaver unravels into threads...";
        this.victoryDialogue = "The Silk Weaver has defeated you!";
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
     * Returns the current stage of this Silk Weaver
     * @return the stage number (11-19)
     */
    public int getStage() {
        return this.stage;
    }
}
