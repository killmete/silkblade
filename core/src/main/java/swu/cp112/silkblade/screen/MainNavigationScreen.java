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
import swu.cp112.silkblade.screen.transition.ScreenTransition;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Main navigation screen to choose where to go next.
 */
public class MainNavigationScreen implements Screen {
    /**
     * Display configuration
     */
    private static final class DisplayConfig {
        // Layout
        static final float TITLE_Y_POSITION = 150;
        static final float PLAYER_INFO_Y = 220;
        static final float OPTIONS_START_Y = 350;
        static final float OPTIONS_SPACING = 70;
        static final float LEFT_MARGIN = 180;
        static final float FONT_SCALE = 2f;

        // Colors
        static final Color TITLE_COLOR = Color.WHITE;
        static final Color PLAYER_INFO_COLOR = Color.CYAN;
        static final Color SELECTED_COLOR = Color.YELLOW;
        static final Color DEFAULT_COLOR = Color.WHITE;
        static final Color BACKGROUND_COLOR = Color.BLACK;
    }

    /**
     * Menu configuration
     */
    private static final class MenuConfig {
        static final String TITLE = "Where would you like to go?";
        static final String[] OPTIONS = {
            "Story Mode",
            "Shop",
            "Inventory",
            "Back"
        };
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
    private int selectedIndex = 0;
    private boolean inputEnabled = true;

    public MainNavigationScreen(Game game) {
        this.game = game;
        this.viewport = Main.getViewport();
        this.camera = Main.getCamera();

        this.batch = new SpriteBatch();
        this.font = new BitmapFont(Gdx.files.internal(AudioConfig.FONT_PATH));
        font.setColor(DisplayConfig.DEFAULT_COLOR);
        font.getData().setScale(DisplayConfig.FONT_SCALE);

        this.selectSound = Gdx.audio.newSound(Gdx.files.internal(AudioConfig.SELECT_SOUND_PATH));
        // Keep for future reference but comment out
        // this.music = Gdx.audio.newMusic(Gdx.files.internal(AudioConfig.MUSIC_PATH));
        // this.music.setLooping(true);
        // this.music.setVolume(AudioConfig.MUSIC_VOLUME);

        // Load player data
        this.player = Player.loadFromFile();
    }

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

        // Draw player info
        font.setColor(DisplayConfig.PLAYER_INFO_COLOR);
        font.draw(batch, player.getName() + " - Level " + player.getLevel() + " - Gold: " + player.getGold(),
                DisplayConfig.LEFT_MARGIN,
                screenHeight - DisplayConfig.PLAYER_INFO_Y);

        // Draw menu options
        for (int i = 0; i < MenuConfig.OPTIONS.length; i++) {
            boolean isSelected = (i == selectedIndex);
            font.setColor(isSelected ? DisplayConfig.SELECTED_COLOR : DisplayConfig.DEFAULT_COLOR);
            String option = (isSelected ? "> " : "  ") + MenuConfig.OPTIONS[i];
            float y = screenHeight - DisplayConfig.OPTIONS_START_Y - i * DisplayConfig.OPTIONS_SPACING;
            if(i != MenuConfig.OPTIONS.length - 1) {
                font.draw(batch, option, DisplayConfig.LEFT_MARGIN, y);
            } else {
                font.draw(batch, option, DisplayConfig.LEFT_MARGIN, y - 60);
            }
        }

        batch.end();
    }

    /**
     * Input handling methods
     */
    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            moveSelection(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            moveSelection(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            selectOption(selectedIndex);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goBack();
        }
    }

    private void moveSelection(int direction) {
        selectedIndex = (selectedIndex + direction + MenuConfig.OPTIONS.length) % MenuConfig.OPTIONS.length;
        selectSound.play();
    }

    private void selectOption(int index) {
        inputEnabled = false;
        selectSound.play();

        switch (index) {
            case 0: // Stages
                goToStageSelection();
                break;
            case 1: // Shop
                goToShop();
                break;
            case 2: // Inventory
                goToInventory();
                break;
            case 3: // Back
                goBack();
                break;
        }
    }

    private void goToStageSelection() {
        game.setScreen(new ScreenTransition(
            game,
            this,
            new StageSelectionScreen(game),
            ScreenTransition.TransitionType.CROSS_FADE
        ));
    }

    private void goToInventory() {
        game.setScreen(new ScreenTransition(
            game,
            this,
            new InventoryScreen(game),
            ScreenTransition.TransitionType.CROSS_FADE
        ));
    }

    private void goToShop() {
        game.setScreen(new ScreenTransition(
            game,
            this,
            new ShopScreen(game),
            ScreenTransition.TransitionType.CROSS_FADE
        ));
    }

    private void goBack() {
        game.setScreen(new ScreenTransition(
            game,
            this,
            new SaveFileSelectionScreen(game),
            ScreenTransition.TransitionType.CROSS_FADE
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

        // Make sure the shared music is playing
        swu.cp112.silkblade.core.Main.resumeBackgroundMusic();
    }

    // Unused Screen interface methods
    @Override public void pause() {}
    @Override public void resume() {}
}
