# SilkBlade Game - Technical Documentation

## Table of Contents
1. [Introduction](#introduction)
2. [Architecture Overview](#architecture-overview)
3. [Core Systems](#core-systems)
   - [Main Game Class](#main-game-class)
   - [Screen System](#screen-system)
4. [Entity System](#entity-system)
   - [Player](#player)
   - [Enemy System](#enemy-system)
5. [Attack Pattern System](#attack-pattern-system)
6. [Combat System](#combat-system)
7. [Player Progression](#player-progression)
   - [Experience and Leveling](#experience-and-leveling)
   - [Stats System](#stats-system)
8. [Skill System](#skill-system)
   - [Skill Types](#skill-types)
   - [Skill Unlocking](#skill-unlocking)
   - [Skill Usage](#skill-usage)
9. [Item System](#item-system)
   - [ItemDatabase](#itemdatabase)
   - [Inventory System](#inventory-system)
   - [Item Types](#item-types)
10. [Screen Management](#screen-management)
11. [Save System](#save-system)

## Introduction
SilkBlade is a turn-based, bullet-hell combat game built with LibGDX. The game features:
- A rich combat system with bullet patterns and player movement
- Multiple enemy types with unique attack patterns
- Item and skill systems
- Level progression and character development
- Save/load functionality

## Architecture Overview
SilkBlade follows an object-oriented architecture with a clear separation of concerns. The game is built on the [LibGDX](https://libgdx.com/) framework, utilizing its game loop, rendering, and asset management capabilities.

Key architectural components:
- **Main Game Class**: Entry point that handles initialization and core game systems
- **Screen Management**: Different game screens for menus, combat, inventory, etc.
- **Entity System**: Hierarchical class structure for game objects (players, enemies, items)
- **Pattern System**: Modular attack patterns for enemies
- **Combat System**: Handles bullet spawning, collision detection, and damage calculation

## Core Systems

### Main Game Class
The `Main` class serves as the entry point for the game and manages core systems:

```java
public class Main extends Game {
    // Core components
    private static OrthographicCamera camera;
    private static FitViewport viewport;
    private static Music backgroundMusic;
    
    // Lifecycle methods
    @Override
    public void create() {
        // Initialize systems
        initializeGraphics();
        initializeItemDatabase();
        initializeAudio();
        // Set initial screen
        setScreen(new MainMenuScreen(this));
    }
}
```

Key responsibilities:
- Graphics initialization (camera, viewport)
- Audio management
- Item database initialization
- Screen management
- Resource management

### Screen System
The game uses LibGDX's Screen interface to manage different game screens:
- `MainMenuScreen`: The initial screen showing play, options, and quit buttons
- `CharacterCreationScreen`: For creating new player characters
- `SaveFileSelectionScreen`: For loading saved games
- `StageSelectionScreen`: For selecting combat stages
- `CombatScene`: The core gameplay screen for battle encounters
- `InventoryScreen`: For managing player items
- `ShopScreen`: For purchasing items
- `OptionsScreen`: For game settings

## Entity System

### Player
The Player class represents the user-controlled character, handling:
- Movement and positioning
- Stats (HP, MP, attack, defense)
- Experience and leveling
- Inventory and equipment management
- Skill system
- Save/load functionality

### Enemy System
Enemies follow a class hierarchy:

#### Enemy Interface
The base interface that defines the contract for all enemy entities:

```java
public interface Enemy {
    // Basic properties
    String getName();
    
    // Health management
    int getMaxHP();
    int getCurrentHP();
    void setHP(int hp);
    void damage(int amount, boolean isCritical);
    boolean isDefeated();
    
    // Visual properties
    Texture getTexture();
    float getWidth();
    float getHeight();
    void draw(SpriteBatch batch, float x, float y);
    
    // Combat system
    void update(float delta);
    List<Bullet> generateAttack(float arenaX, float arenaY, float arenaWidth, float arenaHeight);
    EnemyAttackPattern getCurrentPattern();
    
    // Dialogue system
    String getEncounterDialogue();
    String getAttackDialogue();
    String getDefeatDialogue();
    // ...other dialogue methods
    
    // Rewards
    int getExpReward();
    int getGoldReward();
}
```

#### AbstractEnemy
Abstract base class that implements the Enemy interface with common functionality:

```java
public abstract class AbstractEnemy implements Enemy {
    // Basic properties
    protected String name;
    protected int maxHP;
    protected int currentHP;
    protected Texture texture;
    
    // Combat properties
    protected float arenaWidth;
    protected float arenaHeight;
    protected float attackInterval;
    protected int maxBullets;
    protected int attackDamage;
    
    // Pattern management
    protected EnemyAttackPatternManager patternManager;
    protected EnemyAttackPattern currentPattern;
    
    // Methods for scaling enemies based on player level
    public void scaleToPlayerLevel(int playerLevel) {
        // Scale HP, attack damage, and rewards
        int levelDiff = playerLevel - 1;
        this.maxHP = (int)(maxHP * Math.pow(LEVEL_SCALING_FACTOR, levelDiff));
        this.attackDamage = (int)(attackDamage * (1 + levelDiff * 0.25f));
        this.xp = (int)(baseXP * (1 + levelDiff * 0.5f));
        this.baht = (int)(baseGold * (1 + levelDiff * 0.3f));
    }
    
    // Common implementations for movement, attacks, etc.
}
```

#### Concrete Enemy Examples

##### SilkCicada
A mid-tier enemy with different variants based on stage level:

```java
public class SilkCicada extends AbstractEnemy {
    public SilkCicada(int stage) {
        super("Silk Cicada", BASE_HP,
              new Texture(Gdx.files.internal("enemy/baseCicada.png")),
              300f, 300f);
        
        // Configure based on stage range
        if (stage >= 27 && stage <= 29) {
            // High difficulty (red tint)
            this.addAttackPattern(new HighAttackPattern());
            this.primaryColor = new Color(1.0f, 0.3f, 0.3f, 1.0f);
            this.name = "Crimson Silk Cicada";
        } else if (stage >= 24 && stage <= 26) {
            // Medium difficulty (green tint)
            this.addAttackPattern(new MediumAttackPattern());
            this.primaryColor = new Color(0.3f, 1.0f, 0.5f, 1.0f);
            this.name = "Emerald Silk Cicada";
        } else {
            // Low difficulty (no tint)
            this.addAttackPattern(new LowAttackPattern());
            this.primaryColor = Color.WHITE;
        }
        
        // Initialize dialogues and other properties
    }
}
```

## Attack Pattern System

The pattern system provides a modular way to define enemy attack behaviors:

### EnemyAttackPattern Interface
```java
public interface EnemyAttackPattern {
    List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                               float arenaWidth, float arenaHeight);
    String getPatternName();
    AttackPatternConfig getConfig();
}
```

### AttackPatternConfig
Configuration class for attack patterns:
```java
public class AttackPatternConfig {
    private final int minDamage;
    private final int maxDamage;
    private final float patternDuration;
    private final float bulletSpeed;
    private final int maxBullets;
    private final float arenaWidth;
    private final float arenaHeight;
    private final Color bulletColor;
    private final boolean usesCustomColors;
    private final String patternName;
    private final float bulletSize;
    
    // Constructor, getters, etc.
}
```

### Pattern Implementation Example - MediumAttackPattern

```java
public class MediumAttackPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        12, 15, 3.5f, 1.55f, 13, 380f, 330f, new Color(0.3f, 0.7f, 0.1f, 1.0f), true,
        "Cicada's Rhythmic Pulse", 1.8f
    );
    
    // Attack phase trackers
    private int currentPhase = 0;
    private float phaseTimer = 0f;
    
    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                       float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();
        
        // Update timers
        float delta = Gdx.graphics.getDeltaTime();
        phaseTimer += delta;
        
        // Execute current phase
        switch (currentPhase) {
            case 0:
                // Phase 1: Grid pattern of bullets
                createGridPattern(bullets, enemyX, enemyY, playerX, playerY,
                                 baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;
            case 1:
                // Phase 2: Wave pattern toward player
                createWavePattern(bullets, enemyX, enemyY, playerX, playerY,
                                baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;
            case 2:
                // Phase 3: Diagonal cross pattern
                createDiagonalPattern(bullets, enemyX, enemyY, playerX, playerY,
                                    baseSpeed, minDamage, maxDamage, enemyColor);
                currentPhase++;
                break;
        }
        
        // Occasionally spawn healing bullets
        if (MathUtils.random() < HEALING_CHANCE) {
            createHealingBullet(bullets, enemyX, enemyY, playerX, playerY);
        }
        
        return bullets;
    }
    
    // Helper methods to create specific bullet patterns
    private void createGridPattern(...) { /* ... */ }
    private void createWavePattern(...) { /* ... */ }
    private void createDiagonalPattern(...) { /* ... */ }
    private void createHealingBullet(...) { /* ... */ }
    
    @Override
    public String getPatternName() {
        return CONFIG.getPatternName();
    }
    
    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
```

## Combat System

The `CombatScene` class is the core of the gameplay, handling:

### Core Components
- Player/enemy interaction
- Turn-based combat flow
- Bullet spawning and movement
- Damage calculation
- Arena management
- Visual effects
- UI elements for combat

### Combat Flow
1. Player enters combat with an enemy
2. Combat starts with an encounter dialogue
3. Player selects an action (Fight, Skill, Item, Run)
4. If "Fight" is selected, enemy spawns bullets in a pattern
5. Player moves to avoid bullets (bullet-hell gameplay)
6. Damage is calculated based on hits/misses
7. Combat continues until player or enemy is defeated
8. Rewards are given upon victory

### Key Combat Methods
```java
// Start combat with an enemy
public void startCombat() {
    playerTurn = false;
    enemyTurn = true;
    inCombat = true;
    // Set up initial combat state
}

// Handle bullet spawning
private void spawnBullet() {
    // Create bullets based on enemy's current pattern
    // Configure bullet properties
}

// Update bullet positions and check collisions
private void updateBullets(float delta) {
    // Move bullets
    // Check for collisions with player
    // Apply damage if collision occurs
}

// Handle player damage
public void decreaseHP(int damage) {
    // Apply damage to player
    // Show damage number
    // Check for death
    // Apply Death Defiance if available
}
```

## Player Progression

The player progression system handles character development through experience points, leveling, and unlocking new skills.

### Experience and Leveling

```java
private static final int[] EXP_REQUIREMENTS = {
    0,      // LV 1:   0 EXP
    10,     // LV 2:  10 EXP
    30,     // LV 3:  30 EXP
    // ... more levels
    99999   // LV 20: 99999 EXP
};

private static final int MAX_LEVEL = 20;

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

        // Stat increases on level up
        maxHP += 5 + (int)(level * 1.2f);
        currentHP = maxHP;  // Full heal on level up
        attack += 2 + (int)(level * 1.05f);
        maxMP += 3 + (int)(level * 1.087f);
        defense += 1 + (int)(level * 0.4f);
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
```

### Stats System

The player has several key stats that affect combat:

1. **Health Points (HP)**: Determines how much damage the player can take
2. **Mana Points (MP)**: Used to cast skills
3. **Attack**: Determines base damage output
4. **Defense**: Reduces damage taken from enemies
5. **Critical Rate**: Chance to deal critical damage (bonus damage)

Stats can be improved through:
- Leveling up
- Equipment bonuses
- Temporary buffs from items and skills

## Skill System

The skill system provides special abilities that the player can use during combat.

### Skill Types

```java
public enum SkillType {
    BASIC("Slash"),  // Always available
    SKILL1("Dark Silk Face Slap"),
    SKILL2("Silk Scratch of Salvation"),
    SKILL3("Duckfoot Knot from Heaven"),
    SKILL4("Sai-Oua Silk Wrap"),
    SKILL5("Lamphun Blade: Piip Slash Supreme"),
    SKILL6("Silk End - I Am Cosmic Weave");
    
    // ... methods and properties
}
```

### Skill Unlocking

Skills are unlocked based on player progression:
- Some skills unlock at specific player levels
- Some skills unlock after defeating specific bosses
- Some skills unlock when reaching specific stages

```java
public void checkSkillUnlocks() {
    // Level-based unlocks
    if (level >= 12 && !isSkillUnlocked(SkillType.SKILL3)) {
        unlockSkill(SkillType.SKILL3);
    }
    
    // Other unlocks handled by boss defeat or stage progression
}
```

### Skill Usage

Skills require MP to use and provide special effects:

```java
// MP costs for skills
private static final int[] SKILL_MP_COSTS = {
    0,    // BASIC:  0 MP
    15,   // SKILL1: 15 MP
    25,   // SKILL2: 25 MP
    30,   // SKILL3: 30 MP
    35,   // SKILL4: 35 MP
    50,   // SKILL5: 50 MP
    -1    // SKILL6: special case - uses all MP
};

// Calculate skill damage with modifiers
public DamageResult calculateSkillDamage(SkillType skill) {
    boolean isCritical = false;
    int damage = 0;
    
    switch (skill) {
        case BASIC:
            // Basic attack logic
            break;
        case SKILL1:
            // Skill 1 logic with special effects
            break;
        // ... more skills with unique effects
    }
    
    return new DamageResult(damage, isCritical);
}
```

## Item System

Items in SilkBlade provide various effects during and outside combat:

### ItemDatabase
Singleton that loads and manages all available items:

```java
public class ItemDatabase {
    private static ItemDatabase instance;
    private Map<String, Item> items = new HashMap<>();
    
    public static ItemDatabase getInstance() {
        if (instance == null) {
            instance = new ItemDatabase();
        }
        return instance;
    }
    
    // Methods to load items, get items by ID, etc.
}
```

### Inventory System

The player has an inventory to store and use items:

```java
public class Inventory {
    private List<Item> items;
    private Map<Equipment.Slot, Equipment> equippedItems;
    
    // Methods to add, remove, and use items
    public void addItem(Item item) { /* ... */ }
    public void removeItem(Item item) { /* ... */ }
    public void useItem(Item item, Player player) { /* ... */ }
    
    // Equipment methods
    public void equipItem(Equipment equipment) { /* ... */ }
    public void unequipItem(Equipment.Slot slot) { /* ... */ }
    
    // Stat bonus calculations from equipment
    public int getTotalAttackBonus() { /* ... */ }
    public int getTotalDefenseBonus() { /* ... */ }
    public int getTotalMaxHPBonus() { /* ... */ }
    // ... other stat bonus methods
}
```

### Item Types
- **Consumable Items**: Provide temporary effects (healing, buffs)
  - Health Potions: Restore HP
  - Mana Potions: Restore MP
  - Buff Items: Provide temporary stat boosts
  
- **Equipment**: Permanent stat boosts when equipped
  - Weapons: Boost attack and other stats
  - Armor: Boost defense and HP
  - Accessories: Provide special effects
  
- **Key Items**: Used for storyline progression

## Screen Management

### ScreenManager
Handles transitions between game screens and maintains state:

```java
public class ScreenManager {
    private static Screen currentScreen;
    private static Screen previousScreen;
    
    public static void setScreen(Screen screen) {
        previousScreen = currentScreen;
        currentScreen = screen;
        Main.getGame().setScreen(screen);
    }
    
    public static void goBack() {
        if (previousScreen != null) {
            Main.getGame().setScreen(previousScreen);
            
            // Swap current and previous
            Screen temp = currentScreen;
            currentScreen = previousScreen;
            previousScreen = temp;
        }
    }
}
```

## Save System

The game implements a save system that stores:
- Player stats and progress
- Inventory contents
- Unlocked stages
- Game settings

Save files are stored as JSON in the "save" directory.

```java
public void saveToFile() {
    try {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        FileHandle file = Gdx.files.local(SAVE_FILE);
        file.writeString(json.toJson(this), false);
    } catch (Exception e) {
        GameLogger.logError("Failed to save player data", e);
    }
}

public static Player loadFromFile() {
    try {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (file.exists()) {
            Json json = new Json();
            Player player = json.fromJson(Player.class, file);
            
            // Initialize transient fields
            player.initializeSprite();
            player.initializeSounds();
            player.buffManager = new BuffManager(player);
            
            return player;
        }
    } catch (Exception e) {
        GameLogger.logError("Failed to load player data", e);
    }
    return new Player();
}