package swu.cp112.silkblade.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import swu.cp112.silkblade.core.Main;

/**
 * Utility class to manage screens and get the current active screen.
 */
public class ScreenManager {
    
    private static Screen currentScreen;
    
    /**
     * Set the current screen
     * @param screen The screen to set as current
     */
    public static void setCurrentScreen(Screen screen) {
        currentScreen = screen;
    }
    
    /**
     * Get the current active screen
     * @return The current screen or null if no screen is set
     */
    public static Screen getCurrentScreen() {
        if (currentScreen == null) {
            // If not explicitly set, get from the game instance
            Game game = Main.getGame();
            if (game != null) {
                return game.getScreen();
            }
        }
        return currentScreen;
    }
} 