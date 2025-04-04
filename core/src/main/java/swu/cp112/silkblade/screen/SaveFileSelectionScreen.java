package swu.cp112.silkblade.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import swu.cp112.silkblade.core.Main;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.screen.transition.ScreenTransition;
import swu.cp112.silkblade.util.GameLogger;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Handles the save file selection screen of the game.
 */
public class SaveFileSelectionScreen implements Screen {
    /**
     * Display configuration
     */
    private static final class DisplayConfig {
        // Layout
        static final float TITLE_Y_POSITION = 200;
        static final float SAVE_FILES_START_Y = 350;
        static final float SAVE_FILE_SPACING = 140;
        static final float SAVE_FILE_X_POSITION = 0.2f;
        static final float FONT_SCALE = 2f;
        static final int MAX_SAVES_DISPLAYED = 5;

        // Colors
        static final Color SELECTED_COLOR = Color.YELLOW;
        static final Color DEFAULT_COLOR = Color.WHITE;
        static final Color BACKGROUND_COLOR = Color.BLACK;
        static final Color NEW_SAVE_COLOR = Color.WHITE;
        static final Color STATS_COLOR = Color.CYAN;
    }

    /**
     * Menu configuration
     */
    private static final class MenuConfig {
        static final String TITLE = "Select a Save File";
        static final String NO_SAVES_MESSAGE = "No save files found.";
        static final String SELECTED_PREFIX = "> ";
        static final String UNSELECTED_PREFIX = "  ";
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
     * Class to hold save file information
     */
    private static class SaveFileInfo {
        String fileName;
        String playerName;
        int level;
        int maxHP;
        int attack;
        int defense;
        int currentStage;
        int gold;
        int attackBonus;
        int defenseBonus;
        int maxHPBonus;

        public SaveFileInfo(String fileName, String playerName, int level, int maxHP, int attack, int defense,
                            int currentStage, int gold, int attackBonus, int defenseBonus, int maxHPBonus) {
            this.fileName = fileName;
            this.playerName = playerName;
            this.level = level;
            this.maxHP = maxHP;
            this.attack = attack;
            this.defense = defense;
            this.currentStage = currentStage;
            this.gold = gold;
            this.attackBonus = attackBonus;
            this.defenseBonus = defenseBonus;
            this.maxHPBonus = maxHPBonus;
        }
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
    // Keep for future reference but comment out
    // private final Music music;

    /**
     * State
     */
    private int selectedIndex = 0;
    private boolean inputEnabled = true;
    private Array<SaveFileInfo> saveFiles = new Array<>();
    private String saveFolderPath = "save";

    public SaveFileSelectionScreen(Game game) {
        this.game = game;
        this.viewport = Main.getViewport();
        this.camera = Main.getCamera();

        this.batch = initializeGraphics();
        this.font = initializeFont();

        this.selectSound = initializeSound();
        // Keep for future reference but comment out
        // this.music = initializeMusic();
        
        this.saveFiles = new Array<>();
        this.selectedIndex = 0;
        this.inputEnabled = true;
        
        loadSaveFiles();
    }

    /**
     * Initialization methods
     */
    private SpriteBatch initializeGraphics() {
        return new SpriteBatch();
    }

