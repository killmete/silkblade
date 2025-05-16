package swu.cp112.silkblade.entity.combat;

import java.io.FileNotFoundException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.entity.item.Equipment;
import swu.cp112.silkblade.entity.item.Inventory;
import swu.cp112.silkblade.util.GameLogger;
import swu.cp112.silkblade.screen.CombatScene;
import swu.cp112.silkblade.screen.ScreenManager;

public class Player implements Json.Serializable {
    private static final String SAVE_FILE = "save/player_save.json";
    private static final int[] EXP_REQUIREMENTS = {
        0,      // LV 1:   0 EXP
        10,     // LV 2:  10 EXP
        30,     // LV 3:  30 EXP
        70,     // LV 4:  70 EXP
        120,    // LV 5: 120 EXP
        200,    // LV 6: 200 EXP
        300,    // LV 7: 300 EXP
        500,    // LV 8: 500 EXP
        800,    // LV 9: 800 EXP
        1200,   // LV 10: 1200 EXP
        1700,   // LV 11: 1700 EXP
        2500,   // LV 12: 2500 EXP
        3500,   // LV 13: 3500 EXP
        5000,   // LV 14: 5000 EXP
        7000,   // LV 15: 7000 EXP
        10000,  // LV 16: 10000 EXP
        15000,  // LV 17: 15000 EXP
        25000,  // LV 18: 25000 EXP
        50000,  // LV 19: 50000 EXP
        99999   // LV 20: 99999 EXP
    };

    private static final int MAX_LEVEL = 20;

    // Stats
    private String name;
    private int level;
    private int exp;
    private int mp;
    private int maxMP;
    private int attack;
    private float critRate;
    private int defense;
    private int maxHP;
    private int currentHP;
    private int mpBeforeConsume;
    private int currentStage = 1; // Default to stage 1
    private int gold = 0;         // Default to 0 gold

    // Boss tracking
    private boolean boss1Defeated = false;
    private boolean boss2Defeated = false;
    private boolean boss3Defeated = false;
    private boolean boss4Defeated = false;
    private boolean boss5Defeated = false;

    // Visual representation
    private transient Texture texture;  // transient means it won't be serialized
    private transient Sprite sprite;

    // Sound Effects
    private transient Sound basicAttackSound;
    private transient Sound skill1Sound;
    private transient Sound skill2Sound;
    private transient Sound skill3Sound;
    private transient Sound skill4Sound;
    private transient Sound skill5Sound;
    private transient Sound skill6Sound;

    // Add this field to store sound durations
    private float[] skillSoundDurations;

    // Add this near the other constants
    private static final int[] SKILL_MP_COSTS = {
        0,    // BASIC:  0 MP
        15,   // SKILL1: 15 MP - Dark Silk Face Slap
        25,   // SKILL2: 25 MP - Silk Scratch of Salvation
        30,   // SKILL3: 30 MP - Duckfoot Knot from Heaven
        35,   // SKILL4: 35 MP - Sai-Oua Silk Wrap
        50,   // SKILL5: 50 MP - Lamphun Blade: Piip Slash Supreme
        -1    // SKILL6: special case - uses all MP
    };

    private static final float BASE_MP_REGEN = 1; // Base MP regen per turn
    private static final float MP_REGEN_LEVEL_SCALING = 2.5f; // Additional MP regen per level

    // Track free skill cast availability
    private boolean freeSkillCastAvailable = false;

    public enum SkillType {
        BASIC("Slash"),  // Always available
        SKILL1("Dark Silk Face Slap"),
        SKILL2("Silk Scratch of Salvation"),
        SKILL3("Duckfoot Knot from Heaven"),
        SKILL4("Sai-Oua Silk Wrap"),
        SKILL5("Lamphun Blade: Piip Slash Supreme"),
        SKILL6("Silk End - I Am Cosmic Weave");

        private final String displayName;

        SkillType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Add these fields with other private fields
    private boolean[] unlockedSkills;
    private SkillType currentSkill;

    // Add inventory system
    private Inventory inventory;

    // Add buff manager for temporary effects
    private transient BuffManager buffManager;

    public Player() {
        // Default constructor for JSON deserialization
        this.level = 1;
        this.exp = 0;
        this.attack = 12;
        this.mp = 20;     // Starting MP
        this.maxMP = 20;  // Starting max MP
        this.defense = 10;
        this.maxHP = 20;
        this.currentHP = maxHP;
        this.critRate = 0.1f;
        this.currentStage = 1;
        this.gold = 0;
        this.inventory = new Inventory();
        // Initialize boss tracking to false
        this.boss1Defeated = false;
        this.boss2Defeated = false;
        this.boss3Defeated = false;
        this.boss4Defeated = false;
        this.boss5Defeated = false;
        initializeSprite();
        initializeSkills();
        initializeSounds();
        this.buffManager = new BuffManager(this);
    }

    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.exp = 0;
        this.attack = 12;
        this.mp = 20;     // Starting MP
        this.maxMP = 20;  // Starting max MP
        this.defense = 10;
        this.maxHP = 20;
        this.currentHP = 20;
        this.critRate = 0.1f;
        this.currentStage = 1;
        this.gold = 0;
        this.inventory = new Inventory();
        // Initialize boss tracking to false
        this.boss1Defeated = false;
        this.boss2Defeated = false;
        this.boss3Defeated = false;
        this.boss4Defeated = false;
        this.boss5Defeated = false;
        initializeSprite();
        initializeSkills();
        initializeSounds();
        this.buffManager = new BuffManager(this);
    }

    private void initializeSprite() {
        try {
            texture = new Texture("player.png");
            sprite = new Sprite(texture);
        } catch (Exception e) {
            GameLogger.logError("Failed to initialize player sprite", e);
        }
    }

