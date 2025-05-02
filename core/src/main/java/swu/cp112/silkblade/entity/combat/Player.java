package swu.cp112.silkblade.entity.combat;

import java.io.FileNotFoundException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.entity.item.Equipment;
import swu.cp112.silkblade.entity.item.Inventory;
import swu.cp112.silkblade.util.GameLogger;

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
        5,    // SKILL1: 5 MP
        8,    // SKILL2: 8 MP
        12,   // SKILL3: 12 MP
        15,   // SKILL4: 15 MP
        20,   // SKILL5: 20 MP
        -1    // SKILL6: special case - uses all MP
    };

    private static final float BASE_MP_REGEN = 1; // Base MP regen per turn
    private static final float MP_REGEN_LEVEL_SCALING = 2.5f; // Additional MP regen per level

    public enum SkillType {
        BASIC("Slash"),  // Always available
        SKILL1("Quick Strike"),
        SKILL2("Power Slash"),
        SKILL3("Blade Dance"),
        SKILL4("Shadow Strike"),
        SKILL5("Dragon Fang"),
        SKILL6("Ultimate Technique");

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
    }

    private void initializeSprite() {
        try {
            texture = new Texture("player.png");
            sprite = new Sprite(texture);
            GameLogger.logInfo("Player sprite initialized");
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

            GameLogger.logInfo("Player sounds initialized");
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
        unlockedSkills[SkillType.SKILL1.ordinal()] = false;  // Unlock Skill for debug
        unlockedSkills[SkillType.SKILL2.ordinal()] = false;  // Unlock Skill for debug
        unlockedSkills[SkillType.SKILL3.ordinal()] = false;  // Unlock Skill for debug
        unlockedSkills[SkillType.SKILL4.ordinal()] = false;  // Unlock Skill for debug
        unlockedSkills[SkillType.SKILL5.ordinal()] = false;  // Unlock Skill for debug
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
            // HP increases more significantly at higher levels
            maxHP += 4 + (level * 3);  // Changed from (level * 2)
            currentHP = maxHP;  // Full heal on level up

            // Attack increases more with level
            attack += 2 + (int)(level * 1.5f);  // Changed from (2 + level)

            // MP scales better with level
            maxMP += 5 + (int)(level * 1.2f);  // Changed from (4 + level)
            setMP(getMaxMP());

            // Defense scales better but not too quickly
            defense += 1 + (int)(level * 0.7f);  // Changed from (1 + (level / 4))

            // Crit rate increases more significantly
            critRate = Math.min(0.1f + (level * 0.008f), 0.35f);  // Changed max from 0.25f to 0.35f

            // Unlock SKILL6 at max level
            if (level == MAX_LEVEL) {
                unlockSkill(SkillType.SKILL6);
                GameLogger.logInfo("Maximum level reached! SKILL6 unlocked!");
            }
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

        public DamageResult(int damage, boolean isCritical) {
            this.damage = damage;
            this.isCritical = isCritical;
        }
    }

    public DamageResult calculateDamage() {
        float critRoll = (float) Math.random();
        boolean isCrit = critRoll <= getCritRate();  // Use getCritRate() to include bonuses
        int totalAttack = getAttack();  // Use getAttack() to include equipment bonuses
        int baseDamage = totalAttack + (int)(Math.random() * 3) - 1; // -1 to +1 variance
        int finalDamage = isCrit ? baseDamage * 3 : baseDamage;
        return new DamageResult(finalDamage, isCrit);
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
                damageMultiplier = 1.4f;  // Increased from 1.3f
                break;
            case SKILL2:
                damageMultiplier = 1.8f;  // Increased from 1.6f
                break;
            case SKILL3:
                damageMultiplier = 2.3f;  // Increased from 2.0f
                break;
            case SKILL4:
                damageMultiplier = 2.8f;  // Increased from 2.5f
                break;
            case SKILL5:
                damageMultiplier = 3.5f;  // Increased from 3.0f
                break;
            case SKILL6:
                // Improved ultimate scaling
                float mpRatio = (float)mpBeforeConsume / maxMP;
                damageMultiplier = 3.0f + (mpRatio * 4.0f);  // Changed from 2.0f + (mpRatio * 3.0f)
                break;
            default:
                damageMultiplier = 1.0f;
        }

        DamageResult baseResult = calculateDamage();
        int modifiedDamage = (int)(baseResult.damage * damageMultiplier);
        return new DamageResult(modifiedDamage, baseResult.isCritical);
    }

    public String getSkillMessage(SkillType skill, int damage, Enemy currentEnemy) {
        switch (skill) {
            case BASIC:
                return "You dealt " + damage + " damage to " + currentEnemy.getName() + "!";
            case SKILL1:
                return "Your Quick Strike dealt " + damage + " damage to " + currentEnemy.getName() + "!";
            case SKILL2:
                return "Your Power Slash dealt " + damage + " damage to " + currentEnemy.getName() + "!";
            case SKILL3:
                return "Your Blade Dance dealt " + damage + " damage to " + currentEnemy.getName() + "!";
            case SKILL4:
                return "Your Shadow Strike dealt " + damage + " damage to " + currentEnemy.getName() + "!";
            case SKILL5:
                return "Your Dragon Fang dealt " + damage + " damage to " + currentEnemy.getName() + "!";
            case SKILL6:
                return "Your Ultimate Technique dealt " + damage + " damage to " + currentEnemy.getName() + "!";
            default:
                return "You dealt " + damage + " damage to " + currentEnemy.getName() + "!";
        }
    }

    public String useSkillMessage(SkillType skill) {
        int mpCost = calculateMPCost(skill);

        switch (skill) {
            case BASIC:
                return "You used Slash!";
            case SKILL1:
                return "You cast Quick Slash using " + mpCost + " MP!";
            case SKILL2:
                return "You cast Power Slash using " + mpCost + " MP!";
            case SKILL3:
                return "You cast Blade Dance using " + mpCost + " MP!";
            case SKILL4:
                return "You cast Shadow Strike using " + mpCost + " MP!";
            case SKILL5:
                return "You cast Dragon Fang using " + mpCost + " MP!";
            case SKILL6:
                return "You cast Ultimate Technique using all your MP (" + mpCost + " MP)!";
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

    public void takeDamage(int damage) {
        int totalDefense = getDefense(); // This includes base defense + equipment bonuses
        int baseDefense = defense; // Base defense without equipment
        int defenseBonus = totalDefense - baseDefense; // Equipment bonus portion

        int actualDamage = Math.max(1, damage - (totalDefense / 2));
        currentHP = Math.max(0, currentHP - actualDamage);

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
            GameLogger.logInfo("Player data loaded successfully");
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
        json.writeValue("unlockedSkills", unlockedSkills);
        json.writeValue("currentSkill", currentSkill.name());
        json.writeValue("currentStage", currentStage);
        json.writeValue("gold", gold);
        json.writeValue("inventory", inventory);
        // Save boss tracking
        json.writeValue("boss1Defeated", boss1Defeated);
        json.writeValue("boss2Defeated", boss2Defeated);
        json.writeValue("boss3Defeated", boss3Defeated);
        json.writeValue("boss4Defeated", boss4Defeated);
        json.writeValue("boss5Defeated", boss5Defeated);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        name = jsonData.getString("name");
        level = jsonData.getInt("level");
        exp = jsonData.getInt("exp");
        mp = jsonData.getInt("mp");
        maxMP = jsonData.getInt("maxMP");
        attack = jsonData.getInt("attack");
        critRate = jsonData.getFloat("critRate");
        defense = jsonData.getInt("defense");
        maxHP = jsonData.getInt("maxHP");
        currentHP = jsonData.getInt("currentHP");

        // Handle skill data
        unlockedSkills = json.readValue("unlockedSkills", boolean[].class, jsonData);
        if (unlockedSkills == null || unlockedSkills.length != SkillType.values().length) {
            // Initialize if missing or invalid
            initializeSkills();
        } else {
            // Ensure BASIC is always unlocked
            unlockedSkills[SkillType.BASIC.ordinal()] = true;
        }

        // Read current skill, default to BASIC if not found
        String skillName = jsonData.getString("currentSkill", "BASIC");
        try {
            currentSkill = SkillType.valueOf(skillName);
            if (!isSkillUnlocked(currentSkill)) {
                currentSkill = SkillType.BASIC;
            }
        } catch (IllegalArgumentException e) {
            currentSkill = SkillType.BASIC;
        }

        // Read the new fields with default values if not found
        currentStage = jsonData.getInt("currentStage", 1);
        gold = jsonData.getInt("gold", 0);

        // Read boss tracking status with default values if not found
        boss1Defeated = jsonData.getBoolean("boss1Defeated", false);
        boss2Defeated = jsonData.getBoolean("boss2Defeated", false);
        boss3Defeated = jsonData.getBoolean("boss3Defeated", false);
        boss4Defeated = jsonData.getBoolean("boss4Defeated", false);
        boss5Defeated = jsonData.getBoolean("boss5Defeated", false);

        // Read inventory or create a new one if not found
        JsonValue inventoryValue = jsonData.get("inventory");
        if (inventoryValue != null) {
            inventory = json.readValue(Inventory.class, inventoryValue);
        } else {
            inventory = new Inventory();
        }

        // Initialize non-serialized components
        initializeSprite();
        initializeSounds();
    }

    // Getters and Setters
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getMP() { return mp; }
    public int getAttack() {
        return attack + inventory.getTotalAttackBonus();
    }
    public float getCritRate() {
        return critRate + inventory.getTotalCritRateBonus();
    }
    public int getDefense() {
        return defense + inventory.getTotalDefenseBonus();
    }
    public int getMaxMP() {
        return maxMP + inventory.getTotalMaxMPBonus();
    }
    public int getMaxHP() {
        return maxHP + inventory.getTotalMaxHPBonus();
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
        if (skill == SkillType.SKILL6) {
            // Ultimate skill requires more than 50% of max MP
            return mp > (maxMP / 2);
        } else {
            // Other skills use normal MP cost check
            int mpCost = calculateMPCost(skill);
            return mp >= mpCost;
        }
    }

    public void consumeMPForSkill(SkillType skill) {
        mpBeforeConsume = mp;
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
}
