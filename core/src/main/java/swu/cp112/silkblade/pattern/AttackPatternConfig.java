package swu.cp112.silkblade.pattern;

import com.badlogic.gdx.graphics.Color;

/**
 * Configuration class for enemy attack patterns.
 * Allows for easy definition and modification of attack characteristics.
 */
public class AttackPatternConfig {
    private final float minDamage;
    private final float maxDamage;
    private final float speedMultiplier;
    private final float attackInterval;
    private final int maxBullets;
    private final float arenaWidth;
    private final float arenaHeight;
    private final Color bulletColor;
    private final boolean isHealing;
    private final String patternDescription;
    private final float endPhaseDelay; // Delay before combat phase ends after all bullets are fired

    /**
     * Constructor for attack pattern configuration.
     *
     * @param minDamage Minimum damage for bullets
     * @param maxDamage Maximum damage for bullets
     * @param speedMultiplier Bullet speed multiplier
     * @param attackInterval Time between attacks
     * @param maxBullets Maximum number of bullets
     * @param arenaWidth Arena width for this pattern
     * @param arenaHeight Arena height for this pattern
     * @param bulletColor Color of bullets
     * @param isHealing Whether the pattern is healing
     * @param patternDescription Description of the attack pattern
     */
    public AttackPatternConfig(float minDamage, float maxDamage, float speedMultiplier,
                               float attackInterval, int maxBullets, float arenaWidth,
                               float arenaHeight, Color bulletColor, boolean isHealing,
                               String patternDescription) {
        this(minDamage, maxDamage, speedMultiplier, attackInterval, maxBullets, 
            arenaWidth, arenaHeight, bulletColor, isHealing, patternDescription, 2.0f);
    }
    
    /**
     * Constructor for attack pattern configuration with custom end phase delay.
     *
     * @param minDamage Minimum damage for bullets
     * @param maxDamage Maximum damage for bullets
     * @param speedMultiplier Bullet speed multiplier
     * @param attackInterval Time between attacks
     * @param maxBullets Maximum number of bullets
     * @param arenaWidth Arena width for this pattern
     * @param arenaHeight Arena height for this pattern
     * @param bulletColor Color of bullets
     * @param isHealing Whether the pattern is healing
     * @param patternDescription Description of the attack pattern
     * @param endPhaseDelay Delay before combat phase ends after all bullets are fired
     */
    public AttackPatternConfig(float minDamage, float maxDamage, float speedMultiplier,
                               float attackInterval, int maxBullets, float arenaWidth,
                               float arenaHeight, Color bulletColor, boolean isHealing,
                               String patternDescription, float endPhaseDelay) {
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.speedMultiplier = speedMultiplier;
        this.attackInterval = attackInterval;
        this.maxBullets = maxBullets;
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.bulletColor = bulletColor;
        this.isHealing = isHealing;
        this.patternDescription = patternDescription;
        this.endPhaseDelay = endPhaseDelay;
    }

    // Getters for all fields
    public float getMinDamage() { return minDamage; }
    public float getMaxDamage() { return maxDamage; }
    public float getSpeedMultiplier() { return speedMultiplier; }
    public float getAttackInterval() { return attackInterval; }
    public int getMaxBullets() { return maxBullets; }
    public float getArenaWidth() { return arenaWidth; }
    public float getArenaHeight() { return arenaHeight; }
    public Color getBulletColor() { return bulletColor; }
    public boolean isHealing() { return isHealing; }
    public String getPatternDescription() { return patternDescription; }
    public float getEndPhaseDelay() { return endPhaseDelay; }
}