    private BitmapFont initializeFont() {
        BitmapFont font = new BitmapFont(Gdx.files.internal(AudioConfig.FONT_PATH));
        font.setColor(DisplayConfig.DEFAULT_COLOR);
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
     * Save File Methods
     */
    private void loadSaveFiles() {
        saveFiles.clear();

        FileHandle saveDir = Gdx.files.local(saveFolderPath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
            GameLogger.logInfo("Created save directory");
            return;
        }

        // Get all JSON files in the save folder
        FileHandle[] files = saveDir.list(".json");
        if (files != null && files.length > 0) {
            // Sort by last modified time (newest first)
            Arrays.sort(files, Comparator.comparing(FileHandle::lastModified).reversed());

            for (FileHandle file : files) {
                try {
                    // Load player data from file
                    Json json = new Json();
                    Player playerData = json.fromJson(Player.class, file.readString());

                    String fileName = file.nameWithoutExtension();

                    // Get base stats and bonuses from equipment
                    int attackBonus = playerData.getAttack() - playerData.getBaseAttack();
                    int defenseBonus = playerData.getDefense() - playerData.getBaseDefense();
                    int maxHPBonus = playerData.getMaxHP() - playerData.getBaseMaxHP();

                    SaveFileInfo saveInfo = new SaveFileInfo(
                        fileName,
                        playerData.getName(),
                        playerData.getLevel(),
                        playerData.getBaseMaxHP(),
                        playerData.getBaseAttack(),
                        playerData.getBaseDefense(),
                        playerData.getCurrentStage(),
                        playerData.getGold(),
                        attackBonus,
                        defenseBonus,
                        maxHPBonus
                    );

                    saveFiles.add(saveInfo);
                    GameLogger.logInfo("Found save file: " + fileName + " - " + playerData.getName());
                } catch (Exception e) {
                    // If we can't read the player data, still add the file but with placeholder info
                    String fileName = file.nameWithoutExtension();
                    SaveFileInfo saveInfo = new SaveFileInfo(fileName, "Unknown", 0, 0, 0, 0, 1, 0, 0, 0, 0);
                    saveFiles.add(saveInfo);
                    GameLogger.logError("Error loading player data from " + fileName, e);
                }
            }
        }
    }

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

        if (saveFiles.size == 0) {
            drawNoSavesMessage(screenWidth, screenHeight);
        } else {
            drawSaveFiles(screenWidth, screenHeight);
        }

        drawNewSaveOption(screenWidth, screenHeight);
        batch.end();
    }

    private void drawTitle(float screenWidth, float screenHeight) {
        font.setColor(DisplayConfig.DEFAULT_COLOR);
        font.draw(batch, MenuConfig.TITLE,
                screenWidth * DisplayConfig.SAVE_FILE_X_POSITION,
                screenHeight - DisplayConfig.TITLE_Y_POSITION);
    }

    private void drawNoSavesMessage(float screenWidth, float screenHeight) {
        font.setColor(DisplayConfig.DEFAULT_COLOR);
        font.draw(batch, MenuConfig.NO_SAVES_MESSAGE,
                screenWidth * DisplayConfig.SAVE_FILE_X_POSITION,
                screenHeight - DisplayConfig.SAVE_FILES_START_Y);
    }

    private void drawSaveFiles(float screenWidth, float screenHeight) {
        int endIndex = Math.min(saveFiles.size, DisplayConfig.MAX_SAVES_DISPLAYED);
        float statsYOffset = 70; // Space between character name and stats
        float stageYOffset = 40; // Space between stats and stage/gold info
        float statScale = 0.7f; // Smaller scale for stats text

        for (int i = 0; i < endIndex; i++) {
            SaveFileInfo saveInfo = saveFiles.get(i);
            boolean isSelected = (i == selectedIndex);
            String prefix = isSelected ? MenuConfig.SELECTED_PREFIX : MenuConfig.UNSELECTED_PREFIX;

            // Position for character name
            float yPosition = screenHeight - DisplayConfig.SAVE_FILES_START_Y - i * DisplayConfig.SAVE_FILE_SPACING;

            // Draw character name
            font.setColor(isSelected ? DisplayConfig.SELECTED_COLOR : DisplayConfig.DEFAULT_COLOR);
            font.draw(batch, prefix + saveInfo.playerName,
                    screenWidth * DisplayConfig.SAVE_FILE_X_POSITION, yPosition);

            // Draw stats with smaller font
            float originalScale = font.getData().scaleX;
            font.getData().setScale(originalScale * statScale);
            font.setColor(DisplayConfig.STATS_COLOR);

            // Format: Lv. XX | HP: XXX (+YY) | ATK: XX (+YY) | DEF: XX (+YY)
            String statsText = String.format("Lv. %d | HP: %d (%s%d) | ATK: %d (%s%d) | DEF: %d (%s%d)",
                    saveInfo.level,
                    saveInfo.maxHP,
                    saveInfo.maxHPBonus >= 0 ? "+" : "", saveInfo.maxHPBonus,
                    saveInfo.attack,
                    saveInfo.attackBonus >= 0 ? "+" : "", saveInfo.attackBonus,
                    saveInfo.defense,
                    saveInfo.defenseBonus >= 0 ? "+" : "", saveInfo.defenseBonus);

            font.draw(batch, statsText,
                    screenWidth * DisplayConfig.SAVE_FILE_X_POSITION + 15, // Indent stats slightly
                    yPosition - statsYOffset);

            // Draw stage and gold info
            String progressText = String.format("Stage: %d | Gold: %d",
                    saveInfo.currentStage, saveInfo.gold);

            font.draw(batch, progressText,
                    screenWidth * DisplayConfig.SAVE_FILE_X_POSITION + 15,
                    yPosition - statsYOffset - stageYOffset);

            // Reset font scale
            font.getData().setScale(originalScale);
        }
    }

