package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.pattern.EnemyAttackPatternManager;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.pattern.hundredsilkogre.OgreEvolutionPattern;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Hundred-Silk Ogre - The final enemy family before the boss for stages 41-49
 * Features a complex evolution-based attack pattern system that becomes more chaotic
 * with higher stage groups. The enemy's appearance shifts through vibrant colors.
 * Uses Phase_5.jpeg as the combat background for all encounters.
 */
public class HundredSilkOgre extends AbstractEnemy {
    private Player player;
    private int stage;

    // Base stats (higher than Spirit of the Loom)
    private static final int BASE_HP = 300;
    private static final int BASE_ATTACK = 20;
    private static final int BASE_XP = 100;
    private static final int BASE_GOLD = 120;

    // Background to use for all Hundred-Silk Ogre encounters
    public static final String COMBAT_BACKGROUND = "background/Phase_5.jpeg";

    // Music to use for all Hundred-Silk Ogre encounters
    public static final String COMBAT_MUSIC = "music/Phase_5.mp3";

    // Visual effects
    private float colorShiftTimer = 0f;
    private float colorShiftSpeed = 0.5f;
    private Color[] colorShiftPalette = {
        new Color(0.8f, 0.2f, 0.2f, 1f), // Red
        new Color(0.8f, 0.5f, 0.2f, 1f), // Orange
        new Color(0.8f, 0.8f, 0.2f, 1f), // Yellow
        new Color(0.2f, 0.8f, 0.2f, 1f), // Green
        new Color(0.2f, 0.5f, 0.8f, 1f), // Blue
        new Color(0.5f, 0.2f, 0.8f, 1f)  // Purple
    };
    private int currentColorIndex = 0;
    private int targetColorIndex = 1;
    private float colorLerpProgress = 0f;
    private static final float COLOR_TRANSITION_DURATION = 0.25f; // Time in seconds for each color transition

    public HundredSilkOgre(int stage) {
        super("Hundred-Silk Ogre", BASE_HP,
                new Texture(Gdx.files.internal("enemy/baseOgre.png")),
                350f, 350f);

        // Ensure stage is between 41-49
        this.stage = Math.max(41, Math.min(49, stage));

        // Log the stage being created
        GameLogger.logInfo("Creating HundredSilkOgre for stage " + this.stage);

        // Initialize components
        this.player = Player.loadFromFile();

        // Clear any default patterns
        this.patternManager = new EnemyAttackPatternManager(); // Reinitialize

        // Add the evolving pattern based on stage group
        int stageGroup = 1;
        int evolutionCount = 2; // Default for stage group 1 (2 patterns)

        if (stage >= 47) {
            stageGroup = 3; // Stages 47-49: Most complex pattern
            evolutionCount = 4; // 4 patterns for stage group 3
            GameLogger.logInfo("HundredSilkOgre: Using stage group 3 pattern (47-49) with " + evolutionCount + " evolutions");
        } else if (stage >= 44) {
            stageGroup = 2; // Stages 44-46: Moderate complexity
            evolutionCount = 3; // 3 patterns for stage group 2
            GameLogger.logInfo("HundredSilkOgre: Using stage group 2 pattern (44-46) with " + evolutionCount + " evolutions");
        } else {
            stageGroup = 1; // Stages 41-43: Basic pattern
            evolutionCount = 2; // 2 patterns for stage group 1
            GameLogger.logInfo("HundredSilkOgre: Using stage group 1 pattern (41-43) with " + evolutionCount + " evolutions");
        }

        // Add the evolution pattern that internally handles complexity based on stage group
        this.addAttackPattern(new OgreEvolutionPattern(stageGroup, evolutionCount));
        GameLogger.logInfo("HundredSilkOgre: Initialized OgreEvolutionPattern with stage group " + stageGroup);

        // Select initial pattern explicitly
        this.currentPattern = this.patternManager.selectRandomPattern();

        // Initialize remaining properties
        initializeEnemy();

        // Set color shift speed based on stage group - higher stages shift colors faster
        // Use a more moderate speed scaling to prevent too rapid shifting
        colorShiftSpeed = 0.3f + (stageGroup * 0.1f);
    }

    private void initializeEnemy() {
        // Set base rewards
        this.setBaseRewards(BASE_XP, BASE_GOLD);
        scaleToPlayerLevel(player.getLevel());
        // Scale stats based on stage
        scaleToStage(this.stage);

        // Set initial color (will shift during gameplay)
        this.primaryColor = colorShiftPalette[0];

        // Initialize dialogues
        initializeDialogues();
    }

