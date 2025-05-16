package swu.cp112.silkblade.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import swu.cp112.silkblade.core.Main;
import swu.cp112.silkblade.screen.transition.ScreenTransition;
import swu.cp112.silkblade.util.GameLogger;

public class CreditsScreen implements Screen {
    // =================== Constants ===================
    private static final float LETTER_DELAY = 0.05f;
    private static final float PUNCTUATION_DELAY = 0.8f;
    private static final float FADE_DURATION = 1f;
    private static final float TITLE_DISPLAY_DURATION = 2f;
    private static final float SCROLL_SPEED = 60f;
    private static final float THANK_YOU_DISPLAY_DURATION = 5f;
    private static final float TITLE_SCALE = 0.5f;

    // =================== Core Game Objects ===================
    private final Game game;
    private final FitViewport viewport;
    private final OrthographicCamera camera;
    private final SpriteBatch spriteBatch;
    private final BitmapFont font;

    // =================== Audio Resources ===================
    private final Sound typingSound;

    // =================== Visual Resources ===================
    private final Texture titleTexture;

    // =================== Animation States ===================
    private CreditsState state = CreditsState.TYPING_NARRATIVE;
    private enum CreditsState {
        TYPING_NARRATIVE,
        FADE_OUT_NARRATIVE,
        FADE_IN_TITLE,
        DISPLAY_TITLE,
        SCROLLING_CREDITS,
        FADE_OUT_CREDITS,
        FADE_IN_THANK_YOU,
        DISPLAY_THANK_YOU,
        FADE_OUT_THANK_YOU
    }

    private Music backgroundMusic;

    // =================== Typing Effect Variables ===================
    private final String narrativeText = "With the Great Evil defeated \nPeace has returned to Lamphun.";
    private final StringBuilder currentDisplayText = new StringBuilder();
    private int currentLetterIndex = 0;
    private float letterTimer = 0;
    private boolean isTyping = true;

    // =================== Fade Variables ===================
    private float fadeTimer = 0;
    private float alpha = 0f;

    // =================== Scrolling Variables ===================
    private final Array<CreditLine> credits = new Array<>();
    private float scrollPosition = 0;
    private float stateTimer = 0;
    private float screenHeight;
    private float screenWidth;

    // =================== Thank You Message ===================
    private final String thankYouLine1 = "Thanks for playing,";
    private final String thankYouLine2 = "SilkBlade Team";

