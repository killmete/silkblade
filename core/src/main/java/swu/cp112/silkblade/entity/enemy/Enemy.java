package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;

import java.util.List;

/**
 * Represents an enemy entity in the game that can engage in combat,
 * display visuals, and interact through dialogue.
 */
public interface Enemy {
    /**
     * Basic properties
     */
    String getName();

    /**
     * Health management
     */
    int getMaxHP();
    int getCurrentHP();
    void setHP(int hp);
    void damage(int amount, boolean isCritical);
    boolean isDefeated();

    /**
     * Visual properties and rendering
     */
    Texture getTexture();
    float getWidth();
    float getHeight();
    void draw(SpriteBatch batch, float x, float y);
    Color getPrimaryColor();
    
    /**
     * Position methods
     * Gets the X position of the enemy (center of sprite)
     * @return The X coordinate of the enemy's center
     */
    float getX();
    
    /**
     * Gets the Y position of the enemy (center of sprite)
     * @return The Y coordinate of the enemy's center
     */
    float getY();
    
    /**
     * Sets the enemy's center position
     * @param x The X coordinate for the enemy's center
     * @param y The Y coordinate for the enemy's center
     */
    void setPosition(float x, float y);

    /**
     * Combat system
     */
    void update(float delta);
    List<Bullet> generateAttack(float arenaX, float arenaY, float arenaWidth, float arenaHeight);
    int getAttackDamage();
    float getAttackInterval();
    float getArenaWidth();
    float getArenaHeight();
    int getMaxBullets();
    void scaleToPlayerLevel(int playerLevel);
    
    /**
     * Get the current attack pattern.
     * 
     * @return The current attack pattern, or null if none is set
     */
    EnemyAttackPattern getCurrentPattern();
    
    /**
     * Turn management
     */
    boolean isTurnActive();
    void startTurn();
    void endTurn();
    void setAlpha(float alpha);

    /**
     * Dialogue system
     */
    // Combat-related dialogue
    String getEncounterDialogue();
    String getAttackDialogue();
    String getDefeatDialogue();
    String getVictoryDialogue();
    String getRewardDialogue();

    // Turn-based dialogue
    String getTurnPassDialogue();
    String getPlayerTurnStartDialogue();

    int getExpReward();
    int getGoldReward();

    /**
     * Update the last known player position.
     * This method allows enemies to track player movement.
     *
     * @param x The x-coordinate of the player
     * @param y The y-coordinate of the player
     */
    void updatePlayerPosition(float x, float y);

    /**
     * Gets the custom combat background path for this enemy, if any.
     * @return The path to the background image, or null if no custom background is set.
     */
    default String getCombatBackground() {
        return null;
    }
    
    /**
     * Gets the custom combat music path for this enemy, if any.
     * @return The path to the music file, or null if no custom music is set.
     */
    default String getCombatMusic() {
        return null;
    }
}