    private void drawNewSaveOption(float screenWidth, float screenHeight) {
        String DISPLAY_TEXT;
        if (saveFiles.size == 0)
            DISPLAY_TEXT = "CREATE";
        else
            DISPLAY_TEXT = "RESET";

        boolean isSelected = (selectedIndex == saveFiles.size);
        String prefix = isSelected ? MenuConfig.SELECTED_PREFIX : MenuConfig.UNSELECTED_PREFIX;
        font.setColor(isSelected ? DisplayConfig.SELECTED_COLOR : DisplayConfig.NEW_SAVE_COLOR);

        float yPosition = screenHeight - DisplayConfig.SAVE_FILES_START_Y -
                Math.min(saveFiles.size, DisplayConfig.MAX_SAVES_DISPLAYED) * DisplayConfig.SAVE_FILE_SPACING -
                DisplayConfig.SAVE_FILE_SPACING;

        font.draw(batch, prefix + DISPLAY_TEXT,
                screenWidth * DisplayConfig.SAVE_FILE_X_POSITION, yPosition);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // Return to main menu or save selection
            game.setScreen(new ScreenTransition(
                game,
                this,
                new MainMenuScreen(game),
                ScreenTransition.TransitionType.CROSS_FADE
            ));
        }
    }

    private void updateSelection(int direction) {
        int totalOptions = saveFiles.size + 1; // +1 for the "Create New Save" option
        selectedIndex = (selectedIndex + direction + totalOptions) % totalOptions;
        selectSound.play();
    }

    private void handleSelectionInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            processMenuSelection(selectedIndex);
        }
    }

    private void processMenuSelection(int index) {
        if (index == saveFiles.size) {
            // Create new save selected
            createNewSave();
        } else if (index >= 0 && index < saveFiles.size) {
            // Existing save selected
            loadSaveFile(saveFiles.get(index).fileName);
        }
    }

    private void createNewSave() {
        inputEnabled = false;
        game.setScreen(new ScreenTransition(
            game,
            this,
            new CharacterCreationScreen(game),
            ScreenTransition.TransitionType.CROSS_FADE
        ));
    }

    private void loadSaveFile(String fileName) {
        try {
            FileHandle file = Gdx.files.local(saveFolderPath + "/" + fileName + ".json");
            if (file.exists()) {
                // Load the save file data
                GameLogger.logInfo("Loading save file: " + fileName);

                // Navigate to the main navigation screen instead of stage selection
                inputEnabled = false;
                game.setScreen(new ScreenTransition(
                    game,
                    this,
                    new MainNavigationScreen(game),
                    ScreenTransition.TransitionType.CROSS_FADE
                ));
            }
        } catch (Exception e) {
            GameLogger.logError("Failed to load save file: " + fileName, e);
        }
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
        loadSaveFiles(); // Refresh save files when screen is shown
    }

    // Unused Screen interface methods
    @Override public void pause() {}
    @Override public void resume() {}
}
