package swu.cp112.silkblade.lwjgl3;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import swu.cp112.silkblade.core.Main;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    private static final Graphics.DisplayMode screen = Lwjgl3ApplicationConfiguration.getDisplayMode();

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return ; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Silk Blade: Silk Warriors of Lamphun");
        configuration.useVsync(true);
        configuration.setForegroundFPS(screen.refreshRate + 1);
        configuration.setWindowedMode(1280, 720);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}

