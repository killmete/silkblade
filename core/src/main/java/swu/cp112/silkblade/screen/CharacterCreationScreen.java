package swu.cp112.silkblade.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
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
 * Screen for naming a new character and creating a save file.
 */
public class CharacterCreationScreen implements Screen, InputProcessor {
    /**
     * Display configuration
     */
    private static final class DisplayConfig {
        // Layout
        static final float TITLE_Y_POSITION = 200;
        static final float INPUT_FIELD_Y = 350;
        static final float BUTTON_Y = 520;
        static final float TEXT_X_POSITION = 0.2f;
        static final float FONT_SCALE = 2f;
        static final int MAX_NAME_LENGTH = 12;

        // Colors
        static final Color TITLE_COLOR = Color.WHITE;
        static final Color TEXT_COLOR = Color.WHITE;
        static final Color INPUT_COLOR = Color.YELLOW;
        static final Color BUTTON_COLOR = Color.WHITE;
        static final Color BACKGROUND_COLOR = Color.BLACK;
    }

    /**
     * Menu configuration
     */
    private static final class MenuConfig {
        static final String TITLE = "Name your character:";
        static final String CONFIRM_BUTTON = "Confirm";
        static final String PLACEHOLDER = "Enter name...";
    }

    /**
     * Audio configuration
     */
    private static final class AudioConfig {
        static final float MUSIC_VOLUME = 0.05f;
        static final String FONT_PATH = "fonts/DTM.fnt";
        static final String MUSIC_PATH = "music/main_menu.mp3";
        static final String SELECT_SOUND_PATH = "sounds/select.wav";
        static final String TYPE_SOUND_PATH = "sounds/typing_2.wav";
    }

    /**
     * Core components
     */
    private final Game game;
    private final FitViewport viewport;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;

    /**
     * Audio components
     */
    private final Sound selectSound;
    private final Sound typeSound;
    // Keep for future reference but comment out
    // private final Music music;

    /**
     * State
     */
    private StringBuilder characterName = new StringBuilder();
    private boolean inputEnabled = true;
    private boolean confirmed = false;
    private float cursorBlinkTimer = 0;
    private boolean cursorVisible = true;

    public CharacterCreationScreen(Game game) {
        this.game = game;
        this.viewport = Main.getViewport();
        this.camera = Main.getCamera();

        this.batch = initializeGraphics();
        this.font = initializeFont();

        this.selectSound = initializeSelectSound();
        this.typeSound = initializeTypeSound();
        // Keep for future reference but comment out
        // this.music = initializeMusic();

        Gdx.input.setInputProcessor(this);
    }

    /**
     * Initialization methods
     */
    private SpriteBatch initializeGraphics() {
        return new SpriteBatch();
    }

    private BitmapFont initializeFont() {
        BitmapFont font = new BitmapFont(Gdx.files.internal(AudioConfig.FONT_PATH));
        font.setColor(DisplayConfig.TEXT_COLOR);
        font.getData().setScale(DisplayConfig.FONT_SCALE);
        return font;
    }

    private Sound initializeSelectSound() {
        return Gdx.audio.newSound(Gdx.files.internal(AudioConfig.SELECT_SOUND_PATH));
    }

    private Sound initializeTypeSound() {
        return Gdx.audio.newSound(Gdx.files.internal(AudioConfig.TYPE_SOUND_PATH));
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
        updateCursorBlink(delta);
        drawScreen();

        if (inputEnabled && !ScreenTransition.isTransitioning() && confirmed) {
            createCharacter();
        }
    }

    private void clearScreen() {
        ScreenUtils.clear(DisplayConfig.BACKGROUND_COLOR);
    }

    private void updateCursorBlink(float delta) {
        cursorBlinkTimer += delta;
        if (cursorBlinkTimer >= 0.5f) {
            cursorBlinkTimer = 0;
            cursorVisible = !cursorVisible;
        }
    }

    private void drawScreen() {
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw title
        font.setColor(DisplayConfig.TITLE_COLOR);
        font.draw(batch, MenuConfig.TITLE,
                screenWidth * DisplayConfig.TEXT_X_POSITION,
                screenHeight - DisplayConfig.TITLE_Y_POSITION);

        // Draw input field
        font.setColor(DisplayConfig.INPUT_COLOR);
        String displayText = characterName.length() > 0 ?
                characterName.toString() : "";

        String textWithCursor = displayText;
        if (characterName.length() > 0 && cursorVisible) {
            textWithCursor = displayText + "_";
        } else if (characterName.length() == 0 && cursorVisible) {
            textWithCursor = "_" + displayText;
        }

        font.draw(batch, textWithCursor,
                screenWidth * DisplayConfig.TEXT_X_POSITION,
                screenHeight - DisplayConfig.INPUT_FIELD_Y);

        // Draw confirm button
        if (characterName.length() > 0) {
            font.setColor(DisplayConfig.BUTTON_COLOR);
            font.draw(batch, MenuConfig.CONFIRM_BUTTON,
                    screenWidth * DisplayConfig.TEXT_X_POSITION,
                    screenHeight - DisplayConfig.BUTTON_Y);
        }

        batch.end();
    }

    /**
     * Create new character and save file
     */
    private void createCharacter() {
        if (characterName.length() > 0) {
            try {
                // Create new player with the given name
                Player player = new Player(characterName.toString());
                
                // Add gold for testing shop
                player.setGold(10000);

                // Save the player data
                player.saveToFile();

                GameLogger.logInfo("Created new character: " + characterName);

                // Navigate to the stage selection screen
                inputEnabled = false;
                game.setScreen(new ScreenTransition(
                    game,
                    this,
                    new MainNavigationScreen(game),
                    ScreenTransition.TransitionType.CROSS_FADE
                ));
            } catch (Exception e) {
                GameLogger.logError("Failed to create character", e);
            }
        }
    }

    /**
     * InputProcessor methods
     */
    @Override
    public boolean keyDown(int keycode) {
        if (!inputEnabled) return false;

        if (keycode == Input.Keys.ENTER) {
            if (characterName.length() > 0) {
                confirmed = true;
                selectSound.play();
            }
            return true;
        }

        if (keycode == Input.Keys.ESCAPE) {
            // Return to save file selection screen
            game.setScreen(new ScreenTransition(
                game,
                this,
                new SaveFileSelectionScreen(game),
                ScreenTransition.TransitionType.CROSS_FADE
            ));
            return true;
        }

        if (keycode == Input.Keys.BACKSPACE && characterName.length() > 0) {
            characterName.deleteCharAt(characterName.length() - 1);
            typeSound.play(0.5f);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        if (!inputEnabled) return false;

        if (characterName.length() < DisplayConfig.MAX_NAME_LENGTH
                && character >= 32 && character < 127) {
            characterName.append(character);
            typeSound.play(0.5f);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
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
        selectSound.dispose();
        typeSound.dispose();
        // Keep for future reference but comment out
        // music.dispose();
        
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void hide() {
        // Keep for future reference but comment out
        // if (music != null) {
        //     music.stop();
        // }
        
        // No need to stop the shared music
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void show() {
        // Keep for future reference but comment out
        // if (music != null && !music.isPlaying()) {
        //     music.play();
        // }
        
        // Make sure the shared music is playing
        swu.cp112.silkblade.core.Main.resumeBackgroundMusic();
        Gdx.input.setInputProcessor(this);
    }

    // Unused Screen interface methods
    @Override public void pause() {}
    @Override public void resume() {}
}
