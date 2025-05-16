package swu.cp112.silkblade.screen;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.files.FileHandle;

import swu.cp112.silkblade.core.Main;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.combat.DamageNumber;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.entity.enemy.silkgod.DemoEnemy;
import swu.cp112.silkblade.screen.transition.ScreenTransition;
import swu.cp112.silkblade.util.GameLogger;
import swu.cp112.silkblade.entity.combat.BulletTextures;
import swu.cp112.silkblade.entity.combat.BuffManager;
import swu.cp112.silkblade.entity.item.ConsumableItem;
import swu.cp112.silkblade.entity.item.ItemEffectSystem;
import swu.cp112.silkblade.screen.OptionsScreen;

public class CombatScene implements Screen {
    // =================== Constants ===================
    private static final float PLAYER_SIZE = 18f;
    private static final float HP_BAR_WIDTH = 50f;
    private static final float HP_TRANSITION_SPEED = 0.05f;
    private static final float ARENA_MARGIN = 2f;
    private static final float ARENA_TRANSITION_SPEED = 12f;
    private static final float BUTTON_WIDTH = 190f;
    private static final float BUTTON_HEIGHT = 50f;
    private static final float BUTTON_MARGIN = 10f;
    private static final float ARENA_DEFAULT_WIDTH = (BUTTON_WIDTH * 4 + 10f * 3);
    private static final float ARENA_DEFAULT_HEIGHT = 200f;
    private static final float LETTER_DELAY = 0.05f;
    private static final float PUNCTUATION_DELAY = 0.8f; // Additional delay for punctuation marks
    // Add a grace period before bullets start spawning
    private static final float COMBAT_START_GRACE_PERIOD = 0.2f;

    // Text speed constants
    private static final float TEXT_SPEED_FAST = 0.02f;
    private static final float TEXT_SPEED_NORMAL = LETTER_DELAY; // 0.05f
    private static final float TEXT_SPEED_SLOW = 0.1f;
    private static final float TEXT_SPEED_VERY_SLOW = 0.15f;

    private static final float IMMUNITY_DURATION = 0.85f;
    private static final float BLINK_INTERVAL = 0.1f;
    private static final float SHAKE_DURATION = 0.3f;
    private static final float SHAKE_INTENSITY = 5.0f;
    private static final float ATTACK_SEQUENCE_DELAY = 1.25f;
    private static final float BASE_HP_TRANSITION_SPEED = 0.05f;
    private static final float MAX_HP_TRANSITION_SPEED = 1.0f;
    private static final float DAMAGE_SCALING_FACTOR = 0.01f;
    private static final float HEAL_SCALING_FACTOR = 0.015f; // Healing slightly slower than damage
    private static final float MIN_HP_TRANSITION_SPEED = 0.02f; // For very small changes
    private static final float HP_BAR_LINGER_TIME = 1.5f;
    private static final float DEFEATED_ENEMY_LINGER_TIME = 1.5f;
    private static final float END_COMBAT_DELAY = 0f;
    private static final float DEFAULT_END_COMBAT_PHASE_DELAY = 2.0f; // Default time to wait after all bullets are fired before ending combat phase

    // Death Defiance constants
    private static final float DEATH_DEFIANCE_DURATION = 5f;
    private static final int DEATH_DEFIANCE_DEFENSE_BOOST = 999999;
    private static final float RAINBOW_CYCLE_SPEED = 0.5f; // Speed of color cycling

    private static final int[] DIALOGUE_SKIP_KEYS = {
            Input.Keys.Z,
            Input.Keys.ENTER,
            Input.Keys.SPACE
    };
    private static final float COMBAT_PHASE_ALPHA = 0.4f; // Adjust this value to control dimming amount
    private static final float STAT_TEXT_SPACING = 10f; // Spacing between stats
    private static final int TRAIL_LENGTH = 28;
    private static final float TRAIL_ALPHA = 0.3f;
    private static final float MIN_TRAIL_DISTANCE = 8f;
    private static final float GLOW_SIZE_MULTIPLIER = 1.8f;
    private static final float GLOW_ALPHA_DECAY = 0.3f;

    // Item menu constants
    private static final int ITEMS_PER_ROW = 2;
    private static final int ITEMS_PER_PAGE = 4;
    private static final float ITEM_MENU_PADDING = 40f;
    private static final float ITEM_SPACING = 40f;
    private static final float PAGE_INDICATOR_PADDING = 10f;

    // Damage number constants
    private static final float DAMAGE_NUMBER_ENEMY_Y_OFFSET = 50f;
    private static final float DAMAGE_NUMBER_PLAYER_Y_OFFSET = 20f;

    // Add constants for skill window similar to item window
    private static final int SKILLS_PER_ROW = 1;
    private static final int SKILLS_PER_PAGE = 2;
    private static final float SKILL_MENU_PADDING = 40f;
    private static final float SKILL_SPACING = 40f;

    // =================== Core Game Objects ===================
    private final Game game;
    private final FitViewport viewport;
    private final OrthographicCamera camera;
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;

    // =================== Audio Resources ===================
    private Music backgroundMusic;
    private final Sound typingSound;
    private final Sound selectSound;
    private final Sound attackSound;
    private final Sound escapeSound;
    private final Sound hurtSound;
    private final Sound healSound;
    private final Sound levelUpSound;
    private final Sound manaRegenSound;
    private final Sound explosionSound;
    private final Sound deathExplosionSound;
    private final Sound deathDefianceSound; // Added Death Defiance sound effect

    // =================== Player State ===================
    private final Texture playerTexture;
    private final Rectangle playerHitbox;
    private final Sprite playerSprite;
    private final Player player;
    private float currentHPWidth;
    private float targetHPWidth;
    private float currentHPText;
    private float targetHPText;
    private float currentMPText;
    private float targetMPText;
    private float hpBarLingerTimer = 0f;
    private boolean isImmune = false;
    private float immunityTimer = 0;
    private float blinkTimer = 0;
    private boolean isVisible = true;
    private boolean didLevelUp = false;
    private float playerX;
    private float playerY;

    // =================== Enemy State ===================
    private Enemy currentEnemy;
    private float currentEnemyHPWidth;
    private float targetEnemyHPWidth;
    private boolean showDefeatedEnemy = false;
    private float defeatedEnemyLingerTimer = 0f;
    // Add enemy explosion animation
    private boolean showEnemyExplosion = false;
    private float enemyExplosionTimer = 0f;
    private TextureAtlas explosionAtlas;
    private Animation<TextureRegion> explosionAnimation;
    private static final float EXPLOSION_FRAME_DURATION = 0.05f;
    private float enemyDeathX;
    private float enemyDeathY;

    // =================== Combat State ===================
    private boolean playerTurn = true;
    private boolean enemyTurn = false;
    private boolean inCombat = false;
    private boolean combatActive = false;
    private boolean showHPAfterDamage = false;
    private float endCombatTimer = 0;
    private int selectedButton = -1;
    private final String[] buttonLabels = {"FIGHT", "SKILL", "ITEM", "RUN!"};
    private final Array<Bullet> bullets = new Array<>();
    private float bulletSpawnTimer = 0;
    private float bulletSpawnInterval = 0.5f;
    private int maxBullets = 20;
    private int bulletsSpawned = 0;
    private boolean inAttackSequence = false;
    private float attackSequenceTimer = 0;
    private boolean delayedCombatPending = false;
    private float delayedCombatTimer = 0;
    private boolean allBulletsFired = false;
    private float endCombatPhaseTimer = 0;
    // Add a timer for the grace period
    private float combatStartGraceTimer = 0;

    // =================== Item Menu State ===================
    private boolean showingItemMenu = false;
    private int selectedItemIndex = 0;
    private int currentItemPage = 0;
    private int totalItemPages = 1;
    // Map to track temporary item usage during combat (not saved until combat is won)
    private java.util.Map<String, Integer> temporaryItemUsage = new java.util.HashMap<>();

    // =================== Skill Menu State ===================
    private boolean showingSkillMenu = false;
    private int selectedSkillIndex = 0;
    private int currentSkillPage = 0;
    private int totalSkillPages = 1;

    // =================== Camera Effects ===================
    private static class ShakeEffect {
        float time;
        float duration;
        float intensity;
    }
    private final List<ShakeEffect> activeShakes = new ArrayList<>();
    private float originalCameraX;
    private float originalCameraY;

    // =================== UI State ===================
    private Rectangle arena;
    private float dialogueArenaWidth;
    private float dialogueArenaHeight;
    private float targetArenaWidth;
    private float targetArenaHeight;
    private float currentArenaWidth;
    private float currentArenaHeight;
    private boolean isTransitioning = false;
    private float targetX;
    private float targetY;
    private int combatKeyPressCount = 0;
    private static float COMBAT_ARENA_WIDTH = (BUTTON_WIDTH * 4 + 10f * 3);
    private static float COMBAT_ARENA_HEIGHT = 250f;
    // =================== Dialogue State ===================
    private String fullDialogueText = "";
    private StringBuilder currentDisplayText = new StringBuilder();
    private float letterTimer = 0;
    private int currentLetterIndex = 0;
    private boolean isTyping = false;
    private boolean dialogueCompleted = false;
    private float currentTextSpeed;

    // =================== Death Defiance State ===================
    private boolean deathDefianceAvailable = true; // If the player can use Death Defiance in the current combat
    private boolean inDeathDefianceState = false; // If the player is currently in the Death Defiance state
    private float deathDefianceTimer = 0f; // Time left in Death Defiance state
    private float rainbowColorTime = 0f; // Timer for rainbow color effect
    private Color rainbowColor = new Color(1, 1, 1, 1); // Current rainbow color

    // =================== Additional State ===================
    private boolean pendingDefeatMessage = false;
    private String savedDefeatMessage = "";
    private final List<DamageNumber> damageNumbers = new ArrayList<>();

    // Add a static flag to track if we're returning from combat
    public static boolean returningFromCombat = false;

    // Add a field to track buff information
    private boolean buffsExpired = false;

    // Add a new field to track if the enemy has been exploded
    private boolean enemyExploded = false;

    // Add a field to track if we should show credits after the final boss fight
    private boolean showCreditsAfterDefeat = false;

    // Add a new instance variable to track whether player centering in combat has been applied
    private boolean initialCombatCenteringApplied = false;

    // Add a new field for enemy background
    private Texture enemyBackgroundTexture;
    // Flag to track if loading the background was attempted
    private boolean backgroundLoadAttempted = false;

    private boolean stageUnlocked = false;
    private String stageUnlockedMessage = "";

    // Add new fields for the blurred background
    private Texture blurredBackgroundTexture;
    private FrameBuffer blurFrameBuffer;
    private boolean backgroundBlurInitialized = false;
    private static final float BACKGROUND_BLUR_STRENGTH = 5.5f; // Adjustable blur strength

    // Add player snapshot fields to restore state on death/retreat
    private Player playerSnapshot;
    private boolean snapshotTaken = false;

    // =================== Constructor ===================
    public CombatScene(Game game) {
        this(game, new DemoEnemy());
    }

