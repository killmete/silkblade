package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.pattern.threadmancer.ThreadCagePattern;
import swu.cp112.silkblade.pattern.threadmancer.PhantomNeedlePattern;
import swu.cp112.silkblade.pattern.threadmancer.WeavingMatrixPattern;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Threadmancer - The boss enemy for stage 30
 * A mystical entity that manipulates threads of reality with precision and artistry.
 * Uses 3 unique attack patterns that require strategic movement and timing to dodge.
 * Features hypnotic thread-like visual effects and precise, deadly attacks.
 */
public class Threadmancer extends AbstractEnemy {
    private Player player;
    private float threadAnimationTimer = 0f;
    private float threadPulsation = 0f;
    private float threadOpacity = 0.9f;
    private int currentThreadColor = 0;
    private int targetThreadColor = 1;
    private float colorTransitionProgress = 0f;

    // Constants
    private static final int BASE_HP = 200;
    private static final int BASE_ATTACK = 12;
    private static final int BASE_XP = 120;
    private static final int BASE_GOLD = 180;

    // Visual effects
    private static final float THREAD_ANIMATION_SPEED = 0.8f;
    private static final float COLOR_TRANSITION_SPEED = 0.6f;
    private static final float MIN_THREAD_OPACITY = 0.7f;
    private static final float MAX_THREAD_OPACITY = 1.0f;

    // Thread color palette
    private static final Color[] THREAD_COLORS = {
        new Color(0.8f, 0.2f, 0.8f, 1f),  // Purple
        new Color(0.2f, 0.6f, 0.9f, 1f),  // Azure
        new Color(0.9f, 0.3f, 0.3f, 1f),  // Crimson
        new Color(0.2f, 0.8f, 0.5f, 1f),  // Emerald
        new Color(0.9f, 0.7f, 0.2f, 1f)   // Gold
    };

    // Background to use for Threadmancer encounters
    public static final String COMBAT_BACKGROUND = "background/Phase_3.jpeg";

    // Music to use for Threadmancer encounters
    public static final String COMBAT_MUSIC = "music/mus_boss3.mp3";

    public Threadmancer() {
        super("Threadmancer", BASE_HP,
                new Texture(Gdx.files.internal("enemy/Threadmancer.png")),
                320f, 320f);

        GameLogger.logInfo("Creating Threadmancer boss for stage 30");

        // Initialize components
        this.player = Player.loadFromFile();
        this.threadAnimationTimer = 0f;

        // Add all patterns to the manager
        this.addAttackPattern(new ThreadCagePattern());     // Pattern 1: Creates enclosing thread cages
        this.addAttackPattern(new PhantomNeedlePattern());  // Pattern 2: Needles that appear with telegraphing
        this.addAttackPattern(new WeavingMatrixPattern()); // Pattern 3: Complex grid-like pattern that weaves
        // Initialize remaining properties
        initializeThreadmancer();
    }

    private void initializeThreadmancer() {
        // Set base rewards
        this.setBaseRewards(BASE_XP, BASE_GOLD);
        this.attackDamage = BASE_ATTACK;

        // Set primary color (initial thread color)
        this.primaryColor = THREAD_COLORS[0];

        // Scale to player level
        scaleToPlayerLevel(player.getLevel());

        // Initialize dialogues
        initializeDialogues();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update thread animation
        threadAnimationTimer += delta * THREAD_ANIMATION_SPEED;
        if (threadAnimationTimer > MathUtils.PI2) {
            threadAnimationTimer -= MathUtils.PI2;
        }

        // Update thread pulsation (smooth sine wave)
        threadPulsation = MathUtils.sin(threadAnimationTimer);

        // Calculate thread opacity based on pulsation
        threadOpacity = MIN_THREAD_OPACITY + (MAX_THREAD_OPACITY - MIN_THREAD_OPACITY) *
                        (0.5f + 0.5f * threadPulsation);

        // Update color transition
        colorTransitionProgress += delta * COLOR_TRANSITION_SPEED;
        if (colorTransitionProgress >= 1.0f) {
            // Move to next color
            currentThreadColor = targetThreadColor;
            targetThreadColor = (targetThreadColor + 1) % THREAD_COLORS.length;
            colorTransitionProgress = 0f;
        }

        // Interpolate between current and target colors
        this.primaryColor = new Color(
            THREAD_COLORS[currentThreadColor].r + (THREAD_COLORS[targetThreadColor].r - THREAD_COLORS[currentThreadColor].r) * colorTransitionProgress,
            THREAD_COLORS[currentThreadColor].g + (THREAD_COLORS[targetThreadColor].g - THREAD_COLORS[currentThreadColor].g) * colorTransitionProgress,
            THREAD_COLORS[currentThreadColor].b + (THREAD_COLORS[targetThreadColor].b - THREAD_COLORS[currentThreadColor].b) * colorTransitionProgress,
            1f
        );
    }

    private void initializeDialogues() {
        // Set encounter dialogue
        this.encounterDialogue = "The Threadmancer emerges from an intricate tapestry of reality!";

        // Set attack dialogues
        this.attackDialogue = "The Threadmancer weaves a deadly pattern of prismatic threads!";

        // Clear and add random turn dialogues
        this.clearRandomTurnDialogues();
        this.addRandomTurnDialogue("Threads of possibility dance around the Threadmancer's form.");
        this.addRandomTurnDialogue("The air shimmers as invisible needles prepare to strike.");
        this.addRandomTurnDialogue("The Threadmancer's fingers trace hypnotic patterns in the air.");
        this.addRandomTurnDialogue("Reality bends and frays at the edges around the Threadmancer.");
        this.addRandomTurnDialogue("You feel the pull of countless invisible threads around you.");

        // Set defeat and victory dialogues
        this.defeatDialogue = "The Threadmancer unravels into countless shimmering strands...";
        this.victoryDialogue = "The Threadmancer binds your fate in an unbreakable tapestry of defeat!";
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

        // Draw thread-like visual effects
        drawThreadEffects(batch, drawX, y);

        // Reset batch color
        batch.setColor(originalColor);
    }

    /**
     * Draws thread-like visual effects around the Threadmancer
     */
    private void drawThreadEffects(SpriteBatch batch, float x, float y) {
        // Create a thread color with pulsing alpha
        Color threadColor = new Color(primaryColor.r, primaryColor.g, primaryColor.b, threadOpacity * 0.7f);
        batch.setColor(threadColor);

        // Draw outer thread aura (slightly larger sprite)
        float threadScale = 1.12f + 0.04f * threadPulsation;
        float threadWidth = width * threadScale;
        float threadHeight = height * threadScale;
        float threadX = x - (threadWidth - width) / 2;
        float threadY = y - (threadHeight - height) / 2;

        batch.draw(texture, threadX, threadY, threadWidth, threadHeight);

        // Draw inner brighter thread aura
        threadColor = new Color(primaryColor.r, primaryColor.g, primaryColor.b, threadOpacity * 0.9f);
        batch.setColor(threadColor);

        threadScale = 1.06f + 0.03f * threadPulsation;
        threadWidth = width * threadScale;
        threadHeight = height * threadScale;
        threadX = x - (threadWidth - width) / 2;
        threadY = y - (threadHeight - height) / 2;

        batch.draw(texture, threadX, threadY, threadWidth, threadHeight);
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
