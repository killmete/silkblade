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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import swu.cp112.silkblade.core.Main;
import swu.cp112.silkblade.entity.combat.Player;
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
        static final float TOTAL_STAGES = 50;

        // Colors
        static final Color TITLE_COLOR = Color.WHITE;
        static final Color PLAYER_INFO_COLOR = Color.CYAN;
        static final Color SELECTED_COLOR = Color.YELLOW;
        static final Color AVAILABLE_COLOR = Color.WHITE;
        static final Color LOCKED_COLOR = Color.GRAY;
        static final Color BOSS_COLOR = Color.RED;
        static final Color BACKGROUND_COLOR = Color.BLACK;
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

    public StageSelectionScreen(Game game) {
        this.game = game;
        this.viewport = Main.getViewport();
        this.camera = Main.getCamera();

        this.batch = initializeGraphics();
        this.font = initializeFont();

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

        // Use smaller scale for stages
        font.getData().setScale(DisplayConfig.STAGE_FONT_SCALE);

        // Set appropriate color
        if (isSelected) {
            font.setColor(DisplayConfig.SELECTED_COLOR);
        } else if (!isUnlocked) {
            font.setColor(DisplayConfig.LOCKED_COLOR);
        } else if (isBossStage) {
            font.setColor(DisplayConfig.BOSS_COLOR);
        } else {
            font.setColor(DisplayConfig.AVAILABLE_COLOR);
        }

        String stageText = MenuConfig.STAGE_PREFIX + stageNumber;
        if (isSelected) {
            stageText = "> " + stageText;
        }

        font.draw(batch, stageText, x, y);

        // Restore original font scale
        font.getData().setScale(originalScale);
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
                startStage(selectedStage);
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

    private void startStage(int stageNumber) {
        GameLogger.logInfo("Starting stage: " + stageNumber);

        // For now, all stages use the demo enemy
        // In a full implementation, enemies would be selected based on the stage
        inputEnabled = false;
        game.setScreen(new ScreenTransition(
            game,
            this,
            new CombatScene(game, new DemoEnemy()),
            ScreenTransition.TransitionType.FADE_TO_WHITE
        ));
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
        // Keep for future reference but comment out
        // if (music != null && !music.isPlaying()) {
        //     music.play();
        // }
        
        // If returning from combat, restart music from beginning
        if (CombatScene.returningFromCombat) {
            swu.cp112.silkblade.core.Main.restartBackgroundMusic();
            CombatScene.returningFromCombat = false; // Reset flag
        } else {
            // Otherwise just make sure it's playing
            swu.cp112.silkblade.core.Main.resumeBackgroundMusic();
        }
    }

    // Unused Screen interface methods
    @Override public void pause() {}
    @Override public void resume() {}
}
