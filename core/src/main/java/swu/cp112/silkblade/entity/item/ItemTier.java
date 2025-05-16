package swu.cp112.silkblade.entity.item;

import com.badlogic.gdx.graphics.Color;

/**
 * Represents the rarity tiers for items in the game.
 */
public enum ItemTier {
    NORMAL("Normal", new Color(0.9f, 0.9f, 0.9f, 1.0f), false),
    RARE("Rare", new Color(0.0f, 0.8f, 0.2f, 1.0f), false),
    HEROIC("Heroic", new Color(0.4f, 0.4f, 1.0f, 1.0f), false),
    LEGENDARY("Legendary", new Color(0.8f, 0.4f, 0.8f, 1.0f), false),
    GENESIS("Genesis", new Color(0.3f, 0.1f, 0.8f, 1.0f), true),
    END("End", new Color(1.0f, 0.5f, 0.0f, 1.0f), true);

    private final String displayName;
    private final Color color;
    private final boolean animated;

    ItemTier(String displayName, Color color, boolean animated) {
        this.displayName = displayName;
        this.color = color;
        this.animated = animated;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Checks if this tier should use animation effect
     */
    public boolean isAnimated() {
        return animated;
    }
}
