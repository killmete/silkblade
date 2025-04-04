package swu.cp112.silkblade.entity.item;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Manages the player's equipment and storage.
 */
public class Inventory implements Json.Serializable {

    private static final int MAX_EQUIPPED_SLOTS = 3; // Weapon, Armor, Accessory
    private static final int MAX_INVENTORY_SLOTS = 8; // Active inventory slots
    private static final int MAX_COMBAT_ITEMS = 8; // Maximum combat items allowed

    private Array<Equipment> equippedItems;
    private Array<Equipment> inventoryItems;
    private Array<Equipment> storageItems; // Storage box for unused items
    private Array<ConsumableItem> consumableItems; // Consumable items (can stack)
    private Array<ConsumableItem> combatItems; // Items selected for use in combat

    public Inventory() {
        equippedItems = new Array<>(MAX_EQUIPPED_SLOTS);
        // Initialize equipped slots with null values
        for (int i = 0; i < MAX_EQUIPPED_SLOTS; i++) {
            equippedItems.add(null);
        }

        inventoryItems = new Array<>(true, MAX_INVENTORY_SLOTS);
        storageItems = new Array<>();
        consumableItems = new Array<>();
        combatItems = new Array<>(true, MAX_COMBAT_ITEMS);

        // Add starter items for testing
        addStarterItems();
    }

    /**
     * Add some starter items for testing.
     */
    private void addStarterItems() {
        // Get items from the database
        ItemDatabase db = ItemDatabase.getInstance();

        // Normal tier examples (already in starter items)
        Equipment realKnife = db.getEquipmentById("WEAPON_REAL_KNIFE");
        if (realKnife != null) {
            addToInventory(realKnife);
        }

        Equipment heartLocket = db.getEquipmentById("ACCESSORY_HEART_LOCKET");
        if (heartLocket != null) {
            addToInventory(heartLocket);
        }

        // Rare tier example items
        Equipment azureBlade = db.getEquipmentById("TIER_RARE_WEAPON");
        if (azureBlade != null) {
            addToInventory(azureBlade);
        }

        Equipment cobaltMail = db.getEquipmentById("TIER_RARE_ARMOR");
        if (cobaltMail != null) {
            addToInventory(cobaltMail);
        }

        // Heroic tier example items
        Equipment amethystSaber = db.getEquipmentById("TIER_HEROIC_WEAPON");
        if (amethystSaber != null) {
            addToInventory(amethystSaber);
        }

        Equipment violetPendant = db.getEquipmentById("TIER_HEROIC_ACCESSORY");
        if (violetPendant != null) {
            addToInventory(violetPendant);
        }

        // Legendary tier example item
        Equipment excalibur = db.getEquipmentById("TIER_LEGENDARY_WEAPON");
        if (excalibur != null) {
            addToInventory(excalibur);
        }

        Equipment divine_plate = db.getEquipmentById("TIER_LEGENDARY_ARMOR");
        if (divine_plate != null) {
            addToInventory(divine_plate);
        }

        // Genesis tier example item
        Equipment etherealCrown = db.getEquipmentById("TIER_GENESIS_ACCESSORY");
        if (etherealCrown != null) {
            addToInventory(etherealCrown);
        }

        // Add example consumables of different tiers
        ConsumableItem elixir = db.getConsumableById("CONSUMABLE_ELIXIR");
        if (elixir != null) {
            elixir.increaseQuantity(2); // Start with 3 elixirs (1 + 2)
            addConsumableItem(elixir);
        }

        ConsumableItem manaPotion = db.getConsumableById("CONSUMABLE_MANA_POTION");
        if (manaPotion != null) {
            manaPotion.increaseQuantity(98);
            addConsumableItem(manaPotion);
        }

        // Add rare tier consumable
        ConsumableItem greaterHealing = db.getConsumableById("TIER_RARE_POTION");
        if (greaterHealing != null) {
            addConsumableItem(greaterHealing);
        }

        // Add heroic tier consumable
        ConsumableItem royalElixir = db.getConsumableById("TIER_HEROIC_POTION");
        if (royalElixir != null) {
            addConsumableItem(royalElixir);
        }

        // Add legendary tier consumable
        ConsumableItem goldenNectar = db.getConsumableById("TIER_LEGENDARY_POTION");
        if (goldenNectar != null) {
            addConsumableItem(goldenNectar);
        }

        // Add genesis tier consumable
        ConsumableItem essenceOfCreation = db.getConsumableById("TIER_GENESIS_POTION");
        if (essenceOfCreation != null) {
            addConsumableItem(essenceOfCreation);
        }
    }

