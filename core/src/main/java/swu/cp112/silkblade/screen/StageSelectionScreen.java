package swu.cp112.silkblade.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import swu.cp112.silkblade.core.Main;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.entity.enemy.*;
import swu.cp112.silkblade.entity.enemy.silkgod.DemoEnemy;
import swu.cp112.silkblade.screen.transition.ScreenTransition;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Screen for selecting a game stage to play.
 */
public class StageSelectionScreen implements Screen {
    /**
     * Display configuration
     */
    private static final class DisplayConfig {
        // Layout
        static final float TITLE_Y_POSITION = 150;
        static final float PLAYER_INFO_Y = 220;
        static final float STAGES_START_Y = 300;
        static final float STAGES_SPACING_Y = 70;
        static final float STAGES_SPACING_X = 210;
        static final int STAGES_PER_ROW = 5;
        static final int VISIBLE_ROWS = 6;
        static final float LEFT_MARGIN = 180;
        static final float LEFT_STAGE_MARGIN = 130;
        static final float FONT_SCALE = 2f;
        static final float STAGE_FONT_SCALE = 1.5f;
        static final float BOSS_FONT_SCALE = 1.8f;
        static final float TOTAL_STAGES = 50;

        // Colors
        static final Color TITLE_COLOR = Color.WHITE;
        static final Color PLAYER_INFO_COLOR = Color.CYAN;
        static final Color SELECTED_COLOR = Color.YELLOW;
        static final Color AVAILABLE_COLOR = Color.WHITE;
        static final Color LOCKED_COLOR = Color.GRAY;
        static final Color BOSS_COLOR = Color.RED;
        static final Color BOSS_GLOW_COLOR = new Color(1f, 0.5f, 0f, 1f); // Orange glow
        static final Color BACKGROUND_COLOR = Color.BLACK;

        // Boss text animation
        static final float BOSS_ANIMATION_SPEED = 3f; // Speed of the animation
        static final float BOSS_CHAR_OFFSET_MAX = 3f; // Max vertical offset for characters
    }

    /**
     * Menu configuration
     */
    private static final class MenuConfig {
        static final String TITLE = "Select a Stage";
        static final String STAGE_PREFIX = "Stage ";
    }

    /**
     * Audio configuration
     */
    private static final class AudioConfig {
        static final float MUSIC_VOLUME = 0.05f;
        static final String FONT_PATH = "fonts/DTM.fnt";
        static final String MUSIC_PATH = "music/main_menu.mp3";
        static final String SELECT_SOUND_PATH = "sounds/select.wav";
    }

    /**
     * Core components
     */
    private final Game game;
    private final FitViewport viewport;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Player player;

    /**
     * Audio components
     */
    private final Sound selectSound;
    // Keep for future reference but comment out
    // private final Music music;

    /**
     * State
     */
    private int selectedStage = 1;
    private int unlockedStages = 10; // For demonstration, first 10 stages are unlocked
    private int scrollOffset = 0;
    private boolean inputEnabled = true;
    private float animationTime = 0f;
    private GlyphLayout glyphLayout;

    // Static property to track which stage the player is currently challenging
    private static int currentChallengingStage = 0;

    /**
     * Returns the stage that the player is currently challenging
     * @return the current challenging stage number
     */
    public static int getCurrentChallengingStage() {
        return currentChallengingStage;
    }

    /**
     * Sets the stage that the player is currently challenging
     * @param stageNumber the stage number being challenged
     */
    public static void setCurrentChallengingStage(int stageNumber) {
        currentChallengingStage = stageNumber;
        GameLogger.logInfo("Player is now challenging Stage " + stageNumber);
    }

    public StageSelectionScreen(Game game) {
        this.game = game;
        this.viewport = Main.getViewport();
        this.camera = Main.getCamera();

        this.batch = initializeGraphics();
        this.font = initializeFont();
        this.glyphLayout = new GlyphLayout();

        this.selectSound = initializeSound();
        // Keep for future reference but comment out
        // this.music = initializeMusic();

        // Load player data
        this.player = Player.loadFromFile();
        unlockedStages = player.getCurrentStage();
    }

    /**
     * Initialization methods
     */
    private SpriteBatch initializeGraphics() {
        return new SpriteBatch();
    }

    private BitmapFont initializeFont() {
        BitmapFont font = new BitmapFont(Gdx.files.internal(AudioConfig.FONT_PATH));
        font.setColor(DisplayConfig.TITLE_COLOR);
        font.getData().setScale(DisplayConfig.FONT_SCALE);
        return font;
    }