    private void initializeSounds() {
        try {
            // Initialize the sounds array to store durations
            skillSoundDurations = new float[SkillType.values().length];

            // Load sounds and their durations
            basicAttackSound = Gdx.audio.newSound(Gdx.files.internal("sounds/attack.wav"));
            skill1Sound = Gdx.audio.newSound(Gdx.files.internal("sounds/skill1.wav"));
            skill2Sound = Gdx.audio.newSound(Gdx.files.internal("sounds/skill2.wav"));
            skill3Sound = Gdx.audio.newSound(Gdx.files.internal("sounds/skill3.wav"));
            skill4Sound = Gdx.audio.newSound(Gdx.files.internal("sounds/skill4.wav"));
            skill5Sound = Gdx.audio.newSound(Gdx.files.internal("sounds/skill5.wav"));
            skill6Sound = Gdx.audio.newSound(Gdx.files.internal("sounds/skill6.wav"));

            // Store durations using the helper method
            skillSoundDurations[SkillType.BASIC.ordinal()] = getSoundDuration("sounds/attack.wav");
            skillSoundDurations[SkillType.SKILL1.ordinal()] = getSoundDuration("sounds/skill1.wav");
            skillSoundDurations[SkillType.SKILL2.ordinal()] = getSoundDuration("sounds/skill2.wav");
            skillSoundDurations[SkillType.SKILL3.ordinal()] = getSoundDuration("sounds/skill3.wav");
            skillSoundDurations[SkillType.SKILL4.ordinal()] = getSoundDuration("sounds/skill4.wav");
            skillSoundDurations[SkillType.SKILL5.ordinal()] = getSoundDuration("sounds/skill5.wav");
            skillSoundDurations[SkillType.SKILL6.ordinal()] = getSoundDuration("sounds/skill6.wav");
        } catch (Exception e) {
            GameLogger.logError("Failed to initialize player sounds", e);
        }
    }

    // Add helper method to get sound duration
    private float getSoundDuration(String path) {
        try {
            // Load the sound file as a WAV file to get its duration
            FileHandle file = Gdx.files.internal(path);
            if (!file.exists()) {
                GameLogger.logError("Sound file not found: " + path, new FileNotFoundException(path));
                return 1.0f; // Default duration if file not found
            }

            // Read WAV file header to get duration
            byte[] bytes = file.readBytes();

            // Validate WAV file header (first 4 bytes should be "RIFF")
            if (bytes.length < 44 ||
                bytes[0] != 'R' || bytes[1] != 'I' ||
                bytes[2] != 'F' || bytes[3] != 'F') {
                System.err.println("Invalid WAV file: " + path);
                return 1.0f; // Default duration
            }

            // Extract data chunk size (bytes 40-43 in WAV format)
            int dataSize = ((bytes[43] & 0xff) << 24) |
                ((bytes[42] & 0xff) << 16) |
                ((bytes[41] & 0xff) << 8)  |
                (bytes[40] & 0xff);

            // Get sample rate (bytes 24-27)
            int sampleRate = ((bytes[27] & 0xff) << 24) |
                ((bytes[26] & 0xff) << 16) |
                ((bytes[25] & 0xff) << 8)  |
                (bytes[24] & 0xff);

            // Get number of channels (bytes 22-23)
            int channels = ((bytes[23] & 0xff) << 8) | (bytes[22] & 0xff);

            // Get bits per sample (bytes 34-35)
            int bitsPerSample = ((bytes[35] & 0xff) << 8) | (bytes[34] & 0xff);

            // Calculate duration in seconds
            return (float) dataSize / (sampleRate * channels * ((float) bitsPerSample / 8));
        } catch (Exception e) {
            GameLogger.logError("Error reading sound duration: " + path, e);
            return 1.0f; // Default duration if there's an error
        }
    }

    private void initializeSkills() {
        unlockedSkills = new boolean[SkillType.values().length];
        unlockedSkills[SkillType.BASIC.ordinal()] = true;  // BASIC is always unlocked
        unlockedSkills[SkillType.SKILL1.ordinal()] = false;  // Dark Silk Face Slap (unlocked at stage 10)
        unlockedSkills[SkillType.SKILL2.ordinal()] = false;  // Silk Scratch of Salvation (unlocked after 30th floor boss)
        unlockedSkills[SkillType.SKILL3.ordinal()] = false;  // Duckfoot Knot from Heaven (unlocked at level 12)
        unlockedSkills[SkillType.SKILL4.ordinal()] = false;  // Sai-Oua Silk Wrap (unlocked after 20th floor boss)
        unlockedSkills[SkillType.SKILL5.ordinal()] = false;  // Lamphun Blade: Piip Slash Supreme (unlocked after 40th floor boss)
        unlockedSkills[SkillType.SKILL6.ordinal()] = false;  // Silk End - I Am Cosmic Weave (unlocked after defeating Boss 5)
        currentSkill = SkillType.BASIC;
    }

    // Experience and Leveling
    public boolean gainExp(int amount) {
        if (level >= MAX_LEVEL) {
            return false;
        }

        int oldLevel = level;
        exp += amount;

        // Check for level up
        while (level < MAX_LEVEL && exp >= getExpToNextLevel()) {
            level++;

            // Modified stat increase formulas for better scaling:
            // Tune down HP scaling to be more linear
            // HP increases more significantly at higher levels
            maxHP += 5 + (int)(level * 1.2f);  // Changed from 4 + (level * 3)
            currentHP = maxHP;  // Full heal on level up

            // Tune down Attack scaling to be more linear
            // Attack increases more with level
            attack += 2 + (int)(level * 1.05f);  // Changed from 2 + (int)(level * 1.5f)

            // MP scales better with level (keep this the same)
            maxMP += 3 + (int)(level * 1.087f);

            // Tune down Defense scaling to be more linear
            // Defense scales better but not too quickly
            defense += 1 + (int)(level * 0.4f);  // Changed from 1 + (int)(level * 0.7f)

            // Crit rate increases more significantly (keep this the same)
            critRate = Math.min(0.1f + (level * 0.008f), 0.35f);

            // Check for skill unlocks
            checkSkillUnlocks();
        }

        // Cap exp at max level requirement
        if (level >= MAX_LEVEL) {
            exp = EXP_REQUIREMENTS[MAX_LEVEL - 1];
        }

        // Save after gaining exp
        saveToFile();

        return level > oldLevel;
    }

    // Combat Methods
    public static class DamageResult {
        public final int damage;
        public final boolean isCritical;
        public final boolean isDoubleAttack;

