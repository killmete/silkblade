package swu.cp112.silkblade.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import swu.cp112.silkblade.core.Main;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.entity.enemy.SilkWraith;
import swu.cp112.silkblade.entity.enemy.SilkWeaver;
import swu.cp112.silkblade.entity.enemy.SilkCicada;
import swu.cp112.silkblade.entity.enemy.SpiritOfTheLoom;
import swu.cp112.silkblade.entity.enemy.HundredSilkOgre;
import swu.cp112.silkblade.util.GameLogger;
import swu.cp112.silkblade.screen.transition.ScreenTransition;

/**
 * Screen that shows the death animation and game over message with options to retry or quit
 */
public class GameOverScreen implements Screen {
    // Animation constants
    private static final float HEART_LINGER_DURATION = 1.5f;
    private static final float EXPLOSION_FRAME_DURATION = 0.05f;
    private static final float GAME_OVER_FADE_IN_DURATION = 2.0f; // Faster fade-in (was 3.0f)
    private static final float OPTION_FADE_IN_DURATION = 0.3f; // Faster fade-in (was 0.5f)
    private static final float HEART_PULSE_SPEED = 15.0f;
    private static final float HEART_MAX_SCALE = 1.5f;
    private static final float YOU_DIED_DELAY = 0.5f; // Shorter delay (was 1.0f)

    // Screen shake constants
    private static final float SHAKE_DURATION = 0.8f;
    private static final float SHAKE_INTENSITY = 15.0f;
    private static final float SHAKE_FALLOFF = 0.9f;

    // UI positioning constants
    private static final float TITLE_Y_POSITION = 250f; // Move up (was 300f)
    private static final float OPTIONS_START_Y = 450f; // Move down (was 400f)
    private static final float OPTIONS_SPACING_Y = 70f;
    private static final float FONT_SCALE_TITLE = 4.0f;
    private static final float FONT_SCALE_OPTIONS = 2.0f;

    // Color constants for Dark Souls style
    private static final Color YOU_DIED_COLOR = new Color(0.7f, 0.0f, 0.0f, 1.0f); // Dark red

    // Core components
    private final Game game;
    private final FitViewport viewport;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;

    // Death animation resources
    private final Texture heartTexture;
    private final Texture whiteTexture;
    private final TextureAtlas explosionAtlas;
    private final Animation<TextureRegion> explosionAnimation;
    private final Sound explosionSound;
    private final Sound selectSound;
    private final Sound youDiedSound;

    // Animation state
    private float heartLingerTimer = 0f;
    private float explosionTimer = 0f;
    private float gameOverFadeTimer = 0f;
    private float optionFadeTimer = 0f;
    private float flashAlpha = 0f;
    private boolean explosionStarted = false;
    private boolean explosionSoundPlayed = false;
    private boolean showGameOver = false;
    private boolean showOptions = false;
    private boolean youDiedSoundPlayed = false;
    private float youDiedDelay = 0f;

    // Player position when death occurred
    private float deathX;
    private float deathY;

    // Menu state
    private int selectedOption = 0;
    private final String[] options = {"RETRY", "GIVE UP"};

    // Enemy reference for retry
    private final Enemy currentEnemy;

    // Screen shake state
    private float shakeTimer = 0f;
    private float shakeAmount = 0f;
    private float origCameraX;
    private float origCameraY;

    /**
     * Creates a new GameOverScreen with the position where the player died
     */
    public GameOverScreen(Game game, float deathX, float deathY, Enemy currentEnemy) {
        this.game = game;
        this.deathX = deathX;
        this.deathY = deathY;

        // Clone or store the enemy class for retry, not the instance itself
        // This ensures we get a fresh enemy when retrying
        this.currentEnemy = currentEnemy;

        // Initialize core components
        viewport = Main.getViewport();
        camera = Main.getCamera();
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("fonts/DTM.fnt"));

        // Load textures and animations
        heartTexture = new Texture("player.png");

        // Create a white pixel texture for flashing effect
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();
        explosionAtlas = new TextureAtlas(Gdx.files.internal("assets/atlas/explosion_atlas.atlas"));
        explosionAnimation = new Animation<>(EXPLOSION_FRAME_DURATION,
                explosionAtlas.findRegions("explosion_frame"));

