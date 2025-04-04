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
    
    public Equipment() {
        // Default constructor for JSON deserialization
        this.tier = ItemTier.NORMAL; // Default tier
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
        } else {
            // Handle case where item ID is not found in the database
            this.name = "Unknown Item";
            this.description = "This item appears to be corrupted.";
            this.type = EquipmentType.ACCESSORY; // Default
            this.tier = ItemTier.NORMAL; // Default tier
        }
    }
} 