        public DamageResult(int damage, boolean isCritical) {
            this(damage, isCritical, false);
        }

        public DamageResult(int damage, boolean isCritical, boolean isDoubleAttack) {
            this.damage = damage;
            this.isCritical = isCritical;
            this.isDoubleAttack = isDoubleAttack;
        }
    }

    public DamageResult calculateDamage() {
        float critRoll = (float) Math.random();
        boolean isCrit = critRoll <= getCritRate();  // Use getCritRate() to include bonuses
        int totalAttack = getAttack();  // Use getAttack() to include equipment bonuses
        int baseDamage = totalAttack + (int)(Math.random() * 3) - 1; // -1 to +1 variance
        int finalDamage = isCrit ? baseDamage * 3 : baseDamage;

        // Check for double attack
        boolean isDoubleAttack = inventory.hasDoubleAttack();

        // If double attack, multiply damage by 2
        if (isDoubleAttack) {
            finalDamage *= 2;
            GameLogger.logInfo("Double attack triggered! Damage multiplied by 2.");
        }

        return new DamageResult(finalDamage, isCrit, isDoubleAttack);
    }

    public DamageResult calculateSkillDamage(SkillType skill) {
        int mpCost = calculateMPCost(skill);

        // Updated damage multipliers for better scaling
        float damageMultiplier;
        switch (skill) {
            case BASIC:
                damageMultiplier = 1.0f;
                break;
            case SKILL1:
                // Dark Silk Face Slap - 1.2x ATK
                damageMultiplier = 1.2f;
                GameLogger.logInfo("Skill 1 using ATK: " + getAttack() + " (base: " + attack +
                                  ", bonus: " + (getAttack() - attack) + ")");
                break;
            case SKILL2:
                // Silk Scratch of Salvation - healing skill
                // Return 0 damage since this is a healing skill
                return new DamageResult(0, false, false);
            case SKILL3:
                // Duckfoot Knot from Heaven - 2.6x ATK + defense buff
                damageMultiplier = 2.6f;
                GameLogger.logInfo("Skill 3 using ATK: " + getAttack() + " (base: " + attack +
                                  ", bonus: " + (getAttack() - attack) + ")");
                break;
            case SKILL4:
                // Sai-Oua Silk Wrap - defensive skill with healing
                // Return 0 damage since this is mainly a buff skill
                return new DamageResult(0, false, false);
            case SKILL5:
                // Lamphun Blade: Piip Slash Supreme - 4.0x ATK
                damageMultiplier = 4.0f;
                GameLogger.logInfo("Skill 5 using ATK: " + getAttack() + " (base: " + attack +
                                  ", bonus: " + (getAttack() - attack) + ")");
                break;
            case SKILL6:
                // Silk End - I Am Cosmic Weave: 3.0 * MP * ATK - UNCAPPED TRUE FINAL ATTACK
                // Use all MP with no effective limit
                int effectiveMp = mpBeforeConsume;
                // Calculate multiplier based on all MP used (3.0 * MP/100)
                float mpMultiplier = 3.0f * effectiveMp / 10.0f;
                // No cap - let it scale to the maximum possible value
                // Final damage multiplier
                damageMultiplier = mpMultiplier;
                GameLogger.logInfo("Silk End ultimate attack using ATK: " + getAttack() + ", MP: " + effectiveMp +
                                  ", Uncapped Multiplier: " + mpMultiplier);
                break;
            default:
                damageMultiplier = 1.0f;
        }

        DamageResult baseResult = calculateDamage();
        int modifiedDamage = (int)(baseResult.damage * damageMultiplier);

        // Double attack only applies to BASIC attacks, not to skills
        boolean isDoubleAttack = skill == SkillType.BASIC ? baseResult.isDoubleAttack : false;

        return new DamageResult(modifiedDamage, baseResult.isCritical, isDoubleAttack);
    }

    public String getSkillMessage(SkillType skill, int damage, Enemy currentEnemy) {
        switch (skill) {
            case BASIC:
                return "You dealt " + damage + " damage to " + currentEnemy.getName() + "!";
            case SKILL1:
                return "Your Dark Silk Face Slap dealt " + damage + " damage to " + currentEnemy.getName() + "!";
            case SKILL2:
                // Silk Scratch of Salvation - healing skill
                int healAmount = (int)(getMaxHP() * 0.2);
                return "You cast Silk Scratch of Salvation and healed yourself for " + healAmount + " HP!";
            case SKILL3:
                return "Your Duckfoot Knot from Heaven dealt " + damage + " damage to " + currentEnemy.getName() + " and increased your DEF by 50 for 2 turns!";
            case SKILL4:
                // Sai-Oua Silk Wrap - defensive skill with healing
                int healAmount2 = (int)(getMaxHP() * 0.35);
                return "You cast Sai-Oua Silk Wrap, increasing your DEF by 100 and healing " + healAmount2 + " HP!";
            case SKILL5:
                return "Your Lamphun Blade: Piip Slash Supreme dealt " + damage + " damage to " + currentEnemy.getName() + "!";
            case SKILL6:
                return "Your Silk End - I Am Cosmic Weave unleashed the power of silk with " + damage + " massive damage to " + currentEnemy.getName() + "!";
            default:
                return "You dealt " + damage + " damage to " + currentEnemy.getName() + "!";
        }
    }

    public String useSkillMessage(SkillType skill) {
        int mpCost = calculateMPCost(skill);
        boolean isFree = hasFreeSkillCastAvailable();
        String mpText = isFree ? "FREE!" : mpCost + " MP";

        switch (skill) {
            case BASIC:
                return "You used Slash!";
            case SKILL1:
                return "You cast Dark Silk Face Slap using " + mpText + "!";
            case SKILL2:
                return "You cast Silk Scratch of Salvation using " + mpText + "!";
            case SKILL3:
                return "You cast Duckfoot Knot from Heaven using " + mpText + "!";
            case SKILL4:
                return "You cast Sai-Oua Silk Wrap using " + mpText + "!";
            case SKILL5:
                return "You cast Lamphun Blade: Piip Slash Supreme using " + mpText + "!";
            case SKILL6:
                if (isFree) {
                    return "You unleash Silk End - I Am Cosmic Weave for FREE!";
                } else {
                    return "You unleash Silk End - I Am Cosmic Weave using all your MP (" + mpCost + " MP)!";
                }
            default:
                return "You used Slash!";
        }
    }

