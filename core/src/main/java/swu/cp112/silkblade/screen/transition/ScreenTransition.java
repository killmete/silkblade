package swu.cp112.silkblade.screen.transition;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class ScreenTransition implements Screen {
    private Game game;
    private Screen currentScreen;
    private Screen nextScreen;
    private float transitionDuration = 0.5f; // Transition time in seconds
    private float currentTime = 0f;
    private TransitionType type;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FrameBuffer currentScreenBuffer;
    private FrameBuffer nextScreenBuffer;

    private static boolean isTransitioning = false;

    public enum TransitionType {
        FADE_TO_WHITE,
        CROSS_FADE
    }

    public static boolean isTransitioning() {
        return isTransitioning;
    }

    public ScreenTransition(Game game, Screen currentScreen, Screen nextScreen, TransitionType type) {
        this.game = game;
        this.currentScreen = currentScreen;
        this.nextScreen = nextScreen;
        this.type = type;

        batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();

        // Create frame buffers for capturing screen states
        currentScreenBuffer = new FrameBuffer(
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            false
        );

        nextScreenBuffer = new FrameBuffer(
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            false
        );

        isTransitioning = true;
    }

    @Override
    public void render(float delta) {
        // Reset blend function to ensure proper rendering
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Capture screens if not already captured
        captureScreens();

        currentTime += delta;
        float progress = Math.min(currentTime / transitionDuration, 1f);

        switch (type) {
            case FADE_TO_WHITE:
                renderFadeToWhiteTransition(progress);
                break;
            case CROSS_FADE:
                renderCrossFadeTransition(progress);
                break;
        }

        // Reset blend function again after rendering transition
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (progress >= 1f) {
            isTransitioning = false;
            game.setScreen(nextScreen);
            disposeBuffers();
        }
    }

    private void captureScreens() {
        // Capture current screen
        currentScreenBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        currentScreen.render(Gdx.graphics.getDeltaTime());
        currentScreenBuffer.end();

        // Capture next screen
        nextScreenBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        nextScreen.render(Gdx.graphics.getDeltaTime());
        nextScreenBuffer.end();
    }

    private void renderFadeToWhiteTransition(float progress) {
        Texture currentTexture = currentScreenBuffer.getColorBufferTexture();
        Texture nextTexture = nextScreenBuffer.getColorBufferTexture();

        Gdx.gl.glClearColor(1, 1, 1, 1);  // Clear to white
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Calculate dimensions to maintain aspect ratio
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float textureWidth = currentTexture.getWidth();
        float textureHeight = currentTexture.getHeight();

        // Calculate viewport coordinates (keeps image centered without stretching)
        float x = camera.position.x - (screenWidth / 2f);
        float y = camera.position.y - (screenHeight / 2f);

        // First half of transition: current screen fading to white
        if (progress < 0.5f) {
            float fadeToWhiteProgress = progress * 2f; // 0 to 1 in first half
            // Start from normal color (1,1,1) and increase RGB values to create white fade
            batch.setColor(
                1f + fadeToWhiteProgress,  // Red increases
                1f + fadeToWhiteProgress,  // Green increases
                1f + fadeToWhiteProgress,  // Blue increases
                1f - fadeToWhiteProgress   // Alpha decreases
            );
            batch.draw(currentTexture, x, y, screenWidth, screenHeight,
                    0, 0, currentTexture.getWidth(), currentTexture.getHeight(),
                    false, true);
        }
        // Second half of transition: white fading to next screen
        else {
            float fadeFromWhiteProgress = (progress - 0.5f) * 2f; // 0 to 1 in second half

            // Draw the next screen
            batch.setColor(1, 1, 1, fadeFromWhiteProgress);
            batch.draw(nextTexture, x, y, screenWidth, screenHeight,
                    0, 0, nextTexture.getWidth(), nextTexture.getHeight(),
                    false, true);

            // Overlay white that fades out
            batch.setColor(1, 1, 1, 1 - fadeFromWhiteProgress);
            batch.draw(currentTexture, x, y, screenWidth, screenHeight,
                    0, 0, currentTexture.getWidth(), currentTexture.getHeight(),
                    false, true);
        }

        batch.end();
    }

    private void renderCrossFadeTransition(float progress) {
        Texture currentTexture = currentScreenBuffer.getColorBufferTexture();
        Texture nextTexture = nextScreenBuffer.getColorBufferTexture();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Calculate dimensions to maintain aspect ratio
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float textureWidth = currentTexture.getWidth();
        float textureHeight = currentTexture.getHeight();

        // Calculate viewport coordinates (keeps image centered without stretching)
        float x = camera.position.x - (screenWidth / 2f);
        float y = camera.position.y - (screenHeight / 2f);

        // First half of transition: current screen fading to black
        if (progress < 0.5f) {
            float fadeOutProgress = progress * 2f; // 0 to 1 in first half
            batch.setColor(1 - fadeOutProgress, 1 - fadeOutProgress, 1 - fadeOutProgress, 1);
            batch.draw(currentTexture, x, y, screenWidth, screenHeight,
                    0, 0, currentTexture.getWidth(), currentTexture.getHeight(),
                    false, true);
        }
        // Second half of transition: next screen fading in
        else {
            float fadeInProgress = (progress - 0.5f) * 2f; // 0 to 1 in second half

            // Draw black background
            batch.setColor(0, 0, 0, 1);
            batch.draw(currentTexture, x, y, screenWidth, screenHeight,
                    0, 0, currentTexture.getWidth(), currentTexture.getHeight(),
                    false, true);

            // Fade in next screen
            batch.setColor(1, 1, 1, fadeInProgress);
            batch.draw(nextTexture, x, y, screenWidth, screenHeight,
                    0, 0, nextTexture.getWidth(), nextTexture.getHeight(),
                    false, true);
        }

        batch.end();
    }

    private void disposeBuffers() {
        if (currentScreenBuffer != null) {
            currentScreenBuffer.dispose();
        }
        if (nextScreenBuffer != null) {
            nextScreenBuffer.dispose();
        }
    }

    // Screen lifecycle methods
    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        // Resize buffers and screens
        disposeBuffers();
        
        try {
            // First attempt: try to create framebuffers with exact dimensions
            currentScreenBuffer = new FrameBuffer(
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888,
                width,
                height,
                false
            );
            nextScreenBuffer = new FrameBuffer(
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888,
                width,
                height,
                false
            );
        } catch (IllegalStateException e) {
            // Handle framebuffer creation error - some hardware requires power-of-two textures
            // Get nearest power of 2 dimensions
            int pot_width = nextPowerOfTwo(width);
            int pot_height = nextPowerOfTwo(height);
            
            // Log warning about frame buffer resize
            Gdx.app.log("ScreenTransition", "Failed to create framebuffer with dimensions " + 
                        width + "x" + height + ", using " + pot_width + "x" + pot_height + " instead");
            
            currentScreenBuffer = new FrameBuffer(
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888,
                pot_width,
                pot_height,
                false
            );
            nextScreenBuffer = new FrameBuffer(
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888,
                pot_width,
                pot_height,
                false
            );
        }

        currentScreen.resize(width, height);
        nextScreen.resize(width, height);
    }
    
    /**
     * Returns the next power of two value.
     * @param value The value to get the next power of two for
     * @return The next power of two
     */
    private int nextPowerOfTwo(int value) {
        if (value == 0) return 1;
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value + 1;
    }

    @Override
    public void dispose() {
        // Reset blend function to default before disposing
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.dispose();
        disposeBuffers();
    }

    @Override
    public void show() {
        // Reset blend function when showing this screen
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void hide() {
        // Reset blend function when hiding this screen
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override public void pause() {}
    @Override public void resume() {}
}
