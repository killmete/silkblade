package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.pattern.EnemyAttackPatternManager;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.pattern.spiritoftheloom.LoomPattern;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Spirit of the Loom - A late-game enemy for stages 31-39
 * Unlike other enemies, this one has no visual tinting regardless of stage
 * Instead, its attack pattern complexity increases with higher stages
 * Uses Phase_4.jpeg as the combat background for all encounters
 */
public class SpiritOfTheLoom extends AbstractEnemy {
    private Player player;
    private int stage;

    // Base stats (higher than Silk Cicada)
    private static final int BASE_HP = 340;
    private static final int BASE_ATTACK = 14;
    private static final int BASE_XP = 75;
    private static final int BASE_GOLD = 90;

    // Background to use for all Spirit of the Loom encounters
    public static final String COMBAT_BACKGROUND = "background/Phase_4.jpeg";

    // Music to use for all Spirit of the Loom encounters
    public static final String COMBAT_MUSIC = "music/Phase_4.mp3";

    public SpiritOfTheLoom(int stage) {
        super("Spirit of the Loom", BASE_HP,
                new Texture(Gdx.files.internal("enemy/baseSpirit.png")),
                320f, 320f);

        // Ensure stage is between 31-39
        this.stage = Math.max(31, Math.min(39, stage));

        // Log the stage being created
        GameLogger.logInfo("Creating SpiritOfTheLoom for stage " + this.stage);

        // Initialize components
        this.player = Player.loadFromFile();

        // Clear any default patterns
        this.patternManager = new EnemyAttackPatternManager(); // Reinitialize

        // Add the evolving pattern based on stage group
        int stageGroup = 1;
        if (stage >= 37) {
            stageGroup = 3; // Stages 37-39: Most complex pattern
            GameLogger.logInfo("SpiritOfTheLoom: Using stage group 3 pattern (37-39)");
        } else if (stage >= 34) {
            stageGroup = 2; // Stages 34-36: Moderate complexity
            GameLogger.logInfo("SpiritOfTheLoom: Using stage group 2 pattern (34-36)");
        } else {
            stageGroup = 1; // Stages 31-33: Basic pattern
            GameLogger.logInfo("SpiritOfTheLoom: Using stage group 1 pattern (31-33)");
        }

        // Add the single pattern that internally handles complexity based on stage group
        this.addAttackPattern(new LoomPattern(stageGroup));
        GameLogger.logInfo("SpiritOfTheLoom: Initialized LoomPattern with stage group " + stageGroup);

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

        // Set color (no tinting for this enemy regardless of stage)
        this.primaryColor = Color.WHITE;

        // Initialize dialogues
        initializeDialogues();
    }

    /**
     * Scale enemy stats based on the current stage number (31-39)
     * This enemy gets progressively stronger with higher stages
     */
    private void scaleToStage(int stage) {
        float stageMultiplier = 1.0f + ((stage - 31) * 0.2f);

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

    private void initializeDialogues() {
        // Set encounter dialogue
        this.encounterDialogue = "An ancient " + this.getName() + " appears!";

        // Set attack dialogue based on stage group
        if (stage >= 37) {
            this.attackDialogue = "The Spirit of the Loom weaves a deadly pattern of fate!";
        } else if (stage >= 34) {
            this.attackDialogue = "The Spirit of the Loom threads a complex attack pattern!";
        } else {
            this.attackDialogue = "The Spirit of the Loom begins its mystical weave!";
        }

        // Clear and add random turn dialogues
        this.clearRandomTurnDialogues();

        // Common dialogues for all stage groups
        this.addRandomTurnDialogue("Glowing threads of fate dance through the air.");
        this.addRandomTurnDialogue("The Spirit's form shifts like silk in the wind.");

        // Add different dialogues based on stage group
        if (stage >= 37) {
            this.addRandomTurnDialogue("The fabric of reality warps around the ancient spirit.");
            this.addRandomTurnDialogue("Cosmic threads bind and ensnare with deadly precision.");
            this.addRandomTurnDialogue("Time itself seems to slow as the Loom's pattern takes form.");
        } else if (stage >= 34) {
            this.addRandomTurnDialogue("Threads of light and shadow interweave in complex patterns.");
            this.addRandomTurnDialogue("The Spirit channels the essence of ancient weavers.");
            this.addRandomTurnDialogue("Reality trembles as the Loom's pattern manifests.");
        } else {
            this.addRandomTurnDialogue("Spectral threads reach out, seeking to entangle you.");
            this.addRandomTurnDialogue("The Loom's pattern grows more intricate with each moment.");
            this.addRandomTurnDialogue("The air hums with the spirit's otherworldly energy.");
        }

        // Set defeat and victory dialogues
        this.defeatDialogue = "The Spirit of the Loom unravels into countless shimmering threads...";
        this.victoryDialogue = "Your fate is woven into the eternal tapestry of the Loom!";
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // Get current color before drawing
        Color currentColor = batch.getColor().cpy();

        // Create a copy of our primary color (always white)
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
     * Returns the current stage of this Spirit of the Loom
     * @return the stage number (31-39)
     */
    public int getStage() {
        return this.stage;
    }
}
