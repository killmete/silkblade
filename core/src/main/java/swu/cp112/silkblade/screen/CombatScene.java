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

import swu.cp112.silkblade.core.Main;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.entity.enemy.silkgod.DemoEnemy;
import swu.cp112.silkblade.screen.transition.ScreenTransition;
import swu.cp112.silkblade.util.GameLogger;
import swu.cp112.silkblade.entity.combat.BulletTextures;
import swu.cp112.silkblade.entity.item.ConsumableItem;

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

    // =================== Item Menu State ===================
    private boolean showingItemMenu = false;
    private int selectedItemIndex = 0;
    private int currentItemPage = 0;
    private int totalItemPages = 1;
    // Map to track temporary item usage during combat (not saved until combat is won)
    private java.util.Map<String, Integer> temporaryItemUsage = new java.util.HashMap<>();

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

    // =================== Additional State ===================
    private boolean pendingDefeatMessage = false;
    private String savedDefeatMessage = "";

    // Add a static flag to track if we're returning from combat
    public static boolean returningFromCombat = false;

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
            viewport = Main.getViewport();
            camera = Main.getCamera();

            // Initialize default text speed
            this.currentTextSpeed = LETTER_DELAY;

            // Initialize arena Rectangle right after viewport and camera
            this.arena = new Rectangle(0, 0, ARENA_DEFAULT_WIDTH, ARENA_DEFAULT_HEIGHT);
            this.dialogueArenaWidth = ARENA_DEFAULT_WIDTH;
            this.dialogueArenaHeight = ARENA_DEFAULT_HEIGHT;
            this.currentArenaWidth = ARENA_DEFAULT_WIDTH;
            this.currentArenaHeight = ARENA_DEFAULT_HEIGHT;

            // Initialize rendering objects
            spriteBatch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            font = new BitmapFont(Gdx.files.internal("fonts/DTM.fnt"));
            font.setColor(Color.WHITE);
            font.getData().setScale(2);

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
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music/music.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(.05f);

            typingSound = Gdx.audio.newSound(Gdx.files.internal("sounds/typing_2.wav"));
            selectSound = Gdx.audio.newSound(Gdx.files.internal("sounds/select.wav"));
            attackSound = Gdx.audio.newSound(Gdx.files.internal("sounds/attack.wav"));
            escapeSound = Gdx.audio.newSound(Gdx.files.internal("sounds/escape.wav"));
            hurtSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hurt.wav"));
            healSound = Gdx.audio.newSound(Gdx.files.internal("sounds/heal.wav"));
            levelUpSound = Gdx.audio.newSound(Gdx.files.internal("sounds/level_up.wav"));
            manaRegenSound = Gdx.audio.newSound(Gdx.files.internal("sounds/mana_regen.wav"));
            explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.wav"));

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
            shapeRenderer.dispose();

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
        // Only update linger timer after defeat message is shown
        if (showDefeatedEnemy && !pendingDefeatMessage) {
            defeatedEnemyLingerTimer += delta;
            if (defeatedEnemyLingerTimer >= DEFEATED_ENEMY_LINGER_TIME) {
                showDefeatedEnemy = false;
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
    }

    // =================== Rendering Methods ===================
    private void renderGameElements() {
        // Show enemy if:
        // 1. Not defeated OR
        // 2. Showing damage message OR
        // 3. Waiting for death message to be shown (pendingDefeatMessage)
        if (!currentEnemy.isDefeated() || showHPAfterDamage || pendingDefeatMessage) {
            renderEnemy();
            if (showHPAfterDamage || pendingDefeatMessage) {
                renderEnemyHP();
            }
        }

        renderCombatBox();
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
        } else {
            renderDialogueText();
            // Render bullets on top of dialogue if any exist after combat
            if (bullets.size > 0) {
                renderBullets();
            }
        }

        renderButtons();
    }

    // =================== Arena/Box Management Methods ===================

    private void centerArena() {
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        // Calculate positions for both cases
        float centerX = (screenWidth - arena.width) / 2;
        float buttonY = 20;
        float buttonHeight = BUTTON_HEIGHT;
        float marginAboveButtons = 60;

        float hudY = buttonY + buttonHeight + marginAboveButtons;
        float centerY = (screenHeight - arena.height) / 2;

        // Set X position (always centered horizontally)
        arena.x = centerX;

        // Determine Y position based on size with smooth transition
        if (arena.width <= 100 && arena.height <= 100) {
            arena.y = centerY;
        } else {
            arena.y = hudY;
        }

        // When transitioning between sizes, update target position before animation starts
        if (isTransitioning) {
            targetX = centerX;
            targetY = (targetArenaWidth <= 100 && targetArenaHeight <= 100) ? centerY : hudY;
        }
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

        // Check if transition is complete
        if (Math.abs(currentArenaWidth - targetArenaWidth) < 0.5f &&
                Math.abs(currentArenaHeight - targetArenaHeight) < 0.5f &&
                Math.abs(arena.x - targetX) < 0.5f &&
                Math.abs(arena.y - targetY) < 0.5f) {
            isTransitioning = false;
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

            targetX = (screenWidth - targetArenaWidth) / 2;

            float buttonY = 20;
            float buttonHeight = BUTTON_HEIGHT;
            float marginAboveButtons = 60;

            float hudY = buttonY + buttonHeight + marginAboveButtons;
            float centerY = (screenHeight - targetArenaHeight) / 2;

            targetY = (targetArenaWidth <= 100 && targetArenaHeight <= 100) ? centerY : hudY;
        }

        this.isTransitioning = true;
    }

    private void renderCombatBox() {
        Gdx.gl.glLineWidth(5);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw filled black background
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
                showHPAfterDamage = false;
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
        COMBAT_ARENA_WIDTH = currentEnemy.getArenaWidth();
        COMBAT_ARENA_HEIGHT = currentEnemy.getArenaHeight();
        setArenaSize(COMBAT_ARENA_WIDTH, COMBAT_ARENA_HEIGHT);
        playerTurn = false;
        enemyTurn = true;
        bullets.clear();
        bulletsSpawned = 0;
        combatActive = true;
        showHPAfterDamage = false;
        allBulletsFired = false;  // Reset the all bullets fired flag

        // Dim the enemy during combat
        currentEnemy.setAlpha(COMBAT_PHASE_ALPHA);

        // Update these values from the enemy
        bulletSpawnInterval = currentEnemy.getAttackInterval();
        maxBullets = currentEnemy.getMaxBullets();

        if (currentEnemy != null) {
            currentEnemy.startTurn();
            // Update interval again after startTurn in case it changed
            bulletSpawnInterval = currentEnemy.getAttackInterval();
            maxBullets = currentEnemy.getMaxBullets();
        }

        float centerX = targetX + (targetArenaWidth / 2) - (PLAYER_SIZE / 2);
        float centerY = targetY + (targetArenaHeight / 2) - (PLAYER_SIZE / 2);

        playerSprite.setPosition(centerX, centerY);
        playerHitbox.x = centerX;
        playerHitbox.y = centerY;
        playerHitbox.width = PLAYER_SIZE;
        playerHitbox.height = PLAYER_SIZE;
    }

    public void endCombat() {
        inCombat = false;
        setArenaSize(dialogueArenaWidth, dialogueArenaHeight);
        playerTurn = true;
        enemyTurn = false;
        currentDisplayText.setLength(0);
        currentLetterIndex = 0;
        isTyping = true;
        dialogueCompleted = false;

        // Reset blend function to ensure proper rendering after combat
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Restore enemy brightness
        currentEnemy.setAlpha(1.0f);

        // Regenerate MP and play sound
        float oldMP = player.getMP();
        player.regenMP();
        if (player.getMP() > oldMP) {  // Only play sound if MP actually increased
            manaRegenSound.play(0.15f);  // Adjust volume (0.0 to 1.0) as needed
        }
        updateMPTargets();

        if (currentEnemy != null) {
            currentEnemy.endTurn();
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

                // Step 3: Play skill sound & start timer
                player.consumeMPForSkill(player.getCurrentSkill());
                updateMPTargets();
                attackSequenceTimer = -player.playSkillSound();
                playAudioOnce = false;
            }

            // Step 4: Increment timer
            attackSequenceTimer += delta;

            // Step 5: Once timer reaches threshold, execute attack
            if (attackSequenceTimer >= 1.2) {
                inAttackSequence = false;
                Player.DamageResult damageResult = player.calculateSkillDamage(player.getCurrentSkill());
                currentEnemy.damage(damageResult.damage, damageResult.isCritical);
                startShake(0.3f, 8.0f);

                String attackMessage = damageResult.isCritical
                    ? "CRITICAL! " + player.getSkillMessage(player.getCurrentSkill(), damageResult.damage, currentEnemy)
                    : player.getSkillMessage(player.getCurrentSkill(), damageResult.damage, currentEnemy);

                setDialogueText(attackMessage);
                showHPAfterDamage = true;
                updateEnemyHPTargets();

                if (currentEnemy.isDefeated()) {
                    backgroundMusic.stop();
                    savedDefeatMessage = currentEnemy.getDefeatDialogue() + "\n" + currentEnemy.getRewardDialogue();
                    pendingDefeatMessage = true;
                } else {
                    startDelayedCombat();
                }

                // Reset for the next attack
                playAudioOnce = true;
            }
        }
    }

    // =================== Player/Enemy Health Management Methods ===================

    public void decreaseHP(int damage) {
        if (!isImmune) {
            // Get current HP before damage for comparison
            float oldHP = player.getCurrentHP();

            // Apply damage
            player.takeDamage(damage);

            // Get new HP after damage
            float newHP = player.getCurrentHP();

            // If HP was extremely high and damage didn't cause a visible change
            // force a visible reduction in the bar
            if (oldHP > player.getMaxHP() && Math.abs(oldHP - newHP) / oldHP < 0.01) {
                // Make sure we see at least a small visual change
                targetHPWidth = Math.max(targetHPWidth - 1.0f, 0f);
            }

            if (player.isDead()) {
                // Reset blend function to normal before transitioning to main menu
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                backgroundMusic.stop();
                game.setScreen(new StageSelectionScreen(game));
            }

            startShake(0.3f, 8.0f);
            isImmune = true;
            immunityTimer = 0;
            isVisible = false;

            updateHPTargets();
        }
    }

    public void increaseHP(int healing) {
        player.heal(healing);  // Instead of direct HP manipulation
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

        // Check if HP reached 0 and start linger timer
        if (currentEnemyHPWidth <= 0f && showHPAfterDamage) {
            hpBarLingerTimer += delta;
            if (hpBarLingerTimer >= HP_BAR_LINGER_TIME) {
                showHPAfterDamage = false;
                hpBarLingerTimer = 0f;
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
            // Check if the previous character is a punctuation mark that should cause a pause
            if (currentChar == '.' || currentChar == ',' || currentChar == '!' ||
                currentChar == '?' || currentChar == ':' || currentChar == ';') {
                shouldPause = true;
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
            bulletSpawnTimer += delta;
            if (bulletSpawnTimer >= bulletSpawnInterval && bulletsSpawned < maxBullets) {
                List<Bullet> enemyBullets = currentEnemy.generateAttack(
                    arena.x, arena.y, arena.width, arena.height
                );

                if (enemyBullets != null && !enemyBullets.isEmpty()) {
                    bullets.addAll(enemyBullets.toArray(new Bullet[0]));
                    bulletsSpawned++;
                }

                bulletSpawnTimer = 0;
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
                    increaseHP(damageValue);
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

        // Calculate a scale factor based on viewport size for consistent line thickness
        float viewportWidth = viewport.getWorldWidth();
        float viewportHeight = viewport.getWorldHeight();
        float scaleFactor = Math.min(viewportWidth, viewportHeight);

        // Set projection matrix with camera
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (Bullet bullet : bullets) {
            if (bullet.isTelegraphing()) {
                float alpha = bullet.getTelegraphAlpha();
                if (alpha <= 0) continue;  // Skip if alpha is 0 or negative

                // Scale line width based on bullet size and screen size
                float lineWidth = Math.max(2f, bullet.getSize() * scaleFactor);
                Gdx.gl.glLineWidth(lineWidth);

                // Get the telegraph start and current bullet position
                float[] startPoint = bullet.getTelegraphStartPoint();
                float[] endpoint = bullet.getRemainingTelegraphEndPoint();

                // Skip if either point is null or if the line would be too short to be visible
                if (startPoint == null || endpoint == null) continue;

                // Check if the line is at least partially within the viewport bounds
                boolean isVisible = isLineVisibleInViewport(startPoint[0], startPoint[1], endpoint[0], endpoint[1]);
                if (!isVisible) continue;

                // Create a disco effect alternating between softer blue and purple
                Color telegraphColor = new Color();

                // Create faster flashing effect between soft blue and soft purple
                long currentTime = System.currentTimeMillis();
                boolean flashPhase = (currentTime / 120) % 2 == 0; // Very fast flashing - 12.5 times per second

                if (flashPhase) {
                    // Brighter blue for better visibility
                    telegraphColor.set(0.5f, 0.6f, 1.0f, alpha);
                } else {
                    // Brighter purple for better visibility
                    telegraphColor.set(0.8f, 0.5f, 1.0f, alpha);
                }

                // Draw telegraph line with disco color - ensure alpha is applied
                shapeRenderer.setColor(telegraphColor);
                shapeRenderer.line(startPoint[0], startPoint[1], endpoint[0], endpoint[1]);
            }
        }

        // End the shape renderer
        shapeRenderer.end();
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
                    // ADDED: Display current skill name below player name
                    font.setColor(Color.CYAN);
                    font.getData().setScale(0.9f);
                    String skillName = "Skill: " + player.getCurrentSkillDisplayName().toString();
                    font.draw(spriteBatch, skillName, x, playerNameY + 250);
                    font.setColor(Color.WHITE);
                    font.getData().setScale(1.2f);
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

        // Draw button outlines
        Gdx.gl.glLineWidth(4);
        shapeRenderer.setProjectionMatrix(camera.combined);
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
                spriteBatch.draw(playerTexture, heartX - 15, heartY, heartWidth, heartHeight);
            }
        }

        spriteBatch.end();
    }

    private void renderPlayer() {
        if (isVisible) {
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            Color prevColor = spriteBatch.getColor().cpy();
            spriteBatch.setColor(1, 1, 1, 1); // Ensure full opacity for player
            spriteBatch.draw(playerTexture, playerSprite.getX(), playerSprite.getY(), PLAYER_SIZE, PLAYER_SIZE);
            spriteBatch.setColor(prevColor);
            spriteBatch.end();
        }
    }

    // =================== Input & Game Logic Methods ===================

    private void handleInput(float deltaTime) {
        // If transitioning, only allow camera shake control and not other input
        if (ScreenTransition.isTransitioning()) return;

        final float MOVEMENT_SPEED = 300f;

        // Skip or escape input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GameLogger.logInfo("Escape key pressed in combat");
            if (inAttackSequence || delayedCombatPending) return;
            if (inCombat) return;
            if (currentEnemy.isDefeated()) return;

            if (showingItemMenu) {
                // Return from item menu to main combat UI
                showingItemMenu = false;
                selectedItemIndex = 0;
                currentItemPage = 0;
                selectSound.play(0.5f);
            }
            return;
        }

        if (currentEnemy.isDefeated()) {
            handleDefeatedEnemyInput();
        } else if (inCombat) {
            handleActiveCombatInput(deltaTime, MOVEMENT_SPEED);
        } else if (showingItemMenu) {
            handleItemMenuInput();
        } else {
            handleActiveCombatInput(deltaTime, MOVEMENT_SPEED);
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
                if(didLevelUp) {
                    levelUpSound.play(0.15f);
                    savedDefeatMessage += "\nLEVEL UP! You are now level " + player.getLevel() + "!";
                    player.heal(player.getMaxHP());
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
                if (!isBoss && player.getCurrentStage() % 10 == 0) {
                    isBoss = true;
                    // Calculate boss number based on stage (stage 10 = boss 1, stage 20 = boss 2, etc.)
                    bossNumber = player.getCurrentStage() / 10;
                    if (bossNumber > 5) bossNumber = 5; // Cap at 5 bosses total
                }
                
                // Mark the appropriate boss as defeated
                if (isBoss && bossNumber > 0 && bossNumber <= 5) {
                    player.setBossDefeated(bossNumber, true);
                    GameLogger.logInfo("Boss " + bossNumber + " marked as defeated");
                }
                
                setDialogueText(savedDefeatMessage);
                showDefeatedEnemy = false;
                pendingDefeatMessage = false;
                updateHPTargets();
                updateMPTargets();
            } else {
                // Apply the temporary item usage since combat was successful
                applyItemUsage();

                // Reset blend function before transitioning to main menu
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                player.saveToFile();
                transitionToMainMenu(ScreenTransition.TransitionType.CROSS_FADE);
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
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerSprite.translateX(-speed * deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerSprite.translateX(speed * deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            playerSprite.translateY(speed * deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            playerSprite.translateY(-speed * deltaTime);
        }
    }

    private void cycleSkill() {
        Player.SkillType[] skills = Player.SkillType.values();
        int currentIndex = player.getCurrentSkill().ordinal();
        int nextIndex = (currentIndex + 1) % skills.length;

        // Only set the skill if it's unlocked
        while (nextIndex != currentIndex) {
            if (player.isSkillUnlocked(skills[nextIndex])) {
                player.setCurrentSkill(skills[nextIndex]);
                break;
            }
            nextIndex = (nextIndex + 1) % skills.length;
        }
    }

    private void handleMenuInput() {
        if (ScreenTransition.isTransitioning()) return;

        if (currentEnemy.isDefeated() || inAttackSequence) {
            selectedButton = -1;
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            cycleSkill();
            player.playSkillSound();
        }
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
            case 0: // FIGHT
                // Check if player has enough MP for the current skill
                if (player.hasEnoughMPForSkill(player.getCurrentSkill())) {
                    startAttackSequence();
                } else {
                    if (player.getCurrentSkill() == Player.SkillType.SKILL6) {
                        setDialogueText("Not enough MP! Ultimate Technique requires more than 50% MP!");
                    } else {
                        setDialogueText("Not enough MP to use this skill!");
                    }
                }
                break;
            case 1: // ACT/SKILL
                // Add logic for ACT
                setDialogueText("You tried to reason with the enemy.\nBut they don't seem interested in talking.");
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

        // Use target dimensions when in combat, current dimensions otherwise
        float arenaWidth = inCombat ? COMBAT_ARENA_WIDTH : arena.width;
        float arenaHeight = inCombat ? COMBAT_ARENA_HEIGHT : arena.height;

        float clampedX = MathUtils.clamp(
            playerX,
            arena.x + ARENA_MARGIN,
            arena.x + arenaWidth - playerSprite.getWidth() - ARENA_MARGIN
        );

        float clampedY = MathUtils.clamp(
            playerY,
            arena.y + ARENA_MARGIN,
            arena.y + arenaHeight - playerSprite.getHeight() - ARENA_MARGIN
        );

        playerSprite.setPosition(clampedX, clampedY);
        playerHitbox.x = clampedX;
        playerHitbox.y = clampedY;
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
        float screenCenterX = viewport.getWorldWidth() / 2;
        float enemyX = screenCenterX - (currentEnemy.getWidth() / 2);
        float enemyY = arena.y + ARENA_DEFAULT_HEIGHT + 30;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        currentEnemy.draw(spriteBatch, enemyX, enemyY);
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

        // Clear the temporary item usage when running away
        // No items should be lost if the player doesn't win the battle
        temporaryItemUsage.clear();

        backgroundMusic.stop();
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
                if (mainItem.getId().equals(combatItem.getId())) {
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
                if (mainItem.getId().equals(item.getId())) {
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
            font.setColor(i == startIdx + selectedItemIndex ? Color.YELLOW : Color.WHITE);
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
                if (mainItem.getId().equals(combatItem.getId())) {
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
                selectedItemIndex = selectedItemIndex - (selectedItemIndex % ITEMS_PER_ROW); // Keep the same row
                // Ensure the index is valid for the new page
                int itemsOnNewPage = Math.min(ITEMS_PER_PAGE, combatItems.size - (currentItemPage * ITEMS_PER_PAGE));
                selectedItemIndex = Math.min(selectedItemIndex, itemsOnNewPage - 1);
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
                // Move to the rightmost column of the same row
                int itemsOnNewPage = Math.min(ITEMS_PER_PAGE, combatItems.size - (currentItemPage * ITEMS_PER_PAGE));
                int targetColumn = ITEMS_PER_ROW - 1;
                int targetRow = selectedItemIndex / ITEMS_PER_ROW;
                int targetIndex = targetRow * ITEMS_PER_ROW + targetColumn;
                selectedItemIndex = Math.min(targetIndex, itemsOnNewPage - 1);
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
        Array<ConsumableItem> combatItems = player.getInventory().getCombatItems();
        int actualIndex = currentItemPage * ITEMS_PER_PAGE + selectedItemIndex;

        if (actualIndex >= 0 && actualIndex < combatItems.size) {
            ConsumableItem combatItem = combatItems.get(actualIndex);

            // Check if this item still exists in the main inventory
            boolean itemExists = false;
            ConsumableItem mainInventoryItem = null;

            for (ConsumableItem item : player.getInventory().getConsumableItems()) {
                if (item.getId().equals(combatItem.getId())) {
                    itemExists = true;
                    mainInventoryItem = item;
                    break;
                }
            }

            // If the item no longer exists in the main inventory, don't allow using it
            if (!itemExists) {
                setDialogueText("You don't have any " + combatItem.getName() + " left!");
                return;
            }

            // If we've already used all available of this item in this combat, don't allow using more
            int usedCount = temporaryItemUsage.getOrDefault(combatItem.getId(), 0);
            if (usedCount >= mainInventoryItem.getQuantity()) {
                setDialogueText("You don't have any more " + combatItem.getName() + " to use!");
                return;
            }

            // Apply item effect
            String effectMessage = "";

            switch (combatItem.getEffect()) {
                case HEAL_HP:
                    int oldHP = player.getCurrentHP();
                    player.heal(combatItem.getEffectAmount());
                    int healed = player.getCurrentHP() - oldHP;
                    effectMessage = "You used " + combatItem.getName() + ".\nYou recovered " + healed + " HP!";
                    healSound.play(0.15f);
                    break;

                case RESTORE_MP:
                    int oldMP = player.getMP();
                    player.increaseMP(combatItem.getEffectAmount());
                    int restored = player.getMP() - oldMP;
                    effectMessage = "You used " + combatItem.getName() + ".\nYou recovered " + restored + " MP!";
                    manaRegenSound.play(0.15f);
                    break;

                case BUFF_ATK:
                    effectMessage = "You used " + combatItem.getName() + ".\nATK INCREASED!";
                    selectSound.play(0.15f);
                    break;

                case BUFF_DEF:
                    effectMessage = "You used " + combatItem.getName() + ".\n*DEF INCREASED!";
                    selectSound.play(0.15f);
                    break;

                default:
                    effectMessage = "You used " + combatItem.getName() + ".";
                    selectSound.play(0.15f);
                    break;
            }

            // Track temporary item usage
            String itemId = combatItem.getId();
            temporaryItemUsage.put(itemId, temporaryItemUsage.getOrDefault(itemId, 0) + 1);

            // Update player stats
            updateHPTargets();
            updateMPTargets();

            // Set dialogue text to show effect
            setDialogueText(effectMessage);

            // Exit item menu
            showingItemMenu = false;

            // Start enemy turn (delayed)
            startDelayedCombat();
        }
    }

    /**
     * Apply the temporary item usage when combat is successfully completed
     */
    private void applyItemUsage() {
        if (temporaryItemUsage.isEmpty()) return;

        // Get all consumable items
        Array<ConsumableItem> consumables = player.getInventory().getConsumableItems();

        // Apply usage counts
        for (ConsumableItem item : consumables) {
            Integer usageCount = temporaryItemUsage.get(item.getId());
            if (usageCount != null && usageCount > 0) {
                // Reduce item quantity by the number used during combat
                for (int i = 0; i < usageCount; i++) {
                    player.getInventory().useConsumableItem(item);
                }
            }
        }

        // Save the player state after all changes are applied
        player.saveToFile();

        // Clear the temporary usage
        temporaryItemUsage.clear();
    }
}