    /**
     * Gets all equipped items.
     */
    public Array<Equipment> getEquippedItems() {
        return equippedItems;
    }

    /**
     * Gets the weapon currently equipped.
     */
    public Equipment getWeapon() {
        return equippedItems.get(Equipment.EquipmentType.WEAPON.ordinal());
    }

    /**
     * Gets the armor currently equipped.
     */
    public Equipment getArmor() {
        return equippedItems.get(Equipment.EquipmentType.ARMOR.ordinal());
    }

    /**
     * Gets the accessory currently equipped.
     */
    public Equipment getAccessory() {
        return equippedItems.get(Equipment.EquipmentType.ACCESSORY.ordinal());
    }

    /**
     * Equips an item, replacing any existing equipped item of the same type.
     * Returns the previously equipped item or null if none was equipped.
     */
    public Equipment equipItem(Equipment item) {
        if (item == null) return null;

        int slot = item.getType().ordinal();
        Equipment previousItem = equippedItems.get(slot);
        equippedItems.set(slot, item);

        // Remove the item from inventory if it was there
        inventoryItems.removeValue(item, true);

        return previousItem;
    }

    /**
     * Unequips an item of the specified type and adds it to inventory if there's space.
     * Returns true if successfully unequipped, false otherwise.
     */
    public boolean unequipItem(Equipment.EquipmentType type) {
        int slot = type.ordinal();
        Equipment item = equippedItems.get(slot);

        if (item == null) {
            return false; // Nothing to unequip
        }

        // Check if there's space in inventory
        if (inventoryItems.size >= MAX_INVENTORY_SLOTS) {
            return false; // No space in inventory
        }

        // Remove item from equipped slot
        equippedItems.set(slot, null);

        // Add to inventory
        inventoryItems.add(item);

        return true;
    }

    /**
     * Adds an item to the inventory. Returns true if successful, false if inventory is full.
     */
    public boolean addToInventory(Equipment item) {
        if (inventoryItems.size >= MAX_INVENTORY_SLOTS) {
            return false; // Inventory full
        }

        inventoryItems.add(item);
        return true;
    }

    /**
     * Adds a consumable item to the inventory or increases its quantity if it already exists.
     * If this is one of the first MAX_COMBAT_ITEMS consumables, also adds it to combat items.
     * Returns true if successful.
     */
    public boolean addConsumableItem(ConsumableItem item) {
        // Check if we already have this item
        for (ConsumableItem existingItem : consumableItems) {
            if (existingItem.getName().equals(item.getName())) {
                // Try to increase quantity
                if (existingItem.increaseQuantity(item.getQuantity())) {
                    return true;
                }
                // If we can't stack more, add as new item
                break;
            }
        }

        // Add as new item
        consumableItems.add(item);

        // If we have fewer than MAX_COMBAT_ITEMS in the combat items list,
        // automatically add this item to combat items too
        if (combatItems.size < MAX_COMBAT_ITEMS) {
            // Create a copy with a quantity of 1 to add to combat items
            ConsumableItem combatCopy = item.clone();
            combatCopy.decreaseQuantity(combatCopy.getQuantity() - 1); // Set to 1
            combatItems.add(combatCopy);
        }

        return true;
    }

    /**
     * Use a consumable item, reducing its quantity.
     * Returns true if successfully used.
     */
    public boolean useConsumableItem(ConsumableItem item) {
        if (item == null || !consumableItems.contains(item, true)) {
            return false;
        }

        if (item.decreaseQuantity(1)) {
            // If quantity becomes 0, remove the item
            if (item.getQuantity() <= 0) {
                consumableItems.removeValue(item, true);
            }
            return true;
        }

        return false;
    }