    public float getSkillDuration(SkillType skill) {
        return skillSoundDurations[skill.ordinal()];
    }
    public float playSkillSound() {
        int timesPlayed = 0;
        float volume = 0.09f;
        Sound soundToPlay;
        int skillIndex = currentSkill.ordinal();

        switch (currentSkill) {
            case BASIC:
                soundToPlay = basicAttackSound;
                break;
            case SKILL1:
                soundToPlay = skill1Sound;
                break;
            case SKILL2:
                soundToPlay = skill2Sound;
                break;
            case SKILL3:
                soundToPlay = skill3Sound;
                break;
            case SKILL4:
                soundToPlay = skill4Sound;
                break;
            case SKILL5:
                soundToPlay = skill5Sound;
                break;
            case SKILL6:
                soundToPlay = skill6Sound;
                break;
            default:
                soundToPlay = basicAttackSound;
                skillIndex = SkillType.BASIC.ordinal();
        }

        if (soundToPlay != null) {
            soundToPlay.play(volume);
        }

        return skillSoundDurations[skillIndex];
    }

    /**
     * Plays only the sound for a specific skill without returning the duration.
     * Used for double attack effects.
     *
     * @param skill The skill to play the sound for
     */
    public void playSkillSoundOnly(SkillType skill) {
        float volume = 0.09f;
        Sound soundToPlay;

        switch (skill) {
            case BASIC:
                soundToPlay = basicAttackSound;
                break;
            case SKILL1:
                soundToPlay = skill1Sound;
                break;
            case SKILL2:
                soundToPlay = skill2Sound;
                break;
            case SKILL3:
                soundToPlay = skill3Sound;
                break;
            case SKILL4:
                soundToPlay = skill4Sound;
                break;
            case SKILL5:
                soundToPlay = skill5Sound;
                break;
            case SKILL6:
                soundToPlay = skill6Sound;
                break;
            default:
                soundToPlay = basicAttackSound;
        }

        if (soundToPlay != null) {
            soundToPlay.play(volume);
        }
    }

    public void takeDamage(int damage) {
        int totalDefense = getDefense(); // This includes base defense + equipment bonuses
        int baseDefense = defense; // Base defense without equipment
        int defenseBonus = totalDefense - baseDefense; // Equipment bonus portion

        int actualDamage = Math.max(1, damage - (totalDefense / 2));
        currentHP = Math.max(0, currentHP - actualDamage);

        // Apply thorn damage to enemy if the player has thorn equipment
        float thornDamagePercent = inventory.getTotalThornDamage();
        if (thornDamagePercent > 0 && ScreenManager.getCurrentScreen() instanceof CombatScene) {
            CombatScene combatScene = (CombatScene) ScreenManager.getCurrentScreen();
            int thornDamageAmount = (int)(actualDamage * thornDamagePercent);

            if (thornDamageAmount > 0) {
                GameLogger.logInfo("Thorn damage reflected: " + thornDamageAmount);
                // Get the current enemy and apply damage silently
                if (combatScene.getCurrentEnemy() != null) {
                    Enemy enemy = combatScene.getCurrentEnemy();

                    // Don't apply any thorn damage if enemy is already at 1 HP
                    if (enemy.getCurrentHP() <= 1) {
                        thornDamageAmount = 0;
                        GameLogger.logInfo("Thorn damage canceled - enemy already at 1 HP");
                    }
                    // Otherwise limit thorn damage to prevent killing
                    else if (enemy.getCurrentHP() <= thornDamageAmount) {
                        thornDamageAmount = Math.max(1, enemy.getCurrentHP() - 1);
                        GameLogger.logInfo("Thorn damage limited to prevent enemy death: " + thornDamageAmount);
                    }

                    // Only apply thorn damage and show numbers if there's actual damage to deal
                    if (thornDamageAmount > 0) {
                        enemy.damage(thornDamageAmount, false);
                        // Create thorn damage number
                        combatScene.createThornDamageNumber(thornDamageAmount);

                        // Make the HP bar visible by updating and showing enemy HP
                        combatScene.updateAndShowEnemyHP();
                    }
                }
            }
        }

        GameLogger.logInfo(String.format(
            "Player took %d damage (reduced from %d by defense %d = %d base + %d bonus), HP: %d/%d",
            actualDamage, damage, totalDefense, baseDefense, defenseBonus, currentHP, getMaxHP()));
    }

    public void heal(int amount) {
        currentHP = Math.min(getMaxHP(), currentHP + Math.abs(amount));
        GameLogger.logInfo(String.format("Player healed %d HP, now at: %d/%d", amount, currentHP, getMaxHP()));
    }

    // Save/Load Methods
    public void saveToFile() {
        try {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            String jsonStr = json.prettyPrint(this);
            FileHandle file = Gdx.files.local(SAVE_FILE);
            file.writeString(jsonStr, false);
            GameLogger.logInfo("Player data saved successfully");
        } catch (Exception e) {
            GameLogger.logError("Failed to save player data", e);
        }
    }

