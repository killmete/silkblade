package swu.cp112.silkblade.entity.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;
import swu.cp112.silkblade.pattern.EnemyAttackPatternManager;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.screen.CombatScene;
import swu.cp112.silkblade.util.GameLogger;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEnemy implements Enemy {
    // Basic properties
    protected String name;
    protected int maxHP;
    protected int currentHP;
    protected Texture texture;
    protected float width;
    protected float height;
    protected boolean turnActive;
    protected Color primaryColor;
    protected Sound hitSound;
    protected Sound criticalSound;

    // Combat properties
    protected float arenaWidth;
    protected float arenaHeight;
    protected float attackInterval;
    protected int maxBullets;
    protected int attackDamage;

    // Reward properties
    protected int baseXP;
    protected int baseGold;
    protected int xp;
    protected int baht;

    // Dialogue properties
    protected String encounterDialogue;
    protected String attackDialogue;
    protected String defeatDialogue;
    protected String victoryDialogue;
    protected String rewardDialogue;
    protected String turnPassDialogue;
    protected String playerTurnStartDialogue;
    protected List<String> randomTurnDialogues;
    protected int lastDialogueIndex;

    // Visual effect properties
    protected boolean isShaking;
    protected float shakeTimer;
    protected float shakeDuration;
    protected float shakeIntensity;
    protected float shakeX;

    // Position properties
    protected float centerX; // Center X position of the enemy
    protected float centerY; // Center Y position of the enemy

    protected static final float LEVEL_SCALING_FACTOR = 1.2f;

    // Add this field to AbstractEnemy class
    protected float alpha = 1.0f;
    protected float currentAlpha = 1.0f;
    protected float targetAlpha = 1.0f;
    protected static final float ALPHA_TRANSITION_SPEED = 2.0f; // Adjust this to control transition speed
    protected float lastPlayerX = 0f;
    protected float lastPlayerY = 0f;

    // New fields for pattern management
    protected EnemyAttackPatternManager patternManager;
    protected EnemyAttackPattern currentPattern;

    private CombatScene combatScene;

    // Constructor and initialization methods
    public AbstractEnemy(String name, int maxHP, Texture texture, float width, float height) {
        initializeBasicProperties(name, maxHP, texture, width, height);
        initializeCombatProperties();
        initializeRewards();
        initializeDialogue();
        initializeShakeEffect();

        // Initialize position at center of screen by default
        this.centerX = Gdx.graphics.getWidth() / 2;
        this.centerY = Gdx.graphics.getHeight() / 2;

        // Initialize pattern manager but DON'T select pattern yet
        this.patternManager = new EnemyAttackPatternManager();
    }

    private void initializeBasicProperties(String name, int maxHP, Texture texture, float width, float height) {
        this.name = name;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.primaryColor = Color.WHITE;
        this.turnActive = false;
        this.hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.wav"));
        this.criticalSound = Gdx.audio.newSound(Gdx.files.internal("sounds/critical_hit.wav"));
    }

    public void scaleToPlayerLevel(int playerLevel) {
        int levelDiff = playerLevel - 1;

        // Scale HP exponentially
        this.maxHP = (int)(maxHP * Math.pow(LEVEL_SCALING_FACTOR, levelDiff));
        this.currentHP = this.maxHP;

        // Scale attack damage linearly with level
        this.attackDamage = (int)(attackDamage * (1 + levelDiff * 0.25f));  // Adjust this multiplier

        // Scale XP and gold rewards
        this.xp = (int)(baseXP * (1 + levelDiff * 0.5f));
        this.baht = (int)(baseGold * (1 + levelDiff * 0.3f));
        this.rewardDialogue = "You earned " + this.xp + " XP and " + this.baht + " GOLD.";
    }

    // Implementation of getX() and getY() methods
    @Override
    public float getX() {
        return centerX;
    }

    @Override
    public float getY() {
        return centerY;
    }

    // Set position method to update enemy's center position
    @Override
    public void setPosition(float x, float y) {
        this.centerX = x;
        this.centerY = y;
    }

    private void initializeCombatProperties() {
        this.arenaWidth = 250f;
        this.arenaHeight = 250f;
        this.attackInterval = 0.5f;
        this.maxBullets = 20;
        this.attackDamage = 5;
    }

    private void initializeRewards() {
        this.baseXP = 150;
        this.baseGold = 200;
        this.xp = this.baseXP;
        this.baht = this.baseGold;
    }

    public void setBaseRewards(int baseXP, int baseGold) {
        this.baseXP = baseXP;
        this.baseGold = baseGold;
        // Also update current XP and gold to the base values
        this.xp = baseXP;
        this.baht = baseGold;
    }

    private void initializeDialogue() {
        this.encounterDialogue = "An enemy appears!";
        this.attackDialogue = "The enemy attacks!";
        this.defeatDialogue = "You defeated " + this.name + "!";
        this.rewardDialogue = "You earned " + this.xp + " XP and " + this.baht + " GOLD.";
        this.victoryDialogue = "The enemy defeated you!";
        this.turnPassDialogue = "The enemy awaits your move.";
        this.playerTurnStartDialogue = "Your turn.";

        this.randomTurnDialogues = new ArrayList<>();
        this.randomTurnDialogues.add("The enemy shifts slightly.");
        this.randomTurnDialogues.add("The enemy looks at you.");
        this.randomTurnDialogues.add("The enemy seems determined.");
        this.lastDialogueIndex = -1;
    }

    private void initializeShakeEffect() {
        this.isShaking = false;
        this.shakeTimer = 0f;
        this.shakeDuration = 0.7f;
        this.shakeIntensity = 10.0f;
        this.shakeX = 0f;
    }

    // Basic property getters and setters
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMaxHP() {
        return maxHP;
    }

    @Override
    public int getCurrentHP() {
        return currentHP;
    }
    @Override
    public int getExpReward() {
        return xp;
    }

    @Override
    public int getGoldReward() {
        return baht;
    }

    @Override
    public void setHP(int hp) {
        this.currentHP = MathUtils.clamp(hp, 0, maxHP);
    }

    @Override
    public boolean isDefeated() {
        return currentHP <= 0;
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public Color getPrimaryColor() {
        return primaryColor;
    }

    @Override
    public void damage(int amount, boolean isCritical) {
        if (isCritical) {
            criticalSound.play(0.7f);
            // Apply critical hit effect
            startShake();
        } else {
            hitSound.play(0.7f);
        }
        this.currentHP = Math.max(0, this.currentHP - amount);
    }

    @Override
    public List<Bullet> generateAttack(float arenaX, float arenaY, float arenaWidth, float arenaHeight) {
        // Pass back to the current pattern if available
        if (currentPattern != null) {
            List<Bullet> generatedBullets = currentPattern.generateBullets(this, arenaX, arenaY, arenaWidth, arenaHeight);
            
            // Notify subclasses that bullets were spawned
            if (generatedBullets != null && !generatedBullets.isEmpty()) {
                onBulletsSpawned();
            }
            
            return generatedBullets;
        }
        return new ArrayList<>();
    }

    /**
     * Called when bullets are successfully generated and spawned.
     * Override this in subclasses to track spawn cycles or implement pattern-based logic.
     */
    protected void onBulletsSpawned() {
        // Default implementation does nothing
        // Subclasses can override this to add custom logic
    }

    @Override
    public int getAttackDamage() {
        return attackDamage;
    }

    @Override
    public float getAttackInterval() {
        if (currentPattern != null) {
            return currentPattern.getConfig().getAttackInterval();
        }
        return attackInterval;
    }

    @Override
    public float getArenaWidth() {
        if (currentPattern != null) {
            return currentPattern.getConfig().getArenaWidth();
        }
        return arenaWidth;
    }

    @Override
    public float getArenaHeight() {
        if (currentPattern != null) {
            return currentPattern.getConfig().getArenaHeight();
        }
        return arenaHeight;
    }

    @Override
    public int getMaxBullets() {
        if (currentPattern != null) {
            return currentPattern.getConfig().getMaxBullets();
        }
        return maxBullets;
    }

    @Override
    public EnemyAttackPattern getCurrentPattern() {
        return currentPattern;
    }

    @Override
    public boolean isTurnActive() {
        return turnActive;
    }

    @Override
    public void startTurn() {
        this.turnActive = true;
        
        // Log before pattern selection
        if (currentPattern != null) {
            GameLogger.logInfo("Enemy turn started with pattern: " + currentPattern.getPatternName());
        } else {
            GameLogger.logInfo("Enemy turn started (no pattern yet)");
        }
        
        // When enemy turn starts, select a random attack pattern if using patterns
        selectNewPattern();
        
        // Log after pattern selection
        if (currentPattern != null) {
            GameLogger.logInfo("Current pattern after selection: " + currentPattern.getPatternName());
        }
    }

    @Override
    public void endTurn() {
        this.turnActive = false;
    }

    @Override
    public String getTurnPassDialogue() {
        // If we have custom dialogues registered, use those
        if (!randomTurnDialogues.isEmpty()) {
            int nextIndex;
            do {
                nextIndex = MathUtils.random(0, randomTurnDialogues.size() - 1);
            } while (nextIndex == lastDialogueIndex && randomTurnDialogues.size() > 1);

            lastDialogueIndex = nextIndex;
            return randomTurnDialogues.get(nextIndex);
        }

        return turnPassDialogue;
    }

    @Override
    public String getPlayerTurnStartDialogue() {
        return playerTurnStartDialogue;
    }

    @Override
    public String getEncounterDialogue() {
        return encounterDialogue;
    }

    @Override
    public String getAttackDialogue() {
        return attackDialogue;
    }

    @Override
    public String getDefeatDialogue() {
        return defeatDialogue;
    }

    @Override
    public String getRewardDialogue() {
        return rewardDialogue;
    }

    @Override
    public String getVictoryDialogue() {
        return victoryDialogue;
    }

    public void setTurnPassDialogue(String dialogue) {
        this.turnPassDialogue = dialogue;
    }

    public void setPlayerTurnStartDialogue(String dialogue) {
        this.playerTurnStartDialogue = dialogue;
    }

    public void addRandomTurnDialogue(String dialogue) {
        this.randomTurnDialogues.add(dialogue);
    }

    public void clearRandomTurnDialogues() {
        this.randomTurnDialogues.clear();
    }

    // Visual effect methods
    public void startShake() {
        isShaking = true;
        shakeTimer = shakeDuration;
        shakeX = 0f;
    }

    public boolean isShaking() {
        return isShaking;
    }

    public void setShakeDuration(float duration) {
        this.shakeDuration = duration;
    }

    public void setShakeIntensity(float intensity) {
        this.shakeIntensity = intensity;
    }

    @Override
    public void update(float delta) {
        updateShakeEffect(delta);
        updateAlpha(delta);
    }

    private void updateShakeEffect(float delta) {
        if (isShaking) {
            shakeTimer -= delta;
            if (shakeTimer <= 0) {
                isShaking = false;
                shakeX = 0f;
            } else {
                shakeX = MathUtils.sin(shakeTimer * 30) * shakeIntensity * (shakeTimer / shakeDuration);
            }
        }
    }

    private void updateAlpha(float delta) {
        if (currentAlpha != targetAlpha) {
            if (currentAlpha < targetAlpha) {
                currentAlpha = Math.min(currentAlpha + (ALPHA_TRANSITION_SPEED * delta), targetAlpha);
            } else {
                currentAlpha = Math.max(currentAlpha - (ALPHA_TRANSITION_SPEED * delta), targetAlpha);
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        Color prevColor = batch.getColor().cpy();
        batch.setColor(1, 1, 1, currentAlpha);
        float drawX = x + shakeX;

        // Update enemy's center position based on the provided coordinates
        this.centerX = drawX + width / 2;
        this.centerY = y + height / 2;

        batch.draw(texture, drawX, y, width, height);
        batch.setColor(prevColor);
    }

    // Sound effect methods
    public void setHitSound(Sound sound) {
        if (this.hitSound != null) {
            this.hitSound.dispose();
        }
        this.hitSound = sound;
    }

    public void setHitSound(String soundPath) {
        if (this.hitSound != null) {
            this.hitSound.dispose();
        }
        this.hitSound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
    }

    // Reward methods
    public void setRewards(int xp, int baht) {
        this.xp = xp;
        this.baht = baht;
        this.rewardDialogue = "You earned " + this.xp + " XP!";
    }

    // Add setter for alpha
    public void setAlpha(float alpha) {
        this.targetAlpha = MathUtils.clamp(alpha, 0f, 1f);
    }

    // Resource management methods
    public void dispose() {
        if (hitSound != null) {
            hitSound.dispose();
        }
        if (criticalSound != null) {
            criticalSound.dispose();
        }
    }

    @Override
    public void updatePlayerPosition(float x, float y) {
        this.lastPlayerX = x;
        this.lastPlayerY = y;
    }

    // New method to handle pattern selection
    protected void selectNewPattern() {
        if (patternManager != null) {
            // Store the old pattern for comparison
            EnemyAttackPattern oldPattern = currentPattern;
            
            // Select a new pattern
            currentPattern = patternManager.selectRandomPattern();

            // Force a position update after pattern changes
            if (lastPlayerX != 0 || lastPlayerY != 0) {
                // Only update if we've had a valid position before
                // This ensures the update uses the last known good position
                updatePlayerPosition(lastPlayerX, lastPlayerY);
            }

            // IMPORTANT: Always notify combat scene of pattern change to ensure arena dimensions
            // are up-to-date, even if the pattern didn't change
            if (combatScene != null) {
                // Notify combat scene of pattern change to update arena dimensions
                combatScene.updateArenaForPattern();
                
                if (currentPattern != oldPattern) {
                    GameLogger.logInfo("Pattern changed to: " + currentPattern.getPatternName() + 
                                      " - Arena: " + currentPattern.getConfig().getArenaWidth() + "x" + 
                                      currentPattern.getConfig().getArenaHeight());
                } else {
                    GameLogger.logInfo("Using same pattern: " + currentPattern.getPatternName() + 
                                      " - Arena: " + currentPattern.getConfig().getArenaWidth() + "x" + 
                                      currentPattern.getConfig().getArenaHeight());
                }
            }
        }
    }

    // Add method to add patterns
    public void addAttackPattern(EnemyAttackPattern pattern) {
        if (patternManager != null) {
            patternManager.addPattern(pattern);
        }
    }

    // Add these getter methods
    public float getLastPlayerX() {
        return lastPlayerX;
    }

    public float getLastPlayerY() {
        return lastPlayerY;
    }

    // Add this new method
    protected void initializePatterns() {
        if (patternManager.getCurrentPattern() == null) {
            selectNewPattern();
        }
    }

    public void setCombatScene(CombatScene scene) {
        this.combatScene = scene;
    }

    public void triggerScreenShake(float duration, float intensity) {
        if (combatScene != null) {
            combatScene.startShake(duration, intensity);
        }
    }
}
