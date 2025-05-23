package swu.cp112.silkblade.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import swu.cp112.silkblade.screen.transition.ScreenTransition;
import swu.cp112.silkblade.core.Main;

/**
 * Handles the main menu screen of the game, including navigation and rendering.
 */
public class MainMenuScreen implements Screen {
    /**
     * Display configuration
     */
    private static final class DisplayConfig {
        // Layout
        static final float TITLE_Y_POSITION = 50;
        static final float TITLE_SCALE = 0.4f;
        static final float MENU_START_Y = 450;
        static final float MENU_ITEM_SPACING = 50; // Smaller spacing
        static final float FONT_SCALE = 1.5f; // Smaller font

        // Colors
        static final Color SELECTED_COLOR = Color.YELLOW;
        static final Color DEFAULT_COLOR = Color.WHITE;
        static final Color BACKGROUND_COLOR = Color.BLACK;
    }

    /**
     * Menu configuration
     */
    private static final class MenuConfig {
        static final String[] OPTIONS = {"START", "OPTIONS", "QUIT"};
        static final String SELECTED_PREFIX = "> ";
        static final String UNSELECTED_PREFIX = "  ";
    }

    /**
     * Asset configuration
     */
    private static final class AssetConfig {
        static final float MUSIC_VOLUME = 0.05f;
        static final String FONT_PATH = "fonts/DTM.fnt";
        static final String MUSIC_PATH = "music/main_menu.mp3";
        static final String SELECT_SOUND_PATH = "sounds/select.wav";
        static final String TITLE_IMAGE_PATH = "title.png";
    }

    /**
     * Core components
     */
    private final Game game;
    private final FitViewport viewport;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Texture titleTexture;

    /**
     * Audio components
     */
    private final Sound selectSound;
    // Keep for future reference but comment out
    // private final Music music;

    /**
     * State
     */
    private int selectedIndex = -1;
    private boolean inputEnabled = true;

    public MainMenuScreen(Game game) {
        this.game = game;
        this.viewport = Main.getViewport();
        this.camera = Main.getCamera();

        this.batch = initializeGraphics();
        this.font = initializeFont();
        this.titleTexture = new Texture(Gdx.files.internal(AssetConfig.TITLE_IMAGE_PATH));

        this.selectSound = initializeSound();
        // Keep for future reference but comment out
        // this.music = initializeMusic();
    }

    /**
     * Initialization methods
     */
    private SpriteBatch initializeGraphics() {
        return new SpriteBatch();
    }

    private BitmapFont initializeFont() {
        BitmapFont font = new BitmapFont(Gdx.files.internal(AssetConfig.FONT_PATH));
        font.setColor(DisplayConfig.DEFAULT_COLOR);
        font.getData().setScale(DisplayConfig.FONT_SCALE);
        return font;
    }

    private Sound initializeSound() {
        return Gdx.audio.newSound(Gdx.files.internal(AssetConfig.SELECT_SOUND_PATH));
    }

    // Keep for future reference but comment out
    // private Music initializeMusic() {
    //     Music music = Gdx.audio.newMusic(Gdx.files.internal(AssetConfig.MUSIC_PATH));
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
        drawMenu();
        if (inputEnabled && !ScreenTransition.isTransitioning()) {
            handleInput();
        }
    }

    private void clearScreen() {
        ScreenUtils.clear(DisplayConfig.BACKGROUND_COLOR);
    }

    private void drawMenu() {
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawTitle(screenWidth, screenHeight);
        drawMenuOptions(screenWidth, screenHeight);
        batch.end();
    }

    private void drawTitle(float screenWidth, float screenHeight) {
        float scaledWidth = titleTexture.getWidth() * DisplayConfig.TITLE_SCALE;
        float scaledHeight = titleTexture.getHeight() * DisplayConfig.TITLE_SCALE;
        float titleX = (screenWidth - scaledWidth) / 2; // Center horizontally
        float titleY = screenHeight - DisplayConfig.TITLE_Y_POSITION;

        batch.draw(titleTexture,
                  titleX, titleY - scaledHeight, // Position
                  scaledWidth, scaledHeight); // Size
    }

    private void drawMenuOptions(float screenWidth, float screenHeight) {
        for (int i = 0; i < MenuConfig.OPTIONS.length; i++) {
            boolean isSelected = (i == selectedIndex);
            drawMenuItem(screenWidth, screenHeight, i, isSelected);
        }
    }

    private void drawMenuItem(float screenWidth, float screenHeight, int index, boolean isSelected) {
        String prefix = isSelected ? MenuConfig.SELECTED_PREFIX : MenuConfig.UNSELECTED_PREFIX;
        String menuText = prefix + MenuConfig.OPTIONS[index];
        font.setColor(isSelected ? DisplayConfig.SELECTED_COLOR : DisplayConfig.DEFAULT_COLOR);

        float textWidth = font.getScaleX() * font.getData().getGlyph('A').width * menuText.length();
        float xPosition = (screenWidth - textWidth) / 2; // Center horizontally
        float yPosition = screenHeight - DisplayConfig.MENU_START_Y - index * DisplayConfig.MENU_ITEM_SPACING;

        font.draw(batch, menuText, xPosition, yPosition);
    }

    /**
     * Input handling methods
     */
    private void handleInput() {
        handleNavigationInput();
        handleSelectionInput();
    }

    private void handleNavigationInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            updateSelection(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            updateSelection(-1);
        }
    }

    private void updateSelection(int direction) {
        selectedIndex = (selectedIndex + direction + MenuConfig.OPTIONS.length) % MenuConfig.OPTIONS.length;
        selectSound.play();
    }

    private void handleSelectionInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && selectedIndex >= 0) {
            processMenuSelection(selectedIndex);
        }
    }

    private void processMenuSelection(int index) {
        switch (index) {
            case 0:
                startGame();
                break;
            case 1: openOptions(); break;
            case 2: Gdx.app.exit(); break;
        }
    }

    /**
     * Screen transition methods
     */
    private void startGame() {
        inputEnabled = false;
        selectedIndex = -1;
        game.setScreen(new ScreenTransition(
            game,
            this,  // Current screen
            new SaveFileSelectionScreen(game),  // Next screen
            ScreenTransition.TransitionType.FADE_TO_WHITE
        ));
    }

    private void openOptions() {
        inputEnabled = false;
        selectedIndex = -1;
        game.setScreen(new ScreenTransition(
            game,
            this,  // Current screen
            new OptionsScreen(game),  // Next screen
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
        titleTexture.dispose();
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