    /**
     * Scale enemy stats based on the current stage number (41-49)
     * This enemy gets progressively stronger with higher stages
     */
    private void scaleToStage(int stage) {
        float stageMultiplier = 1.0f + ((stage - 41) * 0.3f);

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
        // Set encounter dialogue based on stage group
        if (stage >= 47) {
            this.encounterDialogue = "A Transcendent Hundred-Silk Ogre materializes!";
            this.name = "Transcendent Hundred-Silk Ogre";
        } else if (stage >= 44) {
            this.encounterDialogue = "A Greater Hundred-Silk Ogre appears!";
            this.name = "Greater Hundred-Silk Ogre";
        } else {
            this.encounterDialogue = "A Hundred-Silk Ogre appears!";
        }

        // Set attack dialogue based on stage group
        if (stage >= 47) {
            this.attackDialogue = "The Transcendent Ogre unleashes a symphony of chromatic fury!";
        } else if (stage >= 44) {
            this.attackDialogue = "The Greater Ogre channels the power of a hundred silk strands!";
        } else {
            this.attackDialogue = "The Hundred-Silk Ogre attacks with primal force!";
        }

        // Clear and add random turn dialogues
        this.clearRandomTurnDialogues();

        // Common dialogues for all stage groups
        this.addRandomTurnDialogue("Strands of multicolored silk twist around the Ogre's massive form.");
        this.addRandomTurnDialogue("The air vibrates with the Ogre's thunderous breathing.");

        // Add different dialogues based on stage group
        if (stage >= 47) {
            this.addRandomTurnDialogue("Reality fractures as the Transcendent Ogre shifts between dimensions.");
            this.addRandomTurnDialogue("A kaleidoscope of silk tendrils dances with lethal precision.");
            this.addRandomTurnDialogue("The Ogre's form blurs as it manipulates the very fabric of space.");
        } else if (stage >= 44) {
            this.addRandomTurnDialogue("The Greater Ogre's silk tendrils multiply and shift chaotically.");
            this.addRandomTurnDialogue("Waves of prismatic energy emanate from the Greater Ogre.");
            this.addRandomTurnDialogue("The Greater Ogre roars, sending shockwaves of silk energy outward.");
        } else {
            this.addRandomTurnDialogue("The Ogre's silk tendrils lash out with surprising speed.");
            this.addRandomTurnDialogue("Chaotic patterns of silk wrap around the Ogre's muscular form.");
            this.addRandomTurnDialogue("The ground trembles beneath the Ogre's massive presence.");
        }

        // Set defeat and victory dialogues
        this.defeatDialogue = "The Hundred-Silk Ogre dissolves into a shower of radiant threads...";
        this.victoryDialogue = "The Ogre's silken tendrils drag you into eternal darkness!";
    }

    /**
     * Update method for handling visual effects like color shifting
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        // Update color shift with a linear progression based on time
        colorShiftTimer += delta * colorShiftSpeed;

        // Calculate a smoother linear progress (0 to 1) based on time
        float transitionTime = COLOR_TRANSITION_DURATION / colorShiftSpeed;
        colorLerpProgress = (colorShiftTimer % transitionTime) / transitionTime;

        // Check if we need to move to the next color
        if (colorShiftTimer >= transitionTime) {
            currentColorIndex = targetColorIndex;
            targetColorIndex = (targetColorIndex + 1) % colorShiftPalette.length;
            colorShiftTimer = 0f;
            colorLerpProgress = 0f;
        }

        // Interpolate between current and target colors
        this.primaryColor = new Color(
            colorShiftPalette[currentColorIndex].r + (colorShiftPalette[targetColorIndex].r - colorShiftPalette[currentColorIndex].r) * colorLerpProgress,
            colorShiftPalette[currentColorIndex].g + (colorShiftPalette[targetColorIndex].g - colorShiftPalette[currentColorIndex].g) * colorLerpProgress,
            colorShiftPalette[currentColorIndex].b + (colorShiftPalette[targetColorIndex].b - colorShiftPalette[currentColorIndex].b) * colorLerpProgress,
            1f
        );
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
     * Returns the current stage of this Hundred-Silk Ogre
     * @return the stage number (41-49)
     */
    public int getStage() {
        return this.stage;
    }
}
