# SilkBlade Game Overview

<img src="assets/title.png" alt="SilkBlade Title" width="300" align="center"/>

## Table of Contents
- [Game Concept](#game-concept)
- [Core Gameplay Loop](#core-gameplay-loop)
- [Key Game Systems](#key-game-systems)
  - [Combat System](#combat-system)
  - [Character Progression](#character-progression)
  - [Skill System](#skill-system)
  - [Item and Equipment System](#item-and-equipment-system)
  - [Stage Progression](#stage-progression)
- [Technical Architecture](#technical-architecture)
  - [Core Systems](#core-systems)
  - [Enemy Design](#enemy-design)
  - [Pattern Design](#pattern-design)
- [Art and Audio](#art-and-audio)
- [Save and Load System](#save-and-load-system)
- [Running the Game](#running-the-game)
- [Contribution Guidelines](#contribution-guidelines)

## Game Concept
SilkBlade is a turn-based bullet-hell RPG built with [LibGDX](https://libgdx.com/), a Java game development framework. The game combines traditional RPG elements like character progression, equipment, and skills with action-based combat where players must navigate through intricate bullet patterns.

The game is set in a world influenced by Thai silk weaving culture, with enemies and skills themed around silk, weaving, and local cultural elements. The narrative follows a protagonist's journey through increasingly challenging areas to defeat five legendary silk entities.

**Game Features:**
- Turn-based combat with real-time bullet-dodging mechanics
- Progression system with 20 player levels and stat scaling
- 7 unique skills with distinctive effects and animations, each with custom sound effects
- 5 boss enemies and 5 regular enemy types, each with unique attack patterns
- Equipment system with three slot types: weapons, armor, and accessories
- Item system with consumables, stat boosters, and special items
- Death Defiance system that gives players a second chance in combat
- Dynamic difficulty scaling based on player level and stage number
- Comprehensive save/load system using JSON serialization
- Immersive sound effects and music that adapt to game context

## Core Gameplay Loop

```mermaid
graph LR
    A[Select Stage] --> B[Encounter Enemy]
    B --> C[Combat Phase]
    C --> D[Dodge Bullets]
    D --> E[Deal Damage]
    E --> F{Enemy Defeated?}
    F -->|Yes| G[Collect Rewards]
    G --> H[Level Up/Shop]
    H --> A
    F -->|No| C
```

1. **Stage Selection**: Players select stages from the StageSelectionScreen, with difficulty increasing as they progress.
2. **Combat Encounter**: Each stage features enemies with unique attack patterns. Combat is initiated through the CombatScene class.
3. **Tactical Decision**: Players choose from four actions:
   - **FIGHT**: Enter bullet-hell mode to dodge enemy attacks
   - **SKILL**: Use special abilities that consume MP
   - **ITEM**: Use consumable items for various effects
   - **RUN**: Attempt to escape combat (success rate depends on enemy type)
4. **Bullet-Hell Phase**: During the FIGHT or after using a SKILL, enemies spawn bullet patterns that the player must dodge. The pattern complexity increases with stage level.
5. **Damage Calculation**: Successfully dodging leads to dealing damage to the enemy based on player stats and skills used.
6. **Rewards**: Defeating enemies grants experience points (scaled by enemy level) and gold for purchasing items in the shop.
7. **Progression**: Players level up, unlock new skills, and can purchase items between combat encounters.
8. **Boss Encounters**: At milestone stages (10, 20, 30, 40, and 50), players face bosses that unlock new skills upon defeat.

## Key Game Systems

### Combat System
SilkBlade's combat is implemented in the `CombatScene` class (~4000 lines), making it the most complex part of the game:

```java
// Combat phase initialization
public void startCombat() {
    playerTurn = false;
    enemyTurn = true;
    inCombat = true;
    combatStartGraceTimer = COMBAT_START_GRACE_PERIOD;
    // Setup arena and enemy positions
    centerArena();
    updateArenaForPattern();
    // Initialize player position at center of arena
    playerX = arena.x + arena.width / 2;
    playerY = arena.y + arena.height / 2;
}
```

- **Bullet-Hell Mechanics**: 
  - Bullets are generated through enemy attack patterns specific to each enemy type
  - Each bullet has properties including position, velocity, size, color, damage, and special effects
  - Bullet collisions use Rectangle hitboxes with precise collision detection
  - Visual telegraphing shows bullet spawn locations before bullets appear
  - Bullets feature visual trails that help with tracking during intense patterns
  - Some bullets can have special behaviors like homing, zigzagging, or healing

- **Turn-Based Structure**: 
  - Combat flow is managed using state flags (`playerTurn`, `enemyTurn`, `inCombat`)
  - Combat phases include encounter dialogue, action selection, bullet dodging, and result calculation
  - Arena size dynamically adjusts based on the enemy's attack pattern configuration
  - Grace period at combat start gives players time to prepare

- **Damage System**: 
  - Damage calculation includes base damage, critical hits, and skill modifiers
  - Player stats (attack, defense) and equipment significantly affect damage dealt and received
  - Damage numbers appear with animations (upward floating movement) and color coding
  - Hit effects include screen shake, sound effects, and visual flashes
  - Damage numbers show both regular and critical hits in different colors

- **Death Defiance**: 
  - Survival mechanic that activates when player would be defeated
  - Provides temporary invincibility with rainbow visual effect
  - Duration: 5 seconds with a massive defense boost (999999)
  - Limited to once per combat encounter
  - Visually indicated by a rainbow outline around the player

### Character Progression
Character progression is managed by the `Player` class:

```java
// Experience table and level caps
private static final int[] EXP_REQUIREMENTS = {
    0,      // LV 1:   0 EXP
    10,     // LV 2:  10 EXP
    30,     // LV 3:  30 EXP
    /* ... */
    99999   // LV 20: 99999 EXP
};

// Leveling logic
public boolean gainExp(int amount) {
    if (level >= MAX_LEVEL) {
        return false;
    }

    int oldLevel = level;
    exp += amount;

    // Check for level up
    while (level < MAX_LEVEL && exp >= getExpToNextLevel()) {
        level++;
        
        // Stat increases with specialized scaling formulas
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

- **Level System**: 
  - Maximum level: 20
  - Exponential XP curve (10 XP for level 2, 99,999 XP for level 20)
  - Enemies provide scaled XP based on their level and type
  - Boss encounters provide significant XP boosts
  - Full heal on level up and automatic skill unlocks

- **Stat System**:
  - **Health Points (HP)**: Determines survival capacity (scales by 5 + level*1.2 per level)
  - **Mana Points (MP)**: Resource for using skills (scales by 3 + level*1.087 per level)
  - **Attack**: Determines damage output (scales by 2 + level*1.05 per level)
  - **Defense**: Reduces incoming damage (scales by 1 + level*0.4 per level)
  - **Critical Rate**: Chance for critical hits (scales by 0.008 per level, capped at 35%)
  - **Gold**: Currency for purchasing items and equipment

- **Buff System**:
  - Temporary stat enhancements from items, skills, or equipment
  - Managed by the specialized `BuffManager` class
  - Buff types include Attack, Defense, and Critical Rate
  - Visual indicators show active buffs with remaining duration
  - Buffs expire after their duration and can be stacked

### Skill System
The skill system provides special abilities through the `SkillType` enum and methods in the `Player` class:

```java
public enum SkillType {
    BASIC("Slash"),  // Always available
    SKILL1("Dark Silk Face Slap"),
    SKILL2("Silk Scratch of Salvation"),
    SKILL3("Duckfoot Knot from Heaven"),
    SKILL4("Sai-Oua Silk Wrap"),
    SKILL5("Lamphun Blade: Piip Slash Supreme"),
    SKILL6("Silk End - I Am Cosmic Weave");
    
    private final String displayName;
    
    // Constructor and methods...
}

// MP costs for skills
private static final int[] SKILL_MP_COSTS = {
    0,    // BASIC:  0 MP
    15,   // SKILL1: 15 MP - Dark Silk Face Slap
    25,   // SKILL2: 25 MP - Silk Scratch of Salvation
    30,   // SKILL3: 30 MP - Duckfoot Knot from Heaven
    35,   // SKILL4: 35 MP - Sai-Oua Silk Wrap
    50,   // SKILL5: 50 MP - Lamphun Blade: Piip Slash Supreme
    -1    // SKILL6: special case - uses all MP
};
```

- **Skill Unlock Conditions**:
  - **Dark Silk Face Slap**: Unlocked after defeating the boss on stage 10 (SilkGuardian)
  - **Silk Scratch of Salvation**: Unlocked after defeating the boss on stage 30 (Threadmancer)
  - **Duckfoot Knot from Heaven**: Unlocked when player reaches level 12
  - **Sai-Oua Silk Wrap**: Unlocked after defeating the boss on stage 20 (GoldenCocoon)
  - **Lamphun Blade: Piip Slash Supreme**: Unlocked after defeating the boss on stage 40 (CrimsonSericulture) 
  - **Silk End - I Am Cosmic Weave**: Unlocked after defeating the final boss on stage 50 (SilkRevenant)

- **Skill Effects**:
  - **Basic Slash**: Standard attack with no MP cost
  - **Dark Silk Face Slap**: Medium damage with increased critical hit chance
  - **Silk Scratch of Salvation**: Multiple hits with cumulative damage and chance for double attack
  - **Duckfoot Knot from Heaven**: High damage with defense penetration and increased critical damage
  - **Sai-Oua Silk Wrap**: Damage plus a 3-turn defense buff for the player
  - **Lamphun Blade: Piip Slash Supreme**: Massive damage with guaranteed critical hit
  - **Silk End - I Am Cosmic Weave**: Ultimate skill that uses all MP for devastating damage proportional to MP spent

- **Skill Implementation**:
  - Each skill has unique sound effects with different durations
  - MP costs are scaled by skill power and impact
  - Skills can apply buffs, healing, or other special effects
  - Combat messages update based on the skill used
  - Special animations and visual effects for each skill
  - Some skills include secondary effects like buffs or stat modifications

### Item and Equipment System

The inventory system is implemented through the `Inventory` class and various item types:

```java
public class Inventory {
    private List<Item> items;
    private Map<Equipment.Slot, Equipment> equippedItems;
    
    // Methods to manage inventory and equipment
}

// Equipment slot types
public enum Slot {
    WEAPON,
    ARMOR,
    ACCESSORY
}
```

- **Item Categories**:
  - **Consumables**: Single-use items with immediate effects
    - Health potions (Small, Medium, Large): Restore different amounts of HP
    - Mana potions (Small, Medium, Large): Restore different amounts of MP
    - Stat boosters: Temporarily increase Attack, Defense, or Critical Rate
    - Special items: Unique effects like full restoration or Death Defiance reset
  
  - **Equipment**: Permanent stat modifiers when equipped
    - **Weapons**: Primary Attack boost with secondary stats
      - Examples: Silk Dagger (+5 Attack), Ceremonial Blade (+12 Attack, +5% Crit)
    - **Armor**: Primary Defense boost with secondary stats
      - Examples: Silk Garb (+5 Defense), Royal Weave (+15 Defense, +20 Max HP)
    - **Accessories**: Special effects and stat combinations
      - Examples: Lucky Charm (+5% Crit), Silk Emblem (+5% all stats)

- **Inventory Management**:
  - Items are stored and managed in a player's inventory
  - Equipped items occupy specific slots (weapon, armor, accessory)
  - Players can only have one item equipped per slot
  - Equipping a new item automatically unequips the current item in that slot
  - Items can be used during combat or from the inventory screen
  - Item effects are processed through the ItemEffectSystem

- **Equipment Effects**:
  - Equipment provides both flat stat bonuses and percentage-based bonuses
  - Bonuses are calculated dynamically when player stats are requested
  - Multiple equipment pieces can provide synergistic effects
  - Special equipment pieces can provide unique buffs or abilities
  - Higher-tier equipment becomes available as the player progresses

### Stage Progression
The stage system controls game difficulty and progression:

```java
// Player stage tracking
private int currentStage = 1; // Default to stage 1

// Setting stage unlocks skills and adjusts difficulty
public void setCurrentStage(int currentStage) {
    this.currentStage = currentStage;
    // Check if this stage change should unlock any skills
    checkStageUnlocks();
    // Save player data after updating stage
    saveToFile();
}
```

- **Stage Structure**:
  - 50 total stages with increasing difficulty
  - Stages are selected from the StageSelectionScreen
  - Completing a stage unlocks the next stage
  - Each stage features a specific enemy type scaled to the stage number
  - Boss stages (10, 20, 30, 40, 50) unlock new skills and abilities
  - Higher stages provide increased XP and gold rewards

- **Enemy Assignments**:
  - **Stages 1-9**: SilkWraith (Regular Enemy)
  - **Stage 10**: SilkGuardian (Boss)
  - **Stages 11-19**: SilkWeaver (Regular Enemy)
  - **Stage 20**: GoldenCocoon (Boss)
  - **Stages 21-29**: SilkCicada (Regular Enemy)
  - **Stage 30**: Threadmancer (Boss)
  - **Stages 31-39**: SpiritOfTheLoom (Regular Enemy)
  - **Stage 40**: CrimsonSericulture (Boss)
  - **Stages 41-49**: HundredSilkOgre (Regular Enemy)
  - **Stage 50**: SilkRevenant (Final Boss)

- **Enemy Scaling**:
  - Enemy stats are dynamically scaled based on stage number and player level
  - Scaling follows an exponential formula for HP: `baseHP * Math.pow(LEVEL_SCALING_FACTOR, levelDiff)`
  - Attack damage scales linearly: `baseAttack * (1 + levelDiff * 0.25f)`
  - XP and gold rewards also scale with level
  - Higher stages feature more complex bullet patterns with increased damage

## Technical Architecture

### Core Systems

- **Main Game Class** (`Main.java`):
  ```java
  public class Main extends Game {
      // Core components
      private static OrthographicCamera camera;
      private static FitViewport viewport;
      private static Music backgroundMusic;
      
      @Override
      public void create() {
          // Initialize systems and set initial screen
          initializeGraphics();
          initializeItemDatabase();
          initializeAudio();
          setScreen(new MainMenuScreen(this));
      }
      
      // Resource management and other methods...
  }
  ```
  - Entry point for the application
  - Manages global resources like camera, viewport, and audio
  - Handles screen transitions
  - Provides static access to core game components

- **Screen System**:
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
          // Return to previous screen with proper cleanup
      }
  }
  ```
  - Implements various game screens through LibGDX's Screen interface
  - Manages transitions between screens
  - Maintains screen state and history for navigation

- **Entity System**:
  - Hierarchical structure for game objects
  - Base interfaces define common behaviors
  - Abstract classes provide shared implementation
  - Concrete classes implement specific functionality

- **Pattern System**:
  ```java
  public interface EnemyAttackPattern {
      List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                float arenaWidth, float arenaHeight);
      String getPatternName();
      AttackPatternConfig getConfig();
  }
  ```
  - Modular design for enemy attack behaviors
  - Configuration-driven pattern definition
  - Phase-based patterns with timing controls

### Enemy Design

Enemies are implemented with a robust class hierarchy:

```java
// Base interface defines contract
public interface Enemy {
    // Core properties and methods
    String getName();
    int getMaxHP();
    int getCurrentHP();
    void setHP(int hp);
    void damage(int amount, boolean isCritical);
    // Visual properties
    Texture getTexture();
    void draw(SpriteBatch batch, float x, float y);
    // Combat methods
    List<Bullet> generateAttack(float arenaX, float arenaY, float arenaWidth, float arenaHeight);
    // ... other methods
}

// Abstract base class provides common implementation
public abstract class AbstractEnemy implements Enemy {
    // Shared fields and implementation
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
    
    // Common methods with shared logic
    public void scaleToPlayerLevel(int playerLevel) {
        // Scaling formulas...
    }
    
    // Other shared implementation...
}
```

- **Enemy Types**:
  - **Regular Enemies**: 
    - SilkWraith: Fast-moving enemy with basic attack patterns for stages 1-9
    - SilkWeaver: Mid-tier enemy with more complex patterns for stages 11-19
    - SilkCicada: Advanced enemy with color variants for stages 21-29
    - SpiritOfTheLoom: Expert enemy with ethereal patterns for stages 31-39
    - HundredSilkOgre: Master enemy with powerful attacks for stages 41-49
  
  - **Boss Enemies**:
    - SilkGuardian: Stage 10 boss with defensive abilities and multi-phase attacks
    - GoldenCocoon: Stage 20 boss with barrier patterns and healing abilities
    - Threadmancer: Stage 30 boss with complex thread-based attack patterns
    - CrimsonSericulture: Stage 40 boss with devastating multi-phase patterns
    - SilkRevenant: Final boss (stage 50) with resurrection mechanics and ultimate patterns

- **Enemy Implementation**:
  - Each enemy has custom properties including name, HP, textures, and size
  - Enemy dialogue is tailored to each enemy type for narrative progression
  - Enemies use dynamic scaling based on player level and stage number
  - Visual effects include color tints, shaking animations, and alpha transitions
  - Bosses have multiple attack patterns that change during combat
  - Enemies provide scaled XP and gold rewards based on their difficulty

### Pattern Design

Attack patterns follow a modular approach to create varied combat experiences:

```java
// Core interface for all attack patterns
public interface EnemyAttackPattern {
    List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                               float arenaWidth, float arenaHeight);
    String getPatternName();
    AttackPatternConfig getConfig();
}

// Configuration for patterns
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

// Example pattern implementation
public class MediumAttackPattern implements EnemyAttackPattern {
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        12, 15, 3.5f, 1.55f, 13, 380f, 330f, 
        new Color(0.3f, 0.7f, 0.1f, 1.0f), true,
        "Cicada's Rhythmic Pulse", 1.8f
    );
    
    // Pattern implementation...
    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                     float arenaWidth, float arenaHeight) {
        // Create bullets in specific formations...
    }
}
```

- **Pattern System Architecture**:
  - `EnemyAttackPattern` interface defines the contract for all patterns
  - `AttackPatternConfig` provides configuration options for each pattern
  - `EnemyAttackPatternManager` handles pattern selection and management
  - Each enemy type has its own set of specialized patterns
  - Patterns are organized in packages by enemy type (e.g., silkwraith, silkguardian)

- **Pattern Categories**:
  - **Grid Patterns**: Bullets arranged in grid formations with precise spacing
  - **Wave Patterns**: Bullets moving in wave-like motions with sine/cosine trajectories
  - **Spiral Patterns**: Bullets emerging in spiral formations with angular calculations
  - **Targeted Patterns**: Bullets that track player movement and adjust trajectory
  - **Random Patterns**: Bullets with randomized trajectories for unpredictable challenges
  - **Combined Patterns**: Complex patterns using multiple techniques in sequence
  - **Boss Patterns**: Multi-phase patterns with increasing difficulty and telegraphing

- **Bullet Properties**:
  - Customizable damage ranges based on enemy and pattern
  - Adjustable speeds, sizes, and colors
  - Special behaviors including homing, zigzagging, spiraling, and bouncing
  - Visual effects like trails, glows, and telegraphed paths
  - Healing bullets that provide risk/reward mechanics
  - Bullet collision detection with precise hitboxes

## Art and Audio

The game uses sprites and sound effects to create an immersive experience:

- **Visual Assets**:
  - Character sprites with animation frames
  - Enemy sprites with color variations
  - Background art for different stages
  - UI elements for menus and combat
  - Special effect sprites for skills and hits
  - Particle effects for impacts and transitions

- **Audio System**:
  ```java
  // Audio volume control
  public static void setMusicVolume(float volume) {
      musicVolume = volume;
      if (backgroundMusic != null) {
          backgroundMusic.setVolume(musicVolume);
      }
  }
  
  // Play sound effects with volume control
  public static long playSound(Sound sound) {
      if (sound != null) {
          return sound.play(effectVolume);
      }
      return -1;
  }
  ```
  - Background music that changes based on context
  - Sound effects for actions, hits, and UI interactions
  - Volume controls for music and effects
  - Audio caching for performance

## Save and Load System

The game implements a comprehensive save system for player progression using LibGDX's serialization capabilities:

```java
// Save player data to JSON file
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

// Load player data from JSON file
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
```

- **Serialization Implementation**:
  - Uses LibGDX's `Json` class for serialization/deserialization
  - `Json.Serializable` interface implementation with custom `write()` and `read()` methods
  - Handles complex object graphs and nested collections
  - Transient fields (non-serialized) include resources like textures, sprites, and sounds
  - These resources are reinitialized after loading from the save file

- **Saved Data**:
  - Player stats (level, XP, HP, MP, attack, defense, critRate)
  - Current stage progression and unlocked stages
  - Inventory contents including all items
  - Equipped items in each equipment slot
  - Unlocked skills and current skill selections
  - Boss defeat status for tracking progression
  - Gold and other resources
  - Game options and settings

- **Save File Management**:
  - Save files are stored as JSON in the "save" directory
  - Automatic saving after significant events like defeating enemies
  - Error handling with logging for debugging save/load issues
  - Fallback to new player creation if loading fails

## Running the Game

### Requirements
- Java 8 or higher
- OpenGL-compatible graphics card
- 512MB RAM (minimum)
- 100MB disk space
- Windows, macOS, or Linux

### Building from Source
1. Clone the repository: `git clone https://github.com/username/silkblade.git`
2. Navigate to the project directory: `cd silkblade`
3. Build with Gradle:
   - Windows: `gradlew.bat build`
   - macOS/Linux: `./gradlew build`
4. Run the desktop version:
   - Windows: `gradlew.bat desktop:run`
   - macOS/Linux: `./gradlew desktop:run`

### Controls
- **WASD or Arrow Keys**: Move player during combat
- **Z or Enter**: Confirm selection
- **X or Escape**: Cancel/Back
- **Space**: Skip dialogue
- **Tab**: Quick access to inventory during exploration
- **Page Up/Down**: Navigate menu pages for skills and items

### Game Files and Structure
The project uses a standard Gradle structure:
- **core/**: Contains the main game code
  - **src/main/java/swu/cp112/silkblade/**: Java source files
  - **src/main/resources/**: Game assets like images and sounds
- **lwjgl3/**: Desktop launcher implementation
- **assets/**: Game assets including textures, sounds, and music
- **save/**: Directory for save files
- **gradle/**: Gradle wrapper and build configuration

### Configuration
The game supports several configuration options in the options.json file:
- Music volume
- Sound effect volume
- Graphics quality settings
- Control customization

## Contribution Guidelines

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Coding Standards
- Follow Java naming conventions
  - Classes: PascalCase (e.g., `PlayerInventory`)
  - Methods/Variables: camelCase (e.g., `getCurrentHP()`)
  - Constants: UPPER_SNAKE_CASE (e.g., `MAX_LEVEL`)
- Add JavaDoc comments for all public methods and classes
- Maintain the existing architecture patterns and class hierarchies
- Write unit tests for new features when possible
- Use proper exception handling with the GameLogger class

### Adding New Content
- **New Enemies**: Extend AbstractEnemy class and implement required methods
- **New Patterns**: Implement EnemyAttackPattern interface with appropriate configurations
- **New Items**: Implement the Item interface or extend existing item types
- **New Skills**: Add to SkillType enum and implement effects in Player class
- **New Screens**: Implement LibGDX's Screen interface following existing patterns

### Asset Guidelines
- Sprites should be PNG format with transparency
- Audio files should be MP3 (music) or WAV (sound effects)
- Include attribution information for all external assets
- Maintain the existing art style and theme
- Optimize assets for performance (appropriate resolution, compressed formats)

### Testing and Quality Assurance
- Test new features thoroughly before submitting
- Ensure backward compatibility with existing save files
- Check performance on lower-end hardware when possible
- Verify that new content is balanced with existing game progression
- Follow the debugging guidelines in the CONTRIBUTING.md file 