    public CombatScene(Game game, Enemy enemy) {
        try {
            GameLogger.logInfo("Initializing Combat Scene");
            this.game = game;
            this.currentEnemy = enemy;
            if (enemy instanceof AbstractEnemy) {
                ((AbstractEnemy) enemy).setCombatScene(this);
            }
            this.player = Player.loadFromFile();

            // Initialize viewport and camera first so they can be used for texture sizing
            viewport = Main.getViewport();
            camera = Main.getCamera();

            // Initialize rendering objects BEFORE loading background textures
            spriteBatch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            font = new BitmapFont(Gdx.files.internal("fonts/DTM.fnt"));
            font.setColor(Color.WHITE);
            font.getData().setScale(2);

            // Try loading the enemy's custom background if available
            if (enemy.getCombatBackground() != null) {
                try {
                    String backgroundPath = enemy.getCombatBackground();
                    this.enemyBackgroundTexture = new Texture(Gdx.files.internal(backgroundPath));
                    GameLogger.logInfo("Loaded enemy background: " + backgroundPath);
                    // Initialize blur effect AFTER spriteBatch is initialized
                    initializeBackgroundBlur();
                } catch (Exception e) {
                    GameLogger.logError("Failed to load enemy background", e);
                    this.enemyBackgroundTexture = null;
                }
            }
            this.backgroundLoadAttempted = true;

            // Ensure the player's BuffManager is initialized
            player.ensureBuffManagerExists();

            // Initialize default text speed
            this.currentTextSpeed = LETTER_DELAY;

            // Initialize arena Rectangle right after viewport and camera
            this.arena = new Rectangle(0, 0, ARENA_DEFAULT_WIDTH, ARENA_DEFAULT_HEIGHT);
            this.dialogueArenaWidth = ARENA_DEFAULT_WIDTH;
            this.dialogueArenaHeight = ARENA_DEFAULT_HEIGHT;
            this.currentArenaWidth = ARENA_DEFAULT_WIDTH;
            this.currentArenaHeight = ARENA_DEFAULT_HEIGHT;

            // Initialize player
            playerTexture = new Texture("player.png");
            playerSprite = new Sprite(playerTexture);
            playerSprite.setSize(PLAYER_SIZE, PLAYER_SIZE);
            playerHitbox = new Rectangle();
            playerHitbox.x = viewport.getWorldWidth() / 2;
            playerHitbox.y = viewport.getWorldHeight() / 2;
            playerHitbox.width = 18;
            playerHitbox.height = 18;

            // Initialize audio
            String musicPath = "music/mus_boss5.mp3"; // Default music
            if (enemy.getCombatMusic() != null) {
                try {
                    musicPath = enemy.getCombatMusic();
//                    GameLogger.logInfo("Loaded enemy music: " + musicPath);
                } catch (Exception e) {
                    GameLogger.logError("Failed to load enemy music", e);
                    musicPath = "music/mus_boss5.mp3"; // Fallback to default
                }
            }
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(musicPath));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(.15f);

            typingSound = Gdx.audio.newSound(Gdx.files.internal("sounds/typing_2.wav"));
            selectSound = Gdx.audio.newSound(Gdx.files.internal("sounds/select.wav"));
            attackSound = Gdx.audio.newSound(Gdx.files.internal("sounds/attack.wav"));
            escapeSound = Gdx.audio.newSound(Gdx.files.internal("sounds/escape.wav"));
            hurtSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hurt.wav"));
            healSound = Gdx.audio.newSound(Gdx.files.internal("sounds/heal.wav"));
            levelUpSound = Gdx.audio.newSound(Gdx.files.internal("sounds/level_up.wav"));
            manaRegenSound = Gdx.audio.newSound(Gdx.files.internal("sounds/mana_regen.wav"));
            explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.wav"));
            deathExplosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/death_explosion.wav"));
            deathDefianceSound = Gdx.audio.newSound(Gdx.files.internal("sounds/defiance.wav")); // Repurpose level up sound for now

            // Initialize HP values
            initializeHPValues();

            // Set bullet spawn interval based on enemy
            COMBAT_ARENA_WIDTH = currentEnemy.getArenaWidth();
            COMBAT_ARENA_HEIGHT = currentEnemy.getArenaHeight();
            bulletSpawnInterval = currentEnemy.getAttackInterval();
            maxBullets = currentEnemy.getMaxBullets();

            // Store default arena dimensions
            dialogueArenaWidth = ARENA_DEFAULT_WIDTH;
            dialogueArenaHeight = ARENA_DEFAULT_HEIGHT;

            // Initialize explosion animation
            explosionAtlas = new TextureAtlas(Gdx.files.internal("atlas/explosion_atlas.atlas"));
            explosionAnimation = new Animation<>(EXPLOSION_FRAME_DURATION, explosionAtlas.findRegions("explosion_frame"));

        } catch (Exception e) {
            GameLogger.logError("Failed to initialize Combat Scene with enemy", e);
            throw e;
        }
    }

    // =================== Screen Interface Methods ===================
    @Override
    public void show() {
        arena = new Rectangle(0, 0, ARENA_DEFAULT_WIDTH, ARENA_DEFAULT_HEIGHT);
        setDialogueText(currentEnemy.getEncounterDialogue());
        centerArena();
        selectedButton = -1;
        enemyExploded = false; // Reset the explosion flag

        // Take a snapshot of the player's state at the start of combat
        takePlayerSnapshot();

        // Center player when scene is first shown
        centerPlayer();

        // Attempt to load the background if not already loaded
        if (enemyBackgroundTexture == null && !backgroundLoadAttempted) {
            loadEnemyBackground();
        }
        // Force refresh of the background blur to ensure correct sizing even if already loaded
        else if (enemyBackgroundTexture != null) {
            initializeBackgroundBlur();
        }

        // Reset free skill cast availability for the new combat
        player.resetFreeSkillCast();
        GameLogger.logInfo("Free skill cast reset and available for new combat: " + player.hasFreeSkillCastAvailable());

        // Pause the global music and play our combat-specific music
        swu.cp112.silkblade.core.Main.pauseBackgroundMusic();
        backgroundMusic.play();
    }

    @Override
    public void render(float delta) {
        try {
            // Clear the screen
            ScreenUtils.clear(Color.BLACK);

            // Always enable blending at the start of rendering
            Gdx.gl.glEnable(GL20.GL_BLEND);
            // Set to normal blending as the default for this frame
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            // Update game state and render elements
            updateGameState(delta);
            renderGameElements();
            handleInput(delta);
            handleLogic();

            // Always reset to normal blending at the end of rendering
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        } catch (Exception e) {
            // On error, reset blend and try to recover
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            GameLogger.logError("Error in Combat Scene render", e);
            try {
                game.setScreen(new MainMenuScreen(game));
            } catch (Exception e2) {
                GameLogger.logError("Failed to recover from render error", e2);
                Gdx.app.exit();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
        centerArena();

        // Center player after resize if not in active combat
        if (!inCombat || !enemyTurn) {
            centerPlayer();
        }

        // Recreate blur effect after resize if background exists
        if (enemyBackgroundTexture != null) {
            // Need to force recreate the blur effect with the new dimensions
            // GameLogger.logInfo("Recreating background blur on resize: " + width + "x" + height);
            initializeBackgroundBlur();
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // Reset blend function when hiding the screen to ensure clean transitions
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Stop combat music but don't resume main music yet
        // (let the next screen handle resuming it)
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }

        // If we're leaving combat and the enemy wasn't defeated, restore player state
        // This covers cases where we exit without using proper menu options
        if (!currentEnemy.isDefeated()) {
            // Restore the player's pre-combat state
            restorePlayerFromSnapshot();
            GameLogger.logInfo("Combat exit detected - restored player state since combat wasn't completed");
        }

        // Set the flag to indicate we're returning from combat
        returningFromCombat = true;
    }

    @Override
    public void dispose() {
        try {
            // Make sure to reset the blend function to default to prevent issues in other screens
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            player.saveToFile();
            player.dispose();
            GameLogger.logInfo("Disposing Combat Scene");
            spriteBatch.dispose();
            font.dispose();
            playerTexture.dispose();
            backgroundMusic.dispose();
            typingSound.dispose();
            selectSound.dispose();
            attackSound.dispose();
            escapeSound.dispose();
            hurtSound.dispose();
            healSound.dispose();
            levelUpSound.dispose();
            manaRegenSound.dispose();
            explosionSound.dispose();
            deathExplosionSound.dispose();
            deathDefianceSound.dispose();
            shapeRenderer.dispose();

            // Dispose the enemy background texture if it was loaded
            if (enemyBackgroundTexture != null) {
                enemyBackgroundTexture.dispose();
                enemyBackgroundTexture = null;
            }

            // Dispose blur resources
            if (blurredBackgroundTexture != null) {
                blurredBackgroundTexture.dispose();
                blurredBackgroundTexture = null;
            }
            if (blurFrameBuffer != null) {
                blurFrameBuffer.dispose();
                blurFrameBuffer = null;
            }

            // Dispose the explosion atlas
            if (explosionAtlas != null) {
                explosionAtlas.dispose();
            }

            // Dispose the bullet textures
            BulletTextures.getInstance().dispose();
        } catch (Exception e) {
            GameLogger.logError("Error disposing Combat Scene", e);
        }
    }

    // =================== Game State Updates ===================
    private void updateGameState(float delta) {
        updateArenaSize(delta);
        updateHPBar();
        updateHPText();
        updateMPText();
        updateImmunity(delta);
        updateShake(delta);
        updateEnemyHPBar(delta);
        // Update damage numbers
        updateDamageNumbers(delta);
        // Update Death Defiance state if active
        if (inDeathDefianceState) {
            updateDeathDefianceState(delta);
        }
        // Only update linger timer after defeat message is shown
        if (showDefeatedEnemy && !pendingDefeatMessage) {
            defeatedEnemyLingerTimer += delta;
            if (defeatedEnemyLingerTimer >= DEFEATED_ENEMY_LINGER_TIME) {
                showDefeatedEnemy = false;
                // Start explosion animation when enemy disappears
                showEnemyExplosion = true;
                enemyExplosionTimer = 0f;

                // Store enemy position for explosion effect - center of enemy sprite
                float screenCenterX = viewport.getWorldWidth() / 2;
                // Store the center coordinates instead of top-left
                enemyDeathX = screenCenterX;
                enemyDeathY = arena.y + ARENA_DEFAULT_HEIGHT + 30 + currentEnemy.getHeight() / 2;

                // Play explosion sound and shake screen
                deathExplosionSound.play(0.4f);
                startShake(0.5f, 10.0f);
            }
        }
        if (inAttackSequence && !isTransitioning) {
            updateAttackSequence(delta);
        }
        if (delayedCombatPending) {
            updateDelayedCombat(delta);
        }

        if (inCombat) {
            currentEnemy.updatePlayerPosition(playerHitbox.x, playerHitbox.y);
            updateBullets(delta);
            if (!combatActive) {
                updateEndCombat(delta);
            }
        } else {
            // If there are still bullets left after combat ends, keep updating them
            if (bullets.size > 0) {
                updateBullets(delta);
            }
            updateDialogueText(delta);
        }

        // Add missing explosion animation update
        if (showEnemyExplosion) {
            enemyExplosionTimer += delta;
            if (enemyExplosionTimer >= explosionAnimation.getAnimationDuration()) {
                showEnemyExplosion = false;
            }
        }
    }

    // =================== Rendering Methods ===================
    private void renderGameElements() {
            // First, render the background (if custom background exists)
    // This draws only the background portion, not the arena box
    if (enemyBackgroundTexture != null) {
        // Get the latest viewport dimensions and camera position
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        // Reset the projection matrix with the latest camera settings
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Explicitly reset color to WHITE to prevent tinting from other rendering operations
        spriteBatch.setColor(Color.WHITE);

        // Draw the blurred background if available, otherwise fallback to normal
        if (backgroundBlurInitialized && blurredBackgroundTexture != null) {
            // Calculate position to ensure the background fills the entire viewport
            float x = camera.position.x - screenWidth/2;
            float y = camera.position.y - screenHeight/2;

            // Fix for flipped y-coordinate in frame buffer textures
            spriteBatch.draw(
                blurredBackgroundTexture,
                x, y,                   // Position at bottom-left corner of the viewport
                screenWidth, screenHeight,  // Size to draw
                0, 0,                   // Source texture coordinates
                blurredBackgroundTexture.getWidth(), blurredBackgroundTexture.getHeight(),
                false, true             // Flip y-coordinate to fix the upside-down issue
            );
        } else {
            // Calculate position to ensure the background fills the entire viewport
            float x = camera.position.x - screenWidth/2;
            float y = camera.position.y - screenHeight/2;

            // Position background to cover the whole screen
            spriteBatch.draw(
                enemyBackgroundTexture,
                x, y,  // Position at bottom-left corner of the viewport
                screenWidth, screenHeight  // Scale to full screen size
            );
        }

        spriteBatch.end();
    }

        // Then render the enemy if applicable, so it appears BEHIND the combat box
        // Show enemy if:
        // 1. Not defeated OR
        // 2. Showing damage message OR
        // 3. Waiting for death message to be shown (pendingDefeatMessage) OR
        // 4. Not showing the explosion effect (meaning either it's still visible or hasn't been exploded yet)
        if ((!currentEnemy.isDefeated() || showHPAfterDamage || pendingDefeatMessage) && !showEnemyExplosion && !enemyExploded) {
            // Ensure enemy alpha is properly set when in combat mode
            if (inCombat && enemyTurn) {
                currentEnemy.setAlpha(COMBAT_PHASE_ALPHA); // Ensure proper alpha during combat
            }
            renderEnemy();
        }

        // Now render the combat box (arena) ON TOP of the enemy
        renderCombatBox();

        // Render enemy HP if applicable, after combat box to ensure it's visible
        if ((!currentEnemy.isDefeated() || showHPAfterDamage || pendingDefeatMessage) && !showEnemyExplosion && !enemyExploded) {
            if (showHPAfterDamage || pendingDefeatMessage) {
                renderEnemyHP();
            }
        }

        // Then render other UI elements
        renderHUD();

        if (inCombat) {
            if (!playerTurn && enemyTurn) {
                renderBullets();
                renderPlayer();
            }
        } else if (showingItemMenu) {
            renderItemMenu();
            // Render bullets on top of item menu if any exist after combat
            if (bullets.size > 0) {
                renderBullets();
            }
        } else if (showingSkillMenu) {
            renderSkillMenu();
            // Render bullets on top of skill menu if any exist after combat
            if (bullets.size > 0) {
                renderBullets();
            }
        } else {
            renderDialogueText();
            // Render bullets on top of dialogue if any exist after combat
            if (bullets.size > 0) {
                renderBullets();
            }
        }

        // Render damage numbers on top of everything
        renderDamageNumbers();

        // Render explosion effect if active
        if (showEnemyExplosion) {
            renderEnemyExplosion();
        }

        renderButtons();
    }

    // =================== Arena/Box Management Methods ===================

    private void centerArena() {
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        // Always center horizontally
        float centerX = (screenWidth - arena.width) / 2;

        // Position above buttons
        float buttonY = 20;
        float buttonHeight = BUTTON_HEIGHT;
        float marginAboveButtons = 60;
        float hudY = buttonY + buttonHeight + marginAboveButtons;

        // Set positions
        arena.x = centerX;
        arena.y = hudY;

        // When transitioning between sizes, update target position
        if (isTransitioning) {
            targetX = (screenWidth - targetArenaWidth) / 2;
            targetY = hudY;
        }

        // Log for debugging
        // GameLogger.logInfo("centerArena called - arena position set to: " + arena.x + ", " + arena.y);
    }

    // Modify updateArenaSize() to handle position transitions
    private void updateArenaSize(float delta) {
        if (!isTransitioning) return;

        // Smoothly transition width and height
        if (Math.abs(currentArenaWidth - targetArenaWidth) > 0.5f) {
            currentArenaWidth += (targetArenaWidth - currentArenaWidth) * ARENA_TRANSITION_SPEED * delta;
        } else {
            currentArenaWidth = targetArenaWidth;
        }

        if (Math.abs(currentArenaHeight - targetArenaHeight) > 0.5f) {
            currentArenaHeight += (targetArenaHeight - currentArenaHeight) * ARENA_TRANSITION_SPEED * delta;
        } else {
            currentArenaHeight = targetArenaHeight;
        }

        // Smoothly transition position
        if (Math.abs(arena.x - targetX) > 0.5f) {
            arena.x += (targetX - arena.x) * ARENA_TRANSITION_SPEED * delta;
        } else {
            arena.x = targetX;
        }

        if (Math.abs(arena.y - targetY) > 0.5f) {
            arena.y += (targetY - arena.y) * ARENA_TRANSITION_SPEED * delta;
        } else {
            arena.y = targetY;
        }

        // Update arena dimensions
        arena.width = currentArenaWidth;
        arena.height = currentArenaHeight;

        // During transition, update player position if we're going into combat
        // ONLY if initial centering hasn't been applied yet
        if (inCombat && !enemyTurn && !initialCombatCenteringApplied) {
            centerPlayer();
            initialCombatCenteringApplied = true;
        }

        // Check if transition is complete
        if (Math.abs(currentArenaWidth - targetArenaWidth) < 0.5f &&
                Math.abs(currentArenaHeight - targetArenaHeight) < 0.5f &&
                Math.abs(arena.x - targetX) < 0.5f &&
                Math.abs(arena.y - targetY) < 0.5f) {
            isTransitioning = false;

            // Center player in the arena after transition completes
            // For combat, ONLY center if initial centering hasn't been applied
            if (!initialCombatCenteringApplied && (inCombat || !enemyTurn)) {
                centerPlayer();
                initialCombatCenteringApplied = true;
            }
        }
    }

    // Modify setArenaSize to also handle position
    private void setArenaSize(float width, float height) {
        this.targetArenaWidth = width;
        this.targetArenaHeight = height;

        // Initialize current values if not transitioning yet
        if (!isTransitioning) {
            this.currentArenaWidth = arena.width;
            this.currentArenaHeight = arena.height;

            // Calculate the appropriate position for the new size
            float screenWidth = viewport.getWorldWidth();
            float screenHeight = viewport.getWorldHeight();

            // IMPORTANT: Always center horizontally
            targetX = (screenWidth - targetArenaWidth) / 2;

            float buttonY = 20;
            float buttonHeight = BUTTON_HEIGHT;
            float marginAboveButtons = 60;

            float hudY = buttonY + buttonHeight + marginAboveButtons;
            float centerY = (screenHeight - targetArenaHeight) / 2;

            // Always position above buttons for combat
            targetY = hudY;

            // Log for debugging
            // GameLogger.logInfo("setArenaSize called with " + width + "x" + height + " - target position: " + targetX + ", " + targetY);
        }

        this.isTransitioning = true;
    }

    private void renderCombatBox() {
        Gdx.gl.glLineWidth(5);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw filled black background for the arena
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(arena.x, arena.y, arena.width, arena.height);
        shapeRenderer.end();

        // Draw white outline
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(arena.x, arena.y, arena.width, arena.height);
        shapeRenderer.end();

        Gdx.gl.glLineWidth(4);
    }

    // =================== Combat State Management Methods ===================

    private void startAttackSequence() {
        inAttackSequence = true;
        // Start at 0 instead of negative
        centerArena();
        setDialogueText(player.useSkillMessage(player.getCurrentSkill()));
    }

    private void startDelayedCombat() {
        delayedCombatPending = true;
        delayedCombatTimer = 0;
    }

    private void updateDelayedCombat(float delta) {
        if (delayedCombatPending) {
            delayedCombatTimer += delta;

            boolean keyJustPressed = false;
            for(int key : DIALOGUE_SKIP_KEYS) {
                if (Gdx.input.isKeyJustPressed(key)) {
                    keyJustPressed = true;
                    break;
                }
            }

            if (keyJustPressed) {
                combatKeyPressCount++;
            }

            if (combatKeyPressCount >= 2) {
                delayedCombatPending = false;
                // Let the startCombat method handle setting showHPAfterDamage to false
                // so the HP bar is visible up until combat actually starts
                startCombat();
                combatKeyPressCount = 0;
            }
        } else {
            combatKeyPressCount = 0;
        }
    }

    public void startCombat() {
        inCombat = true;

        // Get arena dimensions from current pattern
        // IMPORTANT: We need to get the dimensions directly from the enemy's current pattern
        // This ensures dimensions are always up to date
        COMBAT_ARENA_WIDTH = currentEnemy.getArenaWidth();
        COMBAT_ARENA_HEIGHT = currentEnemy.getArenaHeight();

        // Save the arena dimensions before starting transition
        float newArenaWidth = COMBAT_ARENA_WIDTH;
        float newArenaHeight = COMBAT_ARENA_HEIGHT;

        // Reset the centering flag at the start of each combat
        initialCombatCenteringApplied = false;

        playerTurn = false;
        enemyTurn = true;

        // We don't update buffs here - buffs should only be decremented after surviving an enemy turn

        bullets.clear();
        bulletsSpawned = 0;
        combatActive = true;
        showHPAfterDamage = false;
        allBulletsFired = false;  // Reset the all bullets fired flag

        // Reset grace period timer when starting combat
        combatStartGraceTimer = 0;

        // Reset Death Defiance for the new combat
        inDeathDefianceState = false;
        rainbowColor.set(Color.WHITE);
        GameLogger.logInfo("Death Defiance reset and available for new combat");

        // Dim the enemy during combat
        currentEnemy.setAlpha(COMBAT_PHASE_ALPHA);

        // IMPORTANT: Let the enemy start their turn BEFORE getting interval/bullet values
        // This ensures the pattern is selected first
        if (currentEnemy != null) {
            currentEnemy.startTurn();
        }

        // Update these values from the enemy's CURRENT pattern (which may have just changed)
        bulletSpawnInterval = currentEnemy.getAttackInterval();
        maxBullets = currentEnemy.getMaxBullets();

        // Also update arena dimensions AGAIN in case the enemy changed patterns during startTurn
        COMBAT_ARENA_WIDTH = currentEnemy.getArenaWidth();
        COMBAT_ARENA_HEIGHT = currentEnemy.getArenaHeight();

        // If dimensions changed during startTurn, update arena transition
        setArenaSize(COMBAT_ARENA_WIDTH, COMBAT_ARENA_HEIGHT);

        // IMPORTANT: Explicitly center the arena AFTER setting its size
        // This ensures it's positioned correctly on screen
        centerArena();

        // Log centering for debugging
        // GameLogger.logInfo("Arena centered in startCombat - dimensions: " + COMBAT_ARENA_WIDTH + "x" + COMBAT_ARENA_HEIGHT);
        // GameLogger.logInfo("Arena position: x=" + arena.x + ", y=" + arena.y);

        // Calculate center position based on target dimensions
        // This ensures the player is centered correctly even if the arena transition isn't complete
        float screenWidth = viewport.getWorldWidth();
        float centerX = (screenWidth - COMBAT_ARENA_WIDTH) / 2 + (COMBAT_ARENA_WIDTH / 2) - (PLAYER_SIZE / 2);

        float buttonY = 20;
        float buttonHeight = BUTTON_HEIGHT;
        float marginAboveButtons = 60;
        float hudY = buttonY + buttonHeight + marginAboveButtons;

        float centerY = hudY + (COMBAT_ARENA_HEIGHT / 2) - (PLAYER_SIZE / 2);

        // Position player immediately at the center of what will be the arena
        playerSprite.setPosition(centerX, centerY);
        playerHitbox.x = centerX;
        playerHitbox.y = centerY;

        // Mark that initial centering has been applied
        initialCombatCenteringApplied = true;
    }

    // Add a new method to update arena size when pattern changes
    // This can be called by the enemy when it selects a new pattern
    public void updateArenaForPattern() {
        if (inCombat && enemyTurn) {
            // Only update if we're in active combat
            float newWidth = currentEnemy.getArenaWidth();
            float newHeight = currentEnemy.getArenaHeight();

            // Update stored arena dimensions
            COMBAT_ARENA_WIDTH = newWidth;
            COMBAT_ARENA_HEIGHT = newHeight;

            // Log before resizing
            // GameLogger.logInfo("Updating arena for pattern: " + newWidth + "x" + newHeight);

            // First trigger arena resize
            setArenaSize(newWidth, newHeight);

            // Then explicitly center the arena
            centerArena();

            // Log after centering
            // GameLogger.logInfo("Arena centered - position: " + arena.x + ", " + arena.y);

            // Recenter player if needed
            if (!combatActive) {
                centerPlayer();
            }
        }
    }

    public void endCombat() {
        inCombat = false;

        // Call endTurn on the enemy to ensure pattern rotation
        if (currentEnemy != null) {
            currentEnemy.endTurn();
        }

        combatActive = false;
        playerTurn = true;
        enemyTurn = false;
        showHPAfterDamage = false; // Explicitly reset HP display flag

        // THIS is the correct place to update buffs - after the player survives an enemy's turn
        // A buff with duration=2 will last through two complete combat cycles
        boolean buffsExpired = player.updateBuffs();
        if (buffsExpired) {
            GameLogger.logInfo("Some buffs expired after surviving enemy's turn");
        }

        // Reset the centering flag
        initialCombatCenteringApplied = false;

        // Reset arena size to dialogue dimensions
        COMBAT_ARENA_WIDTH = ARENA_DEFAULT_WIDTH;
        COMBAT_ARENA_HEIGHT = ARENA_DEFAULT_HEIGHT;
        setArenaSize(ARENA_DEFAULT_WIDTH, ARENA_DEFAULT_HEIGHT);

        // Turn off bullet spawning
        bulletsSpawned = maxBullets + 1;

        // Reset enemy
        if (currentEnemy != null) {
            // Make enemy fully visible again
            currentEnemy.setAlpha(1.0f);
        }

        // Handle post-combat effects, including temporary item usage
        applyItemUsage();

        // Add a delay before ending combat completely
        endCombatTimer = 0;
        bulletSpawnTimer = 0;
        if (pendingDefeatMessage) {
            showEnemyExplosion = true;
            enemyExplosionTimer = 0f;
            enemyDeathX = viewport.getWorldWidth() / 2 - 32; // Center explosion
            enemyDeathY = arena.y + ARENA_DEFAULT_HEIGHT + 30 + currentEnemy.getHeight() / 2 - 32;

            setDialogueText(savedDefeatMessage);
            pendingDefeatMessage = false;

            // Play death explosion sound
            deathExplosionSound.play(0.7f);
        }
    }
    private boolean playAudioOnce = true;

    private void updateAttackSequence(float delta) {
        if (inAttackSequence) {
            // Step 1: Wait for the dialogue to complete
            if (isTyping || !dialogueCompleted) {
                return; // Do nothing until dialogue is done
            }

            // Step 2: Wait for player input after dialogue completes
            if (playAudioOnce) {
                boolean inputReceived = false;
                for (int key : DIALOGUE_SKIP_KEYS) {
                    if (Gdx.input.isKeyJustPressed(key)) {
                        inputReceived = true;
                        break;
                    }
                }

                if (!inputReceived) {
                    return; // Don't proceed until the player presses a key
                }

                // Consume MP before playing sound
                player.consumeMPForSkill(player.getCurrentSkill());
                updateMPTargets();

                // Handle sounds differently based on skill type
                Player.SkillType currentSkill = player.getCurrentSkill();

                if (currentSkill == Player.SkillType.BASIC) {
                    // For basic slash, play attack sound (with double attack if equipped)
                    if (player.willPerformDoubleAttack(currentSkill)) {
                        // Play attack sound twice for double attack
                        attackSound.play(0.5f);
                        // Small delay between sounds
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                attackSound.play(0.5f);
                            }
                        }, 0.2f);
                    } else {
                        // Single attack sound
                        attackSound.play(0.5f);
                    }
                    // Also need to get sound duration for the timer
                    attackSequenceTimer = -player.getSkillDuration(currentSkill);
                } else {
                    // For other skills, only play the skill sound (not the attack sound)
                    // Double attack should not apply to skills (they're separate from the basic attack)
                    attackSequenceTimer = -player.playSkillSound();

                    // Double attack no longer applies to skill sounds - it's just for basic slash
                    // Double attack mechanics are only for basic attacks
                }

                playAudioOnce = false;
            }

            // Step 4: Increment timer
            attackSequenceTimer += delta;

            // Step 5: Once timer reaches threshold, execute attack
            if (attackSequenceTimer >= 1.2) {
                inAttackSequence = false;
                Player.DamageResult damageResult = player.calculateSkillDamage(player.getCurrentSkill());

                // Apply special skill effects before applying damage
                player.applySkillEffects(player.getCurrentSkill());

                // Check if the skill is a buffing skill (SKILL2 or SKILL4)
                Player.SkillType currentSkill = player.getCurrentSkill();
                boolean isBuffSkill = currentSkill == Player.SkillType.SKILL2 || currentSkill == Player.SkillType.SKILL4;

                // Only damage enemy if it's not a buffing skill
                if (!isBuffSkill) {
                    currentEnemy.damage(damageResult.damage, damageResult.isCritical);
                    startShake(0.3f, 8.0f);

                    // Create damage number display for enemy using the new method for better visibility
                    float screenCenterX = viewport.getWorldWidth() / 2;
                    float enemyX = screenCenterX;
                    float enemyY = arena.y + ARENA_DEFAULT_HEIGHT + 30 + DAMAGE_NUMBER_ENEMY_Y_OFFSET;
                    damageNumbers.add(DamageNumber.createEnemyDamage(damageResult.damage, enemyX, enemyY, damageResult.isCritical));
                }

                String attackMessage;
                if (isBuffSkill) {
                    // For buff skills, just use the skill message without damage details
                    attackMessage = player.getSkillMessage(player.getCurrentSkill(), 0, currentEnemy);
                } else if (damageResult.isCritical && damageResult.isDoubleAttack && currentSkill == Player.SkillType.BASIC) {
                    // Only basic attacks can have double attack text
                    attackMessage = "CRITICAL DOUBLE ATTACK! " + player.getSkillMessage(player.getCurrentSkill(), damageResult.damage, currentEnemy);
                } else if (damageResult.isCritical) {
                    attackMessage = "CRITICAL! " + player.getSkillMessage(player.getCurrentSkill(), damageResult.damage, currentEnemy);
                } else if (damageResult.isDoubleAttack && currentSkill == Player.SkillType.BASIC) {
                    // Only basic attacks can have double attack text
                    attackMessage = "DOUBLE ATTACK! " + player.getSkillMessage(player.getCurrentSkill(), damageResult.damage, currentEnemy);
                } else {
                    attackMessage = player.getSkillMessage(player.getCurrentSkill(), damageResult.damage, currentEnemy);
                }

                setDialogueText(attackMessage);

                // Only show HP bar and update HP targets for non-buff skills
                if (!isBuffSkill) {
                    showHPAfterDamage = true;
                    updateEnemyHPTargets();
                } else {
                    // For buff skills, don't show enemy HP bar
                    showHPAfterDamage = false;
                }

                updateHPTargets(); // Update player HP targets after buff/healing effects

                // Only update enemy HP if we actually damaged them
                if (!isBuffSkill) {
                    updateEnemyHPTargets();
                }

                if (!isBuffSkill && currentEnemy.isDefeated()) {
                    backgroundMusic.stop();
                    savedDefeatMessage = currentEnemy.getDefeatDialogue() + "\n" + currentEnemy.getRewardDialogue();
                    pendingDefeatMessage = true;
                    showDefeatedEnemy = true; // Add this line to enable explosion effect
                } else {
                    startDelayedCombat();
                }

                // We shouldn't update buffs here as we haven't completed a turn yet
                // Let buffs remain until the player survives the enemy's turn

                // Reset for the next attack
                playAudioOnce = true;
            }
        }
    }

    // =================== Player/Enemy Health Management Methods ===================

    public void decreaseHP(int damage) {
        // Skip damage entirely if in Death Defiance state
        if (inDeathDefianceState) {
            // Create a special "blocked" message instead of damage
            float damageNumberX = playerHitbox.x + playerHitbox.width / 2;
            float damageNumberY = playerHitbox.y + playerHitbox.height + DAMAGE_NUMBER_PLAYER_Y_OFFSET;
            DamageNumber blockedMsg = new DamageNumber(0, damageNumberX, damageNumberY, false, true);
            blockedMsg.setCustomText("BLOCKED!");
            blockedMsg.setColor(Color.GOLD);
            damageNumbers.add(blockedMsg);
            return;
        }

        if (!isImmune) {
            // Get current HP before damage for comparison
            float oldHP = player.getCurrentHP();

            // Apply damage
            player.takeDamage(damage);

            // Get new HP after damage
            float newHP = player.getCurrentHP();

            // Create damage number above player
            float damageNumberX = playerHitbox.x + playerHitbox.width / 2;
            float damageNumberY = playerHitbox.y + playerHitbox.height + DAMAGE_NUMBER_PLAYER_Y_OFFSET;
            damageNumbers.add(new DamageNumber(damage, damageNumberX, damageNumberY, false, false));

            // If HP was extremely high and damage didn't cause a visible change
            // force a visible reduction in the bar
            if (oldHP > player.getMaxHP() && Math.abs(oldHP - newHP) / oldHP < 0.01) {
                // Make sure we see at least a small visual change
                targetHPWidth = Math.max(targetHPWidth - 1.0f, 0f);
            }

            // Check if player would die and if Death Defiance is available
            if (player.isDead() && player.hasDeathDefiance() && deathDefianceAvailable) {
                // Additional safety check - if the player is already in Death Defiance state,
                // they shouldn't trigger it again
                if (inDeathDefianceState) {
                    GameLogger.logError("Attempted to activate Death Defiance while already in Death Defiance state!", null);
                    deathDefianceAvailable = false;
                } else {
                    // Log the Death Defiance activation for debugging
                    GameLogger.logInfo("Death Defiance activated: Player has " + player.getCurrentHP() + " HP");

                    // Activate Death Defiance instead of dying
                    activateDeathDefiance();
                }
            } else if (player.isDead()) {
                // Log why Death Defiance wasn't activated
                if (!player.hasDeathDefiance()) {
                    GameLogger.logInfo("Player doesn't have Death Defiance equipped - Instant Death");
                } else if (!deathDefianceAvailable) {
                    GameLogger.logInfo("Death Defiance already used this combat");
                }

                // Restore player state to pre-combat snapshot since player is dying
                restorePlayerFromSnapshot();

                // Reset blend function to normal before transitioning to game over screen
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                backgroundMusic.stop();

                // Get the player's current position for death animation
                float playerDeathX = playerHitbox.x;
                float playerDeathY = playerHitbox.y;

                // Transition to the game over screen with the death position
                game.setScreen(new GameOverScreen(game, playerDeathX, playerDeathY, currentEnemy));
                return; // Skip immunity and shake since we're leaving
            }

            startShake(0.3f, 8.0f);
            isImmune = true;
            immunityTimer = 0;
            isVisible = false;

            updateHPTargets();
        }
    }

    public void increaseHP(int healing) {
        int absHealing = Math.abs(healing); // Always make sure healing is positive
        player.heal(absHealing);  // Instead of direct HP manipulation

        // Create healing number (green) above player
        float healNumberX = playerHitbox.x + playerHitbox.width / 2;
        float healNumberY = playerHitbox.y + playerHitbox.height + DAMAGE_NUMBER_PLAYER_Y_OFFSET;
        damageNumbers.add(new DamageNumber(absHealing, healNumberX, healNumberY, true, false));

        updateHPTargets();
    }

    private void updateHPTargets() {
        // Calculate health as a percentage (0-100%), clamped between 0 and 1
        float hpPercentage = MathUtils.clamp(
            (player.getCurrentHP() / (float) player.getMaxHP()),
            0f,
            1f
        );

        // Set target width to percentage * 100
        targetHPWidth = hpPercentage * 100f;

        // For displaying the HP text, clamp it to the max HP for the visual display
        // This ensures that even with very large HP values, the display is reasonable
        targetHPText = Math.min(player.getCurrentHP(), player.getMaxHP());
    }

    private void updateMPTargets() {
        targetMPText = player.getMP();
    }

    private void updateHPBar() {
        float hpPercentage = player.getCurrentHP() / (float) player.getMaxHP();

        // Calculate how much HP changed as a percentage
        float hpChangePercentage = Math.abs(currentHPWidth - targetHPWidth) / 100f;

        // Different scaling for damage vs healing
        float scalingFactor = (currentHPWidth > targetHPWidth) ? DAMAGE_SCALING_FACTOR : HEAL_SCALING_FACTOR;
        float changeScale = Math.min(hpChangePercentage / scalingFactor, 1.0f);
        float speedMultiplier = 1.0f + (changeScale * 19.0f); // Scale from 1x to 20x

        // Combine with original dynamic speed calculation
        float baseSpeed = BASE_HP_TRANSITION_SPEED + (1 - hpPercentage) * 0.12f;
        float dynamicTransitionSpeed = Math.max(
            MIN_HP_TRANSITION_SPEED,
            Math.min(baseSpeed * speedMultiplier, MAX_HP_TRANSITION_SPEED)
        );

        if (currentHPWidth < targetHPWidth) {
            currentHPWidth += dynamicTransitionSpeed;
            if (currentHPWidth > targetHPWidth) currentHPWidth = targetHPWidth;
        } else if (currentHPWidth > targetHPWidth) {
            currentHPWidth -= dynamicTransitionSpeed;
            if (currentHPWidth < targetHPWidth) currentHPWidth = targetHPWidth;
        }
    }

    private void updateHPText() {
        float hpPercentage = player.getCurrentHP() / (float) player.getMaxHP();

        // Calculate how much HP changed
        float hpChangePercentage = Math.abs(currentHPText - targetHPText) / player.getMaxHP();

        // Determine if player is in critical health (below 20%)
        boolean isCritical = hpPercentage <= 0.2f;

        // Different scaling based on damage severity and critical state
        float changeScale = hpChangePercentage;
        float speedMultiplier;

        if (isCritical) {
            // If critical, make transition very fast
            speedMultiplier = 10.0f + (1 - hpPercentage) * 20.0f; // Can go up to 30x speed
        } else {
            // Non-critical scaling
            float damageScaleFactor = (currentHPText > targetHPText) ? DAMAGE_SCALING_FACTOR : HEAL_SCALING_FACTOR;
            speedMultiplier = 1.0f + (Math.min(changeScale / damageScaleFactor, 1.0f) * 19.0f);
        }

        // Base speed calculation
        float baseSpeed = BASE_HP_TRANSITION_SPEED + (1 - hpPercentage) * 0.12f;
        float dynamicTransitionSpeed = Math.max(
            MIN_HP_TRANSITION_SPEED,
            Math.min(baseSpeed * speedMultiplier, MAX_HP_TRANSITION_SPEED)
        );

        if (currentHPText < targetHPText) {
            currentHPText += dynamicTransitionSpeed;
            if (currentHPText > targetHPText) currentHPText = targetHPText;
        } else if (currentHPText > targetHPText) {
            currentHPText -= dynamicTransitionSpeed;
            if (currentHPText < targetHPText) currentHPText = targetHPText;
        }
    }

    private void updateMPText() {
        // Calculate the absolute amount of MP being changed
        float mpChange = Math.abs(currentMPText - targetMPText);

        // Use a fixed base speed that's faster than HP transitions
        float baseSpeed = BASE_HP_TRANSITION_SPEED * 4.0f;

        // Calculate speed based on the raw MP change amount
        float speedMultiplier = Math.min(mpChange / 10f, 5.0f); // Cap at 5x speed
        float dynamicTransitionSpeed = baseSpeed * (1.0f + speedMultiplier);

        // Ensure a minimum speed for large MP pools
        float minimumSpeed = Math.max(mpChange / 15f, 1.0f);
        dynamicTransitionSpeed = Math.max(dynamicTransitionSpeed, minimumSpeed);

        // Apply the transition
        if (currentMPText < targetMPText) {
            currentMPText += dynamicTransitionSpeed;
            if (currentMPText > targetMPText) currentMPText = targetMPText;
        } else if (currentMPText > targetMPText) {
            currentMPText -= dynamicTransitionSpeed;
            if (currentMPText < targetMPText) currentMPText = targetMPText;
        }
    }

    private void updateEnemyHPTargets() {
        if (currentEnemy != null) {
            targetEnemyHPWidth = (currentEnemy.getCurrentHP() / (float) currentEnemy.getMaxHP()) * 100f;
        }
    }

    private void updateEnemyHPBar(float delta) {
        if (currentEnemy == null) return;

        float hpPercentage = currentEnemy.getCurrentHP() / (float) currentEnemy.getMaxHP();

        // Calculate how much HP was lost as a percentage
        float hpLostPercentage = 1.0f - (currentEnemyHPWidth / 100f);

        // Scale transition speed based on damage taken
        // More damage = faster transition
        float damageScale = Math.min(hpLostPercentage / DAMAGE_SCALING_FACTOR, 1.0f);
        float speedMultiplier = 1.0f + (damageScale * 19.0f); // Scale from 1x to 20x

        // Combine with original dynamic speed calculation
        float baseSpeed = BASE_HP_TRANSITION_SPEED + (1 - hpPercentage) * 0.12f;
        float dynamicTransitionSpeed = Math.min(baseSpeed * speedMultiplier, MAX_HP_TRANSITION_SPEED);

        boolean wasAnimating = Math.abs(currentEnemyHPWidth - targetEnemyHPWidth) > 0.5f;

        if (currentEnemyHPWidth < targetEnemyHPWidth) {
            currentEnemyHPWidth += dynamicTransitionSpeed;
            if (currentEnemyHPWidth > targetEnemyHPWidth) {
                currentEnemyHPWidth = targetEnemyHPWidth;
            }
        } else if (currentEnemyHPWidth > targetEnemyHPWidth) {
            currentEnemyHPWidth -= dynamicTransitionSpeed;
            if (currentEnemyHPWidth < targetEnemyHPWidth) {
                currentEnemyHPWidth = targetEnemyHPWidth;
            }
        }

        // Animation has finished when we were animating before but now the difference is small
        boolean animationJustFinished = wasAnimating && Math.abs(currentEnemyHPWidth - targetEnemyHPWidth) <= 0.5f;

        // Start the linger timer when animation finishes ONLY for thorn damage (not when delayed combat is pending)
        if (animationJustFinished && showHPAfterDamage && !delayedCombatPending) {
            hpBarLingerTimer = 0f; // Reset and start counting
        }

        // Only hide HP bar after thorn damage animation when not in delayed combat phase
        // For player attacks, HP bar should stay visible until combat phase starts
        if (showHPAfterDamage && hpBarLingerTimer >= 0f && !delayedCombatPending) {
            hpBarLingerTimer += delta;
            if (hpBarLingerTimer >= 1.25f) { // 1.25 seconds after animation finishes
                showHPAfterDamage = false;
                hpBarLingerTimer = -1f; // Reset to inactive state
            }
        }
    }

    // =================== Dialogue System Methods ===================

    public void setDialogueText(String text) {
        setDialogueText(text, LETTER_DELAY);
    }

    public void setDialogueText(String text, float textSpeed) {
        this.fullDialogueText = text;
        this.currentDisplayText.setLength(0);
        this.currentLetterIndex = 0;
        this.isTyping = true;
        this.dialogueCompleted = false;
        this.currentTextSpeed = textSpeed;

        if (inCombat) {
            endCombat();
        }
    }

    private void updateDialogueText(float delta) {
        if (!isTyping) return;

        // Check for dialogue skip keys
        boolean skipPressed = false;
        for(int key : DIALOGUE_SKIP_KEYS) {
            if (Gdx.input.isKeyJustPressed(key)) {
                skipPressed = true;
                break;
            }
        }

        if (skipPressed && !currentEnemy.isDefeated()) {
            // Instantly complete the current dialogue
            currentDisplayText.setLength(0);
            currentDisplayText.append(fullDialogueText);
            currentLetterIndex = fullDialogueText.length();
            isTyping = false;
            dialogueCompleted = true;

            return;
        }

        letterTimer += delta;

        // Define the punctuation characters that should cause a pause
        boolean shouldPause = false;
        if (currentLetterIndex > 0 && currentLetterIndex < fullDialogueText.length()) {
            char currentChar = fullDialogueText.charAt(currentLetterIndex - 1);

            // Only pause for punctuation if:
            // 1. The current character is one of the pause-worthy punctuation marks
            // 2. AND either:
            //    a. It's the last character in the string, OR
            //    b. The next character is a space or newline (indicating end of sentence)
            if ((currentChar == '.' || currentChar == '!' || currentChar == '?')) {
                // Check if it's the last character or followed by a space/newline
                if (currentLetterIndex == fullDialogueText.length() ||
                    fullDialogueText.charAt(currentLetterIndex) == ' ' ||
                    fullDialogueText.charAt(currentLetterIndex) == '\n') {
                    shouldPause = true;
                }
            }
        }

        // Use a longer delay for punctuation
        float currentDelay = shouldPause ? PUNCTUATION_DELAY : currentTextSpeed;

        if (letterTimer >= currentDelay && currentLetterIndex < fullDialogueText.length()) {
            char nextChar = fullDialogueText.charAt(currentLetterIndex);
            currentDisplayText.append(nextChar);
            currentLetterIndex++;

            if (!Character.isWhitespace(nextChar)) {
                typingSound.play(0.2f);
            }

            letterTimer = 0;
        }

        if (currentLetterIndex >= fullDialogueText.length()) {
            isTyping = false;
            dialogueCompleted = true;
        }
    }

    private void renderDialogueText() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        float textX = arena.x + 20;
        float textY = arena.y + arena.height - 30;
        float maxY = arena.y + 20;

        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        GlyphLayout glyphLayout = new GlyphLayout();
        float maxWidth = arena.width - 40;

        // Split by newline characters to create separate lines
        String[] lines = currentDisplayText.toString().split("\n");
        float y = textY;

        for (String line : lines) {
            // Prepare the line with asterisk prefix
            StringBuilder formattedLine = new StringBuilder("*  ");
            formattedLine.append(line);

            // Calculate text width and handle wrapping
            glyphLayout.setText(font, formattedLine.toString());

            if (glyphLayout.width > maxWidth) {
                // Handle wrapping for lines that are too long
                String[] words = line.split(" ");
                StringBuilder currentLine = new StringBuilder("*  ");

                for (String word : words) {
                    String testLine = currentLine + word + " ";
                    glyphLayout.setText(font, testLine);

                    if (glyphLayout.width > maxWidth && !currentLine.toString().equals("*  ")) {
                        // Draw the current line and start a new one
                        if (y >= maxY) {
                            font.draw(spriteBatch, currentLine.toString(), textX, y);
                        }

                        y -= font.getLineHeight() + 5;
                        // Use indentation for wrapped lines
                        currentLine = new StringBuilder("     " + word + " ");

                        if (y < maxY) break;
                    } else {
                        currentLine.append(word).append(" ");
                    }
                }

                // Draw the last part of the wrapped line
                if (currentLine.length() > 0 && y >= maxY) {
                    font.draw(spriteBatch, currentLine.toString(), textX, y);
                }
            } else {
                // Draw the entire line if it fits
                if (y >= maxY) {
                    font.draw(spriteBatch, formattedLine.toString(), textX, y);
                }
            }

            // Move down for the next line
            y -= font.getLineHeight() + 5; // Increased spacing between main dialogue lines

            if (y < maxY) break;
        }

        // Draw "continue" indicator
        if (dialogueCompleted) {
            if ((System.currentTimeMillis() / 500) % 2 == 0) {
                font.draw(spriteBatch, "v", arena.x + arena.width - 50, arena.y + 50);
            }
        }

        spriteBatch.end();
        font.getData().setScale(2f);
    }

    // =================== Immunity & Effect Methods ===================

    private void updateImmunity(float delta) {
        if (isImmune) {
            immunityTimer += delta;

            blinkTimer += delta;
            if (blinkTimer >= BLINK_INTERVAL) {
                isVisible = !isVisible;
                blinkTimer = 0;
            }

            if (immunityTimer >= IMMUNITY_DURATION) {
                isImmune = false;
                isVisible = true;
            }
        }
    }

    public void startShake(float duration, float intensity) {
        ShakeEffect shake = new ShakeEffect();
        shake.time = 0;
        shake.duration = duration;
        shake.intensity = intensity;
        activeShakes.add(shake);

        // Store original camera position if this is the first shake
        if (activeShakes.size() == 1) {
            originalCameraX = camera.position.x;
            originalCameraY = camera.position.y;
        }
        camera.update();
    }

    private void updateShake(float delta) {
        if (!activeShakes.isEmpty()) {
            float totalOffsetX = 0;
            float totalOffsetY = 0;

            // Update all active shakes
            for (int i = activeShakes.size() - 1; i >= 0; i--) {
                ShakeEffect shake = activeShakes.get(i);
                shake.time += delta;

                if (shake.time < shake.duration) {
                    float progress = shake.time / shake.duration;
                    float decreaseFactor = 1.0f - progress;
                    float offsetX = MathUtils.random(-1.0f, 1.0f) * shake.intensity * decreaseFactor;
                    float offsetY = MathUtils.random(-1.0f, 1.0f) * shake.intensity * decreaseFactor;

                    totalOffsetX += offsetX;
                    totalOffsetY += offsetY;
                } else {
                    activeShakes.remove(i);
                }
            }

            // Apply combined shake effect
            camera.position.x = originalCameraX + totalOffsetX;
            camera.position.y = originalCameraY + totalOffsetY;
            camera.update();

            // Reset camera if no more shakes
            if (activeShakes.isEmpty()) {
                camera.position.x = originalCameraX;
                camera.position.y = originalCameraY;
                camera.update();
            }
        }
    }

    // =================== Bullet/Attack System Methods ===================

    private void spawnBullet() {
        float speed = MathUtils.random(100, 250);
        float size = MathUtils.random(8, 15);
        Color color = Color.WHITE;
        float dmg, x, y, vx, vy;
        boolean isHeal = false;
        int pattern = (bulletsSpawned / 5) % 4;

        switch (pattern) {
            case 0: // Top to bottom - standard bullets
                dmg = MathUtils.random(3, 7);
                x = MathUtils.random(arena.x + size, arena.x + arena.width - size);
                y = arena.y + arena.height;
                vx = MathUtils.random(-50, 50);
                vy = -speed;
                color = Color.WHITE;
                break;

            case 1: // Side to side - faster bullets with medium damage
                dmg = MathUtils.random(5, 10);
                x = bulletsSpawned % 2 == 0 ? arena.x : arena.x + arena.width;
                y = MathUtils.random(arena.y + size, arena.y + arena.height - size);
                vx = bulletsSpawned % 2 == 0 ? speed * 1.2f : -speed * 1.2f;
                vy = MathUtils.random(-50, 50);
                color = Color.YELLOW;
                break;

            case 2: // Diagonal - medium damage but harder to dodge
                dmg = MathUtils.random(7, 12);
                x = arena.x;
                y = arena.y;
                vx = speed * 0.9f;
                vy = speed * 0.9f;
                color = Color.ORANGE;
                break;

            case 3: // Spiral from center - highest damage (special attack)
                float centerX = arena.x + arena.width / 2;
                float centerY = arena.y + arena.height / 2;
                float angle = bulletsSpawned * 36 * MathUtils.degreesToRadians;
                int bulletInPattern = bulletsSpawned % 5;
                dmg = 8 + (bulletInPattern * 3);
                isHeal = true;
                x = centerX;
                y = centerY;
                vx = MathUtils.cos(angle) * speed;
                vy = MathUtils.sin(angle) * speed;
                color = Color.RED;
                break;

            default:
                dmg = 5;
                x = arena.x + arena.width / 2;
                y = arena.y + arena.height;
                vx = 0;
                vy = -speed;
        }

        bullets.add(new Bullet(dmg, x, y, vx, vy, size, color, isHeal));
    }

    private void updateBullets(float delta) {
        // Only spawn new bullets if we're in active combat
        if (inCombat && combatActive) {
            // Update grace period timer
            combatStartGraceTimer += delta;

            // Only start spawning bullets after grace period has elapsed
            if (combatStartGraceTimer >= COMBAT_START_GRACE_PERIOD) {
                bulletSpawnTimer += delta;
                if (bulletSpawnTimer >= bulletSpawnInterval && bulletsSpawned < maxBullets) {
                    List<Bullet> enemyBullets;

                    // Use the target dimensions during transition to ensure consistent bullet spawning
                    if (isTransitioning) {
                        // Use target dimensions and positions for accurate bullet spawning during transition
                        enemyBullets = currentEnemy.generateAttack(
                            targetX, targetY, targetArenaWidth, targetArenaHeight
                        );
                    } else {
                        // Use current arena dimensions once transition is complete
                        enemyBullets = currentEnemy.generateAttack(
                            arena.x, arena.y, arena.width, arena.height
                        );
                    }

                    if (enemyBullets != null && !enemyBullets.isEmpty()) {
                        bullets.addAll(enemyBullets.toArray(new Bullet[0]));
                        bulletsSpawned++;
                    }

                    bulletSpawnTimer = 0;
                }
            }

            // Check if all bullets have been fired
            if (bulletsSpawned >= maxBullets && !allBulletsFired) {
                allBulletsFired = true;
                endCombatPhaseTimer = 0;
            }

            // Special condition: End combat immediately if all bullets are gone and all have been fired
            if (allBulletsFired && bullets.size == 0) {
                combatActive = false;
                endCombatTimer = 0;
                return;
            }

            // If all bullets have been fired, start timer for ending combat phase
            if (allBulletsFired) {
                endCombatPhaseTimer += delta;

                // Get the end phase delay from the current enemy's attack pattern, or use default
                float currentEndPhaseDelay = DEFAULT_END_COMBAT_PHASE_DELAY;

                if (currentEnemy != null && currentEnemy.getCurrentPattern() != null) {
                    currentEndPhaseDelay = currentEnemy.getCurrentPattern().getConfig().getEndPhaseDelay();
                }

                // We still process bullets during the end phase countdown
                // Only mark combat as inactive once the timer is complete
                if (endCombatPhaseTimer >= currentEndPhaseDelay) {
                    combatActive = false;
                    endCombatTimer = 0;
                    return;
                }
            }
        }

        // Always update and check each bullet
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(delta, playerHitbox.x + PLAYER_SIZE / 2, playerHitbox.y + PLAYER_SIZE / 2);

            // Ensure explosion happens before removing bullet
            if (!bullet.isActive()) {
                List<Bullet> newBullets = bullet.getExplosionBullets();
                if (newBullets != null && !newBullets.isEmpty()) {
                    // Check if we're using a pattern that handles its own explosion sound
                    boolean shouldPlaySound = true;

                    // Get the current pattern
                    if (currentEnemy != null && currentEnemy.getCurrentPattern() != null) {
                        String patternName = currentEnemy.getCurrentPattern().getPatternName();
                        // Don't play explosion sound for FallenStarPattern (it has its own)
                        if (patternName.equals("Tears of Heaven")) {
                            shouldPlaySound = false;
                        }
                        // Add more patterns here as needed that handle their own explosion sounds
                    }

                    // Only play the sound if needed
                    if (shouldPlaySound) {
                        explosionSound.play(0.175f);
                    }

                    // Convert List to Array.items
                    for (Bullet newBullet : newBullets) {
                        bullets.add(newBullet);
                    }
                }
                bullets.removeIndex(i);
                continue;
            }

            // Check if bullet is offscreen
            if (isBulletOffScreen(bullet)) {
                if (bullet.hasExplosionTimer()) {  // Check if bullet is set to explode
                    continue;  // Let explosion run instead of removing it immediately
                }
                bullet.destroy();
                bullets.removeIndex(i);
                continue;
            }

            // Only check for player collision if we're still in active combat
            if (inCombat && combatActive &&
                bullet.getHitbox().overlaps(playerHitbox) &&
                (!bullet.isTelegraphing() || bullet.getTelegraphTimer() >= bullet.getTelegraphDuration()) &&
                !bullet.isFading()) {

                int damageValue = (int) bullet.getDamage();

                // Handle damage and sounds
                if (damageValue < 0) {
                    increaseHP(Math.abs(damageValue)); // Use Math.abs since increaseHP expects a positive value
                    healSound.setVolume(healSound.play(), 0.15f);
                    bullet.destroy();
                    bullets.removeIndex(i);
                } else if (damageValue > 0 && !isImmune) {
                    decreaseHP(damageValue);
                    hurtSound.setVolume(hurtSound.play(), 0.2f);

                    // If this bullet can explode, start its timer instead of destroying it
                    if (bullet.getOnExplodeCallback() != null) {
                        bullet.startExplosionTimer(0.5f); // Half second delay before explosion
                        continue; // Don't destroy the bullet, let it explode
                    }

                    // If it's not an explosive bullet, destroy it normally
                    bullet.destroy();
                    bullets.removeIndex(i);
                }
                continue;
            }
        }

        // Check for the special condition outside the combat check to handle any remaining bullets
        if (allBulletsFired && bullets.size == 0 && combatActive) {
            combatActive = false;
            endCombatTimer = 0;
        }
    }

    private boolean isBulletOffScreen(Bullet bullet) {
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        // Calculate the trail buffer based on bullet size and trail length
        float trailBuffer = bullet.getSize() * TRAIL_LENGTH;

        // Only consider the bullet completely off screen if both the bullet and its trail are off screen
        return bullet.getX() < -trailBuffer ||
               bullet.getX() > screenWidth + trailBuffer ||
               bullet.getY() < -trailBuffer ||
               bullet.getY() > screenHeight + trailBuffer;
    }

    private void renderBullets() {
        try {
            // Enable blending for transparency
            Gdx.gl.glEnable(GL20.GL_BLEND);

            // First render telegraph paths with normal blending
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            renderTelegraphedPaths();

            // Use SpriteBatch for rendering
            spriteBatch.setProjectionMatrix(camera.combined);

            // First pass: Render only non-glowing elements with normal blending
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            spriteBatch.begin();

            // Render non-glowing trails first
            for (Bullet bullet : bullets) {
                if (!bullet.isGlowing()) {
                    bullet.drawTrailWithSpriteBatch(spriteBatch);
                }
            }

            // Then render non-glowing bullets
            for (Bullet bullet : bullets) {
                if (!bullet.isGlowing()) {
                    bullet.drawWithSpriteBatch(spriteBatch);
                }
            }
            spriteBatch.end();

            // Second pass: Render only glowing elements with additive blending
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            spriteBatch.begin();

            // Render glowing trails first
            for (Bullet bullet : bullets) {
                if (bullet.isGlowing()) {
                    bullet.drawTrailWithSpriteBatch(spriteBatch);
                }
            }

            // Then render glowing bullets
            for (Bullet bullet : bullets) {
                if (bullet.isGlowing()) {
                    bullet.drawWithSpriteBatch(spriteBatch);
                }
            }

            // For an extra glow effect, render the glowing bullets again with slightly reduced size and opacity
            for (Bullet bullet : bullets) {
                if (bullet.isGlowing()) {
                    // This will create an extra "bloom" effect for glowing bullets
                    bullet.drawAdditionalGlowPass(spriteBatch);
                }
            }

            spriteBatch.end();

            // IMPORTANT: Reset blend func to normal
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        } catch (Exception e) {
            // In case of any rendering errors, make sure to restore normal blending
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            GameLogger.logError("Error rendering bullets", e);
        }
    }

    // Update the renderTelegraphedPaths method
    private void renderTelegraphedPaths() {
        // Need to begin ShapeRenderer for drawing paths
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // First pass: Draw filled telegraph paths (better representation of hitbox)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Bullet bullet : bullets) {
            if (bullet.isTelegraphing()) {
                float alpha = bullet.getTelegraphAlpha();
                if (alpha <= 0) continue;  // Skip if alpha is 0 or negative

                // Get the telegraph start and end points
                float[] startPoint = bullet.getTelegraphStartPoint();
                float[] endpoint = bullet.getRemainingTelegraphEndPoint();

                // Skip if either point is null or if the line would be too short to be visible
                if (startPoint == null || endpoint == null) continue;

                // Check if the line is at least partially within the viewport bounds
                boolean isVisible = isLineVisibleInViewport(startPoint[0], startPoint[1], endpoint[0], endpoint[1]);
                if (!isVisible) continue;

                // Get the actual hitbox of the bullet for accurate representation
                Rectangle hitbox = bullet.getHitbox();
                float hitboxWidth = hitbox.width;

                // Set color with low alpha for filled area
                Color telegraphColor = new Color();
                // Create faster flashing effect between soft blue and purple
                long currentTime = System.currentTimeMillis();
                boolean flashPhase = (currentTime / 120) % 2 == 0;
                // if (flashPhase) {
                //     telegraphColor.set(0.5f, 0.6f, 1.0f, alpha * 0.15f); // Lower alpha for filled area
                // } else {
                //     telegraphColor.set(0.8f, 0.5f, 1.0f, alpha * 0.15f); // Lower alpha for filled area
                // }
                if (flashPhase) {
                    telegraphColor.set(1.0f, 0.8f, 0.0f, alpha * 0.15f); // Yellow with lower alpha for filled area
                } else {
                    telegraphColor.set(1.0f, 0.5f, 0.0f, alpha * 0.15f); // Orange with lower alpha for filled area
                }

                // Calculate direction vector
                float dirX = endpoint[0] - startPoint[0];
                float dirY = endpoint[1] - startPoint[1];
                float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);

                if (length > 0) {
                    // Normalize direction
                    dirX /= length;
                    dirY /= length;

                    // Calculate perpendicular vector for rectangle width
                    float perpX = -dirY;
                    float perpY = dirX;

                    // Build rectangle vertices
                    float halfWidth = hitboxWidth / 2;
                    float[] verts = new float[8];

                    // Top-left
                    verts[0] = startPoint[0] + perpX * halfWidth;
                    verts[1] = startPoint[1] + perpY * halfWidth;

                    // Top-right
                    verts[2] = startPoint[0] - perpX * halfWidth;
                    verts[3] = startPoint[1] - perpY * halfWidth;

                    // Bottom-right
                    verts[4] = endpoint[0] - perpX * halfWidth;
                    verts[5] = endpoint[1] - perpY * halfWidth;

                    // Bottom-left
                    verts[6] = endpoint[0] + perpX * halfWidth;
                    verts[7] = endpoint[1] + perpY * halfWidth;

                    shapeRenderer.setColor(telegraphColor);
                    shapeRenderer.triangle(
                        verts[0], verts[1],
                        verts[2], verts[3],
                        verts[4], verts[5]
                    );
                    shapeRenderer.triangle(
                        verts[0], verts[1],
                        verts[4], verts[5],
                        verts[6], verts[7]
                    );
                }
            }
        }

        shapeRenderer.end();

        // Second pass: Draw outline for better visibility
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (Bullet bullet : bullets) {
            if (bullet.isTelegraphing()) {
                float alpha = bullet.getTelegraphAlpha();
                if (alpha <= 0) continue;  // Skip if alpha is 0 or negative

                // Get the telegraph start and end points
                float[] startPoint = bullet.getTelegraphStartPoint();
                float[] endpoint = bullet.getRemainingTelegraphEndPoint();

                // Skip if already checked in first pass
                if (startPoint == null || endpoint == null) continue;

                boolean isVisible = isLineVisibleInViewport(startPoint[0], startPoint[1], endpoint[0], endpoint[1]);
                if (!isVisible) continue;

                // Get the actual hitbox of the bullet for accurate representation
                Rectangle hitbox = bullet.getHitbox();
                float hitboxWidth = hitbox.width;

                // Set line width to match hitbox edge
                Gdx.gl.glLineWidth(3f); // Consistent outline width

                // Create faster flashing effect between soft blue and purple with higher alpha for outline
                Color telegraphColor = new Color();
                long currentTime = System.currentTimeMillis();
                boolean flashPhase = (currentTime / 120) % 2 == 0;

                // if (flashPhase) {
                //     telegraphColor.set(0.5f, 0.6f, 1.0f, alpha); // Full alpha for outline
                // } else {
                //     telegraphColor.set(0.8f, 0.5f, 1.0f, alpha); // Full alpha for outline
                // }
                if (flashPhase) {
                    telegraphColor.set(1.0f, 0.8f, 0.0f, alpha); // Yellow with full alpha for outline
                } else {
                    telegraphColor.set(1.0f, 0.5f, 0.0f, alpha); // Orange with full alpha for outline
                }
                // Calculate direction vector
                float dirX = endpoint[0] - startPoint[0];
                float dirY = endpoint[1] - startPoint[1];
                float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);

                if (length > 0) {
                    // Normalize direction
                    dirX /= length;
                    dirY /= length;

                    // Calculate perpendicular vector for rectangle width
                    float perpX = -dirY;
                    float perpY = dirX;

                    // Build rectangle vertices
                    float halfWidth = hitboxWidth / 2;
                    float[] verts = new float[8];

                    // Top-left
                    verts[0] = startPoint[0] + perpX * halfWidth;
                    verts[1] = startPoint[1] + perpY * halfWidth;

                    // Top-right
                    verts[2] = startPoint[0] - perpX * halfWidth;
                    verts[3] = startPoint[1] - perpY * halfWidth;

                    // Bottom-right
                    verts[4] = endpoint[0] - perpX * halfWidth;
                    verts[5] = endpoint[1] - perpY * halfWidth;

                    // Bottom-left
                    verts[6] = endpoint[0] + perpX * halfWidth;
                    verts[7] = endpoint[1] + perpY * halfWidth;

                    // Draw outline
                    shapeRenderer.setColor(telegraphColor);
                    shapeRenderer.line(verts[0], verts[1], verts[2], verts[3]); // Top
                    shapeRenderer.line(verts[2], verts[3], verts[4], verts[5]); // Right
                    shapeRenderer.line(verts[4], verts[5], verts[6], verts[7]); // Bottom
                    shapeRenderer.line(verts[6], verts[7], verts[0], verts[1]); // Left
                }
            }
        }

        // End the shape renderer
        shapeRenderer.end();

        // Reset line width to default
        Gdx.gl.glLineWidth(1f);
    }

    /**
     * Helper method to check if a line is at least partially visible in the viewport
     */
    private boolean isLineVisibleInViewport(float x1, float y1, float x2, float y2) {
        // Get viewport bounds
        float vpX = camera.position.x - viewport.getWorldWidth() / 2;
        float vpY = camera.position.y - viewport.getWorldHeight() / 2;
        float vpWidth = viewport.getWorldWidth();
        float vpHeight = viewport.getWorldHeight();

        // Simple check if either endpoint is inside viewport
        boolean p1Inside = (x1 >= vpX && x1 <= vpX + vpWidth && y1 >= vpY && y1 <= vpY + vpHeight);
        boolean p2Inside = (x2 >= vpX && x2 <= vpX + vpWidth && y2 >= vpY && y2 <= vpY + vpHeight);

        if (p1Inside || p2Inside) return true;

        // Line segment intersection with viewport borders
        // Check intersection with left edge
        if (lineIntersectsVertical(x1, y1, x2, y2, vpX, vpY, vpY + vpHeight)) return true;
        // Check intersection with right edge
        if (lineIntersectsVertical(x1, y1, x2, y2, vpX + vpWidth, vpY, vpY + vpHeight)) return true;
        // Check intersection with bottom edge
        if (lineIntersectsHorizontal(x1, y1, x2, y2, vpY, vpX, vpX + vpWidth)) return true;
        // Check intersection with top edge
        if (lineIntersectsHorizontal(x1, y1, x2, y2, vpY + vpHeight, vpX, vpX + vpWidth)) return true;

        return false;
    }

    /**
     * Check if line intersects with vertical line segment
     */
    private boolean lineIntersectsVertical(float x1, float y1, float x2, float y2, float vx, float vy1, float vy2) {
        if ((x1 <= vx && x2 >= vx) || (x1 >= vx && x2 <= vx)) {
            // Calculate the y-coordinate at the intersection
            float slope = (y2 - y1) / (x2 - x1);
            if (Float.isInfinite(slope)) return (Math.min(y1, y2) <= vy2 && Math.max(y1, y2) >= vy1);

            float y = y1 + (vx - x1) * slope;
            return (y >= vy1 && y <= vy2);
        }
        return false;
    }

    /**
     * Check if line intersects with horizontal line segment
     */
    private boolean lineIntersectsHorizontal(float x1, float y1, float x2, float y2, float hy, float hx1, float hx2) {
        if ((y1 <= hy && y2 >= hy) || (y1 >= hy && y2 <= hy)) {
            // Calculate the x-coordinate at the intersection
            float slope = (x2 - x1) / (y2 - y1);
            if (Float.isInfinite(slope)) return (Math.min(x1, x2) <= hx2 && Math.max(x1, x2) >= hx1);

            float x = x1 + (hy - y1) * slope;
            return (x >= hx1 && x <= hx2);
        }
        return false;
    }
    private float getHue(Color color) {
        float r = color.r;
        float g = color.g;
        float b = color.b;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));

        if (max == min) return 0;

        float hue;
        if (max == r) {
            hue = (g - b) / (max - min);
        } else if (max == g) {
            hue = 2f + (b - r) / (max - min);
        } else {
            hue = 4f + (r - g) / (max - min);
        }

        hue = hue / 6f;
        if (hue < 0) hue += 1;
        return hue;
    }

    private Color hueToRGB(float hue, float saturation, float value) {
        int h = (int)(hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h % 6) {
            case 0: return new Color(value, t, p, 1);
            case 1: return new Color(q, value, p, 1);
            case 2: return new Color(p, value, t, 1);
            case 3: return new Color(p, q, value, 1);
            case 4: return new Color(t, p, value, 1);
            default: return new Color(value, p, q, 1);
        }
    }

    // =================== UI Drawing Methods ===================

    private void renderHUD() {
        float startX = (viewport.getWorldWidth() - (BUTTON_WIDTH * 4 + BUTTON_MARGIN * 3)) / 2;
        float startY = 50;
        float playerNameY = startY + BUTTON_HEIGHT + BUTTON_MARGIN;

        // Draw HP bar background and foreground
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < buttonLabels.length; i++) {
            float x = startX + i * (BUTTON_WIDTH + BUTTON_MARGIN);

            if (i == 3) {
                // Draw red background
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.rect(x - 5, playerNameY - 23, HP_BAR_WIDTH, 25);

                // Draw yellow foreground with proper scaling - ensure the bar is capped at 100%
                shapeRenderer.setColor(Color.YELLOW);
                float hpRatio = MathUtils.clamp(currentHPWidth / 100f, 0f, 1f);
                shapeRenderer.rect(x - 5, playerNameY - 23, HP_BAR_WIDTH * hpRatio, 25);
            }
        }

        shapeRenderer.end();

        // Draw HUD text
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);

        for (int i = 0; i < buttonLabels.length; i++) {
            float x = startX + i * (BUTTON_WIDTH + BUTTON_MARGIN);

            switch (i) {
                case 0:
                    font.draw(spriteBatch, player.getName(), x, playerNameY);
                    // Remove cyan skill debug text
                    break;
                case 1:
                    // Draw LV text
                    font.draw(spriteBatch, "LV " + player.getLevel(), x, playerNameY);
                    break;
                case 2:
                    font.draw(spriteBatch, "MP " + (int) currentMPText + " / " + player.getMaxMP(),
                        x - 70, playerNameY);
                    break;
                case 3:
                    font.draw(spriteBatch, "HP", x - 45, playerNameY);

                    // Modified HP display logic
                    if (player.getCurrentHP() > player.getMaxHP()) {
                        // If HP exceeds max, show actual/max with a special indicator
                        font.setColor(Color.YELLOW);  // Use a special color
                        font.draw(spriteBatch, " " + player.getCurrentHP() + " / " + player.getMaxHP(),
                                 x + HP_BAR_WIDTH - 5, playerNameY);
                        font.setColor(Color.WHITE);  // Reset color
                    } else {
                        // Normal display for regular HP values
                        font.draw(spriteBatch, " " + (int) currentHPText + " / " + player.getMaxHP(),
                                 x + HP_BAR_WIDTH - 5, playerNameY);
                    }
                    break;
            }
        }

        spriteBatch.end();
        font.getData().setScale(2f);
    }

    private void renderButtons() {
        float screenWidth = viewport.getWorldWidth();
        float totalButtonsWidth = BUTTON_WIDTH * 4 + BUTTON_MARGIN * 3;
        float startX = (screenWidth - totalButtonsWidth) / 2;
        float startY = 20;

        // First, draw the solid black background for each button
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);

        for (int i = 0; i < buttonLabels.length; i++) {
            float x = startX + i * (BUTTON_WIDTH + BUTTON_MARGIN);
            shapeRenderer.rect(x, startY, BUTTON_WIDTH, BUTTON_HEIGHT);
        }

        shapeRenderer.end();

        // Draw button outlines
        Gdx.gl.glLineWidth(4);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int i = 0; i < buttonLabels.length; i++) {
            float x = startX + i * (BUTTON_WIDTH + BUTTON_MARGIN);

            // Check if enemy is defeated and don't show selected state if it is
            boolean showSelected = i == selectedButton && playerTurn && !inAttackSequence
                    && !delayedCombatPending && !currentEnemy.isDefeated();

            shapeRenderer.setColor(showSelected ? Color.YELLOW : Color.ORANGE);
            shapeRenderer.rect(x, startY, BUTTON_WIDTH, BUTTON_HEIGHT);
        }

        shapeRenderer.end();

        // Draw button text and heart indicator
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        for (int i = 0; i < buttonLabels.length; i++) {
            float x = startX + i * (BUTTON_WIDTH + BUTTON_MARGIN);
            float y = startY;
            float textHeight = font.getLineHeight() * font.getData().scaleY;

            // Use the same condition for selected state for text and heart indicator
            boolean showSelected = i == selectedButton && playerTurn && !inAttackSequence
                    && !delayedCombatPending && !currentEnemy.isDefeated();

            font.setColor(showSelected ? Color.YELLOW : Color.ORANGE);

            float heartWidth = 32f;
            float textWidth = buttonLabels[i].length() * font.getData().getGlyph('A').width * font.getData().scaleX;
            float textX = x + 15f;

            if (showSelected) {
                textX += heartWidth;
            }

            if (textX + textWidth > x + BUTTON_WIDTH - 5f) {
                textX = x + BUTTON_WIDTH - textWidth - 5f;
            }

            font.draw(spriteBatch, buttonLabels[i], textX, y + BUTTON_HEIGHT / 5 + textHeight / 4);

            if (showSelected) {
                float heartHeight = 28f;
                float heartX = x + BUTTON_WIDTH / 5 - heartWidth / 2;
                float heartY = y + BUTTON_HEIGHT / 2 - heartHeight / 2;
                spriteBatch.setColor(1, 0, 0, 1);
                spriteBatch.draw(playerTexture, heartX - 15, heartY, heartWidth, heartHeight);
            }
        }

        spriteBatch.end();
    }

    private void renderPlayer() {
        if (isVisible) {
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();

            // Save the original color to restore it later
            Color prevColor = spriteBatch.getColor().cpy();

            // Check if player is in Death Defiance state
            if (inDeathDefianceState) {
                // Set up blend function for additive blending to make colors pop
                spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

                // Draw a larger rainbow glow effect behind the player first
                float glowSize = PLAYER_SIZE * 2.0f;
                float glowX = playerSprite.getX() - (glowSize - PLAYER_SIZE) / 2;
                float glowY = playerSprite.getY() - (glowSize - PLAYER_SIZE) / 2;

                // Create intense rainbow glow
                Color glowColor = new Color(rainbowColor);
                glowColor.a = 0.8f;
                spriteBatch.setColor(glowColor);
                spriteBatch.draw(playerTexture, glowX, glowY, glowSize, glowSize);

                // Draw rainbow colored afterimages - more of them, further out
                for (int i = 1; i <= 6; i++) {
                    float offset = i * 1.5f;
                    float alpha = 0.4f - (i * 0.05f);

                    // Get a color from different part of rainbow wheel
                    float hueOffset = (i * 0.15f) % 1.0f;
                    Color afterimageColor = hsvToRgb((rainbowColorTime + hueOffset) % 1.0f, 1.0f, 1.0f);
                    afterimageColor.a = alpha;
                    spriteBatch.setColor(afterimageColor);

                    // Draw offset afterimages in 8 directions instead of 4 for fuller effect
                    float angle = (i % 8) * MathUtils.PI / 4; // 8 directions evenly spread
                    float offsetX = MathUtils.cos(angle) * offset;
                    float offsetY = MathUtils.sin(angle) * offset;

                    spriteBatch.draw(playerTexture,
                                    playerSprite.getX() + offsetX,
                                    playerSprite.getY() + offsetY,
                                    PLAYER_SIZE,
                                    PLAYER_SIZE);
                }

                // Draw the main player sprite with the current rainbow color
                // This replaces the white color that let the original red show through
                spriteBatch.setColor(rainbowColor);
                spriteBatch.draw(playerTexture, playerSprite.getX(), playerSprite.getY(), PLAYER_SIZE, PLAYER_SIZE);

                // Add one more pass with additive blending to make it really intense
                spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                Color intensityBoost = new Color(rainbowColor);
                intensityBoost.a = 0.4f;
                spriteBatch.setColor(intensityBoost);
                spriteBatch.draw(playerTexture, playerSprite.getX(), playerSprite.getY(), PLAYER_SIZE, PLAYER_SIZE);

                // Reset blend function
                spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                // Regular player color
                spriteBatch.setColor(1, 0, 0, 1); // Pure red for the heart
                spriteBatch.draw(playerTexture, playerSprite.getX(), playerSprite.getY(), PLAYER_SIZE, PLAYER_SIZE);
            }

            // Always properly restore the original color to prevent affecting other rendering
            spriteBatch.setColor(prevColor);

            // Always ensure blend function is reset to default to avoid affecting other rendering
            spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            spriteBatch.end();
        }
    }

    // =================== Input & Game Logic Methods ===================

    private void handleInput(float deltaTime) {
        // If transitioning, only allow camera shake control and not other input
        if (ScreenTransition.isTransitioning()) return;

        // Get player movement speed from settings, or use default if settings unavailable
        float movementSpeed = 300f; // Default fallback speed
        try {
            // Try to access the settings from the options screen
            OptionsScreen.GameSettings settings = loadGameSettings();
            if (settings != null) {
                movementSpeed = settings.playerMovementSpeed;
            }
        } catch (Exception e) {
            GameLogger.logError("Could not load movement speed from settings, using default", e);
        }

        // Skip or escape input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (inAttackSequence || delayedCombatPending) return;
            if (inCombat) return;
            if (currentEnemy.isDefeated()) return;

            if (showingItemMenu) {
                // Return from item menu to main combat UI
                showingItemMenu = false;
                selectedItemIndex = 0;
                currentItemPage = 0;
                selectSound.play(0.5f);
            } else if (showingSkillMenu) {
                // Return from skill menu to main combat UI
                showingSkillMenu = false;
                selectedSkillIndex = 0;
                currentSkillPage = 0;
                selectSound.play(0.5f);
            }
            return;
        }

        if (currentEnemy.isDefeated()) {
            handleDefeatedEnemyInput();
        } else if (inCombat) {
            handleActiveCombatInput(deltaTime, movementSpeed);
        } else if (showingItemMenu) {
            handleItemMenuInput();
        } else if (showingSkillMenu) {
            handleSkillMenuInput();
        } else {
            // If dialogue is still typing or incomplete, allow skipping but not menu selection
            if (isTyping || !dialogueCompleted) {
                handleActiveCombatInput(deltaTime, movementSpeed);
                for (int key : DIALOGUE_SKIP_KEYS) {
                    if (Gdx.input.isKeyJustPressed(key)) {
                        completeDialogue();
                        return;
                    }
                }
            } else {
                // Only handle menu input after dialogue is complete
                handleActiveCombatInput(deltaTime, movementSpeed);
            }
        }
    }

    private void handleDefeatedEnemyInput() {
        // Skip input handling if a transition is already in progress
        if (ScreenTransition.isTransitioning()) return;

        boolean skipKeyPressed = false;
        for(int key : DIALOGUE_SKIP_KEYS) {
            if (Gdx.input.isKeyJustPressed(key)) {
                skipKeyPressed = true;
                break;
            }
        }

        if (skipKeyPressed) {
            if (!dialogueCompleted) {
                completeDialogue();
            } else if (pendingDefeatMessage) {
                int oldLevel = player.getLevel();
                player.gainExp(currentEnemy.getExpReward());
                player.addGold(currentEnemy.getGoldReward());
                didLevelUp = player.getLevel() > oldLevel;

                // Initial defeat message without level up text or stage unlock text
                String defeatAndRewardText = currentEnemy.getDefeatDialogue() + "\n" + currentEnemy.getRewardDialogue();

                // Check if this stage completion will unlock a new stage
                int currentPlayerStage = player.getCurrentStage();
                // Get the current challenging stage from StageSelectionScreen
                int currentChallengingStage = StageSelectionScreen.getCurrentChallengingStage();

                // Only unlock next stage if player completed their highest currently unlocked stage
                // Special case for Stage 50 - don't unlock Stage 51, instead show credits
                int stageBeingChallenged = StageSelectionScreen.getCurrentChallengingStage();
                if (stageBeingChallenged == 50) {
                    stageUnlocked = false;
                    showCreditsAfterDefeat = true;
                    GameLogger.logInfo("Stage 50 boss defeated! Transitioning to credits after dialog...");
                }
                // For all other stages, follow normal unlocking logic
                else if (currentChallengingStage > 0 && currentChallengingStage == currentPlayerStage) {
                    // Unlock the next stage (current + 1)
                    int nextStage = currentPlayerStage + 1;
                    player.setCurrentStage(nextStage);
                    stageUnlockedMessage = "Stage " + nextStage + " unlocked!";
                    stageUnlocked = true;
                    GameLogger.logInfo("Player unlocked stage: " + nextStage + " after completing stage " + currentChallengingStage);
                } else {
                    stageUnlocked = false;
                    GameLogger.logInfo("No new stage unlocked. Completed stage " + currentChallengingStage +
                                      " but highest unlocked stage is " + currentPlayerStage);
                }

                // Check if the defeated enemy is a boss, and if so, mark it as defeated
                // A boss can be detected in two ways:
                // 1. Enemy name contains "BOSS"
                // 2. Current stage is a multiple of 10 (every 10th stage is a boss)
                boolean isBoss = false;
                int bossNumber = -1;

                // Check by name first
                if (currentEnemy.getName().contains("BOSS")) {
                    isBoss = true;
                    try {
                        // Extract boss number from name (assuming names like "BOSS 1" or "BOSS1")
                        String name = currentEnemy.getName();
                        if (name.contains("BOSS 1") || name.contains("BOSS1")) {
                            bossNumber = 1;
                        } else if (name.contains("BOSS 2") || name.contains("BOSS2")) {
                            bossNumber = 2;
                        } else if (name.contains("BOSS 3") || name.contains("BOSS3")) {
                            bossNumber = 3;
                        } else if (name.contains("BOSS 4") || name.contains("BOSS4")) {
                            bossNumber = 4;
                        } else if (name.contains("BOSS 5") || name.contains("BOSS5")) {
                            bossNumber = 5;
                        }
                    } catch (Exception e) {
                        GameLogger.logError("Error determining boss number from name: " + currentEnemy.getName(), e);
                    }
                }

                // If not identified by name, check by stage number
                // IMPORTANT: Use the currentChallengingStage instead of player.getCurrentStage()
                // because getCurrentStage might already be updated to the next stage
                int curStageBeingChallenged = StageSelectionScreen.getCurrentChallengingStage();
                if (!isBoss && curStageBeingChallenged % 10 == 0) {
                    isBoss = true;
                    // Calculate boss number based on stage (stage 10 = boss 1, stage 20 = boss 2, etc.)
                    bossNumber = curStageBeingChallenged / 10;
                    if (bossNumber > 5) bossNumber = 5; // Cap at 5 bosses total

                    // Log for debugging
                    GameLogger.logInfo("Stage " + curStageBeingChallenged + " recognized as boss stage with boss number " + bossNumber);
                }

                // Mark the appropriate boss as defeated
                if (isBoss && bossNumber > 0 && bossNumber <= 5) {
                    player.setBossDefeated(bossNumber, true);
                    GameLogger.logInfo("Boss " + bossNumber + " marked as defeated");
                }

                // Prepare post-combat messages (level up and/or stage unlock)
                StringBuilder postCombatMessage = new StringBuilder();

                // If both level up and stage unlock occurred, combine them
                if (didLevelUp && stageUnlocked) {
                    levelUpSound.play(0.15f);
                    player.fullRestore();
                    postCombatMessage.append("LEVEL UP! You are now level ").append(player.getLevel()).append("!\n");
                    postCombatMessage.append(stageUnlockedMessage);
                    // Clear individual flags since we're handling both together
                    didLevelUp = false;
                    stageUnlocked = false;
                    savedDefeatMessage = postCombatMessage.toString();
                }
                // If only level up occurred
                else if (didLevelUp) {
                    levelUpSound.play(0.15f);
                    player.fullRestore();
                    savedDefeatMessage = "LEVEL UP! You are now level " + player.getLevel() + "!";
                }
                // If only stage unlock occurred
                else if (stageUnlocked) {
                    savedDefeatMessage = stageUnlockedMessage;
                    stageUnlocked = false;
                }
                else {
                    // Neither level up nor stage unlock
                    savedDefeatMessage = "";
                }

                setDialogueText(defeatAndRewardText);
                showDefeatedEnemy = false;
                pendingDefeatMessage = false;

                // Trigger explosion effect now that the enemy is fully defeated and all messages shown
                showEnemyExplosion = true;
                enemyExplosionTimer = 0f;
                enemyExploded = true; // Add this line to mark enemy as exploded

                // Check if this was the stage 50 boss again
                if (curStageBeingChallenged == 50) {
                    showCreditsAfterDefeat = true;
                }

                // Store enemy position for explosion effect - center of enemy sprite
                float screenCenterX = viewport.getWorldWidth() / 2;
                // Store the center coordinates instead of top-left
                enemyDeathX = screenCenterX;
                enemyDeathY = arena.y + ARENA_DEFAULT_HEIGHT + 30 + currentEnemy.getHeight() / 2;

                // Play explosion sound and shake screen
                deathExplosionSound.play(0.4f);
                startShake(0.5f, 10.0f);

                updateHPTargets();
                updateMPTargets();
            } else if (!savedDefeatMessage.isEmpty()) {
                // Show the combined message (could be level up, stage unlock, or both)
                setDialogueText(savedDefeatMessage);
                savedDefeatMessage = "";
            } else {
                // Apply the temporary item usage since combat was successful
                applyItemUsage();

                // Clear the player snapshot since we won the combat
                clearPlayerSnapshot();

                // Reset blend function before transitioning
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                player.saveToFile();

                // Check if we should show credits (for stage 50 boss) or return to main menu
                if (showCreditsAfterDefeat) {
                    transitionToCreditsScreen();
                } else {
                    transitionToMainMenu(ScreenTransition.TransitionType.CROSS_FADE);
                }
            }
        }
    }

    private void handleActiveCombatInput(float deltaTime, float speed) {
        if (!playerTurn && enemyTurn) {
            handleMovementInput(deltaTime, speed);
        } else if (!inAttackSequence && !delayedCombatPending) {
//            if (!inCombat && selectedButton == 0 && Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
//                startCombat();
//            }
            handleMenuInput();
        }
    }

    private void handleMovementInput(float deltaTime, float speed) {
        // Get player movement speed from settings, or use default if settings unavailable
        float movementSpeed = speed; // Use the passed-in speed which should come from settings
        try {
            // Try to access the settings from the options screen
            OptionsScreen.GameSettings settings = loadGameSettings();
            if (settings != null) {
                movementSpeed = settings.playerMovementSpeed;
            }
        } catch (Exception e) {
            GameLogger.logError("Could not load movement speed from settings, using default", e);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerSprite.translateX(-movementSpeed * deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerSprite.translateX(movementSpeed * deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            playerSprite.translateY(movementSpeed * deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            playerSprite.translateY(-movementSpeed * deltaTime);
        }
    }

    private void handleMenuInput() {
        if (ScreenTransition.isTransitioning()) return;

        if (currentEnemy.isDefeated() || inAttackSequence) {
            selectedButton = -1;
            return;
        }

        // Remove the Q key cycling code
        // if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
        //     cycleSkill();
        //     player.playSkillSound();
        // }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedButton = (selectedButton - 1 + buttonLabels.length) % buttonLabels.length;
            selectSound.play();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedButton = (selectedButton + 1) % buttonLabels.length;
            selectSound.play();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && !isTransitioning) {
            selectButton();
        }
    }

    private void selectButton() {
        switch (selectedButton) {
            case 0: // FIGHT - Use basic slash directly
                // Set current skill to BASIC
                player.setCurrentSkill(Player.SkillType.BASIC);
                // Check if player has enough MP (should always be true for basic)
                if (player.hasEnoughMPForSkill(Player.SkillType.BASIC)) {
                    startAttackSequence();
                }
                break;
            case 1: // SKILL
                // Show skill menu
                showingSkillMenu = true;
                selectedSkillIndex = 0;
                currentSkillPage = 0;
                selectSound.play(0.5f);
                break;
            case 2: // ITEM
                // Show item menu
                showingItemMenu = true;
                selectedItemIndex = 0;
                currentItemPage = 0;
                selectSound.play(0.5f);
                break;
            case 3: // RUN
                 // Play sound with a slight delay
                transitionToMainMenu(ScreenTransition.TransitionType.CROSS_FADE);
                escapeSound.play(0.5f);
                break;
        }
    }

    private void handleLogic() {
        updatePlayerPosition();
    }

    private void updatePlayerPosition() {
        playerX = playerSprite.getX();
        playerY = playerSprite.getY();

        // If we're in combat and it's enemy's turn, allow player to move within arena bounds
        if (inCombat && enemyTurn) {
            // Use arena's current width and height for constraints
            float clampedX = MathUtils.clamp(
                playerX,
                arena.x + ARENA_MARGIN,
                arena.x + arena.width - PLAYER_SIZE - ARENA_MARGIN
            );

            float clampedY = MathUtils.clamp(
                playerY,
                arena.y + ARENA_MARGIN,
                arena.y + arena.height - PLAYER_SIZE - ARENA_MARGIN
            );

            playerSprite.setPosition(clampedX, clampedY);
            playerHitbox.x = clampedX;
            playerHitbox.y = clampedY;
        }
        // If not in combat or in player's turn, ensure player is centered
        // BUT only if we're not in the middle of active combat
        else if (!inCombat || (!enemyTurn && !initialCombatCenteringApplied)) {
            // Calculate the center of the arena
            float centerX = arena.x + (arena.width / 2) - (PLAYER_SIZE / 2);
            float centerY = arena.y + (arena.height / 2) - (PLAYER_SIZE / 2);

            playerSprite.setPosition(centerX, centerY);
            playerHitbox.x = centerX;
            playerHitbox.y = centerY;
        }
    }

    // =================== Initialization Methods ===================
    private void initializeHPValues() {
        currentHPWidth = MathUtils.clamp((player.getCurrentHP() / (float) player.getMaxHP()) * 100f, 0f, 100f);
        targetHPWidth = (player.getCurrentHP() / (float) player.getMaxHP()) * 100f;
        currentHPText = player.getCurrentHP();
        targetHPText = player.getCurrentHP();
        currentMPText = player.getMP();
        targetMPText = player.getMP();
        if (currentEnemy != null) {
            currentEnemyHPWidth = (currentEnemy.getCurrentHP() / (float) currentEnemy.getMaxHP()) * 100f;
            targetEnemyHPWidth = currentEnemyHPWidth;
        }
    }

    // =================== Rendering Methods ===================
    private void renderEnemy() {
        currentEnemy.update(Gdx.graphics.getDeltaTime());
        
        // Use viewport's world dimensions to calculate position
        float screenWidth = viewport.getWorldWidth();
        float enemyWidth = currentEnemy.getWidth();
        float enemyX = screenWidth / 2 - enemyWidth / 2;
        float enemyY = arena.y + ARENA_DEFAULT_HEIGHT + 30; // Position above the arena
        
        // Explicitly update the enemy's internal position to match the rendering position
        // This ensures getX() and getY() return coordinates that are consistent with the rendering
        currentEnemy.setPosition(enemyX + enemyWidth/2, enemyY + currentEnemy.getHeight()/2);

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Store the original batch color
        Color originalColor = spriteBatch.getColor().cpy();

        // Draw the enemy
        currentEnemy.draw(spriteBatch, enemyX, enemyY);

        // Restore the original color to prevent affecting other rendering
        spriteBatch.setColor(originalColor);

        spriteBatch.end();
    }

    private void renderEnemyHP() {
        if (currentEnemy == null) return;

        // Show HP bar if:
        // 1. Showing damage message OR
        // 2. Waiting for death message to be shown (pendingDefeatMessage)
        if (!showHPAfterDamage && !pendingDefeatMessage) return;

        float barWidth = 100f;
        float barHeight = 10f;
        float x = arena.x + (arena.width - barWidth) / 2;
        float y = arena.y + arena.height + 10;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, y, barWidth * (currentEnemyHPWidth / 100f), barHeight);
        shapeRenderer.end();
    }

    // =================== Utility Methods ===================
    private void completeDialogue() {
        currentDisplayText.setLength(0);
        currentDisplayText.append(fullDialogueText);
        currentLetterIndex = fullDialogueText.length();
        isTyping = false;
        dialogueCompleted = true;
    }

    private void updateEndCombat(float delta) {
        if (!combatActive && inCombat) {
            endCombatTimer += delta;
            if (endCombatTimer >= END_COMBAT_DELAY) {
                endCombat();
                if (playerTurn) {
                    setDialogueText(currentEnemy.getTurnPassDialogue());
                }
            }
        }
    }

    private void transitionToMainMenu(ScreenTransition.TransitionType type) {
        // Reset OpenGL blend function to normal before transitioning
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Restore player state to pre-combat snapshot since player is retreating
        restorePlayerFromSnapshot();

        // Clear the temporary item usage when running away
        // No items should be lost if the player doesn't win the battle
        temporaryItemUsage.clear();

        backgroundMusic.stop();

        // Set the returning from combat flag to true
        returningFromCombat = true;

        game.setScreen(new ScreenTransition(
            game,
            this,
            new StageSelectionScreen(game),
            type
        ));
    }

    /**
     * Renders the item menu with combat items in a grid layout.
     */
    private void renderItemMenu() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Get combat items from player inventory, but filter out any that are no longer available
        Array<ConsumableItem> allCombatItems = player.getInventory().getCombatItems();
        Array<ConsumableItem> combatItems = new Array<>();

        // Filter the combat items based on what's actually available
        for (ConsumableItem combatItem : allCombatItems) {
            // Check if this item exists in main inventory
            boolean itemExists = false;
            int availableQuantity = 0;

            for (ConsumableItem mainItem : player.getInventory().getConsumableItems()) {
                if (mainItem.getId().equals(combatItem.getId()) && mainItem.getTier().equals(combatItem.getTier())) {
                    itemExists = true;
                    availableQuantity = mainItem.getQuantity();
                    break;
                }
            }

            // Only show items that exist in main inventory and haven't been fully used this combat
            if (itemExists) {
                int usedCount = temporaryItemUsage.getOrDefault(combatItem.getId(), 0);
                if (usedCount < availableQuantity) {
                    combatItems.add(combatItem);
                }
            }
        }

        // Calculate total pages
        totalItemPages = (int) Math.ceil((float) combatItems.size / ITEMS_PER_PAGE);
        if (totalItemPages == 0) totalItemPages = 1;

        // Make sure current page is valid
        currentItemPage = Math.min(currentItemPage, totalItemPages - 1);

        // Calculate starting index for current page
        int startIdx = currentItemPage * ITEMS_PER_PAGE;
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, combatItems.size);
        int itemsOnCurrentPage = endIdx - startIdx;

        // Ensure selectedItemIndex is valid for the current page
        selectedItemIndex = Math.min(selectedItemIndex, itemsOnCurrentPage - 1);
        if (selectedItemIndex < 0 && itemsOnCurrentPage > 0) {
            selectedItemIndex = 0;
        }

        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        float itemX = arena.x + ITEM_MENU_PADDING;
        float itemY = arena.y + arena.height - ITEM_MENU_PADDING;
        float rowHeight = 50f;
        float maxItemWidth = (arena.width / 2) - ITEM_MENU_PADDING - 30f; // Leave space for quantity

        // Draw items in a grid
        for (int i = startIdx; i < endIdx; i++) {
            ConsumableItem item = combatItems.get(i);
            int gridPos = i - startIdx;
            int row = gridPos / ITEMS_PER_ROW;
            int col = gridPos % ITEMS_PER_ROW;

            float x = itemX + col * (arena.width / 2);
            float y = itemY - row * rowHeight;

            // Find how many are left after temporary usage
            int totalQuantity = 0;
            for (ConsumableItem mainItem : player.getInventory().getConsumableItems()) {
                if (mainItem.getId().equals(item.getId()) && mainItem.getTier().equals(item.getTier())) {
                    totalQuantity = mainItem.getQuantity();
                    break;
                }
            }
            int usedCount = temporaryItemUsage.getOrDefault(item.getId(), 0);
            int remaining = totalQuantity - usedCount;

            // Calculate if name will overflow
            String itemName = item.getName();
            String prefix = "* ";
            String quantity = " x" + remaining;

            // Check if item name will overflow
            GlyphLayout layout = new GlyphLayout(font, prefix + itemName);
            if (layout.width > maxItemWidth) {
                // Truncate name to fit
                int maxChars = 0;
                for (int j = 1; j <= itemName.length(); j++) {
                    GlyphLayout testLayout = new GlyphLayout(font, prefix + itemName.substring(0, j) + "...");
                    if (testLayout.width > maxItemWidth - font.getSpaceXadvance() * 3 /* space for ellipsis */) {
                        break;
                    }
                    maxChars = j;
                }

                if (maxChars > 0) {
                    itemName = itemName.substring(0, maxChars) + "...";
                }
            }

            // Draw item with proper spacing
            String itemText = prefix + itemName + quantity;
            font.setColor(gridPos == selectedItemIndex ? Color.YELLOW : Color.WHITE);
            font.draw(spriteBatch, itemText, x, y);
        }

        // Draw page indicator at bottom right (if multiple pages)
        if (totalItemPages > 1) {
            String pageText = "*PG " + (currentItemPage + 1) + "/" + totalItemPages + "*";
            GlyphLayout layout = new GlyphLayout(font, pageText);
            float pageX = arena.x + arena.width - layout.width - PAGE_INDICATOR_PADDING;
            float pageY = arena.y + PAGE_INDICATOR_PADDING + layout.height;
            font.getData().setScale(1.0f);
            font.setColor(Color.LIGHT_GRAY);
            font.draw(spriteBatch, pageText, pageX, pageY);
        }

        // If no items available
        if (combatItems.size == 0) {
            String noItemsText = "No items available";
            GlyphLayout layout = new GlyphLayout(font, noItemsText);
            float textX = arena.x + (arena.width - layout.width) / 2;
            float textY = arena.y + (arena.height + layout.height) / 2;
            font.setColor(Color.WHITE);
            font.draw(spriteBatch, noItemsText, textX, textY);
        }

        spriteBatch.end();
        font.getData().setScale(2.0f);
    }

    /**
     * Handles navigation and selection in the item menu.
     */
    private void handleItemMenuInput() {
        // Get filtered combat items
        Array<ConsumableItem> allCombatItems = player.getInventory().getCombatItems();
        Array<ConsumableItem> combatItems = new Array<>();

        // Filter the combat items
        for (ConsumableItem combatItem : allCombatItems) {
            boolean itemExists = false;
            int availableQuantity = 0;

            for (ConsumableItem mainItem : player.getInventory().getConsumableItems()) {
                if (mainItem.getId().equals(combatItem.getId()) && mainItem.getTier().equals(combatItem.getTier())) {
                    itemExists = true;
                    availableQuantity = mainItem.getQuantity();
                    break;
                }
            }

            if (itemExists) {
                int usedCount = temporaryItemUsage.getOrDefault(combatItem.getId(), 0);
                if (usedCount < availableQuantity) {
                    combatItems.add(combatItem);
                }
            }
        }

        // Calculate items on current page
        int startIdx = currentItemPage * ITEMS_PER_PAGE;
        int itemsOnCurrentPage = Math.min(ITEMS_PER_PAGE, combatItems.size - startIdx);

        if (itemsOnCurrentPage <= 0) {
            // No items, just handle escape
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                showingItemMenu = false;
                selectSound.play(0.5f);
            }
            return;
        }

        // Ensure selectedItemIndex is valid
        selectedItemIndex = Math.min(selectedItemIndex, itemsOnCurrentPage - 1);
        if (selectedItemIndex < 0) {
            selectedItemIndex = 0;
        }

        // Navigate between items
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (selectedItemIndex % ITEMS_PER_ROW < ITEMS_PER_ROW - 1 &&
                selectedItemIndex + 1 < itemsOnCurrentPage) {
                // Normal movement to the right within the page
                selectedItemIndex++;
                selectSound.play(0.5f);
            } else if (currentItemPage < totalItemPages - 1) {
                // At the rightmost column, go to next page
                currentItemPage++;
                selectedItemIndex = 0; // Reset to first item on new page for consistency
                selectSound.play(0.5f);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (selectedItemIndex % ITEMS_PER_ROW > 0) {
                // Normal movement to the left within the page
                selectedItemIndex--;
                selectSound.play(0.5f);
            } else if (currentItemPage > 0) {
                // At the leftmost column, go to previous page
                currentItemPage--;
                // Calculate items on the previous page
                int itemsOnPrevPage = Math.min(ITEMS_PER_PAGE,
                    combatItems.size - (currentItemPage * ITEMS_PER_PAGE));
                // Set index to last item on previous page
                selectedItemIndex = itemsOnPrevPage - 1;
                selectSound.play(0.5f);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            int newIndex = selectedItemIndex + ITEMS_PER_ROW;
            if (newIndex < itemsOnCurrentPage) {
                selectedItemIndex = newIndex;
                selectSound.play(0.5f);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (selectedItemIndex >= ITEMS_PER_ROW) {
                selectedItemIndex -= ITEMS_PER_ROW;
                selectSound.play(0.5f);
            }
        }

        // Keep PAGE_UP/PAGE_DOWN navigation for accessibility
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN) && currentItemPage < totalItemPages - 1) {
            currentItemPage++;
            selectedItemIndex = 0;
            selectSound.play(0.5f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP) && currentItemPage > 0) {
            currentItemPage--;
            selectedItemIndex = 0;
            selectSound.play(0.5f);
        }

        // Select item
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            useSelectedItem();
        }

        // Cancel
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            showingItemMenu = false;
            selectSound.play(0.5f);
        }
    }

    /**
     * Uses the currently selected item.
     */
    private void useSelectedItem() {
        // Get the filtered combat items first
        Array<ConsumableItem> allCombatItems = player.getInventory().getCombatItems();
        Array<ConsumableItem> combatItems = new Array<>();

        // Filter out items that aren't available
        for (ConsumableItem combatItem : allCombatItems) {
            boolean itemExists = false;
            int availableQuantity = 0;

            for (ConsumableItem mainItem : player.getInventory().getConsumableItems()) {
                if (mainItem.getId().equals(combatItem.getId()) && mainItem.getTier().equals(combatItem.getTier())) {
                    itemExists = true;
                    availableQuantity = mainItem.getQuantity();
                    break;
                }
            }

            if (itemExists) {
                int usedCount = temporaryItemUsage.getOrDefault(combatItem.getId(), 0);
                if (usedCount < availableQuantity) {
                    combatItems.add(combatItem);
                }
            }
        }

        // Calculate the actual index based on current page and selection
        int startIdx = currentItemPage * ITEMS_PER_PAGE;
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, combatItems.size);

        // Validate selected index
        if (selectedItemIndex < 0 || startIdx + selectedItemIndex >= combatItems.size) {
            return; // Invalid selection
        }

        // Get the selected item
        ConsumableItem combatItem = combatItems.get(startIdx + selectedItemIndex);

        // Find corresponding item in main inventory
        ConsumableItem mainInventoryItem = null;
        for (ConsumableItem item : player.getInventory().getConsumableItems()) {
            if (item.getId().equals(combatItem.getId()) && item.getTier().equals(combatItem.getTier())) {
                mainInventoryItem = item;
                break;
            }
        }

        // If the item no longer exists in the main inventory, don't allow using it
        if (mainInventoryItem == null) {
            setDialogueText("You don't have any " + combatItem.getName() + " left!");
            return;
        }

        // If we've already used all available of this item in this combat, don't allow using more
        int usedCount = temporaryItemUsage.getOrDefault(combatItem.getId(), 0);
        if (usedCount >= mainInventoryItem.getQuantity()) {
            setDialogueText("You don't have any more " + combatItem.getName() + " to use!");
            return;
        }

        // Clone the item to avoid modifying the original
        ConsumableItem itemToUse = mainInventoryItem.clone();

        // Apply item effect using the ItemEffectSystem, but don't actually consume from inventory yet
        // We only mark it as used in combat and will apply actual consumption when combat is won
        String effectMessage = "";
        StringBuilder buffMessage = new StringBuilder();

        // Record pre-effect values to calculate buffs applied
        int attackBefore = player.getAttack();
        int defenseBefore = player.getDefense();

        // Track temporary item usage
        String itemId = combatItem.getId();
        temporaryItemUsage.put(itemId, temporaryItemUsage.getOrDefault(itemId, 0) + 1);

        // Based on the item effect, play appropriate sounds and create message
        switch (itemToUse.getEffect()) {
            case HEAL_HP:
                int oldHP = player.getCurrentHP();
                ItemEffectSystem.applyItemEffect(player, itemToUse, true);
                int healed = player.getCurrentHP() - oldHP;
                effectMessage = "You used " + itemToUse.getName() + ".\nYou recovered " + healed + " HP!";
                healSound.play(0.15f);
                break;

            case RESTORE_MP:
                int oldMP = player.getMP();
                ItemEffectSystem.applyItemEffect(player, itemToUse, true);
                int restored = player.getMP() - oldMP;
                effectMessage = "You used " + itemToUse.getName() + ".\nYou recovered " + restored + " MP!";
                manaRegenSound.play(0.15f);
                break;

            case FULL_HEAL:
                int oldFullHP = player.getCurrentHP();
                ItemEffectSystem.applyItemEffect(player, itemToUse, true);
                int fullHealed = player.getCurrentHP() - oldFullHP;
                effectMessage = "You used " + itemToUse.getName() + ".\nYou fully recovered your HP!";
                healSound.play(0.15f);
                break;

            case FULL_RESTORE:
                ItemEffectSystem.applyItemEffect(player, itemToUse, true);
                effectMessage = "You used " + itemToUse.getName() + ".\nYou fully recovered HP and MP!";
                healSound.play(0.15f);
                break;

            case BUFF_ATK:
                ItemEffectSystem.applyItemEffect(player, itemToUse, true);
                effectMessage = "You used " + itemToUse.getName() + ".\nATK INCREASED!";
                selectSound.play(0.15f);
                break;

            case BUFF_DEF:
                ItemEffectSystem.applyItemEffect(player, itemToUse, true);
                effectMessage = "You used " + itemToUse.getName() + ".\nDEF INCREASED!";
                selectSound.play(0.15f);
                break;

            default:
                ItemEffectSystem.applyItemEffect(player, itemToUse, true);
                effectMessage = "You used " + itemToUse.getName() + ".";
                selectSound.play(0.15f);
                break;
        }

        // Check if any buffs were applied
        int attackAfter = player.getAttack();
        int defenseAfter = player.getDefense();

        // If the item has buff duration, add buff message
        if (itemToUse.getBuffDuration() > 0) {
            buffMessage.append("\n");

            if (itemToUse.getBuffAtkAmount() > 0) {
                buffMessage.append("ATK +" + itemToUse.getBuffAtkAmount() + "!");

                // Add space instead of newline if we'll also be adding DEF buff
                if (itemToUse.getBuffDefAmount() > 0) {
                    buffMessage.append(" ");
                }
            }

            if (itemToUse.getBuffDefAmount() > 0) {
                buffMessage.append("DEF +" + itemToUse.getBuffDefAmount() + "!");
            }
        }
        // If there were stat changes without buff duration, it might be from equipment effects
        else if (attackAfter > attackBefore || defenseAfter > defenseBefore) {
            if (attackAfter > attackBefore) {
                buffMessage.append("\nATK +" + (attackAfter - attackBefore) + "!");

                // Add space instead of newline if we'll also be adding DEF buff
                if (defenseAfter > defenseBefore) {
                    buffMessage.append(" ");
                }
            }
            if (defenseAfter > defenseBefore) {
                buffMessage.append("\nDEF +" + (defenseAfter - defenseBefore) + "!");
            }
        }

        // Combine the effect message with any buff messages
        effectMessage += buffMessage.toString();

        // Update player stats
        updateHPTargets();
        updateMPTargets();

        // Set dialogue text to show effect
        setDialogueText(effectMessage);

        // Exit item menu
        showingItemMenu = false;

        // We shouldn't update buffs here as we haven't completed a turn yet
        // Let buffs remain until after enemy's turn

        // Start enemy turn (delayed)
        startDelayedCombat();
    }

    /**
     * Apply the temporary item usage when combat is successfully completed
     */
    private void applyItemUsage() {
        if (temporaryItemUsage.isEmpty()) return;

        // Get all consumable items
        Array<ConsumableItem> consumables = player.getInventory().getConsumableItems();

        // Apply usage counts
        for (Map.Entry<String, Integer> usage : temporaryItemUsage.entrySet()) {
            String itemId = usage.getKey();
            int usageCount = usage.getValue();

            // Skip if no usage
            if (usageCount <= 0) continue;

            // Find matching item in inventory
            for (int i = 0; i < consumables.size; i++) {
                ConsumableItem item = consumables.get(i);
                if (item.getId().equals(itemId)) {
                    // Reduce item quantity by the number used during combat
                    for (int j = 0; j < usageCount; j++) {
                        player.getInventory().useConsumableItem(item);
                    }
                    break;
                }
            }
        }

        // Save the player state after all changes are applied
        player.saveToFile();

        // Clear the temporary usage
        temporaryItemUsage.clear();
    }

    /**
     * Gets the current enemy in this combat scene
     * @return The current enemy
     */
    public Enemy getCurrentEnemy() {
        return currentEnemy;
    }

    /**
     * Creates a thorn damage number display for the enemy
     * @param damage The amount of thorn damage dealt
     */
    public void createThornDamageNumber(int damage) {
        if (currentEnemy == null) return;

        float screenCenterX = viewport.getWorldWidth() / 2;
        float enemyX = screenCenterX;
        float enemyY = arena.y + ARENA_DEFAULT_HEIGHT + 30 + DAMAGE_NUMBER_ENEMY_Y_OFFSET;

        // Use the special thorn damage creator
        damageNumbers.add(DamageNumber.createThornDamage(damage, enemyX, enemyY));
    }

    /**
     * Updates all active damage numbers
     * @param delta Time elapsed since last update
     */
    private void updateDamageNumbers(float delta) {
        for (int i = damageNumbers.size() - 1; i >= 0; i--) {
            DamageNumber damageNumber = damageNumbers.get(i);
            if (!damageNumber.update(delta)) {
                damageNumbers.remove(i);
            }
        }
    }

    /**
     * Renders all active damage numbers
     */
    private void renderDamageNumbers() {
        if (damageNumbers.isEmpty()) return;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        for (DamageNumber damageNumber : damageNumbers) {
            damageNumber.draw(spriteBatch, font);
        }

        spriteBatch.end();
    }

    // Add a public method that enemies can use to create damage numbers for thorn damage
    public void addDamageNumber(int damage, float x, float y, boolean isHealing, boolean isCritical) {
        damageNumbers.add(new DamageNumber(damage, x, y, isHealing, isCritical));
    }

    /**
     * Public method to update and show the enemy HP bar
     * This is called when thorn damage is applied during combat
     */
    public void updateAndShowEnemyHP() {
        updateEnemyHPTargets();
        showHPAfterDamage = true;
        hpBarLingerTimer = -1f; // Reset to inactive state until animation finishes
    }

    /**
     * Updates the Death Defiance state, including the rainbow effect and timer
     */
    private void updateDeathDefianceState(float delta) {
        // Update the timer
        deathDefianceTimer -= delta;

        // Update rainbow color effect with an even faster cycle speed for more noticeable changes
        rainbowColorTime += delta * RAINBOW_CYCLE_SPEED * 5.0f;
        if (rainbowColorTime > 1.0f) {
            rainbowColorTime -= 1.0f;
        }

        // Calculate rainbow color with maximum saturation and brightness for vivid effect
        float hue = rainbowColorTime;
        rainbowColor = hsvToRgb(hue, 1.0f, 1.0f);

        // If the timer is up, exit the Death Defiance state
        if (deathDefianceTimer <= 0) {
            exitDeathDefianceState();
        }
    }

    /**
     * Converts HSV values to an RGB Color
     * @param h Hue (0-1)
     * @param s Saturation (0-1)
     * @param v Value (0-1)
     * @return Color object with the converted RGB values
     */
    private Color hsvToRgb(float h, float s, float v) {
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        float r, g, b;
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            default: r = v; g = p; b = q; break;
        }

        return new Color(r, g, b, 1f);
    }

    /**
     * Activates the Death Defiance effect when the player would otherwise die
     */
    private void activateDeathDefiance() {
        // Mark as used for this combat - this is critical to ensure it can only be used once
        deathDefianceAvailable = false;
        GameLogger.logInfo("Death Defiance has been used! No longer available for this combat.");

        // Activate the effect
        inDeathDefianceState = true;
        deathDefianceTimer = DEATH_DEFIANCE_DURATION;

        // Also set immunity to true for consistent visuals and double protection
        isImmune = true;
        immunityTimer = 0;

        // Play sound effect
        deathDefianceSound.play(0.3f); // Increase volume for more impact

        // Display message indicating Death Defiance activated
        String defianceMessage = "DEATH DEFIANCE ACTIVATED!";

        // Create a damage number at the top center of the screen with this message
        float screenCenterX = viewport.getWorldWidth() / 2;
        float screenTopY = viewport.getWorldHeight() - 50;

        // Create a special "damage number" for the message that stays longer
        DamageNumber messageNumber = new DamageNumber(0, screenCenterX, screenTopY, false, true);
        messageNumber.setCustomText(defianceMessage);
        messageNumber.setColor(Color.GOLD);
        messageNumber.setLifetime(DEATH_DEFIANCE_DURATION * 0.8f);
        damageNumbers.add(messageNumber);

        // Heal the player
        player.fullHeal();
        updateHPTargets();

        // Add massive defense buff for the duration - maximum defense boost value to ensure invulnerability
        player.addStatBuff(BuffManager.StatType.DEFENSE, DEATH_DEFIANCE_DEFENSE_BOOST, (int)Math.ceil(DEATH_DEFIANCE_DURATION));

        // Start a big shake effect for dramatic impact
        startShake(0.75f, 20.0f);

        // Create a healing number to show the full heal
        float healNumberX = playerHitbox.x + playerHitbox.width / 2;
        float healNumberY = playerHitbox.y + playerHitbox.height + DAMAGE_NUMBER_PLAYER_Y_OFFSET;
        DamageNumber healNumber = new DamageNumber(player.getMaxHP(), healNumberX, healNumberY, true, true);
        healNumber.setLifetime(DEATH_DEFIANCE_DURATION * 0.6f); // Show heal number for a bit shorter than the effect
        damageNumbers.add(healNumber);
    }

    /**
     * Exits the Death Defiance state
     */
    private void exitDeathDefianceState() {
        // Only proceed if we're actually in Death Defiance state
        if (!inDeathDefianceState) {
            return;
        }

        inDeathDefianceState = false;
        rainbowColor.set(Color.WHITE); // Reset color

        // Keep deathDefianceAvailable as false for the rest of this combat
        // This ensures it can't be used more than once per fight
        deathDefianceAvailable = false;

        // Make sure player is visible and not immune after leaving Death Defiance state
        isVisible = true;
        isImmune = false;

        // Remove the massive defense buff
        player.removeStatBuff(BuffManager.StatType.DEFENSE, DEATH_DEFIANCE_DEFENSE_BOOST);

        // Show a message indicating Death Defiance has worn off (only if in active gameplay)
        if (inCombat || !playerTurn) {
            float screenCenterX = viewport.getWorldWidth() / 2;
            float screenTopY = viewport.getWorldHeight() - 80;

            DamageNumber wornOffMsg = new DamageNumber(0, screenCenterX, screenTopY, false, false);
            wornOffMsg.setCustomText("Death Defiance Worn Off!");
            wornOffMsg.setColor(Color.ORANGE);
            wornOffMsg.setLifetime(1.5f);
            damageNumbers.add(wornOffMsg);

            GameLogger.logInfo("Death Defiance worn off and no longer available for this combat");
        }
    }

    // Add this new method for rendering the explosion
    private void renderEnemyExplosion() {
        TextureRegion currentFrame = explosionAnimation.getKeyFrame(enemyExplosionTimer, false);
        float explosionWidth = 250f;
        float explosionHeight = 350f;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Draw explosion centered on the enemy's position
        // Calculate the position to center the explosion
        float explosionX = enemyDeathX - (explosionWidth / 2);
        float explosionY = enemyDeathY - (explosionHeight / 2);

        // Draw glow behind explosion
        float glowIntensity = 1.0f - (enemyExplosionTimer / explosionAnimation.getAnimationDuration());
        spriteBatch.setColor(1f, 0.7f, 0.2f, glowIntensity * 0.7f);
        float glowWidth = explosionWidth * 1.2f;
        float glowHeight = explosionHeight * 1.2f;
        float glowX = explosionX - (glowWidth - explosionWidth) / 2f;
        float glowY = explosionY - (glowHeight - explosionHeight) / 2f;
        spriteBatch.draw(currentFrame, glowX, glowY, glowWidth, glowHeight);

        // Draw actual explosion
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.draw(currentFrame, explosionX, explosionY, explosionWidth, explosionHeight);

        spriteBatch.end();
    }

    // Add skill menu rendering method
    private void renderSkillMenu() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Get player's unlocked skills (excluding BASIC)
        Player.SkillType[] allSkills = Player.SkillType.values();
        java.util.List<Player.SkillType> unlockedSkills = new java.util.ArrayList<>();

        for (Player.SkillType skill : allSkills) {
            // Skip the BASIC skill
            if (skill != Player.SkillType.BASIC && player.isSkillUnlocked(skill)) {
                unlockedSkills.add(skill);
            }
        }

        // Calculate total pages
        totalSkillPages = (int) Math.ceil((float) unlockedSkills.size() / SKILLS_PER_PAGE);
        if (totalSkillPages == 0) totalSkillPages = 1;

        // Make sure current page is valid
        currentSkillPage = Math.min(currentSkillPage, totalSkillPages - 1);

        // Calculate starting index for current page
        int startIdx = currentSkillPage * SKILLS_PER_PAGE;
        int endIdx = Math.min(startIdx + SKILLS_PER_PAGE, unlockedSkills.size());
        int skillsOnCurrentPage = endIdx - startIdx;

        // Ensure selectedSkillIndex is valid for the current page
        selectedSkillIndex = Math.min(selectedSkillIndex, skillsOnCurrentPage - 1);
        if (selectedSkillIndex < 0 && skillsOnCurrentPage > 0) {
            selectedSkillIndex = 0;
        }

        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        float skillX = arena.x + SKILL_MENU_PADDING;
        float skillY = arena.y + arena.height - SKILL_MENU_PADDING;
        float rowHeight = 50f;
        float maxSkillWidth = arena.width - (SKILL_MENU_PADDING * 2);

        // Draw skills in a list
        for (int i = startIdx; i < endIdx; i++) {
            Player.SkillType skill = unlockedSkills.get(i);
            int gridPos = i - startIdx;

            float x = skillX;
            float y = skillY - gridPos * rowHeight;

            // Calculate MP cost
            int mpCost = player.getSkillMPCost(skill);
            String mpText = " (" + mpCost + " MP)";
            String skillName = player.getSkillDisplayName(skill);
            String prefix = "* ";

            // Check if skill name will overflow
            GlyphLayout layout = new GlyphLayout(font, prefix + skillName + mpText);
            if (layout.width > maxSkillWidth) {
                // Truncate name to fit
                int maxChars = 0;
                for (int j = 1; j <= skillName.length(); j++) {
                    GlyphLayout testLayout = new GlyphLayout(font, prefix + skillName.substring(0, j) + "..." + mpText);
                    if (testLayout.width > maxSkillWidth) {
                        break;
                    }
                    maxChars = j;
                }

                if (maxChars > 0) {
                    skillName = skillName.substring(0, maxChars) + "...";
                }
            }

            // Draw skill with proper spacing
            String skillText = prefix + skillName + mpText;
            // Set color based on selection and MP availability
            if (gridPos == selectedSkillIndex) {
                font.setColor(Color.YELLOW); // Selected skill is yellow
            } else if (player.hasFreeSkillCastAvailable()) {
                font.setColor(Color.CYAN); // Free skill cast available - cyan
            } else if (player.hasEnoughMPForSkill(skill)) {
                font.setColor(Color.WHITE); // Normal usable skill is white
            } else {
                font.setColor(Color.GRAY); // Not enough MP - gray
            }
            font.draw(spriteBatch, skillText, x, y);
        }

        // Draw page indicator at bottom right (if multiple pages)
        if (totalSkillPages > 1) {
            String pageText = "*PG " + (currentSkillPage + 1) + "/" + totalSkillPages + "*";
            GlyphLayout layout = new GlyphLayout(font, pageText);
            float pageX = arena.x + arena.width - layout.width - PAGE_INDICATOR_PADDING;
            float pageY = arena.y + PAGE_INDICATOR_PADDING + layout.height;
            font.getData().setScale(1.0f);
            font.setColor(Color.LIGHT_GRAY);
            font.draw(spriteBatch, pageText, pageX, pageY);
        }

        // If no skills available
        if (unlockedSkills.size() == 0) {
            String noSkillsText = "No skills available";
            GlyphLayout layout = new GlyphLayout(font, noSkillsText);
            float textX = arena.x + (arena.width - layout.width) / 2;
            float textY = arena.y + (arena.height + layout.height) / 2;
            font.setColor(Color.WHITE);
            font.draw(spriteBatch, noSkillsText, textX, textY);
        }

        spriteBatch.end();
        font.getData().setScale(2.0f);
    }

    // Add skill menu input handling
    private void handleSkillMenuInput() {
        // Get unlocked skills (excluding BASIC)
        Player.SkillType[] allSkills = Player.SkillType.values();
        java.util.List<Player.SkillType> unlockedSkills = new java.util.ArrayList<>();

        for (Player.SkillType skill : allSkills) {
            if (skill != Player.SkillType.BASIC && player.isSkillUnlocked(skill)) {
                unlockedSkills.add(skill);
            }
        }

        // Calculate skills on current page
        int startIdx = currentSkillPage * SKILLS_PER_PAGE;
        int skillsOnCurrentPage = Math.min(SKILLS_PER_PAGE, unlockedSkills.size() - startIdx);

        if (skillsOnCurrentPage <= 0) {
            // No skills, just handle escape
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                showingSkillMenu = false;
                selectSound.play(0.5f);
            }
            return;
        }

        // Ensure selectedSkillIndex is valid
        selectedSkillIndex = Math.min(selectedSkillIndex, skillsOnCurrentPage - 1);
        if (selectedSkillIndex < 0) {
            selectedSkillIndex = 0;
        }

        // Navigate between skills - vertical navigation only
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (selectedSkillIndex < skillsOnCurrentPage - 1) {
                // Move down within the page
                selectedSkillIndex++;
                selectSound.play(0.5f);
            } else if (currentSkillPage < totalSkillPages - 1) {
                // At the bottom of page, go to next page
                currentSkillPage++;
                selectedSkillIndex = 0;
                selectSound.play(0.5f);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (selectedSkillIndex > 0) {
                // Move up within the page
                selectedSkillIndex--;
                selectSound.play(0.5f);
            } else if (currentSkillPage > 0) {
                // At the top of page, go to previous page
                currentSkillPage--;
                // Set index to last skill on previous page
                int skillsOnPrevPage = Math.min(SKILLS_PER_PAGE,
                    unlockedSkills.size() - (currentSkillPage * SKILLS_PER_PAGE));
                selectedSkillIndex = skillsOnPrevPage - 1;
                selectSound.play(0.5f);
            }
        }

        // Keep PAGE_UP/PAGE_DOWN navigation for accessibility
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN) && currentSkillPage < totalSkillPages - 1) {
            currentSkillPage++;
            selectedSkillIndex = 0;
            selectSound.play(0.5f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP) && currentSkillPage > 0) {
            currentSkillPage--;
            selectedSkillIndex = 0;
            selectSound.play(0.5f);
        }

        // Select skill
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            useSelectedSkill();
        }

        // Cancel
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            showingSkillMenu = false;
            selectSound.play(0.5f);
        }
    }

    // Add method to use the selected skill
    private void useSelectedSkill() {
        // Get the unlocked skills (excluding BASIC)
        Player.SkillType[] allSkills = Player.SkillType.values();
        java.util.List<Player.SkillType> unlockedSkills = new java.util.ArrayList<>();

        for (Player.SkillType skill : allSkills) {
            if (skill != Player.SkillType.BASIC && player.isSkillUnlocked(skill)) {
                unlockedSkills.add(skill);
            }
        }

        // Calculate the actual index based on current page and selection
        int startIdx = currentSkillPage * SKILLS_PER_PAGE;
        int endIdx = Math.min(startIdx + SKILLS_PER_PAGE, unlockedSkills.size());

        // Validate selected index
        if (selectedSkillIndex < 0 || startIdx + selectedSkillIndex >= unlockedSkills.size()) {
            return; // Invalid selection
        }

        // Get the selected skill
        Player.SkillType selectedSkill = unlockedSkills.get(startIdx + selectedSkillIndex);

        // Set as current skill
        player.setCurrentSkill(selectedSkill);

        // Check if player has enough MP for this skill
        if (player.hasEnoughMPForSkill(selectedSkill)) {
            // Close skill menu and start attack sequence
            showingSkillMenu = false;
            startAttackSequence();
        } else {
            // Close skill menu first, then show not enough MP message
            showingSkillMenu = false;

            // Show not enough MP message
            if (selectedSkill == Player.SkillType.SKILL6) {
                setDialogueText("Not enough MP! Silk End - I Am Cosmic Weave requires atleast 50% MP!");
            } else {
                setDialogueText("Not enough MP to use this skill!");
            }

            // Reset any in-progress actions to prevent conflicts
            inAttackSequence = false;
            delayedCombatPending = false;
            combatKeyPressCount = 0;
        }
    }

    /**
     * Centers the player in the arena based on current arena dimensions
     */
    private void centerPlayer() {
        // If in combat or transitioning to combat, use special calculation
        if (inCombat) {
            // Use the target arena dimensions
            float screenWidth = viewport.getWorldWidth();
            float centerX = (screenWidth - targetArenaWidth) / 2 + (targetArenaWidth / 2) - (PLAYER_SIZE / 2);

            float buttonY = 20;
            float buttonHeight = BUTTON_HEIGHT;
            float marginAboveButtons = 60;
            float hudY = buttonY + buttonHeight + marginAboveButtons;

            float centerY = hudY + (targetArenaHeight / 2) - (PLAYER_SIZE / 2);

            playerSprite.setPosition(centerX, centerY);
            playerHitbox.x = centerX;
            playerHitbox.y = centerY;
        } else {
            // When not in combat, use current arena dimensions
            float centerX = arena.x + (arena.width / 2) - (PLAYER_SIZE / 2);
            float centerY = arena.y + (arena.height / 2) - (PLAYER_SIZE / 2);

            playerSprite.setPosition(centerX, centerY);
            playerHitbox.x = centerX;
            playerHitbox.y = centerY;
        }
    }

    /**
     * Loads or reloads the enemy's custom background texture.
     * This can be called to attempt a reload if the texture wasn't successfully loaded at initialization.
     */
    private void loadEnemyBackground() {
        if (currentEnemy == null || backgroundLoadAttempted) {
            return;
        }

        if (currentEnemy.getCombatBackground() != null) {
            try {
                // Dispose existing textures if any
                if (enemyBackgroundTexture != null) {
                    enemyBackgroundTexture.dispose();
                }
                if (blurredBackgroundTexture != null) {
                    blurredBackgroundTexture.dispose();
                }
                if (blurFrameBuffer != null) {
                    blurFrameBuffer.dispose();
                }

                String backgroundPath = currentEnemy.getCombatBackground();
                this.enemyBackgroundTexture = new Texture(Gdx.files.internal(backgroundPath));
                GameLogger.logInfo("Loaded enemy background: " + backgroundPath);

                // Initialize blur effect only if spriteBatch is initialized
                if (spriteBatch != null) {
                    initializeBackgroundBlur();
                } else {
                    GameLogger.logInfo("SpriteBatch not yet initialized - deferring blur effect");
                }
            } catch (Exception e) {
                GameLogger.logError("Failed to load enemy background", e);
                this.enemyBackgroundTexture = null;
                this.blurredBackgroundTexture = null;
            }
        }

        this.backgroundLoadAttempted = true;
    }

    // Add method to create blurred background texture
    private void initializeBackgroundBlur() {
        try {
            if (enemyBackgroundTexture == null) return;

            // Check if spriteBatch is initialized
            if (spriteBatch == null) {
                GameLogger.logInfo("Cannot initialize background blur - SpriteBatch is null");
                backgroundBlurInitialized = false;
                return;
            }

            // Get current viewport dimensions for proper sizing
            float screenWidth = viewport.getWorldWidth();
            float screenHeight = viewport.getWorldHeight();

            // Clean up existing resources if they exist
            if (blurFrameBuffer != null) {
                blurFrameBuffer.dispose();
            }
            if (blurredBackgroundTexture != null) {
                blurredBackgroundTexture.dispose();
            }

            // Create a frame buffer at current screen resolution
            blurFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888,
                (int)screenWidth, (int)screenHeight, false);

            // Create the blurred texture
            createBlurredBackgroundTexture();

            backgroundBlurInitialized = true;
        } catch (Exception e) {
            GameLogger.logError("Failed to initialize background blur", e);
            backgroundBlurInitialized = false;
        }
    }

    // Method to actually create the blurred texture
    private void createBlurredBackgroundTexture() {
        if (enemyBackgroundTexture == null || blurFrameBuffer == null) return;

        // Check if spriteBatch is initialized
        if (spriteBatch == null) {
            GameLogger.logInfo("Cannot create blurred background - SpriteBatch is null");
            return;
        }

        // Get current viewport dimensions
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        // Save current SpriteBatch state
        Matrix4 oldProjection = new Matrix4(spriteBatch.getProjectionMatrix());

        // Simple blur implementation - draw at reduced alpha multiple times with small offsets
        blurFrameBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Use identity matrix for frame buffer rendering to avoid camera positioning issues
        Matrix4 identityMatrix = new Matrix4().setToOrtho2D(0, 0, screenWidth, screenHeight);
        spriteBatch.setProjectionMatrix(identityMatrix);

        spriteBatch.begin();
        // Draw the original texture scaled to current screen size
        spriteBatch.setColor(1, 1, 1, 0.5f);
        spriteBatch.draw(enemyBackgroundTexture, 0, 0, screenWidth, screenHeight);

        // Draw multiple semi-transparent copies with offsets for a simple blur effect
        spriteBatch.setColor(1, 1, 1, 0.3f);
        float blurSize = BACKGROUND_BLUR_STRENGTH;

        // Draw offset copies
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                if (xOffset == 0 && yOffset == 0) continue; // Skip center (original position)
                spriteBatch.draw(enemyBackgroundTexture,
                    xOffset * blurSize, yOffset * blurSize,
                    screenWidth, screenHeight);
            }
        }

        // Add a darker overlay for contrast with gameplay elements
        spriteBatch.setColor(0, 0, 0, 0.1f);
        spriteBatch.draw(enemyBackgroundTexture, 0, 0, screenWidth, screenHeight);

        spriteBatch.end();
        blurFrameBuffer.end();

        // Convert the frame buffer to a texture
        if (blurredBackgroundTexture != null) {
            blurredBackgroundTexture.dispose();
        }
        blurredBackgroundTexture = blurFrameBuffer.getColorBufferTexture();

        // Restore original projection matrix
        spriteBatch.setProjectionMatrix(oldProjection);
    }

    // Helper method to load game settings
    private OptionsScreen.GameSettings loadGameSettings() {
        try {
            FileHandle file = Gdx.files.local("options.json");
            if (file.exists()) {
                Json json = new Json();
                return json.fromJson(OptionsScreen.GameSettings.class, file.readString());
            }
        } catch (Exception e) {
            GameLogger.logError("Failed to load game settings", e);
        }
        return new OptionsScreen.GameSettings(); // Return default settings if loading fails
    }

    // Add these new methods for player state snapshot

    /**
     * Takes a snapshot of the player's current state at the start of combat
     */
    private void takePlayerSnapshot() {
        if (!snapshotTaken) {
            try {
                // Create a deep copy of the player
                playerSnapshot = player.createSnapshot();
                snapshotTaken = true;
//                GameLogger.logInfo("Player snapshot taken at start of combat");
            } catch (Exception e) {
                GameLogger.logError("Failed to take player snapshot", e);
            }
        }
    }

    /**
     * Restores the player's state from the snapshot (used when retreating or dying)
     */
    private void restorePlayerFromSnapshot() {
        if (snapshotTaken && playerSnapshot != null) {
            try {
                // Restore player from snapshot
                player.restoreFromSnapshot(playerSnapshot);
//                GameLogger.logInfo("Player state restored from snapshot after retreat/death");

                // Update UI values to match restored state
                // updateHPTargets();
                // updateMPTargets();

                // Clear temporary item usage
                temporaryItemUsage.clear();

                // Save the restored state to disk to ensure persistence between screens
                player.saveToFile();
            } catch (Exception e) {
                GameLogger.logError("Failed to restore player from snapshot", e);
            }
        }
    }

    /**
     * Clears the player snapshot (used after successful combat)
     */
    private void clearPlayerSnapshot() {
        playerSnapshot = null;
        snapshotTaken = false;
    }

    /**
     * Transitions to the credits screen after defeating the final boss
     */
    private void transitionToCreditsScreen() {
        // Stop current music
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }

        // Save player data
        player.saveToFile();

        // Set returning from combat flag
        returningFromCombat = true;

        // Transition to credits screen
        game.setScreen(new ScreenTransition(
            game,
            this,
            new CreditsScreen(game),
            ScreenTransition.TransitionType.FADE_TO_WHITE
        ));
    }
}
