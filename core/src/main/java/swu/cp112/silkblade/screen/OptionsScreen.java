package swu.cp112.silkblade.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import swu.cp112.silkblade.core.Main;
import swu.cp112.silkblade.screen.transition.ScreenTransition;

/**
 * Options screen for game settings like resolution and fullscreen toggle.
 */
public class OptionsScreen implements Screen {

    /**
     * Represents a screen resolution option
     */
    private static class Resolution {
        int width;
        int height;
        String label;

        public Resolution(int width, int height) {
            this.width = width;
            this.height = height;
            this.label = width + "x" + height;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * Contains the game's settings
     */
    public static class GameSettings {
        public int resolutionIndex = 2; // Default to 1280x720
        public boolean fullscreen = false;
        public float playerMovementSpeed = 300f; // Default movement speed
        
        // Default constructor needed for JSON serialization
        public GameSettings() {}
    }

    /**
     * Returns the width and height for a resolution by index
     * @param index The resolution index
     * @return Array containing [width, height], or null if invalid index
     */
    public static int[] getResolutionByIndex(int index) {
        // Available resolutions (must match the ones initialized in constructor)
        int[][] resolutions = {
            {800, 600},    // 0
            {1024, 768},   // 1
            {1280, 720},   // 2 (default)
            {1366, 768},   // 3
            {1600, 900},   // 4
            {1920, 1080}   // 5
        };
        
        if (index >= 0 && index < resolutions.length) {
            return resolutions[index];
        }
        return null;
    }

    /**
     * Display configuration
     */
    private static final class DisplayConfig {
        // Layout
        static final float TITLE_Y_POSITION = 150;
        static final float OPTIONS_START_Y = 250;
        static final float OPTION_SPACING = 60;
        static final float TITLE_X_POSITION = 0.5f; // Centered
        static final float OPTION_LABEL_X_POSITION = 0.3f;
        static final float OPTION_VALUE_X_POSITION = 0.7f;
        static final float FONT_SCALE = 1.5f;

        // Colors
        static final Color TITLE_COLOR = Color.WHITE;
        static final Color SELECTED_COLOR = Color.YELLOW;
        static final Color DEFAULT_COLOR = Color.WHITE;
        static final Color BACKGROUND_COLOR = Color.BLACK;
    }

    /**
     * Options configuration
     */
    public static final class OptionsConfig {
        static final String TITLE = "OPTIONS";
        static final String[] OPTION_LABELS = {
            "Resolution",
            "Fullscreen",
            "Movement Speed",
            "Back"
        };
        public static final String OPTIONS_FILE = "options.json";
    }

    /**
     * Asset configuration
     */
    private static final class AssetConfig {
        static final String FONT_PATH = "fonts/DTM.fnt";
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

    /**
     * Audio components
     */
    private final Sound selectSound;

    /**
     * Options
     */
    private final Array<Resolution> resolutions;
    private GameSettings settings;

    /**
     * State
     */
    private int selectedIndex = 0;
    private boolean changing = false;
    private boolean inputEnabled = true;

    public OptionsScreen(Game game) {
        this.game = game;
        this.viewport = Main.getViewport();
        this.camera = Main.getCamera();

        this.batch = initializeGraphics();
        this.font = initializeFont();

        this.selectSound = initializeSound();

        // Initialize resolutions
        this.resolutions = new Array<>();
        resolutions.add(new Resolution(800, 600));
        resolutions.add(new Resolution(1024, 768));
        resolutions.add(new Resolution(1280, 720));
        resolutions.add(new Resolution(1366, 768));
        resolutions.add(new Resolution(1600, 900));
        resolutions.add(new Resolution(1920, 1080));

        // Load settings
        this.settings = loadSettings();
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

    /**
     * Settings methods
     */
    private GameSettings loadSettings() {
        FileHandle file = Gdx.files.local(OptionsConfig.OPTIONS_FILE);
        if (file.exists()) {
            try {
                Json json = new Json();
                GameSettings loadedSettings = json.fromJson(GameSettings.class, file.readString());
                
                // Apply loaded settings
                applySettings(loadedSettings);
                
                return loadedSettings;
            } catch (Exception e) {
                Gdx.app.error("OptionsScreen", "Failed to load settings", e);
            }
        }
        
        // Create and return default settings if loading fails
        GameSettings defaultSettings = new GameSettings();
        applySettings(defaultSettings);
        return defaultSettings;
    }
    
    private void saveSettings() {
        try {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            String jsonStr = json.prettyPrint(settings);
            
            FileHandle file = Gdx.files.local(OptionsConfig.OPTIONS_FILE);
            file.writeString(jsonStr, false);
            
            Gdx.app.log("OptionsScreen", "Settings saved successfully");
        } catch (Exception e) {
            Gdx.app.error("OptionsScreen", "Failed to save settings", e);
        }
    }

    private void applySettings(GameSettings settings) {
        // Apply resolution
        if (settings.resolutionIndex >= 0 && settings.resolutionIndex < resolutions.size) {
            Resolution res = resolutions.get(settings.resolutionIndex);
            if (!settings.fullscreen) { // Only apply window mode resolution
                Gdx.graphics.setWindowedMode(res.width, res.height);
            }
        }
        
        // Apply fullscreen
        if (settings.fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    /**
     * Rendering methods
     */
    @Override
    public void render(float delta) {
        clearScreen();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawTitle();
        drawOptions();
        batch.end();

        if (inputEnabled && !ScreenTransition.isTransitioning()) {
            handleInput();
        }
    }

    private void clearScreen() {
        ScreenUtils.clear(DisplayConfig.BACKGROUND_COLOR);
    }

    private void drawTitle() {
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();
        float textWidth = font.getScaleX() * font.getData().getGlyph('A').width * OptionsConfig.TITLE.length();
        float xPosition = (screenWidth - textWidth) / 2; // Center horizontally

        font.setColor(DisplayConfig.TITLE_COLOR);
        font.draw(batch, OptionsConfig.TITLE,
                xPosition,
                screenHeight - DisplayConfig.TITLE_Y_POSITION);
    }

    private void drawOptions() {
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();
        float optionY = screenHeight - DisplayConfig.OPTIONS_START_Y;

        // Draw each option
        for (int i = 0; i < OptionsConfig.OPTION_LABELS.length; i++) {
            String label = OptionsConfig.OPTION_LABELS[i];
            String value = getOptionValue(i);

            float labelX = screenWidth * DisplayConfig.OPTION_LABEL_X_POSITION;
            float valueX = screenWidth * DisplayConfig.OPTION_VALUE_X_POSITION;

            boolean isSelected = (i == selectedIndex);
            boolean isChanging = (isSelected && changing);

            // Set color based on selection state
            Color labelColor = isSelected ? DisplayConfig.SELECTED_COLOR : DisplayConfig.DEFAULT_COLOR;
            Color valueColor = isChanging ? Color.ORANGE : labelColor;
            
            font.setColor(labelColor);
            font.draw(batch, label, labelX, optionY);
            
            font.setColor(valueColor);
            font.draw(batch, value, valueX, optionY);
            
            optionY -= DisplayConfig.OPTION_SPACING;
        }
    }

    private String getOptionValue(int optionIndex) {
        switch (optionIndex) {
            case 0: // Resolution
                if (settings.resolutionIndex >= 0 && settings.resolutionIndex < resolutions.size) {
                    return resolutions.get(settings.resolutionIndex).toString();
                }
                return "Unknown";
                
            case 1: // Fullscreen
                return settings.fullscreen ? "ON" : "OFF";
                
            case 2: // Movement Speed
                return String.format("%.1f", settings.playerMovementSpeed);
                
            case 3: // Back
                return "";
                
            default:
                return "";
        }
    }

    /**
     * Input handling methods
     */
    private void handleInput() {
        if (changing) {
            handleValueChange();
        } else {
            handleNavigationInput();
            handleSelectionInput();
        }
    }

    private void handleNavigationInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            updateSelection(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            updateSelection(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // Go back to main menu
            saveSettings();
            returnToMainMenu();
        }
    }

    private void updateSelection(int direction) {
        selectedIndex = (selectedIndex + direction + OptionsConfig.OPTION_LABELS.length) % OptionsConfig.OPTION_LABELS.length;
        selectSound.play();
    }

    private void handleSelectionInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            processMenuSelection(selectedIndex);
        }
    }

    private void processMenuSelection(int index) {
        switch (index) {
            case 0: // Resolution
            case 1: // Fullscreen
                changing = true;
                break;
            case 2: // Movement Speed
                changing = true;
                break;
            case 3: // Back
                saveSettings();
                returnToMainMenu();
                break;
        }
    }

    private void handleValueChange() {
        boolean valueChanged = false;

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            valueChanged = changeValue(-1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            valueChanged = changeValue(1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                  Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changing = false;
            selectSound.play();
        }

        if (valueChanged) {
            selectSound.play();
            applySettings(settings);
        }
    }

    private boolean changeValue(int direction) {
        switch (selectedIndex) {
            case 0: // Resolution
                int oldResIndex = settings.resolutionIndex;
                settings.resolutionIndex = MathUtils.clamp(
                    settings.resolutionIndex + direction,
                    0,
                    resolutions.size - 1
                );
                return oldResIndex != settings.resolutionIndex;
                
            case 1: // Fullscreen
                boolean oldFullscreen = settings.fullscreen;
                settings.fullscreen = !settings.fullscreen;
                return oldFullscreen != settings.fullscreen;
                
            case 2: // Movement Speed
                float oldSpeed = settings.playerMovementSpeed;
                // Adjust by 25 units per step, clamp between 100 and 500
                settings.playerMovementSpeed = MathUtils.clamp(
                    settings.playerMovementSpeed + (direction * 25f),
                    100f,
                    500f
                );
                return oldSpeed != settings.playerMovementSpeed;
                
            default:
                return false;
        }
    }

    private void returnToMainMenu() {
        inputEnabled = false;
        game.setScreen(new ScreenTransition(
            game,
            this,
            new MainMenuScreen(game),
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
        selectSound.dispose();
    }

    @Override
    public void show() {
        // Make sure the shared music is playing
        Main.resumeBackgroundMusic();
    }

    @Override
    public void hide() {
        // No specific actions needed when hiding
    }

    // Unused Screen interface methods
    @Override public void pause() {}
    @Override public void resume() {}
}
