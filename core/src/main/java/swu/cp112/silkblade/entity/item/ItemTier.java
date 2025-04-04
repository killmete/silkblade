package swu.cp112.silkblade.entity.item;

import com.badlogic.gdx.graphics.Color;

/**
 * Represents the rarity tiers for items in the game.
 */
public enum ItemTier {
    NORMAL("Normal", new Color(1, 1, 1, 1)),              // White
    RARE("Rare", new Color(0.2f, 0.5f, 1f, 1)),          // Blue
    HEROIC("Heroic", new Color(0.7f, 0.3f, 1f, 1)),      // Purple
    LEGENDARY("Legendary", new Color(1, 0.385f, 0.385f, 1)),  // Red
    GENESIS("Genesis", null);                            // Rainbow animated (handled specially)

    private final String displayName;
    private final Color color;

    ItemTier(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Checks if this tier should use rainbow animation effect
     */
    public boolean isAnimated() {
        return this == GENESIS;
    }
}