    /**
     * Gets all consumable items.
     */
    public Array<ConsumableItem> getConsumableItems() {
        return consumableItems;
    }

    /**
     * Gets all items selected for combat.
     */
    public Array<ConsumableItem> getCombatItems() {
        return combatItems;
    }

    /**
     * Checks if a consumable item is selected for combat.
     */
    public boolean isSelectedForCombat(ConsumableItem item) {
        for (ConsumableItem combatItem : combatItems) {
            if (combatItem.getName().equals(item.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Toggles a consumable item's selection for combat.
     * If selected, it will be unselected. If not selected and there's space, it will be selected.
     * Returns true if the selection state was changed.
     */
    public boolean toggleCombatSelection(ConsumableItem item) {
        // Check if already selected
        for (int i = 0; i < combatItems.size; i++) {
            ConsumableItem combatItem = combatItems.get(i);
            if (combatItem.getName().equals(item.getName())) {
                // Remove from combat items
                combatItems.removeIndex(i);
                return true;
            }
        }

        // If not selected and there's space, add it
        if (combatItems.size < MAX_COMBAT_ITEMS) {
            // Create a copy with quantity 1 for combat
            ConsumableItem combatCopy = item.clone();
            combatCopy.decreaseQuantity(combatCopy.getQuantity() - 1); // Set to 1
            combatItems.add(combatCopy);
            return true;
        }

        return false;
    }

    /**
     * Moves an item from inventory to storage.
     */
    public boolean moveToStorage(Equipment item) {
        if (!inventoryItems.contains(item, true)) {
            return false; // Item not in inventory
        }

        inventoryItems.removeValue(item, true);
        storageItems.add(item);
        return true;
    }

    /**
     * Moves an item from storage to inventory if there's space.
     */
    public boolean moveToInventory(Equipment item) {
        if (!storageItems.contains(item, true)) {
            return false; // Item not in storage
        }

        if (inventoryItems.size >= MAX_INVENTORY_SLOTS) {
            return false; // Inventory full
        }

        storageItems.removeValue(item, true);
        inventoryItems.add(item);
        return true;
    }

    /**
     * Gets items in the inventory.
     */
    public Array<Equipment> getInventoryItems() {
        return inventoryItems;
    }

    /**
     * Gets items in storage.
     */
    public Array<Equipment> getStorageItems() {
        return storageItems;
    }

    /**
     * Calculates the total attack bonus from all equipped items.
     */
    public int getTotalAttackBonus() {
        int total = 0;
        for (Equipment item : equippedItems) {
            if (item != null) {
                total += item.getAttackBonus();
            }
        }
        return total;
    }

    /**
     * Calculates the total defense bonus from all equipped items.
     */
    public int getTotalDefenseBonus() {
        int total = 0;
        for (Equipment item : equippedItems) {
            if (item != null) {
                total += item.getDefenseBonus();
            }
        }
        return total;
    }

    /**
     * Calculates the total max HP bonus from all equipped items.
     */
    public int getTotalMaxHPBonus() {
        int total = 0;
        for (Equipment item : equippedItems) {
            if (item != null) {
                total += item.getMaxHPBonus();
            }
        }
        return total;
    }

    /**
     * Calculates the total max MP bonus from all equipped items.
     */
    public int getTotalMaxMPBonus() {
        int total = 0;
        for (Equipment item : equippedItems) {
            if (item != null) {
                total += item.getMaxMPBonus();
            }
        }
        return total;
    }

    /**
     * Calculates the total crit rate bonus from all equipped items.
     */
    public float getTotalCritRateBonus() {
        float total = 0;
        for (Equipment item : equippedItems) {
            if (item != null) {
                total += item.getCritRateBonus();
            }
        }
        return total;
    }

    // Json.Serializable implementation
    @Override
    public void write(Json json) {
        // Write equipped items
        json.writeArrayStart("equippedItems");
        for (Equipment item : equippedItems) {
            json.writeValue(item);
        }
        json.writeArrayEnd();

        // Write inventory items
        json.writeArrayStart("inventoryItems");
        for (Equipment item : inventoryItems) {
            json.writeValue(item);
        }
        json.writeArrayEnd();

        // Write storage items
        json.writeArrayStart("storageItems");
        for (Equipment item : storageItems) {
            json.writeValue(item);
        }
        json.writeArrayEnd();

        // Write consumable items
        json.writeArrayStart("consumableItems");
        for (ConsumableItem item : consumableItems) {
            json.writeValue(item);
        }
        json.writeArrayEnd();

        // Write combat items
        json.writeArrayStart("combatItems");
        for (ConsumableItem item : combatItems) {
            json.writeValue(item);
        }
        json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        // Clear existing items
        equippedItems.clear();
        inventoryItems.clear();
        storageItems.clear();
        consumableItems.clear();
        combatItems.clear();

        // Initialize equipped slots with null values
        for (int i = 0; i < MAX_EQUIPPED_SLOTS; i++) {
            equippedItems.add(null);
        }

        // Read equipped items
        JsonValue equippedArray = jsonData.get("equippedItems");
        if (equippedArray != null) {
            int index = 0;
            for (JsonValue itemValue = equippedArray.child; itemValue != null && index < MAX_EQUIPPED_SLOTS;
                 itemValue = itemValue.next, index++) {
                if (!itemValue.isNull()) {
                    Equipment item = json.readValue(Equipment.class, itemValue);
                    if (item != null) {
                        equippedItems.set(item.getType().ordinal(), item);
                    }
                }
            }
        }

        // Read inventory items
        JsonValue inventoryArray = jsonData.get("inventoryItems");
        if (inventoryArray != null) {
            for (JsonValue itemValue = inventoryArray.child; itemValue != null; itemValue = itemValue.next) {
                Equipment item = json.readValue(Equipment.class, itemValue);
                if (item != null) {
                    if (inventoryItems.size < MAX_INVENTORY_SLOTS) {
                        inventoryItems.add(item);
                    } else {
                        // If inventory is full, move to storage
                        storageItems.add(item);
                    }
                }
            }
        }

        // Read storage items
        JsonValue storageArray = jsonData.get("storageItems");
        if (storageArray != null) {
            for (JsonValue itemValue = storageArray.child; itemValue != null; itemValue = itemValue.next) {
                Equipment item = json.readValue(Equipment.class, itemValue);
                if (item != null) {
                    storageItems.add(item);
                }
            }
        }

        // Read consumable items
        JsonValue consumablesArray = jsonData.get("consumableItems");
        if (consumablesArray != null) {
            for (JsonValue itemValue = consumablesArray.child; itemValue != null; itemValue = itemValue.next) {
                ConsumableItem item = json.readValue(ConsumableItem.class, itemValue);
                if (item != null) {
                    consumableItems.add(item);
                }
            }
        } else {
            // If no consumables in save file, add starter consumables
            addStarterItems();
        }

        // Read combat items
        JsonValue combatItemsArray = jsonData.get("combatItems");
        if (combatItemsArray != null) {
            for (JsonValue itemValue = combatItemsArray.child; itemValue != null && combatItems.size < MAX_COMBAT_ITEMS; itemValue = itemValue.next) {
                ConsumableItem item = json.readValue(ConsumableItem.class, itemValue);
                if (item != null) {
                    combatItems.add(item);
                }
            }
        } else {
            // If no combat items in save file, add the first MAX_COMBAT_ITEMS consumable items
            for (int i = 0; i < consumableItems.size && combatItems.size < MAX_COMBAT_ITEMS; i++) {
                ConsumableItem origItem = consumableItems.get(i);
                ConsumableItem combatCopy = origItem.clone();
                combatCopy.decreaseQuantity(combatCopy.getQuantity() - 1); // Set to 1
                combatItems.add(combatCopy);
            }
        }
    }
}