    public static Player loadFromFile() {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            if (!file.exists()) {
                GameLogger.logInfo("No save file found, creating new player");
                return new Player("CHARA");
            }

            Json json = new Json();
            // Register ItemDatabase to ensure it's initialized before deserialization
            swu.cp112.silkblade.entity.item.ItemDatabase.getInstance();

            Player player = json.fromJson(Player.class, file.readString());
            player.initializeSprite();  // Re-initialize sprite after loading
            player.initializeSounds();  // Re-initialize sounds after loading
            return player;
        } catch (Exception e) {
            GameLogger.logError("Failed to load player data", e);
            return new Player("CHARA");
        }
    }

    // Json.Serializable Implementation
    @Override
    public void write(Json json) {
        json.writeValue("name", name);
        json.writeValue("level", level);
        json.writeValue("exp", exp);
        json.writeValue("mp", mp);
        json.writeValue("maxMP", maxMP);
        json.writeValue("attack", attack);
        json.writeValue("critRate", critRate);
        json.writeValue("defense", defense);
        json.writeValue("maxHP", maxHP);
        json.writeValue("currentHP", currentHP);
        json.writeValue("currentStage", currentStage);
        json.writeValue("gold", gold);
        json.writeValue("unlockedSkills", unlockedSkills);
        json.writeValue("currentSkill", currentSkill);
        json.writeValue("inventory", inventory);
        // Boss tracking
        json.writeValue("boss1Defeated", boss1Defeated);
        json.writeValue("boss2Defeated", boss2Defeated);
        json.writeValue("boss3Defeated", boss3Defeated);
        json.writeValue("boss4Defeated", boss4Defeated);
        json.writeValue("boss5Defeated", boss5Defeated);

        // We don't serialize the BuffManager since it's transient and contains
        // only temporary effects that are reset when the game loads
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        name = jsonData.getString("name", "CHARA");
        level = jsonData.getInt("level", 1);
        exp = jsonData.getInt("exp", 0);
        mp = jsonData.getInt("mp", 20);
        maxMP = jsonData.getInt("maxMP", 20);
        attack = jsonData.getInt("attack", 12);
        critRate = jsonData.getFloat("critRate", 0.1f);
        defense = jsonData.getInt("defense", 10);
        maxHP = jsonData.getInt("maxHP", 20);
        currentHP = jsonData.getInt("currentHP", maxHP);
        currentStage = jsonData.getInt("currentStage", 1);
        gold = jsonData.getInt("gold", 0);

        // Read skill unlocks
        JsonValue unlockedSkillsValue = jsonData.get("unlockedSkills");
        if (unlockedSkillsValue != null) {
            unlockedSkills = json.readValue(boolean[].class, unlockedSkillsValue);
        } else {
            initializeSkills(); // Create default skills if not in save
        }

        // Read current skill
        JsonValue currentSkillValue = jsonData.get("currentSkill");
        if (currentSkillValue != null) {
            currentSkill = json.readValue(SkillType.class, currentSkillValue);
        } else {
            currentSkill = SkillType.BASIC; // Default to basic attack
        }

        // Read boss tracking
        boss1Defeated = jsonData.getBoolean("boss1Defeated", false);
        boss2Defeated = jsonData.getBoolean("boss2Defeated", false);
        boss3Defeated = jsonData.getBoolean("boss3Defeated", false);
        boss4Defeated = jsonData.getBoolean("boss4Defeated", false);
        boss5Defeated = jsonData.getBoolean("boss5Defeated", false);

        // Read inventory
        JsonValue inventoryValue = jsonData.get("inventory");
        if (inventoryValue != null) {
            inventory = json.readValue(Inventory.class, inventoryValue);
        } else {
            inventory = new Inventory();
        }

        // Initialize non-serialized components
        initializeSprite();
        initializeSounds();
        this.buffManager = new BuffManager(this); // Create a fresh BuffManager
    }

    // Getters and Setters
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getMP() { return mp; }
    public int getAttack() {
        ensureBuffManagerExists();
        int baseAttack = attack;
        // Apply percentage bonus first to base stat
        float percentBonus = inventory.getTotalAttackPercentBonus();
        int percentBonusAmount = (int)(baseAttack * percentBonus);
        // Add flat bonuses and buffs
        return baseAttack + percentBonusAmount + inventory.getTotalAttackBonus() + buffManager.getAttackBuff();
    }
    public float getCritRate() {
        ensureBuffManagerExists();
        return critRate + inventory.getTotalCritRateBonus() + buffManager.getCritRateBuff();
    }
    public int getDefense() {
        ensureBuffManagerExists();
        int baseDefense = defense;
        // Apply percentage bonus first to base stat
        float percentBonus = inventory.getTotalDefensePercentBonus();
        int percentBonusAmount = (int)(baseDefense * percentBonus);
        // Add flat bonuses and buffs
        return baseDefense + percentBonusAmount + inventory.getTotalDefenseBonus() + buffManager.getDefenseBuff();
    }
    public int getMaxMP() {
        int baseMaxMP = maxMP;
        // Apply percentage bonus first to base stat
        float percentBonus = inventory.getTotalMaxMPPercentBonus();
        int percentBonusAmount = (int)(baseMaxMP * percentBonus);
        // Add flat bonuses
        return baseMaxMP + percentBonusAmount + inventory.getTotalMaxMPBonus();
    }
    public int getMaxHP() {
        int baseMaxHP = maxHP;
        // Apply percentage bonus first to base stat
        float percentBonus = inventory.getTotalMaxHPPercentBonus();
        int percentBonusAmount = (int)(baseMaxHP * percentBonus);
        // Add flat bonuses
        return baseMaxHP + percentBonusAmount + inventory.getTotalMaxHPBonus();
    }
    public int getCurrentHP() { return currentHP; }
    public Sprite getSprite() { return sprite; }

    public void setName(String name) { this.name = name; }
    public void setMP(int mp) {
        this.mp = Math.min(mp, getMaxMP());  // Use getMaxMP() instead of maxMP to account for bonuses
    }
    public void setMaxMP(int maxMP) {
        this.maxMP = maxMP;
        this.mp = Math.min(this.mp, getMaxMP());  // Use getMaxMP() instead of this.maxMP
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
        // Dispose of all sounds
        if (basicAttackSound != null) basicAttackSound.dispose();
        if (skill1Sound != null) skill1Sound.dispose();
        if (skill2Sound != null) skill2Sound.dispose();
        if (skill3Sound != null) skill3Sound.dispose();
        if (skill4Sound != null) skill4Sound.dispose();
        if (skill5Sound != null) skill5Sound.dispose();
        if (skill6Sound != null) skill6Sound.dispose();
    }

    // Helper method to get exp needed for next level
    public int getExpToNextLevel() {
        // If at max level, return -1 or current exp to indicate no more leveling
        if (level >= MAX_LEVEL) {
            return -1;
        }

        // Get exp required for next level
        return EXP_REQUIREMENTS[Math.min(level, EXP_REQUIREMENTS.length - 1)];
    }

    // Status checks
    public boolean isDead() {
        return currentHP <= 0;
    }


    public String getStats() {
        int attackBonus = inventory.getTotalAttackBonus();
        int defenseBonus = inventory.getTotalDefenseBonus();
        int maxHPBonus = inventory.getTotalMaxHPBonus();

        return String.format(
            "LV %d\nHP: %d/%d (%s%d)\nAT: %d (%s%d)\nDF: %d (%s%d)\nEXP: %d\nNext: %d",
            level,
            currentHP, getMaxHP(),
            maxHPBonus >= 0 ? "+" : "", maxHPBonus,
            getAttack(),
            attackBonus >= 0 ? "+" : "", attackBonus,
            getDefense(),
            defenseBonus >= 0 ? "+" : "", defenseBonus,
            exp,
            getExpToNextLevel() == -1 ? 0 : getExpToNextLevel()
        );
    }

    // Add method to check if max level reached
    public boolean isMaxLevel() {
        return level >= MAX_LEVEL;
    }

    // Add method to get exp progress percentage
    public float getExpProgress() {
        if (isMaxLevel()) {
            return 1.0f;
        }

        int currentLevelExp = EXP_REQUIREMENTS[Math.max(0, level - 1)];
        int nextLevelExp = getExpToNextLevel();
        return (float)(exp - currentLevelExp) / (nextLevelExp - currentLevelExp);
    }

    // Add utility methods for MP management
    public void increaseMP(int amount) {
        this.mp = Math.min(this.mp + amount, getMaxMP());  // Use getMaxMP() to account for bonuses
    }

    public void decreaseMP(int amount) {
        this.mp = Math.max(this.mp - amount, 0);
    }

    // Add these new methods before the dispose() method
    public boolean isSkillUnlocked(SkillType skill) {
        return unlockedSkills[skill.ordinal()];
    }

    public void unlockSkill(SkillType skill) {
        if (skill != SkillType.BASIC) {  // Can't "unlock" BASIC as it's always available
            unlockedSkills[skill.ordinal()] = true;
            GameLogger.logInfo("Unlocked skill: " + skill.name());
            saveToFile();  // Save progress when unlocking new skill
        }
    }

    public SkillType getCurrentSkill() {
        return currentSkill;
    }

    public void setCurrentSkill(SkillType skill) {
        if (isSkillUnlocked(skill)) {
            this.currentSkill = skill;
        } else {
            GameLogger.logInfo("Attempted to use locked skill: " + skill.name());
        }
    }

    public boolean hasUltimateSkill() {
        return isSkillUnlocked(SkillType.SKILL6);
    }

    public boolean hasEnoughMPForSkill(SkillType skill) {
        if (skill == null) {
            return true;
        }

        // If free skill cast is available, any skill can be cast
        if (freeSkillCastAvailable) {
            return true;
        }

        int mpCost = calculateMPCost(skill);

        if (skill == SkillType.SKILL6) {
            // Ultimate skill requires atleast 50% MP
            return mp >= maxMP * 0.5f;
        }

        return mp >= mpCost;
    }

    public void consumeMPForSkill(SkillType skill) {
        mpBeforeConsume = mp;

        // If free skill cast is available, use it instead of consuming MP
        if (freeSkillCastAvailable) {
            useFreeSkillCast();
            return;
        }

        int mpCost = calculateMPCost(skill);
        decreaseMP(mpCost);
    }

    private int calculateMPCost(SkillType skill) {
        if (skill == SkillType.SKILL6) {
            // Ultimate attack uses ALL current MP
            return mp;
        } else {
            // Other skills use fixed MP costs
            return SKILL_MP_COSTS[skill.ordinal()];
        }
    }

    // And add this helper method to get the display name
    public String getCurrentSkillDisplayName() {
        return currentSkill.getDisplayName();
    }

    /**
     * Get the display name for a specific skill
     * @param skill The skill to get the display name for
     * @return The display name of the skill
     */
    public String getSkillDisplayName(SkillType skill) {
        return skill.getDisplayName();
    }

    /**
     * Get the MP cost for a specific skill
     * @param skill The skill to get the MP cost for
     * @return The MP cost of the skill
     */
    public int getSkillMPCost(SkillType skill) {
        return calculateMPCost(skill);
    }

    public void regenMP() {
        float regenAmount = BASE_MP_REGEN + (level - 1) * MP_REGEN_LEVEL_SCALING;
        mp = (int) Math.min(mp + regenAmount, getMaxMP());
    }

    // Getters and setters for new fields
    public int getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(int currentStage) {
        this.currentStage = currentStage;

        // Check if this stage change should unlock any skills
        checkStageUnlocks();

        // Save player data after updating stage
        saveToFile();
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    // Add or update these getters to include equipment bonuses
    public int getBaseAttack() { return attack; }
    public int getBaseDefense() { return defense; }
    public int getBaseMaxHP() { return maxHP; }
    public int getBaseMaxMP() { return maxMP; }
    public float getBaseCritRate() { return critRate; }

    public Inventory getInventory() {
        return inventory;
    }

    // Boss tracking methods
    public boolean isBoss1Defeated() {
        return boss1Defeated;
    }

    public boolean isBoss2Defeated() {
        return boss2Defeated;
    }

    public boolean isBoss3Defeated() {
        return boss3Defeated;
    }

    public boolean isBoss4Defeated() {
        return boss4Defeated;
    }

    public boolean isBoss5Defeated() {
        return boss5Defeated;
    }

    public void setBoss1Defeated(boolean defeated) {
        this.boss1Defeated = defeated;
    }

    public void setBoss2Defeated(boolean defeated) {
        this.boss2Defeated = defeated;
    }

    public void setBoss3Defeated(boolean defeated) {
        this.boss3Defeated = defeated;
    }

    public void setBoss4Defeated(boolean defeated) {
        this.boss4Defeated = defeated;
    }

    public void setBoss5Defeated(boolean defeated) {
        this.boss5Defeated = defeated;
    }

    // Helper method to set boss defeated by boss number
    public void setBossDefeated(int bossNumber, boolean defeated) {
        switch (bossNumber) {
            case 1:
                setBoss1Defeated(defeated);
                break;
            case 2:
                setBoss2Defeated(defeated);
                break;
            case 3:
                setBoss3Defeated(defeated);
                break;
            case 4:
                setBoss4Defeated(defeated);
                break;
            case 5:
                setBoss5Defeated(defeated);
                break;
            default:
                GameLogger.logError("Invalid boss number: " + bossNumber, null);
        }

        // Check if this boss defeat unlocks any skills
        checkBossDefeatSkillUnlocks(bossNumber);
    }

    /**
     * Check if defeating a specific boss unlocks any skills
     * @param bossNumber The boss number that was defeated
     */
    private void checkBossDefeatSkillUnlocks(int bossNumber) {
        // Boss on 30th floor unlocks Silk Scratch of Salvation (Skill 2)
        if (bossNumber == 3 && !isSkillUnlocked(SkillType.SKILL2)) {
            unlockSkill(SkillType.SKILL2);
            GameLogger.logInfo("Silk Scratch of Salvation unlocked after defeating boss on 30th floor!");
        }

        // Boss on 20th floor unlocks Sai-Oua Silk Wrap (Skill 4)
        if (bossNumber == 2 && !isSkillUnlocked(SkillType.SKILL4)) {
            unlockSkill(SkillType.SKILL4);
            GameLogger.logInfo("Sai-Oua Silk Wrap unlocked after defeating boss on 20th floor!");
        }

        // Boss on 40th floor unlocks Lamphun Blade: Piip Slash Supreme (Skill 5)
        if (bossNumber == 4 && !isSkillUnlocked(SkillType.SKILL5)) {
            unlockSkill(SkillType.SKILL5);
            GameLogger.logInfo("Lamphun Blade: Piip Slash Supreme unlocked after defeating boss on 40th floor!");
        }

        // Boss on 50th floor unlocks Silk End - I Am Cosmic Weave (Skill 6)
        if (bossNumber == 5 && !isSkillUnlocked(SkillType.SKILL6)) {
            unlockSkill(SkillType.SKILL6);
            GameLogger.logInfo("Silk End - I Am Cosmic Weave unlocked after defeating boss on 50th floor!");
        }
    }

    /**
     * Update buffs between turns
     */
    public boolean updateBuffs() {
        if (buffManager != null) {
            return buffManager.updateBuffs();
        }
        return false;
    }

    /**
     * Checks if the player has Death Defiance from their equipped accessory
     * @return true if the player has Death Defiance
     */
    public boolean hasDeathDefiance() {
        return inventory.hasDeathDefiance();
    }

    /**
     * Add a temporary stat buff to the player
     */
    public void addStatBuff(BuffManager.StatType statType, int amount, int duration) {
        if (buffManager != null) {
            buffManager.addBuff(statType, amount, duration);
        }
    }

    /**
     * Clear all temporary buffs
     */
    public void clearBuffs() {
        if (buffManager != null) {
            buffManager.clearBuffs();
        }
    }

    /**
     * Initialize the buff manager if it doesn't exist
     */
    public void ensureBuffManagerExists() {
        if (buffManager == null) {
            buffManager = new BuffManager(this);
        }
    }

    public void fullHeal() {
        currentHP = getMaxHP();
        GameLogger.logInfo("Player fully healed: " + currentHP + "/" + getMaxHP());
    }

    public void fullRestore() {
        fullHeal();
        mp = getMaxMP();
        GameLogger.logInfo("Player fully restored HP and MP: " + currentHP + "/" + getMaxHP() + ", " + mp + "/" + getMaxMP());
    }

    /**
     * Creates a deep snapshot of the player's current state that can be used for restoration
     * @return A new Player object containing a copy of the player's current state
     */
    public Player createSnapshot() {
        try {
            // Create a new player
            Player snapshot = new Player();

            // Copy basic stats
            snapshot.name = this.name;
            snapshot.level = this.level;
            snapshot.exp = this.exp;
            snapshot.attack = this.attack;
            snapshot.defense = this.defense;
            snapshot.maxHP = this.maxHP;
            snapshot.currentHP = this.currentHP;
            snapshot.maxMP = this.maxMP;
            snapshot.mp = this.mp;
            snapshot.critRate = this.critRate;
            snapshot.currentStage = this.currentStage;
            snapshot.gold = this.gold;

            // Copy boss defeat status
            snapshot.boss1Defeated = this.boss1Defeated;
            snapshot.boss2Defeated = this.boss2Defeated;
            snapshot.boss3Defeated = this.boss3Defeated;
            snapshot.boss4Defeated = this.boss4Defeated;
            snapshot.boss5Defeated = this.boss5Defeated;

            // Copy skill data
            snapshot.unlockedSkills = new boolean[this.unlockedSkills.length];
            System.arraycopy(this.unlockedSkills, 0, snapshot.unlockedSkills, 0, this.unlockedSkills.length);
            snapshot.currentSkill = this.currentSkill;

            // Create a copy of the inventory (serialize and deserialize for deep copy)
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            String inventoryJson = json.toJson(this.inventory);
            snapshot.inventory = json.fromJson(Inventory.class, inventoryJson);

            // Initialize transient fields
            snapshot.initializeSprite();
            snapshot.initializeSounds();

            // Initialize and copy buffs if they exist
            snapshot.ensureBuffManagerExists();
            if (this.buffManager != null) {
                for (BuffManager.StatBuff buff : this.buffManager.getActiveBuffs()) {
                    snapshot.buffManager.addBuff(
                        buff.getType(),
                        buff.getAmount(),
                        buff.getRemainingTurns()
                    );
                }
            }

            return snapshot;
        } catch (Exception e) {
            GameLogger.logError("Failed to create player snapshot", e);
            return null;
        }
    }

    /**
     * Restores player state from a snapshot
     * @param snapshot The player snapshot to restore from
     */
    public void restoreFromSnapshot(Player snapshot) {
        if (snapshot == null) {
            GameLogger.logError("Cannot restore from null snapshot", null);
            return;
        }

        try {
            // Restore basic stats
            this.name = snapshot.name;
            this.level = snapshot.level;
            this.exp = snapshot.exp;
            this.attack = snapshot.attack;
            this.defense = snapshot.defense;
            this.maxHP = snapshot.maxHP;
            this.currentHP = snapshot.currentHP;
            this.maxMP = snapshot.maxMP;
            this.mp = snapshot.mp;
            this.critRate = snapshot.critRate;
            this.currentStage = snapshot.currentStage;
            this.gold = snapshot.gold;

            // Restore boss defeat status
            this.boss1Defeated = snapshot.boss1Defeated;
            this.boss2Defeated = snapshot.boss2Defeated;
            this.boss3Defeated = snapshot.boss3Defeated;
            this.boss4Defeated = snapshot.boss4Defeated;
            this.boss5Defeated = snapshot.boss5Defeated;

            // Restore skill data
            this.unlockedSkills = new boolean[snapshot.unlockedSkills.length];
            System.arraycopy(snapshot.unlockedSkills, 0, this.unlockedSkills, 0, snapshot.unlockedSkills.length);
            this.currentSkill = snapshot.currentSkill;

            // Restore inventory (serialize and deserialize for deep copy)
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            String inventoryJson = json.toJson(snapshot.inventory);
            this.inventory = json.fromJson(Inventory.class, inventoryJson);

            // Clear and restore buffs
            this.ensureBuffManagerExists();
            this.buffManager.clearBuffs();
            for (BuffManager.StatBuff buff : snapshot.buffManager.getActiveBuffs()) {
                this.buffManager.addBuff(
                    buff.getType(),
                    buff.getAmount(),
                    buff.getRemainingTurns()
                );
            }

//            GameLogger.logInfo("Player state restored from snapshot");
        } catch (Exception e) {
            GameLogger.logError("Failed to restore player from snapshot", e);
        }
    }

    /**
     * Checks if the player will perform a double attack with the given skill.
     * This depends on whether the player has a weapon with double attack capability.
     * Only applies to basic attacks, not skills.
     *
     * @param skill The skill being used
     * @return true if this will be a double attack
     */
    public boolean willPerformDoubleAttack(SkillType skill) {
        // Only basic attacks can have double attack
        if (skill != SkillType.BASIC) {
            return false;
        }

        // Check if the player's weapon has double attack capability
        return inventory.hasDoubleAttack();
    }

    /**
     * Remove a specific stat buff with a given amount
     * @param statType The type of stat buff to remove
     * @param amount The amount to match and remove
     */
    public void removeStatBuff(BuffManager.StatType statType, int amount) {
        if (buffManager != null) {
            // We'll need to check each buff and find the one matching this type and amount
            Array<BuffManager.StatBuff> activeBuffs = buffManager.getActiveBuffs();
            BuffManager.StatBuff buffToRemove = null;

            // Look for any buff that matches this type and amount
            for (BuffManager.StatBuff buff : activeBuffs) {
                if (buff.getType() == statType && buff.getAmount() == amount) {
                    buffToRemove = buff;
                    break;
                }
            }

            // If we found a matching buff, remove it
            if (buffToRemove != null) {
                buffManager.removeBuff(buffToRemove);
                GameLogger.logInfo("Removed " + statType + " buff of " + amount);
            }
        }
    }

    // Add a new method to handle special skill effects
    public void applySkillEffects(SkillType skill) {
        switch (skill) {
            case SKILL2:
                // Silk Scratch of Salvation - Heal 20% of Max HP
                int healAmount = (int)(getMaxHP() * 0.2);
                GameLogger.logInfo("Skill 2 healing: " + healAmount + " HP (20% of max HP: " + getMaxHP() +
                                  ", base max HP: " + maxHP + ")");
                heal(healAmount);
                break;
            case SKILL3:
                // Duckfoot Knot from Heaven - Increase defense by 50 for 2 turns
                addStatBuff(BuffManager.StatType.DEFENSE, 50, 2);
                break;
            case SKILL4:
                // Sai-Oua Silk Wrap - Increase defense by 100 and heal 35% of Max HP for 1 turn
                addStatBuff(BuffManager.StatType.DEFENSE, 100, 1);
                int healAmount2 = (int)(getMaxHP() * 0.35);
                GameLogger.logInfo("Skill 4 healing: " + healAmount2 + " HP (35% of max HP: " + getMaxHP() +
                                  ", base max HP: " + maxHP + ")");
                heal(healAmount2);
                break;
            default:
                // No additional effects for other skills
                break;
        }
    }

    // Modify this method to check for unlocking skills based on the new requirements
    public void checkSkillUnlocks() {
        // No level-based unlocks in the new spec except Duckfoot Knot from Heaven at level 12
        if (level >= 12 && !isSkillUnlocked(SkillType.SKILL3)) {
            unlockSkill(SkillType.SKILL3);
            GameLogger.logInfo("Duckfoot Knot from Heaven unlocked!");
        }
    }

    // Helper method to check if a boss is defeated by boss number
    public boolean isBossDefeated(int bossNumber) {
        switch (bossNumber) {
            case 1:
                return isBoss1Defeated();
            case 2:
                return isBoss2Defeated();
            case 3:
                return isBoss3Defeated();
            case 4:
                return isBoss4Defeated();
            case 5:
                return isBoss5Defeated();
            default:
                GameLogger.logError("Invalid boss number: " + bossNumber, null);
                return false;
        }
    }

    /**
     * Checks if the current stage unlocks any skills
     */
    private void checkStageUnlocks() {
        // Dark Silk Face Slap unlocks at stage 10
        if (currentStage >= 10 && !isSkillUnlocked(SkillType.SKILL1)) {
            unlockSkill(SkillType.SKILL1);
            GameLogger.logInfo("Dark Silk Face Slap unlocked after reaching stage 10!");
        }
    }

    // Add these new methods for free skill cast functionality
    public boolean hasFreeSkillCastAvailable() {
        return freeSkillCastAvailable;
    }

    public void resetFreeSkillCast() {
        // Called at the start of a battle to reset the availability
        freeSkillCastAvailable = inventory.hasFreeSkillCast();
    }

    public void useFreeSkillCast() {
        freeSkillCastAvailable = false;
    }
}
