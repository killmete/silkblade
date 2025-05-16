package swu.cp112.silkblade.core;

import swu.cp112.silkblade.entity.item.ItemDatabase;
import swu.cp112.silkblade.screen.MainMenuScreen;
import swu.cp112.silkblade.screen.OptionsScreen;
import swu.cp112.silkblade.screen.SaveFileSelectionScreen;
import swu.cp112.silkblade.util.GameLogger;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Main game class that handles initialization and core game components.
 */
public class Main extends Game {
    /**
     * Display configuration
     */
    private static final class DisplayConfig {
        static final int WORLD_WIDTH = 1280;
        static final int WORLD_HEIGHT = 720;
    }

    /**
     * Audio configuration
     */
    private static final class AudioConfig {
        static final float MUSIC_VOLUME = 0.15f;
        static final String MAIN_MUSIC_PATH = "music/main_menu.mp3";
    }

    /**
     * Core components
     */
    private static OrthographicCamera camera;
    private static FitViewport viewport;
    private static Music backgroundMusic;
    private static Game gameInstance;
    
    /**
     * Audio settings
     */
    private static float musicVolume = AudioConfig.MUSIC_VOLUME;
    private static float effectVolume = 0.7f;

    /**
     * Lifecycle methods
     */
    @Override
    public void create() {
        try {
            GameLogger.logInfo("Initializing game...");
            
            // Store reference to game instance
            gameInstance = this;

            // Initialize core systems
            initializeGraphics();
            initializeItemDatabase();
            initializeAudio();
            initializeOptions();

            // Create save directory if it doesn't exist
            FileHandle saveDir = Gdx.files.local("save");
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            // Set initial screen
            setInitialScreen();

            GameLogger.logInfo("Game initialized successfully");
        } catch (Exception e) {
            GameLogger.logError("Failed to initialize game", e);
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
        try {
            super.resize(width, height);
            updateViewport(width, height);
        } catch (Exception e) {
            GameLogger.logError("Error during resize", e);
        }
    }

    @Override
    public void render() {
        try {
            super.render();
        } catch (Exception e) {
            GameLogger.logError("Error during game render", e);
            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        try {
            super.dispose();
            if (backgroundMusic != null) {
                backgroundMusic.dispose();
            }
            GameLogger.logInfo("Game disposed successfully");
        } catch (Exception e) {
            GameLogger.logError("Error during game disposal", e);
        }
    }

    /**
     * Initialization methods
     */
    private void initializeGraphics() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(
            DisplayConfig.WORLD_WIDTH,
            DisplayConfig.WORLD_HEIGHT,
            camera
        );
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
    }

    /**
     * Initialize the item database to ensure items are loaded
     */
    private void initializeItemDatabase() {
        GameLogger.logInfo("Initializing item database...");
        ItemDatabase.getInstance(); // This initializes the singleton
        GameLogger.logInfo("Item database initialized");
    }

    /**
     * Initialize global audio
     */
    private void initializeAudio() {
        GameLogger.logInfo("Initializing audio...");
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(AudioConfig.MAIN_MUSIC_PATH));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(AudioConfig.MUSIC_VOLUME);
        backgroundMusic.play();
        GameLogger.logInfo("Audio initialized");
    }

    /**
     * Initialize options and apply settings
     */
    private void initializeOptions() {
        GameLogger.logInfo("Loading game options...");
        
        // Check if options file exists
        FileHandle file = Gdx.files.local(OptionsScreen.OptionsConfig.OPTIONS_FILE);
        OptionsScreen.GameSettings settings;
        
        if (file.exists()) {
            try {
                // Load existing options
                Json json = new Json();
                settings = json.fromJson(OptionsScreen.GameSettings.class, file.readString());
                GameLogger.logInfo("Loaded existing options file");
            } catch (Exception e) {
                // Create default settings if loading fails
                GameLogger.logError("Failed to load options, creating defaults", e);
                settings = new OptionsScreen.GameSettings();
                saveDefaultOptions(settings, OptionsScreen.OptionsConfig.OPTIONS_FILE);
            }
        } else {
            // Create default settings if file doesn't exist
            GameLogger.logInfo("No options file found, creating defaults");
            settings = new OptionsScreen.GameSettings();
            saveDefaultOptions(settings, OptionsScreen.OptionsConfig.OPTIONS_FILE);
        }
        
        // Apply the settings
        applyGameSettings(settings);
    }
    
    /**
     * Save default options to file
     */
    private void saveDefaultOptions(OptionsScreen.GameSettings settings, String optionsFile) {
        try {
            Json json = new Json();
            String jsonStr = json.prettyPrint(settings);
            
            FileHandle file = Gdx.files.local(optionsFile);
            file.writeString(jsonStr, false);
            
            GameLogger.logInfo("Default options saved successfully");
        } catch (Exception e) {
            GameLogger.logError("Failed to save default options", e);
        }
    }
    
    /**
     * Apply loaded game settings
     */
    private void applyGameSettings(OptionsScreen.GameSettings settings) {
        // Apply resolution if not in fullscreen
        if (!settings.fullscreen) {
            int[] resolution = OptionsScreen.getResolutionByIndex(settings.resolutionIndex);
            if (resolution != null) {
                Gdx.graphics.setWindowedMode(resolution[0], resolution[1]);
            }
        }
        
        // Apply fullscreen
        if (settings.fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    private void setInitialScreen() {
        setScreen(new MainMenuScreen(this));
    }

    private void updateViewport(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
    }

    /**
     * Accessors
     */
    public static OrthographicCamera getCamera() {
        return camera;
    }

    public static FitViewport getViewport() {
        return viewport;
    }
    
    /**
     * Returns the game instance
     */
    public static Game getGame() {
        return gameInstance;
    }

    /**
     * Music management methods
     */
    public static Music getBackgroundMusic() {
        return backgroundMusic;
    }

    public static void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    public static void resumeBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    /**
     * Restarts the background music from the beginning
     */
    public static void restartBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.play();
        }
    }
    
    /**
     * Volume control methods
     */
    public static void setMusicVolume(float volume) {
        musicVolume = volume;
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(musicVolume);
        }
    }
    
    public static float getMusicVolume() {
        return musicVolume;
    }
    
    public static void setEffectVolume(float volume) {
        effectVolume = volume;
    }
    
    public static float getEffectVolume() {
        return effectVolume;
    }
    
    /**
     * Plays a sound with the global effect volume
     */
    public static long playSound(Sound sound) {
        if (sound != null) {
            return sound.play(effectVolume);
        }
        return -1;
    }
}