    private Sound initializeSound() {
        return Gdx.audio.newSound(Gdx.files.internal(AudioConfig.SELECT_SOUND_PATH));
    }

    // Keep for future reference but comment out
    // private Music initializeMusic() {
    //     Music music = Gdx.audio.newMusic(Gdx.files.internal(AudioConfig.MUSIC_PATH));
    //     music.setLooping(true);
    //     music.setVolume(AudioConfig.MUSIC_VOLUME);
    //     return music;
    // }

    /**
     * Rendering methods
     */
    @Override
    public void render(float delta) {
        clearScreen();

        // Update animation time
        animationTime += delta;

        drawScreen();
        if (inputEnabled && !ScreenTransition.isTransitioning()) {
            handleInput();
        }
    }

    private void clearScreen() {
        ScreenUtils.clear(DisplayConfig.BACKGROUND_COLOR);
    }

    private void drawScreen() {
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw title
        font.setColor(DisplayConfig.TITLE_COLOR);
        font.draw(batch, MenuConfig.TITLE,
                DisplayConfig.LEFT_MARGIN,
                screenHeight - DisplayConfig.TITLE_Y_POSITION);

        // Draw player info with gold
        font.setColor(DisplayConfig.PLAYER_INFO_COLOR);
        font.draw(batch,  player.getName() + " Level " + player.getLevel() + " Stage: " + player.getCurrentStage(),
                DisplayConfig.LEFT_MARGIN,
                screenHeight - DisplayConfig.PLAYER_INFO_Y);

        // Draw stages grid
        drawStagesGrid(screenWidth, screenHeight);

        batch.end();
    }