    public CreditsScreen(Game game) {
        this.game = game;
        this.viewport = Main.getViewport();
        this.camera = Main.getCamera();
        this.spriteBatch = new SpriteBatch();
        this.font = new BitmapFont(Gdx.files.internal("fonts/DTM.fnt"));
        this.typingSound = Gdx.audio.newSound(Gdx.files.internal("sounds/typing_2.wav"));
        this.titleTexture = new Texture(Gdx.files.internal("title.png"));
        String musicPath = "music/mus_goodbye.mp3";
        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(musicPath));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(.15f);
        } catch (Exception e) {
            GameLogger.logError("Error loading music: ", e);
        }
        screenWidth = viewport.getWorldWidth();
        screenHeight = viewport.getWorldHeight();

        initializeCredits();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        // Initialize alpha to 1.0 for the typing state
        alpha = 1.0f;
    }

    private void initializeCredits() {
        // Add credits in order
        credits.add(new CreditLine("", CreditLineType.SPACING, 60f));
        credits.add(new CreditLine("Lead Developer", CreditLineType.ROLE));
        credits.add(new CreditLine("Arnut Meesut", CreditLineType.NAME));
        credits.add(new CreditLine("", CreditLineType.SPACING, 40f));
        credits.add(new CreditLine("Game Designer", CreditLineType.ROLE));
        credits.add(new CreditLine("Krittin Boonsu", CreditLineType.NAME));
        credits.add(new CreditLine("", CreditLineType.SPACING, 40f));
        credits.add(new CreditLine("Level Designer", CreditLineType.ROLE));
        credits.add(new CreditLine("Kowit Phetnil", CreditLineType.NAME));
        credits.add(new CreditLine("", CreditLineType.SPACING, 40f));
        credits.add(new CreditLine("Quality Assurance", CreditLineType.ROLE));
        credits.add(new CreditLine("Thanapoom Duangmak", CreditLineType.NAME));
        credits.add(new CreditLine("", CreditLineType.SPACING, 60f));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        switch (state) {
            case TYPING_NARRATIVE:
                updateNarrativeTyping(delta);
                renderNarrative();
                break;
            case FADE_OUT_NARRATIVE:
                updateFade(delta, false);
                renderNarrative();
                break;
            case FADE_IN_TITLE:
                updateFade(delta, true);
                renderTitleImage();
                break;
            case DISPLAY_TITLE:
                stateTimer += delta;
                renderTitleImage();
                if (stateTimer >= TITLE_DISPLAY_DURATION) {
                    state = CreditsState.SCROLLING_CREDITS;
                    stateTimer = 0;
                }
                break;
            case SCROLLING_CREDITS:
                updateScrolling(delta);
                renderScrollingCredits();
                break;
            case FADE_OUT_CREDITS:
                updateFade(delta, false);
                renderScrollingCredits();
                break;
            case FADE_IN_THANK_YOU:
                updateFade(delta, true);
                renderThankYouMessage();
                break;
            case DISPLAY_THANK_YOU:
                stateTimer += delta;
                renderThankYouMessage();
                if (stateTimer >= THANK_YOU_DISPLAY_DURATION) {
                    state = CreditsState.FADE_OUT_THANK_YOU;
                    fadeTimer = 0;
                    alpha = 1f;
                }
                break;
            case FADE_OUT_THANK_YOU:
                updateFade(delta, false);
                renderThankYouMessage();
                break;
        }

        spriteBatch.end();
    }

    private void updateNarrativeTyping(float delta) {
        if (!isTyping) return;

        letterTimer += delta;

        // Check for punctuation for extra delay
        boolean shouldPause = false;
        if (currentLetterIndex > 0 && currentLetterIndex < narrativeText.length()) {
            char currentChar = narrativeText.charAt(currentLetterIndex - 1);

            if ((currentChar == '.' || currentChar == '!' || currentChar == '?')) {
                if (currentLetterIndex == narrativeText.length() ||
                    narrativeText.charAt(currentLetterIndex) == ' ' ||
                    narrativeText.charAt(currentLetterIndex) == '\n') {
                    shouldPause = true;
                }
            }
        }

        float currentDelay = shouldPause ? PUNCTUATION_DELAY : LETTER_DELAY;

        if (letterTimer >= currentDelay && currentLetterIndex < narrativeText.length()) {
            char nextChar = narrativeText.charAt(currentLetterIndex);
            currentDisplayText.append(nextChar);
            currentLetterIndex++;

            if (!Character.isWhitespace(nextChar)) {
                typingSound.play(0.2f);
            }

            letterTimer = 0;
        }

        if (currentLetterIndex >= narrativeText.length()) {
            isTyping = false;
            stateTimer = 0;

            // Schedule transition to next state
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    state = CreditsState.FADE_OUT_NARRATIVE;
                    fadeTimer = 0;
                    alpha = 1f;
                }
            }, 1f);
        }
    }

    private void updateFade(float delta, boolean fadeIn) {
        fadeTimer += delta;
        float progress = Math.min(fadeTimer / FADE_DURATION, 1f);

        if (fadeIn) {
            alpha = Interpolation.fade.apply(progress);
        } else {
            alpha = 1f - Interpolation.fade.apply(progress);
        }

        if (progress >= 1f) {
            switch (state) {
                case FADE_OUT_NARRATIVE:
                    state = CreditsState.FADE_IN_TITLE;
                    fadeTimer = 0;
                    alpha = 0f;
                    break;
                case FADE_IN_TITLE:
                    state = CreditsState.DISPLAY_TITLE;
                    stateTimer = 0;
                    break;
                case FADE_OUT_CREDITS:
                    state = CreditsState.FADE_IN_THANK_YOU;
                    fadeTimer = 0;
                    alpha = 0f;
                    break;
                case FADE_IN_THANK_YOU:
                    state = CreditsState.DISPLAY_THANK_YOU;
                    stateTimer = 0;
                    break;
                case FADE_OUT_THANK_YOU:
                    // Transition back to main menu
                    transitionToMainMenu();
                    break;
            }
        }
    }

    private void updateScrolling(float delta) {
        // Increase scroll position - this moves content upward
        scrollPosition += SCROLL_SPEED * delta;

        // Check if all credits scrolled off screen
        float totalHeight = 0;
        for (CreditLine line : credits) {
            totalHeight += line.getHeight();
        }

        // Add title height
        totalHeight += titleTexture.getHeight() * TITLE_SCALE;

        // Reduce the threshold to transition sooner - changed from totalHeight + screenHeight
        // to just totalHeight so we transition when credits have scrolled past the top
        if (scrollPosition > totalHeight + 10) { // Just a small buffer
            state = CreditsState.FADE_OUT_CREDITS;
            fadeTimer = 0;
            alpha = 1f;
        }
    }


    private void renderNarrative() {
        font.setColor(1, 1, 1, alpha);

        GlyphLayout layout = new GlyphLayout(font, currentDisplayText.toString());
        float textX = (screenWidth - layout.width) / 2;
        float textY = screenHeight / 2 + layout.height / 2;

        font.draw(spriteBatch, currentDisplayText.toString(), textX, textY);
    }

    private void renderTitleImage() {
        float scaledWidth = titleTexture.getWidth() * TITLE_SCALE;
        float scaledHeight = titleTexture.getHeight() * TITLE_SCALE;
        float titleX = (screenWidth - scaledWidth) / 2;
        float titleY = screenHeight / 2 + scaledHeight / 2;

        if (state == CreditsState.SCROLLING_CREDITS) {
            titleY = screenHeight - scrollPosition;
        }

        spriteBatch.setColor(1, 1, 1, alpha);
        spriteBatch.draw(titleTexture, titleX, titleY - scaledHeight, scaledWidth, scaledHeight);
    }

   private void renderScrollingCredits() {
    // First render the title image
    float scaledWidth = titleTexture.getWidth() * TITLE_SCALE;
    float scaledHeight = titleTexture.getHeight() * TITLE_SCALE;
    float titleX = (screenWidth - scaledWidth) / 2;

    // In LibGDX, Y=0 is at the bottom of the screen and increases upward
    // Position the title image near the top of the screen and move up with scrolling
    float titleY = screenHeight - scaledHeight - 20 + scrollPosition;

    spriteBatch.setColor(1, 1, 1, alpha);
    spriteBatch.draw(titleTexture, titleX, titleY, scaledWidth, scaledHeight);

    // Place credits below the title image (with less Y value in LibGDX coordinates)
    float yPos = titleY - 60; // Start credits below the title

    for (CreditLine line : credits) {
        if (line.type == CreditLineType.SPACING) {
            yPos -= line.getHeight(); // Move down for spacing
            continue;
        }

        Color color = line.type == CreditLineType.ROLE ? Color.GOLD : Color.WHITE;
        float scale = line.type == CreditLineType.ROLE ? 1.5f : 1.2f;

        font.setColor(color.r, color.g, color.b, alpha);
        font.getData().setScale(scale);

        GlyphLayout layout = new GlyphLayout(font, line.text);
        float textX = (screenWidth - layout.width) / 2;

        // Only draw if visible on screen
        if (yPos > -100 && yPos < screenHeight + 100) {
            font.draw(spriteBatch, line.text, textX, yPos);
        }

        yPos -= layout.height + 20; // Move down for next credit line
    }

    // Reset scale
    font.getData().setScale(1.5f);
    }

    private void renderThankYouMessage() {
        font.setColor(1, 1, 1, alpha);

        GlyphLayout layout1 = new GlyphLayout(font, thankYouLine1);
        float text1X = (screenWidth - layout1.width) / 2;
        float text1Y = screenHeight / 2 + layout1.height / 2 + 20;

        GlyphLayout layout2 = new GlyphLayout(font, thankYouLine2);
        float text2X = (screenWidth - layout2.width) / 2;
        float text2Y = screenHeight / 2 - layout2.height / 2 - 20;

        font.draw(spriteBatch, thankYouLine1, text1X, text1Y);
        font.draw(spriteBatch, thankYouLine2, text2X, text2Y);
    }

    private void transitionToMainMenu() {
        try {
            // Direct transition without white fade
            backgroundMusic.stop();
            game.setScreen(new MainMenuScreen(game));
        } catch (Exception e) {
            GameLogger.logError("Failed to transition to MainMenuScreen", e);
            // Direct transition if the transition fails
            game.setScreen(new MainMenuScreen(game));
        }
    }

    // Helper classes
    private enum CreditLineType {
        ROLE,
        NAME,
        SPACING
    }

    private class CreditLine {
        String text;
        CreditLineType type;
        float customHeight = 0f;

        CreditLine(String text, CreditLineType type) {
            this.text = text;
            this.type = type;
        }

        CreditLine(String text, CreditLineType type, float customHeight) {
            this.text = text;
            this.type = type;
            this.customHeight = customHeight;
        }

        float getHeight() {
            if (type == CreditLineType.SPACING) {
                return customHeight;
            }

            float scale = type == CreditLineType.ROLE ? 1.5f : 1.2f;
            font.getData().setScale(scale);
            GlyphLayout layout = new GlyphLayout(font, text);
            font.getData().setScale(1.5f); // Reset to default
            return layout.height + 20;
        }
    }

    // Timer implementation
    private static class Timer {
        private static Array<Task> tasks = new Array<Task>();

        public static void schedule(Task task, float delaySeconds) {
            task.delaySeconds = delaySeconds;
            tasks.add(task);

            // Create a timer thread if none exists
            if (timerThread == null || !timerThread.isAlive()) {
                timerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!tasks.isEmpty()) {
                            try {
                                Thread.sleep(10); // Check every 10ms

                                for (int i = tasks.size - 1; i >= 0; i--) {
                                    Task t = tasks.get(i);
                                    t.delaySeconds -= 0.01f;

                                    if (t.delaySeconds <= 0) {
                                        final Task currentTask = t;
                                        tasks.removeIndex(i);

                                        // Execute task on main thread
                                        Gdx.app.postRunnable(new Runnable() {
                                            @Override
                                            public void run() {
                                                currentTask.run();
                                            }
                                        });
                                    }
                                }
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                });
                timerThread.setDaemon(true);
                timerThread.start();
            }
        }

        private static Thread timerThread;

        public static abstract class Task {
            float delaySeconds;
            public abstract void run();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        screenWidth = viewport.getWorldWidth();
        screenHeight = viewport.getWorldHeight();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        font.dispose();
        typingSound.dispose();
        titleTexture.dispose();
        backgroundMusic.dispose();
    }

    @Override public void show() {
        if(backgroundMusic != null) {
            backgroundMusic.play();
        }
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }
    }
}
