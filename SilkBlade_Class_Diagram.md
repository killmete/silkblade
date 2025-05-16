# SilkBlade Class Diagram

## Table of Contents
- [Class Diagram](#class-diagram)
- [Key Relationships](#key-relationships)
- [Core Classes and Interfaces](#core-classes-and-interfaces)
- [Implementation Details](#implementation-details)

## Class Diagram

The following diagram illustrates the key classes and their relationships in the SilkBlade codebase:

```mermaid
classDiagram
    %% Core Game Classes
    Game <|-- Main
    Main --> ScreenManager
    Main --> ItemDatabase
    
    %% Screen Management
    ScreenManager --> Screen
    Screen <|-- MainMenuScreen
    Screen <|-- CombatScene
    Screen <|-- InventoryScreen
    Screen <|-- ShopScreen
    Screen <|-- StageSelectionScreen
    Screen <|-- SaveFileSelectionScreen
    Screen <|-- CharacterCreationScreen
    Screen <|-- OptionsScreen
    Screen <|-- GameOverScreen
    Screen <|-- CreditsScreen
    
    %% Entity System - Enemies
    interface Enemy {
        +String getName()
        +int getMaxHP()
        +int getCurrentHP()
        +void setHP(int)
        +void damage(int, boolean)
        +boolean isDefeated()
        +Texture getTexture()
        +void draw(SpriteBatch, float, float)
        +List~Bullet~ generateAttack(float, float, float, float)
        +EnemyAttackPattern getCurrentPattern()
        +String getEncounterDialogue()
        +int getExpReward()
        +int getGoldReward()
    }
    
    class AbstractEnemy {
        #String name
        #int maxHP
        #int currentHP
        #Texture texture
        #float arenaWidth
        #float arenaHeight
        #float attackInterval
        #int maxBullets
        #int attackDamage
        #EnemyAttackPatternManager patternManager
        #EnemyAttackPattern currentPattern
        +void scaleToPlayerLevel(int)
    }
    
    Enemy <|.. AbstractEnemy
    AbstractEnemy <|-- SilkCicada
    AbstractEnemy <|-- SilkWeaver
    AbstractEnemy <|-- SilkWraith
    AbstractEnemy <|-- SilkGuardian
    AbstractEnemy <|-- SilkRevenant
    AbstractEnemy <|-- GoldenCocoon
    AbstractEnemy <|-- SpiritOfTheLoom
    AbstractEnemy <|-- Threadmancer
    AbstractEnemy <|-- CrimsonSericulture
    AbstractEnemy <|-- HundredSilkOgre
    
    %% Player System
    class Player {
        -int level
        -int maxHP
        -int currentHP
        -int maxMP
        -int currentMP
        -int attack
        -int defense
        -int experiencePoints
        -List~Item~ inventory
        -Map~String, Integer~ skills
        +void levelUp()
        +void addItem(Item)
        +void useItem(Item)
        +void saveToFile()
        +static Player loadFromFile()
    }
    
    %% Attack Pattern System
    interface EnemyAttackPattern {
        +List~Bullet~ generateBullets(Enemy, float, float, float, float)
        +String getPatternName()
        +AttackPatternConfig getConfig()
    }
    
    class AttackPatternConfig {
        -int minDamage
        -int maxDamage
        -float patternDuration
        -float bulletSpeed
        -int maxBullets
        -float arenaWidth
        -float arenaHeight
        -Color bulletColor
        -boolean usesCustomColors
        -String patternName
        -float bulletSize
    }
    
    class EnemyAttackPatternManager {
        -List~EnemyAttackPattern~ patterns
        +void addPattern(EnemyAttackPattern)
        +EnemyAttackPattern selectRandomPattern()
    }
    
    EnemyAttackPattern --> AttackPatternConfig
    EnemyAttackPatternManager --> EnemyAttackPattern
    AbstractEnemy --> EnemyAttackPatternManager
    
    %% Combat System
    class Bullet {
        -float damage
        -float x
        -float y
        -float velocityX
        -float velocityY
        -float size
        -Color color
        -boolean isHealing
        +void update(float)
        +void render(SpriteBatch)
        +Rectangle getHitbox()
    }
    
    class CombatScene {
        -Player player
        -Enemy currentEnemy
        -Array~Bullet~ bullets
        -boolean playerTurn
        -boolean enemyTurn
        -boolean inCombat
        +void startCombat()
        +void spawnBullet()
        +void updateBullets(float)
        +void decreaseHP(int)
    }
    
    CombatScene --> Player
    CombatScene --> Enemy
    CombatScene --> Bullet
    
    %% Item System
    interface Item {
        +String getName()
        +String getDescription()
        +int getValue()
        +void use(Player)
        +Texture getTexture()
    }
    
    class ConsumableItem {
        -int healAmount
        -int manaAmount
        -Map~String, Integer~ statBoosts
        +void use(Player)
    }
    
    class Equipment {
        -Slot equipmentSlot
        -Map~StatType, Integer~ statBonuses
        -Map~StatType, Float~ percentBonuses
        +Slot getSlot()
        +int getStatBonus(StatType)
        +float getPercentBonus(StatType)
    }
    
    class ItemDatabase {
        -static ItemDatabase instance
        -Map~String, Item~ items
        +static ItemDatabase getInstance()
        +Item getItemById(String)
        +List~Item~ getAllItems()
        +List~Item~ getItemsByType(ItemType)
    }
    
    class Inventory {
        -List~Item~ items
        -Map~Equipment.Slot, Equipment~ equippedItems
        +void addItem(Item)
        +void removeItem(Item)
        +void useItem(Item, Player)
        +void equipItem(Equipment)
        +int getTotalAttackBonus()
        +int getTotalDefenseBonus()
    }
    
    enum Equipment.Slot {
        WEAPON
        ARMOR
        ACCESSORY
    }
    
    Item <|.. ConsumableItem
    Item <|.. Equipment
    ItemDatabase --> Item
    Player --> Inventory
    Inventory --> Item
    Inventory --> Equipment.Slot
    Equipment --> Equipment.Slot
    
    %% Buff System
    class BuffManager {
        -Player player
        -Map~StatType, Integer~ statBuffs
        -Map~StatType, Integer~ buffDurations
        +void addBuff(StatType, int, int)
        +void updateBuffs(float)
        +int getAttackBuff()
        +int getDefenseBuff()
        +float getCritRateBuff()
    }
    
    enum BuffManager.StatType {
        ATTACK
        DEFENSE
        CRIT_RATE
    }
    
    BuffManager --> BuffManager.StatType
    Player --> BuffManager
    
    %% Skill System
    enum Player.SkillType {
        BASIC
        SKILL1
        SKILL2
        SKILL3
        SKILL4
        SKILL5
        SKILL6
    }
    
    Player --> Player.SkillType
```

## Key Relationships

### 1. Main Game System
- `Main` extends LibGDX's `Game` class, providing the entry point
- `Main` initializes core systems and manages screen transitions
- Central resource management (camera, viewport, audio) is handled here

### 2. Screen Management
- Various screens extend LibGDX's `Screen` interface
- `ScreenManager` handles navigation between screens
- Each screen type handles a specific game state (menus, combat, inventory)

### 3. Entity System
- `Enemy` interface defines the contract for enemy entities
- `AbstractEnemy` provides common implementation for all enemies
- Concrete enemy classes implement specific behaviors and visuals
- `Player` class manages player state, progression, and abilities
- Both player and enemies participate in the combat system

### 4. Combat System
- `CombatScene` orchestrates bullet-hell gameplay
- `Bullet` represents projectiles in combat with position, velocity, and damage
- Enemy attack patterns determine bullet behavior and formations
- Combat flow is managed through state transitions (playerTurn, enemyTurn)

### 5. Pattern System
- `EnemyAttackPattern` defines attack behaviors through the generation of bullets
- `AttackPatternConfig` provides configuration for attack patterns
- `EnemyAttackPatternManager` manages patterns for enemies
- Patterns determine difficulty and visual style of combat encounters

### 6. Item System
- `Item` interface defines the contract for all items
- `ConsumableItem` implements single-use items with effects
- `Equipment` implements permanent stat-boosting items
- `ItemDatabase` provides centralized item management
- `Inventory` handles the player's collection of items

## Core Classes and Interfaces

### Main.java
The entry point for the application that extends LibGDX's Game class:
```java
public class Main extends Game {
    private static OrthographicCamera camera;
    private static FitViewport viewport;
    private static Music backgroundMusic;
    
    @Override
    public void create() {
        // Initialize core systems
        initializeGraphics();
        initializeItemDatabase();
        initializeAudio();
        setScreen(new MainMenuScreen(this));
    }
    
    // Additional lifecycle and utility methods
}
```

### Enemy.java
Interface defining the contract for all enemy entities:
```java
public interface Enemy {
    String getName();
    int getMaxHP();
    int getCurrentHP();
    void setHP(int hp);
    void damage(int amount, boolean isCritical);
    boolean isDefeated();
    
    Texture getTexture();
    float getWidth();
    float getHeight();
    void draw(SpriteBatch batch, float x, float y);
    
    List<Bullet> generateAttack(float arenaX, float arenaY, float arenaWidth, float arenaHeight);
    EnemyAttackPattern getCurrentPattern();
    
    String getEncounterDialogue();
    String getAttackDialogue();
    String getDefeatDialogue();
    
    int getExpReward();
    int getGoldReward();
}
```

### Player.java
Class managing the player character with progression, stats, and skills:
```java
public class Player implements Json.Serializable {
    // Level and progression
    private int level;
    private int exp;
    
    // Core stats
    private int maxHP;
    private int currentHP;
    private int maxMP;
    private int mp;
    private int attack;
    private int defense;
    private float critRate;
    
    // Skills system
    private boolean[] unlockedSkills;
    private SkillType currentSkill;
    
    // Inventory and equipment
    private Inventory inventory;
    
    // Methods for leveling, combat, and state management
    public boolean gainExp(int amount) { /* ... */ }
    public DamageResult calculateDamage() { /* ... */ }
    public void takeDamage(int damage) { /* ... */ }
    public void heal(int amount) { /* ... */ }
    
    // Skill system methods
    public boolean isSkillUnlocked(SkillType skill) { /* ... */ }
    public void unlockSkill(SkillType skill) { /* ... */ }
    public DamageResult calculateSkillDamage(SkillType skill) { /* ... */ }
    
    // Save/load functionality
    public void saveToFile() { /* ... */ }
    public static Player loadFromFile() { /* ... */ }
}
```

### CombatScene.java
The core gameplay screen handling combat mechanics:
```java
public class CombatScene implements Screen {
    // Core components
    private Player player;
    private Enemy currentEnemy;
    private Array<Bullet> bullets;
    
    // State management
    private boolean playerTurn;
    private boolean enemyTurn;
    private boolean inCombat;
    
    // Core methods
    public void startCombat() { /* ... */ }
    private void updateBullets(float delta) { /* ... */ }
    private void handleCollisions() { /* ... */ }
    private void renderBullets() { /* ... */ }
    
    // Screen implementation
    @Override
    public void render(float delta) { /* ... */ }
    @Override
    public void dispose() { /* ... */ }
}
```

## Implementation Details

### Enemy Creation
Enemies are created with specific properties and attack patterns:

```java
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
```

### Attack Pattern Generation
Patterns generate bullets based on enemy state and configuration:

```java
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
            // Phase 1: Grid pattern
            createGridPattern(bullets, enemyX, enemyY, playerX, playerY,
                             baseSpeed, minDamage, maxDamage, enemyColor);
            currentPhase++;
            break;
        // Additional phases...
    }
    
    return bullets;
}
```

### Player Progression
The leveling system handles stat growth and skill unlocks:

```java
public boolean gainExp(int amount) {
    if (level >= MAX_LEVEL) {
        return false;
    }

    int oldLevel = level;
    exp += amount;

    // Check for level up
    while (level < MAX_LEVEL && exp >= getExpToNextLevel()) {
        level++;
        
        // Stat increases with scaling formulas
        maxHP += 5 + (int)(level * 1.2f);
        currentHP = maxHP;  // Full heal on level up
        attack += 2 + (int)(level * 1.05f);
        maxMP += 3 + (int)(level * 1.087f);
        defense += 1 + (int)(level * 0.4f);
        critRate = Math.min(0.1f + (level * 0.008f), 0.35f);
        
        // Check for skill unlocks
        checkSkillUnlocks();
    }
    
    return level > oldLevel;
}
``` 