        // Load sounds
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/death_explosion.wav"));
        selectSound = Gdx.audio.newSound(Gdx.files.internal("sounds/select.wav"));
        youDiedSound = Gdx.audio.newSound(Gdx.files.internal("sounds/you_died.wav"));
    }

    @Override
    public void render(float delta) {
        // Update screen shake
        updateShake(delta);

        // Clear screen to pure black
        ScreenUtils.clear(0, 0, 0, 1);

        // Update animation timers
        updateAnimationState(delta);

        // Draw elements
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw current animation phase
        if (!explosionStarted) {
            // Phase 1: Draw heart in death position with pulsing effect
            batch.setColor(Color.RED);

            // Calculate pulse effect (gets faster as we approach explosion)
            float pulseRatio = heartLingerTimer / HEART_LINGER_DURATION; // 0 to 1
            float pulseSpeed = HEART_PULSE_SPEED * (0.5f + pulseRatio * 2.0f); // Speed increases
            float pulse = 1.0f + (float)Math.sin(heartLingerTimer * pulseSpeed) * 0.2f * pulseRatio;

            // Add a glow effect that intensifies before explosion
            float glowScale = 1.0f + pulseRatio * 0.5f;
            float alpha = 0.3f + pulseRatio * 0.4f;
            batch.setColor(1f, 0.2f, 0.2f, alpha);

            // Draw glow around the heart
            float glowSize = 30f * glowScale;
            float glowX = deathX - (glowSize - 18f) / 2f;
            float glowY = deathY - (glowSize - 18f) / 2f;
            batch.draw(heartTexture, glowX, glowY, glowSize, glowSize);

            // Calculate heart size with pulse
            float heartSize = 18f * pulse;
            float heartX = deathX - (heartSize - 18f) / 2f;
            float heartY = deathY - (heartSize - 18f) / 2f;

            // Draw actual heart
            batch.setColor(Color.RED);
            batch.draw(heartTexture, heartX, heartY, heartSize, heartSize);
        } else if (explosionTimer < explosionAnimation.getAnimationDuration()) {
            // Phase 2: Draw explosion animation
            TextureRegion currentFrame = explosionAnimation.getKeyFrame(explosionTimer, false);

            // Calculate position to center explosion on heart
            float heartWidth = 18f;
            float heartHeight = 18f;
            float explosionWidth = 250f;  // Increased from 142f for more dramatic effect
            float explosionHeight = 350f; // Increased from 200f for more dramatic effect

            // Center the explosion on the heart
            float explosionX = deathX - ((explosionWidth - heartWidth) / 2);
            float explosionY = deathY - ((explosionHeight - heartHeight) / 2);

            // Draw glow behind explosion
            float glowIntensity = 1.0f - (explosionTimer / explosionAnimation.getAnimationDuration());
            batch.setColor(1f, 0.7f, 0.2f, glowIntensity * 0.7f);
            float glowWidth = explosionWidth * 1.2f;
            float glowHeight = explosionHeight * 1.2f;
            float glowX = explosionX - (glowWidth - explosionWidth) / 2f;
            float glowY = explosionY - (glowHeight - explosionHeight) / 2f;
            batch.draw(currentFrame, glowX, glowY, glowWidth, glowHeight);

            // Draw actual explosion
            batch.setColor(Color.WHITE);
            batch.draw(currentFrame, explosionX, explosionY, explosionWidth, explosionHeight);
        }

        // Draw "YOU DIED" text with fade-in
        if (showGameOver) {
            // Calculate alpha using a smooth curve for the Dark Souls style
            // Start slow, then accelerate, then slow down again
            float fadeProgress = Math.min(1.0f, gameOverFadeTimer / GAME_OVER_FADE_IN_DURATION);

            // Use a sigmoid-like curve for more dramatic fade effect
            // This gives a longer lingering faint appearance before becoming fully visible
            float alpha = (float)(1.0f / (1.0f + Math.exp(-12 * (fadeProgress - 0.5f))));

            // Set up large, bold font for YOU DIED text
            font.getData().setScale(FONT_SCALE_TITLE);

            // Center the text
            String gameOverText = "YOU DIED";
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, gameOverText);
            float titleX = (viewport.getWorldWidth() - layout.width) / 2;
            float titleY = (viewport.getWorldHeight() + layout.height) / 2; // Center vertically

            // Draw text with a dark blood red color
            Color textColor = YOU_DIED_COLOR.cpy();
            textColor.a = alpha;
            font.setColor(textColor);
            font.draw(batch, gameOverText, titleX, titleY);
        }

        // Draw menu options with fade-in
        if (showOptions) {
            float alpha = Math.min(1.0f, optionFadeTimer / OPTION_FADE_IN_DURATION);
            font.getData().setScale(FONT_SCALE_OPTIONS);

            for (int i = 0; i < options.length; i++) {
                boolean isSelected = (i == selectedOption);
                Color textColor = isSelected ? Color.YELLOW : Color.WHITE;
                String displayText = isSelected ? "> " + options[i] + " <" : options[i];

                // Use GlyphLayout for better text measurement
                com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, displayText);
                float optionX = (viewport.getWorldWidth() - layout.width) / 2;
                float optionY = viewport.getWorldHeight() - OPTIONS_START_Y - (i * OPTIONS_SPACING_Y);

                // Draw text
                font.setColor(textColor.r, textColor.g, textColor.b, alpha);
                font.draw(batch, displayText, optionX, optionY);
            }
        }

        // Draw flash overlay
        if (flashAlpha > 0) {
            batch.setColor(1, 1, 1, flashAlpha);
            // Draw a white rectangle over the entire screen
            batch.draw(whiteTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        }

        batch.end();

        // Handle input if options are showing and animation is complete
        if (showOptions && isAnimationComplete()) {
            handleInput();
        }
    }

    /**
     * Updates screen shake effect
     */
    private void updateShake(float delta) {
        if (shakeTimer > 0) {
            shakeTimer -= delta;
            if (shakeTimer <= 0) {
                // Reset camera when shake is complete
                camera.position.x = origCameraX;
                camera.position.y = origCameraY;
                camera.update();
            } else {
                // Apply shake effect
                shakeAmount = shakeAmount * SHAKE_FALLOFF + (1f - SHAKE_FALLOFF) * SHAKE_INTENSITY;
                float offsetX = (float)(Math.random() * 2 - 1) * shakeAmount;
                float offsetY = (float)(Math.random() * 2 - 1) * shakeAmount;
                camera.position.x = origCameraX + offsetX;
                camera.position.y = origCameraY + offsetY;
                camera.update();
            }
        }
    }

    /**
     * Start screen shake effect
     */
    private void startShake() {
        shakeTimer = SHAKE_DURATION;
        shakeAmount = SHAKE_INTENSITY;
    }

    /**
     * Updates all animation timers and states
     */
    private void updateAnimationState(float delta) {
        if (!explosionStarted) {
            // Phase 1: Wait for heart to linger

            // Add dramatic slow-motion effect as we approach the explosion
            float slowdownFactor = 1.0f;
            float threshold = HEART_LINGER_DURATION * 0.8f;

            if (heartLingerTimer > threshold) {
                // Calculate slowdown factor (gradually slows down to 0.3x speed)
                float progress = (heartLingerTimer - threshold) / (HEART_LINGER_DURATION - threshold);
                slowdownFactor = 1.0f - (progress * 0.7f);

                // Apply slowdown to delta
                delta *= slowdownFactor;
            }

            heartLingerTimer += delta;
            if (heartLingerTimer >= HEART_LINGER_DURATION) {
                explosionStarted = true;
                // Start screen shake when explosion begins
                startShake();
                // Start flash effect
                flashAlpha = 1.0f;
            }
        } else {
            // Phase 2: Play explosion animation
            explosionTimer += delta;

            // Play explosion sound once at start of explosion
            if (!explosionSoundPlayed) {
                explosionSound.play(0.4f);
                explosionSoundPlayed = true;
            }

            // Add another shake in the middle of the explosion for extra dramatic effect
            if (explosionTimer > explosionAnimation.getAnimationDuration() * 0.4f &&
                explosionTimer < explosionAnimation.getAnimationDuration() * 0.6f) {
                if (shakeTimer <= 0.1f) {
                    startShake();
                }
            }

            // Fade out flash effect
            if (flashAlpha > 0) {
                flashAlpha = Math.max(0, flashAlpha - delta * 2.0f);
            }

            // Check if explosion animation is complete
            if (explosionTimer >= explosionAnimation.getAnimationDuration()) {
                // Start the YOU DIED delay countdown after explosion
                youDiedDelay += delta;

                // Wait for the delay before showing game over text
                if (youDiedDelay >= YOU_DIED_DELAY && !showGameOver) {
                    showGameOver = true;
                    // Play the YOU DIED sound effect when the text begins to appear
                    if (!youDiedSoundPlayed) {
                        long soundId = youDiedSound.play(0.6f);
                        youDiedSound.setPitch(soundId, 1.1f);
                        youDiedSoundPlayed = true;
                    }
                }
            }

            // Update game over fade timer if showing
            if (showGameOver) {
                gameOverFadeTimer += delta * 0.8f; // Faster fade-in (was 0.5f)

                // After game over text has faded in, show options
                if (gameOverFadeTimer >= GAME_OVER_FADE_IN_DURATION * 0.6f && !showOptions) { // Show options sooner
                    showOptions = true;
                }

                // Update options fade timer if showing
                if (showOptions) {
                    optionFadeTimer += delta * 1.5f; // Faster fade-in for options
                }
            }
        }
    }

    /**
     * Handles user input for menu navigation
     */
    private void handleInput() {
        // Only allow input when animation is complete
        if (!isAnimationComplete()) {
            return;
        }

        // Option navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedOption = (selectedOption - 1 + options.length) % options.length;
            selectSound.play(0.5f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedOption = (selectedOption + 1) % options.length;
            selectSound.play(0.5f);
        }

        // Option selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.Z) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {

            selectOption(selectedOption);
        }
    }

    /**
     * Checks if the animation sequence is complete and the menu can be interacted with
     */
    private boolean isAnimationComplete() {
        return showOptions && optionFadeTimer >= OPTION_FADE_IN_DURATION;
    }

    /**
     * Handles the selection of a menu option
     */
    private void selectOption(int option) {
        switch (option) {
            case 0: // RETRY
                GameLogger.logInfo("Player chose to retry the fight");
                selectSound.play(0.3f);
                retry();
                break;

            case 1: // GIVE UP
                GameLogger.logInfo("Player chose to give up");
                selectSound.play(0.3f);
                giveUp();
                break;
        }
    }

    /**
     * Restarts the current combat with a fresh instance of the same enemy type
     */
    private void retry() {
        if (currentEnemy != null) {
            try {
                // Get the current player for reference only (no need to modify it here)
                // The CombatScene will restore the player from the snapshot taken at combat start
                swu.cp112.silkblade.entity.combat.Player player = swu.cp112.silkblade.entity.combat.Player.loadFromFile();

                // Create a brand new instance of the same enemy class
                Enemy freshEnemy;

                // Special handling for SilkWraith which requires a stage parameter
                if (currentEnemy instanceof swu.cp112.silkblade.entity.enemy.SilkWraith) {
                    // Get stage from the current enemy
                    int stage = ((swu.cp112.silkblade.entity.enemy.SilkWraith) currentEnemy).getStage();
                    freshEnemy = new swu.cp112.silkblade.entity.enemy.SilkWraith(stage);

                    // Make sure the challenging stage is set correctly for this retry
                    StageSelectionScreen.setCurrentChallengingStage(stage);
                    GameLogger.logInfo("Created fresh SilkWraith for stage: " + stage);
                }
                // Special handling for SilkWeaver which requires a stage parameter
                else if (currentEnemy instanceof swu.cp112.silkblade.entity.enemy.SilkWeaver) {
                    // Get stage from the current enemy
                    int stage = ((swu.cp112.silkblade.entity.enemy.SilkWeaver) currentEnemy).getStage();
                    freshEnemy = new swu.cp112.silkblade.entity.enemy.SilkWeaver(stage);

                    // Make sure the challenging stage is set correctly for this retry
                    StageSelectionScreen.setCurrentChallengingStage(stage);
                    GameLogger.logInfo("Created fresh SilkWeaver for stage: " + stage);
                }
                // Special handling for SilkCicada which requires a stage parameter
                else if (currentEnemy instanceof swu.cp112.silkblade.entity.enemy.SilkCicada) {
                    // Get stage from the current enemy
                    int stage = ((swu.cp112.silkblade.entity.enemy.SilkCicada) currentEnemy).getStage();
                    freshEnemy = new swu.cp112.silkblade.entity.enemy.SilkCicada(stage);

                    // Make sure the challenging stage is set correctly for this retry
                    StageSelectionScreen.setCurrentChallengingStage(stage);
                    GameLogger.logInfo("Created fresh SilkCicada for stage: " + stage);
                }
                // Special handling for SpiritOfTheLoom which requires a stage parameter
                else if (currentEnemy instanceof swu.cp112.silkblade.entity.enemy.SpiritOfTheLoom) {
                    // Get stage from the current enemy
                    int stage = ((swu.cp112.silkblade.entity.enemy.SpiritOfTheLoom) currentEnemy).getStage();
                    freshEnemy = new swu.cp112.silkblade.entity.enemy.SpiritOfTheLoom(stage);

                    // Make sure the challenging stage is set correctly for this retry
                    StageSelectionScreen.setCurrentChallengingStage(stage);
                    GameLogger.logInfo("Created fresh SpiritOfTheLoom for stage: " + stage);
                }
                // Special handling for HundredSilkOgre which requires a stage parameter
                else if (currentEnemy instanceof swu.cp112.silkblade.entity.enemy.HundredSilkOgre) {
                    // Get stage from the current enemy
                    int stage = ((swu.cp112.silkblade.entity.enemy.HundredSilkOgre) currentEnemy).getStage();
                    freshEnemy = new swu.cp112.silkblade.entity.enemy.HundredSilkOgre(stage);

                    // Make sure the challenging stage is set correctly for this retry
                    StageSelectionScreen.setCurrentChallengingStage(stage);
                    GameLogger.logInfo("Created fresh HundredSilkOgre for stage: " + stage);
                } else {
                    // For other enemy types, use no-args constructor
                    freshEnemy = currentEnemy.getClass().getDeclaredConstructor().newInstance();
                    GameLogger.logInfo("Created fresh enemy of type: " + freshEnemy.getClass().getSimpleName());
                }

                // Create a new combat scene with the fresh enemy
                game.setScreen(new ScreenTransition(
                    game,
                    this,
                    new CombatScene(game, freshEnemy),
                    ScreenTransition.TransitionType.FADE_TO_WHITE
                ));
            } catch (Exception e) {
                GameLogger.logError("Failed to create fresh enemy instance", e);
                // Fallback to stage selection if we can't create a new enemy
                game.setScreen(new ScreenTransition(
                    game,
                    this,
                    new StageSelectionScreen(game),
                    ScreenTransition.TransitionType.FADE_TO_WHITE
                ));
            }
        } else {
            // Fallback if enemy reference is lost
            game.setScreen(new ScreenTransition(
                game,
                this,
                new StageSelectionScreen(game),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        }
    }

    /**
     * Quits to the main menu
     */
    private void giveUp() {
        // The player state has already been restored when the player died in combat
        // Just transition back to the main menu

        // Explicitly set the flag to indicate we're returning from combat
        CombatScene.returningFromCombat = true;

        game.setScreen(new ScreenTransition(
            game,
            this,
            new StageSelectionScreen(game),
            ScreenTransition.TransitionType.CROSS_FADE
        ));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
    }

    @Override
    public void show() {
        // Reset animation timers when screen is shown
        heartLingerTimer = 0f;
        explosionTimer = 0f;
        gameOverFadeTimer = 0f;
        optionFadeTimer = 0f;
        explosionStarted = false;
        explosionSoundPlayed = false;
        showGameOver = false;
        showOptions = false;
        flashAlpha = 0f;
        selectedOption = 0;
        youDiedSoundPlayed = false;
        youDiedDelay = 0f;

        // Initialize camera position
        origCameraX = camera.position.x;
        origCameraY = camera.position.y;
        shakeTimer = 0f;
        shakeAmount = 0f;
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        heartTexture.dispose();
        whiteTexture.dispose();
        explosionAtlas.dispose();
        explosionSound.dispose();
        selectSound.dispose();
        youDiedSound.dispose();
    }
}