    private void drawStagesGrid(float screenWidth, float screenHeight) {
        int visibleRows = DisplayConfig.VISIBLE_ROWS;
        int stagesPerRow = DisplayConfig.STAGES_PER_ROW;

        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < stagesPerRow; col++) {
                int stageNumber = (row + scrollOffset) * stagesPerRow + col + 1;

                if (stageNumber <= DisplayConfig.TOTAL_STAGES) {
                    float x = DisplayConfig.LEFT_STAGE_MARGIN + col * DisplayConfig.STAGES_SPACING_X;
                    float y = screenHeight - DisplayConfig.STAGES_START_Y - row * DisplayConfig.STAGES_SPACING_Y;

                    drawStage(x, y, stageNumber);
                }
            }
        }
    }

    private void drawStage(float x, float y, int stageNumber) {
        boolean isSelected = (stageNumber == selectedStage);
        boolean isUnlocked = (stageNumber <= unlockedStages);
        boolean isBossStage = (stageNumber % 10 == 0); // Every 10th stage is a boss

        // Save original font scale
        float originalScale = font.getData().scaleX;

        // Set appropriate color and scale
        if (isSelected) {
            font.setColor(DisplayConfig.SELECTED_COLOR);
            font.getData().setScale(isBossStage ? DisplayConfig.BOSS_FONT_SCALE : DisplayConfig.STAGE_FONT_SCALE);
        } else if (!isUnlocked) {
            font.setColor(DisplayConfig.LOCKED_COLOR);
            font.getData().setScale(isBossStage ? DisplayConfig.BOSS_FONT_SCALE * 0.9f : DisplayConfig.STAGE_FONT_SCALE);
        } else if (isBossStage) {
            font.setColor(DisplayConfig.BOSS_COLOR);
            font.getData().setScale(DisplayConfig.BOSS_FONT_SCALE);
        } else {
            font.setColor(DisplayConfig.AVAILABLE_COLOR);
            font.getData().setScale(DisplayConfig.STAGE_FONT_SCALE);
        }

        String stageText = MenuConfig.STAGE_PREFIX + stageNumber;
        if (isSelected) {
            stageText = "> " + stageText;
        }

        // Add special effect for boss stages
        if (isBossStage && isUnlocked) {
            drawStaggeredBossText(stageText, x, y);
        } else {
            font.draw(batch, stageText, x, y);
        }

        // Restore original font scale
        font.getData().setScale(originalScale);
    }

    /**
     * Draws boss stage text with a staggering effect where each character moves up and down
     * slightly out of sync with the others
     */
    private void drawStaggeredBossText(String text, float x, float y) {
        float currentX = x;

        // Draw a subtle glow/shadow effect behind text first
        Color originalColor = font.getColor().cpy();
        font.setColor(DisplayConfig.BOSS_GLOW_COLOR);

        // Measure the text width to center the glow
        glyphLayout.setText(font, text);
        float textWidth = glyphLayout.width;

        // Draw the glow slightly offset and larger
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String charStr = String.valueOf(c);

            // Calculate a unique vertical offset for this character based on its position
            float phase = (i * 0.3f + animationTime * DisplayConfig.BOSS_ANIMATION_SPEED) % (2 * (float)Math.PI);
            float vertOffset = (float) Math.sin(phase) * DisplayConfig.BOSS_CHAR_OFFSET_MAX;

            // Measure the width of this character
            glyphLayout.setText(font, charStr);
            float charWidth = glyphLayout.width;

            // Draw the glow version slightly offset
            font.draw(batch, charStr, currentX - 1, y + vertOffset - 1);

            currentX += charWidth;
        }

        // Reset position for the main text
        currentX = x;

        // Now draw the main text with the staggering effect
        font.setColor(originalColor);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String charStr = String.valueOf(c);

            // Calculate a unique vertical offset for this character based on its position
            float phase = (i * 0.3f + animationTime * DisplayConfig.BOSS_ANIMATION_SPEED) % (2 * (float)Math.PI);
            float vertOffset = (float) Math.sin(phase) * DisplayConfig.BOSS_CHAR_OFFSET_MAX;

            // Measure the width of this character
            glyphLayout.setText(font, charStr);
            float charWidth = glyphLayout.width;

            // Draw this character with its own offset
            font.draw(batch, charStr, currentX, y + vertOffset);

            // Move to the next character position
            currentX += charWidth;
        }
    }

    /**
     * Input handling methods
     */
    private void handleInput() {
        handleNavigationInput();
        handleSelectionInput();
    }

    private void handleNavigationInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            moveSelection(-1, 0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            moveSelection(1, 0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            moveSelection(0, -1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            moveSelection(0, 1);
        }
    }

    private void moveSelection(int deltaX, int deltaY) {
        int stagesPerRow = DisplayConfig.STAGES_PER_ROW;
        int row = (selectedStage - 1) / stagesPerRow;
        int col = (selectedStage - 1) % stagesPerRow;

        // Update column
        col = Math.max(0, Math.min(stagesPerRow - 1, col + deltaX));

        // Update row
        row = Math.max(0, Math.min((int)DisplayConfig.TOTAL_STAGES / stagesPerRow, row + deltaY));

        // Calculate new stage number
        int newStage = row * stagesPerRow + col + 1;

        // Make sure it's within bounds
        newStage = Math.max(1, Math.min((int)DisplayConfig.TOTAL_STAGES, newStage));

        // Only play sound if the selection actually changed
        if (newStage != selectedStage) {
            selectedStage = newStage;
            selectSound.play();

            // Adjust scroll if necessary
            int selectedRow = (selectedStage - 1) / stagesPerRow;
            if (selectedRow < scrollOffset) {
                scrollOffset = selectedRow;
            } else if (selectedRow >= scrollOffset + DisplayConfig.VISIBLE_ROWS) {
                scrollOffset = selectedRow - DisplayConfig.VISIBLE_ROWS + 1;
            }
        }
    }

    private void handleSelectionInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectedStage <= unlockedStages) {
                startChallengeForStage(selectedStage);
            } else {
                // Play error sound or feedback for locked stage
                selectSound.play(0.5f);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // Return to main menu or save selection
            game.setScreen(new ScreenTransition(
                game,
                this,
                new MainNavigationScreen(game),
                ScreenTransition.TransitionType.CROSS_FADE
            ));
        }
    }

    /**
     * Processes stage selection and initiates combat with the appropriate enemy
     * @param stageNumber the selected stage number
     */
    private void startChallengeForStage(int stageNumber) {
        // We need to set the challenging stage for patterns to access it
        currentChallengingStage = stageNumber;

        if (shouldEncounterSecretBoss(stageNumber)) {
            GameLogger.logInfo("Player encountered the secret boss instead of normal enemy!");
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, new DemoEnemy()),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        // Use SilkWraith for stages 1-9
        } else if (stageNumber >= 1 && stageNumber <= 9) {
            // Create a SilkWraith with the appropriate stage number
            SilkWraith wraith = new SilkWraith(stageNumber);
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, wraith),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        // Use SilkGuardian for stage 10 (boss stage)
        } else if (stageNumber == 10) {
            // Create the stage 10 boss - Silk Guardian
            SilkGuardian guardian = new SilkGuardian();
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, guardian),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        // Use SilkWeaver for stages 11-19
        } else if (stageNumber >= 11 && stageNumber <= 19) {
            // Create a SilkWeaver with the appropriate stage number
            SilkWeaver weaver = new SilkWeaver(stageNumber);
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, weaver),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        // Use GoldenCocoon for stage 20 (boss stage)
        } else if (stageNumber == 20) {
            // Create the stage 20 boss - Golden Cocoon
            GoldenCocoon cocoon = new GoldenCocoon();
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, cocoon),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        // Use SilkCicada for stages 21-29
        } else if (stageNumber >= 21 && stageNumber <= 29) {
            // Create a SilkCicada with the appropriate stage number
            SilkCicada cicada = new SilkCicada(stageNumber);
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, cicada),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        } else if (stageNumber == 30) {
            // Create the stage 30 boss - Threadmancer
            Threadmancer thread = new Threadmancer();
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, thread),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        // Use SpiritOfTheLoom for stages 31-39
        } else if (stageNumber >= 31 && stageNumber <= 39) {
            // Create a SpiritOfTheLoom with the appropriate stage number
            SpiritOfTheLoom loom = new SpiritOfTheLoom(stageNumber);
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, loom),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        // Use CrimsonSericulture for stage 40 (boss stage)
        } else if (stageNumber == 40) {
            // Create the stage 40 boss - Crimson Sericulture
            CrimsonSericulture sericulture = new CrimsonSericulture();
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, sericulture),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        // Use HundredSilkOgre for stages 41-49
        } else if (stageNumber >= 41 && stageNumber <= 49) {
            // Create a HundredSilkOgre with the appropriate stage number
            HundredSilkOgre ogre = new HundredSilkOgre(stageNumber);
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, ogre),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        // Use SilkRevenant for stage 50 (boss stage)
        } else if (stageNumber == 50) {
            // Create the stage 50 boss - Silk Revenant
            SilkRevenant revenant = new SilkRevenant();
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, revenant),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        } else {
            // Use DemoEnemy for other stages (temporary)
            game.setScreen(new ScreenTransition(
                game,
                this,
                new CombatScene(game, new DemoEnemy()),
                ScreenTransition.TransitionType.FADE_TO_WHITE
            ));
        }
    }

    /**
     * Determines if the player should encounter the secret boss (DemoEnemy) instead of the regular stage enemy
     * Only applies when replaying stages the player has already cleared, with a 1% chance
     * @param stageNumber the stage being played
     * @return true if the player should encounter the secret boss
     */
    private boolean shouldEncounterSecretBoss(int stageNumber) {
        // Only apply to stages the player has already cleared (current stage > selected stage)
        if (player.getCurrentStage() > stageNumber) {
            // 1% chance to encounter secret boss when replaying a cleared stage
            float randomValue = (float) Math.random();
            return randomValue <= 0.01f; // 1% chance
        }
        return false;
    }

    /**
     * Screen lifecycle methods
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        // Keep for future reference but comment out
        // music.dispose();
        selectSound.dispose();
    }

    @Override
    public void hide() {
        // Keep for future reference but comment out
        // if (music != null) {
        //     music.stop();
        // }

        // No need to stop the shared music
    }

    @Override
    public void show() {
        // If returning from combat, ensure player state is up to date
        if (CombatScene.returningFromCombat) {
            // Reload the player to ensure we have the latest state
            // This ensures that any state restoration (like running away or dying)
            // is properly reflected in the stage selection screen
            unlockedStages = player.getCurrentStage();
            
            // Set the selected stage to the stage the player was just in
            if (currentChallengingStage > 0) {
                selectedStage = currentChallengingStage;
                
                // Adjust scroll if necessary to ensure the selected stage is visible
                int stagesPerRow = DisplayConfig.STAGES_PER_ROW;
                int selectedRow = (selectedStage - 1) / stagesPerRow;
                if (selectedRow < scrollOffset) {
                    scrollOffset = selectedRow;
                } else if (selectedRow >= scrollOffset + DisplayConfig.VISIBLE_ROWS) {
                    scrollOffset = selectedRow - DisplayConfig.VISIBLE_ROWS + 1;
                }
            }
    
            // Restart music from beginning
            swu.cp112.silkblade.core.Main.restartBackgroundMusic();

            // Reset flag
            CombatScene.returningFromCombat = false;
        } else {
            // Otherwise just make sure it's playing
            swu.cp112.silkblade.core.Main.resumeBackgroundMusic();
        }
    }

    // Unused Screen interface methods
    @Override public void pause() {}
    @Override public void resume() {}
}
