package swu.cp112.silkblade.entity.item;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Represents equippable items that can be worn by the player.
 */
public class Equipment implements Json.Serializable, Cloneable {
    
    public enum EquipmentType {
        WEAPON("Weapon"),
        ARMOR("Armor"),
        ACCESSORY("Accessory");
        
        private final String displayName;
        
        EquipmentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private String id; // Unique ID for the item type
    private String name;
    private String description;
    private EquipmentType type;
    private ItemTier tier; // Item rarity tier
    private int attackBonus;
    private int defenseBonus;
    private int maxHPBonus;
    private int maxMPBonus;
    private float critRateBonus;
    
    // Percentage-based bonuses (0.05f = 5%, 0.20f = 20%, etc.)
    private float maxHPPercentBonus;
    private float maxMPPercentBonus;
    private float attackPercentBonus;
    private float defensePercentBonus;
    
    // Special effects
    private boolean hasDoubleAttack; // For weapons: chance to attack twice
    private float thornDamage; // For armor: percentage of damage reflected back to attacker
    private boolean hasDeathDefiance; // For accessories: grants a one-time cheat death ability per combat
    private boolean hasFreeSkillCast; // Grants a one-time free skill cast per battle (no MP cost)
    
    public Equipment() {
        // Default constructor for JSON deserialization
        this.tier = ItemTier.NORMAL; // Default tier
        this.maxHPPercentBonus = 0f;
        this.maxMPPercentBonus = 0f;
        this.attackPercentBonus = 0f;
        this.defensePercentBonus = 0f;
        this.hasDoubleAttack = false;
        this.thornDamage = 0f;
        this.hasDeathDefiance = false;
        this.hasFreeSkillCast = false;
    }
    
    public Equipment(String id, String name, String description, EquipmentType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.tier = ItemTier.NORMAL; // Default tier
        this.attackBonus = 0;
        this.defenseBonus = 0;
        this.maxHPBonus = 0;
        this.maxMPBonus = 0;
        this.critRateBonus = 0;
        this.maxHPPercentBonus = 0f;
        this.maxMPPercentBonus = 0f;
        this.attackPercentBonus = 0f;
        this.defensePercentBonus = 0f;
        this.hasDoubleAttack = false;
        this.thornDamage = 0f;
        this.hasDeathDefiance = false;
        this.hasFreeSkillCast = false;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EquipmentType getType() { return type; }
    public ItemTier getTier() { return tier; }
    public int getAttackBonus() { return attackBonus; }
    public int getDefenseBonus() { return defenseBonus; }
    public int getMaxHPBonus() { return maxHPBonus; }
    public int getMaxMPBonus() { return maxMPBonus; }
    public float getCritRateBonus() { return critRateBonus; }
    public float getMaxHPPercentBonus() { return maxHPPercentBonus; }
    public float getMaxMPPercentBonus() { return maxMPPercentBonus; }
    public float getAttackPercentBonus() { return attackPercentBonus; }
    public float getDefensePercentBonus() { return defensePercentBonus; }
    public boolean hasDoubleAttack() { return hasDoubleAttack; }
    public float getThornDamage() { return thornDamage; }
    public boolean hasDeathDefiance() { return hasDeathDefiance; }
    public boolean hasFreeSkillCast() { return hasFreeSkillCast; }
    
    // Setters (builder pattern)
    public Equipment withTier(ItemTier tier) {
        this.tier = tier;
        return this;
    }
    
    public Equipment withAttackBonus(int attackBonus) {
        this.attackBonus = attackBonus;
        return this;
    }
    
    public Equipment withDefenseBonus(int defenseBonus) {
        this.defenseBonus = defenseBonus;
        return this;
    }
    
    public Equipment withMaxHPBonus(int maxHPBonus) {
        this.maxHPBonus = maxHPBonus;
        return this;
    }
    
    public Equipment withMaxMPBonus(int maxMPBonus) {
        this.maxMPBonus = maxMPBonus;
        return this;
    }
    
    public Equipment withCritRateBonus(float critRateBonus) {
        this.critRateBonus = critRateBonus;
        return this;
    }
    
    public Equipment withMaxHPPercentBonus(float percentBonus) {
        this.maxHPPercentBonus = percentBonus;
        return this;
    }
    
    public Equipment withMaxMPPercentBonus(float percentBonus) {
        this.maxMPPercentBonus = percentBonus;
        return this;
    }
    
    public Equipment withAttackPercentBonus(float percentBonus) {
        this.attackPercentBonus = percentBonus;
        return this;
    }
    
    public Equipment withDefensePercentBonus(float percentBonus) {
        this.defensePercentBonus = percentBonus;
        return this;
    }
    
    public Equipment withDoubleAttack(boolean hasDoubleAttack) {
        this.hasDoubleAttack = hasDoubleAttack;
        return this;
    }
    
    public Equipment withThornDamage(float thornDamage) {
        this.thornDamage = thornDamage;
        return this;
    }
    
    public Equipment withDeathDefiance(boolean hasDeathDefiance) {
        this.hasDeathDefiance = hasDeathDefiance;
        return this;
    }
    
    public Equipment withFreeSkillCast(boolean hasFreeSkillCast) {
        this.hasFreeSkillCast = hasFreeSkillCast;
        return this;
    }
    
    /**
     * Creates a deep copy of this equipment
     */
    @Override
    public Equipment clone() {
        try {
            return (Equipment) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone Equipment", e);
        }
    }
    
    // Json.Serializable implementation
    @Override
    public void write(Json json) {
        json.writeValue("id", id); // Save only the ID
        // No need to save all the other properties as they will be loaded from the database
    }
    
    @Override
    public void read(Json json, JsonValue jsonData) {
        // Only read the ID; other properties will be loaded from the database
        id = jsonData.getString("id");
        
        // Load the equipment data from the database
        Equipment template = ItemDatabase.getInstance().getEquipmentById(id);
        if (template != null) {
            this.name = template.name;
            this.description = template.description;
            this.type = template.type;
            this.tier = template.tier;
            this.attackBonus = template.attackBonus;
            this.defenseBonus = template.defenseBonus;
            this.maxHPBonus = template.maxHPBonus;
            this.maxMPBonus = template.maxMPBonus;
            this.critRateBonus = template.critRateBonus;
            this.maxHPPercentBonus = template.maxHPPercentBonus;
            this.maxMPPercentBonus = template.maxMPPercentBonus;
            this.attackPercentBonus = template.attackPercentBonus;
            this.defensePercentBonus = template.defensePercentBonus;
            this.hasDoubleAttack = template.hasDoubleAttack;
            this.thornDamage = template.thornDamage;
            this.hasDeathDefiance = template.hasDeathDefiance;
            this.hasFreeSkillCast = template.hasFreeSkillCast;
        } else {
            // Handle case where item ID is not found in the database
            this.name = "Unknown Item";
            this.description = "This item appears to be corrupted.";
            this.type = EquipmentType.ACCESSORY; // Default
            this.tier = ItemTier.NORMAL; // Default tier
        }
    }
} 