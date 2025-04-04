package swu.cp112.silkblade.entity.item;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Represents consumable items that can be used by the player.
 */
public class ConsumableItem implements Json.Serializable, Cloneable {
    
    public enum ItemEffect {
        HEAL_HP("Heals HP"),
        RESTORE_MP("Restores MP"),
        BUFF_ATK("Increases ATK temporarily"),
        BUFF_DEF("Increases DEF temporarily");
        
        private final String description;
        
        ItemEffect(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private String id; // Unique ID for the item type
    private String name;
    private String description;
    private ItemEffect effect;
    private ItemTier tier; // Item rarity tier
    private int effectAmount;
    private int quantity;
    private int maxStack; // Maximum stack size (typically 99)
    
    public ConsumableItem() {
        // Default constructor for JSON deserialization
        this.quantity = 1;
        this.maxStack = 99;
        this.tier = ItemTier.NORMAL; // Default tier
    }
    
    public ConsumableItem(String id, String name, String description, ItemEffect effect, int effectAmount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.effect = effect;
        this.effectAmount = effectAmount;
        this.quantity = 1;
        this.maxStack = 99;
        this.tier = ItemTier.NORMAL; // Default tier
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemEffect getEffect() { return effect; }
    public ItemTier getTier() { return tier; }
    public int getEffectAmount() { return effectAmount; }
    public int getQuantity() { return quantity; }
    public int getMaxStack() { return maxStack; }
    
    // Setters (builder pattern)
    public ConsumableItem withTier(ItemTier tier) {
        this.tier = tier;
        return this;
    }
    
    // Stack management
    public boolean increaseQuantity(int amount) {
        if (quantity + amount <= maxStack) {
            quantity += amount;
            return true;
        }
        return false;
    }
    
    public boolean decreaseQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }
    
    public boolean isStackFull() {
        return quantity >= maxStack;
    }
    
    /**
     * Creates a deep copy of this consumable item
     */
    @Override
    public ConsumableItem clone() {
        try {
            return (ConsumableItem) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone ConsumableItem", e);
        }
    }
    
    // Json.Serializable implementation
    @Override
    public void write(Json json) {
        json.writeValue("id", id); // Save only the ID
        json.writeValue("quantity", quantity); // We need to save quantity since it changes
        // No need to save other properties as they will be loaded from the database
    }
    
    @Override
    public void read(Json json, JsonValue jsonData) {
        // Read the ID and quantity from saved data
        id = jsonData.getString("id");
        quantity = jsonData.getInt("quantity", 1);
        
        // Load the item data from the database
        ConsumableItem template = ItemDatabase.getInstance().getConsumableById(id);
        if (template != null) {
            this.name = template.name;
            this.description = template.description;
            this.effect = template.effect;
            this.tier = template.tier;
            this.effectAmount = template.effectAmount;
            this.maxStack = template.maxStack;
        } else {
            // Handle case where item ID is not found in the database
            this.name = "Unknown Item";
            this.description = "This item appears to be corrupted.";
            this.effect = ItemEffect.HEAL_HP; // Default
            this.tier = ItemTier.NORMAL; // Default tier
            this.effectAmount = 0;
            this.maxStack = 99;
        }
    }
} 